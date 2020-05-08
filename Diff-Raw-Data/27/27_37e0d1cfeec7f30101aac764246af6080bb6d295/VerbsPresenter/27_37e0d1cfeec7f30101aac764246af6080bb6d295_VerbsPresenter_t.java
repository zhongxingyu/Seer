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
 
 
 import com.google.gwt.event.shared.EventBus;
 import com.github.a2g.core.interfaces.HostingPanelAPI;
 import com.github.a2g.core.interfaces.MouseToVerbsPresenterAPI;
 import com.github.a2g.core.interfaces.VerbsPanelAPI;
 import com.github.a2g.core.interfaces.VerbsPresenterCallbackAPI;
 import com.github.a2g.core.primitive.ColorEnum;
 
 
 public class VerbsPresenter
 implements MouseToVerbsPresenterAPI
 {
 	private Verbs theVerbs;
 	private VerbsPanelAPI view;
 	private int widthOfScene;
 	private int widthOfInventory;
 	VerbsPresenterCallbackAPI callback;
 	public VerbsPresenter(final HostingPanelAPI panel, EventBus bus, VerbsPresenterCallbackAPI callback)
 	{
 		this.callback = callback;
 		this.theVerbs = new Verbs();
 		this.view = callback.getFactory().createVerbsPanel(this, ColorEnum.Purple, ColorEnum.Black);
 		panel.setThing(view);
 		this.view.setVerbs(theVerbs);
 		this.widthOfScene=0;
 		this.widthOfInventory=0;
 	}
 
 	public void clear()
 	{
 		// doesn't change.
 
 	}
 
 	public Verbs getVerbsModel()
 	{
 		return theVerbs;
 	}
 
 	public VerbsPanelAPI getView() {
 		return view;
 	}
 
	
	
 	public void update()
 	{
		view.setVerbs(theVerbs);
 		view.update();
 	}
	 
 
 	public void setWidthOfScene(int width) {
 		this.widthOfScene = width;
 		view.setWidth(widthOfScene-widthOfInventory);
 	}
 
 	public void setWidthOfInventory(int width) {
 		this.widthOfInventory = width;
 		view.setWidth(widthOfScene-widthOfInventory);
 	}
 
 	@Override
 	public void setMouseOver(String displayName, String textualId, int code)
 	{
 		callback.onMouseOverVerbsOrInventory(displayName,
 				textualId,
 				code
 				);
 
 	}
 
 	@Override
 	public void doClick() {
 		callback.onClickVerbsOrInventory();
 	}
 	/*
 	public int getWidth() {
 		return width;
 	}
 	 */
 	@Override
 	public void doClick(String displayName, String textualId, int code)
 	{
 		this.setMouseOver(displayName, textualId, code);
 		this.doClick();
 	}
 
 }
