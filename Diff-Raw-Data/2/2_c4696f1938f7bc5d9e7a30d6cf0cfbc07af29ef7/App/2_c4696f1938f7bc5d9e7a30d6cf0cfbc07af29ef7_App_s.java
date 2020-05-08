 package no.ntnu.tdt4215.group7;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import no.ntnu.tdt4215.group7.entity.CodeType;
 import no.ntnu.tdt4215.group7.entity.MedDocument;
 import no.ntnu.tdt4215.group7.indexer.ATCIndexer;
 import no.ntnu.tdt4215.group7.indexer.ICDIndexer;
 import no.ntnu.tdt4215.group7.lookup.ATCQueryEngine;
 import no.ntnu.tdt4215.group7.lookup.CodeAssigner;
 import no.ntnu.tdt4215.group7.lookup.ICDQueryEngine;
 import no.ntnu.tdt4215.group7.lookup.QueryEngine;
 import no.ntnu.tdt4215.group7.parser.ATCParser;
 import no.ntnu.tdt4215.group7.parser.BookParser;
 import no.ntnu.tdt4215.group7.parser.GoldStandardParser;
 import no.ntnu.tdt4215.group7.parser.ICDParser;
 import no.ntnu.tdt4215.group7.parser.PatientCaseParser;
 import no.ntnu.tdt4215.group7.service.EvaluationService;
 import no.ntnu.tdt4215.group7.service.FileService;
 import no.ntnu.tdt4215.group7.service.FileServiceImpl;
 import no.ntnu.tdt4215.group7.service.MatchingService;
 import no.ntnu.tdt4215.group7.service.MatchingServiceImpl;
 import no.ntnu.tdt4215.group7.utils.Paths;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.store.Directory;
 
 public class App implements Runnable {
 	
 	private static Logger logger = Logger.getLogger(App.class);
 	
 	// CODE PARSERS
 	final ICDParser icdParser;
 	final ATCParser atcParser;
 
 	// FILE SERVICE
 	private FileService fileService;
 
 	// QUERY ENGINE
 	final QueryEngine atcQueryEngine;
 	final QueryEngine icdQueryEngine;
 
 	// indices
 	private Directory icdIndex;
 	private Directory atcIndex;
 
 	// parsed documents collections
 	private List<MedDocument> book = new ArrayList<MedDocument>();
 	private List<MedDocument> patientCases = new ArrayList<MedDocument>();
 	private List<MedDocument> goldStandard = new ArrayList<MedDocument>();
 
 	public void run() {
 		
 		// execution setup
 		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());		
 		CompletionService<List<MedDocument>> completionService = new ExecutorCompletionService<List<MedDocument>>(
 				executor);
 		
 		long start = System.currentTimeMillis();
 		
 		// submit ICD and ATC indexer tasks to execution
 		Future<Directory> icdIndexerTask = null;
 		Future<Directory> atcIndexerTask = null;
 		
 		try {
 			icdIndexerTask = executor.submit(new ICDIndexer(Paths.ICD10_INDEX_DIRECTORY,icdParser.parseICD(Paths.ICD10_FILE)));
 			atcIndexerTask = executor.submit(new ATCIndexer(Paths.ATC_INDEX_DIRECTORY,atcParser.parseATC(Paths.ATC_FILE)));
 		} catch (IOException e) {
 			logger.error(e);
 		}
 		
 		System.out.println("Indices submited for parsing and indexing execution. " + (System.currentTimeMillis() - start)/1000);
 
 		// PATIENT FILES
 		List<String> patientFileList = fileService.getPatientFiles();
 
 		// LEGEMIDDELHÅNDBOK
 		List<String> bookFileList = fileService.getBookFiles();
 
 		// parse all patient case files
 		for (String file : patientFileList) {
 			completionService.submit(new PatientCaseParser(file));
 		}
 		
 		System.out.println("Patient case files submited for parsing execution. " + (System.currentTimeMillis() - start)/1000);
 			
 		// parse all book chapters
 		for (String file : bookFileList) {
 			completionService.submit(new BookParser(file));
 		}
 		
 		System.out.println("Book files submited for parsing execution. " + (System.currentTimeMillis() - start)/1000);
 		
 		// parse the gold standard
 		completionService.submit(new GoldStandardParser(Paths.GOLD_STANDARD_FILE));
 		
 		System.out.println("Gold standard submitted for parsing execution.  " + (System.currentTimeMillis() - start)/1000);
 
 		// code assignment part ---
 		try {
 			// wait for ICD/ATC indexing
 			icdIndex = icdIndexerTask.get();
 			atcIndex = atcIndexerTask.get();
 
 			System.out.println("Indices ready. " + (System.currentTimeMillis() - start)/1000);
 			
 			for (int i = 0; i < (patientFileList.size() + bookFileList.size()); i++) {
 				// take ready result from the completion service
 				Future<List<MedDocument>> result = completionService.take(); // blocking
 				
 				// get each MedDoc in result
 				for (MedDocument doc : result.get()) {
 					// save document to collection
 					saveDocument(doc);
 
 					// assign ICD and ATC codes to the document
 					executor.submit(new CodeAssigner(doc, icdIndex, atcIndex, icdQueryEngine, atcQueryEngine));
 				}
 			}
 
 			// shutdown the thread pool and await termination
 			executor.shutdown();
 			
 			System.out.println("Executor closed. " + (System.currentTimeMillis() - start)/1000);
 			
 			while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
 				System.out.println("Awaiting termination. " + (System.currentTimeMillis() - start)/1000);
 			}
 			
 			// executor has finished...
 			System.out.println("All MedDocs completed. " + (System.currentTimeMillis() - start)/1000);
 
 		} catch (InterruptedException e) {
 			logger.error(e.getStackTrace());
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			logger.error(e.getStackTrace());
 		}
 
 		// use matching service to find relevant documents
 		
 		MatchingService matchingService = new MatchingServiceImpl(book);
 		try {
 			fileService.writeResults(patientCases, matchingService);
 			System.out.println("Results written. " + (System.currentTimeMillis() - start)/1000);
 		} catch (IOException e) {
 			logger.error(e);
 		}
 		
 		// evaluate against the gold standard
 		EvaluationService evalService = new EvaluationService(patientCases, goldStandard);
 		
 		try {
 			fileService.writeEval(evalService.call());
 			System.out.println("Evaluation written. " + (System.currentTimeMillis() - start)/1000);
 		} catch (IOException e) {
 			logger.error(e);
 		}
 		
 		System.out.println("Total duration: " + (System.currentTimeMillis() - start)/1000);
 	}
 
 	private void saveDocument(MedDocument doc) {
 		// save document to the right collection by code
 		if (doc.getType() == CodeType.CLINICAL_NOTE) {
 			patientCases.add(doc);
		} else if (doc.getType() == CodeType.CLINICAL_NOTE) {
 			book.add(doc);
 		} else {
 			goldStandard.add(doc);
 		}
 	}
 
 	public App() {
 		fileService = new FileServiceImpl();
 		icdParser = new ICDParser();
 		atcParser = new ATCParser();
 		atcQueryEngine = new ATCQueryEngine();
 		icdQueryEngine = new ICDQueryEngine();
 	}
 	
 	public static void main(String[] args) {
 		
 		App app = new App();
 		
 		Thread d = new Thread(app);
 		
 		d.start();
 		
 		try {
 			d.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
