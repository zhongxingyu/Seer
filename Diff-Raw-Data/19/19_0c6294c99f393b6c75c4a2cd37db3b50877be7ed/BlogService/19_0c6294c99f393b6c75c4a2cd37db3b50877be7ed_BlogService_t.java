 package com.celements.blog.service;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.component.manager.ComponentLookupException;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 
 import com.celements.blog.article.Article;
 import com.celements.blog.article.ArticleLoadException;
 import com.celements.blog.article.ArticleLoadParameter;
 import com.celements.blog.article.IArticleEngineRole;
 import com.celements.blog.plugin.BlogClasses;
 import com.celements.common.classes.IClassCollectionRole;
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 @Component
 public class BlogService implements IBlogServiceRole {
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(BlogService.class);
   
   private Map<SpaceReference, List<DocumentReference>> blogCache;
   
   @Requirement
   private ComponentManager componentManager;
 
   @Requirement
   private QueryManager queryManager;
 
   @Requirement
   private IWebUtilsService webUtils;
   
   @Requirement("celements.celBlogClasses")
   private IClassCollectionRole blogClasses;
 
   @Requirement
   private Execution execution;
   
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty(
         XWikiContext.EXECUTIONCONTEXT_KEY);
   }
 
   @Deprecated
   @Override
   public DocumentReference getBlogDocRefByBlogSpace(String blogSpaceName) {
     try {
      if (StringUtils.isNotBlank(blogSpaceName)) {
        return getBlogConfigDocRef(webUtils.resolveSpaceReference(blogSpaceName, 
            new WikiReference(getContext().getDatabase())));
      }
     } catch (QueryException qex) {
       LOGGER.error("Failed to parse xwql query to get BlogDocRef for blog space ["
           + blogSpaceName + "].", qex);
     } catch (XWikiException xwe) {
       LOGGER.error("Failed to get blog document for blog space '" + blogSpaceName + "'", 
           xwe);
     }
     return null;
   }
 
   @Deprecated
   @Override
   public XWikiDocument getBlogPageByBlogSpace(String blogSpaceName) {
     DocumentReference blogDocRef = getBlogDocRefByBlogSpace(blogSpaceName);
     if (blogDocRef != null) {
       try {
         return getContext().getWiki().getDocument(blogDocRef, getContext());
       } catch (XWikiException exp) {
         LOGGER.error("Failed to get blog document for blog space [" + blogSpaceName
             + "].", exp);
       }
     }
     return null;
   }
 
   @Override
   public DocumentReference getBlogConfigDocRef(SpaceReference spaceRef
       ) throws QueryException, XWikiException {
     DocumentReference ret = null;
     WikiReference wikiRef = new WikiReference(spaceRef.getParent());
     List<DocumentReference> docRefs = getBlogCache(wikiRef).get(spaceRef);
     if ((docRefs != null) && (docRefs.size() > 0)) {
       ret = docRefs.get(0);
     }
     return ret;
   }
 
   private synchronized Map<SpaceReference, List<DocumentReference>> getBlogCache(
       WikiReference wikiRef) throws QueryException, XWikiException {
     if (blogCache == null) {
       Map<SpaceReference, List<DocumentReference>> map = 
           new HashMap<SpaceReference, List<DocumentReference>>();
      String xqwl = "from doc.object(" + BlogClasses.BLOG_CONFIG_CLASS + ") as obj";
       Query query = queryManager.createQuery(xqwl, Query.XWQL).setWiki(wikiRef.getName());
       for (String result : query.<String>execute()) {
         DocumentReference docRef = webUtils.resolveDocumentReference(result, wikiRef);
         SpaceReference spaceRef = getBlogSpaceRef(docRef);
         if (spaceRef != null) {
           List<DocumentReference> list = map.get(spaceRef);
           if (list == null) {
             list = new ArrayList<DocumentReference>();
             map.put(spaceRef, list);
           }
           list.add(docRef);
         }
       }
       blogCache = Collections.unmodifiableMap(map);
     }
     return blogCache;
   }
 
   public synchronized void clearBlogCache() {
     blogCache = null;
   }
 
   @Override
   public SpaceReference getBlogSpaceRef(DocumentReference docRef) throws XWikiException {
     SpaceReference spaceRef = null;
     BaseObject confObj = getBlogConfigObject(docRef);
     if (confObj != null) {
       String spaceName = confObj.getStringValue(BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE);
       if (StringUtils.isNotBlank(spaceName)) {
         spaceRef = webUtils.resolveSpaceReference(spaceName, docRef.getWikiReference());
       }
     }
     LOGGER.debug("getBlogSpace: resolved for '" + docRef + "' space:" + spaceRef);
     return spaceRef;
   }
 
   @Override
   public boolean isSubscribable(DocumentReference blogConfDocRef) throws XWikiException {
     boolean isSubscribable = false;
     BaseObject confObj = getBlogConfigObject(blogConfDocRef);
     if (confObj != null) {
       isSubscribable = confObj.getIntValue(
           BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, -1) == 1;
     }
     return isSubscribable;
   }
   
   @Override
   public List<DocumentReference> getSubribedToBlogs(DocumentReference blogConfDocRef
       ) throws QueryException, XWikiException {
     List<DocumentReference> ret = new ArrayList<DocumentReference>();
     BaseObject confObj = getBlogConfigObject(blogConfDocRef);
     if (confObj != null) {
       String spaceNames = confObj.getStringValue(
           BlogClasses.PROPERTY_BLOG_CONFIG_SUBSCRIBE_TO);
       for (String spaceName : Arrays.asList(spaceNames.split(","))) {
         if (StringUtils.isNotBlank(spaceName)) {
           DocumentReference docRef = getBlogConfigDocRef(webUtils.resolveSpaceReference(
               spaceName, blogConfDocRef.getWikiReference()));
           if (isSubscribable(docRef)) {
             ret.add(docRef);
           }
         }
       }
     }
     return Collections.unmodifiableList(ret);
   }
 
   @Override
   public List<Article> getArticles(DocumentReference blogConfDocRef, 
       ArticleLoadParameter param) throws ArticleLoadException {
     try {
       if (param == null) {
         param = new ArticleLoadParameter();
       }
       param.setExecutionDate(new Date());
       param.setBlogDocRef(blogConfDocRef);
       param.setSubscribedToBlogs(getSubribedToBlogs(blogConfDocRef));
       List<Article> articles = getArticleEngine().getArticles(param);
       LOGGER.info("getArticles: for " + param + " got " + articles.size() + " articles");
       if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("getArticles: for " + param + " got: " + articles);
       }
       return Collections.unmodifiableList(articles);
     } catch (XWikiException xwe) {
       throw new ArticleLoadException("Error for '" + blogConfDocRef + "'", xwe);
     } catch (QueryException qexc) {
       throw new ArticleLoadException("Error for '" + blogConfDocRef + "'", qexc);
     }
   }
 
   private IArticleEngineRole getArticleEngine() throws ArticleLoadException {
     IArticleEngineRole engine = null;
     String engineHint = getContext().getWiki().getXWikiPreference("blog_article_engine",
         "blog.article.engine", null, getContext());
     try {
       Map<String, IArticleEngineRole> engineMap = componentManager.lookupMap(
           IArticleEngineRole.class);
       engine = engineMap.get(engineHint);
       if (engine == null) {
         engine = engineMap.get("default");
       }
     } catch (ComponentLookupException exc) {
       LOGGER.error("Error looking up engine components", exc);
     }
     if (engine != null) {
       LOGGER.info("getBlogArticleEngine: got engine '" + engine + "' for hint '" 
           + engineHint + "'");
       return engine;
     } else {
       throw new ArticleLoadException("Unable to load engine for hint");
     }
   }
   
   private BaseObject getBlogConfigObject(DocumentReference blogConfDocRef
       ) throws XWikiException {
     BaseObject ret = null;
     if (blogConfDocRef != null) {
       XWikiDocument doc = getContext().getWiki().getDocument(blogConfDocRef, getContext());
       ret = doc.getXObject(getBlogConfigClassRef(blogConfDocRef.getWikiReference()));    
     }
     return ret;
   }
   
   DocumentReference getBlogConfigClassRef(WikiReference wikiRef) {
     return ((BlogClasses) blogClasses).getBlogConfigClassRef(wikiRef.getName());
   }
   
   void injectBlogCache(Map<SpaceReference, List<DocumentReference>> blogCache) {
     this.blogCache = blogCache;
   }
 
   void injectQueryManager(QueryManager queryManager) {
     this.queryManager = queryManager;
   }
 
 }
