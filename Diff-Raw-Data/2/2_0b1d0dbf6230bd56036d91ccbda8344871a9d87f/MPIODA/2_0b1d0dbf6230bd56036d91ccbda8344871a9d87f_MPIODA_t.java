 import java.io.File;
 import java.util.Vector;
 
 import mpi.Datatype;
 import util.KullBackLeibler;
 
 import mpi.MPI;
 import mpi.MPIException;
 import incrementallda.IncrEstimator;
 import incrementallda.MixedDataset;
 import jgibblda.Document;
 import jgibblda.LDACmdOption;
 
 
 public class MPIODA {
 
 	static int phase;
 	static int[] nw, nwsum, nw_p, nwsum_p;
 	static int[][][] nd_p; // phases * batch * nbr_wor_in_doc
 	static int[][] ndsum_p;
 	static Vector<Integer>[][] z_p; // z[i][d] : assignment vector for the d^th document in i^th phase
 	static Document[][] data_p;
 	static int K = 10;
 	static int V, M;
 	static int basis_size = 1000;
 	static int batch_size = 100;
 	static double alpha = 50.0 / K;
 	static double beta = 0.1;
 	static double[] p;
 	static int[] indices; // indices of the current document for each process
 	static int rank;
 	static int size;
 	static int root = 0;
 	static int niters = 10;
 	
 	public static void main(String[] args) {
 		MPI.Init(args);
 		rank = MPI.COMM_WORLD.Rank();
 		size = MPI.COMM_WORLD.Size();
 		int last_process = size-1; // process taking care of the last new batch of data
 		int next_process = 0; // process that are going to taking care of the new batch of data
 
 		/**
 		 * Declare data only initialized by root process
 		 */
 		MixedDataset dataset = null;
 		int[] parameters = new int[2];
 		Object[] data = null;
 		double[] theta_all = null;
 		int[] indices_all = null;
 		/**
 		 * Initialize data for root process
 		 */
 		if (rank == root) {
 			String dir = "/home/larsen/idea-IC-107.587/Online-Document-Aligner/corpus/lda";
 			dataset = MixedDataset.readDataSet(dir + File.separator + "en_2005_02.bag", dir + File.separator + "ensy_2005_02.bag");
 			parameters[0] = dataset.V;
 			parameters[1] = dataset.M;
 			data = dataset.docs;
 			theta_all = new double[basis_size*K];
 			indices_all = new int[basis_size];
 		}
 		/**
 		 *  Broadcast and scatter data to other processes
  		 */
 		MPI.COMM_WORLD.Bcast(parameters, 0, parameters.length, MPI.INT, root);
 		V = parameters[0];
 		M = parameters[1];
 		
 		int num_batch = (M - (basis_size - batch_size)) / batch_size;
 		int phase_size = M / basis_size;
 
 		nd_p = new int[phase_size][batch_size][K];
 		ndsum_p = new int[phase_size][batch_size];
 		z_p = new Vector[phase_size][batch_size];
 		data_p = new Document[phase_size][batch_size];
 		nw = new int[V*K];
 		nwsum = new int[K];
 		p = new double[K];
 
 		for (int phase = 0; phase < phase_size; phase++) {
 			Object[] data_p_local = new Object[batch_size];
 			MPI.COMM_WORLD.Scatter(data, phase*basis_size, batch_size, MPI.OBJECT, data_p_local, 0, batch_size, MPI.OBJECT, root);
 			for (int i = 0; i < batch_size; i++)
 				data_p[phase][i] = (Document) data_p_local[i];
 		}
 
 		/**
 		 *  Initialize:
 		 *  Calculate nw_p and nwsum_p and distribute them to all processes
  		 */
 		phase = 0;
 		nw_p = new int[V*K];
 		nwsum_p = new int[K];
 		initialSample(true);
 		indices = computeIndices();
 		MPI.COMM_WORLD.Allreduce(nw_p, 0, nw, 0, V*K, MPI.INT, MPI.SUM);
 		MPI.COMM_WORLD.Allreduce(nwsum_p, 0, nwsum, 0, K, MPI.INT, MPI.SUM);
 
 		/**
 		 *  Start Estimation
  		 */
 		for (int batch = 0; batch < num_batch; batch++) {
 			if (rank == root)
 				System.out.println("Processing on basis documents, with " + batch + " batches added and removed.");
 			/**
 			 *  Sample
  			 */
 			for (int iter = 0; iter < (batch==0 ? niters*size : niters); iter++) {
 				/**
 				 * Clear the number of instances of a word assigned to a topic,
 				 * and also the number of words assigned to a topic.
 				 */
 				nw_p = new int[V*K];
 				nwsum_p = new int[K];
 				/**
 				 * Sample
 				 */
 				for (int m = 0; m < batch_size; m++){				
 					for (int n = 0; n < z_p[phase][m].size(); n++){
 						int topic = sample(m,n);
 						z_p[phase][m].set(n, topic);
 					}// end for each word
 				}// end for each document
 
 				/**
 				 * If it's the last round of sampling, remove the previous batch from the next process so it's ready
 				 * to receive new batch.
 				 */
 				if (iter == (batch==0 ? niters*size : niters) - 1) {
 					if (next_process == rank) {
 						nw_p = new int[V*K];
 						nwsum_p = new int[K];
 					}
 					MPI.COMM_WORLD.Reduce(nw_p, 0, nw, 0, V*K, MPI.INT, MPI.SUM, next_process);
 					MPI.COMM_WORLD.Reduce(nwsum_p, 0, nwsum, 0, K, MPI.INT, MPI.SUM, next_process);
 				} else {
 					/**
 					 * Update nw and nwsum for all processes
  					 */
 					MPI.COMM_WORLD.Allreduce(nw_p, 0, nw, 0, V*K, MPI.INT, MPI.SUM);
 					MPI.COMM_WORLD.Allreduce(nwsum_p, 0, nwsum, 0, K, MPI.INT, MPI.SUM);
 				}
 			}
 
 			/**
 			 * Compute best matches for the current batch by sending necessary info to root process
 			 */
 			double[] theta = computeTheta();
 			MPI.COMM_WORLD.Gather(theta, 0, K*batch_size, MPI.DOUBLE, theta_all, 0, K*batch_size, MPI.DOUBLE, root);
 			MPI.COMM_WORLD.Gather(indices, 0, batch_size, MPI.INT, indices_all, 0, batch_size, MPI.INT, root);
 			/**
 			 * Represent result for the batch by use of root process
 			 */
 			if (rank == root) {
 				representResult(batch, theta_all, last_process, dataset, indices_all);
 			}
 			/*
			 Reassign the next process to the new batch and update nw for all processes
 			 */
 			if (rank == next_process) {
 				phase++;
 				initialSample(false);
 				indices = computeIndices();
 				System.out.println("Process " + rank + " takes the lead");
 			}
 			MPI.COMM_WORLD.Bcast(nw, 0, V*K, MPI.INT, next_process);
 			/**
 			 * Shift the processes
 			 */
 			last_process = (last_process + 1) % size;
 			next_process = (next_process + 1) % size;
 		}
 
 		MPI.Finalize();
 	}
 
 	/*
 	Returns the document indices from the current working batch
 	 */
 	private static int[] computeIndices() {
 		int[] ret = new int[batch_size];
 		for (int i = 0; i < batch_size; i++) {
 			ret[i] = data_p[phase][i].index;
 		}
 		return ret;
 	}
 
 	private static void initialSample(boolean first_sampling) {
 		for (int m = 0; m < batch_size; m++){
 			int N = data_p[phase][m].length;
 			z_p[phase][m] = new Vector<Integer>();
 			
 			//initiliaze for z_p
 			for (int n = 0; n < N; n++){
 				int topic = (int)Math.floor(Math.random() * K);
 				z_p[phase][m].add(topic);
 				
 				// number of instances of word assigned to topic j
 				int w = data_p[phase][m].words[n];
 				if (first_sampling)
 					nw_p[topic*V + w] += 1;
 				else
 					nw[topic*V + w] += 1;
 				// number of words in document i assigned to topic j
 				nd_p[phase][m][topic] += 1;
 				
 				if (first_sampling)
 					nwsum_p[topic] += 1;
 				else
 					nwsum[topic] += 1;
 			}
 			// total number of words in document i
 			ndsum_p[phase][m] = N;
 		}
 	}
 
 	public static int sample(int m, int n){
 		/**
 		 * Remove z_i from the count variable
  		 */
 		int topic = z_p[phase][m].get(n);
 		int w = data_p[phase][m].words[n];
 
 		nw[w+topic*V] -= 1;
 		nd_p[phase][m][topic] -= 1;
 		nwsum[topic] -= 1;
 		ndsum_p[phase][m] -= 1;
 		
 		double Vbeta = V * beta;
 		double Kalpha = K * alpha;
 
 		/**
 		 * Do multinominal sampling via cumulative method
 		 */
 		for (int k = 0; k < K; k++) {
 			p[k] = (nw[w+k*V] + beta)/(nwsum[k] + Vbeta) *
 					(nd_p[phase][m][k] + alpha)/(ndsum_p[phase][m] + Kalpha);
 		}
 
 		/**
 		 * Cumulate multinomial parameters
  		 */
 		for (int k = 1; k < K; k++){
 			p[k] += p[k - 1];
 		}
 
 		/**
 		 * Scaled sample because of unnormalized p[]
  		 */
 		double u = Math.random() * p[K - 1];
 
 		/**
 		 * Sample topic w.r.t distribution p
 		 */
 		for (topic = 0; topic < K; topic++){
 			if (p[topic] > u)
 				break;
 		}
 
 		/**
 		 * Add newly estimated z_i to count variables
  		 */
 		nw[w+V*topic] += 1;
 		nd_p[phase][m][topic] += 1;
 		nwsum[topic] += 1;
 		ndsum_p[phase][m] += 1;
 		nw_p[w+V*topic] += 1;
 		nwsum_p[topic] += 1;
 		
  		return topic;
 	}
 
 	public static double[] computeTheta(){
 		double[] ret = new double[batch_size*K];
 		for (int m = 0; m < batch_size; m++){
 			for (int k = 0; k < K; k++){
 				ret[m*K+k] = (nd_p[phase][m][k] + alpha) / (ndsum_p[phase][m] + K * alpha);
 			}
 		}
 		return ret;
 	}
 
 	private static double[][] getTheta2d(double[] theta_all){
 		double[][] theta = new double[basis_size][K]; // 2d representation of theta_all
 				for (int i = 0; i < basis_size; i++) {
 					for (int k = 0; k < K; k++) {
 						theta[i][k] = theta_all[k+i*K];
 					}
 				}
 		return  theta;
 	}
 
 	private static void representResult(int batch, double[] theta_all, int last_process, MixedDataset dataset,
 	                                    int[] indices_all){
 		double[][] theta2d = getTheta2d(theta_all);
 		int start_q = batch==0 ? 0 : batch_size*last_process;
 		int end_q = batch==0 ? basis_size : batch_size*(last_process+1);
 		for (int q = start_q; q < end_q; q++) {
 			double min_div = Double.MAX_VALUE;
 			int best = -1;
 			for (int c = 0; c < basis_size; c++) {
 				/**
 				 * If it's the same document, skip it
 				 */
 				if (c == q)
 					continue;
 				/**
 				 * If it's the same language, skip it
 				 */
 				if (dataset.type[indices_all[c]] == dataset.type[indices_all[q]])
 					continue;
 				/**
 				 * Else caluclate the similarities between the documents
 				 */
 				double js_div = KullBackLeibler.sym_divergence(theta2d[c], theta2d[q]);
 				if (js_div < min_div) {
 					min_div = js_div;
 					best = c;
 				}
 			}
 			/**
 			 * If they are similiar, show it.
 			 */
 			if (min_div < 0.005) {
 				System.out.println("======================================");
 				System.out.println("Score for (" + indices_all[q] + ", " + indices_all[best] + ") = " + min_div);
 				System.out.println("--------------------------------------");
 				System.out.println(dataset.getRawDoc(indices_all[q]));
 				System.out.println("--------------------------------------");
 				System.out.println(dataset.getRawDoc(indices_all[best]));
 				System.out.println("======================================");
 			}
 		}
 	}
 }
 
 
