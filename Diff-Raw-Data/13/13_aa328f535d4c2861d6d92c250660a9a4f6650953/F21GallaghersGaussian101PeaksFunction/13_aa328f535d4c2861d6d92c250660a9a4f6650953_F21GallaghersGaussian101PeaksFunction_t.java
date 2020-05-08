 package evolution.real.functions;
 
 import evolution.RandomNumberGenerator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Martin Pilat
  * Date: 11.11.13
  * Time: 19:42
  * To change this template use File | Settings | File Templates.
  */
 public class F21GallaghersGaussian101PeaksFunction extends RealFunction {
 
     ArrayList<double[]> C;
     ArrayList<double[]> y;
     double[] w;
 
     public F21GallaghersGaussian101PeaksFunction(int D) {
         super(D);
         w = new double[101];
         w[0] = 10;
         for (int i = 1; i < 101; i++) {
             w[i] = 1.1 + 8.0 * (i - 1.0) / 99.0;
         }
     }
 
     @Override
     public void reinit() {
         double g1 = RandomNumberGenerator.getInstance().nextGaussian();
         double g2 = RandomNumberGenerator.getInstance().nextGaussian();
         fopt = Math.min(1000.0, Math.max(-1000.0, (Math.round(100.0 * 100.0 * g1 / g2) / 100.0)));
         R = getRandomRotationMatrix();
         ArrayList<Double> alphas = new ArrayList<Double>();
         for (int i = 0; i < 100; i++) {
             alphas.add(Math.pow(1000, 2.0 * i / 99));
         }
         C = new ArrayList<double[]>();
         double[] tmp = createLambda(1000);
         for (int i = 0; i < D; i++) {
             tmp[i] /= Math.pow(1000, 0.25);
         }
         ArrayList<Double> tmpDiag = new ArrayList<Double>();
         for (int i = 0; i < D; i++) {
             tmpDiag.add(tmp[i]);
         }
         Collections.shuffle(tmpDiag);
         for (int i = 0; i < D; i++) {
             tmp[i] = tmpDiag.get(i);
         }
         C.add(tmp);
         for (int k = 0; k < 100; k++) {
             int index = RandomNumberGenerator.getInstance().nextInt(alphas.size());
             double alpha = alphas.get(index);
             alphas.remove(index);
             tmp = createLambda(alpha);
             for (int i = 0; i < D; i++) {
                 tmp[i] /= Math.pow(alpha, 0.25);
             }
             tmpDiag = new ArrayList<Double>();
             for (int i = 0; i < D; i++) {
                 tmpDiag.add(tmp[i]);
             }
             Collections.shuffle(tmpDiag);
             for (int i = 0; i < D; i++) {
                 tmp[i] = tmpDiag.get(i);
             }
             C.add(tmp);
         }
         y = new ArrayList<double[]>();
         double[] ytmp = new double[D];
         for (int i = 0; i < D; i++) {
             ytmp[i] = RandomNumberGenerator.getInstance().nextDouble() * 4 - 8;
         }
         y.add(ytmp);
        xopt = ytmp;
         for (int j = 0; j < 100; j++) {
             ytmp = new double[D];
             for (int i = 0; i < D; i++) {
                 ytmp[i] = RandomNumberGenerator.getInstance().nextDouble() * 4 - 8;
             }
             y.add(ytmp);
         }
     }
 
     @Override
     public double value(double[] x) {
 
         double max = -Double.MAX_VALUE;
         for (int i = 0; i < 101; i++) {
             double[] z = mult(R, minus(x, y.get(i)));
             double[] c = diagMult(C.get(i), z);
             double sum = 0.0;
             for (int k = 0; k < D; k++) {
                 sum += z[k] * c[k];
             }
             double tmp = -1.0 / (2.0 * D) * sum;
             tmp = w[i] * Math.exp(tmp);
             max = Math.max(max, tmp);
         }
 
         return Tosz(new double[]{Math.pow(10 - max, 2)})[0] + fpen(x) + fopt;
     }
 
 }
