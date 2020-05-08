 package net.cyklotron.cms.modules.actions.poll;
 
 import java.util.Map;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.poll.util.Answer;
 import net.cyklotron.cms.poll.util.Question;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  *
  * @author <a href="mailo:pablo@ngo.pl">Pawel Potempski</a>
  * @version $Id: AddAnswer.java,v 1.3 2005-03-08 10:53:05 pablo Exp $
  */
 public class AddAnswer
     extends BasePollAction
 {
     
     public AddAnswer(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, PollService pollService, WorkflowService workflowService)
     {
         super(logger, structureService, cmsDataFactory, pollService, workflowService);
         
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         String type = parameters.get("type", "poll");
 
         if("poll".equals(type))
         {
            savePoll(httpContext, parameters);
            int qid = parameters.getInt("qid", -1);
             if(qid == -1)
             {
                 throw new ProcessingException("Question position not found");
             }
 
             Map questions = (Map)httpContext.getSessionAttribute(POLL_KEY);
             Question question = (Question)questions.get(new Integer(qid));
             question.addAnswer("", -1);
         }
         else if("vote".equals(type))
         {
            saveVote(httpContext, parameters);
             Map answers = (Map)httpContext.getSessionAttribute(VOTE_KEY);
             answers.put(answers.size(), new Answer("", -1));
         }
     }
 }
 
 
