 /*
  * Copyright (c) 2012, Warner Onstine and Leo Przybylski
  * All rights reserved.
  * 
  * - Redistribution and use in source and binary forms, with or without modification, 
  * are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this list of 
  * conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice, this list 
  * of conditions and the following disclaimer in the documentation and/or other materials 
  * provided with the distribution.
  * 
  * - Neither the name of the <ORGANIZATION> nor the names of its contributors may be used
  * to endorse or promote products derived from this software without specific prior written 
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
  * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.clearboxmedia.couchspring.test.spring;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.jcouchdb.db.Database;
 import org.jcouchdb.document.ValueRow;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.client.RestTemplate;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.ResourceLoaderAware;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.ResourceLoader;
 
 import org.springframework.stereotype.Component;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.util.logging.Logger;
 
 /**
  *
  */
 @Component
 public class CouchDbLoader implements InitializingBean, ResourceLoaderAware {
     public static final Logger LOG = Logger.getLogger(CouchDbLoader.class.getSimpleName());
 
     @Autowired
     private Database database;
 
     private ResourceLoader resourceLoader;
     
     @Autowired
     private RestTemplate restTemplate;
 
     public RestTemplate getRestTemplate() {
         return this.restTemplate;
     }
 
     protected void setRestTemplate(final RestTemplate restTemplate) {
         this.restTemplate = restTemplate;
     }
 
     public void setResourceLoader(final ResourceLoader resourceLoader) {
         this.resourceLoader = resourceLoader;
     }
 
     public ResourceLoader getResourceLoader() {
         return resourceLoader;
     }
 
     public void setDatabase(final Database database) {
         this.database = database;
     }
 
     public Database getDatabase() {
         return this.database;
     }
 
     protected boolean isEvent(final ValueRow<Map> row) {
        final String docType = ((String) row.getValue().get("docType"));
        return docType != null && docType.equalsIgnoreCase("event");
     }
 
    /**
      * Read json data from a file and load it into the remote couch database.
      *
      */
     public void afterPropertiesSet()  throws Exception {
         LOG.warning("Clearing database");
 
         final List toDelete = new ArrayList();
         for (final ValueRow<Map> row : getDatabase().listDocuments(null, null).getRows()) {
             if (isEvent(row)) {
                 toDelete.add(row.getValue());
             }
         }
         getDatabase().bulkDeleteDocuments(toDelete);
 
         LOG.warning("Loading data");
         final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
         final Map<String,Object> eventData = mapper.readValue(getResourceLoader().getResource("classpath:json_data/events.json").getInputStream(), Map.class);
         
         getDatabase().bulkCreateDocuments((List<Map<String,Object>>) eventData.get("rows"), false);
     }
 }
