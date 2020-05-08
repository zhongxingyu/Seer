 package cs444.acceptance;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class Asst2 {
 
 //	@Test
 //    public void testTypeCorrectJoosCode() throws IOException, InterruptedException {
 //        TestHelper.assertReturnCodeForFiles("JoosPrograms/TypeCorrectPrograms/", 0, false,  true);
 //    }
 //
 //    @Test
 //    public void testNonTypeCorrectJoosCode() throws IOException, InterruptedException {
 //        TestHelper.assertReturnCodeForFiles("JoosPrograms/NonTypeCorrectPrograms/", 42, false, false);
 //    }
 
     @Test
     public void testMarmosetTestCases() throws IOException, InterruptedException {
         TestHelper.assertReturnCodeForFiles("JoosPrograms/MarmosetPrograms/a2/valid/", 0, false, false);
        //TestHelper.assertReturnCodeForFiles("JoosPrograms/MarmosetPrograms/a2/invalid/", 42, false,  false);
     }
 }
