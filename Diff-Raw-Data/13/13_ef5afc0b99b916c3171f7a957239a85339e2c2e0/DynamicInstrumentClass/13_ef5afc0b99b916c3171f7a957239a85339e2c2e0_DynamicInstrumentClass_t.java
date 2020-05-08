 package de.cgarbs.apsynth.instrument.dynamic;
 
 import de.cgarbs.apsynth.envelope.Envelope;
 import de.cgarbs.apsynth.instrument.Instrument;
 import de.cgarbs.apsynth.instrument.InstrumentClass;
 import de.cgarbs.apsynth.internal.Pool;
 import de.cgarbs.apsynth.note.EnvelopeNote;
 import de.cgarbs.apsynth.note.Note;
 import de.cgarbs.apsynth.signal.Signal;
 import de.cgarbs.apsynth.signal.SignalClass;
 
 public class DynamicInstrumentClass extends InstrumentClass {
 
     private String name = "unnamedDynamicInstrumentClass";
     private String signal = "Null";
     private Envelope env = null;
     
     public DynamicInstrumentClass(String name, String signal, Envelope env) {
         this.name = name;
         this.signal = signal;
         this.env = env;
     }
     
     public Instrument instanciate(Signal[] s) {
         return new DynamicInstrument(Pool.getSignalClass(signal), s, this.env);
     }
 
     public String getName() {
         return this.name;
     }
     
    @Override
    public int getParamCount() {
        return Pool.getSignalClass(signal).getParamCount() - 1;
    }



     public class DynamicInstrument extends Instrument {
 
         private SignalClass signalClass = null;
         private Signal[] s = {};
         private Envelope env = null;
         
         private DynamicInstrument(SignalClass signalClass, Signal[] s, Envelope env) {
             this.signalClass = signalClass;
             this.s = s;
             this.env = env;
         }
         
         public Note play(Signal freq, long length) {
             
            Signal[] signals = new Signal[s.length + 1];
            signals[0] = freq;
             for (int i=0; i<s.length ;i++) {
                 signals[i+1] = s[i];
             }
             
             if (this.env == null) {
                 return new Note(
                         signalClass.instantiate(signals),
                                 length
                                 );
             } else {
                 return new EnvelopeNote(
                         signalClass.instantiate(signals),
                                 length,
                                 env
                                 );
             }
         }
         
     }
 
 }
