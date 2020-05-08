 package uk.ac.ucl.cs.clonedetector;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 
 public class CloneDetector {
 	
 	public static Options options;
 
 	/**
 	 * Retrieves the file extension from a given relative or absolute filename.
 	 * Filenames with no extension return "".
 	 * 
	 * @param filename Filename to get the extension for
 	 * @return Extension for the filename
 	 */
 	public static String getExtension(String filename) {
 		String chunks[] = filename.split("\\.");
 		if (chunks.length > 1)
 			return chunks[chunks.length - 1];
 		return "";
 	}
 	
	/**
	 * Finds clones from the specified index (which encapsulates both the forward
	 * and inverted index).
	 * @param index The Index class to find clones from.
	 * @return
	 */
 	public CloneManager findClones(Index index) {
 	
 		CloneManager cloneManager = new CloneManager();
 		
		// Iterate over the index
 		for (BigInteger fingerprint : index) {
			ArrayList<Reference> postings = index.lookup(fingerprint); // Fetch the postings list
 			if (postings.size() > 1) {
 				for (Reference iStart : postings) {
 					for (Reference jStart : postings) {
 						if (iStart != jStart) {
 							Reference iEnd = iStart.clone();
 							Reference jEnd = jStart.clone();
 							Reference i = iStart.clone();
 							Reference j = jStart.clone();
 							boolean matching = true;
 							
 							while (matching) {
 								// This is the start of a clone
 								Reference iNext = i.next();
 								Reference jNext = j.next();
 							
 								BigInteger iNextFingerprint = index.lookup(iNext);
 								
 								ArrayList<Reference> iNextPostings = index.lookup(iNextFingerprint);
 								if (iNextPostings.contains(jNext)) {
 									i = iNext;
 									j = jNext;
 									iEnd.incLine();
 									jEnd.incLine();
 								} else {
 									matching = false;
 								}
 							}
 							Clone c = new Clone(iStart, iEnd, jStart, jEnd);
 							cloneManager.add(c);
 						}
 					}
 				}
 			}
 		}
 		return cloneManager;
 	}
 
 	/*
 	 * Handles all the output:
 	 */
 	public void findClones() {
 		Index index = new Index();
 		
 		for (String filename: options.getFilenames()) {
 			try {				
 				index.updateIndex(filename, options.getAlgorithm());
 			} catch (FileNotFoundException e) {
 				System.err.println("File not found!");
 			} catch (NoSuchAlgorithmException e) {
 				System.err.println("No such algorithm available on this system!");
 			} catch (IOException e) {
 				System.err.println("An error occurred whilst reading the file.");
 			}
 		}
 		CloneManager clones = findClones(index);
 		System.out.println(clones);
 	}
 
 	public static void main(String[] args) {
 		CloneDetector cd = new CloneDetector();
 		if (args.length < 1) {
 			System.out.println("USAGE: java -jar clone.java <filename(s)>");		
 		} else {
 			options = new Options(args);
 			cd.findClones();
 		}
 	}
 }
