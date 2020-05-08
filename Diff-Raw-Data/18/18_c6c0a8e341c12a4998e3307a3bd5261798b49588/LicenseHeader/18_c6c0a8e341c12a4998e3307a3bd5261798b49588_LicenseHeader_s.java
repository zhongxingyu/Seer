 package de.uni_koblenz.ist.utilities.license_header;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 
 import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
 
 public class LicenseHeader {
 
 	private static final String INDENT = "    ";
 	private static final String JAVA_FIRST_LINE = "/*";
 	private static final String JAVA_PREFIX = " * ";
 	private static final String JAVA_LAST_LINE = " */";
 
 	private static enum ParseState {
 		/**
 		 * Denotes the state when the parser is before a header (or just left a
 		 * header)
 		 */
 		BEFORE_HEADER,
 
 		/**
 		 * Denotes the state when the parser is inside an old header (skipping
 		 * it)
 		 */
 		IN_HEADER,
 
 		/**
 		 * Denotes the state when the parser passed through all headers (when
 		 * the package statement, an import statement or the class statement is
 		 * hit)
 		 */
 		AFTER_HEADERS
 	}
 
 	public static void main(String[] args) {
 		System.out.println(Arrays.toString(args));
 		CommandLine cl = processCommandLineOptions(args);
 		assert cl.hasOption('i');
 		assert cl.hasOption('l');
 		LicenseHeader lh = new LicenseHeader(cl.getOptionValue('i'), cl
 				.getOptionValue('l'), cl.hasOption('r'), cl.hasOption('V'));
 		try {
 			lh.process();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private static CommandLine processCommandLineOptions(String[] args) {
 		OptionHandler oh = new OptionHandler("java "
 				+ LicenseHeader.class.getName(), "1.0");
 		Option input = new Option("i", "input", true,
 				"(required): The file or directory to process.");
 		// input.setArgs(1);
		input.setRequired(false);
 		input.setArgName("fileOrDirectory");
 		oh.addOption(input);
 
 		Option licenceHeader = new Option(
 				"l",
 				"licence",
 				true,
 				"(required): The file containing the licence header in the correct format. This file should be in plain text without any language specific syntax for comments.");
 		// licenceHeader.setArgs(1);
		licenceHeader.setRequired(false);
 		licenceHeader.setArgName("licenseFile");
 		oh.addOption(licenceHeader);
 
 		// TODO add this feature
 		// Option fileType = new Option(
 		// "x",
 		// "--xml",
 		// false,
 		// "(optional): This flag tells the program to operate on xml files instead of java files (experimental).");
 		// fileType.setRequired(false);
 		// oh.addOption(fileType);
 
 		Option recursive = new Option(
 				"r",
 				"recursive",
 				false,
 				"(optional): This flag tells the program to process the given directory fully recursively. If only a file is given, this option is ignored. If this flag is not set, only the given directory is processed, without subdirectories.");
 		recursive.setRequired(false);
 		oh.addOption(recursive);
 
 		Option verbose = new Option(
 				"V",
 				"verbose",
 				false,
 				"(optional): This flag tells the program to be more verbose while processing the files. If it is not set, only a summary will be given in the end.");
 		verbose.setRequired(false);
 		oh.addOption(verbose);
 
 		return oh.parse(args);
 	}
 
 	private File input;
 	private File licence;
 	private boolean fullyRecursive;
 	private boolean verbose;
 	private int newlyAdded;
 	private int replaced;
 
 	public LicenseHeader(String input, String licence, boolean fullyRecursive,
 			boolean verbose) {
 		super();
 		this.input = new File(input);
 		this.licence = new File(licence);
 		this.fullyRecursive = fullyRecursive;
 		this.verbose = verbose;
 		newlyAdded = 0;
 		replaced = 0;
 	}
 
 	private void printIndent(int level) {
 		for (int i = 0; i < level; i++) {
 			System.out.print(INDENT);
 		}
 	}
 
 	public void process() throws IOException {
 		if (!input.exists()) {
 			throw new FileNotFoundException("The given input file/directory \""
 					+ input.getAbsolutePath() + "\" does not exist.");
 		}
 		if (!licence.exists()) {
 			throw new FileNotFoundException("The given licence file \""
 					+ licence.getAbsolutePath() + "\" does not exist.");
 		}
 		if (licence.isDirectory()) {
 			throw new IllegalArgumentException("The given licence file \""
 					+ licence.getAbsolutePath() + "\" is a directory");
 		}
 		if (input.isDirectory()) {
 			processDirectory(input, 0);
 		} else {
 			processJavaFile(input, 0, JAVA_FIRST_LINE, JAVA_PREFIX,
 					JAVA_LAST_LINE);
 		}
 		int processed = replaced + newlyAdded;
 		System.out.println();
 		System.out.println("Summary:");
 		System.out.println("Processed " + processed + " files.");
 		System.out.println(newlyAdded + "/" + processed + " files didn't have a license header.");
 		System.out.println(replaced + "/" + processed + " files' headers were replaced by the new one.");
 	}
 
 	private void processDirectory(final File toProcess, int level)
 			throws IOException {
 
 		File[] directories = toProcess.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.isDirectory()
 						&& !pathname.getName().equals(".svn");
 			}
 		});
 
 		File[] javaFilesToProcess = toProcess.listFiles(new FilenameFilter() {
 
 			@Override
 			public boolean accept(File dir, String name) {
 				return dir.getAbsolutePath()
 						.equals(toProcess.getAbsolutePath())
 						&& name.toLowerCase().endsWith(".java");
 			}
 		});
 
 		if (fullyRecursive) {
 			for (File currentSubdirectory : directories) {
 				if (verbose) {
 					printIndent(level);
 					System.out.println("Entering directory "
 							+ currentSubdirectory.getName());
 				}
 				processDirectory(currentSubdirectory, level + 1);
 				if (verbose) {
 					printIndent(level);
 					System.out.println("Leaving directory "
 							+ currentSubdirectory.getName());
 				}
 			}
 		}
 
 		for (File currentJavaFile : javaFilesToProcess) {
 			processJavaFile(currentJavaFile, level, JAVA_FIRST_LINE,
 					JAVA_PREFIX, JAVA_LAST_LINE);
 		}
 	}
 
 	private void processJavaFile(File toProcess, int level, String firstLine,
 			String prefix, String lastLine) throws IOException {
 		if (verbose) {
 			printIndent(level);
			System.out.println("Processing file" + toProcess.getName());
 		}
 
 		List<String> outputLines = new LinkedList<String>();
 
 		// add header to outputLines
 		BufferedReader reader = new BufferedReader(new FileReader(licence));
 		outputLines.add(firstLine);
 		String currentLine = null;
 		do {
 			currentLine = reader.readLine();
 			if (currentLine != null) {
 				outputLines.add(prefix + currentLine);
 			}
 		} while (currentLine != null);
 		outputLines.add(lastLine);
 
 		reader.close();
 		// now open the current input file
 		reader = new BufferedReader(new FileReader(toProcess));
 		ParseState state = ParseState.BEFORE_HEADER;
 		int skippedHeaders = 0;
 		do {
 			currentLine = reader.readLine();
 			if (currentLine != null) {
 				switch (state) {
 				case BEFORE_HEADER:
					if (currentLine.trim().startsWith(prefix.trim())) {
 						state = ParseState.IN_HEADER;
 						skippedHeaders++;
 						continue;
 					}
 					if (currentLine.contains("package")
 							|| currentLine.contains("import")
 							|| currentLine.contains("class")) {
						// copy the line
						outputLines.add(currentLine);
 						state = ParseState.AFTER_HEADERS;
						continue;
 					}
 					break;
 				case IN_HEADER:
 					if (currentLine.trim().endsWith(lastLine.trim())) {
 						state = ParseState.BEFORE_HEADER;
 					}
 					break;
 				case AFTER_HEADERS:
 					// normal case, just copy
 					outputLines.add(currentLine);
 					break;
 				}
 			}
 		} while (currentLine != null);
 		if (skippedHeaders > 0) {
 			if (verbose) {
 				printIndent(level + 1);
 				System.out.println("Skipped " + skippedHeaders
 						+ " headers and replaced "
 						+ (skippedHeaders == 1 ? "it" : "them")
 						+ " with new header.");
 			}
 			replaced++;
 		} else {
 			if (verbose) {
 				printIndent(level + 1);
 				System.out.println("Added header.");
 			}
 			newlyAdded++;
 		}
 
 		PrintWriter writer = new PrintWriter(toProcess);
 		for (String currentOutputLine : outputLines) {
 			writer.println(currentOutputLine);
 		}
 		writer.flush();
 		writer.close();
 
 	}
 }
