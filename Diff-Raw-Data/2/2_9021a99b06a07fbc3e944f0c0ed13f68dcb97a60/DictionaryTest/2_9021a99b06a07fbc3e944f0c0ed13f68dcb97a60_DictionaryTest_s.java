 package com.alexrnl.subtitlecorrector.io;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Test suite for the {@link Dictionary} class.
  * @author Alex
  */
 public class DictionaryTest {
 	/** The file for the dictionary. */
 	private static Path	dictionaryFile;
 	/** The file for the copy of the dictionary */
 	private static Path	dictionaryCopy;
 	
 	/** A read-only dictionary */
 	private Dictionary dictionary;
 	/** An editable dictionary */
 	private Dictionary editableDictionary;
 	
 
 	/**
 	 * Copy the dictionary to a temporary file so it can be safely edited.
 	 * @throws IOException
 	 *         if there is a problem when loading the file.
 	 * @throws URISyntaxException
 	 *         if the the path is badly formatted.
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass () throws IOException, URISyntaxException {
		dictionaryFile = Paths.get(Dictionary.class.getResource("/dico.fr.txt").toURI());
 		dictionaryCopy = Files.createTempFile("dictionary", ".txt");
 		dictionaryCopy.toFile().deleteOnExit();
 		Files.copy(dictionaryFile, dictionaryCopy, StandardCopyOption.REPLACE_EXISTING);
 	}
 	
 	/**
 	 * Set up test attributes.
 	 * @throws IOException
 	 *         if there is a problem when loading the file.
 	 * @throws URISyntaxException
 	 *         if the the path is badly formatted.
 	 */
 	@Before
 	public void setUp () throws IOException, URISyntaxException {
 		dictionary = new Dictionary(dictionaryFile);
 		editableDictionary = new Dictionary(dictionaryCopy, true);
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#Dictionary(java.nio.file.Path)}.
 	 * @throws IOException
 	 *         if there is a problem when loading the file.
 	 */
 	@Test(expected = NullPointerException.class)
 	public void testDictionaryNPEPath () throws IOException {
 		new Dictionary(null);
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#Dictionary(java.nio.file.Path)}.
 	 * @throws IOException
 	 *         if there is a problem when loading the file.
 	 */
 	@Test(expected = NullPointerException.class)
 	public void testDictionaryNPECharSet () throws IOException {
 		new Dictionary(Paths.get("dummy", "path"), null, true);
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#Dictionary(java.nio.file.Path)}.
 	 * @throws IOException
 	 *         if there is a problem when loading the file.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testDictionaryIAEPathNotExists () throws IOException {
 		new Dictionary(Paths.get("dummy", "path"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#Dictionary(java.nio.file.Path)}.
 	 * @throws IOException
 	 *         if there is a problem when loading the file.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testDictionaryIAENotWritable () throws IOException {
 		final Path temporaryFile = Files.createTempFile("dictionary", ".txt");
 		temporaryFile.toFile().setWritable(false);
 		temporaryFile.toFile().deleteOnExit();
 		new Dictionary(temporaryFile, StandardCharsets.UTF_8, true);
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#save()}.
 	 * @throws IOException
 	 *         if there is a problem when writing the file.
 	 */
 	@Test
 	public void testSave () throws IOException {
 		assertFalse(editableDictionary.contains("zedzfrgtlermforopfz"));
 		assertTrue(editableDictionary.addWord("zedzfrgtlermforopfz"));
 		editableDictionary.save();
 		final Dictionary savedDictionary = new Dictionary(dictionaryCopy);
 		assertTrue(savedDictionary.contains("zedzfrgtlermforopfz"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#save()}.
 	 * @throws IOException
 	 *         if there is a problem when writing the file.
 	 */
 	@Test(expected = IllegalStateException.class)
 	public void testSaveIllegalStateExcetion () throws IOException {
 		dictionary.save();
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#isEditable()}.
 	 */
 	@Test
 	public void testIsEditable () {
 		assertFalse(dictionary.isEditable());
 		assertTrue(editableDictionary.isEditable());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#isUpdated()}.
 	 * @throws IOException
 	 *         if a save action fails.
 	 */
 	@Test
 	public void testIsUpdated () throws IOException {
 		assertFalse(dictionary.isUpdated());
 		assertTrue(dictionary.addWord("zedzfrgtforopfz"));
 		assertTrue(dictionary.isUpdated());
 
 		assertFalse(editableDictionary.isUpdated());
 		assertTrue(editableDictionary.addWord("zedgtlermforop"));
 		assertTrue(editableDictionary.isUpdated());
 		editableDictionary.save();
 		assertFalse(editableDictionary.isUpdated());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#contains(java.lang.String)}.
 	 */
 	@Test
 	public void testContains () {
 		assertFalse(dictionary.contains("zedzfrgtlermforo"));
 		assertFalse(dictionary.contains(null));
 		assertFalse(dictionary.contains(""));
 		assertTrue(dictionary.contains("mot"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.io.Dictionary#addWord(java.lang.String)}.
 	 */
 	@Test
 	public void testAddWord () {
 		assertFalse(dictionary.addWord("mot"));
 		assertFalse(dictionary.contains("zedzfrgtlermforopfz"));
 		assertTrue(dictionary.addWord("zedzfrgtlermforopfz"));
 		assertTrue(dictionary.contains("zedzfrgtlermforopfz"));
 	}
 }
