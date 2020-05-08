 package org.gethydrated.hydra.config;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.gethydrated.hydra.api.configuration.ConfigItemNotFoundException;
 import org.gethydrated.hydra.api.configuration.ConfigItemTypeException;
 import org.gethydrated.hydra.api.configuration.Configuration;
 import org.gethydrated.hydra.api.configuration.ConfigurationItem;
 import org.gethydrated.hydra.config.tree.ConfigList;
 import org.gethydrated.hydra.config.tree.ConfigValue;
 
 /**
  * 
  * @author Hanno Sternberg
  * @since 0.1.0
  * 
  */
 public class ConfigurationImpl implements Configuration {
 
     /**
      * 
      */
     private ConfigList root;
 
     /**
      * 
      */
     private static final String configSeparator = ".";
 
     /**
      * 
      */
     public ConfigurationImpl() {
         root = new ConfigList("Configuration");
     }
 
     @Override
     public final int hashCode() {
         final int prime = 31;
         int result = 1;
         if (root == null) {
             result = prime * result;
         } else {
             result = prime * result + root.hashCode();
         }
         return result;
     }
 
     @Override
     public final boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         ConfigurationImpl other = (ConfigurationImpl) obj;
         return root.equals(other.getRoot());
     }
 
     /**
      * 
      * @return root item.
      */
 
     public final ConfigurationItem getRoot() {
         return root;
     }
 
     @Override
     public void setRoot(ConfigurationItem root) {
         this.root = (ConfigList) root;
     }
 
     /**
      * 
      * @return configuration separation character.
      */
     public static String getConfigSeparator() {
         return configSeparator;
     }
 
 
     /**
      * 
      * @return copy of this configuration.
      */
     public final ConfigurationImpl copy() {
         ConfigurationImpl cp = new ConfigurationImpl();
         for (ConfigurationItem itm : root.getChildren()) {
             ((ConfigList)cp.getRoot()).getChildren().add(itm.copy());
         }
         return cp;
     }
 
     /**
      *
      * @param base
      *            Configuration base item.
      * @param l
      *            List with sub commands.
      * @param name
      *            Item name.
      * @return List of sub items.
      */
     private List<String> listFromItem(final ConfigurationItem base,
             final List<String> l, final String name) {
         if (name.trim().isEmpty()) {
             if (base.hasChildren()) {
                 for (ConfigurationItem itm : ((ConfigList) base).getChildren()) {
                     l.add(itm.getName());
                 }
             } else {
                 /* raise exception ?*/
             }
         } else if (!name.isEmpty()) {
             for (ConfigurationItem itm : ((ConfigList) base).getChildren()) {
                 if (name.equalsIgnoreCase(itm.getName())) {
                     return listFromItem(itm, l, "");
                 } else if (name.startsWith(itm.getName() + configSeparator)) {
                     return listFromItem(itm, l,
                             name.replace(itm.getName() + configSeparator, ""));
                 }
             }
         }
         return l;
     }
 
     @Override
     public final List<String> list(final String name) {
         List<String> l = new LinkedList<>();
         return listFromItem(getRoot(), l, name);
     }
 
     @Override
     public final List<String> list() {
         return list("");
     }
 
     /**
      * 
      * @param base
      *            base item.
      * @param name
      *            item name.
      * @param value
      *            item value.
      * @param type
      *            Type of the item.
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public final void setItem(final ConfigList base, final String name,
             final Object value, final Class<?> type) {
         String[] namesplit = name.split("\\" + configSeparator);
         if (namesplit.length == 1) {
             /* Got value element, set value */
             try {
                 ConfigValue child = (ConfigValue) base.getChild(name);
                 child.set(value);
             } catch (ConfigItemNotFoundException e) {
                 /* Value doesn't exists, add new element */
                 base.getChildren().add(new ConfigValue<>(name, value));
             }
         } else if (namesplit.length > 1) {
             /* Merge namesplit to tail */
             String nametail = namesplit[1];
             for (int i = 2; i < namesplit.length; i++) {
                nametail = nametail.concat(configSeparator + namesplit[i]);
             }
             ConfigList l;
             try {
                 /* List exists: append */
                 l = (ConfigList) base.getChild(namesplit[0]);
             } catch (ConfigItemNotFoundException e) {
                 /* List doesn't exists: create */
                 l = new ConfigList(namesplit[0]);
                 base.getChildren().add(l);
             }
             setItem(l, nametail, value, type);
         }
     }
 
     @Override
     public final void set(final String name, final Object value) {
         set(name, value, value.getClass());
     }
 
     /**
      * 
      * @param name
      *            configuration name.
      * @param value
      *            configuration value.
      * @param type
      *            configuration value type.
      */
     public final void set(final String name, final Object value,
             final Class<?> type) {
         setItem(root, name, value, type);
     }
 
     /**
      * 
      * @param base
      *            .
      * @param name
      *            configuration name.
      * @return Configuration value.
      * @throws ConfigItemNotFoundException .
      */
     private Object getFromItem(final ConfigList base, final String name)
             throws ConfigItemNotFoundException {
         for (ConfigurationItem i : base.getChildren()) {
             if (i.hasValue() && i.getName().equals(name)) {
                 return ((ConfigValue<?>) i).value();
             } else if (i.hasChildren()
                     && name.startsWith(i.getName() + configSeparator)) {
                 return getFromItem((ConfigList) i,
                         name.replace(i.getName() + configSeparator, ""));
             }
         }
         throw new ConfigItemNotFoundException(name);
     }
 
     @Override
     public final Object get(final String name)
             throws ConfigItemNotFoundException {
         return getFromItem(root, name);
     }
 
     @Override
     public final Boolean has(final String name) {
         try {
             Object o = get(name);
             return true;
         } catch(ConfigItemNotFoundException e) {
             return false;
         }
     }
 
     @Override
     public final Configuration getSubItems(final String name)
             throws ConfigItemNotFoundException, ConfigItemTypeException{
         Configuration subCfg = new ConfigurationImpl();
         subCfg.setRoot(((ConfigList) getRoot()).getSubItem(name, configSeparator));
         return subCfg;
     }
 
     @Override
     public final void setBoolean(final String name, final Boolean value) {
         set(name, value, value.getClass());
 
     }
 
     @Override
     public final Boolean getBoolean(final String name)
             throws ConfigItemNotFoundException {
         return (Boolean) get(name);
     }
 
     @Override
     public final void setInteger(final String name, final Integer value) {
         set(name, value, value.getClass());
     }
 
     @Override
     public final Integer getInteger(final String name)
             throws ConfigItemNotFoundException {
         return (Integer) get(name);
     }
 
     @Override
     public final void setFloat(final String name, final Double value) {
         set(name, value, value.getClass());
     }
 
     @Override
     public final Double getFloat(final String name)
             throws ConfigItemNotFoundException {
         return (Double) get(name);
     }
 
     @Override
     public final void setString(final String name, final String value) {
         set(name, value, value.getClass());
     }
 
     @Override
     public final String getString(final String name)
             throws ConfigItemNotFoundException {
         return (String) get(name);
     }
 
 }
