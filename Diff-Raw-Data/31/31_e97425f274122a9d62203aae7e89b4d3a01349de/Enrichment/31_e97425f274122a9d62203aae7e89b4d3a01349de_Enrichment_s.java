 /**
  * Performs enrichment on a gene list.
  * 
  * @author		Edward Y. Chen
  * @since		8/2/2012 
  */
 
 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 
 import edu.mssm.pharm.maayanlab.common.core.FileUtils;
 import edu.mssm.pharm.maayanlab.common.core.Settings;
 import edu.mssm.pharm.maayanlab.common.core.SettingsChanger;
 
 public class Enrichment implements SettingsChanger {
 	
 	// Default settings
 	private final Settings settings = new Settings() {
 		{
 			// String: rank the Enrichment by the Fisher Exact test's p-value, rank against the background of random genes, or combined score of the two. [combined score/p-value/rank]
 			set(Enrichment.SORT_BY, Enrichment.COMBINED_SCORE);
 		}
 	};
 	// Settings variables
 	public final static String SORT_BY = "sort enrichment by";
 	public final static String COMBINED_SCORE = "combined score";
 	public final static String PVALUE = "p-value";
 	public final static String RANK = "rank";
 	
 	public static final String HEADER = "Term\tOverlap\tP-value\tAdjusted P-value\tZ-score\tCombined Score\tGenes";
 	
 	private Collection<String> geneList; 
 	
 	public static void main(String[] args) {		
 		try {
 			Enrichment app = new Enrichment(FileUtils.readFile(args[1]), true);
 			FileUtils.writeFile(args[2], HEADER, app.enrich(args[0]));
 		} catch (ParseException e) {
 			if (e.getErrorOffset() == -1)
 				System.err.println("Invalid input: Input list is empty.");
 			else
 				System.err.println("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
 		}
 	}
 	
 	public Enrichment(Collection<String> geneList) {
 		this.geneList = geneList;
 	}
 	
 	public Enrichment(Collection<String> geneList, boolean validate) throws ParseException {
 		if (validate)	// Check if input list is valid
 			FileUtils.validateList(geneList);
 		this.geneList = geneList;
 	}
 	
 	@Override
 	public void setSetting(String key, String value) {
 		settings.set(key, value);
 	}
 	
 	public Collection<String> getInput() {
 		return geneList;
 	}
 	
 	public ArrayList<EnrichedTerm> enrich(String backgroundType) {
 		return enrich(backgroundType, true);
 	}
 	
 	public ArrayList<EnrichedTerm> enrich(String backgroundType, boolean useRanks) {
 		// Read background list and ranks
 		Collection<String> backgroundLines = FileUtils.readResource(backgroundType + ".gmt");
 		if (useRanks) {
 			Collection<String> rankLines = FileUtils.readResource(backgroundType + "_ranks.txt");
 			return enrich(new GeneSetLibrary(backgroundLines, rankLines));
 			
 		}
 		else {
 			return enrich(new GeneSetLibrary(backgroundLines));
 		}
 		
 	}
 	
 	public ArrayList<EnrichedTerm> enrich(GeneSetLibrary geneSetLibrary) {
 		HashSet<String> inputGenes = filterInputGenes(geneSetLibrary);
 		
 		ArrayList<EnrichedTerm> enrichedTerms = new ArrayList<EnrichedTerm>();
 		for (Term currentTerm : geneSetLibrary.getTerms()) {
 			EnrichedTerm enrichedTerm = currentTerm.getEnrichedTerm(inputGenes);
 								
 			if (enrichedTerm != null)
 				enrichedTerms.add(enrichedTerm);		
 		}
 		
 		sortEnrichedTerms(geneSetLibrary, enrichedTerms);
 		
 		return enrichedTerms;
 	}
 	
 	// Filter genes from the input list that are not in the gene-set library
 	private HashSet<String> filterInputGenes(GeneSetLibrary geneSetLibrary) {
 		HashSet<String> inputGenes = new HashSet<String>();
 		for (String gene : geneList) {
 			gene = gene.toUpperCase();
 			if (geneSetLibrary.contains(gene))
 				inputGenes.add(gene);
 		}
 		
 		return inputGenes;
 	}
 	
 	// Sort enriched terms
 	private void sortEnrichedTerms(GeneSetLibrary geneSetLibrary, ArrayList<EnrichedTerm> enrichedTerms) {
 		// Sort by p-value
 		Collections.sort(enrichedTerms);
 		
 		// Calculate adjusted p-value via Benjamini-Hochberg
 		double previousValue = 1;	// Prevent adjusted p-value to be > 1
 		for (int rank = enrichedTerms.size(); rank > 0; rank--) {
 			EnrichedTerm enrichedTerm = enrichedTerms.get(rank-1);
 			double adjustedPValue = Math.min(previousValue, enrichedTerm.getPValue() * enrichedTerms.size() / rank);	// Ensure monotonicity
 			previousValue = adjustedPValue;
 			enrichedTerm.setAdjustedPValue(adjustedPValue);
 			if (geneSetLibrary.isRanked())
 				enrichedTerm.computeScore(rank);			
 		}
 		
 		if (geneSetLibrary.isRanked() && settings.get(SORT_BY).equals(COMBINED_SCORE)) {
 			Collections.sort(enrichedTerms, new Comparator<EnrichedTerm>() {
 				@Override
 				public int compare(EnrichedTerm o1, EnrichedTerm o2) {
 					if (o1.getCombinedScore() < o2.getCombinedScore())				
 						return 1;
 					else if (o1.getCombinedScore() > o2.getCombinedScore())
 						return -1;
 					else
 						return 0;
 				}
 			});
 		}
 	}
 }
