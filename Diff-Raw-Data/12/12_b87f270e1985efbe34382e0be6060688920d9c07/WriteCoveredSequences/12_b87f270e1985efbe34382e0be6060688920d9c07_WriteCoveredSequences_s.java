 package net.derkholm.nmica.extra.app.seq;
 
 import gfftools.GFFUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.BioException;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.program.gff.SimpleGFFRecord;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SequenceIterator;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.db.HashSequenceDB;
 import org.biojava.bio.seq.db.SequenceDB;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.LocationTools;
 import org.biojava.bio.symbol.RangeLocation;
 import org.biojavax.bio.seq.RichSequence;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 import org.bjv2.util.cli.UserLevel;
 
 @App(overview = "Write out sequence regions covered by GFF features", generateStub = true)
 @NMExtraApp(launchName = "nmcoveredseq", vm = VirtualMachine.SERVER)
 public class WriteCoveredSequences {
     public static enum Format {
     	GFF,
     	FASTA,
     	TABLE
     }
 	
 	private boolean negate;
 	private File gffFile;
 	private File seqFile;
 	
 	private Format outputFormat = Format.FASTA;
 	private boolean validate = true;
 	private int filterAboveLength;
 	private HashSequenceDB seqDB;
 
 	@Option(help="Input feature file")
 	public void setFeatures(File f) {
 		this.gffFile = f;
 	}
 	
 	@Option(help="Input sequence file. Needed if you want to use -format fasta and/or -negate.", optional=true)
 	public void setSeqs(File f) throws FileNotFoundException, BioException {
 		this.seqFile = f;
 		
 		this.seqDB = new HashSequenceDB();
 		for (SequenceIterator si = RichSequence
 				.IOTools
 					.readFastaDNA(
 						new BufferedReader(
 							new FileReader(seqFile)), null); si.hasNext();) {
 			seqDB.addSequence(si.nextSequence());
 		}
 	}
 	
 	@Option(help="Output format: fasta|gff", optional=true)
 	public void setFormat(Format format) {
 		this.outputFormat = format;
 	}
 	
 	@Option(help="Negate the output, i.e. output sequences that are NOT covered by the features.", optional=true)
 	public void setNegate(boolean b) {
 		this.negate = b;
 	}
 	
 	@Option(help="Validate input (check that sequence identifiers match)", optional=true, userLevel = UserLevel.DEBUG)
 	public void setValidate(boolean b) {
 		this.validate = b;
 	}
 	
 	@Option(help="Filter out features above specified maximum length", optional=true)
 	public void setFilterFeaturesAboveLength(int i) {
 		if (i < 0) {
 			System.err.println("-filterFeaturesAboveLength value should be > 0");
 			System.exit(2);
 		}
 		this.filterAboveLength = i;
 	}
 
 	public void main(String[] args)
 	throws Exception
 	{   
 		if (seqFile == null) {
 			if (negate) {
 				System.err.println("You need to specify input sequences with -seqs for negated output.");
 				System.exit(2);
 			}
 			if (outputFormat == Format.FASTA) {
 				System.err.println("You need to specify input sequences with -seqs when you want to output sequences (-format fasta).");
 			}
 		}
 		
 		Map<String,Location> locs = GFFUtils.gffToLocationMap(gffFile);
 		
 
 		
 		if (validate) {
 			WriteCoveredSequences.validateGFFSequenceIdentifiersAgainstSequences(locs,seqDB);
 		}
 		
 		if (filterAboveLength > 0) {
 			Set<String> keys = locs.keySet();
 			for (String str : keys) {
 				Location loc = locs.get(str);
 				if (loc.getMax() - loc.getMin() > filterAboveLength) {
 					System.err.printf("Filtered feature that spans %d-%d%n",
 							loc.getMin(),
 							loc.getMax());
 					locs.remove(str);
 				}
 			}
 		}
 		
 		for (SequenceIterator si = seqDB.sequenceIterator(); si.hasNext();) {
 			Sequence seq = si.nextSequence();
 			Location loc = locs.get(seq.getName());
 			
 			org.biojava.bio.seq.StrandedFeature.Strand strand = StrandedFeature.UNKNOWN;
 
 			
 			GFFWriter writer = null;
 			if (outputFormat == Format.GFF) {
 				writer = new GFFWriter(new PrintWriter(System.out));
 			}
 			
 			if (loc == null && negate) {
 				if (outputFormat == Format.FASTA) {
 					RichSequence.IOTools.writeFasta(System.out, seq, null);					
 				} else {
 					for (Iterator<?> li = loc.blockIterator(); li.hasNext(); ) {
 						Location l = (Location)li.next();
 						GFFRecord r = new SimpleGFFRecord(
 								seq.getName(),
 								"nmcoveredseq",
 								"covered",
 								l.getMin(),
 								l.getMax(),
 								Double.NaN,
 								strand,
 								0,
 								"",new HashMap<Object, Object>());
 						
 						writer.recordLine(r);
 					}
 				}
 			} else if (loc == null &! negate) {
 				//do nothing (nothing was covered by a location)
 			} else {
 				Location wanted;
 				if (negate) {
 					wanted = LocationTools.subtract(new RangeLocation(1, seq.length()), loc);
 				} else {
 					wanted = loc;
 				}
 				
 				for (Iterator<?> bi = wanted.blockIterator(); bi.hasNext(); ) { 
 					Location wl = (Location) bi.next();
 					
 					if (outputFormat == Format.FASTA) {
 						RichSequence.IOTools.writeFasta(System.out, new SimpleSequence(
 								seq.subList(wl.getMin(), wl.getMax()),
 								null,
 								String.format("%s_|%d-%d|", seq.getName(), wl.getMin(), wl.getMax()),
 								Annotation.EMPTY_ANNOTATION
 						), null);						
 					} else {
 						System.err.printf("%s %s %s %s%n",seq,seq.getName(),wl,strand);
						GFFRecord r = new SimpleGFFRecord(seq.getName(),"nmcoveredseq","covered",wl.getMin(),wl.getMax(),Double.NaN,strand,0,"",null);
 						writer.recordLine(r);
 					}
 				}
 			}   
 			
 			if (writer != null) writer.endDocument();
 		}   
 	}
 
 	public static void validateGFFSequenceIdentifiersAgainstSequences(Map<String,Location> locs, SequenceDB seqDB) 
 		throws SequenceIdentifierValidationException, BioException {
 		
 		Set<String> gffIdentifiers = locs.keySet();
 		Set<String> seqIdentifiers = new TreeSet<String>();
 		for (SequenceIterator si = seqDB.sequenceIterator(); si.hasNext();) {
 			seqIdentifiers.add(si.nextSequence().getName());
 		}
 		
 		boolean validationIssuesFound = false;
 		int i = 0;
 		for (String gffId : gffIdentifiers) {
 			if (!seqIdentifiers.contains(gffId)) {
 				i+=1;
 				System.err.println(
 						"VALIDATION ERROR: sequence identifier " + gffId + 
 						" from the feature input file doesn't match any of the sequence identifiers " +
 						"in the sequence input file.");
 				validationIssuesFound = true;
 			}
 		}
 		if (validationIssuesFound) {
 			throw new SequenceIdentifierValidationException("" + i + " validation issues found");
 		}
 	}   
 }
