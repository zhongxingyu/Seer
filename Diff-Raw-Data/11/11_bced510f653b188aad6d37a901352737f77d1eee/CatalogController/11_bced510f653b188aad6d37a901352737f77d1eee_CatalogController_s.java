 package by.itransition.fanfic.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import by.itransition.fanfic.domain.Fanfic;
 import by.itransition.fanfic.service.FanficService;
 
 @Controller
 @RequestMapping("/catalog")
 public class CatalogController extends VisitPageController {
 
 	@Autowired
 	private FanficService fanficService;
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public String getCatalog(Model model, HttpServletRequest request) {
 		settingModel(model, request);
 		model.addAttribute("allFanfics", fanficService.getAllFanfics());
 		return "catalog";
 	}
 	
 	@RequestMapping(value = "/{category}", method = RequestMethod.GET)
	public String getComedy(@PathVariable("category") String category,
 			Model model, HttpServletRequest request) {
 		settingModel(model, request);
		//List<Fanfic> fanfics = fanficService.getFanficsByCategory(category);
		//List<Fanfic> fanfics = FanficModel.getInstance()
		//		.getFanficsByCategory(category);
		List<Fanfic> fanfics = new ArrayList<Fanfic>();
		model.addAttribute("allFanfics", fanfics);
 		return "catalog";
 	}
 }
