 package de.cgarbs.apsynth;
 
 import java.util.Date;
 
 import de.cgarbs.apsynth.envelope.ADSREnvelopeClass;
 import de.cgarbs.apsynth.instrument.library.HiHatClass;
 import de.cgarbs.apsynth.instrument.library.SawtoothClass;
 import de.cgarbs.apsynth.instrument.library.SineClass;
 import de.cgarbs.apsynth.instrument.library.SquareClass;
 import de.cgarbs.apsynth.instrument.library.SquineClass;
 import de.cgarbs.apsynth.internal.Pool;
 import de.cgarbs.apsynth.signal.library.AmplifierClass;
 import de.cgarbs.apsynth.signal.library.ArpeggioClass;
 import de.cgarbs.apsynth.signal.library.ClipperClass;
 import de.cgarbs.apsynth.signal.library.ConstantSignalClass;
 import de.cgarbs.apsynth.signal.library.DataBlockClass;
 import de.cgarbs.apsynth.signal.library.DelayEchoClass;
 import de.cgarbs.apsynth.signal.library.EndianChangerClass;
 import de.cgarbs.apsynth.signal.library.FiniteImpulseResponseClass;
 import de.cgarbs.apsynth.signal.library.HighPassClass;
 import de.cgarbs.apsynth.signal.library.LowPassClass;
 import de.cgarbs.apsynth.signal.library.MixerClass;
 import de.cgarbs.apsynth.signal.library.PinkNoiseClass;
 import de.cgarbs.apsynth.signal.library.QuantizerClass;
 import de.cgarbs.apsynth.signal.library.RegisterClass;
 import de.cgarbs.apsynth.signal.library.SawtoothWaveClass;
 import de.cgarbs.apsynth.signal.library.SineWaveClass;
 import de.cgarbs.apsynth.signal.library.SquareWaveClass;
 import de.cgarbs.apsynth.signal.library.SquineWaveClass;
 import de.cgarbs.apsynth.signal.library.WhiteNoiseClass;
 import de.cgarbs.apsynth.storage.FilesystemStorage;
 
 public class Apsynth {
     
     public final static int samplefreq = 44100; 
     
     static void initialize() {
 
         // Register Classes
         // TODO auto-register via inspection?
         Pool.registerSignalClass(new AmplifierClass());
         Pool.registerSignalClass(new ConstantSignalClass());
         Pool.registerSignalClass(new DelayEchoClass());
         Pool.registerSignalClass(new EndianChangerClass());
         Pool.registerSignalClass(new MixerClass());
         Pool.registerSignalClass(new WhiteNoiseClass());
         Pool.registerSignalClass(new PinkNoiseClass());
         Pool.registerSignalClass(new QuantizerClass());
         Pool.registerSignalClass(new SawtoothWaveClass());
         Pool.registerSignalClass(new SineWaveClass());
         Pool.registerSignalClass(new SquareWaveClass());
         Pool.registerSignalClass(new SquineWaveClass());
         Pool.registerSignalClass(new ClipperClass());
         Pool.registerSignalClass(new de.cgarbs.apsynth.signal.library.NullClass());
         Pool.registerSignalClass(new RegisterClass());
         Pool.registerSignalClass(new ArpeggioClass());
         Pool.registerSignalClass(new FiniteImpulseResponseClass());
         Pool.registerSignalClass(new DataBlockClass());
         Pool.registerSignalClass(new HighPassClass());
         Pool.registerSignalClass(new LowPassClass());
 
         Pool.registerInstrumentClass(new SineClass());
         Pool.registerInstrumentClass(new SquareClass());
         Pool.registerInstrumentClass(new SquineClass());
         Pool.registerInstrumentClass(new SawtoothClass());
         Pool.registerInstrumentClass(new de.cgarbs.apsynth.instrument.library.NullClass());
         Pool.registerInstrumentClass(new HiHatClass());
         
         Pool.registerEnvelopeClass(new ADSREnvelopeClass());
         
     }
 
     public static void printInfo() {
     	System.out.println("Apsynth prerelease");
     	System.out.println("2006-2007 (c) by Christian Garbs <mitch@cgarbs.de>");
     	System.out.println("Licensed under GNU GPL");
     	System.out.println("");
     	System.out.println("Call with project file as first parameter");
     }
     
 	public static void main(String[] argv) {
 	
 		Apsynth.printInfo();
         Apsynth.initialize();
         
         WaveWriter mainSignal = new FilesystemStorage().readProject(argv[0]);
         
         long startTime = new Date().getTime();
         
         long tick = 0;
 
         do {
             // 10000 ticks granularity for end detection
             for (int i=0; i<10000; i++) {
                 mainSignal.write(tick);
                 tick++;
 
                 if (tick % samplefreq == 0) {
                     timestamp(tick);
                 }
}
         } while (! Pool.allTracksFinished());
         
         // wait for 0.5s pause
         long silence = 0;
         while (silence < samplefreq/2) {
             if (mainSignal.write(tick) < 0.001) {
                 silence++;
             } else {
                 silence = 0;
             }
             tick++;
 
             if (tick % samplefreq == 0) {
                 timestamp(tick);
             }
             
         }
         
         long endTime = new Date().getTime();
         
         mainSignal.close();
 
         long seconds = (endTime - startTime) / 1000;
         
         if (seconds == 0) {
         	System.out.println("done (instant win)");
         } else {
         	System.out.println("done ("+tick+" ticks in " + seconds + " seconds = ~" + tick/seconds + "t/s)");
         }
                 
 	}
 
     private static void timestamp(long tick) {
         long seconds = tick / samplefreq;
         long minutes = seconds / 60;
         seconds = seconds % 60;
         System.out.print(minutes+":");
         System.out.println(seconds);
     }
 }
