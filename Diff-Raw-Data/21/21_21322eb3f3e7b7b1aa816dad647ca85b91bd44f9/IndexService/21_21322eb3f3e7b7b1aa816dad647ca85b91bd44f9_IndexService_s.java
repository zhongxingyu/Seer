 package com.computas.sublima.app.service;
 
 import com.computas.sublima.query.impl.DefaultSparqlDispatcher;
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 import com.computas.sublima.query.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import com.computas.sublima.query.service.MappingService;
 import com.computas.sublima.app.index.Generate;
 import com.computas.sublima.app.index.GenerateUtils;
 import com.hp.hpl.jena.query.*;
 import com.hp.hpl.jena.query.larq.LARQ;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.shared.DoesNotExistException;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * A class to support Lucene/LARQ indexing in the web app
  * Has methods for creating and accessing indexes
  *
  * @author: mha
  * Date: 13.mar.2008
  */
 public class IndexService {
 
   private Generate gen = new Generate();
   private GenerateUtils gu = new GenerateUtils();
 
   public void createIndex() {
     createResourceIndex();
     createTopicIndex();
   }
 
   public void createResourceIndex() {
     gen.generateIndexForResources(Boolean.valueOf(SettingsService.getProperty("sublima.index.external")), SettingsService.getProperty("sublima.basegraph"), gu.getResourceFreetextFieldsToIndex(), gu.getPrefixes());
   }
 
   public void createTopicIndex() {
     gen.generateIndexForTopics(SettingsService.getProperty("sublima.basegraph"), gu.getTopicFieldsToIndex(), gu.getPrefixes());
   }
 
  public boolean indexResource() {
     return true;
   }
 
  public boolean indexTopic() {
     return true;    
   }
 
 
 
   /**
    * A method to validate all urls on the resources. Adds the URL to the list along with
    * the http code.
    */
 
   public void validateURLs() {
    ResultSet resultSet = null;
    //resultSet = getAllExternalResourcesURLs();
 
     // For each URL, do a HTTP GET and check the HTTP code
     int i = 1;
    while (resultSet.hasNext()) {
      String resultURL = resultSet.next().toString();
      String url = resultURL.substring(10, resultURL.length() - 3).trim();
      //logger.info("SUBLIMA: validateURLs() --> Updating status for resource " + i + " " + url);
       URLActions urlAction = new URLActions(url);
       urlAction.getAndUpdateResourceStatus();
       i++;
     }
   }
 
 
  
 }
