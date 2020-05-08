 /**
  * Copyright (C) 2008  Johann Prieur <johann.prieur@gmail.com>
  * Copyright (C) 2012  Severin Heiniger <severinheiniger@gmail.com>
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.valable.job;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.gnome.vala.Class;
 import org.gnome.vala.DataType;
 import org.gnome.vala.ReportItem;
import org.gnome.vala.ReportItemSeverity;
 import org.gnome.vala.SourceFile;
 import org.gnome.vala.SourceReference;
 import org.gnome.vala.TypeSymbol;
 
 import org.valable.model.ValaPackage;
 import org.valable.model.ValaProject;
 import org.valable.model.ValaSource;
 
 public class ValaBuildJob extends Job {
 
 	private static final Logger LOG = Logger.getLogger(ValaBuildJob.class
 			.getName());
 
 	private static final Pattern UNKNOWN_NAME_MESSAGE_PATTERN = Pattern
 			.compile("The name `(\\S+?)' does not exist in the context of `([^\\.']+?)(\\..+?)?'");
 
 	private ValaProject project;
 	private String baseDir;
 	private final Set<IFile> filesToCompile;
 	private final String valac;
 	private final String vapi;
 	private final String output;
 	private final Set<String> packages = new HashSet<String>();
 
 	private final Map<IFile, List<String>> lines = new HashMap<IFile, List<String>>();
 
 	public ValaBuildJob(Set<IFile> filesToCompile, String valac, String vapi,
 			String output) throws CoreException {
 		super("Compiling Vala...");
 
 		this.filesToCompile = filesToCompile;
 		this.valac = valac;
 		this.vapi = vapi;
 		this.output = output;
 
 		if (!filesToCompile.isEmpty()) {
 			long startTime = System.currentTimeMillis();
 			initialise();
 			long endTime = System.currentTimeMillis();
 			LOG.info("ValaBuildJob initialized in " + (endTime - startTime)
 					+ "ms");
 		}
 	}
 
 	/**
 	 * Ensure each file is parsed and appropriate packages and dependencies
 	 * added (recursively).
 	 * 
 	 * @throws CoreException
 	 */
 	private void initialise() throws CoreException {
 		for (IFile file : new HashSet<IFile>(filesToCompile)) {
 			if (project == null) {
 				project = ValaProject.getProject(file);
 				baseDir = file.getProject().getLocation().toOSString();
 			}
 
 			// Parse the file
 			ValaSource source = project.getSource(file);
 			lines.put(file, source.parse());
 
 			// Remove any existing markers
 			for (IMarker marker : file.findMarkers(IMarker.PROBLEM, false, 0)) {
 				marker.delete();
 			}
 
 			for (ReportItem reportItem : source.getReportItems()) {
 				SourceReference sourceReference = reportItem.getSource();
 				int startLine = sourceReference.getBegin().getLine();
 				int endLine = sourceReference.getEnd().getLine();
 				int startColumn = sourceReference.getBegin().getColumn();
 				int endColumn = sourceReference.getEnd().getColumn();
 
 				IMarker marker = file.createMarker(IMarker.PROBLEM);
 				marker.setAttribute(IMarker.MESSAGE, reportItem.getMessage());
				ReportItemSeverity severity = reportItem.getSeverity();
				if (severity.equals(ReportItemSeverity.ERROR)) {
					marker.setAttribute(IMarker.SEVERITY,
							IMarker.SEVERITY_ERROR);
				} else if (severity.equals(ReportItemSeverity.WARNING)) {
					marker.setAttribute(IMarker.SEVERITY,
							IMarker.SEVERITY_WARNING);
				} else if (severity.equals(ReportItemSeverity.NOTE)) {
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
				}
 				marker.setAttribute(IMarker.LOCATION, "Line " + startLine);
 				if (startLine == endLine) {
 					int offset = offsetOfLine(lines.get(file), startLine);
 					marker.setAttribute(IMarker.CHAR_START, offset
 							+ startColumn - 1);
 					marker.setAttribute(IMarker.CHAR_END, offset + endColumn);
 				} else {
 					marker.setAttribute(IMarker.LINE_NUMBER, startLine);
 				}
 			}
 
 			// Add dependencies
 			for (ValaPackage pkg : source.getUses()) {
 				packages.add(pkg.getPkgConfigName());
 			}
 
 			SourceFile sourceFile = source.getSourceFile();
 			for (Class cls : sourceFile.getClasses()) {
 				for (DataType baseType : cls.getBaseTypes()) {
 					TypeSymbol baseTypeSymbol = baseType.getDataType();
 					if (baseTypeSymbol instanceof Class) {
 						Class baseClass = (Class) baseTypeSymbol;
 						ValaSource baseClassSource = project
 								.getSource(baseClass);
 						if (baseClassSource != null
 								&& filesToCompile.add(baseClassSource
 										.getSource())) {
 							initialise();
 						}
 					}
 				}
 			}
 		}
 
 		// baseDir is where the files from valac are written
 		// TODO Get valac to follow --directory with --compile
 		baseDir = new File("").getAbsolutePath();
 	}
 
 	/**
 	 * Builds the vala compiler command to compile the files from the given list
 	 * 
 	 * @param filesToCompile
 	 *            the list of files to compile with that command
 	 * @return the valac command to execute to compile the files
 	 */
 	private String[] buildValacCommand() {
 		List<String> bits = new ArrayList<String>();
 
 		// Vala compiler program
 		bits.add(valac);
 
 		// VAPI directory
 		bits.add(String.format("--vapidir=%s", vapi));
 
 		// Output directory
 		bits.add(String.format("--directory=%s", output));
 
 		// Incremental building
 		bits.add("--compile");
 
 		// Packages
 		for (String pkg : packages) {
 			bits.add(String.format("--pkg=%s", pkg));
 		}
 
 		// Vala files to compile
 		for (IFile file : filesToCompile) {
 			bits.add(file.getLocation().toOSString());
 		}
 
 		return bits.toArray(new String[bits.size()]);
 	}
 
 	/**
 	 * Used to facilitate incremental building, this provides the command to
 	 * link together all the individual {@code .o} files.
 	 * 
 	 * @return Command to link the project.
 	 */
 	private String buildLinkCommand() {
 		StringBuilder command = new StringBuilder();
 
 		// Compiler
 		command.append("gcc");
 
 		// Output file
 		command.append(" -o "
 				+ new File(output, project.getName().replaceAll("\\W+", ""))
 						.getAbsolutePath());
 
 		// Object files to build
 		for (File obj : new File(output).listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".o");
 			}
 		})) {
 			command.append(" ").append(obj.getAbsolutePath());
 		}
 
 		// Packages (need to be stated after the object files)
 		for (ValaPackage pkg : project.getUsedPackages()) {
 			command.append(" ").append(
 					commandOutput("pkg-config", "--libs",
 							pkg.getPkgConfigName()));
 		}
 
 		return command.toString();
 	}
 
 	/**
 	 * Return the string output of a command.
 	 * 
 	 * @param args
 	 *            Command to execute
 	 * @return Output of running <var>args</var>
 	 */
 	private String commandOutput(String... args) {
 		Process process;
 		try {
 			System.out.println(Arrays.asList(args));
 			process = Runtime.getRuntime().exec(args);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException(e);
 		}
 
 		StringBuilder output = new StringBuilder();
 
 		Scanner scanner = new Scanner(process.getInputStream());
 		while (scanner.hasNextLine())
 			output.append(scanner.nextLine()).append("\n");
 
 		return output.toString();
 	}
 
 	/**
 	 * Copy the given input streams to the given output streams. Once all the
 	 * inputs have been completed read, the method returns.
 	 * 
 	 * @param out
 	 *            Target stream
 	 * @param inputs
 	 *            Number of {@link InputStream}s to copy.
 	 * @throws IOException
 	 */
 	protected void copyToStream(OutputStream out, InputStream... inputs)
 			throws IOException {
 		byte[] b = new byte[100];
 
 		boolean done = false;
 		while (!done) {
 			done = true;
 			for (InputStream in : inputs) {
 				int read = in.read(b);
 				if (read != -1) {
 					out.write(b, 0, read);
 					System.out.write(b, 0, read);
 					done = false;
 				} else {
 					done &= true;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Run the compilation for this build job, logging output to a console.
 	 * 
 	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected IStatus run(IProgressMonitor monitor) {
 		long startTime = System.currentTimeMillis();
 		if (filesToCompile.isEmpty()) {
 			return Status.OK_STATUS;
 		}
 
 		Runtime runtime = Runtime.getRuntime();
 		PrintStream out = System.out;
 
 		// -- Incrementally build the files, pulling in any dependencies
 		// necessary...
 		//
 		boolean doAgain = false;
 		String[] command = buildValacCommand();
 		try {
 			out.println(Arrays.asList(command) + "\n");
 
 			Process process = runtime.exec(command);
 			InputStream errorStream = process.getErrorStream();
 
 			// for (ValaReportElement element : reportElements) {
 			// Matcher matcher = UNKNOWN_NAME_MESSAGE_PATTERN.matcher(element
 			// .getMessage());
 			// boolean processed = false;
 			// if (element.getSeverity() == Severity.ERROR
 			// && matcher.matches()) {
 			// String missingName = matcher.group(1);
 			// String containingType = matcher.group(2);
 			//
 			// if (project.hasType(missingName)
 			// && project.hasType(containingType)) {
 			// // doAgain |= project.getClass(containingType).
 			// // getDependencies().
 			// // add(project.getClass(missingName));
 			// processed = true;
 			// }
 			//
 			// }
 			// }
 
 			// -- Wait for process to finish...
 			//
 			if (process.waitFor() == 0) {
 				out.println("Success!");
 
 				// Move .o files to the output dir
 				for (IFile valaFile : filesToCompile) {
 					String name = valaFile.getName().concat(".o");
 					out.println(commandOutput("mv",
 							new File(baseDir, name).getAbsolutePath(), output
 									+ "/"));
 				}
 			} else {
 				out.println("Failed.");
 				if (!doAgain)
 					return Status.CANCEL_STATUS;
 			}
 
 			// -- Run again if we've determined new dependencies...
 			//
 			if (doAgain) {
 				out.println("repeating");
 				initialise();
 				return run(monitor);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Status.CANCEL_STATUS;
 		}
 
 		// -- Link the project files together now...
 		//
 		try {
 			String cmd = buildLinkCommand();
 			out.println(cmd);
 			Process link = runtime.exec(cmd);
 			copyToStream(out, link.getInputStream(), link.getErrorStream());
 			link.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Status.CANCEL_STATUS;
 		}
 		long endTime = System.currentTimeMillis();
 		LOG.info("ValaBuildJob run successfully in " + (endTime - startTime)
 				+ "ms");
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Return the offset of the start of the given line number.
 	 * 
 	 * @param lines
 	 *            All lines in the file.
 	 * @param lineNumber
 	 *            Line in the file, starting from 1.
 	 * @return offset, from 0 of the start of line <var>lineNumber</var>.
 	 */
 	private int offsetOfLine(List<String> lines, int lineNumber) {
 		lineNumber--;
 		int count = 0;
 		for (int i = 0; i < lineNumber; i++) {
 			count += lines.get(i).length() + 1;
 		}
 
 		return count;
 	}
 }
