 package ohtu.radioaine.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.BatchService;
 import ohtu.radioaine.service.SubstanceService;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMethod;
 import ohtu.radioaine.service.EluateService;
 import ohtu.radioaine.tools.Time;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 
 @Controller
 public class RadioMedicineController {
 
     @Autowired
     private BatchService batchService;
     
     @Autowired
     private SubstanceService substanceService;
     
     @Autowired
     private EluateService eluateService;
     
     @RequestMapping(value = "createRadioMedicine", method = RequestMethod.GET)
     public String createRadioMedicineView(Model model) {
         //model.addAttribute("eluates", eluateService.list());
         model.addAttribute("radioMedicine", new RadioMedicineFormObject());
         model.addAttribute("substances", substanceService.list());
         model.addAttribute("batches", batchService.list());
         model.addAttribute("substanceBatches", batchService.listSubstanceBatches(2));
         
         return "createRadioMedicine";
     }
     
     @RequestMapping(value = "createRadioMedicine", method = RequestMethod.POST)
     public String newRadioMedicine(@Valid @ModelAttribute("radioMedicine") RadioMedicineFormObject rmfo, BindingResult result) {
         if (result.hasErrors()) {
             return "createRadioMedicine";
         }
         RadioMedicine newRadioMedicine = createRD(rmfo);
         return "redirect:/RadioMedicine/" + newRadioMedicine.getId();
     }
     
     @RequestMapping("RadioMedicine/{id}")
     public String radioMedicineView(@PathVariable Integer id) {
         return "frontpage";
     }
     
 
     private RadioMedicine createRD(RadioMedicineFormObject rmfo) {
         RadioMedicine radioMedicine = new RadioMedicine();
         
         radioMedicine.setNote(rmfo.getNote());
         radioMedicine.setSignature(rmfo.getSignature());
         radioMedicine.setVolume(rmfo.getVolume());
         radioMedicine.setTimestamp(Time.parseDate(rmfo.getDate()));
         radioMedicine.setStorageLocation(rmfo.getStorageLocation());
         
         List<Eluate> eluates = new ArrayList<Eluate>();
         eluates.add(eluateService.read(rmfo.getEluates()));
         List<Batch> solvents = new ArrayList<Batch>();
         solvents.add(batchService.read(rmfo.getSolvent()));
         List<Batch> kits = new ArrayList<Batch>();
        kits.add(batchService.read(rmfo.getKits()));
         
         radioMedicine.setEluates(eluates);
         radioMedicine.setSolvents(solvents);
         radioMedicine.setKits(kits);
         
         return radioMedicine;
     }
     
 }
