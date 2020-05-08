 /*
  * Copyright (C) 2010 Minwoo Jeong (minwoo.j@gmail.com).
  * This file is part of the "bitextOpenIE" distribution.
  * http://github.com/minwoo/bitextOpenIE/
  * This software is provided under the terms of LGPL.
  */
 
 package openie.crf;
 
 import gnu.trove.map.hash.TIntIntHashMap;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 
 import openie.text.Alphabet;
 import openie.text.Sequence;
 import openie.text.UnitextCorpus;
 import openie.text.SparseVector;
 import openie.text.SparseVector.IntElement;
 import openie.util.Configure;
 import openie.util.Stat;
 
 public class UnitextCRF extends CRF {
 
 	private transient Logger logger = Logger.getLogger(UnitextCRF.class);
     
 	// learning options
 	private double opt_l1prior = 1;
 	private double opt_initLearnRate = 0.5;
 	private double opt_maxiter = 100;
 	private double opt_eta = 1E-05;
 
 	private double[] weight;	// weight vector
 	private double[] penalty;	// used for SGD-L1
 	
 	private int L; // number of labels
 	
 	// internal variables for CRF -- varies by instance 
 	private double[][] nodeScore;
 	private double[][] edgeScore; 
 	private double[][] alpha;
 	private double[][] beta;
 	private double[] alphaScale;
 	private double[] betaScale;
 	private double Z;
 	private int[][] edgeIndex;
 
 	@Override
 	public int[] predict (Sequence instance) {
 		computeNode(instance);
 		computeEdge(instance); 
 		
 		forward();
 		backward();
 		
 		return argmax();
 	}
 	
 	@Override
 	public void test(UnitextCorpus testSet, Configure option) {
 		L = param.sizeLabel();
 		weight = param.getWeight();
 		edgeIndex = param.getEdgeIndex();
 
 		int nCorrect = 0;
 		Iterator<Sequence> iter = testSet.iterator();
 		while (iter.hasNext()) {
 			Sequence instance = iter.next();
 			
 			// do argmax inference for evaluation
 			int[] outcome = predict(instance);
 			for (int i = 0; i < outcome.length; i++) {
 				if (outcome[i] == instance.at(i).getLabel()) 
 					nCorrect ++;
 			}
 		}
 		logger.info(String.format("[TEST] %.4f", (double) nCorrect / testSet.sizeElement() ));
 	}
 	
 	@Override
 	public String[] predict (ArrayList<ArrayList<String>> stringInstances) {
 		String[] output = new String[stringInstances.size()];
 		L = param.sizeLabel();
 		weight = param.getWeight();
 		edgeIndex = param.getEdgeIndex();
 
 		Sequence instance = new Sequence();
 		for (ArrayList<String> stringInstance : stringInstances) {
 			instance.addElement(pack(stringInstance));
 		}
 		
 		int[] outcome = predict(instance);
 		assert(outcome.length == output.length);
 		
 		Alphabet labelDict = param.getLabelAlphabet();
 		
 		for (int i = 0; i < outcome.length; i++) {
 			output[i] = (String) labelDict.getObject(outcome[i]);
 		}
 		
 		return output;
 	}
 
 	@Override
 	public void train(UnitextCorpus trainSet, Configure option) {
 		if (option.contains("maxiter"))
 			this.opt_maxiter = option.getInteger("maxiter");
 		if (option.contains("l1prior"))
 			this.opt_l1prior = option.getDouble("l1prior");
 		if (option.contains("init_learning_rate"))
 			this.opt_initLearnRate = option.getDouble("init_learning_rate");
 		if (option.contains("eta"))
 			this.opt_eta = option.getDouble("eta");
 		
 		runSGDL1(trainSet);		
 	}
 	
 	// pre-computing of node score (i.e. sums of node features)
 	// TODO: reducing the computation by dividing two sets {ENT, NP} and {O, REL}
 	private final void computeNode (Sequence instance) {
 		int T = instance.size();
 		nodeScore = Stat.createMatrix(T+1, L, 1);
 		
 		for (int t = 0; t < T; t++) {
 			for (IntElement point : instance.at(t).getElement()) {
 				TIntIntHashMap index = param.getIndex(point.getId());
 				for (int y : index.keys())  
 					nodeScore[t][y] *= Math.exp(weight[index.get(y)] * point.getVal());
 			}
 		}
 	}
 	
 	// pre-computing of edge score (i.e. transition features)
 	private final void computeEdge (Sequence instance) {
 		edgeScore = Stat.createMatrix(L, L, 1);
 		
 		for (int i = 0; i < L; i++) {
 			for (int j = 0; j < L; j++) {
 				if (edgeIndex[i][j] >= 0) 
 					edgeScore[i][j] = Math.exp(weight[edgeIndex[i][j]]); // NOTE: if you want to exploit complex features like (y_t, y_t-1, x) you should extend this matrix as 3-dim.
 			}
 		}
 	}
 	
 	// forward 
 	private final void forward () {
 		int T = nodeScore.length;
 		alpha = Stat.createMatrix(T, L, 0);
 		alphaScale = Stat.createVector(T, 1);
 		
 		double sum = 0.0;
 		
 		// init
 		for (int i = 0; i < L; i++) {
 			alpha[0][i] += nodeScore[0][i];
 			sum += alpha[0][i];
 		}
 		for (int i = 0; i < L; i++)
 			alpha[0][i] /= sum;
 		alphaScale[0] = sum;
 		
 		// recursion
 		for (int t = 1; t < T - 1; t++) {
 			sum = 0.0;
 			for (int i = 0; i < L; i++) {
 				for (int j = 0; j < L; j++) { 
 					alpha[t][i] += alpha[t-1][j] * edgeScore[i][j];
 				}
 				alpha[t][i] *= nodeScore[t][i];
 				sum += alpha[t][i];
 			}
 			
 			for (int i = 0; i < L; i++) 
 				alpha[t][i] /= sum;
 			alphaScale[t] = sum;
 		}
 		
 		// final
 		for (int i = 0; i < L; i++)
 			alpha[T-1][0] += alpha[T-2][i];
 		alphaScale[T-1] = alpha[T-1][0];
 		
 		Z = alpha[T-1][0]; // = 1
 	}
 	
 	// backward
 	private final void backward () {
 		int T = nodeScore.length;
 		beta = Stat.createMatrix(T, L, 0);
 		betaScale = Stat.createVector(T, 1);
 		
 		double sum = 0.0;
 		
 		// final
 		beta[T-1][0] = 1.0;
 		for (int i = 0; i < L; i++) { 
 			beta[T-2][i] += 1.0;
 			sum += beta[T-2][i];
 		}
 		for (int i = 0; i < L; i++) 
 			beta[T-2][i] /= sum;
 		betaScale[T-2] = sum;
 		
 		// recursion
 		for (int t = T-2; t >= 1; t--) {
 			sum = 0.0;
 			for (int i = 0; i < L; i++) {
 				for (int j = 0; j < L; j++)
 					beta[t-1][i] += beta[t][j] * nodeScore[t][j] * edgeScore[j][i];
 				sum += beta[t-1][i];
 			}
 			
 			for (int i = 0; i < L; i++) 
 				beta[t-1][i] /= sum;
 			betaScale[t-1] = sum;
 		}
 	}
 
 	private final int[] argmax () {
 		int T = nodeScore.length;
 		
 		int[][] psi = new int[T][L]; 
 		double[][] delta = Stat.createMatrix(T, L, 0);
 		
 		// init
 		for (int i = 0; i < L; i++) {
 			psi[0][i] = 0;
 			delta[0][i] = Math.log(nodeScore[0][i]);
 		}
 		
 		// recursion
 		for (int t = 1; t < T-1; t++) {
 			for (int i = 0; i < L; i++) {
 				int maxPsi = 0;
 				double maxDelta = Double.MIN_VALUE;
 				for (int j = 0; j < L; j++) {
 					double val = delta[t-1][j] + Math.log(edgeScore[i][j]); 
 					if (val > maxDelta) {
 						maxDelta = val;
 						maxPsi = j;
 					}
 				}
 				maxDelta += Math.log(nodeScore[t][i]);
 				psi[t][i] = maxPsi;
 				delta[t][i] = maxDelta;
 			}
 		}
 		// last state
 		for (int i = 0; i < L; i++) {
 			psi[T-1][i] = -1;
 			delta[T-1][i] = Double.MIN_VALUE;
 		}
 		int maxPsi = 0; double maxDelta = Double.MIN_VALUE;
 		for (int i = 0; i < L; i++) {
 			if (delta[T-2][i] > maxDelta) {
 				maxDelta = delta[T-2][i];
 				maxPsi = i;
 			}
 		}
 		psi[T-1][0] = maxPsi;
 		delta[T-1][0] = maxDelta;
 		
 		// back-tracking
 		int[] args = new int[T-1];
 		int prev_y = 0;
 		for (int i = T-1; i >= 1; i--) {
 			int y = psi[i][prev_y];
 			prev_y = y;
 			args[i-1] = y;
 		}
 		//double prob = Math.exp(delta[T-1][0]);
 		
 		return args;
 	}
 
 	private final double likelihood (Sequence instance) {
 		int T = instance.size();
 		double prob = 1.0;
 		
 		double trans = 1.0;
 		int prev_y = 0, y = 0;
 		
 		for (int t = 0; t < T+1; t++) {
 			if (t < T) {
 				y = instance.at(t).getLabel();
 				if (t > 0)
 					trans = edgeScore[y][prev_y];
 				prob *= nodeScore[t][y] * trans;
 			} else {
 				y = 0;
 			}
 			
 			prev_y = y;
 			prob /= alphaScale[t];
 		}
		if (prob == 0.0)
			logger.error("seq. prob == 0.0");
 		
 		return prob / Z;
 	}
 
 	private final void runSGDL1 (UnitextCorpus trainSet) {
 		L = param.sizeLabel();
 		weight = param.getWeight();
 		penalty = new double[weight.length];
 		edgeIndex = param.getEdgeIndex();
 		
 		param.clear();
 		Arrays.fill(penalty, 0);
 		
 		double cumulativeRate = 0;
 		double prevLoglikeli = 0;
 		int nElement = trainSet.sizeElement();
 		
 		// iteration
 		for (int niter = 0; niter < opt_maxiter; niter++) {
 			int nCorrect = 0;
 			double learningRate = opt_initLearnRate / ( 1.0 + niter / nElement );
 			cumulativeRate += learningRate * opt_l1prior / nElement;
 			double currentLoglikeli = 0;
 			
 			//trainSet.shuffle(new java.util.Random());
 			Iterator<Sequence> iter = trainSet.iterator();
 			while (iter.hasNext()) {
 				Sequence instance = iter.next();
 				int T = instance.size();
 				
 				computeNode(instance);
 				computeEdge(instance); 
 				forward();
 				backward();
 				
 				int[] outcome = argmax(); // do argmax inference for evaluation
 				
 				// scale factor
 				ArrayList<Double> scaleProduct = new ArrayList<Double>();
 				double prod = 1.0;
 				for (int i = T; i >= 0; i--) {
 					prod *= (betaScale[i] / alphaScale[i]);
 					scaleProduct.add(prod);
 				}
 				Collections.reverse(scaleProduct);
 				
 				
 				int prev_y = 0;
 				for (int t = 0; t < outcome.length; t++) {
 					SparseVector elem = instance.at(t);
 					int y = elem.getLabel();
 					if (outcome[t] == y)
 						nCorrect ++;
 					
 					double scale_factor = scaleProduct.get(t+1) * betaScale[t];
 					double scale_factor2 = scaleProduct.get(t);
 					
 					// node update
 					double[] nodeProbs = new double[L];
 					for (int i = 0; i < L; i++) 
 						nodeProbs[i] = alpha[t][i] * beta[t][i] / Z * scale_factor;
 					updateNode(elem, nodeProbs, learningRate, cumulativeRate);
 					
 					// edge update
 					if (t > 0) {
 						double[][] edgeProbs = new double[L][L];
 						for (int i = 0; i < L; i++)  
 							for (int j = 0; j < L; j++)     
 								edgeProbs[i][j] = alpha[t-1][j] * beta[t][i] * nodeScore[t][i] * edgeScore[i][j] / Z * scale_factor2;
 						updateEdge(y, prev_y, edgeProbs, learningRate, cumulativeRate);
 					}
 					
 					prev_y = y;
 				}
 				
				currentLoglikeli -= Math.log(likelihood(instance));
 			}
 			
 			for (int i = 0; i < weight.length; i++)
 				currentLoglikeli += opt_l1prior * Math.abs(weight[i]);
 			
 			logger.info(String.format("[%d] %e %.4f (%d)", niter+1, currentLoglikeli, (double) nCorrect / nElement , numNonZero()));
 			if ( Math.abs(currentLoglikeli - prevLoglikeli) / (Math.abs(currentLoglikeli) + Math.abs(prevLoglikeli)) < opt_eta)
 				break;
 			prevLoglikeli = currentLoglikeli;
 		}
 		
 		// finally
 		int nCorrect = 0;
 		double currentLogLikeli = 0;
 		Iterator<Sequence> iter = trainSet.iterator();
 		while (iter.hasNext()) {
 			Sequence instance = iter.next();
 			
 			computeNode(instance);
 			computeEdge(instance); 
 			
 			forward();
 			backward();
 
 			int[] outcome = argmax();
 
 			for (int t = 0; t < outcome.length; t++) {
 				SparseVector elem = instance.at(t);
 				if (outcome[t] == elem.getLabel())
 					nCorrect ++;
 			}
 			
 			// true loglikelihood  
			currentLogLikeli -= Math.log(likelihood(instance));
 		}
 		for (int i = 0; i < weight.length; i++)
 			currentLogLikeli += opt_l1prior * Math.abs(weight[i]);
 
 		logger.info(String.format("[FINAL] %e %.4f (%d)", currentLogLikeli, (double) nCorrect / nElement, numNonZero()));
 		param.setWeight(weight);		
 	}
 	
 	private final void updateNode (SparseVector elem, double[] prob, double l, double u) {
 		for (IntElement point : elem.getElement()) {
 			TIntIntHashMap index = param.getIndex(point.getId());
 			for (int y : index.keys()) {
 				
 				int fid = index.get(y);
 				if (y == elem.getLabel())
 					weight[fid] += l * (1 - prob[y]) * point.val;
 				else
 					weight[fid] -= l * prob[y] * point.val;
 
 				// clipping + lazy update
 				double z = weight[fid];
 				if (z > 0) 
 					weight[fid] = Math.max(0, weight[fid] - (u + penalty[fid]));
 				else if (z < 0)
 					weight[fid] = Math.min(0, weight[fid] + (u - penalty[fid]));
 				penalty[fid] += weight[fid] - z;
 			}
 		}		
 	}
 	
 	private final void updateEdge (int y, int prev_y, double[][] prob, double l, double u) {
 		for (int j = 0 ; j < L; j++) {
 			for (int i = 0; i < L; i++) {
 				int fid = edgeIndex[i][j];
 				if (fid < 0) // = null parameter
 					continue;
 				
 				if (i == y && j == prev_y)
 					weight[fid] += l * (1 - prob[i][j]);
 				else
 					weight[fid] -= l * prob[i][j];
 				
 				// clipping + lazy update
 				double z = weight[fid];
 				if (z > 0) 
 					weight[fid] = Math.max(0, weight[fid] - (u + penalty[fid]));
 				else if (z < 0)
 					weight[fid] = Math.min(0, weight[fid] + (u - penalty[fid]));
 				penalty[fid] += weight[fid] - z;
 			}
 		}
 	}
 	
 	private final int numNonZero() {
 		int n = 0;
 		for (double w : weight)
 			if (w != 0)
 				n++;
 		return n;
 	}
 
 }
