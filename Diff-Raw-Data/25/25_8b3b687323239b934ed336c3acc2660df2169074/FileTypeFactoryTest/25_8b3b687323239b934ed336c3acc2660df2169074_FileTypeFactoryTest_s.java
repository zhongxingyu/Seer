 package com.example.fsexplorer.api;
 
 import com.example.fsexplorer.fs.FileType;
 import com.example.fsexplorer.fs.FileTypeFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import static junit.framework.Assert.*;
 
 /**
  * @author Andrey Tarashevsky
  *         Date: 05.02.13
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/test-context.xml")
 public class FileTypeFactoryTest {
 
     Path rootTestPath;
 
     @Autowired
     private FileTypeFactory fileTypeFactory;
 
     @Before
     public void setUp() throws Exception {
        rootTestPath = Paths.get("./web-service/src/test/resources/testFileSystem");
     }
 
     @Test
     public void testThatFromPathReturnsFolderType() throws Exception {
 
         Path folder1 = rootTestPath.resolve("folder1");
         assertTrue(Files.isDirectory(folder1, LinkOption.NOFOLLOW_LINKS));
         assertEquals(fileTypeFactory.fromPath(folder1), FileTypeFactory.FOLDER);
 
     }
 
     @Test
     public void testThatFromPathReturnsNullOnWrongPath() throws Exception {
         Path wrongPath = rootTestPath.resolve("somepath/that/not/existst");
         assertNull(fileTypeFactory.fromPath(wrongPath));
     }
 
     @Test
     public void testThatFromPathReturnsProperTypeForUnknownExtension() throws Exception {
         Path unknownExtPath = rootTestPath.resolve("unknownfile.unk");
         assertTrue(Files.exists(unknownExtPath));
         assertEquals(FileTypeFactory.UNKNOWN, fileTypeFactory.fromPath(unknownExtPath));
     }
 
     @Test
     public void testThatFromPathReturnsProperTypeForOneExtension() throws Exception {
 
        Path oneFile = rootTestPath.resolve("file.one");
         assertTrue(Files.exists(oneFile));
         FileType oneType = fileTypeFactory.fromPath(oneFile);
         assertEquals(oneType.getClassName(), "one");
 
     }
 
     @Test
     public void testThatFromPathReturnsSameTypeForDifferentExtensions() throws Exception {
 
        Path twoFile = rootTestPath.resolve("file.two");
        Path threeFile = rootTestPath.resolve("file.three");
 
         assertTrue(Files.exists(twoFile));
         assertTrue(Files.exists(threeFile));
 
         FileType twoType = fileTypeFactory.fromPath(twoFile);
         FileType threeType = fileTypeFactory.fromPath(threeFile);
 
         assertEquals(twoType.getClassName(), "multi");
         assertEquals(twoType, threeType);
     }
 }
