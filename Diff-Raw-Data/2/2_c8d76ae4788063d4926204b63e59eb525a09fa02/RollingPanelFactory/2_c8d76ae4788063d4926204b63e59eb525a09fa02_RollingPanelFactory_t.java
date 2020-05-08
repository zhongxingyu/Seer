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
 package br.com.sysmap.crux.widgets.client.rollingpanel;
 
 import br.com.sysmap.crux.core.client.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.client.declarative.TagAttribute;
 import br.com.sysmap.crux.core.client.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagAttributes;
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
 import br.com.sysmap.crux.core.client.screen.factory.HasHorizontalAlignmentFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasVerticalAlignmentFactory;
 import br.com.sysmap.crux.core.client.screen.factory.align.AlignmentAttributeParser;
 import br.com.sysmap.crux.core.client.screen.factory.align.HorizontalAlignment;
 import br.com.sysmap.crux.core.client.screen.factory.align.VerticalAlignment;
 import br.com.sysmap.crux.core.client.screen.parser.CruxMetaDataElement;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Widget;
 
 class RollingPanelContext extends WidgetFactoryContext
 {
 
 	public String verticalAlignment;
 	public String horizontalAlignment;
 	public String width;
 	public String height;
 	
 }
 
 /**
  * @author Thiago da Rosa de Bustamante
  *
  */
 @DeclarativeFactory(id="rollingPanel", library="widgets")
 public class RollingPanelFactory extends WidgetFactory<RollingPanel, RollingPanelContext>
        implements HasHorizontalAlignmentFactory<RollingPanel, RollingPanelContext>, 
                   HasVerticalAlignmentFactory<RollingPanel, RollingPanelContext>
 {
 
 	@Override
 	public RollingPanel instantiateWidget(CruxMetaDataElement element, String widgetId)
 	{
 		String verticalAttr = element.getProperty("vertical");
 		boolean vertical = false;
 		if (!StringUtils.isEmpty(verticalAttr))
 		{
 			vertical = Boolean.parseBoolean(verticalAttr);
 		}
 		return new RollingPanel(vertical);
 	}
 
 	@Override
 	@TagAttributesDeclaration({
		@TagAttributeDeclaration(value="vertical", type=Boolean.class, defaultValue="false")
 	})
 	@TagAttributes({
 		@TagAttribute("horizontalNextButtonStyleName"),
 		@TagAttribute("horizontalPreviousButtonStyleName"),
 		@TagAttribute("verticalNextButtonStyleName"),
 		@TagAttribute("verticalPreviousButtonStyleName"),
 		@TagAttribute(value="scrollToAddedWidgets", type=Boolean.class),
 		@TagAttribute(value="spacing", type=Integer.class)
 	})
 	public void processAttributes(RollingPanelContext context) throws InterfaceConfigException
 	{
 		super.processAttributes(context);
 
 	}
 	
 	@Override
 	@TagChildren({
 		@TagChild(RollingPanelProcessor.class)
 	})		
 	public void processChildren(RollingPanelContext context) throws InterfaceConfigException {}
 	
 	@TagChildAttributes(minOccurs="0", maxOccurs="unbounded")
 	public static class  RollingPanelProcessor extends ChoiceChildProcessor<RollingPanel, RollingPanelContext> 
 	{
 		@Override
 		@TagChildren({
 			@TagChild(RollingCellProcessor.class),
 			@TagChild(VerticalWidgetProcessor.class)
 		})		
 		public void processChildren(RollingPanelContext context) throws InterfaceConfigException  {}
 	}
 	
 	@TagChildAttributes(minOccurs="0", maxOccurs="unbounded", tagName="cell")
 	public static class RollingCellProcessor extends WidgetChildProcessor<RollingPanel, RollingPanelContext>
 	{
 		@TagAttributesDeclaration({
 			@TagAttributeDeclaration("height"),
 			@TagAttributeDeclaration("width"),
 			@TagAttributeDeclaration(value="horizontalAlignment", type=HorizontalAlignment.class, defaultValue="defaultAlign"),
 			@TagAttributeDeclaration(value="verticalAlignment", type=VerticalAlignment.class)
 		})
 		@TagChildren({
 			@TagChild(value=VerticalWidgetProcessor.class)
 		})		
 		public void processChildren(RollingPanelContext context) throws InterfaceConfigException 
 		{
 			context.height = context.readChildProperty("height");
 			context.width = context.readChildProperty("width");
 			context.horizontalAlignment = context.readChildProperty("horizontalAlignment");
 			context.verticalAlignment = context.readChildProperty("verticalAlignment");
 		}
 	}
 		
 	@TagChildAttributes(type=AnyWidget.class)
 	public static class VerticalWidgetProcessor extends WidgetChildProcessor<RollingPanel, RollingPanelContext> 
 	{
 		@Override
 		public void processChildren(RollingPanelContext context) throws InterfaceConfigException
 		{
 			Widget child = createChildWidget(context.getChildElement());
 			RollingPanel rootWidget = context.getWidget();
 			rootWidget.add(child);
 
 			if (!StringUtils.isEmpty(context.height))
 			{
 				rootWidget.setCellHeight(child, context.height);
 			}
 			if (!StringUtils.isEmpty(context.horizontalAlignment))
 			{
 				rootWidget.setCellHorizontalAlignment(child, 
 					  AlignmentAttributeParser.getHorizontalAlignment(context.horizontalAlignment, HasHorizontalAlignment.ALIGN_DEFAULT));
 			}
 			if (!StringUtils.isEmpty(context.verticalAlignment))
 			{
 				rootWidget.setCellVerticalAlignment(child, AlignmentAttributeParser.getVerticalAlignment(context.verticalAlignment));
 			}
 			if (!StringUtils.isEmpty(context.width))
 			{
 				rootWidget.setCellWidth(child, context.width);
 			}
 			
 			context.height = null;
 			context.width = null;
 			context.horizontalAlignment = null;
 			context.verticalAlignment = null;
 		}
 	}	
 }
