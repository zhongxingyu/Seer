 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.resources;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.UnsupportedRDFormatException;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.ansell.restletutils.RestletUtilRole;
 import com.github.ansell.restletutils.RestletUtilUser;
 import com.github.ansell.restletutils.SesameRealmConstants;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.restlet.PoddSesameRealm;
 import com.github.podd.restlet.PoddWebServiceApplication;
 import com.github.podd.restlet.RestletUtils;
 import com.github.podd.utils.PoddObjectLabel;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddRoles;
 import com.github.podd.utils.PoddUser;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * 
  * @author kutila
  */
 public class UserRolesResourceImpl extends AbstractPoddResourceImpl
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /**
      * Handle an HTTP POST request submitting RDF data to update (i.e. map/unmap) a PoddUser's
      * Roles. <br>
      * <br>
      * User authorization is checked for each Role to modify. The service proceeds to modify Role
      * mappings in the Realm only if the current user has sufficient privileges carry out ALL the
      * modifications.
      */
     @Post("rdf|rj|json|ttl")
     public Representation editUserRolesRdf(final Representation entity, final Variant variant) throws ResourceException
     {
         this.log.info("editUserRolesRdf");
         
         final User user = this.getRequest().getClientInfo().getUser();
         this.log.info("authenticated user: {}", user);
         
         final String userIdentifier =
                 (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
         this.log.info("editing Roles of user: {}", userIdentifier);
         
         final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
         
         // - validate User whose Roles are to be edited
         if(userIdentifier == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user to edit Roles");
         }
         final RestletUtilUser restletUserToUpdate = nextRealm.findUser(userIdentifier);
         if(restletUserToUpdate == null || !(restletUserToUpdate instanceof PoddUser))
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User not found");
         }
         final PoddUser poddUser = (PoddUser)restletUserToUpdate;
         
         // - retrieve 'delete' parameter
         boolean isDelete = false;
         final String deleteQueryParam = this.getQuery().getFirstValue(PoddWebConstants.KEY_DELETE);
         if(deleteQueryParam != null)
         {
             isDelete = Boolean.valueOf(deleteQueryParam);
         }
         this.log.info(" edit Roles is a 'delete' = {}", isDelete);
         
         Map<RestletUtilRole, Collection<URI>> rolesToEdit = null;
         
         // parse input content to a Model
         try (final InputStream inputStream = entity.getStream();)
         {
             final RDFFormat inputFormat =
                     Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
             final Model model = Rio.parse(inputStream, "", inputFormat);
             
            rolesToEdit = RestletUtils.extractRoleMappings(model);
         }
         catch(IOException | RDFParseException | UnsupportedRDFormatException e1)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse input");
         }
         
         if(rolesToEdit.isEmpty())
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "Did not specify any role edits in body of request");
         }
         
         // - check authorization for each Role mapping
         for(final RestletUtilRole role : rolesToEdit.keySet())
         {
             for(final URI mappedUri : rolesToEdit.get(role))
             {
                 PoddAction action = PoddAction.PROJECT_ROLE_EDIT;
                 if(PoddRoles.getRepositoryRoles().contains(role))
                 {
                     action = PoddAction.REPOSITORY_ROLE_EDIT;
                     if(mappedUri != null)
                     {
                         throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                                 "Unwanted optional Object URI found");
                     }
                 }
                 this.checkAuthentication(action, mappedUri);
             }
         }
         
         // - do the mapping/unmapping of Roles
         for(final RestletUtilRole role : rolesToEdit.keySet())
         {
             for(final URI mappedUri : rolesToEdit.get(role))
             {
                 if(isDelete)
                 {
                     nextRealm.unmap(poddUser, role.getRole(), mappedUri);
                     this.log.info(" User [{}] unmapped from Role [{}]", poddUser.getIdentifier(), role.getName());
                 }
                 else
                 {
                     nextRealm.map(poddUser, role.getRole(), mappedUri);
                     this.log.info(" User [{}] mapped to Role [{}], [{}]", poddUser.getIdentifier(), role.getName(),
                             mappedUri);
                 }
             }
         }
         
         // - prepare response
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
         final RDFFormat outputFormat =
                 Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
         try
         {
             Rio.write(Arrays.asList(PoddRdfConstants.VF.createStatement(poddUser.getUri(),
                     SesameRealmConstants.OAS_USERIDENTIFIER,
                     PoddRdfConstants.VF.createLiteral(poddUser.getIdentifier()))), output, outputFormat);
         }
         catch(final OpenRDFException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
         }
         
         return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
     }
     
     /**
      * Display the HTML page for User Role Management
      */
     @Get(":html")
     public Representation getRoleManagementPageHtml(final Variant variant) throws ResourceException
     {
         this.log.info("getRoleManagementHtml");
         
         final String requestedUserIdentifier =
                 (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
         this.log.info("requesting role management of user: {}", requestedUserIdentifier);
         
         if(requestedUserIdentifier == null)
         {
             // no identifier specified.
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user");
         }
         
         final User user = this.getRequest().getClientInfo().getUser();
         this.log.info("authenticated user: {}", user);
         
         // identify needed Action
         PoddAction action = PoddAction.OTHER_USER_EDIT;
         if(user != null && requestedUserIdentifier.equals(user.getIdentifier()))
         {
             action = PoddAction.CURRENT_USER_EDIT;
         }
         
         this.checkAuthentication(action);
         // completed checking authorization
         
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
         dataModel.put("contentTemplate", "editUserRoles.html.ftl");
         dataModel.put("pageTitle", "User Role Management");
         dataModel.put("authenticatedUserIdentifier", user.getIdentifier());
         
         final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
         final PoddUser poddUser = (PoddUser)realm.findUser(requestedUserIdentifier);
         
         if(poddUser == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
         }
         else
         {
             dataModel.put("requestedUser", poddUser);
             
             // - include all available PoddRoles
             dataModel.put("repositoryRolesList", PoddRoles.getRepositoryRoles());
             
             // - include user's current Roles and optional mapped objects
             final List<Entry<RestletUtilRole, PoddObjectLabel>> roleList =
                     RestletUtils.getUsersRoles(realm, poddUser, this.getPoddArtifactManager());
             
             dataModel.put("userRoleList", roleList);
         }
         
         // Output the base template, with contentTemplate from the dataModel defining the
         // template to use for the content in the body of the page
         return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                 MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
     }
     
     /**
      * Display the HTML page for User Role Management
      */
     @Get(":rdf|rj|json|ttl")
     public Representation getRoleManagementRdf(final Variant variant) throws ResourceException
     {
         this.log.info("getRoleManagementHtml");
         
         final String requestedUserIdentifier =
                 (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
         this.log.info("requesting role management of user: {}", requestedUserIdentifier);
         
         if(requestedUserIdentifier == null)
         {
             // no identifier specified.
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user");
         }
         
         final User user = this.getRequest().getClientInfo().getUser();
         this.log.info("authenticated user: {}", user);
         
         // identify needed Action
         PoddAction action = PoddAction.OTHER_USER_EDIT;
         if(user != null && requestedUserIdentifier.equals(user.getIdentifier()))
         {
             action = PoddAction.CURRENT_USER_EDIT;
         }
         
         this.checkAuthentication(action);
         // completed checking authorization
         
         final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
         final PoddUser poddUser = (PoddUser)realm.findUser(requestedUserIdentifier);
         
         if(poddUser == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
         }
         else
         {
             // - include user's current Roles and optional mapped objects
             final Map<RestletUtilRole, Collection<URI>> mappings = RestletUtils.getUsersRoles(realm, poddUser);
             final Model results = new LinkedHashModel();
             
            RestletUtils.dumpRoleMappings(mappings, results);
             
             // - prepare response
             final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
             final RDFFormat outputFormat =
                     Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
             try
             {
                 Rio.write(results, output, outputFormat);
             }
             catch(final OpenRDFException e)
             {
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
             }
             
             return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat
                     .getDefaultMIMEType()));
             
         }
     }
     
 }
