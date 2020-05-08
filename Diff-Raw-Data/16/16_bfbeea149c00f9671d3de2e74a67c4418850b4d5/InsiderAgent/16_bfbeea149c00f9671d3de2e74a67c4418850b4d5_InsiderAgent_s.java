 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang.outfit.shell.insider;
 
 
 import java.lang.management.ManagementFactory;
 import javax.management.MBeanServer;
 
 /**
  * A Java agent that works in 2 ways: install a
  * Installs the {@link Insider JMX bean}
  * and executes immediately for a {@link Runtime#halt(int)}.
  * .
  * <p>
  * The JMX-based approach respects the Java Language Specification as it doesn't rely on
  * proprietary API. It is also faster.
  * <p>
  * The hot load of a Java agent relies on one of Sun's proprietary API. It is also looks slower.
  * But it's a full Java, multiplatform mean to shutdown a JVM.
  *
  *
  * @author Laurent Caillette
  */
 @SuppressWarnings( { "UnusedDeclaration" } )
 public class InsiderAgent
 {
 
     private InsiderAgent()
     {
     }
 
     /**
      * Called when launching a JVM with {@code -javaagent} option.
      * See <a href="http://java.sun.com/javase/6/docs/api/java/lang/instrument/package-summary.html">Java Instrumentation specification</a>.
      */
     public static void premain( final String arguments )
     {
         final MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer() ;
         final LocalInsider managedBean ;
 
         try {
           if( arguments != null
               && arguments.startsWith( Insider.MAXIMUM_HEARTBEATDELAY_PARAMETERNAME )
           ) out : {
             final String delayAsString = arguments.substring(
                 Insider.MAXIMUM_HEARTBEATDELAY_PARAMETERNAME.length() ) ;
             final long delay;
             try {
               delay = Long.parseLong( delayAsString );
             } catch( NumberFormatException e ) {
               System.err.println( "Couldn't parse arguments '" + arguments + "', using defaults." ) ;
               managedBean = new LocalInsider() ;
               break out ;
             }
             managedBean = new LocalInsider( delay ) ;
           } else {
             managedBean = new LocalInsider() ;
           }
           beanServer.registerMBean( managedBean, Insider.NAME ) ;
         } catch( Exception e ) {
           // Checked exceptions prevent the JVM from loading the agent.
           throw new RuntimeException( e ) ;
         }
         System.out.println( "Loaded " + InsiderAgent.class.getName() + "." ) ;
     }
 
 
     /**
      * Called when hot-loaded through
      * <a href="http://java.sun.com/javase/6/docs/jdk/api/attach/spec/com/sun/tools/attach/VirtualMachine.html#loadAgent%28java.lang.String,%20java.lang.String%29">Attach API</a>.
      */
     public static void agentmain( final String args )
     {
         new Thread( new Runnable()
         {
             public void run()
             {
                 System.out.println( getClass().getName() + " halting JVM..." ) ;
                 Runtime.getRuntime().halt( 1 ) ;
             }
         } ).start() ;
     }
 }
