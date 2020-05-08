 /*  
  * Copyright 2008-2010 the original author or authors 
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
 package org.kaleidofoundry.core.store;
 
 import static org.kaleidofoundry.core.i18n.InternalBundleHelper.StoreMessageBundle;
 import static org.kaleidofoundry.core.persistence.UnmanagedEntityManagerFactory.KaleidoPersistentContextUnitName;
 import static org.kaleidofoundry.core.store.FileStoreConstants.ClobJpaStorePluginName;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Calendar;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.kaleidofoundry.core.context.RuntimeContext;
 import org.kaleidofoundry.core.io.FileHelper;
 import org.kaleidofoundry.core.lang.annotation.NotNull;
 import org.kaleidofoundry.core.lang.annotation.Task;
 import org.kaleidofoundry.core.plugin.Declare;
 import org.kaleidofoundry.core.store.entity.ResourceHandlerEntity;
 import org.kaleidofoundry.core.util.StringHelper;
 
 /**
  * JPA file store. Resource will be stored in clob or / blob database<br/>
  * 
  * @author Jerome RADUGET
  * @see FileStoreContextBuilder enum of context configuration properties available
  */
 @Declare(ClobJpaStorePluginName)
 @Task(comment = "Annotate it as @Stateless ejb to enable ejb exposition + injection - import right ejb 3.x library - problem : coupling it to ejb3")
 public class JpaFileStore extends AbstractFileStore implements FileStore {
 
    @PersistenceContext(unitName = KaleidoPersistentContextUnitName)
    private EntityManager em;
 
    /**
     * @return current entity manager (handle managed one or not)
     */
    @NotNull
    protected final EntityManager getEntityManager() {
 	/*
 	 * done via aop : PersistenceContextAspect
 	 * // unmanaged environment
 	 * if (em == null) {
 	 * return UnmanagedEntityManagerFactory.currentEntityManager(KaleidoPersistentContextUnitName);
 	 * }
 	 * // managed environment
 	 * else {
 	 * return em;
 	 * }
 	 */
 	return em;
    }
 
    /**
     * @param context
     */
    public JpaFileStore(@NotNull final RuntimeContext<FileStore> context) {
 	super(context);
    }
 
    /**
     * @param baseUri
     * @param context
     */
    public JpaFileStore(final String baseUri, final RuntimeContext<FileStore> context) {
 	super(baseUri, context);
    }
 
    /**
     * @see AbstractFileStore#AbstractFileStore()
     */
    JpaFileStore() {
 	super();
    }
 
    /*
     * (non-Javadoc)
     * @see org.kaleidofoundry.core.store.AbstractFileStore#getStoreType()
     */
    @Override
    public FileStoreType[] getStoreType() {
 	return new FileStoreType[] { FileStoreTypeEnum.jpa };
    }
 
    /*
     * (non-Javadoc)
     * @see org.kaleidofoundry.core.store.AbstractFileStore#doLoad(java.net.URI)
     */
    @Override
    protected ResourceHandler doGet(final URI resourceUri) throws ResourceNotFoundException, ResourceException {
 	final ResourceHandlerEntity entity = getEntityManager().find(ResourceHandlerEntity.class, resourceUri.toString());
 	if (entity == null) {
 	   throw new ResourceNotFoundException(resourceUri.toString());
 	} else {
 	   final ResourceHandler resourceHandler = new ResourceHandlerBean(resourceUri.toString(), new ByteArrayInputStream(entity.getContent()));
 	   return resourceHandler;
 	}
    }
 
    /*
     * (non-Javadoc)
     * @see org.kaleidofoundry.core.store.AbstractFileStore#doRemove(java.net.URI)
     */
    @Override
    protected void doRemove(final URI resourceUri) throws ResourceNotFoundException, ResourceException {
 	final ResourceHandlerEntity entity = getEntityManager().find(ResourceHandlerEntity.class, resourceUri.toString());
 	if (entity == null) {
 	   throw new ResourceNotFoundException(resourceUri.toString());
 	} else {
 	   getEntityManager().remove(entity);
 	}
    }
 
    /*
     * (non-Javadoc)
     * @see org.kaleidofoundry.core.store.AbstractFileStore#doStore(java.net.URI, org.kaleidofoundry.core.store.ResourceHandler)
     */
    @Override
    protected void doStore(final URI resourceUri, final ResourceHandler resource) throws ResourceException {
 
 	boolean isNew = true;
 	final String filename = resourceUri.getPath().substring(1);
 	ResourceHandlerEntity storeEntity;
 
 	storeEntity = getEntityManager().find(ResourceHandlerEntity.class, resourceUri.toString());
 
 	if (storeEntity == null) {
 	   storeEntity = newInstance();
 	   storeEntity.setUri(resourceUri.toString());
 	   storeEntity.setName(FileHelper.getFileName(filename));
 	   storeEntity.setPath(filename);
 	   storeEntity.setCreationDate(Calendar.getInstance().getTime());
 	   isNew = true;
 	} else {
 	   storeEntity.setUpdatedDate(Calendar.getInstance().getTime());
 	   isNew = false;
 	}
 
 	if (resource.getInputStream() != null) {
 
	   final int buffSize = !StringHelper.isEmpty(context.getString(FileStoreContextBuilder.BufferSize)) ? context
		   .getInteger(FileStoreContextBuilder.BufferSize) : 128;
	   final byte[] buffer = new byte[buffSize];
 	   final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 
 	   try {
		while (resource.getInputStream().read(buffer) >= 0) {
		   outputStream.write(buffer);
 		}
 		storeEntity.setContent(outputStream.toByteArray());
 
 		if (isNew) {
 		   getEntityManager().persist(storeEntity);
 		   getEntityManager().flush(); // to remove
 		} else {
 		   getEntityManager().merge(storeEntity);
 		   getEntityManager().flush(); // to remove
 		}
 
 	   } catch (final IOException ioe) {
 		throw new ResourceException(ioe, resourceUri.toString());
 	   }
 	}
    }
 
    /**
     * @return new {@link ResourceHandlerEntity} instance (default or custom) depending from the current context
     */
    private ResourceHandlerEntity newInstance() {
 
 	final String customResourceHandlerEntityClass = context.getString(FileStoreContextBuilder.CustomResourceHandlerEntity);
 
 	if (StringHelper.isEmpty(customResourceHandlerEntityClass)) {
 	   return new ResourceHandlerEntity();
 	} else {
 	   try {
 		final Class<?> customClass = Class.forName(customResourceHandlerEntityClass);
 		final Object instance = customClass.newInstance();
 
 		if (instance instanceof ResourceHandlerEntity) {
 		   return (ResourceHandlerEntity) instance;
 		} else {
 		   throw new IllegalStateException(StoreMessageBundle.getMessage("store.context.customentity.illegaltype", context.getName(),
 			   context.getProperty(FileStoreContextBuilder.CustomResourceHandlerEntity), FileStoreContextBuilder.CustomResourceHandlerEntity));
 		}
 
 	   } catch (final ClassNotFoundException cnfe) {
 		throw new IllegalStateException(StoreMessageBundle.getMessage("store.context.customentity.notfound", context.getName(),
 			context.getProperty(FileStoreContextBuilder.CustomResourceHandlerEntity), FileStoreContextBuilder.CustomResourceHandlerEntity));
 	   } catch (final IllegalAccessException iae) {
 		throw new IllegalStateException(StoreMessageBundle.getMessage("store.context.customentity.illegalconstructor", customResourceHandlerEntityClass,
 			iae.getMessage()));
 
 	   } catch (final InstantiationException ie) {
 		throw new IllegalStateException(StoreMessageBundle.getMessage("store.context.customentity.cantcreate", customResourceHandlerEntityClass,
 			ie.getMessage()));
 	   }
 	}
    }
 }
