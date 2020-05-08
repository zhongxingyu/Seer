 package enderdom.eddie.tasks.database;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.log4j.Logger;
 
 import enderdom.eddie.bio.lists.ClustalAlign;
 import enderdom.eddie.bio.lists.Fasta;
 import enderdom.eddie.bio.sequence.BioFileType;
 import enderdom.eddie.bio.sequence.GenericSequence;
 import enderdom.eddie.bio.sequence.SequenceList;
 import enderdom.eddie.bio.sequence.UnsupportedTypeException;
 
 import enderdom.eddie.databases.bioSQL.psuedoORM.BioSequence;
 import enderdom.eddie.databases.manager.DatabaseManager;
 
 import enderdom.eddie.tasks.TaskState;
 import enderdom.eddie.tasks.TaskXT;
 import enderdom.eddie.tools.Tools_CLI;
 import enderdom.eddie.tools.Tools_File;
 import enderdom.eddie.tools.Tools_System;
 import enderdom.eddie.ui.UI;
 
 public class Task_dbTools extends TaskXT{
 
 	private String contig;
 	private boolean readsasfasta;
 	private boolean readsasaln;
 	private String output;
 	private boolean all;
 	private String input;
 	private int run_id;
 	
 	public Task_dbTools(){
 		setHelpHeader("--Database Tools--");
 	}
 	
 	public void printHelpMessage(){
 		Tools_CLI.printHelpMessage(getHelpHeader(), "-- Share And Enjoy! --", this.options);
 		System.out.println("Examples Uses:");
 		System.out.println("-task -c Seqman_Digest_2121 -readsasfasta -o /tmp/Out.fasta");
 		System.out.println("[Will download all reads which were used for contig with ID Seqman_Digest_2121]");
 		System.out.println();
 		System.out.println("-task -run_id 2 -o /tmp/Out.fasta");
 		System.out.println("[Will download all contigs which were assembled during the assembly linked by run_id 2]");
 		System.out.println();
 	}
 	
 	public void parseArgsSub(CommandLine cmd){
 		super.parseArgsSub(cmd);
 		readsasfasta = cmd.hasOption("readsasfasta");
 		readsasaln = cmd.hasOption("readsasaln");
 		output = this.getOption(cmd, "o", null);
 		input = this.getOption(cmd, "i", null);
 		contig = this.getOption(cmd, "c", null);
 		run_id = this.getOption(cmd, "run_id", -1);
 		all=(cmd.hasOption("run_id") || cmd.hasOption("all"));
 	}
 	
 	public void buildOptions(){
 		super.buildOptions();
 		options.addOption(new Option("c","contig", true, "Contig name, use with readsasfasta " +
 				"to download reads rather than consensus"));
 		options.addOption(new Option("i", true, "Bulk download input, input being a list of sequence names"));
 		options.addOption(new Option("readsasfasta", false, "Pulls reads which make this contig as fasta"));
		options.addOption(new Option("readsasaln", false, "Pulls reads which make this contig as clustal aln"));
 		options.addOption(new Option("o","output", true, "Output file"));
 		options.addOption(new Option("all",false, "Batch download all contigs available"));
 		options.addOption(new Option("run_id",true, "Batch download all contigs attached to run_id"));
 	}
 	
 	public void run(){
 		setCompleteState(TaskState.STARTED);
 		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
 		try{
 			if((readsasfasta || readsasaln) && contig != null){
 				if(readsasfasta)DBReadsAsList(contig, output, password, ui, BioFileType.FASTA);
 				else DBReadsAsList(contig, output, password, ui, BioFileType.CLUSTAL_ALN);
 			}
 			else if(contig != null || (input != null)){
 				if(input != null){
 					ContigsAsFasta(output, Tools_File.quickRead2Array(new File(input)), password, ui);
 				}
 				else{
 					ContigAsFasta(output, contig, password, ui);
 				}
 			}
 			else if(all){
 				if(this.output == null){
 					logger.error("Please set output");
 					return;
 				}
 				DBContigsAsFasta(output, run_id, password, ui);
 			}
 			else{
 				logger.info("No option selected");
 			}
 		}
 		catch(Exception e){
 			logger.error("Failed to establish database connection", e);
 			setCompleteState(TaskState.ERROR);
 			return;
 		}
 		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
 	    setCompleteState(TaskState.FINISHED);
 	}
 	
 	
 	public static void DBReadsAsList(String contig, String output, String password, UI ui, BioFileType type) throws Exception{
 		DatabaseManager manager = ui.getDatabaseManager(password);
 	
 		manager.open();
 		SequenceList list;
 		if(contig != null && output != null){
 			int m = manager.getBioSQLXT().getBioEntryId(manager, contig, true, manager.getEddieDBID());
 			if(m == -1){
 				Logger.getRootLogger().error("Failed to retrieve the ID for contig " + contig);
 			}
 			else{
 				int[][] reads = manager.getBioSQLXT().getReads(manager, m);
 				if(reads.length == 0){
 					Logger.getRootLogger().error("Failed to get any reads for contig " + contig + "(ID:"+m+")");
 					return;
 				}
 				else{
 					if(BioFileType.CLUSTAL_ALN == type)list = new ClustalAlign();
 					else list = new Fasta();
 					for(int i =0 ; i < reads[0].length ;i++){
 						String[] names = manager.getBioSQL().getBioEntryNames(manager.getCon(), reads[0][i]);
 						String sequence = manager.getBioSQL().getSequence(manager.getCon(), reads[0][i]);
 						GenericSequence seq = new GenericSequence(names[0], sequence);
 						if(BioFileType.CLUSTAL_ALN == type){
 							seq.extendLeft(reads[2][i]);
 						}
 						list.addSequenceObject(seq);
 					}
 					boolean success = false;
 					try {
 						 success = list.saveFile(new File(output), type).length > 0;
 					} 
 					catch (IOException e) {
 						success = false;
 						Logger.getRootLogger().error("Failed to save fasta" , e);
 					}
 					if(success){
 						Logger.getRootLogger().info("Successfully saved fasta at " + output);
 					}
 				}
 			}
 		}
 		else{
 			Logger.getRootLogger().error("No output/contig set");
 		}
 	}
 	
 	public static void DBContigsAsFasta(String output, int run_id, String password, UI ui) throws Exception{
 		DatabaseManager manager = ui.getDatabaseManager(password);
 		
 		manager.open();
 		SequenceList list = new Fasta();
 		list = manager.getBioSQLXT().getContigsAsList(manager, list, run_id);
 		Logger.getRootLogger().info("Saving "+list.getNoOfSequences()+" Contigs to Fasta file " + output);
 		try {
 			list.saveFile(new File(output), BioFileType.FASTA);
 		} catch (UnsupportedTypeException e) {
 			Logger.getRootLogger().error(e);
 		} catch (Exception e) {
 			Logger.getRootLogger().error(e);
 		}
 	}
 	
 	public static void ContigAsFasta(String output, String contig, String password, UI ui) throws Exception{
 		ContigsAsFasta(output, new String[]{contig}, password, ui);
 	}
 	
 	public static void ContigsAsFasta(String output, String[] contigs, String password, UI ui) throws Exception{
 		DatabaseManager manager = ui.getDatabaseManager(password);
 		if(manager.open()){
 			Fasta fasta = new Fasta();
 			for(String contig : contigs){
 				int bio = manager.getBioSQL().getBioEntry(manager.getCon(), contig, contig, manager.getEddieDBID());
 				BioSequence[] seq = manager.getBioSQLXT().getBioSequences(manager, bio);
 				for(BioSequence b : seq)fasta.addSequenceObject(new GenericSequence(contig, b.getSequence()));
 			}
 			if(fasta.getNoOfSequences() == 0) Logger.getRootLogger().error("Failed to retrieve any sequences with contig name");
 			else{
 				fasta.save2Fasta(new File(output));
 			}
 		}
 		else{
 			throw new Exception("Failed to open database");
 		}
 	}
 	
 }
 
