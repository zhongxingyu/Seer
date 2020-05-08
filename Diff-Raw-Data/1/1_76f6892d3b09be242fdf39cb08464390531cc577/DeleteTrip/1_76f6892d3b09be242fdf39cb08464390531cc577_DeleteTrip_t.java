 package com.trekker.controller;
 
 import com.trekker.model.Trip;
 import com.trekker.model.User;
 import com.trekker.service.TripService;
 import com.trekker.service.UserService;
 import java.io.IOException;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Named;
 import org.omnifaces.util.Faces;
 import org.omnifaces.util.Messages;
 
 @Named
 @RequestScoped
 public class DeleteTrip {
     private User user;
         
     @EJB
     private UserService userService;
     
     @EJB
     private TripService tripService;
     
     @PostConstruct
     public void init() {
         user = userService.currentUser();
     }
     
     public void submit(int tripId) throws IOException {
         Trip triptoDelete = tripService.find(tripId);
         user.getTrips().remove(triptoDelete);
         userService.update(user);
        tripService.delete(triptoDelete);
         Messages.addFlashGlobalInfo("<div class=\"alert alert-success\">Trip successfully deleted</div>");
         Faces.redirect("profile.xhtml");
     }
 }
