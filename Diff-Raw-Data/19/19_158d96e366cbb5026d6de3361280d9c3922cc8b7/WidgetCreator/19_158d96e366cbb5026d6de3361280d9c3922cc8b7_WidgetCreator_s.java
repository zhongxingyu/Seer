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
 package br.com.sysmap.crux.core.rebind.screen.widget;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import br.com.sysmap.crux.core.client.Crux;
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.client.utils.StyleUtils;
 import br.com.sysmap.crux.core.config.ConfigurationFactory;
 import br.com.sysmap.crux.core.i18n.MessagesFactory;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.GeneratorMessages;
 import br.com.sysmap.crux.core.rebind.screen.Screen;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.event.AttachEvtBind;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.event.DettachEvtBind;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.event.LoadWidgetEvtProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttribute;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributes;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvent;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvents;
 
 import com.google.gwt.core.ext.GeneratorContextExt;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.dom.client.PartialSupport;
 
 /**
  * Generate code for gwt widgets creation. Generates code based on a JSON meta data array
  * containing the information declared on crux pages. 
  * 
  * @author Thiago da Rosa de Bustamante
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
 	@TagAttribute(value="style", processor=WidgetCreator.StyleProcessor.class)
 })
 @TagEvents({
 	@TagEvent(LoadWidgetEvtProcessor.class),
 	@TagEvent(AttachEvtBind.class),
 	@TagEvent(DettachEvtBind.class)
 })
 public abstract class WidgetCreator <C extends WidgetCreatorContext>
 {
 	private static GeneratorMessages messages = (GeneratorMessages)MessagesFactory.getMessages(GeneratorMessages.class);
 	
 	private WidgetCreatorAnnotationsProcessor annotationProcessor;
 	private ViewFactoryCreator factory = null;
 	
 	/**
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 * @throws CruxGeneratorException 
 	 */
 	public static JSONArray ensureChildren(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException 
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
 	public static JSONObject ensureFirstChild(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException
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
 	public static String ensureHtmlChild(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_html");
 		if (!acceptsNoChild && (result == null || result.length() == 0))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureHtmlChildEmpty());
 		}
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 * @throws CruxGeneratorException 
 	 */
 	public static String ensureTextChild(JSONObject metaElem, boolean acceptsNoChild) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_text");
 		if (!acceptsNoChild && (result == null || result.length() == 0))
 		{
 			throw new CruxGeneratorException(messages.widgetCreatorEnsureTextChildEmpty());
 		}
 		return result;
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
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
 	public static boolean hasHeight(JSONObject metaElem)
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
 	public static boolean hasWidth(JSONObject metaElem)
 	{
 		if (!metaElem.has("width"))
 		{
 			return false;
 		}
 		String width = metaElem.optString("width");
 		return width != null && (width.length() > 0);
 	}
 
 	/**
 	 * @param metaElem
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public static boolean isHtmlChild(JSONObject metaElem) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_html");
 		return (!StringUtils.isEmpty(result));
 	}
 
 	/**
 	 * @param metaElem
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public static boolean isTextChild(JSONObject metaElem) throws CruxGeneratorException
 	{
 		String result = metaElem.optString("_text");
 		return (!StringUtils.isEmpty(result));
 	}	
 	
 	public boolean containsWidget(String widgetId)
 	{
 		return factory.containsWidget(widgetId);
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
 	 * @param addToScreen
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public String createChildWidget(SourcePrinter out, JSONObject metaElem, boolean addToScreen) throws CruxGeneratorException
 	{
 		if (!metaElem.has("id"))
 		{
 			throw new CruxGeneratorException(messages.screenFactoryWidgetIdRequired());
 		}
 		String widgetId = metaElem.optString("id");
 		return factory.newWidget(out, metaElem, widgetId, factory.getMetaElementType(metaElem), addToScreen);
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
 	 * @param varName
 	 * @return
 	 */
 	public String createVariableName(String varName)
 	{
 		return ViewFactoryCreator.createVariableName(varName);
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
 		boolean partialSupport = hasPartialSupport();
 		C context = createContext(out, metaElem, widgetId, addToScreen);
 		if (partialSupport)
 		{
 			out.println("if ("+getWidgetClassName()+".isSupported()){");
 		}
 		if (context != null)
 		{
 			processAttributes(out, context);
 			annotationProcessor.processAttributes(out, context);
 			processEvents(out, context);
 			annotationProcessor.processEvents(out, context);
 			processChildren(out, context);
 			annotationProcessor.processChildren(out, context);
 			postProcess(out, context);
 			if (partialSupport)
 			{
 				out.println("}");
 			}
 			return context.getWidget();
 		}
 		else
 		{
 			if (partialSupport)
 			{
 				out.println("}");
 				out.println("else {");
 				out.println("Crux.getErrorHandler().handleError(Crux.getMessages().screenFactoryUnsupportedWidget());");
 				out.println("}");
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param element
 	 * @return
 	 */
 	public JSONObject ensureWidget(JSONObject metaElem) 
 	{
 		assert(isWidget(metaElem)):Crux.getMessages().widgetFactoryEnsureWidgetFail();
 		return metaElem;
 	}
 	
 	/**
 	 * @param metaElem
 	 * @return
 	 */
 	public String getChildWidgetClassName(JSONObject metaElem)
 	{
 		return factory.getWidgetCreator(factory.getMetaElementType(metaElem)).getWidgetClassName();
 	}
 	
 	/**
 	 * @return
 	 */
 	public GeneratorContextExt getContext()
 	{
 		return factory.getContext();
 	}
 
 	/**
 	 * @param subType
 	 * @param superClass
 	 * @param interfaces
 	 * @param imports
 	 * @return
 	 */
 	public  SourcePrinter getSubTypeWriter(String subType, String superClass, String[] interfaces, String[] imports)
 	{
 		return factory.getSubTypeWriter(subType, superClass, interfaces, imports);
 	}
 	
 	/**
 	 * Create a new printer for a subType. That subType will be declared on the same package of the
 	 * {@code ViewFactory}. 
 	 * 
      * @param subType
      * @param superClass
      * @param interfaces
      * @param imports
      * @param makeInterface
      * @return 
      */
     public SourcePrinter getSubTypeWriter(String subType, String superClass, String[] interfaces, String[] imports, boolean makeInterface)
     {
     	return factory.getSubTypeWriter(subType, superClass, interfaces, imports, makeInterface);
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
 	 * @return
 	 */
 	public TreeLogger getLogger()
 	{
 		return factory.getLogger();
 	}
 
 	/**
 	 * @return
 	 */
 	public Screen getScreen()
 	{
 		return factory.getScreen();
 	}
 
 	/**
 	 * @return
 	 */
 	public Class<?> getWidgetClass()
     {
 	    return factory.getWidgetCreatorHelper(getWidgetFactoryDeclaration()).getWidgetType();
     }
 
 	/**
 	 * @return
 	 */
 	public String getWidgetClassName()
     {
 	    return getWidgetClass().getCanonicalName();
     }
 	
 	/**
 	 * @return
 	 */
 	public String getWidgetFactoryDeclaration()
 	{
 		DeclarativeFactory declarativeFactory = getClass().getAnnotation(DeclarativeFactory.class);
 		if (declarativeFactory != null)
 		{
 			return declarativeFactory.library()+"_"+declarativeFactory.id();
 		}
 		throw new CruxGeneratorException(messages.widgetCreatorErrorReadingFactoryDeclaration()); 
 	}
 	
 	/**
 	 * @param metaElem
 	 * @return
 	 */
 	public boolean hasChildPartialSupport(JSONObject metaElem)
 	{
 		return factory.getWidgetCreator(factory.getMetaElementType(metaElem)).hasPartialSupport();
 	}
 	
 	/**
 	 * @return
 	 */
 	public boolean hasPartialSupport()
     {
 	    return getWidgetClass().getAnnotation(PartialSupport.class) != null;
     }
 	
 	/**
 	 * @return
 	 */
     public abstract C instantiateContext();
 	
 	
 	/**
 	 * @param metaElem
 	 * @param widgetId
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	public String instantiateWidget(SourcePrinter out, JSONObject metaElem, String widgetId) throws CruxGeneratorException
 	{
 		String varName = createVariableName("widget");
 		String className = getWidgetClassName();
 		out.println("final "+className + " " + varName+" = new "+className+"();");
 		return varName;
 	}
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
     public boolean isWidget(JSONObject metaElem)
 	{
 		return factory.isValidWidget(metaElem);
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
 	public void processAttributes(SourcePrinter out, C context) throws CruxGeneratorException
 	{
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
 	public void processEvents(SourcePrinter out, C context) throws CruxGeneratorException
 	{
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
 				if (Boolean.parseBoolean(ConfigurationFactory.getConfigurations().renderWidgetsWithIDs()))
 				{
 					out.println("ViewFactoryUtils.updateWidgetElementId("+EscapeUtils.quote(widgetId)+", "+widget+");");
 				}
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
 	 * Print code that will be executed after the viewFactory completes the widgets construction
 	 * @param s code string
 	 */
 	protected void printlnPostProcessing(String s)
 	{
 		factory.printlnPostProcessing(s);
 	}
 	
 	/**
 	 * @return
 	 */
 	ViewFactoryCreator getViewFactory()
 	{
 		return this.factory;
 	}
 	
 	/**
 	 * @param factory
 	 */
 	void setViewFactory(ViewFactoryCreator factory)
 	{
 		this.factory = factory;
 		this.annotationProcessor = new WidgetCreatorAnnotationsProcessor(getClass(), this);
 	}
 	
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
 	public static class StyleProcessor extends AttributeProcessor<WidgetCreatorContext>
 	{
 		public StyleProcessor(WidgetCreator<?> widgetCreator)
         {
 	        super(widgetCreator);
         }
 
 		public void processAttribute(SourcePrinter out, WidgetCreatorContext context, String style)
 		{
 			String[] styleAttributes = style.split(";");
 			if (styleAttributes.length > 0)
 			{
 				String element = ViewFactoryCreator.createVariableName("elem");
 				out.println("Element "+element+" = "+context.getWidget()+".getElement();");
 				for (int i=0; i<styleAttributes.length; i++)
 				{
 					String[] attr = styleAttributes[i].split(":");
 					if (attr != null && attr.length == 2)
 					{
 						out.println(StyleUtils.class.getCanonicalName()+".addStyleProperty("+element+", "+EscapeUtils.quote(getStylePropertyName(attr[0]))+
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
 }
