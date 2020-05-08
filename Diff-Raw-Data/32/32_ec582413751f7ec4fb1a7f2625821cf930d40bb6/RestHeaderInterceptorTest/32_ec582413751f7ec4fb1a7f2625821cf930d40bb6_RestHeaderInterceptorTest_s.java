 package jobengine.app.request;
 

 import org.junit.Test;
 
 import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class RestHeaderInterceptorTest {
 
     @Test
    public void when_getting_media_type_it_should_extract_version() {
        Pattern p = Pattern.compile("vnd\\.jobrapido\\.(.*)\\+(.*)");
        Matcher m = p.matcher("vnd.jobrapido.alpha+json");

        assertTrue(m.matches());

        assertThat(m.group(1), is("alpha"));

     }
 
     @Test
    public void when_getting_media_type_it_should_extract_json() {
        String version = "vnd.jobrapido.alpha+json".replaceAll("vnd\\.jobrapido\\.(.*)\\+(.*)", "$2");

        assertThat(version, is("json"));
     }
 
 
 }
