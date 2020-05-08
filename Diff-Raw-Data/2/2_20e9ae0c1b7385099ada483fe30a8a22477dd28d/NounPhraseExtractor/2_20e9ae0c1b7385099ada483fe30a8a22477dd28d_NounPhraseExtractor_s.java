 package com.personalityextractor.entity.extractor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import com.personalityextractor.evaluation.PerfMetrics;
 import com.personalityextractor.evaluation.PerfMetrics.Metric;
 
 import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
 import edu.stanford.nlp.trees.Tree;
 
 public class NounPhraseExtractor implements IEntityExtractor {
 
 	private static LexicalizedParser lp;
 
 	static {
 		try {
 			Date d1 = new Date();
			lp = new LexicalizedParser("/Users/tejaswi/Documents/workspace/PersonalityExtraction/lair/englishPCFG.ser.gz");
 			lp.setOptionFlags(new String[] { "-maxLength", "80",
 					"-retainTmpSubcategories" });
 			Date d2 = new Date();
 			PerfMetrics.getInstance().addToMetrics(Metric.LOAD, (d2.getTime()-d1.getTime()));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	public List<String> extract(String text) {
 		
 		
 		String[] lines = text.split("[;'\"?><,\\.!$%^&()-+=~`{}|]+");
 		ArrayList<String> phrases = new ArrayList<String>();
 		
 		for (String line : lines) {
 			if(line.trim().length()==0)
 				continue;
 			String[] sent = line.split(" ");
 			Tree parse = (Tree) lp.apply(Arrays.asList(sent));
 			// TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
 			// tp.printTree(parse);
 			ArrayList<Tree> queue = new ArrayList<Tree>();
 			queue.add(parse);
 
 			StringBuffer str = new StringBuffer();
 			boolean flag = false;
 			while (!queue.isEmpty()) {
 				Tree topNode = queue.remove(0);
 
 				if (topNode.isPreTerminal()) {
 					if (topNode.value().startsWith("NN")) {
 						str.append(topNode.children()[0].value() + " ");
 						flag = true;
 					} else if (flag == true) {
 						flag = false;
 						phrases
 								.add(str.toString().trim()
 										.replaceAll("\\.", ""));
 						str = new StringBuffer();
 					}
 				} else if (flag == true) {
 					flag = false;
 					phrases.add(str.toString().trim().replaceAll("\\.", ""));
 					str = new StringBuffer();
 				}
 				// add all children to queue regardless
 				for (Tree c : topNode.children()) {
 					queue.add(c);
 				}
 			}
 
 			if (flag == true) {
 				phrases.add(str.toString().trim().replaceAll("\\.", ""));
 			}
 		}
 		return phrases;
 	}
 
 	public static void main(String[] args) {
 //		NounPhraseExtractor
 //				.initialize("/home/semanticvoid/PE/PersonalityExtraction/lair/englishPCFG.ser.gz");
 
 		IEntityExtractor e = new NounPhraseExtractor();
 		List<String> sentences = Arrays.asList(
 				"@Kv @Ushu My cousin told a Hyderabadi auto driver \"Station jaane ki aavashyakatha hai\" apparently yet to come out of the Madhyamika hangover"
 				//"Ushu My cousin told a Hyderabadi auto driver ."
 //				"Rest in Peace!",
 //				 "New blog post: 50 days with Google Nexus S: http://www.venu.in/blog/?p=314",
 //				 "@dpolice Hard to say. If the user is geeky - Nexus S . Otherwise iPhone 4 . :) Both are great phones.",
 //				 "About to embark on the unthinkable... Driving to New York City. Wish me luck.",
 //				 "Best part of The Hurt Locker ? The lack of background music! Silence speaks quite loudly in this movie.",
 //				 "I'm playing the Age of Empires.",
 //				 "iTunes / ipod ecosystem needs to learn a thing or two from Doggcatcher. Seriously. This is the best solution for podcast listeners out there.",
 //				 "loved India New Land of Opportunity on Boxee http://bit.ly/ghYcfj",
 //				 "@vjvegi Why this comment about Pakistan all of a sudden? :)",
 //				 "Swapped the Elantra with a Santa Fe to deal with all that snow on the roads."
 		);
 
 		for (String sentence : sentences) {
 			List<String> entities = e.extract(sentence);
 
 			for (String entity : entities) {
 				System.out.println("'" + entity + "'");
 			}
 		}
 	}
 }
