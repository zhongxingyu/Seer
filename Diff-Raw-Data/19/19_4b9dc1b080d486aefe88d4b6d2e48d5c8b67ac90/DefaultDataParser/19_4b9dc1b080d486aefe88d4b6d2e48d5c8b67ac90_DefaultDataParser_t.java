 package layr.util;
 
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 public class DefaultDataParser implements IConverter {
 
     /* (non-Javadoc)
      * @see layr.binding.IConverter#decode(java.lang.Object, java.lang.Class, java.lang.reflect.Type[])
      */
     @Override
     public Object decode(Object serializedData, Class<?> type, Type[] genericTypes) {
         if (!String.class.isInstance(serializedData))
             return serializedData;
         
         String serializedDataAsString = (String)serializedData;
         
 		if (Collection.class.isAssignableFrom(type))
         	return decodeRawArray(serializedDataAsString, genericTypes[0]);
         
         if (Double.class.isAssignableFrom(type))
             return Double.parseDouble(serializedDataAsString);
 
         else if (Float.class.isAssignableFrom(type))
             return Float.parseFloat(serializedDataAsString);
 
         else if (Boolean.class.isAssignableFrom(type))
             return Boolean.parseBoolean(serializedDataAsString);
 
         else if (Byte.class.isAssignableFrom(type))
             return Byte.parseByte(serializedDataAsString);
 
         else if (Integer.class.isAssignableFrom(type))
             return Integer.parseInt(serializedDataAsString);
 
         else if (Short.class.isAssignableFrom(type))
             return Short.parseShort(serializedDataAsString);
 
         else if (Long.class.isAssignableFrom(type))
             return Long.parseLong(serializedDataAsString);
 
         else if (BigDecimal.class.isAssignableFrom(type))
             return new BigDecimal(serializedDataAsString);
 
         else if (BigInteger.class.isAssignableFrom(type))
             return new BigInteger(serializedDataAsString);
 
         return tryToParseAsJSON(serializedDataAsString, type);
     }
 
 	public Object tryToParseAsJSON(String serializedDataAsString, Class<?> type) {
 		GsonBuilder builder = new GsonBuilder();
 		builder.registerTypeAdapter(Collection.class, new CollectionDeserializer());
 		Gson gson = builder.create();
 		return gson.fromJson(serializedDataAsString, type);
 	}
     
 	public <T> List<T> decodeRawArray( String serializedData, T genericType ) {
     	CollectionDeserializer deserializer = new CollectionDeserializer();
     	JsonElement jsonElement = new JsonParser().parse(serializedData);
     	return (List<T>)deserializer.parseAsArrayList(jsonElement, genericType);
     }
 
 	/* (non-Javadoc)
 	 * @see layr.binding.IConverter#encode(java.lang.Object)
 	 */
 	@Override
 	public String encode(Object object) {
 		if (object == null)
 			return null;
 		else if (String.class.isInstance(object))
             return (String)object;
 		else if (object.getClass().getPackage().getName().equals("java.lang"))
 			return String.valueOf(object);
 
		return encodeAsJSON(object);
	}
	
	public String encodeAsJSON(Object object) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Class.class, new InvalidTypeSerializer());
		Gson gson = builder.create();
		return gson.toJson(object);
 	}
 
 }
