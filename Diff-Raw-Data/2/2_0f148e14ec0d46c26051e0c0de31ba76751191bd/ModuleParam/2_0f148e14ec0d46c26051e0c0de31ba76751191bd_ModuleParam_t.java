 /**
  * XWeb project
  * Created by Hamed Abdollahpour
  * https://github.com/abdollahpour/xweb
  */
 
 package ir.xweb.module;
 
 import java.io.File;
 import java.lang.String;
 import java.net.URL;
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class ModuleParam implements Map<String, String> {
 
     private final Map<String, String> data;
 
     private final List<String> defaults;
 
     protected ModuleParam() {
         this(null, null);
     }
 
     protected ModuleParam(final Map<String, String> data) {
         this(data, null);
     }
 
     protected ModuleParam(final Map<String, String> data, final List<String> defaults) {
         this.data = data != null ? data : new HashMap<String, String>();
         this.defaults = defaults != null ? defaults : new ArrayList<String>();
     }
 
     protected void put(final String name, final String value, final boolean isDefault) {
         final String oldValue = data.put(name, value);
         if(isDefault) {
             defaults.add(name);
         } else if(oldValue != null) {
             defaults.remove(name);
         }
     }
 
     public String get(String name, String def) {
         return getString(name, def);
     }
 
     public String getString(final String name, final String def) {
         String s = data.get(name);
         return (s == null || s.length() == 0) ? def : s;
     }
 
     public String getString(final String name) {
         return getString(name, null);
     }
 
     public String[] getStrings(final String name, final char separator) {
         final String value = this.data.get(name);
         if(value != null && value.length() > 0) {
             final String[] strings = value.split(Pattern.quote(Character.toString(separator)));
             return strings;
         }
         return new String[0];
     }
 
     public Integer getInt(final String name, final Integer def) {
         String s = data.get(name);
         return (s == null || s.length() == 0) ? def : Integer.parseInt(s);
     }
 
     public Integer getInt(final String name) {
         return getInt(name, null);
     }
 
     public Float getFloat(final String name, final Float def) {
         String s = data.get(name);
         return (s == null || s.length() == 0) ? def : Float.parseFloat(s);
     }
 
     public Float getFloat(final String name) {
         return getFloat(name, null);
     }
 
     public Double getDouble(final String name, final Double def) {
         String s = data.get(name);
         return (s == null || s.length() == 0) ? def : Double.parseDouble(s);
     }
 
     public Double getDouble(final String name) {
         return getDouble(name, null);
     }
 
     public Long getLong(final String name, final Long def) {
         String s = data.get(name);
         return (s == null || s.length() == 0) ? def : Long.parseLong(s);
     }
 
     public Long getLong(final String name) {
         return getLong(name, null);
     }
 
     public Long[] getLongs(final String name, final char separator) {
         final String[] strings = getStrings(name, separator);
         final Long[] longs = new Long[strings.length];
         for(int i=0; i<strings.length; i++) {
             longs[i] = Long.parseLong(strings[i]);
         }
        return longs;
     }
 
     public Byte getByte(final String name, final Byte def) {
         String s = data.get(name);
         return (s == null || s.length() == 0) ? def : Byte.parseByte(s);
     }
 
     public Byte getByte(final String name) {
         return getByte(name, null);
     }
 
     public Boolean getBoolean(final String name, final Boolean def) {
         String s = data.get(name);
         return (s == null || s.length() == 0) ? def : Boolean.parseBoolean(s);
     }
 
     public Boolean getBoolean(final String name) {
         return getBoolean(name, null);
     }
 
     public URL getURL(final String name, final URL def) throws IllegalArgumentException {
         final String s = data.get(name);
         try {
             return (s == null || s.length() == 0) ? def : new URL(s);
         } catch (Exception ex) {
             throw new IllegalArgumentException("Illegal URL: " + s);
         }
     }
 
     public File getFile(final String name, final File def) {
         String path = getString(name, null);
         return path == null ? def : new File(path);
     }
 
     public File getFile(final String name) {
         return getFile(name, null);
     }
 
     public Locale getLocale(final String name, final Locale def) {
         String s = data.get(name);
         if((s == null || s.length() == 0)) {
             return def;
         }
 
         Locale locale = parseLocale(s);
         if(!isValid(locale)) {
             throw new IllegalArgumentException("Locale is not valid. Name:" + name + ", Value: " + s);
         }
         return locale;
     }
 
     public Locale getLocale(final String name) {
         return getLocale(name, null);
     }
 
     public <T> T get(Class<T> clazz, String name) {
         String value = get(name);
         if(value == null) {
             return null;
         }
 
         if(clazz.equals(Integer.class)) {
             return clazz.cast(Integer.parseInt(value));
         } else if(clazz.equals(Float.class)) {
             return clazz.cast(Float.parseFloat(value));
         } else if(clazz.equals(Double.class)) {
             return clazz.cast(Double.parseDouble(value));
         } else if(clazz.equals(Long.class)) {
             return clazz.cast(Long.parseLong(value));
         } else if(clazz.equals(Boolean.class)) {
             return clazz.cast(Boolean.parseBoolean(value));
         } else if(clazz.equals(String.class)) {
             return clazz.cast(value);
         }
 
         throw new IllegalArgumentException("type does not support: " + clazz);
     }
 
     /**
      * http://stackoverflow.com/questions/3684747/how-to-validate-a-locale-in-java
      * @param locale
      * @return
      */
     private boolean isValid(Locale locale) {
         try {
             return locale.getISO3Language() != null && locale.getISO3Country() != null;
         } catch (MissingResourceException e) {
             return false;
         }
     }
 
     /**
      * http://stackoverflow.com/questions/3684747/how-to-validate-a-locale-in-java
      * @param locale
      * @return
      */
     private Locale parseLocale(String locale) {
         if(locale == null) {
             throw new IllegalArgumentException("null locale");
         }
 
         String[] parts = locale.split("_");
         switch (parts.length) {
             case 3: return new Locale(parts[0], parts[1], parts[2]);
             case 2: return new Locale(parts[0], parts[1]);
             case 1: return new Locale(parts[0]);
             default: throw new IllegalArgumentException("Invalid locale: " + locale);
         }
     }
 
     /*public ValidModuleParam validate2(String name, Collection<?> values, boolean required) throws ModuleException {
         if(name == null) {
             throw new IllegalArgumentException("null name");
         }
         String value = data.get(name);
         if(required && (value == null || value.length() == 0)) {
             throw new ModuleException("Illegal parameter: " + name);
         }
 
         if(values != null && (value != null && value.length() > 0)) {
             if(!values.contains(value)) {
                 throw new ModuleException("Invalid parameter. name: " + name + ", value: " + value);
             }
         }
 
         return new ValidModuleParam(data, name);
     }*/
 
     public ValidModuleParam exists(String name) throws ModuleException {
         return validate(name, null, true);
     }
 
     public ValidModuleParam validate(String name, String regex, boolean required) throws ModuleException {
         if(name == null) {
             throw new IllegalArgumentException("null name");
         }
         String value = data.get(name);
         if(required && (value == null || value.length() == 0)) {
             throw new ModuleException("Illegal parameter: " + name);
         }
 
         if(regex != null && (value != null && value.length() > 0)) {
             if(!value.matches(regex)) {
                 throw new ModuleException("Invalid parameter. name: " + name + ", value: " + value);
             }
         }
 
         return new ValidModuleParam(data, defaults, name);
     }
 
     @Override
     public int size() {
         return data.size();
     }
 
     @Override
     public boolean isEmpty() {
         return data.isEmpty();
     }
 
     @Override
     public boolean containsKey(Object key) {
         return data.containsKey(key);
     }
 
     public boolean hasValueFor(final Object key) {
         final String v = data.get(key);
         if(v != null) {
             return v.trim().length() > 0;
         }
         return false;
     }
 
     @Override
     public boolean containsValue(Object value) {
         return data.containsValue(value);
     }
 
     @Override
     public String get(Object key) {
         return data.get(key);
     }
 
     @Override
     public String put(String key, String value) {
         return data.put(key, value);
     }
 
     @Override
     public String remove(Object key) {
         return data.remove(key);
     }
 
     @Override
     public void putAll(Map<? extends String, ? extends String> m) {
         data.putAll(m);
     }
 
     @Override
     public void clear() {
         data.clear();
     }
 
     @Override
     public Set<String> keySet() {
         return data.keySet();
     }
 
     public boolean isDefaultProperties(final String name) {
         return defaults.contains(name);
     }
 
     @Override
     public Collection<String> values() {
         return data.values();
     }
 
     @Override
     public Set<Entry<String, String>> entrySet() {
         return data.entrySet();
     }
 }
