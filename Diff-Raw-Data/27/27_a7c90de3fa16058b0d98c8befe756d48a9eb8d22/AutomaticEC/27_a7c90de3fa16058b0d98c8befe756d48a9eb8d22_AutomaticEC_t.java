 package nzgot.ec;
 
 import jebl.evolution.align.scores.Blosum80;
 import jebl.evolution.io.FastaExporter;
 import jebl.evolution.io.FastaImporter;
 import jebl.evolution.io.ImportException;
 import jebl.evolution.sequences.BasicSequence;
 import jebl.evolution.sequences.GeneticCode;
 import jebl.evolution.sequences.Sequence;
 import jebl.evolution.sequences.SequenceType;
 import nzgot.core.community.OTU;
 import nzgot.core.community.OTUs;
 import nzgot.core.community.io.CommunityImporter;
 import nzgot.core.util.SequenceUtil;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Automatic error correction
  * @author Thomas Hummel
  * @author Walter Xie
  */
 public class AutomaticEC {
 
 	//Sequence files
	final String fileSeq = "/Users/dxie004/Documents/ModelEcoSystem/454/2010-pilot/Combined_CO1_CO1Soil/Indirect/IndirectSoil_endTrimmed.fasta";
	final String fileRef = "/Users/dxie004/Documents/ModelEcoSystem/454/2010-pilot/Combined_CO1_CO1Soil/1608_Sanger_translated.fasta";
	final String fileCor = "/Users/dxie004/Documents/ModelEcoSystem/454/2010-pilot/Combined_CO1_CO1Soil/IndirectSoil_corrected_fullRef.fasta";
 
 	//Mapping files
	final String mapSeqOtu = "/Users/dxie004/Documents/ModelEcoSystem/454/2010-pilot/Combined_CO1_CO1Soil/Indirect/otu/otu_map_IndirectSoil_userout.m8";
	final String mapOtuRef = "/Users/dxie004/Documents/ModelEcoSystem/454/2010-pilot/Combined_CO1_CO1Soil/Indirect/otu/reference_85_IndirectSoil_userout.m8";
 
 	List<Sequence> sequences;
 	List<Sequence> references;
 	List<Sequence> sequencesCor;
 
 	/**
 	 *Automatic error correction of all sequences given 
 	 *in the <code>fileSeq</code> file which mapped to the <code>fileRef</code> file 
 	 */
 	public void doEC() throws IOException, ImportException{
         File sequenceIn = new File(fileSeq);
         File referenceIn = new File(fileRef);
         File sequenceOut = new File(fileCor);
 
 //		Mapping map = new Mapping();
 		FastaImporter sequenceImport = new FastaImporter(sequenceIn , SequenceType.NUCLEOTIDE);
 		FastaImporter referenceImport = new FastaImporter(referenceIn, SequenceType.AMINO_ACID);
 		sequences = sequenceImport.importSequences();
 		references =referenceImport.importSequences();
 		sequencesCor = new ArrayList<Sequence>(2000);
 
         File file = new File(mapSeqOtu);
         OTUs otus = new OTUs(file.getName());
         CommunityImporter.importOTUsAndMapping(file, otus);
 
         file = new File(mapOtuRef);
         CommunityImporter.importReferenceSequenceMapping(file, otus);
 
 
 //		map.parseSeqOtuTable(mapSeqOtu);
 //		map.parseOtuRefTable(mapOtuRef);
 
         double count = 0;
         double size = sequences.size();
 
         String referenceLabel;
         String referenceSeq;
 
         for (Sequence seq : sequences) {
 
 			count++;
 			referenceLabel = null;
 			referenceSeq = null;
 
             OTU otu = (OTU) otus.getOTU(seq.getTaxon().toString());
            if (otu != null && otu.getReference() != null)
                referenceLabel = otu.getReference().toString();
 //			referenceLabel = map.findReference(seq.getTaxon().toString());
 
 			//reference String 
 			if (referenceLabel != null)
 				referenceSeq = SequenceUtil.getSequenceStringFrom(referenceLabel, references);
 
 			//correct sequence with reference alignment and save in list
 			if (referenceSeq != null) {
 				AlignAndCorrect ac = new AlignAndCorrect(new Blosum80(), -10, -10, -100, GeneticCode.INVERTEBRATE_MT);
 				ac.doAlignment(seq.getString(), referenceSeq);
 				try{
 					Sequence correctedSeq = new BasicSequence(SequenceType.NUCLEOTIDE, seq.getTaxon(), ac.getMatch()[1].toString()); //replace gaps with '?'...	
 					sequencesCor.add(correctedSeq);
 					Debugger.log( String.format("%.5g", ((count/size))*100) +"%" );
 				}
 				catch (NullPointerException e) {
 					Debugger.log(seq.getTaxon().toString());
 				}
 
 			}
 
 			//ac.getCorrected(seq);
 			//ac.doMatch(new SystemOut(), "");
 		}
 
         Writer write = new OutputStreamWriter(new FileOutputStream(sequenceOut));
 		FastaExporter fe = new FastaExporter(write);
 		fe.exportSequences(sequencesCor);
 		write.flush();
 		write.close();
 	}
 
 
 	public static void main(String[] args) throws ImportException, IOException{
 		// TODO Auto-generated method stub
 		AutomaticEC ec = new AutomaticEC();
 		ec.doEC();
 	}
 
 }
