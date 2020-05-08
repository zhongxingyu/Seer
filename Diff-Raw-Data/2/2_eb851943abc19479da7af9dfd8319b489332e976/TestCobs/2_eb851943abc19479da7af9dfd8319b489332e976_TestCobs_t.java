 package test;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import parsingGrouping.COBS;
 
 import utils.MapResiduesToIndex;
 import utils.Pearson;
 
 import covariance.algorithms.McBASCCovariance;
 import covariance.datacontainers.Alignment;
 import covariance.datacontainers.AlignmentLine;
 import junit.framework.TestCase;
 
 public class TestCobs extends TestCase
{ 
 	private static Random RANDOM = new Random();
 		//new Random(3432421);  // seed is there for consistent results
 	
 	public void testCobs() throws Exception
 	{
 		int seqLength =100;
 		int[][] substitutionMatrix = McBASCCovariance.getMaxhomMetric();
 		
 		List<AlignmentLine> list = new ArrayList<AlignmentLine>();
 		String s1 = getRandomProtein(seqLength);
 		String s2 = getRandomProtein(seqLength);
 		
 		for( int x=0; x < 5; x++)
 			list.add( new AlignmentLine(">R1_", getRandomProtein(seqLength)));
 		
 		for(int x=0; x < 10;x++)
 			list.add(new AlignmentLine(">S1" + x, s1));
 		
 		for(int x=11; x < 20;x++)
 			list.add(new AlignmentLine(">S2" + x, s2));
 		
 		for( int x=0; x < 5; x++)
 			list.add( new AlignmentLine(">R2_", getRandomProtein(seqLength)));
 
 		Alignment a = new Alignment("A", list);
 		double testImp = getCobs(a, 5, 21, 48, 64, substitutionMatrix);
 		System.out.println("Test implementation of COBS = " + testImp );
 		
 		COBS cobs = new COBS();
 		double testFromCobs = cobs.getScore(a, 5, 21, 48, 64);
 		System.out.println("Value from previous implementation = " + testFromCobs );
 		
 		assertEquals(testImp, testFromCobs,0.00001);
 		
 	}
 	
 	private double getCobs( Alignment a, int startPosLeft, int endPosLeft, int startPosRight, int endPosRight,
 			int[][] subMatrix)
 		throws Exception
 	{
 		List<Double> vectorI = new ArrayList<Double>();
 		List<Double> vectorJ = new ArrayList<Double>();
 		
 		startPosLeft--;  startPosRight--;
 		
 		for( int x=0; x < a.getNumSequencesInAlignment() -1; x++)
 		{
 			String leftTopString= a.getAlignmentLines().get(x).getSequence().substring(startPosLeft, endPosLeft);
 			String rightTopString = a.getAlignmentLines().get(x).getSequence().substring(startPosRight, endPosRight);
 			
 			for( int y=x+1; y < a.getNumSequencesInAlignment(); y++)
 			{
 				String leftbottomString= a.getAlignmentLines().get(y).getSequence().substring(startPosLeft, endPosLeft);
 				String rightBottomString = a.getAlignmentLines().get(y).getSequence().substring(startPosRight, endPosRight);
 				
 				vectorI.add( COBS.getSubstitutionMatrixSum(leftTopString, leftbottomString, subMatrix));
 				vectorJ.add( COBS.getSubstitutionMatrixSum(rightTopString, rightBottomString, subMatrix));
 			}
 		}
 			
 		
 		return Pearson.getPearsonR(vectorI, vectorJ);
 	}
 	
 	
 	static String getRandomProtein(int length) throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < length;x++)
 			buff.append("" + MapResiduesToIndex.getChar(RANDOM.nextInt(20)));
 		
 		return buff.toString();
 	}
 	
 	
 	
 }
