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
 /**
  *
  */
 package de.escidoc.core.cmm.business.fedora;
 
 import de.escidoc.core.cmm.business.fedora.contentModel.ContentModelHandlerRetrieve;
 import de.escidoc.core.cmm.business.interfaces.ContentModelHandlerInterface;
 import de.escidoc.core.cmm.business.stax.handler.contentModel.ContentModelCreateHandler;
 import de.escidoc.core.cmm.business.stax.handler.contentModel.ContentModelPropertiesHandler;
 import de.escidoc.core.cmm.business.stax.handler.contentModel.MdRecordDefinitionHandler;
 import de.escidoc.core.cmm.business.stax.handler.contentModel.ResourceDefinitionHandler;
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.business.fedora.resources.StatusType;
 import de.escidoc.core.common.business.fedora.resources.create.ContentModelCreate;
 import de.escidoc.core.common.business.fedora.resources.create.ContentStreamCreate;
 import de.escidoc.core.common.business.fedora.resources.create.MdRecordCreate;
 import de.escidoc.core.common.business.fedora.resources.create.MdRecordDefinitionCreate;
 import de.escidoc.core.common.business.fedora.resources.create.ResourceDefinitionCreate;
 import de.escidoc.core.common.business.fedora.resources.listener.ResourceListener;
 import de.escidoc.core.common.business.filter.SRURequest;
 import de.escidoc.core.common.business.filter.SRURequestParameters;
 import de.escidoc.core.common.business.stax.handler.common.ContentStreamsHandler;
 import de.escidoc.core.common.business.stax.handler.context.DcUpdateHandler;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentStreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
 import de.escidoc.core.common.exceptions.application.violated.ResourceInUseException;
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
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ContentModelFoXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProviderConstants;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithChildElements;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithText;
 import org.escidoc.core.services.fedora.FedoraServiceClient;
 import org.escidoc.core.services.fedora.IngestPathParam;
 import org.escidoc.core.services.fedora.IngestQueryParam;
 import org.escidoc.core.services.fedora.ModifiyDatastreamPathParam;
 import org.escidoc.core.services.fedora.ModifyDatastreamQueryParam;
 import org.escidoc.core.utils.io.EscidocBinaryContent;
 import org.escidoc.core.utils.io.MimeTypes;
 import org.escidoc.core.utils.io.Stream;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.PostConstruct;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author Frank Schwichtenberg
  */
 @Service("business.FedoraContentModelHandler")
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class FedoraContentModelHandler extends ContentModelHandlerRetrieve implements ContentModelHandlerInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(FedoraContentModelHandler.class);
 
     private final Collection<ResourceListener> contentModelListeners = new ArrayList<ResourceListener>();
 
     @Autowired
     @Qualifier("de.escidoc.core.common.business.filter.SRURequest")
     private SRURequest sruRequest;
 
     @Autowired
     @Qualifier("common.business.indexing.IndexingHandler")
     private ResourceListener indexingHandler;
 
     @Autowired
     private FedoraServiceClient fedoraServiceClient;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected FedoraContentModelHandler() {
     }
 
     @PostConstruct
     private void init() {
         contentModelListeners.add(this.indexingHandler);
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public String retrieve(final String id) throws ContentModelNotFoundException, SystemException {
 
         setContentModel(id);
         return render();
     }
 
     @Override
     public String retrieveContentStream(final String id, final String name) throws ContentModelNotFoundException,
         TripleStoreSystemException, IntegritySystemException, WebserverSystemException, FedoraSystemException {
         setContentModel(id);
         return renderContentStream(name, true);
     }
 
     @Override
     public EscidocBinaryContent retrieveContentStreamContent(final String id, final String name)
         throws ContentModelNotFoundException, ContentStreamNotFoundException, InvalidStatusException,
         FedoraSystemException, TripleStoreSystemException, WebserverSystemException, IntegritySystemException {
 
         setContentModel(id);
         if (getContentModel().isWithdrawn()) {
             final String msg =
                 "The object is in state '" + Constants.STATUS_WITHDRAWN + "'. Content is not accessible.";
             throw new InvalidStatusException(msg);
         }
 
         return getContentStream(name);
     }
 
     private EscidocBinaryContent getContentStream(final String name) throws WebserverSystemException {
 
         final Datastream cs = getContentModel().getContentStream(name);
 
         final EscidocBinaryContent bin = new EscidocBinaryContent();
         final String fileName = cs.getLabel();
         bin.setFileName(fileName);
 
         final String mimeType = cs.getMimeType();
         bin.setMimeType(mimeType);
 
         if ("R".equals(cs.getControlGroup())) {
             bin.setRedirectUrl(cs.getLocation());
         }
         else {
             final Stream stream =
                 this.fedoraServiceClient.getBinaryContent(getContentModel().getId(), name, getContentModel()
                     .getVersionDate());
             try {
                 bin.setContent(stream.getInputStream());
             }
             catch (IOException e) {
                 throw new WebserverSystemException("Error on loading binary content.", e);
             }
         }
         return bin;
     }
 
     private EscidocBinaryContent retrieveOtherContent(final String dsName) throws WebserverSystemException {
         final Datastream ds = getContentModel().getOtherStream(dsName);
         return getContent(ds);
     }
 
     private EscidocBinaryContent getContent(final Datastream ds) throws WebserverSystemException {
 
         final EscidocBinaryContent bin = new EscidocBinaryContent();
         final String name = ds.getName();
         final String fileName = ds.getLabel();
         bin.setFileName(fileName);
 
         final String mimeType = ds.getMimeType();
         bin.setMimeType(mimeType);
 
         if ("R".equals(ds.getControlGroup())) {
             bin.setRedirectUrl(ds.getLocation());
         }
         else {
             final Stream stream =
                 this.fedoraServiceClient.getBinaryContent(ds.getParentId(), name, getContentModel().getVersionDate());
             try {
                 bin.setContent(stream.getInputStream());
             }
             catch (IOException e) {
                 throw new WebserverSystemException("Error on loading binary content.", e);
             }
         }
 
         return bin;
 
     }
 
     @Override
     public String retrieveResources(final String id) throws ContentModelNotFoundException, SystemException {
         setContentModel(id);
         return renderResources();
     }
 
     public String retrieveResourceDefinitions(final String id) throws ContentModelNotFoundException,
         TripleStoreSystemException, IntegritySystemException, FedoraSystemException, WebserverSystemException {
         setContentModel(id);
         return renderResourceDefinitions();
     }
 
     public String retrieveResourceDefinition(final String id, final String name) throws ContentModelNotFoundException,
         TripleStoreSystemException, IntegritySystemException, FedoraSystemException, WebserverSystemException {
         setContentModel(id);
         return renderResourceDefinition(name);
     }
 
     @Override
     public EscidocBinaryContent retrieveMdRecordDefinitionSchemaContent(final String id, final String name)
         throws ContentModelNotFoundException, WebserverSystemException, TripleStoreSystemException,
         IntegritySystemException, FedoraSystemException {
 
         setContentModel(id);
         return retrieveOtherContent(name + "_xsd");
     }
 
     @Override
     public EscidocBinaryContent retrieveResourceDefinitionXsltContent(final String id, final String name)
         throws ResourceNotFoundException, FedoraSystemException, WebserverSystemException, TripleStoreSystemException,
         IntegritySystemException {
 
         setContentModel(id);
 
         // xslt is stored in sdef
 
         // sDef ID is build from CM ID and operation name
         final String sDefId =
             "sdef:" + getContentModel().getId().replaceAll(":", Constants.COLON_REPLACEMENT_PID) + '-' + name;
 
         // get the 'xslt' datastream from sDef
         final Datastream ds;
         try {
             ds = new Datastream("xslt", sDefId, null);
         }
         catch (final StreamNotFoundException e) {
             throw new ResourceNotFoundException("No XSLT for operation '" + name + "' in content model " + id + '.', e);
         }
 
         return getContent(ds);
     }
 
     @Override
     public String retrieveVersionHistory(final String id) throws ContentModelNotFoundException,
         EncodingSystemException, IntegritySystemException, FedoraSystemException, WebserverSystemException,
         TripleStoreSystemException {
 
         setContentModel(id);
         final String versionsXml;
 
         try {
             versionsXml =
                 getContentModel().getWov().toStringUTF8().replaceFirst(
                     '<' + Constants.WOV_NAMESPACE_PREFIX + ':' + Elements.ELEMENT_WOV_VERSION_HISTORY,
                     '<' + Constants.WOV_NAMESPACE_PREFIX + ':' + Elements.ELEMENT_WOV_VERSION_HISTORY + " xml:base=\""
                         + XmlUtility.getEscidocBaseUrl() + "\" " + Elements.ATTRIBUTE_LAST_MODIFICATION_DATE + "=\""
                         + getContentModel().getLastModificationDate() + "\" ");
         }
         catch (final StreamNotFoundException e) {
             throw new IntegritySystemException("Version history not found.", e);
         }
 
         return versionsXml;
     }
 
     /**
      * Retrieves a filtered list of Content Models.
      *
      * @param parameters parameters from the SRU request
      * @return Returns XML representation of the list of Content Model objects.
      */
     @Override
     public String retrieveContentModels(final SRURequestParameters parameters) throws WebserverSystemException {
         final StringWriter result = new StringWriter();
 
         if (parameters.isExplain()) {
             sruRequest.explain(result, ResourceType.CONTENT_MODEL);
         }
         else {
             sruRequest.searchRetrieve(result, new ResourceType[] { ResourceType.CONTENT_MODEL }, parameters);
         }
         return result.toString();
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public String create(final String xmlData) throws InvalidContentException, MissingAttributeValueException,
         SystemException, XmlCorruptedException {
 
         final ContentModelCreate contentModel = parseContentModel(xmlData);
 
         // check that the objid was not obtained from the representation
         contentModel.setObjid(null);
 
         contentModel.setIdProvider(getIdProvider());
         validate(contentModel);
         contentModel.persist(true);
         final String objid = contentModel.getObjid();
         final String resultContentModel;
         try {
             resultContentModel = retrieve(objid);
         }
         catch (final ResourceNotFoundException e) {
             final String msg =
                 "The Content Model with id '" + objid + "', which was just created, "
                     + "could not be found for retrieve.";
             throw new IntegritySystemException(msg, e);
         }
         fireContentModelCreated(objid, resultContentModel);
         return resultContentModel;
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public void delete(final String id) throws ContentModelNotFoundException, SystemException, LockingException,
         InvalidStatusException, ResourceInUseException {
 
         setContentModel(id);
         checkLocked();
         if (!(getContentModel().isPending() || getContentModel().isInRevision())) {
             throw new InvalidStatusException("Content Model must be is public status pending or "
                 + "submitted in order to delete it.");
         }
 
         // check if objects refer this content model
         if (this.tripleStoreUtility.hasReferringResource(id)) {
             throw new ResourceInUseException("The content model is referred by "
                 + "an resource and can not be deleted.");
         }
 
         // delete every behavior (sdef, sdep) even those from old versions
         this.fedoraServiceClient.deleteObject(getContentModel().getId());
         this.fedoraServiceClient.sync();
         this.tripleStoreUtility.reinitialize();
         fireContentModelDeleted(getContentModel().getId());
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public String update(final String id, final String xmlData) throws ContentModelNotFoundException,
         OptimisticLockingException, SystemException, ReadonlyVersionException, MissingAttributeValueException,
         InvalidXmlException, InvalidContentException {
 
         setContentModel(id);
         final DateTime startTimestamp = getContentModel().getLastFedoraModificationDate();
         checkLatestVersion();
 
         // parse incomming XML
         final StaxParser sp = new StaxParser();
         // check optimistic locking criteria! and ID in root element?
         sp.addHandler(new OptimisticLockingHandler(getContentModel().getId(), Constants.CONTENT_MODEL_OBJECT_TYPE,
             getContentModel().getLastModificationDate()));
         // get name and description
         final ContentModelPropertiesHandler cmph = new ContentModelPropertiesHandler(sp);
         sp.addHandler(cmph);
         // get md-record definitions
         final MdRecordDefinitionHandler mrdh =
             new MdRecordDefinitionHandler(sp, "/content-model/md-record-definitions");
         sp.addHandler(mrdh);
         // get resource definitions
         final ResourceDefinitionHandler rdh = new ResourceDefinitionHandler(sp, "/content-model/resource-definitions");
         sp.addHandler(rdh);
         // get content-streams
         final ContentStreamsHandler csh = new ContentStreamsHandler(sp, "/content-model/content-streams");
         sp.addHandler(csh);
 
         try {
             sp.parse(xmlData);
         }
         catch (final WebserverSystemException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final InvalidXmlException e) {
             throw e;
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
 
         final String description = getContentModel().getDescription() != null ? getContentModel().getDescription() : "";
 
         if (!getContentModel().getTitle().equals(cmph.getProperties().getObjectProperties().getTitle())
             || !description.equals(cmph.getProperties().getObjectProperties().getDescription())) {
             // update DC (title, description)
             final Datastream dc = getContentModel().getDc();
             final StaxParser dcParser = new StaxParser();
 
             final TreeMap<String, StartElementWithText> updateElementsDc = new TreeMap<String, StartElementWithText>();
 
             updateElementsDc.put(Elements.ELEMENT_DC_TITLE, new StartElementWithText(Elements.ELEMENT_DC_TITLE,
                 Constants.DC_NS_URI, Constants.DC_NS_PREFIX, cmph.getProperties().getObjectProperties().getTitle(),
                 null));
 
             updateElementsDc.put(Elements.ELEMENT_DC_DESCRIPTION, new StartElementWithText(
                 Elements.ELEMENT_DC_DESCRIPTION, Constants.DC_NS_URI, Constants.DC_NS_PREFIX, cmph
                     .getProperties().getObjectProperties().getDescription(), null));
 
             final DcUpdateHandler dcUpdateHandler = new DcUpdateHandler(updateElementsDc, dcParser);
 
             dcParser.addHandler(dcUpdateHandler);
             final HashMap<String, String> extractPathes = new HashMap<String, String>();
             final MultipleExtractor me = new MultipleExtractor(extractPathes, dcParser);
             extractPathes.put("/dc", null);
             dcParser.addHandler(me);
             final byte[] dcNewBytes;
             try {
                 dcParser.parse(dc.getStream());
                 final ByteArrayOutputStream dcUpdated = (ByteArrayOutputStream) me.getOutputStreams().get("dc");
                 dcNewBytes = dcUpdated.toByteArray();
             }
             catch (final Exception e) {
                 throw new XmlParserSystemException(e);
             }
             final String dcNew;
             try {
                 dcNew = new String(dcNewBytes, XmlUtility.CHARACTER_ENCODING);
             }
             catch (final UnsupportedEncodingException e) {
                 throw new EncodingSystemException(e);
             }
 
             final Datastream newDs;
             try {
                 newDs =
                     new Datastream("DC", getContentModel().getId(), dcNew.getBytes(XmlUtility.CHARACTER_ENCODING),
                         MimeTypes.TEXT_XML);
             }
             catch (final UnsupportedEncodingException e) {
                 throw new WebserverSystemException(e);
             }
             getContentModel().setDc(newDs);
 
         }
 
         final String sdexIdMidfix = getContentModel().getId().replaceAll(":", Constants.COLON_REPLACEMENT_PID) + '-';
         final String sdefIdPrefix = "sdef:" + sdexIdMidfix;
         // String sdepIdPrefix = "sdep:" + sdexIdMidfix;
         final Map<String, ResourceDefinitionCreate> resourceDefinitions = rdh.getResourceDefinitions();
 
         // update RELS-EXT
         // FIXME store operation names in ContentModel and remove and add
         // services in one pass or just remove if really gone
 
         // delete service entries which are in Fedora but not send
         final Map<String, List<StartElementWithChildElements>> deleteFromRelsExt =
             new HashMap<String, List<StartElementWithChildElements>>();
         final List<StartElementWithChildElements> deleteElementList = new ArrayList<StartElementWithChildElements>();
 
         for (final ResourceDefinitionCreate resourceDefinition : getContentModel().getResourceDefinitions().values()) {
             if (!resourceDefinitions.containsKey(resourceDefinition.getName())) {
                 final StartElementWithChildElements element =
                     new StartElementWithChildElements("hasService", Constants.FEDORA_MODEL_NS_URI, null, null, null,
                         null);
                 element.addAttribute(new Attribute("resource", Constants.RDF_NAMESPACE_URI, null, resourceDefinition
                     .getFedoraId(getContentModel().getId())));
                 deleteElementList.add(element);
             }
         }
         deleteFromRelsExt.put("/RDF/Description/hasService", deleteElementList);
         final byte[] tmpRelsExt = Utility.updateRelsExt(null, deleteFromRelsExt, null, getContentModel(), null);
 
         // add services to RELS-EXT
         final List<StartElementWithChildElements> addToRelsExt = new ArrayList<StartElementWithChildElements>();
         for (final ResourceDefinitionCreate resourceDefinition : resourceDefinitions.values()) {
             // FIXME do update existing resource definitions
             if (!getContentModel().getResourceDefinitions().containsKey(resourceDefinition.getName())) {
                 final StartElementWithChildElements hasServiceElement = new StartElementWithChildElements();
                 hasServiceElement.setLocalName("hasService");
                 hasServiceElement.setPrefix(Constants.FEDORA_MODEL_NS_PREFIX);
                 hasServiceElement.setNamespace(Constants.FEDORA_MODEL_NS_URI);
                 final Attribute resource =
                     new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX,
                         Constants.IDENTIFIER_PREFIX + sdefIdPrefix + resourceDefinition.getName());
                 hasServiceElement.addAttribute(resource);
                 addToRelsExt.add(hasServiceElement);
             }
         }
 
         // TODO remove services from RELS-EXT
         // (<hasService
         // rdf:resource="info:fedora/sdef:escidoc_9003-trans"
         // xmlns="info:fedora/fedora-system:def/model#"/>)
         getContentModel().setRelsExt(Utility.updateRelsExt(addToRelsExt, null, tmpRelsExt, getContentModel(), null));
 
         // Metadata Record Definitions
         final List<MdRecordDefinitionCreate> mdRecordDefinitions = mrdh.getMdRecordDefinitions();
         // update DS-COMPOSITE
         final Map<String, Object> valueMap = new HashMap<String, Object>();
         valueMap.put("MD_RECORDS", mdRecordDefinitions);
         final String dsCompositeModelContent =
             ContentModelFoXmlProvider.getInstance().getContentModelDsComposite(valueMap);
         getContentModel().setDsCompositeModel(dsCompositeModelContent);
         // TODO create, delete, update *_XSD datastreams
         for (final MdRecordDefinitionCreate mdRecordDefinition : mdRecordDefinitions) {
             final String name = mdRecordDefinition.getName();
             final String xsdUrl = mdRecordDefinition.getSchemaHref();
             getContentModel().setOtherStream(
                 name + "_xsd",
                 new Datastream(name + "_xsd", getContentModel().getId(), xsdUrl,
                     de.escidoc.core.common.business.fedora.Constants.STORAGE_EXTERNAL_MANAGED, MimeTypes.TEXT_XML));
         }
 
         // Resource Definitions
         // services are already added to RELS-EXT
         // TODO delete sdef+sdep
         // create service definitions and deployments or update xslt
 
         for (final ResourceDefinitionCreate resourceDefinition : resourceDefinitions.values()) {
             final String sdefId = sdefIdPrefix + resourceDefinition.getName();
             if (this.tripleStoreUtility.exists(sdefId)) {
                 // check if href for xslt is changed
                 // /cmm/content-model/escidoc:40013/resource-\
                 // definitions/resource-definition/trans/xslt
                 if (resourceDefinition.getXsltHref().equalsIgnoreCase(
                     "/cmm/content-model/" + getContentModel().getId() + "/resource-definitions/resource-definition/"
                         + resourceDefinition.getName() + "/xslt/content")) {
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Do not update xslt.");
                     }
                 }
                 else {
                     final ModifiyDatastreamPathParam path = new ModifiyDatastreamPathParam(sdefId, "xslt");
                     final ModifyDatastreamQueryParam query = new ModifyDatastreamQueryParam();
                     query.setDsLabel("Transformation instructions for operation '" + resourceDefinition.getName()
                         + "'.");
                     query.setMimeType(MimeTypes.TEXT_XML);
                     query.setDsLocation(resourceDefinition.getXsltHref());
                     this.fedoraServiceClient.modifyDatastream(path, query, null);
                 }
             }
             else {
                 // create
                 final String sdefFoxml = getSDefFoXML(resourceDefinition);
                 final IngestPathParam path = new IngestPathParam();
                 final IngestQueryParam query = new IngestQueryParam();
                 this.fedoraServiceClient.ingest(path, query, sdefFoxml);
                 final String sdepFoxml = getSDepFoXML(resourceDefinition);
                 this.fedoraServiceClient.ingest(path, query, sdepFoxml);
             }
         }
 
         // Content Streams
         final List<ContentStreamCreate> contentStreams = csh.getContentStreams();
         setContentStreams(contentStreams);
 
         /*
          * 
          */
 
         // check if modified
         final String updatedXmlData;
         final DateTime endTimestamp = getContentModel().getLastFedoraModificationDate();
         if (!startTimestamp.isEqual(endTimestamp) || getContentModel().isNewVersion()) {
             // object is modified
             getUtility().makeVersion("ContentModelHandler.update()", null, getContentModel());
             getContentModel().persist();
 
             updatedXmlData = retrieve(getContentModel().getId());
             fireContentModelModified(getContentModel().getId(), updatedXmlData);
         }
         else {
             updatedXmlData = render();
         }
 
         return updatedXmlData;
     }
 
     /**
      * Render service definition FoXML.
      *
      * @param resourceDefinition The resource definition create object.
      * @return FoXML representation of service definition.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      */
     private String getSDefFoXML(final ResourceDefinitionCreate resourceDefinition) throws WebserverSystemException {
         final Map<String, Object> valueMap = new HashMap<String, Object>();
         valueMap.putAll(getBehaviorValues(resourceDefinition));
         return ContentModelFoXmlProvider.getInstance().getServiceDefinitionFoXml(valueMap);
     }
 
     /**
      * Render service deployment FoXML.
      *
      * @param resourceDefinition The resource definition create object.
      * @return FoXML representation of service deployment.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      */
     private String getSDepFoXML(final ResourceDefinitionCreate resourceDefinition) throws WebserverSystemException {
         final Map<String, Object> valueMap = new HashMap<String, Object>();
         valueMap.putAll(getBehaviorValues(resourceDefinition));
         return ContentModelFoXmlProvider.getInstance().getServiceDeploymentFoXml(valueMap);
     }
 
     private Map<String, Object> getBehaviorValues(final ResourceDefinitionCreate resourceDefinition) {
         final Map<String, Object> valueMap = new HashMap<String, Object>();
         valueMap.put(XmlTemplateProviderConstants.BEHAVIOR_CONTENT_MODEL_ID, getContentModel().getId());
         valueMap.put(XmlTemplateProviderConstants.BEHAVIOR_CONTENT_MODEL_ID_UNDERSCORE, getContentModel()
             .getId().replaceAll(":", Constants.COLON_REPLACEMENT_PID));
 
         valueMap.put(XmlTemplateProviderConstants.BEHAVIOR_OPERATION_NAME, resourceDefinition.getName());
         valueMap.put(XmlTemplateProviderConstants.BEHAVIOR_TRANSFORM_MD, resourceDefinition.getMdRecordName());
         valueMap.put(XmlTemplateProviderConstants.BEHAVIOR_XSLT_HREF, resourceDefinition.getXsltHref());
         return valueMap;
     }
 
     /**
      * Creates Stream objects from the values in {@code contentStreamMap} and calls Item.setContentStreams with a
      * HashMap which contains the metadata datastreams as Stream objects.
      * @param contentStreams
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     private void setContentStreams(final Iterable<ContentStreamCreate> contentStreams) throws WebserverSystemException,
         IntegritySystemException, FedoraSystemException {
         final Map<String, Datastream> contentStreamDatastreams = new HashMap<String, Datastream>();
         for (final ContentStreamCreate contentStream : contentStreams) {
             final String name = contentStream.getName();
             final Datastream ds;
             if (contentStream.getContent() != null && contentStream.getContent().getContent() != null) {
                 try {
                     ds =
                         new Datastream(name, getContentModel().getId(), contentStream
                             .getContent().getContent().getBytes(XmlUtility.CHARACTER_ENCODING), contentStream
                             .getMimeType());
                 }
                 catch (final UnsupportedEncodingException e) {
                     throw new WebserverSystemException(e);
                 }
             }
             else if (contentStream.getContent().getDataLocation() != null) {
                 ds =
                     new Datastream(name, getContentModel().getId(), contentStream
                         .getContent().getDataLocation().toString(), contentStream
                         .getContent().getStorageType().getESciDocName(), contentStream.getMimeType());
             }
             else {
                 throw new IntegritySystemException("Content streams has neither href nor content.");
             }
             String title = contentStream.getTitle();
             if (title == null) {
                 title = "";
             }
             ds.setLabel(title.trim());
             contentStreamDatastreams.put(name, ds);
         }
 
         getContentModel().setContentStreams(contentStreamDatastreams);
     }
 
     /**
      * Check if the requested item version is the latest version.
      *
      * @throws ReadonlyVersionException If the requested item version is not the latest one.
      * @throws IntegritySystemException 
      */
     protected void checkLatestVersion() throws ReadonlyVersionException, IntegritySystemException {
         final String thisVersion = getContentModel().getVersionNumber();
         if (thisVersion != null && !thisVersion.equals(getContentModel().getLatestVersionNumber())) {
             throw new ReadonlyVersionException("Only latest version can be modified.");
         }
     }
 
     /**
      * Validate the Content Model structure in general (independent if create or ingest was selected).
      * <p/>
      * Checks if all required values are set and consistent.
      *
      * @param item The item which is to validate.
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidContentException
      */
     private static void validate(final ContentModelCreate item) throws InvalidContentException {
 
         // check public status of Content Model
         final StatusType publicStatus = item.getProperties().getObjectProperties().getStatus();
 
         if (publicStatus != StatusType.PENDING) {
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("New Content Model has to be in public-status '" + StatusType.PENDING + "'.");
             }
             item.getProperties().getObjectProperties().setStatus(StatusType.PENDING);
         }
 
         // validate Metadata Records
         checkMetadataRecords(item);
     }
 
     /**
      * Check if the name attribute of Metadata Records is unique and at least one Metadata Record has the name
      * "escidoc".
      *
      * @param item Item which is to validate.
      * @throws InvalidContentException Thrown if content is invalid.
      */
     private static void checkMetadataRecords(final ContentModelCreate item) throws InvalidContentException {
 
         final List<MdRecordCreate> mdRecords = item.getMetadataRecords();
 
         if (!(mdRecords == null || mdRecords.size() < 1)) {
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
         }
     }
 
     /**
      * @param xml
      * @throws WebserverSystemException       If an error occurs.
      * @throws InvalidContentException        If invalid content is found.
      * @throws MissingAttributeValueException If a required attribute can not be found.
      * @throws XmlParserSystemException       If an unexpected error occurs while parsing.
      * @throws XmlCorruptedException          Thrown if the schema validation of the provided data failed.
      * @return
      */
     private static ContentModelCreate parseContentModel(final String xml) throws WebserverSystemException,
         InvalidContentException, MissingAttributeValueException, XmlParserSystemException, XmlCorruptedException {
 
         final StaxParser sp = new StaxParser();
 
         final ContentModelCreateHandler contentModelHandler = new ContentModelCreateHandler(sp);
         sp.addHandler(contentModelHandler);
 
         try {
             sp.parse(xml);
         }
         catch (final WebserverSystemException e) {
             throw e;
         }
         catch (final MissingAttributeValueException e) {
             throw e;
         }
         catch (final XmlCorruptedException e) {
             throw e;
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException(null, e);
         }
 
         return contentModelHandler.getContentModel();
     }
 
     /**
      * Check if the content model is locked.
      *
      * @throws WebserverSystemException Thrown in case of an internal error.
      * @throws LockingException         If the item is locked and the current user is not the one who locked it.
      */
     protected void checkLocked() throws LockingException, WebserverSystemException {
         if (getContentModel().isLocked() && !getContentModel().getLockOwner().equals(Utility.getCurrentUser()[0])) {
             throw new LockingException("Content Model + " + getContentModel().getId() + " is locked by "
                 + getContentModel().getLockOwner() + '.');
         }
     }
 
     /**
      * Set the SRURequest object.
      *
      * @param sruRequest SRURequest
      */
     public void setSruRequest(final SRURequest sruRequest) {
         this.sruRequest = sruRequest;
     }
 
     @Override
     public String ingest(final String xmlData) throws InvalidContentException, MissingAttributeValueException,
         SystemException, XmlCorruptedException {
 
         final ContentModelCreate cm = parseContentModel(xmlData);
         cm.setIdProvider(getIdProvider());
         validate(cm);
         cm.persist(true);
         final String objid = cm.getObjid();
         try {
             if (EscidocConfiguration.getInstance().getAsBoolean(
                 EscidocConfiguration.ESCIDOC_CORE_NOTIFY_INDEXER_ENABLED)) {
                 fireContentModelCreated(objid, retrieve(objid));
             }
         }
         catch (final ResourceNotFoundException e) {
             throw new IntegritySystemException("The Content Model with id '" + objid + "', which was just ingested, "
                 + "could not be found for retrieve.", e);
         }
         return objid;
     }
 
     /**
      * Notify the listeners that a Content Model was modified.
      *
      * @param id      Content Model id
      * @param xmlData complete Content Model XML
      * @throws SystemException One of the listeners threw an exception.
      */
     private void fireContentModelModified(final String id, final String xmlData) throws SystemException {
         for (final ResourceListener contentModelListener : this.contentModelListeners) {
             contentModelListener.resourceModified(id, xmlData);
         }
     }
 
     /**
      * Notify the listeners that an Content Model was created.
      *
      * @param id      Content Model id
      * @param xmlData complete Content Model XML
      * @throws SystemException One of the listeners threw an exception.
      */
     private void fireContentModelCreated(final String id, final String xmlData) throws SystemException {
         for (final ResourceListener contentModelListener : this.contentModelListeners) {
             contentModelListener.resourceCreated(id, xmlData);
         }
     }
 
     /**
      * Notify the listeners that an Content Model was deleted.
      *
      * @param id Content Model id
      * @throws SystemException One of the listeners threw an exception.
      */
     private void fireContentModelDeleted(final String id) throws SystemException {
         for (final ResourceListener contentModelListener : this.contentModelListeners) {
             contentModelListener.resourceDeleted(id);
         }
     }
 }
