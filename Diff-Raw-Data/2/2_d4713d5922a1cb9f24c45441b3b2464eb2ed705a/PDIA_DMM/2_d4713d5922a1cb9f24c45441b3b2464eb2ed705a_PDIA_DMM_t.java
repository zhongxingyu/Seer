 
 package edu.columbia.stat.wood.pdia;
 
 import edu.columbia.stat.wood.hpyp.MutableDouble;
 import edu.columbia.stat.wood.hpyp.MutableInteger;
 import edu.columbia.stat.wood.hpyp.RestaurantFranchise;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import org.apache.commons.math.special.Gamma;
 /**
  * Implements a PDIA to learn a Directed Markov Model, in the sense of
  * "Constructing States for Reinforcement Learning" MM Mahmud, ICML 2010
  *
  * When passing data, it is assumed that the first array is actions, second is
  * observations, and third is rewards
  * @author David Pfau, 2011
  */
 public class PDIA_DMM implements Serializable, PDIA {
 
     protected RestaurantFranchise rf;
     public HashMap<MultiPair, Integer> dMatrix;
     public HashMap<SinglePair, int[]> rMatrix; // the number of times a given reward is observed following a given state and action
     public HashMap<SinglePair, int[]> oMatrix; // the set of action/observation pairs visited by the data, used for clearing unseen transitions
     protected int[] nSymbols;
     protected double beta;
     protected double logLike;
     protected static Random RNG = new Random(0L);
     private static final long serialVersionUID = 1L;
 
     public PDIA_DMM(int[] n) {
         assert n.length == 3 : "Need size of action, observation, and reward space.";
         rf = new RestaurantFranchise(2);
         nSymbols = n;
         dMatrix = new HashMap<MultiPair, Integer>();
         beta = 10.0;
     }
 
     public PDIASequence run(int[][]... data) {
         assert data.length == 3 : "Need actions, observations and rewards.";
         return new PDIASequence(this,0,data);
     }
 
     public PDIASequence run(int init, int[][]... data) {
         assert data.length == 3 : "Need actions, observations and rewards";
         return new PDIASequence(this,init,data);
     }
 
 
     /**
      * Samples a new string from the current PDIA without changing any data structures
      * @param init The initial state of the new string
      * @param length The length of the new string
      * @return
      */
     /*public int[] generate(int init, int length) {
         int[] line = new int[length];
         int i = 0;
         for (SinglePair p : new PDIAContinuation(Util.copy(this),init,length)) {
             line[i] = p.symbol(0);
             i++;
         }
         return line;
     }*/
 
     /**
      * Set the seed of RNG using the current system time.
      */
     public static void reseedRNG() {
     	RNG.setSeed(System.currentTimeMillis());
     }
 
     /**
      * Given data, returns an iterator over PDIA that forms an MCMC sampler
      * @param nSymbols The size of the alphabet for each data type
      * @param data
      * @return The iterator over PDIA
      */
     public static PDIASample sample(int[] nSymbols, int[][]... data) {
         assert nSymbols.length == data.length : "Number of data types is inconsistent!";
         return new PDIASample(new PDIA_DMM(nSymbols),data);
     }
 
     //Hacked version of the above to make Matlab scripts work
     public static PDIASample sample(int[] nSymbols, Object[] action, Object[] observation, Object[] reward) {
         int[][] castAct = new int[action.length][];
         for (int i = 0; i < action.length; i++) {
             castAct[i] = (int[])action[i];
         }        
         int[][] castObs = new int[observation.length][];
         for (int i = 0; i < action.length; i++) {
             castObs[i] = (int[])observation[i];
         }        
         int[][] castRew = new int[reward.length][];
         for (int i = 0; i < action.length; i++) {
             castRew[i] = (int[])reward[i];
         }
         return new PDIASample(new PDIA_DMM(nSymbols),castAct,castObs,castRew);
     }
 
     /**
      * Runs an MCMC sampler a specified number of times, saving samples along the way
      * @param burnIn Number of burn in samples
      * @param interval Number of samples between saves
      * @param samples Number of saved samples
      * @param nSymbols The size of the alphabet for each data type
      * @param data
      * @return An array of posterior samples from the Markov chain
      */
     public static PDIA_DMM[] sample(int burnIn, int interval, int samples, int[] nSymbols, int[][]... data) {
         PDIA_DMM[] ps = new PDIA_DMM[samples];
         int i = 0;
         for (PDIA p : PDIA_DMM.sample(nSymbols,data)) {
             if (i < burnIn) {
                 System.out.println("Burn In Sample " + i + " of " + burnIn);
             }
             if (i >= burnIn && (i-burnIn) % interval == 0) {
                 ps[(i-burnIn)/interval] = (PDIA_DMM)Util.copy(p);
                 System.out.println("Wrote sample " + Integer.toString((i-burnIn)/interval) + " of " + samples);
             }
             i++;
             if (i == burnIn + interval*samples) break;
         }
         return ps;
     }
 
     public Set<Integer> states() {
         HashSet<Integer> states = new HashSet<Integer>();
        for (Pair p : dMatrix.keySet()) {
             states.add(p.state());
         }
         return states;
     }
 
     /**
      * @return A deterministic map from state/symbol pairs to next states.
      * Returns null if that state/symbol pair is not in the transition matrix.
      */
     public HashMap<MultiPair,Integer> transition() { return dMatrix; }
 
     /**
      * Given a state and symbol observed in that state, returns the next state,
      * or null if that state is not in the transition matrix
      * @param state
      * @param symbol
      * @return
      */
     public Integer transition(int state, int[] symbol) {
         assert symbol.length == 2; // check to make sure we're leaving out the reward when transitioning
         return dMatrix.get(new MultiPair(state,symbol));
     }
 
     public Integer transition(int state, int action, int observation) {
         return dMatrix.get(new MultiPair(state, new int[]{action, observation}));
     }
 
     /**
      * Given a state and action, returns an array of counts for observations seen
      * @param state
      * @param action
      * @return
      */
     public int[] observation(int state, int action) {
         return oMatrix.get(new SinglePair(state,action));
     }
 
     /**
      * Given a state and action, returns an array of counts for rewards seen
      * @param state
      * @param action
      * @return
      */
     public int[] reward(int state, int action) {
         return rMatrix.get(new SinglePair(state,action));
     }
 
     public Integer transition(Pair p) {
         int[] symbols = new int[]{p.symbol(0),p.symbol(1)};
         return dMatrix.get(new MultiPair(p.state(),symbols));
     }
 
     public Integer transitionAndAdd(Pair p) {
         Integer state = transition(p);
         if (state == null) {
             int[] context = new int[]{p.symbol(0),p.symbol(1)};
             state = rf.generate(context);
             rf.seat(state, context);
             dMatrix.put(new MultiPair(p.state(),context), state);
         }
         return state;
     }
 
     public Integer transitionAndAdd(int state, int action, int observation) {
         return transitionAndAdd(new MultiPair(state, new int[]{action, observation}));
     }
 
     private double[][] transitionProbability(int[] context) {
         HashMap<Integer,MutableDouble> map = rf.predictiveProbabilityExistingTypes(context);
         double[][] probs = new double[map.size()][2];
         int i = 0;
         for (int key : map.keySet()) {
             probs[i][0] = key;
             probs[i][1] = map.get(key).doubleVal();
             i++;
         }
         return probs;
     }
 
     /**
      * Returns a two-column matrix of next states in the first column (with -1
      * for an unused state) and the probability of transitioning to that state
      * from an unknown state without any assumption about the observed action or
      * observation associated with that transition
      */
     public double[][] transitionProbability() {
         return transitionProbability(null);
     }
 
     /**
      * Returns a two-column matrix with the same format as above, but for a
      * transition with a known observation
      * @param observation
      * @return
      */
     public double[][] transitionProbability(int observation) {
         return transitionProbability(new int[]{observation});
     }
 
     /**
      * Returns a two-column matrix with the same format as both above, but for a
      * transition with a known observation and action
      * @param action
      * @param observation
      * @return
      */
     public double[][] transitionProbability(int action, int observation) {
         return transitionProbability(new int[]{action,observation});
     }
 
     public void count(int[][]... data) {
         oMatrix = new HashMap<SinglePair, int[]>();
         rMatrix = new HashMap<SinglePair, int[]>();
         for (Pair p : run(data)) {
             SinglePair sa = new SinglePair(p.state(),p.symbol(0)); // state/action pair
             
             int[] oCounts = oMatrix.get(sa);
             if (oCounts == null) {
                 oCounts = new int[nSymbols[1]];
                 oMatrix.put(sa, oCounts);
             }
             
             int[] rCounts = rMatrix.get(sa);
             if (rCounts == null) {
                 rCounts = new int[nSymbols[2]];
                 rMatrix.put(sa, rCounts);
             }
 
             oCounts[p.symbol(1)] ++;
             rCounts[p.symbol(2)] ++;
         }
         logLike = logLik();
     }
 
     public double jointScore() {
         return logLike + rf.logLik();
     }
 
     public double logLik() {
         double logLik = 0;
         double lgb = Gamma.logGamma(beta);
         double bn = beta / nSymbols[nSymbols.length - 1];
         double lgbn = Gamma.logGamma(bn);
 
         for (int[] counts : rMatrix.values()) {
             for (int i = 0; i < counts.length; i++) {
                 if (counts[i] != 0) {
                     logLik += Gamma.logGamma(counts[i] + bn) - lgbn;
                 }
             }
             logLik -= Gamma.logGamma(Util.sum(counts) + beta) - lgb;
         }
 
         return logLik;
     }
 
     /**
      * Samples the hyperparameter over the emission distribution
      * @param proposalSTD
      */
     protected void sampleBeta(double proposalSTD) {
         double currentBeta = beta;
 
         double proposal = currentBeta + RNG.nextGaussian() * proposalSTD;
         if (proposal <= 0) {
             return;
         }
         beta = proposal;
         double pLogLik = logLik();
         double r = Math.exp(pLogLik - logLike - proposal + currentBeta);
         if (RNG.nextDouble() >= r) {
             beta = currentBeta;
         } else {
             logLike = pLogLik;
         }
     }
 
     public void sampleOnce(int[][]... data) {
         sampleD(data);
         rf.sample();
         sampleBeta(1.0);
     }
 
     /**
      * One sweep of sampling over the transition matrix.
      * @param data
      */
     private void sampleD(int[][]... data) {
         for (MultiPair p : randomPairArray()) {
             if (dMatrix.get(p) != null) {
                 sampleD(p,data);
             }
             fixDMatrix();
         }
     }
 
     /**
      * Samples a single entry of the transition matrix
      * @param p The state/symbol pair to be sampled
      * @param data
      */
     private void sampleD(MultiPair p, int[][]... data) {
         int[] context = p.symbol();
         double cLogLik = logLike;
         Integer currentType = dMatrix.get(p);
         assert (currentType != null);
 
         rf.unseat(currentType, context);
         Integer proposedType = rf.generate(context);
         rf.seat(currentType, context);
         dMatrix.put(p, proposedType);
 
         HashMap<SinglePair, int[]> oMatOld = (HashMap<SinglePair,int[]>)Util.intArrayMapCopy(oMatrix);
         HashMap<SinglePair, int[]> rMatOld = (HashMap<SinglePair,int[]>)Util.intArrayMapCopy(rMatrix);
         count(data);
         double pLogLik = logLik();
 
         if (Math.log(RNG.nextDouble()) < pLogLik - cLogLik) { // accept
             rf.unseat(currentType, context);
             rf.seat(proposedType, context);
         } else { // reject
             oMatrix = oMatOld;
             rMatrix = rMatOld;
             logLike = cLogLik;
             dMatrix.put(p, currentType);
         }
     }
 
     /**
      * After sampling, clears out state/symbol pairs for which there are no observed data
      */
     private void fixDMatrix() {
         HashSet<MultiPair> keysToDiscard = new HashSet<MultiPair>();
 
         for (MultiPair p : dMatrix.keySet()) {
             int[] counts = oMatrix.get(p.toSingle());
             if (counts == null || counts[p.symbol(1)] == 0) {
                 keysToDiscard.add(p);
             }
         }
 
         int[] context = new int[2];
         for (MultiPair p : keysToDiscard) {
             context[0] = p.symbol(0);
             context[1] = p.symbol(1);
             rf.unseat(dMatrix.get(p), context);
             dMatrix.remove(p);
         }
     }
 
     public double[] score(int init, int[][]... data) {
         int totalLength = 0;
         int no = nSymbols[1];
         int nr = nSymbols[2];
         for (int i = 0; i < data[0].length; i++) {
             totalLength += data[0][i].length;
         }
 
         double[] score = new double[totalLength];
 
         int index = 0;
         for (Pair p : run(init,data)) {
             MultiPair mp = (MultiPair)p;
             int[] oCounts = oMatrix.get(mp.toSingle());
             if (oCounts == null) {
                 oCounts = new int[no];
                 oMatrix.put(mp.toSingle(),oCounts);
             }
 
             int[] rCounts = rMatrix.get(mp.toSingle());
             if (rCounts == null) {
                 rCounts = new int[nr];
                 rMatrix.put(mp.toSingle(),rCounts);
             }
 
             double totalCount = Util.sum(rCounts);
             score[(index++)] = ((rCounts[mp.symbol(2)] + beta / nr) / (totalCount + beta));
             oCounts[mp.symbol(1)] ++;
             rCounts[mp.symbol(2)] ++;
         }
 
         return score;
     }
 
     /**
      * Same as above, but with predictions averaged over multiple PDIAs
      * We do things in this order because we need to average single-datum
      * probabilities before taking the sum of log probabilities.
      * @param ps Array of PDIA posterior samples
      * @param init
      * @param data
      * @return
      */
     public static double[] score(PDIA_DMM[] ps, int init, int[][]... data) {
         int n = 10;
         double[] score = new double[Util.totalLen(data[0])];
         for (PDIA_DMM pdia : ps) {
             for (int i = 0; i < n; i++) {
                 PDIA_DMM copy = Util.copy(pdia);
                 Util.addArrays(score, copy.score(init, data));
             }
         }
 
         for (int i = 0; i < score.length; i++) {
             score[i] /= (n*ps.length);
         }
         return score;
     }
 
     // Use with caution, I haven't checked this
     public void check() {
     	HashMap<int[], HashMap<Integer, MutableInteger>> dCustomerCounts = new HashMap<int[], HashMap<Integer, MutableInteger>>();
 
         for (MultiPair p : dMatrix.keySet()) {
             int[] context = p.symbol();
 
             HashMap<Integer, MutableInteger> typeCountMap = dCustomerCounts.get(context);
             if (typeCountMap == null) {
                 typeCountMap = new HashMap<Integer, MutableInteger>();
                 dCustomerCounts.put(context, typeCountMap);
             }
 
             Integer tKey = dMatrix.get(p);
             MutableInteger count = typeCountMap.get(tKey);
 
             if (count == null) {
                 count = new MutableInteger(0);
                 typeCountMap.put(tKey, count);
             }
 
             count.increment();
         }
 
         int[] tCounts = new int[2];
         for (int i = 0; i < nSymbols[0]; i++) {
             for (int j = 0; j < nSymbols[1]; j++) {
                 int[] context = new int[]{i,j};
                 HashMap<Integer, MutableInteger> hm = dCustomerCounts.get(context);
                 for (Integer type : hm.keySet()) {
                     rf.get(context).getCounts(type.intValue(), tCounts);
                     assert (tCounts[0] == hm.get(type).intVal());
                 }
             }
         }
     }
 
     protected MultiPair[] randomPairArray() {
         Object[] oa = Util.randArray(dMatrix.keySet());
         MultiPair[] pa = new MultiPair[oa.length];
         System.arraycopy(oa, 0, pa, 0, oa.length);
         return pa;
     }
 
 
 
 
 }
 
