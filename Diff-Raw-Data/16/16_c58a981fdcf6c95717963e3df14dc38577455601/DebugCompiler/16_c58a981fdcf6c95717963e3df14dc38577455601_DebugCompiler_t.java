 package org.strategoxt.debug.core.util;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.compiler.CompilationProgress;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.terms.TermFactory;
 import org.strategoxt.debug.core.util.table.FileLineLengthTable;
 import org.strategoxt.debug.core.util.table.LineLengthTable;
 import org.strategoxt.lang.Context;
 import org.strategoxt.lang.StrategoExit;
 
 public class DebugCompiler {
 	
 
 	//public static final String WORKING_DIR = "/home/rlindeman/workspace/strj-dbg-app/working";
 	//private String workingDir = null;
 	
 	private DebugCompileProgress debugCompileProgress = null;
 	
 	public DebugCompiler()
 	{
 		//this.workingDir = workingDir;
 		this.debugCompileProgress = new DebugCompileProgress();
 	}
 	
 	private void init(DebugSessionSettings settings)
 	{
 		File projectDir = settings.getProjectDirectory().toFile();
 		if (!projectDir.exists())
 		{
 			// create it
 			projectDir.mkdirs();
 		} else if (!projectDir.isDirectory())
 		{
 			// it is not a directory
 			projectDir.mkdirs();
 		}
 	}
 	
 	/*
 	public String getWorkingDir()
 	{
 		return this.workingDir;
 	}*/
 	
 	/**
 	 * Clear the contents of the given project directory.
 	 * 
 	 * Folders 'stratego', 'java' and 'class' are completely cleaned.
 	 * @param projectName
 	 */
 	public void cleanDirectories(DebugSessionSettings settings)
 	{
 		String projectStrategoDir = settings.getStrategoDirectory().toOSString();
 		String projectJavaDir = settings.getJavaDirectory().toOSString();
 		String projectClassDir = settings.getClassDirectory().toOSString();
 		
 		File dir = null;
 		
 		FileUtil.deleteFile(projectStrategoDir); // delete the 'stratego' dir
 		FileUtil.deleteFile(projectJavaDir); // delete the 'java' dir
 		FileUtil.deleteFile(projectClassDir); // delete the 'class' dir
 		
 		dir = new File(projectStrategoDir);
 		dir.mkdirs();
 		dir = new File(projectJavaDir);
 		dir.mkdirs();
 		dir = new File(projectClassDir);
 		dir.mkdirs();
 		
 	}
 	
 	/**
 	 * Compile a stratego program to java classes.
 	 * 
 	 * @param strategoFilePath strategoFilePath should point to the stratego file
 	 * @param projectName projectName is the name of the project, used to create temp folders
 	 * @return returns the basedir of the binary files
 	 * @throws IOException
 	 * @throws DebugCompileException 
 	 */
 	public IPath runCompile(DebugSessionSettings debugSessionSettings) throws IOException, DebugCompileException
 	{
 		// TODO: Use debugsettings
 		init(debugSessionSettings);
 		IPath strategoSourceBasedir = debugSessionSettings.getStrategoSourceBasedir();
 		IPath strategoFilePath = debugSessionSettings.getStrategoFilePath();
 		String projectName = debugSessionSettings.getProjectName();
 		// throw exception if completeInputPath does not exist
 		
 		File absFilePath = strategoSourceBasedir.append(strategoFilePath).toFile();
 		//File f = new File(strategoFilePath);
 		if (!absFilePath.exists())
 		{
 			throw new FileNotFoundException("Input file '" + absFilePath.getAbsolutePath() + "' does not exists!");
 		}
 		
 		cleanDirectories(debugSessionSettings); // create directory structure in the working dir
 		// projectName should be the stratego filename without the dir and the extension		
 		
 		//String projectDir = debugSessionSettings.getProjectDirectory();
 		IPath projectJavaDir = debugSessionSettings.getJavaDirectory(); // save generated java classes in a temp working dir
 		IPath projectClassDir = debugSessionSettings.getClassDirectory(); // save generated class files in a temp working dir
 		
 		String libraryName = projectName; // will be the packageName
 		String className = projectName;
 		String packageFolder = projectName;
 		IPath compiledStrategoFilename = projectJavaDir.append(packageFolder).append(className + ".java"); // packageName + className
 		IPath inputStrategoFilename = strategoSourceBasedir.append(strategoFilePath);
 		boolean succes = compileStratego(debugSessionSettings, inputStrategoFilename, libraryName, compiledStrategoFilename); // stratego to java
 		if (!succes)
 		{
 			// TODO: what to do when compile fails... Throw an Exception?
 			throw new DebugCompileException("Failed to compile stratego program to java.");
 		}
 		
 		IPath sourceBasedir = projectJavaDir;
 		IPath mainSourceFilename = new Path(packageFolder).append(className + ".java");
 		IPath binBase = compileJava(debugSessionSettings, sourceBasedir, mainSourceFilename, projectClassDir); // java to class
 		
 		return binBase;	
 	}
 
 	/**
 	 * Compile a stratego program with debug information to java classes.
 	 * 
 	 * @param strategoFilePath strategoFilePath should point to the stratego file
 	 * @param projectName projectName is the name of the project, used to create temp folders
 	 * @return returns the basedir of the binary files
 	 * @throws FileNotFoundException 
 	 * @throws DebugCompileException 
 	 */
 	public IPath debugCompile(DebugSessionSettings debugSessionSettings) throws FileNotFoundException, DebugCompileException
 	{
 		// TODO: use debug settings
 		init(debugSessionSettings);
 		IPath strategoSourceBasedir = debugSessionSettings.getStrategoSourceBasedir();
 		IPath strategoFilePath = debugSessionSettings.getStrategoFilePath();
 		String projectName = debugSessionSettings.getProjectName();
 		
 		File absFilePath = strategoSourceBasedir.append(strategoFilePath).toFile();
 		if (!absFilePath.exists())
 		{
 			throw new FileNotFoundException("Input file '" + absFilePath.getAbsolutePath() + "' does not exists!");
 		}
 		
 		cleanDirectories(debugSessionSettings); // create directory structure in the working dir
 		// projectName should be unique, let the caller of this method decide what the name of the project is.
 		
 		Collection<String> libraryPaths = new ArrayList<String>();
 		libraryPaths.add("."); // the "-I" arguments
 		
		//IPath projectDir = debugSessionSettings.getProjectDirectory();
 		IPath projectStrategoDir = debugSessionSettings.getStrategoFilePath();
 		IPath projectJavaDir = debugSessionSettings.getJavaDirectory();
 		IPath projectClassDir = debugSessionSettings.getClassDirectory();
 
 		//String strOutputBasedir = projectStrategoDir;
 		// strategoSourceBasedir + strategoFilePath is the input stratego file (without debug information)
 		// strOutputBasedir + strategoFilePath will be the output stratego file (with debug information)
 		// TODO: removed , strategoSourceBasedir, strategoFilePath, strOutputBasedir
 		Map<IPath, IPath> result = generateStratego(debugSessionSettings, libraryPaths); // str to str (with debug info)
 		Collection<IPath> inputFiles = result.keySet();
 		Collection<IPath> generatedFiles = result.values();
 		// TODO check if files are actually created!
 		if (generatedFiles == null || generatedFiles.isEmpty())
 		{
 			throw new DebugCompileException("Failed to compile stratego program with debug information.");
 		}
 		
 		// create lookup table
 		IPath tableFilename = projectStrategoDir.append(projectName + ".table"); // location of the table
 		// the table contains all debug lookup information for all files in the project
 		generateLookupTable(tableFilename, generatedFiles); // TODO: sort the filenames on their path
 		
 		// create character offset table
 		// TODO: optimize
 		List<FileLineLengthTable> tables = new ArrayList<FileLineLengthTable>();
 		for(IPath inputFilePath : inputFiles) // TODO: sort the inputFiles on their path
 		{
 			IPath basedir = strategoSourceBasedir; // end with '/'
 			//IPath inputFilePath = new Path(inputFileString);
 			
 			if (basedir.isPrefixOf(inputFilePath)) {
 				//inputFile = inputFile.substring(basedir.length()); // make relative to the basedir
 				inputFilePath = inputFilePath.makeRelativeTo(basedir);
 			}
 			else
 			{
 				System.err.println("inputfile '"+inputFilePath+"' is not located in the strategoSourceBasedir '"+strategoSourceBasedir+"'");
 			}
 			FileLineLengthTable t = new FileLineLengthTable(inputFilePath.toOSString());
 			t.create(basedir);
 			tables.add(t);
 		}
 		IPath charOffsetTableFilename = projectStrategoDir.append((projectName + ".offset")); // location of the character offset table
 		LineLengthTable.writeLineLengthTable(charOffsetTableFilename, tables);
 		
 		String libraryName = projectName; // will be the packageName
 		String className = projectName;
 		String packageFolder = projectName;
 		IPath compiledStrategoFilename = projectJavaDir.append(packageFolder).append(className + ".java"); // packageName + className
 		//String inputStrategoFilename = strOutputBasedir + "/" + strategoFilePath; // the output of str to str is used as input
 		IPath foo = debugSessionSettings.getStrategoSourceBasedir().append(debugSessionSettings.getStrategoFilePath());
 		IPath inputStrategoFilename = result.get(foo); // maps the original input to the generated output
 		boolean succes = compileStratego(debugSessionSettings, inputStrategoFilename, libraryName, compiledStrategoFilename); // stratego to java
 		if (!succes)
 		{
 			// TODO: what to do when compile fails... Throw an Exception?
 			throw new DebugCompileException("Failed to compile stratego program to java.");
 		}
 		IPath sourceBasedir = projectJavaDir;
 		IPath mainSourceFilename = new Path(packageFolder).append(className + ".java");
 		IPath binBase = compileJava(debugSessionSettings, sourceBasedir, mainSourceFilename, projectClassDir); // java to class
 		
 		return binBase;
 	}
 	
 	/**
 	 * Add stratego debug information to the given strategofile at sourceBasedir/inputFilename
 	 * The output will be saved in the strOutputBasedir.
 	 * The inputFilePath should be relative to the sourceBasedir. 
 	 * libraryPaths contains all "-I" paths
 	 * 
 	 * The inputFilePath will be saved in the debug information instead
 	 * of the complete absolute path so it points to a relative file containing the original stratego source.
 	 * The debugger needs to figure out what the actual absolute path is.
 	 * 
 	 * Method returns a Map, the key is the input file, the value is the generated file.
 	 * 
 	 * @param sourceBasedir
 	 * @param inputFilePath
 	 * @param outputFilename
 	 * @return
 	 * @throws DebugCompileException 
 	 */
 	protected Map<IPath, IPath> generateStratego(DebugSessionSettings debugSessionSettings, Collection<String> libraryPaths) throws DebugCompileException
 	{
 		String sourceBasedir = debugSessionSettings.getStrategoSourceBasedir().toOSString();
 		String inputFilePath = debugSessionSettings.getStrategoFilePath().toOSString();
 		String strOutputBasedir = debugSessionSettings.getStrategoDirectory().toOSString();
 		// assume f is a valid file
 		File absInput = new File(sourceBasedir, inputFilePath);
 		System.out.println("Adding debug information to " + absInput.getAbsolutePath());
 		System.out.println("Please wait...");
 		
 		//System.out.println(sourceBasedir);
 		//System.out.println(inputFilePath);
 		//System.out.println(outputFilename);
 		
 		//the transform a stratego program to a stratego program with debug information
 		Context context = org.strategoxt.imp.debug.stratego.transformer.trans.Main.init();
 		context.setStandAlone(true);
 		// TODO: set CustomIOAgent to forward error messages
 		
 		IStrategoTerm term = null;; 
 		String stratego_input = sourceBasedir + "/" + inputFilePath;
 		String[] basic_args = new String[]{
 				"-i", stratego_input
 				, "--gen-dir", strOutputBasedir
 				, "--base-dir", sourceBasedir 
 			};
 
 		String[] transformer_args = StringUtil.concat(basic_args, debugSessionSettings.getGenerateStrategoExtraArguments());
 		
 		boolean strategoGenerationHasFailed = false; // true if the transformer program failed with an exception or one of the files could not be transformed
 		long startTime = System.currentTimeMillis();
 		//term = org.strategoxt.imp.debug.stratego.transformer.trans.apply_debug_project_0_1.instance.invoke(context, input, termArguments);
 		try {
 			term = org.strategoxt.imp.debug.stratego.transformer.trans.Main.mainNoExit(context, transformer_args);
 			// term will be null because of the StrategoExit Exception
 		} catch (StrategoExit e) {
 			if (e.getValue() != StrategoExit.SUCCESS)
 			{
 				strategoGenerationHasFailed = true;
 				System.err.println("Stratego transformer exited with: ");
 				e.printStackTrace();
 			}
 		}
 		long finishTime = System.currentTimeMillis();
 		long duration = finishTime - startTime;
 		this.debugCompileProgress.setGenerateStrategoDuration(duration);
 		System.out.println("Generate Stratego finished in " + duration +" ms.");
 		// luckily we stored the term using java-store-term
 		term = org.strategoxt.imp.debug.stratego.transformer.strategies.java_store_term_0_0.instance.getStoredTerm();
 		
 		List<String> generatedFiles = new ArrayList<String>();
 		List<String> inputFiles = new ArrayList<String>();
 		
 		if (term == null)
 		{
 			System.out.println("Adding debug information failed!");
 			return null;
 		}
 		else if (term.getTermType() == IStrategoTerm.LIST)
 		{
 			IStrategoList listTerm = (IStrategoList) term;
 			IStrategoTerm[] subterms = listTerm.getAllSubterms();
 			for(IStrategoTerm subterm : subterms)
 			{
 				if (subterm.getTermType() == IStrategoTerm.TUPLE)
 				{
 					// should be a tuple: (status, filename)
 					IStrategoTerm[] tupleTerms = subterm.getAllSubterms();
 					if (tupleTerms.length != 3)
 					{
 						// tuple should have two items
 						System.err.println("ERROR, has no three items!");
 					}
 					else
 					{
 						IStrategoString sSucces = (IStrategoString) tupleTerms[0];
 						String status = sSucces.stringValue();
 						IStrategoString sGenFilename = (IStrategoString) tupleTerms[1];
 						String genFilename = sGenFilename.stringValue();
 						IStrategoString sInputFilename = (IStrategoString) tupleTerms[2];
 						String inputFilename = sInputFilename.stringValue();
 						//System.out.println(status + " " + filename);
 						if (!"SUCCES".equals(status))
 						{
 							strategoGenerationHasFailed = true;
 							System.out.println(status + " : Failed at " + sInputFilename);
 						}
 						else
 						{
 							generatedFiles.add(genFilename);
 							inputFiles.add(inputFilename);
 						}
 					}
 				}
 				
 			}
 		}
 		else
 		{
 			// incorrect return type
 		}
 
 
 		if (strategoGenerationHasFailed)
 		{
 			System.out.println("Adding debug information failed!");
 			throw new DebugCompileException("Adding debug information failed!");
 		}
 		
 		Map<IPath, IPath> result = new HashMap<IPath, IPath>();
 		for(int i = 0; i < generatedFiles.size(); i++)
 		{
 			IPath genFile = new Path(generatedFiles.get(i));
 			IPath inputFile = new Path(inputFiles.get(i));
 			result.put(inputFile, genFile);
 		}
 		return result;
 	}
 	
 	protected void generateLookupTable(IPath tableFilenameString, Collection<IPath> strategoDebugFileNames)
 	{
 		// transform a stratego program to a stratego program with debug information
 		Context context = org.strategoxt.imp.debug.stratego.transformer.trans.Main.init();
 		// TODO: Use CustomIOAgent to forward output
 		StrategoTermBuilder builder = new StrategoTermBuilder();
 		IStrategoList inputfilenames = builder.convertToIStrategoList(strategoDebugFileNames);
 		//IStrategoTerm input = new BasicStrategoString(strategoDebugFileName);
 		
 		// table-filename is the location of the output table
 		// current term should be a list of inputfilenames each pointing to a stratego file
 		// output is the location of the table
 		// STRATEGO: create-table(|table-filename): inputfilenames* -> table-filename
 		IStrategoTerm current = inputfilenames;
 		TermFactory factory = new TermFactory();
 		IStrategoTerm tableFilename = factory.makeString(tableFilenameString.toOSString()); 
 		long startTime = System.currentTimeMillis();
 		IStrategoTerm output = org.strategoxt.imp.debug.stratego.transformer.trans.create_table_0_1.instance.invoke(context, current, tableFilename);
 		long finishTime = System.currentTimeMillis();
 		long duration = finishTime - startTime;
 		this.debugCompileProgress.setGenerateLookupTableDuration(duration);
 		
 		if (output == null || !output.toString().equals(tableFilenameString))
 		{
 			// output should match tableFilenameString
 		}
 		
 	}
 	
 	/**
 	 * Compile the given stratego file to java.
 	 * 
 	 * @param inputStrategoFilename
 	 * @param libraryName
 	 * @param compiledStrategoFilename
 	 * @throws DebugCompileException 
 	 */
 	protected boolean compileStratego(DebugSessionSettings debugSessionSettings, IPath inputStrategoFilename, String libraryName, IPath compiledStrategoFilename) throws DebugCompileException
 	{
 		System.out.println("Generated file at " + inputStrategoFilename);
 		System.out.println("Compile str to java...");
 		// compile the stratego file at $outputFilename
 		String strategodebuglib_rtree_dir = debugSessionSettings.getStrategoDebugLibraryDirectory().toOSString();
 		String javaImportName = "org.strategoxt.imp.debug.stratego.runtime.trans"; // was: "org.strategoxt.libstrategodebuglib"
 		// TODO: when compiling we may need extra arguments
 		String[] extra_args = debugSessionSettings.getCompileTimeExtraArguments();
 		
 
 		//		"-I", "/home/rlindeman/Documents/TU/strategoxt/spoofax-imp/source/org.strategoxt.imp.debug.stratego.transformer/"
 
 		String[] basic_strj_args = new String[] {
 			"-i", 	inputStrategoFilename.toOSString()
 			, "-o", compiledStrategoFilename.toOSString() // output will be java, so folders should match the library name
 			, "-I", strategodebuglib_rtree_dir // location of rtree files
 			, "-p", libraryName // will be the package name
 			//, "--silent"
 			, "--clean" // remove previous java
 			, "-la", javaImportName // used as java import
 		};
 		// Combine the basic_strj_args and the extra_I_args
 		String[] strj_args = StringUtil.concat(basic_strj_args, extra_args);
 		boolean succes = false;
 		Context c = org.strategoxt.strj.Main.init();
 		CustomIOAgent ioAgent = new CustomIOAgent();
 		c.setIOAgent(ioAgent);
 		long startTime = System.currentTimeMillis();
 		long finishTime = -1;
 		try {
 			// TODO: can we forward the error log messages?
 			org.strategoxt.strj.Main.mainNoExit(c, strj_args);
 		}
 		catch(StrategoExit e)
 		{
 			if (e.getValue() == StrategoExit.SUCCESS)
 			{
 				succes = true;
 			}
 			else
 			{
 				System.out.println("Exception: " + e.getMessage());
 				String message = "Failed to compile stratego program to java. \n" + ioAgent.getStderr().trim();
 				DebugCompileException de = new DebugCompileException(message, e);
 				de.setStdErrContents(ioAgent.getStderr());
 				throw de;
 			}
 		}
 		catch(Exception e)
 		{
 			System.out.println("Exception: " + e.getMessage());
 			String message = "Failed to compile stratego program to java. \n" + ioAgent.getStderr().trim();
 			DebugCompileException de = new DebugCompileException(message, e);
 			de.setStdErrContents(ioAgent.getStderr());
 			throw de;
 			
 		}
 		finally
 		{
 			finishTime = System.currentTimeMillis();
 			long duration = finishTime - startTime;
 			this.debugCompileProgress.setCompileStrategoDuration(duration);
 			System.out.println("Stratego to Java compiler finished in " + duration +" ms.");
 		}
 		
 		/*
 		System.out.println("Strj compiler finished.");
 		String s = ioAgent.getStderr();
 		System.out.println("ERR:");
 		System.out.println(s);
 		
 		String s2 = ioAgent.getStdout();
 		System.out.println("OUT:");
 		System.out.println(s2);
 		*/
 		return succes;
 	}
 	
 	/**
 	 * Compiles the mainSourceFileName (path relative to sourceBasedir) and places the generated class files in the binBasedir.
 	 * @param sourceBasedir
 	 * @param mainSourceFileName
 	 * @param binBasedir
 	 * @return
 	 */
 	protected IPath compileJava(DebugSessionSettings debugSessionSettings, IPath sourceBasedir, IPath mainSourceFileName, IPath binBasedir)
 	{
 		log("Compiling " + mainSourceFileName);
 		log("Please wait...");
 		// import org.strategoxt.stratego_lib.*;
 		// import org.strategoxt.libstrategodebuglib.*;
 		// import org.strategoxt.lang.*;
 		String strategoxtjar = debugSessionSettings.getStrategoxtJar().toOSString();
 		String libstrategodebuglib = debugSessionSettings.getStrategoDebugRuntimeJar().toOSString();
 		String strjdebugruntime = debugSessionSettings.getStrategoDebugRuntimeJavaJar().toOSString();
 		
 		String classPath = strategoxtjar + java.io.File.pathSeparatorChar + libstrategodebuglib + java.io.File.pathSeparatorChar + strjdebugruntime + java.io.File.pathSeparatorChar + sourceBasedir;
 		if (debugSessionSettings.getJavaCompileExtraClasspath() != null)
 		{
 			for(String c : debugSessionSettings.getJavaCompileExtraClasspath())
 			{
 				classPath += java.io.File.pathSeparatorChar + c;
 			}
 		}
 		//log(classPath);
 		// http://www.javaworld.com/javatips/jw-javatip131.html
 		IPath filename = sourceBasedir.append(mainSourceFileName);
 		String[] args = new String[] {
 		        "-d", binBasedir.toOSString(),
 		         filename.toOSString(),
 		         "-cp", classPath,
 		         "-source", "1.5"
 		    };
 		// TODO: get extra compiler arguments from debugSessionSettings
 		FileOutputStream outStream = null;
 		FileOutputStream errorStream = null;
 		try {
 			File outFile = new File("out.log");
 			File errorFile = new File("error.log");
 			outStream = new FileOutputStream(outFile, false);
 			errorStream = new FileOutputStream(errorFile, false);
 			
 			log("Logfile: " + outFile.getAbsolutePath());
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		PrintWriter outWriter = null;
 		PrintWriter errWriter = null;
 		
 		if (outStream != null)
 		{
 			outWriter = new PrintWriter(outStream);
 		} else
 		{
 			outWriter = new PrintWriter(System.out);
 		}
 		if (errorStream != null)
 		{
 			errWriter = new PrintWriter(errorStream);
 		}
 		else
 		{
 			errWriter = new PrintWriter(System.err);
 		}
 		
 		//boolean systemExitWhenFinished = false;
 		//Map customDefaultOptions = new HashMap();
 		CompilationProgress compilationProgress = this.debugCompileProgress.getJavaCompileProgress();
 		
 		//org.eclipse.jdt.internal.compiler.batch.Main main = new Main(outWriter, errWriter, systemExitWhenFinished, customDefaultOptions, compilationProgress);
 		//boolean result = main.compile(args);
 		boolean result = org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(args, outWriter, errWriter, compilationProgress);
 		log("Compile result: " + result);
 		//org.eclipse.jdt.internal.compiler.batch.Main.main(args);
 		
 		/*
 		org.eclipse.jdt.compiler.CompilationProgress progress = null; // instantiate your subclass
 		org.eclipse.jdt.internal.compiler.batch.BatchCompiler.compile(
 		"-classpath rt.jar A.java",
 		new PrintWriter(System.out),
 		new PrintWriter(System.err),
 		progress);
 		
 		org.eclipse.jdt.internal.compiler.batch.Main.
 		*/
 		/*
 		int status = com.sun.tools.javac.Main.compile(args);
 		if (status == 0)
 		{
 			System.out.println("Compile ok");
 		}
 		else
 		{
 			System.out.println("Compile error");
 		}*/
 		long duration = this.debugCompileProgress.getJavaCompileProgress().getDuration();
 		log("Java compiler finished in " + duration + " ms.");
 	    return binBasedir;
 	}
 	
 	public DebugCompileProgress getDebugCompileProgress() {
 		return debugCompileProgress;
 	}
 	
 	/**
 	 * Takes the filename from the path and returns a suitable project name.
 	 * 
 	 * '.' and '-' are both replaced by '_' as they are not valid characters in a java identifier.
 	 * @param f
 	 * @return
 	 */
 	public static String createProjectName(File f)
 	{
 		String name = f.getName();
 		// replace '.' with '_'
 		name = name.replace('.', '_');
 		// replace '-' with '_'
 		name = name.replace('-', '_');
 		return name;
 	}
 	
 	private void log(String message){
 		System.out.println(message);
 	}
 
 }
