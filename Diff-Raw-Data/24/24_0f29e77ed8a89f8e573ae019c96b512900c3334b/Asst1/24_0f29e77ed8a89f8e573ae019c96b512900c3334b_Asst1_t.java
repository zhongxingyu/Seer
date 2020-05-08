 package cs444.acceptance;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class Asst1 {
 
     @Test
     public void testValidJoosCode() throws IOException, InterruptedException {
         String path = "JoosPrograms/SyntacticallyValidPrograms/";
 
         TestHelper.assertReturnCodeForFiles(path, 0,  true);
     }
 
     @Test
     public void testInvalidJoosCode() throws IOException, InterruptedException {
         String path = "JoosPrograms/SyntacticallyInvalidPrograms/";
 
         TestHelper.assertReturnCodeForFiles(path, 42,  false);
     }
 
     @Test
    public void testMarmosetValidCases() throws IOException, InterruptedException{
         String path = "JoosPrograms/MarmosetPrograms/a1/valid/";
         TestHelper.assertReturnCodeForFiles(path, 0,  true);
    }
 
    @Test
    public void testMarmosetInvalidCases() throws IOException, InterruptedException {
        String path = "JoosPrograms/MarmosetPrograms/a1/invalid/";
         TestHelper.assertReturnCodeForFiles(path, 42, false);
     }
 }
