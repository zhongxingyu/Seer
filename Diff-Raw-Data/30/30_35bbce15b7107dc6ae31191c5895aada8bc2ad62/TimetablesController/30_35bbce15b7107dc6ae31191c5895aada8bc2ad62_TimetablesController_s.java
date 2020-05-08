 package wad.timetables.controller;
 
 import java.io.IOException;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import wad.timetables.domain.Search;
 import wad.timetables.domain.SearchResult;
 import wad.timetables.service.TimetablesService;
 
 /* @author mhaanran */
 @Controller
 public class TimetablesController {
 
    @Autowired
     private TimetablesService timetablesService;
     
     @RequestMapping(value = "search", method = RequestMethod.GET)
     public String searchPage(@ModelAttribute("searchForm") Search searchForm) {
         return "search";
     }
 
     @RequestMapping(value = "search", method = RequestMethod.POST)
     public String search(Model model, @ModelAttribute("searchForm") Search searchForm) throws IOException {
         String error = "Stop could not be found by search parameter used";
         if (!searchForm.getStopNumber().isEmpty()) {
             SearchResult searchByStopNumber = timetablesService.searchByStopNumber(searchForm.getStopNumber());
             if(searchByStopNumber!=null) {
                 model.addAttribute("results",searchByStopNumber);
             }
             else {
                 model.addAttribute("error",error);
             }
         }
         else if (!searchForm.getStopName().isEmpty()) {
             List<SearchResult> searchByStopName = timetablesService.searchByStopName(searchForm.getStopName());
             if(searchByStopName!=null) {
                 model.addAttribute("stopName", searchByStopName);
             }
             else {
                 model.addAttribute("error",error);
             }
         }          
         return "search";
     }
 
     public void setTimetablesService(TimetablesService timetablesService) {
         this.timetablesService = timetablesService;
     }
     
     
     
     
 }
