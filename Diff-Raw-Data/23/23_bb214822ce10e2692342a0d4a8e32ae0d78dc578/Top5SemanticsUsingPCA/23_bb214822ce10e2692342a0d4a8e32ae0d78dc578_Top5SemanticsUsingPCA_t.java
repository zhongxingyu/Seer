 package edu.mwdb.project;
 
 import java.io.StringReader;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import matlabcontrol.MatlabProxy;
 import matlabcontrol.MatlabProxyFactory;
 import matlabcontrol.extensions.MatlabNumericArray;
 import matlabcontrol.extensions.MatlabTypeConverter;
 
 import org.apache.lucene.analysis.CharArraySet;
 import org.apache.lucene.analysis.StopFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 public class Top5SemanticsUsingPCA {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		Utility utilityObj = new Utility();
 		try 
 		{
 			Connection con = utilityObj.getDBConnection();
 			// List to store the author abstracts
 			List<String> abstracts = new ArrayList<String>();
 
 			Statement stmt = con.createStatement();
 			// use 1632672 instead of args[0]
 			//String personId = args[0];
 
 			String personId = "1632672";
 
 			// To get the list of papers written by the given author.
 			String query_authorid = 
 					"select p.abstract from papers p join " +  
 							"(select distinct w.paperid from " + 
 							"authors a join writtenby w where a.personid = w.personid and a.personid = " + personId  + 
 							" order by paperid) T1 on p.paperid = T1.paperid where p.abstract != \"\"";
 
 			ResultSet rs = stmt.executeQuery(query_authorid);
 			while (rs.next())
 			{
 				abstracts.add(rs.getString("abstract"));
 			}
 
 			// To know the count of abstracts in the DB and store it in noOfDocs.
 			Statement stmt1 = con.createStatement();
 			int noOfDocs = 0;
 			String query_alldocs_count = "SELECT count(*) AS count FROM papers WHERE abstract != \"\"";
 			ResultSet rs1 = stmt1.executeQuery(query_alldocs_count);
 			while (rs1.next()) 
 			{
 				noOfDocs = rs1.getInt("count");
 			}
 
 			//Extract the abstracts from the DB
 			Statement stmt2 = con.createStatement();
 			String query_alldocs = "SELECT abstract FROM papers WHERE abstract != \"\"";
 			ResultSet rs2 = stmt2.executeQuery(query_alldocs);
 
 			// Creation of a Index Directory.
 			StandardAnalyzer docAnalyzer = new StandardAnalyzer(Version.LUCENE_36);
 			IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_36,docAnalyzer);
 			Directory indexDirectory = new RAMDirectory();
 			IndexWriter indexWr = new IndexWriter(indexDirectory, indexConfig);
 
 			String doc="";
 
 			// Adding a field 'doc' from the abstract to create an indexed document.
 			while (rs2.next()) 
 			{
 				doc = rs2.getString("abstract");
 				Document document = new Document();
 				document.add(new Field("doc", doc, Field.Store.YES,Field.Index.ANALYZED));
 				indexWr.addDocument(document);
 				indexWr.commit();
 			}
 
 			CharArraySet stopWordsCharArrSet;
 			TokenStream docStream;
 			TokenStream keywords;
 			int noOfWords = 0;
 
 			Map<String,Float> termFreq = new HashMap<String, Float>();
 			KeywordConfig config;		
 			List<KeywordConfig> configList = new ArrayList<KeywordConfig>();
 
 			// List of Lists - where each list stores the keywords of the respective documents.
 			List<List<String>> docKeywords = new ArrayList<List<String>>();
 
 			// List of Maps - where each list stores the IDF map of the respective documents.
 			List<Map<String,Float>> docIdfMapList = new ArrayList<Map<String,Float>>();
 			stopWordsCharArrSet = new CharArraySet(Version.LUCENE_36, utilityObj.createStopWordsSet(), true);
 
 			for (int i=0;i<abstracts.size();i++)
 			{
 				String[] rowDataArr = abstracts.get(i).split("[ ]+");
 				noOfWords += rowDataArr.length;
 
 				//Creating the Character Array Set from the list of stop words
 
 				//Creating a token stream from the abstract got from the DB for the given paperId
 				docStream = new StandardTokenizer(Version.LUCENE_36, new StringReader(abstracts.get(i)));
 
 				//Creating the Keywords of a given abstract
 				keywords = new StopFilter(Version.LUCENE_36, docStream ,stopWordsCharArrSet);
 
 				termFreq = utilityObj.createNewTF(keywords, abstracts.get(i));
 
 				List<String> keywordsList = new ArrayList<String>();
 				for(Map.Entry<String, Float> k : termFreq.entrySet())
 				{
 					keywordsList.add(k.getKey());
 				}
 				docKeywords.add(keywordsList);
 
				Map<String,Float> idfMap = utilityObj.createTFIDF(noOfDocs,indexDirectory, termFreq, "TF-IDF");
 				docIdfMapList.add(idfMap);
 
 				for(Map.Entry<String, Float> keys : termFreq.entrySet())
 				{
 					config = new KeywordConfig();
 					config.setKeyword(keys.getKey());
 					config.setWeightedFreq(keys.getValue());
 					configList.add(config);
 				}
 			}
 
 			Map<String,Float> termFinalFreq = new HashMap<String, Float>();
 			for (KeywordConfig itr: configList){
 				Float val = termFinalFreq.get(itr.getKeyword());
 				termFinalFreq.put(itr.getKeyword(), (val == null) ? itr.getWeightedFreq() : (val + itr.getWeightedFreq()));
 			}		
 			for(Map.Entry<String, Float> k: termFinalFreq.entrySet())
 			{
 				termFinalFreq.put(k.getKey(), k.getValue()/noOfWords);
 			}
 
 			int rowSize = abstracts.size();
 			int columnSize = termFinalFreq.size();
 
 			double docKeywordCorpusMatrix[][] = new double[rowSize][columnSize];
 
 			// Build the input Corpus matrix.
 			for(int row=0;row<rowSize;row++)
 			{
 				List<String> tempList = docKeywords.get(row);
 				for(int column=0;column<columnSize;column++)
 				{
 					for(Map.Entry<String, Float> k: termFinalFreq.entrySet())
 					{
 						for(int i=0;i<tempList.size();i++)
 						{
 							if(k.getKey().equals(tempList.get(i)))
 							{
 								docKeywordCorpusMatrix[row][column] = docIdfMapList.get(row).get(k.getKey());
 								break;
 							}
 							docKeywordCorpusMatrix[row][column] = 0;
 						}
 						if (column<columnSize-1)
 							column++;
 					}
 				}
 			}
 
 			// Print the i/p Corpus matrix.
 			/*for(int row=0;row<rowSize;row++)
 			{
 				for(int column=0;column<columnSize;column++)
 				{
 					System.out.print(docKeywordCorpusMatrix[row][column] + "\t");
 				}
 				System.out.println();
 			}*/
 
 			double[][] docKeywordCoeffMatrix = new double[columnSize][columnSize];
 
 			// Connecting to the Matlab
 			MatlabProxyFactory factory = new MatlabProxyFactory();
 			MatlabProxy proxy = factory.getProxy();
 
 			MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
 			processor.setNumericArray("docKeywordCorpusMatrix", new MatlabNumericArray(docKeywordCorpusMatrix, null));
 
 			// For PCA:
 			proxy.eval("[pc,score]=princomp(docKeywordCorpusMatrix);");
 			double[][] tempMatrix = new double[columnSize][columnSize];
 
 			for(int k=0;k<columnSize;k++)
 			{
 				Object[] obj=proxy.returningEval("pc(:,"+ (k+1) +")" ,1);
 				tempMatrix[k]=(double[]) obj[0];
 			}
 
 			double[][] resultSematicMatrixPCA = new double[columnSize][5];
 			for(int a=0;a<columnSize;a++)
 			{
 				for(int b=0;b<5;b++)
 				{
 					resultSematicMatrixPCA[a][b]= tempMatrix[b][a];
 				}
 			}
 			
 			// Print the Top5 Latent/Topic Matrix
 			/*for(int i=0;i<columnSize;i++)
 			{
 				for(int j=0;j<5;j++)
 				{
 					System.out.print(resultSematicMatrixPCA[i][j] + "\t");
 				}
 				System.out.println();
 			}*/
 			
 			// For SVD:
 			proxy.eval("[U,S,V]=svd(docKeywordCorpusMatrix);");
 			for(int k=0;k<columnSize;k++)
 			{
 				Object[] obj=proxy.returningEval("V(:,"+ (k+1) +")" ,1);
 				tempMatrix[k]=(double[])obj[0];
 			}
 			
 			System.out.println("***********Printing the Latent Semantics using SVD***********");
 			double[][] resultSematicMatrixSVD = new double[5][columnSize];
 			for(int a=0;a<5;a++)
 			{
 				for(int b=0;b<columnSize;b++)
 				{
 					resultSematicMatrixSVD[a][b]= tempMatrix[b][a];
 				}
 			}
 			
 			for(int i=0;i<5;i++)
 			{
 				for(int j=0;j<columnSize;j++)
 				{
 					System.out.print(resultSematicMatrixSVD[i][j] + "\t");
 				}
 				System.out.println();
 			}
 
 			proxy.disconnect();
 
 			con.close();
 		}
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 
 	}
 }
