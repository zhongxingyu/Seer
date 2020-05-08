 package com;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import com.cybozu.labs.langdetect.Detector;
 import com.cybozu.labs.langdetect.DetectorFactory;
 import com.cybozu.labs.langdetect.LangDetectException;
 
 public class TextAnalysis {
 	private String contents;
	// TODO: check this patch
 	private final String langdetectProfileDirectory = "profiles/";
 	private Map<String, Integer> wordCounts = new HashMap<String, Integer>();
	
 	private static Boolean langFactoryInitialized = false;
	
 	private HashMap<String, String> languageNames = new HashMap<String, String>();
 	
 	
 	// saved results
 	private float gunningFogIndex = 0;
 	private int numberOfSigns = 0;
 	private int numberOfWords = 0;
 	private int numberOfSentences = 0;
 	private int maxSentenceLength = 0;
 	private int minSentenceLength;
 	private int numberOfSignsWtSpaces = 0;
 	
 	
 	TextAnalysis(String c) {
 		setUpLanguageMap();
 		
 		if(langFactoryInitialized == false)
 		{
 			try {
 				langFactoryInitialized = true;
 				DetectorFactory.loadProfile(langdetectProfileDirectory);
 			} catch (LangDetectException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		contents = c;
 		numberOfSigns = contents.length()- 1; // najwidoczniej dodaje 1 znak na
 												// koncu
 		/*for (int i = 0; i < contents.length(); i++) {
 			System.out.println(i + ": " + contents.charAt(i));
 		}*/
 		numberOfWords = countWords();
 		minSentenceLength = numberOfSigns;
 		countSentences();
 		getWordOccurances();
 		gunningFogIndex = (float) (0.4 * (numberOfWords / numberOfSentences));
 		// TODO: + 100 (complex words / words)
 		//l = detectLanguage();
 	}
 
 	public void setUpLanguageMap()
 	{
 		languageNames.put("af", "Afrikaans");
 		languageNames.put("ar", "Arabic");
 		languageNames.put("bg", "Bulgarian");
 		languageNames.put("bn", "Bengali");
 		languageNames.put("cs", "Czech");
 		languageNames.put("da", "Danish");
 		languageNames.put("de", "German");
 		languageNames.put("el", "Greek");
 		languageNames.put("en", "English");
 		languageNames.put("es", "Spanish");
 		languageNames.put("et", "Estonian");
 		languageNames.put("fa", "Persian");
 		languageNames.put("fi", "Finnish");
 		languageNames.put("fr", "French");
 		languageNames.put("gu", "Gujarati");
 		languageNames.put("he", "Hebrew");
 		languageNames.put("hi", "Hindi");
 		languageNames.put("hr", "Croatian");
 		languageNames.put("hu", "Hungarian");
 		languageNames.put("id", "Indonesian");
 		languageNames.put("it", "Italian");
 		languageNames.put("ja", "Japanese");
 		languageNames.put("kn", "Kannada");
 		languageNames.put("ko", "Korean");
 		languageNames.put("lt", "Lithuanian");
 		languageNames.put("lv", "Latvian");
 		languageNames.put("mk", "Macedonian");
 		languageNames.put("ml", "Malayalam");
 		languageNames.put("mr", "Marathi");
 		languageNames.put("ne", "Nepali");
 		languageNames.put("nl", "Dutch");
 		languageNames.put("no", "Norwegian");
 		languageNames.put("pa", "Punjabi");
 		languageNames.put("pl", "Polish");
 		languageNames.put("pt", "Portuguese");
 		languageNames.put("ro", "Romanian");
 		languageNames.put("ru", "Russian");
 		languageNames.put("sk", "Slovak");
 		languageNames.put("sl", "Slovene");
 		languageNames.put("so", "Somali");
 		languageNames.put("sq", "Albanian");
 		languageNames.put("sv", "Swedish");
 		languageNames.put("sw", "Swahili");
 		languageNames.put("ta", "Tamil");
 		languageNames.put("te", "Telugu");
 		languageNames.put("th", "Thai");
 		languageNames.put("tl", "Tagalog");
 		languageNames.put("tr", "Turkish");
 		languageNames.put("uk", "Ukrainian");
 		languageNames.put("ur", "Urdu");
 		languageNames.put("vi", "Vietnamese");
 		languageNames.put("zh-cn", "Simplified Chinese");
 		languageNames.put("zh-tw", "Traditional Chinese");
 	}
 	
 	public float getGunningFogIndex() {
 		return gunningFogIndex;
 	}
 
 	public Integer getNumberOfSigns() {
 		return numberOfSigns;
 	}
 
 	public Integer getNumberOfWords() {
 		return numberOfWords;
 	}
 
 	public Integer getNumberOfSentences() {
 		return numberOfSentences;
 	}
 
 	public Integer getMinSentenceLength() {
 		return minSentenceLength;
 	}
 
 	public Integer getMaxSentenceLength() {
 		return maxSentenceLength;
 	}
 
 	public Integer getNumberOfSignsWtSpaces() {
 		return numberOfSignsWtSpaces;
 	}
 
 	public Map<String, Integer> getWordCounts() {
 		return wordCounts;
 	}
 
 	private int countWords() {
 		Scanner scanner = new Scanner(contents);
 		int count = 0;
 		while (scanner.hasNext()) {
 			String s = scanner.next();
 			count++;
 		}
 		scanner.close();
 		return count;
 	}
 
 	private void countSentences() {
 		int length = 0;
 		int spaces = 0;
 		char a;
 		boolean dots = false;
 		for (int i = 0; i < contents.length(); i++) {
 			a = contents.charAt(i);
 			if (a == ' ')
 				spaces++;
 			if (((a == '.') || (a == '') || (a == '!') || (a == '?'))
 					&& (!dots)) {
 				numberOfSentences++;
 				length++;
 				dots = true;
 				if (length < minSentenceLength)
 					minSentenceLength = length;
 				if (length > maxSentenceLength)
 					maxSentenceLength = length;
 				length = 0;
 			} else {
 				if (!dots)
 					length++;
 				dots = false;
 			}
 		}
 		numberOfSignsWtSpaces = contents.length() - spaces;
 	}
 
 	private void getWordOccurances() {
 		String[] words = contents.toLowerCase().split("\\s+");
 
 		for (String word : words) {
 			Integer count = wordCounts.get(word);
 			if (count == null) {
 				count = 0;
 			}
 			wordCounts.put(word, count + 1);
 		}
 	}
 
 	public String detectLanguage() {
 		String lang;
 		try {
 			Detector detector = DetectorFactory.create();
 			lang = detector.detect();
 			lang = languageNames.get(lang);
 		} catch (LangDetectException e) {
 			// TODO We need a logging system!
 			lang = e.getMessage();
 		}
 		return lang;
 	}
 }
