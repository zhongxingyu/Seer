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
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.TextResource;
 
 /**
  * OlhieResources.java<br/>
  * Responsibilities:<br/>
  * 1. Defines all resources (css, js) used in application<br/>
  * 
  * @author John DeStefano
  * @version 1.0
  * @since May 26, 2013
  * 
  */
 public interface OlhieResources extends ClientBundle {
 	public static final OlhieResources INSTANCE = GWT
 			.create(OlhieResources.class);
 
 	@Source("com/pronoiahealth/olhie/resources/css/mainscreen.css")
 	@CssResource.NotStrict
 	CssResource mainscreencss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/sidebarmenu.css")
 	@CssResource.NotStrict
 	CssResource sidebarmenucss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/searchscreen.css")
 	@CssResource.NotStrict
 	CssResource searchscreencss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/starrating.css")
 	@CssResource.NotStrict
 	CssResource starratingcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/bulletinboard.css")
 	@CssResource.NotStrict
 	CssResource bulletinboardcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/cbg_button.css")
 	@CssResource.NotStrict
 	CssResource cbgbuttoncss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/ioscheckbox.css")
 	@CssResource.NotStrict
 	CssResource ioscheckboxcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/logindialog.css")
 	@CssResource.NotStrict
 	CssResource logindialogcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/errordialog.css")
 	@CssResource.NotStrict
 	CssResource errordialogcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/registerdialog.css")
 	@CssResource.NotStrict
 	CssResource registerdialogcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/commentdialog.css")
 	@CssResource.NotStrict
 	CssResource commentdialogcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/newsdisplay.css")
 	@CssResource.NotStrict
 	CssResource newsdisplaycss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/newbook.css")
 	@CssResource.NotStrict
 	CssResource newbookcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/bookcase.css")
 	@CssResource.NotStrict
 	CssResource bookcasecss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/booklist3d.css")
 	@CssResource.NotStrict
 	CssResource booklist3dcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/viewbookasset.css")
 	@CssResource.NotStrict
 	CssResource viewbookassetcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/lookupuserdialog.css")
 	@CssResource.NotStrict
 	CssResource lookupuserdialogcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/chatdialog.css")
 	@CssResource.NotStrict
 	CssResource chatdialogcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/bookcomment.css")
 	@CssResource.NotStrict
 	CssResource bookcommentcss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/bookcommentdisplay.css")
 	@CssResource.NotStrict
 	CssResource bookcommentdisplaycss();
 
 	@Source("com/pronoiahealth/olhie/resources/css/bookscroller.css")
 	@CssResource.NotStrict
 	CssResource bookscrollercss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/glasspanelspinner.css")
 	@CssResource.NotStrict
 	CssResource glasspanelspinnercss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/carouselsliderwidget.css")
 	@CssResource.NotStrict
 	CssResource carouselsliderwidgetcss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/events.css")
 	@CssResource.NotStrict
 	CssResource eventscss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/fullcalendar.css")
 	@CssResource.NotStrict
 	CssResource fullcalendarcss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/fullcalendar.print.css")
 	@CssResource.NotStrict
 	CssResource fullcalendarprintcss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/jquery-ui.min.css")
 	@CssResource.NotStrict
 	CssResource jqueryuimincss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/animate.min.css")
 	@CssResource.NotStrict
 	CssResource animatemincss();
 	
 	@Source("com/pronoiahealth/olhie/resources/css/jquery-ui-1.10.3.custom.min.css")
 	@CssResource.NotStrict
 	CssResource jqueryminuicss();
 	
	//@Source("com/pronoiahealth/olhie/resources/css/styles.css")
	//@CssResource.NotStrict
	//CssResource stylescss();
	
 	//@Source("com/pronoiahealth/olhie/resources/css/custom_icons.css")
 	//@CssResource.NotStrict
 	//CssResource customiconscss();
 
 	// @Source("com/pronoiahealth/olhie/resources/js/jquery.bookshelfslider.js")
 	// TextResource jquerybookshelfsliderjs();
 
 	@Source("com/pronoiahealth/olhie/resources/js/jquery.contentcarousel.js")
 	TextResource jquerycontentcarouseljs();
 
 	@Source("com/pronoiahealth/olhie/resources/js/jquery.easing.1.3.js")
 	TextResource jqueryeasingjs();
 
 	@Source("com/pronoiahealth/olhie/resources/js/jquery.mousewheel.js")
 	TextResource jquerymousewheeljs();
 	
 	@Source("com/pronoiahealth/olhie/resources/js/jquery-ui-1.10.3.custom.min.js")
 	TextResource jqueryuicustomminjs();
 	
 	@Source("com/pronoiahealth/olhie/resources/js/fullcalendar.min.js")
 	TextResource fullcalendarminjs();
 }
