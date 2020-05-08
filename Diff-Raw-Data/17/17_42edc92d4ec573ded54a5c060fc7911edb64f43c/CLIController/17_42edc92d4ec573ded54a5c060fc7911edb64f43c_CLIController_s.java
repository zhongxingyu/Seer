 package org.bestgrid.control;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.RemoteFileSystemException;
 //import grisu.control.exceptions.JobPropertiesException;
 //import grisu.control.exceptions.JobSubmissionException;
 //import grisu.frontend.model.job.JobObject;
 //import grisu.jcommons.constants.Constants;
 import grisu.model.FileManager;
 import grisu.model.GrisuRegistryManager;
 
 //import java.util.Scanner;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.PosixParser;
 //import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.bestgrid.model.BlastJobCLI;
 
 public class CLIController {
 	private Options options;
 	private Options blastOptions;
 	private BlastJobCLI job;
 	private ServiceInterface si;
 	private FileManager fm;
 	//private String command;
 	
 	public void createJob() {
 		job = new BlastJobCLI();
 	}
 	
 	public BlastJobCLI getJobObject(){
 		return job;
 	}
 	
 	public void process(String[] args) throws ParseException {
 		createOptions();
 
 		//System.out.println("Creating job...");
 		//createJob();
 		job = new BlastJobCLI();
 		
 		int cpu = 3;
 		String submitLoc = "dev8_1:ng2hpc.canterbury.ac.nz#Loadleveler";
 		String vo = "/ARCS/LocalAccounts/CanterburyHPC";
 		
 		//job.setServiceInterface(getServiceInterface());
 		job.setCpus(cpu);
 		job.setSubmissionLocation(submitLoc);
 		job.setVo(vo);
 		
 		//getting arguments for job specification
 		CommandLineParser parser = new PosixParser();
 		
 		//testing the contents of args
 		for(int i = 0; i < args.length; i++) {
 			System.out.println(args[i] + " ");
 		}
 		
 		try {
 			System.out.println("check parsing");
 			CommandLine line = parser.parse(options, args);			
 
 			if(line.hasOption("cpu")) {
 				System.out.println("cpu " + line.getOptionValue("cpu"));
 				try {
 					//int cpu;
 					Integer.parseInt(line.getOptionValue("cpu"));
 					if(Integer.parseInt(line.getOptionValue("cpu")) < 3) {
 						System.out.println("Minimum CPU to be used is 3");
 						System.out.println("using 3 as default CPU number to be used");
 						job.setCpus(cpu);
 					} else {
 						job.setCpus(Integer.parseInt(line.getOptionValue("cpu")));
 					}
 				} catch(Exception e) {
 					System.out.println("Please enter the value as integer");
 					System.exit(1);
 				}
 			} 
 			
 			if(line.hasOption("vo")) {
 				System.out.println("vo " + line.getOptionValue("vo"));
 				job.setVo(line.getOptionValue("vo"));
 			} 
 			if(line.hasOption("sl")) {
 				System.out.println("sl " + line.getOptionValue("sl"));
 				job.setSubmissionLocation(line.getOptionValue("sl"));
 			}
 			
 			if(line.hasOption("type")) {
 				//String option = line.getOptionValue("type");
 				//System.out.println("XXX: " + option);
 				
 				if(line.getOptionValue("type").equals("blastn")) setBlastNOptions();
				if(line.getOptionValue("type").equals("blastp")) {
 					createBlastPOptions();
 					job.setCommandline("blastp ");
 					processBlastP(args);
				}
				if(line.getOptionValue("type").equals("blastx")) {
 					createBlastXOptions();
 					job.setCommandline("blastx ");
 					processBlastX(args);
 					
				}
				if(line.getOptionValue("type").equals("tblastn")) {
 					createTBlastNOptions();
 					job.setCommandline("tblastn ");
 					processTBlastX(args);
				}
				if(line.getOptionValue("type").equals("tblastx")) {
 					createTBlastXOptions();
 					job.setCommandline("tblastx ");
 					processTBlastX(args);
 				}
 			}
 		} 
 		catch(ParseException exp) {
 			System.err.println("Parsing failed. Reason: " + exp.getMessage());
 			exp.printStackTrace();
 			System.exit(1);
 		}
 		
 	}
 	
 	public Options createOptions() {
 		Options opt = new Options();  
 				
 		opt.addOption("cpu", true, "number of CPUs to use");
 		opt.addOption("vo", true, "VO to use");
 		opt.addOption("sl", true, "location to submit the job");
 
 		opt.addOption(OptionBuilder.withLongOpt( "type" )
                 .withDescription( "blast application to use" )
                 .hasArg()
                 .withArgName("blast")
                 .create() );
 /*
 		Option blast = new Option("t", "type", true, "blast application to use");
 		blast.setValueSeparator('=');
 		opt.addOption(blast);
 	*/	
 		//add options for blast types
 		/*
 		OptionBuilder blastOption = OptionBuilder.withLongOpt("type");
 		blastOption = blastOption.withDescription("blast application to use");
 		blastOption = blastOption.hasArg();
 		blastOption = blastOption.isRequired();
 		opt.addOption(blastOption.create());
 		*/
 		return this.options = opt;
 	}
 	
 	public void setBlastNOptions() {
 		
 	}
 	
 	public Options createBlastPOptions() {
 		System.out.println("Creating blastp options");
 		Options opt = this.options;
 		
 		opt.addOption("query", true, "Input file name");
 		opt.addOption("query_loc", true, "Location on the query sequence " +
 				"in 1-based offsets (format:start-stop)");
 		opt.addOption("evalue", true, "Expectation value (E) threshold for " +
 				"saving hits. Default ='10'");
 		opt.addOption("word_size", true, "Word size for wordfinder algorithm");
 		opt.addOption("gapopen", true, "Cost to open a gap");
 		opt.addOption("gapextend", true, "Cost to extend a gap");
 		opt.addOption("matrix", true, "Scoring matrix name (normally BLOSUM62)");
 		
 		
 		opt.addOption("cstat", "comp_based_stats", true, "Use composition-based " +
 				"statistics for blastp / tblastn");
 		
 		opt.addOption("seg", true, "Filter query sequence with SEG " +
 				"(Format: 'yes', 'window locut hicut', or 'no' to disable). " +
 				"Default = 'no'");
 		opt.addOption("lcase_masking", false, "Use lower case filtering in query" +
 		" and subject sequence(s)?");
 		
 		return this.blastOptions = opt;
 	}
 
 	public Options createBlastXOptions() {
 		System.out.println("Creating blastx options");
 		Options opt = this.options;
 		
 		opt.addOption("query", true, "Input file name");
 		opt.addOption("query_loc", true, "Location on the query sequence " +
 				"in 1-based offsets (format:start-stop)");
		opt.addOption("query-gencode", true, "Genetic code to use to " +
 				"translate query. Default = '1'");
 		opt.addOption("evalue", true, "Expectation value (E) threshold for " +
 				"saving hits. Default ='10'");
 		opt.addOption("word_size", true, "Word size for wordfinder algorithm");
 		opt.addOption("gapopen", true, "Cost to open a gap");
 		opt.addOption("gapextend", true, "Cost to extend a gap");
 		opt.addOption("matrix", true, "Scoring matrix name (normally BLOSUM62)");
 		opt.addOption("seg", true, "Filter query sequence with SEG " +
 				"(Format: 'yes', 'window locut hicut', or 'no' to disable). " +
 				"Default = '12 2.2 2.5'");
 		opt.addOption("lcase_masking", false, "Use lower case filtering in query" +
 				" and subject sequence(s)?");
 		
 		return this.blastOptions = opt;
 		
 	}
 
 	public Options createTBlastNOptions() {
 		System.out.println("Creating tblastn options");
 		Options opt = this.options;
 		
 		opt.addOption("query", true, "Input file name");
 		opt.addOption("query_loc", true, "Location on the query sequence " +
 				"in 1-based offsets (format:start-stop)");
 		
 		opt.addOption("evalue", true, "Expectation value (E) threshold for " +
 				"saving hits. Default ='10'");
 		opt.addOption("word_size", true, "Word size for wordfinder algorithm");
 		opt.addOption("gapopen", true, "Cost to open a gap");
 		opt.addOption("gapextend", true, "Cost to extend a gap");
 		
 		opt.addOption("matrix", true, "Scoring matrix name (normally BLOSUM62)");
 		
 		opt.addOption("cstat", "comp_based_stats", true, "Use composition-based " +
 		"statistics for blastp / tblastn");
 		
 		opt.addOption("seg", true, "Filter query sequence with SEG " +
 				"(Format: 'yes', 'window locut hicut', or 'no' to disable). " +
 				"Default = '12 2.2 2.5'");
 		opt.addOption("soft_masking", true, "Apply filtering locations as soft " +
 				"masks. Default = 'false'");
 		opt.addOption("lcase_masking", false, "Use lower case filtering in query" +
 				" and subject sequence(s)?");
 		
 		return this.blastOptions = opt;
 	}
 	
 	public Options createTBlastXOptions() {
 		System.out.println("Creating tblastx options");
 		Options opt = this.options;
 		
 		opt.addOption("query", true, "Input file name");
 		opt.addOption("query_loc", true, "Location on the query sequence " +
 				"in 1-based offsets (format:start-stop)");
 		opt.addOption("query-gencode", true, "Genetic code to use to " +
 				"translate query. Default = '1'");
 		opt.addOption("evalue", true, "Expectation value (E) threshold for " +
 				"saving hits. Default ='10'");
 		opt.addOption("word_size", true, "Word size for wordfinder algorithm");
 		
 		opt.addOption("matrix", true, "Scoring matrix name (normally BLOSUM62)");
 		opt.addOption("seg", true, "Filter query sequence with SEG " +
 				"(Format: 'yes', 'window locut hicut', or 'no' to disable). " +
 				"Default = '12 2.2 2.5'");
 		opt.addOption("soft_masking", true, "Apply filtering locations as soft " +
 				"masks. Default = 'false'");
 		opt.addOption("lcase_masking", false, "Use lower case filtering in query" +
 				" and subject sequence(s)?");
 		
 		return this.blastOptions = opt;
 	}
 	
 	public void processBlastP(String[] theArgs) {
 		CommandLineParser parser = new GnuParser();
 		
 		try {
 			CommandLine line = parser.parse(options, theArgs);			
 
 			if(line.hasOption("query")) {
 				try {
 					job.setFastaFile(line.getOptionValue("query"));
 					job.addCommand("-query " + line.getOptionValue("query"));
 					job.setCommandline(job.getCommand());
 				} catch (RemoteFileSystemException e) {
 					System.out.println("Invalid argument for query");
 					System.exit(1);
 				}
 			} 
 			
 			if(line.hasOption("query_loc")) {
 				try {
 					job.setQueryLocation(line.getOptionValue("query_loc"));
 					job.addCommand("-query_loc " + line.getOptionValue("query_loc"));
 					job.setCommandline(job.getCommand());
 				} catch (NumberFormatException e) {
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				} catch (Exception e){
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				}
 			} 
 			
 			if(line.hasOption("query_gencode")) {
 				
 				try {
 					job.setGeneticCode(line.getOptionValue("query_gencode"));
 					job.addCommand("-query_gencode " + line.getOptionValue("query_gencode"));
 					job.setCommandline(job.getCommand());
 				} catch(NumberFormatException e) {
 					System.out.println("query_gencode value must be integer");
 					//System.exit(1);
 				}
 						
 			}
 			
 			if(line.hasOption("evalue")) {
 				//make sure argument value is integer
 				int e;
 				try {
 					e = Integer.parseInt(line.getOptionValue("evalue"));
 					//e value must be more than 0 (Real)
 					if(e < 0) {
 						System.out.println("evalue argument is not real number");
 						System.exit(1);
 					} else job.setExpectationValue(line.getOptionValue("evalue"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("evalue must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-evalue " + line.getOptionValue("evalue"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("word_size")) {
 				//make sure argument value is integer
 				int w;
 				try {
 					w = Integer.parseInt(line.getOptionValue("word_size"));
 					//e value must be 2 or more
 					if(w < 2) {
 						System.out.println("word size must be more than 2");
 						System.exit(1);
 					} else job.setWordSize(line.getOptionValue("word_size"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("word size must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-word_size " + line.getOptionValue("word_size"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("gapopen")) {
 				//make sure argument value is integer
 				try {
 					//gO = Integer.parseInt(line.getOptionValue("gapopen"));
 					job.setOpenCost(line.getOptionValue("gapopen"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("the cost for opening a gap " +
 							"should be in integer format");
 					System.exit(1);
 				}
 				job.addCommand("-gapopen " + line.getOptionValue("gapopen"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("gapextend")) {
 				//make sure argument value is integer
 				try {
 					//gE = Integer.parseInt(line.getOptionValue("evalue"));
 					job.setExtendCost(line.getOptionValue("evalue"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("the cost for extending a gap " +
 							"should be in integer format");
 					System.exit(1);
 				}
 				job.addCommand("-gapextend " + line.getOptionValue("gapextend"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("matrix")) {
 				job.setMatrix(line.getOptionValue("matrix"));
 				job.addCommand("-matrix " + line.getOptionValue("matrix"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("comp_based_stats")) {
 				try {
 					job.setCompBasedStat(line.getOptionValue("comp_based_stats"));
 				} catch (Exception e) {
 					System.out.println("comp_based_stats argument failed. " +
 							"Reason: " + e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("seg")) {
 				try {
 					job.setSEGFilter(line.getOptionValue("seg"));
 					job.addCommand("-seg " + line.getOptionValue("seg"));
 					job.setCommandline(job.getCommand());
 				} catch (Exception e){
 					System.out.println("seg argument failed. Reason: " + e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("lcase_masking")) {
 				job.setLowerCaseFilter(true);
 				job.addCommand("-lcase_masking ");
 			}
 		}catch(ParseException exp) {
 			System.err.println("Parsing failed. Reason: " + exp.getMessage());
 		}
 	}
 	
 	public void processBlastX(String[] theArgs) {
 		CommandLineParser parser = new GnuParser();
 		
 		try {
 			CommandLine line = parser.parse(options, theArgs);			
 
 			if(line.hasOption("query")) {
 				try {
 					job.setFastaFile(line.getOptionValue("query"));
 					job.addCommand("-query " + line.getOptionValue("query"));
 					job.setCommandline(job.getCommand());
 				} catch (RemoteFileSystemException e) {
 					System.out.println("Invalid argument for query");
 					System.exit(1);
 				}
 			} 
 			
 			if(line.hasOption("query_loc")) {
 				try {
 					job.setQueryLocation(line.getOptionValue("query_loc"));
 					job.addCommand("-query_loc " + line.getOptionValue("query_loc"));
 					job.setCommandline(job.getCommand());
 				} catch (NumberFormatException e) {
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				} catch (Exception e){
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				}
 			} 
 			
 			if(line.hasOption("query_gencode")) {
 				//make sure argument value is integer				
 				try {
 					job.setGeneticCode(line.getOptionValue("query_gencode"));
 				} catch(NumberFormatException e) {
 					System.out.println("query_gencode value must be integer");
 					//System.exit(1);
 				}
 				
 				job.addCommand("-query_gencode " + line.getOptionValue("query_gencode"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("evalue")) {
 				//make sure argument value is integer
 				int e;
 				try {
 					e = Integer.parseInt(line.getOptionValue("evalue"));
 					//e value must be more than 0 (Real)
 					if(e < 0) {
 						System.out.println("evalue argument is not real number");
 						System.exit(1);
 					} else job.setExpectationValue(line.getOptionValue("evalue"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("evalue must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-evalue " + line.getOptionValue("evalue"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("word_size")) {
 				//make sure argument value is integer
 				int w;
 				try {
 					w = Integer.parseInt(line.getOptionValue("word_size"));
 					//e value must be 2 or more
 					if(w < 2) {
 						System.out.println("word size must be more than 2");
 						System.exit(1);
 					} else job.setWordSize(line.getOptionValue("word_size"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("word size must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-word_size " + line.getOptionValue("word_size"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("gapopen")) {
 				//make sure argument value is integer
 				try {
 					//gO = Integer.parseInt(line.getOptionValue("gapopen"));
 					job.setOpenCost(line.getOptionValue("gapopen"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("the cost for opening a gap " +
 							"should be in integer format");
 					System.exit(1);
 				}
 				job.addCommand("-gapopen " + line.getOptionValue("gapopen"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("gapextend")) {
 				//make sure argument value is integer
 				try {
 					//gE = Integer.parseInt(line.getOptionValue("evalue"));
 					job.setExtendCost(line.getOptionValue("evalue"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("the cost for extending a gap " +
 							"should be in integer format");
 					System.exit(1);
 				}
 				job.addCommand("-gapextend " + line.getOptionValue("gapextend"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("matrix")) {
 				job.setMatrix(line.getOptionValue("matrix"));
 				job.addCommand("-matrix " + line.getOptionValue("matrix"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("seg")) {
 				try {
 					job.setSEGFilter(line.getOptionValue("seg"));
 					job.addCommand("-seg " + line.getOptionValue("seg"));
 					job.setCommandline(job.getCommand());
 				} catch (Exception e){
 					System.out.println("seg argument failed. Reason: " + e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("lcase_masking")) {
 				job.setLowerCaseFilter(true);
 				job.addCommand("-lcase_masking ");
 			}
 		}catch(ParseException exp) {
 			System.err.println("Parsing failed. Reason: " + exp.getMessage());
 		}
 	}
 	
 	public void processTBlastN(String[] theArgs) {
 		CommandLineParser parser = new GnuParser();
 		
 		try {
 			CommandLine line = parser.parse(options, theArgs);			
 
 			if(line.hasOption("query")) {
 				try {
 					job.setFastaFile(line.getOptionValue("query"));
 					job.addCommand("-query " + line.getOptionValue("query"));
 					job.setCommandline(job.getCommand());
 				} catch (RemoteFileSystemException e) {
 					System.out.println("Invalid argument for query");
 					System.exit(1);
 				}
 			} 
 			
 			if(line.hasOption("query_loc")) {
 				try {
 					job.setQueryLocation(line.getOptionValue("query_loc"));
 					job.addCommand("-query_loc " + line.getOptionValue("query_loc"));
 					job.setCommandline(job.getCommand());
 				} catch (NumberFormatException e) {
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				} catch (Exception e){
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				}
 			} 
 			
 			if(line.hasOption("query_gencode")) {
 				
 				try {
 					job.setGeneticCode(line.getOptionValue("query_gencode"));
 					job.addCommand("-query_gencode " + line.getOptionValue("query_gencode"));
 					job.setCommandline(job.getCommand());
 				} catch(NumberFormatException e) {
 					System.out.println("query_gencode value must be integer");
 					//System.exit(1);
 				}
 						
 			}
 			
 			if(line.hasOption("evalue")) {
 				//make sure argument value is integer
 				int e;
 				try {
 					e = Integer.parseInt(line.getOptionValue("evalue"));
 					//e value must be more than 0 (Real)
 					if(e < 0) {
 						System.out.println("evalue argument is not real number");
 						System.exit(1);
 					} else job.setExpectationValue(line.getOptionValue("evalue"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("evalue must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-evalue " + line.getOptionValue("evalue"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("word_size")) {
 				//make sure argument value is integer
 				int w;
 				try {
 					w = Integer.parseInt(line.getOptionValue("word_size"));
 					//e value must be 2 or more
 					if(w < 2) {
 						System.out.println("word size must be more than 2");
 						System.exit(1);
 					} else job.setWordSize(line.getOptionValue("word_size"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("word size must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-word_size " + line.getOptionValue("word_size"));
 				job.setCommandline(job.getCommand());
 			}
 
 			if(line.hasOption("gapopen")) {
 				//make sure argument value is integer
 				try {
 					//gO = Integer.parseInt(line.getOptionValue("gapopen"));
 					job.setOpenCost(line.getOptionValue("gapopen"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("the cost for opening a gap " +
 							"should be in integer format");
 					System.exit(1);
 				}
 				job.addCommand("-gapopen " + line.getOptionValue("gapopen"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("gapextend")) {
 				//make sure argument value is integer
 				try {
 					//gE = Integer.parseInt(line.getOptionValue("evalue"));
 					job.setExtendCost(line.getOptionValue("evalue"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("the cost for extending a gap " +
 							"should be in integer format");
 					System.exit(1);
 				}
 				job.addCommand("-gapextend " + line.getOptionValue("gapextend"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("matrix")) {
 				job.setMatrix(line.getOptionValue("matrix"));
 				job.addCommand("-matrix " + line.getOptionValue("matrix"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("comp_based_stats")) {
 				try {
 					job.setCompBasedStat(line.getOptionValue("comp_based_stats"));
 				} catch (Exception e) {
 					System.out.println("comp_based_stats argument failed. " +
 							"Reason: " + e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("seg")) {
 				try {
 					job.setSEGFilter(line.getOptionValue("seg"));
 					job.addCommand("-seg " + line.getOptionValue("seg"));
 					job.setCommandline(job.getCommand());
 				} catch (Exception e){
 					System.out.println("seg argument failed. Reason: " + e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("soft_masking")) {
 				try {
 					job.setSoftMasking(line.getOptionValue("soft_masking"));
 				} catch (Exception e) {
 					System.out.println("invalid argument for soft_masking. "
 							+ e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("lcase_masking")) {
 				job.setLowerCaseFilter(true);
 				job.addCommand("-lcase_masking ");
 			}
 		}catch(ParseException exp) {
 			System.err.println("Parsing failed. Reason: " + exp.getMessage());
 		}
 	}
 	
 	public void processTBlastX(String[] theArgs) {
 		CommandLineParser parser = new GnuParser();
 		
 		try {
 			CommandLine line = parser.parse(options, theArgs);			
 
 			if(line.hasOption("query")) {
 				try {
 					job.setFastaFile(line.getOptionValue("query"));
 					job.addCommand("-query " + line.getOptionValue("query"));
 					job.setCommandline(job.getCommand());
 				} catch (RemoteFileSystemException e) {
 					System.out.println("Invalid argument for query");
 					System.exit(1);
 				}
 			} 
 			
 			if(line.hasOption("query_loc")) {
 				try {
 					job.setQueryLocation(line.getOptionValue("query_loc"));
 					job.addCommand("-query_loc " + line.getOptionValue("query_loc"));
 					job.setCommandline(job.getCommand());
 				} catch (NumberFormatException e) {
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				} catch (Exception e){
 					System.out.println("query_loc option failed. Reason:" 
 							+ e.getMessage());
 				}
 			} 
 			
 			if(line.hasOption("query_gencode")) {
 				
 				try {
 					job.setGeneticCode(line.getOptionValue("query_gencode"));
 					job.addCommand("-query_gencode " + line.getOptionValue("query_gencode"));
 					job.setCommandline(job.getCommand());
 				} catch(NumberFormatException e) {
 					System.out.println("query_gencode value must be integer");
 					//System.exit(1);
 				}
 						
 			}
 			
 			if(line.hasOption("evalue")) {
 				//make sure argument value is integer
 				int e;
 				try {
 					e = Integer.parseInt(line.getOptionValue("evalue"));
 					//e value must be more than 0 (Real)
 					if(e < 0) {
 						System.out.println("evalue argument is not real number");
 						System.exit(1);
 					} else job.setExpectationValue(line.getOptionValue("evalue"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("evalue must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-evalue " + line.getOptionValue("evalue"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("word_size")) {
 				//make sure argument value is integer
 				int w;
 				try {
 					w = Integer.parseInt(line.getOptionValue("word_size"));
 					//e value must be 2 or more
 					if(w < 2) {
 						System.out.println("word size must be more than 2");
 						System.exit(1);
 					} else job.setWordSize(line.getOptionValue("word_size"));
 				} catch(NumberFormatException nFE) {
 					System.out.println("word size must be integer");
 					System.exit(1);
 				}
 				
 				job.addCommand("-word_size " + line.getOptionValue("word_size"));
 				job.setCommandline(job.getCommand());
 			}
 
 			if(line.hasOption("matrix")) {
 				job.setMatrix(line.getOptionValue("matrix"));
 				job.addCommand("-matrix " + line.getOptionValue("matrix"));
 				job.setCommandline(job.getCommand());
 			}
 			
 			if(line.hasOption("seg")) {
 				try {
 					job.setSEGFilter(line.getOptionValue("seg"));
 					job.addCommand("-seg " + line.getOptionValue("seg"));
 					job.setCommandline(job.getCommand());
 				} catch (Exception e){
 					System.out.println("seg argument failed. Reason: " + e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("soft_masking")) {
 				try {
 					job.setSoftMasking(line.getOptionValue("soft_masking"));
 				} catch (Exception e) {
 					System.out.println("invalid argument for soft_masking. "
 							+ e.getMessage());
 				}
 			}
 			
 			if(line.hasOption("lcase_masking")) {
 				job.setLowerCaseFilter(true);
 				job.addCommand("-lcase_masking ");
 			}
 		}catch(ParseException exp) {
 			System.err.println("Parsing failed. Reason: " + exp.getMessage());
 		}
 	}
 	
 	public ServiceInterface getServiceInterface() {
 		return this.si;
 	}
 	
 	public void setServiceInterface(ServiceInterface si) {
 		this.si = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 	}
 	
 }
