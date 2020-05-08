 package uk.ac.ucl.cs.clonedetector;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 
import uk.ac.ucl.cs.clonedetector.Index.CDoc;

 public class CloneDetector {
 
 	private Index index = new Index();
 
 	/**
 	 * Find clones in the given filename using the specified algorithm.
 	 * 
 	 * @param filename
 	 * @param algorithm
 	 * @return
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException
 	 *             Thrown if the algorithm is not available on the system.
 	 */
 	public CloneManager findClones(String filename, String algorithm) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
 		CloneManager cloneManager = new CloneManager();
 		Normalizer normalizer = new Normalizer(getExtension(filename));
 		String line;
 		int lineNum = 1;
 
 		int iStart = -1;
 		int jStart = -1;
 		int matchLength = -1;
 		int prevMatchingLine = -1;
 		
 		BufferedReader in = new BufferedReader(new FileReader(filename));
 
 		while ((line = in.readLine()) != null) {
 			String normalizedLine = normalizer.normalize(line);
 			BigInteger fingerprint = computeFingerprint(normalizedLine, algorithm);
 
			ArrayList<CDoc> matchingLines = index.linesWithFingerprint(fingerprint);
 
 			// Start of a new match:
 			if (matchingLines.size() > 0 && matchLength == -1) {
				int earliestMatch = matchingLines.get(0).getLineNumber();
 				prevMatchingLine = earliestMatch;
 				iStart = lineNum;
 				jStart = earliestMatch;
 				matchLength = 1;
 			} else if (matchLength > -1) {
 				if (matchingLines.contains(prevMatchingLine + 1)) { // continue match
 					matchLength++;
 					prevMatchingLine++;
 				} else { // end match
 					cloneManager.add(iStart, jStart, matchLength - 1);
 					iStart = -1;
 					jStart = -1;
 					matchLength = -1;
 				}
 			}
 
			index.updateIndex(fingerprint, filename, lineNum);
 			lineNum++;
 		}
 		return cloneManager;
 	}
 
 	/**
 	 * Retrieves the file extension from a given relative or absolute filename.
 	 * Filenames with no extension return "".
 	 * 
 	 * @param filename
 	 *            Filename to get the extension for
 	 * @return Extension for the filename
 	 */
 	public static String getExtension(String filename) {
 		String chunks[] = filename.split("\\.");
 		if (chunks.length > 1)
 			return chunks[chunks.length - 1];
 		return "";
 	}
 
 	public static BigInteger computeFingerprint(String line, String algorithm) throws NoSuchAlgorithmException {
 
 		if (algorithm.equals("StringHashCode"))
 			return BigInteger.valueOf(line.hashCode());
 
 		/*
 		 * Else hand over to MessageDigest:
 		 */
 
 		if (line.equals(""))
 			return BigInteger.ZERO;
 
 		BigInteger fingerprint = null;
 		MessageDigest m = MessageDigest.getInstance(algorithm);
 		m.update(line.getBytes(), 0, line.length());
 		fingerprint = new BigInteger(1, m.digest());
 		return fingerprint;
 	}
 
 	public void info(String[] fileNames){
 		System.out.print("Searching for clones in ");
 		for (int i = 1; i < fileNames.length; i++) {
 			System.out.print(fileNames[i] + " ");
 		}
 
 		System.out.println();
 	}
 	
 	/*
 	 * Handles all the output:
 	 */
 	public void findClones(String algorithm, String[] fileNames) {
 		info(fileNames);
 		
 		for (int i = 1; i < fileNames.length; i++) {
 			try {
 				CloneManager clones = findClones(fileNames[i], algorithm);
 				System.out.println(clones);
 
 				if (fileNames.length > 1 && i < fileNames.length - 1)
 					System.out.println("");
 			} catch (FileNotFoundException e) {
 				System.err.println("File not found!");
 			} catch (NoSuchAlgorithmException e) {
 				System.err.println("No such algorithm available on this system!");
 			} catch (IOException e) {
 				System.err.println("An error occurred whilst reading the file.");
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		CloneDetector cd = new CloneDetector();
 
 		if (args.length < 2) {
 			System.out.println("USAGE: java -jar clone.java <algorithm> <filename(s)>");
 		} else {
 			cd.findClones(args[0], args);
 		}
 	}
 }
