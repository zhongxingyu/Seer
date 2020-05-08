 package textmarker.add;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.LabelProviderChangedEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IDecoratorManager;
 import org.eclipse.ui.PlatformUI;
 
 import textmarker.parse.ParseXMLForMarkers;
 
 public class AddMarkers {
 
 	public static void addMarkerToFile(String patchName, String fileName, int lineNum, IProject proj, String code, boolean applied, boolean lineAdded, int patchLine) {
 
 		// allow for default context numbers
		//TODO JB: fix for newly created files, or all deleted files
 		if (!applied)
 			lineNum = lineNum + 3;
 	
 		int index = fileName.indexOf(proj.getName());
 		IPath path = new Path(fileName);
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
 		File javaFile = new File(fileName);
 		
 		try {
 			IMarker marker = null;
 			boolean conflict = false;
 
 			// deprecated: look for conflicts with other markers (e.g. AspectJ)
 			/*
 			 * IMarker[] markers = iFile.findMarkers(null, true,
 			 * IResource.DEPTH_ZERO);
 			 * 
 			 * for (int i = markers.length - 1; i >= 0; i--) { IMarker
 			 * markerAtIndex = markers[i]; try { int lineOfMarker = ((Integer)
 			 * markerAtIndex.getAttribute(IMarker.LINE_NUMBER)).intValue();
 			 * 
 			 * 
 			 * if (lineOfMarker == lineNum) { marker =
 			 * iFile.createMarker(IMarker.PROBLEM);
 			 * marker.setAttribute(IMarker.MESSAGE, patchName +
 			 * " patch and possibly an aspect apply here!"); conflict = true; }
 			 * } catch (NullPointerException npe) {
 			 * 
 			 * } }
 			 */
 
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
 						int lineStart = getCharStart(lineNum - 1, javaFile);
 						marker.setAttribute(IMarker.LINE_NUMBER, lineNum -1);
 						marker.setAttribute(IMarker.CHAR_START, lineStart);
 						marker.setAttribute(IMarker.CHAR_END, lineStart + code.length());
 					} else {
 						marker = file.createMarker("patchLinesRemovedMarker");
 						marker.setAttribute(IMarker.MESSAGE, patchName + " patch has applied here. Line removed.");
 						marker.setAttribute(IMarker.CHAR_START, getCharStart(lineNum - 1, javaFile));
 						marker.setAttribute(IMarker.CHAR_END, getCharStart(lineNum, javaFile));
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
 
 	public static void addRemovedMarkerToFile(String patchName, String fileName, int lineNum, IProject proj, String code, boolean lineAdded, int patchLine, String fileContents) {
 		// TODO this works the first time the file is opened, but a refresh
 		// after that does strange higlighting things
		int index = fileName.indexOf(proj.getName());
		IPath path = new Path(fileName.substring(index));
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 
 		try {
 			IMarker[] markers = file.findMarkers("patchLinesMarker", false, 0);
 			IMarker marker = null;
 			marker = file.createMarker("patchLinesRemovedMarker");
 			marker.setAttribute(IMarker.MESSAGE, patchName + " patch has applied here. Line removed.");
 			int lineStart = getCharStartFromString(lineNum, fileContents) - 1;
 			marker.setAttribute(IMarker.CHAR_START, lineStart);
 			marker.setAttribute(IMarker.CHAR_END, lineStart + code.length());
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
 
 	public static void clearMarkers(String fileName, String ownerName, IProject proj) {
 
		int index = fileName.indexOf(proj.getName());
		IPath path = new Path(fileName.substring(index));
		IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 
 		try {
 			iFile.deleteMarkers("patchAppliesMarker", true, IResource.DEPTH_ZERO);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		try {
 			iFile.deleteMarkers("patchLinesMarker", true, IResource.DEPTH_ZERO);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		try {
 			iFile.deleteMarkers("patchLinesRemovedMarker", true, IResource.DEPTH_ZERO);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static int getCharStart(int lineNum, File javaFile) {
 		try {
 			BufferedReader read = new BufferedReader(new FileReader(javaFile));
 			int lineNumber = 0;
 			String line = "";
 			int charPos = 0;
 			int carrLength = 1;
 			
 			try {
 				while ((line = read.readLine()) != null) {
 					//The three are "\r", "\n" and "\r\n"
 					if(lineNumber == 0 && line.endsWith("\r\n")){
 						carrLength = 2;
 					}
 					if (lineNum == lineNumber) {
 						break;
 					}
 
 					charPos += line.length() + carrLength;
 					lineNumber++;
 				}
 
 				return charPos;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	public static int getCharStartFromString(int lineNum, String javaFile) {
 
 		BufferedReader read = new BufferedReader(new StringReader(javaFile));
 		int lineNumber = 0;
 		String line = "";
 		int charPos = 0;
 		int carrLength = 1;
 
 		try {
 			while ((line = read.readLine()) != null) {
 				if(lineNumber == 0 && line.endsWith("\r\n")){
 					carrLength = 2;
 				}
 				if (lineNum == lineNumber) {
 					break;
 				}
 
 				charPos += line.length() + carrLength; 
 				lineNumber++;
 			}
 
 			return charPos;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 }
