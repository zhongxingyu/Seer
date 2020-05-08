 package web.controller;
 
 import java.security.Principal;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import web.model.Comment;
 import web.model.Evaluation;
 import web.model.Property;
 import web.model.PropertyOptions;
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
  * @author Bernard <bernard.debecker@gmail.com>
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
 
     @RequestMapping(value = "/s/account/{username}", method = RequestMethod.GET)
     public String userView(@PathVariable String username, Model model, Principal current) {
 
         // Get User
         User user = userService.findByUsername(username);
         Integer propertyCount = propertyService.findProperty(user).size();
         List<Property> properties = propertyService.findProperty(user);
         Integer evaluation = 0;
         Integer nbEval = 0;
         for (int i = 0; i < properties.size(); i++) {
             if (properties.get(i).getNote() != null) {
                 evaluation = evaluation + properties.get(i).getNote();
                 nbEval++;
             }
         }
         if (nbEval > 0) {
             evaluation = evaluation / nbEval;
         } else {
             evaluation = -1;
         }
         String pathMap;
         pathMap = StaticMap.buildMapURL(properties, null);
 
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             model.addAttribute("current", u_log);
             Boolean isUserCurrent = (u_log.getId() == null ? user.getId() == null : u_log.getId().equals(user.getId()));
             model.addAttribute("isUserCurrent", isUserCurrent);
         }
 
         model.addAttribute("user", user);
         model.addAttribute("propertyCount", propertyCount);
         model.addAttribute("map", pathMap);
         model.addAttribute("evaluation", evaluation);
 
         return "user";
     }
 
     @RequestMapping(value = "/s/account/delete", method = RequestMethod.POST)
     public String deleteUser(final String id, Model model, Principal current) {
         User user = userService.findById(id);
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             if (u_log.getId().equals(user.getId())) {
 //                user.setEnabled(!user.isEnabled());
 //                userService.saveUser(user);
                 List<Property> properties = propertyService.findProperty(user);
                 for (int i = 0; i < properties.size(); i++) {
                     Property property = properties.get(i);
                     PropertyOptions options = optionsService.findByProperty(property);
                     if (options != null) {
                         optionsService.deletePropertyOptions(options.getId());
                     }
                     List<Comment> comments = commentService.findByProperty(property);
                     for (int j = 0; j < comments.size(); j++) {
                         Comment comment = comments.get(j);
                         commentService.deleteComment(comment.getId());
                     }
                     List<Reservation> reservations = reservationService.findByProperty(property);
                     for (int j = 0; j < reservations.size(); j++) {
                         Reservation reservation = reservations.get(j);
                         reservationService.deleteReservation(reservation.getId());
                     }
                     Evaluation evaluation = evaluationService.findByProperty(property);
                     if (evaluation != null) {
                         evaluationService.deleteEvaluation(evaluation.getId());
                     }
                     propertyService.deleteProperty(property.getId());
                 }
             }
         }
         return "redirect:/";
     }
 
     @RequestMapping(value = "/s/account/update", method = RequestMethod.POST)
     public String updateUser(final User user, Model model, Principal current) {
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             u_log.setName(user.getName());
             u_log.setUsername(user.getUsername());
             u_log.setFirstname(user.getFirstname());
             u_log.setEmail(user.getEmail());
             userService.saveUser(u_log);
         }
         return "redirect:/s/account/" + user.getUsername();
     }
 
     @RequestMapping(value = "/s/account/{username}/properties", method = RequestMethod.GET)
     public String userPropertiesView(@PathVariable String username, Model model, Principal current) {
 
         // Get User
         User user = userService.findByUsername(username);
         List<Property> properties = propertyService.findProperty(user);
 
         String pathMap;
        pathMap = StaticMap.buildMapURL(properties, null);
 
 
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             model.addAttribute("current", u_log);
         }
         model.addAttribute("user", user);
         model.addAttribute("properties", properties);
         model.addAttribute("map", pathMap);
 
 
 
         return "user_properties";
     }
 }
