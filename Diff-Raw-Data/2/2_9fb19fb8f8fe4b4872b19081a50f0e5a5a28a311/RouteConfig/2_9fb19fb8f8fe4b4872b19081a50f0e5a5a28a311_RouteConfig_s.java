 package ${groupId}.integration;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.spring.SpringRouteBuilder;
 import org.apache.camel.spring.spi.SpringTransactionPolicy;
 
 /**
  */
 public class RouteConfig extends SpringRouteBuilder {
 
     @Override
     public void configure() throws Exception {
         SpringTransactionPolicy requirenew = lookup("PROPAGATION_REQUIRES_NEW", SpringTransactionPolicy.class);
 
        from("file:target/data/in?delay=5000&preMove=inprogress&move=done/${date:now:yyyyMMdd}/${file:name}&moveFailed=/error/${date:now:yyyyMMdd}/${file:name}")
                 .convertBodyTo(String.class)     // use .streaming() instead
                 .policy(requirenew)
                 .to("dbfile:resources")          // stream to blob
                 .choice()
                 .when(header("CamelFileName").isEqualTo("pom.xml")).to("dbwork:processPomFiles")
                 .otherwise().to("dbwork:processOtherFiles");
 
         from("dbwork:processPomFiles?delay=5000")
                 .policy(requirenew)
                 .process(new Processor() {
                     public void process(Exchange exchange) throws Exception {
                         System.out.println(exchange.getIn().getBody());
                     }
                 });
 
         from("dbwork:processOtherFiles?delay=5000").policy(requirenew).process(new Processor() {
             public void process(Exchange exchange) throws Exception {
                 System.out.println("Got unknown file: " + exchange.getExchangeId());
             }
         });
     }
 
 }
