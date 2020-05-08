 package com.trekker.controller;
 
 import com.trekker.model.Accommodation;
 import com.trekker.model.Trip;
 import com.trekker.service.TripService;
 import java.io.IOException;
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import org.omnifaces.util.Faces;
 import org.omnifaces.util.Messages;
 
 @ManagedBean
 @RequestScoped
 public class AddAccommodation {
     @ManagedProperty(value="#{param.id}")
     private int id;
     private Trip trip;
     private Accommodation accommodation;
     
    private Date date;
     private int checkinHour;
     private int checkinMinute;
     
     private int checkoutHour;
     private int checkoutMinute;
     
     @EJB
     private TripService tripService;
     
     @PostConstruct
     public void init() {
         trip = tripService.find(id);
         accommodation = new Accommodation();
     }
     
     public void submit() throws IOException {
         accommodation.setTripId(trip);
        accommodation.setCheckinTime(date);
        accommodation.setCheckoutTime((Date)date.clone());
         accommodation.getCheckinTime().setHours(checkinHour);
         accommodation.getCheckinTime().setMinutes(checkinMinute);
         accommodation.getCheckoutTime().setHours(checkoutHour);
         accommodation.getCheckoutTime().setMinutes(checkoutMinute);
         
         trip.getAccommodationCollection().add(accommodation);
         tripService.update(trip);
         
         Messages.addFlashGlobalInfo("<div class=\"alert alert-success\">Accommodation successfully updated</div>");
         Faces.redirect("trip.xhtml?id=%s", Integer.toString(id));
     }
     
     public int getId() {
         return id;
     }
     
     public void setId(int id) {
         this.id = id;
     }
     
     public Trip getTrip() {
         return trip;
     }
 
    public Date getDate() {
        return date;
     }
     
     public Accommodation getAccommodation() {
         return accommodation;
     }
 
    public void setDate(Date date) {
        this.date = date;
     }
 
     public int getCheckinHour() {
         return checkinHour;
     }
 
     public void setCheckinHour(int checkinHour) {
         this.checkinHour = checkinHour;
     }
 
     public int getCheckinMinute() {
         return checkinMinute;
     }
 
     public void setCheckinMinute(int checkinMinute) {
         this.checkinMinute = checkinMinute;
     }
 
     public int getCheckoutHour() {
         return checkoutHour;
     }
 
     public void setCheckoutHour(int checkoutHour) {
         this.checkoutHour = checkoutHour;
     }
 
     public int getCheckoutMinute() {
         return checkoutMinute;
     }
 
     public void setCheckoutMinute(int checkoutMinute) {
         this.checkoutMinute = checkoutMinute;
     }
 }
