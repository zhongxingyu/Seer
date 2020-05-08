 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 import com.flexive.faces.javascript.FxJavascriptUtils;
 import com.flexive.faces.beans.UserConfigurationBean;
 import com.flexive.faces.beans.MessageBean;
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.*;
 import com.flexive.war.servlet.ThumbnailServlet;
 import com.flexive.war.FxRequest;
 import com.flexive.war.JsonWriter;
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
 import java.io.StringWriter;
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Renders an FxValueInput component in edit mode.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 class EditModeHelper extends RenderHelper {
     private static final String REQUEST_EDITORINIT = "REQUEST_EDITORINIT";
     private static final String JS_OBJECT = "fxValue";
 
     private boolean multiLine = false;
     private boolean useHTMLEditor = false;
     private int rows = -1;
 
     protected FxEnvironment environment;
 
     public EditModeHelper(FxValueInput component, String clientId, FxValue value) {
         super(component, clientId, value);
         environment = CacheAdmin.getEnvironment();
         if (value != null && StringUtils.isNotBlank(value.getXPath()) && value instanceof FxString) {
             if (CacheAdmin.getEnvironment().assignmentExists(value.getXPath())) {
                 FxPropertyAssignment pa = (FxPropertyAssignment) environment.getAssignment(value.getXPath());
                 multiLine = pa.isMultiLine();
                 if (multiLine) {
                     rows = pa.getMultiLines();
                     if (rows <= 1)
                         rows = -1;
                 }
                 useHTMLEditor = pa.getOption(FxStructureOption.OPTION_HTML_EDITOR).isValueTrue();
             }
             else if (CacheAdmin.getEnvironment().propertyExists(value.getXPath())) {
                 FxProperty p = CacheAdmin.getEnvironment().getProperty(value.getXPath());
                 multiLine = p.getOption(FxStructureOption.OPTION_MULTILINE).isValueTrue();
                 if (multiLine) {
                     rows = p.getMultiLines();
                     if (rows <= 1)
                         rows = -1;
                 }
                 useHTMLEditor = p.getOption(FxStructureOption.OPTION_HTML_EDITOR).isValueTrue();
             }
         }
         if (useHTMLEditor && !(value instanceof FxString))
             useHTMLEditor = false; //prevent showing HTML editor for non-string types
 
         if (value instanceof FxHTML && !useHTMLEditor) {
             //if no xpath is available, always show the HTML editor for FxHTML values
             if (StringUtils.isEmpty(value.getXPath()))
                 useHTMLEditor = true;
         }
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
 
         final List<UIComponent> rows = new ArrayList<UIComponent>();
         final HashMap<Long, LanguageSelectWriter.InputRowInfo> rowInfos =
                 new HashMap<Long, LanguageSelectWriter.InputRowInfo>(languages.size());
         for (final FxLanguage language : languages) {
             final String containerId = clientId + FxValueInputRenderer.LANG_CONTAINER + language.getId();
             final String inputId = clientId + FxValueInputRenderer.INPUT + language.getId();
             rowInfos.put(language.getId(), new LanguageSelectWriter.InputRowInfo(containerId, inputId));
 
             final LanguageContainerWriter languageContainer = new LanguageContainerWriter();
             languageContainer.setContainerId(containerId);
             languageContainer.setLanguageId(language.getId());
             rows.add(languageContainer);
 
             encodeField(languageContainer, inputId, language);
             encodeDefaultLanguageRadio(languageContainer, clientId, radioName, language);
         }
 
         final LanguageSelectWriter languageSelect = new LanguageSelectWriter();
         languageSelect.setInputClientId(clientId);
         languageSelect.setRowInfos(rowInfos);
         languageSelect.setDefaultLanguageId(value.getDefaultLanguage());
         container.getChildren().add(languageSelect);
         // add children to language select because the language select needs to write code before and after the input rows
         languageSelect.getChildren().addAll(rows);
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
         if (language == null) {
             final ContainerWriter container = new ContainerWriter();
             container.setInputClientId(clientId);
             parent.getChildren().add(container);
             // use container as parent for all subsequent operations
             parent = container;
         }
         if (useHTMLEditor || multiLine) {
             renderTextArea(parent, inputId, language, rows, useHTMLEditor);
         } else if (value instanceof FxSelectOne) {
             renderSelectOne(parent, inputId, language);
         } else if (value instanceof FxSelectMany) {
             renderSelectMany(parent, inputId, language);
         } else if (value instanceof FxDate) {
             renderDateInput(parent, inputId, language);
         } else if (value instanceof FxDateTime) {
             renderDateTimeInput(parent, inputId, language);
         } else if (value instanceof FxDateRange) {
             renderDateRangeInput(parent, inputId, language);
         } else if (value instanceof FxDateTimeRange) {
             renderDateTimeRangeInput(parent, inputId, language);
         } else if (value instanceof FxReference) {
             renderReferenceSelect(parent, inputId, language);
         } else if (value instanceof FxBinary) {
             renderBinary(parent, inputId, language);
         } else if (value instanceof FxBoolean) {
             renderCheckbox(parent, inputId, language);
         } else {
             renderTextInput(parent, inputId, language);
         }
     }
 
     private void renderTextInput(UIComponent parent, String inputId, FxLanguage language) throws IOException {
         renderTextInput(component, parent, value, inputId, language != null ? language.getId() : -1);
         if (getInputValue(component) instanceof FxReference) {
             // add a browse reference popup button
             renderReferencePopupButton(parent, inputId);
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
         input.setStyleClass(FxValueInputRenderer.CSS_TEXT_INPUT + singleLanguageStyle(languageId));
 
         // add autocomplete YUI component
         if (StringUtils.isNotBlank(inputComponent.getAutocompleteHandler())) {
             final YuiAutocompleteWriter yuiWriter = new YuiAutocompleteWriter();
             yuiWriter.setInputClientId(inputId);
             yuiWriter.setAutocompleteHandler(inputComponent.getAutocompleteHandler());
             parent.getChildren().add(parent.getChildren().size() - 1, yuiWriter);
         }
     }
 
     private void renderTextArea(UIComponent parent, final String inputId, final FxLanguage language, final int rows, final boolean useHTMLEditor) throws IOException {
         final TextAreaWriter textArea = new TextAreaWriter();
         textArea.setInputClientId(inputId);
         textArea.setLanguageId(language != null ? language.getId() : -1);
         textArea.setRows(rows);
         textArea.setUseHTMLEditor(useHTMLEditor);
         parent.getChildren().add(textArea);
     }
 
     /**
      * Render additional HTML attributes passed to the FxValueInput component.
      *
      * @param component the input component
      * @param writer    the output writer
      * @throws IOException if the output could not be written
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
         listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH + singleLanguageStyle(language));
         // update posted value
         if (selectValue.getTranslation(language) != null) {
             listbox.setValue(selectValue.getTranslation(language).getId());
         }
         storeSelectItems(listbox, selectValue.getSelectList());
     }
 
     private void renderSelectMany(UIComponent parent, String inputId, FxLanguage language) {
         final FxSelectMany selectValue = (FxSelectMany) value;
         final SelectMany sm = selectValue.getTranslation(language) != null ? selectValue.getTranslation(language) : new SelectMany(selectValue.getSelectList());
         final Long[] selected = new Long[sm.getSelected().size()];
         for (int i = 0; i < selected.length; i++) {
             selected[i] = sm.getSelected().get(i).getId();
         }
         if (component.isForceLineInput()) {
             // render a single line dropdown
             final HtmlSelectOneListbox listbox = (HtmlSelectOneListbox) createUISelect(parent, inputId, HtmlSelectOneListbox.COMPONENT_TYPE);
             listbox.setSize(1);
             listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH + singleLanguageStyle(language));
             if (selected.length > 0) {
                 // choose first selected element - other selections get discarded
                 listbox.setValue(selected[0]);
             }
             storeSelectItems(listbox, selectValue.getSelectList());
         } else {
             // render a "multiple" select list
             final HtmlSelectManyListbox listbox = (HtmlSelectManyListbox) createUISelect(parent, inputId, HtmlSelectManyListbox.COMPONENT_TYPE);
             listbox.setStyleClass(FxValueInputRenderer.CSS_INPUTELEMENTWIDTH + singleLanguageStyle(language));
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
         if (selectList == null) {
             throw new FxInvalidParameterException("selectList", "ex.jsf.valueInput.select.emptyList",
                     component.getClientId(FacesContext.getCurrentInstance())).asRuntimeException();
         }
         // store available items in select component
         final UISelectItems selectItems = (UISelectItems) FxJsfUtils.createComponent(UISelectItems.COMPONENT_TYPE);
         final List<SelectItem> items = FxJsfUtils.asSelectList(selectList);
         Collections.sort(items, new FxJsfUtils.SelectItemSorter());
         selectItems.setValue(items);
         listbox.setConverter(FxJsfUtils.getApplication().createConverter(Long.class));
         listbox.getChildren().add(selectItems);
     }
 
     private void renderDateInput(UIComponent parent, String inputId, FxLanguage language) {
         final Date date = value.isTranslationEmpty(language) ? null : (Date) value.getTranslation(language);
         createDateInput(parent, inputId, date);
     }
 
     private void createDateInput(UIComponent parent, String inputId, Date date) {
         final HtmlInputText input = (HtmlInputText) FxJsfUtils.addChildComponent(parent, HtmlInputText.COMPONENT_TYPE);
         input.setId(stripForm(inputId));
         input.setSize(10);
         input.setValue(date == null ? "" : FxFormatUtils.toString(date));
 
         //createInputDate(parent, inputId, language);
         final HtmlGraphicImage img = (HtmlGraphicImage) FxJsfUtils.addChildComponent(parent, HtmlGraphicImage.COMPONENT_TYPE);
         img.setId("calendarButton_" + stripForm(inputId));
         img.setUrl(FxJsfUtils.getWebletURL("com.flexive.faces.weblets", "/images/calendar.gif"));
         img.setStyleClass("button");
 
         final YuiDateInputWriter diw = new YuiDateInputWriter();
         diw.setInputClientId(inputId);
         diw.setButtonId(img.getClientId(FacesContext.getCurrentInstance()));
         diw.setDate(date);
         parent.getChildren().add(diw);
     }
 
     private void renderDateRangeInput(UIComponent parent, String inputId, FxLanguage language) {
         final DateRange range = value.isTranslationEmpty(language) ? null : (DateRange) value.getTranslation(language);
         createDateInput(parent, inputId + "_1", range != null ? range.getLower() : null);
         renderLiteral(parent, " - ");
         createDateInput(parent, inputId + "_2", range != null ? range.getUpper() : null);
     }
 
 
     private void renderDateTimeInput(UIComponent parent, String inputId, FxLanguage language) {
 //        createInputDate(parent, inputId, language).setType("full");
 
         final Date date = value.isTranslationEmpty(language) ? null : (Date) value.getTranslation(language);
         createDateTimeInput(parent, inputId, date);
     }
 
     private void createDateTimeInput(UIComponent parent, String inputId, Date date) {
         createDateInput(parent, inputId, date);
         final Calendar cal = date != null ? Calendar.getInstance() : null;
         if (cal != null) {
             cal.setTime(date);
         }
         renderTimeInput(parent, inputId, "_hh", cal != null ? cal.get(Calendar.HOUR_OF_DAY) : -1);
         renderLiteral(parent, ":");
         renderTimeInput(parent, inputId, "_mm", cal != null ? cal.get(Calendar.MINUTE) : -1);
         renderLiteral(parent, ":");
         renderTimeInput(parent, inputId, "_ss", cal != null ? cal.get(Calendar.SECOND) : -1);
     }
 
     private void renderTimeInput(UIComponent parent, String inputId, String suffix, int value) {
         final HtmlInputText input = (HtmlInputText) FxJsfUtils.addChildComponent(parent, HtmlInputText.COMPONENT_TYPE);
         input.setId(stripForm(inputId) + suffix);
         input.setSize(2);
         input.setMaxlength(2);
         if (value != -1) {
             input.setValue(new Formatter().format("%02d", value));
         }
     }
 
     private void renderDateTimeRangeInput(UIComponent parent, String inputId, FxLanguage language) {
         final DateRange range = value.isTranslationEmpty(language) ? null : (DateRange) value.getTranslation(language);
         createDateTimeInput(parent, inputId + "_1", range != null ? range.getLower() : null);
         renderLiteral(parent, " -<br/>").setEscape(false);
         createDateTimeInput(parent, inputId + "_2", range != null ? range.getUpper() : null);
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
         // render hidden input that contains the actual reference
         final HtmlInputHidden inputPk = (HtmlInputHidden) FxJsfUtils.addChildComponent(parent, HtmlInputHidden.COMPONENT_TYPE);
         inputPk.setId(stripForm(inputId));
         // render hidden input where the caption is stored
         final HtmlInputHidden inputCaption = (HtmlInputHidden) FxJsfUtils.addChildComponent(parent, HtmlInputHidden.COMPONENT_TYPE);
         inputCaption.setId(stripForm(inputId) + "_caption");
 
         // render popup button
         renderReferencePopupButton(parent, inputId);
 
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
             image.setUrl(ThumbnailServlet.getUrl(translation, BinaryDescriptor.PreviewSizes.PREVIEW2));
             hidden.setValue(translation);
         } else {
             image.setUrl("/pub/images/empty.gif");
         }*/
     }
 
     private void renderReferencePopupButton(UIComponent parent, String inputId) {
         final HtmlOutputLink link = (HtmlOutputLink) FxJsfUtils.addChildComponent(parent, HtmlOutputLink.COMPONENT_TYPE);
         link.setValue("javascript:flexive.input.openReferenceQueryPopup('" + StringUtils.defaultString(value.getXPath()) + "', '"
                 + inputId + "', '" + getForm(inputId) + "')");
         final HtmlGraphicImage button = (HtmlGraphicImage) FxJsfUtils.addChildComponent(link, HtmlGraphicImage.COMPONENT_TYPE);
         button.setUrl(FxJsfUtils.getWebletURL("com.flexive.faces.weblets", "/images/findReferences.png"));
         button.setStyle("border:0");
         button.setStyleClass(FxValueInputRenderer.CSS_FIND_REFERENCES);
     }
 
     private void renderBinary(UIComponent parent, String inputId, FxLanguage language) throws IOException {
         if (!value.isEmpty() && (language == null || value.translationExists((int) language.getId()))) {
             final BinaryDescriptor descriptor = ((FxBinary) value).getTranslation(language);
             if (!descriptor.isNewBinary()) {
                 final HtmlGraphicImage image = (HtmlGraphicImage) FxJsfUtils.addChildComponent(parent, HtmlGraphicImage.COMPONENT_TYPE);
                 image.setUrl(ThumbnailServlet.getLink(XPathElement.getPK(value.getXPath()),
                         BinaryDescriptor.PreviewSizes.PREVIEW2, value.getXPath(), descriptor.getCreationTime(), language));
                 if (component.isReadOnlyShowTranslations()) {
                     //TODO: might add another attribute to indicate if description should be visible
                     image.setStyle("padding: 5px;");
                     addImageDescriptionComponent(parent, language);
                 }
             } else
                 addImageDescriptionComponent(parent, language);
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
 
     private HtmlOutputText renderLiteral(UIComponent parent, String value) {
         HtmlOutputText output = (HtmlOutputText) FxJsfUtils.addChildComponent(parent, HtmlOutputText.COMPONENT_TYPE);
         output.setValue(value);
         return output;
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
             writeValue = value.isTranslationEmpty(languageId) ? value.getEmptyValue() : value.getTranslation(languageId);
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
 
     private static String singleLanguageStyle(long languageId) {
         return (languageId == -1 ? " " + FxValueInputRenderer.CSS_SINGLE_LANG : "");
     }
 
     private static String singleLanguageStyle(FxLanguage language) {
         return singleLanguageStyle(language != null ? language.getId() : -1);
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
         public static class InputRowInfo implements Serializable {
             private static final long serialVersionUID = -2146282630839499609L;
             
             private final String rowId;
             private final String inputId;
 
             public InputRowInfo(String rowId, String inputId) {
                 this.rowId = rowId;
                 this.inputId = inputId;
             }
 
             public String getRowId() {
                 return rowId;
             }
 
             public String getInputId() {
                 return inputId;
             }
         }
 
         private Map<Long, InputRowInfo> rowInfos;
         private String languageSelectId;
         private long defaultLanguageId;
 
         public Map<Long, InputRowInfo> getRowInfos() {
             return rowInfos;
         }
 
         public void setRowInfos(Map<Long, InputRowInfo> rowInfos) {
             this.rowInfos = rowInfos;
         }
 
         public void setDefaultLanguageId(long defaultLanguageId) {
             this.defaultLanguageId = defaultLanguageId;
         }
 
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter writer = facesContext.getResponseWriter();
             languageSelectId = inputClientId + FxValueInputRenderer.LANG_SELECT;
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
 
 //            writer.startElement("br", null);
 //            writer.writeAttribute("clear", "all", null);
 //            writer.endElement("br");
         }
 
         @Override
         public void encodeEnd(FacesContext facesContext) throws IOException {
             final ResponseWriter writer = facesContext.getResponseWriter();
             // attach JS handler object to container div
             final JsonWriter jsonWriter = new JsonWriter().startMap();
             for (Map.Entry<Long, InputRowInfo> entry : rowInfos.entrySet()) {
                 jsonWriter.startAttribute(String.valueOf(entry.getKey()))
                         .startMap()
                         .writeAttribute("rowId", entry.getValue().getRowId())
                         .writeAttribute("inputId", entry.getValue().getInputId())
                         .closeMap();
             }
             jsonWriter.closeMap().finishResponse();
             writer.write(MessageFormat.format(
                     "<script language=\"javascript\">\n"
                             + "<!--\n"
                             + "  document.getElementById(''{0}'')." + JS_OBJECT
                             + " = new flexive.input.FxMultiLanguageValueInput(''{0}'', ''{1}'', {2}, ''{3}'', ''{4}'');\n"
                             + "  document.getElementById(''{3}'').onchange();\n"
                             + "//-->\n"
                             + "</script>",
                     inputClientId, inputClientId + FxValueInputRenderer.LANG_CONTAINER,
                     jsonWriter.toString(),
                     languageSelectId,
                     defaultLanguageId
             ));
         }
 
         @Override
         public Object saveState(FacesContext context) {
             final Object[] state = new Object[3];
             state[0] = super.saveState(context);
             state[1] = rowInfos;
             state[2] = defaultLanguageId;
             return state;
         }
 
         @SuppressWarnings({"unchecked"})
         @Override
         public void restoreState(FacesContext context, Object stateValue) {
             final Object[] state = (Object[]) stateValue;
             super.restoreState(context, state[0]);
             rowInfos = (Map<Long, InputRowInfo>) state[1];
             defaultLanguageId = (Long) state[2];
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
                     + "')." + JS_OBJECT + ".onDefaultLanguageChanged(this, " + languageId + ")", null);
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
         private int rows = -1;
         private boolean useHTMLEditor = false;
 
         public long getLanguageId() {
             return languageId;
         }
 
         public void setLanguageId(long languageId) {
             this.languageId = languageId;
         }
 
         public void setRows(int rows) {
             this.rows = rows;
         }
 
        public int getRows() {
            return rows;
        }

         public void setUseHTMLEditor(boolean useHTMLEditor) {
             this.useHTMLEditor = useHTMLEditor;
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
             if (useHTMLEditor) {
                 // render tinyMCE editor
                 writer.writeAttribute("name", inputClientId, null);
                 writer.writeAttribute("class", FxValueInputRenderer.CSS_TEXTAREA_HTML + singleLanguageStyle(languageId), null);
                 writer.writeText(getTextValue(value, languageId), null);
                 writer.endElement("textarea");
                 final FxRequest request = FxJsfUtils.getRequest();
                 writer.startElement("script", null);
                 writer.writeAttribute("type", "text/javascript", null);
                 if (FxJsfUtils.isAjaxRequest() && request.getAttribute(REQUEST_EDITORINIT) == null) {
                     // reset tinyMCE to avoid getDoc() error messages
                     writer.write("tinyMCE.idCounter = 0;\n");
                     request.setAttribute(REQUEST_EDITORINIT, true);
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
                 writer.writeAttribute("class", FxValueInputRenderer.CSS_TEXTAREA + singleLanguageStyle(languageId), null);
                 if (rows > 0)
                     writer.writeAttribute("rows", String.valueOf(rows), null);
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
 
     public static class YuiAutocompleteWriter extends DeferredInputWriter {
         private String autocompleteHandler;
 
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter out = facesContext.getResponseWriter();
             // write autocomplete container
             final String containerId = inputClientId + "_ac";
             out.write("<div id=\"" + containerId + "\" class=\"fxValueInputAutocomplete\"> </div>");
 
             // initialize autocomplete
             FxJavascriptUtils.beginJavascript(out);
             FxJavascriptUtils.writeYahooRequires(out, "autocomplete");
             FxJavascriptUtils.onYahooLoaded(out,
                     "function() {\n"
                             + "    var handler = eval('(' + \"" + StringUtils.replace(autocompleteHandler, "\"", "\\\"") + "\" + ')');\n"
                             + "    var ds = handler.getDataSource();\n"
                             + "    var ac = new YAHOO.widget.AutoComplete('" + inputClientId + "', '" + containerId + "', ds);\n"
                             + "    ac.formatResult = handler.formatResult;\n"
                             + "    ac.forceSelection = false;\n"
                             + "}"
             );
             FxJavascriptUtils.endJavascript(out);
         }
 
         public String getAutocompleteHandler() {
             return autocompleteHandler;
         }
 
         public void setAutocompleteHandler(String autocompleteHandler) {
             this.autocompleteHandler = autocompleteHandler;
         }
 
         @Override
         public Object saveState(FacesContext context) {
             final Object[] state = new Object[2];
             state[0] = super.saveState(context);
             state[1] = autocompleteHandler;
             return state;
         }
 
         @Override
         public void restoreState(FacesContext context, Object stateValue) {
             final Object[] state = (Object[]) stateValue;
             super.restoreState(context, state[0]);
             autocompleteHandler = (String) state[1];
         }
     }
 
     public static class YuiDateInputWriter extends DeferredInputWriter {
         private String buttonId;
         private Date date;
 
         public String getButtonId() {
             return buttonId;
         }
 
         public void setButtonId(String buttonId) {
             this.buttonId = buttonId;
         }
 
         public Date getDate() {
             return date != null ? (Date) date.clone() : null;
         }
 
         public void setDate(Date date) {
             this.date = date != null ? (Date) date.clone() : null;
         }
 
         @Override
         public void encodeBegin(FacesContext facesContext) throws IOException {
             final ResponseWriter out = facesContext.getResponseWriter();
             final String containerId = "cal_" + inputClientId;
             out.write("<div id=\"" + containerId + "\" class=\"popupCalendar\"> </div>\n");
             FxJavascriptUtils.beginJavascript(out);
             FxJavascriptUtils.writeYahooRequires(out, "calendar");
             FxJavascriptUtils.onYahooLoaded(out,
                     "function() {\n"
                             + "    var button = document.getElementById('" + buttonId + "');\n"
                             + "    var container = document.getElementById('" + containerId + "');\n"
                             + "    var input = document.getElementById('" + inputClientId + "');\n"
                             + "    var date = " + (date != null ? "'" + new SimpleDateFormat("M/d/yyyy").format(date) + "'" : "''") + ";\n"
                             + "    var pdate = " + (date != null ? "'" + new SimpleDateFormat("M/yyyy").format(date) + "'" : "''") + ";\n"
                             + "    var cal = new YAHOO.widget.Calendar('" + containerId + "', '" + containerId + "', \n"
                             + "                  { navigator: true, close: true, title: '"
                             + MessageBean.getInstance().getResource("FxValueInput.datepicker.title")
                             + "', selected: date, pagedate: pdate });\n"
                             + "    cal.selectEvent.subscribe(function(type, args, obj) {\n"
                             + "             var date = args[0][0];\n"
                             // YYYY/MM/DD
                             + "             input.value = flexive.util.zeroPad(date[0], 4) + '-' "
                             + " + flexive.util.zeroPad(date[1], 2) + '-' + flexive.util.zeroPad(date[2], 2);\n"
                             + "             cal.hide();\n"
                             + "         }, cal, true);\n"
                             + "    cal.render();\n"
                             + "    var Dom = YAHOO.util.Dom;\n"
                             + "    YAHOO.util.Event.on('" + buttonId + "', 'click', \n"
                             + "          function() { cal.show(); Dom.setXY(container, Dom.getXY(button)); }, cal, true);\n"
                             + "}"
             );
             FxJavascriptUtils.endJavascript(out);
         }
 
         @Override
         public Object saveState(FacesContext context) {
             final Object[] state = new Object[3];
             state[0] = super.saveState(context);
             state[1] = buttonId;
             state[2] = date;
             return state;
         }
 
         @Override
         public void restoreState(FacesContext context, Object stateValue) {
             final Object[] state = (Object[]) stateValue;
             super.restoreState(context, state[0]);
             buttonId = (String) state[1];
             date = (Date) state[2];
         }
     }
 }
