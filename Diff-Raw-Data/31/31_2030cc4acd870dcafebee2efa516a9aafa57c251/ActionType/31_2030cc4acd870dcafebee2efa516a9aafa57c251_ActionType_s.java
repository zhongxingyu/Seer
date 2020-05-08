 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler/Oracle - initial API and implementation
  *    Xiaonan Jiang/IBM  - https://bugs.eclipse.org/bugs/show_bug.cgi?id=308337
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.taglibprocessing.attributevalues;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IWorkspaceContextResolver;
 import org.eclipse.jst.jsf.core.JSFVersion;
 import org.eclipse.jst.jsf.core.jsfappconfig.internal.JSFAppConfigManagerFactory;
 import org.eclipse.jst.jsf.facesconfig.FacesConfigPlugin;
 import org.eclipse.jst.jsf.facesconfig.emf.DisplayNameType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationCaseType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationRuleType;
 import org.eclipse.jst.jsf.metadataprocessors.features.IPossibleValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidationMessage;
 import org.eclipse.jst.jsf.metadataprocessors.features.PossibleValue;
 import org.eclipse.jst.jsf.metadataprocessors.features.ValidationMessage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.osgi.framework.Bundle;
 
 /**
  * Meta-data processing type representing an "action" attribute
  * 
  * <p><b>Provisional API - subject to change</b></p>
  * @author Gerry Kessler - Oracle
  */
 public class ActionType extends MethodBindingType implements IPossibleValues{
 	/**
 	 * Image to use if metadata defined image cannot be created
 	 */
 	protected static final ImageDescriptor MISSING_IMAGE = ImageDescriptor.getMissingImageDescriptor();
 	
 	private static final String IMAGE_NAME = "/icons/full/obj16/NavigationCaseType.gif"; //$NON-NLS-1$
 	private ImageDescriptor imageDescriptor;
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.MethodBindingType#isValidValue(java.lang.String)
 	 */
 	public boolean isValidValue(String value){
 		if (value == null || (value != null && value.length() == 0)) {
 			IValidationMessage msg = new ValidationMessage(Messages.ActionType_invalid_empty_value);
 			getValidationMessages().add(msg);
 			return false;
 		}
 		//any other value should be one of the possible values
 		//optimize
 		IWorkspaceContextResolver wr = IStructuredDocumentContextResolverFactory.INSTANCE.getWorkspaceContextResolver(getStructuredDocumentContext());
 		if (wr == null)
 			return true;//shouldn't get here
 		
 		//in case that this is not JSF faceted or missing configs, need to pass
 		if (JSFAppConfigManagerFactory.getJSFAppConfigManagerInstance(wr.getProject()) == null) 
 			return true;
 			
 		IFile jsp = (IFile)wr.getResource();
 		List<NavigationRuleType> rules = JSFAppConfigManagerFactory.getJSFAppConfigManagerInstance(wr.getProject()).getNavigationRulesForPage(jsp);
 		for (final NavigationRuleType rule : rules) {
 			for (Iterator cases=rule.getNavigationCase().iterator();cases.hasNext();) {				
 				NavigationCaseType navCase = (NavigationCaseType)cases.next();					
				if (navCase.getFromOutcome() != null && 
 						value.equals(navCase.getFromOutcome().getTextContent().trim()))
 					return true;				
 			}
 		}
 		if (JSFVersion.valueOfProject(jsp.getProject()).compareTo(JSFVersion.V2_0) >= 0) {
 			int index = value.indexOf('?');
 			if (index != -1) {
 				value = value.substring(0, index);
 			}
 			if (value != null && value.length() > 1) {
 				IVirtualFolder webRoot = ComponentCore.createComponent(jsp.getProject()).getRootFolder();
 				if (value.charAt(0) == '/') {
 					IVirtualFile file = webRoot.getFile(new Path(value));
 					if (file.exists()) {
 						return true;
 					}
 				} else {
 					IPath webContentPath = webRoot.getUnderlyingFolder().getFullPath();
 					IPath filePath = jsp.getFullPath();
 					if (filePath.matchingFirstSegments(webContentPath) == webContentPath.segmentCount()) {
 						String extension = filePath.getFileExtension();
 						filePath = filePath.removeFirstSegments(webContentPath.segmentCount());
 						filePath = filePath.removeLastSegments(1);
 						filePath = filePath.append(value);
 						if (filePath.getFileExtension() == null && extension != null) {
 							filePath = filePath.addFileExtension(extension);
 						}
 						IVirtualFile file = webRoot.getFile(filePath);
 						if (file.exists()) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 
 		IValidationMessage msg = new ValidationMessage(Messages.ActionType_invalid_value);
 		getValidationMessages().add(msg);
 		return false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.metadataprocessors.features.IPossibleValues#getPossibleValues()
 	 */
 	public List getPossibleValues() {
 		final List<NavigationRuleType> ret = new ArrayList<NavigationRuleType>();
 		if (getStructuredDocumentContext() == null)
 			return ret;
 		
 		final IWorkspaceContextResolver wr = IStructuredDocumentContextResolverFactory.INSTANCE.getWorkspaceContextResolver(getStructuredDocumentContext());
 		if (wr != null && JSFAppConfigManagerFactory.getJSFAppConfigManagerInstance(wr.getProject()) != null) {//may not be JSF faceted project or know faces-config){			
 			IFile jsp = (IFile)wr.getResource();
 			List<NavigationRuleType> rules = JSFAppConfigManagerFactory.getJSFAppConfigManagerInstance(wr.getProject()).getNavigationRulesForPage(jsp);
 			for (final NavigationRuleType rule : rules) {
 				if (rule != null)
 					ret.addAll(createProposals(rule));
 			}
 		}
 		return ret;
 	}
 
 	private List createProposals(NavigationRuleType rule) {
 		List ret = new ArrayList();
 		List cases = rule.getNavigationCase();
 		for (Iterator it=cases.iterator();it.hasNext();) {
 			NavigationCaseType navCase = (NavigationCaseType)it.next();
 			PossibleValue pv = createProposal(rule, navCase);
 			if (pv != null)
 				ret.add(pv);
 		}
 		return ret;
 	}
 
 	private PossibleValue createProposal(NavigationRuleType rule, NavigationCaseType navCase) {
 		PossibleValue pv = null;
 		String value = null;
 		String ruleDisp = null;
 		String navDisplay = null;
 		String navAction = null;
 		String toViewId = null;
 		
 		if (navCase.getFromOutcome() != null)
 			value = navCase.getFromOutcome().getTextContent();
 		if (navCase.getToViewId()!= null)
 			toViewId = navCase.getToViewId().getTextContent();
 		if (rule.getFromViewId() != null)
 			ruleDisp = rule.getFromViewId().getTextContent();
 		if (navCase.getDisplayName() != null
 				&& navCase.getDisplayName().size() > 0) {
 			navDisplay = ((DisplayNameType) navCase
 					.getDisplayName().get(0)).getTextContent();	
 		}
 		if (navCase.getFromAction() != null) {
 			navAction= navCase.getFromAction().getTextContent();	
 		}
 		if (navDisplay == null || navDisplay.trim().equals("")) //$NON-NLS-1$
 			navDisplay = NLS.bind(Messages.ActionType_navcase_display, new String[]{value, toViewId});
 		
 		if (value != null){
 			pv = new PossibleValue(value, navDisplay);
 			pv.setIcon(getNavCaseImageDescriptor());
 			pv.setAdditionalInformation("from-outcome: "+value  //$NON-NLS-1$
 										+ "<br>to-view-id: " + toViewId //$NON-NLS-1$
 										+ "<br>from-view-id: " + ruleDisp //$NON-NLS-1$
 										+ "<br>from-action: " + (navAction == null ? "null" : navAction)); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		return pv;
 	}
 
 	
 	private ImageDescriptor getNavCaseImageDescriptor() {
 		ImageDescriptor ret = super.getImage();
 		if (ret != null && ret != MISSING_IMAGE)
 			return ret;
 
 		if (imageDescriptor == null){				
 			imageDescriptor = getImageDesc(IMAGE_NAME);
 		}
 		return imageDescriptor;
 
 	}
 	
 	private ImageDescriptor getImageDesc(String img) 
 	{
 		Bundle bundle = FacesConfigPlugin.getPlugin().getBundle();
 		URL url = FileLocator.find(bundle, new Path(img), null);
 		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
 		if (desc == MISSING_IMAGE) {
 			return null;
 		}
 		return desc;
 	}
 }
