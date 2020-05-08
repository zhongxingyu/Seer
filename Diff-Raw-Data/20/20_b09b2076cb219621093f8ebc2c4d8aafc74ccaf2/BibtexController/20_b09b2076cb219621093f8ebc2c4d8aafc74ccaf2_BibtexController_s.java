 package fi.helsinki.jkmv;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class BibtexController {
     
         @Autowired
         public ReferenceService referenceService;
         
         /*
          * viitteen lisäys osoitteessa /app/add
          */
        @RequestMapping(value = "add", method = RequestMethod.POST)
        public String add(@ModelAttribute("reference") Reference reference) {
            if(reference.hasValidType() == true)
                referenceService.add(reference);
            // else valitetaan jotain viewiin tai jonnekkin :)
            return "redirect:/app/list";
        }
        
         /*
          * lomakkeen näyttäminen lisäämistä varten
          */
         @RequestMapping(value = "add", method = RequestMethod.GET)
         public String showAdd(Model model) { 
             return "addNew";
         }
 
         /*
          * viitteiden listaaminen osoitteessa app/list
          */
         @RequestMapping(value="list", method=RequestMethod.GET)
         public String getReferences(Model model) {
         	if(referenceService.list().size() == 0) {
         		model.addAttribute("message", "Empty reference list.");
         	}
 		model.addAttribute("referencelist", this.referenceService.list());   
 		return "list";
         }
         
         
         @RequestMapping(value="bibtex", method=RequestMethod.GET)
         public String showBibtex(Model model) {
         	if(referenceService.list().size() == 0) {
         		model.addAttribute("message", "Empty reference list.");
         	}
         	
 		model.addAttribute("bibtex", referenceService.renderBibtex());   
 		return "bibtex";
         }  
 }
