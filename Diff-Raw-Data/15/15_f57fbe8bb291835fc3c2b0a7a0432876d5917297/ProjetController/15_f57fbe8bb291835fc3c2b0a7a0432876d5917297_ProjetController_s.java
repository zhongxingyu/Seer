 package com.axxx.dps.apv.web.controller;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.axxx.dps.apv.model.Projet;
 import com.axxx.dps.apv.model.Tdr;
 import com.axxx.dps.apv.service.ProjetService;
 import com.axxx.dps.apv.service.TdrService;
 
 @Controller
 public class ProjetController {
     
     @Autowired
     private ProjetService projetService;
     
     @Autowired
     private TdrService tdrService;
 
     @RequestMapping("projet/list")
     public String list(@RequestParam(value="tdrId", required=false) Long tdrId, Map<String, Object> map) {
         List<Projet> projets;
         if (tdrId != null && tdrId != 0) {
             
            Tdr tdr = tdrService.getById(tdrId);
             if (tdr != null) {
                 projets = projetService.listByTdr(tdrId);
                 //projets = tdr.getProjets(); // NO no session
                 //projets = tdrService.getProjets(tdr);
             } else {
                 throw new RuntimeException("no tdr for tdrId " +  tdrId); // TODO better
             }
         } else {
             projets = projetService.list();
         }
         map.put("projets", projets);
         //map.put("projet", new Projet()); // done by initProjet()
         map.put("tdrId", tdrId);
         return "projets";
     }
    
     // LATER put in a separate controller for only create & update requests
     @ModelAttribute("projet")
     public Projet initProjet(@RequestParam(value = "id", required=false) Long projetId, @RequestParam(value="tdrId", required=false) Long tdrId) {
         if (projetId != null) {
             return projetService.getById(projetId);
         } else if (tdrId != null) {
             Projet projet = new Projet();
             projet.setStatus("created");
             Tdr tdr = tdrService.getById(tdrId);
             projet.setTdr(tdr);
             return projet;
         }
         return null;
     }
 
     @RequestMapping(method=RequestMethod.POST, value="/projet/add")
     public String add(@Valid @ModelAttribute("projet") Projet projet, BindingResult result) {
         if (result.hasErrors()) {
             return "projets";
         }
         //Tdr tdr = tdrService.getById(projet.getTdr().getId());projet.setTdr(tdr); // done by initProjet()
         projet.computeTotalBenef();
         projetService.create(projet);
         return "redirect:list?tdrId=" + projet.getTdr().getId();
     }
 
     /**
      * Save the details of a projet
      * @param map
      * @param tdrId 
      * @return
      */
     @RequestMapping(method=RequestMethod.POST, value="/projet/save")
     public String save(@Valid @ModelAttribute("projet") Projet projet, BindingResult result) {
         if (result.hasErrors()) { // validation check, see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-jsr303-valid-example/
             return "redirect:/projet/details/" + projet.getId();
         }
         projet.computeTotalBenef();
         projetService.update(projet);
         return "redirect:/projet/details/" + projet.getId();
     }
     
     @RequestMapping(method=RequestMethod.GET, value="projet/delete/{projetId}")
     public String delete(@PathVariable("projetId") long projetId) {
         projetService.delete(projetId);
         return "redirect:/projet/list";
     }
     
     @RequestMapping(method=RequestMethod.GET, value="projet/details/{projetId}")
     public String details(Map<String, Object> map, @PathVariable("projetId") long projetId) {
         Projet projet = projetService.getById(projetId);
         map.put("projet", projet);
         return "projetDetails";
     }
 
     @RequestMapping(method=RequestMethod.GET, value="/projet/newProjet")
     public String newProjet(@RequestParam("tdrId") long tdrId, Map<String, Object> map) {
         //map.put("projet", new Projet()); // for form
         map.put("tdrId", tdrId);        
         return "newProjet";
     }
     
     @RequestMapping(method=RequestMethod.POST, value="/projet/approve")
     public String approve(@Valid @ModelAttribute("projet") Projet projet, BindingResult result) {
         if (result.hasErrors()) {
             return "projets";
         }
         projet.computeTotalBenef();
         projetService.update(projet);
         try{    
             projet.setStatus("approved");
             projetService.update(projet);
             tdrService.computeTdb(projet.getTdr());
         }
         catch(Exception ex){
             // TODO better error gestion
             ex.printStackTrace();
         }
        return "redirect:list";
     }    
     
 }
