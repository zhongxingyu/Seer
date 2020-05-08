 /*******************************************************************************
  * Copyright (c) 2013 Rudy D'hauwe @ Whizu
  * Licensed under the EUPL V.1.1
  *   
  * This Software is provided to You under the terms of the European 
  * Union Public License (the "EUPL") version 1.1 as published by the 
  * European Union. Any use of this Software, other than as authorized 
  * under this License is strictly prohibited (to the extent such use 
  * is covered by a right of the copyright holder of this Software).
  *
  * This Software is provided under the License on an "AS IS" basis and 
  * without warranties of any kind concerning the Software, including 
  * without limitation merchantability, fitness for a particular purpose, 
  * absence of defects or errors, accuracy, and non-infringement of 
  * intellectual property rights other than copyright. This disclaimer 
  * of warranty is an essential part of the License and a condition for 
  * the grant of any rights to this Software.
  *   
  * For more  details, see <http://joinup.ec.europa.eu/software/page/eupl>.
  *
  * Contributors:
  *     2013 - Rudy D'hauwe @ Whizu - initial API and implementation
  *******************************************************************************/
 package org.whizu.tutorial.helloworld;
 
 import java.util.Date;
 
 import org.whizu.annotation.Page;
 import org.whizu.html.Title;
 import org.whizu.layout.Layout;
 import org.whizu.ui.Application;
 import org.whizu.ui.ClickListener;
 import org.whizu.ui.Document;
 import org.whizu.ui.Form;
 import org.whizu.ui.Label;
 import org.whizu.ui.UI;
 import org.whizu.value.IntegerValue;
 import org.whizu.value.StringValue;
 
 /**
  * @author Rudy D'hauwe
  */
 @Page("/whizu/tutorial/helloworld")
 public class HelloWorld implements Application {
 
 	@Title
 	public String getTitle() {
 		return "Hello World";
 	}
 
 	@Override
 	public void init(final UI ui) {
 		//model
 		final StringValue message = new StringValue("message");
 		final IntegerValue aantal = new IntegerValue("aantal");
 		aantal.value(0);
 
 		//user interface
 		Document document = ui.getDocument();
 		final Layout history = ui.createVerticalLayout();
 		Layout layout = ui.createHorizontalLayout();
 		
 		Layout left = ui.createVerticalLayout();
 		left.css("left-column");
 		left.add(ui.createLabel("Welcome to your very first meet and greet with Whizu."));
 		left.add(ui.createLabel("Share a thought and allow for Whizu to echo your words."));
 		Form form = ui.createForm();
 		form.add(ui.createTextField(message).css("message"));
 		form.css("form");
 		Label button = ui.createLabel("Share").css("submit-button");
		System.out.println("before add listener");
		ClickListener listener = new ClickListener() {
 
 			@Override
 			public void click() {
 				aantal.increment();
 				Layout detail = ui.createVerticalLayout();
 				detail.add(ui.createLabel(new Date().toString()).css("detail-date"));
 				detail.add(ui.createLabel(message.value()));
 				detail.css("detail");
 				history.prepend(detail);
 				message.clear();
 			}
		};
		button.addClickListener(listener);
 		form.add(button);
 		left.add(form);
 
 		Layout right = ui.createVerticalLayout();
 		right.css("right-column");
 		right.add(ui.createLabel("Thank you for sharing $1 comments.", aantal).css("tekst"));
 		right.add(history);
 		
 		layout.add(left);
 		layout.add(right);
 		
 		document.add(layout);
 	}
 }
