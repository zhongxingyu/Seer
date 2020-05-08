 package net.derkholm.nmica.extra.app.seq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.BioException;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SequenceIterator;
 import org.biojava.bio.seq.io.SeqIOTools;
 import org.biojava.bio.symbol.Symbol;
 import org.biojava.bio.symbol.SymbolList;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @NMExtraApp(launchName = "nmgccontent", vm = VirtualMachine.SERVER)
 @App(overview = "Calculate the GC content of sequence windows", generateStub = true)
 public class CalculateGCContent {
 
 	private File seqsFilename;
 	private int winSize;
 
 	@Option(help="Sequence filename")
 	public void setSeqs(File f) {
 		this.seqsFilename = f;
 
 	}
 	
 	@Option(help="Window size")
 	public void setWindowSize(int winSize) {
 		this.winSize = winSize;
 	}
 	
 	public void main(String[] args) throws FileNotFoundException, BioException {
 		BufferedReader br = new BufferedReader(new FileReader(seqsFilename));
 		SequenceIterator stream = SeqIOTools.readFastaDNA(br);
 		
 		
 		while (stream.hasNext()) {
 			Sequence seq = stream.nextSequence();
 			int len = seq.length();
 			
 			for (int i = 1; i <= (len-winSize); i++) {
 				SymbolList symList = seq.subList(i, i+winSize);
 				
 				int gc = 0;
 			    for (int pos = 1; pos <= winSize; ++pos) {
 					Symbol sym = seq.symbolAt(pos);
 					if (sym == DNATools.g() || sym == DNATools.c()) ++gc;
 			    }
 			    
			    System.out.printf("%d\t%f",i,(double)gc/(double)winSize);
 			}
 		}
 	}
 }
