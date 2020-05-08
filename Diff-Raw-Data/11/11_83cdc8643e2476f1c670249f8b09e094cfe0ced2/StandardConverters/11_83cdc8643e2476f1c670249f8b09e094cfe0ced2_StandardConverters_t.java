 package ca.hullabaloo.properties;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Converters are used to tranlate property data (usually a String) into the declared
  * return type of the method that's bound to that property value.
  * <p/>
  * This set of converters is for the basic types String,Integer,Long,Float,Double
  * and the primitive versions of same.
  */
 class StandardConverters {
     private static final Map<Class<?>, Converter> CONVERTERS;
 
     static {
         Map<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>(4);
         converters.put(String.class, new BaseConverter<String>() {
             public String convert(Object value) {
                 return value == null ? null : value.toString();
             }
         });
         converters.put(Integer.class, new BaseConverter<Integer>() {
             public Integer convert(Object value) {
                 if (value == null)
                     return null;
                 if (value instanceof String)
                     return Integer.parseInt((String) value);
                 if (value instanceof Number)
                     return ((Number) value).intValue();
                 throw new ClassCastException("could not convert to Integer " + value.getClass());
             }
         });
         converters.put(Integer.TYPE, new BaseConverter<Integer>() {
             public Integer convert(Object value) {
                 if (value == null)
                     throw new NullPointerException();
                 if (value instanceof String)
                     return Integer.parseInt((String) value);
                 if (value instanceof Number)
                     return ((Number) value).intValue();
                 throw new ClassCastException("could not convert to Integer " + value.getClass());
             }
         });
         converters.put(Double.class, new BaseConverter<Double>() {
             public Double convert(Object value) {
                 if (value == null)
                     return null;
                 if (value instanceof String)
                     return Double.parseDouble((String) value);
                 if (value instanceof Number)
                     return ((Number) value).doubleValue();
                 throw new ClassCastException("could not convert to Double " + value.getClass());
             }
         });
         converters.put(Double.TYPE, new BaseConverter<Double>() {
             public Double convert(Object value) {
                 if (value == null)
                     throw new NullPointerException();
                 if (value instanceof String)
                     return Double.parseDouble((String) value);
                 if (value instanceof Number)
                     return ((Number) value).doubleValue();
                 throw new ClassCastException("could not convert to Double " + value.getClass());
             }
         });
         converters.put(Long.class, new BaseConverter<Long>() {
             public Long convert(Object value) {
                 if (value == null)
                     return null;
                 if (value instanceof String)
                     return Long.parseLong((String) value);
                 if (value instanceof Number)
                     return ((Number) value).longValue();
                 throw new ClassCastException("could not convert to Long " + value.getClass());
             }
         });
         converters.put(Long.TYPE, new BaseConverter<Long>() {
             public Long convert(Object value) {
                 if (value == null)
                     throw new NullPointerException();
                 if (value instanceof String)
                     return Long.parseLong((String) value);
                 if (value instanceof Number)
                     return ((Number) value).longValue();
                 throw new ClassCastException("could not convert to Long " + value.getClass());
             }
         });
         converters.put(Float.class, new BaseConverter<Float>() {
             public Float convert(Object value) {
                 if (value == null)
                     return null;
                 if (value instanceof String)
                     return Float.parseFloat((String) value);
                 if (value instanceof Number)
                     return ((Number) value).floatValue();
                 throw new ClassCastException("could not convert to Float " + value.getClass());
             }
         });
         converters.put(Float.TYPE, new BaseConverter<Float>() {
             public Float convert(Object value) {
                 if (value == null)
                     throw new NullPointerException();
                 if (value instanceof String)
                     return Float.parseFloat((String) value);
                 if (value instanceof Number)
                     return ((Number) value).floatValue();
                 throw new ClassCastException("could not convert to Float " + value.getClass());
             }
         });
 
         CONVERTERS = converters;
     }
 
     public static Converter all() {
         return new Converter() {
             public boolean supportsType(Class<?> type) {
                 return CONVERTERS.containsKey(type);
             }
 
            public <T> T convert(Object object, Class<T> targetType) {
                 return CONVERTERS.get(targetType).convert(object, targetType);
             }
         };
     }
 }
