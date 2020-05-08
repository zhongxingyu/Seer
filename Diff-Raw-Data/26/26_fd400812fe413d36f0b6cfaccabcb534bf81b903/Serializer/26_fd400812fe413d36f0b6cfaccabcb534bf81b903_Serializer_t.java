 package com.example.swp_ucd_2013_eule.model;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.example.swp_ucd_2013_eule.net.APIException;
 import com.example.swp_ucd_2013_eule.net.HttpJsonClient.Response;
 
 public class Serializer<T extends Model> {
 
 	/**
 	 * serialization of list fields (friends, items, ... ) not necessary nested
 	 * objects are serialized using their primary keys
 	 * 
 	 * @param model
 	 * @return
 	 * @throws APIException
 	 */
 	public JSONObject serialize(T model) throws APIException {
 		JSONObject json = new JSONObject();
 		for (String name : this.getSimplePropertyNames(model)) {
 			/**
 			 * serialize "simple" fields
 			 */
 			Object o = this.invokeGet(model, name);
 			try {
 				json.put(name, o);
 			} catch (JSONException e) {
 				System.out.println("SERIALIZER - deserialize(model, json) "
 						+ e.getClass() + ": " + e.getMessage());
 			}
 
 		}
 		return json;
 	}
 
 	/**
 	 * 
 	 * @param skeleton
 	 * @param json
 	 * @return
 	 * @throws APIException
 	 */
 	public T deserialize(T skeleton, JSONObject json) throws APIException {
 		System.out.println("SERIALIZER - deserialize(model, json) "
 				+ json.toString());
 		for (String name : this.getSimplePropertyNames(skeleton)) {
 			/**
 			 * deserialize "simple" fields
 			 */
 			Object value = null;
 			try {
 				value = json.get(name);
 				this.invokeSet(skeleton, name, value);
 			} catch (JSONException e) {
 				System.out.println("SERIALIZER - deserialize(model, json) "
 						+ e.getClass() + ": " + e.getMessage());
 			}
 		}
 		return skeleton;
 	}
 
 	/**
 	 * deserialize list fields through calling the api
 	 * 
 	 * @param list
 	 * @return
 	 */
 	public List<T> deserializeList(T skeleton, Response response)
 			throws APIException {
 		List<T> models = new ArrayList<T>();
 		JSONObject json = response.getJsonResponse();
 		System.out.println("SERIALIZER - deserializeList(skeleton, response) "
 				+ json.toString());
 		try {
 			for (int i = 0; i < json.getJSONArray("results").length(); i++) {
 				T instance = (T) Class.forName(skeleton.getClass().getName())
 						.newInstance();
 				models.add(deserialize(instance, json.getJSONArray("results")
 						.getJSONObject(i)));
 			}
 		} catch (JSONException e) {
 			throw new APIException(e.getMessage());
 		} catch (IllegalAccessException e) {
 			Log.e("SERIALIZER", e + ":" + e.getMessage());
 			throw new APIException(e.getMessage());
 		} catch (InstantiationException e) {
 			Log.e("SERIALIZER", e + ":" + e.getMessage());
 			throw new APIException(e.getMessage());
 		} catch (ClassNotFoundException e) {
 			Log.e("SERIALIZER", e + ":" + e.getMessage());
 			throw new APIException(e.getMessage());
 		}
 		return models;
 	}
 
 	/**
 	 * deserialize list fields through calling the api
 	 * 
 	 * @param list
 	 * @return
 	 */
 	public List<T> deserializeList(T skeleton, Response response,
 			String relation) throws APIException {
 		List<T> children = new ArrayList<T>();
 		JSONObject json = response.getJsonResponse();
 		System.out
 				.println("SERIALIZER - deserializeList(skeleton, response, relation) "
 						+ json.toString());
 		try {
 			for (int i = 0; i < json.getJSONArray("results").length(); i++) {
 				T instance = (T) Class.forName(skeleton.getClass().getName())
 						.newInstance();
 				children.add(deserialize(instance, json.getJSONArray("results")
 						.getJSONObject(i)));
 			}
 			this.invokeSet(skeleton, relation, children);
 		} catch (JSONException e) {
 			throw new APIException(e.getMessage());
 		} catch (IllegalAccessException e) {
 			Log.e("SERIALIZER", e + ":" + e.getMessage());
 			throw new APIException(e.getMessage());
 		} catch (InstantiationException e) {
 			Log.e("SERIALIZER", e + ":" + e.getMessage());
 			throw new APIException(e.getMessage());
 		} catch (ClassNotFoundException e) {
 			Log.e("SERIALIZER", e + ":" + e.getMessage());
 			throw new APIException(e.getMessage());
 		}
 		return children;
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @param response
 	 * @return
 	 * @throws APIException
 	 */
 	public T deserialize(T model, Response response) throws APIException {
 		JSONObject json = response.getJsonResponse();
 		if (response.getResponse().getStatusLine().getStatusCode() == 400) {
 			System.out.println("SERIALIZER - deserialize(model, response) 400"
 					+ response.getResponseBody());
 			throw new APIException(response.getResponseBody());
 		} else if (response.getResponse().getStatusLine().getStatusCode() == 200
 				|| response.getResponse().getStatusLine().getStatusCode() == 201) {
 			System.out
 					.println("SERIALIZER - deserialize(model, response) 200 or 201 "
 							+ json.toString());
 			boolean isStatus = false;
 			try {
 				json.getString("detail");
 				System.out
 						.println("SERIALIZER - deserialize(model, response) status-response "
 								+ json.toString());
 				isStatus = true;
 			} catch (JSONException e) {
 
 			}
 			if (isStatus) {
 				return model;
 			} else {
 				return this.deserialize(model, json);
 			}
 		}
 		throw new APIException(response.getResponseBody());
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @param property
 	 * @return
 	 */
 	private Object invokeGet(T model, String property) {
 		try {
 			String name = property.substring(0, 1).toUpperCase();
 			name += property.substring(1);
//			System.out.println("SERIALIZER - trying to invoke method " + "get"
//					+ name + " on " + model.getClass().getSimpleName());
 			return model.getClass().getMethod("get" + name).invoke(model);
 		} catch (NoSuchMethodException e) {
 			System.out.println(e.getClass() + ": " + e.getMessage());
 		} catch (InvocationTargetException e) {
 			System.out.println(e.getClass() + ": " + e.getMessage());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.getClass() + ": " + e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @param property
 	 * @param value
 	 */
 	private void invokeSet(T model, String property, Object value) {
 		try {
 			String name = property.substring(0, 1).toUpperCase();
 			name += property.substring(1);
//			System.out.println("SERIALIZER - trying to invoke method " + "set"
//					+ name + " with value: " + value + " ("
//					+ value.getClass().getSimpleName() + ")" + " on "
//					+ model.getClass().getSimpleName());
 
 			if (value != null) {
 				if (value instanceof Double) {
 					try {
 						value = (Double) value;
 					} catch (Exception e) {
 						// TODO: handle exception
 					}
 				}
 				Method method = model.getClass().getMethod("set" + name,
 						value.getClass());
 				method.invoke(model, value);
 			}
 		} catch (NoSuchMethodException e) {
 			System.out.println(e.getClass() + ": " + e.getMessage());
 		} catch (InvocationTargetException e) {
 			System.out.println(e.getClass() + ": " + e.getMessage());
 		} catch (IllegalAccessException e) {
 			System.out.println(e.getClass() + ": " + e.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @return
 	 */
 	private List<String> getSimplePropertyNames(T model) {
 		List<String> names = new ArrayList<String>();
 		for (Field field : model.getClass().getDeclaredFields()) {
 			if (field.getType() != List.class) {
 				names.add(field.getName());
 			}
 		}
 		for (Field field : model.getClass().getSuperclass().getDeclaredFields()) {
 			if (field.getType() != List.class) {
 				names.add(field.getName());
 			}
 		}
 		// for (String string : names) {
 		// System.out.println("SERIALIZER - getSimplePropertyNames() Found property "
 		// + string + " in "
 		// + model.getClass().getSimpleName());
 		// }
 		return names;
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @return
 	 */
 	private List<String> getListPropertyNames(T model) {
 		List<String> names = new ArrayList<String>();
 		for (Field field : model.getClass().getDeclaredFields()) {
 			if (field.getType() == List.class) {
 				names.add(field.getName());
 			}
 		}
 		for (Field field : model.getClass().getSuperclass().getDeclaredFields()) {
 			if (field.getType() == List.class) {
 				names.add(field.getName());
 			}
 		}
 		// for (String string : names) {
 		// System.out.println("SERIALIZER - getListPropertyNames() Found property "
 		// + string + " in "
 		// + model.getClass().getSimpleName());
 		// }
 		return names;
 	}
 
 }
