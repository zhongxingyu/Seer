 package org.jackie.compiler.jmodelimpl.annotations;
 
 import static org.jackie.compiler.util.Context.context;
 import org.jackie.jmodel.JPrimitive;
 import org.jackie.utils.Assert;
 
 import java.lang.annotation.Annotation;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Patrik Beno
  */
 public class AnnotationProxy implements InvocationHandler {
 
 	protected AnnotationImpl annotation;
 
 	protected Class<? extends Annotation> _class;
 
 	protected WeakReference<Map<String, Object>> _cache;
 
 	public AnnotationProxy(AnnotationImpl annotation) {
 		this.annotation = annotation;
 	}
 
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		if (isInheritedMethod(method)) {
 			return method.invoke(annotation(), args);
 		}
 
 		String name = method.getName();
 		Class<?> type = method.getReturnType();
 
 		Object value = cache().get(name);
 		if (value != null) {
 			return value;
 		}
 
 		value = buildValue(name, type);
 		cache().put(name, value);
 
 		return value; // todo Assert.typecast(value, type);
 	}
 
 	protected boolean isInheritedMethod(Method m) {
 		boolean inherited = false;
 		//noinspection ConstantConditions
 		inherited |= m.getDeclaringClass().equals(Annotation.class);
 		inherited |= m.getDeclaringClass().equals(Object.class);
 		return inherited;
 	}
 
 	protected Annotation annotation() {
 		return new Annotation() {
 			public int hashCode() {
 				return super.hashCode(); // todo implement this
 			}
 
 			public boolean equals(Object obj) {
 				return super.equals(obj); // todo implement this
 			}
 
 			public String toString() {
 				return super.toString(); // todo implement this
 			}
 
 			public Class<? extends Annotation> annotationType() {
 				return clazz();
 			}
 		};
 	}
 
 	protected Class<? extends Annotation> clazz() {
 		if (_class != null) {
 			return _class;
 		}
 		//noinspection unchecked
 		_class = load(annotation.type.jclass.getFQName());
 		return _class;
 	}
 
 	protected Map<String, Object> cache() {
 		Map<String, Object> c = (_cache != null) ? _cache.get() : null;
 		if (c != null) { return c; }
 
 		c = new HashMap<String, Object>();
 		_cache = new WeakReference<Map<String, Object>>(c);
 
 		return c;
 	}
 
 	protected Class load(String clsname) {
 		try {
 			return Class.forName(clsname, false, context().annotationClassLoader());
 		} catch (ClassNotFoundException e) {
 			throw Assert.notYetHandled(e);
 		}
 	}
 
 	protected Object buildValue(String name, Class<?> type) {
 		AnnotationAttributeImpl attrdef = annotation.type.getAttributeImpl(name);
 		AnnotationAttributeValueImpl attr = annotation.getAttributeValue(attrdef);
 		if (attr == null) {
 			attr = attrdef.defaultValue;
 		}
 		return convert(attr.value, type);
 	}
 
 	protected Object convert(Object value, Class type) {
 		if (type.isPrimitive()) {
 			assert JPrimitive.isObjectWrapper(type);
 			return value;
 
 		} else if (String.class.equals(type)) {
 			assert value instanceof String;
 			return value;
 
 		} else if (Class.class.equals(type)) {
 			assert value instanceof String;
 			return load((String) value);
 
 		} else if (type.isEnum()) {
 			assert value instanceof String;
 			//noinspection unchecked
 			return Enum.valueOf(type, (String) value);
 
 		} else if (type.isAnnotation()) {
 			return new AnnotationProxy((AnnotationImpl) value);
 
 		} else if (type.isArray()) {
 			return convertArray(value, type);
 
 		} else {
 			throw Assert.invariantFailed("Unhandled type: %s", type);
 		}
 	}
 
 	protected Object convertArray(Object value, Class type) {
 		List list = (List) value;
 		Object array = Array.newInstance(type.getComponentType(), list.size());
 		for (int i=0; i<list.size(); i++) {
 			Object o = list.get(i);
 			Object converted = convert(o, type.getComponentType());
			Array.set(array, i, converted); // fixme possible bug - modifies original array
 		}
 		return array;
 	}
 
 
 
 
 }
