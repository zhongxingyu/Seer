 package web.controller;
 
 import java.io.IOException;
 import java.security.Principal;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import web.model.Property;
 import web.model.Reservation;
 import web.model.User;
 import web.service.CommentService;
 import web.service.EvaluationService;
 import web.service.PropertyOptionsService;
 import web.service.PropertyService;
 import web.service.ReservationService;
 import web.service.UserService;
 import web.utils.StaticMap;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>, Romain <ro.foncier@foncier.com>
  */
 @Controller
 public class UserController {
 
     @Autowired
     private UserService userService;
     @Autowired
     private PropertyService propertyService;
     @Autowired
     private PropertyOptionsService optionsService;
     @Autowired
     private CommentService commentService;
     @Autowired
     private ReservationService reservationService;
     @Autowired
     private EvaluationService evaluationService;
 
     // Security
     @Autowired
     @Qualifier("authenticationManager")
     AuthenticationManager authenticationManager;
     
     /**
      * Get all informations about the user given in parameter. Only for logged users.
      * @param username, model, current
      * @return user profile
      */
     @RequestMapping(value = "/s/{username}", method = RequestMethod.GET)
     public String userView(@PathVariable String username, Model model, Principal current, HttpServletRequest request, HttpServletResponse response) {
         // Specified if the current page is active and set the tab in the navbar.
         model.addAttribute("home", true);
         
         // Get User
         if (current != null) {
             User user = userService.findByUsername(username);
             model.addAttribute("current", userService.findByUsername(current.getName()));
             model.addAttribute("isUserCurrent", current.getName().equals(username));
             
             // Get all properties for the user to display
             List<Property> properties = propertyService.findProperty(user);
             Integer propertyCount = properties.size();
             Integer evaluation = 0;
             Integer nbEval = 0;
             for (int i = 0; i < properties.size(); i++) {
                 if (properties.get(i).getNote() != null) {
                     evaluation += properties.get(i).getNote();
                     nbEval++;
                 }
             }
             evaluation =  (nbEval > 0) ? evaluation / nbEval : -1;
             
             String pathMap = (!properties.isEmpty()) ? StaticMap.buildMapURL(properties, null) : null;
 
             model.addAttribute("user", user);
             model.addAttribute("propertyCount", propertyCount);
             model.addAttribute("map", pathMap);
             model.addAttribute("evaluation", evaluation);
 
             return "user";
         }
         
         // Return a status 401 : Unauthorize.
         try {
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         return null;
        
     }
     
     /**
      * Update informations about the user given in parameter. Only the owner of this profile 
      * can update its data. If not, return a 401 unauthorized error code.
      * @param username, model, current
      * @return update user profile
      */
     @RequestMapping(value = "/s/{username}/update", method = RequestMethod.POST)
     public String updateUser(@PathVariable String username, final User user, Model model, Principal current, 
             HttpServletRequest request, HttpServletResponse response) {
         if (current != null && current.getName().equals(username)) {
             User u_log = userService.findByUsername(username);
             u_log.setName(user.getName());
             u_log.setUsername(user.getUsername());
             u_log.setFirstname(user.getFirstname());
             u_log.setEmail(user.getEmail());
             userService.saveUser(u_log);
             
             return "redirect:/s/" + username;
         }
         
         // Return a status 401 : Unauthorize.
         try {
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         return null;
     }
 
     /**
      * Delete informations about the user given in parameter. Only for logged users.
      * @param username, model, current
      * @return update user profile
      */
     @RequestMapping(value = "/s/{username}/delete", method = RequestMethod.POST)
     public String deleteUser(@PathVariable String username, Model model, Principal current, HttpServletRequest request, HttpServletResponse response) {
         if (current != null && current.getName().equals(username)) {
             User user = userService.findByUsername(username);
             
             //Only reservations for this user can be deleted
             List<Reservation> reservations = reservationService.findByUser(user);
             for (int i = 0; i < reservations.size(); i++) {
                 reservationService.deleteReservation(reservations.get(i).getId());
             }
             
             // Set this user as enabled. In this way, he should not be able to login on the platform.
             user.setEnabled(Boolean.FALSE);
             userService.saveUser(user);
             
             // Logout user and remove session & context
             try {
                 HttpSession session = request.getSession(false);
                 if (session != null) {
                     session.invalidate();
                 }
             SecurityContextHolder.getContext().setAuthentication(null);
             SecurityContextHolder.clearContext();
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
             
             return "redirect:/";
         }
         
         // Return a status 401 : Unauthorize.
         try {
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         return null;
     }
 
     /**
      * Get all informations about the properties of the user given in parameter. Only the owner of this profile 
      * can access to these data. If not, return a 401 unauthorized error code.
      * @param username, model, current
      * @return user profile
      */
     @RequestMapping(value = "/s/{username}/properties", method = RequestMethod.GET)
     public String userPropertiesView(@PathVariable String username, Model model, Principal current, HttpServletRequest request, HttpServletResponse response) {
         // Specified if the current page is active and set the tab in the navbar.
         model.addAttribute("home", true);
         
         if (current != null) {
             User user = userService.findByUsername(username);
             model.addAttribute("current", userService.findByUsername(current.getName()));
             
             // Get all properties
             List<Property> properties = propertyService.findProperty(user);
 
            String pathMap = (!properties.isEmpty()) ? StaticMap.buildMapURL(properties, null) : null;
 
             model.addAttribute("user", user);
             model.addAttribute("properties", properties);
             model.addAttribute("map", pathMap);
 
             return "user_properties";
         }
         
         // Return a status 401 : Unauthorize.
         try {
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         return null;
     }
     
     /** Methods used to return user data in Modal **/
     
     // Mapping for dynamically load the user form within modal during the edition operations
     @RequestMapping(value = "/s/{username}/modal", method = RequestMethod.GET)
     public String userModalView(@PathVariable String username, Model model, Principal current, HttpServletRequest request, HttpServletResponse response) {
         if (current != null && current.getName().equals(username)) {
             User u_log = userService.findByUsername(username);
             model.addAttribute("user", u_log);
         
             return "user_modal";
         }
         
         // Return a status 401 : Unauthorize.
         try {
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         return null;
     }
 }
