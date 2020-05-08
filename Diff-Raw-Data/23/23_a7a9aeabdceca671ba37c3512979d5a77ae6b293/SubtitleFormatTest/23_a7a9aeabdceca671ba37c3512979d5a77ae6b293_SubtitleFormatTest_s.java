 package com.alexrnl.subtitlecorrector.io;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotEquals;
 import static org.junit.Assert.assertNull;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 import java.util.Arrays;
 import java.util.HashSet;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 /**
  * Test suite for the {@link SubtitleFormat} class.
  * @author Alex
  */
 public class SubtitleFormatTest {
 	/** The SubRip format */
 	private SubtitleFormat	subrip;
 	/** The ASS format */
 	private SubtitleFormat	ass;
 	/** The mocked subtitle reader */
 	@Mock
 	private SubtitleReader	reader;
 	/** The mocked subtitle writer */
 	@Mock
 	private SubtitleWriter	writer;
 	
 	/**
 	 * Set up test attributes.
 	 */
 	@Before
 	public void setUp () {
 		initMocks(this);
		subrip = new SubtitleFormat("subrip", Arrays.asList("srt"), reader, null);
		ass = new SubtitleFormat("ass", null, writer, "ass", "ass2");
 	}
 	
 	/**
 	 * Test method for {@link SubtitleFormat#getName()}.
 	 */
 	@Test
 	public void testGetName () {
		assertEquals("subrip", subrip.getName());
		assertEquals("ass", ass.getName());
 	}
 	
 	/**
 	 * Test method for {@link SubtitleFormat#getExtensions()}.
 	 */
 	@Test
 	public void testGetExtensions () {
 		assertEquals(new HashSet<>(Arrays.asList("srt")), subrip.getExtensions());
		assertEquals(new HashSet<>(Arrays.asList("ass", "ass2")), ass.getExtensions());
 	}
 	
 	/**
 	 * Test method for {@link SubtitleFormat#getReader()}.
 	 */
 	@Test
 	public void testGetReader () {
 		assertEquals(reader, subrip.getReader());
 		assertNull(ass.getReader());
 	}
 	
 	/**
 	 * Test method for {@link SubtitleFormat#getWriter()}.
 	 */
 	@Test
 	public void testGetWriter () {
 		assertNull(subrip.getWriter());
 		assertEquals(writer, ass.getWriter());
 	}
 	
 	/**
 	 * Test method for {@link SubtitleFormat#hashCode()}.
 	 */
 	@Test
 	public void testHashCode () {
 		assertNotEquals(subrip.hashCode(), ass.hashCode());
 		assertEquals(subrip.hashCode(), subrip.hashCode());
 		assertEquals(ass.hashCode(), ass.hashCode());
 	}
 
 	/**
 	 * Test method for {@link SubtitleFormat#equals(Object)}.
 	 */
 	@Test
 	public void testEqualsObject () {
 		assertNotEquals(subrip, null);
 		assertNotEquals(subrip, ass);
		assertEquals(subrip, new SubtitleFormat("subrip", null, null));
 		assertNotEquals(subrip, new SubtitleFormat("Subrip", null, null));
		assertEquals(ass, new SubtitleFormat("ass", null, null));
 		assertNotEquals(ass, new SubtitleFormat(".ass", null, null));
 	}
 	
 	/**
 	 * Test method for {@link SubtitleFormat#toString()}.
 	 */
 	@Test
 	public void testToString () {
		assertEquals("subrip [srt]", subrip.toString());
		assertEquals("ass [ass, ass2]", ass.toString());
 	}
 }
