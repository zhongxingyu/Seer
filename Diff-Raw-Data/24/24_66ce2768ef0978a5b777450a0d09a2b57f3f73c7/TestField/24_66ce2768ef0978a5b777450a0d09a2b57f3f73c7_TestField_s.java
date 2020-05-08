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
 
 import org.apache.commons.math.distribution.NormalDistributionImpl;
 
 import obj.Annotations;
 import obj.DataFile;
 import obj.Genome;
 import obj.ValIdx;
 
 import util.StatOps;
 import worker.Converger;
 import worker.ITComputer;
 
 public class TestField {
 	private static float[] getMetaGene(float[][] data, ArrayList<Integer> idx, int n){
 		int m = idx.size();
 		float[] out = new float[n];
 		for(int j = 0; j < n; j++){
 			for(Integer i : idx){
 				out[j] += data[i][j];
 			}
 			out[j] /= m;
 		}
 		return out;
 	}
 	private static ArrayList<Integer> getAttractorIdx(float[][] data, float[][] val, ArrayList<Integer> inputIdx, int m, int n, ITComputer itc, int attractorSize, int maxIter) throws Exception{
 		ArrayList<Integer> metaIdx = inputIdx;
 		ArrayList<Integer> preMetaIdx = new ArrayList<Integer>();
 		ArrayList<Integer> cycMetaIdx = new ArrayList<Integer>();
 		
 		preMetaIdx.addAll(inputIdx);
 		cycMetaIdx.addAll(inputIdx);
 		
 		float[] metagene = getMetaGene(data, metaIdx, n);
 		ValIdx[] vec = new ValIdx[m];
 		int cnt = 0;
 		
 		
 		while(cnt < maxIter){
 			float[] mi = itc.getAllMIWith(metagene, val);
 			for(int i = 0; i < m; i++){
 				vec[i] = new ValIdx(i, mi[i]);
 			}
 			Arrays.sort(vec);
 			
 			System.out.println(cnt + "\t" + vec[0].idx() + "\t" + vec[attractorSize/2].idx() + "\t" 
 					+ "\t" + vec[attractorSize-1].idx());
 			
 			metaIdx.clear();
 			for(int i = 0; i < attractorSize; i++){
 				metaIdx.add(vec[i].idx());
 			}
 			if(metaIdx.equals(preMetaIdx) || metaIdx.equals(cycMetaIdx)){
 				break;
 			}
 			cycMetaIdx.clear();
 			cycMetaIdx.addAll(preMetaIdx);
 			preMetaIdx.clear();
 			preMetaIdx.addAll(metaIdx);
 			cnt++;
 			metagene = getMetaGene(data, metaIdx, n);
 			
 		}
 		
 		return metaIdx;
 	}
 /*	static class ValIdx implements Comparable<ValIdx>{
 		float val;
 		int idx;
 		ValIdx(int i, float v){
 			this.idx = i;
 			this.val = v;
 		}
 		
 		public int compareTo(ValIdx other) {
 			return -Double.compare(this.val, other.val);
 		}
 	}*/
 	
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		String path = "/home/weiyi/workspace/data/brca/gse2034/";
 		if(!path.endsWith("/")){
 			path = path + "/";
 		}
 		
 		
 		System.out.println("Loading files...");
 		DataFile ma = DataFile.parse(path + "ge.13271x286.var.txt");
 		
 		final String geneLocFile = "/home/weiyi/workspace/data/annot/affy/u133p2/gene.location3";
 		//final String geneLocFile = "/home/weiyi/workspace/javaworks/caf/output/639/gene.location3";
 		
 		String command = "CNV";
 		float power = 2f;
 		boolean excludeTop = false;
 		boolean miDecay = true;
 		int winSize = -1;
 		
 		//ma.normalizeRows();
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		float[][] data = ma.getData();
 		
 		ArrayList<String> gs = new ArrayList<String>();
 		/*BufferedReader br = new BufferedReader(new FileReader("COL11A1_50"));
 		br.readLine();
 		String line = br.readLine();
 		while(line != null){
 			String[] tokens = line.split("\t");
 			gs.add(tokens[0]);
 			line = br.readLine();
 		}
 		br.close();*/
 		
 		//gs.add("LAPTM5");
 		//gs.add("ZNF777");
		gs.add("EXOSC4");
		gs.add("PUF60");
		gs.add("LSM1");
		gs.add("ASH2L");
 		long jobID = System.currentTimeMillis();
 		
 		//String annotPath = "/home/weiyi/workspace/data/annot/affy/u133p2/annot.csv";
 		//Annotations annot = Annotations.parseAnnotations(annotPath);
 		Annotations annot = null;
 		
 		Converger cvg = new Converger(0, 1, jobID);
 		
 		Genome gn = Genome.parseGeneLocation(geneLocFile);
 		if(command.equals("CNV")) gn.linkToDataFile(ma);
 		
 		/*String[] chr8 = gn.getNeighbors("ASH2L", 51);
 		for(String s : chr8){
 			gs.add(s);
 		}*/
 		
 		
 		ITComputer itc = new ITComputer(6, 3, 0, 1, true);
 		cvg.linkITComputer(itc);
 		HashMap<String, Integer> geneMap = ma.getRows();
 		ArrayList<String> geneNames = ma.getProbes();
 		
 		new File("tmp").mkdir();
 		for(String g : gs){
 			if(geneNames.contains(g)){
 				System.out.println("Processing " + g + " (" + (gs.indexOf(g)+1) + "/" + gs.size() + ")" + "...");
 				String outFile = command.equals("CAF")? "tmp/" + g + "_Attractor.txt" : "tmp/" + g + "_CNV.txt";
 				try{
 					PrintWriter pw = new PrintWriter(new FileWriter(outFile));
 					float[] out = new float[m];
 					float[] vec = new float[m];
 					int idx = geneMap.get(g);
 					if(command.equals("CAF")){
 						vec = data[idx];
 						out = cvg.findWeightedAttractor(ma, g, vec, power, excludeTop);
 					}else if(command.equals("CNV")){
 						String[] neighbors = gn.getNeighbors(g, winSize);
 						if(neighbors == null){
 							pw.println("No neighbors");
 							pw.close();
 							continue;
 						}
 						DataFile ma2 = ma.getSubProbes(neighbors);
 						geneNames = ma2.getProbes();
 						m = ma2.getNumRows();
 						idx = ma2.getRows().get(g);
 						data = ma2.getData();
 						vec = data[idx];
 						out = cvg.findWeightedCNV(ma2, g, gn, vec, power, excludeTop, miDecay);
 					}else{
 						System.out.println("Wrong command!");
 						System.exit(1);
 					}
 					
 					
 					if(out[0] == -1){
 						pw.println("Not converged.");
 						pw.close();
 						continue;
 					}
 					
 					ArrayList<ValIdx> vi = new ArrayList<ValIdx>();
 					for(int i = 0; i < m; i++){
 						out[i] = (float) Math.round(out[i] * 10000) / 10000;
 						vi.add(new ValIdx(i, out[i]));
 					}
 					Collections.sort(vi);
 					for(int i = 0; i < m; i++){
 						pw.println(geneNames.get(vi.get(i).idx) + "\t" + vi.get(i).val);
 					}
 					pw.close();
 				}catch (FileNotFoundException e){
 					System.out.println("Exception: " + e);
 					continue;
 				}
 			}else{
 				System.out.println("Does not contain gene " + g + "!!");
 			}
 		}
 		
 		/*cvg.setZThreshold(8f);
 		cvg.setConvergeMethos("ZSCORE");
 		cvg.setMIParameter(6, 3);*/
 		
 		/*HashMap<String, Integer> geneMap = ma.getRows();
 		ArrayList<String> attractees = new ArrayList<String>();
 		ArrayList<ArrayList<ValIdx>> attractors = new ArrayList<ArrayList<ValIdx>>();
 		ArrayList<String> geneNames = ma.getProbes();
 		int cnt = 0;
 		for(String g : gs){
 			System.out.println(g + "...");
 			int idx = geneMap.get(g);
 			ArrayList<ValIdx> out = cvg.findAttractor(data, idx);
 			if(attractors.contains(out)){
 				int j = attractors.indexOf(out);
 				String s = attractees.get(j);
 				s = s + "," + g;
 				attractees.set(j, s);
 			}else{
 				attractees.add(g);
 				attractors.add(out);
 			}
 			cnt = cnt + 1;
 			if(cnt == 10){
 				break;
 			}
 		}
 		
 		PrintWriter pw = new PrintWriter(new FileWriter("lala.txt"));
 		int kk = attractors.size();
 		for(int i = 0; i < kk; i++){
 			pw.print(attractees.get(i));
 			ArrayList<ValIdx> vv = attractors.get(i);
 			//Collections.sort(vv);
 			for(ValIdx vj : vv){
 				pw.print("\t" + geneNames.get(vj.idx()));
 				if(annot != null){
 					pw.print(":" + annot.getGene(geneNames.get(vj.idx())));
 				}
 				pw.print(":" + vj.val());
 			}
 			pw.println();
 		}
 		pw.close();*/
 		
 		/*String path = "/home/weiyi/workspace/javaworks/caf/output/207/";
 		
 		BufferedReader br = new BufferedReader(new FileReader(path + "leadingAttractors.txt"));
 		ArrayList<String> leadingAttractors = new ArrayList<String>();
 		String line = br.readLine();
 		while(line != null){
 			leadingAttractors.add(line.replaceAll(".txt", ""));
 			line = br.readLine();
 		}
 		br.close();
 		
 		br = new BufferedReader(new FileReader(path + "attractors.gwt"));
 		PrintWriter pw = new PrintWriter(new FileWriter(path + "leadingAttractors.detail.txt"));
 		line = br.readLine();
 		while(line != null){
 			String name = line.split("\t")[0];
 			if(leadingAttractors.contains(name)){
 				pw.println(leadingAttractors.indexOf(name) + "\t" + line);
 			}
 			line = br.readLine();
 		}
 		pw.close();
 		br.close();*/
 		
 		System.out.println("Done.");
 	}
 
 }
