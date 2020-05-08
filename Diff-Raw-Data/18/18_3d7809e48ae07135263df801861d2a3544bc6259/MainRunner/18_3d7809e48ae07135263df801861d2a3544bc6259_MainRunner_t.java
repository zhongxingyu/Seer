 package br.edu.ufcg.dsc;
 
 /**
  * @author Jefferson Almeida - jra at cin dot ufpe dot br 
  */
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPlatformRunnable;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.application.WorkbenchAdvisor;
 import org.eclipse.ui.testing.ITestHarness;
 import org.eclipse.ui.testing.TestableObject;
 import safeEvolution.fileProperties.FilePropertiesObject;
 import safeEvolution.fileProperties.FilePropertiesReader;
 import edu.mit.csail.sdg.alloy4.Err;
 import br.edu.ufcg.dsc.builders.ProductGenerator;
 import br.edu.ufcg.dsc.evaluation.Analyzer;
 import br.edu.ufcg.dsc.gui.AppWindow;
 import br.edu.ufcg.dsc.util.AssetNotFoundException;
 import br.edu.ufcg.dsc.util.DirectoryException;
 
 public class MainRunner implements IPlatformRunnable, ITestHarness {
 
 	private TestableObject testableObject;
 	private String[] arguments;
 
 	public Object run(Object args) throws Exception {
 		this.setArguments((String[]) args);
 		testableObject = PlatformUI.getTestableObject();
 		testableObject.setTestHarness(this);
 		Display display = PlatformUI.createDisplay();
 		try {
 			System.out.println("SPL Refactoring Checker");
 			PlatformUI.createAndRunWorkbench(display, new NullAdvisor());
 			AppWindow refinementChecker = new AppWindow("Software Product Line Refinement Checker");
 			refinementChecker.open();
 			Shell shell = refinementChecker.getShell();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch())
 					display.sleep();
 			}
 			return EXIT_OK;
 		} finally {
 			display.dispose();
 		}
 	}
 
 	public void runTests() {
 		testableObject.testingStarting();
 		System.out.println("... Checking Start");
 		
 		testableObject.runTest(new Runnable() {
 			public void run() {
 				
 				ProductGenerator.MAX_TENTATIVAS = 2000;
 				FilePropertiesReader propertiesReader = new FilePropertiesReader("/home/jefferson/workspace/ferramentaLPSSM/inputFiles/bank4.0.properties");
 				FilePropertiesObject propertiesObject = propertiesReader.getPropertiesObject();
 				try {
 					Analyzer.getInstance().analize(propertiesObject);
 				} catch (DirectoryException e) {
 					e.printStackTrace();
 				} catch (Err e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (AssetNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		testableObject.testingFinished();
 	}
 	
 	public void setArguments(String[] arguments) {
 		this.arguments = arguments;
 	}
 	public String[] getArguments() {
 		return arguments;
 	}
 	public static class NullAdvisor extends WorkbenchAdvisor {
 		@Override
 		public String getInitialWindowPerspectiveId() {
 			return null;
 		}
 	}
 	
 }
