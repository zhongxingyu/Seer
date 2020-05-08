 package thewebsemantic;
 
 import java.beans.PropertyDescriptor;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.ParameterizedType;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.rdf.model.Model;
 
 class PropertyContext extends ValuesContext {
 	
 	private Log logger = LogFactory.getLog(getClass());
 	PropertyDescriptor property;
 	TypeWrapper type;
 	boolean idmethod = false;
 
 
 	public PropertyContext(Object bean, PropertyDescriptor p) {
 		subject = bean;
 		property = p;
 		type = TypeWrapper.type(bean);
 	}
 
 
 	public PropertyContext(Object bean, PropertyDescriptor p, boolean b) {
 		this(bean, p);
 		idmethod = b;
 	}
 	
 	
 	@Override
 	public boolean isAnnotatedBy(Class<? extends Annotation> annotationClass) {
 		return property.getReadMethod().isAnnotationPresent(annotationClass);
 	}
 	
 	
 	@Override
 	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
 		return property.getReadMethod().getAnnotation(annotationClass);
 	}
 
 
 	@Override
 	public String uri() {
 		return type.uri(property.getReadMethod(), property.getName());
 	}
 
 
 	@Override
 	public boolean existsInModel(Model m) {
 		return m.getGraph().contains(Node.createURI(uri()), Node.ANY, Node.ANY);
 	}
 
 
 	@Override
 	public Object invokeGetter() {
 		Object result = null;
 		try {
 			result = property.getReadMethod().invoke(subject);
 		} catch (Exception e) {
 			logger.warn("Error calling read method.", e);
 		}
 		return result;
 	}
 
 
 	@Override
 	public void setProperty(Object v) {
 		try {
 			property.getWriteMethod().invoke(subject, v);
 		} catch (Exception e) {
 			logger.warn("Error calling write method.", e);
 		}
 	}
 
 
 	@Override
 	public boolean isPrimitive() {
 		return PrimitiveWrapper.isPrimitive(property.getPropertyType());
 	}
 
 
 	@Override
 	public Class<?> type() {
 		return property.getPropertyType();
 	}
 
 
 	@Override
 	public String getName() {
 		return property.getName();
 	}
 
 	
 	@Override
 	public Class<?> t() {
		ParameterizedType type = (ParameterizedType) property.getReadMethod()
				.getGenericReturnType();
		return getGenericType(type);
 	}
 
 
 	@Override
 	public boolean isId() {
 		return idmethod;
 	}
 
 
 	@Override
 	public AccessibleObject getAccessibleObject() {
 		return property.getReadMethod();
 	}
 	
 	
 	public TypeWrapper tw() {
 		return type;
 	}
 
 
 }
