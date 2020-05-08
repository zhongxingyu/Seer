package ut.org.whitesource.bamboo.agent;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
import org.whitesource.bamboo.agent.ResourceUtils;
 import org.whitesource.bamboo.agent.ResourceUtils.PathResolutionException;
 
 /**
  * NOTE: derived from http://stackoverflow.com/a/3054692/45773.
  * 
  */
 public class ResourceUtilsTest
 {
     @Test
     public void testGetRelativePathsUnix()
     {
         assertEquals("stuff/xyz.dat", ResourceUtils.getRelativePath("/var/data/stuff/xyz.dat", "/var/data/", "/"));
         assertEquals("../../b/c", ResourceUtils.getRelativePath("/a/b/c", "/a/x/y/", "/"));
         assertEquals("../../b/c", ResourceUtils.getRelativePath("/m/n/o/a/b/c", "/m/n/o/a/x/y/", "/"));
     }
 
     @Test
     public void testGetRelativePathFileToFile()
     {
         String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
         String base = "C:\\Windows\\Speech\\Common\\sapisvr.exe";
 
         String relPath = ResourceUtils.getRelativePath(target, base, "\\");
         assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
     }
 
     @Test
     public void testGetRelativePathDirectoryToFile()
     {
         String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
         String base = "C:\\Windows\\Speech\\Common\\";
 
         String relPath = ResourceUtils.getRelativePath(target, base, "\\");
         assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
     }
 
     @Test
     public void testGetRelativePathFileToDirectory()
     {
         String target = "C:\\Windows\\Boot\\Fonts";
         String base = "C:\\Windows\\Speech\\Common\\foo.txt";
 
         String relPath = ResourceUtils.getRelativePath(target, base, "\\");
         assertEquals("..\\..\\Boot\\Fonts", relPath);
     }
 
     @Test
     public void testGetRelativePathDirectoryToDirectory()
     {
         String target = "C:\\Windows\\Boot\\";
         String base = "C:\\Windows\\Speech\\Common\\";
         String expected = "..\\..\\Boot";
 
         String relPath = ResourceUtils.getRelativePath(target, base, "\\");
         assertEquals(expected, relPath);
     }
 
     @Test
     public void testGetRelativePathDifferentDriveLetters()
     {
         String target = "D:\\sources\\recovery\\RecEnv.exe";
         String base = "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";
 
         try
         {
             ResourceUtils.getRelativePath(target, base, "\\");
             fail();
 
         }
         catch (PathResolutionException ex)
         {
             // expected exception
         }
     }
 }
