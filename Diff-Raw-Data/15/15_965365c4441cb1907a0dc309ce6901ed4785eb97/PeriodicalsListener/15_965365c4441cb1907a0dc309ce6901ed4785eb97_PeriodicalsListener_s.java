 package net.cyklotron.cms.periodicals;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.event.EventWhiteboard;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.BaseSiteListener;
 import net.cyklotron.cms.site.SiteCreationListener;
 import net.cyklotron.cms.site.SiteDestructionValve;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * Periodicals Listener implementation
  *
  * @author <a href="mailto:pablo@ngo.pl">Pawel Potempski</a>
 * @version $Id: PeriodicalsListener.java,v 1.1 2005-06-02 11:14:57 pablo Exp $
  */
 public class PeriodicalsListener
 extends BaseSiteListener
 implements SiteCreationListener, SiteDestructionValve, Startable
 {
     /** workflow service */
     private WorkflowService workflowService;
 
     /** workflow service */
     private PeriodicalsService periodicalsService;
 
     public PeriodicalsListener(Logger logger, CoralSessionFactory sessionFactory,
         SecurityService cmsSecurityService, EventWhiteboard eventWhiteboard,
         PeriodicalsService periodicalsService, WorkflowService workflowService)
     {
         super(logger, sessionFactory, cmsSecurityService, eventWhiteboard);
         this.periodicalsService = periodicalsService;
         this.workflowService = workflowService;
         eventWhiteboard.addListener(SiteCreationListener.class,this,null);
     }
     
     /**
      * {@inheritDoc}
      */
     public void start()
     {
     }
     
     /**
      * {@inheritDoc}
      */
     public void stop()
     {
     }
 
     //  --------------------       listeners implementation  ----------------------
     /**
      * Called when a new site is created.
      *
      * <p>The method will be called after the site Resources are successfully
      * copied from the template.</p>
      *
      * @param template the site template name.
      * @param name the site name.
      */
     public void createSite(SiteService siteService, String template, String name)
     {
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             SiteResource site = siteService.getSite(coralSession, name);
             Resource root = periodicalsService.getApplicationRoot(coralSession, site);
             cmsSecurityService.createRole(coralSession, site.getAdministrator(),
                                           "cms.periodicals.administrator", root);
         }
         catch(Exception e)
         {
             log.error("PeriodicalsListenerException: ", e);
         }
         finally
         {
             coralSession.close();
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void clearApplication(CoralSession coralSession, SiteService siteService, SiteResource site) throws Exception
     {
         Resource[] res = coralSession.getStore().getResource(site, "applications");
         if(res.length > 0)
         {
             res = coralSession.getStore().getResource(res[0], "periodicals");
             if(res.length > 0)
             {
                 deleteSiteNode(coralSession, res[0]);
             }
         }    
     }
     
 
     public void clearSecurity(CoralSession coralSession, SiteService siteService, SiteResource site) throws Exception
     {
     }
 }
