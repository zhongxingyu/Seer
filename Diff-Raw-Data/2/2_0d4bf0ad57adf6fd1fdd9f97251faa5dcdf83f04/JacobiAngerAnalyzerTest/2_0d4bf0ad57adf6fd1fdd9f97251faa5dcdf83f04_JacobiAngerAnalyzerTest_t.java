 package org.sade.analyzers;
 
 import org.hamcrest.CoreMatchers;
 import org.hamcrest.Matchers;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class JacobiAngerAnalyzerTest
 {
     private final int PointNum = 512;
     JacobiAngerAnalyzer analyzer = new JacobiAngerAnalyzer();
 
     @Test
     public void Gutter()
     {
         double omega = 3;
         double delta = 0.4;
         double phi = 0.7;
         double[] sample = PrepeareSample(1, omega, Math.PI*2/PointNum, phi, delta, 0, PointNum);
         MinimizeParameters parameters = analyzer.AnalyzeSample(sample, 3, 0.5, 1).getParameters();
         AssertParameters(omega, phi, delta, parameters, 1E-1);
     }
 
     private void AssertParameters(double omega, double phi, double delta, MinimizeParameters parameters, double precision)
     {
         Assert.assertThat(parameters.getDelta(), Matchers.closeTo(delta, precision));
         Assert.assertThat(parameters.getPhi(), Matchers.closeTo(phi, precision));
         Assert.assertThat(parameters.getOmega(), Matchers.closeTo(omega, precision));
     }
 
     @Test
     public void Scaling()
     {
         int pointNum = 500;
         double[] sample = PrepeareSample(1, 3, Math.PI * 2 / pointNum, 0.7, 0.4, 0, pointNum);
         MinimizeParameters parameters = analyzer.AnalyzeSample(sample, 3, 0.4, 0.7).getParameters();
         AssertParameters(3, 0.7, 0.4, parameters, 1E-1);
     }
 
     @Test
     public void Chunk()
     {
         double omega = 2.5;
         double phi = 0.7;
         double delta = 1.5;
         double[] sample = PrepeareSample(omega, omega, Math.PI * 2 / 200, phi, delta, 2, 20000);
         List<AnalyzeResult> parameters = new ArrayList<AnalyzeResult>();
        final int chunkSize = 8192;
         long now = System.currentTimeMillis();
         for (double[] chunkSample = sample; chunkSample.length > 0; chunkSample = JacobiAngerAnalyzer.skip(chunkSample, chunkSize))
         {
             parameters.addAll(analyzer.DivideAndAnalyze(JacobiAngerAnalyzer.take(chunkSample, chunkSize)));
             System.out.println("Chunk elapsed time: " + (System.currentTimeMillis() - now)/1000.0);
             now = System.currentTimeMillis();
         }
         Assert.assertThat(parameters.size(), CoreMatchers.equalTo(98));
         for (AnalyzeResult parameter : parameters)
         {
             AssertParameters(omega, phi, delta, parameter.getParameters(), 1E-1);
         }
     }
 
     @Test
     public void Stability()
     {
         int n = 6;
         for (int i = 0; i < n; i++)
         {
             for (int j = 0; j < n; j++)
             {
                 double phi = Math.PI/3*i/n;
                 double delta = Math.PI/3*j/n;
                 double[] sample = PrepeareSample(1, 3, Math.PI * 2 / PointNum, phi, delta, 0, PointNum);
                 MinimizeParameters parameters = analyzer.AnalyzeSample(sample, 2.5, Math.PI/6, Math.PI/6).getParameters();
                 System.out.println("Stability: i="+i+", j="+j);
                 Assert.assertThat(parameters.getOmega(), Matchers.closeTo(3, 2E-1));
                 Assert.assertThat(parameters.getPhi(), Matchers.closeTo(phi, 1E-1));
                 Assert.assertThat(parameters.getDelta(), Matchers.closeTo(delta, 1E-1));
             }
         }
     }
 
     @Test
     public void ShiftAndAmplitude()
     {
         double omega = 3;
         double delta = 0.4;
         double phi = 0.7;
         double[] sample = PrepeareSample(10, omega, Math.PI * 2 / PointNum, phi, delta, 5, PointNum);
         MinimizeParameters parameters = analyzer.AnalyzeSample(sample, 2.5, 0.2, 0.3).getParameters();
         AssertParameters(omega, phi, delta, parameters, 1E-1);
     }
 
     public static double[] PrepeareSample(double amplitude, double phaseAmplitude, double omega, double phi,
                                           double delta, double shift, int pointNum) {
         return TestSample.prepareSample(amplitude, phaseAmplitude, omega, phi, delta, shift, pointNum);
     }
 }
