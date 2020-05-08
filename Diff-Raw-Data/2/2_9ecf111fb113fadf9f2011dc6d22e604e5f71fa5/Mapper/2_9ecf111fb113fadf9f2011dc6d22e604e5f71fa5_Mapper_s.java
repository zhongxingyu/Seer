 package com.astrider.sfc.app.lib;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.sql.ResultSet;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.astrider.sfc.app.annotation.Column;
 import com.astrider.sfc.app.model.BaseVo;
 
 /**
  * O/R Mapper.
  * 
  * @author astrider
  *         <p>
  *         Resultset<->ValueObject<->RequestParameterのマッピングを行う。
  *         </p>
  * @param <T>
  *            ValueObject型
  */
 public class Mapper<T extends BaseVo> {
 	private Class<T> type;
 
 	/**
 	 * @param t
 	 *            通常は空のまま利用
 	 *            <p>
 	 *            可変長引数は T型のインスタンスを得るために利用
 	 *            </p>
 	 */
 	@SuppressWarnings("unchecked")
 	public Mapper(T... t) {
 		this.type = (Class<T>) t.getClass().getComponentType();
 	}
 
 	/**
 	 * ResultSetからのMapping.
 	 * 
 	 * @param ResultSet
 	 * @return ValueObject
 	 */
 	public T fromResultSet(ResultSet rs) {
 		T vo = getNewInstance();
 		mapResultSet(rs, vo);
 		return vo;
 	}
 
 	/**
 	 * RequestParametersからのマッピング.
 	 * 
 	 * @param HttpServletRequest
 	 * @return ValueObject
 	 */
 	public T fromHttpRequest(HttpServletRequest request) {
 		T vo = getNewInstance();
 		mapRequestParameters(request, vo);
 		return vo;
 	}
 
 	/**
 	 * @return ValueObjectの新しいインスタンス
 	 *         <p>
 	 *         可変長引数を用いたハックによってインスタンスを生成
 	 *         </p>
 	 */
 	private T getNewInstance() {
 		T obj = null;
 		try {
 			obj = type.newInstance();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return obj;
 	}
 
 	private void mapResultSet(ResultSet rs, T vo) {
 		Field[] declaredFields = vo.getClass().getDeclaredFields();
 
 		// VOに定義されている全フィールドを取得
 		for (Field f : declaredFields) {
 			// Columnアノテーションを取得、アノテーションがなければ無視
 			Column column = f.getAnnotation(Column.class);
 			if (column == null) {
 				continue;
 			}
 
 			String columnName = column.physic();
 			castResultSetValues(f, rs, columnName, vo);
 		}
 	}
 
 	private void mapRequestParameters(HttpServletRequest request, T vo) {
 		Field[] declaredFields = vo.getClass().getDeclaredFields();
 
 		// VOに定義されている前フィールドを取得
 		for (Field f : declaredFields) {
 			// Columnアノテーションを取得、アノテーションがなければ無視
 			Column column = f.getAnnotation(Column.class);
 			if (column == null) {
 				continue;
 			}
 			String parameter = request.getParameter(column.physic());
 			castRequestParameterValues(f, parameter, vo);
 		}
 	}
 
 	/**
 	 * field型に併せてResultSetの中の値をキャストする.
 	 * 
 	 * @param ValueObject内の1フィールド
 	 * @param ResultSet
 	 * @param カラム名
 	 * @param ValueObject
 	 */
 	private void castResultSetValues(Field f, ResultSet rs, String columnName, T vo) {
 		f.setAccessible(true);
 		try {
 			Method method = ResultSet.class.getMethod(getGetterName(f), String.class);
 			f.set(vo, method.invoke(rs, columnName));
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * field型に併せてStringをキャストする.
 	 * 
 	 * @param ValueObject内の1フィールド
 	 * @param RequestParameter
 	 * @param ValueObject
 	 */
 	private void castRequestParameterValues(Field f, String value, T vo) {
 		f.setAccessible(true);
 		Type t = f.getType();
 		try {
 			if (t == String.class)
 				f.set(vo, value);
 			if (t == short.class || t == Short.class)
 				f.set(vo, Short.valueOf(value));
 			if (t == int.class || t == Integer.class)
 				f.set(vo, Integer.valueOf(value));
 			if (t == long.class || t == Long.class)
 				f.set(vo, Long.valueOf(value));
 			if (t == float.class || t == Float.class)
 				f.set(vo, Float.valueOf(value));
 			if (t == double.class || t == Double.class)
 				f.set(vo, Double.valueOf(value));
 			if (t == boolean.class || t == Boolean.class)
 				f.set(vo, Boolean.valueOf(value));
 		} catch (NumberFormatException e) {
			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * resultSetの各データ型に対応したメソッド名を生成.
 	 * 
 	 * @param ValueObject内の1フィールド
 	 * @return メソッド名
 	 */
 	private String getGetterName(Field f) {
 		String type = f.getType().getSimpleName();
 		// ResultSet has no method getInteger()
 		if (type.equals("Integer")) {
 			type = "Int";
 		}
 
 		return "get" + StringUtils.toCamelCase(type);
 	}
 }
