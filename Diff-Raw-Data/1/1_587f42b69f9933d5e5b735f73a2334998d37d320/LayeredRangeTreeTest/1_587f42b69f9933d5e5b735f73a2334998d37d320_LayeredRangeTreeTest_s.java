 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.rangesearch;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import net.epsilony.tsmf.util.DoubleArrayComparator;
 import net.epsilony.tsmf.util.TestTool;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author Man YUAN <epsilonyuan@gmail.com>
  */
 public class LayeredRangeTreeTest {
 
     public LayeredRangeTreeTest() {
     }
 
     /**
      * Test of rangeSearch method, of class LayeredRangeTree.
      */
     @Test
     public void testRangeSearch2D() {
         System.out.println("rangeSearch2D");
         List<double[]> samples = new LinkedList<>();
         double[][] lim = new double[][]{{-1.1, 1.1}, {-1.1, 1.1}};
         int numPerDim = 40;
         int washRatio = 1;
         final int dim = 2;
         int testTime = 10000;
         int testShakeGap = 10;
         double shakeRaius = 2.0 / numPerDim / 3;
         double sampleVibrationRatio = 1;
 
         ArrayList<double[]> samplePosByDim = new ArrayList<>(dim);
         for (int i = 0; i < dim; i++) {
             double[] samplePt = TestTool.linSpace(lim[i][0], lim[i][1], numPerDim);
             samplePosByDim.add(samplePt);
         }
         for (double d1 : samplePosByDim.get(0)) {
             for (double d2 : samplePosByDim.get(1)) {
                 samples.add(new double[]{d1, d2});
             }
         }
 
         TestTool.wash(samples, samples.size() * washRatio);
 
         ArrayList<Comparator<double[]>> comps = new ArrayList<>();
         for (int i = 0; i < dim; i++) {
             comps.add(new DoubleArrayComparator(i));
         }
 
         LayeredRangeTree<double[]> lrTree = new LayeredRangeTree(samples, comps);
 
         double[] from = new double[]{-0.1, 0.1};
         double[] to = new double[]{0.45, 0.54};
 
         ArrayList<double[]> acts = new ArrayList<>();
         for (int i = 0; i < testTime; i++) {
             try {
                 lrTree.rangeSearch(acts, from, to);
                 LinkedList<double[]> exps = rangeSearch(samples, from, to);
                 DictComparator<double[]> dComp = new DictComparator<>(comps, false, 0);
                 Collections.sort(acts, dComp);
                 Collections.sort(exps, dComp);
                 //System.out.println("acts.size(), exps.size() = " + acts.size() + ", " + exps.size());
 
                 // just for debug
                 if (acts.size() != exps.size()) {
                     lrTree.rangeSearch(acts, from, to);
                 }
                 for (double[] act : acts) {
                     double[] exp = exps.pollFirst();
                     assertArrayEquals(exp, act, 1e-14);
                 }
                 double[][] fromTo = randomTestRang(lim, sampleVibrationRatio);
                 from = fromTo[0];
                 to = fromTo[1];
                 if (i % testShakeGap == 0) {
                     samples = shakeSamples(shakeRaius, samples);
                     lrTree = new LayeredRangeTree<>(samples, comps);
                 }
             } catch (Throwable t) { // just for debug
                 LinkedList<double[]> exps = rangeSearch(samples, from, to);
                 DictComparator<double[]> dComp = new DictComparator<>(comps, false, 0);
                 Collections.sort(exps, dComp);
                 lrTree.rangeSearch(acts, from, to);
             }
         }
     }
 
     /**
      * Test of rangeSearch method, of class LayeredRangeTree.
      */
     @Test
     public void testRangeSearch3D() {
         System.out.println("rangeSearch3D");
         List<double[]> samples = new LinkedList<>();
         double[][] lim = new double[][]{{-1.0, 1.0}, {-2.0, 2.0}, {-3.0, 3.0}};
         int numPerDim = 13;
         int washRatio = 1;
         final int dim = 3;
         int testTime = 100;
         int testShakeGap = 10;
         double shakeRaius = 2.0 / numPerDim / 3;
 
         double sampleVibrationRatio = 1;
 
         ArrayList<double[]> samplePosByDim = new ArrayList<>(dim);
         for (int i = 0; i < dim; i++) {
             double[] samplePt = TestTool.linSpace(lim[i][0], lim[i][1], numPerDim);
             samplePosByDim.add(samplePt);
         }
         for (double d1 : samplePosByDim.get(0)) {
             for (double d2 : samplePosByDim.get(1)) {
                 for (double d3 : samplePosByDim.get(2)) {
                     samples.add(new double[]{d1, d2, d3});
                 }
             }
         }
 
         TestTool.wash(samples, samples.size() * washRatio);
 
         ArrayList<Comparator<double[]>> comps = new ArrayList<>();
         for (int i = 0; i < dim; i++) {
             comps.add(new DoubleArrayComparator(i));
         }
 
         LayeredRangeTree<double[]> lrTree = new LayeredRangeTree(samples, comps);
 
         double[] from = new double[]{-0.1, -1.1, 0.0};
         double[] to = new double[]{0.5, 0.5, 0.6};
 
         ArrayList<double[]> acts = new ArrayList<>();
         for (int i = 0; i < testTime; i++) {
             lrTree.rangeSearch(acts, from, to);
             LinkedList<double[]> exps = rangeSearch(samples, from, to);
             DictComparator<double[]> dComp = new DictComparator<>(comps, false, 0);
             Collections.sort(acts, dComp);
             Collections.sort(exps, dComp);
             //System.out.println("acts.size(), exps.size() = " + acts.size() + ", " + exps.size());
             for (double[] act : acts) {
                 double[] exp = exps.pollFirst();
                 assertArrayEquals(exp, act, 1e-14);
             }
             double[][] fromTo = randomTestRang(lim, sampleVibrationRatio);
             from = fromTo[0];
             to = fromTo[1];
             if (i > 0 && i % testShakeGap == 0) {
                 samples = shakeSamples(shakeRaius, samples);
                 lrTree = new LayeredRangeTree<>(samples, comps);
             }
         }
     }
 
     public LinkedList<double[]> rangeSearch(List<double[]> samples, double[] from, double[] to) {
         LinkedList<double[]> res = new LinkedList<>();
         for (double[] ds : samples) {
             boolean add = true;
             for (int i = 0; i < ds.length; i++) {
                 if (from[i] > ds[i] || to[i] < ds[i]) {
                     add = false;
                     break;
                 }
             }
             if (add) {
                 res.add(ds);
             }
         }
         return res;
     }
 
     public double[][] randomTestRang(double[][] dataRange, double vib) {
         double[] from = new double[dataRange.length];
         double[] to = new double[dataRange.length];
         Random rand = new Random();
         for (int i = 0; i < dataRange.length; i++) {
             double d1 = rand.nextDouble();
             double d2 = rand.nextDouble();
             double rgFrom = dataRange[i][0];
             double rgTo = dataRange[i][1];
             double rgW = rgTo - rgFrom;
             rgFrom = rgFrom - rgW * vib / 2;
             rgW = rgW * (1 + vib);
             d1 = rgFrom + rgW * d1;
             d2 = rgFrom + rgW * d2;
             if (d1 <= d2) {
                 from[i] = d1;
                 to[i] = d2;
             } else {
                 from[i] = d2;
                 to[i] = d1;
             }
         }
         return new double[][]{from, to};
     }
 
     public List<double[]> shakeSamples(double radius, List<double[]> sample) {
         ArrayList<double[]> result = new ArrayList<>(sample.size());
         Random rand = new Random();
         for (double[] s : sample) {
             double[] new_s = new double[s.length];
             for (int i = 0; i < s.length; i++) {
                 new_s[i] = s[i] + (rand.nextDouble() - 0.5) * 2 * radius;
             }
             result.add(new_s);
         }
         return result;
     }
 }
