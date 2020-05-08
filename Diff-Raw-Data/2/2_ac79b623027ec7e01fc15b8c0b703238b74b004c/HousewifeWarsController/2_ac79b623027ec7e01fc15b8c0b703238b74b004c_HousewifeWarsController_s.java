 package com.twoclams.hww.server;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTime;
 import org.joda.time.Minutes;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.twoclams.hww.server.model.DailyBonus.Reward;
 import com.twoclams.hww.server.model.House;
 import com.twoclams.hww.server.model.Housewife;
 import com.twoclams.hww.server.model.Husband;
 import com.twoclams.hww.server.model.OtherPlayerProfileResponse;
 import com.twoclams.hww.server.model.SimpleResponse;
 import com.twoclams.hww.server.model.SynchronizeResponse;
 import com.twoclams.hww.server.model.Wallet;
 import com.twoclams.hww.server.service.UsersService;
 import com.twoclams.hww.server.utils.DateUtils;
 
 @Controller
 public class HousewifeWarsController extends BaseController {
     private static final Log logger = LogFactory.getLog(HousewifeWarsController.class);
 
     @Autowired
     private UsersService userService;
 
     @RequestMapping(value = "/synchronize")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String synchronizeGame(@RequestParam(value = "papayaUserId") String papayaUserId, HttpServletRequest request)
             throws IOException {
         SynchronizeResponse response = userService.synchronizeGame(papayaUserId);
 
         return getDefaultSerializer().deepSerialize(response);
     }
 
     @RequestMapping(value = "/synchronizeGame")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String synchronizeGame(@RequestParam(value = "wife") String wifeJsonStr,
             @RequestParam(value = "husband") String husbandJsonStr,
             @RequestParam(value = "wallet") String walletJsonStr,
             @RequestParam(value = "passport") String passportJsonStr,
             @RequestParam(value = "house") String houseJsonStr, HttpServletRequest request) throws IOException {
         Husband husband = null;
         Housewife housewife = null;
         House house;
         try {
             husband = this.buildHusband(new JSONObject(husbandJsonStr));
         } catch (JSONException e) {
             logger.error("An error ocurred while processing husband json: " + husbandJsonStr, e);
         }
 
         try {
             logger.info("WifeJson: " + wifeJsonStr);
             housewife = this.buildWife(new JSONObject(wifeJsonStr));
             logger.info("WifeBuilt: " + housewife.toString());
         } catch (JSONException e) {
             logger.error("An error ocurred while processing wife json: " + wifeJsonStr, e);
         }
 
         try {
            houseJsonStr.replace("{row:,col:,height:,type:custom-floor,id:NoneNone,furnitureId:175},", "");
             house = this.buildHouse(new JSONObject(houseJsonStr));
             userService.synchronizeHouse(house.getPapayaUserId(), house);
         } catch (JSONException e) {
             logger.error("An error ocurred while processing house json: " + houseJsonStr, e);
         }
 
         try {
             JSONObject jsonWallet = new JSONObject(walletJsonStr);
             Wallet wallet = new Wallet(jsonWallet);
             userService.synchronizeWallet(wallet);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         SimpleResponse response = userService.registeUser(housewife, husband);
         return this.getDefaultSerializer().deepSerialize(response);
     }
 
     @RequestMapping(value = "/syncHusband")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String syncHusband(@ModelAttribute Husband husband, HttpServletRequest request) throws IOException {
         SimpleResponse response = userService.synchronizeHusband(husband);
         return getDefaultSerializer().deepSerialize(response);
     }
 
     @RequestMapping(value = "/syncWife")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String syncWife(@ModelAttribute Housewife housewife, HttpServletRequest request) throws IOException {
         housewife.setSkinTones(this.getSkinTone(request));
         SimpleResponse response = userService.synchronizeHousewife(housewife);
         return getDefaultSerializer().deepSerialize(response);
     }
 
     @RequestMapping(value = "/register")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String register(@RequestParam(value = "wife") String wifeJsonStr,
             @RequestParam(value = "husband") String husbandJsonStr, HttpServletRequest request) throws IOException {
         Husband husband = null;
         Housewife housewife = null;
         try {
             husband = this.buildHusband(new JSONObject(husbandJsonStr));
         } catch (JSONException e) {
             logger.error("An error ocurred while processing husband json: " + husbandJsonStr, e);
         }
 
         try {
             logger.info("WifeJson: " + wifeJsonStr);
             housewife = this.buildWife(new JSONObject(wifeJsonStr));
             logger.info("WifeBuilt: " + housewife.toString());
         } catch (JSONException e) {
             logger.error("An error ocurred while processing wife json: " + wifeJsonStr, e);
         }
         SimpleResponse response = userService.registeUser(housewife, husband);
         return this.getDefaultSerializer().deepSerialize(response);
     }
 
     @RequestMapping(value = "/getOtherPlayerProfile")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getOtherPlayerProfile(@RequestParam(value = "userId") String userId, HttpServletRequest request)
             throws IOException, JSONException {
         OtherPlayerProfileResponse response = userService.getOtherPlayerProfile(userId);
 
         return this.getDefaultSerializer().include("messages").include("skinTone").deepSerialize(response);
     }
 
     @RequestMapping(value = "/getCurrentDateAndTick")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getCurrentDateAndTick(HttpServletRequest request) throws IOException, JSONException {
         DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
         DateTime start = new DateTime(2012, 1, 1, 0, 0, 0, 0);
         Date today = new Date();
         DateTime end = new DateTime(today.getTime());
         Minutes minutes = Minutes.minutesBetween(start, end);
 
         Map<String, Object> todayResponse = new HashMap<String, Object>();
         todayResponse.put("currentTick", new Integer(minutes.getMinutes()));
         todayResponse.put("currentDay", dateFormatter.format(today));
         return this.getDefaultSerializer().deepSerialize(todayResponse);
     }
 
     @RequestMapping(value = "/getDailyBonus")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getDailyBonus(@RequestParam(value = "papayaUserId") String papayaUserId, HttpServletRequest request)
             throws IOException, JSONException {
         Map<String, Object> response = new HashMap<String, Object>();
         DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
 
         Reward reward = userService.getDailyBonus(papayaUserId);
         int isReward = 0;
         if (reward != null) {
             isReward = 1;
             response.put("reward.count", reward.getCount());
             response.put("reward.amount", reward.getAmount());
             response.put("reward.currency", reward.getCurrency());
         }
 
         response.put("reward", isReward);
         response.put("currentDay", dateFormatter.format(DateUtils.getCurrentDay()));
         return this.getDefaultSerializer().deepSerialize(response);
     }
 
 }
