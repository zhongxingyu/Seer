 package net.cyklotron.cms.modules.components.documents;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.modules.components.SkinableCMSComponent;
 import net.cyklotron.cms.poll.PollService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 
public class CommunityVotes
    extends SkinableCMSComponent
 {
     private final PollService pollService;
 
     public CommunityVotes(Context context, Logger logger, Templating templating,
        CmsDataFactory cmsDataFactory, SkinService skinService, MVCFinder mvcFinder,
        PollService pollService)
     {
         super(context, logger, templating, cmsDataFactory, skinService, mvcFinder);
         this.pollService = pollService;
     }
 
     @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
        NavigationNodeResource node = cmsDataFactory.getCmsData(context).getNode();
         templatingContext.put("voteBaseUrl", pollService.getVoteBaseUrl(httpContext));
        templatingContext.put("positive", (node != null) ? node.getVotesPositive(0) : 0);
        templatingContext.put("negative", (node != null) ? node.getVotesNegative(0) : 0);
     }
 }
