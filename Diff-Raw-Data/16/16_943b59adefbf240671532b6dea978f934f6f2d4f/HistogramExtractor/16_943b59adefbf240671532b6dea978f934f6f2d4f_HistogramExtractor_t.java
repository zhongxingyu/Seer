 package com.cse454.nel.extract;
 
 import java.io.BufferedWriter;
 
 import com.cse454.warmup.sf.retriever.ProcessedCorpus;
 
 public class HistogramExtractor {
 
 	private ProcessedCorpus mCorpus;
 
 	public HistogramExtractor(ProcessedCorpus corpus) {
 		mCorpus = corpus;
 	}
 
 	public void extract(BufferedWriter out) {
		/* NerExtractor entityExtractor = new NerExtractor();
		List<Sentence> annotations = null;
 		while(mCorpus.hasNext()) {
 			if (annotations == null) {
 				annotations = mCorpus.next();
 			}
 			// use the first sentence's meta file to find the last sentence in doc
 			String[] split = annotations.get(SFConstants.META).split("\t");
 			String curId = split[2];
 			String documentId = curId;
 			List<Map<String, String>> sentences = new ArrayList<Map<String,String>>();
 			Map<Entity, Integer> histogram = new HashMap<Entity, Integer>();
 			while (curId.equals(documentId)) {
 				sentences.add(annotations);
 				List<EntityMention> entities = entityExtractor.extract(annotations);
 				for (Entity entity: entities) {
 					if (!histogram.containsKey(entity)) {
 						histogram.put(entity, 1);
 					} else {
 						histogram.put(entity, histogram.get(entity) + 1);
 					}
 				}
 				if (!mCorpus.hasNext()) {
 					break;
 				}
 				annotations = mCorpus.next();
 				split = annotations.get(SFConstants.META).split("\t");
 				curId = split[2];
 			}
 
 			for (Entry<Entity,Integer> entry : histogram.entrySet()) {
 				System.out.println(entry.getKey().toString() + "\t" + entry.getValue());
 			}
 			break;
 		}
		*/
 	}
 }
