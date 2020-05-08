 package enderdom.eddie.bio.factories;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import enderdom.eddie.bio.assembly.ACEFileParser;
 import enderdom.eddie.bio.assembly.BasicContigList;
 import enderdom.eddie.bio.lists.ClustalAlign;
 import enderdom.eddie.bio.lists.Fasta;
 import enderdom.eddie.bio.sequence.BioFileType;
 import enderdom.eddie.bio.sequence.ContigList;
 import enderdom.eddie.bio.sequence.SequenceList;
 import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.exceptions.EddieGenericException;
 import enderdom.eddie.tools.bio.Tools_Bio_File;
 
 public class SequenceListFactory {
 	
 	public SequenceListFactory(){
 		
 	}
 			
 	public static SequenceList getSequenceList(File i) throws FileNotFoundException, UnsupportedTypeException, IOException{
 		BioFileType filetype = Tools_Bio_File.detectFileType(i.getName());
 		Fasta f;
 		switch(filetype){
 			case FASTA:
 				f = new Fasta();
 				f.loadFile(i, filetype);
 				return f;
 			case FASTQ:
 				f = new Fasta();
 				f.loadFile(i, filetype);
 				return f;
 			case QUAL:
 				f = new Fasta();
 				f.loadFile(i, filetype);
 				return f;
 			case ACE:
 				f = new Fasta();
 				ACEFileParser parser = new ACEFileParser(i);
 				while(parser.hasNext()){
 					f.addSequenceObject(parser.next().getConsensus());
 				}
 				return f;
 			case CLUSTAL_ALN:
 				ClustalAlign a = new ClustalAlign(i, BioFileType.CLUSTAL_ALN);
 				return a;
 			//case SAM://TODO this method	
 			default:
 			throw new UnsupportedTypeException("You are trying to get a sequence list from " 
 					+ filetype.toString() + " which is not yet supported");	
 		}
 	}
 		
 	
 	/**
 	 * Task in general is supposed to remove some
 	 * of the cludge in Tasks for creating bio objects
 	 * by moving the cludge here and adding extra layers
 	 * of abstraction so you have no idea whats going on.
 	 * Enjoy :) 
 	 * 
 	 * Dominic
 	 * 
 	 * @param input String path of a file to load
 	 * @return A SequenceList object which you can manipulate
 	 * @throws FileNotFoundException
 	 * @throws UnsupportedTypeException
 	 * @throws IOException
 	 */
 	public static SequenceList getSequenceList(String input) throws FileNotFoundException, UnsupportedTypeException, IOException{
 		File i = new File(input);
 		if(!i.exists()){
 			throw new FileNotFoundException("File: " + input + " does not exist");
 		}
 		else if(i.isDirectory()){
 			throw new FileNotFoundException("File: " + input + " is a directory");//TODO support directory
 		}
 		else{
 			return getSequenceList(i);
 		}
 	}
 	
 	public static SequenceList getSequenceList(File i, File i2)throws FileNotFoundException, UnsupportedTypeException, 
 			IOException{
 		BioFileType f1 = Tools_Bio_File.detectFileType(i.getName());
 		BioFileType f2 = Tools_Bio_File.detectFileType(i2.getName());
 		if((f1 == BioFileType.FASTA && f2 == BioFileType.QUAL) || (f1 == BioFileType.QUAL && f2 == BioFileType.FASTA)){
 			Fasta f = new Fasta();
 			f.loadFile(i, f1);
 			f.loadFile(i2, f2);
 			return f;
 		}
 //		else if((f1 == BioFileType.ACE && f2 == BioFileType.FASTA)){
 //			//TODO
 //		}
 		else{
 			throw new UnsupportedTypeException("Cannot generate Sequence list from " 
 		+ f1.toString() + " and " + f2.toString() + " filetypes");
 		}
 	}
 	
 	public static SequenceList getSequenceList(String input, String input2) throws FileNotFoundException,
 			UnsupportedTypeException, IOException{
 		File i = new File(input);
 		File i2 = new File(input2);
 		if(!i.exists() || !i2.exists()){
 			throw new FileNotFoundException("File: " + input +" or "+ input2+" does not exist");
 		}
 		else if(i.isDirectory() || i2.isDirectory()){
 			throw new FileNotFoundException("File: " + input +" or "+ input2+ " is a directory");
 		}
 		else{
 			return getSequenceList(i, i2);
 		}
 	}
 	
 	public static SequenceList buildSequenceList(BioFileType type) throws UnsupportedTypeException{
 		switch(type){
 			case FASTQ:
 				Fasta f = new Fasta();
 				f.setFastq(true);
 				return f;
 			case FASTA:
 				return new Fasta();
 			case FAST_QUAL:
 				return new Fasta();
 			default:
 				throw new UnsupportedTypeException("Builder for this filetype not yet implemented");
 		}
 	}
 	
	public static ContigList getContigList(String input) throws EddieGenericException, IOException, UnsupportedTypeException{
 		File i = new File(input);
 		if(!i.exists()){
			throw new EddieGenericException("File: " + input + " does not exist");
 		}
 		else if(i.isDirectory()){
			throw new EddieGenericException("File: " + input + " is a directory");
 		}
 		else{
			return getContigList(i);
 		}
 	}
 	
 	
 	public static ContigList getContigList(String input, String input2) throws IOException, UnsupportedTypeException{
 		File i = new File(input);
 		File i2 = new File(input2);
 		if(!i.exists() || !i2.exists()){
 			throw new FileNotFoundException("File: " + input +" or "+ input2+" does not exist");
 		}
 		else if(i.isDirectory() || i2.isDirectory()){
 			throw new FileNotFoundException("File: " + input +" or "+ input2+ " is a directory");
 		}
 		else{
 			return getContigList(i, i2);
 		}
 	}
 	
 	public static ContigList getContigList(File i) throws IOException, UnsupportedTypeException {
 		BioFileType filetype = Tools_Bio_File.detectFileType(i.getName());
 		switch(filetype){
 			case ACE:
 				return new BasicContigList(i, filetype);
 			case SAM:
 				return new BasicContigList(i, filetype);
 			default:
 			throw new UnsupportedTypeException("You are trying to get a sequence list from " 
 					+ filetype.toString() + " which is not yet supported");	
 		}
 	}
 
 	
 	public static ContigList getContigList(File i, File i2) throws IOException, UnsupportedTypeException{
 		BioFileType filetype = Tools_Bio_File.detectFileType(i.getName());
 		BioFileType filetype2 = Tools_Bio_File.detectFileType(i2.getName());
 		switch(filetype){
 			case SAM:
 				if(filetype2 == BioFileType.FASTA || filetype2 == BioFileType.FAST_QUAL || filetype2 == BioFileType.FASTQ){
 					return new BasicContigList(i, i2, filetype);
 				}
 				else throw new UnsupportedTypeException("SAM file needs to have a fasta file, " 
 						+ i2.getName() + " is not detected as such");
 			default:
 			throw new UnsupportedTypeException("You are trying to get a sequence list from " 
 					+ filetype.toString() + " which is not yet supported");	
 		}
 	}
 }
