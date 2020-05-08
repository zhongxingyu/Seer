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
 
 import java.io.PrintStream;
 import static org.hamcrest.CoreMatchers.*;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test cases for {@link CapturingOutputStream}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class CapturingOutputStreamTest {
 
     private final CapturingOutputStream sut = new CapturingOutputStream();
     private PrintStream backup;
 
     @Before public void changeSystemOut() {
         backup = System.out;
         System.setOut(new PrintStream(sut));
     }
 
     @After public void restoreSystemOut() {
         System.setOut(backup);
     }
 
     @Test public void writeAndGetCapturedOutput() {
         assertThat("", is(sut.getCapturedOutput()));
         System.out.print("foo");
         assertThat("foo", is(sut.getCapturedOutput()));
         System.out.print("bar");
         assertThat("foobar", is(sut.getCapturedOutput()));
     }
 
     @Test public void getStringRepresentation() {
         final PrintStream stream = new PrintStream(sut);
         stream.print("foobar");
         assertThat("CapturingOutputStream{capturedOutput=foobar}", is(sut.toString()));
     }
 
     @Test public void testHashCode() {
         final PrintStream stream = new PrintStream(sut);
         stream.print("foobar");
        stream.close();
         final CapturingOutputStream other = new CapturingOutputStream();
         final PrintStream streamOther = new PrintStream(other);
         streamOther.print("foobar");
        streamOther.close();
         assertThat(other.hashCode(), is(sut.hashCode()));
     }
 
    @SuppressWarnings({"IncompatibleEquals", "ObjectEqualsNull"}) // I want to test this explicetly.
     @Test public void equals() {
         final PrintStream stream = new PrintStream(sut);
         stream.print("foobar");
        stream.close();
         final CapturingOutputStream other = new CapturingOutputStream();
         final PrintStream streamOther = new PrintStream(other);
         streamOther.print("foobar");
        streamOther.close();
         final CapturingOutputStream other2 = new CapturingOutputStream();
         final PrintStream streamOther2 = new PrintStream(other2);
         streamOther2.print("snafu");
        streamOther2.close();
 
        assertThat(sut.equals("foobar"), is(false)); // NOPMD sut is never null.
        assertThat(sut.equals(null), is(false)); // NOPMD Explicety check that null will return in false.
         assertThat(sut.equals(other), is(true));
         assertThat(sut.equals(other2), is(false));
         assertThat(other.equals(other2), is(false));
     }
 
 }
