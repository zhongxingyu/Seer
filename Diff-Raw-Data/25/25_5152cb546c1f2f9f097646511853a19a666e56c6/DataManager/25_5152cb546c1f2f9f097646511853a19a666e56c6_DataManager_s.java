 package com.xpn.xwiki.watch.client.data;
 
 import com.xpn.xwiki.watch.client.Watch;
 import com.xpn.xwiki.watch.client.FilterStatus;
 import com.xpn.xwiki.watch.client.Constants;
 import com.xpn.xwiki.watch.client.Feed;
 import com.xpn.xwiki.gwt.api.client.app.ModalMessageDialogBox;
 import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
 import com.xpn.xwiki.gwt.api.client.XWikiService;
 import com.xpn.xwiki.gwt.api.client.XObject;
 import com.xpn.xwiki.gwt.api.client.Document;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.*;
 
 import org.gwtwidgets.client.util.SimpleDateFormat;
 
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
 
 public class DataManager {
     protected Watch watch;
     protected boolean useLucene;
 
     public DataManager(Watch watch) {
         this.watch = watch;
     }
 
     public void addFeed(final Feed feed, final AsyncCallback cb) {
         if (feed==null)
             cb.onFailure(null);
 
         final String feedName = feed.getName();
         final String feedURL = feed.getUrl();
         final List feedGroups = feed.getGroups();
         watch.getXWikiServiceInstance().getUniquePageName(watch.getWatchSpace(), feedName, new XWikiAsyncCallback(watch) {
             public void onFailure(Throwable caught) {
                 super.onFailure(caught);
                 // We failed to get a unique page name
                 // This should not happen
                 cb.onFailure(caught);
             }
 
             public void onSuccess(Object result) {
                 super.onSuccess(result);
                 // Construct the full page name
                 final String pageName = watch.getWatchSpace() + "." + result;
                 XObject feedObj = new XObject();
                 feedObj.setName(pageName);
                 feedObj.setClassName(Constants.CLASS_AGGREGATOR_URL);
                 feedObj.setNumber(0);
                 feedObj.setProperty(Constants.PROPERTY_AGGREGATOR_URL_NAME, feedName);
                 feedObj.setProperty(Constants.PROPERTY_AGGREGATOR_URL_URL, feedURL);
                 feedObj.setProperty(Constants.PROPERTY_AGGREGATOR_URL_GROUPS, feedGroups);
                 watch.getXWikiServiceInstance().saveObject(feedObj, new AsyncCallback() {
                     public void onFailure(Throwable throwable) {
                         cb.onFailure(throwable);
                     }
 
                     public void onSuccess(Object object) {
                         //save the content of the feed -- should make sure that the sheet exists
                         String feedDefaultContent = "#includeForm(\"" 
                                 + Constants.DEFAULT_SHEETS_SPACE + "." + Constants.SHEET_FEED + "\")";
                         watch.getXWikiServiceInstance().saveDocumentContent(pageName, feedDefaultContent, 
                             new XWikiAsyncCallback(watch) {
                                 public void onFailure(Throwable throwable) {
                                     cb.onFailure(throwable);
                                 }
                                 public void onSuccess(Object object) {
                                     super.onSuccess(object);
                                     // We return the page name
                                     if (!((Boolean)object).booleanValue())
                                         cb.onFailure(null);
                                     else
                                         cb.onSuccess(pageName);                            
                                 }
                         });                        
                     }
                 });                
             }
         });
     }
 
     /**
      * Test if a feed aggregator with the specified name exists (case-insensitive).
      * The response is returned in the <tt>onSuccess(Object o)</tt> function of the passed callback:
      * {link@Boolean#TRUE} if the feed exists and {link@Boolean#FALSE} otherwise.
      * The invocation of {link@AsyncCallback#onFailure} for the passed <tt>cb</tt> means that
      * an error occurred and the test could not be completed.
      *
      * @param feedName the name of the feed to search for
      * @param cb callback to return the response
      */
     public void existsFeed(String feedName, final AsyncCallback cb) {
         if (feedName == null || feedName.equals("")) {
             cb.onFailure(null);
             return;
         }
 
        String feedNameEscaped = feedName.replaceAll("'", "''");
         //create the query
         final String feedQuery = ", BaseObject as obj, XWiki.AggregatorURLClass as feed "
            + "where doc.fullName=obj.name and obj.className='XWiki.AggregatorURLClass' "
            + "and obj.id=feed.id and lower(feed.name) = lower('" + feedNameEscaped + "')";
 
         watch.getXWikiServiceInstance().searchDocuments(feedQuery, 1, 0, new AsyncCallback() {
             public void onFailure(Throwable throwable)
             {
                 //didn't manage to get a response from the server
                 cb.onFailure(throwable);
             }
 
             public void onSuccess(Object o)
             {
                 //check the response from the server
                 List docList = (List)o;
                 if (docList.size() > 0) {
                     cb.onSuccess(Boolean.TRUE);
                 } else {
                     cb.onSuccess(Boolean.FALSE);
                 }
             }
         });
     }
 
 
     /**
      * Test if a group with the specified name exists (case-insensitive).
      * The response is returned in the <tt>onSuccess(Object o)</tt> function of the passed callback:
      * {link@Boolean#TRUE} if the group exists and {link@Boolean#FALSE} otherwise.
      * The invocation of {link@AsyncCallback#onFailure} for the passed <tt>cb</tt> means that
      * an error occurred and the test could not be completed.
      *
      * @param groupName the name of the group to search for
      * @param cb callback to return the response
      */
     public void existsGroup(String groupName, final AsyncCallback cb) {
         if (groupName == null || groupName.equals("")) {
             cb.onFailure(null);
             return;
         }
 
        String groupNameEscaped = groupName.replaceAll("'", "''");
         //create the query
         final String groupQuery = ", BaseObject as obj, XWiki.AggregatorGroupClass as groupObj "
            + "where doc.fullName=obj.name and obj.className='XWiki.AggregatorGroupClass' "
            + "and obj.id=groupObj.id and lower(groupObj.name) = lower('" + groupNameEscaped + "')";
 
         watch.getXWikiServiceInstance().searchDocuments(groupQuery, 1, 0, new AsyncCallback() {
             public void onFailure(Throwable throwable)
             {
                 //didn't manage to get a response from the server
                 cb.onFailure(throwable);
             }
 
             public void onSuccess(Object o)
             {
                 //check the response from the server
                 List docList = (List)o;
                 if (docList.size() > 0) {
                     cb.onSuccess(Boolean.TRUE);
                 } else {
                     cb.onSuccess(Boolean.FALSE);
                 }
             }
         });
     }
 
     /**
      * Test if the specified keyword with the specified group exists (case-insensitive).
      * The response is returned in the <tt>onSuccess(Object o)</tt> function of the passed callback:
      * {link@Boolean#TRUE} if the keyword exists and {link@Boolean#FALSE} otherwise.
      * The invocation of {link@AsyncCallback#onFailure} for the passed <tt>cb</tt> means that
      * an error occurred and the test could not be completed.
      *
      * @param keyword the name of the feed to search for
      * @param group the name of the group of the keyword
      * @param cb callback to return the response
      */    
     public void existsKeyword(String keyword, String group, final AsyncCallback cb) {
         if (keyword == null || keyword.equals("")) {
             cb.onFailure(null);
             return;       
         }
 
         String groupEscaped = (group == null) ? "" : group.trim().replaceAll("'", "''");
 
         String keywordEscaped = keyword.trim().replaceAll("'", "''");
 
         String keywordQuery = ", BaseObject as obj, XWiki.KeywordClass as kwObj "
            + "where doc.fullName=obj.name and obj.className='XWiki.KeywordClass' "
            + "and obj.id=kwObj.id and lower(kwObj.name) = lower('" + keywordEscaped + "') "
            + "and lower(trim(kwObj.group)) = lower(trim('" + groupEscaped + "'))";
 
         watch.getXWikiServiceInstance().searchDocuments(keywordQuery, 1, 0, new AsyncCallback() {
             public void onFailure(Throwable throwable)
             {
                 //didn't manage to get a response from the server
                 cb.onFailure(throwable);
             }
 
             public void onSuccess(Object o)
             {
                 //check the response from the server
                 List docList = (List)o;
                 if (docList.size() > 0) {
                     cb.onSuccess(Boolean.TRUE);
                 } else {
                     cb.onSuccess(Boolean.FALSE);
                 }
             }
         });
     }
 
     public void updateFeed(Feed feed, final XWikiAsyncCallback cb) {
         if ((feed==null)||(feed.getPageName()==null)||(feed.getPageName().equals("")))
             cb.onFailure(null);
 
         // Construct the full page name
         final String pageName = feed.getPageName();
         XObject feedObj = new XObject();
         feedObj.setName(pageName);
         feedObj.setClassName(Constants.CLASS_AGGREGATOR_URL);
         feedObj.setNumber(0);
         feedObj.setProperty(Constants.PROPERTY_AGGREGATOR_URL_NAME, feed.getName());
         feedObj.setProperty(Constants.PROPERTY_AGGREGATOR_URL_URL, feed.getUrl());
         feedObj.setProperty(Constants.PROPERTY_AGGREGATOR_URL_GROUPS, feed.getGroups());
         watch.getXWikiServiceInstance().saveObject(feedObj, new AsyncCallback() {
             public void onFailure(Throwable throwable) {
                 cb.onFailure(throwable);
             }
 
             public void onSuccess(Object object) {
                 // We return the page name
                 if (!((Boolean)object).booleanValue())
                     cb.onFailure(null);
                 else
                     cb.onSuccess(pageName);
             }
         });
     }
     
     public void updateGroup(Group group, final XWikiAsyncCallback cb) {
         final String pageName = group.getPageName();
         if (pageName == null || pageName.equals("") || group.getName().equals("")) {
             cb.onFailure(null);
         }
         //create a group object and save it
         XObject groupObj = new XObject();
         groupObj.setName(pageName);
         groupObj.setClassName(Constants.CLASS_AGGREGATOR_GROUP);
         groupObj.setNumber(0);
         groupObj.setProperty(Constants.PROPERTY_GROUP_NAME, group.getName());
         watch.getXWikiServiceInstance().saveObject(groupObj, new AsyncCallback() {
             public void onFailure(Throwable throwable) {
                 cb.onFailure(throwable);
             }
             public void onSuccess(Object object) {
                 // We return the page name
                 if (!((Boolean)object).booleanValue()) {
                     cb.onFailure(null);
                 } else {
                     cb.onSuccess(pageName);
                 }
             }
         });
     }
     
     public void updateKeyword(Keyword keyword, final XWikiAsyncCallback cb) {
         final String pageName = keyword.getPageName();
         XObject kwObject = new XObject();
         kwObject.setName(pageName);
         kwObject.setClassName(Constants.CLASS_AGGREGATOR_KEYWORD);
         kwObject.setNumber(0);
         kwObject.setProperty(Constants.PROPERTY_KEYWORD_NAME, keyword.getName());
         kwObject.setProperty(Constants.PROPERTY_KEYWORD_GROUP, keyword.getGroup());
         watch.getXWikiServiceInstance().saveObject(kwObject, new AsyncCallback() {
             public void onFailure(Throwable throwable) {
                 cb.onFailure(throwable);
             }
 
             public void onSuccess(Object object) {
                 // We return the page name
                 if (!((Boolean)object).booleanValue())
                     cb.onFailure(null);
                 else
                     cb.onSuccess(pageName);
             }
         });        
     }
 
     public void addKeyword(final Keyword keyword, final AsyncCallback cb) {
         if (keyword==null)
             cb.onFailure(null);
 
         watch.getXWikiServiceInstance().getUniquePageName(watch.getWatchSpace(), 
                 "Keyword_" + keyword.getName(), new XWikiAsyncCallback(watch) {
             public void onFailure(Throwable caught) {
                 super.onFailure(caught);
                 // We failed to get a unique page name
                 // This should not happen
                 cb.onFailure(caught);
             }
 
             public void onSuccess(Object result) {
                 super.onSuccess(result);
                 // Construct the full page name
                 final String pageName = watch.getWatchSpace() + "." + result;
                 XObject feedObj = new XObject();
                 feedObj.setName(pageName);
                 feedObj.setClassName(Constants.CLASS_AGGREGATOR_KEYWORD);
                 feedObj.setNumber(0);
                 feedObj.setProperty(Constants.PROPERTY_KEYWORD_NAME, keyword.getName());
                 feedObj.setProperty(Constants.PROPERTY_KEYWORD_GROUP, keyword.getGroup());
                 watch.getXWikiServiceInstance().saveObject(feedObj, new AsyncCallback() {
                     public void onFailure(Throwable throwable) {
                         cb.onFailure(throwable);
                     }
 
                     public void onSuccess(Object object) {
                         //save the content of the keyword -- should make sure that the sheet exists
                         String keywordDefaultContent = "#includeForm(\"" 
                                 + Constants.DEFAULT_SHEETS_SPACE + "." + Constants.SHEET_KEYWORD + "\")";
                         watch.getXWikiServiceInstance().saveDocumentContent(pageName, keywordDefaultContent, 
                             new XWikiAsyncCallback(watch) {
                                 public void onFailure(Throwable throwable) {
                                     cb.onFailure(throwable);
                                 }
                                 public void onSuccess(Object object) {
                                     super.onSuccess(object);
                                     // We return the page name
                                     if (!((Boolean)object).booleanValue())
                                         cb.onFailure(null);
                                     else
                                         cb.onSuccess(pageName);                            
                                 }
                         });
                     }
                 });
             }
         });
     }
 
     public void addGroup(final Group group, final AsyncCallback cb) {
         if (group==null)
             cb.onFailure(null);
 
         watch.getXWikiServiceInstance().getUniquePageName(watch.getWatchSpace(), 
                 "Group_" + group.getName(), new XWikiAsyncCallback(watch) {
             public void onFailure(Throwable caught) {
                 super.onFailure(caught);
                 // We failed to get a unique page name
                 // This should not happen
                 cb.onFailure(caught);
             }
 
             public void onSuccess(Object result) {
                 super.onSuccess(result);
                 // Construct the full page name
                 final String pageName = watch.getWatchSpace() + "." + result;
                 XObject feedObj = new XObject();
                 feedObj.setName(pageName);
                 feedObj.setClassName(Constants.CLASS_AGGREGATOR_GROUP);
                 feedObj.setNumber(0);
                 feedObj.setProperty(Constants.PROPERTY_GROUP_NAME, group.getName());
                 watch.getXWikiServiceInstance().saveObject(feedObj, new AsyncCallback() {
                     public void onFailure(Throwable throwable) {
                         cb.onFailure(throwable);
                     }
                     public void onSuccess(Object object) {
                         //save the content of the group -- should make sure that the sheet exists
                         String groupDefaultContent = "#includeForm(\"" 
                                 + Constants.DEFAULT_SHEETS_SPACE + "." + Constants.SHEET_GROUP + "\")";
                         watch.getXWikiServiceInstance().saveDocumentContent(pageName, groupDefaultContent, 
                             new XWikiAsyncCallback(watch) {
                                 public void onFailure(Throwable throwable) {
                                     cb.onFailure(throwable);
                                 }
                                 public void onSuccess(Object object) {
                                     super.onSuccess(object);
                                     // We return the page name
                                     if (!((Boolean)object).booleanValue())
                                         cb.onFailure(null);
                                     else
                                         cb.onSuccess(pageName);                            
                                 }
                         });
                     }
                 });
                 
             }
         });
     }
 
     public void removeFeed(Feed feed, final boolean deleteArticles, final AsyncCallback cb) {
         //create the articles query for the current feed
         String feedUrl = feed.getUrl();
         String feedUrlEscaped = feedUrl.replaceAll("'", "''");
         final String articlesQuery = ", BaseObject as obj, XWiki.FeedEntryClass as feedentry "
                 + "where doc.fullName=obj.name and obj.className='XWiki.FeedEntryClass' "
                 + "and obj.id=feedentry.id and feedentry.feedurl = '" + feedUrlEscaped + "'";
         try {
             if ((feed.getPageName()==null)||(feed.getPageName().equals("")))
                 cb.onFailure(null);
 
             watch.getXWikiServiceInstance().deleteDocument(feed.getPageName(), new XWikiAsyncCallback(watch) {
                 public void onFailure(Throwable caught) {
                     super.onFailure(caught);
                     cb.onFailure(caught);
                 }
 
                 public void onSuccess(final Object result) {
                     //if the articles need to be deleted
                     super.onSuccess(result);
                     //check the response from the server
                     if (((Boolean)result).booleanValue()) {
                         if (deleteArticles) {
                             watch.getXWikiServiceInstance()
                                     .deleteDocuments(articlesQuery, new XWikiAsyncCallback(watch) {
                                         public void onFailure(Throwable throwable)
                                         {
                                             super.onFailure(throwable);
                                             cb.onFailure(throwable);
                                         }
 
                                         public void onSuccess(Object o)
                                         {
                                             super.onSuccess(o);
                                             cb.onSuccess(result);
                                         }
                                     });
                         } else {
                             cb.onSuccess(result);
                         }
                     } else {
                         cb.onFailure(null);
                     }                    
                 }
             });
         } catch(Exception e) {
             cb.onFailure(e);
         }
     }
 
     public void removeGroup(Group group, final AsyncCallback cb) {
         try {
             if ((group==null)||(group.equals(""))) {
                 cb.onFailure(null);
                 return;
             }
             //first update feeds in this group to unset the group
             //TODO: decide what to do if one update fails
             Collection feedList = (Collection)((Map)watch.getConfig().getFeedsByGroupList()
                                   .get(group.getPageName())).values();
  	   	    for (Iterator feedListIt = feedList.iterator(); feedListIt.hasNext();) {
  	   	        Feed currentFeed = (Feed)feedListIt.next();
  	   	        currentFeed.getGroups().remove(group.getPageName());
  	   	        this.updateFeed(currentFeed, new XWikiAsyncCallback(watch) {
  	   	            public void onSuccess(Object result) {
  	   	                super.onSuccess(result);
  	   	            }
  	   	            public void onFailure(Throwable caught) {
  	   	                super.onFailure(caught);
  	   	            }
  	   	        });
  	   	    }
             
             //update the keywords for this group and delete them
             List keywords = watch.getConfig().getKeywords();
             for (Iterator kwIt = keywords.iterator(); kwIt.hasNext();) {
                 Keyword kw = (Keyword)kwIt.next();
                 if (kw.getGroup().equals(group.getPageName())) {
                     //update keyword remove group
                     kw.setGroup("");
                     this.updateKeyword(kw, new XWikiAsyncCallback(watch) {
                         public void onSuccess(Object result) {
                             super.onSuccess(result);
                         }
                         public void onFailure(Throwable caught) {
                             super.onFailure(caught);
                         }
                     });                    
                 }
             }
             
  	   	    //now delete the group
  	   	    watch.getXWikiServiceInstance().deleteDocument(group.getPageName(), 
  	   	            new XWikiAsyncCallback(watch) {
                 public void onFailure(Throwable caught) {
                     super.onFailure(caught);
                     cb.onFailure(caught);
                 }
 
                 public void onSuccess(Object result) {
                     super.onSuccess(result);
                     cb.onSuccess(result);
                 }
             });
         } catch(Exception e) {
             cb.onFailure(e);
         }
     }
 
     public void removeKeyword(Keyword keyword, final AsyncCallback cb) {
         String pageName = keyword.getPageName(); 
         try {
             if ((pageName == null) || (pageName.equals(""))) {
                 cb.onFailure(null);
             }
 
             watch.getXWikiServiceInstance().deleteDocument(pageName, new XWikiAsyncCallback(watch) {
                 public void onFailure(Throwable caught) {
                     super.onFailure(caught);
                     cb.onFailure(caught);
                 }
 
                 public void onSuccess(Object result) {
                     super.onSuccess(result);
                     cb.onSuccess(result);
                 }
             });
         } catch(Exception e) {
             cb.onFailure(e);
         }
     }
 
     public void addComment(FeedArticle article, String text, final AsyncCallback cb) {
         watch.getXWikiServiceInstance().addComment(article.getPageName(), text, new XWikiAsyncCallback(watch) {
             public void onFailure(Throwable throwable) {
                 super.onFailure(throwable);
                 cb.onFailure(throwable);
             }
             public void onSuccess(Object object) {
                 super.onSuccess(object);
                 cb.onSuccess(object);
             }
         });
     }
 
     public void getFeedList(final XWikiAsyncCallback cb) {
         watch.getXWikiServiceInstance().getDocuments(", BaseObject as obj where doc.fullName=obj.name and obj.className in ('XWiki.AggregatorURLClass','XWiki.AggregatorGroupClass', 'XWiki.KeywordClass') and doc.web='" + watch.getWatchSpace() + "'",
                 0, 0, true, true, false, cb);
     }
 
 
     public void getArticles(FilterStatus filterStatus, int nb, int start, final AsyncCallback cb) {
         try {
             String sql = prepareSQLQuery(filterStatus);
             watch.getXWikiServiceInstance().getDocuments(sql, nb, start, true, true, false, cb);
         } catch(Exception e) {
             cb.onFailure(e);
         }
     }
 
     private String prepareSQLQuery(FilterStatus filterStatus) {
         String skeyword = (filterStatus.getKeyword() ==null) ? null : filterStatus.getKeyword().replaceAll("'", "''");
         String sql = ", BaseObject as obj, XWiki.FeedEntryClass as feedentry ";
         String wheresql = "where doc.fullName=obj.name and obj.className='XWiki.FeedEntryClass' and obj.id=feedentry.id ";
 
         if ((filterStatus.getTags() !=null)&&(filterStatus.getTags().size()>0)) {
             for(int i=0;i< filterStatus.getTags().size();i++) {
                 String tag = (String) filterStatus.getTags().get(i);
                 wheresql += " and '" + tag.replaceAll("'","''") + "' in elements(feedentry.tags) ";
             }
         }
 
         if ((skeyword != null) && (!skeyword.trim().equals(""))) {
             wheresql  += " and (lower(feedentry.title) like '%" + skeyword.toLowerCase() + "%' "
                     + " or lower(feedentry.content) like '%" + skeyword.toLowerCase() + "%' "
                     + " or lower(feedentry.fullContent) like '%" + skeyword.toLowerCase() + "%') ";
         }
 
         if (filterStatus.getFlagged() ==1) {
             wheresql += " and feedentry.flag=1";
         } else if ((filterStatus.getFlagged() ==-1)&&(filterStatus.getTrashed() ==-1)) {
             wheresql += " and (feedentry.flag=0 or feedentry.flag is null)";
         } else if (filterStatus.getTrashed() ==1) {
             wheresql += " and feedentry.flag=-1";
         } else if (filterStatus.getTrashed() ==-1) {
             wheresql += " and (feedentry.flag>-1 or feedentry.flag is null)";
         } else if (filterStatus.getFlagged() ==-1) {
             wheresql += " and (feedentry.flag<1 or feedentry.flag is null)";
         }
 
         Feed feed = filterStatus.getFeed();
         String feedurl = (feed==null) ? null : feed.getUrl();
         if ((feedurl !=null)&&(!feedurl.trim().equals(""))) {
             wheresql += " and feedentry.feedurl='" + feedurl.replaceAll("'","''") + "'";
         } else if ((filterStatus.getGroup() !=null)&&(!filterStatus.getGroup().trim().equals(""))) {
             wheresql += "and feedentry.feedurl in ("
                 + "select feed.url from XWiki.AggregatorURLClass as feed where '" + filterStatus.getGroup().replaceAll("'","''") + "' in elements(feed.group))";
         }
 
         if (filterStatus.getDateStart() !=null) {
             //format date
             SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
             String sdate = format.format(filterStatus.getDateStart());
             wheresql += " and feedentry.date >= '" + sdate + "' ";
         } else {
             if ("1".equals(watch.getParam("withdatelimit"))) {
                 Date date = new Date();
                 date = new Date(date.getTime() - 3 * 24 * 60 * 60 * 1000);
                 SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
                 String sdate = format.format(date);
                 wheresql += " and feedentry.date >= '" + sdate + "' ";
             }
         }
 
         if (filterStatus.getDateEnd() != null) {
             SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
             String sdate = format.format(filterStatus.getDateEnd());
             wheresql += " and feedentry.date < '" + sdate + "'";
         }
 
         if (filterStatus.getRead() ==1) {
             wheresql += " and feedentry.read=1";
         } else if (filterStatus.getRead() ==-1) {
             wheresql += " and (feedentry.read is null or feedentry.read=0)";
         }
 
         sql += wheresql + " and doc.web='" + watch.getWatchSpace() + "' order by feedentry.date desc";
         return sql;
     }
 
     /**
      * Retrieves one article from the server
      * @param pageName
      * @param cb
      * @return
      */
     public void getArticle(String pageName, final AsyncCallback cb) {
         watch.getXWikiServiceInstance().getDocument(pageName, true, true, false, new XWikiAsyncCallback(watch) {
             public void onFailure(Throwable caught) {
                 super.onFailure(caught);
                 cb.onFailure(caught);
             }
 
             public void onSuccess(Object result) {
                 super.onSuccess(result);
                 // We encapsulate the result in a FeedArticle object
                 FeedArticle article = new FeedArticle((Document) result);
                 cb.onSuccess(article);
             }
         });
     }
 
     public void updateTags(FeedArticle article, String tags, AsyncCallback cb) {
         List taglist = new ArrayList();
         tags = (tags==null) ? "" : tags;
         String[] tagarray = tags.split(" ");
         for (int i=0;i<tagarray.length;i++)
             taglist.add(tagarray[i]);
         watch.getXWikiServiceInstance().updateProperty(article.getPageName(), "XWiki.FeedEntryClass", "tags", taglist, cb);
     }
 
     public void getTagsList(AsyncCallback cb) {
         Map params = new HashMap();
         params.put("space", watch.getWatchSpace());
         watch.getXWikiServiceInstance().customQuery(Constants.DEFAULT_QUERIES_SPACE + "." + Constants.QUERY_PAGE_TAGSLIST, params, 0, 0, cb);
     }
 
     public void getNewArticles(AsyncCallback cb) {
         Map params = new HashMap();
         params.put("space", watch.getWatchSpace());
         watch.getXWikiServiceInstance().customQuery(Constants.DEFAULT_QUERIES_SPACE + "." + Constants.QUERY_PAGE_NEWARTICLES, params, 0, 0, cb);
     }
 
     public void getArticleCount(AsyncCallback cb) {
         Map params = new HashMap();
         params.put("space", watch.getWatchSpace());
         watch.getXWikiServiceInstance().customQuery(Constants.DEFAULT_QUERIES_SPACE + "." + Constants.QUERY_PAGE_ARTICLENUMBER, params, 0, 0, cb);
     }
 
     public void updateArticleFlagStatus(FeedArticle article, int newflagstatus, XWikiAsyncCallback cb) {
         watch.getXWikiServiceInstance().updateProperty(article.getPageName(), "XWiki.FeedEntryClass", "flag", newflagstatus, cb);
     }
 
     public void updateArticleReadStatus(FeedArticle article, AsyncCallback cb) {
         watch.getXWikiServiceInstance().updateProperty(article.getPageName(), "XWiki.FeedEntryClass", "read", 1, cb);
     }
 
     public void getAnalysisHTML(FilterStatus filterStatus, String language, AsyncCallback cb) {
         Map map = filterStatus.getMap();
         map.put("space", watch.getWatchSpace());
         if (language != null && !language.trim().equals("")) {
             map.put("filterlang", language);
         }
         watch.getXWikiServiceInstance().getDocumentContent(Constants.DEFAULT_CODE_SPACE + "." + Constants.PAGE_TAGCLOUD, true, map, cb);
     }
 
     public void getPressReview(FilterStatus filterStatus, String pressReviewPage, AsyncCallback cb) {
         Map map = filterStatus.getMap();
         map.put("space", watch.getWatchSpace());
         watch.getXWikiServiceInstance().getDocumentContent(pressReviewPage, true, map, cb);
     }
 
     public void sendEmail(FilterStatus filterStatus, String sendEmailPage, String mailSubject, 
                           String[] mailTo, String mailContent, boolean withArticlesContent, 
                           boolean withArticlesComments, AsyncCallback cb) {
         Map map = filterStatus.getMap();
         map.put("space", watch.getWatchSpace());
         map.put("address", mailTo);
         map.put("subject", mailSubject);
         map.put("content", mailContent);
         if (withArticlesContent) {
             map.put("withcontent", "1");
         } else {
             map.put("withcontent", "0");
         }
         if (withArticlesComments) {
             map.put("withcomments", "1");
         } else {
             map.put("withcomments", "0");
         }
         watch.getXWikiServiceInstance().getDocumentContent(sendEmailPage, true, map, cb);
     }
 }
