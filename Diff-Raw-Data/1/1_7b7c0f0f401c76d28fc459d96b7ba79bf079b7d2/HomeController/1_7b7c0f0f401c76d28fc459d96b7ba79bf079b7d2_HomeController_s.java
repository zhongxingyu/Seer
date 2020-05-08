 /**
  * @author Gutey Bogdan
  */
 package epam.ph.sg.controllers;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import epam.ph.sg.models.User;
 import epam.ph.sg.models.lang.LangSelector;
 
 @Controller
 @SessionAttributes("lang")
 public class HomeController {
 	private static Logger log = Logger.getLogger(HomeController.class);
 
 	@RequestMapping("/index.html")
 	public String index(HttpServletRequest request, HttpSession session) {
 		User user = (User) request.getSession().getAttribute("user");
 		log.info(request.getRequestURI() + " request received. User id="
 				+ user.getId());
		session.setAttribute("currentPos", "Menu.html");
 		return "index";
 	}
 
 	@RequestMapping("CurrentPos.html")
 	public String currentPos(HttpServletRequest request) {
 		User user = (User) request.getSession().getAttribute("user");
 		log.info(request.getRequestURI() + " request received. User id="
 				+ user.getId());
 		String currentPos = (String) request.getSession().getAttribute(
 				"currentPos");
 		if (currentPos == null) {
 			currentPos = "Menu.html";
 		}
 		return "redirect:" + currentPos;
 	}
 
 	@RequestMapping("/Menu.html")
 	public String menu(HttpServletRequest request) {
 		User user = (User) request.getSession().getAttribute("user");
 		log.info(request.getRequestURI() + " request received. User id="
 				+ user.getId());
 		return "Menu";
 	}
 
 	@RequestMapping(value = "/chLang.html", method = RequestMethod.POST)
 	public @ResponseBody
 	String chLang(@RequestParam("lang") String lang, HttpServletRequest request) {
 		User user = (User) request.getSession().getAttribute("user");
 		if (user != null) {
 			log.info(request.getRequestURI() + " request received. User id="
 					+ user.getId() + " Language: " + lang);
 		}
 		request.getSession().setAttribute("lang", lang);
 		Map<String, String> langPack = new LangSelector()
 				.getRb((String) request.getSession().getAttribute("lang"));
 		request.getSession().setAttribute("langPack", langPack);
 		return lang;
 	}
 }
