 package utils.ConservationImage;
 
 import java.util.List;
 
 import utils.ConservationImage.managers.GapManager;
 import utils.ConservationImage.managers.MoleculeManager;
 
 import fileformats.fastaIO.Pair;
 
 public class InformationProfiler extends Profiler {
 
 	@Override
 	public double[] getdata(List<Pair<String, String>> alin, MoleculeManager manager, GapManager gap) {
 	// From "Crooks, Gavin,E. et al, WebLogo: a A sequence Logo Generator. 2004"
 	// R_seq = S_max - S_obs = Log_2(N) - ( - SUM(n=1,N) p_n * Log_2(p_n) )
 
 
 	double S_max;
 	double[][] p;
 	double[] data;
 	int N;
 	int Len;
 	int n_seqs=0;
 
 	n_seqs = alin.size();
 	
 	Len = alin.get(0).getSecond().length();
 	N = manager.alphabetSize();
 	
 	p = new double[Len][N];
 	
 	S_max = Math.log(N)/Math.log(2);
 	
 	p = getFreq(p,alin,n_seqs, manager.alphabet()); 
 
 	data = new double[Len];
 	
 	for (int i = 0; i < data.length; i++) {
 		
 		double s_obs=0;
 		
 		double freqSum = 0; 
 
 		for (int j = 0; j < N; j++) {
 			
 			if (p[i][j]!= 0 ) { 
 				
 				s_obs = s_obs +  p[i][j] * Math.log(p[i][j]) / Math.log(2);
 				
 				freqSum = gap.attempToSumFreq(freqSum, p[i][j]) ;
 				
 			}
 		}
 		
 		data[i] = (S_max + s_obs) * freqSum / S_max;
 	}
 
 	return data;
 }	
 
 
 private double[][] 			getFreq								(double[][] p ,List<Pair<String, String>> alin, int n_seqs, String chars) {
 
 	// Initialize array
 	
 	for (int i=0;i<p.length;i++) {
 		for (int j = 0; j < p[i].length; j++) {
 			p[i][j]=0;
 		} 
 	}
 	
 	// count characters in each position
 	
 	for (int i = 0; i<p.length; i++) {
 		//iterate over each position
 		
 		for (int j = 0; j < n_seqs; j++) {
 			//iterate over each sequence
 
 			char c = Character.toUpperCase(alin.get(j).getSecond().charAt(i));
 			
 			try {
 				p[i][chars.lastIndexOf(c)]++;	
 			} catch (Exception e) {
				if (c!='-') System.err.println("el caracter es: " +  c + " y el índice es: " + chars.lastIndexOf(c));
 			}
 			
 		} 
 		
 	}
 	
 	// calculate frequencies
 	
 	for (int i=0;i<p.length;i++) {
 		for (int j = 0; j < p[i].length; j++) {
 			p[i][j]=p[i][j]/n_seqs;
 		} 
 	}
 			
 	return p;
 }
 
 }
