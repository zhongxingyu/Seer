 package org.icemobile.samples.spring.mediacast;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 @Controller
 @RequestMapping("/media")
 @SessionAttributes({"uploadModel"})
 public class ApplicationController {
 
 	@Autowired
 	private MediaStore mediaStore;
 	
 	String currentFileName = null;
 	
 	private static final Log log = LogFactory
 			.getLog(ApplicationController.class);
 	
 	@ModelAttribute("uploadModel")
 	public MediaMessage getUploadModel(){
 		MediaMessage uploadModel = new MediaMessage();
 		log.debug("returning new uploadModel="+uploadModel);
 		return uploadModel;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public String getMediacastPage(Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel) {
 		model.addAttribute("mediaStore", mediaStore);
 		if( !model.containsAttribute("uploadModel")){
 			model.addAttribute("uploadModel", uploadModel);
 		}
 		log.debug("uploadModel="+uploadModel);
 		return "mediacast";
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public String getMediaViewerPage(@PathVariable String id, Model model) {
 		MediaMessage msg = mediaStore.getMediaMessage(id);
 		if (msg != null) {
 			log.debug("found media " + msg);
 			model.addAttribute("media", msg);
 			return "mediaviewer";
 		} else {
 			log.warn("Could not find message with id=" + id);
 			return "redirect:media";
 		}
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
 	public String deleteMedia(@PathVariable String id) {
 		mediaStore.removeMessage(id);
 		log.debug("removed media message id=" + id);
 		return "redirect:/";
 	}
 
 	@RequestMapping(method = RequestMethod.POST, consumes="multipart/form-data")
 	public String processUpload(
 			HttpServletRequest request,
 			@RequestParam(value = "upload", required = false) MultipartFile file,
 			Model model, 
 			@ModelAttribute("uploadModel") MediaMessage uploadModel,
 			final RedirectAttributes redirectAttrs)
 			throws IOException {
 		
 		String newFileName = null;
 		String type = request.getParameter("type");
 		if( "photo".equals(type)){
 			newFileName = saveImage(request, file, uploadModel);
 			Media media = new Media();
 			media.setFileName(newFileName);
 			media.setType(file.getContentType());
 			uploadModel.setPhoto(media);
 		}
 		else if( "video".equals(type)){
 			newFileName = saveVideo(request, file, uploadModel);
 			Media media = new Media();
 			media.setFileName(newFileName);
 			media.setType(file.getContentType());
 			uploadModel.setVideo(media);
 		}
 		else if( "audio".equals(type)){
 			newFileName = saveAudio(request, file, uploadModel);
 			Media media = new Media();
 			media.setFileName(newFileName);
 			media.setType(file.getContentType());
 			uploadModel.setAudio(media);
 		}
 		log.debug("processUpload() type=" + type + ", file="+file);
 		if ((null != file) && !file.isEmpty()) {
 			uploadModel.setUploadMsg(String.format("Thank you, your %s file was uploaded successfully.", type));
 			log.debug("successfully uploaded media, uploadModel=" + uploadModel);
 		}
 		redirectAttrs.addFlashAttribute("uploadModel", uploadModel);
 		
 		return "redirect:/";
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String submitMessage(Model model, 
 			@RequestParam String tags,
 			@RequestParam String title, 
 			@RequestParam String description,
 			@ModelAttribute("uploadModel") MediaMessage uploadModel, 
 			SessionStatus status) {
 
 		if (uploadModel.getId() == null) {
 			uploadModel.setId(newId());
 		}
 		if (tags != null && tags.indexOf(" ") > -1) {
 			// workaround split returns null if no delimiter found
 			uploadModel.setTags(Arrays.asList(StringUtils
 					.split(tags + " ", " ")));
 		}
 
 		uploadModel.setDescription(description);
 		uploadModel.setTitle(title);
 		mediaStore.getMedia().add(uploadModel.clone());
 		log.debug("successfully added message to mediaStore, uploadModel="
 				+ uploadModel);
 		uploadModel.clear();
 		status.setComplete();
 		return "redirect:/";
 	}
 
 	private String newId() {
 		return Long.toString(
 				Math.abs(UUID.randomUUID().getMostSignificantBits()), 32);
 	}
 
 	private String saveImage(HttpServletRequest request, MultipartFile file,
 			MediaMessage uploadModel) throws IOException {
 
 		if (uploadModel.getId() == null) {
 			uploadModel.setId(newId());
 		}
 		String fileName = null;
 		String newFileName = "img-" + uploadModel.getId() + ".jpg";
 		String newPathName = "resources/uploads/" + newFileName;
 		if ((null != file) && !file.isEmpty()) {
 			fileName = file.getOriginalFilename();
			file.transferTo(new File(request.getServletContext().getRealPath("/" + newPathName)));
 			currentFileName = newPathName;
 		}
 		if (null == fileName) {
 			// use previously uploaded file, such as from ICEmobile-SX
 			newFileName = getCurrentFileName(request);
 
 		}
 
 		return newFileName;
 	}
 
 	private String saveVideo(HttpServletRequest request, MultipartFile file,
 			MediaMessage uploadModel) throws IOException {
 
 		if (uploadModel.getId() == null) {
 			uploadModel.setId(newId());
 		}
 		String fileName = null;
 		String newFileName = "video-" + uploadModel.getId() + ".mp4";
 		String newPathName = "resources/uploads/" + newFileName;
 		if ((null != file) && !file.isEmpty()) {
 			fileName = file.getOriginalFilename();
			file.transferTo(new File(request.getServletContext().getRealPath("/" + newPathName)));
 			currentFileName = newPathName;
 		}
 		if (null == fileName) {
 			// use previously uploaded file, such as from ICEmobile-SX
 			newFileName = getCurrentFileName(request);
 
 		}
 
 		return newFileName;
 	}
 
 	private String saveAudio(HttpServletRequest request, MultipartFile file,
 			MediaMessage uploadModel) throws IOException {
 
 		if (uploadModel.getId() == null) {
 			uploadModel.setId(newId());
 		}
 		String fileName = null;
 		String newFileName = "audio-" + uploadModel.getId() + ".m4a";
 		String newPathName = "resources/uploads/" + newFileName;
 		if ((null != file) && !file.isEmpty()) {
 			fileName = file.getOriginalFilename();
			file.transferTo(new File(request.getServletContext().getRealPath("/" + newPathName)));
 			currentFileName = newPathName;
 		}
 		if (null == fileName) {
 			// use previously uploaded file, such as from ICEmobile-SX
 			newFileName = getCurrentFileName(request);
 
 		}
 
 		return newFileName;
 	}
 
 	private String getCurrentFileName(HttpServletRequest request) {
 		if (null == currentFileName) {
 			return "resources/images/uploaded.jpg";
 		}
 		return currentFileName;
 	}
 
 }
