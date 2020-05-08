 package evolution.real.functions;
 
 import evolution.RandomNumberGenerator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Martin Pilat
  * Date: 11.11.13
  * Time: 19:42
  * To change this template use File | Settings | File Templates.
  */
 public class F09RosenbrockRotatedFunction extends RealFunction {
 
     double scale;
 
     public F09RosenbrockRotatedFunction(int D) {
         super(D);
         scale = Math.max(1, Math.sqrt(D) / 8.0);
     }
 
     @Override
     public void reinit() {
        xopt = new double[D];
         double g1 = RandomNumberGenerator.getInstance().nextGaussian();
         double g2 = RandomNumberGenerator.getInstance().nextGaussian();
         fopt = Math.min(1000.0, Math.max(-1000.0, (Math.round(100.0 * 100.0 * g1 / g2) / 100.0)));
         R = getRandomRotationMatrix();
     }
 
     @Override
     public double value(double[] x) {
         double[] z = mult(R, x);
 
         for (int i = 0; i < D; i++) {
             z[i] = scale * z[i] + 0.5;
         }
 
         double sum = 0;
         for (int i = 0; i < D - 1; i++) {
             sum += 100 * Math.pow((z[i] * z[i] - z[i + 1]), 2) + Math.pow(z[i] - 1, 2);
         }
 
         return sum + fopt;
     }
 
 }
