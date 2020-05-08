 /*
  * RestFoldersAdminController.java
  * 
  * Copyright (C) 2012
  * 
  * This file is part of Proyecto persistenceGeo
  * 
  * This software is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * As a special exception, if you link this library with other files to produce
  * an executable, this library does not by itself cause the resulting executable
  * to be covered by the GNU General Public License. This exception does not
  * however invalidate any other reasons why the executable file might be covered
  * by the GNU General Public License.
  * 
  * Authors:: Alejandro Díaz Torres (mailto:adiaz@emergya.com)
  */
 package com.emergya.persistenceGeo.web;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.collections.ListUtils;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.emergya.persistenceGeo.dto.FolderDto;
 import com.emergya.persistenceGeo.dto.LayerDto;
 import com.emergya.persistenceGeo.dto.TreeFolderDto;
 import com.emergya.persistenceGeo.dto.TreeNode;
 import com.emergya.persistenceGeo.dto.Treeable;
 import com.emergya.persistenceGeo.dto.UserDto;
 import com.emergya.persistenceGeo.service.FoldersAdminService;
 import com.emergya.persistenceGeo.service.LayerAdminService;
 import com.emergya.persistenceGeo.service.UserAdminService;
 import com.emergya.persistenceGeo.utils.FolderStyle;
 import com.emergya.persistenceGeo.utils.FoldersUtils;
 import com.google.common.base.Strings;
 
 /**
  * Rest controller to admin and load foders
  * 
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  */
 @Controller
 public class RestFoldersAdminController implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -422396545892957323L;
 
 	@Resource
 	private UserAdminService userAdminService;
 	@Resource
 	private FoldersAdminService foldersAdminService;
 	@Resource
 	private LayerAdminService layerAdminService;
 
 	protected final String RESULTS = "results";
 	protected final String ROOT = "data";
 	protected final String SUCCESS = "success";
 
 	public static final String LOAD_FOLDERS_BY_USER = "user";
 	public static final String LOAD_FOLDERS_BY_GROUP = "group";
 	public static final String LOAD_FOLDERS_STYLE_TREE = "tree";
 	public static final String LOAD_FOLDERS_STYLE_STRING = "string";
 	
 	/**
 	 * The folder Id that must be passed to loadFoldersById so it
 	 * returns the unassigned layers.
 	 * {@link RestFoldersAdminController#loadFoldersById(String)}
 	 */
 	public static final Long UNASSIGNED_LAYERS_VIRTUAL_FOLDER_ID= -1L;
 
 	/**
 	 * Filter to show zone channels in
 	 * {@link RestFoldersAdminController#loadChannels(String)}
 	 */
 	public static final String ALL_CHANNEL_IN_ZONES = "ALL_CHANNEL_IN_ZONES";
 
 	/**
 	 * Filter to show only channel layers in
 	 * {@link RestFoldersAdminController#loadFoldersById(String, String)}
 	 */
 	public static final String ONLY_CHANNEL_MARK = "ONLY_CHANNEL_MARK";
 
 	/**
 	 * Filter to show only not channel layers in
 	 * {@link RestFoldersAdminController#loadFoldersById(String, String)}
 	 */
 	public static final String ONLY_NOT_CHANNEL_MARK = "ONLY_NOT_CHANNEL_MARK";
 	
 	
 	/**
 	 * Filter to show subfolder's layers in 
 	 * {@link RestFoldersAdminController#loadFoldersById(String, String)}
 	 */
 	public static final String RECURSIVE_FOLDER_LAYERS_MARK = "RECURSIVE_FOLDER_LAYERS_MARK";
 
 	/**
 	 * Filter to show layers in channel tree
 	 * {@link RestFoldersAdminController#loadChannels(String)}
 	 */
 	public static final String SHOW_FOLDER_LAYERS = "SHOW_FOLDER_LAYERS";
 
 	/**
 	 * Filter to hide IPT layers
 	 * {@link RestFoldersAdminController#loadChannels(String)}
 	 */
 	public static final String HIDE_IPT_CHANNELS = "HIDE_IPT_CHANNELS";
 	
 	
 
 	/**
 	 * This method loads layers.json related with a folder
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layers
 	 */
 	@RequestMapping(value = "/persistenceGeo/moveFolderTo", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> moveFolderTo(@RequestParam("folderId") String folderId,
 			@RequestParam("toFolder") String toFolder,
 			@RequestParam(value = "toOrder", required = false) String toOrder) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		FolderDto folder = null;
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			Long idFolder = Long.decode(folderId);
 			folder = (FolderDto) foldersAdminService.getById(idFolder);
 			folder.setIdParent(Long.decode(toFolder));
 			if (toOrder != null) {
 				folder.setOrder(Integer.decode(toOrder));
 			}
 			folder = (FolderDto) foldersAdminService.update(folder);
 
 			result.put(SUCCESS, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folder != null ? 1 : 0);
 		result.put(ROOT, folder);
 
 		return result;
 	}
 
 	/**
 	 * This method saves a folder related with a group
 	 * 
 	 * @param group
 	 */
 	@RequestMapping(value = "/persistenceGeo/saveFolderByGroup/{groupId}", method = RequestMethod.POST)
 	public @ResponseBody
 	FolderDto saveFolderByGroup(
 			@PathVariable String groupId,
 			@RequestParam("name") String name,
 			@RequestParam("enabled") String enabled,
 			@RequestParam("isChannel") String isChannel,
 			@RequestParam("isPlain") String isPlain,
 			@RequestParam(value = "parentFolder", required = false) String parentFolder) {
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			Long folder_type = foldersAdminService.DEFAULT_FOLDER_TYPE;
 			// TODO isPlain should be folderType, but I haven't changed because could be broke down
 			try{
 				if(StringUtils.isEmpty(isPlain) || !StringUtils.isNumeric(isPlain)){
 					// To do anything
 				}else{
 					folder_type = Long.decode(isPlain);
 				}
 			}catch (Exception e){
 				e.printStackTrace();
 			}
 			Long idGroup = Long.decode(groupId);
 			FolderDto rootFolder = foldersAdminService
 					.getRootGroupFolder(idGroup);
 			if (StringUtils.isEmpty(parentFolder)
 					|| !StringUtils.isNumeric(parentFolder)) {
 				return saveFolderBy(name, enabled, isChannel, folder_type,
 						rootFolder != null ? rootFolder.getId() : null, null,
 						idGroup);
 			} else {
 				return saveFolderBy(name, enabled, isChannel, folder_type,
 						Long.decode(parentFolder), null, idGroup);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * This method saves a folder related with a user
 	 * 
 	 * @param user
 	 */
 	@RequestMapping(value = "/persistenceGeo/saveFolder/{username}", method = RequestMethod.POST)
 	public @ResponseBody
 	FolderDto saveFolder(
 			@PathVariable String username,
 			@RequestParam("name") String name,
 			@RequestParam("enabled") String enabled,
 			@RequestParam("isChannel") String isChannel,
 			@RequestParam("isPlain") String isPlain,
 			@RequestParam(value = "parentFolder", required = false) String parentFolder) {
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			Long folder_type = foldersAdminService.DEFAULT_FOLDER_TYPE;
 			// TODO isPlain should be folderType, but I haven't changed because could be broke down
 			try{
 				if(StringUtils.isEmpty(isPlain) || !StringUtils.isNumeric(isPlain)){
 					// To do anything
 				}else{
 					folder_type = Long.decode(isPlain);
 				}
 			}catch (Exception e){
 				e.printStackTrace();
 			}
 			UserDto user = userAdminService.obtenerUsuario(username);
 			if (StringUtils.isEmpty(parentFolder)
 					|| !StringUtils.isNumeric(parentFolder)) {
 				FolderDto rootFolder = foldersAdminService.getRootFolder(user
 						.getId());
 				return saveFolderBy(name, enabled, isChannel, folder_type,
 						rootFolder != null ? rootFolder.getId() : null,
 						user.getId(), null);
 			} else {
 				return saveFolderBy(name, enabled, isChannel, folder_type,
 						Long.decode(parentFolder), user.getId(), null);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private FolderDto saveFolderBy(String name, String enabled, String isChannel,
 			Long folderTypeId, Long parentFolder, Long userId, Long groupId){
 		FolderDto folder = new FolderDto();
 		folder.setName(name);
 		folder.setEnabled(enabled != null ? enabled.toLowerCase().equals("true") : false);
 		folder.setIsChannel(isChannel != null ? isChannel.toLowerCase().equals("true") : false);
 		folder.setIdParent(parentFolder);
 		folder.setIdAuth(groupId);
 		folder.setIdUser(userId);
 		folder.setIdFolderType(folderTypeId);
 		
 		//TODO: folder.setZoneList(zoneList);
 		return foldersAdminService.saveFolder(folder);
 	}
 
 	/**
 	 * Load folders without layers
 	 */
 	public static final String FILTER_WITHOUT_LAYERS = "NO_LAYERS";
 
 	/**
 	 * Load folders with layers
 	 */
 	public static final String FILTER_WITH_LAYERS = "LAYERS";
 
 	/**
 	 * This method loads all folders related with a user
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with folders
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadFolders/{username}", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> loadFolders(@PathVariable String username,
 			@RequestParam(value = "filter", required = false) String filter) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<FolderDto> folders = null;
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			if (username != null) {
 				folders = new LinkedList<FolderDto>();
 				UserDto user = userAdminService.obtenerUsuario(username);
 				FolderDto rootFolder;
 				rootFolder = foldersAdminService.getRootFolder(user.getId());
 				FoldersUtils.getFolderTreeFiltered(rootFolder, folders,
 						filter != null ? new Boolean(filter) : null);
 			}
 			result.put(SUCCESS, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folders != null ? folders.size() : 0);
 		result.put(ROOT, folders);
 
 		return result;
 	}
 
 	/**
 	 * This method loads all folders related with a user
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with folders
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadFoldersByGroup/{idGroup}", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> loadFoldersByGroup(@PathVariable String idGroup,
 			@RequestParam(value = "filter", required = false) String filter) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<FolderDto> folders = null;
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			if (idGroup != null) {
 				folders = new LinkedList<FolderDto>();
 				FolderDto rootFolder;
 				if (filter != null) {
 					rootFolder = foldersAdminService.getRootGroupFolder(Long
 							.decode(idGroup));
 				} else {
 					rootFolder = foldersAdminService.getRootGroupFolder(Long
 							.decode(idGroup));
 				}
 				FoldersUtils.getFolderTreeFiltered(rootFolder, folders,
 						filter != null ? new Boolean(filter) : null);
 			}
 			result.put(SUCCESS, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folders != null ? folders.size() : 0);
 		result.put(ROOT, folders);
 
 		return result;
 	}
 
 	/**
 	 * Load folder tree by user or group styled
 	 * 
 	 * @param type
 	 *            user or group, default group
 	 * @param style
 	 *            tree or string, default string
 	 * @param userOrGroup
 	 *            id group or username
 	 * 
 	 * @see FoldersUtils
 	 * @see FolderStyle
 	 * 
 	 * @return List of user or group folders parsed with style
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadFolders/{type}/{style}/{userOrGroup}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> loadFolders(@PathVariable String type,
 			@PathVariable String style, @PathVariable String userOrGroup) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<FolderDto> folders = null;
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			FolderDto rootFolder;
 			folders = new LinkedList<FolderDto>();
 			if (LOAD_FOLDERS_BY_USER.equals(type)) {
 				UserDto user = userAdminService.obtenerUsuario(userOrGroup);
 				rootFolder = foldersAdminService.getRootFolder(user.getId());
 			} else {
 				rootFolder = foldersAdminService.getRootGroupFolder(Long
 						.decode(userOrGroup));
 			}
 			if (LOAD_FOLDERS_STYLE_TREE.equals(style)) {
 				FoldersUtils.getFolderTree(rootFolder, folders,
 						FolderStyle.TREE, null);
 			} else {
 				FoldersUtils.getFolderTree(rootFolder, folders);
 			}
 			result.put(SUCCESS, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folders != null ? folders.size() : 0);
 		result.put(ROOT, folders);
 
 		return result;
 	}
 
 	/**
 	 * Remove a folder and her children
 	 * 
 	 * @param folderId
 	 * 
 	 * @return JSON file with success
 	 */
 	@RequestMapping(value = "/persistenceGeo/deleteFolder", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> deleteFolder(@RequestParam("folderId") String folderId) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		FolderDto folder = null;
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			Long idFolder = Long.decode(folderId);
 			folder = (FolderDto) foldersAdminService.getById(idFolder);
 			foldersAdminService.delete(folder);
 			result.put(SUCCESS, true);
 			result.put(RESULTS, 1);
 			result.put(ROOT, "");
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 			result.put(RESULTS, 0);
 			result.put(ROOT, null);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Rename a folder
 	 * 
 	 * @param folderId
 	 * @param name
 	 * 
 	 * @return JSON file with success
 	 */
 	@RequestMapping(value = "/persistenceGeo/renameFolder", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> renameFolder(@RequestParam("folderId") String folderId,
 			@RequestParam("name") String name) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		FolderDto folder = null;
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			Long idFolder = Long.decode(folderId);
 			folder = (FolderDto) foldersAdminService.getById(idFolder);
 			folder.setName(name);
 			folder = (FolderDto) foldersAdminService.update(folder);
 			result.put(SUCCESS, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folder != null ? 1 : 0);
 		result.put(ROOT, folder);
 
 		return result;
 	}
 
 	/**
 	 * Clone user context
 	 * 
 	 * @param originUser
 	 * @param targetUser
 	 * @param merge
 	 *            indicates if actual user context must be deleted or merged
 	 * 
 	 * @return JSON file with success
 	 */
 	@RequestMapping(value = "/persistenceGeo/cloneUserContext", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> cloneUserContext(
 			@RequestParam("originUser") String originUser,
 			@RequestParam("targetUser") String targetUser,
 			@RequestParam(value = "merge", required = false) String merge) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		FolderDto folder = null;
 		try {
 			/*
 			 * //TODO: Secure with logged user String username = ((UserDetails)
 			 * SecurityContextHolder.getContext()
 			 * .getAuthentication().getPrincipal()).getUsername();
 			 */
 			Long originUserId = userAdminService.obtenerUsuario(originUser)
 					.getId();
 			Long targetUserId = userAdminService.obtenerUsuario(targetUser)
 					.getId();
 			folder = (FolderDto) foldersAdminService.copyUserContext(
 					originUserId, targetUserId, merge != null ? new Boolean(
 							merge) : false);
 			result.put(SUCCESS, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folder != null ? 1 : 0);
 		result.put(ROOT, folder);
 
 		return result;
 	}
 
 	/**
 	 * This method loads all folders or layers in a folder
 	 * 
 	 * @param idFolder
 	 * 
 	 * @return JSON file with folders
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadFoldersById/{idFolder}", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> loadFoldersById(@PathVariable String idFolder,
 			@RequestParam(value = "filter", required = false) String filter) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<Treeable> tree = null;
 		
 		List<String> filters;
 		if(!Strings.isNullOrEmpty(filter)){
 			filters = Arrays.asList(filter.split(","));
 		} else {
 			filters = new LinkedList<String>();
 		}
 		
 		boolean onlyChannelMark = filters.contains(ONLY_CHANNEL_MARK);
 		boolean onlyNotChannelMark = filters.contains(ONLY_NOT_CHANNEL_MARK);
 		boolean recursiveLoadMark = filters.contains(RECURSIVE_FOLDER_LAYERS_MARK);
 			
 		// add layers
 		Boolean isChannel = null;
 		if (onlyChannelMark) {
 			isChannel = Boolean.TRUE;
 		} else if (onlyNotChannelMark) {
 			isChannel = Boolean.FALSE;
 		}
 		
 		if (idFolder == null) {
 			result.put(SUCCESS, true);
 			return result;
 		}
 		
 		try {
 			
 			Long folderId = new Long(idFolder);
 			tree = new LinkedList<Treeable>();
 			
 			if(folderId.equals(UNASSIGNED_LAYERS_VIRTUAL_FOLDER_ID)){
 				List<LayerDto> previusLayers = layerAdminService.getUnassignedLayers();
 				for (LayerDto subRes : previusLayers) {
					tree.add(new TreeNode(subRes));
 				}
 			} else {
 				loadLayersInTree(folderId, isChannel, recursiveLoadMark, tree);
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 			return result;
 		}
 
 		result.put(SUCCESS, true);
 		result.put(RESULTS, tree != null ? tree.size() : 0);
 		result.put(ROOT, tree);
 
 		return result;
 	}
 	
 	private void loadLayersInTree(
 			Long folderId,  Boolean isChannel, boolean recursive, List<Treeable> tree) {
 		// add folders
 		List<FolderDto> childrenFolders = foldersAdminService
 				.findByZone(null, folderId, Boolean.TRUE);
 		if (childrenFolders != null && !childrenFolders.isEmpty()) {
 			for (FolderDto subRes : childrenFolders) {
 				if(!recursive) {
 					tree.add((Treeable) FoldersUtils.getFolderDecorator()
 							.applyStyle(subRes, FolderStyle.NORMAL));
 				} else {
 					loadLayersInTree(subRes.getId(), isChannel, true, tree);
 				}						
 			}
 		}
 		
 		List<LayerDto> previusLayers = layerAdminService
 				.getLayersByFolder(folderId, isChannel, Boolean.TRUE);
 		for (LayerDto subRes : previusLayers) {
 			tree.add(new TreeNode(subRes));
 		}
 	}
 
 	/**
 	 * This method loads all channel folders in a zone
 	 * 
 	 * @param filter
 	 *            can be null
 	 *            {@link RestFoldersAdminController#ALL_CHANNEL_IN_ZONES} or id
 	 *            zone to filter
 	 * 
 	 * @return JSON file with folders
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadChannels", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> loadChannels(
 			@RequestParam(value = "filter", required = false) String filter) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<FolderDto> folders = null;
 		List<FolderDto> previusFolders = null;
 		try {
 			if (filter != null) {
 				if (filter.contains(ALL_CHANNEL_IN_ZONES)) {
 					previusFolders = foldersAdminService.getChannelFolders(
 							Boolean.TRUE, null, Boolean.TRUE);
 				} else {
 					Long zoneId = null;
 					try {
 						zoneId = Long.decode(filter);
 						previusFolders = foldersAdminService.getChannelFolders(
 								null, zoneId, Boolean.TRUE);
 					} catch (Exception e) {
 						previusFolders = foldersAdminService.getChannelFolders(
 								Boolean.FALSE, null, Boolean.TRUE);
 					}
 				}
 
 				if (filter.contains(HIDE_IPT_CHANNELS)) {
 					List<FolderDto> filteredFolders = new ArrayList<FolderDto>();
 
 					for (FolderDto folder : previusFolders) {
 						if (folder.getIdFolderType() == null 
 							 || folder.getIdFolderType().equals(FoldersAdminService.DEFAULT_FOLDER_TYPE)) {
 							filteredFolders.add(folder);
 						}
 					}
 					previusFolders = filteredFolders;
 				}
 
 			} else {
 				previusFolders = foldersAdminService.getChannelFolders(
 						Boolean.FALSE, null, Boolean.TRUE);
 			}
 			folders = new LinkedList<FolderDto>();
 
 			for (FolderDto subRes : previusFolders) {
 				TreeFolderDto folder = (TreeFolderDto) FoldersUtils
 						.getFolderDecorator().applyStyle(subRes,
 								FolderStyle.NORMAL);
 				if (filter != null && filter.contains(SHOW_FOLDER_LAYERS)) {
 					folder.setLeaf(false);
 				} else {
 					folder.setLeaf(true);
 				}
 				folders.add(folder);
 			}
 			
 			result.put(SUCCESS, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folders != null ? folders.size() : 0);
 		result.put(ROOT, folders);
 
 		return result;
 	}
 
 	/**
 	 * Returns all the folders of a specific zone and optionaly with a specific
 	 * parent folder.
 	 * 
 	 * @param zone
 	 *            Zone the folder belongs to
 	 * @param parent
 	 *            Folder parent id
 	 * 
 	 * @return JSON file with success
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadFoldersByZone", produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> loadFoldersByZone(
 			@RequestParam(value = "zone", required = true) String zoneId,
 			@RequestParam(value = "parent", required = false) String parentId) {
 
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<FolderDto> folders = null;
 
 		try {
 
 			if (parentId == null) {
 				folders = (List<FolderDto>) foldersAdminService.findByZone(
 						new Long(zoneId), Boolean.TRUE);
 			} else {
 				folders = (List<FolderDto>) foldersAdminService.findByZone(
 						new Long(zoneId), new Long(parentId), Boolean.TRUE);
 			}
 			result.put(SUCCESS, true);
 
 		} catch (Exception e) {
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folders != null ? folders.size() : 0);
 		result.put(ROOT, folders != null ? folders : ListUtils.EMPTY_LIST);
 
 		return result;
 	}
 	
 	/**
 	 * Returns all the folders of a specific folder type
 	 * 
 	 * @param zone Zone the folder belongs to
      * @param parent Folder parent id
 	 *
 	 * @return JSON file with success
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadFoldersByFoldersType",
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> loadFoldersByFoldersType(@RequestParam(value="type", required=true) String typeId,
             @RequestParam(value="parent", required=false) String parentId) {
 
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<FolderDto> folders = null;
 
 		try {
 
             if (parentId == null) {
                 folders = (List<FolderDto>) foldersAdminService.findFoldersByType(new Long(typeId));
             } else {
                 //folders = (List<FolderDto>) foldersAdminService.findByZone(new Long(zoneId), new Long(parentId), Boolean.TRUE);
             }
 			result.put(SUCCESS, true);
 
 		} catch (Exception e) {
 			result.put(SUCCESS, false);
 		}
 
 		result.put(RESULTS, folders != null ? folders.size() : 0);
 		result.put(ROOT, folders != null ? folders : ListUtils.EMPTY_LIST);
 
 		return result;
     }
 }
