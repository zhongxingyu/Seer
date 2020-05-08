 package player;
 
 //import java.io.File;
 //import java.io.FileNotFoundException;
 //import java.util.Scanner;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 
 import sound.SequencePlayer;
 
 //import sound.SequencePlayer;
 
 /**
  * Main entry point of your application.
  */
 public class Main {
 
     /**
      * Plays the input file using Java MIDI API and displays
      * header information to the standard output stream.
      * 
      * (Your code should not exit the application abnormally using
      * System.exit().)
      * 
      * @param file the name of input abc file
      */
     public static void play(String file) {
         // YOUR CODE HERE
         
         String music = file; 
 
         //Create Lexer
         Lexer myLexer = new Lexer(music);
         //Tokenize
         ArrayList<Token> tokens = myLexer.lex();        
         //Parse
         Parser myParser = new Parser(tokens);
         ArrayList<BarLineObject> barLineObjects = myParser.parse(); 
         
        //Get Header Vals
         String keySignature = myParser.getKey() ;
         int tempo =   myParser.getTempo() ;
         System.out.println("Main's tempo: "+tempo);
         String name = myParser.getName();
         String title = myParser.getTitle();
         int indexNum = myParser.getIndexNum();
         
         //Parse 1
        ArrayList<String> voiceNames = myParser.getVoice();
 
         int coreNumerator = 1;
         int coreDenominator = 4;
         if (myParser.getDefLen().size()==2){
         	coreNumerator = myParser.getDefLen().get(0);
         	coreDenominator = myParser.getDefLen().get(1);
         }
        
         tempo = (tempo * 4 * coreNumerator) / coreDenominator;
        
         int MeterNum = 1;
         int MeterDen = 1;
         if (myParser.getMeter().size()==2){
         	MeterNum = myParser.getMeter().get(0);
         	MeterDen = myParser.getMeter().get(1);

         }
         
         //Parse 2
         Parser2 myParser2 = new Parser2(voiceNames);
         myParser2.parse(barLineObjects);
         ArrayList<Voice> voices = myParser2.getVoices();
         
         Song mySong = new Song(voices);
         SequencePlayer sqPlayer;
         try {
             System.out.println("Creating SQ "+tempo+", minTpQ: "+mySong.getMinTicksPerQuarter());
             sqPlayer = new SequencePlayer(tempo, mySong.getMinTicksPerQuarter());
             myPlayer MrAhmed = new myPlayer(keySignature, sqPlayer);
             mySong.addToPlayer(MrAhmed);
             sqPlayer.play();
             
         } catch (MidiUnavailableException e) {
             e.printStackTrace();
         } catch (InvalidMidiDataException e) {
             e.printStackTrace();
         }
         
  
     }
     
     public static String getHeyJude(){
         return "^d [c5/2^D5/8C5/8^G5/8^G,3/8] z/8 ^G,3/8 z/8 [^D5/8C5/8^G5/8^G,3/8] |";
 //                +"z/8 ^G,3/8 z/8 [^D5/8C5/8^G5/8^G,3/8] z/8 [c/2^G,3/8] z/8 |"
 //                +"[^d/2^G5/8c5/8^G,3/8] z/8 [f/2^G,3/8] z/8 [^A2^A,5/8^D5/8=G,3/8] z/8 |"
 //                +"^D,3/8 z/8 [^A,5/8^D5/8G,3/8] z/8 ^D,3/8 z/8 [^A,5/8^D5/8G,3/8] z/8 |"
 //                +"^D,3/8 z/8 [^A/2^D5/8G,3/8] z/8 [c/2^G,3/8] z/8 |"
 //                +"[^c5/8=G5/8^A5/8^A,3/8] z/8 ^D,3/8 z/8 [^g11/8^A5/8^c5/8^C3/8] z/8 |"
 //                +"^D,3/8 z/8 [^A5/8^c5/8^C3/8] z/8 [^g3/8^D,3/8] z/8 |"
 //               +"[=g3/8^A5/8^c5/8^C3/8] z/8 [^d3/8^D,3/8] z/8 [f3/8^G5/8^G,3/8] z/8 |"
 //               +"[^d/4^G,3/8] ^c/4 [=c13/8^D5/8^G5/8^G,3/8] z/8 ^G,3/8 z/8 |"
 //                +"[^D5/8^G5/8^G,3/8] z/8 ^G,3/8 z/8 [^D3/8^G3/8^G,3/8] z/8 |"
 //                +"[^d3/8^G3/8c3/8^G,3/8] z/8 [f3/8^G5/8^c5/8^C3/8] z/8 [f7/8^C,3/8] z/8 |"
 //                +"[^G^c^C3/8] z/8 [f3/8^C,3/8] z/8 [^a/8g/8^C3/8] z/8 [^g3/8f3/8z/4] ||";
                
     }
     
     public static String getAwayFromTheSun(){
         
             return "^c3/8 ^g3/8 f3/8 ^c3/8 ^g3/8 f3/8 ^c3/8 ^g3/8 =c3/8 ^g3/8 f3/8 c3/8 |"
                     +"=g3/8 ^g3/8 ^d3/8 ^g3/8 ^G3/8 ^g3/8 ^d3/8 ^G3/8 ^g3/8 ^d3/8 ^G3/8 |"
                     +"^g3/8 =G3/8 ^g3/8 ^d3/8 G3/8 =g3/8 ^g3/8 ^a3/8 c'3/8 [^c3/8f3/4^g3/8] |"
                     +"[^g3/8z3/8] [f3/8z3/8] [^c3/8z3/8] [^g3/8z3/8] [f3/8z3/8] [^c3/8z3/8] |"
                     +"^g3/8 [=c9/8^g3/8f3/4] [^g3/2z3/8] [f15/8z3/8] [c15/8z3/8] =g3/8 |"
                     +"[^g3/4z3/8] ^d3/8 ^g3/8 [^D3/4^A6G6^d6=g15/8z3/8] ^a3/8 [^D3/4z3/8] |"
                     +"^g3/8 [^D9/8z3/8] [=g3z3/4] ^D3/8 [^D3/4z3/8] ^a3/8 [^D3/4z3/8] ^g3/8 |"
                     +"[^D3/2z3/8] [=g9/8z3/4] ^g3/8 [^c3/8^C3] ^g3/8 f3/8 ^c3/8 ^g3/8 f3/8 |";
                
     }
     
     public static String getFallenLeaves(){
         
         return "[e/4^c/4] z/4 [e/4^G/4] z/4 [e/4^c/4] z/4 [e/4^G/4] z/4 [^d/4=c/4] z/4 |"
                +" [^d/4^G/4] z/4 [^d/4c/4] z/4 [^d/4^G/4] z/4 [^d/4c/4] z/4 [^d/4^G/4] |"
                  +"       z/4 [^f/4^G/4] z/4 [^f/4^G/4] z/4 [e/4B/4] z/4 [e/4^G/4] z/4 [e/4B/4] |"
                    +"     z/4 [e/8^G/8] z/4 [e/4A/4] z/4 [e/4^G/4] z/4 [e/4A/4] z/4 [e/4^G/4] |"
                      +"   z/4 [^c/4^G/4^F/4] z3/8 [^c/4^G/4^F/4] z3/8 [^c/4^G/8^F/4] z3/8 |"
                        +" [^c/4^G/8^F/4] z3/8 [^c/4^G/8E/4] z3/8 [^c/4^G/8E/4] z/4 [^c^G/8E] |"
                         +"z3/8 ^G/8 z3/8 [=c/4^G/4^D/4] z/4 [c/4^G/4^D/4] z/4 [c/4^G/4^D/4] z/4 |"
                         +"[^c/8=c/4^G/4^D/4] z/8 ^d/8 z/8 [e/4^c/4] z/4 [^c/4e/4^G/4] z/4 |"
                         +"[e/4^c/4] z/4 [^f/4e/4^G/4] z/8 [^g^d/4=c/4] z/4 [^d/4^G/4] z/4 |"
                         +"[^g/4^d/4c/4] z/4 [^g/4^d/4^G/4] z/4 [^g/4^d/4c/4] z/4 [a/4^d/4^G/4]"
                         +"z/4 [^g/4^f/4^G/4] z/4 [^f/4^G/4] z/4 [e/2B/4] z/4 [e/2^G/4] z/4"
                         +"[e/8B/8] z/4 [e/4^G/4] z/4 [e/2A/4] z/4 [e/2^G/4] z/4 [e/4A/4] z/4"
                         +"[e/4^G/4] z/4 [e^c/4^G/8^F/4] z3/8 [^c/4^G/8^F/4] z3/8"
                         +"[e/4^c/4^G/8^F/4] z3/8 [e/4^c/4^G/8^F/4] z3/8 [e7/8^c/8^G/8E/8] z/4"
                         +"[^c/4^G/8E/4] z3/8 [e/4^c^G/8E] z3/8 [^f/4^G/8] z3/8"
                         +"[^d2=c/4^G/4^D/4] z/4 [c/4^G/4^D/4] z/4 [c/4^G/4^D/4] z/4"
                         +"[c/4^G/4^D/4] z/4 [^c^G^C^C,] |";
            
 }
 
     public static String getFurElise(){
         return "e^d|e^deB=dc|A2 z CEA|B2 z E^GB|c2 z Ee^d|";
     }
     
     
     public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
 
         try {
             play(readABCFile("paddy.abc"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         
 //         //CALL play() HERE
 
     }
 
     public static void alprint(ArrayList l, String name){
         System.out.println("\nNOW PRINTING "+name+"\n");
         for (int i = 0; i<l.size(); i++){
             System.out.println(l.get(i));
         }
     }
     
     private static String readABCFile(String name) throws IOException {
 
         File file = new File("sample_abc\\"+name);
         StringBuilder fileContents = new StringBuilder((int)file.length());
         Scanner scanner = new Scanner(file);
         String lineSeparator = System.getProperty("line.separator");
 
         try {
             while(scanner.hasNextLine()) {        
                 fileContents.append(scanner.nextLine() + lineSeparator);
             }
             return fileContents.toString();
         } finally {
             scanner.close();
         }
     }
 }
