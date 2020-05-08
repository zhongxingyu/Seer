 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.blog.plugin;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Api;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.api.Object;
 import com.xpn.xwiki.api.Property;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 public class Article extends Api{
 
   private static Log mLogger = LogFactory.getFactory().getInstance(Article.class);
   
   private Map<String, Object> articleObj;
   private Boolean isSubscribed;
   private Map<String, String> extract;
   private Map<String, Boolean> hasMoreLink;
   private Map<String, Boolean> hasMoreLinkDots;
   private String defaultLang;
   
   public Article(XWikiDocument articleDoc, XWikiContext context) throws XWikiException, EmptyArticleException{
     this(articleDoc.newDocument(context), context);
   }
   
   public Article(Document articleDoc, XWikiContext context) throws XWikiException, EmptyArticleException{
     this(articleDoc.getObjects("XWiki.ArticleClass"), articleDoc.getSpace(), context);
   }
   
   public Article(List<Object> objList, String space, XWikiContext context) throws XWikiException, EmptyArticleException{
     super(context);
     for (Iterator<Object> iterator = objList.iterator(); iterator.hasNext();) {
       Object artObj = iterator.next();
       init(artObj, space);
     }
     if(articleObj == null) {
       throw new EmptyArticleException();
     }
   }
   
   public void init(Object obj, String space){
     if (articleObj == null) {
       articleObj = new HashMap<String, Object>();
       defaultLang = context.getWiki().getWebPreference("default_language", space, "", context);
     }
     mLogger.debug("Init Article Object");
     if (obj != null) {
       Property prop = obj.getProperty("lang");
       if (prop != null) {
         articleObj.put((String) prop.getValue(), obj);
       } else {
         articleObj.put("", obj);
       }
     }
   }
   
   private void setSubscribed() {
     XWikiDocument doc = null;
     try {
       doc = context.getWiki().getDocument(getDocName(), context);
     } catch (XWikiException e) {
       mLogger.error(e);
     }
     if(doc != null) {
       BaseObject obj = doc.getObject("Celements2.BlogArticleSubscriptionClass", "subscriber", context.getDoc().getFullName(), false);
       mLogger.info("Search for object with subscriber == '" + context.getDoc().getFullName() + "' had result: " + obj);
       if(obj != null){
         int isSubscr = obj.getIntValue("doSubscribe");
         mLogger.info("'" + doc.getFullName() + "' doSubscribe is: '" + isSubscr + "'");
         if(isSubscr == 1){
           isSubscribed = true;
         } else if(isSubscr == 0){
           isSubscribed = false;
         }
       } else{
         isSubscribed = null;
         mLogger.info("'" + doc.getFullName() + "' doSubscribe is: '" + isSubscribed + "'");
       }
     }
   }
   
   public Date getPublishDate(){
     Date date = getDateProperty(getObj(defaultLang), "publishdate");
     return date;
   }
   
   public Date getPublishDate(String lang){
     Date date = getDateProperty(getObj(lang), "publishdate");
     return date;
   }
   
   public Date getArchiveDate(){
     Date date = getDateProperty(getObj(defaultLang), "archivedate");
     return date;
   }
   
   public Date getArchiveDate(String lang){
     Date date = getDateProperty(getObj(lang), "archivedate");
     return date;
   }
   
   /**
    * Get the effective language of the title. E.g. getTitleLang('fr') returns 'de' if 
    * default is 'de' and there is no 'fr' translation.
    * @param lang
    * @return
    */
   public String getTitleLang(String lang){
     return getTitleDetailed(lang)[0];
   }
   
   public String getTitle(String lang){
     return getTitleDetailed(lang)[1];
   }
   
   String[] getTitleDetailed(String lang) {
     String title = getStringProperty(getObj(lang), "title");
     if((title == null) || "".equals(title.trim())) {
       title = getStringProperty(getObj(defaultLang), "title");
       lang = defaultLang;
     }
     return new String[]{lang, title};
   }
   
   /**
    * Get the effective language of the title. E.g. getExtractLang('fr', x) returns 'de' if 
    * default is 'de' and there is no 'fr' translation.
    * @param lang
    * @return
    */
   public String getExtractLang(String lang, boolean isViewtypeFull) {
     return getExtractDetailed(lang, isViewtypeFull, 250)[0];
   }
   
   public String getExtract(String lang, boolean isViewtypeFull) {
     return getExtractDetailed(lang, isViewtypeFull, 250)[1];
   }
 
   public String getExtract(String lang, boolean isViewtypeFull, int maxNumChars){
     return getExtractDetailed(lang, isViewtypeFull, maxNumChars)[1];
   }
   
   public String[] getExtractDetailed(String lang, boolean isViewtypeFull, int maxNumChars){
     String effectiveLang = lang;
     mLogger.info("getExtract('" + lang + "', " + isViewtypeFull + "')");
     if((extract == null) || !extract.containsKey(lang)){
       String fullExtract = getStringProperty(getObj(lang), "extract");
       boolean needsMoreLink = true;
       boolean needsMoreLinkDots = false;
       if(isEmptyStringAndNotDefLang(fullExtract, lang)){
         fullExtract = getStringProperty(getObj(defaultLang), "extract");
         effectiveLang = defaultLang;
       }
       if(fullExtract.trim().equals("") || isViewtypeFull){
         fullExtract = getFullArticle(lang);
         needsMoreLink = false;
         if(isEmptyStringAndNotDefLang(fullExtract, lang)){
           fullExtract = getFullArticle(defaultLang);
           effectiveLang = defaultLang;
        } else {
          effectiveLang = lang;
         }
         if(!isViewtypeFull && (fullExtract.length() > maxNumChars)){
           fullExtract = fullExtract.substring(0, fullExtract.lastIndexOf(" ", maxNumChars) + 1);
           needsMoreLink = true;
           needsMoreLinkDots = true;
         }
       }
       
       if(extract == null){ extract = new HashMap<String, String>(); }
       extract.put(lang, fullExtract);
       if(hasMoreLink == null){ hasMoreLink = new HashMap<String, Boolean>(); }
       hasMoreLink.put(lang, needsMoreLink);
       if(hasMoreLinkDots == null){ hasMoreLinkDots = new HashMap<String, Boolean>(); }
       hasMoreLinkDots.put(lang, needsMoreLinkDots);
     }
     
     String extr = extract.get(lang);
 //    mLogger.info("getExtract('" + lang + "', " + isViewtypeFull + "') => '" + extr + "'");
     //TODO check if this code is unreachable / senseless since last refactoring, since 
     //     fallback to default language is already handled earlier
 //    if(isEmptyStringAndNotDefLang(extr, lang)){
 //      extr = getExtract(defaultLang, isViewtypeFull);
 //    }
     return new String[]{effectiveLang, extr};
   }
 
   boolean isEmptyStringAndNotDefLang(String string, String lang) {
     return (string.trim().length() <= 0) && !lang.equals(defaultLang);
   }
   
   public String getFullArticle(String lang){
     return getStringProperty(getObj(lang), "content");
   }
   
   public String getEditor(String lang){
     return getStringProperty(getObj(lang), "blogeditor");
   }
   
   public Boolean isCommentable(){
     return getBooleanProperty(getObj(defaultLang), "hasComments");
   }
   
   public Boolean isCommentable(String lang){
     return getBooleanProperty(getObj(lang), "hasComments");
   }
 
   public Boolean isSubscribable(){
     return getBooleanProperty(getObj(defaultLang), "isSubscribable");
   }
   
   //Attention! May return null if not set so use -> if(isSubscribable(lang) == true)
   public Boolean isSubscribable(String lang){
     return getBooleanProperty(getObj(lang), "isSubscribable");
   }
   
   public boolean isFromSubscribableBlog(String blogArticleSpace){
     boolean result = true;
     if(getDocName().startsWith(blogArticleSpace + ".")){
       result = false;
     }
     return result;
   }
   
   public String getDocName(){
     for (Iterator<String> iterator = articleObj.keySet().iterator(); iterator.hasNext();) {
       String key = (String) iterator.next();
       return articleObj.get(key).getName();
     }
     return "";
   }
 
   public String getStringProperty(Object obj, String name){
     String result = "";
     if(obj != null){
       Property prop = obj.getProperty(name);
       if(prop != null){
         result = prop.getValue().toString();
       }
     }
     return result;
   }
   
   public Date getDateProperty(Object obj, String name){
     Date result = null;
     if(obj != null){
       Property prop = obj.getProperty(name);
       if(prop != null){
         result = (Date)prop.getValue();
       }
     }
     return result;
   }
   
   // not set = null, false = 0, true = 1
   public Boolean getBooleanProperty(Object obj, String name){
     Boolean result = null;
     if(obj != null){
       Property prop = obj.getProperty(name);
       if(prop != null){
         Integer val = (Integer)prop.getValue();
         if(val != null){
           if(val == 1){
             result = true;
           } else if(val == 0){
             result = false;
           }
         }
       }
     }
     return result;
   }
   
   public Object getObj(String lang){
     Object obj = null;
     if(articleObj.containsKey(lang)){
       mLogger.info("'" + getDocName() + "' - Getting object for lang '" + lang + "'");
       obj = articleObj.get(lang);
     } else{
       if(articleObj.containsKey(defaultLang)){
         mLogger.info("'" + getDocName() + "' - Getting object for defaultLang '" + lang + "'");
         obj = articleObj.get(defaultLang);
       } else{
         if(articleObj.containsKey("")){
           mLogger.info("'" + getDocName() + "' - Getting object failed for lang ''");
           obj = articleObj.get("");
         } else{
           mLogger.info("'" + getDocName() + "' - Getting object failed for lang '" + lang + "' and defaultLang '" + defaultLang + "'");
         }
       }
     }
     mLogger.info("Object found: doc name='" + ((obj != null)?obj.getName():"") + "' obj='" + obj + "'");
     return obj;
   }
 
   public Boolean isSubscribed() {
     if(isSubscribed == null){
       setSubscribed();
     }
     return isSubscribed;
   }
 
   public boolean hasMoreLink(String lang, boolean isViewtypeFull) {
     if((hasMoreLink == null) || !hasMoreLink.containsKey(lang)){
       getExtract(lang, isViewtypeFull);
     }
     return hasMoreLink.get(lang);
   }
 
   public boolean hasMoreLinkDots(String lang, boolean isViewtypeFull) {
     if(hasMoreLink(lang, isViewtypeFull)){
       getExtract(lang, isViewtypeFull);
     }
     return hasMoreLinkDots.get(lang);
   }
 }
