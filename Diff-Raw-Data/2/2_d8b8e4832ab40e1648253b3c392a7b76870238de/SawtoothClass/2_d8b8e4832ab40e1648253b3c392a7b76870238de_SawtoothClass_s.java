 package de.cgarbs.apsynth.instrument.library;
 
 import de.cgarbs.apsynth.instrument.Instrument;
 import de.cgarbs.apsynth.internal.Pool;
 import de.cgarbs.apsynth.note.Note;
 import de.cgarbs.apsynth.signal.Signal;
 import de.cgarbs.apsynth.signal.library.ADSREnvelopeClass.ADSREnvelope;
 
 public class SawtoothClass extends DefaultInstrumentClass {
 
     public SawtoothClass() {
         this.paramCount = 0;
     }
     
     public String getName() {
         return "Sawtooth";
     }
 
     public Instrument instanciate(Signal[] s) {
         checkParams(s);
         return new Sawtooth();
     }
 
     public static class Sawtooth extends Instrument {
 
         private Sawtooth() {
             
         };
         
         public Note play(Signal freq, long length) {
             return new Note(
                     Pool.getSignalClass("Amplifier").instantiate(new Signal[]{
                            Pool.getSignalClass("WhiteSawtoothWave").instantiate(new Signal[]{freq}),
                             new ADSREnvelope(length, 0, 0, 1, 100)   
                     }),
                     length
                     );
         }
         
     }
 }
