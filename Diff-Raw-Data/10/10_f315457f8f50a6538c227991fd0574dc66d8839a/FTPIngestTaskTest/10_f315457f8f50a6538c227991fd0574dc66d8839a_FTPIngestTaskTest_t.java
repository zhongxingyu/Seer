 package gov.usgs.cida.ncetl.utils;
 
 import org.joda.time.DateTime;
 import gov.usgs.cida.ncetl.utils.FTPIngestTask.Builder;
 import java.net.MalformedURLException;
 import java.util.Date;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 /**
  *
  * @author Ivan Suftin <isuftin@usgs.gov>
  */
 public class FTPIngestTaskTest {
     
     public FTPIngestTaskTest() {
     }
 
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     @Test
     public void testBuilder() throws MalformedURLException {
         Builder builder = new Builder("testName", "ftp://127.0.0.1/");
         Date now = new Date();
         
         FTPIngestTask test = builder
                 .active(false)
                 .fileRegex("fileRegex")
                 .location("ftp://127.0.0.1/")
                 .name("name")
                 .password("password")
                 .rescanEvery(1)
                 .startDate(new DateTime(now))
                 .username("username")
                 .build();
         
         assertThat(test.isActive(), is(false));
         assertThat(test.getRescanEvery(), is(equalTo(new Long(1))));
         assertThat(test.toJSONString().contains("{\"rescanEvery\":1,\"username\":\"username\",\"filePattern\":\"fileRegex\",\"lastSuccess\""),  is(true));
         assertThat(test.toXMLString(), containsString("<?xml"));
         assertThat(test.getName(), is(equalTo("name")));
         
     }
     
 }
