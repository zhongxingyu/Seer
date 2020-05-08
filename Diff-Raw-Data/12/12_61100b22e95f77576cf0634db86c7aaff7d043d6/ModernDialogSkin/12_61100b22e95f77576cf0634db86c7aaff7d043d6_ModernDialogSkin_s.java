 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 
 package com.netspective.sparx.theme.basic;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.netspective.commons.validate.ValidationContext;
 import com.netspective.sparx.form.Dialog;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogDirector;
 import com.netspective.sparx.form.DialogFlags;
 import com.netspective.sparx.form.DialogIncludeJavascriptFile;
 import com.netspective.sparx.form.DialogPerspectives;
 import com.netspective.sparx.form.DialogSkin;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldFlags;
 import com.netspective.sparx.form.field.DialogFieldPopup;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.form.field.type.CompositeField;
 import com.netspective.sparx.form.field.type.GridField;
 import com.netspective.sparx.form.field.type.SectionField;
 import com.netspective.sparx.form.field.type.SeparatorField;
 import com.netspective.sparx.panel.HtmlPanel;
 import com.netspective.sparx.theme.Theme;
 
 /**
  * @author Aye Thu
 * @version $Id: ModernDialogSkin.java,v 1.4 2004-08-15 02:27:29 shahid.shah Exp $
  */
 public class ModernDialogSkin extends BasicHtmlPanelSkin implements DialogSkin
 {
     public static final String SKIN_NAME = "modern";
 
     public static final String FIELDROW_PREFIX = "_dfr.";
     public static final String GRIDHEADROW_PREFIX = "_dghr.";
     public static final String GRIDFIELDROW_PREFIX = "_dgfr.";
 
     /* flag indicating whether or not to display all the field errors at the top of the dialog */
     private boolean summarizeErrors;
 
     private String dialogTableStyleClass = null;
 
     /* CSS class name for the block representing the field (this includes the caption and the input) */
     private String fieldBlockStyleClass = null;
     /* CSS class name for the block representing the field (this includes the caption and the input) that has an error */
     private String fieldBlockWithErrorStyleClass = null;
     /* CSS class name for the hint of a field */
     private String fieldHintStyleClass = null;
     /* CSS class name for the hint of a field */
     private String fieldHiddenHintStyleClass = null;
     /* CSS class name foe the hint of a field that is hidden until the focus is on the field */
     private String fieldHintHiddenStyleClass = null;
     /* CSS style name for the error message element of a field */
     private String fieldErrorStyleClass = null;
     /* html to be included in the input part of a field */
     private String fieldControlAttrs;
     /* CSS style name for the input part of a field */
     private String fieldControlAreaStyleClass;
     /* CSS style name for the input part of a required field */
     private String fieldControlAreaRequiredStyleClass;
     /* CSS style name for the input part of read-only field */
     private String fieldControlAreaReadonlyStyleClass;
     /* CSS style name for the caption of a field */
     private String fieldCaptionStyleClass = null;
     /* CSS style name for the caption of a required field */
     private String fieldCaptionRequiredStyleClass = null;
     /* CSS style name for the separator field's block */
     private String separatorBlockStyleClass = null;
     /* CSS style name for the banner of a separator field */
     private String separatorBannerStyleClass = null;
 
     /* CSS style name for the dialog director block element */
     private String directorStyleClass = null;
     /* CSS style name for the error message of the dialog */
     private String errorStyleClass = null;
     /* CSS style name for the error message heading element of the dialog */
     private String errorHeadingStyleClass = null;
 
     /* The string to display as the error message heading */
     private String errorMessageHeadingText = null;
     /* CSS style name for the error description link displayed */
     private String errorMessageLinkStyleClass = null;
     /* CSS style name for the error message itself */
     private String errorMessageStyleClass = null;
     /* CSS style name for the grid's row caption block element */
     private String gridCaptionBlockStyleClass = null;
     /* CSS style name for the grid's row data block element */
     private String gridRowDataBlockStyleClass = null;
     /* CSS style name for the grid's caption string */
     private String gridCaptionStyleClass = null;
     /* CSS style name for the grid's required caption string */
     private String gridCaptionRequiredStyleClass = null;
     /* CSS style name for the grid's row caption block element */
     private String gridRowCaptionBlockStyleClass = null;
     /* CSS style name for the grid's row caption string */
     private String gridRowCaptionStyleClass = null;
     /* CSS style name for the grid table */
     private String gridTableStyleClass = null;
 
     /* custom string to append to every caption of the dialog */
     private String captionSuffix;
 
     protected String includePreScripts;
     protected String includePostScripts;
     //protected String includePreStyleSheets;
     //protected String includePostStyleSheets;
     protected String customStyleSheets;
     protected String prependPreScript;
     protected String prependPostScript;
     protected String appendPreScript;
     protected String appendPostScript;
     protected Map includePostScriptsMap;
     protected Map includePreScriptsMap;
 
     public ModernDialogSkin()
     {
         super();
         setName(SKIN_NAME);
         // these are the default settings
         setPanelClassNamePrefix("panel-output");
         setPanelResourcesPrefix("panel/output");
         initializeStyles();
     }
 
     public ModernDialogSkin(Theme theme, String s, String s1, String s2, boolean b)
     {
         super(theme, s, s1, s2, b);
         initializeStyles();
     }
 
     /**
      * Initialize the CSS style classes for the dialog skin
      */
     protected void initializeStyles()
     {
         dialogTableStyleClass = "dialog";
         fieldCaptionStyleClass = "dialog-field-caption";
         fieldCaptionRequiredStyleClass = "dialog-field-caption-required";
         fieldBlockStyleClass = "dialog-field-block";
         fieldBlockWithErrorStyleClass = "dialog-field-block-errors";
         fieldHintStyleClass = "dialog-field-hint";
         fieldHiddenHintStyleClass = "dialog-field-hint-hidden";
         fieldErrorStyleClass = "dialog-field-errors";
         fieldControlAreaStyleClass = "dialog-field-input";
         fieldControlAreaRequiredStyleClass = "dialog-field-input-required";
         fieldControlAreaReadonlyStyleClass = "dialog-field-input-readonly";
         fieldControlAttrs = " onfocus='return controlOnFocus(this, event)' onchange='controlOnChange(this, event)' " +
                             "onblur='controlOnBlur(this, event)' onkeypress='controlOnKeypress(this, event)' " +
                             "onclick='controlOnClick(this, event) '";
         separatorBlockStyleClass = "dialog-field-separator-block";
         separatorBannerStyleClass = "dialog-field-separator-banner";
         directorStyleClass = "dialog-buttons-block";
         errorMessageStyleClass = "";
         errorMessageHeadingText = "Please review the following:";
         errorMessageLinkStyleClass = "";
         gridCaptionBlockStyleClass = "dialog-field-grid-column-block";
         gridCaptionStyleClass = "dialog-field-grid-column-caption";
         gridCaptionRequiredStyleClass = "dialog-field-grid-column-caption-required";
         gridRowDataBlockStyleClass = "dialog-field-grid-data-block";
         gridRowCaptionBlockStyleClass = "dialog-field-grid-row-caption-block";
         gridRowCaptionStyleClass = "dialog-field-grid-row-caption";
         gridTableStyleClass = "dialog-field-grid-table";
         captionSuffix = "";
     }
 
     public String getDialogTableStyleClass()
     {
         return dialogTableStyleClass;
     }
 
     public void setDialogTableStyleClass(String dialogTableStyleClass)
     {
         this.dialogTableStyleClass = dialogTableStyleClass;
     }
 
     /**
      * Gets the CSS class name for the grid block table element
      */
     public String getGridTableStyleClass()
     {
         return gridTableStyleClass;
     }
 
     /**
      * Sets the CSS class name for the grid block table element
      */
     public void setGridTableStyleClass(String gridTableStyleClass)
     {
         this.gridTableStyleClass = gridTableStyleClass;
     }
 
     /**
      * Gets the CSS class name for the separator's banner
      */
     public String getSeparatorBannerStyleClass()
     {
         return separatorBannerStyleClass;
     }
 
     /**
      * Sets the CSS class name for the separator's banner
      */
     public void setSeparatorBannerStyleClass(String separatorBannerStyleClass)
     {
         this.separatorBannerStyleClass = separatorBannerStyleClass;
     }
 
     /**
      * Gets the CSS class name for the separator field block
      */
     public String getSeparatorBlockStyleClass()
     {
         return separatorBlockStyleClass;
     }
 
     /**
      * Sets the CSS class name for the separator field block
      */
     public void setSeparatorBlockStyleClass(String separatorBlockStyleClass)
     {
         this.separatorBlockStyleClass = separatorBlockStyleClass;
     }
 
     /**
      * Gets the CSS class name for a field's caption
      *
      * @return CSS class name
      */
     public String getCaptionStyleClass()
     {
         return fieldCaptionStyleClass;
     }
 
     /**
      * Sets the CSS class name for a field's caption
      */
     public void setCaptionStyleClass(String fieldCaptionStyleClass)
     {
         this.fieldCaptionStyleClass = fieldCaptionStyleClass;
     }
 
     /**
      * Gets the CSS class name for a required field's caption
      */
     public String getCaptionRequiredStyleClass()
     {
         return fieldCaptionRequiredStyleClass;
     }
 
     /**
      * Sets the CSS class name for a field's caption
      */
     public void setCaptionRequiredStyleClass(String fieldCaptionRequiredStyleClass)
     {
         this.fieldCaptionRequiredStyleClass = fieldCaptionRequiredStyleClass;
     }
 
     /**
      * Gets the custom string to append to every caption of the dialog
      */
     public String getCaptionSuffix()
     {
         return captionSuffix;
     }
 
     /**
      * Sets the custom string to append to every caption of the dialog
      */
     public void setCaptionSuffix(String value)
     {
         captionSuffix = value;
     }
 
     /**
      * Gets the CSS class name for the error message text
      */
     public String getErrorMessageStyleClass()
     {
         return errorMessageStyleClass;
     }
 
     /**
      * Sets the CSS class name for the error message text
      */
     public void setErrorMessageStyleClass(String errorMessageStyleClass)
     {
         this.errorMessageStyleClass = errorMessageStyleClass;
     }
 
     /**
      * Gets the CSS class name for the grid row's caption string
      */
     public String getGridRowCaptionStyleClass()
     {
         return gridRowCaptionStyleClass;
     }
 
     /**
      * Sets the CSS class name for the grid row's caption string
      */
     public void setGridRowCaptionStyleClass(String gridRowCaptionStyleClass)
     {
         this.gridRowCaptionStyleClass = gridRowCaptionStyleClass;
     }
 
     /**
      * Gets the CSS class name for the grid row's caption block element
      */
     public String getGridRowCaptionBlockStyleClass()
     {
         return gridRowCaptionBlockStyleClass;
     }
 
     /**
      * Sets the CSS class name for the grid row's caption block element
      */
     public void setGridRowCaptionBlockStyleClass(String gridRowCaptionBlockStyleClass)
     {
         this.gridRowCaptionBlockStyleClass = gridRowCaptionBlockStyleClass;
     }
 
     /**
      * Gets the CSS class name for the grid row's data block element
      */
     public String getGridRowDataBlockStyleClass()
     {
         return gridRowDataBlockStyleClass;
     }
 
     /**
      * Sets the CSS class name for the grid row's data block element
      */
     public void setGridRowDataBlockStyleClass(String gridRowDataBlockStyleClass)
     {
         this.gridRowDataBlockStyleClass = gridRowDataBlockStyleClass;
     }
 
     /**
      * Gets the CSS class name for a grid's required caption
      */
     public String getGridCaptionRequiredStyleClass()
     {
         return gridCaptionRequiredStyleClass;
     }
 
     /**
      * Sets the CSS class name for a grid's required caption
      */
     public void setGridCaptionRequiredStyleClass(String gridCaptionRequiredStyleClass)
     {
         this.gridCaptionRequiredStyleClass = gridCaptionRequiredStyleClass;
     }
 
     /**
      * Gets the CSS class name for the block element that contains the grid row caption
      */
     public String getGridCaptionBlockStyleClass()
     {
         return gridCaptionBlockStyleClass;
     }
 
     /**
      * Sets the CSS class name for the block element that contains the grid row caption
      * (e.g TD or a DIV)
      */
     public void setGridCaptionBlockStyleClass(String gridCaptionBlockStyleClass)
     {
         this.gridCaptionBlockStyleClass = gridCaptionBlockStyleClass;
     }
 
     /**
      * Gets the CSS class name for the caption of the a grid row
      */
     public String getGridCaptionStyleClass()
     {
         return gridCaptionStyleClass;
     }
 
     /**
      * Sets the CSS class name for the caption of the a grid row
      */
     public void setGridCaptionStyleClass(String gridCaptionStyleClass)
     {
         this.gridCaptionStyleClass = gridCaptionStyleClass;
     }
 
     /**
      * Gets the CSS class name for the dialog's error message list item link
      */
     public String getErrorMessageLinkStyleClass()
     {
         return errorMessageLinkStyleClass;
     }
 
     /**
      * Sets the CSS class name for the dialog's error message list item link
      */
     public void setErrorMessageLinkStyleClass(String errorMessageLinkStyleClass)
     {
         this.errorMessageLinkStyleClass = errorMessageLinkStyleClass;
     }
 
     /**
      * Gets the string to display as the error message heading of the dialog
      */
     public String getErrorMessageHeadingText()
     {
         return errorMessageHeadingText;
     }
 
     /**
      * Sets the string to display as the error message heading of the dialog
      */
     public void setErrorMessageHeadingText(String errorMessageHeadingText)
     {
         this.errorMessageHeadingText = errorMessageHeadingText;
     }
 
     /**
      * Gets the CSS class name for the dialog's error message heading
      */
     public String getErrorHeadingStyleClass()
     {
         return errorHeadingStyleClass;
     }
 
     /**
      * Sets the CSS class name for the dialog's error message heading
      */
     public void setErrorHeadingStyleClass(String errorHeadingStyleClass)
     {
         this.errorHeadingStyleClass = errorHeadingStyleClass;
     }
 
     /**
      * Gets the CSS class name for the dialog's error message list item
      */
     public String getErrorStyleClass()
     {
         return errorStyleClass;
     }
 
     /**
      * Sets the CSS class name for the dialog's error message list item
      */
     public void setErrorStyleClass(String errorStyleClass)
     {
         this.errorStyleClass = errorStyleClass;
     }
 
     /**
      * Gets the CSS class name for the dialog director's button block
      */
     public String getDirectorStyleClass()
     {
         return directorStyleClass;
     }
 
     /**
      * Sets the CSS class name for the dialog director's button block
      */
     public void setDirectorStyleClass(String directorStyeClass)
     {
         this.directorStyleClass = directorStyeClass;
     }
 
     /**
      * Gets the CSS class name for the field block element. The block is handled differently because there
      * is an error associated with the field.
      */
     public String getFieldBlockWithErrorStyleClass()
     {
         return fieldBlockWithErrorStyleClass;
     }
 
     /**
      * Sets the CSS class name for the field block element. The block is handled differently because there
      * is an error associated with the field.
      */
     public void setFieldBlockWithErrorStyleClass(String fieldBlockWithErrorStyleClass)
     {
         this.fieldBlockWithErrorStyleClass = fieldBlockWithErrorStyleClass;
     }
 
     /**
      * Gets the CSS class name for setting the look and feel of the field's error message
      */
     public String getFieldErrorStyleClass()
     {
         return fieldErrorStyleClass;
     }
 
     /**
      * Sets the CSS class name for setting the look and feel of the field's error message
      */
     public void setFieldErrorStyleClass(String fieldErrorStyleClass)
     {
         this.fieldErrorStyleClass = fieldErrorStyleClass;
     }
 
     /**
      * Gets the CSS class name for setting the look and feel of the field's hint message
      */
     public String getFieldHintStyleClass()
     {
         return fieldHintStyleClass;
     }
 
     /**
      * Sets the CSS class name for setting the look and feel of the field's hint message
      */
     public void setFieldHintStyleClass(String fieldHintStyleClass)
     {
         this.fieldHintStyleClass = fieldHintStyleClass;
     }
 
     public String getFieldHiddenHintStyleClass()
     {
         return fieldHiddenHintStyleClass;
     }
 
     public void setFieldHiddenHintStyleClass(String fieldHiddenHintStyleClass)
     {
         this.fieldHiddenHintStyleClass = fieldHiddenHintStyleClass;
     }
 
     /**
      * Gets the CSS class name for the field block element
      *
      * @return CSS Class name
      */
     public String getFieldBlockStyleClass()
     {
         return fieldBlockStyleClass;
     }
 
     /**
      * Sets the CSS class name for the field block element
      *
      * @param fieldBlockStyleClass CSS class name
      */
     public void setFieldBlockStyleClass(String fieldBlockStyleClass)
     {
         this.fieldBlockStyleClass = fieldBlockStyleClass;
     }
 
     /**
      * @param writer
      * @param dc
      * @param parentField
      *
      * @throws java.io.IOException
      */
     public void renderCompositeControlsHtml(Writer writer, DialogContext dc, DialogField parentField) throws IOException
     {
         DialogFields children = parentField.getChildren();
         writer.write("<table id=\"" + parentField.getHtmlFormControlId() + "\" class='dialog-fields-no-arrow'><tr>");
         // loop through all the children field
         for(int i = 0; i < children.size(); i++)
         {
             DialogField field = children.get(i);
             if(field.isAvailable(dc))
             {
                 if(field.isInputHidden(dc))
                 {
                     // render the hidden field
                     field.renderControlHtml(writer, dc);
                 }
                 else
                 {
                     // render the field
                     DialogFieldFlags flags = field.getFlags();
                     //if(flags.flagIsSet(DialogFieldFlags.COLUMN_BREAK_BEFORE))
                     //    writer.write("<br/>");
 
                     writer.write("<td>");
                     //check to see if the caption of the child field should be shown
                     boolean showCaption = field.showCaptionAsChild();
                     if(showCaption)
                     {
                         String caption = field.getCaption().getTextValue(dc);
                         if(caption != DialogField.CUSTOM_CAPTION && caption != null)
                         {
                             String captionHtml = generateFieldCaption(field, dc);
                             writer.write("<nobr>" + captionHtml);
                             //writer.write("<br/>");
                         }
                     }
                     //field.renderControlHtml(writer, dc);
                     String hintHtml = generateFieldHint(field, dc);
                     String controlHtml = generateFieldControl(field, dc);
                     writer.write(controlHtml + (hintHtml != null ? "<br/>" + hintHtml : ""));
                     writer.write("&nbsp;");
                     if(showCaption) writer.write("</nobr>");
                     writer.write("</td>");
                     //if(flags.flagIsSet(DialogFieldFlags.COLUMN_BREAK_AFTER))
                     //    writer.write("<br/>");
                 }
             }
         }
         writer.write("</tr></table>");
     }
 
     /**
      * Render the Section field's contents
      */
     public void renderSectionControlsHtml(Writer writer, DialogContext dc, DialogField parentField, List fieldErrorMsgs) throws IOException
     {
         DialogFields children = parentField.getChildren();
         StringWriter hiddenWriter = new StringWriter();
         writer.write("<fieldset>\n");
         if(parentField.getCaption() != null && parentField.getCaption().hasValue(dc))
             writer.write("<legend>" + parentField.getCaption().getTextValue(dc) + "</legend>\n");
         writer.write("<table class=\"dialog-section-field\">\n");
         int displayedColCount = 0;
         for(int i = 0; i < children.size(); i++)
         {
             DialogField field = children.get(i);
             if(field.isAvailable(dc))
             {
                 if(field.isInputHidden(dc))
                 {
                     // render the hidden field
                     field.renderControlHtml(hiddenWriter, dc);
                 }
                 else
                 {
                     //if (displayedColCount == 0)
                     //    writer.write("<tr>\n");
                     // render the field
                     String captionHtml = generateFieldCaption(field, dc);
                     String controlHtml = generateFieldControl(field, dc);
                     String messagesHtml = generateFieldErrorMessage(field, dc, fieldErrorMsgs);
                     String hintHtml = generateFieldHint(field, dc);
                     //writer.write("<td>");
                     //writer.write(captionHtml + "<br/>" + controlHtml);
                     //writer.write(hintHtml != null ? "<br/>"+  hintHtml : "");
 
                     writer.write(generateFieldBlock(dc, field, captionHtml, controlHtml, hintHtml, messagesHtml));
 
                     // now append the error message html on the next row
                     //if (errorHtml != null && errorHtml.length() > 0)
                     //    fieldsHtml.append("<tr><td><span class=\"dialog-fields-errors\">&nbsp;&nbsp;&nbsp;"+  errorHtml + "</span></td></tr>\n");
 
                     //writer.write("</td>\n");
                     displayedColCount++;
 
 
                     //if (displayedColCount == ((SectionField)parentField).getDisplayColumns())
                     //{
                     //    writer.write("</tr>\n");
                     //    displayedColCount = 0;
                     //}
                 }
             }
         }
         //if (displayedColCount != 0)
         //{
         //    writer.write("<td colspan=\"" + (4- displayedColCount) + "\">&nbsp;</td></tr>");
         //}
         writer.write("</table>\n");
         writer.write("</fieldset>\n");
         writer.write(hiddenWriter.getBuffer().toString());
     }
 
     /**
      * Generates the HTML for an input column of the grid's row
      *
      * @param dc    current dialog context
      * @param field row's child column field
      */
     public String generateGridRowMemberColumn(DialogContext dc, DialogField field) throws IOException
     {
         StringBuffer html = new StringBuffer();
         html.append("<span " + getControlAreaStyleClass() + ">" + generateFieldControl(field, dc) + "</span>");
         // get the html for the field input
         String hintHtml = generateFieldHint(field, dc);
         if(hintHtml != null)
             html.append("<br/>" + hintHtml);
 
         return html.toString();
     }
 
     /**
      * Generates the ID for the head row of a grid
      */
     protected String generateGridHeadRowId(DialogField rowField)
     {
         return GRIDHEADROW_PREFIX + rowField.getQualifiedName();
     }
 
     /**
      * Generates the ID for a data row of a grid
      */
     protected String generateGridRowId(DialogField rowField)
     {
         return GRIDFIELDROW_PREFIX + rowField.getQualifiedName();
     }
 
     /**
      * @param dc
      * @param gridField
      * @param rowField
      * @param row
      *
      * @return
      *
      * @throws java.io.IOException
      */
     public String generateGridRowHtml(DialogContext dc, GridField gridField, DialogField rowField, int row) throws IOException
     {
         String rowAttr = " id='" + generateGridRowId(rowField) + "' ";
         StringBuffer rowHtml = new StringBuffer("\n<tr valign='top' " + rowAttr + ">");
         DialogFields rowChildren = rowField.getChildren();
 
         // get the row's name
         String rowCaption = rowField.getCaption().getTextValue(dc);
         if(rowCaption == null)
             rowCaption = "";
 
         if(row == 0)
         {
             // use the first row's captions as the column headers of the grid
             String hRowAttr = " id='" + generateGridHeadRowId(rowField) + "' ";
             StringBuffer headerHtml = new StringBuffer("\n<tr " + hRowAttr + ">");
 
             int fieldNum = 0;
             String[] fieldCaptions = gridField.getCaptions(dc);
             // save space in the header for the row captions
             headerHtml.append("<td>&nbsp;</td> ");
             // append the row caption to the first row
             rowHtml.append("<td" + (getGridRowCaptionBlockStyleClass() != null
                                     ? " class=\"" + getGridRowCaptionBlockStyleClass() + "\"" : "") + ">");
             rowHtml.append("<span class=\"" + getGridRowCaptionStyleClass() + "\">" + rowCaption + "</span>");
             rowHtml.append("</td>");
             for(int i = 0; i < rowChildren.size(); i++)
             {
                 DialogField columnField = rowChildren.get(i);
                 if(columnField.isAvailable(dc))
                 {
                     String caption = fieldNum < fieldCaptions.length
                                      ? fieldCaptions[fieldNum] : columnField.getCaption().getTextValue(dc);
 
                     headerHtml.append("<td" + (getGridCaptionBlockStyleClass() != null
                                                ? " class=\"" + getGridCaptionBlockStyleClass() + "\"" : "") + ">");
                     if(caption != null && caption != DialogField.CUSTOM_CAPTION)
                     {
                         if(columnField.isRequired(dc))
                             headerHtml.append("<span class=\"" + getGridCaptionRequiredStyleClass() + "\">");
                         else
                             headerHtml.append("<span class=\"" + getGridCaptionStyleClass() + "\">");
                         headerHtml.append(caption);
                         headerHtml.append("</span>");
                     }
                     headerHtml.append("</td>");
 
                     rowHtml.append("<td" + (getGridRowDataBlockStyleClass() != null
                                             ? " class=\"" + getGridRowDataBlockStyleClass() + "\"" : "") + ">");
                     rowHtml.append(generateGridRowMemberColumn(dc, columnField));
                     rowHtml.append("</td>");
                 }
                 fieldNum++;
             }
 
             headerHtml.append("</tr>");
             headerHtml.append(rowHtml);
             headerHtml.append("</tr>");
 
             return headerHtml.toString();
         }
         else
         {
             // append the row caption to the first row
             rowHtml.append("<td" + (getGridRowCaptionBlockStyleClass() != null
                                     ? " class=\"" + getGridRowCaptionBlockStyleClass() + "\"" : "") + ">");
             rowHtml.append("<span class=\"" + getGridRowCaptionStyleClass() + "\">" + rowCaption + "</span>");
             rowHtml.append("</td>");
 
             for(int i = 0; i < rowChildren.size(); i++)
             {
                 DialogField field = rowChildren.get(i);
                 if(field.isAvailable(dc))
                 {
                     rowHtml.append("<td" + (getGridRowDataBlockStyleClass() != null
                                             ? " class=\"" + getGridRowDataBlockStyleClass() + "\"" : "") + ">");
                     rowHtml.append(generateGridRowMemberColumn(dc, field));
                     rowHtml.append("</td>");
                 }
             }
             rowHtml.append("</tr>");
             return rowHtml.toString();
         }
     }
 
     /**
      * Writes the Grid field's html
      *
      * @param writer    writer object
      * @param dc        current dialog context
      * @param gridField the grid field
      */
     public void renderGridControlsHtml(Writer writer, DialogContext dc, GridField gridField) throws IOException
     {
         writer.write("\n<table class=\"" + getGridTableStyleClass() + "\">");
 
         DialogFields gridChildren = gridField.getChildren();
         int colsCount = 0;
         // loop through each row of the grid
         for(int row = 0; row < gridChildren.size(); row++)
         {
             DialogField rowField = gridChildren.get(row);
             if(colsCount == 0)
                 colsCount = rowField.getChildren().size();
 
             if(rowField.isAvailable(dc))
             {
                 StringBuffer messagesHtml = new StringBuffer();
                 boolean haveErrors = false;
                 boolean firstMsg = true;
                 List errorMessages = dc.getValidationContext().getValidationErrorsForScope(dc.getFieldStates().getState(rowField).getValidationContextScope());
                 if(errorMessages != null)
                 {
                     // display the error message for the row
                     messagesHtml.append("<span class=\"" + getErrorMessageLinkStyleClass() + "\">");
                     Iterator emi = errorMessages.iterator();
                     while(emi.hasNext())
                     {
                         if(!firstMsg)
                             messagesHtml.append("<br/>");
                         else
                             firstMsg = false;
                         messagesHtml.append((String) emi.next());
                     }
                     messagesHtml.append("</span>");
                     haveErrors = true;
                 }
                 writer.write(generateGridRowHtml(dc, gridField, rowField, row));
                 if(haveErrors)
                 {
                     writer.write("<tr><td colspan='" + colsCount + "'>");
                     writer.write(messagesHtml.toString());
                     writer.write("</td></tr>");
                 }
             }
         }
 
         writer.write("\n</table>");
     }
 
 
     /**
      * @param dc
      * @param field
      * @param fieldsHtml
      * @param fieldsJSDefn
      * @param fieldErrorMsgs
      *
      * @throws java.io.IOException
      */
     public void appendCompositeFieldHtml(DialogContext dc, DialogField field, StringBuffer fieldsHtml, StringBuffer fieldsJSDefn, List fieldErrorMsgs) throws IOException
     {
         // if the composite field is hidden, then all the children must be hidden too
         StringWriter writer = new StringWriter();
         renderCompositeControlsHtml(writer, dc, field);
 
         String captionHtml = generateFieldCaption(field, dc);
         fieldsHtml.append(generateFieldBlock(dc, field, captionHtml, writer.getBuffer().toString(), null, null));
         if(field.getName() != null)
             fieldsJSDefn.append(field.getJavaScriptDefn(dc));
     }
 
     public void appendSectionFieldHtml(DialogContext dc, DialogField field, StringBuffer fieldsHtml, StringBuffer fieldsJSDefn, List fieldErrorMsgs) throws IOException
     {
         // if the section field is hidden, then all the children must be hidden too
         StringWriter writer = new StringWriter();
         renderSectionControlsHtml(writer, dc, field, fieldErrorMsgs);
         String idHtml = generateFieldBlockId(field);
         //fieldsHtml.append("<tr" + getFieldBlockStyleClass() != null ? (" class=\"" + getFieldBlockStyleClass() + "\">\n") : ">\n" +
         //                "<td>\n" + writer.getBuffer().toString() + "</td></tr>\n");
 
         fieldsHtml.append("<tr class=\"" + getFieldBlockStyleClass() + "\">\n<td colspan=\"2\">\n" + writer.getBuffer().toString() + "</td></tr>\n");
         //fieldsHtml.append(writer.getBuffer().toString());
 
         if(field.getName() != null)
             fieldsJSDefn.append(field.getJavaScriptDefn(dc));
     }
 
     /**
      * Appends the HTML for the separator field to the buffer
      *
      * @param dc             current context of the dialog
      * @param field          current dialog field
      * @param fieldsHtml     the buffer object containing the html of the dialog's fields
      * @param fieldsJSDefn   the buffer object containing the JS declarations of the dialog's fields
      * @param fieldErrorMsgs list containing dialog error messages
      */
     public void appendSeparatorFieldHtml(DialogContext dc, DialogField field, StringBuffer fieldsHtml, StringBuffer fieldsJSDefn, List fieldErrorMsgs) throws IOException
     {
         String controlHtml = generateFieldControl(field, dc);
         fieldsHtml.append("<tr" + getFieldBlockStyleClass() != null
                           ? (" class=\"" + getFieldBlockStyleClass() + "\">")
                           : ">" +
                             "<td class=\"dialog-fields-separator\" colspan='2'>" + controlHtml + "</td></tr>\n");
         // add a JS also for the separator
         if(field.getName() != null)
             fieldsJSDefn.append(field.getJavaScriptDefn(dc));
     }
 
     /**
      * Writes the dialog field's html
      *
      * @param dc             current dialog context
      * @param field          dialog field
      * @param fieldsHtml     buffer containing the dialog's html
      * @param fieldsJSDefn   buffer containing the dialog's JS definitions
      * @param fieldErrorMsgs list of errors
      */
     public void appendFieldHtml(DialogContext dc, DialogField field, StringBuffer fieldsHtml, StringBuffer fieldsJSDefn, List fieldErrorMsgs) throws IOException
     {
         // Handle the special fields that have unique rendering aside from normal dialog fields
         if(field instanceof CompositeField)
         {
             appendCompositeFieldHtml(dc, field, fieldsHtml, fieldsJSDefn, fieldErrorMsgs);
             return;
         }
         else if(field instanceof SeparatorField)
         {
             StringWriter writer = new StringWriter();
             renderSeparatorHtml(writer, dc, (SeparatorField) field);
             fieldsHtml.append(writer);
             return;
         }
         else if(field instanceof SectionField)
         {
             appendSectionFieldHtml(dc, field, fieldsHtml, fieldsJSDefn, fieldErrorMsgs);
             return;
         }
 
         if(field.isInputHidden(dc))
         {
             StringWriter writer = new StringWriter();
             field.renderControlHtml(writer, dc);
             fieldsHtml.append(writer);
             // even if the field is hidden, you still need to register it in JS
             if(field.getName() != null)
                 fieldsJSDefn.append(field.getJavaScriptDefn(dc));
             return;
         }
 
         String captionHtml = generateFieldCaption(field, dc);
         String controlHtml = generateFieldControl(field, dc);
         String messagesHtml = generateFieldErrorMessage(field, dc, fieldErrorMsgs);
         String hintHtml = generateFieldHint(field, dc);
 
         fieldsHtml.append(generateFieldBlock(dc, field, captionHtml, controlHtml, hintHtml, messagesHtml));
 
         if(field.getName() != null)
             fieldsJSDefn.append(field.getJavaScriptDefn(dc));
     }
 
     /**
      * Writes the dialog's html, css, and associated JS declarations and loops through each child
      * field to generate their respective html entries.
      *
      * @param writer the writer object to write to
      * @param dc     current dialog context
      */
     public void renderHtml(Writer writer, DialogContext dc) throws IOException
     {
         renderPanelRegistration(writer, dc);
         if(dc.getDialog().hideHeading(dc))
             dc.setPanelRenderFlags(dc.getPanelRenderFlags() | HtmlPanel.RENDERFLAG_HIDE_FRAME_HEADING);
         int panelRenderFlags = dc.getPanelRenderFlags();
 
         if((panelRenderFlags & HtmlPanel.RENDERFLAG_NOFRAME) == 0)
         {
             renderFrameBegin(writer, dc);
             writer.write("    <table class=\"" + getDialogTableStyleClass() + "\" width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\">\n");
         }
         else
             writer.write("    <table id=\"" + dc.getPanel().getPanelIdentifier() + "_content\" class=\"" + getDialogTableStyleClass() + "\" width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\">\n");
 
         List fieldErrorMsgs = new ArrayList();
         List dlgErrorMsgs = dc.getValidationContext().getValidationErrorsForScope(ValidationContext.VALIDATIONSCOPE_ENTIRE_CONTEXT);
         if(dlgErrorMsgs != null)
             fieldErrorMsgs.addAll(dlgErrorMsgs);
 
         Dialog dialog = dc.getDialog();
 
         int layoutColumnsCount = dialog.getLayoutColumnsCount();
         int dlgTableColSpan = 2;
 
         // define the buffers to hold the fields' html and JS definitions
         StringBuffer fieldsHtml = new StringBuffer();
         StringBuffer fieldsJSDefn = new StringBuffer();
 
         DialogDirector director = dialog.getDirector();
         if(layoutColumnsCount == 1)
         {
             DialogFields fields = dc.getDialog().getFields();
             for(int i = 0; i < fields.size(); i++)
             {
                 DialogField field = fields.get(i);
                 if(!field.isAvailable(dc))
                     continue;
 
                 appendFieldHtml(dc, field, fieldsHtml, fieldsJSDefn, fieldErrorMsgs);
             }
             fieldsHtml.append(generateDirectorBlock(director, dc, dlgTableColSpan));
         }
         else
         {
             StringBuffer[] layoutColsFieldsHtml = new StringBuffer[layoutColumnsCount];
             for(int i = 0; i < layoutColumnsCount; i++)
                 layoutColsFieldsHtml[i] = new StringBuffer();
 
             int activeColumn = 0;
 
             DialogFields fields = dc.getDialog().getFields();
             for(int i = 0; i < fields.size(); i++)
             {
                 DialogField field = fields.get(i);
                 if(!field.isAvailable(dc))
                     continue;
 
                 DialogFieldFlags flags = field.getFlags();
                 if(flags.flagIsSet(DialogFieldFlags.COLUMN_BREAK_BEFORE))
                     activeColumn++;
                 appendFieldHtml(dc, field, layoutColsFieldsHtml[activeColumn], fieldsJSDefn, fieldErrorMsgs);
                 if(flags.flagIsSet(DialogFieldFlags.COLUMN_BREAK_AFTER))
                     activeColumn++;
             }
 
             int lastColumn = layoutColumnsCount - 1;
             int cellWidth = 100 / layoutColumnsCount;
             dlgTableColSpan = 0;
 
             fieldsHtml.append("<tr valign='top'>");
             for(int c = 0; c < layoutColumnsCount; c++)
             {
 
                 fieldsHtml.append("<td width='" + cellWidth + "%'><table width='100%'>");
                 fieldsHtml.append(layoutColsFieldsHtml[c]);
                 fieldsHtml.append("</table></td>");
                 dlgTableColSpan++;
 
                 if(c < lastColumn)
                 {
                     fieldsHtml.append("<td>&nbsp;&nbsp;</td>");
                     dlgTableColSpan++;
                 }
             }
             fieldsHtml.append("</tr>");
             fieldsHtml.append(generateDirectorBlock(director, dc, dlgTableColSpan));
         }
 
         StringBuffer errorMsgsHtml = new StringBuffer();
         if(fieldErrorMsgs.size() > 0)
             errorMsgsHtml.append(generateErrorMessages(fieldErrorMsgs, dlgTableColSpan));
         List fileList = dialog.getClientJs();
         String[] includeJSList = new String[fileList.size()];
         for(int i = 0; i < includeJSList.length; i++)
         {
             DialogIncludeJavascriptFile jsFileObj = (DialogIncludeJavascriptFile) fileList.get(i);
             includeJSList[i] = jsFileObj.getHref().getTextValue(dc);
         }
 
 
         if(prependPreScript != null)
             writer.write(prependPreScript);
         writer.write("<script language='JavaScript'>var _version = 1.0;</script>\n" +
                      "<script language='JavaScript1.1'>_version = 1.1;</script>\n" +
                      "<script language='JavaScript1.2'>_version = 1.2;</script>\n" +
                      "<script language='JavaScript1.3'>_version = 1.3;</script>\n" +
                      "<script language='JavaScript1.4'>_version = 1.4;</script>\n");
         if(includePreScripts != null)
             writer.write(includePreScripts);
 
         writer.write("<script language='JavaScript'>\n" +
                      "<!--\n" +
                      "	if(typeof dialogLibraryLoaded == 'undefined')\n" +
                      "	{\n" +
                      "		alert('ERROR: dialog.js was not loaded.');\n" +
                      "	}\n" +
                      "-->\n" +
                      "</script>\n");
 
         if(includeJSList.length > 0)
         {
             for(int i = 0; i < includeJSList.length; i++)
             {
                 writer.write("<script language='JavaScript' src='" + includeJSList[i] + "'></script>\n");
             }
         }
         if(includePostScripts != null)
             writer.write(includePostScripts);
         if(prependPostScript != null)
             writer.write(prependPostScript);
 
         DialogFlags dflags = dialog.getDialogFlags();
         if(dflags.flagIsSet(DialogFlags.DISABLE_CLIENT_VALIDATION))
             writer.write("<script>ALLOW_CLIENT_VALIDATION = false;</script>");
         if(dflags.flagIsSet(DialogFlags.TRANSLATE_ENTER_KEY_TO_TAB_KEY))
             writer.write("<script>TRANSLATE_ENTER_KEY_TO_TAB_KEY = true;</script>");
         if(dflags.flagIsSet(DialogFlags.SHOW_DATA_CHANGED_MESSAGE_ON_LEAVE))
             writer.write("<script>SHOW_DATA_CHANGED_MESSAGE_ON_LEAVE = true;</script>");
         if(dflags.flagIsSet(DialogFlags.DISABLE_CLIENT_KEYPRESS_FILTERS))
             writer.write("<script>ENABLE_KEYPRESS_FILTERS = flase;</script>");
         if(dflags.flagIsSet(DialogFlags.HIDE_HINTS_UNTIL_FOCUS))
             writer.write("<script>HIDE_HINTS_UNTIL_FOCUS = true;</script>");
 
         String dialogName = dialog.getHtmlFormName();
         String encType = dialog.getDialogFlags().flagIsSet(DialogFlags.ENCTYPE_MULTIPART_FORMDATA)
                          ? "enctype=\"multipart/form-data\"" : "";
 
         String actionURL = null;
         if(director != null)
             actionURL = director.getSubmitActionUrl() != null
                         ? director.getSubmitActionUrl().getValue(dc).getTextValue() : null;
 
         if(actionURL == null)
             actionURL = ((HttpServletRequest) dc.getRequest()).getRequestURI();
 
         renderContentsHtml(writer, dc, dialogName, actionURL, encType, dlgTableColSpan, errorMsgsHtml, fieldsHtml);
 
         if(appendPreScript != null)
             writer.write(appendPreScript);
 
         writer.write("<script language='JavaScript'>\n" +
                      "<!--\n" +
                      "       var " + dialogName + " = new Dialog(\"" + dialogName + "\");\n" +
                      "       var dialog = " + dialogName + "; setActiveDialog(dialog);\n" +
                      "       var field;\n" +
                      fieldsJSDefn +
                      "       dialog.finalizeContents();\n" +
                      "-->\n" +
                      "</script>\n");
 
         if(appendPostScript != null)
             writer.write(appendPostScript);
 
         // panel end
         writer.write("    </table>\n");
         if((panelRenderFlags & HtmlPanel.RENDERFLAG_NOFRAME) == 0)
             renderFrameEnd(writer, dc);
     }
 
     /**
      * Generates the html for the dialog's error messages. Generally, if there are field errors
      * they are also displayed at the top of the dialog.
      *
      * @param fieldErrorMsgs  list of field error messages
      * @param dlgTableColSpan the number of columns defined for display
      */
     protected String generateErrorMessages(List fieldErrorMsgs, int dlgTableColSpan)
     {
         StringBuffer errorMsgsHtml = new StringBuffer();
         errorMsgsHtml.append("<tr><td colspan='" + dlgTableColSpan + "'><ul type=\"square\">" +
                              "<span class=\"" + getErrorHeadingStyleClass() + "\">" + getErrorMessageHeadingText() + "</span>\n");
         for(int i = 0; i < fieldErrorMsgs.size(); i++)
         {
             String errorMsg = (String) fieldErrorMsgs.get(i);
             errorMsgsHtml.append("<li><a href='#dc_error_msg_" + i + "' class=\">" + getErrorMessageLinkStyleClass() + "\" " +
                                  errorMsg + "</a></li>\n");
         }
         errorMsgsHtml.append("</ul></td></tr>\n");
         return errorMsgsHtml.toString();
     }
 
     /**
      * Generates the html for the dialog director block element
      */
     protected String generateDirectorBlock(DialogDirector director, DialogContext dc, int dlgTableColSpan)
             throws IOException
     {
         StringBuffer fieldsHtml = new StringBuffer();
         if(director != null && director.isAvailable(dc) && !dc.getDialogState().getPerspectives().flagIsSet(DialogPerspectives.PRINT))
         {
             fieldsHtml.append("<tr" + (getDirectorStyleClass() != null
                                        ? " class=\"" + getDirectorStyleClass() + "\"" : "") + ">" +
                               "<td colspan='" + dlgTableColSpan + "'>");
             StringWriter directorHtml = new StringWriter();
             director.renderControlHtml(directorHtml, dc);
             fieldsHtml.append(directorHtml);
             fieldsHtml.append("</td></tr>");
         }
         return fieldsHtml.toString();
     }
 
 
     /**
      * Generates the ID for the html block that will represent the field. This block could be any html block element
      * such as a TR or a DIV.
      *
      * @param field dialog field
      *
      * @return the field block ID (e.g. id="_dfr.myField")
      */
     protected String generateFieldBlockId(DialogField field)
     {
         return " id=\"" + FIELDROW_PREFIX + field.getQualifiedName() + "\" ";
     }
 
     /**
      * Generates the dialog field's complete html block containing positioning and look
      *
      * @param field       dialog field
      * @param captionHtml the field caption html
      * @param controlHtml the field input html
      * @param hintHtml    the field hint html
      * @param errorHtml   the field error html
      *
      * @return the field's complete html block
      */
     protected String generateFieldBlock(DialogContext dc, DialogField field, String captionHtml, String controlHtml, String hintHtml,
                                         String errorHtml)
     {
         StringBuffer fieldsHtml = new StringBuffer();
         /*
         * each field block (row) gets its own ID so DHTML can hide/show the row
         */
         String idHtml = generateFieldBlockId(field);
 
         String blockStyle = null;
         if(errorHtml != null && errorHtml.length() > 0)
             blockStyle = getFieldBlockWithErrorStyleClass();
         else
             blockStyle = getFieldBlockStyleClass();
 
         if(captionHtml == null)
         {
             // append the field input html
             fieldsHtml.append("<tr" + idHtml + (blockStyle != null ? (" class=\"" + blockStyle + "\">") : ">") +
                               "<td colspan='2'>" + controlHtml);
             // now append the hint message html on the NEXT LINE but within the same row. This is so that when you hide the
             // field using the block ID, the hint will  also be hidden
             fieldsHtml.append((hintHtml != null ? "<br/>" + hintHtml : "") +
                               "</td></tr>\n");
             // now append the error message html on the next row
             if(errorHtml != null && errorHtml.length() > 0)
                 fieldsHtml.append("<tr><td colspan=\"2\"><span class=\"dialog-fields-errors\">&nbsp;&nbsp;&nbsp;" + errorHtml + "</span></td></tr>\n");
         }
         else
         {
 
             fieldsHtml.append("<tr" + idHtml + (blockStyle != null ? (" class=\"" + blockStyle + "\">") : ">") +
                               "<td class=\"" + (field.isRequired(dc)
                                                 ? getCaptionRequiredStyleClass() : getCaptionStyleClass()) + "\">" +
                               captionHtml + "</td><td>" + controlHtml + "");
             fieldsHtml.append((hintHtml != null ? "<br/>" + hintHtml : "") +
                               "</td></tr>\n");
             // now append the error message html on the next row
             if(errorHtml != null && errorHtml.length() > 0)
                 fieldsHtml.append("<tr><td>&nbsp;</td><td><span class=\"dialog-fields-errors\">&nbsp;&nbsp;&nbsp;" + errorHtml + "</span></td></tr>\n");
         }
         return fieldsHtml.toString();
     }
 
     /**
      * Modifies the field's caption HTML when an accesskey is assgined for the field. Currently, access keys are supported
      * in the Windows environment by the following browsers:
      * <p/>
      * <ul>
      * <li> IE 6 :          Alt+AccessKey to put focus on the link (not visible), then Enter to activate. For example: home is alt+2 and then press enter.
      * <li> IE 5.5, 5, 4:   Alt+AccessKey to put focus on the link (visible), then Enter to activate. For example: home is alt+2 and then press enter.
      * <li> Mozilla:        Alt+AccessKey. For example: home is alt+2 and then press enter.
      * <li> Netscape 6, 7:  Alt+AccessKey. For example: home is alt+2 and then press enter.
      * </ul>
      * <p/>
      * Under Mac OS X 10, access keys are not supported by Mozilla, OmniWeb 4, and Safari.
      *
      * @param field     the dialog field
      * @param caption   the field's caption string
      * @param accessKey the access key assigned to the field
      *
      * @return the caption is returned unchanged if access keys are not defined.
      */
     protected String assignFieldAccessKey(DialogField field, String caption, String accessKey)
     {
         StringBuffer fieldsHtml = new StringBuffer();
         if(accessKey != null && accessKey.length() > 0)
         {
             int accessKeyPos = caption.toLowerCase().indexOf(accessKey.toLowerCase());
             if(accessKeyPos > 0 && accessKeyPos < caption.length() - 1)
             {
                 fieldsHtml.append("<label for=\"" + field.getHtmlFormControlId() + "\" accesskey=\"" +
                                   field.getAccessKey() + "\">" + caption.substring(0, accessKeyPos) + "<span class=\"accesskey\">" +
                                   caption.substring(accessKeyPos, accessKeyPos + 1) + "</span>" + caption.substring(accessKeyPos + 1) + "</label>");
             }
             else if(accessKeyPos == caption.length() - 1)
             {
                 fieldsHtml.append("<label for=\"" + field.getHtmlFormControlId() + "\" accesskey=\"" +
                                   field.getAccessKey() + "\">" + caption.substring(0, accessKeyPos) + "<span class=\"accesskey\">" +
                                   caption.substring(accessKeyPos) + "</span></label>");
             }
             else if(accessKeyPos == 0)
             {
                 fieldsHtml.append("<label for=\"" + field.getHtmlFormControlId() + "\" accesskey=\"" +
                                   field.getAccessKey() + "\">" + "<span class=\"accesskey\">" +
                                   caption.substring(0, 1) + "</span>" + caption.substring(1) + "</label>");
             }
             else
             {
 // access key assigned is not included in the caption
                 fieldsHtml.append("<label for=\"" + field.getHtmlFormControlId() + "\" accesskey=\"" +
                                   field.getAccessKey() + "\">" + caption + "</label>");
             }
         }
         return fieldsHtml.length() > 0 ? fieldsHtml.toString() : caption;
     }
 
     /**
      * Generates the hint html for the field. This method should not set the location of the hint message; it just
      * sets the hint's font characteristics such as color, weight, and size.
      *
      * @param field dialog field
      * @param dc    current context of the dialog
      *
      * @return a null string if no hint is defined for the field
      */
     protected String generateFieldHint(DialogField field, DialogContext dc)
     {
        StringBuffer hintHtml = new StringBuffer();
         String hint = field.getHint().getTextValue(dc);
         if(hint != null)
         {
             DialogFlags dialogFlags = dc.getDialog().getDialogFlags();
             if((field.isReadOnly(dc) && dialogFlags.flagIsSet(DialogFlags.HIDE_READONLY_HINTS)))
             {
                 // if the field is read only and the hide-readonly-hints flag is set.
                 hintHtml.append("");
             }
             else if(dialogFlags.flagIsSet(DialogFlags.HIDE_HINTS_UNTIL_FOCUS))
             {
                 // hide the hints until the field is being edited
                 hintHtml.append("<span id=\"" + field.getQualifiedName() + "_hint\" class=\"" + getFieldHiddenHintStyleClass() + "\">&nbsp;&nbsp;&nbsp;" + hint + "</span>");
             }
             else
             {
                 hintHtml.append("<span id=\"" + field.getQualifiedName() + "_hint\" class=\"" + getFieldHintStyleClass() + "\">&nbsp;&nbsp;&nbsp;" + hint + "</span>");
             }

         }
        return hintHtml.toString();
     }
 
 
     /**
      * Generates the error html for the field. This method should not set the location of the error message; it just
      * sets the error message's font characteristics such as color, weight, and size. Only when there are more than one error
      * message, the generated HTML should contain special formatting to seperate the error messages.
      *
      * @param field          dialog field
      * @param dc             current context of the dialog
      * @param fieldErrorMsgs list containing the dialog's individual field messages
      *
      * @return a null string if there are no error messages else the field error html
      */
     protected String generateFieldErrorMessage(DialogField field, DialogContext dc, List fieldErrorMsgs)
     {
         StringBuffer messagesHtml = null;
         DialogField.State state = dc.getFieldStates().getState(field);
         List errorMessages = dc.getValidationContext().getValidationErrorsForScope(state.getValidationContextScope());
         if(errorMessages.size() > 0)
         {
             messagesHtml = new StringBuffer();
             messagesHtml.append("<span class=\"" + getErrorMessageStyleClass() + "\">");
             for(int i = 0; i < errorMessages.size(); i++)
             {
                 String msgStr = (String) errorMessages.get(i);
                 fieldErrorMsgs.add(msgStr);
                 if(i > 0)
                     messagesHtml.append("<br/>");
                 messagesHtml.append("<a name='dc_error_msg_" + i + "'>" + msgStr + "</a>");
             }
             messagesHtml.append("</span>");
         }
         return messagesHtml != null ? messagesHtml.toString() : null;
     }
 
     /**
      * Generates the input html for the field. Essentially it calls the <code>renderControlHtml()</code> method
      * of the field and appends extra html to handle popups and adjacent area settings. This method SHOULD not set the
      * location of the field's input; it just sets the input's font characteristics such as color, weight, and size.
      *
      * @param field dialog field
      * @param dc    current context of the dialog
      *
      * @return generated html string
      */
     protected String generateFieldControl(DialogField field, DialogContext dc)
             throws IOException
     {
         StringWriter controlHtml = new StringWriter();
         DialogField.State state = dc.getFieldStates().getState(field);
         if(field.isRequired(dc))
             controlHtml.write("<span class=\"" + getControlAreaRequiredStyleClass() + "\">");
         else if(field.isBrowserReadOnly(dc) || field.isReadOnly(dc))
             controlHtml.write("<span class=\"" + getControlAreaReadonlyStyleClass() + "\">");
         else
             controlHtml.write("<span class=\"" + getControlAreaStyleClass() + "\">");
         field.renderControlHtml(controlHtml, dc);
         String popupHtml = generatePopupHtml(dc, field);
         if(popupHtml != null)
             controlHtml.write(popupHtml);
 
         controlHtml.write(generateFieldAdjacentArea(field, dc, state));
         controlHtml.write("</span>");
         return controlHtml.getBuffer().toString();
     }
 
     /**
      * Generate the html for an adjacent area which is beside the control input
      *
      * @param field dialog field
      * @param dc    current dialog context
      * @param state dialog's field state
      */
     protected String generateFieldAdjacentArea(DialogField field, DialogContext dc, DialogField.State state)
     {
         DialogFieldFlags stateFlags = dc.getFieldStates().getState(field).getStateFlags();
         if(stateFlags.flagIsSet(DialogFieldFlags.CREATE_ADJACENT_AREA))
         {
             String adjValue = state.getAdjacentAreaValue();
             return ("&nbsp;<span id='" + field.getQualifiedName() + "_adjacent'>" + (adjValue != null ? adjValue : "") + "</span>");
         }
         return "";
     }
 
     /**
      * Generates the caption html for the field. If the caption is set to a special character &quot;*&quot;
      * and the field has childre, then a special caption will be created using the names of the children.
      * For example, if the children fields have the captions, Cat and Dog, then the caption of the field
      * will be Cat/Dog.
      *
      * @param field dialog field
      * @param dc    current context of the dialog
      *
      * @return the field's caption html
      */
     protected String generateFieldCaption(DialogField field, DialogContext dc)
     {
         String caption = field.getCaption().getTextValue(dc);
         DialogFields fieldChildren = field.getChildren();
         if(caption != null && fieldChildren != null && caption.equals(DialogField.GENERATE_CAPTION))
         {
             // if the caption is '*' and the field has children, create the caption based on the children's captions
             StringBuffer generated = new StringBuffer();
             for(int i = 0; i < fieldChildren.size(); i++)
             {
                 DialogField childField = fieldChildren.get(i);
                 String childCaption = childField.getCaption().getTextValue(dc);
                 if(childCaption != null && childCaption != DialogField.CUSTOM_CAPTION)
                 {
                     if(generated.length() > 0)
                         generated.append(" / ");
                     if(childField.isRequired(dc))
                         generated.append("<label for=\"" + field.getHtmlFormControlId() + "\" class=\"" + getCaptionRequiredStyleClass() + "\">" + childCaption + (endsWithPunctuation(caption)
                                                                                                                                                                    ? ""
                                                                                                                                                                    : ":") + "</label>");
                     else
                         generated.append("<label for=\"" + field.getHtmlFormControlId() + "\" class=\"" + getCaptionStyleClass() + "\">" + childCaption + (endsWithPunctuation(caption)
                                                                                                                                                            ? ""
                                                                                                                                                            : ":") + "</label>");
                 }
             }
             caption = generated.toString();
         }
         else
         {
             // currently  access keys are only supported for non-generated captions
             String accessKey = field.getAccessKey();
             if(accessKey != null && accessKey.length() > 0)
                 caption = assignFieldAccessKey(field, caption, accessKey);
 
             // if the caption suffix is set, append it to the caption
             if(getCaptionSuffix() != null && caption != null && caption.length() > 0) caption += getCaptionSuffix();
             if(caption != null)
             {
                 if(field.isRequired(dc))
                     caption = "<label class=\"" + getCaptionRequiredStyleClass() + "\" for=\"" + field.getHtmlFormControlId() + "\">" + caption +
                               (endsWithPunctuation(caption) ? "" : ":") + "</label>";
                 else
                     caption = "<label class=\"" + getCaptionStyleClass() + "\" for=\"" + field.getHtmlFormControlId() + "\">" +
                               (caption != null ? caption + (endsWithPunctuation(caption) ? "" : ":") : "") + "</label>";
             }
         }
 
         return caption;
     }
 
     public static boolean endsWithPunctuation(String caption)
     {
         boolean punctuation = false;
         char lastChar = caption.charAt(caption.length() - 1);
         switch(lastChar)
         {
             case '?':
             case '!':
             case ':':
             case '.':
                 punctuation = true;
                 break;
             default:
                 punctuation = false;
         }
         return punctuation;
     }
 
     /**
      * Generates the html for a popup field.
      *
      * @param dc    current dialog context
      * @param field popup field
      *
      * @return html for the popup field
      */
     public String generatePopupHtml(DialogContext dc, DialogField field)
     {
         DialogFieldPopup[] popup = field.getPopups();
 
         if(popup == null || popup.length == 0)
             return null;
 
         StringBuffer html = new StringBuffer();
         StringBuffer expression = null;
         for(int i = 0; i < popup.length; i++)
         {
             expression = new StringBuffer("new DialogFieldPopup('" + dc.getDialog().getHtmlFormName() + "', '" + field.getQualifiedName() +
                                           "', '" + popup[i].getAction().getTextValueOrBlank(dc) + "', '" + popup[i].getWindowClass() + "', " + popup[i].isCloseAfterSelect() +
                                           ", " + popup[i].isAllowMulti() + ", ");
 
             StringBuffer tmpBuffer = new StringBuffer();
             String[] fillFields = popup[i].getFill();
             if(fillFields != null && fillFields.length > 0)
             {
                 tmpBuffer.append("new Array(");
                 for(int k = 0; k < fillFields.length; k++)
                     tmpBuffer.append("'" + fillFields[k] + (k < fillFields.length - 1 ? "', " : "'"));
                 tmpBuffer.append(")");
             }
             else
                 tmpBuffer.append("null");
             expression.append(tmpBuffer.toString() + ", ");
 
             tmpBuffer = new StringBuffer();
             String[] extractFields = popup[i].getExtract();
             // append the list of extract fields if they exist
             if(extractFields != null && extractFields.length > 0)
             {
                 tmpBuffer.append("new Array(");
                 for(int k = 0; k < extractFields.length; k++)
                 {
                     tmpBuffer.append("'" + extractFields[k] + (k < extractFields.length - 1 ? "', " : "'"));
                 }
                 tmpBuffer.append(")");
             }
             else
                 tmpBuffer.append("null");
             expression.append(tmpBuffer.toString() + ", ");
 
             // append evaluation script
             if(popup[i].getPreActionScript() != null)
                 expression.append("'" + popup[i].getPreActionScript() + "'");
             else
                 expression.append("null");
             expression.append(")");
 
             if(popup[i].getStyle().getValueIndex() == DialogFieldPopup.Style.TEXT)
             {
                 html.append("&nbsp;<a href='' style='cursor:hand;' onclick=\"javascript:" + expression +
                             ";return false;\">" + popup[i].getStyleText().getTextValue(dc) + "</a>&nbsp;");
             }
             else
             {
                 String imageUrl = popup[i].getImageSrc().getTextValue(dc);
                 if(imageUrl == null)
                     imageUrl = getTheme().getResourceUrl("/images/panel/input/content-popup.gif");
                 html.append("&nbsp;<a href='' style='cursor:hand;' onclick=\"javascript:" + expression +
                             ";return false;\"><img border='0' src='" + imageUrl + "' alt='pop-up'></a>&nbsp;");
             }
         }
         return html.toString();
     }
 
     /**
      * Writes the html FORM element of the dialog
      */
     public void renderContentsHtml(Writer writer, DialogContext dc, String dialogName, String actionURL, String encType,
                                    int dlgTableColSpan, StringBuffer errorMsgsHtml, StringBuffer fieldsHtml) throws IOException
     {
         if(isSummarizeErrors())
             writer.write(errorMsgsHtml.toString());
 
         writer.write("<form id='" + dialogName + "' name='" + dialogName + "' action='" + actionURL + "' method='post' " +
                      encType + " onsubmit='return(activeDialog.isValid())'>\n" +
                      dc.getStateHiddens() + "\n" +
                      fieldsHtml +
                      "</form>\n");
     }
 
     /**
      * Write the separator field html to the writer object
      *
      * @param writer writer object to write the HTML to
      * @param dc     current dialog context
      * @param field  the separator field
      */
     public void renderSeparatorHtml(Writer writer, DialogContext dc, SeparatorField field) throws IOException
     {
         String heading = field.getHeading().getTextValue(dc);
         DialogFieldFlags flags = field.getFlags();
 
         if(heading != null)
         {
             writer.write("<tr" + (getSeparatorBlockStyleClass() != null
                                   ? (" class=\"" + getSeparatorBlockStyleClass() + "\">") : ">") +
                          "<td colspan='2'>\n");
             String sep = "<a name=\"" + URLEncoder.encode(heading) + "\">" + heading + "</a>";
             if(field.getBanner() != null)
             {
                 sep += "<br/><span class=\"" + getSeparatorBannerStyleClass() + "\">";
                 sep += field.getBanner().getTextValue(dc);
                 sep += "</span>";
             }
 
             /*
             if(flags.flagIsSet(SeparatorField.Flags.RULE))
                 sep += separatorHtml;
             */
 
             if(flags.flagIsSet(DialogFieldFlags.COLUMN_BREAK_BEFORE))
                 writer.write(sep);
             else
                 writer.write("<br/>" + sep);
         }
         else
         {
             if(!flags.flagIsSet(DialogFieldFlags.COLUMN_BREAK_BEFORE))
                 writer.write(flags.flagIsSet(SeparatorField.Flags.RULE) ? "<br/>" : "");
         }
         writer.write("</td></tr>\n");
     }
 
     /**
      * @param writer
      * @param dc
      * @param redirectUrl
      *
      * @throws java.io.IOException
      */
     public void renderRedirectHtml(Writer writer, DialogContext dc, String redirectUrl) throws IOException
     {
         writer.write("<script>window.location = \"");
         writer.write(redirectUrl);
         writer.write("\";</script>");
     }
 
     public boolean isSummarizeErrors()
     {
         return summarizeErrors;
     }
 
     public void setSummarizeErrors(boolean summarizeErrors)
     {
         this.summarizeErrors = summarizeErrors;
     }
 
     /**
      * Gets the HTML included in the input part of a field
      */
     public String getDefaultControlAttrs()
     {
         return fieldControlAttrs;
     }
 
     /**
      * This method is here only to satisfy the interface requirement. Only cascading style sheets are used instead
      * of setting font tags.
      *
      * @deprecated since 7.0.0
      */
     public String getControlAreaFontAttrs()
     {
         return null;
     }
 
     /**
      * This method is here only to satisfy the interface requirement.Only cascading style sheets are used instead
      * of setting font tags.
      *
      * @deprecated since 7.0.0
      */
     public String getControlAreaStyleAttrs()
     {
         return null;
     }
 
     /**
      * Gets the CSS class name used for field input
      *
      * @return CSS style class name
      *
      * @deprecated since 7.0.0
      */
     public String getControlAreaStyleClass()
     {
         return fieldControlAreaStyleClass;
     }
 
     /**
      * Gets the CSS class name used for a required field input
      *
      * @return CSS style class name
      */
     public String getControlAreaRequiredStyleClass()
     {
         return fieldControlAreaRequiredStyleClass;
     }
 
     /**
      * Gets the CSS class name used for a read-only field input
      *
      * @return CSS style class name
      */
     public String getControlAreaReadonlyStyleClass()
     {
         return fieldControlAreaReadonlyStyleClass;
     }
 
 }
