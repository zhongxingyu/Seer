 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.profileservice.controllers.rest;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
 import eu.trentorise.smartcampus.exceptions.SmartCampusException;
 import eu.trentorise.smartcampus.profileservice.managers.ProfileServiceException;
 import eu.trentorise.smartcampus.profileservice.managers.PermissionManager;
 import eu.trentorise.smartcampus.profileservice.managers.ProfileManager;
 import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
 import eu.trentorise.smartcampus.profileservice.model.ExtendedProfiles;
 import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;
 import eu.trentorise.smartcampus.resourceprovider.controller.SCController;
 import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;
 import eu.trentorise.smartcampus.resourceprovider.model.User;
 
 /**
  * Access to the extended profiles data
  * @author raman
  *
  */
 @Controller("extendedProfileController")
 public class ExtendedProfileController extends SCController {
 
 	private static final Logger logger = Logger
 			.getLogger(ExtendedProfileController.class);
 
 	@Autowired
 	private ProfileManager profileManager;
 
 	@Autowired
 	private ProfileStorage storage;
 
 	@Autowired
 	private PermissionManager permissionManager;
 
 	@Autowired
 	private AuthServices services;
 	@Override
 	protected AuthServices getAuthServices() {
 		return services;
 	}
 
 	/**
 	 * Creates a extended profile for a user given application and profileId
 	 * Valid only if userId is the authenticated user
 	 * 
 	 * @param response
 	 * @param userId
 	 * @param appId
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/app/{userId}/{appId}/{profileId}")
 	public void createExtendedProfile(HttpServletResponse response,
 			@PathVariable("userId") String userId,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 
 		
 		ExtendedProfile extProfile = new ExtendedProfile();
 		extProfile.setAppId(appId);
 		extProfile.setProfileId(profileId);
 		extProfile.setUserId(userId);
 		extProfile.setContent(content);
 		extProfile.setUser(userId);
 		extProfile.setUpdateTime(System.currentTimeMillis());
 
 		try {
 			User user = getUserObject(userId);
 			if (user == null) {
 				throw new SmartCampusException("No user found for id "+userId);
 			}
 			profileManager.create(user, extProfile);
 		} catch (AlreadyExistException e) {
 			logger.error(
 					String.format(
 							"Extended profile already exists userId:%s, appId:%s, profileId:%s",
 							userId, appId, profileId), e);
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		} catch (SmartCampusException e) {
 			logger.error(
 					"General exception creating extended profile for user "
 							+ userId, e);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 
 	}
 
 
 	/**
 	 * Returns extended profile of a user given application and profileId,
 	 * filtered by user visibility permissions
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param userId
 	 * @param appId
 	 * @param profileId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/app/{userId}/{appId}/{profileId}")
 	public @ResponseBody
 	ExtendedProfile getExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("userId") String userId,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			ExtendedProfile profile = storage.findExtendedProfile(userId, appId, profileId);
 			return profile;
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns extended profile of an authenticate user given application and
 	 * profileId
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param appId
 	 * @param profileId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/me/{appId}/{profileId}")
 	public @ResponseBody
 	ExtendedProfile getMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 
 			return storage.findExtendedProfile(userId, appId, profileId);
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns extended profiles of an authenticate user given application
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param appId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/me/{appId}")
 	public @ResponseBody
 	ExtendedProfiles getMyAppExtendedProfiles(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("appId") String appId) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 			ExtendedProfiles ext = new ExtendedProfiles();
 			ext.setProfiles(storage.findExtendedProfiles(userId, appId));
 			return ext;
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns extended profiles of an authenticate user
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/me")
 	public @ResponseBody
 	ExtendedProfiles getMyExtendedProfiles(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 			ExtendedProfiles ext = new ExtendedProfiles();
 			ext.setProfiles(storage.findExtendedProfiles(""+userId));
 			return ext;
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return null;
 		}
 	}
 
 
 	/**
 	 * Returns all extended profile for given application and profileId, given the profile attributes
 	 * 
 	 * @param request
 	 * @param response
 	 * @param profileId
 	 * @param appId
 	 * @param profileAttrs
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/all/{appId}/{profileId}")
 	public @ResponseBody
 	ExtendedProfiles getUsersExtendedProfilesByAttributes(
 			HttpServletRequest request, HttpServletResponse response,
 			@PathVariable String profileId, @PathVariable String appId,
 			@RequestBody Map<String, Object> profileAttrs) throws IOException,
 			ProfileServiceException {
 
 		try {
 			List<ExtendedProfile> profiles = storage.findExtendedProfiles(appId, profileId, profileAttrs);
 
 			ExtendedProfiles ext = new ExtendedProfiles();
 			ext.setProfiles(profiles);
 			return ext;
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return null;
 		}
 
 	}
 
 	/**
 	 * Returns the list of extended profiles of a list of userIds.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param userIds
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/all")
 	public @ResponseBody
 	ExtendedProfiles getUsersExtendedProfiles(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestParam List<String> userIds) throws IOException,
 			ProfileServiceException {
 		return getAllProfiles(response, userIds, null, null);
 	}
 	/**
 	 * Returns the list of extended profiles of a list of userIds given an
 	 * application.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param userIds
 	 * @param userId
 	 * @param appId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/all/{appId}")
 	public @ResponseBody
 	ExtendedProfiles getUsersAppExtendedProfiles(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestParam List<String> userIds,
 			@PathVariable("appId") String appId) throws IOException,
 			ProfileServiceException {
 		return getAllProfiles(response, userIds, appId, null);
 	}
 	/**
 	 * Returns the list of extended profiles of a list of userIds given an
 	 * application and profile.
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param userIds
 	 * @param userId
 	 * @param appId
 	 * @param profileId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/all/{appId}/{profileId}")
 	public @ResponseBody
 	ExtendedProfiles getUsersAppProfileExtendedProfiles(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestParam List<String> userIds,
 			@PathVariable("appId") String appId, @PathVariable String profileId) throws IOException,
 			ProfileServiceException {
 		return getAllProfiles(response, userIds, appId, profileId);
 	}
 
 	protected ExtendedProfiles getAllProfiles(HttpServletResponse response, List<String> userIds, String appId, String profileId) {
 		try {
 			List<ExtendedProfile> profiles = new ArrayList<ExtendedProfile>();
 			for (String userId : userIds) {
 				if (profileId != null && appId != null) {
 					profiles.add(storage.findExtendedProfile(userId, appId, profileId));
 				} else if (appId != null) {
 					profiles.addAll(storage.findExtendedProfiles(userId, appId));
 				} else {
 					profiles.addAll(storage.findExtendedProfiles(userId));
 				}
 			}
 
 			ExtendedProfiles ext = new ExtendedProfiles();
 			ext.setProfiles(profiles);
 			return ext;
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return null;
 		}
 	}
 
 	/**
 	 * Updates a extended profile of a user given application and profileId
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param userId
 	 * @param appId
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/extprofile/app/{userId}/{appId}/{profileId}")
 	public void updateExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("userId") String userId,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 		try {
 			ExtendedProfile profile = storage.findExtendedProfile(userId,
 					appId, profileId);
 
 			if (profile == null) {
 				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 				return;
 			}
 
 			profile.setContent(content);
 			profile.setUpdateTime(System.currentTimeMillis());
 			storage.updateObject(profile);
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	/**
 	 * Updates or creates an extended profile of the current user given application and profileId
 	 * Valid only if userId is the authenticated user
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param appId
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/me/{appId}/{profileId}")
 	public void createMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 			User user = getUserObject(userId);
 			ExtendedProfile extProfile = new ExtendedProfile();
 			extProfile.setAppId(appId);
 			extProfile.setProfileId(profileId);
 			extProfile.setUserId(userId);
 			extProfile.setContent(content);
 			extProfile.setUser(userId);
 			extProfile.setUpdateTime(System.currentTimeMillis());
 			profileManager.create(user, extProfile);
 		} catch (AlreadyExistException e) {
 			logger.error(
 					String.format(
 							"Extended profile already exists: appId:%s, profileId:%s",
 							appId, profileId), e);
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			return;
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	/**
 	 * Updates or creates an extended profile of the current user given application and profileId
 	 * Valid only if userId is the authenticated user
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param appId
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.PUT, value = "/extprofile/me/{appId}/{profileId}")
 	public void updateMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 			ExtendedProfile profile = storage.findExtendedProfile(userId, appId, profileId);
 
 			if (profile == null) {
 				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 				return;
 			} else {
 				profile.setContent(content);
 				profile.setUpdateTime(System.currentTimeMillis());
 				storage.updateObject(profile);
 			}
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	/**
 	 * Deletes an extended profile of a user given application and profileId
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param userId
 	 * @param appId
 	 * @param profileId
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/extprofile/app/{userId}/{appId}/{profileId}")
 	public void deleteExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("userId") String userId,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			storage.deleteExtendedProfile(userId, appId, profileId);
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 	/**
 	 * Deletes an extended profile of a user given application and profileId
 	 * Valid only if userId is the authenticated user
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param appId
 	 * @param profileId
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.DELETE, value = "/extprofile/me/{appId}/{profileId}")
 	public void deleteMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			storage.deleteExtendedProfile(getUserId(), appId, profileId);
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 	
 	/**
 	 * @param appId
 	 * @param profileId
 	 * @return all profiles of specific profile type shared with the current user
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/shared/{appId}/{profileId}")
 	public @ResponseBody ExtendedProfiles getProfileSharedExtendedProfile(@PathVariable("appId") String appId,
 			@PathVariable("profileId") String profileId) 
 	{
 		return getSharedProfiles(getUserObject(getUserId()).getSocialId(), appId, profileId);
 	}
 	
 	/**
 	 * @param appId
 	 * @param profileId
 	 * @return all profiles of specific app shared with the current user
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/shared/{appId}")
 	public @ResponseBody ExtendedProfiles getAppSharedExtendedProfile(@PathVariable("appId") String appId) 
 	{
 		return getSharedProfiles(getUserObject(getUserId()).getSocialId(), appId, null);
 	}
 
 	/**
 	 * @param appId
 	 * @param profileId
 	 * @return all profiles shared with the current user
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/shared")
 	public @ResponseBody ExtendedProfiles getAllSharedExtendedProfile()  {
 		return getSharedProfiles(getUserObject(getUserId()).getSocialId(), null, null);
 	}
 
 	protected ExtendedProfiles getSharedProfiles(Long actorId, String appId, String profileId) {
 		List<ExtendedProfile> res = new ArrayList<ExtendedProfile>();
 		List<Long> list = profileManager.getShared(actorId);
 		for (Long entityId : list) {
 			ExtendedProfile p = storage.getObjectByEntityId(entityId, appId, profileId);
 			if (p != null) res.add(p);
 		}
 		ExtendedProfiles eps = new ExtendedProfiles();
 		eps.setProfiles(res);
 		return eps;
 	}
 }
