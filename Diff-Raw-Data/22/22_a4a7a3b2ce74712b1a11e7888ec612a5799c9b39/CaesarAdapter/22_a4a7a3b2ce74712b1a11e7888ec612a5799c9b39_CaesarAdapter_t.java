 package org.caesarj.ui.builder;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.aspectj.asm.StructureModel;
 import org.aspectj.asm.StructureModelManager;
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.Main;
 import org.caesarj.compiler.ast.phylum.JCompilationUnit;
 import org.caesarj.ui.model.AsmBuilder;
 import org.caesarj.ui.model.StructureModelDump;
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
 public final class CaesarAdapter extends Main {
 
 	private static Logger log = Logger.getLogger(CaesarAdapter.class);
 
 	public static IProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
 
 	private Collection errors;
 
 	private IProgressMonitor progressMonitor;
 
 	private StructureModel model;
 
 	static Logger logger = Logger.getLogger(CaesarAdapter.class);
 
 	public CaesarAdapter(String projectLocation) {
 		super(projectLocation, null);
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
 
 		this.model = StructureModelManager.INSTANCE.getStructureModel();
 		AsmBuilder.preBuild(this.model);
 		boolean success = false;
 		String args[] = new String[sourceFiles.size() + 4];
 
 		int i = 0;
 
 		args[i++] = "-d"; //$NON-NLS-1$
 		args[i++] = outputPath;
 
 		args[i++] = "-classpath"; //$NON-NLS-1$
 		args[i++] = classPath;
 
 		for (Iterator it = sourceFiles.iterator(); it.hasNext();) {
 			args[i++] = it.next().toString();
 		}
 
 		this.errors = errorsArg;
 		this.progressMonitor = progressMonitorArg != null ? progressMonitorArg
 				: NULL_PROGRESS_MONITOR;
 
 		this.progressMonitor.beginTask("compiling source files", sourceFiles //$NON-NLS-1$
 				.size() + 1);
 
 		try {
 			success = run(args);
 		} 
 		catch (RuntimeException e) {
 			e.printStackTrace();
 			errors.add("internal compiler error: " + e.toString());	
 		}
 		AsmBuilder.postBuild(this.model);
 		//_dumpModel("final structure model", this.model); //$NON-NLS-1$
 		
 		return success;
 	}
 	
 	protected JCompilationUnit parseFile(File file, KjcEnvironment env) {
 		if (this.progressMonitor.isCanceled()) {
 			return null;
 		}
 
 		JCompilationUnit res;
 		this.progressMonitor.subTask("compiling " + file.getName()); //$NON-NLS-1$
 		res = super.parseFile(file, env);
 		this.progressMonitor.worked(1);
 
 		//AsmBuilder.build(res, this.model);
 
 		return res;
 	}
 	
 	protected void preWeaveProcessing(JCompilationUnit[] cu) {
         for (int i = 0; i < cu.length; i++) {
             AsmBuilder.build(cu[i], this.model);
         }
     }
 	
 
 	protected void weaveClasses() {
 		if (this.progressMonitor.isCanceled()) {
 			return;
 		}
 
 		this.progressMonitor.subTask("weaving classes..."); //$NON-NLS-1$
 
 		AsmBuilder.preWeave(this.model);
 
 		//_dumpModel("structure model before weave", model);
 		// add model to world and WEAVE
 		//		CaesarBcelWorld world = CaesarBcelWorld.getInstance();
 		//		world.setModel(model);
 
 		super.weaveClasses();
 
 		this.progressMonitor.worked(1);
 	}
 
 	private void _dumpModel(String description, StructureModel modelArg) {
 
 		log.debug("--- " + description + " ---"); //$NON-NLS-1$ //$NON-NLS-2$
 		StructureModelDump modelDumpBeforeWeave = new StructureModelDump(
 				System.out);
 		modelDumpBeforeWeave.print("", modelArg.getRoot()); //$NON-NLS-1$
 	}
 }
