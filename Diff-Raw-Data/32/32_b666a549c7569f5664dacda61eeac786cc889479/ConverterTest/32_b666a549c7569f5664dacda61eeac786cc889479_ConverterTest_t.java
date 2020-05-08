/**
 * Copyright 2011 Alexandre Dutra
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
 package fr.xebia.confluence2wordpress.core.converter;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.renderer.MacroManager;
 import com.atlassian.renderer.RenderContext;
 import com.atlassian.renderer.WikiStyleRenderer;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public class ConverterTest {
 	
 	@Mock
 	private WikiStyleRenderer wikiStyleRenderer;
 	
    @Mock
    private MacroManager macroManager;
    
 	@Mock
 	private ContentEntityObject page;
 	
 	private Converter converter;
 
 	@Before
 	public void createConverter() throws Exception {
		converter = new Converter(wikiStyleRenderer, macroManager);
 	}
 
 	@Test
 	public void test_more_macro() throws IOException {
		when(wikiStyleRenderer.convertWikiToXHtml(any(RenderContext.class), any(String.class))).thenReturn("<!--more-->");
 		String html = converter.convert(page, new ConverterOptions());
 		assertEquals("<!--more-->", StringUtils.trim(html));
 	}
 
 }
