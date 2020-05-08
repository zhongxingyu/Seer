 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.clientcerts.wizards.forms;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.Globals;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 import com.adito.wizard.AbstractWizardSequence;
 import com.adito.wizard.forms.DefaultWizardForm;
 
 /**
  */
 public class CreateClientCertDetailForm extends DefaultWizardForm {
     private static final int NO_TYPE_SELECTED = -1; 
     public static final String ATTR_TYPE = "type";
     private int type = 0;
    private static final Log LOG = LogFactory.getLog(CreateClientCertDetailForm.class);
 
     /**
      * Construtor
      */
     public CreateClientCertDetailForm() {
     /**
      * @param nextAvailable
      * @param previousAvailable
      * @param page
      * @param focussedField
      * @param autoComplete
      * @param finishAvailable
      * @param pageName
      * @param resourceBundle
      * @param resourcePrefix
      * @param stepIndex
      */
        super(true, false, "/WEB-INF/jsp/content/clientcerts/clientcertwizard/createClientCert.jspf", "", false, false,
                        "createClientCert", "clientCerts", "clientCerts.createClientCertWizard", 1);
     }
         
     /* (non-Javadoc)
      * @see com.adito.wizard.forms.AbstractWizardForm#apply(com.adito.wizard.AbstractWizardSequence)
      */
     public void apply(AbstractWizardSequence sequence) throws Exception {
	LOG.info("apply called");
         super.apply(sequence);
         sequence.putAttribute(ATTR_TYPE, new Integer(type));
	LOG.info("apply finished");
     }
 
     /**
      * @return int
      */
     public int getType() {
         return type;
     }
 
     /**
      * @param type
      */
     public void setType(int type) {
         this.type = type;
     }
 
     @Override
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
	LOG.info("validate called");
         if(isCommiting() && type == NO_TYPE_SELECTED) {
             ActionErrors errors = new ActionErrors();
             errors.add(Globals.ERROR_KEY, new ActionMessage("clientCerts.createClientCertWizard.error.noSelection"));
             return errors;
         }
	LOG.info("validate finished");
         return null;
     }
 }
