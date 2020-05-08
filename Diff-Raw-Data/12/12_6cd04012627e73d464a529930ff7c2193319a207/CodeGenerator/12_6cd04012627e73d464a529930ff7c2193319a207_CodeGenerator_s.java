 package eu.whrl.aottracegen;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import eu.whrl.aottracegen.exceptions.CGeneratorFaultException;
 import eu.whrl.aottracegen.exceptions.CompilationException;
 import eu.whrl.aottracegen.exceptions.ITraceDescGeneratorFaultException;
 import eu.whrl.aottracegen.exceptions.ITraceGeneratorFaultException;
 import eu.whrl.aottracegen.exceptions.UnimplementedInstructionException;
 
 public class CodeGenerator {
 	public CodeGenerator() { }
 	
 	/*
 	 * This method is called to generate the C, ASM, and then injectable ASM and trace description files.
 	 * Everything in the CodeGenContext will have been already set up at this point.
 	 */
 	public void generateCodeFromContext(CodeGenContext context) {
 		int numTraces = context.traces.size();
 		String[] cTraceFileNames = new String[numTraces];
 		String[] asmTraceFileNames = new String[numTraces];
 		
 		// Generate all the file names.
 		for (int i = 0; i < numTraces; i++) {
 			cTraceFileNames[i] = String.format("trace_0x%x.c", context.traceEntryAddresses.get(i));
 			asmTraceFileNames[i] = String.format("trace_0x%x.S", context.traceEntryAddresses.get(i));
 		}
 		
 		try {
 			
 			for (int i = 0; i < numTraces; i++) {
 				
 				// Makes sure the generateC() and compileC() functions use the correct trace!
 				context.setCurrentTraceIndex(i);
 				System.out.println(String.format("Handling trace at 0x%x", context.getCurrentTraceEntryAddress()));
 				
 				// Do the generation and compilation.
 				generateC(context, cTraceFileNames[i]);
 				compileC(cTraceFileNames[i], asmTraceFileNames[i]);
 				
 			}
 			
 			// Take the list of generated asm files, and produce a 'injectable trace' asm file.
			emitITrace(context, asmTraceFileNames, "InjectableTrace.S");
 			
 			// Produce the trace description file that the VM will read to know when to inject traces.
 			emitITraceDesc(context, "trace_inject_desc.cfg");
 			
 		} catch (UnimplementedInstructionException e) {
 			
 			System.err.println(String.format("Unimplemented instruction: '%s' at 0x%x. Cannot generate code.", 
 					e.getUnimplementedInstructionName(), e.getUnimplementedInstructionCodeAddress()));
 			
 		} catch (CompilationException e) {
 			
 			System.err.println("Couldn't compile generated C. Cannot continue.");
 			
 		} catch (CGeneratorFaultException e) {
 			
 			System.err.println("Fault in the C generator. Cannot continue.");
 			
 		} catch (ITraceGeneratorFaultException e) {
 			
 			System.err.println("Fault in the injectable trace generator. Cannot continue.");
 			
 		} catch (ITraceDescGeneratorFaultException e) {
 			
 			System.err.println("Fault in the injectable trace description generator. Cannot continue.");
 			
 		}
 		
 	}
 	
 	/*
 	 * Takes the current context (a trace must have been selected using context.setCurrentTraceIndex(int)!)
 	 * and generates a C file called called cTraceFileName that represents the currently selected trace.
 	 * This will also update the trace's metadata with literal pool, exception and helper functon info.
 	 * 
 	 * Will throw an UnimplementedInstructionException if any of the bytecodes in the trace have not yet
 	 * been implemented.
 	 */
 	public void generateC(CodeGenContext context, String cTraceFileName) throws UnimplementedInstructionException, CGeneratorFaultException {
 		System.out.println("Generating C...");
 		CTraceGenerator generator = new CTraceGenerator(context);
 		generator.prepare(cTraceFileName);
 		generator.generate();
 		generator.finish();
 	}
 	
 	/*
 	 * Compile the C file called cTraceFileName into the .S file called asmTraceFileName using GCC.
 	 * 
 	 * Will throw a CompilationException if it encounters any errors during compilation.
 	 */
 	public void compileC(String cTraceFileName, String asmTraceFileName) throws CompilationException {
 		String command = String.format("arm-linux-androideabi-gcc -march=armv7-a -mfloat-abi=softfp -mthumb -O3 -S -o %s %s", asmTraceFileName, cTraceFileName);
 		System.out.println("Compiling C...");
 		System.out.println("  (cmd: " + command + ")");
 		try {
 			// Compile the C
 			Process process = Runtime.getRuntime().exec(command);
 			BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 			
 			process.waitFor();
 			
 			boolean error = false;
 			String line;
 			
 			while (errorStreamReader.ready()) {
 				line = errorStreamReader.readLine();
 				if (line.contains("error")) {
 					System.err.println(line);
 					if (!error) {
 						error = true;
 					}
 				}
 			}
 			if (error) {
 				throw new CompilationException();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new CompilationException();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			throw new CompilationException();
 		}
 	}
 	
 	/*
 	 * Convert all the provided asmFileNames into one asm file in the format that can link with the DVM.
 	 */
 	public void emitITrace(CodeGenContext context, String[] asmFileNames, String iTraceName) throws ITraceGeneratorFaultException {
 		System.out.println("Producing linkable assembly file...");
 		ITraceGenerator asmLoader = new ITraceGenerator();
 		asmLoader.loadAsmFiles(context, asmFileNames);
 		asmLoader.createITrace(context, iTraceName);
 	}
 	
 	/*
 	 * Produce the injectable trace description file that the DVM looks for to know which traces can be injected.
 	 */
 	public void emitITraceDesc(CodeGenContext context, String fileName) throws ITraceDescGeneratorFaultException {
 		System.out.println("Producing trace description file...");
 		ITraceDescGenerator iTraceDescGen = new ITraceDescGenerator();
 		iTraceDescGen.createInjectableTraceDesc(context, fileName);
 	}
 }
