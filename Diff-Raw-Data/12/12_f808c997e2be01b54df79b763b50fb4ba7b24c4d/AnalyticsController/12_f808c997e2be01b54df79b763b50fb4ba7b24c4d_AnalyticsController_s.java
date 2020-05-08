 package edu.rit.asksg.web;
 
 import com.google.common.base.Optional;
 import edu.rit.asksg.analytics.WordCounter;
 import edu.rit.asksg.analytics.domain.GraphData;
 import edu.rit.asksg.analytics.domain.Topic;
 import edu.rit.asksg.common.Log;
 import edu.rit.asksg.domain.Service;
 import edu.rit.asksg.repository.TopicRepository;
 import edu.rit.asksg.repository.WordCountRepository;
 import edu.rit.asksg.service.AnalyticsService;
 import edu.rit.asksg.service.AnalyticsServiceImpl;
 import edu.rit.asksg.service.ProviderService;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.context.request.WebRequest;
 
 import javax.swing.text.html.Option;
 import java.util.ArrayList;
 import java.util.List;
 
 @Controller
 @RequestMapping("/analytics")
 public class AnalyticsController {
 
     @Log
     Logger logger;
 
     @Autowired
     ProviderService providerService;
 
     @Autowired
     WordCounter wordCounter;
 
     @Autowired
     WordCountRepository wordCountRepository;
 
     @Autowired
     TopicRepository topicRepository;
 
     @Autowired
     AnalyticsService analyticsService;
 
 
     @RequestMapping(value = "/csv")
     public ResponseEntity<String> buildCsv() {
 
         HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain; charset=utf-8");
 
        return new ResponseEntity<String>(analyticsService.buildCSV(LocalDateTime.now().minusWeeks(1),
                LocalDateTime.now()), headers, HttpStatus.OK);
 
     }
 
     @RequestMapping(value = "/wordcount")
     public ResponseEntity<String> getWordCountsForDates(WebRequest params) {
         String s = params.getParameter("since");
         String u = params.getParameter("until");
         LocalDateTime since = null;
         LocalDateTime until = null;
 
         //todo: is failed parse just null?
         if (s != null) since = LocalDateTime.parse(s);
         if (s != null) until = LocalDateTime.parse(u);
 
         if (since == null) since = LocalDateTime.now().minusWeeks(1);
         if (until == null) until = LocalDateTime.now();
 
         List<GraphData> data = analyticsService.getGraphDataInRange(since, until);
 
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/json; charset=utf-8");
 
         return new ResponseEntity<String>(GraphData.toJsonArray(data), headers, HttpStatus.OK);
     }
 
 
     @RequestMapping(value = "/count")
     public ResponseEntity<String> count() {
 
         if (topicRepository.count() < 1) {
             topicTest();
         }
 
         analyticsService.count();
 
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "text/plain; charset=utf-8");
 
         return new ResponseEntity<String>("Counting...", headers, HttpStatus.OK);
 
     }
 
     @RequestMapping(value = "/words")
     public ResponseEntity<String> wordCounts() {
         List<GraphData> data = analyticsService.getGraphDataInRange(LocalDateTime.now().minusDays(3), LocalDateTime.now());
 
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/json; charset=utf-8");
 
         return new ResponseEntity<String>(GraphData.toJsonArray(data), headers, HttpStatus.OK);
     }
 
 
     @RequestMapping(value = "/topics")
     public ResponseEntity<String> topicTest() {
 
         List<Topic> topics = new ArrayList<Topic>();
 
         Topic t = new Topic("rit", "#rit,#ritnews");
         topics.add(t);
 
         Topic t2 = new Topic("parking", "tickets, fees");
         topics.add(t2);
 
         Topic t3 = new Topic("debit", "dining");
         topics.add(t3);
 
         Topic t4 = new Topic("food", "healthy");
         topics.add(t4);
 
         Topic t5 = new Topic("sustainability", "#sustainability,@ritgreen");
         topics.add(t5);
 
         Topic t6 = new Topic("activities", "cab,@ritcab");
         topics.add(t6);
 
         Topic t7 = new Topic("events", "conference,parties");
         topics.add(t7);
 
         Topic t8 = new Topic("advisors", "advisor,department");
         topics.add(t8);
 
         Topic t9 = new Topic("academics");
         topics.add(t9);
 
         Topic t10 = new Topic("mycourses", "grades");
         topics.add(t10);
 
         Topic t11 = new Topic("registration", "iap,waitlist,wait-list");
         topics.add(t11);
 
         Topic t12 = new Topic("weather", "rochester,snow");
         topics.add(t12);
 
         Topic t13 = new Topic("sis", "#sis,oracle");
         topics.add(t13);
 
         Topic t14 = new Topic("classes", "withdraw");
         topics.add(t14);
 
         Topic t15 = new Topic("sg", "#sg,elections,#elections");
         topics.add(t15);
 
         Topic t16 = new Topic("housing", "dorms,laundry");
         topics.add(t16);
 
         Topic t17 = new Topic("admission", "admissions,accepted");
         topics.add(t17);
 
         Topic t18 = new Topic("executives", "administration");
         topics.add(t18);
 
         Topic t19 = new Topic("co-op", "coop,jobzone,#jobzone");
         topics.add(t19);
 
         Topic t20 = new Topic("grounds", "fountain,trees,flowers,architecture");
         topics.add(t20);
 
         Topic t21 = new Topic("greek", "mansions,#ritgreek");
         topics.add(t21);
 
         Topic t22 = new Topic("athletics", "hockey,tigers");
         topics.add(t22);
 
         topicRepository.save(topics);
 
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "text/plain; charset=utf-8");
         return new ResponseEntity<String>("#RIT", headers, HttpStatus.OK);
 
     }
 
 
 }
