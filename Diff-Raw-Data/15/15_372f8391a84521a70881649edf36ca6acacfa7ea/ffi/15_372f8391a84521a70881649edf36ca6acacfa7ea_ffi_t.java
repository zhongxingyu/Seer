 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 import java.io.*;
 
 public class ffi {
 	public static void main(String args[]) throws Exception {
 		// check number of arguments
 		if (args.length != 1) {
 			System.out.println("       java ffi path/to/input");
 			System.exit(0);
 		}
 		FileReader stgfile = new FileReader("ffi.stg");
 		StringTemplateGroup ffistg = new StringTemplateGroup(stgfile);
 		stgfile.close();
 		parse(new File(args[0]), ffistg);
 	}
 
 	public static void parse(File input, StringTemplateGroup stg) throws Exception {
 		// check whether first and second argument are files or directories
 		if (input.isDirectory()) {
 			for (File infile : input.listFiles()) {
 				parse(infile, stg);
 			}
 		} else {
 			// ensure correct extension on input and output files
 			if (!input.toString().endsWith(".ffi")) {
 				System.out.println(input.toString() + " skipped");
 				return;
 			};
 			System.out.println("parsing " + input.toString() + "...");
 
 			// create AST
			Tree t = null;
 			System.out.println("creating abstract syntax tree...");
 			ANTLRFileStream instream = new ANTLRFileStream(input.toString());
 			FFILexer lexer = new FFILexer(instream);
 			CommonTokenStream tokens = new CommonTokenStream(lexer);
 			FFIParser parser = new FFIParser(tokens);
			t = (Tree)parser.ffi().tree;
			if (parser.getNumberOfSyntaxErrors() > 0) {
				System.exit(-1);
			}
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
				if (opt.getNumberOfSyntaxErrors() > 0) {
					System.exit(-1);
				}
 			} while (opt.again);
 
 			// generate ffi code
 			System.out.println("generating ffi file...");
 			CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
 			nodes.setTokenStream(tokens);
 			FFITreeTemplate gen = new FFITreeTemplate(nodes);
 			gen.setTemplateLib(stg);
 			FFITreeTemplate.ffi_return r = gen.ffi();
			if (gen.getNumberOfSyntaxErrors() > 0) {
				System.exit(-1);
			}
 			//BufferedWriter ffi = new BufferedWriter(new FileWriter(output.toString()));
 			//ffi.write(r.st.toString());
 			//ffi.close();
 			System.out.println(r.st.toString());
 		}
 	}
 
 }
 
