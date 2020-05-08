 import org.junit.Test;
 
import static org.hamcrest.CoreMatchers.nullValue;

 import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
 
 public class GuruTest {
 

    private Guru guru = new Guru();

     @Test
    public void guru_should_say_hello() {
        assertThat(guru.sayHello(), nullValue());
     }
 }
