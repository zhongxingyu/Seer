 /*
  * ###
  * Phresco Service
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 
 package com.photon.phresco.service.converters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.springframework.data.document.mongodb.MongoOperations;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.TechnologyDAO;
 import com.photon.phresco.util.ServiceConstants;
 
 public class TechnologyConverter implements Converter<TechnologyDAO, Technology>, ServiceConstants {
 
     @Override
     public Technology convertDAOToObject(TechnologyDAO technologyDAO, MongoOperations mongoOperation) throws PhrescoException {
         Technology technology = new Technology(technologyDAO.getId());
         technology.setAppTypeId(technologyDAO.getAppTypeId());
         technology.setCreationDate(technologyDAO.getCreationDate());
         technology.setCustomerIds(technologyDAO.getCustomerIds());
         technology.setDescription(technologyDAO.getDescription());
         technology.setHelpText(technologyDAO.getHelpText());
         technology.setName(technologyDAO.getName());
         technology.setStatus(technologyDAO.getStatus());
         technology.setSystem(technologyDAO.isSystem());
         if (CollectionUtils.isNotEmpty(technologyDAO.getTechVersions())) {
         	technology.setTechVersions(technologyDAO.getTechVersions());
         }
         technology.setUsed(technologyDAO.isUsed());
         String archetypeGroupDAOId = technologyDAO.getArchetypeGroupDAOId();
         ArtifactGroupDAO artifactGrpDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
         		new Query(Criteria.whereId().is(archetypeGroupDAOId)), ArtifactGroupDAO.class);
         Converter<ArtifactGroupDAO, ArtifactGroup> artifactConverter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
         if (artifactGrpDAO != null) { 
 	        ArtifactGroup artifactGroup = artifactConverter.convertDAOToObject(artifactGrpDAO, mongoOperation);
 	        technology.setArchetypeInfo(artifactGroup);
         }
         if(CollectionUtils.isNotEmpty(technologyDAO.getPluginIds())) {
         	List<ArtifactGroupDAO> pluginsDAO = mongoOperation.find(ARTIFACT_GROUP_COLLECTION_NAME, 
             		new Query(Criteria.whereId().is(technologyDAO.getPluginIds().toArray())), ArtifactGroupDAO.class);
             List<ArtifactGroup> plugins = new ArrayList<ArtifactGroup>();
             if(pluginsDAO != null) {
             	for (ArtifactGroupDAO artifactGroupDAO : pluginsDAO) {
                 	ArtifactGroup artifactGroup = artifactConverter.convertDAOToObject(artifactGroupDAO, mongoOperation);
                 	plugins.add(artifactGroup);
         		}
                 technology.setPlugins(plugins);
             }
         }
         if(CollectionUtils.isNotEmpty(technologyDAO.getOptions())) {
         	technology.setOptions(technologyDAO.getOptions());
         }
         technology.setTechGroupId(technologyDAO.getTechGroupId());
         if(CollectionUtils.isNotEmpty(technologyDAO.getReports())) {
         	technology.setReports(technologyDAO.getReports());
         }
         return technology;
     }
 
     @Override
     public TechnologyDAO convertObjectToDAO(Technology technology) throws PhrescoException {
         TechnologyDAO techDAO = new TechnologyDAO();
         techDAO.setId(technology.getId());
         techDAO.setAppTypeId(technology.getAppTypeId());
         techDAO.setCustomerIds(technology.getCustomerIds());
         techDAO.setDescription(technology.getDescription());
         techDAO.setHelpText(technology.getHelpText());
         techDAO.setName(technology.getName());
         techDAO.setStatus(technology.getStatus());
         techDAO.setSystem(technology.isSystem());
         techDAO.setTechVersions(technology.getTechVersions());
         techDAO.setUsed(technology.isUsed());
        techDAO.setArchetypeGroupDAOId(technology.getArchetypeInfo().getId());
         List<ArtifactGroup> plugins = technology.getPlugins();
        if(plugins != null) {
        	techDAO.setPluginIds(createPluginId(plugins));
        }
         techDAO.setOptions(technology.getOptions());
         techDAO.setTechGroupId(technology.getTechGroupId());
         techDAO.setReports(technology.getReports());
         return techDAO;
     }
 
 	private List<String> createPluginId(List<ArtifactGroup> plugins) {
 		List<String> ids = new ArrayList<String>();
 		for (ArtifactGroup artifactGroup : plugins) {
 			ids.add(artifactGroup.getId());
 		}
 		return ids;
 	}
 }
