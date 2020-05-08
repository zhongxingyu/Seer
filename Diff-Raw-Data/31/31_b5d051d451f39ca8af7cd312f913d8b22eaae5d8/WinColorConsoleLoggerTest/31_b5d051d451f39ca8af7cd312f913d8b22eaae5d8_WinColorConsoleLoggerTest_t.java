 package com.hhnn.tools.ant.listener;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.BuildEvent;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.reset;
 import static org.easymock.EasyMock.verify;
 
 public class WinColorConsoleLoggerTest
 {
 
   private transient WinColorConsoleLogger logger;
   private transient WinColorConsole console;
   private transient PrintStream stream;
 
   @Before
   public void setUp() throws Exception
   {
     console = createMock(WinColorConsole.class);
     logger = new WinColorConsoleLogger();
     logger.setConsole(console);
   }
 
   @Test
   public void testBuildStarted()
   {
     console.keepColors();
     replay(console);
     logger.buildStarted(new BuildEvent(new Project()));
     verify(console);
   }
 
   @Test
   public void testBuildFinished()
   {
    console.keepColors();
     console.restoreColors();
     replay(console);
     logger.buildFinished(new BuildEvent(new Project()));
     verify(console);
   }
 
   @Test
   public void testPrintMessageSequential() throws IOException
   {
     console.keepColors();
      console.setColor(Color.FOREGROUND_RED.winCode());
      console.setColor(Color.FOREGROUND_MAGENTA.winCode());
      console.setColor(Color.FOREGROUND_CYAN.winCode());
      console.setColor(Color.FOREGROUND_GREEN.winCode());
      console.setColor(Color.FOREGROUND_BLUE.winCode());
      replay(console);
 
      stream = new PrintStream(new File("target/log/testPrintMessageSequential.log"));
 
      logger.printMessage("test err", stream, Project.MSG_ERR);
      logger.printMessage("test warn", stream, Project.MSG_WARN);
      logger.printMessage("test info", stream, Project.MSG_INFO);
      logger.printMessage("test verbose", stream, Project.MSG_VERBOSE);
      logger.printMessage("test debug", stream, Project.MSG_DEBUG);
      verify(console);
   }
 
   @Test
   public void testPrintMessage() throws IOException
   {
     stream = new PrintStream(new File("target/log/testPrintMessage.log"));
 
    console.keepColors();
     doTestPrintMessage(Project.MSG_ERR, Color.FOREGROUND_RED, stream);
     doTestPrintMessage(Project.MSG_WARN, Color.FOREGROUND_MAGENTA, stream);
     doTestPrintMessage(Project.MSG_INFO, Color.FOREGROUND_CYAN, stream);
     doTestPrintMessage(Project.MSG_VERBOSE, Color.FOREGROUND_GREEN, stream);
     doTestPrintMessage(Project.MSG_DEBUG, Color.FOREGROUND_BLUE, stream);
   }
 
   private void doTestPrintMessage(final int level, final Color color, final PrintStream stream)
   {
     console.setColor(color.winCode());
     replay(console);
     logger.printMessage("test " + color, stream, level);
     verify(console);
     reset(console);
   }
 
   @Test(expected=NullPointerException.class)
   public void testPrintMessageWithNullConsole()
   {
     logger.setConsole(null);
     doTestPrintMessage(Project.MSG_ERR, Color.FOREGROUND_RED, System.out);
   }
 
   @Test
   public void testPrintMessageWithNullMessageDoesNothing()
   {
    console.keepColors();
     replay(console);
     logger.printMessage(null, System.out, Project.MSG_ERR);
     verify(console);
 
     logger.printMessage(null, null, Project.MSG_ERR);
     verify(console);
   }
 }
