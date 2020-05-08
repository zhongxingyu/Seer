 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 import java.io.*;
 
 public class xml2ffi {
 	public static void main(String args[]) throws Exception {
 		// check number of arguments
 		if (args.length != 2) {
 			System.out.println("       java xml2ffi path/to/input path/to/output");
 			System.exit(0);
 		}
 		FileReader stgfile = new FileReader("ffi.stg");
 		StringTemplateGroup ffistg = new StringTemplateGroup(stgfile);
 		stgfile.close();
 		translate(new File(args[0]), new File(args[1]), ffistg, ".ffi");
 	}
 
 	public static void translate(File input, File output, StringTemplateGroup stg, String ext) throws Exception {
 		// check whether first and second argument are files or directories
 		if (input.isDirectory()) {
 			for (File infile : input.listFiles()) {
 				File outfile = new File(output, infile.getName());
 				if (infile.isDirectory())
 					outfile.mkdir();
 				translate(infile, outfile, stg, ext);
 			}
 		} else {
 			// ensure correct extension on input and output files
 			if (!input.toString().endsWith(".xml")) {
 				System.out.println(input.toString() + " skipped");
 				return;
 			};
 			if (!output.toString().endsWith(ext)) {
 				String outputfilename = output.toString();
 				outputfilename = outputfilename.substring(0, outputfilename.lastIndexOf("."));
 				output = new File(outputfilename + ext);
 			};
 			System.out.println(input.toString() + " -> " + output.toString());
 
 			// create AST
 			System.out.println("creating abstract syntax tree...");
 			ANTLRFileStream instream = new ANTLRFileStream(input.toString());
 			XMLLexer lexer = new XMLLexer(instream);
 			CommonTokenStream tokens = new CommonTokenStream(lexer);
 			XMLParser parser = new XMLParser(tokens);
			Tree t = (Tree)parser.ffi().tree;
 			// for debugging:
 			//System.out.println(r.tree.toStringTree());
 
 			// optimize AST (multiple passes if needed)
 			FFITreeOpt opt;
 			do {
 				System.out.println("optimization pass...");
 				CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
 				nodes.setTokenStream(tokens);
 				opt = new FFITreeOpt(nodes);
 				t = (Tree)opt.ffi().tree;
 			} while (opt.again);
 
 			// generate ffi code
 			System.out.println("generating ffi file...");
 			CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
 			nodes.setTokenStream(tokens);
 			FFITreeTemplate gen = new FFITreeTemplate(nodes);
 			gen.setTemplateLib(stg);
 			FFITreeTemplate.ffi_return r = gen.ffi();
 			BufferedWriter ffi = new BufferedWriter(new FileWriter(output.toString()));
 			ffi.write(r.st.toString());
 			ffi.close();
 		}
 	}
 
 }
 
