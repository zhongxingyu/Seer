 package net.cyklotron.cms.security.internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcontainer.dna.Configuration;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.BackendException;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityExistsException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.schema.AttributeDefinition;
 import org.objectledge.coral.schema.CircularDependencyException;
 import org.objectledge.coral.schema.UnknownAttributeException;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.PermissionAssignment;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.security.RoleAssignment;
 import org.objectledge.coral.security.RoleImplication;
 import org.objectledge.coral.security.SecurityException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.SubtreeVisitor;
 import org.objectledge.coral.store.ValueRequiredException;
 
 import net.cyklotron.cms.CmsTool;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.integration.SchemaPermissionResource;
 import net.cyklotron.cms.integration.SchemaRoleResource;
 import net.cyklotron.cms.security.CmsSecurityException;
 import net.cyklotron.cms.security.RoleResource;
 import net.cyklotron.cms.security.RoleResourceImpl;
 import net.cyklotron.cms.security.SubtreeRoleResource;
 import net.cyklotron.cms.security.SubtreeRoleResourceImpl;
 import net.cyklotron.cms.site.SiteResource;
 
 
 /**
  * An implementation of CMS SecurityService.
  *
  * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
  * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
  * @author <a href="mailto:pablo@ngo.pl">Pawe� Potempski</a>
  * @version $Id: SecurityServiceImpl.java,v 1.17 2008-12-16 16:24:53 rafal Exp $
  */
 public class SecurityServiceImpl
     implements net.cyklotron.cms.security.SecurityService
 {
     // instance variables ////////////////////////////////////////////////////
 
     private static final String TEAM_MEMBER_GROUP_SHORT_NAME = "@team_member";
 
     public static final String GROUP_NAME_PREFIX = "cms.site.group_member";
     
     public static final String TEAM_MEMBER_GROUP_NAME_PREFIX = "cms.site.team_member";
 
     private Logger logger;
     
     /** The integration service */
     private IntegrationService integrationService;
 
     private boolean allowAddUser;
     
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Initializes the service.
      */
     public SecurityServiceImpl(Logger logger, Configuration config, IntegrationService integrationService)
     {
         this.logger = logger;
         this.integrationService = integrationService;
         allowAddUser = config.getChild("allow_add_user").getValueAsBoolean(false);
     }
 
     // SecurityService interface /////////////////////////////////////////////
 
     public boolean getAllowAddUser()
     {
         return allowAddUser;
     }
     
     /**
      * Return the Subject entry for the named user, or create it if necessary.
      * 
      * @param coralSession the coral session.
      * @param dn user's Distinguished Name.
      * @return Subject object.
      */
     public Subject getSubject(CoralSession coralSession, String dn)
     {
         try 
         {
             return coralSession.getSecurity().getSubject(dn);
         }
         catch(EntityDoesNotExistException e)
         {
             // dn value came from UserManager.getUserBySubject(), so apparently the user is
             // present in the directory, but is missing a Coral Subject entry. Let's create it.
             try
             {
                 Subject subject = coralSession.getSecurity().createSubject(dn);
                 Role role = coralSession.getSecurity().getUniqueRole("cms.registered");
                 coralSession.getSecurity().grant(role, subject, false);
                 return subject;
             }
             catch(EntityExistsException ee)
             {
                 throw new IllegalArgumentException("internal error", ee);
             }
             catch(SecurityException ee)
             {
                 throw new IllegalArgumentException("internal error", ee);
             }
         }
     }
     
     /**
      * Returns the security information parent node, useful for showing role
      * tree using the Table toolkit.
      *
      * @param site the site in question.
      */
     public Resource getRoleInformationRoot(CoralSession coralSession, SiteResource site)
     {
         Resource parentNode = site;
         if(parentNode == null)
         {
             try
             {
                 parentNode = coralSession.getStore().getUniqueResourceByPath("/cms");
             }
             catch(AmbigousEntityNameException e)
             {
                 throw new IllegalStateException("ambigous cms main node");
             }
             catch(EntityDoesNotExistException e)
             {
                 throw new IllegalStateException("failed to lookuop cms main node");
             }
         }
         Resource res[] = coralSession.getStore().getResource(parentNode, "security");
         if (res.length != 1)
         {
             throw new IllegalStateException("failed to lookuop security node" +
                     " under '"+parentNode.getPath()+ "' resource");
         }
         return res[0];
     }
 
     /**
      * Returns the roles defined for a site.
      *
      * @param site the site in question.
      */
     public RoleResource[] getRoles(CoralSession coralSession,SiteResource site)
     {
         List<RoleResource> roles = new ArrayList<RoleResource>();
         Deque<Resource> stack = new LinkedList<Resource>();
         stack.push(getRoleInformationRoot(coralSession, site));
         while (!stack.isEmpty())
         {
             Resource r = stack.pop();
             for (Resource element : coralSession.getStore().getResource(r))
             {
                 roles.add((RoleResource)element);
                 stack.push(element);
             }
         }
         return roles.toArray(new RoleResource[roles.size()]);
     }
 
     /**
      * Returns a RoleResource object describing a specific role.
      *
      * @param site the site to search.
      * @param role the role in question.
      * @return the RoleResource object, or <code>null</code> if not found.
      */
     public RoleResource getRole(CoralSession coralSession,SiteResource site, Role role)
     {
         Deque<Resource> stack = new LinkedList<Resource>();
         stack.push(getRoleInformationRoot(coralSession, site));
         while (!stack.isEmpty())
         {
             Resource r = stack.pop();
             if (r instanceof RoleResource)
             {
                 RoleResource rr = (RoleResource)r;
                 if (rr.getRole().equals(role))
                 {
                     return rr;
                 }
             }
             for (Resource element : coralSession.getStore().getResource(r))
             {
                 stack.push(element);
             }
         }
         return null;
     }
 
     /**
      * Registers a role for a site.
      * @param site the site the role belongs to.
      * @param role the role.
      * @param subtree the root of the subtree the role has rigths to (may be
      * <code>null</code>).
      * @param recursive if subtree is not null, are rights assigned to the
      *        role, on the specified node recursive or not.
      * @param deletable can this role be deleted and recreated from the UI
      * @param descriptionKey the i18n key of the role's description.
      * @param superRole the role's super role (may be <code>null</code>).
      */
     public RoleResource registerRole(
         CoralSession coralSession,
         SiteResource site,
         Role role,
         Resource subtree,
         boolean recursive,
         boolean deletable,
         String descriptionKey,
         RoleResource superRole)
     {
         if (role == null)
         {
             throw new IllegalArgumentException("null role");
         }
         Resource root = getRoleInformationRoot(coralSession, site);
         Resource parent = (superRole != null) ? (Resource)superRole : root;
         
         Resource[] resources = coralSession.getStore().getResource(parent,role.getName());
         if(resources.length > 0)
         {
             //already registered
             return (RoleResource)resources[0];
         }
         
         RoleResource roleRes = null;
         try
         {
             try
             {
                 if(subtree == null)
                 {
                     roleRes = RoleResourceImpl.createRoleResource(coralSession, role.getName(),
                         parent, role, deletable, false);
                 }
                 else
                 {
                     roleRes = SubtreeRoleResourceImpl.createSubtreeRoleResource(coralSession, role
                         .getName(), parent, role, deletable, subtree, recursive, false);
                 }
             }
             catch(InvalidResourceNameException e)
             {
                 throw new RuntimeException("role " + role.toString()
                     + "has illegal characters in it's name", e);
             }
             /*
             CrossReference refs = root.getRelations();
             refs.put(parent, roleRes);
             root.setRelations(refs);
             root.update(subject);
             */
         }
         catch (ValueRequiredException e)
         {
             // won't happen
         }
         if (descriptionKey != null)
         {
             roleRes.setDescriptionKey(descriptionKey);
             roleRes.update();
         }
         return roleRes;
     }
 
     /**
      * Unregisters a role for a site.
      *
      * @param site the site.
      * @param role the role.
      */
     public void unregisterRole(CoralSession coralSession, SiteResource site, Role role, boolean ignoreDeletableFlag) throws EntityInUseException
     {
         RoleResource roleRes = getRole(coralSession, site, role);
         if (!ignoreDeletableFlag && !roleRes.getDeletable())
         {
             throw new IllegalArgumentException("role " + role.getName() + " is not deletable");
         }
         try
         {
             Resource[] children = coralSession.getStore().getResource(roleRes);
             for (int i = 0; i < children.length; i++)
             {
                 coralSession.getStore().setParent(children[i], roleRes.getParent());
             }
         }
         catch (CircularDependencyException e)
         {
             throw new BackendException("unexpected ARL exception", e);
         }
         coralSession.getStore().deleteResource(roleRes);
     }
 
     // here goes the changes...
 
     /**
      * Creates a Role upon following parameters:
      * @param superRole    super role of all roles defined in roles schema
      * @param roleName     selected role name - one which can be found in roles schema
      * @param subtree      subtree of resources for which role is created
      */
     public Role createRole(CoralSession coralSession, Role superRole, String roleName, Resource subtree) throws CmsSecurityException
     {
         Resource schemaRoleRoot = integrationService.getSchemaRoleRoot(coralSession, subtree.getResourceClass());
 
         // get a schema role representing created role
         SchemaRoleResource schemaRoleResource = getSchemaRoleResource(coralSession, schemaRoleRoot, roleName);
         if (schemaRoleResource == null)
         {
             throw new CmsSecurityException("Couldn't find role schema for resource class: " + subtree.getResourceClass().getName());
         }
 
         Role role = null;
 
         // check if role reference should be saved in subtree attribute
         AttributeDefinition roleAttributeDef = null;
         if (schemaRoleResource.getRoleAttributeName() != null && subtree.getResourceClass().hasAttribute(schemaRoleResource.getRoleAttributeName()))
         {
             roleAttributeDef = subtree.getResourceClass().getAttribute(schemaRoleResource.getRoleAttributeName());
             role = (Role) (subtree.get(roleAttributeDef));
         }
 
         if (role == null)
         {
             // get site
             SiteResource site = CmsTool.getSite(subtree);
             
             //find super role from schema and stick current role under it
             SchemaRoleResource schemaSuperRole = (SchemaRoleResource)schemaRoleResource.getSuperRole();
             // fallback to parent resource in role schema when superRole attribute is not set
             if(schemaSuperRole == null && schemaRoleResource.getParent() instanceof SchemaRoleResource)
             {
                 schemaSuperRole = (SchemaRoleResource)schemaRoleResource.getParent();
             }
             Role newSuperRole = null;
             while (schemaSuperRole != null && newSuperRole == null)
             {
                 String realSuperRoleName = roleNameFromSufix(schemaSuperRole, subtree);
                 Role[] temp = coralSession.getSecurity().getRole(realSuperRoleName);
                 if (temp.length > 1)
                 {
                     throw new CmsSecurityException("Cannot cope with multiple super roles");
                 }
                 else if (temp.length == 1)
                 {
                     newSuperRole = temp[0];
                 }
                 schemaSuperRole = (SchemaRoleResource)schemaSuperRole.getSuperRole();
             }
             if (newSuperRole != null)
             {
                 superRole = newSuperRole;
             }
             else
             {
                 // here check if super role in resource view exist in
                 // reference...
                 /*
                 if(schemaRoleResource.getSubtreeRole())
                 {
                     Resource parent = subtree.getParent();
                     while(parent != null)
                     {
                     parent = parent.getParent();
                     }
                 }
                 */
             }
 
             // get super role resource -> null if has no super role
             RoleResource superRoleRes = getRole(coralSession, site, superRole);
 
             // try to grab or create the role
             String realRoleName = roleNameFromSufix(schemaRoleResource, subtree);
             Role[] temp = coralSession.getSecurity().getRole(realRoleName);
             if (temp.length == 1)
             {
                 role = temp[0];
             }
             else
             {
                 role = coralSession.getSecurity().createRole(realRoleName);
 
                 // create role resource
                 RoleResource roleRes = null;
                 // create a subtree role according to role schema
                 if (schemaRoleResource.getSubtreeRole())
                 {
                     roleRes =
                         registerRole(
                             coralSession,
                             site,
                             role,
                             subtree,
                             schemaRoleResource.getRecursive(),
                             schemaRoleResource.getDeletable(),
                             roleName,
                             superRoleRes);
                 }
                 else
                 {
                     roleRes =
                         registerRole(coralSession, site, role, null, schemaRoleResource.getRecursive(), schemaRoleResource.getDeletable(), roleName, superRoleRes);
                 }
 
                 // grant permissions
                 grantPermissions(coralSession, schemaRoleResource, roleName, subtree);
 
                 // add "role" under "superRole" and move subroles under current role
                 try
                 {
                     coralSession.getSecurity().addSubRole(superRole, role);
 
                     Resource[] resources = coralSession.getStore().getResource(schemaRoleResource);
                     for (int i = 0; i < resources.length; i++)
                     {
                         if (resources[i] instanceof SchemaRoleResource)
                         {
                             SchemaRoleResource schemaSubRole = (SchemaRoleResource)resources[i];
                             String subRoleName = roleNameFromSufix(schemaSubRole, subtree);
                             Role[] subRole = coralSession.getSecurity().getRole(subRoleName);
                             for (int j = 0; j < subRole.length; j++)
                             {
                                 Role currentSubRole = subRole[j];
                                 if (currentSubRole != null)
                                 {
                                     coralSession.getSecurity().deleteSubRole(superRole, currentSubRole);
                                     coralSession.getSecurity().addSubRole(role, currentSubRole);
                                     coralSession.getStore().setParent(getRole(coralSession, site, currentSubRole), roleRes);
                                 }
                             }
                         }
                     }
                 }
                 catch (CircularDependencyException e)
                 {
                     throw new CmsSecurityException("Error when moving subroles", e);
                 }
             }
 
             // save role reference in subtree attribute if needed
             if (roleAttributeDef != null)
             {
                 try
                 {
                     subtree.set(roleAttributeDef, role);
                     subtree.update();
                 }
                 catch (Exception e)
                 {
                     throw new CmsSecurityException("ARL error when assigning role to resource", e);
                 }
             }
         }
 
         return role;
     }
 
     /**
      * Deletes a role assigned to a resource subtree, role is revoked with root subject rights.
      * @param roleName selected role name - one which can be found in roles schema
      * @param subtree the resource to which a role is assigned.
      */
     public void deleteRole(CoralSession coralSession, String roleName, Resource subtree, boolean ignoreDeletableFlag) throws CmsSecurityException
     {
         Resource schemaRoleRoot = integrationService.getSchemaRoleRoot(coralSession, subtree.getResourceClass());
         // get a schema role representing created role
         SchemaRoleResource schemaRoleResource = getSchemaRoleResource(coralSession, schemaRoleRoot, roleName);
         if (schemaRoleResource == null)
         {
             throw new CmsSecurityException("Couldn't find role '" + roleName + "' in schema for resource class: " + subtree.getResourceClass().getName());
         }
 
         // get the role
         Role role = null;
         Role[] temp = coralSession.getSecurity().getRole(roleNameFromSufix(schemaRoleResource, subtree));
         if (temp.length > 1)
         {
             throw new CmsSecurityException("Cannot delete roles, many roles with the same name");
         }
         else if (temp.length == 1)
         {
             role = temp[0];
         }
         else // == 0 - guard from null pointer exceptions when there is no role to delete
             {
             return;
         }
 
         // remove resource -> role reference if one exists
         if (schemaRoleResource.getRoleAttributeName() != null && subtree.getResourceClass().hasAttribute(schemaRoleResource.getRoleAttributeName()))
         {
             try
             {
                 AttributeDefinition roleAttributeDef = subtree.getResourceClass().getAttribute(schemaRoleResource.getRoleAttributeName());
                 subtree.unset(roleAttributeDef);
                 subtree.update();
             }
             catch (Exception e)
             {
                 throw new CmsSecurityException("ARL error when removing role from resource", e);
             }
         }
 
         // get site
         
         SiteResource site = CmsTool.getSite(subtree);
 
         // delete the role from RoleResource tree and Role graph
         try
         {
             unregisterRole(coralSession, site, role, ignoreDeletableFlag);
             RoleAssignment[] assignments = role.getRoleAssignments();
             for (int i = 0; i < assignments.length; i++)
             {
                 coralSession.getSecurity().revoke(role, assignments[i].getSubject());
             }
             Role superRole = null;
             List<Role> subRoles = new ArrayList<Role>();
             for (RoleImplication implication : role.getImplications())
             {
                 if (implication.getSubRole().equals(role))
                 {
                     superRole = implication.getSuperRole();
                     coralSession.getSecurity().deleteSubRole(superRole, role);
                 }
                 else
                 {
                     subRoles.add(implication.getSubRole());
                     coralSession.getSecurity().deleteSubRole(role, implication.getSubRole());
                 }
             }
             for (Role subRole : subRoles)
             {
                 coralSession.getSecurity().addSubRole(superRole, subRole);
             }
             for(PermissionAssignment pa : role.getPermissionAssignments())
             {
                 coralSession.getSecurity().revoke(pa.getResource(), role, pa.getPermission());
                 if(superRole != null && !superRole.hasPermission(pa.getResource(), pa.getPermission()))
                 {
                     coralSession.getSecurity().grant(pa.getResource(), superRole,
                         pa.getPermission(), pa.isInherited());
                 }
             }
             coralSession.getSecurity().deleteRole(role);
         }
         catch (Exception e)
         {
             throw new CmsSecurityException("failed to delete role", e);
         }
     }
 
     /**
      * Removes any roles created using the schema from the resource, or a
      * resource tree.
      * @param resource the resource, or tree root.
      * @param recursive <code>true<?code> to cleanup whole tree.
      */
     public void cleanupRoles(CoralSession coralSession, Resource resource, boolean recursive) throws CmsSecurityException
     {
 		if (recursive)
         {
 			logger.debug("Probuje oczyscic zasob ktory jest rekursywny: "+resource.getPath());
             Deque<Resource> stack = new LinkedList<Resource>();
             stack.push(resource);
             while (!stack.isEmpty())
             {
 				resource = stack.pop();
 				logger.debug("Zdejmuje ze stosu zasob "+resource.getPath());
                 cleanupRoles(coralSession, resource);
                 for (Resource element : coralSession.getStore().getResource(resource))
                 {
 					logger.debug("Wkladam na stos "+element.getPath());
                     stack.push(element);
                 }
             }
         }
         else
         {
             cleanupRoles(coralSession, resource);
         }
     }
 
     private void cleanupRoles(CoralSession coralSession, Resource resource) throws CmsSecurityException
     {
 		logger.debug("Probuje oczyscic zasob: "+resource.getPath());
         try
         {
 			for (PermissionAssignment element : resource.getPermissionAssignments())
             {
                 coralSession.getSecurity().revoke(resource, element.getRole(), element.getPermission());
             }
 			logger.debug("Zdjalem wszystkie prawa z zasobu");
             Resource schemaRoleRoot = integrationService.getSchemaRoleRoot(coralSession, resource.getResourceClass());
 			logger.debug("Pobralem scheme dla zasobu");
             //prepare stack
             Deque<Resource> stack = new LinkedList<Resource>();
             prepareStack(coralSession, stack, schemaRoleRoot);
 			logger.debug("Przygotowalem stos definicji rol");
             
             Resource securityRoot = getRoleInformationRoot(coralSession, getSite(resource));
 
             //put roles by name    
             Map<String, RoleResource> map = new HashMap<String, RoleResource>();
             prepareRolesMap(coralSession, map, securityRoot);
 			logger.debug("Przygotowalem mape rol");
             while (!stack.isEmpty())
             {
                 SchemaRoleResource roleDef = (SchemaRoleResource)stack.pop();
                 String roleName = roleNameFromSufix(roleDef, resource);
                 if (map.containsKey(roleName))
                 {
 					logger.debug("No to mam role: " + roleName);
                     RoleResource roleResource = (RoleResource)map.get(roleName);
 					logger.debug("No to mam role2");
                     // unbind role from resource
                     try
                     {
 						AttributeDefinition roleAttributeDef = resource.getResourceClass().getAttribute(roleDef.getRoleAttributeName());
 						logger.debug("Pobralem def atrybutu");
                         resource.unset(roleAttributeDef);
 						logger.debug("Kasuje atrybut");
                         resource.update();
 						logger.debug("Aktualizuje baze");
                     }
                     catch (UnknownAttributeException e)
                     {
 						logger.debug("EXCEPTION"+e);    // ignore it.
                     }
                     catch (ValueRequiredException e)
                     {
                         // ignore it.
 						logger.debug("EXCEPTION2"+e);
                     }
 					Role role = coralSession.getSecurity().getUniqueRole(roleName);
 					RoleAssignment[] ra = role.getRoleAssignments();
 					for(int i = 0; i < ra.length; i++)
 					{
 						coralSession.getSecurity().revoke(ra[i].getRole(), ra[i].getSubject());
 					}
 					logger.debug("Usunalem granty");
 					RoleImplication[] ri = role.getImplications();
 					for(int i = 0; i < ri.length; i++)
 					{
 						coralSession.getSecurity().deleteSubRole(ri[i].getSuperRole(), ri[i].getSubRole());
 					}
 					logger.debug("Usunalem zaleznosci rol");
 					logger.debug("A teraz pragne usunac zasob nr. "+roleResource.getIdString()+" : "+roleResource.getPath());
 					coralSession.getStore().deleteResource(roleResource);
 					logger.debug("No i udalo sie");
 					logger.debug("I jeszcze sprobujemy usunac kikut roli");
                     try
                     {
                         coralSession.getSecurity().deleteRole(role);
                         logger.debug("No i ponownie udalo sie");
                     }
                     catch(Exception e)
                     {
                         logger.debug("No i udalo sie usunąć roli");
                     }
                }
                 else
                 {
                     logger.debug("Nie mam roli: " + roleName);
                 }
 				logger.debug("Pa pa resource\n");
             }
 
             /*
             AttributeDefinition attrs[] = resource.getResourceClass().getAllAttributes();
             for (int i = 0; i < attrs.length; i++)
             {
                 AttributeDefinition attr = attrs[i];
                 if(Role.class.isAssignableFrom(attr.getAttributeClass().getJavaClass()))
                 {
                     Role role = (Role)resource.get(attr);
                     if(role != null)
                     {
                         String roleName = role.getName();
                         roleName = roleName.substring(0, roleName.lastIndexOf('.'));
                         SchemaRoleResource schemaRoleResource = getSchemaRoleResource(schemaRoleRoot, roleName);
                         if(schemaRoleResource != null)
                         {
                             deleteRole(roleName, resource, subject, true);
                         }
                     }
                 }
             }
             */
         }
         catch (Exception e)
         {
             throw new CmsSecurityException("Exception occured: ", e);
         }
 		logger.debug("Skonczylem czyscic zasob: "+resource.getPath());
     }
 
     private void prepareStack(CoralSession coralSession, Deque<Resource> stack, Resource resource)
     {
         for (Resource child : resource.getChildren())
         {
             if (child instanceof SchemaRoleResource)
             {
                 stack.push(child);
                 prepareStack(coralSession, stack, child);
             }
         }
     }
 
     private void prepareRolesMap(CoralSession coralSession, Map<String, RoleResource> map, Resource resource)
     {
         for (Resource child : resource.getChildren())
         {
             if (child instanceof RoleResource)
             {
                 map.put(child.getName(), (RoleResource)child);
                 prepareRolesMap(coralSession, map, child);
             }
         }
     }
 
     private SiteResource getSite(Resource resource) throws CmsSecurityException
     {
         Resource temp = resource;
         while (temp != null && !(temp instanceof SiteResource))
         {
             temp = temp.getParent();
         }
         if (temp == null)
         {
             throw new CmsSecurityException("Resource does not belong to any site");
         }
         return (SiteResource)temp;
     }
 
     /**
      * Get the role name for specified schemar role and resource.
      *
      * @param schemaRole the schema role.
      * @param resource the resource.
      * @return role name.
      */
     public String roleNameFromSufix(SchemaRoleResource schemaRole, Resource resource)
     {
         String roleName = schemaRole.getName();
         if (schemaRole.getSuffixAttributeName() != null && schemaRole.getSuffixAttributeName().length() > 0)
         {
             AttributeDefinition roleSuffixAttributeDef = resource.getResourceClass().getAttribute(schemaRole.getSuffixAttributeName());
             roleName = roleName + '.' + resource.get(roleSuffixAttributeDef);
         }
         return roleName;
     }
 
     /**
      * Get the role for specified resource and base role name.
      *
      * @param roleName the base name of the role.
      * @param resource the resource.
      * @return the role.
      */
     public Role getRole(CoralSession coralSession,String roleName, Resource resource) throws CmsSecurityException
     {
         Resource schemaRoot = integrationService.getSchemaRoleRoot(coralSession, resource.getResourceClass());
         SchemaRoleResource schema = getSchemaRoleResource(coralSession, schemaRoot, roleName);
         if (schema == null)
         {
             throw new CmsSecurityException(
                 "Schema resource for role '" + roleName + "' does not exists in schema defined for class '" + resource.getResourceClass().getName() + "'");
         }
         String fullRoleName = roleNameFromSufix(schema, resource);
         Role role = coralSession.getSecurity().getUniqueRole(fullRoleName);
         return role;
     }
 
     /**
      * Grants permissions to a given role on a given subtree according to a given role schema.
      *
      * @param schemaRoleResource roles schema - a tree of roles with assigned permissions
      * @param roleName selected role name - one which allows finding an original role name
      *                                         in roles schema
      * @param subtree the resource to which a role is assigned, and from which gets its name suffix.
      */
     public void grantPermissions(CoralSession coralSession,SchemaRoleResource schemaRoleResource, String roleName, Resource subtree) throws CmsSecurityException
     {
         // get the role
         Role role = null;
         String realRoleName = roleNameFromSufix(schemaRoleResource, subtree);
         Role[] temp = coralSession.getSecurity().getRole(realRoleName);
         if (temp.length == 1)
         {
             role = temp[0];
         }
         else
         {
             throw new CmsSecurityException("Cannot grant permission (many roles or no role with the name '" + realRoleName + "')");
         }
 
         try
         {
             grantPermisions(coralSession,schemaRoleResource, role, subtree, true);
         }
         catch (SecurityException e)
         {
             throw new CmsSecurityException("Can't grant permissions", e);
         }
     }
 
     private void grantPermisions(CoralSession coralSession,SchemaRoleResource schemaRoleResource, Role role, Resource subtree, boolean recursive)
         throws SecurityException
     {
         Resource[] resources = coralSession.getStore().getResource(schemaRoleResource);
         for (int i = 0; i < resources.length; i++)
         {
             if (resources[i] instanceof SchemaPermissionResource)
             {
                 SchemaPermissionResource schemaPermissionResource = (SchemaPermissionResource)resources[i];
                 Permission permission = coralSession.getSecurity().getUniquePermission(schemaPermissionResource.getName());
                 if (!role.hasPermission(subtree, permission))
                 {
                     coralSession.getSecurity().grant(subtree, role, permission, schemaPermissionResource.getRecursive());
                 }
             }
             if (recursive && resources[i] instanceof SchemaRoleResource)
             {
                 grantPermisions(coralSession,(SchemaRoleResource)resources[i], role, subtree, true);
             }
         }
     }
 
     // useful private methods
     private SchemaRoleResource getSchemaRoleResource(CoralSession coralSession,Resource schemaRolesRoot, String roleName)
     {
         Resource[] resources = coralSession.getStore().getResource(schemaRolesRoot);
         for (int i = 0; i < resources.length; i++)
         {
             if (resources[i] instanceof SchemaRoleResource)
             {
                 SchemaRoleResource result = getSchemaRoleResource(coralSession,(SchemaRoleResource)resources[i], roleName);
                 if (result != null)
                 {
                     return result;
                 }
             }
         }
         return null;
     }
 
     private SchemaRoleResource getSchemaRoleResource(CoralSession coralSession, SchemaRoleResource schemaRole, String roleName)
     {
         // Deep first search
         if (schemaRole.getName().equals(roleName))
         {
             return schemaRole;
         }
         Resource[] resources = coralSession.getStore().getResource(schemaRole);
         for (int i = 0; i < resources.length; i++)
         {
             if (resources[i] instanceof SchemaRoleResource)
             {
                 SchemaRoleResource result = getSchemaRoleResource(coralSession,(SchemaRoleResource)resources[i], roleName);
                 if (result != null)
                 {
                     return result;
                 }
             }
         }
         return null;
     }
     
     
     public void cleanupSite(CoralSession coralSession, SiteResource site)
         throws Exception 
     {
         site.setAdministrator(null);
         site.setLayoutAdministrator(null);
         site.setTeamMember(null);
         Role siteRole = site.getSiteRole();
         site.setSiteRole(null);
         site.update();
         if(siteRole != null)
         {
             clearRole(coralSession, siteRole.getName());
             deleteRole(coralSession, siteRole.getName());
         }
         Resource[] res = coralSession.getStore().getResource(site, "security");
         if(res.length == 0)
         {
             return;
         }
         
         RoleResource[] roles = getRoles(coralSession, site);
         for(int i = roles.length-1; i >= 0; i--)
         {
             RoleResource roleDef = roles[i];
             Role role = roleDef.getRole();
             if(roleDef instanceof SubtreeRoleResource)
             {
                 deleteRole(coralSession, roleDef.getDescriptionKey(), ((SubtreeRoleResource)roleDef).getSubtreeRoot(), true);
             }
             else
             {
                 coralSession.getStore().deleteResource(roleDef);
                 clearRole(coralSession, role.getName());
                 deleteRole(coralSession, role.getName());
             }
         }
     }
       
     private void clearRole(CoralSession coralSession, String name)
         throws Exception
     {
         Role[] p = coralSession.getSecurity().getRole(name);
         for (int i = 0; i < p.length; i++)
         {
             PermissionAssignment[] pa = p[i].getPermissionAssignments();
             for(int j = 0; j < pa.length; j++)
             {
                 coralSession.getSecurity().revoke(pa[j].getResource(), pa[j].getRole(), pa[j].getPermission());
             }
         }
     }
     
     private void deleteRole(CoralSession coralSession, String name)
         throws Exception
     {
         try
         {
             Role[] p = coralSession.getSecurity().getRole(name);
             for (int i = 0; i < p.length; i++)
             {
                 RoleAssignment[] ra = p[i].getRoleAssignments();
                 for(int j = 0; j < ra.length; j++)
                 {
                     coralSession.getSecurity().revoke(ra[j].getRole(), ra[j].getSubject());
                 }
                 RoleImplication[] ri = p[i].getImplications();
                 for(int j = 0; j < ri.length; j++)
                 {
                     coralSession.getSecurity().deleteSubRole(ri[j].getSuperRole(), ri[j].getSubRole());
                 }
                 coralSession.getSecurity().deleteRole(p[i]);
             }
         }
         catch(Exception e)
         {
             throw new Exception("failed to delete role: '"+name+"'", e);
         }
     }
     
     @Override
     public RoleResource[] getGroups(CoralSession coralSession, SiteResource site)
     {
         ArrayList<RoleResource> result = new ArrayList<RoleResource>();
         Resource roleRoot = getRoleInformationRoot(coralSession, site);
         for(Resource r : roleRoot.getChildren())
         {
             if(r instanceof RoleResource && r.getName().startsWith(GROUP_NAME_PREFIX))
             {
                 result.add((RoleResource)r);
             }
         }
         return result.toArray(new RoleResource[result.size()]);
     }
 
     @Override
     public RoleResource createGroup(CoralSession coralSession, SiteResource site, String groupName, boolean sharingWorkgroup)
         throws CmsSecurityException
     {
         if(!isValidGroupName(groupName))
         {
             throw new CmsSecurityException("invalid group name");
         }
         if(isGroupNameInUse(coralSession, site, groupName))
         {
             throw new CmsSecurityException("group already exists");
         }
         String fullName = getFullGroupName(site, groupName);
         Resource roleRoot = getRoleInformationRoot(coralSession, site);
         try
         {
             Role groupRole = coralSession.getSecurity().createRole(fullName);
             RoleResource groupResource = RoleResourceImpl.createRoleResource(coralSession, fullName, roleRoot, groupRole, true, sharingWorkgroup);      
             return groupResource;
         }
         catch(Exception e)
         {
             throw new CmsSecurityException("internal error", e);
         }
     }
 
     @Override
     public boolean isGroupResource(RoleResource roleResource)
     {
         return roleResource.getName().startsWith(GROUP_NAME_PREFIX)
             || roleResource.getName().startsWith(TEAM_MEMBER_GROUP_NAME_PREFIX);
     }
 
     @Override
     public boolean isValidGroupName(String groupName)
     {
         if(groupName.length() == 0)
         {
             return false;
         }
         for(char c : groupName.toCharArray())
         {
             if(!Character.isLetterOrDigit(c) && c != '_' && c != ' ')
             {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public boolean isGroupNameInUse(CoralSession coralSession, SiteResource site, String groupName)
     {
         String fullName = getFullGroupName(site, groupName);
         Resource roleRoot = getRoleInformationRoot(coralSession, site);
         for(Resource r : roleRoot.getChildren())
         {
             if(r.getName().equals(fullName))
             {
                 return true;                
             }
         }
         return false;
     }
 
     public String getFullGroupName(SiteResource site, String groupName)
     {
         return GROUP_NAME_PREFIX + "." + site.getName() + "." + groupName;
     }
     
     @Override
     public String getShortGroupName(RoleResource roleResource) throws CmsSecurityException
     {
         if(roleResource.getName().startsWith(TEAM_MEMBER_GROUP_NAME_PREFIX))
         {
             return TEAM_MEMBER_GROUP_SHORT_NAME;
         }
         Resource site = roleResource.getParent().getParent();
         if(!roleResource.getName().startsWith(GROUP_NAME_PREFIX))
         {
             throw new CmsSecurityException("invalid RoleResource: invalid prefix");           
         }
         if(!roleResource.getName().substring(GROUP_NAME_PREFIX.length() + 1).startsWith(site.getName()+"."))
         {
             throw new CmsSecurityException("invalid RoleResource: invalid site name infix");           
         }
         return roleResource.getName().substring(GROUP_NAME_PREFIX.length() + site.getName().length() + 2);
     }
     
     @Override
     public void deleteGroup(CoralSession coralSession, RoleResource group)
         throws CmsSecurityException
     {
         getShortGroupName(group); // to perform the check if the RoleResource does represent a group
         try
         {
             Role r = group.getRole();
             coralSession.getStore().deleteResource(group);
             for(RoleAssignment assignment : r.getRoleAssignments())
             {
                 coralSession.getSecurity().revoke(r, assignment.getSubject());
             }
             for(RoleImplication implication : r.getImplications())
             {
                 coralSession.getSecurity().deleteSubRole(implication.getSuperRole(), implication.getSubRole());
             }
             coralSession.getSecurity().deleteRole(r);
         }
         catch(EntityInUseException e)
         {
             throw new CmsSecurityException("internal error", e);
         }
         catch(IllegalArgumentException e)
         {
             throw new CmsSecurityException("internal error", e);
         }
         catch(SecurityException e)
         {
             throw new CmsSecurityException("internal error", e);
         }
     }
     
     @Override
     public Set<Subject> getSharingWorkgroupPeers(CoralSession coralSession, SiteResource site,
         Subject subject)
         throws CmsSecurityException
     {
         Set<Subject> peers = new HashSet<Subject>();
         for(RoleResource roleRes : getRoles(coralSession, site))
         {
             if(isGroupResource(roleRes) && roleRes.getSharingWorkgroup() && subject.hasRole(roleRes.getRole()))
             {
                 peers.addAll(Arrays.asList(roleRes.getRole().getSubjects()));
             }
         }     
         peers.add(subject); // make sure the subject is included in the result set  
         return peers;
     }
     
     public String subtreeRoleConsistencyUpdate(CoralSession coralSession, final boolean plan)
         throws CmsSecurityException
     {
         final StringBuilder buff = new StringBuilder();
 
         try
         {
             // collect all subtree roles in the system
             QueryResults q = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM cms.security.subtree_role");
             // organize subtree roles according to their subtree root and description key
             Map<Resource, Map<String, SubtreeRoleResource>> resourceRoleSet = new HashMap<Resource, Map<String, SubtreeRoleResource>>();
             for(Resource r : q.getArray(1))
             {
                 SubtreeRoleResource srr = (SubtreeRoleResource)r;
                 Resource root = srr.getSubtreeRoot();
                 Map<String, SubtreeRoleResource> roleSet = resourceRoleSet.get(root);
                 if(roleSet == null)
                 {
                     roleSet = new HashMap<String, SubtreeRoleResource>();
                     resourceRoleSet.put(root, roleSet);
                 }
                 roleSet.put(srr.getDescriptionKey(), srr);
             }
             // iterate over all resources that have associated subtreeRoles
             for(Resource r : resourceRoleSet.keySet())                
             {
                 final SiteResource site = CmsTool.getSite(r);
                 final String rClassName = r.getResourceClass().getName();
                 final String location = site != null ? r.getPath().substring(site.getPath().length()) : "";                
                 final Map<String, SubtreeRoleResource> existingRoles = resourceRoleSet.get(r);
                 // find role schema apropriate for the resource's class
                 final Resource schemaRoleRoot = integrationService.getSchemaRoleRoot(coralSession,
                     r.getResourceClass());
                 // set of permissions that each of the subtree role implementations should have
                 final Map<String, Set<SchemaPermissionResource>> specificRolePermissions = new HashMap<String, Set<SchemaPermissionResource>>();
                 // collect all permissions per defined role schema node
                 new SubtreeVisitor()
                     {
                         @SuppressWarnings("unused")
                         public void visit(SchemaRoleResource p)
                         {
                             specificRolePermissions.put(p.getName(),
                                 new HashSet<SchemaPermissionResource>());
                         }
 
                         @SuppressWarnings("unused")
                         public void visit(SchemaPermissionResource p)
                         {
                             Set<SchemaPermissionResource> pSet = specificRolePermissions.get(p
                                 .getParent().getName());
                             pSet.add(p);
                         }
                     }.traverseBreadthFirst(schemaRoleRoot);
                 // traverse schema tree depth first, ie. starting from leaves
                 new SubtreeVisitor()
                     {
                         @SuppressWarnings("unused")
                         public void visit(SchemaRoleResource p)
                         {
                             // no actual subtree role corresponding to this schema node exists
                             if(!existingRoles.containsKey(p.getName()))
                             {
                                 Set<SchemaPermissionResource> pSet = specificRolePermissions
                                     .remove(p.getName());
                                 // if this schema role has super role attach the permissions to the
                                 // super role
                                 if(p.getParent() instanceof SchemaRoleResource)
                                 {
                                     specificRolePermissions.get(p.getParent().getName()).addAll(
                                         pSet);
                                 }
                             }
                         }
                     }.traverseDepthFirst(schemaRoleRoot);
                 new SubtreeVisitor()
                     {
                         @SuppressWarnings("unused")
                         public void visit(SchemaRoleResource p)
                         {
                             // actual subtree role corresponding to this schema node exists
                             if(existingRoles.containsKey(p.getName()))
                             {
                                 Set<SchemaPermissionResource> thisPSet = specificRolePermissions
                                     .get(p.getName());
                                 // traverse ancestor schema nodes
                                 Resource pp = p.getParent();
                                 while(pp instanceof SchemaRoleResource)
                                 {
                                     // ancestor has actual subtree role
                                     if(existingRoles.containsKey(pp.getName()))
                                     {
                                         Set<SchemaPermissionResource> parentPSet = specificRolePermissions
                                             .get(pp.getName());
                                         Iterator<SchemaPermissionResource> i = parentPSet.iterator();
                                         // compare permission schema nodes in each set
                                         while(i.hasNext())
                                         {
                                             SchemaPermissionResource spr1 = i.next();
                                             for(SchemaPermissionResource spr2 : thisPSet)
                                             {
                                                 // if permission schema nodes are equivalent, remove from parent set
                                                 if(spr1.getName().equals(spr2.getName()) && spr1.getRecursive() == spr2.getRecursive())
                                                 {
                                                     i.remove();
                                                 }
                                             }
                                         }
                                     }
                                     pp = pp.getParent();
                                 }
                             }
                         }
 
                     }.traverseDepthFirst(schemaRoleRoot);
                 // check grants on the existing roles
                 for(String roleName : existingRoles.keySet())
                 {
                     SubtreeRoleResource srr = existingRoles.get(roleName);
                     // check for missing permissions
                     Set<SchemaPermissionResource> pSet = specificRolePermissions.get(roleName);
                     for(SchemaPermissionResource spr : pSet)
                     {
                         boolean found = false;
                         boolean needsUpgrade = false;
                         for(PermissionAssignment pa : srr.getRole().getPermissionAssignments())
                         {
                             if(pa.getResource().equals(r)
                                 && pa.getPermission().getName().equals(spr.getName()))
                             {
                                 if(pa.isInherited() == false && spr.getRecursive() == true)
                                 {
                                     needsUpgrade = true;
                                 }
                                 found = true;
                             }
                         }
                         if(!found || needsUpgrade)
                         {
                             if(needsUpgrade)
                             {
                                 if(plan)
                                 {
                                     buff.append(site.getName()).append(";");
                                     buff.append(rClassName).append(";");
                                     buff.append(srr.getName()).append(";");
                                     buff.append("revoke1;");
                                     buff.append(location).append(";");
                                 }
                                 // move non-recursive grant out of the way
                                 buff.append("REVOKE PERMISSION ").append(spr.getName()).append(" ");
                                 buff.append("ON '").append(r.getPath().replace("&","&amp;")).append("' ");
                                 buff.append("FROM " + srr.getName());
                                 buff.append(";\n");
                             }
                             if(plan)
                             {
                                 buff.append(site.getName()).append(";");
                                 buff.append(rClassName).append(";");
                                 buff.append(srr.getName()).append(";");
                                 buff.append("grant;");
                                 buff.append(location).append(";");
                             }
                             buff.append("GRANT PERMISSION ").append(spr.getName()).append(" ");
                             buff.append("ON '").append(r.getPath().replace("&","&amp;")).append("' ");
                             if(spr.getRecursive())
                             {
                                 buff.append("RECURSIVE ");
                             }
                             buff.append("TO " + srr.getName());
                             buff.append(";\n");
                         }
                     }
                     // check for superfluous permissions
                     for(PermissionAssignment pa : srr.getRole().getPermissionAssignments())
                     {
                         boolean found = false;
                         for(SchemaPermissionResource spr : pSet)
                         {
                             if(pa.getResource().equals(r)
                                 && pa.getPermission().getName().equals(spr.getName()))
                             {
                                 found = true;
                             }
                         }
                         if(!found)
                         {
                             if(plan)
                             {
                                 buff.append(site.getName()).append(";");
                                 buff.append(rClassName).append(";");
                                 buff.append(srr.getName()).append(";");
                                 buff.append("revoke;");
                                 buff.append(location).append(";");
                             }
                             buff.append("REVOKE PERMISSION ").append(pa.getPermission().getName()).append(" ");
                             buff.append("ON '").append(r.getPath().replace("&","&amp;")).append("' ");
                             buff.append("FROM " + srr.getName());
                             buff.append(";\n");
                         }
                     }
                 }
                 // ensure proper sub/super role relationships exist 
                 new SubtreeVisitor() {
                     public void visit(SchemaRoleResource sr) {
                         SubtreeRoleResource rr = existingRoles.get(sr.getName());
                         // acutal subtree role exists
                         if(rr != null)
                         {
                             Role r = rr.getRole();
                             Role apr = null;
                             // traverse ancestor schema nodes
                             Resource psr = sr.getParent();
                             while(psr instanceof SchemaRoleResource)
                             {
                                 SubtreeRoleResource prr = existingRoles.get(psr.getName());
                                 // ancestor has actual subtree role
                                 if(existingRoles.containsKey(psr.getName()))
                                 {
                                     Role pr = prr.getRole();
                                     boolean found = false;
                                     for(RoleImplication ri : r.getImplications())
                                     {
                                         if(ri.getSuperRole().equals(pr) && ri.getSubRole().equals(r))
                                         {
                                             found = true;
                                         }
                                     }
                                     if(!found)
                                     {
                                         if(plan)
                                         {
                                             buff.append(site.getName()).append(";");
                                             buff.append(rClassName).append(";");
                                             buff.append(r.getName());
                                             buff.append("role+;");
                                             buff.append(location).append(";");
                                         }
                                         buff.append("ALTER ROLE '").append(pr.getName()).append("' ");
                                         buff.append("ADD SUBROLES ('").append(r.getName()).append("')");
                                         buff.append(";\n");
                                     }
                                     apr = pr;
                                     // break role chain traversal
                                     break;
                                 }
                                 psr = psr.getParent();
                             }
                             // remove any super roles that are sub tree roles for resource r except apr
                             for(RoleImplication ri : r.getImplications())
                             {
                                 if(ri.getSubRole().equals(r) && !ri.getSuperRole().equals(apr))
                                 {
                                     boolean found = false;
                                     for(SubtreeRoleResource srr : existingRoles.values())
                                     {
                                         if(srr.getRole().equals(ri.getSuperRole()))
                                         {
                                             found = true;
                                         }
                                     }
                                     if(found)
                                     {
                                         if(plan)
                                         {
                                             buff.append(site.getName()).append(";");
                                             buff.append(rClassName).append(";");
                                             buff.append(r.getName());
                                             buff.append("role-;");
                                             buff.append(location).append(";");
                                         }
                                         buff.append("ALTER ROLE '").append(
                                             ri.getSuperRole().getName()).append("' ");
                                         buff.append("DELETE SUBROLES ('").append(r.getName())
                                             .append("')");
                                         buff.append(";\n");
                                     }
                                 }
                             }
                         }
                     }
                 }.traverseDepthFirst(schemaRoleRoot);
             }
             return buff.toString();
         }
         catch(MalformedQueryException e)
         {
             throw new CmsSecurityException("failed to retrieve subtree roles", e);
         }
     }    
 }
