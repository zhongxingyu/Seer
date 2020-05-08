 package com.taobao.metamorphosis.example;
 
 import static com.taobao.metamorphosis.example.Help.initMetaConfig;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.taobao.metamorphosis.Message;
 import com.taobao.metamorphosis.client.extension.AsyncMessageSessionFactory;
 import com.taobao.metamorphosis.client.extension.AsyncMetaMessageSessionFactory;
 import com.taobao.metamorphosis.client.producer.MessageProducer;
 import com.taobao.metamorphosis.client.producer.SendMessageCallback;
 import com.taobao.metamorphosis.client.producer.SendResult;
 
 
 /**
  * <pre>
  * 첽Ϣ *
  * 
  * 
 * ڴ첽ϢĻỰ.
  * 
 * ʹó:
  *      ڷͿɿҪô,Ҫ߷ЧʺͽͶӦõӰ죬Ӧõȶ.
  *      ,ռ־ûΪϢȳ.
  * ע:
  *      Ϣ󷵻صĽв׼ȷmessageId,partition,offset,Щֵ-1
 * 
  * @author ޻
  * @Date 2012-2-27
  * 
  */
 public class AsyncOnewayProducer {
     public static void main(final String[] args) throws Exception {
         // New session factory,ǿҽʹõ
         final AsyncMessageSessionFactory sessionFactory = new AsyncMetaMessageSessionFactory(initMetaConfig());
         // create producer,ǿҽʹõ
         final MessageProducer producer = sessionFactory.createAsyncProducer();
         // publish topic
        final String topic = "meta-test";
         producer.publish(topic);
 
         final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
         String line = null;
         while ((line = readLine(reader)) != null) {
             // send message
             try {
                 producer.sendMessage(new Message(topic, line.getBytes()), new SendMessageCallback() {
 
                     @Override
                     public void onMessageSent(final SendResult result) {
                         if (result.isSuccess()) {
                             System.out.println("Send message successfully,sent to " + result.getPartition());
                         }
                         else {
                             System.err.println("Send message failed,error message:" + result.getErrorMessage());
                         }
 
                     }
 
 
                     @Override
                     public void onException(final Throwable e) {
                         e.printStackTrace();
 
                     }
                 });
 
             }
             catch (final Exception e) {
                 e.printStackTrace();
             }
             // check result
         }
     }
 
 
     private static String readLine(final BufferedReader reader) throws IOException {
         System.out.println("Type a message to send:");
         return reader.readLine();
     }
 }
