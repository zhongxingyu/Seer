 /*----------------------------------------------------------------------------*
  * PhazeBasicAoSWriter
  *----------------------------------------------------------------------------*/
 
 package Phaze;
 
 import org.apache.commons.cli.*;
 import java.io.*;
 import java.util.*;
 
 public class PhazeBasicAoSWriter implements PhazeWriter {
 
 	private PhazeBoilerPlate bp_;
 	private PrintWriter file_;
 
 	public PhazeBasicAoSWriter() {
 		bp_ = new PhazeBoilerPlateC();
 	} // PhazeBasicAoSWriter
 
 	public void writeHeader(String inputFile, CommandLine line,
 		Hashtable<String, PhazeStruct> structs) throws Exception {
 
 		/*----------------------------------------------------------------------*
 		 * Keep track of static variables. These will become globals
 		 * in the library output.
 		 *----------------------------------------------------------------------*/
 
 		Set<PhazeVariable> statics = new TreeSet<PhazeVariable>();
 
 		Enumeration e = structs.keys();
 		while(e.hasMoreElements()) {
 			Set<PhazeVariable> vars = structs.get(e.nextElement()).variables();
 
 			Iterator<PhazeVariable> ita = vars.iterator();
 			while(ita.hasNext()) {
 				PhazeVariable var = ita.next();
 
 				if(var.isStatic) {
 					statics.add(var);
 				} // if
 			} // while
 		} // while
 
 		/*----------------------------------------------------------------------*
 		 * Create file and write generic comment
 		 *----------------------------------------------------------------------*/
 
 // FIXME: Move up in the call chain
 		int slash = inputFile.lastIndexOf('/');
 		String inputDir = slash == -1 ? "./" : inputFile.substring(0, slash);
 
 		String path = line.hasOption("d") ?
 			line.getOptionValue("d") : inputDir;
 // FIXME
 
 		file_ = new PrintWriter(path + "/phaze.h");
 
		file_.print(String.format(bp_.genericHeader(inputFile)));
 
 		file_.print("\n#ifndef phaze_h\n");
 		file_.print("#define phaze_h\n\n");
 
 		/*----------------------------------------------------------------------*
 		 *
 		 *----------------------------------------------------------------------*/
 
 /*
 		Iterator<PhazeVariable> ita = statics.iterator();
 		while(ita.hasNext()) {
 			PhazeVariable var = ita.next();
 
 			file_.println(var.staticString());
 		} // while
 */
 
 		/*----------------------------------------------------------------------*
 		 *
 		 *----------------------------------------------------------------------*/
 
 		Set<PhazeVariable> vars = structs.get("cell").variables();
 
 		file_.print(bp_.startComment());
 		file_.println(" * cell_t structure prototype");
 		file_.println(bp_.endComment());
 
 		file_.println("struct cell_t {");
 
 // FIXME: NEED TO HANDLE STORAGE CLASSES
 
 		// check dimension
 		int dim = line.hasOption("D") ?
 			Integer.parseInt(line.getOptionValue("D")) : 3;
 
 		/*----------------------------------------------------------------------*
 		 * Iterate variables
 		 *----------------------------------------------------------------------*/
 
 		Iterator<PhazeVariable> ita = vars.iterator();
 		while(ita.hasNext()) {
 			PhazeVariable var = ita.next();
 
 			switch(var.type) {
 				case pos32:
 				case vec32:
 					file_.println("\tfloat " + var.id + "[" + dim + "];");
 					break;
 				case pos64:
 				case vec64:
 					file_.println("\tdouble " + var.id + "[" + dim + "];");
 					break;
 				default:
 // FIXME: make sure toString gives valid output
 					file_.println("\t" + var.toString());
 					break;
 			} // switch
 		} // while
 
 		file_.println("}; // struct cell_t\n");
 
 		/*----------------------------------------------------------------------*
 		 *
 		 *----------------------------------------------------------------------*/
 
 		vars = structs.get("material").variables();
 
 		file_.print(bp_.startComment());
 		file_.println(" * material_t structure prototype");
 		file_.println(bp_.endComment());
 
 		file_.println("struct material_t {");
 
 // FIXME: NEED TO HANDLE STORAGE CLASSES
 
 		ita = vars.iterator();
 		while(ita.hasNext()) {
 			PhazeVariable var = ita.next();
 
 			switch(var.type) {
 				case pos32:
 				case vec32:
 					file_.println("\tfloat " + var.id + "[" + dim + "];");
 					break;
 				case pos64:
 				case vec64:
 					file_.println("\tdouble " + var.id + "[" + dim + "];");
 					break;
 				default:
 					file_.println("\t" + var.toString());
 					break;
 			} // switch
 		} // while
 
 		file_.println("}; // struct material_t\n");
 
 //
 //
 //
 
 		// static interface
 		file_.print(bp_.staticInterface());
 
 		// header file finalization
 		file_.print("\n#endif // phaze_h\n");
 
 		file_.close();
 	} // writeHeader
 
 } // class PhazeBasicAoSWriter
