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
 package org.novelang.testing.junit;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
 import org.junit.rules.MethodRule;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.Statement;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.testing.DirectoryFixture;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * @author Laurent Caillette
  */
 public class MethodSupport implements MethodRule, Supplier< File > {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( MethodSupport.class ) ;
 
   private String testName = null ;
 
   private DirectoryFixture directoryFixture = null ;
 
   /**
    * Synchronize every access to object's fields on this object.
    */
   private final Object stateLock = new Object() ;
 
   private final Object executionLock ;
 
   public MethodSupport() {
     this.executionLock = null ;
   }
 
   public MethodSupport( final Object executionLock ) {
     this.executionLock = checkNotNull( executionLock ) ;
   }
 
   @Override
   public Statement apply(
       final Statement base,
       final FrameworkMethod frameworkMethod,
       final Object target
   ) {
     synchronized( stateLock ) {
       testName = frameworkMethod.getName() ;
       return new WrappedStatement( base, executionLock ) ;
     }
   }
 
   public final String getTestName() {
     synchronized( stateLock ) {
       Preconditions.checkState( testName != null ) ;
       return testName ;
     }
   }
 
   public final File getDirectory() {
     synchronized( stateLock ) {
       if( directoryFixture == null ) {
         directoryFixture = new DirectoryFixture( getTestName() );
       }
       try {
         return directoryFixture.getDirectory() ;
       } catch( IOException e ) {
         throw new RuntimeException( e ) ;
       }
     }
   }
 
   @Override
   public File get() {
     return getDirectory() ;
   }
 
   protected void beforeStatementEvaluation() throws Exception { }
 
 
   protected void afterStatementEvaluation() throws Exception { }
 
 
   private class WrappedStatement extends Statement {
     protected final Statement base ;
     private final Object lock ;
 
     public WrappedStatement( final Statement base, final Object lock ) {
       this.base = base ;
       this.lock = lock ;
     }
 
     @Override
     public void evaluate() throws Throwable {
       if( lock == null ) {
         doEvaluate() ;
       } else {
         synchronized( lock ) {
           doEvaluate() ;
         }
       }
     }
 
     private void doEvaluate() throws Throwable {
       LOGGER.info( "*** Evaluating ", getTestName(), "... ***" );
       try {
         beforeStatementEvaluation() ;
         base.evaluate();
       } catch( Throwable throwable ) {
         LOGGER.error( throwable, "Test failed." ) ;
        Throwables.propagateIfPossible( throwable ) ;
       } finally {
         afterStatementEvaluation() ;
       }
       LOGGER.info( "*** Done with ", getTestName(), ". ***" );
     }
   }
 
 
 }
