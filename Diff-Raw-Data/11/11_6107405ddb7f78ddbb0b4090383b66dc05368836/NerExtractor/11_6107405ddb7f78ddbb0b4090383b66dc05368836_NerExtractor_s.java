 package com.cse454.nel.extract;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cse454.nel.EntityMention;
 import com.cse454.nel.Sentence;
 
 /**
  * Extracts entities using the information given in the annotation data (the sentence.*
  * files).
  *
  * @author andrewrogers
  *
  */
 public class NerExtractor extends AbstractEntityExtractor {
 
 	@Override
 	public List<EntityMention> extract(List<Sentence> sentences) {
 		List<EntityMention> entities = new ArrayList<EntityMention>();
 		for (Sentence sentence : sentences) {
 			String[] tokens = sentence.getTokens();
 			String[] ner = sentence.getNer();
 			int sentenceID = sentence.getSentenceId();
 			int numTokens = tokens.length;
 
 			for (int i = 0; i < numTokens; i++) {
 				if (ner[i].length() == 1
 						|| ner[i].equals("DATE")
 						|| ner[i].equals("PERCENT")
						|| ner[i].equals("NUMBER")
						|| ner[i].equals("MISC")) {
 					continue;
 				}
 				int startIndex = i;
 				StringBuffer buffer = new StringBuffer(tokens[i]);
 				while (i < numTokens - 1 && ner[i].equals(ner[i + 1])) {
 					i++;
 					buffer.append(" " + tokens[i]);
 				}
 				String entityText = buffer.toString();
 				int numToks = i - startIndex + 1;
 				entities.add(new EntityMention(sentenceID, entityText, startIndex, numToks));
 			}
 		}
 		return entities;
 	}
 }
