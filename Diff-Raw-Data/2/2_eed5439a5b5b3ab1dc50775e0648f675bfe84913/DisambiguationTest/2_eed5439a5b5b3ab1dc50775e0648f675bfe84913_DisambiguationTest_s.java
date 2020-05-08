 package com.cse454.nel;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import com.cse454.nel.disambiguate.AbstractDisambiguator;
 import com.cse454.nel.disambiguate.SimpleDisambiguator;
 import com.cse454.nel.extract.AbstractEntityExtractor;
 import com.cse454.nel.extract.NerExtractor;
 import com.cse454.nel.search.AbstractSearcher;
 import com.cse454.nel.search.BasicSearcher;
 
 public class DisambiguationTest {
 
 	private static Scanner scanner;
 
 	public static void main(String[] args) throws Exception {
 		scanner = new Scanner(System.in);
 
 		while (true) {
 			System.out.print("Enter a docId: ");
 			String docId = scanner.nextLine();
 
 			WikiConnect wiki = new WikiConnect();
 			DocumentConnect docs = new DocumentConnect();
			List<Sentence> sentences = docs.getDocument(Integer.valueOf(docId).intValue());
 
 			AbstractEntityExtractor extractor = new NerExtractor();
 			List<EntityMention> mentions = extractor.extract(sentences);
 
 			AbstractSearcher searcher = new BasicSearcher(wiki);
 			for (EntityMention mention : mentions) {
 				searcher.GetCandidateEntities(mention);
 			}
 
 			AbstractDisambiguator disambiguator = new SimpleDisambiguator();
 			Map<EntityMention, Entity> map = disambiguator.disambiguate(mentions);
 
 			for (Entry<EntityMention, Entity> entry : map.entrySet()) {
 				if (entry == null || entry.getValue() == null) {
 					continue;
 				}
 				String wikiId = entry.getValue().wikiTitle;
 				System.out.printf("%s\t\t%s\n", entry.getKey().mentionString, wikiId);
 			}
 
 		}
 	}
 
 }
