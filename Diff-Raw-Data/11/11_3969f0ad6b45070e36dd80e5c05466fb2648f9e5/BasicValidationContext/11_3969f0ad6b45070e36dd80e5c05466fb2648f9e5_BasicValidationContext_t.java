 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: BasicValidationContext.java,v 1.3 2004-03-23 20:33:30 shahid.shah Exp $
  */
 
 package com.netspective.commons.validate;
 
 import java.util.List;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.netspective.commons.value.ValueContext;
 
 public class BasicValidationContext implements ValidationContext
 {
     private Map errorsByKey;
     private List contextErrors;
     private List allErrors;
     private ValueContext valueContext;
     private boolean valid = true;
 
     public BasicValidationContext()
     {
         errorsByKey = new HashMap();
         contextErrors = getValidationErrorsForScope(VALIDATIONSCOPE_ENTIRE_CONTEXT);
         allErrors = new ArrayList();
     }
 
     public BasicValidationContext(ValueContext valueContext)
     {
         this();
         this.valueContext = valueContext;
     }
 
    public void reset()
    {
        valid = true;
        errorsByKey.clear();
        contextErrors = getValidationErrorsForScope(VALIDATIONSCOPE_ENTIRE_CONTEXT);
        allErrors.clear();
    }

     public boolean isValid()
     {
         return valid;
     }
 
     public List getValidationErrorsForScope(Object scope)
     {
         List errors = (List) errorsByKey.get(scope);
         if(errors == null)
         {
             errors = new ArrayList();
             errorsByKey.put(scope, errors);
         }
         return errors;
     }
 
     public List getAllValidationErrors()
     {
         return allErrors;
     }
 
     public void addValidationError(ValidationException exception)
     {
         contextErrors.add(exception);
         allErrors.add(exception);
         valid = false;
     }
 
     public void addValidationError(Object key, ValidationException exception)
     {
         getValidationErrorsForScope(key).add(exception);
         allErrors.add(exception);
         valid = false;
     }
 
     public void addValidationError(Object scope, String messageFormat, Object[] formatArgs)
     {
         addError(scope, java.text.MessageFormat.format(messageFormat, formatArgs));
     }
 
     public void addValidationError(String messageFormat, Object[] formatArgs)
     {
         addError(java.text.MessageFormat.format(messageFormat, formatArgs));
     }
 
     public void addError(Object key, String message)
     {
         getValidationErrorsForScope(key).add(message);
         allErrors.add(message);
         valid = false;
     }
 
     public void addError(String message)
     {
         contextErrors.add(message);
         allErrors.add(message);
         valid = false;
     }
 
     public ValueContext getValidationValueContext()
     {
         return valueContext;
     }
 }
