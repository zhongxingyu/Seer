 package br.ufal.cideei.handlers;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.internal.core.JavaModel;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
 import br.ufal.cideei.features.IFeatureExtracter;
 import br.ufal.cideei.soot.AssignmentsCounter;
 import br.ufal.cideei.soot.SootManager;
 import br.ufal.cideei.soot.analyses.FeatureSensitiviteFowardFlowAnalysis;
 import br.ufal.cideei.soot.analyses.TestReachingDefinitions;
 import br.ufal.cideei.soot.analyses.wholeline.WholeLineLiftedReachingDefinitions;
 import br.ufal.cideei.soot.analyses.wholeline.WholeLineRunnerReachingDefinitions;
 import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;
 import br.ufal.cideei.util.MethodDeclarationSootMethodBridge;
 
 import soot.G;
 import soot.PackManager;
 import soot.Scene;
 import soot.SootClass;
 import soot.SootMethod;
 import soot.Transform;
 
 public class DoAnalysisOnClassPath extends AbstractHandler {
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		try {
 			IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
 			Object firstElement = selection.getFirstElement();
 			if (firstElement instanceof IJavaProject) {
 				IJavaProject javaProject = (IJavaProject) firstElement;
 
 				IClasspathEntry[] classPathEntries = null;
 				try {
 					classPathEntries = javaProject.getResolvedClasspath(true);
 				} catch (JavaModelException e) {
 					e.printStackTrace();
 					throw new ExecutionException("No source classpath identified");
 				}
 
				/*
				 * To build the path string variable that will represent Soot's
				 * classpath we will first iterate through all libs (.jars)
				 * files, then through all source classpaths.
				 * 
				 * FIXME: WARNING: A bug was found on Soot, in which the
				 * FileSourceTag would contain incorrect information regarding
				 * the absolute localtion of the source file. In order to
				 * workaround this, the classpath must be injected into the
				 * FeatureModelInstrumentorTransformer class (it is done though
				 * its constructor).
				 * 
				 * As a consequence, we CANNOT build an string with all
				 * classpaths that contains source code for the project and thus
				 * one only source code classpath can be analysed at a given
				 * time.
				 * 
				 * This seriously restricts the range of projects that can be
				 * analysed with this tool.
				 */
 				StringBuilder libsPaths = new StringBuilder();
 				for (IClasspathEntry entry : classPathEntries) {
 					if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
 						File file = entry.getPath().makeAbsolute().toFile();
 						if (file.isAbsolute()) {
 							libsPaths.append(file.getAbsolutePath() + File.pathSeparator);
 						} else {
 							libsPaths.append(ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString() + File.pathSeparator);
 						}
 					}
 				}
 				for (IClasspathEntry entry : classPathEntries) {
 					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 						this.addPacks(javaProject, entry, libsPaths.toString());
 					}
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			G.v().reset();
 		}
 	}
 
 	private void addPacks(IJavaProject javaProject, IClasspathEntry entry, String libs) {
 		/*
 		 * if the classpath entry is "", then JDT will complain about it.
 		 */
 		String classPath;
 		if (entry.getPath().toOSString().equals(File.separator + javaProject.getElementName())) {
 			classPath = javaProject.getResource().getLocation().toFile().getAbsolutePath();
 		} else {
 			classPath = ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath()).getLocation().toOSString();
 		}
 
 		SootManager.configure(classPath + File.pathSeparator + libs);
 
 		IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().newExtracter(javaProject);
 		IPackageFragmentRoot[] packageFragmentRoots = javaProject.findPackageFragmentRoots(entry);
 		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
 			IJavaElement[] children = null;
 			try {
 				children = packageFragmentRoot.getChildren();
 			} catch (JavaModelException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			for (IJavaElement child : children) {
 				IPackageFragment packageFragment = (IPackageFragment) child;
 				ICompilationUnit[] compilationUnits = null;
 				try {
 					compilationUnits = packageFragment.getCompilationUnits();
 				} catch (JavaModelException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				for (ICompilationUnit compilationUnit : compilationUnits) {
 					// CompilationUnit a = compilationUnit.
 					String fragmentName = packageFragment.getElementName();
 					String compilationName = compilationUnit.getElementName();
 					StringBuilder qualifiedNameStrBuilder = new StringBuilder(fragmentName);
 					// If it's the default package:
 					if (qualifiedNameStrBuilder.length() == 0) {
 						// Remove ".java" suffix
 						qualifiedNameStrBuilder.append(compilationName.substring(0, compilationName.length() - 5));
 					} else {
 						// Remove ".java" suffix
 						qualifiedNameStrBuilder.append(".").append(compilationName.substring(0, compilationName.length() - 5));
 					}
 
 					// This goes into Soot loadAndSupport
 					SootClass sootClass = SootManager.loadAndSupport(qualifiedNameStrBuilder.toString());
 				}
 			}
 		}
 		Scene.v().loadNecessaryClasses();
 
 		Transform t = new Transform("jap.fminst", FeatureModelInstrumentorTransformer.v(extracter, classPath));
 		PackManager.v().getPack("jap").add(t);
 
 		Transform t2 = new Transform("jap.rdrunner", WholeLineRunnerReachingDefinitions.v());
 		PackManager.v().getPack("jap").add(t2);
 
 		Transform t3 = new Transform("jap.rdlifted", WholeLineLiftedReachingDefinitions.v());
 		PackManager.v().getPack("jap").add(t3);
 
 		// #ifdef METRICS
 		Transform t4 = new Transform("jap.asgnmc", AssignmentsCounter.v());
 		PackManager.v().getPack("jap").add(t4);
 		// #endif
 
 		SootManager.runPacks(extracter);
 
 		// #ifdef METRICS
 		String format = "|%1$-50s|%2$-50s|\n";
 		double runnerTime = ((double) WholeLineRunnerReachingDefinitions.v().getAnalysesTime()) / 1000000;
 		double liftedTime = ((double) WholeLineLiftedReachingDefinitions.v().getAnalysesTime()) / 1000000;
 		System.out.format(format, "runner took:", runnerTime + "ms");
 		System.out.format(format, "lifted took:", liftedTime + "ms");
 		System.out.format(format, "runner/lifted:", runnerTime / liftedTime);

 		long runnerFlowThroughCounter = FeatureSensitiviteFowardFlowAnalysis.getFlowThroughCounter();
 		System.out.format(format, "Runner no. of flowThroughs called: ", runnerFlowThroughCounter);
 		long liftedFlowThroughCounter = TestReachingDefinitions.getFlowThroughCounter();
 		System.out.format(format, "Lifted no. of flowThroughs called: ", liftedFlowThroughCounter);
 
 		long totalBodies = FeatureModelInstrumentorTransformer.getTotalBodies();
 		long coloredBodies = FeatureModelInstrumentorTransformer.getTotalColoredBodies();
 
 		System.out.format(format, "Total bodies: ", totalBodies);
 		System.out.format(format, "Bodies with at least 1 ft.: ", coloredBodies);
 		System.out.format(format, "Percentage: ", ((((double) coloredBodies) / ((double) (totalBodies))) * 100) + "%");
 		System.out.format(format, "Total of assignments: ", AssignmentsCounter.v().getCounter());
		System.out.format(format, "Average assignments/bodies: ", AssignmentsCounter.v().getCounter() / totalBodies);
 
 		WholeLineLiftedReachingDefinitions.v().reset();
 		WholeLineRunnerReachingDefinitions.v().reset();
 		FeatureModelInstrumentorTransformer.v().reset();
 		AssignmentsCounter.v().reset();
 		FeatureSensitiviteFowardFlowAnalysis.reset();
 		TestReachingDefinitions.reset();
 		// #endif
 	}
 }
