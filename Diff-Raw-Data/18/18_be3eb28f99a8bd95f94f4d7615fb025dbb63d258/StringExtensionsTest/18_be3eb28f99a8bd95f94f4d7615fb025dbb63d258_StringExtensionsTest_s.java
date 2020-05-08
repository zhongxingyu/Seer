 package extensions;
 
 import play.test.UnitTest;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 import static org.hamcrest.Matchers.*;
 import org.hamcrest.MatcherAssert;
 
 /**
  * @author neteller
  * @created: Jan 16, 2011
  */
 public class StringExtensionsTest extends UnitTest {
 
   @Test
   public void shouldFormatUrlCorrectly() {
     MatcherAssert.assertThat(
         StringExtensions.url("www.google.com"),
         equalTo("http://www.google.com")
     );
     MatcherAssert.assertThat(
         StringExtensions.url("http://www.google.com"),
         equalTo("http://www.google.com")
     );
     MatcherAssert.assertThat(
         StringExtensions.url("https://www.google.com"),
         equalTo("https://www.google.com")
     );
     MatcherAssert.assertThat(
         StringExtensions.url(null),
         equalTo("#")
     );
     MatcherAssert.assertThat(
         StringExtensions.url(""),
         equalTo("#")
     );
   }
 
 }
