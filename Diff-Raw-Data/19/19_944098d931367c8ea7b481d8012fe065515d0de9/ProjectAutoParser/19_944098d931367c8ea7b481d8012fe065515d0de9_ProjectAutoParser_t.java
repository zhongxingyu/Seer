 package org.nitlanguage.ndt.core.asthelpers;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.nitlanguage.ndt.core.BuildMsg;
 import org.nitlanguage.ndt.core.PluginParams;
 import org.nitlanguage.ndt.core.builder.NitCompilerMessageInterpreter;
 import org.nitlanguage.ndt.core.plugin.NitActivator;
import org.nitlanguage.ndt.core.plugin.NitInstallation;
 
 /**
  * 
  * @author lucas.bajolet
  */
 public class ProjectAutoParser {
 
 	
 	/**
 	 * Project to parse
 	 */
 	private IProject projectToParse;
 
 	/**
 	 * The Parser Helper
 	 */
 	private AstParserHelper aph;
 
 	/**
 	 * Target of parser, updated on every Visit operation
 	 */
 	private IFile target;
 
 	/**
 	 * Reposit of all problematic files in the current project, updated on each
 	 * compilation
 	 */
 	private HashMap<String, IFile> nitFilesOfProject;
 
 	/**
 	 * Job instance, used to keep track of what is executing and to add new
 	 * elements to parse
 	 */
 	private CompilerCallLightJob cclj;
 
 	/**
 	 * Constructor
 	 */
 	public ProjectAutoParser() {
 		this.aph = new AstParserHelper();
 		this.nitFilesOfProject = new HashMap<String, IFile>();
 		this.cclj = new CompilerCallLightJob(BuildMsg.PARSING);
 	}
 
 	/**
 	 * Sets the project to be fully parsed
 	 * @param proj Projet to be set as default
 	 */
 	public void setProject(IProject proj) {
 		this.projectToParse = proj;
 		try {
 			proj.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 		} catch (CoreException e1) {
 			if (NitActivator.DEBUG_MODE)
 				e1.printStackTrace();
 		}
 		ParsingJob pj = new ParsingJob(BuildMsg.PARSING_PROJECT
 				+ this.projectToParse.getName());
 		try {
 			this.nitFilesOfProject.clear();
 			proj.accept(new NitFilesOfProjectParser());
 			pj.schedule();
 		} catch (CoreException e) {
 			if (NitActivator.DEBUG_MODE)
 				e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Launches the job to build the reposit of files in the project
 	 * @param proj The project to parse
 	 * @return Reposit of IFiles for the filenames
 	 */
 	public HashMap<String, IFile> buildFilesInProjectRepo(IProject proj) {
 		this.nitFilesOfProject.clear();
 		try {
 			proj.accept(new NitFilesOfProjectParser());
 		} catch (CoreException e) {
 			if (NitActivator.DEBUG_MODE)
 				e.printStackTrace();
 		}
 		return nitFilesOfProject;
 	}
 
 	/**
 	 * Adds a job to the queue of files to be parsed
 	 * @param fichier The file to add to the parsing queue
 	 */
 	public void addToQueue(IFile fichier) {
 		buildFilesInProjectRepo(fichier.getProject());
 		this.cclj.queueJob(fichier);
 	}
 
 	/**
 	 * Job to call compiler on nit files 
 	 * Adds markers on errors
 	 */
 	private class CompilerCallLightJob extends Job {		
 		/**
 		 * Boolean set to true if a job is active at the moment
 		 */
 		private boolean isActive;
 
 		/**
 		 * Queue for the jobs to be done
 		 */
 		ConcurrentLinkedQueue<IFile> workToBeDone;
 
 		/**
 		 * Helper to parse the messages returned by the nit compiler
 		 */
 		NitCompilerMessageInterpreter ncmi;
 
 		/**
 		 * Default constructor for the job
 		 * 
 		 * @param name
 		 */
 		public CompilerCallLightJob(String name) {
 			super(name);
 			workToBeDone = new ConcurrentLinkedQueue<IFile>();
 			this.isActive = false;
 			this.ncmi = new NitCompilerMessageInterpreter();
 		}
 
 		/**
 		 * Add new job to the queue of jobs
 		 * 
 		 * @param fileToAdd
 		 *            The IFile to parse
 		 */
 		public void queueJob(IFile fileToAdd) {
 			if (!workToBeDone.contains(fileToAdd)) {
 				workToBeDone.add(fileToAdd);
 			}
 			if (this.isActive == false) {
 				this.schedule();
 			}
 		}
 
 		private void logCompilerNotFound(){
 			NitActivator
 			.getDefault()
 			.getLog()
 			.log(new Status(
 					Status.ERROR,
 					BuildMsg.COMPILATION_ERROR,
 					BuildMsg.ERROR_COMPILER_NOT_FOUND));
 		}
 		private String constructCompilerCommand(String compilerPath)
 		{
 			return compilerPath + " "
 					+ BuildMsg.COMPILER_ARG_ONLY_MM + " "
 					+ BuildMsg.COMPILER_ARG_NO_COLOR;
 		}
 		
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			this.isActive = true;
 			Process compileProcess = null;
 			int totalFiles = nitFilesOfProject.size();
 			AstParserHelper aph = new AstParserHelper();
 
 			monitor.beginTask(BuildMsg.PARSING_TASK, totalFiles);
 
			String pathToCompiler = NitActivator.getDefault().getNitInstallation().getCompiler();
 
 			if (pathToCompiler != null) {
 
 				while (!this.workToBeDone.isEmpty() && !monitor.isCanceled()) {
 
 					IFile toParse = workToBeDone.poll();
 					target = toParse;
 
 					// Parse and add to main AST
 					aph.getAstForDocument(toParse);
 
 					// Remove markers of file
 					try {
 						toParse.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 					} catch (CoreException e2) {
 						if (NitActivator.DEBUG_MODE) e2.printStackTrace();
 					}
 
 					try {
 						String peon = pathToCompiler + " "
 								+ BuildMsg.COMPILER_ARG_ONLY_MM + " "
 								+ BuildMsg.COMPILER_ARG_NO_COLOR + " " 
 								+ toParse.getLocation().toString();
 
 						// Check if compiler is accessible
 						File comp = new File(pathToCompiler);
 						if (!comp.exists() || !comp.isFile() || !comp.canExecute()) {
 							logCompilerNotFound();
 						}
 						compileProcess = Runtime.getRuntime().exec(peon);
 					} catch (IOException e1) {
 						if (NitActivator.DEBUG_MODE) e1.printStackTrace();
 					}
 
 					try {
 						compileProcess.waitFor();
 					} catch (InterruptedException e) {
 						if (NitActivator.DEBUG_MODE) e.printStackTrace();
 					} catch (NullPointerException e) {
 						if (NitActivator.DEBUG_MODE) e.printStackTrace();
 					}
 
 					try {
 						// Get the messages
 						InputStream inpt = compileProcess.getErrorStream();
 						Reader in = new InputStreamReader(inpt, PluginParams.CHARSET);
 						StringBuilder result = new StringBuilder();
 						char[] buf = new char[0x10000];
 						int read = 0;
 						do {
 							read = in.read(buf, 0, buf.length);
 							if (read > 0) {
 								result.append(buf, 0, read);
 							}
 						} while (read >= 0);
 
 						ncmi.addMessagesToProblems(ncmi
 								.processMessagesOfCompiler(result.toString()),
 								target, nitFilesOfProject);
 					} catch (Exception e) {
 						if (NitActivator.DEBUG_MODE)
 							e.printStackTrace();
 					}
 					monitor.worked(1);
 				}
 			}
 
 			this.isActive = false;
 
 			if (monitor.isCanceled()) {
 				this.workToBeDone.clear();
 			}
 
 			monitor.done();
 
 			return Status.OK_STATUS;
 		}
 
 	}
 
 	/**
 	 * A visitor needed to get the files of a project.
 	 */
 	private class NitProjectVisitor implements IResourceVisitor {
 
 		public NitProjectVisitor() {
 		}
 
 		@Override
 		public boolean visit(IResource resource) throws CoreException {
 			if (resource instanceof IFile) {
 				IFile fichier = (IFile) resource;
 				String extension = fichier.getFileExtension();
 				if (extension != null && extension.equals(PluginParams.NIT_FILE_EXTENSION)) {
 					if (aph.getAstForDocument(fichier) == null) {
 						cclj.queueJob(fichier);
 					}
 				}
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * Visitor to get all the files of a project and add them to
 	 *         the Hashmap of files
 	 */
 	private class NitFilesOfProjectParser implements IResourceVisitor {
 
 		@Override
 		public boolean visit(IResource resource) throws CoreException {
 			if (resource instanceof IFile) {
 				IFile fichier = (IFile) resource;
 				String extension = fichier.getFileExtension();
 				if (extension != null && extension.equals(PluginParams.NIT_FILE_EXTENSION)) {
 					nitFilesOfProject.put(fichier.getName(), fichier);
 				}
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * The Eclipse job used to parse all the files in the project (with compiler)
 	 */
 	private class ParsingJob extends Job {
 
 		/**
 		 * Default constructor for the Job
 		 * 
 		 * @param name
 		 *            Name required by the super constructor
 		 */
 		public ParsingJob(String name) {
 			super(name);
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			monitor.beginTask(BuildMsg.PARSING_PROJECT + projectToParse.getName(),
 					100);
 			try {
 				projectToParse.accept(new NitProjectVisitor());
 			} catch (CoreException e) {
 				if (NitActivator.DEBUG_MODE)
 					e.printStackTrace();
 			}
 			monitor.done();
 			return Status.OK_STATUS;
 		}
 
 	}
 }
