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
 
 import br.com.sysmap.crux.core.client.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.client.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagChild;
 import br.com.sysmap.crux.core.client.declarative.TagChildAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagChildren;
 import br.com.sysmap.crux.core.client.screen.InterfaceConfigException;
 import br.com.sysmap.crux.core.client.screen.WidgetFactory;
 import br.com.sysmap.crux.core.client.screen.WidgetFactoryContext;
 import br.com.sysmap.crux.core.client.screen.children.ChoiceChildProcessor;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor.AnyWidget;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor.HTMLTag;
 import br.com.sysmap.crux.core.client.screen.factory.HasBeforeSelectionHandlersFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasSelectionHandlersFactory;
 import br.com.sysmap.crux.core.client.screen.parser.CruxMetaDataElement;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.StackLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 class StackLayoutContext extends WidgetFactoryContext
 {
 	boolean selected = false;
 	double headerSize;
 	Widget headerWidget;
 	String title;
 	boolean isHtmlTitle;
 	
 	public void clearAttributes() 
 	{
 		title = null;
 		isHtmlTitle = false;
 		selected = false;
 		headerSize = 0;
 		headerWidget = null;
 	}
 }
 
 /**
  * @author Thiago da Rosa de Bustamante
  *
  */
 @DeclarativeFactory(id="stackLayoutPanel", library="gwt")
 public class StackLayoutPanelFactory extends WidgetFactory<StackLayoutPanel, StackLayoutContext> 
 	   implements HasBeforeSelectionHandlersFactory<StackLayoutPanel, StackLayoutContext>, 
 	   			  HasSelectionHandlersFactory<StackLayoutPanel, StackLayoutContext>
 {
 	@Override
 	public StackLayoutPanel instantiateWidget(CruxMetaDataElement element, String widgetId) 
 	{
 		return new StackLayoutPanel(AbstractLayoutPanelFactory.getUnit(element.getProperty("unit")));
 	}
 	
 	@Override
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration(value="unit", type=Unit.class, defaultValue="PX")
 	})
 	public void processAttributes(StackLayoutContext context) throws InterfaceConfigException
 	{
 		super.processAttributes(context);
 	}
 	
 	@Override
 	@TagChildren({
 		@TagChild(StackItemProcessor.class)
 	})
 	public void processChildren(StackLayoutContext context) throws InterfaceConfigException
 	{
 		super.processChildren(context);
 	}
 
 	@TagChildAttributes(tagName="item", maxOccurs="unbounded")
 	public static class StackItemProcessor extends WidgetChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		@TagChildren({
 			@TagChild(StackHeaderProcessor.class),
 			@TagChild(StackContentProcessor.class)
 		})
 		@TagAttributesDeclaration({
 			@TagAttributeDeclaration(value="selected", type=Boolean.class, defaultValue="false")
 		})
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException 
 		{
 			context.clearAttributes();
 			String selected = context.readChildProperty("selected");
 			if (!StringUtils.isEmpty(selected))
 			{
 				context.selected = Boolean.parseBoolean(selected);
 			}
 		}
 	}
 
 	@TagChildAttributes(tagName="header")
 	public static class StackHeaderProcessor extends WidgetChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		@TagChildren({
 			@TagChild(StackHeader.class)
 		})
 		@TagAttributesDeclaration({
 			@TagAttributeDeclaration(value="size", type=Double.class, required=true)
 		})
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException 
 		{
 			context.headerSize = Double.parseDouble(context.readChildProperty("size"));
 		}
 	}
 
 	public static class StackHeader extends ChoiceChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		@TagChildren({
 			@TagChild(StackHeaderWidgetProcessor.class),
 			@TagChild(StackHeaderTextProcessor.class),
 			@TagChild(StackHeaderHTMLProcessor.class)
 		})
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException 
 		{
 		}
 	}
 	
 	@TagChildAttributes(tagName="text", type=String.class)
	public abstract static class StackHeaderTextProcessor extends WidgetChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException 
 		{
 			context.title = ensureTextChild(context.getChildElement(), true);
 			context.isHtmlTitle = false;
 		}
 	}
 	
 	@TagChildAttributes(tagName="html", type=HTMLTag.class)
	public abstract static class StackHeaderHTMLProcessor extends WidgetChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException 
 		{
 			context.title = ensureHtmlChild(context.getChildElement(), true);
 			context.isHtmlTitle = true;
 		}
 	}
 
 	@TagChildAttributes(tagName="widget", type=AnyWidget.class)
 	public static class StackHeaderWidgetProcessor extends WidgetChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException
 		{
 			Widget childWidget = createChildWidget(context.getChildElement());
 			context.headerWidget = childWidget;
 		}
 	}
 
 	@TagChildAttributes(tagName="content")
 	public static class StackContentProcessor extends WidgetChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		@TagChildren({
 			@TagChild(StackContentWidgetProcessor.class)
 		})
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException {}
 	}
 
 	@TagChildAttributes(type=AnyWidget.class)
 	public static class StackContentWidgetProcessor extends WidgetChildProcessor<StackLayoutPanel, StackLayoutContext>
 	{
 		@Override
 		public void processChildren(StackLayoutContext context) throws InterfaceConfigException
 		{
 			Widget contentWidget = createChildWidget(context.getChildElement());
 			StackLayoutPanel rootWidget = context.getWidget();
 			
 			if (context.headerWidget != null)
 			{
 				rootWidget.add(contentWidget, context.headerWidget, context.headerSize);
 			}
 			else
 			{
 				rootWidget.add(contentWidget, context.title, context.isHtmlTitle, context.headerSize);
 			}
 
 			if (context.selected)
 			{
 				rootWidget.showWidget(contentWidget);
 			}
 		}
 	}
 }
