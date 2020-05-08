 package enderdom.eddie.tasks.bio;
 
 import org.apache.commons.cli.CommandLine;
 
 import enderdom.eddie.bio.homology.PantherGene;
 import enderdom.eddie.bio.lists.Fasta;
 import enderdom.eddie.tasks.TaskState;
 import enderdom.eddie.tasks.TaskXT;
 import enderdom.eddie.tools.Tools_System;
 import enderdom.eddie.tools.bio.NCBI_DATABASE;
 import enderdom.eddie.tools.bio.Tools_NCBI;
 import enderdom.eddie.tools.bio.Tools_Panther;
 import enderdom.eddie.tools.bio.Tools_Uniprot;
 
 public class Task_DataMiner extends TaskXT{
 
 	private String panther;
 	private String organism;
 	private String output;
 	
 	public Task_DataMiner(){
 		
 	}
 	
 	public void parseArgsSub(CommandLine cmd){
 		super.parseArgsSub(cmd);
 		panther = getOption(cmd, "PTHR", null);
 		organism = getOption(cmd, "organism", null);
 		output = getOption(cmd, "output", null);
 	}
 	
 	public void buildOptions(){
 		super.buildOptions();
 		options.addOption("PTHR",true, "Panther term");
 		options.addOption("organism",true, "Limit to organism");
		options.addOption("o","output",true, "Output file for data");
 		options.removeOption("p");
 	}
 	
 	public void run(){
 		setCompleteState(TaskState.STARTED);
 		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
 		if(panther != null && output != null){
 			runPantherScript();
 		}
 		else if(output == null)logger.error("output is null");
 		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
 	    setCompleteState(TaskState.FINISHED);
 	}
 	
 	public void runPantherScript(){
 		try{
 			Fasta f = new Fasta();
 			PantherGene[] genes = Tools_Panther.getGeneList(panther);
 			String shortn = null;
 			if(organism != null){
 				shortn = Tools_Panther.getShortSpeciesName(organism);
 				logger.debug("Retrieved short name as "  +shortn);
 			}
 			int ncbin=0, uidn =0, fail=0, cull =0;
 			logger.debug("Retrieving sequences from online databases");
 			for(PantherGene gene : genes){
 				String uid = gene.getUniprot();
 				String ncbi = gene.getNCBI();
 				if(shortn != null){
 					if(gene.getShortSpecies().equals(shortn)){
 						if(uid != null){
 							f.addSequenceObject(Tools_Uniprot.getUniprot(uid));
 							uidn++;
 						}
 						else if(ncbi != null){
 							f.addSequenceObject(Tools_NCBI.getSequencewAcc(NCBI_DATABASE.protein, ncbi));
 							ncbin++;
 						}
 						else{
 							logger.trace("No support for downloading " +gene.getGeneAcc());
 							fail++;
 						}
 					}
 					else cull++;
 				}
 				else{
 					if(uid != null){
 						f.addSequenceObject(Tools_Uniprot.getUniprot(uid));
 						uidn++;
 					}
 					else if(ncbi != null){
 						f.addSequenceObject(Tools_NCBI.getSequencewAcc(NCBI_DATABASE.protein, ncbi));
 						ncbin++;
 					}
 					else{
 						logger.trace("No support for downloading " +gene.getGeneAcc());
 						fail++;
 					}
 				}
 				System.out.print("\r"+ (uidn+ncbin)+" sequences downloaded    ");
 			}
 			System.out.println();
 			logger.info(cull+" Sequences removed due to incorrect species");
 			logger.info(fail+" Sequences not included no uniprot or ncbi available");
 			logger.info(uidn+" sequences retrieved from uniprot and "+ ncbin +" retrieved from ncbi");
 			logger.info("Saving "+f.getNoOfSequences()+" sequences to fasta");
 			f.save2Fasta(output);
 		}
 		catch(Exception e){
 			logger.error("Failed to download panther data", e);
 		}
 	}
 }
