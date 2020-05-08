 package com.trekker.controller;
 
 import com.trekker.model.Media;
 import com.trekker.model.Trip;
 import com.trekker.model.User;
 import com.trekker.service.MediaService;
 import com.trekker.service.TripService;
 import com.trekker.service.UserService;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import org.omnifaces.util.Faces;
 import org.omnifaces.util.Messages;
 
 @ManagedBean
 @RequestScoped
 public class EditMedia {
     @ManagedProperty(value="#{param.id}")
     private int id;
     
     private Map<Integer, Boolean> checked = new HashMap<Integer, Boolean>();
     
     private Trip trip;
     private User user;
     
     @EJB
     private TripService tripService;
     
     @EJB
     private MediaService mediaService;
     
     @EJB
     private UserService userService;
     
     @PostConstruct
     public void init() {
         trip = tripService.find(id);
         user = userService.currentUser();
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
     
     public Map<Integer, Boolean> getChecked() {
         return checked;
     }
     
     public void delete() throws IOException {
         for(Integer id : checked.keySet()) {
             if (checked.get(id)) {
                 Media mediaToDelete = mediaService.find(id);
                 trip.getMediaCollection().remove(mediaToDelete);
                 mediaService.delete(mediaToDelete);
                String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
                String path = instanceRoot + "uploads/" + user.getId() + "/" + this.id + "/";
                 File mediaFile = new File(path + mediaToDelete.getFilename());
                 File mediaThumbFile = new File(path + "t/" + mediaToDelete.getFilename());
                 mediaFile.delete();
                 mediaThumbFile.delete();
             }
         }
         tripService.update(trip);
         Messages.addFlashGlobalInfo("<div class=\"alert alert-success\">Media successfully updated</div>");
         Faces.redirect("editmedia.xhtml?id=%s", Integer.toString(this.id));
     }
     
     public void submit() throws IOException {
         tripService.update(trip);
         Messages.addFlashGlobalInfo("<div class=\"alert alert-success\">Media successfully updated</div>");
         Faces.redirect("editmedia.xhtml?id=%s", Integer.toString(this.id));
     }
 }
