 package at.bestsolution.persistence.mybatis.impl;
 
 import java.util.List;
 
 import org.apache.ibatis.reflection.MetaObject;
 import org.apache.ibatis.reflection.factory.ObjectFactory;
 import org.apache.ibatis.reflection.property.PropertyTokenizer;
 import org.apache.ibatis.reflection.wrapper.BeanWrapper;
 import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
 import org.eclipse.emf.ecore.EObject;
 
 public class EMFObjectWrapper implements ObjectWrapper {
 	private BeanWrapper wrapper;
 	private EObject object;
 	
 	public EMFObjectWrapper(MetaObject metaObject, EObject object) {
 		this.wrapper = new BeanWrapper(metaObject, object);
 		this.object = object;
 	}
 	
 	@Override
 	public void add(Object arg0) {
 		wrapper.add(arg0);
 	}
 
 	@Override
 	public <E> void addAll(List<E> arg0) {
 		wrapper.addAll(arg0);
 	}
 
 	@Override
 	public String findProperty(String arg0, boolean arg1) {
 		return wrapper.findProperty(arg0, arg1);
 	}
 
 	@Override
 	public Object get(PropertyTokenizer arg0) {
 		return object.eGet(object.eClass().getEStructuralFeature(arg0.getName()));
 	}
 
 	@Override
 	public String[] getGetterNames() {
 		return wrapper.getGetterNames();
 	}
 
 	@Override
 	public Class<?> getGetterType(String arg0) {
 		return object.eClass().getEStructuralFeature(arg0).getEType().getInstanceClass();
 	}
 
 	@Override
 	public String[] getSetterNames() {
 		return wrapper.getSetterNames();
 	}
 
 	@Override
 	public Class<?> getSetterType(String arg0) {
 		return object.eClass().getEStructuralFeature(arg0).getEType().getInstanceClass();
 	}
 
 	@Override
 	public boolean hasGetter(String arg0) {
 		return wrapper.hasGetter(arg0);
 	}
 
 	@Override
 	public boolean hasSetter(String arg0) {
 		return wrapper.hasSetter(arg0);
 	}
 
 	@Override
 	public MetaObject instantiatePropertyValue(String arg0,
 			PropertyTokenizer arg1, ObjectFactory arg2) {
 		return wrapper.instantiatePropertyValue(arg0, arg1, arg2);
 	}
 
 	@Override
 	public boolean isCollection() {
 		return wrapper.isCollection();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void set(PropertyTokenizer arg0, Object arg1) {
//		EStructuralFeature feature = object.eClass().getEStructuralFeature(arg0.getName());
//		if( feature.isMany() ) {
//			((List<Object>)object.eGet(feature)).addAll((Collection<? extends Object>) arg1);
//		} else {
//			object.eSet(feature, arg1);
//		}
 	}
 
 }
