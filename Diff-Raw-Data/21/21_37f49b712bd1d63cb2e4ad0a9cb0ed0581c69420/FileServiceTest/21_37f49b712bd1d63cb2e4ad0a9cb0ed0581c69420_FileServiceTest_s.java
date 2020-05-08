 package core.service;
 
 import core.service.exceptions.CreateFileException;
 import core.service.exceptions.WriteFileException;
 import org.apache.commons.io.FileUtils;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 import static org.powermock.api.mockito.PowerMockito.mockStatic;
 import static org.powermock.api.mockito.PowerMockito.verifyStatic;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(FileUtils.class)
 public class FileServiceTest {
 
   private FileService service;
 
   @Before
   public void setup() {
     service = new FileService();
   }
 
   @Test
   public void testCreateFileIfNotExist() throws CreateFileException, IOException {
     File created = service.createFileIfNotExist("temp.fit");
     assertTrue(created.exists());
     created.delete();
 
     File beforeCreatedFile = new File("temp.fit");
     beforeCreatedFile.createNewFile();
     created = service.createFileIfNotExist(beforeCreatedFile);
     assertTrue(created.exists());
     beforeCreatedFile.delete();
     created.delete();
 
     created = service.createFileIfNotExist(new File("temp.fit"));
     assertTrue(created.exists());
     created.delete();
   }
 
   @Test(expected = CreateFileException.class)
   public void testCreateFileIfNotExistCreateException() throws Exception {
     File inputMock = mock(File.class);
     when(inputMock.exists()).thenReturn(false);
     when(inputMock.createNewFile()).thenThrow(IOException.class);
     service.createFileIfNotExist(inputMock);
   }
 
   @Test
   public void testCreateFileIfNotExistWithMocks() throws Exception {
     File inputMock = mock(File.class);
     when(inputMock.exists()).thenReturn(true);
     when(inputMock.createNewFile()).thenThrow(IOException.class);
     File fileIfNotExist = service.createFileIfNotExist(inputMock);
     assertSame(inputMock, fileIfNotExist);
   }
 
   @Test(expected = CreateFileException.class)
   public void testCreateFileIfNotExistFileNotCreated() throws Exception {
     File inputMock = mock(File.class);
     when(inputMock.exists()).thenReturn(false);
     when(inputMock.createNewFile()).thenReturn(false);
     service.createFileIfNotExist(inputMock);
   }
 
   @Test
   public void testWriteFile() throws Exception {
     File inputMock = mock(File.class);
     String content = "anyContent";
     mockStatic(FileUtils.class);
 
     service.writeToFile(inputMock, content);
     verifyStatic(times(1));
     FileUtils.writeStringToFile(inputMock, content);
   }
 
   @Test (expected = WriteFileException.class)
   public void testWriteFileWithException() throws Exception {
     File inputMock = mock(File.class);
     String content = "anyContent";
     mockStatic(FileUtils.class);
     PowerMockito.doThrow(new IOException()).when(FileUtils.class);
     FileUtils.writeStringToFile(inputMock, content);
 
      service.writeToFile(inputMock, content);
     verifyStatic(times(1));
     FileUtils.writeStringToFile(inputMock, content);
   }
 
   @Test
   public void testWriteToFileWithPath() throws WriteFileException {
     FileService spy = spy(service);
     doNothing().when(spy).writeToFile(any(File.class), eq("anyContent"));
     spy.writeToFile("/anyPath", "anyContent");
     verify(spy).writeToFile(argThat(new FileMatcher("/anyPath")), eq("anyContent"));
   }
 
   @Test (expected = WriteFileException.class)
   public void testWriteToFileWithPathException() throws WriteFileException {
     FileService spy = spy(service);
     doThrow(WriteFileException.class).when(spy).writeToFile(any(File.class), eq("anyContent"));
     spy.writeToFile("/anyPath", "anyContent");
     verify(spy).writeToFile(argThat(new FileMatcher("/anyPath")), eq("anyContent"));
   }
 
   @Test
   public void testWriteToFileCreateIfNotExist() throws Exception {
     FileService spy = spy(service);
    doNothing().when(spy).createFileIfNotExist("/anyPath");
    doNothing().when(spy).writeToFile("/anyPath", "anyContent");
     spy.writeToFileCreateIfNotExist("/anyPath", "anyContent");
     verify(spy, times(1)).createFileIfNotExist("/anyPath");
    verify(spy, times(1)).writeToFile("/anyPath", "anyContent");
   }
 
   private class FileMatcher extends BaseMatcher<File> {
     private String absolutePath;
 
     public FileMatcher(String absolutePath) {
 
       this.absolutePath = absolutePath;
     }
 
     @Override
     public boolean matches(Object o) {
       if(o instanceof File) {
         File file = (File) o;
         if(absolutePath.equals(file.getAbsolutePath())) {
           return true;
         }
       }
       return false;
     }
 
     @Override
     public void describeTo(Description description) {
       description.appendText("Absolute Path does not match").appendValue(absolutePath);
     }
   }
 }
