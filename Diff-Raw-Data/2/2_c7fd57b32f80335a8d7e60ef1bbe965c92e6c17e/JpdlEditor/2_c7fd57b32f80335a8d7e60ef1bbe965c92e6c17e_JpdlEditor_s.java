 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jbpm.gd.jpdl.editor;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.FileEditorInput;
 import org.jbpm.gd.common.editor.ContentProvider;
 import org.jbpm.gd.common.editor.Editor;
 import org.jbpm.gd.common.editor.GraphicalViewer;
 import org.jbpm.gd.common.editor.OutlineViewer;
 import org.jbpm.gd.common.editor.SelectionSynchronizer;
 import org.jbpm.gd.common.model.SemanticElement;
 import org.jbpm.gd.jpdl.deployment.DeploymentInfo;
 import org.jbpm.gd.jpdl.model.ProcessDefinition;
 import org.jbpm.gd.jpdl.part.JpdlEditorOutlineEditPartFactory;
 import org.jbpm.gd.jpdl.part.JpdlGraphicalEditPartFactory;
 
 public class JpdlEditor extends Editor { 
 
 	private JpdlDeploymenEditorPage deploymentInfoEditorPage;
 	private DeploymentInfo deploymentInfo;
 	
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		super.init(site, input);
 		initPartName();
 	}
 	
 	private void initPartName() {
 		FileEditorInput fileInput = (FileEditorInput) getEditorInput();
 		String fileName = fileInput.getFile().getName();
 		String processName = fileName;
 		if ("processdefinition.xml".equals(fileName)) {
 			IPath path = fileInput.getPath().removeLastSegments(1);
 			path = path.removeFirstSegments(path.segmentCount() - 1);
 			processName = path.lastSegment();
 		} else if (fileName.endsWith(".jpdl.xml")){
 			int index = fileName.indexOf(".jpdl.xml");
 			processName = fileName.substring(0, index);
 		}
 		setPartName(processName);
 	}
 
 	protected SelectionSynchronizer createSelectionSynchronizer() {
 		return new JpdlSelectionSynchronizer();
 	}
 
 	protected ContentProvider createContentProvider() {
 		return new JpdlContentProvider(this);
 	}
 
 	protected GraphicalViewer createGraphicalViewer() {
 		return new GraphicalViewer(this) {
 			protected void initEditPartFactory() {
 				setEditPartFactory(new JpdlGraphicalEditPartFactory());
 			}			
 		};
 	}
 
 	protected OutlineViewer createOutlineViewer() {
 		return new OutlineViewer(this) {
 			protected void initEditPartFactory() {
 				getViewer().setEditPartFactory(new JpdlEditorOutlineEditPartFactory()); 
 			}
 		};
 	}
 
 	protected void createPages() {
 		super.createPages();
 		initDeploymentInfoPage();
 	}
 	
 	protected void initDeploymentInfoPage() {
 		((JpdlContentProvider)getContentProvider()).initializeDeploymentInfo(getDeploymentInfo(), getEditorInput());
 		deploymentInfoEditorPage = new JpdlDeploymenEditorPage(this);
		addPage(1, deploymentInfoEditorPage, "DeploymentInfo");
 	}
 
 	protected SemanticElement createMainElement() {
 		return getSemanticElementFactory().createById("org.jbpm.gd.jpdl.processDefinition");
 	}
 
 	public ProcessDefinition getProcessDefinition() {
 		return (ProcessDefinition)getRootContainer().getSemanticElement();
 	}
 	
 	public DeploymentInfo getDeploymentInfo() {
 		if (deploymentInfo == null) {
 			deploymentInfo = new DeploymentInfo();
 		}
 		return deploymentInfo;
 	}
 	
 	@SuppressWarnings("restriction")
 	public boolean isSaveOnCloseNeeded() {
 		return isDirty() || super.isSaveOnCloseNeeded();
 	}
 
 }
