 package caf;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
 
 import obj.Annotations;
 import obj.DataFile;
 import obj.GeneSet;
 import worker.Converger.ValIdx;
 
 public class GroupAttractors {
 	
 	static GeneSet parseAttractor(String file, HashMap<String, Integer> rowmap) throws Exception{
 		BufferedReader br = new BufferedReader(new FileReader(file));
 		br.readLine();
 		String line = br.readLine();
 		ArrayList<ValIdx> viList = new ArrayList<ValIdx>();
 		while(line != null){
 			String[] tokens = line.split("\t");
 			int nt = tokens.length;
 			int i = rowmap.get(tokens[0]);
 			float v = Float.parseFloat(tokens[nt-1]);
 			viList.add(new ValIdx(i, v));
 			line = br.readLine();
 		}
 		br.close();
 		return new GeneSet(viList.toArray(new ValIdx[0]));
 	}
 	
 	static ArrayList<GeneSet> parseAttractorInOneFile(String file, HashMap<String, Integer> rowmap) throws Exception{
 		BufferedReader br = new BufferedReader(new FileReader(file));
 		String line = br.readLine();
 		ArrayList<GeneSet> allAttractors = new ArrayList<GeneSet>();
 		while(line != null){ // each line is an attractor
 			String[] tokens = line.split("\t");
 			String name = tokens[0];
 			int nt = tokens.length;
 			int numChild = Integer.parseInt(tokens[1].split(":")[1]);
 			ArrayList<ValIdx> viList = new ArrayList<ValIdx>();
 			for(int j = 2; j < nt; j++){
 				String[] t2 = tokens[j].split(":");
 				int i = rowmap.get(t2[0]);
 				float v = Float.parseFloat(t2[1]);
 				viList.add(new ValIdx(i, v));
 				
 			}
 			allAttractors.add(new GeneSet(name, viList.toArray(new ValIdx[0]), numChild));
 			line = br.readLine();
 		}
 		br.close();
 		return allAttractors;
 	}
 	private static float[] getMetaGene(float[][] data, ValIdx[] idx, int n){
 		int m = idx.length;
 		float[] out = new float[n];
 		for(int j = 0; j < n; j++){
 			for(ValIdx vi : idx){
 				out[j] += data[vi.idx()][j];
 			}
 			out[j] /= m;
 		}
 		return out;
 	}
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
		String path = "/home/weiyi/workspace/javaworks/caf/output/ov.gse9891L3.rownorm.z11";
 		if(!path.endsWith("/")){
 			path = path + "/";
 		}
 		
 		boolean annotation = false;
 		int minSize = 10;
 		
 		System.out.println("Loading files...");
		DataFile ma = DataFile.parse("/home/weiyi/workspace/data/ov/gse9891/ge.20765x285.var.txt");
 		//DataFile ma = DataFile.parse("/home/weiyi/workspace/data/ov/tcga/ge/ge.12042x582.txt");
 		//ma.normalizeRows();
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		float[][] data = ma.getData();
 		HashMap<String, Integer> rowmap = ma.getRows();
 		
 		String annotPath = "/home/weiyi/workspace/data/annot/affy/u133p2/annot.csv";
 		Annotations annot = annotation? Annotations.parseAnnotations(annotPath) : null;
 		
 		ArrayList<String> probeNames = ma.getProbes();
 		if(annotation){
 			GeneSet.setAnnotations(annot);
 		}
 		GeneSet.setProbeNames(probeNames);
 		
 		
 		System.out.print("Loading gene sets...");
 		
 		/*ArrayList<GeneSet> allGeneSet = new ArrayList<GeneSet>();
 		String[] attractorFiles = new File(path + "lists/").list();
 		Arrays.sort(attractorFiles);
 		int na = attractorFiles.length;
 		for(String f : attractorFiles){
 			GeneSet gs = parseAttractor(path + "lists/" + f, rowmap);
 			gs.setName(f);
 			allGeneSet.add(gs);
 		}*/
 		
 		ArrayList<GeneSet> allGeneSet = parseAttractorInOneFile(path + "attractors.gwt", rowmap);
 		int N = allGeneSet.size();
 		System.out.println(N + " gene sets are loaded.");
 		
 		ArrayList<Integer> deleteIdx = new ArrayList<Integer>();
 		
 		System.out.println("Filtering gene sets...");
 		int progress = 0;
 		
 		for(int i = 0; i < N; i++){
 			progress++;
 			boolean delete = false;
 			GeneSet gs = allGeneSet.get(i);
 			if(gs.size() < minSize){
 				delete = true;
 			}else{
 				//System.out.print(gs.getName());
 				//int sz = gs.size();
 				int sz = gs.getNumChild();
 				float lastMI = gs.getGeneIdx()[minSize-1].val();
 				for(int j = 0; j < N; j++){
 					if(j == i){
 						continue;
 					}
 					GeneSet gs2 = allGeneSet.get(j);
 					if(gs2.size() < minSize) continue;
 					int ovlp = gs.overlapWith(gs2);
 					if(ovlp < 1) continue;
 					HypergeometricDistributionImpl phyper = new HypergeometricDistributionImpl(m, gs.size(), gs2.size());
 					double p = phyper.upperCumulativeProbability(ovlp) * N;
 					if(p < 0.05){
 					//else{
 						//System.out.print("\t" + gs2.getName());
 						//int sz2 = gs2.size();
 						int sz2 = gs2.getNumChild();
 						if(sz2 > sz){
 							delete=true;
 							break;
 						}else if(sz2 == sz){
 							//System.out.print("\t" + "here");
 							float lastMI2 = gs2.getGeneIdx()[minSize-1].val();
 							if(lastMI2 > lastMI){
 								
 								delete = true;
 								break;
 							}
 						}
 					}
 				}
 			}
 			if(delete){
 				//System.out.print("\tdelete");
 				deleteIdx.add(i);
 			}
 			//System.out.println();
 			
 			if(progress % 100 == 0) System.out.println(progress + " / " + N);
 		}
 		Collections.sort(deleteIdx);
 		Collections.reverse(deleteIdx);
 		for(Integer i : deleteIdx){
 			allGeneSet.remove(i.intValue());
 		}
 		N = allGeneSet.size();
 		System.out.println(N + " gene sets are left.");
 		System.out.println("Output to files...");
 		
 		PrintWriter pw = new PrintWriter(new FileWriter(path + "ClusterLeaders.txt"));
 		//PrintWriter pw4 = new PrintWriter(new FileWriter(path + "forProf.txt"));
 		
 		
 		//int numOut = 5;
 		
 		for(int i = 0; i < N; i++){
 			GeneSet gs = allGeneSet.get(i);
 			pw.print(gs.getName() + "\t" + gs.size() + "\t");
 			if(annotation){
 				pw.println(gs.toGenes());
 			}else{
 				pw.println(gs.toProbes());
 			}
 			
 			//ValIdx[] lala = gs.getGeneIdx();
 			/*pw4.print(gs.getName() + "\t" + gs.size());
 			for(int j = 0; j < numOut; j++){
 				if(annotation){
 					pw4.print("\t" + probeNames.get(lala[j].idx()) + " : " + annot.getGene(probeNames.get(lala[j].idx())));
 				}else{
 					pw4.print("\t" + probeNames.get(lala[j].idx()));
 				}
 			}
 			pw4.println();*/
 			
 		}
 		pw.close();
 		//pw4.close();
 		/*for(ArrayList<GeneSet> gslist : cluster){
 			String name = "Cluster" + String.format("%03d", cnt) + ".txt";
 			PrintWriter pw = new PrintWriter(new FileWriter(path + "clusters/" + name));
 			pw3.print(name);
 			Integer[] member = members.get(cnt);
 			for(int i : member){
 				if(annotation){
 					pw3.print("\t" + probeNames.get(i) + ":" + annot.getGene(probeNames.get(i)));
 				}else{
 					pw3.print("\t" + probeNames.get(i));
 				}
 			}
 			
 			int sz = gslist.size();
 			for(int i = 0; i < sz; i++){
 				GeneSet gs = gslist.get(i);
 				pw.print(gs.getName());
 				pw.print("\t" + gs.size() + "\t");
 				if(!annotation){
 					pw.println(gs.toProbes());
 				}else{
 					pw.println(gs.toGenes());
 				}
 			}
 			cnt++;
 			pw.close();
 		}*/
 		
 		int k = allGeneSet.size();
 		
 		float[][] newData = new float[k][n];
 		ArrayList<String> attractorNames = new ArrayList<String>();
 		HashMap<String, Integer> newRowMap = new HashMap<String, Integer>();
 		
 		int cnt = 0;
 		System.out.println("Calculating metagenes...");
 		for(GeneSet gs: allGeneSet){
 			attractorNames.add(gs.getName());
 			newRowMap.put(gs.getName(),cnt);
 			System.arraycopy(getMetaGene(data, gs.getGeneIdx(), n), 0, newData[cnt], 0, n);
 			cnt++;
 		}
 		
 		DataFile out = new DataFile(newData, newRowMap, ma.getCols(), attractorNames, ma.getChipID());
 		System.out.println("Output to files...");
 		out.output2Gct(path + "attractorSpace." + out.getNumRows() + "x" + out.getNumCols() + ".txt");
 		
 		
 		System.out.println("Done.");
 	}
 
 }
