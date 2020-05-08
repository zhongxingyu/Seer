 /*
  * $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The DOMS project.
  * Copyright (C) 2007-2010  The State and University Library
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 
 package dk.statsbiblioteket.doms.central.connectors.fedora;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
 import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
 import dk.statsbiblioteket.doms.central.connectors.Connector;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamType;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectDatastreams;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectFieldsType;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.ResultType;
 import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Aug 25, 2010
  * Time: 1:50:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FedoraRest extends Connector implements Fedora {
     private static Client client = Client.create();
     private WebResource restApi;
 
     private static Log log = LogFactory.getLog(
             FedoraRest.class);
     private String port;
 
 
     public FedoraRest(Credentials creds, String location)
             throws MalformedURLException {
         super(creds, location);
         restApi = client.resource(location + "/objects");
          port = calculateFedoraPort(location);
     }
 
     private String calculateFedoraPort(String location) {
         String portString = location.substring(location.lastIndexOf(':') + 1);
         portString = portString.substring(0, portString.indexOf('/'));
         return portString;
     }
 
     public ObjectProfile getObjectProfile(String pid) throws
                                                       BackendMethodFailedException,
                                                       BackendInvalidCredsException,
                                                       BackendInvalidResourceException {
         try {
             //Get basic fedora profile
             dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectProfile profile =
                     restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                             .queryParam("format", "text/xml")
                             .header("Authorization", credsAsBase64())
                             .get(dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectProfile.class);
             ObjectProfile prof = new ObjectProfile();
             prof.setObjectCreatedDate(           profile.getObjCreateDate().toGregorianCalendar().getTime());
             prof.setObjectLastModifiedDate(profile.getObjLastModDate().toGregorianCalendar().getTime());
             prof.setLabel(profile.getObjLabel());
             prof.setOwnerID(profile.getObjOwnerId());
             prof.setState(profile.getObjState());
             prof.setPid(profile.getPid());
             List<String> contentmodels = new ArrayList<String>();
             for (String s : profile.getObjModels().getModel()) {
                 if (s.startsWith("info:fedora/")){
                     s = s.substring("info:fedora/".length());
                 }
                 contentmodels.add(s);
             }
             prof.setContentModels(contentmodels);
 
             //Get relations
             List<FedoraRelation> relations = getNamedRelations(pid, null);
             prof.setRelations(relations);
 
             //get Datastream list
             ObjectDatastreams datastreams = restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/datastreams")
                     .queryParam("format", "text/xml")
                     .header("Authorization", credsAsBase64())
                     .get(ObjectDatastreams.class);
             List<DatastreamProfile> pdatastreams = new ArrayList<DatastreamProfile>();
             for (DatastreamType datastreamType : datastreams.getDatastream()) {
                 pdatastreams.add(getDatastreamProfile(pid,datastreamType.getDsid()));
             }
             prof.setDatastreams(pdatastreams);
 
             //decode type
             prof.setType(ObjectType.DATA_OBJECT);
             if (prof.getContentModels().contains("fedora-system:ContentModel-3.0")){
                 prof.setType(ObjectType.CONTENT_MODEL);
             }
             if (prof.getContentModels().contains("doms:ContentModel_File")){
                 prof.setType(ObjectType.FILE);
             }
             if (prof.getContentModels().contains("doms:ContentModel_Collection")){
                 prof.setType(ObjectType.COLLECTION);
             }
 
             for (FedoraRelation fedoraRelation : prof.getRelations()) {
                 String predicate = fedoraRelation.getPredicate();
                 if ("http://ecm.sourceforge.net/relations/0/2/#isTemplateFor".equals(predicate)){
                     prof.setType(ObjectType.TEMPLATE);
                     break;
                 }
             }
 
 
             return prof;
 
 
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
 
     }
 
     public DatastreamProfile getDatastreamProfile(String pid, String dsid)
             throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
         try {
             dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamProfile fdatastream =
                     restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                             .path("/datastreams/")
                             .path(dsid)
                             .queryParam("format", "text/xml")
                             .header("Authorization", credsAsBase64())
                             .get(dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamProfile.class);
             DatastreamProfile profile = new DatastreamProfile();
             profile.setID(fdatastream.getDsID());
             profile.setLabel(fdatastream.getDsLabel());
             profile.setState(fdatastream.getDsState());
 
             profile.setChecksum(fdatastream.getDsChecksum());
             profile.setChecksumType(fdatastream.getDsChecksumType());
 
 
 
             profile.setCreated(fdatastream.getDsCreateDate().toGregorianCalendar().getTime().getTime());
             profile.setFormatURI(fdatastream.getDsFormatURI());
             profile.setMimeType(fdatastream.getDsMIME());
 
             String type = fdatastream.getDsControlGroup();
             if (type.equals("X")){
                 profile.setInternal(true);
             } else if (type.equals("E") || type.equals("R")){
                 profile.setInternal(false);
                 profile.setUrl(fdatastream.getDsLocation());
             }
             return profile;
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
 
     }
 
     public void modifyObjectState(String pid, String state, String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }
             restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                     .queryParam("state", state)
                     .queryParam("logMessage", comment)
                     .header("Authorization", credsAsBase64())
                     .put();
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException(e.getResponse().getEntity(String.class), e);
             }
         }
     }
 
     public void modifyDatastreamByValue(String pid,
                                         String datastream,
                                         String contents, String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }
 
             restApi.path("/")
                     .path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/datastreams/")
                     .path(URLEncoder.encode(datastream, "UTF-8"))
                     .queryParam("mimeType", "text/xml")
                     .queryParam("logMessage", comment)
                     .header("Authorization", credsAsBase64())
                     .post(contents);
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     public String getXMLDatastreamContents(String pid, String datastream)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             String contents = restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/datastreams/")
                     .path(URLEncoder.encode(datastream, "UTF-8"))
                     .path("/content")
                     .header("Authorization", credsAsBase64())
                     .get(String.class);
             return contents;
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     public void addRelation(String pid, String subject, String predicate, String object, boolean literal, String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }  //TODO, fedora should take this logmessage
 
             if (!literal){
                 if (!object.startsWith("info:fedora/")) {
                     object = "info:fedora/" + object;
                 }
             }
 
             URI predURI = new URI(predicate);
             if (!predURI.isAbsolute()){
                 predicate = "info:fedora/"+predicate;
             }
 
 
             restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/relationships/new")
                     .queryParam("predicate", predicate)
                     .queryParam("object", object)
                     .queryParam("isLiteral",""+literal)
                     .header("Authorization", credsAsBase64())
                     .post();
 
             if (predicate.equals("http://doms.statsbiblioteket.dk/relations/default/0/1/#hasLicense")){
                 //this is a license relation, update the policy datastream
                 if (object.startsWith("info:fedora/")){
                     object = object.substring("info:fedora/".length());
                 }
                 restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                         .path("/datastreams/POLICY")
                         .queryParam("dsLocation", "http://localhost:"+port+
                                     "/fedora/objects/" + object + "/datastreams/LICENSE/content")
                         .queryParam("mimeType", "application/rdf+xml")
                         .queryParam("ignoreContent", "true")
                         .header("Authorization", credsAsBase64())
                         .put();
             }
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 log.info(e.getResponse().toString());
                 throw new BackendMethodFailedException("Server error", e);
             }
         } catch (URISyntaxException e) {
             throw new BackendMethodFailedException("Failed to parse predicate as an URI", e);
         }
     }
 
     @Override
     public List<FedoraRelation> getNamedRelations(String pid, String name)
             throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
         try {
             String subject = pid;
             if (!subject.startsWith("info:fedora/")) {
                 subject = "info:fedora/" + subject;
             }
             WebResource temp = restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/relationships/")
                     .queryParam("subject", subject)
                     .queryParam("format", "n-triples");
             if (name != null) {
                 temp = temp.queryParam("predicate", name);
             }
             String relationString = temp.header("Authorization", credsAsBase64()).get(String.class);
 
 
             String[] lines = relationString.split("\n");
             List<FedoraRelation> relations = new ArrayList<FedoraRelation>();
             for (String line : lines) {
                 String[] elements = line.split(" ");
                 if (elements.length > 2) {
                     FedoraRelation rel = new FedoraRelation(cleanInfo(elements[0]), clean(elements[1]), cleanInfo(
                             elements[2]));
                     if (elements[2].startsWith("<info:fedora/")){
                         rel.setLiteral(false);
                     } else {
                         rel.setLiteral(true);
                     }
                     relations.add(rel);
                 }
             }
             return relations;
 
 
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     private String clean(String element) {
         if (element.startsWith("<") && element.endsWith(">")) {
             element = element.substring(1, element.length() - 1);
         }
         return element;
         //To change body of created methods use File | Settings | File Templates.
     }
 
     private String cleanInfo(String element) {
         element = clean(element);
 
         if (element.startsWith("info:fedora/")) {
             element = element.substring("info:fedora/".length());
         }
 
         return element;
         //To change body of created methods use File | Settings | File Templates.
     }
 
 
     @Override
     public void deleteRelation(String pid, String subject, String predicate, String object, boolean literal,
                                String comment)
             throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             } //TODO, fedora should take this logmessage
 
             if (!literal){
                 if (!object.startsWith("info:fedora/")) {
                     object = "info:fedora/" + object;
                 }
             }
             URI predURI = new URI(predicate);
             if (!predURI.isAbsolute()){
                 predicate = "info:fedora/"+predicate;
             }
 
             restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/relationships/")
                     .queryParam("predicate", predicate)
                     .queryParam("object", object)
                     .queryParam("isLiteral",""+literal)
                     .header("Authorization", credsAsBase64())
                     .delete();
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         } catch (URISyntaxException e) {
             throw new BackendMethodFailedException("Failed to parse predicate as an URI", e);
         }
     }
 
     @Override
     public List<String> getObjectsInCollection(String collectionPid, String contentModel)
             throws BackendInvalidCredsException, BackendMethodFailedException{
         try {
             if (!collectionPid.startsWith("info:fedora/")){
                 collectionPid = "info:fedora/"+collectionPid.trim();
             }
             if (!contentModel.startsWith("info:fedora/")){
                 contentModel = "info:fedora/"+contentModel.trim();
             }
             String query = "select $object\n"
                            + "from <#ri>\n"
                            + "where $object <http://doms.statsbiblioteket.dk/relations/default/0/1/#isPartOfCollection> <"+collectionPid+">\n"
                            + "and $object <fedora-model:hasModel> <"+contentModel+">\n"
                            + "and ($object <fedora-model:state> <fedora-model:Active>\n" +
                            "or $object <fedora-model:state> <fedora-model:Inactive>)";
             String objects = client.resource(location)
                     .path("/risearch")
                     .queryParam("type", "tuples")
                     .queryParam("lang", "iTQL")
                     .queryParam("format", "CSV")
                     .queryParam("flush", "true")
                     .queryParam("stream", "on")
                     .queryParam("query", query)
                     .header("Authorization", credsAsBase64())
                     .post(String.class);
             String[] lines = objects.split("\n");
             List<String> foundobjects = new ArrayList<String>();
             for (String line : lines) {
                 if (line.startsWith("\"")) {
                     continue;
                 }
                 if (line.startsWith("info:fedora/")) {
                     line = line.substring("info:fedora/".length());
                 }
                 foundobjects.add(line);
             }
             return foundobjects;
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             }  else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
 
     }
 
     public List<String> listObjectsWithThisLabel(String label)
             throws
             BackendInvalidCredsException,
             BackendMethodFailedException {
         //TODO sanitize label
         try {
             String query = "select $object\n"
                            + "from <#ri>\n"
                            + "where $object <fedora-model:label> '" + label + "'";
             String objects = client.resource(location)
                     .path("/risearch")
                     .queryParam("type", "tuples")
                     .queryParam("lang", "iTQL")
                     .queryParam("format", "CSV")
                     .queryParam("flush", "true")
                     .queryParam("stream", "on")
                     .queryParam("query", query)
                     .header("Authorization", credsAsBase64())
                     .post(String.class);
             String[] lines = objects.split("\n");
             List<String> foundobjects = new ArrayList<String>();
             for (String line : lines) {
                 if (line.startsWith("\"")) {
                     continue;
                 }
                 if (line.startsWith("info:fedora/")) {
                     line = line.substring("info:fedora/".length());
                 }
                 foundobjects.add(line);
             }
             return foundobjects;
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             }  else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     public void modifyObjectLabel(String pid, String name, String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }
 
             restApi.path("/").path(URLEncoder.encode(pid, "UTF-8"))
                     .queryParam("label", name)
                     .queryParam("logMessage", comment)
                     .header("Authorization", credsAsBase64())
                     .put();
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     @Override
     public List<String> findObjectFromDCIdentifier(String string)
             throws BackendInvalidCredsException, BackendMethodFailedException {
 
         //TODO sanitize label
 
         try {
             String query = "select $object\n"
                            + "from <#ri>\n"
                            + "where $object <dc:identifier> '" + string + "'"
                            + "and ($object <fedora-model:state> <fedora-model:Active>\n" +
                            "or $object <fedora-model:state> <fedora-model:Inactive>)";
             String objects = client.resource(location)
                     .path("/risearch")
                     .queryParam("type", "tuples")
                     .queryParam("lang", "iTQL")
                     .queryParam("format", "CSV")
                     .queryParam("flush", "true")
                     .queryParam("stream", "on")
                     .queryParam("query", query)
                     .header("Authorization", credsAsBase64())
                     .post(String.class);
             String[] lines = objects.split("\n");
             List<String> foundobjects = new ArrayList<String>();
             for (String line : lines) {
                 if (line.startsWith("\"")) {
                     continue;
                 }
                 if (line.startsWith("info:fedora/")) {
                     line = line.substring("info:fedora/".length());
                 }
                 foundobjects.add(line);
             }
             return foundobjects;
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
 
     }
 
     @Override
     public void flushTripples() throws BackendInvalidCredsException, BackendMethodFailedException {
         findObjectFromDCIdentifier("doms:ContentModel_DOMS");
     }
 
     @Override
     public List<SearchResult> fieldsearch(String query,
                                           int offset,
                                           int pageLength) throws
                                                           BackendMethodFailedException,
                                                           BackendInvalidCredsException
                                                           {
         try {
 
             ResultType searchResult = restApi.queryParam("terms", query)
                     .queryParam("maxResults", pageLength + "")
                     .queryParam("resultFormat", "xml")
                     .queryParam("pid", "true")
                     .queryParam("label", "true")
                     .queryParam("state", "true")
                     .queryParam("cDate", "true")
                     .queryParam("mDate", "true")
                     .header("Authorization", credsAsBase64())
                     .get(ResultType.class);
 
             if (offset > 0){
 
                 for (int i = 1; i <= offset; i++) {
                     String token = searchResult.getListSession().getToken();
                     searchResult = restApi.queryParam("query", query)
                             .queryParam("sessionToken", token)
                             .queryParam("resultFormat", "xml")
                             .header("Authorization", credsAsBase64())
                             .get(ResultType.class);
                 }
             }
             List<SearchResult> outputResults = new ArrayList<SearchResult>(searchResult.getResultList().getObjectFields().size());
             for (ObjectFieldsType objectFieldsType : searchResult.getResultList().getObjectFields()) {
 
                 outputResults.add(new SearchResult(objectFieldsType.getPid(),
                                                    objectFieldsType.getLabel(),
                                                    objectFieldsType.getState(),
                                                    DateUtils.parseDateStrict(objectFieldsType.getCDate()).getTime(),
                                                    DateUtils.parseDateStrict(objectFieldsType.getMDate()).getTime()));
             }
             return outputResults;
 
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         } catch (ParseException e) {
             throw new BackendMethodFailedException("Failed to parse date from search result",e);
         }
     }
 
     @Override
     public List<FedoraRelation> getInverseRelations(String pid, String predicate)
             throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
         try {
 
 
             String subject = pid;
             if (!subject.startsWith("info:fedora/")) {
                 subject = "info:fedora/" + subject;
             }
            String predicateOrig = predicate;
            String queryStart = "select $object";
            if (predicate == null || predicate.isEmpty()){
                 queryStart+= " $predicate";
                predicate = "$predicate";
                predicateOrig = null;
            } else {
                predicate = "<"+predicate+">";
            }
 
            String query = queryStart+"\n"
                            + "from <#ri>\n"
                           + "where $object "+predicate+" <" + subject + ">\n"
                            + "and ($object <fedora-model:state> <fedora-model:Active>\n" +
                            "or $object <fedora-model:state> <fedora-model:Inactive>)";
             String objects = client.resource(location)
                     .path("/risearch")
                     .queryParam("type", "tuples")
                     .queryParam("lang", "iTQL")
                     .queryParam("format", "CSV")
                     .queryParam("flush", "true")
                     .queryParam("stream", "on")
                     .queryParam("query", query)
                     .header("Authorization", credsAsBase64())
                     .post(String.class);
             String[] lines = objects.split("\n");
             List<FedoraRelation> relations = new ArrayList<FedoraRelation>();
 
             for (String line : lines) {
                 if (line.startsWith("\"")) {
                     continue;
                 }
                 if (line.startsWith("info:fedora/")) {
                     line = line.substring("info:fedora/".length());
                 }
                 String[] components = line.split(",");
 
                if (predicate.equals("$predicate")){
                    relations.add(new FedoraRelation(components[0],components[1],pid));
                } else {
                    relations.add(new FedoraRelation(components[0],predicateOrig,pid));
                }

 
             }
             return relations;
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus()
                 == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException(
                         "Invalid Credentials Supplied",
                         e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 }
