 package org.ict4h.atomfeed.spring.resource;
 
 import org.ict4h.atomfeed.client.service.EventWorker;
 import org.ict4h.atomfeed.client.service.FeedClient;
 import org.ict4h.atomfeed.server.service.Event;
 import org.ict4h.atomfeed.server.service.EventService;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.UUID;
 
 @Controller
 public class NotificationController {
 
     private static Logger logger = LoggerFactory.getLogger(NotificationController.class);
 
     private EventService eventService;
     private FeedClient feedClient;
 
     public NotificationController() {}
 
     @Autowired
     public NotificationController(EventService eventService, FeedClient feedClient) {
         this.eventService = eventService;
         this.feedClient = feedClient;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/entry/new")
     @Transactional
     public String showForm(ModelMap model) {
         model.addAttribute("entry", new Entry());
         return "form";
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "/entry/new")
     @Transactional
     public String addEntry(Entry entry, BindingResult result) throws URISyntaxException {
         Event event = new Event(
                 UUID.randomUUID().toString(),
                 entry.getTitle(),
                 DateTime.now(),
                 entry.getUrl(),
                 entry.getContent(),
                "standalone");
         eventService.notify(event);
         return showForm(new ModelMap());
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "/processevents")
     @ResponseBody
     @Transactional
     public String processEvents(String feedUri, String processType) throws URISyntaxException {
         EventWorker eventWorker = (processType.equalsIgnoreCase("fail"))
                 ? new FailEventWorker() : new SuccessEventWorker();
 
         feedClient.processEvents(new URI(feedUri), eventWorker);
 
         return "Finished processing events.\n";
     }
 
     @RequestMapping(method = RequestMethod.POST, value = "/processfailedevents")
     @ResponseBody
     @Transactional
     public String processFailedEvents(String feedUri, String processType) throws URISyntaxException {
         EventWorker eventWorker = (processType.equalsIgnoreCase("fail"))
                 ? new FailEventWorker() : new SuccessEventWorker();
 
         feedClient.processFailedEvents(new URI(feedUri), eventWorker);
 
         return "Finished processing failed events.\n";
     }
 
     class FailEventWorker implements EventWorker {
         @Override
         public void process(org.ict4h.atomfeed.client.domain.Event event) {
             logger.info("Failing Event" + event);
             throw new RuntimeException();
         }
     }
 
     class SuccessEventWorker implements EventWorker {
         @Override
         public void process(org.ict4h.atomfeed.client.domain.Event event) {
             logger.info("Successfully processing " + event);
         }
     }
 }
