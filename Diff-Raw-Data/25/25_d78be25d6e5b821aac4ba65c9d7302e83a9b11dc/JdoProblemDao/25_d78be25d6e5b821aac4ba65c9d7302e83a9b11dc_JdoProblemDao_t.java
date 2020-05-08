 /**
  * Copyright 2011 Paweł Doleciński.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.tdm.server.datastore.dao;
 
 import java.util.Collection;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.tdm.common.dto.Problem;
 import com.tdm.server.datastore.entities.ProblemEntity;
 import com.tdm.server.datastore.exception.ConstraintsViolationException;
 import com.tdm.server.datastore.exception.ObjectNotFoundException;
 
 
 /**
  * Defualt JDO implementation of ProblemDao
  * 
  * @author Paweł Doleciński
  * 
  * @see ProblemDao
  */
 @Repository
 public class JdoProblemDao implements ProblemDao {
 
 	@Autowired(required = true)
 	private PersistenceManagerFactory persistenceManagerFactory;
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public Problem create(String username, String resourceName) {
 
 		ProblemEntity request = new ProblemEntity();
 		request.setAuthorUsername(username);
 		request.setResourceName(resourceName);
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			pm.makePersistent(request);
 		} finally {
 			pm.close();
 		}
 		return entityToDto(request);
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public Problem create(String userName, String resourceName,
 			String userMessage) {
 		ProblemEntity request = new ProblemEntity();
 		request.setAuthorUsername(userName);
 		request.setResourceName(resourceName);
 		request.setAuthorMessage(userMessage);
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			pm.makePersistent(request);
 		} finally {
 			pm.close();
 		}
 		return entityToDto(request);
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public Problem read(long id) throws ObjectNotFoundException {
 		ProblemEntity request = findRequest(id);
 		return entityToDto(request);
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void update(Problem request) throws ObjectNotFoundException,
 			ConstraintsViolationException {
 		ProblemEntity updatedRequest = findRequest(request.getId());
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void delete(long id) throws ObjectNotFoundException {
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			pm.deletePersistent(findRequest(id));
 		} finally {
 			pm.close();
 		}
 	}
 
 	/**
 	 * Finds request with given id or throws exception when not not found.
 	 */
 	private ProblemEntity findRequest(long id) throws ObjectNotFoundException {
 		PersistenceManager pm = getPersistenceManager();
 		ProblemEntity request = null;
 		try {
 			Query q = pm.newQuery(ProblemEntity.class, "id == i");
 			q.declareParameters("java.lang.Long i");
 			request = (ProblemEntity) q.execute(id);
 		} finally {
 			pm.close();
 		}
 
 		if (request == null) {
 			throw new ObjectNotFoundException(ProblemEntity.class, id);
 		}
 		return request;
 	}
 
 	/**
 	 * Simply creates {@code RequestDTO} based on JPA Request entity.
 	 */
 	private Problem entityToDto(ProblemEntity request) {
 		return null;
 		// return new ProblemDTO(request.getId(), request.getCreationDate(),
 		// request.getSentDate(), request.getStatus(),
 		// request.getResourceName(), request.getAuthorUsername(),
 		// request.getAuthorMessage());
 	}
 
 	private PersistenceManager getPersistenceManager() {
 		return persistenceManagerFactory.getPersistenceManager();
 	}
 
 	@Override
 	public Collection<Problem> findAllAssignedTo(long expertId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<Problem> findAllAssignedTo(long expertId,
 			String expertRole) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
