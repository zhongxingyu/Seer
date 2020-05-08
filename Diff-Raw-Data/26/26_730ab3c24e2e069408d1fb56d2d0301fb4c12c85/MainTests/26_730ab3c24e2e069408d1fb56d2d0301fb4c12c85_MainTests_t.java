 import java.io.IOException;
 import java.util.ArrayList;
 
 
 public class MainTests
 {
 
 	public static void main(String [ ] args) throws IOException
 	{
 		int select;
 		System.out.println("Pick a format: 1. CVS. 2. Text");
 		select = System.in.read();
 		
 		switch(select)
 		{
 			case('1'): 
 			{
 				testCVS();break;
 			}
 			case('2'):
 			{
 				testText(); break;
 			}
 		}
 	}
 	
 	static void testCVS()
 	{
 		ArrayList<Vector> vecList = MyParser.parseNumCSV("data/numFile.txt");
 		System.out.println("Euclidean distance for 1st vector <1, 2, 3, 5>: " + 
				VectorMath.getEucledianDistance(vecList.get(0)));
 		System.out.println("Euclidean distance between 1st and 2nd vectors: " + 
				VectorMath.getEucledianDistance(vecList.get(0), vecList.get(1)));
 		
 		System.out.println("Dimensional length of 1st vector <1, 2, 3, 5>: " + 
				VectorMath.getDimensionLength(vecList.get(0)));
 		System.out.println("Dot product of first two vectors: " + 
				VectorMath.getDotProduct(vecList.get(0), vecList.get(1)));
 		System.out.println("Manhattan Distance of 2 vectors: " + 
				VectorMath.getManhattanDistance(vecList.get(0), vecList.get(1)));
 		System.out.println("Pearson Correlation of first two vectors: " + 
				VectorMath.getPearsonCorrelation(vecList.get(0), vecList.get(1)));
 		System.out.println(" ");
 		
 		VectorMath.LSMrow(vecList.get(1));
 		VectorMath.LSMcolumn(vecList, 1);
 		
 		System.out.println("Standard Deviation for 1st vector <1, 2, 3, 5>: " + 
				VectorMath.standardDeviation(vecList.get(0)));
 		System.out.println("Standard Deviation for 2nd column: " + 
 				VectorMath.standardDeviation(vecList, 1));
 		
 		
 	}
 	
 	static void testText()
 	{
		ArrayList<TextToken> lis = MyParser.parseTextFile("data/HuckFinn.txt");
 		WordLists wl = new WordLists(lis);
 		
 		System.out.println("Found " + wl.uniqueWords() + " unique words");
 		System.out.println("Found " + wl.totalWords() + " total words");
 		System.out.println("Found " + wl.numSentences() + " total sentences");
 		System.out.println("Found " + wl.numParagraphs() + " total paragraphs");
 		System.out.println(" ");
 		
 		wl.pFreqAtLeast(2);
 		wl.pFreqAt(2);
 		wl.pMostCommon(10);
 		
 		System.out.println("Finding word Don't: "+ wl.findWord("Don't"));
 		System.out.println("Finding word object: "+ wl.findWord("object"));
 		System.out.println("Finding word Filby: "+ wl.findWord("Filby"));
 		System.out.println("Finding word Alfalfa: "+ wl.findWord("Alfalfa"));
 		
 		System.out.println("Finding words solid and body in sentence (they are): "
 				+ wl.findTwoWords("solid", "body"));
 		System.out.println("Finding words wait and cube in sentence(they aren't): "
				+ wl.findTwoWords("wait", "cube"));
 		System.out.println("Finding words solid and cucumber in sentence(they aren't): "
 				+ wl.findTwoWords("solid", "cucumber"));
 		
 	
 	}
 }
