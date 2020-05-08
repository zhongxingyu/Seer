 /**
  * 
  */
 package com.github.ansell.restletutils;
 
 /**
  * Copyright 2005-2012 Restlet S.A.S.
  * 
  * The contents of this file are subject to the terms of one of the following open source licenses:
  * Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the "Licenses"). You can select the
  * license that you prefer but you may not use this file except in compliance with one of these
  * Licenses.
  * 
  * You can obtain a copy of the Apache 2.0 license at http://www.opensource.org/licenses/apache-2.0
  * 
  * You can obtain a copy of the LGPL 3.0 license at http://www.opensource.org/licenses/lgpl-3.0
  * 
  * You can obtain a copy of the LGPL 2.1 license at http://www.opensource.org/licenses/lgpl-2.1
  * 
  * You can obtain a copy of the CDDL 1.0 license at http://www.opensource.org/licenses/cddl1
  * 
  * You can obtain a copy of the EPL 1.0 license at http://www.opensource.org/licenses/eclipse-1.0
  * 
  * See the Licenses for the specific language governing permissions and limitations under the
  * Licenses.
  * 
  * Alternatively, you can obtain a royalty free commercial license with less limitations,
  * transferable or non-transferable, directly at http://www.restlet.com/products/restlet-framework
  * 
  * Restlet is a registered trademark of Restlet S.A.S.
  */
 
 import info.aduna.iteration.Iterations;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.openrdf.OpenRDFUtil;
 import org.openrdf.model.Literal;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.Dataset;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.rio.ntriples.NTriplesUtil;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.data.ClientInfo;
 import org.restlet.data.Status;
 import org.restlet.engine.security.RoleMapping;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.Enroler;
 import org.restlet.security.Group;
 import org.restlet.security.LocalVerifier;
 import org.restlet.security.Realm;
 import org.restlet.security.Role;
 import org.restlet.security.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Security realm based on a memory model. The model is composed of root groups, users and mapping
  * to associated roles.
  * 
  * @author Jerome Louvel
  */
 
 /**
  * Patched for OAS to look for users in a Sesame Repository
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class RestletUtilSesameRealm extends Realm
 {
     
     /**
      * Enroler based on the default security model.
      */
     private class DefaultOasSesameRealmEnroler implements Enroler
     {
         
         @Override
         public void enrole(final ClientInfo clientInfo)
         {
             final RestletUtilUser user = RestletUtilSesameRealm.this.findUser(clientInfo.getUser().getIdentifier());
             
             if(user != null)
             {
                 // Find all the inherited groups of this user
                 final Set<Group> userGroups = RestletUtilSesameRealm.this.findGroups(user);
                 
                 // Add roles specific to this user
                 final Set<Role> userRoles = RestletUtilSesameRealm.this.findRoles(user);
                 
                 for(final Role role : userRoles)
                 {
                     clientInfo.getRoles().add(role);
                 }
                 
                 if(clientInfo.isAuthenticated()
                         && !clientInfo.getRoles().contains(RestletUtilRoles.AUTHENTICATED.getRole()))
                 {
                     clientInfo.getRoles().add(RestletUtilRoles.AUTHENTICATED.getRole());
                 }
                 
                 // Add roles common to group members
                 final Set<Role> groupRoles = RestletUtilSesameRealm.this.findRoles(userGroups);
                 
                 for(final Role role : groupRoles)
                 {
                     clientInfo.getRoles().add(role);
                 }
             }
         }
     }
     
     /**
      * Verifier based on the default security model. It looks up users in the mapped organizations.
      */
     private class DefaultOasSesameRealmVerifier extends LocalVerifier
     {
         @Override
         protected User createUser(final String identifier, final Request request, final Response response)
         {
             final RestletUtilUser checkUser = RestletUtilSesameRealm.this.findUser(identifier);
             
             if(checkUser == null)
             {
                 RestletUtilSesameRealm.this.log.error("Cannot create a user for the given identifier: {}", identifier);
                 throw new IllegalArgumentException("Cannot create a user for the given identifier");
             }
             
             final RestletUtilUser result =
                     new RestletUtilUser(identifier, (char[])null, checkUser.getFirstName(), checkUser.getLastName(),
                             checkUser.getEmail());
             
             return result;
         }
         
         @Override
         public char[] getLocalSecret(final String identifier)
         {
             char[] result = null;
             final User user = RestletUtilSesameRealm.this.findUser(identifier);
             
             if(user != null)
             {
                 result = user.getSecret();
             }
             
             return result;
         }
     }
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /**
      * The Sesame Repository to use to get access to user information.
      */
     private Repository repository;
     
     private URI[] userManagerContexts;
     
     private ValueFactory vf;
     
     /** The modifiable list of role mappings. */
     // private final List<RoleMapping> roleMappings;
     
     /** The currently cached list of root groups. */
     private volatile List<Group> cachedRootGroups;
     
     /** The modifiable list of users. */
     // private final List<User> users;
     
     /**
      * Constructor.
      */
     public RestletUtilSesameRealm(final Repository repository, final URI... contexts)
     {
         OpenRDFUtil.verifyContextNotNull(contexts);
         this.setRepository(repository);
         this.setContexts(contexts);
         this.setVerifier(new DefaultOasSesameRealmVerifier());
         this.setEnroler(new DefaultOasSesameRealmEnroler());
         // this.cachedRootGroups = new CopyOnWriteArrayList<Group>();
         // this.rootGroups = new CopyOnWriteArrayList<Group>();
         // this.roleMappings = new CopyOnWriteArrayList<RoleMapping>();
         // this.users = new CopyOnWriteArrayList<User>();
     }
     
     /**
      * Recursively adds groups where a given user is a member.
      * 
      * @param user
      *            The member user.
      * @param userGroups
      *            The set of user groups to update.
      * @param currentGroup
      *            The current group to inspect.
      * @param stack
      *            The stack of ancestor groups.
      * @param inheritOnly
      *            Indicates if only the ancestors groups that have their "inheritRoles" property
      *            enabled should be added.
      */
     private void addGroupsForUser(final RestletUtilUser user, final Set<Group> userGroups, final Group currentGroup,
             final Set<Group> stack, final boolean inheritOnly)
     {
         if((currentGroup != null) && !stack.contains(currentGroup))
         {
             stack.add(currentGroup);
             
             if(currentGroup.getMemberUsers().contains(user))
             {
                 userGroups.add(currentGroup);
                 
                 // Add the ancestor groups as well
                 boolean inherit = !inheritOnly || currentGroup.isInheritingRoles();
                 
                 if(inherit)
                 {
                     for(final Group group : stack)
                     {
                         userGroups.add(group);
                         inherit = !inheritOnly || group.isInheritingRoles();
                     }
                 }
             }
             
             for(final Group group : currentGroup.getMemberGroups())
             {
                 this.addGroupsForUser(user, userGroups, group, stack, inheritOnly);
             }
         }
     }
     
     private void addRoleMapping(final RoleMapping nextMapping) throws RepositoryException
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.repository.getConnection();
             conn.begin();
             
             final URI nextRoleMappingUUID = this.vf.createURI("urn:oas:rolemapping:", UUID.randomUUID().toString());
             
             conn.add(this.vf.createStatement(nextRoleMappingUUID, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING),
                     this.getContexts());
             
             conn.add(this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDROLE, this
                     .getRoleByName(nextMapping.getTarget().getName()).getURI()), this.getContexts());
             
             if(nextMapping.getSource() instanceof Group)
             {
                 conn.add(
                         this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDGROUP,
                                 this.vf.createLiteral(((Group)nextMapping.getSource()).getName())), this.getContexts());
             }
             else if(nextMapping.getSource() instanceof User)
             {
                 conn.add(
                         this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDUSER,
                                 this.vf.createLiteral(((User)nextMapping.getSource()).getIdentifier())),
                         this.getContexts());
             }
             else
             {
                 conn.rollback();
                 throw new RuntimeException("Could not map role for unknown source type: "
                         + nextMapping.getSource().getClass().getName());
             }
             
             conn.commit();
         }
         catch(final RepositoryException e)
         {
             this.log.error("Found exception while adding role mapping", e);
             if(conn != null)
             {
                 conn.rollback();
             }
         }
         finally
         {
             if(conn != null)
             {
                 try
                 {
                     conn.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
     }
     
     /**
      * Adds a fully populated root group to the underlying repository, including a statement
      * indicating that this group is a root group.
      * 
      * @param nextRootGroup
      */
     public void addRootGroup(final Group nextRootGroup)
     {
         this.getRootGroups().add(nextRootGroup);
         
         RepositoryConnection conn = null;
         try
         {
             conn = this.repository.getConnection();
             conn.begin();
             
             this.storeGroup(nextRootGroup, conn, true);
             
             conn.commit();
         }
         catch(final RepositoryException e)
         {
             this.log.error("Found exception while storing root group", e);
             if(conn != null)
             {
                 try
                 {
                     conn.rollback();
                 }
                 catch(final RepositoryException e1)
                 {
                     this.log.error("Found exception while trying to roll back connection", e1);
                 }
             }
         }
         finally
         {
             if(conn != null)
             {
                 try
                 {
                     conn.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
     }
     
     public URI addUser(final User nextUser)
     {
         URI nextUserUUID =
                 this.vf.createURI("urn:oas:user:", nextUser.getIdentifier() + ":" + UUID.randomUUID().toString());
         
         RepositoryConnection conn = null;
         try
         {
             conn = this.repository.getConnection();
             conn.begin();
             
             final List<Statement> userIdentifierStatements =
                     Iterations.asList(conn.getStatements(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                             this.vf.createLiteral(nextUser.getIdentifier()), true, this.getContexts()));
             
             // FIXME: Is it safe to overwrite old users like this...
             if(!userIdentifierStatements.isEmpty())
             {
                 for(final Statement nextUserIdentifierStatement : userIdentifierStatements)
                 {
                     if(nextUserIdentifierStatement.getSubject() instanceof URI)
                     {
                         // retrieve the user URI to persist it with the new statements
                         // does not matter if this is overwritten multiple times if there were
                         // multiple users with this identifier in the database
                         nextUserUUID = (URI)nextUserIdentifierStatement.getSubject();
                     }
                     
                     final List<Statement> currentUserStatements =
                             Iterations.asList(conn.getStatements(nextUserIdentifierStatement.getSubject(), null, null,
                                     true, this.getContexts()));
                     
                     // remove all of the previously known statements
                     conn.remove(currentUserStatements, this.getContexts());
                 }
             }
             
             conn.add(nextUserUUID, RDF.TYPE, SesameRealmConstants.OAS_USER, this.getContexts());
             
             conn.add(nextUserUUID, SesameRealmConstants.OAS_USERIDENTIFIER,
                     this.vf.createLiteral(nextUser.getIdentifier()), this.getContexts());
             
             conn.add(nextUserUUID, SesameRealmConstants.OAS_USERSECRET,
                     this.vf.createLiteral(new String(nextUser.getSecret())), this.getContexts());
             
             if(nextUser.getFirstName() != null)
             {
                 conn.add(nextUserUUID, SesameRealmConstants.OAS_USERFIRSTNAME,
                         this.vf.createLiteral(nextUser.getFirstName()), this.getContexts());
             }
             
             if(nextUser.getLastName() != null)
             {
                 conn.add(nextUserUUID, SesameRealmConstants.OAS_USERLASTNAME,
                         this.vf.createLiteral(nextUser.getLastName()), this.getContexts());
             }
             
             if(nextUser.getEmail() != null)
             {
                 conn.add(nextUserUUID, SesameRealmConstants.OAS_USEREMAIL, this.vf.createLiteral(nextUser.getEmail()),
                         this.getContexts());
             }
             
             conn.commit();
         }
         catch(final RepositoryException e)
         {
             this.log.error("Found repository exception while adding user", e);
             try
             {
                 conn.rollback();
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Found unexpected exception while rolling back repository connection after exception");
             }
         }
         finally
         {
             if(conn != null)
             {
                 try
                 {
                     conn.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found unexpected repository exception", e);
                 }
             }
         }
         
         return nextUserUUID;
     }
     
     /**
      * Builds a RestletUtilUser from the data retrieved in a SPARQL result.
      * 
      * @param userIdentifier
      *            The unique identifier of the User.
      * @param bindingSet
      *            Results of a single user from SPARQL.
      * @return A RestletUtilUser account.
      * 
      */
     protected RestletUtilUser buildRestletUserFromSparqlResult(final String userIdentifier, final BindingSet bindingSet)
     {
         log.info("result={}", bindingSet);
         
         String userEmail = bindingSet.getValue("userEmail").stringValue();
         char[] userSecret = bindingSet.getValue("userSecret").stringValue().toCharArray();
         String userFirstName = null;
         if(bindingSet.hasBinding("userFirstName"))
         {
             userFirstName = bindingSet.getValue("userFirstName").stringValue();
         }
         String userLastName = null;
         if(bindingSet.hasBinding("userLastName"))
         {
             userLastName = bindingSet.getValue("userLastName").stringValue();
         }
         
         return new RestletUtilUser(userIdentifier, userSecret, userFirstName, userLastName, userEmail);
     }
     
     /**
      * Builds a SPARQL query to retrieve details of a RestletUtilUser. This method could be
      * overridden to search for other information regarding a user.
      * 
      * @param userIdentifier
      *            The unique identifier of the User to search for.
      * @return A String representation of the SPARQL Select query
      */
     protected String buildSparqlQueryToFindUser(final String userIdentifier)
     {
         final StringBuilder query = new StringBuilder();
         
         query.append(" SELECT ?userUri ?userSecret ?userFirstName ?userLastName ?userEmail ");
         query.append(" WHERE ");
         query.append(" { ");
         query.append("   ?userUri a <" + SesameRealmConstants.OAS_USER + "> . ");
         query.append("   ?userUri <" + SesameRealmConstants.OAS_USERIDENTIFIER + "> ?userIdentifier . ");
         query.append("   ?userUri <" + SesameRealmConstants.OAS_USERSECRET + "> ?userSecret . ");
         query.append("   OPTIONAL{ ?userUri <" + SesameRealmConstants.OAS_USERFIRSTNAME + "> ?userFirstName . } ");
         query.append("   OPTIONAL{ ?userUri <" + SesameRealmConstants.OAS_USERLASTNAME + "> ?userLastName . } ");
         query.append("   OPTIONAL{ ?userUri <" + SesameRealmConstants.OAS_USEREMAIL + "> ?userEmail . } ");
         query.append("   FILTER(str(?userIdentifier) = \"" + NTriplesUtil.escapeString(userIdentifier) + "\") ");
         query.append(" } ");
         return query.toString();
     }
     
     private Group createGroupForStatements(final List<Statement> nextGroupStatements)
     {
         final Group nextGroup = new Group();
         
         for(final Statement nextStatement : nextGroupStatements)
         {
             if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPNAME))
             {
                 nextGroup.setName(nextStatement.getObject().stringValue());
             }
             else if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPDESCRIPTION))
             {
                 nextGroup.setDescription(nextStatement.getObject().stringValue());
             }
             else if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPINHERITINGROLES))
             {
                 nextGroup.setInheritingRoles(((Literal)nextStatement.getObject()).booleanValue());
             }
             else if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPMEMBERUSER))
             {
                 nextGroup.getMemberUsers().add(this.findUser(nextStatement.getObject().stringValue()));
             }
             else if(nextStatement.getPredicate().equals(RDF.TYPE)
                     && (nextStatement.getObject().equals(SesameRealmConstants.OAS_GROUP) || nextStatement.getObject()
                             .equals(SesameRealmConstants.OAS_ROOTGROUP)))
             {
                 this.log.trace("Found rdf type statement for group: {}", nextStatement);
             }
             else
             {
                 this.log.debug("Found unrecognised statement parsing group: {}", nextStatement);
             }
         }
         
         return nextGroup;
     }
     
     private Group createGroupHierarchy(final Group parentGroup, final RepositoryConnection conn, final URI nextGroupUri)
     {
         try
         {
             // get the statements for the nextGroupUri
             final List<Statement> nextRootGroupStatements =
                     Iterations.asList(conn.getStatements(nextGroupUri, null, null, true, this.getContexts()));
             // create the group
             final Group newGroup = this.createGroupForStatements(nextRootGroupStatements);
             
             if(parentGroup != null)
             {
                 // add the group as a member group for the parent group
                 parentGroup.getMemberGroups().add(newGroup);
             }
             
             // check if there are any member groups for this item
             if(conn.hasStatement(nextGroupUri, SesameRealmConstants.OAS_GROUPMEMBERGROUP, null, true,
                     this.getContexts()))
             {
                 final List<Statement> nextMemberGroupStatements =
                         Iterations.asList(conn.getStatements(nextGroupUri, SesameRealmConstants.OAS_GROUPMEMBERGROUP,
                                 null, true, this.getContexts()));
                 
                 for(final Statement nextMemberGroupStatement : nextMemberGroupStatements)
                 {
                     if(nextMemberGroupStatement.getObject() instanceof URI)
                     {
                         // FIXME: Need to do cycle checking here to avoid infinite loops
                         
                         // recursively call addGroup to add children to newGroup
                         this.createGroupHierarchy(newGroup, conn, (URI)nextMemberGroupStatement.getObject());
                     }
                     else
                     {
                         this.log.error("Found member group reference that was not a URI: {}", nextMemberGroupStatement);
                     }
                 }
             }
             
             return newGroup;
         }
         catch(final RepositoryException e)
         {
             this.log.error("Found error trying to examine member groups", e);
             
             throw new RuntimeException(e);
         }
         
     }
     
     public URI deleteUser(final User nextUser)
     {
         URI nextUserUUID = null;
         
         final RestletUtilUser findUser = this.findUser(nextUser.getIdentifier());
         
         if(findUser == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No such user found");
         }
         
         RepositoryConnection conn = null;
         try
         {
             conn = this.repository.getConnection();
             conn.begin();
             
             final List<Statement> userIdentifierStatements =
                     Iterations.asList(conn.getStatements(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                             this.vf.createLiteral(nextUser.getIdentifier()), true, this.getContexts()));
             
             if(!userIdentifierStatements.isEmpty())
             {
                 for(final Statement nextUserIdentifierStatement : userIdentifierStatements)
                 {
                     if(nextUserIdentifierStatement.getSubject() instanceof URI)
                     {
                         // retrieve the user URI to persist it with the new statements
                         // does not matter if this is overwritten multiple times if there were
                         // multiple users with this identifier in the database
                         nextUserUUID = (URI)nextUserIdentifierStatement.getSubject();
                     }
                     
                     final List<Statement> currentUserStatements =
                             Iterations.asList(conn.getStatements(nextUserIdentifierStatement.getSubject(), null, null,
                                     true, this.getContexts()));
                     
                     // remove all of the previously known statements
                     conn.remove(currentUserStatements, this.getContexts());
                 }
             }
             
             conn.commit();
         }
         catch(final RepositoryException e)
         {
             this.log.error("Found repository exception while adding user", e);
             try
             {
                 conn.rollback();
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Found unexpected exception while rolling back repository connection after exception");
             }
         }
         finally
         {
             if(conn != null)
             {
                 try
                 {
                     conn.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found unexpected repository exception", e);
                 }
             }
         }
         
         return nextUserUUID;
     }
     
     /**
      * Finds the set of groups where a given user is a member. Note that inheritable ancestors
      * groups are also returned.
      * 
      * @param user
      *            The member user.
      * @return The set of groups.
      */
     public Set<Group> findGroups(final RestletUtilUser user)
     {
         return this.findGroups(user, true);
     }
     
     /**
      * Finds the set of groups where a given user is a member.
      * 
      * @param user
      *            The member user.
      * @param inheritOnly
      *            Indicates if only the ancestors groups that have their "inheritRoles" property
      *            enabled should be added.
      * @return The set of groups.
      */
     public Set<Group> findGroups(final RestletUtilUser user, final boolean inheritOnly)
     {
         final Set<Group> result = new HashSet<Group>();
         Set<Group> stack;
         
         // Recursively find user groups
         for(final Group group : this.getRootGroups())
         {
             stack = new LinkedHashSet<Group>();
             this.addGroupsForUser(user, result, group, stack, inheritOnly);
         }
         
         return result;
     }
     
     /**
      * Finds the roles mapped to given user group.
      * 
      * @param userGroup
      *            The user group.
      * @return The roles found.
      */
     public Set<Role> findRoles(final Group userGroup)
     {
         final Set<Role> result = new HashSet<Role>();
         
         for(final RoleMapping mapping : this.getRoleMappings())
         {
             final Object source = mapping.getSource();
             
             if((userGroup != null) && userGroup.equals(source))
             {
                 result.add(mapping.getTarget());
             }
         }
         
         return result;
     }
     
     /**
      * Finds the roles mapped to given user groups.
      * 
      * @param userGroups
      *            The user groups.
      * @return The roles found.
      */
     public Set<Role> findRoles(final Set<Group> userGroups)
     {
         final Set<Role> result = new HashSet<Role>();
         
         for(final RoleMapping mapping : this.getRoleMappings())
         {
             final Object source = mapping.getSource();
             
             if((userGroups != null) && userGroups.contains(source))
             {
                 result.add(mapping.getTarget());
             }
         }
         
         return result;
     }
     
     /**
      * Finds the roles mapped to a given user.
      * 
      * @param user
      *            The user.
      * @return The roles found.
      */
     public Set<Role> findRoles(final User user)
     {
         final Set<Role> result = new HashSet<Role>();
         
         for(final RoleMapping mapping : this.getRoleMappings())
         {
             final Object source = mapping.getSource();
             
             if((user != null) && user.equals(source))
             {
                 // TODO: Fix this hardcoding when Restlet implements equals for Role objects again
                RestletUtilRole standardRole = RestletUtilRoles.getRoleByName(mapping.getTarget().getName());
                 if(standardRole != null)
                 {
                     result.add(standardRole.getRole());
                 }
                 else
                 {
                     result.add(mapping.getTarget());
                 }
             }
         }
         
         return result;
     }
     
     /**
      * Finds a user in the organization based on its identifier.
      * 
      * @param userIdentifier
      *            The identifier to match.
      * @return The matched user or null.
      */
     public RestletUtilUser findUser(final String userIdentifier)
     {
         if(userIdentifier == null)
         {
             throw new NullPointerException("User identifier was null");
         }
         
         RestletUtilUser result = null;
         
         RepositoryConnection conn = null;
         try
         {
             conn = this.repository.getConnection();
             
             final String query = this.buildSparqlQueryToFindUser(userIdentifier);
             
             if(this.log.isDebugEnabled())
             {
                 this.log.debug("findUser: query={}", query);
             }
             
             final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
             
             final TupleQueryResult queryResult = tupleQuery.evaluate();
             
             try
             {
                 if(queryResult.hasNext())
                 {
                     final BindingSet bindingSet = queryResult.next();
                     
                     result = this.buildRestletUserFromSparqlResult(userIdentifier, bindingSet);
                 }
                 else
                 {
                     this.log.info("Could not find user with identifier: {}", userIdentifier);
                 }
             }
             finally
             {
                 queryResult.close();
             }
             
         }
         catch(final RepositoryException e)
         {
             throw new RuntimeException("Failure finding user in repository", e);
         }
         catch(final MalformedQueryException e)
         {
             throw new RuntimeException("Failure finding user in repository", e);
         }
         catch(final QueryEvaluationException e)
         {
             throw new RuntimeException("Failure finding user in repository", e);
         }
         finally
         {
             try
             {
                 conn.close();
             }
             catch(final RepositoryException e)
             {
                 this.log.error("Failure to close connection", e);
             }
         }
         
         return result;
     }
     
     public URI[] getContexts()
     {
         return this.userManagerContexts;
     }
     
     public Repository getRepository()
     {
         return this.repository;
     }
     
     /**
      * Returns the modifiable list of role mappings.
      * 
      * @return The modifiable list of role mappings.
      */
     private List<RoleMapping> getRoleMappings()
     {
         final List<RoleMapping> results = new ArrayList<RoleMapping>();
         
         RepositoryConnection conn = null;
         try
         {
             conn = this.repository.getConnection();
             
             final RepositoryResult<Statement> typeStatements =
                     conn.getStatements(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING, true, this.getContexts());
             
             try
             {
                 // We iterate through this gradually to reduce the load as the size of this
                 // collection will grow with users
                 while(typeStatements.hasNext())
                 {
                     final Statement next = typeStatements.next();
                     
                     if(next.getSubject() instanceof URI)
                     {
                         final URI nextRoleMappingUri = (URI)next.getSubject();
                         
                         final RoleMapping nextRoleMapping = new RoleMapping();
                         
                         // dump all of these statements into a list as the size will be relatively
                         // constant and small for all scenarios
                         final List<Statement> nextRoleMappingStatements =
                                 Iterations.asList(conn.getStatements(nextRoleMappingUri, null, null, true,
                                         this.getContexts()));
                         
                         for(final Statement nextRoleMappingStatement : nextRoleMappingStatements)
                         {
                             if(nextRoleMappingStatement.getPredicate().equals(SesameRealmConstants.OAS_ROLEMAPPEDROLE))
                             {
                                 if(nextRoleMappingStatement.getObject() instanceof URI)
                                 {
                                     // XXX: When Restlet allows custom .equals for Role, switch to
                                     // avoid only using
                                     // StandardOASRoles here, until then we have no easy way of
                                     // matching roles out of the
                                     // repository to objects
                                     
                                     final RestletUtilRole nextStandardRole =
                                             this.getRoleByUri((URI)nextRoleMappingStatement.getObject());
                                     
                                     if(nextStandardRole == null)
                                     {
                                         this.log.warn(
                                                 "Failed to find an in-memory role for the given role mapped role: {}",
                                                 nextRoleMappingStatement);
                                     }
                                     else
                                     {
                                         nextRoleMapping.setTarget(nextStandardRole.getRole());
                                     }
                                 }
                                 else
                                 {
                                     this.log.warn("Found a non-URI as the target for a role mapped role statement: {}",
                                             nextRoleMappingStatement);
                                 }
                             }
                             else if(nextRoleMappingStatement.getPredicate().equals(
                                     SesameRealmConstants.OAS_ROLEMAPPEDGROUP))
                             {
                                 if(nextRoleMappingStatement.getObject() instanceof Literal)
                                 {
                                     final String nextGroupName =
                                             ((Literal)nextRoleMappingStatement.getObject()).stringValue();
                                     
                                     // TODO: Support nested groups here
                                     
                                     final List<Group> rootGroups = this.getRootGroups();
                                     
                                     for(final Group nextRootGroup : rootGroups)
                                     {
                                         if(nextRootGroup.getName().equals(nextGroupName))
                                         {
                                             nextRoleMapping.setSource(nextRootGroup);
                                         }
                                         else
                                         {
                                             // TODO: need to check further for nested groups
                                         }
                                     }
                                 }
                                 else
                                 {
                                     this.log.warn(
                                             "Found a non-Literal as the target for a role mapped group statement: {}",
                                             nextRoleMappingStatement);
                                 }
                             }
                             else if(nextRoleMappingStatement.getPredicate().equals(
                                     SesameRealmConstants.OAS_ROLEMAPPEDUSER))
                             {
                                 if(nextRoleMappingStatement.getObject() instanceof Literal)
                                 {
                                     final String nextUserIdentifier =
                                             ((Literal)nextRoleMappingStatement.getObject()).stringValue();
                                     
                                     final RestletUtilUser nextUser = this.findUser(nextUserIdentifier);
                                     
                                     if(nextUser != null)
                                     {
                                         nextRoleMapping.setSource(nextUser);
                                     }
                                     else
                                     {
                                         this.log.warn(
                                                 "Failed to find a role mapped user internally for the given user identifier: {}",
                                                 nextRoleMappingStatement);
                                     }
                                 }
                                 else
                                 {
                                     this.log.warn(
                                             "Found a non-Literal as the target for a role mapped group statement: {}",
                                             nextRoleMappingStatement);
                                 }
                             }
                             else if(nextRoleMappingStatement.getPredicate().equals(RDF.TYPE))
                             {
                                 this.log.trace("Found rdf:type statement for role mapping: {}",
                                         nextRoleMappingStatement);
                             }
                             else
                             {
                                 this.log.debug("Found unknown statement for role mapping: {}", nextRoleMappingStatement);
                             }
                         }
                         
                         // verify that the source and target were both setup before adding this
                         // mapping to results
                         if(nextRoleMapping.getSource() != null && nextRoleMapping.getTarget() != null)
                         {
                             results.add(nextRoleMapping);
                         }
                         else
                         {
                             this.log.error("Not adding incomplete role mapping to results: uri={}, partialMapping={}",
                                     nextRoleMappingUri, nextRoleMapping);
                         }
                     }
                     else
                     {
                         this.log.warn("Found non-URI for role mapping, ignoring this role mapping: {}", next);
                     }
                 }
             }
             finally
             {
                 typeStatements.close();
             }
         }
         catch(final RepositoryException e)
         {
             this.log.error("Found exception while retrieving role mappings", e);
         }
         finally
         {
             if(conn != null)
             {
                 try
                 {
                     conn.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
         
         return results;
     }
     
     /**
      * @param role
      * @return
      */
     protected RestletUtilRole getRoleByName(final String name)
     {
         final RestletUtilRole oasRole = RestletUtilRoles.getRoleByName(name);
         return oasRole;
     }
     
     /**
      * @param nextRoleMappingStatement
      * @return
      */
     protected RestletUtilRole getRoleByUri(final URI uri)
     {
         final RestletUtilRole nextStandardRole = RestletUtilRoles.getRoleByUri(uri);
         return nextStandardRole;
     }
     
     /**
      * Returns the modifiable list of root groups.
      * 
      * @return The modifiable list of root groups.
      */
     public List<Group> getRootGroups()
     {
         List<Group> results = this.cachedRootGroups;
         
         if(results == null)
         {
             synchronized(this)
             {
                 results = this.cachedRootGroups;
                 if(results == null)
                 {
                     results = new ArrayList<Group>();
                     
                     RepositoryConnection conn = null;
                     
                     try
                     {
                         conn = this.getRepository().getConnection();
                         
                         final RepositoryResult<Statement> rootGroupStatements =
                                 conn.getStatements(null, RDF.TYPE, SesameRealmConstants.OAS_ROOTGROUP, true,
                                         this.getContexts());
                         
                         try
                         {
                             while(rootGroupStatements.hasNext())
                             {
                                 final Statement nextRootGroupStatement = rootGroupStatements.next();
                                 
                                 if(nextRootGroupStatement.getSubject() instanceof URI)
                                 {
                                     final URI nextRootGroupUri = (URI)nextRootGroupStatement.getSubject();
                                     // add the group recursively to enable member groups to be added
                                     // recursively
                                     results.add(this.createGroupHierarchy(null, conn, nextRootGroupUri));
                                 }
                                 else
                                 {
                                     this.log.warn("Not including root group as it did not have a URI identifier: {}",
                                             nextRootGroupStatement);
                                 }
                             }
                         }
                         finally
                         {
                             rootGroupStatements.close();
                         }
                     }
                     catch(final RepositoryException e)
                     {
                         this.log.error("Found exception while trying to get root groups", e);
                     }
                     finally
                     {
                         try
                         {
                             if(conn != null)
                             {
                                 conn.close();
                             }
                         }
                         catch(final RepositoryException e)
                         {
                             this.log.error("Found unexpected exception while closing repository connection", e);
                         }
                     }
                     
                     this.cachedRootGroups = results;
                 }
             }
         }
         
         return results;
         
         // throw new RuntimeException(
         // "TODO: Implement code not to rely on getting a complete list of groups where possible");
         // return this.rootGroups;
     }
     
     private Dataset getSesameDataset()
     {
         final DatasetImpl result = new DatasetImpl();
         
         result.setDefaultInsertGraph(this.getContexts()[0]);
         
         for(final URI nextContext : this.getContexts())
         {
             result.addDefaultGraph(nextContext);
             result.addDefaultRemoveGraph(nextContext);
             result.addNamedGraph(nextContext);
         }
         
         return result;
     }
     
     /**
      * Returns the modifiable list of users.
      * 
      * @return The modifiable list of users.
      */
     private List<RestletUtilUser> getUsers()
     {
         throw new RuntimeException("TODO: Implement code not to rely on ever getting a complete list of users");
         // return this.users;
     }
     
     /**
      * Maps a group defined in a component to a role defined in the application.
      * 
      * @param group
      *            The source group.
      * @param role
      *            The target role.
      */
     public void map(final Group group, final Role role)
     {
         try
         {
             this.addRoleMapping(new RoleMapping(group, role));
         }
         catch(final RepositoryException e)
         {
             throw new RuntimeException("Found unexpected exception while adding role mapping", e);
         }
     }
     
     /**
      * Maps a user defined in a component to a role defined in the application.
      * 
      * @param user
      *            The source user.
      * @param role
      *            The target role.
      */
     public void map(final RestletUtilUser user, final Role role)
     {
         try
         {
             this.addRoleMapping(new RoleMapping(user, role));
         }
         catch(final RepositoryException e)
         {
             throw new RuntimeException("Found unexpected exception while adding role mapping", e);
         }
     }
     
     public void setContexts(final URI... contexts)
     {
         if(contexts.length == 0)
         {
             // for security and usability we insist that a named graph is provided
             throw new IllegalArgumentException(
                     "Cannot create an OasSesameRealm without specifying the contexts that are used to manage user data.");
         }
         this.userManagerContexts = contexts;
     }
     
     public void setRepository(final Repository repository)
     {
         this.repository = repository;
         if(this.repository != null)
         {
             this.vf = this.repository.getValueFactory();
         }
         else
         {
             this.vf = null;
         }
     }
     
     /**
      * Sets the modifiable list of root groups. This method clears the current list and adds all
      * entries in the parameter list.
      * 
      * @param rootGroups
      *            A list of root groups.
      */
     @Deprecated
     public void setRootGroups(final List<Group> rootGroups)
     {
         throw new RuntimeException("TODO: Implement me if necessary, or convert to add and remove methods");
         // synchronized(this.getRootGroups())
         // {
         // if(rootGroups != this.getRootGroups())
         // {
         // this.getRootGroups().clear();
         //
         // if(rootGroups != null)
         // {
         // this.getRootGroups().addAll(rootGroups);
         // }
         // }
         // }
     }
     
     /**
      * Sets the modifiable list of users. This method clears the current list and adds all entries
      * in the parameter list.
      * 
      * @param users
      *            A list of users.
      */
     @Deprecated
     private void setUsers(final List<RestletUtilUser> users)
     {
         throw new RuntimeException(
                 "TODO: Convert all calls to this method to add and remove methods, with optionally a reset/clear method");
         // synchronized(this.getUsers())
         // {
         // if(users != this.getUsers())
         // {
         // this.getUsers().clear();
         //
         // if(users != null)
         // {
         // this.getUsers().addAll(users);
         // }
         // }
         // }
     }
     
     /**
      * Stores the group, including a root group statement if rootGroup is true.
      * 
      * @param nextGroup
      * @param isRootGroup
      * @throws RepositoryException
      */
     private void storeGroup(final Group nextGroup, final RepositoryConnection conn, final boolean isRootGroup)
         throws RepositoryException
     {
         if(conn.hasStatement(null, SesameRealmConstants.OAS_GROUPNAME, this.vf.createLiteral(nextGroup.getName()),
                 true, this.getContexts()))
         {
             // TODO: Create an update method
             throw new RuntimeException(
                     "A user with the given identifier already exists. Cannot add a new user with that identifier.");
         }
         
         final URI nextGroupUUID =
                 this.vf.createURI("urn:oas:group:", nextGroup.getName() + ":" + UUID.randomUUID().toString());
         
         conn.add(this.vf.createStatement(nextGroupUUID, RDF.TYPE, SesameRealmConstants.OAS_GROUP), this.getContexts());
         
         if(isRootGroup)
         {
             conn.add(this.vf.createStatement(nextGroupUUID, RDF.TYPE, SesameRealmConstants.OAS_ROOTGROUP),
                     this.getContexts());
         }
         
         conn.add(
                 this.vf.createStatement(nextGroupUUID, SesameRealmConstants.OAS_GROUPNAME,
                         this.vf.createLiteral(nextGroup.getName())), this.getContexts());
         conn.add(
                 this.vf.createStatement(nextGroupUUID, SesameRealmConstants.OAS_GROUPDESCRIPTION,
                         this.vf.createLiteral(nextGroup.getDescription())), this.getContexts());
         conn.add(
                 this.vf.createStatement(nextGroupUUID, SesameRealmConstants.OAS_GROUPINHERITINGROLES,
                         this.vf.createLiteral(nextGroup.isInheritingRoles())), this.getContexts());
         
         // only store users who cannot be found based on their identifier
         for(final User nextUser : nextGroup.getMemberUsers())
         {
             if(this.findUser(nextUser.getIdentifier()) == null)
             {
                 final URI nextUserUri = this.addUser(nextUser);
             }
         }
         
         if(!nextGroup.getMemberGroups().isEmpty())
         {
             for(final Group nextMemberGroup : nextGroup.getMemberGroups())
             {
                 // always set rootGroup parameter to false when recursing into member groups
                 this.storeGroup(nextMemberGroup, conn, false);
             }
         }
         
     }
     
     /**
      * Unmaps a group defined in a component from a role defined in the application.
      * 
      * @param group
      *            The source group.
      * @param role
      *            The target role.
      */
     public void unmap(final Group group, final Role role)
     {
         this.unmap(role, SesameRealmConstants.OAS_ROLEMAPPEDGROUP, group.getName());
     }
     
     /**
      * Unmaps a user defined in a component from a role defined in the application.
      * 
      * @param user
      *            The source user.
      * @param role
      *            The target role.
      */
     public void unmap(final RestletUtilUser user, final Role role)
     {
         this.unmap(role, SesameRealmConstants.OAS_ROLEMAPPEDUSER, user.getIdentifier());
     }
     
     public void unmap(final Role role, final URI mappingUri, final String identifier)
     {
         RepositoryConnection conn = null;
         try
         {
             conn = this.repository.getConnection();
             conn.begin();
             final StringBuilder query = new StringBuilder();
             
             final RestletUtilRole oasRole = this.getRoleByName(role.getName());
             
             if(oasRole == null)
             {
                 throw new IllegalArgumentException("Did not recognise role as a standard OAS role" + role.getName());
             }
             
             query.append(" SELECT ?roleMappingUri ");
             query.append(" WHERE ");
             query.append(" { ");
             query.append("   ?roleMappingUri a <" + SesameRealmConstants.OAS_ROLEMAPPING + "> . ");
             query.append("   ?roleMappingUri <" + mappingUri + "> ?identifier . ");
             query.append("   ?roleMappingUri <" + SesameRealmConstants.OAS_ROLEMAPPEDROLE + "> ?roleUri . ");
             query.append("   FILTER(str(?identifier) = \"" + NTriplesUtil.escapeString(identifier) + "\") ");
             query.append("   FILTER(?roleUri = <" + oasRole.getURI() + "> ) ");
             query.append(" } ");
             
             if(this.log.isDebugEnabled())
             {
                 this.log.debug("findUser: query={}", query.toString());
             }
             
             final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
             tupleQuery.setDataset(this.getSesameDataset());
             
             final TupleQueryResult queryResult = tupleQuery.evaluate();
             
             try
             {
                 if(!queryResult.hasNext())
                 {
                     this.log.info("Could not find any role mappings to remove for this role: {} and this target: {}",
                             role, identifier);
                 }
                 
                 while(queryResult.hasNext())
                 {
                     final BindingSet bindingSet = queryResult.next();
                     
                     if(queryResult.hasNext())
                     {
                         this.log.warn(
                                 "Found duplicate roleMapping, will remove all mappings for this role: {} and this target: {}",
                                 role, identifier);
                     }
                     
                     final Value roleMappingUri = bindingSet.getValue("roleMappingUri");
                     
                     if(roleMappingUri instanceof Resource)
                     {
                         conn.remove((Resource)roleMappingUri, null, null, this.getContexts());
                     }
                     else
                     {
                         this.log.warn("This should not happen while RDF only allows URIs and blank nodes in the subject position of triples");
                     }
                 }
             }
             finally
             {
                 queryResult.close();
             }
             conn.commit();
         }
         catch(final RepositoryException e)
         {
             try
             {
                 conn.rollback();
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Repository Exception while rolling back connection");
             }
             throw new RuntimeException("Failure finding user in repository", e);
         }
         catch(final MalformedQueryException e)
         {
             try
             {
                 conn.rollback();
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Repository Exception while rolling back connection");
             }
             throw new RuntimeException("Failure finding user in repository", e);
         }
         catch(final QueryEvaluationException e)
         {
             try
             {
                 conn.rollback();
             }
             catch(final RepositoryException e1)
             {
                 this.log.error("Repository Exception while rolling back connection");
             }
             throw new RuntimeException("Failure finding user in repository", e);
         }
         finally
         {
             try
             {
                 conn.close();
             }
             catch(final RepositoryException e)
             {
                 this.log.error("Failure to close connection", e);
             }
         }
         
     }
     
 }
