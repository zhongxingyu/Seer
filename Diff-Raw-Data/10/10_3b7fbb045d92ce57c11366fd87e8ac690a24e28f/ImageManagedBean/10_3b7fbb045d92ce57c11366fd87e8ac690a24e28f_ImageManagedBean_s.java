 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commomInfrastructure.utility.managedbean;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @ViewScoped
 public class ImageManagedBean {
     private List<String> images;  
   
     public ImageManagedBean() {  
         images = new ArrayList<String>();  
        images.add("hotel_1.jpg");  
        images.add("hotel_2.jpg");  
        images.add("hotel_3.jpg");  
        images.add("hotel_4.jpg");  
        images.add("hotel_5.jpg"); 
     }  
   
     public List<String> getImages() {  
         return images;  
     }  
 }
