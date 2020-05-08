   
   /*
    * Preppa, Inc.
    * 
    * Copyright 2009. All rights
   reserved.
    * 
    * $Id$
    */
 
 package com.preppa.web.components;
 
 import java.util.List;
 import com.preppa.web.data.ArticleDAO;
 import com.preppa.web.entities.Article;
 import com.preppa.web.pages.contribution.article.ShowArticle;
 import com.preppa.web.pages.contribution.article.search.Index;
 import java.util.ArrayList;
 
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.search.Query;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.hibernate.HibernateSessionManager;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.search.FullTextSession;
 import org.hibernate.search.Search;
 import org.slf4j.Logger;
 
 /**
  *
  * @author Jan Jan
  */
 public class ArticleSearch {
     @Property
     private String aName;
     @Parameter
     private String searcher;
     @Component(id = "articlesearch")
     private Form myform;
     @Inject
     private ArticleDAO articleDAO;
     @Inject
     private HibernateSessionManager sessionManager;
     private Session session;
     @InjectPage
     private ShowArticle _next;
     @Inject
     private Logger logger;
 
     @InjectPage
     private Index articleResults;
 //        Object onSuccessFromArticleSearch() {
 //		_next.set(aName);
 //		return _next;
 //	}
 
 
 @SetupRender
 void setSearchString()
 {
          if(this.searcher == null)
          {
              aName = "";
          }
          else
          {
              aName = searcher;
          }
  }
      Object onSuccessFromArticleSearch() {
 
          session = sessionManager.getSession();
 
         FullTextSession fullTextSession = Search.getFullTextSession(session);
         Transaction tx = fullTextSession.beginTransaction();
 
         //create Lucene Search query
         String[] fields = new String[]{"title", "body", "article.taglist"};
 
         MultiFieldQueryParser parser = new MultiFieldQueryParser(fields,
                 new StandardAnalyzer());
         Query query = null;
         try {
             //Note this is Lucene query
              query = parser.parse(aName);
         } catch (ParseException ex) {
             logger.warn(ex.getMessage());
         }
         //wrap lucene query in Hibernate query
         org.hibernate.Query hiQuery = fullTextSession.createFullTextQuery(query, Article.class);
         List<Article> results = hiQuery.list();
 
         tx.commit();
         
         System.out.println("There are " + results.size() + " results");
         articleResults.setSearchResults(results, aName);
         return articleResults;
      }
 
     List<String> onProvideCompletionsFromAName(String partial)
     {
         List<Article> matches = articleDAO.findByPartialName(partial);
 
         List<String> result = new ArrayList<String>();
 
             for (Article a : matches)
             {
                 if (!result.contains(a.getTitle())) {
                     result.add(a.getTitle());
                 }
             }
 
         return result;
     }
 }
