 package com.abudko.scheduled.jobs.publish;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 import com.abudko.reseller.huuto.query.service.list.QueryListService;
 import com.abudko.scheduled.jobs.Scheduler;
 import com.abudko.scheduled.service.huuto.PublishManager;
 
 public class PublishLekmerScheduler implements Scheduler {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Autowired
     @Qualifier("lekmerHtmlQueryListServiceImpl")
     private QueryListService lekmerQueryListService;
 
     @Autowired
     @Qualifier("lekmerPublishManager")
     private PublishManager lekmerPublishManager;
 
     public void schedule() {
         log.info("********* Start Publish Lekmer Scheduler *******");
         try {
             publishLekmer();
 
             log.info("********* End Publish Lekmer Scheduler *******");
 
         } catch (Exception e) {
             log.error("Exception happened during Publish Lekmer Scheduler: ", e);
             throw new RuntimeException(e);
         }
     }
 
     private void publishLekmer() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
             UnsupportedEncodingException, URISyntaxException, InterruptedException {
         publishLekmerInternal("talvihaalari", "TALVIHAALARI", 0);
         publishLekmerInternal("välikausihaalari", "VALIKAUSIHAALARI", 0);
         publishLekmerInternal("toppahaalari", "TALVIHAALARI", 0);
         publishLekmerInternal("toppapuku", "TALVIHAALARI", 0);
         publishLekmerInternal("kaksiosainen+setti+talvi", "TALVIHAALARI", 0);
         publishLekmerInternal("wind+haalari", "VALIKAUSIHAALARI", 0);
         publishLekmerInternal("sadeasut", "SADEHAALARI", 50);
        publishLekmerInternal("kevät+takki", "VALIKAUSITAKKI", 25);
        publishLekmerInternal("talvitakkeja", "TALVITAKKI", 5);
 //        publishLekmerInternal("toppahousut", "VALIKAUSIHAALARI", 5);
     }
 
     private void publishLekmerInternal(String query, String categoryenum, int limit)
             throws UnsupportedEncodingException, IllegalAccessException, InvocationTargetException,
             NoSuchMethodException, URISyntaxException, InterruptedException {
         List<ListResponse> list = new ArrayList<ListResponse>();
         SearchParams searchParams = new SearchParams();
         searchParams.setCategoryenum(categoryenum);
 
         log.info(String.format("Quering search: %s", query));
 
         Collection<ListResponse> queryListResponses = lekmerQueryListService.search(query, searchParams);
 
         if (limit == 0) {
             list.addAll(queryListResponses);
         }
         else {
             int i = 0;
             for (Iterator<ListResponse> iterator = queryListResponses.iterator(); iterator.hasNext();) {
                 ListResponse response = iterator.next();
                 if (i++ < limit) {
                     list.add(response);
                 }
             }
         }
 
         Category category = Category.valueOf(categoryenum);
         if (category != null) {
             lekmerPublishManager.publishResults(category, list);
         } else {
             log.warn(String.format("Can't find category for '%s'", categoryenum));
         }
     }
 }
