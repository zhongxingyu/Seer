 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.om.business.fedora.contentRelation;
 
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 
 import org.escidoc.core.services.fedora.ChecksumType;
 import org.escidoc.core.services.fedora.DatastreamState;
 import org.escidoc.core.services.fedora.FedoraServiceClient;
 import org.escidoc.core.services.fedora.management.DatastreamProfileTO;
 import org.escidoc.core.services.fedora.management.DatastreamProfilesTO;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.LockHandler;
 import de.escidoc.core.common.business.fedora.HandlerBase;
 import de.escidoc.core.common.business.fedora.Triple;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.LockStatus;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.business.fedora.resources.StatusType;
 import de.escidoc.core.common.business.fedora.resources.create.ContentRelationCreate;
 import de.escidoc.core.common.business.fedora.resources.create.MdRecordCreate;
 import de.escidoc.core.common.business.fedora.resources.listener.ResourceListener;
 import de.escidoc.core.common.business.filter.SRURequest;
 import de.escidoc.core.common.business.filter.SRURequestParameters;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.PidSystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.persistence.PIDSystem;
 import de.escidoc.core.common.persistence.PIDSystemFactory;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.RelsExtReadHandler;
 import de.escidoc.core.common.util.stax.handler.TaskParamHandler;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ContentRelationXmlProvider;
 import de.escidoc.core.om.business.fedora.ContentRelationsUtility;
 import de.escidoc.core.om.business.interfaces.ContentRelationHandlerInterface;
 import de.escidoc.core.om.business.stax.handler.item.ContentRelationHandler;
 
 /**
  * ContentRelation handler.
  * 
  * @author Steffen Wagner
  */
 @Service("business.FedoraContentRelationHandler")
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class FedoraContentRelationHandler extends HandlerBase implements ContentRelationHandlerInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(FedoraContentRelationHandler.class);
 
     private final Collection<ResourceListener> contentRelationListeners = new ArrayList<ResourceListener>();
 
     @Autowired
     private ContentRelationXmlProvider contentRelationXmlProvider;
 
     @Autowired
     @Qualifier("de.escidoc.core.common.business.filter.SRURequest")
     private SRURequest sruRequest;
 
     @Autowired
     @Qualifier("common.business.indexing.IndexingHandler")
     private ResourceListener indexingHandler;
 
     @Autowired
     @Qualifier("business.LockHandler")
     private LockHandler lockHandler;
 
     @Autowired
     private FedoraServiceClient fedoraServiceClient;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     private PIDSystemFactory pidGenFactory;
 
     private PIDSystem pidGen;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected FedoraContentRelationHandler() {
     }
 
     @PostConstruct
     public void init() {
         addContentRelationListener(indexingHandler);
     }
 
     /**
      * Create Content Relation.
      * 
      * @param xmlData
      *            XML representation of Content Relation
      * @return XML representation of created Content Relation
      * @throws MissingMethodParameterException
      *             Thrown if method parameter is missing
      * @throws ReferencedResourceNotFoundException
      *             Thrown if referenced resource does not exist.
      * @throws RelationPredicateNotFoundException
      *             Thrown if the predicate is not registered.
      * @throws InvalidContentException
      *             Thrown if content is invalid
      * @throws MissingAttributeValueException
      *             Thrown if attribute value is missing
      * @throws SystemException
      *             Thrown if internal error occur
      */
     @Override
     public String create(final String xmlData) throws MissingAttributeValueException, MissingMethodParameterException,
         InvalidContentException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException,
         SystemException, XmlCorruptedException {
 
         final ContentRelationCreate cr = parseContentRelation(xmlData);
 
         // make sure that some values are fixed/ignored
         cr.setObjid(null);
         cr.getProperties().setStatus(StatusType.PENDING);
         cr.getProperties().setPid(null);
 
         validate(cr);
 
         cr.setIdProvider(getIdProvider());
         cr.persist(true);
 
         final String resultCR = this.contentRelationXmlProvider.getContentRelationXml(cr);
 
         fireContentRelationCreated(cr, resultCR);
         return resultCR;
     }
 
     /**
      * Get escidoc XML representation of ContentRelation.
      * 
      * @param id
      *            objid of ContentRelation resource
      * @return escidoc XML representation of ContentRelation
      * @throws ContentRelationNotFoundException
      *             Thrown if under provided id no ContentRelation could be found
      * @throws SystemException
      *             Thrown if internal error occurs.
      */
     @Override
     public String retrieve(final String id) throws ContentRelationNotFoundException, SystemException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         enrichWithMetadataContent(cr);
         return this.contentRelationXmlProvider.getContentRelationXml(cr);
     }
 
     /**
      * Retrieves a filtered list of content relations.
      * 
      * @param parameters
      *            parameters from the SRU request
      * @return Returns XML representation of the list of content relation objects.
      */
     @Override
     public String retrieveContentRelations(final SRURequestParameters parameters) throws WebserverSystemException {
         final StringWriter result = new StringWriter();
 
         if (parameters.isExplain()) {
             sruRequest.explain(result, ResourceType.CONTENT_RELATION);
         }
         else {
             sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.CONTENT_RELATION }, parameters);
         }
         return result.toString();
     }
 
     /**
      * Get escidoc XML representation of ContentRelations properties.
      * 
      * @param id
      *            objid of ContentRelation resource
      * @return escidoc XML representation of ContentRelation
      * @throws ContentRelationNotFoundException
      *             Thrown if under provided id no ContentRelation could be found
      * @throws SystemException
      *             Thrown if internal error occurs.
      */
     @Override
     public String retrieveProperties(final String id) throws ContentRelationNotFoundException, SystemException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         return this.contentRelationXmlProvider.getContentRelationPropertiesXml(cr);
     }
 
     /**
      * Get escidoc XML representation of ContentRelations md-records.
      * 
      * @param id
      *            objid of ContentRelation resource
      * @return escidoc XML representation of ContentRelation
      * @throws ContentRelationNotFoundException
      *             Thrown if under provided id no ContentRelation could be found
      * @throws SystemException
      *             Thrown if internal error occurs.
      */
     @Override
     public String retrieveMdRecords(final String id) throws ContentRelationNotFoundException, SystemException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         enrichWithMetadataContent(cr);
         return this.contentRelationXmlProvider.getContentRelationMdRecords(cr);
     }
 
     /**
      * Get escidoc XML representation of ContentRelations md-records.
      * 
      * @param id
      *            objid of ContentRelation resource
      * @param name
      *            name of a md-record
      * @return escidoc XML representation of ContentRelation
      * @throws ContentRelationNotFoundException
      *             Thrown if under provided id no ContentRelation could be found
      * @throws MdRecordNotFoundException
      *             e
      * @throws SystemException
      *             Thrown if internal error occurs.
      */
     @Override
     public String retrieveMdRecord(final String id, final String name) throws ContentRelationNotFoundException,
         MdRecordNotFoundException, SystemException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         enrichWithMetadataContent(cr);
         final List<MdRecordCreate> mdRecords = cr.getMetadataRecords();
         if (mdRecords != null) {
             for (final MdRecordCreate mr : mdRecords) {
                 if (mr.getName().equals(name)) {
                     return this.contentRelationXmlProvider.getContentRelationMdRecord(cr, mr);
                 }
             }
         }
         throw new MdRecordNotFoundException("The md-record with name=" + name + " does not exist.");
     }
 
     /**
      * Update Content Relation.
      * 
      * @param id
      *            objid of Content Relation
      * @param xmlData
      *            XML representation of Content Relation
      * @return XML representation of updated Content Relation
      * @throws ContentRelationNotFoundException
      *             Thrown if no Content Relation could be found under provided objid
      * @throws OptimisticLockingException
      *             Thrown if resource is updated in the meantime and last modification date differs
      * @throws InvalidStatusException
      *             Thrown if resource has invalid status to update
      * @throws MissingAttributeValueException
      *             Thrown if attribute value is missing
      * @throws LockingException
      *             Thrown if resource is locked through other user
      * @throws MissingMethodParameterException
      *             Thrown if method parameter is missing
      * @throws ReferencedResourceNotFoundException
      *             Thrown if referenced resource does not exist.
      * @throws RelationPredicateNotFoundException
      *             Thrown if the predicate is not registered.
      * @throws InvalidContentException
      *             Thrown if content is invalid
      * @throws SystemException
      *             Thrown if internal error occur
      */
     @Override
     public String update(final String id, final String xmlData) throws ContentRelationNotFoundException,
         OptimisticLockingException, InvalidContentException, InvalidStatusException, LockingException,
         MissingAttributeValueException, MissingMethodParameterException, SystemException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException, XmlCorruptedException {
 
         // instance of stored Content Relation
         final ContentRelationCreate cr = setContentRelation(id);
         // instance of the update representation
         final ContentRelationCreate updatedCR = parseContentRelation(xmlData);
 
         Utility.checkOptimisticLockingCriteria(cr.getProperties().getLastModificationDate(), updatedCR
             .getLastModificationDate(), "Content Relation " + cr.getObjid());
 
         enrichWithMetadataContent(cr);
 
         checkLocked(cr);
         checkReleased(cr);
 
         validate(updatedCR);
 
         // now compare this.contentRelation with updatedCR and transfer data
         boolean resourceChanged = false;
         if (cr.merge(updatedCR) > 0) {
             cr.persist();
             resourceChanged = true;
         }
         final String result = this.contentRelationXmlProvider.getContentRelationXml(cr);
 
         if (resourceChanged) {
             fireContentRelationModified(cr, result);
         }
 
         return result;
     }
 
     /**
      * Delete Content Relation.
      * <p/>
      * Even if Content Relations have life cycle with release etc. it should be possible to remove. This rule is
      * different to the release status of Item and Container where release means that the resource will be public
      * available anytime and could only be withdrawn. It's assumed that the anytime feature isn't required for Content
      * Relations because they not contain primary data.
      * 
      * @param id
      *            The objid of the Content Relation
      * @throws ContentRelationNotFoundException
      *             Thrown if a content relation with the provided id cannot be found.
      * @throws LockingException
      *             Thrown if Content Relation is locked by other user
      * @throws SystemException
      *             Thrown if internal error occurs.
      */
     @Override
     public void delete(final String id) throws ContentRelationNotFoundException, SystemException, LockingException {
         final ContentRelationCreate cr = setContentRelation(id);
         checkLocked(cr);
         this.fedoraServiceClient.deleteObject(cr.getObjid());
         this.fedoraServiceClient.sync();
         try {
             this.tripleStoreUtility.reinitialize();
         }
         catch (final TripleStoreSystemException e) {
             throw new FedoraSystemException("Error on reinitializing triple store.", e);
         }
         fireContentRelationDeleted(cr);
     }
 
     /**
      * @param xmlData
      *            content relation as XML
      * @return ingested content relation as XML
      */
     @Override
     public String ingest(final String xmlData) throws WebserverSystemException {
 
         throw new WebserverSystemException("Missing implementation");
     }
 
     /**
      * Submit the Content Relation.
      * 
      * @param id
      *            objid of ContentRelation
      * @param param
      *            Task parameter
      * @return XML result
      * @throws ContentRelationNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws InvalidContentException
      *             e
      */
     @Override
     public String submit(final String id, final String param) throws ContentRelationNotFoundException,
         LockingException, InvalidStatusException, MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidContentException, XmlCorruptedException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         checkLocked(cr);
 
         validateToSubmitStatus(cr);
 
         // check optimistic locking criteria
         Utility.checkOptimisticLockingCriteria(cr.getProperties().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Content relation " + id);
 
         cr.getProperties().setStatus(StatusType.SUBMITTED);
         // set status comment
         if (taskParameter.getComment() != null) {
             cr.getProperties().setStatusComment(taskParameter.getComment());
         }
         else {
             cr.getProperties().setStatusComment("Status changed to 'submitted'");
         }
 
         cr.persistProperties(true);
 
         // load metadata content to resource
         enrichWithMetadataContent(cr);
         fireContentRelationModified(cr, this.contentRelationXmlProvider.getContentRelationXml(cr));
 
         return Utility.prepareReturnXml(cr.getProperties().getLastModificationDate(), null);
     }
 
     /**
      * Register an content relation listener.
      * 
      * @param listener
      *            listener which will be added to the list
      */
     public void addContentRelationListener(final ResourceListener listener) {
         contentRelationListeners.add(listener);
     }
 
     /**
      * @param id
      *            objid of ContentRelation
      * @param param
      *            Task parameter
      * @return XML result
      * @throws ContentRelationNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws InvalidContentException
      *             e
      */
     @Override
     public String release(final String id, final String param) throws ContentRelationNotFoundException,
         LockingException, InvalidStatusException, MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidContentException, XmlCorruptedException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         checkLocked(cr);
         checkReleased(cr);
         if (cr.getProperties().getStatus() != StatusType.SUBMITTED) {
             throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_SUBMITTED
                 + "' and can not be " + Constants.STATUS_RELEASED + '.');
         }
         Utility.checkOptimisticLockingCriteria(cr.getProperties().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Content relation " + id);
 
         cr.getProperties().setStatus(StatusType.RELEASED);
 
         // set status comment
         if (taskParameter.getComment() != null) {
             cr.getProperties().setStatusComment(taskParameter.getComment());
         }
         else {
             cr.getProperties().setStatusComment("Status changed to 'released'");
         }
 
         cr.persistProperties(true);
 
         // load metadata content to resource
         enrichWithMetadataContent(cr);
         fireContentRelationModified(cr, this.contentRelationXmlProvider.getContentRelationXml(cr));
 
         return Utility.prepareReturnXml(cr.getProperties().getLastModificationDate(), null);
     }
 
     /**
      * Revise the Content Relation.
      * 
      * @param id
      *            Objid of Content Relation
      * @param taskParam
      *            TaskParameter
      * @return XML result
      * @throws ContentRelationNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws XmlCorruptedException
      *             e
      * @throws InvalidContentException
      *             e
      */
     @Override
     public String revise(final String id, final String taskParam) throws ContentRelationNotFoundException,
         LockingException, InvalidStatusException, MissingMethodParameterException, SystemException,
         OptimisticLockingException, XmlCorruptedException, InvalidContentException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(taskParam);
         checkLocked(cr);
         if (cr.getProperties().getStatus() != StatusType.SUBMITTED) {
             throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_SUBMITTED
                 + "' and can not be revised.");
         }
         Utility.checkOptimisticLockingCriteria(cr.getProperties().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Content relation " + id);
 
         cr.getProperties().setStatus(StatusType.INREVISION);
         // set status comment
         if (taskParameter.getComment() != null) {
             cr.getProperties().setStatusComment(taskParameter.getComment());
         }
         else {
             cr.getProperties().setStatusComment("Status changed to 'in-revision'");
         }
 
         cr.persistProperties(true);
 
         // load metadata content to resource
         enrichWithMetadataContent(cr);
         fireContentRelationModified(cr, this.contentRelationXmlProvider.getContentRelationXml(cr));
 
         return Utility.prepareReturnXml(cr.getProperties().getLastModificationDate(), null);
     }
 
     /**
      * Lock a Content Relation for other user access.
      * 
      * @param id
      *            Objid of Content Relation
      * @param param
      *            XML TaskParam
      * @return Result XML data structure
      * @throws ContentRelationNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws InvalidStatusException
      *             e
      */
     @Override
     public String lock(final String id, final String param) throws ContentRelationNotFoundException, LockingException,
         InvalidContentException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         InvalidStatusException, XmlCorruptedException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         checkReleased(cr);
         checkLocked(cr);
 
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         Utility.checkOptimisticLockingCriteria(cr.getProperties().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Content relation " + id);
         if (!cr.getProperties().isLocked()) {
             lockHandler.lock(cr.getObjid(), Utility.getCurrentUser());
 
             cr.getProperties().setLockStatus(LockStatus.LOCKED);
             cr.getProperties().setLockOwnerId(getUtility().getCurrentUserId());
             cr.getProperties().setLockOwnerName(getUtility().getCurrentUserRealName());
             cr.getProperties().setLockDate(cr.getProperties().getLastModificationDate());
         }
 
         fireContentRelationModified(cr, this.contentRelationXmlProvider.getContentRelationXml(cr));
 
         // timestamp
         return Utility.prepareReturnXml(cr.getProperties().getLastModificationDate(), null);
     }
 
     /**
      * Unlock a Content Relation for other user access.
      * 
      * @param id
      *            Objid of Content Relation
      * @param param
      *            XML TaskParam
      * @return Result XML data structure
      * @throws ContentRelationNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws InvalidStatusException
      *             Thrown if resource is not locked.
      */
     @Override
     public String unlock(final String id, final String param) throws ContentRelationNotFoundException,
         LockingException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         InvalidContentException, InvalidStatusException, XmlCorruptedException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         Utility.checkOptimisticLockingCriteria(cr.getProperties().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Content relation " + id);
         if (cr.getProperties().isLocked()) {
             lockHandler.unlock(cr.getObjid());
 
             cr.getProperties().setLockStatus(LockStatus.UNLOCKED);
             cr.getProperties().setLockOwnerId(null);
             cr.getProperties().setLockOwnerId(null);
             cr.getProperties().setLockDate((DateTime) null);
         }
 
         fireContentRelationModified(cr, this.contentRelationXmlProvider.getContentRelationXml(cr));
 
         return Utility.prepareReturnXml(cr.getProperties().getLastModificationDate(), null);
     }
 
     /**
      * Assign persistent identifier to a Content-relation object.
      * 
      * @param id
      *            The Id of the Content-relation witch is to assign with an ObjectPid.
      * @param taskParam
      *            XML snippet with parameter for the persistent identifier system.
      * @return The assigned persistent identifier for the Content-relation.
      * @throws ContentRelationNotFoundException
      *             Thrown if the object with id is does not exist or is no Item.
      * @throws LockingException
      *             Thrown if the Item is locked
      * @throws MissingMethodParameterException
      *             Thrown if a parameter is missing within {@code taskParam}.
      * @throws OptimisticLockingException
      *             Thrown if Item was altered in the mean time.
      * @throws PidAlreadyAssignedException
      *             Thrown if a Content-relation is already assigned a PID.
      * @throws SystemException
      *             Thrown in case of internal error.
      */
     @Override
     public String assignObjectPid(final String id, final String taskParam) throws ContentRelationNotFoundException,
         LockingException, MissingMethodParameterException, OptimisticLockingException, SystemException,
         PidAlreadyAssignedException, XmlCorruptedException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         if (cr.getProperties().getPid() != null) {
             throw new PidAlreadyAssignedException("A content relation with id " + id + " is already assigned a PID");
         }
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(taskParam);
         checkLocked(cr);
         Utility.checkOptimisticLockingCriteria(cr.getProperties().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Content-relation " + cr.getObjid());
 
         String pid = taskParameter.getPid();
         if (pid == null) {
             // get PID from external PID System
             pid = getPid(id, taskParam);
         }
         cr.getProperties().setPid(pid);
         cr.persist();
         return prepareResponse(cr, pid);
     }
 
     /**
      * Retrieves a list of registered predicates which can be used to create content relations.
      * 
      * @return String containing a list with registered predicates.
      * @throws InvalidContentException
      *             Thrown if a xml file with an ontology has invalid content
      * @throws InvalidXmlException
      *             Thrown if a xml file with an ontology is invalid rdf/xml
      * @throws SystemException
      *             e
      */
     @Override
     public String retrieveRegisteredPredicates() throws InvalidContentException, InvalidXmlException, SystemException {

         final List<String> predicates = ContentRelationsUtility.getPredicates();
         final Iterator<String> it = predicates.iterator();

         final StringBuilder sb = new StringBuilder();
        sb.append(Constants.XML_HEADER);
        sb.append("<registeredPredicates:predicates");
        sb.append(" xmlns:registeredPredicates=\"http://www.escidoc.org/schemas/predicate-list/0.1\" >\n");
         while (it.hasNext()) {
            sb.append("<registeredPredicates:predicate>");
             sb.append(it.next());
            sb.append("</registeredPredicates:predicate>\n");
         }
        sb.append("</registeredPredicates:predicates>\n");
         return sb.toString();
     }
 
     /**
      * Get Persistent Identifier from configured PID (Manager) service.
      * 
      * @param id
      *            Item ID
      * @param param
      *            XML snippet with PID Manager parameter.
      * @return Persistent Identifier
      * @throws PidSystemException
      *             Thrown if the communication with PID (Management) System fails.
      * @throws MissingMethodParameterException
      *             Thrown if necessary parameters are not part of the param XML structure.
      * @throws WebserverSystemException
      *             Thrown by assignPid().
      */
     public String getPid(final String id, final String param) throws PidSystemException,
         MissingMethodParameterException, WebserverSystemException {
 
         if (this.pidGenFactory == null) {
             this.pidGenFactory = PIDSystemFactory.getInstance();
         }
         if (this.pidGen == null) {
             this.pidGen = pidGenFactory.getPIDGenerator();
         }
 
         return pidGen.assignPID(id, param);
     }
 
     /**
      * Retrieve virtual resources.
      * 
      * @param id
      *            objid of Content Relation
      * @return XML representation of resources
      */
     @Override
     public String retrieveResources(final String id) throws ContentRelationNotFoundException, SystemException {
 
         final ContentRelationCreate cr = setContentRelation(id);
         return this.contentRelationXmlProvider.getContentRelationResourcesXml(cr);
     }
 
     /**
      * Bounds a Content Relation object to this handler.
      * 
      * @param id
      *            The ID of the Content Relation which should be bound to this Handler.
      * @return value object of Content Relation with provided objid
      * @throws ContentRelationNotFoundException
      *             If there is no item with {@code id} in the repository.
      * @throws SystemException
      *             Thrown in case of an internal system error.
      */
     protected ContentRelationCreate setContentRelation(final String id) throws ContentRelationNotFoundException,
         SystemException {
 
         final ContentRelationCreate cr = new ContentRelationCreate();
         cr.setObjid(id);
 
         cr.getProperties().setCreationDate(getCreationDate(cr.getObjid()));
 
         // parse RELS-EXT and instantiate ContentRelationCreate
         setRelsExtValues(cr);
         setMetadata(cr);
 
         // set further values (obtained from other sources)
         if (lockHandler.isLocked(id)) {
             cr.getProperties().setLockStatus(LockStatus.LOCKED);
             cr.getProperties().setLockDate(lockHandler.getLockDate(id));
             cr.getProperties().setLockOwnerId(lockHandler.getLockOwner(id));
             cr.getProperties().setLockOwnerName(lockHandler.getLockOwnerTitle(id));
         }
 
         // be aware some of them are triple store requests, which are expensive
         cr.getProperties().setLastModificationDate(this.fedoraServiceClient.getObjectProfile(id).getObjLastModDate());
 
         return cr;
     }
 
     /**
      * Check if the Content Relation is locked.
      * 
      * @param cr
      *            Content Relation
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @throws LockingException
      *             If the Content is locked and the current user is not the one who locked it.
      */
 
     private void checkLocked(final ContentRelationCreate cr) throws LockingException, WebserverSystemException {
 
         if (cr.getProperties().isLocked()
             && !cr.getProperties().getLockOwnerId().equals(getUtility().getCurrentUserId())) {
             throw new LockingException("Content Relation + " + cr.getObjid() + " is locked by "
                 + cr.getProperties().getLockOwnerId() + " (" + cr.getProperties().getLockOwnerName() + ") .");
         }
     }
 
     /**
      * Check release status of object.
      * 
      * @param cr
      *            ContentRelation
      * @throws InvalidStatusException
      *             Thrown if object is not in status released.
      */
     private static void checkReleased(final ContentRelationCreate cr) throws InvalidStatusException {
 
         final StatusType status = cr.getProperties().getStatus();
         if (status == StatusType.RELEASED) {
             throw new InvalidStatusException("The object is in state '" + Constants.STATUS_RELEASED
                 + "' and can not be" + " changed.");
         }
     }
 
     /**
      * Retrieve the properties of the last version (RELS-EXT) and inject values into ContentRelationCreate object.
      * 
      * @param cr
      *            ContentRelation object
      * @throws SystemException
      *             Thrown in case of internal failure.
      * @throws ContentRelationNotFoundException
      *             Thrown if resource with provided id could not be found in Fedora repository.
      */
     private static void setRelsExtValues(final ContentRelationCreate cr) throws SystemException,
         ContentRelationNotFoundException {
 
         // retrieve resource with id from Fedora
         final Datastream relsExt;
         try {
             relsExt = new Datastream(Datastream.RELS_EXT_DATASTREAM, cr.getObjid(), null);
         }
         catch (final StreamNotFoundException e) {
             throw new ContentRelationNotFoundException("Content Relation with id '" + cr.getObjid()
                 + "' could not be found.", e);
         }
 
         final StaxParser sp = new StaxParser();
 
         final RelsExtReadHandler eve = new RelsExtReadHandler(sp);
         eve.cleanIdentifier(true);
         sp.addHandler(eve);
         try {
             sp.parse(relsExt.getStream());
         }
         catch (final Exception e) {
             throw new WebserverSystemException(e);
         }
 
         final List<Triple> triples = eve.getElementValues().getTriples();
 
         // write triple values into ContentRelation object
 
         for (final Triple triple : triples) {
             if (triple.getPredicate().equals(TripleStoreUtility.PROP_FRAMEWORK_BUILD)) {
                 cr.setBuildNumber(triple.getObject());
             }
             // creator --------------
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_CREATED_BY_ID)) {
                 cr.getProperties().setCreatedById(triple.getObject());
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_CREATED_BY_TITLE)) {
                 cr.getProperties().setCreatedByName(triple.getObject());
             }
             // modifier --------------
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_MODIFIED_BY_ID)) {
                 cr.getProperties().setModifiedById(triple.getObject());
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_MODIFIED_BY_TITLE)) {
                 cr.getProperties().setModifiedByName(triple.getObject());
             }
             // public-status --------------
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_PUBLIC_STATUS)) {
 
                 final StatusType st;
                 try {
                     st = StatusType.getStatusType(triple.getObject());
                 }
                 catch (final InvalidStatusException e) {
                     // shouldn't happen
                     throw new SystemException(e);
                 }
                 cr.getProperties().setStatus(st);
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_PUBLIC_STATUS_COMMENT)) {
                 cr.getProperties().setStatusComment(triple.getObject());
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_OBJECT_TYPE)) {
                 // this is not the ContentRelation type, this is the type of
                 // resource
                 if (!(Constants.CONTENT_RELATION2_OBJECT_TYPE.equals(triple.getObject()) || (Constants.RDF_NAMESPACE_URI + "Statement")
                     .equals(triple.getObject()))) {
                     throw new WebserverSystemException("Resource is not from type ContentRelation.");
                 }
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_CONTENT_RELATION_SUBJECT)) {
                 cr.setSubject(triple.getObject());
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_CONTENT_RELATION_OBJECT)) {
                 cr.setObject(triple.getObject());
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_CONTENT_RELATION_DESCRIPTION)) {
                 cr.getProperties().setDescription(triple.getObject());
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_CONTENT_RELATION_TYPE)) {
                 try {
                     cr.setType(new URI(triple.getObject()));
                 }
                 catch (final URISyntaxException e) {
                     // shouldn't happen
                     throw new SystemException("Stored value for URI in invalid.", e);
                 }
             }
             else if (triple.getPredicate().equals(TripleStoreUtility.PROP_CONTENT_RELATION_OBJECT_VERSION)) {
                 cr.setObjectVersion(triple.getObject());
             }
             else {
                 // add values for mapping
                 LOGGER.warn("Predicate not mapped " + triple.getPredicate() + " = " + triple.getObject());
             }
         }
     }
 
     /**
      * Get creation date of Content Relation.
      * 
      * @param objid
      *            objid of Content Relation
      * @return creation date
      * @throws ContentRelationNotFoundException
      *             Thrown if creation date could not be found (indicates, that resource not exists)
      * @throws WebserverSystemException
      *             Thrown if internal failure occurs.
      * @throws TripleStoreSystemException
      *             Thrown if triple store request failed.
      */
     private DateTime getCreationDate(final String objid) throws ContentRelationNotFoundException,
         TripleStoreSystemException {
 
         final String date;
         try {
             date = this.tripleStoreUtility.getCreationDate(objid);
         }
         catch (final TripleStoreSystemException e) {
 
             if (e.getMessage().contains("Creation date not found")) {
                 throw new ContentRelationNotFoundException("Content Relation with objid '" + objid
                     + "' does not exist.", e);
             }
             else {
                 throw e;
             }
         }
         return new DateTime(date, DateTimeZone.UTC);
     }
 
     /**
      * @param cr
      *            ContentRelation
      * @throws FedoraSystemException
      *             Thrown if access to Fedora failed.
      * @throws IntegritySystemException
      *             Thrown if data integrity is violated.
      */
     private void setMetadata(final ContentRelationCreate cr) throws IntegritySystemException {
 
         final DatastreamProfilesTO dsProfiles = getFedoraServiceClient().getDatastreamProfiles(cr.getObjid(), null);
 
         for (final DatastreamProfileTO datastreamProfileTO : dsProfiles.getDatastreamProfile()) {
             if (datastreamProfileTO.getDsAltID().contains(Datastream.METADATA_ALTERNATE_ID)
                 && !DatastreamState.D.value().equals(datastreamProfileTO.getDsState())) {
                 final MdRecordCreate mdRecord = new MdRecordCreate();
 
                 try {
                     mdRecord.setName(datastreamProfileTO.getDsID());
                     cr.addMdRecord(mdRecord);
                 }
                 catch (final InvalidContentException e) {
                     throw new IntegritySystemException(e);
                 }
 
                 mdRecord.setLabel(datastreamProfileTO.getDsLabel());
                 mdRecord.setChecksum(datastreamProfileTO.getDsChecksum());
                 mdRecord.setMimeType(datastreamProfileTO.getDsMIME());
                 mdRecord.setControlGroup(datastreamProfileTO.getDsControlGroup());
                 mdRecord.setDatastreamLocation(datastreamProfileTO.getDsLocation());
                 mdRecord.getRepositoryIndicator().setResourceIsNew(false);
 
                 if (ChecksumType.DISABLED.toString().equals(datastreamProfileTO.getDsChecksumType())
                     || "none".equals(datastreamProfileTO.getDsChecksumType())) {
                     mdRecord.setChecksumEnabled(false);
                 }
                 else {
                     mdRecord.setChecksumEnabled(true);
                 }
 
                 // alternate ids
                 if (datastreamProfileTO.getDsAltID().size() > 1) {
                     mdRecord.setType(datastreamProfileTO.getDsAltID().get(1));
 
                     if (datastreamProfileTO.getDsAltID().size() > 2) {
                         mdRecord.setSchema(datastreamProfileTO.getDsAltID().get(2));
                     }
                 }
                 else if (datastreamProfileTO.getDsAltID().size() <= 3) {
                     LOGGER.warn("Expected 3 entries in datastream profile alternative IDs.");
                 }
             }
         }
     }
 
     /**
      * Check if relation type is registered predicate.
      * 
      * @param predicate
      *            the predicate as URI
      * @throws InvalidContentException
      *             Thrown if predicate is invalid
      * @throws WebserverSystemException
      *             Thrown if internal error occur
      * @throws RelationPredicateNotFoundException
      *             Thrown if the predicate is not registered.
      */
     private static void checkRelationType(final URI predicate) throws RelationPredicateNotFoundException {
         if (!ContentRelationsUtility.validPredicate(predicate)) {
             throw new RelationPredicateNotFoundException("Predicate " + predicate
                 + " is not on the registered predicate list. ");
         }
 
     }
 
     /**
      * Obtain values from XML an create value object ContentRelation.
      * 
      * @param xml
      *            The content relation XML (validated by schema).
      * @return ContentRelation
      * @throws InvalidContentException
      *             Thrown if content is invalid
      * @throws MissingAttributeValueException
      *             Thrown if attribute value is missing
      * @throws SystemException
      *             Thrown if internal error occur
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
      */
     private static ContentRelationCreate parseContentRelation(final String xml) throws MissingAttributeValueException,
         InvalidContentException, SystemException, XmlCorruptedException {
 
         final StaxParser sp = new StaxParser();
 
         final ContentRelationHandler contentRelationHandler = new ContentRelationHandler(sp);
         sp.addHandler(contentRelationHandler);
 
         try {
             sp.parse(xml);
         }
         catch (final InvalidContentException e) {
             throw new InvalidContentException(e.getMessage(), e);
         }
         catch (final RelationPredicateNotFoundException e) {
             // shouldn't happen
             throw new SystemException(e);
         }
         catch (final XmlCorruptedException e) {
             throw new XmlCorruptedException(e.getMessage(), e);
         }
         catch (final MissingAttributeValueException e) {
             throw new MissingAttributeValueException(e.getMessage(), e);
         }
         catch (final InvalidStatusException e) {
             // shouldn't happen
             throw new SystemException(e);
         }
         catch (final SystemException e) {
             throw new SystemException(null, e);
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         return contentRelationHandler.getContentRelation();
     }
 
     /**
      * Enrich all meta data records with the (blob) content from repository.
      * 
      * @param cr
      *            ContentRelation
      * @throws WebserverSystemException
      *             Thrown if access to repository (Feodra) failed.
      */
     private static void enrichWithMetadataContent(final ContentRelationCreate cr) {
         final List<MdRecordCreate> mdRecords = cr.getMetadataRecords();
         if (mdRecords != null) {
             for (final MdRecordCreate mdRecord : mdRecords) {
                 if (mdRecord.getContent() == null) {
                     final Datastream ds =
                         new Datastream(mdRecord.getName(), cr.getObjid(), cr.getProperties().getVersionDate(), mdRecord
                             .getMimeType(), mdRecord.getDatastreamLocation(), mdRecord.getControlGroup());
                     mdRecord.setContent(new String(ds.getStream(), Charset.forName("UTF-8")));
                 }
             }
         }
     }
 
     /**
      * Validate Content Relation.
      * <p/>
      * Checks if the required values are given and references valid.
      * 
      * @param cr
      *            ContentRelation
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore access failed.
      * @throws WebserverSystemException
      *             If internal error occur
      * @throws ReferencedResourceNotFoundException
      *             Thrown if referenced resource does not exist.
      * @throws RelationPredicateNotFoundException
      *             Thrown if the predicate is not registered.
      * @throws InvalidContentException
      *             Thrown if predicate is invalid
      */
     private void validate(final ContentRelationCreate cr) throws TripleStoreSystemException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException {
 
         validateReference(cr.getSubject());
         validateReference(cr.getObject());
         checkRelationType(cr.getType());
     }
 
     /**
      * Check if referenced resources exists.
      * 
      * @param reference
      *            reference to be checked
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore access failed.
      * @throws WebserverSystemException
      *             If internal error occur
      * @throws ReferencedResourceNotFoundException
      *             Thrown if referenced resource does not exist.
      */
     private void validateReference(final String reference) throws TripleStoreSystemException,
         ReferencedResourceNotFoundException {
 
         if (!this.tripleStoreUtility.exists(reference)) {
             throw new ReferencedResourceNotFoundException("The referenced resource with objid=" + reference
                 + " does not exist.");
         }
 
     }
 
     /**
      * Checks if the ContentRelation has right status to change status to submitted.
      * 
      * @param cr
      *            ContentRelation which is to check.
      * @throws InvalidStatusException
      *             Thrown if the current status of the ContentRelation is invalid to change to submitted.
      */
     private static void validateToSubmitStatus(final ContentRelationCreate cr) throws InvalidStatusException {
 
         /*
          * Resource has to have status pending or in-revision when submit is possible.
          */
         if (!(cr.getProperties().getStatus() == StatusType.PENDING || cr.getProperties().getStatus() == StatusType.INREVISION)) {
             throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_PENDING + "' or '"
                 + Constants.STATUS_IN_REVISION + "' and can not be" + " submitted.");
         }
 
     }
 
     /**
      * Notify the listeners that a content relation was modified.
      * 
      * @param cr
      *            Content Relation
      * @param xmlData
      *            complete content relation XML
      * @throws SystemException
      *             One of the listeners threw an exception.
      */
     private void fireContentRelationModified(final ContentRelationCreate cr, final String xmlData)
         throws SystemException {
         for (final ResourceListener contentRelationListener : this.contentRelationListeners) {
             contentRelationListener.resourceModified(cr.getObjid(), xmlData);
         }
     }
 
     /**
      * Notify the listeners that an content relation was created.
      * 
      * @param cr
      *            content relation
      * @param xmlData
      *            complete content relation XML
      * @throws SystemException
      *             One of the listeners threw an exception.
      */
     private void fireContentRelationCreated(final ContentRelationCreate cr, final String xmlData)
         throws SystemException {
         for (final ResourceListener contentRelationListener : this.contentRelationListeners) {
             contentRelationListener.resourceCreated(cr.getObjid(), xmlData);
         }
     }
 
     /**
      * Notify the listeners that an content relation was deleted.
      * 
      * @param cr
      *            content relation
      * @throws SystemException
      *             One of the listeners threw an exception.
      */
     private void fireContentRelationDeleted(final ContentRelationCreate cr) throws SystemException {
         for (final ResourceListener contentRelationListener : this.contentRelationListeners) {
             contentRelationListener.resourceDeleted(cr.getObjid());
         }
     }
 
     /**
      * Prepare the assignment response message.
      * <p/>
      * Preconditions: The TripleStore must be in sync with the repository.
      * 
      * @param cr
      *            content relation
      * @param pid
      *            The new assigned PID.
      * @return response message
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      * @throws TripleStoreSystemException
      *             Thrown in case of TripleStore error.
      */
     private String prepareResponse(final ContentRelationCreate cr, final String pid) {
         return Utility.prepareReturnXml(cr.getProperties().getLastModificationDate(), "<pid>" + pid + "</pid>\n");
     }
 
 }
