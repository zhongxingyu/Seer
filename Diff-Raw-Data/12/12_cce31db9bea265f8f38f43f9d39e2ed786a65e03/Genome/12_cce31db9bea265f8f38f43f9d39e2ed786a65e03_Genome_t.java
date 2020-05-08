 package obj;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 
 public class Genome {
 	static class IntPair{
 		int x;
 		int y;
 		
 		IntPair(int x){
 			this.x = x;
 		}
 		
 		IntPair(int x, int y){
 			this.x = x;
 			this.y = y;
 		}
 		
 		void setX(int x){
 			this.x = x;
 		}
 		
 		void setY(int y){
 			this.y = y;
 		}
 		
 		public String toString(){
 			return x + "\t" + y;
 			
 		}
 	}
 	static class Gene implements Comparable<Gene>{
 		int entrez;
 		String name;
 		String chr;
 		String chrArm;
 		String chrBand;
 		float coord;
 		
 		Gene(int entrez){
 			this.entrez = entrez;
 		}
 		Gene(int entrez, String name, String chr, String chrArm, String chrBand, float coord){
 			this.entrez = entrez;
 			this.name = name;
 			this.chr = chr;
 			this.chrArm = chrArm;
 			this.chrBand = chrBand;
 			this.coord = coord;
 		}
 		public int hashCode(){
 			return entrez;
 		}
 		public boolean equals(Object other){
 			boolean result = false;
 	        if (other instanceof Gene) {
 	        	Gene that = (Gene) other;
 	            result = this.entrez == that.entrez;
 	        }
 	        return result;
 		}
 		public int compareTo(Gene other) {
 			int k = this.chr.compareTo(other.chr);
 			if( k == 0){
 				return Double.compare(this.coord, other.coord);
 			}else{
 				return k;
 			}
 			
 		}
 	}
 	
 	ArrayList<Gene> genes;
 	HashMap<String, String> chrMap;
 	HashMap<String, String> chrArmMap;
 	HashMap<String, String> chrBandMap;
 	HashMap<String, Integer> idxMap;
 	HashMap<String, Float> coordMap;
 	HashMap<String, IntPair> chrIdxRangeMap;
 	HashMap<String, Integer> entrezMap;
 	
 	public static Genome parseGeneLocation(String file)throws Exception{
 		BufferedReader br = new BufferedReader(new FileReader(file));
 		ArrayList<Gene> genes = new ArrayList<Gene>();
 		
 		br.readLine(); // first row header
 		String line = br.readLine();
 		int lncnt = 0;
 		
 		while(line != null){
 			String[] tokens = line.split("\t");
 			int entrez = Integer.parseInt(tokens[0]);
 			String name = tokens[7];
 			String chr = tokens[1];
 			String chrBand = tokens[2];
 			String chrArm = "---";
 			if (tokens[1].contains("p")){
 				chrArm = chr + "p";
 			}else if(tokens[1].contains("q")){
 				chrArm = chr + "q";
 			}
 			//int startCoord = Integer.parseInt(tokens[4]);
 			//int endCoord = Integer.parseInt(tokens[5]);
 			
 			float chrCoord = Float.parseFloat(tokens[6]);
 			genes.add(new Gene(entrez, name, chr, chrArm, chrBand, chrCoord));
 			lncnt++;
 			line = br.readLine();
 		}
 		br.close();
 		
 		System.out.println("Genome contains " + lncnt + " genes.");
 		
 		return new Genome(genes);
 	}
 	
 	Genome(ArrayList<Gene> genes){
 		this.genes = genes;
 		Collections.sort(genes);
 		this.chrMap = new HashMap<String, String>();
 		this.chrArmMap = new HashMap<String, String>();
 		this.chrBandMap = new HashMap<String, String>();
 		this.entrezMap = new HashMap<String, Integer>();
 		this.idxMap = new HashMap<String, Integer>();
 		this.coordMap = new HashMap<String, Float>();
 		this.chrIdxRangeMap = new HashMap<String, IntPair>();
 		int cnt = 0;
 		String preChr = null;
 		for(Gene g : genes){
 			String chr = g.chr;
 			if(chrIdxRangeMap.get(chr) == null){ // new chr coming up!
 				if(preChr != null){ // previous chr, push the end value
 					chrIdxRangeMap.get(preChr).setY(cnt);
 				}
 				chrIdxRangeMap.put(chr, new IntPair(cnt));
 				preChr = chr;
 				
 			}
 			entrezMap.put(g.name, g.entrez);
 			chrArmMap.put(g.name, g.chrArm);
 			chrBandMap.put(g.name, g.chrBand);
 			chrMap.put(g.name, chr);
 			idxMap.put(g.name, cnt);
 			coordMap.put(g.name, g.coord);
 			cnt++;
 		}
 		chrIdxRangeMap.get(preChr).setY(cnt);
 	}
 	public String[] getAllGenes(){
 		ArrayList<String> out = new ArrayList<String>();
 		for(Gene g : genes){
 			out.add(g.name);
 		}
 		return out.toArray(new String[0]);
 	}
 	public String[] getAllGenesInChr(String chr){
 		ArrayList<String> out = new ArrayList<String>();
 		for(Gene g: genes){
 			if (g.chr.equalsIgnoreCase(chr)){
 				out.add(g.name);
 			}
 		}
 		return out.toArray(new String[0]);
 	}
 	public String[] getAllGenesInChrArm(String chrArm){
 		ArrayList<String> out = new ArrayList<String>();
 		for(Gene g: genes){
 			if (g.chrArm.equals(chrArm)){
 				out.add(g.name);
 			}
 		}
 		return out.toArray(new String[0]);
 	}
 	
 	public String[] getAllGenesInChrBand(String chrBand){
 		ArrayList<String> out = new ArrayList<String>();
 		for(Gene g: genes){
 			if (g.chrBand.contains(chrBand)){
 				out.add(g.name);
 			}
 		}
 		return out.toArray(new String[0]);
 	}
 	public int getIdx(String gene){
 		return idxMap.get(gene);
 	}
 	
 	public float getCoord(String gene){
 		return coordMap.get(gene);
 	}
 	
 	public float getChrIdxRange(String chr){
 		IntPair ip = chrIdxRangeMap.get(chr);
 		return (ip.y - ip.x);
 	}
 	
 	public void linkToGeneSet(String[] inputgenes){
 		Arrays.sort(inputgenes);
 		int k = genes.size();
 		for(int i = k-1; i >= 0; i--){
 			String g = this.genes.get(i).name;
 			if(Arrays.binarySearch(inputgenes, g) < 0){
 				chrMap.remove(g);
 				genes.remove(i);
 			}
 		}
 		k = genes.size();
 		this.chrIdxRangeMap = new HashMap<String, IntPair>();
 		idxMap = new HashMap<String, Integer>();
 		String preChr = null;
 		for(int i = 0; i < k; i++){
 			Gene g = genes.get(i);
 			String chr = g.chr;
 			if(chrIdxRangeMap.get(chr) == null){ // new chr coming up!
 				if(preChr != null){ // previous chr, push the end value
 					chrIdxRangeMap.get(preChr).setY(i);
 				}
 				chrIdxRangeMap.put(chr, new IntPair(i));
 				preChr = chr;
 				
 			}
 			
 			chrMap.put(g.name, chr);
 			idxMap.put(g.name, i);
 			coordMap.put(g.name, g.coord);
 		}
 		chrIdxRangeMap.get(preChr).setY(k);
 		
 		/*for(String c : chrCoordRangeMap.keySet()){
 			System.out.println(c + "\t" + chrCoordRangeMap.get(c));
 		}*/
 		System.out.println(k + " genes linked.");
 	}
 	
 // filtering genes to those contained in the DataFile
 	public void linkToDataFile(DataFile ma){ 
 		ArrayList<String> maGenes = ma.getProbes();
 		int k = genes.size();
 		for(int i = k-1; i >= 0; i--){
 			String g = this.genes.get(i).name;
 			if(!maGenes.contains(g)){
 				chrMap.remove(g);
 				genes.remove(i);
 			}
 		}
 		k = genes.size();
 		this.chrIdxRangeMap = new HashMap<String, IntPair>();
 		idxMap = new HashMap<String, Integer>();
 		String preChr = null;
 		for(int i = 0; i < k; i++){
 			Gene g = genes.get(i);
 			String chr = g.chr;
 			if(chrIdxRangeMap.get(chr) == null){ // new chr coming up!
 				if(preChr != null){ // previous chr, push the end value
 					chrIdxRangeMap.get(preChr).setY(i);
 				}
 				chrIdxRangeMap.put(chr, new IntPair(i));
 				preChr = chr;
 				
 			}
 			
 			chrMap.put(g.name, chr);
 			idxMap.put(g.name, i);
 			coordMap.put(g.name, g.coord);
 		}
 		chrIdxRangeMap.get(preChr).setY(k);
 		
 		/*for(String c : chrCoordRangeMap.keySet()){
 			System.out.println(c + "\t" + chrCoordRangeMap.get(c));
 		}*/
 		System.out.println(k + " genes linked.");
 		
 	}
 	
 // return a gene set within the windowsize centered at the gene, -1 for the whole chromosome
 	public String[] getNeighbors(String gene, int windowsize){
 		int idx = genes.indexOf(new Gene(entrezMap.get(gene)));
 		String chr = getChr(gene);
 		ArrayList<String> outGenes = new ArrayList<String>();
 		IntPair ip = chrIdxRangeMap.get(chr);
 		if(windowsize == -1){
 			for(int i = ip.x; i < ip.y; i++){
 				outGenes.add(genes.get(i).name);
 			}
 		}else{
 			/*if(windowsize > (ip.y - ip.x)){
 				return null;
 			}*/
 			int start = Math.max(0, idx - windowsize/2);
 			int end = Math.min(genes.size()-1, idx + windowsize/2);
 			
 			//if(start < ip.x) {end = end + ip.x - start; start = ip.x; }
 			//if(end >= ip.y) {start = start - end + ip.y - 1; end = ip.y-1; }
			//System.out.println(idx + "\t" + start + "\t" + end);
 			for(int i = start; i <= end; i++){
 				outGenes.add(genes.get(i).name);
 			}
 		}
 		return outGenes.toArray(new String[0]);
 	}
 	
 	public String getChr(String gene){
 		return chrMap.get(gene);
 	}
 	public String getChrArm(String gene){
 		return chrArmMap.get(gene);
 	}
 	public String getChrBand(String gene){
 		return chrBandMap.get(gene);
 	}
 	public String getGene(int idx){
 		return genes.get(idx).name;
 	}
 	
 	public boolean contains(String gene){
 		return genes.contains(new Gene(entrezMap.get(gene)));
 	}
 	
 }
