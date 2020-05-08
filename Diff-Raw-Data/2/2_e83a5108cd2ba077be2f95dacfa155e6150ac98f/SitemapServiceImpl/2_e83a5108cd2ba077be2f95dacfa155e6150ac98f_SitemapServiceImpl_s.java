 package net.cyklotron.cms.sitemap.internal;
 
 import static java.lang.String.format;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jcontainer.dna.ConfigurationException;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.parameters.DefaultParameters;
 import org.objectledge.parameters.Parameters;
 
 import net.cyklotron.cms.integration.ApplicationResource;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.sitemap.SitemapGenerationParticipant;
 import net.cyklotron.cms.sitemap.SitemapItem;
 import net.cyklotron.cms.sitemap.SitemapService;
 
 public class SitemapServiceImpl
     implements SitemapService
 {
     private static final String CONFIGURATION_PATH = "/cms/sitemaps";
 
     private static final String APPLICATION_NAME = "sitemap";
 
     private final List<SitemapGenerationParticipant> participants;
 
     private final CoralSessionFactory coralSessionFactory;
 
     private final IntegrationService integrationService;
 
     private final SiteService siteService;
 
     private final Logger logger;
 
     private final FileSystem fileSystem;
 
     public SitemapServiceImpl(SitemapGenerationParticipant[] participants, FileSystem fileSystem,
         SiteService siteService, IntegrationService integrationService,
         CoralSessionFactory coralSessionFactory, Logger logger)
         throws ConfigurationException
     {
         this.participants = Arrays.asList(participants);
         this.fileSystem = fileSystem;
         this.siteService = siteService;
         this.integrationService = integrationService;
         this.coralSessionFactory = coralSessionFactory;
         this.logger = logger;
     }
 
     @Override
     public SitemapConfiguration configuration()
     {
         try(CoralSession coralSession = coralSessionFactory.getRootSession())
         {
             return (SitemapConfiguration)coralSession.getStore().getUniqueResourceByPath(
                 CONFIGURATION_PATH);
         }
         catch(EntityDoesNotExistException | AmbigousEntityNameException e)
         {
             throw new IllegalStateException("unable to access sitemap application configuration", e);
         }
     }
 
     @Override
     public List<SitemapGenerationParticipant> participants()
     {
         return Collections.unmodifiableList(participants);
     }
 
     public void generateSitemaps()
     {
         try(CoralSession coralSession = coralSessionFactory.getRootSession())
         {
             SitemapConfiguration config = (SitemapConfiguration)coralSession.getStore()
                 .getUniqueResourceByPath(CONFIGURATION_PATH);
             final String basePath = config.getBasePath();
             if(basePath != null)
             {
                 ApplicationResource app = integrationService.getApplication(coralSession,
                     APPLICATION_NAME);
                 for(SiteResource site : siteService.getSites(coralSession))
                 {
                     if(integrationService.isApplicationEnabled(coralSession, site, app))
                     {
                         try
                         {
                             String domain = siteService.getPrimaryMapping(coralSession, site);
                             if(domain == null)
                             {
                                 logger
                                 .error(format(
                                     "unable to generate site map for site %s because primary virtual host is not set",
                                     site.getName()));
                             }
                             else
                             {
                                 SitemapWriter sw = new SitemapWriter(fileSystem, basePath, domain,
                                     config.getCompress());
                                 sw.write(itemIterator(site, domain, config.getParticipantsConfig(),
                                     coralSession));
                             }
                         }
                         catch(Exception e)
                         {
                             logger.error(
                                 format("unable to generate site map for site %s", site.getName()),
                                 e);
                         }
                     }
                 }
             }
             else
             {
                 logger.error("unable to generate site maps, base path is not set in configuration");
             }
         }
         catch(EntityDoesNotExistException | AmbigousEntityNameException e)
         {
             logger.error("unable to access sitemap application configuration");
         }
     }
 
     private Iterator<SitemapItem> itemIterator(final SiteResource site, final String domain,
         final Parameters participantsConfig, final CoralSession coralSession)
     {
         return new Iterator<SitemapItem>()
             {
                 private final Iterator<SitemapGenerationParticipant> outer = participants
                     .iterator();
 
                 private Iterator<SitemapItem> inner = null;
 
                 private Iterator<SitemapItem> nextInner()
                 {
                     final SitemapGenerationParticipant participant = outer.next();
                     if(participant.supportsConfiguration())
                     {
                         return participant.items(site, domain,
                             participantsConfig.getChild(participant.name() + "."), coralSession);
                     }
                     else
                     {
                         return participant.items(site, domain, new DefaultParameters(),
                             coralSession);
                     }
                 }
 
                 @Override
                 public boolean hasNext()
                 {
                    if(inner == null)
                     {
                         if(outer.hasNext())
                         {
                             inner = nextInner();
                         }
                         else
                         {
                             return false;
                         }
                     }
                     return inner.hasNext();
                 }
 
                 @Override
                 public SitemapItem next()
                 {
                     if(inner == null || !inner.hasNext())
                     {
                         inner = nextInner();
                     }
                     return inner.next();
                 }
 
                 @Override
                 public void remove()
                 {
                     throw new UnsupportedOperationException();
                 }
             };
     }
 }
