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
 package org.novelang.logger;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 /**
  * A simplistic implementation.
  *
  * @author Laurent Caillette
  */
 public final class ConsoleLogger extends AbstractLogger {
 
   private ConsoleLogger() { }
 
   public static final ConsoleLogger INSTANCE = new ConsoleLogger() ;
 
   @Override
   protected void log( final Level level, final String message, final Throwable throwable ) {
     final String stackTrace ;
     if( throwable == null ) {
       stackTrace = "" ;
     } else {
       final StringWriter stringWriter = new StringWriter() ;
       throwable.printStackTrace( new PrintWriter( stringWriter ) ) ;
       stackTrace = stringWriter.toString() ;
     }
    System.err.println( "[" + level + "] " + ( message == null ? "" : message ) + stackTrace ) ;
   }
 
   @Override
   public String getName() {
     return getClass().getSimpleName() ;
   }
 
   @Override
   public boolean isTraceEnabled() {
     return false ;
   }
 
   @Override
   public boolean isDebugEnabled() {
     return false ;
   }
 
   @Override
   public boolean isInfoEnabled() {
     return true ;
   }
 }
