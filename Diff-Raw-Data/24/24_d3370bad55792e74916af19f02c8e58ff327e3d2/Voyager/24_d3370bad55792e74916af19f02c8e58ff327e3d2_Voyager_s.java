 package main.nl.marktplaats.objects;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 
 import lemurproject.indri.ParsedDocument;
 import lemurproject.indri.QueryEnvironment;
 import lemurproject.indri.QueryRequest;
 import lemurproject.indri.QueryResult;
 import lemurproject.indri.QueryResults;
 import lemurproject.indri.ScoredExtentResult;
 import main.nl.marktplaats.utils.Configuration;
 import main.nl.marktplaats.utils.FileNegotiations;
 import main.nl.marktplaats.utils.SqlCommands;
 import main.nl.marktplaats.utils.Statistics;
 import main.nl.marktplaats.utils.StringManipulation;
 
 public class Voyager {
 		private List<String> queryTerms;
 		private Configuration configuration;
 		
 		public List<String> getQueryTerms()
 		{
 			return this.queryTerms;
 		}
 				
 		public Voyager(String query)
 		{
 			this.queryTerms = new ArrayList<String>();
 			for(String term: query.split("[\" \"|\t|\n]"))
 			{
 				if(stringContainsChar(term))
 				this.queryTerms.add(term);
 				
 				
 			}
 		}
 
 		public Voyager(Configuration c) {
 			this.configuration = c;
 		}
 
 		private boolean stringContainsChar(String term) {
 			char[] cs=term.toCharArray();
 			for(char c:cs)
 			{
 				if(Character.isLetter(c) || Character.isDigit(c))
 					return true;
 				
 			}
 			return false;
 		}
 
 		public String createQueryIndriQueryLanguage()
 		{
 			String newQuery = "#combine(";
 			List<String> remainingList = new ArrayList<String>();
 			remainingList.addAll(this.queryTerms);
 			for(String term:this.queryTerms)
 			{
 				remainingList.remove(term);
 				
 				for(String remainTerm:remainingList)
 				{
 					newQuery +="#band("+term +" "+remainTerm+") ";
 				}
 			}
 			if(this.queryTerms.size()<2)
 			{
 				return " ";
 			}
 			newQuery +=")";
 			System.out.println(newQuery);
 			return newQuery;
 			
 		}
 		public HashMap<String,Double> runQueryAndTakeResults(String query, HashMap<String,String> categoryNames,HashMap<String,String> attrNames ) throws Exception
 		{
 			//SqlCommands  sql = new SqlCommands();
 			HashMap<String,Double> docWithScores = new HashMap<String, Double>(); 
 			//List<Long> resultIds = new ArrayList<Long>();
 			QueryEnvironment env = new QueryEnvironment();
 			
 			for(int i=1; i<=5;i++)
 			{
 				env.addIndex("/home/varvara/workspace/externalSources/indri-5.3/repositoriesEntireDoc/out"+i);
 			}
 			ScoredExtentResult[] results = env.runQuery(query,200);
 			HashMap<String, Double> termsAndWeights = calculateWeights(env,262766 );
 			String[] ids = env.documentMetadata( results, "docno" );
 			for(String id:ids )
 			{
 				double docScore = 0.0;
 				for(Entry<String, Double> termAndWeight:termsAndWeights.entrySet())
 				{
 					if(containsTerm(id,  termAndWeight.getKey(),env,categoryNames, attrNames))
 						docScore+=termAndWeight.getValue();
 				}
 				docWithScores.put(id, docScore);
 			}
 					
 			return docWithScores;
 		}
 
 		private Long getDocNoByID(Integer id, QueryEnvironment env) {
 			ParsedDocument doc = new ParsedDocument();
 			return null;
 		}
 
 		private boolean containsTerm(String id, String key, QueryEnvironment env,HashMap<String, String> categoryNames,HashMap<String,String> attrNames ) throws Exception {
 			SqlCommands sql = new SqlCommands();
 			if(sql.checkIfhasValue("select * from ads where (title like '% "+key+"%' or description like '%"+key+"%' ) and id="+id+";","cas_ad_service"))
 				return true;
 			else 
 				{
 				//Category category = new Category();
 				//String catId = sql.selectCatQuery("select category_id from ads where id="+id+";");
 				//String CategoryName = sql.selectStringQuery("select parent_path,category_name from categories where category_id="+catId+";");
 				if(categoryNames.containsKey(id)){
 					if(categoryNames.get(id).contains(key))
 						return true;
 					else							
 						return attrNames.get(id).contains(key);
 				}
 				else {
 					if(attrNames.containsKey(id))
 					return attrNames.get(id).contains(key);
 					else 
 						return false;
 					}
 				
 						
 				}
 				
 			
 			/*ScoredExtentResult[] termsResults = env.runQuery(key, 1000);categories where category_id="+catId+"
 			List<Integer> docs = new ArrayList<Integer>();
 			for(ScoredExtentResult result:termsResults)
 			{
 				docs.add(result.document);
 			}
 			
 			if(docs.contains(id))
 				return true;
 			else return false;
 			*/
 		}
 		
 		public HashMap<String,String> getAttrNames(){
 			HashMap<String, String> attrNames = new HashMap<String,String>();
 			SqlCommands sql = new SqlCommands();
 			sql.selectHashMapQuery("select id,attr from vips;","150MarktVIP");
 			return attrNames;
 			
 		}
 		public HashMap<String,String> getCatNames(){
 			HashMap<String, String> catNames = new HashMap<String,String>();
 			SqlCommands sql = new SqlCommands();
 			sql.selectHashMapQuery("select id,category from vips;","150MarktVIP");
 			return catNames;
 			
 		}
 		private HashMap<String, Double> calculateWeights(QueryEnvironment env, int docNumber) throws Exception {
 			HashMap<String, Double> termsAndWeights =   new HashMap<String, Double>();
 			for(String term : this.queryTerms)
 			{
 				double termWeight = Math.log((double)(double)docNumber /(double)env.documentCount(term));
 				if(Math.abs(termWeight)<=4.5)
 				{
 					termsAndWeights.put(term, termWeight);
 				}
 			}
 			return termsAndWeights;
 		}
 		private HashMap<String, Double> getPredefinedWeights() throws Exception {
 			HashMap<String, Double> termsAndWeights =   new HashMap<String, Double>();
 			SqlCommands sql = new SqlCommands();
 			for(String term:this.queryTerms)
 			{
 				Double score = sql.selectDoubleQuery("select score from words where word like '%"+term+"%';","frequentKeywords");
 				termsAndWeights.put(term, score);		
 			}
 			return termsAndWeights;
 		}
 
 		private int[] getDocIds(String query, QueryEnvironment env) throws Exception {
 			QueryRequest queryRequest = new QueryRequest();
 			queryRequest.query = query;
 			queryRequest.resultsRequested = 1000;
 			queryRequest.options = QueryRequest.TextSnippet;
 			QueryResults qResults = env.runQuery(queryRequest);
 			QueryResult[] results = qResults.results;
 			int[] docIds = new int[results.length] ;
 			int i=0;
 			for (QueryResult queryResult : results) {
 				docIds[i] = queryResult.docid;
 				i++;
 			}
 			return docIds;
 		}
 
 
 		/*
 		public void setQueryTermsByList(ArrayList<String> terms)
 		{
 			this.queryTerms = new ArrayList<Term>();
 			this.queryTerms.addAll(terms);
 		}
 		
 		public void setQueryTermsByQuery(String query)
 		{
 			this.queryTerms = new ArrayList<Term>();
 			for(String term:query.split(" "))
 			{
 				Term t =new Term(term);
 				this.queryTerms.add(t);
 			}
 		}
 
 		public int calculateScore(Long doc, int i)
 		{
 			int score=0;
 			for(Term term:queryTerms)
 			{
 				score+=term.getWeight();
 			}
 			return score;
 		}
 	*/
 
 		public int calculateScore(Long doc, int size) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 		public void parseResults(){
 			FileNegotiations f = new FileNegotiations(); 
 			String file = configuration.getVoyagerResultsFolder();
 			System.out.println(file);
 			for (String query:f.getListFiles(file))
 			{	
 				parseVoyagerResults(file+"/"+query);
 			}
 		}
 		public void parseVoyagerResults(String file) {
 			try {
 				SqlCommands sql = new SqlCommands();
 				int count = 0; 
 				String insString="";
 				int flagInsert = 0;
 				for(String line:FileNegotiations.getTxtLinesAndReturnList(file))
 				{
 					if(line.startsWith("Nr:"))
 							flagInsert=0;
 					if(line.startsWith("Rk:")||flagInsert==1)
 					{
 						String intValue = line.replaceAll("[a-zA-Z]|:", ""); 
 						HashMap<String,String> docsWithScores = extractDocAndScoreFromString(intValue);
 						for(Entry<String,String> docsEntry : docsWithScores.entrySet())
 						{
 							String score = docsEntry.getValue().split("_")[1];
 							String seq = docsEntry.getValue().split("_")[0];
 							count++;
 							double ctr = sql.selectDoubleQuery("select ctr from ctrRpmScores where ad_id="+docsEntry.getKey()+";", configuration.getDb());
 							double rpm = sql.selectDoubleQuery("select rpm from ctrRpmScores where ad_id="+docsEntry.getKey()+";", configuration.getDb());
 							if(count<=10000)
 							{
								System.out.println("("+file.replaceAll("[a-zA-Z]|:|/", "")+","+docsEntry.getKey() +","+score+","+seq+",'',"+getCTR(docsEntry.getKey())+","+getRPM(docsEntry.getKey())+"),'"+rpm+"','"+ctr+"");
								insString+="("+file.replaceAll("[a-zA-Z]|:|/", "")+","+docsEntry.getKey() +","+score+","+seq+",'',"+getCTR(docsEntry.getKey())+","+getRPM(docsEntry.getKey())+"),'"+rpm+"','"+ctr+"";
 							}
 							else break;
 						}
 						
						sql.insertQuery("insert into "+configuration.getVoyagerResultsTable()+" values "+insString+"(0,0,0,0,'',0.0,0.0);", configuration.getDb());
 						insString  = "";
 					}
 				}
 			} catch (Exception e) {
 				System.err.println("Error: " + e.getMessage());
 			}
 			
 		}
 
 		private Double getRPM(String key) {
 			SqlCommands sql = new SqlCommands();
 			double score=0.0;
 			score = sql.selectDoubleQuery("select ctr from CTRScores where id="+key+";", configuration.getDb());
 			return score;
 		}
 
 		private Double getCTR(String key) {
 			SqlCommands sql = new SqlCommands();
 			double score=0.0;
 			score = sql.selectDoubleQuery("select rpm from RPMScores where id="+key+";", configuration.getDb());
 			return score;
 		}
 
 		private HashMap<String, String> extractDocAndScoreFromString(
 				String intValue) {
 			String[] results = intValue.split(" ");
 			int count = 1;
 			HashMap<String,String> returnedResults = new HashMap<String,String>();
 			for(String result : results)
 			{
 				String doc = result.split("/")[0];
 				String score = count+"_"+result.split("/")[1];
 				returnedResults.put(doc,score);
 				count++;
 			}
 			return returnedResults;
 		}
 
 		public void compineSystems() {
 			SqlCommands sql = new SqlCommands();
 			HashMap<Long,Double> lastRemaining = new HashMap<Long,Double>();
 			List<Integer> queries = sql.selectListInt("select distinct(query) from allVoyScores where (entireIndex>0 or titleIndex>0) and RPM>0;","150SearchKeywords");
 			for(int query:queries)
 			{
 				List<Long> previous = new ArrayList<Long>();
 				int seq = 0;
 				HashMap<Long,Double> entireDocsWithScores = sql.selectHashMapLongDoubbleQuery("select doc,entireIndex from allVoyScores where allKeywords=1 and entireIndex>0 and query="+query+" and RPM>0;","150SearchKeywords");
 				HashMap<Long,Double> titleDocsWithScores = sql.selectHashMapLongDoubbleQuery("select doc,titleIndex from allVoyScores where titleIndex>0 and query="+query+" and RPM>0 ;","150SearchKeywords");
 				HashMap<Long,Double> entireDocsWithScoresNotContainAllKeywords = sql.selectHashMapLongDoubbleQuery("select doc,entireIndex from allVoyScores where allKeywords=0 and entireIndex>0 and query="+query+" and RPM>0;","150SearchKeywords");
 				
 				for(Entry<Long,Double> docWithScore:entireDocsWithScores.entrySet())
 				{
 					if(titleDocsWithScores.containsKey(docWithScore.getKey()))
 					{
 						seq++;
 						
 						sql.insertQuery("insert into compTitleEntireAll values("+query+","+docWithScore.getKey()+","+docWithScore.getValue()+","+ seq+",1)", "150SearchKeywords");
 						previous.add(docWithScore.getKey());
 						titleDocsWithScores.remove(docWithScore.getKey());
 						lastRemaining.remove(docWithScore.getKey());
 						entireDocsWithScoresNotContainAllKeywords.remove(docWithScore.getKey());
 						
 					}
 					else 
 						lastRemaining.put(docWithScore.getKey(), docWithScore.getValue());
 				}
 				
 				for(Entry<Long,Double> remTitle:titleDocsWithScores.entrySet())
 				{
 					if(!previous.contains(remTitle.getKey()))
 					{
 						seq++;
 						sql.insertQuery("insert into compTitleEntireAll values("+query+","+remTitle.getKey()+","+remTitle.getValue()+","+ seq+",2)", "150SearchKeywords");
 						previous.add(remTitle.getKey());
 						lastRemaining.remove(remTitle.getKey());
 						entireDocsWithScoresNotContainAllKeywords.remove(remTitle.getKey());
 					}
 				}
 				
 				
 				for(Entry<Long,Double> rem:lastRemaining.entrySet())
 				{
 					if(!previous.contains(rem.getKey()))
 					{
 						seq++;
 						sql.insertQuery("insert into compTitleEntireAll values("+query+","+rem.getKey()+","+rem.getValue()+","+ seq+",3)", "150SearchKeywords");
 						previous.add(rem.getKey());
 						titleDocsWithScores.remove(rem.getKey());
 						entireDocsWithScoresNotContainAllKeywords.remove(rem.getKey());
 					}
 				}
 				for(Entry<Long,Double> remEntire:entireDocsWithScoresNotContainAllKeywords.entrySet())
 				{
 					if(!previous.contains(remEntire.getKey()))
 					{
 						seq++;
 						sql.insertQuery("insert into compTitleEntireAll values("+query+","+remEntire.getKey()+","+remEntire.getValue()+","+ seq+",4)", "150SearchKeywords");
 						previous.add(remEntire.getKey());
 					}
 				}
 			}
 		}
 		public void orderResultsBasedRPM()
 		{
 			SqlCommands sql = new SqlCommands();
 			List<Integer> queries = sql.selectListInt("select distinct(query) from allVoyScores where (entireIndex>0 or titleIndex>0) and RPM>0;","150SearchKeywords");
 			for(int query:queries)
 			{
 				int seq = 0;
 				List<Long> type1Docs = sql.selectListLong("select doc from compTitleEntireAll where query="+query+" and type=1 order by RPM desc;","150SearchKeywords");
 				List<Long> type3Docs = sql.selectListLong("select doc from compTitleEntireAll where query="+query+" and type=3 order by RPM desc;", "150SearchKeywords");
 				List<Long> type2Docs = sql.selectListLong("select doc from compTitleEntireAll where query="+query+" and type=2 order by RPM desc;", "150SearchKeywords");
 				List<Long> type4Docs = sql.selectListLong("select doc from compTitleEntireAll where query="+query+" and type=4 order by RPM desc;", "150SearchKeywords");
 				
 				for(Long doc:type1Docs )
 				{
 					seq++;
 					sql.insertQuery("insert into compTitleEntireOrderRPMAll values("+query+","+doc+","+ seq+",1)", "150SearchKeywords");	
 				}
 				for(Long doc:type2Docs )
 				{
 					seq++;
 					sql.insertQuery("insert into compTitleEntireOrderRPMAll values("+query+","+doc+","+ seq+",2)", "150SearchKeywords");
 				}
 				for(Long doc:type3Docs )
 				{
 					seq++;
 					sql.insertQuery("insert into compTitleEntireOrderRPMAll values("+query+","+doc+","+ seq+",3)", "150SearchKeywords");
 				}
 				for(Long doc:type4Docs )
 				{
 					seq++;
 					sql.insertQuery("insert into compTitleEntireOrderRPMAll values("+query+","+doc+","+ seq+",4)", "150SearchKeywords");
 				}
 			}
 			
 		}
 
 		public void addWeight(String db, String table, String system) {
 			SqlCommands sql = new SqlCommands();
 			HashMap<Long,String> vips = sql.selectHashMapLongStringQuery("select distinct(vip), query from "+table+" where system like '"+system+"';", db);
 			for(Entry<Long,String> vip:vips.entrySet())
 			{
 				String newQuery = vip.getValue();
 				int countTerms = vip.getValue().split(",").length;
 				for(int i=0; i<countTerms;i++)
 				{
 					newQuery+=",1";
 				}
 				sql.insertQuery("insert into newVIPQueries values ("+vip.getKey()+",'"+newQuery+"','"+system+"');", db);
 			}
 		}
 
 		public void createQueryVoyagerQueryLanguage() {
 			SqlCommands sql = new SqlCommands();
 			StringManipulation stringMan = new StringManipulation();
 			System.out.println("Reading queries of table "+configuration.getReadTable());
 			for(Entry<Long,String> queryEntry:sql.selectHashMapLongStringQuery("select doc,query from "+configuration.getReadTable()+";", configuration.getDb()).entrySet())
 			{
 				String newQuery = stringMan.getStringSeparatedByCommas(queryEntry.getValue());
 				String request = getVoyagerRequest(newQuery);
 				sql.insertQuery("insert into "+configuration.getVoyagerQueriesTable()+" values("+queryEntry.getKey()+",'"+request+"','"+configuration.getIndexField()+"');", configuration.getDb());
 			}
 			System.out.println("Queries requests saved on "+configuration.getVoyagerQueriesTable());}
 
 		private String getVoyagerRequest(String newQuery) {
 			String request = "";
 			IndexedField index = configuration.getIndexField();
 			switch (index) {
 			case Title:
 				request+=configuration.getVoyagerRequest()+newQuery+configuration.getPostFixVoyagerRequest();	
 				break;
 			case Description:
 				request+=configuration.getVoyagerRequest()+"Contains(Description_i,OR("+newQuery+")"+configuration.getPostFixVoyagerRequest();	
 				break;
 			case Entire:
 				request+=configuration.getVoyagerRequest()+"Rank("+newQuery+")"+configuration.getPostFixVoyagerRequest();	
 			default:
 				System.out.println("Check your IndexedField choice !!!!");
 				break;
 			}
 			return request; 
 			
 		}
 
 		public void runVoyagerQueries() throws IOException {
 			Process process = new ProcessBuilder("/media/Data/Coen/scripts/voyagerCallsFromEclipse.sh").start();
 			InputStream is = process.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			while ((br.readLine()) != null) {
 				//System.out.println(line);
 			}
 	
 		}
 		
 		public void saveVoyagerResultsToTable() {
 			System.out.println("Saving Results....");
 			parseResults();	
 		}
 
 		public void createInputFiles() {
 			FileNegotiations fileNego = new FileNegotiations();
 			fileNego.createInputForTrec(configuration.getTrecInputFolder(), configuration.getDb(), "", configuration.getVoyagerResultsTable());
 		}
 
 		public void getMetrics() {
 			// TODO Auto-generated method stub
 		}
 
 		public void gatherStatistics() {
 			boolean a = configuration.checkIfHasStringValue("Statistics table : ",configuration.getStatisticsTable());
 			if(a)
 			{
 				Statistics stat = new Statistics(configuration);
 				stat.gatherStatistics();
 			}
 			
 		}
 }
