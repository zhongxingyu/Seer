 package jobengine.app.request;
 
 import org.junit.Test;
 
 import java.util.regex.Matcher;
 
import static jobengine.app.request.Versions.versionMatcherOf;
import static junit.framework.Assert.assertFalse;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class RestHeaderInterceptorTest {
 
     @Test
    public void random_mdia_type_patterns_don_t_match() {
        assertFalse(versionMatcherOf("cippalippa").matches());
     }
 
     @Test
    public void when_recognise_the_mdia_type_pattern_it_matches() {
        assertTrue(versionMatcherOf("vnd.jobrapido.alpha+json").matches());
     }
 
    @Test
    public void when_getting_media_type_it_should_extract_version() {
        Matcher matcher = versionMatcherOf("vnd.jobrapido.alpha+json");
        matcher.matches();
 
        assertThat(matcher.group(1), is("alpha"));
    }
 }
