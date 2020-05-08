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
 package br.com.sysmap.crux.core.rebind.widget;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.google.gwt.core.ext.typeinfo.JClassType;
 
 import br.com.sysmap.crux.core.client.Crux;
 import br.com.sysmap.crux.core.client.declarative.TagAttribute;
 import br.com.sysmap.crux.core.client.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagEvent;
 import br.com.sysmap.crux.core.client.declarative.TagEvents;
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.config.ConfigurationFactory;
 import br.com.sysmap.crux.core.i18n.MessagesFactory;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.GeneratorMessages;
 import br.com.sysmap.crux.core.rebind.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.widget.creator.event.AttachEvtBind;
 import br.com.sysmap.crux.core.rebind.widget.creator.event.DettachEvtBind;
 import br.com.sysmap.crux.core.rebind.widget.creator.event.LoadWidgetEvtProcessor;
 
 /**
  * Generate code for gwt widgets creation. Generates code based on a JSON meta data array
  * containing the information declared on crux pages. 
  * 
  * @author Thiago da Rosa de Bustamante
  */
 public abstract class WidgetCreator <C extends WidgetCreatorContext>
 {
 	private static int currentId = 0;
 	private static GeneratorMessages messages = (GeneratorMessages)MessagesFactory.getMessages(GeneratorMessages.class);
 	
 	private ViewFactoryCreator factory = null;
 	private WidgetCreatorAnnotationsProcessor annotationProcessor;
 	
 	/**
 	 * @param factory
 	 */
 	void setViewFactory(ViewFactoryCreator factory)
 	{
 		this.factory = factory;
 		JClassType type = this.factory.getJClassType(getClass());
 		this.annotationProcessor = new WidgetCreatorAnnotationsProcessor(type, this);
 	}
 	
 	/**
 	 * @return
 	 */
 	ViewFactoryCreator getViewFactory()
 	{
 		return this.factory;
 	}
 	
 	/**
 	 * Retrieve the widget child element name
 	 * @param childElement element representing the child
 	 * @return child name
 	 */
 	public static String getChildName(JSONObject childElement)
 	{
 		return childElement.optString("_childTag");
 	}
 	
 	/**
 	 * Used by widgets that need to create new widgets as children. 
 	 * 
 	 * @param out
 	 * @param metaElem
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public String createChildWidget(SourcePrinter out, JSONObject metaElem) throws CruxGeneratorException
 	{
 		if (!metaElem.has("id"))
 		{
 			throw new CruxGeneratorException(messages.screenFactoryWidgetIdRequired());
 		}
 		String widgetId = metaElem.optString("id");
 		return factory.newWidget(out, metaElem, widgetId, factory.getMetaElementType(metaElem));
 	}
 
 	/**
 	 * Used by widgets that need to create new widgets as children. 
 	 * 
 	 * @param out
 	 * @param metaElem
 	 * @param widgetId
 	 * @param widgetType
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public String createChildWidget(SourcePrinter out, JSONObject metaElem, String widgetId, String widgetType) throws CruxGeneratorException
 	{
 		return factory.newWidget(out, metaElem, widgetId, widgetType);
 	}
 	
 	/**
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 * @throws CruxGeneratorException 
 	 */
 	protected static JSONArray ensureChildren(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException 
 	{
 		if (!acceptsNoChild && !metaElem.has("_children"))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureChildrenEmpty());
 		}
 		
 		JSONArray children = metaElem.optJSONArray("_children");
 		if (acceptsNoChild && children == null)
 		{
 			return null;
 		}
 
 		if (!acceptsNoChild && (children == null || children.length() == 0 || children.opt(0)==null))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureChildrenEmpty());
 		}
 		return children;
 	}
 
 	/**
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 */
 	protected static JSONObject ensureFirstChild(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException
 	{
 		if (!acceptsNoChild && !metaElem.has("_children"))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureChildrenEmpty());
 		}
 		JSONArray children = metaElem.optJSONArray("_children");
 		if (acceptsNoChild && children == null)
 		{
 			return null;
 		}
 		if (!acceptsNoChild && (children == null || children.length() == 0))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureChildrenEmpty());
 		}
 		JSONObject firstChild = children.optJSONObject(0);
 		if (!acceptsNoChild && firstChild == null)
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureChildrenEmpty());
 		}
 		return firstChild;
 	}
 
 	/**
 	 * 
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 * @throws CruxGeneratorException 
 	 */
 	protected static String ensureTextChild(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_text");
 		if (!acceptsNoChild && (result == null || result.length() == 0))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureTextChildEmpty());
 		}
 		return result;
 	}
 
 	/**
 	 * @param metaElem
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	protected static boolean isTextChild(JSONObject metaElem) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_text");
 		return (!StringUtils.isEmpty(result));
 	}	
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 * @throws CruxGeneratorException 
 	 */
 	protected static String ensureHtmlChild(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_html");
 		if (!acceptsNoChild && (result == null || result.length() == 0))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureHtmlChildEmpty());
 		}
 		return result;
 	}
 
 	/**
 	 * @param metaElem
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	protected static boolean isHtmlChild(JSONObject metaElem) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_html");
 		return (!StringUtils.isEmpty(result));
 	}		
 	
 	/**
 	 * Creates a sequential id
 	 * @return
 	 */
 	protected static String generateNewId() 
 	{
 		return "_crux_" + (++currentId );//TODO precisa disso ainda?
 	}
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
 	protected static boolean hasHeight(JSONObject metaElem)
 	{
 		if (!metaElem.has("height"))
 		{
 			return false;
 		}
 		String width = metaElem.optString("height");
 		return width != null && (width.length() > 0);
 	}
 
 	/**
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
 	protected static boolean hasWidth(JSONObject metaElem)
 	{
 		if (!metaElem.has("width"))
 		{
 			return false;
 		}
 		String width = metaElem.optString("width");
 		return width != null && (width.length() > 0);
 	}
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
 	protected boolean isWidget(JSONObject metaElem)
 	{
 		return factory.isValidWidget(metaElem);
 	}
 
 	/**
 	 * 
 	 * @param out
 	 * @param metaElem
 	 * @param widgetId
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public final String createWidget(SourcePrinter out, JSONObject metaElem, String widgetId) throws CruxGeneratorException
 	{
 		return createWidget(out, metaElem, widgetId, true);
 	}
 	
 	/**
 	 * Generates the code for the given widget creation. 
 	 * 
 	 * @param out
 	 * @param metaElem
 	 * @param widgetId
 	 * @param addToScreen
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public String createWidget(SourcePrinter out, JSONObject metaElem, String widgetId, boolean addToScreen) throws CruxGeneratorException
 	{
 		C context = createContext(out, metaElem, widgetId, addToScreen);
 		if (context != null)
 		{
 			annotationProcessor.processAttributes(out, context);
 			processAttributes(out, context);
 			annotationProcessor.processEvents(out, context);
 			processEvents(out, context);
 			annotationProcessor.processChildren(out, context);
 			processChildren(out, context);
 			postProcess(out, context);
 			return context.getWidget();
 		}
 		return null;
 	}
 	
 	/**
 	 * @param property
 	 * @return
 	 */
	public String getDeclaredMessage(String property)
 	{
 		return factory.getDeclaredMessage(property);
 	}
 	
 	/**
 	 * @param metaElem
 	 * @param widgetId
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public abstract String instantiateWidget(SourcePrinter out, JSONObject metaElem, String widgetId) throws CruxGeneratorException;
 
 	/**
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected C instantiateContext()
 	{   //TODO remover isso
 		return (C)new WidgetCreatorContext();
 	}
 
 	/**
 	 * Process element children
 	 * @param out 
 	 * @param context
 	 * @throws CruxGeneratorException 
 	 */
 	public void postProcess(SourcePrinter out, C context) throws CruxGeneratorException
 	{
 	}
 	
 	/**
 	 * Process widget attributes
 	 * @param out 
 	 * @param element page DOM element representing the widget (Its &lt;span&gt; tag)
 	 * @throws CruxGeneratorException 
 	 */
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration(value="id", required=true)
 	})
 	@TagAttributes({
 		@TagAttribute("width"),
 		@TagAttribute("height"),
 		@TagAttribute("styleName"),
 		@TagAttribute(value="visible", type=Boolean.class),
 		@TagAttribute(value="tooltip", supportsI18N=true, property="title"),
 		@TagAttribute(value="style", processor=StyleProcessor.class)
 	})
 	public void processAttributes(SourcePrinter out, C context) throws CruxGeneratorException
 	{
 	}
 	
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
 	public static class StyleProcessor extends AttributeProcessor<WidgetCreatorContext>
 	{
 		public void processAttribute(SourcePrinter out, WidgetCreatorContext context, String style)
 		{
 			String[] styleAttributes = style.split(";");
 			if (styleAttributes.length > 0)
 			{
 				String element = ViewFactoryCreator.createVariableName("elem");
 				out.println("Element  = "+context.getWidget()+".getElement();");
 				for (int i=0; i<styleAttributes.length; i++)
 				{
 					String[] attr = styleAttributes[i].split(":");
 					if (attr != null && attr.length == 2)
 					{
 						out.println("StyleUtils.addStyleProperty("+element+", "+EscapeUtils.quote(getStylePropertyName(attr[0]))+
 								", "+EscapeUtils.quote(attr[1])+");");
 					}
 				}
 			}
 		}
 		
 		private String getStylePropertyName(String property)
 		{
 			int index = -1;
 			while ((index = property.indexOf('-')) >0)
 			{
 				if (index < property.length()-1)
 				{
 					property = property.substring(0, index) + Character.toUpperCase(property.charAt(index+1)) + property.substring(index+2);
 				}
 			}
 			return property;
 		}
 	}
 	
 	/**
 	 * Process element children
 	 * @param out 
 	 * @param context
 	 * @throws CruxGeneratorException 
 	 */
 	public void processChildren(SourcePrinter out, C context) throws CruxGeneratorException
 	{
 	}
 	
 	/**
 	 * Process widget events
 	 * @param out 
 	 * @param context 
 	 * @throws CruxGeneratorException
 	 */
 	@TagEvents({
 		@TagEvent(LoadWidgetEvtProcessor.class),
 		@TagEvent(AttachEvtBind.class),
 		@TagEvent(DettachEvtBind.class)
 	})
 	public void processEvents(SourcePrinter out, C context) throws CruxGeneratorException
 	{
 	}
 	
 	/**
 	 * Print code that will be executed after the viewFactory completes the widgets construction
 	 * @param s code string
 	 */
 	protected void printlnPostProcessing(String s)
 	{
 		factory.printlnPostProcessing(s);
 	}
 	
 	/**
 	 * @param srcWriter 
 	 * @param element
 	 * @param widgetId
 	 * @param addToScreen
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	protected C createContext(SourcePrinter out, JSONObject metaElem, String widgetId, boolean addToScreen) throws CruxGeneratorException
 	{
 		String widget = instantiateWidget(out, metaElem, widgetId);
 		if (widget != null)
 		{
 			if(addToScreen)
 			{
 				out.println(factory.getScreenVariable()+".addWidget("+EscapeUtils.quote(widgetId)+", "+widget+");");
 			}			
 			if (Boolean.parseBoolean(ConfigurationFactory.getConfigurations().renderWidgetsWithIDs()))
 			{
 				out.println("ViewFactoryUtils.updateWidgetElementId("+EscapeUtils.quote(widgetId)+", "+widget+");");
 			}
 			C context = instantiateContext();
 			context.setWidget(widget);
 			context.setWidgetElement(metaElem);
 			context.setWidgetId(widgetId);
 			context.setChildElement(metaElem);
 			return context;
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param element
 	 * @return
 	 */
 	protected JSONObject ensureWidget(JSONObject metaElem) 
 	{
 		assert(isWidget(metaElem)):Crux.getMessages().widgetFactoryEnsureWidgetFail();
 		return metaElem;
 	}
 	
 }
