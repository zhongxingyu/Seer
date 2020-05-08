 package com.example.solfege;
 
 import java.lang.reflect.Array;
 import java.util.Random;
 import java.util.Vector;
 import android.webkit.JavascriptInterface;
 
 public class MainGauche {
 
 	private int renversement;
 	private int hauteur;
 	private int degre;
 	private int veloout;
 	private String accord_name;
 	private int[] accord;
 	private int tempo;
 	private int dureeout;
 	private String mode;
 	private int random;
 
 	public MainGauche() {
 		// TODO Auto-generated constructor stub
 		renversement = 0;
 		hauteur = 48;
 		degre = 1;
 		veloout = 60;
 		tempo = 100;
 		dureeout = 50;
 		random = 0;
 		mode = new String("maj");
 		accord_name = new String("maj");
 		accord = new int[3];
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@android.webkit.JavascriptInterface
 	public int test() {
 		return 123;
 	}
 
 	/**
 	 * @param args
 	 */
 	@android.webkit.JavascriptInterface
 	public String genereAccordMidi(String modein, int degrein) {
 
 		set_mode(modein);
 		Vector reponse = gauche(degrein);
 		String retour = reponse.toString();
 		return retour;
 	}
 	
 	/**
	 * Generates a string of 3 notes representing a chord in abc notation, e.g., "a b c"
 	 * @param modein either "maj" or "min"--> should be bool then?
 	 * @param degrein degree of the chord, valid values are [???,???]
 	 * @return the 3 notes of a chord in abc notation (http://en.wikipedia.org/wiki/ABC_notation)
 	 */
 	public String genereAccordAbc(String modein, int degrein) {
 
 		set_mode(modein);
 		Vector chord = gauche(degrein);
 		String strChord = chord.toString();
 
 		//get numbers from string in ints
 		String[] strChordMidiNotes =  strChord.substring(1, strChord.length()-1).split(",");
 		
 		String strAbcChord = "";
 		//use midiToAbc() to get the corresponding letters
 		for (int iCurNote = 0; iCurNote < strChordMidiNotes.length; ++iCurNote){
 			try {
 				strAbcChord += midiToAbc(Integer.parseInt(strChordMidiNotes[iCurNote].trim())) + " ";
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		//put them in a string like "a b c"
 		return strAbcChord.trim();
 		
 	}
 
 	public Vector gauche(int n) {
 
 		Vector temp = new Vector();
 
 		if (mode.equals("maj")) {
 
 			switch (n) {
 			case 1:
 				accord_name = "maj";
 				degre = 0;
 				break;
 			case 2:
 				accord_name = "min";
 				degre = 1;
 				break;
 			case 3:
 				accord_name = "min";
 				degre = 2;
 				break;
 			case 4:
 				accord_name = "maj";
 				degre = 3;
 				break;
 			case 5:
 				accord_name = "maj";
 				degre = 4;
 				break;
 			case 6:
 				accord_name = "min";
 				degre = 5;
 				break;
 			case 7:
 				accord_name = "dim";
 				degre = 6;
 				break;
 			}// switch
 			setaccord();
 			temp.add(accord[0]);
 			temp.add(accord[1]);
 			temp.add(accord[2]);
 
 		}// Maj
 		if (mode.equals("min")) {
 			switch (n) {
 			case 1:
 				accord_name = "min";
 				degre = 0;
 				break;
 			case 2:
 				accord_name = "dim";
 				degre = 1;
 				break;
 			case 3:
 				accord_name = "maj";
 				degre = 2;
 				break;
 			case 4:
 				accord_name = "min";
 				degre = 3;
 				break;
 			case 5:
 				accord_name = "maj";
 				degre = 4;
 				break;
 			case 6:
 				accord_name = "maj";
 				degre = 5;
 				break;
 			case 7:
 				accord_name = "maj";
 				degre = 6;
 				break;
 			}// switch
 			setaccord();
 			temp.add(accord[0]);
 			temp.add(accord[1]);
 			temp.add(accord[2]);
 		} // min
 
 		return temp;
 
 	}
 	
     private static final String[] notes= { "c", "c#", "d", "d#","e","f","f#","g","g#","a", "a#", "b","b#" }; // find the octave number
 	
 	private int abcToMidi(String note, int octave) {
 		int i;
 		for (i = 0; i < notes.length; i++){
 			if (note.equals(notes[i])){
 				return 12 * (octave + 1) + i;
 			}
 		}
 		return -1;
 	}
 
 	private String midiToAbc(int p_iMidiNote) {
 		return notes[p_iMidiNote % 12];
 	}
 	
 	public void setaccord() {
 
 		Random rnd = new Random(3);
 		if (random == -1) {
 			renversement = rnd.nextInt(3);
 		} else {
 			renversement = random;
 		}
 
 		if (accord_name.equals("maj")) {
 			switch (renversement) {
 			case 0:
 				accord[0] = 0;
 				accord[1] = 4;
 				accord[2] = 7;
 				break;
 			case 1:
 				accord[0] = 4;
 				accord[1] = 7;
 				accord[2] = 12;
 				break;
 			case 2:
 				accord[0] = 7;
 				accord[1] = 12;
 				accord[2] = 16;
 				break;
 			}
 		} else if (accord_name.equals("min")) {
 			switch (renversement) {
 			case 0:
 				accord[0] = 0;
 				accord[1] = 3;
 				accord[2] = 7;
 				break;
 			case 1:
 				accord[0] = 3;
 				accord[1] = 7;
 				accord[2] = 12;
 				break;
 			case 2:
 				accord[0] = 7;
 				accord[1] = 12;
 				accord[2] = 15;
 				break;
 			}
 		} else if (accord_name.equals("dim")) {
 			switch (renversement) {
 			case 0:
 				accord[0] = 0;
 				accord[1] = 3;
 				accord[2] = 6;
 				break;
 			case 1:
 				accord[0] = 3;
 				accord[1] = 6;
 				accord[2] = 12;
 				break;
 			case 2:
 				accord[0] = 6;
 				accord[1] = 12;
 				accord[2] = 15;
 				break;
 			}
 		} else if (accord_name.equals("aug")) {
 			switch (renversement) {
 			case 0:
 				accord[0] = 0;
 				accord[1] = 4;
 				accord[2] = 8;
 				break;
 			case 1:
 				accord[0] = 4;
 				accord[1] = 8;
 				accord[2] = 12;
 				break;
 			case 2:
 				accord[0] = 8;
 				accord[1] = 12;
 				accord[2] = 16;
 				break;
 			}
 		}
 		accord[0] = degre2midi(degre) + (hauteur + accord[0]);
 		accord[1] = degre2midi(degre) + (hauteur + accord[1]);
 		accord[2] = degre2midi(degre) + (hauteur + accord[2]);
 
 	}
 
 	int degre2midi(int degre) {
 
 		if (mode.equals("maj")) {
 			switch (degre) {
 			case 0:
 				return 0;
 			case 1:
 				return 2;
 			case 2:
 				return 4;
 			case 3:
 				return 5;
 			case 4:
 				return 7;
 			case 5:
 				return 9;
 			case 6:
 				return 11;
 			}// switch
 		}// Majeur
 
 		else {
 			switch (degre) {
 			case 0:
 				return 0;
 			case 1:
 				return 2;
 			case 2:
 				return 3;
 			case 3:
 				return 5;
 			case 4:
 				return 7;
 			case 5:
 				return 8;
 			case 6:
 				return 10;
 			}// switch
 		}// else (Mineur)
 		return 0;
 	}
 
 	private void set_mode(String modein) {
 
 		mode = modein;
 	}
 
 }// MainGauche
