 package retrieWin.PatternBuilder;
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import java.util.ArrayList;
 
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import retrieWin.Indexer.ProcessTrecTextDocument;
 import retrieWin.Indexer.TrecTextDocument;
 
 
 import retrieWin.Querying.ExecuteQuery;
 import retrieWin.Querying.QueryBuilder;
 import retrieWin.SSF.Constants;
 import retrieWin.SSF.Entity;
 import retrieWin.SSF.SlotPattern;
 import retrieWin.SSF.SlotPattern.Rule;
 import retrieWin.Utils.NLPUtils;
 import retrieWin.Utils.PriorityQueue;
 import retrieWin.Utils.Utils;
 import retrieWin.SSF.Constants.EdgeDirection;
 import retrieWin.SSF.Constants.EntityType;
 import retrieWin.Utils.FileUtils;
 
 import edu.stanford.nlp.stats.IntCounter;
 import edu.stanford.nlp.util.Pair;
 import fig.basic.LogInfo;
 import fig.basic.Option;
 import fig.exec.Execution;
 
 public class ContactMeetPlaceTime implements Runnable{
 	@Option(gloss="working Directory") public String workingDirectory;
 	@Option(gloss="Index Location") public String indexLocation;
 	@Option(gloss="output File") public String outputFile;
 	List<Entity> entities;
 	public static void main(String[] args) {
 		Execution.run(args, "Main", new ContactMeetPlaceTime());
 	}
 	
 	public ContactMeetPlaceTime(){
 		readEntities();
 	}
 
 	@Override
 	public void run() {
 		LogInfo.begin_track("run()");
 		//runBootstrap();
 		if (!workingDirectory.endsWith("/"))
 			workingDirectory = workingDirectory + "/";
 		//gatherResults();
 		List<String> filename_nonSchools = new ArrayList<String>();
 		List<String> filenames = new ArrayList<String>();
 		
 		filenames.add("output_20120721");
 		filenames.add("outputDay2_20120722");
 		
 		
 		filename_nonSchools.add("output_nonSchools");
 		filename_nonSchools.add("output_nonSchools_20120721");
 		doBootStrap();
 		//aggregateResults(filenames,filename_nonSchools);
 		//writeHumanReadableResults(filenames);
 		//aggregateBootstrapResults("bootstrapResults");
 		LogInfo.end_track();
 	}
 	
 	public void aggregateBootstrapResults(String filename)
 	{
 		Map<Pair<String,String>,Set<String>> allPairs = new HashMap<Pair<String,String>,Set<String>>(); 
 		try{
 		BufferedReader buf = new BufferedReader(new FileReader(filename));
 		String line;
 		Pair<String,String> newPair = new Pair<String,String>();
 		while((line = buf.readLine())!=null)
 		{
 			if (!line.startsWith("$"))
 			{
 				String[] tokens = line.split(":");
 				newPair = new Pair<String,String>(tokens[0],tokens[1]);
 				allPairs.put(newPair, new HashSet<String>());
 			}
 			else
 			{
 				String sentence = line.substring(1);
 				Set<String> existing = allPairs.get(newPair);
 				existing.add(sentence);
 				allPairs.put(newPair, existing);
 			}
 		}
 		
 		buf.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		PriorityQueue<Pair<String,String>> pq = new PriorityQueue<Pair<String,String>>();
 		
 		for (Pair<String,String> pr : allPairs.keySet())
 		{
 			pq.add(pr, allPairs.get(pr).size());
 		}
 		
 		try
 		{
 			BufferedWriter wbuf = new BufferedWriter(new FileWriter(outputFile));
 			while(!pq.isEmpty())
 			{
 				Pair<String,String> p = pq.next();
 				String output = String.format("%s:%s",p.first,p.second);
 				wbuf.write(output);
 				wbuf.newLine();
 			}
 			wbuf.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public List<SlotPattern> populateBootstrapSlotPatterns()
 	{
 		List<SlotPattern> result = new ArrayList<SlotPattern>();
 		Rule r1 = new Rule();
 		r1.edgeType = "nsubj";
 		r1.direction = EdgeDirection.Out;
 		Rule r2 = new Rule();
 		r2.edgeType = "prep_at";
 		r2.direction = EdgeDirection.Out;
 		List<Rule> rules = new ArrayList<Rule>();
 		rules.add(r1);	rules.add(r2);
 		
 		// stay, nsubj, prep-at
 		SlotPattern p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setPattern("stay");
 		p.setRules(rules);
 		result.add(p);
 		
 		// arrive, nsubj, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("arrive");
 		result.add(p);
 		
 		// play, nsubj, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("play");
 		result.add(p);
 		
 		// present, nsubj, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("present");
 		result.add(p);
 		
 		// gather, nsubj, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("gather");
 		result.add(p);
 		
 		// gave
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("give");
 		result.add(p);
 		
 		// spoke
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("speak");
 		result.add(p);
 		
 		
 		r1 = new Rule();
 		r1.edgeType = "nsubjpass";
 		r1.direction = EdgeDirection.Out;
 		r2 = new Rule();
 		r2.edgeType = "prep_at";
 		r2.direction = EdgeDirection.Out;
 		rules = new ArrayList<Rule>();
 		rules.add(r1);	rules.add(r2);
 		// held, nsubjpass, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("hold");
 		result.add(p);
 		
 		// award, nsubjpass, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("award");
 		result.add(p);
 		
 		// present, nsubjpass, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("present");
 		result.add(p);
 		
 		r1 = new Rule();
 		r1.edgeType = "poss";
 		r1.direction = EdgeDirection.Out;
 		r2 = new Rule();
 		r2.edgeType = "prep_at";
 		r2.direction = EdgeDirection.Out;
 		rules = new ArrayList<Rule>();
 		rules.add(r1);	rules.add(r2);
 		// held, nsubjpass, prep_at
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("speech");
 		result.add(p);
 		
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("talk");
 		result.add(p);
 		
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("presentation");
 		result.add(p);
 		
 		p = new SlotPattern();
 		p.setConfidenceScore(0.0);
 		p.setRules(rules);
 		p.setPattern("appearance");
 		result.add(p);
 		
 		return result;
 	}
 	public void doBootStrap()
 	{
 		List<SlotPattern> patterns = populateBootstrapSlotPatterns();
 		Map<Pair<String,String>,Set<String>> results = new HashMap<Pair<String,String>,Set<String>>();
 		NLPUtils nlp = new NLPUtils();
 		ExecuteQuery eq = new ExecuteQuery(indexLocation);
 		int numResults = 100;
 		Map<SlotPattern,List<String>> patternToStringMap = new HashMap<SlotPattern,List<String>>();
 		for (SlotPattern p:patterns)
 		{
 			System.out.println("Starting query for : " + p.getPattern());
 			String query = String.format("#10(%s at)", p.getPattern());
 			System.out.println("Query String: " + query);
 			List<TrecTextDocument> trecDocs = eq.executeQuery(query,numResults, workingDirectory);
 			List<String> sentences = ProcessTrecTextDocument.getCleanedSentences(
 					ProcessTrecTextDocument.extractRelevantSentences(trecDocs, p.getPattern()));
 			patternToStringMap.put(p, sentences);
 			System.out.println("Done querying for: " + p.getPattern());
 		}
 		
 		//NLPUtils nlp = new NLPUtils();
 		
 		for (SlotPattern p:patternToStringMap.keySet())
 		{
 			int numthreads = 16;
 			ExecutorService exc = Executors.newFixedThreadPool(numthreads);
 			
 			List<Future<Map<Pair<String,String>,List<String>>>> futuresList = new ArrayList<Future<Map<Pair<String,String>,List<String>>>>();
 			
 			
 			for (String sentence:patternToStringMap.get(p))
 			{
 				Callable<Map<Pair<String,String>,List<String>>> c = new parallelBootstrapper(sentence,p,nlp);
 				Future<Map<Pair<String,String>,List<String>>> s = exc.submit(c);
 				futuresList.add(s);
 			}
 		
 			for (Future<Map<Pair<String,String>,List<String>>> f:futuresList)
 			{
 				Map<Pair<String,String>,List<String>> thisResult = new HashMap<Pair<String,String>,List<String>>();
 				try
 				{
 					thisResult = f.get();
 				}
 				catch (Exception excep)
 				{
 					excep.printStackTrace();
 				}
 				for (Pair<String,String> pr:thisResult.keySet())
 				{
 					if (results.containsKey(pr))
 					{
 						Set<String> existing = results.get(pr);
 						existing.addAll(thisResult.get(pr));
 						results.put(pr,existing);
 					}
 					else
 					{
 						results.put(pr, new HashSet<String>(thisResult.get(pr)));
 					}
 				}
 			}
 			exc.shutdown();
 		}
 		
 		PriorityQueue<Pair<String,String>> resultsQueue = new PriorityQueue<Pair<String,String>>();
 		for (Pair<String,String> pr:results.keySet())
 		{
 			resultsQueue.add(pr,(double)(results.get(pr).size()));
 		}
 		
 		try
 		{
 			BufferedWriter buf = new BufferedWriter(new FileWriter(outputFile));
 			while(!resultsQueue.isEmpty())
 			{
 				Pair<String,String> pr = resultsQueue.next();
 				Set<String> sentences = results.get(pr);
 				String pairLine = String.format("%s:%s:%d", pr.first,pr.second,sentences.size());
 				buf.write(pairLine);
 				buf.newLine();
 				for (String s:sentences)
 				{
 					String sentenceLine = String.format("$%s", s);
 					buf.write(sentenceLine);
 					buf.newLine();
 				}
 				buf.newLine();
 			}
 			
 			buf.close();
 		}
 		catch (Exception e)
 		{
 			System.out.println("Failed writing to file");
 			e.printStackTrace();
 		}
 	}
 	
 private static class parallelBootstrapper implements Callable<Map<Pair<String,String>,List<String>>>{
 		
 		String sentence;
 		SlotPattern pattern;
 		
 		NLPUtils nlp;
 		public parallelBootstrapper(String sen, SlotPattern pin, NLPUtils nlpin)
 		{
 			nlp = nlpin;
 			sentence = sen;
 			pattern = pin;
 		}
 		
 		@Override
 		public Map<Pair<String,String>,List<String>> call() throws Exception {
 
 			return nlp.getTwoSidesForPatternWord(sentence, pattern);
 		}
 	}
 
 
 	
 	public void aggregateResults(List<String> filenames, List<String> nonSchoolFilenames)
 	{
 		Map<SlotPattern,Set<String>> allPatterns = new HashMap<SlotPattern,Set<String>>();
 		for (String filename:filenames)
 		{
 			@SuppressWarnings("unchecked")
 			Map<SlotPattern,Set<String>> currentPattern = (Map<SlotPattern,Set<String>>)FileUtils.readFile(filename);
 			for (SlotPattern p:currentPattern.keySet())
 			{
 				if (allPatterns.containsKey(p))
 				{
 					Set<String> existing = allPatterns.get(p);
 					existing.addAll(currentPattern.get(p));
 					allPatterns.put(p, existing);
 				}
 				else
 				{
 					allPatterns.put(p, currentPattern.get(p));
 				}
 			}
 		}
 		Map<SlotPattern,Set<String>> allPatternsNonSchools = new HashMap<SlotPattern,Set<String>>();
 		for (String filename:nonSchoolFilenames)
 		{
 			@SuppressWarnings("unchecked")
 			Map<SlotPattern,Set<String>> currentPattern = (Map<SlotPattern,Set<String>>)FileUtils.readFile(filename);
 			for (SlotPattern p:currentPattern.keySet())
 			{
 				if (allPatternsNonSchools.containsKey(p))
 				{
 					Set<String> existing = allPatternsNonSchools.get(p);
 					existing.addAll(currentPattern.get(p));
 					allPatternsNonSchools.put(p, existing);
 				}
 				else
 				{
 					allPatternsNonSchools.put(p, currentPattern.get(p));
 				}
 			}
 		}
 		double totalCount = 0;
 		Map<SlotPattern,Double> patternCounts = new HashMap<SlotPattern,Double>();
 		for (SlotPattern pattern:allPatterns.keySet())
 		{
 			totalCount += allPatterns.get(pattern).size();
 			if (patternCounts.containsKey(pattern))
 			{
 				patternCounts.put(pattern, patternCounts.get(pattern)+allPatterns.get(pattern).size());
 			}
 			else
 			{
 				patternCounts.put(pattern, (double)allPatterns.get(pattern).size());
 			}
 		}
 		
 		// Add 1.5 times the count of non schools
 		for (SlotPattern pattern:allPatternsNonSchools.keySet())
 		{
 			totalCount += 1.5*allPatternsNonSchools.get(pattern).size();
 			if (patternCounts.containsKey(pattern))
 			{
 				patternCounts.put(pattern, patternCounts.get(pattern)+(1.5*allPatternsNonSchools.get(pattern).size()));
 			}
 			else
 			{
 				patternCounts.put(pattern, 1.5*allPatternsNonSchools.get(pattern).size());
 			}
 		}
 		Map<SlotPattern,Double> patternWeights = new HashMap<SlotPattern,Double>();
 		PriorityQueue<SlotPattern> pq = new PriorityQueue<SlotPattern>();
 		for (SlotPattern p: patternCounts.keySet())
 		{
 			double confidence = patternCounts.get(p)/totalCount;
 			patternWeights.put(p,confidence);
 			pq.add(p,confidence);
 		}
 		try
 		{
 			BufferedWriter buf = new BufferedWriter(new FileWriter(outputFile));
 			while(!pq.isEmpty())
 			{
 				SlotPattern p = pq.next();
 				double confidence = patternWeights.get(p);
 				p.setConfidenceScore(confidence);
 				buf.write(p.toString());
 				buf.newLine();
 			}
 			buf.flush();
 			buf.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	public void writeHumanReadableResults(List<String> filenames)
 	{
 		Map<SlotPattern,Set<String>> allPatterns = new HashMap<SlotPattern,Set<String>>();
 		for (String filename:filenames)
 		{
 			@SuppressWarnings("unchecked")
 			Map<SlotPattern,Set<String>> currentPattern = (Map<SlotPattern,Set<String>>)FileUtils.readFile(filename);
 			for (SlotPattern p:currentPattern.keySet())
 			{
 				if (allPatterns.containsKey(p))
 				{
 					Set<String> existing = allPatterns.get(p);
 					existing.addAll(currentPattern.get(p));
 					allPatterns.put(p, existing);
 				}
 				else
 				{
 					allPatterns.put(p, currentPattern.get(p));
 				}
 			}
 		}
 		try
 		{
 			BufferedWriter buf = new BufferedWriter(new FileWriter(outputFile));
 			for (SlotPattern p:allPatterns.keySet())
 			{
 				buf.write("pattern$" + p.getPattern()+"\n");
 				List<Rule> rules = p.getRules();
 				buf.write("rule1$" + rules.get(0).edgeType + "$" + rules.get(0).direction + "\n");
 				buf.write("rule2$" + rules.get(1).edgeType + "$" + rules.get(1).direction + "\n");
 				Set<String> sentences = allPatterns.get(p);
 				for (String sentence:sentences)
 				{
 					buf.write(sentence + "\n");
 				}
 				buf.write("\n");
 			}
 			buf.flush();
 			buf.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void gatherResults() {
 		
 		
 		NLPUtils nlp = new NLPUtils();
 		List<String> relations = new ArrayList<String>();
 		IntCounter<SlotPattern> patternWeights = new IntCounter<SlotPattern>();
 		Map<SlotPattern,Set<String>> allPatterns = new HashMap<SlotPattern,Set<String>>();
 		relations.add("prep_at");
 		//relations.add("prep_in");
 		List<String> folders = new ArrayList<String>();
 		
 		for (int j = 23;j<24;j++)
 		{
 		for (int i = 0;i<24;i++)
 		{
 			String folderName = String.format("2012-07-%02d-%02d", j,i);
 			folders.add(folderName);
 		}
 		}
 		
 		//folders.add("2012-07-25-01");
 		Map<Entity,String> entityToQueries = new HashMap<Entity,String>();
 		List<String> queries = new ArrayList<String>();
 		
 		for(Entity e:entities) {
 			if (e.getEntityType()!=EntityType.PER)
 				continue;
 			String query = QueryBuilder.buildOrQuery(e.getExpansions());
 			entityToQueries.put(e, query);
 			queries.add(query);
 		}
 			
 		Map<String,List<TrecTextDocument>> AllTrecDocs = QueryFactory.DoQuery(folders, queries, workingDirectory, entities);
 		/*
 		for (String q:AllTrecDocs.keySet())
 		{
 			System.out.println("query: " + q + " Number of results is: " + AllTrecDocs.get(q).size());
 		}
 		*/
 		int numthreads = 16;
 		ExecutorService exc = Executors.newFixedThreadPool(numthreads);
 		for (Entity e:entities)
 		{
 			exc.execute(new ParallelNLPParsers(e, AllTrecDocs,nlp,relations, allPatterns));
 		}
 		exc.shutdown();
 		while(true)
 		{
 			try 
 			{
 				if (exc.awaitTermination(1, TimeUnit.MINUTES))
 						break;
 				System.out.println("Waiting in NLPParser");
 			}
 			catch(InterruptedException ie)
 			{
 				ie.printStackTrace();
 				System.out.println("Waiting in NLPParser - Thread interrupted");
 			}
 		}
 		
 		for (SlotPattern pattern:allPatterns.keySet())
 		{
 			patternWeights.incrementCount(pattern,allPatterns.get(pattern).size());
 		}
 		
 		for (SlotPattern pattern:patternWeights.keySet())
 		{
 			System.out.println(pattern.toString());
 			System.out.println("Sentences: ");
 			for (String s:allPatterns.get(pattern))
 			{
 				System.out.println(s);
 			}
 			System.out.println("Count: ");
 			System.out.println(patternWeights.getCountAsString(pattern));
 		}
 		
 		
 		FileUtils.writeFile(allPatterns, outputFile);
 		try{
 			
 			String s3directory = "s3://contactmeetentity/";
 			Process p;
 	
 			String s3PutIndex = String.format("s3cmd put %s %s",outputFile,s3directory);
 			System.out.println(s3PutIndex);
 			p = Runtime.getRuntime().exec(s3PutIndex);
 			p.waitFor();
 		}
 		catch (Exception e)
 		{
 			System.out.println("Writing to S3 failed");
 			e.printStackTrace();
 		}
 		
 		
 	}
 	public List<String> getDisambiguations(String entity) {
 		String baseFolder = "data/entities_expanded_new/";
 		List<String> disambiguations = new ArrayList<String>();
 		
 		try {
 			File file = new File(baseFolder + entity);
 			System.out.println(file.getAbsolutePath());
 			if(file.exists()) {
 				System.out.println("file exists");
 				BufferedReader reader = new BufferedReader(new FileReader(file));
 				String line = "";
 				while((line = reader.readLine()) != null) {
 					disambiguations.add(line.trim());
 				}
 				reader.close();
 			}
 		}
 		catch (Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 		return disambiguations;
 	}
 	@SuppressWarnings("unchecked")
 	public void readEntities() {
 		File file = new File(Constants.entitiesSerilizedFile);
 		if(file.exists()) {
 			entities = (List<Entity>)FileUtils.readFile(file.getAbsolutePath().toString());
 		}
 		else {
 			// Read from expansions file
 			Map<String,Set<String>> namesToExpansions = new HashMap<String,Set<String>>();
 			String expFileName = "data/entities_expansions";
 			try{
 				BufferedReader buf = new BufferedReader(new FileReader(expFileName));
 				String line;
 				while((line = buf.readLine())!=null)
 				{
 					String[] tok = line.split(":");
 					String name = tok[0];
 					Set<String> expansions = new HashSet<String>();
					String[] dollahSep = tok[1].split("$");
 					for (int j = 0;j<dollahSep.length;j++)
 					{
 						if (dollahSep[j].length() <= 0) continue;
 						expansions.add(dollahSep[j].toLowerCase());
 					}
 					namesToExpansions.put(name,expansions);
 				}
 				buf.close();
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			String fileName = "data/entities.csv";
 			entities = new ArrayList<Entity>();
 			try {
 				BufferedReader reader = new BufferedReader(new FileReader(fileName));
 				//BufferedWriter wbuf = new BufferedWriter(new FileWriter("entities_final"));
 				String line = "";
 				while((line = reader.readLine()) != null) {
 					String[] splits = line.split("\",\"");
 					EntityType type = splits[1].replace("\"", "").equals("PER") ? EntityType.PER : (splits[1].equals("ORG") ? EntityType.ORG : EntityType.FAC);
 					String name = splits[0].replace("\"", "").replace("http://en.wikipedia.org/wiki/", "").replace("https://twitter.com/", "");
 					//List<String> equivalents = Utils.getEquivalents(splits[3].replace("\"", ""));
 					List<String> equivalents = new ArrayList<String>(namesToExpansions.get(name));
 					Entity entity = new Entity(splits[0].replace("\"", ""), name, type, splits[2].replace("\"", ""),
 							equivalents, getDisambiguations(name));
 					entities.add(entity);
 					/*
 					wbuf.write(name + ":");
 					for (String s:equivalents)
 					{
 						wbuf.write(s);
 						wbuf.write("$");
 					}
 					wbuf.newLine();
 					wbuf.flush();
 					*/
 				}
 				reader.close();
 				//wbuf.close();
 			}
 			catch (Exception ex) {
 				System.out.println(ex.getMessage());
 			}
 			FileUtils.writeFile(entities, Constants.entitiesSerilizedFile);
 		}
 		/* for(Entity e:entities) {
 			System.out.println(e.getName());
 			System.out.println(e.getTargetID());
 			System.out.println(e.getGroup());
 			System.out.println(e.getEntityType());
 			System.out.println(e.getExpansions());
 			System.out.println(e.getDisambiguations());
 		} */
 	}
 	
 	
 	private static class ParallelNLPParsers implements Runnable{
 		Map<SlotPattern,Set<String>> allPatterns;
 		List<TrecTextDocument> trecDocs = new ArrayList<TrecTextDocument>();
 		Entity ent;
 		NLPUtils nlp;
 		List<String> relations;
 		public ParallelNLPParsers(Entity e, Map<String,List<TrecTextDocument>> queryResults, NLPUtils nlpin, 
 				List<String> relIn, Map<SlotPattern,Set<String>> out)
 		{
 			allPatterns = out;
 			ent = e;
 			nlp = nlpin;
 			relations = relIn;
 			String query = QueryBuilder.buildOrQuery(e.getExpansions());
 			if (queryResults.containsKey(query))
 				trecDocs = queryResults.get(query);
 		}
 		
 		private synchronized void addToPatternMap(Map<SlotPattern,List<String>> patterns)
 		{
 			for (SlotPattern pattern:patterns.keySet())
 			{
 				if (allPatterns.containsKey(pattern))
 				{
 					Set<String> existing = allPatterns.get(pattern);
 					existing.addAll(patterns.get(pattern));
 					allPatterns.put(pattern,existing);
 				}
 				else
 				{
 					allPatterns.put(pattern, new HashSet<String>(patterns.get(pattern)));
 				}
 			}
 		}
 		
 		public void run()
 		{
 			Set<String> uniqueSentences = new HashSet<String>();
 			Map<String, Set<String>> expansionToSentences = new HashMap<String,Set<String>>();
 			if (!trecDocs.isEmpty())
 			{
 				for(String expansion:ent.getExpansions()) 
 				{	
 					expansionToSentences.put(expansion, new HashSet<String>());
 					List<String> sentences  = ProcessTrecTextDocument.getCleanedSentences(ProcessTrecTextDocument.extractRelevantSentences(trecDocs, expansion));
 					for (String sentence:sentences)
 					{
 						if (uniqueSentences.contains(sentence))
 							continue;
 						//if (sentence.contains(" at "))
 							//System.out.println("sentence: " + sentence);
 						uniqueSentences.add(sentence);
 						Set<String> currentExpansionSet = expansionToSentences.get(expansion);
 						currentExpansionSet.add(sentence);
 						expansionToSentences.put(expansion,currentExpansionSet);
 						
 					}
 				}
 				//System.out.println("Query: " + ent.getExpansions().get(0) + " Size of results: " + uniqueSentences.size());
 				for (String expansion:expansionToSentences.keySet())
 				{
 					//System.out.println("Expansion: " + expansion);
 					Set<String> currentExpansionSet = expansionToSentences.get(expansion);
 					for (String sentence:currentExpansionSet)
 					{
 						Map<SlotPattern,List<String>> patterns = nlp.findSlotPatternGivenEntityAndRelation(sentence, expansion, relations);
 						addToPatternMap(patterns);	
 					}
 				}
 			}
 		}
 	}
 }
 
