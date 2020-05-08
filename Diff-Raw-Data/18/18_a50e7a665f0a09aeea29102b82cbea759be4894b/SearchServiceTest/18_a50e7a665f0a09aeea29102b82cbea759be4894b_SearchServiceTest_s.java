 package com.xiaoma.sousuo.service;
 
 import com.xiaoma.sousuo.po.SearchResult;
 import com.xiaoma.sousuo.searcher.SearcherUtil;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: wangchao
  * Date: 2011-06-13 15:31
  *
  * @author <a href="mailto:ohergal@gmail.com">ohergal</a>
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/spring-config.xml")
 public class SearchServiceTest {
     private static final Log log = LogFactory.getLog(SearchServiceTest.class);
 
     @Autowired
     private SearcherSevice searcherSevice;
 
     @Before
     public void setUpTest() {
         //
     }
 
     @After
     public void tearDownTest() {
         // execute "tear down" logic within the transaction
     }
 
     @Test
     public void testSearchKeywordsForAsk() {
         // delete id 37
         //searcherSevice.deleteAsk(37);
         //searcherSevice.flushIndexWriterForAll();
     }
 
     @Test
     public void testdeleteIndexForAsk() {
         //测试查询问答频道的
         //SearchResult resluts = searcherSevice.search("测试");
         //assertNotNull(resluts);
     }
 
     @Test
     public void testPretreatmentKeywords(){
         String s1 = "测试异常解决";
         String result = SearcherUtil.pretreatmentKeywords(s1);
         String s2 = "托福SAT雅思gre,SSAT,ACT,AP";
         String result2 = SearcherUtil.pretreatmentKeywords(s2);
         log.debug(result);
         log.debug(result2);
         assertEquals(" 测试 异常 解决", result);
         assertEquals(" 托福 sat 雅思 gre ssat act ap", result2);
     }
 
     @Test
     public void testClearBBCODE(){
         String s1 = searcherSevice.clearBBcode("[size=6][color=red]20号的机经预测，急求啊，小马老师[/color][/size]");
         String s2 = "20号的机经预测，急求啊，小马老师";
         assertEquals(s1, s2);
     }
 
     public SearcherSevice getSearcherSevice() {
         return searcherSevice;
     }
 
     public void setSearcherSevice(SearcherSevice searcherSevice) {
         this.searcherSevice = searcherSevice;
     }
 }
