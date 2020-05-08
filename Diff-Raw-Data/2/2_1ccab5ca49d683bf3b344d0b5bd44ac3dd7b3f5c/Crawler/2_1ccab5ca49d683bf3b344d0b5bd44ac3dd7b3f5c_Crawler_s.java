 import java.io.BufferedReader;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.StringTokenizer;
 
 public class Crawler {
 
 	private String folderName;
 	private String stopWordsFile;
 	private HashSet<String> stopWords;
 
 	public Crawler(String folderName) {
 		this.folderName = folderName;
 		stopWords = new HashSet<String>();
 	}
 
 	public Crawler(String folderName, String stopWordsFile) {
 		this.folderName = folderName;
 		this.stopWordsFile = stopWordsFile;
 	}
 
 	/**
 	 * Reads the stop words from the file given by the stopWordsFile field. The stop words are
 	 * upperCased and after that added into a hashSet.
 	 *
 	 */
 	public void readStopWords() {
 		FileInputStream inputStream;
 		try {
 			inputStream = new FileInputStream(stopWordsFile);
 			DataInputStream dataInput = new DataInputStream(inputStream);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					dataInput));
 
 			String line;
 			while ((line = reader.readLine()) != null) {
 				stopWords.add(line.toUpperCase());
 			}
 
 			inputStream.close();
 		} catch (IOException e) {
 			System.out.println("The stop word file can not be found!");
 			System.exit(2);
 		}
 	}
 
 
 	private void processFile(BufferedReader reader, DocEntry docEntry)
 	throws IOException {
 		String line;
 		while ((line = reader.readLine()) != null) {
 			line = line.replaceAll("[^a-zA-Z]", " ");
 
			StringTokenizer tokens = new StringTokenizer(line);
 
 			while (tokens.hasMoreElements()) {
 				String token = tokens.nextToken();
 				if (Config.enableStopwordElimination && stopWords.contains(token) == true) {
 					continue;
 				}
 				if (Config.enableStemming) {
 					Stemmer stemmer = new Stemmer();
 					stemmer.add(token.toLowerCase().toCharArray(), token.length());
 					stemmer.stem();
 
 					token = stemmer.toString().toUpperCase();
 				}
 
 				if (token.isEmpty() == false) {
 					docEntry.incCount(token);
 				}
 			}
 		}
 	}
 
 	public void readDocEntries(DocSet docSet, String docSetName) throws IOException {
 		File folder = new File(docSetName);
 		if(folder.isDirectory() == false)
 			throw new IOException("The input should be a folder!"); 
 
 		File[] listOfFiles = folder.listFiles();
 		for (int i = 0; i < listOfFiles.length; i++) {
 			String fileName = listOfFiles[i].getName();
 			
 			if (fileName.startsWith(".")) {
 			    continue;
 			}
 
 			boolean spam = false; 
 			if(fileName.startsWith("spmsg"))
 				spam = true; 
 			//	System.out.println(fileName + " " + spam);
 			DocEntry docEntry = new DocEntry(fileName, spam);
 
 			FileInputStream inputStream = new FileInputStream(listOfFiles[i]);
 			DataInputStream dataInput = new DataInputStream(inputStream);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					dataInput));
 
 			processFile(reader, docEntry);
 			docSet.addDoc(docEntry);
 
 			inputStream.close();
 		}
 	}
 
 	public ArrayList<DocSet> readDocSet() throws IOException{
 		File folder = new File(folderName);
 		if(folder.isDirectory() == false)
 			throw new IOException("The input should be a folder!");
 
 		ArrayList<DocSet> listDocSets = new ArrayList<DocSet>();
 
 		File[] listOfFiles = folder.listFiles();
 		for (int i = 0; i < listOfFiles.length; i++) {
 			if(listOfFiles[i].isDirectory()){
 				DocSet docSet = new DocSet();
 				readDocEntries(docSet, listOfFiles[i].getAbsolutePath()); 
 				listDocSets.add(docSet);
 			}
 		}
 
 		return listDocSets;
 	}
 
 	/**
 	 * Retrieves the stop words read from the stopWordsFile but upper cased.
 	 *
 	 * @return A HashSet containing all stop words.
 	 */
 	public HashSet<String> getStopWords() {
 		return stopWords;
 	}
 
 	/**
 	 * Sets the file where the stop words are stored.
 	 *
 	 * @param stopWordsFile The name of the file.
 	 */
 	public void setStopWordsFile(String stopWordsFile) {
 		this.stopWordsFile = stopWordsFile;
 	}
 }
