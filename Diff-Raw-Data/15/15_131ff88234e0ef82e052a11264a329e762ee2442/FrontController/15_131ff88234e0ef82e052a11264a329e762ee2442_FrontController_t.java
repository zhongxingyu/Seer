 package net.pdp7.tvguide.spring.web;
 
 import java.util.Date;
 
 import net.pdp7.commons.util.MapUtils;
 import net.pdp7.tvguide.dao.EpgDao;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class FrontController {
 
 	protected final EpgDao epgDao;
 
 	public FrontController(EpgDao epgDao) {
 		this.epgDao = epgDao;
 	}
 	
 	@RequestMapping("/index.html")
 	public ModelAndView index() {
 		return new ModelAndView("index", 
 				MapUtils
 					.build("programs", epgDao.getPrograms(AlexChannels.alexChannels, new Date(), 4))
 					.map);
 	}
 }
