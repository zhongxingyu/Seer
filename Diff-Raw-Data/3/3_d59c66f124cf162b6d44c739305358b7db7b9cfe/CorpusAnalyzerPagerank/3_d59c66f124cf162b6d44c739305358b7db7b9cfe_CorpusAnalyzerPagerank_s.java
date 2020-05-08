 package edu.nyu.cs.cs2580;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import edu.nyu.cs.cs2580.SearchEngine.Options;
 import edu.nyu.cs.cs2580.util.Util;
 
 /**
  * @CS2580: Implement this class for HW3.
  */
 public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
 	public CorpusAnalyzerPagerank(Options options) {
 		super(options);
 	}
 
 	final int linksBlockSize = 2000;
 	final String tempFolder = "data/temp/";
 	final String graphFile = "data/corpus.graph";
 	final String pageRanksFile = "data/pageranks";
 
 	/**
 	 * This function processes the corpus as specified inside {@link _options}
 	 * and extracts the "internal" graph structure from the pages inside the
 	 * corpus. Internal means we only store links between two pages that are both
 	 * inside the corpus.
 	 * 
 	 * Note that you will not be implementing a real crawler. Instead, the corpus
 	 * you are processing can be simply read from the disk. All you need to do is
 	 * reading the files one by one, parsing them, extracting the links for them,
 	 * and computing the graph composed of all and only links that connect two
 	 * pages that are both in the corpus.
 	 * 
 	 * Note that you will need to design the data structure for storing the
 	 * resulting graph, which will be used by the {@link compute} function. Since
 	 * the graph may be large, it may be necessary to store partial graphs to
 	 * disk before producing the final graph.
 	 *
 	 * @throws IOException
 	 */
 	@Override
 	public void prepare() throws IOException {
 		System.out.println("Preparing " + this.getClass().getName());
 
 		//create temporary folders
 		File directory = new File(tempFolder);
 		if(!directory.exists()){ directory.mkdirs(); }
 
 		directory = new File(tempFolder+"graph");
 		if(!directory.exists()){ directory.mkdirs(); }
 
 		int blockSize = linksBlockSize;
 		int blockNumber = 0;
 
 		try{
 
 			File corpusDirectory = new File(_options._corpusPrefix);
 
 			//returns if the corpus prefix is not a directory
 			if(!corpusDirectory.isDirectory()){
 				return;
 			}
 
 			//Maps page link to ID
 			Map<String, Integer> pages = new HashMap<String, Integer>();
 			int pageCount = -1;
 			for(File page : corpusDirectory.listFiles()){
 				pages.put(page.getName(), ++pageCount);
 			}
 
 			//Maps Link ID to set of outcoming links
 			Map<Integer, Set<Integer>> outgoingLinks = new HashMap<Integer, Set<Integer>>();
 
 			for(File page : corpusDirectory.listFiles()){
 
 				HeuristicLinkExtractor extractor = new HeuristicLinkExtractor(page);
 				String sourcePageLink = extractor.getLinkSource();
 				Integer sourcePageID;
 				
 				//continue if the page is not present in the corpurs.
 				if((sourcePageID = pages.get(sourcePageLink)) == null){
 					continue;
 				}
 
 				String targetPageLink = null;
 
 				while((targetPageLink = extractor.getNextInCorpusLinkTarget())!=null){
 
 					Integer targetLinkID;
 					if((targetLinkID = pages.get(targetPageLink)) == null){
 						continue;
 					}
 
 					Set<Integer> links;
 					if((links = outgoingLinks.get(sourcePageID)) == null){
 						links = new HashSet<Integer>();
 						outgoingLinks.put(sourcePageID, links);
 					}
 					links.add(targetLinkID);
 
 				}
 
 				blockSize--;
 				//write the temp link list to file
 				if(blockSize == 0){
 					Util.writeTempGraphToFile(outgoingLinks, tempFolder+"graph/graph"+blockNumber);
 					outgoingLinks.clear();
 					blockNumber++;
 					blockSize = linksBlockSize;
 				}
 
 			}
 
 			//write remaining links
 			Util.writeTempGraphToFile(outgoingLinks, tempFolder+"graph/graph"+blockNumber);
 			outgoingLinks.clear();
 			outgoingLinks = null;
 			blockNumber++;
 			blockSize = linksBlockSize;
 
 			//Merge all the temp files
 			Util.mergeGraphFiles(tempFolder+"graph/", graphFile, pageCount+1);
 
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		return;
 	}
 
 	/**
 	 * This function computes the PageRank based on the internal graph generated
 	 * by the {@link prepare} function, and stores the PageRank to be used for
 	 * ranking.
 	 * 
 	 * Note that you will have to store the computed PageRank with each document
 	 * the same way you do the indexing for HW2. I.e., the PageRank information
 	 * becomes part of the index and can be used for ranking in serve mode. Thus,
 	 * you should store the whatever is needed inside the same directory as
 	 * specified by _indexPrefix inside {@link _options}.
 	 *
 	 * @throws IOException
 	 */
 	@Override
 	public void compute() throws IOException {
 		System.out.println("Computing using " + this.getClass().getName());
 
 		FileReader fileReader = new FileReader(graphFile);
 		BufferedReader graphReader = new BufferedReader(fileReader);
 
 		int numberofIterations = 2;
 		double lambda = 0.1;
 
 		try{
 
 			int totalPages = Integer.parseInt(graphReader.readLine());
 			double[] currentPageRank = new double[totalPages];
 			double[] resultingPageRank = new double[totalPages];
 
 			//start with each page been equally likely
 			double startingPageRank = 1.0/totalPages;
 			for(int i=0; i<currentPageRank.length; i++){
 				currentPageRank[i] = startingPageRank;
 			}
 
 
 			while(numberofIterations > 0){
 				
 				//each page has lambda/totalpages chance of random selection.
 				for(int i=0; i<resultingPageRank.length; i++){
 					resultingPageRank[i] = lambda/totalPages;
 				}
 
 				String entry = null;
 				while((entry = graphReader.readLine()) != null){
 					String[] entries = entry.trim().split("\\s+");
 					if(entries.length==0){
 						continue;
 					}
 
 					int pageID = Integer.parseInt(entries[0]);
 					int qTotal = entries.length-1;
 
 					if(entries.length > 1){ //page has outgoing links
 						for(int q=1;q<entries.length;q++){
 							int targetPageID = Integer.parseInt(entries[q]);
 							//probability of being at target page 
 							resultingPageRank[targetPageID] += (1-lambda) * (currentPageRank[pageID] / qTotal);
 						}
 					}else{ //no outgoing links
 						for(int q=0;q<totalPages;q++){
 							//probability is divided evenly among all the pages.
 							resultingPageRank[q] += (1-lambda) * (currentPageRank[pageID] / totalPages);
 						}
 					}
 
 					//update current pageRank estimate
 					for(int i=0; i<totalPages; i++){
 						currentPageRank[i] = resultingPageRank[i];
 					}
 				}
 
 				--numberofIterations;
 				graphReader.close();
 				
 				//reset the graph file to start reading from the start
 				if(numberofIterations > 0){
 					graphReader = new BufferedReader(fileReader);
 					graphReader.readLine(); //skip the first line.
 				}
 				
 			}
 
 			//write all pageranks to file
 			Util.writePageRanks(resultingPageRank, pageRanksFile);
 
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			graphReader.close();
 			fileReader.close();
 		}
 
 		return;
 	}
 
 	/**
 	 * During indexing mode, this function loads the PageRank values computed
 	 * during mining mode to be used by the indexer.
 	 *
 	 * @throws IOException
 	 */
 	@Override
 	public Object load() throws IOException {
 		System.out.println("Loading using " + this.getClass().getName());
 		double[] pageRanks = null;
 
 		FileReader fileReader = new FileReader(pageRanksFile);
 		BufferedReader bufferedReader = new BufferedReader(fileReader);
 
 		try{
 
 			int totalPages = Integer.parseInt(bufferedReader.readLine());
 			pageRanks = new double[totalPages];
 
 			String line = null;
 			while((line = bufferedReader.readLine()) != null){
 				String[] entry = line.trim().split("\\s+");
 				if(entry.length < 2) continue;
 				int pageID = Integer.parseInt(entry[0]);
 				double pageRank = Double.parseDouble(entry[1]);
 				pageRanks[pageID] = pageRank;
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			bufferedReader.close();
 			fileReader.close();
 		}
 
 		return pageRanks;
 	}
 }
