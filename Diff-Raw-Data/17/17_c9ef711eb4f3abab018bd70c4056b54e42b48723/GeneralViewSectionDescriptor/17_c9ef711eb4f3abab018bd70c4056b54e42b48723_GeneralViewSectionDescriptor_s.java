 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.pageflow.properties;
 
 import java.util.List;
 
import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.PageflowLinkEditPart;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.PageflowElementEditPart;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart.PageflowElementTreeEditPart;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowLink;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.PageflowValidation;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.wst.common.ui.properties.internal.provisional.ISection;
 import org.eclipse.wst.common.ui.properties.internal.provisional.ISectionDescriptor;
 import org.eclipse.wst.common.ui.properties.internal.provisional.ITypeMapper;
 
 /**
  * This class defines the section descriptor for pageflow attributes. Following
  * is the typical function of SectionDescriptor for tabbed property views. 1. In
  * tabbed property view, there are multiple sections, one or more than one
  * sections can be composed of one tab. 2. Each of section has an ID, and their
  * relationship is defined by the function "getAfterSection". 3. Not all
  * sections are enabled always. They are enabled according to current
  * WorkbenchPart and selection, which is defined in function "appliesTo".
  * 
  * @author Xiao-guang Zhang
  */
 public class GeneralViewSectionDescriptor implements ISectionDescriptor {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISectionDescriptor#getId()
 	 */
 	public String getId() {
 		return ITabbedPropertiesConstants.GENERAL_TAB_ID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISectionDescriptor#getFilter()
 	 */
 	public ITypeMapper getFilter() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISectionDescriptor#getInputTypes()
 	 */
 	public List getInputTypes() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISectionDescriptor#getSectionClass()
 	 */
 	public ISection getSectionClass() {
 		return new GeneralViewSection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.xtools.common.ui.properties.ISectionDescriptor#getTargetTab()
 	 */
 	public String getTargetTab() {
 		return ITabbedPropertiesConstants.GENERAL_TAB_ID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISectionDescriptor#appliesTo(IWorkbenchPart, ISelection)
 	 */
 	public boolean appliesTo(IWorkbenchPart part, ISelection selection) {
 		Object object = null;
 		// FIXME: Should move the appliesTo to differnt page of
 		// MultiPageEditorPart.
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection structuredSelection = (StructuredSelection) selection;
 			object = structuredSelection.getFirstElement();
 			// The general tab will be shown for only pageflow element and
 			// pageflow link editparts
 			if (object instanceof PageflowElementEditPart
 					|| object instanceof PageflowElementTreeEditPart) {
 				return true;
 			} else if (object instanceof PageflowLinkEditPart) {
				PageflowLink pfLink = (PageflowLink) ((PageflowLinkEditPart) object).getModel();
 				if (PageflowValidation.getInstance().isValidLinkForProperty(
 						pfLink)) {
 					return true;
 				}
 			} else {
 				return true;
 			}
		} else if (selection instanceof TextSelection) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ISectionDescriptor#getAfterSection()
 	 */
 	public String getAfterSection() {
 		return "";
 	}
 
 	public int getEnablesFor() {
		// TODO Auto-generated method stub
 		return ENABLES_FOR_ANY;
 	}

 }
