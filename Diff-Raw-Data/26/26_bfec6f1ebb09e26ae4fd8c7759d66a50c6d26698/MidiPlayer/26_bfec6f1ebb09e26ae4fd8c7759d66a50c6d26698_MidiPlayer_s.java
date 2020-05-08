 package GameOfLife;
 
 
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.Synthesizer;
 import javax.sound.midi.MidiChannel;
 
 public class MidiPlayer {
 
 	public int channelNum; // 0 = piano
 
 	public int volume;
 	public int duration; // milliseconds
 
 	public Synthesizer synth;
 
 	public MidiChannel channel;
 
 	public MidiScale scale; // optional
 
 	public MidiPlayer(int channelNum, int volume, int duration) {
 		this.channelNum = channelNum;
 		this.volume = volume;
 		this.duration = duration;
 		try {
 			this.synth = MidiSystem.getSynthesizer();
 			synth.open();
 			MidiChannel[] channels = synth.getChannels();
 
 			channel = channels[channelNum];
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public MidiPlayer() {
 		this(8,80,200);
 	}
 
 	public void close() {
 		try {
 			Thread.sleep( 500 );
 			this.synth.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void playSound(int note) {
 		try {
 			this.channel.noteOn( note, volume ); // C note
 			Thread.sleep( duration );
 			this.channel.noteOff( note );
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void playNormalized(int n, int max) {
 		int maxMidi = 120;
 		int note = (int) ((1.0*n) / max * maxMidi);
 		playSound(note);
 	}
 
 	public void playNormalizedWithScale(int n, int max, boolean keepOrder) {
 		// To be removed from the scale
 		int start = 20;
 		int end = 35; // Minus
 		
 		int no;
 		if(keepOrder) {
 			no = (int) ((1.0*n) / max * (scale.getLength() - end)) + start;
 		} else {
 			no = start + (n % (scale.getLength() - end));
 		}
 		playWithScale(no);
 	}
 	
 	public void playIntWithScale(int n) {
 		int start = 20;
 		int end = 35; // Minus
 		
 		int no = start + (n % (scale.getLength() - end));
 		playWithScale(no);
 	}
 
 	public void playWithScale(int n) {
 		playSound(scale.getArray()[n]);
 	}
 	
	public void playMultipleNotesInInterval(int[] notes, int intervalDuration) {
 		int saveDuration = duration;
		duration = intervalDuration / notes.length;
 		for(int note : notes) {
 			System.out.println(note);
 			playIntWithScale(note);
 		}
 		duration = saveDuration;
 	}
 
 
 
 	public static void main(String[] args) {
 		try {
 			MidiPlayer a = new MidiPlayer(8,80,200);
 			a.scale = new MidiScale();
 
 				/*
 				Instruments (channels)
 					0-6 Like Pianos
 					7 Gameboy
 					8 Glockenspiel ;)
 					9 Percussion
 					10 Futuristic
 					14 church bells
 
 				*/
 
 				// Example song
 
 				// init random notes
 				int n = 16;
 				int ran[] = new int[n];
 				for(int i = 0; i < n; i++) {
 					ran[i] = (int) (Math.random()*500);
 				}
 
 				a.scale.setScale("A", "moll");
 				System.out.println("A moll");
				a.playMultipleNotesInInterval(ran,3000);
 
 				a.scale.setScale("F", "dur");
 				System.out.println("F dur");
				a.playMultipleNotesInInterval(ran, 3000);
 
 				a.scale.setScale("C", "dur");
 				System.out.println("C dur");
				a.playMultipleNotesInInterval(ran, 3000);
 				a.scale.setScale("G", "dur");
 				System.out.println("G dur");
				a.playMultipleNotesInInterval(ran, 3000);
 			a.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 
 	}
 }
