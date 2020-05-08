 /**
  * Copyright 2013 Maxime Bossard
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
 
 package fr.mby.utils.common.jpa;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceException;
 
 /**
  * This callback allow execution of code on an EntityManager. <br/>
  * Properly open and close resources.
  * 
  * @param <T>
  *            Type of Object return by this JpaTransactionExecutor.
  * 
  * @author Maxime Bossard - 2013
  * 
  */
 public abstract class EmCallback<T> {
 
 	private T result;
 
 	/**
 	 * Execute some code on an EntityManager. <br/>
 	 * Create and close properly the EntityManager.
 	 * 
 	 * @param em
 	 *            the EntityManager to use.
 	 * @throws PersistenceException
 	 *             which may occured during transaction processing.
 	 */
 	public EmCallback(final EntityManagerFactory emf) throws PersistenceException {
 		super();
 
 		final EntityManager em = emf.createEntityManager();
 
 		try {
			this.executeWithEntityManager(em);
 		} finally {
 			if (em != null && em.isOpen()) {
 				em.close();
 			}
 		}
 	}
 
 	/**
 	 * This method is executed when the Object is construct.
 	 * 
 	 * @param em
 	 *            the EntityManager to use
 	 * @return the object returned can be retrieved with getReturnedValue().
 	 * @Throws PersistenceException
 	 */
 	protected abstract T executeWithEntityManager(final EntityManager em) throws PersistenceException;
 
 	/**
 	 * Return the value which was returned by this callback.
 	 * 
 	 * @return the returned value.
 	 */
 	public T getReturnedValue() {
 		return this.result;
 	}
 
 }
