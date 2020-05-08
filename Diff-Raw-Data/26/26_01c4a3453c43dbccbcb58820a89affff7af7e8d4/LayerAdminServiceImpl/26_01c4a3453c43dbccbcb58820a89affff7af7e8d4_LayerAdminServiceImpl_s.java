 /*
  * LayerAdminServiceImpl.java
  * 
  * Copyright (C) 2011
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
  * Authors:: Moisés Arcos Santiago (mailto:marcos@emergya.com)
  */
 package com.emergya.persistenceGeo.service.impl;
 
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.io.FileUtils;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.emergya.persistenceGeo.dao.AbstractGenericDao;
 import com.emergya.persistenceGeo.dao.AuthorityEntityDao;
 import com.emergya.persistenceGeo.dao.FolderEntityDao;
 import com.emergya.persistenceGeo.dao.GenericDAO;
 import com.emergya.persistenceGeo.dao.LayerEntityDao;
 import com.emergya.persistenceGeo.dao.LayerTypeEntityDao;
 import com.emergya.persistenceGeo.dao.RuleEntityDao;
 import com.emergya.persistenceGeo.dao.StyleEntityDao;
 import com.emergya.persistenceGeo.dao.UserEntityDao;
 import com.emergya.persistenceGeo.dto.FolderDto;
 import com.emergya.persistenceGeo.dto.LayerDto;
 import com.emergya.persistenceGeo.dto.RuleDto;
 import com.emergya.persistenceGeo.dto.StyleDto;
 import com.emergya.persistenceGeo.metaModel.AbstractAuthorityEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractFolderEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractLayerEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractLayerPropertyEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractLayerTypeEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractLayerTypePropertyEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractRuleEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractRulePropertyEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractStyleEntity;
 import com.emergya.persistenceGeo.metaModel.AbstractUserEntity;
 import com.emergya.persistenceGeo.metaModel.Instancer;
 import com.emergya.persistenceGeo.model.LayerEntity;
 import com.emergya.persistenceGeo.service.LayerAdminService;
 
 /**
  * LayerAdminService transactional implementation based on daos uses
  * {@link AbstractGenericDao}
  * 
  * @author <a href="mailto:marcos@emergya.com">marcos</a>
  * 
  */
 @SuppressWarnings("unchecked")
 @Repository
 @Transactional
 public class LayerAdminServiceImpl extends AbstractServiceImpl<LayerDto, AbstractLayerEntity>
 		implements LayerAdminService {
 
 	@Resource
 	private Instancer instancer;
 	@Resource
 	private LayerEntityDao layerDao;
 	@Resource
 	private LayerTypeEntityDao layerTypeDao;
 	@Resource
 	private StyleEntityDao styleDao;
 	@Resource
 	private RuleEntityDao ruleDao;
 	@Resource
 	private UserEntityDao userDao;
 	@Resource
 	private AuthorityEntityDao authDao;
 	@Resource
 	private FolderEntityDao folderDao;
 	
 	public LayerAdminServiceImpl(){
 		super();
 	}
 
 	/**
 	 * Get a layer list by name
 	 * 
 	 * @param layerName
 	 * 
 	 * @return If not found, it's created
 	 */
 	@Cacheable("persistenceGeo")
 	public List<LayerDto> getLayersByName(String layerName) {
 		List<LayerDto> layersDto = new LinkedList<LayerDto>();
 		LayerDto dto = null;
 		List<AbstractLayerEntity> layers = layerDao.getLayers(layerName);
 		for(AbstractLayerEntity l: layers){
 			dto = entityToDto(l);
 			if(dto == null){
 				dto = entityToDto(layerDao.createLayer(layerName));
 			}
 			layersDto.add(dto);
 		}
 		return layersDto;
 	}
 	
 	/**
 	 * Get a layer list by names list
 	 * 
 	 * @param namesList
 	 * 
 	 * @return If not found, it's created
 	 */
 	@Cacheable("persistenceGeo")
 	public List<LayerDto> getLayersByName(List<String> namesList) {
 		List<LayerDto> layersDto = new LinkedList<LayerDto>();
 		for(String name: namesList){
 			layersDto.addAll(this.getLayersByName(name));
 		}
 		return layersDto;
 	}
 
 	/**
 	 * Add a rule to a layer style
 	 * 
 	 * @param styleID
 	 * @param ruleID
 	 * 
 	 */
 	public void addRuleToStyleLayer(Long styleID, Long ruleID) {
 		AbstractStyleEntity styleEntity = styleDao.findById(styleID, false);
 		List<AbstractRuleEntity> rules = styleEntity.getRuleList();
 		if(rules == null){
 			rules = new LinkedList<AbstractRuleEntity>();
 		}
 		boolean enc = false;
 		for(AbstractRuleEntity re: rules){
 			if(re.getId().equals(ruleID)){
 				enc = true;
 				break;
 			}
 		}
 		if(!enc){
 			rules.add(ruleDao.findById(ruleID, false));
 			styleEntity.setRuleList(rules);
 			styleDao.makePersistent(styleEntity);
 		}
 	}
 
 	/**
 	 * Add a style to a layer
 	 * 
 	 * @param layerID
 	 * @param styleID
 	 * 
 	 */
 	public void addStyleToLayer(Long layerID, Long styleID) {
 		AbstractLayerEntity layerEntity = layerDao.findById(layerID, false);
 		AbstractStyleEntity style = styleDao.findById(styleID, false);
 		layerEntity.setStyleList(addToList(layerEntity.getStyleList(), style));
 		layerDao.save(layerEntity);
 	}
 	
 	/**
 	 * Add authorities to a layer
 	 * 
 	 * @param auth_id
 	 * @param styleID
 	 * 
 	 */
 	public void addAuthoritiesToLayer(Long auth_id, Long layer_id) {
 		AbstractLayerEntity entity = layerDao.findById(layer_id, false);
 		AbstractAuthorityEntity authority = entity.getAuth();
 		if(authority == null){
 			authority = instancer.createAuthority();
 		}
 		boolean enc = false;
 		if(authority.getId().equals(auth_id)){
 			enc = true;
 		}
 		
 		if(!enc){
 			entity.setAuth(authDao.findById(auth_id, false));
 			layerDao.save(entity);
 		}
 	}
 
 	/**
 	 * Add a user to a layer
 	 * 
 	 * @param user_id
 	 * @param layer_id
 	 * 
 	 */
 	public void addUserToLayer(Long user_id, Long layer_id) {
 		AbstractLayerEntity entity = layerDao.findById(layer_id, false);
 		AbstractUserEntity user = userDao.findById(user_id, false);
 		entity.setUser(user);
 		layerDao.save(entity);
 	}
 	
 	/**
 	 * Add a folder to a layer
 	 * 
 	 * @param folder_id
 	 * @param layer_id
 	 * 
 	 */
 	public void addFolderToLayer(Long folder_id, Long layer_id){
 		AbstractLayerEntity entity = layerDao.findById(layer_id, false);
 		AbstractFolderEntity folder = folderDao.findById(folder_id, false);
 		entity.setFolder(folder);
 		layerDao.save(entity);
 	}
 
 	/**
 	 * Get layers by folder
 	 * 
 	 * @param folderId to be loaded
 	 * 
 	 * @return all layers in the folder
 	 */
 	public List<LayerDto> getLayersByFolder(Long folderId){
 		return (List<LayerDto>) entitiesToDtos(layerDao.getLayersByFolder(folderId));
 	}
 
 	/**
 	 * Get layers by folder
 	 * 
 	 * @param folderId to be loaded
 	 * @param isChannel
 	 * @param isEnabled
 	 * 
 	 * @return all layers in the folder
 	 */
 	public List<LayerDto> getLayersByFolder(Long folderId, Boolean isChannel, Boolean isEnabled){
 		return (List<LayerDto>) entitiesToDtos(layerDao.getLayersByFolder(folderId, isChannel, isEnabled));
 	}
 
 	protected LayerDto entityToDto(AbstractLayerEntity entity) {
 		LayerDto dto = null;
 		if(entity != null){
 			dto = new LayerDto();
 			// Add own parameters
 			dto.setId(entity.getId());
 			dto.setName(entity.getName());
 			dto.setOrder(entity.getOrder());
 			dto.setServer_resource(entity.getServer_resource());
 			dto.setPublicized(entity.getPublicized());
 			dto.setEnabled(entity.getEnabled());
 			dto.setPertenece_a_canal(entity.getIsChannel());
 			dto.setCreateDate(entity.getCreateDate());
 			dto.setUpdateDate(entity.getUpdateDate());
 			
 			//Layer type
			if(entity.getType() != null
					&& entity.getType().getName() != null){
				dto.setType(entity.getType().getName());
 			}
 			
 			if(entity.getData() != null){
 				try {
 					File file = com.emergya.persistenceGeo.utils.FileUtils.createFileTemp(entity.getName(), entity.getType() != null ? entity.getType().getName() : "xml");
 					FileUtils.writeByteArrayToFile(file, entity.getData());
 					dto.setData(file);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			// Add relational parameters
 			// Add users
 			AbstractUserEntity user = layerDao.findByLayer(entity.getId());
 			if(user != null){
 				dto.setUser(user.getNombreCompleto());
 			}
 			// Add authority
 			dto.setAuthId(entity.getAuth() != null ? entity.getAuth().getId() : null);
 			// Add style
 			Map<StyleDto, Map<RuleDto, Map<String, String>>> styles = new HashMap<StyleDto, Map<RuleDto, Map<String, String>>>();
 			if(entity.getStyleList() != null){
 				for (Object style: entity.getStyleList()){
 					StyleDto styleDto = entityStyleToDto((AbstractStyleEntity) style);
 					styles.put(styleDto, styleDto.getRules());
 				}
 			}
 			dto.setStyles(styles);
 			// Add folder
 			dto.setFolderId(entity.getFolder() != null ? entity.getFolder().getId(): null);
 			
 			// Properties
 			if(entity.getProperties() != null && entity.getProperties().size()>0){
 				Map<String, String> properties = new HashMap<String, String>();
 				List<AbstractLayerPropertyEntity> propertiesList = entity.getProperties();
 				for(AbstractLayerPropertyEntity property: propertiesList){
 					properties.put(property.getName(), property.getValue());
 				}
 				dto.setProperties(properties);
 			}
 		}
 		return dto;
 	}
 
 	protected AbstractLayerEntity dtoToEntity(LayerDto dto) {
 		AbstractLayerEntity entity = null;
 		if(dto != null){
 			Date now = new Date();
 			if(dto.getId() != null && dto.getId() > 0){
 				entity = (AbstractLayerEntity) layerDao.findById(dto.getId(), false);
 				//Grupos
 //				authDao.clearUser(dto.getId());
 			}else{
 				entity =  instancer.createLayer();
 				entity.setCreateDate(now);
 			}
 			
 			// Properties
 			if(dto.getProperties() != null && dto.getProperties().size()>0){
 				List<AbstractLayerPropertyEntity> propertiesList = new LinkedList<AbstractLayerPropertyEntity>();
 				for(String name: dto.getProperties().keySet()){
 					AbstractLayerPropertyEntity property = instancer.createLayerProperty();
 					property.setName(name);
 					property.setValue(dto.getProperties().get(name));
 					propertiesList.add(property);
 				}
 				entity.setProperties(propertiesList);
 			}
 			
 			// Add own parameters
 			//entity.setId(dto.getId());
 			entity.setName(dto.getName());
 			entity.setOrder(dto.getOrder());
 			entity.setServer_resource(dto.getServer_resource());
 			entity.setPublicized(dto.getPublicized());
 			entity.setEnabled(dto.getEnabled());
 			entity.setIsChannel(dto.getPertenece_a_canal());
 			entity.setUpdateDate(now);
 			
 			//Layer type
 			if(dto.getType() != null){
 				entity.setType(layerTypeDao.getLayerType(dto.getType()));
 			}
 			
 			//Layer data
 			if(dto.getData() != null){
 				try {
 					entity.setData(FileUtils.readFileToByteArray(dto.getData()));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 			// Add relational parameters
 			// Add users
 			String usersDto = dto.getUser();
 			if(usersDto != null){
 				AbstractUserEntity user = userDao.getUser(usersDto);
 				if(user != null){
 					entity.setUser(user);
 				}
 			}
 			// Add authorities
 			Long authId = dto.getAuthId();
 			if(authId != null){
 				entity.setAuth(authDao.findById(authId, false));
 			}
 			// Add style
 			List<AbstractEntity> styleList = new LinkedList<AbstractEntity>();
 			if(dto.getStyles() != null){
 				for (StyleDto styleDto: dto.getStyles().keySet()){
 					styleList =  addToList(styleList, dtoStyleToEntity(styleDto, entity));
 				}
 			}
 			entity.setStyleList(styleList);
 			// Add folder
 			if(dto.getFolderId() != null){
 				entity.setFolder(folderDao.findById(dto.getFolderId(), false));
 			}
 		}
 		return entity;
 	}
 
 	private StyleDto entityStyleToDto(AbstractStyleEntity entity){
 		StyleDto dto = null;
 		if(entity != null){
 			dto = new StyleDto();
 			// Add own attributes
 			dto.setId(entity.getId());
 			dto.setName(entity.getName());
 			dto.setCreateDate(entity.getCreateDate());
 			dto.setUpdateDate(entity.getUpdateDate());
 			// Add relational attributes
 			// Add layer
 //			List<AbstractLayerEntity> layers = entity.getLayerList();
 //			if(layers != null 
 //					&& layers.size()>0){
 //				dto.setLayerId(layers.get(0).getId());
 //			}
 			// Add rules
 			Map<RuleDto, Map<String, String>> rulesDto = new HashMap<RuleDto, Map<String,String>>();
 			List<AbstractRuleEntity> rules = entity.getRuleList();
 			if(rules != null){
 				for(AbstractRuleEntity ruleEntity: rules){
 					Map<String, String> properties = new HashMap<String, String>();
 					List<AbstractRulePropertyEntity> ruleProperties = ruleEntity.getProperties();
 					for(AbstractRulePropertyEntity property: ruleProperties){
 						properties.put(property.getName(), property.getValue());
 					}
 					rulesDto.put(ruleEntityToDto(ruleEntity, dto.getName()), properties);
 				}
 			}
 			dto.setRules(rulesDto);
 		}
 		return dto;
 	}
 	
 	private RuleDto ruleEntityToDto(AbstractRuleEntity entity, String styleName){
 		RuleDto dto = null;
 		if(entity != null){
 			dto = new RuleDto();
 			// Add own attributes
 			dto.setRule_id((Long )entity.getId());
 			dto.setSymbolizer(entity.getSymbolizer());
 			dto.setFilter(entity.getFilter());
 			dto.setCreateDate(entity.getCreateDate());
 			dto.setUpdateDate(entity.getUpdateDate());
 			// Add relational attributes
 			// Add style
 			dto.setStyle(styleName);
 		}
 		return dto;
 	}
 
 	@Override
 	@Cacheable("persistenceGeo")
 	public List<LayerDto> getLayersByUser(Long idUser) {
 		return (List<LayerDto>) entitiesToDtos(layerDao.findByUserId(idUser));
 	}
 
 	@Override
 	@Cacheable("persistenceGeo")
 	public List<LayerDto> getPublicLayers() {		
 		return (List<LayerDto>) entitiesToDtos(layerDao.getPublicLayers());
 	}
 	
 	@Override
 	@Cacheable("persistenceGeo")
 	public List<LayerDto> getLayersByAuthority(Long id) {
 		return (List<LayerDto>) entitiesToDtos(layerDao.findByAuthorityId(id));
 	}
 
 	/**
 	 * Get a layer list by authority id
 	 * 
 	 * @param layerName
 	 * @param isChannel indicates if layers can be channel layers 
 	 * 
 	 * @return list
 	 */
 	public List<LayerDto> getLayersByAuthority(Long id, Boolean isChannel){
 		return (List<LayerDto>) entitiesToDtos(layerDao.findByAuthorityId(id, isChannel));
 	}
 
 	@Cacheable("persistenceGeo")
 	public List<String> getAllLayerTypes() {
 		List<String> result = new LinkedList<String>();
 		
 		for(AbstractLayerTypeEntity layerType: layerTypeDao.findAll()){
 			result.add(layerType.getName());
 		}
 		
 		return result;
 	}
 
 	@Cacheable("persistenceGeo")
 	public List<String> getAllLayerTypeProperties(String layerType) {
 		List<String> result = new LinkedList<String>();
 		
 		for(AbstractLayerTypePropertyEntity layerTypeProperty: layerTypeDao.getLayerTypeProperties(layerType)){
 			result.add(layerTypeProperty.getName());
 		}
 		
 		return result;
 	}
 
 	public List<LayerDto> getLayersByFolder(Long folderId, Boolean isChannel) {
 		return (List<LayerDto>) entitiesToDtos(layerDao.getLayersByFolder(folderId, isChannel, Boolean.TRUE));
 	}
 	
 	/**
 	 * Saves a folder
 	 * 
 	 * @return saved folder
 	 */
 	public FolderDto saveFolder(FolderDto folder){
 		AbstractFolderEntity entity = dtoFolderToEntity(folder);
 		return entityFolderToDto(folderDao.makePersistent(entity));
 	}
 
 	private FolderDto entityFolderToDto(AbstractFolderEntity entity) {
 		FolderDto dto = null;
 		if(entity != null){
 			dto = new FolderDto();
 			dto.setEnabled(entity.getEnabled());
 			dto.setIsChannel(entity.getIsChannel());
 			dto.setUpdateDate(entity.getUpdateDate());
 			dto.setCreateDate(entity.getCreateDate());
 			dto.setId(entity.getId());
 			dto.setName(entity.getName());
 			
 			
 			//Children
 			List<AbstractFolderEntity> children = folderDao.getFolders(entity.getId());
 			if(children != null){
 				List<FolderDto> subFolders = new LinkedList<FolderDto>();
 				for(AbstractFolderEntity child: children){
 					//Recursive case
 					subFolders.add(entityFolderToDto(child));
 				}
 				dto.setFolderList(subFolders);
 			}//else: base case
 			
 			//Parent
 			if(entity.getParent() != null
 					&& entity.getParent().getId() != null){
 				dto.setIdParent(entity.getParent().getId());
 			}
 
 			//Auth
 			if(entity.getAuthority() != null
 					&& entity.getAuthority().getId() != null){
 				dto.setIdAuth(entity.getAuthority().getId());
 			}
 
 			//User
 			if(entity.getUser() != null
 					&& entity.getUser().getId() != null){
 				dto.setIdUser((Long) entity.getUser().getId());
 			}
 			
 			//TODO: entity.setZoneList(zoneList);
 		}
 		return dto;
 	}
 
 	private AbstractFolderEntity dtoFolderToEntity(FolderDto dto) {
 		AbstractFolderEntity entity = null;
 		if(dto != null){
 			if(dto.getId() != null){
 				entity = folderDao.findById(dto.getId(), true);
 			}else{
 				entity = instancer.createFolder();
 			}
 			entity.setEnabled(dto.getEnabled());
 			entity.setIsChannel(dto.getIsChannel());
 			entity.setUpdateDate(dto.getUpdateDate());
 			entity.setCreateDate(dto.getCreateDate());
 			entity.setName(dto.getName());
 			
 			//TODO: Children if is necesary
 			
 			//Parent
 			if (dto.getIdParent() != null) {
 				AbstractFolderEntity parent = folderDao.findById(dto.getIdParent(), false);
 				entity.setParent(parent);
 			}
 			
 			//Auth & user
 			if(dto.getIdAuth() != null){
 				entity.setAuthority(authDao.findById(dto.getIdAuth(), false));
 			}
 			if(dto.getIdUser() != null){
 				entity.setUser(userDao.findById(dto.getIdUser(), false));
 			}
 			
 			//TODO: entity.setZoneList(zoneList);
 		}
 		return entity;
 	}
 
 	private AbstractStyleEntity dtoStyleToEntity(StyleDto dto, AbstractLayerEntity layerEntity) {
 		AbstractStyleEntity entity = null;
 		if(dto != null){
 			if(dto.getId() != null){
 				entity = styleDao.findById(dto.getId(), true);
 			}else{
 				entity = instancer.createStyle();
 			}
 			entity.setName(dto.getName());
 			
 			//Rules
 			List<AbstractRuleEntity> rules = new LinkedList<AbstractRuleEntity>();
 			if(dto.getRules() != null
 					&& !dto.getRules().isEmpty()){
 				for(RuleDto ruleDto: dto.getRules().keySet()){
 					rules.add(ruleDtoToEntity(ruleDto, dto.getRules().get(ruleDto)));
 				}
 			}
 			entity.setRuleList(rules);
 
 			//Layer list
 //			entity.setLayerList(addToList(entity.getLayerList(), layerEntity));
 		}
 		return entity;
 	}
 	
 	/**
 	 * Adds a entity to a list or initialize it
 	 * 
 	 * @param entities
 	 * @param toAdd
 	 * 
 	 * @return List with toAdd added
 	 */
 	private List<AbstractEntity> addToList(List<AbstractEntity> entities, AbstractEntity toAdd){
 		//Layer list
 		boolean alreadyAdded = false;
 		List<AbstractEntity> result;
 		if(entities != null 
 				&& !entities.isEmpty()){
 			result = entities;
 			for(AbstractEntity entity: entities){
 				if(entity.equals(toAdd)){
 					alreadyAdded = true;
 					break;
 				}
 			}
 		}else{
 			result = new LinkedList<AbstractEntity>();
 		}
 		if(!alreadyAdded){
 			result.add(toAdd);
 		}
 		return result;
 	}
 
 	private AbstractRuleEntity ruleDtoToEntity(RuleDto ruleDto,
 			Map<String, String> properties) {
 		AbstractRuleEntity entity = null;
 		if(ruleDto != null){
 			Date now = new Date();
 			if(ruleDto.getRule_id() != null){
 				ruleDao.findById(ruleDto.getRule_id(), true);
 			}else{
 				entity = ruleDao.createRule();
 				entity.setCreateDate(now);
 			}
 			entity.setFilter(ruleDto.getFilter());
 			entity.setUpdateDate(now);
 			entity.setSymbolizer(ruleDto.getSymbolizer());
 			//TODO? entity.setStyle(style);
 			
 			//Rule properties
 			List<AbstractRulePropertyEntity> ruleProperties = new LinkedList<AbstractRulePropertyEntity>();
 			if(properties != null){
 				for(String name: properties.keySet()){
 					AbstractRulePropertyEntity ruleProperty = instancer.createRulePropertyEntity();
 					ruleProperty.setName(name);
 					ruleProperty.setValue(properties.get(name));
 					ruleProperties.add(ruleProperty);
 				}
 			}
 			entity.setProperties(ruleProperties);
 		}
 		return entity;
 	}
 
 	@Override
 	protected GenericDAO<AbstractLayerEntity, Long> getDao() {
 		return layerDao;
 	}
 
 	@Override
 	public void deleteLayerById(Long layerId) {
 		// Delete the layer properties
 		
 		layerDao.delete(layerId);
 	}
 }
