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
 package org.eclipse.bpmn2.modeler.core.runtime;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
 import org.eclipse.ui.views.properties.tabbed.TabContents;
 
 public class Bpmn2TabDescriptor extends AbstractTabDescriptor {
 
 	protected String id;
 	protected String category;
 	protected String label;
 	protected String afterTab = null;
 	protected String replaceTab = null;
 	protected boolean indented = false;
 	protected Image image = null;
 	protected boolean popup = true;
 
 	public Bpmn2TabDescriptor(IConfigurationElement e) {
 		id = e.getAttribute("id"); //$NON-NLS-1$
 		category = e.getAttribute("category"); //$NON-NLS-1$
 		if (category==null || category.isEmpty())
 			category = "BPMN2"; //$NON-NLS-1$
 		label = e.getAttribute("label"); //$NON-NLS-1$
 		afterTab = e.getAttribute("afterTab"); //$NON-NLS-1$
 		replaceTab = e.getAttribute("replaceTab"); //$NON-NLS-1$
 		String s = e.getAttribute("indented"); //$NON-NLS-1$
 		indented = s!=null && s.trim().equalsIgnoreCase("true"); //$NON-NLS-1$
 		s = e.getAttribute("popup"); //$NON-NLS-1$
 		if (s!=null && s.trim().equalsIgnoreCase("false")) //$NON-NLS-1$
 			popup = false;
 	}
 	
 	public Bpmn2TabDescriptor(String id, String category, String label) {
 		this.id = id;
 		if (category==null || category.isEmpty() )
 			category = "BPMN2"; //$NON-NLS-1$
 		this.category = category;
 		this.label = label;
 	}
 	
 	@Override
 	public String getCategory() {
 		return category;
 	}
 
 	@Override
 	public String getId() {
 		return id;
 	}
 
 	@Override
 	public String getLabel() {
 		return label;
 	}
 	
 	public boolean isPopup() {
 		return popup;
 	}
 
 	public void setPopup(boolean popup) {
 		this.popup = popup;
 	}
 
 	@Override
 	public String getAfterTab() {
 		if (afterTab==null || afterTab.trim().length()==0)
 			return super.getAfterTab();
 		return afterTab;
 	}
 
 	@Override
 	public Image getImage() {
 		if (image==null)
 			return super.getImage();
 		return image;
 	}
 
 	@Override
 	public TabContents createTab() {
 		// TODO Auto-generated method stub
 		return super.createTab();
 	}
 
 	@Override
 	public boolean isSelected() {
 		// TODO Auto-generated method stub
 		return super.isSelected();
 	}
 
 	@Override
 	public void setSectionDescriptors(List sectionDescriptors) {
 		// TODO Auto-generated method stub
 		super.setSectionDescriptors(sectionDescriptors);
 	}
 
 	@Override
 	public boolean isIndented() {
 		return indented;
 	}
 
 	@Override
 	public Object clone() {
 		Bpmn2TabDescriptor td = new Bpmn2TabDescriptor(id, category, label);
 		td.afterTab = this.afterTab;
 		td.replaceTab = this.replaceTab;
 		if (image!=null)
 			td.image = new Image(Display.getDefault(), this.image, SWT.IMAGE_COPY);
 		td.indented = this.indented;
 //		for (Bpmn2SectionDescriptor sd : (List<Bpmn2SectionDescriptor>)getSectionDescriptors()) {
 //			clone.getSectionDescriptors().add( new Bpmn2SectionDescriptor(sd) );
 //		}
 		return td;
 	}
 
 	public Bpmn2TabDescriptor copy() {
 		Bpmn2TabDescriptor td = new Bpmn2TabDescriptor(id, category, label);
 		td.id += td.hashCode();
 		td.afterTab = this.afterTab;
 		td.replaceTab = this.replaceTab;
 		if (image!=null)
 			td.image = new Image(Display.getDefault(), this.image, SWT.IMAGE_COPY);
 		td.indented = this.indented;
 		for (Bpmn2SectionDescriptor sd : (List<Bpmn2SectionDescriptor>)getSectionDescriptors()) {
 			td.getSectionDescriptors().add(new Bpmn2SectionDescriptor(td, sd));
 		}
 		return td;
 	}
 
 	public String getReplaceTab() {
 		if (replaceTab==null || replaceTab.trim().length()==0)
 			return null;
 		return replaceTab;
 	}
 	
 	public boolean isReplacementForTab(String id) {
 		String replacements = getReplaceTab();
 		if (replacements!=null) {
 			String[] rep = replacements.split(" "); //$NON-NLS-1$
 			for (String r : rep) {
 				if (r.equals(id))
 					return true;
 			}
 		}
 		return false;
 	}
 }
