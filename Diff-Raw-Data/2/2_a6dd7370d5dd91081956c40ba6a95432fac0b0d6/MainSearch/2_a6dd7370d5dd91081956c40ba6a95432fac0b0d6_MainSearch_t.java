 package com.eli.web.action;
 
 
 
 import com.eli.index.manager.MultiNRTSearcherAgent;
 import com.eli.index.manager.ZhihuIndexManager;
 import com.eli.web.BasicAction;
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.cjk.CJKAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.*;
 import org.apache.lucene.search.highlight.*;
 import org.apache.lucene.util.Version;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MainSearch extends BasicAction {
     private static final Logger logger = Logger.getLogger(MainSearch.class);
     private static Analyzer analyzer= new CJKAnalyzer(Version.LUCENE_36);
 
     @Override
     protected void execute() throws IOException {
        String token = super.getParam("q", "");
         int offset  = Integer.parseInt(super.getParam("offset", "0"));
         int limit  = Integer.parseInt(super.getParam("limit", "20"));
 
         super.put("query", token);
         super.put("offset", offset);
         super.put("limit", limit);
         super.put("total", 0);
         super.put("page", 0);
 
         QueryParser qp = new QueryParser(Version.LUCENE_36, "content.NGRAM", analyzer);
         QueryParser qp1 = new QueryParser(Version.LUCENE_36, "title.NGRAM", analyzer);
         List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
         MultiNRTSearcherAgent agent = ZhihuIndexManager.INSTANCE.acquire();
         try{
             IndexSearcher searcher = agent.getSearcher();
             Query sub = qp.parse(token);
             Query sub1 = qp1.parse(token);
 
             BooleanQuery query = new BooleanQuery();
             query.add(sub, BooleanClause.Occur.SHOULD);
             query.add(sub1, BooleanClause.Occur.SHOULD);
 
             TopDocs hits = searcher.search(query, offset + limit);
             QueryScorer scorer = new QueryScorer(query);
             Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"hi\">", "</span>"), new SimpleHTMLEncoder(), scorer);
             highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
 
             super.put("total", hits.totalHits);
 
             super.put("page",((hits.totalHits+19)/20)+1);
 
             for (int i = offset; i < hits.scoreDocs.length && i < offset + limit; i++) {
                 int docId = hits.scoreDocs[i].doc;
                 Document doc = searcher.doc(docId);
                 String content =  doc.get("content.NGRAM");
                 String title =  doc.get("title.NGRAM");
                 if (title == null)
                     title = "";
                 TokenStream stream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docId, "content.NGRAM", doc, analyzer );
                 content = highlighter.getBestFragment(stream, content);
                 stream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docId, "title.NGRAM", doc, analyzer );
                 title = highlighter.getBestFragment(stream, title);
                 if (title == null)
                     title = "无标题";
                 if  (content == null)
                     content = "无内容";
                 String url     =  doc.get("url.None");
                 Map<String,String> map = new HashMap<String, String>();
                 map.put("content", content);
                 map.put("title", title);
                 map.put("url", url);
                 ret.add(map);
             }
         } catch (Exception e) {
             logger.error(e);
         } finally {
             ZhihuIndexManager.INSTANCE.release(agent);
         }
         super.put("ret", ret);
 
     }
 
 }
