 package com.appsolut.composition.pitch_detection;
 
 import java.util.ArrayList;
 
 import com.leff.midi.MidiFile;
 import com.leff.midi.MidiTrack;
 import com.leff.midi.event.meta.Tempo;
 import com.leff.midi.event.meta.TimeSignature;
 
 
 public class WaveToMidi {
 	private final static double[] FREQS_MIDI_OCT_0 = {	261.6255653006,
 																277.1826309769,
 																293.6647679174,
 																311.1269837221,
 																329.6275569129,
 																349.2282314330,
 																369.9944227116,
 																391.9954359817,
 																415.3046975799,
 																440.0000000000,
 																466.1637615181,
 																493.8833012561};
 	private final static int DEFAULT_CLIP_RATE = 5;//Number of frequencies/second
 	
 	// MIDI resources
 	private MidiFile midiFile;
 	private MidiTrack tempoTrack;
 	private MidiTrack noteTrack;
 	
 	public WaveToMidi(int bpm) {
 	    // MIDI Instantiation
 	    midiFile = new MidiFile(MidiFile.DEFAULT_RESOLUTION);
 	    tempoTrack = new MidiTrack();
 	    noteTrack = new MidiTrack();
 	    
 	    // Tempo track
 	    TimeSignature ts = new TimeSignature();
 	    ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
 	    Tempo t = new Tempo();
 	    t.setBpm(bpm);
 	    tempoTrack.insertEvent(ts);
 	    tempoTrack.insertEvent(t);
 	    midiFile.addTrack(tempoTrack);
 	}
 	
 	public MidiFile audioToMidiFile(double[] audio, long sampleRate) {
 		return audioToMidiFile(audio,sampleRate,DEFAULT_CLIP_RATE);
 	}
 	
 	public MidiFile audioToMidiFile(double[] audio, long sampleRate, int clipRate){
 		Pair<Integer[],Long[]> midiNums = getMidiNumsWithTicks(audioToMidiNums(audio,sampleRate,clipRate));
 		long onTick = 0;
 		for(int i=0;i<midiNums.left.length;i++){
 			noteTrack.insertNote(0, midiNums.left[i], 127, onTick,midiNums.right[i]);
 			onTick += midiNums.right[i];
 		}
 		midiFile.addTrack(noteTrack);
 		return midiFile;
 		
 	}
 	public Pair<Integer[],Long[]> getMidiNumsWithTicks(int[] midiNums){
 		long TICKS_PER_OCCURRENCE = 12;
 		ArrayList<Integer> newMidiNums = new ArrayList<Integer>();
 		ArrayList<Long> ticksPerMidiNum = new ArrayList<Long>();
 		int lastNum = midiNums[0];
 		int dur = 1;
<<<<<<< HEAD
=======
 		for(int m: midiNums){
 			if(m != lastNum){
 				newMidiNums.add(m);
 				ticksPerMidiNum.add(dur*TICKS_PER_OCCURRENCE);
 				lastNum = m;
 				dur = 1;
 			}else{
 				dur++;
 			}
 		}
>>>>>>> changed comments
		for(int m: midiNums){
			if(m != lastNum){
				newMidiNums.add(m);
				ticksPerMidiNum.add(dur*TICKS_PER_OCCURRENCE);
				lastNum = m;
			}else{
				dur++;
			}
		}
//		for(int m: midiNums){
//			newMidiNums.add(m);
//			ticksPerMidiNum.add(TICKS_PER_OCCURRENCE);
//		}
 		Integer[] outMidiNums= new Integer[0];
 		outMidiNums = newMidiNums.toArray(outMidiNums);
 		Long[] outTicksPer = new Long[0];
 		outTicksPer = ticksPerMidiNum.toArray(outTicksPer);
 		return new Pair<Integer[],Long[]>(outMidiNums,outTicksPer);
 		
 	}
 	
 	static int[] audioToMidiNums(double[] audio, long sampleRate, int clipRate){
 		return 	snapIntervals(
 				freqsToRawIntervals(
 				PlotTones.audioToFreqs(audio, sampleRate, clipRate)));
 	}
 	
 	static Pair<Integer,double[]> freqsToRawIntervals(int[] freqs){
 		int baseFreq = freqs[0];
 		double[] intervals = new double[freqs.length];
 		for(int i=0; i<freqs.length; i++){
 			intervals[i] = 12*Math.log(1.0*freqs[i]/baseFreq)/Math.log(2);//The number of half steps from baseFreq
 		}
 		return new Pair<Integer,double[]>(baseFreq,intervals);
 	}
 	
 	static int[] snapIntervals(Pair<Integer,double[]> inp){
 		int baseFreq = inp.left;
 		double[] intervals = inp.right;
 		int octaveNum = (int) Math.round(Math.log(baseFreq/FREQS_MIDI_OCT_0[9])/Math.log(2));
 		double[] octave = new double[12];
 		for(int i=0;i<octave.length;i++){
 			octave[i] = Math.pow(2, octaveNum)*FREQS_MIDI_OCT_0[i];
 		}
 		double min = Integer.MAX_VALUE;
 		int closestTone = 0;
 		for(int i=0;i<octave.length; i++ ){
 			if(Math.abs(baseFreq-octave[i])<min){
 				min = Math.abs(baseFreq-octave[i]);
 				closestTone = i;
 			}
 		}
 		closestTone = closestTone + 60 + octaveNum*12;
 		int[] midiNums = new int[intervals.length];
 		for(int i=0; i<intervals.length; i++){
 				midiNums[i] = (int) Math.round(intervals[i])+ closestTone;
 		}
 		return midiNums;
 	}
 	
 
 }
