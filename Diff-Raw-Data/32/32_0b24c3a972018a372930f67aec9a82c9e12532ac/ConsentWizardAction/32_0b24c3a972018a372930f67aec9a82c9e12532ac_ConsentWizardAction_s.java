 /*******************************************************************************
  * Copyright (c) 2012, 5AM Solutions, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
  * following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following 
  * disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following 
  * disclaimer in the documentation 
  * and/or other materials provided with the distribution.
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
  * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.fiveamsolutions.plc.web.struts2.wizard;
 
 import java.util.Map;
 
 import org.apache.struts2.interceptor.SessionAware;
 import org.apache.struts2.interceptor.validation.SkipValidation;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 /**
  * Generic class for all consent wizard actions.
  *
  * @author Abraham J. Evans-EL <aevansel@5amsolutions.com>
  */
 public class ConsentWizardAction extends ActionSupport implements SessionAware {
     private static final long serialVersionUID = 1L;
     private Map<String, Object> session;
 
     /**
      * Field required key.
      */
     protected static final String FIELD_REQUIRED_KEY = "action.requiredFieldMessage";
 
     /**
      * Next.
      */
     public static final String NEXT = "next";
 
     /**
      * Continues the wizard.
      * @return struts2 forwarding result
      */
     public String nextStep() {
         return NEXT;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SkipValidation
     public String execute() {
         return SUCCESS;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setSession(Map<String, Object> session) {
         this.session = session;
     }
 
     /**
      * @return the session
      */
     public Map<String, Object> getSession() {
         return session;
     }
 }
