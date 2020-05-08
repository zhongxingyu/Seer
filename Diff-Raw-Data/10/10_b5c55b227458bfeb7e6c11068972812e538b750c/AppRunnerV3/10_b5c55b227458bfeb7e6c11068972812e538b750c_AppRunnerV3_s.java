 package ru.orderbook.v3;
 
 import ru.orderbook.v3.app.XmlAppEnvironment;
 import ru.orderbook.v3.consumer.OrderConsumerImpl;
 import ru.orderbook.v3.iface.AppEnvironment;
 import ru.orderbook.v3.iface.LogLevel;
 
 public class AppRunnerV3 {
 
     public static void main(String[] args) {
 //        AppEnvironment environment = new FakeAppEnvironment(LogLevel.INFO);
        String filename = "/Users/dima/IdeaProjects/katas/src/main/scala/ru/orderbook/orders1.xml";
         AppEnvironment environment = new XmlAppEnvironment(filename, LogLevel.INFO);
         environment.registerHandler(new OrderConsumerImpl());
         environment.run();
     }
 }
