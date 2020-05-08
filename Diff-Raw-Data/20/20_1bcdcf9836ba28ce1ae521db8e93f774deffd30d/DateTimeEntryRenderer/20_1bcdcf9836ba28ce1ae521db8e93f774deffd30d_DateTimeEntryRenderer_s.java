 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: (ICE-6978) Used JSONBuilder to add the functionality of escaping JS output.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.datetimeentry;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 
 import org.icefaces.ace.renderkit.InputRenderer;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="dateTimeEntry", value="org.icefaces.ace.component.datetimeentry.DateTimeEntry")
 public class DateTimeEntryRenderer extends InputRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
 //        printParams();
         DateTimeEntry dateTimeEntry = (DateTimeEntry) component;
 
         if(dateTimeEntry.isDisabled() || dateTimeEntry.isReadonly()) {
             return;
         }
 
         String param = dateTimeEntry.getClientId(context) + "_input";
         String submittedValue = context.getExternalContext().getRequestParameterMap().get(param);
 
         if(submittedValue != null) {
             dateTimeEntry.setSubmittedValue(submittedValue);
         }
 
         decodeBehaviors(context, dateTimeEntry);
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         DateTimeEntry dateTimeEntry = (DateTimeEntry) component;
         String value = DateTimeEntryUtils.getValueAsString(context, dateTimeEntry);
 
         encodeMarkup(context, dateTimeEntry, value);
         encodeScript(context, dateTimeEntry, value);
     }
 
     protected void encodeMarkup(FacesContext context, DateTimeEntry dateTimeEntry, String value) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = dateTimeEntry.getClientId(context);
         String inputId = clientId + "_input";
         boolean popup = dateTimeEntry.isPopup();
 
         writer.startElement("span", dateTimeEntry);
         writer.writeAttribute("id", clientId, null);
         String style = dateTimeEntry.getStyle();
         if(style != null) writer.writeAttribute("style", style, null);
         String styleClass = dateTimeEntry.getStyleClass();
         if(styleClass != null) writer.writeAttribute("class", styleClass, null);
 
         //inline container
         if(!popup) {
             writer.startElement("div", null);
             writer.writeAttribute("id", clientId + "_inline", null);
             writer.endElement("div");
         }
 
         //input
         String type = popup ? "text" : "hidden";
 
         writer.startElement("input", null);
         writer.writeAttribute("id", inputId, null);
         writer.writeAttribute("name", inputId, null);
         writer.writeAttribute("type", type, null);
 		writer.writeAttribute("tabindex", dateTimeEntry.getTabindex(), null);
 
         if(!isValueBlank(value)) {
             writer.writeAttribute("value", value, null);
         }
 
         if(popup) {
             if(themeForms()) writer.writeAttribute("class", DateTimeEntry.INPUT_STYLE_CLASS, null);
             if(dateTimeEntry.isReadOnlyInputText()) writer.writeAttribute("readonly", "readonly", null);
             if(dateTimeEntry.isDisabled()) writer.writeAttribute("disabled", "disabled", null);
 
             renderPassThruAttributes(context, dateTimeEntry, HTML.INPUT_TEXT_ATTRS);
         }
 
         writer.endElement("input");
 
         writer.endElement("span");
     }
 
     protected void encodeScript(FacesContext context, DateTimeEntry dateTimeEntry, String value) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = dateTimeEntry.getClientId(context);
        
         writer.startElement("script", null);
         writer.writeAttribute("type", "text/javascript", null);
 
         String showOn = dateTimeEntry.getShowOn();
         StringBuilder script = new StringBuilder();
         JSONBuilder json = JSONBuilder.create();
 
         script.append("ice.ace.jq(function(){").append(resolveWidgetVar(dateTimeEntry)).append(" = new ");
         json.beginFunction("ice.ace.Calendar").
             item(clientId).
             beginMap().
                 entry("popup", dateTimeEntry.isPopup()).
                 entry("locale", dateTimeEntry.calculateLocale(context).toString());
                if(!isValueBlank(value)) json.entry("defaultDate", value);
                 json.entryNonNullValue("pattern", DateTimeEntryUtils.convertPattern(dateTimeEntry.getPattern()));
                 if(dateTimeEntry.getPages() != 1) json.entry("numberOfMonths", dateTimeEntry.getPages());
                 json.entryNonNullValue("minDate", DateTimeEntryUtils.getDateAsString(dateTimeEntry, dateTimeEntry.getMindate())).
                 entryNonNullValue("maxDate", DateTimeEntryUtils.getDateAsString(dateTimeEntry, dateTimeEntry.getMaxdate()));
                 json.entryNonNullValue("showButtonPanel", dateTimeEntry.isShowButtonPanel());
                 if(dateTimeEntry.isShowWeek()) json.entry("showWeek", true);
                 if(dateTimeEntry.isDisabled()) json.entry("disabled", true);
                 json.entryNonNullValue("yearRange", dateTimeEntry.getYearRange());
                 if(dateTimeEntry.isNavigator()) {
                     json.entry("changeMonth", true).
                     entry("changeYear", true);
                 }
 
                 if(dateTimeEntry.getEffect() != null) {
                     json.entry("showAnim", dateTimeEntry.getEffect()).
                     entry("duration", dateTimeEntry.getEffectDuration());
                 }
                 if(!showOn.equalsIgnoreCase("focus")) {
                     String iconSrc = dateTimeEntry.getPopupIcon() != null ? getResourceURL(context, dateTimeEntry.getPopupIcon()) : getResourceRequestPath(context, DateTimeEntry.POPUP_ICON);
 
                     json.entry("showOn", showOn).
                     entry("buttonImage", iconSrc).
                     entry("buttonImageOnly", dateTimeEntry.isPopupIconOnly());
                 }
 
                 if(dateTimeEntry.isShowOtherMonths()) {
                     json.entry("showOtherMonths", true).
                     entry("selectOtherMonths", dateTimeEntry.isSelectOtherMonths());
                 }
 
                 //time
                 if(dateTimeEntry.hasTime()) {
                    json.entry("timeOnly", dateTimeEntry.isTimeOnly()).
        
                     //step
                     entry("stepHour", dateTimeEntry.getStepHour()).
                     entry("stepMinute", dateTimeEntry.getStepMinute()).
                     entry("stepSecond", dateTimeEntry.getStepSecond()).
                     
                     //minmax
                     entry("hourMin", dateTimeEntry.getMinHour()).
                     entry("hourMax", dateTimeEntry.getMaxHour()).
                     entry("minuteMin", dateTimeEntry.getMinMinute()).
                     entry("minuteMax", dateTimeEntry.getMaxMinute()).
                     entry("secondMin", dateTimeEntry.getMinSecond()).
                     entry("secondMax", dateTimeEntry.getMaxSecond());
                 }
 
                 encodeClientBehaviors(context, dateTimeEntry, json);
 
                 if(!themeForms()) {
                     json.entry("theme", false);
                 }
                 json.entry("disableHoverStyling", dateTimeEntry.isDisableHoverStyling());
                 json.entry("showCurrentAtPos", 0 - dateTimeEntry.getLeftMonthOffset());
             json.endMap();
         json.endFunction();
 
         script.append(json.toString()).append("});");
 //        System.out.println(script);
         writer.write(script.toString());
 
         writer.endElement("script");
     }
 
     @Override
     public Object getConvertedValue(FacesContext context, UIComponent component, Object value) throws ConverterException {
         DateTimeEntry dateTimeEntry = (DateTimeEntry) component;
         String submittedValue = (String) value;
         Converter converter = dateTimeEntry.getConverter();
 
         if(isValueBlank(submittedValue)) {
             return null;
         }
 
         //Delegate to user supplied converter if defined
         if(converter != null) {
             return converter.getAsObject(context, dateTimeEntry, submittedValue);
         }
 
         //Use built-in converter
         try {
             Date convertedValue;
             Locale locale = dateTimeEntry.calculateLocale(context);
             SimpleDateFormat format = new SimpleDateFormat(dateTimeEntry.getPattern(), locale);
             format.setTimeZone(dateTimeEntry.calculateTimeZone());
             convertedValue = format.parse(submittedValue);
             
             return convertedValue;
 
         } catch (ParseException e) {
             throw new ConverterException(e);
         }
     }
 
     public static void printParams() {
         Map<String, String[]> paramValuesMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();
        Iterator<Map.Entry<String, String[]>> it = paramValuesMap.entrySet().iterator();
        Map.Entry entry;
         String key;
         String[] values;
        while (it.hasNext()) {
            entry = it.next();
            key = (String) entry.getKey();
            values = (String[]) entry.getValue();
             System.out.print(key);
             System.out.print(" = ");
             for (String value : values) {
                 System.out.print(value);
                 System.out.print(", ");
             }
             System.out.println();
         }
     }
 }
