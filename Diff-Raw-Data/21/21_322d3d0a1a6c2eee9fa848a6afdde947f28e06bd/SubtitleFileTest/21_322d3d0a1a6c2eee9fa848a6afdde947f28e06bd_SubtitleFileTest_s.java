 package com.alexrnl.subtitlecorrector.common;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
import java.io.File;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.alexrnl.commons.utils.CollectionUtils;
 
 /**
  * Test suite for the {@link SubtitleFile} class.
  * @author Alex
  */
 public class SubtitleFileTest {
 	/** An empty subtitle file */
 	private SubtitleFile	emptyFile;
 	/** A file with subtitles */
 	private SubtitleFile	file;
 	
 	/**
 	 * Set up test attributes.
 	 */
 	@Before
 	public void setUp () {
 		emptyFile = new SubtitleFile(null);
 		file = new SubtitleFile(Paths.get("this", "is", "dumb"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.common.SubtitleFile#getFile()}.
 	 */
 	@Test
 	public void testGetFile () {
 		assertNull(emptyFile.getFile());
		assertEquals("this" + File.separatorChar + "is" + File.separatorChar + "dumb", file.getFile().toString());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.subtitlecorrector.common.SubtitleFile#update()}.
 	 */
 	@Test
 	public void testUpdate () {
 		final List<Subtitle> subs = new ArrayList<>(3);
 
 		subs.add(new Subtitle(28, 30, "Test"));
 		subs.add(new Subtitle(88, 94, "LDR"));
 		subs.add(new Subtitle(128, 136, "ABA"));
 		
 		file.addAll(subs);
 		assertTrue(CollectionUtils.isSorted(file));
 		assertEquals(3, file.size());
 		subs.get(1).setBegin(888);
 		subs.get(1).setEnd(894);
 		assertFalse(CollectionUtils.isSorted(file));
 		assertEquals(3, file.size());
 		file.update();
 		assertTrue(CollectionUtils.isSorted(file));
 		assertEquals(3, file.size());
 		file.update();
 		assertTrue(CollectionUtils.isSorted(file));
 		assertEquals(3, file.size());
 	}
 }
