 /**
  *    Copyright 2012-2013 Trento RISE
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
  */
 
 package eu.trentorise.smartcampus.filestorage.managers;
 
 import it.unitn.disi.sweb.webapi.client.WebApiException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import eu.trentorise.smartcampus.filestorage.model.Account;
 import eu.trentorise.smartcampus.filestorage.model.AlreadyStoredException;
 import eu.trentorise.smartcampus.filestorage.model.Metadata;
 import eu.trentorise.smartcampus.filestorage.model.NotFoundException;
 import eu.trentorise.smartcampus.filestorage.model.Resource;
 import eu.trentorise.smartcampus.filestorage.model.SmartcampusException;
 import eu.trentorise.smartcampus.filestorage.services.MetadataService;
 import eu.trentorise.smartcampus.social.model.User;
 
 /**
  * <i>MetadataManager</i> manages all aspects about {@link Metadata} of a
  * resource.
  * 
  * @author mirko perillo
  * 
  */
 @Service
 public class MetadataManager {
 
 	private static final Logger logger = Logger.getLogger(Metadata.class);
 	@Autowired
 	MetadataService metadataSrv;
 
 	@Autowired
 	SocialManager socialManager;
 
 	@Autowired
 	AccountManager accountManager;
 
 	/**
 	 * creates and saves metadata info for given {@link Resource}
 	 * 
 	 * @param accountId
 	 *            the id of user storage account in which resource is stored
 	 * @param user
 	 *            user owner of the resource
 	 * @param resource
 	 *            the resource
 	 * @param createSocialData
 	 *            true to create social information to associate to the resource
 	 * @return metadata created
 	 * @throws AlreadyStoredException
 	 *             if metadata already exists
 	 * @throws SmartcampusException
 	 *             general exception
 	 */
 	public Metadata create(String accountId, User user, Resource resource,
 			boolean createSocialData) throws AlreadyStoredException,
 			SmartcampusException {
 
 		return metadataSrv.save(toMetadata(accountId, user, resource,
 				createSocialData));
 	}
 
 	/**
 	 * deletes {@link Metadata}
 	 * 
 	 * @param resourceId
 	 *            the id of the resource binded with metadata
 	 * @throws NotFoundException
 	 *             if metadata for given resource doesn't exist
 	 * @throws SmartcampusException
 	 *             general exception
 	 */
 	public void delete(String resourceId) throws NotFoundException,
 			SmartcampusException {
 		String eid = null;
 		try {
 			eid = metadataSrv.getEntityByResource(resourceId);
 			metadataSrv.delete(resourceId);
 			logger.info(String.format("Deleted metadata of resource %s",
 					resourceId));
 			if (eid != null) {
 				socialManager.deleteEntity(Long.parseLong(eid));
 				logger.info(String.format(
 						"Deleted entity associated to resource %s", resourceId));
 			} else {
 				logger.info(String.format(
 						"Resource %s not associated to any entity", resourceId));
 			}
 		} catch (NumberFormatException e) {
 			logger.error("Exception parsing entity id: " + eid);
 			throw new NotFoundException();
 		} catch (WebApiException e) {
 			logger.error("Exception invoking social engine", e);
 			throw new SmartcampusException(
 					"Social engine error deleting entity");
 		}
 	}
 
 	/**
 	 * updates a {@link Metadata}. Only field lastModifiedTs is updated
 	 * 
 	 * @param resource
 	 *            resource to update
 	 * @throws NotFoundException
 	 *             if metadata for given resource doesn't exist
 	 */
 	public void update(Resource resource) throws NotFoundException {
 		Metadata metadata = metadataSrv.getMetadata(resource.getId());
 		metadata.setSize(resource.getContent().length);
 		metadata.setLastModifiedTs(System.currentTimeMillis());
 		metadataSrv.update(metadata);
 	}
 
 	/**
 	 * retrieves a {@link Metadata} given binded resource id
 	 * 
 	 * @param resourceId
 	 *            the resource id
 	 * @return {@link Metadata} binded to resource
 	 * @throws NotFoundException
 	 *             if metadata doesn't exist
 	 */
 	public Metadata findByResource(String resourceId) throws NotFoundException {
 		return metadataSrv.getMetadata(resourceId);
 	}
 
 	public List<Metadata> findAllBy(String appId, String userId,
 			Integer position, Integer size) throws NotFoundException {
 		List<Metadata> metadata = new ArrayList<Metadata>();
 		if (appId != null && userId != null) {
 			List<Account> accounts = accountManager.findBy(userId, appId);
 			List<String> accountIds = new ArrayList<String>();
 			for (Account account : accounts) {
 				accountIds.add(account.getId());
 			}
 			metadata = metadataSrv.getMetadataByAccountIds(accountIds,
 					position, size);
 		} else if (appId != null) {
 			metadata = metadataSrv.getMetadataByApp(appId, position, size);
 		} else {
 			throw new IllegalArgumentException(
 					"userId and appId cannot be either null");
 		}
 
 		return metadata;
 	}
 
 	public String getOwner(String resourceId) throws NotFoundException {
 		Metadata meta = metadataSrv.getMetadata(resourceId);
 		Account account = accountManager.findById(meta.getAccountId());
 		return account.getUserId();
 	}
 
 	/**
 	 * updates the social data relative to a resource
 	 * 
 	 * @param resourceId
 	 *            the resource id
 	 * @param entityId
 	 *            social id to associate to the resource
 	 * @return the information about the resource updated
 	 * @throws NotFoundException
 	 *             social entity not found
 	 * @throws SecurityException
 	 *             social entity is not owned by the user
 	 */
 	public Metadata updateSocialData(User owner, String resourceId,
 			String entityId) throws NotFoundException, SecurityException {
 		Metadata meta = findByResource(resourceId);
 
 		// check if entityId is owned by owner of the resource
 		if (socialManager.isOwnedBy(owner, entityId)) {
 			meta.setSocialId(entityId);
 			metadataSrv.update(meta);
 		} else {
 			throw new SecurityException("Entity is not owned by the user");
 		}
 		return meta;
 	}
 
 	public Metadata toMetadata(String accountId, User user, Resource resource,
 			boolean createSocialData) throws SmartcampusException {
 		Metadata metadata = new Metadata();
 		metadata.setContentType(resource.getContentType());
 		metadata.setCreationTs(System.currentTimeMillis());
 		metadata.setName(resource.getName());
 		metadata.setResourceId(resource.getId());
 		metadata.setAccountId(accountId);
 		metadata.setFileExternalId(resource.getName());
 		metadata.setSize(resource.getContent() != null ? resource.getContent().length
				: 0);
 		// account data
 		Account userAccount;
 		try {
 			userAccount = accountManager.findById(accountId);
 			metadata.setAppId(userAccount.getAppId());
 		} catch (NotFoundException e1) {
 			logger.error(String.format("userAccount not found: %s", accountId));
 			throw new SmartcampusException("Account not found");
 		}
 		if (createSocialData) {
 			try {
 				metadata.setSocialId(socialManager.createEntity(resource, user)
 						.toString());
 			} catch (WebApiException e) {
 				logger.error("Exception invoking social engine", e);
 				throw new SmartcampusException(
 						"Social engine error creating entity");
 			}
 		}
 		return metadata;
 	}
 }
