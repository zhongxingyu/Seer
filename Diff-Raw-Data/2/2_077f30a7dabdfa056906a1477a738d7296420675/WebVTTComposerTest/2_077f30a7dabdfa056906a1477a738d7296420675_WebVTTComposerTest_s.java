 package cc.explain.server.subtitle.composer;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import antlr.RecognitionException;
 import cc.explain.server.subtitle.AbstractTest;
 import cc.explain.server.subtitle.Subtitle;
 import cc.explain.server.subtitle.SubtitleElement;
 import org.joda.time.LocalTime;
 import org.junit.Test;
 
 public class WebVTTComposerTest extends AbstractTest {
 
 	@Test
 	public void shouldComposeWebVVTForTestSubtitle() throws IOException, RecognitionException {
 		Subtitle subtitle = createSubtitleFromFile("/subtitle.srt");
 		
 		String result = new WebVTTComposer().compose(subtitle);
 		
 		assertEquals(read("/subtitle.vtt"), result);
 	}
 	
 	@Test
 	public void shouldConvertSubtitleElementToString() {
 		SubtitleElement element = new SubtitleElement();
 		element.setId(1);
 		element.setText("text");
 		element.setStart(new LocalTime(1,12,23,456));
 		element.setEnd(new LocalTime(2,23,45,678));
 
 		String result = new WebVTTComposer().convertSubtitleElementToString(element);
 		
		assertEquals("1\r\n01:12:23.456 --> 02:23:45.678\r\ntext\r\n\r\n", result);
 	}
 }
