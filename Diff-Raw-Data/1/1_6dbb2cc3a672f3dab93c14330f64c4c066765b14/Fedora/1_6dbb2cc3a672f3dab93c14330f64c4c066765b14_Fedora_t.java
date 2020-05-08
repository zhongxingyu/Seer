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
 
 import dk.statsbiblioteket.doms.central.connectors.Connector;
 import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
 import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
 import dk.statsbiblioteket.doms.webservices.Credentials;
 
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import java.util.ArrayList;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Aug 25, 2010
  * Time: 1:50:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Fedora extends Connector {
     private static Client client = Client.create();
     public static final String STATE_ACTIVE = "A";
     public static final String STATE_INACTIVE = "I";
     public static final String STATE_DELETED = "D";
     private WebResource restApi;
 
 
 
 
 
     public Fedora(Credentials creds, String location)
             throws MalformedURLException {
         super(creds, location);
         restApi = client.resource(location + "/objects/");
 
     }
 
     public void modifyObjectState(String pid, String state)
             throws BackendMethodFailedException, BackendInvalidCredsException {
         try {
             restApi.path(URLEncoder.encode(pid, "UTF-8"))
                     .queryParam("state", state)
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
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     public void modifyDatastreamByValue(String pid,
                                         String datastream,
                                         String contents)
             throws BackendMethodFailedException, BackendInvalidCredsException {
         try {
             restApi.path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/datastreams/")
                     .path(URLEncoder.encode(datastream,"UTF-8"))
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
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     public String getXMLDatastreamContents(String pid, String datastream)
             throws BackendMethodFailedException, BackendInvalidCredsException {
         try {
             String contents = restApi.path(URLEncoder.encode(pid, "UTF-8"))
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
             } else {
                 throw new BackendMethodFailedException("Server error", e);
             }
         }
     }
 
     public void addRelation(String pid, String subject, String property, String object)
             throws BackendMethodFailedException, BackendInvalidCredsException {
         try {
             restApi.path(URLEncoder.encode(pid, "UTF-8"))
                     .path("/relationships/new")
                     .queryParam("subject",subject)
                     .queryParam("predicate",property)
                     .queryParam("object",object)
                     .header("Authorization", credsAsBase64())
                     .post();
         } catch (UnsupportedEncodingException e) {
             throw new BackendMethodFailedException("UTF-8 not known....", e);
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
 
     public List<String> listObjectsWithThisLabel(String label)
             throws BackendInvalidCredsException, BackendMethodFailedException {
         //TODO sanitize label
         try {
             String query = "select $object\n"
                            + "from <#ri>\n"
                            + "where $object <fedora-model:label> '"+label+"'";
             String objects = client.resource(location)
                     .path("/risearch")
                     .queryParam("type", "tuples")
                     .queryParam("lang", "iTQL")
                     .queryParam("format", "CSV")
                     .queryParam("query", query)
                    .header("Authorization", credsAsBase64())
                     .post(String.class);
             String[] lines = objects.split("\n");
             List<String> foundobjects = new ArrayList<String>();
             for (String line : lines) {
                 if (line.startsWith("\"")){
                     continue;
                 }
                 if (line.startsWith("info:fedora/")){
                     line = line.substring("info:fedora/".length());
                 }
                 foundobjects.add(line);
             }
             return foundobjects;
         }  catch (UniformInterfaceException e) {
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
 }
