 package org.tanukisoftware.wrapper.test;
 
 /*
  * Copyright (c) 1999, 2004 Tanuki Software
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of the Java Service Wrapper and associated
  * documentation files (the "Software"), to deal in the Software
  * without  restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sub-license,
  * and/or sell copies of the Software, and to permit persons to
  * whom the Software is furnished to do so, subject to the
  * following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  * 
  * 
  * Portions of the Software have been derived from source code
  * developed by Silver Egg Technology under the following license:
  * 
  * Copyright (c) 2001 Silver Egg Technology
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sub-license, and/or 
  * sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following 
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  */
 
 // $Log$
// Revision 1.16  2004/04/15 06:42:11  mortenson
// Fix a typo in the access_violation_native action.
//
 // Revision 1.15  2004/03/27 14:39:20  mortenson
 // Add actions for the stopImmediate method.
 //
 // Revision 1.14  2004/01/16 04:41:55  mortenson
 // The license was revised for this version to include a copyright omission.
 // This change is to be retroactively applied to all versions of the Java
 // Service Wrapper starting with version 3.0.0.
 //
 // Revision 1.13  2004/01/15 09:50:30  mortenson
 // Fix some problems where the Wrapper was not handling exit codes correctly.
 //
 // Revision 1.12  2004/01/10 15:44:15  mortenson
 // Rework the test wrapper app so there is less code duplication.
 //
 // Revision 1.11  2004/01/10 13:59:14  mortenson
 // Add a command button to test the user functions.
 //
 // Revision 1.10  2003/10/31 05:59:33  mortenson
 // Added a new method, setConsoleTitle, to the WrapperManager class which
 // enables the application to dynamically set the console title.
 //
 // Revision 1.9  2003/10/30 17:13:24  mortenson
 // Add an action to the WrapperActionServer which makes it possible to test
 // simmulate a JVM hang for testing.
 //
 // Revision 1.8  2003/10/18 07:51:10  mortenson
 // The DeadlockPrintStream should not be set until after the WrapperManager class
 // has been initialized.
 //
 // Revision 1.7  2003/10/18 07:35:30  mortenson
 // Add test cases to test how the wrapper handles it when the System.out stream
 // becomes deadlocked.  This can happen if buggy usercode overrides those streams.
 //
 // Revision 1.5  2003/06/19 05:45:00  mortenson
 // Modified the suggested behavior of the WrapperListener.controlEvent() method.
 //
 // Revision 1.4  2003/06/07 05:19:10  mortenson
 // Add a new class, WrapperActionServer, which makes it easy to remotely control
 // the Wrapper remotely by opening a socket and sending commands.  See the
 // javadocs of the class for more details.
 //
 // Revision 1.3  2003/04/03 04:05:22  mortenson
 // Fix several typos in the docs.  Thanks to Mike Castle.
 //
 // Revision 1.2  2003/03/02 08:39:56  mortenson
 // Improve the example code for the controlEvent method.
 //
 // Revision 1.1  2003/02/03 06:55:29  mortenson
 // License transfer to TanukiSoftware.org
 //
 
 import java.awt.Button;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import org.tanukisoftware.wrapper.WrapperActionServer;
 import org.tanukisoftware.wrapper.WrapperManager;
 import org.tanukisoftware.wrapper.WrapperListener;
 
 /**
  * This is a Test / Example program which can be used to test the
  *  main features of the Wrapper.
  * <p>
  * It is also an example of Integration Method #3, where you implement
  *  the WrapperListener interface manually.
  * <p>
  * <b>NOTE</b> that in most cases you will want to use Method #1, using the
  *  WrapperSimpleApp helper class to integrate your application.  Please
  *  see the <a href="http://wrapper.tanukisoftware.org/doc/english/integrate.html">integration</a>
  *  section of the documentation for more details.
  *
  * @author Leif Mortenson <leif@tanukisoftware.com>
  * @version $Revision$
  */
 public class Main
     extends AbstractActionApp
     implements WrapperListener
 {
     private MainFrame m_frame;
     
     private DeadlockPrintStream m_out;
     private DeadlockPrintStream m_err;
     
     private Thread m_userRunner;
     
     /**************************************************************************
      * Constructors
      *************************************************************************/
     private Main() {
     }
     
     private class MainFrame extends Frame implements ActionListener
     {
         MainFrame()
         {
             super( "Wrapper Test Application" );
             
             WrapperManager.setConsoleTitle( getTitle() );
             
             init();
             pack();
             setResizable( false );
         }
         
         private void init()
         {
             GridBagLayout gridBag = new GridBagLayout();
             GridBagConstraints c = new GridBagConstraints();
             setLayout( gridBag );
             
             buildCommand( gridBag, c, "Stop(0)", "stop0",
                 "Calls WrapperManager.stop( 0 ) to shutdown the JVM and Wrapper with a success exit code." );
             
             buildCommand( gridBag, c, "Stop(1)", "stop1",
                 "Calls WrapperManager.stop( 1 ) to shutdown the JVM and Wrapper with a failure exir code." );
             
             buildCommand( gridBag, c, "Exit(0)", "exit0",
                 "Calls System.exit( 0 ) to shutdown the JVM and Wrapper with a success exit code." );
             
             buildCommand( gridBag, c, "Exit(1)", "exit1",
                 "Calls System.exit( 1 ) to shutdown the JVM and Wrapper with a failure exit code." );
             
             buildCommand( gridBag, c, "StopImmediate(0)", "stopimmediate0",
                 "Calls WrapperManager.stopImmediate( 0 ) to immediately shutdown the JVM and Wrapper with a success exit code." );
             
             buildCommand( gridBag, c, "StopImmediate(1)", "stopimmediate1",
                 "Calls WrapperManager.stopImmediate( 1 ) to immediately shutdown the JVM and Wrapper with a failure exir code." );
             
             buildCommand( gridBag, c, "Halt", "halt",
                 "Calls Runtime.getRuntime().halt(0) to kill the JVM, the Wrapper will restart it." );
             
             buildCommand( gridBag, c, "Request Restart", "restart",
                 "Calls WrapperManager.restart() to shutdown the current JVM and start a new one." );
             
             buildCommand( gridBag, c, "Access Violation", "access_violation",
                 "Attempts to cause an access violation within the JVM, relies on a JVM bug and may not work." );
             
            buildCommand( gridBag, c, "Native Access Violation", "access_violation_native",
                 "Causes an access violation using native code, the JVM will crash and be restarted." );
             
             buildCommand( gridBag, c, "Simulate JVM Hang", "appear_hung",
                 "Makes the JVM appear to be hung as viewed from the Wrapper, it will be killed and restarted." );
             
             buildCommand( gridBag, c, "Request Thread Dump", "dump",
                 "Calls WrapperManager.requestThreadDump() to cause the JVM to dump its current thread state." );
             
             buildCommand( gridBag, c, "System.out Deadlock", "deadlock_out",
                 "Simulates a failure mode where the System.out object has become deadlocked." );
             
             buildCommand( gridBag, c, "Poll Users", "users",
                 "Begins calling WrapperManager.getUser() and getInteractiveUser() to monitor the current and interactive users." );
             
             buildCommand( gridBag, c, "Poll Users with Groups", "groups",
                 "Same as above, but includes information about the user's groups." );
         }
         
         private void buildCommand( GridBagLayout gridBag,
                                    GridBagConstraints c,
                                    String label,
                                    String command,
                                    String description )
         {
             Button button = new Button( label );
             button.setActionCommand( command );
             
             c.fill = GridBagConstraints.BOTH;
             c.gridwidth = 1;
             gridBag.setConstraints( button, c );
             add( button );
             button.addActionListener(this);
             
             c.gridwidth = GridBagConstraints.REMAINDER;
             Label desc = new Label( description );
             gridBag.setConstraints( desc, c );
             add( desc );
         }
         
         public void actionPerformed( ActionEvent event )
         {
             Main.this.doAction( event.getActionCommand() );
         }
     }
     
     /**************************************************************************
      * WrapperListener Methods
      *************************************************************************/
     public Integer start( String[] args )
     {
         System.out.println( "start()" );
 
         prepareSystemOutErr();
         
         try
         {
             m_frame = new MainFrame();
             m_frame.setVisible( true );
         }
         catch ( java.lang.InternalError e )
         {
             System.out.println();
             System.out.println( "ERROR - Unable to display the Swing GUI:" );
             System.out.println( "          " + e.toString() );
             System.out.println( "Exiting" );
             System.out.println();
             return new Integer( 1 );
         }
 
         try
         {
             int port = 9999;
             WrapperActionServer server = new WrapperActionServer( port );
             server.enableShutdownAction( true );
             server.enableHaltExpectedAction( true );
             server.enableRestartAction( true );
             server.enableThreadDumpAction( true );
             server.enableHaltUnexpectedAction( true );
             server.enableAccessViolationAction( true );
             server.enableAppearHungAction( true );
             server.start();
             
             System.out.println( "ActionServer Enabled. " );
             System.out.println( "  Telnet localhost 9999" );
             System.out.println( "  Commands: " );
             System.out.println( "    S: Shutdown" );
             System.out.println( "    H: Expected Halt" );
             System.out.println( "    R: Restart" );
             System.out.println( "    D: Thread Dump" );
             System.out.println( "    U: Unexpected Halt (Simmulate crash)" );
             System.out.println( "    V: Access Violation (Actual crash)" );
             System.out.println( "    G: Make the JVM appear to be hung." );
         }
         catch ( java.io.IOException e )
         {
             System.out.println( "Unable to open the action server socket: " + e.getMessage() );
         }
         
         return null;
     }
     
     public int stop( int exitCode )
     {
         System.out.println( "stop(" + exitCode + ")" );
         
         if ( m_frame != null )
         {
             if ( !WrapperManager.hasShutdownHookBeenTriggered() )
             {
                 m_frame.setVisible( false );
                 m_frame.dispose();
             }
             m_frame = null;
         }
         
         return exitCode;
     }
     
     public void controlEvent( int event )
     {
         System.out.println( "controlEvent(" + event + ")" );
         
         if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT )
             && WrapperManager.isLaunchedAsService() )
         {
             System.out.println( "  Ignoring logoff event" );
             // Ignore
         }
         else
         {
             WrapperManager.stop( 0 );
         }
     }
     
     /**************************************************************************
      * Main Method
      *************************************************************************/
     /**
      * IMPORTANT: Please read the Javadocs for this class at the top of the
      *  page before you start to use this class as a template for integrating
      *  your own application.  This will save you a lot of time.
      */
     public static void main( String[] args )
     {
         System.out.println( "Initializing..." );
         
         // Start the application.  If the JVM was launched from the native
         //  Wrapper then the application will wait for the native Wrapper to
         //  call the application's start method.  Otherwise the start method
         //  will be called immediately.
         WrapperManager.start( new Main(), args );
     }
 }
 
