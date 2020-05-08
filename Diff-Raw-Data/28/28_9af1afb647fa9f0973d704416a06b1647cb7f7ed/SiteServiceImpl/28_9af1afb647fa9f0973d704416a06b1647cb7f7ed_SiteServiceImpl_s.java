 package net.cyklotron.cms.site.internal;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.schema.CircularDependencyException;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.event.EventWhiteboard;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.security.RoleResource;
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.SiteCopyingListener;
 import net.cyklotron.cms.site.SiteCreationListener;
 import net.cyklotron.cms.site.SiteDestructionListener;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.site.VirtualServerResource;
 import net.cyklotron.cms.site.VirtualServerResourceImpl;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureException;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Provides information about deployed sites.
  *
  * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
  * @version $Id: SiteServiceImpl.java,v 1.16 2006-01-02 10:23:04 rafal Exp $
  */
 public class SiteServiceImpl
     implements SiteService, Startable
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** The StructureService. */
     private StructureService structureService;
     
     /** Cms securty service */
     private SecurityService cmsSecurityService;
     
     /** Event whiteboard */
     private EventWhiteboard eventWhiteboard;
 
 
     /** The logger. */
     protected Logger log;
 
     /** The /cms/sites node. */
     private Resource sites;
 
     /** The /cms/aliases node. */
     private Resource aliases;
     
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Initializes the service.
      */
     public SiteServiceImpl(Logger logger, EventWhiteboard eventWhiteboard, 
         StructureService structureService, SecurityService cmsSecurityService,
         CoralSessionFactory sessionFactory)
     {
         this.log = logger;
         this.eventWhiteboard = eventWhiteboard;
         this.structureService = structureService;
         this.cmsSecurityService = cmsSecurityService;
     }
     
     public void start()
     {
         //ensure all listeners are ready 
     }
     
     public void stop()
     {
     }
     // SiteService interface /////////////////////////////////////////////////
 
     /**
      * Returns the deployed sites.
      *
      * @return the deployed sites.
      */
     public SiteResource[] getSites(CoralSession coralSession)
     {
         Resource[] res = coralSession.getStore().getResource(getSitesRoot(coralSession));
         ArrayList<SiteResource> temp = new ArrayList<SiteResource>();
         for(int i=0; i<res.length; i++)
         {
             if(!((SiteResource)res[i]).getTemplate())
             {
                 temp.add((SiteResource)res[i]);
             }
         }
         SiteResource[] result = new SiteResource[temp.size()];
         temp.toArray(result);
         return result;
     }
 
     /**
      * Returns the available site templates.
      *
      * @return the available site templates.
      */
     public SiteResource[] getTemplates(CoralSession coralSession)
     {
         Resource[] res = coralSession.getStore().getResource(getSitesRoot(coralSession));
         ArrayList<SiteResource> temp = new ArrayList<SiteResource>();
         for(int i=0; i<res.length; i++)
         {
             if(((SiteResource)res[i]).getTemplate())
             {
                 temp.add((SiteResource)res[i]);
             }
         }
         SiteResource[] result = new SiteResource[temp.size()];
         temp.toArray(result);
         return result;
     }
 
     /**
      * Returns the names of all defined virtual servers.
      *
      * @return the names of all defined virtual servers.
      */
     public String[] getVirtualServers(CoralSession coralSession)
     {
         Resource[] res = coralSession.getStore().getResource(getAliasesRoot(coralSession));
         ArrayList<String> temp = new ArrayList<String>();
         for(int i=0; i<res.length; i++)
         {
             temp.add(res[i].getName());
         }
         String[] result = new String[temp.size()];
         temp.toArray(result);
         return result;
     }
 
     /**
      * Checks if a host name is one of the virtual servers recongized 
      * by the system.
      *
      * @param host the host name.
      * @return <code>true</code> if the host is one of the virtual servers 
      *         recongized by the system.
      */
     public boolean isVirtualServer(CoralSession coralSession, String host)
         throws SiteException
     {
         Resource[] res = coralSession.getStore().getResource(getAliasesRoot(coralSession), host);
         return res.length > 0;
     }
 
     /**
      * Maps a virtual server name name name into a site.
      *
      * @param server the virtual server fully qualified domain name.
      * @return the site matching the alias name, or site "default" if not
      *         matched, or <code>null</code> if site default does not exit.
      */
     public SiteResource getSiteByAlias(CoralSession coralSession, String server)
         throws SiteException
     {
         Resource[] match = coralSession.getStore().getResource(getAliasesRoot(coralSession), server);
         if(match.length == 1)
         {
             SiteResource site = (SiteResource)((VirtualServerResource)match[0]).getSite();
             if(!site.getTemplate())
             {
                 return site;
             }
         }
         return getSite(coralSession, "default");
     }
 
     /**
      * Maps a virtual server name name name into a site.
      *
      * @param server the virtual server fully qualified domain name.
      * @return the default navigation node that should be displayed
      *         when this vritual server is requested.
      */
     public NavigationNodeResource getDefaultNode(CoralSession coralSession, String server)
         throws SiteException
     {
         Resource[] match = coralSession.getStore().getResource(getAliasesRoot(coralSession), server);
         if(match.length == 1)
         {
             VirtualServerResource virtual = (VirtualServerResource)match[0];
             if(!virtual.getSite().getTemplate())
             {
                 return virtual.getNode();
             }
         }
         SiteResource site = getSite(coralSession, "default");
         if(site == null)
         {
             return null;
         }
         try
         {
             return structureService.getRootNode(coralSession, site);
         }
         catch(StructureException e)
         {
             throw new SiteException("failed to locate the root navigation node", e);
         }
     }
 
     /**
      * Returns virtual server mappings for a site.
      *
      * @param site the site.
      * @return the virtual server mappings.
      */
     public String[] getMappings(CoralSession coralSession, SiteResource site)
         throws SiteException
     {
         Resource[] servers = coralSession.getStore().getResource(getAliasesRoot(coralSession));
         ArrayList<String> temp = new ArrayList<String>();
         for(int i=0; i<servers.length; i++)
         {
             if(((VirtualServerResource)servers[i]).getSite().equals(site))
             {
                 temp.add(servers[i].getName());
             }
         }
         String[] result = new String[temp.size()];
         temp.toArray(result);
         return result;
     }
 
     /**
      * Adds a virtual server mapping for a site.
      *
      * @param site the site.
      * @param server the virtual server name.
      * @param node the default navigation node that should be displayed
      *        when this vritual server is requested.
      * @throws SiteException if the server is already mapped to a site.
      * @throws InvalidResourceNameException if the server argument contains invalid characters.
      */
     public void addMapping(CoralSession coralSession, SiteResource site, String server,
                            NavigationNodeResource node)
         throws SiteException, InvalidResourceNameException
     {
         Resource[] res = coralSession.getStore().getResource(getAliasesRoot(coralSession), server);
         if(res.length > 1)
         {
             throw new SiteException("multiple mappings for "+server);
         }
         if(res.length != 0)
         {
             throw new SiteException(server+" is already mapped to "+
                                     ((VirtualServerResource)res[0]).getSite().getName());
         }
         try
         {
             VirtualServerResourceImpl.
                 createVirtualServerResource(coralSession, server, getAliasesRoot(coralSession), site, node, false);
         }
         catch(ValueRequiredException e)
         {
             throw new SiteException("ARL exception", e);
         }
     }
 
 
     /**
      * Changes the default navigation node in a mapping.
      *
      * @param site the site.
      * @param server the virtual server name.
      * @param node the default navigation node that should be displayed
      *        when this vritual server is requested.
      * @throws SiteException if operation fails.
      */
     public void updateMapping(CoralSession coralSession, SiteResource site, String server, NavigationNodeResource node)
         throws SiteException
     {
         Resource[] res = coralSession.getStore().getResource(getAliasesRoot(coralSession), server);
         if(res.length == 0)
         {
             throw new SiteException("no mapping exists for "+server);
         }
         if(res.length > 1)
         {
             throw new SiteException("multiple mappings for "+server);
         }
         try
         {
             ((VirtualServerResource)res[0]).setNode(node);
             res[0].update();
         }
         catch(Exception e)
         {
             throw new SiteException("ARL exception", e);
         }
     }
 
     /**
      * Removes a virtual server mapping.
      *
      * @param server the virtual server name.
      * @throws SiteException if the server is not mapped to any site.
      */
     public void removeMapping(CoralSession coralSession, String server)
         throws SiteException
     {
         Resource[] res = coralSession.getStore().getResource(getAliasesRoot(coralSession), server);
         if(res.length == 0)
         {
             throw new SiteException("no mapping exists for "+server);
         }
         if(res.length > 1)
         {
             throw new SiteException("multiple mappings for "+server);
         }
         try
         {
             coralSession.getStore().deleteResource(res[0]);
         }
         catch(EntityInUseException e)
         {
             throw new SiteException("ARL exception", e);
         }
     }
 
     /**
      * Checks if a virtual server is primary for a site.
      *
      * @param server the virtual server name.
      * @return <code>true</code> if the mapping is declared as primary.
      */
     public boolean isPrimaryMapping(CoralSession coralSession, String server)
         throws SiteException
     {
         Resource[] res = coralSession.getStore().getResource(getAliasesRoot(coralSession), server);
         if(res.length == 0)
         {
             throw new SiteException("no mapping exists for "+server);
         }
         if(res.length > 1)
         {
             throw new SiteException("multiple mappings for "+server);
         }
         return ((VirtualServerResource)res[0]).getPrimary();
     }
 
     /**
      * Sets the mapping to be primary for a site.
      *
      * @param site the site.
      * @param server the virtual server name.
      */
     public void setPrimaryMapping(CoralSession coralSession, SiteResource site, String server)
         throws SiteException
     {
         Resource[] mappings = coralSession.getStore().getResource(getAliasesRoot(coralSession));
         for(int i=0; i<mappings.length; i++)
         {
             VirtualServerResource alias = (VirtualServerResource)mappings[i];
             if(alias.getSite().equals(site))
             {
                 if(alias.getName().equals(server))
                 {
                     alias.setPrimary(true);
                 }
                 else
                 {
                     alias.setPrimary(false);
                 }
             }
             alias.update();
         }
     }
     
     /**
      * Returns the primary mapping for a site.
      *
      * @param site the site.
      * @return the name of the domain makred as primary, a name of one
      *        of the domains mapped to the site if none is marked, or
      *        <code>null</code> if no mappins for the site exist.
      */
     public String getPrimaryMapping(CoralSession coralSession, SiteResource site)
         throws SiteException
     {
         Resource[] mappings = coralSession.getStore().getResource(getAliasesRoot(coralSession));
         ArrayList<String> list = new ArrayList<String>();
         for(int i=0; i<mappings.length; i++)
         {
             VirtualServerResource alias = (VirtualServerResource)mappings[i];
             if(alias.getSite().equals(site))
             {
                 if(alias.getPrimary())
                 {
                     return alias.getName();
                 }
                 list.add(alias.getName());
             }
         }
         if(list.size() > 0)
         {
             Collections.sort(list);
             return list.get(0);
         }
         else
         {
             return null;
         }
     }    
     
     /**
      * Returns the site root node.
      *
      * @param name the site name.
      * @return site root resource, or <code>null</code> if no such site exists.
      */
     public SiteResource getSite(CoralSession coralSession, String name)
         throws SiteException
     {
         Resource[] res = coralSession.getStore().getResource(getSitesRoot(coralSession), name);
         if(res.length != 1)
         {
             return null;
         }
         return (SiteResource)res[0];
     }
 
     /**
      * Create a new site.
      *
      * @param template the site template.
      * @param name the site name.
      * @param description the site description.
      * @return the newly created site.
      * @throws InvalidResourceNameException 
      */
     public SiteResource createSite(CoralSession coralSession, SiteResource template, String name,
                                    String description, boolean requiresSecureChannel, Subject owner)
         throws SiteException, InvalidResourceNameException
     {
         if(!template.getTemplate())
         {
             throw new SiteException(template.getName()+" is not a site template");
         }
         Resource[] check = coralSession.getStore().getResource(getSitesRoot(coralSession), name);
         if(check.length != 0)
         {
             throw new SiteException("site "+name+" already exists");
         }
         try
         {
             coralSession.getStore().copyTree(template, getSitesRoot(coralSession), name);
         }
         catch(CircularDependencyException e)
         {
             throw new SiteException("site was not created properly", e);
         }
         check = coralSession.getStore().getResource(getSitesRoot(coralSession), name);
         if(check.length != 1)
         {
             throw new SiteException("site was not created properly");
         }
         SiteResource site = (SiteResource)check[0];
         site.setTemplate(false);
         site.setDescription(description);
         site.setRequiresSecureChannel(requiresSecureChannel);
         site.update();
         coralSession.getStore().setOwner(site, owner);
         setupSecurity(coralSession, site, owner);
         // notify listeners
         try
         {
             Method method = SiteCreationListener.class.
                 getMethod("createSite", new Class[] { SiteService.class, String.class, String.class });
             Object[] args = { this, template.getName(), name};
             eventWhiteboard.fireEvent(method, args, null);
         }
         catch(NoSuchMethodException e)
         {
             throw new SiteException("Incompatible change of SiteCopyingListener interface", e);
         }
         return site;
     }
 
     /**
      * Copy an existing, non-template site.
      *
      * @param source the site to copy.
      * @param destination the destination site name.
      * @return the copy site object.
      */
     public SiteResource copySite(CoralSession coralSession, SiteResource source, String destination)
         throws SiteException, InvalidResourceNameException
     {
         Resource[] check = coralSession.getStore().getResource(getSitesRoot(coralSession), destination);
         if(check.length != 0)
         {
             throw new SiteException("site "+destination+" already exists");
         }
         try
         {
             coralSession.getStore().copyTree(source, getSitesRoot(coralSession), destination);
         }
         catch(CircularDependencyException e)
         {
             throw new SiteException("site was not created properly", e);
         }
         // notify listeners
         try
         {
             Method method = SiteCopyingListener.class.
                 getMethod("copySite", new Class[] { String.class, String.class });
             Object[] args = { source.getName(), destination};
             eventWhiteboard.fireEvent(method, args, null);
         }
         catch(NoSuchMethodException e)
         {
             throw new SiteException("Incompatible change of SiteCopyingListener interface", e);
         }
         check = coralSession.getStore().getResource(getSitesRoot(coralSession), destination);
         return (SiteResource)check[0];
     }
 
     /**
      * Destroy a site.
      *
      * <p>CAUTION. This operation cannot be undone.</p>
      *
      * @param site the site to destroy.
      */
     public void destroySite(CoralSession coralSession, SiteResource site)
         throws SiteException
     {
         try
         {
             Method method = SiteDestructionListener.class.
                 getMethod("destroySite", new Class[] { SiteService.class, SiteResource.class });
             Object[] args = {this, site};
             eventWhiteboard.fireEvent(method, args, null);
         }
         catch(NoSuchMethodException e)
         {
             throw new SiteException("Incompatible change of SiteDestructionListener interface", e);
         }
         
         // do not forget about virtual servers...
         String[] mappings = getMappings(coralSession, site);
         for(String virtual: mappings)
         {
             removeMapping(coralSession, virtual);
         }
         try
         {
            coralSession.getStore().deleteResource(site);
         }
         catch(Exception e)
         {
             throw new SiteException("failed to delete site", e);
         }
         
     }
 
     
     /**
      * Sets up the roles and permissions needed for administering the site.
      *
      * @param site the site.
      * @param owner the site's owner.
      */
     protected void setupSecurity(CoralSession coralSession, SiteResource site, Subject owner)
     {
         String name = site.getName();
         try
         {
             Role masterAdmin = coralSession.getSecurity().getUniqueRole("cms.administrator");
             Role teamMember = coralSession.getSecurity().
                 createRole("cms.site.team_member."+name);
             Role administrator = coralSession.getSecurity().
                 createRole("cms.site.administrator."+name);
             Role layoutAdministrator = coralSession.getSecurity().
                 createRole("cms.layout.administrator."+name);
             Role siteRole = coralSession.getSecurity().
                 createRole("cms.site.siterole."+name);
 
             coralSession.getSecurity().addSubRole(masterAdmin, administrator);
             coralSession.getSecurity().addSubRole(administrator, layoutAdministrator);
             
             coralSession.getSecurity().
                 grant(teamMember, owner, true);
             coralSession.getSecurity().
                 grant(administrator, owner, true);
             
             cmsSecurityService.
                 registerRole(coralSession, site, teamMember, null, false,
                              false, "cms.site.team_member", null);
 
             // register the site team as a workgroup
             Role workgroup = coralSession.getSecurity().
                 getUniqueRole("cms.workgroup");
             coralSession.getSecurity().addSubRole(workgroup, teamMember);
 
             RoleResource administratorRole = cmsSecurityService.
                 registerRole(coralSession, site, administrator, null, false, false,
                             "cms.site.administrator", null);
             cmsSecurityService.
                 registerRole(coralSession, site, layoutAdministrator, null, false, false,
                              "cms.layout.administrator", administratorRole);
 
             site.setTeamMember(teamMember);
             site.setAdministrator(administrator);
             site.setLayoutAdministrator(layoutAdministrator);
             site.setSiteRole(siteRole);
             site.update();
 
             Permission siteAdminister = coralSession.getSecurity().
                 getUniquePermission("cms.site.administer");
             Permission layoutAdminister = coralSession.getSecurity().
                 getUniquePermission("cms.layout.administer");
             coralSession.getSecurity().
                 grant(site, administrator, siteAdminister, true);
             coralSession.getSecurity().
                 grant(site, layoutAdministrator, layoutAdminister, true);
             
             // general roles (created using SecurityService functionality)
             
             Role seniorEditor = cmsSecurityService.createRole(coralSession, administrator,
                 "cms.site.senior_editor", site);
             cmsSecurityService.createRole(coralSession, seniorEditor, "cms.site.editor", site);
             
             // subtree roles
 
             Role nodeAdministrator = cmsSecurityService.
                 createRole(coralSession, administrator, "cms.structure.administrator", structureService.getRootNode(coralSession, site));
             coralSession.getSecurity().
                 grant(nodeAdministrator, owner, true);
             Role visitor = cmsSecurityService.
                 createRole(coralSession, administrator, "cms.structure.visitor", structureService.getRootNode(coralSession, site));
             coralSession.getSecurity().
                 addSubRole(teamMember, visitor);
         }
         catch(Exception e)
         {
             log.error("failed to setup security for site "+name, e);
         }
     }
     
     public Resource getSitesRoot(CoralSession coralSession)
     {
         if(sites == null)
         {
             Resource[] res =  coralSession.getStore().getResourceByPath("/cms/sites");
             if(res.length != 1)
             {
                 throw new ComponentInitializationError("failed to lookup /cms/sites node");
             }
             sites = res[0];
         }
         return sites;
     }
     
     public Resource getAliasesRoot(CoralSession coralSession)
     {
         if(aliases == null)
         {
             Resource[] res =  coralSession.getStore().getResourceByPath("/cms/aliases");
             if(res.length != 1)
             {
                 throw new ComponentInitializationError("failed to lookup /cms/aliases node");
             }
             aliases = res[0];
         }
         return aliases;
     }
 }
