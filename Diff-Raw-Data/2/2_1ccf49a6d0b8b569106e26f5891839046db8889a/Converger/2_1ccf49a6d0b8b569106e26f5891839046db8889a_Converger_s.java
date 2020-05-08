 package worker;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import obj.Annotations;
 import obj.Chromosome;
 import obj.DataFile;
 import obj.Genome;
 import obj.InverseAnnotations;
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
 	public Converger(int id, int totalComputers, long jobID, int maxIter, boolean rankBased){
 		super(id, totalComputers, jobID);
 		Converger.maxIter = maxIter;
 		Converger.rankBased = rankBased;
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
 			if(w[i] > 0){
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
 	/*
 	 * Apr 6 2012
 	 * Probe-level find CNV
 	 * 
 	 */
 	public ValIdx[] findWeightedCNV(DataFile ma, float[] vec, Genome gn,
 			String[] neighborsG, InverseAnnotations invannot, int winSize,
 			float power, Annotations annot) throws Exception {
 		
 		float[][] data = ma.getData();
 		int m = data.length;
 		int n = data[0].length;
 		int mg = neighborsG.length;
 		HashMap<String, Integer> probeMap = ma.getRows(); 
 		
 		float[] mivec = itc.getAllMIWith(vec, data);
 		float[] premiVec = new float[m];
 		System.arraycopy(mivec, 0, premiVec, 0, m);
 		int c = 0;
 		float convergeTh = precision * precision / m;
 		ValIdx[] wVec = fillInWVec(mivec, neighborsG, probeMap, invannot, mg);
 		while(c < maxIter){
 			float[] metaGene = getWeightedMetageneFromPbs(data, wVec, power,  m, n, mg);
 			mivec = itc.getAllMIWith(metaGene, data);
 			float err = calcMSE(mivec, premiVec, m);
 			//System.out.println(err);
 			if(err < convergeTh){
 				//pw.close();
 				//System.out.println("Converged.");
 				return wVec;
 			}
 			wVec = fillInWVec(mivec, neighborsG, probeMap, invannot, mg);
 			System.arraycopy(mivec, 0, premiVec, 0, m);
 			c++;
 		}
 		//System.out.println("Not converged.");
 		//pw.close();
 		wVec[0] = null;
 		return wVec;
 		
 	}
 	
 	public ValIdx[] findWeightedAttractorOptimizePower(DataFile ma, int idx, float pstart, float pend, float delp, int quantile) throws Exception{
 		float[][] data = ma.getData();
 		float[] vec = data[idx];
 		int m = data.length;
 		int n = data[0].length;
 		
 		ValIdx[] bestWVec = null;
 		float bestScore = -1;
 		float bestPow = -1;
 		
 		for(float power = pstart; power <= pend; power+=delp)
 		{
 		
 		
 		float[] wVec = itc.getAllMIWith(vec, data);
 		
 		float[] preWVec = new float[m];
 		System.arraycopy(wVec, 0, preWVec, 0, m);
 		int c = 0;
 		float convergeTh = 5E-13f;//precision * precision / m;
 		boolean converge = false;
 		
 		while(c < maxIter){
 			float[] metaGene = getWeightedMetaGene(data, wVec, power,  m, n);
 			wVec = itc.getAllMIWith(metaGene, data);
 			float err = calcMSE(wVec, preWVec, m);
 			//System.out.println(err);
 			if(err < convergeTh){
 				converge = true;
 				break;
 			}
 			System.arraycopy(wVec, 0, preWVec, 0, m);
 			c++;
 		}
 		if(converge){
 			ValIdx[] vis = new ValIdx[m];
 			for(int i = 0; i < m; i++){
 				vis[i] = new ValIdx(i, wVec[i]);
 			}
 			Arrays.sort(vis);
 			
 			float score = vis[quantile-1].val;
 			System.out.println(ma.getProbes().get(vis[0].idx) + "\t" + power + "\t" + score);
 			if(vis[0].idx != idx){
 				System.out.println("Not seed.");
 				continue;
 			}
 			if(score > bestScore){
 				bestScore = score;
 				bestPow = power;
 				bestWVec = new ValIdx[m];
 				System.arraycopy(vis, 0, bestWVec, 0, m);
 			}
 			
 		}
 		
 		} // END power iteration
 		return bestWVec;
 	}
 	public void findWeightedCNV(DataFile ma, Genome gn, float pstart, float pend, float delp, int quantile) throws Exception{
 		
 		ma = ma.getSubProbes(gn.getAllGenes());
 		
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		ArrayList<String> genes = ma.getProbes();
 		
 		int start = id * m / totalComputers;
 		int end = (id+1) * m / totalComputers;
 		
 		System.out.println("Processing gene " + (start+1) + " to " + end);
 		
 		prepare("geneset");
 		PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/geneset/caf." + String.format("%05d", id)+".txt"));
 		
 		for(int idx = start; idx < end; idx++){
 			String g = genes.get(idx);
 			String chrarm = gn.getChrArm(g);
 			String[] neighbors = gn.getAllGenesInChrArm(chrarm);
 			DataFile ma2 = ma.getSubProbes(neighbors);
 			ArrayList<String> genes2 = ma2.getProbes();
 			int m2 = ma2.getNumRows();
 			float[][] data = ma2.getData();
 			int idx2 = ma2.getRows().get(g);
 			float[] vec = data[idx2];
 			
 			ValIdx[] bestWVec = null;
 			float bestScore = -1;
 			float bestPow = -1;
 			
 			for(float power = pstart; power <= pend; power+=delp)
 			{
 			
 			float convergeTh = precision * precision /m2;
 			System.out.print("Processing " + g + "..." + chrarm + "\t" + m2 + "\t" + convergeTh + "\t");
 						
 			float[] wVec = itc.getAllMIWith(vec, data);
 			float[] preWVec = new float[m2];
 			System.arraycopy(wVec, 0, preWVec, 0, m2);
 			int c = 0;
 			
 			boolean converge = false;
 			
 			while(c < maxIter){
 				float[] metaGene = getWeightedMetaGene(data, wVec, power,  m2, n);
 				wVec = itc.getAllMIWith(metaGene, data);
 				
 				float err = calcMSE(wVec, preWVec, m2);
 				//System.out.println(err);
 				if(err < convergeTh){
 					converge = true;
 					break;
 				}
 				System.arraycopy(wVec, 0, preWVec, 0, m2);
 				c++;
 			}
 			if(converge){
 				ValIdx[] vis = new ValIdx[m2];
 				for(int i = 0; i < m2; i++){
 					vis[i] = new ValIdx(i, wVec[i]);
 				}
 				Arrays.sort(vis);
 				if(vis[0].idx != idx2){
 					continue;
 				}
 				if(vis[quantile-1].val > bestScore){
 					bestScore = vis[quantile-1].val;
 					bestWVec = new ValIdx[m2];
 					System.arraycopy(vis, 0, bestWVec, 0, m2);
 				}
 			}
 			
 			}// END power iteration
 			if(bestWVec != null){
 				pw.print(g + "\t" + chrarm);
 				for(int i = 0; i < m2; i++){
 					pw.print("\t" + genes2.get(bestWVec[i].idx) + ":" + bestWVec[i].val);
 				}pw.println();
 			}
 		} // END idx iteration
 		pw.close();
 	}
 	public void findWeightedCNVCoef(DataFile ma, Genome gn, int wstart, int wend, int delw, float pstart, float pend, float delp, int quantile) throws Exception{
 		ma = ma.getSubProbes(gn.getAllGenes());
 		
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		ArrayList<String> genes = ma.getProbes();
 		HashMap<String, Integer> maMap = ma.getRows();
 		
 		int start = id * m / totalComputers;
 		int end = (id+1) * m / totalComputers;
 		
 		System.out.println("Processing gene " + (start+1) + " to " + end);
 		new File("output").mkdir();
 		new File("output/" + jobID).mkdir();
 		PrintWriter pw = new PrintWriter("output/" + jobID + "/basinScores." + String.format("%05d", id)+ ".txt");
 		
 		for(int idx = start; idx < end; idx++)
 		{
 			float bestScore = -1;
 			int bestWinSize = -1;
 			float bestExp = -1;
 			ValIdx[] bestVec = null;
 			String g = genes.get(idx);
 			System.out.print("Processing " + g + "..."); 
 			
 			for(int winSize = wstart; winSize <= wend; winSize += delw)
 			{
 				
 				String[] neighbors = gn.getNeighbors(g, winSize);
 				if(neighbors == null){
 					System.out.println("No neighbors :(");
 					break;
 				}
 				
 				DataFile ma2 = ma.getSubProbes(neighbors);
 				ArrayList<String> ma2Genes = ma2.getProbes();
 				int m2 = ma2.getNumRows();
 				if(m2 < quantile){
 					continue;
 				}
 				float[][] data = ma2.getData();
 				int idx2 = ma2.getRows().get(g);
 				float[] vec = data[idx2];
 				float convergeTh = precision * precision /m2;
 				
 				for(float power = pstart; power <= pend; power += delp)
 				{
 					
 					float[] wVec = itc.getAllMIWith(vec, data);
 					float[] preWVec = new float[m2];
 					System.arraycopy(wVec, 0, preWVec, 0, m2);
 					int c = 0;
 					boolean converge = false;
 					float score = -1;
 					
 					while(c < maxIter){
 						float[] metaGene = getWeightedMetaGene(data, wVec, power,  m2, n);
 						wVec = itc.getAllMIWith(metaGene, data);
 						
 						float err = calcMSE(wVec, preWVec, m2);
 						System.arraycopy(wVec, 0, preWVec, 0, m2);
 						
 						if(err < convergeTh){
 							Arrays.sort(preWVec);
 							score = preWVec[m2 - quantile];
 							converge = true;
 							break;
 						}
 						
 						c++;
 					}
 					
 					if(converge && score > bestScore){
 						bestScore = score;
 						bestWinSize = winSize;
 						bestVec = new ValIdx[m2];
 						for(int i = 0; i < m2; i++){
 							bestVec[i] = new ValIdx(maMap.get(ma2Genes.get(i)), wVec[i]);
 						}
 					}
 					
 					
 				} // END power iteration
 			
 			} // END winSize iteration
 			
 			System.out.println(bestScore);
 			
 			if(bestScore < 0){
 				continue;
 			}
 			
 			String chr = gn.getChr(g);
 			
 			pw.print(g + "\t" + chr);
 			for(int i = 0; i < bestVec.length; i++){
 				pw.print("\t" + bestVec[i].idx + ":" + bestVec[i].val);
 			}pw.println();
 			
 		}// END idx iteration
 		pw.close();
 		
 	}
 
 	/*
 	 * Apr 6 2012
 	 * Probe-level find attractor
 	 * 
 	 */
 	public ValIdx[] findWeightedAttractor(DataFile ma, float[] vec, String[] allgenes, InverseAnnotations invannot, float power, Annotations annot) throws Exception{
 		float[][] data = ma.getData();
 		int m = data.length;
 		int n = data[0].length;
 		int mg = allgenes.length;
 		HashMap<String, Integer> probeMap = ma.getRows();
 		
 		float[] miVec = itc.getAllMIWith(vec, data);
 		float[] preMIVec = new float[m];
 		System.arraycopy(miVec, 0, preMIVec, 0, m);
 		int c = 0;
 		float convergeTh = precision * precision / m;
 		
 		ValIdx[] wVec = fillInWVec(miVec, allgenes, probeMap, invannot, mg); 
 		/*Arrays.sort(wVec);
 		for(int i = 0; i < 5; i++){
 			String gg = ma.getProbes().get(wVec[i].idx);
 			System.out.println(gg + "\t" + annot.getGene(gg) + "\t"  + wVec[i].val);
 		}*/
 		while(c < maxIter){
 			float[] metaGene = getWeightedMetageneFromPbs(data, wVec, power, m, mg, n);
 			//float[] metaGene = getWeightedMetaGene(data, wVec, power,  m, n);
 			
 			miVec = itc.getAllMIWith(metaGene, data);
 			float err = calcMSE(miVec, preMIVec, m);
 			System.out.println("delta: " + err);
 			if(err < convergeTh){
 				System.out.println("Converged.");
 				return wVec;
 			}
 			wVec = fillInWVec(miVec, allgenes, probeMap, invannot, mg);
 			/*Arrays.sort(wVec);
 			for(int i = 0; i < 5; i++){
 				String gg = ma.getProbes().get(wVec[i].idx);
 				System.out.println(gg + "\t" + annot.getGene(gg) + "\t"  + wVec[i].val);
 			}*/
 			
 			
 			System.arraycopy(miVec, 0, preMIVec, 0, m);
 			c++;
 		}
 		System.out.println("Not converged.");
 		//pw.close();
 		wVec[0] = null;
 		return wVec;
 
 	}
 	private float[] getWeightedMetageneFromPbs(float[][] data, ValIdx[] wVec,
 			float power, int m, int n, int mg) {
 		float[] out = new float[n];
 		double sum = 0;
 		for(int i = 0; i < mg; i++){
 			if(wVec[i].val > 0){
 				double f = Math.exp(power*Math.log(wVec[i].val));
 				sum += f;
 				for(int j = 0; j < n; j++){
 					out[j] += data[wVec[i].idx][j] * f;
 				}
 			}
 		}
 		for(int j = 0; j < n; j++){
 			out[j] /= sum;
 		}
 		return out;
 	}
 
 	private ValIdx[] fillInWVec(float[] mivec, String[] allgenes
 			, HashMap<String, Integer> probeMap,InverseAnnotations invannot, int mg ){
 		ValIdx[] wvec = new ValIdx[mg];
 		for(int i = 0; i < mg; i++){
 			String s = allgenes[i];
 			int bestIdx = -1;
 			float bestVal = -1;
 			for(String p : invannot.getProbes(s)){
 				//System.out.println(p);
 				if(probeMap.get(p) != null){
 					int idx = probeMap.get(p);
 					if(mivec[idx] > bestVal){
 						bestIdx = idx;
 						bestVal = mivec[idx];
 					}
 				}
 			}
 			wvec[i] = new ValIdx(bestIdx, bestVal);
 		}
 		return wvec;
 	}
 	
 	
 	public float[] findWeightedAttractor(DataFile ma, float[] vec, float power) throws Exception{
 		float[][] data = ma.getData();
 		int m = data.length;
 		int n = data[0].length;
 		//ArrayList<String> genes = ma.getProbes();
 		
 		float[] wVec = itc.getAllMIWith(vec, data);
 		
 		float[] preWVec = new float[m];
 		System.arraycopy(wVec, 0, preWVec, 0, m);
 		int c = 0;
 		float convergeTh = precision * precision / m;
 		
 		while(c < maxIter){
 			float[] metaGene = getWeightedMetaGene(data, wVec, power,  m, n);
 			wVec = itc.getAllMIWith(metaGene, data);
 			
 			float err = calcMSE(wVec, preWVec, m);
 			if(err < convergeTh){
 				System.out.println("Converged.");
 				return wVec;
 			}
 			System.arraycopy(wVec, 0, preWVec, 0, m);
 			c++;
 		}
 		System.out.println("Not converged.");
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
 				
 				float err = calcMSE(wVec, preWVec, m);
 				System.arraycopy(wVec, 0, preWVec, 0, m);
 				if(err < convergeTh){
 					Arrays.sort(preWVec);
 					if(preWVec[m-1] - preWVec[m-2] > 0.5){
 						System.out.println("Top dominated.");
 						converge=false;
 					}else{
 						System.out.println("Converged.");
 						converge=true;
 					}
 					break;
 				}
 				
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
 					pw.print("\t" + basin.get(j));
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
