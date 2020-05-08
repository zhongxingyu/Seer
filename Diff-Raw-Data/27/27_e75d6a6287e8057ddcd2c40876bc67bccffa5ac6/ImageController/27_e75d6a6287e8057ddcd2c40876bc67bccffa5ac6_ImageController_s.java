 package com.silkstream.platform.controllers;
 
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
 
 @Controller
@RequestMapping("/api/image")
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
	@RequestMapping(method = RequestMethod.GET, value = "{id}")
 	@ResponseBody
 	public byte[] index(@PathVariable String id) {
 		ClassPathResource resource = new ClassPathResource("images/" + id + ".jpg");
 		if (!resource.exists()) {
 			throw new Error("ID is not valid.");
 		}
 		byte[] bytes = null;
 		try {
			BufferedImage bufferedImage = ImageIO.read(resource.getFile());
 
			// get DataBufferBytes from Raster
			WritableRaster raster = bufferedImage.getRaster();
			DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
			bytes = data.getData();
 		} catch (Exception e) {
 
 		}
 		return bytes;
 	}
 }
