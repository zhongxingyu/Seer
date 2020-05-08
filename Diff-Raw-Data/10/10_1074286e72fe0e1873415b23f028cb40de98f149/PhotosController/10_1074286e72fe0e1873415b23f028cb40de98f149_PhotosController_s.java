 package se.bhg.photos.web;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import se.bhg.photos.model.Photo;
 import se.bhg.photos.service.PhotoService;
 
 /**
  * @author Fredrik Tuomas
  * 
  */
 @Controller
 @RequestMapping("/photos")
 public class PhotosController {
 	private static final Logger LOG = LoggerFactory.getLogger(PhotosController.class);
 	
     @Autowired
     PhotoService photoService;
     
     @RequestMapping("")
     public String index(Model model) {
     	LOG.debug("Accessed photos controller");
     	Iterable<Photo> photos = photoService.getPhotos();
     	List<Photo> full = new ArrayList<>(5000);
     	for(Photo p: photos) {
     	    full.add(p);
     	}
     	//Collections.shuffle(full);
        List<Photo> subSet = full.subList(1000, 1020);
    	model.addAttribute("photos", subSet);
         return "photos";
     }
 }
