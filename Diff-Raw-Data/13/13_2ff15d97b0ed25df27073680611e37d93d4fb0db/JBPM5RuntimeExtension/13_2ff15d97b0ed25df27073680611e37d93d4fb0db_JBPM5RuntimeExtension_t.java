 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc.
  *  All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  *
  * @author Bob Brodt
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import org.apache.xerces.parsers.SAXParser;
 import org.apache.xerces.xni.Augmentations;
 import org.apache.xerces.xni.QName;
 import org.apache.xerces.xni.XMLAttributes;
 import org.apache.xerces.xni.XNIException;
 import org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension;
 import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
 import org.eclipse.bpmn2.modeler.core.runtime.CustomTaskDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor.Property;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil.Bpmn2DiagramType;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.features.JbpmCustomTaskFeatureContainer;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WIDException;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WIDHandler;
 import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WorkItemDefinition;
 import org.eclipse.bpmn2.modeler.ui.DefaultBpmn2RuntimeExtension.RootElementParser;
 import org.eclipse.bpmn2.modeler.ui.wizards.FileService;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.internal.GraphitiUIPlugin;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.xml.sax.InputSource;
 
 @SuppressWarnings("restriction")
 public class JBPM5RuntimeExtension implements IBpmn2RuntimeExtension {
 
 	private static final String DROOLS_NAMESPACE = "http://www.jboss.org/drools";
 
 	private List<WorkItemDefinition> workItemDefinitions;
 	
 	/* (non-Javadoc)
 	 * Check if the given input file is a drools-generated (jBPM) process file.
 	 * 
 	 * @see org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension#isContentForRuntime(org.eclipse.core.resources.IFile)
 	 */
 	@Override
 	public boolean isContentForRuntime(IEditorInput input) {
 		InputSource source = new InputSource( FileService.getInputContents(input) );
 		RootElementParser parser = new RootElementParser(DROOLS_NAMESPACE);
 		parser.parse(source);
 		return parser.getResult();
 	}
 
 	public String getTargetNamespace(Bpmn2DiagramType diagramType){
 		return DROOLS_NAMESPACE;
 	}
 	
 	public List<WorkItemDefinition> getWorkItemDefinitions() {
 		if (workItemDefinitions==null)
 			workItemDefinitions = new ArrayList<WorkItemDefinition>();
 		return workItemDefinitions;
 	}
 
 	public WorkItemDefinition getWorkItemDefinition(String taskName) {
 		List<WorkItemDefinition> wids = getWorkItemDefinitions();
 		for (WorkItemDefinition wid : wids) {
 			if (taskName.equals(wid.getName())) {
 				return wid;
 			}
 		}
 		return null;
 	}
 	
 	/** 
 	 * Initialize in this case finds all the *.wid/*.conf files in the project
 	 * and creates CustomTaskDescriptors for each task included
 	 * @see org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension#initialize(DiagramEditor)
 	 */
 	public void initialize(DiagramEditor editor) {
 		ISelection sel = editor.getEditorSite().getWorkbenchWindow().getSelectionService().getSelection();
 		if (sel instanceof IStructuredSelection) {
 			Object o = ((IStructuredSelection)sel).getFirstElement();
 			// TODO: if selection came from a Guvnor Repository view, this will be a
 			// org.guvnor.tools.views.model.TreeObject - figure out how to add this dependency.
 			// In this case we may want to explicitly make the editor read-only
 			System.out.println(o);
 		}
 
 		IProject project = Bpmn2Preferences.getActiveProject();
 		if (project != null) {
 			getWorkItemDefinitions();
 			workItemDefinitions.clear();
 			try {
 				final WIDResourceVisitor visitor = new WIDResourceVisitor();
 				project.accept(visitor, IResource.DEPTH_INFINITE, false);
 				if (visitor.getWIDFiles().size() > 0) {
 					Iterator<IFile> fileIter = visitor.getWIDFiles().iterator();
 					while (fileIter.hasNext()) {
 						IFile file = fileIter.next();
 						HashMap<String, WorkItemDefinition> widMap = 
 								new LinkedHashMap<String, WorkItemDefinition>();
 						WIDHandler.evaluateWorkDefinitions(widMap, file);
 						workItemDefinitions.addAll(widMap.values());
 					}
 				}
 				if (!workItemDefinitions.isEmpty()) {
 					List<CustomTaskDescriptor> removed = new ArrayList<CustomTaskDescriptor>();
 					for (CustomTaskDescriptor d : TargetRuntime.getCurrentRuntime().getCustomTasks()) {
 						if (!d.isPermanent())
 							removed.add(d);
 					}
 					TargetRuntime.getCurrentRuntime().getCustomTasks().removeAll(removed);
 				
 					java.util.Iterator<WorkItemDefinition> widIterator = workItemDefinitions.iterator();
 					while(widIterator.hasNext()) {
 						final WorkItemDefinition wid = widIterator.next();
 						final CustomTaskDescriptor ctd = convertWIDtoCT(wid);
 						if (ctd != null) {
 							if (TargetRuntime.getCurrentRuntime().customTaskExists(ctd.getId())) {
 								Display.getDefault().asyncExec( new Runnable() {
 									@Override
 									public void run() {
 										MessageDialog.openError(Display.getDefault().getActiveShell(),
 												"Duplicate Custom Task",
 												"A Custom Task with id '"+
 												ctd.getId()+
 												"' was already defined.\n"+
 												"The new Custom Task defined in the file: "+
 												wid.getDefinitionFile().getFullPath().toString()+"\n"+
 												"will be ignored.");
 									}
 								});
 							}
 							else
 								TargetRuntime.getCurrentRuntime().addCustomTask(ctd);
 						}
 					}
 				}
 			} catch (CoreException e) {
 				e.printStackTrace();
 			} catch (WIDException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/*
 	 * Convert a WID to a CustomTaskDescriptor
 	 * @param wid
 	 * @return
 	 */
 	private CustomTaskDescriptor convertWIDtoCT ( WorkItemDefinition wid ) {
 		if (wid != null) {
 			String id = wid.getName();
 			String name = wid.getName();
 			CustomTaskDescriptor ct = new CustomTaskDescriptor(id,name);
 			ct.setType("Task");
 			ct.setDescription(wid.getName());
 			ct.setFeatureContainer(new JbpmCustomTaskFeatureContainer());
 			ct.getFeatureContainer().setCustomTaskDescriptor(ct);
 			ct.getFeatureContainer().setId(id);
 			
 			// process basic properties here
 			setBasicProps ( ct, wid);
 			
 			// push the icon into the image registry
 			IProject project = Bpmn2Preferences.getActiveProject();
 			String iconPath = getWIDPropertyValue("icon", wid);
 			if (iconPath != null) {
 				Path tempPath = new Path(iconPath);
 				String iconName = tempPath.lastSegment();
 				IconResourceVisitor visitor = new IconResourceVisitor(iconName);
 				try {
 					project.accept(visitor, IResource.DEPTH_INFINITE, false);
 					if (visitor.getIconResources() != null && visitor.getIconResources().size() > 0) {
 						ArrayList<IResource> icons = visitor.getIconResources();
 						IResource icon = icons.get(0);
 						URL url = icon.getLocationURI().toURL();
 						ImageDescriptor image = ImageDescriptor.createFromURL(url);
 	
 						ImageRegistry imageRegistry = GraphitiUIPlugin.getDefault().getImageRegistry();
 						if (imageRegistry.get(iconPath) == null)
 							imageRegistry.put(iconPath, image);
 					}
 				} catch (CoreException e1) {
 					e1.printStackTrace();
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			// process xml properties here - i.e. task variables
 			Property ioSpecification = createIOSpecificationSection(ct, wid);
 			createDataAssociations(ioSpecification, ct);
 			
 			return ct;
 		}
 		return null;
 	}
 	
 	/*
 	 * Process the high-level props
 	 * @param propName
 	 * @param wid
 	 * @return
 	 */
 	private String getWIDPropertyValue ( String propName, WorkItemDefinition wid) {
 		if (propName.equalsIgnoreCase("taskname")) {
 			return wid.getName();
 		}
 		if (propName.equalsIgnoreCase("displayName")) {
 			return wid.getDisplayName();
 		}
 		if (propName.equalsIgnoreCase("icon")) {
 			return wid.getIcon();
 		}
 		if (propName.equalsIgnoreCase("customEditor")) {
 			return wid.getCustomEditor();
 		}
 		if (propName.equalsIgnoreCase("eclipse:customEditor")) {
 			return wid.getEclipseCustomEditor();
 		}
 		return null;
 	}
 	
 	/*
 	 * Get the high-level prop from the WID
 	 * @param propName
 	 * @param wid
 	 * @return
 	 */
 	private Property getPropertyFromWID ( String propName, WorkItemDefinition wid ) {
 		String name = propName;
 		String value = getWIDPropertyValue(propName, wid);
 		String description = null;
 		String type = "EString";
 		Property prop = new Property(name, description);
 		prop.type = type;
 		if (value == null && propName.equalsIgnoreCase("icon")) {
 			value = "task.png";
 		}
 		if (value!=null)
 			prop.getValues().add(value);
 		return prop;
 	}
 	
 	/*
 	 * Create the input and output data associations
 	 * @param ioSpecification
 	 * @param ct
 	 */
 	private void createDataAssociations ( Property ioSpecification, CustomTaskDescriptor ct) {
 		Object[] values = ioSpecification.getValues().toArray();
 		int inputCounter = -1;
 //		int outputCounter = -1;
 		for (int i = 0; i < values.length; i++) {
 			if (values[i] instanceof Property) {
 				Property prop = (Property) values[i];
 				if (prop.name.equals("dataInputs")) {
 					inputCounter++;
 					Property dataInputAssociations = new Property ( "dataInputAssociations", null);
 					Property targetRef = new Property ("targetRef", null);
 					targetRef.ref = "ioSpecification/dataInputs#" + inputCounter;
 					dataInputAssociations.getValues().add(targetRef);
 					ct.getProperties().add(dataInputAssociations);
 				}
 //				} else 	if (prop.name.equals("dataOutputs")) {
 //					outputCounter++;
 //					Property dataOutputAssociations = new Property ( "dataOutputAssociations", null);
 //					Property sourceRef = new Property ("sourceRef", null);
 //					sourceRef.ref = "ioSpecification/dataOutputs#" + outputCounter;
 //					dataOutputAssociations.getValues().add(sourceRef);
 ////					Property targetRef = new Property ("targetRef", null);
 ////					dataOutputAssociations.getValues().add(targetRef);
 //					ct.getProperties().add(dataOutputAssociations);
 //				}
 
 			}
 		}
 	}
 	
 	/*
 	 * Handle creating the ioSpecification from the WID/CT
 	 * @param ct
 	 * @param wid
 	 */
 	private Property createIOSpecificationSection ( CustomTaskDescriptor ct, WorkItemDefinition wid ) {
 		Property ioSpecification = new Property ( "ioSpecification", null);
 		
 		for (Entry<String, String> entry : wid.getParameters().entrySet()) {
 			Property dataInputs = new Property("dataInputs", null);
 			Property dataInputsName = new Property("name", null);
 			dataInputsName.getValues().add(entry.getKey());
 			dataInputs.getValues().add(dataInputsName);
 			ioSpecification.getValues().add(dataInputs);
 		}
 
 		// this code if enabled will create a default output variable
 		
 //		if (wid.getResults().isEmpty()) {
 //			Property dataOutputs = new Property("dataOutputs", null);
 //			Property dataOutputsName = new Property("name", null);
 //			dataOutputsName.getValues().add("result");
 //			dataOutputs.getValues().add(dataOutputsName);
 //			ioSpecification.getValues().add(dataOutputs);
 //		} else {
 			for (Entry<String, String> entry : wid.getResults().entrySet()) {
 				Property dataOutputs = new Property("dataOutputs", null);
 				Property dataOutputsName = new Property("name", null);
 				dataOutputsName.getValues().add(entry.getKey());
 				dataOutputs.getValues().add(dataOutputsName);
 				ioSpecification.getValues().add(dataOutputs);
 			}
 //		}
 
 		Object[] values = ioSpecification.getValues().toArray();
 		int inputCounter = -1;
 		int outputCounter = -1;
 		Property inputSets = new Property("inputSets", null);
 		Property outputSets = new Property("outputSets", null);
 		for (int i = 0; i < values.length; i++) {
 			if (values[i] instanceof Property) {
 				Property prop = (Property) values[i];
 				if (prop.name.equals("dataInputs")) {
 					inputCounter++;
 					Property inputSetsRef = new Property ("dataInputRefs", null);
 					inputSetsRef.ref = "ioSpecification/dataInputs#" + inputCounter;
 					inputSets.getValues().add(inputSetsRef);
 				} else 	if (prop.name.equals("dataOutputs")) {
 					outputCounter++;
 					Property outputSetsRef = new Property ("dataOutputRefs", null);
 					outputSetsRef.ref = "ioSpecification/dataOutputs#" + outputCounter;
 					outputSets.getValues().add(outputSetsRef);
 				}
 			}
 		}
 		if (inputSets.getValues().size() > 0) 
 			ioSpecification.getValues().add(inputSets);
 		if (outputSets.getValues().size() > 0) 
 			ioSpecification.getValues().add(outputSets);
 		
 		ct.getProperties().add(ioSpecification);
 		return ioSpecification;
 	}
 	
 	/*
 	 * Handle the top-level props
 	 * @param ct
 	 * @param wid
 	 */
 	private void setBasicProps ( CustomTaskDescriptor ct, WorkItemDefinition wid) {
 		String[] basicProps = new String[] { "taskName", "displayName", "icon" };
 		for (int i = 0; i < basicProps.length; i++) {
 			Property prop = getPropertyFromWID(basicProps[i], wid);
 			ct.getProperties().add(prop);
 		}
 	}
 
 	/*
 	 * Class: Visits each file in the project to see if it's a *.conf/*.wid
 	 * @author bfitzpat
 	 *
 	 */
 	private class WIDResourceVisitor implements IResourceVisitor {
 		
 		private ArrayList<IFile> widFiles = new ArrayList<IFile>();
 		
 		public boolean visit (IResource resource) throws CoreException {
 			if (resource.getType() == IResource.FILE) {
 				if ("conf".equalsIgnoreCase(((IFile)resource).getFileExtension()) ||
 						"wid".equalsIgnoreCase(((IFile)resource).getFileExtension())) {
 					widFiles.add((IFile)resource);
 					return true;
 				}
 			}
			else if (resource.getType() == IResource.FOLDER) {
				// skip over "bin" and "target" folders
				String name = resource.getName();
				if ("bin".equals(name) || "target".equals(name))
					return false;
			}
 			return true;
 		}
 		
 		public ArrayList<IFile> getWIDFiles() {
 			return widFiles;
 		}
 	}
 	
 	/*
 	 * Class: Visits each file in the project looking for the icon
 	 * @author bfitzpat
 	 *
 	 */
 	private class IconResourceVisitor implements IResourceVisitor {
 		
 		private ArrayList<IResource> iconResources = new ArrayList<IResource>();
 		private String iconName;
 		
 		public IconResourceVisitor ( String iconName ) {
 			this.iconName = iconName;
 		}
 		
 		public boolean visit (IResource resource) throws CoreException {
 			if (resource.getType() == IResource.FILE) {
 				if (((IFile)resource).getName().equalsIgnoreCase(iconName)) {
 					iconResources.add(resource);
 					return true;
 				}
 			}
 			return true;
 		}
 		
 		public ArrayList<IResource> getIconResources() {
 			return iconResources;
 		}
 	}
 
 	@Override
 	public Composite getPreferencesComposite(Composite parent, Bpmn2Preferences preferences) {
 		return null;
 	}
 
 }
