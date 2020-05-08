 package org.eweb4j.ioc;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eweb4j.cache.IOCConfigBeanCache;
 import org.eweb4j.cache.SingleBeanCache;
 import org.eweb4j.config.Log;
 import org.eweb4j.config.LogFactory;
 import org.eweb4j.ioc.config.IOCConfigConstant;
 import org.eweb4j.ioc.config.bean.IOCConfigBean;
 import org.eweb4j.ioc.config.bean.Injection;
 import org.eweb4j.util.ReflectUtil;
 
 /**
  * IOC Bean工厂，负责生产出各种各样的bean 按照配置文件配置信息进行bean的生产 服从依赖注入
  * <b>目前支持的功能非常单薄，仅能注入基本类型和自定义类类型，集合类暂时不支持</b>
  * 
  * @author cfuture.aw
  * @since v1.a.0
  */
 @SuppressWarnings("all")
 public class IOC {
 	private static Log log = LogFactory.getIOCLogger(IOC.class);
 
 	/**
 	 * 查找beanID的bean是否存在
 	 * 
 	 * @param beanID
 	 * @return
 	 */
	public static boolean containsBean(String beanID) {
 		return IOCConfigBeanCache.containsKey(beanID);
 	}
 
 	/**
 	 * 查看beanID的bean是什么类型
 	 * 
 	 * @param beanID
 	 * @return
 	 * @throws Exception
 	 */
	public Class<?> getType(String beanID) throws Exception {
 		return IOCConfigBeanCache.get(beanID).getClass();
 	}
 
 	/**
 	 * 查看beanID的bean生命周期是否是原型
 	 * 
 	 * @param beanID
 	 * @return
 	 * @throws Exception
 	 */
	public boolean isPrototype(String beanID) throws Exception {
 		return IOCConfigConstant.PROTOTYPE_SCOPE.equals(IOCConfigBeanCache.get(
 				beanID).getScope());
 	}
 
 	/**
 	 * 查看beanID的生命周期是否是单件
 	 * 
 	 * @param beanID
 	 * @return
 	 * @throws Exception
 	 */
	public boolean isSingleton(String beanID) throws Exception {
 		return IOCConfigConstant.SINGLETON_SCOPE.equals(IOCConfigBeanCache.get(
 				beanID).getScope());
 	}
 
 	/**
 	 * 查看beanID和targetType是否相符
 	 * 
 	 * @param beanID
 	 * @param targetType
 	 * @return
 	 * @throws Exception
 	 */
	public boolean isTypeMatch(String beanID, Class<?> targetType)
 			throws Exception {
 		return targetType.equals(IOCConfigBeanCache.get(beanID).getClass());
 	}
 
 	/**
 	 * 生产出符合beanID名字的bean
 	 * 
 	 * @param <T>
 	 * @param beanID
 	 * @return
 	 * @throws Exception
 	 */
	public static <T> T getBean(String beanID) {
 		if (!containsBean(beanID)) {
 			return null;
 		}
 		// 声明用来返回的对象
 		T t = null;
 		try {
			// 声明构造方法参数列表的初始化值
 			Object[] initargs = null;
 			// 声明构造方法参数列表
 			Class<?>[] args = null;
 			List<Object> initargList = new ArrayList<Object>();
 			List<Class<?>> argList = new ArrayList<Class<?>>();
 			
 			// setters cache
 			Map<String, Object> properties = new Hashtable<String, Object>();
 			
 			// 遍历配置文件，找出beanID的bean
 			if (IOCConfigBeanCache.containsKey(beanID)) {
 				IOCConfigBean iocBean = IOCConfigBeanCache.get(beanID);
 				// 取出该bean的类型，便于最后使用反射调用构造方法实例化
 				Class<T> clazz = (Class<T>) Class.forName(iocBean.getClazz());
 				// 判断该bean的生命周期
 				if (IOCConfigConstant.SINGLETON_SCOPE.equalsIgnoreCase(iocBean.getScope())) {
 					// 如果是单件，就从单件缓存池中取
 					if (SingleBeanCache.containsKey(beanID)) {
 						t = (T) SingleBeanCache.get(beanID);
 						return t;
 					}
 				}
 
 				// 遍历每个bean的注入配置
 				for (Iterator<Injection> it = iocBean.getInject().iterator(); it.hasNext();) {
 					Injection inj = it.next();
 					if (inj == null)
 						continue;
 					String ref = inj.getRef();
 					if (ref != null && !"".equals(ref)) {
 						// 如果ref不为空，说明注入的是对象类型，后面需要进入递归
 						String name = inj.getName();
 						if (name != null && !"".equals(name)) {
 							// 如果属性名字不为空，说明使用的是setter注入方式
 							properties.put(name, getBean(ref));
 
 						} else {
 							// 如果属性名字为空，说明使用的是构造器注入方式
 							// 使用构造器注入的时候，需要按照构造器参数列表顺序实例化
 							Object obj = getBean(ref);
 							initargList.add(obj);
 							String type = inj.getType();
 							if (type != null && type.trim().length() > 0){
 								Class<?> cls = Class.forName(type);
 								argList.add(cls);
 							}else{
 								argList.add(obj.getClass());
 							}
 						}
 					} else {
 						// 注入基本类型
 						String type = inj.getType();
 						String value = inj.getValue();
 						if (value == null) 
 							value = "";
 						
 						String name = inj.getName();
 						if (name != null && !"".equals(name)) {
 							// 如果属性名字不为空，说明使用的是setter注入方式
 							if (IOCConfigConstant.INT_ARGTYPE.equalsIgnoreCase(type)|| "java.lang.Integer".equalsIgnoreCase(type)) {
 								if ("".equals(value.trim()))
 									value = "0";
 
 								// int
 								properties.put(name, Integer.parseInt(value));
 							} else if (IOCConfigConstant.STRING_ARGTYPE.equalsIgnoreCase(type) || "java.lang.String".equalsIgnoreCase(type)) {
 								// String
 								properties.put(name, value);
 							} else if (IOCConfigConstant.LONG_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Long".equalsIgnoreCase(type)) {
 								// long
 								if ("".equals(value.trim()))
 									value = "0";
 
 								properties.put(name, Long.parseLong(value));
 							} else if (IOCConfigConstant.FLOAT_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Float".equalsIgnoreCase(type)) {
 								// float
 								if ("".equals(value.trim()))
 									value = "0.0";
 
 								properties.put(name, Float.parseFloat(value));
 							} else if (IOCConfigConstant.BOOLEAN_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Boolean".equalsIgnoreCase(type)) {
 								// boolean
 								if ("".equals(value.trim())) 
 									value = "false";
 								
 								properties.put(name, Boolean.parseBoolean(value));
 							} else if (IOCConfigConstant.DOUBLE_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Double".equalsIgnoreCase(type)) {
 								// double
 								if ("".equals(value.trim())) 
 									value = "0.0";
 								
 								properties.put(name, Double.parseDouble(value));
 							}
 						} else {
 							// 如果属性名字为空，说明使用的是构造器注入方式
 							// 使用构造器注入的时候，需要按照构造器参数列表顺序实例化
 							if (IOCConfigConstant.INT_ARGTYPE.equalsIgnoreCase(type)|| "java.lang.Integer".equalsIgnoreCase(type)) {
 								// int
 								if ("".equals(value.trim())) 
 									value = "0";
 								
 								argList.add(int.class);
 								initargList.add(Integer.parseInt(value));
 							} else if (IOCConfigConstant.LONG_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Long".equalsIgnoreCase(type)) {
 								// long
 								if ("".equals(value.trim())) 
 									value = "0";
 								
 								argList.add(long.class);
 								initargList.add(Long.parseLong(value));
 							} else if (IOCConfigConstant.FLOAT_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Float".equalsIgnoreCase(type)) {
 								// float
 								if ("".equals(value.trim())) 
 									value = "0.0";
 								
 								argList.add(float.class);
 								initargList.add(Float.parseFloat(value));
 							} else if (IOCConfigConstant.BOOLEAN_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Boolean".equalsIgnoreCase(type)) {
 								// boolean
 								if ("".equals(value.trim())) 
 									value = "false";
 								
 								argList.add(boolean.class);
 								initargList.add(Boolean.parseBoolean(value));
 							} else if (IOCConfigConstant.DOUBLE_ARGTYPE.equalsIgnoreCase(type) || "java.lang.Double".equalsIgnoreCase(type)) {
 								// double
 								if ("".equals(value.trim())) 
 									value = "0.0";
 								
 								argList.add(double.class);
 								initargList.add(Double.parseDouble(value));
 							} else if (IOCConfigConstant.STRING_ARGTYPE.equalsIgnoreCase(type) || "java.lang.String".equalsIgnoreCase(type)) {
 								// String
 								argList.add(String.class);
 								initargList.add(value);
 							}
 						}
 					}
 				}
 				// 如果构造方法参数列表不为空，说明需要使用构造方法进行注入
 				if (argList.size() > 0 && initargList.size() > 0) {
 					args = new Class<?>[argList.size()];
 					initargs = new Object[initargList.size()];
 					for (int i = 0; i < argList.size(); i++) {
 						args[i] = argList.get(i);
 						initargs[i] = initargList.get(i);
 					}
 
 					t = clazz.getDeclaredConstructor(args).newInstance(initargs);
 				} else if (t == null){
 					t = clazz.newInstance();
 					for (Iterator<Entry<String, Object>> it = properties.entrySet().iterator(); it.hasNext();) {
 						Entry<String, Object> e = it.next();
 						final String name = e.getKey();
 						ReflectUtil ru = new ReflectUtil(t);
 						Method m = ru.getSetter(name);
 						if (m == null)
 							continue;
 						
 						m.invoke(t, e.getValue());
 					}
 				}
 				
 				// 判断该bean的生命周期
 				if (IOCConfigConstant.SINGLETON_SCOPE.equalsIgnoreCase(iocBean.getScope())) {
 					// 如果是单件，就从单件缓存池中取
 					if (!SingleBeanCache.containsKey(beanID)) {
 						SingleBeanCache.add(beanID, t);
 					}
 				}
 			}
 		} catch (Exception e) {
 			log.error("IOC.getBean(" + beanID + ") failed!", e);
 		}
 
 		String info = "IOC.getBean(" + beanID + ") ——> " + t;
 		log.debug(info);
 
 		return t;
 	}
 }
