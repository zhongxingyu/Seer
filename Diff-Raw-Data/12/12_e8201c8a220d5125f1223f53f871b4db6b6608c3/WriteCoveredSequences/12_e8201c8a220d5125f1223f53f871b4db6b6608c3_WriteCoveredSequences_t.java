 package net.derkholm.nmica.extra.app;
 
 import gfftools.GFFUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SequenceIterator;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.seq.io.FastaFormat;
 import org.biojava.bio.seq.io.SeqIOTools;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.LocationTools;
 import org.biojava.bio.symbol.RangeLocation;
 import org.biojavax.bio.seq.RichSequence;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "Write out sequence regions covered by GFF features", generateStub = true)
 @NMExtraApp(launchName = "nmcoveredseq", vm = VirtualMachine.SERVER)
 public class WriteCoveredSequences {
 	private boolean negate;
 	private File gffFile;
 	private File seqFile;
 
 	@Option(help="Input sequence file(s)")
 	public void setFeatures(File f) {
 		this.gffFile = f;
 	}
 	
 	@Option(help="Input sequence file(s)")
 	public void setSeqs(File f) {
 		this.seqFile = f;
 	}
 	
 	@Option(help="Negate the output, i.e. output sequences that are NOT covered by the features.")
 	public void setNegate(boolean b) {
 		this.negate = b;
 	}
 
 	public void main(String[] args)
 	throws Exception
 	{   
 
 		Map<String,Location> locs = GFFUtils.gffToLocationMap(gffFile);
 		for (SequenceIterator si = RichSequence
 									.IOTools
 										.readFastaDNA(
 											new BufferedReader(
 												new FileReader(seqFile)), null); si.hasNext();) {
 			Sequence seq = si.nextSequence();
 			Location loc = locs.get(seq.getName());
 			
 			if (loc == null && negate) {
 				RichSequence.IOTools.writeFasta(System.out, seq, null);
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
 					
 					RichSequence.IOTools.writeFasta(System.out, new SimpleSequence(
 							seq.subList(wl.getMin(), wl.getMax()),
 							null,
 							String.format("%s_%d-%d", seq.getName(), wl.getMin(), wl.getMax()),
 							Annotation.EMPTY_ANNOTATION
 					), 
					null);
 				}   
 			}   
 		}   
 	}   
 }
