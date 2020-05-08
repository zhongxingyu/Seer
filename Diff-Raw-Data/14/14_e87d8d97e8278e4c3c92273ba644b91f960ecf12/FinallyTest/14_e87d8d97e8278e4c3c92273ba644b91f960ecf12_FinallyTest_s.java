 package com.smalaca.trycatchfinally;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class FinallyTest {
 
     final private Finally thrower = new Finally();
 
     @Test
     public void notThrownExceptionWithReturnInFinally() {
         assertSame(0, thrower.giveMeSomething(false));
     }
     
     @Test
     public void thrownExceptionWithReturnInFinally() {
         assertSame(0, thrower.giveMeSomething(true));
     }
 
     @Test
     public void noExceptionThrown() {
         assertSame(13, thrower.manageException(false));
         assertSame(true, thrower.wasFinallyReached());
     }
     
     @Test
     public void handledException() {
         assertSame(-1, thrower.manageException(true));
         assertSame(true, thrower.wasFinallyReached());
     }
 
     @Test
     public void finallyWasReachedWithNoException() throws Exception {
         thrower.letMeToPassException(false);
         
         assertSame(true, thrower.wasFinallyReached());
     }
 
    @Test(expected = Exception.class)
    public void finallyWasReachedAndExceptionWasPassed() throws Exception {
        thrower.letMeToPassException(true);
        
        assertSame(true, thrower.wasFinallyReached());
     }
 }
