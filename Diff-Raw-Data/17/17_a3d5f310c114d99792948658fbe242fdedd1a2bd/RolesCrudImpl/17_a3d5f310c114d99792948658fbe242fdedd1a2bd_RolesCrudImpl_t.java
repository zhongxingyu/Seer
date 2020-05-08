 package com.varun.scholar.business.data.impl.settings;
 
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaQuery;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.varun.scholar.business.data.entities.administration.users.RolesSearch;
 import com.varun.scholar.shared.interfaces.CrudOperations;
 import com.varun.scholar.shared.interfaces.administration.users.RolesCrud;
 
 @Stateless
 public class RolesCrudImpl implements RolesCrud {
 
 	@Inject
 	private Logger log;
 
 	@Inject
 	private EntityManager em;
 
 	private GsonBuilder gsonBuilder = new GsonBuilder();
 	private Gson gson = gsonBuilder.create();
 
 	public String find(Long id) {
 		return gson.toJson(em.find(RolesSearch.class, id));
 	}
 
 	@Override
 	public String createOrUpdate(String rolesJson) {
 		gsonBuilder.registerTypeAdapter(RolesSearch.class,
 				new JsonDeserializer<RolesSearch>() {
 
 					@Override
 					public RolesSearch deserialize(JsonElement jsonEl,
 							Type arg1, JsonDeserializationContext arg2)
 							throws JsonParseException {
 
 						RolesSearch roles = new RolesSearch();
 
 						JsonObject jsonObj = jsonEl.getAsJsonObject();
 						roles.setRoleName(jsonObj.get("roleName").getAsString());
 						return roles;
 					}
 				});
 		em.merge(gson.fromJson(rolesJson, RolesSearch.class));
 		return findAll(1, 1, CrudOperations.NUM_RECS);
 	}
 
 	@Override
 	public String remove(String rolesJson) {
		gsonBuilder.registerTypeAdapter(RolesSearch.class,
				new JsonDeserializer<RolesSearch>() {

					@Override
					public RolesSearch deserialize(JsonElement jsonEl,
							Type arg1, JsonDeserializationContext arg2)
							throws JsonParseException {

						RolesSearch roles = new RolesSearch();

						JsonObject jsonObj = jsonEl.getAsJsonObject();
						roles.setRoleName(jsonObj.get("roleName").getAsString());
						return roles;
					}
				});
 		RolesSearch fromJson = gson.fromJson(rolesJson, RolesSearch.class);
 		RolesSearch find = em.find(RolesSearch.class, fromJson.getId());
 		em.remove(find);
 		em.flush();
 		return findAll(1, 1, CrudOperations.NUM_RECS);
 	}
 
 	@Override
 	public String findAll(int page, int start, int limit) {
 
 		CriteriaQuery<RolesSearch> criteria = em.getCriteriaBuilder()
 				.createQuery(RolesSearch.class);
 
 		criteria.select(criteria.from(RolesSearch.class));
 
 		List<RolesSearch> vehicles = em.createQuery(criteria)
 				.setMaxResults(limit).setFirstResult(start).getResultList();
 
 		int resultCount = em.createQuery(criteria).getResultList().size();
 		String total = "\"total\":" + resultCount;
 
 		String vehiclesJson = gson.toJson(vehicles);
 
 		return "{" + total + ", \"data\":" + vehiclesJson + "}";
 	}
 
 }
