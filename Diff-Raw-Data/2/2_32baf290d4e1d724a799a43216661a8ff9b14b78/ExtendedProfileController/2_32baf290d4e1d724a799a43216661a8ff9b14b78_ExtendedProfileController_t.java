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
 import eu.trentorise.smartcampus.profileservice.managers.PermissionManager;
 import eu.trentorise.smartcampus.profileservice.managers.ProfileManager;
 import eu.trentorise.smartcampus.profileservice.managers.ProfileServiceException;
 import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
 import eu.trentorise.smartcampus.profileservice.model.ExtendedProfiles;
 import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;
 import eu.trentorise.smartcampus.resourceprovider.controller.SCController;
 import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;
 import eu.trentorise.smartcampus.social.model.User;
 
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
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/app/{userId}/{profileId}")
 	public void createExtendedProfile(HttpServletResponse response,
 			@PathVariable("userId") String userId,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 
 		
 		ExtendedProfile extProfile = new ExtendedProfile();
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
 							"Extended profile already exists userId:%s, profileId:%s",
 							userId, profileId), e);
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
 	 * @param profileId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/app/{userId}/{profileId}")
 	public @ResponseBody
 	ExtendedProfile getExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("userId") String userId,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			ExtendedProfile profile = storage.findExtendedProfile(userId, profileId);
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
 	 * @param profileId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/me/{profileId}")
 	public @ResponseBody
 	ExtendedProfile getMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 
 			return storage.findExtendedProfile(userId, profileId);
 
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
 	 * @param profileAttrs
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/all/{profileId}")
 	public @ResponseBody
 	ExtendedProfiles getUsersExtendedProfilesByAttributes(
 			HttpServletRequest request, HttpServletResponse response,
 			@PathVariable String profileId, 
 			@RequestBody Map<String, Object> profileAttrs) throws IOException,
 			ProfileServiceException {
 
 		try {
 			List<ExtendedProfile> profiles = storage.findExtendedProfiles(profileId, profileAttrs);
 
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
 		return getAllProfiles(response, userIds, null);
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
 	 * @param profileId
 	 * @return
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/all/{profileId}")
 	public @ResponseBody
 	ExtendedProfiles getUsersAppProfileExtendedProfiles(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestParam List<String> userIds,
 			@PathVariable String profileId) throws IOException,
 			ProfileServiceException {
 		return getAllProfiles(response, userIds, profileId);
 	}
 
 	protected ExtendedProfiles getAllProfiles(HttpServletResponse response, List<String> userIds, String profileId) {
 		try {
 			List<ExtendedProfile> profiles = new ArrayList<ExtendedProfile>();
 			for (String userId : userIds) {
 				if (profileId != null) {
 					profiles.add(storage.findExtendedProfile(userId, profileId));
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
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/extprofile/app/{userId}/{profileId}")
 	public void updateExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("userId") String userId,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 		try {
 			ExtendedProfile profile = storage.findExtendedProfile(userId, profileId);
 
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
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/me/{profileId}")
 	public void createMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 			User user = getUserObject(userId);
 			ExtendedProfile extProfile = new ExtendedProfile();
 			extProfile.setProfileId(profileId);
 			extProfile.setUserId(userId);
 			extProfile.setContent(content);
 			extProfile.setUser(userId);
 			extProfile.setUpdateTime(System.currentTimeMillis());
 			profileManager.create(user, extProfile);
 		} catch (AlreadyExistException e) {
 			logger.error(
 					String.format(
 							"Extended profile already exists: profileId:%s", profileId), e);
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
 	 * @param profileId
 	 * @param content
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.PUT, value = "/extprofile/me/{profileId}")
 	public void updateMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("profileId") String profileId,
 			@RequestBody Map<String, Object> content) throws IOException,
 			ProfileServiceException {
 		try {
 			String userId = getUserId();
 			ExtendedProfile profile = storage.findExtendedProfile(userId, profileId);
 
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
 	 * @param profileId
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.DELETE, value = "/extprofile/app/{userId}/{profileId}")
 	public void deleteExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("userId") String userId,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			storage.deleteExtendedProfile(userId, profileId);
 
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
 	 * @param profileId
 	 * @throws IOException
 	 * @throws ProfileServiceException
 	 */
 	@RequestMapping(method = RequestMethod.DELETE, value = "/extprofile/me/{profileId}")
 	public void deleteMyExtendedProfile(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("profileId") String profileId) throws IOException,
 			ProfileServiceException {
 		try {
 			storage.deleteExtendedProfile(getUserId(), profileId);
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 	
 	/**
 	 * @param profileId
 	 * @return all profiles of specific profile type shared with the current user
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/shared/{profileId}")
 	public @ResponseBody ExtendedProfiles getProfileSharedExtendedProfile(@PathVariable("profileId") String profileId) 
 	{
 		return getSharedProfiles(getUserObject(getUserId()).getSocialId(), profileId);
 	}
 	
 	/**
 	 * @param profileId
 	 * @return all profiles shared with the current user
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/shared")
 	public @ResponseBody ExtendedProfiles getAllSharedExtendedProfile()  {
 		return getSharedProfiles(getUserObject(getUserId()).getSocialId(), null);
 	}
 
 	protected ExtendedProfiles getSharedProfiles(String actorId, String profileId) {
 		List<ExtendedProfile> res = new ArrayList<ExtendedProfile>();
 		List<Long> list = profileManager.getShared(actorId);
 		for (Long entityId : list) {
 			ExtendedProfile p = storage.getObjectByEntityId(entityId, profileId);
 			if (p != null) res.add(p);
 		}
 		ExtendedProfiles eps = new ExtendedProfiles();
 		eps.setProfiles(res);
 		return eps;
 	}
 }
