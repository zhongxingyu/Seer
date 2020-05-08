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
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.blog.service.IBlogServiceRole;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.web.XWikiMessageTool;
 
 
 public class BlogPluginTest extends AbstractBridgedComponentTestCase{
   BlogPlugin plugin;
   XWikiContext context;
   XWiki xwiki;
   private IBlogServiceRole blogServiceMock;
   
   @Before
   public void setUp_BlogPluginTest() throws Exception {
     xwiki = createMock(XWiki.class);
     context = getContext();
     context.setWiki(xwiki);
     plugin = new BlogPlugin("", "", context);
     blogServiceMock = createMock(IBlogServiceRole.class);
     plugin.injected_BlogService = blogServiceMock;
   }
 
   @Test
   public void testGetBlogArticles_getArchivedArticles() throws XWikiException {
     String artSpace = "ArtSpace";
     DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace,
         "Article");
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     String articleFN = artSpace + ".Article";
     artName.add(articleFN);
     expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))
         ).andReturn(artName).once();
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         artSpace, "BlogConfig");
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(confxDoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(articleDocRef), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("ArtSpace"))).andReturn(
         blogConfigDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), 
         same(context))).andReturn("de").once();
     expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
     expect(confDoc.hasAccessLevel((String)anyObject())).andReturn(true).atLeastOnce();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc).atLeastOnce();
     BaseObject obj = new BaseObject();
     obj.setDocumentReference(articleDocRef);
     obj.setStringValue("lang", "de");
     obj.setDateValue("publishdate", new Date(0, 11, 31));
     obj.setDateValue("archivedate", new Date(10, 0, 1));
     obj.setStringValue("title", "the title");
     obj.setStringValue("content", "the content");
     Vector<BaseObject> objVec = new Vector<BaseObject>();
     objVec.add(obj);
     expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     replayAll(confDoc, confxDoc, xdoc);
     List<Article> result = plugin.getBlogArticles(artSpace, "", "de", true, false,
         false, true, true, true, true, false, true, true, context);
     assertEquals(1, result.size());
     verifyAll(confDoc, confxDoc, xdoc);
   }
 
   @Test
   public void testGetBlogArticles_getArchivedArticles_notYetArchived(
       ) throws XWikiException {
     String artSpace = "ArtSpace";
     DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace,
         "Article");
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     String articleFN = artSpace + ".Article";
     artName.add(articleFN);
     expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))
         ).andReturn(artName).once();
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(confxDoc
         ).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), 
         same(context))).andReturn("de").once();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc);
     BaseObject obj = new BaseObject();
     obj.setDocumentReference(articleDocRef);
     obj.setStringValue("lang", "de");
     obj.setDateValue("publishdate", new Date(8089, 0, 1));
     obj.setDateValue("archivedate", new Date(8099, 11, 31));
     obj.setStringValue("title", "the title");
     obj.setStringValue("content", "the content");
     Vector<BaseObject> objVec = new Vector<BaseObject>();
     objVec.add(obj);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
     replayAll(confDoc, confxDoc, xdoc);
     List<Article> result = plugin.getBlogArticles(artSpace, "", "de", true, false,
         false, true, true, true, true, false, true, true, context);
     assertEquals("0 Articles expected, but received " + result.size(), 0, result.size());
     verifyAll(confDoc, confxDoc, xdoc);
   }
 
   @Test
   public void testGetBlogArticles_getArchivedArticles_archiveBeforePublish(
       ) throws XWikiException {
     String artSpace = "ArtSpace";
     DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace,
         "Article");
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     String articleFN = artSpace + ".Article";
     artName.add(articleFN);
     expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))
         ).andReturn(artName).once();
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         artSpace, "BlogConfig");
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(confxDoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(articleDocRef), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(confxDoc
       ).atLeastOnce();
     expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("ArtSpace"))).andReturn(
         blogConfigDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), 
         same(context))).andReturn("de").once();
     expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
     expect(confDoc.hasAccessLevel((String)anyObject())).andReturn(true).atLeastOnce();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc);
     BaseObject obj = new BaseObject();
     obj.setDocumentReference(articleDocRef);
     obj.setStringValue("lang", "de");
     obj.setDateValue("publishdate", new Date(8099, 11, 31));
     obj.setDateValue("archivedate", new Date(0, 0, 1));
     obj.setStringValue("title", "the title");
     obj.setStringValue("content", "the content");
     Vector<BaseObject> objVec = new Vector<BaseObject>();
     objVec.add(obj);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
     replayAll(confDoc, confxDoc, xdoc);
     List<Article> result = plugin.getBlogArticles(artSpace, "", "de", true, false,
         false, true, true, true, true, false, true, true, context);
     assertEquals(1, result.size());
     verifyAll(confDoc, confxDoc, xdoc);
   }
 
   @Test
   public void testGetBlogArticles_getArchivedArticles_archiveBeforePublishBeforeToday(
       ) throws XWikiException {
     String artSpace = "ArtSpace";
     DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace,
         "Article");
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     String articleFN = artSpace + ".Article";
     artName.add(articleFN);
     expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))
         ).andReturn(artName).once();
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         artSpace, "BlogConfig");
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(confxDoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(articleDocRef), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(confxDoc
       ).atLeastOnce();
     expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("ArtSpace"))).andReturn(
         blogConfigDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), 
         same(context))).andReturn("de").once();
     expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
     expect(confDoc.hasAccessLevel((String)anyObject())).andReturn(true).atLeastOnce();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc);
     BaseObject obj = new BaseObject();
     obj.setDocumentReference(articleDocRef);
     obj.setStringValue("lang", "de");
     obj.setDateValue("publishdate", new Date(10, 11, 31));
     obj.setDateValue("archivedate", new Date(0, 0, 1));
     obj.setStringValue("title", "the title");
     obj.setStringValue("content", "the content");
     Vector<BaseObject> objVec = new Vector<BaseObject>();
     objVec.add(obj);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
     replayAll(confDoc, confxDoc, xdoc);
     List<Article> result = plugin.getBlogArticles(artSpace, "", "de", true, false,
         false, true, true, true, true, false, true, true, context);
     assertEquals(1, result.size());
     verifyAll(confDoc, confxDoc, xdoc);
   }
 
   @Test
   public void testGetBlogArticles_getArchivedArticles_noArchiveDateSet(
       ) throws XWikiException {
     String artSpace = "ArtSpace";
     DocumentReference articleDocRef = new DocumentReference("xwikidb", artSpace,
         "Article");
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     String articleFN = artSpace + ".Article";
     artName.add(articleFN);
     expect(xwiki.search(eq(getAllArticlesHQL(artSpace)), same(context))
         ).andReturn(artName).once();
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         artSpace, "BlogConfig");
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     expect(xwiki.getDocument(eq(articleFN), same(context))).andReturn(xdoc).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("ArtSpace"))).andReturn(confxDoc
       ).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq(artSpace), eq(""), 
         same(context))).andReturn("de").once();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc);
     BaseObject obj = new BaseObject();
     obj.setDocumentReference(articleDocRef);
     obj.setStringValue("lang", "de");
     obj.setDateValue("publishdate", new Date(9999, 11, 31));
     obj.setDateValue("archivedate", null);
     obj.setStringValue("title", "the title");
     obj.setStringValue("content", "the content");
     Vector<BaseObject> objVec = new Vector<BaseObject>();
     objVec.add(obj);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(objVec);
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn(artSpace).atLeastOnce();
     replayAll(confDoc, confxDoc, xdoc);
     List<Article> result = plugin.getBlogArticles(artSpace, "", "de", true, false,
         false, true, true, true, true, false, true, true, context);
     assertEquals(0, result.size());
     verifyAll(confDoc, confxDoc, xdoc);
   }
   
   @Test
   public void testGetNeighbourArticle_next() throws XWikiException {
     context.setLanguage("de");
     Article article = createMock(Article.class);
     expect(article.getDocName()).andReturn("Space.Article2").atLeastOnce();
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     artName.add("Space.Article1");
     artName.add("Space.Article2");
     artName.add("Space.Article3");
     artName.add("Space.Article4");
     expect(xwiki.search(eq(getAllArticlesHQL("Space")), same(context))
         ).andReturn(artName).once();
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         "Space", "BlogConfig");
     expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Space"))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("Space"))).andReturn(
         blogConfigDocRef).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article1")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article1"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article2")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article2"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article3")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article3"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article4")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article4"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("Space"), eq(""), 
         same(context))).andReturn("de").atLeastOnce();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc).atLeastOnce();
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef).anyTimes();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(1)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(2)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(3)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(4)).once();
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "Space", "Bla");
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn("Space").atLeastOnce();
     expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
     expect(confDoc.hasAccessLevel((String)anyObject())).andReturn(true).atLeastOnce();
     replayAll(article, confDoc, confxDoc, xdoc);
     Article next = plugin.getNeighbourArticle(article, true, context);
     assertNotNull(next);
     assertEquals("Space.Article3", next.getDocName());
     verifyAll(article, confDoc, confxDoc, xdoc);
   }
   
   @Test
   public void testGetNeighbourArticle_next_last() throws XWikiException {
     context.setLanguage("de");
     Article article = createMock(Article.class);
     expect(article.getDocName()).andReturn("Space.Article4").atLeastOnce();
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     artName.add("Space.Article1");
     artName.add("Space.Article2");
     artName.add("Space.Article3");
     artName.add("Space.Article4");
     expect(xwiki.search(eq(getAllArticlesHQL("Space")), same(context))
         ).andReturn(artName).once();
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         "Space", "BlogConfig");
     expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Space"))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("Space"))).andReturn(
         blogConfigDocRef).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article1")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article1"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article2")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article2"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article3")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article3"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article4")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article4"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("Space"), eq(""), 
         same(context))).andReturn("de").atLeastOnce();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc).atLeastOnce();
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef).anyTimes();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(1)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(2)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(3)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(4)).once();
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "Space", "Bla");
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn("Space").atLeastOnce();
     expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
     expect(confDoc.hasAccessLevel((String)anyObject())).andReturn(true).atLeastOnce();
     replayAll(article, confDoc, confxDoc, xdoc);
     Article next = plugin.getNeighbourArticle(article, true, context);
     assertNotNull(next);
     assertEquals("Space.Article1", next.getDocName());
     verifyAll(article, confDoc, confxDoc, xdoc);
   }
   
   @Test
   public void testGetNeighbourArticle_prev() throws XWikiException {
     context.setLanguage("de");
     Article article = createMock(Article.class);
     expect(article.getDocName()).andReturn("Space.Article3").atLeastOnce();
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     artName.add("Space.Article1");
     artName.add("Space.Article2");
     artName.add("Space.Article3");
     artName.add("Space.Article4");
     expect(xwiki.search(eq(getAllArticlesHQL("Space")), same(context))
         ).andReturn(artName).once();
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         "Space", "BlogConfig");
     expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Space"))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("Space"))).andReturn(
         blogConfigDocRef).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article1")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article1"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article2")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article2"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article3")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article3"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article4")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article4"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("Space"), eq(""), 
         same(context))).andReturn("de").atLeastOnce();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc).atLeastOnce();
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef).anyTimes();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(1)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(2)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(3)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(4)).once();
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "Space", "Bla");
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn("Space").atLeastOnce();
     expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
     expect(confDoc.hasAccessLevel((String)anyObject())).andReturn(true).atLeastOnce();
     replayAll(article, confDoc, confxDoc, xdoc);
     Article next = plugin.getNeighbourArticle(article, false, context);
     assertNotNull(next);
     assertEquals("Space.Article2", next.getDocName());
     verifyAll(article, confDoc, confxDoc, xdoc);
   }
   
   @Test
   public void testGetNeighbourArticle_prev_first() throws XWikiException {
     context.setLanguage("de");
     Article article = createMock(Article.class);
     expect(article.getDocName()).andReturn("Space.Article1").atLeastOnce();
     XWikiDocument xdoc = createMock(XWikiDocument.class);
     Document doc = new Document(xdoc, context);
     List<Object> artName = new ArrayList<Object>();
     artName.add("Space.Article1");
     artName.add("Space.Article2");
     artName.add("Space.Article3");
     artName.add("Space.Article4");
     expect(xwiki.search(eq(getAllArticlesHQL("Space")), same(context))
         ).andReturn(artName).once();
     XWikiDocument confxDoc = createMock(XWikiDocument.class);
     Document confDoc = createMock(Document.class);
     DocumentReference blogConfigDocRef = new DocumentReference(context.getDatabase(),
         "Space", "BlogConfig");
     expect(xwiki.getDocument(eq(blogConfigDocRef), same(context))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Space"))).andReturn(confxDoc
         ).atLeastOnce();
     expect(blogServiceMock.getBlogDocRefByBlogSpace(eq("Space"))).andReturn(
         blogConfigDocRef).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article1")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article1"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article2")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article2"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article3")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article3"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getDocument(eq(getDocRef("Space","Article4")), same(context))).andReturn(
         xdoc).atLeastOnce();
     expect(xwiki.getDocument(eq("Space.Article4"), same(context))).andReturn(xdoc
         ).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("Space"), eq(""), 
         same(context))).andReturn("de").atLeastOnce();
     expect(confxDoc.newDocument(same(context))).andReturn(confDoc).atLeastOnce();
     expect(xdoc.newDocument(same(context))).andReturn(doc).atLeastOnce();
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki",
         "ArticleClass");
     expect(xdoc.clone()).andReturn(xdoc).atLeastOnce();
     expect(xdoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef).anyTimes();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(1)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(2)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(3)).once();
     expect(xdoc.getXObjects(eq(articleClassRef))).andReturn(getArticleObjVect(4)).once();
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "Space", "Bla");
     expect(xdoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xdoc.getSpace()).andReturn("Space").atLeastOnce();
     expect(confDoc.hasProgrammingRights()).andReturn(true).atLeastOnce();
     expect(confDoc.hasAccessLevel((String)anyObject())).andReturn(true).atLeastOnce();
     replayAll(article, confDoc, confxDoc, xdoc);
     Article next = plugin.getNeighbourArticle(article, false, context);
     assertNotNull(next);
     assertEquals("Space.Article4", next.getDocName());
     verifyAll(article, confDoc, confxDoc, xdoc);
   }
   
   @Test
   public void testBatchImportReceivers_Exception() throws XWikiException {
     Map<String, String> result = new TreeMap<String, String>();
     result.put("myname@email.com", "invalid");
     String importData = "\r\n,My Name <myName@email.com>,,,,";
     String nl = "My.Newsletter";
     XWikiMessageTool messageTool = createMock(XWikiMessageTool.class);
     XWikiContext context = createMock(XWikiContext.class);
     expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
     expect(context.getWiki()).andReturn(xwiki).anyTimes();
     expect(context.getUser()).andReturn("XWiki.Admin").anyTimes();
     expect(xwiki.search((String)anyObject(), same(context))).andReturn(null).anyTimes();
     expect(xwiki.generateRandomString(eq(16))).andReturn("abc");
     expect(xwiki.exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
     expect(xwiki.getDocument(eq("NewsletterReceivers.abc"), same(context)))
         .andThrow(new XWikiException());
     expect(messageTool.get(eq("cel_newsletter_subscriber_invalid"))).andReturn(
         "invalid").once();
     replayAll(context, messageTool);
     assertEquals(result, plugin.batchImportReceivers(false, importData, nl, context));
     verifyAll(context, messageTool);
   }
   
   @Test
   public void testBatchImportReceivers_activeAdd() throws XWikiException {
     Map<String, String> result = new TreeMap<String, String>();
     result.put("myname@email.com", "active");
     String importData = "\r\n,My Name <myName@email.com>,,,,";
     String nl = "My.Newsletter";
     XWikiMessageTool messageTool = createMock(XWikiMessageTool.class);
     XWikiContext context = createMock(XWikiContext.class);
     expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
     expect(context.getWiki()).andReturn(xwiki).anyTimes();
     expect(context.getUser()).andReturn("XWiki.Admin").anyTimes();
     expect(xwiki.search((String)anyObject(), same(context))).andReturn(null).anyTimes();
     expect(xwiki.generateRandomString(eq(16))).andReturn("abc");
     expect(xwiki.exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
     XWikiDocument doc1 = createMock(XWikiDocument.class);
     expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
     expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
     expect(context.getDoc()).andReturn(doc1);
     expect(xwiki.getDocument(eq("NewsletterReceivers.abc"), same(context)))
         .andReturn(doc1).times(2);
     XWikiDocument nlDoc = createMock(XWikiDocument.class);
     expect(xwiki.exists(eq(nl), same(context))).andReturn(true);
     expect(xwiki.getDocument(eq(nl), same(context))).andReturn(nlDoc);
     expect(nlDoc.getFullName()).andReturn(nl).anyTimes();
     expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), 
         eq(nl), eq(false))).andReturn(null);
     BaseObject obj = new BaseObject();
     expect(doc1.newObject(eq("Celements.NewsletterReceiverClass"), same(context)))
     .andReturn(obj);
     expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), 
         eq(nl), eq(false))).andReturn(obj);
     xwiki.saveDocument(same(doc1), same(context));
     expectLastCall();
     expect(messageTool.get(eq("cel_newsletter_subscriber_active"))).andReturn(
         "active").once();
     replayAll(context, doc1, messageTool, nlDoc);
     assertEquals(result, plugin.batchImportReceivers(false, importData, nl, context));
     verifyAll(context, doc1, messageTool, nlDoc);
   }
   
   @Test
   public void testBatchImportReceivers_activeAddInactive() throws XWikiException {
     Map<String, String> result = new TreeMap<String, String>();
     result.put("myname@email.com", "inactive");
     String importData = "\r\n,My Name <myName@email.com>,,,,";
     String nl = "My.Newsletter";
     XWikiMessageTool messageTool = createMock(XWikiMessageTool.class);
     XWikiContext context = createMock(XWikiContext.class);
     expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
     expect(context.getWiki()).andReturn(xwiki).anyTimes();
     expect(context.getUser()).andReturn("XWiki.Admin").anyTimes();
     expect(xwiki.search((String)anyObject(), same(context))).andReturn(null);
     expect(xwiki.generateRandomString(eq(16))).andReturn("abc");
     expect(xwiki.exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
     XWikiDocument doc1 = createMock(XWikiDocument.class);
     expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
     expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
     expect(context.getDoc()).andReturn(doc1);
     expect(xwiki.getDocument(eq("NewsletterReceivers.abc"), same(context)))
         .andReturn(doc1).times(2);
     XWikiDocument nlDoc = createMock(XWikiDocument.class);
     expect(xwiki.exists(eq(nl), same(context))).andReturn(true);
     expect(xwiki.getDocument(eq(nl), same(context))).andReturn(nlDoc);
     expect(nlDoc.getFullName()).andReturn(nl).anyTimes();
     List<Object> list = new ArrayList<Object>();
     list.add("NewsletterReceivers.abc");
     expect(xwiki.search((String)anyObject(), same(context))).andReturn(list);
     BaseObject obj = new BaseObject();
     obj.setIntValue("isactive", 1);
     expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), 
         eq(nl), eq(false))).andReturn(obj).times(2);
     expect(messageTool.get(eq("cel_newsletter_subscriber_inactive"))).andReturn(
         "inactive").anyTimes();
     xwiki.saveDocument(same(doc1), same(context));
     expectLastCall();
     replayAll(context, doc1, messageTool, nlDoc);
     assertEquals(result, plugin.batchImportReceivers(true, importData, nl, context));
     verifyAll(context, doc1, messageTool, nlDoc);
   }
   
   @Test
   public void testBatchImportReceivers_inactiveAddActive() throws XWikiException {
     Map<String, String> result = new TreeMap<String, String>();
     result.put("myname@email.com", "inactive");
     String importData = "\r\n,My Name <myName@email.com>,,,,";
     String nl = "My.Newsletter";
     XWikiMessageTool messageTool = createMock(XWikiMessageTool.class);
     XWikiContext context = createMock(XWikiContext.class);
     expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
     expect(context.getWiki()).andReturn(xwiki).anyTimes();
     expect(context.getUser()).andReturn("XWiki.Admin").anyTimes();
     expect(xwiki.search((String)anyObject(), same(context))).andReturn(null);
     expect(xwiki.generateRandomString(eq(16))).andReturn("abc");
     expect(xwiki.exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
     XWikiDocument doc1 = createMock(XWikiDocument.class);
     expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
     expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
     expect(context.getDoc()).andReturn(doc1);
     expect(xwiki.getDocument(eq("NewsletterReceivers.abc"), same(context)))
         .andReturn(doc1).times(2);
     XWikiDocument nlDoc = createMock(XWikiDocument.class);
     expect(xwiki.exists(eq(nl), same(context))).andReturn(true);
     expect(xwiki.getDocument(eq(nl), same(context))).andReturn(nlDoc);
     expect(nlDoc.getFullName()).andReturn(nl).anyTimes();
     List<Object> list = new ArrayList<Object>();
     list.add("NewsletterReceivers.abc");
     expect(xwiki.search((String)anyObject(), same(context))).andReturn(list);
     BaseObject obj = new BaseObject();
     obj.setIntValue("isactive", 0);
     expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), 
         eq(nl), eq(false))).andReturn(obj).times(2);
     expect(messageTool.get(eq("cel_newsletter_subscriber_inactive"))).andReturn(
         "inactive").anyTimes();
     replayAll(context, doc1, messageTool, nlDoc);
     assertEquals(result, plugin.batchImportReceivers(false, importData, nl, context));
     assertEquals(0, obj.getIntValue("isactive"));
     verifyAll(context, doc1, messageTool, nlDoc);
   }
   
   @Test
   public void testBatchImportReceivers_inactiveAdd() throws XWikiException {
     Map<String, String> result = new TreeMap<String, String>();
     result.put("myname@email.com", "inactive");
     result.put("myName2@email", "invalid");
     result.put("myName @email.com", "invalid");
     String importData = "\r\n,myName @email.com,,,myName2@email\nmyName@email.com";
     String nl = "My.Newsletter";
     XWikiMessageTool messageTool = createMock(XWikiMessageTool.class);
     XWikiContext context = createMock(XWikiContext.class);
     expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
     expect(context.getWiki()).andReturn(xwiki).anyTimes();
     expect(context.getUser()).andReturn("XWiki.Admin").anyTimes();
     expect(xwiki.search((String)anyObject(), same(context))).andReturn(null).anyTimes();
     expect(xwiki.generateRandomString(eq(16))).andReturn("abc");
     expect(xwiki.exists(eq("NewsletterReceivers.abc"), same(context))).andReturn(false);
     XWikiDocument doc1 = createMock(XWikiDocument.class);
     expect(doc1.getObject(eq("Celements2.BlogConfigClass"))).andReturn(null);
     expect(doc1.getObject(eq("XWiki.ArticleClass"))).andReturn(null);
     expect(context.getDoc()).andReturn(doc1);
     expect(xwiki.getDocument(eq("NewsletterReceivers.abc"), same(context)))
         .andReturn(doc1).times(2);
     XWikiDocument nlDoc = createMock(XWikiDocument.class);
     expect(xwiki.exists(eq(nl), same(context))).andReturn(true);
     expect(xwiki.getDocument(eq(nl), same(context))).andReturn(nlDoc);
     expect(nlDoc.getFullName()).andReturn(nl).anyTimes();
     expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), 
         eq(nl), eq(false))).andReturn(null);
     BaseObject obj = new BaseObject();
     expect(doc1.newObject(eq("Celements.NewsletterReceiverClass"), same(context)))
     .andReturn(obj);
     expect(doc1.getObject(eq("Celements.NewsletterReceiverClass"), eq("subscribed"), 
         eq(nl), eq(false))).andReturn(obj);
     xwiki.saveDocument(same(doc1), same(context));
     expectLastCall();
     expect(messageTool.get(eq("cel_newsletter_subscriber_invalid"))).andReturn("invalid")
         .times(2);
     expect(messageTool.get(eq("cel_newsletter_subscriber_inactive"))).andReturn(
         "inactive").once();
     replayAll(context, doc1, messageTool, nlDoc);
     assertEquals(result, plugin.batchImportReceivers(true, importData, nl, context));
     verifyAll(context, doc1, messageTool, nlDoc);
   }
 
   @Test
   public void testExtractEmailFromString() {
     assertEquals("bla@da.ch", plugin.extractEmailFromString("gla@bla@da.ch"));
     assertEquals("xpo+bedo@credo.ch", plugin.extractEmailFromString("xpo+bedo@credo.ch"));
     assertEquals("aco@bla.tra", plugin.extractEmailFromString("H. Meier <aco@bla.tra>"));
     assertEquals("aco@bla.tra", plugin.extractEmailFromString("wucht aco@bla.tra trala"));
   }
 
   @Test
   public void testContainsEmailAddress_true() {
     assertTrue(plugin.containsEmailAddress("gla@bla@da.ch"));
     assertTrue(plugin.containsEmailAddress("a@b.c"));
     assertTrue(plugin.containsEmailAddress("xpo+bedo@credo.ch"));
     assertTrue(plugin.containsEmailAddress("hans_wurscht@plumpl.com"));
     assertTrue(plugin.containsEmailAddress("friz.meier@geier.xy"));
     assertTrue(plugin.containsEmailAddress("plimm@blubb.co.uk"));
     assertTrue(plugin.containsEmailAddress("baem@da-ga_ba.na_za.bla-ta.ch"));
     assertTrue(plugin.containsEmailAddress("Heiri Meier <aco@bla.tra>"));
     assertTrue(plugin.containsEmailAddress("wurscht aco@bla.tra tralla"));
     assertTrue(plugin.containsEmailAddress("trullalla@is.icc.u-tokai.ac.jp"));
   }
 
   @Test
   public void testContainsEmailAddress_false() {
     assertFalse(plugin.containsEmailAddress(""));
     assertFalse(plugin.containsEmailAddress("quatsch"));
     assertFalse(plugin.containsEmailAddress("agar@agar"));
     assertFalse(plugin.containsEmailAddress("@blab.com"));
   }
   
   @Test
   public void testGetContainsEmailRegex() {
     assertTrue("myName@mail.com".matches(plugin.getContainsEmailRegex()));
     assertFalse("Nice meaningless text.".matches(plugin.getContainsEmailRegex()));
   }
   
   @Test
   public void testSplitImportDataToEmailCandidates_one() {
     String importData = "\r\n,My Name <myName@email.com>,,,,";
     String[] result = plugin.splitImportDataToEmailCandidates(importData);
     List<String> resultList = new ArrayList();
     for (String string : result) {
       resultList.add(string);
     }
     assertTrue(resultList.contains("My Name <myName@email.com>"));
   }
   
   @Test
   public void testSplitImportDataToEmailCandidates_several() {
     String importData = "\r\n,myName @email.com,,,myName2@email\nmyName@email.com";
     String[] result = plugin.splitImportDataToEmailCandidates(importData);
     List<String> resultList = new ArrayList();
     for (String string : result) {
       resultList.add(string);
     }
     assertTrue(resultList.contains("myName @email.com"));
     assertTrue(resultList.contains("myName2@email"));
     assertTrue(resultList.contains("myName@email.com"));
   }
   
   //*************************************************************************************
   // HELPER
   //*************************************************************************************
 
   
   private void replayAll(Object ... mocks) {
     replay(xwiki, blogServiceMock);
     replay(mocks);
   }
 
   private void verifyAll(Object ... mocks) {
     verify(xwiki, blogServiceMock);
     verify(mocks);
   }
 
   private DocumentReference getDocRef(String spaceName, String pageName) {
     return new DocumentReference(context.getDatabase(), spaceName, pageName);
   }
 
   private Vector<BaseObject> getArticleObjVect(int i) {
     Vector<BaseObject> objVec = new Vector<BaseObject>();
     BaseObject obj = new BaseObject();
     obj.setName("Space.Article" + i);
     obj.setStringValue("lang", "de");
     obj.setDateValue("publishdate", new Date(i, 11, 31));
     obj.setDateValue("archivedate", new Date(9999, 0, 1));
     obj.setStringValue("title", "the title");
     obj.setStringValue("content", "the content");
     objVec.add(obj);
     return objVec;
   }
 
   private String getAllArticlesHQL(String artSpace) {
     return "select doc.fullName from XWikiDocument as doc, BaseObject as obj, " +
         "DateProperty as date, StringProperty as lang where obj.name=doc.fullName and " +
         "obj.className='XWiki.ArticleClass' and (doc.space = '" + artSpace + "' ) and " +
         "lang.id.id=obj.id and lang.id.name='lang' and lang.value = 'de' and obj.id = " +
         "date.id.id and date.id.name='publishdate' order by date.value desc, " +
         "doc.creationDate desc ";
   }
   
   private String getBlogConfigHQL(String artSpace) {
     return "select doc.fullName from XWikiDocument as doc, BaseObject as obj, " +
         "StringProperty bspace where obj.name=doc.fullName and " +
         "obj.className='Celements2.BlogConfigClass' and obj.id = bspace.id.id and " +
         "bspace.id.name = 'blogspace' and bspace.value = '" + artSpace + "'";
   }
 
 }
 
