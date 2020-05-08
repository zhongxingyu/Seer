 package org.genericsystem.myadmin.beans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.enterprise.context.SessionScoped;
 import javax.faces.model.SelectItem;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.genericsystem.constraints.RequiredConstraintImpl;
 import org.genericsystem.constraints.SingularConstraintImpl;
 import org.genericsystem.constraints.SizeConstraintImpl;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.exception.NotRemovableException;
 import org.genericsystem.generic.MapProvider;
 import org.genericsystem.map.AbstractMapProvider;
 import org.genericsystem.map.AxedPropertyClass;
 import org.genericsystem.map.ConstraintsMapProvider;
 import org.genericsystem.map.PropertiesMapProvider;
 import org.genericsystem.map.SystemPropertiesMapProvider;
 import org.genericsystem.myadmin.util.GsMessages;
 import org.genericsystem.systemproperties.CascadeRemoveSystemProperty;
import org.genericsystem.systemproperties.NoInheritanceProperty;
 import org.genericsystem.systemproperties.NoReferentialIntegritySystemProperty;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Bean for manipulation of maps with MyAdmin Edit.
  * 
  * @version 0.5
  * @author Alexei KLENIN
  */
 @Named
 @SessionScoped
 public class MapProviderBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	protected static Logger log = LoggerFactory.getLogger(MapProviderBean.class);
 
	@Inject
	private GuiGenericsTreeBean genericTreeBean; // generic tree bean
	@Inject
	private PopupPanelBean popupPanelBean; // Popup Panel bean
	@Inject
	private GsMessages messages; // messages bean
 
 	private String key;
 	private int pos;
 	private boolean value;
 	private String valueStr;
 
 	/**
 	 * Returns map of map provider.
 	 * 
 	 * @param mapProvider
 	 *            - map provider.
 	 * 
 	 * @return map object.
 	 */
 	public <T extends AbstractMapProvider<?, ?>> Map<Serializable, Serializable> getMap(MapProvider mapProvider) {
 		// return mapProvider.getMap((Class<T>)genericTreeBean.getSelectedTreeNode().getGeneric().getClass());
 		return mapProvider.getExtendedMap(genericTreeBean.getSelectedTreeNode().getGeneric());
 	}
 
 	public boolean isPropertiesMapProvider(MapProvider mapProvider) {
 		return PropertiesMapProvider.class.isAssignableFrom((Class<?>) mapProvider.getValue());
 	}
 
 	/**
 	 * Returns set of etries of the map.
 	 * 
 	 * @param map
 	 *            - the map.
 	 * 
 	 * @return set of entries of the map.
 	 */
 	public Set<Entry<Serializable, Serializable>> getMapEntries(Map<Serializable, Serializable> map) {
 		return map.entrySet();
 	}
 
 	/**
 	 * Adds a couple of key and value from the bean to the map.
 	 * 
 	 * @param map
 	 *            - the map.
 	 */
 	public void addKeyValueToMap(Map<Serializable, Serializable> map) {
 		addKeyValueToMap(map, key, pos, value);
 	}
 
 	/**
 	 * Adds a couple of key and value to the map.
 	 * 
 	 * @param key
 	 *            - key.
 	 * @param value
 	 *            - value.
 	 */
 	public void addKeyValueToMap(Map<Serializable, Serializable> map, String key, int pos, boolean value) {
 		log.info("key " + key + " getClassByName(key) " + getClassByName(key) + " pos " + pos + " value " + value);
 		map.put(new AxedPropertyClass(getClassByName(key), pos), value);
 	}
 
 	public void addKeyValueToPropertiesMap(Map<Serializable, Serializable> map) {
 		addKeyValueToPropertiesMap(map, key, valueStr);
 	}
 
 	public void addKeyValueToPropertiesMap(Map<Serializable, Serializable> map, String key, String value) {
 		map.put(key, value);
 	}
 
 	/**
 	 * Removes an entry from the map.
 	 * 
 	 * @param map
 	 *            - map object.
 	 * @param entry
 	 *            - couple of key and value.
 	 */
 	public void removeEntryFromMap(Map<Serializable, Serializable> map, Entry<Serializable, Serializable> entry) {
 		try {
 			map.remove(entry.getKey());
 			messages.info("remove", entry.getKey());
 		} catch (NotRemovableException e) {
 			messages.info("cannotremove", e.getMessage());
 		}
 	}
 
 	/**
 	 * Return an class by it's name.
 	 * 
 	 * @param className
 	 *            - name of the class.
 	 * 
 	 * @return the class.
 	 */
 	@SuppressWarnings({ "unchecked" })
 	private Class<GenericImpl> getClassByName(String className) {
 		try {
 			return (Class<GenericImpl>) Class.forName(className.substring(6, className.length()));
 		} catch (ClassNotFoundException e) {
 			messages.info("ClassNotFoundException " + e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the list of possible classes for objects that figure in the key positions of the map.
 	 * 
 	 * @param mapProviderClass
 	 *            - class of map provider.
 	 * 
 	 * @return list of SelectItems for drop-down list in the popup.
 	 */
 	public <T extends MapProvider> List<SelectItem> getKeyClasses(Class<T> mapProviderClass) {
 		// TODO: Implement introspection of packages to dynamically get available constraint and
 		// properties classes.
 		List<SelectItem> items = new ArrayList<>();
 		if (mapProviderClass == ConstraintsMapProvider.class) {
 			items.add(new SelectItem(RequiredConstraintImpl.class, RequiredConstraintImpl.class.getSimpleName()));
 			items.add(new SelectItem(SingularConstraintImpl.class, SingularConstraintImpl.class.getSimpleName()));
 			items.add(new SelectItem(SizeConstraintImpl.class, SizeConstraintImpl.class.getSimpleName()));
 		} else {
 			if (mapProviderClass == PropertiesMapProvider.class) {
 				// TODO: What to do?
 			} else {
 				if (mapProviderClass == SystemPropertiesMapProvider.class) {
 					items.add(new SelectItem(CascadeRemoveSystemProperty.class, CascadeRemoveSystemProperty.class.getSimpleName()));
					items.add(new SelectItem(NoInheritanceProperty.class, NoInheritanceProperty.class.getSimpleName()));
 					items.add(new SelectItem(NoReferentialIntegritySystemProperty.class, NoReferentialIntegritySystemProperty.class.getSimpleName()));
 				}
 			}
 		}
 		return items;
 	}
 
 	public String getKey() {
 		return key;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	public int getPos() {
 		return pos;
 	}
 
 	public void setPos(int pos) {
 		this.pos = pos;
 	}
 
 	public boolean getValue() {
 		return value;
 	}
 
 	public void setValue(boolean value) {
 		this.value = value;
 	}
 
 	public String getValueStr() {
 		return valueStr;
 	}
 
 	public void setValueStr(String valueStr) {
 		this.valueStr = valueStr;
 	}
 
 }
