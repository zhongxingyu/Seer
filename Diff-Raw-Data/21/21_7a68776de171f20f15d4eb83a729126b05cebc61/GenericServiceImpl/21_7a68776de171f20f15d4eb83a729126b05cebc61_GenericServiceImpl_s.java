 /**
  *
  *
  * Copyright (c) 2007 - 2013 www.Abiss.gr
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package gr.abiss.calipso.jpasearch.service.impl;
 
 import gr.abiss.calipso.ddd.core.model.interfaces.MetadataSubject;
 import gr.abiss.calipso.ddd.core.model.interfaces.Metadatum;
 import gr.abiss.calipso.jpasearch.model.acl.AclClass;
 import gr.abiss.calipso.jpasearch.model.acl.AclObject;
 import gr.abiss.calipso.jpasearch.model.acl.AclObjectIdentity;
 import gr.abiss.calipso.jpasearch.model.acl.AclSid;
 import gr.abiss.calipso.jpasearch.repository.BaseRepository;
 import gr.abiss.calipso.jpasearch.repository.acl.AclClassRepository;
 import gr.abiss.calipso.jpasearch.repository.acl.AclObjectIdentityRepository;
 import gr.abiss.calipso.jpasearch.repository.acl.AclSidRepository;
 import gr.abiss.calipso.jpasearch.service.GenericService;
 import gr.abiss.calipso.userDetails.util.SecurityUtil;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.resthub.common.service.CrudServiceImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.Persistable;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.CollectionUtils;
 
 /**
  * Provides access to domain class type and transactional write support
  */
 @Transactional(readOnly = true)
 public abstract class GenericServiceImpl<T extends Persistable<ID>, ID extends Serializable, R extends BaseRepository<T, ID>>
 		extends CrudServiceImpl<T, ID, R> implements GenericService<T, ID> {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(GenericServiceImpl.class);
 
 	private AclObjectIdentityRepository aclObjectIdentityRepository;
 	private AclClassRepository aclClassRepository;
 	private AclSidRepository aclSidRepository;
 
 	@Inject
 	public void setAclObjectIdentityRepository(
 			AclObjectIdentityRepository aclObjectIdentityRepository) {
 		this.aclObjectIdentityRepository = aclObjectIdentityRepository;
 	}
 
 	@Inject
 	public void setAclClassRepository(AclClassRepository aclClassRepository) {
 		this.aclClassRepository = aclClassRepository;
 	}
 
 	@Inject
 	public void setAclSidRepository(AclSidRepository aclSidRepository) {
 		this.aclSidRepository = aclSidRepository;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
 	public Class<T> getDomainClass() {
 		return this.repository.getDomainClass();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	@Transactional(readOnly = false)
 	public T create(T resource) {
 		Map<String, Metadatum> metadata = noteMetadata(resource);
 		T saved = super.create(resource);
 		createAclObjectIdentity(saved, this.getDomainClass());
 		persistNotedMetadata(metadata, saved);
 		return saved;
 	}
 
 	protected void createAclObjectIdentity(T saved, Class domainClass) {
 		if (AclObject.class.isAssignableFrom(domainClass)) {
 			AclObject<ID, ID> aclObject = (AclObject<ID, ID>) saved;
			AclClass aclClass = this.aclClassRepository	.findByClassName(domainClass.getName());
			
 			Serializable sid = aclObject.getOwner();
 			AclSid aclSid = null;
 			// use current principal as owner?
			if(sid == null){
 				UserDetails userDetails = SecurityUtil.getPrincipal();
 				if (userDetails != null) {
 					sid = userDetails.getUsername();
 				}
 			}
			
 			// add owner if any
			if(sid != null){
 				aclSid = this.aclSidRepository.findBySid(sid.toString());
 			}
				
 			// TODO: parent
 			this.aclObjectIdentityRepository.save(new AclObjectIdentity(
 					aclObject.getIdentity().toString(), aclClass, null, aclSid,
					aclObject
					.getEntriesInheriting()));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public T update(T resource) {
 		LOGGER.info("update, resource: " + resource);
 		Map<String, Metadatum> metadata = noteMetadata(resource);
 		T saved = super.update(resource);
 		persistNotedMetadata(metadata, saved);
 		return saved;
 	}
 
 
 	private void persistNotedMetadata(Map<String, Metadatum> metadata, T saved) {
 
 		LOGGER.info("persistNotedMetadata, noted: " + metadata);
 		if (!CollectionUtils.isEmpty(metadata)) {
 			MetadataSubject subject = (MetadataSubject) saved;
 			Metadatum[] metaArray = metadata.values().toArray(
 					new Metadatum[metadata.values().size()]);
 			for (int i = 0; i < metaArray.length; i++) {
 				Metadatum metadatum = metaArray[i];
 				subject.addMetadatum(this.repository.addMetadatum(
 						saved.getId(), metadatum.getPredicate(),
 						metadatum.getObject()));
 			}
 		}
 	}
 
 	private Map<String, Metadatum> noteMetadata(T resource) {
 		Map<String, Metadatum> metadata = null;
 		if (MetadataSubject.class.isAssignableFrom(this.getDomainClass())) {
 			metadata = ((MetadataSubject) resource).getMetadata();
 			((MetadataSubject) resource)
 					.setMetadata(new HashMap<String, Metadatum>());
 			LOGGER.info("noteMetadata, noted: " + metadata);
 		} else {
 			LOGGER.info("noteMetadata, not a metadata subject");
 		}
 		return metadata;
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public void addMetadatum(ID subjectId, Metadatum dto) {
 		LOGGER.info("addMetadatum subjectId: "+subjectId + ", metadatum: "+dto);
 		this.repository.addMetadatum(subjectId, dto.getPredicate(),
 				dto.getObject());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public void addMetadata(ID subjectId, Collection<Metadatum> dtos) {
 		if (!CollectionUtils.isEmpty(dtos)) {
 			for (Metadatum dto : dtos) {
 				this.addMetadatum(subjectId, dto);
 			}
 		}
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void removeMetadatum(ID subjectId, String predicate) {
 		LOGGER.info("removeMetadatum subjectId: " + subjectId + ", predicate: "
 				+ predicate);
 		this.repository.removeMetadatum(subjectId, predicate);
 	}
 
 
 }
