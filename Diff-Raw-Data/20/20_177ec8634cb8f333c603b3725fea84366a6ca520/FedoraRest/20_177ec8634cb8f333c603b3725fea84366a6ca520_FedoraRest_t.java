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
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
 import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
 import dk.statsbiblioteket.doms.central.connectors.Connector;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamType;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectDatastreams;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectFieldsType;
 import dk.statsbiblioteket.doms.central.connectors.fedora.generated.ResultType;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectType;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.SearchResult;
 import dk.statsbiblioteket.doms.central.connectors.fedora.utils.Constants;
 import dk.statsbiblioteket.doms.central.connectors.fedora.utils.DateUtils;
 import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
 import dk.statsbiblioteket.util.xml.DOM;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import javax.ws.rs.core.MediaType;
 import javax.xml.transform.TransformerException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA. User: abr Date: Aug 25, 2010 Time: 1:50:31 PM To change this template use File | Settings |
  * File Templates.
  */
 public class FedoraRest extends Connector implements Fedora {
 
     private WebResource restApi;
 
     private static Log log = LogFactory.getLog(FedoraRest.class);
     private String port;
     private static final String AS_OF_DATE_TIME = "asOfDateTime";
 
 
     public FedoraRest(Credentials creds,
                       String location)
             throws
             MalformedURLException {
         super(creds, location);
 
         restApi = client.resource(location + "/objects");
         restApi.addFilter(new HTTPBasicAuthFilter(creds.getUsername(), creds.getPassword()));
         port = calculateFedoraPort(location);
     }
 
     private String calculateFedoraPort(String location) {
         String portString = location.substring(location.lastIndexOf(':') + 1);
         portString = portString.substring(0, portString.indexOf('/'));
         return portString;
     }
 
     @Override
     public boolean exists(String pid,
                           Long asOfDateTime)
             throws
             BackendInvalidCredsException,
             BackendMethodFailedException {
         try {
             ObjectProfile profile = getObjectProfile(pid, asOfDateTime);
         } catch (BackendInvalidResourceException e) {
             return false;
         }
         return true;
     }
 
     @Override
     public boolean isDataObject(String pid,
                                 Long asOfDateTime)
             throws
             BackendInvalidCredsException,
             BackendMethodFailedException {
         try {
             ObjectProfile profile = getObjectProfile(pid, asOfDateTime);
             return profile.getType().equals(ObjectType.DATA_OBJECT);
         } catch (BackendInvalidResourceException e) {
             return false;
         }
     }
 
     @Override
     public boolean isTemplate(String pid,
                               Long asOfDateTime)
             throws
             BackendInvalidCredsException,
             BackendMethodFailedException {
         try {
             ObjectProfile profile = getObjectProfile(pid, asOfDateTime);
             return profile.getType().equals(ObjectType.TEMPLATE);
         } catch (BackendInvalidResourceException e) {
             return false;
         }
 
     }
 
     @Override
     public boolean isContentModel(String pid,
                                   Long asOfDateTime)
             throws
             BackendInvalidCredsException,
             BackendMethodFailedException {
         try {
             ObjectProfile profile = getObjectProfile(pid, asOfDateTime);
             return profile.getType().equals(ObjectType.CONTENT_MODEL);
         } catch (BackendInvalidResourceException e) {
             return false;
         }
 
     }
 
     @Override
     public String getObjectXml(String pid,
                                Long asOfTime)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
 
         try {
             //Get basic fedora profile
             String
                     xml =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .path("/objectXML")
                            .type(MediaType.TEXT_XML_TYPE)
                            .get(String.class);
             xml = modifyForDate(xml, asOfTime);
             return xml;
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     private String modifyForDate(String xml,
                                  Long asOfTime) {
         //TODO filter out all versions newer than asOfTime
         return xml;
     }
 
 
     private String StringOrNull(Long time) {
         if (time != null && time > 0) {
             DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
             return formatter.format(new Date(time));
         }
         return "";
     }
 
 
     @Override
     public String ingestDocument(Document document,
                                  String logmessage)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException {
         String payload;
         try {
 
             payload = DOM.domToString(document);
         } catch (TransformerException e) {
             throw new BackendMethodFailedException("Supplied document not valid", e);
         }
         try {
 
             String
                     pid =
                     restApi.path("/")
                            .path(URLEncoder.encode("new", "UTF-8"))
                            .type(MediaType.TEXT_XML_TYPE)
                            .post(String.class, payload);
             return pid;
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied when ingesting document: \n'"
                                                        + payload
                                                        + "'", e);
             } else {
                 throw new BackendMethodFailedException("Server error when ingesting document: \n'" + payload + "'", e);
             }
         }
     }
 
 
     public ObjectProfile getObjectProfile(String pid,
                                           Long asOfTime)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             //Get basic fedora profile
             dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectProfile
                     profile =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .queryParam("format", "text/xml")
                            .get(dk.statsbiblioteket.doms.central.connectors.fedora.generated.ObjectProfile.class);
             ObjectProfile prof = new ObjectProfile();
             prof.setObjectCreatedDate(profile.getObjCreateDate().toGregorianCalendar().getTime());
             prof.setObjectLastModifiedDate(profile.getObjLastModDate().toGregorianCalendar().getTime());
             prof.setLabel(profile.getObjLabel());
             prof.setOwnerID(profile.getObjOwnerId());
             prof.setState(profile.getObjState());
             prof.setPid(profile.getPid());
             List<String> contentmodels = new ArrayList<String>();
             for (String s : profile.getObjModels().getModel()) {
                 if (s.startsWith("info:fedora/")) {
                     s = s.substring("info:fedora/".length());
                 }
                 contentmodels.add(s);
             }
             prof.setContentModels(contentmodels);
 
             //Get relations
             List<FedoraRelation> relations = getNamedRelations(pid, null, asOfTime);
             prof.setRelations(relations);
 
             //get Datastream list
             ObjectDatastreams
                     datastreams =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .path("/datastreams")
                            .queryParam("format", "text/xml")
                            .get(ObjectDatastreams.class);
             List<DatastreamProfile> pdatastreams = new ArrayList<DatastreamProfile>();
             for (DatastreamType datastreamType : datastreams.getDatastream()) {
                 pdatastreams.add(getDatastreamProfile(pid, datastreamType.getDsid(), asOfTime));
             }
             prof.setDatastreams(pdatastreams);
 
             //decode type
             prof.setType(ObjectType.DATA_OBJECT);
             if (prof.getContentModels().contains("fedora-system:ContentModel-3.0")) {
                 prof.setType(ObjectType.CONTENT_MODEL);
             }
             if (prof.getContentModels().contains("doms:ContentModel_File")) {
                 prof.setType(ObjectType.FILE);
             }
             if (prof.getContentModels().contains("doms:ContentModel_Collection")) {
                 prof.setType(ObjectType.COLLECTION);
             }
 
             for (FedoraRelation fedoraRelation : prof.getRelations()) {
                 String predicate = fedoraRelation.getPredicate();
                 if (Constants.TEMPLATE_REL.equals(predicate)) {
                     prof.setType(ObjectType.TEMPLATE);
                     break;
                 }
             }
 
 
             return prof;
 
 
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
         }
 
     }
 
     public DatastreamProfile getDatastreamProfile(String pid,
                                                   String dsid,
                                                   Long asOfTime)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
 
             dk.statsbiblioteket.doms.central.connectors.fedora.generated.DatastreamProfile
                     fdatastream =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .path("/datastreams/")
                            .path(dsid)
                            .queryParam(AS_OF_DATE_TIME, StringOrNull(asOfTime))
                            .queryParam("format", "text/xml")
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
             if (type.equals("X")) {
                 profile.setInternal(true);
             } else if (type.equals("E") || type.equals("R")) {
                 profile.setInternal(false);
                 profile.setUrl(fdatastream.getDsLocation());
             }
             return profile;
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
         }
 
     }
 
     public void modifyObjectState(String pid,
                                   String state,
                                   String comment)
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
                    .queryParam("state", state)
                    .queryParam("logMessage", comment)
                    .put();
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
         }
     }
 
     private void createDatastreamByValue(String pid,
                                          String datastream,
                                          String checksumType,
                                          String checksum,
                                          String contents,
                                          String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }
             WebResource
                     resource =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .path("/datastreams/")
                            .path(URLEncoder.encode(datastream, "UTF-8"))
                            .queryParam("mimeType", "text/xml")
                            .queryParam("logMessage", comment);
 
             if (checksumType != null) {
                 resource = resource.queryParam("checksumType", checksumType);
 
             }
             if (checksum != null) {
                 resource = resource.queryParam("checksum", checksum);
 
             }
 
             resource.post(contents);
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
         }
 
 
     }
 
     private void updateExistingDatastreamByValue(String pid,
                                                  String datastream,
                                                  String checksumType,
                                                  String checksum,
                                                  String contents,
                                                  String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }
 
             WebResource
                     resource =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .path("/datastreams/")
                            .path(URLEncoder.encode(datastream, "UTF-8"))
                            .queryParam("mimeType", "text/xml")
                            .queryParam("logMessage", comment);
 
             if (checksumType != null) {
                 resource = resource.queryParam("checksumType", checksumType);
 
             }
             if (checksum != null) {
                 resource = resource.queryParam("checksum", checksum);
 
             }
             resource.put(contents);
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
         }
 
     }
 
     public void modifyDatastreamByValue(String pid,
                                         String datastream,
                                         String checksumType,
                                         String checksum,
                                         String contents,
                                         String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             updateExistingDatastreamByValue(pid, datastream, checksumType, checksum, contents, comment);
         } catch (BackendInvalidResourceException e) {
             //perhaps the datastream did not exist
            createDatastreamByValue        (pid, datastream, checksumType, checksum, contents, comment);
         }
     }
 
     public String getXMLDatastreamContents(String pid,
                                            String datastream,
                                            Long asOfTime)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
 
             String
                     contents =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .path("/datastreams/")
                            .path(URLEncoder.encode(datastream, "UTF-8"))
                            .path("/content")
                            .queryParam(AS_OF_DATE_TIME, StringOrNull(asOfTime))
                            .get(String.class);
             return contents;
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
         }
     }
 
     public void addRelation(String pid,
                             String subject,
                             String predicate,
                             String object,
                             boolean literal,
                             String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }  //TODO, fedora should take this logmessage
 
             if (!literal) {
                 if (!object.startsWith("info:fedora/")) {
                     object = "info:fedora/" + object;
                 }
             }
 
             URI predURI = new URI(predicate);
             if (!predURI.isAbsolute()) {
                 predicate = "info:fedora/" + predicate;
             }
 
 
             restApi.path("/")
                    .path(pid)
                    .path("/relationships/new")
                    .queryParam("predicate", predicate)
                    .queryParam("object", object)
                    .queryParam("isLiteral", "" + literal)
                    .post();
 
             if (predicate.equals("http://doms.statsbiblioteket.dk/relations/default/0/1/#hasLicense")) {
                 //this is a license relation, update the policy datastream
                 if (object.startsWith("info:fedora/")) {
                     object = object.substring("info:fedora/".length());
                 }
                 restApi.path("/")
                        .path(URLEncoder.encode(pid, "UTF-8"))
                        .path("/datastreams/POLICY")
                        .queryParam("dsLocation", "http://localhost:" + port +
                                                  "/fedora/objects/" + object + "/datastreams/LICENSE/content")
                        .queryParam("mimeType", "application/rdf+xml")
                        .queryParam("ignoreContent", "true")
                        .put();
             }
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
 
         } catch (URISyntaxException e) {
             throw new BackendMethodFailedException("Failed to parse predicate as an URI", e);
         }
     }
 
     @Override
     public List<FedoraRelation> getNamedRelations(String pid,
                                                   String name,
                                                   Long asOfTime)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             //TODO use asOfTime here, when fedora supports it
 
             String subject = pid;
             if (!subject.startsWith("info:fedora/")) {
                 subject = "info:fedora/" + subject;
             }
             WebResource
                     temp =
                     restApi.path("/")
                            .path(pid)
                            .path("/relationships/")
                            .queryParam("subject", subject)
                            .queryParam("format", "n-triples");
             if (name != null) {
                 temp = temp.queryParam("predicate", name);
             }
             String relationString = temp.get(String.class);
 
 
             String[] lines = relationString.split("\n");
             List<FedoraRelation> relations = new ArrayList<FedoraRelation>();
             for (String line : lines) {
                 String[] elements = line.split(" ");
                 if (elements.length > 2) {
                     FedoraRelation
                             rel =
                             new FedoraRelation(cleanInfo(elements[0]), clean(elements[1]), cleanInfo(elements[2]));
                     if (elements[2].startsWith("<info:fedora/")) {
                         rel.setLiteral(false);
                     } else {
                         rel.setLiteral(true);
                     }
                     relations.add(rel);
                 }
             }
             return relations;
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
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
     public void deleteRelation(String pid,
                                String subject,
                                String predicate,
                                String object,
                                boolean literal,
                                String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             } //TODO, fedora should take this logmessage
 
             if (!literal) {
                 if (!object.startsWith("info:fedora/")) {
                     object = "info:fedora/" + object;
                 }
             }
             URI predURI = new URI(predicate);
             if (!predURI.isAbsolute()) {
                 predicate = "info:fedora/" + predicate;
             }
 
             restApi.path("/")
                    .path(pid)
                    .path("/relationships/")
                    .queryParam("predicate", predicate)
                    .queryParam("object", object)
                    .queryParam("isLiteral", "" + literal)
                    .delete();
 
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
 
         } catch (URISyntaxException e) {
             throw new BackendMethodFailedException("Failed to parse predicate as an URI", e);
         }
     }
 
 
     public void modifyObjectLabel(String pid,
                                   String name,
                                   String comment)
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
                    .queryParam("label", name)
                    .queryParam("logMessage", comment)
                    .put();
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
 
     @Override
     public List<SearchResult> fieldsearch(String query,
                                           int offset,
                                           int pageLength)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException {
         try {
 
             ResultType
                     searchResult =
                     restApi.queryParam("terms", query)
                            .queryParam("maxResults", pageLength + "")
                            .queryParam("resultFormat", "xml")
                            .queryParam("pid", "true")
                            .queryParam("label", "true")
                            .queryParam("state", "true")
                            .queryParam("cDate", "true")
                            .queryParam("mDate", "true")
                            .get(ResultType.class);
 
             if (offset > 0) {
 
                 for (int i = 1; i <= offset; i++) {
                     String token = searchResult.getListSession().getToken();
                     searchResult =
                             restApi.queryParam("query", query)
                                    .queryParam("sessionToken", token)
                                    .queryParam("resultFormat", "xml")
                                    .get(ResultType.class);
                 }
             }
             List<SearchResult>
                     outputResults =
                     new ArrayList<SearchResult>(searchResult.getResultList().getObjectFields().size());
             for (ObjectFieldsType objectFieldsType : searchResult.getResultList().getObjectFields()) {
 
                 outputResults.add(new SearchResult(objectFieldsType.getPid(),
                                                    objectFieldsType.getLabel(),
                                                    objectFieldsType.getState(),
                                                    DateUtils.parseDateStrict(objectFieldsType.getCDate()).getTime(),
                                                    DateUtils.parseDateStrict(objectFieldsType.getMDate()).getTime()));
             }
             return outputResults;
 
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         } catch (ParseException e) {
             throw new BackendMethodFailedException("Failed to parse date from search result", e);
         }
     }
 
 
     @Override
     public void addExternalDatastream(String pid,
                                       String datastream,
                                       String label,
                                       String url,
                                       String formatURI,
                                       String mimeType,
                                       String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         addExternalDatastream(pid, datastream, label, url, formatURI, mimeType, null, null, comment);
     }
 
     @Override
     public void addExternalDatastream(String pid,
                                       String datastream,
                                       String label,
                                       String url,
                                       String formatURI,
                                       String mimeType,
                                       String md5sum,
                                       String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         addExternalDatastream(pid, datastream, label, url, formatURI, mimeType, "MD5", md5sum, comment);
     }
 
     @Override
     public void addExternalDatastream(String pid,
                                       String datastream,
                                       String label,
                                       String url,
                                       String formatURI,
                                       String mimeType,
                                       String checksumType,
                                       String checksum,
                                       String comment)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException,
             BackendInvalidResourceException {
         try {
             if (comment == null || comment.isEmpty()) {
                 comment = "No message supplied";
             }
 
             /*
            @PathParam(RestParam.PID) String pid,
                                  @PathParam(RestParam.DSID) String dsID,
                                  @QueryParam(RestParam.CONTROL_GROUP) @DefaultValue("X") String controlGroup,
                                  @QueryParam(RestParam.DS_LOCATION) String dsLocation,
                                  @QueryParam(RestParam.ALT_IDS) List<String> altIDs,
                                  @QueryParam(RestParam.DS_LABEL) String dsLabel,
                                  @QueryParam(RestParam.VERSIONABLE) @DefaultValue("true") Boolean versionable,
                                  @QueryParam(RestParam.DS_STATE) @DefaultValue("A") String dsState,
                                  @QueryParam(RestParam.FORMAT_URI) String formatURI,
                                  @QueryParam(RestParam.CHECKSUM_TYPE) String checksumType,
                                  @QueryParam(RestParam.CHECKSUM) String checksum,
                                  @QueryParam(RestParam.MIME_TYPE) String mimeType,
                                  @QueryParam(RestParam.LOG_MESSAGE) String logMessage
             */
             WebResource
                     resource =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                            .path("/datastreams/")
                            .path(URLEncoder.encode(datastream, "UTF-8"))
                            .queryParam("controlGroup", "R")
                            .queryParam("dsLocation", url)
                            .queryParam("dsLabel", label)
                            .queryParam("formatURI", formatURI)
                            .queryParam("mimeType", mimeType)
                            .queryParam("logMessage", comment);
             if (checksumType != null) {
                 resource = resource.queryParam("checksumType", checksumType);
             }
             if (checksum != null) {
                 resource = resource.queryParam("checksum", checksum);
             }
             resource.post();
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied: pid '" + pid + "'", e);
             } else if (e.getResponse().getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                 throw new BackendInvalidResourceException("Resource '" + pid + "'not found", e);
             } else {
                 throw new BackendMethodFailedException("Server error for '" + pid + "'", e);
             }
         }
 
     }
 
     @Override
     public String newEmptyObject(String pid,
                                  List<String> oldIDs,
                                  List<String> collections,
                                  String logMessage)
             throws
             BackendMethodFailedException,
             BackendInvalidCredsException {
 
         try {
 
             String
                     createdPid =
                     restApi.path("/")
                            .path(URLEncoder.encode(pid, "UTF-8"))
                           .queryParam("state","I")
                            .type(MediaType.TEXT_XML_TYPE)
                            .post(String.class);
            //Because fedora ignores the state param in the new object.....
            modifyObjectState(createdPid,"I",logMessage);
             if (!oldIDs.isEmpty()) {
                 String dublinCore = getXMLDatastreamContents(createdPid, "DC", null);
                Document dcDoc = DOM.stringToDOM(dublinCore,true);
                 XPathSelector
                         xpath =
                         DOM.createXPathSelector("dc", Constants.NAMESPACE_DC, "oai_dc", Constants.NAMESPACE_OAIDC);
                 Node existingIdentifier = xpath.selectNode(dcDoc, "/oai_dc:dc/dc:identifier");
                 for (String oldID : oldIDs) {
                     Element
                             identifierNode =
                             dcDoc.createElementNS(existingIdentifier.getNamespaceURI(),
                                                   existingIdentifier.getNodeName());
                     identifierNode.appendChild(dcDoc.createTextNode(oldID));
                     existingIdentifier.getParentNode().appendChild(identifierNode);
                 }
                modifyDatastreamByValue(pid, "DC",null,null, DOM.domToString(dcDoc), logMessage);
             }
             for (String collection : collections) {
                 addRelation(createdPid, createdPid, Constants.RELATION_COLLECTION, collection, false, logMessage);
             }
             return pid;
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                 throw new BackendInvalidCredsException("Invalid Credentials Supplied", e);
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         } catch (TransformerException e) {
             throw new BackendMethodFailedException("Failed to convert DC back to string", e);
         } catch (BackendInvalidResourceException e) {
             throw new BackendMethodFailedException("Failed to retrieve from the just created object", e);
         }
 
     }
 }
