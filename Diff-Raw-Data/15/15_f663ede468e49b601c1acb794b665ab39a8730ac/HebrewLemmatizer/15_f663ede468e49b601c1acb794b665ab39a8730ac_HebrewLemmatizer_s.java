 package org.wikipedia.miner.util.text;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import vohmm.application.SimpleTagger3;
 import vohmm.corpus.Anal;
 import vohmm.corpus.Sentence;
 import vohmm.corpus.Token;
 import vohmm.corpus.TokenExt;
 
 public class HebrewLemmatizer extends TextProcessor {
 	
 	private SimpleTagger3 _tagger;
 	private BufferedWriter _logger = null;
 	
 	{
 		try {
 			this._tagger = new SimpleTagger3("./tagger_data/");
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		try {
 			FileWriter fw = new FileWriter(new File("lemmatizer.log"));
 			this._logger = new BufferedWriter(fw); 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String processText(String text) {
 		
 		String result = "";
 		
 		try{
 			if (_logger != null){
 				_logger.write("start working on text: "+text);
 				_logger.newLine();
 			}
 			
 			StringBuilder textSB = new StringBuilder("");
 			List<Sentence> taggedSentences = this._tagger.getTaggedSentences(text);
 			for (Sentence sentence : taggedSentences){
 				StringBuilder sentenceSB = new StringBuilder("");
 				for (TokenExt tokenExt : sentence.getTokens()){
 					Token token = tokenExt._token;
 					Anal anal =  token.getSelectedAnal();
					String lemma = anal.getLemma().toString();
 					
					if (!lemma.isEmpty()){
						String[] wordParts = lemma.split("\\^");
 						sentenceSB.append(wordParts[wordParts.length - 1]).append(" ");
 					}
 				}
 				
 				if (!sentenceSB.toString().isEmpty()){
 					textSB.append(sentenceSB.toString().trim()).append("\n");
 				}
 			}
 			result = textSB.toString().trim();
 			
 			if (_logger != null){
 				_logger.write("finished. final text is " + result);
 				_logger.newLine();
 			}
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 
 }
