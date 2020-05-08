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
 import com.flexive.faces.beans.UserConfigurationBean;
 import com.flexive.faces.beans.MessageBean;
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.configuration.SystemParameters;
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
 import javax.faces.context.FacesContext;
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
 
     public EditModeHelper( FxValueInput component, String clientId, FxValue value) {
         super(component, clientId, value);
     }
 
     /**
      * Renders a multi langugage input field.
      *
      * @throws java.io.IOException if the component could not be rendered
      */
     @Override
     protected void encodeMultiLanguageField() throws IOException {
         final List<FxLanguage> languages = FxValueInputRenderer.getLanguages();
         //ensureDefaultLanguageExists(value, languages);
         final String radioName = clientId + FxValueInputRenderer.DEFAULT_LANGUAGE;
 
         final ContainerWriter container = new ContainerWriter();
         container.setInputClientId(clientId);
         component.getChildren().add(container);
 
         final List<String> rowIds = new ArrayList<String>(languages.size());
         for (final FxLanguage language : languages) {
             final String containerId = clientId + FxValueInputRenderer.LANG_CONTAINER + language.getId();
             final String inputId = clientId + FxValueInputRenderer.INPUT + language.getId();
             rowIds.add("'" + containerId + "'");
 
             final LanguageContainerWriter languageContainer = new LanguageContainerWriter();
             languageContainer.setContainerId(containerId);
             languageContainer.setLanguageId(language.getId());
             container.getChildren().add(languageContainer);
 
             encodeField(languageContainer, inputId, language);
             encodeDefaultLanguageRadio(languageContainer, clientId, radioName, language);
         }
 
         final LanguageSelectWriter languageSelect = new LanguageSelectWriter();
         languageSelect.setInputClientId(clientId);
         languageSelect.setRowIds(rowIds);
         container.getChildren().add(languageSelect);
     }
 
     /**
      * Ensure that the default language of the given value exists in languages.
      * If it does not exist and languages is not empty, the first language is chosen
      * as default language.
      *
      * @param value     the FxValue to be checked
      * @param languages the available languages
      */
     private void ensureDefaultLanguageExists(FxValue value, List<FxLanguage> languages) {
         boolean defaultLanguageExists = false;
         for (FxLanguage language : languages) {
             if (language.getId() == value.getDefaultLanguage()) {
                 defaultLanguageExists = true;
                 break;
             }
         }
         if (!defaultLanguageExists && languages.size() > 0) {
             value.setDefaultLanguage((int)languages.get(0).getId(), true);
         }
     }
 
     /**
      * Render the default language radiobutton for the given language.
      *
      * @param parent    the parent component
      * @param clientId  the client ID
      * @param radioName name of the radio input control
      * @param language  the language for which this input should be rendered @throws IOException if a io error occured
      */
     private void encodeDefaultLanguageRadio(LanguageContainerWriter parent, String clientId, final String radioName, final FxLanguage language) {
         final DefaultLanguageRadioWriter radio = new DefaultLanguageRadioWriter();
         radio.setRadioName(radioName);
         radio.setLanguageId(language.getId());
         radio.setContainerId(parent.getContainerId());
         radio.setLanguageCode(language.getIso2digit());
         radio.setInputClientId(clientId);
         parent.getChildren().add(radio);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void encodeField(UIComponent parent, String inputId, FxLanguage language) throws IOException {
         boolean multiLine = false;
         if (language == null) {
             final ContainerWriter container = new ContainerWriter();
             container.setInputClientId(clientId);
             parent.getChildren().add(container);
             // use container as parent for all subsequent operations
             parent = container;
         }
         if (value != null && StringUtils.isNotBlank(value.getXPath()) && value instanceof FxString) {
             multiLine = ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(value.getXPath())).isMultiLine();
         }
         if (value instanceof FxHTML || multiLine) {
             renderTextArea(parent, inputId, language);
         } else if (value instanceof FxSelectOne) {
             renderSelectOne(parent, inputId, language);
         } else if (value instanceof FxSelectMany) {
             renderSelectMany(parent, inputId, language);
         } else if (value instanceof FxDate) {
             renderDateInput(parent, inputId, language);
         } else if (value instanceof FxDateTime) {
             renderDateTimeInput(parent, inputId, language);
         } else if (value instanceof FxReference) {
             renderReferenceSelect(parent, inputId, language);
         } else if (value instanceof FxBinary) {
             renderBinary(parent, inputId, language);
         } else if (value instanceof FxBoolean) {
             renderCheckbox(parent, inputId, language);
         } else {
             renderTextInput(component, parent, value, inputId, language != null ? language.getId() : -1);
         }
     }
 
     private static void renderTextInput(FxValueInput inputComponent, UIComponent parent, FxValue value, final String inputId, long languageId) throws IOException {
         final HtmlInputText input = (HtmlInputText) FxJsfUtils.addChildComponent(parent, HtmlInputText.COMPONENT_TYPE);
         addHtmlAttributes(inputComponent, input);
         input.setId(stripForm(inputId));
         if (value.getMaxInputLength() > 0) {
             input.setMaxlength(value.getMaxInputLength());
         }
         input.setValue(getTextValue(value, languageId));
         input.setStyleClass(FxValueInputRenderer.CSS_TEXT_INPUT);
     }
 
     private void renderTextArea(UIComponent parent, final String inputId, final FxLanguage language) throws IOException {
         final TextAreaWriter textArea = new TextAreaWriter();
         textArea.setInputClientId(inputId);
         textArea.setLanguageId(language != null ? language.getId() : -1);
         parent.getChildren().add(textArea);
     }
 
     /**
      * Render additional HTML attributes passed to the FxValueInput component.
      *
      * @throws IOException if the output could not be written
      * @param component the input component
      * @param writer    the output writer
      */
     private static void writeHtmlAttributes(FxValueInput component, ResponseWriter writer) throws IOException {
         if (StringUtils.isNotBlank(component.getOnchange())) {
             writer.writeAttribute("onchange", component.getOnchange(), null);
         }
     }
 
     private void renderSelectOne(UIComponent parent, String inputId, FxLanguage language) throws IOException {
         final FxSelectOne selectValue = (FxSelectOne) value;
         // create selectone component
         final HtmlSelectOneListbox listbox = (HtmlSelectOneListbox) createUISelect(parent, inputId, HtmlSelectOneListbox.COMPONENT_TYPE);
         listbox.setSize(1);
         listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH);
         // update posted value
         if (selectValue.getTranslation(language) != null) {
             listbox.setValue(String.valueOf(selectValue.getTranslation(language).getId()));
         }
         storeSelectItems(listbox, selectValue.getSelectList());
     }
 
     private void renderSelectMany(UIComponent parent, String inputId, FxLanguage language) {
         final FxSelectMany selectValue = (FxSelectMany) value;
         final SelectMany sm = selectValue.getTranslation(language) != null ? selectValue.getTranslation(language) : new SelectMany(selectValue.getSelectList());
         final String[] selected = new String[sm.getSelected().size()];
         for (int i = 0; i < selected.length; i++) {
             selected[i] = String.valueOf(sm.getSelected().get(i).getId());
         }
         if (component.isForceLineInput()) {
             // render a single line dropdown
             final HtmlSelectOneListbox listbox = (HtmlSelectOneListbox) createUISelect(parent, inputId, HtmlSelectOneListbox.COMPONENT_TYPE);
             listbox.setSize(1);
             listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH);
             if (selected.length > 0) {
                 // choose first selected element - other selections get discarded
                 listbox.setValue(selected[0]);
             }
             storeSelectItems(listbox, selectValue.getSelectList());
         } else {
             // render a "multiple" select list
             final HtmlSelectManyListbox listbox = (HtmlSelectManyListbox) createUISelect(parent, inputId, HtmlSelectManyListbox.COMPONENT_TYPE);
             listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH);
             listbox.setSelectedValues(selected);
             storeSelectItems(listbox, selectValue.getSelectList());
             // automatically limit select list rows for very long lists
             listbox.setSize(Math.min(selectValue.getSelectList().getItems().size(), 7));
         }
     }
 
     private static void addHtmlAttributes(FxValueInput component, UIComponent target) {
         if (component.getOnchange() != null) {
             //noinspection unchecked
             target.getAttributes().put("onchange", component.getOnchange());
         }
     }
 
     private UIInput createUISelect(UIComponent parent, String inputId, String componentType) {
         final UIInput listbox = (UIInput) FxJsfUtils.addChildComponent(parent, componentType);
         listbox.setId(stripForm(inputId));
         addHtmlAttributes(component, listbox);
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
 
     private void renderDateInput(UIComponent parent, String inputId, FxLanguage language) {
         createInputDate(parent, inputId, language);
     }
 
     private void renderDateTimeInput(UIComponent parent, String inputId, FxLanguage language) {
         createInputDate(parent, inputId, language).setType("full");
     }
 
     @SuppressWarnings({"unchecked"})
     private HtmlInputDate createInputDate(UIComponent parent, String inputId, FxLanguage language) {
         final HtmlInputDate inputDate = (HtmlInputDate) FxJsfUtils.addChildComponent(parent, HtmlInputDate.COMPONENT_TYPE);
         inputDate.setId(stripForm(inputId));
         if (!value.isTranslationEmpty(language)) {
             inputDate.setValue(((FxValue<Date, ?>) value).getBestTranslation(language));
         }
         inputDate.setPopupCalendar(false);
         return inputDate;
     }
 
     private void renderReferenceSelect(UIComponent parent, String inputId, FxLanguage language) throws IOException {
         final String popupLink = "javascript:openReferenceQueryPopup('" + value.getXPath() + "', '"
                 + inputId + "', '" + getForm(inputId) + "')";
         // render hidden input that contains the actual reference
         final HtmlInputHidden inputPk = (HtmlInputHidden) FxJsfUtils.addChildComponent(parent, HtmlInputHidden.COMPONENT_TYPE);
         inputPk.setId(stripForm(inputId));
         // render hidden input where the caption is stored
         final HtmlInputHidden inputCaption = (HtmlInputHidden) FxJsfUtils.addChildComponent(parent, HtmlInputHidden.COMPONENT_TYPE);
         inputCaption.setId(stripForm(inputId) + "_caption");
 
         // render popup button
         final HtmlOutputLink link = (HtmlOutputLink) FxJsfUtils.addChildComponent(parent, HtmlOutputLink.COMPONENT_TYPE);
         link.setValue(popupLink);
         final HtmlGraphicImage button = (HtmlGraphicImage) FxJsfUtils.addChildComponent(link, HtmlGraphicImage.COMPONENT_TYPE);
         button.setUrl("/adm/images/contentEditor/findReferences.png");
         button.setStyle("border:0");
 
         // render image container (we need this since the image id attribute does not get rendered)
         final HtmlOutputText captionContainer = (HtmlOutputText) FxJsfUtils.addChildComponent(parent, HtmlOutputText.COMPONENT_TYPE);
         captionContainer.setId(stripForm(inputId) + "_preview");
 
         // render caption
         if (!value.isEmpty() && ((language != null && !value.isTranslationEmpty(language)) || language == null)) {
             final ReferencedContent reference = ((FxReference) value).getTranslation(language);
             final String caption = reference.getCaption();
             captionContainer.setValue(caption);
             inputCaption.setValue(caption);
             inputPk.setValue(reference.toString());
         }
         // render the image itself
         /*final HtmlGraphicImage image = (HtmlGraphicImage) FxJsfUtils.addChildComponent(captionContainer, HtmlGraphicImage.COMPONENT_TYPE);
         image.setStyle("border:0");
         if (!value.isEmpty() && ((language != null && !value.isTranslationEmpty(language)) || language == null)) {
             // render preview image
             final FxPK translation = referenceValue.getTranslation(language);
             image.setUrl(ThumbnailServlet.getLink(translation, BinaryDescriptor.PreviewSizes.PREVIEW2));
             hidden.setValue(translation);
         } else {
             image.setUrl("/pub/images/empty.gif");
         }*/
     }
 
     private void renderBinary(UIComponent parent, String inputId, FxLanguage language) throws IOException {
         if (!value.isEmpty() && (language == null || value.translationExists((int)language.getId()))) {
             final BinaryDescriptor descriptor = ((FxBinary) value).getTranslation(language);
             if (!descriptor.isNewBinary()) {
                 final HtmlGraphicImage image = (HtmlGraphicImage) FxJsfUtils.addChildComponent(parent, HtmlGraphicImage.COMPONENT_TYPE);
                 image.setUrl(ThumbnailServlet.getLink(XPathElement.getPK(value.getXPath()),
                         BinaryDescriptor.PreviewSizes.PREVIEW2, value.getXPath(), descriptor.getCreationTime(), language));
                 // render preview image
 /*
                 StringBuilder sb = new StringBuilder(1000);
                 String urlThumb = FxJsfUtils.getServletContext().getContextPath() +
                         ThumbnailServlet.getLink(XPathElement.getPK(value.getXPath()),
                                 BinaryDescriptor.PreviewSizes.PREVIEW2, value.getXPath(), descriptor.getCreationTime());
                 String urlOriginal = FxJsfUtils.getServerURL() + FxJsfUtils.getServletContext().getContextPath() +
                         ThumbnailServlet.getLink(XPathElement.getPK(value.getXPath()),
                                 BinaryDescriptor.PreviewSizes.ORIGINAL, value.getXPath(), descriptor.getCreationTime());
                 final String text = descriptor.getName()+", "+descriptor.getSize()+" byte"+(descriptor.isImage()
                         ? ", "+descriptor.getWidth()+"x"+descriptor.getHeight()
                         : "");
                 sb.append("<a title=\"").append(text).append("\" href=\"").append(urlOriginal).
                         append("\" rel=\"lytebox[ce]\"><img style=\"border-style:none;\" alt=\"").
                         append(text).
                         append("\" src=\"").
                         append(urlThumb).
                         append("\"/></a>");
                 writer.write(sb.toString());
 */
             }
         }
         final HtmlInputFileUpload upload = (HtmlInputFileUpload) FxJsfUtils.addChildComponent(parent, HtmlInputFileUpload.COMPONENT_TYPE);
         addHtmlAttributes(component, upload);
         upload.setId(stripForm(inputId));
         upload.setStyleClass("fxValueFileInput");
     }
 
     private void renderCheckbox(UIComponent parent, String inputId, FxLanguage language) throws IOException {
         final HtmlSelectBooleanCheckbox checkbox = (HtmlSelectBooleanCheckbox) FxJsfUtils.addChildComponent(parent, HtmlSelectBooleanCheckbox.COMPONENT_TYPE);
         checkbox.setId(stripForm(inputId));
         checkbox.setValue(value.getTranslation(language));
         addHtmlAttributes(component, checkbox);
     }
 
     private static String getTextValue(FxValue value, long languageId) {
         if (value.isEmpty()) {
             return "";
         }
         final Object writeValue = getWriteValue(value, languageId);
         //noinspection unchecked
         return value.isValid() ? value.getStringValue(writeValue) :
                 (writeValue != null ? writeValue.toString() : "");
     }
 
     private static Object getWriteValue(FxValue value, long languageId) {
         final Object writeValue;
         if (languageId != -1) {
             //noinspection unchecked
             writeValue = value.isTranslationEmpty(languageId) ? "" : value.getTranslation(languageId);
         } else {
             //noinspection unchecked
             writeValue = value.getDefaultTranslation();
         }
         return writeValue;
     }
 
     private static String stripForm(String inputId) {
         return inputId.substring(inputId.lastIndexOf(':') + 1);
     }
 
     private static String getForm(String inputId) {
         return inputId.substring(0, inputId.indexOf(':'));
     }
 
     /**
      * Renders the container for the whole fxValueInput component.
      */
     public static class ContainerWriter extends DeferredInputWriter {
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter writer = facesContext.getResponseWriter();
             writer.startElement("div", null);
             writer.writeAttribute("id", inputClientId, null);
             writer.writeAttribute("class", FxValueInputRenderer.CSS_CONTAINER + " "
                     + getInputValue().getClass().getSimpleName() + "Input", null);
         }
 
         @Override
         public void encodeEnd(FacesContext facesContext) throws IOException {
             facesContext.getResponseWriter().endElement("div");
         }
     }
 
     /**
      * Renders the container of a single language input for multilanguage input components.
      * Remember to add all elements of the language row to this component, not the parent.
      */
     public static class LanguageContainerWriter extends DeferredInputWriter {
         private long languageId;
         private String containerId;
 
         public long getLanguageId() {
             return languageId;
         }
 
         public void setLanguageId(long languageId) {
             this.languageId = languageId;
         }
 
         public String getContainerId() {
             return containerId;
         }
 
         public void setContainerId(String containerId) {
             this.containerId = containerId;
         }
 
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter writer = facesContext.getResponseWriter();
             writer.startElement("div", null);
             writer.writeAttribute("id", containerId, null);
             writer.writeAttribute("class", FxValueInputRenderer.CSS_LANG_CONTAINER, null);
             if (languageId != getInputValue().getDefaultLanguage()) {
                 writer.writeAttribute("style", "display:none", null);
             }
         }
 
         @Override
         public void encodeEnd(FacesContext facesContext) throws IOException {
             facesContext.getResponseWriter().endElement("div");
         }
 
         @Override
         public Object saveState(FacesContext context) {
             final Object[] state = new Object[3];
             state[0] = super.saveState(context);
             state[1] = languageId;
             state[2] = containerId;
             return state;
         }
 
         @Override
         public void restoreState(FacesContext context, Object stateValue) {
             final Object[] state = (Object[]) stateValue;
             super.restoreState(context, state[0]);
             this.languageId = (Long) state[1];
             this.containerId = (String) state[2];
         }
     }
 
     /**
      * Renders the language select for multilanguage components and adds a Javascript-based
      * row switcher.
      */
     public static class LanguageSelectWriter extends DeferredInputWriter {
         private List<String> rowIds;
 
         public List<String> getRowIds() {
             return rowIds;
         }
 
         public void setRowIds(List<String> rowIds) {
             this.rowIds = rowIds;
         }
 
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter writer = facesContext.getResponseWriter();
             final String languageSelectId = inputClientId + FxValueInputRenderer.LANG_SELECT;
             writer.startElement("select", null);
             writer.writeAttribute("name", languageSelectId, null);
             writer.writeAttribute("id", languageSelectId, null);
             writer.writeAttribute("class", "languages", null);
             writer.writeAttribute("onchange", "document.getElementById('" + inputClientId + "')."
                     + JS_OBJECT + ".onLanguageChanged(this)", null);
             // use the current page input language 
             final long inputLanguageId = FxJsfUtils.getManagedBean(UserConfigurationBean.class).getInputLanguageId();
             writer.startElement("option", null);
             writer.writeAttribute("value", "-2", null);
             writer.writeText(MessageBean.getInstance().getMessage("FxValueInput.language.all.short"), null);
             writer.endElement("option");
             for (FxLanguage language : FxValueInputRenderer.getLanguages()) {
                 writer.startElement("option", null);
                 writer.writeAttribute("value", language.getId(), null);
                 if ((inputLanguageId == -1 && language.getId() == getInputValue().getDefaultLanguage())
                     || (inputLanguageId == language.getId())) {
                     writer.writeAttribute("selected", "selected", null);
                 }
                 writer.writeText(language.getIso2digit(), null);
                 writer.endElement("option");
             }
             writer.endElement("select");
 
             writer.startElement("br", null);
             writer.writeAttribute("clear", "all", null);
             writer.endElement("br");
 
             // attach JS handler object to container div
             writer.write(MessageFormat.format(
                     "<script language=\"javascript\">\n"
                             + "<!--\n"
                             + "  document.getElementById(''{0}'')." + JS_OBJECT
                             + " = new FxMultiLanguageValueInput(''{0}'', ''{1}'', [{2}], ''{3}'');\n"
                             + "  document.getElementById(''{3}'').onchange();\n"
                             + "//-->\n"
                             + "</script>",
                     inputClientId, inputClientId + FxValueInputRenderer.LANG_CONTAINER,
                     StringUtils.join(rowIds.iterator(), ','),
                     languageSelectId
             ));
         }
 
         @Override
         public Object saveState(FacesContext context) {
             final Object[] state = new Object[2];
             state[0] = super.saveState(context);
             state[1] = rowIds;
             return state;
         }
 
         @SuppressWarnings({"unchecked"})
         @Override
         public void restoreState(FacesContext context, Object stateValue) {
             final Object[] state = (Object[]) stateValue;
             super.restoreState(context, state[0]);
             rowIds = (List<String>) state[1];
         }
     }
 
     /**
      * Renders a radio button to choose the default language of a multilanguage component.
      */
     public static class DefaultLanguageRadioWriter extends DeferredInputWriter {
         private long languageId;
         private String radioName;
         private String containerId;
         private String languageCode;
 
         public long getLanguageId() {
             return languageId;
         }
 
         public void setLanguageId(long languageId) {
             this.languageId = languageId;
         }
 
         public String getLanguageCode() {
             return languageCode;
         }
 
         public void setLanguageCode(String languageCode) {
             this.languageCode = languageCode;
         }
 
         public String getRadioName() {
             return radioName;
         }
 
         public void setRadioName(String radioName) {
             this.radioName = radioName;
         }
 
         public String getContainerId() {
             return containerId;
         }
 
         public void setContainerId(String containerId) {
             this.containerId = containerId;
         }
 
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter writer = facesContext.getResponseWriter();
 
             writer.startElement("input", null);
             writer.writeAttribute("type", "checkbox", null);
             writer.writeAttribute("name", radioName, null);
             writer.writeAttribute("value", languageId, null);
             writer.writeAttribute("class", "fxValueDefaultLanguageRadio", null);
             if (languageId == getInputValue().getDefaultLanguage()) {
                 writer.writeAttribute("checked", "true", null);
             }
            writer.writeAttribute("onclick", "document.getElementById('" + inputClientId
                     + "')." + JS_OBJECT + ".onDefaultLanguageChanged(this)", null);
             writer.endElement("input");

            writer.startElement("div", null);
            writer.writeAttribute("id", containerId + "_language", null);
            writer.writeAttribute("class", FxValueInputRenderer.CSS_LANG_ICON, null);
            writer.writeText(languageCode, null);
            writer.endElement("div");
         }
 
         @Override
         public Object saveState(FacesContext context) {
             final Object[] state = new Object[5];
             state[0] = super.saveState(context);
             state[1] = languageId;
             state[2] = radioName;
             state[3] = containerId;
             state[4] = languageCode;
             return state;
         }
 
         @Override
         public void restoreState(FacesContext context, Object stateValue) {
             final Object[] state = (Object[]) stateValue;
             super.restoreState(context, state[0]);
             languageId = (Long) state[1];
             radioName = (String) state[2];
             containerId = (String) state[3];
             languageCode = (String) state[4];
         }
     }
 
     /**
      * Renders a text area for plain-text or HTML input values.
      */
     public static class TextAreaWriter extends DeferredInputWriter {
         private long languageId;
 
         public long getLanguageId() {
             return languageId;
         }
 
         public void setLanguageId(long languageId) {
             this.languageId = languageId;
         }
 
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter writer = facesContext.getResponseWriter();
             final FxValue value = getInputValue();
             if (getInputComponent().isForceLineInput()) {
                 renderTextInput(getInputComponent(), this, value, inputClientId, languageId);
                 return;
             }
             writer.startElement("textarea", null);
             writer.writeAttribute("id", inputClientId, null);
             writeHtmlAttributes(getInputComponent(), writer);
             if (value instanceof FxHTML) {
                 // render tinyMCE editor
 //                    if (!FxJsfUtils.isAjaxRequest()) {
                     // when the page is updated via ajax, tinyMCE generates an additional hidden input
                     // and the text area is essential an anonymous div container (not sure why)
                     writer.writeAttribute("name", inputClientId, null);
 //                    }
                 writer.writeAttribute("class", FxValueInputRenderer.CSS_TEXTAREA_HTML, null);
                 writer.writeText(getTextValue(value, languageId), null);
                 writer.endElement("textarea");
                 writer.startElement("script", null);
                 writer.writeAttribute("type", "text/javascript", null);
                 if (FxJsfUtils.isAjaxRequest() && FxJsfUtils.getRequest().getAttribute(REQUEST_EDITORINIT) == null) {
                     // reset tinyMCE to avoid getDoc() error messages
                     writer.write("tinyMCE.idCounter = 0;\n");
                     FxJsfUtils.getRequest().setAttribute(REQUEST_EDITORINIT, true);
                 }
                 writer.write("tinyMCE.execCommand('mceAddControl', false, '" + inputClientId + "');\n");
                 if (FxJsfUtils.isAjaxRequest()) {
                     // explicitly set content for firefox, since it messes up HTML markup
                     // when populated directly from the textarea content
                     writer.write("if (tinyMCE.isGecko) {\n");
                     writer.write("    tinyMCE.execInstanceCommand('" + inputClientId + "', 'mceSetContent', false, '"
                             + FxFormatUtils.escapeForJavaScript(getTextValue(value, languageId), false, false) + "');\n");
                     writer.write("}\n");
                 }
                 writer.endElement("script");
             } else {
                 // render standard text area
                 writer.writeAttribute("name", inputClientId, null);
                 writer.writeAttribute("class", FxValueInputRenderer.CSS_TEXTAREA, null);
                 writer.writeText(getTextValue(value, languageId), null);
                 writer.endElement("textarea");
             }
         }
 
         @Override
         public Object saveState(FacesContext context) {
             final Object[] state = new Object[2];
             state[0] = super.saveState(context);
             state[1] = languageId;
             return state;
         }
 
         @Override
         public void restoreState(FacesContext context, Object stateValue) {
             final Object[] state = (Object[]) stateValue;
             super.restoreState(context, state[0]);
             languageId = (Long) state[1];
         }
     }
 }
