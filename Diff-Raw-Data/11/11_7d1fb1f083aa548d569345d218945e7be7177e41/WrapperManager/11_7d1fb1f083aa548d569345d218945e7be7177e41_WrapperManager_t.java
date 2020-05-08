 package org.tanukisoftware.wrapper;
 
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
// Revision 1.33  2004/03/18 07:40:43  mortenson
// Fix a problem where unwanted read timeout messages were being displayed when
// the ping interval was set to a large value.
//
 // Revision 1.32  2004/03/10 14:06:52  mortenson
 // Add some additional debug output to make it easier to debug startup,
 // shutdown and restart problems.
 //
 // Revision 1.31  2004/01/24 17:46:30  mortenson
 // The addition of the getInteractiveUser placed a dependency on Windows NT
 // version 5.0.  Work around this so the Wrapper can still be used on NT 4.0.
 //
 // Revision 1.30  2004/01/16 04:42:00  mortenson
 // The license was revised for this version to include a copyright omission.
 // This change is to be retroactively applied to all versions of the Java
 // Service Wrapper starting with version 3.0.0.
 //
 // Revision 1.29  2004/01/10 16:45:30  mortenson
 // Add a way to get information about the currently logged in user.
 //
 // Revision 1.28  2004/01/09 05:15:11  mortenson
 // Implement a tick timer and convert the system time over to be compatible.
 //
 // Revision 1.27  2004/01/01 12:51:54  mortenson
 // Requesting the groups of a user is a fairly heavy operation on Windows, so make
 // the requesting of a users groups optional.
 //
 // Revision 1.26  2003/11/05 16:45:43  mortenson
 // The WrapperManager class now checks to make sure that its current version
 // matches the version of the native library and Wrapper.
 //
 // Revision 1.25  2003/11/02 20:55:05  mortenson
 // Add some javadocs.
 // Remove code that was just checked in so it can be used later if ever needed.
 //
 // Revision 1.24  2003/11/02 20:29:30  mortenson
 // Add the ability to get information about the user account which is running the
 // Wrapper as well as the user account with which the Wrapper is interacting.
 //
 // Revision 1.23  2003/10/31 17:30:52  mortenson
 // Add some additional debug output to help identify the cause of problems
 // loading the native library.
 //
 // Revision 1.22  2003/10/31 10:57:53  mortenson
 // Fix a problem where CTRL-C was being ignored by the WrapperManager if a
 // WrapperListener is never registered.
 //
 // Revision 1.21  2003/10/31 05:59:34  mortenson
 // Added a new method, setConsoleTitle, to the WrapperManager class which
 // enables the application to dynamically set the console title.
 //
 // Revision 1.20  2003/10/18 08:11:16  mortenson
 // Modify the WrapperManager class so it now stores references to System.out
 // and System.err on initialization and always writes to those stored streams.
 //
 // Revision 1.19  2003/10/12 18:59:06  mortenson
 // Add a new property, wrapper.native_library, which can be used to specify
 // the base name of the native library.
 //
 // Revision 1.18  2003/10/12 18:01:52  mortenson
 // Back out some changes which made the WrapperManager look for the native
 // library by using the OS/arch as keys.  I want to solve that problem a different
 // way.
 //
 // Revision 1.17  2003/09/12 04:03:52  mortenson
 // Make it possible to load the native library using files named after the platform
 // and architecture.
 //
 // Revision 1.16  2003/09/04 05:40:08  mortenson
 // Added a new wrapper.ping.interval property which lets users control the
 // frequency that the Wrapper pings the JVM.
 //
 // Revision 1.15  2003/09/03 14:39:58  mortenson
 // Added a pair of MBean interfaces which allow the Wrapper to be controlled
 // using JMX.  See the new JMX section in the documentation for details.
 //
 // Revision 1.14  2003/09/03 09:26:26  mortenson
 // Modify the WrapperManager.isLaunchedAsService() method on UNIX systems so it
 // now returns true if the Wrapper was launched with the wrapper.daemonize flag
 // set.
 //
 // Revision 1.13  2003/09/03 02:33:38  mortenson
 // Requested restarts no longer reset the restart count.
 // Add new wrapper.ignore_signals property.
 //
 // Revision 1.12  2003/08/28 07:21:54  mortenson
 // Remove output to System.out in the WrapperManager.requestThreadDump()
 // method to avoid hang problems if the JVM is already hung while accessing
 // the System.out object.
 //
 // Revision 1.11  2003/08/14 09:31:34  mortenson
 // Fix a problem where the native library was missing from the release of OSX
 //
 // Revision 1.10  2003/07/01 14:49:45  mortenson
 // Fix a problem where the JVM would sometimes hang when trying to shutdown if
 // the wrapper.key parameter was passed to the JVM while not being controlled
 // by the Wrapper.
 //
 // Revision 1.9  2003/06/07 05:18:32  mortenson
 // Add a new method WrapperManager.stopImmediate which will cause the JVM to
 // exit immediately without calling any stop methods or shutdown hooks.
 //
 // Revision 1.8  2003/05/29 09:27:14  mortenson
 // Improve the debug output so that packet codes are now shown using a name
 // rather than a raw number.
 //
 // Revision 1.7  2003/04/15 15:32:06  mortenson
 // Fix a typo in a warning message.
 //
 // Revision 1.6  2003/04/09 06:26:14  mortenson
 // Add some extra checks in the event where the native library can not be loaded
 // so that the WrapperManager can differentiate between the library missing and
 // not being readable due to permission problems.
 //
 // Revision 1.5  2003/04/03 04:05:23  mortenson
 // Fix several typos in the docs.  Thanks to Mike Castle.
 //
 // Revision 1.4  2003/04/02 10:05:53  mortenson
 // Modified the wrapper.ping.timeout property so it also controls the ping
 // timeout within the JVM.  Before the timeout on responses to the Wrapper
 // could be controlled, but the ping timeout within the JVM was hardcoded to
 // 30 seconds.
 //
 // Revision 1.3  2003/03/07 02:11:18  mortenson
 // Fix a problem with the wrapper.disable_shutdown_hook.  Due to a typo in the
 // source, the property was being ignored.  This was broken in the 3.0.0 release.
 //
 // Revision 1.2  2003/03/02 04:23:31  mortenson
 // Add a little more javadocs.
 //
 // Revision 1.1  2003/02/03 06:55:28  mortenson
 // License transfer to TanukiSoftware.org
 //
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.BindException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.tanukisoftware.wrapper.resources.ResourceManager;
 
 /**
  * Handles all communication with the native portion of the Wrapper code.
  *	The native wrapper code will launch Java in a separate process and set
  *	up a server socket which the Java code is expected to open a socket to
  *	on startup.  When the server socket is created, a port will be chosen
  *	depending on what is available to the system.  This port will then be
  *	passed to the Java process as property named "wrapper.port".
  *
  * For security reasons, the native code will only allow connections from
  *	localhost and will expect to receive the key specified in a property
  *	named "wrapper.key".
  *
  * This class is implemented as a singleton class.
  *
  * Generate JNI Headers with the following command in the build/classes
  *  directory:
  *    javah -jni -classpath ./ org.tanukisoftware.wrapper.WrapperManager
  *
  * @author Leif Mortenson <leif@tanukisoftware.com>
  * @version $Revision$
  */
 public final class WrapperManager
     implements Runnable
 {
     private static final String  WRAPPER_CONNECTION_THREAD_NAME = "Wrapper-Connection";
     
     private static final int DEFAULT_PORT                = 15003;
     private static final int DEFAULT_SO_TIMEOUT          = 10000;
     private static final long DEFAULT_CPU_TIMEOUT        = 10000L;
     
     /** The number of milliseconds in one tick.  Used for internal system
      *   time independent time keeping. */
     private static final int TICK_MS                     = 100;
     private static final int TIMER_FAST_THRESHOLD     = 2 * 24 * 3600 * 1000 / TICK_MS; // 2 days.
     private static final int TIMER_SLOW_THRESHOLD     = 2 * 24 * 3600 * 1000 / TICK_MS; // 2 days.
     
     private static final byte WRAPPER_MSG_START          = (byte)100;
     private static final byte WRAPPER_MSG_STOP           = (byte)101;
     private static final byte WRAPPER_MSG_RESTART        = (byte)102;
     private static final byte WRAPPER_MSG_PING           = (byte)103;
     private static final byte WRAPPER_MSG_STOP_PENDING   = (byte)104;
     private static final byte WRAPPER_MSG_START_PENDING  = (byte)105;
     private static final byte WRAPPER_MSG_STARTED        = (byte)106;
     private static final byte WRAPPER_MSG_STOPPED        = (byte)107;
     private static final byte WRAPPER_MSG_KEY            = (byte)110;
     private static final byte WRAPPER_MSG_BADKEY         = (byte)111;
     private static final byte WRAPPER_MSG_LOW_LOG_LEVEL  = (byte)112;
     private static final byte WRAPPER_MSG_PING_TIMEOUT   = (byte)113;
     
     /** Log commands are actually 116 + the LOG LEVEL. */
     private static final byte WRAPPER_MSG_LOG            = (byte)116;
     
     public static final int WRAPPER_CTRL_C_EVENT         = 200;
     public static final int WRAPPER_CTRL_CLOSE_EVENT     = 201;
     public static final int WRAPPER_CTRL_LOGOFF_EVENT    = 202;
     public static final int WRAPPER_CTRL_SHUTDOWN_EVENT  = 203;
     
     /** Log message at debug log level. */
     public static final int WRAPPER_LOG_LEVEL_DEBUG      = 1;
     /** Log message at info log level. */
     public static final int WRAPPER_LOG_LEVEL_INFO       = 2;
     /** Log message at status log level. */
     public static final int WRAPPER_LOG_LEVEL_STATUS     = 3;
     /** Log message at warn log level. */
     public static final int WRAPPER_LOG_LEVEL_WARN       = 4;
     /** Log message at error log level. */
     public static final int WRAPPER_LOG_LEVEL_ERROR      = 5;
     /** Log message at fatal log level. */
     public static final int WRAPPER_LOG_LEVEL_FATAL      = 6;
     
     /** Reference to the original value of System.out. */
     private static PrintStream m_out;
     
     /** Reference to the original value of System.err. */
     private static PrintStream m_err;
     
     private static boolean m_disposed = false;
     private static boolean m_started = false;
     private static WrapperManager m_instance = null;
     private static Thread m_hook = null;
     private static boolean m_hookTriggered = false;
     
     private static String[] m_args;
     private static int m_port    = DEFAULT_PORT;
     private static String m_key;
     private static int m_soTimeout = DEFAULT_SO_TIMEOUT;
     private static long m_cpuTimeout = DEFAULT_CPU_TIMEOUT;
     
     /** The number of threads to ignore when deciding when all application
      *   threads have completed. */
     private static int m_systemThreadCount;
     
     /** The lowest configured log level in the Wrapper's configuration.  This 
      *   is set to a high value by default to disable all logging if the
      *   Wrapper does not register its low level or is not present. */
     private static int m_lowLogLevel = WRAPPER_LOG_LEVEL_FATAL + 1;
     
     /** The maximum amount of time in ms to allow to pass without the JVM
      *   pinging the server before the JVM is terminated to allow a resynch. */
     private static int m_pingTimeout = 30000;
     
     /** Flag, set when the JVM is launched that is used to remember whether
      *   or not system signals are supposed to be ignored. */
     private static boolean m_ignoreSignals = false;
     
     /** Thread which processes all communications with the native code. */
     private static Thread m_commRunner;
     private static boolean m_commRunnerStarted = false;
     private static Thread m_eventRunner;
     private static int m_eventRunnerTicks;
     
     /** True if the system time should be used for internal timeouts. */
     private static boolean m_useSystemTime;
     
     /** The threashold of how many ticks the timer can be fast before a
      *   warning is displayed. */
     private static int m_timerFastThreshold;
     
     /** The threashold of how many ticks the timer can be slow before a
      *   warning is displayed. */
     private static int m_timerSlowThreshold;
     
     /** An integer which stores the number of ticks since the
      *   JVM was launched.  Using an int rather than a long allows the value
      *   to be used without requiring any synchronization.  This is only
      *   used if the m_useSystemTime flag is false. */
     private static volatile int m_ticks;
     
     private static WrapperListener m_listener;
     
     private static int m_lastPingTicks;
     private static ServerSocket m_serverSocket;
     private static Socket m_socket;
     private static boolean m_shuttingDown = false;
     private static boolean m_appearHung = false;
     
     private static Method m_addShutdownHookMethod = null;
     private static Method m_removeShutdownHookMethod = null;
     
     private static boolean m_service = false;
     private static boolean m_debug = false;
     private static int m_jvmId = 0;
     private static boolean m_stopping = false;
     private static Thread m_stoppingThread;
     private static boolean m_libraryOK = false;
     private static byte[] m_commandBuffer = new byte[512];
     
     // message resources: eventually these will be split up
     private static ResourceManager m_res        = ResourceManager.getResourceManager();
     private static ResourceManager m_error      = m_res;
     private static ResourceManager m_warning    = m_res;
     private static ResourceManager m_info       = m_res;
     
     /*---------------------------------------------------------------
      * Class Initializer
      *-------------------------------------------------------------*/
     /**
      * When the WrapperManager class is first loaded, it attempts to load the
      *	configuration file specified using the 'wrapper.config' system property.
      *	When the JVM is launched from the Wrapper native code, the
      *	'wrapper.config' and 'wrapper.key' parameters are specified.
      *	The 'wrapper.key' parameter is a password which is used to verify that
      *	connections are only coming from the native Wrapper which launched the
      *	current JVM.
      */
     static
     {
         // Store references to the original System.out and System.err
         //  PrintStreams.  The WrapperManager will always output to the
         //  original streams so its output will always end up in the
         //  wrapper.log file even if the end user code redirects the
         //  output to another log file.
         // This is also important to be protect the Wrapper's functionality
         //  from the case where the user PrintStream enters a deadlock state.
         m_out = System.out;
         m_err = System.err;
         
         // Check for the debug flag
         m_debug = getBooleanProperty( "wrapper.debug" );
         
         if ( m_debug )
         {
             m_out.println( "WrapperManager class initialized by thread: "
                 + Thread.currentThread().getName()
                 + "  Using classloader: " + WrapperManager.class.getClassLoader() );
         }
         
         // Check for the jvmID
         m_jvmId = getIntProperty( "wrapper.jvmid", 1 );
         if ( m_debug )
         {
             m_out.println( "Wrapper Manager: JVM #" + m_jvmId );
         }
         
         // Initialize the timerTicks to a very high value.  This means that we will
         // always encounter the first rollover (200 * WRAPPER_MS / 1000) seconds
         // after the Wrapper the starts, which means the rollover will be well
         // tested.
         m_ticks = Integer.MAX_VALUE - 200;
         
         m_useSystemTime = ( System.getProperty( "wrapper.use_system_time" ) != null );
         m_timerFastThreshold =
             getIntProperty( "wrapper.timer_fast_threshold", TIMER_FAST_THRESHOLD ) * 1000 / TICK_MS;
         m_timerSlowThreshold =
             getIntProperty( "wrapper.timer_slow_threshold", TIMER_SLOW_THRESHOLD ) * 1000 / TICK_MS;
         
         // Check to see if we should register a shutdown hook
         boolean disableShutdownHook =
             ( System.getProperty( "wrapper.disable_shutdown_hook" ) != null );
         
         // Locate the add and remove shutdown hook methods using reflection so
         //  that this class can be compiled on 1.2.x versions of java.
         try
         {
             m_addShutdownHookMethod =
                 Runtime.class.getMethod( "addShutdownHook", new Class[] { Thread.class } );
             m_removeShutdownHookMethod =
                 Runtime.class.getMethod( "removeShutdownHook", new Class[] { Thread.class } );
         }
         catch ( NoSuchMethodException e )
         {
             if ( m_debug )
             {
                 m_out.println(
                     "Wrapper Manager: Shutdown hooks not supported by current JVM." );
             }
             m_addShutdownHookMethod = null;
             m_removeShutdownHookMethod = null;
             disableShutdownHook = true;
         }
         
         // If the shutdown hook is not disabled, then register it.
         if ( !disableShutdownHook )
         {
             if ( m_debug )
             {
                 m_out.println( "Wrapper Manager: Registering shutdown hook" );
             }
             m_hook = new Thread( "Wrapper-Shutdown-Hook" )
             {
                 /**
                  * Run the shutdown hook. (Triggered by the JVM when it is about to shutdown)
                  */
                 public void run()
                 {
                     if ( m_debug )
                     {
                         m_out.println( "Wrapper Manager: ShutdownHook started" );
                     }
                     
                     // Stop the Wrapper cleanly.
                     m_hookTriggered = true;
                     
                     // If we are not already stopping, then do so.
                     WrapperManager.stop( 0 );
                     
                     if ( m_debug )
                     {
                         m_out.println( "Wrapper Manager: ShutdownHook complete" );
                     }
                 }
             };
             
             // Actually register the shutdown hook using reflection.
             try
             {
                 m_addShutdownHookMethod.invoke( Runtime.getRuntime(), new Object[] { m_hook } );
             }
             catch ( IllegalAccessException e )
             {
                 m_out.println( "Wrapper Manager: Unable to register shutdown hook: "
                     + e.getMessage() );
             }
             catch ( InvocationTargetException e )
             {
                 m_out.println( "Wrapper Manager: Unable to register shutdown hook: "
                     + e.getMessage() );
             }
         }
         
         // A key is required for the wrapper to work correctly.  If it is not
         //  present, then assume that we are not being controlled by the native
         //  wrapper.
         if ( ( m_key = System.getProperty( "wrapper.key" ) ) == null )
         {
             if ( m_debug )
             {
                 m_out.println( "Wrapper Manager: Not using wrapper.  (key not specified)" );
             }
             
             // The wrapper will not be used, so other values will not be used.
             m_port = 0;
             m_service = false;
             m_cpuTimeout = 31557600000L; // One Year.  Effectively never.
         }
         else
         {
             if ( m_debug )
             {
                 m_out.println( "Wrapper Manager: Using wrapper" );
             }
             
             // Replace the System.in stream with one of our own to disable it.
             System.setIn( new WrapperInputStream() );
             
             // A port must have been specified.
             String sPort;
             if ( ( sPort = System.getProperty( "wrapper.port" ) ) == null )
             {
                 String msg = m_res.format( "MISSING_PORT" );
                 m_out.println( msg );
                 throw new ExceptionInInitializerError( msg );
             }
             try
             {
                 m_port = Integer.parseInt( sPort );
             }
             catch ( NumberFormatException e )
             {
                 String msg = m_res.format( "BAD_PORT", sPort );
                 m_out.println( msg );
                 throw new ExceptionInInitializerError( msg );
             }
             
             // Check for the ignore signals flag
             m_ignoreSignals = getBooleanProperty( "wrapper.ignore_signals" );
             
             // If this is being run as a headless server, then a flag would have been set
             m_service = getBooleanProperty( "wrapper.service" );
             
             // Get the cpuTimeout
             String sCPUTimeout = System.getProperty( "wrapper.cpu.timeout" );
             if ( sCPUTimeout == null )
             {
                 m_cpuTimeout = DEFAULT_CPU_TIMEOUT;
             }
             else
             {
                 try
                 {
                     m_cpuTimeout = Integer.parseInt( sCPUTimeout ) * 1000L;
                 }
                 catch ( NumberFormatException e )
                 {
                     String msg = m_res.format( "BAD_CPU_TIMEOUT", sCPUTimeout );
                     m_out.println( msg );
                     throw new ExceptionInInitializerError( msg );
                 }
             }
         }
         
         // Make sure that the version of the Wrapper is correct.
         verifyWrapperVersion();
         
         // Initialize the native code to trap system signals
         initializeNativeLibrary();
         
         // Make sure that the native library's version is correct.
         verifyNativeLibraryVersion();
         
         // Start a thread which looks for control events sent to the
         //  process.  The thread is also used to keep track of whether
         //  the VM has been getting CPU to avoid invalid timeouts and
         //  to maintain the number of ticks since the JVM was launched.
         m_eventRunnerTicks = getTicks();
         m_eventRunner = new Thread( "Wrapper-Control-Event-Monitor" )
         {
             public void run()
             {
                 int lastTickOffset = 0;
                 boolean first = true;
                 
                 while ( !m_shuttingDown )
                 {
                     if ( !m_useSystemTime )
                     {
                         // Get the tick count based on the system time.
                         int sysTicks = getSystemTicks();
                         
                         // Increment the tick counter by 1. This loop takes just slightly
                         //  more than the length of a "tick" but it is a good enough
                         //  approximation for our purposes.  The accuracy of the tick length
                         //  falls sharply when the system is under heavly load, but this
                         //  has the desired effect as the Wrapper is also much less likely
                         //  to encounter false timeouts due to the heavy load.
                         // The ticks field is volatile and a single integer, so it is not
                         //  necessary to synchronize this.
                         // When the ticks count reaches the upper limit of the int range,
                         //  it is ok to just let it overflow and wrap.
                         m_ticks++;
                         
                         // Calculate the offset between the two tick counts.
                         //  This will always work due to overflow.
                         int tickOffset = sysTicks - m_ticks;
                         
                         // The number we really want is the difference between this tickOffset
                         //  and the previous one.
                         int offsetDiff = tickOffset - lastTickOffset;
                         
                         if ( first )
                         {
                             first = false;
                         }
                         else
                         {
                             if ( offsetDiff > m_timerSlowThreshold )
                             {
                                 m_out.println( "The timer fell behind the system clock by "
                                     + ( offsetDiff * TICK_MS ) + "ms." );
                             }
                             else if ( offsetDiff < - m_timerFastThreshold )
                             {
                                 m_out.println( "The system clock fell behind the timer by "
                                     + ( -1 * offsetDiff * TICK_MS ) + "ms." );
                             }
                         }
                         
                         // Store this tick offset for the net time through the loop.
                         lastTickOffset = tickOffset;
                     }
                     
                     // Attempt to detect whether or not we are being starved of CPU.
                     //  This will only have any effect if the m_useSystemTime flag is
                     //  set.
                     int nowTicks = getTicks();
                     long age = getTickAge( m_eventRunnerTicks, nowTicks );
                     if ( age > m_cpuTimeout )
                     {
                         m_out.println( "JVM Process has not received any CPU time for "
                             + ( age / 1000 ) + " seconds.  Extending timeouts." );
                         
                         // Make sure that we don't get any ping timeouts in this event
                         m_lastPingTicks = nowTicks;
                     }
                     m_eventRunnerTicks = nowTicks;
                     
                     if ( m_libraryOK )
                     {
                         // Look for a control event in the wrapper library
                         int event = WrapperManager.nativeGetControlEvent();
                         if ( event != 0 )
                         {
                             WrapperManager.controlEvent( event );
                         }
                     }
                     
                     // Wait before checking for another control event.
                     try
                     {
                         Thread.sleep( TICK_MS );
                     }
                     catch ( InterruptedException e )
                     {
                     }
                 }
             }
         };
         m_eventRunner.setDaemon( true );
         m_eventRunner.start();
         
         // Resolve the system thread count based on the Java Version
         String fullVersion = System.getProperty( "java.fullversion" );
         if ( fullVersion == null )
         {
             fullVersion = System.getProperty( "java.runtime.version" ) + " "
                 + System.getProperty( "java.vm.name" );
         }
         if ( fullVersion.indexOf( "JRockit" ) >= 0 )
         {
             // BEA Weblogic JRockit(R) Virtual Machine
             // This JVM handles its shutdown thread differently that IBM, Sun
             //  and Blackdown.
             m_systemThreadCount = 0;
         }
         else
         {
             // All other known JVMs have a system thread which is used by the
             //  system to trigger a JVM shutdown after all other threads have
             //  terminated.  This thread must be ignored when counting the
             //  remaining number of threads.
             m_systemThreadCount = 1;
         }
         
         if ( m_debug )
         {
             // Display more JVM infor right after the call initialization of the library.
             m_out.println( "Java Version   : " + fullVersion );
             m_out.println( "Java VM Vendor : " + System.getProperty( "java.vm.vendor" ) );
             m_out.println();
         }
         
         // Create the singleton
         m_instance = new WrapperManager();
     }
 
     /*---------------------------------------------------------------
      * Native Methods
      *-------------------------------------------------------------*/
     private static native void nativeInit( boolean debug );
     private static native String nativeGetLibraryVersion();
     private static native int nativeGetControlEvent();
     private static native void nativeRequestThreadDump();
     private static native void accessViolationInner();
     private static native void nativeSetConsoleTitle( byte[] titleBytes );
     private static native WrapperUser nativeGetUser( boolean groups );
     private static native WrapperUser nativeGetInteractiveUser( boolean groups );
     
     /*---------------------------------------------------------------
      * Methods
      *-------------------------------------------------------------*/
     /**
      * Resolves an integer property.
      *
      * @param name The name of the property to lookup.
      * @param defaultValue The value to return if it is not set or is invalid.
      *
      * @return The requested property value.
      */
     private static int getIntProperty( String name, int defaultValue )
     {
         String val = System.getProperty( name );
         if ( val != null )
         {
             try
             {
                 return Integer.parseInt( val );
             }
             catch ( NumberFormatException e )
             {
                 return defaultValue;
             }
         }
         else
         {
             return defaultValue;
         }
     }
     
     /**
      * Returns a tick count calculated from the system clock.
      */
     private static int getSystemTicks()
     {
         // Calculate a tick count using the current system time.  The
         //  conversion from a long in ms, to an int in TICK_MS increments
         //  will result in data loss, but the loss of bits and resulting
         //  overflow is expected and Ok.
         return (int)( System.currentTimeMillis() / TICK_MS );
     }
     
     /**
      * Returns the number of ticks since the JVM was launched.  This
      *  count is not good enough to be used where accuracy is required but
      *  it allows us to implement timeouts in environments where the system
      *  time is modified while the JVM is running.
      * <p>
      * An int is used rather than a long so the counter can be implemented
      *  without requiring any synchronization.  At the tick resolution, the
      *  tick counter will overflow and wrap (every 6.8 years for 100ms ticks).
      *  This behavior is expected.  The getTickAge method should be used
      *  in cases where the difference between two ticks is required.
      *
      * Returns the tick count.
      */
     private static int getTicks()
     {
         if ( m_useSystemTime )
         {
             return getSystemTicks();
         }
         else
         {
             return m_ticks;
         }
     }
     
     /**
      * Returns the number of milliseconds that have elapsed between the
      *  start and end counters.  This method assumes that both tick counts
      *  were obtained by calling getTicks().  This method will correctly
      *  handle cases where the tick counter has overflowed and reset.
      *
      * @param start A base tick count.
      * @param end An end tick count.
      *
      * @return The number of milliseconds that are represented by the
      *         difference between the two specified tick counts.
      */
     private static long getTickAge( int start, int end )
     {
         if ( end < start )
         {
             // The tick counter wrapped.
             return ( ( (long)end + Integer.MAX_VALUE - Integer.MIN_VALUE ) - start ) * TICK_MS;
         }
         else
         {
             return ( end - start ) * (long)TICK_MS;
         }
     }
     
     /**
      * Attempts to load the a native library file.
      *
      * @param name Name of the library to load.
      * @param file Name of the actual library file.
      *
      * @return True if the library was successfully loaded.
      */
     private static boolean loadNativeLibrary( String name, String file )
     {
         try
         {
             System.loadLibrary( name );
             
             if ( m_debug )
             {
                 m_out.println( "Loaded native library: " + file );
             }
             
             return true;
         }
         catch ( UnsatisfiedLinkError e )
         {
             if ( m_debug )
             {
                 m_out.println( "Loading native library failed: " + file + "  Cause: " + e );
             }
             return false;
         }
     }
     
     /**
      * Searches for a file on a path.
      *
      * @param file File to look for.
      * @param path Path to be searched.
      *
      * @return Reference to thr file object if found, otherwise null.
      */
     private static File locateFileOnPath( String file, String path )
     {
         // A library path exists but the library was not found on it.
         String pathSep = System.getProperty( "path.separator" );
         
         // Search for the file on the library path to verify that it does not
         //  exist, it could be some other problem
         StringTokenizer st = new StringTokenizer( path, pathSep );
         while( st.hasMoreTokens() )
         {
             File libFile = new File( new File( st.nextToken() ), file );
             if ( libFile.exists() )
             {
                 return libFile;
             }
         }
         
         return null;
     }
     
     /**
      * Searches for and then loads the native library.  This method will attempt
      *  locate the wrapper library using one of the following 3 naming 
      */
     private static void initializeNativeLibrary()
     {
         String libraryHead;
         String libraryTail;
         
         // Look for the base name of the library.
         String baseName = System.getProperty( "wrapper.native_library" );
         if ( baseName == null )
         {
             // This should only happen if an old version of the Wrapper binary is being used.
             m_out.println( "WARNING - The wrapper.native_library system property was not" );
             m_out.println( "          set. Using the default value, 'wrapper'." );
             baseName = "wrapper";
         }
         
         // Resolve the osname and osarch for the currect system.
         String osName = System.getProperty( "os.name" );
         if ( osName.indexOf( "Windows" ) >= 0 )
         {
             libraryHead = "";
             libraryTail = ".dll";
         }
         else if ( osName.indexOf( "Mac" ) >= 0 )
         {
             libraryHead = "lib";
             libraryTail = ".jnilib";
         }
         else
         {
             libraryHead = "lib";
             libraryTail = ".so";
         }
         
         String file = libraryHead + baseName + libraryTail;
         
         // Attempt to load the native library using various names.
         if ( loadNativeLibrary( baseName, file ) )
         {
             m_libraryOK = true;
             
             // The library was loaded correctly, so initialize it.
             if ( m_debug )
             {
                 m_out.println( "Calling native initialization method." );
             }
             nativeInit( m_debug );
         }
         else
         {
             m_libraryOK = false;
             
             // The library could not be loaded, so we want to give the user a useful
             //  clue as to why not.
             String libPath = System.getProperty( "java.library.path" );
             m_out.println();
             if ( libPath.equals( "" ) )
             {
                 // No library path
                 m_out.println(
                     "WARNING - Unable to load the Wrapper's native library because the" );
                 m_out.println(
                     "          java.library.path was set to ''.  Please see the" );
                 m_out.println(
                     "          documentation for the wrapper.java.library.path " );
                 m_out.println(
                     "          configuration property.");
             }
             else
             {
                 File libFile = locateFileOnPath( file, libPath );
                 if ( libFile == null )
                 {
                     // The library could not be located on the library path.
                     m_out.println(
                         "WARNING - Unable to load the Wrapper's native library because the file" );
                     m_out.println(
                         "          '" + file + "' could not be located in the following" );
                     m_out.println(
                         "          java.library.path:" );
                     String pathSep = System.getProperty( "path.separator" );
                     StringTokenizer st = new StringTokenizer( libPath, pathSep );
                     while ( st.hasMoreTokens() )
                     {
                         File pathElement = new File( st.nextToken() );
                         m_out.println( "            " + pathElement.getAbsolutePath() );
                     }
                     m_out.println(
                         "          Please see the documentation for the "
                         +          "wrapper.java.library.path" );
                     m_out.println(
                         "          configuration property." );
                 }
                 else
                 {
                     // The library file was found but could not be loaded for some reason.
                     m_out.println(
                         "WARNING - Unable to load the Wrapper's native library '" + file + "'." );
                     m_out.println(
                         "          The file is located on the path at the following location but" );
                     m_out.println(
                         "          could not be loaded:" );
                     m_out.println(
                         "            " + libFile.getAbsolutePath() );
                     m_out.println(
                         "          Please verify that the file is readable by the current user" );
                     m_out.println(
                         "          and that the file has not been corrupted in any way." );
                 }
             }
             m_out.println( "          System signals will not be handled correctly." );
             m_out.println();
         }
     }
     
     /**
      * Compares the version of the wrapper which launched this JVM with that of
      *  the jar.  If they differ then a Warning message will be displayed.  The
      *  Wrapper application will still be allowed to start.
      */
     private static void verifyWrapperVersion()
     {
         // If we are not being controlled by the wrapper then return.
         if ( !WrapperManager.isControlledByNativeWrapper() )
         {
             return;
         }
         
         // Lookup the version from the wrapper.  It should have been set as a property
         //  when the JVM was launched.
         String wrapperVersion = System.getProperty( "wrapper.version" );
         if ( wrapperVersion == null )
         {
             wrapperVersion = "unknown";
         }
         
         if ( !WrapperInfo.getVersion().equals( wrapperVersion ) )
         {
             m_out.println(
                 "WARNING - The Wrapper jar file currently in use is version \""
                 + WrapperInfo.getVersion() + "\"" );
             m_out.println(
                 "          while the version of the Wrapper which launched this JVM is " );
             m_out.println(
                 "          \"" + wrapperVersion + "\"." );
             m_out.println(
                 "          The Wrapper may appear to work correctly but some features may" );
             m_out.println(
                 "          not function correctly.  This configuration has not been tested" );
             m_out.println(
                 "          and is not supported." );
             m_out.println();
         }
     }
     
     /**
      * Compares the version of the native library with that of this jar.  If
      *  they differ then a Warning message will be displayed.  The Wrapper
      *  application will still be allowed to start.
      */
     private static void verifyNativeLibraryVersion()
     {
         // If the library was not loaded then return
         if ( !m_libraryOK )
         {
             return;
         }
         
         // Request the version from the native library.  Be careful as the method
         //  will not exist if the library is old.
         String jniVersion;
         try
         {
             jniVersion = nativeGetLibraryVersion();
         }
         catch ( Throwable e )
         {
             if ( m_debug )
             {
                 m_out.println( "Call to nativeGetLibraryVersion() failed: " + e );
             }
             jniVersion = "unknown";
         }
         
         if ( !WrapperInfo.getVersion().equals( jniVersion ) )
         {
             m_out.println(
                 "WARNING - The Wrapper jar file currently in use is version \""
                 + WrapperInfo.getVersion() + "\"" );
             m_out.println(
                 "          while the version of the native library is \"" + jniVersion + "\"." );
             m_out.println(
                 "          The Wrapper may appear to work correctly but some features may" );
             m_out.println(
                 "          not function correctly.  This configuration has not been tested" );
             m_out.println(
                 "          and is not supported." );
             m_out.println();
         }
     }
     
     /**
      * Returns true if the specified system property is set.
      *
      * @return True if the specified system property is set.
      */
     private static boolean getBooleanProperty( String name )
     {
         return ( System.getProperty( name ) != null );
     }
     
     /**
      * Obtain the current version of Wrapper.
      *
      * @return The version of the Wrapper.
      */
     public static String getVersion()
     {
         return WrapperInfo.getVersion();
     }
     
     /**
      * Obtain the build time of Wrapper.
      *
      * @return The time that the Wrapper was built.
      */
     public static String getBuildTime()
     {
         return WrapperInfo.getBuildTime();
     }
     
     /**
      * Returns the Id of the current JVM.  JVM Ids increment from 1 each time
      *  the wrapper restarts a new one.
      *
      * @return The Id of the current JVM.
      */
     public static int getJVMId()
     {
         return m_jvmId;
     }
     
     /**
      * Sets the title of the console in which the Wrapper is running.  This
      *  is currently only supported on Windows platforms.
      * <p>
      * As an alternative, it is also possible to set the console title from
      *  within the wrapper.conf file using the wrapper.console.title property.
      *
      * @param title The new title.  The specified string will be encoded
      *              to a byte array using the default encoding for the
      *              current platform.
      */
     public static void setConsoleTitle( String title )
     {
         if ( m_libraryOK )
         {
             // Convert the unicode string to a string of bytes using the default
             //  platform encoding.
             byte[] titleBytes = title.getBytes();
             
             // We need a null terminated string.
             byte[] nullTermBytes = new byte[titleBytes.length + 1];
             System.arraycopy( titleBytes, 0, nullTermBytes, 0, titleBytes.length );
             nullTermBytes[titleBytes.length] = 0;
             
             nativeSetConsoleTitle( nullTermBytes );
         }
     }
     
     /**
      * Returns a WrapperUser object which describes the user under which the
      *  Wrapper is currently running.  Additional platform specific information
      *  can be obtained by casting the object to a platform specific subclass.
      *  WrapperWin32User, for example.
      *
      * @param groups True if the user's groups should be returned as well.
      *               Requesting the groups that a user belongs to increases
      *               the CPU load required to complete the call.
      *
      * @return An object describing the current user.
      */
     public static WrapperUser getUser( boolean groups )
     {
         WrapperUser user = null;
         if ( m_libraryOK )
         {
             user = nativeGetUser( groups );
         }
         return user;
     }
     
     /**
      * Returns a WrapperUser object which describes the interactive user whose
      *  desktop is being interacted with.  When a service running on a Windows
      *  platform has its interactive flag set, this method will return the user
      *  who is currently logged in.  Additional platform specific information
      *  can be obtained by casting the object to a platform specific subclass.
      *  WrapperWin32User, for example.
      * <p>
      * If a user is not currently logged on then this method will return null.
      *  User code can repeatedly call this method to detect when a user has
      *  logged in.  To detect when a user has logged out, there are two options.
      *  1) The user code can continue to call this method until it returns null.
      *  2) Or if the WrapperListener method is being implemented, the
      *     WrapperListener.controlEvent method will receive a WRAPPER_CTRL_LOGOFF_EVENT
      *     event when the user logs out.
      * <p>
      * On XP systems, it is possible to switch to another account rather than
      *  actually logging out.  In such a case, the interactive user will be
      *  the first user that logged in.  This will also be the only user with
      *  which the service will interact.  If other users are logged in when the
      *  interactive user logs out, the service will not automatically switch to
      *  another logged in user.  Rather, the next user to log in will become
      *  the new user which the service will interact with.
      * <p>
      * This method will always return NULL on versions of NT prior to Windows
      *  2000.  This can not be helped as some required functions were not added
      *  to the windows API until NT version 5.0, also known as Windows 2000.
      *
      * @param groups True if the user's groups should be returned as well.
      *               Requesting the groups that a user belongs to increases
      *               the CPU load required to complete the call.
      *
      * @return The current interactive user, or null.
      */
     public static WrapperUser getInteractiveUser( boolean groups )
     {
         WrapperUser user = null;
         if ( m_libraryOK )
         {
             user = nativeGetInteractiveUser( groups );
         }
         return user;
     }
     
     /**
      * Requests that the current JVM process request a thread dump.  This is
      *  the same as pressing CTRL-BREAK (under Windows) or CTRL-\ (under Unix)
      *  in the the console in which Java is running.  This method does nothing
      *  if the native library is not loaded.
      */
     public static void requestThreadDump()
     {
         if ( m_libraryOK )
         {
             nativeRequestThreadDump();
         }
         else
         {
             m_out.println( "  wrapper library not loaded." );
         }
     }
     
     /**
      * (Testing Method) Causes the WrapperManager to go into a state which makes the JVM appear
      *  to be hung when viewed from the native Wrapper code.  Does not have any effect when the
      *  JVM is not being controlled from the native Wrapper. Useful for testing the Wrapper 
      *  functions.
      */
     public static void appearHung()
     {
         m_out.println( "WARNING: Making JVM appear to be hung..." );
         m_appearHung = true;
     }
     
     /**
      * (Testing Method) Cause an access violation within the Java code.  Useful
      *  for testing the Wrapper functions.  This currently only crashes Sun
      *  JVMs and takes advantage of Bug #4369043 which does not exist in newer
      *  JVMs.  Use of the accessViolationNative() method is preferred.
      */
     public static void accessViolation()
     {
         m_out.println( "WARNING: Attempting to cause an access violation..." );
         
         try
         {
             Class c = Class.forName( "java.lang.String" );
             java.lang.reflect.Method m = c.getDeclaredMethod( null, null );
         }
         catch( NoSuchMethodException ex )
         {
             // Correctly did not find method.  access_violation attempt failed.  Not Sun JVM?
         }
         catch( Exception ex )
         {
             if ( ex instanceof NoSuchFieldException )
             {
                 // Can't catch this in a catch because the compiler doesn't think it is being
                 //  thrown.  But it is thrown on IBM jvms at least
                 // Correctly did not find method.  access_violation attempt failed.  Not Sun JVM?
             }
             else
             {
                 // Shouldn't get here.
                 ex.printStackTrace();
             }
         }					
         
         m_out.println( "  Attempt to cause access violation failed.  JVM is still alive." );
     }
 
     /**
      * (Testing Method) Cause an access violation within native JNI code.  Useful for testing the
      *  Wrapper functions. This currently causes the access violation by attempting to write to 
      *  a null pointer.
      */
     public static void accessViolationNative()
     {
         m_out.println( "WARNING: Attempting to cause an access violation..." );
         if ( m_libraryOK )
         {
             accessViolationInner();
         
             m_out.println( "  Attempt to cause access violation failed.  "
                 + "JVM is still alive." );
         }
         else
         {
             m_out.println( "  wrapper library not loaded." );
         }
     }
         
     /**
      * Returns true if the JVM was launched by the Wrapper application.  False
      *  if the JVM was launched manually without the Wrapper controlling it.
      *
      * @return True if the current JVM was launched by the Wrapper.
      */
     public static boolean isControlledByNativeWrapper()
     {
         return m_key != null;
     }
     
     /**
      * Returns true if the Wrapper was launched as an NT service on Windows or
      *  as a daemon process on UNIX platforms.  False if launched as a console.
      *  This can be useful if you wish to display a user interface when in
      *  Console mode.  On UNIX platforms, this is not as useful because an
      *  X display may not be visible even if launched in a console.
      *
      * @return True if the Wrapper is running as an NT service or daemon
      *         process.
      */
     public static boolean isLaunchedAsService()
     {
         return m_service;
     }
     
     /**
      * Returns true if the wrapper.debug property, or any of the logging
      *  channels are set to DEBUG in the wrapper configuration file.  Useful
      *  for deciding whether or not to output certain information to the
      *  console.
      *
      * @return True if the Wrapper is logging any Debug level output.
      */
     public static boolean isDebugEnabled()
     {
         return m_debug;
     }
     
     /**
      * Start the Java side of the Wrapper code running.  This will make it
      *  possible for the native side of the Wrapper to detect that the Java
      *  Wrapper is up and running.
      */
     public static synchronized void start( WrapperListener listener, String[] args )
     {
         m_out.println( "Wrapper (Version " + getVersion() + ") http://wrapper.tanukisoftware.org" );
         m_out.println();
         
         if ( m_debug )
         {
             StringBuffer sb = new StringBuffer();
             sb.append( "args[" );
             for ( int i = 0; i < args.length; i++ )
             {
                 if ( i > 0 )
                 {
                     sb.append( ", " );
                 }
                 sb.append( "\"" );
                 sb.append( args[i] );
                 sb.append( "\"" );
             }
             sb.append( "]" );
             
             m_out.println( "WrapperManager.start(" + listener + ", " + sb.toString() + ") "
                 + "called by thread: " + Thread.currentThread().getName() );
         }
         
         // Make sure that the class has not already been disposed.
         if ( m_disposed)
         {
             throw new IllegalStateException( "WrapperManager has already been disposed." );
         }
         
         if ( m_listener != null )
         {
             throw new IllegalStateException(
                 "WrapperManager has already been started with a WrapperListener." );
         }
         if ( listener == null )
         {
             throw new IllegalStateException( "A WrapperListener must be specified." );
         }
         m_listener = listener;
         
         m_args = args;
         
         startRunner();
         
         // If this JVM is being controlled by a native wrapper, then we want to
         //  wait for the command to start.  However, if this is a standalone
         //  JVM, then we want to start now.
         if ( !isControlledByNativeWrapper() )
         {
             startInner();
         }
     }
     
     /**
      * Tells the native wrapper that the JVM wants to restart, then informs
      *	all listeners that the JVM is about to shutdown before killing the JVM.
      */
     public static void restart()
     {
         if ( m_debug )
         {
             m_out.println( "WrapperManager.restart() called by thread: "
                 + Thread.currentThread().getName() );
         }
         
         boolean stopping;
         synchronized(m_instance)
         {
             stopping = m_stopping;
             if ( !stopping )
             {
                 m_stopping = true;
             }
         }
         
         if ( !stopping )
         {
             if ( !m_commRunnerStarted )
             {
                 startRunner();
                 // Wait to give the runner a chance to connect.
                 try
                 {
                     Thread.sleep(500);
                 }
                 catch ( InterruptedException e )
                 {
                 }
             }
             
             // Always send the stop command
             sendCommand( WRAPPER_MSG_RESTART, "restart" );
         }
         
         // Give the Wrapper a chance to register the stop command before stopping.
         // This avoids any errors thrown by the Wrapper because the JVM died before
         //  it was expected to.
         try
         {
             Thread.sleep( 1000 );
         }
         catch ( InterruptedException e )
         {
         }
         
         stopInner( 0 );
     }
     
     /**
      * Tells the native wrapper that the JVM wants to shut down, then informs
      *	all listeners that the JVM is about to shutdown before killing the JVM.
      *
      * @param exitCode The exit code that the Wrapper will return when it exits.
      */
     public static void stop( int exitCode )
     {
         if ( m_debug )
         {
             m_out.println( "WrapperManager.stop(" + exitCode + ") called by thread: "
                 + Thread.currentThread().getName() );
         }
         
         stopCommon( exitCode, 1000 );
         
         stopInner( exitCode );
     }
 
     /**
      * Tells the native wrapper that the JVM wants to shut down and then
      *  promptly halts.  Be careful when using this method as an application
      *  will not be given a chance to shutdown cleanly.
      *
      * @param exitCode The exit code that the Wrapper will return when it exits.
      */
     public static void stopImmediate( int exitCode )
     {
         if ( m_debug )
         {
             m_out.println( "WrapperManager.stopImmediate(" + exitCode + ") called by thread: "
                 + Thread.currentThread().getName() );
         }
         
         stopCommon( exitCode, 250 );
         
         signalStopped( exitCode );
         
         // Execute runtime.halt(0) using reflection so this class will
         //  compile on 1.2.x versions of Java.
         Method haltMethod;
         try
         {
             haltMethod =
                 Runtime.class.getMethod( "halt", new Class[] { Integer.TYPE } );
         }
         catch ( NoSuchMethodException e )
         {
             m_out.println( "halt not supported by current JVM." );
             haltMethod = null;
         }
         
         if ( haltMethod != null )
         {
             Runtime runtime = Runtime.getRuntime();
             try
             {
                 haltMethod.invoke( runtime, new Object[] { new Integer( 0 ) } );
             }
             catch ( IllegalAccessException e )
             {
                 m_out.println(
                     "Unable to call runtime.halt: " + e.getMessage() );
             }
             catch ( InvocationTargetException e )
             {
                 m_out.println(
                     "Unable to call runtime.halt: " + e.getMessage() );
             }
         }
         else
         {
             // Shutdown normally
             stopInner( exitCode );
         }
     }
     
     /**
      * Signal the native wrapper that the startup is progressing but that more
      *  time is needed.  The Wrapper will extend the startup timeout by the
      *  specified time.
      *
      * @param waitHint Additional time in milliseconds.
      */
     public static void signalStarting( int waitHint )
     {
         sendCommand( WRAPPER_MSG_START_PENDING, Integer.toString( waitHint ) );
     }
 
     /**
      * Signal the native wrapper that the shutdown is progressing but that more
      *  time is needed.  The Wrapper will extend the stop timeout by the
      *  specified time.
      *
      * @param waitHint Additional time in milliseconds.
      */
     public static void signalStopping( int waitHint )
     {
         m_stopping = true;
         sendCommand( WRAPPER_MSG_STOP_PENDING, Integer.toString( waitHint ) );
     }
     
     /**
      * This method should not normally be called by user code as it is called
      *  from within the stop and restart methods.  However certain applications
      *  which stop the JVM may need to call this method to let the wrapper code
      *  know that the shutdown was intentional.
      */
     public static void signalStopped( int exitCode )
     {
         m_stopping = true;
         sendCommand( WRAPPER_MSG_STOPPED, Integer.toString( exitCode ) );
         
         // Give the socket time to actuall send the packet to the Wrapper
         //  as this call is often immediately followed by a halt command.
         try
         {
             Thread.sleep( 250 );
         }
         catch ( InterruptedException e )
         {
             // Ignore.
         }
     }
     
     /**
      * Returns true if the ShutdownHook for the JVM has already been triggered.
      *  Some code needs to know whether or not the system is shutting down.
      *
      * @return True if the ShutdownHook for the JVM has already been triggered.
      */
     public static boolean hasShutdownHookBeenTriggered()
     {
         return m_hookTriggered;
     }
     
     /**
      * Requests that the Wrapper log a message at the specified log level.
      *  If the JVM is not being managed by the Wrapper then calls to this
      *  method will be ignored.  This method has been optimized to ignore
      *  messages at a log level which will not be logged given the current
      *  log levels of the Wrapper.
      * <p>
      * Log messages will currently by trimmed by the Wrapper at 4k (4096 bytes).
      * <p>
      * Because of differences in the way console output is collected and
      *  messages logged via this method, it is expected that interspersed
      *  console and log messages will not be in the correct order in the
      *  resulting log file.
      * <p>
      * This method was added to allow simple logging to the wrapper.log
      *  file.  This is not meant to be a full featured log file and should
      *  not be used as such.  Please look into a logging package for most
      *  application logging.
      *
      * @param logLevel The level to log the message at can be one of
      *                 WRAPPER_LOG_LEVEL_DEBUG, WRAPPER_LOG_LEVEL_INFO,
      *                 WRAPPER_LOG_LEVEL_STATUS, WRAPPER_LOG_LEVEL_WARN,
      *                 WRAPPER_LOG_LEVEL_ERROR, or WRAPPER_LOG_LEVEL_FATAL.
      * @param message The message to be logged.
      */
     public static void log( int logLevel, String message )
     {
         // Make sure that the logLevel is valid to avoid problems with the
         //  command sent to the server.
         
         if ( ( logLevel < WRAPPER_LOG_LEVEL_DEBUG ) || ( logLevel > WRAPPER_LOG_LEVEL_FATAL ) )
         {
             throw new IllegalArgumentException( "The specified logLevel is not valid." );
         }
         if ( message == null )
         {
             throw new IllegalArgumentException( "The message parameter can not be null." );
         }
         
         if ( m_lowLogLevel <= logLevel )
         {
             sendCommand( (byte)( WRAPPER_MSG_LOG + logLevel ), message );
         }
     }
     
     /*---------------------------------------------------------------
      * Constructors
      *-------------------------------------------------------------*/
     /** 
      * This class can not be instantiated.
      */
     private WrapperManager()
     {
     }
     
     /*---------------------------------------------------------------
      * Private methods
      *-------------------------------------------------------------*/
     /**
      * Executed code common to the stop and stopImmediate methods.
      */
     private static void stopCommon( int exitCode, int delay )
     {
         boolean stopping;
         synchronized( m_instance )
         {
             stopping = m_stopping;
             if ( !stopping )
             {
                 m_stopping = true;
             }
         }
         
         if ( !stopping )
         {
             if ( !m_commRunnerStarted )
             {
                 startRunner();
                 // Wait to give the runner a chance to connect.
                 try
                 {
                     Thread.sleep( 500 );
                 }
                 catch ( InterruptedException e )
                 {
                 }
             }
             
             // Always send the stop command
             sendCommand( WRAPPER_MSG_STOP, Integer.toString( exitCode ) );
         }
         
         // Give the Wrapper a chance to register the stop command before stopping.
         // This avoids any errors thrown by the Wrapper because the JVM died before
         //  it was expected to.
         try
         {
             Thread.sleep( delay );
         }
         catch ( InterruptedException e )
         {
         }
     }
     
     /**
      * Dispose of all resources used by the WrapperManager.  Closes the server
      *	socket which is used to listen for events from the 
      */
     private static void dispose()
     {
         synchronized( m_instance.getClass() )
         {
             m_disposed = true;
             
             // Close the open socket if it exists.
             closeSocket();
             
             // Give the Connection Thread a chance to stop itself.
             try
             {
                 Thread.sleep( 500 );
             }
             catch ( InterruptedException e )
             {
             }
         }
     }
     
     /**
      * Informs the listener that it should start.
      */
     private static void startInner()
     {
         // Set the thread priority back to normal so that any spawned threads
         //	will use the normal priority
         int oldPriority = Thread.currentThread().getPriority();
         Thread.currentThread().setPriority( Thread.NORM_PRIORITY );
         
         if ( m_debug )
         {
             m_out.println( "calling listener.start()" );
         }
         if ( m_listener != null )
         {
             // This is user code, so don't trust it.
             try
             {
                 Integer result = m_listener.start( m_args );
                 if ( result != null )
                 {
                     int exitCode = result.intValue();
                     // Signal the native code.
                     stop( exitCode );
                     // Won't make it here.
                     return;
                 }
             }
             catch ( Throwable t )
             {
                 m_out.println( "Error in WrapperListener.start callback.  " + t );
                 t.printStackTrace();
                 // Kill the JVM, but don't tell the wrapper that we want to stop.
                 //  This may be a problem with this instantiation only.
                 stopInner( 1 );
                 // Won't make it here.
                 return;
             }
         }
         if ( m_debug )
         {
             m_out.println( "returned from listener.start()" );
         }
         
         // Crank the priority back up.
         Thread.currentThread().setPriority( oldPriority );
         
         // Signal that the application has started.
         signalStarted();
     }
     
     private static void shutdownJVM( int exitCode )
     {
         // Do not call System.exit if this is the ShutdownHook
         if ( Thread.currentThread() == m_hook )
         {
             // Signal that the application has stopped and the JVM is about to shutdown.
             signalStopped( 0 );
             
             // Dispose the wrapper.  (If the hook runs, it will do this.)
             dispose();
             
             // This is the shutdown hook, so fall through because things are
             //  already shutting down.
         }
         else
         {
             //  We do not want the ShutdownHook to execute, so unregister it before calling exit
             if ( m_hook != null )
             {
                 // Remove the shutdown hook using reflection.
                 try
                 {
                     m_removeShutdownHookMethod.invoke(
                         Runtime.getRuntime(), new Object[] { m_hook } );
                 }
                 catch ( IllegalAccessException e )
                 {
                     m_out.println( "Wrapper Manager: Unable to unregister shutdown hook: "
                         + e.getMessage() );
                 }
                 catch ( InvocationTargetException e )
                 {
                     m_out.println( "Wrapper Manager: Unable to unregister shutdown hook: "
                         + e.getMessage() );
                 }
                 m_hook = null;
             }
             // Signal that the application has stopped and the JVM is about to shutdown.
             signalStopped( 0 );
             
             // Dispose the wrapper.  (If the hook runs, it will do this.)
             dispose();
             
             if ( m_debug )
             {
                 m_out.println( "calling System.exit(" + exitCode + ")" );
             }
             System.exit( exitCode );
         }
     }
     
     /**
      * Informs the listener that the JVM will be shut down.
      */
     private static void stopInner( int exitCode )
     {
         boolean block;
         synchronized( m_instance )
         {
             // Always set the stopping flag.
             m_stopping = true;
             
             // Only one thread can be allowed to continue.
             if ( m_stoppingThread == null )
             {
                 m_stoppingThread = Thread.currentThread();
                 block = false;
             }
             else
             {
                 if ( Thread.currentThread() == m_stoppingThread )
                 {
                     throw new IllegalStateException(
                         "WrapperManager.stop() can not be called recursively." );
                 }
                 
                 if ( Thread.currentThread() == m_hook )
                 {
                     // The hook should be allowed to fall through.
                     return;
                 }
                 block = true;
             }
         }
         
         if ( block )
         {
             if ( m_debug )
             {
                 m_out.println( "Thread, " + Thread.currentThread().getName()
                     + ", waiting for the JVM to exit." );
             }
             
             // This thread needs to be put into an infinite loop until the JVM exits.
             //  This thread can not be allowed to return to the caller, but another
             //  thread is already responsible for shutting down the JVM, so this
             //  one can do nothing but wait.
             while( true )
             {
                 try
                 {
                     Thread.sleep( 100 );
                 }
                 catch ( InterruptedException e )
                 {
                 }
             }
         }
         
         if ( m_debug )
         {
             m_out.println( "Thread, " + Thread.currentThread().getName()
                 + ", handling the shutdown process." );
         }
         
         // Only stop the listener if the app has been started.
         int code = exitCode;
         if ( m_started )
         {
             // Set the thread priority back to normal so that any spawned threads
             //	will use the normal priority
             int oldPriority = Thread.currentThread().getPriority();
             Thread.currentThread().setPriority( Thread.NORM_PRIORITY );
             
             if ( m_debug )
             {
                 m_out.println( "calling listener.stop()" );
             }
             if ( m_listener != null )
             {
                 // This is user code, so don't trust it.
                 try
                 {
                     code = m_listener.stop( code );
                 }
                 catch ( Throwable t )
                 {
                     m_out.println( "Error in WrapperListener.stop callback.  " + t );
                     t.printStackTrace();
                 }
             }
             if ( m_debug )
             {
                 m_out.println( "returned from listener.stop()" );
             }
             
             // Crank the priority back up.
             Thread.currentThread().setPriority( oldPriority );
         }
 
         shutdownJVM( code );
     }
     
     private static void signalStarted()
     {
         sendCommand( WRAPPER_MSG_STARTED, "" );
         m_started = true;
     }
     
     /**
      * Called by the native code when a control event is trapped by native code.
      * Can have the values: WRAPPER_CTRL_C_EVENT, WRAPPER_CTRL_CLOSE_EVENT, 
      *    WRAPPER_CTRL_LOGOFF_EVENT, or WRAPPER_CTRL_SHUTDOWN_EVENT
      * Calls 
      */
     private static void controlEvent( int event )
     {
         String eventName;
         boolean ignore;
         switch( event )
         {
         case WRAPPER_CTRL_C_EVENT:
             eventName = "WRAPPER_CTRL_C_EVENT";
             ignore = m_ignoreSignals;
             break;
         case WRAPPER_CTRL_CLOSE_EVENT:
             eventName = "WRAPPER_CTRL_CLOSE_EVENT";
             ignore = m_ignoreSignals;
             break;
         case WRAPPER_CTRL_LOGOFF_EVENT:
             eventName = "WRAPPER_CTRL_LOGOFF_EVENT";
             ignore = false;
             break;
         case WRAPPER_CTRL_SHUTDOWN_EVENT:
             eventName = "WRAPPER_CTRL_SHUTDOWN_EVENT";
             ignore = false;
             break;
         default:
             eventName = "Unexpected event: " + event;
             ignore = false;
             break;
         }
         
         if ( ignore )
         {
             if ( m_debug )
             {
                 m_out.println( "Ignoring control event(" + eventName + ")" );
             }
         }
         else
         {
             if ( m_debug )
             {
                 m_out.println( "Processing control event(" + eventName + ")" );
             }
             
             // This is user code, so don't trust it.
             if ( m_listener != null )
             {
                 try
                 {
                     m_listener.controlEvent( event );
                 }
                 catch ( Throwable t )
                 {
                     m_out.println( "Error in WrapperListener.controlEvent callback.  " + t );
                     t.printStackTrace();
                 }
             }
             else
             {
                 // A listener was never registered.  Always respond by exiting.
                 stop( 0 );
             }
         }
     }
 
     private static synchronized Socket openSocket()
     {
         if ( m_debug )
         {
             m_out.println( "Open socket to wrapper..." );
         }
 
         InetAddress iNetAddress;
         try
         {
             iNetAddress = InetAddress.getByName( "127.0.0.1" );
         }
         catch ( UnknownHostException e )
         {
             // This is pretty fatal.
             m_out.println( e );
             stop( 1 );
             return null; //please the compiler
         }
         
         try
         {
             m_socket = new Socket( iNetAddress, m_port );
             if ( m_debug )
             {
                 m_out.println( "Opened Socket" );
             }
         }
         catch ( BindException e )
         {
             m_out.println( "Failed to bind to the Wrapper port." );
             m_out.println( e );
             // This is fatal because the port was bad.
             m_out.println( "Exiting JVM..." );
             stopImmediate( 1 );
         }
         catch ( ConnectException e )
         {
             m_out.println( "Failed to connect to the Wrapper." );
             m_out.println( e );
             // This is fatal because there is nobody listening.
             m_out.println( "Exiting JVM..." );
             stopImmediate( 1 );
         }
         catch ( IOException e )
         {
             m_out.println( e );
             m_socket = null;
             return null;
         }
         try
         {
             // Turn on the TCP_NODELAY flag.  This is very important for speed!!
             m_socket.setTcpNoDelay( true );
             
             // Set the SO_TIMEOUT for the socket (max block time)
             if ( m_soTimeout > 0 )
             {
                 m_socket.setSoTimeout( m_soTimeout );
             }
         }
         catch ( IOException e )
         {
             m_out.println( e );
         }
         
         // Send the key back to the wrapper so that the wrapper can feel safe
         //  that it is talking to the correct JVM
         sendCommand( WRAPPER_MSG_KEY, m_key );
             
         return m_socket;
     }
     
     private static synchronized void closeSocket()
     {
         if ( m_socket != null )
         {
             if ( m_debug )
             {
                 m_out.println( "Closing socket." );
             }
             
             try
             {
                 m_socket.close();
             }
             catch ( IOException e )
             {
             }
             finally
             {
                 m_socket = null;
             }
         }
     }
     
     private static String getPacketCodeName(byte code)
     {
         String name;
     
         switch (code)
         {
         case WRAPPER_MSG_START:
             name ="START";
             break;
     
         case WRAPPER_MSG_STOP:
             name ="STOP";
             break;
     
         case WRAPPER_MSG_RESTART:
             name ="RESTART";
             break;
     
         case WRAPPER_MSG_PING:
             name ="PING";
             break;
     
         case WRAPPER_MSG_STOP_PENDING:
             name ="STOP_PENDING";
             break;
     
         case WRAPPER_MSG_START_PENDING:
             name ="START_PENDING";
             break;
     
         case WRAPPER_MSG_STARTED:
             name ="STARTED";
             break;
     
         case WRAPPER_MSG_STOPPED:
             name ="STOPPED";
             break;
     
         case WRAPPER_MSG_KEY:
             name ="KEY";
             break;
     
         case WRAPPER_MSG_BADKEY:
             name ="BADKEY";
             break;
     
         case WRAPPER_MSG_LOW_LOG_LEVEL:
             name ="LOW_LOG_LEVEL";
             break;
     
         case WRAPPER_MSG_PING_TIMEOUT:
             name ="PING_TIMEOUT";
             break;
     
         case WRAPPER_MSG_LOG + WRAPPER_LOG_LEVEL_DEBUG:
             name ="LOG(DEBUG)";
             break;
     
         case WRAPPER_MSG_LOG + WRAPPER_LOG_LEVEL_INFO:
             name ="LOG(INFO)";
             break;
     
         case WRAPPER_MSG_LOG + WRAPPER_LOG_LEVEL_STATUS:
             name ="LOG(STATUS)";
             break;
     
         case WRAPPER_MSG_LOG + WRAPPER_LOG_LEVEL_WARN:
             name ="LOG(WARN)";
             break;
     
         case WRAPPER_MSG_LOG + WRAPPER_LOG_LEVEL_ERROR:
             name ="LOG(ERROR)";
             break;
     
         case WRAPPER_MSG_LOG + WRAPPER_LOG_LEVEL_FATAL:
             name ="LOG(FATAL)";
             break;
     
         default:
             name = "UNKNOWN(" + code + ")";
             break;
         }
         return name;
     }
     
     private static synchronized void sendCommand( byte code, String message )
     {
         if ( m_debug )
         {
             m_out.println( "Send a packet " + getPacketCodeName( code ) + " : " + message );
         }
         if ( m_appearHung )
         {
             // The WrapperManager is attempting to make the JVM appear hung, so do nothing
         }
         else
         {
             // Make a copy of the reference to make this more thread safe.
             Socket socket = m_socket;
             if ( socket == null && isControlledByNativeWrapper() && ( !m_stopping ) )
             {
                 // The socket is not currently open, try opening it.
                 socket = openSocket();
             }
             
             if ( ( code == WRAPPER_MSG_START_PENDING ) || ( code == WRAPPER_MSG_STARTED ) )
             {
                 // Set the last ping time so that the startup process does not time out
                 //  thinking that the JVM has not received a Ping for too long.
                 m_lastPingTicks = getTicks();
             }
             
             // If the socket is open, then send the command, otherwise just throw it away.
             if ( socket != null )
             {
                 try
                 {
                     // It is possible that a logged message is quite large.  Expand the size
                     // of the command buffer if necessary so that it can be included.  This
                     //  means that the command buffer will be the size of the largest message.
                     byte[] messageBytes = message.getBytes();
                     if ( m_commandBuffer.length < messageBytes.length + 2 )
                     {
                         m_commandBuffer = new byte[messageBytes.length + 2];
                     }
                     
                     // Writing the bytes one by one was sometimes causing the first byte to be lost.
                     // Try to work around this problem by creating a buffer and sending the whole lot
                     // at once.
                     m_commandBuffer[0] = code;
                     System.arraycopy( messageBytes, 0, m_commandBuffer, 1, messageBytes.length );
                     int len = messageBytes.length + 2;
                     m_commandBuffer[len - 1] = 0;
                     
                     OutputStream os = socket.getOutputStream();
                     os.write( m_commandBuffer, 0, len );
                     os.flush();
                 }
                 catch ( IOException e )
                 {
                     m_out.println( e );
                     e.printStackTrace();
                     closeSocket();
                 }
             }
         }
     }
     
     /**
      * Loop reading packets from the native side of the Wrapper until the 
      *  connection is closed or the WrapperManager class is disposed.
      *  Each packet consists of a packet code followed by a null terminated
      *  string up to 256 characters in length.  If the entire packet has not
      *  yet been received, then it must not be read until the complete packet
      *  has arived.
      */
     private static void handleSocket()
     {
         byte[] buffer = new byte[256];
         try
         {
             if ( m_debug )
             {
                 m_out.println( "handleSocket(" + m_socket + ")" );
             }
             DataInputStream is = new DataInputStream( m_socket.getInputStream() );
             while ( !m_disposed )
             {
                 try
                 {
                     // A Packet code must exist.
                     byte code = is.readByte();
                     
                     // Always read from the buffer until a null '\0' is encountered.  But only
                     //  place the first 256 characters into the buffer.
                     byte b;
                     int i = 0;
                     do
                     {
                         b = is.readByte();
                         if ( ( b != 0 ) && ( i < 256 ) )
                         {
                             buffer[i] = b;
                             i++;
                         }
                     }
                     while ( b != 0 );
                     
                     String msg = new String( buffer, 0, i );
                     
                     if ( m_appearHung )
                     {
                         // The WrapperManager is attempting to make the JVM appear hung,
                         //   so ignore all incoming requests
                     }
                     else
                     {
                         if ( m_debug )
                         {
                             m_out.println( "Received a packet " + getPacketCodeName( code )
                                 + " : " + msg );
                         }
                         
                         // Ok, we got a packet.  Do something with it.
                         switch( code )
                         {
                         case WRAPPER_MSG_START:
                             startInner();
                             break;
                             
                         case WRAPPER_MSG_STOP:
                             // Don't do anything if we are already stopping
                             if ( !m_stopping )
                             {
                                 stopInner( 0 );
                                 // Should never get back here.
                             }
                             break;
                             
                         case WRAPPER_MSG_PING:
                             m_lastPingTicks = getTicks();
                             sendCommand( WRAPPER_MSG_PING, "ok" );	
                             break;
                             
                         case WRAPPER_MSG_BADKEY:
                             // The key sent to the wrapper was incorrect.  We need to shutdown.
                             m_out.println(
                                 "Authorization key rejected by Wrapper.  Exiting JVM." );
                             closeSocket();
                             stopInner( 1 );
                             break;
                             
                         case WRAPPER_MSG_LOW_LOG_LEVEL:
                             try
                             {
                                 m_lowLogLevel = Integer.parseInt( msg );
                                 if ( m_debug )
                                 {
                                     m_out.println( "Wrapper Manager: LowLogLevel from Wrapper "
                                         + "is " + m_lowLogLevel );
                                 }
                             }
                             catch ( NumberFormatException e )
                             {
                                 m_out.println( "Encountered an Illegal LowLogLevel from the "
                                     + "Wrapper: " + msg );
                             }
                             break;
                             
                         case WRAPPER_MSG_PING_TIMEOUT:
                             try
                             {
                                 m_pingTimeout = Integer.parseInt( msg ) * 1000;
                                 if ( m_debug )
                                 {
                                     m_out.println( "Wrapper Manager: PingTimeout from Wrapper "
                                         + "is " + m_pingTimeout );
                                 }
                             }
                             catch ( NumberFormatException e )
                             {
                                 m_out.println( "Encountered an Illegal PingTimeout from the "
                                     + "Wrapper: " + msg );
                             }
                             
                             // Make sure that the so timeout is longer than the ping timeout
                            if ( m_pingTimeout <= 0 )
                            {
                                m_socket.setSoTimeout( 0 );
                            }
                            else if ( m_soTimeout < m_pingTimeout )
                             {
                                 m_socket.setSoTimeout( m_pingTimeout );
                             }
                             
                             break;
                             
                         default:
                             // Ignore unknown messages
                             m_out.println( "Wrapper code received an unknown packet type: "
                                 + code );
                             break;
                         }
                     }
                 }
                 catch ( InterruptedIOException e )
                 {
                     int nowTicks = getTicks();
                     
                     // Unless the JVM is shutting dowm we want to show warning messages and maybe exit.
                     if ( ( m_started ) && ( !m_stopping ) )
                     {
                         if ( m_debug )
                         {
                             m_out.println( "Read Timed out. (Last Ping was "
                                 + getTickAge( m_lastPingTicks, nowTicks ) + " milliseconds ago)" );
                         }
                         
                         if ( !m_appearHung )
                         {
                             long lastPingAge = getTickAge( m_lastPingTicks, nowTicks );
                             long eventRunnerAge = getTickAge( m_eventRunnerTicks, nowTicks );
                             
                             // We may have timed out because the system was extremely busy or
                             //  suspended.  Only restart due to a lack of ping events if the
                             //  event thread has been running.
                             if ( eventRunnerAge < 10000 )
                             {
                                 // Only perform ping timeout checks if ping timeouts are enabled.
                                 if ( m_pingTimeout > 0 )
                                 {
                                     // How long has it been since we received the last ping
                                     //  from the Wrapper?
                                     if ( lastPingAge > m_pingTimeout + 90000 )
                                     {
                                         // It has been more than the ping timeout + 90 seconds,
                                         //  so just give up and kill the JVM
                                         m_out.println(
                                             "Wrapper Manager: JVM did not exit.  Give up." );
                                         System.exit(1);
                                     }
                                     else if ( lastPingAge > m_pingTimeout )
                                     {
                                         // It has been more than the ping timeout since the
                                         //  JVM was last pinged.  Ask to be stopped (and restarted).
                                         m_out.println(
                                             "Wrapper Manager: The Wrapper code did not ping the "
                                             + "JVM for " + (lastPingAge / 1000) + " seconds.  "
                                             + "Quit and let the Wrapper resynch.");
                                         
                                         // Don't do anything if we are already stopping
                                         if ( !m_stopping )
                                         {
                                             stopInner( 1 );
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 
                 // Check to see if all non-daemon threads have exited.
                 if ( m_started )
                 {
                     checkThreads();
                 }
             }
             return;
 
         }
         catch ( SocketException e )
         {
             if ( m_debug )
             {
                 if ( m_socket == null )
                 {
                     // This error happens if the socket is closed while reading:
                     // java.net.SocketException: Descriptor not a socket: JVM_recv in socket
                     //                           input stream read
                 }
                 else
                 {
                     m_out.println( "Closed socket: " + e );
                 }
             }
             return;
         }
         catch ( IOException e )
         {
             // This means that the connection was closed.  Allow this to return.
             //m_out.println( e );
             //e.printStackTrace();
             return;
         }
     }
     
     /**
      * Returns a count of all non-daemon threads in the JVM, starting with the top
      *  thread group.
      *
      * @return Number of non-daemon threads.
      */
     protected static int getNonDaemonThreadCount()
     {
         // Locate the top thread group.
         ThreadGroup topGroup = Thread.currentThread().getThreadGroup();
         while ( topGroup.getParent() != null )
         {
             topGroup = topGroup.getParent();
         }
         
         // Get a list of all threads.  Use an array that is twice the total number of
         //  threads as the number of running threads may be increasing as this runs.
         Thread[] threads = new Thread[topGroup.activeCount() * 2];
         topGroup.enumerate( threads, true );
         
         // Only count any non daemon threads which are 
         //  still alive other than this thread.
         int liveCount = 0;
         for ( int i = 0; i < threads.length; i++ )
         {
             /*
             if ( threads[i] != null )
             {
                 m_out.println( "Check " + threads[i].getName() + " daemon="
                 + threads[i].isDaemon() + " alive=" + threads[i].isAlive() );
             }
             */
             if ( ( threads[i] != null ) && ( threads[i].isAlive() && ( !threads[i].isDaemon() ) ) )
             {
                 // Do not count this thread or the wrapper connection thread
                 if ( ( Thread.currentThread() != threads[i] ) && ( threads[i] != m_commRunner ) )
                 {
                     // Non-Daemon living thread
                     liveCount++;
                     //m_out.println( "  -> Non-Daemon" );
                 }
             }
         }
         //m_out.println( "  => liveCount = " + liveCount );
         
         return liveCount;
     }
     
     /**
      * With a normal Java application, the JVM will exit when all non-daemon
      *  threads have completed.  This does not work correctly with the wrapper
      *  because the connection thread is not a daemon.  It would also cause
      *  problems because the wrapper would not know whether the exit had been
      *  intentional or not.  This method takes care of making sure that the
      *  JVM exits when it is supposed to and makes sure that the Wrapper is
      *  propperly informed.
      */
     private static void checkThreads()
     {
         int liveCount = getNonDaemonThreadCount();
         
         // Depending on the JVM, there will always be one (or zero) non-daemon thread alive.
         //  This thread is either the main thread which has not yet completed, or a thread
         //  launched by java when the main thread completes whose job is to wait around for
         //  all other non-daemon threads to complete.  We are overriding that thread here.
         if ( liveCount <= m_systemThreadCount )
         {
             if ( m_debug )
             {
                 m_out.println( "All non-daemon threads have stopped.  Exiting." );
             }
             
             // Exit normally
             WrapperManager.stop( 0 );
             // Will not get here.
         }
         else
         {
             // There are daemons running, let the JVM continue to run.
         }
     }
     
     private static void startRunner()
     {
         if ( isControlledByNativeWrapper() )
         {
             if ( m_commRunner == null )
             {
                 // Create and launch a new thread to manage this connection
                 m_commRunner = new Thread( m_instance, WRAPPER_CONNECTION_THREAD_NAME );
                 // This thread can not be a daemon or the JVM will quit immediately
                 m_commRunner.start();
             }
         }
     }
     
     /*---------------------------------------------------------------
      * Runnable Methods
      *-------------------------------------------------------------*/
     public void run()
     {
         // Make sure that no other threads call this method.
         if ( Thread.currentThread() != m_commRunner )
         {
             throw new IllegalStateException(
                 "Only the comm runner thread is allowed to call this method." );
         }
         
         m_commRunnerStarted = true;
         
         // This thread needs to have a very high priority so that it never
         //	gets put behind other threads.
         Thread.currentThread().setPriority( Thread.MAX_PRIORITY );
         
         // Initialize the last ping tick count.
         m_lastPingTicks = getTicks();
         
         boolean gotPortOnce = false;
         while ( !m_disposed )
         {
             try
             {
                 try
                 {
                     openSocket();
                     if ( m_socket != null )
                     {
                         handleSocket();
                     }
                     else
                     {
                         // Failed, so wait for just a moment
                         try
                         {
                             Thread.sleep( 10 );
                         }
                         catch ( InterruptedException e )
                         {
                         }
                     }
                 }
                 finally
                 {
                     // Always close the socket here.
                     closeSocket();
                 }
             }
             catch ( ThreadDeath td )
             {
                 m_out.println( m_warning.format( "SERVER_DAEMON_KILLED" ) );
             }
             catch ( Throwable t )
             {
                 if ( !m_shuttingDown )
                 {
                     // Show a stack trace here because this is fairly critical
                     m_out.println( m_error.format( "SERVER_DAEMON_DIED" ) );
                     t.printStackTrace();
                 }
             }
         }
         if ( m_debug )
         {
             m_out.println( m_info.format( "SERVER_DAEMON_SHUT_DOWN" ) );
         }
     }
     
     /*---------------------------------------------------------------
      * Inner Classes
      *-------------------------------------------------------------*/
     /**
      * When the JVM is being controlled by the Wrapper, stdin can not be used
      *  as it is undefined.  This class makes it possible to provide the user
      *  application with a descriptive error message if System.in is accessed.
      */
     private static class WrapperInputStream
         extends InputStream
     {
         /**
          * This method will always throw an IOException as the read method is
          *  not valid.
          */
         public int read()
             throws IOException
         {
             throw new IOException( "System.in can not be used when the JVM is being "
                 + "controlled by the Java Service Manager." );
         }
     }
 }
 
