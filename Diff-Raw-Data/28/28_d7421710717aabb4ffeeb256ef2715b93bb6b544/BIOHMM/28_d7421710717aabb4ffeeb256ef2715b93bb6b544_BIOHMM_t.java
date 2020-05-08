 package biosim.app.learnedants;
 
 import biosim.core.util.BTFData;
 import biosim.core.util.KernelDensityEstimator;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 public class BIOHMM{
 	BIOHMMInputParser bip;
 	public double[][][] transitionFunction;
 	Object[][][] transitionMonitors;
 	public double[] prior;
 	public int[] partition;
 	private double[][] completeLLGamma;
 	BTFData data;
 	int dim, numThreads = 4;
 	double kernelSigma = 1.0, bandwidth = 1.0;
 	public static final boolean PRINT_ITERATIONS = true;
 	public final KernelDensityEstimator[] b;
 	public final KernelDensityEstimator sensors;
 		
 	public static double elnsum(double logx, double logy){
 		//given log(x), and log(y), return log(x+y)
 		if(logx == Double.NEGATIVE_INFINITY || logy == Double.NEGATIVE_INFINITY){
 			if(logx != Double.NEGATIVE_INFINITY) return logx;
 			else return logy;
 		} else {
 			//So the log of the sum of two non-zero numbers can be
 			//expressed as a function of their logs in the following way:
 			//ln(x+y) = ln(x) + ln(1 + exp(ln(y) - ln(x)))
 			//we reduce this down like so:
 			//ln(x) + ln(1 + exp(ln(y/x)))
 			//ln(x) + ln(1 + y/x)
 			//ln(x) + ln( (x+y) / x)
 			//ln(x) + ln(x+y) - ln(x)
 			//ln(x+y) hooray!
 			//It's noted that it's important to always subtract buy the larger
 			//number in the exponent, so that the result is a exponent that
 			//doesn't overflow to infinity
 			if(logx < logy){
 				double tmp = logx;
 				logx = logy;
 				logy = tmp;
 			}
 			return (logx + Math.log(1 + Math.exp(logy - logx)));
 		}
 	}
 	
 	public BIOHMM(int numStates, BIOHMMInputParser bip) throws IOException{
 		this.bip = bip;
 		dim = bip.outputDim();
 		transitionFunction = new double[numStates][numStates][(int)Math.pow(2,bip.numSwitches())];
 		transitionMonitors = new Object[numStates][numStates][(int)Math.pow(2,bip.numSwitches())];
 		for(int i=0;i<transitionMonitors.length;i++){
 			for(int j=0;j<transitionMonitors[i].length;j++){
 				for(int k=0;k<transitionMonitors[i][j].length;k++){
 					transitionMonitors[i][j][k] = new Object();
 				}
 			}
 		}
 		prior = new double[numStates];
 		partition = new int[bip.partSize()];
 		completeLLGamma = new double[partition.length][numStates];
 		b = new KernelDensityEstimator[prior.length];
 		sensors = new KernelDensityEstimator(bip.sensorDim(), new KernelDensityEstimator.NormalKernel(kernelSigma));
 		for(int i=0;i<b.length;i++){
 			b[i] = new KernelDensityEstimator(dim, new KernelDensityEstimator.NormalKernel(kernelSigma));
 		}
 		/*
 		for(int i=0;i<numStates;i++){
 			for(int j=0;j<numStates;j++){
 				for(int k=0;k<(int)Math.pow(2,bip.numSwitches());k++){
 					transitionFunction[i][j][k] = 1.0/(numStates*numStates);
 				}
 			}
 			prior[i] = 1.0/numStates;
 		}
 		for(int i=0;i<partition.length;i++){
 			partition[i] = (int)Math.floor((i/(partition.length/numStates)));
 		}
 		for(int i=0;i<b.length;i++){
 			for(int j=0;j<partition.length;j++){
 				if(partition[j] == i){
 					b[i].add(bip.getDataAtIDX(j));
 				}
 			}
 		}
 		*/
 		for(int i=0;i<bip.partSize();i++){
 			sensors.add(bip.getDataAtIDX(i));
 		}
 		bip.initParameters(transitionFunction,prior,partition,b);
 	}
 	
 	public double outputLogProbAtIDX(int idx, int state){
 		double jp = b[state].estimate(bip.getDataAtIDX(idx),bandwidth);
 		double sp = sensors.estimate(bip.getSensorsAtIDX(idx),bandwidth);
 		return Math.log(jp) - Math.log(sp);
 	}
 	
 	public void calculateAlpha(	ArrayList<Integer> seq,
 								KernelDensityEstimator[] b,
 								double[][] alpha){
 		for(int i=0;i<prior.length;i++){
 			alpha[0][i] = prior[i]*b[i].estimate(bip.getDataAtIDX(seq.get(0)),bandwidth);
 		}
 		for(int t=1;t<seq.size();t++){
 			for(int j=0;j<prior.length;j++){
 				alpha[t][j] = 0.0;
 				for(int i=0;i<prior.length;i++){
 					alpha[t][j] += alpha[t-1][i]*transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t-1))];
 				}
 				alpha[t][j] *= b[j].estimate(bip.getDataAtIDX(seq.get(t)),bandwidth);
 			}
 		}
 	}
 	
 	public void calculateLogAlpha(	ArrayList<Integer> seq,
 									KernelDensityEstimator[] b,
 									double[][] logalpha){
 		for(int i=0;i<prior.length;i++){
 			double outputLog = outputLogProbAtIDX(seq.get(0),i);
 			//logalpha[0][i] = Math.log(prior[i])+Math.log(b[i].estimate(bip.getDataAtIDX(seq.get(0)),bandwidth));
 			logalpha[0][i] = Math.log(prior[i])+outputLog;
 		}
 		for(int t=1;t<seq.size();t++){
 			for(int j=0;j<prior.length;j++){
 				logalpha[t][j] = Double.NEGATIVE_INFINITY;
 				for(int i=0;i<prior.length;i++){
 					logalpha[t][j] = elnsum(logalpha[t][j],logalpha[t-1][i]+Math.log(transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t-1))]));
 				}
 				double outputLog = outputLogProbAtIDX(seq.get(t),j);
 				//logalpha[t][j] += Math.log(b[j].estimate(bip.getDataAtIDX(seq.get(t)),bandwidth));
 				logalpha[t][j] += outputLog;
 			}
 		}
 	}
 	
 	public void calculateScaledAlpha(	ArrayList<Integer> seq,
 										KernelDensityEstimator[] b,
 										double[][] hat_alpha,
 										double[] coeff_c){
 		double[] bar_alpha = new double[prior.length];
 		for(int t=0;t<seq.size();t++) coeff_c[t] = 0.0;
 		for(int j=0;j<prior.length;j++){
 			double tmpprior = prior[j];
 			double tmpoutp = b[j].estimate(bip.getDataAtIDX(seq.get(0)),bandwidth);
 			System.out.println("bar_alpha["+j+"] = "+tmpprior+" * "+tmpoutp);
 			bar_alpha[j] = tmpprior*tmpoutp;//prior[j]*b[j].estimate(bip.getDataAtIDX(seq.get(0)),bandwidth);
 			coeff_c[0] += bar_alpha[j];
 		}
 		if(coeff_c[0] != 0.0) coeff_c[0] = 1.0/coeff_c[0];
 		for(int j=0;j<prior.length;j++){
 			hat_alpha[0][j] = coeff_c[0] * bar_alpha[j];
 		}
 		//now do DP
 		for(int t=1;t<seq.size();t++){
 			for(int j=0;j<prior.length;j++){
 				bar_alpha[j] = 0.0;
 				for(int i=0;i<prior.length;i++){
 					double tmp = hat_alpha[t-1][i];
 					tmp = tmp*transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t-1))];
 					//tmp = tmp*b[j].estimate(getDataAtIDX(seq.get(t)),bandwidth);
 					bar_alpha[j] += tmp;
 				}
 				bar_alpha[j] = bar_alpha[j]*b[j].estimate(bip.getDataAtIDX(seq.get(t)),bandwidth);
 				coeff_c[t] += bar_alpha[j];
 			}
 			if(coeff_c[t] != 0.0) coeff_c[t] = 1.0/coeff_c[t];
 			for(int j=0;j<prior.length;j++){
 				hat_alpha[t][j] = coeff_c[t] * bar_alpha[j];
 			}
 		}
 		/*
 		System.out.print("[");
 		for(int i=0;i<coeff_c.length;i++){
 			System.out.print(coeff_c[i]);
 		}
 		System.out.println("]");
 		*/
 	}
 	
 	public void calculateBeta(	ArrayList<Integer> seq,
 								KernelDensityEstimator[] b,
 								double[][] beta){
 		for(int i=0;i<prior.length;i++){
 			beta[seq.size()-1][i] = 1.0;
 		}
 		for(int t=seq.size()-2;t>=0;t--){
 			for(int i=0;i<prior.length;i++){
 				beta[t][i] = 0.0;
 				for(int j=0;j<prior.length;j++){
 					beta[t][i] += transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t))]*b[j].estimate(bip.getDataAtIDX(seq.get(t+1)),bandwidth)*beta[t+1][j];
 				}
 			}
 		}
 	}
 
 	public void calculateLogBeta(	ArrayList<Integer> seq,
 									KernelDensityEstimator[] b,
 									double[][] logbeta){
 		for(int i=0;i<prior.length;i++){
 			logbeta[seq.size()-1][i] = 0.0;
 		}
 		for(int t=seq.size()-2;t>=0;t--){
 			for(int i=0;i<prior.length;i++){
 				logbeta[t][i] = Double.NEGATIVE_INFINITY;
 				for(int j=0;j<prior.length;j++){
 					double tmplog = Math.log(transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t))]);
 					double outputLog = outputLogProbAtIDX(seq.get(t+1),j);
 					//tmplog += Math.log(b[j].estimate(bip.getDataAtIDX(seq.get(t+1)),bandwidth));
 					tmplog += outputLog;
 					tmplog += logbeta[t+1][j];
 					logbeta[t][i] = elnsum(logbeta[t][i],tmplog);
 				}
 			}
 		}
 	}
 	
 	public void calculateScaledBeta(	ArrayList<Integer> seq,
 										KernelDensityEstimator[] b,
 										double[][] hat_beta,
 										double[] coeff_c){
 		double[] bar_beta = new double[prior.length];
 		for(int i=0;i<prior.length;i++){
 			bar_beta[i] = 1.0;
 			hat_beta[seq.size()-1][i] = coeff_c[seq.size()-1]*bar_beta[i];
 		}
 		//now do DP
 		for(int t=seq.size()-2;t>=0;t--){
 			for(int j=0;j<prior.length;j++){
 				bar_beta[j] = 0.0;
 				for(int i=0;i<prior.length;i++){
 					double tmp = transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t))];
 					//tmp = tmp * b[j].estimate(getDataAtIDX(seq.get(t+1)),bandwidth);
 					tmp = tmp * hat_beta[t+1][i];
 					bar_beta[j] += tmp;
 				}
 				bar_beta[j] = bar_beta[j] * b[j].estimate(bip.getDataAtIDX(seq.get(t+1)),bandwidth);
 				hat_beta[t][j] = coeff_c[t]*bar_beta[j];
 			}
 		}
 	}
 	
 	public void calculateXi(	ArrayList<Integer> seq,
 								KernelDensityEstimator[] b,
 								double[][] alpha,
 								double[][] beta,
 								double[][][] xi){
 		double sum;
 		for(int t=0;t<seq.size();t++){
 			sum = 0.0;
 			for(int i=0;i<prior.length;i++){
 				for(int j=0;j<prior.length;j++){
 					if(t == (seq.size()-1)){
 						xi[t][i][j] = 0.0;
 					} else {
 						xi[t][i][j] = alpha[t][i];
 						xi[t][i][j] *= transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t))];
 						xi[t][i][j] *= b[j].estimate(bip.getDataAtIDX(seq.get(t+1)),bandwidth);
 						xi[t][i][j] *= beta[t+1][j];
 						sum += xi[t][i][j];
 					}
 				}
 			}
 			if(sum != 0){
 				for(int i=0;i<prior.length;i++){
 					for(int j=0;j<prior.length;j++){
 						xi[t][i][j] = xi[t][i][j]/sum;
 					}
 				}
 			}
 		}
 	}
 
 	public void calculateLogXi(	ArrayList<Integer> seq,
 								KernelDensityEstimator[] b,
 								double[][] logalpha,
 								double[][] logbeta,
 								double[][][] logxi){
 		double logsum;
 		for(int t=0;t<seq.size();t++){
 			logsum = Double.NEGATIVE_INFINITY;
 			for(int i=0;i<prior.length;i++){
 				for(int j=0;j<prior.length;j++){
 					if(t == (seq.size()-1)){
 						logxi[t][i][j] = Double.NEGATIVE_INFINITY;
 					} else {
 						logxi[t][i][j] = logalpha[t][i];
 						double tmplog = Math.log(transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t))]);
 						double outputLog = outputLogProbAtIDX(seq.get(t+1),j);
 						//tmplog += Math.log(b[j].estimate(bip.getDataAtIDX(seq.get(t+1)),bandwidth));
 						tmplog += outputLog;
 						tmplog += logbeta[t+1][j];
 						logxi[t][i][j] = logxi[t][i][j] + tmplog;
 						logsum = elnsum(logsum,logxi[t][i][j]);
 					}
 				}
 			}
 			for(int i=0;i<prior.length;i++){
 				for(int j=0;j<prior.length;j++){
 					if(logxi[t][i][j] != Double.NEGATIVE_INFINITY){
 						logxi[t][i][j] = logxi[t][i][j]-logsum;
 					}
 				}
 			}
 		}
 	}
 		
 	public void calcXiFromScaled(	ArrayList<Integer> seq,
 									KernelDensityEstimator[] b,
 									double[][] hat_alpha,
 									double[][] hat_beta,
 									double[][][] xi){
 		for(int t=0;t<seq.size();t++){
 			double sum = 0.0;
 			for(int i=0;i<prior.length;i++){
 				for(int j=0;j<prior.length;j++){
 					//the beta check goes one step past the end of the
 					//sequences, so we set our probability of 
 					//transitioning from i at time T to j at time T+1
 					//equal to 1.0, just like we do for beta
 					//Maybe this is wrong? When implementing this in python
 					//I set xi[T][i][j] = 0.0, so lets try that
 					if(t == (seq.size()-1)){
 						xi[t][i][j] = 0.0;
 					} else {
 						xi[t][i][j] = hat_alpha[t][i];
 						xi[t][i][j] *= transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t))];
 						xi[t][i][j] *= b[j].estimate(bip.getDataAtIDX(seq.get(t+1)),bandwidth);
 						xi[t][i][j] *= hat_beta[t+1][j];
 					}
 					sum += xi[t][i][j];
 				}
 			}
 			if(sum != 1.0 && sum != 0.0){
 				for(int i=0;i<prior.length;i++){
 					for(int j=0;j<prior.length;j++){
 						xi[t][i][j] = xi[t][i][j]/sum;
 					}
 				}
 				//System.out.println("sum_ij(Xi["+t+"]) = "+sum);
 				//throw new RuntimeException("Xi not summint to one.");
 			}
 		}
 	}
 	
 	public void calcGammaFromXi(double[][][] xi, double[][] gamma){
 		for(int t=0;t<xi.length;t++){
 			double sum = 0.0;
 			for(int i=0;i<prior.length;i++){
 				gamma[t][i] = 0.0;
 				for(int j=0;j<prior.length;j++){
 					gamma[t][i] += xi[t][i][j];
 				}
 				sum = sum+ gamma[t][i];
 			}
 			if(sum != 1.0 && sum != 0.0){
 				for(int i=0;i<prior.length;i++){
 					gamma[t][i] = gamma[t][i]/sum;
 				}
 				//System.out.println("sum_i(Gamma["+t+"]) = "+sum);
 				//throw new RuntimeException("Gamma not summing to one.");
 			}
 		}
 	}
 	
 	public void calculateLogGamma(double[][] logalpha, double[][] logbeta, double[][] loggamma){
 		for(int t=0;t<logalpha.length;t++){
 			double logsum = Double.NEGATIVE_INFINITY, tmpls = Double.NEGATIVE_INFINITY;
 			for(int i=0;i<prior.length;i++){
 				//System.out.println("logbeta["+t+"]["+i+"] = "+logbeta[t][i]);
 				loggamma[t][i] = logalpha[t][i]+logbeta[t][i];
 				logsum = elnsum(logsum,loggamma[t][i]);
 			}
 			for(int i=0;i<prior.length;i++){
 				loggamma[t][i] = loggamma[t][i] - logsum;
 			}
 		}
 	}
 	
 	public BigDecimal calculateSeqLogLikelihood(double[][] hat_alpha, double[] coeff_c){
 		//According to Rabiner, the likelihood of the observation sequence under
 		//a given model is just the sum of the alpha variable at time T of each
 		//state. According to the errata, hat_alpha_i(t) = alpha_i(t)*C_t
 		//and C_t = c_1 * c_2 * c_3 *...* c_t
 		BigDecimal C_T = BigDecimal.ZERO;
 		double rv = 0.0;
 		for(int t=0;t<coeff_c.length;t++){
 			if(coeff_c[t] == 0.0){ 
 				C_T = BigDecimal.ZERO;
 				break;
 			}
 			C_T = C_T.add(new BigDecimal(Math.log(coeff_c[t])));
 		}
 		for(int i=0;i<prior.length;i++){
 			rv = rv + hat_alpha[hat_alpha.length-1][i];
 		}
 		return ((new BigDecimal(Math.log(rv))).subtract(C_T));
 	}
 	
 	public BigDecimal calculateSeqLogLikelihood(double[][] alpha){
 		double rv = 0.0;
 		for(int i=0;i<prior.length;i++){
 			rv +=alpha[alpha.length-1][i];
 		}
 		return new BigDecimal(Math.log(rv));
 	}
 	
 	public BigDecimal loglikeForSeq(double[][] logalpha){
 		double rv = Double.NEGATIVE_INFINITY;
 		for(int i=0;i<prior.length;i++){
 			rv = elnsum(rv,logalpha[logalpha.length-1][i]);
 		}
 		return new BigDecimal(rv);
 	}
 	
 	public void updatePrior(double[] newPrior, double[][] gamma, int numSequences){
 		synchronized(newPrior){
 			for(int i=0;i<prior.length;i++){
 				newPrior[i] += gamma[0][i]/numSequences;
 			}
 		}
 	}
 	
 	public void updatePriorLog(double[] newPrior, double[][] loggamma, int numSequences){
 		synchronized(newPrior){
 			for(int i=0;i<prior.length;i++){
 				newPrior[i] += Math.exp(loggamma[0][i])/numSequences;
 			}
 		}
 	}
 	
 	public void updateTransitions(	ArrayList<Integer> seq,
 									double[][][] transNumerator, 
 									double[][][] transDenominator,
 									double[][][] xi,
 									double[][] gamma){
 		for(int i=0;i<prior.length;i++){
 			for(int j=0;j<prior.length;j++){
 				for(int k=0;k<transNumerator[i][j].length;k++){
 					double numSum = 0.0, denomSum = 0.0;
 					for(int t=0;t<seq.size();t++){
 						if(k== bip.getSwitchAtIDX(seq.get(t))){
 							numSum += xi[t][i][j];
 							denomSum += gamma[t][i];
 						}
 					}
 					synchronized(transitionMonitors[i][j][k]){
 						transNumerator[i][j][k] += numSum;
 						transDenominator[i][j][k] +=denomSum;
 					}
 				}
 			}
 		}		
 	}
 
 	public void updateTransitionsLog(	ArrayList<Integer> seq,
 										double[][][] logtransNumerator, 
 										double[][][] logtransDenominator,
 										double[][][] logxi,
 										double[][] loggamma){
 		for(int i=0;i<prior.length;i++){
 			for(int j=0;j<prior.length;j++){
 				for(int k=0;k<logtransNumerator[i][j].length;k++){
 					double numSum = Double.NEGATIVE_INFINITY, denomSum = Double.NEGATIVE_INFINITY;
 					for(int t=0;t<seq.size();t++){
 						if(k== bip.getSwitchAtIDX(seq.get(t))){
 							numSum = elnsum(numSum,logxi[t][i][j]);
 							denomSum = elnsum(denomSum,loggamma[t][i]);
 						}
 					}
 					synchronized(transitionMonitors[i][j][k]){
 						logtransNumerator[i][j][k] = elnsum(logtransNumerator[i][j][k], numSum);
 						logtransDenominator[i][j][k] = elnsum(logtransDenominator[i][j][k], denomSum);
 					}
 				}
 			}
 		}		
 	}
 
 	
 	public void updatePartition(ArrayList<Integer> seq, double[][] gamma, int[] newPartition){
 		for(int t=0;t<gamma.length;t++){
 			int max_state = 0;
 			for(int i=1;i<gamma[t].length;i++){
 				if(gamma[t][i] > gamma[t][max_state]){
 					max_state = i;
 				}
 			}
 			newPartition[seq.get(t)] = max_state;
 		}
 	}
 	
 	public void updatePartition(ArrayList<Integer> seq, KernelDensityEstimator[] b, int[] newPartition){
 		double[][] delta = new double[seq.size()][prior.length];
 		int[][] psi = new int[seq.size()][prior.length];
 		for(int i=0;i<prior.length;i++){
 			delta[0][i] = prior[i]*b[i].estimate(bip.getDataAtIDX(seq.get(0)),bandwidth);
 			psi[0][i] = 0;
 		}
 		for(int t=1;t<seq.size();t++){
 			for(int j=0;j<prior.length;j++){
 				int maxState = 0;
 				double max = delta[t-1][0], tmp;
 				max *= transitionFunction[0][j][bip.getSwitchAtIDX(seq.get(t-1))];
 				for(int i=1;i<prior.length;i++){
 					tmp = delta[t-1][i] * transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t-1))];
 					if(tmp > max){ 
 						max = tmp;
 						maxState = i;
 					}
 				}
 				delta[t][j] = max * b[j].estimate(bip.getDataAtIDX(seq.get(t)),bandwidth);
 				psi[t][j] = maxState;
 			}
 		}
 		int tmpMaxState = 0;
 		for(int i=0;i<prior.length;i++){
 			if(delta[seq.size()-1][i] > delta[seq.size()-1][tmpMaxState]){
 				tmpMaxState = i;
 			}
 		}
 		
 		//synchronized(newPartition){
 			newPartition[seq.get(seq.size()-1)] = tmpMaxState;
 			for(int t = seq.size()-2;t >=0;t--){
 				newPartition[seq.get(t)] = psi[t+1][newPartition[seq.get(t+1)]];
 			}
 		//}
 	}
 
 	public void updatePartitionLog(ArrayList<Integer> seq, KernelDensityEstimator[] b, int[] newPartition){
 		double[][] logdelta = new double[seq.size()][prior.length];
 		int[][] psi = new int[seq.size()][prior.length];
 		for(int i=0;i<prior.length;i++){
 			double outputLog = outputLogProbAtIDX(seq.get(0),i);
 			//logdelta[0][i] = Math.log(prior[i])+Math.log(b[i].estimate(bip.getDataAtIDX(seq.get(0)),bandwidth));
 			logdelta[0][i] = Math.log(prior[i])+outputLog;
 			psi[0][i] = 0;
 		}
 		for(int t=1;t<seq.size();t++){
 			for(int j=0;j<prior.length;j++){
 				int maxState = 0;
 				double max = logdelta[t-1][0], tmp;
 				max += Math.log(transitionFunction[0][j][bip.getSwitchAtIDX(seq.get(t-1))]);
 				for(int i=1;i<prior.length;i++){
 					tmp = logdelta[t-1][i] + Math.log(transitionFunction[i][j][bip.getSwitchAtIDX(seq.get(t-1))]);
 					if(tmp > max){ 
 						max = tmp;
 						maxState = i;
 					}
 				}
 				double outputLog = outputLogProbAtIDX(seq.get(t),j);
 				//logdelta[t][j] = max + Math.log(b[j].estimate(bip.getDataAtIDX(seq.get(t)),bandwidth));
 				logdelta[t][j] = max + outputLog;
 				psi[t][j] = maxState;
 			}
 		}
 		int tmpMaxState = 0;
 		for(int i=0;i<prior.length;i++){
 			if(logdelta[seq.size()-1][i] > logdelta[seq.size()-1][tmpMaxState]){
 				tmpMaxState = i;
 			}
 		}
 		
 		//synchronized(newPartition){
 			newPartition[seq.get(seq.size()-1)] = tmpMaxState;
 			for(int t = seq.size()-2;t >=0;t--){
 				newPartition[seq.get(t)] = psi[t+1][newPartition[seq.get(t+1)]];
 			}
 		//}
 	}
 	
 	public void updateCompleteLLGamma(ArrayList<Integer> seq, double[][] loggamma){
 		for(int t=0;t<seq.size();t++){
 			for(int i=0;i<prior.length;i++){
 				completeLLGamma[seq.get(t)][i] = loggamma[t][i];
 			}
 		}
 	}
 	public void addToKDE(KernelDensityEstimator[] b, int[] newPartition){
 		for(int t=0;t<newPartition.length;t++){
 			//b[partition[t]].add(bip.getDataAtIDX(t));
 			for(int i=0;i<prior.length;i++){
 				b[i].add(bip.getDataAtIDX(t),Math.exp(completeLLGamma[t][i]));
 			}
 		}
 	}
 
 	
 	public void learn(double epsilon) throws IOException{ learn(bip.getSequences(), epsilon); }
 	
 	public void learn(final ArrayList<ArrayList<Integer>> sequences, double epsilon) throws IOException{
 		boolean converged = false;
 		int iter = 0;
 		BigDecimal prevLL = null;
 		BigDecimal eps = new BigDecimal(epsilon);
 		//until transitionFunction/prior/partition has converged:
 		do{
 			System.out.println("Iteration "+(iter+1));
 			iter++;
 			final double[] newPrior = new double[prior.length];
 			final double[][][] newTransition = new double[newPrior.length][newPrior.length][(int)Math.pow(2,bip.numSwitches())];
 			final double[][][] newTransitionNumerator = new double[newPrior.length][newPrior.length][(int)Math.pow(2,bip.numSwitches())];
 			final double[][][] newTransitionDenominator = new double[newPrior.length][newPrior.length][(int)Math.pow(2,bip.numSwitches())];
 			for(int i=0;i<newPrior.length;i++){
 				for(int j=0;j<newPrior.length;j++){
 					for(int k=0;k<newTransitionNumerator[i][j].length;k++){
						//newTransitionNumerator[i][j][k] = 0.0;
						//newTransitionDenominator[i][j][k] = 0.0;
						newTransitionNumerator[i][j][k] = Double.NEGATIVE_INFINITY;
						newTransitionDenominator[i][j][k] =Double.NEGATIVE_INFINITY;
 						newTransition[i][j][k] = 0.0;
 					}
 				}
 			}
 			System.out.println("Initializing KDE's");
 			final int[] newPartition = new int[partition.length];
 			for(int i=0;i<newPartition.length;i++) newPartition[i] = -1;
 			//final KernelDensityEstimator[] b = new KernelDensityEstimator[prior.length];
 			double[] datapoint;
 			/*
 			for(int i=0;i<b.length;i++){
 				b[i] = new KernelDensityEstimator(dim,new KernelDensityEstimator.NormalKernel(kernelSigma));
 				for(int j=0;j<partition.length;j++){
 					if(partition[j] == i){
 						b[i].add(bip.getDataAtIDX(j));
 					}
 				}
 			}
 			*/
 			//do each sequence in parallel
 			/* */
 			final ArrayList<Integer> tmpSeqIdx = new ArrayList<Integer>(sequences.size());
 			final BigDecimal[] loglike = new BigDecimal[sequences.size()];
 			for(int s=0;s<sequences.size();s++){ 
 				tmpSeqIdx.add(s);
 			}
 			Thread[] threads = new Thread[(int)Math.min(numThreads,sequences.size())];
 			for(int th=0;th<threads.length;th++){
 				threads[th] = new Thread(new Runnable(){
 						public void run(){
 							int seqsLeft;
 							synchronized(tmpSeqIdx){
 								seqsLeft = tmpSeqIdx.size();
 							}
 							while(seqsLeft > 0){
 								ArrayList<Integer> seq;
 								int tmpIdx = -1;
 								synchronized(tmpSeqIdx){
 									tmpIdx = tmpSeqIdx.remove(0);
 								}
 								seq = sequences.get(tmpIdx);
 								//do the crap
 								/* scaled BW 
 								double[][] hat_alpha = new double[seq.size()][prior.length];
 								double[] coeff_c = new double[seq.size()];
 								calculateScaledAlpha(seq, b, hat_alpha, coeff_c);
 								double[][] hat_beta = new double[seq.size()][prior.length];
 								calculateScaledBeta(seq, b, hat_beta, coeff_c);
 								double[][][] xi = new double[seq.size()][prior.length][prior.length];
 								calcXiFromScaled(seq,b,hat_alpha,hat_beta,xi);
 								double[][] gamma = new double[seq.size()][prior.length];
 								calcGammaFromXi(xi,gamma);
 
 								updatePrior(newPrior,gamma,sequences.size());
 								updateTransitions(seq,newTransitionNumerator, newTransitionDenominator, xi, gamma);
 								updatePartition(seq, b, newPartition);
 								//updatePartition(seq, gamma, newPartition);
 								loglike[tmpIdx] = calculateSeqLogLikelihood(hat_alpha,coeff_c);
 
 								/* */
 								
 								/* regular BW 
 								double[][] alpha = new double[seq.size()][prior.length];
 								calculateAlpha(seq,b,alpha);
 								double[][] beta = new double[seq.size()][prior.length];
 								calculateBeta(seq,b,beta);
 								double[][][] xi = new double[seq.size()][prior.length][prior.length];
 								calculateXi(seq,b,alpha,beta,xi);
 								double[][] gamma = new double[seq.size()][prior.length];
 								calcGammaFromXi(xi,gamma);
 
 								updatePrior(newPrior,gamma,sequences.size());
 								updateTransitions(seq,newTransitionNumerator, newTransitionDenominator, xi, gamma);
 								updatePartition(seq, b, newPartition);
 								//updatePartition(seq, gamma, newPartition);
 								loglike[tmpIdx] = calculateSeqLogLikelihood(alpha);
 								/* */
 								
 								/* Log-space BW */
 								double[][] logalpha = new double[seq.size()][prior.length];
 								calculateLogAlpha(seq, b, logalpha);
 								double[][] logbeta = new double[seq.size()][prior.length];
 								calculateLogBeta(seq, b, logbeta);
 								//double tmpa=Double.NEGATIVE_INFINITY, tmpb=Double.NEGATIVE_INFINITY;
 								//for(int i=0;i<prior.length;i++){
 								//	tmpa = elnsum(tmpa,logalpha[seq.size()-1][i]);
 								//	tmpb = elnsum(tmpb,logbeta[0][i]+Math.log(prior[i])+Math.log(b[i].estimate(bip.getDataAtIDX(seq.get(0)),bandwidth)));
 								//}
 								//System.out.println("tmpa: "+tmpa);
 								//System.out.println("tmpb: "+tmpb);
 								double[][][] logxi = new double[seq.size()][prior.length][prior.length];
 								calculateLogXi(seq, b, logalpha, logbeta, logxi);
 								double[][] loggamma = new double[seq.size()][prior.length];
 								calculateLogGamma(logalpha, logbeta, loggamma);
 								//loggamma[t][i] == sum(logxi[t][i])
 								//for(int t=0;t<seq.size();t++){
 								//	for(int i=0;i<prior.length;i++){
 								//		double tmpsum = Double.NEGATIVE_INFINITY;
 								//		for(int j=0;j<prior.length;j++){
 								//			tmpsum = elnsum(tmpsum,logxi[t][i][j]);
 								//		}
 								//		if(tmpsum != loggamma[t][i]){
 								//			System.out.println(Math.exp(tmpsum)+" != "+Math.exp(loggamma[t][i]));
 								//		}
 								//	}
 								//}
 								updateCompleteLLGamma(seq, loggamma);
 								updatePriorLog(newPrior, loggamma, sequences.size());
 								updateTransitionsLog(seq, newTransitionNumerator, newTransitionDenominator, logxi, loggamma);
 								updatePartitionLog(seq, b, newPartition);
 								loglike[tmpIdx] = loglikeForSeq(logalpha);
 								/* */
 
 								System.out.println("Sequence "+tmpIdx+" completed");
 								//end do the crap
 								synchronized(tmpSeqIdx){
 									seqsLeft = tmpSeqIdx.size();
 								}
 							}
 						}
 					});
 				threads[th].start();
 			}
 			try{
 				for(int th=0;th<threads.length;th++){
 					threads[th].join();
 				}
 			}catch(InterruptedException ie){
 				throw new RuntimeException(ie);
 			}
 			/* */
 			System.out.print("New Partition: [");
 			for(int i=0;i<newPartition.length;i++) System.out.print(newPartition[i]);
 			System.out.println("]");
 			/* */
 			
 			
 			System.out.println("ASSUMING NUM/DENOM ARE LOG!!!!");
 			for(int i=0;i<prior.length;i++){
 				for(int j=0;j<prior.length;j++){
 					for(int k=0;k<newTransition[i][j].length;k++){
						if(newTransitionNumerator[i][j][k] == newTransitionDenominator[i][j][k]){
							newTransition[i][j][k] = 1.0/((double)prior.length*prior.length);//continue;
							//System.out.println("whaaaat.");
							//System.out.println("Num:"+newTransitionNumerator[i][j][k]);
						}else{
							newTransition[i][j][k] = Math.exp(newTransitionNumerator[i][j][k]-newTransitionDenominator[i][j][k]);// transitionFunction[i][j][k];//
						}
 					}
 				}
 			}
 			
 			//now. Compute the difference between this new model and the old one
 			double priorDifference = 0.0, priorSum=0.0;
 			for(int i=0;i<prior.length;i++){
 				priorDifference += Math.abs(prior[i] - newPrior[i]);
 				priorSum = priorSum + prior[i];
 			}
 			if(priorSum != 1.0){
 				System.out.println("sum_i(prior) = "+priorSum);
 				//throw new RuntimeException("prior not summing to 1");
 			}
 			double transitionDiff = 0.0;
 			for(int i=0;i<prior.length;i++){
 				for(int j=0;j<prior.length;j++){
 					for(int k=0;k<newTransition[i][j].length;k++){
 						transitionDiff += Math.abs(newTransition[i][j][k] - transitionFunction[i][j][k]);
 					}
 				}
 			}
 			double percentPartChanged = 0.0;
 			for(int i=0;i<partition.length;i++){
 				if(partition[i] != newPartition[i]) percentPartChanged += 1.0;
 			}
 			percentPartChanged /= partition.length;
 			System.out.println("Prior delta: "+priorDifference);
 			System.out.println("Transition delta: "+ transitionDiff);
 			System.out.println("Partition delta: "+ percentPartChanged);
 			BigDecimal llsum = BigDecimal.ZERO;
 			for(int i=0;i<loglike.length;i++){
 				llsum = llsum.add(loglike[i]);
 			}
 			System.out.println("Sum log-likelihood of all sequences: "+llsum);
 			
 			//if(priorDifference < epsilon && transitionDiff < epsilon && percentPartChanged < epsilon) converged = true;
 			if(prevLL!= null && eps.compareTo(llsum.subtract(prevLL).abs()) > 0) converged = true;
 			if(prevLL!=null && prevLL.compareTo(llsum) > 0) System.out.println("Log-likelihood DECREASING!");
 			prevLL = llsum;
 			prior = newPrior;
 			transitionFunction = newTransition;
 			partition = newPartition;
 			addToKDE(b, newPartition);
 			if(PRINT_ITERATIONS){
 				writeParameters(new File("biohmm_parameters_Iteration_"+iter+".txt"),llsum);
 			}
 		} while(!converged);
 	}
 
 	public void writeParameters(File parameterFile) throws IOException{
 		writeParameters(parameterFile,null);
 	}
 	
 	public void writeParameters(File parameterFile, BigDecimal loglike) throws IOException{
 		FileWriter outf = new FileWriter(parameterFile);
 		//write prior
 		outf.write(prior.length+"\n");
 		for(int i=0;i<prior.length;i++){
 			outf.write(prior[i]+" ");
 		}
 		outf.write("\n");
 		//write transition function
 		outf.write(transitionFunction.length+" ");
 		outf.write(transitionFunction[0].length+" ");
 		outf.write(transitionFunction[0][0].length+"\n");
 		for(int i=0;i<transitionFunction.length;i++){
 			for(int j=0;j<transitionFunction[i].length;j++){
 				for(int k=0;k<transitionFunction[i][j].length;k++){
 					outf.write(transitionFunction[i][j][k]+" ");
 				}
 			}
 		}
 		outf.write("\n");
 		//write partition
 		outf.write(partition.length+"\n");
 		for(int i=0;i<partition.length;i++){
 			outf.write(partition[i]+" ");
 		}
 		outf.write("\n");
 		//write KDE weights
 		for(int i=0;i<b.length;i++){
 			//datapoint then weight
 			//x1 x2 x3 ... xD weight
 			outf.write(i+"\n");
 			for(int j=0;j<b[i].samples.size();j++){
 				double[] sample = b[i].samples.get(j);
 				for(int d=0;d<sample.length;d++){
 					outf.write(sample[d]+" ");
 				}
 				outf.write(b[i].weights.get(j)+"\n");
 			}
 			outf.write("\n");
 		}
 		outf.write("#LogLike = "+loglike+"\n");
 		outf.close();
 	}
 	
 	public static void main(String[] args){
 		if(args.length != 1){
 			System.out.println("Usage: java BIOHMM <btfDirectory>");
 		} else {
 			try{
 				BTFData btf = new BTFData();
 				btf.loadDir(new File(args[0]));
 				ArrayList<ArrayList<Integer>> sequences;
 				BIOHMMInputParser bip = new BIOHMMInputParser(btf);//SimpleInputParser(btf);//
 				BIOHMM biohmm = new BIOHMM(2,bip);
 				sequences = bip.getSequences();
 				System.out.println("Num sequences:"+sequences.size());
 				biohmm.learn(sequences,0.000);
 				System.out.println("Prior:");
 				System.out.print("\t[ ");
 				for(int i =0;i<biohmm.prior.length;i++){
 					System.out.print(biohmm.prior[i]+" ");
 				}
 				System.out.println("]");
 				System.out.println("Transition matrix:");
 				for(int k=0;k<biohmm.transitionFunction[0][0].length;k++){
 					System.out.println("Binary switch: "+k);
 					for(int i=0;i<biohmm.transitionFunction.length;i++){
 						System.out.print("\t [");
 						for(int j=0;j<biohmm.transitionFunction[i].length;j++){
 							System.out.print(" "+biohmm.transitionFunction[i][j][k]);
 						}
 						System.out.println(" ]");
 					}
 				}
 				System.out.println("Partition:");
 				System.out.print("[");
 				for(int i=0;i<biohmm.partition.length;i++){
 					System.out.print(" "+biohmm.partition[i]);
 				}
 				System.out.println(" ]");
 				biohmm.writeParameters(new File(new File(System.getProperties().getProperty("user.dir")),"biohmm_parameters.txt"));
 			} catch(Exception e){
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	
 }
