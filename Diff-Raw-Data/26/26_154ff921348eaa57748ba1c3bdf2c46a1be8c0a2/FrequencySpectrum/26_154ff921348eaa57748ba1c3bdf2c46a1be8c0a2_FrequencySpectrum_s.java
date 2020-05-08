 package statistics.dna;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JFrame;
 
 import population.Locus;
 
 import dnaModels.DNASequence;
 
 import statistics.Collectible;
 import statistics.DefaultStatisticConfigurator;
 import statistics.NoHistoConfigTool;
 import statistics.Options;
 import statistics.Statistic;
 
 /**
  * A statistic to store the site frequency spectrum, based on the minor allele frequency. It maintains 
  * a list in which the ith element is the frequency of sites which have a minor allele frequency of i.
  * Hence the 1st element is the mean number of singletons observed, etc. 
  * @author brendan
  *
  */
 public class FrequencySpectrum extends DNAStatistic {
 
 	public static final String identifier = "Site frequency spectrum";
 	
 	ArrayList<Double> spectrum = new ArrayList<Double>();
 
 	int calls = 0;
 	int dnaLength=1;
 	
 	public boolean collectDuringBurnin() {
 		return false;
 	}
 	
 	@Override
 	public void collect(Collectible pop) {
 		calls++;
 		
 		List<Locus> sample = pop.getSample(sampleSize);
 
 		int length = sample.get(0).getPrimaryDNA().length();
 		dnaLength = length;
 		for(int i=0; i<length; i++) {
 			int count = getMinorAlleleFreq(sample, i);
 			
 			while(spectrum.size() <= count)
 				spectrum.add(0.0);
 			if (count > 0)
 				spectrum.set(count, spectrum.get(count)+1);
 		}
 	}
 
 	
 	/**
 	 * True if a configuration tool is available for this statistic
 	 * @return
 	 */
 	public boolean hasConfigurationTool() {
 		return true;
 	}
 	
 	/**
 	 * Returns a GUI component that allows the user to configure some options of this statistic
 	 * @return
 	 */
 	public JFrame getConfigurationTool() {
 		return new NoHistoConfigTool(this);
 	}
 	
 	public void summarize(PrintStream out) {
 		out.println("Summary for Site Frequency Spectrum (fraction of times a mutation with minor allele frequency i was observed)");
 		out.println("Number of samples :" + calls);
 		double sum = 0;
 		for(int i=0; i<spectrum.size(); i++) {
 			out.println(i + "\t" + formatter.format(spectrum.get(i)/(double)calls));
 			sum += spectrum.get(i);
 		}
 		
 	}
 	
 	private int getMinorAlleleFreq(List<Locus> sample, int site) {
 		int[] counts = getBaseCounts(sample, site);
 		Arrays.sort(counts);
 		
 		//Return the second-greatest element. In cases where there are multiple segregating mutations at
 		//a single site, this favors the mutation with higher frequency... but hopefully this won't bias
 		//things too much
 		return counts[2];
 	}
 	
 	private int[] getBaseCounts(List<Locus> sample, int site) {
 		int[] counts = new int[4];
 		
 		for(int i=0; i<sample.size(); i++) {
 			switch( sample.get(i).getPrimaryDNA().getBaseChar(site) ) {
 			case 'A' : counts[0]++; break;
 			case 'G' : counts[1]++; break;
 			case 'C' : counts[2]++; break;
 			case 'T' : counts[3]++; break;
 			}
 		}
 	
 		return counts;
 	}
 	
 	@Override
 	public String getDescription() {
 		return "Distribution of mutation frequencies across sites";
 	}
 
 	@Override
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	@Override
 	public Statistic getNew(Options ops) {
 		return new FrequencySpectrum();
 	}
 
 }
