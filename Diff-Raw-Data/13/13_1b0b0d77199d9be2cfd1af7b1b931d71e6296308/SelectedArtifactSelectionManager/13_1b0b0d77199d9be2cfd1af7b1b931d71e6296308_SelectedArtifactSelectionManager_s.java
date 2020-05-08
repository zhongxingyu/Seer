 /* This file is part of SoftwareFm
 /* SoftwareFm is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
 /* SoftwareFm is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
 /* You should have received a copy of the GNU General Public License along with SoftwareFm. If not, see <http://www.gnu.org/licenses/> */
 
 /* This file is part of SoftwareFm
 /* SoftwareFm is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
 /* SoftwareFm is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
 /* You should have received a copy of the GNU General Public License along with SoftwareFm. If not, see <http://www.gnu.org/licenses/> */
 
 package org.softwareFm.explorer.eclipse;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.ITypeRoot;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.NodeFinder;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jdt.ui.SharedASTProvider;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.softwareFm.jdtBinding.api.BindingRipperResult;
 import org.softwareFm.jdtBinding.api.IBindingRipper;
 import org.softwareFm.utilities.collections.Lists;
 import org.softwareFm.utilities.exceptions.WrappedException;
 import org.softwareFm.utilities.maps.Maps;
 
 public class SelectedArtifactSelectionManager implements ISelectedBindingManager, ISelectionListener {
 	private static final Object selectionKey = "selection";
 	private static final Object ripperKey = "ripper";
 
 	private final IBindingRipper bindingRipper;
 
 	private final List<ISelectedBindingListener> listeners = Lists.newList();
 
 	public SelectedArtifactSelectionManager(IBindingRipper bindingRipper) {
 		this.bindingRipper = bindingRipper;
 	}
 
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			BindingRipperResult ripperResult = selectToBindingResult(bindingRipper, textSelection);
			if (ripperResult != null)
				fireListeners(ripperResult);
 		}
 	}
 
 	public static BindingRipperResult reRip(BindingRipperResult result) {
 		if (result == null)
 			return null;
 		Map<String, Object> cargo = result.cargo;
 		TextSelection selection = (TextSelection) cargo.get(selectionKey);
 		IBindingRipper ripper = (IBindingRipper) cargo.get(ripperKey);
 		return selectToBindingResult(ripper, selection);
 	}
 
 	public static BindingRipperResult selectToBindingResult(IBindingRipper ripper, ITextSelection textSelection) {
 		try {
 			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 			if (activePage != null) {
 				IEditorPart editor = activePage.getActiveEditor();
 				ITypeRoot typeRoot = getJavaInput(editor);
 				if (typeRoot instanceof ICompilationUnit) {
 					ICompilationUnit cu = (ICompilationUnit) typeRoot;
 					CompilationUnit root = SharedASTProvider.getAST(cu, SharedASTProvider.WAIT_NO, null);
 					if (root != null) {
 						ASTNode node = NodeFinder.perform(root, textSelection.getOffset(), textSelection.getLength());
 						if (node instanceof Expression) {
 							Expression expression = (Expression) node;
 							BindingRipperResult ripperResult = ripper.rip(expression, Maps.<String, Object> makeMap(//
 									selectionKey, textSelection,//
 									ripperKey, ripper));
 							return ripperResult;
 						}
 					}
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			throw WrappedException.wrap(e);
 		}
 	}
 
 	private void fireListeners(BindingRipperResult ripperResult) {
 		for (ISelectedBindingListener listener : listeners)
 			listener.selectionOccured(ripperResult);
 	}
 
 	static ITypeRoot getJavaInput(IEditorPart part) {
 		if (part != null) {
 			IEditorInput editorInput = part.getEditorInput();
 			if (editorInput != null) {
 				IJavaElement input = JavaUI.getEditorInputJavaElement(editorInput);
 				if (input instanceof ITypeRoot) {
 					return (ITypeRoot) input;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void addSelectedArtifactSelectionListener(ISelectedBindingListener listener) {
 		listeners.add(listener);
 	}
 
 	@Override
 	public void removeSelectedArtifactSelectionListener(ISelectedBindingListener listener) {
 		listeners.remove(listener);
 	}
 
 }
