 package de.zib.gndms.GORFX.offer.service;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
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
 
 
 
 import de.zib.gndms.GORFX.context.service.globus.resource.ExtTaskResourceHome;
 import de.zib.gndms.GORFX.context.service.globus.resource.TaskResource;
 import de.zib.gndms.GORFX.offer.service.globus.resource.ExtOfferResourceHome;
 import de.zib.gndms.GORFX.offer.service.globus.resource.OfferResource;
 import de.zib.gndms.kit.util.WidAux;
 import de.zib.gndms.model.gorfx.OfferType;
 import de.zib.gndms.model.gorfx.Task;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.globus.wsrf.ResourceKey;
 import org.jetbrains.annotations.NotNull;
 import org.apache.log4j.Logger;
 
 import javax.persistence.EntityManager;
 import java.rmi.RemoteException;
 
 /** 
  * TODO:I am the service side implementation class.  IMPLEMENT AND DOCUMENT ME
  * 
  * @created by Introduce Toolkit version 1.2
  * 
  */
 public class OfferImpl extends OfferImplBase {
 
     private static final Logger logger = Logger.getLogger(OfferImpl.class);
 
     public OfferImpl() throws RemoteException {
         super();
     }
 
     public org.apache.axis.message.addressing.EndpointReferenceType accept() throws RemoteException, de.zib.gndms.GORFX.ORQ.stubs.types.PermissionDenied {
 
         try {
            // Create task object
             @NotNull final ExtOfferResourceHome home = (ExtOfferResourceHome) getResourceHome( );
             @NotNull final OfferResource ores = home.getAddressedResource();
             WidAux.initWid(ores.getCachedWid());
             WidAux.initGORFXid( ores.getOrqCalc().getORQArguments().getActId() );
             @NotNull final Task task = ores.accept( );
            @NotNull final ExtTaskResourceHome thome = ( ExtTaskResourceHome) getTaskResourceHome();
            @NotNull final EntityManager em = thome.getEntityManagerFactory().createEntityManager();
             logger.debug( "accepted" );
 
 
             // Persist task object
             persistTask(task, em);
 
             // Create resource and build epr
             EndpointReferenceType epr;
             try {
                 final ResourceKey key = thome.getKeyForId(task.getId());
                TaskResource tres = (TaskResource) thome.find(key);
                 epr = thome.getResourceReference(key).getEndpointReference();
                 return epr;
             }
             catch (RuntimeException e) {
                 try {
                     logger.error( "Runtime Exception, removing task", e);
                     removeTask(task, em);
                 }
                 finally {
                     // but keep causing exception if even that fails
                     throw e;
                 }
             }
         } catch ( Exception e ) {
            logger.error( "Exception occured", e);
             throw new RemoteException(e.getMessage(), e);
         }
         finally {
            WidAux.removeGORFXid(); 
             WidAux.removeWid();
         }
     }
 
 
     private void removeTask(final Task taskParam, final EntityManager emParam) {// on failure, try to del the task from the db
         emParam.getTransaction().begin();
         try {
             if (emParam.contains(taskParam))
                 emParam.remove(taskParam);
             emParam.getTransaction().commit();
         }
         finally {
             if (emParam.getTransaction().isActive())
                 emParam.getTransaction().rollback();
         }
     }
 
 
     private void persistTask(final Task taskParam, final EntityManager emParam) {
         emParam.getTransaction().begin();
         try {
             final @NotNull OfferType newType = emParam.find(OfferType.class, taskParam.getOfferType().getOfferTypeKey());
             newType.getTasks().add(taskParam);
             taskParam.setOfferType(newType);
             emParam.persist(taskParam);
             emParam.getTransaction().commit();
         } catch ( RuntimeException e ) { // for debug purpose
             throw e;
         }
         finally {
             if (emParam.getTransaction().isActive())
                 emParam.getTransaction().rollback();
         }
     }
 
 
     @Override
     public ExtOfferResourceHome getResourceHome() throws Exception {
         return (ExtOfferResourceHome) super.getResourceHome();    // Overridden method
     }
 }
 
