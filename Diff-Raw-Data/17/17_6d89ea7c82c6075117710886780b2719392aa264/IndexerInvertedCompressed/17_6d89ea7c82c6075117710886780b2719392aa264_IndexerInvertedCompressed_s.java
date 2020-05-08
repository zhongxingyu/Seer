 package edu.nyu.cs.cs2580;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.Vector;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParser;
 import com.google.gson.reflect.TypeToken;
 
 import edu.nyu.cs.cs2580.SearchEngine.Options;
 import edu.nyu.cs.cs2580.FileManager.T3FileReader;
 import edu.nyu.cs.cs2580.FileManager.T3FileWriter;
 
 /**
  * @CS2580: Implement this class for HW2.
  */
 public class IndexerInvertedCompressed extends Indexer {
 
 	// Maps terms to (docs to position offset Sums) -----------------------------------------
 	private Map<Integer, HashMap<Integer, Integer>> _sumOfOffsets
 	= new HashMap<Integer, HashMap<Integer, Integer>>();
 
 	// Maps each term to their posting list
 	//	private Map<Integer, HashMap<Integer, Integer>> _docTermFrequencyInvertedIndex 
 	//	= new HashMap<Integer, HashMap<Integer, Integer>>();
 
 	// Maps each term to their integer representation
 	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
 
 	// Maps each url to its docid
 	private Map<String, Integer> _docIds = new HashMap<String, Integer>();
 
 	// All unique terms appeared in corpus. Offsets are integer representations.
 	private Vector<String> _terms = new Vector<String>();
 
 	// Term document frequency, key is the integer representation of the term and
 	// value is the number of documents the term appears in.
 	private Map<Integer, Integer> _termDocFrequency =
 		new HashMap<Integer, Integer>();
 
 	// Term frequency, key is the integer representation of the term and value is
 	// the number of times the term appears in the corpus.
 	private Map<Integer, Integer> _termCorpusFrequency =
 		new HashMap<Integer, Integer>();
 
 	// Stores all Document in memory.
 	private Vector<DocumentIndexed> _documents = new Vector<DocumentIndexed>();
 
 
 
 	private final Integer INFINITY = Integer.MAX_VALUE;
 	private final Integer documentBlock = 1000;
 
 	private final String contentFolderName;
 	private final String indexFolderName = "invertedOccurenceCompressionIndex";
 	private final String indexFileName = "invertedOccurenceCompressionIndex.idx";
 
 
 	//used for merging indexes
 	private static int fileId = 1;
 	private static int docId = 1;
 	private static int documentsCount = 0;
 	private static Map<String,Scanner> scanners= new HashMap<String,Scanner>();
 	private static Map<String,String> pointerToScanners= new HashMap<String,String>();
 	private static int finalIndexCount = 1;
 
 	private final double[] pageRanks;
 	private final Map<Integer, Integer> numViews;
 	
 	Map<Integer, PostingsWithOccurences<String>> postingLists = 
 			new HashMap<Integer, PostingsWithOccurences<String>>();
 
 	// Maps each term to their posting list
 	private HashMap<Integer, PostingsWithOccurences<String>>  _invertedIndexWithCompresion = 
 		new HashMap<Integer, PostingsWithOccurences<String>>();
 
 
 	@SuppressWarnings("unchecked")
 	public IndexerInvertedCompressed(Options options) {
 		super(options);
 		try{
 
 			System.out.println("Using Indexer: " + this.getClass().getSimpleName());
 			contentFolderName = _options._corpusPrefix;
 			String indexFolder = _options._indexPrefix + "/"+indexFolderName;
 			File indexDirectory = new File(indexFolder);
 			if(!indexDirectory.exists())
 				indexDirectory.mkdir();
 
 			pageRanks = (double[]) new CorpusAnalyzerPagerank(options).load();
 			numViews = (Map<Integer, Integer>) new LogMinerNumviews(options).load();
 
 		}catch(Exception e){
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	private static String hexOut(Integer inNo){
 		String in = Integer.toBinaryString(inNo);
 		int buffer = 0;
 		if(in.length() % 7 != 0)
 			buffer = 7 - in.length() % 7;
 		StringBuilder sevenAligned = new StringBuilder();
 		while(buffer != 0) {
 			sevenAligned.append("0");
 			buffer--;
 		}
 		sevenAligned.append(in);
 		int length = sevenAligned.length();
 		StringBuilder out = new StringBuilder();
 		int index = 0;
 		while(length != 0) {
 
 			if(length-7 == 0) {
 				String eightAligned = "1" + sevenAligned.substring(index,index+7);
 				String temp = Integer.toHexString(Integer.parseInt(eightAligned,2));
 				if(temp.length() == 1)
 					out.append("0"+temp);
 				else
 					out.append(temp);
 			}
 			else {
 				String eightAligned = "0" + sevenAligned.substring(index, index+7);
 				String temp = Integer.toHexString(Integer.parseInt(eightAligned,2));
 				if(temp.length() == 1)
 					out.append("0"+temp);
 				else
 					out.append(temp);
 			}
 			index += 7;
 			length -= 7;
 		}
 		return out.toString();
 	}
 
 	public static Vector<Integer> decode(Vector<String> encoded) {
 		Vector<Integer> out = new Vector<Integer>();
 		for(String str : encoded) {
 			out.add(decodeCorrected(str));
 		}
 		return deltaDecode(out);
 	}
 
 	private static Vector<Integer> deltaDecode(Vector<Integer> encoded) {
 		Vector<Integer> out = new Vector<Integer>();
 		out.add(encoded.elementAt(0));
 		for(int i = 1; i < encoded.size(); i++) {
 			out.add(i, encoded.elementAt(i)+out.elementAt(i-1));
 		}
 		return out;
 	}
 
 	private static int decodeCorrected(String hex) {
 		int buffer = 0;
 		int length = hex.length();
 		if(hex.length() % 2 != 0)
 			buffer  = 8 - hex.length() % 8;
 		StringBuilder twoAligned = new StringBuilder();
 		while(buffer != 0) {
 			twoAligned.append("0");
 			buffer--;
 		}
 		twoAligned.append(hex);
 		StringBuilder outBinary = new StringBuilder();
 		int index = 0;
 		while(length != 0) {
 			outBinary.append(hexToBinary(twoAligned.substring(index, index+2)));
 			length -=2;
 			index +=2;
 			if(length == 0)
 				break;
 		}
 
 		return Integer.parseInt(outBinary.toString(),2);
 	}
 
 	private static String hexToBinary(String hex) {
 		String tempStr = "";
 		//tempStr = temp;
 		int i = Integer.parseInt(hex, 16);
 		String Bin = Integer.toBinaryString(i);
 		int buffer = 0;
 		if(Bin.length() % 8 != 0)
 			buffer  = 8 - Bin.length() % 8;
 		StringBuilder eightAligned = new StringBuilder();
 		while(buffer != 0) {
 			eightAligned.append("0");
 			buffer--;
 		}
 		eightAligned.append(Bin);
 		tempStr = eightAligned.toString().substring(1);
 		return tempStr;
 	}
 
 
 
 	@Override
 	public void constructIndex() throws IOException {
 
 		DocumentProcessor documentProcessor = new DocumentProcessor();
 
 		File contentFolder = new File(contentFolderName);
 		int fileCount = 0;
 
 		for(File file : contentFolder.listFiles()){
 
 			processDocument(file, documentProcessor);
 			fileCount++;
 
 			if(fileCount % 100 == 0){
 				saveIndexInFile();
 			}
 
 			System.out.println("processing file "+fileCount+" out of"+contentFolder.listFiles().length);
 		}
 
 		saveIndexInFile();
 		mergeFile();
 
 		System.out.println(
 				"Indexed " + Integer.toString(_numDocs) + " docs with " +
 				Long.toString(_totalTermFrequency) + " terms.");
 
 		String indexFile = _options._indexPrefix + "/"+indexFolderName+"/"+indexFileName;
 		System.out.println("Store index to: " + indexFile);
 		ObjectOutputStream writer =
 			new ObjectOutputStream(new FileOutputStream(indexFile));
 		writer.writeObject(this);
 		writer.close();
 
 	}
 
 	private void mergeFile()
 	{
 		//Final index file
 		T3FileWriter indexWriter = new T3FileWriter(_options._indexPrefix+"/Comp_index/"+(finalIndexCount++)+".idx");
 
		File indexDirectory = new File(_options._indexPrefix+"/temp");
 		Gson gson = new Gson();
 
 		if(indexDirectory.isDirectory())
 		{
 			indexWriter.write("{");
 			for(int  i = 0 ; i < _dictionary.size();i++){
 
 				System.out.println("Merging indexes "+i+" out of "+_dictionary.size()+" terms");
 
 				//get posting list of term_id i from all the files and merge them
 				List<Integer> mergedPostingList = new ArrayList<Integer>();
 
 				File[] files = indexDirectory.listFiles();
 
 				Comparator<File> comp = new Comparator<File>()
 				{
 					public int compare(File f1, File f2)
 					{
 
 						// Alphabetic order otherwise
 						Integer fileIndex1 = Integer.parseInt(f1.getName().replaceFirst(".idx",""));
 						Integer fileIndex2 = Integer.parseInt(f2.getName().replaceFirst(".idx",""));
 						return fileIndex1.compareTo(fileIndex2);
 					}
 				};
 				Arrays.sort(files, comp); 
 
 				for(File indexTempFile : files)
 				{
 					if(scanners.get(indexTempFile.getName()) == null)
 					{
 						try 
 						{
 							Scanner scanner = new Scanner(indexTempFile);
 							scanner.useDelimiter("],");
 							scanners.put(indexTempFile.getName(),scanner);
 
 						} catch (FileNotFoundException e) 
 						{
 							e.printStackTrace();
 						}
 					}
 
 					String postingList = getPostingList(indexTempFile , i );
 
 					//sample posting list = [1,2,3,4,5]
 					if(postingList != null){
 						try{
 
 							if(postingList.charAt(postingList.length()-2)== '}'){
 								postingList = postingList.substring(0,postingList.length()-2);
 							}
 
 							int[] intList = gson.fromJson(postingList, int[].class); 
 							mergedPostingList.addAll(asList(intList));
 						}catch(Exception e){
 							e.printStackTrace();
 						}
 					}
 				}
 
 				//Write the merger list to file i
 				if(i%1000 == 0 && i >0 ){
 					indexWriter.write("}");
 					indexWriter.close();
 					indexWriter = new T3FileWriter(_options._indexPrefix+"/Comp_index/"+(finalIndexCount++)+".idx");
 					indexWriter.write("{");
 				}
 				String entry = "\""+i+"\""+":"+gson.toJson(mergedPostingList);
 				indexWriter.write(entry);
 				if((i+1)%1000 != 0){
 					indexWriter.write(",");
 				}
 
 				if(i == _dictionary.size() -1){
 					indexWriter.write("}");
 					indexWriter.close();
 				}
 			}
 		}
 
 		indexWriter.close();
 		//clean temp files 
 		deleteTempFiles();
 	}
 
 
 	private void deleteTempFiles() 
 	{
 		File directory = new File(_options._indexPrefix+"/Comp_temp");
 		try{
 			delete(directory);
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 
 	public static void delete(File file) throws IOException{
 
 		if(file.isDirectory()){
 
 			//directory is empty, then delete it
 			if(file.list().length==0){
 
 				file.delete();
 				System.out.println("Directory is deleted : " 
 						+ file.getAbsolutePath());
 
 			}else{
 
 				//list all the directory contents
 				String files[] = file.list();
 
 				for (String temp : files) {
 					//construct the file structure
 					File fileDelete = new File(file, temp);
 
 					//recursive delete
 					delete(fileDelete);
 				}
 
 				//check the directory again, if empty then delete it
 				if(file.list().length==0){
 					file.delete();
 					System.out.println("Directory is deleted : " 
 							+ file.getAbsolutePath());
 				}
 			}
 
 		}else{
 			//if file, then delete it
 			file.delete();
 			System.out.println("File is deleted : " + file.getAbsolutePath());
 		}
 	}
 
 
 	public List<Integer> asList(int[] ints)
 	{
 		List<Integer> intList = new ArrayList<Integer>();
 		for (int index = 0; index < ints.length; index++)
 		{
 			intList.add(ints[index]);
 		}
 		return intList;
 	}
 
 	private String getPostingList(File indexTempFile, int term_id)
 	{
 		Scanner scanner = scanners.get(indexTempFile.getName());
 
 		while(scanner.hasNext()){
 
 			String nextElement ;
 			if(pointerToScanners.get(indexTempFile.getName()) == null){
 				nextElement = scanner.next();
 				nextElement += "]";
 
 			}else{
 				nextElement = pointerToScanners.get(indexTempFile.getName());
 			}
 
 
 			nextElement = nextElement.substring(nextElement.indexOf("\""));
 
			String currentTerm_id =nextElement.substring(nextElement.indexOf("\"")+1,nextElement.lastIndexOf("\""));
 
 			if(term_id == Integer.parseInt(currentTerm_id)){
 				pointerToScanners.remove(indexTempFile.getName());
 				return nextElement.substring(nextElement.indexOf(":")+1);
 			}
 
 			if(Integer.parseInt(currentTerm_id) > term_id){
 				pointerToScanners.put(indexTempFile.getName(), nextElement);
 				break;
 			}else{
 				System.out.println("If this line is showing up then something went terribly wrong....");
 			}
 		}
 
 		return null;
 	}
 
 	private void saveIndexInFile() {
 
 		System.out.println("Saving file "+fileId);
 
 		T3FileWriter fileWriter= new T3FileWriter(_options._indexPrefix+"/Comp_temp/"+(fileId++)+".idx");
 		GsonBuilder builder = new GsonBuilder();
 
 		Gson gson =
 			builder.enableComplexMapKeySerialization().setPrettyPrinting().create();
 		Type type = new TypeToken<HashMap<Integer, PostingsWithOccurences<String>>>(){}.getType();
 		String json = gson.toJson(_invertedIndexWithCompresion, type);
 
 		fileWriter.write(json);
 		fileWriter.close();
 
 		fileWriter= new T3FileWriter(_options._indexPrefix+"/Comp_Documents/"+(docId++)+".idx");
 		json = gson.toJson(_documents);
 		fileWriter.write(json);
 		fileWriter.close();
 
 		clearMem();
 	}
 
 
 	private void clearMem() 
 	{
 		_invertedIndexWithCompresion.clear();
 		_documents.clear();
 	}
 
 
 	/**
 	 * Process the raw content (i.e., one line in corpus.tsv) corresponding to a
 	 * document, and constructs the token vectors for both title and body.
 	 * @param content
 	 */
 	private void processDocument(File file, DocumentProcessor documentProcessor) {
 		try{
 
 			//System.out.println(file.getName());
 
 			Vector<String> titleTokens_Str = documentProcessor.process(file.getName());
 			Vector<String> bodyTokens_Str = documentProcessor.process(file);
 
 			Vector<Integer> titleTokens = new Vector<Integer>();
 			readTermVector(titleTokens_Str, titleTokens);
 
 			Vector<Integer> bodyTokens = new Vector<Integer>();
 			readTermVector(bodyTokens_Str, bodyTokens);
 
 			//Document tokens
 			Vector<Integer> documentTokens = bodyTokens;
 			documentTokens.addAll(titleTokens);
 
 			String title = file.getName();
 
 			Integer documentID = documentsCount++;
 			int numView = numViews.get(documentID);
 			double pageRank = pageRanks[documentID];
 			DocumentIndexed doc = new DocumentIndexed(documentID, null);
 			doc.setTitle(title);
 			doc.setNumViews(numView);
 			doc.setPageRank((float) pageRank);
 			doc.setDocumentTokens(documentTokens);
 			_documents.add(doc);
 			_docIds.put(title, documentID);
 			++_numDocs;
 
 			Set<Integer> uniqueTerms = new HashSet<Integer>();
 			updateStatistics(documentID, doc.getDocumentTokens(), uniqueTerms);
 
 			for (Integer idx : uniqueTerms) {
 				_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
 			}
 
 		}catch(FileNotFoundException fnfe){
 			fnfe.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Tokenize {@code content} into terms, translate terms into their integer
 	 * representation, store the integers in {@code tokens}.
 	 * @param content
 	 * @param tokens
 	 */
 	private void readTermVector(Vector<String> tokens_str, Vector<Integer> tokens) {
 		for (String token : tokens_str) {
 			int idx = -1;
 			if (_dictionary.containsKey(token)) {
 				idx = _dictionary.get(token);
 			} else {
 				idx = _terms.size();
 				_terms.add(token);
 				_dictionary.put(token, idx);
 				_termCorpusFrequency.put(idx, 0);
 				_termDocFrequency.put(idx, 0);
 			}
 			tokens.add(idx);
 		}
 		return;
 	}
 
 
 	/**
 	 * Update the corpus statistics with {@code tokens}. Using {@code uniques} to
 	 * bridge between different token vectors.
 	 * @param tokens
 	 * @param uniques
 	 */
 	@SuppressWarnings("unchecked")
 	private void updateStatistics(Integer documentID, Vector<Integer> tokens, Set<Integer> uniques) {
 
 		for(int i=0; i<tokens.size(); i++){
 
 			Integer idx = tokens.get(i);
 			uniques.add(idx);
 
 			//populates the inverted index
 			if(!_invertedIndexWithCompresion.containsKey(idx)){
 				_invertedIndexWithCompresion.put(idx, new PostingsWithOccurences<String>());
 				_sumOfOffsets.put(idx, new HashMap<Integer,Integer>());
 			}
 			if(!_sumOfOffsets.get(idx).containsKey(documentID))
 				_sumOfOffsets.get(idx).put(documentID, 0);
 
 			int tempSum = _sumOfOffsets.get(idx).get(documentID);
 			String temp = hexOut(i+1-tempSum);
 			_invertedIndexWithCompresion.get(idx).addEntry(documentID, temp); //offset start from 1
 			//			_sumOfOffsets.get(idx).put(documentID, tempSum+i+1);
 			_sumOfOffsets.get(idx).put(documentID, i+1);
 			_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
 			++_totalTermFrequency;
 
 			//populating the docTermFrequency index
 			//			if(!_docTermFrequencyInvertedIndex.containsKey(idx)){
 			//				_docTermFrequencyInvertedIndex.put(idx, new HashMap<Integer, Integer>());
 			//			}
 			//			Map<Integer, Integer> termDocFrequencyList = _docTermFrequencyInvertedIndex.get(idx);
 			//
 			//			if(!termDocFrequencyList.containsKey(documentID)){
 			//				termDocFrequencyList.put(documentID, new Integer(1));
 			//			}else{
 			//				termDocFrequencyList.put(documentID, termDocFrequencyList.get(documentID)+1);
 			//			}
 		}
 	}
 
 
 
 	@Override
 	public void loadIndex() throws IOException, ClassNotFoundException {
 		String indexFile = _options._indexPrefix + "/"+indexFileName;
 		System.out.println("Load index from: " + indexFile);
 
 		ObjectInputStream reader =
 			new ObjectInputStream(new FileInputStream(indexFile));
 		IndexerInvertedCompressed loaded = (IndexerInvertedCompressed) reader.readObject();
 
 		this._documents = loaded._documents;
 		// Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
 		this._numDocs = _documents.size();
 		for (Integer freq : loaded._termCorpusFrequency.values()) {
 			this._totalTermFrequency += freq;
 		}
 		this._dictionary = loaded._dictionary;
 		this._docIds = loaded._docIds;
 		this._terms = loaded._terms;
 		this._termCorpusFrequency = loaded._termCorpusFrequency;
 		this._termDocFrequency = loaded._termDocFrequency;
 		reader.close();
 
 		System.out.println(Integer.toString(_numDocs) + " documents loaded " +
 				"with " + Long.toString(_totalTermFrequency) + " terms!");
 	}
 
 
 
 	@Override
 	public DocumentIndexed getDoc(int docid) {
 
 		//check if it's available
 		for(int i = 0 ; i< _documents.size(); i++){
 			if(_documents.get(i)._docid == docid){
 				return _documents.get(i);
 			}
 		}
 
 		//if not then retrieve from backend store
 		_documents = getDocuments(docid);
 
 		//again check if its available
 		for(int i = 0 ; i< _documents.size(); i++){
 			if(_documents.get(i)._docid == docid){
 				return _documents.get(i);
 			}
 		}
 
 		return null;
 		//return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
 	}
 
 	private Vector<DocumentIndexed> getDocuments(int docid2) {
 
 		int file_no = (int)docid2 /100 + 1;
 		String filepath = _options._indexPrefix+"/Comp_Documents/"+file_no+".idx";
 		T3FileReader fileReader = new T3FileReader(filepath);
 		String fileContents = fileReader.readAllBytes();
 		fileReader.close();
 
 		Gson gson = new Gson();
 
 		JsonParser parser = new JsonParser();
 		JsonArray array = parser.parse(fileContents).getAsJsonArray();
 
 		Vector<DocumentIndexed> retVal = new Vector<DocumentIndexed>();
 		for(int i =0;i<array.size();i++){
 			DocumentIndexed doc = gson.fromJson(array.get(i), DocumentIndexed.class);
 			retVal.add(doc);
 		}
 
 		return retVal;
 	}
 
 
 	/**
 	 * In HW2, you should be using {@link DocumentIndexed}.
 	 */
 	@Override
 	public DocumentIndexed nextDoc(Query query, int docid) {
 
 		Vector<String> queryTerms = query._tokens;
 
 		if(queryTerms.size() == 0)
 			return null;
 
 		if(queryTerms.size() == 1){
 			Integer nextDocID = next(queryTerms.get(0), docid);
 			if(nextDocID == INFINITY)
 				return null;
 			return getDoc(nextDocID);
 		}
 
 		//case 1 
 		Vector <Integer> docIds = new Vector<Integer>();
 		for(String token : queryTerms) {
 			Integer nextDocID = next(token,docid);
 			if(nextDocID == INFINITY) {
 				//value not found;
 				return null;
 			}
 			docIds.add(nextDocID);
 		}
 
 		//case 2 
 		boolean documentFound = true;
 		for(int i = 0 ; i < docIds.size() ; i++) {
 			if(docIds.get(i) != docIds.get(0)){
 				documentFound = false;
 				break;
 			}
 		}
 
 		if(documentFound) {
 			return getDoc(docIds.get(0));
 		}
 
 		//case 3 
 		Integer maxDocID = Collections.max(docIds);
 
 		return nextDoc(query, maxDocID-1);
 	}
 
 
 	/**
 	 * Finds the next document containing the term.
 	 * If not found then it returns Integer.Maxvalue
 	 * @param term
 	 * @param docid 
 	 * @return
 	 */
 	private int next(String term , int currentDoc) {
 
 		if(!_dictionary.containsKey(term))
 			return INFINITY;
 
 		PostingsWithOccurences<String> postingList = null;
 
 		int termID = _dictionary.get(term);
 		
 		if(postingLists.containsKey(termID)){
 			postingList = postingLists.get(termID);
 		}else{
 			postingList = _invertedIndexWithCompresion.get(termID);
 			postingLists.put(termID, postingList);
 		}
 
 		Integer lt = postingList.size();
 		Integer ct = postingList.getCachedIndex();
 
 		if(lt == 0 || postingList.get(lt-1).getDocID() <= currentDoc) {
 			return INFINITY;
 		}
 
 		if(postingList.get(0).getDocID() > currentDoc) {
 			postingList.setCachedIndex(0);
 			return postingList.get(0).getDocID();
 		}
 
 		if(ct > 0 && postingList.get(ct-1).getDocID() > currentDoc) {
 			ct = 0;
 		}
 
 		while(postingList.get(ct).getDocID() <= currentDoc) {
 			ct++;
 		}
 		
 		postingList.setCachedIndex(ct);
 		
 		return postingList.get(ct).getDocID();
 	}
 
 	private HashMap<Integer, PostingsWithOccurences<String>> getIndex(Integer integer) {
 
 		int file_no = (int)integer /1000 + 1;
 		String filepath = _options._indexPrefix+"/Comp_index/"+file_no+".idx";
 		T3FileReader fileReader = new T3FileReader(filepath);
 		String fileContents = fileReader.readAllBytes();
 
 		GsonBuilder builder = new GsonBuilder();
 		Gson gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create();
 		Type type = new TypeToken<HashMap<Integer, PostingsWithOccurences<String>>>(){}.getType();
 		HashMap<Integer, PostingsWithOccurences<String>> treeMap = gson.fromJson(fileContents, type);
 
 		return treeMap;
 	}
 
 
 	/**
 	 *Finds the next Phrase.
 	 */
 	@Override
 	public int nextPhrase(Query query, int docid, int position) {
 
 		Document document_verfiy = nextDoc(query, docid-1);
 		if(document_verfiy._docid != docid)
 			return INFINITY;
 
 		Vector<String> queryTerms = query._tokens;
 
 		//case 1 
 		Vector <Integer> positions = new Vector<Integer>();
 		for(String token : queryTerms) {
 			Integer nextPosition = nextPosition(token,docid, position);
 			if(nextPosition == INFINITY) {
 				//value not found;
 				return INFINITY;
 			}
 			positions.add(nextPosition);
 		}
 
 		//case 2 
 		boolean documentFound = true;
 
 		for(int i = 0 ; i < positions.size()-1 ; i++) {
 			if(positions.get(i) + 1 != positions.get(i+1)){
 				documentFound = false;
 				break;
 			}
 		}
 
 		if(documentFound) {
 			return positions.get(0);
 		}
 
 		//case 3 
 		return nextPhrase(query, docid, Collections.max(positions));
 	}
 
 
 	/**
 	 * Finds the next position of the term in document.
 	 * If not found then it returns Integer.MAXVALUE
 	 * @param term
 	 * @param docid 
 	 * @return
 	 */
 	private int nextPosition(String term ,int docId, int pos) {
 
 		if(!_dictionary.containsKey(term))
 			return INFINITY;
 
 
 		PostingsWithOccurences<String> postingList = null;
 		int termID = _dictionary.get(term);
 		
 		if(postingLists.containsKey(termID)){
 			postingList = postingLists.get(termID);
 		}else{
 			postingList = _invertedIndexWithCompresion.get(termID);
 			postingLists.put(termID, postingList);
 		}
 		
 		PostingEntry<String> documentEntry = postingList.searchDocumentID(docId);
 
 		if(documentEntry != null && documentEntry.getDocID() == docId){
 			Vector<String> strOffsets = documentEntry.getOffset();
 			Vector<Integer> offsets = decode(strOffsets);
 			for(int i=0; i<offsets.size()-1; i++){
 				if(offsets.get(i) == pos){
 					return offsets.get(i+1);
 				}
 			}
 		}
 
 		return INFINITY;
 	}
 
 	@Override
 	public int corpusDocFrequencyByTerm(String term) {
 		return _termDocFrequency.get(_dictionary.get(term));
 	}
 
 	@Override
 	public int corpusTermFrequency(String term) {
 		return _termCorpusFrequency.get(_dictionary.get(term));
 	}
 
 	@Override
 	public int documentTermFrequency(String term, String url) {
 
 		if(!_dictionary.containsKey(term))
 			return 0;
 
 		int term_idx = _dictionary.get(term);
 		int docID = _docIds.get(url);
 		PostingsWithOccurences<String> list = 
 			_invertedIndexWithCompresion.get(term_idx);
 
 		PostingEntry<String> entry = list.searchDocumentID(docID);
 
 		return entry.getOffset().size();
 	}
 }
