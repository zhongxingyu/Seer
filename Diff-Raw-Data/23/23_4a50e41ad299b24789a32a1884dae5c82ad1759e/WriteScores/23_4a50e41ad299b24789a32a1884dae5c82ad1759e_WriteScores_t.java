 package averagesAndOutput;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import parsingGrouping.HelixSheetGroup;
 import parsingGrouping.COBS;
 import parsingGrouping.PfamToPDB;
 
 import utils.ConfigReader;
 
 
 import covariance.algorithms.ConservationSum;
 import covariance.algorithms.MICovariance;
 import covariance.algorithms.PNormalize;
 import covariance.algorithms.RandomScore;
 import covariance.datacontainers.Alignment;
 import covariance.datacontainers.AlignmentLine;
 import covariance.datacontainers.PdbChain;
 import covariance.datacontainers.PdbFileWrapper;
 import covariance.datacontainers.PdbResidue;
 import covariance.parsers.PfamParser;
 import dynamicProgramming.MaxhomSubstitutionMatrix;
 import dynamicProgramming.NeedlemanWunsch;
 import dynamicProgramming.PairedAlignment;
 
 public class WriteScores
 {
 	public static final int MIN_PDB_LENGTH = 80;
 	public static final double MIN_PERCENT_IDENTITY= 90;
 	private static MaxhomSubstitutionMatrix substitutionMatrix;
 	private static final int NUM_THREADS = 8;
 	
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
 	
 	public static void main(String[] args) throws Exception
 	{
 		HashMap<String, PfamToPDBAnnotations> pfamToPdbmap = PfamToPDBAnnotations.getAnnotationsAsMap();
 		HashMap<String, PfamToPDB> otherPfamMap = PfamToPDB.getMapByName();
 		Semaphore semaphore = new Semaphore(NUM_THREADS);
 		
 		for(String s : pfamToPdbmap.keySet())
 			System.out.println(s);
 			
		PfamParser parser = new PfamParser();
 
 		for(Alignment a=  parser.getNextAlignment();
 					a != null;
 						a = parser.getNextAlignment())
 		{
 			PfamToPDBAnnotations toPdb = pfamToPdbmap.get(a.getAligmentID());
 			PfamToPDB otherPdb = otherPfamMap.get(a.getAligmentID());
 		
 			if( toPdb != null && (toPdb.getQueryEnd() - toPdb.getQueryStart()) >= MIN_PDB_LENGTH 
 						&& toPdb.getPercentIdentity() >= MIN_PERCENT_IDENTITY  )
 			{
 				kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, new COBS());
 				kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, new AverageMcBASC(a));
 				//kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, new MaxMcBASC(a));
 				kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, 
 					new AverageScoreGenerator(new PNormalize(new RandomScore(a.getAligmentID() +"_random",a))));
 				
 				kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, 
 						new AverageScoreGenerator(new PNormalize(new MICovariance(a))));
 				
 				kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, 
 						new AverageScoreGenerator(new MICovariance(a)));											
 				kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, 
 						new AverageScoreGenerator(new ConservationSum(a)));
 				
 				kickOneOffIfFileDoesNotExist(semaphore, a, toPdb, otherPdb, 
 						new AverageScoreGenerator(new PNormalize(new ConservationSum(a))));
 			}
 			else
 			{	//Quite verbose -- debug only
 				//System.err.println("There was an issue with the PDB qualifying for parsing.  This was either because no pdb was unable to be parsed, the percent identity was too low, or the length minimum not met.");
 			}
 		}
 		
 		int num = NUM_THREADS;
 		
 		while( num > 0)
 		{
 			semaphore.acquire();
 			num--;
 		}
 		
 		System.out.println("Finished");
 
 	}
 	
 	/*
 	 * The syncrhonized is just to force all threads to the most up-to-date view of the data
 	 */
 	private static synchronized void kickOneOffIfFileDoesNotExist(Semaphore semaphore, Alignment a, PfamToPDBAnnotations toPdb, 
 			PfamToPDB otherPdb, GroupOfColumnsInterface gci) throws Exception
 	{
 		File outFile = getOutputFile(a, gci);
 		
 		if(! outFile.exists())
 		{
 			semaphore.acquire();
 			Worker w = new Worker(a,toPdb,otherPdb,gci, semaphore);
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
 		return new File(ConfigReader.getCleanroom() + File.separator + "results" + File.separator + 
 				a.getAligmentID() + "_" + gci.getName() + ".txt");
 	}
 	
 	private static class Worker implements Runnable
 	{
 		private final Alignment a;
 		private final PfamToPDBAnnotations toPDB;
 		private final PfamToPDB otherToPdb;
 		private final GroupOfColumnsInterface gci;
 		private final Semaphore semaphore;
 		
 		private Worker(Alignment a, PfamToPDBAnnotations toPDB,
 				PfamToPDB otherToPdb, GroupOfColumnsInterface gci, Semaphore semaphore)
 		{
 			this.a = a;
 			this.toPDB = toPDB;
 			this.otherToPdb = otherToPdb;
 			this.gci = gci;
 			this.semaphore = semaphore;
 		}
 		
 		public void run()
 		{
 			//So that we ALWAYS have something to close
 			BufferedWriter writer = null;
 			Boolean closedAsExpected = false;
 			try
 			{
 				PdbFileWrapper fileWrapper = new PdbFileWrapper( new File( ConfigReader.getPdbDir() + File.separator + toPDB.getPdbID() + ".txt"));
 				
 				File outputFile = getOutputFile(a, gci);
 				
 				semaphore.release();
 				
 				if(outputFile.exists())
 					throw new Exception(outputFile.getAbsolutePath() + " already exists ");
 				
 				writer = new BufferedWriter(new FileWriter(outputFile));
 				
 				writer.write("region1\tregion2\tcombinedType\tscore\taverageDistance\tminDistance\n");
 				writer.flush();
 				
 				System.out.println(a.getAligmentID());
 				HashMap<Integer, Integer> pdbToAlignmentNumberMap=  getPdbToAlignmentNumberMap(a, toPDB, otherToPdb, fileWrapper);
 				List<HelixSheetGroup> helixSheetGroup= 
 						HelixSheetGroup.getList(ConfigReader.getPdbDir() + File.separator + toPDB.getPdbID() + ".txt",
 								otherToPdb.getChainID(), toPDB.getQueryStart(), toPDB.getQueryEnd());
 				System.out.println(helixSheetGroup);
 				
 				
 				
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
 								getAverageDistance(fileWrapper, xHSG, yHSG, otherToPdb.getChainID());
 						
 						writer.write(distance + "\t");
 						
 						double minDistance = 
 								getMinDistance(fileWrapper, xHSG, yHSG, otherToPdb.getChainID());
 						
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
 			catch(java.io.IOException ex)
 			{
 				System.out.println("End of computation reached, closing " + a.getAligmentID());
 				closedAsExpected=true;
 				ex.printStackTrace();
 				System.exit(1);
 			}
 			catch(Exception ex)
 			{
 				System.out.println("Unknown error for " + a.getAligmentID());
 				ex.printStackTrace();
 				System.exit(1);
 			}
 			finally
 			{
 				try {
 					if (closedAsExpected == false){
 						writer.flush();
 						writer.close();
 					}
 
 				} catch (IOException e) {
 					//This became too verbose, this is basically happening when we have already
 					//closed a file correctly, but our "second try" didn't work because the first one did
 					//Originally written to track down a memory leak, which we are no longer seeing  --- Debug only
 					//System.out.println("INFO:  We are closing this alignment file from a catch block " + a.getAligmentID());
 				}
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
 	
 	public static HashMap<Integer, Integer> getPdbToAlignmentNumberMap( Alignment a, PfamToPDBAnnotations toPDB,
 			 PfamToPDB otherToPdb, PdbFileWrapper fileWrapper) throws Exception
 	{
 		HashMap<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
 		AlignmentLine aLine = a.getAnAlignmentLine(toPDB.getPfamLine());
 		String alignmentSeq = aLine.getSequence().toUpperCase();
 		
 		alignmentSeq = alignmentSeq.replaceAll("-", ".");
 		
 		if(alignmentSeq.indexOf("-") != -1)
 			throw new Exception("No " + alignmentSeq);
 		
 		String pdbSeq;
 		PdbChain c = null;
 		int start = 1111111111;
 		int end = 222222222;
 		try {
 			c = fileWrapper.getChain(otherToPdb.getChainID());
 			start = toPDB.getQueryStart()-1;
 			end = toPDB.getQueryEnd();
 			pdbSeq = fileWrapper.getChain(otherToPdb.getChainID()).getSequence().substring(toPDB.getQueryStart()-1,toPDB.getQueryEnd());
 		} catch (StringIndexOutOfBoundsException e) {
 			throw new Exception("Chain was " + c + "\n" + "Start and end of query PDB were: " + start + " "  + end  + "\n" +  alignmentSeq);
 		}
 		
 		pdbSeq = fileWrapper.getChain(otherToPdb.getChainID()).getSequence().substring(toPDB.getQueryStart()-1,
 				toPDB.getQueryEnd());
 		
 		PairedAlignment pa = 
 				NeedlemanWunsch.globalAlignTwoSequences(
 						pdbSeq, alignmentSeq, substitutionMatrix, -3, -1, false);
 		
 		System.out.println(pa.getFirstSequence());
 		System.out.println(pa.getSecondSequence());
 		
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
 				
 				map.put(pdbNumber,Math.max(0, alignmentPos));
 			}
 			
 		}
 		
 		return map;
 	}
 }
