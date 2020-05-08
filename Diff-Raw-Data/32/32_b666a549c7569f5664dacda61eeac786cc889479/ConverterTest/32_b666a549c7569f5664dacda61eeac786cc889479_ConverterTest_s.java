 package fr.xebia.confluence2wordpress.core.converter;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
import java.io.File;
 import java.io.IOException;
 
import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.atlassian.confluence.core.ContentEntityObject;
 import com.atlassian.renderer.RenderContext;
 import com.atlassian.renderer.WikiStyleRenderer;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public class ConverterTest {
 	
 	@Mock
 	private WikiStyleRenderer wikiStyleRenderer;
 	
 	@Mock
 	private ContentEntityObject page;
 	
 	private Converter converter;
 
 	@Before
 	public void createConverter() throws Exception {
		converter = new Converter(wikiStyleRenderer);
 	}
 
 	@Test
 	public void test_more_macro() throws IOException {
		String moreMacroHtml = FileUtils.readFileToString(new File("src/main/resources/vm/more.vm"));
		when(wikiStyleRenderer.convertWikiToXHtml(any(RenderContext.class), any(String.class))).thenReturn(moreMacroHtml);
 		String html = converter.convert(page, new ConverterOptions());
 		assertEquals("<!--more-->", StringUtils.trim(html));
 	}
 
 }
