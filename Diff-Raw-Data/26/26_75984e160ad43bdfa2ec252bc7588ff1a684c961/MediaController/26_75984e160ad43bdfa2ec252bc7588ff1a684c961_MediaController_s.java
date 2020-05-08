 package net.eyelock.productlocator.controllers.web;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import net.eyelock.productlocator.model.Media;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
 import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
 
 @RequestMapping("/media")
 @Controller
 @RooWebScaffold(path = "media", formBackingObject = Media.class)
 @RooWebFinder
 public class MediaController {
 	@InitBinder
 	protected void initBinder(HttpServletRequest request,
 			ServletRequestDataBinder binder) throws ServletException {
 		binder.registerCustomEditor(byte[].class,
 				new ByteArrayMultipartFileEditor());
 	}
 
 	@RequestMapping(value = "/createmedia", method = RequestMethod.POST)
 	public String createMedia(@Valid Media media, BindingResult result,
 			Model model, @RequestParam("content") MultipartFile content,
 			HttpServletRequest request) {
 
 		media.setMimeType(content.getContentType());
 		media.setFileName(content.getOriginalFilename());
 		media.setSize(content.getSize());
 
 		if (result.hasErrors()) {
 			model.addAttribute("media", media);
 			return "media/create";
 		}
 		media.persist();
 
 		return "redirect:/media?page=1&amp;size=10"
 				+ encodeUrlPathSegment(media.getId().toString(), request);
 	}
 
 	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
 	public String updateForm(@PathVariable("id") Long id, Model uiModel,
 			HttpServletRequest request) {
 		Media media = Media.findMedia(id);
 		media.setUrl(media.getMediaURL(request));
 		populateEditForm(uiModel, media);
 		return "media/update";
 	}
 
 	@RequestMapping(value = "/updatemedia", method = RequestMethod.POST)
 	public String updateMedia(@Valid Media media, BindingResult result,
 			Model model, @RequestParam("content") MultipartFile updatedContent,
 			HttpServletRequest request) throws IOException {
 
 		// Check and see if we have new content, if we don't then get the
 		// original content and
 		// save it with that
 		if (updatedContent != null) {
 			media.setMimeType(updatedContent.getContentType());
 			media.setFileName(updatedContent.getOriginalFilename());
 			media.setSize(updatedContent.getSize());
 			media.setContent(updatedContent.getBytes());
 		}
 
 		if (result.hasErrors()) {
 			model.addAttribute("media", media);
 			return "media/update";
 		}
 		media.persist();
 
 		return "redirect:/media?page=1&amp;size=10"
 				+ encodeUrlPathSegment(media.getId().toString(), request);
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public String show(@PathVariable("id") Long id, Model model,
 			HttpServletRequest request) {
 		Media doc = Media.findMedia(id);
 		doc.setUrl(doc.getMediaURL(request));
 		model.addAttribute("media", Media.findMedia(id));
 		model.addAttribute("itemId", id);
 		return "media/show";
 	}
 
 	@RequestMapping(value = "/showmedia/{id}", method = RequestMethod.GET)
 	public String showMedia(@PathVariable("id") Long id,
 			HttpServletResponse response, Model model) {
 		Media doc = Media.findMedia(id);
 
 		try {
 			response.setHeader("Content-Disposition", "inline;filename=\""
 					+ doc.getFileName() + "\"");
 
 			OutputStream out = response.getOutputStream();
 			response.setContentType(doc.getMimeType());
 			IOUtils.copy(new ByteArrayInputStream(doc.getContent()), out);
 			out.flush();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
