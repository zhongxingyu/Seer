 package com.heroku.javasamplerabbitmq;
 
 import org.springframework.amqp.core.Message;
 import org.springframework.amqp.core.MessageProperties;
 import org.springframework.amqp.rabbit.core.RabbitTemplate;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.io.UnsupportedEncodingException;
 import java.util.regex.Matcher;
 
 public class SpringSender {
 
     public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {
         ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:context.xml");
        Matcher rabbitMqUrlMatcher = ctx.getBean("rabbitMqUrlMatcher", Matcher.class);
         RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);
         while(true){
             String msg = "Spring Sent at:" + System.currentTimeMillis();
             System.out.println(msg);
             byte[] body = msg.getBytes("UTF-8");
             rabbitTemplate.send(new Message(body, new MessageProperties()));
             Thread.sleep(1000);
         }
     }
 }
