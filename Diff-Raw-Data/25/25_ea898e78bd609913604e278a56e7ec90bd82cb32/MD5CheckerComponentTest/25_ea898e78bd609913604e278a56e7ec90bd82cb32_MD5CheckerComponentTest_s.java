 package dk.statsbiblioteket.newspaper.md5checker;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 
 /**
  * Test MD5 checker
  */
 public class MD5CheckerComponentTest {
     @Test
     public void testDoWorkOnBatch() throws Exception {
         MD5CheckerComponent md5CheckerComponent = new MockupIteratorSuper(System.getProperties());
 
         ResultCollector result =
                 new ResultCollector(md5CheckerComponent.getComponentName(), md5CheckerComponent.getComponentVersion());
         Batch batch = new Batch("400022028241");
         batch.setRoundTripNumber(1);
         md5CheckerComponent.doWorkOnBatch(batch, result);
        Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");

 
     }
 }
