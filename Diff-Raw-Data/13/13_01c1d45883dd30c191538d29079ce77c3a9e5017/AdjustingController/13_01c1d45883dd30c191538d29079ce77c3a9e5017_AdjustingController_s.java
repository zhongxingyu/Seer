 package edu.unlv.kilo.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import edu.unlv.kilo.domain.ItemEntity;
 import edu.unlv.kilo.domain.UserData;
 
 @RequestMapping("/adjusting/**")
 @Controller
 public class AdjustingController {
     @RequestMapping
     public String index(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
     	UserData.debugEnsureUserData(session);
 		UserData userData = UserData.getSessionUserData(session);
 		
 		String idString = request.getParameter("id");
 		try {
 			long id = Long.parseLong(idString);
 			ItemEntity item = ItemEntity.findItemEntity(id);
 			
 	        return "adjusting/item";
 		} catch (NumberFormatException e) {
 			// Do nothing
 		} catch (NullPointerException e) {
 			// Do nothing
 		}
 		
 		userData.buildAdjustingModel(modelMap);
 		modelMap.addAttribute("test", "lame");
     	
         return "adjusting/index";
     }
 }
