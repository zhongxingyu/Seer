 package org.emsionline.emsiweb.web.controller;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 
 @Controller
 public class SemiStaticPageController {
 	
 	final Logger logger = LoggerFactory.getLogger(SemiStaticPageController.class);
 	
 	@RequestMapping({"/emsi"})
 	public String partialPathEMSI() {
 		return "redirect:/emsi/intro";
 	}
 	
 	@RequestMapping({"/ebi"})
 	public String partialPathEBI() {
 		return "redirect:/ebi/intro";
 	}
 	
 	@RequestMapping({"/ebi/info"})
 	public String partialPathEBIGeneralInfo() {
 		return "redirect:/ebi/info/purpose";
 	}
 	
 	@RequestMapping({"/ebi/america"})
 	public String partialPathEBIAmerica() {
 		return "redirect:/ebi/america/aboutus";
 	}
 	
 	@RequestMapping({"/ebi/europe"})
 	public String partialPathEBIEurope() {
 		return "redirect:/ebi/europe/aboutus";
 	}
 	
 	@RequestMapping({"/ceom"})
 	public String partialPathCEOM() {
 		return "redirect:/ceom/intro";
 	}
 	
 
 	
 	@RequestMapping({"/emsi/*", "/ebi/**", "/ceom/*", "/news/*"})
 	public String serveSemiStaticContent(
 			Model model
 			, HttpServletRequest request
 			, Locale locale
 			) {
 		String[] pathElements = getPathElements(request.getServletPath());
 		String lang = locale.getLanguage();
 		if (!(lang.equals("en") || lang.equals("zh"))) {
 			lang = "en";
 		}
 		StringBuilder strbld = new StringBuilder();
 		
 		
 		strbld.append("semistatic").append("/");
 		
 		for (int i = 0; i < pathElements.length; i++) {
 			if (i < pathElements.length - 1) {
 				strbld.append(pathElements[i]).append("/");
 			}
 			else { // append language for the last part of the path
 				strbld.append(pathElements[i]).append("_").append(lang);
 
 			}
 		}
 		
 		return strbld.toString();
 	}
 	
 	@RequestMapping({"/donate", "/contactus"})
 	public String serveSemiStaticContentGlobal(
 			Model model
 			, HttpServletRequest request
 			, Locale locale
 			) {
 		String[] pathElements = getPathElements(request.getServletPath());
 		String lang = locale.getLanguage();
		if (!(lang.equals("en") || lang.equals("zh"))) {
			lang = "en";
		}		
 		StringBuilder strbld = new StringBuilder();
 		strbld.append("semistatic").append("/global/")
 			.append(pathElements[0]).append("_").append(lang);
 		return strbld.toString();
 	}
 		
 	
 	private String[] getPathElements(String servletPath) {
 		return StringUtils.split(servletPath, "/");
 	}
 }
