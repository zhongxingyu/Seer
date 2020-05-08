 package troll;
 
 public class Main {
 
     public static final int SAMPLE_COUNT = 1000;
 
     public static void main(String[] args) throws Exception {
         long currentSample = 0;
         Configuration configuration = new Configuration(512, 44100);
 
         OneNoteKeyboard keyboard = new OneNoteKeyboard("foo");
         Adsr adsr = new Adsr("adsr");
        adsr.setAttackMs(10);
        adsr.setDecayMs(500);
        adsr.setSustain(Float.MIN_VALUE / 16);
        adsr.setReleaseMs(100);
 
         Operator osc1 = new Oscillator("OSC1");
 
         adsr.connect(keyboard, OneNoteKeyboard.GATE, Adsr.GATE);
 
         osc1.connect(keyboard, OneNoteKeyboard.FREQUENCY, Oscillator.FREQUENCY);
         osc1.connect(adsr, Adsr.OUTPUT, Oscillator.AMPLITUDE);
 
         Operator dac = new Dac().connect(osc1, Oscillator.OUTPUT, Dac.INPUT);
 
         while (true) {
             dac.execute(configuration, currentSample);
             currentSample += configuration.getSampleSize();
         }
     }
 }
 
