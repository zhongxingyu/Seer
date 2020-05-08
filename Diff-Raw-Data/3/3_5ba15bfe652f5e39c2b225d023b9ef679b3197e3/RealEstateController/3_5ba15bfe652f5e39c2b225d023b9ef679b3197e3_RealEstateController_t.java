 package br.com.camiloporto.tenant.web;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import br.com.camiloporto.tenant.model.Imovel;
 import br.com.camiloporto.tenant.service.ImovelService;
 
 @RequestMapping("/realestates")
 @Controller
 public class RealEstateController {
 	
 	@Autowired
 	private ImovelService imovelService;
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView home() {
 		ModelAndView mav = new ModelAndView("realestate/index");
 		List<Imovel> imoveis = imovelService.findAllSortedByUltimaAtualizacao();
 		mav.addObject("imoveis", imoveis);
 		
 		return mav;
 	}
 	
 	@RequestMapping(value="/{id}", method = RequestMethod.GET)
 	public ModelAndView detail(@PathVariable String id) {
 		ModelAndView mav = new ModelAndView("realestate/detail");
 		Imovel saved = imovelService.findImovel(id);
 		mav.addObject("imovel", saved);
 		
 		return mav;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, params="q")
 	public ModelAndView search(@RequestParam String q) {
		if(q == null || "".equals(q)) {
			return home();
		}
 		ModelAndView mav = new ModelAndView("realestate/index");
 		List<Imovel> imoveis = imovelService.genericQuery(q);
 		mav.addObject("imoveis", imoveis);
 		
 		return mav;
 	}
 
 }
