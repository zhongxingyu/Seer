 package org.eclipse.graphiti.examples.common.navigator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.navigator.ILinkHelper;
 
 /**
  * Provides information to the Common Navigator (Project Explorer) on how to
  * link active editors with selections of diagram nodes.
  * 
  * @since 0.9
  */
 public class EditorLinkHelper implements ILinkHelper {
 
 	/**
 	 * Return a selection that contains the file that the given editor input
 	 * represent. The {@link StructuredSelection} will be compared with nodes in
 	 * the tree. See also extension point
 	 * {@code "org.eclipse.ui.navigator.linkHelper"}.
 	 */
 	public IStructuredSelection findSelection(IEditorInput editorInput) {
 		if (editorInput instanceof DiagramEditorInput) {
 			if (editorInput.exists()) {
 				DiagramEditorInput diagramEditorInput = (DiagramEditorInput) editorInput;
 				final IFile file = getFile(diagramEditorInput.getUri());
 				if (file != null) {
 					return new StructuredSelection(file);
 				}
 			}
 
 		}
 		return StructuredSelection.EMPTY;
 	}
 
 	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
 		// not needed
 	}
 
 	private IFile getFile(URI uri) {
 		if (uri == null) {
 			return null;
 		}
 
 		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 
 		// File URIs
 		final String filePath = getWorkspaceFilePath(uri.trimFragment());
 		if (filePath == null) {
 			final IPath location = Path.fromOSString(uri.toString());
 			final IFile file = workspaceRoot.getFileForLocation(location);
 			if (file != null) {
 				return file;
 			}
 			return null;
 		}
 
 		// Platform resource URIs
 		else {
 			final IResource workspaceResource = workspaceRoot.findMember(filePath);
 			return (IFile) workspaceResource;
 		}
 	}
 
 	private String getWorkspaceFilePath(URI uri) {
 		if (uri.isPlatform()) {
 			return uri.toPlatformString(true);
 		}
 		return null;
 	}
 
 }
