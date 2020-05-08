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
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterType;
 import org.eclipse.jst.jsf.metadataprocessors.features.PossibleValue;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Meta-data processing feature representing an attribute value requiring a "converterid"
  * 
  * <p><b>Provisional API - subject to change</b></p>
  * @author Gerry Kessler - Oracle
  */
 public class FacesConfigConverterIDFeatures extends FacesConfigIdentifierFeatures {
 
 	/**
 	 * Faces converter classname
 	 */
 	protected static final String CONVERTER = "javax.faces.convert.Converter"; //$NON-NLS-1$
 	/**
 	 * Imagename to use when displaying converter
 	 */
 	protected static final String IMAGE_NAME = "/icons/full/obj16/FacesConfig_Converter.gif"; //$NON-NLS-1$
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getElements(org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigManager)
 	 */
 	protected List getElements(JSFAppConfigManager mgr) {
 		if (mgr != null)
 			return mgr.getConverters();
 		return new ArrayList(0);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getReturnType()
 	 */
 	protected String getReturnType(){ return CONVERTER;}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getPossibleValueProposals(java.util.List)
 	 */
 	protected List getPossibleValueProposals(List elements) {
 		List ret = new ArrayList();
 		Collections.sort(elements, new ConverterSorter());
 		for (Iterator it = elements.iterator();it.hasNext();){
 			ConverterType obj = (ConverterType)it.next();
 			if (obj.getConverterId() != null && obj.getConverterId().getTextContent() != null){
 				PossibleValue pv = createProposal(obj.getConverterId().getTextContent(), obj.getDisplayName(), obj.getDescription());
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
 			return Messages.FacesConfigConverterIDFeatures_converterid_empty;
 		
 		return NLS.bind(Messages.FacesConfigIdentifierType_invalid_converter_id, new String[]{singleQuote(value)});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.taglibprocessing.attributevalues.FacesConfigIdentifierFeatures#getElementIDs(org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigManager)
 	 */
 	protected List getElementIDs(JSFAppConfigManager mgr) {
 		List elements = getElements(mgr);
 		List ret = new ArrayList(elements.size());		
 		for (Iterator it = elements.iterator();it.hasNext();){
 			ConverterType aType = (ConverterType)it.next();
 			if (aType.getConverterId() != null && aType.getConverterId().getTextContent() != null){
 				String id = aType.getConverterId().getTextContent();
 				if (id != null)
 					ret.add(id.trim());
 			}
 		}
 		return ret;
 	}
 	
 	/**
 	 * 
 	 * Converter ID Sorter - incomplete
 	 */
	private static class ConverterSorter implements Comparator, Serializable {
	    /**
         * 
         */
        private static final long serialVersionUID = 5255291244511783735L;
 
        public int compare(Object o1, Object o2) {		
 			//TODO
 			return 0;
 
 		}
 		
 	}
 }
