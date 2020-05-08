 package net.cyklotron.cms.poll;
 
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
 
 /**
  * Poll Listener implementation
  *
  * @author <a href="mailto:pablo@ngo.pl">Pawel Potempski</a>
  * @version $Id: PollListener.java,v 1.5 2005-05-31 17:10:28 pablo Exp $
  */
 public class PollListener
 extends BaseSiteListener
 implements SiteCreationListener, SiteDestructionValve, Startable
 {
     /** site service */
     private PollService pollService;
 
     public PollListener(Logger logger, CoralSessionFactory sessionFactory,
         SecurityService cmsSecurityService, EventWhiteboard eventWhiteboard,
         PollService pollService)
     {
         super(logger, sessionFactory, cmsSecurityService, eventWhiteboard);
         this.pollService = pollService;
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
             PollsResource pollsRoot = pollService.getPollsRoot(coralSession, site);
             Resource[] nodes = coralSession.getStore().getResource(site, "security");
             if(nodes.length != 1)
             {
                 log.error("Security node for site couldn't be found");
             }
             cmsSecurityService.createRole(coralSession, site.getAdministrator(),
                 "cms.poll.polls.administrator", pollsRoot);
 
             PollsResource pools = PollsResourceImpl.createPollsResource(coralSession, "pools",
                 pollsRoot);
             PollsResource polls = PollsResourceImpl.createPollsResource(coralSession, "polls",
                 pollsRoot);
             PollsResource votes = PollsResourceImpl.createPollsResource(coralSession, "votes",
                 pollsRoot);
             
             cmsSecurityService.createRole(coralSession, site.getAdministrator(),
                 "cms.poll.polls.administrator", polls);
             cmsSecurityService.createRole(coralSession, site.getAdministrator(),
                 "cms.poll.polls.administrator", pools);
             cmsSecurityService.createRole(coralSession, site.getAdministrator(),
                 "cms.poll.polls.administrator", votes);
         }
         catch(Exception e)
         {
             log.error("PollListenerException: ",e);
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
             res = coralSession.getStore().getResource(res[0], "polls");
             if(res.length > 0)
             {
                 deleteSiteNode(coralSession, res[0]);
             }
         }    
     }
     
 
     public void clearSecurity(CoralSession coralSession, SiteService siteService, SiteResource site) throws Exception
     {
         Resource[] res = coralSession.getStore().getResource(site, "applications");
         if(res.length > 0)
         {
             res = coralSession.getStore().getResource(res[0], "polls");
             if(res.length > 0)
             {
                 PollsResource polls = (PollsResource)res[0];
                 polls.setAdministrator(null);
                 polls.update();
                res = polls.getChildren();
                for(Resource r : res)
                {
                    if(r instanceof PollsResource)
                    {
                        ((PollsResource)r).setAdministrator(null);
                        r.update();
                    }
                }
             }
        }
     }
 }
