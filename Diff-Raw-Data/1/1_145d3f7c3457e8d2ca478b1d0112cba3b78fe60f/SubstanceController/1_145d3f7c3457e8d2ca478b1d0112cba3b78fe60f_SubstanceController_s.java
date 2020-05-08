 package ohtu.radioaine.controller;
 
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.Batch;
 import ohtu.radioaine.domain.Substance;
 import ohtu.radioaine.domain.SubstanceFormObject;
 import ohtu.radioaine.service.BatchService;
 import ohtu.radioaine.service.EventService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.SubstanceService;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class SubstanceController {
 
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private BatchService batchService;
     @Autowired
     private EventService eventService;
 
     @RequestMapping(value = "substance/{id}", method = RequestMethod.GET)
     public String getSubstanceByIdCTRL(@PathVariable Long id, Model model) {
         Substance substance = (Substance) substanceService.read(id);
         model.addAttribute("substance", substance);
         model.addAttribute("substanceBatches", batchService.listSubstanceBatches(id));
         model.addAttribute("substanceHistory", eventService.list(substance.getName()));
         return "substanceBatches";
     }
 
     @RequestMapping(value = "addSubstance/{id}", method = RequestMethod.GET)
     public String addSubstanceViewCTRL(Model model, @PathVariable int id) {
         SubstanceFormObject sfo = new SubstanceFormObject();
         if (id < 0 || id > 2) {
             return "substanceView";
         }
         sfo.setType(id);
         model.addAttribute("substance", sfo);
         return "addSubstanceView";
     }
 
     @RequestMapping(value = "substance/{id}", method = RequestMethod.POST)
     public String addSubstanceCTRL(@Valid @ModelAttribute("substance") SubstanceFormObject sfo, BindingResult result, @PathVariable int id) {
         if (result.hasErrors() || (id < 0 || id > 2)) {
             return "addSubstanceView";
         }
         sfo.setType(id);
         createSubstance(sfo, new Substance());
         return "redirect:/substanceView";
     }
 
     private void createSubstance(SubstanceFormObject sfo, Substance substance) {
         substance.setType(sfo.getType());
         substance.setName(sfo.getName());
         if (sfo.getHalflife() == null || sfo.getHalflife().equals("")) {
             substance.setHalflife(0.0);
         } else {
             substance.setHalflife(Double.parseDouble(sfo.getHalflife()));
         }
         substance.setGenericName(sfo.getGenericName());
         substance.setManufacturer(sfo.getManufacturer());
         substance.setSupplier(sfo.getSupplier());
         substance.setWarningBeforeDays(sfo.getWarningBeforeDays());
         substance.setWarningBeforeAmount(sfo.getWarningBeforeAmount());
         substance.setVolume(sfo.getVolume());
         substance.setQualityControl(sfo.getQualityControl());
         substance.setStrength(sfo.getStrength());
         substance.setInUse(true);
         if (sfo.getType() == 1) {
             //substance.setHalflife(Double.parseDouble(sfo.getHalflife()));
             substance.setEluateName(sfo.getEluateName());
         }
         substanceService.createOrUpdate(substance);
     }
 
     @RequestMapping(value = "substance", method = RequestMethod.GET)
     public String listaaCTRL(Model model) {
         model.addAttribute("substances", substanceService.list());
         return "substanceViewTest";
     }
 
     @RequestMapping(value = "updateSubstance/{id}", method = RequestMethod.POST)
     public String updateSubstanceCTRL(@Valid @ModelAttribute("substance") SubstanceFormObject sfo,
             BindingResult result,
             Model model,
             @PathVariable Long id) {
         createSubstance(sfo, (Substance) substanceService.read(id));
 //        updateSubstance(id, sfm);
         return "redirect:/updateSubstance/" + id;
     }
 
 //    private void updateSubstance(Long id, SubstanceFormObject sfo) {
 //        Substance substanceToUpdate = (Substance) substanceService.read(id);
 //        substanceToUpdate.setType(sfo.getType());
 //        substanceToUpdate.setName(sfo.getName());
 //        substanceToUpdate.setHalflife(sfo.getHalflife());
 //        substanceToUpdate.setGenericName(sfo.getGenericName());
 //        substanceToUpdate.setManufacturer(sfo.getManufacturer());
 //        substanceToUpdate.setSupplier(sfo.getSupplier());
 //        substanceToUpdate.setAlertLimit1(sfo.getAlertLimit1());
 //        substanceToUpdate.setAlertLimit2(sfo.getAlertLimit2());
 //        substanceToUpdate.setVolume(sfo.getVolume());
 //        substanceToUpdate.setTotalAmount(0);
 //        substanceToUpdate.setQualityControl(sfo.getQualityControl());
 //        substanceToUpdate.setStrength(sfo.getStrength());
 //        if (sfo.getType() == 1) {
 //            substanceToUpdate.setHalflife(sfo.getHalflife());
 //            substanceToUpdate.setEluateName(sfo.getEluateName());
 //        }
 //        substanceService.createOrUpdate(substanceToUpdate);
 //    }
 
     @RequestMapping(value = "updateSubstance/{id}", method = RequestMethod.GET)
     public String updateSubstanceViewCTRL(Model model, @PathVariable Long id) {
         System.out.println(id);
         model.addAttribute("substance", substanceService.read(id));
         model.addAttribute("substanceInUse", checkIfSubstancesInUse(id));
         return "substanceUpdateView";
     }
     
     @RequestMapping(value = "deactivateSubstance/{id}", method = RequestMethod.POST)
     public String deactivateSubstanceCTRL(@PathVariable Long id, Model model) {
         Substance sub = (Substance) substanceService.read(id);
         sub.setInUse(false);
         substanceService.createOrUpdate(sub);
         
         return "redirect:/updateSubstance/"+id;
     }
     
     @RequestMapping(value = "activateSubstance/{id}", method = RequestMethod.POST)
     public String activateSubstanceCTRL(@PathVariable Long id, Model model) {
         Substance sub = (Substance) substanceService.read(id);
         sub.setInUse(true);
         substanceService.createOrUpdate(sub);
         
         return "redirect:/updateSubstance/"+id;
     }
     
     private boolean checkIfSubstancesInUse(Long id)    {
         boolean isInUse = false;
         List<Batch> temp = batchService.listSubstanceBatches(id);
         for(int i=0; i < temp.size(); i++) {
             if(temp.get(i).getAmount() > 0)
                 isInUse = true;
         }
         return isInUse;
     }
 }
