 package midiParse;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MetaMessage;
 import javax.sound.midi.MidiEvent;
 import javax.sound.midi.MidiMessage;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.ShortMessage;
 import javax.sound.midi.Track;
 
 public class MidiParser {
 	private static final int TEMPO_MESSAGE_VALUE = 81;
 	private static final int MIRCOSECONDS_IN_MINUTE = 60000000;
 	private static final int MICROSECONDS_IN_SECOND = 1000000;
 	private final String NOTE_ON = "on";
 	private final String NOTE_OFF = "off";
 	private final int MAX_NOTE_ARRAY_SIZE = 12;
 	private final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
 	
 	ScaleDetector scaleDetector = new ScaleDetector();
 	File saveFile = new File("C:\\Users\\cbaldwin\\Desktop\\MidiOutput.txt");
 	
 	ArrayList<String> notesForScaleDetection = new ArrayList<String>();
 	PrintWriter writer;
 	
 	int scaleDetectionIndex;
 	int currentChannel;
 	double bpm = 0;
 	double ppqn = 0;
 	double microsecsPerQuarterNote = 0;
 	double previousTotalDuration = 0.0;
 	double songDuration = 0.0;
 	
 	public File parseFile(String fileName)
 	{
 		Sequence seq;
 
 		try {
 			writer = new PrintWriter(saveFile);
 			seq = MidiSystem.getSequence(new File(fileName));
 			for(Track track : seq.getTracks()) {
 				currentChannel = -1;
 				for(int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
 					MidiEvent event = track.get(eventIndex);
 					MidiMessage message = event.getMessage();
 					ppqn = seq.getResolution();
 							
 					if(message instanceof ShortMessage) {
 						parseShortMessage((ShortMessage)message, currentChannel);
 						getEventDuration(event.getTick());
 					}
 					else if(message instanceof MetaMessage)
 						parseMetaMessage((MetaMessage)message);
 				}
 			}
 			System.out.println("TOTAL: " + songDuration);
 			writer.close();
 		} catch (InvalidMidiDataException | IOException e) {
 			e.printStackTrace();
 		}	
 		
 		System.out.println(scaleDetector.detectScale(getNoteArray()));
 		//notesForScaleDetection.clear();
 		notesForScaleDetection = new ArrayList<String>();
 		scaleDetectionIndex = 0;
 		return saveFile;
 	}
 	
 	private String parseShortMessage(ShortMessage m, int currentChannel)
 	{
 		String output = "";
 		
 		int channel = m.getChannel();
 		int velocity = m.getData2();
 		int key = m.getData1();
 		int octave = (key/MAX_NOTE_ARRAY_SIZE);
 		
 		String note = Notes.getNoteName(key % MAX_NOTE_ARRAY_SIZE);
 		String status = (m.getCommand() == ShortMessage.NOTE_ON) ? NOTE_ON : NOTE_OFF;
 		if(status.equals(NOTE_ON))detectionSetup(note, octave);
 		
 		if(channel != currentChannel) {
 			currentChannel = channel;
 			output = "\r\n\r\nChannel:" + channel + "\r\n\tNote:" + note + octave + "\\Status:" + status + "\\Velocity:" + velocity + "\r\n";
 		}
 		else 
 			output = "\tNote:" + note + octave + " \\Status:" + status + " \\Velocity:" + velocity + "\r\n";
 	
 		System.out.print(output);
 		writer.print(output);
 		writer.flush();
 		
 		return output;
 	}
 	
 	private double getEventDuration(long ticks){
 		double numQuarterNotes = ticks/ppqn;
 		double totalDuration = (numQuarterNotes * microsecsPerQuarterNote) / MICROSECONDS_IN_SECOND;
 		double duration = totalDuration - previousTotalDuration;
		
		if(totalDuration > songDuration)
			songDuration = totalDuration;
		
 		previousTotalDuration = totalDuration;
 		
		String output = ("\tDURATION: " + duration + "\r\n");
 		System.out.println(output);
 		writer.write(output);
 		writer.flush();
 		
 		return duration;
 	}
 	
 	private String parseMetaMessage(MetaMessage m) {
 		String output = "";
 		
 		if(m.getType() == TEMPO_MESSAGE_VALUE)
 		{
 			bpm = getBPM(m.getData());
 			output = "METADATA--------- BPM:" + bpm + "\r\n";
 		}
 		else
 			output = "METADATA--------- " + new String(m.getMessage()) + "\r\n";
 		
 		System.out.print(output);
 		writer.print(output);
 		writer.flush();
 		
 		return output;
 	}
 	
 	//not actually BPM at the moment, result is in mircoseconds
 	
 	private double getBPM(byte[] b) {
 		char[] hexChars = new char[b.length * 2];
 		int v;
 		for(int i = 0; i < b.length; i++) {
 			v = b[i] & 0xFF;
 			hexChars[i * 2] = hexArray[v >>> 4];
 			hexChars[i * 2 + 1] = hexArray[v & 0x0F];
 		}
 		String hexValue = new String(hexChars);
 		microsecsPerQuarterNote = Integer.parseInt(hexValue, 16);
 		return MIRCOSECONDS_IN_MINUTE/microsecsPerQuarterNote;
 	}
 	
 	private String[] getNoteArray() {
 		String[] elements = new String[notesForScaleDetection.size()];
 		notesForScaleDetection.toArray(elements);
 		return elements;
 	}
 	
 	private void detectionSetup(String note, int octave) {
 		if(scaleDetectionIndex<MAX_NOTE_ARRAY_SIZE)
 		{
 			 notesForScaleDetection.add(note + ";" + octave + ";");
 			 scaleDetectionIndex++;
 		}else if(scaleDetectionIndex > MAX_NOTE_ARRAY_SIZE) {
 			System.out.println(scaleDetector.detectScale(getNoteArray()));
 			notesForScaleDetection.clear();
 			notesForScaleDetection = new ArrayList<String>();
 			scaleDetectionIndex = 0;
 		}
 	}
 }
