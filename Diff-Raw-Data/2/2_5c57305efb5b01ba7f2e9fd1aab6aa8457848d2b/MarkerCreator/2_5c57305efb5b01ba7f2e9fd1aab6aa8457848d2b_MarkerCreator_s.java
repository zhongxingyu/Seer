 package org.eclipse.xtext.todo;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.nodemodel.BidiIterator;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.INode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.todo.ui.TodoTaskInputDialog;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 public class MarkerCreator implements IUnitOfWork<Void, XtextResource> {
 	private static final String TASK_MARKER_TYPE = "";
 	private final IResource varResource;
 	private final ITaskElementChecker objElementChecker;
 	private final IProgressMonitor argMonitor;
 
 	MarkerCreator(final IResource varResource,
 			final ITaskElementChecker objElementChecker,
 			final IProgressMonitor argMonitor) {
 		this.varResource = varResource;
 		this.objElementChecker = objElementChecker;
 		this.argMonitor = argMonitor;
 	}
 
 	public java.lang.Void exec(XtextResource argState) {
 		if (argState != null && !argState.getContents().isEmpty()) {
 			EObject varModel = argState.getContents().get(0);
 			ICompositeNode varRoot = NodeModelUtils.getNode(varModel);
 			try {
 				visit(varRoot, varResource, argMonitor);
 			} catch (CoreException e) {
 				Activator
 						.getDefault()
 						.getLog()
 						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 								"Could not create marker", e));
 			}
 		}
 		return null;
 	}
 
 	private void visit(ICompositeNode node, final IResource varResource,
 			final IProgressMonitor argMonitor) throws CoreException {
 		BidiIterator<INode> varAllContents = node.getChildren().iterator();
 		while (varAllContents.hasNext() && !argMonitor.isCanceled()) {
 			INode varNext = varAllContents.next();
 			internalCreateMarker(varNext, varResource);
 			if (varNext instanceof ICompositeNode) {
 				visit((ICompositeNode) varNext, varResource, argMonitor);
 			}
 		}
 	}
 
 	private void internalCreateMarker(INode argNode, IResource argRresource)
 			throws CoreException {
 		String varIgnorePrefix = objElementChecker.getPrefixToIgnore(argNode);
 		if (varIgnorePrefix != null) {
 			IMarker varMarker = argRresource.createMarker(getMarkerType());
 			String text = argNode.getText();
 			// match from including varIgnorePrefix until end of line
 			Matcher matcher = Pattern.compile(
					"(?s).*(" + varIgnorePrefix + ".*?)(\\r)?\\n.*").matcher(
 					text);
 			if (matcher.matches()) {
 				text = matcher.group(1);
 			}
 			varMarker.setAttribute(IMarker.MESSAGE, text.trim());
 			varMarker.setAttribute(IMarker.LOCATION,
 					"line " + argNode.getStartLine());
 			varMarker.setAttribute(IMarker.CHAR_START, argNode.getOffset());
 			varMarker.setAttribute(IMarker.CHAR_END, argNode.getOffset()
 					+ argNode.getLength());
 			varMarker.setAttribute(IMarker.USER_EDITABLE, false);
 			String tagsStr = Activator.getDefault().getPreferenceStore()
 					.getString(TodoTaskInputDialog.COMPILER_TASK_TAGS);
 			int priority = IMarker.PRIORITY_NORMAL;
 			if (tagsStr != null) {
 				List<String> tags = Arrays.asList(tagsStr.split(","));
 				int index = tags.indexOf(varIgnorePrefix);
 				if (index >= 0) {
 					String prioritiesStr = Activator
 							.getDefault()
 							.getPreferenceStore()
 							.getString(
 									TodoTaskInputDialog.COMPILER_TASK_PRIORITIES);
 					if (prioritiesStr != null) {
 						List<String> priorities = Arrays.asList(prioritiesStr
 								.split(","));
 						if (priorities.size() > index) {
 							String priorityStr = priorities.get(index);
 							if(TodoTaskInputDialog.COMPILER_TASK_PRIORITY_HIGH.equals(priorityStr)) {
 								priority = IMarker.PRIORITY_HIGH;
 							} else if(TodoTaskInputDialog.COMPILER_TASK_PRIORITY_NORMAL.equals(priorityStr)) {
 								priority = IMarker.PRIORITY_NORMAL;
 							} else if(TodoTaskInputDialog.COMPILER_TASK_PRIORITY_LOW.equals(priorityStr)) {
 								priority = IMarker.PRIORITY_LOW;
 							} else {
 								priority = IMarker.PRIORITY_NORMAL;
 							}
 						}
 					}
 				}
 			}
 
 			varMarker.setAttribute(IMarker.PRIORITY, priority);
 		}
 	}
 
 	public static String getMarkerType() {
 		if (StringUtils.isEmpty(TASK_MARKER_TYPE)) {
 			String foundMarkerType = "";
 			IConfigurationElement[] config = Platform.getExtensionRegistry()
 					.getConfigurationElementsFor(
 							"org.eclipse.ui.editors.annotationTypes");
 			String markerType = null;
 			for (IConfigurationElement e : config) {
 				if (e != null) {
 					markerType = e.getAttribute("markerType");
 					if (markerType != null
 							&& markerType
 									.endsWith(TaskConstants.XTEXT_MARKER_SIMPLE_NAME)) {
 						foundMarkerType = markerType;
 						break;
 					}
 				}
 			}
 			return foundMarkerType;
 		} else {
 			return TASK_MARKER_TYPE;
 		}
 	}
 }
