 package com.diycomputerscience.minesweepercore;
 import static org.easymock.EasyMock.capture;
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.easymock.Capture;
 import org.easymock.CaptureType;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.diycomputerscience.minesweepercore.Board;
 import com.diycomputerscience.minesweepercore.FilePersistenceStrategy;
 import com.diycomputerscience.minesweepercore.PersistenceStrategy;
 import com.diycomputerscience.minesweepercore.Square;
 
 
 public class FilePersistenceStrategyTest {
 
 	private String expectedSquaresAsText[] = {
 		"0,0:true-COVERED",
 		"0,1:false-COVERED",
 		"0,2:false-COVERED",
 		"0,3:false-COVERED",
 		"0,4:false-COVERED",
 		"0,5:false-COVERED",
 		"1,0:false-COVERED",
 		"1,1:false-COVERED",
 		"1,2:true-COVERED",
 		"1,3:false-COVERED",
 		"1,4:false-COVERED",
 		"1,5:false-COVERED",
 		"2,0:false-COVERED",
 		"2,1:true-COVERED",
 		"2,2:false-COVERED",
 		"2,3:false-COVERED",
 		"2,4:false-COVERED",
 		"2,5:false-COVERED",
 		"3,0:false-COVERED",
 		"3,1:false-COVERED",
 		"3,2:false-COVERED",
 		"3,3:false-COVERED",
 		"3,4:true-COVERED",
 		"3,5:false-COVERED",
 		"4,0:false-COVERED",
 		"4,1:false-COVERED",
 		"4,2:true-COVERED",
 		"4,3:false-COVERED",
 		"4,4:false-COVERED",
 		"4,5:true-COVERED",
 		"5,0:false-COVERED",
 		"5,1:false-COVERED",
 		"5,2:true-COVERED",
 		"5,3:true-COVERED",
 		"5,4:false-COVERED",
 		"5,5:false-COVERED"
 	};
 	
 	/**
 	* X 0 0 0 0 0
 	* 0 0 X 0 0 0
 	* 0 X 0 0 0 0
 	* 0 0 0 0 X 0
 	* 0 0 X 0 0 X
 	* 0 0 X X 0 0
 	*/
 	Square[][] expectedSquares = {
 		{cSq(Square.STATUS.COVERED,true), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false)},
 		{cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,true), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false)},
 		{cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,true), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false)},
 		{cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,true), cSq(Square.STATUS.COVERED,false)},
 		{cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,true), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,true)},
 		{cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,true), cSq(Square.STATUS.COVERED,true), cSq(Square.STATUS.COVERED,false), cSq(Square.STATUS.COVERED,false)},
 	};
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testLoadRegexForNoMatch() {
 		Pattern regexPattern = Pattern.compile(FilePersistenceStrategy.SQUARE_LOAD_REGEX);
 		
 		Matcher matcher = regexPattern.matcher("");
 		assertFalse(matcher.matches());
 		
 		matcher = regexPattern.matcher("0,0:true-COVERED");
 		assertTrue(matcher.matches());
 	}
 	
 	@Test
 	public void testLoadRegexForMatch() {
 		Pattern regexPattern = Pattern.compile(FilePersistenceStrategy.SQUARE_LOAD_REGEX);
 		
 		Matcher matcher = regexPattern.matcher("0,0:true-COVERED");
 		assertTrue(matcher.matches());
 	}	
 	
 	@Test
 	public void testLoadRegexForMatchingGroups() {
 		Pattern regexPattern = Pattern.compile(FilePersistenceStrategy.SQUARE_LOAD_REGEX);
 		
 		Matcher matcher = regexPattern.matcher("0,0:true-COVERED");
 		assertEquals(4, matcher.groupCount());
 		
 		assertTrue(matcher.find());
 		assertEquals("0", matcher.group(1));
 		assertEquals("0", matcher.group(2));
 		assertEquals("true", matcher.group(3));
 		assertEquals("COVERED", matcher.group(4));
 	}
 	
 	@Test
 	public void testSave() throws Exception {
 		
 		
 		
 		
 		//set expectations for the mock Writer
 		BufferedReader reader = createMock(BufferedReader.class);
 		PrintWriter writer = createMock(PrintWriter.class);
 		Capture<String> captureOfString = new Capture<String>(CaptureType.ALL);
 		writer.println(capture(captureOfString));
 		//Note: It is last call 36 times, and NOT last call + 36 times
 		expectLastCall().times(36);
		writer.close();
 		replay(writer);
 					
 		MockFileConnectionFactory mockFileConnectionFactory = new MockFileConnectionFactory(reader, writer);
 		PersistenceStrategy filePersistenceStrategy = new FilePersistenceStrategy(mockFileConnectionFactory);
 		
 		filePersistenceStrategy.save(expectedSquares);
 		verify(writer);
 		List<String> squaresAsText = captureOfString.getValues();
 		for(int i=0; i<expectedSquaresAsText.length; i++) {
 			assertEquals(expectedSquaresAsText[i], squaresAsText.get(i));
 		}
 	}
 	
 	@Test
 	public void testLoad() throws Exception {
 		PrintWriter writer = createMock(PrintWriter.class);
 		BufferedReader reader = createMock(BufferedReader.class);
 		MockFileConnectionFactory mockFileConnectionFactory = new MockFileConnectionFactory(reader, writer);
 		PersistenceStrategy persistence = new FilePersistenceStrategy(mockFileConnectionFactory);
 		
 		//set expectations for the mock reader
 		for(String line : expectedSquaresAsText) {
 			expect(reader.readLine()).andReturn(line);
 		}
 		expect(reader.readLine()).andReturn(null);
 		reader.close();
 		replay(reader);
 		
 		
 		Square squares[][] = persistence.load();
 		assertNotNull(squares);
 		for(int row=0; row<Board.MAX_ROWS; row++) {
 			for(int col=0; col<Board.MAX_COLS; col++) {
 				assertEquals(this.expectedSquares[row][col], squares[row][col]);
 			}
 		}
 	}
 	
 	
 	private static Square cSq(Square.STATUS status, boolean mine) {
 		Square square = new Square();
 		square.setStatus(status);
 		square.setMine(mine);
 		return square;
 	}
 
 }
