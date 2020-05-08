 package retrieWin.PatternBuilder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import retrieWin.Indexer.ProcessTrecTextDocument;
 import retrieWin.Indexer.TrecTextDocument;
 import retrieWin.Querying.ExecuteQuery;
 import retrieWin.Querying.QueryBuilder;
 import retrieWin.SSF.Constants;
 import retrieWin.SSF.Entity;
 import retrieWin.SSF.SlotPattern;
 import retrieWin.SSF.Constants.EntityType;
 import retrieWin.Utils.FileUtils;
 import retrieWin.Utils.NLPUtils;
 import retrieWin.Utils.Utils;
 
 import edu.stanford.nlp.stats.IntCounter;
 import edu.stanford.nlp.util.Pair;
 import fig.basic.LogInfo;
 import fig.basic.Option;
 import fig.exec.Execution;
 
 public class Aju implements Runnable{
 	@Option(gloss="working Directory") public String workingDirectory;
 	@Option(gloss="index Location") public String indexLocation;
 	List<Entity> entities;
 	public static void main(String[] args) {
 		Execution.run(args, "Main", new Aju());
 	}
 	
 	public Aju(){
 		readEntities();
 	}
 
 	@Override
 	public void run() {
 		LogInfo.begin_track("run()");
 		//runBootstrap();
 		runBootStrapforEntityAndNER();
 	
 		LogInfo.end_track();
 	}
 	
 	public void runBootStrapforEntityAndNER() {
 		ExecuteQuery eq = new ExecuteQuery(indexLocation);
		NLPUtils.extractPERRelation("The time has come to reassess to impact of former Presiding Justices Aharon Barak and Dorit Beinisch on Human Rights, the justice system, and the rule of law in the State of Israel.");
 		/*for(Entity e:entities) {
 			if(e.getEntityType()==EntityType.PER) {
 				String query = QueryBuilder.buildOrQuery(e.getExpansions());
 	            //System.out.println("Querying for: " + query); =
 				List<String> folders = new ArrayList<String>();
 				List<String> queries = new ArrayList<String>();
 				folders.add("2012-05-05-05");
 				queries.add(query);
 				Set<TrecTextDocument> trecDocs = QueryFactory.DoQuery(folders, queries, workingDirectory, entities);
 				if(trecDocs.size() > 0) {
 					for(String expansion:e.getExpansions()) {
 						List<TrecTextDocument> list = new ArrayList<TrecTextDocument>();
 						list.addAll(trecDocs);
 						List<String> sents = ProcessTrecTextDocument.extractRelevantSentences(list, expansion);
 						for(String sent:sents) {
 							System.out.println(sent);
 						}
 					}
 				}
 			}
 		}
 		*/
 		
 	}
 		
 	public void runBootstrapForPair() {
 		List<Pair<String,String>> bootstrapList = getBootstrapInput("src/seedSet/slot_Founded_by");
 		NLPUtils utils = new NLPUtils();
 
 		HashMap<SlotPattern, Double> weights = new HashMap<SlotPattern,Double>();
 		for(Pair<String, String> pair:bootstrapList) {
 			ExecuteQuery eq = new ExecuteQuery(indexLocation);
 			List<TrecTextDocument> trecDocs = eq.executeQuery(QueryBuilder.buildUnorderedQuery(pair.first, pair.second, 10), 1000, workingDirectory);
 			IntCounter<SlotPattern> individualWeights = new IntCounter<SlotPattern>();
 			for(String str:ProcessTrecTextDocument.extractRelevantSentences(trecDocs, pair.first, pair.second)) {
 				LogInfo.logs(str);
 				List<SlotPattern> patterns = utils.findSlotPattern(str, pair.first, pair.second);
 				for(SlotPattern pattern:patterns) {
 					individualWeights.incrementCount(pattern);
 				}
 			}
 			
 			double total = individualWeights.totalCount();
 			LogInfo.logs("\n\nIndividual counts for " + pair);
 			for(SlotPattern pattern:individualWeights.keySet()) {
 				LogInfo.logs(pattern + ":" + individualWeights.getCount(pattern));
 				if(!weights.containsKey(pattern)) {
 					weights.put(pattern, 0.0);
 				}
 				weights.put(pattern, weights.get(pattern) + individualWeights.getCount(pattern) / total);
 			}
 		}
 		
 		LogInfo.logs("\n\nDone finding patterns\n");
 		for(SlotPattern pattern:weights.keySet()) {
 			LogInfo.logs(pattern + ":" + weights.get(pattern));
 		}
 	}
 	
 	private static List<Pair<String, String>> getBootstrapInput(String fileName) {
 		List<Pair<String, String>> bootstrapList = new ArrayList<Pair<String, String>>();
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(fileName));
 			String line = null;
 			try {
 				while((line=reader.readLine())!=null) {
 					String[] splits = line.split("\t");
 					bootstrapList.add(new Pair<String, String>(splits[0], splits[1]));
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return bootstrapList;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void readEntities() {
 		File file = new File(Constants.entitiesSerilizedFile);
 		if(file.exists()) {
 			entities = (List<Entity>)FileUtils.readFile(file.getAbsolutePath().toString());
 		}
 	}
 }
