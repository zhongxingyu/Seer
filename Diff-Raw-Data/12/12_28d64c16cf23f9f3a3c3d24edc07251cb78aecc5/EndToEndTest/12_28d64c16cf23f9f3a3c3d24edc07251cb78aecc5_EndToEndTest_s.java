 package com.id.app;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.id.editor.Editor;
 import com.id.file.Tombstone;
 import com.id.fuzzy.FuzzyFinder;
 import com.id.git.GitRepository;
 import com.id.git.Repository;
 import com.id.platform.RealFileSystem;
 
 public class EndToEndTest {
   private ListModel<Editor> editors;
   private ListModel<Editor> stack;
   private Repository repository;
   private FuzzyFinder fuzzyFinder;
   private Controller controller;
   private File tempDir;
   private RealFileSystem fileSystem;
   private BashShell shell;
   private HighlightState highlightState;
 
   @Before
   public void setup() throws IOException {
     tempDir = createTempDirectory();
     shell = new BashShell(tempDir);
     editors = new ListModel<Editor>();
     stack = new ListModel<Editor>();
     fileSystem = new RealFileSystem(tempDir);
     repository = new GitRepository(shell);
     fuzzyFinder = new FuzzyFinder(fileSystem);
     highlightState = new HighlightState();
    controller = new Controller(editors, fileSystem, fuzzyFinder, repository, highlightState, stack);
     repository.init();
   }
 
   @After
   public void shutdown() throws IOException {
     shell.exec("rm -rf " + tempDir.getAbsolutePath());
   }
 
   @Test
   public void deletedLinesCreateGraves() throws IOException {
     fileSystem.save("a", "1", "2", "3", "4", "5", "6", "7", "a1", "a2", "a3");
     repository.commitAll(null);
     fileSystem.save("a", "1", "2", "3", "4", "5", "6", "7", "a1");
 
     controller.importDiffs();
     Editor editor = editors.get(0);
     assertEquals("a", editor.getFilename());
     assertEquals(2, editor.getGrave(7).size());
     assertEquals(Tombstone.Status.NORMAL, editor.getStatus(0));
   }
 
   @Test
   public void deletedLinesShowUp() {
     fileSystem.save("a", "a1", "a2", "a3", "a4", "a5");
     repository.commitAll(null);
     fileSystem.save("a", "a1", "a4");
 
     controller.importDiffs();
     Editor editor = editors.get(0);
     assertEquals("a", editor.getFilename());
     assertEquals(2, editor.getGrave(0).size());
     assertEquals(1, editor.getGrave(1).size());
     assertEquals(Tombstone.Status.NORMAL, editor.getStatus(0));
     assertEquals(Tombstone.Status.NORMAL, editor.getStatus(1));
   }
 
   private File createTempDirectory() throws IOException {
     String tempPath = System.getProperty("java.io.tmpdir");
     File file = new File(tempPath, "idtest");
     if (!file.mkdir()) {
       exec("rm -rf " + file.getAbsolutePath());
       file.mkdir();
     }
     return file;
   }
 
   private void exec(String string) throws IOException {
     Runtime.getRuntime().exec(string, null, tempDir);
   }
 
 }
