 package smartpool.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import smartpool.domain.Buddy;
 import smartpool.domain.JoinRequest;
 import smartpool.persistence.dao.JoinRequestDao;
 import smartpool.service.BuddyService;
 
 import javax.servlet.http.HttpServletRequest;
 
 @Controller
 public class JoinCarPoolController {
     private BuddyService buddyService;
     String userName;
 
     @Autowired
     public JoinCarPoolController(BuddyService buddyService) {
         this.buddyService = buddyService;
     }
 
     @RequestMapping(value = "carpool/{name}/join", method = RequestMethod.GET)
     public String getUserDetails(@PathVariable String name, ModelMap model, HttpServletRequest request){
         String carpoolName = name;
         userName = buddyService.getUserNameFromCAS(request);
         Buddy buddy = buddyService.getBuddy(userName);
         model.put("buddy",buddy);
         model.put("carpoolName",carpoolName);
         model.put("casUserName", userName);
         return "carpool/joinRequest";
     }
 
     @RequestMapping(value = "carpool/{name}/join", method = RequestMethod.POST)
     public String submitUserDetails(@PathVariable String name, @ModelAttribute("request")JoinRequest joinRequest, ModelMap model){
         JoinRequestDao joinRequestDao=new JoinRequestDao();
        joinRequest.setCarpoolName(name);
         joinRequestDao.sendJoinRequest(joinRequest);
         model.put("request",joinRequest);
         return "redirect:../../carpool/"+name;
     }
 }
