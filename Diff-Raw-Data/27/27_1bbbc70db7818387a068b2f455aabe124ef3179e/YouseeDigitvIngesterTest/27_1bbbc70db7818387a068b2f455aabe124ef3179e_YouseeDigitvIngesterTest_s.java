 package dk.statsbiblioteket.digitv.youseeingester;
 

 public class YouseeDigitvIngesterTest {
     @org.junit.Test
     public void testMain() throws Exception {
         YouseeDigitvIngester.main(new String[]{
                 "-filename","testfile.mux",
                 "-starttime","10",
                 "-stoptime","20",
                 "-channelid","dr1",
         });
     }
}
