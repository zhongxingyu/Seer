 import junit.framework.Assert;
 import org.joda.time.DateTime;
 import org.joda.time.chrono.ISOChronology;
 import org.junit.Test;
 import tsdaggregator.CounterVariable;
 import tsdaggregator.QueryLogLineData;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 
 public class LineDataTests {
 	@Test
 	public void ConstructTest()	{
 		QueryLogLineData data = new QueryLogLineData();
 		Assert.assertNotNull(data);
 	}
 	
 	@Test
 	public void ParseSingleEntry() {
 		QueryLogLineData data = new QueryLogLineData();
 		data.parseLogLine("[initTimestamp=1300976184.02,set/view=26383.8768005,]");
 		Map<String, CounterVariable> map = data.getVariables();
 		Assert.assertEquals(1, map.size());
                 ArrayList setView = map.get("set/view").getValues();
                 Assert.assertEquals(1, setView.size());
 		Assert.assertEquals(26383.8768005, setView.get(0));
 		DateTime timestamp = data.getTime();
 		Assert.assertEquals(new DateTime((long)(1300976184.02d * 1000d), ISOChronology.getInstanceUTC()), timestamp);
 	}
 	
 	@Test
 	public void ParseMultipleEntries() {
 		QueryLogLineData data = new QueryLogLineData();
 		data.parseLogLine("[initTimestamp=1300976164.85,passport/signin=395539.999008,passport/_signinValidatePassword=390913.009644,passport/signinValid=1,]");
 		Map<String, CounterVariable> map = data.getVariables();
 		Assert.assertEquals(3, map.size());
 		Assert.assertEquals(1d, map.get("passport/signinValid").getValues().get(0));
 		DateTime timestamp = data.getTime();
 		Assert.assertEquals(new DateTime((long)(1300976164.85d * 1000d), ISOChronology.getInstanceUTC()), timestamp);
 	}
         
     @Test
     public void Parse2aVersionEntry() {
         QueryLogLineData data = new QueryLogLineData();
         data.parseLogLine("{\"version\":\"2a\",\"counters\":{\"initTimestamp\":1311574570.3563},\"timers\":{\"qmanager\\/index\":[99496.126174927,106616.37284927]},\"annotations\":[]}");
         Map<String, CounterVariable> map = data.getVariables();
         Assert.assertEquals(1, map.size());
         ArrayList<Double> vals = map.get("qmanager/index").getValues();
         Assert.assertEquals(99496.126174927d, vals.get(0));
         Assert.assertEquals(106616.37284927d, vals.get(1));
         DateTime timestamp = data.getTime();
         Assert.assertEquals(new DateTime((long)(1311574570.3563d * 1000d), ISOChronology.getInstanceUTC()), timestamp);
     }
 
     @Test
     public void Parse2bVersionEntry() {
         QueryLogLineData data = new QueryLogLineData();
         data.parseLogLine("{\"version\":\"2b\",\"counters\":{},\"timers\":{\"/incentive/bestfor\":[2070]},\"annotations\":{\"initTimestamp\":\"1347527687.486\",\"finalTimestamp\":\"1347527687.686\"}}");
         Map<String, CounterVariable> map = data.getVariables();
         Assert.assertEquals(1, map.size());
         ArrayList<Double> vals = map.get("/incentive/bestfor").getValues();
        Assert.assertEquals(2070d, vals.get(0));
         DateTime timestamp = data.getTime();
         Assert.assertEquals(new DateTime((long)(1347527687.686 * 1000d), ISOChronology.getInstanceUTC()), timestamp);
     }
 
 }
