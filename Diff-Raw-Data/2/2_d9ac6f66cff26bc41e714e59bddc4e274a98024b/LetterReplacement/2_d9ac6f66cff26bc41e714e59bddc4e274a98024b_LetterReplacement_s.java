 package com.alexrnl.subtitlecorrector.correctionstrategy;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.utils.Word;
 import com.alexrnl.subtitlecorrector.common.Subtitle;
 
 /**
  * TODO
  * @author Alex
  */
 public class LetterReplacement implements Strategy {
 	/** Logger */
 	private static Logger				lg	= Logger.getLogger(LetterReplacement.class.getName());
 	
 	/** The original letter to replace */
 	private final Parameter<Character>	originalLetter;
 	/** The new letter to put */
 	private final Parameter<Character>	newLetter;
 	/** Flag indicating to replace only in word which are not in the dictionary */
 	private final Parameter<Boolean>	onlyMissingFromDictionary;
 	/** Flag indicating to prompt user each time before replacing the letter */
 	private final Parameter<Boolean>	promptBeforeCorrecting;
 
 	/**
 	 * Constructor #1.<br />
 	 */
 	private LetterReplacement () {
 		super();
 		this.originalLetter = new Parameter<>(ParameterType.FREE, "strategy.letterreplacement.originalletter");
 		this.newLetter = new Parameter<>(ParameterType.FREE, "strategy.letterreplacement.newletter");
		this.onlyMissingFromDictionary = new Parameter<>(ParameterType.BOOLEAN, "strategy.letterreplacement.onlyfromdictionary", false, true);
 		this.promptBeforeCorrecting = new Parameter<>(ParameterType.BOOLEAN, "strategy.letterreplacement.promptbeforecorrecting", false, true);
 	}
 
 	@Override
 	public List<Parameter<?>> getParameters () {
 		final List<Parameter<?>> parameters = new ArrayList<>(4);
 		parameters.add(originalLetter);
 		parameters.add(newLetter);
 		parameters.add(onlyMissingFromDictionary);
 		parameters.add(promptBeforeCorrecting);
 		return parameters;
 	}
 	
 	@Override
 	public void correct (final Subtitle subtitle) {
 		if (!subtitle.getContent().contains(originalLetter.getValue().toString())) {
 			// Skip subtitles which are not concerned
 			return;
 		}
 		
 		String remaining = subtitle.getContent();
 		final StringBuilder newContent = new StringBuilder();
 		while (!remaining.isEmpty()) {
 			final Word currentWord = Word.getNextWord(remaining);
 			newContent.append(remaining.substring(0, currentWord.getBegin() - 1));
 			remaining = remaining.substring(currentWord.getEnd());
 			
 			if (!currentWord.getWord().contains(originalLetter.getValue().toString())) {
 				// The letter to replace is not in the word
 				newContent.append(currentWord);
 				continue;
 			}
 			
 			if (onlyMissingFromDictionary.getValue()) {// && dictionary.contains(currentWord.getWord())) {
 				// The current word is in the dictionary
 				continue;
 			}
 			
 			final String replacement = currentWord.getWord().replaceAll(
 					originalLetter.getValue().toString(), newLetter.getValue().toString());
 			
 			if (promptBeforeCorrecting.getValue()) {
 				// replacement = prompt.ask(subtitle.getContent(), currentWord, replacement);
 			}
 			newContent.append(replacement);
 		}
 		
 		
 		subtitle.setContent(newContent.toString());
 	}
 }
