 package ru.alepar.minesweeper.thirdparty;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import ru.alepar.minesweeper.testsupport.DesignedFor;
 import ru.alepar.minesweeper.testsupport.OsSpecificRespectingClassRunner;
 
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static ru.alepar.minesweeper.testsupport.OS.WINDOWS;
 
 
 @RunWith(OsSpecificRespectingClassRunner.class)
 public class WinmineApplicationTest {
 
     @Test(timeout = 2000L) @DesignedFor(WINDOWS)
     public void startsAndClosesApplicationWithoutExceptions() throws Exception {
         WinmineApplication app = new WinmineApplication();
         try {
             assertThat(WinmineApplication.findWinmineWindow(), notNullValue());
         } finally {
             app.close();
         }
         assertThat(WinmineApplication.findWinmineWindow(), nullValue());
     }
 
     @Test @DesignedFor(WINDOWS)
     public void suppliesNotNullWindowScreenshot() throws Exception {
         WinmineApplication app = new WinmineApplication();
         try {
             assertThat(app.getScreenshot(), notNullValue());
         } finally {
             app.close();
         }
     }
 }
