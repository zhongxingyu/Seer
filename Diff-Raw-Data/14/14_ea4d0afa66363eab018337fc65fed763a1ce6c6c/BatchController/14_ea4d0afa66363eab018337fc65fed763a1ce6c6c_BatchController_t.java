 /*
  * Contains following controllers for batch page:
  *  - batch/{id}: fetches batch by id from db, gives it in model to view 'batchView'
  *  - batch: fetches all batches from db, gives them in model to view 'batch' 
  *  - addBatch: 
  */
 /**
  *
  */
 package ohtu.radioaine.controller;
 
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.service.BatchService;
 import ohtu.radioaine.service.EventService;
 import ohtu.radioaine.service.StorageService;
 import ohtu.radioaine.service.SubstanceService;
 import ohtu.radioaine.tools.EventHandler;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 import ohtu.radioaine.tools.Time;
 import org.springframework.web.bind.annotation.*;
 
 /**
  * Controllers for batch creation and viewing
  *
  * @author rmjheino
  */
 @Controller
 public class BatchController {
 
     @Autowired
     private BatchService batchService;
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private EventService eventService;
     @Autowired
     private StorageService storageService;
 
     @RequestMapping(value = "batch/{id}", method = RequestMethod.GET)
     public String getBatchById(@PathVariable Long id, Model model) {
         model.addAttribute("batch", batchService.read(id));
         model.addAttribute("storages", storageService.list());
         return "batchView";
     }
     
 
     @RequestMapping(value = "doCheck/{id}+{sid}", method = RequestMethod.POST)
     public String qualityCheck(@PathVariable Long id,
             @PathVariable Long sid,
             @RequestParam String sig,
             @RequestParam Integer qualityCheck) {
         if (sig.length() < 2) {
             if (sid <= 0) {
                 return "redirect:/batch/" + id;
             }
             return "redirect:/substance/" + sid;
         }
         Batch batch = batchService.read(id);
         batch.setQualityCheck(qualityCheck);
         batchService.createOrUpdate(batch);
         updateSubstance(batch.getSubstance());
         Event event = EventHandler.qualityCheckEvent(batch, sig);
         eventService.createOrUpdate(event);
         if (sid <= 0) {
             return "redirect:/batch/" + id;
         }
         return "redirect:/substance/" + sid;
     }
 
     @RequestMapping(value = "batch", method = RequestMethod.GET)
     public String batchList(Model model) {
         model.addAttribute("batches", batchService.list());
         return "batchView";
     }
 
     @RequestMapping(value = "addBatch", method = RequestMethod.GET)
     public String addbatchView(Model model) {
         model.addAttribute("batch", new BatchFormObject());
         model.addAttribute("substances", substanceService.list());
         model.addAttribute("storages", storageService.list());
         
         String names = "'";
         for(int i = 0; i < storageService.storageNamesList().size(); i++)   {
             if(!storageService.list().get(i).isHidden())  
                 names += storageService.storageNamesList().get(i) + "^separate^";
             else
                 names += "^hidden^^separate^";
         }
         names += "'";
         model.addAttribute("storageNames", names);
         
         return "addBatchView";
     }
     
     @RequestMapping(value = "removeFromBatch/{id}", method = RequestMethod.GET)
     public String removeFromBatchView(@PathVariable Long id, Model model) {
         model.addAttribute("batch", batchService.read(id));
         model.addAttribute("storages", storageService.list());
         return "removeFromBatchView";
     }
     
     @RequestMapping(value = "removeFromBatch/{id}", method = RequestMethod.POST)
     public String removeFromBatch(@PathVariable Long id, @RequestParam Integer[] amounts, @RequestParam String remover, @RequestParam String reason) {
         Batch temp = batchService.read(id);
         Long[][] locs = temp.getStorageLocations();
         int tempTotalAmount = 0;
         for(int i=0; i<amounts.length;++i){
            if(amounts[i] != null && locs[i][1] != null)    {
                if(amounts[i] > 0 && locs[i][1] >= amounts[i])   {
                    System.out.println(locs[i][1]);
                    locs[i][1] -= amounts[i];
                    System.out.println(locs[i][1]);
                }
             }
            if(locs[i][1] != null)
                tempTotalAmount += locs[i][1];
         }
         temp.setAmount(tempTotalAmount);
         temp.setStorageLocations(locs);
         batchService.createOrUpdate(temp);
         Event event = EventHandler.removeFromBatchEvent(temp, reason, remover);
         eventService.createOrUpdate(event);
             
         return "redirect:/batch/" + id;
     }
     
     @RequestMapping(value = "batch", method = RequestMethod.POST)
     public String addBatch(@Valid @ModelAttribute("batch") BatchFormObject bfo, BindingResult result) {
         if (result.hasErrors()) {
             System.out.println(result);
             return "redirect:/addBatch";
         }
         Batch batch = createBatch(bfo);
         Batch temp = batchService.read(batch.getBatchNumber(), bfo.getSubstance());
         if (temp == null) {
             batch = batchService.createOrUpdate(batch);
             Event event = EventHandler.newBatchEvent(batch, bfo.getSignature());
             eventService.createOrUpdate(event);
         } else {
             batch = addToBatch(temp.getId(), bfo);
         }
         return "redirect:/batch/" + batch.getId();
     }
 
     @RequestMapping(value = "updateBatch/{id}")
     public String batchUpdateRequest(Model model, @PathVariable Long id) {
         model.addAttribute("substances", substanceService.list());
         model.addAttribute("batch", batchService.read(id));
         model.addAttribute("storages", storageService.list());
         String names = "'"; 
         for(int i = 0; i < storageService.storageNamesList().size(); i++)   {
             names += storageService.storageNamesList().get(i) + "^separate^";
         }
         names += "'";
         model.addAttribute("storageNames", names);
         return "batchUpdateView";
     }
 
     @RequestMapping(value = "updateBatch/{id}", method = RequestMethod.POST)
     public String batchUpdate(@Valid @ModelAttribute("batch") BatchFormObject bfm,
             BindingResult result,
             Model model,
             @PathVariable Long id) {
 
         
         //Checks if the new total amount differs from the old total amount and if it does, the update fails
         Batch temp = batchService.read(id);
         int newTotalAmount = 0;
         for (int i = 0; i < bfm.getStorageLocations().length; i++) {
             System.out.println("storagelocationi on : " + bfm.getStorageLocations().length);
             if(bfm.getStorageLocations()[i][1] != null)
                 newTotalAmount += bfm.getStorageLocations()[i][1];
         }
         
         if (result.hasErrors() || temp.getAmount() != newTotalAmount) {
 
             System.out.println(result);
             return "redirect:/updateBatch/" + id;
         }
         System.out.println("ZZZ3");
         updateBatch(id, bfm);
         return "redirect:/batch/" + id;
     }
 
     private Batch updateBatch(Long id, BatchFormObject bfo) {
         Batch batch = batchService.read(id);
         Substance substance = batch.getSubstance();
         batch.setStorageLocations(bfo.getStorageLocations());
         batch.setSubstanceVolume(bfo.getSubstanceVolume());
         batch.setBatchNumber(bfo.getBatchNumber());
         batch.setNote(bfo.getNote());
         batch.setArrivalDate(Time.parseDate(bfo.getArrivalDate()));
         batch.setExpDate(Time.parseDate(bfo.getExpDate()));
         long temp = 0;
         for (int i = 0; i < bfo.getStorageLocations().length; i++) {
             if(bfo.getStorageLocations()[i][1] != null)
                 temp += bfo.getStorageLocations()[i][1];
         }
         bfo.setAmount((int)temp);
         //Checks if batch substance has been changed
         if (batch.getSubstance().getId() != bfo.getSubstance()) {
             int oldAmount = batch.getAmount();
             batch.setAmount(bfo.getAmount());
             Substance newSubstance = (Substance) substanceService.read(bfo.getSubstance());
             substance.setTotalAmount(substance.getTotalAmount() - oldAmount);
             newSubstance.setTotalAmount(newSubstance.getTotalAmount() + batch.getAmount());
             batch.setSubstance(newSubstance);
             substanceService.createOrUpdate(newSubstance);
         } else {
             int amountChange = amountChange(batch, bfo);
             batch.setAmount(batch.getAmount() + amountChange);
             substance.setTotalAmount(substance.getTotalAmount() + amountChange);
         }
         batch = batchService.createOrUpdate(batch);
         substanceService.createOrUpdate(substance);
         Event event = EventHandler.updateBatchEvent(batch, bfo.getSignature());
         eventService.createOrUpdate(event);
         return batch;
     }
 
     private int amountChange(Batch batch, BatchFormObject bfm) {
         int tempAmount;
         for (int i = 0; i < bfm.getStorageLocations().length; i++) {
             System.out.println("kaapissa " + i + " " + bfm.getStorageLocations()[i][1] + " kpl");
         }
         System.out.println("bfm.getAmount() : " + bfm.getAmount());
         System.out.println("batch.getAmount() : " + batch.getAmount());
         if (batch.getAmount() > bfm.getAmount()) {
             tempAmount = -(batch.getAmount() - bfm.getAmount());
         } else if (batch.getAmount() < bfm.getAmount()) {
             tempAmount = (bfm.getAmount() - batch.getAmount());
         } else {
             tempAmount = 0;
         }
         System.out.println("Palautuva tempAmount : " + tempAmount);
         return tempAmount;
     }
 
     @RequestMapping(value = "batchDelete/{id}", method = RequestMethod.POST)
     public String deleteBatch(@RequestParam String name, @RequestParam Integer amount, @PathVariable Long id) {
         Batch batch = batchService.read(id);
         Substance substance = batch.getSubstance();
         int total = batch.getAmount() - amount;
         if (total >= 0 && name.length() >= 1) {
             substance.setTotalAmount(substance.getTotalAmount() - amount);
             substanceService.createOrUpdate(substance);
             batch.setAmount(total);
             batchService.createOrUpdate(batch);
         }
         return "redirect:/batch/" + id;
     }
 
     private Batch createBatch(BatchFormObject bfo) {
         Batch batch = new Batch();
         batch.setBatchNumber(bfo.getBatchNumber());
         batch.setNote(bfo.getNote());
         batch.setArrivalDate(Time.parseDate(bfo.getArrivalDate()));
         batch.setExpDate((Time.parseDate(bfo.getExpDate())));
         long temp = 0;
         for (int i = 0; i < bfo.getStorageLocations().length; i++) {
             if(bfo.getStorageLocations()[i][1] != null)
                 temp += bfo.getStorageLocations()[i][1];
         }
 
         bfo.setAmount((int)temp);
         batch.setAmount(bfo.getAmount());
         batch.setSubstanceVolume(bfo.getSubstanceVolume());
         batch.setStorageLocations(bfo.getStorageLocations());
         Substance substance = (Substance) substanceService.read(bfo.getSubstance());
         if(batch.getExpDate().compareTo(substance.getOldestDate()) < 0){
             substance.setOldestDate(batch.getExpDate());
             substance.setWarningDate(Time.parseWarningDate(batch.getExpDate()));
         }
         substance.setTotalAmount(substance.getTotalAmount() + bfo.getAmount());
         substanceService.createOrUpdate(substance);
         batch.setSubstance(substance);
         batch.setManufacturer(substance.getManufacturer());
         batch.setSupplier(substance.getSupplier());
         return batch;
     }
 
     private Batch addToBatch(Long id, BatchFormObject bfm) {
         Batch batch = batchService.read(id);
         batch.setAmount(batch.getAmount() + bfm.getAmount());
         batch.setNote(batch.getNote() + "\n" + bfm.getNote());
         Event event = EventHandler.addToBatchEvent(batch, bfm.getSignature());
         eventService.createOrUpdate(event);
         return batchService.createOrUpdate(batch);
 
     }
 
     private void updateSubstance(Substance substance) {
         Substance temp = (Substance) substanceService.read(substance.getId());
         int status = temp.getQualityStatus();
         
         List<Batch> batches = batchService.listSubstanceBatches(temp.getId());
         for(Batch batch : batches){
             if(batch.getQualityCheck() == 2 && status == 0 ){ 
                 status = 2;
             }
             else if(batch.getQualityCheck() == 1 && (status == 0 | status == 2)){ 
                 status = 1;
             }
         }
         
         temp.setQualityStatus(status);
         substanceService.createOrUpdate(temp);
     }
 }
