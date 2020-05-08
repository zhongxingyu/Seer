 package com.zibea.parser.core.workers;
 
 import com.zibea.parser.core.task.Task;
 import com.zibea.parser.core.utils.ZapOfferParser;
 import com.zibea.parser.model.domain.Offer;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * @author: Mikhail Bragin
  */
 public class OfferParseWorker implements Worker {
 
     private ExecutorService offerParsePool;
 
     private OfferArchiver offerArchiver;
 
     private AtomicLong tasksProduced = new AtomicLong();
 
     public OfferParseWorker(OfferArchiver offerArchiver) {
        this.offerParsePool = Executors.newFixedThreadPool(3, new CustomThreadFactory("offer-parse-worker"));
         this.offerArchiver = offerArchiver;
     }
 
     @Override
     public void addTask(Task task) {
 
         offerParsePool.submit(new ParseWorker(task) {
 
             @Override
             public void processTask() throws InterruptedException {
                 int attempt = 0;
 
                 boolean parsed = false;
 
                 while (!parsed) {
                     try {
                         attempt++;
 
                         Document doc = Jsoup.connect(task.getUrl()).timeout(20 * 1000).get();
                         ZapOfferParser parser = new ZapOfferParser(doc);
                         Offer offer = parser.parse();
 
                         offer.setState(task.getState());
                         offer.setCity(task.getCity());
                         offer.setDistrict(task.getDistrict());
                         offer.setApartment(task.getApartment());
                         offer.setTransaction(task.getTransaction());
 
                         offerArchiver.addToBatch(offer); //put task to archiving queue
                         parsed = true;
                         tasksProduced.incrementAndGet();
                     } catch (IOException e) {
 
                         if (attempt > 5) {
                             //todo save to file/or db
                             System.out.println("Giving up to get document. [url="
                                     + task.getUrl() + "]");
                             return;
                         }
 
                         Thread.sleep(2000);
                     }
                 }
             }
         });
     }
 
     @Override
     public long getTasksProduced() {
         return tasksProduced.get();
     }
 }
