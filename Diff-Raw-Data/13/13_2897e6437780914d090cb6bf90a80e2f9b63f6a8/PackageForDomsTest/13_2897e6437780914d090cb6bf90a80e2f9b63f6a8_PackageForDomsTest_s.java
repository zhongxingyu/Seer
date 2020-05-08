 package dk.statsbiblioteket.doms.radiotv;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 10/1/12
  * Time: 2:50 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PackageForDomsTest {
     @org.junit.Test
     public void testMain() throws Exception {
 
         String[] args = {
                 "-channelID", "dr1",
                 "-format", "mpegts",
                "-startTime", "20120915110000",
                "-endTime", "20120915120000",
                 "-recorder", "yousee",
                 "-filename", "dr1_yousee.2217592800-2040-04-09-16.00.00_2217596400-2040-04-09-17.00.00_ftp.ts",
                 "-muxChannelNR", "3",
                 "-checksum", "ccccc9fbf8f5b122205275779baccbdb"
         };
         PackageForDoms.main(args);
 
     }
 }
