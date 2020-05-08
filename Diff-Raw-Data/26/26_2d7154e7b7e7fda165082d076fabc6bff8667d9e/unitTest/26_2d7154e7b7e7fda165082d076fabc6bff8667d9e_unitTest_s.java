 package tests;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import music.*;
 import logic.Analyzer;
 import logic.Analyzer3;
 import tests.MidiAPITest;
 import util.Edge;
 import util.Graph;
 import util.Node;
 
 public class unitTest{
         public static void main(String args[]) {
         	
                 KeySignature CMajor = new KeySignature(new Rational(4, 4), 0, true);
                 KeySignature DMajor = new KeySignature(new Rational(4, 4), 2, true);
                 Pitch C = MidiAPITest.createPitchFromMidiPitch(60);
                 Pitch D = MidiAPITest.createPitchFromMidiPitch(62);
                 Pitch E = MidiAPITest.createPitchFromMidiPitch(64);
                 Pitch F = MidiAPITest.createPitchFromMidiPitch(65);
                 Pitch G = MidiAPITest.createPitchFromMidiPitch(67);
                 Pitch Gs = MidiAPITest.createPitchFromMidiPitch(68);
                 Pitch A = MidiAPITest.createPitchFromMidiPitch(69);
                 Pitch B = MidiAPITest.createPitchFromMidiPitch(71);
                 Pitch C5 = MidiAPITest.createPitchFromMidiPitch(72);
                 
                 Analyzer analyzer = new Analyzer();
                 List<List<Pitch>> melody = new ArrayList<List<Pitch>>();
                 
                 ArrayList<Pitch> melodyInstance1 = new ArrayList<Pitch>();
                melodyInstance1.add(C5);
//                melodyInstance1.add(E);
//                melodyInstance1.add(C);
                 melody.add(melodyInstance1);
                 
                 ArrayList<Pitch> melodyInstance2 = new ArrayList<Pitch>();
                 melodyInstance2.add(B);
                 melody.add(melodyInstance2);
                 
                 ArrayList<Pitch> melodyInstance3 = new ArrayList<Pitch>();
                 melodyInstance3.add(A);
 //                melodyInstance3.add(D);
                 melody.add(melodyInstance3);
                 
                 ArrayList<Pitch> melodyInstance4 = new ArrayList<Pitch>();
 //                melodyInstance4.add(C);
 //                melodyInstance4.add(E);
                 melodyInstance4.add(G);
                 melody.add(melodyInstance4);
                 
                 List<List<ChordSymbol>> allPossibleChords = analyzer.analyzeMelody(melody, CMajor);
                 
                 
 //                //testing testing
 //
 //            	ChordSymbol c1 = new ChordSymbol(new ScaleDegree(1, Accidental.NATURAL), ChordType.MAJOR);
 //            	ChordSymbol c2 = new ChordSymbol(new ScaleDegree(6, Accidental.NATURAL), ChordType.GERAUG6);
 //            	System.out.println(c1.equals(c2));
 //            	
 //            	System.out.println(analyzer.getChordPreferencesGraph().findNode(c1).getValue());
 //            	//testing testing
                 
 //                System.out.println("ChordProgressions Graph contains the following ChordSymbols: ");
 //                for(Node<ChordSymbol> toPrint : analyzer.getChordPreferencesGraph().getNodes()) {
 //                	
 //                	System.out.println(toPrint.getValue().getSymbolText());
 //                }
 //                System.out.println("graph is " + analyzer.getChordPreferencesGraph());
                 
                 //print out all possible chords
                 int melodyNo = 0;
                 for(List<ChordSymbol> chordInstance : allPossibleChords) {
                 	
                 	System.out.println("=====melodyInstance " + melodyNo + "=====");
                 	System.out.println("existing pitches are: ");
                 	List<Pitch> melodyInstance = melody.get(melodyNo);
                 	
                 	for(Pitch pitch : melodyInstance) {
                 		
                 		System.out.println(pitch);
                 	}
                 	
                 	System.out.println("chords that contain these pitches are:");
                 	for(ChordSymbol chordsym : chordInstance) {
                 		
                 		System.out.println(chordsym.getSymbolText());
                 	}
                 	
                 	melodyNo++;
                 }
                 System.out.println("");
                 
                 boolean onlyOptimalProgressions = false;
                 System.out.println("Only get optimal progressions: " + onlyOptimalProgressions);
                 Graph<ChordSymbol> possibleProgressionsGraph = analyzer.createPossibleProgressionsGraph(allPossibleChords);
                 System.out.println("Possible chord progressions: ");
                 printGraph(possibleProgressionsGraph);
                 
                 //testing getChordsAtIndex()
                 int index = 2;
                 List<Node<ChordSymbol>> NodesAtIndex = analyzer.getChordsAtIndex(possibleProgressionsGraph, index);
                 System.out.println("Chords at index " + index + " are:");
                 if(NodesAtIndex != null) {
                 	for(Node<ChordSymbol> node : NodesAtIndex) {
 	                	ChordSymbol chordsym = node.getValue();
 	                	System.out.println(printChordSymbol(chordsym));
 	                }
                 }
                 
                 //testing setChordsAtIndex()
                 ChordSymbol chordI = new ChordSymbol(new ScaleDegree(1, Accidental.NATURAL), ChordType.MAJOR); 
                 ChordSymbol chordiii = new ChordSymbol(new ScaleDegree(3, Accidental.NATURAL), ChordType.MINOR);
                 ChordSymbol chordV7 = new ChordSymbol(new ScaleDegree(5, Accidental.NATURAL), ChordType.MAJORMINOR7);
                 ChordSymbol chordvi = new ChordSymbol(new ScaleDegree(6, Accidental.NATURAL), ChordType.MINOR);
                 
                 ChordSymbol toTest = chordiii;
                 System.out.println("After setting index " + index + " to chord " + printChordSymbol(toTest));
                 printGraph(analyzer.setChordsAtIndex(toTest, possibleProgressionsGraph, index));
                 
 //              
                 // testing harmonizeMelodyInstance
 //                List<Pitch> melodyInstance = new ArrayList<Pitch>();
                 ChordSymbol harmonizeChord = chordvi;
                 KeySignature chosenKeySig = CMajor;
                 List<Pitch> chosenMelodyInstance = melodyInstance1;
                 List<Pitch> harmonizedMelody = new Analyzer().harmonizeMelodyInstance(chosenMelodyInstance, harmonizeChord, chosenKeySig);
                 System.out.println("harmonizing:");
                 for(Pitch pitch : chosenMelodyInstance) {
                 	System.out.println(pitch);
                 }
                 System.out.println("with chord: " + printChordSymbol(harmonizeChord) + " in key: " + chosenKeySig.getKeySigPitch());
                 for(Pitch pitch : harmonizedMelody) {
                 	
                 	System.out.println(pitch);
                 }
                 
                 System.out.println(C.compareTo(A));
         }
         
         //Function that prints the all the possible traversals from the starting node of printGraph
         public static void printGraph(Graph<ChordSymbol> printGraph) {
         	
         	Node<ChordSymbol> rootNode = printGraph.getStartingNode();
         	
         	if(rootNode != null) {
 	        	List<Edge<ChordSymbol>> startingEdges = rootNode.getFollowing();
 	        	for(Edge<ChordSymbol> edge : startingEdges) {
 	        		
 	        		ArrayList<ChordSymbol> chordSymbolPrintList = new ArrayList<ChordSymbol>();
 	        		
 	        		Node<ChordSymbol> startingNode = edge.getBack();
 	        		chordSymbolPrintList.add(startingNode.getValue());
 	//        		System.out.println("startingNode.getValue = " + startingNode.getValue().getSymbolText());
 	        		printGraphHelper(startingNode, chordSymbolPrintList);
 	        	}
         	}
         }
         
         //Helper function for printGraph
         public static void printGraphHelper(Node<ChordSymbol> current, ArrayList<ChordSymbol> toPrint) {
         	
         	List<Edge<ChordSymbol>> followingEdges = current.getFollowing();
         	if(followingEdges.isEmpty()) {
         		
         		//Graph is thoroughly traversed, print List toPrint;
         		for(ChordSymbol chordsym : toPrint) {
         			
         			System.out.print(printChordSymbol(chordsym) + " - ");
         		}
         		System.out.println("");
         	}
         	else {
         		
         		for(Edge<ChordSymbol> edge : followingEdges) {
         			
         			Node<ChordSymbol> nextNode = edge.getBack();
 
     				//clone the toPrint List
     				ArrayList<ChordSymbol> toPrintClone = (ArrayList<ChordSymbol>) toPrint.clone();
     				toPrintClone.add(nextNode.getValue());
 //    				System.out.println("  nextNode.getValue = " + nextNode.getValue().getSymbolText());
     				//recur
     				printGraphHelper(nextNode, toPrintClone);
         		}
         	}
         }
         
         // returns a String for printing some necessary information of a ChordSymbol object
         public static String printChordSymbol(ChordSymbol chordsym) {
         	
         	return (chordsym.getSymbolText() + chordsym.getTopInversionText() + chordsym.getBotInversionText());
         }
         
         
 }
