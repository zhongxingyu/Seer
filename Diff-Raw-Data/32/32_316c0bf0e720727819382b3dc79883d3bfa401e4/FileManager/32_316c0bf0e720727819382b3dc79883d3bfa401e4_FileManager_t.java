 package justen;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 
 import data.PropertiesOfPeer;
 
 public class FileManager implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private HashSet<String> localFiles;
 	private HashSet<String> remoteFiles;
 	private HashSet<String> openFiles;
 	private Hashtable<String, ArrayList<Integer>> versionMap;
	private Hashtable<String, Lock> lockMap;
 	
 	private final Object lock = new Object();
 	
 	public FileManager() {
 		localFiles = new HashSet<String>();
 		remoteFiles = new HashSet<String>();
 		openFiles = new HashSet<String>();
 		versionMap = new Hashtable<String, ArrayList<Integer>>();
		lockMap = new Hashtable<String, Lock>();
 	}
 	
 	/*
 	 version map: test1.pdf : [0,0,1,1]
 	 localFiles: test2_v0.pdf, test1_v3.pdf
 	 remoteFiles = test1_v0.pdf, test1_v1.pdf
 	 openFiles = test1_v0.pdf
 	 */
 	
 	public HashSet<String> getLocalFiles() {
 		return localFiles;
 	}
 	
 	public HashSet<String> getRemoteFiles() {
 		return remoteFiles;
 	}
 	
 	public void openFile(String fileName) { // fileName = test1_v0.pdf
 		openFiles.add(fileName);
 	}
 	
 	/**
 	 * 
 	 * @param fileName (test1_v0.pdf)
 	 * @return
 	 */
 	public boolean isFileOpen(String fileName) { // fileName = test1_v0.pdf
 		return openFiles.contains(fileName);
 	}
 	
 	public void closeFile(String fileName) { // fileName = test1_v0.pdf
 		openFiles.remove(fileName);
 	}
 	
 	public Hashtable<String, ArrayList<Integer>> getVersionMap() {
 		return versionMap;
 	}
 	
 	public boolean fileExistsInVersionMap(String fileName) {// fileName = test1.pdf
 		return versionMap.containsKey(fileName);
 	}
 	
 	public ArrayList<String> getLogicalView() {
 		ArrayList<String> allFiles = new ArrayList<String>();
 		for (Entry<String, ArrayList<Integer>> e : versionMap.entrySet()) {
 			String fileName = e.getKey();
 			ArrayList<Integer> temp = e.getValue();
 			String properName = fileName.substring(0, fileName.lastIndexOf("."));
 			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
 			for (int i = 0; i < temp.size(); i++) {
 				if (temp.get(i) == null) 
 					continue;
 				allFiles.add(properName + "_v" + i + "." + extension);
 			}
 		}
 		return allFiles;
 	}
 	
	public Hashtable<String, Lock> getLockMap() {
 		return lockMap;
 	}
 	
 	// filename contains version num
 	public void setLock(String fileName, Lock lock) { // fileName = test1.pdf
 		lockMap.put(fileName, lock);
 	}
 	
 	/**
 	 * Returns type of lock on file. If null, no lock on file.
 	 * @param fileName
 	 * @return lock type
 	 */
 	public Lock getLockType(String fileName) {
 		if (lockMap.containsKey(fileName))
 			return lockMap.get(fileName);
 		
 		return null;
 	}
 	
 	public void createNewFile(String fileName) { // fileName = test1.pdf
 		String properName = fileName.substring(0, fileName.lastIndexOf("."));
 		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
 		localFiles.add(properName + "_v0." + extension);
 		ArrayList<Integer> bitString = new ArrayList<Integer>();
 		bitString.add(1);
 		versionMap.put(fileName, bitString);
 	}
 	
 	public void processStatusUpdate(Status s) {
 		synchronized(lock) {
 			processVersionMap(s.fileVersionMap);
 		}
 	}
 	
 	private void processVersionMap(Hashtable<String, ArrayList<Integer>> map) {	
 		for (Entry<String, ArrayList<Integer>> e : map.entrySet()) {
 			ArrayList<String> temp = getAllFilesForMapIndex(e.getKey(), e.getValue());
 			for (String file : temp) {
 				if (!localFiles.contains(file) && !remoteFiles.contains(file)) {
 					// no knowledge of this file
 					remoteFiles.add(file);
 					String properName = getProperName(file);
 					int vNum = getVersionNumberFromFile(file);
 					
 					if (!versionMap.containsKey(file)) {
 						ArrayList<Integer> bitString = new ArrayList<Integer>(vNum);
 						for (int i = 0; i < vNum; i++) 
 							bitString.add(0);
 						versionMap.put(e.getKey(), bitString);
 					}
 					else if (vNum > versionMap.get(properName).size()) {
 						int j = versionMap.get(properName).size();
 						versionMap.get(properName).ensureCapacity(vNum);
 						for (int i = j; i <= vNum; i++) 
 							versionMap.get(properName).add(0);
 					} 
 					else 
 						versionMap.get(properName).set(vNum, 0);
 				}
 			}
 		}
 	}
 	
 	private ArrayList<String> getAllFilesForMapIndex(String fileName, ArrayList<Integer> bitString) {
 		ArrayList<String> temp = new ArrayList<String>();
 		String properName = fileName.substring(0, fileName.lastIndexOf("."));
 		String extension = fileName.substring(fileName.lastIndexOf("."));
 		for (int i = 0; i < bitString.size(); i++) 
 			temp.add(properName + "_v" + i + extension);
 		return temp;
 	}
 	
 	public boolean saveNewFileVersion(String fileName) { // fileName = test1.pdf
 		if (!versionMap.containsKey(fileName)) // should be create
 			return false;
 
 		versionMap.get(fileName).add(1);
 		String properName = fileName.substring(0, fileName.lastIndexOf("."));
 		String extension = fileName.substring(fileName.lastIndexOf("."));
 		localFiles.add(properName + "_v" + (versionMap.get(fileName).size() - 1) + extension);
 		return true;
 	}
 	
 	public boolean deleteSingleFile(String fileName) { // fileName = test1_v0.pdf
 		if (!localFiles.contains(fileName))
 			return false;
 		
 		String path = new File(PropertiesOfPeer.folderPeerName + "\\" + fileName).getAbsolutePath();
 		try {
 			Files.delete(Paths.get(path));
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		int vNum = getVersionNumberFromFile(fileName);
 		
 		// ok we have file, let's remove from local and from version map
 		localFiles.remove(fileName);
 		ArrayList<Integer> temp = versionMap.get(getProperName(fileName));
 		temp.set(vNum, null);
 		
 		if (!allFilesDeleted(temp))
 			versionMap.put(getProperName(fileName), temp);
 		else
 			versionMap.remove(getProperName(fileName));
 		
 		return true;
 	}
 	
 	private boolean allFilesDeleted(ArrayList<Integer> bitString) {
 		boolean allGone = true;
 		for (int i = 0; i < bitString.size(); i++) {
 			if (bitString.get(i) != null) {
 				allGone = false;
 				break;
 			}
 		}
 		return allGone;
 	}
 	
 	public boolean deleteAllVersionsOfFile(String fileName) { // fileName = test1.pdf
 		ArrayList<String> versionsToDelete = new ArrayList<String>();
 		for (String file : localFiles) {
 			String temp = getProperName(file);
 			if (fileName.equals(temp)) 
 				versionsToDelete.add(file);
 		}
 		
 		if (versionsToDelete.size() != 0) {
 			for (String file : versionsToDelete)
 				localFiles.remove(file);
 		}
 		
 		versionMap.remove(fileName);
 		return true;
 	}
 	
 	/**
 	 * Takes in filename with version and gives just file name
 	 * @param fileName (test1_v0.pdf)
 	 * @return test1.pdf
 	 */
 	public static String getProperName(String fileName) {
 		String properName = fileName.substring(0, fileName.lastIndexOf("_")); //test1
 		String extension = fileName.substring(fileName.lastIndexOf(".")); //.pdf
 		return properName + extension;
 	}
 	
 	public static int getVersionNumberFromFile(String fileName) {
 		return Integer.parseInt(
 				fileName.substring(fileName.lastIndexOf("_v") + 2, 
 				fileName.lastIndexOf(".")));
 	}
 	
 	/**
 	 * 
 	 * @param fileName (test1_v0.pdf)
 	 * @return
 	 */
 	public boolean fileExists(String fileName) { // fileName = test1_v0.pdf
 		return localFiles.contains(fileName) || remoteFiles.contains(fileName);
 	}
 	
 	public void addRemoteFile(String fileName) { // fileName = test1_v0.pdf
 		remoteFiles.add(fileName);
 	}
 	
 	/**
 	 * 
 	 * @param fileName (test1_v0.pdf)
 	 * @return
 	 */
 	public boolean containsFileLocally(String fileName) { // fileName = test1_v0.pdf
 		return localFiles.contains(fileName);
 	}
 	
 	public boolean containsFileRemotely(String filename) {
 		return remoteFiles.contains(filename);
 	}
 }
