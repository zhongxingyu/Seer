 package com.vidlib.clipviewer;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.util.UrlPathHelper;
 
 import com.vidlib.domain.Scene;
 import com.vidlib.service.SceneService;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 
 	//@Autowired
 	//private MediaDAO dao;
 	
 	@Autowired
 	SceneService sceneService;
 	
 	@Autowired
 	private FileStore fileStore;
 
 	@Autowired
 	private WebApplicationContext context;
 
	private Page<Scene> currentCarouselPage;
 	
 	private static final Logger logger = LoggerFactory
 			.getLogger(HomeController.class);
 	
 	@RequestMapping(value = "/media/{id}", method = (RequestMethod.GET), produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody
 	CarouselPanePage getCrouselPage(@PathVariable("id") String mediaId, @RequestParam int page, @RequestParam int size)
 	{
 		
 		long id = Long.parseLong(mediaId);
 		
		page = 0;
 		PageRequest pageRequest = new PageRequest(page, size);
 		
 		this.currentCarouselPage = sceneService.FindByMediaIdPageable(id, pageRequest);
 		
 		CarouselPanePage carouselPane = new CarouselPanePage();
 		
		for(int i = 0; i< this.currentCarouselPage.getNumberOfElements(); i++)
 		{
			Scene scene = this.currentCarouselPage.getContent().get(i);
 			carouselPane.addSceneId(scene.getIdScene());
 		}
 		carouselPane.numPages = this.currentCarouselPage.getTotalPages();
 		return carouselPane;
 	}
 	
 	
 	@RequestMapping(value = "/thumbnails", method = (RequestMethod.GET), produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody
 	ImageItemList listJSON(@RequestParam int first, @RequestParam int last, @RequestParam String id) {
 
 		logger.info("Getting thumbnails");
 		logger.info("First:" + first + " last:" + last + " id:" + id);
 
 
 		List<String> fileNames = fileStore.GetThumbnailsUrls(1, 1, new Date());
 
 		ImageItemList theList = new ImageItemList();
 
 		ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
 
 		List<String> slice = fileNames.subList(first-1, last);
 		
 		
 		for (int i = 0; i < slice.size(); i++) {
 			ImageItem item = new ImageItem();
 
 			logger.info("Adding-" + slice.get(i));
 			
 			item.setUrl("/" + slice.get(i));
 			item.setTimeStamp(slice.get(i));
 			imageItems.add(item);
 		}
 
 		theList.setImageList(imageItems);
 		theList.setTotal(fileNames.size());
 
 		return theList;
 	}
 
 	@RequestMapping(value = "/images/*", method = RequestMethod.GET)
 	public void getFileFromStore(HttpServletRequest request, HttpServletResponse response) {
 		UrlPathHelper helper = new UrlPathHelper();
 		String pathInfo = helper.getServletPath(request);
 
 		String filename = pathInfo.replaceFirst("/.*/", "");
 
 		File file = new File("/home/john/Dropbox/temp_jpgs/", filename);
 
 		response.setContentType(context.getServletContext().getMimeType(
 				file.getName()));
 		response.setContentLength((int) file.length());
 		response.setHeader("Content-Disposition",
 				"inline; filename=\"" + file.getName() + "\"");
 
 		BufferedInputStream input = null;
 		BufferedOutputStream output = null;
 
 		try {
 			input = new BufferedInputStream(new FileInputStream(file));
 			output = new BufferedOutputStream(response.getOutputStream());
 
 			byte[] buffer = new byte[8192];
 			int length;
 			while ((length = input.read(buffer)) > 0) {
 				output.write(buffer, 0, length);
 			}
 		} catch (Exception ex) {
 
 		} finally {
 			if (output != null)
 				try {
 					output.close();
 				} catch (IOException ignore) {
 				}
 			if (input != null)
 				try {
 					input.close();
 				} catch (IOException ignore) {
 				}
 		}
 
 	}
 
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		logger.info("Welcome home! The client locale is {}.", locale);
 
 		sceneService.find(1L);
 		
 		//String name = dao.findFirstMediaName();
 		
 		int [] scene_id = new int[3];
 		
 		scene_id[0] = 1;
 		scene_id[1] = 2;
 		scene_id[2] = 3;
 		
 		model.addAttribute("scene_id",scene_id );
 		return "home";
 	}
 
 }
