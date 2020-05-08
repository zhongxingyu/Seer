 package com.credera.trails.controller;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.imageio.ImageIO;
 
 import org.imgscalr.Scalr;
 //import org.slf4j.Logger;
 //import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.support.MutableSortDefinition;
 import org.springframework.beans.support.PropertyComparator;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import com.credera.trails.model.Region;
 import com.credera.trails.model.Route;
 import com.credera.trails.model.S3Photo;
 import com.credera.trails.service.RegionService;
 import com.credera.trails.service.RepositoryService;
 import com.credera.trails.service.RouteService;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class RegionController {
 
     public static final String LAST_UPDATE_VIEW_KEY = "lastUpdate";
     public static final String REGION_VIEW_KEY = "region";
     public static final String ROUTES_VIEW_KEY = "routes";
 
 	@Autowired
 	private RepositoryService repositoryService;
 
 	@Autowired
 	private RegionService regionService;
 	
 	@Autowired
 	private RouteService routeService;
 	
 	@RequestMapping(value = "/{regionUrl}", method = RequestMethod.GET)
 	public ModelAndView getRegion(@PathVariable(value = "regionUrl") String regionUrl, @ModelAttribute("message") String message, @RequestParam(value="sort",required=false) String sort) {
 		Map<String, Object> model = new HashMap<String, Object>();
 
 		if (message != null) {
 			model.put("message", message);
 		}
 		
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return invalidRegion(regionUrl);
 		}
 		model.put("region", region);
 		
 		List<Route> routes = new ArrayList<Route>(routeService.getAllRoutesByRegion(region));
 		PropertyComparator.sort(routes, new MutableSortDefinition("name", true, true));
 
 		if (sort != null && sort.length() > 0 && sort.equalsIgnoreCase("averageRating")) {
 			PropertyComparator.sort(routes, new MutableSortDefinition("averageRating", true, false));
 		}
 		model.put("routes", routes);
 		
 		return new ModelAndView("region", model);
 	}
 
 	@RequestMapping(value = "/addregion", method = RequestMethod.GET)
 	public ModelAndView addRegion() {
 		Map<String, Object> model = new HashMap<String, Object>();
 		
 		model.put("region", new Region());
 		
 		return new ModelAndView("regionAdd", model);
 	}
 
 	@RequestMapping(value = "/addregion", method = RequestMethod.POST)
 	public ModelAndView postRegion(@ModelAttribute("region") Region region) {
 		if (region.getFileData() != null) {			
 			try {
 		        String key = UUID.randomUUID().toString() + ".jpg";
 		        BufferedImage bufferedImage = ImageIO.read(region.getFileData().getInputStream());
 
 		        BufferedImage thumbnail = Scalr.resize(bufferedImage, 300);
 
 		        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		        ImageIO.write(thumbnail, "jpg", byteArrayOutputStream);
 		        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
 
 				repositoryService.putAsset(key, inputStream);
 				
 				S3Photo s3Photo = new S3Photo("com.credera.trails.run.dev", key);
 				region.setPhoto(s3Photo);
 				
 				regionService.saveRegion(region);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		return new ModelAndView(new RedirectView(region.getUrlFriendlyName()));
 	}
 
 	@RequestMapping(value = "/{regionUrl}/delete", method = RequestMethod.GET)
 	public ModelAndView deleteRegion(@PathVariable(value = "regionUrl") String regionUrl) {
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return invalidRegion(regionUrl);
 		}
 		regionService.deleteRegion(region);
 		
 		return new ModelAndView(new RedirectView("../"));
 	}
 
     @RequestMapping(value = "/{regionUrl}/feed", method = RequestMethod.GET)        
     @Transactional
     public ModelAndView feed(@PathVariable(value = "regionUrl") String regionUrl) {
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return invalidRegion(regionUrl);
 		}
         ModelAndView modelAndView = new ModelAndView();
         modelAndView.setViewName("atomFeedView");
         modelAndView.addObject(REGION_VIEW_KEY, region);
         modelAndView.addObject(LAST_UPDATE_VIEW_KEY, getCreationDateOfTheLast(region.getRoutes()));
         return modelAndView;
     }
  
 	private Date getCreationDateOfTheLast(List<Route> routes) {
 		if(routes.size() > 0) {
 			return routes.get(0).getCreationDate();
 		}
 		return new Date(0);
 	}
 
 	static public ModelAndView invalidRegion(String regionUrl) {
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("message", "Region " + regionUrl + " not found");
 		return new ModelAndView(new RedirectView("/", true), model);
 	}
 
 }
