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
 package com.github.podd.client.impl.restlet;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.openrdf.model.Literal;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.UnsupportedRDFormatException;
 import org.restlet.data.CharacterSet;
 import org.restlet.data.CookieSetting;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.InputRepresentation;
 import org.restlet.representation.ReaderRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.restlet.util.Series;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.ansell.restletutils.RestletUtilMediaType;
 import com.github.ansell.restletutils.RestletUtilRole;
 import com.github.ansell.restletutils.SesameRealmConstants;
 import com.github.podd.api.DanglingObjectPolicy;
 import com.github.podd.api.DataReferenceVerificationPolicy;
 import com.github.podd.api.file.DataReference;
 import com.github.podd.api.file.DataReferenceConstants;
 import com.github.podd.client.api.PoddClient;
 import com.github.podd.client.api.PoddClientException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddRoles;
 import com.github.podd.utils.PoddUser;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * Restlet based PODD Client implementation.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class RestletPoddClientImpl implements PoddClient
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     private String serverUrl = null;
     
     private Series<CookieSetting> currentCookies = new Series<CookieSetting>(CookieSetting.class);
     
     public RestletPoddClientImpl()
     {
     }
     
     public RestletPoddClientImpl(final String serverUrl)
     {
         this();
         this.serverUrl = serverUrl;
     }
     
     @Override
     public void addRole(final String userIdentifier, final RestletUtilRole role, final InferredOWLOntologyID artifact)
         throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         resource.getCookies().addAll(this.currentCookies);
         resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
         
         final Map<RestletUtilRole, Collection<URI>> mappings = new HashMap<>();
         final Collection<URI> artifacts = Arrays.asList(artifact.getOntologyIRI().toOpenRDFURI());
         mappings.put(role, artifacts);
         
         final Model model = new LinkedHashModel();
         PoddRoles.dumpRoleMappingsUser(mappings, model);
         
         final Representation post = this.postRdf(resource, model);
         
         try
         {
             final Model parsedStatements = this.parseRdf(post);
             
             if(!parsedStatements.contains(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                     PoddRdfConstants.VF.createLiteral(userIdentifier)))
             {
                 this.log.warn("Role edit response did not seem to contain the user identifier");
             }
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Could not parse results due to an IOException", e);
         }
     }
     
     @Override
     public InferredOWLOntologyID appendArtifact(final InferredOWLOntologyID ontologyIRI,
             final InputStream partialInputStream, final RDFFormat format) throws PoddClientException
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public InferredOWLOntologyID attachDataReference(final DataReference ref) throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         resource.getCookies().addAll(this.currentCookies);
         resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, ref.getArtifactID().getOntologyIRI()
                 .toString());
         resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, ref.getArtifactID()
                 .getVersionIRI().toString());
         resource.addQueryParameter(DataReferenceConstants.KEY_OBJECT_URI, ref.getObjectIri().toString());
         
         final Model model = ref.toRDF();
         
         final Representation post = this.postRdf(resource, model);
         
         try
         {
             final Model parsedStatements = this.parseRdf(post);
             
             final Collection<InferredOWLOntologyID> result = OntologyUtils.modelToOntologyIDs(parsedStatements);
             
             if(!result.isEmpty())
             {
                 return result.iterator().next();
             }
             
             throw new PoddClientException("Failed to verify that the file reference was attached correctly.");
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Could not parse artifact details due to an IOException", e);
         }
     }
     
     @Override
     public PoddUser createUser(final PoddUser user) throws PoddClientException
     {
         try
         {
             final Model model = new LinkedHashModel();
             user.toModel(model, true);
             final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
             Rio.write(model, output, RDFFormat.RDFJSON);
             final InputRepresentation rep =
                     new InputRepresentation(new ByteArrayInputStream(output.toByteArray()),
                             MediaType.valueOf(RDFFormat.RDFJSON.getDefaultMIMEType()));
             
             final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
             resource.getCookies().addAll(this.currentCookies);
             
             this.log.info("cookies: {}", this.currentCookies);
             
             resource.addQueryParameter("format", RDFFormat.RDFJSON.getDefaultMIMEType());
             
             // Request the results in Turtle to reduce the bandwidth
             final Representation post = resource.post(rep, MediaType.APPLICATION_RDF_TURTLE);
             
             final Model parsedStatements = this.parseRdf(post);
             
             final PoddUser result = PoddUser.fromModel(model);
             
             return result;
         }
         catch(final IOException | RDFHandlerException | ResourceException e)
         {
             throw new PoddClientException("Could not parse artifact details due to an exception", e);
         }
     }
     
     @Override
     public boolean deleteArtifact(final InferredOWLOntologyID artifactId) throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_DELETE));
         resource.getCookies().addAll(this.currentCookies);
         
         resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
         
         if(artifactId.getVersionIRI() != null)
         {
             // FIXME: Versions are not supported in general by PODD, but they are important for
             // verifying the state of the client to allow for early failure in cases where the
             // client is out of date.
             resource.addQueryParameter("versionUri", artifactId.getVersionIRI().toString());
         }
         
         resource.delete();
         
         if(resource.getStatus().isSuccess())
         {
             return true;
         }
         else
         {
             throw new PoddClientException("Failed to successfully delete artifact: "
                     + artifactId.getOntologyIRI().toString());
         }
     }
     
     @Override
     public void downloadArtifact(final InferredOWLOntologyID artifactId, final OutputStream outputStream,
             final RDFFormat format) throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         resource.getCookies().addAll(this.currentCookies);
         
         resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
         
         if(artifactId.getVersionIRI() != null)
         {
             // FIXME: Versions are not supported in general by PODD, but they are important for
             // verifying the state of the client to allow for early failure in cases where the
             // client is out of date.
             resource.addQueryParameter("versionUri", artifactId.getVersionIRI().toString());
         }
         
         // Pass the desired format to the get method of the ClientResource
         final Representation get = resource.get(MediaType.valueOf(format.getDefaultMIMEType()));
         
         try
         {
             get.write(outputStream);
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Could not write downloaded artifact to output stream", e);
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.client.api.PoddClient#getPoddServerUrl()
      */
     @Override
     public String getPoddServerUrl()
     {
         return this.serverUrl;
     }
     
     /**
      * Creates the URL for a given path using the current {@link #getPoddServerUrl()} result, or
      * throws an IllegalStateException if the server URL has not been set.
      * 
      * @param path
      *            The path of the web service to get a full URL for.
      * @return The full URL to the given path.
      * @throws IllegalStateException
      *             If {@link #setPoddServerUrl(String)} has not been called with a valid URL before
      *             this point.
      */
     private String getUrl(final String path)
     {
         if(this.serverUrl == null)
         {
             throw new IllegalStateException("PODD Server URL has not been set for this client");
         }
         
         if(path == null)
         {
             throw new NullPointerException("Path cannot be null");
         }
         
         if(!path.startsWith("/"))
         {
             return this.serverUrl + "/" + path;
         }
         else
         {
             return this.serverUrl + path;
         }
     }
     
     @Override
     public PoddUser getUserDetails(final String userIdentifier) throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
         resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
         
         resource.getCookies().addAll(this.currentCookies);
         
         try
         {
             final Representation getResponse = resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
             
             if(!resource.getStatus().equals(Status.SUCCESS_OK))
             {
                 throw new PoddClientException("Server returned a non-success status code: "
                         + resource.getStatus().toString());
             }
             
             final InputStream stream = getResponse.getStream();
             
             if(stream == null)
             {
                 throw new PoddClientException("Did not receive valid response from server");
             }
             
             final RDFFormat format =
                     Rio.getParserFormatForMIMEType(getResponse.getMediaType().getName(), RDFFormat.RDFXML);
             
             final Model model = Rio.parse(stream, "", format);
             
             final PoddUser poddUser = PoddUser.fromModel(model);
             
             return poddUser;
         }
         catch(final RDFParseException e)
         {
             throw new PoddClientException("Failed to parse RDF", e);
         }
         catch(final ResourceException e)
         {
             throw new PoddClientException("Failed to communicate with PODD Server", e);
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Input output exception while parsing RDF", e);
         }
     }
     
     @Override
     public boolean isLoggedIn()
     {
         return !this.currentCookies.isEmpty();
     }
     
     private List<InferredOWLOntologyID> listArtifactsInternal(final boolean published, final boolean unpublished)
         throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_LIST));
         resource.getCookies().addAll(this.currentCookies);
         
         resource.addQueryParameter(PoddWebConstants.KEY_PUBLISHED, Boolean.toString(published));
         resource.addQueryParameter(PoddWebConstants.KEY_UNPUBLISHED, Boolean.toString(unpublished));
         
         try
         {
             final Representation getResponse = resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
             
             if(!resource.getStatus().equals(Status.SUCCESS_OK))
             {
                 throw new PoddClientException("Server returned a non-success status code: "
                         + resource.getStatus().toString());
             }
             
             final InputStream stream = getResponse.getStream();
             
             if(stream == null)
             {
                 throw new PoddClientException("Did not receive valid response from server");
             }
             
             final RDFFormat format =
                     Rio.getParserFormatForMIMEType(getResponse.getMediaType().getName(), RDFFormat.RDFXML);
             
             return OntologyUtils.modelToOntologyIDs(Rio.parse(stream, "", format));
         }
         catch(final RDFParseException e)
         {
             throw new PoddClientException("Failed to parse RDF", e);
         }
         catch(final ResourceException e)
         {
             throw new PoddClientException("Failed to communicate with PODD Server", e);
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Input output exception while parsing RDF", e);
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.client.api.PoddClient#listDataReferenceRepositories()
      */
     @Override
     public List<String> listDataReferenceRepositories() throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
         resource.getCookies().addAll(this.currentCookies);
         
         try
         {
             final Representation getResponse = resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
             
             if(!resource.getStatus().equals(Status.SUCCESS_OK))
             {
                 throw new PoddClientException("Server returned a non-success status code: "
                         + resource.getStatus().toString());
             }
             
             final InputStream stream = getResponse.getStream();
             
             if(stream == null)
             {
                 throw new PoddClientException("Did not receive valid response from server");
             }
             
             final RDFFormat format =
                     Rio.getParserFormatForMIMEType(getResponse.getMediaType().getName(), RDFFormat.RDFXML);
             
             final Model model = Rio.parse(stream, "", format);
             
             DebugUtils.printContents(model);
             
             final Set<Value> aliases = model.filter(null, PoddRdfConstants.PODD_BASE_HAS_ALIAS, null).objects();
             
             final List<String> aliasResults = new ArrayList<String>(aliases.size());
             for(final Value nextAlias : aliases)
             {
                 aliasResults.add(((Literal)nextAlias).getLabel());
             }
             
             Collections.sort(aliasResults);
             
             return aliasResults;
         }
         catch(final RDFParseException e)
         {
             throw new PoddClientException("Failed to parse RDF", e);
         }
         catch(final ResourceException e)
         {
             throw new PoddClientException("Failed to communicate with PODD Server", e);
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Input output exception while parsing RDF", e);
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.client.api.PoddClient#listPublishedArtifacts()
      */
     @Override
     public List<InferredOWLOntologyID> listPublishedArtifacts() throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         return this.listArtifactsInternal(true, false);
     }
     
     @Override
     public Map<RestletUtilRole, Collection<String>> listRoles(final InferredOWLOntologyID artifactId)
         throws PoddClientException
     {
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_ROLES));
         resource.getCookies().addAll(this.currentCookies);
         resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
         
         this.log.info("cookies: {}", this.currentCookies);
         
         final Representation get = resource.get(MediaType.APPLICATION_RDF_TURTLE);
         
         try
         {
             return PoddRoles.extractRoleMappingsArtifact(this.parseRdf(get));
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Could not parse role details due to an IOException", e);
         }
         
     }
     
     @Override
     public Map<RestletUtilRole, Collection<URI>> listRoles(final String userIdentifier) throws PoddClientException
     {
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
         resource.getCookies().addAll(this.currentCookies);
         if(userIdentifier != null)
         {
             resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
         }
         
         this.log.info("cookies: {}", this.currentCookies);
         
         final Representation get = resource.get(MediaType.APPLICATION_RDF_TURTLE);
         
         try
         {
             return PoddRoles.extractRoleMappingsUser(this.parseRdf(get));
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Could not parse role details due to an IOException", e);
         }
         
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.client.api.PoddClient#listUnpublishedArtifacts()
      */
     @Override
     public List<InferredOWLOntologyID> listUnpublishedArtifacts() throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         return this.listArtifactsInternal(false, true);
     }
     
     @Override
     public List<PoddUser> listUsers() throws PoddClientException
     {
         // Implement this when the service is available
         throw new RuntimeException("TODO: Implement me!");
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.client.api.PoddClient#login(java.lang.String, char[])
      */
     @Override
     public boolean login(final String username, final String password) throws PoddClientException
     {
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_LOGIN_SUBMIT));
         resource.getCookies().addAll(this.currentCookies);
         
         // TODO: when Cookies natively supported by Client Resource, or another method remove this
         // Until then, this is necessary to manually attach the cookies after login to the
         // redirected address.
         // GitHub issue for this: https://github.com/restlet/restlet-framework-java/issues/21
         resource.setFollowingRedirects(false);
         
         final Form form = new Form();
         form.add("username", username);
         form.add("password", password);
         
         final Representation rep = resource.post(form.getWebRepresentation(CharacterSet.UTF_8));
         
         try
         {
             this.log.info("login result status: {}", resource.getStatus());
             if(rep != null)
             {
                 this.log.info("login result: {}", rep.getText());
             }
             else
             {
                 this.log.info("login result was null");
             }
             
             // HACK
             if(resource.getStatus().equals(Status.REDIRECTION_SEE_OTHER) || resource.getStatus().isSuccess())
             {
                 this.currentCookies = resource.getCookieSettings();
             }
             
             this.log.info("cookies: {}", this.currentCookies);
             
             return !this.currentCookies.isEmpty();
         }
         catch(final ResourceException e)
         {
             this.currentCookies.clear();
             this.log.warn("Error with request", e);
             throw new PoddClientException(e);
         }
         catch(final IOException e)
         {
             this.currentCookies.clear();
             this.log.warn("Error with getting login result text for debugging", e);
             throw new PoddClientException(e);
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.client.api.PoddClient#logout()
      */
     @Override
     public boolean logout() throws PoddClientException
     {
         this.log.info("cookies: {}", this.currentCookies);
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_LOGOUT));
         // add the cookie settings so that the server knows who to logout
         resource.getCookies().addAll(this.currentCookies);
         
         // TODO: when Cookies natively supported by Client Resource, or another method remove this
         // Until then, this is necessary to manually attach the cookies after login to the
         // redirected address.
         // GitHub issue for this: https://github.com/restlet/restlet-framework-java/issues/21
         resource.setFollowingRedirects(false);
         
         final Representation rep = resource.get();
         
         this.currentCookies = resource.getCookieSettings();
         
         try
         {
             this.log.info("logout result status: {}", resource.getStatus());
             
             if(rep != null)
             {
                 this.log.info("logout result: {}", rep.getText());
             }
             else
             {
                 this.log.info("logout result was null");
             }
             
             this.log.info("cookies: {}", this.currentCookies);
             
             this.currentCookies.clear();
             
             return true;
         }
         catch(final ResourceException e)
         {
             this.log.warn("Error with request", e);
             throw new PoddClientException(e);
         }
         catch(final IOException e)
         {
             this.log.warn("Error with getting logout result text for debugging", e);
             throw new PoddClientException(e);
         }
     }
     
     private Model parseRdf(final Representation rep) throws PoddClientException, IOException
     {
         final RDFFormat format = Rio.getParserFormatForMIMEType(rep.getMediaType().getName());
         
         if(format == null)
         {
             throw new PoddClientException("Did not understand the format for the RDF response: "
                     + rep.getMediaType().getName());
         }
         
         try
         {
             return Rio.parse(rep.getStream(), "", format);
         }
         catch(RDFParseException | UnsupportedRDFormatException e)
         {
             throw new PoddClientException("There was an error parsing the artifact", e);
         }
     }
     
     /**
      * @param resource
      * @param rdf
      * @return
      * @throws PoddClientException
      * @throws ResourceException
      */
     private Representation postRdf(final ClientResource resource, final Model rdf) throws PoddClientException,
         ResourceException
     {
         final StringWriter writer = new StringWriter();
         
         try
         {
             Rio.write(rdf, writer, RDFFormat.RDFJSON);
         }
         catch(final RDFHandlerException e)
         {
             throw new PoddClientException("Could not generate request entity", e);
         }
         
         final Representation rep =
                 new ReaderRepresentation(new StringReader(writer.toString()), RestletUtilMediaType.APPLICATION_RDF_JSON);
         
         final Representation post = resource.post(rep, RestletUtilMediaType.APPLICATION_RDF_JSON);
         return post;
     }
     
     @Override
     public InferredOWLOntologyID publishArtifact(final InferredOWLOntologyID ontologyIRI) throws PoddClientException
     {
         throw new RuntimeException("TODO: Implement me!");
     }
     
     @Override
     public void removeRole(final String userIdentifier, final RestletUtilRole role, final InferredOWLOntologyID artifact)
         throws PoddClientException
     {
         throw new RuntimeException("TODO: Implement me");
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.client.api.PoddClient#setPoddServerUrl(java.lang.String)
      */
     @Override
     public void setPoddServerUrl(final String serverUrl)
     {
         this.serverUrl = serverUrl;
     }
     
     @Override
     public InferredOWLOntologyID unpublishArtifact(final InferredOWLOntologyID ontologyIRI) throws PoddClientException
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public InferredOWLOntologyID updateArtifact(final InferredOWLOntologyID ontologyIRI,
             final InputStream fullInputStream, final RDFFormat format) throws PoddClientException
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public InferredOWLOntologyID uploadNewArtifact(final InputStream input, final RDFFormat format)
         throws PoddClientException
     {
         return this.uploadNewArtifact(input, format, DanglingObjectPolicy.REPORT,
                 DataReferenceVerificationPolicy.DO_NOT_VERIFY);
     }
     
     @Override
     public InferredOWLOntologyID uploadNewArtifact(final InputStream input, final RDFFormat format,
             final DanglingObjectPolicy danglingObjectPolicy,
             final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws PoddClientException
     {
         final InputRepresentation rep = new InputRepresentation(input, MediaType.valueOf(format.getDefaultMIMEType()));
         
         final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         resource.getCookies().addAll(this.currentCookies);
         
         this.log.info("cookies: {}", this.currentCookies);
         
         resource.addQueryParameter("format", format.getDefaultMIMEType());
         if(danglingObjectPolicy == DanglingObjectPolicy.FORCE_CLEAN)
         {
             resource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, "true");
         }
         if(dataReferenceVerificationPolicy == DataReferenceVerificationPolicy.VERIFY)
         {
             resource.addQueryParameter(PoddWebConstants.KEY_EDIT_VERIFY_FILE_REFERENCES, "true");
         }
         
         // Request the results in Turtle to reduce the bandwidth
         final Representation post = resource.post(rep, MediaType.APPLICATION_RDF_TURTLE);
         
         try
         {
             final Model parsedStatements = this.parseRdf(post);
             
             final Collection<InferredOWLOntologyID> result = OntologyUtils.modelToOntologyIDs(parsedStatements);
             
             if(!result.isEmpty())
             {
                 return result.iterator().next();
             }
             
             throw new PoddClientException("Failed to verify that the artifact was uploaded correctly.");
         }
         catch(final IOException e)
         {
             throw new PoddClientException("Could not parse artifact details due to an IOException", e);
         }
     }
 }
