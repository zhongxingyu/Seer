 package org.wikipedia.miner.util.text;
 
 import java.util.List;
 
 import vohmm.application.SimpleTagger3;
 import vohmm.corpus.Anal;
 import vohmm.corpus.Sentence;
 import vohmm.corpus.Token;
 import vohmm.corpus.TokenExt;
 
 public class HebrewLemmatizer extends TextProcessor {
 	
 	private SimpleTagger3 _tagger;
 	
 	{
 		try {
 			this._tagger = new SimpleTagger3("./");
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String processText(String text) {
 		
 		String result = "";
 		
 		try{
 			StringBuilder textSB = new StringBuilder();
 			List<Sentence> taggedSentences = this._tagger.getTaggedSentences(text);
 			for (Sentence sentence : taggedSentences){
 				StringBuilder sentenceSB = new StringBuilder();
 				for (TokenExt tokenExt : sentence.getTokens()){
 					Token token = tokenExt._token;
 					Anal anal =  token.getSelectedAnal();
					String lemma = anal.getLemma().toString();
					
					if (!lemma.isEmpty()){
						String[] wordParts = lemma.split("\\^");
						lemma = wordParts[wordParts.length - 1];
					}
					
					sentenceSB.append(lemma).append(" ");
 				}
 				textSB.append(sentenceSB.toString().trim()).append("\n");
 			}
 			result = textSB.toString().trim();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 
 }
