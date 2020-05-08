 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import dfh.grammar.Grammar;
 import dfh.grammar.GrammarException;
 import dfh.grammar.Match;
 import dfh.grammar.Matcher;
 
 /**
  * For making graphs showing the structure of matches. The output must be
  * processed with GraphViz or something else that can make graph images from
  * .dot files.
  * <p>
  * <b>Creation date:</b> Apr 2, 2011
  * 
  * @author David Houghton
  * 
  */
 public class MatchToDot {
 
 	/**
 	 * Applies the grammar to the file, creating a dot file with graphs of all
 	 * matches. The output file is named by appending ".dot" to the name of the
 	 * text file.
 	 * 
 	 * @param args
 	 *            grammar file and text file
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 * @throws GrammarException
 	 */
 	public static void main(String[] args) throws GrammarException,
 			FileNotFoundException, IOException {
 		File[] returnAr = validateArgs(args);
 		File gf = returnAr[0];
 		File inf = returnAr[1];
 		Grammar g = new Grammar(gf);
 		String text = fileToText(inf);
 		Matcher m = g.find(text);
 		StringBuilder b = startDot(inf);
 		Match n;
 		int[] index = { 1 };
 		while ((n = m.match()) != null) {
 			if (!n.zeroWidth())
 				appendGraph(b, n, text, index);
 		}
 		endDot(b);
 		dotFile(inf, b);
 	}
 
 	private static void dotFile(File inf, StringBuilder b) throws IOException {
 		File outf = new File(inf.getPath() + ".dot");
 		BufferedWriter writer = new BufferedWriter(new FileWriter(outf));
 		writer.write(b.toString());
 		writer.close();
 	}
 
 	private static void endDot(StringBuilder b) {
 		b.append("}\n");
 	}
 
 	private static void appendGraph(StringBuilder b, Match n, String text,
 			int[] index) {
 		b.append("subgraph cluster").append(index[0]).append(" {\n");
 		b.append("label = ");
 		b.append(id(cleanText(n, text))).append("\n");
 		// b.append("graph [rankdir=TB]\n").append('\n');
 		Map<Object, String> idMap = new HashMap<Object, String>();
 		Set<String> sameRank = new TreeSet<String>();
 		for (Match m : n.passingMatches(Match.WIDE)) {
 			String id = "n" + index[0]++;
 			idMap.put(m, id);
 			b.append(id).append(' ').append("[label=");
 			b.append(id(m.explicit() ? m.rule().label().toString() : m.rule()
 					.label().id));
 			if (m.explicit())
 				b.append(",shape=box");
 			b.append("]\n");
 			if (m.isTerminal()) {
 				String id2 = "n" + index[0]++;
 				sameRank.add(id2);
 				String s = cleanText(m, text);
 				idMap.put(s, id2);
 				b.append(id2).append(' ').append("[label=");
 				b.append(id(s)).append("]\n");
 				b.append(id).append(" -- ").append(id2).append('\n');
 			} else {
 				b.append(id).append(" -- ");
 				int count = 0;
 				for (Match child : m.children()) {
 					if (!child.zeroWidth())
 						count++;
 				}
 				if (count == 1) {
 					for (Match child : m.children()) {
 						if (!child.zeroWidth()) {
 							b.append(idMap.get(m.children()[0]));
 							break;
 						}
 					}
 				} else {
 					b.append('{');
 					for (Match child : m.children()) {
 						if (!child.zeroWidth()) {
 							String id2 = idMap.get(child);
 							b.append(' ').append(id2);
 						}
 					}
 					b.append('}');
 				}
 				b.append('\n');
 			}
 		}
 		if (sameRank.size() > 1) {
 			b.append("{ rank = same; ");
 			for (String s : sameRank) {
 				b.append(s).append("; ");
 			}
 			b.append("}\n");
 		}
 		b.append("}\n");
 	}
 
 	private static String cleanText(Match n, String text) {
 		String suffix = "(" + n.start() + ", " + n.end() + ")";
 		if (n.zeroWidth())
 			return suffix;
 		String s = text.substring(n.start(), n.end()).trim();
 		if (s.length() == 0)
 			s = "\" \"";
 		else
 			s = s.replaceAll("\\s++", " ");
 		return s + ' ' + suffix;
 	}
 
 	private static StringBuilder startDot(File inf) {
 		StringBuilder b = new StringBuilder();
 		b.append("graph ");
 		b.append(id(inf.getName()));
 		b.append(" {\n");
 		b.append("node [shape=plaintext,fontsize=10]\n");
 		return b;
 	}
 
 	/**
 	 * @param name
 	 * @return GraphViz ID
 	 */
 	private static String id(String name) {
 		StringBuilder b = new StringBuilder();
 		b.append('"');
 		for (char c : name.toCharArray()) {
 			if (c == '\\' || c == '"')
 				b.append('\\');
 			b.append(c);
 		}
 		b.append('"');
 		return b.toString();
 	}
 
 	private static String fileToText(File inf) throws FileNotFoundException,
 			IOException {
 		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
 				inf));
 		int i;
 		StringBuilder b = new StringBuilder();
 		while ((i = bis.read()) > -1) {
 			b.append((char) i);
 		}
 		String text = b.toString();
 		return text;
 	}
 
 	private static File[] validateArgs(String[] args) {
 		if (args.length != 2)
 			usage("two arguments expected");
 		String grammarDoc = args[0];
 		File grammarFile = new File(grammarDoc);
 		if (!(grammarFile.exists() && grammarFile.canRead()))
 			usage(grammarDoc + " must be a readable grammar file");
 		String fileToMatch = args[1];
 		File matchingFile = new File(fileToMatch);
 		if (!(matchingFile.exists() && matchingFile.canRead()))
 			usage(matchingFile + " must be a readable text file");
 		File[] returnAr = { grammarFile, matchingFile };
 		return returnAr;
 	}
 
 	private static void usage(String string) {
 		System.err.println("ERROR: " + string);
 		System.err.println("USAGE: <executable> <grammar file> <text file>");
 		System.exit(1);
 	}
 
 }
