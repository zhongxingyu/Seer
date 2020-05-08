 /**
  * 
  */
 package com.personalityextractor.evaluation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 
 import com.personalityextractor.entity.extraction.IEntityExtractor;
 import com.personalityextractor.entity.extraction.PoorMansEntityExtractor;
 
 /**
  * @author akishore
  *
  */
 public class EntityExtractionEvaluation {
 
 	private ArrayList<Record> records;
 	
 	public EntityExtractionEvaluation(String filePath) throws Exception {
 		records = new ArrayList<EntityExtractionEvaluation.Record>();
 		readDataSet(filePath);
 	}
 	
 	private void readDataSet(String path) throws Exception {
 		BufferedReader rdr = new BufferedReader(new FileReader(new File(path)));
 		
 		String line;
 		while((line = rdr.readLine()) != null) {
 			Record r = parseLine(line);
 			if(r != null || r.text != null) {
 				records.add(r);
 			}
 		}
 	}
 	
 	private Record parseLine(String line) {
 		String text = line.replaceAll("(<E>)|(</E>)", "");
 		Record r = new Record();
 		r.text = text;
 		
 		int flag = 0;
 		int sIndex = -1;
 		int eIndex = -1;
 		for(int i=0; i<line.length(); i++) {
 			char c = line.charAt(i);
 			
			if(c == '<' && flag == 0) {
				flag = 1;
			} else if(c == 'E' && flag == 1) {
				flag = 2;
			} else if(c == '>' && flag == 2) {
				flag = 3;
			} else if(c == '/' && flag == 1) {
				flag = 4;
			} else if(c == 'E' && flag == 4) {
				flag = 5;
			} else if(c == '>' && flag == 5) {
				flag = 6;
 			} else {
 				flag = 0;
 			}
 			
 			if(flag == 3 && sIndex == -1) {
 				sIndex = i + 1;
			} else if(flag == 6) {
 				eIndex = i - 3;
 				r.entities.add(line.substring(sIndex, eIndex));
 				sIndex = -1;
 				eIndex = -1;
 			}
 		}
 		
 		return r;
 	}
 	
 	public EvalMetrics evaluate(IEntityExtractor extractor) {
 		EvalMetrics metrics = new EvalMetrics();
 		
 		for(Record r : records) {
 			ArrayList<String> extractedEntities = extractor.extract(r.text);
 			for(String exEntity : extractedEntities) {
 				if(r.entities.size() > 0) {
 					for(String entity : r.entities) {
 						if(exEntity.equalsIgnoreCase(entity)) {
 							metrics.incrTP();
 						} else {
 							metrics.incrFP();
 						}
 					}
 				} else {
 					metrics.incrFP();
 				}
 			}
 		}
 		
 		return metrics;
 	}
 	
 	public static void printUsage() {
 		System.out.println("java com....EntityExtEval <annotated dataset>");
 		System.exit(0);
 	}
 	
 	private class Record {
 		public String text;
 		public ArrayList<String> entities = new ArrayList<String>();
 	}
 	
 	public static void main(String[] args) {
 		if(args.length < 1) {
 			printUsage();
 		}
 		
 		try {
 			EntityExtractionEvaluation eval = new EntityExtractionEvaluation(args[0]);
 			EvalMetrics metrics = eval.evaluate(new PoorMansEntityExtractor());
 			System.out.println(metrics.calculateError());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 }
