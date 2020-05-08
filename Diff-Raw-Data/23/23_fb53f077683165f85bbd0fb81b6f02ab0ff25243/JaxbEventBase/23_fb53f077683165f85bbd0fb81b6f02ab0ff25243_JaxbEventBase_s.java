 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004 Megatome Technologies.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution, if
  *    any, must include the following acknowlegement:
  *       "This product includes software developed by
  *        Megatome Technologies."
  *    Alternately, this acknowlegement may appear in the software itself,
  *    if and wherever such third-party acknowlegements normally appear.
  *
  * 4. The names "The Frame2 Project", and "Frame2", 
  *    must not be used to endorse or promote products derived
  *    from this software without prior written permission. For written
  *    permission, please contact iamthechad@sourceforge.net.
  *
  * 5. Products derived from this software may not be called "Frame2"
  *    nor may "Frame2" appear in their names without prior written
  *    permission of Megatome Technologies.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL MEGATOME TECHNOLOGIES OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  */
 package org.megatome.frame2.jaxb;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.ValidationEvent;
 import javax.xml.bind.ValidationEventHandler;
 import javax.xml.bind.Validator;
 
 import org.megatome.frame2.errors.Errors;
 import org.megatome.frame2.event.CommonsValidatorEvent;
 
 
 /**
  * The JaxbEventBase is a base class that helps bind JavaBeans generated through JAXB into Frame2
  * framework.  By specifying this as base class for JAXB generated classes they become Frame2
  * events complete with appropriate validation behaviors.  See the JAXB User Guide for instructions
  * on how to customize the bindings for an external superclass.
  */
 public class JaxbEventBase extends CommonsValidatorEvent {
 
    /**
     * Validate the state of the object using the internal JAXB validator.
     * @param errors Errors object that will be populated with any validation errors.
     * @return The result of validation 
     * @see org.megatome.frame2.event.Event#validate(Errors)
     */
    public boolean validate(Errors errors) {
       boolean result = false;
 
       ValidationMonitor monitor = new ValidationMonitor();
 
       try {
          JAXBContext context = JAXBContext.newInstance(getPackageName(), this.getClass().getClassLoader());
          Validator validator = context.createValidator();
 
          validator.setEventHandler(monitor);
 
          result = validator.validate(this);
       } catch (Exception e) {
          if (errors != null) {
             errors.add(getPackageName(), e.getMessage());
          }
       } finally {
          monitor.populate(errors);
       }
       
       // calling super's validate after jaxb validate
       // per customer requirement
       result &= super.validate(errors);
 
       return result;
    }
 
    private String getPackageName() {
       final int backoff = ".impl".length();
 
       String implPackageName = getClass().getPackage().getName();
 
       return implPackageName.substring(0, implPackageName.length() - backoff);
    }
 
    private class ValidationMonitor implements ValidationEventHandler {
       private List _events = new ArrayList();
 
       /**
        * @see javax.xml.bind.ValidationEventHandler#handleEvent(ValidationEvent)
        */
       public boolean handleEvent(ValidationEvent arg0) {
          _events.add(arg0);
 
          return true;
       }
 
       void populate(Errors errors) {
          if ((errors != null) && !_events.isEmpty()) {
 
             for (int i = 0; i < _events.size(); i++) {
                ValidationEvent event = (ValidationEvent) _events.get(i);
 
                String key = getKey(event);
                String msg = getMessage(event);
 
                errors.add(key, msg);
             }
          }
       }
 
       // DOC: In the present JAXB implementation we can't resolve from the
       // ValidationEvent 
       String getKey(ValidationEvent event) {
          String className = event.getLocator().getObject().getClass().getName();
 
          int delimiterIndex = className.indexOf("$");
 
          StringBuffer buffer = null;
 
          if (delimiterIndex == -1) {
             buffer = new StringBuffer(className);
          } else {
             buffer = new StringBuffer(className.substring(0, className.indexOf("$")));
          }
 
          String attr = getAttributeName(event);
 
          if (attr != null) {
             buffer.append(".");
             buffer.append(attr);
          }
 
          return buffer.toString();
       }
 
       String getMessage(ValidationEvent event) {
          String message = event.getMessage();
 
          if (isAttribute(event)) {
             return message.substring(message.indexOf(":") + 2);
          } else {
             return message;
          }
       }
 
       String getAttributeName(ValidationEvent event) {
          if (isAttribute(event)) {
            return "partNum";
         } else {
            return null;
         }
       }
 
       boolean isAttribute(ValidationEvent event) {
       	 boolean retval = false;
       	 String msg = event.getMessage();
       	 if (msg != null) {
       	 	retval = msg.indexOf("attribute") == 0;
          }
       	 
          return retval;
       }
    }
 }
