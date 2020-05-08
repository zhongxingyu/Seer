 package cobsScripts;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.concurrent.Semaphore;
 
 import utils.ConfigReader;
 import covariance.algorithms.FileScoreGenerator;
 import covariance.algorithms.McBASCCovariance;
 import covariance.algorithms.RandomScore;
 import covariance.algorithms.ScoreGenerator;
 import covariance.datacontainers.Alignment;
 import covariance.parsers.PfamParser;
 import covariance.parsers.PfamToPDBBlastResults;
 
 public class WriteOneDScores
 {
 	public static void main(String[] args) throws Exception
 	{
 		HashMap<String, PfamToPDBBlastResults> pfamToPdbmap = PfamToPDBBlastResults.getAnnotationsAsMap();
 		Semaphore semaphore = new Semaphore(WriteScores.NUM_THREADS);
 		
 		PfamParser parser = new PfamParser();
 
 		for(Alignment a=  parser.getNextAlignment();
 					a != null;
 						a = parser.getNextAlignment())
 		{
 			PfamToPDBBlastResults toPdb = pfamToPdbmap.get(a.getAligmentID());
 			
 			if( toPdb != null &&  (toPdb.getQueryEnd() - toPdb.getQueryStart()) >= WriteScores.MIN_PDB_LENGTH 
 					&& toPdb.getPercentIdentity() >= WriteScores.MIN_PERCENT_IDENTITY  )
 			{
 				// McBASC is all we really need to cache; the other algorithsm are fast
 				//kickOneOffIfFileDoesNotExist(semaphore, a, new ConservationSum(a));
 				//kickOneOffIfFileDoesNotExist(semaphore, a, new MICovariance(a));
 				kickOneOffIfFileDoesNotExist(semaphore, a, new RandomScore());
 				
 				kickOneOffIfFileDoesNotExist(semaphore, a, new McBASCCovariance(a));
 			}
 		}
 	}
 	
 	/*
 	 * The syncrhonized is just to force all threads to the most up-to-date view of the data
 	 */
 	private static synchronized void kickOneOffIfFileDoesNotExist(Semaphore semaphore, Alignment a, 
 			ScoreGenerator sg) throws Exception
 	{
 		File outFile = getOutputFile(a, sg);
 		
 		
 		if( outFile.exists())
 		{
 			FileScoreGenerator fsg = new FileScoreGenerator("foo", outFile, a);
 			
 			int expectedNum = a.getNumColumnsInAlignment() * (a.getNumColumnsInAlignment()-1) / 2;
 			
 			if( fsg.getNumScores() != expectedNum)
 			{
 				System.out.println(outFile.getAbsolutePath() +" truncated.  Deleting");
 				outFile.delete();
 				
 				if( outFile.exists())
 					throw new Exception("Could not delete " + outFile.getPath());
 			}
 		}
 		
 		if(! outFile.exists())
 		{
 			semaphore.acquire();
 			Worker w = new Worker(a,sg, semaphore);
 				new Thread(w).start();
 		}
 		else
 		{
 			Date date = new Date();
 			String stringDate = date.toString();
 			System.out.println(outFile.getAbsolutePath() + "exists. skipping at " + stringDate);
 		}
 	}
 	
 	private static File getOutputFile(Alignment a, ScoreGenerator sg) throws Exception
 	{
		return new File(ConfigReader.getCleanroom() + File.separator + "results" + File.separator + 
				"oneD" + File.separator + 
 				a.getAligmentID() + "_" + sg.getAnalysisName() + ".txt");
 	}
 	
 	private static class Worker implements Runnable
 	{
 		private final Alignment a;
 		private final Semaphore semaphore;
 		private final ScoreGenerator sg;
 		
 		private Worker(Alignment a, ScoreGenerator sg, Semaphore semaphore)
 		{
 			this.a = a;
 			this.semaphore = semaphore;
 			this.sg = sg;
 		}
 
 		public void run()
 		{
 			//So that we ALWAYS have something to close
 			BufferedWriter writer = null;
 			
 			try
 			{		
 				File outputFile = getOutputFile(a, sg);
 				
 				if(outputFile.exists())
 					throw new Exception(outputFile.getAbsolutePath() + " already exists ");
 				
 				writer = new BufferedWriter(new FileWriter(outputFile));
 				
 				writer.write("i\tj\tscore\n");
 				
 				System.out.println(a.getAligmentID());
 				
 				for( int x=0; x < a.getNumColumnsInAlignment() -1; x++)
 					for( int y=x+1; y < a.getNumColumnsInAlignment(); y++)
 					{
 						writer.write(x + "\t" + y + "\t" + sg.getScore(a, x, y) + "\n");
 					}
 				
 				writer.flush();  
 				writer.close();
 				System.out.println("Finished " + a.getAligmentID() + "_" + sg.getAnalysisName());
 				semaphore.release();
 				
 			}
 			catch(Exception ex)
 			{
 				ex.printStackTrace();
 				System.exit(1);
 			}
 		}
 	}
 }
