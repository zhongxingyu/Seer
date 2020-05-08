 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package jp.dip.komusubi.lunch.wicket.panel;
 
 import java.io.Serializable;
 
 import jp.dip.komusubi.lunch.wicket.WicketApplication;
 import jp.dip.komusubi.lunch.wicket.WicketSession;
 import jp.dip.komusubi.lunch.wicket.page.Home;
 
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Header extends Panel {
 
 	private static final long serialVersionUID = -467641882462547658L;
 	private static final Logger logger = LoggerFactory.getLogger(Header.class);
 	
 	public static class HeaderBean implements Serializable {
 		private static final long serialVersionUID = -1010180169521998908L;
 		
 		public Class<? extends WebPage> config;
 		public Class<? extends WebPage> history;
 		public Class<? extends WebPage> order;
 		public String pageTitle;
 	}
 	
 	public Header(String id, Model<HeaderBean> model, boolean authVisible) {
 		super(id, model);
 		add(new Label("pageTitle", model.getObject().pageTitle));
 		add(getPageLink("link.home", WicketApplication.get().getHomePage()));
 		add(getAuthLink("auth", true));
 		add(getWebMarkupContainer("nav", model.getObject()));
 	}
 	
 	public Header(String id, Model<HeaderBean> model) {
 		this(id, model, true);
 	}
 	
 //	public Header(String id, Model<String> model, boolean authVisible) {
 //		super(id, model);
 //		Label label;
 //		if (model != null) {
 //			label = new Label("pageTitle", model);
 //		} else {
 //			throw new IllegalArgumentException("model is null");
 //		}
 //		add(label);
 //		// FIXME bookmarkable page link not nice url
 //		add(new BookmarkablePageLink<WebPage>("link.home", WicketApplication.get().getHomePage(), null));
 //		
 //		add(getAuthLink("auth", authVisible));
 //		add(getWebMarkupContainer("nav", null));
 //	}
 	
 	private Link<Void> getAuthLink(String id, final boolean visible) {
 		Link<Void> link; 
 		String label;
 		
 		if (WicketSession.get().isSignedIn()) {
 			link = new Link<Void>(id) {
 
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void onClick() {
 					logger.info("{} is logout", WicketSession.get().getSignedInUser().getId());
 					WicketSession.get().invalidate();
 				}
 				@Override
 				public boolean isVisible() {
 					return visible;
 				}
 			};
 			label = getLocalizer().getString("logout.label", Header.this);
 		} else {
 			link = new Link<Void>(id) {
 
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void onClick() {
 					setResponsePage(WicketApplication.get().getSignInPageClass());
 				}
 				@Override
 				public boolean isVisible() {
 					return visible;
 				}
 			};
 			label = getLocalizer().getString("login.label", Header.this);
 		}
 				
 		link.add(new Label("auth.label", label));
 		return link;
 	}
 	
 	private WebMarkupContainer getWebMarkupContainer(String id, HeaderBean bean) {
 		WebMarkupContainer container = new WebMarkupContainer(id) {
 			
 			private static final long serialVersionUID = 9112302809195248590L;
 
 			@Override
 			public boolean isVisible() {
 //				return WicketSession.get().isSignedIn();
 			    return false;
 			}
 			
 		};
 		// FIXME reference from panel to page package. should change how to access. 
 		if (bean == null) {
 			bean = new HeaderBean();
 			bean.order = Home.class;
 			bean.history = Home.class;
 			bean.config = Home.class;
 		} 
 		container.add(getPageLink("nav.new", bean.order == null ? Home.class : bean.order));
 		container.add(getPageLink("nav.history", bean.history == null ? Home.class : bean.history));
 		container.add(getPageLink("nav.config", bean.config == null ? Home.class : bean.config));
 
 		
 		return container;
 	}
 	
 	protected Link<String> getPageLink(String id, final Class<? extends WebPage> pageClass) {
 		return new Link<String>(id) {
 
 			private static final long serialVersionUID = 7343393596673757124L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(pageClass);
 			}
 		};
 	}
 }
