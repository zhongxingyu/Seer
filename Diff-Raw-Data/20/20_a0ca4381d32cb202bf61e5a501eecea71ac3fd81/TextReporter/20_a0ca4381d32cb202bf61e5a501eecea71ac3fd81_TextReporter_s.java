 package Reports;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import Peppy.Match;
 import Peppy.Properties;
 import Peppy.Sequence;
 import Peppy.Spectrum;
 import Utilities.U;
 
 
 /**
  * Okay, so you've got all of your results.  Now what?
  * I'll tell you now what.  You want to see them presented in
  * a nice, easy and intuitive manner.  That's what this
  * class does.
  * @author Brian Risk
  *
  */
 public class TextReporter {
 	
 	ArrayList<Match> matches;
 	ArrayList<Spectrum> spectra;
 	ArrayList<Sequence> sequences;
 	File reportDir;
 	
 	
 	/**
 	 * @param matches
 	 * @param spectra
 	 * @param sequences
 	 */
 	public TextReporter(ArrayList<Match> matches,
 			ArrayList<Spectrum> spectra, ArrayList<Sequence> sequences, File reportDir) {
 		this.matches = matches;
 		this.spectra = spectra;
 		this.sequences = sequences;
 		this.reportDir = reportDir;
 	}
 
 	public void generatePropertiesFile() {	
 		reportDir.mkdirs();
 		//set up our main index file
 		File paramFile = new File(reportDir, Properties.spectraDirectoryOrFile.getName()+".Parameter");
 		try {	
 			
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(paramFile)));
 
 			StringBuffer sb;
 			//print header
 			sb = new StringBuffer();
 			sb.append("Spectrum FileName or Directory");
 			sb.append('\t');
 			sb.append(Properties.spectraDirectoryOrFile.getName());
 			pw.println(sb);
 			sb = new StringBuffer();
 			sb.append("Sequence FileName or Directory");
 			sb.append('\t');
 			sb.append(Properties.sequenceDirectoryOrFile.getName());
 			pw.println(sb);
 			sb = new StringBuffer();
 			sb.append("Scoring Uyetm Used");
 			sb.append('\t');
 			sb.append(Properties.defaultScore);
 			pw.println(sb);
 			sb = new StringBuffer();
 			sb.append("Number of Missed Cleavages");
 			sb.append('\t');
 			sb.append(Properties.numberOfMissedCleavages);
 			pw.println(sb);
 			sb = new StringBuffer();
 			sb.append("Precursor Mass Threshold");
 			sb.append('\t');
 			sb.append(Properties.spectrumToPeptideMassError);
 			pw.println(sb);
 			sb = new StringBuffer();
 			sb.append("MS/MS Mass Threshold");
 			sb.append('\t');
 			sb.append(Properties.peakDifferenceThreshold);
 			pw.println(sb);
 			sb = new StringBuffer();
 			if (Properties.useEValueCutOff) {
 				sb = new StringBuffer();
 				sb.append("EValue Cutoff Used");
 				sb.append('\t');
 				sb.append(Properties.eValueCutOff);
 				pw.println(sb);
 			}
 			pw.flush();
 			pw.close();
 
 			
 	} catch (FileNotFoundException e) {
 			U.p("could not find file: " + paramFile.getName());
 			e.printStackTrace();
 		} catch (IOException e) {
 			U.p("could not read file: " + paramFile.getName());
 			e.printStackTrace();
 		}
 			
 	}
 	
 	public void generateFullReport() {	
 		reportDir.mkdirs();
 		//set up our main index file
 		File reportFile = new File(reportDir, Properties.spectraDirectoryOrFile.getName() + ".txt");
 		try {	
 			
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(reportFile)));
 			
 			//CHANGE THIS WITH EACH ADJUSTMENT TO FILE FORMAT
 			pw.println("format version 5");
 			
 			if (Properties.isSequenceFileDNA) {
 				pw.println("> analysis-type: nucleotide");
 			} else {
 				pw.println("> analysis-type: protein");
 			}
 			if (Properties.defaultScore == Properties.DEFAULT_SCORE_HMM) pw.println("> scoring method: HMM_Score");
 			if (Properties.defaultScore == Properties.DEFAULT_SCORE_TANDEM_FIT) pw.println("> scoring method: TandemFit");
 			
 			//sorting our matches by spectrum then score
 			Match.setSortParameter(Match.SORT_BY_E_VALUE);
 			Collections.sort(matches);
 			
 			
 			StringBuffer sb;
 			//print header
 			sb = new StringBuffer();
 			sb.append("Spectrum ID");
 			sb.append('\t');
 			sb.append("MD5");
 			sb.append('\t');
 			sb.append("File Name");
 			sb.append('\t');
 			sb.append("Score");
 			sb.append('\t');
 			sb.append("Precursor M/Z");
 			sb.append('\t');
 			sb.append("Precursor Neutral Mass");
 			sb.append('\t');
 			sb.append("E Value");
 			sb.append('\t');
 			sb.append("Peptide Sequence");
 			sb.append('\t');
 			if (Peppy.Properties.isSequenceFileDNA) {
 				sb.append("Sequence File");
 				sb.append('\t');
 				sb.append("START");
 				sb.append('\t');
 				sb.append("STOP");
 				sb.append('\t');
 				sb.append("INTRON-START");
 				sb.append('\t');
 				sb.append("INTRON-STOP");
 				sb.append('\t');
 				sb.append("Strand");
 				sb.append('\t');
 				sb.append("Is Spliced");
 			} else {
 				sb.append("Protein Name");
 			}
 			sb.append('\t');
 			sb.append("Match Rank");
 			sb.append('\t');
 			sb.append("Rank Count");
 			sb.append('\t');
 			sb.append("Ion Count");	
 			pw.println(sb);		
 			
 			//print rows
 			for (Match match: matches) {;
				if (Properties.useEValueCutOff && match.getEValue() <= Properties.eValueCutOff) {
 					sb = new StringBuffer();
 					sb.append(match.getSpectrum().getId());
 					sb.append('\t');
 					sb.append(match.getSpectrum().getMD5());
 					sb.append('\t');
 					sb.append(match.getSpectrum().getFile().getName());
 					sb.append('\t');
 					sb.append(match.getScore());
 					sb.append('\t');
 					sb.append(match.getSpectrum().getPrecursorMZ());
 					sb.append('\t');
 					sb.append(match.getSpectrum().getPrecursorMass());
 					sb.append('\t');
 					sb.append(match.getEValue());
 					sb.append('\t');
 					sb.append(match.getPeptide().getAcidSequenceString());
 					sb.append('\t');
 					if (Peppy.Properties.isSequenceFileDNA) {
 						sb.append(match.getSequence().getSequenceFile().getName());
 						sb.append('\t');
 						sb.append(match.getPeptide().getStartIndex());
 						sb.append('\t');
 						sb.append(match.getPeptide().getStopIndex());
 						sb.append('\t');
 						sb.append(match.getPeptide().getIntronStartIndex());
 						sb.append('\t');
 						sb.append(match.getPeptide().getIntronStopIndex());
 						sb.append('\t');
 						sb.append(match.getPeptide().isForward() ? "+" : "-");
 						sb.append('\t');
 						sb.append(match.getPeptide().isSpliced());
 					} else {
 						sb.append(match.getPeptide().getProtein().getName());
 					}
 					sb.append('\t');
 					sb.append(match.getRank());
 					sb.append('\t');
 					sb.append(match.getRankCount());
 					sb.append('\t');
 					sb.append(match.getIonMatchTally());
 					pw.println(sb);
 				}
 			}
 			
 
 			pw.flush();
 			pw.close();
 
 			
 		} catch (FileNotFoundException e) {
 			U.p("could not find file: " + reportFile.getName());
 			e.printStackTrace();
 		} catch (IOException e) {
 			U.p("could not read file: " + reportFile.getName());
 			e.printStackTrace();
 		}
 	}
 	
 	
 
 }
