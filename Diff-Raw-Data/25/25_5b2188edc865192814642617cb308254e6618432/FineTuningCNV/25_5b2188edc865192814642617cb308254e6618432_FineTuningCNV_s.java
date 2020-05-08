 package caf;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import obj.DataFile;
 import obj.Genome;
 import obj.ValIdx;
 import worker.Converger;
 import worker.ITComputer;
 
 public class FineTuningCNV {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		int wstart = 51;
 		int wend = 301;
 		float estart = 1;
 		float eend = 6f;
 		
 		int quantile = 5;
 		
 		String targetArm = "chr8q24";
 		
 		System.out.println("Loading files...");
 		
 		//String outPath = "/home/weiyi/workspace/javaworks/caf/output/656/";
 		
 		
 		final String[] dataFiles={
 			"/home/weiyi/workspace/data/brca/gse2034/ge.12160x286.jetset.mean.txt",
 			"/home/weiyi/workspace/data/brca/tcga/ge/ge.17814x536.knn.txt",
 			"/home/weiyi/workspace/data/coad/gse14333/ge.19189x290.jetset.mean.txt",
 			"/home/weiyi/workspace/data/coad/tcga/ge/ge.17814x154.knn.txt",
 			"/home/weiyi/workspace/data/ov/gse9891/ge.19189x285.jetset.mean.txt",
 			"/home/weiyi/workspace/data/ov/tcga/ge/ge.17814x584.knn.txt",
 			"/home/weiyi/workspace/data/ov/tcga/ge/ge.12042x582.txt"
 		};
 		
 		final String[] outputDirs={
 			"brca.gse2034.jetset.mean",
 			"brca.tcga",
 			"coad.gse14333.jetset.mean",
 			"coad.tcga",
 			"ov.gse9891.jetset.mean",
 			"ov.tcga.affy"
 		};
 		
 		
 		String outPath = "/home/weiyi/workspace/javaworks/caf/output/cnv.finetune/hsf1.try/";
 		if(!outPath.endsWith("/")){
 			outPath = outPath + "/";
 		}
 		new File("output").mkdir();
 		new File("output/cnv.finetune").mkdir();
 		new File(outPath).mkdir();
 		
 		final String geneLocFile = "/home/weiyi/workspace/data/annot/affy/u133p2/gene.location4";
 		//final String geneLocFile = "output/window/gene.location3";
 		Genome gn = Genome.parseGeneLocation(geneLocFile);
 		
 		
 		for(int qq = 0; qq < dataFiles.length; qq++)
 		{
 		
 		System.out.println("Data: " + dataFiles[qq]);
 		new File(outPath + outputDirs[qq]).mkdir();
 		
 		
 		DataFile ma = DataFile.parse(dataFiles[qq]);
 		
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		float[][] data = ma.getData();
 		
 		//gn.linkToDataFile(ma);
 		//String[] testList = gn.getAllGenesInChrArm(targetArm);
 		String[] testList = gn.getNeighbors("HSF1", 31);
 				
 		long jobID = System.currentTimeMillis();
 		Converger cvg = new Converger(0, 1, jobID);
 		ITComputer itc = new ITComputer(6, 3, 0, 1, true);
 		//itc.negateMI(true);
 		cvg.linkITComputer(itc);
 		HashMap<String, Integer> geneMap = ma.getRows();
 		
 		
 		System.out.println(testList.length + " genes were selected.");
 		
 		String outSummaryFile = outPath + outputDirs[qq] + "/ScoreSummary.txt";
 		PrintWriter pw2 = new PrintWriter(new FileWriter(outSummaryFile));
 		pw2.println("Seed\tScore");
 		ArrayList<String> geneNames = ma.getProbes();
 		String undisputedSeed = "";
 		float undisputedScore = -1;
 		
 		for(String gtest : testList){
 			if(!geneNames.contains(gtest)){
 				continue;
 			}
 			float power = estart;
 			float bestScore = -1;
 			System.out.print("Testing " + gtest + "...");
 			ValIdx[] bestVec = new ValIdx[wstart];
 			int bestWSize = 0;
 			float bestPow = 0;
 			while(true){
 				if(power > eend){
 					break;
 				}
 				int wsize = wstart;
 				while(true){
 					if(wsize > wend){
 						break;
 					}
 					//System.out.println("Window size " + wsize + "\tPower " + power + "...");
 								
 					String[] neighbors = gn.getNeighbors(gtest, wsize);
					if(neighbors == null || neighbors.length < quantile){
 						//pw.println("No neighbors");
 						//pw.close();
 						continue;
 					}
 					DataFile ma2 = ma.getSubProbes(neighbors);
 					geneNames = ma2.getProbes();
 					m = ma2.getNumRows();
 					int idx = ma2.getRows().get(gtest);
 					data = ma2.getData();
 					float[] vec = data[idx];
 					float[] out = cvg.findWeightedCNV(ma2, gtest, gn, vec, wsize, power,  false, false);
								
 					if(out[0] == -1){
 						//pw.println("Not converged.");
 						//pw.close();
 						wsize += 10;
 						continue;
 					}
 								
 					ValIdx[] vis = new ValIdx[m];
 					for(int i = 0; i < m; i++){
 						vis[i] = new ValIdx(i, out[i]);
 					}
 					Arrays.sort(vis);
 							
 					float score = vis[quantile-1].val;
 					if(score > bestScore){
 						bestScore = score;
 						bestVec = new ValIdx[m];
 						System.arraycopy(vis, 0, bestVec, 0, m);
 						bestWSize = wsize;
 						bestPow = power;
 					}
 					//System.out.println("\tScore: " + score);
 					wsize += 50;
 				}
 				power += 0.5;	
 			}
 			if(bestScore > undisputedScore){
 				undisputedScore = bestScore;
 				undisputedSeed = gtest;
 			}
 			System.out.println("\tScore: " + bestScore);
 			pw2.println(gtest + "\t" + bestScore);
 			String outFile = outPath + outputDirs[qq] + "/" + targetArm + "_Attractor_" + gtest + ".txt";
 			String[] neighbors = gn.getNeighbors(gtest, bestWSize);
 			DataFile ma2 = ma.getSubProbes(neighbors);
 			geneNames = ma2.getProbes();
 			int m2 = ma2.getNumRows();
 			PrintWriter pw = new PrintWriter(new FileWriter(outFile));
 			for(int i = 0; i < m2; i++){
 				String gg = geneNames.get(bestVec[i].idx);
 				pw.println(gg + "\t"  + bestVec[i].val);
 			}
 			pw.close();
 			
 			
 		}
 		pw2.close();
 		System.out.println("Final best seed: " + undisputedSeed);
 		
 		
 		}
 	}
 
 }
