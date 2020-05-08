 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.sql;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import zcu.xutil.Objutil;
 import zcu.xutil.sql.handl.BeanRow;
 import zcu.xutil.sql.handl.FirstField;
 import zcu.xutil.utils.LRUCache;
 import zcu.xutil.utils.ProxyHandler;
 import zcu.xutil.utils.Util;
 import static zcu.xutil.utils.Util.getParameterType;
 import static zcu.xutil.Objutil.validate;
 
 public final class DaoProxy implements InvocationHandler {
 	private static final LRUCache<Class, Map<Method, Define>> cache = new LRUCache<Class, Map<Method, Define>>(95, null);
 	private final DBTool dbtool;
 	private final Map<Method, Define> maps;
 
 	public DaoProxy(DBTool tool, Class daoIface) {
 		dbtool = tool;
 		Map<Method, Define> defs = cache.get(daoIface);
 		if (defs == null) {
 			defs = new HashMap<Method, Define>();
 			for (Method m : daoIface.getDeclaredMethods())
 				defs.put(m, new Define(m));
 			defs = Objutil.ifNull(cache.putIfAbsent(daoIface, defs), defs);
 		}
 		maps = defs;
 	}
 
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		Object ret = ProxyHandler.proxyObjectMethod(proxy, method, args);
 		if (ret != null)
 			return ret;
 		return maps.get(method).invoke(dbtool, method, args);
 	}
 
 	private static final class Define {
 		private final NpSQL npsql;
 		private String[] maparams;
 		private boolean collection;
 		private boolean hasOptions;
 		private boolean batch;
 		private ResultHandler rh;
 		private String idGetter;
 
 		@SuppressWarnings("unchecked")
 		Define(Method m) {
 			int length = Util.getParamsLength(m);
 			Select select = m.getAnnotation(Select.class);
 			Class<?> retype = m.getReturnType(), clazz;
 			if (select != null) {
 				npsql = new NpSQL(select.value());
 				if (retype == Collection.class || retype == List.class) {
 					collection = true;
 					if (length > 0 && QueryOptions.class.isAssignableFrom(getParameterType(m, length - 1))) {
 						length--;
 						hasOptions = true;
 					}
 					clazz = (Class) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
 				} else
 					clazz = retype;
 				rh = sqltypes(clazz) ? FirstField.get(clazz) : new BeanRow(clazz);
 				if (length == 0 || sqltypes(getParameterType(m, 0)))
 					maparams = new String[length];
 			} else {
 				Update update = Objutil.notNull(m.getAnnotation(Update.class), "absent @Select or @Update: {}", m);
 				npsql = new NpSQL(update.value());
 				if (length == 0)
 					maparams = new String[0];
 				else {
 					if (Object[].class.isAssignableFrom(clazz = getParameterType(m, 0))) {
 						batch = true;
 						clazz = clazz.getComponentType();
 					}
 					if (sqltypes(clazz))
 						maparams = new String[length];
 				}
 				if (retype != void.class) {
 					if (batch)
 						validate(retype == int[].class, "batch return void or int[], {}", m);
 					else if (!npsql.insert)
 						validate(retype == int.class, "update return void or int, {}", m);
 					else {
 						validate(maparams != null, "entity insert return void, {}", m);
 						ID id = Objutil.notNull(m.getAnnotation(ID.class), "mark @ID or return void. {}", m);
 						idGetter = id.value();
 					}
 				}
 			}
			if (maparams != null) {
 				int unnamed = -1;
 				Annotation[][] pas = m.getParameterAnnotations();
 				HashSet<String>  names = npsql.createNamesSet();
 				outer: while (--length >= 0) {
 					for (Annotation a : pas[length]) {
 						if (a instanceof Param) {
 							if(names.remove(maparams[length] = ((Param) a).value()))
 								continue outer;
 							throw new IllegalArgumentException(m + ". name not in SQL or repeat: " + maparams[length]); 
 						}
 					}
 					validate(unnamed < 0, "multi params not annotated. {}", m);
 					unnamed = length;
 				}
 				if (unnamed >= 0){
 					if(names.size() != 1)
 						throw new IllegalArgumentException(m + ". absent @Param. " + names); 
 					maparams[unnamed] = names.iterator().next();
 				}else if(!names.isEmpty())
 					throw new IllegalArgumentException(m + ". remain params in SQL. " + names); 
 			}
 		}
 
 		private boolean sqltypes(Class c) {
 			return SQLType.get(c) != null || c.isEnum() || Blob.class.isAssignableFrom(c)
 					|| Clob.class.isAssignableFrom(c);
 		}
 
 		@SuppressWarnings("unchecked")
 		Object invoke(DBTool dbtool, Method method, Object[] args) throws SQLException {
 			if (rh != null) {
 				Handler h = collection ? rh.list(hasOptions ? (QueryOptions) args[args.length - 1] : null) : rh;
 				if (maparams == null)
 					return dbtool.entityQuery(npsql, h, args[0]);
 				return dbtool.mapQuery(npsql, h, getMapParams(method, args));
 			}
 			int iret;
 			Class retype = method.getReturnType();
 			if (maparams != null) {
 				if (batch) {
 					int[] ret = dbtool.mapBatch(npsql, getMapArray(method, args));
 					return retype == void.class ? null : ret;
 				}
 				Map<String, Object> map = getMapParams(method, args);
 				iret = dbtool.mapUpdate(npsql, map);
 				if (idGetter != null)
 					return map.get("");
 			} else if (batch) {
 				int[] ret = dbtool.entityBatch(npsql, (Object[]) args[0]);
 				return retype == void.class ? null : ret;
 			} else
 				iret = dbtool.entityUpdate(npsql, args[0]);
 			return retype == void.class || npsql.insert ? null : iret;
 		}
 
 		@SuppressWarnings("unchecked")
 		private Map<String, Object>[] getMapArray(Method method, Object[] args) {
 			int rows = ((Object[]) args[0]).length;
 			Map[] maps = new Map[rows];
 			while (--rows >= 0) {
 				int columns = args.length;
 				Object[] params = new Object[columns];
 				while (--columns >= 0)
 					params[columns] = ((Object[]) args[columns])[rows];
 				maps[rows] = getMapParams(method, params);
 			}
 			return maps;
 		}
 
 		private Map<String, Object> getMapParams(Method method, Object[] args) {
 			Map<String, Object> map;
 			if (idGetter != null) {
 				EntityMap<Object> emap = new EntityMap<Object>();
 				emap.setGenerator(new IDGenerator("", method.getReturnType(), idGetter));
 				map = emap;
 			} else if (maparams.length == 0)
 				return Collections.emptyMap();
 			else
 				map = new HashMap<String, Object>();
 			int i = maparams.length;
 			while (--i >= 0) {
 				Object o = args[i];
 				map.put(maparams[i], o == null ? getParameterType(method, i) : o);
 			}
 			return map;
 		}
 	}
 }
