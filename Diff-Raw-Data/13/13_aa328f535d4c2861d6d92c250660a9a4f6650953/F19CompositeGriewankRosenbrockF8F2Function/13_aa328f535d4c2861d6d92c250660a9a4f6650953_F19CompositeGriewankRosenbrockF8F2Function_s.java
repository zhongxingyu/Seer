 package evolution.real.functions;
 
 import evolution.RandomNumberGenerator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Martin Pilat
  * Date: 11.11.13
  * Time: 19:42
  * To change this template use File | Settings | File Templates.
  */
 public class F19CompositeGriewankRosenbrockF8F2Function extends RealFunction {
 
     double scale;
 
     public F19CompositeGriewankRosenbrockF8F2Function(int D) {
         super(D);
         scale = Math.max(1, Math.sqrt(D) / 8.0);
     }
 
     @Override
     public void reinit() {
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
 
         double[] s = new double[D - 1];
         for (int i = 0; i < D - 1; i++) {
             s[i] = 100 * Math.pow((z[i] * z[i] - z[i + 1]), 2) + Math.pow(z[i] - 1, 2);
         }
 
         double sum = 0;
         for (double d : s) {
             sum += d / 4000.0 - Math.cos(d);
         }
 
         return 10.0 / (D - 1) * sum + 10.0 + fopt;
     }
 
 }
