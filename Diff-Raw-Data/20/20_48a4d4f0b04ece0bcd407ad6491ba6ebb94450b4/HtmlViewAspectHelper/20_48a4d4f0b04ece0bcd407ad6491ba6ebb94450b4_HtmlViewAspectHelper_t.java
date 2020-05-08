 /*
  * Copyright 2006 Giordano Maestro (giordano.maestro--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.romaframework.aspect.view.html;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.romaframework.aspect.session.SessionAspect;
 import org.romaframework.aspect.view.SelectionMode;
 import org.romaframework.aspect.view.ViewAspect;
 import org.romaframework.aspect.view.ViewConstants;
 import org.romaframework.aspect.view.area.AreaComponent;
 import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.feature.ViewClassFeatures;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.aspect.view.form.ViewComponent;
 import org.romaframework.aspect.view.html.area.HtmlViewFormArea;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenArea;
 import org.romaframework.aspect.view.html.area.mode.HtmlViewAreaMode;
 import org.romaframework.aspect.view.html.constants.RequestConstants;
 import org.romaframework.aspect.view.html.css.StyleBuffer;
 import org.romaframework.aspect.view.html.exception.DefaultJspTemplateNotFoundException;
 import org.romaframework.aspect.view.html.http.MockHttpServletResponse;
 import org.romaframework.aspect.view.html.screen.HtmlViewBasicScreen;
 import org.romaframework.aspect.view.html.screen.HtmlViewScreen;
 import org.romaframework.aspect.view.html.transformer.helper.TransformerHelper;
 import org.romaframework.core.Roma;
 import org.romaframework.core.config.RomaApplicationContext;
 import org.romaframework.core.flow.ObjectContext;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaClassElement;
 import org.romaframework.core.schema.SchemaFeatures;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.web.session.HttpAbstractSessionAspect;
 
 public class HtmlViewAspectHelper {
 
 	/**
 	 * The parameter name used to put an object in the session
 	 */
 	private final static String	VA_SESSION	= "$#$HtmlViewVASession$#$";
 	public static final String	REDIRECTED	= "$#$REDIRECTED$#$";
	public static final String	POJO_NAME		= "pojo";
 
 	/**
 	 * Return the HttpRequest
 	 * 
 	 * @return
 	 */
 	public static ServletRequest getServletRequest() {
 		return ObjectContext.getInstance().getContextComponent(HttpAbstractSessionAspect.CONTEXT_REQUEST_PAR);
 	}
 
 	/**
 	 * Return the HttpResponse
 	 * 
 	 * @return
 	 */
 	public static ServletResponse getServletResponse() {
 		return ObjectContext.getInstance().getContextComponent(HttpAbstractSessionAspect.CONTEXT_RESPONSE_PAR);
 	}
 
 	/**
 	 * Return the the html view aspect session object
 	 * 
 	 * @return
 	 */
 	public static HtmlViewSession getHtmlViewSession() {
 		final SessionAspect sessionAspect = Roma.session();
 		HtmlViewSession result = (HtmlViewSession) sessionAspect.getProperty(VA_SESSION);
 		if (result == null) {
 			result = new HtmlViewSession();
 			sessionAspect.setProperty(VA_SESSION, result);
 		}
 		return result;
 	}
 
 	/**
 	 * Destroy the current html view aspect session
 	 */
 	public static void destroyHtmlViewSession() {
 		final SessionAspect sessionAspect = Roma.session();
 		if (sessionAspect.getActiveSessionInfo() == null) {
 			return;
 		}
 		final HtmlViewSession result = (HtmlViewSession) sessionAspect.getProperty(VA_SESSION);
 		if (result != null) {
 			sessionAspect.setProperty(VA_SESSION, null);
 		}
 	}
 
 	/**
 	 * Get the configure label of an element
 	 * 
 	 * @param iFeatures
 	 *          the schema feature
 	 * @return the label of the element
 	 */
 	public static String getLabel(final SchemaFeatures iFeatures) {
 		String fieldRender;
 		if (iFeatures instanceof SchemaField) {
 			fieldRender = iFeatures.getFeature(ViewFieldFeatures.LABEL);
 		} else {
 			fieldRender = iFeatures.getFeature(ViewActionFeatures.LABEL);
 		}
 		return fieldRender;
 	}
 
 	/**
 	 * Get the configure label of an element
 	 * 
 	 * @param iFeatures
 	 *          the schema feature
 	 * @return the label of the element
 	 */
 	public static String getHint(final SchemaFeatures iFeatures) {
 		String fieldRender;
 		if (iFeatures instanceof SchemaField) {
 			fieldRender = iFeatures.getFeature(ViewFieldFeatures.DESCRIPTION);
 		} else {
 			fieldRender = iFeatures.getFeature(ViewActionFeatures.DESCRIPTION);
 		}
 		return fieldRender;
 	}
 
 	/**
 	 * Return the default render type to use for a component or a primitive java type
 	 * 
 	 * @param schemaFeatures
 	 *          the schema feature of the element
 	 * @return the string
 	 */
 	public static String getDefaultRenderType(final SchemaFeatures schemaFeatures) {
 
		String result = null;
 		if (schemaFeatures instanceof SchemaField) {
 			result = schemaFeatures.getFeature(ViewFieldFeatures.RENDER);
		} else if (schemaFeatures instanceof SchemaAction) {
 			result = schemaFeatures.getFeature(ViewActionFeatures.RENDER);
 		}
 
 		if (result != null) {
 			if (result.equals(ViewConstants.LAYOUT_POPUP)) {
 				return HtmlViewAspectHelper.POJO_NAME;
 			}
 			return result;
 		}
 
 		if (schemaFeatures instanceof SchemaClassDefinition) {
 			return getRender(((SchemaClassDefinition) schemaFeatures).getName(), HtmlViewAspectHelper.POJO_NAME);
 		} else if (schemaFeatures instanceof SchemaAction) {
 			return ViewConstants.RENDER_BUTTON;
 		} else {
 			final SchemaField schemaField = (SchemaField) schemaFeatures;
 			Class<?> fieldType = (Class<?>) schemaField.getLanguageType();
 			String render = getHtmlViewAspect().getRender(fieldType.getName());
 			if (render != null)
 				return render;
 			render = getHtmlViewAspect().getRender(fieldType.getSimpleName());
 			if (render != null)
 				return render;
 			if (String.class.isAssignableFrom(fieldType)) {
 				return getRender(String.class.getSimpleName(), ViewConstants.RENDER_TEXT);
 			}
 
 			if (Date.class.isAssignableFrom(fieldType)) {
 				return getRender(Date.class.getSimpleName(), ViewConstants.RENDER_DATE);
 			}
 
 			if (Long.class.isAssignableFrom(fieldType) || long.class.isAssignableFrom(fieldType)) {
 				return getRender(Long.class.getSimpleName(), ViewConstants.RENDER_NUMBER);
 			}
 			if (Integer.class.isAssignableFrom(fieldType) || int.class.isAssignableFrom(fieldType)) {
 				return getRender(Integer.class.getSimpleName(), ViewConstants.RENDER_NUMBER);
 			}
 			if (Short.class.isAssignableFrom(fieldType) || short.class.isAssignableFrom(fieldType)) {
 				return getRender(Short.class.getSimpleName(), ViewConstants.RENDER_NUMBER);
 			}
 			if (Float.class.isAssignableFrom(fieldType) || float.class.isAssignableFrom(fieldType)) {
 				return getRender(Float.class.getSimpleName(), ViewConstants.RENDER_DECIMAL);
 			}
 			if (Double.class.isAssignableFrom(fieldType) || double.class.isAssignableFrom(fieldType)) {
 				return getRender(Double.class.getSimpleName(), ViewConstants.RENDER_DECIMAL);
 			}
 			if (Number.class.isAssignableFrom(fieldType)) {
 				return getRender(Number.class.getSimpleName(), ViewConstants.RENDER_DECIMAL);
 			}
 			if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
 				return getRender(Boolean.class.getSimpleName(), ViewConstants.RENDER_CHECK);
 			}
 
 			if (List.class.isAssignableFrom(fieldType)) {
 				return getRender(List.class.getSimpleName(), ViewConstants.RENDER_LIST);
 			}
 
 			if (Collection.class.isAssignableFrom(fieldType)) {
 				return getRender(Collection.class.getSimpleName(), ViewConstants.RENDER_LIST);
 			}
 
 			if (fieldType.isArray()) {
 				return ViewConstants.RENDER_LIST;
 			}
 			render = (String) schemaField.getType().getFeature(ViewClassFeatures.RENDER);
 			if (render != null)
 				return render;
 			return ViewConstants.RENDER_OBJECTLINK;
 		}
 	}
 
 	private static String getRender(String type, String def) {
 		String render = getHtmlViewAspect().getRender(type);
 		return render != null ? render : def;
 	}
 
 	public static HtmlViewAspect getHtmlViewAspect() {
 		return ((HtmlViewAspect) Roma.view());
 	}
 
 	/**
 	 * Render a component searching for a jsp template
 	 * 
 	 * @param component
 	 *          The component to be rendered
 	 * @param request
 	 *          the request
 	 * @param response
 	 *          the response
 	 * @throws ServletException
 	 *           {@link ServletException}
 	 * @throws IOException
 	 *           {@link IOException}
 	 */
	public static void renderByJsp(final ViewComponent component, final ServletRequest request, Writer writer)
			throws ServletException, IOException {
 		final Object obj = component.getContent();
 		if (obj != null) {
 			final Class<?> clazz = obj.getClass();
 			final String classJsp = getJspForClass(clazz);
 			final Object previousFormInRequest = request.getAttribute(RequestConstants.CURRENT_REQUEST_FORM);
 			request.setAttribute(RequestConstants.CURRENT_REQUEST_FORM, component);
 			getHtmlFromJSP(request, classJsp, writer);
 			request.setAttribute(RequestConstants.CURRENT_REQUEST_FORM, previousFormInRequest);
 		}
 	}
 
 	/**
 	 * Render a component searching for a jsp template
 	 * 
 	 * @param component
 	 *          The component to be rendered
 	 * @throws ServletException
 	 *           {@link ServletException}
 	 * @throws IOException
 	 *           {@link IOException}
 	 */
 	public static void renderByJsp(final ViewComponent component, Writer writer) throws ServletException, IOException {
 		renderByJsp(component, getServletRequest(), writer);
 	}
 
 	/**
 	 * Search the jsp template for a class
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	private static String getJspForClass(final Class<?> clazz) {
 		if (existsJspForClass(clazz)) {
 			return calculateJspPath(clazz);
 		}
 		if (Object.class.equals(clazz)) {
 			throw new DefaultJspTemplateNotFoundException();
 		}
 		return getJspForClass(clazz.getSuperclass());
 	}
 
 	private static boolean existsJspForClass(final Class<?> clazz) {
 		final URL url = RomaApplicationContext.getResourceAccessor().getResource(calculateJspPath(clazz));
 		if (url != null) {
 			return true;
 		}
 		return false;
 	}
 
 	private static String calculateJspPath(final Class<?> clazz) {
 		return RequestConstants.JSP_PATH + clazz.getSimpleName() + ".jsp";
 	}
 
 	public static StyleBuffer getCssBuffer() {
 		StyleBuffer buffer = Roma.context().component(StyleBuffer.class);
 		if (buffer == null) {
 			final HtmlViewSession session = getHtmlViewSession();
 			buffer = session.getCssBuffer(getCurrentPageId());
 			Roma.context().setComponent(StyleBuffer.class, buffer);
 		}
 		return buffer;
 	}
 
 	public static HtmlViewCodeBuffer getJsBuffer() {
 		HtmlViewCodeBuffer buffer = Roma.context().component(HtmlViewCodeBuffer.class);
 		if (buffer == null) {
 			buffer = new HtmlViewJQueryBuffer();
 			createJsBufer(buffer);
 		}
 		return buffer;
 	}
 
 	public static void createJsBufer(HtmlViewCodeBuffer buff) {
 		Roma.context().setComponent(HtmlViewCodeBuffer.class, buff);
 	}
 
 	public static void appendToJsBuffer(String id, final String string) {
 		// getJsBuffer(getCurrentPageId()).append(string + "\n");
 		getJsBuffer().setScript(id, string + "\n");
 	}
 
 	public static void removeCssBuffer() {
 		Roma.context().setComponent(StyleBuffer.class, null);
 	}
 
 	public static void removeJsBuffer() {
 		Roma.context().setComponent(HtmlViewCodeBuffer.class, null);
 	}
 
 	private static long	lastPageId	= 0;
 
 	public static String newPageId() {
 		return "" + ++lastPageId;
 	}
 
 	public static String getCurrentPageId() {
 		return (String) getServletRequest().getAttribute(HtmlServlet.PAGE_ID_PARAM);
 	}
 
 	public static Object parseToRightType(final Object value, final SchemaField schemaField) {
 		if (value == null) {
 			final Class<?> type = schemaField.getValue(value).getClass();
 			if (type.equals(short.class)) {
 				return (short) 0;
 			}
 			if (type.equals(int.class)) {
 				return (int) 0;
 			}
 			if (type.equals(long.class)) {
 				return (long) 0;
 			}
 			if (type.equals(float.class)) {
 				return (float) 0;
 			}
 			if (type.equals(double.class)) {
 				return (double) 0;
 			}
 			if (type.equals(boolean.class)) {
 				return false;
 			}
 			return value;
 		}
 		final Class<?> type = schemaField.getValue(value).getClass();
 		if (type.equals(String.class)) {
 			return value;
 		}
 		if (Short.class.equals(type) || short.class.equals(type)) {
 			return Short.parseShort("" + value);
 		}
 		if (Integer.class.isAssignableFrom(type) || int.class.equals(type)) {
 			return Integer.parseInt("" + value);
 		}
 		if (Long.class.isAssignableFrom(type) || long.class.equals(type)) {
 			return Long.parseLong("" + value);
 		}
 		if (Float.class.isAssignableFrom(type) || float.class.equals(type)) {
 			return Float.parseFloat("" + value);
 		}
 		if (Double.class.isAssignableFrom(type) || double.class.equals(type)) {
 			return Double.parseDouble("" + value);
 		}
 		return value;
 	}
 
 	public static String getPopupsScreenAreaId() {
 		final HtmlViewScreen screen = (HtmlViewScreen) Roma.aspect(ViewAspect.class).getScreen();
 		final HtmlViewAreaMode mode = (HtmlViewAreaMode) ((HtmlViewScreenArea) screen.getPopupsScreenArea()).getAreaMode();
 		return TransformerHelper.getInstance().getHtmlId(mode, null);
 	}
 
	public static AreaComponent searchAreaForRendering(final String featureLayout, final SchemaClassElement iField,
			final HtmlViewFormArea rootArea) {
 		AreaComponent areaForRendering = null;
 		// Search for a defined area
 		if (featureLayout != null && featureLayout.startsWith("form:")) {
 			String fieldName = featureLayout.split(":")[1];
 			if (fieldName.equals(rootArea.getName())) {
 				return rootArea;
 			}
 			areaForRendering = rootArea.searchArea(fieldName);
 			if (areaForRendering == null) {
 				// TODO add a warning or an error
 			}
 			return areaForRendering;
 		}
 
 		if (featureLayout != null && featureLayout.startsWith("screen:")) {
 			final HtmlViewBasicScreen screen = (HtmlViewBasicScreen) Roma.aspect(ViewAspect.class).getScreen();
 			areaForRendering = screen.getArea(featureLayout.split(":")[1]);
 			if (areaForRendering == null) {
 				// TODO add a warning or an error
 			}
 			return areaForRendering;
 		}
 
 		// Search by convention on the element name
 		final String elementName = iField.getName();
 		if (elementName.equals(rootArea.getName())) {
 			return rootArea;
 		}
 		areaForRendering = rootArea.searchArea("//" + elementName);
 		if (areaForRendering != null) {
 			return areaForRendering;
 		}
 
 		// Return the default area
 
 		if (iField instanceof SchemaField) {
 			return rootArea.searchArea("//" + "fields");
 		} else if (iField instanceof SchemaAction) {
 			return rootArea.searchArea("//" + "actions");
 		}
 
 		// TODO error?
 		return null;
 	}
 
 	public static boolean isSelected(final ViewComponent contentComponent, final Object element, final Integer index) {
 		try {
 			final ViewComponent parent = (ViewComponent) contentComponent.getContainerComponent();
 			final String selectionFieldName = (String) contentComponent.getSchemaField().getFeature(ViewFieldFeatures.SELECTION_FIELD);
 			if (selectionFieldName == null || selectionFieldName.equals("")) {
 				return false;
 			}
 			final SchemaField fieldSelection = parent.getSchemaObject().getField(selectionFieldName);
 			if (fieldSelection == null) {
 				return false;
 			}
 			final Object selection = fieldSelection.getValue(parent.getContent());
 			if (selection == null) {
 				return false;
 			}
 			SelectionMode selectionMode = contentComponent.getSchemaField().getFeature(ViewFieldFeatures.SELECTION_MODE);
 			if (selectionMode == SelectionMode.SELECTION_MODE_INDEX) {
 				// index selection mode
 				if (index.equals(selection)) {
 					return true;
 				}
 				if (selection instanceof Collection<?> && ((Collection<?>) selection).contains(index)) {
 					return true;
 				}
 				if (selection instanceof Object[]) {
 					for (final Object item : (Object[]) selection) {
 						if (index.equals(item)) {
 							return true;
 						}
 					}
 				}
 			} else {
 				// value selection mode
 				if (element.equals(selection)) {
 					return true;
 				}
 				if (selection instanceof Collection<?> && ((Collection<?>) selection).contains(element)) {
 					return true;
 				}
 				if (selection instanceof Object[]) {
 					for (final Object item : (Object[]) selection) {
 						if (element.equals(item)) {
 							return true;
 						}
 					}
 				}
 			}
 		} catch (final Exception e) {
 		}
 		return false;
 	}
 
	public static void getHtmlFromJSP(final ServletRequest request, String jspUrl, Writer writer) throws ServletException,
			IOException {
 		MockHttpServletResponse response = new MockHttpServletResponse(writer);
 		request.getRequestDispatcher(jspUrl).include(request, response);
 	}
 
 }
