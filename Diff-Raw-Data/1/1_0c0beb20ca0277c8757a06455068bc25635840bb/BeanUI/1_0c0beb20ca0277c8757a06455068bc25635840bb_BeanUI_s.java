 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.richbeans.beans;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.beanutils.BeanMap;
 import org.apache.commons.beanutils.BeanUtils;
 
 import uk.ac.gda.beans.BeansFactory;
 import uk.ac.gda.richbeans.event.ValueListener;
 
 /**
  * Class concerned with sending state between beans and ui. It does this through the IFieldWidget interface. Initially
  * the design was to have no IFieldWidget and for BeanUI to synchonize and object but this lead to a complex and
  * confused design of BeanUI but more importantly of RichBeanEditorPart implementations. Now a IFieldWidget must
  * existing for each field in the bean mapping to each widget.
  * 
  * @author fcp94556
  */
 public class BeanUI {
 
 //	private static Logger logger = LoggerFactory.getLogger(BeanUI.class);
 
 	/**
 	 * NOTE: The order of the arguments. The first object is the bean, the second object is the uiObject which we are
 	 * going to look for getters and setters.
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @throws Exception
 	 * @throws InvocationTargetException
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 */
 
 	public static void beanToUI(final Object bean, final Object uiObject) throws Exception {
 
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception {
 				final Object value = prop.getValue();
 				box.setFieldName(prop.getKey().toString());
 				if (value == null /*&& !box.isActivated()*/) {
 					return;
 				}
 				box.setValue(value);
 			}
 		});
 	}
 
 	/**
 	 * Call to fire all value listeners
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @throws Exception
 	 */
 	public static void fireValueListeners(final Object bean, final Object uiObject) throws Exception {
 
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception {
 				box.fireValueListeners();
 			}
 		});
 	}
 
 	/**
 	 * Call to fire all value listeners
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @throws Exception
 	 */
 	public static void fireBoundsUpdaters(final Object bean, final Object uiObject) throws Exception {
 
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception {
 				box.fireBoundsUpdaters();
 			}
 		});
 	}
 
 	/**
 	 * NOTE: The order of the arguments. The first object is the uiobject, the second object is the bean which we are
 	 * going to set properties from the UI with.
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @throws Exception
 	 * @throws InvocationTargetException
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 */
 	public static void uiToBean(final Object uiObject, final Object bean) throws Exception {
 
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception {
 				final Object ob = box.getValue();
 				if (ob != null && !isNaN(ob) && !isInfinity(ob)) {
 					prop.setValue(ob); // Throws IllegalArgumentException if method does not exist.
 				}
 			}
 
 		});
 	}
 
 	/**
 	 * Set the value of a single field specified by field name in the bean from the ui.
 	 * 
 	 * @param uiObject
 	 * @param bean
 	 * @param fieldName
 	 * @throws Exception
 	 */
 	public static void uiToBean(final Object uiObject, final Object bean, final String fieldName) throws Exception {
 
 		if (fieldName == null)
 			throw new Exception("Null fieldName passed to uiToBean. Please set the field name.");
 
 		final IFieldWidget box = BeanUI.getFieldWiget(fieldName, uiObject);
 		if (box == null)
 			return; // Not all properties have to be in the UI.
 
 		if (!box.isActivated())
 			return;
 		final Object ob = box.getValue();
 		if (ob != null && !isNaN(ob) && !isInfinity(ob)) {
 			BeansFactory.setBeanValue(bean, fieldName, ob);
 		} else {
 			// Required to fix fields inside a list editor being edited to no value.
 			if (ob != null) {
 				final Method setter = bean.getClass().getMethod(BeansFactory.getSetterName(fieldName), ob.getClass());
 				setter.invoke(bean, ob.getClass().cast(null));
 			}
 		}
 
 	}
 
 	private static boolean isInfinity(Object ob) {
 		if (!(ob instanceof Double))
 			return false;
 		return Double.isInfinite(((Double) ob).doubleValue());
 	}
 
 	private static boolean isNaN(Object ob) {
 		if (!(ob instanceof Double))
 			return false;
 		return Double.isNaN(((Double) ob).doubleValue());
 	}
 
 	/**
 	 * Add a value listener for the UI objects, if that method exists.
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @param listener
 	 * @throws Exception
 	 */
 	public static void addValueListener(final Object bean, final Object uiObject, final ValueListener listener)
 			throws Exception {
 
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception {
 				box.addValueListener(listener);
 			}
 		});
 	}
 
 	/**
 	 * Removes a value listener for the UI objects, if that method exists.
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @param listener
 	 * @throws Exception
 	 */
 	public static void removeValueListener(final Object bean, final Object uiObject, final ValueListener listener)
 			throws Exception {
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception {
 				box.removeValueListener(listener);
 			}
 		});
 	}
 
 	/**
 	 * Holds the cached existing widgets for editing fields in beans.
 	 */
 	private static Map<String, IFieldWidget> cachedWidgets;
 
 	/**
 	 * Because of Lazy initiation some fields that will exist may not exists at the point at which we wish to listen to
 	 * them. Therefore a queue of listeners is kept. These are added to the widget and removed from the queue when and
 	 * if the widget is created.
 	 */
 	private static Map<String, Collection<ValueListener>> waitingListeners;
 
 	/**
 	 * You can record widgets associated with editing a particular bean field here. They are then available to be
 	 * listened to by other parts of the user interface. However all the field editors are recorded in a map so care
 	 * should be taken (memeory leak etc). Editors of lists like BeanListEditor have the same field to edit multiple
 	 * objects. So although you could add a listener to the field you cannot be sure which actual XML entry it is
 	 * currently dealing with.
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @throws Exception
 	 */
 	public static void setBeanFields(final Object bean, final Object uiObject) throws Exception {
 
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception {
 				addBeanField(bean.getClass(), prop.getKey().toString(), box);
 			}
 		});
 	}
 
 	/**
 	 * You can add a field associated with a bean (even if it is viewing a property and not actually editing one). All
 	 * editing fields are added through reflection with setBeanFields(...) however some are not fields and still can be
 	 * listened to.
 	 * 
 	 * @param beanClazz
 	 * @param fieldName
 	 * @param box
 	 */
 	public static void addBeanField(Class<? extends Object> beanClazz, String fieldName, final IFieldWidget box) {
 		fieldName = fieldName.substring(0, 1).toLowerCase(Locale.US) + fieldName.substring(1);
 		if (cachedWidgets == null)
 			cachedWidgets = new ConcurrentHashMap<String, IFieldWidget>(89);
 		final String id = beanClazz.getName() + ":" + fieldName;
 		cachedWidgets.put(id, box);
 		if (waitingListeners != null) {
 			final Collection<ValueListener> listeners = waitingListeners.get(id);
 			if (listeners != null) {
 				for (ValueListener valueListener : listeners)
 					box.addValueListener(valueListener);
 				waitingListeners.remove(id);
 			}
 		}
 	}
 
 	/**
 	 * Adds a value listener for the given class and field. Throws an exception if the class and field is not recorded
 	 * as having a UI editor at the moment.
 	 * 
 	 * @param beanClass
 	 * @param fieldName
 	 * @param listener
 	 */
 	public static void addBeanFieldValueListener(final Class<? extends Object> beanClass, String fieldName,
 			final ValueListener listener) {
 		fieldName = fieldName.substring(0, 1).toLowerCase(Locale.US) + fieldName.substring(1);
 		final String id = beanClass.getName() + ":" + fieldName;
 		final IFieldWidget box = (cachedWidgets != null) ? cachedWidgets.get(id) : null;
 		if (box == null) {
 			if (waitingListeners == null)
 				waitingListeners = new ConcurrentHashMap<String, Collection<ValueListener>>(31);
 			Collection<ValueListener> listeners = waitingListeners.get(id);
 			if (listeners == null) {
 				listeners = new HashSet<ValueListener>(3);
 				waitingListeners.put(id, listeners);
 			}
 			listeners.add(listener);
 			return;
 		}
 		box.addValueListener(listener);
 	}
 
 	/**
 	 * Attempts to retrieve the widget for editing the given field. The widget may not have been created yet, in which
 	 * case returns null.
 	 * 
 	 * @param beanClasses
 	 * @param fieldName
 	 * @return IFieldWidget
 	 */
 	public static IFieldWidget getBeanField(String fieldName, final Class<? extends Object>... beanClasses) {
 
 		fieldName = fieldName.substring(0, 1).toLowerCase(Locale.US) + fieldName.substring(1);
 
 		for (int i = 0; i < beanClasses.length; i++) {
 			final String id = beanClasses[i].getName() + ":" + fieldName;
 			final IFieldWidget box = (cachedWidgets != null) ? cachedWidgets.get(id) : null;
 			if (box != null)
 				return box;
 		}
 		return null;
 	}
 
 	/**
 	 * Used to switch all ui controls on or off.
 	 * 
 	 * @param bean
 	 * @param uiObject
 	 * @param on
 	 * @throws Exception
 	 * @throws InvocationTargetException
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 */
 	public static void switchState(final Object bean, final Object uiObject, final boolean on) throws Exception {
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) {
 				if (on) {
 					box.on();
 				} else {
 					box.off();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Attempts to set any IFieldWidgets available from getter methods on.
 	 * 
 	 * @param uiObject
 	 * @param on
 	 */
 	public static void switchState(Object uiObject, boolean on) throws Exception {
 		final Method[] methods = uiObject.getClass().getMethods();
 		for (int i = 0; i < methods.length; i++) {
 			final Method m = methods[i];
 			if (m.getReturnType() != null && m.getName().startsWith("get")) {
 				final Object ob = m.invoke(uiObject);
 				if (ob instanceof IFieldWidget) {
 					final IFieldWidget box = (IFieldWidget) ob;
 					if (on) {
 						box.on();
 					} else {
 						box.off();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param bean
 	 * @param uiObject
 	 * @param isEnabled
 	 * @throws Exception
 	 */
 	public static void setEnabled(final Object bean, final Object uiObject, final boolean isEnabled) throws Exception {
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) {
 				box.setEnabled(isEnabled);
 			}
 		});
 	}
 
 	public static void dispose(final Object bean, final Object uiObject) throws Exception {
 		BeanUI.notify(bean, uiObject, new BeanProcessor() {
 			@Override
 			public void process(Entry<Object, Object> prop, IFieldWidget box) {
 				box.dispose();
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	public final static void notify(final Object bean, final Object uiObject, final BeanProcessor worker)
 			throws Exception {
 
 		final BeanMap properties = new BeanMap(bean);
 		final Iterator<Entry<Object, Object>> it = properties.entryIterator();
 		Collection<String> names  = BeanUI.getEditingFields(bean, uiObject);
 		while (it.hasNext()) {
 			final Entry<Object, Object> prop = it.next();
 				final String fieldName = prop.getKey().toString();
 				if (names.contains(fieldName)) {					
 					if (fieldName.equals("class"))
 						continue;
 					final IFieldWidget box = BeanUI.getFieldWiget(fieldName, uiObject);
 					// NOTE non-IFieldWidget fields will be ignored.
 					if (box != null)
 						worker.process(prop, box);
 				}
 		}
 	}
 
 	public static interface BeanProcessor {
 		void process(Entry<Object, Object> prop, IFieldWidget box) throws Exception;
 	}
 
 	/**
 	 * Get the ui field out of the object container.
 	 * 
 	 * @param fieldName
 	 * @param uiObject
 	 * @return IFieldWidget or null if is not an IFieldWidget instance
 	 * @throws Exception
 	 * @throws NoSuchMethodException
 	 * @throws SecurityException
 	 * @throws InvocationTargetException
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 */
 	public static IFieldWidget getFieldWiget(final String fieldName, final Object uiObject) throws Exception {
 		final String methodName = BeansFactory.getGetterName(fieldName);
 		final Method getter = uiObject.getClass().getMethod(methodName);
 		final Object box = getter.invoke(uiObject);
 		if (box instanceof IFieldWidget) {
 			return (IFieldWidget) box;
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a new list of cloned beans (deep).
 	 * 
 	 * @param beans
 	 * @return list of cloned beans.
 	 * @throws Exception
 	 */
 	public static List<?> cloneBeans(final List<?> beans) throws Exception {
 		final List<Object> ret = new ArrayList<Object>(beans.size());
 		for (Object bean : beans)
 			ret.add(BeansFactory.deepClone(bean));
 		return ret;
 	}
 
 	/**
 	 * Retrieves a list of fields which are both in the bean and being edited by the user.
 	 * 
 	 * @param editorBean
 	 * @param editorUI
 	 * @return list of fields
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public static List<String> getEditingFields(Object editorBean, Object editorUI) throws Exception {
 
 		final Collection<String> fields = BeanUtils.describe(editorBean).keySet();
 		final List<String> expressionFields = new ArrayList<String>(fields);
 		expressionFields.remove("class");
 
 		for (Iterator<String> it = expressionFields.iterator(); it.hasNext();) {
 			String field = it.next();
 			try {
 				final IFieldWidget wid = BeanUI.getFieldWiget(field, editorUI);
 				if (wid == null)
 					it.remove();
 			} catch (Exception ne) {
 				it.remove();
 			}
 		}
 
 		return expressionFields;
 	}
 
 	/**
 	 * Bean from string using standard java serialization, useful for tables of beans with serialized strings. Used
 	 * externally to the GDA.
 	 * 
 	 * @param xml
 	 * @return the bean
 	 */
 	public static Object getBean(final String xml, final ClassLoader loader) throws Exception {
 
 		final ClassLoader original = Thread.currentThread().getContextClassLoader();
 		final ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
 		try {
 			Thread.currentThread().setContextClassLoader(loader);
 			XMLDecoder d = new XMLDecoder(new BufferedInputStream(stream));
 			final Object bean = d.readObject();
 			return bean;
 		} finally {
 			Thread.currentThread().setContextClassLoader(original);
 			stream.close();
 		}
 	}
 
 	/**
 	 * Used externally to the GDA.
 	 * 
 	 * @param bean
 	 * @return the string
 	 */
 	public static String getString(Object bean) throws Exception {
 
 		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
 
 		final ClassLoader original = Thread.currentThread().getContextClassLoader();
 		try {
 			Thread.currentThread().setContextClassLoader(bean.getClass().getClassLoader());
 			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(stream));
 			e.writeObject(bean);
 			e.close();
 			return stream.toString("UTF-8");
 		} finally {
 			Thread.currentThread().setContextClassLoader(original);
 			stream.close();
 		}
 	}
 }
