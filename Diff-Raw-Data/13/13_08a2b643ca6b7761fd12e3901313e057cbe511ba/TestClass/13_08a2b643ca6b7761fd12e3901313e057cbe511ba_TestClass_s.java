 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import com.diwan.translation.*;
 
 public class TestClass {
 
 	public static void main(String[] args) throws TranslateFault {
 
 		String AppId = "6C9A92CF0DDDEF484F4C4ECEA2C82D8CE591A2AD";
 		String[] textsArray = { "I want this translated", "to something",
 				"in another language" };
 
 		try {
 
			Translate t = new Translate(AppId, "text/plain", "general",
					"username", null);
 			String text = "Translate this text into German";
 			t.init();
 
 			// t.addTranslation("translate", "¸bersetzen", "en", "de", 1);
 			// t.addTranslationArray(translations, "en", "de", options);
 
 			// Break Sentences
 			String breaksentence = "Please break up this string into sentences! I would like this string to be broken into its respective sentences. Is this possible?";
 			System.out.println("The text is: " + breaksentence);
 			System.out
 					.println("BreakSentences broke up the above sentence into "
 							+ t.breakSentences(breaksentence, "en").length
 							+ " sentences.");
 
 			// Detect Language
 			String detect = t
 					.detect("I have no idea what this language may be");
 			System.out.println("The detected language friendly code is: "
 					+ detect);
 
 			// Detect Language Array
 			String[] detectArray = t.detectArray(textsArray);
 			for (int i = 0; i < detectArray.length; i++) {
 				System.out.println("The detected languages are: "
 						+ detectArray[i]);
 			}
 
 			// App ID Token
 			System.out.println("Your tokenized AppId is: "
 					+ t.getAppIdToken(5, 4, 300));
 
 			// Language Names
 			String[] codeString = { "de", "en", "fr", "ko" };
 			String languageNames[] = t.getLanguagesNames("en", codeString);
 			for (int i = 0; i < languageNames.length; i++) {
 				System.out.println("Language Name for " + codeString[i]
 						+ " is " + languageNames[i]);
 			}
 
 			// Speak Languages
 			String[] speakLanguages = t.getLanguageForSpeak();
 			for (int i = 0; i < speakLanguages.length; i++) {
 				System.out.println("The languages available are:"
 						+ speakLanguages[i]);
 			}
 
 			// Translate Languages
 			String[] translateLanguages = t.getLanguagesForTranslate();
 			System.out.println("The languages available for translation are: ");
 			for (int i = 0; i < translateLanguages.length; i++) {
 				System.out.println(translateLanguages[i]);
 			}
 
 			// Translations
 			String[] translations = t.getTranslations(
 					"To change this template, choose Tools", "en", "de", 5);
 			for (int i = 0; i < translations.length; i++) {
 				System.out.println("The matches are :" + translations[i]);
 			}
 			// Translation Array
 
 			String[] translationarray = t.getTranslationsArray(textsArray,
 					"en", "fr", 5);
 			for (int i = 0; i < translationarray.length; i++) {
 				System.out.println("The array of matches : "
 						+ translationarray[i]);
 			}
 
 			/*
 			 * System.out.println("The translation array matches:" +
 			 * t.getTranslationsArray(textsArray, "en", "fr")[0]);
 			 */
 			// Speak
 			System.out.println("Speak Method : "
 					+ t.speak("je suis pianiste", "fr", "audio/wav"));
 			// Translate line
 			try {
 
 				BufferedReader in = new BufferedReader(new FileReader("in.txt"));
 				BufferedWriter out = new BufferedWriter(new FileWriter(
 						"out.txt"));
 				String str;
 				out.write(0xfeff);
 				System.out.println("The translated lines are:");
 				while ((str = in.readLine()) != null) {
 					str = t.translateLine(str, "en", "ar");
 					System.out.println(str);
 					out.write(str+"\r");
 				}
 
 				out.close();
 				in.close();
 			} catch (IOException e) {
 			}
 
 			// Array Translation
 			for (int i = 0; i < textsArray.length; i++) {
 				System.out.println("Array Translation: "
 						+ t.translateArray(textsArray, "en", "fr")[i]
 								.getTranslatedText());
 			}
 
 		}
 
 		catch (TranslateFault e) {
 			e.printStackTrace();
 		}
 	}
 
 }
