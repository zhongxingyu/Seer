 package org.atlasapi.remotesite.bbc;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.topic.TopicStore;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 
 @Controller
 public class BbcSlashProgrammesController {
 
     private BbcProgrammeAdapter fetcher;
     private ExecutorService executor;
 
     public BbcSlashProgrammesController(ContentWriter writer, TopicStore topicStore, AdapterLog log) {
         this.fetcher = new BbcProgrammeAdapter(writer, topicStore, log);
         this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("BBC /programmes %s").build());
     }
 
     @RequestMapping(value = "/system/update/bbc/programmes/{pid}", method = RequestMethod.POST)
    public void updatePid(HttpServletResponse response, HttpServletRequest request, @PathVariable("pid") final String pid) {
         
        executor.submit(new Runnable() {
             @Override
            public void run() {
                fetcher.createOrUpdate(BbcFeeds.slashProgrammesUriForPid(pid));
             }
         });
         
     }
 
 }
