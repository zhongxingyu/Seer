 package com.silkstream.platform.controllers;
 
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
 
 @Controller
@RequestMapping(value = "/api/image", produces = "application/json")
 public class ImageController extends BasicController {
 
 	@ResponseStatus(HttpStatus.BAD_REQUEST)
 	@RequestMapping(value = "api/**", method = RequestMethod.GET)
 	public ModelAndView wrongApi(ModelAndView model) {
 		model.addObject("Error", "Wrong api called");
 		model.setViewName("error");
 		return model;
 	}
 
 	@ResponseStatus(HttpStatus.FORBIDDEN)
 	@RequestMapping(value = "/403", method = RequestMethod.GET)
 	public ModelAndView accessDenied(ModelAndView model) {
 		//model.addObject("ApplicationException", "Wrong api called");
 		model.setViewName("error");
 		return model;
 	}
 
 	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = "{id}", produces = "application/octet-stream")
 	@ResponseBody
 	public byte[] index(@PathVariable String id) {
 		ClassPathResource resource = new ClassPathResource("images/" + id + ".jpg");
 		if (!resource.exists()) {
 			throw new Error("ID is not valid.");
 		}
 		byte[] bytes = null;
 		try {
 
			BufferedImage image = ImageIO.read(resource.getFile());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			bytes = baos.toByteArray();

//			ImageInfo origInfo = new ImageInfo(); //load image info
//			MagickImage image = new MagickImage(origInfo); //load image
//			image = image.scaleImage(finalWidth, finalHeight); //to Scale image
//			image.setFileName(absNewFilePath); //give new location
//			image.writeImage(origInfo); //save

 		} catch (Exception e) {
 
 		}
 		return bytes;
 	}
 }
