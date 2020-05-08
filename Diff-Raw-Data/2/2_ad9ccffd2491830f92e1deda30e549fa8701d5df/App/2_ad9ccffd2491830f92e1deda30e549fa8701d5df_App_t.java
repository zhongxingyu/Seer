 package com.mycompany.testconsumer;
 
 import com.mysql.jdbc.Statement;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.QueueingConsumer;
 import java.io.ByteArrayInputStream;
 import java.io.ObjectInputStream;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import twitter4j.Status;
 
 public class App {
 
     public static void main(String[] args)
             throws java.io.IOException,
             java.lang.InterruptedException,
             ClassNotFoundException {
 
         Connection conn = null;
         ConnectionFactory factory = new ConnectionFactory();
         factory.setHost("localhost");
         conn = factory.newConnection();
         Channel chan = conn.createChannel();
         chan.queueDeclare("testqueue", false, false, false, null);
         Class.forName("com.mysql.jdbc.Driver");
         String url = "jdbc:mysql://155.246.61.53:3306/newtweets?useUnicode=true&characterEncoding=UTF8";
         java.sql.Connection mysqlCon;
         try {
             mysqlCon = DriverManager.getConnection(url, "lsa", "stigmergy");
         } catch (SQLException ex) {
             ex.printStackTrace();
             return;
         }
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         final String sqlStatement = "INSERT INTO  `newtweets`.`tweet` (`tweet_id` ,`tweet_user` ,`tweet_user_login` ,`tweet_date` ,`tweet_text` ,`tweet_retweet` ,`tweet_latitude` ,`tweet_longitude` ,`tweet_place` ,`tweet_language`)VALUES (?,?,?,?,?,?,?,?,?,?);";
         System.out.println("URL: " + url);
         System.out.println("Connection: " + mysqlCon);
         System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
         QueueingConsumer consumer = new QueueingConsumer(chan);
         chan.basicConsume("testqueue", true, consumer);
        ExecutorService newCachedThreadPool = Executors.newFixedThreadPool(10);
         while (true)
         {
             QueueingConsumer.Delivery delivery = consumer.nextDelivery();
             Worker newWorker = new Worker(delivery, mysqlCon, sqlStatement);
             newCachedThreadPool.execute(newWorker);
         }
     }
 }
