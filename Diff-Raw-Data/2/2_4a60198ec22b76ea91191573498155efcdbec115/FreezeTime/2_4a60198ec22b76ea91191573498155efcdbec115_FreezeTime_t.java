 package eu.kratochvil.util;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeUtils;
 
 /**
 * Once upon a time Mark Needham <a href="http://www.markhneedham.com/blog/2008/09/24/testing-with-joda-time/">wrote</a>
  * about freezing Joda Time. Mark gives all the important details for freezing time (which is often helpful for
  * testing), but I came up with some additional code that I like to add on top of his example.
  * <p/>
  * Two things bother me about Mark's example. First of all, I always like the last line of
  * my test to be the assertion. It's not a law, but it is a guideline I like to follow.
  * Secondly, I don't like having to remember that I need to reset the time back to following
  * the system clock.
  * <p/>
  * I came up with the following idea. It's definitely a poor man's closure, but it does the
  * job for me
  * <p/>
  * Using this code I can keep all assertions as close to the end of the test method as possible,
  * and it's not possible to forget to reset the time back to the system clock.
  * <p/>
  * <code><pre>
  * &#64;Test
  * public void shouldFreezeTime() {
  *    Freeze.timeAt("2008-09-04").thawAfter(new Snippet() {{
  *       assertEquals(new DateTime(2008, 9, 4, 1, 0, 0, 0), new DateTime());
  *    }});
  * }
  * </pre></code>
  *
  * @author Jiri Kratochvil <jiri.kratochvil@topmonks.com>
  */
 public class FreezeTime {
 
 	public static FreezeTime timeAt(String dateTimeString) {
 		DateTimeUtils.setCurrentMillisFixed(DateTime.parse(dateTimeString).getMillis());
 		return new FreezeTime();
 	}
 
 	public void thawAfter(Snippet snippet) {
 		DateTimeUtils.setCurrentMillisSystem();
 	}
 }
