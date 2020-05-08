 package net.derkholm.nmica.extra.app.seq.chip;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.TreeSet;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.BioException;
 import org.biojava.bio.SimpleAnnotation;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.db.HashSequenceDB;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.symbol.DummySymbolList;
 import org.biojava.bio.symbol.RangeLocation;
 import org.biojavax.bio.seq.RichSequence;
 import org.biojavax.bio.seq.RichSequenceIterator;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 
 @App(overview = "Create an SQLite database usable by nextgenseq-dazzle", generateStub = true)
 @NMExtraApp(launchName = "ngsamprocess", vm = VirtualMachine.SERVER)
 public class SAMProcessor {
 
 	private String in;
 	private SAMFileReader inReader;
 	private File outFile;
 	private HashSequenceDB seqDB;
 	private int qualityCutoff = 10;
 	private int expandReadsBy;
 
 	@Option(help="Input map in SAM format -- " +
 			"input needs to be sorted according to reference sequence identifier " +
 			"and ascending start position (read from stdin if '-' given)")
 	public void setIn(String str) {
 		if (str.equals("-")) {
 			this.inReader = new SAMFileReader(System.in);
 		} else {
 			this.inReader = new SAMFileReader(new File(str));
 		}
 
 		this.in = str;
 	}
 
 	@Option(help="Expand reads")
 	public void setExpandReadsBy(int i) {
 		this.expandReadsBy = i;
 	}
 
 	@Option(help="Reference sequence that the reads are mapped to (FASTA formatted)")
 	public void setRef(File f) throws FileNotFoundException, NoSuchElementException, BioException {
 		this.seqDB = new HashSequenceDB();
 		RichSequenceIterator iter = RichSequence.IOTools.readFastaDNA(new BufferedReader(new FileReader(f)), null);
 
 		while (iter.hasNext()) {
 			Sequence s = iter.nextSequence();
 
 			DummySymbolList symList = new DummySymbolList(DNATools.getDNA(), Integer.MAX_VALUE);
 			Sequence refS = new SimpleSequence(symList,s.getName(),s.getName(),Annotation.EMPTY_ANNOTATION);
 			seqDB.addSequence(refS);
 		}
 	}
 
 	@Option(help="Mapping quality threshold (exclude reads whose mapping quality is below. default=10)", optional=true)
	public void setMappingQualityCutoff(int quality) {
 		this.qualityCutoff = quality;
 	}
 
 	@Option(help="Output file")
 	public void setOut(String str) {
 		this.outFile = new File(str);
 	}
 
 	public void main(String[] args) throws BioException {
 
 		int excludedReads = 0;
 		int readCount = 0;
 		Sequence seq = null;
 		String source = "ngsam";
 		String type = "read";
 		Annotation annotation = new SimpleAnnotation();
 
 		Set<String> seenRefSeqNames = new TreeSet<String>();
 
 
 		for (final SAMRecord samRecord : this.inReader) {
 			readCount += 1;
 
 			int quality = samRecord.getMappingQuality();
 			if (quality < qualityCutoff) {
 				excludedReads += 1;
 				continue;
 			}
 			String refName = samRecord.getReferenceName();
 			assert refName != null;
 			seenRefSeqNames.add(refName);
 
 			int start = samRecord.getAlignmentStart();
 			int end = samRecord.getAlignmentEnd();
 
 
 			boolean isPosStrand = !samRecord.getReadNegativeStrandFlag();
 
 			if (isPosStrand) {
 				end = Math.min(seq.length()-1, end + this.expandReadsBy);
 			} else {
 				start = Math.max(0, start - this.expandReadsBy);
 			}
 
 			if (seq == null) {
 				System.err.println("Collecting reads mapped to reference sequence " + refName);
 				seq = seqDB.getSequence(refName);
 			} else if (seq.getName().equals(refName)) {
 				StrandedFeature.Template template = new StrandedFeature.Template();
 				template.source = source;
 				template.type = type;
 				template.location = new RangeLocation(start, end);
 				template.strand = isPosStrand ? StrandedFeature.POSITIVE : StrandedFeature.NEGATIVE;
 
 				seq.createFeature(template);
 			} else {
 				assert !seenRefSeqNames.contains(refName);
 
 				this.processSequence(seq);
 
 				System.err.println("Collecting reads mapped to reference sequence " + refName);
 				seq = seqDB.getSequence(refName);
 			}
 		}
 
 		System.err.printf("Excluded %d reads (%.2f%)%n", excludedReads, (double)excludedReads / (double)readCount * 100.0);
 	}
 
 	private void processSequence(Sequence seq) {
 
 		System.err.println("Processing "+ seq.getName());
 	}
 }
