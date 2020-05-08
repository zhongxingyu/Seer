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
 // Revision 1.4  2004/03/27 14:39:20  mortenson
 // Add actions for the stopImmediate method.
 //
 // Revision 1.3  2004/01/16 04:41:55  mortenson
 // The license was revised for this version to include a copyright omission.
 // This change is to be retroactively applied to all versions of the Java
 // Service Wrapper starting with version 3.0.0.
 //
 // Revision 1.2  2004/01/15 09:50:30  mortenson
 // Fix some problems where the Wrapper was not handling exit codes correctly.
 //
 // Revision 1.1  2004/01/10 15:44:15  mortenson
 // Rework the test wrapper app so there is less code duplication.
 //
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.tanukisoftware.wrapper.WrapperManager;
 
 /**
  * @author Leif Mortenson <leif@tanukisoftware.com>
  * @version $Revision$
  */
 public abstract class AbstractActionApp {
     private DeadlockPrintStream m_out;
     private DeadlockPrintStream m_err;
     
     private Thread m_userRunner;
     private boolean m_groups;
     
     /**************************************************************************
      * Constructors
      *************************************************************************/
     protected AbstractActionApp() {
     }
     
     /**************************************************************************
      * Methods
      *************************************************************************/
     protected void prepareSystemOutErr()
     {
         m_out = new DeadlockPrintStream( System.out );
         System.setOut( m_out );
         m_err = new DeadlockPrintStream( System.err );
         System.setErr( m_err );
     }
     protected boolean doAction( String action )
     {
         if ( action.equals( "stop0" ) )
         {
             WrapperManager.stop( 0 );
             
         }
         else if ( action.equals( "stop1" ) )
         {
             WrapperManager.stop( 1 );
             
         }
         else if ( action.equals( "exit0" ) )
         {
             System.exit( 0 );
             
         }
         else if ( action.equals( "exit1" ) )
         {
             System.exit( 1 );
             
         }
         else if ( action.equals( "stopimmediate0" ) )
         {
             WrapperManager.stopImmediate( 0 );
         }
         else if ( action.equals( "stopimmediate1" ) )
         {
             WrapperManager.stopImmediate( 1 );
         }
         else if ( action.equals( "halt" ) )
         {
             // Execute runtime.halt(0) using reflection so this class will
             //  compile on 1.2.x versions of Java.
             Method haltMethod;
             try
             {
                 haltMethod = Runtime.class.getMethod( "halt", new Class[] { Integer.TYPE } );
             }
             catch ( NoSuchMethodException e )
             {
                 System.out.println( "halt not supported by current JVM." );
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
                     System.out.println( "Unable to call runitme.halt: " + e.getMessage() );
                 }
                 catch ( InvocationTargetException e )
                 {
                     System.out.println( "Unable to call runitme.halt: " + e.getMessage() );
                 }
             }
         }
         else if ( action.equals( "restart" ) )
         {
             WrapperManager.restart();
             
         }
         else if ( action.equals( "access_violation" ) )
         {
             WrapperManager.accessViolation();
             
         }
        else if ( action.equals( "native_access_violation" ) )
         {
             WrapperManager.accessViolationNative();
             
         }
         else if ( action.equals( "appear_hung" ) )
         {
             WrapperManager.appearHung();
             
         }
         else if ( action.equals( "dump" ) )
         {
             WrapperManager.requestThreadDump();
             
         }
         else if ( action.equals( "deadlock_out" ) )
         {
             System.out.println( "Deadlocking System.out and System.err ..." );
             m_out.setDeadlock( true );
             m_err.setDeadlock( true );
             
         }
         else if ( action.equals( "users" ) || action.equals( "groups" ) )
         {
             System.out.println( "Begin polling the current and interactive users." );
             m_groups = action.equals( "groups" );
             if ( m_userRunner == null )
             {
                 m_userRunner = new Thread()
                 {
                     public void run()
                     {
                         while ( true )
                         {
                             System.out.println( "The current user is: "
                                 + WrapperManager.getUser( m_groups ) );
                             System.out.println( "The current interactive user is: "
                                 + WrapperManager.getInteractiveUser( m_groups ) );
                             try
                             {
                                 Thread.sleep( 10000 );
                             }
                             catch ( InterruptedException e )
                             {
                             }
                             System.gc();
                         }
                     }
                 };
                 m_userRunner.setDaemon( true );
                 m_userRunner.start();
             }
         }
         else
         {
             // Unknown action
             return false;
         
         }
         
         return true;
     }
 }
 
