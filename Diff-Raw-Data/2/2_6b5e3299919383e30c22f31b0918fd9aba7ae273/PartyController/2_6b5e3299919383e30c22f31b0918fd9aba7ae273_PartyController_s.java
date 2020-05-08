 package drinkcounter.web.controllers.ui;
 
 import drinkcounter.DrinkCounterService;
 import drinkcounter.UserService;
 import drinkcounter.authentication.AuthenticationChecks;
 import drinkcounter.model.Party;
 import drinkcounter.model.User;
 import javax.servlet.http.HttpSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 
 /**
  *
  * @author Toni
  */
 @Controller
 public class PartyController {
 
     @Autowired private DrinkCounterService drinkCounterService;
     @Autowired private UserService userService;
 
     @Autowired private AuthenticationChecks authenticationChecks;
 
     @RequestMapping("/viewParty")
     public ModelAndView viewParty(HttpSession session, @RequestParam("id") String partyId, @RequestParam(value="kick", required=false) String toKick){
         String openId = (String)session.getAttribute(AuthenticationController.OPENID);
 
         int pid = Integer.parseInt(partyId);
         authenticationChecks.checkRightsForParty(openId, pid);
         
         if (toKick != null)
             drinkCounterService.unlinkUserFromParty(Integer.parseInt(toKick), pid);
         
         ModelAndView mav = new ModelAndView();
         mav.setViewName("party");
         mav.addObject("party", drinkCounterService.getParty(pid));
         mav.addObject("allUsers", userService.listUsers());
         mav.addObject("users", drinkCounterService.listUsersByParty(pid));
         return mav;
     }
 
     @RequestMapping("/partytouch")
     public ModelAndView partyTouch(HttpSession session, @RequestParam("id") String partyId){
         String openId = (String)session.getAttribute(AuthenticationController.OPENID);
         int pid = Integer.parseInt(partyId);
         authenticationChecks.checkRightsForParty(openId, pid);
         User user = userService.getUserByOpenId(openId);
 
         ModelAndView mav = new ModelAndView();
         mav.setViewName("partytouch");
         mav.addObject("party", drinkCounterService.getParty(pid));
         mav.addObject("user", user);
         return mav;
     }
 
     @RequestMapping("/addParty")
     public String addParty(HttpSession session, @RequestParam("name") String partyName, @RequestParam("userId") String userId){
         String openId = (String)session.getAttribute(AuthenticationController.OPENID);
         int uid = Integer.parseInt(userId);
         authenticationChecks.checkLowLevelRightsToUser(openId, uid);
         
         Party party = drinkCounterService.startParty(partyName);
         drinkCounterService.linkUserToParty(uid, party.getId());
         return "redirect:partytouch?id="+party.getId();
     }
    
     @RequestMapping("/addAnonymousUser")
     public String addAnonymousUser(HttpSession session,
             @RequestParam("partyId") String partyId,
             @RequestParam("name") String name,
             @RequestParam("sex") String sex,
             @RequestParam("weight") float weight){
         String openId = (String)session.getAttribute(AuthenticationController.OPENID);
         int pid = Integer.parseInt(partyId);
         authenticationChecks.checkRightsForParty(openId, pid);
         
         User user = new User();
         user.setName(name);
         user.setSex(User.Sex.valueOf(sex));
         user.setWeight(weight);
         user.setGuest(true);
         userService.addUser(user);
         drinkCounterService.linkUserToParty(user.getId(), pid);
         return "redirect:partytouch?id=" + partyId;
     }
     
     @RequestMapping("/linkUserToParty")
     public String linkUserToParty(HttpSession session, @RequestParam("partyId") String partyId,
             @RequestParam("userId") String userId){
                 
         String openId = (String)session.getAttribute(AuthenticationController.OPENID);
         int pid = Integer.parseInt(partyId);
         int uid = Integer.parseInt(userId);
         authenticationChecks.checkRightsForParty(openId, pid);
        authenticationChecks.checkHighLevelRightsToUser(openId, uid);
         drinkCounterService.linkUserToParty(uid, pid);
         return "redirect:partytouch?id="+partyId;
     }
 
     @RequestMapping("/removeUserFromParty")
     public String removeUserFromParty(HttpSession session, @RequestParam("partyId") String partyId,
             @RequestParam("userId") String userId){
                 
         String openId = (String)session.getAttribute(AuthenticationController.OPENID);
         int pid = Integer.parseInt(partyId);
         int uid = Integer.parseInt(userId);
         authenticationChecks.checkRightsForParty(openId, pid);
         authenticationChecks.checkHighLevelRightsToUser(openId, uid);
         drinkCounterService.unlinkUserFromParty(uid, pid);
         
         // TODO: return to where you came from
         return "redirect:user";
     }
 }
