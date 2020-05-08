 package net.paguo.trafshow.backend.snmp.summary.model;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Date;
 
 /**
  * User: sreentenko
  * Date: 15.11.2007
  * Time: 23:42:02
  */
 public class TestRouterSummaryTraffic {
     private RouterSummaryTraffic traffic;
     private TrafficRecord oldRecord;
     private static final String ABC = "ABC";
 
     @Before
     public void prepare(){
         traffic = new RouterSummaryTraffic();
         traffic.setDate(new Date());
         traffic.setRouter(ABC);
         traffic.setIface(ABC);
 
         oldRecord = new TrafficRecord();
         oldRecord.setRouter(ABC);
         oldRecord.setUptime(230L);
         oldRecord.setIface(ABC);
         oldRecord.setInput(5L);
         oldRecord.setOutput(5L);
     }
 
     private TrafficRecord createTrafficRecord(long amount, long uptime){
         TrafficRecord record = new TrafficRecord();
         record.setRouter(ABC);
         record.setUptime(uptime);
         record.setIface(ABC);
         record.setInput(amount);
         record.setOutput(amount);
         return record;
     }
 
     @Test
     public void testIntialFill(){
         traffic.processRecord(oldRecord);
        Assert.assertEquals("No traffic yet", Long.valueOf(5L), traffic.getTotalInput());
        Assert.assertEquals("No traffic yet", Long.valueOf(5L), traffic.getTotalOutput());
     }
 
     @Test
     public void testAddRecord(){
        traffic.setLastProcessed(oldRecord);
         TrafficRecord newRecord = createTrafficRecord(10L, 235L);
         traffic.processRecord(newRecord);
         Assert.assertEquals("Normal flow expected", Long.valueOf(5L), traffic.getTotalInput());
     }
 
     @Test
     public void testAddOverflowRecord(){
         traffic.processRecord(oldRecord);
        TrafficRecord newRecord = createTrafficRecord(1L, 235L);
         traffic.processRecord(newRecord);
        Assert.assertEquals("Much traffic expected", Long.valueOf(4294967297L), traffic.getTotalInput());
     }
 
     @Test
     public void testOvertimedRecord(){
         traffic.processRecord(oldRecord);
         TrafficRecord newRecord = createTrafficRecord(1L, 20L);
         traffic.processRecord(newRecord);
        Assert.assertEquals("Counters zeroed", Long.valueOf(6L), traffic.getTotalInput());
     }
 
     @Test
     public void TestZeroRecords(){
         oldRecord.setInput(0L);
         oldRecord.setOutput(0L);
         TrafficRecord newRecord = createTrafficRecord(0L, 235L);
         traffic.processRecord(oldRecord);
         traffic.processRecord(newRecord);
         Assert.assertEquals("No traffic expected", Long.valueOf(0L), traffic.getTotalInput());
     }
 
     @Test
     public void TestEqualRecords(){
         TrafficRecord newRecord = createTrafficRecord(oldRecord.getInput(), 235L);
         traffic.processRecord(oldRecord);
         Long oldValue = traffic.getTotalInput();
         traffic.processRecord(newRecord);
         Assert.assertEquals("No traffic expected", oldValue, traffic.getTotalInput());
     }
 }
