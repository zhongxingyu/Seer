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
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.item.Component;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.notfound.FileNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.AddNewSubTreesToDatastream;
 import de.escidoc.core.common.util.stax.handler.MultipleExtractor;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.FoXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithChildElements;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 import de.escidoc.core.om.business.stax.handler.item.ComponentMetadataHandler;
 import de.escidoc.core.om.business.stax.handler.item.OneComponentContentHandler;
 import de.escidoc.core.om.business.stax.handler.item.OneComponentPropertiesHandler;
 import de.escidoc.core.om.business.stax.handler.item.OneComponentTitleHandler;
 import org.escidoc.core.services.fedora.IngestPathParam;
 import org.escidoc.core.services.fedora.IngestQueryParam;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.xml.stream.XMLStreamException;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Contains methods pertaining creation of an item. Is extended at least by FedoraItemHandler.
  *
  * @author Michael Schneider
  */
 public class ItemHandlerCreate extends ItemResourceListener {
 
     private static final String DATASTREAM_CONTENT = "content";
 
     private static final Pattern PATTERN_INVALID_FOXML =
         Pattern.compile("fedora.server.errors.ObjectValidityException");
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ItemHandlerCreate.class);
 
     /**
      * Render RELS-EXT of a Component.
      *
      * @param id         Objid of Component.
      * @param properties Component properties
      * @param inCreate   Set true if Component is to create, false if it's an update.
      * @return RELS-EXT XML representation of Component
      * @throws WebserverSystemException Thrown in case of internal error.
      */
     protected String getComponentRelsExtWithVelocity(
         final String id, final Map<String, String> properties, final boolean inCreate) throws WebserverSystemException {
 
         return getFoxmlRenderer().renderComponentRelsExt(id, properties, inCreate);
     }
 
     /**
      * Get FoXML for Component (rendered with Velocity).
      *
      * @param id                 Objid of Component.
      * @param contentMimeType    MIME type of content.
      * @param dataStreams        Map with data (chaotic structure)
      * @param metadataAttributes Map with attributes of md-records.
      * @param nsUri              Name space URI
      * @param storage            Type of storage.
      * @return FoXML representation of Component
      */
     private String getComponentFoxmlWithVelocity(
         final String id, final String contentMimeType, final Map dataStreams,
         final Map<String, Map<String, String>> metadataAttributes, final String nsUri, final String storage)
         throws WebserverSystemException, EncodingSystemException, InvalidContentException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         // dc-mapping prototyping
         if (dataStreams.containsKey(XmlUtility.NAME_MDRECORDS)
             && ((Map) dataStreams.get(XmlUtility.NAME_MDRECORDS)).containsKey(Elements.MANDATORY_MD_RECORD_NAME)) {
             final String dcXml;
             try {
                 // no content model id for component dc-mapping, default mapping
                 // should be applied
                 dcXml =
                     XmlUtility.createDC(nsUri, ((ByteArrayOutputStream) ((Map) dataStreams
                         .get(XmlUtility.NAME_MDRECORDS)).get(Elements.MANDATORY_MD_RECORD_NAME))
                         .toString(XmlUtility.CHARACTER_ENCODING), id, null);
             }
             catch (final UnsupportedEncodingException e) {
                 throw new EncodingSystemException(e.getMessage(), e);
             }
             if (dcXml != null) {
                 values.put(XmlTemplateProvider.DC, dcXml);
             }
         }
         values.put(XmlTemplateProvider.OBJID, id);
         values.put(XmlTemplateProvider.TITLE, "Component " + id);
 
         if (dataStreams.get(Datastream.RELS_EXT_DATASTREAM) != null) {
             values.put(XmlTemplateProvider.RELS_EXT, dataStreams.get(Datastream.RELS_EXT_DATASTREAM));
         }
 
         if (dataStreams.get("uploadUrl") != null) {
             values.put(XmlTemplateProvider.MIME_TYPE, contentMimeType);
             final String theUrl = Utility.processUrl((String) dataStreams.get("uploadUrl"), null, null);
             values.put(XmlTemplateProvider.REF, theUrl);
             values.put(XmlTemplateProvider.REF_TYPE, "URL");
             if (storage.equals(de.escidoc.core.common.business.fedora.Constants.STORAGE_EXTERNAL_URL)) {
                 values.put(XmlTemplateProvider.CONTROL_GROUP, "R");
             }
             else if (storage.equals(de.escidoc.core.common.business.fedora.Constants.STORAGE_EXTERNAL_MANAGED)) {
                 values.put(XmlTemplateProvider.CONTROL_GROUP, "E");
             }
             else if (storage.equals(de.escidoc.core.common.business.fedora.Constants.STORAGE_INTERNAL_MANAGED)) {
                 values.put(XmlTemplateProvider.CONTROL_GROUP, "M");
             }
             values.put(XmlTemplateProvider.REF_TYPE, "URL");
             values.put(XmlTemplateProvider.CONTENT_CHECKSUM_ALGORITHM, EscidocConfiguration.getInstance().get(
                 EscidocConfiguration.ESCIDOC_CORE_OM_CONTENT_CHECKSUM_ALGORITHM, "DISABLED"));
 
         }
 
         if (dataStreams.get(FoXmlProvider.DATASTREAM_MD_RECORDS) != null) {
             final Map mdRecordsStreams = (Map) dataStreams.get(FoXmlProvider.DATASTREAM_MD_RECORDS);
             if (!mdRecordsStreams.isEmpty()) {
                 final Collection<Map<String, String>> mdRecords =
                     new ArrayList<Map<String, String>>(mdRecordsStreams.size());
                 values.put(XmlTemplateProvider.MD_RECORDS, mdRecords);
 
                 for (final Object o : mdRecordsStreams.keySet()) {
                     final String key = (String) o;
                     final ByteArrayOutputStream mdRecordStream = (ByteArrayOutputStream) mdRecordsStreams.get(key);
 
                     final Map<String, String> mdRecord = new HashMap<String, String>();
 
                     final Map<String, String> mdAttributes = metadataAttributes.get(key);
                     String schema = null;
                     String type = null;
                     if (mdAttributes != null) {
                         schema = mdAttributes.get("schema");
                         type = mdAttributes.get("type");
                     }
 
                     mdRecord.put(XmlTemplateProvider.MD_RECORD_SCHEMA, schema);
                     mdRecord.put(XmlTemplateProvider.MD_RECORD_TYPE, type);
                     mdRecord.put(XmlTemplateProvider.MD_RECORD_NAME, key);
                     try {
                         mdRecord.put(XmlTemplateProvider.MD_RECORD_CONTENT, mdRecordStream
                             .toString(XmlUtility.CHARACTER_ENCODING));
 
                     }
                     catch (final UnsupportedEncodingException e) {
                         throw new EncodingSystemException(e);
                     }
                     mdRecords.add(mdRecord);
                 }
             }
         }
         return getFoxmlRenderer().renderComponent(values);
     }
 
     /**
      * Get WOV as XML representation.
      *
      * @return XML representation of WOV
      */
     protected String getWovDatastream(
         final String id, final String title, final String versionNo, final String lastModificationDate,
         final String versionStatus, final String comment) throws WebserverSystemException {
         return getFoxmlRenderer().renderWov(id, title, versionNo, lastModificationDate, versionStatus, comment);
 
     }
 
     /**
      * Add a component to the item.
      *
      * @param xmlData The component xml.
      * @return The xml representation of the component after creation.
      * @throws SystemException              Thrown in case of an internal system error.
      * @throws XmlCorruptedException        If xml data is corrupt.
      * @throws XmlSchemaValidationException If xml schema validation fails.
      * @throws LockingException             If the item is locked and the current user is not the one who locked it.
      * @throws InvalidStatusException       If the item is not in a status to add a component.
      * @throws FileNotFoundException        If binary content can not be retrieved.
      * @throws MissingElementValueException If a required value is missing in xml data.
      * @throws ReadonlyElementViolationException
      *                                      If a read-only Element is set.
      * @throws ReadonlyAttributeViolationException
      *                                      If a read-only attribute is set.
      * @throws InvalidContentException      If there is invalid content in xml data.
      * @throws MissingContentException      If some required content is missing in xml data.
      */
     public String addComponent(final String xmlData) throws SystemException, FileNotFoundException,
         MissingElementValueException, ReadonlyElementViolationException, ReadonlyAttributeViolationException,
         InvalidContentException, MissingContentException, XmlParserSystemException, WebserverSystemException {
 
         // TODO move all precondition checks to service method
         // checkLocked();
         // checkReleased();
 
         final String componentId = getIdProvider().getNextPid();
         final StaxParser sp = new StaxParser();
         // find out the creator of the component
 
         // add Handler to the StaxParser to split the xml stream
         // in to data streams and modify these datastreams
 
         final List<DefaultHandler> handlerChain = new ArrayList<DefaultHandler>();
         // TODO Einkommentieren
         // OptimisticLockingHandler lockingHandler = new
         // OptimisticLockingHandler(id, sp);
         // handlerChain.add(lockingHandler);
 
         final OneComponentPropertiesHandler componentPropertiesHandler = new OneComponentPropertiesHandler(sp);
         handlerChain.add(componentPropertiesHandler);
         final OneComponentContentHandler contentHandler = new OneComponentContentHandler(sp);
         handlerChain.add(contentHandler);
         final OneComponentTitleHandler titleHandler = new OneComponentTitleHandler(sp);
         handlerChain.add(titleHandler);
         final ComponentMetadataHandler cmh = new ComponentMetadataHandler(sp, "/component");
         final List<String> pids = new ArrayList<String>();
         pids.add(componentId);
         cmh.setObjids(pids);
         handlerChain.add(cmh);
         final HashMap<String, String> extractPathes = new HashMap<String, String>();
         extractPathes.put("/component/content", null);
         extractPathes.put("/component/md-records/md-record", "name");
         final List<String> componentPid = new ArrayList<String>();
         componentPid.add(componentId);
         final MultipleExtractor me = new MultipleExtractor(extractPathes, sp);
         me.setPids(componentPid);
         handlerChain.add(me);
         sp.setHandlerChain(handlerChain);
 
         try {
             sp.parse(xmlData);
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (final MissingElementValueException e) {
             throw e;
         }
         catch (final ReadonlyElementViolationException e) {
             throw e;
         }
         catch (final ReadonlyAttributeViolationException e) {
             throw e;
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final MissingContentException e) {
             throw e;
         }
         catch (final WebserverSystemException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         // reset StaxParser
         sp.clearHandlerChain();
         final Map<String, String> componentBinary = contentHandler.getComponentBinary();
         // get modified data streams
         final Map streams = me.getOutputStreams();
         final Map<String, String> properties = componentPropertiesHandler.getProperties();
 
         properties.put(TripleStoreUtility.PROP_CREATED_BY_ID, getUtility().getCurrentUserId());
         properties.put(TripleStoreUtility.PROP_CREATED_BY_TITLE, getUtility().getCurrentUserRealName());
         final Map components = (Map) streams.get("components");
         final Map componentStreams = (Map) components.get(componentId);
         final Map<String, Map<String, String>> componentMdAttributes = cmh.getMetadataAttributes().get(componentId);
         final String escidocMdNsUri = cmh.getNamespacesMap().get(componentId);
         if (componentBinary.get("storage") == null) {
             throw new InvalidContentException("The attribute 'storage' of the element " + "'content' is missing.");
         }
         if (componentBinary.get(DATASTREAM_CONTENT) != null
             && (componentBinary.get("storage").equals(
                 de.escidoc.core.common.business.fedora.Constants.STORAGE_EXTERNAL_URL) || componentBinary
                 .get("storage").equals(de.escidoc.core.common.business.fedora.Constants.STORAGE_EXTERNAL_MANAGED))) {
             throw new InvalidContentException(
                 "The component section 'content' with the attribute 'storage' set to 'external-url' "
                     + "or 'external-managed' may not have an inline content.");
         }
         handleComponent(componentId, properties, componentBinary, componentStreams, componentMdAttributes,
             escidocMdNsUri);
 
         final AddNewSubTreesToDatastream addNewEntriesHandler = new AddNewSubTreesToDatastream("/RDF", sp);
 
         final StartElement pointer = new StartElement();
         pointer.setLocalName("Description");
         pointer.setPrefix(Constants.RDF_NAMESPACE_PREFIX);
         pointer.setNamespace(Constants.RDF_NAMESPACE_URI);
 
         addNewEntriesHandler.setPointerElement(pointer);
         final StartElementWithChildElements newComponentIdElement = new StartElementWithChildElements();
         newComponentIdElement.setLocalName("component");
         newComponentIdElement.setPrefix(Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         newComponentIdElement.setNamespace(Constants.STRUCTURAL_RELATIONS_NS_URI);
         final Attribute resource =
             new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX, "info:fedora/"
                 + componentId);
         newComponentIdElement.addAttribute(resource);
         newComponentIdElement.setChildrenElements(null);
         final List<StartElementWithChildElements> elements = new ArrayList<StartElementWithChildElements>();
 
         elements.add(newComponentIdElement);
         addNewEntriesHandler.setSubtreeToInsert(elements);
         sp.addHandler(addNewEntriesHandler);
 
         try {
             sp.parse(getItem().getRelsExt().getStream());
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e.getMessage(), e);
         }
         catch (final Exception e) {
             throw new WebserverSystemException(e);
         }
         sp.clearHandlerChain();
         final ByteArrayOutputStream relsExtNew = addNewEntriesHandler.getOutputStreams();
         getItem().setRelsExt(relsExtNew);
         this.getFedoraServiceClient().sync();
         try {
             this.getTripleStoreUtility().reinitialize();
         }
         catch (TripleStoreSystemException e) {
             throw new FedoraSystemException("Error on reinitializing triple store.", e);
         }
         final String component;
         try {
             final Component c = new Component(componentId, getItem().getId(), null);
             getItem().addComponent(c);
 
            component = renderComponent(componentId, true);
         }
         catch (final ResourceNotFoundException e) {
             throw new IntegritySystemException("Component not found.", e);
         }
         return component;
 
     }
 
     /**
      * Create a new Component.
      * <p/>
      * ATTENTION: Object is created but sync is not called!
      *
      * @param xmlData eSciDoc XML representation of Component.
      * @return objid of the new Component
      */
     public String createComponent(final String xmlData) throws SystemException, FileNotFoundException,
         MissingElementValueException, ReadonlyElementViolationException, ReadonlyAttributeViolationException,
         InvalidContentException, MissingContentException, XmlParserSystemException, WebserverSystemException {
 
         final StaxParser sp = new StaxParser();
         // find out the creator of the component
         final List<DefaultHandler> handlerChain = new ArrayList<DefaultHandler>();
 
         final OneComponentPropertiesHandler componentPropertiesHandler = new OneComponentPropertiesHandler(sp);
         handlerChain.add(componentPropertiesHandler);
         final OneComponentContentHandler contentHandler = new OneComponentContentHandler(sp);
         handlerChain.add(contentHandler);
         final OneComponentTitleHandler titleHandler = new OneComponentTitleHandler(sp);
         handlerChain.add(titleHandler);
         final ComponentMetadataHandler cmh = new ComponentMetadataHandler(sp, "/component");
         final List<String> pids = new ArrayList<String>();
 
         final String componentId = getIdProvider().getNextPid();
         pids.add(componentId);
         cmh.setObjids(pids);
         handlerChain.add(cmh);
         final HashMap<String, String> extractPathes = new HashMap<String, String>();
 
         // extractPathes.put("/component/properties", null);
         extractPathes.put("/component/content", null);
         extractPathes.put("/component/md-records/md-record", "name");
         final List<String> componentIds = new ArrayList<String>();
         componentIds.add(componentId);
         final MultipleExtractor me = new MultipleExtractor(extractPathes, sp);
         me.setPids(componentIds);
         handlerChain.add(me);
         sp.setHandlerChain(handlerChain);
 
         try {
             sp.parse(xmlData);
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (final MissingElementValueException e) {
             throw e;
         }
         catch (final ReadonlyElementViolationException e) {
             throw e;
         }
         catch (final ReadonlyAttributeViolationException e) {
             throw e;
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final MissingContentException e) {
             throw e;
         }
         catch (final WebserverSystemException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         // reset StaxParser
         sp.clearHandlerChain();
         final Map<String, String> componentBinary = contentHandler.getComponentBinary();
         // get modified data streams
         final Map<String, Object> streams = me.getOutputStreams();
         final Map<String, String> properties = componentPropertiesHandler.getProperties();
 
         properties.put(TripleStoreUtility.PROP_CREATED_BY_ID, getUtility().getCurrentUserId());
         properties.put(TripleStoreUtility.PROP_CREATED_BY_TITLE, getUtility().getCurrentUserRealName());
         final Map components = (Map) streams.get("components");
         final Map componentStreams = (Map) components.get(componentId);
         final Map<String, Map<String, String>> componentMdAttributes = cmh.getMetadataAttributes().get(componentId);
         final String escidocMdNsUri = cmh.getNamespacesMap().get(componentId);
         if (componentBinary.get("storage") == null) {
             throw new InvalidContentException("The attribute 'storage' of the element " + "'content' is missing.");
         }
         if (componentBinary.get(DATASTREAM_CONTENT) != null
             && (componentBinary.get("storage").equals(
                 de.escidoc.core.common.business.fedora.Constants.STORAGE_EXTERNAL_URL) || componentBinary
                 .get("storage").equals(de.escidoc.core.common.business.fedora.Constants.STORAGE_EXTERNAL_MANAGED))) {
             throw new InvalidContentException(
                 "The component section 'content' with the attribute 'storage' set to 'external-url' "
                     + "or 'external-managed' may not have an inline content.");
         }
         handleComponent(componentId, properties, componentBinary, componentStreams, componentMdAttributes,
             escidocMdNsUri);
 
         return componentId;
     }
 
     /**
      * The method prepares datastreams for a fedora object which will represent a component. - it calls a jhof service
      * to get a technical metadata about a binary data of the component. - in case that a binary data is an inline
      * binary stream, the method uploads this binary stream in to a staging area using a staging component in order to
      * get an access url. - then it calls the method buildComponentFoxml() with following parameter: provided
      * componentStreams, an access url to a binary content and a component title - store retrieved component FOXML in to
      * Fedora.
      *
      * @param componentId        component id
      * @param properties         Property Map
      * @param binaryContent      data of the binary data stream
      * @param datastreams        HashMap with component data streams
      * @param mdRecordAttributes Attributes of eSciDoc XML md-record element.
      * @param nsUri              Name space URI
      * @throws IntegritySystemException If the integrity of the repository is violated.
      * @throws EncodingSystemException  If encoding fails.
      * @throws WebserverSystemException In case of an internal error.
      * @throws FileNotFoundException    If binary content can not be retrieved.
      */
     protected void handleComponent(
         final String componentId, final Map<String, String> properties, final Map<String, String> binaryContent,
         final Map datastreams, final Map<String, Map<String, String>> mdRecordAttributes, final String nsUri)
         throws FileNotFoundException, WebserverSystemException, IntegritySystemException, FedoraSystemException {
 
         if (datastreams.containsKey(DATASTREAM_CONTENT)) {
             datastreams.remove(DATASTREAM_CONTENT);
         }
         String mimeType = properties.get(TripleStoreUtility.PROP_MIME_TYPE);
         if (mimeType == null || mimeType.length() == 0) {
             mimeType = FoXmlProvider.MIME_TYPE_APPLICATION_OCTET_STREAM;
         }
         datastreams.put(Datastream.RELS_EXT_DATASTREAM, getComponentRelsExtWithVelocity(componentId, properties, true));
         if (datastreams.get(FoXmlProvider.DATASTREAM_MD_RECORDS) == null) {
             datastreams.put(FoXmlProvider.DATASTREAM_MD_RECORDS, new HashMap());
         }
         String uploadUrl = binaryContent.get(FoXmlProvider.DATASTREAM_UPLOAD_URL);
         if (binaryContent.get(DATASTREAM_CONTENT) != null) {
             final String fileName = "component " + componentId;
             uploadUrl = uploadBase64EncodedContent(binaryContent.get(DATASTREAM_CONTENT), fileName, mimeType);
         }
         datastreams.put(FoXmlProvider.DATASTREAM_UPLOAD_URL, uploadUrl);
         try {
             final String componentFoxml =
                 getComponentFoxmlWithVelocity(componentId, mimeType, datastreams, mdRecordAttributes, nsUri,
                     binaryContent.get(FoXmlProvider.DATASTREAM_STORAGE_ATTRIBUTE));
             final IngestPathParam path = new IngestPathParam();
             final IngestQueryParam query = new IngestQueryParam();
             this.getFedoraServiceClient().ingest(path, query, componentFoxml);
         }
         catch (final Exception e) {
             final Matcher invalidFoxml = PATTERN_INVALID_FOXML.matcher(e.getCause().getMessage());
             if (invalidFoxml.find()) {
                 throw new IntegritySystemException(e);
             }
             handleFedoraUploadError(uploadUrl, e);
         }
     }
 }
