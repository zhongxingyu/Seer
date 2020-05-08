 package com.melexis;
 
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.spring.Main;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 
 
 public class Route extends RouteBuilder {
 
     @Override
     public void configure() throws Exception {
         final TransactionId transactionId = new TransactionId();
 
         from("timer:oracle_transactions?fixedRate=true&period=5000")
                 .beanRef("transactionId", "getMaxTransactionId")
                 .setBody(simple("select * from mtl_material_transactions where transaction_id > ${in.body} order by transaction_id"))
                .to("jdbc:viiper")
                 .split()
                     .method("transactionId", "split")
                     .streaming()
                         .wireTap("seda:update_transactionid")
                         .to("stream:out");
 
         from("seda:update_transactionid")
                 .beanRef("transactionId", "updateMaxTransactionId")
                 .log("ticking");
     }
 
     public static void main(String[] args) throws Exception {
         new Main().run(args);
     }
 
     public static class TransactionId {
 
         private BigDecimal maxTransactionId = new BigDecimal(48288312);
 
         public TransactionId() {}
 
         public List<Map<String, Object>> split(final List<Map<String, Object>> results) {
             return results;
         }
 
         public void updateMaxTransactionId(final Map<String, Object> result) {
             this.maxTransactionId = this.maxTransactionId.max((BigDecimal) result.get("TRANSACTION_ID"));
         }
 
         public BigDecimal getMaxTransactionId() {
             return maxTransactionId;
         }
     }
 
 
 }
