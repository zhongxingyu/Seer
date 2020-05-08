 package com.cse454.nel;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.SQLException;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import com.cse454.nel.disambiguate.AbstractDisambiguator;
 import com.cse454.nel.disambiguate.EntityWikiMentionHistogramDisambiguator;
 import com.cse454.nel.disambiguate.InLinkDisambiguator;
 import com.cse454.nel.disambiguate.SimpleDisambiguator;
 import com.cse454.nel.scoring.Scorer;
 
 
 public class Main {
 
 	//private static final String sentencesFile = "sentences.entities";
     private static Object lock = new Object();
     private static int count = 0;
     private static int counter = 0;
     private static int NUM_DOCUMENTS = 100;
     private static boolean FINISHED_READING_DOCNAMES = false;
     private static int THREADS_WORKING = 0;
 
 	@SuppressWarnings("resource")
 	public static void main(String[] args) throws InterruptedException, SQLException {
 		System.out.println("Start");
 
 		final Scorer scorer;
 		try {
 			scorer = new Scorer();
 		} catch (IOException e1) {
 			System.out.println("Cannot load gold data file");
 			e1.printStackTrace();
 			return;
 		}
 
 		// Setup thread pool
 		final ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
 
 		final BlockingQueue<String> docNames = new ArrayBlockingQueue<>(100);
		for (int i = 0; i < 1; i++) {
 			DocumentProcessWorker worker = new DocumentProcessWorker(docNames, new DocumentConnect(), scorer);
 			executor.execute(worker);
 		}
 
 		// open the doc_gold, and feed each doc name to docNames
 		String path = "./doc_gold.txt";
 		try {
 			// Open file
 			BufferedReader reader = null;
 			if (new File(path).exists()) {
 				reader = new BufferedReader(
 					new InputStreamReader(
 						new FileInputStream(path), "UTF-8"));
 			} else {
 				throw new Exception("Could not open file: " + path);
 			}
 
 			// read lines
 			String line;
 			while ((line = reader.readLine()) != null) {
 				synchronized (lock) {
 					count++;
 					if (count % 100 == 0) {
 						System.out.println("Count: " + count);
 					}
 				}
 				String docName = line.split("\t")[0];
 				docNames.put(docName);
 			}
 			while (!docNames.isEmpty()) {
 				Thread.sleep(0);
 			}
 			Thread.sleep(100);
 			// Wait for threads to finish
 			while (THREADS_WORKING > 0) {
 				Thread.sleep(0);
 			}
 			executor.shutdownNow();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		// Now that we have results from the docs, evaluate the scorer
 		scorer.ScoreOverall();
 		System.exit(0);
 	}
 
 	static class DocumentProcessWorker implements Runnable {
 
 		private final BlockingQueue<String> docs;
 		private final DocumentConnect documentConnect;
 		private final Scorer scorer;
 
 		public DocumentProcessWorker(BlockingQueue<String> docs, DocumentConnect documentConnect, Scorer scorer) {
 			this.docs = docs;
 			this.documentConnect = documentConnect;
 			this.scorer = scorer;
 		}
 
 		@Override
 		public void run() {
 			String docName = null;
 			while (true) {
 				try {
 					DocumentProcessor process;
 					int count = 0;
 //					synchronized (lock) {
 //						if (counter > NUM_DOCUMENTS) {
 //							break;
 //						}
 //						count = counter;
 //						counter++;
 //					}
 					docName = docs.take();
 					synchronized (lock) {
 						THREADS_WORKING++;
 					}
 					System.out.println("starting process");
 					
 					//AbstractDisambiguator disambiguator = new EntityWikiMentionHistogramDisambiguator(new WikiConnect(), true);
 					AbstractDisambiguator disambiguator = new InLinkDisambiguator(new WikiConnect());
 					//AbstractDisambiguator disambiguator = new SimpleDisambiguator();
 					process = new DocumentProcessor(count, docName, documentConnect, scorer, disambiguator);
 					process.run();
 					System.out.println("finishing process");
 					synchronized (lock) {
 						THREADS_WORKING--;
 					}
 				} catch (InterruptedException e) {
 					// ignore
 				} catch (Exception e) {
 					System.err.println("Error processing document: " + docName);
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static void usage() {
 		System.err.println("java com.cse454.nel.Main " +
 				"<keyfile>");
 		System.exit(-1);
 	}
 }
