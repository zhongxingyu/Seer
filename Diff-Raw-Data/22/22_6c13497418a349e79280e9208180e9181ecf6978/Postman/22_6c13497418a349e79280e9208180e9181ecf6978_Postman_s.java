 package com.my.suicidenote.mail;
 
 import com.my.suicidenote.dto.Note;
 import com.my.suicidenote.repo.NoteRepository;
 
 import it.sauronsoftware.cron4j.Scheduler;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author Andrii_Manuiev
  */
 @Component
 public class Postman {
 
 	@Autowired
 	NoteRepository repository;
 	
 	@Autowired
 	Mailer mailer;
 	
     private Scheduler s = new Scheduler();
     private static final String CRON_EXPRESSION = "*/5 * * * *";
             
     private List<Note> prepareData() {
         Calendar currentDate = Calendar.getInstance();              
         return repository.findByWhenLessThan(currentDate.getTimeInMillis());
     }
             
    public void Postman() {
         s.schedule(CRON_EXPRESSION, new Runnable() {
             @Override
             public void run() {
                 List<Note> notes = prepareData();
                 for (Note note : notes) {
                     mailer.send(note);
                 }
             }
         });
     }
     
     @PostConstruct
     public void start() {
         s.start();
     }
     
     @PreDestroy
     public void stop() {
         s.stop();
     }
 }
