 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class GreeterTest {
     @Test
     public void says_hello_to_the_world() {
         Greeter greeter = new Greeter();
        assertThat("", is(""));
     }
 }
