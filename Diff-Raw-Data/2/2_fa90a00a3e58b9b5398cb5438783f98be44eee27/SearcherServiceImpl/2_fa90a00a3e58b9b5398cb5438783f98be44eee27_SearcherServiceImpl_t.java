 package com.xiaoma.sousuo.service.impl;
 
 import com.xiaoma.constant.GlobalConstant;
 import com.xiaoma.sousuo.po.SearchResult;
 import com.xiaoma.sousuo.po.SearchView;
 import com.xiaoma.sousuo.searcher.*;
 import com.xiaoma.sousuo.service.MemCachedClientService;
 import com.xiaoma.sousuo.service.SearcherSevice;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleFragmenter;
 import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.wltea.analyzer.lucene.IKAnalyzer;
 import org.wltea.analyzer.lucene.IKQueryParser;
 import org.wltea.analyzer.lucene.IKSimilarity;
 import ru.perm.kefir.bbcode.TextProcessor;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: wangchao
  * Date: 2011-06-14 16:58
  *
  * @author <a href="mailto:ohergal@gmail.com">ohergal</a>
  */
 public class SearcherServiceImpl implements SearcherSevice {
     private static final org.apache.commons.logging.Log log = LogFactory.getLog(SearcherServiceImpl.class);
     private byte[] updatelock = new byte[0];
 
     private GlobalConstant constant;
 
 
     private Directory directory;
     private IndexWriter indexWriter_all;
     private IndexReader indexReader;
     private IndexSearcher indexSearcher;
 
     private Directory directory_ask;
     private IndexWriter indexWriter_ask;
     private IndexReader indexReader_ask;
     private IndexSearcher indexSearcher_ask;
 
     private Directory directory_dl;
     private IndexWriter indexWriter_dl;
     private IndexReader indexReader_dl;
     private IndexSearcher indexSearcher_dl;
 
     private Directory directory_blog;
     private IndexWriter indexWriter_blog;
     private IndexReader indexReader_blog;
     private IndexSearcher indexSearcher_blog;
 
     private Directory directory_thread;
     private IndexWriter indexWriter_thread;
     private IndexReader indexReader_thread;
     private IndexSearcher indexSearcher_thread;
 
     private Directory directory_post;
     private IndexWriter indexWriter_post;
     private IndexReader indexReader_post;
     private IndexSearcher indexSearcher_post;
 
     private Directory directory_article;
     private IndexWriter indexWriter_article;
     private IndexReader indexReader_article;
     private IndexSearcher indexSearcher_article;
 
     @Autowired
     private MemCachedClientService memCachedClientService;
     @Autowired
     private TextProcessor textProcessor;
 
     public SearcherServiceImpl(GlobalConstant constant) {
         this.constant = constant;
         initSearcher(this.constant);
     }
 
     /**
      * 初始化
      *
      * @param constant
      */
     public void initSearcher(GlobalConstant constant) {
         this.constant = constant;
         this.initIndexReaderAll();
         this.initIndexReaderAsk();
         this.initIdnexReaderDl();
         this.initIdnexReaderBlog();
         this.initIdnexReaderThread();
         this.initIdnexReaderPost();
         this.initIdnexReaderArticle();
     }
 
 
     /**
      * 初始化索引更新和删除的工具
      */
     private void initIndexReaderAll() {
         try {
             Directory directory = FSDirectory.open(new File(constant.getIndexPath()));
             this.directory = directory;
             // 初始化索引更新和写入以及删除的IndexWriter
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_all = new IndexWriter(this.directory, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_all, true);
             this.indexReader = reader;
             // 初始化搜索器
             this.indexSearcher = new IndexSearcher(reader);
             this.indexSearcher.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("index writer all init error" + e);
         }
     }
 
     /**
      * 初始化索引更新和删除的工具
      */
     private void initIndexReaderAsk() {
         try {
             Directory directory_ask = FSDirectory.open(new File(constant.getIndexPath_ASK()));
             this.directory_ask = directory_ask;
             // 初始化索引更新和写入以及删除的IndexWriter
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_ask = new IndexWriter(this.directory_ask, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_ask, true);
             this.indexReader_ask = reader;
             // 初始化搜索器
             this.indexSearcher_ask = new IndexSearcher(reader);
             this.indexSearcher_ask.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("index writer ask init error" + e);
         }
     }
 
     private void initIdnexReaderDl() {
         try {
             Directory directory_dl = FSDirectory.open(new File(constant.getIndexPath_DL()));
             this.directory_dl = directory_dl;
             // 初始化索引更新和写入以及删除的IndexWriter
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_dl = new IndexWriter(this.directory_dl, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_dl, true);
             this.indexReader_dl = reader;
             // 初始化搜索器
             this.indexSearcher_dl = new IndexSearcher(reader);
             this.indexSearcher_dl.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("index writer dl init error" + e);
         }
     }
 
     private void initIdnexReaderPost() {
         try {
             Directory directory_post = FSDirectory.open(new File(constant.getIndexPath_POST()));
             this.directory_post = directory_post;
             // 初始化索引更新和写入以及删除的IndexWriter
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_post = new IndexWriter(this.directory_post, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_post, true);
             this.indexReader_post = reader;
             // 初始化搜索器
             this.indexSearcher_post = new IndexSearcher(reader);
             this.indexSearcher_post.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("index writer post init error" + e);
         }
     }
 
     private void initIdnexReaderThread() {
         try {
             Directory directory_thread = FSDirectory.open(new File(constant.getIndexPath_THREAD()));
             this.directory_thread = directory_thread;
             // 初始化索引更新和写入以及删除的IndexWriter
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_thread = new IndexWriter(this.directory_thread, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_thread, true);
             this.indexReader_thread = reader;
             // 初始化搜索器
             this.indexSearcher_thread = new IndexSearcher(reader);
             this.indexSearcher_thread.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("index writer thread init error" + e);
         }
     }
 
     private void initIdnexReaderBlog() {
         try {
             Directory directory_blog = FSDirectory.open(new File(constant.getIndexPath_BLOG()));
             this.directory_blog = directory_blog;
             // 初始化索引更新和写入以及删除的IndexWriter
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_blog = new IndexWriter(this.directory_blog, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_blog, true);
             this.indexReader_blog = reader;
             // 初始化搜索器
             this.indexSearcher_blog = new IndexSearcher(reader);
             this.indexSearcher_blog.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("index writer blog init error" + e);
         }
     }
 
     private void initIdnexReaderArticle() {
         try {
             Directory directory_article = FSDirectory.open(new File(constant.getIndexPath_ARTICLE()));
             this.directory_article = directory_article;
             // 初始化索引更新和写入以及删除的IndexWriter
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_article = new IndexWriter(this.directory_article, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_article, true);
             this.indexReader_article = reader;
             // 初始化搜索器
             this.indexSearcher_article = new IndexSearcher(reader);
             this.indexSearcher_article.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("index writer article init error" + e);
         }
     }
 
 
     public SearchResult search(String keywords, String page, String type) {
         // 缓存处理
         SearchResult searchResult = new SearchResult();
         String memkey = MD5.encode(keywords);
         log.debug("memcache key :" + keywords);
         SearchResult cacheResult = memCachedClientService.getSearchResult(memkey);
         // 当缓存不存在的时候 重新查询
         if (cacheResult == null) {
             log.debug("has not cache hit ,so start research");
             searchResult = research(keywords, page, type);
             memCachedClientService.setSearchResult(memkey, searchResult);
             memCachedClientService.setCommon(memkey + "num", searchResult.getTotalNum());
         } else {
             // 先判断是否需要重新查询,重新查询的条件是当前页所在的查询总数大于之前保存的doc的总数,且要在可能的最大查询范围之内
             int currentPage = Integer.parseInt(page);
             // 当前页的最大值
             int currentPage_max_doc_num = currentPage * 10;
             // 求最大查询范围
             int max_query_num = 0;
             max_query_num = (cacheResult.getTotalNum() % 100) > 0 ?
                     ((cacheResult.getTotalNum() / 100) + 1) * 100 : cacheResult.getTotalNum();
             int totalNum_now = this.researchForTotalNum(keywords, type);
             if (memCachedClientService.getCommon(memkey + "num") != null) {
                 int old_total = (Integer) memCachedClientService.getCommon(memkey + "num");
                 if (old_total < totalNum_now) {
                     // 如果搜索结果有变化
                     log.debug("cache record has change ,old is :" + old_total +",new is :" + totalNum_now);
                     searchResult = research(keywords, page, type);
                     memCachedClientService.setSearchResult(memkey, searchResult);
                     memCachedClientService.setCommon(memkey + "num", searchResult.getTotalNum());
                 } else if (max_query_num > currentPage_max_doc_num && currentPage_max_doc_num > cacheResult.getCurrentQueryMaxNum()) {
                     log.debug("in the query rang but not cache");
                     // 如果不在缓存里的缓存范围 且在允许的查询范围内
                     searchResult = research(keywords, page, type);
                     memCachedClientService.setSearchResult(memkey, searchResult);
                     memCachedClientService.setCommon(memkey + "num", searchResult.getTotalNum());
                } else if (currentPage_max_doc_num <= cacheResult.getCurrentQueryMaxNum()) {
                     // 直接从缓存查询
                     log.debug("query from the cache");
                     searchResult = researchFromCache(cacheResult, currentPage);
                     memCachedClientService.setSearchResult(memkey, searchResult);
                     memCachedClientService.setCommon(memkey + "num", searchResult.getTotalNum());
                 }
             }
 
         }
         return searchResult;
     }
 
     /**
      * 重新查询总数
      *
      * @param keywords
      * @param type
      * @return
      */
     public int researchForTotalNum(String keywords, String type) {
         int totalNum;
         String[] fields = {"title", "description", "content"};
         try {
             Query query = IKQueryParser.parseMultiField(fields, SearcherUtil.pretreatmentKeywords(keywords));
             // 查询
             TopDocs topDocs = indexSearcher.search(query, 1);
             totalNum = topDocs.totalHits;
         } catch (IOException e) {
             log.error("search total number erorr" + e);
             totalNum = 0;
         }
         return totalNum;
     }
 
 
     public SearchResult researchFromCache(SearchResult cacheResult, int currentPage) {
         // 这种情况不用再查询了,只需要分析是否需要重组result
         // 拿出缓存里的list
         List result = new ArrayList();
         // 总数
         int totalNum = cacheResult.getTotalNum();
         // 结果的开始数
         int start = (currentPage - 1) * 10 + 1;
         int end = currentPage * 10;
         // 如果开始数就大于总数 此次搜索结果为0
         if (start > totalNum) {
             return null;
         }
         // 如果结束数大于总数
         if (end > totalNum) {
             end = totalNum;
         }
         cacheResult.setEnd(end);
         cacheResult.setStart(start);
         cacheResult.setCurrentPageNum(currentPage);
         Document[] docs = cacheResult.getDocs();
 
         for (int i = start; i <= end; i++) {
             Document targetDoc = docs[i - 1];
             SearchView sv = new SearchView();
             sv.setDesc(targetDoc.get("description"));
             sv.setTitle(targetDoc.get("title"));
             sv.setUrl(targetDoc.get("url"));
             result.add(sv);
         }
         cacheResult.setCurrentPage(result);
         return cacheResult;
     }
 
     /**
      * 重新搜索结果
      *
      * @param keywords
      * @param page
      * @param type
      * @return
      */
     public SearchResult research(String keywords, String page, String type) {
         int currentPage = Integer.parseInt(page);
         SearchResult searchResult = new SearchResult();
         searchResult.setSearchtype(type);
         String keywords_url = keywords.replaceAll("\\s", "+");
         searchResult.setKeywords(keywords_url);
         // 先查询有没有缓存
         List result = new ArrayList();
         String[] fields = {"title", "description", "content"};
         try {
             Query query = IKQueryParser.parseMultiField(fields, SearcherUtil.pretreatmentKeywords(keywords));
             searchResult.setCurrentQueryMaxNum(0);
             // 每次多查询100个就是10页
             int pageQueryNum = ((currentPage / 10) + 1) * 100;
             // 查询
             TopDocs topDocs = indexSearcher.search(query, pageQueryNum);
             log.debug("命中:" + topDocs.totalHits);
             //URLEncoder.encode()
             // 总数
             int totalNum = topDocs.totalHits;
             // 结果的开始数
             int start = (currentPage - 1) * 10 + 1;
             int end = currentPage * 10;
             // 如果开始数就大于总数 此次搜索结果为0
             if (start > totalNum) {
                 return searchResult;
             }
             // 如果结束数大于总数
             if (end > totalNum) {
                 end = totalNum;
             }
             int totalPage = totalNum / 10 + 1;
             searchResult.setEnd(end);
             searchResult.setStart(start);
             searchResult.setTotalNum(totalNum);
             searchResult.setCurrentPageNum(currentPage);
             searchResult.setTotalPage(totalPage);
 
 
             //输出结果
             ScoreDoc[] scoreDocs = topDocs.scoreDocs;
             Document[] docs = new Document[totalNum];
             for (int ai = 0; ai < scoreDocs.length; ai++) {
                 docs[ai] = indexSearcher.doc(scoreDocs[ai].doc);
             }
             for (int i = start; i <= end; i++) {
                 Document targetDoc = docs[i - 1];
                 SearchView sv = new SearchView();
                 sv.setDesc(targetDoc.get("description"));
                 sv.setTitle(targetDoc.get("title"));
                 sv.setUrl(targetDoc.get("url"));
                 result.add(sv);
                 //log.debug("内容:" + targetDoc.toString());
                 //log.debug("url:" + targetDoc.get("url"));
             }
             searchResult.setDocs(docs);
             searchResult.setCurrentPage(result);
             searchResult.setCurrentQueryMaxNum(pageQueryNum);
         } catch (IOException e) {
             log.error("search query erorr" + e);
             searchResult = null;
         }
         return searchResult;
     }
 
 
     public void deleteDl(HashMap<String, Object> dl) {
         Term term = new Term("sid", "dl" + String.valueOf(dl.get("id")));
         if (this.indexWriter_all != null) {
             try {
                 this.indexWriter_all.deleteDocuments(term);
             } catch (IOException e) {
                 log.error("delete dl in all by term error :" + e);
             }
         }
         if (this.indexWriter_dl != null) {
             try {
                 this.indexWriter_dl.deleteDocuments(term);
             } catch (IOException e) {
                 log.error("delete dl by term error :" + e);
             }
         }
     }
 
     /**
      * 删除一个问答的帖子
      *
      * @param ask
      */
     public void deleteAsk(HashMap<String, Object> ask) {
         Term ask_t = new Term("sid", "ask" + String.valueOf(ask.get("id")));
         if (this.indexWriter_all != null) {
             try {
                 this.indexWriter_all.deleteDocuments(ask_t);
             } catch (IOException e) {
                 log.error("delete ask by term error :" + e);
             }
         }
         if (this.indexWriter_ask != null) {
             try {
                 this.indexWriter_ask.deleteDocuments(ask_t);
             } catch (IOException e) {
                 log.error("delete ask by term error :" + e);
             }
         }
     }
 
     public void deleteSource(HashMap<String, Object> map) {
         Term ask_t = new Term("sid", "source" + String.valueOf(map.get("id")));
         if (this.indexWriter_all != null) {
             try {
                 this.indexWriter_all.deleteDocuments(ask_t);
             } catch (IOException e) {
                 log.error("delete source by term error :" + e);
             }
         }
 
     }
 
     public void deleteTranslation(HashMap<String, Object> map) {
         Term ask_t = new Term("sid", "tran" + String.valueOf(map.get("id")));
         if (this.indexWriter_all != null) {
             try {
                 this.indexWriter_all.deleteDocuments(ask_t);
             } catch (IOException e) {
                 log.error("delete translation by term error :" + e);
             }
         }
     }
 
     /**
      * 增加一个问答
      *
      * @param ask
      */
     public void addAsk(HashMap<String, Object> ask) {
         log.debug("add a ask");
         if (this.indexWriter_ask != null && this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 String url = constant.getASK_BASE_URL() + String.valueOf(ask.get("id"));
                 String content = HtmlTools.html2Text((String) ask.get("content"));
                 // 截取200字符串 ,并清除bbcode
                 String desc = TextHandleTools.cutstring(HtmlTools.html2Text((String) ask.get("content")), 200);
 
                 Timestamp ts = (Timestamp) ask.get("updated_at");
                 String updated_at = String.valueOf(ts.getTime() / 1000);
                 Field f_title = new Field("title", (String) ask.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(2.2f);
                 doc.add(new Field("sid", "ask" + String.valueOf(ask.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(ask.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "ask", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("status", String.valueOf(ask.get("status")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("updated_at", updated_at, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_ask.addDocument(doc);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new ask error :" + e);
             }
         }
     }
 
     public void addSource(HashMap<String, Object> map) {
         log.debug("add a source");
         if (this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 String url = constant.getARTICLE_BASE_URL() + "/sources/" + String.valueOf(map.get("id"));
                 String content = HtmlTools.html2Text((String) map.get("content"));
                 // 截取200字符串 ,并清除bbcode
                 String desc = TextHandleTools.cutstring(HtmlTools.html2Text((String) map.get("desc")), 200);
 
                 Timestamp ts = (Timestamp) map.get("updated_at");
                 String updated_at = String.valueOf(ts.getTime() / 1000);
                 Field f_title = new Field("title", (String) map.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(2.4f);
                 doc.add(new Field("sid", "source" + String.valueOf(map.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(map.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "source", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("status", String.valueOf(map.get("status")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("updated_at", updated_at, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new source error :" + e);
             }
         }
     }
 
     public void addTranslation(HashMap<String, Object> map) {
         log.debug("add a translation");
         if (this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 String url = constant.getARTICLE_BASE_URL() + "/sources/" + String.valueOf(map.get("id"))
                         + "/translations/" + String.valueOf(map.get("source_id"));
                 String content = HtmlTools.html2Text((String) map.get("content"));
                 // 截取200字符串 ,并清除bbcode
                 String desc = TextHandleTools.cutstring(HtmlTools.html2Text((String) map.get("desc")), 200);
 
                 Timestamp ts = (Timestamp) map.get("updated_at");
                 String updated_at = String.valueOf(ts.getTime() / 1000);
                 Field f_title = new Field("title", (String) map.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(2.4f);
                 doc.add(new Field("sid", "tran" + String.valueOf(map.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(map.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "tran", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("status", String.valueOf(map.get("status")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("updated_at", updated_at, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new translation error :" + e);
             }
         }
     }
 
 
 
 
     public void addDl(HashMap<String, Object> dl) {
         log.debug("add a dl");
         if (this.indexWriter_dl != null && this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 String url = constant.getDL_BASE_URL() + String.valueOf(dl.get("id"));
                 String content = HtmlTools.html2Text((String) dl.get("content"));
                 // 截取200字符串
                 String desc = TextHandleTools.cutstring(HtmlTools.html2Text((String) dl.get("desc")), 200);
 
                 Timestamp ts = (Timestamp) dl.get("updated_at");
                 String updated_at = String.valueOf(ts.getTime() / 1000);
                 Field f_title = new Field("title", (String) dl.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(2.0f);
                 doc.add(new Field("updated_at", updated_at, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("sid", "dl" + String.valueOf(dl.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(dl.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "dl", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_dl.addDocument(doc);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new ask error :" + e);
             }
         }
     }
 
     public void addArticle(HashMap<String, Object> article) {
         log.debug("add a article");
         if (this.indexWriter_article != null && this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 // 组出url
                 int datei = (Integer) article.get("pubdate");
                 long datel = new Long(datei) * 1000;
                 Date date = new Date(datel);
                 String date_str = DateUtil.getDateText(date, "yyyyMMdd");
                 String article_id = String.valueOf(article.get("id"));
                 String url = constant.getARTICLE_BASE_URL() + String.valueOf(article.get("dir")) + "/"
                         + date_str + "/" + article_id + ".html";
                 // 替换掉不需要的cms路径代码
                 url = url.replaceAll("\\{cmspath\\}", "");
                 String desc = TextHandleTools.cutstring(HtmlTools.html2Text((String) article.get("desc")), 200);
                 String content = HtmlTools.html2Text((String) article.get("content"));
                 // 截取200字符串
 
                 Field f_title = new Field("title", (String) article.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(2.5f);
                 doc.add(new Field("updated_at", String.valueOf(datei), Field.Store.YES, Field.Index.NOT_ANALYZED));
 
                 doc.add(new Field("sid", "article" + String.valueOf(article.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(article.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "article", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_article.addDocument(doc);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new article error :" + e);
             }
         }
     }
 
     public void addPost(HashMap<String, Object> post) {
         log.debug("add a post");
         if (this.indexWriter_post != null && this.indexWriter_all != null) {
             try {
                 if (post.get("id") != null && post.get("pid") != null && post.get("title") != null) {
                     Document doc = new Document();
                     String threadid = String.valueOf(post.get("id"));
                     String uid = String.valueOf(post.get("user_id"));
                     String url = constant.getTHREAD_BASE_URL();
                     url = url.replaceAll("\\{threadid\\}", threadid);
                     String desc = this.clearBBcode(TextHandleTools.cutstring(HtmlTools.html2Text((String) post.get("content")), 200));
                     // 截取200字符串
                     String content = HtmlTools.html2Text((String) post.get("content"));
 
                     Field f_title = new Field("title", (String) post.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                     f_title.setBoost(1.8f);
                     Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                     f_desc.setBoost(1.6f);
                     Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                     f_content.setBoost(1.4f);
                     doc.setBoost(0.9f);
                     doc.add(new Field("updated_at", String.valueOf(post.get("dateline")), Field.Store.YES, Field.Index.NOT_ANALYZED));
 
 
                     doc.add(new Field("sid", "post" + String.valueOf(post.get("pid")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                     doc.add(new Field("id", String.valueOf(post.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                     doc.add(new Field("type", "post", Field.Store.YES, Field.Index.NOT_ANALYZED));
                     doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                     doc.add(f_title);
                     doc.add(f_desc);
                     doc.add(f_content);
                     indexWriter_post.addDocument(doc);
                     indexWriter_all.addDocument(doc);
                 }
             } catch (IOException e) {
                 log.error("add new post error :" + e);
             }
         }
     }
 
     public void addThread(HashMap<String, Object> thread) {
         log.debug("add a thread");
         if (this.indexWriter_thread != null && this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 String threadid = String.valueOf(thread.get("id"));
                 String uid = String.valueOf(thread.get("user_id"));
                 String url = constant.getTHREAD_BASE_URL();
                 url = url.replaceAll("\\{threadid\\}", threadid);
                 String desc = this.clearBBcode(TextHandleTools.cutstring(HtmlTools.html2Text((String) thread.get("content")), 200));
                 // 截取200字符串
                 String content = HtmlTools.html2Text((String) thread.get("content"));
 
                 Field f_title = new Field("title", (String) thread.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(1.8f);
                 doc.add(new Field("updated_at", String.valueOf(thread.get("dateline")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("sid", "thread" + String.valueOf(thread.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(thread.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "thread", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_thread.addDocument(doc);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new thread error :" + e);
             }
         }
     }
 
     public void addBlog(HashMap<String, Object> blog) {
         log.debug("add a blog");
         if (this.indexWriter_blog != null && this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 String blogid = String.valueOf(blog.get("id"));
                 String uid = String.valueOf(blog.get("user_id"));
                 String url = constant.getBLOG_BASE_URL();
                 url = url.replaceAll("\\{blogid\\}", blogid);
                 url = url.replaceAll("\\{uid\\}", uid);
                 String desc = this.clearBBcode(TextHandleTools.cutstring(HtmlTools.html2Text((String) blog.get("content")), 200));
                 // 截取200字符串
                 String content = HtmlTools.html2Text((String) blog.get("content"));
                 Field f_title = new Field("title", (String) blog.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(1.2f);
                 doc.add(new Field("updated_at", String.valueOf(blog.get("dateline")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("sid", "blog" + String.valueOf(blog.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(blog.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "blog", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_blog.addDocument(doc);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new blog error :" + e);
             }
         }
 
     }
 
     /**
      * 添加一个新的专题
      *
      * @param map
      */
     public void addZhuanti(HashMap<String, Object> map) {
         log.debug("add a zhuanti");
         if (this.indexWriter_blog != null && this.indexWriter_all != null) {
             try {
                 Document doc = new Document();
                 String url = (String) map.get("url");
                 String desc = TextHandleTools.cutstring(HtmlTools.html2Text((String) map.get("desc")), 200);
                 Timestamp ts = (Timestamp) map.get("updated_at");
                 String updated_at = String.valueOf(ts.getTime() / 1000);
                 // 截取200字符串
                 String content = HtmlTools.html2Text((String) map.get("content"));
                 Field f_title = new Field("title", (String) map.get("title"), Field.Store.YES, Field.Index.ANALYZED);
                 f_title.setBoost(1.8f);
                 Field f_desc = new Field("description", desc, Field.Store.YES, Field.Index.ANALYZED);
                 f_desc.setBoost(1.6f);
                 Field f_content = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED);
                 f_content.setBoost(1.4f);
                 doc.setBoost(5.0f);
                 doc.add(new Field("updated_at", updated_at, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("sid", "zhuanti" + String.valueOf(map.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("id", String.valueOf(map.get("id")), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("type", "zhuanti", Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
                 doc.add(f_title);
                 doc.add(f_desc);
                 doc.add(f_content);
                 indexWriter_all.addDocument(doc);
             } catch (IOException e) {
                 log.error("add new zhuanti error :" + e);
             }
         }
     }
 
     /**
      * 删除专题
      *
      * @param map
      */
     public void deleteZhuanti(HashMap<String, Object> map) {
         Term ask_t = new Term("sid", "zhuanti" + String.valueOf(map.get("id")));
         if (this.indexWriter_all != null) {
             try {
                 this.indexWriter_all.deleteDocuments(ask_t);
             } catch (IOException e) {
                 log.error("delete zhuanti by term error :" + e);
             }
         }
     }
 
     /**
      * 刷新集合索引的reader和searcher
      */
     public void refreshReaderAll() {
         log.debug("start to refresh all index");
         synchronized (updatelock) {
             try {
                 IndexReader reader = this.indexReader.reopen();
                 this.indexReader.close();
                 this.indexReader = reader;
                 // 重建搜索器
                 this.indexSearcher.close();
                 this.indexSearcher = new IndexSearcher(reader);
                 this.indexSearcher.setSimilarity(new IKSimilarity());
             } catch (IOException e) {
                 log.error("reopen indexReader for all error :" + e);
             }
             this.refreshReaderArticle();
             this.refreshReaderBlog();
             this.refreshReaderAsk();
             this.refreshReaderDl();
             this.refreshReaderPost();
             this.refreshReaderThread();
         }
         log.debug("end to refresh all index");
     }
 
     public void refreshReaderAsk() {
         log.debug("start to refresh ask index");
         try {
             IndexReader reader = this.indexReader_ask.reopen();
             this.indexReader_ask.close();
             this.indexReader_ask = reader;
             // 重建搜索器
             this.indexSearcher_ask.close();
             this.indexSearcher_ask = new IndexSearcher(reader);
             this.indexSearcher_ask.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("reopen indexReader for ask error :" + e);
         }
         log.debug("end to refresh ask index");
     }
 
     public void refreshReaderDl() {
         log.debug("start to refresh dl index");
         try {
             IndexReader reader = this.indexReader_dl.reopen();
             this.indexReader_dl.close();
             this.indexReader_dl = reader;
             // 重建搜索器
             this.indexSearcher_dl.close();
             this.indexSearcher_dl = new IndexSearcher(reader);
             this.indexSearcher_dl.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("reopen indexReader for dl error :" + e);
         }
         log.debug("end to refresh dl index");
     }
 
     public void refreshReaderArticle() {
         log.debug("start to refresh article index");
         try {
             IndexReader reader = this.indexReader_article.reopen();
             this.indexReader_article.close();
             this.indexReader_article = reader;
             // 重建搜索器
             this.indexSearcher_article.close();
             this.indexSearcher_article = new IndexSearcher(reader);
             this.indexSearcher_article.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("reopen indexReader for article error :" + e);
         }
         log.debug("end to refresh article index");
     }
 
     public void refreshReaderPost() {
         log.debug("start to refresh post index");
         try {
             IndexReader reader = this.indexReader_post.reopen();
             indexReader_post.close();
             this.indexReader_post = reader;
             // 重建搜索器
             this.indexSearcher_post.close();
             this.indexSearcher_post = new IndexSearcher(reader);
             this.indexSearcher_post.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("reopen indexReader for post error :" + e);
         }
         log.debug("end to refresh post index");
     }
 
     public void refreshReaderThread() {
         log.debug("start to refresh thread index");
         try {
             IndexReader reader = this.indexReader_thread.reopen();
             this.indexReader_thread.close();
             this.indexReader_thread = reader;
             // 重建搜索器
             this.indexSearcher_thread.close();
             this.indexSearcher_thread = new IndexSearcher(reader);
             this.indexSearcher_thread.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("reopen indexReader for thread error :" + e);
         }
         log.debug("end to refresh thread index");
     }
 
     public void refreshReaderBlog() {
         log.debug("start to refresh blog index");
         try {
             IndexReader reader = this.indexReader_blog.reopen();
             this.indexReader_blog.close();
             this.indexReader_blog = reader;
             // 重建搜索器
             this.indexSearcher_blog.close();
             this.indexSearcher_blog = new IndexSearcher(reader);
             this.indexSearcher_blog.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("reopen indexReader for blog error :" + e);
         }
         log.debug("end to refresh blog index");
     }
 
     /**
      * 重建所有的索引
      */
     public void rebuildIndexAll() {
         log.debug("start to rebuild all index");
         synchronized (updatelock) {
             try {
                 // 关闭之前的
                 this.indexSearcher.close();
                 this.indexReader.close();
                 log.debug("optimize the all index");
                 this.indexWriter_all.optimize();
                 this.indexWriter_all.commit();
                 this.indexWriter_all.close();
                 // 重新打开
                 Analyzer analyzer = new IKAnalyzer();
                 IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
                 this.indexWriter_all = new IndexWriter(this.directory, indexWriterConfig);
                 // 更新的时候用的
                 IndexReader reader = IndexReader.open(this.indexWriter_all, true);
                 this.indexReader = reader;
                 // 初始化搜索器
                 this.indexSearcher = new IndexSearcher(reader);
                 this.indexSearcher.setSimilarity(new IKSimilarity());
 
             } catch (IOException e) {
                 log.error("rebuild all index error :" + e);
             }
             // rebuild other index
             this.rebuildIndexAsk();
             this.rebuildIndexDl();
             this.rebuildIndexBlog();
             this.rebuildIndexThread();
             this.rebuildIndexPost();
             this.rebuildIndexArticle();
             log.debug("end to rebuild all index");
         }
     }
 
     private void rebuildIndexArticle() {
         log.debug("start to rebuild article index");
         try {
             // 关闭之前的
             this.indexSearcher_article.close();
             this.indexReader_article.close();
             log.debug("optimize the article index");
             this.indexWriter_article.optimize();
             this.indexWriter_article.commit();
             this.indexWriter_article.close();
             // 重新打开
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_article = new IndexWriter(this.directory_article, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_article, true);
             this.indexReader_article = reader;
             // 初始化搜索器
             this.indexSearcher_article = new IndexSearcher(reader);
             this.indexSearcher_article.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("rebuild article index error :" + e);
         }
         log.debug("end to rebuild article index");
     }
 
 
     /**
      * 重建ask索引
      */
     private void rebuildIndexAsk() {
         log.debug("start to rebuild ask index");
         try {
             // 关闭之前的
             this.indexSearcher_ask.close();
             this.indexReader_ask.close();
             log.debug("optimize the ask index");
             this.indexWriter_ask.optimize();
             this.indexWriter_ask.commit();
             this.indexWriter_ask.close();
             // 重新打开
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_ask = new IndexWriter(this.directory_ask, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_ask, true);
             this.indexReader_ask = reader;
             // 初始化搜索器
             this.indexSearcher_ask = new IndexSearcher(reader);
             this.indexSearcher_ask.setSimilarity(new IKSimilarity());
         } catch (IOException e) {
             log.error("rebuild ask index error :" + e);
         }
         log.debug("end to rebuild ask index");
     }
 
     /**
      * 重建dl索引
      */
     private void rebuildIndexDl() {
         log.debug("start to rebuild dl index");
         try {
             // 关闭之前的
             this.indexSearcher_dl.close();
             this.indexReader_dl.close();
             log.debug("optimize the dl index");
             this.indexWriter_dl.optimize();
             this.indexWriter_dl.commit();
             this.indexWriter_dl.close();
             // 重新打开
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_dl = new IndexWriter(this.directory_dl, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_dl, true);
             this.indexReader_dl = reader;
             // 初始化搜索器
             this.indexSearcher_dl = new IndexSearcher(reader);
             this.indexSearcher_dl.setSimilarity(new IKSimilarity());
 
         } catch (IOException e) {
             log.error("rebuild dl index error :" + e);
         }
         log.debug("end to rebuild dl index");
     }
 
     private void rebuildIndexPost() {
         log.debug("start to rebuild post index");
         try {
             // 关闭之前的
             this.indexSearcher_post.close();
             this.indexReader_post.close();
             log.debug("optimize the post index");
             this.indexWriter_post.optimize();
             this.indexWriter_post.commit();
             this.indexWriter_post.close();
             // 重新打开
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_post = new IndexWriter(this.directory_post, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_post, true);
             this.indexReader_post = reader;
             // 初始化搜索器
             this.indexSearcher_post = new IndexSearcher(reader);
             this.indexSearcher_post.setSimilarity(new IKSimilarity());
 
         } catch (IOException e) {
             log.error("rebuild post index error :" + e);
         }
         log.debug("end to rebuild post index");
     }
 
     private void rebuildIndexThread() {
         log.debug("start to rebuild thread index");
         try {
             // 关闭之前的
             this.indexSearcher_thread.close();
             this.indexReader_thread.close();
             log.debug("optimize the thread index");
             this.indexWriter_thread.optimize();
             this.indexWriter_thread.commit();
             this.indexWriter_thread.close();
             // 重新打开
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_thread = new IndexWriter(this.directory_thread, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_thread, true);
             this.indexReader_thread = reader;
             // 初始化搜索器
             this.indexSearcher_thread = new IndexSearcher(reader);
             this.indexSearcher_thread.setSimilarity(new IKSimilarity());
 
         } catch (IOException e) {
             log.error("rebuild thread index error :" + e);
         }
         log.debug("end to rebuild thread index");
     }
 
     private void rebuildIndexBlog() {
         log.debug("start to rebuild blog index");
         try {
             // 关闭之前的
             this.indexSearcher_blog.close();
             this.indexReader_blog.close();
             log.debug("optimize the blog index");
             this.indexWriter_blog.optimize();
             this.indexWriter_blog.commit();
             this.indexWriter_blog.close();
             // 重新打开
             Analyzer analyzer = new IKAnalyzer();
             IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_32, analyzer);
             this.indexWriter_blog = new IndexWriter(this.directory_blog, indexWriterConfig);
             // 更新的时候用的
             IndexReader reader = IndexReader.open(this.indexWriter_blog, true);
             this.indexReader_blog = reader;
             // 初始化搜索器
             this.indexSearcher_blog = new IndexSearcher(reader);
             this.indexSearcher_blog.setSimilarity(new IKSimilarity());
 
         } catch (IOException e) {
             log.error("rebuild blog index error :" + e);
         }
         log.debug("end to rebuild blog index");
     }
 
     /**
      * 清除bbcode
      *
      * @return
      */
     public String clearBBcode(String bb) {
         return textProcessor.process(bb);
     }
 
 
 
     public IndexSearcher getIndexSearcher() {
         return indexSearcher;
     }
 
     public void setIndexSearcher(IndexSearcher indexSearcher) {
         this.indexSearcher = indexSearcher;
     }
 
     public Directory getDirectory() {
         return directory;
     }
 
     public void setDirectory(Directory directory) {
         this.directory = directory;
     }
 
     public GlobalConstant getConstant() {
         return constant;
     }
 
     public void setConstant(GlobalConstant constant) {
         this.constant = constant;
     }
 
     public IndexWriter getIndexWriter_all() {
         return indexWriter_all;
     }
 
     public void setIndexWriter_all(IndexWriter indexWriter_all) {
         this.indexWriter_all = indexWriter_all;
     }
 
     public IndexWriter getIndexWriter_ask() {
         return indexWriter_ask;
     }
 
     public void setIndexWriter_ask(IndexWriter indexWriter_ask) {
         this.indexWriter_ask = indexWriter_ask;
     }
 
     public IndexReader getIndexReader() {
         return indexReader;
     }
 
     public void setIndexReader(IndexReader indexReader) {
         this.indexReader = indexReader;
     }
 
     public MemCachedClientService getMemCachedClientService() {
         return memCachedClientService;
     }
 
     public void setMemCachedClientService(MemCachedClientService memCachedClientService) {
         this.memCachedClientService = memCachedClientService;
     }
 
     public byte[] getUpdatelock() {
         return updatelock;
     }
 
     public void setUpdatelock(byte[] updatelock) {
         this.updatelock = updatelock;
     }
 
     public TextProcessor getTextProcessor() {
         return textProcessor;
     }
 
     public void setTextProcessor(TextProcessor textProcessor) {
         this.textProcessor = textProcessor;
     }
 }
