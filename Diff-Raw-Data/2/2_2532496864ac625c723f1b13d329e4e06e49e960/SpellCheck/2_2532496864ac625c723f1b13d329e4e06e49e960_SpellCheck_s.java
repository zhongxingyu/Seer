 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.transform.text;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.datatypes.core.exceptions.UnsupportedDataTypeException;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.generic.io.IOUtils;
 
 import com.swabunga.spell.engine.SpellDictionary;
 import com.swabunga.spell.engine.SpellDictionaryHashMap;
 import com.swabunga.spell.event.SpellCheckEvent;
 import com.swabunga.spell.event.SpellCheckListener;
 import com.swabunga.spell.event.SpellChecker;
 import com.swabunga.spell.event.StringWordTokenizer;
 
 @Component(
         creator = "Boris Capitanu",
         description = "Performs spell checking on the input and optionally replaces misspelled words " +
             		  "with the top ranked suggestion. The component also produces a list of the misspellings " +
             		  "in the document.",
         name = "Spell Check",
         tags = "dictionary, word, spell check",
         firingPolicy = FiringPolicy.any,
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class SpellCheck extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The text, tokens, or token counts that needs to be spell checked" +
                           "<br>TYPE: java.lang.String" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                           "<br>TYPE: byte[]" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
     
     @ComponentInput(
             name = "dictionary",
             description = "The wordlist to be used as dictionary or the location of the wordlist file" +
                           "<br>TYPE: java.net.URI" +
                           "<br>TYPE: java.net.URL" +
                           "<br>TYPE: java.lang.String" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                           "<br>TYPE: byte[]" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_DICTIONARY = "dictionary";
     
     //------------------------------ OUTPUTS -----------------------------------------------------
     
     @ComponentOutput(
             name = "replacement_rules",
             description = "The replacement rules for misspelled words in the following format: correctedWord = {badWord1, badWord2, ... }; ..." +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_RULES = "replacement_rules";
     
     @ComponentOutput(
             name = "replacements",
             description = "The replacements suggested for misspelled words" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
     )
     protected static final String OUT_REPLACEMENTS = "replacements";
     
     @ComponentOutput(
             name = Names.PORT_TEXT,
             description = "The original text with corrections applied if the 'do_correction' property was set to 'true'" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TEXT = Names.PORT_TEXT;
     
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = "do_correction",
             description = "True to correct misspelled words, False otherwise",
             defaultValue = "true"
     )
     protected static final String PROP_DO_CORRECTION = "do_correction";
 
     //--------------------------------------------------------------------------------------------
     
     private boolean _doCorrection;
     private Queue<Object> _inputQueue;
     private SpellChecker _spellChecker;
     
     //--------------------------------------------------------------------------------------------
     
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _doCorrection = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_DO_CORRECTION, ccp));
         _inputQueue = new LinkedList<Object>();
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         if (cc.isInputAvailable(IN_DICTIONARY)) {
             Object in_dictionary = cc.getDataComponentFromInput(IN_DICTIONARY);
             Reader dictReader;
             
             try {
                 // try parsing as url
                 URI dictUri = DataTypeParser.parseAsURI(in_dictionary);
                 dictReader = IOUtils.getReaderForResource(dictUri);
             }
             catch (Exception e) {
                 // parse as wordlist
                 String[] wordList = DataTypeParser.parseAsString(in_dictionary);
                 StringBuilder sb = new StringBuilder();
                 for (String word : wordList)
                     sb.append(word).append("\n");
                 dictReader = new StringReader(sb.toString());
             }
             
             SpellDictionary dictionary = new SpellDictionaryHashMap(dictReader);
             _spellChecker = new SpellChecker(dictionary);
         }
         
         if (cc.isInputAvailable(IN_TEXT))
             _inputQueue.offer(cc.getDataComponentFromInput(IN_TEXT));
         
         if (_spellChecker != null && _inputQueue.size() > 0) {
             for (int i = 0, iMax = _inputQueue.size(); i < iMax; i++) {
                 Object input = _inputQueue.poll();
                 
                 try {
                     // try parsing as token counts
                     Map<String,Integer> tokenCounts = DataTypeParser.parseAsStringIntegerMap(input);
                     processTokenCounts(tokenCounts);
                 }
                 catch (UnsupportedDataTypeException e) {
                     // try parsing as tokenized sentences
                     try {
                         Map<String,String[]> tokenizedSentences = DataTypeParser.parseAsStringStringArrayMap(input);
                         processTokenizedSentences(tokenizedSentences);
                     }
                     catch (UnsupportedDataTypeException e1) {
                         // parse as text
                         String[] text = DataTypeParser.parseAsString(input);
                         processText(text);
                     }                    
                 }
             }
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _inputQueue.clear();
         _spellChecker = null;
     }
     
     //--------------------------------------------------------------------------------------------
 
     @Override
     protected void handleStreamInitiators() throws Exception {
         if (!inputPortsWithInitiators.contains(IN_TEXT )) {
             console.severe("Unexpected StreamInitiator received");
             return;
         }
 
         componentContext.pushDataComponentToOutput(OUT_TEXT, componentContext.getDataComponentFromInput(IN_TEXT));
         componentContext.pushDataComponentToOutput(OUT_RULES, componentContext.getDataComponentFromInput(IN_TEXT));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, componentContext.getDataComponentFromInput(IN_TEXT));
     }
     
     @Override
     protected void handleStreamTerminators() throws Exception {
         if (!inputPortsWithTerminators.contains(IN_TEXT)) {
             console.severe("Unexpected StreamTerminator received");
             return;
         }
 
         componentContext.pushDataComponentToOutput(OUT_TEXT, componentContext.getDataComponentFromInput(IN_TEXT));
         componentContext.pushDataComponentToOutput(OUT_RULES, componentContext.getDataComponentFromInput(IN_TEXT));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, componentContext.getDataComponentFromInput(IN_TEXT));
     }
     
     //--------------------------------------------------------------------------------------------
 
     private void processText(String[] text) throws ComponentContextException {
         SuggestionListener listener = new SuggestionListener(_doCorrection, console);
         _spellChecker.addSpellCheckListener(listener);
         
         for (int i = 0, iMax = text.length; i < iMax; i++) {
             StringWordTokenizer wordTokenizer = new StringWordTokenizer(text[i]);
             _spellChecker.checkSpelling(wordTokenizer);
             text[i] = wordTokenizer.getContext();
         }
         
         _spellChecker.removeSpellCheckListener(listener);
         
         String replacementRules = listener.getReplacementRules();
         console.fine("Replacement rules: " + replacementRules);
         
         Map<String,Set<String>> replacements = listener.getReplacements();
         
         componentContext.pushDataComponentToOutput(OUT_RULES, BasicDataTypesTools.stringToStrings(replacementRules));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, BasicDataTypesTools.mapToStringMap(getReplacementsMap(replacements)));
         componentContext.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
     }
 
     private void processTokenizedSentences(Map<String, String[]> tokenizedSentences) throws ComponentContextException {
         SuggestionListener listener = new SuggestionListener(_doCorrection, console);
         _spellChecker.addSpellCheckListener(listener);
         
         Map<String, String[]> correctedTokenizedSentences = new HashMap<String, String[]>(tokenizedSentences.size());
         for (Entry<String, String[]> entry : tokenizedSentences.entrySet()) {
             String sentence = entry.getKey();
             String[] tokens = entry.getValue();
             
             StringWordTokenizer wordTokenizer = new StringWordTokenizer(sentence);
             _spellChecker.checkSpelling(wordTokenizer);
             
             if (_doCorrection) {
                 sentence = wordTokenizer.getContext();
                 
                 for (int i = 0, iMax = tokens.length; i < iMax; i++) {
                     wordTokenizer = new StringWordTokenizer(tokens[i]);
                     _spellChecker.checkSpelling(wordTokenizer);
                     tokens[i] = wordTokenizer.getContext();
                 }
                 
                 correctedTokenizedSentences.put(sentence, tokens);
             }
         }
         
         if (!_doCorrection)
             correctedTokenizedSentences = tokenizedSentences;
         
         _spellChecker.removeSpellCheckListener(listener);
         
         String replacementRules = listener.getReplacementRules();
         console.fine("Replacement rules: " + replacementRules);
         
         Map<String,Set<String>> replacements = listener.getReplacements();
         
         componentContext.pushDataComponentToOutput(OUT_RULES, BasicDataTypesTools.stringToStrings(replacementRules));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, BasicDataTypesTools.mapToStringMap(getReplacementsMap(replacements)));
         componentContext.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.mapToStringMap(correctedTokenizedSentences));
     }
 
     private void processTokenCounts(Map<String, Integer> tokenCounts) throws ComponentContextException {
         SuggestionListener listener = new SuggestionListener(_doCorrection, console);
         _spellChecker.addSpellCheckListener(listener);
         
         Map<String, Integer> correctedTokenCounts = new HashMap<String, Integer>(tokenCounts.size());
         for (Entry<String,Integer> entry : tokenCounts.entrySet()) {
             StringWordTokenizer wordTokenizer = new StringWordTokenizer(entry.getKey());
             _spellChecker.checkSpelling(wordTokenizer);
             if (_doCorrection) {
                 String token = wordTokenizer.getContext();
                 Integer oldCount = correctedTokenCounts.containsKey(token) ? correctedTokenCounts.get(token) : 0;
                 correctedTokenCounts.put(token,  oldCount + entry.getValue());
             }
         }
         
         if (!_doCorrection)
             correctedTokenCounts = tokenCounts;
         
         _spellChecker.removeSpellCheckListener(listener);
         
         String replacementRules = listener.getReplacementRules();
         console.fine("Replacement rules: " + replacementRules);
         
         Map<String,Set<String>> replacements = listener.getReplacements();
         
         componentContext.pushDataComponentToOutput(OUT_RULES, BasicDataTypesTools.stringToStrings(replacementRules));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, BasicDataTypesTools.mapToStringMap(getReplacementsMap(replacements)));   
         componentContext.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.mapToIntegerMap(correctedTokenCounts, false));
     }
     
     private Map<String,String[]> getReplacementsMap(Map<String,Set<String>> map) {
         Map<String,String[]> result = new HashMap<String, String[]>(map.size());
         for (Entry<String,Set<String>> entry : map.entrySet()) {
             String[] arr = new String[entry.getValue().size()];
             result.put(entry.getKey(), entry.getValue().toArray(arr));
         }
         
         return result;
     }
     
     //--------------------------------------------------------------------------------------------
 
     public static class SuggestionListener implements SpellCheckListener {
 
         private final Map<String, Set<String>> _replacements;
         private final boolean _doCorrection;
         private final Logger _logger;
 
         public SuggestionListener(boolean doCorrection) {
             this(doCorrection, null);
         }
         
         public SuggestionListener(boolean doCorrection, Logger logger) {
             _replacements = new HashMap<String, Set<String>>();
             _doCorrection = doCorrection;
             _logger = logger;
         }
 
         public void spellingError(SpellCheckEvent event) {
             if (_logger != null) _logger.finer("Misspelling: " + event.getInvalidWord());
             List<?> suggestions = event.getSuggestions();
             
             if (!suggestions.isEmpty()) {
                 String topRankedSuggestion = suggestions.iterator().next().toString();
                 if (_logger != null) _logger.finer("Top suggestion: " + topRankedSuggestion);
                 
                 Set<String> misspellings = _replacements.get(topRankedSuggestion);
                 if (misspellings == null) {
                     misspellings = new HashSet<String>();
                     _replacements.put(topRankedSuggestion, misspellings);
                 }
                 misspellings.add(event.getInvalidWord());
                 
                 if (_doCorrection)
                     event.replaceWord(topRankedSuggestion, true);
                 else
                     event.ignoreWord(true);
             }
         }
         
         public String getReplacementRules() {
             StringBuilder sb = new StringBuilder();
             for (Entry<String, Set<String>> entry : _replacements.entrySet()) {
                 StringBuilder sbWord = new StringBuilder();
                 for (String word : entry.getValue())
                     sbWord.append(", ").append(word);
                 String rule = String.format("; %s = {%s}", entry.getKey(), sbWord.substring(2));
                 sb.append(rule);
             }
             
            return sb.substring(2);
         }
         
         public Map<String, Set<String>> getReplacements() {
             return _replacements;
         }
     }
 }
