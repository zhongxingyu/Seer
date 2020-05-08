 public class WaveFunc
 implements Func {
    private float scale  = 16f;
    private float period = 25f;
    private float freq   = TWO_PI/period;
 
    private static final float PI     = (float) Math.PI;
    private static final float TWO_PI = 2f * PI;
 
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
