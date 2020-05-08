 package org.eclipse.rmf.reqif10.search.ui;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.SpecificationEditor;
 import org.eclipse.search.ui.ISearchResultViewPart;
 import org.eclipse.search.ui.NewSearchUI;
 import org.eclipse.ui.IEditorActionDelegate;
 import org.eclipse.ui.IEditorPart;
 
 public class ResultNavigator implements IEditorActionDelegate, IExecutableExtension {
 	
 	final static String NEXT_ID = "org.eclipse.rmf.reqif10.search.next";
 	final static String PREV_ID = "org.eclipse.rmf.reqif10.search.prev";
 
 	private Specification spec;
 	private SpecHierarchy selectedSpecHierarchy;
 	private SpecificationEditor editor;
 
 	@Override
 	public void run(IAction action) {
 		editor.setFocus();
 		SpecHierarchy match = null;
 		if (action.getId().equals(NEXT_ID)) {
 			match = next();
 		} else if (action.getId().equals(PREV_ID)) {
 			match = prev();
 		} else {
 			throw new IllegalStateException(action + "");
 		}
 
 		if (match != null) {
 			ISelection selection = new StructuredSelection(match);
 			editor.getSite().getSelectionProvider().setSelection(selection);
 		} else {
 			MessageDialog.openInformation(editor.getSite().getShell(),
 					"No more elements", "No more elements in this direction.");
 		}
 	}
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (! (selection instanceof IStructuredSelection)) {
 			action.setEnabled(false);
 			return;
 		}
 		Object element = ((IStructuredSelection)selection).getFirstElement();
 		if (! (element instanceof SpecHierarchy)) {
 			action.setEnabled(false);
 			return;
 		}
 		action.setEnabled(specInSearchResult());
 		this.selectedSpecHierarchy = (SpecHierarchy) element;
 	}
 
 	@Override
 	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
 		if (targetEditor instanceof SpecificationEditor) {
 			this.editor = (SpecificationEditor)targetEditor;
 			this.spec = ((SpecificationEditor)targetEditor).getSpecification();
 		} else {
 			this.spec = null;
 			this.editor = null;
 		}
 	}
 	
 	private boolean specInSearchResult() {
 		for (SpecHierarchy sh : getMatchingSpecsHierarchies()) {
 			Specification s = getSpecForSpecHierarchy(sh);
 			if (s == spec) return true;			
 		}
 		return false;
 	}
 
 	private Set<SpecHierarchy> getMatchingSpecsHierarchies() {
 		HashSet<SpecHierarchy> set = new HashSet<SpecHierarchy>();
 
 		ISearchResultViewPart view = NewSearchUI.getSearchResultView();
 		if (view != null && view.getActivePage() instanceof ReqIFSearchResultPage) {
 			ReqIFSearchResultPage page = (ReqIFSearchResultPage)view.getActivePage();
 			UsageSearchResult result = page.getSearchResult();
 			Collection<EObject> entries = result.getSearchEntries().get(spec.eResource());
 			if (entries == null) {
 				return set;
 			}
 			for (EObject entry: entries) {
 				if (entry instanceof SpecHierarchy) {
 					set.add((SpecHierarchy) entry);
 				}				
 			}
 		}
 		return set;
 	}
 	
 	private Specification getSpecForSpecHierarchy(SpecHierarchy entry) {
 		while (true) {
 			if (entry.eContainer() instanceof Specification) {
 				return (Specification) entry.eContainer();
 			} else if (entry.eContainer() instanceof SpecHierarchy) {
 				entry = (SpecHierarchy) entry.eContainer();
 			} else {
 				return null;
 			}
 		}
 	}
 
 	@Override
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 		// Not used, but interface implemented to ensure proper class loading.		
 	}
 	
 	private SpecHierarchy next() {
 		Set<SpecHierarchy> bag = getMatchingSpecsHierarchies();
 		SpecHierarchy next = oneDown(selectedSpecHierarchy);
 		while (next != null) {
 			if (bag.contains(next)) return next;
 			next = oneDown(next);
 		}
 		return null;
 	}
 
 	private SpecHierarchy oneDown(SpecHierarchy sh) {
 		// If there are children, go down the hierarchy.
 		if (sh.getChildren().size() > 0) {
 			return sh.getChildren().get(0);
 		}
 		
 		EObject element = sh;
 		while (element instanceof SpecHierarchy) {
 			EObject container = element.eContainer();
			int pos = container.eContents().indexOf(element);		
			if (pos < container.eContents().size() - 1) {
				return (SpecHierarchy) container.eContents().get(pos +1);
 			} else {
 				element = container;
 			}
 		}
 		return null;
 	}
 
 	private SpecHierarchy prev() {
 		Set<SpecHierarchy> bag = getMatchingSpecsHierarchies();
 		SpecHierarchy next = oneUp(selectedSpecHierarchy);
 		while (next != null) {
 			if (bag.contains(next)) return next;
 			next = oneUp(next);
 		}
 		return null;
 	}
 
 	private SpecHierarchy oneUp(SpecHierarchy sh) {
 		EObject element = sh;
 		while (element instanceof SpecHierarchy) {
 			EObject container = element.eContainer();
			int pos = container.eContents().indexOf(element);		
 			if (pos > 0) {
				sh = (SpecHierarchy) container.eContents().get(pos - 1);
 				while (sh.getChildren().size() > 0) {
 					sh = sh.getChildren().get(sh.getChildren().size() - 1);
 				}
 				return sh;
 			} else {
 				// Go one up, then as deep as possible
 				if (container instanceof SpecHierarchy) {
 					return (SpecHierarchy) container;
 				} else {
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 	
 }
