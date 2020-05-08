 /*
  * Copyright 2013 SciFY NPO <info@scify.org>.
  *
  * This product is part of the NewSum Free Software.
  * For more information about NewSum visit
  * 
  * 	http://www.scify.gr/site/en/our-projects/completed-projects/newsum-menu-en
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * If this code or its output is used, extended, re-engineered, integrated, 
  * or embedded to any extent in another software or hardware, there MUST be 
  * an explicit attribution to this work in the resulting source code, 
  * the packaging (where such packaging exists), or user interface 
  * (where such an interface exists). 
  * The attribution must be of the form "Powered by NewSum, SciFY"
  */ 
 
 
 package org.scify.NewSumServer.Server.NewSumFreeService;
 
 import gr.demokritos.iit.jinsect.storage.INSECTDB;
 import gr.demokritos.iit.jinsect.storage.INSECTFileDBWithDir;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jws.WebService;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import org.scify.NewSumServer.Server.Comms.Communicator;
 import org.scify.NewSumServer.Server.Searching.Indexer;
 import org.scify.NewSumServer.Server.Sources.RSSSources;
 import org.scify.NewSumServer.Server.Storage.IDataStorage;
 import org.scify.NewSumServer.Server.Storage.InsectFileIO;
 import org.scify.NewSumServer.Server.Structures.Article;
 import org.scify.NewSumServer.Server.Structures.Topic;
 import org.scify.NewSumServer.Server.Summarisation.ArticleClusterer;
 import org.scify.NewSumServer.Server.Summarisation.Summariser;
 import org.scify.NewSumServer.Server.Utils.Utilities;
 
 /**
  *
  * @author gkioumis
  */
 @WebService(serviceName = "NewSumFreeService")
 //@Stateless()
 
 /**
  * Executes all client calls and sends the appropriate responses.
  */
 public class NewSumFreeService {
 
     private static HashMap Switches = null;
     protected IDataStorage ids;
     protected RSSSources r;
     protected ArticleClusterer ac;
     protected INSECTDB idb;
     protected Summariser sum;
     protected Indexer ind;
     protected Communicator cm;
 
     public NewSumFreeService() {
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Yahoo! {0}", new File(".").getAbsolutePath());
         Switches = Communicator.getSwitches();
         String sPath = (String) Switches.get("PathToSources");
         Locale loc = sPath.endsWith("GR.txt") ? new Locale("el") : new Locale("en");
         Logger.getAnonymousLogger().log(Level.INFO,
         "Sending {0} messages", sPath.endsWith("EN.txt")?"ENg":"GRe");
         ids = new InsectFileIO((String) Switches.get("BaseDir"));
        r = new RSSSources((String) Switches.get("PathToSources"));
         ac = new ArticleClusterer((ArrayList<Article>)ids.loadObject("AllArticles", "feeds"),
             ids, (String) Switches.get("ArticlePath"));
         idb = new INSECTFileDBWithDir((String) Switches.get("SummaryPath"), "");
         sum = new Summariser(new HashSet<Topic>(
                     ids.readClusteredTopics().values()), idb);
         ind = new Indexer((String) Switches.get("ArticlePath"),
                 (String) Switches.get("indexPath"), loc);
         cm = new Communicator(ids, ac, sum, ind);
     }
     /**
      * @deprecated
      */
     @WebMethod(operationName = "getCategorySources")
     public String getCategorySources(@WebParam(name = "sCategory") String sCategory) {
         return cm.getCategorySources(sCategory);
     }
 
     /**
      * Traverses the User Sources preference and returns the Categories that
      * correspond to these Sources. <p>The Categories are packed in the
      * format <br>Cat1-FLS-Cat2-FLS, etc.</p>
      * @param sUserSources The user selected Sources. if "All", all sources
      * are considered valid
      * @return the categories that correspond to the specified user sources
      */
     @WebMethod(operationName = "getCategories")
     public String getCategories(@WebParam(name = "sUserSources") String sUserSources) {
         String sCats = cm.getCategories(sUserSources);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Returning sources {0}", sCats);
         return sCats;
     }
 
     /**
      * Returns the Topics that correspond to the specified category and user
      * sources. The Topics are packed in the format: <p>
      * TopicID-SLS-TopicTitle-SLS-TopicDate-FLS-TopicID-SLS-TopicTitle-SLS-
      * TopicDate</p> where <br>FLS is the First Level Separator, and <br>SLS is
      * the Second Level Separator.<p>The Topics are sorted first of all by date,
      * and secondly by each Topic's article count for the specific date. 
      * @param sUserSources the user sources preference
      * @param sCategory the category of interest
      * @return the Topics that correspond to the specified category and
      * user sources.
      */
     @WebMethod(operationName = "getTopicTitles")
     public String getTopicTitles(@WebParam(name = "sUserSources") String sUserSources, @WebParam(name = "sCategory") String sCategory) {
 //        String sTopicTitles = cm.getTopicTitles(sUserSources, sCategory); // version Beta
         // version 1.0
         String sTopicTitles = cm.getTopics(sUserSources, sCategory);
 //        String sTopicTitles = cm.getTopicTitlesSorted(sUserSources, sCategory);
 //            cm.checkTopicTitles(sUserSources, sCategory);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Got Category {0}", sCategory);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Got User Sources {0}", sUserSources);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Returning Titles {0}", sTopicTitles);
         return sTopicTitles;
     }
 
     /**
      * Returns the summary for the specified ID. <p>The summary is packed in the
      * format: Object-FLS-Object-FLS-Object <br>where<br>FLS is the First Level
      * Separator.(@see {@link #getFirstLevelSeparator() })
      * <br>The First object of the summary is the data containing the
      * sources with their corresponding labels, i.e. if the summary is derived 
      * from two sources, the first object will be like <br>Source-TLS-Label-SLS-
      * Source-TLS-Label<br>The remaining objects are the summary contents, i.e. if
      * the summary contains only two sentences, it will be like <br>Sentence-SLS
      * - SourceLink-SLS-feed-SLS-Label<br>FLS<br>Sentence-SLS-SourceLink-SLS-feed-SLS
      * -Label<br></p><p>Sentence is a specific snippet of a topic,
      * <br>SourceLink is the URL Link to the article that the snippet comes from,
      * <br>feed is the RSS feed that the snippet was taken from, and <br>Label is
      * the label for the specified SourceLink. <br>Likewise, 
      * </p><p>SLS corresponds to the Second Level Separator, @see
      * {@link #getSecondLevelSeparator() },and<br>TLS to the Third Level
      * Separator (@see {@link #getThirdLevelSeparator() })
      * </p>
      * @param sTopicID The topic ID of interest
      * @param sUserSources The user selected sources. If "All", all sources
      * are considered valid
      * @return The summary that corresponds to the specified ID
      */
     @WebMethod(operationName = "getSummary")
     public String getSummary(@WebParam(name = "sTopicID") String sTopicID, @WebParam(name = "sUserSources") String sUserSources) {
         if (sTopicID == null || sTopicID.equals("") || sTopicID.equals("anyType{}")) {
             return "";
         }
         // debug
         String Title = cm.getTopicTitleByID(sTopicID);
         String str = "Got Topic ID " + sTopicID + " = " + Title;
         Logger.getAnonymousLogger()
             .log(Level.INFO, str);
         // debug
         String summary = cm.getSummary(sTopicID, sUserSources);
         if (summary.contains(cm.getFirstLevelSeparator())) {
             // debug
             String [] aSum = summary.split(cm.getFirstLevelSeparator());
             int count = Utilities.countDiffArticles(aSum);
             Logger.getAnonymousLogger()
                .log(Level.INFO, "Summary From {0} Articles", count);
             Logger.getAnonymousLogger()
                 .log(Level.INFO, "Returning Summary: {0}", summary);
             // debug
             return summary;
         } else if (!summary.equals("")) {
             return summary+cm.getFirstLevelSeparator();
         } else {
             // debug
             Logger.getAnonymousLogger()
                 .log(Level.INFO, "Returning Summary: \" \" {0}", summary);
             // debug
             return summary; // = ""
         }
     }
 
     /**
      * @deprecated
      */
     @WebMethod(operationName = "getTopicTitlesByIDs")
     public String getTopicTitlesByIDs(@WebParam(name = "sTopicIDs") String sTopicIDs) {
         String str = "Got Topic IDs: " + sTopicIDs;
         Logger.getAnonymousLogger()
             .log(Level.INFO, str);
         String Titles = cm.getTopicTitlesByIDs(sTopicIDs);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Returning Topic Titles {0}", Titles);
         return Titles;
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getTopicTitlesByID")
     public String getTopicTitlesByID(@WebParam(name = "sTopicID") String sTopicID) {
         return cm.getTopicTitlesByID(sTopicID);
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getTopicIDs")
     public String getTopicIDs(@WebParam(name = "sUserSources") String sUserSources, @WebParam(name = "sCategory") String sCategory) {
         String sTopicIDs = cm.getTopicIDs(sUserSources, sCategory);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Got Category {0}", sCategory);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Returning Topic IDs {0}", sTopicIDs);
         return sTopicIDs;
     }
 
     /**
      * @deprecated
      */
     @WebMethod(operationName = "getTopicIDsByKeyword")
     public String getTopicIDsByKeyword(@WebParam(name = "sKeyword") String sKeyword, @WebParam(name = "sUserSources") String sUserSources) {
         Logger.getAnonymousLogger().
             log(Level.INFO, "INDEXPATH: {0}", (String) Switches.get("indexPath"));
         String sTopicIDs = cm.getTopicIDsByKeyword(this.ind, sKeyword, sUserSources);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Got Keyword {0}", sKeyword);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Returning Topic IDs {0}", sTopicIDs);
         return sTopicIDs;
     }
 
     /**
      *
      * @return The links (i.e. RSS feeds) and their assigned labels
      */
     @WebMethod(operationName = "getLinkLabels")
     public String getLinkLabels() {
         String linkLabels = cm.getLinkLabelsFromFile();
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Returning LinksLabels {0} ", linkLabels);
         return linkLabels;
     }
 
     /**
      * The First level separator is used for level one unpacking of
      * transferred strings.
      * @return The first level separator
      */
     @WebMethod(operationName = "getFirstLevelSeparator")
     public String getFirstLevelSeparator() {
         return cm.getFirstLevelSeparator();
     }
 
     /**
      * The Second level separator is used for level two unpacking of
      * transferred strings.
      * @return The second level separator
      */
     @WebMethod(operationName = "getSecondLevelSeparator")
     public String getSecondLevelSeparator() {
         return cm.getSecondLevelSeparator();
     }
 
     /**
      * The Third level separator is used for level three unpacking of
      * transferred strings.
      * @return The third level separator
      */
     @WebMethod(operationName = "getThirdLevelSeparator")
     public String getThirdLevelSeparator() {
         return cm.getThirdLevelSeparator();
     }
 
     /**
      * Searches the Topic index for the specified keyword, and returns all topics
      * that correspond to that keyword. The topics are sorted by the number of
      * appearances of the keyword. The Topics are packed in the format: <p>
      * TopicID-SLS-TopicTitle-SLS-TopicDate-FLS-TopicID-SLS-TopicTitle-SLS-
      * TopicDate</p> where <br>FLS is the First Level Separator, and <br>SLS is
      * the Second Level Separator.
      * @param sKeyword the user search entry
      * @param sUserSources the user sources preference. if "All", all sources
      * are considered valid
      * @return the topics found for the specified keyword
      */
     @WebMethod(operationName = "getTopicsByKeyword")
     public String getTopicsByKeyword(@WebParam(name = "sKeyword") String sKeyword, @WebParam(name = "sUserSources") String sUserSources) {
         //TODO write your implementation code here:
         String sTopics = cm.getTopicsByKeyword(this.ind, sKeyword, sUserSources);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Got Keyword {0}", sKeyword);
         Logger.getAnonymousLogger()
             .log(Level.INFO, "Returning Topics {0}", sTopics);
         if (sTopics.contains(cm.getFirstLevelSeparator())) { // if more than one topics found
             return sTopics;
         } else if (sTopics.contains(cm.getSecondLevelSeparator())) { // if only one Topic found
             return sTopics;
         } else { // no topics found
             return "";
         }
     }
 }
