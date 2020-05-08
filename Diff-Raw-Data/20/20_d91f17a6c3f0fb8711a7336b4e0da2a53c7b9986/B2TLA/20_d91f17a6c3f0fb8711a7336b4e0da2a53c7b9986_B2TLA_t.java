 package de.b2tla;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 
 import tlc2.TLCGlobals;
 
 import de.b2tla.B2TLAGlobals;
 
 import de.b2tla.analysis.UsedStandardModules;
 import de.b2tla.analysis.UsedStandardModules.STANDARD_MODULES;
 import de.b2tla.exceptions.B2TLAIOException;
 import de.b2tla.exceptions.B2tlaException;
 import de.b2tla.tlc.TLCOutput;
 import de.b2tla.tlc.TLCOutputInfo;
 import de.b2tla.tlc.TLCOutput.TLCResult;
 import de.b2tla.util.StopWatch;
 import de.be4.classicalb.core.parser.exceptions.BException;
 
 public class B2TLA {
 
 	private String mainFileName;
 	private String machineFileNameWithoutFileExtension;
 	private String pathAndName;
 	private String path;
 	private String tlaModule;
 	private String config;
 	private B2TlaTranslator translator;
 	private TLCOutputInfo tlcOutputInfo;
 	private String ltlFormula;
 	
 	
 	public static void main(String[] args) throws IOException {
 		StopWatch.start("Translation");
 		B2TLA b2tla = new B2TLA();
 
 		try {
 			b2tla.progress(args);
 		} catch (BException e) {
 			System.err.println(e.getMessage());
 			return;
 		} catch (B2tlaException e) {
 			System.err.println(e.getMessage());
 			System.out.println("Model checking time: 0 sec");
 			System.out.println("Result: " + e.getError());
 			return;
 		}
 		StopWatch.stop("Translation");
 
 		if (B2TLAGlobals.isRunTLC()) {
 			ArrayList<String> output = TLCRunner.runTLC(b2tla.machineFileNameWithoutFileExtension,
 					b2tla.path);
 			b2tla.evalOutput(output, true);
 		}
 
 	}
 
 	public static TLCResult test(String[] args, boolean deleteFiles) throws IOException {
 		B2TLAGlobals.resetGlobals();
 		B2TLAGlobals.setDeleteOnExit(deleteFiles);
 		B2TLA b2tla = new B2TLA();
 		try {
 			b2tla.progress(args);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println(e.getMessage());
 			return null;
 		}
 
 		if (B2TLAGlobals.isRunTLC()) {
 			ArrayList<String> output = TLCRunner.runTLCInANewJVM(
 					b2tla.machineFileNameWithoutFileExtension, b2tla.path);
 			TLCResult error = TLCOutput.findError(output);
 			System.out.println(error);
 			return error;
 		}
 		return null;
 	}
 
 	private void evalOutput(ArrayList<String> output, boolean createTraceFile) {
 		TLCOutput tlcOutput = new TLCOutput(machineFileNameWithoutFileExtension,
 				output.toArray(new String[output.size()]), tlcOutputInfo);
 		tlcOutput.parseTLCOutput();
 		System.out.println("Model checking time: " + tlcOutput.getRunningTime()
 				+ " sec");
 
 		System.out.println("Result: " + tlcOutput.getError());
 		if (tlcOutput.hasTrace() && createTraceFile) {
 			StringBuilder trace = tlcOutput.getErrorTrace();
 			String tracefileName = machineFileNameWithoutFileExtension + ".tla.trace";
 			createFile(path, tracefileName, trace.toString(), "Trace file '"
 					+ path + tracefileName + "'created.", false);
 		}
 	}
 
 	private void handleParameter(String[] args) {
 		int index = 0;
 		while (index < args.length) {
 			if (args[index].toLowerCase().equals("-nodead")) {
 				B2TLAGlobals.setDeadlockCheck(false);
 			} else if (args[index].toLowerCase().equals("-notlc")) {
 				B2TLAGlobals.setRunTLC(false);
 			} else if (args[index].toLowerCase().equals("-notranslation")) {
 				B2TLAGlobals.setTranslate(false);
 			} else if (args[index].toLowerCase().equals("-nogoal")) {
 				B2TLAGlobals.setGOAL(false);
 			} else if (args[index].toLowerCase().equals("-noinv")) {
 				B2TLAGlobals.setInvariant(false);
 			} else if (args[index].toLowerCase().equals("-tool")) {
 				B2TLAGlobals.setTool(false);
 			}else if (args[index].toLowerCase().equals("-tmp")) {
					path =  System.getProperty("java.io.tmpdir");
 			} else if (args[index].toLowerCase().equals("-noltl")) {
 				B2TLAGlobals.setCheckltl(false);
 			} else if (args[index].toLowerCase().equals("-ltlformula")) {
 				index = index + 1;
 				if (index == args.length) {
 					throw new B2TLAIOException(
 							"Error: LTL formula requiered after option '-ltlformula'.");
 				}
 				ltlFormula = args[index];
 			} else if (args[index].charAt(0) == '-') {
 				throw new B2TLAIOException("Error: unrecognized option: "
 						+ args[index]);
 			} else {
 				if (mainFileName != null) {
 					throw new B2TLAIOException(
 							"Error: more than one input files: " + mainFileName
 									+ " and " + args[index]);
 				}
 				mainFileName = args[index];
 
 			}
 			index++;
 		}
 		if (mainFileName == null) {
 			throw new B2TLAIOException("Main machine required!");
 		}
 	}
 
 	private void progress(String[] args) throws IOException, BException {
 		handleParameter(args);
 
 		handleMainFileName();
 		if (B2TLAGlobals.isTranslate()) {
 			translator = new B2TlaTranslator(machineFileNameWithoutFileExtension, getFile(), this.ltlFormula);
 			translator.translate();
 
 			this.tlaModule = translator.getModuleString();
 			this.config = translator.getConfigString();
 			this.tlcOutputInfo = translator.getTLCOutputInfo();
 			createFiles();
 		}
 
 	}
 
 	private void handleMainFileName() {
 		String name = mainFileName;
 		name = name.replace("\\", File.separator);
 		name = name.replace("/", File.separator);
 
 		mainFileName = name;
 
 		if (name.toLowerCase().endsWith(".mch")) {
 			name = name.substring(0, name.length() - 4);
 		}
 		pathAndName = name;
 		if(path == null){
 			if (name.contains(File.separator)) {
 				path = name.substring(0, name.lastIndexOf(File.separator) + 1);
 			} else {
 				path = "." + File.separator;
 			}
 		}
 
 
 		machineFileNameWithoutFileExtension = name.substring(name.lastIndexOf(File.separator) + 1);
 	}
 
 	private void createFiles() {
 		createFile(path, machineFileNameWithoutFileExtension + ".tla", tlaModule, "TLA+ module '"
				+ path + machineFileNameWithoutFileExtension+ ".tla' created.", B2TLAGlobals.isDeleteOnExit());
 		createFile(path, machineFileNameWithoutFileExtension + ".cfg", config, "Configuration file '"
				+ path + machineFileNameWithoutFileExtension+".cfg' created.", B2TLAGlobals.isDeleteOnExit());
 
 		createStandardModules();
 	}
 
 	private void createStandardModules() {
 		if (translator.getUsedStandardModule().contains(
 				STANDARD_MODULES.Relations)) {
 			createStandardModule(path, STANDARD_MODULES.Relations.toString());
 		}
 
 		if (translator.getUsedStandardModule().contains(
 				STANDARD_MODULES.Functions)) {
 			createStandardModule(path, STANDARD_MODULES.Functions.toString());
 		}
 
 		if (translator.getUsedStandardModule().contains(
 				STANDARD_MODULES.BBuiltIns)) {
 			createStandardModule(path, STANDARD_MODULES.BBuiltIns.toString());
 		}
 
 		if (translator.getUsedStandardModule().contains(
 				STANDARD_MODULES.FunctionsAsRelations)) {
 			createStandardModule(path,
 					STANDARD_MODULES.FunctionsAsRelations.toString());
 			if (!translator.getUsedStandardModule().contains(
 					STANDARD_MODULES.Functions)) {
 				createStandardModule(path,
 						STANDARD_MODULES.Functions.toString());
 			}
 		}
 
 		if (translator.getUsedStandardModule().contains(
 				STANDARD_MODULES.SequencesExtended)) {
 			createStandardModule(path,
 					STANDARD_MODULES.SequencesExtended.toString());
 		}
 
 		if (translator.getUsedStandardModule().contains(
 				STANDARD_MODULES.SequencesAsRelations)) {
 			createStandardModule(path,
 					STANDARD_MODULES.SequencesAsRelations.toString());
 			
 			if (!translator.getUsedStandardModule().contains(
 					STANDARD_MODULES.Relations)) {
 				createStandardModule(path,
 						STANDARD_MODULES.Relations.toString());
 			}
 			
 			if (!translator.getUsedStandardModule().contains(
 					STANDARD_MODULES.FunctionsAsRelations)) {
 				createStandardModule(path,
 						STANDARD_MODULES.FunctionsAsRelations.toString());
 			}
 			
 			if (!translator.getUsedStandardModule().contains(
 					STANDARD_MODULES.Functions)) {
 				createStandardModule(path,
 						STANDARD_MODULES.Functions.toString());
 			}
 		}
 	}
 
 	private void createStandardModule(String path, String name) {
 		// standard modules are copied from the standardModules folder to the
 		// current directory
 
 		File file = new File(path + name + ".tla");
 		InputStream is = null;
 		FileOutputStream fos = null;
 		try {
 			is = this.getClass().getClassLoader()
 					.getResourceAsStream("standardModules/" + name + ".tla");
 			if (is == null) {
 				is = new FileInputStream("src/main/resources/standardModules/"
 						+ name + ".tla");
 			}
 
 			fos = new FileOutputStream(file);
 
 			int read = 0;
 			byte[] bytes = new byte[1024];
 
 			while ((read = is.read(bytes)) != -1) {
 				fos.write(bytes, 0, read);
 			}
 
 			System.out.println("Standard module '" + path + name
 					+ ".tla' created.");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (B2TLAGlobals.isDeleteOnExit()) {
 				file.deleteOnExit();
 			}
 			try {
 				is.close();
 				fos.flush();
 				fos.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	public static void createUsedStandardModules(String path,
 			ArrayList<UsedStandardModules.STANDARD_MODULES> standardModules) {
 		if (standardModules.contains(STANDARD_MODULES.Relations)) {
 			File naturalsFile = new File(path + File.separator
 					+ "Relations.tla");
 			if (!naturalsFile.exists()) {
 				try {
 					naturalsFile.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			try {
 				Writer fw = new FileWriter(naturalsFile);
 				fw.write(StandardModules.Relations);
 				fw.close();
 				System.out.println("Standard module '" + naturalsFile.getName()
 						+ "' created.");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		if (standardModules.contains(STANDARD_MODULES.BBuiltIns)) {
 			File bBuiltInsFile = new File(path + File.separator
 					+ "BBuiltIns.tla");
 			if (!bBuiltInsFile.exists()) {
 				try {
 					bBuiltInsFile.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			try {
 				Writer fw = new FileWriter(bBuiltInsFile);
 				fw.write(StandardModules.BBuiltIns);
 				fw.close();
 				System.out.println("Standard modules BBuiltIns.tla created.");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	private File getFile() {
 		File mainFile = new File(mainFileName);
 		if (!mainFile.exists()) {
 			throw new B2TLAIOException("The file " + mainFileName
 					+ " does not exist.");
 		}
 		return mainFile;
 	}
 
 	public static String fileToString(String fileName) throws IOException {
 		StringBuilder res = new StringBuilder();
 		BufferedReader in = new BufferedReader(new FileReader(fileName));
 		String str;
 		while ((str = in.readLine()) != null) {
 			res.append(str + "\n");
 		}
 		in.close();
 		return res.toString();
 	}
 
 	public static void createFile(String dir, String fileName, String text,
 			String message, boolean deleteOnExit) {
 		File d = new File(dir);
 		d.mkdirs();
 		File file = new File(dir + File.separator + fileName);
 		try {
 			file.createNewFile();
 			FileWriter fw;
 			fw = new FileWriter(file);
 			fw.write(text);
 			fw.close();
 			System.out.println(message);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new B2TLAIOException(e.getMessage());
 		} finally {
 			if (deleteOnExit) {
 				file.deleteOnExit();
 			}
 		}
 	}
 
 }
