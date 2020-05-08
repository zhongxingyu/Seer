 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.SwingWorker;
 
 import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentCategory;
 import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentLibrary;
 import edu.mssm.pharm.maayanlab.common.bio.EnrichedTerm;
 import edu.mssm.pharm.maayanlab.common.bio.FuzzyGeneSetLibrary;
 import edu.mssm.pharm.maayanlab.common.bio.GeneSetLibrary;
 import edu.mssm.pharm.maayanlab.common.core.FileUtils;
 import edu.mssm.pharm.maayanlab.common.core.Settings;
 import edu.mssm.pharm.maayanlab.common.core.SettingsChanger;
 import edu.mssm.pharm.maayanlab.common.core.SimpleXMLWriter;
 
 public class EnrichrBatcher implements SettingsChanger {
 	
 	private static Logger log = Logger.getLogger("maayanlab");
 
 	// progress tracking
 	private SwingWorker<Void, Void> task = null;
 	private int progress = 0;
 	private String note = "";
 	private boolean isCancelled = false;
 	
 	// Default settings
 	private final Settings settings = new Settings() {
 		{			
 			for (EnrichmentCategory category : ResourceLoader.getInstance().getCategories()) {
 				for (EnrichmentLibrary library : category.getLibraries()) {
 					set(library.getName(), true);
 				}
 			}
 		}
 	};
 	
 	// Formatter
 	private final DecimalFormat scientificNotation = new DecimalFormat("0.##E0");
 	
 	// Output header
 	public static final String HEADER = "Term\tOverlap\tP-value\tGenes"; 
 	
 	private HashMap<String, ArrayList<EnrichedTerm>> resultsMap = new HashMap<String, ArrayList<EnrichedTerm>>(18);
 	
 	public static void main(String[] args) {
 		// Set logger display level
 		if (!Boolean.getBoolean("verbose"))
             log.setLevel(Level.WARNING);
 		
 		if (args.length == 2) {
 			EnrichrBatcher eb = new EnrichrBatcher();
 			eb.run(args[0]);
 			eb.writeFile(args[1]);
 		}
 		else if (args.length == 3) {
 			EnrichrBatcher eb = new EnrichrBatcher();
 			eb.run(args[0], args[1], false, args[2]);
 		}
 		else if (args.length == 4) {
 			EnrichrBatcher eb = new EnrichrBatcher();
 			eb.run(args[0], args[1], Boolean.parseBoolean(args[2]), args[3]);
 		}
 		else
 			log.warning("Usage: java -jar Enrichr.jar gene_list [background_file is_fuzzy] output");
 	}
 	
 	// By default, load settings from file
 	public EnrichrBatcher() {
 		settings.loadSettings();
 	}
 	
 	// Load external settings, primarily for use with X2K
 	public EnrichrBatcher(Settings externalSettings) {
 		settings.loadSettings(externalSettings);
 	}
 	
 	// Task methods
 	public void setTask(SwingWorker<Void, Void> task) {
 		this.task = task;
 	}
 	
 	private void setProgress(int progress, String note) throws InterruptedException {
 		if (task != null) {
 			if (isCancelled)
 				throw new InterruptedException("Task cancelled at " + progress + "%!");
 			task.firePropertyChange("progress", this.progress, progress);
 			task.firePropertyChange("note", this.note, note);
 			this.progress = progress;
 			this.note = note;
 		}
 	}
 	
 	public void cancel() {
 		isCancelled = true;
 	}
 
 	@Override
 	// Used for other methods to set settings	
 	public void setSetting(String key, String value) {
 		settings.set(key, value);
 	}
 	
 	public HashMap<String, ArrayList<EnrichedTerm>> getEnrichmentResults() {
 		return resultsMap;
 	}
 	
 	public void writeFile(String filename) {
 		// Prefix for individual files
 		String outputPrefix = filename.replaceFirst("\\.\\w+$", "");
 		
 		SimpleXMLWriter sxw = new SimpleXMLWriter(filename);
 		sxw.startPlainElement("Enrichment");
 		
 		sxw.startPlainElement("Summary");
 		LinkedList<EnrichedTerm> combinedTerms = new LinkedList<EnrichedTerm>();
 		for (ArrayList<EnrichedTerm> termList : resultsMap.values())
 			combinedTerms.addAll(termList);
 		Collections.sort(combinedTerms);
 		
 		// Filter down to top 10
 		while (combinedTerms.size() > 10)
 			combinedTerms.removeLast();
 		
 		for (EnrichedTerm term : combinedTerms)
 			sxw.listElement("Term", term.getName(), "p-value", scientificNotation.format(term.getPValue()));
 		
 		sxw.endElement();
 		
 		for (String bgType : resultsMap.keySet()) {
 			// Write XML summary output
 			sxw.startElementWithAttributes("Background", "name", bgType);
 			int i = 1;
 			for (EnrichedTerm term : resultsMap.get(bgType)) {
 				sxw.listElement("Term",	term.getName(), "p-value", scientificNotation.format(term.getPValue()));
 				// Stop after 10 entries
 				if (i++ >= 10)
 					break;
 			}
 			sxw.endElement();
 					
 			// Write individual enrichment tsv outputs
 			FileUtils.writeFile(outputPrefix + "_" + bgType + ".txt", Enrichment.HEADER, resultsMap.get(bgType));
 		}
 		
 		sxw.close();
 	}
 	
 	// Run from cli with custom database
 	public void run(String geneList, String backgroundFile, boolean isFuzzy, String outputFile) {
 		GeneSetLibrary geneSetLibrary;
 		
 		log.info("Running with custom database");
 		ArrayList<String> inputList = FileUtils.readFile(geneList);
 		
 		try {
 			if (FileUtils.validateList(inputList)) {
 				Enrichment app = new Enrichment(FileUtils.readFile(geneList));
 				if (isFuzzy)
 					geneSetLibrary = new FuzzyGeneSetLibrary(FileUtils.readFile(backgroundFile));
 				else
 					geneSetLibrary = new GeneSetLibrary(FileUtils.readFile(backgroundFile));
 				FileUtils.writeFile(outputFile, Enrichment.HEADER, app.enrich(geneSetLibrary));
 			}
 		} catch (ParseException e) {
 			if (e.getErrorOffset() == -1)
 				log.warning("Invalid input: Input list is empty.");
 			else
 				log.warning("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
 			System.exit(-1);
 		}
 	}
 	
 	// Run for file names
 	public void run(String geneList) {
 		ArrayList<String> inputList = FileUtils.readFile(geneList);
 		
 		try {
			if (FileUtils.validateList(inputList))
				run(inputList);
 		} catch (ParseException e) {
 			if (e.getErrorOffset() == -1)
 				log.warning("Invalid input: Input list is empty.");
 			else
 				log.warning("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
 			System.exit(-1);
 		}
 	}
 	
 	// Run for calling from other methods and pass in collection
	public void run(Collection<String> geneList) {
 		LinkedList<String> bgList = new LinkedList<String>();
 		
 		for (EnrichmentCategory category : ResourceLoader.getInstance().getCategories()) {
 			for (EnrichmentLibrary library : category.getLibraries()) {
 				if (settings.getBoolean(library.getName())) {
 					bgList.add(library.getName());
 				}
 			}
 		}
 		
 		
 		try {
 			setProgress(0, "Enriching terms...");
 			computeEnrichment(bgList, geneList);
 			setProgress(95, "Writing results...");
 		} catch (InterruptedException e) {
 			log.info(e.getMessage());
 			return;
 		}
 	}
 
	public void computeEnrichment(LinkedList<String> backgroundList, Collection<String> geneList) throws InterruptedException {
 		int iteration = 0;
 		int increment = 80 / backgroundList.size();
 		
		Enrichment app = new Enrichment(geneList);
 		
 		for (String bgType : backgroundList) {
 			try {
 				setProgress(5+increment*iteration, bgType.replace("_", " ") + " enrichment...");
 				iteration++;
 			} catch (InterruptedException e) {
 				throw new InterruptedException(e.getMessage());
 			}
 			
 			ArrayList<EnrichedTerm> resultTerms = app.enrich(bgType); 
 			
 			// Only add to results if there are actual results
 			if (!resultTerms.isEmpty())
 				resultsMap.put(bgType, resultTerms);
 		}
 	}
 	
 }
