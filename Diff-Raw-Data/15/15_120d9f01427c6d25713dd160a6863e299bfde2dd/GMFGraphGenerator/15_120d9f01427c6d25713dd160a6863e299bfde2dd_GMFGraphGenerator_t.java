 package org.eclipse.gmf.tests.gen;
 
 import java.net.URL;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.codegen.jet.JETException;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.gmf.common.UnexpectedBehaviourException;
 import org.eclipse.gmf.common.codegen.GeneratorBase;
 import org.eclipse.gmf.common.codegen.ImportUtil;
 import org.eclipse.gmf.gmfgraph.Figure;
import org.eclipse.gmf.gmfgraph.util.RuntimeFQNSwitch;
 import org.eclipse.gmf.graphdef.codegen.FigureGenerator;
 
 public class GMFGraphGenerator extends GeneratorBase {
 	private final Config myConfig;
 	private final Emitter myFigureGenerator;
 	private final GMFGraphTree myInputTree;
 
 	public static interface Config {
 		public String getPluginName();
 		public String getMainPackageName();
 	}
 	
 	public static class ConfigImpl implements Config {
 		private final String myMainPackageName;
 		private final String myPluginName;
 
 		public ConfigImpl(String pluginName, String mainPackageName){
 			myPluginName = pluginName;
 			myMainPackageName = mainPackageName;
 		}
 		
 		public String getMainPackageName() {
 			return myMainPackageName;
 		}
 		
 		public String getPluginName() {
 			return myPluginName;
 		}
 	}
 	
 	public interface GMFGraphTree {
 		public TreeIterator getTreeContents();
 		
 		public static class ResourceAdapter implements GMFGraphTree {
 			private final Resource myResource;
 
 			public ResourceAdapter(Resource resource){
 				myResource = resource;
 			}
 			
 			public TreeIterator getTreeContents() {
 				return myResource.getAllContents();
 			}
 		}	
 		
 		public static class EObjectAdapter implements GMFGraphTree {
 			private final EObject myRoot;
 
 			public EObjectAdapter(EObject root){
 				myRoot = root;
 			}
 			
 			public TreeIterator getTreeContents() {
 				return new TreeIteratorWithRoot(myRoot);
 			}
 			
 			private static class TreeIteratorWithRoot implements TreeIterator {
 				private final EObject myRoot;
 				private TreeIterator myContents;
 				private long myVisitedCount;
 				private boolean myIsRootCancelled;
 
 				public TreeIteratorWithRoot(EObject root){
 					myRoot = root;
 				}
 				
 				public void remove(){
 					throw new UnsupportedOperationException();
 				}
 				
 				private boolean isRootVisited(){
 					return myVisitedCount > 0;
 				}
 				
 				private boolean isContentsVisitingStarted(){
 					return myVisitedCount > 1;
 				}
 				
 				public boolean hasNext() {
 					if (myIsRootCancelled){
 						return false;
 					}
 					return !isRootVisited() || myContents.hasNext();
 				}
 
 				public Object next() {
 					if (myIsRootCancelled){
 						throw new IllegalStateException();
 					}
 					Object result;
 					if (isRootVisited()){
 						result = myContents.next();
 					} else {
 						result = myRoot;
 						myContents = myRoot.eAllContents();
 					}
 					myVisitedCount++;
 					return result;
 				}
 				
 				public void prune() {
 					if (!isRootVisited()){
 						throw new IllegalStateException("You have at least try first");
 					}
 					if (isContentsVisitingStarted()){
 						myContents.prune();
 					} else {
 						myIsRootCancelled = true;
 					}
 				}
 			}
 		}	
 	}
 	
 	public GMFGraphGenerator(GMFGraphTree inputTree, Config config){
 		myInputTree = inputTree;
 		myConfig = config;
 		myFigureGenerator = new FigureGeneratorAdapter( //
				new FigureGenerator(getPackageName(), new ImportUtil(getPackageName()), new RuntimeFQNSwitch())
 		);
 	}
 	
 	protected void setupProgressMonitor() {
 		//setupProgressMonitor("Generating GMFGraph plugin", 100);
 	}
 	
 	protected URL getJMergeControlFile() {
 		return null;
 	}
 	
 	protected List createReferencedProjectsList() {
 		return Collections.EMPTY_LIST;
 	}
 	
 	protected void customRun() throws InterruptedException, JETException, UnexpectedBehaviourException {
 		initializeEditorProject(myConfig.getPluginName(), createReferencedProjectsList());
 		try {
 			generateTopLevelFigures();
 		} catch (IllegalStateException e){
 			throw new UnexpectedBehaviourException(e);
 		}
 	}
 	
 	private void generateTopLevelFigures() throws InterruptedException {
 		for (TreeIterator treeContents = myInputTree.getTreeContents(); treeContents.hasNext();){
 			EObject next = (EObject) treeContents.next();
 			if (next instanceof Figure){
 				visitFigure((Figure)next);
 				treeContents.prune();
 			}
 		}
 	}
 	
 	private void visitFigure(Figure figure) throws InterruptedException {
 		doGenerateJavaClass(myFigureGenerator, getPackageName(), figure.getName(), figure);
 	}
 
 	private String getPackageName(){
 		return myConfig.getMainPackageName();
 	}
 
 	private static class FigureGeneratorAdapter implements GeneratorBase.Emitter {
 		private final FigureGenerator myDelegate;
 
 		public FigureGeneratorAdapter(FigureGenerator delegate){
 			myDelegate = delegate;
 		}
 		
 		public String generate(IProgressMonitor monitor, Object param) throws JETException {
 			if (false == param instanceof Figure){
 				throw new IllegalStateException("Figure expected: " + param);
 			}
 			return myDelegate.go((Figure)param);
 		}
 	}
 	
 }
