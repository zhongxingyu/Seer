 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase.evaluation;
 
 import mase.EvaluationResult;
 import java.util.Arrays;
 import net.jafama.FastMath;
 
 /**
  *
  * @author jorge
  */
 public class VectorBehaviourResult implements BehaviourResult {
 
     protected float[] behaviour;
     protected Distance dist;
 
     public enum Distance {
 
         COSINE, BRAY_CURTIS, EUCLIDEAN
     };
 
     public VectorBehaviourResult(float... bs) {
         this.behaviour = bs;
     }
     
     public VectorBehaviourResult(Distance dist, float... bs) {
         this.behaviour = bs;
         this.dist = dist;
     }
 
     @Override
     public float distanceTo(BehaviourResult other) {
         return vectorDistance(behaviour, (float[]) other.value());
     }
 
     @Override
     public Object value() {
         return getBehaviour();
     }
 
     public void setBehaviour(float[] b) {
         this.behaviour = b;
     }
 
     public float[] getBehaviour() {
         return this.behaviour;
     }
 
     @Override
     public EvaluationResult mergeEvaluations(EvaluationResult[] results) {
         float[] merged = new float[behaviour.length];
         Arrays.fill(merged, 0f);
         for (int i = 0; i < merged.length; i++) {
             for (EvaluationResult r : results) {
                 merged[i] += ((float[]) r.value())[i];
             }
             merged[i] /= results.length;
         }
         return new VectorBehaviourResult(merged);
     }
 
     @Override
     public String toString() {
         String res = "";
         for (int i = 0; i < behaviour.length - 1; i++) {
             res += behaviour[i] + " ";
         }
         res += behaviour[behaviour.length - 1];
         return res;
     }
 
     public float vectorDistance(float[] v1, float[] v2) {
         switch (dist) {
             case BRAY_CURTIS:
                 float diffs = 0;
                 float total = 0;
                 for (int i = 0; i < v1.length; i++) {
                     diffs += Math.abs(v1[i] - v2[i]);
                     total += v1[i] + v2[i];
                 }
                 return diffs / total;
             case COSINE:
                 return cosineSimilarity(v1, v2);
             default:
             case EUCLIDEAN:
                 float d = 0;
                 for (int i = 0; i < v1.length; i++) {
                     d += FastMath.pow(v1[i] - v2[i], 2);
                 }
                 return (float) FastMath.sqrtQuick(d);
         }
     }
 
     private float cosineSimilarity(float[] docVector1, float[] docVector2) {
         float dotProduct = 0.0f;
         float magnitude1 = 0.0f;
         float magnitude2 = 0.0f;
         float cosineSimilarity;
         for (int i = 0; i < docVector1.length; i++) {
             dotProduct += docVector1[i] * docVector2[i];  //a.b
             magnitude1 += FastMath.pow(docVector1[i], 2);  //(a^2)
             magnitude2 += FastMath.pow(docVector2[i], 2); //(b^2)
         }
         magnitude1 = (float) FastMath.sqrtQuick(magnitude1);//sqrt(a^2)
         magnitude2 = (float) FastMath.sqrtQuick(magnitude2);//sqrt(b^2)
 
         if (magnitude1 != 0.0 | magnitude2 != 0.0) {
             cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
         } else {
             return 0.0f;
         }
         return cosineSimilarity;
     }
 }
