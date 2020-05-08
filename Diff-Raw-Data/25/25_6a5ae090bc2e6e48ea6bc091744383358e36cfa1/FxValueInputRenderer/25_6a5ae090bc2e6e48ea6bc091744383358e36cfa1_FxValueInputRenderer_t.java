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
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxUpdateException;
 import com.flexive.shared.value.*;
 import com.flexive.faces.FxJsfUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.myfaces.custom.date.HtmlInputDate;
 import org.apache.myfaces.custom.fileupload.HtmlInputFileUpload;
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.component.NamingContainer;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.Renderer;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Renderer for the FxValueInput component.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FxValueInputRenderer extends Renderer {
     private static final Log LOG = LogFactory.getLog(FxValueInputRenderer.class);
 
     protected static final String LANG_CONTAINER = "_language_";
     protected static final String DEFAULT_LANGUAGE = "_defaultLanguage";
     protected static final String LANG_SELECT = "_languageSelect";
     protected static final String INPUT = "_input_";
     protected static final String CSS_CONTAINER = "fxValueInput";
     protected static final String CSS_LANG_CONTAINER = "fxValueInputRow";
     protected static final String CSS_TEXT_INPUT = "fxValueTextInput";
     protected static final String CSS_TEXTAREA = "fxValueTextArea";
     protected static final String CSS_TEXTAREA_HTML = "fxValueTextAreaHtml";
     protected static final String CSS_INPUTELEMENTWIDTH = "fxValueInputElementWidth";
     private static final String DBG = "fxValueInput: ";
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void encodeBegin(FacesContext context, UIComponent input) throws IOException {
         FxValueInput component = (FxValueInput) input;
 
 //        if (component.calcConfigurationMask() != component.getConfigurationMask()) {
             buildComponent(context, component);
 //        }
     }
 
     public static void buildComponent(FacesContext context, FxValueInput component) {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = component.getClientId(context);
         //noinspection unchecked
         FxValue value = component.getInputMapper().encode(getFxValue(context, component));
         RenderHelper helper = component.isReadOnly()
                 ? new ReadOnlyModeHelper(component, clientId, value)
                 : new EditModeHelper(component, clientId, value);
         /*if (!component.getChildren().isEmpty()) {
             LOG.warn(DBG + "Component " + clientId + " already has " + component.getChildren().size()
                     + " children which will be discarded. Please don't use fx:fxValueInput inside render-time tags like ui:repeat.");
         }*/
         component.getChildren().clear();
         if (!(value instanceof FxVoid)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug(DBG + "Rendering " + (component.isReadOnly() ? "read only" : "editable")
                     + " component " + clientId + " for value=" + value);
             }
             try {
                 helper.render();
             } catch (IOException e) {
                 throw new IllegalStateException(e);
             }
         }
         component.resetConfigurationMask();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void decode(FacesContext context, UIComponent component) {
         FxValueInput input = (FxValueInput) component;
         buildComponent(context, input);
         if (!(input.getValue() instanceof FxVoid) && !input.isReadOnly()) {
             input.setSubmittedValue(decodeFxValue(context, component));
         }
     }
 
     /**
      * Return the FxValue used as input for the component.
      *
      * @param context the faces context
      * @param input   the FxValueInput component @return the FxValue used as input for the component.
      * @return the FxValue stored in the input component
      */
     private static FxValue getFxValue(FacesContext context, UIInput input) {
         Object o = input.getSubmittedValue() != null ? input.getSubmittedValue() : input.getValue();
         if (o == null) {
             throw new FxInvalidParameterException("VALUE", "ex.jsf.valueInput.null", input.getClientId(context)).asRuntimeException();
         } else if (!(o instanceof FxValue)) {
             throw new FxInvalidParameterException("VALUE", "ex.jsf.valueInput.invalidType",
                     o.getClass().getCanonicalName()).asRuntimeException();
         }
         return (FxValue) o;
     }
 
     /**
      * Decode FxValue items posted for this component.
      *
      * @param context   the current faces context
      * @param component the component to be rendered
      * @return FxValue items posted for this component.
      */
     private FxValue decodeFxValue(FacesContext context, UIComponent component) {
         final FxValueInput input = (FxValueInput) component;
         final Map parameters = context.getExternalContext().getRequestParameterMap();
         final Map parameterValues = context.getExternalContext().getRequestParameterValuesMap();
         final String clientId = component.getClientId(context);
         final FxValue value = getFxValue(context, input).copy();
 
         if (LOG.isDebugEnabled()) {
             LOG.debug(DBG + "Decoding value for " + clientId + ", component value=" + value);
         }
         if (value.isMultiLanguage() && !input.isDisableMultiLanguage()) {
             final int defaultLanguageId = Integer.parseInt((String) parameters.get(clientId + DEFAULT_LANGUAGE));
             value.setDefaultLanguage(defaultLanguageId, true);
             // update all languages
             for (FxLanguage language : getLanguages()) {
                 final String inputId = clientId + INPUT + language.getId();
                 updateTranslation(context, input, value, inputId, language.getId(), parameters, parameterValues);
             }
         } else {
             updateTranslation(context, input, value, clientId + FxValueInputRenderer.INPUT, value.getDefaultLanguage(), parameters, parameterValues);
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug(DBG + "Decoded value for " + clientId + ": " + value);
         }
         return value;
     }
 
     @SuppressWarnings({"unchecked"})
     private void updateTranslation(FacesContext context, FxValueInput input, FxValue value, String inputId, long languageId, Map parameters, Map parameterValues) {
         if (value instanceof FxSelectMany) {
             final String selectedOptions = StringUtils.join((String[]) parameterValues.get(inputId), ',');
             if (StringUtils.isNotBlank(selectedOptions)) {
                 // update selection
                 value.setTranslation(languageId, selectedOptions);
            } else if (value.getTranslation(languageId) != null) {
                 // remove all selected items
                ((FxSelectMany) value).getTranslation(languageId).deselectAll();
             }
         } else if (value instanceof FxDate || value instanceof FxDateTime) {
             // get date value from dateinput child
             //noinspection unchecked
             final HtmlInputDate dateInput = (HtmlInputDate) input.findComponent(stripNamingContainers(inputId));
             if (dateInput == null) {
                 throw new FxUpdateException(LOG, "ex.jsf.valueInput.date.input", inputId, value).asRuntimeException();
             }
             dateInput.processDecodes(context);
             HtmlInputDate.UserData data = (HtmlInputDate.UserData) dateInput.getSubmittedValue();
             if (data != null) {
                 try {
                     final Date date = data.parse();
                     if (date != null) {
                         value.setTranslation(languageId, date);
                     } else {
                         value.setEmpty(languageId);
                     }
                 } catch (ParseException e) {
                     // keep old value
                 }
             }
         } else if (value instanceof FxBinary) {
             final HtmlInputFileUpload upload = (HtmlInputFileUpload) input.findComponent(stripNamingContainers(inputId));
             if (upload != null) {
                 try {
                     upload.processDecodes(context);
                     final UploadedFile file = (UploadedFile) upload.getSubmittedValue();
                     if (file != null && file.getSize() > 0) {
                         //noinspection unchecked
 
                         String name = file.getName();
                         if (name.indexOf('\\') > 0)
                             name = name.substring(name.lastIndexOf('\\') + 1);
                         value.setTranslation(languageId, new BinaryDescriptor(name, file.getSize(), file.getInputStream()));
                     }
                 } catch (Exception e) {
                     throw new FxUpdateException(LOG, e, "ex.jsf.valueInput.file.upload.io", e).asRuntimeException();
                 }
             }
         } else if (value instanceof FxBoolean) {
             value.setTranslation(languageId, StringUtils.isNotBlank((String) parameters.get(inputId)));
         } else {
             final String postedValue = (String) parameters.get(inputId);
             if (StringUtils.isNotBlank(postedValue)) {
                 value.setTranslation(languageId, postedValue);
             } else {
                 value.removeLanguage(languageId);
             }
         }
     }
 
     /**
      * Load all available languages.
      *
      * @return all available languages.
      */
     protected static List<FxLanguage> getLanguages() {
         List<FxLanguage> languages;
         try {
             languages = EJBLookup.getLanguageEngine().loadAvailable(true);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         return languages;
     }
 
     /**
      * Strips all naming containers from our client ID (e.g. forms, iterator components).
      * This is usful for finding components by id within our FxValueInput component.
      *
      * @param clientId  the client ID
      * @return  the client ID without naming containers
      */
     private static String stripNamingContainers(String clientId) {
         return clientId.indexOf(NamingContainer.SEPARATOR_CHAR) != -1
                 ? clientId.substring(clientId.lastIndexOf(NamingContainer.SEPARATOR_CHAR) + 1) : clientId;
     }
 }
