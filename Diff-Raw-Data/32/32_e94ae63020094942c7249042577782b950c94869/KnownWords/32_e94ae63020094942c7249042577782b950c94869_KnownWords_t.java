 /**
  * Copyright 2011 Richard North
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.yomiassist.sourcedata;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.yomiassist.Options;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 import com.google.common.io.LineProcessor;
 
 /**-
  * Holds words which should be considered 'known' by the target audience of
  * the ebook being produced by yomiassist.
  * 
  * Word list should be in format: Kanji-Hiragana e.g.
  * 
  * <pre>
  * {@code
  *  -あ
  *  -ああ
  *  -あいさつ
  *  間-あいだ
  *  合う-あう
  *  }
  * </pre>
  * 
  * @author Richard North <rich.north+yomiassist@gmail.com>
  * 
  */
 public class KnownWords {
 
 	private Set<String> knownWords;
 
 	public KnownWords(Options options) throws IOException {
 		File sourceFile = options.vocabularySourceFile;
 		knownWords = Files.readLines(sourceFile, Charsets.UTF_8, new LineProcessor<Set<String>>() {
 
 			Set<String> words = new HashSet<String>();
 
 			public boolean processLine(String line) throws IOException {
 				final boolean kanaOnly = line.indexOf('-') == 0;
 				if (kanaOnly) {
 					words.add(line.substring(1));
 				} else {
 					final String kanjiPortion = line.substring(0, line.indexOf('-'));
 					String[] kanjiVariants = kanjiPortion.split("\\s");
 					for (String variant : kanjiVariants) {
						words.add(variant);
 					}
 				}
 				return true;
 			}
 
 			public Set<String> getResult() {
 				return words;
 			}
 		});
 	}
 
 	public Set<String> getKnownWords() {
 		return knownWords;
 	}
 
 }
