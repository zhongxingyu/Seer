 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright � 2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: CaesarAdapter.java,v 1.36 2006-10-06 17:05:47 gasiunas Exp $
  */
 
 package org.caesarj.ui.builder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.aspectj.weaver.IWeaveRequestor;
 import org.aspectj.weaver.bcel.UnwovenClassFile;
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.KjcOptions;
 import org.caesarj.compiler.Main;
 import org.caesarj.compiler.asm.CaesarJAsmManager;
 import org.caesarj.compiler.aspectj.CaesarBcelWorld;
 import org.caesarj.compiler.ast.phylum.JCompilationUnit;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.CWarning;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.UnpositionedError;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * This is the main interface to caesar compiler. Responsible for invoking the
  * compiler, displaying progress, building the structure model and collecting
  * compiler errors.
  * 
  * @author Ivica Aracic <ivica.aracic@bytelords.de>
  */
 public final class CaesarAdapter extends Main implements IWeaveRequestor {
 	
 	//protected final static boolean buildAsm = true;
     //protected final static boolean printAsm = true;
     
     public static IProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
 
 	private Collection errors;
 
 	private IProgressMonitor progressMonitor;
 
 	private int worked;
 	
 	private boolean weave = true;
 	
 	static Logger logger = Logger.getLogger(CaesarAdapter.class);
 
 	public CaesarAdapter(String projectLocation) {
 		this(projectLocation, 0, true);
 	}
 	
 	public CaesarAdapter(String projectLocation, int worked, boolean weave) {
 		super(projectLocation, null);
 		CaesarAdapter.buildAsm = true;
 		CaesarAdapter.printAsm = false;
 		this.weave = weave;
 		this.worked = worked;
 	}
 
 	public void reportTrouble(PositionedError error) {
 		/**
 		 * TODO: move filtering of messages to compiler
 		 */
 		if (error instanceof CWarning) {
             if (options.warning != 0 && filterWarning((CWarning)error)) {
             	this.errors.add(error);
             }
         }
 		else {
 			this.errors.add(error);
 		}
 		super.reportTrouble(error);
 	}
 	
 	public void reportTrouble(UnpositionedError error) {
 		this.errors.add(error);
 		super.reportTrouble(error);
     }
 
 	public boolean compile(Collection sourceFiles, String classPath,
 			String outputPath, Collection errorsArg) {
 		return compile(sourceFiles, classPath, outputPath, errorsArg, null);
 	}
 
 	public boolean compile(Collection sourceFiles, String classPath,
 			String outputPath, Collection errorsArg,
 			IProgressMonitor progressMonitorArg) {
 
 		// The singleton is not used anymore, because the plugin can have more than
 		// one project and each project must have a structure model
 		//this.model = StructureModelManager.INSTANCE.getStructureModel();
 		
 		boolean success = false;
 		String args[] = new String[sourceFiles.size() + 6];
 
 		int i = 0;
 
 		args[i++] = "-d"; //$NON-NLS-1$
 		args[i++] = outputPath;
 
 		args[i++] = "-classpath"; //$NON-NLS-1$
 		args[i++] = classPath;
 		
 		args[i++] = "-O"; // [mef] test compilation without optimization
 		args[i++] = "9";
 
 		for (Iterator it = sourceFiles.iterator(); it.hasNext();) {
 			args[i++] = it.next().toString();
 		}
 
 		this.errors = errorsArg;
 		this.progressMonitor = progressMonitorArg != null ? progressMonitorArg
 				: NULL_PROGRESS_MONITOR;
 		
 		this.progressMonitor.beginTask(
 				"Compiling source files", //$NON-NLS-1$ 
 				sourceFiles.size() * 3); // Approximately ( 1 for parsing, 2 for weaving)
 		
 		try {
 			success = run(args);
 		}
 		catch (RuntimeException e) {
			e.printStackTrace();
 			errors.add("Internal compiler error: " + e.toString());	
 		}
 		
 		this.progressMonitor.done();
 		return success;
 	}
 	
 	protected void notifyWork(int inc) {
 		this.worked += inc;
 		this.progressMonitor.worked(inc);
 	}
 	
     protected JCompilationUnit[] parseFiles(KjcEnvironment environment) {
     	if (this.progressMonitor.isCanceled()) {
 			// TODO - check how to stop compilation
     		//return null;
 		}
 
     	JCompilationUnit[] res = super.parseFiles(environment);
     	
 		this.progressMonitor.subTask("Compiling..."); //$NON-NLS-1$
 		this.notifyWork(1);
 		
 		return res;
     }
     
 	protected JCompilationUnit parseFile(File file, KjcEnvironment env) {
 		if (this.progressMonitor.isCanceled()) {
 			// TODO - check how to stop compilation
     		//return null;
 		}
 
 		this.progressMonitor.subTask("Parsing " + file.getName()); //$NON-NLS-1$
 
 		JCompilationUnit res = super.parseFile(file, env);
 		
 		this.notifyWork(1);
 		
 		return res;
 	}
 
 	public void genCode(TypeFactory factory, JCompilationUnit cus[]) {
 		if (this.progressMonitor.isCanceled()) {
 			return;
 		}
 		
 		this.progressMonitor.subTask("Generating code..."); //$NON-NLS-1$
 		
 		super.genCode(factory, cus);
 	}
 	
 	protected void performWeaving(CaesarBcelWorld bcelWorld) 
 		throws UnpositionedError, IOException, PositionedError {
 		
 		if (this.progressMonitor.isCanceled()) {
 			return;
 		}
 		
 		this.progressMonitor.subTask("Weaving classes..."); //$NON-NLS-1$
 
 		weaver.performWeaving(bcelWorld, this);
 	}
 	
 	/**
 	 * @return Returns the CaesarJAsmManager used on the compilation.
 	 */
 	public CaesarJAsmManager getAsmManager() {
 		return this.asmManager;
 	}
 
 	/**
 	 * Builds the abstract syntax tree for the given file and environment.
 	 * 
 	 * @param file
 	 * @param env
 	 * @return the root of the AST
 	 */
 	public JCompilationUnit buildAST(File file, KjcEnvironment env){
 		return super.parseFile(file, env);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.caesarj.compiler.MainSuper#createEnvironment(org.caesarj.compiler.KjcOptions)
 	 */
 	protected KjcEnvironment createEnvironment(KjcOptions options) {
 		KjcEnvironment env = super.createEnvironment(options);
 		this.env = env;
         return env;
     }
 	
 	private KjcEnvironment env = null;
 	
 	public KjcEnvironment getKjcEnvironment(){
 		return this.env;
 	}
 	
 	public boolean noWeaveMode() {
 		return ! this.weave;
     }
 	
 	// Implementation for IWeaveRequestor
 	
 	public void acceptResult(UnwovenClassFile result) {
 		if (result != null) {
 			this.progressMonitor.subTask("Weaving completed for " + result.getClassName()); //$NON-NLS-1$
 		}
 		this.notifyWork(1);
 	}
 	public void processingReweavableState() {
 		this.progressMonitor.subTask("Processing reweavable state..."); //$NON-NLS-1$
 		this.notifyWork(1);
 	}
 	public void addingTypeMungers() {
 		this.progressMonitor.subTask("Adding type mungers..."); //$NON-NLS-1$
 		this.notifyWork(1);
 	}
 	public void weavingAspects() {
 		this.progressMonitor.subTask("Weaving aspects..."); //$NON-NLS-1$
 		this.notifyWork(1);
 	}
 	public void weavingClasses() {
 		this.progressMonitor.subTask("Weaving classes..."); //$NON-NLS-1$
 		this.notifyWork(1);
 	}
 	public void weaveCompleted() {
 		this.progressMonitor.subTask("Weaving completed!"); //$NON-NLS-1$
 		this.notifyWork(1);
 	}
 
 	public int getWorked() {
 		return worked;
 	}
 
 	public void setWorked(int worked) {
 		this.worked = worked;
 	}
 }
