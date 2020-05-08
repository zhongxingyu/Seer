 package com.taobao.meta.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.Iterator;
 
 import org.junit.Test;
 
 import com.taobao.metamorphosis.Message;
 import com.taobao.metamorphosis.client.TopicBrowser;
 
 
 public class TopicBrowserTest extends BaseMetaTest {
 
    private final String topic = "meta-test";
 
 
     @Test
     public void sendConsume() throws Exception {
         this.createProducer();
         this.producer.publish(this.topic);
         // ָ߱
         this.createConsumer("group1");
 
         try {
             // Ϣ
             final int count = 100;
             this.sendMessage(count, "hello", this.topic);
 
             // ĽϢ֤ȷ
             this.subscribe(this.topic, 1024 * 1024, count);
         }
         finally {
             this.producer.shutdown();
             this.consumer.shutdown();
         }
 
         TopicBrowser topicBrowser = this.sessionFactory.createTopicBrowser(this.topic);
         try {
             Iterator<Message> it = topicBrowser.iterator();
             int n = 0;
             while (it.hasNext()) {
                 assertNotNull(it.next());
                 n++;
             }
             assertEquals(100, n);
         }
         finally {
             topicBrowser.shutdown();
         }
     }
 }
