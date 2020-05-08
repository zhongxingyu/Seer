 package com.celements.blog.service;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryExecutor;
 import org.xwiki.query.QueryManager;
 import org.xwiki.query.internal.DefaultQuery;
 
 import com.celements.blog.plugin.BlogClasses;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.web.Utils;
 
 public class BlogServiceTest extends AbstractBridgedComponentTestCase {
 
   private XWiki xwiki;
   private XWikiContext context;
   private QueryManager queryManagerMock;
   private QueryExecutor queryExecutorMock;
   
   private BlogService blogService;
   
   private final WikiReference wikiRef = new WikiReference("wiki");
   
   @Before
   public void setUp_BlogServiceTest() throws Exception {
     context = getContext();
     xwiki = getWikiMock();
     blogService = (BlogService) Utils.getComponent(IBlogServiceRole.class);
     queryManagerMock = createMockAndAddToDefault(QueryManager.class);
     queryExecutorMock = createMockAndAddToDefault(QueryExecutor.class);
     blogService.injectQueryManager(queryManagerMock);
   }
 
   @Test
   public void testGetBlogConfigDocRef() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
     SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
     XWikiDocument doc = getBlogDoc(docRef, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, 
         spaceRef.getName());
    String xwql = "from doc.object(" + BlogClasses.BLOG_CONFIG_CLASS + ") as obj";
     Query query = new DefaultQuery("theStatement", Query.XWQL, queryExecutorMock);
     
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andReturn(query).once();
     expect(queryExecutorMock.execute(eq(query))).andReturn(Arrays.<Object>asList(
         "space.blog")).once();    
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     DocumentReference ret = blogService.getBlogConfigDocRef(spaceRef);
     verifyDefault();
     
     assertEquals(docRef, ret);
     assertEquals(wikiRef.getName(), query.getWiki());
   }
 
   @Test
   public void testGetBlogConfigDocRef_cache() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");   
     SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
     Map<SpaceReference, List<DocumentReference>> blogCache = 
         new HashMap<SpaceReference, List<DocumentReference>>();
     blogCache.put(spaceRef, Arrays.asList(docRef));
     blogService.injectBlogCache(blogCache);
 
     replayDefault();
     DocumentReference ret = blogService.getBlogConfigDocRef(spaceRef);
     verifyDefault();
     
     assertEquals(docRef, ret);
   }
 
   @Test
   public void testGetBlogConfigDocRef_cache_empty() throws Exception {   
     SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
     Map<SpaceReference, List<DocumentReference>> blogCache = 
         new HashMap<SpaceReference, List<DocumentReference>>();
     blogService.injectBlogCache(blogCache);
 
     replayDefault();
     DocumentReference ret = blogService.getBlogConfigDocRef(spaceRef);
     verifyDefault();
     
     assertNull(ret);
   }
   
   @Test
   public void testGetBlogSpaceRef() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");   
     SpaceReference spaceRef = new SpaceReference("blogSpace", wikiRef);
     XWikiDocument doc = getBlogDoc(docRef, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, 
         spaceRef.getName());
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     SpaceReference ret = blogService.getBlogSpaceRef(docRef);
     verifyDefault();
     
     assertEquals(spaceRef, ret);
   }
   
   @Test
   public void testGetBlogSpaceRef_notSet() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
     XWikiDocument doc = getBlogDoc(docRef, null, null);
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     SpaceReference ret = blogService.getBlogSpaceRef(docRef);
     verifyDefault();
     
     assertNull(ret);
   }
   
   @Test
   public void testGetBlogSpaceRef_noBObj() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
     XWikiDocument doc = new XWikiDocument(docRef);
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     SpaceReference ret = blogService.getBlogSpaceRef(docRef);
     verifyDefault();
     
     assertNull(ret);
   }
   
   @Test
   public void testGetBlogSpaceRef_XWikiException() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(new XWikiException()
         ).once();
 
     replayDefault();
     try {
       blogService.getBlogSpaceRef(docRef);
       fail("expecting XWE");
     } catch (XWikiException xwe) {
       // expected
     }
     verifyDefault();
   }
   
   @Test
   public void testGetBlogSpaceRef_null() throws Exception {
     replayDefault();
     SpaceReference ret = blogService.getBlogSpaceRef(null);
     verifyDefault();
     
     assertNull(ret);
   }
   
   @Test
   public void testIsSubscribable() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
     XWikiDocument doc = getBlogDoc(docRef, "", "");
     doc.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
         BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     boolean ret = blogService.isSubscribable(docRef);
     verifyDefault();
     
     assertTrue(ret);
   }
   
   @Test
   public void testIsSubscribable_wrongVal() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");  
     XWikiDocument doc = getBlogDoc(docRef, "", "");
     doc.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
         BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 0);
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     boolean ret = blogService.isSubscribable(docRef);
     verifyDefault();
     
     assertFalse(ret);
   }
   
   @Test
   public void testIsSubscribable_notSet() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");  
     XWikiDocument doc = getBlogDoc(docRef, null, null);
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     boolean ret = blogService.isSubscribable(docRef);
     verifyDefault();
     
     assertFalse(ret);
   }
   
   @Test
   public void testIsSubscribable_noObj() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");  
     XWikiDocument doc = new XWikiDocument(docRef);
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
 
     replayDefault();
     boolean ret = blogService.isSubscribable(docRef);
     verifyDefault();
     
     assertFalse(ret);
   }
   
   @Test
   public void testIsSubscribable_XWikiException() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
     
     expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(new XWikiException()
         ).once();
 
     replayDefault();
     try {
       blogService.isSubscribable(docRef);
       fail("expecting XWE");
     } catch (XWikiException xwe) {
       // expected
     }
     verifyDefault();
   }
   
   @Test
   public void testGetSubribedToBlogs() throws Exception {
     DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "blog");
     String space1 = "space1";
     String space2 = "space2";
     String space3 = "space3";
     XWikiDocument doc = getBlogDoc(docRef, BlogClasses.PROPERTY_BLOG_CONFIG_SUBSCRIBE_TO, 
         space1 + "," + space2 + "," + space3 + ",space4");
     DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "blog1");
     XWikiDocument doc1 = getBlogDoc(docRef1, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, 
         space1);
     doc1.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
         BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);
     DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "blog2");
     XWikiDocument doc2 = getBlogDoc(docRef2, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, 
         space2);
     doc2.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
         BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 0);
     DocumentReference docRef3 = new DocumentReference(wikiRef.getName(), "space", "blog3");
     XWikiDocument doc3 = getBlogDoc(docRef3, BlogClasses.PROPERTY_BLOG_CONFIG_BLOGSPACE, 
         space3);
     doc3.getXObject(blogService.getBlogConfigClassRef(wikiRef)).setIntValue(
         BlogClasses.PROPERTY_BLOG_CONFIG_IS_SUBSCRIBABLE, 1);
     Map<SpaceReference, List<DocumentReference>> blogCache = 
         new HashMap<SpaceReference, List<DocumentReference>>();
     blogCache.put(new SpaceReference(space1, wikiRef), Arrays.asList(docRef1));
     blogCache.put(new SpaceReference(space2, wikiRef), Arrays.asList(docRef2));
     blogCache.put(new SpaceReference(space3, wikiRef), Arrays.asList(docRef3));
     blogService.injectBlogCache(blogCache);
 
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
     expect(xwiki.getDocument(eq(docRef1), same(context))).andReturn(doc1).once();
     expect(xwiki.getDocument(eq(docRef2), same(context))).andReturn(doc2).once();
     expect(xwiki.getDocument(eq(docRef3), same(context))).andReturn(doc3).once();
 
     replayDefault();
     List<DocumentReference> ret = blogService.getSubribedToBlogs(docRef);
     verifyDefault();
     
     assertEquals(Arrays.asList(docRef1, docRef3), ret);
   }
   
   @Test
   public void testGetArticles() {
     fail("TODO"); // TODO
   }
   
   private XWikiDocument getBlogDoc(DocumentReference docRef, String field, String val) {
     XWikiDocument doc = new XWikiDocument(docRef);
     BaseObject blogObj = new BaseObject();
     blogObj.setXClassReference(blogService.getBlogConfigClassRef(wikiRef));
     if (field != null) {
       blogObj.setStringValue(field, val);
     }
     doc.addXObject(blogObj);
     return doc;
   }
   
 }
