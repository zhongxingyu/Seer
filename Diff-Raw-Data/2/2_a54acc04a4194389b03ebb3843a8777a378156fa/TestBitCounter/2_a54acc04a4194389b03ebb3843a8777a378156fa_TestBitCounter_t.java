 package test.testCoPhylog;
 
 import java.util.Random;
 
 import utils.Translate;
 import junit.framework.TestCase;
 import coPhylog.BitHolder;
 
 public class TestBitCounter extends TestCase
 {
 	public void testMultipleStopsReverseTranscribe() throws Exception
 	{
 		String s1 = getRandomString(8);
 		String s2 = getRandomString(8);
 		String s3 = getRandomString(8);
 		
		String allString = "X" + s1 + "X" + s2 + "XX" + s3;
 
 		BitHolder bh = new BitHolder(3);
 		bh.setToString(allString, true);
 		allString = Translate.safeReverseTranscribe(allString);
 		System.out.println(allString);
 		
 		assertEquals(bh.getJoinedSequence(), allString.substring(0,7));
 		assertTrue(bh.advance());
 		assertEquals(bh.getJoinedSequence(), allString.substring(1,8));
 		
 		assertTrue(bh.advance());
 		
 		assertEquals( bh.getJoinedSequence(), allString.substring(10,17));
 
 		assertTrue(bh.advance());
 		assertEquals( bh.getJoinedSequence(), allString.substring(11,18));
 		
 		assertTrue(bh.advance());
 		assertEquals( bh.getJoinedSequence(), allString.substring(19,26));
 		assertTrue(bh.advance());
 		assertEquals( bh.getJoinedSequence(), allString.substring(20,27));
 		
 		assertFalse(bh.advance());
 
 	}
 	
 	public void testMultipleStops() throws Exception
 	{
 		String s1 = getRandomString(8);
 		String s2 = getRandomString(8);
 		String s3 = getRandomString(8);
 		
 		String allString = s1 + "X" + s2 + "XX" + s3;
 		
 		BitHolder bh = new BitHolder(3);
 		bh.setToString(allString, false);
 		
 		assertEquals(bh.getJoinedSequence(), allString.substring(0,7));
 		assertTrue(bh.advance());
 		assertEquals(bh.getJoinedSequence(), allString.substring(1,8));
 		
 		assertTrue(bh.advance());
 		assertEquals(bh.getJoinedSequence(), s2.substring(0,7));
 
 		assertTrue(bh.advance());
 		assertEquals(bh.getJoinedSequence(), s2.substring(1,8));
 		
 		assertTrue(bh.advance());
 		assertEquals(bh.getJoinedSequence(), s3.substring(0,7));
 
 		assertTrue(bh.advance());
 		assertEquals(bh.getJoinedSequence(), s3.substring(1,8));
 
 		assertFalse(bh.advance());
 		
 	}
 	
 	public void testLong() throws Exception
 	{
 		String s= getRandomString(100);
 		
 		BitHolder bh = new BitHolder(13);
 		bh.setToString(s, false);
 		
 		for( int x=0; x< 100 - (13*2 +1); x++)
 		{
 			assertEquals(bh.getJoinedSequence(), s.substring(x, x+27));
 			assertTrue(bh.advance());
 		}
 		
 		assertFalse(bh.advance());
 		
 		
 		// set to reverseTranscribe
 		bh.setToString(s, true);
 		s = Translate.reverseTranscribe(s);
 		
 		for( int x=0; x< 100 - (13*2 +1); x++)
 		{
 			assertEquals(bh.getJoinedSequence(), s.substring(x, x+27));
 			assertTrue(bh.advance());
 		}
 		
 		assertFalse(bh.advance());
 		
 	}
 	
 	private String getRandomString(int length) throws Exception
 	{
 		Random random = new Random();
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < length; x++)
 		{
 			int val = random.nextInt(4);
 			
 			if( val ==0)
 				buff.append("A");
 			else if ( val == 1)
 				buff.append("C");
 			else if ( val == 2)
 				buff.append("G");
 			else if ( val == 3)
 				buff.append("T");
 			else throw new Exception("No");
 			
 				
 		}
 		
 		return buff.toString();
 	}
 	
 	public void testReverseTranscribe() throws Exception
 	{
 		String s = "ACCTTTACGGGGAAAGGTTAACCCAA";
 		String reverseS = Translate.reverseTranscribe(s);
 		
 		BitHolder bit1 = new BitHolder(5);
 		BitHolder bit2 = new BitHolder(5);
 		
 		bit1.setToString(s, true);
 		bit2.setToString(reverseS, false);
 		
 		assertEquals(bit1.getJoinedSequence(), bit2.getJoinedSequence());
 		
 		assertEquals(bit1.getJoinedSequence(), reverseS.substring(0, 11));
 		
 		assertEquals(bit1.getBits(), bit2.getBits());
 		
 		for(int x=0; x < 15; x++)
 		{
 			assertEquals(bit1.getJoinedSequence(), bit2.getJoinedSequence());
 			assertEquals(bit1.getJoinedSequence(), reverseS.substring(x, x+11));
 			assertEquals(bit1.getBits(), bit2.getBits());
 			assertEquals( bit1.getMiddleChar(), bit2.getMiddleChar() );
 			assertEquals(bit2.getIndex(), x + 10);
 			assertTrue( bit1.advance());
 			assertTrue( bit2.advance());
 		}
 		
 		assertEquals(bit1.getJoinedSequence(), bit2.getJoinedSequence());
 		assertFalse(bit1.advance());
 		assertFalse(bit2.advance());
 	}
 
 	public void testInvalidChars() throws Exception
 	{
 		BitHolder bh = new BitHolder(2);
 		assertEquals(bh.getContextSize(), 2);
 		
 		String s= "TTXCCGCCT";
 		
 		assertTrue( bh.setToString(s,false));
 		
 		assertEquals(bh.getMiddleChar(), 'G');
 		assertEquals(bh.getNumValidChars(),5);
 		assertEquals(bh.getIndex(),7);
 		
 		long cBase = 0x1l;
 		long expectedAnswer = cBase<< 2;
 		expectedAnswer = expectedAnswer | cBase;
 		expectedAnswer = expectedAnswer << 32 + (32-4);
 		
 		long rightAnswer = cBase;
 		rightAnswer = cBase<< 2;
 		rightAnswer= rightAnswer | cBase;
 		rightAnswer = rightAnswer << (32-4);
 		
 		expectedAnswer = expectedAnswer | rightAnswer;
 		assertEquals(expectedAnswer, bh.getBits());
 		
 		
 		assertTrue(bh.advance());
 		assertEquals(bh.getIndex(), 8);
 		assertEquals(bh.getMiddleChar(),'C');
 		
 		//CGCT
 		expectedAnswer = ( 0x01l << 2 ) | (0x02l );
 		expectedAnswer = expectedAnswer << 32 + (32-4);
 		rightAnswer = ( 0x01l << 2 ) | (0x03l );
 		rightAnswer = rightAnswer << (32-4);
 		expectedAnswer = expectedAnswer | rightAnswer;
 		assertEquals(expectedAnswer, bh.getBits());
 		
 		assertFalse(bh.advance());
 	}
 	
 	public void testAdvance() throws Exception
 	{
 		BitHolder bh = new BitHolder(2);
 		assertEquals(bh.getContextSize(), 2);
 		
 		String s= "TTACCG";
 		
 		assertEquals( bh.setToString(s,false), true);
 		assertEquals( bh.getNumValidChars(), 5);
 		assertEquals( bh.getMiddleChar(), 'A');
 		assertEquals( bh.getIndex(), 4);
 		
 		
 		long tBase= 0x03l;
 		long expectedAnswer = tBase<< 2;
 		expectedAnswer = expectedAnswer | tBase;
 		expectedAnswer = expectedAnswer << 32 + (32-4);
 		
 		long cBase= 0x01l;
 		long rightAnswer = cBase<< 2;
 		rightAnswer= rightAnswer | cBase;
 		rightAnswer = rightAnswer << (32-4);
 		
 		expectedAnswer = expectedAnswer | rightAnswer;
 		assertEquals(expectedAnswer, bh.getBits());
 		
 		assertEquals( bh.advance(), true); 
 		assertEquals( bh.getNumValidChars(), 6);
 		assertEquals( bh.getMiddleChar(), 'C');
 		assertEquals(bh.getIndex(), 5);
 		assertEquals(s.charAt(bh.getIndex()), 'G');
 		
 		
 		expectedAnswer = tBase;
 		expectedAnswer = expectedAnswer | tBase;
 		expectedAnswer = expectedAnswer << 32 + (32-2);
 		
 		rightAnswer = cBase;
 		rightAnswer = rightAnswer << 2;
 		rightAnswer = rightAnswer | 0x2l;
 		rightAnswer = rightAnswer << (32-4);
 		
 		expectedAnswer = expectedAnswer | rightAnswer;
 		
 		assertEquals(expectedAnswer, bh.getBits());
 		
 		assertEquals( bh.advance(), false );
 	}
 	
 	public void testInitial() throws Exception
 	{
 		BitHolder bh = new BitHolder(3);
 		
 		String s= "CCCATTTCCCCCCCCCCCCC";
 		
 		
 		assertEquals( bh.setToString(s,false),true);
 		
 		assertEquals(bh.getIndex(), 6);
 		assertEquals(bh.getNumValidChars(),7);
 		assertEquals(bh.getMiddleChar(),'A');
 		
 		//System.out.println( Long.toBinaryString(bh.getBits()));
 		
 		long cBase= 0x01l;
 		long expectedAnswer = cBase<< 2;
 		expectedAnswer = expectedAnswer | cBase;
 		expectedAnswer = expectedAnswer << 2;
 		expectedAnswer = expectedAnswer | cBase;
 		expectedAnswer = expectedAnswer << (32-6);
 		expectedAnswer = expectedAnswer << (32);
 		
 		long tBase = 0x03l;
 		long rightAnswer = tBase << 2;
 		rightAnswer = rightAnswer | tBase;
 		rightAnswer = rightAnswer << 2;
 		rightAnswer = rightAnswer | tBase;
 		rightAnswer = rightAnswer << (32-6);
 		
 		expectedAnswer = expectedAnswer | rightAnswer;
 		assertEquals(expectedAnswer, bh.getBits());
 		assertEquals(bh.getContextSize(), 3);
 	}
 }
