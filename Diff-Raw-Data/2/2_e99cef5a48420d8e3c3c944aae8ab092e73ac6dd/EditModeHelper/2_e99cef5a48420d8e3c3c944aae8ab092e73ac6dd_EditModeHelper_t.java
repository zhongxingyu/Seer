 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces.components.input;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxSelectList;
 import com.flexive.shared.value.*;
 import com.flexive.war.servlet.ThumbnailServlet;
 import org.apache.commons.lang.StringUtils;
 import org.apache.myfaces.custom.date.HtmlInputDate;
 import org.apache.myfaces.custom.fileupload.HtmlInputFileUpload;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.component.UISelectItems;
 import javax.faces.component.html.*;
 import javax.faces.context.ResponseWriter;
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Renders an FxValueInput component in edit mode.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 class EditModeHelper extends RenderHelper {
     private static final String REQUEST_EDITORINIT = "REQUEST_EDITORINIT";
     private static final String JS_OBJECT = "fxValue";
 
     public EditModeHelper(ResponseWriter writer, FxValueInput component, String clientId, FxValue value) {
         super(writer, component, clientId, value);
     }
 
     /**
      * Renders a multi langugae input field.
      *
      * @throws java.io.IOException if the component could not be rendered
      */
     @Override
     protected void encodeMultiLanguageField() throws IOException {
         ArrayList<FxLanguage> languages = FxValueInputRenderer.getLanguages();
         ensureDefaultLanguageExists(value, languages);
         String radioName = clientId + FxValueInputRenderer.DEFAULT_LANGUAGE;
         writer.startElement("div", null);
         writer.writeAttribute("id", clientId, null);
         writer.writeAttribute("class", FxValueInputRenderer.CSS_CONTAINER, null);
 
         List<String> rowIds = new ArrayList<String>(languages.size());
         for (FxLanguage language : languages) {
             String containerId = clientId + FxValueInputRenderer.LANG_CONTAINER + language.getId();
             String inputId = clientId + FxValueInputRenderer.INPUT + language.getId();
             rowIds.add("'" + containerId + "'");
 
             writer.startElement("div", null);
             writer.writeAttribute("id", containerId, null);
             writer.writeAttribute("class", FxValueInputRenderer.CSS_LANG_CONTAINER, null);
             if (language.getId() != value.getDefaultLanguage()) {
                 writer.writeAttribute("style", "display:none", null);
             }
 
             encodeField(inputId, language);
             encodeDefaultLanguageRadio(radioName, language);
 
             writer.endElement("div");
         }
 
         // language select
         String languageSelectId = clientId + FxValueInputRenderer.LANG_SELECT;
         writer.startElement("select", null);
         writer.writeAttribute("name", languageSelectId, null);
         writer.writeAttribute("style", "float:right", null);
         writer.writeAttribute("onchange", "document.getElementById('" + clientId + "')."
                 + JS_OBJECT + ".onLanguageChanged(this)", null);
         for (FxLanguage language : languages) {
             writer.startElement("option", null);
             writer.writeAttribute("value", language.getId(), null);
             if (language.getId() == value.getDefaultLanguage()) {
                 writer.writeAttribute("selected", "selected", null);
             }
             writer.writeText(language.getName().getBestTranslation(), null);
             writer.endElement("option");
         }
         writer.endElement("select");
 
         writer.startElement("br", null);
         writer.writeAttribute("clear", "all", null);
         writer.endElement("br");
         writer.write("&nbsp;");
         writer.endElement("div");
 
         // attach JS handler object to container div
         writer.write(MessageFormat.format(
                 "<script language=\"javascript\">\n"
                         + "<!--\n"
                         + "  document.getElementById(''{0}'')." + JS_OBJECT
                         + " = new FxMultiLanguageValueInput(''{0}'', ''{1}'', [{2}]);\n"
                         + "//-->\n"
                         + "</script>",
                 clientId, clientId + FxValueInputRenderer.LANG_CONTAINER, StringUtils.join(rowIds.iterator(), ',')
         ));
     }
 
     /**
      * Ensure that the default language of the given value exists in languages.
      * If it does not exist and languages is not empty, the first language is chosen
      * as default language.
      *
      * @param value     the FxValue to be checked
      * @param languages the available languages
      */
     private void ensureDefaultLanguageExists(FxValue value, ArrayList<FxLanguage> languages) {
         boolean defaultLanguageExists = false;
         for (FxLanguage language : languages) {
             if (language.getId() == value.getDefaultLanguage()) {
                 defaultLanguageExists = true;
                 break;
             }
         }
         if (!defaultLanguageExists && languages.size() > 0) {
             value.setDefaultLanguage(languages.get(0).getId(), true);
         }
     }
 
     /**
      * Render the default language radiobutton for the given language.
      *
      * @param radioName name of the radio input control
      * @param language  the language for which this input should be rendered
      * @throws IOException if a io error occured
      */
     private void encodeDefaultLanguageRadio(String radioName, FxLanguage language) throws IOException {
         writer.startElement("input", null);
         writer.writeAttribute("type", "radio", null);
         writer.writeAttribute("name", radioName, null);
         writer.writeAttribute("value", language.getId(), null);
         writer.writeAttribute("style", "float:left", null);
         if (language.getId() == value.getDefaultLanguage()) {
             writer.writeAttribute("checked", "true", null);
         }
         writer.endElement("input");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void encodeField(String inputId, FxLanguage language) throws IOException {
         boolean multiLine = false;
         if (value != null && StringUtils.isNotBlank(value.getXPath()) && value instanceof FxString) {
             multiLine = ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(value.getXPath())).isMultiLine();
         }
         if (value instanceof FxHTML || multiLine) {
             renderTextArea(inputId, language);
         } else if (value instanceof FxSelectOne) {
             renderSelectOne(inputId, language);
         } else if (value instanceof FxSelectMany) {
             renderSelectMany(inputId, language);
         } else if (value instanceof FxDate) {
             renderDateInput(inputId, language);
         } else if (value instanceof FxDateTime) {
             renderDateTimeInput(inputId, language);
         } else if (value instanceof FxReference) {
             renderReferenceSelect(inputId, language);
         } else if (value instanceof FxBinary) {
             renderBinary(inputId, language);
         } else if (value instanceof FxBoolean) {
             renderCheckbox(inputId, language);
         } else {
             renderTextInput(inputId, language);
         }
     }
 
     private void renderTextInput(String inputId, FxLanguage language) throws IOException {
         writer.startElement("input", null);
         writeHtmlAttributes();
         writer.writeAttribute("type", "text", null);
         writer.writeAttribute("name", inputId, null);
         writer.writeAttribute("id", inputId, null);
         if (value.getMaxInputLength() > 0) {
             writer.writeAttribute("maxlength", value.getMaxInputLength(), null);
         }
         writer.writeAttribute("value", getTextValue(language), null);
         writer.writeAttribute("class", FxValueInputRenderer.CSS_TEXT_INPUT, null);
         writer.endElement("input");
     }
 
     private void renderTextArea(String inputId, FxLanguage language) throws IOException {
         if (component.isForceLineInput()) {
             renderTextInput(inputId, language);
             return;
         }
         writer.startElement("textarea", null);
         writer.writeAttribute("id", inputId, null);
         writeHtmlAttributes();
         if (value instanceof FxHTML) {
             // render tinyMCE editor
             if (!FxJsfUtils.isAjaxRequest()) {
                 // when the page is updated via ajax, tinyMCE generates an additional hidden input
                 // and the text area is essential an anonymous div container (not sure why)
                 writer.writeAttribute("name", inputId, null);
             }
             writer.writeAttribute("class", FxValueInputRenderer.CSS_TEXTAREA_HTML, null);
             writer.endElement("textarea");
             writer.startElement("script", null);
             writer.writeAttribute("type", "text/javascript", null);
             if (FxJsfUtils.isAjaxRequest() && FxJsfUtils.getRequest().getAttribute(REQUEST_EDITORINIT) == null) {
                 // reset tinyMCE to avoid getDoc() error messages
                 writer.write("tinyMCE.idCounter = 0;\n");
                 FxJsfUtils.getRequest().setAttribute(REQUEST_EDITORINIT, true);
             }
             writer.write("tinyMCE.execCommand('mceAddControl', false, '" + inputId + "');\n");
             writer.write("tinyMCE.execInstanceCommand('" + inputId + "', 'mceSetContent', false, '"
                    + FxFormatUtils.escapeForJavaScript(getTextValue(language), false, false) + "');\n");
             writer.endElement("script");
         } else {
             // render standard text area
             writer.writeAttribute("name", inputId, null);
             writer.writeAttribute("class", FxValueInputRenderer.CSS_TEXTAREA, null);
             writer.writeText(getTextValue(language), null);
             writer.endElement("textarea");
         }
     }
 
     /**
      * Render additional HTML attributes passed to the FxValueInput component.
      *
      * @throws IOException if the output could not be written
      */
     private void writeHtmlAttributes() throws IOException {
         if (StringUtils.isNotBlank(component.getOnchange())) {
             writer.writeAttribute("onchange", component.getOnchange(), null);
         }
     }
 
     private void renderSelectOne(String inputId, FxLanguage language) throws IOException {
         final FxSelectOne selectValue = (FxSelectOne) value;
         // create selectone component
         final HtmlSelectOneListbox listbox = (HtmlSelectOneListbox) createUISelect(inputId, HtmlSelectOneListbox.COMPONENT_TYPE);
         listbox.setSize(1);
         listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH);
         // update posted value
         listbox.setValue(String.valueOf(selectValue.getTranslation(language).getId()));
         storeSelectItems(listbox, selectValue.getSelectList());
     }
 
     private void renderSelectMany(String inputId, FxLanguage language) {
         final FxSelectMany selectValue = (FxSelectMany) value;
         final SelectMany sm = selectValue.getTranslation(language) != null ? selectValue.getTranslation(language) : new SelectMany(selectValue.getSelectList());
         final String[] selected = new String[sm.getSelected().size()];
         for (int i = 0; i < selected.length; i++) {
             selected[i] = String.valueOf(sm.getSelected().get(i).getId());
         }
         if (component.isForceLineInput()) {
             // render a single line dropdown
             final HtmlSelectOneListbox listbox = (HtmlSelectOneListbox) createUISelect(inputId, HtmlSelectOneListbox.COMPONENT_TYPE);
             listbox.setSize(1);
             listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH);
             if (selected.length > 0) {
                 // choose first selected element - other selections get discarded
                 listbox.setValue(selected[0]);
             }
             storeSelectItems(listbox, selectValue.getSelectList());
         } else {
             // render a "multiple" select list
             final HtmlSelectManyListbox listbox = (HtmlSelectManyListbox) createUISelect(inputId, HtmlSelectManyListbox.COMPONENT_TYPE);
             listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH);
             listbox.setSelectedValues(selected);
             storeSelectItems(listbox, selectValue.getSelectList());
         }
     }
 
     private void addHtmlAttributes(UIComponent listbox) {
         if (component.getOnchange() != null) {
             //noinspection unchecked
             listbox.getAttributes().put("onchange", component.getOnchange());
         }
     }
 
     private UIInput createUISelect(String inputId, String componentType) {
         final UIInput listbox = (UIInput) FxJsfUtils.addChildComponent(component, componentType);
         listbox.setId(stripForm(inputId));
         addHtmlAttributes(listbox);
         return listbox;
     }
 
     private void storeSelectItems(UIInput listbox, FxSelectList selectList) {
         // store available items in select component
         final UISelectItems selectItems = (UISelectItems) FxJsfUtils.createComponent(UISelectItems.COMPONENT_TYPE);
         final List<SelectItem> items = FxJsfUtils.asSelectList(selectList);
         Collections.sort(items, new FxJsfUtils.SelectItemSorter());
         selectItems.setValue(items);
         listbox.setConverter(FxJsfUtils.getApplication().createConverter(Long.class));
         listbox.getChildren().add(selectItems);
     }
 
     private void renderDateInput(String inputId, FxLanguage language) {
         createInputDate(inputId, language);
     }
 
     private void renderDateTimeInput(String inputId, FxLanguage language) {
         createInputDate(inputId, language).setType("both");
     }
 
     @SuppressWarnings({"unchecked"})
     private HtmlInputDate createInputDate(String inputId, FxLanguage language) {
         final HtmlInputDate inputDate = (HtmlInputDate) FxJsfUtils.addChildComponent(component, HtmlInputDate.COMPONENT_TYPE);
         inputDate.setId(stripForm(inputId));
         if (!value.isTranslationEmpty(language)) {
             inputDate.setValue(((FxValue<Date, ?>) value).getBestTranslation(language));
         }
         inputDate.setPopupCalendar(false);
         return inputDate;
     }
 
     private void renderReferenceSelect(String inputId, FxLanguage language) throws IOException {
         FxReference referenceValue = (FxReference) value;
         final String popupLink = "javascript:openReferenceQueryPopup('" + value.getXPath() + "', '"
                 + inputId + "', '" + getForm(inputId) + "')";
         // render hidden input that contains the actual reference
         final HtmlInputHidden hidden = (HtmlInputHidden) FxJsfUtils.addChildComponent(component, HtmlInputHidden.COMPONENT_TYPE);
         hidden.setId(stripForm(inputId));
         // render image container (we need this since the image id attribute does not get rendered)
         final HtmlOutputLink imageContainer = (HtmlOutputLink) FxJsfUtils.addChildComponent(component, HtmlOutputLink.COMPONENT_TYPE);
         imageContainer.setId(stripForm(inputId) + "_preview");
         imageContainer.setValue(popupLink);
         // render the image itself
         final HtmlGraphicImage image = (HtmlGraphicImage) FxJsfUtils.addChildComponent(imageContainer, HtmlGraphicImage.COMPONENT_TYPE);
         image.setStyle("border:0");
         if (!value.isEmpty() && ((language != null && !value.isTranslationEmpty(language)) || language == null)) {
             // render preview image
             final FxPK translation = referenceValue.getTranslation(language);
             image.setUrl(ThumbnailServlet.getLink(translation, BinaryDescriptor.PreviewSizes.PREVIEW2));
             hidden.setValue(translation);
         } else {
             image.setUrl("/pub/images/empty.gif");
         }
         // render popup button
         final HtmlOutputLink link = (HtmlOutputLink) FxJsfUtils.addChildComponent(component, HtmlOutputLink.COMPONENT_TYPE);
         link.setValue(popupLink);
         final HtmlGraphicImage button = (HtmlGraphicImage) FxJsfUtils.addChildComponent(link, HtmlGraphicImage.COMPONENT_TYPE);
         button.setUrl("/adm/images/contentEditor/findReferences.png");
         button.setStyle("border:0");
     }
 
     private void renderBinary(String inputId, FxLanguage language) throws IOException {
         if (!value.isEmpty()) {
             final HtmlGraphicImage image = (HtmlGraphicImage) FxJsfUtils.addChildComponent(component, HtmlGraphicImage.COMPONENT_TYPE);
             image.setUrl(ThumbnailServlet.getLink(XPathElement.getPK(value.getXPath()),
                     BinaryDescriptor.PreviewSizes.PREVIEW2, value.getXPath()));
         }
         final HtmlInputFileUpload upload = (HtmlInputFileUpload) FxJsfUtils.addChildComponent(component, HtmlInputFileUpload.COMPONENT_TYPE);
         upload.setId(stripForm(inputId));
     }
 
     private void renderCheckbox(String inputId, FxLanguage language) throws IOException {
         final HtmlSelectBooleanCheckbox checkbox = (HtmlSelectBooleanCheckbox) FxJsfUtils.addChildComponent(component, HtmlSelectBooleanCheckbox.COMPONENT_TYPE);
         checkbox.setId(stripForm(inputId));
         checkbox.setValue(value.getTranslation(language));
         addHtmlAttributes(checkbox);
     }
 
     private String getTextValue(FxLanguage language) {
         if (value.isEmpty()) {
             return "";
         }
         final Object writeValue = getWriteValue(language);
         //noinspection unchecked
         return value.isValid() ? value.getStringValue(writeValue) :
                 (writeValue != null ? writeValue.toString() : "");
     }
 
     private Object getWriteValue(FxLanguage language) {
         final Object writeValue;
         if (language != null) {
             //noinspection unchecked
             writeValue = value.isTranslationEmpty(language) ? "" : value.getTranslation(language);
         } else {
             //noinspection unchecked
             writeValue = value.getDefaultTranslation();
         }
         return writeValue;
     }
 
     private String stripForm(String inputId) {
         return inputId.substring(inputId.lastIndexOf(':') + 1);
     }
 
     private String getForm(String inputId) {
         return inputId.substring(0, inputId.indexOf(':'));
     }
 
 }
