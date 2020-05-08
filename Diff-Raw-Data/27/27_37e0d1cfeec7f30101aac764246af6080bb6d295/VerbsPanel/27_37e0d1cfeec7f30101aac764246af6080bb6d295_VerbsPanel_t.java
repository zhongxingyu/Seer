 /*
  * Copyright 2012 Anthony Cassidy
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.github.a2g.core.objectmodel;
 
 
 import com.github.a2g.core.interfaces.InternalAPI;
 import com.github.a2g.core.interfaces.MouseToVerbsPresenterAPI;
 import com.github.a2g.core.interfaces.VerbsPanelAPI;
 import com.github.a2g.core.platforms.html4.mouse.VerbMouseClickHandler;
 import com.github.a2g.core.platforms.html4.mouse.VerbMouseOverHandler;
 import com.github.a2g.core.primitive.ColorEnum;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Label;
 
 
 public class VerbsPanel
 extends Grid
 implements
 VerbsPanelAPI
 {
 	ColorEnum rolloverColor;
 	final MouseToVerbsPresenterAPI mouseToPresenter;
 
 	public VerbsPanel(InternalAPI api, MouseToVerbsPresenterAPI mouseToPresenter, ColorEnum fore, ColorEnum back)
 	{
 		this.mouseToPresenter = mouseToPresenter;
 		DOM.setStyleAttribute(getElement(), "color",fore.toString());
 		DOM.setStyleAttribute(getElement(), "backgroundColor", back.toString());
 
 	}
 
 	@Override
 	public void setVerbs(Verbs verbs) {
 		int rows = verbs.getNumberOfRows();
 		int columns = verbs.getNumberOfColumns();
 		this.resize(rows, columns);
 		for (int i = 0; i < (rows * columns); i++)
 		{
 			int row = i / columns;
			if(i>=verbs.items().size())
				continue;
 			int column = i % columns;
 
 			Verb verb = verbs.items().get(i);
 			String textualId = verb.gettextualId();
 			String displayText = verb.getdisplayText();
 			int code = verb.getCode();
 			Label widget = new Label(
 					textualId);
 
 			this.setWidget(row, column, widget);
 			widget.addMouseMoveHandler(
 					new VerbMouseOverHandler(mouseToPresenter,displayText,textualId, code)
 					);
 			widget.addClickHandler(
 					new VerbMouseClickHandler(mouseToPresenter,displayText,textualId, code));
 		}
 	}
 
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setWidth(int i) {
 		// do nothing
 		// since the panel hierarchy
 		// in html evaluates in such a way
 		// as to keep the width of the verbs
 		// panel reasonable.
 
 	}
 }
