 package com.example.swp_ucd_2013_eule.model;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.util.Log;
 
 import com.example.swp_ucd_2013_eule.net.APIException;
 import com.example.swp_ucd_2013_eule.net.ApiClient;
 import com.example.swp_ucd_2013_eule.net.HttpJsonClient.Response;
 
 public class APIModel<T extends Model, P extends Model> {
 
 	private Class<T> type;
 
 	private ApiClient api;
 
 	public APIModel(Class<T> type) {
 		this.type = type;
 		this.api = ApiClient.getInstance();
 	}
 
 	/**
 	 * get single model GET http://server/[model]/[id]/
 	 */
 	public T get(T model) throws APIException {
 		try {
			Log.e("APIModel get(model) id:", model.getId().toString());
 			Serializer<T> serializer = new Serializer<T>();
 			Response response = api
 					.get(this.buildEndpoint(model, model.getId()));
 			return serializer.deserialize(model, response);
 		} catch (IOException e) {
 			throw new APIException();
 		}
 	}
 
 	/**
 	 * get single model GET http://server/[model]/[id]/
 	 */
 	public T get(T model, Integer id) throws APIException {
 		try {
 			Serializer<T> serializer = new Serializer<T>();
 			Response response = api.get(this.buildEndpoint(model, id));
 			return serializer.deserialize(model, response);
 		} catch (IOException e) {
 			throw new APIException();
 		}
 	}
 
 	/**
 	 * save single model PUT http://server/[model]/[id]/
 	 */
 	public T save(T model) throws APIException {
 		try {
 			Serializer<T> serializer = new Serializer<T>();
 
 			Response response;
 			if (model.getId() == null || model.getId() < 1) {
 				response = api.post(this.buildEndpoint(),
 						serializer.serialize(model));
 			} else {
 				response = api.put(this.buildEndpoint(model),
 						serializer.serialize(model));
 			}
 
 			// Response response = api.post(this.buildEndpoint(model),
 			// serializer.serialize(model));
 			return serializer.deserialize(model, response);
 		} catch (Exception e) {
 			throw new APIException(e.getMessage());
 		}
 	}
 
 	/**
 	 * add model to parent model PUT http://server/[parent]/[id]/[model]/[id]/
 	 */
 	public T addToParent(T model, P parent, String relation)
 			throws APIException {
 		try {
 			Serializer<T> serializer = new Serializer<T>();
 			Response response = api.put(
 					this.buildEndpoint(model, parent, relation),
 					serializer.serialize(model));
 			return serializer.deserialize(model, response);
 		} catch (Exception e) {
 			throw new APIException(e.getMessage());
 		}
 	}
 
 	/**
 	 * remove model from parent model DELETE
 	 * http://server/[parent]/[id]/[model]/[id]/
 	 */
 	public T deleteFromParent(T model, T parent) throws APIException {
 		return null;
 	}
 
 	/**
 	 * get all GET http://server/[model]/
 	 */
 	public List<T> getAll(T model) throws APIException {
 		try {
 			Serializer<T> serializer = new Serializer<T>();
 			Response response = api.get(this.buildEndpoint());
 			return serializer.deserializeList(model, response);
 		} catch (Exception e) {
 			throw new APIException(e.getMessage());
 		}
 	}
 
 	/**
 	 * get by parent model GET http://server/[parent]/[id]/[model]/
 	 */
 	public List<T> getAllByParent(P parent, T skeleton, String relation)
 			throws APIException {
 		try {
 			Serializer<T> serializer = new Serializer<T>();
 			Response response = api.get(this.buildEndpoint(null, parent,
 					relation));
 
 			return serializer.deserializeList(skeleton, response, relation);
 		} catch (Exception e) {
 			throw new APIException(e.getMessage());
 		}
 	}
 
 	private String buildEndpoint() {
 		return "/" + this.type.getSimpleName().toLowerCase() + "s" + "/";
 	}
 
 	private String buildEndpoint(T model) {
 		return "/" + this.type.getSimpleName().toLowerCase() + "s" + "/"
 				+ model.getId() + "/";
 	}
 
 	private String buildEndpoint(T model, Integer id) {
 		return "/" + this.type.getSimpleName().toLowerCase() + "s" + "/" + id
 				+ "/";
 	}
 
 	private String buildEndpointForParent(P model, Integer id) {
 		return "/" + model.getClass().getSimpleName().toLowerCase() + "s" + "/"
 				+ id + "/";
 	}
 
 	private String buildEndpoint(T model, P parent, String relation) {
 		if (model == null) {
 			return this.buildEndpointForParent(parent, parent.getId())
 					+ relation + "/";
 		}
 		return this.buildEndpointForParent(parent, parent.getId()) + relation
 				+ "/" + model.getId() + "/";
 	}
 
 }
