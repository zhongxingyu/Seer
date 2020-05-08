 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.shape_func;
 
 import gnu.trove.list.array.TDoubleArrayList;
 import static java.lang.Math.PI;
 import static java.lang.Math.cos;
 import static java.lang.Math.sin;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import net.epsilony.mf.model.MFNode;
 import net.epsilony.tb.analysis.Math2D;
 import net.epsilony.tb.TestTool;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class MLSTest {
 
     public MLSTest() {
     }
     double[] sampleInterval = new double[]{1, 4};
 
     LinkedList<double[]> genSampleCoords(int numPerDim) {
         double[] v = TestTool.linSpace(sampleInterval[0], sampleInterval[1], numPerDim);
         LinkedList<double[]> coords = new LinkedList<>();
         for (double x : v) {
             for (double y : v) {
                 coords.add(new double[]{x, y});
             }
         }
         return coords;
     }
 
     public double[] polynomialSample(double[] xy) {
         double x = xy[0], y = xy[1];
         return new double[]{
             1.1 + 3 * x + 4.4 * y - x * x - 1.3 * x * y + 2.2 * y * y,
             3 - 2 * x - 1.3 * y,
             4.4 - 1.3 * x + 4.4 * y
         };
     }
 
     public double[] sin_cos_sample(double[] xy) {
         double cycle = 40;
         double par = 2 * PI / cycle;
         double x = xy[0], y = xy[1];
         double val = sin(x * par) * cos(y * par);
         double val_x = par * cos(x * par) * cos(y * par);
         double val_y = -par * sin(x * par) * sin(y * par);
         return new double[]{val, val_x, val_y};
     }
 
     public static interface SampFunc {
 
         double[] val(double[] xy);
     }
     SampFunc[] funcs = new SampFunc[]{
         new SampFunc() {
             @Override
             public double[] val(double[] xy) {
                 return polynomialSample(xy);
             }
         },
         new SampFunc() {
             @Override
             public double[] val(double[] xy) {
                 return sin_cos_sample(xy);
             }
         }
     };
 
     double[] randomRads(double rad, double range, int num) {
         Random rand = new Random();
         double[] res = new double[num];
         for (int i = 0; i < res.length; i++) {
             res[i] = rad + range * rand.nextDouble();
         }
         return res;
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testPartionOfUnity() {
 
         int numPerDim = 50;
         int testPerDim = 7;
         double rad_avg = (sampleInterval[1] - sampleInterval[0]) / numPerDim * 4;
         double range = rad_avg * 0.2;
         MLS mls = new MLS();
         mls.setDiffOrder(1);
         LinkedList<double[]> coords = genSampleCoords(numPerDim);
         LinkedList<double[]> testPts = genSampleCoords(testPerDim);
         TDoubleArrayList rads = new TDoubleArrayList(randomRads(rad_avg, range, coords.size()));
         double[] exp = new double[]{1, 0, 0};
         for (double[] pt : testPts) {
             List<MFNode> nodes = searchCoords(pt, coords, rads);
             TDoubleArrayList[] vals = mls.values(pt, nodes, null);
             double[] acts = new double[]{vals[0].sum(), vals[1].sum(), vals[2].sum()};
             assertArrayEquals(exp, acts, 1e-12);
         }
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testFitness() {
         int numPerDim = 50;
         int testPerDim = 7;
         double rad_avg = (sampleInterval[1] - sampleInterval[0]) / numPerDim * 4;
         double range = rad_avg * 0.2;
         boolean isVer = false;
         MLS mls = new MLS();
         mls.setDiffOrder(1);
         LinkedList<double[]> coords = genSampleCoords(numPerDim);
         LinkedList<double[]> testPts = genSampleCoords(testPerDim);
         TDoubleArrayList rads = new TDoubleArrayList(randomRads(rad_avg, range, coords.size()));
        double[] errs = new double[]{1e-10, 1e-5};
 
         for (double[] pt : testPts) {
             for (int i = 0; i < errs.length; i++) {
                 List<MFNode> nodes = searchCoords(pt, coords, rads);
 
                 TDoubleArrayList[] vals = mls.values(pt, nodes, null);
                 double[] acts = new double[]{0, 0, 0};
                 int j = 0;
                 for (MFNode node : nodes) {
                     double cv = funcs[i].val(node.getCoord())[0];
                     acts[0] += vals[0].get(j) * cv;
                     acts[1] += vals[1].get(j) * cv;
                     acts[2] += vals[2].get(j) * cv;
                     j++;
                 }
                 double[] exps = funcs[i].val(pt);
                 if (isVer) {
                     System.out.println("acts = " + Arrays.toString(acts) + "exps = " + Arrays.toString(acts));
                 }
                 assertArrayEquals(exps, acts, errs[i]);
             }
         }
     }
 
     public static List<MFNode> searchCoords(double[] center, List<double[]> coords, TDoubleArrayList rads) {
         List<MFNode> result = new LinkedList<>();
         int i = 0;
         for (double[] coord : coords) {
             double rad = rads.get(i);
             double dst = Math2D.distance(center, coord);
             if (dst < rad) {
                 MFNode node = new MFNode(coord);
                 node.setInfluenceRadius(rad);
                 node.setAssemblyIndex(i);
                 result.add(node);
             }
             i++;
         }
         return result;
     }
 }
