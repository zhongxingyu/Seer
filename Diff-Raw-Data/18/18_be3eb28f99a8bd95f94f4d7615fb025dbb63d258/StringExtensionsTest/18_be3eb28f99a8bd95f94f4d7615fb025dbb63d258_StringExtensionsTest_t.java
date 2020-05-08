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
 
  @Test
  public void shouldAbbreviateWhenContentLessThanCharacters() {
    final String s = StringExtensions.abbreviate("<p>A SaaS app that makes it very easy to post your jobs to many relevant job sites (1-click posting, we do the submitting on the backend), and that makes it very easy to manage incoming applicants/resumes together with your team.</p>", 200);
    MatcherAssert.assertThat(s.length(), lessThanOrEqualTo(220));
  }

 }
