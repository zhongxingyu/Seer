 package cobsScripts;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 import java.util.zip.GZIPOutputStream;
 
 import parsingGrouping.HelixSheetGroup;
 import parsingGrouping.COBS;
 
 import utils.ConfigReader;
 import utils.MapResiduesToIndex;
 
 import covariance.algorithms.ConservationSum;
 import covariance.algorithms.FileScoreGenerator;
 import covariance.algorithms.MICovariance;
 import covariance.algorithms.McBASCCovariance;
 import covariance.algorithms.PNormalize;
 import covariance.algorithms.RandomScore;
 import covariance.datacontainers.Alignment;
 import covariance.datacontainers.AlignmentLine;
 import covariance.datacontainers.PdbFileWrapper;
 import covariance.datacontainers.PdbResidue;
 import covariance.parsers.PfamParser;
 import covariance.parsers.PfamToPDBBlastResults;
 import dynamicProgramming.MaxhomSubstitutionMatrix;
 import dynamicProgramming.NeedlemanWunsch;
 import dynamicProgramming.PairedAlignment;
 
 public class WriteScores
 {
 	public static final int MIN_PDB_LENGTH = 80;
 	public static final double MIN_PERCENT_IDENTITY= 90;
 	public static MaxhomSubstitutionMatrix substitutionMatrix;
 	
 	static
 	{
 		try
 		{
 			substitutionMatrix = new MaxhomSubstitutionMatrix();
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 			System.exit(1);
 		}
 		
 	}
 	
 	
 	private static File getOneDFileName(Alignment a, String type) throws Exception
 	{
 		File file =  new File(ConfigReader.getCleanroom() + File.separator + "results" + File.separator + 
 				"oneD" + File.separator + 
 				a.getAligmentID() + "_" + type+ ".txt");
 		
 		if( ! file.exists())
 		{
 			file =   new File(ConfigReader.getCleanroom() + File.separator + "results" + File.separator + 
 					"oneD" + File.separator + 
 					a.getAligmentID() + "_" + type + ".txt.gz");
 		}
 		
 		return file;
 	}
 	
 	
 	private static FileScoreGenerator getOneDFileOrNull(Alignment a, String type) throws Exception
 	{
 		File file = getOneDFileName(a, type);
 		
 		if (! file.exists())
 		{
 			System.out.println("Could not find " + file.getAbsolutePath());
 			return null;
 			
 		}
 			
		FileScoreGenerator fsg = new FileScoreGenerator("McBASC", file, a);
 		
 		if( a.getNumColumnsInAlignment() * (a.getNumColumnsInAlignment()-1) / 2 != fsg.getNumScores() )
 		{
 			System.out.println("Truncated " + file.getAbsolutePath());			
 			return null;
 			
 		}
 			
 		return fsg;
 	}
 	
 	
 	public static void main(String[] args) throws Exception
 	{
 		HashMap<String, PfamToPDBBlastResults> pfamToPdbmap = PfamToPDBBlastResults.getAnnotationsAsMap();
 		
 		System.out.println(pfamToPdbmap.keySet());
 		
 		int numThreads = ConfigReader.getNumThreads();
 		Semaphore semaphore = new Semaphore(numThreads);
 		
 		PfamParser parser = new PfamParser();
 
 		for(Alignment a=  parser.getNextAlignment();
 					a != null;
 						a = parser.getNextAlignment())
 		{
 			PfamToPDBBlastResults toPdb = pfamToPdbmap.get(a.getAligmentID());
 			
 			System.out.println("Trying " + a.getAligmentID());
 			
 			if( toPdb != null && (toPdb.getQueryEnd() - toPdb.getQueryStart()) >= MIN_PDB_LENGTH 
 						&& toPdb.getPercentIdentity() >= MIN_PERCENT_IDENTITY  )
 			{
 				FileScoreGenerator mcbascFSG = getOneDFileOrNull(a, McBASCCovariance.MCBASC_ANALYSIS);
 				
 				if( mcbascFSG != null)
 				{
 					System.out.println("Starting " + a.getAligmentID());
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, 
 							new AverageScoreGenerator(mcbascFSG));
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, new COBS());
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb,
 						new AverageScoreGenerator(getOneDFileOrNull(a, RandomScore.RANDOM_NAME)));
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, 
 							new AverageScoreGenerator(new PNormalize(new MICovariance(a))));
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, 
 							new AverageScoreGenerator(new MICovariance(a)));											
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, 
 							new AverageScoreGenerator(new ConservationSum(a)));
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, 
 							new AverageScoreGenerator(new PNormalize(new ConservationSum(a))));
 					
 					kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, 
 							new AverageScoreGenerator( new PNormalize(mcbascFSG)));
 				}
 				else
 				{	
 					System.out.println("Skipping " + a.getAligmentID());
 				}
 			}
 		}
 		
 		
 		while( numThreads > 0)
 		{
 			semaphore.acquire();
 			numThreads--;
 		}
 		
 		System.out.println("Finished");
 
 	}
 	
 	/*
 	 * The syncrhonized is just to force all threads to the most up-to-date view of the data
 	 */
 	private static synchronized void kickOneOffIfFileDoesNotExist(Semaphore semaphore, Alignment a, PfamToPDBBlastResults toPdb, 
 			GroupOfColumnsInterface gci) throws Exception
 	{
 		File outFile = getOutputFile(a, gci);
 		
 		if(! outFile.exists())
 		{
 			semaphore.acquire();
 			Worker w = new Worker(a,toPdb,gci, semaphore);
 				new Thread(w).start();
 		}
 		else
 		{
 			Date date = new Date();
 			String stringDate = date.toString();
 			System.out.println(outFile.getAbsolutePath() + "exists. skipping at " + stringDate);
 		}
 	}
 	
 	private static File getOutputFile(Alignment a, GroupOfColumnsInterface gci) throws Exception
 	{
 		File directory = new File( ConfigReader.getCleanroom() + File.separator + "results"); 
 		
 		directory.mkdirs();
 		
 		return new File( directory.getAbsolutePath() + File.separator + 
 				a.getAligmentID() + "_" + gci.getName() + ".txt" + 
 				(ConfigReader.writeZippedResults() ? ".gz" : "")  );
 	}
 	
 	private static class Worker implements Runnable
 	{
 		private final Alignment a;
 		private final PfamToPDBBlastResults toPDB;
 		private final GroupOfColumnsInterface gci;
 		private final Semaphore semaphore;
 		
 		private Worker(Alignment a, PfamToPDBBlastResults toPDB,
 				GroupOfColumnsInterface gci, Semaphore semaphore)
 		{
 			this.a = a;
 			this.toPDB = toPDB;
 			this.gci = gci;
 			this.semaphore = semaphore;
 		}
 		
 		public void run()
 		{
 			try
 			{
 				PdbFileWrapper fileWrapper = new PdbFileWrapper( new File( ConfigReader.getPdbDir() + File.separator + toPDB.getPdbID() + ".txt"));
 				HashMap<Integer, Integer> pdbToAlignmentNumberMap=  getPdbToAlignmentNumberMap(a, toPDB, fileWrapper);
 				
 				File outputFile = getOutputFile(a, gci);
 				
 				if(outputFile.exists())
 					throw new Exception(outputFile.getAbsolutePath() + " already exists ");
 				
 				BufferedWriter writer = ConfigReader.writeZippedResults() ? 
 						new BufferedWriter(new OutputStreamWriter( 
 								new GZIPOutputStream( new FileOutputStream( outputFile )))) : 
 						new BufferedWriter(new FileWriter(outputFile));
 				
 				writer.write("region1\tregion2\tcombinedType\tscore\taverageDistance\tminDistance\n");
 				writer.flush();
 				
 				System.out.println(a.getAligmentID());
 				List<HelixSheetGroup> helixSheetGroup= 
 						HelixSheetGroup.getList(ConfigReader.getPdbDir() + File.separator + toPDB.getPdbID() + ".txt",
 								toPDB.getChainId(), toPDB.getQueryStart(), toPDB.getQueryEnd());
 				System.out.println(helixSheetGroup);
 				
 				for(HelixSheetGroup hsg : helixSheetGroup)
 				{
 					System.out.println( hsg.getStartPos() + "-" + hsg.getEndPos() + " " +  
 							pdbToAlignmentNumberMap.get(hsg.getStartPos()) +  "-" + 
 							pdbToAlignmentNumberMap.get(hsg.getEndPos()));
 				}
 					
 				
 				for(int x=0; x< helixSheetGroup.size() -1; x++)
 				{
 					HelixSheetGroup xHSG = helixSheetGroup.get(x);
 					
 					for( int y=x+1; y < helixSheetGroup.size(); y++)
 					{
 						HelixSheetGroup yHSG = helixSheetGroup.get(y);
 						
 						writer.write(xHSG.toString() + "\t");
 						writer.write(yHSG.toString() + "\t");
 						List<String> aList = new ArrayList<String>();
 						
 						aList.add(xHSG.getElement());
 						aList.add(yHSG.getElement());
 						Collections.sort(aList);
 						writer.write(aList.get(0) + "_TO_" + aList.get(1) + "\t");
 						
 						double score =
 								gci.getScore(a, pdbToAlignmentNumberMap.get(xHSG.getStartPos()), 
 										   pdbToAlignmentNumberMap.get(xHSG.getEndPos()), 
 										   pdbToAlignmentNumberMap.get(yHSG.getStartPos()),
 										    pdbToAlignmentNumberMap.get(yHSG.getEndPos()));
 						
 						writer.write(score+ "\t");
 						
 						double distance = 
 								getAverageDistance(fileWrapper, xHSG, yHSG, toPDB.getChainId());
 						
 						writer.write(distance + "\t");
 						
 						double minDistance = 
 								getMinDistance(fileWrapper, xHSG, yHSG, toPDB.getChainId());
 						
 						writer.write(minDistance + "\n");
 						writer.flush();
 					}
 				}
 				
 				writer.flush();  
 				writer.close();
 				Date date = new Date();
 				String stringDate = date.toString();
 				System.out.println("Finished " + a.getAligmentID() + "_" + gci.getName() + " at " + stringDate);
 				
 			}
 			catch(Exception ex)
 			{
 				ex.printStackTrace();
 				
 				// todo:  Once all the bugs are out put the hard exit back in!
 				//System.exit(1);
 			}
 			finally
 			{
 				semaphore.release();
 			}
 		}
 	}
 	
 	private static double getAverageDistance( PdbFileWrapper wrapper, HelixSheetGroup xGroup, HelixSheetGroup yGroup, char chain )
 		throws Exception
 	{
 		double n=0;
 		double sum=0;
 		
 		for( int x=xGroup.getStartPos(); x <= xGroup.getEndPos(); x++ )
 		{
 			PdbResidue xResidue = wrapper.getChain(chain).getPdbResidueByPdbPosition(x);
 			
 			if( xResidue == null || xResidue.getCbAtom() == null){
 				//Too verbose -- debug only use
 				System.out.println("WARNING:  " + wrapper.getFourCharId() + " " + chain + "  " +  x);
 			}
 				
 			else
 			{
 				for( int y= yGroup.getStartPos(); y <= yGroup.getEndPos(); y++)
 				{
 					PdbResidue yResidue = wrapper.getChain(chain).getPdbResidueByPdbPosition(y);
 					
 					if( yResidue == null || yResidue.getCbAtom() == null)
 					{
 						//Too verbose -- debug only use
 						//System.out.println("WARNING: NO " + wrapper.getFourCharId() + " " + chain + "  " +  y);
 					}
 					else
 					{
 						sum += xResidue.getCbAtom().getDistance(yResidue.getCbAtom());
 						n++;
 					}		
 				}
 			}
 		}
 		
 		return sum / n;
 	}
 	
 	private static double getMinDistance( PdbFileWrapper wrapper, HelixSheetGroup xGroup, HelixSheetGroup yGroup, char chain )
 			throws Exception
 		{
 			double val = Double.MAX_VALUE;
 			
 			for( int x=xGroup.getStartPos(); x <= xGroup.getEndPos(); x++ )
 			{
 				PdbResidue xResidue = wrapper.getChain(chain).getPdbResidueByPdbPosition(x);
 				
 				if( xResidue == null || xResidue.getCbAtom() == null ){
 					//Too verbose -- debug only use
 					//System.out.println("WARNING: No " + wrapper.getFourCharId() + " " + chain + "  " +  x);
 				}
 					
 				else
 				{
 					for( int y= yGroup.getStartPos(); y <= yGroup.getEndPos(); y++)
 					{
 						PdbResidue yResidue = wrapper.getChain(chain).getPdbResidueByPdbPosition(y);
 						
 						if( yResidue == null || yResidue.getCbAtom() == null)
 						{
 							//Too verbose, removed
 							//System.out.println("WARNING: NO " + wrapper.getFourCharId() + " " + chain + "  " +  y);
 						}
 						else
 						{
 							val = Math.min(xResidue.getCbAtom().getDistance(yResidue.getCbAtom()), val);
 						}		
 					}
 				}
 			}
 			
 			return val;
 		}
 	
 	public static double getFractionIdentity(String pdbString, String pfamString) throws Exception
 	{
 		double num =0;
 		double numMatch=0;
 		
 		for( int x=0; x < pdbString.length(); x++)
 		{
 			char c = pdbString.charAt(x);
 			
 			if ( c != '-')
 			{
 				num++;
 				
 				if( c == pfamString.charAt(x))
 					numMatch++;
 			}
 		}
 		
 		return numMatch / num;
 	}
 	
 	public static HashMap<Integer, Integer> getPdbToAlignmentNumberMap( Alignment a, PfamToPDBBlastResults toPDB,
 			 PdbFileWrapper fileWrapper) throws Exception
 	{
 		HashMap<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
 		AlignmentLine aLine = a.getAnAlignmentLine(toPDB.getPfamLine());
 		String pFamSeq = aLine.getSequence().toUpperCase();
 		
 		HashMap<Integer, Integer> ungappedPfamToGappedPfamMap = new LinkedHashMap<>();
 		
 		StringBuffer ungappedPfam = new StringBuffer();
 		int ungappedPosition =-1;
 		
 		for( int x=0; x< pFamSeq.length(); x++)
 		{
 			char c = pFamSeq.charAt(x);
 			
 			if( MapResiduesToIndex.isValidResidueChar(c) )
 			{
 				ungappedPfam.append(c);
 				ungappedPosition++;
 				ungappedPfamToGappedPfamMap.put(ungappedPosition, x);
 			}
 		}
 		
 		System.out.println(toPDB.getPdbID()+ " " + toPDB.getChainId() + " " + toPDB.getQueryStart() + " " + toPDB.getQueryEnd());
 		System.out.println(fileWrapper.getChain(toPDB.getChainId()).getSequence());
 		System.out.println(fileWrapper.getChain(toPDB.getChainId()).getSequence().length());
 		
 		
 		String pdbSeq = fileWrapper.getChain(toPDB.getChainId()).getSequence().substring(toPDB.getQueryStart()-1,
 																		toPDB.getQueryEnd());
 		
 		pdbSeq = pdbSeq.toUpperCase();
 		
 		PairedAlignment pa = 
 				NeedlemanWunsch.globalAlignTwoSequences(
 						pdbSeq, ungappedPfam.toString(), substitutionMatrix, -3, 0, false);
 		
 		//System.out.println(pa.getFirstSequence());
 		//System.out.println(pa.getSecondSequence());
 		
 		double fractionMatch = getFractionIdentity(pa.getFirstSequence(), pa.getSecondSequence());
 		
 		//System.out.println("fractionMatch = " + fractionMatch);
 		
 		if( fractionMatch < 0.9)
 		{
 			throw new Exception("Alignment failure\n" + pa.getFirstSequence() + "\n"
 						+ pa.getSecondSequence() + "\n" +	fractionMatch + "\n\n" );
 		}
 			
 		int x=-1;
 		int alignmentPos =-1;
 		int pdbNumber = toPDB.getQueryStart() -1;
 		
 		while(pdbNumber < toPDB.getQueryEnd())
 		{
 			x++;
 			
 			if( pa.getSecondSequence().charAt(x) != '-')
 				alignmentPos++;
 			
 			if( pa.getFirstSequence().charAt(x) != '-' )
 			{
 				pdbNumber++;
 				
 				map.put(pdbNumber,ungappedPfamToGappedPfamMap.get(alignmentPos));
 			}
 		}
 		
 		double num=0;
 		double numMatch =0;
 		StringBuffer out = new StringBuffer();
 		for(Integer pdbNum : map.keySet())
 		{
 			num++;
 			char pdbChar = pdbSeq.charAt(pdbNum- toPDB.getQueryStart());
 			char pfamChar = pFamSeq.charAt(map.get(pdbNum));
 			
 			if( pdbChar == pfamChar)
 				numMatch++;
 			
 			out.append(pdbNum + "\t" + pdbChar + "\t" + map.get(pdbNum) + "\t" + pfamChar + "\n");
 			
 		}
 		
 		double postAlignmentMatch = numMatch / num;
 		
 		//System.out.println("Post alignment match = " + postAlignmentMatch);
 		
 		if( postAlignmentMatch <0.9)
 			throw new Exception("Alignment failure\n" + postAlignmentMatch + "\n" + out + "\n\n");
 		
 		return map;
 	}
 }
