 package org.cs27x.dropbox.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.attribute.FileTime;
 
 import org.cs27x.dropbox.DefaultFileManager;
 import org.cs27x.dropbox.DropboxCmd;
 import org.cs27x.dropbox.DropboxCmdProcessor;
 import org.cs27x.filewatcher.FileState;
 import org.cs27x.filewatcher.FileStates;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 
 
 public class DropboxCmdProcessorTest {
 
     private static final DropboxCmd COMMAND_ADD = new DropboxCmd();
     private static final DropboxCmd COMMAND_REMOVE = new DropboxCmd();
    private static final int JUNK_SIZE = 7;
    private static final String JUNK_PATH = "abc";
    private static final byte[] JUNK_DATA = new byte[11];
 
     static {
         COMMAND_ADD.setOpCode(DropboxCmd.OpCode.ADD);
        COMMAND_ADD.setPath(JUNK_PATH);
        COMMAND_ADD.setData(JUNK_DATA);

         COMMAND_REMOVE.setOpCode(DropboxCmd.OpCode.REMOVE);
        COMMAND_REMOVE.setPath(JUNK_PATH);
        COMMAND_REMOVE.setData(JUNK_DATA);
     }
 
     @Mock FileStates mockFileStates;
     @Mock DefaultFileManager mockFileManager;
     @Mock Path mockPath;
 
     FileTime mockFileTime = FileTime.fromMillis(0);
 
     public DropboxCmdProcessorTest() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void testUpdateFileStateAdd() {
         final DropboxCmdProcessor testClass = new DropboxCmdProcessor(mockFileStates, mockFileManager);
         final FileState addedState = new FileState(-1, null);
 
         assertNotNull("DropboxCmdProcessor must be initialized", testClass);
 
         Mockito.when(mockPath.toAbsolutePath()).thenReturn(mockPath);
         // The output of toString is used as the key in the Map, but it can be any value.
         Mockito.when(mockPath.toString()).thenReturn("123");
         Mockito.when(mockFileStates.getOrCreateState(mockPath)).thenReturn(addedState);
 
         testClass.updateFileState(COMMAND_ADD, mockPath);
         Mockito.verify(mockFileStates).getOrCreateState(mockPath);
 
         assertEquals("Size must match", COMMAND_ADD.getData().length, addedState.getSize());
         assertEquals("File timestamps must match", mockFileTime, addedState.getLastModificationDate());
     }
 
     @Test
     public void testUpdateFileStateRemove() {
         final DropboxCmdProcessor testClass = new DropboxCmdProcessor(mockFileStates, mockFileManager);
        final FileState removedState = new FileState(JUNK_SIZE, null);
 
         assertNotNull("DropboxCmdProcessor must be initialized", testClass);
 
         Mockito.when(mockFileStates.getState(mockPath)).thenReturn(removedState);
 
        assertEquals("Size must be 7", JUNK_SIZE, removedState.getSize());
         testClass.updateFileState(COMMAND_REMOVE, mockPath);
         assertEquals("Size must be -1", -1, removedState.getSize());
     }
 
     @Test
     public void testCmdReceived() {
         final DropboxCmdProcessor testClass = new DropboxCmdProcessor(mockFileStates, mockFileManager);
 
         assertNotNull("DropboxCmdProcessor must be initialized", testClass);
 
         Mockito.when(mockFileManager.resolve(Mockito.any(String.class))).thenReturn(mockPath);
         Mockito.when(mockFileStates.getOrCreateState(mockPath)).thenReturn(new FileState(-1, null));
 
         testClass.cmdReceived(COMMAND_ADD);
 
         try {
             Mockito.verify(mockFileManager).write(mockPath, COMMAND_ADD.getData(), false);
         } catch (final IOException e) {
             e.printStackTrace();
             fail("IOException!");
         }

        testClass.cmdReceived(COMMAND_REMOVE);

        try {
            Mockito.verify(mockFileManager).delete(mockPath);
        } catch (final IOException e) {
            e.printStackTrace();
            fail("IOException!");
        }
     }
 }
