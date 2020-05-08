 package loyalitymonitor;
 
 import junit.framework.Assert;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.data.TupleFactory;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 
 public class TimestampRoundUpTest {
     private static final TupleFactory tupleFactory = TupleFactory.getInstance();
 
     @Test
     public void testDateRoundUpTo10() throws IOException {
        TimestampRoundUp eval = new TimestampRoundUp(10);
 
         Tuple tuple = tupleFactory.newTuple();
         tuple.append("Wed May 22 08:32:41 +0000 2013");
 
         String s = eval.exec(tuple);
         Assert.assertEquals("201305220830", s);
     }
 
     @Test
     public void testDateRoundUpTo15() throws IOException {
        TimestampRoundUp eval = new TimestampRoundUp(15);
 
         Tuple tuple = tupleFactory.newTuple();
         tuple.append("Wed May 22 08:49:41 +0000 2013");
 
         String s = eval.exec(tuple);
         Assert.assertEquals("201305220845", s);
     }
 
     @Test
     public void testDateRoundUpToZero() throws IOException {
        TimestampRoundUp eval = new TimestampRoundUp(15);
 
         Tuple tuple = tupleFactory.newTuple();
         tuple.append("Wed May 22 08:14:15 +0000 2013");
 
         String s = eval.exec(tuple);
         Assert.assertEquals("201305220800", s);
     }
 
 }
