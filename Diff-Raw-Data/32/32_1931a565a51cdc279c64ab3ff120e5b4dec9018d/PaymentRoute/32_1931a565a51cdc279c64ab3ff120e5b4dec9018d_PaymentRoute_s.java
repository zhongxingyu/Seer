 package eu.nets.javazone.route;
 
 import eu.nets.javazone.service.BalanceValidator;
 import eu.nets.javazone.service.MessageResource;
 import org.apache.camel.Exchange;
 import org.apache.camel.Predicate;
 import org.apache.camel.Processor;
 import org.apache.camel.Route;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.impl.DefaultExchange;
 import org.apache.camel.management.InstrumentationProcessor;
 import org.apache.camel.processor.DefaultChannel;
 import org.apache.camel.processor.UnitOfWorkProcessor;
 import org.apache.camel.processor.aggregate.AggregationStrategy;
 import org.apache.camel.spi.AggregationRepository;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicLong;
 
 public class PaymentRoute extends RouteBuilder {
 
     public static final String RECEIVE = "jms:receive";
     public static final String RECEIPT = "jms:receipt";
     public static final String BALANCE_SPLITTER = "jms:balance_splitter?transacted=true";
     public static final String BALANCE = "jms:balance?concurrentConsumers=100&maxConcurrentConsumers=100&transacted=true";
     public static final String CLEARING_AGGREGATOR = "jms:clearing_aggregator?concurrentConsumers=100&maxConcurrentConsumers=100&transacted=true";
     public static final String CLEARING = "direct:clearing";
 
     private final AtomicLong startTime = new AtomicLong(0);
 
     @Override
     public void configure() throws Exception {
 
         from(RECEIVE)
                 .routeId("receive")
                 .process(new StartTimingProcessor())
                 .inOnly(RECEIPT)
                 .inOnly(BALANCE_SPLITTER);
 
         from(BALANCE_SPLITTER)
                 .transacted()
                 .setHeader("MyCorrelationId", simple("${exchangeId}"))
                 .split(body(String.class).tokenize("\n"))
                 .to(BALANCE);
 
         from(BALANCE)
                 .routeId("balance")
                 .transacted()
                 .validate(bean(BalanceValidator.class))
                 .beanRef("balanceService", "checkBalanceAndReserveAmount")
                 .inOnly(CLEARING_AGGREGATOR);
 
         from(RECEIPT)
                 .routeId("receipt")
                 .transform(body().prepend("Received OK\n"))
                 .to("file:data/receipts/");
 
         from(CLEARING_AGGREGATOR)
                 .transacted()
                .filter(header("BALANCE_CHECK").isEqualTo("OK"))
                 .aggregate(header("MyCorrelationId"), groupExchanges())
                    .completionTimeout(20000)
                     .completionSize(1000)
                     .aggregationRepositoryRef("aggregatorRepository")
                    //.discardOnCompletionTimeout()
                 .onCompletion()
                     .choice()
                         .when(timeout())
                             .beanRef("balanceService", "rollbackReservations")
                         .otherwise()
                             .beanRef("balanceService", "commitReservations")
                             .to(CLEARING)
                     .end()
                 .end();
 
         from(CLEARING)
                 .routeId("clearing")
                 .transacted()
                 .beanRef("csminsert")
                 .process(new StopTimingProcessor());
 
        /*from("timer://rollback?fixedRate=true&period=1000")
                .transacted()
                .beanRef("balanceService", "rollbackReservations");*/
 
    }
 
     private Predicate timeout() {
         return header("CamelAggregatedCompletedBy").isEqualTo("timeout");
     }
 
     public static AggregationStrategy groupExchanges() {
         return new AggregationStrategy() {
 
             @Override
             public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
 
                 List list;
                 Exchange answer = oldExchange;
 
                 if (oldExchange == null) {
                     answer = new DefaultExchange(newExchange);
                     list = new ArrayList<Exchange>();
                     answer.getIn().setBody(list);
                 } else {
                     list = oldExchange.getIn().getBody(List.class);
                 }
 
                 if (newExchange != null) {
                     list.add(newExchange.getIn().getBody());
                 }
                 return answer;
 
             }
         };
     }
 
     class StartTimingProcessor implements Processor {
         @Override
         public void process(Exchange exchange) throws Exception {
             startTime.set(System.currentTimeMillis());
         }
     }
 
     class StopTimingProcessor implements Processor {
         @Override
         public void process(Exchange exchange) throws Exception {
             MessageResource.message = "Last shipment cleared in " + (System.currentTimeMillis() - startTime.get()) / 1000 + " secs in total.";
             System.err.println("Last shipment cleared in " + (System.currentTimeMillis() - startTime.get()) / 1000 + " secs in total.");
         }
     }
 }
