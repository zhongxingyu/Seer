 package dk.statsbiblioteket.broadcasttranscoder.fetcher;
 
 import dk.statsbiblioteket.broadcasttranscoder.fetcher.cli.FetcherContext;
 import dk.statsbiblioteket.broadcasttranscoder.util.CentralWebserviceFactory;
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.central.RecordDescription;
 import junit.framework.TestCase;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 11/21/12
  * Time: 5:07 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DomsTranscodingStructureFetcherTest extends TestCase {
     public void testProcessThis() throws Exception {
         FetcherContext context = new FetcherContext();
         context.setBatchSize(100);
         context.setCollection("doms:RadioTV_Collection");
         context.setState("Published");
         context.setViewAngle("GUI");
         context.setDomsPassword("fedoraAdminPass");
         context.setDomsUsername("fedoraAdmin");
        context.setDomsEndpoint("http://alhena:7480/centralWebservice-service/central/");
        RecordDescription record;
        record = new RecordDescription();
        record.setPid("uuid:d11c2f49-4e6f-47bd-b04f-7ee6293520ea");
        System.out.println(record.getPid());
         DomsTranscodingStructureFetcher thing = new DomsTranscodingStructureFetcher();
         String result = thing.processThis(context, record);
         System.out.println(result);
 
     }
 }
