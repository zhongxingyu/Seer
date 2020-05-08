 package troll;
 
 public class Adsr extends Operator {
     public static final String GATE = "gate";
     public static final String OUTPUT = "output";
 
     private float attackMs;
     private float decayMs;
     private float sustain; // Between -Float.MAX_FLOAT and +Float.MAX_FLOAT
     private float releaseMs;
 
     private long lastGateStart;
     private long lastGateEnd;
 
     private float[] outputBuffer;
 
     protected Adsr(String name) {
         super(name);
         outputBuffer = new float[0];
         lastGateStart = -Integer.MAX_VALUE;
         lastGateEnd = -Integer.MAX_VALUE + 1;
     }
 
     @Override
     protected void execute(Configuration configuration, long currentSample) {
         normalizeOutput(configuration.getSampleSize());
         float[] gateInput = readInput(configuration, currentSample, GATE);
 
         for (int i = 0; i < gateInput.length; i++) {
             outputBuffer[i] = 0;
             float gateValue = gateInput[0];
 
             if (gateValue != 0 && lastGateStart < lastGateEnd) {
                 lastGateStart = currentSample;
             }
 
             if (gateValue == 0 && lastGateStart > lastGateEnd) {
                 lastGateEnd = currentSample;
             }
 
             outputBuffer[i] = currentValue(configuration.getSamplingRate(), currentSample + i, lastGateStart, lastGateEnd);
         }
 
         publish(OUTPUT, outputBuffer);
     }
 
     public float currentValue(double samplingRate, long sample, long lastGateStart, long lastGateEnd) {
         double attackDuration = samplingRate * (attackMs / 1000d);
         double decayDuration = samplingRate * (decayMs / 1000d);
         double releaseDuration = samplingRate * (releaseMs / 1000d);
 
         if (lastGateStart > lastGateEnd) {
             long samplesSinceStart = sample - lastGateStart;
             if (sample < lastGateStart + attackDuration) {
                 double ratio = samplesSinceStart / attackDuration;
                return (float) ((Float.MAX_VALUE) * ratio);
             } else if (sample < lastGateStart + attackDuration + decayDuration) {
                 double samplesSinceDecayStart = samplesSinceStart - attackDuration;
                double step = Float.MAX_VALUE - sustain;
                double ratio = samplesSinceDecayStart / decayDuration;
                return (float) (Float.MAX_VALUE - (ratio * step));
             } else {
                 return sustain;
             }
         } else {
             long samplesSinceEnd = sample - lastGateEnd;
             if (samplesSinceEnd < releaseDuration) {
                 double value = sustain - (samplesSinceEnd / releaseDuration) * sustain;
                 return (float) value;
             } else {
                 return 0f;
             }
         }
     }
 
     private void normalizeOutput(int sampleSize) {
         if (outputBuffer.length != sampleSize)
             outputBuffer = new float[sampleSize];
     }
 
     public void setAttackMs(float attackMs) {
         this.attackMs = attackMs;
     }
 
     public void setDecayMs(float decayMs) {
         this.decayMs = decayMs;
     }
 
     public void setSustain(float sustain) {
         this.sustain = sustain;
     }
 
     public void setReleaseMs(float releaseMs) {
         this.releaseMs = releaseMs;
     }
 }
