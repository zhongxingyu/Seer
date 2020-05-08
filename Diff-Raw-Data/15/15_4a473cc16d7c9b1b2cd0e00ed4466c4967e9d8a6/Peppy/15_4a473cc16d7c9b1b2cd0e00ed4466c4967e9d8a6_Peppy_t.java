 package Peppy;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import Reports.HTMLReporter;
 import Reports.TextReporter;
 import Utilities.U;
 
 
 
 
 /**
  * Peppy
  * A very stripped down Java version of the MS/MS to genome mapping of Morgan Gidding's GFS.
  * Designed with the following goals:
  * 1) More simple code to promote open source development
  * 2) Takes advantage of Java's popularity over Objective-C
  * 3) better multi-threading
  * @author Brian Risk
  *
  */
 public class Peppy {
 	
 	//So that we may report the total amount of peptides found
 	static int peptideTally = 0;
 	
 	public static void main(String [] args) {
 		init(args);
		runJobs(args);
 //		new Peppy(args);
 //		exportPeptideList();
 		U.p("done");
 	}
 	
 	public Peppy(String [] args) {
 		U.startStopwatch();
 		printGreeting();
 		peptideTally = 0;
 		
 		//Load our spectra
 		U.p("loading spectra...");
 		ArrayList<Spectrum> spectra = Spectrum.loadSpectra();
 		U.p("loaded " +spectra.size() + " spectra.");
 		
 		//Get references to our sequence files -- no nucleotide data is loaded at this point
 		ArrayList<Sequence> sequences = Sequence.loadSequences(Properties.sequenceDirectoryOrFile);
 		
 		//initialize our ArrayList of matches
 		ArrayList<Match> matches = new ArrayList<Match>();
 		
 		if (Properties.useSpliceVariants) {
 			//gets the first nucleotide sequence in the first sequence file
 			Sequence sequenceFile = sequences.get(0);
 			RNA_Sequence rna = new RNA_Sequence(sequenceFile.getNucleotideSequences().get(0), Properties.sequenceRegionStart, Properties.sequenceRegionStop);
 			
 			U.p("digesting...");
 			RNA_Digestor rnaDigestor = new RNA_Digestor(rna);
 			ArrayList<Peptide> peptides  = rnaDigestor.getPeptides();
 			U.p("peptide tally: " + peptides.size());
 			
 			U.p("getting matches...");
 			//TODO get rid of this getMatches function when this is overhauled
 			matches = getMatches(peptides, spectra, sequenceFile);
 			
 		} else {	
 			matches.addAll(getMatches(sequences, spectra));
 			U.p("peptide tally: " + peptideTally);
 		}
 		
 		//create new report directory
 		File reportDir = new File(Properties.reportDirectory, Properties.reportDirectoryTitle + " " + System.currentTimeMillis());
 		if (Properties.isSequenceFileDNA && Properties.createHTMLReport) {
 			U.p("creating HTML reports");
 			HTMLReporter report = new HTMLReporter(matches, spectra, sequences, reportDir);
 			report.generateFullReport();
 		}
 		
 		U.p("creating text reports");
 		TextReporter textReport = new TextReporter(matches, spectra, sequences, reportDir);
 		textReport.generateFullReport();
 		textReport.generatePropertiesFile();
 		
 		U.p();
 		U.stopStopwatch();
 	}
 	
	public static void runJobs(String [] args) {
 		File jobsDir = new File("jobs");
 		File[] potentialJobsFiles = jobsDir.listFiles();
 		ArrayList<File> jobFiles = new ArrayList<File>();
 		if (potentialJobsFiles != null) {
 			for (int i = 0; i < potentialJobsFiles.length; i++) {
 				if (potentialJobsFiles[i].getName().toLowerCase().endsWith(".txt")) {
 					jobFiles.add(potentialJobsFiles[i]);
 				}	
 			}
 		}
 		if (jobFiles.size() == 0) {
 			U.p("no jobs in jobs folder.  running according to main properties file");
 			new Peppy(null);
 		} else {
 			U.p("running " + jobFiles.size() + " jobs");
 			for (int i = 0; i < jobFiles.size(); i++) {
 				U.p("running job " + (i + 1) + "; " + jobFiles.get(i).getName());
				init(args);
 				Properties.loadProperties(jobFiles.get(i));
 				new Peppy(null);
 			}
 		}
 	}
 
 
 	public static void init(String propertiesFile) {
 		Properties.loadProperties(propertiesFile);
 		if (Properties.defaultScore == Properties.DEFAULT_SCORE_HMM) {
 			HMMScore.HMMClass.HmmSetUp();
 		}
 	}
 	
 	
 	public static void init(String [] args) {
 		if (args.length == 0) {
 			init();
 		} else {
 			init(args[0]);
			U.p("Initializing with properties file: " + args[0]);
 		}
 	}
 	
 	public static void init() {
 		init("properties.txt");
 	}
 	
 	
 	/**
 	 * Assumes that we are doing the normal, forwards digestion of our sequences
 	 * @param sequences
 	 * @param spectra
 	 * @return
 	 */
 	public static ArrayList<Match> getMatches(ArrayList<Sequence> sequences, ArrayList<Spectrum> spectra) {
 		return getMatches(sequences, spectra, false);
 	}
 	
 	public static ArrayList<Match> getReverseMatches(ArrayList<Sequence> sequences, ArrayList<Spectrum> spectra) {
 		return getMatches(sequences, spectra, true);
 	}
 	
 	
 	/**
 	 * 
 	 * @param sequences our list of sequences where we will be getting our peptides
 	 * @param spectra our list of spectra
 	 * @param reverse if we are doing a normal, forwards search or if this is a null, reverse search
 	 * @return
 	 */
 	private static ArrayList<Match> getMatches(ArrayList<Sequence> sequences, ArrayList<Spectrum> spectra, boolean reverse) {
 		ArrayList<Match> matches = new ArrayList<Match>() ;
 		for (Sequence sequence: sequences) {
 			U.p("working on sequence: " +sequence.getSequenceFile().getName());
 			
 			ArrayList<Peptide> peptides;
 			if (Properties.isSequenceFileDNA) {
 				peptides = sequence.extractMorePeptides(reverse);
 				//continually extract peptides from the sequence until there aren't anymore
 				while (peptides != null) {
 					peptideTally += peptides.size();
 					//This is where the bulk of the processing in long jobs takes
 					ArrayList<Match> newMatches = (new ScoringThreadServer(peptides, spectra, sequence)).getMatches();
 					//Possible to add only matches with a decent e value
 					evaluateMatches(newMatches, matches);
 					//free up the memory of the old peptide arraylist
 					peptides.clear();
 					System.gc();
 					peptides = sequence.extractMorePeptides(reverse);
 				}
 				sequence.clearNucleotideData();
 				removeDuplicateMatches(matches);
 			} else {
 				peptides = ProteinDigestion.getPeptidesFromDatabase(sequence.getSequenceFile(), reverse);
 				peptideTally += peptides.size();
 				//This is where the bulk of the processing in long jobs takes
 				ArrayList<Match> newMatches = (new ScoringThreadServer(peptides, spectra, sequence)).getMatches();
 				//Add only matches with a decent e value
 				evaluateMatches(newMatches, matches);
 			}		
 		}
 		assignRankToMatches(matches);
 		assignRepeatedPeptideCount(matches);	
 		assignConfidenceValuesToMatches(matches);
 		return matches;
 	}
 	
 	/**
 	 * Gets matches where a list of peptides is already derived
 	 * @param peptides
 	 * @param spectra
 	 * @param sequence
 	 * @return
 	 */
 	public static ArrayList<Match> getMatches(ArrayList<Peptide> peptides, ArrayList<Spectrum> spectra, Sequence sequence) {
 		ArrayList<Match> matches = new ArrayList<Match>() ;
 		peptideTally += peptides.size();
 		
 		//This is where the bulk of the processing in long jobs takes
 		ArrayList<Match> newMatches = (new ScoringThreadServer(peptides, spectra, sequence)).getMatches();
 		
 		//Add only matches with a decent e value
 		evaluateMatches(newMatches, matches);
 		
 		if (Properties.isSequenceFileDNA) {
 			removeDuplicateMatches(matches);
 		}
 		assignRankToMatches(matches);
 		assignRepeatedPeptideCount(matches);
 		assignConfidenceValuesToMatches(matches);
 		
 		return matches;
 	}
 	
 		
 	public static void assignRankToMatches(ArrayList<Match> matches) {
 		//first error check
 		if (matches.size() == 0) return;
 		Match.setSortParameter(Match.SORT_BY_SPECTRUM_ID_THEN_SCORE);
 		Collections.sort(matches);
 		Match match = matches.get(0);
 		Match previousMatch = matches.get(0);
 		//set for the first
 		match.setRank(1);
 		int rank = 1;
 		for (int i = 1; i < matches.size(); i++) {
 			//see if these are matches for a different spectrum
 			match = matches.get(i);
 			if (match.getSpectrum().getId() != previousMatch.getSpectrum().getId()) {
 				rank = 1;
 			}
 			if (match.getScore() == previousMatch.getScore()) {
 				rank = previousMatch.getRank();
 			}
 			match.setRank(rank);
 			rank++;
 			previousMatch = match;
 		}
 		//Setting Score ratios for those with rank 1
 		int i = matches.size() - 1;
 		double previousScore = match.getScore();
 		for (; i >= 0; i--) {
 			match = matches.get(i);
 			if (match.getRank() == 1) {
 				match.setScoreRatio(match.getScore() / previousScore);
 			} else {
 				previousScore = match.getScore();
 			}
 		}
 	}
 	
 	/**
 	 * finds the number of times a certain amino acid is found for each spectrum 
 	 * @param matches
 	 */
 	public static void assignRepeatedPeptideCount(ArrayList<Match> matches) {
 		//first error check
 		if (matches.size() == 0) return;
 		Match.setSortParameter(Match.SORT_BY_SPECTRUM_ID_THEN_PEPTIDE);
 		Collections.sort(matches);
 		Match match;
 		Match previousMatch = matches.get(0);
 		int rankCount = 1;
 		for (int i = 1; i < matches.size(); i++) {
 			//see if these are matches for a different spectrum
 			match = matches.get(i);
 			if (match.getSpectrum().getId() != previousMatch.getSpectrum().getId()) {
 				for (int j = i - rankCount; j < i; j++) {
 					matches.get(j).setRepeatCount(rankCount);
 				}
 				rankCount = 1;
 			} else {
 				if (match.getPeptide().equals(previousMatch.getPeptide())) {
 					rankCount++;
 				} else {
 					for (int j = i - rankCount; j < i; j++) {
 						matches.get(j).setRepeatCount(rankCount);
 					}
 					rankCount = 1;
 				}
 			}
 			previousMatch = match;
 		}
 	}
 	
 	public static void removeDuplicateMatches(ArrayList<Match> matches) {
 		//first error check
 		if (matches.size() == 0) return;
 		
 		Match.setSortParameter(Match.SORT_BY_SPECTRUM_ID_THEN_PEPTIDE);
 		Collections.sort(matches);
 		int numberOfMatches = matches.size();
 		Match match;
 		Match previousMatch = matches.get(0);
 		int spectrumID;
 		int previousSpectrumID = previousMatch.getSpectrum().getId();
 		boolean areEqual;
 		for (int i = 1; i < numberOfMatches; i++) {
 			match = matches.get(i);
 			spectrumID = match.getSpectrum().getId();
 			areEqual = false;
 			if (match.getPeptide().isForward() && previousMatch.getPeptide().isForward()) {
 				if (match.equals(previousMatch) && match.getPeptide().getStartIndex() == previousMatch.getPeptide().getStartIndex() && spectrumID == previousSpectrumID) {
 					areEqual = true;
 					matches.remove(i);
 					i--;
 					numberOfMatches--;
 				}
 			}
 			if (!match.getPeptide().isForward() && !previousMatch.getPeptide().isForward()) {
 				if (match.equals(previousMatch) && match.getPeptide().getStartIndex() == previousMatch.getPeptide().getStartIndex() && spectrumID == previousSpectrumID) {
 					areEqual = true;
 					matches.remove(i);
 					i--;
 					numberOfMatches--;
 				}
 			}
 			if (!areEqual) {
 				previousMatch = match;
 				previousSpectrumID = spectrumID;
 			}
 		}
 	}
 	
 	public static void assignConfidenceValuesToMatches(ArrayList<Match> matches) {
 		for (Match match: matches) {
 			if (match.calculateEValue() < match.calculateIMP()) {
 				match.setEValue(Double.MAX_VALUE);
 			}
 		}
 	}
 	
 	/**
 	 * Evaluates newMatches, adds appropriate ones to matches
 	 * @param newMatches
 	 * @param matches
 	 */
 	public static void evaluateMatches(ArrayList<Match> newMatches, ArrayList<Match> matches) {	
 		if (Properties.useEValueCutOff) {
 			for (Match match: newMatches) {
 				//The match E value should be less than our cutoff
 				if (match.getEValue() <= Properties.eValueCutOff) {
 					//the match IMP value should always be less than its E value
 					if (match.calculateIMP() < match.getEValue()) {
 						matches.add(match);
 					}
 				}
 			}
 		} else {
 			matches.addAll(newMatches);
 		}
 	}
 
 	/**
 	 * saves a file of peptide information
 	 * @param peptides
 	 */
 	@SuppressWarnings("unused")
 	private static void exportPeptideList() {
 		U.p("exporting peptide list");
 		//Get references to our sequence files -- no nucleotide data is loaded at this point
 		ArrayList<Sequence> sequences = Sequence.loadSequences(Properties.sequenceDirectoryOrFile);
 		
 		try {
 			//loop through each sequence in the sequences ArrayList
 			File peptidesFolder = new File ("peptides");
 			peptidesFolder.mkdir();
 			for (Sequence sequence: sequences) {
 				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(peptidesFolder, sequence.getSequenceFile().getName()))));
 				
 				ArrayList<Peptide> peptides = null;
 				if (Properties.isSequenceFileDNA) {
 					peptides = sequence.extractAllPeptides(false);
 				} else {
 					peptides = ProteinDigestion.getPeptidesFromDatabase(sequence.getSequenceFile());
 				}
 				
 				U.p("number of peptides: " + peptides.size());
 
 				for (Peptide peptide: peptides) {
 					pw.println(peptide);
 				}
 				
 				peptides = null;
 				System.gc();
 				pw.flush();
 				pw.close();
 				U.p(sequence.getSequenceFile().getName() + " digested.");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void printGreeting() {
 		U.p("Welcome to Peppy");
 		U.p("Proteogenomic mapping software.");
 		U.p("Developed 2010 by the Giddings Lab");
 		U.p();
 	}
 	
 
 }
