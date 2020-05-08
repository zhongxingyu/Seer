 package de.tu_berlin.textmining.translator.prototypes;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import de.tu_berlin.textmining.translator.prototypes.data.lexicon.Dictionary;
 import de.tu_berlin.textmining.translator.prototypes.data.lexicon.HashDictionary;
 import de.tu_berlin.textmining.translator.prototypes.models.BiGramModel;
 import de.tu_berlin.textmining.translator.prototypes.models.LanguageModel;
 import de.tu_berlin.textmining.translator.prototypes.reader.DictionaryFileReader;
 import de.tu_berlin.textmining.translator.prototypes.reader.ElcombriReader;
 
 public class Translator {
 
 	private static final String[] GERMAN_SENTENCES = {
 			"Über den Wolken, schwingen die Vögel ihre Flügel.",
 			"Zehn zahme Ziegen zogen zehn Zentner Zucker im Zwickauer Zoo",
 			"Franz jagt im komplett verwahrlosten Taxi quer durch Bayern.",
 			"Hamburg - Der Druck auf die Bundesregierung wächst, den künftigen europäischen Rettungsschirm ESM aufzustocken.",
 			"Nach Informationen des SPIEGEL wirbt Italiens Ministerpräsident Mario Monti dafür, das Finanzierungsvolumen des Rettungsschirms von 500 Milliarden Euro auf eine Billion Euro zu erhöhen.",
 			"Eine solche Maßnahme schaffe Vertrauen in die Währungsunion, was die Zinsen für Staatsanleihen sinken lasse, argumentiert Monti. In dieser Einschätzung wird er von Spanien und Portugal unterstützt.",
 			"Auch Frankreich plädiert dafür, dass Deutschland seine wirtschaftliche und finanzielle Stärke zugunsten des Euro in die Waagschale wirft.",
 			"Die Bundesregierung hat Monti von seinen Wünschen schon in Kenntnis gesetzt.",
 			"Rückendeckung bekommt der italienische Ministerpräsident auch von seinem Landsmann Mario Draghi, dem Präsidenten der Europäischen Zentralbank (EZB).",
 			"Dieser hat einen Kompromissvorschlag zur Diskussion gestellt.",
 			"Demnach sollen die unverbrauchten Mittel des vorläufigen Rettungsschirms EFSF nicht auf den ESM angerechnet werden.",
 			"Stattdessen soll der " + "dauerhafte Rettungsschirm die Restmittel zusätzlich zur Verfügung gestellt bekommen.",
 			"So ließe sich dessen Volu" + "men immerhin auf rund 750 Milliarden Euro aufstocken.",
 			"Die Bundesregierung verweist bisher dagegen eisern darauf, dass erst im März geprüft werde, ob der ESM mit ausreichendem Kapital ausgestattet ist.",
 			"So war es beim letzten EU-Gipfel vereinbart worden." };
 
 	private final Dictionary dict;
 	private final LanguageModel langModel;
 
 	public Translator(Dictionary dict, LanguageModel languageModel) {
 		this.dict = dict;
 		this.langModel = languageModel;
 	}
 
 	public String translateSentence(String sentence) {
 		StringBuilder strBld = new StringBuilder();
 		List<List<String>> translation = this.dict.translateSentence(sentence);
 		String lastWord = "<start>";
 		String candidate = "";
 
 		for (List<String> possibilities : translation) {
 			double maxProb = 0.0;
			boolean allEqual = true;
 			candidate = "";
 			for (String currentWord : possibilities) {
 				double prob = this.langModel.getBiGramProbability(lastWord, currentWord);
 				if (prob > maxProb) {
 					maxProb = prob;
					if (!candidate.equals("")) {
						allEqual = false;
					}
 					candidate = currentWord;
 				}
 			}
 			
			if (allEqual) {
 				for (String currentWord : possibilities) {
					maxProb = 0.0;
 					double prob = this.langModel.getUniGramProbability(currentWord);
 					if (prob > maxProb) {
 						maxProb = prob;
 						candidate = currentWord;
 					}
 				}
 			}
 
 			lastWord = candidate;
 			strBld.append(candidate);
 			strBld.append(" ");
 		}
 
 		return strBld.toString().trim() + "";
 	}
 
 	public static void main(String... args) throws UnsupportedEncodingException {
 		if (args.length < 2) {
 			System.out.println("Usage:\nTranslator [path to dict] [path to corpus]");
 			return;
 		}
 		String pathToDict = args[0];
 		String pathToCorpus = args[1];
 		DictionaryFileReader dictReader = null;
 		BufferedReader langReader = null;
 		Dictionary dict = null;
 		LanguageModel langModel = null;
 		try {
 			dictReader = new ElcombriReader(pathToDict);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		System.out.println("Loading dictionary...");
 		try {
 			dict = new HashDictionary(dictReader);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("DONE!");
 
 		try {
 			langReader = new BufferedReader(new FileReader(pathToCorpus));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		System.out.println("Loading language model...");
 		try {
 			langModel = new BiGramModel(langReader);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("DONE!");
 
 		Translator transe = new Translator(dict, langModel);
		for (String sentence : GERMAN_SENTENCES) {
 			System.out.println(transe.translateSentence(sentence));
		}
 	}
 }
