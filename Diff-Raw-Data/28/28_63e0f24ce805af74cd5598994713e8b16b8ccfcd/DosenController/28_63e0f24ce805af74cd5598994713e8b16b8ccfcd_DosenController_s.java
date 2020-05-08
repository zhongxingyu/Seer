 package id.ac.pcr.springhibernate.web.controller;
 
 
 import id.ac.pcr.springhibernate.model.Dosen;
 import id.ac.pcr.springhibernate.service.DosenService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class DosenController {
 
     @Autowired
     @Qualifier("dosenService")
     private DosenService dosenService;
 
 /* -------------------------- Other Methods -------------------------- */
 
     @RequestMapping(value = "/dosen/new", method = RequestMethod.GET)
     public String create(ModelMap modelMap) {
         Dosen dosen = new Dosen();
         modelMap.addAttribute("dosen", dosen);
 
         return "dosen/create";
     }
 
     @RequestMapping(value = "/dosen/{id}/delete", method = RequestMethod.GET)
     public String delete(@PathVariable("id") Long id) {
        Dosen data = dosenService.findDosen(id);
        dosenService.delete(data);
         return "redirect:/dosen";
     }
 
     @RequestMapping(value = "/dosen/{id}/edit", method = RequestMethod.GET)
     public String edit(ModelMap modelMap, @PathVariable("id") Long id) {
         Dosen dosen = dosenService.findDosen(id);
         modelMap.addAttribute("dosen", dosen);
 
 
         return "dosen/edit";
     }
 
     @RequestMapping(value = "/dosen", method = RequestMethod.GET)
     public String index(ModelMap modelMap) {
         modelMap.addAttribute("data", dosenService.findDosens());
         return "dosen/index";
     }
 
     @RequestMapping(value = "/dosen", method = RequestMethod.POST)
     public String save(@ModelAttribute("dosen") Dosen dosen, BindingResult result, ModelMap modelMap) {
         if (dosen == null) throw new IllegalArgumentException("An Object is required");
         if (result.hasErrors()) {
             modelMap.addAttribute("dosen", dosen);
 
             return "dosen/create";
         }
 
         dosenService.save(dosen);
 
         return "redirect:/dosen/" + dosen.getId();
     }
 
     @RequestMapping(value = "/dosen/{id}", method = RequestMethod.GET)
     public String show(ModelMap modelMap, @PathVariable("id") Long id) {
         Dosen data = dosenService.findDosen(id);
         modelMap.addAttribute("data", data);
 
         return "dosen/show";
     }
 
     @RequestMapping(value = "/dosen", method = RequestMethod.PUT)
     public String update(@ModelAttribute("dosen") Dosen dosen, BindingResult result, ModelMap modelMap) {
         if (dosen == null) throw new IllegalArgumentException("A Product is required");
         if (result.hasErrors()) {
             modelMap.addAttribute("dosen", dosen);
 
             return "dosen/edit";
         }
 
         dosenService.save(dosen);
 
 
         return "redirect:/dosen/" + dosen.getId();
     }
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
 //        binder.registerCustomEditor(User.class, new UserEditor(securityService));
     }
 
 }
