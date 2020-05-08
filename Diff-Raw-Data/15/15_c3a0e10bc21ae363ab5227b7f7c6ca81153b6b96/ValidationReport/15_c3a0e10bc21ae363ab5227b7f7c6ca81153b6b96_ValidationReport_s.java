 	package Validate;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.imageio.ImageIO;
 
 import Math.MathFunctions;
 import Peppy.Match;
 import Peppy.MatchConstructor;
 import Peppy.Peptide;
 import Peppy.Properties;
import Peppy.Sequence_DNA;
 import Peppy.Sequence_Protein;
 import Reports.ReportStrings;
 import Utilities.U;
 
 public class ValidationReport {
 	
 	public static ArrayList<TestSet> tests;
 	public static File databaseFile = new File("");
 	public static File reportFolder;
 	public static PrintWriter indexWriter;
 	public static boolean doForwards = true;
 	public static boolean doReverse = false;
 	public static int forwardsDatabaseSize = 0;
 	public static int reverseDatabaseSize = 0;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {	
 		setUp();
 		addTests();
 		U.startStopwatch();
 		if (doForwards) forwards();
 		if (doReverse) reverse();
 		createReport();
 		U.stopStopwatch();
 		U.p("done.");
 		
 //		createResultsFiles();
 //		makeOnlyWrongReport();
 //		compareEValueReports();
 	}
 	
 	/**
 	 * If a test set has been previously generated and loaded
 	 * @param testSet
 	 */
 	public ValidationReport(ArrayList<TestSet> testSets) {
 		tests = testSets;
 		setUp();
 	}
 	
 	private static void setUp() {
 		System.setProperty("java.awt.headless", "true"); 
 		//Hello, world!
 		U.p("Are you ready for the food ball?  I mean: football.  I mean:  validation report");
 		reportFolder =  new File(Properties.validationDirectory, "" + System.currentTimeMillis() + "/");
 		reportFolder.mkdirs();
 		File indexFile = new File(reportFolder, "index.html");
 		try {
 			indexWriter = new PrintWriter(new BufferedWriter(new FileWriter(indexFile)));
 		} catch (IOException e) {
 			U.p("ERROR: Could not create file writer for our report");
 			e.printStackTrace();
 		}
 		
 		//how many missed cleavages when we digest
 		Properties.numberOfMissedCleavages = 1;
 		
 		Properties.maximumNumberOfMatchesForASpectrum = 1;
 		
 		//we'd prefer not to have duplicate matches -- especially for the correct ones
 		Properties.reduceDuplicateMatches = true;
 		
 		//What scoring mechanism?
 //		Properties.scoringMethodName = "Peppy.Match_Fake";
 		Properties.scoringMethodName = "Peppy.Match_IMP";
 //		Properties.scoringMethodName = "Peppy.Match_TandemFit";
 		Properties.matchConstructor = new MatchConstructor(Properties.scoringMethodName);
 		
 		//uncomment to use HMM score
 //		Properties.scoringMethodName = "Peppy.Match_HMM";
 //		Properties.matchConstructor = new MatchConstructor(Properties.scoringMethodName);
 //		HMMScore.HMMClass.HmmSetUp();
 //		Properties.highIntensityCleaning = true;
 //		Properties.peakDifferenceThreshold = 0.5;
 
 		databaseFile = new File("/Users/risk2/PeppyOverflow/tests/databases/uniprot_sprot.fasta");
 //		databaseFile = new File("/Users/risk2/PeppyOverflow/tests/databases/uniprot_sprot 2011_04.fasta");
 //		Properties.spectrumToPeptideMassError = 0.01;
 		Properties.spectrumToPeptideMassError = 2;
 		Properties.peakDifferenceThreshold = 0.3;
 		
 	}
 	
 	private static void addTests() {
 		//set up which tests we will perform
 		String testDirectoryName = "/Users/risk2/PeppyOverflow/tests/";
 //		String testDirectoryName = "tests/";
 		tests = new ArrayList<TestSet>();
 		tests.add(new TestSet(testDirectoryName, "ecoli", Color.RED));
 		tests.add(new TestSet(testDirectoryName, "human", Color.BLUE));
 		tests.add(new TestSet(testDirectoryName, "aurum", Color.GREEN));	
 //		tests.add(new TestSet(testDirectoryName, "USP", Color.DARK_GRAY));
 		tests.add(new TestSet(testDirectoryName, "USP top 10", Color.DARK_GRAY));
 //		Properties.isSequenceFileDNA = true;
 //		Sequence ecoli = new Sequence("/Users/risk2/PeppyOverflow/sequences ecoli/ecoli.fasta");
 //		ArrayList<Peptide> peptides = ecoli.extractAllPeptides(false);
 //		U.p("forwards database size: " + peptides.size());
 //		forwardsDatabaseSize = peptides.size();
 	}
 	
 	
 	/**
 	 * Completes a search on all test sets in our list using the correct (forward) digestion of our protein database.
 	 */
 	public static void forwards() {
 
 		Sequence_Protein sequence = new Sequence_Protein(databaseFile);	
 //		Sequence_DNA sequence = new Sequence_DNA(new File("/Users/risk2/PeppyOverflow/sequences ecoli/ecoli.fasta"));	
 		
 		ArrayList<Peptide> peptides = sequence.extractMorePeptides(false);
 		forwardsDatabaseSize += peptides.size();
 
 		while (peptides.size() > 0) {
 			for (TestSet test: tests) {
 				U.p("Getting matches for: " + test.getName());
 				test.findPositiveMatches(peptides);
 			}	
 			peptides.clear();
 			System.gc();
 			peptides = sequence.extractMorePeptides(false);
 			
 			if (peptides == null) {
 				break;
 			} else {
 				forwardsDatabaseSize += peptides.size();
 			}
 		}
 		
 		for (TestSet test: tests) {
 			test.cleanMatches();
 			test.calculateStastics();
 		}
 		
 	}
 	
 	/**
 	 * Get the reverse of the database.  Should produce a database of about the same size but
 	 * with, most likely, nearly no correct matches.
 	 */
 	public static void reverse() {
 		//We only want one match per spectrum
 		Properties.maximumNumberOfMatchesForASpectrum = 1;
 		
 		Sequence_Protein peptideDatabase = new Sequence_Protein(databaseFile);
 //		Sequence_DNA peptideDatabase = new Sequence_DNA(new File("/Users/risk2/PeppyOverflow/sequences ecoli/ecoli.fasta"));	
 		ArrayList<Peptide> peptides = peptideDatabase.extractAllPeptides(true);
 		reverseDatabaseSize = peptides.size();
 		
 		for (TestSet test: tests) {
 			U.p("Getting false matches for: " + test.getName());
 			test.findFalsePositiveMatches(peptides);
 		}		
 	}
 	
 	
 	
 	public static void createReport() {
 		U.p("Data collected, now generating the report...");
 
 		try {
 			File indexFile = new File(reportFolder, "index.html");
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(indexFile)));
 			NumberFormat nfPercent = NumberFormat.getPercentInstance();
 			nfPercent.setMaximumFractionDigits(2);
 			
 			pw.println("<html>");
 			pw.println("<body>");
 			pw.println("<h1>Validation Report</h1>");
 			
 			pw.println("<br>");
 			String scoringMethod = Properties.scoringMethodName;
 			pw.println("Scoring method: " + scoringMethod);
 			
 			pw.println("<br>");
 			pw.println("Database: " + databaseFile.getName());
 			
 			if (doForwards) {
 				pw.println("<br>");
 				pw.println("Forwards Database Size: " + forwardsDatabaseSize);
 			}
 			
 			if (doReverse) {
 				pw.println("<br>");
 				pw.println("Reverse Database Size: " + reverseDatabaseSize);
 			}
 			
 			pw.println("<h2>Basic performance metrics</h2>");
 			pw.println("<table border=1>");
 			
 			
 			//headers
 			pw.println("<tr>");
 			pw.println("<td>Metric</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<th>" + testSet.getName() + "</th>");
 			}
 			
 			//# spectra in the set
 			pw.println("<tr>");
 			pw.println("<td># spectra in the set</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getSetSize() + "</td>");
 			}
 			
 			//average number of peaks per spectrum
 			pw.println("<tr>");
 			pw.println("<td>Average # of peaks / spectrum</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getAverageNumberOfPeaksPerSpectrum() + "</td>");
 			}
 			
 			//Milliseconds per spectrum
 			pw.println("<tr>");
 			pw.println("<td>Milliseconds per spectrum</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getMilisecondsPerSpectrum() + "</td>");
 			}
 			
 			
 			//# of correct TPs
 			pw.println("<tr>");
 			pw.println("<td># of true positives</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getTrueTally() + "</td>");
 			}
 			
 			//% of correct TPs
 			pw.println("<tr>");
 			pw.println("<td>% of true positives</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + (nfPercent.format((double) testSet.getTrueTally() / testSet.getSetSize())) + "</td>");
 			}
 			
 			//# found at 1% FPR
 			pw.println("<tr>");
 			pw.println("<td># found at 5% FPR</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getTrueTallyAtFivePercentError() + 
 						" (" + nfPercent.format(testSet.getPercentAtFivePercentError()) + ")</td>");
 			}
 			
 //			//% of total found at 1% FPR
 //			pw.println("<tr>");
 //			pw.println("<td>% of total found at 1% FPR</td>");
 //			for (TestSet testSet: tests) {
 //				pw.println("<td>" + nfPercent.format(testSet.getPercentAtFivePercentError()) + "</td>");
 //			}
 //			//E value at 1% FPR
 //			pw.println("<tr>");
 //			pw.println("<td>E value at 1% FPR</td>");
 //			for (TestSet testSet: tests) {
 //				pw.println("<td>" + testSet.getEValueAtFivePercentError() + "</td>");
 //			}
 			
 			//PR Curve
 			pw.println("<tr>");
 			pw.println("<td>Precision-recall curve</td>");
 			for (TestSet testSet: tests) {
 				BufferedImage bufferedImage = testSet.generatePrecisionRecallCurve();
 				File testDirectory = new File(reportFolder, testSet.getName());
 				testDirectory.mkdirs();
 				File imageFile = new File(testDirectory, "/precision-recall.jpg");
 				String fileNameForPRCurve = testSet.getName() + "/" + imageFile.getName();
 				ImageIO.write(bufferedImage,"JPG",imageFile);
 				pw.println("<td><img src=\"" + fileNameForPRCurve + "\" width=200></td>");
 			}
 			
 			//Area under PR Curve
 			pw.println("<tr>");
 			pw.println("<td>Area under PR Curve</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + nfPercent.format(testSet.getAreaUnderPRCurve()) + "</td>");
 			}
 			
 			pw.println("</table>");
 			
 			
 
 			pw.println("<h2>E value distribution for forwards and reverse database search</h2>");
 			pw.println("<h3>Forwards</h3>");
 			pw.println("<table border=1>");
 			
 			pw.println("<tr>");
 			pw.println("<td>E value marking top 1% (forwards)</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getEValueAtPercentForwards(0.01) + "</td>");
 			}
 			
 			pw.println("<tr>");
 			pw.println("<td>E value marking top 5% (forwards)</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getEValueAtPercentForwards(0.05) + "</td>");
 			}
 			pw.println("<tr>");
 			pw.println("<td>E value marking top 25% (forwards)</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getEValueAtPercentForwards(0.25) + "</td>");
 			}
 			pw.println("<tr>");
 			pw.println("<td>E value marking top 50% (forwards)</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getEValueAtPercentForwards(0.50) + "</td>");
 			}
 			pw.println("<tr>");
 			pw.println("<td>E value marking top 75% (forwards)</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getEValueAtPercentForwards(0.75) + "</td>");
 			}
 			pw.println("<tr>");
 			pw.println("<td>E value marking top 95% (forwards)</td>");
 			for (TestSet testSet: tests) {
 				pw.println("<td>" + testSet.getEValueAtPercentForwards(0.95) + "</td>");
 			}
 			pw.println("</table>");
 			
 			//REVERSE
 			if (doReverse) {
 				pw.println("<h3>Reverse</h3>");
 				pw.println("<table border=1>");
 				pw.println("<tr>");
 				pw.println("<td>E value marking top 1% (reverse)</td>");
 				for (TestSet testSet: tests) {
 					pw.println("<td>" + testSet.getEValueAtPercentReverse(0.01) + "</td>");
 				}
 				
 				pw.println("<tr>");
 				pw.println("<td>E value marking top 5% (reverse)</td>");
 				for (TestSet testSet: tests) {
 					pw.println("<td>" + testSet.getEValueAtPercentReverse(0.05) + "</td>");
 				}
 				pw.println("<tr>");
 				pw.println("<td>E value marking top 25% (reverse)</td>");
 				for (TestSet testSet: tests) {
 					pw.println("<td>" + testSet.getEValueAtPercentReverse(0.25) + "</td>");
 				}
 				pw.println("<tr>");
 				pw.println("<td>E value marking top 50% (reverse)</td>");
 				for (TestSet testSet: tests) {
 					pw.println("<td>" + testSet.getEValueAtPercentReverse(0.50) + "</td>");
 				}
 				pw.println("<tr>");
 				pw.println("<td>E value marking top 75% (reverse)</td>");
 				for (TestSet testSet: tests) {
 					pw.println("<td>" + testSet.getEValueAtPercentReverse(0.75) + "</td>");
 				}
 				pw.println("<tr>");
 				pw.println("<td>E value marking top 95% (reverse)</td>");
 				for (TestSet testSet: tests) {
 					pw.println("<td>" + testSet.getEValueAtPercentReverse(0.95) + "</td>");
 				}
 				pw.println("</table>");
 			}
 
 
 			pw.println("</table>");
 
 			pw.println("</body>");
 			pw.println("</html>");
 			
 			pw.flush();
 			pw.close();
 			
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static void createResultsFiles() {
 		for (TestSet test: tests) {
 			try {
 				File indexFile = new File(reportFolder, test.getName() + ".txt");
 				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(indexFile)));
 				ArrayList<MatchContainer> topForwardsTestedMatches = test.getTopForwardsTestedMatches();
 				U.p("number of topForwardsTestedMatches: " + topForwardsTestedMatches.size());
 				Match.setSortParameter(Match.SORT_BY_SPECTRUM_ID_THEN_PEPTIDE);
 				Collections.sort(topForwardsTestedMatches);
 				for (MatchContainer match: topForwardsTestedMatches) {
 					StringBuffer sb = new StringBuffer();
 					sb.append(match.getMatch().getSpectrum().getId());
 					sb.append('\t');
 					sb.append(match.isTrue());
 					sb.append('\t');
 					sb.append(match.getEValue());
 					pw.println(sb.toString());
 				}
 				pw.flush();
 				pw.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * This is for the very specific task of comparing e value reports to see
 	 * on average how many orders of magnitude e values are shifted when changes
 	 * to the e value computation method are made
 	 * 
 	 * This code is not used in the proper report creation process
 	 */
 	public static void compareEValueReports() {
 		String smooth0ReportsName = "evalue report smoothing 0";
 		String smooth4ReportsName = "evalue report smoothing 4";
 		File smooth0Dir = new File(Properties.validationDirectory, smooth0ReportsName);
 		File smooth4Dir = new File(Properties.validationDirectory, smooth4ReportsName);
 		File [] smooth0Files = smooth0Dir.listFiles();
 		double trueDiffTotal = 0;
 		int trueSampleSize = 0;
 		double falseDiffTotal = 0;
 		int falseSampleSize = 0;
 		for (int i = 0; i < smooth0Files.length; i++) {
 			try {
 				File smooth0File = smooth0Files[i];
 				if (!smooth0File.getName().endsWith(".txt")) continue;
 				BufferedReader smooth0BR = new BufferedReader(new FileReader(smooth0File));
 				File smooth4File = new File(smooth4Dir, smooth0File.getName());
 				BufferedReader smooth4BR = new BufferedReader(new FileReader(smooth4File));
 				String smooth0Line = smooth0BR.readLine();
 				String smooth4Line = smooth4BR.readLine();
 				while (smooth0Line != null) {
 					String [] smooth0Chunks = smooth0Line.split("\t");
 					String [] smooth4Chunks = smooth4Line.split("\t");
 					boolean isTrue = Boolean.parseBoolean(smooth0Chunks[1]);
 					double smooth0EValue = Double.parseDouble(smooth0Chunks[2]);
 					double smooth4EValue = Double.parseDouble(smooth4Chunks[2]);
 					if (isTrue) {
 						trueDiffTotal += (Math.log10(smooth4EValue) - Math.log10(smooth0EValue));
 						trueSampleSize++;
 					} else {
 						falseDiffTotal += (Math.log10(smooth4EValue) - Math.log10(smooth0EValue));
 						falseSampleSize++;
 					}
 					smooth0Line = smooth0BR.readLine();
 					smooth4Line = smooth4BR.readLine();
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		U.p("Average magnitude change for true: " + (trueDiffTotal / trueSampleSize));
 		U.p("Average magnitude change for false: " + (falseDiffTotal / falseSampleSize));
 		U.p("Average magnitude change for total: " + ((falseDiffTotal + trueDiffTotal)/ (falseSampleSize + trueSampleSize)));
 	}
 	
 	
 	/**
 	 * Used to validate our test set.  We want visualizations of
 	 * what the "wrong" peptide aligned with the spectrum compared
 	 * to the "right" one.
 	 */
 	public static void makeOnlyWrongReport() {
 		U.p("wrong report");
 		Sequence_Protein sequence = new Sequence_Protein(databaseFile);
 		ArrayList<Peptide> peptides = sequence.extractAllPeptides(false);
 		
 		U.p("forwards database size: " + peptides.size());
 
 		//set up which tests we will perform
 		tests = new ArrayList<TestSet>();
 		String testDirectoryName = "/Users/risk2/PeppyOverflow/tests/";
 		tests.add(new TestSet(testDirectoryName, "ecoli", Color.red));
 //		tests.add(new TestSet(testDirectoryName,"human"));
 //		tests.add(new TestSet(testDirectoryName, "aurum"));	
 //		tests.add(new TestSet(testDirectoryName, "USP"));
 		
 		while (peptides.size() > 0) {
 			for (TestSet test: tests) {
 				test.findPositiveMatches(peptides);
 			}	
 			peptides = sequence.extractMorePeptides(false);
 			if (peptides == null) break;
 		}
 		
 		for (TestSet test: tests) {
 			test.cleanMatches();
 			test.calculateStastics();
 			generateWrongReport(test);
 		}
 	}
 
 	public static void generateRightReport(TestSet test) {
 		String testName = test.getName();
 		ArrayList<MatchContainer> testedMatches = test.getTestedMatches();
 		File testFileFolder = new File(reportFolder, testName);
 		testFileFolder.mkdirs();
 		File testFile = new File(testFileFolder, "right.html");
 		try {
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(testFile)));
 			
 			//print the header of the file
 			pw.println("<html><body><table>");
 			pw.println("<tr><th>acid</th><th>e value</th><th>score</th>");
 			
 			for (MatchContainer matchContainer: testedMatches) {
 				if (matchContainer.isTrue()) {
 					Match ourMatch = matchContainer.getMatch();
 					
 					pw.println("<tr>");
 					
 					pw.println("<td>");
 					pw.println(ourMatch.getPeptide().getAcidSequenceString());
 					pw.println("</td>");
 					
 					pw.println("<td>");
 					pw.println(ourMatch.getEValue());
 					pw.println("</td>");
 					
 					pw.println("<td>");
 					pw.println(ourMatch.getScore());
 					pw.println("</td>");
 					
 					pw.println("</tr>");
 					pw.println();
 					
 				}
 			}
 			
 			//print the footer and close out
 			pw.println("</table></body></html>");
 			pw.flush();
 			pw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void generateWrongReport(TestSet test) {
 		String testName = test.getName();
 		ArrayList<MatchContainer> testedMatches = test.getTopForwardsTestedMatches();
 		U.p("printing out all the spectra that we got wrong and comparing the two different matches");
 
 		File testFileFolder = new File(reportFolder, testName);
 		testFileFolder.mkdirs();
 		File testFile = new File(testFileFolder, "wrong.html");
 		File imagesFolder = new File(testFileFolder, "wrongImages");
 		imagesFolder.mkdir();
 		try {
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(testFile)));
 			
 			//print the header of the file
 			pw.println(ReportStrings.getHeader());
 			
 			pw.println("number of matches: " + testedMatches.size());
 			pw.println("<table border=1 cellspacing=0>");
 //			pw.println("<th>isTrue</th>");
 			pw.println("<th>E value</th>");
 			pw.println("<th>name</th>");
 			pw.println("<th>ions</th>");
 			pw.println("<th>our sequence</th>");
 			pw.println("<th>our spectrum</th>");
 			
 			int imageIndex = 0;
 			for (MatchContainer matchContainer: testedMatches) {
 				Match ourMatch = matchContainer.getMatch();
 				Match trueMatch = matchContainer.getTrueMatch();
 				if (!matchContainer.isTrue()) {
 					
 					pw.println("<tr>");
 					
 
 					pw.println("<td>");
 					pw.println(ourMatch.getEValue());
 					pw.println("</td>");
 					pw.println("<td>");
 					pw.println(ourMatch.getSpectrum().getFile().getName());
 					pw.println("</td>");
 	
 
 					pw.println("<td>");
 					pw.println(ourMatch.getIonMatchTally());
 					pw.println("<br>" + trueMatch.getIonMatchTally());
 					pw.println("</td>");
 					
 					//creating the image of our match
 					//TODO needs to be redone
 					
 					imageIndex++;
 					
 					pw.println("<td>");
 					pw.println(ourMatch.getPeptide().getAcidSequenceString());
 					pw.println("<br>" + matchContainer.getCorrectAcidSequence());
 					pw.println("</td>");
 					
 				}
 
 			}
 			
 			//print the footer and close out
 			pw.println("</table></body></html>");
 			pw.flush();
 			pw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 
 	
 	public static int isPeptidePresentInList(Peptide peptide, ArrayList<Peptide> peptides) {
 		int peptideIndex = MathFunctions.findFirstIndexGreater(peptides, peptide.getMass() - .01);
 		peptideIndex -= 8;
 		if (peptideIndex < 0) peptideIndex = 0;
 		for (int i = peptideIndex; i < peptides.size(); i++) {
 			if (peptide.equals(peptides.get(i))) {
 				return i;
 			}
 			if (peptides.get(i).getMass() > peptide.getMass()) {
 				break;
 			}
 		}
 		return -1;
 	}
 
 }
