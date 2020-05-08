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
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.ByteArrayRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Get;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.User;
 import org.semanticweb.owlapi.model.IRI;
 
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedArtifactVersionException;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.restlet.RestletUtils;
 import com.github.podd.utils.FreemarkerUtil;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddObjectLabel;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * 
  * Get an artifact from PODD. This resource handles requests for asserted statements as well as
  * inferred statements.
  * 
  * @author kutila
  * 
  */
 public class GetArtifactResourceImpl extends AbstractPoddResourceImpl
 {
     
     @Get(":html")
     public Representation getArtifactHtml(final Representation entity) throws ResourceException
     {
         this.log.debug("getArtifactHtml");
         
         final String artifactString = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
         
         if(artifactString == null)
         {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
         }
         
         final String versionString =
                 this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
         
         // optional parameter for inner objects
         final String objectToView = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER, true);
         
         this.log.debug("requesting get artifact (HTML): {}, {}, {}", artifactString, versionString, objectToView);
         
         final UnmanagedArtifactIRIException foundException = null;
         
         InferredOWLOntologyID ontologyID = null;
         try
         {
             if(versionString == null)
             {
                 ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactString));
             }
             else
             {
                 ontologyID =
                         this.getPoddArtifactManager()
                                 .getArtifact(IRI.create(artifactString), IRI.create(versionString));
                 
                 if(ontologyID == null)
                 {
                     ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactString));
                 }
             }
         }
         catch(final UnmanagedArtifactIRIException | UnmanagedArtifactVersionException e)
         {
             if(this.getRequest().getClientInfo().isAuthenticated())
             {
                 throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact",
                         foundException);
             }
             else
             {
                 // Make them authenticate first so that only authenticated users see 404 messages
                 this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, null, true);
                 // Should never hit here, but putting it here to avoid possible NPEs further on
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Something went wrong");
             }
         }
         
         // FIXME: Test this after publish artifact is implemented
         boolean isPublished = false;
         try
         {
             if(ontologyID != null)
             {
                 isPublished = this.getPoddArtifactManager().isPublished(ontologyID);
             }
         }
         catch(final OpenRDFException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Repository exception", e);
         }
         
         if(isPublished)
         {
             this.checkAuthentication(PoddAction.PUBLISHED_ARTIFACT_READ, ontologyID.getOntologyIRI().toOpenRDFURI());
         }
         else
         {
             this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, ontologyID.getOntologyIRI().toOpenRDFURI());
         }
         
         // completed checking authorization
         
         final User user = this.getRequest().getClientInfo().getUser();
         this.log.debug("authenticated user: {}", user);
         
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
         dataModel.put("contentTemplate", "objectDetails.html.ftl");
         dataModel.put("pageTitle", "View Artifact");
         
         try
         {
             this.populateDataModelWithArtifactData(ontologyID, objectToView, dataModel, isPublished);
         }
         catch(final OpenRDFException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model", e);
         }
         
         return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                 MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
     }
     
     @Get(":rdf|rj|json|ttl")
     public Representation getArtifactRdf(final Representation entity, final Variant variant) throws ResourceException
     {
         // FIXME: Some Firefox requests get sent with Accept: */*, and Restlet chooses this method
         // instead of HTML as default
         // To fix this we need to check whether Accept is exactly "*/*" and response with HTML
         // variant instead of an RDF variant
         
         this.log.debug("getArtifactRdf");
         
         final ByteArrayOutputStream stream = new ByteArrayOutputStream();
         
         try
         {
             final String artifactString = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
             
             if(artifactString == null)
             {
                 this.log.error("Artifact ID not submitted");
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
             }
             
             final String versionString =
                     this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
             
             this.log.debug("requesting get artifact ({}): {}", variant.getMediaType().getName(), artifactString);
             
             // FIXME: The artifact may be published here
             this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
                     PoddRdfConstants.VF.createURI(artifactString));
             // completed checking authorization
             
             final User user = this.getRequest().getClientInfo().getUser();
             this.log.debug("authenticated user: {}", user);
             
             InferredOWLOntologyID ontologyID = null;
             if(versionString == null)
             {
                 ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactString));
             }
             else
             {
                 ontologyID =
                         this.getPoddArtifactManager()
                                 .getArtifact(IRI.create(artifactString), IRI.create(versionString));
             }
             
             final String includeInferredString =
                     this.getRequest().getResourceRef().getQueryAsForm()
                             .getFirstValue(PoddWebConstants.KEY_INCLUDE_INFERRED, true);
             final boolean includeInferred = Boolean.valueOf(includeInferredString);
             
             this.getPoddApplication()
                     .getPoddArtifactManager()
                     .exportArtifact(ontologyID, stream,
                             RDFFormat.forMIMEType(variant.getMediaType().getName(), RDFFormat.RDFJSON), includeInferred);
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
         }
         catch(OpenRDFException | PoddException | IOException e)
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to export artifact", e);
         }
         
         return new ByteArrayRepresentation(stream.toByteArray());
     }
     
     /**
      * This method retrieves necessary info about the object being viewed via SPARQL queries and
      * populates the data model.
      * 
      * @param ontologyID
      *            The artifact to be viewed
      * @param objectToView
      *            An optional internal object to view
      * @param ontologyGraphs
      *            The schema ontology graphs that should be part of the context for SPARQL
      * @param dataModel
      *            Freemarker data model to be populated
      * @param isPublished
      *            True if the Project is Published
      * @throws OpenRDFException
      */
     private void populateDataModelWithArtifactData(final InferredOWLOntologyID ontologyID, final String objectToView,
             final Map<String, Object> dataModel, final boolean isPublished) throws OpenRDFException
     {
         
         final PoddObjectLabel theObject =
                 RestletUtils.getParentDetails(this.getPoddArtifactManager(), ontologyID, objectToView);
         // set title & description of object to display
         dataModel.put("poddObject", theObject);
         final URI objectUri = theObject.getObjectURI();
         
         final Map<String, String> parentMap =
                 RestletUtils.populateParentDetails(this.getPoddArtifactManager(), ontologyID, objectUri);
         dataModel.put("parentObject", parentMap);
         
         // find the object's type
         final List<PoddObjectLabel> objectTypes = this.getPoddArtifactManager().getObjectTypes(ontologyID, objectUri);
         if(objectTypes == null || objectTypes.isEmpty())
         {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not determine type of object");
         }
         
         // Get label for the object type
         final PoddObjectLabel label = objectTypes.get(0);
         dataModel.put("objectType", label);
         if(PoddRdfConstants.PODD_SCIENCE_PROJECT.equals(label.getObjectURI()))
         {
             dataModel.put("isProject", true);
         }
         
         // populate the properties of the object
         final List<URI> orderedProperties =
                 this.getPoddArtifactManager().getOrderedProperties(ontologyID, objectUri, false);
         
         final Model allNeededStatementsForDisplay =
                 this.getPoddArtifactManager().getObjectDetailsForDisplay(ontologyID, objectUri);
         
         dataModel.put("artifactUri", ontologyID.getOntologyIRI().toOpenRDFURI());
         dataModel.put("versionIri", ontologyID.getVersionIRI().toOpenRDFURI());
         dataModel.put("propertyList", orderedProperties);
         dataModel.put("completeModel", allNeededStatementsForDisplay);
         
         final int childrenCount = this.getPoddArtifactManager().getChildObjects(ontologyID, objectUri).size();
         dataModel.put("childCount", childrenCount);
         
         if(!isPublished
                 && this.checkAuthentication(PoddAction.ARTIFACT_EDIT, ontologyID.getOntologyIRI().toOpenRDFURI(), false))
         {
             dataModel.put("canEditObject", true);
             dataModel.put("canDelete", true);
             dataModel.put("canAddChildren", true);
         }
         else
         {
             dataModel.put("canDelete", false);
             dataModel.put("canEditObject", false);
             dataModel.put("canAddChildren", false);
         }
         
         if(!isPublished
                 && this.checkAuthentication(PoddAction.PROJECT_ROLE_EDIT, ontologyID.getOntologyIRI().toOpenRDFURI(),
                         false))
         {
             dataModel.put("canEditRoles", true);
         }
         
         if(!isPublished
                 && this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_DELETE, ontologyID.getOntologyIRI()
                         .toOpenRDFURI(), false))
         {
             dataModel.put("canDeleteProject", true);
         }
         
         dataModel.put("selectedObjectCount", 0);
         dataModel.put("childHierarchyList", Collections.emptyList());
         
         dataModel.put("util", new FreemarkerUtil());
         
         // FIXME: No support currently for interactive editing of data references, so hide the edit
         // button to avoid users clicking on it
         for(PoddObjectLabel nextType : objectTypes)
         {
             if(nextType.getObjectURI().equals(PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE)
                     || nextType.getObjectURI().equals(PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL)
                     || nextType.getObjectURI().equals(PoddRdfConstants.PODD_BASE_FILE_REFERENCE_TYPE_SSH))
             {
                 dataModel.put("canEditObject", false);
                dataModel.put("canAddChildren", false);
             }
         }
         
     }
     
 }
