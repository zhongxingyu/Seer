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
 package de.escidoc.core.om.business.fedora.container;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.annotation.PostConstruct;
 import javax.xml.stream.XMLStreamException;
 
 import org.escidoc.core.services.fedora.AddDatastreamPathParam;
 import org.escidoc.core.services.fedora.AddDatastreamQueryParam;
 import org.escidoc.core.services.fedora.ControlGroup;
 import org.escidoc.core.services.fedora.FedoraServiceClient;
 import org.escidoc.core.services.fedora.IngestPathParam;
 import org.escidoc.core.services.fedora.IngestQueryParam;
 import org.escidoc.core.services.fedora.ModifiyDatastreamPathParam;
 import org.escidoc.core.services.fedora.ModifyDatastreamQueryParam;
 import org.escidoc.core.services.fedora.management.DatastreamProfileTO;
 import org.esidoc.core.utils.io.EscidocBinaryContent;
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
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.Container;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.business.fedora.resources.StatusType;
 import de.escidoc.core.common.business.filter.LuceneRequestParameters;
 import de.escidoc.core.common.business.filter.SRURequest;
 import de.escidoc.core.common.business.filter.SRURequestParameters;
 import de.escidoc.core.common.business.indexing.IndexingHandler;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidItemStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.TmeException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
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
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyWithdrawnException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.servlet.invocation.BeanMethod;
 import de.escidoc.core.common.servlet.invocation.MethodMapper;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.MultipleExtractor;
 import de.escidoc.core.common.util.stax.handler.OptimisticLockingHandler;
 import de.escidoc.core.common.util.stax.handler.TaskParamHandler;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProviderConstants;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithChildElements;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 import de.escidoc.core.om.business.fedora.contentRelation.FedoraContentRelationHandler;
 import de.escidoc.core.om.business.fedora.item.FedoraItemHandler;
 import de.escidoc.core.om.business.interfaces.ContainerHandlerInterface;
 import de.escidoc.core.om.business.security.UserFilter;
 import de.escidoc.core.om.business.stax.handler.ContentRelationsAddHandler2Edition;
 import de.escidoc.core.om.business.stax.handler.ContentRelationsCreateHandler2Edition;
 import de.escidoc.core.om.business.stax.handler.ContentRelationsRemoveHandler2Edition;
 import de.escidoc.core.om.business.stax.handler.ContentRelationsUpdateHandler2Edition;
 import de.escidoc.core.om.business.stax.handler.MdRecordsUpdateHandler;
 import de.escidoc.core.om.business.stax.handler.MetadataHandler;
 import de.escidoc.core.om.business.stax.handler.container.BuildRelsExtMemberEntriesFromTaskParamHandlerNew;
 import de.escidoc.core.om.business.stax.handler.container.ContainerPropertiesHandler;
 import de.escidoc.core.om.business.stax.handler.container.StructMapCreateHandler;
 
 /**
  * The retrieve, update, create and delete methods implement the {@link ContainerHandlerInterface
  * ContainerHandlerInterface}. These methods handle strings of xmlData and use the private (get,) set and render methods
  * to set xmlData in the system or get xmlData from the system.
  * <p/>
  * The private set methods take strings of xmlData as parameter and handling objects of type {@link Datastream Stream}
  * that hold the xmlData in Container object.
  * <p/>
  * To split incoming xmlData into the datastreams it consists of, the {@link StaxParser StaxParser} is used. In order to
  * modify datastreams or handle values provided in datastreams more than one Handler (implementations of
  * {@link DefaultHandler DefaultHandler}) can be added to the StaxParser. The {@link MultipleExtractor
  * MultipleExtractor} have to be the last Handler in the HandlerChain of a StaxParser.
  * 
  * @author Rozita Friedman
  */
 @Service("business.FedoraContainerHandler")
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class FedoraContainerHandler extends ContainerHandlerPid implements ContainerHandlerInterface {
 
     /*
      * Attention: The spring/beans setter methods has to be defined in this and not in one of the super classes.
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(FedoraContainerHandler.class);
 
     private static final String MODIFIED_DATE_ATT_NAME = Elements.ATTRIBUTE_LAST_MODIFICATION_DATE;
 
     private static final String COLLECTION = "collection";
 
     @Autowired
     @Qualifier("business.FedoraItemHandler")
     private FedoraItemHandler itemHandler;
 
     @Autowired
     private FedoraContentRelationHandler contentRelationHandler;
 
     @Autowired
     @Qualifier("common.CommonMethodMapper")
     private MethodMapper methodMapper;
 
     @Autowired
     @Qualifier("common.business.indexing.IndexingHandler")
     private IndexingHandler indexingHandler;
 
     @Autowired
     @Qualifier("de.escidoc.core.common.business.filter.SRURequest")
     private SRURequest sruRequest;
 
     @Autowired
     @Qualifier("business.Utility")
     private Utility utility;
 
     @Autowired
     private FedoraServiceClient fedoraServiceClient;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected FedoraContainerHandler() {
     }
 
     /**
      * Create a container.
      * 
      * @param xmlData
      *            The XML Container representation.
      * @return The XML representation of created Container.
      * @throws ContextNotFoundException
      *             e
      * @throws ContentModelNotFoundException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws MissingElementValueException
      *             e
      * @throws SystemException
      *             e
      * @throws ReferencedResourceNotFoundException
      *             e
      * @throws RelationPredicateNotFoundException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws MissingMdRecordException
      *             e
      */
     @Override
     public String create(final String xmlData) throws ContextNotFoundException, ContentModelNotFoundException,
         InvalidContentException, MissingMethodParameterException, XmlCorruptedException,
         MissingAttributeValueException, MissingElementValueException, SystemException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException, InvalidStatusException,
         MissingMdRecordException {
         return doCreate(xmlData, true);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.common.business.interfaces.IngestableResource#ingest( java.lang.String)
      */
     @Override
     public String ingest(final String xmlData) throws ContentModelNotFoundException, ContextNotFoundException,
         MissingAttributeValueException, MissingElementValueException, XmlCorruptedException, InvalidContentException,
         ReferencedResourceNotFoundException, InvalidStatusException, RelationPredicateNotFoundException,
         MissingMdRecordException, MissingMethodParameterException, SystemException, XmlSchemaValidationException {
 
         return doCreate(xmlData, false);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param xmlData
      *            The XML Container representation.
      * @param isCreate
      *            Set true if Container is created, Set false if Container is ingested (Ingest interface).
      * @return The XML representation of created Container.
      * @throws RelationPredicateNotFoundException
      *             cf. Interface
      * @see ContainerHandlerInterface#create(String)
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingElementValueException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
      * @throws de.escidoc.core.common.exceptions.system.SystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidContentException
      */
     private String doCreate(final String xmlData, final boolean isCreate) throws SystemException,
         ContentModelNotFoundException, ContextNotFoundException, MissingAttributeValueException,
         MissingElementValueException, XmlCorruptedException, InvalidContentException,
         ReferencedResourceNotFoundException, InvalidStatusException, RelationPredicateNotFoundException,
         MissingMdRecordException, MissingMethodParameterException {
 
         final String[] creator = Utility.getCurrentUser();
         final String containerId = getIdProvider().getNextPid();
         final String createComment = "Object " + containerId + " created.";
 
         final StaxParser staxParser = new StaxParser();
         final List<DefaultHandler> handlerChain = new ArrayList<DefaultHandler>();
         final ContainerPropertiesHandler propertiesHandler = new ContainerPropertiesHandler(staxParser);
 
         handlerChain.add(propertiesHandler);
         final MetadataHandler metadataHandler = new MetadataHandler(staxParser);
         handlerChain.add(metadataHandler);
         final StructMapCreateHandler structMapHandler = new StructMapCreateHandler(staxParser);
         handlerChain.add(structMapHandler);
         final ContentRelationsCreateHandler2Edition crch = new ContentRelationsCreateHandler2Edition(staxParser);
         handlerChain.add(crch);
         final HashMap<String, String> extractPathes = new HashMap<String, String>();
         extractPathes.put("/container/md-records/md-record", "name");
         extractPathes.put("/container/properties/" + Elements.ELEMENT_CONTENT_MODEL_SPECIFIC, null);
 
         final MultipleExtractor multipleExtractor = new MultipleExtractor(extractPathes, staxParser);
         multipleExtractor.setPids(null);
         handlerChain.add(multipleExtractor);
         staxParser.setHandlerChain(handlerChain);
         try {
             staxParser.parse(xmlData);
         }
         catch (final MissingContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (final LockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final OptimisticLockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final AlreadyExistsException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final OrganizationalUnitNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContentRelationNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final PidAlreadyAssignedException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyElementViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyAttributeViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final TmeException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         // reset StaxParser
         staxParser.clearHandlerChain();
         final Map<String, Object> streams = multipleExtractor.getOutputStreams();
 
         final List<Map<String, String>> relationsData = crch.getContentRelationsData();
         streams.put("version-history", "<versions/>");
 
         final Map<String, String> properties = propertiesHandler.getProperties();
         final String contextId = properties.remove(Elements.ELEMENT_CONTEXT);
 
         checkContextStatus(contextId, Constants.STATUS_CONTEXT_OPENED);
 
         final String contentModel = properties.remove(Elements.ELEMENT_CONTENT_MODEL);
         final Map<String, String> propertiesAsReferences = new HashMap<String, String>();
         propertiesAsReferences.put(Elements.ELEMENT_CREATED_BY, creator[0]);
         propertiesAsReferences.put(Elements.ELEMENT_MODIFIED_BY, creator[0]);
         propertiesAsReferences.put(Elements.ELEMENT_CONTEXT, contextId);
         propertiesAsReferences.put(Elements.ELEMENT_CONTENT_MODEL, contentModel);
         // now properties.name will be store not in a rels-ext datastream, but
         // in dc datastream as dc.title as result of a mapping escidoc->dc
         // String name = "Name";
         // properties.put("name", name);
         // properties.put(Elements.ELEMENT_CREATED_BY_TITLE, creator[1]);
         if (isCreate) {
             properties.put(Elements.ELEMENT_PUBLIC_STATUS, StatusType.PENDING.toString());
         }
         else {
             // set public-status to release is allowed for ingest (but not
             // create)
             final String publicStatus = properties.get(Elements.ELEMENT_PUBLIC_STATUS);
 
             if (publicStatus != null && publicStatus.equals(StatusType.RELEASED.toString())) {
 
                 if (!Boolean.valueOf(System.getProperty("cmm.Container.objectPid.releaseWithoutPid"))
                     && properties.get(Elements.ELEMENT_PID) == null) {
                     throw new InvalidStatusException("Missing object PID for public-status 'released'.");
                 }
             }
             else {
                 // ignore other status than released
                 properties.put(Elements.ELEMENT_PUBLIC_STATUS, StatusType.PENDING.toString());
             }
         }
 
         properties.put(Elements.ELEMENT_PUBLIC_STATUS_COMMENT, createComment);
         final List<String> structMapEntries = structMapHandler.getEntries();
         final String foxml =
             getContainerFoxml(streams, metadataHandler, containerId, contentModel, properties, structMapEntries,
                 XmlTemplateProviderConstants.PLACEHOLDER, relationsData, createComment, propertiesAsReferences);
         final IngestPathParam path = new IngestPathParam();
         final IngestQueryParam query = new IngestQueryParam();
         this.fedoraServiceClient.ingest(path, query, foxml);
         this.fedoraServiceClient.sync();
 
         final String escidocRelsExtWithWrongLmd =
             getFoxmlContainerRenderer().renderRelsExt(properties, structMapEntries, containerId, "bla", relationsData,
                 createComment, propertiesAsReferences);
 
         final AddDatastreamPathParam addPath = new AddDatastreamPathParam(containerId, "ESCIDOC_RELS_EXT");
         final AddDatastreamQueryParam addQuery = new AddDatastreamQueryParam();
         addQuery.setDsLabel("ESCIDOC_RELS_EXT Stream");
         addQuery.setVersionable(Boolean.TRUE);
         addQuery.setControlGroup(ControlGroup.M);
         final Stream addStream = new Stream();
         try {
             addStream.write(escidocRelsExtWithWrongLmd.getBytes(XmlUtility.CHARACTER_ENCODING));
             addStream.lock();
         }
         catch (final IOException e) {
             throw new WebserverSystemException(e);
         }
 
         final DatastreamProfileTO profile = getFedoraServiceClient().addDatastream(addPath, addQuery, addStream);
         final String lastModifiedDate = profile.getDsCreateDate().toString();
 
         // End of the work around
         final String wov =
             getFoxmlContainerRenderer().renderWov(containerId, "Version 1", "1", lastModifiedDate,
                 Constants.STATUS_PENDING, createComment);
 
         final AddDatastreamPathParam addPathVH = new AddDatastreamPathParam(containerId, "version-history");
         final AddDatastreamQueryParam addQueryVH = new AddDatastreamQueryParam();
         addQueryVH.setDsLabel("whole object versioning datastream");
         addQueryVH.setVersionable(Boolean.FALSE);
         addQueryVH.setControlGroup(ControlGroup.M);
         final Stream addStreamVH = new Stream();
         try {
             addStreamVH.write(wov.getBytes(XmlUtility.CHARACTER_ENCODING));
             addStreamVH.lock();
         }
         catch (final UnsupportedEncodingException e) {
             throw new WebserverSystemException(e);
         }
         catch (final IOException e) {
             throw new WebserverSystemException(e);
         }
         getFedoraServiceClient().addDatastream(addPathVH, addQueryVH, addStreamVH);
 
         // update time stamp within RELS-EXT and create a new
         // data stream ESCIDOC_RELS_EXT with a managed Control Group
         // with the same content as RELS-EXT
         final String relsExtNew =
             getFoxmlContainerRenderer().renderRelsExt(properties, structMapEntries, containerId, lastModifiedDate,
                 relationsData, createComment, propertiesAsReferences);
         final ModifiyDatastreamPathParam modifyPath =
             new ModifiyDatastreamPathParam(containerId, Datastream.RELS_EXT_DATASTREAM);
         final ModifyDatastreamQueryParam modifyQuery = new ModifyDatastreamQueryParam();
         modifyQuery.setDsLabel(Datastream.RELS_EXT_DATASTREAM_LABEL);
         final Stream stream = new Stream();
         try {
             stream.write(relsExtNew.getBytes(XmlUtility.CHARACTER_ENCODING));
             stream.lock();
         }
         catch (final IOException e) {
             throw new WebserverSystemException(e);
         }
         this.fedoraServiceClient.modifyDatastream(modifyPath, modifyQuery, stream);
         this.fedoraServiceClient.sync();
         try {
             this.tripleStoreUtility.reinitialize();
         }
         catch (TripleStoreSystemException e) {
             throw new FedoraSystemException("Error on reinitializing triple store.", e);
         }
         String result = null;
 
         if (isCreate) {
             try {
                 result = retrieve(containerId);
             }
             catch (final ResourceNotFoundException e) {
                 throw new IntegritySystemException("Newly created Container not available.", e);
             }
             fireContainerCreated(getContainer().getId(), result);
             // Also reindex members
             for (final String memberId : structMapEntries) {
                 fireContainerMembersModified(memberId);
             }
         }
         else {
             result = containerId;
         }
         return result;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            The ID of the container to delete.
      * @throws LockingException
      *             If container is locked.
      * @throws InvalidStatusException
      *             If container is in invalid status for deletion.
      * @throws AuthorizationException
      *             If further calls fail because of insufficient access rights.
      * @see de.escidoc.core.om.service.interfaces.ContainerHandlerInterface #delete(java.lang.String)
      */
     @Override
     public void delete(final String id) throws ContainerNotFoundException, LockingException, InvalidStatusException,
         SystemException, AuthorizationException {
 
         setContainer(id);
 
         checkLocked();
 
         final String status = getContainer().getProperty(PropertyMapKeys.PUBLIC_STATUS);
         if (!status.equals(Constants.STATUS_PENDING) && !status.equals(Constants.STATUS_IN_REVISION)) {
             throw new InvalidStatusException("Container " + getContainer().getId() + " is in status " + status
                 + ". Cannot delete.");
         }
 
         // check have (existing) members; they may be removed by direct purges
         // to Fedora or some incorrect behavior
         final List<String> memberIds = this.tripleStoreUtility.getMemberList(getContainer().getId(), null);
         for (final String memberId : memberIds) {
             if (this.tripleStoreUtility.exists(memberId)) {
                 throw new InvalidStatusException("Container " + getContainer().getId() + " has members.");
             }
         }
 
         // remove member entries referring this
         final List<String> containers = this.tripleStoreUtility.getContainers(getContainer().getId());
         for (final String parent : containers) {
             try {
                 final Container container = new Container(parent);
                 // call removeMember with current user context (access rights)
                 final String param =
                     "<param last-modification-date=\"" + container.getLastModificationDate() + "\"><id>"
                         + getContainer().getId() + "</id></param>";
                 final BeanMethod method =
                     methodMapper.getMethod("/ir/container/" + parent + "/members/remove", null, null, "POST", param);
                 method.invokeWithProtocol(UserContext.getHandle());
             }
             catch (final InvocationTargetException e) {
                 final Throwable cause = e.getCause();
                 if (cause instanceof Error) {
                     throw (Error) cause;
                 }
                 else if (cause instanceof AuthorizationException) {
                     throw (AuthorizationException) cause;
                 }
                 else {
                     throw new SystemException("An error occured removing member entries for container "
                         + getItem().getId() + ". Container can not be deleted.", cause); // Ignore
                     // FindBugs
                 }
             }
             catch (final Exception e) {
                 throw new SystemException("An error occured removing member entries for container " + getItem().getId()
                     + ". Container can not be deleted.", e);
             }
         }
 
         // purge container
         this.fedoraServiceClient.deleteObject(getContainer().getId());
         this.fedoraServiceClient.sync();
         try {
             this.tripleStoreUtility.reinitialize();
         }
         catch (TripleStoreSystemException e) {
             throw new FedoraSystemException("Error on reinitializing triple store.", e);
         }
         fireContainerDeleted(getContainer().getId());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.om.service.interfaces.ContainerHandlerInterface #retrieve(java.lang.String)
      */
     @Override
     public String retrieve(final String id) throws ContainerNotFoundException, SystemException,
         MissingMethodParameterException {
 
         setContainer(id);
         return getContainerXml(this.getContainer());
     }
 
     /**
      * Sets content model specific properties datastream of the container.
      * 
      * @param xml
      *            The xml representation of the content model specific properties.
      * @throws FedoraSystemException
      *             If Fedora reports an error.
      * @throws WebserverSystemException
      *             In case of an internal error.
      * @throws TripleStoreSystemException
      *             If triple store reports an error.
      * @throws EncodingSystemException
      *             If encoding fails.
      * @throws StreamNotFoundException
      *             TODO
      */
     private void setCts(final String xml) throws StreamNotFoundException, FedoraSystemException,
         WebserverSystemException, EncodingSystemException {
 
         final Datastream oldDs = getContainer().getCts();
 
         try {
             final Datastream newDs =
                 new Datastream(Elements.ELEMENT_CONTENT_MODEL_SPECIFIC, getContainer().getId(), xml
                     .getBytes(XmlUtility.CHARACTER_ENCODING), MimeTypes.TEXT_XML);
 
             if (oldDs == null || !oldDs.equals(newDs)) {
                 // TODO check if update is allowed
                 getContainer().setCts(newDs);
             }
         }
         catch (final UnsupportedEncodingException e) {
             throw new EncodingSystemException(e.getMessage(), e);
         }
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param xmlData
      *            xmlData
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws InvalidXmlException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws SystemException
      *             e
      * @throws ReferencedResourceNotFoundException
      *             e
      * @throws RelationPredicateNotFoundException
      *             e
      * @throws ReadonlyVersionException
      *             cf. Interface
      * @throws MissingAttributeValueException
      *             e
      * @throws MissingMdRecordException
      *             e
      */
     @Override
     public String update(final String id, final String xmlData) throws ContainerNotFoundException, LockingException,
         InvalidContentException, MissingMethodParameterException, InvalidXmlException, OptimisticLockingException,
         InvalidStatusException, MissingAttributeValueException, SystemException, ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, ReadonlyVersionException, MissingMdRecordException {
 
         setContainer(id);
 
         checkStatusNot(Constants.STATUS_WITHDRAWN);
         checkLocked();
         checkLatestVersion();
 
         try {
 
             final DateTime startTimestamp = getContainer().getLastFedoraModificationDate();
 
             final StaxParser sp = new StaxParser();
 
             // TODO get version properties
             // Map<String, String> versionData =
             // getContainer().getLastVersionData();
 
             sp.addHandler(new OptimisticLockingHandler(getContainer().getId(), Constants.CONTAINER_OBJECT_TYPE,
                 getContainer().getLastModificationDate()));
 
             final ContentRelationsUpdateHandler2Edition cruh = new ContentRelationsUpdateHandler2Edition(sp);
             sp.addHandler(cruh);
             final MdRecordsUpdateHandler mdHandler = new MdRecordsUpdateHandler("/container/md-records", sp);
             sp.addHandler(mdHandler);
 
             final HashMap<String, String> extractPathes = new HashMap<String, String>();
             extractPathes.put("/container/properties/" + Elements.ELEMENT_CONTENT_MODEL_SPECIFIC + "", null);
             extractPathes.put("/container/relations", null);
             extractPathes.put("/container/md-records/md-record", "name");
 
             final MultipleExtractor me = new MultipleExtractor(extractPathes, sp);
             me.setPids(null);
             sp.addHandler(me);
 
             try {
                 sp.parse(xmlData);
             }
             catch (final XMLStreamException e) {
                 throw new InvalidContentException(e);
             }
             catch (final LockingException e) {
                 throw new OptimisticLockingException(e);
             }
             catch (final OptimisticLockingException e) {
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
             catch (final WebserverSystemException e) {
                 throw e;
             }
             catch (final RelationPredicateNotFoundException e) {
                 throw e;
             }
             catch (final ReferencedResourceNotFoundException e) {
                 throw e;
             }
             catch (final InvalidContentException e) {
                 throw e;
             }
             catch (final InvalidXmlException e) {
                 throw e;
             }
             catch (final MissingAttributeValueException e) {
                 throw e;
             }
             catch (final MissingMdRecordException e) {
                 throw e;
             }
             catch (final Exception e) {
                 throw new SystemException("Should not occure in FedoraContainerHandler.update()", e);
             }
             sp.clearHandlerChain();
 
             final Map<String, Object> streams = me.getOutputStreams();
 
             // content-type-specific
             final Object ctsStream = streams.get(Elements.ELEMENT_CONTENT_MODEL_SPECIFIC);
             if (ctsStream != null) {
                 final ByteArrayOutputStream os = (ByteArrayOutputStream) ctsStream;
                 final String cts = os.toString(XmlUtility.CHARACTER_ENCODING);
                 setCts(cts);
             }
             // md-records
             final Map<String, Map<String, String>> mdRecordsAttributes = mdHandler.getMetadataAttributes();
             final Map mdRecordsStreams = (Map) streams.get(XmlUtility.NAME_MDRECORDS);
             if (!mdRecordsStreams.containsKey("escidoc")) {
                 throw new XmlCorruptedException("No escidoc internal metadata found " + "(md-record/@name='escidoc'");
             }
 
             final String escidocMdNsUri = mdHandler.getEscidocMdRecordNameSpace();
             setMetadataRecords(mdRecordsStreams, mdRecordsAttributes, escidocMdNsUri);
 
             // set content relations
             final List<String> relationsToUpdate = cruh.getContentRelationsData();
             getContainer().setContentRelations(sp, relationsToUpdate);
 
             // TODO merge with member entries from RELS-EXT if not read-only
             // anymore
 
             // properties
 
             // changed properties from update handler
             // TODO properties are read-only; change handler later
             // HashMap propertiesEntries = cpuh.getProperties();
 
             // current properties from relsExt
             // Map relsExtEntries = getRelsExtEntries();
             // overwrite with new values
             // relsExtEntries.putAll(propertiesEntries);
             // TODO dont forget title
 
             // create new RELS-EXT XML
             // TODO don't write since properties and struct-map are read-only
             // String relsExtXml = null;
             // relsExtXml = getRelsExtXml(relsExtEntries, structMapEntries,
             // false);
             // setRelsExt(relsExtXml);
 
             // }
 
             final String updatedXmlData;
             final DateTime endTimestamp = getContainer().getLastFedoraModificationDate();
             if (!startTimestamp.isEqual(endTimestamp) || getContainer().isNewVersion()) {
                 // object is modified
                 // make new version
                 makeVersion("ContainerHandler.update()");
                 getContainer().persist();
 
                 // retrieve updated container
                 updatedXmlData = retrieve(getContainer().getId());
                 fireContainerModified(getContainer().getId(), updatedXmlData);
             }
             else {
                 updatedXmlData = getContainerXml(this.getContainer());
             }
 
             return updatedXmlData;
         }
         catch (final StreamNotFoundException e) {
             throw new IntegritySystemException(e);
         }
         catch (final UnsupportedEncodingException e) {
             throw new EncodingSystemException(e);
         }
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            The id of the resource.
      * @param parameters
      *            parameters from the SRU request
      * @return Returns xml representation of a list of containers and items.
      * @throws ContainerNotFoundException
      *             Thrown if the given container was not found.
      */
     @Override
     public String retrieveMembers(final String id, final SRURequestParameters parameters)
         throws ContainerNotFoundException, TripleStoreSystemException, IntegritySystemException, SystemException {
         final StringWriter result = new StringWriter();
 
         setContainer(id);
         if (getContainer().isLatestVersion()) {
             if (parameters.isExplain()) {
                 // Items and containers are in the same index.
                 sruRequest.explain(result, ResourceType.ITEM);
             }
             else {
                 String query = "\"/resources/parent\"=" + getContainer().getId();
 
                 if (parameters.getQuery() != null) {
                     query += " AND " + parameters.getQuery();
                 }
                 sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.CONTAINER, ResourceType.ITEM },
                     query, parameters.getMaximumRecords(), parameters.getStartRecord(), parameters.getExtraData(),
                     parameters.getRecordPacking());
             }
         }
         else {
             /*
              * This is a hack, because of a crud concept regarding versions and search.
              * 
              * Obtain all member ids an create a search request to still let the search answer.
              */
             if (parameters.isExplain()) {
                 // Items and containers are in the same index.
                 sruRequest.explain(result, ResourceType.ITEM);
             }
             else {
                 StringBuilder query = new StringBuilder();
 
                 final UserFilter ufilter = new UserFilter();
 
                 List<String> ids = null;
                 try {
                     ids = ufilter.getMemberRefList(getContainer());
                 }
                 catch (MissingMethodParameterException e) {
                     throw new SystemException("Error retrieving member references for Container '" + id + "'.", e);
                 }
 
                 if (ids.isEmpty()) {
                     // create an empty search response (problem: if no query is given, than are all resources returned.
                     // A useless query is not a solution, because the query is part of the response)
                     sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.CONTAINER, ResourceType.ITEM },
                         "\"/id\"=-1", parameters.getMaximumRecords(), parameters.getStartRecord(), parameters
                             .getExtraData(), parameters.getRecordPacking());
                 }
                 else {
                     final Iterator<String> idIter = ids.iterator();
 
                     if (idIter.hasNext()) {
                         query.append("\"/id\"=");
                         query.append(idIter.next());
                     }
                     while (idIter.hasNext()) {
                         query.append(" OR \"/id\"=");
                         query.append(idIter.next());
                     }
 
                     if (parameters.getQuery() != null) {
                         query.append(" AND ");
                         query.append(parameters.getQuery());
                     }
                     sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.CONTAINER, ResourceType.ITEM },
                         query.toString(), parameters.getMaximumRecords(), parameters.getStartRecord(), parameters
                             .getExtraData(), parameters.getRecordPacking());
                 }
             }
         }
         return result.toString();
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            The id of the resource.
      * @param parameters
      *            parameters from the SRU request
      * @return Returns xml representation of a list of tocs.
      * @throws ContainerNotFoundException
      *             Thrown if the given container was not found.
      * @throws SystemException
      *             cf. Interface
      */
     @Override
     public String retrieveTocs(final String id, final SRURequestParameters parameters)
         throws ContainerNotFoundException, SystemException {
         final StringWriter result = new StringWriter();
 
         this.utility.checkIsContainer(id);
         if (parameters.isExplain()) {
             sruRequest.explain(result, ResourceType.ITEM);
         }
         else {
             String query =
                 "\"/resources/parent\"=" + id + " AND \"/properties/content-model/id\"="
                     + EscidocConfiguration.getInstance().get("escidoc-core.toc.content-model");
 
             if (parameters.getQuery() != null) {
                 query += " AND " + parameters.getQuery();
             }
             sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.ITEM }, query, parameters
                 .getMaximumRecords(), parameters.getStartRecord(), parameters.getExtraData(), parameters
                 .getRecordPacking());
         }
         return result.toString();
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param containerId
      *            containerId
      * @param taskParam
      *            taskParam
      * @throws ContainerNotFoundException
      *             e
      * @throws ContextNotFoundException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws LockingException
      *             e
      * @throws MissingMethodParameterException
      *             cf. Interface
      */
     @Override
     public String moveToContext(final String containerId, final String taskParam) throws ContainerNotFoundException,
         ContextNotFoundException, InvalidContentException, LockingException, MissingMethodParameterException {
 
         // TODO: implement
         throw new UnsupportedOperationException("FedoraContainerHandler.moveToContext not yet implemented");
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param parameters
      *            parameters from the SRU request
      * @return The list of Containers matching filter parameter.
      */
     @Override
     public String retrieveContainers(final SRURequestParameters parameters) throws WebserverSystemException {
         final StringWriter result = new StringWriter();
 
         if (parameters.isExplain()) {
             sruRequest.explain(result, ResourceType.CONTAINER);
         }
         else {
             sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.CONTAINER }, parameters);
         }
         return result.toString();
     }
 
     /**
      * @param itemHandler
      *            the itemHandler
      */
     public void setItemHandler(final FedoraItemHandler itemHandler) {
         this.itemHandler = itemHandler;
     }
 
     /**
      * Retrieves item handler.
      * 
      * @return FedoraItemHandler itemHandler
      */
     public FedoraItemHandler getItemHandler() {
         return this.itemHandler;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param xmlData
      *            xmlData
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws InvalidXmlException
      *             cf. Interface
      */
     @Override
     public String createMdRecord(final String id, final String xmlData) throws ContainerNotFoundException,
         LockingException, MissingMethodParameterException, InvalidXmlException {
 
         // TODO: implement
         throw new UnsupportedOperationException("FedoraContainerHandler.createMdRecord not yet implemented");
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param mdRecordId
      *            mdRecordId
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws MdRecordNotFoundException
      *             e
      */
     public void deleteMetadataRecord(final String id, final String mdRecordId) {
         // TODO: implement
         throw new UnsupportedOperationException("FedoraContainerHandler.deleteMetadataRecord not yet implemented");
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param mdRecordId
      *            mdRecordId
      * @throws ContainerNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @throws MdRecordNotFoundException
      *             cf. Interface
      */
     @Override
     public String retrieveMdRecord(final String id, final String mdRecordId) throws ContainerNotFoundException,
         SystemException, MdRecordNotFoundException {
 
         setContainer(id);
         return getMetadataRecordXml(mdRecordId);
     }
 
     /**
      * @param id
      *            id
      * @param mdRecordId
      *            mdRecordId
      * @throws ContainerNotFoundException
      *             e
      * @throws MdRecordNotFoundException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.om.service.interfaces.ContainerHandlerInterface#retrieveMdRecordContent(String, String)
      */
     @Override
     public String retrieveMdRecordContent(final String id, final String mdRecordId) throws ContainerNotFoundException,
         MdRecordNotFoundException, MissingMethodParameterException, SystemException {
         setContainer(id);
         return retrieveMdRecord(mdRecordId);
     }
 
     /**
      * @param id
      *            id
      * @throws ContainerNotFoundException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @see de.escidoc.core.om.service.interfaces.ContainerHandlerInterface# retrieveDcContent(java.lang.String)
      */
     @Override
     public String retrieveDcRecordContent(final String id) throws ContainerNotFoundException,
         MissingMethodParameterException, SystemException {
         setContainer(id);
         return retrieveDc();
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param containerId
      *            containerId
      * @throws ContainerNotFoundException
      *             e
      * @throws SystemException
      *             cf. Interface
      */
     @Override
     public String retrieveMdRecords(final String containerId) throws ContainerNotFoundException, SystemException {
 
         setContainer(containerId);
         return getMetadataRecordsXml();
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param mdRecordId
      *            mdRecordId
      * @param xmlData
      *            xmlData
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws MdRecordNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @throws InvalidXmlException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws ReadonlyVersionException
      *             cf. Interface
      */
     @Override
     public String updateMetadataRecord(final String id, final String mdRecordId, final String xmlData)
         throws ContainerNotFoundException, LockingException, MdRecordNotFoundException, SystemException,
         InvalidXmlException, InvalidStatusException, ReadonlyVersionException {
 
         setContainer(id);
 
         checkStatusNot(Constants.STATUS_WITHDRAWN);
         checkLocked();
         checkLatestVersion();
 
         // TODO: implement
         throw new UnsupportedOperationException("FedoraContainerHandler.updateMetadataRecord not yet implemented");
     }
 
     /**
      * Creates Stream objects from the ByteArrayOutputStreams in {@code mdMap} and calls Container.setMdRecords with a
      * HashMap which contains the metadata datastreams as Stream objects.
      * 
      * @param mdMap
      *            A HashMap which contains the metadata datastreams as ByteArrayOutputStream.
      * @param mdAttributesMap
      *            mdAttributesMap
      * @param escidocMdRecordnsUri
      *            escidocMdRecordnsUri
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @throws FedoraSystemException
      *             e
      * @throws TripleStoreSystemException
      *             e
      * @throws IntegritySystemException
      *             e
      * @throws EncodingSystemException
      *             e
      */
     private void setMetadataRecords(
         final Map<String, ByteArrayOutputStream> mdMap, final Map mdAttributesMap, final String escidocMdRecordnsUri)
         throws WebserverSystemException, FedoraSystemException, TripleStoreSystemException, IntegritySystemException,
         EncodingSystemException {
         final Map<String, Datastream> dsMap = new HashMap<String, Datastream>();
 
         for (final Entry<String, ByteArrayOutputStream> stringByteArrayOutputStreamEntry : mdMap.entrySet()) {
             final ByteArrayOutputStream stream = stringByteArrayOutputStreamEntry.getValue();
             final byte[] xmlBytes = stream.toByteArray();
             HashMap<String, String> mdProperties = null;
             if ("escidoc".equals(stringByteArrayOutputStreamEntry.getKey())) {
                 mdProperties = new HashMap<String, String>();
                 mdProperties.put("nsUri", escidocMdRecordnsUri);
 
             }
             final Datastream ds =
                 new Datastream(stringByteArrayOutputStreamEntry.getKey(), getContainer().getId(), xmlBytes,
                     MimeTypes.TEXT_XML, mdProperties);
             final Map mdRecordAttributes = (Map) mdAttributesMap.get(stringByteArrayOutputStreamEntry.getKey());
             ds.addAlternateId(Datastream.METADATA_ALTERNATE_ID);
             ds.addAlternateId((String) mdRecordAttributes.get("type"));
             ds.addAlternateId((String) mdRecordAttributes.get("schema"));
             dsMap.put(stringByteArrayOutputStreamEntry.getKey(), ds);
         }
         getContainer().setMdRecords(dsMap);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @throws ContainerNotFoundException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             cf. Interface
      */
     @Override
     public String retrieveRelations(final String id) throws ContainerNotFoundException,
         MissingMethodParameterException, SystemException {
 
         setContainer(id);
         return getRelationsXml(this.getContainer());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @throws ContainerNotFoundException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             cf. Interface
      */
     @Override
     public String retrieveResources(final String id) throws ContainerNotFoundException,
         MissingMethodParameterException, SystemException {
 
         setContainer(id);
         return getResourcesXml();
     }
 
     /**
      * @param id
      * @param resourceName
      * @return
      * @throws SystemException
      * @throws ContainerNotFoundException
      * @throws OperationNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     public EscidocBinaryContent retrieveResource(final String id, final String resourceName) throws SystemException,
         ContainerNotFoundException, OperationNotFoundException {
         return retrieveResource(id, resourceName, null);
     }
 
     @Override
     public EscidocBinaryContent retrieveResource(
         final String id, final String resourceName, final Map<String, String[]> parameters) throws SystemException,
         ContainerNotFoundException, OperationNotFoundException {
 
         final EscidocBinaryContent content = new EscidocBinaryContent();
         content.setMimeType(MimeTypes.TEXT_XML);
 
         if ("members".equals(resourceName)) {
             try {
                 content.setContent(new ByteArrayInputStream(
                     retrieveMembers(id, new LuceneRequestParameters(parameters))
                         .getBytes(XmlUtility.CHARACTER_ENCODING)));
             }
             catch (final UnsupportedEncodingException e) {
                 throw new WebserverSystemException(e);
             }
             catch (final IOException e) {
                 throw new WebserverSystemException(e);
             }
         }
         else if ("version-history".equals(resourceName)) {
             try {
                 content.setContent(new ByteArrayInputStream(retrieveVersionHistory(id).getBytes(
                     XmlUtility.CHARACTER_ENCODING)));
             }
             catch (final UnsupportedEncodingException e) {
                 throw new WebserverSystemException(e);
             }
             catch (final IOException e) {
                 throw new WebserverSystemException(e);
             }
         }
         else if ("relations".equals(resourceName)) {
             try {
                 content.setContent(new ByteArrayInputStream(retrieveContentRelations(id).getBytes(
                     XmlUtility.CHARACTER_ENCODING)));
             }
             catch (final UnsupportedEncodingException e) {
                 throw new WebserverSystemException(e);
             }
             catch (final IOException e) {
                 throw new WebserverSystemException(e);
             }
         }
         else if ("parents".equals(resourceName)) {
             try {
                 content
                     .setContent(new ByteArrayInputStream(retrieveParents(id).getBytes(XmlUtility.CHARACTER_ENCODING)));
             }
             catch (final UnsupportedEncodingException e) {
                 throw new WebserverSystemException(e);
             }
             catch (final IOException e) {
                 throw new WebserverSystemException(e);
             }
         }
         else {
             setContainer(id);
             final String contentModelId = getContainer().getProperty(PropertyMapKeys.LATEST_VERSION_CONTENT_MODEL_ID);
             final Stream stream = this.fedoraServiceClient.getDissemination(id, contentModelId, resourceName);
             try {
                 content.setContent(stream.getInputStream());
             }
             catch (IOException e) {
                 throw new FedoraSystemException("Error on reading stream.", e);
             }
         }
 
         return content;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @throws ContainerNotFoundException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             cf. Interface
      */
     @Override
     public String retrieveProperties(final String id) throws ContainerNotFoundException,
         MissingMethodParameterException, SystemException {
 
         setContainer(id);
         return getPropertiesXml(this.getContainer());
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            The id of the Container.
      * @return The struct-map of the Container.
      * @throws ContainerNotFoundException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      */
     @Override
     public String retrieveStructMap(final String id) throws ContainerNotFoundException,
         MissingMethodParameterException, SystemException {
 
         setContainer(id);
         return getStructMapXml(this.getContainer());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            The id of the Container.
      * @param param
      *            The task parameter structure.
      * @throws ContainerNotFoundException
      *             Thrown if no Container could be found under the provided id.
      * @throws LockingException
      *             Thrown of the Container is locked through others.
      * @throws MissingMethodParameterException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws ReadonlyVersionException
      *             cf. Interface
      */
     @Override
     public String release(final String id, final String param) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, InvalidStatusException, SystemException, OptimisticLockingException,
         ReadonlyVersionException, XmlCorruptedException {
 
         setContainer(id);
         checkLatestVersion();
         checkLocked();
         checkReleased();
         checkPid();
 
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         final String label = "Container " + getContainer().getId();
         if (checkUnlocked(getContainer().isLocked(), "Release", label, getContainer().getLockOwner())
             && Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
                 .getLastModificationDate(), label)) {
 
             // check version status
             final String curStatus =
                 this.tripleStoreUtility.getPropertiesElements(getContainer().getId(),
                     TripleStoreUtility.PROP_LATEST_VERSION_STATUS);
             if (!Constants.STATUS_SUBMITTED.equals(curStatus)) {
                 throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_SUBMITTED
                     + "' and can not be" + " released.");
             }
 
             releaseMembers(id);
             setContainer(id);
 
             // set status "submited"
             // only renew the timestamp and set status with version entry
            makeVersion(taskParameter.getComment(), Constants.STATUS_RELEASED);
             getContainer().setLatestReleasePid();
             getContainer().persist();
         }
 
         // getUtility().notifyIndexerAddPublication(getContainer().getHref());
         fireContainerModified(getContainer().getId(), null);
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * Calls release methods for all container members.
      * <p/>
      * The concept of releasing members is incomplete and create huge security holes.
      * 
      * @param id
      *            The objid of the parent Container.
      * @throws OptimisticLockingException
      *             Thrown if objects altered through other processes during the release.
      * @throws SystemException
      *             Thrown in case of internal error.
      */
     @Deprecated
     private void releaseMembers(final String id) throws OptimisticLockingException, SystemException {
 
         // Find all members of container
         final List<String> memberIds = this.tripleStoreUtility.getMemberList(id, null);
 
         // for each refered item or container
 
         for (final String memberId : memberIds) {
             final String objectType = this.tripleStoreUtility.getObjectType(memberId);
 
             if (Constants.CONTAINER_OBJECT_TYPE.equals(objectType)) {
 
                 final Container container;
                 try {
                     container = new Container(memberId);
                 }
                 catch (final ResourceNotFoundException e1) {
                     throw new WebserverSystemException(e1);
                 }
                 final String param = "<param last-modification-date=\"" + container.getLastModificationDate() + "\"/>";
                 try {
                     release(memberId, param);
                 }
                 catch (final InvalidStatusException e) {
                     // do next member
                     LOGGER
                         .warn("Member '" + memberId + "' of container '" + getContainer().getId() + "' not released.");
                 }
                 catch (final LockingException e) {
                     LOGGER.warn("Member '" + memberId + "' of container '" + getContainer().getId() + "' locked.");
                 }
                 catch (final OptimisticLockingException e) {
                     LOGGER.warn(
                         "Member '" + memberId + "' of container '" + getContainer().getId() + "' not released.", e);
                     throw e;
                 }
                 catch (final WebserverSystemException e) {
                     throw e;
                 }
                 catch (final Exception e) {
                     throw new IntegritySystemException(e);
                 }
             }
             else if (Constants.ITEM_OBJECT_TYPE.equals(objectType)) {
 
                 // TODO do equavalent to container
                 // String lastModificationDate =
                 // TripleStoreUtility.getInstance().getPropertiesElements(
                 // memberId, "latest-version.date",
                 // Constants.
                 // PROPERTIES_NAMESPACE_URI);
                 try {
                     setItem(memberId);
                 }
                 catch (final Exception e) {
                     throw new WebserverSystemException(e);
                     // if (LOGGER.isWarnEnabled()) {
                     // LOGGER.warn("Error on setting item.");
                     // }
                     // if (LOGGER.isDebugEnabled()) {
                     // LOGGER.debug("Error on setting item.", e);
                     // }
                     // // do nothing
                     // continue;
                 }
                 final String param = "<param last-modification-date=\"" + getItem().getLastModificationDate() + "\"/>";
 
                 try {
                     itemHandler.release(memberId, param);
                 }
                 catch (final InvalidStatusException e) {
                     // do next member
                     LOGGER
                         .warn("Member '" + memberId + "' of container '" + getContainer().getId() + "' not released.");
                 }
                 catch (final LockingException e) {
                     LOGGER.warn("Member '" + memberId + "' of container '" + getContainer().getId() + "' locked.");
                 }
                 catch (final Exception e) {
                     throw new IntegritySystemException(e);
 
                 }
 
             }
             else {
                 throw new IntegritySystemException(StringUtility.format("Wrong object type of the member: "
                     + "member must be either item or container", objectType));
             }
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param param
      *            param
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws ReadonlyVersionException
      *             cf. Interface
      */
     @Override
     public String submit(final String id, final String param) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, InvalidStatusException, SystemException, OptimisticLockingException,
         ReadonlyVersionException, XmlCorruptedException {
 
         setContainer(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         checkLatestVersion();
         // checkLocked();
         // checkStatusNot(Constants.STATUS_RELEASED);
         checkReleased();
 
         final String label = "Container " + getContainer().getId();
         if (checkUnlocked(getContainer().isLocked(), "Submit", label, getContainer().getLockOwner())
             && Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
                 .getLastModificationDate(), label)) {
 
             // check version status
             final String curStatus =
                 this.tripleStoreUtility.getPropertiesElements(getContainer().getId(),
                     TripleStoreUtility.PROP_LATEST_VERSION_STATUS);
             if (!(Constants.STATUS_PENDING.equals(curStatus) || Constants.STATUS_IN_REVISION.equals(curStatus))) {
                 throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_PENDING + "' or '"
                     + Constants.STATUS_IN_REVISION + "' and can not be" + " submitted.");
             }
 
             // set status "submited"
             // only renew the timestamp and set status with version entry
             makeVersion(taskParameter.getComment(), Constants.STATUS_SUBMITTED);
             getContainer().persist();
             fireContainerModified(getContainer().getId(), null);
         }
 
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param param
      *            param
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws ReadonlyVersionException
      *             cf. Interface
      * @throws XmlCorruptedException
      *             e
      */
     @Override
     public String revise(final String id, final String param) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, InvalidStatusException, SystemException, OptimisticLockingException,
         ReadonlyVersionException, XmlCorruptedException {
 
         setContainer(id);
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         checkLatestVersion();
         // checkLocked();
         checkVersionStatus(Constants.STATUS_SUBMITTED);
 
         final String label = "Container " + getContainer().getId();
 
         // FIXME Item and Container throw different Exceptions if the last-modification-date is not given within the
         // taskParam XML. It's decided to keep consistency between version 1.3 and 1.4 and therefore not to implement
         // the same behavior for version 1.4. Consistency could be reached, if taskParam is described as XML schema.
         if (taskParameter.getLastModificationDate() == null) {
             throw new XmlCorruptedException("Missing last-modification-date");
         }
 
         if (checkUnlocked(getContainer().isLocked(), "Submit", label, getContainer().getLockOwner())
             && Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
                 .getLastModificationDate(), label)) {
 
             // check version status
             final String curStatus =
                 this.tripleStoreUtility.getPropertiesElements(getContainer().getId(),
                     TripleStoreUtility.PROP_LATEST_VERSION_STATUS);
             if (!Constants.STATUS_SUBMITTED.equals(curStatus)) {
                 throw new InvalidStatusException("The object is not in state '" + Constants.STATUS_SUBMITTED
                     + "' and can not be" + " set in revision.");
             }
 
             // set status "in-revision"
             // only renew the timestamp and set status with version entry
             makeVersion(taskParameter.getComment(), Constants.STATUS_IN_REVISION);
             getContainer().persist();
 
             fireContainerModified(getContainer().getId(), null);
         }
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param param
      *            param
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws AlreadyWithdrawnException
      *             e
      * @throws ReadonlyVersionException
      *             cf. Interface
      */
     @Override
     public String withdraw(final String id, final String param) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, InvalidStatusException, SystemException, OptimisticLockingException,
         AlreadyWithdrawnException, ReadonlyVersionException, XmlCorruptedException {
 
         if (id == null || !this.tripleStoreUtility.exists(id)) {
             throw new ContainerNotFoundException("Container with id " + id + " does not exist.");
         }
         else if (!Constants.CONTAINER_OBJECT_TYPE.equals(this.tripleStoreUtility.getObjectType(id))) {
             throw new ContainerNotFoundException(StringUtility.format("Object is no container", id));
         }
 
         final String curStatus =
             this.tripleStoreUtility.getPropertiesElements(id, TripleStoreUtility.PROP_PUBLIC_STATUS);
 
         if (curStatus.equals(Constants.STATUS_WITHDRAWN)) {
             throw new AlreadyWithdrawnException("The object is already withdrawn");
         }
         if (!curStatus.equals(Constants.STATUS_RELEASED)) {
             throw new InvalidStatusException("The object is not in state 'released' and can not be withdrawn.");
         }
 
         setContainer(id);
         checkLatestVersion();
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         final String label = "Container " + getContainer().getId();
         if (checkUnlocked(getContainer().isLocked(), "Withdraw", label, getContainer().getLockOwner())
             && Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
                 .getLastModificationDate(), label)) {
 
             // FIXME Under which circumstances members should be withdrawn?
             // Look for content-type-type not for name.
             if (this.tripleStoreUtility.getPropertiesElements(id, TripleStoreUtility.PROP_CONTENT_MODEL_TITLE).equals(
                 COLLECTION)) {
                 withdrawMembers(id, taskParameter.getWithdrawComment());
                 setContainer(id);
             }
 
             // withdrawing is possible in every version status
 
             // only renew the timestamp and set status with version entry
             makeVersion(taskParameter.getComment(), Constants.STATUS_WITHDRAWN);
             getContainer().persist();
 
             // notify indexer
             // getUtility().notifyIndexerDeletePublication(
             // getContainer().getHref());
             fireContainerModified(getContainer().getId(), null);
         }
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
 
     }
 
     /**
      * Calls withdraw method for all container members.
      * 
      * @param id
      *            id
      * @param withdrawComment
      *            withdrawComment
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     private void withdrawMembers(final String id, final String withdrawComment) throws TripleStoreSystemException,
         IntegritySystemException, FedoraSystemException, WebserverSystemException {
 
         // Find all members of container
         final List<String> memberIds = this.tripleStoreUtility.getMemberList(id, null);
 
         // for each refered item or container
 
         for (final String memberId : memberIds) {
             final String objectType = this.tripleStoreUtility.getObjectType(memberId);
 
             if (Constants.CONTAINER_OBJECT_TYPE.equals(objectType)) {
                 final String lastModificationDate =
                     this.tripleStoreUtility
                         .getPropertiesElements(memberId, TripleStoreUtility.PROP_LATEST_VERSION_DATE);
                 final String param =
                     "<param last-modification-date=\"" + lastModificationDate + "\"><withdraw-comment>"
                         + withdrawComment + "</withdraw-comment></param>";
 
                 try {
                     withdraw(memberId, param);
                 }
                 catch (final InvalidStatusException e) {
                     if (LOGGER.isWarnEnabled()) {
                         LOGGER.warn("Error on withdraw container.");
                     }
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Error on withdraw container.", e);
                     }
                     // do next member
                 }
                 catch (final Exception e) {
                     throw new IntegritySystemException(e);
                 }
             }
             else if (Constants.ITEM_OBJECT_TYPE.equals(objectType)) {
 
                 try {
                     setItem(memberId);
                 }
                 catch (final Exception e) {
                     if (LOGGER.isWarnEnabled()) {
                         LOGGER.warn("Error on setting item.");
                     }
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Error on setting item.", e);
                     }
                     // do nothing
                 }
 
                 final DateTime lastModificationDate = getItem().getLastModificationDate();
 
                 final String param =
                     "<param last-modification-date=\"" + lastModificationDate + "\"><withdraw-comment>"
                         + withdrawComment + "</withdraw-comment></param>";
 
                 try {
                     itemHandler.withdraw(memberId, param);
                 }
                 catch (final InvalidStatusException e) {
                     if (LOGGER.isWarnEnabled()) {
                         LOGGER.warn("Error on withdraw item.");
                     }
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Error on withdraw item.", e);
                     }
                     // do next member
                 }
                 catch (final Exception e) {
                     throw new IntegritySystemException(e);
 
                 }
             }
             else {
                 throw new IntegritySystemException(StringUtility.format("Wrong object type of the member: "
                     + "member must be either item or container", objectType));
             }
         }
     }
 
     /**
      * The Container will be locked for changes until the lockOwner (the current user) or an Administrator unlocks the
      * Container.
      * 
      * @param id
      *            The id of the Container.
      * @param param
      *            The task parameter structure.
      * @throws ContainerNotFoundException
      *             Thrown if the Container was not found.
      * @throws LockingException
      *             Thrown if the Container could not been locked.
      * @throws InvalidStatusException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @see de.escidoc.core.om.service.interfaces.ContainerHandlerInterface#lock(String, String)
      */
     @Override
     public String lock(final String id, final String param) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
         XmlCorruptedException {
 
         setContainer(id);
         try {
             checkStatusNot(Constants.STATUS_WITHDRAWN);
         }
         catch (final InvalidStatusException e) {
             throw new InvalidStatusException("Container can not be locked, because it "
                 + "is in an inappropriate state.", e);
         }
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
 
         if (getContainer().isLocked()) {
             throw new LockingException("Container " + getContainer().getId() + " is already locked by "
                 + getContainer().getLockOwner());
         }
         else if (Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Container " + getContainer().getId())) {
             getContainer().setLocked(true, Utility.getCurrentUser());
             fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
         }
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * The Container will be unlocked. The lockOwner or an Administrator may unlock the Container.
      * 
      * @param id
      *            The id of the Container.
      * @param param
      *            The task parameter structure.
      * @return XML result with last-modification-date of Container as attribute.
      * @throws ContainerNotFoundException
      *             Thrown if the Container was not found.
      * @throws LockingException
      *             Thrown if the Container could not been unlocked.
      * @throws InvalidStatusException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @see de.escidoc.core.om.service.interfaces.ContainerHandlerInterface#unlock(String, String)
      */
     @Override
     public String unlock(final String id, final String param) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
         XmlCorruptedException {
 
         setContainer(id);
         try {
             checkStatusNot(Constants.STATUS_WITHDRAWN);
         }
         catch (final InvalidStatusException e) {
             throw new InvalidStatusException("Container can not be unlocked, because it "
                 + "is in an inappropriate state.", e);
         }
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         if (!getContainer().isLocked()) {
             throw new LockingException("Container " + getContainer().getId() + " is not locked.");
         }
         else if (Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Container " + getContainer().getId())) {
             getContainer().setLocked(false, Utility.getCurrentUser());
             fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
         }
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @throws ContainerNotFoundException
      *             e
      * @throws SystemException
      *             cf. Interface
      */
     @Override
     public String retrieveVersionHistory(final String id) throws ContainerNotFoundException, SystemException {
 
         setContainer(id);
         final String versionsXml;
 
         try {
             versionsXml =
                 getVersions().replaceFirst(
                     '<' + Constants.WOV_NAMESPACE_PREFIX + ':' + Elements.ELEMENT_WOV_VERSION_HISTORY,
                     "<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + Constants.WOV_NAMESPACE_PREFIX + ':'
                         + Elements.ELEMENT_WOV_VERSION_HISTORY + " xml:base=\"" + XmlUtility.getEscidocBaseUrl()
                         + "\" " + MODIFIED_DATE_ATT_NAME + "=\"" + getContainer().getLastModificationDate() + "\" ");
             // versionsXml = getVersions().replaceFirst(
             // "xlink:type=\"simple\"",
             // "xml:base=\"" + XmlUtility.getEscidocBaseUrl()
             // + "\" xlink:type=\"simple\" " + MODIFIED_DATE_ATT_NAME
             // + "=\"" + getContainer().getLastModificationDate() + "\" ");
         }
         catch (final StreamNotFoundException e) {
             throw new IntegritySystemException("Version history not found.", e);
         }
 
         return versionsXml;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @throws ContainerNotFoundException
      *             e
      * @throws SystemException
      *             cf. Interface
      */
     @Override
     public String retrieveParents(final String id) throws ContainerNotFoundException, SystemException {
         this.utility.checkIsContainer(id);
         return getContainerRenderer().renderParents(id);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param containerId
      *            containerId
      * @param xmlData
      *            xmlData
      * @throws ContainerNotFoundException
      *             e
      * @throws MissingContentException
      *             e
      * @throws ContextNotFoundException
      *             e
      * @throws ContentModelNotFoundException
      *             e
      * @throws ReadonlyElementViolationException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws MissingElementValueException
      *             e
      * @throws ReadonlyAttributeViolationException
      *             e
      * @throws MissingMethodParameterException
      *             e
      * @throws FileNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws InvalidContextException
      *             e
      * @throws RelationPredicateNotFoundException
      *             e
      * @throws ReferencedResourceNotFoundException
      *             e
      * @throws SystemException
      *             e
      * @throws MissingMdRecordException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws AuthorizationException
      *             e
      */
     @Override
     public String createItem(final String containerId, final String xmlData) throws ContainerNotFoundException,
         MissingContentException, ContextNotFoundException, ContentModelNotFoundException,
         ReadonlyElementViolationException, MissingAttributeValueException, MissingElementValueException,
         ReadonlyAttributeViolationException, MissingMethodParameterException, FileNotFoundException, LockingException,
         InvalidContentException, InvalidContextException, RelationPredicateNotFoundException,
         ReferencedResourceNotFoundException, SystemException, MissingMdRecordException, InvalidStatusException,
         AuthorizationException, XmlCorruptedException {
 
         this.utility.checkSameContext(containerId, xmlData);
         // checkContextStatus(contextId, Constants.STATUS_CONTEXT_OPENED);
 
         setContainer(containerId);
         checkLocked();
 
         final String itemXml = itemHandler.create(xmlData);
         final String itemId = XmlUtility.getIdFromXml(itemXml);
 
         makeVersion("Item added");
 
         // rebuild rels-ext
         final List<StartElementWithChildElements> elements = new ArrayList<StartElementWithChildElements>();
 
         final StartElementWithChildElements newMemberElement = new StartElementWithChildElements();
         newMemberElement.setLocalName("member");
         newMemberElement.setPrefix(Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         newMemberElement.setNamespace(Constants.STRUCTURAL_RELATIONS_NS_URI);
         final Attribute resource =
             new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX, "info:fedora/"
                 + itemId);
         newMemberElement.addAttribute(resource);
         newMemberElement.setChildrenElements(null);
         elements.add(newMemberElement);
 
         final byte[] relsExtNewBytes = Utility.updateRelsExt(elements, null, null, getContainer(), null);
         getContainer().setRelsExt(relsExtNewBytes);
         getContainer().persist();
 
         fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
         itemHandler.fireItemModified(itemId, itemXml);
 
         return itemXml;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param containerId
      *            containerId
      * @param xmlData
      *            xmlData
      * @throws MissingMethodParameterException
      *             e
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws ContextNotFoundException
      *             e
      * @throws ContentModelNotFoundException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws MissingAttributeValueException
      *             e
      * @throws MissingElementValueException
      *             e
      * @throws AuthenticationException
      *             e
      * @throws AuthorizationException
      *             e
      * @throws InvalidContextException
      *             e
      * @throws RelationPredicateNotFoundException
      *             e
      * @throws ReferencedResourceNotFoundException
      *             e
      * @throws SystemException
      *             cf. Interface
      * @throws InvalidStatusException
      *             Thrown if an organizational unit is in an invalid status.
      * @throws MissingMdRecordException
      *             e
      */
     @Override
     public String createContainer(final String containerId, final String xmlData)
         throws MissingMethodParameterException, ContainerNotFoundException, LockingException, ContextNotFoundException,
         ContentModelNotFoundException, InvalidContentException, MissingAttributeValueException,
         MissingElementValueException, AuthenticationException, AuthorizationException, InvalidContextException,
         RelationPredicateNotFoundException, InvalidStatusException, ReferencedResourceNotFoundException,
         SystemException, MissingMdRecordException, XmlSchemaValidationException, XmlCorruptedException {
 
         this.utility.checkSameContext(containerId, xmlData);
         // checkContextStatus(contextId, Constants.STATUS_CONTEXT_OPENED);
 
         setContainer(containerId);
         checkLocked();
 
         final String containerXml = create(xmlData);
         setContainer(containerId);
 
         final String objid = XmlUtility.getIdFromXml(containerXml);
 
         makeVersion("Add Container");
 
         // rebuild rels-ext
 
         final List<StartElementWithChildElements> elements = new ArrayList<StartElementWithChildElements>();
 
         final StartElementWithChildElements newMemberElement = new StartElementWithChildElements();
         newMemberElement.setLocalName("member");
         newMemberElement.setPrefix(Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         newMemberElement.setNamespace(Constants.STRUCTURAL_RELATIONS_NS_URI);
         final Attribute resource =
             new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX, "info:fedora/"
                 + objid);
         newMemberElement.addAttribute(resource);
         // newComponentIdElement.setElementText(componentId);
         newMemberElement.setChildrenElements(null);
 
         elements.add(newMemberElement);
 
         final byte[] relsExtNewBytes = Utility.updateRelsExt(elements, null, null, getContainer(), null);
 
         // FIXME first set RELS-EXT, then persist, then check if container is
         // changed and make version, retrieve new xml representation and fire
         // "container changed" ("container created" is fired in previous called
         // create method)
         getContainer().setRelsExt(relsExtNewBytes);
         getContainer().persist();
 
         fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
         fireContainerModified(objid, containerXml);
 
         return containerXml;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param taskParam
      *            taskParam
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws SystemException
      *             e
      * @throws InvalidContextException
      *             e
      * @throws MissingAttributeValueException
      *             cf. Interface
      */
     @Override
     public String addMembers(final String id, final String taskParam) throws ContainerNotFoundException,
         LockingException, InvalidContentException, OptimisticLockingException, SystemException,
         InvalidContextException, MissingAttributeValueException {
 
         setContainer(id);
 
         try {
             checkLocked();
             try {
                 checkStatusNot(Constants.STATUS_WITHDRAWN);
             }
             catch (final InvalidStatusException e) {
                 throw new InvalidStatusException("Members can not be added, because the "
                     + "container is in an inappropriate state.", e);
             }
             final DateTime startTimestamp = getContainer().getLastFedoraModificationDate();
 
             final StaxParser sp = new StaxParser();
 
             sp.addHandler(new OptimisticLockingHandler(getContainer().getId(), Constants.CONTAINER_OBJECT_TYPE,
                 getContainer().getLastModificationDate()));
 
             // add a member retrieving handler
             final BuildRelsExtMemberEntriesFromTaskParamHandlerNew bremeftph =
                 new BuildRelsExtMemberEntriesFromTaskParamHandlerNew(getContainer().getId(), "add");
             sp.addHandler(bremeftph);
 
             sp.parse(taskParam);
             sp.clearHandlerChain();
             // check for same context
             final List<String> memberIds = bremeftph.getMemberIds();
             for (final String memberId : memberIds) {
                 if (!this.utility.hasSameContext(memberId, getContainer().getContextId())) {
                     throw new InvalidContextException("Member has not the same context as container.");
                 }
             }
 
             // rebuild rels-ext
             final List<StartElementWithChildElements> elements = new ArrayList<StartElementWithChildElements>();
 
             for (final String memberId : memberIds) {
                 final StartElementWithChildElements newComponentIdElement = new StartElementWithChildElements();
                 newComponentIdElement.setLocalName("member");
                 newComponentIdElement.setPrefix(Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
                 newComponentIdElement.setNamespace(Constants.STRUCTURAL_RELATIONS_NS_URI);
                 final Attribute resource =
                     new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX,
                         "info:fedora/" + memberId);
                 newComponentIdElement.addAttribute(resource);
                 // newComponentIdElement.setElementText(componentId);
                 newComponentIdElement.setChildrenElements(null);
 
                 elements.add(newComponentIdElement);
             }
             boolean resourceUpdated = false;
             if (!elements.isEmpty()) {
                 resourceUpdated = true;
                 final byte[] relsExtNewBytes = Utility.updateRelsExt(elements, null, null, getContainer(), null);
                 getContainer().setRelsExt(
                     new Datastream(Datastream.RELS_EXT_DATASTREAM, getContainer().getId(), relsExtNewBytes,
                         MimeTypes.TEXT_XML));
             }
             final DateTime endTimestamp = getContainer().getLastFedoraModificationDate();
             if (resourceUpdated || !startTimestamp.isEqual(endTimestamp)) {
                 // updateTimeStamp();
                 makeVersion("Container.addMembers");
                 getContainer().persist();
 
                 fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
 
                 // Also reindex members
                 for (final String memberId : memberIds) {
                     fireContainerMembersModified(memberId);
                 }
             }
 
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final TripleStoreSystemException e) {
             throw e;
         }
         catch (final InvalidContextException e) {
             throw e;
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (final Exception e) {
             throw new SystemException("Should not occur in FedoraContainerHandler.addMember.", e);
         }
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param taskParam
      *            taskParam
      * @throws ContainerNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws InvalidContentException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws SystemException
      *             e
      * @throws InvalidContextException
      *             e
      * @throws MissingAttributeValueException
      *             cf. Interface
      * @see ContainerHandlerInterface#addMembers(String, String)
      */
     @Override
     public String addTocs(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         InvalidContentException, OptimisticLockingException, SystemException, InvalidContextException,
         MissingAttributeValueException {
 
         final StaxParser sp = new StaxParser();
 
         sp.addHandler(new OptimisticLockingHandler(getContainer().getId(), Constants.CONTAINER_OBJECT_TYPE,
             getContainer().getLastModificationDate()));
 
         // add a member retrieving handler
         final BuildRelsExtMemberEntriesFromTaskParamHandlerNew bremeftph =
             new BuildRelsExtMemberEntriesFromTaskParamHandlerNew(getContainer().getId());
         sp.addHandler(bremeftph);
 
         try {
             sp.parse(taskParam);
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final TripleStoreSystemException e) {
             throw e;
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (final Exception e) {
             throw new SystemException("Should not occure in FedoraContainerHandler.addTocs.", e);
         }
 
         final List<String> memberIds = bremeftph.getMemberIds();
         final Iterator<String> it = memberIds.iterator();
         final String tocContentModel = EscidocConfiguration.getInstance().get("escidoc-core.toc.content-model");
         while (it.hasNext()) {
             final String memberId = it.next();
             final String memberContentModel =
                 this.tripleStoreUtility.getProperty(memberId, TripleStoreUtility.PROP_CONTENT_MODEL_ID);
             if (!tocContentModel.equals(memberContentModel)) {
                 throw new InvalidContentException("Object with id " + memberId + " must have content model "
                     + tocContentModel + '.');
             }
         }
 
         return addMembers(id, taskParam);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param taskParam
      *            taskParam
      * @throws LockingException
      *             e
      * @throws ItemNotFoundException
      *             e
      * @throws InvalidContextStatusException
      *             e
      * @throws InvalidItemStatusException
      *             e
      * @throws SystemException
      *             e
      * @throws ContainerNotFoundException
      *             e
      * @throws InvalidContentException
      *             cf. Interface
      */
     @Override
     public String removeMembers(final String id, final String taskParam) throws LockingException,
         ItemNotFoundException, InvalidContextStatusException, InvalidItemStatusException, SystemException,
         ContainerNotFoundException, InvalidContentException {
         // TODO: implement
         setContainer(id);
 
         try {
             checkLocked();
             try {
                 checkStatusNot(Constants.STATUS_WITHDRAWN);
                 checkStatusNot(Constants.STATUS_RELEASED);
             }
             catch (final InvalidStatusException e) {
                 throw new InvalidStatusException("Members can not be removed, because the "
                     + "container is in an inappropriate state.", e);
             }
 
             final StaxParser sp = new StaxParser();
 
             sp.addHandler(new OptimisticLockingHandler(getContainer().getId(), Constants.CONTAINER_OBJECT_TYPE,
                 getContainer().getLastModificationDate()));
 
             // add a member retrieving handler
             final BuildRelsExtMemberEntriesFromTaskParamHandlerNew bremeftph =
                 new BuildRelsExtMemberEntriesFromTaskParamHandlerNew(getContainer().getId(), "remove");
             sp.addHandler(bremeftph);
 
             sp.parse(taskParam);
 
             final List<String> memberIds = bremeftph.getMemberIdsToRemove();
             if (memberIds.isEmpty()) {
                 return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
             }
             else {
                 // rebuild rels-ext
 
                 final Map<String, List<StartElementWithChildElements>> removeElements =
                     new TreeMap<String, List<StartElementWithChildElements>>();
 
                 final Iterator<String> iterator = memberIds.iterator();
                 final List<StartElementWithChildElements> elementsToRemove =
                     new ArrayList<StartElementWithChildElements>();
                 while (iterator.hasNext()) {
                     final String memberId = iterator.next();
                     final StartElementWithChildElements newContentRelationElement = new StartElementWithChildElements();
                     newContentRelationElement.setLocalName("member");
                     newContentRelationElement.setPrefix(Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
                     newContentRelationElement.setNamespace(Constants.STRUCTURAL_RELATIONS_NS_URI);
                     final Attribute resource =
                         new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX,
                             "info:fedora/" + memberId);
                     newContentRelationElement.addAttribute(resource);
                     newContentRelationElement.setChildrenElements(null);
                     elementsToRemove.add(newContentRelationElement);
 
                 }
                 removeElements.put("/RDF/Description/member", elementsToRemove);
 
                 final byte[] relsExtNewBytes = Utility.updateRelsExt(null, removeElements, null, getContainer(), null);
                 getContainer().setRelsExt(
                     new Datastream(Datastream.RELS_EXT_DATASTREAM, getContainer().getId(), relsExtNewBytes,
                         MimeTypes.TEXT_XML));
 
                 // updateTimeStamp
                 makeVersion("Container.removeMembers");
                 getContainer().persist();
                 fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
 
                 // Also reindex removed members
                 for (final String memberId : memberIds) {
                     fireContainerMembersModified(memberId);
                 }
 
                 return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
 
             }
 
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final TripleStoreSystemException e) {
             throw e;
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (final Exception e) {
             throw new SystemException("Should not occur in FedoraContainerHandler.removeMember.", e);
         }
 
     }
 
     /**
      * @param comment
      *            comment
      * @throws SystemException
      *             e
      */
     private void makeVersion(final String comment) throws SystemException {
         makeVersion(comment, null);
     }
 
     /**
      * @param comment
      *            comment
      * @param newStatus
      *            newStatus
      * @throws SystemException
      *             e
      */
     private void makeVersion(final String comment, final String newStatus) throws SystemException {
         getUtility().makeVersion(comment, newStatus, getContainer());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param taskParam
      *            taskParam
      * @throws SystemException
      *             e
      * @throws ContainerNotFoundException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws ReferencedResourceNotFoundException
      *             e
      * @throws RelationPredicateNotFoundException
      *             e
      * @throws AlreadyExistsException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws MissingElementValueException
      *             e
      * @throws LockingException
      *             e
      * @throws ReadonlyVersionException
      *             e
      * @throws InvalidContentException
      *             cf. Interface
      */
     @Override
     public String addContentRelations(final String id, final String taskParam) throws SystemException,
         ContainerNotFoundException, OptimisticLockingException, ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, AlreadyExistsException, InvalidStatusException,
         MissingElementValueException, LockingException, ReadonlyVersionException, InvalidContentException,
         XmlCorruptedException {
 
         setContainer(id);
         checkLatestVersion();
         final DateTime startTimestamp = getContainer().getLastFedoraModificationDate();
 
         checkLocked();
         checkStatusNot(Constants.STATUS_WITHDRAWN);
 
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(taskParam);
         Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Container " + id);
 
         final StaxParser sp = new StaxParser();
 
         final ContentRelationsAddHandler2Edition addHandler =
             new ContentRelationsAddHandler2Edition(sp, getContainer().getId());
         sp.addHandler(addHandler);
         try {
             sp.parse(taskParam);
             sp.clearHandlerChain();
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e.getMessage(), e);
         }
         catch (final ContextNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyAttributeViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingMdRecordException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final OrganizationalUnitNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContentModelNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyElementViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final LockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingAttributeValueException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContentRelationNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final PidAlreadyAssignedException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final TmeException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         final List<Map<String, String>> relationsData = addHandler.getRelations();
 
         if (relationsData != null && !relationsData.isEmpty()) {
             final List<StartElementWithChildElements> elements = new ArrayList<StartElementWithChildElements>();
             boolean resourceUpdated = false;
             for (final Map<String, String> relation : relationsData) {
                 resourceUpdated = true;
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
                 elements.add(newContentRelationElement);
             }
             final byte[] relsExtNewBytes = Utility.updateRelsExt(elements, null, null, getContainer(), null);
             try {
 
                 getContainer().setRelsExt(
                     new Datastream(Datastream.RELS_EXT_DATASTREAM, getContainer().getId(), relsExtNewBytes,
                         MimeTypes.TEXT_XML));
                 final DateTime endTimestamp = getContainer().getLastFedoraModificationDate();
                 if (resourceUpdated || !startTimestamp.isEqual(endTimestamp)) {
                     makeVersion("Container.addContentRelations");
                     getContainer().persist();
                     fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
                 }
             }
             catch (final MissingMethodParameterException e) {
                 throw new IntegritySystemException(e.getMessage(), e);
             }
         }
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      *            id
      * @param param
      *            param
      * @throws SystemException
      *             e
      * @throws OptimisticLockingException
      *             e
      * @throws InvalidStatusException
      *             e
      * @throws ContentRelationNotFoundException
      *             e
      * @throws LockingException
      *             e
      * @throws ContainerNotFoundException
      *             e
      */
     @Override
     public String removeContentRelations(final String id, final String param) throws SystemException,
         ContainerNotFoundException, OptimisticLockingException, InvalidStatusException, MissingElementValueException,
         ContentRelationNotFoundException, LockingException, ReadonlyVersionException, XmlCorruptedException {
 
         setContainer(id);
         checkLatestVersion();
         final DateTime startTimestamp = getContainer().getLastFedoraModificationDate();
 
         checkLocked();
         checkStatusNot(Constants.STATUS_WITHDRAWN);
 
         final TaskParamHandler taskParameter = XmlUtility.parseTaskParam(param);
         Utility.checkOptimisticLockingCriteria(getContainer().getLastModificationDate(), taskParameter
             .getLastModificationDate(), "Container " + id);
 
         final StaxParser sp = new StaxParser();
 
         final ContentRelationsRemoveHandler2Edition removeHandler =
             new ContentRelationsRemoveHandler2Edition(sp, getContainer().getId());
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
             for (final Object anEntrySet : entrySet) {
                 final Entry entry = (Entry) anEntrySet;
                 final String predicateValue = (String) entry.getKey();
                 final List<StartElementWithChildElements> elements =
                     (List<StartElementWithChildElements>) entry.getValue();
                 toRemove.put("/RDF/Description/" + predicateValue, elements);
             }
 
             final byte[] relsExtNewBytes = Utility.updateRelsExt(null, toRemove, null, getContainer(), null);
             try {
                 getContainer().setRelsExt(
                     new Datastream(Datastream.RELS_EXT_DATASTREAM, getContainer().getId(), relsExtNewBytes,
                         MimeTypes.TEXT_XML));
                 final DateTime endTimestamp = getContainer().getLastFedoraModificationDate();
                 if (resourceUpdated || !startTimestamp.isEqual(endTimestamp)) {
                     makeVersion("Container.removeContentRelations");
                     getContainer().persist();
                 }
                 fireContainerModified(getContainer().getId(), retrieve(getContainer().getId()));
             }
             catch (final MissingMethodParameterException e) {
                 throw new IntegritySystemException(e.getMessage(), e);
             }
         }
         return getUtility().prepareReturnXmlFromLastModificationDate(getContainer().getLastModificationDate());
     }
 
     /**
      * Retrieve all content relation in which the current resource is subject or object.
      * 
      * @param id
      *            container id
      * @return list of content relations
      * @throws ContainerNotFoundException
      *             Thrown if an item with the specified id could not be found.
      * @throws SystemException
      *             If an error occurs.
      */
     private String retrieveContentRelations(final String id) throws ContainerNotFoundException, SystemException {
         final Map<String, String[]> filterParams = new HashMap<String, String[]>();
 
         setContainer(id);
         filterParams.put("query", new String[] { "\"/subject/id\"=" + getContainer().getId() + " or "
             + "\"/subject/id\"=" + getContainer().getFullId() + " or " + "\"/object/id\"=" + getContainer().getId()
             + " or " + "\"/object/id\"=" + getContainer().getFullId() });
 
         final String searchResponse =
             contentRelationHandler.retrieveContentRelations(new LuceneRequestParameters(filterParams));
         return transformSearchResponse2relations(searchResponse);
 
     }
 
     @PostConstruct
     private void init() {
         addContainerListener(indexingHandler);
         addContainerMemberListener(indexingHandler);
     }
 
     /**
      * Check if locked is set to false. If it set to true a LockingException is thrown.
      * 
      * @param locked
      *            Indicates if the object is locked.
      * @param method
      *            The method which shall be executed.
      * @param label
      *            The label identifying the object.
      * @param lockOwner
      *            The lock owner if there was one found.
      * @return true if the object is not locked.
      * @throws LockingException
      *             Thrown if the object is locked.
      */
     private static boolean checkUnlocked(
         final boolean locked, final String method, final String label, final String lockOwner) throws LockingException {
         if (locked) {
             throw new LockingException(method + " failed!" + label + " is locked by " + lockOwner + '.');
         }
         return true;
     }
 }
