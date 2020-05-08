 package ru.ttk.camel;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.ejb.EjbComponent;
 import org.apache.camel.impl.DefaultCamelContext;
 
 /**
  *
  */
 public class CamelSQL {
 
     public static void main(String... argv) throws Exception {
         System.out.println("Test - TTK Camel SQL");
 
         // create CamelContext
         CamelContext camelContext = createCamelContext();
         System.out.println("Camel context:" + camelContext);
 
         camelContext.addRoutes(new RouteBuilder() {
             public void configure() {
 
                 from("stream:in?promptMessage=Press ENTER to start")
                         .bean(new SuperTime(),"time")
                         .to("stream:out")
 
                         .loop(5)
                             .transform(body().append("B"))
                             .to("stream:out")
                         .end()
 
                         .bean(new SuperTime(), "time")
                         .to("stream:out")
                         .end();
             }
         });
 
         // Run execution
         camelContext.start();
 
         // Just wait for 10 seconds
         Thread.sleep(5000);
         camelContext.stop();
 
     }
 
     public static CamelContext createCamelContext() throws Exception {
         CamelContext _context = new DefaultCamelContext();
         // do something EXTRA
         return _context;
     }
 }
