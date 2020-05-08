 /*
  * :
  *  - admin: directs caller to admin page.
  */
 package ohtu.radioaine.controller;
 
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.radioaine.domain.*;
 import ohtu.radioaine.service.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.tools.Time;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 /**
  * Controllers for admin page.
  *
  * @author rmjheino
  *
  */
 @Controller
 public class AdminController {
 
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private StorageService storageService;
     @Autowired
     private BatchService batchService;
     @Autowired
     private EluateService eluateService;
     @Autowired
     private RadioMedService radioMedService;
 
     @RequestMapping("admin")
     public String adminView(Model model) {
         model.addAttribute("substances", substanceService.list());
         model.addAttribute("warning", Time.getWarningDate());
         return "admin";
     }
 
     @RequestMapping("management")
     public String managementView(Model model) {
         return "management";
     }
 
     @RequestMapping("substanceView")
     public String substancesUpdate(Model model) {
         model.addAttribute("substances", substanceService.list());
         return "substanceView";
     }
 
     @RequestMapping(value = "addStorage", method = RequestMethod.GET)
     public String addStorageView(Model model) {
         model.addAttribute("storage", new StorageFormObject());
         return "addStorageView";
     }
 
     @RequestMapping(value = "addStorage", method = RequestMethod.POST)
     public String addStorage(@RequestParam String name) {
         List<Storage> storageList = storageService.list();
         for(Storage storage : storageList)  {
             if(storage.isHidden() == true)  {
                 storage.setHidden(false);
                 storage.setName(name);
                 storage.setInUse(false);
                 storageService.createOrUpdate(storage);
                 return "redirect:/storagesView";
             }
         }
         storageService.createOrUpdate(createStorage(name));
         return "redirect:/storagesView";
     }
 
     @RequestMapping("storagesView")
     public String storageView(Model model) {
        setStoragesInUse();
         model.addAttribute("storages", storageService.list());
         return "storagesView";
     }
 
     private Storage createStorage(String name) {
         Storage storage = new Storage();
         storage.setName(name);
         storage.setHidden(false);
         storage.setInUse(false);
 
         return storage;
     }
     
     @RequestMapping(value = "updateStorageName/{id}", method = RequestMethod.POST)
     public String updateStorageName(@RequestParam String name, @PathVariable Long id) {
         Storage temp = storageService.read(id);
         temp.setName(name);
         storageService.createOrUpdate(temp);
         System.out.println("uusi nimi on: " + storageService.read(id).getName());
         return "redirect:/storagesView";
     }
     
     @RequestMapping(value = "removeStorageName/{id}", method = RequestMethod.POST)
     public String removeStorageName(@PathVariable Long id, Model model) {
         List<Batch> batchList = batchService.list();
         List<Eluate> eluateList = eluateService.list();
         List<RadioMedicine> radioMedicineList = radioMedService.list();
         Storage temp = storageService.read(id);
         
         //removes the storage only if it is not used in any batches, eluates or radiomedicines
         for(Batch batch : batchList)    {
             Long[][] locations = batch.getStorageLocations();
             for(int i=0; i < locations.length; i++) {
                 if(locations[i][0] == id)   {
                     temp.setInUse(true);
                     storageService.createOrUpdate(temp);
                     return "redirect:/storagesView";
                 }
             }
         }
         for(Eluate eluate : eluateList)    {
             if(eluate.getStorageLocation() == id)   {
                 temp.setInUse(true);
                 storageService.createOrUpdate(temp);
                 return "redirect:/storagesView";
             }
         }
         for(RadioMedicine radioMedicine : radioMedicineList)    {
             if(radioMedicine.getStorageLocation() == id)    {
                 temp.setInUse(true);
                 storageService.createOrUpdate(temp);
                 return "redirect:/storagesView";
             }
         }
         temp.setName("^hidden^");
         temp.setInUse(false);
         temp.setHidden(true);
         storageService.createOrUpdate(temp);
         
         return "redirect:/storagesView";
     }
 
     @RequestMapping(value = "addStatusComment/{sid}+{cid}")
     public String addStatusComment(@RequestParam String comment,
             @PathVariable Long sid,
             @PathVariable Integer cid) {
         Substance temp = (Substance) substanceService.read(sid);
         String[] comments = temp.getStatusMessages();
         comments[cid] = comment;
         temp.setStatusMessages(comments);
         substanceService.createOrUpdate(temp);
         return "redirect:/admin";
     }
     
     public void setStoragesInUse() {
         List<Batch> batchList = batchService.list();
         List<Eluate> eluateList = eluateService.list();
         List<RadioMedicine> radioMedicineList = radioMedService.list();
         List<Storage> storageList = storageService.list();
         
         for(Storage storage : storageList)  {
             storage.setInUse(false);
             storageService.createOrUpdate(storage);
         }
         
         for(Batch batch : batchList)    {
             Long[][] locations = batch.getStorageLocations();
             for(int i=0; i < locations.length; i++) {
                 if(locations[i][0] > 0 && !storageService.read(locations[i][0]).isInUse()) {
                     Storage temp = storageService.read(locations[i][0]);
                     temp.setInUse(true);
                     storageService.createOrUpdate(temp);
                 }
             }
         }
         for(Eluate eluate : eluateList)    {
             if(eluate.getStorageLocation() >= 0)   {
                 if(!storageService.read(eluate.getStorageLocation()).isInUse())  {
                     Storage temp = storageService.read(eluate.getStorageLocation());
                     temp.setInUse(true);
                     storageService.createOrUpdate(temp);
                 }
             }
         }
         for(RadioMedicine radioMedicine : radioMedicineList)    {
             if(radioMedicine.getStorageLocation() >= 0)   {
                 if(!storageService.read(radioMedicine.getStorageLocation()).isInUse())  {
                     Storage temp = storageService.read(radioMedicine.getStorageLocation());
                     temp.setInUse(true);
                     storageService.createOrUpdate(temp);
                 }
             }
         }
     }
 }
