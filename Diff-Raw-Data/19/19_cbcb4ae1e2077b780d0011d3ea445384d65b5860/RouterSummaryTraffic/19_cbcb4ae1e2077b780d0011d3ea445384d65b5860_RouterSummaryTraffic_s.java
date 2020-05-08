 package net.paguo.trafshow.backend.snmp.summary.model;
 
 import java.util.Date;
 
 /**
  * @author Reyentenko
  */
 public class RouterSummaryTraffic {
     private String router;
 
     private String iface;
 
     private Long totalInput = 0l;
 
     private Long totalOutput = 0l;
 
     private Date date;
 
     private TrafficRecord lastProcessed;
 
     public RouterSummaryTraffic() {
 
     }
 
     public void processRecord(TrafficRecord record) {
         if (lastProcessed != null) {
             // Check for router restart
             if (record.getUptime() >= lastProcessed.getUptime()) {
                 totalInput += findDifference(lastProcessed.getInput(), record.getInput());
                 totalOutput += findDifference(lastProcessed.getOutput(), record.getOutput());
             } else {
                System.err.println("Possibly router restart");
                 totalInput += record.getInput();
                 totalOutput += record.getOutput();
             }
         } else {
             totalInput = 0L;
             totalOutput = 0L;
         }
         this.lastProcessed = record;
     }
 
     private Long findDifference(Long input, Long input1) {
         Long result = 0L;
         final long difference = input - input1;
         // On counter overflow this difference should be a reasonable high
         // Other way we get abnormaly high values on ill-behaved routers
         if (difference < 10000L) {
             result = input1 - input;
         } else {
             result = ((long) Math.pow(2, 32) - input) + input1;
         }
         return Math.abs(result);
     }
 
     /**
      * Test-only method.
      *
      * @param lastProcessed last processed record
      */
     void setLastProcessed(TrafficRecord lastProcessed) {
         this.lastProcessed = lastProcessed;
     }
 
     public String getRouter() {
         return router;
     }
 
     public void setRouter(String router) {
         this.router = router;
     }
 
     public String getIface() {
         return iface;
     }
 
     public void setIface(String iface) {
         this.iface = iface;
     }
 
     public Long getTotalInput() {
         return totalInput;
     }
 
     public void setTotalInput(Long totalInput) {
         this.totalInput = totalInput;
     }
 
     public Long getTotalOutput() {
         return totalOutput;
     }
 
     public void setTotalOutput(Long totalOutput) {
         this.totalOutput = totalOutput;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 }
