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
 package br.com.sysmap.crux.widgets.rebind.stackmenu;
 
 import java.util.LinkedList;
 
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreator;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreatorContext;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.HasPostProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.event.SelectionEvtBind;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChild;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChildren;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagConstraints;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvent;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvents;
 import br.com.sysmap.crux.widgets.client.stackmenu.StackMenu;
 import br.com.sysmap.crux.widgets.client.stackmenu.StackMenuItem;
 
 /**
  * Factory for Stack Menu
  * @author Gesse S. F. Dafe
  */
 @DeclarativeFactory(id="stackMenu", library="widgets", targetWidget=StackMenu.class)
 @TagChildren({
 	@TagChild(StackMenuFactory.StackMenuItemProcessor.class)
 })
 @TagEvents({
 	@TagEvent(SelectionEvtBind.class)
 })
 public class StackMenuFactory extends WidgetCreator<StackMenuContext>
 {
 
 	@Override
 	public void processChildren(SourcePrinter out, StackMenuContext context) throws CruxGeneratorException
 	{
 		context.itemStack.add(context.getWidget());
 	}
 	
 	@TagConstraints(tagName="item", minOccurs="0", maxOccurs="unbounded")
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration(value="key", required=true),
 		@TagAttributeDeclaration(value="label", supportsI18N=true, required=true),
 		@TagAttributeDeclaration(value="open", type=Boolean.class),
 		@TagAttributeDeclaration(value="style"),
 		@TagAttributeDeclaration(value="styleName"),
 		@TagAttributeDeclaration(value="tooltip")
 	})
 	@TagChildren({
 		@TagChild(StackMenuItemProcessor.class)
 	})
 	public static class StackMenuItemProcessor extends WidgetChildProcessor<StackMenuContext> implements HasPostProcessor<StackMenuContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, StackMenuContext context) throws CruxGeneratorException 
 		{
 			String item = getWidgetCreator().createVariableName("item");
 			String className = StackMenuItem.class.getCanonicalName();
 			
 			String label = context.getChildElement().optString("label");
 			label = getWidgetCreator().getDeclaredMessage(label);
 			
 			String key = context.getChildElement().optString("key");
 			key = EscapeUtils.quote(key);	
 			
 			out.println(className + " " + item+" = new "+className+"("+key+", "+ label +");");			
 			setItemAttributes(out, context, item);
 			String parentWidget = context.itemStack.getFirst();
 			out.println(parentWidget+".add("+item+");");
 			
 			context.itemStack.addFirst(item);
 		}
 		
 		public void postProcessChildren(SourcePrinter out, StackMenuContext context) throws CruxGeneratorException
 		{
 			context.itemStack.removeFirst();			
 		}
 
 		/**
 		 * Sets the item attributes before adding it to the parent.
 		 * @param out
 		 * @param context
 		 * @param item
 		 */
 		private void setItemAttributes(SourcePrinter out, StackMenuContext context, String item)
 		{
 			String open = context.readChildProperty("open");
 			if (!StringUtils.isEmpty(open))
 			{
 				out.println(item + ".setOpen(" + Boolean.parseBoolean(open) + ");");
 			}
 			
 			String style = context.readChildProperty("style");
 			if (!StringUtils.isEmpty(style))
 			{
 				out.println(item + ".setStyle(" + EscapeUtils.quote(style) + ");");
 			}
 			
 			String styleName = context.readChildProperty("styleName");
 			if (!StringUtils.isEmpty(styleName))
 			{
 				out.println(item + ".setStyleName(" + EscapeUtils.quote(styleName) + ");");
 			}
 			
 			String tooltip = context.readChildProperty("tooltip");
 			if (!StringUtils.isEmpty(tooltip))
 			{
 				out.println(item + ".setTitle(" + EscapeUtils.quote(tooltip) + ");");
 			}
 		}				
 	}
 	
 	@Override
     public StackMenuContext instantiateContext()
     {
 	    return new StackMenuContext();
     }
 }
 
 /**
  * Context for Stack Menu
  * @author Gesse S. F. Dafe
  */
 class StackMenuContext extends WidgetCreatorContext
 {
 	LinkedList<String> itemStack = new LinkedList<String>();
 }
