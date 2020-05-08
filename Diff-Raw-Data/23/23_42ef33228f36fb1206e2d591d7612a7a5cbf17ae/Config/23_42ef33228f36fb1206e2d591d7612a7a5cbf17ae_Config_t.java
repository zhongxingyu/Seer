 package com.xpn.xwiki.watch.client;
 
 import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
 import com.xpn.xwiki.gwt.api.client.Document;
 import com.xpn.xwiki.gwt.api.client.XObject;
 import com.xpn.xwiki.watch.client.data.FeedArticle;
 import com.xpn.xwiki.watch.client.data.Keyword;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.*;
 
 /**
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  * <p/>
  * This is free software;you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation;either version2.1of
  * the License,or(at your option)any later version.
  * <p/>
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY;without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
  * Lesser General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software;if not,write to the Free
  * Software Foundation,Inc.,51 Franklin St,Fifth Floor,Boston,MA
  * 02110-1301 USA,or see the FSF site:http://www.fsf.org.
  *
  * @author ldubost
  */
 public class Config {
     private Watch watch;
     private Map feedsList;
     private Map feedsByGroupList;
     private List keywords;
     private Map groups;
     private List articles;
     private boolean lastPage; 
 
     public Config() {
     }
 
     public Config(Watch watch) {
         this.watch = watch;
         clearConfig();
     }
 
     public Map getFeedsList() {
         return feedsList;
     }
 
     public Map getFeedsByGroupList() {
         return feedsByGroupList;
     }
 
     public Map getGroups() {
         return groups;
     }
 
     public List getKeywords() {
         return keywords;
     }
 
     public void clearConfig() {
         feedsList = new HashMap();
         feedsByGroupList = new HashMap();
         keywords = new ArrayList();
         groups = new HashMap();
         articles = new ArrayList();
         this.lastPage = false;
     }
 
     /**
      * Read the feed list, groups and keywords on the server
      */
     public void refreshConfig(final XWikiAsyncCallback cb) {
         watch.getDataManager().getFeedList(new XWikiAsyncCallback(watch) {
             public void onSuccess(Object result) {
                 super.onSuccess(result);
                 List feedDocuments = (List) result;
                 clearConfig();
                 for (int index=0;index<feedDocuments.size();index++) {
                         addToConfig((Document) feedDocuments.get(index));
                 }
                 if (cb!=null)
                  cb.onSuccess(result);
             }
         });
     }
     
     public void refreshArticleList(final AsyncCallback cb) {
         FilterStatus fstatus = watch.getFilterStatus();
         //hack to test the existance of next articles: ask for one more: 
         //if we get it, then we have more articles
         watch.getDataManager().getArticles(fstatus, watch.getArticleNbParam() + 1, fstatus.getStart(), new AsyncCallback() {
             public void onFailure(Throwable caught) {
                 if (cb != null) {
                     cb.onFailure(caught);
                 }
             }
 
             public void onSuccess(Object result) {
                 //test the size of the list
                 List resultList = (List)result;
                 if (resultList.size() == (watch.getArticleNbParam() + 1)) {
                     //we have next
                     lastPage = false;
                 } else {
                     lastPage = true;
                 }
                 //remove the last element if fetched for next
                 if (!lastPage) {
                     resultList.remove(resultList.size() - 1);
                 }
                 //update the article list
                 updateArticleList(resultList);
                 if (cb != null) {
                     cb.onSuccess(result);
                 }
             }
         });
     }
     
     private void updateArticleList(List result) {
         this.articles.clear();
         //update the articles list
         for (Iterator rIt = result.iterator(); rIt.hasNext();) {
             this.articles.add(new FeedArticle((Document)rIt.next()));
         }
     }
 
     private void addToGroup(String group, String groupTitle, Feed feed) {
         Map feeds = (Map) feedsByGroupList.get(group);
         if (feeds == null) {
             feeds = new HashMap();
             feedsByGroupList.put(group, feeds);
         }
         if (feed!=null)
          feeds.put(feed.getName(), feed);
         if (!groups.containsKey(group))
           groups.put(group, groupTitle);
     }
 
     private void addToConfig(Document feedpage) {
         List fobjects = feedpage.getObjects("XWiki.AggregatorURLClass");
         if (fobjects!=null) {
             for (int i=0;i<fobjects.size();i++) {
                 XObject xobj = (XObject) fobjects.get(i);
                 Feed feed = new Feed(xobj);
                 List feedgroups = feed.getGroups();
                 if (feedgroups!=null) {
                     for (int j=0;j<feedgroups.size();j++) {
                         String groupFullName = (String) feedgroups.get(j);
                         addToGroup(groupFullName, groupFullName, feed);
                     }
                 }
                 String all = watch.getTranslation("all");
                 addToGroup(all, all, feed);
                 feedsList.put(feed.getName(), feed);
             }
         }
         List kobjects = feedpage.getObjects("XWiki.KeywordClass");
         if (kobjects!=null) {
             for (int j=0;j<kobjects.size();j++) {
                 XObject xobj = (XObject) kobjects.get(j);
                 Keyword keyword = new Keyword(xobj);
                 keywords.add(keyword);
             }
          }
 
         List gobjects = feedpage.getObjects("XWiki.AggregatorGroupClass");
         if (gobjects!=null) {
             for (int j=0;j<gobjects.size();j++) {
                 XObject xobj = (XObject) gobjects.get(j);
                 String name = (String) xobj.getViewProperty("name");
                 if ((name!=null)&&(!name.equals("")))
                     groups.put(feedpage.getFullName(), name);
             }
          }
 
     }
 
      public void refreshArticleNumber(final AsyncCallback cb) {
          // Load the article counts
         watch.getDataManager().getArticleCount(new AsyncCallback() {
             public void onFailure(Throwable throwable) {
                 if (cb != null) {
                     cb.onFailure(throwable);
                 }
             }
             public void onSuccess(Object result) {
                 // Update the article list with the current results
                 updateArticleNumbers((List) result);
                 //call the cb.onSuccess if cb exists
                 if (cb != null) {
                     cb.onSuccess(result);
                 }
             }
         });
      }
 
     public void updateArticleNumbers(List list) {
         if (list!=null) {
             for (int i=0;i<list.size();i++) {
                 List result = (List) list.get(i);
                //get the values from Strings rather than rely on a specific 
                //type returned (direct object cast causes problems in 
                //1.3 snaphshot due to the long value returned for the 
                //count on position 1) 
                String feedname = (String) result.get(0).toString();
                Integer count = new Integer(result.get(1).toString());
                 Feed feed = (Feed) feedsList.get(feedname);
                 if (feed!=null) {
                    feed.setNb(count);
                 }
             }
         }
         // watch.refreshFeedTreeUI();
     }
 
     public List getArticles()
     {
         return articles;
     }
 
     public boolean isLastPage()
     {
         return lastPage;
     }
 
 }
