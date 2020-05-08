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
 
 import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
 import dk.statsbiblioteket.doms.webservices.Credentials;
 import dk.statsbiblioteket.doms.webservices.ConfigCollection;
 import dk.statsbiblioteket.util.caching.TimeSensitiveCache;
 
 import java.util.List;
 import java.net.MalformedURLException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Oct 25, 2010
  * Time: 11:52:18 AM
  * To change this template use File | Settings | File Templates.
  */
 public class FedoraCache implements Fedora{
 
 
     private final Fedora fedora;
 
     /**
      * This is the blob of user specific caches. Note that this is itself a cache
      * so it will be garbage collected
      */
     private static TimeSensitiveCache<Credentials,Caches> userspecificCaches;
 
     private Caches myCaches;
 
 
     public FedoraCache(Credentials creds, Fedora fedora)
             throws MalformedURLException {
         if (userspecificCaches == null){
             String lifetime = ConfigCollection.getProperties().getProperty(
                     "dk.statsbiblioteket.doms.central.connectors.fedora.usercache.lifetime",
                     "" + 1000 * 60 * 10);
             String size = ConfigCollection.getProperties().getProperty(
                     "dk.statsbiblioteket.doms.central.connectors.fedora.usercache.size",
                     "" + 20);
             userspecificCaches = new TimeSensitiveCache<Credentials,Caches>(
                     Long.parseLong(lifetime),
                     true,
                     Integer.parseInt(size));
         }
 
         this.fedora = fedora;
         myCaches = userspecificCaches.get(creds);
         if (myCaches == null){
             myCaches = new Caches();
             userspecificCaches.put(creds, myCaches);
         }
 
     }
 
     public void modifyObjectState(String pid, String state) throws
                                                             BackendMethodFailedException,
                                                             BackendInvalidCredsException,
                                                             BackendInvalidResourceException {
         fedora.modifyObjectState(pid,state);
     }
 
     public void modifyDatastreamByValue(String pid,
                                         String datastream,
                                         String contents) throws
                                                          BackendMethodFailedException,
                                                          BackendInvalidCredsException,
                                                          BackendInvalidResourceException {
         fedora.modifyDatastreamByValue(pid,datastream,contents);
     }
 
     public String getXMLDatastreamContents(String pid, String datastream) throws
                                                                           BackendMethodFailedException,
                                                                           BackendInvalidCredsException,
                                                                           BackendInvalidResourceException {
         String content = myCaches.getDatastreamContents(pid,datastream);
        if (content != null){
             content = fedora.getXMLDatastreamContents(pid,datastream);
             myCaches.putDatastreamContents(pid,datastream,content);
         }
         return content;
     }
 
     public void addRelation(String pid,
                             String subject,
                             String property,
                             String object) throws
                                            BackendMethodFailedException,
                                            BackendInvalidCredsException,
                                            BackendInvalidResourceException {
         fedora.addRelation(pid,subject,property,object);
     }
 
     public List<String> listObjectsWithThisLabel(String label) throws
                                                                BackendInvalidCredsException,
                                                                BackendMethodFailedException,
                                                                BackendInvalidResourceException {
         return fedora.listObjectsWithThisLabel(label);
     }
 
     public void modifyObjectLabel(String pid, String name) throws
                                                            BackendMethodFailedException,
                                                            BackendInvalidCredsException,
                                                            BackendInvalidResourceException {
         fedora.modifyObjectLabel(pid,name);
     }
 }
