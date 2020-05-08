 package uk.ac.ucl.cs.clonedetector;
 
 import java.io.*;
 import java.security.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.math.*;
 
 public class CloneDetector {
 
 	
 	public ArrayList<Clone> findClones(String filename, String algorithm) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

 		List<BigInteger> fingerprints = new ArrayList<BigInteger>();
 		BufferedReader in = new BufferedReader(new FileReader(filename));
 		
		String line;
 		Normalizer n = new Normalizer(getExtension(filename));
 		while ((line = in.readLine()) != null) {
 			line = n.normalise(line);
 			//System.out.println(line);
 			BigInteger fingerprint = computeFingerprint(line, algorithm);
 			fingerprints.add(fingerprint);
 		}
 		return findClonesFromFingerprints(fingerprints);
 	}
 	
 	public static String getExtension(String filename) {
 		String chunks[] = filename.split("\\.");
 		if (chunks.length > 0) return chunks[chunks.length-1];
 		return "";
 	}
 		
 	
 	// BUG: Cannot find a clone if it's on the last line of the file.
 	public ArrayList<Clone> findClonesFromFingerprints(List<BigInteger> fingerprints) {
 		ArrayList<Clone> clones = new ArrayList<Clone>();
 		// Build comparison matrix between hashes:
 		for (int i = 0; i < fingerprints.size(); i++) { // i is the main pointer
 		
 			for (int j = i+1; j < fingerprints.size(); j++) { // check line i against all following it
 				BigInteger fi = fingerprints.get(i);
 				BigInteger fj = fingerprints.get(j);
 				// Store these values just in case this is the start of a clone
 				int iStart = i + 1;
 				int jStart = j + 1;
 				int cloneLength = 0;
 
 				while (fi.equals(fj) && !fi.equals(BigInteger.ZERO) && j < fingerprints.size()-1) { // while the lines are clones
 					// Increase i and j in step:
 					i++;
 					j++;
 					fi = fingerprints.get(i);
 					fj = fingerprints.get(j);
 					cloneLength++;
 				}
 				// Clone finishes, so save it:
 				if (cloneLength > 0) {
 					Clone c = new Clone(iStart, jStart, cloneLength-1);
 					clones.add(c);
 				}
 			}
 		}
 		return clones;
 	}
 
 	public static BigInteger computeFingerprint(String line, String algorithm) throws NoSuchAlgorithmException {
 
 		if (algorithm.equals("StringHashCode")) return BigInteger.valueOf(line.hashCode());
 
 		// Else hand over to MessageDigest:
 
 		if (line.equals("")) return BigInteger.ZERO;
 		
 		BigInteger fingerprint = null;
 		MessageDigest m = MessageDigest.getInstance(algorithm);
 		m.update(line.getBytes(), 0, line.length());
 		fingerprint = new BigInteger(1, m.digest());
 		return fingerprint;
 	}
 	
 	
 	// Handles all the output:
 	public void findClonesFromFiles(String[] filenames, String algorithm) throws FileNotFoundException, NoSuchAlgorithmException, IOException {
 		for (int i=0; i<filenames.length; i++) {
 			if (filenames.length > 1) System.out.println(filenames[i]);
 			ArrayList<Clone> clones = findClones(filenames[i], algorithm);
 			for (Clone clone : clones) System.out.println(clone);
 			if (filenames.length > 1 && i<filenames.length-1) System.out.println("");
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
