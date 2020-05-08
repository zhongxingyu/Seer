 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.support.kb;
 
 import java.util.Set;
 import org.eclipse.ui.IEditorInput;
 
 import org.jboss.tools.common.kb.KbDinamicResource;
 import org.jboss.tools.common.kb.KbProposal;
 
 public class WTPKbJsfValuesResource extends WTPKbdBeanPropertyResource {
 	private static final String[] fixedJsfValues = {
 		"header", "headerValues", "param", "paramValues",
 		"cookie", "initParam", "requestScope",
 		"sessionScope", "applicationScope",
 		"facesContext", "view"};
 
 	public WTPKbJsfValuesResource(IEditorInput editorInput, WTPTextJspKbConnector connector) {
 		super(editorInput, connector);
 	}
 
 	public String getType() {
 		return KbDinamicResource.JSF_VARIABLES_TYPE;
 	}
 
 	protected void fillSortedProposalStrings(Set sorted, String beanName, boolean hasProperty) {
		if((query.indexOf("#{")>-1 || query.indexOf("${")>-1)
				&& (!query.trim().endsWith("."))
				&& (!query.endsWith(")"))
				&& (!query.endsWith("]"))
				&& (beanName == null || beanName.length() == 0 || !hasProperty)) {
			for (int i = 0; i < fixedJsfValues.length; i++) {
				sorted.add(fixedJsfValues[i]);
			}
 		}
 	}
 
 	protected int getKbProposalRelevance() {
 		return KbProposal.R_JSP_JSF_EL_VARIABLE_ATTRIBUTE_VALUE;
 	}
 
 	/**
      * 
      * @return
      */
     public static String[] getJsfValues() {
     	return fixedJsfValues;
     }
 }
