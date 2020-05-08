 package cse.unl.edu.Simulation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
import java.util.Random;
 import java.util.Set;
 import cse.unl.edu.util.Utils;
 
 public class Author {
 
 	public String authorName;
 	public int allFilesCount = 0;
 
 	Map<Integer, List> filesMap;
 	private float median;
	
	List<String> allFiles;
 
 	public Author() {
 		filesMap = new HashMap();
		allFiles = new ArrayList();
 
 	}
 
 	public Author(String authorName, String files) {
 		this();
 		this.authorName = authorName;
 		this.populateFiles(files);
 		// System.out.println(filesMap);
 
 	}
 
 	/*
 	 * private void populateFiles(String files) {
 	 * 
 	 * String[] fileNames = files.split("\n"); this.allFilesCount =
 	 * fileNames.length;
 	 * 
 	 * String[] fileInfo = new String[2]; int lastCount = -1; int keys = -1;
 	 * 
 	 * for (int i = 0; i < fileNames.length; i++) { fileInfo =
 	 * fileNames[i].split(" : ");
 	 * 
 	 * if(i==0 && fileInfo[0].equals("Median")) { this.median =
 	 * Float.parseFloat(fileInfo[1]); continue; }
 	 * 
 	 * if (lastCount != -1 && lastCount == Integer.parseInt(fileInfo[1])) {
 	 * filesMap.get(keys).add(fileInfo[0]); } else { lastCount =
 	 * Integer.parseInt(fileInfo[1]); keys++;
 	 * 
 	 * filesMap.put(keys, new ArrayList()); filesMap.get(keys).add(fileInfo[0]);
 	 * } } }
 	 */
 
 	private void populateFiles(String files) {
 
 		String[] fileNames = files.split("\n");
 		this.allFilesCount = fileNames.length;
 
 		String[] fileInfo = new String[2];
 		int lastCount = -1;
 		int keys = 0;
 
 		for (int i = 0; i < fileNames.length; i++) {
 			fileInfo = fileNames[i].split(" : ");
 
 			if (i == 0 && fileInfo[0].equals("Median")) {
 				this.median = Float.parseFloat(fileInfo[1]);
 				continue;
 			}
 
 			if (Integer.parseInt(fileInfo[1]) >= median)
 				keys = 0;
 			else
 				keys = 1;
 
 			if (!filesMap.containsKey(keys))
 				filesMap.put(keys, new ArrayList());
 
 			filesMap.get(keys).add(fileInfo[0]);
			allFiles.add(fileInfo[0]);
 		}
 	}
 
 	// returns the required # of files from teh set of all files changed by this
 	// developer in the past.
 	public List getFiles(int count, List<File> remoteFiles) {
 
 		if (count > allFilesCount)
 			return null;
 
 		List files = new ArrayList();
 
 		int requiredFiles = count;
 
 		for (int i = 0; i < filesMap.size(); i++) {
 			if (requiredFiles > 0) {
 				files.addAll(getRandomFiles(filesMap.get(i), requiredFiles,
 						remoteFiles));
 				requiredFiles = count - files.size();
 			} else
 				break;
 		}
 
 		return files;
 	}
 
 	private Collection getRandomFiles(List list, int requiredFiles,
 			List<File> remoteFiles) {
 
 		// System.out.println(list + " :  " + requiredFiles + " " +
 		// remoteFiles);
 		List files = new ArrayList();
 
 		if (requiredFiles >= list.size()) {
 			for (Object file : list) {
 				String fileName = (String) file;
 				fileName = fileName.replaceAll("storm--src/jvm", "classes")
 						.replaceAll("storm--src/clj", "classes").split("\\.")[0];
 
 				boolean bAdd = true;
 				for (File remFile : remoteFiles) {
 					if (remFile.fileName.equals(fileName)) {
 						bAdd = false;
 					}
 				}
 
 				if (bAdd)
 					files.add(file);
 			}
 			return files;
 		}
 
 		List<Integer> exclude = new ArrayList();
 
 		for (int i = 0; i < requiredFiles; i++) {
 			int pickedNumer = Utils.getRandomNumber(0, list.size() - 1,
 					exclude, false);
 			exclude.add(pickedNumer);
 			// System.out.println(pickedNumer +" : "+ requiredFiles);
 
 			String fileName = (String) list.get(pickedNumer);
 			// fileName = fileName.replaceAll("storm--src/jvm",
 			// "classes").replaceAll("storm--src/clj",
 			// "classes").split("//.")[0];
 
 			String tempFile = fileName.replaceAll("storm--src/jvm", "classes")
 					.replaceAll("storm--src/clj", "classes").split("\\.")[0];
 
 			boolean bAdd = true;
 			for (File file : remoteFiles) {
 				if (file.fileName.equals(tempFile)) {
 					i--;
 					bAdd = false;
 					;
 				}
 			}
 
 			if (bAdd)
 				files.add(fileName);
 
 			if (exclude.size() == list.size())
 				return files;
 		}
 
 		return files;
 	}
 
 	ArrayList<Integer[]> combinations;
 	int[] allCombinations;
 	
 	private void processSubsets(int n, int k) {
 		allCombinations = new int[n];
 		for (int i = 0; i < n; i++)
 			allCombinations[i] = i;
 
 		int[] subset = new int[k];
 		combinations = new ArrayList();
 		processLargerSubsets(allCombinations, subset, 0, 0);
 	}
 	
 	private void processLargerSubsets(int[] set, int[] subset, int subsetSize,
 			int nextIndex) {
 		if (subsetSize == subset.length) {
 			Integer[] newArray = new Integer[subset.length];
 			int i = 0;
 			for (int value : subset) {
 				newArray[i++] = Integer.valueOf(value);
 			}
 			combinations.add(newArray);
 		} else {
 			for (int j = nextIndex; j < set.length; j++) {
 				subset[subsetSize] = set[j];
 				processLargerSubsets(set, subset, subsetSize + 1, j + 1);
 			}
 		}
 	}
	
	public List getFilesForBaseCase(int size) {
		
		List files = new ArrayList();
		Collections.shuffle(allFiles);
		
	    int length = allFiles.size()-1;
	    List<Integer> exclude = new ArrayList();
	      
	    if(size > length)
	    {
	    	return allFiles;
	    }
	    
	    for (int i = 0; i < size; i++) {
	    	 Integer value = Utils.getRandomNumber(0, length, exclude, true);
	    	 files.add(allFiles.get(value));
	    	 exclude.add(value);
	    }
		return files;
	}
 
 	public static void main(String[] args) {
 
 		String fileContents = "Median : 2.5\nstorm--test/clj/backtype/storm/integration_test.clj : 3\n"
 				+ "storm--src/jvm/backtype/storm/Config.java : 3\n"
 				+ "storm--src/clj/backtype/storm/testing.clj : 3\n"
 				+ "storm--src/clj/backtype/storm/messaging/zmq.clj : 2\n"
 				+ "storm--src/clj/backtype/storm/messaging/local.clj : 1\n"
 				+ "storm--src/clj/backtype/storm/daemon/worker.clj : 1\n"
 				+ "storm--src/clj/backtype/storm/daemon/task.clj : 1";		
 		
 		/*
 		 * for(int i=0;i<5;i++) { int num = Utils.getRandomNumber(1, 10, null,
 		 * true); //System.out.println(num); vals.add(num);
 		 * 
 		 * System.out.println("ST " + (prevStd -
 		 * Utils.standardDeviation(vals))); prevStd =
 		 * Utils.standardDeviation(vals); }
 		 */
 
 		/*
 		 * ArrayList<Integer[]> combinations = new ArrayList(); List<Integer>
 		 * hashCodes = new ArrayList();
 		 * 
 		 * for (int i = 0; i < 75; i++) {
 		 * 
 		 * Integer[] a = Utils.pickKRandomArray(8, 2);
 		 * 
 		 * List<Integer> vals = new ArrayList();
 		 * System.out.println(Utils.standardDeviation(vals));
 		 * 
 		 * if (hashCodes.contains(Arrays.hashCode(a))) {
 		 * System.out.println("Contains " + Arrays.toString(a));
 		 * 
 		 * } else { combinations.add(a); hashCodes.add(Arrays.hashCode(a));
 		 * System.out.println("Added " + Arrays.toString(a) + " : "+
 		 * Arrays.hashCode(a)); //System.out.println(hashCodes); } }
 		 */
 
 		/*
 		 * 
 		 * int i = 25; int requiredFile = (int) Math.round(i * 0.01 * 22);
 		 * 
 		 * String fileName="storm--test/clj/zilch/test/mq.clj";
 		 * 
 		 * fileName = fileName.replaceAll("storm--src/jvm",
 		 * "classes").replaceAll("storm--src/clj", "classes").split("\\.")[0];
 		 * 
 		 * System.out.println(fileName);
 		 * 
 		 * fileName = fileName.split("\\.")[0]; System.out.println(fileName);
 		 */
 
 		/*
 		 * List<Integer> exclude = new ArrayList(); exclude.add(2);
 		 * exclude.add(3); for (int i = 0; i < 3; i++) { Integer pickedNum =
 		 * Utils.getRandomNumber(0, 4, exclude, false); exclude.add(pickedNum);
 		 * 
 		 * }
 		 */
 
 	}
 
 }
