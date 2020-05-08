 package drinkcounter.web.controllers.api;
 
 import com.csvreader.CsvWriter;
 import com.google.common.base.Charsets;
 import drinkcounter.DrinkCounterService;
 import drinkcounter.UserService;
 import drinkcounter.authentication.AuthenticationChecks;
 import drinkcounter.model.Drink;
 import drinkcounter.model.User;
 import drinkcounter.util.PartyMarshaller;
 import drinkcounter.web.controllers.ui.AuthenticationController;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.servlet.http.HttpSession;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  * @author Toni
  */
 @Controller
 public class APIController {
 
     private static final Logger log = LoggerFactory.getLogger(APIController.class);
 
     @Autowired
     private PartyMarshaller partyMarshaller;
 
     @Autowired
     private DrinkCounterService drinkCounterService;
     
     @Autowired
     private AuthenticationChecks authenticationChecks;
 
     @Autowired
     private UserService userService;
 
     @RequestMapping("/parties/{partyId}")
     public @ResponseBody byte[] printXml(HttpSession session, @PathVariable String partyId) throws IOException{
         int id = Integer.parseInt(partyId);
         authenticationChecks.checkRightsForParty((String)session.getAttribute(AuthenticationController.OPENID), id);
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         partyMarshaller.marshall(id, baos);
         byte[] bytesXml = baos.toByteArray();
         return bytesXml;
     }
 
     @RequestMapping("/users/{userId}/show-drinks")
     public @ResponseBody byte[] showDrinks(HttpSession session, @PathVariable String userId) throws IOException{
         int id = Integer.parseInt(userId);
         authenticationChecks.checkLowLevelRightsToUser((String)session.getAttribute(AuthenticationController.OPENID), id);
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         partyMarshaller.marshallDrinks(id, baos);
         byte[] bytesXml = baos.toByteArray();
         return bytesXml;
     }
 
     @RequestMapping("/users/{userId}/add-drink")
     public @ResponseBody String addDrink(HttpSession session, @PathVariable String userId){
         int id = Integer.parseInt(userId);
         authenticationChecks.checkHighLevelRightsToUser((String)session.getAttribute(AuthenticationController.OPENID), id);
         int drinkId = drinkCounterService.addDrink(id);
         User user = userService.getUser(id);
         return Integer.toString(drinkId);
     }
 
     @RequestMapping("/users/{userId}/remove-drink/{drinkId}")
     public @ResponseBody String removeDrinkFromUser(HttpSession session, @PathVariable String userId, @PathVariable String drinkId){
         int userIdInt = Integer.parseInt(userId);
         authenticationChecks.checkHighLevelRightsToUser((String)session.getAttribute(AuthenticationController.OPENID), userIdInt);
         int drinkIdInt = Integer.parseInt(drinkId);
         drinkCounterService.removeDrinkFromUser(userIdInt, drinkIdInt);
         log.info(String.format("Removed drink %d from user %d.", drinkIdInt, userIdInt));
         return "";
     }
     
     @RequestMapping("/users/{userId}")
     public @ResponseBody byte[] userXml(HttpSession session, @PathVariable String userId) throws IOException{
         int id = Integer.parseInt(userId);
         authenticationChecks.checkHighLevelRightsToUser((String)session.getAttribute(AuthenticationController.OPENID), id);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         partyMarshaller.marshallUser(id, baos);
         byte[] bytesXml = baos.toByteArray();
         return bytesXml;
     }
     
     @RequestMapping("/users/{userId}/drinks")
     public ResponseEntity<byte[]> drinkHistory(HttpSession session, @PathVariable String userId) throws IOException{
         int id = Integer.parseInt(userId);
         authenticationChecks.checkLowLevelRightsToUser((String)session.getAttribute(AuthenticationController.OPENID), id);
 
         User user = userService.getUser(id);
         List<Drink> drinks = user.getDrinks();
 
         Map<String, Integer> drinksPerDay = new LinkedHashMap<String, Integer>();
         String format = "YYYY-MM-dd";
 
         for (Drink d : drinks) {
             // TODO time zones?
             DateTime dt = new DateTime(d.getTimeStamp());
             String s = dt.toString(format);
 
             Integer i = 0;
             if (drinksPerDay.containsKey(s))
                 i = drinksPerDay.get(s);
             i += 1;
             drinksPerDay.put(s, i);
         }
 
         String today = new DateTime().toString(format);
         if (!drinksPerDay.containsKey(today))
             drinksPerDay.put(today, 0);
 
         HttpHeaders headers = new HttpHeaders();
         headers.set("Content-Type", "text/plain;charset=utf-8");
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         CsvWriter csvWriter = new CsvWriter(new OutputStreamWriter(baos, Charsets.UTF_8), ',');
         csvWriter.writeRecord(new String[]{"Time", "Drinks"});
 
         for (Entry<String, Integer> p : drinksPerDay.entrySet()) {
             DateTime dt = new DateTime(p.getKey());
             csvWriter.writeRecord(new String[]{Long.toString(dt.getMillis()), p.getValue().toString()});
         }
 
         csvWriter.close();
         byte[] bytes = baos.toByteArray();
         return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
     }
 
     @RequestMapping("/users/{userId}/show-history")
     public ResponseEntity<byte[]> showHistory(HttpSession session, @PathVariable String userId) throws IOException{
         int id = Integer.parseInt(userId);
         authenticationChecks.checkHighLevelRightsToUser((String)session.getAttribute(AuthenticationController.OPENID), id);
         HttpHeaders headers = new HttpHeaders();
         headers.set("Content-Type", "text/plain;charset=utf-8");
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         CsvWriter csvWriter = new CsvWriter(new OutputStreamWriter(baos, Charsets.UTF_8), ',');
         csvWriter.writeRecord(new String[]{"Time", "Alcohol"});
 
         User user = userService.getUser(id);
         int intervalMs = 2 * 60 * 1000;
         
         DateTime now = new DateTime();
         DateTime start = now.minusMinutes(300);
         List<Float> history = user.getPromillesAtInterval(start.toDate(), now.toDate(), intervalMs);
 
         DateTime time = start;
         for (Float f : history) {
             csvWriter.writeRecord(
                     new String[]{Long.toString(time.getMillis()), Float.toString(f)}
             );
             time = time.plusMillis(intervalMs);
         }
         csvWriter.close();
         byte[] bytes = baos.toByteArray();
         return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
     }
 }
