 /**
  *        Licensed to the Apache Software Foundation (ASF) under one
  *        or more contributor license agreements.  See the NOTICE file
  *        distributed with this work for additional information
  *        regarding copyright ownership.  The ASF licenses this file
  *        to you under the Apache License, Version 2.0 (the
  *        "License"); you may not use this file except in compliance
  *        with the License.  You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *        Unless required by applicable law or agreed to in writing,
  *        software distributed under the License is distributed on an
  *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *        KIND, either express or implied.  See the License for the
  *        specific language governing permissions and limitations
  *        under the License.
  *
  */
/**
 * 
 */
 package com.intelligentsia.dowsers.entity.store;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.base.Preconditions;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.intelligentsia.dowsers.entity.reference.Reference;
 import com.intelligentsia.dowsers.entity.reference.References;
 
 /**
  * <code>CachedEntityStore</code> adding cache functionality.
  * 
  * @author <a href="mailto:jguibert@intelligents-ia.com">Jerome Guibert</a>
  * 
  */
 public class CachedEntityStore implements EntityStore {
 
 	/**
 	 * Delegate {@link EntityStore}.
 	 */
 	protected final EntityStore entityStore;
 
 	private final LoadingCache<KeyCache, Object> entities;
 
 	/**
 	 * Build a new instance of <code>CachedEntityStore</code> with default
 	 * cache:
 	 * <ul>
 	 * <li>Maximum size : 1000 elements</li>
 	 * <li>expire after access : 1 hour</li>
 	 * </ul>
 	 * 
 	 * @param entityStore
 	 * @throws NullPointerException
 	 */
 	public CachedEntityStore(final EntityStore entityStore) throws NullPointerException {
 		this(entityStore, CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(1, TimeUnit.HOURS));
 	}
 
 	/**
 	 * Build a new instance of <code>CachedEntityStore</code>.
 	 * 
 	 * @param entityStore
 	 * @param cacheBuilder
 	 */
 	public CachedEntityStore(final EntityStore entityStore, final CacheBuilder<Object, Object> cacheBuilder) {
 		super();
 		this.entityStore = Preconditions.checkNotNull(entityStore);
 		/**
 		 * Add specific cache loader.
 		 */
 		entities = cacheBuilder.build(new CacheLoader<KeyCache, Object>() {
 			@Override
 			public Object load(final KeyCache key) throws Exception {
 				return entityStore.find(key.expectedType, key.reference);
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T> T find(final Class<T> expectedType, final Reference reference) throws EntityNotFoundException, NullPointerException, IllegalArgumentException {
 		try {
 			return (T) entities.get(new KeyCache(expectedType, reference));
 		} catch (final ExecutionException e) {
 			throw new EntityNotFoundException(e);
 		}
 	}
 
 	@Override
 	public <T> void store(final T entity) throws NullPointerException, ConcurrencyException, IllegalArgumentException {
 		entities.invalidate(new KeyCache(null, References.identify(entity)));
 		entityStore.store(entity);
 	}
 
 	@Override
 	public <T> void remove(final T entity) throws NullPointerException, IllegalArgumentException {
 		entities.invalidate(new KeyCache(null, References.identify(entity)));
 		entityStore.remove(entity);
 	}
 
 	@Override
 	public void remove(final Reference reference) throws NullPointerException, IllegalArgumentException {
 		entities.invalidate(new KeyCache(null, reference));
 		entityStore.remove(reference);
 	}
 
 	/**
 	 * <code>KeyCache</code>.
 	 * 
 	 * @author <a href="mailto:jguibert@intelligents-ia.com">Jerome Guibert</a>
 	 * 
 	 */
 	private static class KeyCache {
 		private final Class<?> expectedType;
 		private final Reference reference;
 
 		public KeyCache(final Class<?> expectedType, final Reference reference) {
 			super();
 			this.expectedType = expectedType;
 			this.reference = reference;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = (prime * result) + ((reference == null) ? 0 : reference.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (getClass() != obj.getClass()) {
 				return false;
 			}
 			final KeyCache other = (KeyCache) obj;
 			if (reference == null) {
 				if (other.reference != null) {
 					return false;
 				}
 			} else if (!reference.equals(other.reference)) {
 				return false;
 			}
 			return true;
 		}
 	}
 
 }
