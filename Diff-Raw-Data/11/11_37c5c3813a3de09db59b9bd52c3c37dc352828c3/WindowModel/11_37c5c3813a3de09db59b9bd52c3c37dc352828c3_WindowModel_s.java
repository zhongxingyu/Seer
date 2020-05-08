 package cs224n.deep;
 import java.lang.*;
 import java.util.*;
 
 import org.ejml.data.*;
 import org.ejml.ops.CommonOps;
 import org.ejml.ops.SpecializedOps;
 import org.ejml.simple.*;
 
 
 import java.text.*;
 
 public class WindowModel {
     public static final String START_TOKEN = "<s>";
     public static final String END_TOKEN = "</s>";
     
     public final boolean gradCheck;
 
 	protected SimpleMatrix L, W, b1, U;
 	double b2;
 	public int windowSize, wordSize, hiddenSize;
 	public double learningRate;
 	public double C = 0;
   
   
 	protected Map<String, Integer> wordToNum;
 
 	public WindowModel(int _windowSize, int _hiddenSize, double _lr, SimpleMatrix L,
 	                   Map<String, Integer> wordToNum){
       this(_windowSize, _hiddenSize, _lr, L, wordToNum, false);
 	}
   
 	public WindowModel(int _windowSize, int _hiddenSize, double _lr, SimpleMatrix L,
 	                   Map<String, Integer> wordToNum, boolean gradCheck){
       this.L = L;
       this.wordToNum = wordToNum;
       
 	  windowSize = _windowSize;
 	  wordSize = L.numRows();
       learningRate = _lr;
       hiddenSize = _hiddenSize;
       
       this.gradCheck = gradCheck;
 	}
 
 	/**
 	 * Initializes the weights randomly. 
 	 */
 	public void initWeights(){
       Random rand = new Random();
       double e = Math.sqrt(6.0/(wordSize*windowSize+hiddenSize));
       
       W = SimpleMatrix.random(hiddenSize, windowSize*wordSize, -e, e, rand);
       b1 = new SimpleMatrix(hiddenSize, 1);
       b1.zero();
       
       //U = SimpleMatrix.random(hiddenSize, 1, -0.1, 0.1, rand);
       U = new SimpleMatrix(hiddenSize, 1);
       U.zero();
       b2 = 0.0;
       
       C = C/(W.getNumElements()+U.getNumElements());
 	}
 
 
 	/**
 	 * Simplest SGD training 
 	 */
 	public void train(List<Datum> trainData ){
 	  int numTrainWords = trainData.size();
       SimpleMatrix trainExample = new SimpleMatrix(windowSize*wordSize, 1);
 
       for (int epoch = 0; epoch < 2; epoch++) {
         System.out.println("Iteration - " + epoch);
 
         for (int i = 0; i < numTrainWords; i++) {
           List<Integer> windowIndices = getWindowIndices(trainData, i);
           String label = trainData.get(i).label;
           int y = label.equals("PERSON") ? 1 : 0;
 
           getWordVecs(trainExample, windowIndices);
          PropagationResult res = forwardProp(trainExample, true, y, false);
 
           if (gradCheck)
             doGradCheck(res, trainExample, y);
 
           // gradient descent
          U.minus(res.gradU.scale(learningRate));
          W.minus(res.gradW.scale(learningRate));
          b1.minus(res.gradb1.scale(learningRate));
           b2 -= res.gradb2 * learningRate;
 
           // apply L update
           SimpleMatrix LUpdate = res.gradL.scale(learningRate);
           for (int j = 0; j < windowIndices.size(); j++) {
             int index = windowIndices.get(j);
             SimpleMatrix update = LUpdate.extractMatrix(j*L.numRows(), (j+1)*L.numRows(), 0, 1);
             L.insertIntoThis(0, index, L.extractVector(false, index).minus(update));
           }
         }
       }
 	}
   
 	public void test(List<Datum> testData){
 	  int numWords = testData.size();
       SimpleMatrix x = new SimpleMatrix(windowSize*wordSize, 1);
       
       int truePositives = 0;
       int predictedPositives = 0;
       int goldPositives = 0;      
 	  
 	  for (int i = 0; i < numWords; i++) {
         String label = testData.get(i).label;
         
         List<Integer> windowIndices = getWindowIndices(testData, i);
         getWordVecs(x, windowIndices);
         PropagationResult res = forwardProp(x, false, 0, false);
         
         String prediction = (res.h >= 0.5) ? "PERSON" : "O";
         
         if (prediction.equals("PERSON")) {
           predictedPositives++;
           
           if (label.equals("PERSON"))
             truePositives++;
         }
         
         if (label.equals("PERSON"))
           goldPositives++;
 	  }
     
 	  double precision = (double)truePositives/predictedPositives;
 	  double recall = (double)truePositives/goldPositives;
       double f1 = (2.0*precision*recall)/(precision+recall);
       
       System.out.println("F1 score - " + f1);
 	}
   
   
     public PropagationResult forwardProp(SimpleMatrix x, boolean calcGrad, int y, boolean calcCost) {
         // forward prop
         SimpleMatrix z = W.mult(x).plus(b1);
         SimpleMatrix a = z.copy();
         applyTanh(a);
         
         double ua = U.dot(a);
         double h = sigmoid(ua+b2);
         
         PropagationResult res = new PropagationResult(h);
         
         if (calcGrad) {
           double coeff = (-y)+h;
           SimpleMatrix a_prime = a.copy();
           CommonOps.elementMult(a_prime.getMatrix(), a_prime.getMatrix());
           CommonOps.changeSign(a_prime.getMatrix());
           CommonOps.add(a_prime.getMatrix(), 1.0);
           
           SimpleMatrix delta = U.scale(coeff);
           CommonOps.elementMult(delta.getMatrix(), a_prime.getMatrix());
           
           res.gradb1 = delta;
           res.gradb2 = coeff;
           res.gradW = delta.mult(x.transpose()).plus(W.scale(C));
           res.gradU = a.scale(coeff).plus(U.scale(C));
           res.gradL = delta.transpose().mult(W).transpose();
         }
         
         // calculate cost
         if (calcCost) {
           res.cost = (-y)*Math.log(h)-(1-y)*Math.log(1-h) + 
               (C/2.0)*(SpecializedOps.elementSumSq(W.getMatrix()) + SpecializedOps.elementSumSq(U.getMatrix()));
         }
         
         return res;
     }
     
     public void doGradCheck(PropagationResult res, SimpleMatrix x, int y) {
       double diff = 0.0;
       final double eps = 1e-4;
       
       double c1, c2, grad, graddiff, org;
       
       for (int i = 0; i < x.getNumElements(); i++) {
         org = x.get(i);
         x.set(i, org+eps);
         c1 = forwardProp(x, false, y, true).cost;
         x.set(i, org-eps);
         c2 = forwardProp(x, false, y, true).cost;
         
         grad = (c1-c2) / (2*eps);
         graddiff = Math.abs(res.gradL.get(i) - grad);
         diff += graddiff * graddiff;
         
         x.set(i, org);
       }
       
       for (int i = 0; i < W.getNumElements(); i++) {
         org = W.get(i);
         W.set(i, org+eps);
         c1 = forwardProp(x, false, y, true).cost;
         W.set(i, org-eps);
         c2 = forwardProp(x, false, y, true).cost;
         
         grad = (c1-c2) / (2*eps);
         graddiff = Math.abs(res.gradW.get(i) - grad);
         diff += graddiff * graddiff;
         
         W.set(i, org);
       }
       
       for (int i = 0; i < U.getNumElements(); i++) {
         org = U.get(i);
         U.set(i, org+eps);
         c1 = forwardProp(x, false, y, true).cost;
         U.set(i, org-eps);
         c2 = forwardProp(x, false, y, true).cost;
         
         grad = (c1-c2) / (2*eps);
         graddiff = Math.abs(res.gradU.get(i) - grad);
         diff += graddiff * graddiff;
         
         U.set(i, org);
       }
       
       for (int i = 0; i < b1.getNumElements(); i++) {
         org = b1.get(i);
         b1.set(i, org+eps);
         c1 = forwardProp(x, false, y, true).cost;
         b1.set(i, org-eps);
         c2 = forwardProp(x, false, y, true).cost;
         
         grad = (c1-c2) / (2*eps);
         graddiff = Math.abs(res.gradb1.get(i) - grad);
         diff += graddiff * graddiff;
         
         b1.set(i, org);
       }
       
       org = b2;
       b2 += eps;
       c1 = forwardProp(x, false, y, true).cost;
       b2 = org - eps;
       c2 = forwardProp(x, false, y, true).cost;
         
       grad = (c1-c2) / (2*eps);
       graddiff = Math.abs(res.gradb2 - grad);
       diff += graddiff * graddiff;
         
       b2 = org;
       
       System.out.print(diff);
       System.out.println(diff < 1e-7 ? " Ok" : " Fail");
     }
 	
 	private void applyTanh(SimpleMatrix x) {
       int numElements = x.getNumElements();
       
       for (int i = 0; i < numElements; i++) {
         double elem = x.get(i);
         x.set(i, Math.tanh(elem));
       }
 	}
   
 	private double sigmoid(double x) {
       return 1.0 / (1 + Math.exp(-x));
 	}
   
     private void getWordVecs(SimpleMatrix x, List<Integer> indices) {
       for (int i = 0; i < indices.size(); i++) {
         x.insertIntoThis(i*wordSize, 0, L.extractVector(false, indices.get(i)));
       }
     }
 	
 	private List<Integer> getWindowIndices(List<Datum> trainData, int center) {
       List<Integer> indices = new ArrayList<Integer>(windowSize);
       
       for (int current = center-windowSize/2; current <= center+windowSize/2; current++) {
         String word;
         
         if (current < 0) {
           word = START_TOKEN;
         }
         else if (current >= trainData.size()) {
           word = END_TOKEN;
         }
         else
           word = trainData.get(current).word;
         
         if (wordToNum.containsKey(word))
           indices.add(wordToNum.get(word));
         else
           indices.add(0);
       }
       
       return indices;
 	}
   
 	public class PropagationResult {
       public double h;
       public SimpleMatrix gradW, gradb1, gradL, gradU;
       public double gradb2;
       public double cost;
       
       
       public PropagationResult(double h) {
         this.h = h;
       }
 	}
 }
