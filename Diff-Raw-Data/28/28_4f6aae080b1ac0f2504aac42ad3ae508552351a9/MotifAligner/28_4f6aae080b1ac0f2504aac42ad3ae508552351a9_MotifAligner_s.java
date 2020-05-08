 package net.derkholm.nmica.extra.app;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.motif.comparison.BLiCMotifComparitor;
 import net.derkholm.nmica.extra.motif.comparison.KullbackLeiblerDifferenceMotifComparitor;
 import net.derkholm.nmica.model.metamotif.MetaMotif;
 import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
 import net.derkholm.nmica.motif.Motif;
 import net.derkholm.nmica.motif.MotifComparitorIFace;
 import net.derkholm.nmica.motif.MotifIOTools;
 import net.derkholm.nmica.motif.SquaredDifferenceMotifComparitor;
 import net.derkholm.nmica.motif.align.MotifAlignment;
 
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview="A tool for aligning a set of motifs and output the alignment", 
 	generateStub=true)
 @NMExtraApp(launchName="nmalign", vm=VirtualMachine.SERVER)
 public class MotifAligner {
 	
 	private File motifs;
 	private String outputType = "align_cons";
 	private String outFile;
 	private MotifComparitorIFace mc = SquaredDifferenceMotifComparitor.getMotifComparitor();
 	private int minColPerPos = 2;
 	
 	@Option(help="Input motifset")
 	public void setMotifs(File motifs) {
 		this.motifs = motifs;
 	}
 	
 	@Option(help="Output file",optional=true)
 	public void setOut(String outFile) {
 		this.outFile = outFile;
 	}
 	
 	@Option(help="Minimum number of columns per position " +
 			"to allow it to make it to output (default=2)",optional=true)
 	public void setMinCols(int i) {
 		this.minColPerPos = i;
 	}
 	
 	
 	@Option(help="Distance metric:sqdiff2.5(default)|sqdiff|kd|blic",optional=true)
 	public void setDist(String dist) {
 		if (!dist.equals("sqdiff2.5") &!
 				dist.equals("sqdiff") &!
 				dist.equals("kd") &!
 				dist.equals("blic"))
 			return;
 		
 		if (dist.equals("sqdiff2.5"))
 			mc = SquaredDifferenceMotifComparitor.getMotifComparitor();
 		else if (dist.equals("sqdiff"))
 			mc = SquaredDifferenceMotifComparitor.getCarthesianMotifComparitor();
 		else if (dist.equals("kd"))
 			mc = KullbackLeiblerDifferenceMotifComparitor.getMotifComparitor();
 		else if (dist.equals("blic"))
 			mc = BLiCMotifComparitor.getMotifComparitor();
 		else {
 			System.err.println("Incorrect distance metric type : " + dist);
 			System.exit(1);
 		}
 	}
 	
 	@Option(help="Output type (default=align_cons): avg|metamotif|cons|align_cons",optional=true)
 	public void setOutputType(String type) {
 		if (!type.equals("avg") &! 
 				type.equals("all") &!
 				type.equals("metamotif") &! 
 				type.equals("cons") &!
 				type.equals("align_cons")) {
 			System.err.println("Allowed output types: all|avg|metamotif|cons|align_cons (default=align_cons)");
 			System.exit(1);
 		}
 		this.outputType = type;
 	}
 	
 	public void main(String[] args) throws FileNotFoundException, Exception {
 		FileReader motifFileReader = new FileReader(this.motifs);
 		Motif[] motifs = MotifIOTools.loadMotifSetXML(motifFileReader);
 		if (motifFileReader != null) motifFileReader.close();
 		
 		if (motifs.length == 1) {
 			System.err.println(
 				"WARNING! The supplied motif set file contains only one motif.\n");
 			System.exit(1);
 		}
 		
 		MotifAlignment alignment 
 			= new MotifAlignment(motifs, mc);
 		
 		alignment = new MotifAlignment(alignment.motifs(), mc);
 		alignment = new MotifAlignment(alignment.motifs(), mc);
 		alignment = alignment.alignmentWithZeroOffset();
 		//if (minColPerPos > 1) {
 		//	alignment = alignment.trimToColumnsPerPosition(minColPerPos);
 		//}
 		
 		alignment.setName(this.motifs.getName().replace("\\.xms", "")+"_alignment");
 		
 		if (!outputType.equals("align_cons")) {
 			System.err.println(alignment.alignmentConsensusString());
 		}
 		
 		if (outputType.equals("avg"))
 			MotifIOTools.writeMotifSetXML(
 					System.out,
 					new Motif[] {alignment.averageMotif()});
 		if (outputType.equals("all"))
 			MotifIOTools.writeMotifSetXML(
 					System.out, 
 					alignment.motifs());
 		else if (outputType.equals("metamotif")) {
 			//System.err.println(alignment.alignmentConsensusString());
 			MetaMotifIOTools.
 				writeMetaMotifSetToMotifSetWithAnnotations(
 					System.out, 
 					new MetaMotif[] {alignment.metamotif(true)});
 			
 		} else if (outputType.equals("cons"))
 			System.out.println(alignment.consensusString());
 		else if (outputType.equals("align_cons"))
 			System.out.println(alignment.alignmentConsensusString());
 
 	}
 }
