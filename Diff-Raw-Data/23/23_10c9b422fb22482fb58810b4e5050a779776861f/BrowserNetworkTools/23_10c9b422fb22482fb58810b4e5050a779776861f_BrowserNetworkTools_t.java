 /*
  * Copyright 2013 cruxframework.org.
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
 package org.cruxframework.crux.core.client.screen;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ErrorEvent;
 import com.google.gwt.event.dom.client.ErrorHandler;
 import com.google.gwt.event.dom.client.LoadEvent;
 import com.google.gwt.event.dom.client.LoadHandler;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Inspect network conditions and give requested feedback to user.
  * 
  * @author samuel.cardoso
  */
 public class BrowserNetworkTools
 {
 	private static NetworkChecker networkChecker = null;
 
 	public interface NetworkChecker
 	{
 		public boolean isOnline();
 	}
 
 	/**
 	 * @return true if the client is online and false otherwise.
 	 */
 	public static boolean isOnline()
 	{
 		return getStaticInstance().isOnline();
 	}
 
 	private static NetworkChecker getStaticInstance()
 	{
 		if (networkChecker == null)
 		{
 			networkChecker = GWT.create(NetworkChecker.class);
 		}
 		return networkChecker;
 	}
 
 	/**
 	 * NetworkChecker implementation for Default browsers
 	 * @author samuel.cardoso
 	 */
 	public static class NetworkCheckerDefault implements NetworkChecker
 	{
		@SuppressWarnings("unused")
 		private static final int REPEATING_INTERVAL = 5000;
 		private static final String TESTER_ID = "_network_tester_";
 		private final String URL_IMG = "clear.cache.gif";
		private boolean isOnline = true;
 		private Panel testerPanel;
 
 		public NetworkCheckerDefault()
 		{
			/*
 			testerPanel = createTesterPanel();
 			Scheduler.get().scheduleFixedDelay(new RepeatingCommand()
 			{
 				public boolean execute()
 				{
 					updateNetworkStatus();
 					return true;
 				}
 			}, REPEATING_INTERVAL);
			*/
 		}
 
 		@Override
 		/*As this is set by a timer task, it should be initialized
 		 * at the application bootstrap otherwise it may indicate
 		 * a false answer. */ 
 		public boolean isOnline()
 		{
 			//this will be replaced by another implementation that uses the bootstrap
			return isOnline;
 		}
 
		@SuppressWarnings("unused")
 		private void updateNetworkStatus()
 		{
			Image img = new Image(com.google.gwt.core.client.GWT.getModuleName()+"/"+URL_IMG+"?"+System.currentTimeMillis());
 			img.getElement().getStyle().setPosition(Position.ABSOLUTE);
 			img.getElement().getStyle().setLeft(-1000, Unit.PX);
 			img.addLoadHandler(new LoadHandler()
 			{
 				@Override
 				public void onLoad(LoadEvent event)
 				{
 					isOnline = true;
 					((Image)event.getSource()).removeFromParent();
 				}
 			});
 
 			img.addErrorHandler(new ErrorHandler()
 			{
 				@Override
 				public void onError(ErrorEvent event)
 				{
 					isOnline = false;
 					((Image)event.getSource()).removeFromParent();
 				}
 			});
 			testerPanel.add(img);	
 		}
 
		@SuppressWarnings("unused")
 		private Panel createTesterPanel()
 		{
 			final Element div = Document.get().createDivElement();
 			div.setId(TESTER_ID);
 			Document.get().getBody().appendChild(div);
 			return RootPanel.get(TESTER_ID);
 		}
 	}
 
 	/**
 	 * NetworkChecker implementation for Safari browsers
 	 * @author samuel.cardoso
 	 */
 	public static class NetworkCheckerSafari implements NetworkChecker
 	{
 		@Override
 		public native boolean isOnline() /*-{
 			return window.navigator.onLine;
 		}-*/;
 	}
 	
 	/**
 	 * NetworkChecker implementation for IE8+ browsers
 	 * @author samuel.cardoso
 	 */
 	public static class NetworkCheckerIE8AndAbove implements NetworkChecker
 	{
 		@Override
 		public native boolean isOnline() /*-{
 			return window.navigator.onLine;
 		}-*/;
 	}
 }
