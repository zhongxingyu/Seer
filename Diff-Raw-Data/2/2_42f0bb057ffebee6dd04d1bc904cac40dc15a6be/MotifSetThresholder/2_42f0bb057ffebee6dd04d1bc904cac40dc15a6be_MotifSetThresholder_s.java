 package net.derkholm.nmica.extra.app;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.motif.Motif;
 import net.derkholm.nmica.motif.MotifIOTools;
 
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "Set the threshold for ", generateStub = true)
 @NMExtraApp(launchName = "nmthreshold")
 public class MotifSetThresholder {
 
 	private double scoreThreshold=0.0;
 	private Set names = null;
 	private String outFileName;
 	private File inFile;
 	
 	@Option(help="The input motif set file")
 	public void setMotifs(File f) {
 		this.inFile = f;
 	}
 	
 	@Option(help="The score threshold to set")
 	public void setScoreThreshold(double d) {
 		if (Double.isInfinite(d) || Double.isNaN(d)) {
 			System.err.printf("ERROR: invalid scoreThreshold=%e%n",d);
 		}
 		if (d > 0) {
 			System.err.printf("ERROR: scoreThreshold %e > 0%n",d);
 			System.exit(1);
 		}
 		this.scoreThreshold = d;
 	}
 	
 	@Option(help="Names of motifs to set the threshold for. " +
 			"If unspecified, all motifs in the set will be given the specified threshold",
 			optional=true)
 	public void setNames(String[] strings) {
 		this.names = new HashSet();
 		for (String s : strings) {
 			this.names.add(s);
 		}
 	}
 	
 	@Option(help="Output filename",optional=true)
 	public void setOut(String str) {
 		this.outFileName = str;
 	}
 	
 	public void main(String[] args) throws FileNotFoundException, Exception {
 		FileReader reader = null;
 		BufferedReader bufferedReader = null;
 		Motif[] motifs = null;
 		
 		try {
 			reader = new FileReader(inFile);
 			if (reader != null) {
 				bufferedReader = new BufferedReader(reader);
 			}
 			motifs = MotifIOTools.loadMotifSetXML(bufferedReader);
 		} catch (IOException ioe) {
 			System.err.printf("Could not read from file %s %n",inFile.getCanonicalFile());
 			ioe.printStackTrace();
 		} finally {
 			if (bufferedReader != null)
 				bufferedReader.close();
 			if (reader != null)
 				reader.close();
 		}
 		if (motifs == null || motifs.length == 0) {
 			System.err.printf("Could not read motif set from %n");
 		}
 		for (Motif m : motifs) {
			if (this.names.contains(m.getName()) || names == null) {
 				m.setThreshold(scoreThreshold);
 			}
 		}
 		if (outFileName != null) {
 			MotifIOTools.writeMotifSetXML(
 					new BufferedOutputStream(
 							new FileOutputStream(outFileName)), motifs);
 		} else {
 			MotifIOTools.writeMotifSetXML(System.out, motifs);
 		}
 	}
 }
