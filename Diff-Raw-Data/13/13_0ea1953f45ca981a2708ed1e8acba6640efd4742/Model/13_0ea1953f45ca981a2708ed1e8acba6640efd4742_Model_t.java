 package gibbs;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.RoundingMode;
 import java.util.HashSet;
 
import semsup.eval.Constants;
 import cc.mallet.types.Dirichlet;
 import data.Alphabet;
 import data.Dataset;
 import data.Instance;
 import em.implementation.EmModel;
 
 /**
  * Implements a naive bayes model for a gibbs sampler.
  * 
  * @author dmitriy dligach
  */
 public class Model {
 
   public static final int numSamples = 50;
   public static final boolean sampleThetaAtInit = true;
 
   // hyperparameters of beta distribution
   public static final double[] betaParams = {1, 1};  
   // hyperparameters of dirichlet distribution
   public static final double[] dirichletParams = {1, 1};
 
   // number of classes
   protected int numClasses;
   // number of words (features)
   protected int numWords;
   // number of examples in training data
   protected int numInstances;
 
   // word (feature) counts for each class
   protected double[][] wordCounts;
   // number of examples for each class
   protected int[] labelCounts;
   // total word (feature) count in each class
   private int[] totalClassWords;
 
   // p(w|c) for all classes 
   protected double[][] theta;
 
   // map labels to ints and ints to labels
   Alphabet labelAlphabet;
   // word to ints mapping
   Alphabet featureAlphabet;
 
   // may not need a class member
   public Dataset labeled;
   public Dataset unlabeled;
   public Dataset test;
   public Dataset sampled; 
 
   /**
    * Constructor.
    */
   public Model(Dataset labeled, Dataset unlabeled, Dataset test, Alphabet labelAlphabet, Alphabet featureAlphabet) {
 
     this.labeled = labeled;
     this.unlabeled = unlabeled;
     this.test = test;
 
     this.labelAlphabet = labelAlphabet;
     this.featureAlphabet = featureAlphabet;
 
     numClasses = labelAlphabet.size();
     numWords = featureAlphabet.size(); 
     numInstances = labeled.size() + unlabeled.size() + test.size();
 
     labelCounts = new int[numClasses];
     wordCounts = new double[numClasses][numWords];
     totalClassWords = new int[numClasses];
 
     theta = new double[numClasses][numWords];
   }
 
   /**
    * Set initial model parameters and counts.
    */
   public void initialize() {
 
     // label unlabeled examples
     labeled.setInstanceClassProbabilityDistribution(new HashSet<String>(labelAlphabet.getStrings()));
     labeled.setAlphabets(labelAlphabet, featureAlphabet);
     labeled.makeVectors();
 
    EmModel classifier = new EmModel(labelAlphabet, Constants.defaultLambda);
     classifier.train(labeled);
     
     unlabeled.setAlphabets(labelAlphabet, featureAlphabet);
     test.setAlphabets(labelAlphabet, featureAlphabet);
     unlabeled.makeVectors();
     test.makeVectors();
     
     classifier.label2(unlabeled);
     classifier.label2(test);
 
     Dataset all = new Dataset(labeled.getInstances(), unlabeled.getInstances(), test.getInstances());
     all.setAlphabets(labelAlphabet, featureAlphabet);
     all.makeVectors();
 
     // compute counts
     computeLabelCounts(all);
     computeWordCounts(all);
     computeTotalClassWords(all);
 
     // compute p(w|c)
     if(sampleThetaAtInit) {
       sampleTheta();
     } else {
       computeTheta();
     }
 
     // wrap instances that need to be sampled into a dataset object
     sampled = new Dataset(unlabeled.getInstances(), test.getInstances());
     sampled.setAlphabets(labelAlphabet, featureAlphabet);
     sampled.makeVectors();
   }
   
   /**
    * A single sampling iteration.
    */
   public void sample() {
 
     for(Instance instance : sampled.getInstances()) {
 
       // subtract this instance's word counts and label counts
       int oldLabel = labelAlphabet.getIndex(instance.getLabel());
       labelCounts[oldLabel]--;
       for(int word = 0; word < numWords; word++) {
         Float wordCount = instance.getDimensionValue(word);
         if(wordCount == null) {
           continue;
         }
         wordCounts[oldLabel][word] -= wordCount;
       }
 
       double[] logSum = getUnnormalizedLogProbForClasses(instance); 
       double[] p = logToProb(logSum);
       int newLabel = Math.random() < p[0] ? 0 : 1;
       instance.setLabel(labelAlphabet.getString(newLabel));
 
       // keep track of labels for the test set 
       // assumption: test set has gold labels in 'temp' field
       if(instance.getTemp() != null) {
         instance.addToSequence(newLabel);
       }
 
       // add counts back
       labelCounts[newLabel]++;
       for(int word = 0; word < numWords; word++) {
         Float wordCount = instance.getDimensionValue(word);
         if(wordCount == null) {
           continue;
         }
         wordCounts[newLabel][word] += wordCount;
       }
 
       sampleTheta();
     }
   }
 
   /**
    * Equation 49 from "Gibbs Sampling for the Uninitiated" by Resnik and Hardisty (June 2010 version).
    * Compute log[p(c)p(w_0|c)...p(w_n-1|c)] = log[p(c)] + log[p(w_0|c)] + ... + log[p(w_n-1|c)]
    */
   private double[] getUnnormalizedLogProbForClasses(Instance instance){
 
     double[] logSum = new double[numClasses];
 
     for(int label = 0; label < numClasses; label++) {
 
       double outer = (labelCounts[label] + betaParams[label] - 1) / 
           (double)(numInstances + betaParams[0] + betaParams[1] - 1);
 
       logSum[label] = Math.log10(outer);
 
       for(int word = 0; word < numWords; word++) {
         Float wordCount = instance.getDimensionValue(word);
         if(wordCount == null) {
           continue;
         }
 
         double inner = wordCount * Math.log10(theta[label][word]);
         logSum[label] += inner;
       }
     }
 
     return logSum;
   }
 
   /**
    * Convert unnormalized log probabilities to probabilities for each class.
    */
   public double[] logToProb(double[] unnormalizedClassLogProbs) {
 
     double[] probs = new double[unnormalizedClassLogProbs.length];
 
     // unnormalized probabilities are often very small, e.g. 10^-802.345
     // which causes an underflow, so need to use big decimal instead
     BigDecimal[] unnormalizedClassProbs = new BigDecimal[unnormalizedClassLogProbs.length];
 
     // we have log(p(c)p(w_0|c)...p(w_n-1|c)) for each class
     // compute unnormalized probabilities as 10^(p(c)p(w_0|c)...p(w_n-1|c))
     for(int label = 0; label < numClasses; label++) {
       unnormalizedClassProbs[label] = powerOfTen(unnormalizedClassLogProbs[label]);
     }
 
     // compute the normalization constant
     BigDecimal normalizer = new BigDecimal(0);
     for(int label = 0; label < numClasses; label++) {
       normalizer = normalizer.add(unnormalizedClassProbs[label]);
     }
 
     for(int label = 0; label < numClasses; label++) {
       probs[label] = unnormalizedClassProbs[label].divide(normalizer, RoundingMode.HALF_UP).doubleValue();
     }
 
     return probs;
   }
 
   /**
    * Calculate 10^exponent when y is very small and fractional.
    * Basically split exponent into its integer i and fractional f parts.
    * I.e. 10^exponent = 10^(i + f) = 10^i * 10^f
    */
   private static BigDecimal powerOfTen(double exponent) {
 
     BigDecimal exponentAsBigDecimal = new BigDecimal(String.valueOf(exponent));
     BigDecimal integerPart = new BigDecimal(exponentAsBigDecimal.intValue());
     BigDecimal fractionalPart = exponentAsBigDecimal.subtract(integerPart);
 
     BigDecimal tenToIntegerPart = new BigDecimal(BigInteger.valueOf(10), -1 * integerPart.intValue());
     BigDecimal tenToFractionalPart = new BigDecimal(Math.pow(10, fractionalPart.doubleValue()));
 
     BigDecimal result = tenToIntegerPart.multiply(tenToFractionalPart);
 
     return result;
   }
 
   /**
    * Compute p(w|c) parameters.
    * 
    * Smoothing is needed because feature alphabet is computed based
    * on the entire dataset (i.e. all classes). If no smoothing is done,
    * for some words, class probability p(w|c) will be zero because some 
    * words are only seen in the instances of one class. For classification,
    * we need to ensure that no p(w|c) in the model is zero.
    * 
    * During classification, there will be OOV words (i.e. words that were not
    * seen during training). These words will be just ignored.
    */
   public void computeTheta() {
 
     for(int label = 0; label < numClasses; label++) {
       for(int word = 0; word < numWords; word++) {
         theta[label][word] = (wordCounts[label][word] + 1) / (totalClassWords[label] + numWords);
       }
     }
   }
   
   /**
    * Sample thetas from Dirichlet distribution.
    */
   public void sampleTheta() {
     
     for(int label = 0; label < numClasses; label++) {
       double[] params = new double[numWords];
       for(int word = 0; word < numWords; word++) {
         params[word] = wordCounts[label][word] + dirichletParams[label];
       }
       Dirichlet dirichlet = new Dirichlet(params);
       theta[label] = dirichlet.nextDistribution();
     }
   }
   
   /**
    * Compute feature frequency n x m matrix, where
    * n: number of classes, m: number of dimensions
    * 
    * TODO: Make more efficient by changing the inner loop
    * to iterate over feature names (later converted to dimensions)
    * instead of all dimensions.
    */
   public void computeWordCounts(Dataset dataset) {
 
     for(Instance instance : dataset.getInstances()) {
       for(int index = 0; index < numWords; index++) {
         int label = labelAlphabet.getIndex(instance.getLabel());
         Float value = instance.getDimensionValue(index); 
         if(value != null) {
           wordCounts[label][index] += value;
         }
       }
     }
   }
 
   /**
    * For each label, compute the number of examples that have that label.
    */
   public void computeLabelCounts(Dataset dataset) {
 
     for(Instance instance : dataset.getInstances()) {
       int label = labelAlphabet.getIndex(instance.getLabel());
       labelCounts[label]++;
     }
   }
 
   /**
    * Compute total number of words in each class.
    */
   public void computeTotalClassWords(Dataset dataset) {
 
     for(Instance instance : dataset.getInstances()) {
       int label = labelAlphabet.getIndex(instance.getLabel());
       totalClassWords[label] += instance.getTotalMass();
     }
   }
 
   /**
    * Run sampler.
    */
   public void run() {
 
     initialize();
     for(int sample = 0; sample < numSamples; sample++) {
       sample();
     }
   }
 
   /**
    * Produce final label by averaging labels obtained from samples.
    * Evaluate the accuracy on the test set.
    */
   public double evaluate() {
 
     int correct = 0;
 
     for(Instance instance : sampled.getInstances()) {
       // only evaluate instances from test set
       if(instance.getTemp() != null) {
         int cumulative = 0;
         for(int label : instance.getSequence()) {
           cumulative += label;
         }
 
         int prediction;
         if(((double) cumulative / instance.getSequence().size()) < 0.5) {
           prediction = 0;
         } else {
           prediction = 1;
         }
 
         if(labelAlphabet.getString(prediction).equals(instance.getTemp())) {
           correct++;
         }
       }
     }
 
     return (double) correct / test.size();
   }
 }
