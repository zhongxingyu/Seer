 package org.icemobile.samples.springbasic;
 
 import java.io.IOException;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.io.File;
 import javax.servlet.http.HttpServletRequest;
 
 @Controller
 @SessionAttributes("camcorderBean")
 public class CamcorderController {
     boolean mediaReady = false;
 
 	@ModelAttribute
 	public void ajaxAttribute(WebRequest request, Model model) {
 		model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
 	}
 
 	@ModelAttribute("camcorderBean")
 	public ModelBean createBean() {
 		return new ModelBean();
 	}
 
 	@RequestMapping(value = "/camcorder", method=RequestMethod.GET)
     public void processVideo(Model model)  {
         model.addAttribute("mediaReady", new Boolean(mediaReady));
     }
 
	@RequestMapping(value = "/camcorder", method=RequestMethod.POST)
 	public void processVideo(HttpServletRequest request, ModelBean modelBean,
                              @RequestParam(value = "camvid", required = false) MultipartFile file,
                              Model model) throws IOException {
 
         String fileName = "empty";
         if (null != file)  {
             fileName = file.getOriginalFilename();
            file.transferTo(new File(request.getRealPath("/resources/media/video.mp4")));
             mediaReady = true;
         }
         model.addAttribute("mediaReady", new Boolean(mediaReady));
 		model.addAttribute("message", "Hello " + modelBean.getName() +
                 ", your video file '" + fileName + "' was uploaded successfully.");
 
     }
 
 }
