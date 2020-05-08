 package controllers;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static play.test.Helpers.contentType;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import play.mvc.Content;
 
 /**
  * Tests the Application controller.
  * 
  * @author Tiago Garcia
  * @see http://github.com/tiagorg
  */
 public class ApplicationTest {
 
 	/**
 	 * Tests the template renderization.
 	 */
 	@Test
 	public void testTemplateRenderization() {
		List<String> stylesheetsList = new ArrayList<String>();
		stylesheetsList.add("test.css");
		
 		List<String> scriptsList = new ArrayList<String>();
 		scriptsList.add("test.js");
		
		Content html = views.html.index.render(stylesheetsList, scriptsList);
		
 		assertThat(contentType(html)).isEqualTo("text/html");
 		assertThat(html.body().contains("test.js")).isTrue();
		assertThat(html.body().contains("test.css")).isTrue();
 	}
 
 }
