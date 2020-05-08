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
 * $Id: TextField.java,v 1.22 2004-03-25 05:01:28 aye.thu Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
import com.netspective.commons.text.TextUtils;
import com.netspective.commons.validate.rule.TextValueValidationRule;
 import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.field.DialogField;
import com.netspective.sparx.form.field.DialogFieldFlags;
 import com.netspective.sparx.form.field.DialogFieldValidations;
 import com.netspective.sparx.form.field.DialogFieldValue;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;
 
 import java.io.IOException;
 import java.io.Writer;
 
 public class TextField extends DialogField
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     private static final Log log = LogFactory.getLog(TextField.class);
     public static Perl5Util perlUtil = new Perl5Util();
 
     public static final Flags.FlagDefn[] TEXT_FIELD_FLAG_DEFNS = new Flags.FlagDefn[DialogFieldFlags.FLAG_DEFNS.length + 4];
     static
     {
         for(int i = 0; i < DialogFieldFlags.FLAG_DEFNS.length; i++)
             TEXT_FIELD_FLAG_DEFNS[i] = DialogFieldFlags.FLAG_DEFNS[i];
         TEXT_FIELD_FLAG_DEFNS[DialogFieldFlags.FLAG_DEFNS.length + 0] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "MASK_ENTRY", Flags.MASK_ENTRY);
         TEXT_FIELD_FLAG_DEFNS[DialogFieldFlags.FLAG_DEFNS.length + 1] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "UPPERCASE", Flags.UPPERCASE);
         TEXT_FIELD_FLAG_DEFNS[DialogFieldFlags.FLAG_DEFNS.length + 2] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "LOWERCASE", Flags.LOWERCASE);
         TEXT_FIELD_FLAG_DEFNS[DialogFieldFlags.FLAG_DEFNS.length + 3] = new XdmBitmaskedFlagsAttribute.FlagDefn(Flags.ACCESS_XDM, "TRIM", Flags.TRIM);
     }
 
     public class Flags extends DialogFieldFlags
     {
         public static final int MASK_ENTRY = DialogFieldFlags.START_CUSTOM;
         public static final int UPPERCASE = MASK_ENTRY * 2;
         public static final int LOWERCASE = UPPERCASE * 2;
         public static final int TRIM = LOWERCASE * 2;
         public static final int START_CUSTOM = TRIM * 2;
 
         public Flags()
         {
 
         }
 
         public Flags(int flags)
         {
             super(flags);
         }
 
         public Flags(State dfs)
         {
             super(dfs);
         }
 
         public Flags(TextField df)
         {
             super(df);
         }
 
         public FlagDefn[] getFlagsDefns()
         {
             return TEXT_FIELD_FLAG_DEFNS;
         }
     }
 
     public class TextFieldState extends State
     {
         public class TextFieldValue extends BasicStateValue
         {
             public Class getValueHolderClass()
             {
                 return String.class;
             }
 
             public boolean hasValue()
             {
                 String textValue = getTextValue();
                 return textValue != null && textValue.length() > 0;
             }
         }
 
         public TextFieldState(DialogContext dc, DialogField field)
         {
             super(dc, field);
         }
 
         public DialogFieldValue constructValueInstance()
         {
             return new TextFieldValue();
         }
     }
 
     private int size = 32;
     private String displayPattern;
     private String submitPattern;
     private TextValueValidationRule textValidationRule;
 
     public TextField()
     {
     }
 
     public DialogField.State constructStateInstance(DialogContext dc)
     {
         return new TextFieldState(dc, this);
     }
 
     public Class getStateClass()
     {
         return TextFieldState.class;
     }
 
     public Class getStateValueClass()
     {
         return TextFieldState.TextFieldValue.class;
     }
 
     public int getMaxLength()
     {
         return textValidationRule.getMaxLength();
     }
 
     /**
      * Convenience wrapper so that max-length will be available as a XDM attribute in XML
      */
     public void setMaxLength(int maxLength)
     {
         textValidationRule.setMaxLength(maxLength);
     }
 
     /**
      * Convenience wrapper so that min-length will be available as a XDM attribute in XML
      */
     public void setMinLength(int minLength)
     {
         textValidationRule.setMinLength(minLength);
     }
 
     /**
      * Convenience wrapper so that reg-expr will be available as a XDM attribute in XML
      */
     public void setRegExpr(String regExpr)
     {
         textValidationRule.setRegExpr(regExpr);
     }
 
     public void setInvalidRegExMessage(String invalidRegExMessage)
     {
         textValidationRule.setInvalidRegExMessage(invalidRegExMessage);
     }
 
     public DialogFieldValidations constructValidationRules()
     {
         DialogFieldValidations rules = super.constructValidationRules();
         textValidationRule = new TextValueValidationRule();
         textValidationRule.setMinLength(0);
         textValidationRule.setMaxLength(255);
         rules.addRule(textValidationRule);
         return rules;
     }
 
     public void addValidation(DialogFieldValidations rules)
     {
         super.addValidation(rules);
 
         // hang onto the text validation rule since we're going to need it for javascript definition and rendering
         boolean found = false;
         for(int i = 0; i < rules.size(); i++)
         {
             if(rules.get(i) instanceof TextValueValidationRule)
             {
                 if(found)
                     throw new RuntimeException("Multiple text validation rules not allowed.");
 
                 textValidationRule = (TextValueValidationRule) rules.get(i);
                 found = true;
             }
         }
     }
 
     public DialogFieldFlags createFlags()
     {
         return new TextField.Flags();
     }
 
     public DialogFieldFlags createFlags(State state)
     {
         return new TextField.Flags(state);
     }
 
     public final int getSize()
     {
         return size;
     }
 
     public void setSize(int value)
     {
         size = value;
     }
 
     public String getDisplayPattern()
     {
         return displayPattern;
     }
 
     public void setDisplayPattern(String displayPattern)
     {
         this.displayPattern = displayPattern;
     }
 
     public String getSubmitPattern()
     {
         return submitPattern;
     }
 
     public void setSubmitPattern(String formatPattern)
     {
         this.submitPattern = formatPattern;
     }
 
     /**
      * Format the dialog field value for every dialog stage(display/validation stages) but not
      * after successful validation(submit stage).
      *
      * @param   value field value
      * @return String formatted text
      */
     public String formatDisplayValue(String value)
     {
         if(value == null) return null;
 
         long flags = getFlags().getFlags();
         if((flags & Flags.UPPERCASE) != 0) value = value.toUpperCase();
         if((flags & Flags.LOWERCASE) != 0) value = value.toLowerCase();
         if((flags & Flags.TRIM) != 0) value = value.trim();
 
         String pattern = getDisplayPattern();
         if(pattern != null)
         {
             synchronized(perlUtil)
             {
                 try
                 {
                     value = perlUtil.substitute(pattern, value);
                 }
                 catch(MalformedPerl5PatternException e)
                 {
                     value = e.toString();
                     log.error("Unable to format display value " + value, e);
                 }
             }
         }
         return value;
     }
 
     /**
      * Format the dialog field value after successful validation.
      *
      * @param   value field value
      * @return String formatted text
      */
     public String formatSubmitValue(String value)
     {
         if(value == null) return null;
 
         long flags = getFlags().getFlags();
         if((flags & Flags.UPPERCASE) != 0) value = value.toUpperCase();
         if((flags & Flags.LOWERCASE) != 0) value = value.toLowerCase();
         if((flags & Flags.TRIM) != 0) value = value.trim();
 
         String pattern = getSubmitPattern();
         if(pattern != null)
         {
             synchronized(perlUtil)
             {
                 try
                 {
                     value = perlUtil.substitute(pattern, value);
                 }
                 catch(MalformedPerl5PatternException e)
                 {
                     value = e.toString();
                     log.error("Unable to format submit value " + value, e);
                 }
             }
         }
         return value;
     }
 
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         if(isInputHidden(dc))
         {
             writer.write(getHiddenControlHtml(dc));
             return;
         }
 
         DialogField.State state = dc.getFieldStates().getState(this);
         DialogFieldFlags stateFlags =  state.getStateFlags();
         String textValue = state.getValue().getTextValue();
 
         if(textValue == null)
             textValue = "";
         else
             textValue = TextUtils.escapeHTML(textValue);
 
         String className = isRequired(dc) ? dc.getSkin().getControlAreaRequiredStyleClass() : dc.getSkin().getControlAreaStyleClass();
 
         String controlAreaStyle = dc.getSkin().getControlAreaStyleAttrs();
         if(isReadOnly(dc))
         {
             writer.write("<input type='hidden' name='" + getHtmlFormControlId() + "' value=\"" + textValue + "\"><span id='" + getQualifiedName() + "'>" + textValue + "</span>");
         }
         else if(isBrowserReadOnly(dc))
         {
             className = dc.getSkin().getControlAreaReadonlyStyleClass();
             writer.write("<input type=\"text\" id=\"" + getHtmlFormControlId() + "\" name=\"" + getHtmlFormControlId() + "\" readonly value=\"" +
                     textValue + "\" maxlength=\"" + textValidationRule.getMaxLength() + "\" size=\"" + size + "\" " + controlAreaStyle +
                     " class=\"" + className + "\" " + dc.getSkin().getDefaultControlAttrs() + ">");
         }
         else if(!stateFlags.flagIsSet(Flags.MASK_ENTRY))
         {
             writer.write("<input type=\"text\" id=\"" + getHtmlFormControlId() + "\" name=\"" + getHtmlFormControlId() + "\" value=\"" + textValue + "\" maxlength=\"" +
                     textValidationRule.getMaxLength() + "\" size=\"" + size + "\" " + controlAreaStyle + " class=\"" + className + "\" " +
                     dc.getSkin().getDefaultControlAttrs() + ">");
         }
         else
         {
             writer.write("<input type=\"password\" id=\"" + getHtmlFormControlId() + "\" name=\"" + getHtmlFormControlId() + "\" value=\"" + textValue + "\" maxlength=\"" +
                     textValidationRule.getMaxLength() + "\" size=\"" + size + "\" " + controlAreaStyle + " class=\"" + className + "\" " +
                     dc.getSkin().getDefaultControlAttrs() + ">");
         }
     }
 
     /**
      *
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
 
         DialogFieldFlags flags =  getFlags();
 
         if(flags.flagIsSet(Flags.UPPERCASE))
             buf.append("field.uppercase = 'yes';\n");
         else
             buf.append("field.uppercase = 'no';\n");
 
         if(flags.flagIsSet(Flags.LOWERCASE))
             buf.append("field.lowercase = 'yes';\n");
         else
             buf.append("field.lowercase = 'no';\n");
 
         if(flags.flagIsSet(Flags.TRIM))
             buf.append("field.trim = 'yes';\n");
         else
             buf.append("field.trim = 'no';\n");
 
         if(textValidationRule != null)
         {
             if(textValidationRule.getRegExpr() != null)
             {
                buf.append("field.text_format_pattern = /" + textValidationRule.getRegExpr() + "/;\n");
                 if(textValidationRule.getInvalidRegExMessage() != null)
                     buf.append("field.text_format_err_msg = \"" + textValidationRule.getInvalidRegExMessage() + "\";\n");
             }
         }
 
         return buf.toString();
     }
 }
