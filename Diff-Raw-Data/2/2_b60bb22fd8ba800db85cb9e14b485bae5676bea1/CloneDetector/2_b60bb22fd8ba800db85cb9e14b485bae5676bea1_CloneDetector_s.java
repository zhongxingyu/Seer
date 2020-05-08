 package uk.ac.ucl.cs.clonedetector;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 public class CloneDetector {
 	/*
 	 * The number of input lines which the algorithm expects: (this is a soft
 	 * limit: the algorithm needs to know it only for performance reasons)
 	 */
 	public static final int NUMBER_OF_LINES = 1024;
 
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
 	public ArrayList<Clone> findClones(String filename, String algorithm) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
 
 		ArrayList<Clone> clones = new ArrayList<Clone>();
 
 		/*
 		 * The HashTable class is not used because we want to see the collisions
 		 * happening:
 		 */
 
 		HashMap<BigInteger, LinkedList<Line>> hashCodeTable = new HashMap<BigInteger, LinkedList<Line>>(NUMBER_OF_LINES);
 
 		/*
 		 * there couldn't possibly be collisions between line numbers (as each
 		 * line has a unique line number), so no linked lists are needed:
 		 */
 
 		BufferedReader in = new BufferedReader(new FileReader(filename));
 		HashMap<Integer, Line> lineNumberTable = new HashMap<Integer, Line>(NUMBER_OF_LINES);
 
 		/*
 		 * re-using variables for performance reasons (the garbage collector
 		 * won't have to do so much work this way):
 		 */
 
 		String line;
 		Line currentLine;
 		BigInteger fingerprint = null;
 
 		/*
 		 * "previousFingerprint" is the fingerprint of the line immediately
 		 * prior to the current line
 		 */
 		BigInteger previousFingerprint = null;
 
 		/*
 		 * "collider" is what the collision is colliding with
 		 */
 		int offset = 0;
 		int colliderStart = 0;
 		int currentCollisionStart = 0;
 		int currentCollisionEnd = 0;
 
 		int lineNumber = 0;
 		LinkedList<Line> currentList;
 		boolean inCollisionBlock = false;
 
 		while ((line = in.readLine()) != null) {
 			lineNumber++;
 
 			/*
 			 * \s matches all whitespace characters
 			 */
 			String processedLine = line.replaceAll("\\s*", "");
 
 			fingerprint = computeFingerprint(processedLine, algorithm);
 			currentLine = new Line(lineNumber, processedLine, fingerprint);
 
 			/*
 			 * storing the read line and its attributes:
 			 */
 			lineNumberTable.put(lineNumber, currentLine);
 
 			if (processedLine.equals("")) {
 				if (inCollisionBlock) {
 					inCollisionBlock = false;
 					currentCollisionEnd = lineNumber - 1;
 					if (colliderStart == (currentCollisionStart - 1)) {
 						currentCollisionStart--;
 					}
 					clones.add(new Clone(colliderStart, currentCollisionStart, currentCollisionEnd - currentCollisionStart));
 				}
 				previousFingerprint = BigInteger.ZERO;
 				continue;
 			}
 
 			/*
 			 * a collision has happened
 			 */
 			if (hashCodeTable.containsKey(fingerprint)) {
 
 				/*
 				 * checking to see if this really is a block of collisions,
 				 * rather than sequence of random collisions with random lines:
 				 */
 
 				if (inCollisionBlock) {
 					offset = lineNumber - currentCollisionStart;
 
 					if (lineNumberTable.get(colliderStart + offset) == null) {
 						System.out.println("lineNumber is: " + lineNumber);
 						System.out.println("collderStart is: " + colliderStart);
 						System.out.println("offset is: " + offset);
 						System.out.println("NULL");
 						System.exit(0);
 					}
 
 					if ((!lineNumberTable.get(colliderStart + offset).getFingerprint().equals(fingerprint))) {
 						inCollisionBlock = false;
 						currentCollisionEnd = lineNumber - 1;
 						if (colliderStart == (currentCollisionStart - 1)) {
 							currentCollisionStart--;
 						}
 						clones.add(new Clone(colliderStart, currentCollisionStart, currentCollisionEnd - currentCollisionStart));
 					}
 				}
 
 				/*
 				 * entering a new collision block:
 				 */
 				if (!inCollisionBlock) {
 					inCollisionBlock = true;
 					currentCollisionStart = lineNumber;
 					colliderStart = hashCodeTable.get(fingerprint).getFirst().getLineNumber();
 				}
 
 				currentList = hashCodeTable.remove(fingerprint);
 				currentList.add(currentLine);
 				hashCodeTable.put(fingerprint, currentList);
 			}
 			/*
 			 * no collision has happened
 			 */
 			else {
 				currentList = new LinkedList<Line>();
 				currentList.add(currentLine);
 				hashCodeTable.put(fingerprint, currentList);
 
 				/*
 				 * exiting the current collision block:
 				 */
 				if (inCollisionBlock) {
 					inCollisionBlock = false;
 					currentCollisionEnd = lineNumber - 1;
 					if (colliderStart == (currentCollisionStart - 1)) {
 						currentCollisionStart--;
 					}
 					clones.add(new Clone(colliderStart, currentCollisionStart, currentCollisionEnd - currentCollisionStart));
 				}
 			}
 
 			previousFingerprint = fingerprint;
 		}
 		if (inCollisionBlock) {
 			inCollisionBlock = false;
 			currentCollisionEnd = lineNumber;
 			if (colliderStart == (currentCollisionStart - 1)) {
 				currentCollisionStart--;
 			}
 			clones.add(new Clone(colliderStart, currentCollisionStart, currentCollisionEnd - currentCollisionStart));
 		}
 
 		in.close();
 		return clones;
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
		if (chunks.length > 0)
 			return chunks[chunks.length - 1];
 		return "";
 	}
 
 	public static BigInteger computeFingerprint(String line, String algorithm) throws NoSuchAlgorithmException {
 
 		if (algorithm.equals("StringHashCode"))
 			return BigInteger.valueOf(line.hashCode());
 
 		/*
 		 *  Else hand over to MessageDigest:
 		 */
 
 		if (line.equals(""))
 			return BigInteger.ZERO;
 
 		BigInteger fingerprint = null;
 		MessageDigest m = MessageDigest.getInstance(algorithm);
 		m.update(line.getBytes(), 0, line.length());
 		fingerprint = new BigInteger(1, m.digest());
 		return fingerprint;
 	}
 
 	/*
 	 * Handles all the output:
 	 */
 	public void findClonesFromFiles(String[] filenames, String algorithm) throws FileNotFoundException, NoSuchAlgorithmException, IOException {
 		for (int i = 0; i < filenames.length; i++) {
 			if (filenames.length > 1)
 				System.out.println(filenames[i]);
 			
 			ArrayList<Clone> clones = findClones(filenames[i], algorithm);
 			
 			for (Clone clone : clones)
 				System.out.println(clone);
 			
 			if (filenames.length > 1 && i < filenames.length - 1)
 				System.out.println("");
 		}
 	}
 
 	public static void main(String[] args) {
 		CloneDetector cd = new CloneDetector();
 		if (args.length < 1) {
 			System.out.println("Missing filename");
 		} else {
 			try {
 				cd.findClonesFromFiles(args, "SHA-1");
 			} catch (FileNotFoundException e) {
 				System.out.println("File not found!");
 			} catch (IOException e) {
 				System.out.println("An error occurred whilst reading the file.");
 			} catch (NoSuchAlgorithmException e) {
 				System.out.println("No such algorithm available on this system!");
 			}
 		}
 	}
 }
