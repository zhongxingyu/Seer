 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.taglibprocessing.attributevalues;
 
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigManager;
 import org.eclipse.jst.jsf.facesconfig.emf.ValidatorType;
 import org.eclipse.jst.jsf.metadataprocessors.features.PossibleValue;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * ValidatorID attribute feature
  * 
  * <p><b>Provisional API - subject to change</b></p>
  *
  */
 public class FacesConfigValidatorIDFeatures extends FacesConfigIdentifierFeatures {
 
 	/**
 	 * Faces validator classname
 	 */
 	protected static final String VALIDATOR = "javax.faces.validator.Validator"; //$NON-NLS-1$
 	/**
 	 * Imagename to use when displaying validator
 	 */
 	protected static final String IMAGE_NAME = "/icons/full/obj16/FacesConfig_Validator.gif"; //$NON-NLS-1$
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getReturnType()
 	 */
 	protected String getReturnType(){ return VALIDATOR;}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getElements(org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigManager)
 	 */
 	protected List getElements(JSFAppConfigManager mgr) {
 		if (mgr != null)
 			return mgr.getValidators();
 		return new ArrayList(0);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getPossibleValueProposals(java.util.List)
 	 */
 	protected List getPossibleValueProposals(List elements) {
 		List ret = new ArrayList();
 		Collections.sort(elements, new ValidatorSorter());
 		for (Iterator it = elements.iterator();it.hasNext();){
 			ValidatorType obj = (ValidatorType)it.next();
 			if (obj.getValidatorId() != null && obj.getValidatorId().getTextContent() != null){
 				PossibleValue pv = createProposal(obj.getValidatorId().getTextContent(), obj.getDisplayName(), obj.getDescription());
 				if (pv != null){
 					pv.setIcon(getImage());
 					ret.add(pv);
 				}
 			}
 		}
 		return ret;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getImageName()
 	 */
 	protected String getImageName() {		
 		return IMAGE_NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getMyValidationMessage(java.lang.String)
 	 */
 	protected String getMyValidationMessage(String value) {		
 		if (value == null || value.trim().equals("")) //$NON-NLS-1$
 			return Messages.FacesConfigValidatorIDFeatures_validatorid_empty;
 		
 		return NLS.bind(Messages.FacesConfigIdentifierType_invalid_validator_id, new String[]{singleQuote(value)});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getElementIDs(org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigManager)
 	 */
 	protected List getElementIDs(JSFAppConfigManager mgr) {
 		List elements = getElements(mgr);
 		List ret = new ArrayList(elements.size());		
 		for (Iterator it = elements.iterator();it.hasNext();){
 			ValidatorType aType = (ValidatorType)it.next();
 			if (aType.getValidatorId() != null && aType.getValidatorId().getTextContent() != null){
 				String id = aType.getValidatorId().getTextContent();
 				if (id != null)
 					ret.add(id.trim());
 			}
 		}
 		return ret;
 	}
 	
 	/**
 	 * Validator id sorter - incomplete
 	 */
	private static class ValidatorSorter implements Comparator, Serializable {
		/**
         * 
         */
        private static final long serialVersionUID = -398026037193914126L;
 
        public int compare(Object o1, Object o2) {		
 			//TODO
 			return 0;
 
 		}
 		
 	}
 }
