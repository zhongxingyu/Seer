 package cz.cvut.felk.cig.jcop.problem.bucket;
 
 import cz.cvut.felk.cig.jcop.problem.ProblemFormatException;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 
 public class BucketTokenizer {
 
	public BucketInstance getKnapsackInstance(String line) {
 		StringTokenizer st = new StringTokenizer(line);
 		BucketInstance bucketInstance = new BucketInstance();
 
 		try {
 			bucketInstance.setId(st.nextToken());
 			bucketInstance.setDimension(Integer.valueOf(st.nextToken()));
 			bucketInstance.setCapacities(loadIntegers(bucketInstance.getDimension(), st));
 			bucketInstance.setStartingContents(loadIntegers(bucketInstance.getDimension(), st));
 			bucketInstance.setDestinationContents(loadIntegers(bucketInstance.getDimension(), st));
 
 			if (st.hasMoreTokens()) {
 				throw new ProblemFormatException("Too many elements in line");
 			}
 		} catch (NoSuchElementException e) {
 			throw new ProblemFormatException("Insufficient number of elements in line");
 		} catch (NumberFormatException e) {
 			throw new ProblemFormatException("Non numeric elements found in line");
 		}
 
 		return bucketInstance;
 	}
 
 	private int[] loadIntegers(int numberOfIntegers, StringTokenizer st) {
 		int[] integers = new int[numberOfIntegers];
 		for (int i = 0; i < numberOfIntegers; i++) {
 			Integer integer = Integer.valueOf(st.nextToken());
 			integers[i] = integer;
 		}
 		return integers;
 	}
 }
