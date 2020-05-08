 /*
  * Copyright 2004-2011 ICEsoft Technologies Canada Corp. (c)
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
  */
 package org.icefaces.component.datespinner;
 
 import com.sun.org.apache.xalan.internal.xsltc.dom.LoadDocument;
 import org.icefaces.component.utils.BaseInputRenderer;
 import org.icefaces.component.utils.PassThruAttributeWriter;
 import org.icefaces.component.utils.Utils;
 
 import javax.faces.application.Resource;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.logging.Logger;
 
 public class DateSpinnerRenderer extends BaseInputRenderer  {
     private static Logger logger = Logger.getLogger(DateSpinnerRenderer.class.getName());
     public static final String DATESPINNER_JS_KEY = "false";
 
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         DateSpinner dateSpinner = (DateSpinner) component;
         String clientId = dateSpinner.getClientId(context);
         if(dateSpinner.isDisabled() || dateSpinner.isReadonly()) {
             return;
         }
         String inputValue = context.getExternalContext().getRequestParameterMap().get(clientId+"_input");
         String submittedValue = context.getExternalContext().getRequestParameterMap().get(clientId + "_hidden");
         //if we want to use the type="date" for html5 then we will use the hidden field
         if(!isValueBlank(inputValue)) {
             dateSpinner.setSubmittedValue(inputValue);
         }
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         DateSpinner spinner = (DateSpinner) component;
         ResponseWriter writer = context.getResponseWriter();
         String clientId = spinner.getClientId(context);
    //    Map contextMap = context.getViewRoot().getViewMap(); //this does not work
         Map contextMap = context.getAttributes();
         if (!contextMap.containsKey(DATESPINNER_JS_KEY)) {
             Resource jsFile = context.getApplication().getResourceHandler().createResource("dateSpinner.js", "org.icefaces.component.datespinner");
             String src = jsFile.getRequestPath();
             writer.startElement("script", component);
             writer.writeAttribute("text", "text/javascript", null);
             writer.writeAttribute("src", src, null);
             writer.endElement("script");
             contextMap.put(DATESPINNER_JS_KEY, "true");
         }
         String initialValue = getStringValueToRender(context, component);
         String value = this.encodeValue(spinner, initialValue);
         encodeMarkup(context, component, value);
         encodeScript(context, component);
     }
 
     protected void encodeMarkup(FacesContext context, UIComponent uiComponent, String value) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         DateSpinner dateEntry = (DateSpinner) uiComponent;
         String clientId = dateEntry.getClientId(context);
         String eventStr = "onclick";
         if (Utils.isMobile(context)){
            eventStr="ontouchend";
        }
         //for now assume always a popuop
         boolean popup = true;
         int yMin = dateEntry.getYearStart();
         int yMax = dateEntry.getYearEnd();
         StringBuilder popupBaseClass = new StringBuilder(DateSpinner.CONTAINER_CLASS);
         //should any component entered styleclass be applied to all base classes?
         if (popup) {
             popupBaseClass.append("-hide");
         }
         //first do the input field and the button
         // build out first input field
         writer.startElement("input", uiComponent);
         writer.writeAttribute("id", clientId + "_input", "id");
         writer.writeAttribute("name", clientId+"_input", "name");
         // apply class attribute and pass though attributes for style.
         PassThruAttributeWriter.renderNonBooleanAttributes(writer, uiComponent,
                 dateEntry.getCommonAttributeNames());
         StringBuilder classNames = new StringBuilder(DateSpinner.INPUT_CLASS)
                 .append(" ").append(dateEntry.getStyleClass());
         writer.writeAttribute("class", classNames.toString(), null);
         if (value!=null){
             writer.writeAttribute("value", value, null);
         }
         writer.writeAttribute("type", "text", "type");
         if(dateEntry.isReadonly()) writer.writeAttribute("readonly", "readonly", null);
         if(dateEntry.isDisabled()) writer.writeAttribute("disabled", "disabled", null);
         writer.endElement("input");
         // build out command button for show/hide of date select popup.
         writer.startElement("input", uiComponent);
         writer.writeAttribute("type", "button", "type");
         writer.writeAttribute("value", "", null);
         writer.writeAttribute("class", DateSpinner.POP_UP_CLASS, null);
         if(dateEntry.isDisabled()) writer.writeAttribute("disabled", "disabled", null);
         else{
             writer.writeAttribute(eventStr, "mobi.datespinner.toggle('"+clientId+"');", null);
         }
         writer.endElement("input");
         //hidden field to store state of current instance
         writer.startElement("input", uiComponent);
         writer.writeAttribute("type", "hidden", null);
         writer.writeAttribute("id", clientId + "_hidden", "id");
         writer.writeAttribute("name", clientId + "_hidden", "name");
         writer.endElement("input");
         // dive that is use to hide/show the popup screen black out.
         writer.startElement("div",uiComponent);
         writer.writeAttribute("id", clientId, "id");
         writer.endElement("div");
         // actual popup code.
         writer.startElement("div", uiComponent);
         writer.writeAttribute("id", clientId+"_popup", "id");
         writer.writeAttribute("class", popupBaseClass.toString(), null);
         writer.startElement("div", uiComponent);
         writer.writeAttribute("id", clientId+"_title", "id");
         writer.writeAttribute("class", DateSpinner.TITLE_CLASS, null);
         writer.write(value);
         writer.endElement("div");
         writer.startElement("div", uiComponent);                            //entire selection container
         writer.writeAttribute("class", DateSpinner.SELECT_CONT_CLASS, null);
         //day
         writer.startElement("div",uiComponent);                             //date select container
         writer.writeAttribute("class", DateSpinner.VALUE_CONT_CLASS, null);
         writer.startElement("div", uiComponent);                            //button increment
         writer.writeAttribute("class", DateSpinner.BUTTON_INC_CONT_CLASS, null);
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", DateSpinner.BUTTON_INC_CLASS, null);
         writer.writeAttribute("id", clientId + "_dUpBtn", null);
         writer.writeAttribute("type", "button",null);
         writer.writeAttribute(eventStr, "mobi.datespinner.dUp('"+clientId+"');",null);
         writer.endElement("input");
         writer.endElement("div");                                         //end button incr
         writer.startElement("div", uiComponent);                          //day value
         writer.writeAttribute("class", DateSpinner.SEL_VALUE_CLASS, null);
         writer.writeAttribute("id", clientId+"_dInt",null);
         writer.write( String.valueOf(dateEntry.getDayInt())) ;
         writer.endElement("div");                                         //end of day value
         writer.startElement("div", uiComponent);                          //button decrement
         writer.writeAttribute("class", DateSpinner.BUTTON_DEC_CONT_CLASS, null);
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", DateSpinner.BUTTON_DEC_CLASS, null);
         writer.writeAttribute("id", clientId + "_dDnBtn", null);
         writer.writeAttribute("type", "button",null);
         writer.writeAttribute(eventStr, "mobi.datespinner.dDn('"+clientId+"');",null);
         writer.endElement("input");
         writer.endElement("div");                                         //end button decrement
         writer.endElement("div");                                         //end of dateEntry select container
         //month
         writer.startElement("div",uiComponent);                             //month select container
         writer.writeAttribute("class", DateSpinner.VALUE_CONT_CLASS, null);
         writer.startElement("div", uiComponent);                            //button increment
         writer.writeAttribute("class", DateSpinner.BUTTON_INC_CONT_CLASS, null);
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", DateSpinner.BUTTON_INC_CLASS, null);
         writer.writeAttribute("id", clientId + "_mUpBtn", null);
         writer.writeAttribute("type", "button",null);
         writer.writeAttribute(eventStr, "mobi.datespinner.mUp('"+clientId+"');",null);
         writer.endElement("input");
         writer.endElement("div");                                         //end button incr
         writer.startElement("div", uiComponent);                          //month value
         writer.writeAttribute("class", DateSpinner.SEL_VALUE_CLASS, null);
         writer.writeAttribute("id", clientId+"_mInt",null);
         writer.write( String.valueOf(dateEntry.getMonthInt())) ;
         writer.endElement("div");                                         //end of month value
         writer.startElement("div", uiComponent);                          //button decrement
         writer.writeAttribute("class", DateSpinner.BUTTON_DEC_CONT_CLASS, null);
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", DateSpinner.BUTTON_DEC_CLASS, null);
         writer.writeAttribute("id", clientId + "_mDnBtn", null);
         writer.writeAttribute("type", "button",null);
         writer.writeAttribute(eventStr, "mobi.datespinner.mDn('"+clientId+"');",null);
         writer.endElement("input");
         writer.endElement("div");                                         //end button decrement
         writer.endElement("div");                                         //end of month select container
         //year
         writer.startElement("div",uiComponent);                             //year select container
         writer.writeAttribute("class", DateSpinner.VALUE_CONT_CLASS, null);
         writer.startElement("div", uiComponent);                            //button increment
         writer.writeAttribute("class", DateSpinner.BUTTON_INC_CONT_CLASS, null);
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", DateSpinner.BUTTON_INC_CLASS, null);
         writer.writeAttribute("id", clientId + "_yUpBtn", null);
         writer.writeAttribute("type", "button",null);
         writer.writeAttribute(eventStr, "mobi.datespinner.yUp('"+clientId+"',"+yMin+","+yMax+");",null);
         writer.endElement("input");
         writer.endElement("div");                                         //end button incr
         writer.startElement("div", uiComponent);                          //year value
         writer.writeAttribute("class", DateSpinner.SEL_VALUE_CLASS, null);
         writer.writeAttribute("id", clientId+"_yInt",null);
         writer.write( String.valueOf(dateEntry.getYearInt())) ;
         writer.endElement("div");                                         //end of year value
         writer.startElement("div", uiComponent);                          //button decrement
         writer.writeAttribute("class", DateSpinner.BUTTON_DEC_CONT_CLASS, null);
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", DateSpinner.BUTTON_DEC_CLASS, null);
         writer.writeAttribute("id", clientId + "_yDnBtn", null);
         writer.writeAttribute("type", "button",null);
         writer.writeAttribute(eventStr, "mobi.datespinner.yDn('"+clientId+"',"+yMin+","+yMax+");",null);
         writer.endElement("input");
         writer.endElement("div");                                         //end button decrement
         writer.endElement("div");                                         //end of year select container
 
         writer.endElement("div");                                         //end of selection container
         writer.startElement("div", uiComponent);                          //button container for set or cancel
         writer.writeAttribute("class", "mobi-date-submit-container", null);
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", "mobi-button mobi-button-default", null);
         writer.writeAttribute ("type", "button", "type");
         writer.writeAttribute("value", "Set", null);
         //prep for singleSubmit in
         boolean singleSubmit = dateEntry.isSingleSubmit();
 //       writer.writeAttribute(eventStr, "mobi.datespinner.select('"+clientId+"',"+singleSubmit+");", null);
        writer.writeAttribute(eventStr, "mobi.datespinner.select('"+clientId+"');", null);
         writer.endElement("input");
         writer.startElement("input", uiComponent);
         writer.writeAttribute("class", "mobi-button mobi-button-default", null);
         writer.writeAttribute ("type", "button", "type");
         writer.writeAttribute("value", "Cancel", null);
         writer.writeAttribute(eventStr, "mobi.datespinner.close('"+clientId+"');", null);
         writer.endElement("input");
         writer.endElement("div");                                        //end of button container
 
         writer.endElement("div");                                         //end of entire container
     }
 
     public void encodeScript(FacesContext context, UIComponent uiComponent) throws IOException{
            //need to initialize the component on the page and can also
         ResponseWriter writer = context.getResponseWriter();
         DateSpinner spinner = (DateSpinner) uiComponent;
         String clientId = spinner.getClientId(context);
         //separate the value into yrInt, mthInt, dateInt for now just use contstants
         int yrInt = spinner.getYearInt();
         int mnthInt  = spinner.getMonthInt();
         int dateInt = spinner.getDayInt();
         int yrStart = spinner.getYearStart();
         int yrEnd = spinner.getYearEnd();
 
         writer.startElement("script", null);
         writer.writeAttribute("text", "text/javascript", null);
         writer.write("mobi.datespinner.init('"+clientId+"',"+yrInt+","+mnthInt+","+dateInt+",'"+spinner.getPattern()+"');");
     /*    writer.write("ice.onUnload(function(){" +
                 "mobi.datespinner.unload('" + clientId + "');" +
                 "});\n");      */
         writer.endElement("script");
     }
 
 
     @Override
     public Object getConvertedValue(FacesContext context, UIComponent component, Object value) throws ConverterException {
         DateSpinner spinner = (DateSpinner) component;
         String submittedValue = String.valueOf(value);
         Object objVal = null;
         Converter converter = spinner.getConverter();
 
         //Delegate to user supplied converter if defined
         if(converter != null) {
             objVal =  converter.getAsObject(context, spinner, submittedValue);
             return objVal;
         }
 
         try {
             Date convertedValue;
             Locale locale = spinner.calculateLocale(context);
             SimpleDateFormat format = new SimpleDateFormat(spinner.getPattern(), locale);
             format.setTimeZone(spinner.calculateTimeZone());
             convertedValue = format.parse(submittedValue);
             return convertedValue;
 
         } catch (ParseException e) {
             throw new ConverterException(e);
         }
     }
 
     private void setIntValues(DateSpinner spinner, Date aDate) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(aDate);
         spinner.setYearInt(cal.get(Calendar.YEAR));
         spinner.setMonthInt(cal.get(Calendar.MONTH)+1);  //month is 0-indexed 0 = jan, 1=feb, etc
         spinner.setDayInt(cal.get(Calendar.DAY_OF_MONTH));
     }
 
     private String convertStringInput(String patternIn,String patternOut, String inString){
         SimpleDateFormat df1 = new SimpleDateFormat(patternIn);   //default date pattern
         SimpleDateFormat df2 = new SimpleDateFormat(patternOut);
         String returnString = inString;
         try {
             Date aDate = df1.parse(inString);
             returnString = df2.format(aDate);
         }catch (Exception e){
             //means that it was already in the pattern format so just return the inString
             e.printStackTrace();
         }
         return returnString;
     }
 
 
     private boolean isValueBlank(String value) {
 		if(value == null)
 			return true;
 
 		return value.trim().equals("");
 	}
 
     private String encodeValue( DateSpinner spinner,  String initialValue)  {
         String value = "";
         Date aDate = new Date();
         SimpleDateFormat df2 = new SimpleDateFormat(spinner.getPattern());
         if (isValueBlank(initialValue)){
             //nothing values already set as default
         }else {
             try{
                 if (isFormattedDate(initialValue, spinner.getPattern())){
                     value= initialValue;
                     aDate = df2.parse(value);
                 } else if (isFormattedDate(initialValue, "EEE MMM dd hh:mm:ss zzz yyyy")){
                     value= convertStringInput("EEE MMM dd hh:mm:ss zzz yyyy", spinner.getPattern(), initialValue); //converts to the patter the spinner is set for
                     aDate = df2.parse(value);
                 }
             }catch (Exception e){
                throw new ConverterException();
             }
 
         }
         this.setIntValues(spinner, aDate);
         return value;
     }
 
     private boolean isFormattedDate(String inStr, String format){
          SimpleDateFormat sdf = new SimpleDateFormat(format);
          return sdf.parse(inStr, new ParsePosition(0)) != null;
     }
 
 
 }
 
