 package org.romaframework.aspect.view.html.transformer.helper;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.romaframework.aspect.i18n.I18NType;
 import org.romaframework.aspect.validation.feature.ValidationFieldFeatures;
 import org.romaframework.aspect.view.FormatHelper;
 import org.romaframework.aspect.view.area.AreaComponent;
 import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.feature.ViewBaseFeatures;
 import org.romaframework.aspect.view.feature.ViewClassFeatures;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.aspect.view.html.actionhandler.EventHelper;
 import org.romaframework.aspect.view.html.area.HtmlViewArea;
 import org.romaframework.aspect.view.html.area.HtmlViewFormArea;
 import org.romaframework.aspect.view.html.area.HtmlViewFormAreaInstance;
 import org.romaframework.aspect.view.html.area.HtmlViewRenderable;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenArea;
 import org.romaframework.aspect.view.html.component.HtmlViewAbstractComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewAbstractContentComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewActionComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewComposedComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.component.HtmlViewContentComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewContentComponentImpl;
 import org.romaframework.aspect.view.html.component.HtmlViewGenericComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewInvisibleContentComponent;
 import org.romaframework.aspect.view.html.component.composed.list.HtmlViewCollectionComposedComponent;
 import org.romaframework.aspect.view.html.transformer.Transformer;
 import org.romaframework.aspect.view.html.transformer.freemarker.Griddable;
 import org.romaframework.core.Roma;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassElement;
 import org.romaframework.core.schema.SchemaElement;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaObject;
 import org.romaframework.web.session.HttpAbstractSessionAspect;
 import org.romaframework.web.view.HttpUtils;
 
 public class JaniculumWrapper {
 	private static long																				counter			= 0;
 	private static final ArrayList<HtmlViewGenericComponent>	EMPTY_LIST	= new ArrayList<HtmlViewGenericComponent>();
 	private static TransformerHelper													helper			= TransformerHelper.getInstance();
 
 	public JaniculumWrapper(Transformer transformer, HtmlViewRenderable component, String styles) {
 		// TODO manage style configurations
 	}
 
 	public static String id(HtmlViewRenderable component, String part) {
 		return helper.getHtmlId(component, part);
 	}
 
 	public static Long progressiveLong(HtmlViewRenderable component) {
 		return counter++;
 	}
 
 	public static HtmlViewGenericComponent getContainerComponent(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewGenericComponent) {
 			return (HtmlViewGenericComponent) ((HtmlViewGenericComponent) component).getContainerComponent();
 		} else if (component instanceof AreaComponent) {
 			return (HtmlViewGenericComponent) ((AreaComponent) component).getParent();
 		}
 		return null;
 	}
 
 	public static Collection<?> getChildren(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewGenericComponent) {
 			return ((HtmlViewGenericComponent) component).getChildren();
 		} else if (component instanceof HtmlViewArea && ((HtmlViewArea) component).getComponents() != null) {
 			return ((HtmlViewArea) component).getComponents();
 		}
 		return EMPTY_LIST;
 	}
 
 	public static boolean haveChildren(HtmlViewRenderable component) {
 		Collection<?> children = JaniculumWrapper.getChildren(component);
 		if (children != null && children.size() > 0)
 			return true;
 		else
 			return false;
 	}
 
 	public static Object content(HtmlViewRenderable component) {
 		return content(component, false);
 	}
 
 	public static Object content(HtmlViewRenderable component, boolean quoteDblquotes) {
 		if (component instanceof HtmlViewContentComponent) {
 			Object result = ((HtmlViewContentComponent) component).getContent();
 			if (result != null && result instanceof String && quoteDblquotes) {
 				result = ((String) result).replaceAll("\"", "&quot;");
 			}
 			return result;
 		}
 		return null;
 	}
 
 	public static Object formattedContent(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewContentComponent) {
 			SchemaField field = ((HtmlViewContentComponent) component).getSchemaField();
 			Object content = ((HtmlViewContentComponent) component).getContent();
 			return FormatHelper.format(content, field, true);
 		}
 		return null;
 	}
 
 	public static String contentAsString(HtmlViewRenderable component) {
 		Object content = content(component);
 		if (content == null) {
 			return "";
 		}
 		return content.toString();
 	}
 
 	public static String imageLabel(HtmlViewRenderable component) {
 		SchemaClassElement schema = getSchemaElement(component);
 		String result = null;
 		if (schema instanceof SchemaField) {
 			Object content = content(component);
 			if (content != null) {
 				result = content.toString().replaceAll("\\$", "");
 			}
 		} else {
 
 			result = ((String) ((HtmlViewGenericComponent) component).getSchemaElement().getFeature(ViewBaseFeatures.LABEL)).replaceAll("\\$", "");
 
 		}
 		return result;
 	}
 
 	public static String cssClass(HtmlViewRenderable component, String transformerName, String part) {
 		return cssSpecificClass(component, transformerName, part);
 	}
 
 	public static String cssSpecificClass(HtmlViewRenderable thisComponent, String transformerName, String part) {
 		return helper.getHtmlClass(transformerName, part, thisComponent);
 	}
 
 	public static String tableRowCssClass(HtmlViewRenderable component, String transformerName, int rowIndex) {
 		return tableRowCssSpecificClass(component, transformerName, rowIndex);
 	}
 
 	public static String tableRowCssSpecificClass(HtmlViewRenderable thisComponent, String transformerName, int rowIndex) {
 		if (thisComponent instanceof HtmlViewCollectionComposedComponent) {
 			HtmlViewCollectionComposedComponent tableComponent = (HtmlViewCollectionComposedComponent) thisComponent;
 			if (tableComponent.getChildren().size() > rowIndex) {
 				HtmlViewGenericComponent rowComponent = new ArrayList<HtmlViewGenericComponent>(tableComponent.getChildren()).get(rowIndex);
 				if (rowComponent.getSchemaObject() != null) {
 					Object feature = rowComponent.getSchemaObject().getFeature(ViewClassFeatures.STYLE);
 					if (feature != null && !feature.toString().isEmpty() && feature.toString().charAt(0) != '{') {
 						// CLASS NAME: USE IT
 						return feature.toString();
 					}
 				}
 			}
 		}
 		return cssClass(thisComponent, transformerName, "body_row");
 	}
 
 	public static String inlineStyle(HtmlViewRenderable component, String part) {
 		if (component instanceof HtmlViewGenericComponent) {
 			SchemaField schemaField = ((HtmlViewGenericComponent) component).getSchemaField();
 			if (schemaField != null) {
 				Object feature = schemaField.getFeature(ViewFieldFeatures.STYLE);
 				if (feature != null) {
 					String style = ((String) feature).trim();
 					if (style.isEmpty() || style.charAt(0) != '{')
 						return "";
 
 					return style.substring(1, style.length() - 2);
 				}
 			}
 		} else if (component instanceof HtmlViewFormAreaInstance) {
 			HtmlViewFormAreaInstance instance = (HtmlViewFormAreaInstance) component;
 			if (instance.getAreaStyle() != null) {
 				String style = instance.getAreaStyle();
 				if (style.isEmpty() || style.charAt(0) != '{')
 					return "";
 				return style.substring(1, style.length() - 2);
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * for form areas
 	 * 
 	 * @return
 	 */
 	public static boolean isLabelRendered(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewConfigurableEntityForm) {
 			HtmlViewFormArea area = ((HtmlViewConfigurableEntityForm) component).getAreaForComponentPlacement();
 			return !"placeholder".equals(((HtmlViewFormAreaInstance) area).getType());
 		}
 		if (component instanceof HtmlViewScreenArea) {
 			return false;
 		}
 		return true;
 	}
 
 	public static String i18NLabel(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewGenericComponent) {
 			if (((HtmlViewGenericComponent) component).getSchemaElement() != null) {
 				SchemaElement element = ((HtmlViewGenericComponent) component).getSchemaElement();
 				if (element instanceof SchemaField)
 					return Roma.i18n().get(element, I18NType.LABEL, ViewFieldFeatures.LABEL);
 				else if (element instanceof SchemaAction)
 					return Roma.i18n().get(element, I18NType.LABEL, ViewActionFeatures.LABEL);
 			}
 		} else if (component instanceof HtmlViewFormArea) {
 			if (((HtmlViewFormArea) component).getComponents() != null && ((HtmlViewFormArea) component).getComponents().size() == 1
 					&& !(((HtmlViewFormArea) component).getComponents().get(0) instanceof HtmlViewInvisibleContentComponent)) {
 				return i18NLabel(((HtmlViewFormArea) component).getComponents().get(0));
 			}
 		}
 		return "";
 	}
 
 	public static String i18NObjectLabel(HtmlViewRenderable component) {
 		SchemaObject object = ((HtmlViewGenericComponent) component).getSchemaObject();
 		return Roma.i18n().get(object, I18NType.LABEL, ViewClassFeatures.LABEL);
 	}
 
 	public static String i18NHint(HtmlViewRenderable component) {
 		if (((HtmlViewGenericComponent) component).getSchemaElement() != null) {
 			SchemaElement element = ((HtmlViewGenericComponent) component).getSchemaElement();
 			if (element instanceof SchemaField)
 				return Roma.i18n().get(element, I18NType.HINT, ViewFieldFeatures.DESCRIPTION);
 			else if (element instanceof SchemaAction)
 				return Roma.i18n().get(element, I18NType.HINT, ViewActionFeatures.DESCRIPTION);
 		}
 		return "";
 	}
 
 	@Deprecated
 	public static String i18N(HtmlViewRenderable component, String string) {
 		String text = Roma.i18n().get(string);
 		if (text == null) {
 			text = "";
 			if (string.endsWith(".label")) {
 				String[] toParse = string.split("\\.");
 				String tempText = toParse[toParse.length - 2];
 				for (int i = 0; i < tempText.length(); i++) {
 					if (i == 0) {
 						text = "" + tempText.charAt(i);
 						text = text.toUpperCase();
 					} else {
 						if (Character.isUpperCase(tempText.charAt(i)))
 							text = text + " " + tempText.charAt(i);
 						else
 							text = text + tempText.charAt(i);
 					}
 				}
 
 			} else
 				text = string;
 		}
 		return text;
 	}
 
 	public static boolean getField(HtmlViewRenderable component) {
 		return isField(component);
 	}
 
 	public static boolean isField(HtmlViewRenderable component) {
 		SchemaClassElement el = getSchemaElement(component);
 		if (el instanceof SchemaField) {
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isAction(HtmlViewRenderable component) {
 		SchemaClassElement el = getSchemaElement(component);
 		if (el instanceof SchemaAction) {
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isHidden(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewInvisibleContentComponent) {
 			return true;
 		}
 		return false;
 	}
 
 	@Deprecated
 	public static boolean isAction(HtmlViewAbstractComponent component) {
 		return (component instanceof HtmlViewActionComponent);
 	}
 
 	private static SchemaClassElement getSchemaElement(HtmlViewRenderable component) {
 		SchemaClassElement el = ((HtmlViewGenericComponent) component).getSchemaElement();
 		return el;
 	}
 
 	public static String actionName(HtmlViewRenderable component) {
 		HtmlViewGenericComponent actionComponent = (HtmlViewGenericComponent) component;
 
 		return TransformerHelper.POJO_ACTION_PREFIX + TransformerHelper.SEPARATOR + actionComponent.getId() + TransformerHelper.SEPARATOR
 				+ actionComponent.getSchemaElement().getName() + TransformerHelper.SEPARATOR + actionComponent.getScreenArea();
 	}
 
 	public static String event(HtmlViewRenderable component, String event) {
 		HtmlViewGenericComponent actionComponent = (HtmlViewGenericComponent) component;
 		return EventHelper.getEventHtmlName(actionComponent, event);
 		// return "(PojoAction)_" + actionComponent.getId() + TransformerHelper.SEPARATOR + elementName(event)
 		// + TransformerHelper.SEPARATOR + actionComponent.getScreenArea();
 	}
 
 	public static Set<String> getAvailableEvents(HtmlViewRenderable component) {
 		return availableEvents(component);
 	}
 
 	public static Set<String> availableEvents(HtmlViewRenderable component) {
 		Set<String> result = new HashSet<String>();
 		SchemaClassElement element = getSchemaElement(component);
 		if (element instanceof SchemaField) {
 			Set<String> standardEvents = Roma.component(EventHelper.class).getStandardEvents();
 			Iterator<SchemaEvent> eventIterator = ((SchemaField) element).getEventIterator();
 			while (eventIterator.hasNext()) {
 				String name = eventIterator.next().getName();
 				if (standardEvents.contains(name))
 					result.add(name);
 			}
 		}
 		return result;
 	}
 
 	@Deprecated
 	public static String action(HtmlViewRenderable component, String action) {
 		HtmlViewGenericComponent actionComponent = (HtmlViewGenericComponent) component;
 
 		return TransformerHelper.POJO_ACTION_PREFIX + TransformerHelper.SEPARATOR + actionComponent.getId() + TransformerHelper.SEPARATOR + action + TransformerHelper.SEPARATOR
 				+ actionComponent.getScreenArea();
 	}
 
 	public static String fieldName(HtmlViewRenderable component) {
 		return "" + (component).getId();
 	}
 
 	public static boolean isDisabled(HtmlViewRenderable component) {
 		return disabled(component);
 	}
 
 	public static boolean disabled(HtmlViewRenderable component) {
 		if (((HtmlViewGenericComponent) component).getSchemaElement() instanceof SchemaAction) {
 			Boolean feature = ((HtmlViewGenericComponent) component).getSchemaElement().getFeature(ViewActionFeatures.ENABLED);
 			if (feature == null) {
 				return false;
 			}
 			return !feature;
 		} else if (((HtmlViewGenericComponent) component).getSchemaElement() instanceof SchemaField) {
 			Boolean feature = ((HtmlViewGenericComponent) component).getSchemaElement().getFeature(ViewFieldFeatures.ENABLED);
 			if (feature == null) {
 				return false;
 			}
 			return !feature;
 		}
 		return false;
 
 	}
 
 	public static boolean checked(HtmlViewRenderable component) {
 		Object content = content(component);
 		if (content != null && content instanceof Boolean)
 			return ((Boolean) content).booleanValue();
 		return false;
 	}
 
 	public static boolean isValid(HtmlViewRenderable iComponent) {
 
 		HtmlViewAbstractContentComponent component = (HtmlViewAbstractContentComponent) iComponent;
 		return component.isValid();
 
 	}
 
 	public static boolean isRequired(HtmlViewRenderable iComponent) {
 		if (iComponent instanceof HtmlViewContentComponent) {
 			if (((HtmlViewGenericComponent) iComponent).getSchemaElement() != null) {
 				SchemaElement element = ((HtmlViewGenericComponent) iComponent).getSchemaElement();
 				if (element instanceof SchemaField && ((SchemaField) element).getFeature(ValidationFieldFeatures.REQUIRED))
 					return true;
 			}
 		} else if (iComponent instanceof HtmlViewFormAreaInstance && ((HtmlViewFormAreaInstance) iComponent).getComponents().size() == 1) {
 			HtmlViewRenderable renderable = ((HtmlViewFormAreaInstance) iComponent).getComponents().get(0);
 			return isRequired(renderable);
 		}
 		return false;
 	}
 
 	public static String validationMessage(HtmlViewRenderable iComponent) {
 		HtmlViewAbstractContentComponent component = (HtmlViewAbstractContentComponent) iComponent;
 		return component.getValidationMessage();
 	}
 
 	public static Collection<String> headers(HtmlViewRenderable component) {
 		Collection<String> result = new ArrayList<String>();
 		if (component instanceof HtmlViewComposedComponent) {
 			return ((HtmlViewComposedComponent) component).getHeaders();
 		}
 		return result;
 	}
 
 	public static Collection<String> headersRaw(HtmlViewRenderable component) {
 		Collection<String> result = new ArrayList<String>();
 		if (component instanceof HtmlViewComposedComponent) {
 			return ((HtmlViewComposedComponent) component).getHeadersRaw();
 		}
 		return result;
 	}
 
 	public static boolean isEvent(HtmlViewRenderable component, String eventName) {
 		HtmlViewGenericComponent actionComponent = (HtmlViewGenericComponent) component;
 		if (actionComponent.getSchemaField() != null)
 			return false;
 		return actionComponent.getSchemaField().getEvent(eventName) != null;
 	}
 
 	public static String formatDateContent(HtmlViewRenderable component) {
 		return formatDateContent(component, "dd/MM/yyyy");
 	}
 
 	public static String formatDateTimeContent(HtmlViewRenderable component) {
 		return formatDateContent(component, "HH:mm:ss");
 	}
 
 	public static String formatTimeContent(HtmlViewRenderable component) {
 		return formatDateContent(component, "dd/MM/yyyy HH:mm:ss");
 	}
 
 	public static String formatDateContent(HtmlViewRenderable component, String format) {
 		Date content = (Date) content(component);
 		if (content == null)
 			return "";
 		DateFormat formatter = new SimpleDateFormat(format);
 		return formatter.format(content);
 	}
 
 	public static String formatNumberContent(HtmlViewRenderable component) {
 		return "" + formattedContent(component);
 	}
 
 	/**
 	 * 
 	 * @return true if this field is a collection or an array and its selection field is a single object
 	 */
 	public static boolean isSingleSelection(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewContentComponent) {
 			return ((HtmlViewContentComponent) component).isSingleSelection();
 		}
 
 		return false;
 	}
 
 	/**
 	 * 
 	 * @return true if this field is a collection or an array and its selection field a collection or an array
 	 */
 	public static boolean isMultiSelection(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewContentComponent) {
 			return ((HtmlViewContentComponent) component).isMultiSelection();
 		}
 
 		return false;
 	}
 
 	public static Set<Integer> selectedIndexes(HtmlViewRenderable component) {
 		Set<Integer> result = new HashSet<Integer>();
 		if (component instanceof HtmlViewContentComponent) {
 			result = ((HtmlViewContentComponent) component).selectedIndex();
 			return result;
 		}
 		return result;
 	}
 
 	public static String selectedIndexesAsString(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewCollectionComposedComponent) {
 			HtmlViewCollectionComposedComponent collComponent = (HtmlViewCollectionComposedComponent) component;
 			if (collComponent.isMap()) {
 				return "" + collComponent.getSelectedMapIndex();
 			}
 		}
 		Set<Integer> indexes = selectedIndexes(component);
 		if (indexes == null) {
 			return "";
 		}
 		StringBuffer result = new StringBuffer("");
 		boolean firstElement = true;
 		for (Integer index : indexes) {
 			if (firstElement) {
 				firstElement = false;
 			} else {
 				result.append(",");
 			}
 			result.append(index);
 		}
 		return result.toString();
 	}
 
 	public static boolean isSelected(HtmlViewRenderable component, int index) {
 		Set<Integer> sel = selectedIndexes(component);
 		return sel != null && sel.contains(index);
 	}
 
 	public static String imageId(HtmlViewRenderable component) {
 		final Long longId = component.getId();
 
 		if (longId == null) {
 			return "";
 		}
 		return longId.toString();
 	}
 
 	public static int areaSize(HtmlViewRenderable component) {
 		int result = 1;
 		if (component instanceof AreaComponent) {
 			return ((AreaComponent) component).getAreaSize();
 		}
 
 		if (component instanceof HtmlViewCollectionComposedComponent) {
 
 			Object me = ((HtmlViewCollectionComposedComponent) component).getContainerComponent().getContent();
 			if (me instanceof Griddable) {
 				return ((Griddable) me).getSizeOfGrid();
 			}
 		}
 
 		return result;
 	}
 
 	public static String areaVerticalAlignment(final HtmlViewRenderable iComponent) {
 		String align = null;
 
 		if (iComponent instanceof AreaComponent) {
 			align = ((AreaComponent) iComponent).getAreaAlign();
 
 		} else if (iComponent instanceof AreaComponent) {
 			AreaComponent c = (AreaComponent) iComponent;
 			align = c.getAreaAlign();
 		}
 
 		if (align == null)
 			return "";
 
 		String[] aligns = align.split(" ");
 
 		if (aligns[0] != null && aligns[0].equals("center"))
 			aligns[0] = "middle";
 
 		return aligns[0];
 	}
 
 	public static String areaHorizontalAlignment(final HtmlViewRenderable iComponent) {
 		// if (iComponent == component)
 		// System.out.println("areaHorizontalAlignment(!" + iComponent + ")->" + iComponent.getClass());
 		// else
 		// System.out.println("areaHorizontalAlignment(" + iComponent + ")->" + iComponent.getClass());
 
 		AreaComponent area = null;
 
 		if (iComponent instanceof AreaComponent)
 			area = (AreaComponent) iComponent;
 		else
 			return "";
 
 		String align = area.getAreaAlign();
 
 		// System.out.println("= " + align);
 
 		if (align == null)
 			return "";
 
 		String[] aligns = align.split(" ");
 		return aligns.length > 1 ? aligns[1] : "";
 	}
 
 	public static String loadAsUrl(HtmlViewRenderable component) {
 		if (content(component) == null)
 			return "";
 
 		String url = contentAsString(component);
 		if (url.length() == 0)
 			return "";
 
 		HttpServletRequest request = (HttpServletRequest) Roma.context().component(HttpAbstractSessionAspect.CONTEXT_REQUEST_PAR);
 
 		boolean propagateSession = false;
 
 		String serverName;
 
 		if (url.contains(HttpUtils.VAR_LOCALHOST)) {
 			serverName = "localhost";
 			url = url.replace(HttpUtils.VAR_LOCALHOST, "");
 		} else
 			serverName = request.getServerName();
 
 		if (url.contains(HttpUtils.VAR_APPLICATION)) {
 			String app = HttpUtils.VAR_URL_HTTP + serverName + ":" + request.getServerPort() + request.getContextPath();
 			url = url.replace(HttpUtils.VAR_APPLICATION, app);
 		}
 
 		StringBuilder buffer = null;
 		if (url.contains(HttpUtils.VAR_CLIENT)) {
 			// LET THE CLIENT LOAD THE CONTENT
 			url = url.replace(HttpUtils.VAR_CLIENT, "");
 
 			String width = "200px";
 			String height = "200px";
 
 			String scrolling = "no";
 
 			buffer = new StringBuilder();
 			buffer.append("<iframe frameborder='0' width='");
 			buffer.append(width != null ? width.toString() : "100%");
 			buffer.append("' height='");
 			buffer.append(height != null ? height.toString() : "100%");
 			buffer.append("' scrolling='");
 			buffer.append(scrolling);
 			buffer.append("' src='");
 			buffer.append(url);
 			buffer.append("'/>");
 		} else {
 			if (url.contains(HttpUtils.VAR_SESSION)) {
 				url = url.replace(HttpUtils.VAR_SESSION, "");
 				propagateSession = true;
 			}
 
 			buffer = HttpUtils.loadUrlResource(url, propagateSession, request);
 
 		}
 		if (buffer != null) {
 			return buffer.toString();
 		} else {
 			return "";
 		}
 	}
 
 	public static String contextPath() {
 		HttpServletRequest request = (HttpServletRequest) Roma.context().component(HttpAbstractSessionAspect.CONTEXT_REQUEST_PAR);
 		return request.getContextPath();
 	}
 
 	public static boolean selectionAviable(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewContentComponent) {
 			return ((HtmlViewContentComponent) component).hasSelection();
 		}
 
 		return false;
 	}
 
 	@Deprecated
 	public static long currentTime() {
 		return System.currentTimeMillis();
 	}
 
 	public static Object additionalInfo(HtmlViewRenderable component) {
 		if (component instanceof HtmlViewContentComponentImpl) {
 			return ((HtmlViewContentComponentImpl) component).getAdditionalInfo();
 		}
 		return new Object();
 	}
 
 	public static String getInAreaLabel(HtmlViewRenderable component) {
		if (!(component instanceof HtmlViewContentComponent) && component instanceof HtmlViewInvisibleContentComponent)
 			return null;
 		String label = i18NLabel(component);
 		if ("".equals(label))
 			return null;
 		return label;
 	}
 }
