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
 * $Id: SelectField.java,v 1.8 2003-08-28 03:33:18 aye.thu Exp $
  */
 
 package com.netspective.sparx.form.field.type;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.field.DialogFieldPopup;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldValue;
 import com.netspective.sparx.form.field.type.TextField;
 import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
 import com.netspective.commons.xdm.XdmEnumeratedAttribute;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.PresentationValue;
import com.netspective.commons.value.GenericValue;
 import com.netspective.commons.value.source.StaticValueSource;
 
 public class SelectField extends TextField
 {
     private static final Log log = LogFactory.getLog(SelectField.class);
     protected static final int PRESENTATIONITEMFLAG_IS_SELECTED = 1;
 
     public static final Flags.FlagDefn[] SELECT_FIELD_FLAG_DEFNS = new Flags.FlagDefn[TextField.TEXT_FIELD_FLAG_DEFNS.length + 4];
     static
     {
         for(int i = 0; i < TextField.TEXT_FIELD_FLAG_DEFNS.length; i++)
             SELECT_FIELD_FLAG_DEFNS[i] = TextField.TEXT_FIELD_FLAG_DEFNS[i];
         SELECT_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 0] = new Flags.FlagDefn(TextField.Flags.ACCESS_XDM, "SORT_CHOICES", Flags.SORT_CHOICES);
         SELECT_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 1] = new Flags.FlagDefn(TextField.Flags.ACCESS_XDM, "PREPEND_BLANK", Flags.PREPEND_BLANK);
         SELECT_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 2] = new Flags.FlagDefn(TextField.Flags.ACCESS_XDM, "APPEND_BLANK", Flags.APPEND_BLANK);
         SELECT_FIELD_FLAG_DEFNS[TextField.TEXT_FIELD_FLAG_DEFNS.length + 3] = new Flags.FlagDefn(TextField.Flags.ACCESS_XDM, "SEND_CHOICES_TO_CLIENT", Flags.SEND_CHOICES_TO_CLIENT);
     }
 
     public class Flags extends TextField.Flags
     {
         public static final int SORT_CHOICES = TextField.Flags.START_CUSTOM;
         public static final int PREPEND_BLANK = SORT_CHOICES * 2;
         public static final int APPEND_BLANK = PREPEND_BLANK * 2;
         public static final int SEND_CHOICES_TO_CLIENT = APPEND_BLANK * 2;
         public static final int START_CUSTOM = SEND_CHOICES_TO_CLIENT * 2;
 
         public Flags()
         {
         }
 
         public Flags(State dfs)
         {
             super(dfs);
         }
 
         public XdmBitmaskedFlagsAttribute.FlagDefn[] getFlagsDefns()
         {
             return SELECT_FIELD_FLAG_DEFNS;
         }
     }
 
     public static class Style extends XdmEnumeratedAttribute
     {
         public static final int RADIO = 0;
         public static final int COMBO = 1;
         public static final int LIST = 2;
         public static final int MULTICHECK = 3;
         public static final int MULTILIST = 4;
         public static final int MULTIDUAL = 5;
         public static final int POPUP = 6;
         public static final int TEXT = 7;
 
         public static final String[] VALUES = new String[] { "radio", "combo", "list", "multicheck", "multilist", "multidual", "popup", "text" };
 
         public Style()
         {
         }
 
         public Style(int valueIndex)
         {
             super(valueIndex);
         }
 
         public String[] getValues()
         {
             return VALUES;
         }
 
         public boolean isMulti()
         {
             int style = getValueIndex();
             return style == MULTICHECK ||
                    style == MULTILIST ||
                    style == MULTIDUAL;
         }
     }
 
     public class SelectFieldState extends State
     {
         public class SelectFieldValue extends BasicStateValue
         {
             private PresentationValue.Items choices;
             private PresentationValue.Items.Item selectedChoice;
 
             public boolean hasValue()
             {
                 if(isMulti())
                     return size() > 0;
                 else
                 {
                     String textValue = getTextValue();
                     return textValue != null && textValue.length() > 0;
                 }
             }
 
             public PresentationValue.Items getChoices()
             {
                 return choices;
             }
 
             public PresentationValue.Items.Item getSelectedChoice()
             {
                 return selectedChoice;
             }
 
             public void calcSelections(PresentationValue.Items choices)
             {
                 this.choices = choices;
 
                 // make everthing "unselected" by default
                 for(int i = 0; i < choices.size(); i++)
                     choices.getItem(i).setFlags(0);
 
                 if(getField().isMulti())
                 {
                     String[] values = getTextValues();
                     if(values != null)
                     {
                         for(int v = 0; v < values.length; v++)
                         {
                             PresentationValue.Items.Item item = choices.getItemWithValue(values[v]);
                             if(item != null)
                                 item.setFlags(PRESENTATIONITEMFLAG_IS_SELECTED);
                         }
                     }
                 }
                 else
                 {
                     String value = getTextValue();
                     if(value != null)
                     {
                         PresentationValue.Items.Item item = choices.getItemWithValue(value);
                         if(item != null)
                         {
                             item.setFlags(PRESENTATIONITEMFLAG_IS_SELECTED);
                             selectedChoice = item;
                         }
                     }
                 }
             }
         }
 
         public SelectFieldState(DialogContext dc)
         {
             super(dc);
         }
 
         public DialogFieldValue constructValueInstance()
         {
             return new SelectFieldValue();
         }
     }
 
     public class SelectFieldPopup extends DialogFieldPopup
     {
         private String lvsSessionAttrName;
 
         public SelectFieldPopup()
         {
         }
 
         public void prepareForPopup(DialogContext dc)
         {
             ((HttpServletRequest) dc.getRequest()).getSession(true).setAttribute(lvsSessionAttrName, choices);
         }
 
         public void initialize()
         {
             setAction(new StaticValueSource("popup-url:cmd=lvs,reference;session:LVSPOPUP_"+ getQualifiedName()+";yes"));
             setFill(getQualifiedName() + "," + getQualifiedName() + "_adjacent");
             lvsSessionAttrName = "LVSPOPUP_"+ getQualifiedName();
         }
     }
 
     private ValueSource choices;
     private Style style = new Style(Style.COMBO);
     private int multiDualWidth = 125;
     private ValueSource multiDualCaptionLeft = new StaticValueSource("Available");
     private ValueSource multiDualCaptionRight = new StaticValueSource("Selected");
     private String controlSeparator = "<br>";
 
     public SelectField()
     {
         super();
         setSize(8);
     }
 
     public DialogField.Flags createFlags()
     {
         return new Flags();
     }
 
     public DialogField.State constructStateInstance(DialogContext dc)
     {
         return new SelectFieldState(dc);
     }
 
     public Class getStateClass()
     {
         return SelectFieldState.class;
     }
 
     public Class getStateValueClass()
     {
         return SelectFieldState.SelectFieldValue.class;
     }
 
     public ValueSource getChoices()
     {
         return choices;
     }
 
     public void setChoices(ValueSource choices)
     {
         this.choices = choices;
     }
 
     public boolean isMulti()
     {
         return style.isMulti();
     }
 
     public Style getStyle()
     {
         return style;
     }
 
     public void setStyle(Style style)
     {
         this.style = style;
 
         switch(style.getValueIndex())
         {
             case Style.POPUP:
                 Flags flags = (Flags) getFlags();
                 if(! flags.flagIsSet(Flags.CREATE_ADJACENT_AREA | Flags.CREATE_ADJACENT_AREA_HIDDEN))
                     flags.setFlag(Flags.CREATE_ADJACENT_AREA);
                 addPopup(new SelectFieldPopup());
                 break;
 
             case Style.TEXT:
                 getFlags().setFlag(Flags.SEND_CHOICES_TO_CLIENT);
                 break;
         }
     }
 
     public ValueSource getMultiDualCaptionLeft()
     {
         return multiDualCaptionLeft;
     }
 
     public void setMultiDualCaptionLeft(ValueSource multiDualCaptionLeft)
     {
         this.multiDualCaptionLeft = multiDualCaptionLeft;
     }
 
     public ValueSource getMultiDualCaptionRight()
     {
         return multiDualCaptionRight;
     }
 
     public void setMultiDualCaptionRight(ValueSource multiDualCaptionRight)
     {
         this.multiDualCaptionRight = multiDualCaptionRight;
     }
 
     public int getMultiDualWidth()
     {
         return multiDualWidth;
     }
 
     public void setMultiDualWidth(int multiDualWidth)
     {
         this.multiDualWidth = multiDualWidth;
     }
 
     public String getControlSeparator()
     {
         return controlSeparator;
     }
 
     public void setControlSeparator(String controlSeparator)
     {
         this.controlSeparator = controlSeparator;
     }
 
     /**
      * Sets the qualified name of the dialog
      *
      * @param newName new qualified name
      */
     public void setQualifiedName(String newName)
     {
         super.setQualifiedName(newName);
 
         // if this select field is a child of another field and of popup style, then the qualified name change needs to
         // be passed to popup
         if (style.getValueIndex() == Style.POPUP)
         {
             SelectFieldPopup popup = (SelectFieldPopup) getPopup();
             popup.initialize();
         }
     }
 
     public void populateValue(DialogContext dc, int formatType)
     {
         SelectFieldState state = (SelectFieldState) dc.getFieldStates().getState(this);
         SelectFieldState.SelectFieldValue sfValue = (SelectFieldState.SelectFieldValue) state.getValue();
 
         if(isMulti())
         {
             // multi select list
             String[] values = sfValue.getTextValues();
             if(values == null || values.length == 0)
                 values = dc.getRequest().getParameterValues(getHtmlFormControlId());
 
             // initial display of the dialog
             if(dc.getRunSequence() == 1)
             {
                 // if no request parameter is passed in and the XML defined default value exists
                 ValueSource defaultValue = getDefault();
                 if((values != null && values.length == 0 && defaultValue != ValueSource.NULL_VALUE_SOURCE) ||
                         (values == null && defaultValue != ValueSource.NULL_VALUE_SOURCE))
                     sfValue.setValue(defaultValue.getValue(dc).getListValue());
             }
             else
                 sfValue.setValue(values);
         }
         else
         {
             super.populateValue(dc, formatType);
             if(style.getValueIndex() == Style.POPUP && choices != null)
             {
                 ((SelectFieldPopup) getPopup()).prepareForPopup(dc);
                 PresentationValue.Items.Item selectedItem = choices.getPresentationItem(dc, sfValue.getTextValue());
                 if(selectedItem != null)
                     state.setAdjacentAreaValue(selectedItem.getValue());
             }
         }
     }
 
     public String getMultiDualControlHtml(DialogContext dc, PresentationValue.Items choices)
     {
         int size = getSize();
         String dialogName = dc.getDialog().getHtmlFormName();
 
         String width = multiDualWidth + " pt";
         String sorted = getFlags().flagIsSet(Flags.SORT_CHOICES) ? "true" : "false";
         String id = getHtmlFormControlId();
         String name = getQualifiedName();
         String fieldAreaFontAttrs = dc.getSkin().getControlAreaFontAttrs();
 
         StringBuffer selectOptions = new StringBuffer();
         StringBuffer selectOptionsSelected = new StringBuffer();
 
         for(int i = 0; i < choices.size(); i++)
         {
             PresentationValue.Items.Item item = choices.getItem(i);
             if((item.getFlags() & PRESENTATIONITEMFLAG_IS_SELECTED) != 0)
                 selectOptionsSelected.append("<option value=\"" + item.getValue() + "\">" + item.getCaption() + "</option>\n");
             else
                 selectOptions.append("<option value=\"" + item.getValue() + "\">" + item.getCaption() + "</option>\n");
         }
 
         return
                 "<TABLE CELLSPACING=0 CELLPADDING=1 ALIGN=left BORDER=0>\n" +
                 "<TR>\n" +
                 "<TD ALIGN=left><FONT " + fieldAreaFontAttrs + ">" + multiDualCaptionLeft.getTextValue(dc) + "</FONT></TD><TD></TD>\n" +
                 "<TD ALIGN=left><FONT " + fieldAreaFontAttrs + ">" + multiDualCaptionRight.getTextValue(dc) + "</FONT></TD>\n" +
                 "</TR>\n" +
                 "<TR>\n" +
                 "<TD ALIGN=left VALIGN=top>\n" +
                 "	<SELECT class='dialog_control' ondblclick=\"MoveSelectItems('" + dialogName + "', '" + name + "_From', '" + id + "', " + sorted + ")\" NAME='" + name + "_From' SIZE='" + size + "' MULTIPLE STYLE=\"width: " + width + "\">\n" +
                 "	" + selectOptions + "\n" +
                 "	</SELECT>\n" +
                 "</TD>\n" +
                 "<TD ALIGN=center VALIGN=middle>\n" +
                 "	&nbsp;<INPUT TYPE=button NAME=\"" + name + "_addBtn\" onClick=\"MoveSelectItems('" + dialogName + "', '" + name + "_From', '" + id + "', " + sorted + ")\" VALUE=\" > \">&nbsp;<BR CLEAR=both>\n" +
                 "	&nbsp;<INPUT TYPE=button NAME=\"" + name + "_removeBtn\" onClick=\"MoveSelectItems('" + dialogName + "', '" + id + "', '" + name + "_From', " + sorted + ")\" VALUE=\" < \">&nbsp;\n" +
                 "</TD>\n" +
                 "<TD ALIGN=left VALIGN=top>\n" +
                 "	<SELECT class='dialog_control' ondblclick=\"MoveSelectItems('" + dialogName + "', '" + id + "', '" + name + "_From', " + sorted + ")\" NAME='" + id + "' SIZE='" + size + "' MULTIPLE STYLE=\"width: " + width + "\" " + dc.getSkin().getDefaultControlAttrs() + ">\n" +
                 "	" + selectOptionsSelected + "\n" +
                 "	</SELECT>\n" +
                 "</TD>\n" +
                 "</TR>\n" +
                 "</TABLE>";
     }
 
     public void renderPopupControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         // as a popup, we're a simple text field so just use it's rendering method
         super.renderControlHtml(writer, dc);
     }
 
     public String getHiddenControlHtml(DialogContext dc, PresentationValue.Items choices, boolean showCaptions)
     {
         String id = getHtmlFormControlId();
         StringBuffer html = new StringBuffer();
 
         if(showCaptions)
         {
             for(int i = 0; i < choices.size(); i++)
             {
                 PresentationValue.Items.Item item = choices.getItem(i);
                 if((item.getFlags() & PRESENTATIONITEMFLAG_IS_SELECTED) != 0)
                 {
                     if(html.length() > 0)
                         html.append("<br>");
                     html.append("<input type='hidden' name='" + id + "' value=\"" + item.getValue() + "\"><span id='" + getQualifiedName() + "'>" + item.getCaption() + "</span>");
                 }
             }
         }
         else
         {
             for(int i = 0; i < choices.size(); i++)
             {
                 PresentationValue.Items.Item item = choices.getItem(i);
                 if((item.getFlags() & PRESENTATIONITEMFLAG_IS_SELECTED) != 0)
                     html.append("<input type='hidden' name='" + id + "' value=\"" + item.getValue() + "\">");
             }
         }
 
         return html.toString();
     }
 
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         // we do this first because popups don't want to pull in all the data at once like other select styles do
         final int styleValueIndex = style.getValueIndex();
         if(styleValueIndex == Style.POPUP || styleValueIndex == Style.TEXT)
         {
             renderPopupControlHtml(writer, dc);
             return;
         }
 
         SelectFieldState state = (SelectFieldState) dc.getFieldStates().getState(this);
 
         PresentationValue pValue;
         if(choices != null)
        {
            try
            {
                pValue = choices.getPresentationValue(dc);
            }
            catch (Exception e)
            {
                // an exception occurred while trying to construct the choices so create a dummy field with a warning message
                pValue = new PresentationValue();
                PresentationValue.Items choices = pValue.createItems();
                choices.addItem(e.getMessage());
            }
        }
         else
         {
             pValue = new PresentationValue();
             pValue.createItems();
         }
 
         PresentationValue.Items choices = pValue.getItems();
         SelectFieldState.SelectFieldValue sfValue = (SelectFieldState.SelectFieldValue) state.getValue();
         sfValue.calcSelections(choices);
 
         if(isInputHidden(dc))
         {
             writer.write(getHiddenControlHtml(dc, choices, false));
             return;
         }
 
         if(isReadOnly(dc))
         {
             writer.write(getHiddenControlHtml(dc, choices, true));
             return;
         }
 
         boolean readOnly = isReadOnly(dc);
         String id = getHtmlFormControlId();
         String defaultControlAttrs = dc.getSkin().getDefaultControlAttrs();
 
         StringBuffer options = new StringBuffer();
         int itemIndex = 0;
 
         switch(styleValueIndex)
         {
             case Style.RADIO:
                 for(int i = 0; i < choices.size(); i++)
                 {
                     PresentationValue.Items.Item choice = choices.getItem(i);
                     boolean selected = (choice.getFlags() & PRESENTATIONITEMFLAG_IS_SELECTED) != 0;
                     if(options.length() > 0)
                         options.append(controlSeparator);
                     options.append("<nobr><input type='radio' name='" + id + "' id='" + id + itemIndex + "' value=\"" + choice.getValue() + "\" " + (selected ? "checked " : "") + defaultControlAttrs + "> <label for='" + id + itemIndex + "'>" + choice.getCaption() + "</label></nobr>");
                 }
                 writer.write(options.toString());
                 return;
 
             case Style.MULTICHECK:
                 for(int i = 0; i < choices.size(); i++)
                 {
                     PresentationValue.Items.Item choice = choices.getItem(i);
                     boolean selected = (choice.getFlags() & PRESENTATIONITEMFLAG_IS_SELECTED) != 0;
                     if(options.length() > 0)
                         options.append(controlSeparator);
                     options.append("<nobr><input type='checkbox' name='" + id + "' id='" + id + itemIndex + "' value=\"" + choice.getValue() + "\" " + (selected ? "checked " : "") + defaultControlAttrs + "> <label for='" + id + itemIndex + "'>" + choice.getCaption() + "</label></nobr>");
                 }
                 writer.write(options.toString());
                 return;
 
             case Style.COMBO:
             case Style.LIST:
             case Style.MULTILIST:
                 if(readOnly)
                 {
                     for(int i = 0; i < choices.size(); i++)
                     {
                         PresentationValue.Items.Item choice = choices.getItem(i);
                         boolean selected = (choice.getFlags() & PRESENTATIONITEMFLAG_IS_SELECTED) != 0;
                         if(selected)
                         {
                             if(options.length() > 0)
                                 options.append(", ");
                             options.append("<input type='hidden' name='" + id + "' value=\"" + choice.getValue() + "\">");
                             options.append(choice.getCaption());
                         }
                     }
                     writer.write(options.toString());
                     return;
                 }
                 else
                 {
                     boolean prependBlank = false;
                     boolean appendBlank = false;
 
                     if(styleValueIndex == Style.COMBO || styleValueIndex == Style.LIST)
                     {
                         prependBlank = getFlags().flagIsSet(Flags.PREPEND_BLANK);
                         appendBlank = getFlags().flagIsSet(Flags.APPEND_BLANK);
                     }
 
                     if(prependBlank)
                         options.append("    <option value=''></option>\n");
 
                     for(int i = 0; i < choices.size(); i++)
                     {
                         PresentationValue.Items.Item choice = choices.getItem(i);
                         boolean selected = (choice.getFlags() & PRESENTATIONITEMFLAG_IS_SELECTED) != 0;
                         options.append("    <option value=\"" + choice.getValue() + "\" " + (selected ? "selected" : "") + ">" + choice.getCaption() + "</option>\n");
                     }
 
                     if(appendBlank)
                         options.append("    <option value=''></option>\n");
 
                     switch(styleValueIndex)
                     {
                         case Style.COMBO:
                             writer.write("<select class=\"" + dc.getSkin().getControlAreaStyleClass() + "\" name='" + id + "' " + defaultControlAttrs +
                                     (isInputHidden(dc) ? " style=\"display:none;\"" : "") +
                                     ">\n" + options + "</select>\n");
                             break;
 
                         case Style.LIST:
                             writer.write("<select class=\"" + dc.getSkin().getControlAreaStyleClass() + "\" name='" + id + "' size='" + getSize() + "' " + defaultControlAttrs +
                                     (isInputHidden(dc) ? " style=\"display:none;\"" : "") +
                                     ">\n" + options + "</select>\n");
                             break;
 
                         case Style.MULTILIST:
                             writer.write("<select class=\"" + dc.getSkin().getControlAreaStyleClass() + "\" name='" + id + "' size='" + getSize() + "' multiple='yes' " + defaultControlAttrs +
                                     (isInputHidden(dc) ? " style=\"display:none;\"" : "") +
                                     ">\n" + options + "</select>\n");
                             break;
                     }
 
                     return;
                 }
 
             case Style.MULTIDUAL:
                 writer.write(getMultiDualControlHtml(dc, choices));
                 break;
 
             default:
                 writer.write("Unknown style " + style);
         }
     }
 
     /**
      * Produce select field specific Javascript definitions
      * @param dc
      * @return String
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer buf = new StringBuffer(super.getCustomJavaScriptDefn(dc));
         buf.append("field.style = " + getStyle().getValueIndex() + ";\n");
 
         if (dc.getFieldStates().getState(this).getStateFlags().flagIsSet(Flags.SEND_CHOICES_TO_CLIENT))
         {
             PresentationValue pValue = this.choices.getPresentationValue(dc);
             PresentationValue.Items choices = pValue.getItems();
 
             StringBuffer captionBuf = new StringBuffer();
             StringBuffer valueBuf = new StringBuffer();
             captionBuf.append("field.choicesCaption = new Array(");
             valueBuf.append("field.choicesValue = new Array(");
 
             for(int i = 0; i < choices.size(); i++)
             {
                 PresentationValue.Items.Item choice = choices.getItem(i);
                 captionBuf.append((i != 0 ? ", \"" : "\"") + choice.getCaption() + "\"");
                 valueBuf.append((i != 0 ? ", \"" : "\"") + choice.getValue() + "\"");
             }
             captionBuf.append(");\n");
             valueBuf.append(");\n");
             buf.append(captionBuf.toString() + valueBuf.toString());
         }
         return buf.toString();
     }
 }
