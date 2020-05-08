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
 package de.escidoc.core.om.business.fedora.item;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.xml.stream.XMLStreamException;
 
 import org.esidoc.core.utils.io.MimeTypes;
 import org.esidoc.core.utils.io.Stream;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.business.fedora.resources.StatusType;
 import de.escidoc.core.common.business.fedora.resources.create.ItemCreate;
 import de.escidoc.core.common.business.fedora.resources.create.MdRecordCreate;
 import de.escidoc.core.common.business.fedora.resources.create.RelationCreate;
 import de.escidoc.core.common.business.fedora.resources.item.Component;
 import de.escidoc.core.common.business.filter.LuceneRequestParameters;
 import de.escidoc.core.common.business.filter.SRURequest;
 import de.escidoc.core.common.business.filter.SRURequestParameters;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.TmeException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingLicenceException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.FileNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.XmlSchemaNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyPublishedException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyWithdrawnException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.NotPublishedException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyViolationException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.MultipleExtractor;
 import de.escidoc.core.common.util.stax.handler.OptimisticLockingHandler;
 import de.escidoc.core.common.util.stax.handler.TaskParamHandler;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithChildElements;
 import de.escidoc.core.om.business.fedora.ContentRelationsUtility;
 import de.escidoc.core.om.business.fedora.contentRelation.FedoraContentRelationHandler;
 import de.escidoc.core.om.business.interfaces.ItemHandlerInterface;
 import de.escidoc.core.om.business.stax.handler.ContentRelationsAddHandler2Edition;
 import de.escidoc.core.om.business.stax.handler.ContentRelationsRemoveHandler2Edition;
 import de.escidoc.core.om.business.stax.handler.ContentRelationsUpdateHandler2Edition;
 import de.escidoc.core.om.business.stax.handler.MdRecordsUpdateHandler;
 import de.escidoc.core.om.business.stax.handler.component.NewComponentExtractor;
 import de.escidoc.core.om.business.stax.handler.item.ComponentMdRecordsUpdateHandler;
 import de.escidoc.core.om.business.stax.handler.item.ComponentUpdateHandler;
 import de.escidoc.core.om.business.stax.handler.item.ContentStreamHandler;
 import de.escidoc.core.om.business.stax.handler.item.ItemHandler;
 import de.escidoc.core.om.business.stax.handler.item.ItemUpdateHandler;
 
 /**
  * The retrieve, update, create and delete methods implement the {@link ItemHandlerInterface ItemHandlerInterface}.
  * These methods handle strings of xmlData and use the private (get,) set and render methods to set xmlData in the
  * system or get xmlData from the system.
  * <p/>
  * The private set methods take strings of xmlData as parameter and handling objects of type {@link Datastream Stream}
  * that hold the xmlData in an Item or Component object.
  * <p/>
  * To split incoming xmlData into the datastreams it consists of, the {@link StaxParser StaxParser} is used. In order to
  * modify datastreams or handle values provided in datastreams more than one Handler (implementations of DefaultHandler
  * can be added to the StaxParser. The {@link MultipleExtractor MultipleExtractor} have to be the last Handler in the
  * HandlerChain of a StaxParser.
  *
  * @author Frank Schwichtenberg
  */
 @Service("business.FedoraItemHandler")
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class FedoraItemHandler extends ItemHandlerPid implements ItemHandlerInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(FedoraItemHandler.class);
 
     @Autowired
     @Qualifier("business.FedoraContentRelationHandler")
     private FedoraContentRelationHandler contentRelationHandler;
 
     private static final String NO_UPDATE_ALLOWED_MSG = "No update allowed.";
 
     @Autowired
     @Qualifier("de.escidoc.core.common.business.filter.SRURequest")
     private SRURequest sruRequest;
 
     @Autowired
     @Qualifier("business.Utility")
     private Utility utility;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     @Override
     public String retrieve(final String id) throws ItemNotFoundException, MissingMethodParameterException,
         SystemException, ComponentNotFoundException, AuthorizationException {
 
         setItem(id);
         final String message =
             "You cannot access a full surrogate item representation"
                 + " because you have no access rights on the item " + getOriginId()
                 + " . You can access subressourcess owned by a " + "surrogate item using retrieve methods on "
                 + "subresources.";
         loadOrigin(message);
 
         return render();
     }
 
     /**
      * @return new XML representation of updated Item.
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#update(String, String)
      */
     @Override
     public String update(final String id, final String xmlData) throws ItemNotFoundException, FileNotFoundException,
         InvalidContextException, InvalidStatusException, LockingException, NotPublishedException,
         MissingLicenceException, ComponentNotFoundException, MissingAttributeValueException, InvalidXmlException,
         MissingMethodParameterException, InvalidContentException, SystemException, OptimisticLockingException,
         RelationPredicateNotFoundException, ReferencedResourceNotFoundException, ReadonlyVersionException,
         MissingMdRecordException, AuthorizationException, ReadonlyElementViolationException,
         ReadonlyAttributeViolationException {
 
         setItem(id);
         final DateTime startTimestamp = getItem().getLastFedoraModificationDate();
 
         checkLatestVersion();
         checkLocked();
         checkWithdrawn(NO_UPDATE_ALLOWED_MSG);
         String originId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         final String originVersionId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN_VERSION);
         if (originVersionId != null) {
             originId = originId + ':' + originVersionId;
         }
         final boolean origin =
             loadOrigin("You cannot update a full surrogate Item representation "
                 + "because you have no access rights on the Item '" + originId
                 + "'. Subressources which are part of the surrogate Item "
                 + "(and not the origin Item) are still accessible. Try " + "using subresource methods.");
 
         try {
             final StaxParser sp = new StaxParser();
 
             final OptimisticLockingHandler olh =
                 new OptimisticLockingHandler(getItem().getId(), Constants.ITEM_OBJECT_TYPE, getItem()
                     .getLastModificationDate());
             sp.addHandler(olh);
             final ContentRelationsUpdateHandler2Edition cruh = new ContentRelationsUpdateHandler2Edition(sp);
             sp.addHandler(cruh);
 
             sp.addHandler(new ItemUpdateHandler(getItem().getId(), sp));
 
             final MdRecordsUpdateHandler mdHandler = new MdRecordsUpdateHandler("/item/md-records", sp, origin);
             sp.addHandler(mdHandler);
 
             ContentStreamHandler csh = null;
             NewComponentExtractor nce = null;
             ComponentMdRecordsUpdateHandler cmuh = null;
 
             if (!origin) {
                 csh = new ContentStreamHandler(getItem());
                 sp.addHandler(csh);
                 final ComponentUpdateHandler cuh =
                     new ComponentUpdateHandler(getItem().getId(), "/item/components/component", sp);
                 sp.addHandler(cuh);
                 nce = new NewComponentExtractor(sp);
                 cmuh = new ComponentMdRecordsUpdateHandler("/item/components/component", sp);
 
                 sp.addHandler(cmuh);
                 sp.addHandler(nce);
             }
             final HashMap<String, String> extractPathes = new HashMap<String, String>();
             extractPathes.put("/item/properties/" + Elements.ELEMENT_CONTENT_MODEL_SPECIFIC + "", null);
             extractPathes.put("/item/relations", null);
             extractPathes.put("/item/resources", null);
             extractPathes.put("/item/md-records/md-record", "name");
             // extractPathes.put("/item/components/component", "objid");
             if (!origin) {
                 extractPathes.put("/item/components/component/properties", null);
                 extractPathes.put("/item/components/component/content", null);
                 extractPathes.put("/item/components/component/md-records/md-record", "name");
             }
             final MultipleExtractor me = new MultipleExtractor(extractPathes, sp);
             sp.addHandler(me);
 
             try {
                 sp.parse(xmlData);
             }
             catch (final OptimisticLockingException e) {
                 throw e;
             }
             catch (final MissingAttributeValueException e) {
                 throw e;
             }
             catch (final MissingMdRecordException e) {
                 throw e;
             }
             catch (final WebserverSystemException e) {
                 throw e;
             }
             catch (final InvalidContentException e) {
                 throw e;
             }
             catch (final InvalidXmlException e) {
                 throw e;
             }
             catch (final ReferencedResourceNotFoundException e) {
                 throw e;
             }
             catch (final RelationPredicateNotFoundException e) {
                 throw e;
             }
             catch (final TripleStoreSystemException e) {
                 throw e;
             }
             catch (final EncodingSystemException e) {
                 throw e;
             }
             catch (final XmlParserSystemException e) {
                 throw e;
             }
             catch (final XMLStreamException e) {
                 throw new XmlParserSystemException(e);
             }
             catch (final Exception e) {
                 XmlUtility.handleUnexpectedStaxParserException("", e);
             }
             sp.clearHandlerChain();
 
             final Map<String, Object> streams = me.getOutputStreams();
             try {
                 final Object cmsStream = streams.get(Elements.ELEMENT_CONTENT_MODEL_SPECIFIC);
                 if (cmsStream != null) {
                     setContentTypeSpecificProperties(((ByteArrayOutputStream) cmsStream)
                         .toString(XmlUtility.CHARACTER_ENCODING));
                 }
             }
             catch (final UnsupportedEncodingException e) {
                 throw new EncodingSystemException(e.getMessage(), e);
             }
             final Map<String, ByteArrayOutputStream> mdRecordsStreams =
                 (Map<String, ByteArrayOutputStream>) streams.get(XmlUtility.NAME_MDRECORDS);
             if (mdRecordsStreams != null && !mdRecordsStreams.containsKey("escidoc") && !origin) {
                 throw new MissingMdRecordException("No escidoc internal metadata found " + "(md-record/@name='escidoc'");
             }
             final Map<String, Map<String, String>> mdRecordsAttributes = mdHandler.getMetadataAttributes();
             final String escidocMdNsUri = mdHandler.getEscidocMdRecordNameSpace();
             setMetadataRecords(mdRecordsStreams, mdRecordsAttributes, escidocMdNsUri);
 
             // set content streams
             if (!origin) {
                 setContentStreams(csh.getContentStreams());
             }
             // set content relations
             final List<String> relationsToUpdate = cruh.getContentRelationsData();
             getItem().setContentRelations(sp, relationsToUpdate);
 
             // components
             // TODO: Aenderungen an ITEM.RELS-EXT fuer alle components in einem
             // schritt machen + Aenderungen an content relations (? FRS)
             boolean resourceUpdated = false;
             if (!origin) {
                 Map<String, Object> components = (Map<String, Object>) streams.get("components");
 
                 if (components == null) {
                     components = new HashMap<String, Object>();
                 }
                 final Map<String, Map<String, Map<String, String>>> componentMdRecordsAttributes =
                     cmuh.getMetadataAttributes();
 
                 final Map<String, String> nsUris = cmuh.getNamespacesMap();
                 components.put("new", nce.getOutputStreams());
                 setComponents(components, componentMdRecordsAttributes, nsUris);
 
                 // this persist is necessary to control the Components
                 resourceUpdated = getItem().persistComponents();
             }
 
             // check if modified
             final String updatedXmlData;
             if (resourceUpdated || !startTimestamp.isEqual(getItem().getLastFedoraModificationDate())
                 || getItem().isNewVersion()) {
                 // object is modified
                 makeVersion("ItemHandler.update()");
                 getItem().persist();
 
                 updatedXmlData = retrieve(getItem().getId());
                 fireItemModified(getItem().getId(), updatedXmlData);
             }
             else {
                 updatedXmlData = render();
             }
 
             return updatedXmlData;
         }
         catch (final MissingContentException e) {
             throw new WebserverSystemException(e);
         }
         catch (final MissingElementValueException e) {
             throw new WebserverSystemException("unreachable", e);
         }
     }
 
     /**
      * Create an Item.
      */
     @Override
     public String create(final String xml) throws MissingContentException, ContextNotFoundException,
         ContentModelNotFoundException, ReadonlyElementViolationException, MissingAttributeValueException,
         MissingElementValueException, ReadonlyAttributeViolationException, XmlCorruptedException,
         MissingMethodParameterException, FileNotFoundException, SystemException, ReferencedResourceNotFoundException,
         InvalidContentException, RelationPredicateNotFoundException, MissingMdRecordException, InvalidStatusException,
         AuthorizationException {
 
         final ItemCreate item = parseItem(xml);
 
         // check that the objid was not obtained from the representation
         item.setObjid(null);
 
         item.setIdProvider(getIdProvider());
         validateCreate(item);
         item.persist(true);
         final String objid = item.getObjid();
         final String resultItem;
         try {
             resultItem = retrieve(objid);
         }
         catch (final ResourceNotFoundException e) {
             throw new IntegritySystemException("The Item with id '" + objid + "', which was just created, "
                 + "could not be found for retrieve.", e);
         }
         fireItemCreated(objid, resultItem);
         return resultItem;
     }
 
     /**
      * Ingest an item.
      *
      * @param xml The item to be ingested.
      * @return the pid of the ingested item.
      */
     @Override
     public String ingest(final String xml) throws ReadonlyElementViolationException,
         ReadonlyAttributeViolationException, MissingContentException, ContextNotFoundException,
         ContentModelNotFoundException, MissingAttributeValueException, MissingElementValueException,
         InvalidXmlException, MissingMethodParameterException, FileNotFoundException,
         ReferencedResourceNotFoundException, InvalidContentException, RelationPredicateNotFoundException,
         MissingMdRecordException, InvalidStatusException, SystemException, AuthorizationException {
 
         final ItemCreate item = parseItem(xml);
         item.setIdProvider(getIdProvider());
         validateIngest(item);
         item.persist(true);
         final String objid = item.getObjid();
         try {
             if (EscidocConfiguration.getInstance().getAsBoolean(
                 EscidocConfiguration.ESCIDOC_CORE_NOTIFY_INDEXER_ENABLED)) {
                 fireItemCreated(objid, retrieve(objid));
             }
         }
         catch (final ResourceNotFoundException e) {
             throw new IntegritySystemException("The Item with id '" + objid + "', which was just ingested, "
                 + "could not be found for retrieve.", e);
         }
         return objid;
 
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#delete(String)
      */
     @Override
     public void delete(final String id) throws ItemNotFoundException, AlreadyPublishedException, LockingException,
         InvalidStatusException, SystemException, AuthorizationException {
 
         remove(id);
         fireItemDeleted(id);
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveProperties(String)
      */
     @Override
     public String retrieveProperties(final String id) throws ItemNotFoundException, MissingMethodParameterException,
         SystemException {
 
         setItem(id);
         return renderProperties();
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveMdRecords(String)
      */
     @Override
     public String retrieveMdRecords(final String id) throws ItemNotFoundException, MissingMethodParameterException,
         SystemException, AuthorizationException {
 
         setItem(id);
         String originId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         final String originVersionId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN_VERSION);
         if (originVersionId != null) {
             originId = originId + ':' + originVersionId;
         }
         loadOrigin("You cannot retrieve md-records of the surrogate Item "
             + "because you have no access rights on the Item '" + originId
             + "'. Subressources which are part of the surrogate Item "
             + "(and not the origin Item) are still accessible. Try " + "using subresource methods.");
 
         return renderMdRecords(true);
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveMdRecord(String, String)
      */
     @Override
     public String retrieveMdRecord(final String id, final String mdRecordId) throws ItemNotFoundException,
         MdRecordNotFoundException, MissingMethodParameterException, SystemException, AuthorizationException {
 
         setItem(id);
 
         String mdRecord;
         try {
             mdRecord = renderMdRecord(mdRecordId, false, true);
             if (mdRecord.length() == 0) {
                 throw new MdRecordNotFoundException();
             }
         }
         catch (final MdRecordNotFoundException e) {
             final String originObjectId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
             if (originObjectId != null) {
                 final String message =
                     "Md-record with name " + mdRecordId + " is owned by the item " + getOriginId()
                         + ", which is reffered by a surrogate item " + id + ". You have no access rights on the item "
                         + getOriginId() + ". Therefore you cannot access any md-records " + "of this item.";
                 loadOrigin(message);
                 mdRecord = renderMdRecord(mdRecordId, true, true);
             }
             else {
                 throw e;
             }
         }
 
         if (mdRecord.length() == 0) {
             throw new MdRecordNotFoundException("Metadata record with name " + mdRecordId + " not found in item " + id
                 + '.');
         }
         return mdRecord;
     }
 
     /**
      * This Method is used by OAI Provider to retrieve metadata without the surrounding md-record element.
      *
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveMdRecordContent(String, String)
      */
     @Override
     public String retrieveMdRecordContent(final String id, final String mdRecordId) throws ItemNotFoundException,
         MdRecordNotFoundException, MissingMethodParameterException, SystemException, AuthorizationException {
         setItem(id);
         String mdRecord;
         try {
             mdRecord = retrieveMdRecord(mdRecordId, false);
         }
         catch (final MdRecordNotFoundException e) {
             String message =
                 "Md-record with name " + mdRecordId + " is owned by the item " + getOriginId()
                     + ", which is reffered by a surrogate item " + id + ". You have no access rights on the item "
                     + getOriginId() + ". Therefore you cannot access any md-records " + "of this item.";
             final boolean origin = loadOrigin(message);
             if (origin) {
                 mdRecord = retrieveMdRecord(mdRecordId, true);
             }
             else {
                 message = "Metadata record with name " + mdRecordId + " not found in item " + id + '.';
                 throw new MdRecordNotFoundException(message, e);
             }
         }
         return mdRecord;
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveMdRecordContent(String, String)
      */
     @Override
     public String retrieveDcRecordContent(final String id) throws ItemNotFoundException,
         MissingMethodParameterException, SystemException, MdRecordNotFoundException, AuthorizationException {
         setItem(id);
         String dc;
         try {
             final Datastream mdRecord = getItem().getMdRecord(XmlTemplateProvider.DEFAULT_METADATA_FOR_DC_MAPPING);
             if (mdRecord.isDeleted()) {
                 throw new MdRecordNotFoundException();
             }
             dc = getItem().getDc().toString();
         }
         catch (final MdRecordNotFoundException e) {
             String message =
                 "Md-record with name DC" + " is owned by the item " + getOriginId()
                     + ", which is reffered by a surrogate item " + id + ". You have no access rights on the item "
                     + getOriginId() + ". Therefore you cannot access any md-records " + "of this item.";
             final boolean origin = loadOrigin(message);
             if (origin) {
                 dc = getOriginItem().getDc().toString();
             }
             else {
                 message = "Metadata record with name DC" + " not found in item " + id + '.';
                 throw new MdRecordNotFoundException(message, e);
             }
 
         }
         return dc;
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#updateMdRecord(String, String, String)
      */
     @Override
     public String updateMetadataRecord(final String id, final String mdRecordId, final String xmlData)
         throws ItemNotFoundException, XmlSchemaNotFoundException, LockingException, XmlCorruptedException,
         XmlSchemaValidationException, InvalidContentException, MdRecordNotFoundException, ReadonlyViolationException,
         MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
         ReadonlyVersionException, AuthorizationException {
 
         setItem(id);
         final DateTime startTimestamp = getItem().getLastFedoraModificationDate();
 
         checkLatestVersion();
         checkLocked();
         checkReleased();
         checkWithdrawn(NO_UPDATE_ALLOWED_MSG);
 
         final StaxParser sp = new StaxParser();
         final OptimisticLockingHandler olh =
             new OptimisticLockingHandler(getItem().getId(), Constants.ITEM_OBJECT_TYPE, getItem()
                 .getLastModificationDate());
         sp.addHandler(olh);
         final MdRecordsUpdateHandler mdHandler = new MdRecordsUpdateHandler("", sp);
         sp.addHandler(mdHandler);
 
         final MultipleExtractor me = new MultipleExtractor("/md-record", "name", sp);
         sp.addHandler(me);
 
         try {
             sp.parse(xmlData);
         }
         catch (final XMLStreamException e) {
             // the only exception thrown by MultipleExtractor
             throw new XmlParserSystemException(e);
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(e.getMessage(), e);
         }
 
         final Map mds = (Map) me.getOutputStreams().get(XmlUtility.NAME_MDRECORDS);
         // there is only one md-record (root element is md-record)
         final ByteArrayOutputStream mdXml = (ByteArrayOutputStream) mds.get(mdRecordId);
         final Map<String, Map<String, String>> mdAttributes = mdHandler.getMetadataAttributes();
         final Map<String, String> mdRecordAttributes = mdAttributes.get(mdRecordId);
         try {
             setMetadataRecord(mdRecordId, mdXml.toString(XmlUtility.CHARACTER_ENCODING), mdRecordAttributes);
         }
         catch (final UnsupportedEncodingException e) {
             throw new EncodingSystemException(e.getMessage(), e);
         }
 
         if (!startTimestamp.isEqual(getItem().getLastFedoraModificationDate())) {
             makeVersion("Item.updateMedataRecord");
             getItem().persist();
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final ComponentNotFoundException e) {
                 throw new SystemException(e);
             }
         }
         final String newMdRecord;
         try {
             newMdRecord = retrieveMdRecord(getItem().getId(), mdRecordId);
         }
         catch (final MdRecordNotFoundException e) {
             throw new IntegritySystemException("After succesfully update metadata.", e);
         }
 
         return newMdRecord;
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#createMetadataRecord(String, String)
      */
     @Override
     @Deprecated
     public String createMetadataRecord(final String id, final String xmlData) throws ItemNotFoundException,
         XmlSchemaNotFoundException, SystemException, XmlSchemaValidationException, LockingException,
         MissingAttributeValueException, InvalidStatusException, MissingMethodParameterException, XmlCorruptedException,
         ComponentNotFoundException, AuthorizationException {
 
         return createMdRecord(id, xmlData);
     }
 
     /**
      * Create Metadata Record.
      *
      * @param id      objid of Item
      * @param xmlData XML of new Metadata Record
      * @return XML representation of created Metadata Record
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#createMdRecord(String, String)
      */
     @Override
     public String createMdRecord(final String id, final String xmlData) throws ItemNotFoundException, SystemException,
         XmlSchemaValidationException, LockingException, MissingAttributeValueException, InvalidStatusException,
         ComponentNotFoundException, AuthorizationException {
 
         setItem(id);
         final DateTime startTimestamp = getItem().getLastFedoraModificationDate();
 
         checkLocked();
         checkReleased();
 
         final HashMap<String, String> extractPathes = new HashMap<String, String>();
         extractPathes.put("/md-record", "name");
 
         final StaxParser sp = new StaxParser();
         final MultipleExtractor me = new MultipleExtractor(extractPathes, sp);
         sp.addHandler(me);
 
         // retrieve name from xml
         try {
             sp.parse(xmlData);
         }
         catch (final Exception e) {
             throw new WebserverSystemException("Unexpected exception while parsing xml data in "
                 + "FedoraItemHandler.createMetadataRecord.", e);
         }
         final Map map = me.getOutputStreams();
         final Map mdRecords = (Map) map.get(XmlUtility.NAME_MDRECORDS);
         final Set keySet = mdRecords.keySet();
         final Iterator it = keySet.iterator();
         if (!it.hasNext()) {
             throw new XmlSchemaValidationException("No name found for metadata datastream.");
         }
         final String name = (String) it.next();
 
         final byte[] xmlDataBytes;
         try {
             xmlDataBytes = xmlData.getBytes(XmlUtility.CHARACTER_ENCODING);
         }
         catch (final UnsupportedEncodingException e) {
             throw new EncodingSystemException(e.getMessage(), e);
         }
         final Datastream newMDS = new Datastream(name, getItem().getId(), xmlDataBytes, MimeTypes.TEXT_XML);
         newMDS.addAlternateId(Datastream.METADATA_ALTERNATE_ID); // this is the
         // reason for
         // setMdRecord etc.
         // FIXME persist DS by set it in the resource object
         newMDS.persist(false);
 
         final DateTime endTimestamp = getItem().getLastFedoraModificationDate();
         if (!startTimestamp.equals(endTimestamp)) {
             makeVersion("Item.createMetadataRecord");
             getItem().persist();
         }
         final String newMdRecord;
         try {
             newMdRecord = retrieveMdRecord(getItem().getId(), name);
             fireItemModified(getItem().getId(), retrieve(getItem().getId()));
         }
         catch (final ItemNotFoundException e) {
             throw new IntegritySystemException("After succesfully create metadata.", e);
         }
         catch (final MdRecordNotFoundException e) {
             throw new IntegritySystemException("After succesfully create metadata.", e);
         }
         catch (final MissingMethodParameterException e) {
             throw new IntegritySystemException("After succesfully create metadata.", e);
         }
 
         return newMdRecord;
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveComponents(String)
      */
     @Override
     public String retrieveComponents(final String id) throws ItemNotFoundException, ComponentNotFoundException,
         MissingMethodParameterException, SystemException, AuthorizationException {
 
         setItem(id);
         String originId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         final String originVersionId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN_VERSION);
         if (originVersionId != null) {
             originId = originId + ':' + originVersionId;
         }
         loadOrigin("You have no access rights on the item " + originId + " , which is reffered by a surrogate item "
             + id + ". Therefore you cannot access any subressources of this item.");
 
         return renderComponents(true);
     }
 
     /**
      * Retrieve all content relation in which the current item is subject or object.
      *
      * @param id item id
      * @return list of content relations
      * @throws ItemNotFoundException Thrown if an item with the specified id could not be found.
      * @throws SystemException       If an error occurs.
      */
     private String retrieveContentRelations(final String id) throws ItemNotFoundException, SystemException {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         setItem(id);
         filterParams.put("query", new String[] { "\"/subject/id\"=" + getItem().getId() + " or " + "\"/subject/id\"="
             + getItem().getFullId() + " or " + "\"/object/id\"=" + getItem().getId() + " or " + "\"/object/id\"="
             + getItem().getFullId() });
 
         final String searchResponse =
             contentRelationHandler.retrieveContentRelations(new LuceneRequestParameters(filterParams));
         return transformSearchResponse2relations(searchResponse);
 
     }
 
     /**
      * @param id
      * @param xmlData
      * @return
      * @throws ItemNotFoundException
      * @throws ComponentNotFoundException
      * @throws FileNotFoundException
      * @throws XmlSchemaValidationException
      * @throws LockingException
      * @throws ReadonlyElementViolationException
      * @throws ReadonlyAttributeViolationException
      * @throws MissingAttributeValueException
      * @throws AlreadyPublishedException
      * @throws InvalidStatusException
      * @throws SystemException
      * @throws MissingMethodParameterException
      * @throws OptimisticLockingException
      * @throws MissingContentException
      * @throws MissingElementValueException
      * @throws InvalidContentException
      * @throws XmlCorruptedException
      * @throws ReadonlyVersionException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      */
     public String updateComponents(final String id, final String xmlData) throws ItemNotFoundException,
         ComponentNotFoundException, FileNotFoundException, LockingException, ReadonlyElementViolationException,
         ReadonlyAttributeViolationException, InvalidStatusException, SystemException, MissingMethodParameterException,
         MissingContentException, MissingElementValueException, InvalidContentException, ReadonlyVersionException {
 
         setItem(id);
         checkLatestVersion();
         checkLocked();
         checkReleased();
         checkWithdrawn(NO_UPDATE_ALLOWED_MSG);
 
         // TODO check if this is realy needed. the intension may be to set the
         // components map.
 
         renderComponents(false);
 
         final DateTime startTimestamp = getItem().getLastFedoraModificationDate();
 
         final StaxParser sp = new StaxParser();
         final OptimisticLockingHandler olh =
             new OptimisticLockingHandler(getItem().getId(), Constants.ITEM_OBJECT_TYPE, getItem()
                 .getLastModificationDate());
         sp.addHandler(olh);
 
         final ComponentMdRecordsUpdateHandler cmuh =
             new ComponentMdRecordsUpdateHandler("/components/component/md-records", sp);
         sp.addHandler(cmuh);
         // extract datastreams from xmlData
         final HashMap<String, String> extractPathes = new HashMap<String, String>();
         extractPathes.put("/components/component", "objid");
         extractPathes.put("/components/component/properties", null);
         extractPathes.put("/components/component/licenses", null);
         extractPathes.put("/components/component/content", null);
         extractPathes.put("/components/component/md-records/md-record", "name");
 
         final MultipleExtractor me = new MultipleExtractor(extractPathes, sp);
         sp.addHandler(me);
 
         try {
             sp.parse(xmlData);
         }
         catch (final XMLStreamException e) {
             // the only exception thrown from MultipleExtractor
             throw new XmlParserSystemException(e);
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
 
         final Map<String, Object> streams = me.getOutputStreams();
         setComponents(streams, cmuh.getMetadataAttributes(), cmuh.getNamespacesMap());
 
         // return the new item
         String updatedXmlData = null;
         try {
             updatedXmlData = retrieveComponents(id);
         }
         catch (final AuthorizationException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on retrieving components.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on retrieving components.", e);
             }
             // can not occur
         }
 
         if (!startTimestamp.isEqual(getItem().getLastFedoraModificationDate())) {
             makeVersion("Item.updateComponents");
             getItem().persist();
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
         }
 
         return updatedXmlData;
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveResources(String)
      */
     @Override
     public String retrieveResources(final String id) throws ItemNotFoundException, MissingMethodParameterException,
         WebserverSystemException, TripleStoreSystemException, IntegritySystemException, FedoraSystemException,
         XmlParserSystemException {
 
         setItem(id);
         return renderResources();
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#retrieveResources(String)
      */
     @Override
     public EscidocBinaryContent retrieveResource(
         final String id, final String resourceName, final Map<String, String[]> parameters) throws SystemException,
         ItemNotFoundException, OperationNotFoundException {
 
         final EscidocBinaryContent content = new EscidocBinaryContent();
         content.setMimeType(MimeTypes.TEXT_XML);
 
         if ("version-history".equals(resourceName)) {
             try {
                 content.setContent(new ByteArrayInputStream(retrieveVersionHistory(id).getBytes(
                     XmlUtility.CHARACTER_ENCODING)));
                 return content;
             }
             catch (final UnsupportedEncodingException e) {
                 throw new WebserverSystemException(e);
             }
         }
         else if ("relations".equals(resourceName)) {
             try {
                 content.setContent(new ByteArrayInputStream(retrieveContentRelations(id).getBytes(
                     XmlUtility.CHARACTER_ENCODING)));
                 return content;
             }
             catch (final UnsupportedEncodingException e) {
                 throw new WebserverSystemException(e);
             }
         }
 
         setItem(id);
         final String contentModelId = getItem().getProperty(PropertyMapKeys.LATEST_VERSION_CONTENT_MODEL_ID);
         final Stream stream = this.getFedoraServiceClient().getDissemination(id, contentModelId, resourceName);
         try {
             content.setContent(stream.getInputStream());
         }
         catch (IOException e) {
             throw new FedoraSystemException("Error on reading stream.", e);
         }
         return content;
     }
 
     @Override
     public String retrieveRelations(final String id) throws ItemNotFoundException, MissingMethodParameterException,
         SystemException {
         setItem(id);
         return renderRelations();
     }
 
     @Override
     public String retrieveComponent(final String id, final String componentId) throws ItemNotFoundException,
         ComponentNotFoundException, MissingMethodParameterException, SystemException, AuthorizationException {
 
         setItem(id);
         String originId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         final String originVersionId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN_VERSION);
         if (originVersionId != null) {
             originId = originId + ':' + originVersionId;
         }
         loadOrigin("You have no access rights on the item " + originId + " , which is reffered by a surrogate item "
             + id + ". Therefore you cannot access any subressources of this item.");
 
         return renderComponent(componentId, true);
     }
 
     /**
      * @see ItemHandlerInterface #retrieveComponentMdRecords(java.lang.String,java.lang.String)
      */
     @Override
     public String retrieveComponentMdRecords(final String id, final String componentId) throws ItemNotFoundException,
         ComponentNotFoundException, MissingMethodParameterException, SystemException, AuthorizationException {
 
         setItem(id);
         String originId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         final String originVersionId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN_VERSION);
         if (originVersionId != null) {
             originId = originId + ':' + originVersionId;
         }
         loadOrigin("You have no access rights on the item " + originId + " , which is reffered by a surrogate item "
             + id + ". Therefore you cannot access any subressources of this item.");
 
         return renderComponentMdRecords(componentId, true);
     }
 
     @Override
     public String retrieveComponentMdRecord(final String id, final String componentId, final String mdRecordId)
         throws ItemNotFoundException, ComponentNotFoundException, MdRecordNotFoundException,
         MissingMethodParameterException, SystemException, AuthorizationException {
 
         setItem(id);
         String originId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         final String originVersionId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN_VERSION);
         if (originVersionId != null) {
             originId = originId + ':' + originVersionId;
         }
         loadOrigin("You have no access rights on the item " + originId + " , which is reffered by a surrogate item "
             + id + ". Therefore you cannot access any subressources of this item.");
 
         return renderComponentMdRecord(componentId, mdRecordId, true);
     }
 
     @Override
     public void deleteComponent(final String itemId, final String componentId) throws LockingException,
         MissingMethodParameterException, SystemException, InvalidStatusException, ComponentNotFoundException,
         ItemNotFoundException {
 
         setItem(itemId);
 
         // TODO ugly stuff, removing a Component of the item beside the Item!
         // this is easy to move into the Item class
         // removeComponent(componentId);
         getItem().deleteComponent(componentId);
 
         makeVersion("Item.deleteComponent");
         getItem().persist();
 
         try {
             fireItemModified(getItem().getId(), retrieve(getItem().getId()));
         }
         catch (final AuthorizationException e) {
             throw new SystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      *
      * @param id      The id of the Item
      * @param xmlData The XML representation of the component.
      * @throws MissingAttributeValueException cf. Interface
      */
     @Override
     public String createComponent(final String id, final String xmlData) throws ItemNotFoundException,
         MissingContentException, LockingException, MissingElementValueException, XmlCorruptedException,
         InvalidStatusException, MissingMethodParameterException, FileNotFoundException, InvalidContentException,
         SystemException, XmlSchemaValidationException, OptimisticLockingException, MissingAttributeValueException,
         ComponentNotFoundException, ReadonlyElementViolationException, ReadonlyAttributeViolationException {
 
         setItem(id);
         final StaxParser sp = new StaxParser();
         sp.addHandler(new OptimisticLockingHandler(getItem().getId(), Constants.ITEM_OBJECT_TYPE, getItem()
             .getLastModificationDate()));
 
         try {
             sp.parse(xmlData);
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final WebserverSystemException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         sp.clearHandlerChain();
         final String addedComponent = addComponent(xmlData);
         makeVersion("Component added.");
         getItem().persist();
 
         try {
             fireItemModified(getItem().getId(), retrieve(getItem().getId()));
         }
         catch (final AuthorizationException e) {
             throw new SystemException(e);
         }
 
         return addedComponent;
     }
 
     /**
      * @see de.escidoc.core.om.service.interfaces.ItemHandlerInterface#updateComponent(String, String, String)
      */
     @Override
     public String updateComponent(final String id, final String componentId, final String xmlData)
         throws ItemNotFoundException, ComponentNotFoundException, LockingException, XmlSchemaValidationException,
         FileNotFoundException, ReadonlyElementViolationException, MissingAttributeValueException,
         InvalidStatusException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         XmlCorruptedException, ReadonlyAttributeViolationException, MissingContentException, InvalidContentException,
         ReadonlyVersionException, AuthorizationException {
 
         setItem(id);
         if (!getItem().getComponentIds().contains(componentId)) {
             try {
                 getItem().addComponent(new Component(componentId, getItem().getId(), null));
             }
             catch (final ResourceNotFoundException e) {
                 throw new ComponentNotFoundException(e);
             }
         }
         final DateTime startTimestamp = getItem().getLastFedoraModificationDate();
 
         checkLatestVersion();
         checkLocked();
         checkReleased();
         checkWithdrawn(NO_UPDATE_ALLOWED_MSG);
 
         final HashMap<String, String> extractPathes = new HashMap<String, String>();
         extractPathes.put("/component/properties", null);
         extractPathes.put("/component/md-records/md-record", "name");
         extractPathes.put("/component/content", null);
 
         final StaxParser sp = new StaxParser();
         final OptimisticLockingHandler olh =
             new OptimisticLockingHandler(getItem().getId(), Constants.ITEM_OBJECT_TYPE, getItem()
                 .getLastModificationDate());
         sp.addHandler(olh);
         final ComponentMdRecordsUpdateHandler cmuh = new ComponentMdRecordsUpdateHandler("/component/md-records", sp);
         sp.addHandler(cmuh);
         final MultipleExtractor me = new MultipleExtractor(extractPathes, sp);
         sp.addHandler(me);
 
         try {
             sp.parse(xmlData);
         }
         catch (final XMLStreamException e) {
             // the only exception MultipleExtractor throws
             throw new XmlParserSystemException(e);
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
 
         final Map<String, Object> compsMap = (Map<String, Object>) me.getOutputStreams().get("components");
         final Map compMap = (Map) compsMap.get(componentId);
 
         setComponent(getItem().getComponent(componentId), compMap, cmuh.getMetadataAttributes().get(componentId), cmuh
             .getNamespacesMap().get(componentId));
 
         final String updatedXmlData = retrieveComponent(id, componentId);
 
         if (!startTimestamp.isEqual(getItem().getLastFedoraModificationDate())) {
             makeVersion("Item.updateComponent");
             getItem().persist();
 
             fireItemModified(getItem().getId(), retrieve(getItem().getId()));
         }
 
         return updatedXmlData;
     }
 
     @Override
     public String release(final String id, final String param) throws ItemNotFoundException, LockingException,
         InvalidStatusException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException, ComponentNotFoundException, XmlCorruptedException {
 
         setItem(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         checkWithdrawn("No modification allowed.");
         checkLocked();
         checkLatestVersion();
         checkReleased();
         checkPid();
 
         if (Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + getItem().getId())) {
 
             // check version status
             final String curStatus = getItem().getProperty(PropertyMapKeys.LATEST_VERSION_VERSION_STATUS);
             if (!Constants.STATUS_SUBMITTED.equals(curStatus)) {
                 throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_SUBMITTED
                     + "' and can not be " + Constants.STATUS_RELEASED + '.');
             }
 
             // set status "released"
             // only renew the timestamp and set status with version entry
             makeVersion(taskParameter.getComment(), Constants.STATUS_RELEASED);
             getItem().setLatestReleasePid();
             getItem().persist();
 
             // notify indexer
             // getUtility().notifyIndexerAddPublication(getItem().getHref());
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
             // find surrogate items which reference this item by a floating
             // reference, recache them and if necessary reindex them.
             final List<String> surrogateItemIds = this.tripleStoreUtility.getSurrogates(getItem().getId());
             final Collection<String> referencedSurrogateItemIds = new ArrayList<String>();
             for (final String surrogateId : surrogateItemIds) {
                 final String versionId =
                     this.tripleStoreUtility.getRelation(surrogateId, TripleStoreUtility.PROP_ORIGIN_VERSION);
                 if (versionId == null) {
                     setOriginId(getItem().getId());
                     setOriginItem(getItem());
                     referencedSurrogateItemIds.add(surrogateId);
                 }
             }
             // run item recaching/reindexing asynchronously
             queueItemsModified(referencedSurrogateItemIds);
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     @Override
     public String submit(final String id, final String param) throws ItemNotFoundException, LockingException,
         InvalidStatusException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException, ComponentNotFoundException, XmlCorruptedException {
 
         setItem(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         checkWithdrawn("No modification allowed.");
         checkLatestVersion();
         checkLocked();
         checkReleased();
 
         if (Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + getItem().getId())) {
 
             // check version status
             final String curStatus = getItem().getVersionStatus();
             if (!(Constants.STATUS_PENDING.equals(curStatus) || Constants.STATUS_IN_REVISION.equals(curStatus))) {
                 throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_PENDING + "' or '"
                     + Constants.STATUS_IN_REVISION + "' and can not be" + " submitted.");
             }
 
             // set status "submited"
             // only renew the timestamp and set status with version entry
             makeVersion(taskParameter.getComment(), Constants.STATUS_SUBMITTED);
             getItem().persist();
 
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     @Override
     public String revise(final String id, final String param) throws ItemNotFoundException, LockingException,
         InvalidStatusException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException, XmlCorruptedException, ComponentNotFoundException {
 
         setItem(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         checkWithdrawn("No modification allowed.");
         checkLatestVersion();
         checkLocked();
         checkVersionStatus(Constants.STATUS_SUBMITTED);
 
         if (Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + getItem().getId())) {
 
             // set status "in-revision"
             // only renew the timestamp and set status with version entry
             makeVersion(taskParameter.getComment(), Constants.STATUS_IN_REVISION);
             getItem().persist();
 
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     @Override
     public String withdraw(final String id, final String param) throws ItemNotFoundException, NotPublishedException,
         LockingException, AlreadyWithdrawnException, InvalidStatusException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, ReadonlyViolationException, ReadonlyVersionException,
         ComponentNotFoundException, XmlCorruptedException {
 
         // we want special exceptions if already withdrawn, so check something
         // before setItem()
         this.utility.checkIsItem(id);
 
         final String curStatus =
             this.tripleStoreUtility.getPropertiesElements(id, TripleStoreUtility.PROP_PUBLIC_STATUS);
         if (curStatus.equals(Constants.STATUS_WITHDRAWN)) {
             throw new AlreadyWithdrawnException("The object is already withdrawn");
         }
         if (!curStatus.equals(Constants.STATUS_RELEASED)) {
             throw new NotPublishedException("The object is not in state 'released' and can not be " + "withdrawn.");
         }
 
         setItem(id);
 
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         String withdrawComment = taskParameter.getWithdrawComment();
         if (withdrawComment == null) {
             withdrawComment = taskParameter.getComment();
             if (withdrawComment == null) {
                 throw new MissingMethodParameterException("No withdraw comment found.");
             }
         }
 
         checkLatestVersion();
         checkLocked();
 
         if (Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + getItem().getId())) {
 
             makeVersion(withdrawComment, Constants.STATUS_WITHDRAWN);
             getItem().persist();
 
             // getUtility().notifyIndexerDeletePublication(getItem().getHref());
 
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     @Override
     public String addContentRelations(final String id, final String param) throws SystemException,
         ItemNotFoundException, OptimisticLockingException, ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, AlreadyExistsException, InvalidStatusException, InvalidContentException,
         InvalidXmlException, ReadonlyAttributeViolationException, MissingElementValueException, LockingException,
         ReadonlyElementViolationException, ReadonlyVersionException, ComponentNotFoundException {
 
         setItem(id);
         checkLatestVersion();
         checkLocked();
         checkWithdrawn("Adding of content relations is not allowed.");
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + id);
 
         final StaxParser sp = new StaxParser();
 
         final ContentRelationsAddHandler2Edition addHandler =
             new ContentRelationsAddHandler2Edition(sp, getItem().getId());
         sp.addHandler(addHandler);
         try {
             sp.parse(param);
             sp.clearHandlerChain();
         }
         catch (final MissingElementValueException e) {
             throw e;
         }
         catch (final ReferencedResourceNotFoundException e) {
             throw e;
         }
         catch (final RelationPredicateNotFoundException e) {
             throw e;
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final InvalidXmlException e) {
             throw e;
         }
         catch (final SystemException e) {
             throw e;
         }
         catch (final AlreadyExistsException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         final List<Map<String, String>> relationsData = addHandler.getRelations();
 
         if (relationsData != null && !relationsData.isEmpty()) {
             final List<StartElementWithChildElements> elements = new ArrayList<StartElementWithChildElements>();
             for (final Map<String, String> relation : relationsData) {
                 final String predicateValue = relation.get("predicateValue");
                 final String predicateNs = relation.get("predicateNs");
                 final String target = relation.get("target");
                 final StartElementWithChildElements newContentRelationElement = new StartElementWithChildElements();
                 newContentRelationElement.setLocalName(predicateValue);
                 newContentRelationElement.setPrefix(Constants.CONTENT_RELATIONS_NS_PREFIX_IN_RELSEXT);
                 newContentRelationElement.setNamespace(predicateNs);
                 final Attribute resource =
                     new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX,
                         "info:fedora/" + target);
                 newContentRelationElement.addAttribute(resource);
                 // newComponentIdElement.setElementText(componentId);
                 newContentRelationElement.setChildrenElements(null);
 
                 elements.add(newContentRelationElement);
             }
             final byte[] relsExtNewBytes = Utility.updateRelsExt(elements, null, null, getItem(), null);
             getItem().setRelsExt(relsExtNewBytes);
 
             makeVersion("Item.addContentRelations");
             getItem().persist();
 
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final MissingMethodParameterException e) {
                 throw new SystemException(e);
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     @Override
     public String removeContentRelations(final String id, final String param) throws SystemException,
         ItemNotFoundException, OptimisticLockingException, InvalidStatusException, MissingElementValueException,
         ContentRelationNotFoundException, LockingException, ReadonlyViolationException, ReadonlyVersionException,
         ComponentNotFoundException, XmlCorruptedException {
 
         setItem(id);
         checkLatestVersion();
         final DateTime startTimestamp = getItem().getLastFedoraModificationDate();
         checkLocked();
         checkWithdrawn("Removing of content relations is not allowed.");
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + id);
 
         final StaxParser sp = new StaxParser();
 
         final ContentRelationsRemoveHandler2Edition removeHandler =
             new ContentRelationsRemoveHandler2Edition(sp, getItem().getId());
         sp.addHandler(removeHandler);
         try {
             sp.parse(param);
             sp.clearHandlerChain();
         }
         catch (final MissingElementValueException e) {
             throw new MissingElementValueException(e);
         }
         catch (final ContentRelationNotFoundException e) {
             throw new ContentRelationNotFoundException(e);
         }
         catch (final TripleStoreSystemException e) {
             throw new TripleStoreSystemException(e);
         }
         catch (final WebserverSystemException e) {
             throw new TripleStoreSystemException(e);
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
 
         final List<Map<String, String>> relationsData = removeHandler.getRelations();
         if (relationsData != null && !relationsData.isEmpty()) {
             final Map<String, List<StartElementWithChildElements>> toRemove =
                 new TreeMap<String, List<StartElementWithChildElements>>();
             final Iterator<Map<String, String>> iterator = relationsData.iterator();
             final HashMap<String, List<StartElementWithChildElements>> predicateValuesVectorAssignment =
                 new HashMap<String, List<StartElementWithChildElements>>();
             boolean resourceUpdated = false;
             while (iterator.hasNext()) {
                 resourceUpdated = true;
                 final Map<String, String> relation = iterator.next();
 
                 final String predicateValue = relation.get("predicateValue");
                 final String predicateNs = relation.get("predicateNs");
                 final String target = relation.get("target");
 
                 final StartElementWithChildElements newContentRelationElement = new StartElementWithChildElements();
                 newContentRelationElement.setLocalName(predicateValue);
                 newContentRelationElement.setPrefix(Constants.CONTENT_RELATIONS_NS_PREFIX_IN_RELSEXT);
                 newContentRelationElement.setNamespace(predicateNs);
                 final Attribute resource =
                     new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX,
                         "info:fedora/" + target);
                 newContentRelationElement.addAttribute(resource);
                 newContentRelationElement.setChildrenElements(null);
                 if (predicateValuesVectorAssignment.containsKey(predicateValue)) {
                     final List<StartElementWithChildElements> vector =
                         predicateValuesVectorAssignment.get(predicateValue);
                     vector.add(newContentRelationElement);
                 }
                 else {
                     final List<StartElementWithChildElements> vector = new ArrayList<StartElementWithChildElements>();
                     vector.add(newContentRelationElement);
                     predicateValuesVectorAssignment.put(predicateValue, vector);
                 }
 
             }
 
             final Set<Entry<String, List<StartElementWithChildElements>>> entrySet =
                 predicateValuesVectorAssignment.entrySet();
             for (final Entry<String, List<StartElementWithChildElements>> anEntrySet : entrySet) {
                 final String predicateValue = anEntrySet.getKey();
                 final List<StartElementWithChildElements> elements = anEntrySet.getValue();
                 toRemove.put("/RDF/Description/" + predicateValue, elements);
             }
 
             final byte[] relsExtNewBytes = Utility.updateRelsExt(null, toRemove, null, getItem(), null);
             getItem().setRelsExt(relsExtNewBytes);
 
             if (resourceUpdated || !startTimestamp.isEqual(getItem().getLastFedoraModificationDate())) {
                 makeVersion("Item.removeContentRelations");
                 getItem().persist();
             }
 
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
             catch (final MissingMethodParameterException e) {
                 throw new SystemException(e);
             }
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     /**
      * Lock an Item for other user access.
      */
     @Override
     public String lock(final String id, final String param) throws ItemNotFoundException, LockingException,
         InvalidContentException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         ComponentNotFoundException, InvalidStatusException, XmlCorruptedException {
 
         setItem(id);
         checkWithdrawn("No modification allowed.");
 
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         checkLocked();
 
         if (Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + getItem().getId())) {
             getItem().setLocked(true, Utility.getCurrentUser());
             // to lock/unlock is no modification of the object, don't update
             // timestamp
 
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     @Override
     public String unlock(final String id, final String param) throws ItemNotFoundException, LockingException,
         MissingMethodParameterException, SystemException, OptimisticLockingException, ComponentNotFoundException,
         XmlCorruptedException {
 
         setItem(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         // checked by AA
         // checkLocked();
 
         if (Utility.checkOptimisticLockingCriteria(getItem().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Item " + getItem().getId())) {
 
             getItem().setLocked(false, null);
             // to lock/unlock is no modification of the object, don't update
             // timestamp
 
             try {
                 fireItemModified(getItem().getId(), retrieve(getItem().getId()));
             }
             catch (final AuthorizationException e) {
                 throw new SystemException(e);
             }
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getItem().getLastModificationDate());
     }
 
     @Override
     public String retrieveVersionHistory(final String id) throws ItemNotFoundException, EncodingSystemException,
         IntegritySystemException, FedoraSystemException, WebserverSystemException, TripleStoreSystemException,
         XmlParserSystemException {
 
         setItem(id);
         final String versionsXml;
 
         try {
             // versionsXml =
             // getVersions().replaceFirst(
             // "xlink:type=\"simple\"",
             // "xml:base=\"" + XmlUtility.getEscidocBaseUrl()
             // + "\" xlink:type=\"simple\" ");
             versionsXml =
                 getVersions().replaceFirst(
                     '<' + Constants.WOV_NAMESPACE_PREFIX + ':' + Elements.ELEMENT_WOV_VERSION_HISTORY,
                     "<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + Constants.WOV_NAMESPACE_PREFIX + ':'
                         + Elements.ELEMENT_WOV_VERSION_HISTORY + " xml:base=\"" + XmlUtility.getEscidocBaseUrl()
                         + "\" " + Elements.ATTRIBUTE_LAST_MODIFICATION_DATE + "=\""
                         + getItem().getLastModificationDate() + "\" ");
         }
         catch (final StreamNotFoundException e) {
             throw new IntegritySystemException("Version history not found.", e);
         }
 
         return versionsXml;
     }
 
     /**
      * See Interface for functional description.
      *
      * @param id id
      * @throws ItemNotFoundException e
      * @throws SystemException       cf. Interface
      */
     @Override
     public String retrieveParents(final String id) throws ItemNotFoundException, SystemException {
         this.utility.checkIsItem(id);
         return renderParents(id);
     }
 
     /**
      * @param parameters
      * @return
      */
     @Override
     public String retrieveItems(final SRURequestParameters parameters) throws WebserverSystemException {
         final StringWriter result = new StringWriter();
 
         if (parameters.isExplain()) {
             sruRequest.explain(result, ResourceType.ITEM);
         }
         else {
             sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.ITEM }, parameters);
         }
         return result.toString();
     }
 
     @Override
     public String retrieveComponentProperties(final String id, final String componentId) throws ItemNotFoundException,
         ComponentNotFoundException, MissingMethodParameterException, SystemException, AuthorizationException {
 
         setItem(id);
         String originId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         final String originVersionId = getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN_VERSION);
         if (originVersionId != null) {
             originId = originId + ':' + originVersionId;
         }
         loadOrigin("You have no access rights on the item " + originId + " , which is reffered by a surrogate item "
             + id + ". Therefore you cannot access any subressources of this item.");
 
         return renderComponentProperties(componentId);
     }
 
     /**
      * @param id The id of the item.
      * @return TODO
      * @throws ItemNotFoundException TODO
      */
     public String retrieveRevisions(final String id) {
         // FIXME
         throw new UnsupportedOperationException();
     }
 
     /**
      * @param id        The id of the item.
      * @param taskParam Taskparam XML including the latest-modification-date.
      * @return TODO
      * @throws ItemNotFoundException TODO
      */
     @Override
     public String moveToContext(final String id, final String taskParam) throws ItemNotFoundException,
         ContextNotFoundException, InvalidContentException, LockingException, InvalidStatusException,
         MissingMethodParameterException, SystemException {
         // FIXME
         throw new UnsupportedOperationException();
     }
 
     /**
      * @param comment Optional comment to associate with the created version or event.
      * @throws SystemException If an error occures.
      */
     private void makeVersion(final String comment) throws SystemException {
         makeVersion(comment, null);
     }
 
     /**
      * Parses the item.
      *
      * @param xml the String containing the item xml
      * @return ItemCreate
      * @throws de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException
      * @throws de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingContentException
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidContentException
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingElementValueException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException
      * @throws de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     private static ItemCreate parseItem(final String xml) throws WebserverSystemException, XmlParserSystemException,
         ReadonlyElementViolationException, ReadonlyAttributeViolationException, ContentModelNotFoundException,
         ContextNotFoundException, MissingContentException, MissingAttributeValueException,
         MissingElementValueException, XmlCorruptedException, InvalidContentException,
         ReferencedResourceNotFoundException, InvalidStatusException, RelationPredicateNotFoundException,
         MissingMdRecordException, EncodingSystemException, TripleStoreSystemException, IntegritySystemException {
 
         final StaxParser sp = new StaxParser();
 
         final ItemHandler itemHandler = new ItemHandler(sp);
         sp.addHandler(itemHandler);
 
         try {
             sp.parse(xml);
         }
         catch (final LockingException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         catch (final OptimisticLockingException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         catch (final AlreadyExistsException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         catch (final OrganizationalUnitNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         catch (final ContentRelationNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         catch (final PidAlreadyAssignedException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         catch (final TmeException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
         catch (final XMLStreamException e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
 
         return itemHandler.getItem();
     }
 
     /**
      * @param comment   Optional comment to associate with the created version or event.
      * @param newStatus The status of the new version.
      * @throws SystemException If an error occures.
      */
     private void makeVersion(final String comment, final String newStatus) throws SystemException {
         getUtility().makeVersion(comment, newStatus, getItem());
     }
 
     /**
      * Sets the metadata datastream with name {@code name} for this item.
      *
      * @param name         The name of the metadata datastream.
      * @param xml          The datastream.
      * @param mdAttributes A Map containing the metadata datastreams type and schema.
      * @throws SystemException Thrown in case of an internal error.
      */
     private static void setMetadataRecord(final String name, final String xml, final Map<String, String> mdAttributes)
         throws SystemException {
         // this method must be reimplemented to use set-method in item
         throw new SystemException("Not yet implemented.");
     }
 
     /**
      * Creates Stream objects from the ByteArrayOutputStreams in {@code mdMap} and calls Item.setMdRecords with a
      * HashMap which contains the metadata datastreams as Stream objects.
      *
      * @param mdMap           A HashMap which contains the metadata datastreams as ByteArrayOutputStream.
      * @param mdAttributesMap A HashMap which contains the metadata attributes.
      * @param escidocMdRecordnsUri
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     private void setMetadataRecords(
         final Map<String, ByteArrayOutputStream> mdMap, final Map<String, Map<String, String>> mdAttributesMap,
         final String escidocMdRecordnsUri) throws TripleStoreSystemException, EncodingSystemException,
         IntegritySystemException, FedoraSystemException, WebserverSystemException {
         final Map<String, Datastream> dsMap = new HashMap<String, Datastream>();
         if (mdMap == null) {
             getItem().setMdRecords(dsMap);
         }
         else {
            for (final Entry<String, ByteArrayOutputStream> entry : mdMap.entrySet()) {
                final ByteArrayOutputStream stream = entry.getValue();
                 final byte[] xmlBytes = stream.toByteArray();
                 HashMap<String, String> mdProperties = null;
                if ("escidoc".equals(entry.getKey())) {
                     mdProperties = new HashMap<String, String>();
                     mdProperties.put("nsUri", escidocMdRecordnsUri);
 
                 }
                 final Datastream ds =
                    new Datastream(entry.getKey(), getItem().getId(), xmlBytes, MimeTypes.TEXT_XML, mdProperties);
                final Map<String, String> mdRecordAttributes = mdAttributesMap.get(entry.getKey());
                 ds.addAlternateId(Datastream.METADATA_ALTERNATE_ID);
                 ds.addAlternateId(mdRecordAttributes.get("type"));
                 ds.addAlternateId(mdRecordAttributes.get("schema"));
                dsMap.put(entry.getKey(), ds);
             }
             getItem().setMdRecords(dsMap);
         }
     }
 
     /**
      * Creates Stream objects from the values in {@code contentStreamMap} and calls Item.setContentStreams with a
      * HashMap which contains the metadata datastreams as Stream objects.
      *
      * @param contentStreamMap A HashMap which contains the metadata datastreams as ByteArrayOutputStream.
      * @throws IntegritySystemException e
      * @throws WebserverSystemException e
      * @throws FedoraSystemException    e
      */
     @Deprecated
     private void setContentStreams(final Map<String, Map<String, Object>> contentStreamMap)
         throws FedoraSystemException, IntegritySystemException {
         final Map<String, Datastream> contentStreamDatastreams = new HashMap<String, Datastream>();
 
         for (final Entry<String, Map<String, Object>> stringMapEntry : contentStreamMap.entrySet()) {
             final Map<String, Object> csValues = stringMapEntry.getValue();
             final Datastream ds;
             if (csValues.containsKey(Elements.ELEMENT_CONTENT)) {
                 final ByteArrayOutputStream stream = (ByteArrayOutputStream) csValues.get(Elements.ELEMENT_CONTENT);
                 final byte[] xmlBytes = stream.toByteArray();
                 ds =
                     new Datastream(stringMapEntry.getKey(), getItem().getId(), xmlBytes, (String) csValues
                         .get(Elements.ATTRIBUTE_CONTENT_STREAM_MIME_TYPE));
             }
             else if (csValues.containsKey(Elements.ATTRIBUTE_XLINK_HREF)) {
                 ds =
                     new Datastream(stringMapEntry.getKey(), getItem().getId(), (String) csValues
                         .get(Elements.ATTRIBUTE_XLINK_HREF), (String) csValues.get(Elements.ATTRIBUTE_STORAGE),
                         (String) csValues.get(Elements.ATTRIBUTE_CONTENT_STREAM_MIME_TYPE));
             }
             else {
                 throw new IntegritySystemException("Content streams has neither href nor content.");
             }
             String title = (String) csValues.get(Elements.ATTRIBUTE_XLINK_TITLE);
             if (title == null) {
                 title = "";
             }
             ds.setLabel(title.trim());
             contentStreamDatastreams.put(stringMapEntry.getKey(), ds);
         }
 
         getItem().setContentStreams(contentStreamDatastreams);
     }
 
     /**
      * Validate the Item.
      * <p/>
      * Checks if all required values are set and consistent.
      *
      * @param item The item which is to validate.
      * @throws InvalidStatusException         Thrown if Item has invalid status.
      * @throws MissingMdRecordException       Thrown if required md-record is missing.
      * @throws ReferencedResourceNotFoundException
      *                                        Thrown if reference to another resource could not be resolved as valid
      *                                        resoure.
      * @throws InvalidContentException        Thrown if content is invalid.
      * @throws RelationPredicateNotFoundException
      *                                        Thrown if the predicate of the relation was not found within the
      *                                        ontologie.
      * @throws AuthorizationException         Thrown if permissions are restricted on origin Item.
      * @throws MissingAttributeValueException Thrown if required attribute is missing.
      * @throws XmlCorruptedException          e
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     private void validateCreate(final ItemCreate item) throws InvalidStatusException, MissingMdRecordException,
         InvalidContentException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException,
         AuthorizationException, TripleStoreSystemException, WebserverSystemException, IntegritySystemException,
         FedoraSystemException {
 
         /*
          * Distinguish between create and ingest in the business level and not
          * in the parser leads to this circumstance where values (like
          * public-status), which where ignored before by parser, has now to be
          * checked in a higher level.
          * 
          * Maybe there exist different ways to avoid these additional (method
          * depending) check use either separate parser (but these would be
          * mostly identical). Or enhance the XML schema and use different XML
          * schema for ingest and create (seems for me just now, SWA, a elegant
          * solution).
          */
         validateOriginItem(item);
         validate(item);
 
         // check status public status of Item
         final StatusType publicStatus = item.getProperties().getObjectProperties().getStatus();
 
         if (publicStatus != StatusType.PENDING) {
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("New Items has to be in public-status '" + StatusType.PENDING + "'.");
             }
             item.getProperties().getObjectProperties().setStatus(StatusType.PENDING);
         }
     }
 
     /**
      * Validate the Item.
      * <p/>
      * Checks if all required values are set and consistent.
      *
      * @param item The item which is to validate.
      * @throws InvalidStatusException         Thrown if Item has invalid status.
      * @throws MissingMdRecordException       Thrown if required md-record is missing.
      * @throws ReferencedResourceNotFoundException
      *                                        Thrown if reference to another resource could not be resolved as valid
      *                                        resoure.
      * @throws InvalidContentException        Thrown if content is invalid.
      * @throws RelationPredicateNotFoundException
      *                                        Thrown if the predicate of the relation was not found within the
      *                                        ontologie.
      * @throws MissingAttributeValueException Thrown if name Attribute is not unique or no Metadata Record with name
      *                                        "escidoc" exists.
      * @throws XmlParserSystemException       Thrown if XML parser throws exception.
      * @throws EncodingSystemException        Thrown if encoding is invalid.
      * @throws TripleStoreSystemException     Thrown if access to TripleStore failed.
      * @throws WebserverSystemException       Thrown in case of internal error.
      */
     private void validateIngest(final ItemCreate item) throws InvalidStatusException, TripleStoreSystemException,
         WebserverSystemException, MissingMdRecordException, InvalidContentException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException {
 
         /*
          * see validateCreate
          */
         validate(item);
 
         // check status public status of Item
         final StatusType publicStatus = item.getProperties().getObjectProperties().getStatus();
         if (publicStatus == StatusType.RELEASED) {
             // check if we need a PID if the release an Item and if the PID is
             // given.
             if (!Boolean.valueOf(System.getProperty("cmm.Item.objectPid.releaseWithoutPid"))
 
             && item.getProperties().getObjectProperties().getPid() == null) {
                 throw new InvalidStatusException("Item with public-status released requires an PID.");
             }
             item.getProperties().getCurrentVersion().setStatus(StatusType.RELEASED);
             item.getProperties().setLatestReleasedVersion(item.getProperties().getCurrentVersion());
         }
         else if (publicStatus != StatusType.PENDING) {
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("New Items has to be in public-status '" + StatusType.PENDING + "' or '"
                     + StatusType.RELEASED);
             }
             item.getProperties().getObjectProperties().setStatus(StatusType.PENDING);
             item.getProperties().getCurrentVersion().setStatus(StatusType.PENDING);
         }
     }
 
     /**
      * Validate the Item structure in general (independent if create or ingest was selected).
      * <p/>
      * Checks if all required values are set and consistent.
      *
      * @param item The item which is to validate.
      * @throws InvalidStatusException         Thrown if Item has invalid status.
      * @throws MissingMdRecordException       Thrown if required md-record is missing.
      * @throws ReferencedResourceNotFoundException
      *                                        Thrown if reference to another resource could not be resolved as valid
      *                                        resoure.
      * @throws InvalidContentException        Thrown if content is invalid.
      * @throws RelationPredicateNotFoundException
      *                                        Thrown if the predicate of the relation was not found within the
      *                                        ontologie.
      * @throws MissingAttributeValueException Thrown if name Attribute is not unique or no Metadata Record with name
      *                                        "escidoc" exists.
      * @throws XmlParserSystemException       Thrown if XML parser throws exception.
      * @throws EncodingSystemException        Thrown if encoding is invalid.
      * @throws TripleStoreSystemException     Thrown if access to TripleStore failed.
      * @throws WebserverSystemException       Thrown in case of internal error.
      * @throws XmlCorruptedException          e
      */
     private void validate(final ItemCreate item) throws InvalidStatusException, TripleStoreSystemException,
         WebserverSystemException, MissingMdRecordException, InvalidContentException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException {
 
         checkContextStatus(item.getProperties().getObjectProperties().getContextId(), Constants.STATUS_CONTEXT_OPENED);
 
         // validate Content Relations
         final Iterator<RelationCreate> it = item.getRelations().iterator();
         if (it != null) {
             while (it.hasNext()) {
                 final RelationCreate relation = it.next();
                 checkRefElement(relation.getTarget());
                 if (!ContentRelationsUtility.validPredicate(relation.getPredicateNs() + '#' + relation.getPredicate())) {
                     throw new RelationPredicateNotFoundException("Predicate '" + relation.getPredicate()
                         + "' is invalid. ");
                 }
             }
         }
 
         // validate Metadata Records
         checkMetadataRecords(item);
     }
 
     /**
      * Validate permissions and status of origin Item (surrogate Item concept).
      *
      * @param item The surrogate Item.
      * @throws InvalidContentException Thrown if content is invalid.
      * @throws InvalidStatusException  Thrown if status of origin Item in invalid.
      * @throws AuthorizationException  Thrown if access to origin Item is denied.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     private void validateOriginItem(final ItemCreate item) throws InvalidContentException, InvalidStatusException,
         AuthorizationException, TripleStoreSystemException, WebserverSystemException, IntegritySystemException,
         FedoraSystemException {
 
         String origin = item.getProperties().getObjectProperties().getOrigin();
         if (origin != null) {
             final String objid = item.getProperties().getObjectProperties().getOriginObjectId();
             final String versionNumber = item.getProperties().getObjectProperties().getOriginVersionId();
             // in a case of a floating reference to the origin item
             // ensure, that a latest release and not a latest version
             // of the origin item will be fetched.
             final String publicStatus =
                 this.tripleStoreUtility.getPropertiesElements(objid, TripleStoreUtility.PROP_PUBLIC_STATUS);
             if (publicStatus == null) {
                 throw new InvalidContentException("A referenced Item '" + origin + "' does not exist.");
             }
             else if (publicStatus.equals(Constants.STATUS_WITHDRAWN)) {
                 throw new InvalidStatusException("The referenced Item '" + origin
                     + "' is in status 'withdrawn'. The surrogate Item can " + "not be created.");
             }
 
             final String latestReleaseNumber =
                 this.tripleStoreUtility.getPropertiesElements(objid, TripleStoreUtility.PROP_LATEST_RELEASE_NUMBER);
             if (latestReleaseNumber == null) {
                 throw new InvalidStatusException("The referenced Item with id '" + origin + "' is not released.");
             }
             if (versionNumber == null) {
                 origin = objid + ':' + latestReleaseNumber;
             }
 
             if (!checkUserRights(origin)) {
                 throw new AuthorizationException("You can not create a surrogate Item based " + "on the Item '"
                     + origin + "' because you have no access " + "rights on this Item.");
             }
             try {
                 setOriginItem(origin);
             }
             catch (final ItemNotFoundException e) {
                 // Ignore FindBugs
                 throw new InvalidContentException("The referenced Item '" + origin + "' does not exist.");
             }
 
             if (getOriginItem().getResourceProperties().get(PropertyMapKeys.ORIGIN) != null) {
                 throw new InvalidContentException("A referenced original Item should be "
                     + "a regular Item, not a surrogate Item.");
             }
             final String versionStatus =
                 getOriginItem().getResourceProperties().get(PropertyMapKeys.CURRENT_VERSION_STATUS);
             if (!versionStatus.equals(Constants.STATUS_RELEASED)) {
                 throw new InvalidStatusException("The referenced Item version is not released. "
                     + "You can create a surrogate Item only based on a " + "released Item version.");
             }
         }
 
     }
 
     /**
      * Check if the name attribute of Metadata Records is unique and at least one Metadata Record has the name
      * "escidoc".
      *
      * @param item Item which is to validate.
      * @throws MissingMdRecordException Thrown if the mandatory MdRecortd with name "escidoc" is missing.
      * @throws InvalidContentException  Thrown if content is invalid.
      */
     private static void checkMetadataRecords(final ItemCreate item) throws MissingMdRecordException,
         InvalidContentException {
 
         /*
          * TODO move Item validation to seperate validation class.
          */
         final List<MdRecordCreate> mdRecords = item.getMetadataRecords();
 
         // check if md-record with name 'escidoc'
 
         if (mdRecords == null || mdRecords.size() < 1) {
             if (item.getProperties().getObjectProperties().getOrigin() == null) {
                 throw new MissingMdRecordException("The Item representation doesn't contain a "
                     + "mandatory md-record. A regular Item must contain a " + "mandatory md-record.");
             }
 
         }
         else {
 
             final Collection<String> mdRecordNames = new ArrayList<String>();
             for (final MdRecordCreate mdRecord : mdRecords) {
 
                 final String name = mdRecord.getName();
 
                 // check uniqueness of names
                 if (mdRecordNames.contains(name)) {
                     throw new InvalidContentException("Metadata 'md-record' with name='" + name
                         + "' exists multiple times.");
                 }
 
                 mdRecordNames.add(name);
             }
             if (!mdRecordNames.contains(Elements.MANDATORY_MD_RECORD_NAME)
                 && item.getProperties().getObjectProperties().getOrigin() == null) {
                 throw new MissingMdRecordException("The item representation doesn't contain a "
                     + "mandatory md-record. A regular item must contain a " + "mandatory md-record. ");
             }
 
         }
     }
 
     /**
      * @param targetId Objid of reference target.
      * @throws InvalidContentException    Thrown if reference points to an not supported Resource (not to Item or
      *                                    Container)
      * @throws TripleStoreSystemException Thrown if requesting type of resource failed.
      * @throws ReferencedResourceNotFoundException
      *                                    Thrown if no resource under provided id exists.
      * @throws WebserverSystemException   Thrown if creating instance of TripleStoreUtility failed.
      */
     private void checkRefElement(final String targetId) throws InvalidContentException, TripleStoreSystemException,
         ReferencedResourceNotFoundException {
 
         final String targetObjectType = this.tripleStoreUtility.getObjectType(targetId);
         if (targetObjectType == null) {
             throw new ReferencedResourceNotFoundException("Resource with id '" + targetId + "' does not exist.");
         }
         if (!Constants.ITEM_OBJECT_TYPE.equals(targetObjectType)
             && !Constants.CONTAINER_OBJECT_TYPE.equals(targetObjectType)) {
             throw new InvalidContentException("A related resource '" + targetId
                 + "' is neither 'Item' nor 'Container' ");
         }
     }
 
 }
