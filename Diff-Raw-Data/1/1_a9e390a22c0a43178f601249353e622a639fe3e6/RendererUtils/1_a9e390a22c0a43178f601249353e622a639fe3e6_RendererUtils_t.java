 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.ajax4jsf.renderkit;
 
 import java.awt.Dimension;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.FacesException;
 import javax.faces.application.ViewHandler;
 import javax.faces.component.EditableValueHolder;
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIForm;
 import javax.faces.component.UIViewRoot;
 import javax.faces.component.ValueHolder;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 
 import org.ajax4jsf.Messages;
 import org.ajax4jsf.javascript.JSEncoder;
 import org.ajax4jsf.javascript.JSFunctionDefinition;
 import org.ajax4jsf.renderkit.compiler.TemplateContext;
 import org.ajax4jsf.resource.InternetResource;
 import org.ajax4jsf.resource.Java2Dresource;
 import org.ajax4jsf.util.HtmlDimensions;
 
 /**
  * Util class for common render operations - render passthru html attributes,
  * iterate over child components etc.
  * 
  * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
  * @version $Revision: 1.1.2.6 $ $Date: 2007/02/08 19:07:16 $
  * 
  */
 public class RendererUtils {
 	
 	//we'd better use this instance multithreadly quickly
 	private static final RendererUtils instance = new RendererUtils();
 	
 	public static final String DUMMY_FORM_ID = ":_form";
 	/**
 	 * Substitutions for components properies names and HTML attributes names.
 	 */
 	private static Map<String, String> substitutions = new HashMap<String, String>();
 	
 	private static Set<String> requiredAttributes = new HashSet<String>();
 
 	static {
 		substitutions.put(HTML.class_ATTRIBUTE, "styleClass");
 		requiredAttributes.add(HTML.alt_ATTRIBUTE);
 		Arrays.sort(HTML.PASS_THRU);
 		Arrays.sort(HTML.PASS_THRU_BOOLEAN);
 		Arrays.sort(HTML.PASS_THRU_URI);
 	}
 
 	//can be created by subclasses; 
 	//administratively restricted to be created by package members ;)
 	protected RendererUtils() {
 		super();
 	}
 	
 	/**
 	 * Use this method to get singleton instance of RendererUtils
 	 * @return singleton instance
 	 */
 	public static RendererUtils getInstance() {
 		return instance;
 	}
 
 	/**
 	 * Common HTML elements and attributes names.
 	 * 
 	 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 	 * @version $Revision: 1.1.2.6 $ $Date: 2007/02/08 19:07:16 $
 	 * 
 	 */
 	public interface HTML {
 		// elements
 		public static final String BUTTON = "button";
 		// attributes
 		public static final String id_ATTRIBUTE = "id";
 		public static final String class_ATTRIBUTE = "class";
 		// public static final String STYLE_ATTRIBUTE = "style";
 		// public static final String dir_ATTRIBUTE = "dir";
 		// public static final String lang_ATTRIBUTE = "lang";
 		// public static final String title_ATTRIBUTE = "title";
 		public static final String accesskey_ATTRIBUTE = "accesskey";
 		public static final String alt_ATTRIBUTE = "alt";
		public static final String autocomplete_ATTRIBUTE = "autocomplete";
 		public static final String cols_ATTRIBUTE = "cols";
 
 		public static final String height_ATTRIBUTE = "height";
 
 		public static final String lang_ATTRIBUTE = "lang";
 
 		public static final String longdesc_ATTRIBUTE = "longdesc";
 
 		public static final String maxlength_ATTRIBUTE = "maxlength";
 
 		public static final String onblur_ATTRIBUTE = "onblur";
 
 		public static final String onchange_ATTRIBUTE = "onchange";
 
 		public static final String onclick_ATTRIBUTE = "onclick";
 
 		public static final String ondblclick_ATTRIBUTE = "ondblclick";
 
 		public static final String onfocus_ATTRIBUTE = "onfocus";
 
 		public static final String onkeydown_ATTRIBUTE = "onkeydown";
 
 		public static final String onkeypress_ATTRIBUTE = "onkeypress";
 
 		public static final String onkeyup_ATTRIBUTE = "onkeyup";
 
 		public static final String onload_ATTRIBUTE = "onload";
 
 		public static final String onmousedown_ATTRIBUTE = "onmousedown";
 
 		public static final String onmousemove_ATTRIBUTE = "onmousemove";
 
 		public static final String onmouseout_ATTRIBUTE = "onmouseout";
 
 		public static final String onmouseover_ATTRIBUTE = "onmouseover";
 
 		public static final String onmouseup_ATTRIBUTE = "onmouseup";
 
 		public static final String onreset_ATTRIBUTE = "onreset";
 
 		public static final String onselect_ATTRIBUTE = "onselect";
 
 		// public static final String onsubmit_ATTRIBUTE = "onsubmit";
 		public static final String onunload_ATTRIBUTE = "onunload";
 
 		public static final String rows_ATTRIBUTE = "rows";
 
 		public static final String size_ATTRIBUTE = "size";
 
 		public static final String tabindex_ATTRIBUTE = "tabindex";
 
 		public static final String title_ATTRIBUTE = "title";
 
 		public static final String style_ATTRIBUTE = "style";
 
 		public static final String align_ATTRIBUTE = "align";
 
 		public static final String width_ATTRIBUTE = "width";
 
 		public static final String dir_ATTRIBUTE = "dir";
 
 		public static final String rules_ATTRIBUTE = "rules";
 
 		public static final String frame_ATTRIBUTE = "frame";
 
 		public static final String border_ATTRIBUTE = "border";
 
 		public static final String cellspacing_ATTRIBUTE = "cellspacing";
 
 		public static final String cellpadding_ATTRIBUTE = "cellpadding";
 
 		public static final String summary_ATTRIBUTE = "summary";
 
 		public static final String bgcolor_ATTRIBUTE = "bgcolor";
 
 		public static final String usemap_ATTRIBUTE = "usemap";
 
 		public static final String enctype_ATTRIBUTE = "enctype";
 
 		public static final String accept_charset_ATTRIBUTE = "accept-charset";
 
 		public static final String accept_ATTRIBUTE = "accept";
 
 		public static final String target_ATTRIBUTE = "target";
 
 		public static final String onsubmit_ATTRIBUTE = "onsubmit";
 
 		public static final String readonly_ATTRIBUTE = "readonly";
 
 		public static final String nowrap_ATTRIBUTE = "nowrap";
 
 		public static final String src_ATTRIBUTE = "src";
 
 		public static final String media_ATTRIBUTE = "media";
 
 		// public static final String onreset_ATTRIBUTE = "onreset";
 		// attributes sets.
 		public static final String[] PASS_THRU = {
 				// DIR_ATTRIBUTE,
 				// LANG_ATTRIBUTE,
 				// STYLE_ATTRIBUTE,
 				// TITLE_ATTRIBUTE
 				"accesskey", "alt", "cols", "height", "lang", "longdesc",
 				"maxlength", "onblur", "onchange", "onclick", "ondblclick",
 				"onfocus", "onkeydown", "onkeypress", "onkeyup", "onload",
 				"onmousedown", "onmousemove", "onmouseout", "onmouseover",
 				"onmouseup", "onreset", "onselect", "onsubmit", "onunload",
 				"rows", "size", "tabindex", "title", "width", "dir", "rules",
 				"frame", "border", "cellspacing", "cellpadding", "summary",
 				"bgcolor", "usemap", "enctype", "accept-charset", "accept",
 				"target", "charset", "coords", "hreflang", "rel", "rev",
 				"shape", "disabled", "readonly", "ismap", "align"
 
 		};
 
 		/**
 		 * HTML attributes allowed boolean-values only
 		 */
 		public static final String[] PASS_THRU_BOOLEAN = { "disabled",
 				"declare", "readonly", "compact", "ismap", "selected",
 				"checked", "nowrap", "noresize", "nohref", "noshade",
 				"multiple" };
 
 		/**
 		 * all HTML attributes with URI value.
 		 */
 		public static final String[] PASS_THRU_URI = { "usemap", "background",
 				"codebase", "cite", "data", "classid", "href", "longdesc",
 				"profile", "src" };
 
 		public static final String[] PASS_THRU_STYLES = { "style", "class", };
 
 		public static final String SPAN_ELEM = "span";
 		public static final String DIV_ELEM = "div";
 		public static final String SCRIPT_ELEM = "script";
 		public static final String LINK_ELEMENT = "link";
 		public static final String STYLE_CLASS_ATTR = "styleClass";
 		public static final String DISABLED_ATTR = "disabled";
 		public static final String TYPE_ATTR = "type";
 		public static final String CHARSET_ATTR = "charset";
 		public static final String COORDS_ATTR = "coords";
 		public static final String HREFLANG_ATTR = "hreflang";
 		public static final String HREF_ATTR = "href";
 		public static final String REL_ATTR = "rel";
 		public static final String REV_ATTR = "rev";
 		public static final String SHAPE_ATTR = "shape";
 		public static final String title_ELEM = "title";
 		public static final String FORM_ELEMENT = "form";
 		public static final String NAME_ATTRIBUTE = "name";
 		public static final String METHOD_ATTRIBUTE = "method";
 		public static final String ACTION_ATTRIBUTE = "action";
 		public static final String INPUT_ELEM = "input";
 		public static final Object INPUT_TYPE_HIDDEN = "hidden";
 		public static final String value_ATTRIBUTE = "value";
 		public static final String td_ELEM = "td";
 		public static final String th_ELEM = "th";
 		public static final String valign_ATTRIBUTE = "valign";
 		public static final String a_ELEMENT = "a";
 		public static final String HTML_ELEMENT = "html";
 		public static final String HEAD_ELEMENT = "head";
 		public static final String BODY_ELEMENT = "body";
 		public static final String TABLE_ELEMENT = "table";
 		public static final String TR_ELEMENT = "tr";
 		public static final String CAPTION_ELEMENT = "caption";
 		public static final String TBOBY_ELEMENT = "tbody";
 		public static final String THEAD_ELEMENT = "thead";
 		public static final String TFOOT_ELEMENT = "tfoot";
 		public static final String IMG_ELEMENT = "img";
 		
 		public static final String DT_ELEMENT = "dt";
 		public static final String DL_ELEMENT = "dl";
 	}
 
 	/**
 	 * Encode id attribute with clientId component property
 	 * 
 	 * @param context
 	 * @param component
 	 * @throws IOException
 	 */
 	public void encodeId(FacesContext context, UIComponent component)
 			throws IOException {
 		encodeId(context, component, HTML.id_ATTRIBUTE);
 	}
 
 	/**
 	 * Encode clientId to custom attribute ( for example, to control name )
 	 * 
 	 * @param context
 	 * @param component
 	 * @param attribute
 	 * @throws IOException
 	 */
 	public void encodeId(FacesContext context, UIComponent component,
 			String attribute) throws IOException {
 		String clientId = null;
 		try {
 			clientId = component.getClientId(context);
 		} catch (Exception e) {
 			// just ignore if clientId wasn't inited yet
 		}
 		if (null != clientId) {
 			context.getResponseWriter().writeAttribute(attribute, clientId,
 					(String) getComponentAttributeName(attribute));
 		}
 	}
 
 	/**
 	 * Encode id attribute with clientId component property. Encoded only if id
 	 * not auto generated.
 	 * 
 	 * @param context
 	 * @param component
 	 * @throws IOException
 	 */
 	public void encodeCustomId(FacesContext context, UIComponent component)
 			throws IOException {
 		if (component.getId() != null
 				&& !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
 			context.getResponseWriter().writeAttribute(HTML.id_ATTRIBUTE,
 					component.getClientId(context), HTML.id_ATTRIBUTE);
 		}
 	}
 
 	/**
 	 * Encode common pass-thru html attributes.
 	 * 
 	 * @param context
 	 * @param component
 	 * @throws IOException
 	 */
 	public void encodePassThru(FacesContext context, UIComponent component)
 			throws IOException {
 		encodeAttributesFromArray(context, component, HTML.PASS_THRU);
 	}
 
 	/**
 	 * Encode pass-through attributes except specified ones
 	 * 
 	 * @param context
 	 * @param component
 	 * @param exclusions
 	 * @throws IOException
 	 */
 	public void encodePassThruWithExclusions(FacesContext context,
 			UIComponent component, String exclusions) throws IOException {
 		if (null != exclusions) {
 			String[] exclusionsArray = exclusions.split(",");
 			encodePassThruWithExclusionsArray(context, component,
 					exclusionsArray);
 		}
 	}
 
 	public void encodePassThruWithExclusionsArray(FacesContext context,
 			UIComponent component, Object[] exclusions) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		Map<String, Object> attributes = component.getAttributes();
 		Arrays.sort(exclusions);
 		for (int i = 0; i < HTML.PASS_THRU.length; i++) {
 			String attribute = HTML.PASS_THRU[i];
 			if (Arrays.binarySearch(exclusions, attribute) < 0) {
 				encodePassThruAttribute(context, attributes, writer, attribute);
 			}
 		}
 	}
 
 	/**
 	 * Encode one pass-thru attribute, with plain/boolean/url value, got from
 	 * properly component attribute.
 	 * 
 	 * @param context
 	 * @param component
 	 * @param writer
 	 * @param attribute
 	 * @throws IOException
 	 */
 	public void encodePassThruAttribute(FacesContext context, Map<String, Object> attributes,
 			ResponseWriter writer, String attribute) throws IOException {
 		Object value = attributeValue(attribute, attributes
 				.get(getComponentAttributeName(attribute)));
 		if (null != value && shouldRenderAttribute(attribute, value)) {
 			if (Arrays.binarySearch(HTML.PASS_THRU_URI, attribute) >= 0) {
 				String url = context.getApplication().getViewHandler()
 						.getResourceURL(context, value.toString());
 				url = context.getExternalContext().encodeResourceURL(url);
 				writer.writeURIAttribute(attribute, url, attribute);
 			} else {
 				writer.writeAttribute(attribute, value, attribute);
 			}
 		}
 	}
 
 	public void encodeAttributesFromArray(FacesContext context,
 			UIComponent component, String[] attrs) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		Map<String, Object> attributes = component.getAttributes();
 		for (int i = 0; i < attrs.length; i++) {
 			String attribute = attrs[i];
 			encodePassThruAttribute(context, attributes, writer, attribute);
 		}
 	}
 
 	/**
 	 * Encode attributes given by comma-separated string list.
 	 * 
 	 * @param context
 	 *            current JSF context
 	 * @param component
 	 *            for with render attributes values
 	 * @param attrs
 	 *            comma separated list of attributes
 	 * @throws IOException
 	 */
 	public void encodeAttributes(FacesContext context, UIComponent component,
 			String attrs) throws IOException {
 		if (null != attrs) {
 			String[] attrsArray = attrs.split(",");
 			encodeAttributesFromArray(context, component, attrsArray);
 		}
 	}
 
 	/**
 	 * @param context
 	 * @param component
 	 * @param string
 	 * @param string2
 	 * @throws IOException
 	 */
 	public void encodeAttribute(FacesContext context, UIComponent component,
 			Object property, String attributeName) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		Object value = component.getAttributes().get(property);
 		if (shouldRenderAttribute(attributeName, value)) {
 			writer.writeAttribute(attributeName, value, property.toString());
 		}
 
 	}
 
 	public void encodeAttribute(FacesContext context, UIComponent component,
 			String attribute) throws IOException {
 		encodeAttribute(context, component,
 				getComponentAttributeName(attribute), attribute);
 	}
 
 	/**
 	 * Write html-attribute
 	 * 
 	 * @param writer
 	 * @param attribute
 	 * @param value
 	 * @throws IOException
 	 */
 	public void writeAttribute(ResponseWriter writer, String attribute,
 			Object value) throws IOException {
 		if (shouldRenderAttribute(attribute, value)) {
 			writer.writeAttribute(attribute, value.toString(), attribute);
 		}
 	}
 
 	/**
 	 * @return true if and only if the argument <code>attributeVal</code> is
 	 *         an instance of a wrapper for a primitive type and its value is
 	 *         equal to the default value for that type as given in the spec.
 	 */
 
 	public boolean shouldRenderAttribute(Object attributeVal) {
 		if (null == attributeVal) {
 			return false;
 		} else if (attributeVal instanceof Boolean
 				&& ((Boolean) attributeVal).booleanValue() == Boolean.FALSE
 						.booleanValue()) {
 			return false;
 		} else if (attributeVal.toString().length() == 0) {
 			return false;
 		} else
 			return isValidProperty(attributeVal);
 	}
 	
 	public boolean shouldRenderAttribute(String attributeName, Object attributeVal) {
 		if (!requiredAttributes.contains(attributeName)) {
 			return shouldRenderAttribute(attributeVal);
 		} else {
 			if (null == attributeVal) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Test for valid value of property. by default, for non-setted properties
 	 * with Java primitive types of JSF component return appropriate MIN_VALUE .
 	 * 
 	 * @param property -
 	 *            value of property returned from
 	 *            {@link UIComponent#getAttributes()}
 	 * @return true for setted property, false otherthise.
 	 */
 	public boolean isValidProperty(Object property) {
 		if (null == property) {
 			return false;
 		} else if (property instanceof Integer
 				&& ((Integer) property).intValue() == Integer.MIN_VALUE) {
 			return false;
 		} else if (property instanceof Double
 				&& ((Double) property).doubleValue() == Double.MIN_VALUE) {
 			return false;
 		} else if (property instanceof Character
 				&& ((Character) property).charValue() == Character.MIN_VALUE) {
 			return false;
 		} else if (property instanceof Float
 				&& ((Float) property).floatValue() == Float.MIN_VALUE) {
 			return false;
 		} else if (property instanceof Short
 				&& ((Short) property).shortValue() == Short.MIN_VALUE) {
 			return false;
 		} else if (property instanceof Byte
 				&& ((Byte) property).byteValue() == Byte.MIN_VALUE) {
 			return false;
 		} else if (property instanceof Long
 				&& ((Long) property).longValue() == Long.MIN_VALUE) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Checks if the argument passed in is empty or not.
 	 * Object is empty if it is: <br />
 	 * 	- <code>null<code><br />
 	 * 	- zero-length string<br />
 	 * 	- empty collection<br />
 	 * 	- empty map<br />
 	 * 	- zero-length array<br />
 	 * 
 	 * @param o object to check for emptiness
 	 * @since 3.3.2
 	 * @return <code>true</code> if the argument is empty, <code>false</code> otherwise
 	 */
 	public boolean isEmpty(Object o) {
 		if (null == o) {
 			return true;
 		}
 		if (o instanceof String ) {
 			return (0 == ((String)o).length());
 		}
 		if (o instanceof Collection) {
 			return ((Collection<?>)o).isEmpty();
 		}
 		if (o instanceof Map) {
 			return ((Map<?, ?>)o).isEmpty();
 		}
 		if (o.getClass().isArray()) {
 			return Array.getLength(o) == 0;
 		}
 		return false;
 	}
 	
 	public static enum ScriptHashVariableWrapper {
 		DEFAULT {
 
 			@Override
 			Object wrap(Object o) {
 				return o;
 			}
 			
 		}, 
 		
 		EVENT_HANDLER {
 
 			@Override
 			Object wrap(Object o) {
 				return new JSFunctionDefinition("event").addToBody(o);
 			}
 			
 		};
 		
 		abstract Object wrap(Object o);
 	}
 	
 	/**
 	 * Puts value into map under specified key if the value is not empty and not default. 
 	 * Performs optional value wrapping.
 	 * 
 	 * @param hash
 	 * @param name
 	 * @param value
 	 * @param defaultValue
 	 * @param wrapper
 	 * 
 	 * @since 3.3.2
 	 */
 	public void addToScriptHash(Map<String, Object> hash, 
 			String name, 
 			Object value, 
 			String defaultValue,
 			ScriptHashVariableWrapper wrapper) {
 		
 		ScriptHashVariableWrapper wrapperOrDefault = wrapper != null ? wrapper : ScriptHashVariableWrapper.DEFAULT;
 		
 		if (isValidProperty(value) && !isEmpty(value)) {
 			if (!isEmpty(defaultValue)) {
 				if (!defaultValue.equals(value.toString())) {
 					hash.put(name, wrapperOrDefault.wrap(value));
 				}
 			} else {
 				if (!(value instanceof Boolean) || ((Boolean) value).booleanValue()) {
 					hash.put(name, wrapperOrDefault.wrap(value));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Convert HTML attribute name to component property name.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	protected Object getComponentAttributeName(Object key) {
 		Object converted = substitutions.get(key);
 		if (null == converted) {
 			return key;
 		} else {
 			return converted;
 		}
 	}
 
 	/**
 	 * Convert attribute value to proper object. For known html boolean
 	 * attributes return name for true value, otherthise - null. For non-boolean
 	 * attributes return same value.
 	 * 
 	 * @param name
 	 *            attribute name.
 	 * @param value
 	 * @return
 	 */
 	protected Object attributeValue(String name, Object value) {
 		if (null != value
 				&& Arrays.binarySearch(HTML.PASS_THRU_BOOLEAN, name) >= 0) {
 			boolean checked = false;
 			if (value instanceof Boolean) {
 				checked = ((Boolean) value).booleanValue();
 			} else {
 				if (!(value instanceof String)) {
 					value = value.toString();
 				}
 				checked = Boolean.parseBoolean((String) value);
 			}
 			return checked ? name : null;
 		} else {
 			return value;
 		}
 	}
 
 	/**
 	 * Get boolean value of logical attribute
 	 * 
 	 * @param component
 	 * @param name
 	 *            attribute name
 	 * @return true if attribute is equals Boolean.TRUE or String "true" , false
 	 *         otherwise.
 	 */
 	public boolean isBooleanAttribute(UIComponent component, String name) {
 		Object attrValue = component.getAttributes().get(name);
 		boolean result = false;
 		if (null != attrValue) {
 			if (attrValue instanceof String) {
 				result = "true".equalsIgnoreCase((String) attrValue);
 			} else {
 				result = Boolean.TRUE.equals(attrValue);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Return converted value for {@link javax.faces.component.ValueHolder} as
 	 * String, perform nessesary convertions.
 	 * 
 	 * @param context
 	 * @param component
 	 * @return
 	 */
 	public String getValueAsString(FacesContext context, UIComponent component) {
 		// First - get submitted value for input components
 		if (component instanceof EditableValueHolder) {
 			EditableValueHolder input = (EditableValueHolder) component;
 			String submittedValue = (String) input.getSubmittedValue();
 			if (null != submittedValue) {
 				return submittedValue;
 			}
 		}
 		// If no submitted value presented - convert same for UIInput/UIOutput
 		if (component instanceof ValueHolder) {
 			return formatValue(context, component, ((ValueHolder) component)
 					.getValue());
 		} else {
 			throw new IllegalArgumentException(Messages.getMessage(
 					Messages.CONVERTING_NON_VALUE_HOLDER_COMPONENT_ERROR,
 					component.getId()));
 		}
 	}
 
 	/**
 	 * Convert any object value to string. If component instance of
 	 * {@link ValueHolder } got {@link Converter} for formatting. If not,
 	 * attempt to use converter based on value type.
 	 * 
 	 * @param context
 	 * @param component
 	 * @return
 	 */
 	public String formatValue(FacesContext context, UIComponent component,
 			Object value) {
 		if (value instanceof String) {
 			return (String) value;
 		}
 		Converter converter = null;
 		if (component instanceof ValueHolder) {
 			ValueHolder holder = (ValueHolder) component;
 			converter = holder.getConverter();
 		}
 		if (null == converter && null != value) {
 			try {
 				converter = context.getApplication().createConverter(
 						value.getClass());
 			} catch (FacesException e) {
 				// TODO - log converter exception.
 			}
 		}
 		if (null == converter) {
 			if (null != value) {
 				return value.toString();
 			}
 		} else {
 			return converter.getAsString(context, component, value);
 		}
 		return "";
 	}
 
 	public void encodeDimensions(FacesContext context, UIComponent component,
 			InternetResource resource) throws IOException {
 		if (resource instanceof Java2Dresource) {
 			Java2Dresource j2d = (Java2Dresource) resource;
 			Dimension dim = j2d.getDimensions(context, component);
 			ResponseWriter writer = context.getResponseWriter();
 			writer.writeAttribute("width", String.valueOf(dim.width), "width");
 			writer.writeAttribute("height", String.valueOf(dim.height),
 					"height");
 		}
 	}
 
 	public String encodePx(String value) {
 		return HtmlDimensions.formatPx(HtmlDimensions.decode(value));
 	}
 
 	/**
 	 * formats given value to
 	 * 
 	 * @param value
 	 * @param pattern
 	 * @return
 	 */
 	public String encodePctOrPx(String value) {
 		if (value.indexOf('%') > 0) {
 			return value;
 		} else {
 			return encodePx(value);
 		}
 	}
 
 	/**
 	 * Find nested form for given component
 	 * 
 	 * @param component
 	 * @return nested <code>UIForm</code> component, or <code>null</code>
 	 */
 	public UIForm getNestingForm(FacesContext context, UIComponent component) {
 		UIComponent parent = component.getParent();
 		while (parent != null && !(parent instanceof UIForm)) {
 			parent = parent.getParent();
 		}
 
 		UIForm nestingForm = null;
 		if (parent != null) {
 			// link is nested inside a form
 			nestingForm = (UIForm) parent;
 		}
 		return nestingForm;
 	}
 
 	/**
 	 * @param context
 	 * @param component
 	 * @return
 	 * @throws IOException
 	 */
 	public void encodeBeginFormIfNessesary(FacesContext context,
 			UIComponent component) throws IOException {
 		UIForm form = getNestingForm(context, component);
 		if (null == form) {
 			ResponseWriter writer = context.getResponseWriter();
 			String clientId = component.getClientId(context) + DUMMY_FORM_ID;
 			encodeBeginForm(context, component, writer, clientId);
 			// writer.writeAttribute(HTML.style_ATTRIBUTE, "margin:0;
 			// padding:0;", null);
 		}
 	}
 
 	/**
 	 * @param context
 	 * @param component
 	 * @param writer
 	 * @param clientId
 	 * @throws IOException
 	 */
 	public void encodeBeginForm(FacesContext context, UIComponent component,
 			ResponseWriter writer, String clientId) throws IOException {
 		String actionURL = getActionUrl(context);
 		String encodeActionURL = context.getExternalContext().encodeActionURL(
 				actionURL);
 
 		writer.startElement(HTML.FORM_ELEMENT, component);
 		writer.writeAttribute(HTML.id_ATTRIBUTE, clientId, null);
 		writer.writeAttribute(HTML.NAME_ATTRIBUTE, clientId, null);
 		writer.writeAttribute(HTML.METHOD_ATTRIBUTE, "post", null);
 		writer.writeAttribute(HTML.style_ATTRIBUTE, "margin:0; padding:0; display: inline;",
 				null);
 		writer.writeURIAttribute(HTML.ACTION_ATTRIBUTE, encodeActionURL,
 				"action");
 	}
 
 	/**
 	 * @param context
 	 * @param component
 	 * @throws IOException
 	 */
 	public void encodeEndFormIfNessesary(FacesContext context,
 			UIComponent component) throws IOException {
 		UIForm form = getNestingForm(context, component);
 		if (null == form) {
 			ResponseWriter writer = context.getResponseWriter();
 			// TODO - hidden form parameters ?
 			encodeEndForm(context, writer);
 		}
 	}
 
 	/**
 	 * @param context
 	 * @param writer
 	 * @throws IOException
 	 */
 	public void encodeEndForm(FacesContext context, ResponseWriter writer)
 			throws IOException {
 		AjaxRendererUtils.writeState(context);
 		writer.endElement(HTML.FORM_ELEMENT);
 	}
 
 	/**
 	 * @param facesContext
 	 * @return String A String representing the action URL
 	 */
 	public String getActionUrl(FacesContext facesContext) {
 		ViewHandler viewHandler = facesContext.getApplication()
 				.getViewHandler();
 		String viewId = facesContext.getViewRoot().getViewId();
 		return viewHandler.getActionURL(facesContext, viewId);
 	}
 
 	/**
 	 * 
 	 * @param context
 	 * @param value
 	 * @return URL of target resource (src, href, etc)
 	 */
 	public String encodeResourceURL(TemplateContext context, Object value) {
 		if (value == null) {
 			return "";
 		}
 		FacesContext facesContext = context.getFacesContext();
 		value = facesContext.getApplication().getViewHandler().getResourceURL(
 				facesContext, value.toString());
 		return facesContext.getExternalContext().encodeResourceURL(
 				(String) value);
 	}
 
 	/**
 	 * Simplified version of {@link encodeId}
 	 * 
 	 * @param context
 	 * @param component
 	 * @return client id of current component
 	 */
 	public String clientId(FacesContext context, UIComponent component) {
 		String clientId = "";
 		try {
 			clientId = component.getClientId(context);
 		} catch (Exception e) {
 			// just ignore
 		}
 		return clientId;
 	}
 
 	/**
 	 * Wtrie JavaScript with start/end elements and type.
 	 * 
 	 * @param context
 	 * @param tab
 	 * @param string
 	 */
 
 	public void writeScript(FacesContext context, UIComponent component,
 			Object script) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		writer.startElement(HTML.SCRIPT_ELEM, component);
 		writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", "type");
 		writer.writeText(script, null);
 		writer.endElement(HTML.SCRIPT_ELEM);
 	}
 
 
 	public UIComponent findComponentFor(FacesContext context,
 		UIComponent component, String id) {
 	    	return findComponentFor(component,id);
 	}
 
 	/**
 	 * @param component
 	 * @param id
 	 * @return
 	 */
 	public UIComponent findComponentFor(UIComponent component,
 			String id) {
 	    
 	    	if (id == null) {
 	    	    throw new NullPointerException("id is null!");
 	    	}
 	    	
 	    	if (id.length() == 0) {
 	    	    return null;
 	    	}
 	    
 		UIComponent target = null;
 		UIComponent parent = component;
 		UIComponent root = component;
 		while (null == target && null != parent) {
 			target = parent.findComponent(id);
 			root = parent;
 			parent = parent.getParent();
 		}
 		if (null == target) {
 			target = findUIComponentBelow(root, id);
 		}
 		return target;
 	}
 	
 	/**
 	 * If target component contains generated id and for doesn't, correct for id 
 	 * @param forAttr
 	 * @param target
 	 * 
 	 */
 	public String correctForIdReference(String forAttr, UIComponent component) {
 		
 		int contains = forAttr.indexOf(UIViewRoot.UNIQUE_ID_PREFIX);
 		if (contains <= 0) {
 			String id = component.getId();
 			int pos = id.indexOf(UIViewRoot.UNIQUE_ID_PREFIX); 
 			if ( pos > 0) {
 				forAttr = forAttr.concat(id.substring(pos));
 			}
 		}
 		return forAttr;
 	}
 
 	private UIComponent findUIComponentBelow(UIComponent root, String id) {
 		UIComponent target = null;
 		for (Iterator<UIComponent> iter = root.getFacetsAndChildren(); iter.hasNext();) {
 			UIComponent child = (UIComponent) iter.next();
 			if (child instanceof NamingContainer) {
 				try {
 					target = child.findComponent(id);
 				} catch (IllegalArgumentException iae) {
 					continue;
 				}
 			}
 			if (target == null) {
 				if (child.getChildCount() > 0 || child.getFacetCount() > 0) {
 					target = findUIComponentBelow(child, id);
 				}
 			}
 
 			if (target != null) {
 				break;
 			}
 
 		}
 		return target;
 	}
 	
 	public static void writeEventHandlerFunction(FacesContext context,
 			UIComponent component, String eventName) throws IOException {
 
 		ResponseWriter writer = context.getResponseWriter();
 		Object script = component.getAttributes().get(eventName);
 		if (script != null && !script.equals("")) {
 			JSFunctionDefinition onEventDefinition = new JSFunctionDefinition();
 			onEventDefinition.addParameter("event");
 			onEventDefinition.addToBody(script);
 			writer.writeText(eventName + ": "
 					+ onEventDefinition.toScript(), null);
 		}else{
 			writer.writeText(eventName + ": ''", null);
 		}
 	}
 	
 	public JSFunctionDefinition getAsEventHandler(FacesContext context, UIComponent component, String attributeName, String append) {
 		String event = (String) component.getAttributes().get(attributeName);
 				
 		if (event != null) {
 			event = event.trim();
 		
 			if (event.length() != 0) {
 				JSFunctionDefinition function = new JSFunctionDefinition();
 				function.addParameter("event");
 				if(null!=append && append.length()>0){
 					function.addToBody(event+append);
 				}else{
 					function.addToBody(event);
 				}
 			return  function;
 			}
 		}
 
 		return null;
 	}
 	
 	public String escapeJavaScript(Object o) {
 		if (o != null) {
 			StringBuilder result = new StringBuilder();
 			JSEncoder encoder = new JSEncoder();
 
 			char chars[] = o.toString().toCharArray();
             int start = 0;
             int end = chars.length;
 			for (int x = start; x < end; x++) {
                 char c = chars[x];
                 
                 if (encoder.compile(c)) {
                     continue;
                 }
                 
                 if (start != x) {
                 	result.append(chars, start, x - start );
                 }
                 
                 result.append(encoder.encode(c));
                 start = x + 1;
                 
                 continue;
             }
 			
             if (start != end) {
             	result.append(chars, start, end - start);
             }
 			
             return result.toString();
 		} else {
 			return null;
 		}
 	}
 }
