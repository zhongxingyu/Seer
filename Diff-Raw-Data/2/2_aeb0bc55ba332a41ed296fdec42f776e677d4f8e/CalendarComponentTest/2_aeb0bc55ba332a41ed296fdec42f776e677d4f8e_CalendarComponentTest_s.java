 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 package org.richfaces.component;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TimeZone;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.html.HtmlForm;
 
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.ajax4jsf.tests.AbstractAjax4JsfTestCase;
 import org.apache.commons.lang.StringUtils;
 import org.richfaces.renderkit.CalendarRendererBase;
 import org.richfaces.renderkit.html.CalendarRenderer;
 
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 import com.gargoylesoftware.htmlunit.html.HtmlButton;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlScript;
 import com.gargoylesoftware.htmlunit.html.HtmlSpan;
 
 /**
  * @author Nick Belaevski - mailto:nbelaevski@exadel.com created 08.06.2007
  * 
  */
 public class CalendarComponentTest extends AbstractAjax4JsfTestCase {
 
 	private UIComponent form;
 
 	private UICalendar calendar;
 
 	private UICalendar calendar1;
 
 	private UICalendar calendar2;
 	
 	private UICalendar calendar3;
 	
 	private static Set<String> javaScripts = new HashSet<String>();
 
 	static {
 		javaScripts.add("org.ajax4jsf.javascript.PrototypeScript");
 		javaScripts.add("org.ajax4jsf.javascript.AjaxScript");
 		javaScripts.add("org/richfaces/renderkit/html/scripts/events.js");
 		javaScripts.add("org/richfaces/renderkit/html/scripts/utils.js");
 		javaScripts
 				.add("org/richfaces/renderkit/html/scripts/json/json-dom.js");
 		javaScripts.add("org/richfaces/renderkit/html/scripts/calendar.js");
 		javaScripts.add("org/richfaces/renderkit/html/scripts/scriptaculous/effects.js");
 		javaScripts.add("org/richfaces/renderkit/html/scripts/JQuerySpinBtn.js");
 		javaScripts.add("org/richfaces/renderkit/html/scripts/jquery/jquery.js");
 	}
 
 	public CalendarComponentTest(String name) {
 		super(name);
 	}
 
 	public void setUp() throws Exception {
 
 		super.setUp();
 
 		form = new HtmlForm();
 		form.setId("form");
 		facesContext.getViewRoot().getChildren().add(form);
 		calendar = (UICalendar) application
 				.createComponent(UICalendar.COMPONENT_TYPE);
 		calendar1 = (UICalendar) application
 				.createComponent(UICalendar.COMPONENT_TYPE);
 		calendar.setLocale(Locale.UK);
 		calendar.setDatePattern("d/MM/yyyy");
		calendar.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));
 		
 		calendar.setButtonLabel("PopUp");
 		// XXX test popup false
 		// XXX test CurrentDate = null
 		calendar.setPopup(true);
 		Calendar calendarObject = Calendar.getInstance();
 		calendarObject.clear();
 		calendarObject.set(2001, Calendar.SEPTEMBER, 11, 10, 0, 0);
 		calendar.setId("calendar");
 		calendar.setValue(calendarObject.getTime());
 		form.getChildren().add(calendar);
 		calendar1.setId("_calendar");
 		calendar1.setPopup(false);
 		calendar1.setCurrentDate(null);
 		form.getChildren().add(calendar1);	
 		
 		calendar2 = (UICalendar) application
 		.createComponent(UICalendar.COMPONENT_TYPE);
 		calendar2.setDatePattern("dd/M/yy HH:mm");
 		calendarObject.set(2001, Calendar.SEPTEMBER, 11, 13, 36);
 		calendar2.setValue(calendarObject.getTime());
 		calendar2.setId("timecalendar");
 		form.getChildren().add(calendar2);
 		
 		calendar3 = (UICalendar) application
 		.createComponent(UICalendar.COMPONENT_TYPE);
 		calendar3.setDatePattern("dd/M/yy HH:mm");
 		calendarObject.set(2001, Calendar.SEPTEMBER, 11, 13, 36);
 		calendar3.setValue(calendarObject.getTime());
 		calendar3.setId("timecalendar");
 		calendar3.setMode(UICalendar.AJAX_MODE);
 		calendar3.setLocale(new Locale("ru", "RU", ""));
 		calendar3.setCellHeight("50");
 		calendar3.setCellWidth("50");
 		form.getChildren().add(calendar3);
 	}
 
 	/*
 	 * public void testTidy() throws Exception { UIViewRoot viewRoot =
 	 * facesContext.getViewRoot(); List children = viewRoot.getChildren();
 	 * UIComponent calendar =
 	 * application.createComponent(UICalendar.COMPONENT_TYPE);
 	 * children.add(calendar); HtmlOutputText output = (HtmlOutputText)
 	 * application.createComponent(HtmlOutputText.COMPONENT_TYPE);
 	 * output.setValue("<br>&nbsp;&amp;<a href='#'>");
 	 * output.setEscape(false); calendar.getChildren().add(output);
 	 * 
 	 * HtmlPage renderView = renderView();
 	 * System.out.println(renderView.asXml()); }
 	 */
 	public void testPreloadRanges() throws Exception {
 		UICalendar calendar = (UICalendar) application
 				.createComponent(UICalendar.COMPONENT_TYPE);
 		calendar.setLocale(Locale.FRENCH);
 		Calendar calendarObject = Calendar.getInstance();
 		calendarObject.clear();
 		calendarObject.set(2007, Calendar.JUNE, 10);
 
 		Calendar preloadRangeBegin = Calendar.getInstance();
 		preloadRangeBegin.clear();
 		preloadRangeBegin.setTime(calendar
 				.getDefaultPreloadBegin(calendarObject.getTime()));
 		assertEquals(2007, preloadRangeBegin.get(Calendar.YEAR));
 		assertEquals(Calendar.JUNE, preloadRangeBegin.get(Calendar.MONTH));
 		assertEquals(1, preloadRangeBegin.get(Calendar.DATE));
 
 		Calendar preloadRangeEnd = Calendar.getInstance();
 		preloadRangeEnd.clear();
 		preloadRangeEnd.setTime(calendar.getDefaultPreloadEnd(calendarObject
 				.getTime()));
 		assertEquals(2007, preloadRangeEnd.get(Calendar.YEAR));
 		assertEquals(Calendar.JUNE, preloadRangeEnd.get(Calendar.MONTH));
 		assertEquals(30, preloadRangeEnd.get(Calendar.DATE));
 
 		calendarObject.set(2007, Calendar.JANUARY, 1);
 		preloadRangeBegin.setTime(calendar
 				.getDefaultPreloadBegin(calendarObject.getTime()));
 		assertEquals(2007, preloadRangeBegin.get(Calendar.YEAR));
 		assertEquals(Calendar.JANUARY, preloadRangeBegin.get(Calendar.MONTH));
 		assertEquals(1, preloadRangeBegin.get(Calendar.DATE));
 
 		calendarObject.set(2007, Calendar.JUNE, 10);
 		calendar.setLocale(Locale.US);
 
 		preloadRangeBegin.setTime(calendar
 				.getDefaultPreloadBegin(calendarObject.getTime()));
 		assertEquals(2007, preloadRangeBegin.get(Calendar.YEAR));
 		assertEquals(Calendar.JUNE, preloadRangeBegin.get(Calendar.MONTH));
 		assertEquals(1, preloadRangeBegin.get(Calendar.DATE));
 
 		preloadRangeEnd.setTime(calendar.getDefaultPreloadEnd(calendarObject
 				.getTime()));
 		assertEquals(2007, preloadRangeEnd.get(Calendar.YEAR));
 		assertEquals(Calendar.JUNE, preloadRangeEnd.get(Calendar.MONTH));
 		assertEquals(30, preloadRangeEnd.get(Calendar.DATE));
 	}
 
 	public void testGetPreloadDateRange() throws Exception {
 		UICalendar calendar = (UICalendar) application
 				.createComponent(UICalendar.COMPONENT_TYPE);
 		calendar.setLocale(Locale.FRENCH);
 		Calendar calendarObject = Calendar.getInstance();
 		calendarObject.clear();
 		calendarObject.set(2007, Calendar.JUNE, 10);
 		calendar.setCurrentDate(calendarObject.getTime());
 
 		Date[] range = calendar.getPreloadDateRange();
 		assertEquals(calendar.getPreloadDateRangeBegin(), range[0]);
 		assertEquals(calendar.getPreloadDateRangeEnd(), range[range.length - 1]);
 		assertEquals(30, range.length);
 	}
 		
 	public void testCalendarRenderer() throws Exception {
 
 		HtmlPage page = renderView();
 		assertNotNull(page);
 
 		HtmlElement htmlSpan = page.getHtmlElementById(calendar.getClientId(facesContext)+"Popup");
 		assertNotNull(htmlSpan);
 
 		HtmlInput htmlCalendarInput = (HtmlInput) page
 				.getHtmlElementById(calendar.getClientId(facesContext)
 						+ "InputDate");
 		HtmlButton htmlCalendarButton = (HtmlButton) page
 				.getHtmlElementById(calendar.getClientId(facesContext)
 						+ "PopupButton");
 		HtmlSpan htmlCalendarSpan = (HtmlSpan) page.getHtmlElementById(calendar
 				.getClientId(facesContext)
 				+ "Popup");
 		assertNotNull(htmlCalendarInput);
 		assertNotNull(htmlCalendarButton);
 		assertNotNull(htmlCalendarSpan);
 
 		assertEquals("11/09/2001", htmlCalendarInput.getValueAttribute());
 
 		HtmlElement htmlCalendar1Span = page.getHtmlElementById(calendar1
 				.getClientId(facesContext)+"Popup");
 		assertNotNull(htmlCalendar1Span);
 		assertEquals("display: none", htmlCalendar1Span.getAttributeValue(HTML.style_ATTRIBUTE));
 
 		try {
 			page.getHtmlElementById(calendar1.getClientId(facesContext)+ "InputDate");
 		} catch (ElementNotFoundException e) {
 			assertNotNull(e);
 		}
 		
 		try {
 			page.getHtmlElementById(calendar1.getClientId(facesContext)+ "PopupButton");
 		} catch (ElementNotFoundException e) {
 			assertNotNull(e);
 		}
 		try {
 			page.getHtmlElementById(calendar1.getClientId(facesContext)+ "Popup");
 		} catch (ElementNotFoundException e) {
 			assertNotNull(e);
 		}
 	}
 
 	public void testCalendarDecode() throws Exception {
 		
 		Calendar calendarObject = Calendar.getInstance();
 		calendarObject.clear();
 		calendarObject.set(2001, Calendar.SEPTEMBER, 11);
 		calendar.decode(facesContext);
 		//assertEquals(calendarObject.getTime(), calendar.getSubmittedValue());
 		calendarObject.set(2002, Calendar.SEPTEMBER, 11, 1, 1, 1);
 		calendar.updateCurrentDate(facesContext, calendarObject.getTime());
 		assertEquals(calendarObject.getTime(), calendar.getCurrentDate());
 
 	}
 
 	public void testCalendarStyles() throws Exception {
 		HtmlPage page = renderView();
 		assertNotNull(page);
 
 		List<?> links = page.getDocumentHtmlElement().getHtmlElementsByTagName(HTML.LINK_ELEMENT);
 		assertEquals(1, links.size());
 		HtmlElement link = (HtmlElement) links.get(0);
 		assertTrue(link.getAttributeValue(HTML.HREF_ATTR).contains("css/calendar.xcss"));
 	}
 
 	public void testCalendarScrits() throws Exception {
 		HtmlPage page = renderView();
 		assertNotNull(page);
 
 		List<?> scripts = page.getDocumentHtmlElement().getHtmlElementsByTagName(HTML.SCRIPT_ELEM);
 		for (Iterator<?> it = scripts.iterator(); it.hasNext();) {
 			HtmlScript item = (HtmlScript) it.next();
 			String srcAttr = item.getSrcAttribute();
 			if (item.getFirstDomChild() != null) {
 				String scriptBodyString = item.getFirstDomChild().toString();
 				if (scriptBodyString.contains("new Calendar")&&scriptBodyString.contains("form:calendar")) {
 					
 					assertTrue(scriptBodyString.contains("datePattern"));
 					assertTrue(scriptBodyString.contains("d/MM/yyyy"));
 					assertTrue(scriptBodyString.contains("currentDate"));
 					assertTrue(scriptBodyString.contains("selectedDate"));
 				}else if(scriptBodyString.contains("form:timecalendar")){
 					
 					assertTrue(scriptBodyString.contains("&apos;selectedDate&apos;:new Date(2001,8,11,13,36,0)"));					
 				}
 			}
 			
 
 			if (StringUtils.isNotBlank(srcAttr)) {
 				boolean found = false;
 				for (Iterator<String> srcIt = javaScripts.iterator(); srcIt.hasNext();) {
 					String src = srcIt.next();
 					found = srcAttr.contains(src);
 					if (found) {
 						break;
 					}
 				}
 				assertTrue(found);
 			}
 		}
 	}
 
 	public void testCalendarTime() throws Exception{
 			Calendar calendarObject = Calendar.getInstance();
 			calendarObject.clear();
 			calendarObject.set(2001, Calendar.SEPTEMBER, 11, 13, 36);
 			assertEquals(calendarObject.getTime().toString(), calendar2.getValue().toString());
 			
 	}
 	
 	public void testCalendarGetConvertedValue() throws Exception{
 		UICalendar calendar = (UICalendar) application
 		.createComponent(UICalendar.COMPONENT_TYPE);
 		Calendar calendarObject = Calendar.getInstance();
 		calendarObject.clear();
 		calendarObject.set(2007, Calendar.JUNE, 10);
 		calendar.setCurrentDate(calendarObject.getTime());
 		calendar2.setDatePattern("dd/M/yy");	
 		calendar.getConvertedValue(facesContext, "01/01/01");
 		calendar.getAsDate(new Date());
 		calendar.getAsLocale("ru_RU");
 		CalendarRendererBase renderer = new CalendarRenderer();
         
 		renderer.getConvertedValue(facesContext, calendar, new Date());
 	}
 	
 	public void tearDown() throws Exception {
 		super.tearDown();
 		form = null;
 		calendar = null;
 	}
 }
