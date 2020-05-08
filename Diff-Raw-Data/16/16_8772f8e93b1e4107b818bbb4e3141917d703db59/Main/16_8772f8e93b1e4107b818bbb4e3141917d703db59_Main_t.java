 package player;
 
 import grammar.MusicPlayerBody;
 import grammar.MusicPlayerHeader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 
 import sound.*;
 
 
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
      * @throws FileNotFoundException 
      * @throws InvalidMidiDataException 
      * @throws MidiUnavailableException 
      */
     public static void play(String file) throws FileNotFoundException, MidiUnavailableException, InvalidMidiDataException {
     	File songFile = new File(file);
     	String content = new Scanner(songFile).useDelimiter("\\Z").next();
     	String[] lines = content.split("\\r?\\n");
     	ArrayList<String> header = new ArrayList<String>();
     	ArrayList<String> body = new ArrayList<String>();
     	boolean inHead = true;    	
     	for(String line : lines){
     		if (inHead){
     			header.add(line);
     		}
     		else{
     			body.add(line);
     		}
     		if (line.charAt(0) == 'K'){
     			inHead = false;
     		}
     	}
     	
     	MusicPlayerHeader headPlayer = new MusicPlayerHeader();
     	Song song = headPlayer.parse(join(header,"\n"));
     	System.out.println(song);
     	
     	MusicPlayerBody bodyPlayer = new MusicPlayerBody(song);
     	song = bodyPlayer.parse(join(body, "\n"));
 
     	List<Measure> measures = song.getMeasures();
     	for (Measure measure : measures){
     		System.out.println(measure.toString());
     	}
     	song.play();
 
     }
 
     public static void main(String[] args) throws FileNotFoundException, MidiUnavailableException, InvalidMidiDataException {
         // CALL play() HERE
    	String file = "sample_abc/piece2.abc";
     	play(file);
     }
     
     public static String join(ArrayList<String> strings, String separator){
     	StringBuilder newStrings = new StringBuilder();
     	for(String line : strings){
     		newStrings.append(line+separator);
     	}
     	return newStrings.toString();
     }
     
 }
