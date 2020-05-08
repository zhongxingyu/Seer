 package ohtu.radioaine.controller;
 
 import java.sql.Timestamp;
 import java.util.List;
 import ohtu.radioaine.domain.Batch;
 import ohtu.radioaine.domain.Event;
 import ohtu.radioaine.domain.RadioMedicine;
 import ohtu.radioaine.domain.Substance;
 import ohtu.radioaine.service.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import ohtu.radioaine.tools.Time;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Controllers for historypage
  *
  * @author rmjheino
  */
 @Controller
 public class HistoryController {
 
     @Autowired
     private EventService eventService;
     @Autowired
     private BatchService batchService;
     @Autowired
     private SubstanceService substanceService;
     @Autowired
     private RadioMedService radioMedService;
     @Autowired
     private EluateService eluateService;
 
     @RequestMapping("historyView")
     public String historyCTRL(Model model) {
         model.addAttribute("events", eventService.list());
         return "historyView";
     }
 
     @RequestMapping("seekModify")
     public String modifiedCTRL(Model model) {
         model.addAttribute("modified", eventService.list("type=modify"));
         return "historyView";
     }
     
     @RequestMapping("getAllEvents")
     public String allEventsCTRL(Model model, 
             @RequestParam String start,
             @RequestParam String end) {
         Timestamp startDate = Time.parseTimeStamp(start + " 00:00");
         Timestamp endDate = Time.parseTimeStamp(end + " 23:59");
         model.addAttribute("events", eventService.list(startDate, endDate));
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        
         return "allEventsView";
     }
 
     @RequestMapping("seek")
     public String seekByNameCTRL(@RequestParam String[] reports,
             Model model,
             @RequestParam String start,
             @RequestParam String end) {
 
         Timestamp startDate = Time.parseTimeStamp(start + " 00:00");
         Timestamp endDate = Time.parseTimeStamp(end + " 23:59");
         List<Substance> substances = substanceService.list();
         for (String str : reports) {
             if (str.equals("arrived")) {
                 model.addAttribute("wantArrived", 1);
                 List<Event> events = eventService.listArrived(startDate, endDate);
                 List<Event> arrived = events;
                 model.addAttribute("arrived", arrived);
             }
             if (str.equals("removed")) {
                 model.addAttribute("wantRemoved", 1);
                 model.addAttribute("removed", eventService.listRemoved(startDate, endDate));
             }
             int radioMedDetails = 0;
             if (str.equals("RadioMedQuantity")) {
                 model.addAttribute("wantRadioMedQuantity", 1);
                 radioMedDetails = 1;
             }
             if (str.equals("RadioMedDetails")) {
                 model.addAttribute("wantRadioMedDetails", 1);
                 radioMedDetails = 1;
             }
             if (radioMedDetails == 1) {
                 List<RadioMedicine> radiomeds = radioMedService.list(startDate, endDate);
                 for (Substance substance : substances) {
                     substance.resetCountForReport();
                     for (RadioMedicine radiomed : radiomeds) {
                         for (Batch batch : radiomed.getKits()) {
                             if (batch.getSubstance().getName().equals(substance.getName())) {
                                 substance.incCountForReport();
                             }
                         }
 
                     }
                 }
                 model.addAttribute("radioMeds", radiomeds);
             }
         }
 
         model.addAttribute(
                 "substances", substances);
         model.addAttribute(
                 "startDate", startDate);
         model.addAttribute(
                 "endDate", endDate);
         model.addAttribute("eluates", eluateService.list());
 
 
 
         return "raportView";
     }
 }
