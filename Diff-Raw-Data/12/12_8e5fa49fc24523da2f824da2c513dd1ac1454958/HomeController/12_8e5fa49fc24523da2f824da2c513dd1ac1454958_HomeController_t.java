 package f.rd.paths.web.controller;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 
 @Controller
 public class HomeController {
 	
 	private Logger log = LoggerFactory.getLogger(this.getClass());
 	
 	@RequestMapping("/clickHere")
 	public String click(Model model) {
 		log.info("------------------------");
 		log.info("简单跳转！！");
 		model.addAttribute("calculater", 900);
 		log.info("------------------------");
 		return "index";
 	}
 	
 }
