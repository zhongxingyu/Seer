 package net.moetang.nekocore.ioc.container;
 
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.moetang.nekocore.ioc.IocContainer;
 import net.moetang.nekocore.ioc.annotation.DI;
 import net.moetang.nekocore.ioc.annotation.ResourceBean;
 import net.moetang.nekocore.ioc.subcore.SimpleRegister;
 
 /**
  * 只提供基本的annotation依赖注入服务<br/>
  * 所有实例均为单例<br />
  * 没有unregister的实现<br />
  * 使用时请使用SimpleRegister接口<br />
  * 标注资源使用ResourceBean注解<br />
  * 这里只关心的是Bean的: 名称<br />
  * 注册Bean的时候,如果采用Class方式,并且名称没有指定(ResouceBean或参数中指定),那么名称将为第一个实现的接口全称,如果没有实现的接口,那么则为类全称<br />
  * 如果上一条中指定了名称,则将不会注册接口或类名<br />
  * 使用DI注解标识注入点.<br />
  * DI注解到：1、如果实现类有接口，则注解到第一个接口类型的成员上，2、如果实现类没有接口，则注解到相应类型的成员上<br />
  *
  */
 public class SimpleAnnotationIocContainer implements IocContainer, SimpleRegister, Serializable {
 	private static final long serialVersionUID = -7677381501973211745L;
 
 	private transient Map<String, Object> preReg = new HashMap<String, Object>();
 	private Map<String, Object> beans = new HashMap<>();
 
 	public <T> T get(String name, Class<T> clazz) {
 		Object o = beans.get(name);
 		try {
 			return clazz.cast(o);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void register(Object o, EnumMap<RegParam, String> param) {
 		String name = param.get(RegParam.NAME);
 		String objType = param.get(RegParam.OBJ_TYPE);
 		if(objType == null || objType.equals("obj")){
 			if(name == null || name.length() == 0)
 				return ;
 			register(name, o);
 		}else if(objType.equals("class")){
 			if(name == null || name.length() == 0)
 				register(((Class<?>)o));
 			else
 				register(((Class<?>)o), name);
 		}
 	}
 
 	public void unregister(EnumMap<RegParam, String> param) {
 	}
 
 	public void init() {
 	}
 
 	public void prepare() {
 		for(Entry<String, Object> entry : preReg.entrySet()){
 			beans.put(entry.getKey(), this.analyseObjectDependency(entry.getValue(), preReg));
 		}
 		preReg.clear();
 	}
 
 	public void destroy() {
 	}
 
 	public void register(String name, Object o) {
 		preReg.put(name, o);
 	}
 
 	public void register(Class<?> clazz) {
 		try {
 			ResourceBean anno = clazz.getAnnotation(ResourceBean.class);
 			if(anno == null)
 				return ;
 
 			String name = null;
 			if(anno.value().length() != 0){
 				name = anno.value();
 			}else{
 				Class<?>[] cs = clazz.getInterfaces();
 				if(cs == null || cs.length == 0){
 					name = clazz.getName();
 				}else{
 					Class<?> ifclazz = cs[0];
 					name = ifclazz.getName();
 				}
 			}
 
 			Object o = clazz.newInstance();
 			register(name, o);
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void register(Class<?> clazz, String name) {
 		try {
 			Object o = clazz.newInstance();
 			register(name, o);
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void unregister(String name) {
 	}
 
 	public void unregister(Class<?> clazz) {
 	}
 	
 	protected final <T> T analyseObjectDependency(T obj, Map<String, Object> srcMap){
 		Class<?> clazz = obj.getClass();
 		do{
 			Field[] fields = clazz.getDeclaredFields();
 			for(Field field : fields){
 				DI anno = field.getAnnotation(DI.class);
 				if(anno != null){
 					try {
 						this.fieldInjection(obj, field, anno, srcMap);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			clazz = this.getParentClass(clazz);
 		}while(clazz != null);
 		
 		return obj;
 	}
 	
 	protected <T> T analyseObjectDependency(T obj){
 		return this.analyseObjectDependency(obj, beans);
 	}
 	protected final void fieldInjection(Object obj, Field field, DI anno, Map<String, Object> srcMap) throws IllegalArgumentException, IllegalAccessException{
 		String name = null;
 		if(anno.value().length() != 0){
 			name = anno.value();
 		}else{
 			name = field.getType().getName();
 		}
 		Object injObj = srcMap.get(name);
 		if(injObj != null){
 			try {
				field.setAccessible(true);
 				field.set(obj, injObj);
				field.setAccessible(false);
 			} catch (SecurityException | IllegalAccessException e) {
 				// if security manager refuse to modify the accessible parameter to fields
 				// then
 				try {
 					String setFieldName = "set"+field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
 					obj.getClass().getMethod(setFieldName, injObj.getClass());
				} catch (NoSuchMethodException | SecurityException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 	protected Class<?> getParentClass(Class<?> clazz){
 		return clazz.getSuperclass();
 	}
 
 	@Override
 	public <T> T inject(T obj) {
 		return this.analyseObjectDependency(obj);
 	}
 
 }
