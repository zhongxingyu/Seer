 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package com.cs1530.group4.addendum.client;
 
 import com.cs1530.group4.addendum.shared.User;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class MainView implements EntryPoint, ValueChangeHandler<String>
 {
 	private VerticalPanel contentPanel;
 	private MainView main = this;
 
 	public void onModuleLoad()
 	{
 		initialize();
 		History.fireCurrentHistoryState();
 	}
 
 	public void setContent(Widget content, String historyToken)
 	{
 		History.newItem(historyToken);
 		contentPanel.clear();
 		contentPanel.add(content);
 	}
 
 	private void initialize()
 	{
 		RootPanel rootPanel = RootPanel.get("container");
 
 		VerticalPanel topPanel = new VerticalPanel();
 		contentPanel = new VerticalPanel();
 
 		rootPanel.add(topPanel);
 		rootPanel.add(contentPanel);
 		
 		Storage localStorage = Storage.getLocalStorageIfSupported();
 
 		if(Cookies.getCookie("loggedIn") != null && localStorage.getItem("loggedIn") != null)
 		{
 			User user = User.deserialize(localStorage.getItem("loggedIn"));
 			contentPanel.add(new Stream(this, user));
 		}
 		else
 			contentPanel.add(new Login(this));
 
 		History.addValueChangeHandler(this);
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event)
 	{
 		if(contentPanel == null)
 			initialize();
 
 		Widget content = null;
 
 		String[] historyToken = event.getValue().split("-");
 
 		// Parse the history token
 		if(historyToken[0].equals("login"))
 			content = new Login(main);
 		else if(historyToken[0].equals("profile"))
 		{
 			String loggedUser = Cookies.getCookie("loggedIn");
 			if(loggedUser == null)
 				content = new Login(main);
 			else
 			{
 				Storage localStorage = Storage.getLocalStorageIfSupported();
 				User user = new User(Cookies.getCookie("loggedIn"));
 				if(localStorage.getItem("loggedIn") != null)
 					user = User.deserialize(localStorage.getItem("loggedIn"));
 				
 				content = new Stream(main, user);
 			}
 		}
 		else if(historyToken[0].equals("adminPanel"))
 			content = new AdminPanel(main);
 		else
 		{
 			String username = Cookies.getCookie("loggedIn");
 			if(username == null)
 				content = new Login(main);
 			else
 			{
 				Storage localStorage = Storage.getLocalStorageIfSupported();
 				User user = new User(Cookies.getCookie("loggedIn"));
 				if(localStorage.getItem("loggedIn") != null)
 					user = User.deserialize(localStorage.getItem("loggedIn"));
 				
 				content = new Stream(main, user);
 			}
 		}
 		
 		contentPanel.clear();
 		contentPanel.add(content);
 	}
 }
