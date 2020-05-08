 package net.eyelock.productlocator.controllers.api;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServletResponse;
 
 import net.eyelock.productlocator.model.Media;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/api/media")
 public class MediaAPI {
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public String showMedia(@PathVariable("id") Long id,
 			HttpServletResponse response, Model model) {
 		Media media = Media.findMedia(id);
 
 		try {
 			response.setHeader("Content-Disposition", "inline;filename=\""
 					+ media.getFileName() + "\"");
 
 			OutputStream out = response.getOutputStream();
 			response.setContentType(media.getMimeType());
 			IOUtils.copy(new ByteArrayInputStream(media.getContent()), out);
 			out.flush();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
