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
 package br.com.sysmap.crux.widgets.rebind.titlepanel;
 
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.widget.creator.children.AnyWidgetChildProcessor;
 import br.com.sysmap.crux.core.rebind.widget.creator.children.ChoiceChildProcessor;
 import br.com.sysmap.crux.core.rebind.widget.creator.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.rebind.widget.creator.children.WidgetChildProcessor.HTMLTag;
 import br.com.sysmap.crux.core.rebind.widget.declarative.TagChild;
 import br.com.sysmap.crux.core.rebind.widget.declarative.TagChildAttributes;
 import br.com.sysmap.crux.core.rebind.widget.declarative.TagChildren;
 import br.com.sysmap.crux.gwt.rebind.CellPanelContext;
 import br.com.sysmap.crux.widgets.rebind.decoratedpanel.AbstractDecoratedPanelFactory;
 
 /**
  * Factory for Title Panel widget
  * @author Gesse S. F. Dafe
  */
 public abstract class AbstractTitlePanelFactory extends AbstractDecoratedPanelFactory
 {
 	@TagChildAttributes(tagName="title", minOccurs="0")
 	@TagChildren({
 		@TagChild(TitleChildrenProcessor.class)
 	})
 	public static class TitleProcessor extends WidgetChildProcessor<CellPanelContext> {}
 
 	@TagChildren({
 		@TagChild(TitlePanelHTMLChildProcessor.class),
 		@TagChild(TitlePanelTextChildProcessor.class),
 		@TagChild(TitlePanelWidgetChildProcessor.class)
 	})
 	public static class TitleChildrenProcessor extends ChoiceChildProcessor<CellPanelContext> {}
 	
 	@TagChildAttributes(tagName="html", type=HTMLTag.class)
 	public static abstract class HTMLChildProcessor extends WidgetChildProcessor<CellPanelContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, CellPanelContext context) throws CruxGeneratorException
 		{
 			String innerHtml = ensureHtmlChild(context.getChildElement(),true);
 			String rootWidget = context.getWidget();
 			out.println(rootWidget+".setTitleHtml("+EscapeUtils.quote(innerHtml)+");");
 		}
 	}
 
 	@TagChildAttributes(tagName="text", type=String.class)
 	public static abstract class TextChildProcessor extends WidgetChildProcessor<CellPanelContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, CellPanelContext context) throws CruxGeneratorException
 		{
 			String innerText = ensureTextChild(context.getChildElement(),true);
 			String i18nText = getWidgetCreator().getDeclaredMessage(innerText);
 			String rootWidget = context.getWidget();
 			out.println(rootWidget+".setTitleText("+EscapeUtils.quote(i18nText)+");");
 		}
 	}
 		
 	@TagChildAttributes(tagName="widget")
 	@TagChildren({
 		@TagChild(TitleWidgetTitleProcessor.class)
 	})
 	public static class TitlePanelWidgetChildProcessor extends WidgetChildProcessor<CellPanelContext> {}
 
 	@TagChildAttributes(widgetProperty="titleWidget")
 	public static class TitleWidgetTitleProcessor extends AnyWidgetChildProcessor<CellPanelContext> {}
 	
 	
 	@TagChildAttributes(tagName="body", minOccurs="0")
 	@TagChildren({
 		@TagChild(BodyChildrenProcessor.class)
 	})
 	public static class BodyProcessor extends WidgetChildProcessor<CellPanelContext> {}
 
 	@TagChildren({
 		@TagChild(TitlePanelBodyHTMLChildProcessor.class),
 		@TagChild(TitlePanelBodyTextChildProcessor.class),
 		@TagChild(TitlePanelBodyWidgetProcessor.class)
 	})
 	public static class BodyChildrenProcessor extends ChoiceChildProcessor<CellPanelContext> {}	
 	
 	@TagChildAttributes(tagName="html", type=HTMLTag.class)
 	public static abstract class BodyHTMLChildProcessor extends WidgetChildProcessor<CellPanelContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, CellPanelContext context) throws CruxGeneratorException
 		{
 			String innerHtml = ensureHtmlChild(context.getChildElement(), true);
 			String rootWidget = context.getWidget();
 			out.println(rootWidget+".setContentHtml("+EscapeUtils.quote(innerHtml)+");");
 		}
 	}
 
 	@TagChildAttributes(tagName="text", type=String.class)
 	public static abstract class BodyTextChildProcessor extends WidgetChildProcessor<CellPanelContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, CellPanelContext context) throws CruxGeneratorException
 		{
 			String innerText = ensureTextChild(context.getChildElement(), true);
 			String i18nText = getWidgetCreator().getDeclaredMessage(innerText);
 			String rootWidget = context.getWidget();
 			out.println(rootWidget+".setContentText("+EscapeUtils.quote(i18nText)+");");
 		}
 	}
 	
 	@TagChildAttributes(tagName="widget")
 	@TagChildren({
 		@TagChild(BodyWidgetContentProcessor.class)
 	})
 	public static class TitlePanelBodyWidgetProcessor extends WidgetChildProcessor<CellPanelContext> {}
 	
 	@TagChildAttributes(widgetProperty="contentWidget")
 	public static class BodyWidgetContentProcessor extends AnyWidgetChildProcessor<CellPanelContext> {}
 
 	public static class TitlePanelHTMLChildProcessor extends HTMLChildProcessor{}
 	public static class TitlePanelTextChildProcessor extends TextChildProcessor{}	
 	public static class TitlePanelBodyHTMLChildProcessor extends BodyHTMLChildProcessor{}
 	public static class TitlePanelBodyTextChildProcessor extends BodyTextChildProcessor {}
 	
 }
