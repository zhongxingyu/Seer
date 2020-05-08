 package caf;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import obj.DataFile;
 import obj.Genome;
 import obj.ValIdx;
 
 public class GroupWeightedAttractors {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		// TODO Auto-generated method stub
		String path = "/home/weiyi/workspace/javaworks/caf/output/640/";
 		if(!path.endsWith("/")){
 			path = path + "/";
 		}
 		
 		System.out.println("Loading files...");
 		final String geneLocFile = "/home/weiyi/workspace/data/annot/affy/u133p2/gene.location3";
 		final String dataFile = "/home/weiyi/workspace/data/brca/gse2034/ge.13271x286.var.txt";
 		
 		DataFile ma = DataFile.parse(dataFile);
 		Genome gn = Genome.parseGeneLocation(geneLocFile);
 		int m = ma.getNumRows();
 		int n = ma.getNumCols();
 		ArrayList<String> genes = ma.getProbes();
 		boolean CNV = true;
 		
 		if(CNV) gn.linkToDataFile(ma);
 		
 		BufferedReader br = new BufferedReader(new FileReader(path + "attractors.gwt"));
 		BufferedReader br2 = new BufferedReader(new FileReader(path + "attractees.gwt"));
 		String line = br.readLine();
 		String line2 = br2.readLine();
 		PrintWriter pw = new PrintWriter(new FileWriter(path + "attractors.top10.gwt"));
 		
 		while(line != null){
 			if(CNV){
 				String[] tokens = line.split("\t");
 				int numBasins = line2.split("\t").length - 2;
 				String name = tokens[0];
 				String chr = tokens[1];
 				
 				String[] genesInChr = gn.getAllGenesInChr(chr);
 				float range = gn.getChrCoordRange(chr);
 				int m2 = genesInChr.length;
 				/*System.out.print(m2);
 				System.out.println("\t" + tokens.length);*/
 				
 				pw.print(name + "\t" + chr + "\t" + numBasins);
 				float wVec[] = new float[m2];
 				int maxIdx = -1;
 				float maxW = -1;
 				for(int i = 0; i < m2; i++){
 					float w= Float.parseFloat(tokens[i+2]);
 					if(w > maxW){
 						maxIdx = i; 
 						maxW = w;
 					}
 					wVec[i] = w;
 				}
 				
 				float sum = 0;
 				ArrayList<ValIdx> vec = new ArrayList<ValIdx>();
 				float center = gn.getCoord(genesInChr[maxIdx]);
 				
 				for(int i = 0; i < m2; i++){
 					float f = Math.abs(gn.getCoord(genesInChr[i]) - center) / range;
 					
 					wVec[i] = (float)Math.exp(2 * Math.log(wVec[i] * (1-f)));
 					vec.add(new ValIdx(i, wVec[i]));
 					sum += wVec[i];
 				}
 				Collections.sort(vec);
 				for(int i = 0; i < 10; i++){
 					pw.print("\t" + genesInChr[vec.get(i).idx] + "(" + vec.get(i).val/sum + ")");
 				}pw.println();
 				
 			}else{
 				ArrayList<ValIdx> vec = new ArrayList<ValIdx>();
 				String[] tokens = line.split("\t");
 				int numBasins = line2.split("\t").length - 2;
 				String name = tokens[0];
 				pw.print(name + "\t" + numBasins);
 				float sum = 0;
 				for(int i = 0; i < m; i++){
 					float f= Float.parseFloat(tokens[i+2]);
 					float w = (float) Math.exp(5 * Math.log(f));
 					vec.add(new ValIdx(i, w));
 					
 					sum += w;
 				}
 				Collections.sort(vec);
 				for(int i = 0; i < 10; i++){
 					pw.print("\t" + genes.get(vec.get(i).idx) + "(" + vec.get(i).val/sum + ")");
 				}pw.println();
 			}
 			
 			line2 = br2.readLine();
 			line = br.readLine();
 		}
 		br.close();
 		pw.close();
 		System.out.println("Done.");
 	}
 
 }
