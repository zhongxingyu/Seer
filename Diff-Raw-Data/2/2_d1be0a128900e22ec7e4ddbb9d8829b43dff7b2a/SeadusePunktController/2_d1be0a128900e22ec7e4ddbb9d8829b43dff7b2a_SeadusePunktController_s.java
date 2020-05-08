 package ee.itcollege.p0rn.web;
 
 import javax.servlet.http.HttpServletRequest;
 
 import ee.itcollege.p0rn.entities.Kodakondsus;
 import ee.itcollege.p0rn.entities.Piiririkkuja;
 import ee.itcollege.p0rn.entities.Seadus;
 import ee.itcollege.p0rn.entities.SeadusePunkt;
 import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @RooWebScaffold(path = "seadusepunkts", formBackingObject = SeadusePunkt.class)
 @RequestMapping("/seadusepunkts")
 @Controller
 public class SeadusePunktController {
     @RequestMapping(method = RequestMethod.POST)
     public String create(SeadusePunkt seadusepunkt, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
     	if (bindingResult.hasErrors()) {
             uiModel.addAttribute("seadus", seadusepunkt);
             addDateTimeFormatPatterns(uiModel);
             return "seaduses/update";
         }
         uiModel.asMap().clear();
         seadusepunkt.merge();
         
         String next_url = httpServletRequest.getParameter("next").toString();
     	if (!next_url.equals("")) {
     		return "redirect:"+next_url;
     	}
         
         try {
         	return "redirect:/seaduses/" + encodeUrlPathSegment(seadusepunkt.getSeaduse_ID().getId().toString(), httpServletRequest) + "?form";
         } catch (Exception ex) {
         	return "redirect:/seadusepunkts/";
         }
     }
     
     @RequestMapping(method = RequestMethod.PUT)
     public String update(SeadusePunkt seadusepunkt, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
     	if (bindingResult.hasErrors()) {
             uiModel.addAttribute("seadus", seadusepunkt);
             addDateTimeFormatPatterns(uiModel);
             return "seaduses/update";
         }
     	
     	uiModel.asMap().clear();
         seadusepunkt.merge();
 
         String next_url = httpServletRequest.getParameter("next").toString();
     	if (!next_url.equals("")) {
     		return "redirect:"+next_url;
     	}
         
         try {
         	return "redirect:/seaduses/" + encodeUrlPathSegment(seadusepunkt.getSeaduse_ID().getId().toString(), httpServletRequest) + "?form";
         } catch (Exception ex) {
         	return "redirect:/seadusepunkts/" + encodeUrlPathSegment(seadusepunkt.getId().toString(), httpServletRequest);
         }
     }
     
 	@RequestMapping(params = "form", method = RequestMethod.GET)
     public String createForm(@RequestParam(value = "next", required = false) String next_url, @RequestParam(value = "seadusePunkt_ID", required = false) Long parent_seadusePunkt_ID, @RequestParam(required = false) Long seaduse_ID, Model uiModel) {
 		SeadusePunkt model = new SeadusePunkt();
 		model.setSeaduse_ID(Seadus.findSeadus(seaduse_ID));
         uiModel.addAttribute("seadusePunkt", model);
         uiModel.addAttribute("parent_seadusePunkt_ID", parent_seadusePunkt_ID);
         uiModel.addAttribute("next", next_url);
         addDateTimeFormatPatterns(uiModel);
         return "seadusepunkts/create";
     }
 	
 
     @RequestMapping(value = "/{seaduse_punkt_ID}", params = "form", method = RequestMethod.GET)
     public String updateForm(@PathVariable("seaduse_punkt_ID") Long seaduse_punkt_ID, @RequestParam(value = "next", required = false) String next_url, Model uiModel) {
     	SeadusePunkt us = SeadusePunkt.findSeadusePunkt(seaduse_punkt_ID);
     	
         uiModel.addAttribute("next", next_url);
     	
     	try {
     		uiModel.addAttribute("seaduseYlemPunkt_ID", us.getYlemus_seaduse_punkt_ID().getId().toString());
     	} catch (Exception ex) {}
     	uiModel.addAttribute("seadusePunkt", SeadusePunkt.findSeadusePunkt(seaduse_punkt_ID));
         try {
         	uiModel.addAttribute("alam_seadusepunkts", SeadusePunkt.findAllAlamSeadusePunkts(seaduse_punkt_ID));
         } catch (Exception ex) {
         	System.out.println(ex.toString());
         }
         addDateTimeFormatPatterns(uiModel);
         return "seadusepunkts/update";
     }
 	
 	@RequestMapping(value = "/{seadusepunkt_ID}", method = RequestMethod.DELETE)
     public String delete(@PathVariable("seadusepunkt_ID") Long seadusepunkt_ID, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
 		SeadusePunkt model = SeadusePunkt.findSeadusePunkt(seadusepunkt_ID);
         model.remove();
         uiModel.asMap().clear();	
         uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
         uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/seaduses";
 	}
     
 	@RequestMapping(value = "/{seadusepunkt_ID}", params = "delete", method = RequestMethod.GET)
     public String delete(@PathVariable("seadusepunkt_ID") Long seadusepunkt_ID, @RequestParam(value = "next", required = false) String next_url, Model uiModel) {
         SeadusePunkt model = SeadusePunkt.findSeadusePunkt(seadusepunkt_ID);
         if (model!=null) {
         	model.remove();
         	uiModel.asMap().clear();
         	if (!next_url.equals("")) {
         		return "redirect:"+next_url;
         	}
         	return "redirect:/seaduses/" + model.getSeaduse_ID().getId().toString() + "?form";
         } else {
         	uiModel.asMap().clear();
         	return "redirect:/seaduses";
         }
     }
 	
     void addDateTimeFormatPatterns(Model uiModel) {
     	String datetimeformat = "yyyy-dd-MM";
         uiModel.addAttribute("seadusePunkt_kehtiv_alates_date_format", datetimeformat);
         uiModel.addAttribute("seadusePunkt_kehtiv_kuni_date_format", datetimeformat);
         uiModel.addAttribute("seadusePunkt_avatud_date_format", datetimeformat);
         uiModel.addAttribute("seadusePunkt_muudetud_date_format", datetimeformat);
         uiModel.addAttribute("seadusePunkt_suletud_date_format", datetimeformat);
     }
 }
