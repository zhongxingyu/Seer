 package com.trekker.controller;
 
 import com.trekker.model.Media;
 import com.trekker.model.Trip;
 import com.trekker.model.User;
 import com.trekker.service.TripService;
 import com.trekker.service.UserService;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.imageio.ImageIO;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.shiro.crypto.hash.Md5Hash;
 import org.imgscalr.Scalr;
import org.omnifaces.util.Messages;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.model.UploadedFile;
 
 @ManagedBean
 @RequestScoped
 public class Upload {
     @ManagedProperty(value="#{param.id}")
     private int id;
     private Trip trip;
     private User user;
     private Media media;
     
     @EJB
     private TripService tripService;
     
     @EJB
     private UserService userService;
     
     @PostConstruct
     public void init() {
         trip = tripService.find(id);
         user = userService.currentUser();
         media = new Media();
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
     
     public void handleFileUpload(FileUploadEvent event) {
         UploadedFile file = event.getFile();
         String extension = "." + FilenameUtils.getExtension(file.getFileName()).toLowerCase();
         String path = "uploads/" + user.getId() + "/" + id + "/";  
         String fileName = (new Md5Hash(file.getContents())).toHex();
         
         try {
             InputStream in = file.getInputstream();
             File outFile = new File(path + fileName + extension);
             outFile.getParentFile().mkdir();
             OutputStream out = new FileOutputStream(outFile);
             IOUtils.copy(in, out);
             in.close();
             out.close();
             
             BufferedImage fullSize = ImageIO.read(outFile);
             BufferedImage thumb = Scalr.resize(fullSize, 150);
             File outThumbFile = new File(path + "/t/" + fileName + ".jpg");
             ImageIO.write(thumb, "jpg", outThumbFile);
             
             media.setUserId(user);
             media.setTripId(trip);
             media.setFilename(fileName + extension);
             user.getMediaCollection().add(media);
             trip.getMediaCollection().add(media);
             tripService.update(trip);
             
            Messages.addGlobalInfo("{0} uploaded successfully", file.getFileName());
         } catch (IOException e) {
            Messages.addGlobalError("Upload failed. {0} not uploaded", file.getFileName());
         }
     }
 }
