 package synthseq.musictheory;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Rules {
 
 	private static byte[] noteSeq = { 9, 11, 0, 2, 4, 5, 7};
 	private static String[] invSeq = {"C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"};
 
 	public static ArrayList<ArrayList<Double>> stringToFreqs(String string) {
 		ArrayList<ArrayList<Double>> v = new ArrayList<ArrayList<Double>>();
 		Scanner scn = new Scanner(string);
 		while (scn.hasNext()) {
 			ArrayList<Double> notes = new ArrayList<Double>();
 			String str = scn.next();
 			boolean contained = false;
 			for (String s : Chords.getChords()) {
 				if (str.contains(s)) {
 					contained = true;
 					break;
 				}
 			}
 			if (!contained) {
 				int i;
 				if ((i = stringToMidi(str)) != -1)
 					notes.add(midiToFreq(i));
 			} else {
 				String chord = str.replaceAll("[a-gA-G][s#b]?-?[0-9]?", "");
 				String baseNote = str.replace(chord, "");
 				for(int i : Chords.getChord(stringToMidi(baseNote),chord))
 					notes.add(midiToFreq(i));
 			}
 			v.add(notes);
 		}
 		return v;
 	}
 
 	public static String midiToString(int i) {
 		return invSeq[Math.abs(i%12)] + (i / 12 - 1);
 	}
 	
 	public static int freqToMidi(double f){
 		return (int) (12*(Math.log(f/440)/Math.log(2))+69);
 	};
 
 	public static int stringToMidi(String str) {
 		if (!str.matches("[a-gA-G][s#b]?-?[0-9]?"))
 			return -1;
 		int baseNote = noteSeq[(Character.toUpperCase(str.charAt(0)) - 'A')] + 60;
 		str = str.substring(1);
 		if (str.matches("[s#b]-?[0-9]?")) {
 			if (str.charAt(0) == 's' || str.charAt(0) == '#')
 				baseNote++;
 			else if (str.charAt(0) == 'b')
 				baseNote--;
 			str = str.replaceAll("[s#b]","");
 		}
 		if (str.matches("-?[0-9]"))
 			baseNote += 12 * (Integer.parseInt(str) - (baseNote>7?4:3));
 		return baseNote;
 	}
 
 	public static double midiToFreq(int note) {
 		return 440 * Math.pow(2, (note - 69) / 12.0);
 	}
 
 }
