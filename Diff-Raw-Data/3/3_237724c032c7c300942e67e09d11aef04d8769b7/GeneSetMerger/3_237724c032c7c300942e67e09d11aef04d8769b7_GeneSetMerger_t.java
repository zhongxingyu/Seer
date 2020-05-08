 package worker;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import obj.GeneSet;
 import obj.ValIdx;
 
 public class GeneSetMerger extends DistributedWorker{
 	ArrayList<GeneSet> allGeneSets;
 	public static int mergeCount = 0;
 	public static int minSize = 0;
 	
 	public GeneSetMerger(int id, int totalComputers, long jobID){
 		super(id, totalComputers, jobID);
 		allGeneSets = new ArrayList<GeneSet>();
 	}
 	
 	
 	public void mergeWeightedGeneSets(String path, int numFiles, float precision, boolean finalOutput) throws IOException{
 		if(!path.endsWith("/")) path = path + "/";
 		int start = id * numFiles/totalComputers;
 		int end = (id+1) * numFiles/totalComputers;
 		BufferedReader br;
 		ArrayList<float[]> wVecs = new ArrayList<float[]>();
 		ArrayList<ArrayList<Integer>> basins = new ArrayList<ArrayList<Integer>>();
 		ArrayList<String> chrs = new ArrayList<String>(); 
 		
 		System.out.println("Processing file " + start + " to file " + end );
 		for(int i = start; i < end; i++){
 			System.out.println(i);
 			br = new BufferedReader(new FileReader(path + "caf."+ String.format("%05d", i)+".txt"));
 			String line = br.readLine();
 			// Greedily merge gene set
 			while(line != null){
 				String[] tokens = line.split("\t");
 				String tag = tokens[0];
 				int nt = tokens.length;
 				int m = nt-2;
 				float[] wvec = new float[m];
 				ArrayList<Integer> basin = new ArrayList<Integer>();
 				String[] t2 = tokens[1].split(",");
 				int nt2 = t2.length;
				if(finalOutput && nt2 < 2){
					continue;
				}
 				for(int j = 0; j < nt2; j++){
 					basin.add(Integer.parseInt(t2[j]));
 				}
 				for(int j = 0; j < m; j++){
 					wvec[j] = Float.parseFloat(tokens[j+2]);
 				}
 				boolean newOne = true;
 				int foundIdx = -1;
 				for(int j = 0; j < wVecs.size(); j++){
 					if(tag.equals(chrs.get(j))){
 						float[] fs = wVecs.get(j);
 						float err = Converger.calcMSE(fs, wvec, m);
 						if(err < precision/m){
 							foundIdx = j;
 							newOne = false;
 							break;
 						}
 					}
 				}
 				if(newOne){
 					wVecs.add(wvec);
 					basins.add(basin);
 					chrs.add(tag);
 				}else{
 					basins.get(foundIdx).addAll(basin);
 				}
 				line = br.readLine();
 			}
 			br.close();
 		}
 		if(finalOutput){
 			new File("output").mkdir();
 			new File("output/" + jobID).mkdir();
 			PrintWriter pw = new PrintWriter(new FileWriter("output/" + jobID + "/attractors.gwt"));
 			PrintWriter pw2 = new PrintWriter(new FileWriter("output/" + jobID + "/attractees.gwt"));
 			
 			for(int i = 0; i < wVecs.size(); i++){
 				ArrayList<Integer> basin = basins.get(i);
 				if(basin.size() < 2){
 					continue;
 				}
 				String name = "Attractor" + String.format("%05d", i);
 				pw2.print(name + "\t" + chrs.get(i));
 				pw.print(name+ "\t" + chrs.get(i));
 				
 				for(int j : basin){
 					pw2.print("\t" + j);
 				}pw2.println();
 				
 				float[] fs = wVecs.get(i);
 				for(float f : fs){
 					pw.print("\t" + f);
 				}pw.println();
 			}
 			pw2.close();
 			pw.close();
 		}else{
 			prepare("merge" + mergeCount);
 			PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/merge" + mergeCount+ "/caf."+ String.format("%05d", id)+".txt"));
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
 			
 			mergeCount++;
 		
 		}
 	}
 	public void mergeGeneSets(String path, int numFiles, boolean finalOutput) throws IOException{
 		if(!path.endsWith("/")) path = path + "/";
 		int start = id * numFiles/totalComputers;
 		int end = (id+1) * numFiles/totalComputers;
 		BufferedReader br;
 		System.out.println("Processing file " + start + " to file " + end );
 		for(int i = start; i < end; i++){
 			System.out.println(i);
 			br = new BufferedReader(new FileReader(path + "caf."+ String.format("%05d", i)+".txt"));
 			String line = br.readLine();
 			// Greedily merge gene set
 			while(line != null){
 				String[] tokens = line.split("\t");
 				if(!tokens[2].equals("NA")){
 					//first token: attractees separated by ","
 					HashSet<Integer> attr = new HashSet<Integer>();
 					String[] t2 = tokens[0].split(",");
 					for(String s: t2){
 						attr.add(Integer.parseInt(s));
 					}
 					int nt = tokens.length;
 					ValIdx[] geneIdx = new ValIdx[nt-2];
 					float[] Zs = new float[nt-2];
 					
 					//int[] gIdx = new int[nt-2];
 					//float[] wts = new float[nt-2]; // mi with metagene
 					int numChild = Integer.parseInt(tokens[1]);
 					for(int j = 2; j < nt; j++){
 						t2 = tokens[j].split(",");
 						geneIdx[j-2] = new ValIdx(Integer.parseInt(t2[0]), Float.parseFloat(t2[1]));
 						Zs[j-2] = t2.length > 2 ? Float.parseFloat(t2[2]) : Float.NaN;
 						//gIdx[j-2] = Integer.parseInt(t2[0]);
 						//wts[j-2] = Float.parseFloat(t2[1]);
 					}
 					//GeneSet rookie = new GeneSet(attr,gIdx, wts, numChild); 
 					GeneSet rookie = new GeneSet(attr, geneIdx, Zs, numChild);
 					int origSize = allGeneSets.size();
 					if(origSize == 0){
 						allGeneSets.add(rookie);
 					}else{
 						boolean mergeable = false;
 						for(int j = 0; j < origSize; j++){
 							GeneSet gs = allGeneSets.get(j);
 							if(gs.equals(rookie)){
 								mergeable = true;
 								gs.merge(rookie);
 								break;
 							}
 							/*if(gs.merge(rookie)){
 								mergeable = true;
 								break;
 								// gene set merged
 							}*/
 						}
 						
 						if(!mergeable){
 							allGeneSets.add(rookie);
 						}
 					}
 				}
 				line = br.readLine();
 			}
 			br.close();
 		}
 		if(finalOutput){
 			new File("output").mkdir();
 			new File("output/" + jobID).mkdir();
 			//new File("output/" + jobID + "/lists").mkdir();
 			PrintWriter pw = new PrintWriter(new FileWriter("output/" + jobID + "/attractors.gwt"));
 			PrintWriter pw2 = new PrintWriter(new FileWriter("output/" + jobID + "/attractees.gwt"));
 			//PrintWriter pw3 = new PrintWriter(new FileWriter("output/" + jobID + "/weights.txt"));
 			
 			int cnt = 0;
 			for(GeneSet gs : allGeneSets){
 				//if(gs.size() >= minSize){
 					String name = "Attractor" + String.format("%03d", cnt);
 					
 					gs.sort();
 					//gs.calcWeight();
 					/*pw3.print(name + "\t" + gs.size() + ":" + gs.getAttracteeSize() + "\t");
 					pw3.println(gs.getWeight());*/
 					
 					pw2.print(name + "\t" + gs.size() + ":" + gs.getAttracteeSize() + "\t");
 					pw2.println(gs.getAttractees());
 					
 					pw.print(name + "\t" + gs.size() + ":" + gs.getAttracteeSize() + "\t");
 					if(GeneSet.hasAnnot()){
 						pw.println(gs.toGenes());
 					}else{
 						pw.println(gs.toProbes());
 					}
 					
 					/*PrintWriter pw4 = new PrintWriter(new FileWriter("output/" + jobID + "/lists/" + name + ".txt"));
 					if(GeneSet.hasAnnot()){
 						pw4.println("Probe\tGene\tWeight");
 						//int[] indices = gs.getGeneIdx();
 						for(int i = 0; i < gs.size(); i++){
 							pw4.println(gs.getOnePair(i));
 						}
 					}else{
 						pw4.println("Gene\tWeight");
 						for(int i = 0; i < gs.size(); i++){
 							pw4.println(gs.getOnePair(i));
 						}
 					}
 					
 					pw4.close();*/
 					cnt++;
 				//}
 			}
 			//pw3.close();
 			pw2.close();
 			pw.close();
 		}else{
 			prepare("merge" + mergeCount);
 			PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/merge" + mergeCount + "/caf."+ String.format("%05d", id)+".txt"));
 			for(GeneSet gs : allGeneSets){
 				pw.println(gs.toString());
 			}
 			pw.close();
 			mergeCount++;
 		}
 	}
 	public void setMinSize(int minSize){
 		GeneSetMerger.minSize = minSize;
 	}
 	public static void addMergeCount(){
 		mergeCount++;
 	}
 }
