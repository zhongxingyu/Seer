 package retrieWin.SSF;
 
 import edu.stanford.nlp.parser.lexparser.NoSuchParseException;
 import fig.basic.*;
 import fig.exec.Execution;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import retrieWin.Indexer.Indexer;
 import retrieWin.Indexer.ProcessTrecTextDocument;
 import retrieWin.Indexer.TrecTextDocument;
 import retrieWin.SSF.Constants.EntityType;
 import retrieWin.SSF.Constants.NERType;
 import retrieWin.SSF.Constants.SlotName;
 import retrieWin.SSF.SlotPattern.Rule;
 import retrieWin.Utils.FileUtils;
 import retrieWin.Utils.NLPUtils;
 import retrieWin.Utils.Utils;
 
 public class SSF implements Runnable{
 	@Option(gloss="working Directory") public String workingDirectory;
 	@Option(gloss="download Hour") public String downloadHour;
 	@Option(gloss="index Location") public String indexLocation;
 	@Option(gloss="index Location") public String saveAsDirectory;
 	List<Slot> slots;
 	List<Entity> entities;
 	NLPUtils coreNLP;
 	Concept conceptExtractor;
 	
 	public SSF() {
 		initialize();
 	}
 	
 	public void initialize() {
 		readEntities();
 		readSlots();
 		//coreNLP = new NLPUtils();
 		conceptExtractor = new Concept();
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
					String[] dollahSep = tok[1].split("\\$");
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
 				System.out.println("Entities expansion file not present");
 				e.printStackTrace();
 			}
 			String fileName = "data/entities.csv";
 			entities = new ArrayList<Entity>();
 			try {
 				BufferedReader reader = new BufferedReader(new FileReader(fileName));
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
 				}
 				reader.close();	
 			}
 			catch (Exception ex) {
 				System.out.println(ex.getMessage());
 			}
 			FileUtils.writeFile(entities, Constants.entitiesSerilizedFile);
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
 	public void readSlots() {
 		File file = new File(Constants.slotsSerializedFile);
 		if(file.exists()) {
 			slots = (List<Slot>)FileUtils.readFile(file.getAbsolutePath().toString());
 		}
 		else {
 			String inputFileName = "data/Slots.csv";
 	
 			try {
 					List<Slot> slots = new ArrayList<Slot>();
 					BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
 					String line = "";
 					while((line = reader.readLine()) != null) {
 						String[] vals = line.trim().split(",");
 						Slot slot = new Slot();
 						slot.setName(SlotName.valueOf(vals[0]));
 						slot.setEntityType(EntityType.valueOf(vals[1]));
 						slot.setThreshold(Double.parseDouble(vals[2]));
 						slot.setApplyPatternAfterCoreference(Integer.parseInt(vals[3]) == 1 ? true : false);
 						slot.setPatterns(new ArrayList<SlotPattern>());
 						List<NERType> targetNERTypes = new ArrayList<NERType>();
 						for(String ner: vals[4].split("\\$")) {
 							targetNERTypes.add(NERType.valueOf(ner));
 						}
 						slot.setTargetNERTypes(targetNERTypes);
 						
 						if(slot.getEntityType() == EntityType.FAC)
 							slot.setSourceNERTypes(Arrays.asList(NERType.ORGANIZATION, NERType.LOCATION));
 						else if(slot.getEntityType() == EntityType.PER)
 							slot.setSourceNERTypes(Arrays.asList(NERType.PERSON));
 						else
 							slot.setSourceNERTypes(Arrays.asList(NERType.ORGANIZATION));
 						slots.add(slot);
 					}
 					reader.close();
 					FileUtils.writeFile(slots, Constants.slotsSerializedFile);
 			}
 			catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 		/* for(Slot slot: slots)
 			System.out.println(slot.getName() + "," + slot.getEntityType() + "," + slot.getSourceNERTypes() + "," + slot.getTargetNERTypes() + "," + slot.getThreshold());
 		//System.out.println(slots); */
 	}
 	
 	
 	private boolean containsUppercaseToken(String str) {
 		for(String token: str.split(" ")) {
 			if(token.isEmpty())
 				continue;
 			if(Character.isUpperCase(token.charAt(0)))
 					return true;
 		}
 		return false;
 	}
 	
 	private Map<String, Double> findTitles(Entity entity, Slot slot, Map<String, Map<String, String>> relevantSentences, NLPUtils coreNLP, Concept conceptExtractor) {
 		Map<String, Double> candidates = new HashMap<String, Double>();
 		for(String expansion: entity.getExpansions()) {
 			for(String sentence: relevantSentences.get(expansion).keySet()) {
 				System.out.println(relevantSentences.get(expansion).get(sentence) + ":" + sentence);
 				try {
 					//check for any non-pronominal coreference
 					for(String title: coreNLP.getCorefs(sentence, expansion))
 						if(containsUppercaseToken(title) && !candidates.containsKey(title))
 							candidates.put(title, 1.0);
 					
 					//check for any compund nouns for this entity
 					String nnTitle = coreNLP.getNNs(sentence, expansion);
 					if(containsUppercaseToken(nnTitle) && !candidates.containsKey(nnTitle))
 						candidates.put(nnTitle, 1.0);
 				} catch(NoSuchParseException e) {
 					e.printStackTrace();
 					break;
 				}
 			}
 		}
 		return candidates;
 	}
 
 	
 	private Map<String, Double> findCandidates(Entity entity, Slot slot, Map<String, Map<String, String>> relevantSentences, NLPUtils coreNLP, Concept conceptExtractor) {
 		if(slot.getName().equals(Constants.SlotName.Titles))
 			return findTitles(entity, slot, relevantSentences, coreNLP, conceptExtractor);
 		
 		Map<String, Double> candidates = new HashMap<String, Double>();
 		for(String expansion: entity.getExpansions()) {
 			for(String sentence: relevantSentences.get(expansion).keySet()) {
 				System.out.println(relevantSentences.get(expansion).get(sentence) + ":" + sentence);
 				for(SlotPattern pattern: slot.getPatterns()) {
 					try {
 						//for each sentence, find possible slot values and add to candidate list
 						//arxiv documents
 						if(relevantSentences.get(expansion).get(sentence).contains("arxiv")) {
 							if(!slot.getName().equals(Constants.SlotName.Affiliate_Of) || !entity.getEntityType().equals(Constants.EntityType.PER))
 								continue;
 							arxivDocument arxivDoc = new arxivDocument(relevantSentences.get(expansion).get(sentence));
 							if(arxivDoc.getAuthors().contains(expansion)) {
 								for(String author: arxivDoc.getAuthors()) 
 									if(!author.equals(expansion))
 										candidates.put(author, 1.0);
 								for(String ack: arxivDoc.getAcknowledgements())
 									candidates.put(ack, 1.0);
 							}
 							else if(arxivDoc.getAcknowledgements().contains(expansion)) {
 								for(String author: arxivDoc.getAuthors()) 
 									candidates.put(author, 1.0);
 							}
 							
 							for(List<String> reference: arxivDoc.getReferences()) {
 									if(reference.contains(expansion)) {
 										for(String author: reference) 
 											if(!author.equals(expansion))
 												candidates.put(author, 1.0);
 									}
 							}
 						}
 						//social documents
 						else if(relevantSentences.get(expansion).get(sentence).contains("social")) {
 							for(String str: coreNLP.findSlotValue(sentence, expansion, pattern, slot.getTargetNERTypes())) {
 								//get normalized concept from candidate
 								String concept = conceptExtractor.getConcept(str);
 								if(!candidates.containsKey(concept))
 									candidates.put(concept, pattern.getConfidenceScore());
 								else
 									candidates.put(concept, candidates.get(concept) + pattern.getConfidenceScore());
 							}
 						}
 						//news documents
 						else {
 							for(String str: coreNLP.findSlotValue(sentence, expansion, pattern, slot.getTargetNERTypes())) {
 								//get normalized concept from candidate
 								String concept = conceptExtractor.getConcept(str);
 								if(!candidates.containsKey(concept))
 									candidates.put(concept, pattern.getConfidenceScore());
 								else
 									candidates.put(concept, candidates.get(concept) + pattern.getConfidenceScore());
 							}
 						}
 					} catch(NoSuchParseException e) {
 						e.printStackTrace();
 						break;
 					}
 				}
 			}
 		}
 		return candidates;
 	}
 	
 	public void runSSF(String timestamp) {
 		/** create index for the current hour **/
 		System.out.println("Creating index...");
 		String[] splits = timestamp.split("-");
 		String currentFolder = workingDirectory;
 		for (int i = 0;i<4;i++)
 		{
 			currentFolder = currentFolder + splits[i] + "/";
 			File f = new File(currentFolder);
 			if (!f.exists())
 				f.mkdir();
 		}
 		String year = splits[0],month = splits[1], day = splits[2], hour = splits[3];
 		
 		String baseFolder = String.format("%s/%s/%s/%s/",year,month,day,hour);
 		String indexLocation = workingDirectory + baseFolder + "index/";
 		String tempDirectory = workingDirectory + baseFolder + "temp/";
 		String trecTextSerializedFile = workingDirectory + baseFolder + "filteredSerialized.ser";
 		// if the directory does not exist, create it
 		File baseDir = new File(baseFolder);
 		if (!baseDir.exists())
 			baseDir.mkdirs();
 
 		Indexer.createIndex(timestamp,baseFolder, tempDirectory, indexLocation, trecTextSerializedFile, entities);
 		
 		/*
 		/** read in existing information for entities 
 		System.out.println("Reading entities...");
 		readEntities();
 		
 		/** read in slot information 
 		System.out.println("Reading slots...");
 		readSlots();
 		System.out.println(slots);
 		
 		/** for each entity, for each slot, for each entity expansion
 		System.out.println("Finding slot values...");
 		for(Entity entity: entities) {
 			//get all relevant documents for the entity
 			List<TrecTextDocument> docs = entity.getRelevantDocuments(timestamp, entities);
 			System.out.println("Retrieved " + docs.size() + " TrecTextDocuments for entity: " + entity.getName());
 			if(docs.isEmpty())
 				continue;
 			
 			//get relevant sentences for each expansion, ensure no sentence retrieved twice
 			Map<String, Map<String, String>> relevantSentences = new HashMap<String, Map<String, String>>();
 			Set<String> retrievedSentences = new HashSet<String>();
 			for(String expansion: entity.getExpansions()) {
 				Map<String, String> sentences = new HashMap<String, String>();
 				Map<String, String> returnedSet = ProcessTrecTextDocument.extractRelevantSentencesWithDocID(docs, expansion);
 				for(String sent: returnedSet.keySet()) 
 					if(!retrievedSentences.contains(sent)) {
 							sentences.put(sent, returnedSet.get(sent));
 							retrievedSentences.add(sent);
 					}
 				relevantSentences.put(expansion, sentences);
 			}
 			System.out.println("Number of relevant sentences: " + retrievedSentences.size());
 			
 			//iterate to fill slots
 			for(Slot slot: slots) {
 				//compute only for relevant slots for this entity
 				if(!slot.getEntityType().equals(entity.getEntityType()))
 						continue;
 				//TODO: remove this, computing only one slot right now
 				System.out.println("In slot " + slot.getName());
 				if(!slot.getName().equals(Constants.SlotName.Founder_Of) && !slot.getName().equals(Constants.SlotName.Titles))
 					continue;
 				
 				System.out.println("Finding value for " + slot.getName());
 				//for each expansion, slot pattern, get all possible candidates
 				Map<String, Double> candidates = findCandidates(entity, slot, relevantSentences, coreNLP, conceptExtractor);
 				
 				//remove candidates below the threshold value
 				List<String> finalCandidateList = new ArrayList<String>();
 				for(String key: candidates.keySet()) 
 					if(candidates.get(key) > slot.getThreshold()) 
 						finalCandidateList.add(key);
 				
 				//updating slots
 				System.out.println(entity.getName() + "," + slot.getName() + ":" + entity.updateSlot(slot, finalCandidateList));
 			}
 		}
 		*/
 	}
 	
 	void buildLargeIndex() {
 		List<String> downloadHours = new ArrayList<String>();
 		for(int i = 11; i <= 11; i++) {
 			for(int j=11; j < 12; j++) {
 				String downloadHour = String.format("2011-11-%02d-%02d", i, j);
 				downloadHours.add(downloadHour);
 			}
 		}
 		
 		Indexer.createUnfilteredIndex(downloadHours, workingDirectory, saveAsDirectory, indexLocation);
 	}
 	
 	
 	private void updateSlots() {
 		readSlots();
 		for(Slot slot: slots) {
 			if(slot.getName().equals(Constants.SlotName.Awards_Won))
 				slot.addSlotPatterns("data/slots/awards_won");
 		}
 		System.out.println(slots);
 		FileUtils.writeFile(slots, Constants.slotsSerializedFile);
 	}
 	
 	public static void main(String[] args) {
 		if (!System.getenv().containsKey("LD_LIBRARY_PATH"))
 		{
 			System.out.println("Environment variable not set");
 			return;
 		}
 		//new SSF().updateSlots();
 		Execution.run(args, "Main", new SSF());
 	}
 
 	@Override
 	public void run() {
 		boolean terminate = false;
 		if(workingDirectory == null || workingDirectory.isEmpty()) {
 			LogInfo.logs("Working directory cannot be empty. Set the -workingDirectory option.");
 			terminate = true;
 		}
 		if(downloadHour == null || downloadHour.isEmpty()) {
 			LogInfo.logs("Download hour cannot be empty. Set the -downloadHour option.");
 			terminate = true;
 		}
 		if(terminate) {
 			LogInfo.logs("Terminating execution!");
 			return;
 		}
 		if(workingDirectory!=null && !workingDirectory.endsWith("/"))
 			workingDirectory += "/";
 		
 		if(indexLocation!=null && !indexLocation.endsWith("/"))
 			indexLocation += "/";
 		
 		if(saveAsDirectory !=null && !saveAsDirectory.endsWith("/"))
 			saveAsDirectory += "/";
 		
 		LogInfo.begin_track("run()");
 		LogInfo.logs(String.format("Download hour     : %s", downloadHour));
 		LogInfo.logs(String.format("Working directory : %s", workingDirectory));
 		
 		runSSF(downloadHour);
 		//buildLargeIndex();
 		
 		LogInfo.end_track();
 	}
 }
