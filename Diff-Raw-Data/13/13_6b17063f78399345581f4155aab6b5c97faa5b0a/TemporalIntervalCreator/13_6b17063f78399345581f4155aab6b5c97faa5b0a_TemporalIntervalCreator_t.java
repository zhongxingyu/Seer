 package it.unimib.disco.TemporalIntervalCreator;
 
 import it.unimib.disco.FactExtractor.DateOccurrence;
 import it.unimib.disco.FactExtractor.ResourceFetcher;
 import it.unimib.disco.ReadFiles.ReadFiles;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.hp.hpl.jena.ontology.OntModel;
 
 public class TemporalIntervalCreator {
 
 private static Logger logger = Logger.getLogger(TemporalIntervalCreator.class);
 	
 public static void main (String []args) throws FileNotFoundException{
 	if (args.length < 1) {
		System.out.println("Use: java TemporalIntervalCreator <Resource list file> <temporal defacto output>");
		System.out.println("Example: java TemporalIntervalCreator /temporalIntervalCreator_ResourceList_in2.txt /sortbyplayer-labels-with-space_out_lionel.csv");
 	} else {
		
 		// Resource URI extraction
		List<String> resURIs = ReadFiles.getURIs(new File(TemporalIntervalCreator.class.getResource(args[0]).getFile()));
 		logger.info("DBpedia resources list file parsed");
 
 		ResourceFetcher rf = new ResourceFetcher();
 		
 		HashMap<String,OntModel> resourceModel =  rf.fetch(resURIs);
 		logger.info("Retrieved all dbpedia's entity description");
 		
 		// date and fact extractor
 		MatrixPruningCreator dfe = new MatrixPruningCreator();
 					
 		HashMap<String, HashMap<String, HashSet<String>>> repositoryDates =  dfe.fetchDates(resourceModel);
 		logger.info("Retrieved all dbpedia's property value of type date");
 		
 		
 		HashMap<String, DateOccurrence [][]> maximalMatrixDates =  dfe.maximalMatrixCreator(repositoryDates);
 
 		
 		HashMap<String, HashMap<ArrayList<String>,Integer>> timeAnnotated = dfe.temporalPredicateExtractor(repositoryDates,true);
 
 			
 		HashMap<String,DateOccurrence [][]> reducedMatrix1= dfe.matrixReducerSE(timeAnnotated,maximalMatrixDates);
 
 		
 		timeAnnotated = dfe.temporalPredicateExtractor(repositoryDates,false);
 		logger.info("Retrieved all dbpedia's semantic predicates");
 		
 		HashMap<String,DateOccurrence [][]> reducedMatrix2 = dfe.matrixReducerSP(timeAnnotated,reducedMatrix1);
 		logger.info("Build matrix with starting and ending time points");
 		
 		
 		TemporalIntervalFactAssociator ta = new TemporalIntervalFactAssociator();
 		
 	//	HashMap<String,DateOccurrence [][]> diagonalMatrix = ta.diagonalMatrixCreator(reducedMatrix2);
 		//logger.info("Build diagonal Matrix");
 		
 		// Resource URI extraction
		List<String> temporalDefactoFacts = ReadFiles.getURIs(new File(TemporalIntervalCreator.class.getResource(args[1]).getFile()));
 		logger.info("TemporalDefacto's facts parsed");
 		
 		HashMap<String,DateOccurrence [][]> matrixManhattanDurationUri = ta.matrixManhattanDuration(reducedMatrix2);
 		logger.info("Build temporal distribution curve Matrix");
 		
 		HashMap<String,HashMap<String,HashSet<ArrayList<String>>>> groupOccu= ta.groupOccuByYear(temporalDefactoFacts);
 		logger.info("Build temporal distribution curve Matrix");
 				
 		//matrixHitDuration(groupOccu,temporalDCMatrixURI);
 		FileOutputStream fos = new FileOutputStream("temporalIntervalCreator_out2.csv");
 		PrintWriter pw = new PrintWriter(fos);
 
 			try {
 
 				HashMap<String,DateOccurrence [][]> odc= new HashMap<String,DateOccurrence [][]>();
 				HashMap<String,HashMap<String,DateOccurrence [][]>> sodc = new HashMap<String,HashMap<String,DateOccurrence [][]>>() ;
 				for (String uri: matrixManhattanDurationUri.keySet()){
 					odc= ta.dcCalculator(uri,groupOccu.get(uri),reducedMatrix2,pw);
 					sodc.put(uri, odc);
 				}
 
 			} catch (NullPointerException e) {
 					e.printStackTrace();
 			}
 	
 		      pw.close();
 		      try {
 				fos.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	   		
 		/*Evaluation ev = new Evaluation();
 		HashMap<String, OntModel> sodc;
 		for (String uri:sodc.keySet()){
 			HashMap<String,HashMap<String,DateOccurrence [][]>> matrixManhattanDuration=sodc.get(uri);
 		HashMap<String,ArrayList<Double>> localmetrics=ev.precision(matrixManhattanDuration,temporalDefactoFacts);
 		for (String Uri:localmetrics.keySet()){
 			System.out.println(uri+" "+localmetrics.get(Uri));
 		}
 	}*/
       /*try {
 			
 			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("temporalIntervalCreator_out2.csv")));
 			for (String uri:sodc.keySet()){
 				HashMap<String,DateOccurrence [][]> matrixManhattanDuration=sodc.get(uri);
 
 				for(String res: matrixManhattanDuration.keySet()){
 					DateOccurrence [][] finalMatrix = matrixManhattanDuration.get(res);
 					//ArrayList<Double> metrics=localmetrics.get(res);
 					//bw.write(res+" "+"p "+metrics.get(0)+" "+"r "+metrics.get(1)+ "\n" );
 					bw.write(uri+""+res+" "+"\n" );
 					for (int i=0; i<finalMatrix.length;i++){
 						for (int j=0;j<finalMatrix[i].length;j++){
 							
 							bw.write(finalMatrix[i][j].getDate()+""+ finalMatrix[i][j].getOccurrence()+";");
 						}
 						bw.write("\n");
 				}
 			}
 			}
 		
 			
 			bw.flush();
 			bw.close();
 			logger.info("Results file built");
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();*/
 		}
 	}
 }
 
 
