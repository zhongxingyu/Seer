 package common;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.io.*;
 
 import org.javatuples.*;
 import org.w3c.dom.Node;
 
 import backend.PreprocessorNode;
 
 public class Preprocessor {
 
 	private static final File tmpdir = new File("/tmp");
 
 	/**
 	 * Method generates all combinations of true, false for a given number of elements
 	 * e.g.:
 	 * combinations(2):
 	 * [[true, true], [true,false], [false,true], [false,false]]
 	 * @param n
 	 * @return all combinations as a list of lists of booleans
 	 */
 	public static LinkedList<LinkedList<Boolean>> combinations(int n) {
 		if (n <= 0) {
 			LinkedList<LinkedList<Boolean>> r = new LinkedList<LinkedList<Boolean>>();
 			r.add(new LinkedList<Boolean>());
 			return r;
 		} else {
 			LinkedList<LinkedList<Boolean>> res = new LinkedList<LinkedList<Boolean>>();
 
 			for (LinkedList<Boolean> e: combinations(n-1)) {
 				@SuppressWarnings("unchecked")
 				LinkedList<Boolean> e1 = ((LinkedList<Boolean>)e.clone());
 				@SuppressWarnings("unchecked")
 				LinkedList<Boolean> e2 = ((LinkedList<Boolean>)e.clone());
 				e1.add(true);
 				e2.add(false);
 				res.add(e1);
 				res.add(e2);
 			}
 			return res;
 		}
 	}
 
 	/**
 	 * Method applies the cpp-tool with a given configuration on the input file
 	 * generating the output file
 	 * @param conf
 	 * @param infile
 	 * @param outfile
 	 */
 	public static File run(LinkedList<Pair<Boolean, String>> conf, File infile) {
 		File outfile = null;
 		try {
 			outfile = File.createTempFile("test_se_", ".c", tmpdir);
 			Runtime rt = Runtime.getRuntime();
 			String cppcmd = "cpp";
 			cppcmd += " -CC"; // preserve comments
 			cppcmd += " -P";  // do not create line-markers
 			cppcmd += " -fdirectives-only"; // do not expand macros
 
 			for (Pair<Boolean, String> cp: conf) {
 				if (cp.getValue0()) cppcmd += " -D";
 				else cppcmd += " -U";
 				cppcmd += cp.getValue1();
 			}
 
 			cppcmd += " " + infile.getAbsolutePath(); // input file
 			cppcmd += " " + outfile.getAbsolutePath(); // output file
 
 			Process pr = rt.exec(cppcmd);
 			int exitval = pr.waitFor();
 			if (exitval != 0)
 				System.out.println("preprocessor exited with error code " + exitval);
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			e.printStackTrace();
 		}
 
 		return processCPPOutput(conf, outfile);
 	}
 
 	/**
 	 * This method generates all variants of an annotated input file given a list of configurations confs
 	 * and a list of parameters p
 	 * @param confs
 	 * @param p
 	 * @param infile
 	 * @return
 	 */
 	public static LinkedList<File> runAll(LinkedList<LinkedList<Boolean>> confs, LinkedList<String> p, File infile) {
 		LinkedList<File> res = new LinkedList<File>();
 		LinkedList<Pair<Boolean, String>> cc = null;
 
 		for (LinkedList<Boolean> conf: confs) {
 			cc = Preprocessor.createConfiguration(conf, p);
 			res.add(Preprocessor.run(cc, infile));
 		}
 
 		return res;
 	}
 
 	/**
 	 * This method processes the output of a cpp run (i.e., remove #define macros at start and
 	 * add the configuration #if)
 	 * @param c configuration of the program variant
 	 * @param outfile file that contains unpreprocessed cpp output
 	 * @return
 	 */
 	private static File processCPPOutput(LinkedList<Pair<Boolean, String>> conf, File outfile) {
 		File res = null;
 		StringBuffer buf = new StringBuffer();
 		buf.append("#if ");
 		Iterator<Pair<Boolean, String>> i = conf.iterator();
 
 		while (i.hasNext()) {
 			Pair<Boolean, String> e = i.next();
 			if (!e.getValue0()) buf.append("!");
 			buf.append("defined(" + e.getValue1() + ")");
 
 			if (i.hasNext()) buf.append(" && ");
 		}
 
 		try {
 			res = File.createTempFile("test_seprocessed_", ".c", tmpdir);
 			BufferedReader br = new BufferedReader(new FileReader(outfile));
 			BufferedWriter bw = new BufferedWriter(new FileWriter(res));
 			String line = null;
 			bw.write(buf.toString()+"\n");
 
 			while ((line = br.readLine()) != null) {
 				// beware this might cause a problem
 				// cpp run generates a list of #defines and attaches them prior to
 				// the preprocessed code, if a user uses a #define on his own this
 				// #define is deleted just like the generated ones.
 				if (line.startsWith("#"))
 					continue;
 				bw.write(line+"\n");
 			}
 
 			bw.write("#endif\n");
 			bw.close();
 
 		} catch (IOException e) {
 			System.out.println(e.toString());
 			e.printStackTrace();
 		}
 
 		return res;
 	}
 
 	/**
 	 * Method creates a configuration for the preprocessor for
 	 * @param c
 	 * @param p
 	 * @return
 	 */
 	public static LinkedList<Pair<Boolean, String>> createConfiguration(LinkedList<Boolean> c, LinkedList<String> p) {
 		LinkedList<Pair<Boolean, String>> res = new LinkedList<Pair<Boolean, String>>();
 
 		Iterator<Boolean> i1 = c.iterator();
 		Iterator<String> i2 = p.iterator();
 		while (i1.hasNext() && i2.hasNext()) {
 			res.add(new Pair<Boolean, String>(i1.next(), i2.next()));
 		}
 
 		return res;
 	}
 
 
 	/**
 	 * Method puts the code of pnodes in a file and returns the file-handle.
 	 * @param pnodes
 	 * @return
 	 */
 	public static File writeCode2File(PreprocessorNode pnodes[]) {
 
 		File res = null;
 		try {
 			res = File.createTempFile("test_expanded_", ".c", tmpdir);
 
 			BufferedWriter ofd = new BufferedWriter(new FileWriter(res));
 			Node ifdef = pnodes[0].getNode();
 			Node endif = pnodes[1].getNode();
 			Node parentnode = ifdef;
 
 			while (!(parentnode.getNodeName().startsWith("if")
 				|| parentnode.getNodeName().startsWith("switch")))
 				parentnode = parentnode.getParentNode();
				
 			ofd.write(parentnode.getTextContent()+"\n");
 
 			if (parentnode.getNodeName().startsWith("if"))
 				ofd.write(endif.getTextContent()+"\n");
 
 			ofd.close();
 		} catch (IOException e) {
 			System.out.println(e.toString());
 			e.printStackTrace();
 		}
 
 		return res;
 	}
 
 	/**
 	 * Method to create a temporary file and put String n in it.
 	 * @param n
 	 * @return
 	 */
 	public static File writeCode2File(String n) {
 		File res = null;
 		try {
 			res = File.createTempFile("test_unprocessed_", ".c", tmpdir);
 
 			BufferedWriter ofd = new BufferedWriter(new FileWriter(res));
 			ofd.write(n);
 			ofd.close();
 		} catch (IOException e) {
 			System.out.println(e.toString());
 			e.printStackTrace();
 		}
 
 		return res;
 	}
 }
