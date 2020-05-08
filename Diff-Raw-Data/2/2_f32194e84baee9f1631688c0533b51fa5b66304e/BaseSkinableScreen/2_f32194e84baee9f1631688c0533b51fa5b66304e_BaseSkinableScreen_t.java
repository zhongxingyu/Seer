 package net.cyklotron.cms.modules.views;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.modules.components.EmbeddedScreen;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.builders.BuildException;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 /**
  * The main screen of the CMS application that displays page contents.
  */
 public class BaseSkinableScreen
     extends BaseCMSScreen
 {
     protected StructureService structureService;
 
     protected StyleService styleService;
 
     protected SkinService skinService;
 
     private Map methodMap = new HashMap();
 
     protected MVCFinder mvcFinder;
 
     public BaseSkinableScreen(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, 
         SkinService skinService, MVCFinder mvcFinder, TableStateManager tableStateManager)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager);
         this.structureService = structureService;
         this.styleService = styleService;
         this.skinService = skinService;
         this.mvcFinder = mvcFinder;
     }
 
     public void process(Parameters parameters, MVCContext mvcContext, 
         TemplatingContext templatingContext, HttpContext httpContext, 
         I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         if(templatingContext.containsKey("stackTrace"))
         {
             return;
         }
         prepareState();
     }
 
     /**
      * @{inheritDoc}
      */
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         if(templatingContext.containsKey("stackTrace"))
         {
             return false;
         }
         if(isNodeDefined())
         {
             return !getNode().canView(context, coralSession.getUserSubject(), new Date());
         }
         else
         {
             // not sure about this one...
             return true;
         }
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         if(templatingContext.containsKey("stackTrace"))
         {
             return true;
         }
         if(isNodeDefined())
         {
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             return getNode().canView(context, cmsData, cmsData.getUserData().getSubject());
         }
         else
         {
             return true;
         }
     }
 
     /**
      * Returns a components template.
      *
      * TODO: Rethink the way the errors are being handled, especially when a skinnable screen is
      *       executed outside of the site
      *
      * @param coralSession the coralSession.
      * @return a template to be used for rendering this block.
      */
     public Template getTemplate(CoralSession coralSession, Template defaultTemplate)
     {
         CmsData cmsData;
         try
         {
             cmsData = cmsDataFactory.getCmsData(context);
         }
         catch(ProcessingException e)
         {
             // outside of cms scope?
             logger.warn("faied to acquire CmsData",e );
             return defaultTemplate;
         }
 
         // get site
         SiteResource site = cmsData.getSite();
         if(site == null)
         {
             site = cmsData.getGlobalComponentsDataSite();
             if(site == null)
             {            
                 return defaultTemplate;
             }
         }
 
         // 1. get skin name
         String skin = cmsData.getSkinName();
 
         // 2. get screen info
         String app = null;
         String screen = null;
         String variant = null;
         String state = null;
         try
         {
             state = getState();
         }
         catch(Exception e)
         {
             logger.error("failed to determine screen state", e);
             return defaultTemplate;
         }
         Parameters config = cmsData.getPreferences();
         app = config.get("screen.app",null);
         screen = config.get("screen.class",null);
         if(app != null && screen != null)
         {
             variant = config.get("screen.variant."+app+"."+
                 screen.replace(",","."), "Default");
         }
         if(app == null || screen == null)
         {
             logger.warn(getClass().getName()+" is dervied from BaseSkinableScreen "+
                         "but was launched outside of EmbeddedScreen component");
             return defaultTemplate;
         }
 
         logger.debug("BaseSkinnableScreen "+app+":"+screen+":"+variant+":"+state);
 
         // 3. get template object
         Template templ = null;
 
         try
         {
             // if skin defines a template for the variant
             if(skinService.hasScreenVariant(coralSession, site, skin, app, screen, variant))
             {
                 templ = skinService.getScreenTemplate(coralSession, site, skin, app, screen, variant, state);
             }
             else
             {
                templ = getAppScreenTemplate(app, screen.replace(",","."), state);
             }
         }
         catch(Exception e)
         {
             logger.error("failed to lookup template for screen "+
                       app+":"+screen+" site "+site.getName()+" skin "+skin+
                       " variant "+variant+" state "+state, e);
             templ = null;
         }
 
         // this one throws an exception - we cannot generate component's UI without any templates.
         if(templ == null)
         {
             templ = defaultTemplate;
         }
 
         return templ;
     }
 
     protected Template getAppScreenTemplate(String app, String component, String state)
     {
         if(!state.equalsIgnoreCase("Default"))
         {
             component = component + state;
         }
         Template template = mvcFinder.findBuilderTemplate(component).getTemplate();
         return template;
     }
 
     /**
      * Returns the current state of the component.
      *
      * <p>The base implemenation always returns state "default" which is OK
      * for stateless components, and inintial state of stateful
      * components.</p>
      *
      * @return current state of the component.
      */
     public String getState()
         throws ProcessingException
     {
         return "Default";
     }
 
     /**
      * Runns the prepare&lt;state&gt;(RunData, Context) method of the child
      * class.
      */
     protected void prepareState()
         throws ProcessingException
     {
         String state = getState();
         if(state == null || state.length() == 0)
         {
             cmsDataFactory.getCmsData(context).getComponent().error("wrong state name", null);
             return;
         }
         Method method = (Method)methodMap.get(state);
         if(method == null)
         {
             Class[] args = new Class[] { Context.class };
             try
             {
                 method = getClass().getMethod("prepare"+state, args);
             }
             catch(NoSuchMethodException e)
             {
                 throw new ProcessingException("method prepare"+state+
                                               "(RunData, Context) not declared in class "+
                                               getClass().getName(), e);
             }
             methodMap.put(state, method);
         }
         try
         {
             method.invoke(this, new Object[] { context });
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to invoke prepare"+state+
                                               "(RunData, Context) not declared in class "+
                                               getClass().getName(), e);
         }
     }
 
     /**
      * Default blank implementaion of prepareDefault method()
      */
     public void prepareDefault(Context context)
         throws ProcessingException
     {
         // nothing to do
     }
 
     // configuration support methods ///////////////////////////////////////////////////////////////
 
     protected Parameters getConfiguration(CoralSession coralSession)
     {
         try
         {
             return prepareScreenConfig(coralSession);
         }
         catch(ProcessingException e)
         {
             return null;
         }
     }
 
     protected void screenError(NavigationNodeResource currentNode, Context context, String message)
     throws ProcessingException
     {
         // TODO: Think of a better way of keeping the screen error messages
         message = message + ", embedded screen: "+this.getClass().getName();
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         List messages = (List)(templatingContext.get(EmbeddedScreen.SCREEN_ERROR_MESSAGES_KEY));
         if(messages == null)
         {
             messages = new ArrayList();
         }
         messages.add(message);
 
         templatingContext.put(EmbeddedScreen.SCREEN_ERROR_MESSAGES_KEY, messages);
     }
 
     /**
      * {@inheritDoc}
      */
     public String build(Template template, String embeddedBuildResults)
         throws BuildException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         template = getTemplate(coralSession, template);
         return super.build(template, embeddedBuildResults);
     }
 }
