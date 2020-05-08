 public class WaveFunc
 implements Func {
    protected float scale  = 16f;
    protected float period = 20f;
    protected float freq;
 
    protected static final float PI     = (float) Math.PI;
    protected static final float TWO_PI = 2f * PI;

    protected void calcFreq() {
        freq = TWO_PI/period;
    }

    public WaveFunc() {
        calcFreq();
    }
 
     public float valueAt(float x, float z) {
         return (float) (scale * Math.sin(freq * x));
     }
 
     public float[] normalAt(float x, float z) {
         return new float[] {
             (float) (freq * scale * Math.cos(freq * x)),
             0,
             0
         };
     }
 }
