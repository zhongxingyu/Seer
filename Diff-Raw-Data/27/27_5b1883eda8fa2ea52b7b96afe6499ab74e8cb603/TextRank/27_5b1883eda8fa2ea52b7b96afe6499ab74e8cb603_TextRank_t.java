 import java.io.*;
 import java.util.HashMap;
 
 import LBJ2.nlp.SentenceSplitter;
 import LBJ2.nlp.WordSplitter;
 import LBJ2.nlp.seg.PlainToTokenParser;
import LBJ2.nlp.seg.Token;
 
 import edu.illinois.cs.cogcomp.lbj.pos.POSTagPlain;
 import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
 
 public class TextRank {
 	
 	public static String summarizeText(File file){
 		BufferedReader input; 
 		try {
 			
 			input = new BufferedReader(new FileReader(file));
 			String line = null;
 			if((line = input.readLine()) != null){
 				input.close();
 				return line;
 			}
 			else {
 				input.close();
 				return "";
 			}
 			
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
	public static HashMap<Token, String> getPartOfSpeechTag(File file){
 		POSTagger tagger = new POSTagger();
 		PlainToTokenParser parser = new PlainToTokenParser( new WordSplitter( new SentenceSplitter(file.getPath())));
		Token word;
		HashMap<Token, String> parsedWords = new HashMap<Token, String>();
		
		
 		
		while((word =  (Token) parser.next()) != null){
 			String value = tagger.discreteValue(word);
 			System.out.println(word.form + ": " + value);
 			parsedWords.put(word, value);
 		}
 		
 		return parsedWords;
 	}
 	
	public static HashMap<Token, String> getPartOfSpeechTagAdjNounOnly(File file){
 		POSTagger tagger = new POSTagger();
 		PlainToTokenParser parser = new PlainToTokenParser( new WordSplitter( new SentenceSplitter(file.getPath())));
		Token word;
		HashMap<Token, String> parsedWords = new HashMap<Token, String>();
 		
		while((word =  (Token) parser.next()) != null){
 			String value = tagger.discreteValue(word);
 			System.out.println(word.form + ": " + value);
 			if(value.contains("NN") || value.contains("JJ")){
 				parsedWords.put(word, tagger.discreteValue(word));
 			}
 		}
 		
 		return parsedWords;
 	}
 
 }
