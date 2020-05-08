 package main;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Random;
import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import ICS.SND.Entities.Author;
 import ICS.SND.Entities.DivergencePaper;
 import ICS.SND.Entities.Entry;
 import ICS.SND.Entities.LDABase;
 import ICS.SND.Entities.Query;
 import ICS.SND.Interfaces.IEntry;
 import ICS.SND.Interfaces.IQuery;
 import ICS.SND.Tests.AuthorTest;
 import ICS.SND.Tests.UnitTests;
 import ICS.SND.Utilities.DivergencePaperComparator;
 import ICS.SND.Utilities.Providers.EntryProvider;
 import ICS.SND.Utilities.Providers.HibernateDataProvider;
 
 import com.aliasi.stats.Statistics;
 
 public class Main {
     private static final Logger log = Logger.getLogger(Main.class);
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 	    //Call your function here.
 	}	
 	private static void listAuthorEntry() throws IOException {
 		Hashtable<Integer, ArrayList<Integer>> table = new Hashtable<Integer, ArrayList<Integer>>(); 
 		
 		FileReader fr = new FileReader("C:/KiyanHadoop/EntryNAuthorId.txt");
 		BufferedReader br = new BufferedReader(fr);
 		FileWriter fw = new FileWriter("C:/KiyanHadoop/AuthorNEntryId.txt");
 		BufferedWriter writer = new BufferedWriter(fw);
 		String line ="";
 		String[] splitS, authorIds;
 		String paperID;
 		ArrayList<Integer> paperList;
 //		int count = 10;
 		while((line=br.readLine())!=null){
 			splitS = line.split("<break>");
 			paperID = splitS[0];
 			authorIds = splitS[1].split(",");
 			if(authorIds!=null && authorIds.length >0){
 				for(String authorId : authorIds){
 					paperList = table.get(Integer.parseInt(authorId));
 					if(paperList==null){
 						paperList = new ArrayList<Integer>();
 						paperList.add(Integer.parseInt(paperID));
 						table.put(Integer.parseInt(authorId), paperList);
 						continue;
 					}
 					paperList.add(Integer.parseInt(paperID));
 				}	
 			}
 			else{
 				System.out.println("This paper has no author: " + paperID);
 			}
 //			count--;
 		}
 		for(int i=0;i<1033301;i++){
 			paperList = table.get(i);
 			if(paperList!=null && paperList.size()>0){
 				writer.write(i + "<break>");
 				for(int j : paperList){
 					writer.write(j+ ",");
 				}
 				writer.write("\n");
 			}
 			else{
 				System.out.println("This author has no paper: " + i);
 			}
 		}
 		writer.flush();
 	}
 	private static void listEntryAuthors() throws IOException{
 		FileWriter fw = new FileWriter("C:/KiyanHadoop/EntryNAuthorId.txt");
         BufferedWriter writer = new BufferedWriter(fw);
         Entry e;
         Set <Author> aSet;
         Author a;
         EntryProvider provider = new EntryProvider();
         for(int i=0;i<1632444;i++){
         	e = (Entry) provider.LoadByIndexNumber(Integer.toString(i));
         	if(e!=null){
         		aSet = e.getAuthors();
         		if(aSet.size()>0){
         			java.util.Iterator<Author> iter = aSet.iterator();
 //                	System.out.print(e.getIndexNumber() + "<break>");
                 	writer.write(e.getIndexNumber() + "<break>");
                 	while(iter.hasNext()){
 //                		System.out.print(iter.next().getAuthorId()+",");
                 		writer.write(iter.next().getAuthorId()+",");
                 	}
 //                	System.out.print("\n");
                 	writer.write("\n");	
         		}	
         	}
         }
         writer.flush();
         writer.close();
 	}
 	private static void listByAuthors() {
 	    FileWriter fw;
         try {
             fw = new FileWriter("AuthorOutput.txt");
             BufferedWriter writer = new BufferedWriter(fw);
             
             HibernateDataProvider<Author> authorProvider 
                 = new HibernateDataProvider<Author>();
             EntryProvider entryProvider = new EntryProvider();
             
             IQuery qry = new Query("from Author");
             List<Author> authors = authorProvider.List(qry);
             
             for(Author author : authors) {
                 log.debug(MessageFormat.format("author: {0}", author.getAuthorName()));
                 List<IEntry> entries = entryProvider.ListByAuthor(author);
                 writer.write(MessageFormat.format("{0}<break>", 
                         author.getAuthorId()));
                 for(IEntry entry : entries) {
                     log.debug(MessageFormat.format("\t* paper: {0}", 
                             entry.getTitle()));
                     writer.write(MessageFormat.format("{0},", 
                             entry.getIndexNumber()));
                 }
                 writer.write("\n");
                 writer.flush();
             }
             writer.close();
             fw.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     private static void sortList() throws IOException{
         // TODO Auto-generated method stub
 	    BufferedReader br = new BufferedReader(new FileReader("C:/KiyanHadoop/KLOutputFiles/DivergenceRun5.txt"));
 	    FileWriter fw = new FileWriter("C:/KiyanHadoop/KLOutputFiles/SortedDivergenceRun5.txt");
         BufferedWriter writer = new BufferedWriter(fw);
         String line;
         String[] lines = new String[700000];
         int count =0;
         while((line=br.readLine())!=null){
             lines[count]= line;
             count++;
         }
         String[] splitS;
         
         DivergencePaper dp = new DivergencePaper("index~abc~title~blah");
         int dpListLength = 10000;
         DivergencePaper[] dpList = new DivergencePaper[dpListLength];
         for(int k=0;k<dpListLength;k++){
             dpList[k] = new DivergencePaper("index~abc~title~blah");
             dpList[k].setDivergence(10000);
         }
         int dpCount = 0;
         for(int k=0;k<count;k++){
             splitS = lines[k].split("\\t");
             if(Double.parseDouble(splitS[1])<1){
                 dp = new DivergencePaper("index~abc~title~blah");
                 dp.setDivergence(Double.parseDouble(splitS[1]));
                 dp.setTitle(splitS[2]);
                 dp.setIndexNumber(splitS[0]);
                 dpList[dpCount]=dp;
                 dpCount++;
                 if(dpCount == dpListLength){
                     break;
                 }
             }
         }
         Arrays.sort(dpList);
         
         for(int k=0;k<dpCount;k++){
             writer.write(dpList[k].toString() + "\n");
         }
         writer.flush();
     }
     private static void getUnsortedList() throws IOException{
 	    
 	    LDABase lbase = new LDABase(UnitTests.DATA_PATH +"ldaSeedInput.txt");
 
         PrintStream ps = new PrintStream(new File("C:/KiyanHadoop/ldaKLOutput.txt"));
         System.setOut(ps);
 
         lbase.startEpochs();
         String line = "";
         double[][] seedPaperTopicProb = new double[lbase.sample.numDocuments()][lbase.sample.numTopics()];
         
         
         for(int i=0; i<lbase.sample.numDocuments(); i++){
             for(int j=0;j< lbase.sample.numTopics();j++){
                 seedPaperTopicProb[i][j] = lbase.sample.documentTopicProb(i, j);
             }
         }
 
         String[] lines = new String[700000];
         BufferedReader br = new BufferedReader(new FileReader("C:/KiyanHadoop/KLDivergenceTest.txt"));
 //      BufferedReader br = new BufferedReader(new FileReader(UnitTests.DATA_PATH +"ldaSeedInput.txt"));
         int count=0;
         while((line=br.readLine())!=null){
             lines[count]= line;
             count++;
         }
         int[] docTokens;
         double[] bayesEstimate;
         DivergencePaper dp = new DivergencePaper(lines[0]);
         double currDivergence = 10000;
         String[] splitS;
         
         FileWriter fw = new FileWriter("C:/KiyanHadoop/KLOutputFiles/DivergenceRun5.txt");
         BufferedWriter writer = new BufferedWriter(fw);    
         for(int k=0;k<count;k++){
             dp.setDivergence(10000);
             for(int i=0;i<lbase.sample.numDocuments();i++){
                 docTokens = lbase.getDocumentTokens(lines[k]);
                 splitS = lines[k].split("\\~");
                 dp.setTitle(splitS[2]);
                 dp.setIndexNumber(splitS[0]);
                 
                 if(docTokens.length==0){   
                     continue;
                 }
                 
                 bayesEstimate = lbase.lda.bayesTopicEstimate(docTokens, 100, lbase.burninEpochs, lbase.sampleLag, new Random());
                 for(int j=0;j<bayesEstimate.length;j++){
                     if(bayesEstimate[j]==0){
                         bayesEstimate[j]=0.0000000001;
                     }
                 }
                 
                 line = lines[k];
                 currDivergence = Statistics.symmetrizedKlDivergence(seedPaperTopicProb[i], bayesEstimate);
                 if(currDivergence < dp.getDivergence()){
                     dp.setDivergence(currDivergence);
                 }
             }
 //          System.out.println(dp.toString());
             writer.write(dp.toString()+"\n");
         }
         writer.flush();
 	}
 }
