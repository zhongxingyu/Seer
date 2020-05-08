 package net.derkholm.nmica.extra.app.seq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import net.derkholm.nmica.apps.MotifFinder;
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.model.genematrix.GeneByGeneMatrix;
 import net.derkholm.nmica.model.genematrix.GeneByGeneMatrixExpansionBundle;
import net.derkholm.nmica.seq.OrderedMapSequenceDB;
 
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.db.HashSequenceDB;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.io.SeqIOTools;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview="Expand a gene-by-gene matrix", generateStub=true)
 @NMExtraApp(launchName = "nmexpandgenematrix", vm = VirtualMachine.SERVER)
 public class ExpandGeneByGeneMatrix {
 	
 	protected File matrixFile;
 	protected File seqCountsFile;
 	protected int[] seqCounts;
	private File seqsFile;
	private OrderedMapSequenceDB seqDB;
 
 	@Option(help="Matrix file")
 	public void setMatrix(File f) {
 		this.matrixFile = f;
 	}
 
 	@Option(help="Sequence counts file")
 	public void setSeqCounts(File f) {
 		this.seqCountsFile = f;
 	}
 	
	@Option(help="Sequence input file")
	public void setSeqs(File f) throws FileNotFoundException, BioException {
		this.seqsFile = f;
		
		this.seqDB = new OrderedMapSequenceDB();
        SequenceIterator si = SeqIOTools.readFasta(new BufferedReader(new FileReader(f)),DNATools.getDNA().getTokenization("token"));
        SequenceDB seqDB = new HashSequenceDB();
        while (si.hasNext()) {seqDB.addSequence(si.nextSequence());}
	}
	
 	public void main(String[] args) throws IOException {
 		System.err.println("Loading gene by gene matrix ");
 		GeneByGeneMatrix m = MotifFinder.loadGeneByGeneMatrix(this.matrixFile, null);
 				
 		this.seqCounts = ExpandGeneByGeneMatrix.loadSeqCounts(this.seqCountsFile);
 		
 		for (int i = 0; i < this.seqCounts.length; i++) {System.err.printf("%d\t",this.seqCounts[i]);}
 		
		GeneByGeneMatrixExpansionBundle expandedMBundle = m.expand(this.seqDB);
 		
 		System.out.println(expandedMBundle.expandedMatrix);
 	}
 	
 	public static int[] loadSeqCounts(File seqCountsFile) throws IOException {
 		List<Integer> seqCountsList = new ArrayList<Integer>();
 		BufferedReader r = new BufferedReader(new FileReader(seqCountsFile));
 		
 		String line = r.readLine();
 		
 		StringTokenizer stringTok = new StringTokenizer(line,"\t");
 		while (stringTok.hasMoreTokens()) {
 			seqCountsList.add(Integer.parseInt(stringTok.nextToken()));
 		}
 		
 		int[] seqCounts = new int[seqCountsList.size()];
 		for (int i = 0; i < seqCounts.length; i++) {seqCounts[i] = seqCountsList.get(i);}
 		
 		return seqCounts;
 	}
 	
 }
