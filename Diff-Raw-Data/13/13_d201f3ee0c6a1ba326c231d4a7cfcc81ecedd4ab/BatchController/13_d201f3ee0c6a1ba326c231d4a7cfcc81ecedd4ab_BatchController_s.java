 package ohtu.radioaine.controller;
 
 import javax.validation.Valid;
 import ohtu.radioaine.domain.Batch;
 import ohtu.radioaine.domain.BatchFormObject;
 import ohtu.radioaine.domain.Substance;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.BatchService;
 import ohtu.radioaine.service.SubstanceService;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 import ohtu.radioaine.tools.Time;
 
 @Controller
 public class BatchController {
 
     @Autowired
     private BatchService batchService;
     @Autowired
     private SubstanceService substanceService;
 
     @RequestMapping(value = "batch/{id}", method = RequestMethod.GET)
     public String getBatchById(@PathVariable Integer id, Model model) {
         model.addAttribute("batch", batchService.read(id));
         return "batchView";
     }
 
     @RequestMapping(value = "batch", method = RequestMethod.GET)
     public String batchList(Model model) {
         model.addAttribute("batches", batchService.listaa());
         return "batchView";
     }
 
     @RequestMapping(value = "batch", method = RequestMethod.POST)
     public String addBatch(@Valid @ModelAttribute("batch") BatchFormObject bfm, BindingResult result) {
         if (result.hasErrors()) {
             return "addBatchView";
         }
         Batch batch = batchService.create(createBatch(bfm));
         return "redirect:/batch/" + batch.getId();
     }
 
     private Batch createBatch(BatchFormObject bfm) {
         Batch batch = new Batch();
         batch.setBatchNumber(bfm.getBatchNumber());
         batch.setNote(bfm.getNote());
         batch.setArrivalDate(Time.getTimestamp());
         batch.setExpDate((Time.getTimestamp()));
         batch.setSubstanceCount(bfm.getAmount());
      //  Substance substance = substanceService.get(bfm.getSubstance());
         Substance substance = createTestSubstance(bfm); //luodaan testiaine testausta varten
         batch.setSubstance(substance);
         batch.setManufacturer(substance.getManufacturer());
         batch.setSupplier(substance.getSupplier());
         return batch;
     }
    
     private Substance createTestSubstance(BatchFormObject bfm) {
         Substance substance = new Substance("" + bfm.getSubstance());
         substance.setName("LääkeaineX");
         substance.setNeedsColdStorage(true);
         substance.setAlertLimit1(5);
         substance.setAlertLimit2(8);
         substance.setHasBeenOrdered(false);
         substance.setManufacturer("Pekan lääkefirma");
         substance.setSupplier("Taavon tukkuliike");
         return substance;
     }
 }
