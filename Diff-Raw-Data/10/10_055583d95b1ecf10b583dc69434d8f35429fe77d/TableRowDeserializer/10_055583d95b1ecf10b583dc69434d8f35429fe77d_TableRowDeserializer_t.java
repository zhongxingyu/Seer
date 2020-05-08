 package edu.swmed.qbrc.jacksonate.rest.jackson.deserializers;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.ObjectCodec;
 import org.codehaus.jackson.map.DeserializationContext;
 import org.codehaus.jackson.map.JsonDeserializer;
 
 import edu.swmed.qbrc.jacksonate.rest.jackson.BeanInfoJSON;
 import edu.swmed.qbrc.jacksonate.rest.jackson.BeanPropertyJSON;
 import edu.swmed.qbrc.jacksonate.rest.jackson.ReflectionFactory;
 
 public class TableRowDeserializer<T> extends JsonDeserializer<T> {
 
 	private final ReflectionFactory reflectionFactory;
 	private final Class<?> clazz;
 	
 	public TableRowDeserializer(final Class<?> clazz, ReflectionFactory reflectionFactory) {
 		this.clazz = clazz;
 		this.reflectionFactory = reflectionFactory;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public T deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
 		ObjectCodec oc = parser.getCodec();
 		JsonNode node = oc.readTree(parser);
 
 		Object toReturn = null;
 		try {
 			toReturn = (T)clazz.newInstance();
 		} catch (InstantiationException e1) {
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			e1.printStackTrace();
 		}
 		
 		BeanInfoJSON info = reflectionFactory.getBeanInfo(clazz);
 		Integer index = 0;
 		if (info != null && toReturn != null) {
 			for (BeanPropertyJSON pd : info.getProperties()) {
 				if (!pd.getJSONTransient() && pd.getReadable() && pd.getWriteable()) {
 					try {
 						pd.getPropertyDescriptor().getWriteMethod().invoke(toReturn, getNodeValue(pd.getPropertyDescriptor().getPropertyType(), node.get(index++)));
 					} catch (InvocationTargetException e) {
 						System.out.println("Error writing property " + pd.getPropertyDescriptor().getName() + ": " + getNodeValue(pd.getPropertyDescriptor().getPropertyType(), node.get(index-1)));
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						System.out.println("Error writing property " + pd.getPropertyDescriptor().getName() + ": " + getNodeValue(pd.getPropertyDescriptor().getPropertyType(), node.get(index-1)));
 						e.printStackTrace();
 					}
 				}
 			}
 		}		
 
 		return (T)toReturn;
 	}
 	
 	private Object getNodeValue(Class<?> propertyType, JsonNode node) {
 		if (propertyType.equals(String.class))
 			return node.getTextValue();
		else if (propertyType.equals(Integer.class) && node.isNumber() && !node.isNull())
 			return node.getIntValue();
 		else if (propertyType.equals(Short.class) && node.isNumber() && !node.isNull())
 			return node.getNumberValue().shortValue();
		else if (propertyType.equals(Boolean.class) && node.isBoolean() && !node.isNull())
			return node.getBooleanValue();
 		else if (propertyType.equals(Float.class) && node.isNumber() && ! node.isNull())
 			return (float)node.getNumberValue().doubleValue();
 		else if (propertyType.equals(Double.class) && node.isNumber() && ! node.isNull())
 			return node.getNumberValue().doubleValue();
 		
 		return null;
 	}
 	
 }
