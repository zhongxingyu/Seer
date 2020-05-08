 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: DialogFieldValue.java,v 1.2 2002-02-05 00:00:41 thua Exp $
  */
 
 package com.netspective.sparx.util.value;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.Map;
 
 import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
 
 import com.netspective.sparx.xaf.form.Dialog;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.field.SelectChoice;
 import com.netspective.sparx.xaf.form.field.SelectChoicesList;
 import com.netspective.sparx.xaf.sql.StatementManager;
 
 public class DialogFieldValue extends ValueSource implements ListValueSource
 {
     public DialogFieldValue()
     {
     }
 
     public SingleValueSource.Documentation getDocumentation()
     {
         return new SingleValueSource.Documentation(
                 "Provides access to a specific field of a dialog.",
                 "field-name"
         );
     }
 
     public String getValue(ValueContext vc)
     {
         if(vc instanceof DialogContext)
         {
             return ((DialogContext) vc).getValue(valueKey);
         }
         else
         {
             ServletRequest request = vc.getRequest();
             DialogContext dc = (DialogContext) request.getAttribute(DialogContext.DIALOG_CONTEXT_ATTR_NAME);
             if(dc != null)
             {
                 return dc.getValue(valueKey);
             }
             else
            {
                 return vc.getRequest().getParameter(Dialog.PARAMNAME_CONTROLPREFIX + valueKey);
            }
         }
     }
 
     public SelectChoicesList getSelectChoices(ValueContext vc)
     {
         SelectChoicesList choices = new SelectChoicesList();
         String[] values = getValues(vc);
         for(int i = 0; i < values.length; i++)
             choices.add(new SelectChoice(values[i]));
         return choices;
     }
 
     public String[] getValues(ValueContext vc)
     {
        if (vc instanceof DialogContext)
        {
            String[] values =  ((DialogContext) vc).getValues(valueKey);
            if (values != null)
                return values;
            else
                return new String[] {((DialogContext) vc).getValue(valueKey)};
        }
        else
        {
            return vc.getRequest().getParameterValues(Dialog.PARAMNAME_CONTROLPREFIX + valueKey);
        }
     }
 
     public boolean supportsSetValue()
     {
         return true;
     }
 
     public void setValue(ValueContext vc, String value)
     {
         if(vc instanceof DialogContext)
         {
             ((DialogContext) vc).setValue(valueKey, (String) value);
         }
         else
         {
             throw new RuntimeException("DialogFieldValue.setValue(ValueContext, String) requires a DialogContext as its ValueContext.");
         }
     }
 
     public void setValue(ValueContext vc, ResultSet rs, int storeType) throws SQLException
     {
         if(storeType != RESULTSET_STORETYPE_SINGLEROWFORMFLD)
             throw new RuntimeException("DialogFieldValue.setValue(ValueContext, ResultSet, int) only supports STORETYPE_SINGLEROWFORMFLD");
 
         if(vc instanceof DialogContext)
         {
             DialogContext dc = (DialogContext) vc;
             if(rs.next())
             {
                 ResultSetMetaData rsmd = rs.getMetaData();
                 int colsCount = rsmd.getColumnCount();
                 Map fieldStates = dc.getFieldStates();
                 for(int i = 1; i <= colsCount; i++)
                 {
                     String fieldName = rsmd.getColumnName(i).toLowerCase();
                     DialogContext.DialogFieldState state = (DialogContext.DialogFieldState) fieldStates.get(fieldName);
                     if(state != null)
                         state.value = rs.getString(i);
                 }
             }
         }
         else
         {
             Map rsMap = StatementManager.getResultSetSingleRowAsMap(rs);
             DialogContext dc = (DialogContext) vc.getRequest().getAttribute(DialogContext.DIALOG_CONTEXT_ATTR_NAME);
             if(dc != null)
             {
                 // dialog context has already been created and is available in the request
                 dc.assignFieldValues(rsMap);
             }
             else
             {
                 // stash this away so when the DialogContext is created, the values are available
                 vc.getRequest().setAttribute(DialogContext.DIALOG_FIELD_VALUES_ATTR_NAME, rsMap);
             }
         }
     }
 }
