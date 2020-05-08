 package org.icemobile.samples.spring.mediacast;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;
 
 import javax.inject.Inject;
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.icepush.PushContext;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.context.request.WebRequest;
 
 @Controller
 @SessionAttributes({"uploadModel","msg"})
 public class ContestController implements ServletContextAware {
 
 	@Inject
 	private MediaService mediaService;
 	
 	String currentFileName = null;
 		
 	private ServletContext servletContext;
 	
 	private static final Log log = LogFactory
 			.getLog(ContestController.class);
         
        
 	@Autowired
 	public ContestController(ServletContext servletContext){
 		this.servletContext = servletContext;		
 	}
 	
 	@ModelAttribute("uploadModel")
 	public MediaMessage getUploadModel(){
 		MediaMessage uploadModel = new MediaMessage();
 		log.debug("returning new uploadModel="+uploadModel);
 		return uploadModel;
 	}
 
 	@ModelAttribute
 	public void ajaxAttribute(WebRequest request, Model model) {
 		model.addAttribute("ajaxRequest", isAjaxRequest(request));
 	}
 
 	private void setupInitialModel(Model model, MediaMessage uploadModel){
 		model.addAttribute("mediaService", mediaService);
 		if( !model.containsAttribute("uploadModel")){
 			model.addAttribute("uploadModel", uploadModel);
 		}
 		log.debug("uploadModel="+uploadModel);
 	}
 	
 	@RequestMapping(value="/contest-upload", method = RequestMethod.GET)
 	public String getContestPage(Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel) {
 		
 		setupInitialModel(model,uploadModel);
 		return "contest-upload";
 	}
 	
 	@RequestMapping(value="/contest-tablet", method = RequestMethod.GET)
 	public String getContestTabletPage(Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel) {
 		
 		setupInitialModel(model,uploadModel);
 		return "contest-tablet";
 	}
 	
 	@RequestMapping(value="/contest-carousel", method = RequestMethod.GET)
 	public String getCarouselContent(Model model) {
 		model.addAttribute("mediaService", mediaService);
 		return "contest-carousel";
 	}
 	
 
 	@RequestMapping(value = "/contest-uploads/{id}", method = RequestMethod.GET)
 	public String getMediaViewerPage(@PathVariable String id, Model model) {
 		MediaMessage msg = mediaService.getMediaMessage(id);
 		if (msg != null) {
 			log.debug("found media " + msg);
 			model.addAttribute("media", msg);
 			return "contest-viewer";
 		} else {
 			log.warn("Could not find message with id=" + id);
 			return "redirect:/";
 		}
 	}
 
 	@RequestMapping(value="/contest-upload", method = RequestMethod.POST, 
 			consumes="multipart/form-data")
 	public String uploadPhoto(
 			HttpServletRequest request,
 			@RequestParam(value = "upload", required = false) MultipartFile file,
 			@Valid ContestForm form, BindingResult result, Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel)
 			throws IOException {
 		
 		if (result.hasErrors() || (file != null && file.isEmpty())) {
 			uploadModel.setUploadMsg("Sorry, I think you missed something.");
 			return "contest-upload";
 		}
 		
 		if( file != null ){
 			log.debug("file: " + file);
 			uploadModel.setId(newId());
 			saveImage(request, file, uploadModel);
 			uploadModel.setDescription(form.getDescription());
 			uploadModel.setEmail(form.getEmail());
 			mediaService.addMedia(uploadModel);
 			log.debug("successfully added message to mediaService, uploadModel="
 					+ uploadModel);
 			uploadModel.setUploadMsg("Thank you, your file was uploaded successfully.");
 			uploadModel.clear();
 			PushContext.getInstance(servletContext).push("photos");
 			
 			return "redirect:/";
 		}
 		else{
 			log.warn("upload file was null");
 			return "contest-upload";
 		}
 	}
 	
 	@RequestMapping(value="/contest-gallery", method=RequestMethod.GET)
 	public String showGallery(Model model){
 		
 		model.addAttribute("mediaService", mediaService);
 		return "contest-gallery";
 	}
 	
 	@RequestMapping(value="/contest-photo-list", method=RequestMethod.GET)
 	public String showPhotoList(Model model){
 		
 		model.addAttribute("mediaService", mediaService);
 		return "contest-photo-list";
 	}
 	
 	@RequestMapping(value="/contest-uploads/{id}", method = RequestMethod.POST)
 	public String voteOnPhoto(HttpServletResponse response, @PathVariable String id, 
 			@CookieValue(value="votes", required=false) String cookieVotes, Model model){
 		MediaMessage msg = mediaService.getMediaMessage(id);
 		if (msg != null ){
 			String voterId = null;
 			List<String> votesList = null;
 			if( cookieVotes != null ){
 				voterId = cookieVotes.substring(0,13);
 				String votes = cookieVotes.substring(14);
 				votesList = new ArrayList<String>(Arrays.asList(votes.split(",")));
 				if( votesList.contains(id)){
 					log.debug("attempted duplicate vote!!!");
 					model.addAttribute("msg","Looks like you already voted on this one...try another");
 					return "redirect:/contest-uploads/"+id;
 				}
 			}
 			else{
 				voterId = newId();
 				votesList = new ArrayList<String>();
 			}
 			votesList.add(id);
 			msg.getVotes().add(voterId);
 			model.addAttribute("msg","Awesome, thanks for the vote!");
 			String newVotes = voterId+":"+ votesList.toString().replaceAll(" ", "").replaceAll("^\\[|\\]$","");
 			Cookie cookie = new Cookie("votes", newVotes);
 			cookie.setHttpOnly(true);
 			cookie.setPath("/");
 			response.addCookie(cookie);
 			log.debug("recorded vote");
 			return "redirect:/contest-uploads/"+id;
 			
 		} else {
 			log.warn("Could not find message with id=" + id);
 			return "redirect:/contest-uploads/"+id;
 		}
 		
 	}
 	
 	private String newId() {
 		return Long.toString(
 				Math.abs(UUID.randomUUID().getMostSignificantBits()), 32);
 	}
 	
 	private void addNewMediaToUploadModel(HttpServletRequest request, MultipartFile file, 
 			String suffix, MediaMessage uploadModel) throws IOException {
 		if (uploadModel.getId() == null) {
 			uploadModel.setId(newId());
 		}
 		String newFileName = "img-" + uploadModel.getId() + "." + suffix;
 		String newPathName = "resources/uploads/" + newFileName;
 		File newFile = new File(servletContext.getRealPath("/" + newPathName));
 		if ((null != file) && !file.isEmpty()) {
 			file.transferTo(newFile);
 			currentFileName = newPathName;
 		}
 		else {
 			// use previously uploaded file, such as from ICEmobile-SX
 			newFileName = getCurrentFileName(request);
 			newFile = new File(servletContext.getRealPath("/" + newPathName));
 		}
 		Media media = new Media();
 		media.setFileName(newFileName);
 		media.setFile(newFile);
 		media.setType(file.getContentType());
 		uploadModel.setPhoto(media);
 		log.debug("added new media to uploadModel");
 	}
 
 	private void saveImage(HttpServletRequest request, MultipartFile file,
 			MediaMessage uploadModel) throws IOException {
 
 		addNewMediaToUploadModel(request, file, "jpg", uploadModel);
 	}
 	//TODO look into this
 	private String getCurrentFileName(HttpServletRequest request) {
 		if (null == currentFileName) {
 			return "resources/images/uploaded.jpg";
 		}
 		return currentFileName;
 	}
 	
 	
 	public static boolean isAjaxRequest(WebRequest webRequest) {
 		String requestedWith = webRequest.getHeader("Faces-Request");
 		if ("partial/ajax".equals(requestedWith))  {
             return true;
         }
 
 		requestedWith = webRequest.getHeader("X-Requested-With");
 		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
 	}
 
 	public static boolean isAjaxUploadRequest(WebRequest webRequest) {
 		return webRequest.getParameter("ajaxUpload") != null;
 	}
         
         public void setServletContext(ServletContext sc) {
             servletContext = sc; 
         }
 
 
 }
