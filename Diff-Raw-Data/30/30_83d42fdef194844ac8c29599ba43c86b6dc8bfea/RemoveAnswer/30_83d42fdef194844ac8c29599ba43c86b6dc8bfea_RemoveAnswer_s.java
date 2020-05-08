 package net.cyklotron.cms.modules.actions.poll;
 
 import java.util.HashMap;
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
 import net.cyklotron.cms.poll.util.Question;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * @author <a href="mailo:pablo@ngo.pl">Pawel Potempski</a>
  * @version $Id: RemoveAnswer.java,v 1.3 2005-03-08 10:53:05 pablo Exp $
  */
 public class RemoveAnswer
     extends BasePollAction
 {
 
     public RemoveAnswer(Logger logger, StructureService structureService,
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
        savePoll(httpContext, parameters);
         int qid = parameters.getInt("qid", -1);
         int aid = parameters.getInt("aid", -1);
         String type = parameters.get("type", "poll");
 
         if("poll".equals(type))
         {
             if(qid == -1 || aid == -1)
             {
                 throw new ProcessingException("Question id nor Answer id not found");
             }
 
             Map questions = (Map)httpContext.getSessionAttribute(POLL_KEY);
             if(questions == null || questions.size() <= qid)
             {
                 throw new ProcessingException("Question id exceed questions length");
             }
             Question question = (Question)questions.get(new Integer(qid));
             Map answers = question.getAnswers();
             if(answers == null || answers.size() <= aid)
             {
                 throw new ProcessingException("Answer id exceed answers length");
             }
             Map newAnswers = new HashMap();
             for(int i = 0; i < aid; i++)
             {
                 Integer key = new Integer(i);
                 newAnswers.put(key, answers.get(key));
             }
             for(int i = aid + 1; i < answers.size(); i++)
             {
                 newAnswers.put(new Integer(i - 1), answers.get(new Integer(i)));
             }
             question.setAnswers(newAnswers);
         }
         else if("vote".equals(type))
         {
             if(aid == -1)
             {
                 throw new ProcessingException("Question id nor Answer id not found");
             }
             Map answers = (Map)httpContext.getSessionAttribute(VOTE_KEY);
             if(answers == null || answers.size() <= aid)
             {
                 throw new ProcessingException("Answer id exceed answers length");
             }
            answers.remove(new Integer(aid));
         }
     }
 }
