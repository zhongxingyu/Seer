 package com.heroku.javasamplerabbitmq;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.rabbitmq.client.ConnectionFactory;
 
 public class ConnectionFactoryUtil {
 
    public static ConnectionFactory getConnectionFactory(String url) {
         Pattern rabbitMqPattern = Pattern.compile("amqp://(.*):(.*)@(.*):(.*)/(.*)");
         Matcher matcher = rabbitMqPattern.matcher(url);
         matcher.matches();
 
         ConnectionFactory factory = new ConnectionFactory();
         factory.setUsername(matcher.group(1));
         factory.setPassword(matcher.group(2));
         factory.setHost(matcher.group(3));
         factory.setPort(Integer.parseInt(matcher.group(4)));
        factory.setVirtualHost(matcher.group(5));

        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        System.out.println(matcher.group(3));
        System.out.println(matcher.group(4));
        System.out.println(matcher.group(5));
 
         return factory;
     }
 
 }
