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
 
 package dk.statsbiblioteket.doms.bitstorage.highlevel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fcrepo.server.proxy.AbstractInvocationHandler;
 import org.fcrepo.server.management.ManagementModule;
 import org.fcrepo.server.access.Access;
 import org.fcrepo.server.access.ObjectProfile;
 import org.fcrepo.server.Server;
 import org.fcrepo.server.Context;
 import org.fcrepo.server.errors.ModuleInitializationException;
 import org.fcrepo.server.errors.ServerInitializationException;
 import org.fcrepo.common.Constants;
 
 import javax.xml.namespace.QName;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 import java.io.File;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.List;
 import java.util.LinkedList;
 
 
 /**
  * Hooks the modifyObject method, so that when a file object is set to Active
  * the file is published. Published means that the publish method from
  * HighLevelBitstorage is invoked on the object, which moves the file from
  * temporary bitstorage to permanent.
  *
  * @see HighlevelBitstorageSoapWebservice#publish(String)
  *      <p/>
  *      author: abr
  *      reviewer: jrg
  */
 public class HookApprove extends AbstractInvocationHandler {
 
     /**
      * Logger for this class.
      */
     private static Log LOG = LogFactory.getLog(HookApprove.class);
 
     /**
      * The service name of the bitstorage service. Nessesary for soap
      * operations. Should not ever change
      */
     public static final QName SERVICENAME =
             new QName(
                     "http://highlevel.bitstorage.doms.statsbiblioteket.dk/",
                     "HighlevelBitstorageSoapWebserviceService");
 
     /**
      * The name of the api method to hook
      */
     public static final String HOOKEDMETHOD = "modifyObject";
 
     /**
      * The boolean stating if the initialization steps have been taken
      */
     private boolean initialised = false;
 
     /**
      * The bitstorage client
      */
     private HighlevelBitstorageSoapWebservice bitstorageClient;
 
     /**
      * The Fedora Management module
      */
     private ManagementModule managementModule;
 
     /**
      * The Fedora Access Module
      */
     private Access accessModule;
 
     /**
      * The Fedora Server module
      */
     private Server serverModule;
 
     /**
      * The list of content models that are hooked. An object must have
      * one of these for the invoke method to attempt to publish the object
      */
     private List<String> filemodels;
 
 
     /**
      * The init method, called before the first invocation of the method.
      * Synchronized, to avoid problems with dual inits.
      * Cannot be made as a constructor, as it depends on the presense of other
      * Fedora modules.
      * Reads these parameters from management module section in fedora.fcfg.
      * See the supplied insertToFedora.fcfg for how to set these parameters.
      * <ul>
      * <li>dk.statsbiblioteket.doms.bitstorage.highlevel.hookapprove.filecmodel
      * The pid of the file content model. Only objects with this content model
      * will be published when set to Active.
      * <li>dk.statsbiblioteket.doms.bitstorage.highlevel.hookapprove.
      * webservicelocation
      * The location of the wsdl for the highlevel bitstorage service
      * </ul>
      *
      * @throws ModuleInitializationException If some Fedora module is not
      *                                       initiatialized yet
      * @throws ServerInitializationException If the Fedora server is not
      *                                       initialized yet
      * @throws MalformedURLException         If the webservice location is set
      *                                       to an incorrect url
      * @throws FileCouldNotBePublishedException
      *                                       catchall exception of something
      *                                       failed, but did not throw it's own
      *                                       exception
      */
     public synchronized void init() throws
                                     ModuleInitializationException,
                                     ServerInitializationException,
                                     MalformedURLException,
                                     FileCouldNotBePublishedException {
         if (initialised) {
             return;
         }
 
         serverModule = Server.getInstance(new File(Constants.FEDORA_HOME),
                                           false);
 
         filemodels = new LinkedList<String>();
 
         //get the management module
         managementModule = getManagement();
         if (managementModule == null) {
             throw new FileCouldNotBePublishedException("Could not get the "
                                                        + "management module "
                                                        + "from fedora");
         }
 
         //get the access module
         accessModule = getAccess();
         if (accessModule == null) {
             throw new FileCouldNotBePublishedException("Could not get the "
                                                        + "Access module from "
                                                        + "Fedora");
         }
 
         //read the parameters from the management module
         String fileContentModel = managementModule.getParameter(
                 "dk.statsbiblioteket.doms.bitstorage.highlevel.hookapprove."
                 + "filecmodel");
         if (fileContentModel != null) {
             filemodels.add(fileContentModel);
         } else {
             LOG.warn(
                     "No dk.statsbiblioteket.doms.bitstorage.highlevel."
                     + "hookapprove.filecmodel specified,"
                     + " disabling hookapprove");
         }
 
         String webservicelocation = managementModule.getParameter(
                 "dk.statsbiblioteket.doms.bitstorage.highlevel.hookapprove."
                 + "webservicelocation");
         if (webservicelocation == null) {
             webservicelocation = "http://localhost:8080/ecm/validate/";//TODO
             LOG.info(
                     "No dk.statsbiblioteket.doms.bitstorage.highlevel."
                     + "hookapprove.webservicelocation specified,"
                     + " using default location: " + webservicelocation);
         }
 
         //create the bitstorage client
         URL wsdl;
         wsdl = new URL(webservicelocation);
         bitstorageClient
                 = new HighlevelBitstorageSoapWebserviceService(wsdl,
                                                                SERVICENAME)
                 .getHighlevelBitstorageSoapWebservicePort();
 
         initialised = true;
 
     }
 
     /**
      * Utility method to get the Access Module from the Server module
      *
      * @return the Access Module
      */
     private Access getAccess() {
         Access module = (Access) serverModule.getModule(
                 "org.fcrepo.server.access.Access");
         if (module == null) {
             module = (Access) serverModule.getModule(
                     "fedora.server.access.Access");
         }
         return module;
     }
 
     /**
      * Utility method to get the Management Module from the Server module
      *
      * @return the Management module
      */
     private ManagementModule getManagement() {
         ManagementModule module = (ManagementModule) serverModule.getModule(
                 "org.fcrepo.server.management.Management");
         if (module == null) {
             module = (ManagementModule) serverModule.getModule(
                     "fedora.server.management.Management");
         }
         return module;
 
     }
 
     /**
      * If the Method is modifyObject AND object to be modified has the
      * specified content model AND is set to the Active state, then attempt to
      * publish file via the bitstorage system. If the file cannot be published,
      * the modification to the object is undone (but will leave a trail in the
      * object log).
      *
      * @param proxy  Unknown, probably this object
      * @param method The method to invoke
      * @param args   the arguments to the method, the array depends on which
      *               method is invoked. For modifyObject, the list is
      *               <ul>
      *               <li>0: Context context The calling context
      *               <li>1: String pid the pid of the object
      *               <li>2: String State the new state of the object
      *               <li>3: String label the new label of the object
      *               <li>4: String ownerID the new ownerID of the object
      *               <li>5: String logmessage the logmessage of the change
      *               </ul>
      *               Parameter 2-4 can be null, which means no change.
      * @return the method return type
      * @throws Throwable
      */
     public Object invoke(Object proxy, Method method, Object[] args)
             throws Throwable {
 
         try {
             init();
 
             if (!HOOKEDMETHOD.equals(method.getName())) {
                 //this calls the next proxy in the chain, and does nothing
                 // here
                 //target is a magical variable set to the next proxy
                 return method.invoke(target, args);
             }
 
             //If the call does not change the state to active, pass through
             String state = (String) args[2];
             if (!(state != null && state.startsWith("A"))) {
                 //startsWith to catch both "A" and "Active"
                 return method.invoke(target, args);
             }
 
             //so, we have a modify object that changes state to A
 
             //Get at few relevant variables from the arguments
             Context context = (Context) args[0];//call context
             String pid = args[1].toString();
 
             //Get profile to check if the object has the correct content model
             // and save the profile for rollback
             ObjectProfile profile = accessModule.getObjectProfile(context,
                                                                   pid,
                                                                   null);
 
             //Do the change, to see if it was allowed
             Object returnValue = method.invoke(target, args);
 
 
             //If we are here, the change committed without exceptions thrown
             try {
                 if (isFileObject(profile)) {//is this a file object?
                     if (!profile.objectState.startsWith("A")) {
                         //object was not already active
 
                         bitstorageClient.publish(pid);
                         //publish moves the file from temporary bitstorage to
                         //permanent bitstorage
                         //milestone, any fails beyound this must rollback
 
                     }
                 }
                 return returnValue;
 
             } catch (Exception e) {
                 //something broke in publishing, so undo the state operation
 
 
                 //rollback
                 String old_state = profile.objectState;
                 String old_label = null;
                 if (args[3] != null) {//label
                     //label changed
                     old_label = profile.objectLabel;
                 }
                 String old_ownerid = null;
                 if (args[4] != null) {//ownerid
                     //ownerid changed
                     old_ownerid = profile.objectOwnerId;
                 }
                 //commit the rollback.
                 // TODO perform this directly on the Management, instead?
                 Object new_return = method.invoke(
                         target,
                         context,
                         pid,
                         old_state,
                         old_label,
                         old_ownerid,
                         "Undoing state change because file could not be"
                        + " published");
                 //discard rollback returnvalue
                 throw new FileCouldNotBePublishedException(
                         "The file in '" + pid + "' could not be published. "
                         + "State change rolled back.",
                         e);
             }
         } catch (InvocationTargetException e) {
             //if the invoke method failed, throw the original exception on
             throw e.getCause();
         }//if anything else failed, let it pass
     }
 
     /**
      * Utility method to determine if the object is a file object
      *
      * @param profile the object profile
      * @return true if the object has the specified content model
      * @see #filemodels
      */
     private boolean isFileObject(ObjectProfile profile) {
         for (String model : profile.objectModels) {
             if (model == null) {
                 continue;
             }
             model = ensurePid(model);
             if (filemodels.contains(model)) {
                 return true;
             }
         }
         return false;
 
     }
 
     /**
      * Utility method to remove info:fedora/ prefix from pids, if they have
      * them
      *
      * @param pid the pid to clean
      * @return if the pid starts with info:fedora/ return the pid without this
      *         prefix, otherwise just return the unchanged pid
      */
     private String ensurePid(String pid) {
         if (pid.startsWith("info:fedora/")) {
             return pid.substring("info:fedora/".length());
         }
         return pid;
     }
 
 }
