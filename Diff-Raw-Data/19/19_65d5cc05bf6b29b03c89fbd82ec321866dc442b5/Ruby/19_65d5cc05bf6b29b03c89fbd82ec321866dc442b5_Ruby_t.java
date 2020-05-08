 package models;
 
 import java.util.List;
 import java.util.Stack;
 import java.util.regex.Pattern;
 
 import play.Logger;
 
 import com.google.common.collect.Lists;
 import com.ibm.icu.text.Transliterator;
 
 public class Ruby {
 
 	public String lemma;
 	public String pos;
 	public String inflection;
 	public String pronunciation;
 	
 	public List<RubyPart> rubyParts = Lists.newArrayList();
 
 	public Ruby(String surface, String pos, String inflection, String pronunciation) {
 		this.lemma = surface;
 		this.pos = pos;
 		this.inflection = inflection;
 		if (pronunciation != null) {
 			Transliterator tx = Transliterator.getInstance("Katakana-Hiragana");
 			this.pronunciation = tx.transliterate(pronunciation);
 
 			splitIntoParts(this.lemma, this.pronunciation);
 		}
 		
 	}
 
 	private void splitIntoParts(String surface, String pronunciation) {
 		
 //		String remainingSurface = surface;
 //		String remainingPronunciation = pronunciation;
 //		
 //		StringBuilder accumulatedPronunciation = new StringBuilder();
 //		for (int i=0; i<pronunciation.length(); i++) {
 //			
 //			String thisChar = String.valueOf(pronunciation.charAt(i));
 //			
 //			// find where this pronounciation character appears in surface
 //			String[] split = remainingSurface.split(thisChar);
 //			if (split.length==2) {
 //				String writtenBeforeThisChar = split[0];
 //				remainingSurface = split[1];
 //				
 //				if (accumulatedPronunciation.length()>0) {
 //					rubyParts.add(new RubyPart(accumulatedPronunciation.toString()));
 //					remainingPronunciation = remainingPronunciation.substring(accumulatedPronunciation.length(), remainingPronunciation.length());
 //					accumulatedPronunciation = new StringBuilder();
 //				}
 //				
 //				rubyParts.add(new RubyPart(writtenBeforeThisChar, pronunciation.substring(0,i)));
 //				accumulatedPronunciation.append(thisChar);
 //			} else if (split.length==1) {
 //				Logger.info("spl=1, tc=%s ap=%s rs=%s", thisChar, accumulatedPronunciation, remainingSurface);
 //			}
 //					
 //			if (((i+1)==pronunciation.length())) {
 //				if (remainingSurface.contains(thisChar)) {
 //					
 //					Logger.info("%s %s", remainingPronunciation, remainingSurface);
 //					
 //					accumulatedPronunciation.append(thisChar);
 //					rubyParts.add(new RubyPart(accumulatedPronunciation.toString()));
 //				} else {
 //					rubyParts.add(new RubyPart(accumulatedPronunciation.toString()));
 //					remainingPronunciation = remainingPronunciation.substring(accumulatedPronunciation.length(), remainingPronunciation.length());
 //					rubyParts.add(new RubyPart(remainingSurface, remainingPronunciation));
 //				}
 //			}
 //		}
 //		
 //		List<RubyPart> blanksRemovedRubyParts = Lists.newArrayList();
 //		for (RubyPart part : this.rubyParts) {
 //			if (!part.written.isEmpty()) {
 //				blanksRemovedRubyParts.add(part);
 //			}
 //		}
 //		this.rubyParts = blanksRemovedRubyParts;
 		
 		String remainingSurface = surface;
 		int lastPivotIndex = 0;
 		
 		for (int i=0; i<pronunciation.length(); i++) {
 			String thisChar = "" + pronunciation.charAt(i);
 			if (remainingSurface.contains(thisChar)) {
 				// pivot point
 				String surfaceUpToPivot = remainingSurface.substring(0, remainingSurface.indexOf(thisChar));
 				String prounciationUpToPivot = pronunciation.substring(lastPivotIndex, i);
 				
 				if ("".equals(prounciationUpToPivot) && "".equals(surfaceUpToPivot) && i==0) {
 					// first character
 					surfaceUpToPivot = pronunciation.substring(0,1);
 					prounciationUpToPivot = pronunciation.substring(0,1);
 					remainingSurface = remainingSurface.substring(1);
 					lastPivotIndex = 1;
 				} else {
 					remainingSurface = remainingSurface.substring(remainingSurface.indexOf(thisChar));
					if (surfaceUpToPivot.length() > 0) {
						lastPivotIndex = i;
					}
 				}
 				
 				Logger.info("%s matched with %s", surfaceUpToPivot, prounciationUpToPivot);
 				
 				rubyParts.add(new RubyPart(surfaceUpToPivot, prounciationUpToPivot));
 				
 			} else {
 				// not a pivot point
 			}
 		}
 		String remainingPronunciation = pronunciation.substring(lastPivotIndex);
 		rubyParts.add(new RubyPart(remainingSurface, remainingPronunciation));
 		
 		List<RubyPart> blanksRemovedRubyParts = Lists.newArrayList();
 		for (RubyPart part : this.rubyParts) {
 			if (!part.written.isEmpty()) {
 				blanksRemovedRubyParts.add(part);
 			}
 		}
 		this.rubyParts = blanksRemovedRubyParts;
 		
 		List<RubyPart> consecutiveNonFuriganaPartsMerged = Lists.newArrayList();
 		RubyPart lastNonFuriganaPart = null;
 		for (RubyPart part : this.rubyParts) {
 			if (part.isJustHiragana()) {
 				if (lastNonFuriganaPart!=null) {
 					lastNonFuriganaPart.mergeIn(part);
 				} else {
 					lastNonFuriganaPart = part;
 					consecutiveNonFuriganaPartsMerged.add(part);
 				}
 			} else {
 				lastNonFuriganaPart = null;
 				consecutiveNonFuriganaPartsMerged.add(part);
 			}
 		}
 		this.rubyParts = consecutiveNonFuriganaPartsMerged;
 	}
 
 	public String getFurigana() {
 		return "";
 	}
 
 	public String getPlainForm() {
 		return lemma;
 	}
 	
 	@Override
 	public String toString() {
 		return rubyParts.toString();
 	}
 
 }
