 package io;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.text.html.MinimalHTMLWriter;
 
 import model.CGene;
 import model.Helix;
 
 public class SolutionWriter {

 	public final static String teamId = "Team 5";

	public static void write(List<CGene> genes, String filename)
			throws IOException {
 		PrintWriter out = new PrintWriter(new FileWriter(filename));
 		out.println("> " + teamId);
 		Collections.sort(genes, new Comparator<CGene>() {
 			@Override
 			public int compare(CGene o1, CGene o2) {
 				return o1.minPosition - o2.minPosition;
 			};
 		});
 		for (CGene cGene : genes) {
			out.println(String.format("%d %d %d", cGene.minPosition + 1, cGene.maxPosition + 1, cGene.pairs));
 			Collections.sort(cGene.helixes, new Comparator<Helix>() {
 				@Override
 				public int compare(Helix o1, Helix o2) {
 					return o1.start - o2.start;
 				};
 			});
 			for (Helix helix : cGene.helixes) {
				for (int i = helix.len - 1; i >= 0; --i) {
					out.println(String.format("%d %d", helix.start - i + 1, helix.end + i + 1));
 				}
 			}

 		}

 		out.close();
 	}
 
 	public static void main(String[] args) {
 	}
 
 }
