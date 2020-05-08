 /*
  * Copyright (c) 2010 Google Inc., 2011 Josh Geenen
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.pirules.gcontactsync.android.model;
 
 import com.google.api.client.googleapis.GoogleUrl;
 import com.google.api.client.googleapis.xml.atom.AtomPatchRelativeToOriginalContent;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Data;
 import com.google.api.client.util.Key;
 import com.google.api.client.http.xml.atom.AtomContent;
 
 import org.pirules.gcontactsync.android.util.RedirectHandler;
 import org.pirules.gcontactsync.android.util.Util;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author Yaniv Inbar, Josh Geenen
  */
 public class Entry implements Cloneable {
 
   @Key
   public String id;
 
   @Key
   public String updated;
 
   @Key
   public String title;
 
   @Key("link")
   public List<Link> links;
 
   @Override
   protected Entry clone() {
    return Data.clone(this);
   }
 
   public void executeDelete(HttpTransport transport) throws IOException {
     HttpRequest request = transport.buildDeleteRequest();
     request.setUrl(getEditLink());
     RedirectHandler.execute(request).ignore();
   }
 
   protected Entry executeInsert(HttpTransport transport, GoogleUrl url) throws IOException {
     HttpRequest request = transport.buildPostRequest();
     request.url = url;
     AtomContent content = new AtomContent();
     content.namespaceDictionary = Util.DICTIONARY;
     content.entry = this;
     request.content = content;
     return RedirectHandler.execute(request).parseAs(getClass());
   }
 
   protected Entry executePatchRelativeToOriginal(HttpTransport transport, Entry original) throws IOException {
     HttpRequest request = transport.buildPatchRequest();
     request.setUrl(getEditLink());
     AtomPatchRelativeToOriginalContent content = new AtomPatchRelativeToOriginalContent();
     content.namespaceDictionary = Util.DICTIONARY;
     content.originalEntry = original;
     content.patchedEntry = this;
     request.content = content;
     return RedirectHandler.execute(request).parseAs(getClass());
   }
 
   private String getEditLink() {
     return Link.find(links, "edit");
   }
   
   protected String getSelfLink() {
     return Link.find(links, "self");
   }
 }
