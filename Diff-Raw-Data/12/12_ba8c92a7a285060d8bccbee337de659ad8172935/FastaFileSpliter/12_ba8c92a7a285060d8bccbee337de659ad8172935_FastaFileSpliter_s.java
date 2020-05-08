 package utils;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import fastaIO.FastaMultipleReader;
 import fastaIO.FastaWriter;
 import fastaIO.Pair;
 
 public class FastaFileSpliter {
 
 	public static void main(String[] arg) {
 		FastaMultipleReader fmr = new FastaMultipleReader();
 		FastaWriter fw = new FastaWriter();
 
		//String infile = JOptionPane.showInputDialog(null, "Input File: ",  "Enter Input File", 1);
		//String outdir = JOptionPane.showInputDialog(null, "Output Dir: ",  "Enter Output Dir", 1);
		
		String infile = "C:\\JAvier\\DropBox\\My Dropbox\\Investigacion\\Sandra\\Filogenia SLEV - Mayo 2011\\Datos de Partida\\SLEV ORF nucleotido.fas";
		String outdir = "C:\\JAvier\\DropBox\\My Dropbox\\Investigacion\\Sandra\\Filogenia SLEV - Mayo 2011\\Datos de Partida\\separados";		
		
 		File file = new File(infile);
 		List<Pair<String, String>> fas = null;
 		try {
 			fas = fmr.readFile(file.getCanonicalPath());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
		System.out.println(fas);
		
 		for (Pair<String, String> pair : fas) {
 			String s = pair.getSecond();
 			String d = pair.getFirst();
 
 			String fname = outdir + "\\" + d.substring(0,Math.min(30, d.length()))+ ".fas"; 
 			fname =fname.replaceFirst("/", "_");
 			fname =fname.replaceFirst("/", "_");
 			fw.writeFile(fname, s, d);
 			
 		}
 	}
 }
