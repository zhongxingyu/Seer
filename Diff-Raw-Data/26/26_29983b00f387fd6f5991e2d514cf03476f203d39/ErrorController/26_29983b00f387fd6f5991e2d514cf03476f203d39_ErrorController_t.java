 package ru.pfo.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 public class ErrorController extends BaseController {
 	private static final Logger LOG = Logger.getLogger(ErrorController.class);
 
 	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String error(Model model, HttpServletRequest request, HttpServletResponse response, @RequestParam("message") String message, @RequestParam("code") String code) {
 		try {
 			message = URLDecoder.decode(message, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			LOG.warn("Unsupported Encoding Exception for mesage: " + message);
 		} finally {
 			model.addAttribute("message", message);
 			model.addAttribute("code", code);
 		}
		response.setStatus(HttpStatus.NOT_FOUND.value());
 		return "error";
 	}
 
 }
