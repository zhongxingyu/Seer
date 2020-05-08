 package eu.whrl.aottracegen;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import eu.whrl.aottracegen.exceptions.ITraceGeneratorFaultException;
 
 public class ITraceGenerator {
 	private List<ASMTrace> asmTraces;
 	
 	private boolean prepared;
 	private FileWriter writer;
 	
 	public ITraceGenerator() {
 		asmTraces = new ArrayList<ASMTrace>();
 	}
 	
 	public void loadAsmFiles(CodeGenContext context, String[] asmFileNames) throws ITraceGeneratorFaultException {
 		int i = 0;
 		for (String asmFileName : asmFileNames) {
 			context.setCurrentTraceIndex(i);
 			loadAsmFile(context, asmFileName);
 			i++;
 		}
 		
 	}
 
 	private void loadAsmFile(CodeGenContext context, String asmFileName) throws ITraceGeneratorFaultException {
 		ASMTrace asmTrace = new ASMTrace();
 	
 		asmTrace.setTraceBody(extractTraceBody(context, asmFileName));
 		asmTrace.cleanupTrace(context);
 		asmTraces.add(asmTrace);
 	}
 
 	/*
 	 * Given a filename of a generated .S file, extracts solely the body of the 'trace' function,
 	 * and returns this body as an array of strings, one string for each line.
 	 */
 	private List<String> extractTraceBody(CodeGenContext context, String asmFileName) throws ITraceGeneratorFaultException {
 		final String startMarker = "trace:";
 		final String endMarker = "\t.size\ttrace, .-trace";
 		
 		List<String> traceBody = new ArrayList<String>();
 		
 		try {
 			File file = new File(asmFileName);
 			BufferedReader buff = new BufferedReader(new FileReader(file));
 			
 			String line;
 			boolean inTraceBody = false;
 			
 			while (buff.ready()) {
 				line = buff.readLine();
 				
 				if (line.equals(endMarker)) {
 					inTraceBody = false;
 				}
 				
 				if (inTraceBody) {
 					traceBody.add(line);
 				}
 				
 				if (line.equals(startMarker)) {
 					inTraceBody = true;
 					// Skip past the two @ lines...
 					buff.readLine();
 					buff.readLine();
 				}
 			}
 			
 			buff.close();
 		} catch (FileNotFoundException e) {
 			System.err.println("Couldn't find the compiled assembly file: " + asmFileName);
 			throw new ITraceGeneratorFaultException();
 		} catch (IOException e) {
 			System.err.println("Couldn't read the compiled assembly file: " + asmFileName);
 			throw new ITraceGeneratorFaultException();
 		}
 		
 		return traceBody;
 	}
 
 	public void createITrace(CodeGenContext context, String iTraceName) throws ITraceGeneratorFaultException {
 		prepare(iTraceName);
 		generate(context);
 		finish();
 	}
 	
 	
 
 	public void prepare(String name) {
 		try {
 			File cFile = new File(name);
 			writer = new FileWriter(cFile);			
 			prepared = true;	
 		} catch (IOException e) {
 			System.err.println("Couldn't open injectable trace file for writing!");
 		}
 	}
 	
 	/*
 	 * Closes the file we've been writing C to.
 	 */
 	public void finish() {
 		if (!prepared) {
 			return;
 		}
 		
 		try {
 			writer.close();
 		} catch (IOException e) {
 			System.err.println("Couldn't close injectable trace file?");
 		}
 	}
 	
 	private void generate(CodeGenContext context) throws ITraceGeneratorFaultException {
 		if (!prepared) {
 			System.err.println("Injectable trace generator wasn't prepared?");
 			throw new ITraceGeneratorFaultException();
 		}
 		
 		// Reset the context, just in case.
 		context.setCurrentTraceIndex(0);
 	
 		try {
 			
 			emitStart(writer);
 				
 			emitTables(writer, context);
 			writer.write("\n");
 			
 			emitChainingCells(writer, context);
 			writer.write("\n");
 			
 			emitTraces(writer, context);
 			
 		} catch (IOException e) {
 			System.err.println("Couldn't write to injectable trace file!");
 			throw new ITraceGeneratorFaultException();
 		}
 	}
 	
 	private void emitStart(Writer writer) throws IOException {
 		
 		writer.write("\t.text\n");
 		writer.write("\t.global dvmITraceStartTable\n");
 		writer.write("\t.global dvmITraceEndTable\n");
 		writer.write("\t.global dvmITraceBasePCTable\n");
 		writer.write("\t.global dvmITraceChainingCellsTable\n");
 		writer.write("\t.global dvmITraceLiteralPoolTable\n");
 		writer.write("\n");
 		writer.write("\t.syntax unified\n");
 		writer.write("\t.thumb\n");
 		
 	}
 	
 	private void emitTables(Writer writer, CodeGenContext context) throws IOException {
 		int[] traceEntries = new int[context.traceEntryAddresses.size()];
 		int idx = 0;
 		for (Integer traceEntry : context.traceEntryAddresses) {
 			traceEntries[idx] = traceEntry.intValue();
 			idx++;
 		}
 		
 		writer.write("dvmITraceStartTable:\n");
 		for (int traceEntry : traceEntries) {
 			writer.write(String.format("\t.word ITrace_0x%x_Start\n", traceEntry));
 		}
 		writer.write("dvmITraceEndTable:\n");
 		for (int traceEntry : traceEntries) {
 			writer.write(String.format("\t.word ITrace_0x%x_End\n", traceEntry));
 		}
 		writer.write("dvmITraceBasePCTable:\n");
 		for (int traceEntry : traceEntries) {
 			writer.write(String.format("\t.word ITrace_0x%x_BasePC\n", traceEntry));
 		}
 		writer.write("dvmITraceChainingCellsTable:\n");
 		for (int traceEntry : traceEntries) {
 			writer.write(String.format("\t.word ITrace_0x%x_ChainingCells\n", traceEntry));
 		}
 		writer.write("dvmITraceLiteralPoolTable:\n");
 		for (int traceEntry : traceEntries) {
 			writer.write(String.format("\t.word ITrace_0x%x_LiteralPool\n", traceEntry));
 		}
 		
 	}
 	
 	private void emitChainingCells(Writer writer, CodeGenContext context) throws IOException {
 		for (int traceIdx = 0; traceIdx < context.traces.size(); traceIdx++) {
 			context.setCurrentTraceIndex(traceIdx);
 			Trace curTrace = context.getCurrentTrace();
 			
 			writer.write(String.format("ITrace_0x%x_ChainingCells:\n", curTrace.addresses[0]));
 			
 			for (int successor : curTrace.successors) {
				writer.write(String.format("\t.word LT0x%x_CC_0x%x_value\n", curTrace.addresses[0], successor));
 			}
 		}
 	}
 	
 	private void emitTraces(Writer writer, CodeGenContext context) throws IOException {
 		for (int traceIdx = 0; traceIdx < context.traces.size(); traceIdx++) {
 			context.setCurrentTraceIndex(traceIdx);
 			emitTrace(writer, context);
 			writer.write("\n");
 		}
 	}
 	
 	private void emitTrace(Writer writer, CodeGenContext context) throws IOException {
 		Trace curTrace = context.getCurrentTrace();
 		ASMTrace curAsmTrace = asmTraces.get(context.getCurrentTraceIndex());
 		int traceEntry = curTrace.addresses[0];
 		
 		// start of the trace
 		writer.write(String.format("ITrace_0x%x_Start:\n", traceEntry));
 		writer.write("\n");
 		
 		// trace body - start off with putting fp, self and litpool pointers in the right place
 		writer.write("\tmov\tr0, r5\n");
 		writer.write("\tmov\tr1, r6\n");
 		if (curTrace.meta.literalPoolSize > 0) {
 			writer.write(String.format("\tadr.w\tr2, ITrace_0x%x_LiteralPool\n", curTrace.addresses[0]));
 		}
 		// and the actual trace body now...
 		writer.write(curAsmTrace.getFullStringTraceBody());
 		writer.write("\n");
 		
 		// its literal pool
 		writer.write(String.format("ITrace_0x%x_LiteralPool:\n", traceEntry));
 		for (int litPoolIdx = 0; litPoolIdx < curTrace.meta.literalPoolSize; litPoolIdx++) {
 			writer.write("\t.word 0x00000000\n");
 		}
 		writer.write("\n");
 		
 		// base pc location
 		writer.write(String.format("ITrace_0x%x_BasePC:\n", traceEntry));
 		writer.write("\t.word 0x00000000\n");
 		writer.write("\n");
 		
 		// exception handlers
 		for (Integer exceptionCodeAddress : curTrace.meta.codeAddressesRaisingExceptions) {
 			writer.write(String.format("LT0x%x_EH_0x%x:\n", traceEntry, exceptionCodeAddress.intValue()));
			writer.write(String.format("\tadr.w\tr0, ITrace_0x%x_BasePC\n", traceEntry));
 			writer.write("\tldr\tr0, [r0]\n");
 			writer.write(String.format("\tadd\tr0, r0, #%d\n", exceptionCodeAddress.intValue()*2));
 			writer.write("\tldr\tr1, [r6, #108]\n");
 			writer.write("\tblx\tr1\n");
 		}
 		writer.write("\n");
 		
 		// chaining cells
 		for (int successor : curTrace.successors) {
 			writer.write("\t.align 4\n");
 			writer.write(String.format("LT0x%x_CC_0x%x:\n", traceEntry, successor));
 			writer.write(String.format("\tb\tLT0x%x_CC_0x%x_next\n", traceEntry, successor));
 			writer.write("\torrs\tr0, r0\n");
 			writer.write(String.format("LT0x%x_CC_0x%x_next:\n", traceEntry, successor));
 			writer.write("\tldr\tr0, [r6, #100]\n");
 			writer.write("\tblx\tr0\n");
 			writer.write(String.format("LT0x%x_CC_0x%x_value:\n", traceEntry, successor));
 			writer.write("\t.word 0x00000000\n");
 		}
 		writer.write("\n");
 		
 		// end of the trace
 		writer.write(String.format("ITrace_0x%x_End:\n", traceEntry));
 		writer.write("\t.word 0x00000000\n");
 	}
 }
