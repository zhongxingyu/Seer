 package org.atlasapi.feeds.radioplayer.upload;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 
 import org.atlasapi.feeds.radioplayer.upload.FTPUploadResult.FTPUploadResultType;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Test;
 
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.health.ProbeResult;
 import com.metabroadcast.common.health.ProbeResult.ProbeResultType;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.mongodb.BasicDBObject;
 
 public class RadioPlayerUploadHealthProbeTest {
 
 //    private static final String DATE_TIME = "dd/MM/yy HH:mm:ss";
     public final DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
    public final RadioPlayerUploadHealthProbe probe = new RadioPlayerUploadHealthProbe(mongo, "radio1", "%1$tY%1$tm%1$td_340_PI.xml").withLookAhead(0).withLookBack(0);
     private FTPUploadResultRecorder recorder = new MongoFTPUploadResultRecorder(mongo);
     
     @After
     public void tearDown() {
         mongo.collection("radioplayer").remove(new BasicDBObject());
     }
     
     @Test
     public void testProbe() {
 
         ProbeResult result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(1)));
         assertThat(Iterables.getOnlyElement(result.entries()).getType(), is(equalTo(ProbeResultType.INFO)));
         assertThat(Iterables.getOnlyElement(result.entries()).getValue(), is(equalTo("No Data.")));
         
         DateTime succssDate = new DateTime(DateTimeZones.UTC);
         recorder.record(new DefaultFTPUploadResult(String.format("%s_340_PI.xml", succssDate.toString("yyyyMMdd")), succssDate, FTPUploadResultType.SUCCESS).withMessage("FAIL"));
         
         result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(1)));
         assertThat(Iterables.getOnlyElement(result.entries()).getType(), is(equalTo(ProbeResultType.SUCCESS)));
 //        assertThat(Iterables.getOnlyElement(result.entries()).getValue(), endsWith(String.format("%s. No failures.", succssDate.toString(DATE_TIME))));
         
         DateTime failureDate = new DateTime(DateTimeZones.UTC);
         recorder.record(new DefaultFTPUploadResult(String.format("%s_340_PI.xml", failureDate.toString("yyyyMMdd")), failureDate, FTPUploadResultType.FAILURE).withMessage("FAIL"));
          
         result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(1)));
         assertThat(Iterables.getOnlyElement(result.entries()).getType(), is(equalTo(ProbeResultType.FAILURE)));
 //        assertThat(Iterables.getOnlyElement(result.entries()).getValue(), endsWith(String.format("Last success %s. Last failure %s. FAIL", succssDate.toString(DATE_TIME), failureDate.toString(DATE_TIME))));
     }
 
     @Test
     public void testFailureFirst() {
         
         DateTime failureDate = new DateTime(DateTimeZones.UTC);
         recorder.record(new DefaultFTPUploadResult(String.format("%s_340_PI.xml", failureDate.toString("yyyyMMdd")), failureDate, FTPUploadResultType.FAILURE).withMessage("FAIL"));
         
         ProbeResult result = probe.probe();
         
         assertThat(Iterables.size(result.entries()), is(equalTo(1)));
         assertThat(Iterables.getOnlyElement(result.entries()).getType(), is(equalTo(ProbeResultType.FAILURE)));
 //        assertThat(Iterables.getOnlyElement(result.entries()).getValue(), endsWith(String.format("No successes. Last failure %s. FAIL", failureDate.toString(DATE_TIME))));
 
     }
                                  
 }
