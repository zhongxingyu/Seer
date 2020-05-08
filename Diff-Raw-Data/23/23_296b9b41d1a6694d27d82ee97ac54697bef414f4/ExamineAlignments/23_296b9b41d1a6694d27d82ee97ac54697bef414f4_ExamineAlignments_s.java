 package test;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 
 import cobsScripts.WriteScores;
 
 import parsingGrouping.HelixSheetGroup;
 import utils.ConfigReader;
 import covariance.datacontainers.Alignment;
 import covariance.datacontainers.PdbFileWrapper;
 import covariance.parsers.PfamParser;
 import covariance.parsers.PfamToPDBBlastResults;
 
 public class ExamineAlignments
 {
 	public static void main(String[] args) throws Exception
 	{
 		PfamParser parser = new PfamParser();
 		HashMap<String, PfamToPDBBlastResults> map = PfamToPDBBlastResults.getAnnotationsAsMap();
 		
 		int numTried =0;
 		
 		while( true)
 		{
 			Alignment a = parser.getNextAlignment();
 			
 			if( ! parser.isFinished())
 			{
				System.out.println(a.getAligmentID());
 				
 				numTried++;
 				PfamToPDBBlastResults toPdb = map.get(a.getAligmentID());
 				
 				try {
 					String pdbInQuestion = toPdb.getPdbID();
 					
 					PdbFileWrapper fileWrapper = new PdbFileWrapper(pdbInQuestion);
 						
 						HashMap<Integer, Integer> pdbToAlignmentNumberMap=  WriteScores.getPdbToAlignmentNumberMap(a, toPdb, fileWrapper);
 						List<HelixSheetGroup> helixSheetGroup= 
 								HelixSheetGroup.getList(ConfigReader.getPdbDir() + File.separator + toPdb.getPdbID() + ".txt",
 										toPdb.getChainId(), toPdb.getQueryStart(), toPdb.getQueryEnd());
 				} catch (NullPointerException e) {
 					// TODO Auto-generated catch block
 					System.err.println("We seemed to have an issue parsing for a particular PDB on this family, skipping");
 				}
 			}
 			else
 			{
 				System.out.println("Finished " + numTried );
 				System.exit(1);
 			}
 			
 		} 
 		
 	}
 }
