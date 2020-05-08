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
import br.com.sysmap.crux.core.client.declarative.TagAttributes;
import br.com.sysmap.crux.core.client.screen.AttributeParser;
 import br.com.sysmap.crux.core.client.screen.InterfaceConfigException;
 import br.com.sysmap.crux.core.client.screen.WidgetFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasAllMouseHandlersFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasClickHandlersFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasDirectionFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasTextFactory;
 import br.com.sysmap.crux.core.client.screen.factory.HasWordWrapFactory;
 import br.com.sysmap.crux.gwt.client.align.AlignmentAttributeParser;
 import br.com.sysmap.crux.gwt.client.align.HorizontalAlignment;
 
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 
 /**
 * Represents a Label DeclarativeFactory
  * @author Thiago Bustamante
  *
  */
 public abstract class AbstractLabelFactory<T extends Label> extends WidgetFactory<T> 
        implements HasDirectionFactory<T>, HasWordWrapFactory<T>, HasTextFactory<T>,
                   HasClickHandlersFactory<T>, HasAllMouseHandlersFactory<T>
 {
 	@Override
	@TagAttributes({
		@TagAttribute(value="horizontalAlignment", type=HorizontalAlignment.class, defaultValue="defaultAlign", parser=HorizontalAlignmentParser.class)
 	})
 	public void processAttributes(WidgetFactoryContext context) throws InterfaceConfigException
 	{
 		super.processAttributes(context);
	}
	
	public static class HorizontalAlignmentParser implements AttributeParser
	{
		public void processAttribute(WidgetFactoryContext context, String propertyValue) 
 		{
			Label widget = context.getWidget();
			widget.setHorizontalAlignment(AlignmentAttributeParser.getHorizontalAlignment(propertyValue, HasHorizontalAlignment.ALIGN_DEFAULT));
 		}
 	}
 }
