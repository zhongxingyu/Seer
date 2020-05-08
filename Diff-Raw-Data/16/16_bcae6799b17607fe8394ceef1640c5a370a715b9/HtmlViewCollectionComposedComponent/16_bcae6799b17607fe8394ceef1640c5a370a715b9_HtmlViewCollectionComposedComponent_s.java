 package org.romaframework.aspect.view.html.component.composed.list;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.romaframework.aspect.view.SelectionMode;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenArea;
 import org.romaframework.aspect.view.html.component.HtmlViewComposedComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.component.HtmlViewContentComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewGenericComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewInvisibleContentComponent;
 import org.romaframework.aspect.view.html.component.composed.HtmlViewAbstractComposedComponent;
 import org.romaframework.aspect.view.html.transformer.freemarker.TableDriver;
 import org.romaframework.core.Roma;
 import org.romaframework.core.domain.type.Pair;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaHelper;
 
 public class HtmlViewCollectionComposedComponent extends HtmlViewAbstractComposedComponent {
 
 	private static final LinkedList<String>	EMPTY_HEADER			= new LinkedList<String>();
 	private int															selectedMapIndex	= 0;
 	private TableDriver											driver;
 
 	public HtmlViewCollectionComposedComponent(final HtmlViewContentComponent containerComponent, final SchemaField schemaField, final Object content,
 			final HtmlViewScreenArea screenArea) {
 		super(containerComponent, schemaField, content, screenArea);
 		placeComponents();
 	}
 
 	protected void placeComponents() {
 		clearComponents();
 		if (content == null) {
 			return;
 		}
 
 		if (!(content instanceof Collection<?> || content.getClass().isArray() || isMap())) {
 			log.error("Render " + getSchemaField().getFeature(ViewFieldFeatures.RENDER) + " is supported only for List and Map elements: " + content);
 		}
 
 		String selectionField = (String) getSchemaField().getFeature(ViewFieldFeatures.SELECTION_FIELD);
 		if (selectionField != null && !"".equals(selectionField) && getContainerComponent().getContent() != null) {
 			SchemaClassDefinition classDefinition = Roma.schema().getSchemaClass(getContainerComponent().getContent().getClass());
 			SchemaField selectionFieldSchema = classDefinition.getField(selectionField);
 			// SELECTION FIELD IS SETTED ONLY FOR MAP AND SINGLE SELECTION.
 			if (!SchemaHelper.isMultiValueObject(selectionFieldSchema)) {
 				Object selectionFieldValue = SchemaHelper.getFieldValue(getContainerComponent().getContent(), selectionField);
 				SelectionMode selectionMode = getSchemaField().getFeature(ViewFieldFeatures.SELECTION_MODE);
 				if (selectionMode == SelectionMode.SELECTION_MODE_INDEX) {
 					if (selectionFieldValue instanceof Integer)
 						setSelectedMapIndex((Integer) selectionFieldValue);
 					else if (selectionFieldValue instanceof Short)
 						setSelectedMapIndex((Short) selectionFieldValue);
 					else if (selectionFieldValue instanceof Byte)
 						setSelectedMapIndex((Byte) selectionFieldValue);
 				} else {
 					Object[] contentObjects = SchemaHelper.getObjectArrayForMultiValueObject(getContent());
 					if (contentObjects != null && contentObjects.length > 0) {
 						for (int i = 0; i < contentObjects.length; ++i)
 							if (contentObjects[i].equals(selectionFieldValue)) {
 								setSelectedMapIndex(i);
 								break;
 							}
 					}
 				}
 			}
 		}
 
 		if (isCollection()) {
 			expandCollection();
 		} else if (isMap()) {
 			expandMap();
 		}
 
 		driver = new TableDriver(getSchemaField().getEmbeddedType(), getContentAsList(content));
 	}
 
 	@Override
 	public boolean validate() {
 		Boolean res = super.validate();
 		if (isMap() || isCollection()) {
 			for (HtmlViewGenericComponent genericComponent : getChildren()) {
 				if (!genericComponent.validate()) {
 					res = false;
 				}
 			}
 		}
 		return res;
 	}
 
 	public boolean isMap() {
 		return content instanceof Map<?, ?>;
 	}
 
 	private boolean isCollection() {
 		return content instanceof Collection<?> || (content != null && content.getClass().isArray());
 	}
 
 	private void expandMap() {
 		Collection<Pair<String, Object>> listContent = new ArrayList<Pair<String, Object>>();
 
 		for (Object ent : ((Map<?, ?>) content).entrySet()) {
 			Entry<Object, Object> e = (Entry<Object, Object>) ent;
 			listContent.add(new Pair<String, Object>(e.getKey() != null ? e.getKey().toString() : "", e.getValue()));
 		}
 
 		int index = 0;
 
 		for (final Pair<String, Object> obj : listContent) {
 			String label = obj.getKey();
			if (obj != null && obj.getValue() != null) {
 				createForm(index, null, obj.getValue(), label, this, (index == selectedMapIndex));
 			} else {
 				log.warn("Null object cannot be rendered in RowSet OR ColumnSet render Type");
 			}
 			index++;
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void expandCollection() {
 		if (content instanceof Byte[] || content instanceof byte[] || content instanceof char[] || content instanceof Character[]) {
 			log.debug("Excluding byte[] and char[] from form generation");
 			return;
 		}
 		final Collection<Object> collectionToRender = getContentAsList(content);
 
 		Class<?> typeClass = ((Class<?>) SchemaHelper.getEmbeddedType(getSchemaField()));
 
 		if (typeClass != null && Collection.class.isAssignableFrom(typeClass)) {
 			Iterator iter = collectionToRender.iterator();
 			createBodyFromCollectionOfCollections(iter);
 		} else {
 			// createHeadersForPojo(typeClass);
 			createBodyFromPojoList(collectionToRender, typeClass);
 		}
 	}
 
 	private void createBodyFromPojoList(final Collection<Object> collectionToRender, Class<?> typeClass) {
 		int rowNumber = 0;
 		for (final Object obj : collectionToRender) {
 			if (obj != null) {
 				createForm(rowNumber, null, obj, obj.toString(), this, true);
 				rowNumber++;
 			} else {
 				log.warn("Null object cannot be rendered in RowSet OR ColumnSet render Type");
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<Object> getContentAsList(final Object content) {
 		if (content instanceof Collection) {
 			return (Collection<Object>) content;
 		} else if (SchemaHelper.isMultiValueObject(content)) {
 			List<Object> asList = Arrays.asList(SchemaHelper.getObjectArrayForMultiValueObject(content));
 			return asList;
 		} else {
 			log.error("[HtmlViewAspect]: " + "Render " + getSchemaField().getFeature(ViewFieldFeatures.RENDER) + "  supported only for Collection and Object[] elements");
 			return new LinkedList<Object>();
 		}
 	}
 
 	private void createBodyFromCollectionOfCollections(Iterator<Collection<?>> iter) {
 		int rowNumber = 0;
 		while (iter.hasNext()) {
 			Collection<?> collection = iter.next();
 
 			HtmlViewMockComposedForm mock = new HtmlViewMockComposedForm(this, getSchemaField(), collection, screenArea);
 			if (rowNumber != 0) {
 				addComponent(mock);
 			}
 			int colIndex = 0;
 
 			Iterator<?> objIt = collection.iterator();
 
 			while (objIt.hasNext()) {
 				// Avoid to create a component for the label
 				if (rowNumber != 0) {
 					Object next = objIt.next();
 					createForm(rowNumber, colIndex, next, "", mock, true);
 				} else {
 					objIt.next();
 				}
 				colIndex++;
 			}
 			rowNumber++;
 		}
 	}
 
 	protected void createForm(Integer rowIndex, Integer colIndex, final Object obj, String label, HtmlViewComposedComponent component, boolean toShow) {
 		HtmlViewGenericComponent form;
 		if (toShow) {
 			form = new HtmlViewConfigurableEntityForm(this, Roma.session().getSchemaObject(obj), null, screenArea, rowIndex, colIndex, label);
 			form.setContent(obj);
 		} else {
 			form = new HtmlViewInvisibleContentComponent(this, rowIndex, obj, this.getScreenAreaObject(), label);
 		}
 
 		component.addComponent(form, toShow); // TODO
 	}
 
 	@Override
 	public void setContent(final Object content) {
 		super.setContent(content);
 		placeComponents();
 	}
 
 	public List<String> getHeadersRaw() {
 		if (isCollection()) {
 			List<String> result = new LinkedList<String>();
 
 			final Collection<Object> collectionToRender = getContentAsList(content);
 
 			// Class<?> typeClass = ((Class<?>) SchemaHelper.getEmbeddedType(getSchemaField()));
 			SchemaClass typeClass = getSchemaField().getEmbeddedType();
 
 			// if (Collection.class.isAssignableFrom(typeClass)) {
 			if (SchemaHelper.isMultiValueType(typeClass)) {
 				if (content == null) {
 					return EMPTY_HEADER;
 				}
 				Iterator<Object> iterator = collectionToRender.iterator();
 				if (iterator.hasNext()) {
 					Collection<Object> next = (Collection<Object>) iterator.next();
 					if (next != null) {
 						Iterator<Object> headerIt = next.iterator();
 						while (headerIt.hasNext()) {
 							result.add(headerIt.next() + "");
 						}
 					} else {
 						result.add("");
 					}
 				}
 			} else {
 				result.addAll(driver.getRawName());
 			}
 
 			return result;
 
 		} else if (isMap()) {
 			List<String> result = new LinkedList<String>();
 			result.add("Key");
 			result.add("Value");
 		}
 
 		return EMPTY_HEADER;
 	}
 
 	public List<String> getHeaders() {
 
 		if (isCollection()) {
 			List<String> result = new LinkedList<String>();
 
 			final Collection<Object> collectionToRender = getContentAsList(content);
 
 			// Class<?> typeClass = ((Class<?>) SchemaHelper.getEmbeddedType(getSchemaField()));
 			SchemaClass typeClass = getSchemaField().getEmbeddedType();
 
 			// if (Collection.class.isAssignableFrom(typeClass)) {
 			if (SchemaHelper.isMultiValueType(typeClass)) {
 				if (content == null) {
 					return EMPTY_HEADER;
 				}
 				Iterator<Object> iterator = collectionToRender.iterator();
 				if (iterator.hasNext()) {
 					Collection<Object> next = (Collection<Object>) iterator.next();
 					if (next != null) {
 						Iterator<Object> headerIt = next.iterator();
 						while (headerIt.hasNext()) {
 							result.add(headerIt.next() + "");
 						}
 					} else {
 						result.add("");
 					}
 				}
 			} else {
 				result.addAll(driver.getLabels());
 			}
 
 			return result;
 
 		} else if (isMap()) {
 			List<String> result = new LinkedList<String>();
 			result.add("Key");
 			result.add("Value");
 		}
 
 		return EMPTY_HEADER;
 	}
 
 	public TableDriver getTableDriver() {
 		return driver;
 	}
 
 	public int getSelectedMapIndex() {
 		return selectedMapIndex;
 	}
 
 	public void setSelectedMapIndex(int selectedMapIndex) {
 		this.selectedMapIndex = selectedMapIndex;
 	}
 
 }
