 package org.eclipse.dltk.internal.ui.editor.semantic.highlighting;
 
 import java.util.List;
 
 import org.eclipse.dltk.core.ISourceModule;
 
 /**
  * Install this reconciler on the given editor, presenter and highlightings.
  * 
  * @param editor
  *            the editor
  * @param sourceViewer
  *            the source viewer
  * @param presenter
  *            the semantic highlighting presenter
  * @param semanticHighlightings
  *            the semantic highlightings
  * @param highlightings
  *            the highlightings
  */
 public abstract class PositionUpdater {
 
 	
 	public static class UpdateResult{
 		
 		public final List addedPositions;
 		public final List removedPositions;
 		public UpdateResult(List addedPositions, List removedPositions) {
 			super();
 			this.addedPositions = addedPositions;
 			this.removedPositions = removedPositions;
 		}
 	}
 	public abstract UpdateResult reconcile(ISourceModule ast, SemanticHighlightingPresenter presenter, Highlighting[] highlightings, List currentPositions);
 	
 }
