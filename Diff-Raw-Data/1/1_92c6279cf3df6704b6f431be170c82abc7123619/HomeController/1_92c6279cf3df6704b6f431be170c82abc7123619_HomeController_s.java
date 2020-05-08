 package mdettlaff.mobilemachine.controller;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import mdettlaff.mobilemachine.domain.SimplifiedWebpage;
 import mdettlaff.mobilemachine.service.PageSimplifierService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class HomeController {
 
 	private final PageSimplifierService service;
 
 	@Autowired
 	public HomeController(PageSimplifierService service) {
 		this.service = service;
 	}
 
 	@RequestMapping
 	public String home() {
 		return "index";
 	}
 
 	@RequestMapping("/simplified")
 	public ModelAndView simplified(@RequestParam String url,
 			@RequestParam(required = false, defaultValue = "1") int page) throws IOException {
 		SimplifiedWebpage webpage = service.simplify(url);
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("title", webpage.getTitle());
 		model.put("html", webpage.getPage(page));
		model.put("currentPage", page);
 		model.put("nextPage", page == webpage.getPageCount() ? null : page + 1);
 		model.put("pageCount", webpage.getPageCount());
 		model.put("url", url);
 		return new ModelAndView("simplified", model);
 	}
 }
