 package web.controller;
 
 import com.javadocmd.simplelatlng.LatLng;
 import java.security.Principal;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import web.model.Comment;
 import web.model.Evaluation;
 import web.model.Property;
 import web.model.PropertyOptions;
 import web.model.PropertyType;
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
 public class PropertyController {
 
     @Autowired
     private PropertyService propertyService;
     @Autowired
     private PropertyOptionsService optionsService;
     @Autowired
     private CommentService commentService;
     @Autowired
     private UserService userService;
     @Autowired
     private ReservationService reservationService;
     @Autowired
     private EvaluationService evaluationService;
     
     
     /**
      * Get the property view
      * @param id the property id
      * @param model the model that receive the data to generate the template
      * @param current the current user
      * @return the property String that gonna call the property page.
      */
     @RequestMapping(value = "/property/{id}", method = RequestMethod.GET)
     public String propertyView(@PathVariable Integer id, Model model, Principal current) {
 
 //        Get Property by its id
         Property property = propertyService.findById(id);
 //        Get option of this property
         PropertyOptions options = optionsService.findByProperty(property);
 //        Get the comments of this property
         List<Comment> comments = commentService.findByProperty(property);
 //        Extract the note from the property object
         Integer evaluation = property.getNote();
 //        Generate the static map url based on the latitude and longitude of the property
        String pathMap = StaticMap.buildMapURL(property.getCoordinates(), null);
         List<String> pictures = new LinkedList<String>();
         for (int i = 1; i < 4; i++) {
             pictures.add(new Integer(i).toString() + ".jpg");
         }
 //        If there the user calling the page is logged
         if (current != null) {
 //            Find the user by its username
             User u_log = userService.findByUsername(current.getName());
 //            Add the current value of the model with the user
             model.addAttribute("current", u_log);
 //            Check if the owner of the property is the current user
             Boolean isOwnerCurrent = (u_log.getId() == null ? property.getOwner().getId() == null : u_log.getId().equals(property.getOwner().getId()));
             model.addAttribute("isOwnerCurrent", isOwnerCurrent);
         }
 //        Add the attributes to the model
         model.addAttribute("property", property);
         model.addAttribute("options", options);
         model.addAttribute("evaluation", evaluation);
         model.addAttribute("comments", comments);
         model.addAttribute("map", pathMap);
         model.addAttribute("pictures", pictures);
 
         return "property";
     }
 
     /**
      * Get the new property view
      * @param model the model that receive the data to generate the template
      * @param current the current user
      * @return the new property String that gonna call the property page.
      */
     @RequestMapping(value = "/s/property/new", method = RequestMethod.GET)
     public String newView(Model model, Principal current) {
 //        Create a new Property
         Property property = new Property();
 //        Create a new PropertyOption
         PropertyOptions options = new PropertyOptions();
 //        Twist to get the rentPeriodStart and Stop
         String rentStart = null;
         String rentStop = null;
 //        Get the different existing types of property
         List<PropertyType> types = Arrays.asList(PropertyType.values());
 //        If there the user calling the page is logged
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             model.addAttribute("current", u_log);
         }
 //        Add the attributes to the model
         model.addAttribute("property", property);
         model.addAttribute("options", options);
         model.addAttribute("types", types);
         model.addAttribute("rentStart", rentStart);
         model.addAttribute("rentStop", rentStop);
         return "new_property";
     }
 
     /**
      * POST new property
      * @param property the property object
      * @param options the options
      * @param rentStart the String with the rent start date
      * @param rentStop the String with the rent stop date
      * @param bindingResult 
      * @param model the model that receive the data to generate the template
      * @param current the current user
      * @return 
      */
     @RequestMapping(value = "/s/property/new", method = RequestMethod.POST)
     public String saveProperty(final Property property, final PropertyOptions options, final String rentStart, final String rentStop,
             final BindingResult bindingResult, final Model model, Principal current) {
 
         if (current != null) {
             property.setOwner(userService.findByUsername(current.getName()));
         }
         DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
         DateTime rentStartTmp = formatter.parseDateTime(rentStart + " 00:00:00");
         DateTime rentStopTmp = formatter.parseDateTime(rentStop + " 00:00:00");
         property.setRentPeriodStart(rentStartTmp.toLocalDateTime());
         property.setRentPeriodStop(rentStopTmp.toLocalDateTime());
 
 //        handle new property
         propertyService.saveProperty(property);
         options.setProperty(property);
         optionsService.savePropertyOptions(options);
         return "redirect:/property/" + property.getId();
     }
 
     @RequestMapping(value = "/s/property/update", method = RequestMethod.POST)
     public String updateProperty(final Property modifProperty, final PropertyOptions modifOptions, final String rentStart, final String rentStop,
             final BindingResult bindingResult, final Integer id, final Model model, Principal current) {
         Property property = propertyService.findById(id);
         property.setAddress(modifProperty.getAddress());
         property.setCity(modifProperty.getCity());
         property.setCountry(modifProperty.getCountry());
         property.setPrice(modifProperty.getPrice());
         property.setRooms(modifProperty.getRooms());
         property.setShortDesc(modifProperty.getShortDesc());
         property.setTitle(modifProperty.getTitle());
         property.setType(modifProperty.getType());
         property.setLongDesc(modifProperty.getLongDesc());
         DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
         DateTime rentStartTmp = formatter.parseDateTime(rentStart + " 00:00:00");
         DateTime rentStopTmp = formatter.parseDateTime(rentStop + " 00:00:00");
         property.setRentPeriodStart(rentStartTmp.toLocalDateTime());
         property.setRentPeriodStop(rentStopTmp.toLocalDateTime());
 
         PropertyOptions options = optionsService.findByProperty(property);
         options.setLaundry(modifOptions.getLaundry());
         options.setParking(modifOptions.getParking());
         options.setSwimmingPool(modifOptions.getSwimmingPool());
         options.setWifi(modifOptions.getWifi());
 //        handle new property
         propertyService.saveProperty(property);
         optionsService.savePropertyOptions(options);
         return "redirect:/property/" + property.getId();
     }
 
     @RequestMapping(value = "/s/property/delete", method = RequestMethod.POST)
     public String deleteProperty(final Integer id, Model model, Principal current) {
         Property property = propertyService.findById(id);
         String username = property.getOwner().getUsername();
 
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             if (u_log.getId().equals(property.getOwner().getId())) {
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
 
         return "redirect:/s/account/" + username + "/properties";
     }
 }
