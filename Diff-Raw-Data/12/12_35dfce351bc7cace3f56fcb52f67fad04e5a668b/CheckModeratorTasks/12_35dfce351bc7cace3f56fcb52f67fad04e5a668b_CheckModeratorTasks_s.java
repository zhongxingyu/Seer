 package net.cyklotron.cms.modules.jobs.forum;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18n;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 import org.objectledge.scheduler.Job;
 
 import net.cyklotron.cms.forum.ForumResource;
 import net.cyklotron.cms.forum.MessageResource;
 import net.cyklotron.cms.workflow.AutomatonResource;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * A job that checks whether any tasks appear in task list.
  *
  */
 public class CheckModeratorTasks extends Job
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** logging facility */
     private Logger log;
 
     /** workflow service */
     private WorkflowService workflowService;
 
     /** mail service */
     private MailSystem mailSystem;
 
 	/** i18 service */
 	private I18n i18n;
 
     private CoralSessionFactory sessionFactory;
     
     // initialization ///////////////////////////////////////////////////////
 
     /**
      *
      */
     public CheckModeratorTasks(Logger logger, WorkflowService workflowService, MailSystem mailSystem,
         I18n i18n, CoralSessionFactory sessionFactory)
     {            
         this.log = logger;
         this.workflowService = workflowService;
         this.mailSystem = mailSystem;
         this.i18n = i18n;
         this.sessionFactory = sessionFactory;
     }
 
     // Job interface ////////////////////////////////////////////////////////
 
     /**
      * Performs the mainteance.
      */
     public void run(String[] args)
     {
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             checkMessages(coralSession);
         }
         finally
         {
             coralSession.close();
         }
     }
 
     private void checkMessages(CoralSession coralSession)
     {
         try
         {
 			Locale locale = new Locale("pl","PL");
             Map receipients = new HashMap();
             ResourceClass messageClass = coralSession.getSchema().getResourceClass("cms.forum.message");
             Resource cmsRoot = coralSession.getStore().getUniqueResourceByPath("/cms");
             AutomatonResource automaton = workflowService.getPrimaryAutomaton(coralSession, cmsRoot, messageClass);
             StateResource state = workflowService.getState(coralSession, automaton, "new");
             QueryResults results = coralSession.getQuery().executeQuery("FIND RESOURCE FROM cms.forum.message WHERE state = " + state.getIdString());
             Resource[] nodes = results.getArray(1);
             for (int i = 0; i < nodes.length; i++)
             {
                 MessageResource message = (MessageResource)nodes[i];
                 String replyTo = message.getDiscussion().getReplyTo();
                 if (replyTo == null || replyTo.length() == 0)
                 {
                     replyTo = message.getDiscussion().getForum().getReplyTo();
                 }
                 if (replyTo != null && replyTo.length() > 0)
                 {
                     StringTokenizer st = new StringTokenizer(replyTo, "\n", false);
                     while (st.hasMoreTokens())
                     {
                         String email = st.nextToken();
                         Map sites = (Map)receipients.get(email);
                         if (sites == null)
                         {
                             sites = new HashMap();
                             receipients.put(email, sites);
                         }
                         Set messages = (Set)sites.get(message.getDiscussion().getForum());
                         if (messages == null)
                         {
                             messages = new HashSet();
                             sites.put(message.getDiscussion().getForum(), messages);
                         }
                         messages.add(message);
                     }
                 }
             }
             Iterator i = receipients.keySet().iterator();
             while (i.hasNext())
             {
                 String email = (String)i.next();
                 Map sites = (Map)receipients.get(email);
                 Iterator j = sites.keySet().iterator();
                 while (j.hasNext())
                 {
                     Object ob = j.next();
                     ForumResource forum = (ForumResource)ob;
                     Set messages = (Set)sites.get(forum);
                     try
                     {
                         LedgeMessage msg = mailSystem.newMessage();
                        msg.setTemplate(locale, "PLAIN", "forum/moderator_reminder");
                         msg.getContext().put("forum",forum);
                         msg.getContext().put("messages", messages);
                         msg.getMessage().setSubject(i18n.get(locale, "messages.forum.moderator_tasks_subject"));                     
                         msg.getMessage().setFrom(new InternetAddress(mailSystem.getSystemAddress()));
                         msg.getMessage().setRecipient(Message.RecipientType.TO, new InternetAddress(email));
                         msg.send(false);
                     }
                     catch (Exception e)
                     {
 						log.error("Filed to send message to: "+email);
                         log.error("Exception occured", e);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             log.error("Exception occured", e);
         }
     }
 
 }
