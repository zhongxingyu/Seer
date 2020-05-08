 package com.alexrnl.subtitlecorrector.correctionstrategy;
 
 import static com.alexrnl.subtitlecorrector.common.TranslationKeys.KEYS;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Iterator;
 import java.util.Locale;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.alexrnl.subtitlecorrector.common.Subtitle;
 
 /**
  * Test suite for the {@link FixPunctuation} class.
  * @author Alex
  */
 public class FixPunctuationTest {
 	/** The punctuation fixer strategy */
 	private FixPunctuation		fixPunctuation;
 	/** Reference to the locale parameter of the strategy */
 	private Parameter<Locale>	locale;
 	
 	/**
 	 * Set up test attributes.
 	 * @throws URISyntaxException
 	 *         if there is an issue when parsing the path.
 	 * @throws IOException
 	 *         if there is an issue when loading the rules.
 	 */
 	@Before
 	public void setUp () throws IOException, URISyntaxException {
 		fixPunctuation = new FixPunctuation(Paths.get(FixPunctuation.class.getResource("/punctuation").toURI()));
 		locale = (Parameter<Locale>) fixPunctuation.getParameterByName(KEYS.strategy().fixPunctuation().locale());
 		assertNotNull(locale);
 	}
 	
 	/**
	 * Check that the strategy cannot be created with a file as a parameter.
	 * @throws IOException
	 *         if there is an issue when loading the rules.
	 */
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void createWithFile () throws IOException {
		final Path tempFile = Files.createTempFile("punctuationRule", "txt");
		tempFile.toFile().deleteOnExit();
		new FixPunctuation(tempFile);
	}
	
	/**
 	 * Test method for {@link FixPunctuation#getParameters()}.
 	 */
 	@Test
 	public void testGetParameters () {
 		assertEquals(1, fixPunctuation.getParameters().size());
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#getParameterByName(String)}.
 	 */
 	@Test
 	public void testGetParameterByName () {
 		final Iterator<Parameter<?>> parametersIterator = fixPunctuation.getParameters().iterator();
 		assertEquals(parametersIterator.next(), fixPunctuation.getParameterByName(KEYS.strategy().fixPunctuation().locale()));
 		assertNull(fixPunctuation.getParameterByName("ldr"));
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#toString()}
 	 */
 	@Test
 	public void testToString () {
 		assertEquals("subtitlecorrector.strategy.fixpunctuation", fixPunctuation.toString());
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#getDescription()}
 	 */
 	@Test
 	public void testGetDescription () {
 		assertEquals("subtitlecorrector.strategy.fixpunctuation.description", fixPunctuation.getDescription());
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#correct(Subtitle)}.
 	 */
 	@Test
 	public void testCorrectFrenchRules1 () {
 		final Subtitle subtitleToCorrect = new Subtitle(0, 2000, "Bonjour,comment allez vous?");
 		locale.setValue(Locale.FRENCH);
 		fixPunctuation.correct(subtitleToCorrect);
 		assertEquals("Bonjour, comment allez vous ?", subtitleToCorrect.getContent());
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#correct(Subtitle)}.
 	 */
 	@Test
 	public void testCorrectFrenchRules2 () {
 		final Subtitle subtitleToCorrect = new Subtitle(0, 2000, "Bonjour;comment allez vous!");
 		locale.setValue(Locale.FRENCH);
 		fixPunctuation.correct(subtitleToCorrect);
 		assertEquals("Bonjour ; comment allez vous !", subtitleToCorrect.getContent());
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#correct(Subtitle)}.
 	 */
 	@Test
 	public void testCorrectEnglishRules1 () {
 		final Subtitle subtitleToCorrect = new Subtitle(0, 2000, "Bonjour,comment allez vous?Hey:you");
 		locale.setValue(Locale.ENGLISH);
 		fixPunctuation.correct(subtitleToCorrect);
 		assertEquals("Bonjour, comment allez vous? Hey: you", subtitleToCorrect.getContent());
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#correct(Subtitle)}.
 	 */
 	@Test
 	public void testCorrectEnglishRules2 () {
 		final Subtitle subtitleToCorrect = new Subtitle(0, 2000, "Bonjour;comment allez vous!");
 		locale.setValue(Locale.ENGLISH);
 		fixPunctuation.correct(subtitleToCorrect);
 		assertEquals("Bonjour; comment allez vous!", subtitleToCorrect.getContent());
 	}
 	
 	/**
 	 * Test method for {@link FixPunctuation#correct(Subtitle)}.
 	 */
 	@Test
 	public void testCorrectNewLine () {
 		final Subtitle subtitleToCorrect = new Subtitle(0, 2000, "Bonjour;\ncomment allez vous! Bien");
 		locale.setValue(Locale.ENGLISH);
 		fixPunctuation.correct(subtitleToCorrect);
 		assertEquals("Bonjour;\ncomment allez vous! Bien", subtitleToCorrect.getContent());
 	}
 	
 }
