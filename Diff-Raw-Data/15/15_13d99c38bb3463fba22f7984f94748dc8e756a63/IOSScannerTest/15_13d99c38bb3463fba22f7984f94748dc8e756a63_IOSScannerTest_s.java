 package org.jabylon.ios;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.util.Locale;
 import java.util.Map;
 
 import org.jabylon.properties.ScanConfiguration;
 import org.junit.Before;
 import org.junit.Test;
 
 public class IOSScannerTest {
 
 	private IOSScanner fixture;
 	
 	@Before
 	public void createFixture() {
 		fixture = new IOSScanner();
 	}
 	
 	@Test
 	public void testFindTemplate() {
 		ScanConfiguration config = mock(ScanConfiguration.class);
 		when(config.getMasterLocale()).thenReturn("en");
 		File template = fixture.findTemplate(new File("src/test/resources/en.lproj/foo.strings"), config);
 		assertEquals(new File("src/test/resources/en.lproj/foo.strings"), template);
 	}
 
 	@Test
 	public void testFindTranslations() {
 		ScanConfiguration config = mock(ScanConfiguration.class);
 		when(config.getMasterLocale()).thenReturn("en");
 		Map<Locale, File> translations = fixture.findTranslations(new File("src/test/resources/en.lproj/foo.strings"), config);
 		assertEquals(1, translations.size());
 		assertEquals(new File("src/test/resources/de.lproj/foo.strings"), translations.get(new Locale("de")));
 	}
 
 	@Test
 	public void testComputeTranslationPath() {
 		File translationPath = fixture.computeTranslationPath(new File("src/test/resouces/de.lproj/foo.strings"), new Locale("de"), new Locale("en"));
 		assertEquals(new File("src/test/resouces/en.lproj/foo.strings"), translationPath);
 	}
 
 	@Test
 	public void testGetLocale() {
 		assertEquals(new Locale("de"), fixture.getLocale(new File("de.proj/foo.strings")));
 		assertNull(fixture.getLocale(new File("proj/foo.strings")));
 		assertNull(fixture.getLocale(new File("proj.de/foo/foo.strings")));
 	}
 
 	@Test
 	public void testGetLocaleFromFolder() {
 		assertEquals(new Locale("de"), fixture.getLocaleFromFolder(new File("de.proj")));
 		assertNull(fixture.getLocaleFromFolder(new File("proj")));
 	}
 
 	@Test
 	public void testIsBilingual() {
 		assertFalse(fixture.isBilingual());
 	}
 
 
 	@Test
 	public void testGetDefaultIncludes() {
 		assertArrayEquals(new String[]{"**/*.strings"}, fixture.getDefaultIncludes());
 	}
 
 	@Test
 	public void testGetDefaultExcludes() {
 		assertNotNull(fixture.getDefaultExcludes());
 	}
 
 	@Test
 	public void testGetEncoding() {
 		assertEquals("UTF-16", fixture.getEncoding());
 	}
 
 }
