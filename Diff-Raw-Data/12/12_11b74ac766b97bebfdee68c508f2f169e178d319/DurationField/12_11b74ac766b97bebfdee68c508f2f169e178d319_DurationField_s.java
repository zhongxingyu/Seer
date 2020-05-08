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
 * $Id: DurationField.java,v 1.8 2003-11-13 17:30:51 shahid.shah Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
 import java.util.Date;
 import java.text.ParseException;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogValidationContext;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldFlags;
 import com.netspective.sparx.form.field.DialogFieldStates;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.commons.value.ValueSource;
 
 public class DurationField extends DialogField
 {
     public class Flags extends DialogFieldFlags
     {
         public Flags()
         {
         }
 
         public Flags(State dfs)
         {
             super(dfs);
         }
 
         public void flagsChanged()
         {
             super.flagsChanged();
             if(beginField != null)
             {
                 DateTimeField.Flags flags = (DateTimeField.Flags) beginField.getFlags();
                 flags.updateFlag(REQUIRED, flagIsSet(REQUIRED));
                 flags.updateFlag(COLUMN_BREAK_AFTER, flagIsSet(COLUMN_BREAK_AFTER));
                 flags.updateFlag(DateTimeField.Flags.POPUP_CALENDAR, flagIsSet(DateTimeField.Flags.POPUP_CALENDAR));
             }
 
             if(endField != null)
             {
                 DateTimeField.Flags flags = (DateTimeField.Flags) endField.getFlags();
                 flags.updateFlag(REQUIRED, flagIsSet(REQUIRED));
                 flags.updateFlag(COLUMN_BREAK_BEFORE, flagIsSet(COLUMN_BREAK_BEFORE));
                 flags.updateFlag(DateTimeField.Flags.POPUP_CALENDAR, flagIsSet(DateTimeField.Flags.POPUP_CALENDAR));
             }
         }
     }
 
     protected DateTimeField beginField;
     protected DateTimeField endField;
 
 
     public DurationField()
     {
         super();
         createContents();
     }
 
     /**
      * Creates the begin and end children fields
      */
     public void createContents()
     {
         beginField = new DateTimeField();
         beginField.setName("begin");
         beginField.setCaption(new StaticValueSource("Begin"));
 
         endField = new DateTimeField();
         endField.setName("end");
         endField.setCaption(new StaticValueSource("End"));
     }
 
     public DialogFieldFlags createFlags()
     {
         return new Flags();
     }
 
     public DateTimeField getBeginField()
     {
         return beginField;
     }
 
     public DateTimeField getEndField()
     {
         return endField;
     }
 
     public void setDataType(DateTimeField.DataType dataType)
     {
         beginField.setDataType(dataType);
         endField.setDataType(dataType);
     }
 
     public void setDefaultBegin(ValueSource beginValue)
     {
         beginField.setDefault(beginValue);
     }
 
     public void setDefaultEnd(ValueSource endValue)
     {
         endField.setDefault(endValue);
     }
 
     public void setBeginMinValue(ValueSource beginValue) throws ParseException
     {
         beginField.setMin(beginValue);
     }
 
     public void setEndMaxValue(ValueSource endValue) throws ParseException
     {
         endField.setMax(endValue);
     }
 
     public void validate(DialogValidationContext dvc)
     {
         super.validate(dvc);
         if(!dvc.isValid())
             return;
 
         DialogContext dc = dvc.getDialogContext();
         DialogFieldStates states = dc.getFieldStates();
 
         Date beginDate = (Date) states.getState(beginField).getValue().getValue();
         Date endDate = (Date) states.getState(endField).getValue().getValue();
 
        if(beginDate.after(endDate))
         {
             invalidate(dc, "Beginning value should be before ending value.");
             return;
         }
     }
 
     public void finalizeContents()
     {
         // add the begin and end fields
         addField(beginField);
         addField(endField);
         super.finalizeContents();
     }
 }
