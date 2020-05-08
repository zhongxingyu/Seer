 package Tests;
 
 import java.io.IOException;
 import org.junit.*;
 import org.mockito.Mockito;
 import static org.mockito.Mockito.*;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 
 import fitnesseTfs.*;
 
 public class SourceControlPluginTests {
 
 	private static final String EXPECTED_FILE = "./FitnesseRoot/MyProject/content.txt";
 	private static final String EXPECTED_FILE_RELATIVE = "../FitnesseRoot/MyProject/content.txt";
 	private static final String EXPECTED_ROOT = "c:\\users\\me\\documents\\fitnesse\\";
 	private static final String EXPECTED_OUTPATH = "c:\\users\\me\\documents\\fitnesse\\FitnesseRoot\\MyProject\\content.txt";
     private static final String TFS_PATH = "C:\\Program Files\\Microsoft Visual Studio 9.0\\Common7\\IDE\\";
 	private static final String PAYLOAD = "fitnesseTfs.SourceControlPlugin " + EXPECTED_ROOT + " \"" + TFS_PATH + "\"";
 	private static final String PAYLOAD_NOOUTPUT = PAYLOAD + " nooutput";
 	
     TfRunner fakeTfRunner = Mockito.mock(TfRunner.class);
     FileSystem fakeFileSystem = Mockito.mock(FileSystem.class);
     ConsoleOutputter fakeOutputter = Mockito.mock(ConsoleOutputter.class);
 	
 	@Before
 	public void setup()
 	{
         SourceControlPlugin.setTfRunner(fakeTfRunner);
         SourceControlPlugin.setFileSystem(fakeFileSystem);
         SourceControlPlugin.setOutputter(fakeOutputter);
         SourceControlPlugin.setLastEditedFile("");
 	}
 
 	@Test
 	public void cmEditCallsSetPathOnRunner() throws IOException	{
         SourceControlPlugin.cmEdit(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner).setPathToTfCommand(TFS_PATH);
 	}
 
     @Test
     public void cmEditCallsSetOutputOnRunner() {
         SourceControlPlugin.cmEdit(EXPECTED_FILE, PAYLOAD_NOOUTPUT);
         verify(fakeTfRunner).setOutput(false);
     }
 
     @Test
     public void cmEditCallsSetOutputOnFileSystem() {
         SourceControlPlugin.cmEdit(EXPECTED_FILE, PAYLOAD_NOOUTPUT);
         verify(fakeFileSystem).setOutput(false);
     }
 
     @Test
     public void cmEditBuildsPathAndStripsRelativePath() {
         SourceControlPlugin.cmEdit(EXPECTED_FILE_RELATIVE, PAYLOAD);
         verify(fakeFileSystem).fileExists(EXPECTED_OUTPATH);
     }
 
 	@Test
 	public void cmEditDoesNothingWhenFileDoesNotExist() throws IOException {
         setFileExists(false);
         SourceControlPlugin.cmEdit(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner, never()).execute(anyString(), anyString());
     }
 
     @Test
 	public void cmEditDoesNothingWhenFileExistsAndIsWritable() throws IOException {
         setFileExists(true);
         setFileWritable(true);
         SourceControlPlugin.cmEdit(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner, never()).execute(anyString(), anyString());
     }
 
     @Test
     public void cmEditCallsCheckoutWhenFileExistsAndIsNotWritable() throws IOException {
         setFileExists(true);
         setFileWritable(false);
         SourceControlPlugin.cmEdit(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner).execute("checkout", EXPECTED_OUTPATH);
     }
 
     @Test
     public void cmUpdateCallsAddWhenFileWasNotJustEdited() throws IOException {
         // When Fitnesse creates a new file, it does not call Edit at all.
         // It creates the file first, then calls Update.
         SourceControlPlugin.cmUpdate(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner).execute("add", EXPECTED_OUTPATH);
     }
 	
     @Test
 	public void cmUpdateDoesNothingWhenFileWasJustEdited() throws IOException {
         SourceControlPlugin.cmEdit(EXPECTED_FILE, PAYLOAD);
         SourceControlPlugin.cmUpdate(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner, never()).execute(eq("add"), anyString());
 	}
 
     @Test
     public void cmDeleteCallsUndoWhenFileIsInPendingChanges() throws IOException {
         setFileInPendingChanges(true);
         SourceControlPlugin.cmDelete(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner).execute("status", EXPECTED_OUTPATH);
         verify (fakeTfRunner).execute("undo /recursive", EXPECTED_OUTPATH);
     }
 
     @Test
     public void cmDeleteCallsDeleteWhenFileExistsInTfs() throws IOException	{
         setFileExistsInTfs(true);
         SourceControlPlugin.cmDelete(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner).execute("dir", EXPECTED_OUTPATH);
         verify(fakeTfRunner).execute("delete /recursive", EXPECTED_OUTPATH);
     }
 
     @Test
 	public void cmDeleteDoesNothingWhenFileDoesNotExistInTfsAndIsNotInPendingChanges() throws IOException	{
         setFileExistsInTfs(false);
         setFileInPendingChanges(false);
         SourceControlPlugin.cmDelete(EXPECTED_FILE, PAYLOAD);
         verify(fakeTfRunner, never()).execute(contains("delete"), anyString());
         verify(fakeTfRunner, never()).execute(contains("undo"), anyString());
 	}
 	
     private void setFileExists(boolean exists) {
         when(fakeFileSystem.fileExists(anyString())).thenReturn(exists);
     }
 
     private void setFileWritable(boolean writable) {
         when (fakeFileSystem.isWritable(anyString())).thenReturn(writable);
     }
 
     private void setFileExistsInTfs(boolean exists) {
         if (exists)
            when(fakeTfRunner.execute("dir", EXPECTED_OUTPATH)).thenReturn("1 item");
     }
 
     private void setFileInPendingChanges(boolean inPendingChanges) {
         if (inPendingChanges)
             when(fakeTfRunner.execute("status", EXPECTED_OUTPATH)).thenReturn("1 change(s)");
     }
 
 }
