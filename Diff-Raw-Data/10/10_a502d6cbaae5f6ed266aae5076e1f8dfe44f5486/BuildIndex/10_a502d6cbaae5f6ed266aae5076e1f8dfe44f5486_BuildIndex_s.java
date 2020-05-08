 package com.eli.index.offline;
 
 import com.eli.index.DocumentSupport;
 import com.eli.index.controller.DiscussionController;
 import com.eli.index.controller.MemberController;
 import com.eli.index.controller.TopicController;
 import com.eli.index.document.DiscussionDoc;
 import com.eli.index.document.MemberDoc;
 import com.eli.index.document.TopicDoc;
 import com.eli.index.manager.ZhihuIndexManager;
 import com.eli.util.Config;
 import org.apache.log4j.Logger;
 import org.apache.lucene.index.CorruptIndexException;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: shenchen
  * Date: 8/14/12
  * Time: 4:45 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class BuildIndex {
     private static final Logger logger = Logger.getLogger(BuildIndex.class);
 
     public static boolean start() {
         HashSet<Integer> question_ids_without_answer = new HashSet<Integer>();
         Date start = new Date();
         try {
             logger.info("Indexing to directory '" + Config.INDEX_DIR
                     + "'...");
 
 
             indexDiscussion();
             indexTopic();
             indexMember();
 
             Date end = new Date();
             logger.info(end.getTime() - start.getTime()
                     + " total milliseconds");
 
         } catch (IOException e) {
             logger.info(" caught a " + e.getClass()
                     + "\n with message: " + e.getMessage());
         }
 
         return true;
     }
 
     public static void indexDiscussion()  throws CorruptIndexException, IOException {
         DiscussionController controller = new DiscussionController();
         int start_idx = 0;
         int count = 0;
         Set<Integer> topicIds = controller.getTopicIds();
         for (int topicId : topicIds) {
             List<DiscussionDoc>  list=  controller.getDiscussionDocs(topicId);
             if (count ++ % 100 == 0)
                 logger.info(count + " topics indexed");
             for (DiscussionDoc doc :list) {
                 ZhihuIndexManager.INSTANCE.delDoc(doc.toDeleteQuery());
                 ZhihuIndexManager.INSTANCE.addDoc(doc);
             }
 
         }
 
         logger.info("indexed discussion:" + start_idx);
 
     }
 
     public static void indexTopic()  throws CorruptIndexException, IOException {
         TopicController controller = new TopicController();
         int start_idx = 0;
         int count = 0;
         List<TopicDoc> lists = controller.getTopics();
         for (TopicDoc doc : lists)  {
             if (count ++ % 100 == 0)
                 logger.info(count + " topics indexed");
                 ZhihuIndexManager.INSTANCE.delDoc(doc.toDeleteQuery());
                 ZhihuIndexManager.INSTANCE.addDoc(doc);
         }
 
         logger.info("indexed discussion:" + start_idx);
 
     }
 
     public static void indexMember()  throws CorruptIndexException, IOException {
         MemberController controller = new MemberController();
         int start_idx = 0;
         int count = 0;
         List<MemberDoc> lists = controller.getMembers();
         for (MemberDoc doc : lists)  {
             if (count ++ % 100 == 0)
                 logger.info(count + " topics indexed");
             ZhihuIndexManager.INSTANCE.delDoc(doc.toDeleteQuery());
             ZhihuIndexManager.INSTANCE.addDoc(doc);
         }
 
         logger.info("indexed discussion:" + start_idx);
 
     }
 
     public static void main(String args[]) {
         start();
     }
 }
