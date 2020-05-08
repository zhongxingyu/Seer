 package org.yomiassist;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.java.sen.SenFactory;
 import net.java.sen.StringTagger;
 import net.java.sen.dictionary.Token;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yomiassist.exception.YomiassistProcessingException;
 import org.yomiassist.sourcedata.Edict;
 import org.yomiassist.sourcedata.KnownWords;
 
 import com.ibm.icu.text.Transliterator;
 
 /**
  * Text Analyzer using GoSen as the underlying tokenizer.
  * 
  * @author Richard North <rich.north+yomiassist@gmail.com>
  *
  */
 public class GoSenTextAnalyzer implements TextAnalyzer {
 
 	private StringTagger stringTagger;
 	private static final Logger LOGGER = LoggerFactory.getLogger(GoSenTextAnalyzer.class);
 
 	public GoSenTextAnalyzer() {
 		File gosenConfigFile = new File("dictionaries/gosen/dictionary.xml");
 		String configurationFilename = gosenConfigFile.getAbsolutePath();
 		stringTagger = SenFactory.getStringTagger(configurationFilename);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<Ruby> analyzeText(String textToAnalyze, Map<String, Ruby> forcedRubies, Edict edictDictionary, KnownWords knownWords)
 			throws YomiassistProcessingException {
 		
 		final List<Token> analysis;
 		try {
 			analysis = stringTagger.analyze(textToAnalyze);
 		} catch (IOException e) {
 			throw new YomiassistProcessingException(e);
 		}
 
 		Transliterator tx = Transliterator.getInstance("Katakana-Hiragana");
 		List<Ruby> rubiesFound = new ArrayList<Ruby>();
 
 		for (Token token : analysis) {
 
 			String writtenForm = token.toString();
 			Ruby ruby;
 			final boolean hasReadings = token.getMorpheme().getReadings().size() > 0;
 			final boolean previouslyForcedRuby = forcedRubies.containsKey(writtenForm);
 
 			if (previouslyForcedRuby) {
 				ruby = forcedRubies.get(writtenForm);
 			} else if (hasReadings) {
 				String katakanaReading = token.getMorpheme().getReadings().get(0);
 				String reading = tx.transform(katakanaReading);
 
				String baseForm = token.getMorpheme().getBasicForm();
 				String definition = edictDictionary.lookup(baseForm);
 
 				if (!writtenForm.equals(reading) && !writtenForm.equals(katakanaReading)) {
 					ruby = new Ruby(writtenForm, reading, definition);
 				} else {
 					ruby = new Ruby(writtenForm, definition);
 				}
 			} else {
 				ruby = new Ruby(writtenForm);
 			}
 
			if (knownWords.getKnownWords().contains(writtenForm)) {
 				LOGGER.debug("Written form recognised as known: {}", writtenForm);
 				ruby.setKnownDefinition(true);
 				ruby.setKnownReading(true);
 			}
 
 			rubiesFound.add(ruby);
 		}
 
 		return rubiesFound;
 	}
 }
