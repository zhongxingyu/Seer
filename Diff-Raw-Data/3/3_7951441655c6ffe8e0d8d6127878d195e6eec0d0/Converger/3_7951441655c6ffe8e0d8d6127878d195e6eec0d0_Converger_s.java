 package worker;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import obj.Chromosome;
 import obj.DataFile;
 import obj.Genome;
 import obj.ValIdx;
 
 import org.apache.commons.math.MathException;
 import org.apache.commons.math.distribution.NormalDistributionImpl;
 
 import util.StatOps;
 
 public class Converger extends DistributedWorker{
 	private static float zThreshold = 10;
 	private static int maxIter = 150;
 	private static boolean rankBased = false;
 	private static int attractorSize = 10; // minimum size of an attractor
 	private static String convergeMethod = "FIXEDSIZE";
 	private static int bins = 7;
 	private static int splineOrder = 3;
 	private static boolean miNorm = false;
 	private static float precision = (float) 1E-4;
 	static ITComputer itc;
 	
 	
 	/*public static class ValIdx implements Comparable<ValIdx>{
 		float val;
 		int idx;
 		public ValIdx(int i, float v){
 			this.idx = i;
 			this.val = v;
 		}
 		public int hashCode(){
 			return idx;
 		}
 		
 		public int compareTo(ValIdx other) {
 			return -Double.compare(this.val, other.val);
 		}
 		
 		public int idx(){
 			return idx;
 		}
 		public float val(){
 			return val;
 		}
 		public boolean equals(Object other){
 			boolean result = false;
 	        if (other instanceof ValIdx) {
 	        	ValIdx that = (ValIdx) other;
 	            result = (this.idx == that.idx);
 	        }
 	        return result;
 		}
 		
 	}*/
 	// search ValIdx array by its INDICES!
 	public static int biSearch(ValIdx[] x, ValIdx k){
 		int out = -1;
 		int n = x.length;
 		int r = n;
 		int l = 0;
 		int m;
 		while(r > l){
 			System.out.println("(" + l + "\t" + r + ")");
 			m = (r+l)/2;
 			if(k.idx == x[m].idx){
 				return m;
 			}else if(k.idx > x[m].idx){
 				l = m;
 			}else if(k.idx < x[m].idx){
 				r = m;
 			}
 		}
 		return out;
 	}
 	
 	// Sort ValIdx BY INDICES!!! Note: sorting by value can be directly applied Arrays.sort()
 	public static ValIdx[] mergeSort(ValIdx[] x){
 		int n = x.length;
 		if(n <= 1) return x;
 		
 		ValIdx left[], right[],result[];
 		int m = n/2;
 		left = new ValIdx[m];
 		right = new ValIdx[n - m];
 		
 		for(int i = 0; i < m; i++){
 			left[i] = x[i];
 		}
 		for(int i = m; i < n; i++){
 			right[i-m] = x[i];
 		}
 		left = mergeSort(left);
 		right = mergeSort(right);
 		
 		result = merge(left, right);
 		
 		return result;
 	}
 	
 	private static ValIdx[] merge(ValIdx[] left, ValIdx[] right){
 		int nL = left.length;
 		int nR = right.length;
 		ValIdx[] result = new ValIdx[nL + nR];
 		int iL = 0, iR = 0, iOut = 0;;
 		while(iL < nL || iR < nR){
 			if(iL < nL && iR < nR){
 				if(left[iL].idx <= right[iR].idx){
 					result[iOut] = left[iL];
 					iL++; 
 				}else{
 					result[iOut] = right[iR];
 					iR++;
 				}
 			}else if(iL < nL){
 				result[iOut] = left[iL];
 				iL++; 
 			}else if(iR < nR){
 				result[iOut] = right[iR];
 				iR++;
 			}
 			iOut++;
 		}
 		return result;
 	}
 	
 	public Converger(int id, int totalComputers, long jobID){
 		super(id, totalComputers, jobID);
 	}
 	public Converger(int id, int totalComputers, long jobID, String method, int maxIter, boolean rankBased){
 		super(id, totalComputers, jobID);
 		Converger.maxIter = maxIter;
 		Converger.rankBased = rankBased;
 		Converger.convergeMethod = method;
 	}
 	private static float[] getMetaGene(float[][] data, ArrayList<ValIdx> idx, int n){
 		int m = idx.size();
 		float[] out = new float[n];
 		for(int j = 0; j < n; j++){
 			for(ValIdx vi : idx){
 				out[j] += data[vi.idx][j];
 			}
 			out[j] /= m;
 		}
 		return out;
 	}
 	private static float[] getMetaGene(float[][] data, int start, int len, int n){
 		float[] out = new float[n];
 		for(int j = 0; j < n; j++){
 			for(int l = start; l < (start + len); l++){
 				out[j] += data[l][j];
 			}
 			out[j] /= len;
 		}
 		return out;
 	}
 	private static double sigmoid(double x, double a){
 		return (1/(1 + Math.exp(-2 * Math.PI * a * x)));
 	}
 	private static float[] getWeightedMetaGene(float[][] data, float[] w, float power, int m, int n){
 		float[] out = new float[n];
 		double sum = 0;
 		//float[] z = StatOps.xToZ(w, m);
 		//float[] r = StatOps.rank(w);
 		for(int i = 0; i < m; i++){
 			//if( (z[i]) > 0){
 			//if(w[i] > 0){
 				//double ww = Math.exp(power*Math.log(w[i]));
 				double f = Math.exp(power*Math.log(w[i]));
 				//double sig =  Math.exp(power * Math.log(sigmoid(2*w[i]-1, 1.0)));
 				//double f = w[i] * sig;
 				//double f = sigmoid(2*w[i]-1, power);
 				//double f = sigmoid(2*r[i]/m - 1, power);
 				//double f = Math.tan(w[i] * Math.PI/2);
 				sum += f;
 				for(int j = 0; j < n; j++){
 					out[j] += data[i][j] * f;
 				}
 			//}
 		}
 		for(int j = 0; j < n; j++){
 			out[j] /= sum;
 		}
 		return out;
 	}
 	private static float[] getChrWeightedMetaGene(float[][] data, float[] w, ArrayList<String> genes, 
 			String chr, Genome gn, float power, int m, int n){
 		float[] out = new float[n];
 		double sum = 0;
 		//float[] z = StatOps.xToZ(w, m);
 		//float[] r = StatOps.rank(w);
 		for(int i = 0; i < m; i++){
 			String g = genes.get(i);
 			if(gn.contains(g)){
 				if(gn.getChr(g).equals(chr)){
 				//if( (z[i]) > 8){
 				//if(w[i] > 0){
 					//double ww = Math.exp(power*Math.log(w[i]));
 					double f = Math.exp(power*Math.log(w[i]));
 					//double sig =  Math.exp(power * Math.log(sigmoid(2*w[i]-1, 1.0)));
 					//double f = w[i] * sig;
 					//double f = sigmoid(2*w[i]-1, power);
 					//double f = sigmoid(2*r[i]/m - 1, power);
 					//double f = Math.tan(w[i] * Math.PI/2);
 					sum += f;
 					for(int j = 0; j < n; j++){
 						out[j] += data[i][j] * f;
 					}
 				}
 			}
 		}
 		for(int j = 0; j < n; j++){
 			out[j] /= sum;
 		}
 		return out;
 	}
 	private static float[] getSmoothedWeightedMetaGene(float[][] data, float[] w, float[] pw, float power, int m, int n){
 		float[] out = new float[n];
 		double sum = 0;
 		float vw = StatOps.var(w, m);
 		float a= vw / (vw + StatOps.var(pw, m));
 		for(int i = 0; i < m; i++){
 			if(w[i] > 0){
 				w[i] = a * w[i] + (1-a)*pw[i];
 				double f = (float) Math.exp(power*Math.log(w[i]));
 				sum += f;
 				for(int j = 0; j < n; j++){
 					out[j] += data[i][j] * f;
 				}
 			}
 		}
 		for(int j = 0; j < n; j++){
 			out[j] /= sum;
 		}
 		return out;
 	}
 	public static float calcMSE(float[] a, float[] b, int n){
 		float err = 0;
 		for(int i = 0; i < n; i++){
 			err += (a[i] - b[i]) * (a[i] - b[i]);
 		}
 		return err / n;
 	}
 	public static boolean equal(float[] a, float[] b, int n, float delta){
 		for(int i = 0; i < n; i++){
 			if(Math.abs(a[i] - b[i]) > delta){
 				//System.out.println(Math.abs(a[i] - b[i]));
 				return false;
 			}
 		}
 		return true;
 	}
 	public ArrayList<ValIdx> findAttractor(float[][] data, int idx) throws Exception{
 		int m = data.length;
 		int n = data[0].length;
 		
 		ITComputer itc = new ITComputer(bins, splineOrder, id, totalComputers, miNorm);
 		float[] mi = itc.getAllMIWith(data[idx], data);
 		ArrayList<ValIdx> metaIdx = new ArrayList<ValIdx>();
 		ValIdx[] vec = new ValIdx[m];
 		if(convergeMethod.equals("FIXEDSIZE")){
 			
 			for(int i = 0; i < m; i++){
 				vec[i] = new ValIdx(i, mi[i]);
 			}
 			Arrays.sort(vec);
 			for(int i = 0; i < attractorSize; i++){
 				metaIdx.add(vec[i]);
 			}
 		}else if(convergeMethod.equals("ZSCORE")){
 			float[] z = StatOps.xToZ(mi, m);
 			for(int i = 0; i < m; i++){
 				vec[i] = new ValIdx(i, mi[i]);
 			}
 			//Arrays.sort(vec);
 			for(int i = 0; i < m; i++){
 				if(z[i] > zThreshold){
 					metaIdx.add(vec[i]);
 				}
 			}
 			Collections.sort(metaIdx);
 		}
 		
 		int cnt = 0;
 		ArrayList<ValIdx> preMetaIdx = new ArrayList<ValIdx>();
 		preMetaIdx.addAll(metaIdx);
 		
 		while(cnt < maxIter){
 			
 			// cannot find significant associated genes, exit.
 			
 			if(metaIdx.size() == 0){
 				//System.out.println("Empty set, exit.");
 				break;
 			}
 			System.out.print("Iteration " + cnt + "...");
 			float[] metaGene = getMetaGene(data,metaIdx, n);
 			mi = itc.getAllMIWith(metaGene, data);
 			metaIdx = new ArrayList<ValIdx>();	
 			vec = new ValIdx[m];
 			for(int i = 0; i < m; i++){
 				vec[i] = new ValIdx(i, mi[i]);
 			}
 			if(convergeMethod.equals("FIXEDSIZE")){
 				Arrays.sort(vec);
 				for(int i = 0; i < attractorSize; i++){
 					metaIdx.add(vec[i]);
 				}
 			}else if(convergeMethod.equals("ZSCORE")){
 				float[] z = StatOps.xToZ(mi, m);
 				for(int i = 0; i < m; i++){
 					vec[i] = new ValIdx(i, mi[i]);
 				}
 				//Arrays.sort(vec);
 				for(int i = 0; i < m; i++){
 					if(z[i] > zThreshold){
 						metaIdx.add(vec[i]);
 					}
 				}
 				Collections.sort(metaIdx);
 			}
 			if(preMetaIdx.equals(metaIdx)){
 				System.out.println("Converged.");
 				break;
 			}else{
 				preMetaIdx = metaIdx;
 				System.out.println("Gene Set Size: " + metaIdx.size());
 				cnt++;
 			}
 			
 		}
 		if(cnt == maxIter){
 			System.out.println("Not converged.");
 		}
 		return metaIdx;
 		
 	}
 	public ArrayList<ValIdx> noThConverge(float[][] data, int idx) throws Exception{
 		int m = data.length;
 		int n = data[0].length;
 		
 		ITComputer itc = new ITComputer(bins, splineOrder, id, totalComputers, miNorm);
 		float[] mi = itc.getAllMIWith(data[idx], data);
 		ArrayList<ValIdx> metaIdx = new ArrayList<ValIdx>();
 		ValIdx[] vec = new ValIdx[m];
 		for(int i = 0; i < m; i++){
 			vec[i] = new ValIdx(i, mi[i]);
 		}
 		Arrays.sort(vec);
 		for(int i = 0; i < attractorSize; i++){
 			metaIdx.add(vec[i]);
 		}
 		int cnt = 0;
 		ArrayList<ValIdx> preMetaIdx = new ArrayList<ValIdx>();
 		preMetaIdx.addAll(metaIdx);
 		ArrayList<ValIdx> prepreMetaIdx = new ArrayList<ValIdx>();
 		boolean converged = false;
 		while(cnt < maxIter){
 			
 			// cannot find significant associated genes, exit.
 			
 			if(metaIdx.size() == 0){
 				//System.out.println("Empty set, exit.");
 				break;
 			}
 			System.out.print("Iteration " + cnt + "...");
 			float[] metaGene = getMetaGene(data,metaIdx, n);
 			mi = itc.getAllMIWith(metaGene, data);
 			metaIdx = new ArrayList<ValIdx>();	
 			vec = new ValIdx[m];
 			for(int i = 0; i < m; i++){
 				vec[i] = new ValIdx(i, mi[i]);
 			}
 			Arrays.sort(vec);
 			for(int i = 0; i < attractorSize; i++){
 				metaIdx.add(vec[i]);
 			}
 			
 			if(preMetaIdx.equals(metaIdx)){
 				System.out.println("Converged."); 
 				System.out.println("Gene Set Size: " + metaIdx.size());
 				converged = true;
 				break;
 			}else if (prepreMetaIdx.equals(metaIdx)){
 				System.out.println("Cycled.");
 				converged = true;
 				if(metaIdx.size() >= preMetaIdx.size()){
 					break;
 				}else{
 					metaIdx = preMetaIdx;
 					break;
 				}
 			}
 			else{
 				prepreMetaIdx = preMetaIdx;
 				preMetaIdx = metaIdx;
 				//System.out.println("Gene Set Size: " + metaIdx.size());
 				cnt++;
 			}
 			
 		}
 		if(!converged){
 			System.out.println("Not converged.");
 			metaIdx.clear();
 		}else{
 			System.out.println("Expanding...");
 			HashSet<Integer> base = new HashSet<Integer>();
 			int higher = metaIdx.size() * 2;
 			for(ValIdx vi : metaIdx){
 				base.add(vi.idx);
 			}
 			
 		}
 		return metaIdx;
 		
 	}
 	public void findCNV(float[][] data, float[][] val, ArrayList<Chromosome> chrs, float zth) throws Exception{
 		int n = data[0].length;
 		int m = data.length;
 		
 		int mm = 0;
 		for(Chromosome chr : chrs){
 			mm += chr.size();
 		}
 		int start = id * mm / totalComputers;
 		int end = (id+1) * mm / totalComputers;
 		
 		
 		System.out.println("Processing task " + (start+1) + " to " + end);
 		
 		ITComputer itc = new ITComputer(bins, splineOrder, id, totalComputers, miNorm);
 		//itc.negateMI(true);
 		prepare("geneset");
 		PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/geneset/caf." + String.format("%05d", id)+".txt"));
 		int tt = 0;
 		for(Chromosome chr : chrs){
 			System.out.println("At chromosome " + chr.name() + "...");
 			ArrayList<ValIdx> geneIdx = chr.geneIdx();
 			/*// sort gene idx ascendantly
 			Collections.sort(geneIdx);
 			Collections.reverse(geneIdx);
 			*/
 			int k = geneIdx.size();
 			
 			for(int i = 0; i < k; i++){
 				if(tt >= start && tt < end){
 					int idx = geneIdx.get(i).idx;
 					System.out.print("Processing " + tt + "...");
 					/*
 					 * Step 1: find the genes that are significantly associated with the seed gene 
 					 *         as the initial metagene
 					 */
 					float[] mi = itc.getAllMIWith(val[idx], val);
 					ArrayList<ValIdx> metaIdx = new ArrayList<ValIdx>();
 					
 					float[] z = StatOps.xToZ(mi, m);
 					ValIdx[] vec = new ValIdx[m];
 					for(int j = 0; j < m; j++){
 						vec[j] = new ValIdx(j, z[j]);
 					}
 					Arrays.sort(vec);
 					
 					if(convergeMethod.equals("FIXEDSIZE")){
 						for(int j = 0; j < m; j++){
 							if(metaIdx.size() < attractorSize){
 								if(geneIdx.contains(vec[j])){
 									metaIdx.add(vec[j]);
 								}
 							}else{
 								break;
 							}
 						}
 					}else if(convergeMethod.equals("ZSCORE")){
 						for(int j = 0; j < m; j++){
 							if(vec[j].val > zth){
 								if(geneIdx.contains(vec[j])){
 									metaIdx.add(vec[j]);
 								}
 							}else if(metaIdx.size() < attractorSize){
 								if(geneIdx.contains(vec[j])){
 									metaIdx.add(vec[j]);
 								}
 							}else{
 								break;
 							}
 						}
 					}
 					
 					int cnt = 0;
 					ArrayList<ValIdx> prepreMetaIdx = new ArrayList<ValIdx>();
 					ArrayList<ValIdx> preMetaIdx = new ArrayList<ValIdx>();
 					preMetaIdx.addAll(metaIdx);
 					//System.out.println("Initial gene set size " + metaIdx.size() );
 					
 					/*
 					 * Step 2: Calculate metagene, find the genes that have correlation exceeding the 
 					 *         threshold as the new metagene
 					 */
 					
 					while(cnt < maxIter){
 						
 						// cannot find significant associated genes, exit.
 						
 						if(metaIdx.size() == 0){
 							//System.out.println("Empty set, exit.");
 							break;
 						}
 						//System.out.print("Iteration " + cnt + "...");
 						float[] metaGene = getMetaGene(data,metaIdx, n);
 						if(rankBased){
 							metaGene = StatOps.rank(metaGene);
 						}
 						mi = itc.getAllMIWith(metaGene, val);
 						metaIdx = new ArrayList<ValIdx>();
 						vec = new ValIdx[m];
 						z = StatOps.xToZ(mi, m);
 						for(int j = 0; j < m; j++){
 							vec[j] = new ValIdx(j, z[j]);
 						}
 						Arrays.sort(vec);
 						metaIdx = new ArrayList<ValIdx>();
 						if(convergeMethod.equals("FIXEDSIZE")){
 							for(int j = 0; j < m; j++){
 								if(metaIdx.size() < attractorSize){
 									if(geneIdx.contains(vec[j])){
 										metaIdx.add(vec[j]);
 									}
 								}else{
 									break;
 								}
 							}
 						}else if(convergeMethod.equals("ZSCORE")){
 							for(int j = 0; j < m; j++){
 								if(vec[j].val > zth){
 									if(geneIdx.contains(vec[j])){
 										metaIdx.add(vec[j]);
 									}
 								}else if(metaIdx.size() < attractorSize){
 									if(geneIdx.contains(vec[j])){
 										metaIdx.add(vec[j]);
 									}
 								}else{
 									break;
 								}
 							}
 						}
 						
 						if(preMetaIdx.equals(metaIdx)){
 							System.out.print("Converged. "); 
 							System.out.println("Gene Set Size: " + metaIdx.size());
 							break;
 						}else if (prepreMetaIdx.equals(metaIdx)){
 							System.out.println("Cycled.");
 							if(metaIdx.size() >= preMetaIdx.size()){
 								break;
 							}else{
 								metaIdx = preMetaIdx;
 								break;
 							}
 						}
 						else{
 							prepreMetaIdx = preMetaIdx;
 							preMetaIdx = metaIdx;
 							//System.out.println("Gene Set Size: " + metaIdx.size());
 							cnt++;
 						}
 						
 					}
 					if(cnt == maxIter){
 						System.out.println("Not converged.");
 					}
 					// first token: attractee index
 					pw.print(geneIdx.get(i).idx());
 					pw.print("\t" + 1);
 					if(metaIdx.size() > 1){
 						for(ValIdx vi: metaIdx){
 								pw.print("\t" + vi.idx + "," + vi.val);
 						}
 					}else{
 						pw.print("\tNA");
 					}
 					pw.println();
 					
 				}
 				tt++;
 			}
 		}
 		pw.close();
 	}
 	public ArrayList<ValIdx> findCNV(float[][] data, float[] v, Chromosome chr) throws Exception{
 		int n = data[0].length;
 		int m = data.length;
 		
 		ITComputer itc = new ITComputer(bins, splineOrder, id, totalComputers, miNorm);
 		//itc.negateMI(true);
 		prepare("geneset");
 		PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/geneset/caf." + String.format("%05d", id)+".txt"));
 		int tt = 0;
 		System.out.println("At chromosome " + chr.name() + "...");
 		ArrayList<ValIdx> geneIdx = chr.geneIdx();
 			/*// sort gene idx ascendantly
 			Collections.sort(geneIdx);
 			Collections.reverse(geneIdx);
 			*/
 			/*
 			 * Step 1: find the genes that are significantly associated with the seed gene 
 			 *         as the initial metagene
 			 */
 			float[] mi = itc.getAllMIWith(v, data);
 			ArrayList<ValIdx> metaIdx = new ArrayList<ValIdx>();
 					
 			float[] z = StatOps.xToZ(mi, m);
 			ValIdx[] vec = new ValIdx[m];
 			for(int j = 0; j < m; j++){
 				vec[j] = new ValIdx(j, z[j]);
 			}
 			Arrays.sort(vec);
 			if(convergeMethod.equals("FIXEDSIZE")){
 				for(int j = 0; j < m; j++){
 					if(metaIdx.size() < attractorSize){
 						if(geneIdx.contains(vec[j])){
 							metaIdx.add(vec[j]);
 						}
 					}else{
 						break;
 					}
 				}
 			}else if(convergeMethod.equals("ZSCORE")){
 				for(int j = 0; j < m; j++){
 					if(vec[j].val > zThreshold){
 						if(geneIdx.contains(vec[j])){
 							metaIdx.add(vec[j]);
 						}
 					}else if(metaIdx.size() < attractorSize){
 						if(geneIdx.contains(vec[j])){
 							metaIdx.add(vec[j]);
 						}
 					}else{
 						break;
 					}
 				}
 			}	
 			int cnt = 0;
 			ArrayList<ValIdx> prepreMetaIdx = new ArrayList<ValIdx>();
 			ArrayList<ValIdx> preMetaIdx = new ArrayList<ValIdx>();
 			preMetaIdx.addAll(metaIdx);
 			//System.out.println("Initial gene set size " + metaIdx.size() );
 					
 			/*
 			 * Step 2: Calculate metagene, find the genes that have correlation exceeding the 
 			 *         threshold as the new metagene
 			 */
 				
 			while(cnt < maxIter){
 						
 				// cannot find significant associated genes, exit.
 						
 				if(metaIdx.size() == 0){
 					//System.out.println("Empty set, exit.");
 					break;
 				}
 				//System.out.print("Iteration " + cnt + "...");
 				float[] metaGene = getMetaGene(data,metaIdx, n);
 				mi = itc.getAllMIWith(metaGene, data);
 				metaIdx = new ArrayList<ValIdx>();
 				vec = new ValIdx[m];
 				z = StatOps.xToZ(mi, m);
 				for(int j = 0; j < m; j++){
 					vec[j] = new ValIdx(j, z[j]);
 				}
 				Arrays.sort(vec);
 				metaIdx = new ArrayList<ValIdx>();
 				if(convergeMethod.equals("FIXEDSIZE")){
 					for(int j = 0; j < m; j++){
 						if(metaIdx.size() < attractorSize){
 							if(geneIdx.contains(vec[j])){
 								metaIdx.add(vec[j]);
 							}
 						}else{
 							break;
 						}
 					}
 				}else if(convergeMethod.equals("ZSCORE")){
 				
 					for(int j = 0; j < m; j++){
 						if(vec[j].val > zThreshold){
 							if(geneIdx.contains(vec[j])){
 								metaIdx.add(vec[j]);
 							}
 						}else if(metaIdx.size() < attractorSize){
 							if(geneIdx.contains(vec[j])){
 								metaIdx.add(vec[j]);
 							}
 						}else{
 							break;
 						}
 					}
 				
 				}
 				if(preMetaIdx.equals(metaIdx)){
 					System.out.print("Converged. "); 
 					System.out.println("Gene Set Size: " + metaIdx.size());
 					break;
 				}else if (prepreMetaIdx.equals(metaIdx)){
 					System.out.println("Cycled.");
 					if(metaIdx.size() >= preMetaIdx.size()){
 						break;
 					}else{
 						metaIdx = preMetaIdx;
 						break;
 					}
 				}
 				else{
 					prepreMetaIdx = preMetaIdx;
 					preMetaIdx = metaIdx;
 					//System.out.println("Gene Set Size: " + metaIdx.size());
 					cnt++;
 				}
 				
 			}
 			if(cnt == maxIter){
 				System.out.println("Not converged.");
 			}
 			return metaIdx;
 	}
 	
 	public float[] findWeightedCNV(DataFile ma, String gene, Genome gn, float[] vec, float power, boolean excludeTop, boolean miDecay) throws Exception{
 		float[][] data = ma.getData();
 		int m = data.length;
 		int n = data[0].length;
 		ArrayList<String> genes = ma.getProbes();
 		
 		float[] wVec = itc.getAllMIWith(vec, data);
 		//float[] wVec = StatOps.pearsonCorr(vec, data, m, n);
 		//float[] wVec = StatOps.cov(vec, data, m, n);
 		
 		/*PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + gene + "_CNV.wt.txt"));
 		pw.print("Gene");
 		for(String g : genes){
 			pw.print("\t" + g);
 		}pw.println();
 		pw.print("-1");
 		for(int i = 0; i < m; i++){
 			pw.print("\t" + wVec[i]);
 		}pw.println();*/
 		
 		if(excludeTop){
 			int maxIdx = -1;
 			float maxw = -1;
 			float nextMaxW = -1;
 			for(int i = 0; i < m; i++){
 				if(wVec[i] > maxw){
 					maxIdx = i;
 					nextMaxW = maxw;
 					maxw = wVec[i];
 				}
 			}
 			wVec[maxIdx] = 0;
 		}
 		
 		float center = gn.getCoord(gene);
 		System.out.println(center);
 		float range = gn.getChrCoordRange(gn.getChr(gene));
 		
 		if(miDecay){
 			
 			for(int i = 0; i < m; i++){
 				float f = Math.abs(gn.getCoord(genes.get(i))-center) / (float)range;
 				//System.out.print(f + "\t" + wVec[i]);
 				wVec[i] *=(float) Math.exp(Math.log( 1 - f ) ); 
 				//System.out.println("\t" + wVec[i]);
 			}
 		
 		}
 		float[] preWVec = new float[m];
 		System.arraycopy(wVec, 0, preWVec, 0, m);
 		int c = 0;
 		float convergeTh = precision * precision / m;
 		System.out.println("m : " + m);
 		System.out.println("Convergence threshold : " + convergeTh);
 		while(c < maxIter){
 			float[] metaGene = getWeightedMetaGene(data, wVec, power,  m, n);
 			wVec = itc.getAllMIWith(metaGene, data);
 			int maxIdx = -1;
 			float maxWVec = -1;
 			for(int i = 0; i < m; i++){
 				if(wVec[i] > maxWVec){
 					maxIdx = i;
 					maxWVec = wVec[i];
 				}
 			}
 			center = gn.getCoord(genes.get(maxIdx));
 			//System.out.println(center);
 			if(miDecay){
 				for(int i = 0; i < m; i++){
 					float f = Math.abs(gn.getCoord(genes.get(i))-center) / (float)range;
 					wVec[i] *= (float) Math.exp(Math.log( 1 - f ) ); 
 				}
 			}
 			//System.out.println(wVec[idx]);
 			//wVec = StatOps.pearsonCorr(metaGene, data, m, n);
 			//wVec = StatOps.cov(metaGene, data, m, n);
 			
 			/*pw.print(c);
 			for(int i = 0; i < m; i++){
 				pw.print("\t" + wVec[i]);
 			}pw.println();*/
 			
 			float err = calcMSE(wVec, preWVec, m);
 			System.out.println(err);
 			if(err < convergeTh){
 				//pw.close();
 				System.out.println("Converged.");
 				return wVec;
 			}
 			System.arraycopy(wVec, 0, preWVec, 0, m);
 			c++;
 		}
 		System.out.println("Not converged.");
 		//pw.close();
 		wVec[0] = -1;
 		return wVec;
 	}
 	public void findWeightedCNV(DataFile ma, Genome gn, int winSize, float power, boolean miDecay) throws Exception{
 		
 		gn.linkToDataFile(ma);
 		ma = ma.getSubProbes(gn.getAllGenes());
 		
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		ArrayList<String> genes = ma.getProbes();
 		
 		int start = id * m / totalComputers;
 		int end = (id+1) * m / totalComputers;
 		
 		System.out.println("Processing gene " + (start+1) + " to " + end);
 		
 		ArrayList<float[]> wVecs = new ArrayList<float[]>();
 		ArrayList<ArrayList<Integer>> basins = new ArrayList<ArrayList<Integer>>();
 		ArrayList<String> chrs = new ArrayList<String>();
 		
 		
 		
 		for(int idx = start; idx < end; idx++){
 			String g = genes.get(idx);
 			String chr = gn.getChr(g);
 			String[] neighbors = gn.getNeighbors(g, winSize);
 			DataFile ma2 = ma.getSubProbes(neighbors);
 			ArrayList<String> genes2 = ma2.getProbes();
 			int m2 = ma2.getNumRows();
 			float[][] data = ma2.getData();
 			int idx2 = ma2.getRows().get(g);
 			float[] vec = data[idx2];
 			
 			float convergeTh = precision * precision /m2;
 			
 			System.out.print("Processing " + g + "..." + chr + "\t" + m2 + "\t" + convergeTh + "\t");
 						
 			float[] wVec = itc.getAllMIWith(vec, data);
 			//System.out.println(chr + "\t" + wVec.length);
 			//float[] wVec = StatOps.pearsonCorr(vec, data, m, n);
 			//float[] wVec = StatOps.cov(vec, data, m, n);
 			
 			/*if(excludeTop){
 				int maxIdx = -1;
 				float maxw = -1;
 				float nextMaxW = -1;
 				for(int i = 0; i < m; i++){
 					if(wVec[i] > maxw){
 						maxIdx = i;
 						nextMaxW = maxw;
 						maxw = wVec[i];
 					}
 				}
 				wVec[maxIdx] = 0;
 			}*/
 			
 			float center = gn.getCoord(g);
 			//System.out.println(center);
 			float range = gn.getChrCoordRange(chr);
 			if(miDecay){
 				for(int i = 0; i < m2; i++){
 					float f = Math.abs(gn.getCoord(genes2.get(i))-center) / (float)range;
 					wVec[i] *=(float) Math.exp(Math.log( 1 - f ) ); 
 					
 				}
 			}
 			
 			float[] preWVec = new float[m2];
 			System.arraycopy(wVec, 0, preWVec, 0, m2);
 			int c = 0;
 			
 			boolean converge = false;
 			
 			while(c < maxIter){
 				float[] metaGene = getWeightedMetaGene(data, wVec, power,  m2, n);
 				wVec = itc.getAllMIWith(metaGene, data);
 				//System.out.println(wVec[idx]);
 				//wVec = StatOps.pearsonCorr(metaGene, data, m, n);
 				//wVec = StatOps.cov(metaGene, data, m, n);
 				int maxIdx = -1;
 				float maxWVec = -1;
 				for(int i = 0; i < m2; i++){
 					if(wVec[i] > maxWVec){
 						maxIdx = i;
 						maxWVec = wVec[i];
 					}
 				}
 				center = gn.getCoord(genes2.get(maxIdx));
 				//System.out.println(center);
 				if(miDecay){
 					for(int i = 0; i < m2; i++){
 						float f = Math.abs(gn.getCoord(genes2.get(i))-center) / (float)range;
 						wVec[i] *= (float) Math.exp(Math.log( 1 - f ) ); 
 					}
 				}
 				
 				float err = calcMSE(wVec, preWVec, m2);
 				//System.out.println(err);
 				if(err < convergeTh){
 					//pw.close();
 					System.out.println("Converged.");
 					converge = true;
 					break;
 				}
 				System.arraycopy(wVec, 0, preWVec, 0, m2);
 				c++;
 			}
 			if(converge){
 				boolean newOne = true;
 				for(int i = 0; i < wVecs.size(); i++){
 					if(chr.equals(chrs.get(i))){
 						float[] fs = wVecs.get(i);
 						float err = calcMSE(wVec, fs, m2);
 						if(err < precision/m2){ 
 							newOne = false;
 							basins.get(i).add(idx);
 							break;
 						}
 					}
 				}
 				if(newOne){
 					wVecs.add(wVec);
 					ArrayList<Integer> basin = new ArrayList<Integer>();
 					basin.add(idx);
 					basins.add(basin);
 					chrs.add(chr);
 				}
 			}
 			
 			
 		}
 		prepare("geneset");
 		PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/geneset/caf." + String.format("%05d", id)+".txt"));
 		for(int i = 0; i < wVecs.size(); i++){
 			pw.print(chrs.get(i));
 			ArrayList<Integer> basin = basins.get(i);
 			int k = basin.size();
 			for(int j = 0; j < k; j++){
 				if(j == 0){
 					pw.print("\t" + basin.get(j));
 				}else{
 					pw.print("," + basin.get(j));
 				}
 			}
 			float[] fs = wVecs.get(i);
 			for(float f : fs){
 				pw.print("\t" + f);
 			}
 			pw.println();
 		}
 		pw.close();
 	}
 	public void findWeightedCNVCoef(DataFile ma, Genome gn, int winSize, float power, boolean miDecay) throws Exception{
 		gn.linkToDataFile(ma);
 		ma = ma.getSubProbes(gn.getAllGenes());
 		
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		ArrayList<String> genes = ma.getProbes();
 		
 		int buf = (winSize - 1)/2;
 		
 		int start = id * (m - 2*buf) / totalComputers;
 		int end = (id+1) * (m - 2*buf) / totalComputers;
 		
 		System.out.println("Processing gene " + (start+1) + " to " + end);
 		new File("output").mkdir();
 		new File("output/" + jobID).mkdir();
 		PrintWriter pw = new PrintWriter("output/" + jobID + "/basinScores." + String.format("%05d", id)+ ".txt");
 		
 		for(int idx = start; idx < end; idx++){
 			int ii = idx + buf;
 			String g = genes.get(ii);
 			String chr = gn.getChr(g);
 			String[] neighbors = gn.getNeighbors(g, winSize);
 			DataFile ma2 = ma.getSubProbes(neighbors);
 			ArrayList<String> genes2 = ma2.getProbes();
 			int m2 = ma2.getNumRows();
 			float[][] data = ma2.getData();
 			int idx2 = ma2.getRows().get(g);
 			float[] vec = data[idx2];
 			
 			float convergeTh = precision * precision /m2;
 			
 			System.out.print("Processing " + g + "..." + chr + "\t" + m2 + "\t" + convergeTh + "\t");
 						
 			float[] wVec = itc.getAllMIWith(vec, data);
 			//System.out.println(chr + "\t" + wVec.length);
 			//float[] wVec = StatOps.pearsonCorr(vec, data, m, n);
 			//float[] wVec = StatOps.cov(vec, data, m, n);
 			
 			/*if(excludeTop){
 				int maxIdx = -1;
 				float maxw = -1;
 				float nextMaxW = -1;
 				for(int i = 0; i < m; i++){
 					if(wVec[i] > maxw){
 						maxIdx = i;
 						nextMaxW = maxw;
 						maxw = wVec[i];
 					}
 				}
 				wVec[maxIdx] = 0;
 			}*/
 			
 			float center = gn.getCoord(g);
 			//System.out.println(center);
 			float range = gn.getChrCoordRange(chr);
 			if(miDecay){
 				for(int i = 0; i < m2; i++){
 					float f = Math.abs(gn.getCoord(genes2.get(i))-center) / (float)range;
 					wVec[i] *=(float) Math.exp(Math.log( 1 - f ) ); 
 					
 				}
 			}
 			
 			float[] preWVec = new float[m2];
 			System.arraycopy(wVec, 0, preWVec, 0, m2);
 			int c = 0;
 			
 			boolean converge = false;
 			
 			while(c < maxIter){
 				float[] metaGene = getWeightedMetaGene(data, wVec, power,  m2, n);
 				wVec = itc.getAllMIWith(metaGene, data);
 				//System.out.println(wVec[idx]);
 				//wVec = StatOps.pearsonCorr(metaGene, data, m, n);
 				//wVec = StatOps.cov(metaGene, data, m, n);
 				int maxIdx = -1;
 				float maxWVec = -1;
 				for(int i = 0; i < m2; i++){
 					if(wVec[i] > maxWVec){
 						maxIdx = i;
 						maxWVec = wVec[i];
 					}
 				}
 				center = gn.getCoord(genes2.get(maxIdx));
 				//System.out.println(center);
 				if(miDecay){
 					for(int i = 0; i < m2; i++){
 						float f = Math.abs(gn.getCoord(genes2.get(i))-center) / (float)range;
 						wVec[i] *= (float) Math.exp(Math.log( 1 - f ) ); 
 					}
 				}
 				
 				float err = calcMSE(wVec, preWVec, m2);
 				//System.out.println(err);
 				if(err < convergeTh){
 					//pw.close();
 					System.out.println("Converged.");
 					converge = true;
 					break;
 				}
 				System.arraycopy(wVec, 0, preWVec, 0, m2);
 				c++;
 			}
 			
 			if(converge){
 				Arrays.sort(wVec);
 				int k = m2 >= 10? m2-10 : m2; 
 				pw.println(g + "\t" + wVec[k]);
 			}else{
 				pw.println(g + "\t" + "-1");
 			}
 			
 			
 		}
 		pw.close();
 	}
 	
 	
 	public float[] findWeightedAttractor(DataFile ma, String gene, float[] vec, float power, boolean excludeTop) throws Exception{
 		float[][] data = ma.getData();
 		int m = data.length;
 		int n = data[0].length;
 		//ArrayList<String> genes = ma.getProbes();
 		
 		float[] wVec = itc.getAllMIWith(vec, data);
 		//float[] wVec = StatOps.pearsonCorr(vec, data, m, n);
 		//float[] wVec = StatOps.cov(vec, data, m, n);
 		
 		/*PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + gene + "_CNV.wt.txt"));
 		pw.print("Gene"); fs[j]
 		for(String g : genes){
 			pw.print("\t" + g);
 		}pw.println();
 		pw.print("-1");
 		for(int i = 0; i < m; i++){
 			pw.print("\t" + wVec[i]);
 		}pw.println();*/
 		
 		if(excludeTop){
 			int maxIdx = -1;
 			float maxw = -1;
 			float nextMaxW = -1;
 			for(int i = 0; i < m; i++){
 				if(wVec[i] > maxw){
 					maxIdx = i;
 					nextMaxW = maxw;
 					maxw = wVec[i];
 				}
 			}
 			wVec[maxIdx] = 0;
 		}
 		
 		float[] preWVec = new float[m];
 		System.arraycopy(wVec, 0, preWVec, 0, m);
 		int c = 0;
 		float convergeTh = precision * precision / m;
 		while(c < maxIter){
 			float[] metaGene = getWeightedMetaGene(data, wVec, power,  m, n);
 			wVec = itc.getAllMIWith(metaGene, data);
 			
 			//System.out.println(wVec[idx]);
 			//wVec = StatOps.pearsonCorr(metaGene, data, m, n);
 			//wVec = StatOps.cov(metaGene, data, m, n);
 			
 			/*pw.print(c);
 			for(int i = 0; i < m; i++){
 				pw.print("\t" + wVec[i]);
 			}pw.println();*/
 			
 			float err = calcMSE(wVec, preWVec, m);
 			System.out.println(err);
 			if(err < convergeTh){
 				//pw.close();
 				System.out.println("Converged.");
 				return wVec;
 			}
 			System.arraycopy(wVec, 0, preWVec, 0, m);
 			c++;
 		}
 		System.out.println("Not converged.");
 		//pw.close();
 		wVec[0] = -1;
 		return wVec;
 	}
 	public void findWeightedAttractor(float[][] data, float power) throws Exception{
 		int m = data.length;
 		int n = data[0].length;
 		
 		int start = id * m / totalComputers;
 		int end = (id+1) * m / totalComputers;
 		
 		System.out.println("Processing gene " + (start+1) + " to " + end);
 		
 		ArrayList<float[]> wVecs = new ArrayList<float[]>();
 		ArrayList<ArrayList<Integer>> basins = new ArrayList<ArrayList<Integer>>();
 		
 		float convergeTh = precision * precision / m;
 		
 		for(int idx = start; idx < end; idx++){
 			System.out.print("Processing " + idx + "...");
 			float[] wVec = itc.getAllMIWith(data[idx], data);
 			//float[] wVec = StatOps.pearsonCorr(vec, data, m, n);
 			//float[] wVec = StatOps.cov(vec, data, m, n);
 			float[] preWVec = new float[m];
 			System.arraycopy(wVec, 0, preWVec, 0, m);
 			int c = 0;
 			boolean converge = false;
 			while(c < maxIter){
 				float[] metaGene = getWeightedMetaGene(data, wVec, power,  m, n);
 				wVec = itc.getAllMIWith(metaGene, data);
 				//wVec = StatOps.pearsonCorr(metaGene, data, m, n);
 				//wVec = StatOps.cov(metaGene, data, m, n);
 				//System.out.println(err);
 				
 				float err = calcMSE(wVec, preWVec, m);
 				//System.out.println(err);
 				if(err < convergeTh){
 					//pw.close();
 					System.out.println("Converged.");
 					converge=true;
 					break;
 				}
 				System.arraycopy(wVec, 0, preWVec, 0, m);
 				c++;
 			}
 			if(converge){
 				boolean newOne = true;
 				for(int i = 0; i < wVecs.size(); i++){
 					float[] fs = wVecs.get(i);
 					float err = calcMSE(wVec, fs, m);
 					if(err < precision / m){ 
 						newOne = false;
 						basins.get(i).add(idx);
 						break;
 					}
 				}
 				if(newOne){
 					wVecs.add(wVec);
 					ArrayList<Integer> basin = new ArrayList<Integer>();
 					basin.add(idx);
 					basins.add(basin);
 				}
 			}
 			
 		}
 		
 		prepare("geneset");
 		PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/geneset/caf." + String.format("%05d", id)+".txt"));
 		for(int i = 0; i < wVecs.size(); i++){
 			pw.print("caf"); // add tag (for format consistency with CNV)
 			ArrayList<Integer> basin = basins.get(i);
 			int k = basin.size();
 			for(int j = 0; j < k; j++){
 				if(j == 0){
 					pw.print(basin.get(j));
 				}else{
 					pw.print("," + basin.get(j));
 				}
 			}
 			float[] fs = wVecs.get(i);
 			for(int j = 0; j < m; j++){
 				pw.print("\t" + fs[j]);
 			}
 			pw.println();
 		}
 		pw.close();
 		
 		
 		
 	}
 	public ArrayList<ValIdx> findAttractor(float[][] data, float[] vec)throws Exception{
 		int m = data.length;
 		int n = data[0].length;
 		ITComputer itc = new ITComputer(bins, splineOrder, id, totalComputers, miNorm);
 			/*
 			 * Step 1: find the genes that are significantly associated with the seed gene 
 			 *         as the initial metagene
 			 */
 			
 			float[] mi = itc.getAllMIWith(vec, data);
 			ArrayList<ValIdx> metaIdx = new ArrayList<ValIdx>();
 			ValIdx[] vecMI = new ValIdx[m];
 			for(int i = 0; i < m; i++){
 				vecMI[i] = new ValIdx(i, mi[i]);
 			}
 			ValIdx[] vecZ = new ValIdx[m];
 			
 			if(convergeMethod.equals("FIXEDSIZE")){
 				Arrays.sort(vecMI);
 				for(int i = 0; i < attractorSize; i++){
 					metaIdx.add(vecMI[i]);
 				}
 			}else if(convergeMethod.equals("ZSCORE")){
 				float[] z = StatOps.xToZ(mi, m);
 				for(int i = 0; i < m; i++){
 					vecZ[i] = new ValIdx(i, z[i]);
 				}
 				Arrays.sort(vecZ);
 				/*for(int i = 0; i < attractorSize; i++){
 					metaIdx.add(vecZ[i]);
 				}*/
 				for(int i = 0; i < m; i++){
 					if(vecZ[i].val() > zThreshold){
 						metaIdx.add(vecZ[i]);
 					}else{
 						break;
 					}
 				}
 			}
 			int cnt = 0;
 			ArrayList<ValIdx> prepreMetaIdx = new ArrayList<ValIdx>();
 			ArrayList<ValIdx> preMetaIdx = new ArrayList<ValIdx>();
 			preMetaIdx.addAll(metaIdx);
 			//System.out.println("Initial gene set size " + metaIdx.size() );
 			
 			/*
 			 * Step 2: Calculate metagene, find the genes that have correlation exceeding the 
 			 *         threshold as the new metagene
 			 */
 			
 			while(cnt < maxIter){
 				// cannot find significant associated genes, exit.
 				if(metaIdx.size() == 0){
 					//System.out.println("Empty set, exit.");
 					break;
 				}
 				//System.out.print("Iteration " + cnt + "...");
 				float[] metaGene = getMetaGene(data,metaIdx, n);
 				if(rankBased){
 					metaGene = StatOps.rank(metaGene);
 				}
 				mi = itc.getAllMIWith(metaGene, data);
 				vecMI = new ValIdx[m];
 				for(int i = 0; i < m; i++){
 					vecMI[i] = new ValIdx(i, mi[i]);
 				}
 				metaIdx = new ArrayList<ValIdx>();
 				if(convergeMethod.equals("FIXEDSIZE")){
 					Arrays.sort(vecMI);
 					for(int i = 0; i < attractorSize; i++){
 						metaIdx.add(vecMI[i]);
 					}
 				}else if(convergeMethod.equals("ZSCORE")){
 					vecZ = new ValIdx[m];
 					float[] z = StatOps.xToZ(mi, m);
 					for(int i = 0; i < m; i++){
 						vecZ[i] = new ValIdx(i, z[i]);
 					}
 					Arrays.sort(vecZ);
 					/*for(int i = 0; i < attractorSize; i++){
 						metaIdx.add(vecZ[i]);
 					}*/
 					for(int i = 0; i < m; i++){
 						if(vecZ[i].val() > zThreshold){
 							metaIdx.add(vecZ[i]);
 						}else{
 							break;
 						}
 					}
 				}
 				if(preMetaIdx.equals(metaIdx)){
 					System.out.println("Converged."); 
 					System.out.println("Gene Set Size: " + metaIdx.size());
 					break;
 				}else if (prepreMetaIdx.equals(metaIdx)){
 					System.out.println("Cycled.");
 					metaIdx.clear();
 					break;
 				}
 				else{
 					prepreMetaIdx = preMetaIdx;
 					preMetaIdx = metaIdx;
 					//System.out.println("Gene Set Size: " + metaIdx.size());
 					cnt++;
 				}
 				
 			}
 			if(cnt == maxIter){
 				System.out.println("Not converged.");
 				metaIdx.clear();
 			}
 			return metaIdx;
 	}
 	public void findAttractor(float[][] val, float[][] data) throws Exception{
 		int m = val.length;
 		int n = val[0].length;
 		
 		int start = id * m / totalComputers;
 		int end = (id+1) * m / totalComputers;
 		
 		System.out.println("Processing gene " + (start+1) + " to " + end);
 		
 		ITComputer itc = new ITComputer(bins, splineOrder, id, totalComputers, miNorm);
 		//itc.negateMI(true);
 		prepare("geneset");
 		PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/geneset/caf." + String.format("%05d", id)+".txt"));
 		for(int idx = start; idx < end; idx++){
 			System.out.print("Processing " + idx + "...");
 			/*
 			 * Step 1: find the genes that are significantly associated with the seed gene 
 			 *         as the initial metagene
 			 */
 			
 			float[] mi = itc.getAllMIWith(val[idx], val);
 			ArrayList<ValIdx> metaIdx = new ArrayList<ValIdx>();
 			ValIdx[] vecMI = new ValIdx[m];
 			for(int i = 0; i < m; i++){
 				vecMI[i] = new ValIdx(i, mi[i]);
 			}
 			ValIdx[] vecZ = new ValIdx[m];
 			
 			if(convergeMethod.equals("FIXEDSIZE")){
 				Arrays.sort(vecMI);
 				for(int i = 0; i < attractorSize; i++){
 					metaIdx.add(vecMI[i]);
 				}
 			}else if(convergeMethod.equals("ZSCORE")){
 				float[] z = StatOps.xToZ(mi, m);
 				for(int i = 0; i < m; i++){
 					vecZ[i] = new ValIdx(i, z[i]);
 				}
 				Arrays.sort(vecZ);
 				for(int i = 0; i < attractorSize; i++){
 					metaIdx.add(vecZ[i]);
 				}
 				for(int i = attractorSize; i < m; i++){
 					if(vecZ[i].val() > zThreshold){
 						metaIdx.add(vecZ[i]);
 					}else{
 						break;
 					}
 				}
 			}
 			int cnt = 0;
 			ArrayList<ValIdx> prepreMetaIdx = new ArrayList<ValIdx>();
 			ArrayList<ValIdx> preMetaIdx = new ArrayList<ValIdx>();
 			preMetaIdx.addAll(metaIdx);
 			//System.out.println("Initial gene set size " + metaIdx.size() );
 			
 			/*
 			 * Step 2: Calculate metagene, find the genes that have correlation exceeding the 
 			 *         threshold as the new metagene
 			 */
 			
 			while(cnt < maxIter){
 				// cannot find significant associated genes, exit.
 				if(metaIdx.size() == 0){
 					//System.out.println("Empty set, exit.");
 					break;
 				}
 				//System.out.print("Iteration " + cnt + "...");
 				float[] metaGene = getMetaGene(data,metaIdx, n);
 				if(rankBased){
 					metaGene = StatOps.rank(metaGene);
 				}
 				mi = itc.getAllMIWith(metaGene, val);
 				vecMI = new ValIdx[m];
 				for(int i = 0; i < m; i++){
 					vecMI[i] = new ValIdx(i, mi[i]);
 				}
 				metaIdx = new ArrayList<ValIdx>();
 				if(convergeMethod.equals("FIXEDSIZE")){
 					Arrays.sort(vecMI);
 					for(int i = 0; i < attractorSize; i++){
 						metaIdx.add(vecMI[i]);
 					}
 				}else if(convergeMethod.equals("ZSCORE")){
 					vecZ = new ValIdx[m];
 					float[] z = StatOps.xToZ(mi, m);
 					for(int i = 0; i < m; i++){
 						vecZ[i] = new ValIdx(i, z[i]);
 					}
 					Arrays.sort(vecZ);
 					for(int i = 0; i < attractorSize; i++){
 						metaIdx.add(vecZ[i]);
 					}
 					for(int i = attractorSize; i < m; i++){
 						if(vecZ[i].val() > zThreshold){
 							metaIdx.add(vecZ[i]);
 						}else{
 							break;
 						}
 					}
 				}
 				if(preMetaIdx.equals(metaIdx)){
 					System.out.print("Converged."); 
 					System.out.println("Gene Set Size: " + metaIdx.size());
 					break;
 				}else if (prepreMetaIdx.equals(metaIdx)){
 					System.out.println("Cycled.");
 					if(metaIdx.size() >= preMetaIdx.size()){
 						break;
 					}else{
 						metaIdx = preMetaIdx;
 						break;
 					}
 				}
 				else{
 					prepreMetaIdx = preMetaIdx;
 					preMetaIdx = metaIdx;
 					//System.out.println("Gene Set Size: " + metaIdx.size());
 					cnt++;
 				}
 				
 			}
 			if(cnt == maxIter){
 				System.out.println("Not converged.");
 			}
 			// first token: attractee index
 			pw.print(idx);
 			pw.print("\t" + 1);
 			if(metaIdx.size() > 1){
 				if(convergeMethod.equals("ZSCORE")){
 					for(ValIdx vi: metaIdx){
 						pw.print("\t" + vi.idx + "," + vecMI[vi.idx].val + "," + vi.val);
 					}
 				}else{
 					for(ValIdx vi: metaIdx){
 						pw.print("\t" + vi.idx + "," + vi.val + ",NaN");
 					}
 				}
 			}else{
 				pw.print("\tNA");
 			}
 			pw.println();
 		}
 		pw.close();
 	}	
 	public void setZThreshold(float z) throws MathException{
 		Converger.zThreshold = z;
 	}
 	public void setZThreshold(int m) throws MathException{
 		NormalDistributionImpl norm = new NormalDistributionImpl();
 		double pth = 0.05/m;
 		Converger.zThreshold = (float) -norm.inverseCumulativeProbability(pth);
 	}
 	public void setAttractorSize(int sz){
 		Converger.attractorSize = sz;
 	}
 	public float getZThreshold(){
 		return zThreshold;
 	}
 	public void setConvergeMethos(String mthd){
 		Converger.convergeMethod = mthd;
 	}
 	public void setMIParameter(int bins, int so){
 		Converger.bins = bins;
 		Converger.splineOrder = so;
 	}
 	public void setPrecision(float precision){
 		Converger.precision = precision;
 	}
 	public void miNormalization(boolean miNorm){
 		Converger.miNorm = miNorm;
 	}
 	public void linkITComputer(ITComputer itc){
 		Converger.itc = itc;
 	}
 	
 	
 	
 }
