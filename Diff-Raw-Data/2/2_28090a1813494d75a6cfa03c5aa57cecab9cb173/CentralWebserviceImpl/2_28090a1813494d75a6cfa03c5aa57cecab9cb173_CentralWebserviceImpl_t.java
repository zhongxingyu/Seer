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
 
 
 package dk.statsbiblioteket.doms.central;
 
 import dk.statsbiblioteket.doms.bitstorage.highlevel.Characterisation;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
 import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
 import dk.statsbiblioteket.doms.central.connectors.bitstorage.Bitstorage;
 import dk.statsbiblioteket.doms.central.connectors.ecm.ECM;
 import dk.statsbiblioteket.doms.central.connectors.fedora.Fedora;
 import dk.statsbiblioteket.doms.central.connectors.fedora.FedoraFactory;
 import dk.statsbiblioteket.doms.central.connectors.updatetracker.UpdateTracker;
 import dk.statsbiblioteket.doms.central.connectors.updatetracker.UpdateTrackerRecord;
 import dk.statsbiblioteket.doms.centralWebservice.*;
 import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
 import dk.statsbiblioteket.doms.webservices.configuration.ConfigCollection;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 import java.lang.String;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Aug 18, 2010
  * Time: 2:01:51 PM
  * To change this template use File | Settings | File Templates.
  */
 @WebService(endpointInterface = "dk.statsbiblioteket.doms.centralWebservice.CentralWebservice")
 public class CentralWebserviceImpl implements CentralWebservice {
 
     @Resource
     WebServiceContext context;
 
     private static Log log = LogFactory.getLog(
             CentralWebserviceImpl.class);
     private String ecmLocation;
     private String fedoraLocation;
     private String bitstorageLocation;
     private String updateTrackerLocation;
 
     public CentralWebserviceImpl() {
         bitstorageLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.bitstorageWSDL");
         ecmLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.ecmLocation");
         fedoraLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.fedoraLocation");
         updateTrackerLocation = ConfigCollection.getProperties().getProperty(
                 "dk.statsbiblioteket.doms.central.updateTrackerLocation");
     }
 
 
     public String newObject(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "oldID", targetNamespace = "")
             List<String> oldID) throws
                                 InvalidCredentialsException,
                                 InvalidResourceException,
                                 MethodFailedException {
         try {
             log.trace(
                     "Entering newObject with params pid=" + pid + " and oldIDs="
                     + oldID.toString());
             Credentials creds = getCredentials();
             ECM ecm = new ECM(creds, ecmLocation);
             return ecm.createNewObject(pid, oldID);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
 
     public void setObjectLabel(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "name", targetNamespace = "")
             String name) throws
                          InvalidCredentialsException,
                          InvalidResourceException,
                          MethodFailedException {
         try {
             log.trace("Entering setObjectLabel with params pid=" + pid
                       + " and name=" + name);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             fedora.modifyObjectLabel(pid, name);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
     }
 
     public void deleteObject(
             @WebParam(name = "pids", targetNamespace = "") List<String> pids)
             throws MethodFailedException, InvalidCredentialsException {
         try {
             log.trace("Entering deleteObject with params pid=" + pids);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             for (String pid : pids) {
                 fedora.modifyObjectState(pid, fedora.STATE_DELETED);
             }
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public void markPublishedObject(
             @WebParam(name = "pids", targetNamespace = "")
             List<java.lang.String> pids)
             throws InvalidCredentialsException, MethodFailedException {
         List<String> activated = new ArrayList<String>();
         try {
             log.trace("Entering markPublishedObject with params pids=" + pids);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             for (String pid : pids) {
                 fedora.modifyObjectState(pid, fedora.STATE_ACTIVE);
                 activated.add(pid);
             }
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             //rollback
             markInProgressObject(activated);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             markInProgressObject(activated);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             markInProgressObject(activated);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
 
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             markInProgressObject(activated);
             throw new MethodFailedException("Server error", "Server error", e);
         }
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void markInProgressObject(
             @WebParam(name = "pids", targetNamespace = "")
             List<java.lang.String> pids)
             throws MethodFailedException, InvalidCredentialsException {
         try {
             log.trace("Entering markInProgressObject with params pids=" + pids);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             for (String pid : pids) {
                 fedora.modifyObjectState(pid, fedora.STATE_INACTIVE);
             }
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public void modifyDatastream(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "datastream", targetNamespace = "")
             String datastream,
             @WebParam(name = "contents", targetNamespace = "")
             String contents)
             throws MethodFailedException, InvalidCredentialsException {
         try {
             log.trace("Entering modifyDatastream with params pid=" + pid
                       + " and datastream=" + datastream + " and contents="
                       + contents);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             fedora.modifyDatastreamByValue(pid, datastream, contents);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public String getDatastreamContents(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "datastream", targetNamespace = "")
             String datastream)
             throws MethodFailedException, InvalidCredentialsException {
         try {
             log.trace("Entering getDatastreamContents with params pid=" + pid
                       + " and datastream=" + datastream);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             return fedora.getXMLDatastreamContents(pid, datastream);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public void addFileFromPermanentURL(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "filename", targetNamespace = "") String filename,
             @WebParam(name = "md5sum", targetNamespace = "") String md5Sum,
             @WebParam(name = "permanentURL", targetNamespace = "")
             String permanentURL,
             @WebParam(name = "formatURI", targetNamespace = "")
             String formatURI)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             log.trace("Entering addFileFromPermamentURL with params pid=" + pid
                       + " and filename=" + filename + " and md5sum=" + md5Sum
                       + " and permanentURL=" + permanentURL + " and formatURI="
                       + formatURI);
             Credentials creds = getCredentials();
             Bitstorage bs = new Bitstorage(creds, bitstorageLocation);
             String existingObject = getFileObjectWithURL(permanentURL);
             if (existingObject != null) {
                 log.warn("Attempt to add a permament url that already exists"
                          + "in DOMS");
                 throw new MethodFailedException(
                         "This permanent url has already "
                         + "been added to the object '" +
                         existingObject + "'",
                         "This permanent url has already "
                         + "been added to the object '" +
                         existingObject + "'");
             }
             Characterisation emptycharac = new Characterisation();
             emptycharac.setValidationStatus("valid");
             emptycharac.setBestFormat(formatURI);
             emptycharac.getFormatURIs().add(formatURI);
             bs.uploadFileToObjectFromPermanentURLWithCharacterisation(pid,
                                                                       filename,
                                                                       permanentURL,
                                                                       md5Sum,
                                                                       emptycharac);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
 
     public String getFileObjectWithURL(
             @WebParam(name = "URL", targetNamespace = "") String url)
             throws MethodFailedException, InvalidCredentialsException {
         try {
             log.trace("Entering getFileObjectWithURL with param url=" + url);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             List<String> objects = fedora.listObjectsWithThisLabel(url);
 
             if (objects != null && !objects.isEmpty()) {
                 return objects.get(0);
             } else {
                 return null;
             }
 
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public void addRelation(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "subject", targetNamespace = "")
             String subject,
             @WebParam(name = "predicate", targetNamespace = "")
             String predicate,
             @WebParam(name = "object", targetNamespace = "")
             String object)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             log.trace("Entering addRelation with params pid=" + pid
                       + " and subject=" + subject + " and predicate="
                       + predicate + " and object=" + object);
             Credentials creds = getCredentials();
             Fedora fedora = FedoraFactory.newInstance(creds,
                                                       fedoraLocation);
             fedora.addRelation(pid, subject, predicate, object);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public ViewBundle getViewBundle(
             @WebParam(name = "pid", targetNamespace = "") String pid,
             @WebParam(name = "name", targetNamespace = "")
             String viewAngle)
             throws InvalidCredentialsException, MethodFailedException {
         log.trace("Entering getViewBundle with params pid=" + pid
                   + " and viewAngle=" + viewAngle);
         /*
         * Pseudo kode here
         * We need to figure two things out
         * the bundle
         * the type
         * ECM generates the bundle
         * The type is the entry content model of the origin pid
         * */
 
         Credentials creds = getCredentials();
         try {
             ECM ecm = new ECM(creds, ecmLocation);
 /*
             List<String> types = ecm.getEntryContentModelsForObject(pid,
                                                                     viewAngle);
             if (types.isEmpty()) {
                 throw new BackendInvalidResourceException("Pid '"+pid+"'is not an entry object for angle '"+viewAngle+"'");
             }
 */
             String bundleContentsString = ecm.createBundle(pid, viewAngle);
 
             ViewBundle viewBundle = new ViewBundle();
             viewBundle.setId(pid);
             viewBundle.setContents(bundleContentsString);
             return viewBundle;
 
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
     }
 
     public List<RecordDescription> getIDsModified(
             @WebParam(name = "since", targetNamespace = "") long since,
             @WebParam(name = "collectionPid", targetNamespace = "")
             String collectionPid,
             @WebParam(name = "viewAngle", targetNamespace = "")
             String viewAngle,
             @WebParam(name = "state", targetNamespace = "") String state,
             @WebParam(name = "offset", targetNamespace = "") Integer offset,
             @WebParam(name = "limit", targetNamespace = "") Integer limit)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             logEntering("getIDsModified",
                         since + "",
                         collectionPid,
                         viewAngle,
                         state,
                         offset + "",
                         limit + "");
             Credentials creds = getCredentials();
             UpdateTracker tracker = new UpdateTracker(creds,
                                                       updateTrackerLocation);
             if (state == null || state.isEmpty()) {
                 state = "Published";
             }
             List<UpdateTrackerRecord> modifieds
                     = tracker.listObjectsChangedSince(
                     collectionPid,
                     viewAngle,
                     since,
                     state,
                     offset,
                     limit);
             return transform(modifieds);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
 
     }
 
 
     public long getLatestModified(
             @WebParam(name = "collectionPid", targetNamespace = "")
             String collectionPid,
             @WebParam(name = "viewAngle", targetNamespace = "")
             String viewAngle,
             @WebParam(name = "state", targetNamespace = "")
             String state)
             throws InvalidCredentialsException, MethodFailedException {
         try {
             logEntering("getLatestModified", collectionPid, viewAngle, state);
             Credentials creds = getCredentials();
             UpdateTracker tracker = new UpdateTracker(creds,
                                                       updateTrackerLocation);
             return tracker.getLatestModification(collectionPid,
                                                  viewAngle,
                                                  state);
         } catch (MalformedURLException e) {
             log.error("caught problemException", e);
             throw new MethodFailedException("Webservice Config invalid",
                                             "Webservice Config invalid",
                                             e);
         } catch (BackendMethodFailedException e) {
             log.warn("Failed to execute method", e);
             throw new MethodFailedException("Method failed to execute",
                                             "Method failed to execute",
                                             e);
         } catch (BackendInvalidCredsException e) {
             log.debug("User supplied invalid credentials", e);
             throw new InvalidCredentialsException("Invalid Credentials Supplied",
                                                   "Invalid Credentials Supplied",
                                                   e);
         } catch (BackendInvalidResourceException e) {
             log.debug("Invalid resource requested", e);
             throw new InvalidCredentialsException("Invalid Resource Requested",
                                                   "Invalid Resource Requested",
                                                   e);
 
         } catch (Exception e) {
             log.warn("Caught Unknown Exception", e);
             throw new MethodFailedException("Server error", "Server error", e);
         }
 
     }
 
     private List<RecordDescription> transform(List<UpdateTrackerRecord> input) {
         List<RecordDescription> output = new ArrayList<RecordDescription>();
         for (UpdateTrackerRecord updateTrackerRecord : input) {
             output.add(transform(updateTrackerRecord));
         }
         return output;
     }
 
     private RecordDescription transform(UpdateTrackerRecord updateTrackerRecord) {
         RecordDescription a = new RecordDescription();
         a.setCollectionPid(updateTrackerRecord.getCollectionPid());
         a.setEntryContentModelPid(updateTrackerRecord.getEntryContentModelPid());
         a.setPid(updateTrackerRecord.getPid());
         a.setDate(updateTrackerRecord.getDate().getTime());
         return a;
     }
 
     private Credentials getCredentials() {
         HttpServletRequest request = (HttpServletRequest) context
                 .getMessageContext()
                 .get(MessageContext.SERVLET_REQUEST);
         Credentials creds = (Credentials) request.getAttribute("Credentials");
         if (creds == null) {
             log.warn("Attempted call at Central without credentials");
             creds = new Credentials("", "");
         }
         return creds;
 
     }
 
     private void logEntering(String method, String... params) {
         if (log.isTraceEnabled()) {
             String command = method + "(";
             for (String param : params) {
                command = command + " " + param + ",";
             }
             command = command.substring(0, command.length() - 1) + ")";
             log.trace("Entering " + command);
         }
     }
 
 }
