 /*
  * Team : AGF AM / OSI / SI / BO
  *
  * Copyright (c) 2001 AGF Asset Management.
  */
 package net.codjo.test.release.task.gui;
 import com.sun.image.codec.jpeg.JPEGCodec;
 import com.sun.image.codec.jpeg.JPEGImageEncoder;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import junit.extensions.jfcunit.JFCTestCase;
 import junit.extensions.jfcunit.TestHelper;
 import junit.extensions.jfcunit.finder.DialogFinder;
 import org.apache.log4j.Logger;
 import org.apache.tools.ant.Project;
 /**
  * Classe permettant d'excuter une {@link GuiStep}.
  *
  * @noinspection JUnitTestCaseInProductSource,UnconstructableJUnitTestCase,JUnitTestClassNamingConvention
  */
 public class StepPlayer extends JFCTestCase {
     private static final Logger LOG = Logger.getLogger(StepPlayer.class);
     public static final String TEST_DIRECTORY = "test.dir";
     private GuiStep step;
     private Project project;
     private final String testDirectory;
     private TestContext currentTestContext;
 
 
     public StepPlayer(Project project, String testDirectory) {
         this.project = project;
         this.testDirectory = testDirectory;
     }
 
 
     public void play(GuiStep guiStep) {
         this.step = guiStep;
         this.currentTestContext = new TestContext(this, project);
         if (testDirectory != null) {
             currentTestContext.setProperty(TEST_DIRECTORY, testDirectory);
         }
        setName("playImpl");
         try {
             runBare();
         }
         catch (Throwable throwable) {
             saveScreenshot();
             throw new GuiException(currentTestContext.getTestLocation().getLocationMessage() + " \n--> "
                                    + throwable.getMessage(), throwable);
         }
     }
 
 
     public void playImpl() {
         step.proceed(currentTestContext);
     }
 
 
     public void cleanUp() {
         try {
             runCode(new Runnable() {
                 public void run() {
                     try {
                         resumeAWT();
                         TestHelper.cleanUp(StepPlayer.this);
                         DialogFinder dialogFinder = new DialogFinder(null);
                         dialogFinder.setWait(0);
                         Window window = TestHelper.getWindow(dialogFinder);
                         if (window != null) {
                             window.setVisible(false);
                             window.dispose();
                         }
                         flushAWT();
                     }
                     catch (Throwable e) {
                         setError(e);
                     }
                 }
             });
         }
         catch (Throwable throwable) {
             LOG.warn("cleanup has failed", throwable);
         }
     }
 
 
     private void saveScreenshot() {
         try {
             Robot robot = new Robot();
             BufferedImage image =
                   robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
 
             JPEGImageEncoder encoder =
                   JPEGCodec.createJPEGEncoder(new FileOutputStream(determineScreenShotFile()));
 
             encoder.encode(image, encoder.getDefaultJPEGEncodeParam(image));
         }
         catch (Throwable throwable) {
             LOG.warn("Echec du Screenshot", throwable);
         }
     }
 
 
     File determineScreenShotFile() {
         String imageName;
         if (project.getUserProperty("ant.file") != null) {
             imageName = new File(project.getUserProperty("ant.file")).getName();
         }
         else {
             imageName = "Error-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
         }
         return new File("./target/", imageName + ".jpeg");
     }
 }
