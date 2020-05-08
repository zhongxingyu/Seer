 package no.helsenorge.nipen.heartRate;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.List;
 
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
@Controller
 public class HeartRateController {
 
     @Autowired
     private HeartRateService heartRateService;
 
     @RequestMapping(value="/api/human/heart_rates", method =  GET)
     @ResponseBody
     public List<HeartRate> getHeartRates(@RequestParam(required = true) Integer userId) {
         return heartRateService.getHeartRates(userId);
     }
 
     @ResponseStatus(HttpStatus.OK)
     @RequestMapping(value = "/api/human/heart_rate", method = POST)
     @ResponseBody
     public String addHeartRate(@RequestBody String heartRate){
        // return heartRateService.insertHeartRate((new HeartRateJsonParser(heartRate).toHeartRate()));
         return heartRate;
     }
 
     // For testing
     @RequestMapping(value="/api/human/test", method =  GET)
     @ResponseBody
     public String getHello() {
         return "Hello World!";
     }
 }
