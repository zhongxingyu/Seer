 package textmarker.add;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 
 public class AddMarkers {
 
 	public static void addMarkerToFile(String patchName, String fileName, int lineNum, IProject proj, String code, boolean applied, boolean lineAdded, int patchLine, IEditorPart part) {
 		
 		if(part == null)
 			return;
 		
 		// allow for default context numbers when not applied
 		if (!applied)
 			lineNum = lineNum + 3;
 	
 		IPath path = new Path(fileName);
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
 		
 		IFile editorFile = ((FileEditorInput) part.getEditorInput()).getFile();
 		if(!file.equals(editorFile)){
 			return;
 		}
 		
 		try {
 			IMarker marker = null;
 			boolean conflict = false;
 
 			if (!conflict) {
 
 				if (!applied) {
 					marker = file.createMarker("patchAppliesMarker");
 					marker.setAttribute(IMarker.MESSAGE, patchName + " patch will apply here. Right click to see changes.");
 					marker.setAttribute("description", code);
 				} else {
 					if (lineAdded) {
 						marker = file.createMarker("patchLinesMarker");
 						marker.setAttribute("description", code);
 						marker.setAttribute(IMarker.MESSAGE, patchName + " patch has applied here. Line added.");
 						int lineStart = getCharStart(lineNum - 1, part);
 						marker.setAttribute(IMarker.LINE_NUMBER, lineNum -1);
 						marker.setAttribute(IMarker.CHAR_START, lineStart);
 						marker.setAttribute(IMarker.CHAR_END, lineStart + getLineLength(lineNum - 1, part));
 					} else {
 						/*
 						 * Should never be called now, replaced with next function
 						 * 
 						marker = file.createMarker("patchLinesRemovedMarker");
 						marker.setAttribute(IMarker.MESSAGE, patchName + " patch has applied here. Line removed.");
 						marker.setAttribute(IMarker.CHAR_START, getCharStart(lineNum - 1, javaFile));
 						marker.setAttribute(IMarker.CHAR_END, getCharStart(lineNum, javaFile));
 						*/
 					}
 				}
 			}
 
 			marker.setAttribute("name", patchName);
 			marker.setAttribute("project", proj);
 			marker.setAttribute("patched", applied);
 			marker.setAttribute("patchLine", patchLine);
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
 			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void addRemovedMarkerToFile(String patchName, String fileName, int lineNum, IProject proj, String code, boolean lineAdded, int patchLine, String fileContents, IEditorPart part) {
 
 		IPath path = new Path(fileName);
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
 		if(file.getName().startsWith(".")){
 			//hidden file
 			return;
 		}
 		try {
 			IMarker[] markers = file.findMarkers("patchLinesMarker", false, 0);
 			IMarker marker = null;
 			marker = file.createMarker("patchLinesRemovedMarker");
 			marker.setAttribute(IMarker.MESSAGE, patchName + " patch has applied here. Line removed.");
 			int lineStart = getCharStart(lineNum - 1, part);
 			marker.setAttribute(IMarker.CHAR_START, lineStart);
 			marker.setAttribute(IMarker.CHAR_END, lineStart + getLineLength(lineNum - 1, part));
 			marker.setAttribute("name", patchName);
 			marker.setAttribute("project", proj);
 			marker.setAttribute("patched", true);
 			marker.setAttribute("patchLine", patchLine);
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
 			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
 
 			// update lines by 1 after this line
 			for (int i = 0; i < markers.length; i++) {
 				IMarker curr = markers[i];
 
 				if (curr.getAttribute(IMarker.LINE_NUMBER, 0) >= lineNum) {
 					int prevCharStart = (Integer) curr.getAttribute(IMarker.CHAR_START);
 					int prevCharEnd = (Integer) curr.getAttribute(IMarker.CHAR_END);
 
 					marker = file.createMarker("patchLinesMarker");
 					marker.setAttribute(IMarker.MESSAGE, patchName + " patch has applied here. Line added.");
 					marker.setAttribute(IMarker.LINE_NUMBER, (Integer) curr.getAttribute(IMarker.LINE_NUMBER) + 1);
 					marker.setAttribute(IMarker.CHAR_START, prevCharStart + code.length());
 					marker.setAttribute(IMarker.CHAR_END, prevCharEnd + code.length() + 1);
 					marker.setAttribute("name", patchName);
 					marker.setAttribute("project", proj);
 					marker.setAttribute("patched", true);
 					marker.setAttribute("patchLine", patchLine);
 					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
 
 					// had to create a new one and delete the old to update the
 					// line number
 					curr.delete();
 				}
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void clearMarkers(IFile iFile, String ownerName, IProject proj) {		
 		if(iFile.getName().startsWith(".")){
 			//hidden file
 			return;
 		}
 		
 		try {
 			iFile.deleteMarkers("patchAppliesMarker", true, IResource.DEPTH_ZERO);
 			iFile.deleteMarkers("patchLinesMarker", true, IResource.DEPTH_ZERO);
 			iFile.deleteMarkers("patchLinesRemovedMarker", true, IResource.DEPTH_ZERO);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static int getCharStart(int lineNum, IEditorPart part) {
 		
 		IDocument doc = null;
 		
 		if(part instanceof ITextEditor){
 			ITextEditor teditor = (ITextEditor) part;
 			IDocumentProvider dp = teditor.getDocumentProvider();
 			doc = dp.getDocument(teditor.getEditorInput());
 		} else if(part instanceof FormEditor) {
 			FormEditor feditor = (FormEditor) part;
 			IEditorPart curr = (IEditorPart) feditor.getActiveEditor();
 			
 			if(curr instanceof StructuredTextEditor){
 				StructuredTextEditor teditor = (StructuredTextEditor) curr;
 				IDocumentProvider dp = teditor.getDocumentProvider();
 				doc = dp.getDocument(teditor.getEditorInput());
 			}
 		}
 		
 		if(doc != null){		
 			try {
 				return doc.getLineOffset(lineNum);
 			} catch (BadLocationException e) {
				//e.printStackTrace();
 			}
 		}
 		return -1;
 	}
 	
 	public static int getLineLength(int lineNum, IEditorPart part) {
 		
 		IDocument doc = null;
 		
 		if(part instanceof ITextEditor){
 			ITextEditor teditor = (ITextEditor) part;
 			IDocumentProvider dp = teditor.getDocumentProvider();
 			doc = dp.getDocument(teditor.getEditorInput());
 			
 		} else if(part instanceof FormEditor) {
 			FormEditor feditor = (FormEditor) part;
 			IEditorPart curr = (IEditorPart) feditor.getActiveEditor();
 			
 			if(curr instanceof StructuredTextEditor){
 				StructuredTextEditor teditor = (StructuredTextEditor) curr;
 				IDocumentProvider dp = teditor.getDocumentProvider();
 				doc = dp.getDocument(teditor.getEditorInput());
 			}
 		}
 		
 		if(doc != null){		
 			try {
 				return doc.getLineLength(lineNum);
 			} catch (BadLocationException e) {
				//e.printStackTrace();
 			}
 		}
 		
 		return -1;
 	}
 }
