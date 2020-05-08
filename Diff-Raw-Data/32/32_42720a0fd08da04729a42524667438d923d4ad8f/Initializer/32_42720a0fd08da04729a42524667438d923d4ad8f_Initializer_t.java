 /**
  * 
  */
 package co.pishfa.accelerate.initializer;
 
 import java.beans.PropertyDescriptor;
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.el.ValueExpression;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.beanutils.ConvertUtils;
 import org.apache.commons.beanutils.MethodUtils;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.collections.ArrayStack;
 import org.apache.commons.lang3.StringUtils;
 import org.jdom2.Attribute;
 import org.jdom2.Element;
 import org.jdom2.input.SAXBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.odysseus.el.util.SimpleContext;
 
 /**
  * Builds an object graph from an xml file. The main purpose is to initialize the database from an xml file. The
  * initializer can be configured once and then be used for reading multiple xml files (the anchors are shared). This
  * class is not thread-safe.
  * 
  * @author Taha Ghasemi
  * 
  */
 public class Initializer {
 
 	private static final Logger log = LoggerFactory.getLogger(Initializer.class);
 
 	// Special elements or attributes
 	private static final String LOAD_ELEMENT = "load";
 	private static final String INCLUDE_ELEMENT = "include";
 	private static final String _IN_PARENT = "_in-parent_";
 	private static final String _ACTION = "_action_";
 	private static final String _ANCHOR = "_anchor_";
 	// TODO is not supported yet
 	private static final String _CHILD_ANCHOR = "_child-anchor_";
 
 	private final Map<String, Object> anchores = new HashMap<>();
 	private final ArrayStack stack = new ArrayStack();
 	private final InitListener listener;
 	private boolean insideLoad;
 	private final InitializerFactory factory;
 	private final SimpleContext context = new SimpleContext();
 
 	public Initializer(InitializerFactory factory, InitListener listener, Map<String, Object> contextVars) {
 		this.factory = factory;
 		this.listener = listener;
 		context.setVariable("parents", factory.getEngine().createValueExpression(stack, ArrayStack.class));
 		context.setVariable("anchors", factory.getEngine().createValueExpression(anchores, Map.class));
 		if (contextVars != null) {
 			for (Entry<String, Object> entry : contextVars.entrySet()) {
 				context.setVariable(entry.getKey(),
 						factory.getEngine().createValueExpression(entry.getValue(), entry.getValue().getClass()));
 			}
 		}
 	}
 
 	public Object read(File file) throws Exception {
 		SAXBuilder builder = new SAXBuilder();
 		Element root = builder.build(file).getRootElement();
 		return read(root);
 	}
 
 	public Object read(InputStream in) throws Exception {
 		SAXBuilder builder = new SAXBuilder();
 		Element root = builder.build(in).getRootElement();
 		return read(root);
 	}
 
 	public Object read(Element dataElem) throws Exception {
 		Object lastEntity = null;
 		for (Element childElem : dataElem.getChildren()) {
 			lastEntity = processElement(null, childElem);
 		}
 		return lastEntity;
 	}
 
 	/**
 	 * Processes the given element. Since an element can be a class-instance or property of its parent, parentEntity
 	 * should be passed. In case of no parent, null is an acceptable value.
 	 * 
 	 * @param parentEntity
 	 *            null if no parent is present.
 	 * @param element
 	 *            element to be processed
 	 * @return
 	 */
 	protected Object processElement(InitEntityMetaData parentEntity, Element element) {
 		try {
 			// Process special elements: include, load.
 			if (INCLUDE_ELEMENT.equals(element.getName())) {
 				String srcName = element.getAttributeValue("src");
 				// TODO a better resource loading is required like velociy
 				try (InputStream src = Thread.currentThread().getContextClassLoader().getResourceAsStream(srcName)) {
 					if (src == null) {
 						throw new IllegalArgumentException("Could not find " + srcName + " in class path to include");
 					}
 					read(src);
 				}
 				return null;
 			} else if (LOAD_ELEMENT.equals(element.getName())) {
 				// Support nested load elements
 				boolean oldInsideLoad = insideLoad;
 				insideLoad = true;
 				try {
 					processChildren(element, null);
 					return null;
 				} finally {
 					insideLoad = oldInsideLoad;
 				}
 			}
 
 			// child anchors are previously processed.
 			if (parentEntity != null && !StringUtils.isEmpty(element.getAttributeValue(_CHILD_ANCHOR))) {
 				return null;
 			}
 
 			// check whether it is a class alias or a property of an entity
 			// initEntiy is null when element is not a class alias
 			InitEntityMetaData initEntity = factory.getInitEntityByAlias(element.getName());
 			Object entityObj = getObject(parentEntity, element, initEntity);
 			if (insideLoad) {
 				processAttributes(element, entityObj, initEntity);
 				return null;
 			}
 
 			stack.push(entityObj);
 			try {
 				processAttributes(element, entityObj, initEntity);
 				if (initEntity != null) {
 					listener.entityCreated(initEntity, entityObj);
 				}
 				processChildren(element, initEntity);
 			} finally {
 				stack.pop();
 			}
 
 			if (initEntity != null) {
 				listener.entityFinished(initEntity, entityObj);
 			}
 
 			return entityObj;
 		} catch (Exception e) {
 			log.error("Exception occured during processing of element " + element.getName(), e);
 			return null;
 		}
 	}
 
 	/**
 	 * Creates or finds the object that corresponds to the given element whether it is an instance of a class or
 	 * property of an object.
 	 * 
 	 * @param parentEntity
 	 * @param element
 	 * @param initEntity
 	 *            the corresponding initEntityDate definition to the element. It is null when element is not a class
 	 *            alias
 	 * @return
 	 * @throws Exception
 	 */
 	protected Object getObject(InitEntityMetaData parentEntity, Element element, InitEntityMetaData initEntity)
 			throws Exception {
 		Object entityObj = null;
 		if (initEntity != null) {
 			// class mode
 			stack.push(null);
 			try {
 				entityObj = findOrCreateEntity(initEntity, element);
 			} finally {
 				stack.pop();
 			}
 		} else {
 			// property mode
 			String propName = element.getName();
 
 			// check for alias
 			if (parentEntity != null) {
 				InitPropertyMetaData initProperty = parentEntity.getPropertiesByAlias().get(propName);
 				if (initProperty != null) {
 					propName = initProperty.getName();
 				}
 			}
 
 			if (stack.isEmpty()) {
 				throw new IllegalArgumentException("First level element " + propName
 						+ " must be an alias of a class but no class with this alias is found.");
 			}
 			entityObj = PropertyUtils.getProperty(stack.peek(), propName);
 			if (entityObj == null) {
 				throw new IllegalArgumentException("The property " + element.getName() + " of object " + stack.peek()
 						+ " is null.");
 			}
 		}
 		return entityObj;
 	}
 
 	protected void processChildren(Element element, InitEntityMetaData initEntity) {
 		for (Element childElem : element.getChildren()) {
 			processElement(initEntity, childElem);
 		}
 	}
 
 	protected void processAttributes(Element element, Object entityObj, InitEntityMetaData initEntity) throws Exception {
 		Map<String, Object> allAttributes = getAllAttributes(initEntity, element, entityObj);
 
 		// Auto anchoring: it creates an anchor like this EntityAlias:unique1_unique2_uniqe3, provided that all unique
 		if (factory.isAutoAnchor()) {
 			if (!allAttributes.containsKey(_ANCHOR)) {
 				String[] uniqueProperties = getUniqueProperties(initEntity, allAttributes);
 				StringBuilder uniqeValue = new StringBuilder();
 				boolean allNotNull = true;
 				for (String uniqueProperty : uniqueProperties) {
 					Object propertyValue = allAttributes.get(uniqueProperty);
 					if (propertyValue == null) {
 						allNotNull = false;
 						break;
 					}
 					uniqeValue.append("_").append(propertyValue);
 				}
 				if (allNotNull) {
 					uniqeValue.setCharAt(0, ':'); // convert the first _ to :
 					uniqeValue.insert(0, initEntity.getAlias());
 					allAttributes.put(_ANCHOR, uniqeValue.toString());
 				}
 			}
 		}
 		for (Entry<String, Object> entry : allAttributes.entrySet()) {
 			processAttribute(element, initEntity, entityObj, entry.getKey(), entry.getValue());
 		}
 	}
 
 	/**
 	 * Creates the entity in non-incremental mode. In incremental mode it first finds the values of all attributes and
 	 * then tries to find an entity with values of those attributes that uniquely identifies it. If it fails, the newly
 	 * created entity will be returned.
 	 */
 	protected Object findOrCreateEntity(InitEntityMetaData initEntity, Element element) throws Exception {
 		Object entityObj = initEntity.getEntityClass().newInstance();
 		if (!factory.isIncremental() && !insideLoad) {
 			return entityObj;
 		}
 
 		Map<String, Object> allAttributes = getAllAttributes(initEntity, element, entityObj);
 
 		String[] properties = getUniqueProperties(initEntity, allAttributes);
 		Object[] values = new Object[properties.length];
 		for (int i = 0; i < properties.length; i++) {
 			values[i] = allAttributes.get(properties[i]);
 		}
 
 		Object res = listener.findEntity(initEntity, properties, values);
 		return res == null ? entityObj : res;
 	}
 
 	/**
 	 * finds the unifying attributes. By default, it is factory.getUniquePropertyName().
 	 * 
 	 */
 	protected String[] getUniqueProperties(InitEntityMetaData initEntity, Map<String, Object> allAttributes) {
 		String unique = initEntity.getUnique();
 		if (StringUtils.isEmpty(unique) && allAttributes.containsKey(factory.getUniquePropertyName())) {
 			unique = factory.getUniquePropertyName();
 		}
 
		if ("*".equals(unique)) {
 			StringBuilder ustr = new StringBuilder();
 			for (String name : allAttributes.keySet()) {
 				if (!name.startsWith("_")) {
 					ustr.append(",").append(name);
 				}
 			}
 			unique = ustr.deleteCharAt(0).toString();
 		}
 
 		String[] properties = unique.split(",");
 		return properties;
 	}
 
 	/**
 	 * Get the values of all attributes for this element whether they are explicitly mentioned in the attributes of the
 	 * element or they have default value in the initEntity.
 	 * 
 	 * @param entityObj
 	 *            TODO the instance is not important only its class is used for checking some fields types
 	 */
 	private Map<String, Object> getAllAttributes(InitEntityMetaData initEntity, Element element, Object entityObj)
 			throws Exception {
 		Map<String, Object> attributes = new HashMap<String, Object>();
 		context.setVariable("this", factory.getEngine().createValueExpression(attributes, Map.class));
 
 		for (Attribute attr : element.getAttributes()) {
 			String attrName = attr.getName();
 			String attrValue = attr.getValue();
 			// resolve aliases
 			InitPropertyMetaData prop = null;
 			if (initEntity != null) {
 				prop = initEntity.getPropertiesByAlias().get(attrName);
 				if (prop != null) {
 					attrName = prop.getName();
 				}
 			}
 			attributes.put(attrName, getAttributeValue(element, attrName, attrValue, context, entityObj, prop));
 		}
 		// check for unset defaults
 		if (initEntity != null) {
 			for (InitPropertyMetaData property : initEntity.getProperties()) {
 				String attrName = property.getName();
 				if (!attributes.containsKey(attrName) && property.getDefaultValue() != null) {
 					attributes.put(
 							attrName,
 							getAttributeValue(element, attrName, property.getDefaultValue(), context, entityObj,
 									property));
 				}
 			}
 		}
 
 		return attributes;
 	}
 
 	/**
 	 * Resolves the value attrValue of attribute attrName to a concrete type within the given context.
 	 * 
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	protected Object getAttributeValue(Element element, String attrName, String attrValue, SimpleContext context,
 			Object entityObj, InitPropertyMetaData initProperty) throws Exception {
 		// Detect the target property type
 		Class propertyType = null;
 		if (entityObj != null) {
 			PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(entityObj, attrName);
 			if (descriptor != null) {
 				propertyType = descriptor.getPropertyType();
 			}
 		}
 
 		Object value = null;
 		// check for dynamic references
 		if (attrValue.startsWith("@")) {
 			boolean optional = attrValue.endsWith("?");
 			if (optional) {
 				attrValue = attrValue.substring(0, attrValue.length() - 1);
 			}
 			if (_ACTION.equals(attrName)) {
 				value = attrValue;
 			} else if (attrValue.startsWith("@parent(")) {
 				int arg = Integer.parseInt(attrValue.substring(8, attrValue.length() - 1));
 				try {
 					value = stack.peek(arg);
 				} catch (Exception e) {
 					if (!optional) {
 						throw new IllegalArgumentException("Parent index is not valid in stack: " + arg);
 					}
 				}
 			} else if (attrValue.equals("@parent")) {
 				// Finds the first parent with specified type
 				for (int level = 1; level < stack.size(); level++) {
 					Object parent = stack.peek(level);
 					if (propertyType.isAssignableFrom(parent.getClass())) {
 						value = parent;
 						break;
 					}
 				}
 				if (value == null && !optional) {
 					throw new IllegalArgumentException("Could not find an appropriate parent with type " + propertyType
 							+ " for " + attrName + " in " + element);
 				}
 			} else if (attrValue.equals("@child")) {
 				// now we should look into children to find the one with corresponding child-anchor
 				Element childElem = findChild(element, "child-anchor", attrName);
 				if (childElem == null) {
 					throw new IllegalArgumentException("No child with child-anchor = " + attrName + " in " + element);
 				}
 				value = processElement(null, childElem);
 			} else { // look into anchors
 				if (attrValue.indexOf(';') < 0) {
 					value = getAnchorValue(attrValue, propertyType, optional);
 				} else {
 					List<Object> list = new ArrayList<Object>();
 					for (String part : attrValue.split(";")) {
 						// Note that in this case, passing property type is not useful since it is of type List
 						list.add(getAnchorValue(part, null, optional));
 					}
 					value = list;
 				}
 
 			}
 		} else if (attrValue != null && !"null".equals(attrValue)) {
 			// Evaluate EL, if allowed
 			if (initProperty == null || initProperty.isDynamic()) {
 				ValueExpression valueExpression = factory.getEngine().createValueExpression(context, attrValue,
 						Object.class);
 				value = valueExpression.getValue(context);
 			} else {
 				value = attrValue;
 			}
 			// Do type conversion
 			if (propertyType != null) {
 				if (propertyType.isEnum()) {
 					value = Enum.valueOf(propertyType, value.toString());
 				} else {
 					value = ConvertUtils.convert(value, propertyType);
 				}
 			}
 		}
 		return value;
 	}
 
 	private Object getAnchorValue(String attrValue, Class<?> propertyType, boolean optional) {
 		String anchorName = attrValue.substring(1);
 		Object value = anchores.get(anchorName);
 		if (value != null) {
 			return value;
 		} else {
 			// Auto-scoping: tries to guess
 			// First find the alias corresponding to this propertyType
 			String alias = null;
 			InitEntityMetaData initEntity = factory.getInitEntityByClass(propertyType);
 			if (initEntity != null) {
 				alias = initEntity.getAlias();
 			}
 
 			if (alias != null) {
 				value = anchores.get(alias + ":" + anchorName);
 				if (value != null) {
 					return value;
 				}
 			}
 
 			if (!optional) {
 				throw new IllegalArgumentException("Unknown anchor with name " + anchorName);
 			}
 			return null;
 		}
 	}
 
 	protected void processAttribute(Element element, InitEntityMetaData initEntity, Object entityObj, String attrName,
 			Object attrValue) throws Exception {
 		String value = String.valueOf(attrValue);
 		try {
 			// check for special attributes
 			if (attrName.equals(_ANCHOR)) {
 				if (value.startsWith("parent") || value.equals("child") || StringUtils.containsAny(value, '@', '?')) {
 					throw new IllegalArgumentException("Illegal anchor name " + value);
 				}
 				if (anchores.containsKey(value)) {
 					throw new IllegalArgumentException("Duplicate anchor name " + value);
 				}
 				anchores.put(value, entityObj);
 			} else if (attrName.equals(_ACTION)) {
 				int index = value.indexOf('.');
 				int arg = Integer.parseInt(value.substring(8, index - 1)); // -1 for )
 				Object target = stack.peek(arg);
 				MethodUtils.invokeMethod(target, value.substring(index + 1), entityObj);
 			} else if (attrName.equals(_IN_PARENT)) {
 				Object parent = stack.get(stack.size() - 2);
 				BeanUtils.setProperty(parent, value, entityObj);
 			} else {
 				setPropertyValue(element, entityObj, attrName, attrValue);
 			}
 		} catch (Exception e) {
 			throw new RuntimeException("Exception occured while processing attribute " + attrName + " of element "
 					+ element.getName(), e);
 		}
 
 	}
 
 	protected void setPropertyValue(Element element, Object entityObj, String attrName, Object value)
 			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
 		BeanUtils.setProperty(entityObj, attrName, value);
 	}
 
 	protected Element findChild(Element element, String attrName, String attrValue) {
 		for (Element childElem : element.getChildren()) {
 			if (attrValue.equals(childElem.getAttributeValue(attrName))) {
 				return childElem;
 			}
 		}
 		return null;
 	}
 
 }
