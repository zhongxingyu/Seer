 
 package com.stylepopz.rest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StringUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.singly.client.SinglyAccountStorage;
 import com.singly.client.SinglyService;
 import com.stylepopz.model.Preference;
 import com.stylepopz.model.entity.Preferences;
 import com.stylepopz.model.entity.Profile;
 import com.stylepopz.model.entity.User;
 import com.stylepopz.service.SpopzService;
 import com.sun.jersey.api.NotFoundException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiErrors;
import com.wordnik.swagger.annotations.ApiOperation;
 
 import flexjson.JSONSerializer;
 
 @Path("/user")
@Api(value = "/user", description = "Operations about Users information")
 @Produces({MediaType.APPLICATION_JSON})
 @Component
 public class AppResource {
 
 	private static final Logger logger = LoggerFactory.getLogger(AppResource.class);
 
 	@Context
 	UriInfo uriInfo;
 	@Context
 	Request request;
 
 	@Autowired
 	private SinglyService singlyService;
 
 	@Autowired
 	private SpopzService spopzService;
 
 	@Autowired
 	private SinglyAccountStorage accountStorage;
 
 	/** 
 	 * returns a Json Profile
 	 * Generic for any social login
 	 * 
 	 * @param profileId
 	 * @return
 	 */
 	@GET
 	@Path("/getProfile/{profileId}")
 	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON})
	@ApiOperation(value = "Find Profile by ID", notes = "Profile id must be the ID of the social login", responseClass = "java.lang.String")
	@ApiErrors(value = { @ApiError(code = 400, reason = "Invalid ID supplied"),
			@ApiError(code = 404, reason = "Profile not found") })
 	public String getProfile(@PathParam("profileId") String profileId) throws NotFoundException{
 
 		logger.debug("fetching profile info="+profileId);
 
 		// get the profile and retrieve the access_token
 		String access_token = spopzService.getAccessToken(profileId, "facebook");
 		if(StringUtils.isEmpty(access_token)){
 			throw new NotFoundException("profile Not found");
 		}
 		Map<String, String> postParams = new HashMap<String, String>();
 		/*postParams.put("client_id", "");
 		postParams.put("client_secret", "");
 		postParams.put("code", "");
 		String access_token = singlyService.doGetApiRequest("/oauth/access_token", postParams);*/
 
 		// https://api.singly.com/services/twitter/tweets?access_token=my_token
 		postParams = new HashMap<String, String>();
 		postParams.put("access_token", access_token);
 
 		// invoke the profile service
 		//String output = singlyService.doGetApiRequest("/services/facebook/self", postParams);
 		String output = singlyService.doGetApiRequest("/profile", postParams);
 		logger.debug("result="+output);
 
 		JsonObject o = (JsonObject)new JsonParser().parse(output);
 		System.out.println("jsonobject = "+o);
 
 		ObjectMapper mapper = new ObjectMapper();
 		//JsonNode profile = null;
 		//Profile profile = null;
 		//try {
 		//Map<String, Object> userInMap = mapper.readValue(output, new TypeReference<Map<String, Object>>() {});
 		//profile = mapper.readValue(output, JsonNode.class);
 		//dao.insertProfile(profile, "profile");
 		spopzService.insertProfile(o, "profile");
 		/*} catch (JsonParseException e) {
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}*/
 
 		return output;
 	}
 
 	@GET
 	@Path("/getProfile")
 	public Response showMessageForEmptyProfile(){
 		return Response.serverError().entity("profileId cannot be blank").build();
 	}
 
 	@DELETE
 	@Path("/del/{service}/{profileId}/{account}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response delProfile(@PathParam("service") String service, 
 			@PathParam("profileId") String profileId,
 			@PathParam("acccount") String account) {
 
 		logger.debug("deleting profileid="+profileId);
 
 		// delete the profile
 		Map<String, String> postParams = new HashMap<String, String>();
 		postParams.put("delete", profileId + "@" + service);
 		postParams.put("access_token", accountStorage.getAccessToken(account));
 
 		// delete the profile service
 		singlyService.doPostApiRequest("/profiles", null, postParams);
 
 		logger.debug("deleted profile id="+profileId);
 
 		return Response.ok().build();
 	}
 
 	/*@POST
 	@Path("/setPreference")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response setPreferenceWithId(@FormParam("preference") Preference preference) {
 
 		logger.info("inside setpreference with id");
 		String result = "Pref saved : test";
 		//return Response.status(201).entity(result).build();
 		return Response.ok().build();
 	}*/
 
 
 	@POST
 	@Path("/setPreference/{profileId}")
 	public Response setPreferenceWithId(
 			@PathParam("profileId") String profileId,
 			@FormParam("preference") String preference) {
 
 		Gson gson = new Gson();
 		Preference pref = gson.fromJson(preference, Preference.class);
 
 		spopzService.upsertPreference(pref, preference);
 
 		//logger.info("setPreferenceWithId="+ profileId +"; pref="+pref.getId());
 		String str = "{'stored':'true'}";
 		return Response.ok(str, MediaType.APPLICATION_JSON).build();
 	}
 
 	@POST
 	@Path("/setPreference")
 	public Response setPreference(
 			@FormParam("profileId") String profileId,
 			@FormParam("preference") String preference) {
 
 		Gson gson = new Gson();
 		Preference pref = gson.fromJson(preference, Preference.class);
 		pref.setId(profileId);
 
 		spopzService.upsertPreference(pref, preference);
 
 		//logger.info("setPreferenceWithId="+ profileId +"; pref="+pref.getId());
 		String str = "{'stored':'true'}";
 		return Response.ok(str, MediaType.APPLICATION_JSON).build();
 	}
 
 	@GET
 	@Path("/getPreference/{profileId}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Preference getPreference(@PathParam("profileId") String profileId) {
 
 		Preference pref = new Preference();
 		List<Map<String, String>> size = new ArrayList<Map<String, String>>();
 		List<Map<String, String>> colors = new ArrayList<Map<String, String>>();
 		List<Map<String, String>> prints = new ArrayList<Map<String, String>>();
 		List<Map<String, String>> luxurybrands = new ArrayList<Map<String, String>>();
 		List<Map<String, String>> hi_Street_Brands = new ArrayList<Map<String, String>>();
 		List<Map<String, String>> fast_fashion_brands = new ArrayList<Map<String, String>>();
 		List<Map<String, String>> indie_designers = new ArrayList<Map<String, String>>();
 		List<Map<String, String>> blogger_pref = new ArrayList<Map<String, String>>();
 
 		//size
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("shirt",  "s");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		size.add(map);
 
 		map = new HashMap<String, String>();
 		map.put("pant",  "m");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		size.add(map);
 
 		map = new HashMap<String, String>();
 		map.put("shoes",  "7");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "N");
 		size.add(map);
 		pref.setSize(size);
 
 		//colors
 		map = new HashMap<String, String>();
 		map.put("color",  "skyblue");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		colors.add(map);
 
 		map = new HashMap<String, String>();
 		map.put("color",  "purple");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		colors.add(map);
 		pref.setColors(colors);
 
 		//prints
 		map = new HashMap<String, String>();
 		map.put("print",  "zebra");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		prints.add(map);
 
 		map = new HashMap<String, String>();
 		map.put("print",  "flowery");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "N");
 		prints.add(map);
 		pref.setPrints(colors);
 
 		//luxury brands
 		map = new HashMap<String, String>();
 		map.put("luxbrand",  "Armani");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		luxurybrands.add(map);
 
 		map = new HashMap<String, String>();
 		map.put("luxbrand",  "Gucci");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "N");
 		luxurybrands.add(map);
 		pref.setLuxurybrands(luxurybrands);
 
 		//hi street brands
 		map = new HashMap<String, String>();
 		map.put("hibrand",  "brand 1");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		hi_Street_Brands.add(map);
 
 		map = new HashMap<String, String>();
 		map.put("hibrand",  "brand 2");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "N");
 		hi_Street_Brands.add(map);
 		pref.setHi_street_brands(hi_Street_Brands);
 
 		//fast street brands
 		map = new HashMap<String, String>();
 		map.put("hibrand",  "brand 1");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		fast_fashion_brands.add(map);
 
 		map = new HashMap<String, String>();
 		map.put("hibrand",  "brand 2");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "N");
 		fast_fashion_brands.add(map);
 		pref.setFast_fashion_brands(fast_fashion_brands);
 
 		//indie designers
 		map = new HashMap<String, String>();
 		map.put("indie",  "sonas jeans");
 		map.put("url",  "http://www.google.com");
 		map.put("selected",  "Y");
 		indie_designers.add(map);
 		pref.setIndie_designers(indie_designers);
 
 		//blogger pref
 		map = new HashMap<String, String>();
 		map.put("url",  "http://www.calvintage.com");
 		map.put("selected",  "Y");
 		blogger_pref.add(map);
 		pref.setBlogger_preferences(blogger_pref);
 
 		pref.setId(profileId);
 
 		return pref;
 	}
 
 	@GET
 	@Path("/getPreference")
 	public Response showMessageForEmptyPreference(){
 		return Response.serverError().entity("profileId cannot be blank").build();
 	}
 
 	@GET
 	@Path("/showReport")
 	public String showReport(){
 
 		List<Preferences> listOfPreferences = spopzService.listPreferences();
 		List<Profile> listOfProfiles = spopzService.listProfiles();
		List<User> listOfUsers = spopzService.listUsers(); 
 		
 		return new StringBuffer("Preferences:::").append(new JSONSerializer().deepSerialize(listOfPreferences)).append("Profiles:::").
 		append(new JSONSerializer().deepSerialize(listOfProfiles)).append("Users:::").
 		append(new JSONSerializer().deepSerialize(listOfUsers)).toString();
 	}
 
 
 	/*private Response putAndGetResponse() {
 		Response res;
 
 		if(true) {
 			res = Response.noContent().build();
 		} else {
 			res = Response.created(uriInfo.getAbsolutePath()).build();
 		}
 		return res;
 	}*/
 
 } 
