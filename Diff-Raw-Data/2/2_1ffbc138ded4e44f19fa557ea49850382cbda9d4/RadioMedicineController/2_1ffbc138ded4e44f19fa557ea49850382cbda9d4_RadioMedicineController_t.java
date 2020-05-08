 package ohtu.radioaine.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.*;
 import ohtu.radioaine.service.*;
 import ohtu.radioaine.tools.EventHandler;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMethod;
 import ohtu.radioaine.tools.Time;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 @Controller
 public class RadioMedicineController {
 
     long GENERATOR = 1;
     long KIT = 0;
     long OTHER = 2;
     @Autowired
     private BatchService batchService;
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private EluateService eluateService;
     @Autowired
     private RadioMedService radioMedService;
     @Autowired
     private StorageService storageService;
     @Autowired
     private EventService eventService;
 
     @RequestMapping(value = "createRadioMedicine", method = RequestMethod.GET)
     public String createRadioMedicineViewCTRL(Model model) {
         model.addAttribute("radioMedicine", new RadioMedicineFormObject());
         model.addAttribute("generators", batchService.getBatchesByType(GENERATOR));
         model.addAttribute("kits", batchService.getBatchesByType(KIT));
         model.addAttribute("others", batchService.getBatchesByType(OTHER));
        model.addAttribute("eluates", eluateService.getAllByDate(Time.getTodayDate()));
         model.addAttribute("storages", storageService.list());
 
         return "createRadioMedicine";
     }
 
     @RequestMapping(value = "modifyRadioMed/{id}", method = RequestMethod.GET)
     public String updateRadioMedViewCTRL(Model model, @PathVariable Long id) {
         model.addAttribute("radioMedicineForm", new RadioMedicineFormObject());
         model.addAttribute("radioMedicine", radioMedService.read(id));
         model.addAttribute("generators", batchService.getBatchesByType(GENERATOR));
         model.addAttribute("kits", batchService.getBatchesByType(KIT));
         model.addAttribute("others", batchService.getBatchesByType(OTHER));
         model.addAttribute("eluates", eluateService.list());
         model.addAttribute("storages", storageService.list());
 
         return "radioMedUpdateView";
     }
 
     @RequestMapping(value = "modifyRadioMed/{id}", method = RequestMethod.POST)
     public String modifyRadioMedCTRL(@Valid @ModelAttribute("radioMedicine") RadioMedicineFormObject rmfo,
             @PathVariable Long id,
             BindingResult result) {
         if (result.hasErrors()) {
             System.out.println(result);
             return "radioMedUpdateView";
         }
         updateRadioMed(id, rmfo);
         return "redirect:/RadioMedicine/" + id;
     } 
 
     @RequestMapping(value = "createRadioMedicine", method = RequestMethod.POST)
     public String newRadioMedicineCTRL(@Valid @ModelAttribute("radioMedicine") RadioMedicineFormObject rmfo, BindingResult result, @RequestParam("storageIds") int[] storageIds) {
         for(int i=0; i < storageIds.length; i++)    {
             System.out.println("storage id " + (i+1) + ": " + storageIds[i]);
         }
         if (result.hasErrors()) {
 //            System.out.println(result);
             return "createRadioMedicine";
         }
 
         RadioMedicine newRadioMedicine = radioMedService.createOrUpdate(createRD(rmfo, storageIds));
         return "redirect:/frontpage";
     }
 
     @RequestMapping("RadioMedicine/{id}")
     public String radioMedicineViewCTRL(Model model, @PathVariable Long id) {
         model.addAttribute("radioMedicine", radioMedService.read(id));
         model.addAttribute("storages", storageService.list());
         return "radioMedicineView";
     }
    
     @RequestMapping("removeRadioMedRequest/{id}")
     public String removalRqeust(Model model, @PathVariable Long id) {     
         model.addAttribute("radioMedicine", radioMedService.read(id));
         model.addAttribute("storages", storageService.list());
         return "radioMedRemovalView";
     }
     
     @RequestMapping(value = "removeRadioMed/{id}", method = RequestMethod.POST)
     public String removeRadioMed(@RequestParam String reason,
     @RequestParam String remover,
     @PathVariable Long id) {     
         Event event = EventHandler.removeRadioMedEvent(reason, remover, radioMedService.read(id));
         eventService.createOrUpdate(event);
         radioMedService.delete(id);
         return "redirect:/frontpage";
     }
 
     private RadioMedicine createRD(RadioMedicineFormObject rmfo, int[] storageIds) {
         RadioMedicine radioMedicine = new RadioMedicine();
 
         radioMedicine.setNote(rmfo.getNote());
         radioMedicine.setSignature(rmfo.getSignature());
         if (rmfo.getStrength().equals("")) {
             radioMedicine.setVolume(0.0);
         } else {
             radioMedicine.setStrength(Double.parseDouble(rmfo.getVolume()));
         }
         radioMedicine.setDate(Time.parseDate(rmfo.getDate()));
         if (rmfo.getStrength().equals("")) {
             radioMedicine.setStrength(0.0);
         } else {
             radioMedicine.setStrength(Double.parseDouble(rmfo.getStrength()));
         }
         radioMedicine.setUnit(rmfo.getUnit());
         radioMedicine.setStorageLocation(rmfo.getStorageLocation());
         radioMedicine.setTimestamp(Time.parseTimeStamp(rmfo.getDate() + " " + rmfo.getHours() + ":" + rmfo.getMinutes()));
         List<Eluate> eluates = new ArrayList<Eluate>();
         Long[] eluatesTable = rmfo.getEluates();
         for (int i = 0; i < eluatesTable.length; ++i) {
             if (eluatesTable[i] != null) {
                 eluates.add(eluateService.read(eluatesTable[i]));
             }
         }
 
         List<Batch> kits = new ArrayList<Batch>();
         Long[] kitsTable = rmfo.getKits();
         for (int i = 0; i < kitsTable.length; ++i) {
             System.out.println("kitti indexi: " + i + " kittiTable koko: " + kitsTable.length +" storageIds koko: " + storageIds.length + " storage sisältö: " + storageIds[i]);
             if (kitsTable[i] != null) {
                 kits.add(batchService.read(kitsTable[i]));
                 decreaseKitAmountsOnCreation(i, kitsTable, storageIds);
             }
         }
 
         List<Batch> others = new ArrayList<Batch>();
         Long[] othersTable = rmfo.getOthers();
         for (int i = 0; i < othersTable.length; ++i) {
             if (othersTable[i] != null) {
                 others.add(batchService.read(othersTable[i]));
             }
         }
 
         radioMedicine.setEluates(eluates);
         radioMedicine.setOthers(others);
         radioMedicine.setKits(kits);
 
         return radioMedicine;
     }
     
     private void decreaseKitAmountsOnCreation(int i, Long[] kitsTable, int[] storageIds)    {
         for(int j=0; j < batchService.read(kitsTable[i]).getStorageLocations().length; j++) {
             if(batchService.read(kitsTable[i]).getStorageLocations()[j][0] != null)    {
                 if(batchService.read(kitsTable[i]).getStorageLocations()[j][0] == storageIds[i])    {
                     Batch temp = batchService.read(kitsTable[i]);
                     Long[][] tempStorages = temp.getStorageLocations();
                     tempStorages[j][1] = tempStorages[j][1] - 1;
                     temp.setStorageLocations(tempStorages);
                     temp.setAmount(temp.getAmount() - 1);
                     batchService.createOrUpdate(temp);
                     Substance substanceTemp = temp.getSubstance();
                     substanceTemp.setTotalAmount(substanceTemp.getTotalAmount() - 1);
                     substanceService.createOrUpdate(substanceTemp);
                 }        
             }
         }
     }
     
     private void updateRadioMed(Long id, RadioMedicineFormObject rmfo) {
         RadioMedicine radioMedicine = radioMedService.read(id);
 
         radioMedicine.setNote(rmfo.getNote());
         radioMedicine.setVolume(Double.parseDouble(rmfo.getVolume()));
         radioMedicine.setDate(Time.parseDate(rmfo.getDate()));
         radioMedicine.setStrength(Double.parseDouble(rmfo.getStrength()));
         radioMedicine.setUnit(rmfo.getUnit());
         radioMedicine.setStorageLocation(rmfo.getStorageLocation());
         radioMedicine.setTimestamp(Time.parseTimeStamp(rmfo.getDate() + " " + rmfo.getHours() + ":" + rmfo.getMinutes()));
         List<Eluate> eluates = new ArrayList<Eluate>();
         Long[] eluatesTable = rmfo.getEluates();
         for (int i = 0; i < eluatesTable.length; ++i) {
             if (eluatesTable[i] != null) {
                 eluates.add(eluateService.read(eluatesTable[i]));
             }
         }
 
         List<Batch> kits = new ArrayList<Batch>();
         Long[] kitsTable = rmfo.getKits();
         for (int i = 0; i < kitsTable.length; ++i) {
             if (kitsTable[i] != null) {
                 kits.add(batchService.read(kitsTable[i]));
             }
 
         }
 
         List<Batch> others = new ArrayList<Batch>();
         Long[] othersTable = rmfo.getOthers();
         for (int i = 0; i < othersTable.length; ++i) {
             if (othersTable[i] != null) {
                 others.add(batchService.read(othersTable[i]));
             }
         }
 
         radioMedicine.setEluates(eluates);
         radioMedicine.setOthers(others);
         radioMedicine.setKits(kits);
         radioMedService.createOrUpdate(radioMedicine);
     }
 }
 
