 package logic;
 
 import java.lang.Thread;
 import music.ChordSymbol;
 import music.ChordType;
 import music.Piece;
 import music.KeySignature;
 import java.util.ArrayList;
 import util.*;
 import java.util.List;
 import java.util.ListIterator;
 
 public class Analyzer extends Thread {
 
 	Graph<ChordSymbol> _chordgraph;
 
 
 	public Analyzer() {
 		initMajorKeyGraph();
 	}
 
 	public void addChordSymbols(Piece p) {
 		// go through piece matching notes to chords
 
 	}
 
 	private void initMajorKeyGraph() {
 		_chordgraph = new Graph<ChordSymbol>();
 
 		// create chords
 		Node<ChordSymbol> chordI		= new Node<ChordSymbol>(new ChordSymbol(1, ChordType.MAJOR));
 		Node<ChordSymbol> chordii		= new Node<ChordSymbol>(new ChordSymbol(2, ChordType.MINOR));
 		Node<ChordSymbol> chordiii		= new Node<ChordSymbol>(new ChordSymbol(3, ChordType.MINOR));
 		Node<ChordSymbol> chordIV		= new Node<ChordSymbol>(new ChordSymbol(4, ChordType.MAJOR));
 		Node<ChordSymbol> chordV		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR));
 		Node<ChordSymbol> chordvi		= new Node<ChordSymbol>(new ChordSymbol(6, ChordType.MINOR));
 		Node<ChordSymbol> chordviio		= new Node<ChordSymbol>(new ChordSymbol(7, ChordType.DIMIN));
 
 		// V seven chords
 		Node<ChordSymbol> chordV7		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 0));
 		Node<ChordSymbol> chordV65		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 1));
 		Node<ChordSymbol> chordV43		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 2));
 		Node<ChordSymbol> chordV42		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 3));
 
 		Node<ChordSymbol> chordN		= new Node<ChordSymbol>(new ChordSymbol(2, ChordType.NEAPOLITAN));
 		Node<ChordSymbol> chordIt6		= new Node<ChordSymbol>(new ChordSymbol(6, ChordType.ITAUG6));
 		Node<ChordSymbol> chordFr6		= new Node<ChordSymbol>(new ChordSymbol(6, ChordType.FRAUG6));
 		Node<ChordSymbol> chordGer6		= new Node<ChordSymbol>(new ChordSymbol(6, ChordType.GERAUG6));
 
 		// I -> I ii III iv V VI VIio N It6 Fr6 Ger6
 		// ii -> V VIio N It6 Fr6 Ger6
 		// III -> VI iv
 		// iv -> I V VIio ii N It6 Fr6 Ger6
 		// V -> I VI
 		// VI -> ii iv
 		// VIi -> I V ii N It6 Fr6 Ger6
 
 		///Adding edges for chordI
 		_chordgraph.addEdge(chordI, chordii, 	1);
 		_chordgraph.addEdge(chordI, chordiii, 	1);
 		_chordgraph.addEdge(chordI, chordIV, 	1);
 		_chordgraph.addEdge(chordI, chordV, 	1);
 		_chordgraph.addEdge(chordI, chordvi, 	1);
 		_chordgraph.addEdge(chordI, chordviio, 	1);
 		_chordgraph.addEdge(chordI, chordN, 	2);
 		_chordgraph.addEdge(chordI, chordIt6, 	2);
 		_chordgraph.addEdge(chordI, chordFr6, 	2);
 		_chordgraph.addEdge(chordI, chordGer6, 	2);
 
 		//Adding edges for chordii
 		_chordgraph.addEdge(chordii, chordV, 	1);
 		_chordgraph.addEdge(chordii, chordviio,	2);
 		_chordgraph.addEdge(chordii, chordN, 	3);
 		_chordgraph.addEdge(chordii, chordIt6, 	3);
 		_chordgraph.addEdge(chordii, chordFr6, 	3);
 		_chordgraph.addEdge(chordii, chordGer6,	3);
 
 		//Adding edges for chordIII
 		_chordgraph.addEdge(chordiii, chordvi, 	1);
 		_chordgraph.addEdge(chordiii, chordIV, 	1);
 
 		//Adding edges for chordiv
 		_chordgraph.addEdge(chordIV, chordV, 	1);
 		_chordgraph.addEdge(chordIV, chordii, 	1);
 		_chordgraph.addEdge(chordIV, chordviio, 	1);
 		_chordgraph.addEdge(chordIV, chordI, 	2); //plagal cadence
 		_chordgraph.addEdge(chordIV, chordN, 	2);
 		_chordgraph.addEdge(chordIV, chordIt6, 	2);
 		_chordgraph.addEdge(chordIV, chordFr6, 	2);
 		_chordgraph.addEdge(chordIV, chordGer6,	2);
 
 		//Adding edges for chordV
 		_chordgraph.addEdge(chordV, chordI, 	1); //Authentic cadence
 		_chordgraph.addEdge(chordV, chordvi, 	2); //Deceptive cadence
 
 		//Adding edges for chordVI
 		_chordgraph.addEdge(chordvi, chordii, 	1);
 		_chordgraph.addEdge(chordvi, chordIV, 	1);
 
 		//Adding edges for chordVIio
 		_chordgraph.addEdge(chordviio, chordV, 	1);
 		_chordgraph.addEdge(chordviio, chordI, 	1); //Authentic cadence
 		_chordgraph.addEdge(chordviio, chordii,	1);
 		_chordgraph.addEdge(chordviio, chordN, 	2);
 
 	}
 
 	private void initMinorKeyGraph() {
 		_chordgraph = new Graph<ChordSymbol>();
 
 		// create chords
 		Node<ChordSymbol> chordi		= new Node<ChordSymbol>(new ChordSymbol(1, ChordType.MINOR));
 		Node<ChordSymbol> chordiio		= new Node<ChordSymbol>(new ChordSymbol(2, ChordType.DIMIN));
 		Node<ChordSymbol> chordIII		= new Node<ChordSymbol>(new ChordSymbol(3, ChordType.MAJOR));
 		Node<ChordSymbol> chordiv		= new Node<ChordSymbol>(new ChordSymbol(4, ChordType.MINOR));
 		Node<ChordSymbol> chordV		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR));
 		Node<ChordSymbol> chordVI		= new Node<ChordSymbol>(new ChordSymbol(6, ChordType.MAJOR));
 		Node<ChordSymbol> chordviio		= new Node<ChordSymbol>(new ChordSymbol(7, ChordType.DIMIN));
 
 		// V seven chords
 		Node<ChordSymbol> chordV7		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 0));
 		Node<ChordSymbol> chordV65		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 1));
 		Node<ChordSymbol> chordV43		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 2));
 		Node<ChordSymbol> chordV42		= new Node<ChordSymbol>(new ChordSymbol(5, ChordType.MAJOR7, 3));
 
 		Node<ChordSymbol> chordN		= new Node<ChordSymbol>(new ChordSymbol(0, ChordType.NEAPOLITAN));
 		Node<ChordSymbol> chordIt6		= new Node<ChordSymbol>(new ChordSymbol(0, ChordType.ITAUG6));
 		Node<ChordSymbol> chordFr6		= new Node<ChordSymbol>(new ChordSymbol(0, ChordType.FRAUG6));
 		Node<ChordSymbol> chordGer6		= new Node<ChordSymbol>(new ChordSymbol(0, ChordType.GERAUG6));
 
 		// i -> iio III iv V VI viio N It6 Fr6 Ger6
 		// iio -> V viio N It6 Fr6 Ger6
 		// III -> VI iv
 		// iv -> I V viio iio N It6 Fr6 Ger6
 		// V -> I VI
 		// VI -> iio iv
 		// VIi -> I V iio N It6 Fr6 Ger6
 
 		///Adding edges for chordi
 		_chordgraph.addEdge(chordi, chordiio, 	1);
 		_chordgraph.addEdge(chordi, chordIII, 	1);
 		_chordgraph.addEdge(chordi, chordiv, 	1);
 		_chordgraph.addEdge(chordi, chordV, 	1);
 		_chordgraph.addEdge(chordi, chordVI, 	1);
 		_chordgraph.addEdge(chordi, chordviio, 	1);
 		_chordgraph.addEdge(chordi, chordN, 	2);
 		_chordgraph.addEdge(chordi, chordIt6, 	2);
 		_chordgraph.addEdge(chordi, chordFr6, 	2);
 		_chordgraph.addEdge(chordi, chordGer6, 	2);
 
 		//Adding edges for chordiio
 		_chordgraph.addEdge(chordiio, chordV, 	1);
 		_chordgraph.addEdge(chordiio, chordviio,	2);
 		_chordgraph.addEdge(chordiio, chordN, 	3);
 		_chordgraph.addEdge(chordiio, chordIt6, 	3);
 		_chordgraph.addEdge(chordiio, chordFr6, 	3);
 		_chordgraph.addEdge(chordiio, chordGer6,	3);
 
 		//Adding edges for chordIII
 		_chordgraph.addEdge(chordIII, chordVI, 	1);
 		_chordgraph.addEdge(chordIII, chordiv, 	1);
 
 		//Adding edges for chordiv
 		_chordgraph.addEdge(chordiv, chordV, 	1);
 		_chordgraph.addEdge(chordiv, chordiio, 	1);
 		_chordgraph.addEdge(chordiv, chordviio,	1);
 		_chordgraph.addEdge(chordiv, chordi, 	2); //plagal cadence
 		_chordgraph.addEdge(chordiv, chordN, 	2);
 		_chordgraph.addEdge(chordiv, chordIt6, 	2);
 		_chordgraph.addEdge(chordiv, chordFr6, 	2);
 		_chordgraph.addEdge(chordiv, chordGer6,	2);
 
 		//Adding edges for chordV
 		_chordgraph.addEdge(chordV, chordi, 	1); //Authentic cadence
 		_chordgraph.addEdge(chordV, chordVI, 	2); //Deceptive cadence
 
 		//Adding edges for chordVI
 		_chordgraph.addEdge(chordVI, chordiio, 	1);
 		_chordgraph.addEdge(chordVI, chordi, 	1);
 
 		//Adding edges for chordviio
 		_chordgraph.addEdge(chordviio, chordV, 	1);
 		_chordgraph.addEdge(chordviio, chordi, 	1); //Authentic cadence
 		_chordgraph.addEdge(chordviio, chordiio,1);
 		_chordgraph.addEdge(chordviio, chordN, 	2);
 
 	}
 
 //	public void listPossibleChords(KeySignature key, ChordSymbol chord){
 //
 //
 //	}
 
 	/*
 	 *this function takes a pitch, and a key signature, so that it could compare the pitch with the chords within the _chordgraph,
 	 *and return a list of ChordSymbols which correspond to chords that contain that pitch.
 	 *
 	 */
 	public ArrayList<ChordSymbol> findMatchingChords(int pitch, KeySignature keysig) {
 
 		int halfStepsFromC = pitch % 12;
 		int key = keysig.halfStepsFromC();
 		int pitchDegree = halfStepsFromC - key; //pitch degree is the number of half steps above the tonic 0-11
 		if (pitchDegree < 0)
 			pitchDegree = pitchDegree + 12;
 		System.out.println("pitchDegree = " + pitchDegree);
 
 		if(keysig.getIsMajor())
 			initMajorKeyGraph();
 		else
 			initMinorKeyGraph();
 
 		ArrayList<ChordSymbol> matchingChords = new ArrayList<ChordSymbol>();
 		for(Node node : _chordgraph.getNodes()) {
 
 			ChordSymbol chordsym = (ChordSymbol) node.getValue();
 //			System.out.println("=========Current chord is: " + chordsym.getSymbolText() + "==========");
 
 			//Assigns the a pitch degree to the chord
 			int	chordNotePitchDegree;
 			if(chordsym.getChordType() == ChordType.NEAPOLITAN) {
 
 				chordNotePitchDegree = 1;
 			}
 			else if(chordsym.getChordType() == ChordType.ITAUG6 || chordsym.getChordType() == ChordType.FRAUG6 || chordsym.getChordType() == ChordType.GERAUG6) {
 
 				chordNotePitchDegree = 8;
 			}
 			else {
 
 				chordNotePitchDegree = scaleDegreeToPitchDegree(chordsym.getScaleDegree());
 			}
 
 			//checking if the root of the chord matces the given pitch
 			if(pitchDegree == chordNotePitchDegree) {
 
 				matchingChords.add(chordsym);
 			}
 			else {
 				//checking if the non-root notes of the chord match the given pitch
 				List<Integer> nonRootNotes = chordsym.getNonRootNotes();
 				for(int halfStepsFromRoot : nonRootNotes) {
 
 					int nonrootChordPitchDegree = ((chordNotePitchDegree + halfStepsFromRoot) % 12);
 //					System.out.println("nonrootChordPitchDegree = " + nonrootChordPitchDegree);
 					if(nonrootChordPitchDegree == pitchDegree) {
 
 						matchingChords.add(chordsym);
 						break;
 					}
 				}
 			}
 		}
 
 		return matchingChords;
 	}
 
 
 	/*
 	 * Takes a list of pitches and finds chord progressions that match that list of pitches. Each chord progression will be returned as a list of pitches.
 	 * The more commonly used chord progressions will be considered, depending on the weight of the edges that link the chords in the chord graph.
 	 * A list of best progressions will be returned, in the form of an ArrayList<Arraylist<ChordSymbol>>
 	 *
 	 */
 //	public ArrayList<ArrayList<ChordSymbol>> matchPitchesToChordProgressions(ArrayList<Integer> pitchList, KeySignature keysig) {
 //
 //		ArrayList<ArrayList<ChordSymbol>> allPossibleChords = new ArrayList<ArrayList<ChordSymbol>>();
 //		for(int pitch : pitchList) {
 //
 //			allPossibleChords.add(findMatchingChords(pitch, keysig));
 //		}
 //
 //		ArrayList<ArrayList<ChordSymbol>> matchingChordProgressions;
 ////		ListIterator pitchItr = pitchList.listIterator();
 //
 //		if(pitchList.size() != 0)
 //			matchingChordProgressions = matchPitchesToChordProgressionsHelper(pitchList, 0
 //
 //				, allPossibleChords, matchingChordProgressions);
 //
 //		return matchingChordProgressions;
 //	}
 
 	/*
 	 *Helper function for matchPitchesToChordProgressions() that implements recursion
 	 *
 	 */
 //	public ArrayList<ArrayList<ChordSymbol>> matchPitchesToChordProgressionsHelper(ListIterator<ArrayList<ChordSymbol>> allPossibleChordsItr, Node currentChord, ArrayList<ArrayList<ChordSymbol>> matchingChordProgressions) {
 //
 //		if(!allPossibleChordsItr.hasNext()) {
 //
 //
 //		}
 //		else {
 //
 //
 //		}
 //	}
 
 	/*
 	 * Takes a integer which indicates the scale degree of a note (1-7), and converts that to the pitch degree (0-11, one half step for each pitch degree).
 	 */
 	public int scaleDegreeToPitchDegree(int scaleDegree) {
 
 		if(scaleDegree < 1 || scaleDegree > 7){
 
 //			System.out.println("erroroneous scaleDegree input");
 			return 0;
 		}
 		else if(scaleDegree < 4)
 			return ((scaleDegree - 1) * 2);
 		else
 			return (5 + (scaleDegree - 4) *2);
 	}
 
 	public void run() {
 		// start analysis process here
 
 	}
 }
