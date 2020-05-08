 package ru.orderbook.v4;
 
 import ru.orderbook.v4.app.XmlAppEnvironment;
 import ru.orderbook.v4.consumer.OrderConsumerImpl;
 import ru.orderbook.v4.iface.AppEnvironment;
 import ru.orderbook.v4.iface.LogLevel;
 
 public class AppRunnerV4 {
 
     public static void main(String[] args) {
 //        AppEnvironment environment = new FakeAppEnvironment(LogLevel.INFO);
//        String filename = "/Users/dima/IdeaProjects/katas/src/main/scala/ru/orderbook/orders1.xml";
        String filename = "/Users/dima/IdeaProjects/katas/src/main/scala/ru/orderbook/orders2.xml";
         AppEnvironment environment = new XmlAppEnvironment(filename, LogLevel.INFO);
         environment.registerHandler(new OrderConsumerImpl());
         environment.run();
     }
 }
