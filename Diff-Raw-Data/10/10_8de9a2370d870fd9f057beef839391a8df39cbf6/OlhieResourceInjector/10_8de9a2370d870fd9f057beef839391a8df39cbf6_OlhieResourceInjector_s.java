 /*******************************************************************************
  * Copyright (c) 2013 Pronoia Health LLC.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Pronoia Health LLC - initial API and implementation
  *******************************************************************************/
 package com.pronoiahealth.olhie.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.HeadElement;
 import com.google.gwt.dom.client.LinkElement;
 import com.google.gwt.dom.client.ScriptElement;
 import com.google.gwt.resources.client.TextResource;
 
 /**
  * OlhieResourceInjector.java<br/>
  * Responsibilities:<br/>
  * 1. Inject css and js into application<br/>
  *
  * @author John DeStefano
  * @version 1.0
  * @since May 26, 2013
  *
  */
 public class OlhieResourceInjector {
 
 	private static HeadElement head;
 
 	/**
 	 * Called to configure resources
 	 */
 	public static void configure() {
 		configureWithCssFile();
 		configureJs();
 	}
 
 	/**
 	 * Injects the required CSS styles and JavaScript files into the document
 	 * header.
 	 * 
 	 * <pre>
 	 * It's for NoStyle Module.
 	 * </pre>
 	 */
 	public static void configureWithCssFile() {
 		OlhieResources.INSTANCE.bookcasecss().ensureInjected();
 		OlhieResources.INSTANCE.cbgbuttoncss().ensureInjected();
 		OlhieResources.INSTANCE.mainscreencss().ensureInjected();
 		OlhieResources.INSTANCE.searchscreencss().ensureInjected();
 		OlhieResources.INSTANCE.sidebarmenucss().ensureInjected();
 		OlhieResources.INSTANCE.starratingcss().ensureInjected();
 		OlhieResources.INSTANCE.bulletinboardcss().ensureInjected();
 		OlhieResources.INSTANCE.ioscheckboxcss().ensureInjected();
 		OlhieResources.INSTANCE.logindialogcss().ensureInjected();
 		OlhieResources.INSTANCE.errordialogcss().ensureInjected();
 		OlhieResources.INSTANCE.registerdialogcss().ensureInjected();
 		OlhieResources.INSTANCE.commentdialogcss().ensureInjected();
 		OlhieResources.INSTANCE.newsdisplaycss().ensureInjected();
 		OlhieResources.INSTANCE.newbookcss().ensureInjected();
 		OlhieResources.INSTANCE.booklist3dcss().ensureInjected();
 		OlhieResources.INSTANCE.viewbookassetcss().ensureInjected();
 		OlhieResources.INSTANCE.lookupuserdialogcss().ensureInjected();
 		OlhieResources.INSTANCE.chatdialogcss().ensureInjected();
 		OlhieResources.INSTANCE.bookcommentcss().ensureInjected();
 		OlhieResources.INSTANCE.bookcommentdisplaycss().ensureInjected();
 		OlhieResources.INSTANCE.bookscrollercss().ensureInjected();
 		OlhieResources.INSTANCE.glasspanelspinnercss().ensureInjected();
 		OlhieResources.INSTANCE.carouselsliderwidgetcss().ensureInjected();
 		OlhieResources.INSTANCE.eventscss().ensureInjected();
 		OlhieResources.INSTANCE.jqueryuimincss();
 		OlhieResources.INSTANCE.fullcalendarcss().ensureInjected();
 		OlhieResources.INSTANCE.fullcalendarprintcss().ensureInjected();
 		OlhieResources.INSTANCE.animatemincss().ensureInjected();
 		OlhieResources.INSTANCE.jqueryminuicss().ensureInjected();
 		/*injectResourceCssAsFile("gwt_overrides.css");*/
 	}
 
 	/**
 	 * Injects the required third party javascript header.
 	 */
 	public static void configureJs() {
 
 		// JQuery might be there already from GWTBootstrap
 		//if (isNotLoadedJquery()) {
 		//	injectJs(OlhieResources.INSTANCE.jquery172minjs());
 		//}
 		injectJs(OlhieResources.INSTANCE.jquerycontentcarouseljs());
 		injectJs(OlhieResources.INSTANCE.jqueryeasingjs());
 		injectJs(OlhieResources.INSTANCE.jquerymousewheeljs());
 		injectJs(OlhieResources.INSTANCE.jqueryuicustomminjs());
 		injectJs(OlhieResources.INSTANCE.fullcalendarminjs());
 	}
 
 	/**
 	 * Inject public resource css file as a file.
 	 * 
 	 * @param filename
 	 *            inject file name
 	 */
 	public static void injectResourceCssAsFile(String filename) {
 		LinkElement link = Document.get().createLinkElement();
 		link.setType("text/css");
 		link.setRel("stylesheet");
 		link.setHref(GWT.getModuleName() + "/css/" + filename);
 		getHead().appendChild(link);
 	}
 
 	/**
 	 * Inject javascript in head element
 	 * 
 	 * @param r
 	 */
 	private static void injectJs(TextResource r) {
 		String jsStr = r.getText();
 		HeadElement head = getHead();
 		ScriptElement element = createScriptElement();
 		element.setText(jsStr);
 		head.appendChild(element);
 	}
 
 	/**
 	 * Get page head element
 	 * 
 	 * @return
 	 */
 	private static HeadElement getHead() {
 		if (head == null) {
 			Element elt = Document.get().getElementsByTagName("head")
 					.getItem(0);
 			assert elt != null : "The host HTML page does not have a <head> element"
 					+ " which is required by StyleInjector";
 			head = HeadElement.as(elt);
 		}
 		return head;
 	}
 
 	/**
 	 * Creates a script element
 	 * 
 	 * @return
 	 */
 	private static ScriptElement createScriptElement() {
 		ScriptElement script = Document.get().createScriptElement();
 		script.setAttribute("type", "text/javascript");
 		script.setAttribute("charset", "UTF-8");
 		return script;
 	}
 
 	/**
 	 * Check to see if JQuery is already present
 	 * 
 	 * @return
 	 */
 	private native static boolean isNotLoadedJquery() /*-{
 		return !$wnd['jQuery'] || (typeof $wnd['jQuery'] !== 'function');
 	}-*/;
 
 }
