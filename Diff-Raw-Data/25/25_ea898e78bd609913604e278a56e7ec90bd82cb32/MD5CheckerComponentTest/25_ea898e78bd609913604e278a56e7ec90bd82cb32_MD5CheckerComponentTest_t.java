 package dk.statsbiblioteket.newspaper.md5checker;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 
 /**
  * Test MD5 checker
  */
 public class MD5CheckerComponentTest {

    private static final String EXPECTED_REPORT = "<result tool=\"MD5_checker_component\" version=\"0.1\" xmlns=\"http://schemas.statsbiblioteket.dk/result/\"><outcome>Failure</outcome><date>[date]</date><failures><failure><filereference>B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006-brik.jp2/contents</filereference><type>checksum</type><component>MD5_checker_component-0.1</component><description>Expected checksum d41d8cd98f00b204e9800998ecf8427f, but was d41d8cd98f00b204e9800998ecf8427e</description></failure></failures></result>";

     @Test
     public void testDoWorkOnBatch() throws Exception {
         MD5CheckerComponent md5CheckerComponent = new MockupIteratorSuper(System.getProperties());
 
         ResultCollector result =
                 new ResultCollector(md5CheckerComponent.getComponentName(), md5CheckerComponent.getComponentVersion());
         Batch batch = new Batch("400022028241");
         batch.setRoundTripNumber(1);
         md5CheckerComponent.doWorkOnBatch(batch, result);
        Assert.assertFalse(result.isSuccess(), result.toReport() + "\n");
        Assert.assertEquals(result.toReport().replaceAll("<date>[^<]*</date>", "<date>[date]</date>"), EXPECTED_REPORT);
 
        result = new ResultCollector(md5CheckerComponent.getComponentName(), md5CheckerComponent.getComponentVersion());
        batch = new Batch("400022028241");
        batch.setRoundTripNumber(2);
        md5CheckerComponent.doWorkOnBatch(batch, result);
        Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");
     }
 }
