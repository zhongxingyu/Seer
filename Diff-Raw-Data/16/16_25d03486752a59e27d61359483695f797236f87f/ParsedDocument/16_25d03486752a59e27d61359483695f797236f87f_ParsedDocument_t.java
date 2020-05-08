 package documentParsing;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import application.Parameters;
 
 import keywordExctraction.FrequencyObject;
 import keywordExctraction.Keyphrase;
 import keywordExctraction.Pos;
 
 public class ParsedDocument implements FrequencyObject {
 
 	private Document doc;
 	private Map<Keyphrase, Integer> frequencyPerKeyphrases;
 	private Set<Keyphrase> extractedKeyphrases;
 	private Integer nounsNumber, maxFreq; // per efficienza, così si possono
 											// calcolare una volta sola
 
 	public ParsedDocument(Document document, Map<Keyphrase, Integer> frequencyPerKeyphrase) {
 		super();
 		this.doc = document;
 		this.frequencyPerKeyphrases = new HashMap<>(frequencyPerKeyphrase);
 		this.nounsNumber = null;
 		this.maxFreq = null;
 
 		this.extractedKeyphrases = new HashSet<>();
 		for (Keyphrase k : frequencyPerKeyphrase.keySet()) {
 			if (frequencyPerKeyphrase.get(k) >= Parameters.keywordFrequencyThreshold) {
 				extractedKeyphrases.add(k);
 			}
 		}
 	}
 
 	public Document getDoc() {
 		return doc;
 	}
 
 	public Set<Keyphrase> getKeyphrases() {
 		return new HashSet<>(frequencyPerKeyphrases.keySet());
 	}
 
 	public Set<Keyphrase> getExtractedKeyphrases() {
 		return extractedKeyphrases;
 	}
 
 	// questo metodo sfrutta il fatto che 2 keyphrases siano uguali (equals e
 	// hascode) in phrase e pos
 	public void tag(Set<Keyphrase> taggedKeyphrase) {
 
 		for (Keyphrase k : taggedKeyphrase) {
 
 			int frequency = frequencyPerKeyphrases.get(k);
 
 			frequencyPerKeyphrases.put(k, frequency);
 
 		}
 
 	}
 
 	public int getFrequency(Keyphrase k) {
 		int f = 0;
 		if (frequencyPerKeyphrases.containsKey(k)) {
 			f = frequencyPerKeyphrases.get(k);
 		}
 		return f;
 	}
 
 	@Override
 	public int getLength() {
 		if (nounsNumber == null) {
 			nounsNumber = 0;
 			for (Keyphrase k : frequencyPerKeyphrases.keySet()) {
 				if (k.getPos().equals(Pos.NOUN)) {
 					nounsNumber++;
 				}
 			}
 		}
 		return nounsNumber;
 	}
 
 	@Override
 	public int getMaxFrequency() {
 		if (maxFreq == null) {
 			maxFreq = 0;
 			if (!frequencyPerKeyphrases.isEmpty()) {
 				maxFreq = Collections.max(frequencyPerKeyphrases.values());
 			}
 		}
 		return maxFreq;
 	}
 	
 	//TODO eliminami
 	@Override
 	public String toString() {
		return doc.getUrl() + "\n";
 	}
 }
