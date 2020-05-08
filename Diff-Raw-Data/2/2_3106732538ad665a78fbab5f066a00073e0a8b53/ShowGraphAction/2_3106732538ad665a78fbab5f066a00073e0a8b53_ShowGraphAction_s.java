 package org.meta_environment.eclipse.viz.graph;
 
 import java.util.List;
 
 import org.eclipse.imp.pdb.analysis.AnalysisException;
 import org.eclipse.imp.pdb.browser.FactBrowserView;
 import org.eclipse.imp.pdb.facts.db.FactBase;
 import org.eclipse.imp.pdb.facts.db.IFactKey;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 public class ShowGraphAction implements IViewActionDelegate {
 	private FactBrowserView factBrowser;
 	private GraphView graphView;
 
 	public void init(IViewPart view) {
 		if (view instanceof FactBrowserView) {
 		  factBrowser = (FactBrowserView) view;
 		}
 	}
 
 	public void run(IAction action) {
         List<IFactKey> selected = factBrowser.getCurrentSelection();
 		
 		try {
 			if (!selected.isEmpty()) {
 				IFactKey first = selected.get(0);
 
				graphView = (GraphView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.meta_environment.eclipse.viz.graph.view");
 				graphView.showRelation(FactBase.getInstance().getFact(first));
 			}
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		} catch (AnalysisException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void selectionChanged(IAction action, ISelection selection) {
 		
 	}
 
 }
