 package net.cyklotron.cms.modules.jobs.poll;
 
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.scheduler.Job;
 
 import net.cyklotron.cms.poll.PollService;
 
 /**
  * A job that checks the start and expire date of the polls
  *
  */
 public class CheckPollState
     extends Job
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** The poll service. */
     private PollService pollService;
 
     private CoralSessionFactory sessionFactory;
     
     /**
      *
      */
     public CheckPollState(CoralSessionFactory sessionFactory, PollService pollService)
     {
         this.pollService = pollService;
         this.sessionFactory = sessionFactory;
     }    
 
     /**
      * Performs the mainteance.
      */
     public void run(String[] args)
     {
        System.out.println("CheckPollState job started!!!");
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             pollService.checkPollState(coralSession);
         }
         finally
         {
             coralSession.close();
         }
     }
 }
