 package is.us.util;
 
 import static org.junit.Assert.*;
 
 import java.io.*;
 import java.util.HashMap;
 
 import org.junit.Test;
 
 /**
  * Tests for USStringUtilities.
  * 
  * @author Hugi Thordarson
  */
 
 public class TestUSStringUtilities {
 
 	private static final String[] CORRECT_EMAIL_ADDRESSES = new String[] { "joe@test.smu.voff.arg", "logi@us.is", "@.", "thisisthelongnameofmyeviltwin@thearndolswazrtnaggermoviewasnotsogreat.withthelittleguy" };
 	private static final String[] WRONG_EMAIL_ADDRESSES = new String[] { "logi@rokk", "this.is", "wonderingWhy", "forgot@thedot", "432", "#", "&", "", " ", null };
 
 	private static final String[] PAIRS_THAT_ARE_SIMILAR = new String[] { "HUGI;H U G I", "L OG I;LOGI", "HUG1;HUGI", "YMIR;ÝM 1R" };
 	private static final String[] PAIRS_THAT_ARE_NOT_SIMILAR = new String[] { "HUGI;HUGO", "LOGO;L0GI", "SMU;VOFFVOFF" };
 
 	@Test
 	public void stringHasValue() {
 		assertFalse( USStringUtilities.stringHasValue( "" ) );
 		assertFalse( USStringUtilities.stringHasValue( null ) );
 		assertTrue( USStringUtilities.stringHasValue( " " ) );
 		assertTrue( USStringUtilities.stringHasValue( "testString" ) );
 	}
 
 	@Test
 	public void stringHasValueAndNotNullString() {
 		assertTrue( USStringUtilities.stringHasValueAndNotNullString( " " ) );
 		assertTrue( USStringUtilities.stringHasValueAndNotNullString( "1234" ) );
 		assertFalse( USStringUtilities.stringHasValueAndNotNullString( null ) );
 		assertFalse( USStringUtilities.stringHasValueAndNotNullString( "null" ) );
 		assertFalse( USStringUtilities.stringHasValueAndNotNullString( "Null" ) );
 	}
 
 	@Test
 	public void stringHasValueTrimmed() {
 		assertFalse( USStringUtilities.stringHasValueTrimmed( " " ) );
 		assertTrue( USStringUtilities.stringHasValueTrimmed( "1234" ) );
 		assertFalse( USStringUtilities.stringHasValueTrimmed( null ) );
 		assertTrue( USStringUtilities.stringHasValueTrimmed( "null" ) ); // note that this function does not check for null string values
 	}
 
 	@Test
 	public void validateEmailAddress() {
 
 		for( String nextAddress : CORRECT_EMAIL_ADDRESSES )
 			assertTrue( USStringUtilities.validateEmailAddress( nextAddress ) );
 
 		for( String nextAddress : WRONG_EMAIL_ADDRESSES )
 			assertFalse( USStringUtilities.validateEmailAddress( nextAddress ) );
 	}
 
 	@Test
 	public void validatePermno() {
 		assertFalse( USStringUtilities.validatePermno( " " ) );
 		assertTrue( USStringUtilities.stringHasValueTrimmed( "ox279" ) );
 		assertFalse( USStringUtilities.stringHasValueTrimmed( null ) );
 		assertTrue( USStringUtilities.stringHasValueTrimmed( "sos11" ) );
 	}
 
 	@Test
 	public void stringFromDataUsingEncoding() {
 		assertTrue( USStringUtilities.stringFromDataUsingEncoding( "asdf".getBytes(), "UTF-8" ).equals( "asdf" ) );
 		assertTrue( USStringUtilities.stringFromDataUsingEncoding( null, "UTF-8" ) == null );
 	}
 
 	@Test
 	public void areSimilar() {
 
 		for( String nextString : PAIRS_THAT_ARE_SIMILAR ) {
 			String[] a = nextString.split( ";" );
 			String testString1 = a[0];
 			String testString2 = a[1];
 			boolean areSimilar = USStringUtilities.areSimilar( testString1, testString2 );
 			assertTrue( areSimilar );
 		}
 
 		for( String nextString : PAIRS_THAT_ARE_NOT_SIMILAR ) {
 			String[] a = nextString.split( ";" );
 			String testString1 = a[0];
 			String testString2 = a[1];
 			boolean areSimilar = USStringUtilities.areSimilar( testString1, testString2 );
 			assertFalse( areSimilar );
 		}
 	}
 
 	@Test
 	public void areSimilar2() {
 		assertTrue( USStringUtilities.areSimilar( 'A', 'A' ) );
 		assertTrue( USStringUtilities.areSimilar( 'A', 'Á' ) );
 		assertTrue( USStringUtilities.areSimilar( 'I', 'Í' ) );
 		assertTrue( USStringUtilities.areSimilar( '1', 'Í' ) );
 		assertTrue( USStringUtilities.areSimilar( 'O', 'Ó' ) );
 		assertTrue( USStringUtilities.areSimilar( 'O', '0' ) );
 		assertTrue( USStringUtilities.areSimilar( '0', 'Ó' ) );
 		assertTrue( USStringUtilities.areSimilar( 'Y', 'Ý' ) );
 		assertTrue( USStringUtilities.areSimilar( 'Ý', 'Y' ) );
 	}
 
 	@Test
 	public void padString() {
 		assertTrue( USStringUtilities.padString( "asdf", 5 ).equals( "asdf " ) );
 		assertTrue( USStringUtilities.padString( "asdfasdf", 5 ).equals( "asdfasdf" ) );
 	}
 
 	@Test
 	public void padLeft() {
 		assertTrue( USStringUtilities.padLeft( "asdf", "*", 5 ).equals( "*asdf" ) );
 		assertTrue( USStringUtilities.padLeft( "asdfasdf", "*", 5 ).equals( "asdfasdf" ) );
 		assertTrue( USStringUtilities.padLeft( "asdf", " ", 5 ).equals( " asdf" ) );
 		assertTrue( USStringUtilities.padLeft( "asdf asdf", " ", 10 ).equals( " asdf asdf" ) );
 	}
 
 	@Test
 	public void padRight() {
 		assertTrue( USStringUtilities.padRight( "asdf", "*", 5 ).equals( "asdf*" ) );
 		assertTrue( USStringUtilities.padRight( "asdfasdf", "*", 5 ).equals( "asdfasdf" ) );
 		assertTrue( USStringUtilities.padRight( "asdf", " ", 5 ).equals( "asdf " ) );
 		assertTrue( USStringUtilities.padRight( "asdf asdf", " ", 10 ).equals( "asdf asdf " ) );
 	}
 
 	@Test
 	public void adjustStringToLength() {
 		assertTrue( USStringUtilities.adjustStringToLength( "asdf", 5 ).equals( "asdf " ) );
 		assertTrue( USStringUtilities.adjustStringToLength( "asdfasdf", 5 ).equals( "asdfa" ) );
 		assertTrue( USStringUtilities.adjustStringToLength( "as df", 5 ).equals( "as df" ) );
 		assertTrue( USStringUtilities.adjustStringToLength( "asdf asdf", 10 ).equals( "asdf asdf " ) );
 	}
 
 	@Test
 	public void writeStringToFileUsingEncoding() {
 		String testPath = USTestUtilities.documentPath( this.getClass(), "writeStringToFileUsingEncoding.txt" );
 		try {
 			USStringUtilities.writeStringToFileUsingEncoding( "the lazy cat crawled under the hyper dog", new File( testPath ), "utf-8" );
 			assertEquals( USTestUtilities.streamDigest( testPath ), "eab01c6dd27ef0193bc899b52b51919" );
 		}
 		catch( Exception e ) {
 			fail( e.getMessage() );
 		}
 	}
 
 	@Test
 	public void readStringFromURLUsingEncoding() {
 		String testPath = USTestUtilities.documentPath( this.getClass(), "writeStringToFileUsingEncoding.txt" );
 		try {
 			String retString = USStringUtilities.readStringFromFileUsingEncoding( new File( testPath ), "utf-8" );
 			assertEquals( retString, "the lazy cat crawled under the hyper dog" );
 		}
 		catch( Exception e ) {
 			fail( e.getMessage() );
 		}
 	}
 
 	@Test
 	public void readStringFromInputStreamUsingEncoding() {
 		String testPath = USTestUtilities.documentPath( this.getClass(), "writeStringToFileUsingEncoding.txt" );
 		try {
 			FileInputStream fis = new FileInputStream( new File( testPath ) );
 			String retString = USStringUtilities.readStringFromInputStreamUsingEncoding( fis, "utf-8" );
 			assertEquals( retString, "the lazy cat crawled under the hyper dog" );
 		}
 		catch( Exception e ) {
 			fail( e.getMessage() );
 		}
 	}
 
 	@Test
 	public void replace() {
 		assertEquals( USStringUtilities.replace( "asdf", "s", "d" ), "addf" );
 		assertEquals( USStringUtilities.replace( "aSdf", "S", "d" ), "addf" );
 		assertEquals( USStringUtilities.replace( "hyper dog", "hyper dog", "lazy cat" ), "lazy cat" );
 	}
 
 	@Test
 	public void readStringFromFileUsingEncoding() {
 		String testPath = USTestUtilities.documentPath( this.getClass(), "writeStringToFileUsingEncoding.txt" );
 		try {
 			String retString = USStringUtilities.readStringFromFileUsingEncoding( new File( testPath ), "utf-8" );
 			assertEquals( retString, "the lazy cat crawled under the hyper dog" );
 		}
 		catch( Exception e ) {
 			fail( e.getMessage() );
 		}
 	}
 
 	@Test
 	public void cleanupPermno() {
 		assertEquals( USStringUtilities.cleanupPermno( "ox279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupPermno( "ox 279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupPermno( "ox-279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupPermno( "OX279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupPermno( "OX  279" ), "OX279" );
 	}
 
 	@Test
 	public void cleanupRegno() {
 		assertEquals( USStringUtilities.cleanupRegno( "ox279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupRegno( "ox 279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupRegno( "ox-279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupRegno( "OX279" ), "OX279" );
 		assertEquals( USStringUtilities.cleanupRegno( "OX  279" ), "OX279" );
 	}
 
 	@Test
 	public void replaceIcelandicCharsAndRemoveSpaces() {
 		assertEquals( USStringUtilities.replaceIcelandicCharsAndRemoveSpaces( " ÖÉÝÚÍÓÐÁÆÞöéýúíóðáæþ" ), "OEYUIODAAETHoeyuiodaaeth" );
 	}
 
 	@Test
 	public void numberOfStringsWithValue() {
 		String[] tArr = null;
 		assertEquals( USStringUtilities.numberOfStringsWithValue( tArr ), 0 );
 		assertEquals( USStringUtilities.numberOfStringsWithValue( new String[] { "", "bjkl" } ), 1 );
 		assertEquals( USStringUtilities.numberOfStringsWithValue( new String[] { null, "bjkl" } ), 1 );
 		assertEquals( USStringUtilities.numberOfStringsWithValue( new String[] { "a", "bjkl" } ), 2 );
 
 	}
 
 	@Test
 	public void anyStringHasValue() {
 		String[] tArr = null;
 		assertFalse( USStringUtilities.anyStringHasValue( tArr ) );
 		assertFalse( USStringUtilities.anyStringHasValue( new String[] { "", null } ) );
 		assertTrue( USStringUtilities.anyStringHasValue( new String[] { "", "bjkl" } ) );
 		assertTrue( USStringUtilities.anyStringHasValue( new String[] { null, "bjkl" } ) );
 		assertTrue( USStringUtilities.anyStringHasValue( new String[] { "a", "bjkl" } ) );
 	}
 
 	@Test
 	public void numberOfWildcardCharactersInString() {
 		assertEquals( USStringUtilities.numberOfWildcardCharactersInString( "as?df" ), 1 );
 		assertEquals( USStringUtilities.numberOfWildcardCharactersInString( "as*?df" ), 2 );
 		assertEquals( USStringUtilities.numberOfWildcardCharactersInString( "asdf" ), 0 );
 		assertEquals( USStringUtilities.numberOfWildcardCharactersInString( "*s*d*f*" ), 4 );
 		assertEquals( USStringUtilities.numberOfWildcardCharactersInString( "asdfasdfasdfa*sdfaal kjsehfæ a?oiugh paosidhfg paosiehdf?æaois hgæ aosiurgh æadsoughæa*odurghæoasdihrgæoaiuer*hgp oqhirg" ), 5 );
 	}
 
 	@Test
 	public void numberOfNonWildcardCharactersInString() {
 		assertEquals( USStringUtilities.numberOfNonWildcardCharactersInString( "as?df" ), 4 );
 		assertEquals( USStringUtilities.numberOfNonWildcardCharactersInString( "as*?df" ), 4 );
 		assertEquals( USStringUtilities.numberOfNonWildcardCharactersInString( "asdf" ), 4 );
 		assertEquals( USStringUtilities.numberOfNonWildcardCharactersInString( "*s*d*" ), 2 );
 		assertEquals( USStringUtilities.numberOfNonWildcardCharactersInString( "asdfasdfasdfa*sdfaal kjsehfæ a?oiugh paosidhfg paosiehdf?æaois hgæ aosiurgh æadsoughæa*odurghæoasdihrgæoaiuer*hgp oqhirg" ), 115 );
 	}
 
 	@Test
 	public void stringWithFormat() {
 		assertEquals( USStringUtilities.stringWithFormat( "Hæ {}, segirðu ekki allt {}?", new Object[] { "Hugi", "gott" } ), "Hæ Hugi, segirðu ekki allt gott?" );
 		assertEquals( USStringUtilities.stringWithFormat( "...{}{}{}.{}.", new Object[] { new Integer( 1 ), "2", 3, "4" } ), "...123.4." );
 		assertEquals( USStringUtilities.stringWithFormat( "{}", new Object[] { "testString" } ), "testString" );
 		assertEquals( USStringUtilities.stringWithFormat( "", new Object[] { "testString" } ), "" );
 	}
 
 	@Test
 	public void convertBreakString() {
 		assertEquals( USStringUtilities.convertBreakString( "as\ndf" ), "as<br />\ndf" );
 		assertEquals( USStringUtilities.convertBreakString( "\nas\ndf\n" ), "<br />\nas<br />\ndf<br />\n" );
 		assertEquals( USStringUtilities.convertBreakString( "\na\ns\nd\nf\n" ), "<br />\na<br />\ns<br />\nd<br />\nf<br />\n" );
 		assertEquals( USStringUtilities.convertBreakString( "asdf" ), "asdf" );
 	}
 
 	@Test
 	public void caseInsensitiveEndsWith() {
 		assertTrue( USStringUtilities.caseInsensitiveEndsWith( "asdf", "f" ) );
 		assertTrue( USStringUtilities.caseInsensitiveEndsWith( "asDf", "dF" ) );
 		assertFalse( USStringUtilities.caseInsensitiveEndsWith( "asdf", "s" ) );
 		assertTrue( USStringUtilities.caseInsensitiveEndsWith( "", "" ) );
 	}
 
 	@Test
 	public void fileNameFromPath() {
 		String testPath = USTestUtilities.documentPath( this.getClass(), "somesillyname.txt" );
 		assertEquals( USStringUtilities.fileNameFromPath( testPath ), "somesillyname.txt" );
 		assertEquals( USStringUtilities.fileNameFromPath( "somesillyname.txt" ), "somesillyname.txt" );
 		assertEquals( USStringUtilities.fileNameFromPath( "" ), "" );
 		assertEquals( USStringUtilities.fileNameFromPath( null ), null );
 	}
 
 	@Test
 	public void htmlify() {
 		assertEquals( USStringUtilities.htmlify( " a\nhttp://www.us.is/vedur " ), "a<br />\n<a href=\"http://www.us.is/vedur\">http://www.us.is/vedur</a>" );
 	}
 
 	@Test
 	public void activateHyperlinksInString() {
		assertEquals( USStringUtilities.activateHyperlinksInString( "a http://www.us.is/vedur" ), "a <a href=\"http://www.us.is/vedur\">http://www.us.is/vedur</a>" );
 	}
 
 	@Test
 	public void fixUrl() {
 		assertEquals( USStringUtilities.fixUrl( "www.US.is/vedur" ), "http://www.us.is/vedur" );
 	}
 
 	@Test
 	public void replaceInStringWithValuesFromMap() {
 		HashMap<String, String> daMap = new HashMap<String, String>();
 		daMap.put( "", "Y" );
 		daMap.put( "aa", "xx" );
 		daMap.put( "hyper dog", "excited dog" );
 		assertEquals( USStringUtilities.replaceInStringWithValuesFromMap( "the lazy cat crawled under the hyper dog", daMap ), "the lazy cat crawled under the excited dog" );
 	}
 
 	@Test
 	public void abbrString() {
 		assertEquals( USStringUtilities.abbrString( "the lazy cat crawled under the hyper dog", 5 ), "th..." );
 		assertEquals( USStringUtilities.abbrString( "the lazy cat crawled under the hyper dog", 0 ), "..." );
 		assertEquals( USStringUtilities.abbrString( "", 5 ), "" );
 	}
 
 	@Test
 	public void formatBytes() {
 		assertEquals( USStringUtilities.formatBytes( 1 ), "1B" );
 		assertEquals( USStringUtilities.formatBytes( 1025 ), "1KB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 2 ) ), "1MB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 3 ) ), "1GB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 4 ) ), "1TB" );
 	}
 
 	@Test
 	public void formatBytes2() {
 		assertEquals( USStringUtilities.formatBytes( 1, true ), "1 bytes" );
 		assertEquals( USStringUtilities.formatBytes( 1025, false ), "1KB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 2 ), true ), "1 megabytes" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 3 ), false ), "1GB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 4 ), true ), "1 terabytes" );
 	}
 
 	@Test
 	public void formatBytes3() {
 		assertEquals( USStringUtilities.formatBytes( 1, 0, 1 ), "1B" );
 		assertEquals( USStringUtilities.formatBytes( 1025, 2, 4 ), "1,001KB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 2 ), 0, 1 ), "1MB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 3 ), 2, 2 ), "1,00GB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 4 ), 4, 8 ), "1,0000TB" );
 	}
 
 	@Test
 	public void formatBytes4() {
 		assertEquals( USStringUtilities.formatBytes( 1, 0, 1, " " ), "1 B" );
 		assertEquals( USStringUtilities.formatBytes( 1025, 2, 4, "_" ), "1,001_KB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 2 ), 0, 1, "" ), "1MB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 3 ), 2, 2, null ), "1,00GB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 4 ), 4, 8, "...." ), "1,0000....TB" );
 	}
 
 	@Test
 	public void formatBytes5() {
 		assertEquals( USStringUtilities.formatBytes( 1, 0, 1, " ", true ), "1 bytes" );
 		assertEquals( USStringUtilities.formatBytes( 1025, 2, 4, "", true ), "1,001kilobytes" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 2 ), 0, 1, "", false ), "1MB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 3 ), 2, 2, null, false ), "1,00GB" );
 		assertEquals( USStringUtilities.formatBytes( Math.pow( 1024, 4 ), 4, 8, null, true ), "1,0000terabytes" );
 	}
 
 	@Test
 	public void formatDouble() {
 		assertEquals( USStringUtilities.formatDouble( 1.0 ), "1" );
 		assertEquals( USStringUtilities.formatDouble( 10.0 ), "10" );
 		assertEquals( USStringUtilities.formatDouble( 100.0 ), "100" );
 		assertEquals( USStringUtilities.formatDouble( 1000.0 ), "1.000" );
 		assertEquals( USStringUtilities.formatDouble( Double.MAX_VALUE ), "179.769.313.486.231.570.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000" );
 		assertEquals( USStringUtilities.formatDouble( Double.MIN_VALUE, 100, false ), "0" );
 		assertEquals( USStringUtilities.formatDouble( 0 ), "0" );
 		assertEquals( USStringUtilities.formatDouble( 0.1 ), "0,1" );
 		assertEquals( USStringUtilities.formatDouble( 0.01, 4, true ), "0,0100" );
 		assertEquals( USStringUtilities.formatDouble( 0.0001 ), "0" );
 		assertEquals( USStringUtilities.formatDouble( 1000.0001, 4, true ), "1.000,0001" );
 	}
 
 	@Test
 	public void cleanupPreRegistrationNumber() {
 		assertEquals( USStringUtilities.cleanupPreRegistrationNumber( "123asd" ), "123ASD" );
 		assertEquals( USStringUtilities.cleanupPreRegistrationNumber( " 123asd.." ), "123ASD" );
 		assertEquals( USStringUtilities.cleanupPreRegistrationNumber( "123 asd" ), null );
 		assertEquals( USStringUtilities.cleanupPreRegistrationNumber( "" ), null );
 		assertEquals( USStringUtilities.cleanupPreRegistrationNumber( null ), null );
 	}
 
 	@Test
 	public void unescapeHTML() {
 		assertEquals( USStringUtilities.unescapeHTML( "&ouml;ll l&iacute;fsins g&aelig;&eth;i" ), "öll lífsins gæði" );
 		assertEquals( USStringUtilities.unescapeHTML( "&amp;ouml;ll l&amp;iacute;fsins g&amp;aelig;&amp;eth;i" ), "&ouml;ll l&iacute;fsins g&aelig;&eth;i" );
 		assertEquals( USStringUtilities.unescapeHTML( "" ), "" );
 		assertEquals( USStringUtilities.unescapeHTML( null ), "" );
 	}
 
 	@Test
 	public void escapeHTML() {
 		assertEquals( USStringUtilities.escapeHTML( "öll lífsins gæði" ), "&ouml;ll l&iacute;fsins g&aelig;&eth;i" );
 		assertEquals( USStringUtilities.escapeHTML( "&ouml;ll l&iacute;fsins g&aelig;&eth;i" ), "&amp;ouml;ll l&amp;iacute;fsins g&amp;aelig;&amp;eth;i" );
 		assertEquals( USStringUtilities.escapeHTML( "" ), "" );
 		assertEquals( USStringUtilities.escapeHTML( null ), "" );
 	}
 
 	@Test
 	public void constructURLStringWithParameters() {
 		HashMap<String, String> params = new HashMap<String, String>();
 		assertEquals( USStringUtilities.constructURLStringWithParameters( null, params ), "" );
 		assertEquals( USStringUtilities.constructURLStringWithParameters( "", null ), "" );
 		assertEquals( USStringUtilities.constructURLStringWithParameters( "www.us.is", params ), "www.us.is" );
 		params.put( "a", "b" );
 		assertEquals( USStringUtilities.constructURLStringWithParameters( "www.us.is", params ), "www.us.is?a=b&" );
 		params.put( "æ", "ð" );
 		assertEquals( USStringUtilities.constructURLStringWithParameters( "www.us.is", params ), "www.us.is?a=b&æ=ð&" );
 	}
 }
