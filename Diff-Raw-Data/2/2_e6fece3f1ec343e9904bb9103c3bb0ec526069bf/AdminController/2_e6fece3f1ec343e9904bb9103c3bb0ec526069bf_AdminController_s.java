 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.spring.controller;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringBufferInputStream;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import org.apache.commons.io.IOUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import wad.spring.domain.Measurement;
 import wad.spring.domain.MeasurementForm;
 import wad.spring.domain.Place;
 import wad.spring.service.MeasurementService;
 import wad.spring.service.PlaceService;
 import wad.spring.service.UserService;
 
 /**
  * Ohjaa adminille kohdistuvat pyynnöt (sovellus/admin/jotain) oikeille näkymille ja kutsuu tarvittavat toiminnalisuudet
  * @author tonykovanen
  */
 
 @Controller
 @RequestMapping("admin")
 public class AdminController {
     @Autowired
     UserService userService;
     
     @Autowired
     PlaceService placeService;
     
     @Autowired
     MeasurementService measurementService;
     
     /**
      * Redirects all non-defined addresses form admin/something to home
      * @param principal Java security object that gives access to authenticated username
      * @param model Object which enables tranferring attributes from controller to view
      * @return Homepage view
      */
     @RequestMapping("/*")
     public String adminHome(Principal principal, Model model) {
         model.addAttribute("user", userService.findByUsername(principal.getName()));
         return "admin/home";
     }
     /**
      * Lists all places and provides navigation to see individual place information and shows form to create new places
      * @param model Object which enables tranferring attributes from controller to view
      * @return Places view
      */
     @RequestMapping(value = "/places", method = RequestMethod.GET)
     public String showPlaces(Model model) {
         model.addAttribute("place", new Place());
         model.addAttribute("places", placeService.findAll());
         return "admin/places";
     }
     /**
      * Retrieves a POST HTTP method and creates a new place and saves it to database if validation successful
      * @param place The new Place object retrieved from form data
      * @param result Validation result from creating object, see Place.class
      * @param model Object which enables tranferring attributes from controller to view
      * @return Redirects to admin/places
      */
     @RequestMapping(value = "/places", method = RequestMethod.POST)
     public String addPlace(@Valid @ModelAttribute Place place, BindingResult result, Model model) {
         if (result.hasErrors()) {
             model.addAttribute("places", placeService.findAll());
             return "admin/places";
         }
         if (placeService.findByName(place.getName()) != null) {
             model.addAttribute("message", "A place of this name already exists");
             return "troubleshooting";
             
         }
         place.setMeasurements(new ArrayList<Measurement>());
         placeService.save(place);
         return "redirect:/admin/places";
     }
     /**
      * Shows individual places information and provides a form for changing place informaton as well as deletion of place
      * @param placeId Id of the place that is viewed
      * @param model Object which enables tranferring attributes from controller to view
      * @return Place view
      */
     @RequestMapping(value = "places/{placeId}", method = RequestMethod.GET)
     public String showPlaceInformation(@PathVariable Long placeId, Model model) {
         Place place = placeService.findOne(placeId);
         
         if (place == null) {
             model.addAttribute("message", "Requested place does not exist");
             return "troubleshooting";
         }
         
         model.addAttribute("place", place);
         model.addAttribute("id", placeId);
         model.addAttribute("edit", new Place());
         return "admin/place";
     } 
     /**
      * Takes in the changed place information and saves the changes or returns to the form if validation was not successful
      * @param edit Edited Place object
      * @param result Result object of validation
      * @param placeId Id of the place that was changed
      * @param model Object which enables tranferring attributes from controller to view
      * @return Redirects to admin/places
      */
     @RequestMapping(value = "places/{placeId}", method = RequestMethod.POST)
     public String editPlaceInformation(@Valid @ModelAttribute("edit") Place edit, BindingResult result, @PathVariable Long placeId, Model model) {
         if (result.hasErrors()) {
             model.addAttribute("id", placeId);
             return "admin/place";
         }
         Place nonEdited = placeService.findOne(placeId);
         nonEdited.setName(edit.getName());
         nonEdited.setDescription(edit.getDescription());
         placeService.save(nonEdited);
         return "redirect:/admin/places";
     }
     /**
      * Deletes a place with given id
      * @param placeId Id of the place about to be deleted
      * @return Redirects to admin/places
      */
     @RequestMapping(value = "places/{placeId}", method = RequestMethod.DELETE)
     public String deletePlace(@PathVariable Long placeId) {
         placeService.deleteById(placeId);
         return "redirect:/admin/places";
     }
     /**
      * Shows measurements to an individual place and provides a form to add new measurements
      * @param placeId Id of the target place
      * @param model Object which enables tranferring attributes from controller to view
      * @return Measurements view
      */
     @RequestMapping(value = "places/{placeId}/measurements", method = RequestMethod.GET)
     public String showMeasurements(@PathVariable Long placeId, Model model) {
         Place place = placeService.findOne(placeId);
         
         if (place == null) {
             model.addAttribute("message", "Requested place does not exist");
             return "troubleshooting";
         }
         
         model.addAttribute("measurementform", new MeasurementForm());
         model.addAttribute("place", place);
         return "admin/measurements";
     }
     /**
      * Takes form data from creating new measurements and validates it. Saves to database if valid
      * @param form Form object that has form attributes' values
      * @param result Result object that contains validation errors
      * @param placeId Id of target place
      * @return Redirects to measurements
      */
     @RequestMapping(value = "places/{placeId}/measurements", method = RequestMethod.POST)
     public String addMeasurement(@Valid @ModelAttribute("measurementform") MeasurementForm form, BindingResult result, @PathVariable Long placeId) {
         if (result.hasErrors()) {
             return "admin/mesurements";
         }
         String lines = form.getMeasurements();
         Scanner scanner = new Scanner(lines);
         while (scanner.hasNextLine()) {
             String line = scanner.nextLine().trim();
             
             String[] parts = line.split(" ");
             
 
             //First part of the line should be a mac address
             if (!parts[0].matches("[a-zA-Z0-9:-]+")) {
                 result.addError(new FieldError("measurementform", "measurements", "The first part of the measurement should be a valid mac address following separation standards of : and -"));
                 return "admin/measurements";
             }
             //Second part of the line should be an integer (double precision not necessary here)
             if (parts.length < 2 || !parts[1].matches("[-]?[0-9]+")) {
                 result.addError(new FieldError("measurementform", "measurements", "Second part of each line should be an integer with a negative sign or without a sign."));
                 return "admin/measurements";
             }
             
             
         }
         placeService.addMeasurement(placeId, form);
         return "redirect:/admin/places/" + placeId + "/measurements";
     }
     /**
      * Shows individual measurements info
      * @param placeId Id of place that measurement belongs to
      * @param measurementId Id of given measurement
      * @param model Object which enables tranferring attributes from controller to view
      * @return Returns measurement view
      */
     @RequestMapping(value = "places/{placeId}/measurements/{measurementId}", method = RequestMethod.GET)
     public String showMeasurementInfo(@PathVariable Long placeId, @PathVariable Long measurementId, Model model) {
         Measurement m = measurementService.findOne(measurementId);
         if (m == null) {
             model.addAttribute("message", "Given measurement does not exist.");
            return "troublehshooting";
         }
         model.addAttribute("measurement", m);
         model.addAttribute("place", placeService.findOne(placeId));
         return "admin/measurement";
     }
     /**
      * Deletes measurement
      * @param placeId Place of the measurement
      * @param measurementId Measurement's id
      * @return Redirects to measurements
      */
     @RequestMapping(value = "places/{placeId}/measurements/{measurementId}")
     public String deleteMeasurement(@PathVariable Long placeId, @PathVariable Long measurementId) {
         measurementService.deleteById(placeId, measurementId);
         return "redirect:/admin/places/" + placeId + "/measurements";
     } 
     
     /**
      * Makes a downloadable text file available
      * @param placeId Id of the place to be turned to text file
      * @param response HttpServletResponse is an object that holds the role of delivering output, Model partly uses this object but plain response object is better for this purpose
      * @throws IOException If HttpResponse is not found, which should be NEVER
      */
     @RequestMapping("places/{placeId}/measurements/file")
     public void makeFileOutOfMeasurementsAndSendForDownload(@PathVariable Long placeId, HttpServletResponse response) throws IOException {
         Place place = placeService.findOne(placeId);
         response.setContentType("application/octet-stream");
         response.setHeader("Content-Disposition","attachment;filename=" + place.getName() + ".txt");
         InputStream is = new StringBufferInputStream(placeService.transformDataToText(place));
         IOUtils.copy(is, response.getOutputStream());
         response.flushBuffer();
     }
 }
