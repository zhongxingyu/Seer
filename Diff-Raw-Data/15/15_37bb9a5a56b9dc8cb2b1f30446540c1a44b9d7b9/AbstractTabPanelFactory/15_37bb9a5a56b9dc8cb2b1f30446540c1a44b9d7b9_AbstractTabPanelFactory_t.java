 /*
  * Copyright 2009 Sysmap Solutions Software e Consultoria Ltda.
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
 package br.com.sysmap.crux.gwt.client;
 
 import br.com.sysmap.crux.core.client.declarative.TagAttribute;
 import br.com.sysmap.crux.core.client.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagChildAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagEventDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagEventsDeclaration;
 import br.com.sysmap.crux.core.client.event.bind.ClickEvtBind;
 import br.com.sysmap.crux.core.client.event.bind.KeyDownEvtBind;
 import br.com.sysmap.crux.core.client.event.bind.KeyPressEvtBind;
 import br.com.sysmap.crux.core.client.event.bind.KeyUpEvtBind;
 import br.com.sysmap.crux.core.client.screen.AttributeParser;
 import br.com.sysmap.crux.core.client.screen.InterfaceConfigException;
 import br.com.sysmap.crux.core.client.screen.ScreenFactory;
 import br.com.sysmap.crux.core.client.screen.ScreenLoadEvent;
 import br.com.sysmap.crux.core.client.screen.ScreenLoadHandler;
 import br.com.sysmap.crux.core.client.screen.WidgetFactoryContext;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor.AnyWidget;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor.HTMLTag;
 import br.com.sysmap.crux.core.client.screen.factory.HasAnimationFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasBeforeSelectionHandlersFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasSelectionHandlersFactory;
 import br.com.sysmap.crux.core.client.screen.parser.CruxMetaDataElement;
 
 import com.google.gwt.user.client.ui.TabBar.Tab;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 @SuppressWarnings("deprecation")
 class TabPanelContext extends WidgetFactoryContext
 {
 
 	public CruxMetaDataElement tabElement;
 	public String title;
 	public boolean isHTMLTitle;
 	public Widget titleWidget;
 	
 	public void clearAttributes()
     {
 	    isHTMLTitle = false;
 	    title = null;
 	    titleWidget = null;
 	    tabElement = null;
     }
 	
 }
 
 /**
  * Factory for TabPanel widgets
  * @author Thiago da Rosa de Bustamante
  */
 @SuppressWarnings("deprecation")
 public abstract class AbstractTabPanelFactory<T extends TabPanel> extends CompositeFactory<T, TabPanelContext> 
        implements HasAnimationFactory<T, TabPanelContext>, 
                   HasBeforeSelectionHandlersFactory<T, TabPanelContext>, HasSelectionHandlersFactory<T, TabPanelContext>
 {
 	@Override
 	@TagAttributes({
 		@TagAttribute(value="visibleTab", type=Integer.class, parser=VisibleTabAttributeParser.class)
 	})
 	public void processAttributes(TabPanelContext context) throws InterfaceConfigException
 	{
 		super.processAttributes(context);
 	}
 	
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
	public static class VisibleTabAttributeParser implements AttributeParser<TabPanelContext>
 	{
		public void processAttribute(TabPanelContext context, final String propertyValue)
         {
 			final TabPanel widget = context.getWidget();
 			ScreenFactory.getInstance().addLoadHandler(new ScreenLoadHandler()
 			{
 				public void onLoad(ScreenLoadEvent event)
 				{
 					widget.selectTab(Integer.parseInt(propertyValue));
 				}
 			});
         }
 	}	
 
 	@TagChildAttributes(minOccurs="0", maxOccurs="unbounded", tagName="tab" )
 	public static abstract class AbstractTabProcessor<T extends TabPanel> extends WidgetChildProcessor<T, TabPanelContext> 
 	{
 		@Override
 		@TagAttributesDeclaration({
 			@TagAttributeDeclaration(value="tabEnabled", type=Boolean.class, defaultValue="true"),
 			@TagAttributeDeclaration(value="tabWordWrap", type=Boolean.class, defaultValue="true")
 		})
 		@TagEventsDeclaration({
 			@TagEventDeclaration("onClick"),
 			@TagEventDeclaration("onKeyUp"),
 			@TagEventDeclaration("onKeyDown"),
 			@TagEventDeclaration("onKeyPress")
 		})
 		public void processChildren(TabPanelContext context) throws InterfaceConfigException
 		{
 			context.tabElement = context.getChildElement();
 		}
 	}
 
 	@TagChildAttributes(tagName="tabText", type=String.class)
 	public static abstract class AbstractTextTabProcessor<T extends TabPanel> extends WidgetChildProcessor<T, TabPanelContext>
 	{
 		@Override
 		public void processChildren(TabPanelContext context) throws InterfaceConfigException 
 		{
 			context.title = ScreenFactory.getInstance().getDeclaredMessage(ensureTextChild(context.getChildElement(), true));
 			context.isHTMLTitle = false;
 		}
 	}
 	
 	@TagChildAttributes(tagName="tabHtml", type=HTMLTag.class)
 	public static abstract class AbstractHTMLTabProcessor<T extends TabPanel> extends WidgetChildProcessor<T, TabPanelContext>
 	{
 		@Override
 		public void processChildren(TabPanelContext context) throws InterfaceConfigException 
 		{
 			context.title = ensureHtmlChild(context.getChildElement(), true);
 			context.isHTMLTitle = true;
 		}
 	}
 	
 	@TagChildAttributes(type=AnyWidget.class)
 	public static abstract class AbstractWidgetTitleProcessor<T extends TabPanel> extends WidgetChildProcessor<T, TabPanelContext> 
 	{
 		@Override
 		public void processChildren(TabPanelContext context) throws InterfaceConfigException
 		{
 			context.titleWidget = createChildWidget(context.getChildElement());
 		}
 	}
 	
 	@TagChildAttributes(type=AnyWidget.class)
 	public static abstract class AbstractWidgetContentProcessor<T extends TabPanel> extends WidgetChildProcessor<T, TabPanelContext> 
 	{
 		@SuppressWarnings("unchecked")
 		@Override
 		public void processChildren(TabPanelContext context) throws InterfaceConfigException
 		{
 			Widget widget = createChildWidget(context.getChildElement());
 			T tabWidget = (T)context.getWidget();
 			
 			if (context.titleWidget != null)
 			{
 				tabWidget.add(widget, context.titleWidget);
 			}
 			else
 			{
 				tabWidget.add(widget, context.title, context.isHTMLTitle);
 			}
 			updateTabState(context);
 		}
 		
 		@SuppressWarnings("unchecked")
 		private void updateTabState(TabPanelContext context)
 		{
 			String enabled = context.tabElement.getProperty("enabled");
 			T widget = (T)context.getWidget();
 
 			int tabCount = widget.getTabBar().getTabCount();
 			if (enabled != null && enabled.length() >0)
 			{
 				widget.getTabBar().setTabEnabled(tabCount-1, Boolean.parseBoolean(enabled));
 			}
 
 			Tab currentTab = widget.getTabBar().getTab(tabCount-1);
 			
 			String wordWrap = context.tabElement.getProperty("wordWrap");
 			if (wordWrap != null && wordWrap.trim().length() > 0)
 			{
 				currentTab.setWordWrap(Boolean.parseBoolean(wordWrap));
 			}
 
 			clickEvtBind.bindEvent(context.tabElement, currentTab);
 			keyUpEvtBind.bindEvent(context.tabElement, currentTab);
 			keyPressEvtBind.bindEvent(context.tabElement, currentTab);
 			keyDownEvtBind.bindEvent(context.tabElement, currentTab);
 
 			context.clearAttributes();
 		}	
 		private static ClickEvtBind clickEvtBind = new ClickEvtBind();
 		private static KeyUpEvtBind keyUpEvtBind = new KeyUpEvtBind();
 		private static KeyPressEvtBind keyPressEvtBind = new KeyPressEvtBind();
 		private static KeyDownEvtBind keyDownEvtBind = new KeyDownEvtBind();
 	}
 }
