 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.Message;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import com.thoughtworks.twu.service.ActivityService;
 import com.thoughtworks.twu.service.MessageService;
 import org.json.JSONException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Controller
 public class SearchActivityController {
 
     @RequestMapping(value = "/timesheet/search_activity", method = RequestMethod.GET)
     public ModelAndView loadSearchActivityView() {
 
         ModelAndView modelAndView = new ModelAndView("ui/timesheet/search_activity");
         MessageService service = new MessageService();
 
 
         List<Message> messages = new ArrayList<Message>();
         messages.add(service.getMessageMessageById("Alteast2CharsForSearch"));
         messages.add(service.getMessageMessageById("NoMatchingActivity"));
 
         modelAndView.addObject("messages", messages) ;
 
         HibernateConnection.getInstance().getSession().close();
         return modelAndView;
     }
 
     @RequestMapping(value = "/timesheet/search_activity", method = RequestMethod.POST)
     public @ResponseBody String searchActivities(@RequestParam("s") String query) throws JSONException {
         ActivityService service = new ActivityService();
         HibernateConnection.getInstance().getSession().close();
        return service.getActivities(query).toString();
     }
 }
