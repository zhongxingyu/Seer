 package org.spoofax.modelware.gmf;
 
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.modelware.emf.utils.SpoofaxEMFConstants;
 import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
 import org.strategoxt.imp.runtime.EditorState;
 import org.strategoxt.imp.runtime.Environment;
 
 /**
  * TODO: add analyzed AST change listener to Spoofax instead of this hack
  * 
  * @author oskarvanrest
  */
 public class TextChangeListener {
 
 	private EditorPair editorPair;
 	private boolean debounce;
 	private boolean active;
 
 	public TextChangeListener(EditorPair editorPair) {
 		this.editorPair = editorPair;
 		Thread thread = new Thread(new Timer());
 		active = true;
 		thread.start();
 		editorPair.registerObserver(new Debouncer());
 	}
 
 	private class Timer implements Runnable {
 		public void run() {
 			EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
 
 			boolean failedLastTime = false;
 			
 			try {
 				while (active) {
 					IStrategoTerm newASTgraph = SpoofaxEMFUtils.getASTgraph(editorState);
 					
					if (!failedLastTime && newASTgraph == null) {
						// Note: a failing ASTtext-to-ASTgraph transformation does not necessarily mean that the transformation is erroneous, hence a warning.
						// It may be the case that the ASTtext is 'erroneous' when syntax is not constraint by the grammar but by semantic warnings instead.
						Environment.logWarning("Strategy '" + SpoofaxEMFConstants.STRATEGY_ASTtext_TO_ASTgraph + "' failed for input: " + editorState.getCurrentAst());
						failedLastTime = true;
					}
					else {
 						failedLastTime = false;
					}
					
					if (newASTgraph != null && newASTgraph != editorPair.ASTgraph) {
 						editorPair.ASTgraph = newASTgraph;
 						editorPair.notifyObservers(EditorPairEvent.PostAnalyze);
 					}
 					
 					Thread.sleep(25);
 				}
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void dispose() {
 		active = false;
 	}
 
 	private class Debouncer implements EditorPairObserver {
 
 		@Override
 		public void notify(EditorPairEvent event) {
 			
 			if (event != EditorPairEvent.PreDiagram2TextSelection && event != EditorPairEvent.PostDiagram2TextSelection && event != EditorPairEvent.PreText2DiagramSelection) {
 //				System.out.println(event.toString());			
 			}
 			
 			if (event == EditorPairEvent.PostAnalyze) {
 				if (debounce) {
 					editorPair.notifyObservers(EditorPairEvent.PostLayoutPreservation);
 				}
 				
 				editorPair.doTerm2Model();
 			}
 			
 			if (event == EditorPairEvent.PreLayoutPreservation) {
 				debounce = true;
 			}
 			else if (event == EditorPairEvent.PostLayoutPreservation) {
 				debounce = false;
 			}
 
 			if (event == EditorPairEvent.PreUndo) {
 				debounce = true;
 			}
 			else if (event == EditorPairEvent.PreRedo) {
 				debounce = true;
 			}
 		}
 	}
 }
