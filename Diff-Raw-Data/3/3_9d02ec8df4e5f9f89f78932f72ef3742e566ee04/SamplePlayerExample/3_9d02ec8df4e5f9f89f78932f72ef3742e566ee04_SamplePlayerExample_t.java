 package net.beadsproject.beads.ugens;
 
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.data.Sample;
 import net.beadsproject.beads.data.SampleManager;
 import net.beadsproject.beads.events.AudioContextStopTrigger;
 
 public class SamplePlayerExample {
     public static void main(String[] args) throws Exception {
     	System.out.println("Testing: " + SamplePlayer.class);
     	AudioContext ac = new AudioContext(512);
     	
     	Sample.Regime reg = Sample.Regime.newStreamingRegime(10);
     	// SampleManager.setBufferingRegime(reg);
     	//reg = new Sample.TimedRegime(100,0,0,-1, Sample.TimedRegime.Order.ORDERED);
     	
 //    	Sample s1 = SampleManager.sample("audio/1234.aif");
//    	Sample s1 = SampleManager.sample("/Users/ollie/Music/Audio/Albert Breaks/rud208 1.aiff");
    	Sample s1 = SampleManager.sample("/Users/ollie/Desktop/16475__Jlew__Dialup.mp3");
     	s1.getAudioFile().trace = true;
     	// Sample s1 = SampleManager.sample("../BeadsTests/audio/gammaBrosTheme.mp3");
     	System.out.println(s1.getLength());
     	
     	SamplePlayer sp = new SamplePlayer(ac, s1);
     	// sp.setPosition(s1.getLength()-1000.f);    
     	sp.setKillListener(new AudioContextStopTrigger(ac));
     	
     	//sp.setInterpolationType(SamplePlayer.InterpolationType.CUBIC);
     	sp.setInterpolationType(SamplePlayer.InterpolationType.CUBIC);
 //    	sp.setEnvelopeType(SamplePlayer.EnvelopeType.COARSE);
     	
     	// sp.setPosition(s1.getLength() - 10);    	
     	// sp.setLoopType(SamplePlayer.LoopType.LOOP_BACKWARDS);
 //    	sp.getRateEnvelope().setValue(.898f);
     	
     	/*
     	Envelope rateEnv = new Envelope(ac, 0.25f);
     	rateEnv.addSegment(0.25f, 5000f);
     	rateEnv.addSegment(4f, 20000f);
     	sp.setRateEnvelope(rateEnv);
     	sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING); 
     	sp.getLoopEndEnvelope().setValue(1000f);
     	sp.getLoopStartEnvelope().setValue(500f);
     	*/    	    	
     	
     	ac.out.addInput(sp);
     	ac.start();
     }
 }
