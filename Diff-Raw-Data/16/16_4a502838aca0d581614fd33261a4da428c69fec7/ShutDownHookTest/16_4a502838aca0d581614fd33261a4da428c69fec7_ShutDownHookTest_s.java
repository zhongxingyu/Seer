 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.commons;
 
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 /**
  * Test cases for {@link ShutDownHook}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class ShutDownHookTest {
 
     private final ShutDownHook sut = new ShutDownHook();
 
     @Test public void registerAndRunCalbacksWhichThrowsException() {
         final Runnable callback1 = mock(Runnable.class);
         final Runnable callback2 = mock(Runnable.class);
         doThrow(new RuntimeException()).when(callback2).run();
         final Runnable callback3 = mock(Runnable.class);
         sut.register(callback1)
            .register(callback2)
            .register(callback3);
 
         sut.run();
         verify(callback1, times(1)).run();
         verify(callback2, times(1)).run();
         verify(callback3, times(1)).run();
     }
 
    @Test public void registerAndRunCalbacks() {
         final Runnable callback1 = mock(Runnable.class);
         final Runnable callback2 = mock(Runnable.class);
         final Runnable callback3 = mock(Runnable.class);
         sut.register(callback1)
            .register(callback2)
            .register(callback3);
 
         sut.run();
         verify(callback1, times(1)).run();
         verify(callback2, times(1)).run();
         verify(callback3, times(1)).run();
     }
 
 }
