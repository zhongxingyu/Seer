 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 
 package com.icesoft.faces.component.selectinputdate;
 
 import com.icesoft.faces.component.CSS_DEFAULT;
 import com.icesoft.faces.component.ext.HtmlCommandLink;
 import com.icesoft.faces.component.ext.HtmlGraphicImage;
 import com.icesoft.faces.component.ext.HtmlOutputText;
 import com.icesoft.faces.component.ext.HtmlSelectOneMenu;
 import com.icesoft.faces.component.ext.renderkit.FormRenderer;
 import com.icesoft.faces.component.ext.taglib.Util;
 import com.icesoft.faces.component.util.CustomComponentUtils;
 import com.icesoft.faces.context.DOMContext;
 import com.icesoft.faces.context.effects.JavascriptContext;
 import com.icesoft.faces.renderkit.dom_html_basic.DomBasicInputRenderer;
 import com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer;
 import com.icesoft.faces.renderkit.dom_html_basic.HTML;
 import com.icesoft.faces.renderkit.dom_html_basic.PassThruAttributeRenderer;
 import com.icesoft.faces.util.CoreUtils;
 import com.icesoft.faces.utils.MessageUtils;
 import com.icesoft.util.pooling.CSSNamePool;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import javax.faces.component.*;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.convert.DateTimeConverter;
 import javax.faces.event.ActionEvent;
 import java.io.IOException;
 import java.text.DateFormatSymbols;
 import java.text.MessageFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 /**
  * <p> The SelectInputDateRenderer class is an ICEfaces D2D renderer for the
  * SelectInputDate component. Note: This class originally was derived from the
  * MyFaces Calendar.</p>
  */
 public class SelectInputDateRenderer
         extends DomBasicInputRenderer {
     //	add a static log member
     private static final Log log =
             LogFactory.getLog(SelectInputDateRenderer.class);
 
     private static final String CALENDAR_TABLE = "_ct";
     private static final String CALENDAR_BUTTON = "_cb";
     private static final String CALENDAR_POPUP = "_cp";
     private static final String HIDDEN_FIELD_NAME = "sp";
     private static final String DATE_SELECTED = "ds";
 
     // constants for navigation link ids
     private static final String SELECT_MONTH = "_sm";
     private static final String SELECT_YEAR = "_sy";
     private static final String PREV_MONTH = "_pm";
     private static final String NEXT_MONTH = "_nm";
     private static final String PREV_YEAR = "_py";
     private static final String NEXT_YEAR = "_ny";
     private static final String SELECT_HOUR = "_hr";
     private static final String SELECT_MIN = "_min";
     private static final String SELECT_SEC = "_sec";
     private static final String SELECT_AM_PM = "_amPm";
 
     // constant for selectinputdate links
     private static final String CALENDAR = "_c_";
     private static final String CALENDAR_CLICK = "_cc";
     private static final String ROOT_DIV = "_rd";
 
     private static final int IS_NOT = 0;
     private static final int IS_CALENDAR_BUTTON = 1;
     private static final int IS_CALENDAR = 2;
     private static final int IS_PREV_MONTH = 3;
     private static final int IS_NEXT_MONTH = 4;
     private static final int IS_PREV_YEAR = 5;
     private static final int IS_NEXT_YEAR = 6;
     private static final int IS_HOUR = 7;
     private static final int IS_MIN = 8;
     private static final int IS_SEC             = 9;
     private static final int IS_AM_PM           = 10;
 
     private static final String INPUT_TEXT_TITLE =
             "com.icesoft.faces.component.selectinputdate.INPUT_TEXT_TITLE";
     private static final String CALENDAR_TITLE =
             "com.icesoft.faces.component.selectinputdate.CALENDAR_TITLE";
     private static final String CALENDAR_SUMMARY =
             "com.icesoft.faces.component.selectinputdate.CALENDAR_SUMMARY";
     private static final String POPUP_CALENDAR_TITLE =
             "com.icesoft.faces.component.selectinputdate.POPUP_CALENDAR_TITLE";
     private static final String POPUP_CALENDAR_SUMMARY =
             "com.icesoft.faces.component.selectinputdate.POPUP_CALENDAR_SUMMARY";
     private static final String YEAR_MONTH_SUMMARY =
             "com.icesoft.faces.component.selectinputdate.YEAR_MONTH_SUMMARY";
     private static final String OPEN_POPUP_ALT =
             "com.icesoft.faces.component.selectinputdate.OPEN_POPUP_ALT";
     private static final String OPEN_POPUP_TITLE =
             "com.icesoft.faces.component.selectinputdate.OPEN_POPUP_TITLE";
     private static final String CLOSE_POPUP_ALT =
             "com.icesoft.faces.component.selectinputdate.CLOSE_POPUP_ALT";
     private static final String CLOSE_POPUP_TITLE =
             "com.icesoft.faces.component.selectinputdate.CLOSE_POPUP_TITLE";
     private static final String PREV_YEAR_ALT =
             "com.icesoft.faces.component.selectinputdate.PREV_YEAR_ALT";
     private static final String PREV_YEAR_TITLE =
             "com.icesoft.faces.component.selectinputdate.PREV_YEAR_TITLE";
     private static final String NEXT_YEAR_ALT =
             "com.icesoft.faces.component.selectinputdate.NEXT_YEAR_ALT";
     private static final String NEXT_YEAR_TITLE =
             "com.icesoft.faces.component.selectinputdate.NEXT_YEAR_TITLE";
     private static final String PREV_MONTH_ALT =
             "com.icesoft.faces.component.selectinputdate.PREV_MONTH_ALT";
     private static final String PREV_MONTH_TITLE =
             "com.icesoft.faces.component.selectinputdate.PREV_MONTH_TITLE";
     private static final String NEXT_MONTH_ALT =
             "com.icesoft.faces.component.selectinputdate.NEXT_MONTH_ALT";
     private static final String NEXT_MONTH_TITLE =
             "com.icesoft.faces.component.selectinputdate.NEXT_MONTH_TITLE";
     private static final String PREV_YEAR_LABEL =
             "com.icesoft.faces.component.selectinputdate.PREV_YEAR_LABEL";
     private static final String NEXT_YEAR_LABEL =
             "com.icesoft.faces.component.selectinputdate.NEXT_YEAR_LABEL";
     private static final String WEEK_NUM_HDR = "com.icesoft.faces.component.selectinputdate.WEEK_NUM_HDR";
     private static final String WEEK_NUM_HDR_TITLE = "com.icesoft.faces.component.selectinputdate.WEEK_NUM_HDR_TITLE";
     private static final int yearListSize = 11;
 
     //private static final String[] passThruAttributes = ExtendedAttributeConstants.getAttributes(ExtendedAttributeConstants.ICE_SELECTINPUTDATE);
     //handled title
     private static final String[] passThruAttributes = new String[]{HTML.DIR_ATTR, HTML.LANG_ATTR, HTML.ONCLICK_ATTR, HTML.ONDBLCLICK_ATTR, HTML.ONKEYDOWN_ATTR, HTML.ONKEYPRESS_ATTR, HTML.ONKEYUP_ATTR, HTML.ONMOUSEDOWN_ATTR, HTML.ONMOUSEMOVE_ATTR, HTML.ONMOUSEOUT_ATTR, HTML.ONMOUSEOVER_ATTR, HTML.ONMOUSEUP_ATTR, HTML.STYLE_ATTR, HTML.TABINDEX_ATTR, HTML.TITLE_ATTR};
     //required for popup calendar
     private static final String[] passThruAttributesWithoutTabindex = new String[]{HTML.DIR_ATTR, HTML.LANG_ATTR, HTML.ONCLICK_ATTR, HTML.ONDBLCLICK_ATTR, HTML.ONKEYDOWN_ATTR, HTML.ONKEYPRESS_ATTR, HTML.ONKEYUP_ATTR, HTML.ONMOUSEDOWN_ATTR, HTML.ONMOUSEMOVE_ATTR, HTML.ONMOUSEOUT_ATTR, HTML.ONMOUSEOVER_ATTR, HTML.ONMOUSEUP_ATTR, HTML.STYLE_ATTR, HTML.TITLE_ATTR};
 
    private static final String ID_SUFFIX = UINamingContainer.getSeparatorChar(FacesContext.getCurrentInstance()) + "j_idcl";
 
     private static final String NBSP = HTML.NBSP_ENTITY;
 
     /* (non-Javadoc)
     * @see javax.faces.render.Renderer#getRendersChildren()
     */
     public boolean getRendersChildren() {
 
         return true;
     }
 
     /* (non-Javadoc)
      * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
      */
     public void encodeChildren(FacesContext facesContext,
                                UIComponent uiComponent) {
 
     }
 
     private String getHiddenFieldName(FacesContext facesContext,
                                       UIComponent uiComponent) {
         UIComponent form = findForm(uiComponent);
         String formId = form.getClientId(facesContext);
         String clientId = uiComponent.getClientId(facesContext);
         String hiddenFieldName = formId
                 + NamingContainer.SEPARATOR_CHAR
                 + UIViewRoot.UNIQUE_ID_PREFIX
                 + clientId
                 + HIDDEN_FIELD_NAME;
         return hiddenFieldName;
     }
 
     /* (non-Javadoc)
      * @see com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer#encodeEnd(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
      */
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
         validateParameters(facesContext, uiComponent, SelectInputDate.class);
         DOMContext domContext =
                 DOMContext.attachDOMContext(facesContext, uiComponent);
         SelectInputDate selectInputDate = (SelectInputDate) uiComponent;
 
         Date value;
         boolean actuallyHaveTime = false;
         if (selectInputDate.isNavEvent()) {
             if (log.isTraceEnabled()) {
                 log.trace("Rendering Nav Event");
             }
             value = selectInputDate.getNavDate();
 //System.out.println("navDate: " + value);
         } else if (selectInputDate.isRenderAsPopup() &&
                 !selectInputDate.isEnterKeyPressed(facesContext)) {
             value = selectInputDate.getPopupDate();
             if (value != null) {
                 actuallyHaveTime = true;
             }
         } else {
             if (log.isTraceEnabled()) {
                 log.trace("Logging non nav event");
             }
             value = CustomComponentUtils.getDateValue(selectInputDate);
             if (value != null) {
                 actuallyHaveTime = true;
             }
 //System.out.println("CustomComponentUtils.getDateValue: " + value);
         }
 
         DateTimeConverter converter = selectInputDate.resolveDateTimeConverter(facesContext);
         TimeZone tz = selectInputDate.resolveTimeZone(facesContext);
         Locale currentLocale = selectInputDate.resolveLocale(facesContext);
         DateFormatSymbols symbols = new DateFormatSymbols(currentLocale);
 
 //System.out.println("SIDR.encodeEnd()  timezone: " + tz);
 //System.out.println("SIDR.encodeEnd()  locale: " + currentLocale);
 
         Calendar timeKeeper = Calendar.getInstance(tz, currentLocale);
         timeKeeper.setTime(value != null ? value : new Date());
 
         // get the parentForm
         UIComponent parentForm = findForm(selectInputDate);
         // if there is no parent form - ERROR
         if (parentForm == null) {
             log.error("SelectInputDate::must be in a FORM");
             return;
         }
         String clientId;
         if (!domContext.isInitialized()) {
             Element root = domContext.createRootElement(HTML.DIV_ELEM);
             boolean popupState = selectInputDate.isShowPopup();
 
             clientId = uiComponent.getClientId(facesContext);
             if (uiComponent.getId() != null)
                 root.setAttribute("id", clientId + ROOT_DIV);
 
             Element table = domContext.createElement(HTML.TABLE_ELEM);
             if (selectInputDate.isRenderAsPopup()) {
                 if (log.isTraceEnabled()) {
                     log.trace("Render as popup");
                 }
                 // ICE-2492
                 root.setAttribute(HTML.CLASS_ATTR,
                         Util.getQualifiedStyleClass(uiComponent, CSS_DEFAULT.DEFAULT_CALENDARPOPUP_CLASS, false));
                 Element dateText = domContext.createElement(HTML.INPUT_ELEM);
 //System.out.println("value: " + selectInputDate.getValue());
 
                 dateText.setAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_TEXT); // ICE-2302
                 dateText.setAttribute(HTML.VALUE_ATTR,
                         selectInputDate.getTextToRender());
                 dateText.setAttribute(HTML.ID_ATTR,
                         clientId + SelectInputDate.CALENDAR_INPUTTEXT);
                 dateText.setAttribute(HTML.NAME_ATTR,
                         clientId + SelectInputDate.CALENDAR_INPUTTEXT);
                 dateText.setAttribute(HTML.CLASS_ATTR,
                         selectInputDate.getCalendarInputClass());
                 dateText.setAttribute(HTML.ONFOCUS_ATTR, "setFocus('');");
                 dateText.setAttribute("onkeypress", this.ICESUBMIT);
                 String onblur = combinedPassThru(
                         "setFocus('');",
                         selectInputDate.getPartialSubmit() ? ICESUBMITPARTIAL : null);
                 dateText.setAttribute(HTML.ONBLUR_ATTR, onblur);
                 if (selectInputDate.getTabindex() != null) {
                     dateText.setAttribute(HTML.TABINDEX_ATTR, selectInputDate.getTabindex());
                 }
                 if (selectInputDate.getAutocomplete() != null && "off".equalsIgnoreCase(selectInputDate.getAutocomplete())) {
                     dateText.setAttribute(HTML.AUTOCOMPLETE_ATTR, "off");
                 }
                 String tooltip = null;
                 tooltip = selectInputDate.getInputTitle();
                 if (tooltip == null || tooltip.length() == 0) {
                     // extract the popupdate format and use it as a tooltip
                     tooltip = getMessageWithParamFromResource(
                             facesContext, INPUT_TEXT_TITLE,
                             selectInputDate.getSpecifiedPopupDateFormat());
                 }
                 if (tooltip != null && tooltip.length() > 0) {
                     dateText.setAttribute(HTML.TITLE_ATTR, tooltip);
                 }
                 if (selectInputDate.isDisabled()) {
                     dateText.setAttribute(HTML.DISABLED_ATTR, HTML.DISABLED_ATTR);
                 }
                 if (selectInputDate.isReadonly()) {
                     dateText.setAttribute(HTML.READONLY_ATTR, HTML.READONLY_ATTR);
                 }
                 root.appendChild(dateText);
                 Element calendarButton =
                         domContext.createElement(HTML.INPUT_ELEM);
                 calendarButton
                         .setAttribute(HTML.ID_ATTR, clientId + CALENDAR_BUTTON);
                 calendarButton.setAttribute(HTML.NAME_ATTR,
                         clientId + CALENDAR_BUTTON);
                 calendarButton.setAttribute(HTML.TYPE_ATTR, "image");
                 calendarButton.setAttribute(HTML.ONFOCUS_ATTR, "setFocus('');");
                 // render onclick to set value of hidden field to true
                 String formClientId = parentForm.getClientId(facesContext);
                 String hiddenValue1 = "document.forms['" +
                         formClientId + "']['" +
                         this.getLinkId(facesContext, uiComponent) +
                         "'].value='";
                 String hiddenValue2 = "document.forms['" +
                         formClientId + "']['" +
                         getHiddenFieldName(facesContext, uiComponent) +
                         "'].value='";
                 String onClick = hiddenValue1 + clientId + CALENDAR_BUTTON +
                         "';"
                         + hiddenValue2 + "toggle';"
                         + "iceSubmitPartial( document.forms['" +
                         formClientId +
                         "'], this,event);"
                         + hiddenValue1 + "';"
                         + hiddenValue2 + "';"
                         + "Ice.Calendar.addCloseListener('"
                         + clientId + "','" + formClientId + "','"
                         + this.getLinkId(facesContext, uiComponent) + "','"
                         + getHiddenFieldName(facesContext, uiComponent) + "');"
                         + "return false;";
                 calendarButton.setAttribute(HTML.ONCLICK_ATTR, onClick);
                 if (selectInputDate.getTabindex() != null) {
                     try {
                         int tabIndex = Integer.valueOf(selectInputDate.getTabindex()).intValue();
                         tabIndex += 1;
                         calendarButton.setAttribute(HTML.TABINDEX_ATTR, String.valueOf(tabIndex));
                     } catch (NumberFormatException e) {
                         if (log.isInfoEnabled()) {
                             log.info("NumberFormatException on tabindex");
                         }
                     }
                 }
 
                 if (selectInputDate.isDisabled()) {
                     calendarButton.setAttribute(HTML.DISABLED_ATTR, HTML.DISABLED_ATTR);
                 }
                 if (selectInputDate.isReadonly()) {
                     calendarButton.setAttribute(HTML.READONLY_ATTR, HTML.READONLY_ATTR);
                 }
                 root.appendChild(calendarButton);
                 // render a hidden field to manage the popup state; visible || hidden
                 FormRenderer.addHiddenField(facesContext, getHiddenFieldName(
                         facesContext, uiComponent));
                 String resolvedSrc;
                 if (popupState) {
                     JavascriptContext.addJavascriptCall(facesContext, "Ice.util.adjustMyPosition('" + clientId + CALENDAR_TABLE + "', '" + clientId + ROOT_DIV + "');");
                     if (selectInputDate.isImageDirSet()) {
                         resolvedSrc = CoreUtils.resolveResourceURL(facesContext,
                                 selectInputDate.getImageDir() + selectInputDate.getClosePopupImage());
                     } else {
                         // ICE-2127: allow override of button images via CSS
                         calendarButton.setAttribute(HTML.CLASS_ATTR, selectInputDate.getClosePopupClass());
                         // without this Firefox would display a default text on top of the image
                         resolvedSrc = CoreUtils.resolveResourceURL(facesContext,
                                 selectInputDate.getImageDir() + "spacer.gif");
                     }
                     calendarButton.setAttribute(HTML.SRC_ATTR, resolvedSrc);
                     addAttributeToElementFromResource(facesContext,
                             CLOSE_POPUP_ALT, calendarButton, HTML.ALT_ATTR);
                     addAttributeToElementFromResource(facesContext,
                             CLOSE_POPUP_TITLE, calendarButton, HTML.TITLE_ATTR);
                 } else {
                     if (selectInputDate.isImageDirSet()) {
                         resolvedSrc = CoreUtils.resolveResourceURL(facesContext,
                                 selectInputDate.getImageDir() + selectInputDate.getOpenPopupImage());
                     } else {
                         // ICE-2127: allow override of button images via CSS
                         calendarButton.setAttribute(HTML.CLASS_ATTR, selectInputDate.getOpenPopupClass());
                         // without this Firefox would display a default text on top of the image
                         resolvedSrc = CoreUtils.resolveResourceURL(facesContext,
                                 selectInputDate.getImageDir() + "spacer.gif");
                     }
                     calendarButton.setAttribute(HTML.SRC_ATTR, resolvedSrc);
                     addAttributeToElementFromResource(facesContext,
                             OPEN_POPUP_ALT, calendarButton, HTML.ALT_ATTR);
                     addAttributeToElementFromResource(facesContext,
                             OPEN_POPUP_TITLE, calendarButton, HTML.TITLE_ATTR);
                     FormRenderer.addHiddenField(
                             facesContext,
                             formClientId + ID_SUFFIX);
                     FormRenderer.addHiddenField(
                             facesContext,
                             clientId + CALENDAR_CLICK);
                     PassThruAttributeRenderer.renderHtmlAttributes(facesContext, uiComponent, passThruAttributesWithoutTabindex);
                     domContext.stepOver();
                     return;
                 }
 
                 Text br = domContext.createTextNodeUnescaped("<br/>");
                 root.appendChild(br);
 
                 Element calendarDiv = domContext.createElement(HTML.DIV_ELEM);
                 calendarDiv
                         .setAttribute(HTML.ID_ATTR, clientId + CALENDAR_POPUP);
                 calendarDiv.setAttribute(HTML.NAME_ATTR,
                         clientId + CALENDAR_POPUP);
                 calendarDiv.setAttribute(HTML.STYLE_ELEM,
                         "position:absolute;z-index:10;");
                 addAttributeToElementFromResource(facesContext,
                         POPUP_CALENDAR_TITLE, calendarDiv, HTML.TITLE_ATTR);
                 table.setAttribute(HTML.ID_ATTR, clientId + CALENDAR_TABLE);
                 table.setAttribute(HTML.NAME_ATTR, clientId + CALENDAR_TABLE);
                 table.setAttribute(HTML.CLASS_ATTR,
                         selectInputDate.getStyleClass());
                 table.setAttribute(HTML.STYLE_ATTR, "position:absolute;");
                 table.setAttribute(HTML.CELLPADDING_ATTR, "0");
                 table.setAttribute(HTML.CELLSPACING_ATTR, "0");
                 // set mouse events on table bug 372
                 String mouseOver = selectInputDate.getOnmouseover();
                 table.setAttribute(HTML.ONMOUSEOVER_ATTR, mouseOver);
                 String mouseOut = selectInputDate.getOnmouseout();
                 table.setAttribute(HTML.ONMOUSEOUT_ATTR, mouseOut);
                 String mouseMove = selectInputDate.getOnmousemove();
                 table.setAttribute(HTML.ONMOUSEMOVE_ATTR, mouseMove);
 
                 addAttributeToElementFromResource(facesContext,
                         POPUP_CALENDAR_SUMMARY, table, HTML.SUMMARY_ATTR);
                 Element positionDiv = domContext.createElement(HTML.DIV_ELEM);
                 positionDiv.appendChild(table);
                 calendarDiv.appendChild(positionDiv);
                 Text iframe = domContext.createTextNodeUnescaped("<!--[if lte IE" +
                         " 6.5]><iframe src='" + CoreUtils.resolveResourceURL
                         (FacesContext.getCurrentInstance(), "/xmlhttp/blank") +
                         "' class=\"iceSelInpDateIFrameFix\"></iframe><![endif]-->");
                 calendarDiv.appendChild(iframe);
                 root.appendChild(calendarDiv);
 
             } else {
                 if (log.isTraceEnabled()) {
                     log.trace("Select input Date Normal");
                 }
                 table.setAttribute(HTML.ID_ATTR, clientId + CALENDAR_TABLE);
                 table.setAttribute(HTML.NAME_ATTR, clientId + CALENDAR_TABLE);
                 table.setAttribute(HTML.CLASS_ATTR,
                         selectInputDate.getStyleClass());
                 addAttributeToElementFromResource(facesContext,
                         CALENDAR_TITLE, table, HTML.TITLE_ATTR);
                 table.setAttribute(HTML.CELLPADDING_ATTR, "0");
                 table.setAttribute(HTML.CELLSPACING_ATTR, "0");
                 // set mouse events on table bug 372
                 String mouseOver = selectInputDate.getOnmouseover();
                 table.setAttribute(HTML.ONMOUSEOVER_ATTR, mouseOver);
                 String mouseOut = selectInputDate.getOnmouseout();
                 table.setAttribute(HTML.ONMOUSEOUT_ATTR, mouseOut);
                 String mouseMove = selectInputDate.getOnmousemove();
                 table.setAttribute(HTML.ONMOUSEMOVE_ATTR, mouseMove);
                 addAttributeToElementFromResource(facesContext,
                         CALENDAR_SUMMARY, table, HTML.SUMMARY_ATTR);
                 root.appendChild(table);
 
                 Element dateText = domContext.createElement(HTML.INPUT_ELEM);
                 dateText.setAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN);
                 dateText.setAttribute(HTML.VALUE_ATTR,
                         selectInputDate.getTextToRender());
                 dateText.setAttribute(HTML.ID_ATTR,
                         clientId + SelectInputDate.CALENDAR_INPUTTEXT);
                 dateText.setAttribute(HTML.NAME_ATTR,
                         clientId + SelectInputDate.CALENDAR_INPUTTEXT);
                 root.appendChild(dateText);
             }
         }
         clientId = uiComponent.getClientId(facesContext);
 
 
         String[] weekdays = mapWeekdays(symbols);
         String[] weekdaysLong = mapWeekdaysLong(symbols);
         String[] months = mapMonths(symbols);
 
         // use the currentDay to set focus - do not set
         int lastDayInMonth = timeKeeper.getActualMaximum(Calendar.DAY_OF_MONTH);
         int currentDay = timeKeeper.get(Calendar.DAY_OF_MONTH); // starts at 1
 
         if (currentDay > lastDayInMonth) {
             currentDay = lastDayInMonth;
         }
 
         timeKeeper.set(Calendar.DAY_OF_MONTH, 1);
 
         int weekDayOfFirstDayOfMonth =
                 mapCalendarDayToCommonDay(timeKeeper.get(Calendar.DAY_OF_WEEK));
 
         int weekStartsAtDayIndex =
                 mapCalendarDayToCommonDay(timeKeeper.getFirstDayOfWeek());
 
         // do not require a writer - clean out all methods that reference a writer
         ResponseWriter writer = facesContext.getResponseWriter();
 
         Element root = (Element) domContext.getRootNode();
 
         Element table = null;
         if (selectInputDate.isRenderAsPopup()) {
             if (log.isTraceEnabled()) {
                 log.trace("SelectInputDate as Popup");
             }
 
             // assumption input text is first child
             Element dateText = (Element) root.getFirstChild();
 //System.out.println("dateText  currentValue: " + currentValue);
             dateText.setAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_TEXT); // ICE-2302
             dateText.setAttribute(
                     HTML.VALUE_ATTR,
                     selectInputDate.getTextToRender());
 
             // get tables , our table is the first and only one
             NodeList tables = root.getElementsByTagName(HTML.TABLE_ELEM);
             // assumption we want the first table in tables. there should only be one
             table = (Element) tables.item(0);
 
             PassThruAttributeRenderer.renderHtmlAttributes(facesContext, uiComponent, passThruAttributesWithoutTabindex);
 
             Element tr1 = domContext.createElement(HTML.TR_ELEM);
 
             table.appendChild(tr1);
             writeMonthYearHeader(domContext, facesContext, writer,
                     selectInputDate, timeKeeper,
                     currentDay, tr1,
                     selectInputDate.getMonthYearRowClass(),
                     currentLocale, months, weekdays, weekdaysLong,
                     converter);
 
             Element tr2 = domContext.createElement(HTML.TR_ELEM);
             table.appendChild(tr2);
 
             writeWeekDayNameHeader(domContext, weekStartsAtDayIndex, weekdays,
                     facesContext, writer, selectInputDate, tr2,
                     selectInputDate.getWeekRowClass(),
                     timeKeeper, months, weekdaysLong, converter);
 
             writeDays(domContext, facesContext, writer, selectInputDate,
                     timeKeeper,
                     currentDay, weekStartsAtDayIndex,
                     weekDayOfFirstDayOfMonth,
                     lastDayInMonth, table,
                     months, weekdays, weekdaysLong, converter,
                     (actuallyHaveTime ? value : null));
         } else {
             if (log.isTraceEnabled()) {
                 log.trace("renderNormal::endcodeEnd");
             }
             // assume table is the first child
             table = (Element) root.getFirstChild();
 
             PassThruAttributeRenderer.renderHtmlAttributes(facesContext, uiComponent, passThruAttributes);
 
             Element tr1 = domContext.createElement(HTML.TR_ELEM);
             table.appendChild(tr1);
 
             writeMonthYearHeader(domContext, facesContext, writer,
                     selectInputDate, timeKeeper,
                     currentDay, tr1,
                     selectInputDate.getMonthYearRowClass(),
                     currentLocale, months, weekdays, weekdaysLong,
                     converter);
 
             Element tr2 = domContext.createElement(HTML.TR_ELEM);
 
             writeWeekDayNameHeader(domContext, weekStartsAtDayIndex, weekdays,
                     facesContext, writer, selectInputDate, tr2,
                     selectInputDate.getWeekRowClass(),
                     timeKeeper, months, weekdaysLong,
                     converter);
 
             table.appendChild(tr2);
 
             writeDays(domContext, facesContext, writer, selectInputDate,
                     timeKeeper,
                     currentDay, weekStartsAtDayIndex,
                     weekDayOfFirstDayOfMonth,
                     lastDayInMonth, table,
                     months, weekdays, weekdaysLong, converter,
                     (actuallyHaveTime ? value : null));
         }
 
         //System.out.println("SIDR.encodeEnd()  isTime? " + SelectInputDate.isTime(converter));
         if (SelectInputDate.isTime(converter)) {
             Element tfoot = domContext.createElement(HTML.TFOOT_ELEM);
             Element tr = domContext.createElement(HTML.TR_ELEM);
             Element td = domContext.createElement(HTML.TD_ELEM);
             td.setAttribute(HTML.COLSPAN_ATTR, selectInputDate.isRenderWeekNumbers() ? "8" : "7");
             td.setAttribute(HTML.CLASS_ATTR, selectInputDate.getTimeClass());
             Element tbl = domContext.createElement(HTML.TABLE_ELEM);
             Element tr2 = domContext.createElement(HTML.TR_ELEM);
             Element tdHours = domContext.createElement(HTML.TD_ELEM);
             Element hours = domContext.createElement(HTML.SELECT_ELEM);
             hours.setAttribute(HTML.ID_ATTR, clientId + SELECT_HOUR);
             hours.setAttribute(HTML.NAME_ATTR, clientId + SELECT_HOUR);
             hours.setAttribute(HTML.CLASS_ATTR, selectInputDate.getTimeDropDownClass());
             hours.setAttribute(HTML.ONCHANGE_ATTR, DomBasicRenderer.ICESUBMITPARTIAL);
 
             // Convert from an hour to an index into the list of hours
             int hrs[] = selectInputDate.getHours(facesContext);
 //System.out.println("SIDR.encodeEnd()  hrs: " + hrs[0] + ", " + hrs[hrs.length-1]);
             int hourIndex;
             int min;
             int sec;
             int amPm;
 //System.out.println("SIDR.encodeEnd()  actuallyHaveTime: " + actuallyHaveTime);
             if (!actuallyHaveTime &&
                     selectInputDate.getHoursSubmittedValue() != null &&
                     selectInputDate.getMinutesSubmittedValue() != null) {
 //System.out.println("SIDR.encodeEnd()  Using submitted hours and minutes");
                 hourIndex = selectInputDate.getHoursSubmittedValue().intValue();
 //System.out.println("SIDR.encodeEnd()  hour: " + hourIndex);
                 min = selectInputDate.getMinutesSubmittedValue().intValue();
 //System.out.println("SIDR.encodeEnd()  min: " + min);
                 String amPmStr = selectInputDate.getAmPmSubmittedValue();
 //System.out.println("SIDR.encodeEnd()  amPmStr: " + amPmStr);
                 if (amPmStr != null) {
                     amPm = amPmStr.equalsIgnoreCase("PM") ? 1 : 0;
                 } else {
                     amPm = (hourIndex >= 12) ? 1 : 0;
                 }
 //System.out.println("SIDR.encodeEnd()  amPm: " + amPm);
                 if (hrs[0] == 1) {
                     hourIndex--;
                     if (hourIndex < 0) {
                         hourIndex = hrs.length - 1;
                     }
                 }
 //System.out.println("SIDR.encodeEnd()  hourIndex: " + hourIndex);
             } else {
                 if (hrs.length > 12) {
                     hourIndex = timeKeeper.get(Calendar.HOUR_OF_DAY);
 //System.out.println("SIDR.encodeEnd()  hour 24: " + hourIndex);
                 } else {
                     hourIndex = timeKeeper.get(Calendar.HOUR);
 //System.out.println("SIDR.encodeEnd()  hour 12: " + hourIndex);
                 }
                 if (hrs[0] == 1) {
                     hourIndex--;
                     if (hourIndex < 0) {
                         hourIndex = hrs.length - 1;
                     }
                 }
 //System.out.println("SIDR.encodeEnd()  hourIndex: " + hourIndex);
 
                 min = timeKeeper.get(Calendar.MINUTE);
                 amPm = timeKeeper.get(Calendar.AM_PM);
 //System.out.println("SIDR.encodeEnd()  amPm: " + amPm);
             }
             if (!actuallyHaveTime && selectInputDate.getSecondsSubmittedValue() != null) {
                 sec = selectInputDate.getSecondsSubmittedValue().intValue();
             } else {
                 sec = timeKeeper.get(Calendar.SECOND);
             }
             for (int i = 0; i < hrs.length; i++) {
                 Element hoursOption = domContext.createElement(HTML.OPTION_ELEM);
                 hoursOption.setAttribute(HTML.VALUE_ATTR, String.valueOf(hrs[i]));
                 Text hourText = domContext.createTextNode(String.valueOf(hrs[i]));
                 hoursOption.appendChild(hourText);
                 if (i == hourIndex) {
                     hoursOption.setAttribute(HTML.SELECTED_ATTR, "true");
                 }
                 hours.appendChild(hoursOption);
             }
             Element tdColon = domContext.createElement(HTML.TD_ELEM);
             Element tdMinutes = domContext.createElement(HTML.TD_ELEM);
             Element minutes = domContext.createElement(HTML.SELECT_ELEM);
             minutes.setAttribute(HTML.ID_ATTR, clientId + SELECT_MIN);
             minutes.setAttribute(HTML.NAME_ATTR, clientId + SELECT_MIN);
             minutes.setAttribute(HTML.CLASS_ATTR, selectInputDate.getTimeDropDownClass());
             minutes.setAttribute(HTML.ONCHANGE_ATTR, DomBasicRenderer.ICESUBMITPARTIAL);
             for (int i = 0; i < 60; i++) {
                 Element minutesOption = domContext.createElement(HTML.OPTION_ELEM);
                 minutesOption.setAttribute(HTML.VALUE_ATTR, String.valueOf(i));
                 String digits = String.valueOf(i);
                 if (i < 10) {
                     digits = "0" + digits;
                 }
                 Text minuteText = domContext.createTextNode(digits);
                 minutesOption.appendChild(minuteText);
                 if (i == min) {
                     minutesOption.setAttribute(HTML.SELECTED_ATTR, "true");
                 }
                 minutes.appendChild(minutesOption);
             }
 
             Text colon = domContext.createTextNode(":");
             tfoot.appendChild(tr);
             tr.appendChild(td);
             td.appendChild(tbl);
             tbl.appendChild(tr2);
             tdHours.appendChild(hours);
             tr2.appendChild(tdHours);
             tdColon.appendChild(colon);
             tdMinutes.appendChild(minutes);
             tr2.appendChild(tdColon);
             tr2.appendChild(tdMinutes);
 
             if (selectInputDate.isSecond(facesContext)){
                     Element tdSeconds = domContext.createElement(HTML.TD_ELEM);
                     Element tdSecColon = domContext.createElement(HTML.TD_ELEM);
                 Element seconds = domContext.createElement(HTML.SELECT_ELEM);
                 seconds.setAttribute(HTML.ID_ATTR, clientId+SELECT_SEC);
                 seconds.setAttribute(HTML.NAME_ATTR, clientId+SELECT_SEC);
                 seconds.setAttribute(HTML.CLASS_ATTR, selectInputDate.getTimeDropDownClass());
                 seconds.setAttribute(HTML.ONCHANGE_ATTR, DomBasicRenderer.ICESUBMITPARTIAL);
                 for (int i = 0; i < 60; i++ ) {
                     Element secondsOption = domContext.createElement(HTML.OPTION_ELEM);
                     secondsOption.setAttribute(HTML.VALUE_ATTR, String.valueOf(i));
                     String digits = String.valueOf(i);
                     if (i < 10) {
                        digits = "0" + digits;
                     }
                     Text secondText = domContext.createTextNode(digits);
                     secondsOption.appendChild(secondText);
                     if (i == sec) {
                         secondsOption.setAttribute(HTML.SELECTED_ATTR, "true");
                     }
                     seconds.appendChild(secondsOption);
                 }
                 Text secondColon = domContext.createTextNode(":");
                 tdSecColon.appendChild(secondColon);
                 tdSeconds.appendChild(seconds);
                 tr2.appendChild(tdSecColon);
                 tr2.appendChild(tdSeconds);
             }
 
             if (selectInputDate.isAmPm(facesContext)){
                 Element tdAamPm = domContext.createElement(HTML.TD_ELEM);
                 Element amPmElement = domContext.createElement(HTML.SELECT_ELEM);
                 amPmElement.setAttribute(HTML.ID_ATTR, clientId + SELECT_AM_PM);
                 amPmElement.setAttribute(HTML.NAME_ATTR, clientId + SELECT_AM_PM);
                 amPmElement.setAttribute(HTML.CLASS_ATTR, selectInputDate.getTimeDropDownClass());
                 amPmElement.setAttribute(HTML.ONCHANGE_ATTR, DomBasicRenderer.ICESUBMITPARTIAL);
                 String[] symbolsAmPm = symbols.getAmPmStrings();
 
                 Element amPmElementOption = domContext.createElement(HTML.OPTION_ELEM);
                 amPmElementOption.setAttribute(HTML.VALUE_ATTR, "AM");
                 Text amPmElementText = domContext.createTextNode(symbolsAmPm[0]);
                 amPmElementOption.appendChild(amPmElementText);
 
                 Element amPmElementOption2 = domContext.createElement(HTML.OPTION_ELEM);
                 amPmElementOption2.setAttribute(HTML.VALUE_ATTR, "PM");
                 Text amPmElementText2 = domContext.createTextNode(symbolsAmPm[1]);
                 amPmElementOption2.appendChild(amPmElementText2);
                 if (amPm == 0) {
                     amPmElementOption.setAttribute(HTML.SELECTED_ATTR, "true");
                 } else {
                     amPmElementOption2.setAttribute(HTML.SELECTED_ATTR, "true");
                 }
                 amPmElement.appendChild(amPmElementOption);
                 amPmElement.appendChild(amPmElementOption2);
                 tdAamPm.appendChild(amPmElement);
                 tr2.appendChild(tdAamPm);
             }
             table.appendChild(tfoot);
         }
         // purge child components as they have been encoded no need to keep them around
         selectInputDate.getChildren().clear();
 
         // steps to the position where the next sibling should be rendered
         domContext.stepOver();
     }
 
     private void writeMonthYearHeader(DOMContext domContext,
                                       FacesContext facesContext,
                                       ResponseWriter writer,
                                       SelectInputDate inputComponent,
                                       Calendar timeKeeper,
                                       int currentDay, Element headerTr,
                                       String styleClass, Locale currentLocale,
                                       String[] months, String[] weekdays, String[] weekdaysLong,
                                       Converter converter)
             throws IOException {
 
         Element table = domContext.createElement(HTML.TABLE_ELEM);
         table.setAttribute(HTML.CELLPADDING_ATTR, "0");
         table.setAttribute(HTML.CELLSPACING_ATTR, "0");
         table.setAttribute(HTML.WIDTH_ATTR, "100%");
         addAttributeToElementFromResource(facesContext,
                 YEAR_MONTH_SUMMARY, table, HTML.SUMMARY_ATTR);
 
         Element tr = domContext.createElement(HTML.TR_ELEM);
 
         Element headertd = domContext.createElement(HTML.TD_ELEM);
         table.appendChild(tr);
         headertd.appendChild(table);
         headerTr.appendChild(headertd);
 
         headertd.setAttribute(HTML.COLSPAN_ATTR, inputComponent.isRenderWeekNumbers() ? "8" : "7");
 
         int calYear = timeKeeper.get(Calendar.YEAR);
         if (inputComponent.getHightlightRules().containsKey(Calendar.YEAR + "$" + calYear)) {
             inputComponent.setHighlightYearClass(inputComponent.getHightlightRules().get(Calendar.YEAR + "$" + calYear) + " ");
         } else {
             inputComponent.setHighlightYearClass("");
         }
 
         int calMonth = timeKeeper.get(Calendar.MONTH) + 1;
         if (inputComponent.getHightlightRules().containsKey(Calendar.MONTH + "$" + calMonth)) {
             inputComponent.setHighlightMonthClass(inputComponent.getHightlightRules().get(Calendar.MONTH + "$" + calMonth) + " ");
         } else {
             inputComponent.setHighlightMonthClass("");
         }
         // first render month with navigation back and forward
         if (inputComponent.isRenderMonthAsDropdown()) {
             writeMonthDropdown(facesContext, domContext, inputComponent, tr,
                     months, timeKeeper, currentDay, styleClass,
                     converter);
         } else {
             Calendar cal = shiftMonth(facesContext, timeKeeper, currentDay, -1);
             writeCell(domContext, facesContext, writer, inputComponent,
                     "<", cal.getTime(), styleClass, tr,
                     inputComponent.getImageDir() +
                             inputComponent.getMovePreviousImage(), -1,
                     timeKeeper, months, weekdaysLong, converter);
 
             Element td = domContext.createElement(HTML.TD_ELEM);
             td.setAttribute(HTML.CLASS_ATTR, styleClass);
             td.setAttribute(HTML.WIDTH_ATTR, "40%");
             Text text = domContext
                     .createTextNode(months[timeKeeper.get(Calendar.MONTH)] + "");
             td.appendChild(text);
 
             tr.appendChild(td);
 
             cal = shiftMonth(facesContext, timeKeeper, currentDay, 1);
 /*
             int calYear = cal.get(Calendar.YEAR);
 
             if (inputComponent.getHightlightRules().containsKey(Calendar.YEAR+"$"+calYear)) {
                 inputComponent.setHighlightYearClass(inputComponent.getHightlightRules().get(Calendar.YEAR+"$"+calYear) + " ");
             } else {
                 inputComponent.setHighlightYearClass("");
             }
 
             int calMonth = cal.get(Calendar.MONTH);
             if (inputComponent.getHightlightRules().containsKey(Calendar.MONTH+"$"+calMonth)) {
                 inputComponent.setHighlightMonthClass(inputComponent.getHightlightRules().get(Calendar.MONTH+"$"+calMonth) + " ");
             } else {
                 inputComponent.setHighlightMonthClass("");
             }
 */
             writeCell(domContext, facesContext, writer, inputComponent,
                     ">", cal.getTime(), styleClass, tr,
                     inputComponent.getImageDir() +
                             inputComponent.getMoveNextImage(), -1,
                     timeKeeper, months, weekdaysLong, converter);
         }
 
         // second add an empty td
         Element emptytd = domContext.createElement(HTML.TD_ELEM);
         emptytd.setAttribute(HTML.CLASS_ATTR, styleClass);
         Text emptytext = domContext.createTextNode("");
         emptytd.appendChild(emptytext);
 
         tr.appendChild(emptytd);
 
         // third render year with navigation back and forward
         if (inputComponent.isRenderYearAsDropdown()) {
             writeYearDropdown(facesContext, domContext, inputComponent, tr,
                     timeKeeper, currentDay, styleClass, converter);
         } else {
             Calendar cal = shiftYear(facesContext, timeKeeper, currentDay, -1);
 
             writeCell(domContext, facesContext, writer, inputComponent,
                     "<<", cal.getTime(), styleClass, tr,
                     inputComponent.getImageDir() +
                             inputComponent.getMovePreviousImage(), -1,
                     timeKeeper, months, weekdaysLong, converter);
 
             Element yeartd = domContext.createElement(HTML.TD_ELEM);
             yeartd.setAttribute(HTML.CLASS_ATTR, styleClass);
             Text yeartext =
                     domContext.createTextNode("" + timeKeeper.get(Calendar.YEAR));
             yeartd.appendChild(yeartext);
 
             tr.appendChild(yeartd);
 
             cal = shiftYear(facesContext, timeKeeper, currentDay, 1);
 
             writeCell(domContext, facesContext, writer, inputComponent,
                     ">>", cal.getTime(), styleClass, tr,
                     inputComponent.getImageDir() +
                             inputComponent.getMoveNextImage(), -1,
                     timeKeeper, months, weekdaysLong, converter);
         }
 
     }
 
     private void writeMonthDropdown(FacesContext facesContext,
                                     DOMContext domContext,
                                     SelectInputDate component,
                                     Element tr,
                                     String[] months,
                                     Calendar timeKeeper,
                                     int currentDay,
                                     String styleClass,
                                     Converter converter) throws IOException {
         Element td = domContext.createElement(HTML.TD_ELEM);
         if (styleClass != null) {
             td.setAttribute(HTML.CLASS_ATTR, styleClass);
         }
         tr.appendChild(td);
 
         domContext.setCursorParent(td);
 
         HtmlSelectOneMenu dropDown = new HtmlSelectOneMenu();
         dropDown.setId(component.getId() + SELECT_MONTH);
         dropDown.setPartialSubmit(true);
         dropDown.setTransient(true);
         dropDown.setImmediate(component.isImmediate());
         dropDown.setDisabled(component.isDisabled() || component.isReadonly());
         dropDown.setStyleClass(component.getMonthYearDropdownClass());
         component.getChildren().add(dropDown);
 
         UISelectItem selectItem;
         Calendar calendar;
         int currentMonth = timeKeeper.get(Calendar.MONTH);
         for (int i = 0; i < months.length; i++) {
             selectItem = new UISelectItem();
             calendar = shiftMonth(facesContext, timeKeeper, currentDay, i - currentMonth);
             selectItem.setItemValue(converter.getAsString(facesContext, component, calendar.getTime()));
             selectItem.setItemLabel(months[i]);
             dropDown.getChildren().add(selectItem);
             if (i == currentMonth) {
                 dropDown.setValue(selectItem.getItemValue());
             }
         }
 
         dropDown.encodeBegin(facesContext);
         dropDown.encodeChildren(facesContext);
         dropDown.encodeEnd(facesContext);
         component.getChildren().remove(dropDown);
 
         domContext.stepOver();
     }
 
     private void writeYearDropdown(FacesContext facesContext,
                                    DOMContext domContext,
                                    SelectInputDate component,
                                    Element tr,
                                    Calendar timeKeeper,
                                    int currentDay,
                                    String styleClass,
                                    Converter converter) throws IOException {
         Element td = domContext.createElement(HTML.TD_ELEM);
         if (styleClass != null) {
             td.setAttribute(HTML.CLASS_ATTR, styleClass);
         }
         tr.appendChild(td);
 
         domContext.setCursorParent(td);
 
         HtmlSelectOneMenu dropDown = new HtmlSelectOneMenu();
         dropDown.setId(component.getId() + SELECT_YEAR);
         dropDown.setPartialSubmit(true);
         dropDown.setTransient(true);
         dropDown.setImmediate(component.isImmediate());
         dropDown.setDisabled(component.isDisabled() || component.isReadonly());
         dropDown.setStyleClass(component.getMonthYearDropdownClass());
         component.getChildren().add(dropDown);
 
         int timeKeeperYear = timeKeeper.get(Calendar.YEAR);
         int startYear = timeKeeperYear - yearListSize / 2; // not perfectly centered if size is even
         UISelectItem selectItem;
         Calendar calendar;
         String itemValue, itemLabel;
 
         for (int i = startYear - 1, j = i, k = startYear + yearListSize; i <= k; i++) {
             if (i == j) {
                 calendar = shiftYear(facesContext, timeKeeper, currentDay, -yearListSize);
                 itemLabel = MessageUtils.getResource(facesContext, PREV_YEAR_LABEL);
             } else if (i == k) {
                 calendar = shiftYear(facesContext, timeKeeper, currentDay, yearListSize);
                 itemLabel = MessageUtils.getResource(facesContext, NEXT_YEAR_LABEL);
             } else {
                 calendar = shiftYear(facesContext, timeKeeper, currentDay, i - timeKeeperYear);
                 itemLabel = String.valueOf(calendar.get(Calendar.YEAR));
             }
             itemValue = converter.getAsString(facesContext, component, calendar.getTime());
             selectItem = new UISelectItem();
             selectItem.setItemValue(itemValue);
             selectItem.setItemLabel(itemLabel);
             dropDown.getChildren().add(selectItem);
             if (i == timeKeeperYear) {
                 dropDown.setValue(itemValue);
             }
         }
 
         dropDown.encodeBegin(facesContext);
         dropDown.encodeChildren(facesContext);
         dropDown.encodeEnd(facesContext);
         component.getChildren().remove(dropDown);
 
         domContext.stepOver();
     }
 
     private Calendar shiftMonth(FacesContext facesContext,
                                 Calendar timeKeeper, int currentDay,
                                 int shift) {
         Calendar cal = copyCalendar(facesContext, timeKeeper);
 
         cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + shift);
 
         if (currentDay > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
             currentDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
         }
 
         cal.set(Calendar.DAY_OF_MONTH, currentDay);
         return cal;
     }
 
     private Calendar shiftYear(FacesContext facesContext,
                                Calendar timeKeeper, int currentDay, int shift) {
         Calendar cal = copyCalendar(facesContext, timeKeeper);
 
         cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + shift);
 
         if (currentDay > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
             currentDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
         }
 
         cal.set(Calendar.DAY_OF_MONTH, currentDay);
         return cal;
     }
 
     private Calendar copyCalendar(FacesContext facesContext,
                                   Calendar timeKeeper) {
         Calendar cal = (Calendar) timeKeeper.clone();
         return cal;
     }
 
     private void writeWeekDayNameHeader(DOMContext domContext,
                                         int weekStartsAtDayIndex,
                                         String[] weekdays,
                                         FacesContext facesContext,
                                         ResponseWriter writer,
                                         SelectInputDate inputComponent, Element tr,
                                         String styleClass,
                                         Calendar timeKeeper,
                                         String[] months, String[] weekdaysLong,
                                         Converter converter)
             throws IOException {
         if (inputComponent.isRenderWeekNumbers()) {
             Element td = domContext.createElement(HTML.TD_ELEM);
             td.setAttribute(HTML.CLASS_ATTR, Util.getQualifiedStyleClass(inputComponent,
                     CSS_DEFAULT.DEFAULT_WEEK_NUM_HDR_CLASS, inputComponent.isDisabled()));
             addAttributeToElementFromResource(facesContext, WEEK_NUM_HDR_TITLE, td, HTML.TITLE_ATTR);
             tr.appendChild(td);
             Text text = domContext.createTextNode(MessageUtils.getResource(facesContext, WEEK_NUM_HDR));
             td.appendChild(text);
         }
         // the week can start with Sunday (index 0) or Monday (index 1)
         for (int i = weekStartsAtDayIndex; i < weekdays.length; i++) {
             writeCell(domContext, facesContext,
                     writer, inputComponent, weekdays[i], null, styleClass, tr,
                     null, i,
                     timeKeeper, months, weekdaysLong, converter);
         }
 
         // if week start on Sunday this block is not executed
         // if week start on Monday this block will run once adding Sunday to End of week.
         for (int i = 0; i < weekStartsAtDayIndex; i++) {
             writeCell(domContext, facesContext, writer,
                     inputComponent, weekdays[i], null, styleClass, tr, null, i,
                     timeKeeper, months, weekdaysLong, converter);
         }
     }
 
     private void writeDays(DOMContext domContext, FacesContext facesContext,
                            ResponseWriter writer,
                            SelectInputDate inputComponent, Calendar timeKeeper,
                            int currentDay, int weekStartsAtDayIndex,
                            int weekDayOfFirstDayOfMonth, int lastDayInMonth,
                            Element table, String[] months,
                            String[] weekdays, String[] weekdaysLong,
                            Converter converter, Date value)
             throws IOException {
         Calendar cal;
 
         int space = (weekStartsAtDayIndex < weekDayOfFirstDayOfMonth) ?
                 (weekDayOfFirstDayOfMonth - weekStartsAtDayIndex)
                 : (weekdays.length - weekStartsAtDayIndex +
                 weekDayOfFirstDayOfMonth);
 
         if (space == weekdays.length) {
             space = 0;
         }
 
         int columnIndexCounter = 0;
 
         Element tr1 = null;
         for (int i = 0; i < space; i++) {
             if (columnIndexCounter == 0) {
                 tr1 = domContext.createElement(HTML.TR_ELEM);
                 table.appendChild(tr1);
                 if (inputComponent.isRenderWeekNumbers()) {
                     cal = copyCalendar(facesContext, timeKeeper);
                     cal.set(Calendar.DAY_OF_MONTH, 1);
                     Element td = domContext.createElement(HTML.TD_ELEM);
                     td.setAttribute(HTML.CLASS_ATTR, Util.getQualifiedStyleClass(inputComponent,
                             CSS_DEFAULT.DEFAULT_WEEK_NUM_CLASS, inputComponent.isDisabled()));
                     tr1.appendChild(td);
                     Text text = domContext.createTextNode(String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)));
                     td.appendChild(text);
                 }
             }
 
             writeCell(domContext, facesContext, writer, inputComponent, NBSP,
                     null, inputComponent.getDayCellClass(), tr1, null,
                     (weekStartsAtDayIndex + i) % 7,
                     timeKeeper, months, weekdaysLong, converter);
             columnIndexCounter++;
         }
 
         Element tr2 = null;
         for (int i = 0; i < lastDayInMonth; i++) {
             if (columnIndexCounter == 0) {
                 // don't create a new row until we have finished the last
                 tr2 = domContext.createElement(HTML.TR_ELEM);
                 table.appendChild(tr2);
             }
 
             cal = copyCalendar(facesContext, timeKeeper);
             cal.set(Calendar.DAY_OF_MONTH,
                     i + 1); // i starts at 0 DAY_OF_MONTH start at 1
 
             // get day, month and year
             // use these to check if the currentDayCell style class should be used
             int day = 0;
             int month = 0;
             int year = 0;
             try {
                 Calendar current = copyCalendar(facesContext, timeKeeper);
                 current.setTime(value);
 
                 day = current.get(Calendar.DAY_OF_MONTH); // starts with 1
                 month = current.get(Calendar.MONTH); // starts with 0
                 year = current.get(Calendar.YEAR);
             } catch (Exception e) {
                 // hmmm this should never happen
             }
             if (inputComponent.isRenderWeekNumbers() && columnIndexCounter == 0) {
                 Element td = domContext.createElement(HTML.TD_ELEM);
                 td.setAttribute(HTML.CLASS_ATTR, Util.getQualifiedStyleClass(inputComponent,
                         CSS_DEFAULT.DEFAULT_WEEK_NUM_CLASS, inputComponent.isDisabled()));
                 tr2.appendChild(td);
                 Text text = domContext.createTextNode(String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)));
                 td.appendChild(text);
             }
 
             if (inputComponent.getHightlightRules().size() > 0) {
                 int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
                 int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
                 int date = cal.get(Calendar.DATE);
                 int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                 int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                 int dayOfWeekInMonth = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
 
                 if (inputComponent.getHightlightRules().containsKey(Calendar.WEEK_OF_YEAR + "$" + weekOfYear)) {
                     inputComponent.addHighlightWeekClass(String.valueOf(inputComponent.getHightlightRules().get(Calendar.WEEK_OF_YEAR + "$" + weekOfYear)));
                 }
                 if (inputComponent.getHightlightRules().containsKey(Calendar.WEEK_OF_MONTH + "$" + weekOfMonth)) {
                     inputComponent.addHighlightWeekClass(String.valueOf(inputComponent.getHightlightRules().get(Calendar.WEEK_OF_MONTH + "$" + weekOfMonth)));
                 }
                 if (inputComponent.getHightlightRules().containsKey(Calendar.DATE + "$" + date)) {
                     inputComponent.addHighlightDayClass(String.valueOf(inputComponent.getHightlightRules().get(Calendar.DATE + "$" + date)));
                 }
                 if (inputComponent.getHightlightRules().containsKey(Calendar.DAY_OF_YEAR + "$" + dayOfYear)) {
                     inputComponent.addHighlightDayClass(String.valueOf(inputComponent.getHightlightRules().get(Calendar.DAY_OF_YEAR + "$" + dayOfYear)));
                 }
                 if (inputComponent.getHightlightRules().containsKey(Calendar.DAY_OF_WEEK + "$" + dayOfWeek)) {
                     inputComponent.addHighlightDayClass(String.valueOf(inputComponent.getHightlightRules().get(Calendar.DAY_OF_WEEK + "$" + dayOfWeek)));
                 }
                 if (inputComponent.getHightlightRules().containsKey(Calendar.DAY_OF_WEEK_IN_MONTH + "$" + dayOfWeekInMonth)) {
                     inputComponent.addHighlightDayClass(String.valueOf(inputComponent.getHightlightRules().get(Calendar.DAY_OF_WEEK_IN_MONTH + "$" + dayOfWeekInMonth)));
                 }
             }
 
             String cellStyle = CSSNamePool.get(inputComponent.getDayCellClass() + " " + inputComponent.getHighlightDayCellClass());
 
 
             if ((cal.get(Calendar.DAY_OF_MONTH) == day) &&
                     (cal.get(Calendar.MONTH) == month) &&
                     (cal.get(Calendar.YEAR) == year)) {
                 cellStyle = inputComponent.getCurrentDayCellClass();
             }
 
 
             // do not automatically select date when navigating by month
             if ((cal.get(Calendar.DAY_OF_MONTH) == day) &&
                     (cal.get(Calendar.MONTH) == month) &&
                     (cal.get(Calendar.YEAR) == year)) {
                 cellStyle = inputComponent.getCurrentDayCellClass();
             }
 
             if (tr2 == null) {
                 // finish the first row
                 writeCell(domContext, facesContext, writer,
                         inputComponent, String.valueOf(i + 1), cal.getTime(),
                         cellStyle, tr1, null, i,
                         timeKeeper, months, weekdaysLong, converter);
             } else {
                 // write to new row
                 writeCell(domContext, facesContext, writer,
                         inputComponent, String.valueOf(i + 1), cal.getTime(),
                         cellStyle, tr2, null, i,
                         timeKeeper, months, weekdaysLong, converter);
             }
 
             columnIndexCounter++;
 
             if (columnIndexCounter == weekdays.length) {
                 columnIndexCounter = 0;
             }
             inputComponent.resetHighlightClasses(Calendar.WEEK_OF_YEAR);
         }
 
         if ((columnIndexCounter != 0) && (tr2 != null)) {
             for (int i = columnIndexCounter; i < weekdays.length; i++) {
                 writeCell(domContext, facesContext, writer,
                         inputComponent, NBSP, null,
                         inputComponent.getDayCellClass(), tr2, null,
                         (weekStartsAtDayIndex + i) % 7,
                         timeKeeper, months, weekdaysLong, converter);
             }
         }
 
     }
 
     private void writeCell(DOMContext domContext, FacesContext facesContext,
                            ResponseWriter writer, SelectInputDate component,
                            String content,
                            Date valueForLink, String styleClass, Element tr,
                            String imgSrc, int weekDayIndex,
                            Calendar timeKeeper,
                            String[] months, String[] weekdaysLong,
                            Converter converter)
             throws IOException {
         Element td = domContext.createElement(HTML.TD_ELEM);
         tr.appendChild(td);
 
         if (styleClass != null) {
             td.setAttribute(HTML.CLASS_ATTR, styleClass);
         }
 
         if (valueForLink == null) {
             if (content != null && content.length() > 0) {
                 //code path is complex, so only NBSP is passed
                 //through unescaped
                 Text text;
                 if (NBSP.equals(content))  {
                     text = domContext.createTextNodeUnescaped(content);
                 } else {
                     text = domContext.createTextNode(content);
                 }
                 td.setAttribute(HTML.TITLE_ATTR, weekdaysLong[weekDayIndex]);
                 td.appendChild(text);
             }
         } else {
             // set cursor to render into the td
             domContext.setCursorParent(td);
             writeLink(content, component, facesContext, valueForLink,
                     styleClass, imgSrc, td, timeKeeper,
                     months, weekdaysLong, converter);
             // steps to the position where the next sibling should be rendered
             domContext.stepOver();
         }
 
     }
 
     private void writeLink(String content,
                            SelectInputDate component,
                            FacesContext facesContext,
                            Date valueForLink,
                            String styleClass,
                            String imgSrc,
                            Element td,
                            Calendar timeKeeper,
                            String[] months, String[] weekdaysLong,
                            Converter converter)
             throws IOException {
 
         HtmlCommandLink link = new HtmlCommandLink();
         Calendar cal = copyCalendar(facesContext, timeKeeper);
         cal.setTime(valueForLink);
         String month = months[cal.get(Calendar.MONTH)];
         String year = String.valueOf(cal.get(Calendar.YEAR));
         int dayInt = cal.get(Calendar.DAY_OF_WEEK);
         dayInt = mapCalendarDayToCommonDay(dayInt);
         String day = weekdaysLong[dayInt];
         String altText = null;
         String titleText = null;
         // assign special ids for navigation links
         if (content.equals("<")) {
             link.setId(component.getId() + this.PREV_MONTH);
             altText = getMessageWithParamFromResource(
                     facesContext, PREV_MONTH_ALT, month);
             titleText = getMessageWithParamFromResource(
                     facesContext, PREV_MONTH_TITLE, month);
         } else if (content.equals(">")) {
             link.setId(component.getId() + this.NEXT_MONTH);
             altText = getMessageWithParamFromResource(
                     facesContext, NEXT_MONTH_ALT, month);
             titleText = getMessageWithParamFromResource(
                     facesContext, NEXT_MONTH_TITLE, month);
         } else if (content.equals(">>")) {
             link.setId(component.getId() + this.NEXT_YEAR);
             altText = getMessageWithParamFromResource(
                     facesContext, NEXT_YEAR_ALT, year);
             titleText = getMessageWithParamFromResource(
                     facesContext, NEXT_YEAR_TITLE, year);
         } else if (content.equals("<<")) {
             link.setId(component.getId() + this.PREV_YEAR);
             altText = getMessageWithParamFromResource(
                     facesContext, PREV_YEAR_ALT, year);
             titleText = getMessageWithParamFromResource(
                     facesContext, PREV_YEAR_TITLE, year);
         } else {
             link.setId(component.getId() + CALENDAR + content.hashCode());
             if (log.isDebugEnabled()) {
                 log.debug("linkId=" +
                         component.getId() + CALENDAR + content.hashCode());
             }
         }
 
         link.setPartialSubmit(true);
         link.setTransient(true);
         link.setImmediate(component.isImmediate());
         link.setDisabled(((SelectInputDate) component).isDisabled() || component.isReadonly());
 
         if (imgSrc != null) {
             HtmlGraphicImage img = new HtmlGraphicImage();
             if (component.isImageDirSet()) {
                 img.setUrl(imgSrc);
             } else {
                 // ICE-2127: allow override of button images via CSS
                 // getImageDir() returns default
                 // without a dummy image Firefox would show the alt text
                 img.setUrl(component.getImageDir() + "spacer.gif");
                 if (content.equals("<") || content.equals("<<")) {
                     img.setStyleClass(component.getMovePrevClass());
                 } else if (content.equals(">") || content.equals(">>")) {
                     img.setStyleClass(component.getMoveNextClass());
                 }
             }
             img.setHeight("16");
             img.setWidth("17");
             img.setStyle("border:none;");
             if (altText != null)
                 img.setAlt(altText);
             if (titleText != null)
                 img.setTitle(titleText);
             img.setId(component.getId() + "_img_" + content.hashCode());
             img.setTransient(true);
             link.getChildren().add(img);
         } else {
             HtmlOutputText text = new HtmlOutputText();
             text.setValue(content);
             text.setId(component.getId() + "_text_" + content.hashCode());
             text.setTransient(true);
             text.setTitle(day);
             link.getChildren().add(text);
         }
         // links are focus aware       
         UIParameter parameter = new UIParameter();
         parameter.setId(
                 component.getId() + "_" + valueForLink.getTime() + "_param");
         parameter.setTransient(true);
         parameter.setName(component.getClientId(facesContext) + CALENDAR_CLICK);
         parameter.setValue(
                 converter.getAsString(facesContext, component, valueForLink));
 
         component.getChildren().add(link);
         link.getChildren().add(parameter);
 
         //don't add this parameter for next and previouse button/link        
         if (!content.equals("<") && !content.equals(">") &&
                 !content.equals(">>") && !content.equals("<<")) {
             //this parameter would be use to close the popup selectinputdate after date selection.
             parameter = new UIParameter();
             parameter.setId(component.getId() + "_" + valueForLink.getTime() +
                     "_" + DATE_SELECTED);
             parameter.setName(getHiddenFieldName(facesContext, component));
             parameter.setValue("false");
             link.getChildren().add(parameter);
         }
         link.encodeBegin(facesContext);
         link.encodeChildren(facesContext);
         link.encodeEnd(facesContext);
         td.setAttribute(HTML.ID_ATTR, CSSNamePool.get(link.getClientId(facesContext) + "td"));
         try {
             Integer.parseInt(content);
             ((SelectInputDate) component).getLinkMap()
                     .put(link.getClientId(facesContext), td);
             if (styleClass.equals(CSS_DEFAULT.DEFAULT_CALENDAR + CSS_DEFAULT
                     .DEFAULT_CURRENTDAYCELL_CLASS)) {
                 ((SelectInputDate) component)
                         .setSelectedDayLink(link.getClientId(facesContext));
             }
         } catch (NumberFormatException e) {
 
         }
 
 
     }
 
     protected void addAttributeToElementFromResource(
             FacesContext facesContext, String resName, Element elem, String attrib) {
         String res = MessageUtils.getResource(facesContext, resName);
         if (res != null && res.length() > 0) {
             elem.setAttribute(attrib, res);
         }
     }
 
     protected String getMessageWithParamFromResource(
             FacesContext facesContext, String resName, String param) {
         String msg = null;
         if (param != null && param.length() > 0) {
             String messagePattern = MessageUtils.getResource(
                     facesContext, resName);
             if (messagePattern != null && messagePattern.length() > 0) {
                 msg = MessageFormat.format(
                         messagePattern, new Object[]{param});
             }
         }
         return msg;
     }
 
     private int mapCalendarDayToCommonDay(int day) {
         switch (day) {
             case Calendar.TUESDAY:
                 return 1;
             case Calendar.WEDNESDAY:
                 return 2;
             case Calendar.THURSDAY:
                 return 3;
             case Calendar.FRIDAY:
                 return 4;
             case Calendar.SATURDAY:
                 return 5;
             case Calendar.SUNDAY:
                 return 6;
             default:
                 return 0;
         }
     }
 
     private static String[] mapWeekdays(DateFormatSymbols symbols) {
         String[] weekdays = new String[7];
 
         String[] localeWeekdays = symbols.getShortWeekdays();
 
         weekdays[0] = localeWeekdays[Calendar.MONDAY];
         weekdays[1] = localeWeekdays[Calendar.TUESDAY];
         weekdays[2] = localeWeekdays[Calendar.WEDNESDAY];
         weekdays[3] = localeWeekdays[Calendar.THURSDAY];
         weekdays[4] = localeWeekdays[Calendar.FRIDAY];
         weekdays[5] = localeWeekdays[Calendar.SATURDAY];
         weekdays[6] = localeWeekdays[Calendar.SUNDAY];
 
         return weekdays;
     }
 
     private static String[] mapWeekdaysLong(DateFormatSymbols symbols) {
         String[] weekdays = new String[7];
 
         String[] localeWeekdays = symbols.getWeekdays();
 
         weekdays[0] = localeWeekdays[Calendar.MONDAY];
         weekdays[1] = localeWeekdays[Calendar.TUESDAY];
         weekdays[2] = localeWeekdays[Calendar.WEDNESDAY];
         weekdays[3] = localeWeekdays[Calendar.THURSDAY];
         weekdays[4] = localeWeekdays[Calendar.FRIDAY];
         weekdays[5] = localeWeekdays[Calendar.SATURDAY];
         weekdays[6] = localeWeekdays[Calendar.SUNDAY];
 
         return weekdays;
     }
 
     /**
      * @param symbols
      * @return months - String[] containing localized month names
      */
     public static String[] mapMonths(DateFormatSymbols symbols) {
         String[] months = new String[12];
 
         String[] localeMonths = symbols.getMonths();
 
         months[0] = localeMonths[Calendar.JANUARY];
         months[1] = localeMonths[Calendar.FEBRUARY];
         months[2] = localeMonths[Calendar.MARCH];
         months[3] = localeMonths[Calendar.APRIL];
         months[4] = localeMonths[Calendar.MAY];
         months[5] = localeMonths[Calendar.JUNE];
         months[6] = localeMonths[Calendar.JULY];
         months[7] = localeMonths[Calendar.AUGUST];
         months[8] = localeMonths[Calendar.SEPTEMBER];
         months[9] = localeMonths[Calendar.OCTOBER];
         months[10] = localeMonths[Calendar.NOVEMBER];
         months[11] = localeMonths[Calendar.DECEMBER];
 
         return months;
     }
 
     /**
      * @param facesContext
      * @param uiComponent
      * @return id - used for the commandlink hidden field in the form
      */
     public String getLinkId(FacesContext facesContext,
                             UIComponent uiComponent) {
         //this is a fix for bug 340
         UIComponent form = findForm(uiComponent);
         String formId = form.getClientId(facesContext);
         return formId + ID_SUFFIX;
     }
 
     private int checkLink(Object eventCapturedId, String clickedLink, String clientId) {
 //System.out.println("checkLink()  clickedLink: " + clickedLink);
         if (clickedLink == null || clickedLink.length() == 0) {
 //System.out.println("checkLink()  eventCapturedId: " + eventCapturedId);
             if ((clientId + SELECT_HOUR).equals(eventCapturedId)) {
                 return IS_HOUR;
             } else if ((clientId + SELECT_MIN).equals(eventCapturedId)) {
                 return IS_MIN;
             }
             else if( (clientId+SELECT_SEC).equals(eventCapturedId) ) {
                 return IS_SEC;
             }
             else if ((clientId + SELECT_AM_PM).equals(eventCapturedId)) {
                 return IS_AM_PM;
             }
             return IS_NOT;
         } else if ((clientId + CALENDAR_BUTTON).equals(clickedLink)) {
             return IS_CALENDAR_BUTTON;
         } else if (clickedLink.startsWith(clientId + CALENDAR)) {
             return IS_CALENDAR;
         } else if ((clientId + PREV_MONTH).equals(clickedLink)) {
             return IS_PREV_MONTH;
         } else if ((clientId + NEXT_MONTH).equals(clickedLink)) {
             return IS_NEXT_MONTH;
         } else if ((clientId + PREV_YEAR).equals(clickedLink)) {
             return IS_PREV_YEAR;
         } else if ((clientId + NEXT_YEAR).equals(clickedLink)) {
             return IS_NEXT_YEAR;
         }
         return IS_NOT;
     }
 
     /* (non-Javadoc)
     * @see com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
     */
     public void decode(FacesContext facesContext, UIComponent component) {
         validateParameters(facesContext, component, SelectInputDate.class);
         SelectInputDate dateSelect = (SelectInputDate) component;
         Map requestParameterMap =
                 facesContext.getExternalContext().getRequestParameterMap();
         Object linkId = getLinkId(facesContext, component);
         Object clickedLink = requestParameterMap.get(linkId);
         String clientId = component.getClientId(facesContext);
 //System.out.println("SIDR.decode()  clientId: " + clientId);
 
         Object eventCapturedId = requestParameterMap.get("ice.event.captured");
 //        if (eventCapturedId != null && 
 //        		!eventCapturedId.toString().startsWith(clientId)) {
 //        	return;
 //        }
         
         String monthClientId = clientId + SELECT_MONTH;
         String yearClientId = clientId + SELECT_YEAR;
         String hoursClientId = clientId + SELECT_HOUR;
         String minutesClientId = clientId + SELECT_MIN;
         String secondsClientId = clientId + SELECT_SEC;
         String amPmClientId = clientId + SELECT_AM_PM;
         if (requestParameterMap.containsKey(hoursClientId)) {
 //System.out.println("SIDR.decode()    Hours: " + requestParameterMap.get(hoursClientId));
             dateSelect.setHoursSubmittedValue(requestParameterMap.get(hoursClientId));
         }
 
         if (requestParameterMap.containsKey(minutesClientId)) {
 //System.out.println("SIDR.decode()    Minutes: " + requestParameterMap.get(minutesClientId));
             dateSelect.setMinutesSubmittedValue(requestParameterMap.get(minutesClientId));
         }
 
         if (requestParameterMap.containsKey(secondsClientId)) {
 //System.out.println("SIDR.decode()    Seconds: " + requestParameterMap.get(secondsClientId));
             dateSelect.setSecondsSubmittedValue(requestParameterMap.get(secondsClientId));
         }
 
         if (requestParameterMap.containsKey(amPmClientId)) {
 //System.out.println("SIDR.decode()    AmPm: " + requestParameterMap.get(amPmClientId));
             dateSelect.setAmPmSubmittedValue(requestParameterMap.get(amPmClientId));
         } else {
 //System.out.println("SIDR.decode()    NOT  AmPm");
             dateSelect.setAmPmSubmittedValue(null);
         }
 
         if (monthClientId.equals(eventCapturedId)) {
             dateSelect.setNavEvent(true);
             dateSelect.setNavDate((Date) getConvertedValue(facesContext, component, requestParameterMap.get(monthClientId)));
         } else if (yearClientId.equals(eventCapturedId)) {
             dateSelect.setNavEvent(true);
             dateSelect.setNavDate((Date) getConvertedValue(facesContext, component, requestParameterMap.get(yearClientId)));
         } else if (clickedLink != null) {
             if (log.isDebugEnabled()) {
                 log.debug("linkId::" + linkId + "  clickedLink::" +
                         clickedLink + "  clientId::" + clientId);
             }
 
             String sclickedLink = (String) clickedLink;
             int check = checkLink(eventCapturedId, sclickedLink, clientId);
 /*
 String[] checkStrings = new String[] {
     "IS_NOT",
     "IS_CALENDAR_BUTTON",
     "IS_CALENDAR",
     "IS_PREV_MONTH",
     "IS_NEXT_MONTH",
     "IS_PREV_YEAR",
     "IS_NEXT_YEAR",
     "IS_HOUR",
     "IS_MIN",
     "IS_SEC",
     "IS_AM_PM"
 };
 System.out.println("SIDR.decode()    link: " + checkStrings[check]);
 */
             if (check != IS_NOT) {
                 if (log.isDebugEnabled()) {
                     log.debug("---------------------------------");
                     log.debug("----------START::DECODE----------");
                     log.debug("---------------------------------");
                     log.debug("decode::linkId::" + linkId + "=" + clickedLink +
                             " clientId::" + clientId);
                 }
 
                 if (check == IS_PREV_MONTH ||
                         check == IS_NEXT_MONTH ||
                         check == IS_PREV_YEAR ||
                         check == IS_NEXT_YEAR) {
                     if (log.isDebugEnabled()) {
                         log.debug("-------------Navigation Event-------------");
                     }
                     decodeNavigation(facesContext, component);
                 } else if (check == IS_CALENDAR) {
                     if (log.isDebugEnabled()) {
                         log.debug(
                                 "-------------Select Date Event-------------");
                     }
                     decodeSelectDate(facesContext, component);
                 } else if (check == IS_CALENDAR_BUTTON) {
                     if (log.isDebugEnabled()) {
                         log.debug(
                                 "-------------Popup Event-------------------");
                     }
                     decodePopup(facesContext, component);
                 } else if (check == IS_HOUR ||
                         check == IS_MIN ||
                         check == IS_SEC ||
                         check == IS_AM_PM) {
                     decodeTime(facesContext, component);
                 }
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("-------------InputText enterkey Event ??----");
                 }
                 boolean enterKeyPressed = dateSelect.isEnterKeyPressed(facesContext);
 //System.out.println("SIDR.decode()    enterKeyPressed: " + enterKeyPressed);
                 decodeInputText(facesContext, component);
                 if (enterKeyPressed) {
                     dateSelect.setHoursSubmittedValue(null);
                     dateSelect.setMinutesSubmittedValue(null);
                     dateSelect.setSecondsSubmittedValue(null);
                     if ("13".equalsIgnoreCase(String.valueOf(requestParameterMap.get("ice.event.keycode")))) {
                         component.queueEvent(new ActionEvent(component));
                     }
                 }
             }
         }
     }
 
     private void decodeNavigation(FacesContext facesContext,
                                   UIComponent component) {
         Map requestParameterMap =
                 facesContext.getExternalContext().getRequestParameterMap();
         SelectInputDate dateSelect = (SelectInputDate) component;
 
         // set the navDate on the Calendar
         if (log.isDebugEnabled()) {
             log.debug("setNavDate::");
             log.debug("#################################");
         }
         dateSelect.setNavEvent(true);
         dateSelect.setNavDate(
                 (Date) getConvertedValue(
                         facesContext, dateSelect, requestParameterMap.get(
                                 dateSelect.getClientId(facesContext) + CALENDAR_CLICK)));
     }
 
     private void decodePopup(FacesContext facesContext, UIComponent component) {
         Map requestParameterMap =
                 facesContext.getExternalContext().getRequestParameterMap();
         String popupState = getHiddenFieldName(facesContext, component);
         String showPopup = (String) requestParameterMap.get(popupState);
         SelectInputDate dateSelect = (SelectInputDate) component;
 
         if (log.isDebugEnabled()) {
             log.debug("decodePopup::" + showPopup);
             log.debug("#################################");
         }
         Object eventCapturedId = requestParameterMap.get("ice.event.captured");
         // check showPopup
         if (showPopup != null ) {
 
         	if (!(!dateSelect.isShowPopup() &&
         			eventCapturedId != null && 
               		!eventCapturedId.toString().endsWith(CALENDAR_BUTTON))) {
         		dateSelect.setShowPopup(!dateSelect.isShowPopup());
         	}
             // submit value in text field
             decodeInputText(facesContext, component);
             dateSelect.setHoursSubmittedValue(null);
             dateSelect.setMinutesSubmittedValue(null);
             if ("13".equalsIgnoreCase(String.valueOf(requestParameterMap.get("ice.event.keycode")))) {
                 component.queueEvent(new ActionEvent(component));
             }
         }
         // not a nav event
         dateSelect.setNavEvent(false);
     }
 
     private void decodeSelectDate(FacesContext facesContext,
                                   UIComponent component) {
 
         Map requestParameterMap =
                 facesContext.getExternalContext().getRequestParameterMap();
         String popupState = getHiddenFieldName(facesContext, component);
         String showPopup = (String) requestParameterMap.get(popupState);
         SelectInputDate dateSelect = (SelectInputDate) component;
         String clientId = component.getClientId(facesContext);
         if (log.isDebugEnabled()) {
             log.debug("selectDate::showPopup" + showPopup);
             log.debug("#################################");
         }
         //1. The popup will be closed automatically on selecting a date when 
         //there isn't any time input elements on the calendar (same as 1.7.2 behaviour)
         //2. The popup will NOT be closed automatically on selecting a date, if 
         //there are time fields on the popup
         if (!dateSelect.isTime(facesContext)) {
             dateSelect.setShowPopup(false);
             //ICE-4405 (selectInputDate loses focus after closing calendar.)
             if (dateSelect.isRenderAsPopup()) {
                 JavascriptContext.applicationFocus(facesContext, clientId + CALENDAR_BUTTON);
             }
         }
         if (log.isDebugEnabled()) {
             log.debug("decodeUIInput::");
             log.debug("#################################");
         }
 
         CustomComponentUtils.decodeUIInput(facesContext, component, clientId + CALENDAR_CLICK);
         Object submittedValue = dateSelect.getSubmittedValue();
         if (submittedValue instanceof String &&
                 submittedValue.toString().trim().length() > 0) {
             String inputTextDateAndTime = mergeTimeIntoDateString(
                     facesContext, dateSelect, clientId, submittedValue.toString());
             dateSelect.setSubmittedValue(inputTextDateAndTime);
         }
 
         // not a navigation event
         dateSelect.setNavEvent(false);
     }
 
     private void decodeInputText(FacesContext facesContext,
                                  UIComponent component) {
         Map requestParameterMap =
                 facesContext.getExternalContext().getRequestParameterMap();
         String popupState = getHiddenFieldName(facesContext, component);
         String showPopup = (String) requestParameterMap.get(popupState);
         SelectInputDate dateSelect = (SelectInputDate) component;
         String clientId = dateSelect.getClientId(facesContext);
 //System.out.println("SIDR.decodeInputText()  clientId: " + clientId);
         Object linkId = getLinkId(facesContext, component);
         Object clickedLink = requestParameterMap.get(linkId);
         String inputTextDateId = clientId + SelectInputDate.CALENDAR_INPUTTEXT;
 //System.out.println("SIDR.decodeInputText()  inputTextDateId: " + inputTextDateId);
 
         // inputtext is only available in popup mode 
         if (requestParameterMap.containsKey(inputTextDateId)) {
             if (log.isDebugEnabled()) {
                 log.debug("decoding InputText EnterKey::");
                 log.debug("###################################");
             }
 //            if (showPopup != null) {
 //                if (checkLink((String) clickedLink, clientId) != IS_NOT) {
 //                    if (showPopup.equalsIgnoreCase("true")) {
 ////System.out.println("SIDR.decodeInputText()  setShowPopup( true )");
 //                        dateSelect.setShowPopup(true);
 //                    } else {
 ////System.out.println("SIDR.decodeInputText()  setShowPopup( false )");
 //                        dateSelect.setShowPopup(false);
 //                    }
 //                }
 //            }
             Object inputTextDate = requestParameterMap.get(inputTextDateId);
 //System.out.println("SIDR.decodeInputText()  inputTextDate: " + inputTextDate);
             if (inputTextDate == null) {
                 dateSelect.setSubmittedValue(null);
             } else {
                 String inputTextDateStr = String.valueOf(inputTextDate);
                 if (inputTextDateStr.trim().length() == 0) {
                     dateSelect.setSubmittedValue("");
                 } else {
                     String inputTextDateAndTime = mergeTimeIntoDateString(
                             facesContext, dateSelect, clientId, inputTextDateStr);
                     dateSelect.setSubmittedValue(inputTextDateAndTime);
                 }
             }
         }
     }
 
     private String mergeTimeIntoDateString(
             FacesContext facesContext, SelectInputDate dateSelect, String clientId, String submittedDate) {
 
 //System.out.println("mergeTimeIntoDateString()  clientId: " + clientId);
 //System.out.println("mergeTimeIntoDateString()    submittedDate: " + submittedDate);
         DateTimeConverter converter = dateSelect.resolveDateTimeConverter(facesContext);
         if (SelectInputDate.isTime(converter)) {
 //System.out.println("mergeTimeIntoDateString()    TIME");
             Map requestParameterMap =
                     facesContext.getExternalContext().getRequestParameterMap();
 
             String hoursClientId = clientId + SELECT_HOUR;
             String minutesClientId = clientId + SELECT_MIN;
             String secondsClientId = clientId + SELECT_SEC;
             String amPmClientId = clientId + SELECT_AM_PM;
 
             int setMilitaryHour = -1;
             int setMinute = -1;
             int setSecond = -1;
             long setMillis = -1L;
 
             try {
                 if (requestParameterMap.containsKey(hoursClientId)) {
                     int hour = Integer.parseInt(
                             requestParameterMap.get(hoursClientId).toString());
 //System.out.println("mergeTimeIntoDateString()    hour: " + hour);
 
                     // Make hour 24 hour clock normalised
                     if (requestParameterMap.containsKey(amPmClientId)) {
                         String amPm = requestParameterMap.get(
                                 amPmClientId).toString();
 //System.out.println("mergeTimeIntoDateString()    am/pm: " + amPm);
                         if (hour >= 1 && hour <= 11 && amPm.equals("PM")) {
                             hour += 12;
                         } else if (hour == 12 && amPm.equals("AM")) {
                             hour = 0;
                         }
 //System.out.println("mergeTimeIntoDateString()    hour (24 hour): " + hour);
                     }
                     if (hour == 24) {
                         hour = 0;
                     }
 //System.out.println("mergeTimeIntoDateString()    hour (0-23 hour): " + hour);
 
                     setMilitaryHour = hour;
                 }
                 if (requestParameterMap.containsKey(minutesClientId)) {
                     setMinute = Integer.parseInt(
                             requestParameterMap.get(minutesClientId).toString());
 //System.out.println("mergeTimeIntoDateString()    setMinute: " + setMinute);
                 }
                 if (requestParameterMap.containsKey(secondsClientId)) {
                     setSecond = Integer.parseInt(
                         requestParameterMap.get(secondsClientId).toString());
 //System.out.println("mergeTimeIntoDateString()    setSeconds: " + setSeconds);
                 }
             }
             catch (NumberFormatException e) {
                 // We use drop down menus for the hours and minutes, so this shouldn't be possible
                 if (log.isDebugEnabled()) {
                     log.debug("Invalid hour (" +
                             requestParameterMap.get(hoursClientId) + ") or minute (" +
                             requestParameterMap.get(minutesClientId)+") or second ("+
                             requestParameterMap.get(secondsClientId)+")");
                 }
                 setMilitaryHour = -1;
                 setMinute = -1;
                 setSecond = -1;
             }
 
             if (setMilitaryHour != -1 || setMinute != -1 || setSecond != -1) {
                 Date date;
                 try {
                     date = (Date) converter.getAsObject(facesContext, dateSelect, submittedDate);
                 } catch (Exception e) {
                     date = null;
                 }
 //System.out.println("mergeTimeIntoDateString()    before calendar date: " + date);
                 if (date != null) {
                     TimeZone tz = dateSelect.resolveTimeZone(facesContext);
                     Locale currentLocale = dateSelect.resolveLocale(facesContext);
                     Calendar timeKeeper = Calendar.getInstance(tz, currentLocale);
                     timeKeeper.setTime(date);
 
                     if (setMilitaryHour != -1) {
                         timeKeeper.set(Calendar.HOUR_OF_DAY, setMilitaryHour);
                     }
                     if (setMinute != -1) {
                         timeKeeper.set(Calendar.MINUTE, setMinute);
                     }
                     if (setSecond != -1) {
                         timeKeeper.set(Calendar.SECOND, setSecond);
                     }
                     date = timeKeeper.getTime();
 //System.out.println("mergeTimeIntoDateString()    after calendar date: " + date);
                     submittedDate = converter.getAsString(facesContext, dateSelect, date);
 //System.out.println("mergeTimeIntoDateString()    merged date: " + submittedDate);
                 }
             }
         }
 
         return submittedDate;
     }
 
     private void decodeTime(FacesContext facesContext, UIComponent component) {
         decodeInputText(facesContext, component);
     }
 
     /* (non-Javadoc)
     * @see com.icesoft.faces.renderkit.dom_html_basic.DomBasicInputRenderer#getConvertedValue(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
     */
     public Object getConvertedValue(FacesContext facesContext,
                                     UIComponent uiComponent,
                                     Object submittedValue)
             throws ConverterException {
         validateParameters(facesContext, uiComponent, SelectInputDate.class);
 
         Converter converter = ((SelectInputDate) uiComponent).resolveDateTimeConverter(facesContext);
 
         if (!(submittedValue == null || submittedValue instanceof String)) {
             throw new IllegalArgumentException(
                     "Submitted value of type String expected");
         }
         Object o = converter.getAsObject(facesContext, uiComponent,
                 (String) submittedValue);
 
         return o;
     }
 
 }
