 package net.sapium.livestreamprocessor;
 
 import java.io.File;
 
 import net.sapium.livestreamprocessor.utils.Timelapser;
 import net.sapium.livestreamprocessor.utils.VideoData;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class TimelapserTest extends TestCase {
     public TimelapserTest(String testName){
         super(testName);
     }
     
     public static Test suite() {
         return new TestSuite(TimelapserTest.class);
     }
     
     public void testTimelapse(){
         File timelapseTestVideo = new File("timelapse.flv");
         timelapseTestVideo.delete();
         File timelapseOutVideo = new File("timelapse_out.flv");
         timelapseOutVideo.delete();
         double speedupFactor = 3.14;
         
         timelapseTestVideo.delete();
         TestcaseGenerator.generateVideoWithoutSound("timelapse.flv");
         
         Timelapser timelapser = new Timelapser(null, timelapseTestVideo, timelapseOutVideo, speedupFactor);
         System.out.println(timelapser.getOutFile().getAbsolutePath());
         timelapser.setCurrentTask(Timelapser.TASK_TIMELAPSE);
         
         timelapser.run(); //Not in a thread because the method will end and the test won't get to run
         
         VideoData testData = new VideoData(timelapseTestVideo);
         VideoData testData2 = new VideoData(timelapseOutVideo);
         
         double ratio = testData.getDuration()/(double)testData2.getDuration();
         
         testData.getReader().close();
         testData2.getReader().close();
         
         if(Math.abs(ratio - speedupFactor) < 0.1){
           // timelapseTestVideo.delete();
            //timelapseOutVideo.delete();
             assertTrue(true);
         }else{
             assertEquals(speedupFactor, ratio);
         }
     }
 }
