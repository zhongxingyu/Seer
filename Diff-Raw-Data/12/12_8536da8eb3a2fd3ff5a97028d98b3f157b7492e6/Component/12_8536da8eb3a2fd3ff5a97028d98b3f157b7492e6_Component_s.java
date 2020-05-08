 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 /**
  *
  */
 package de.escidoc.core.common.business.fedora.resources.item;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.escidoc.core.services.fedora.UpdateObjectPathParam;
 import org.escidoc.core.services.fedora.UpdateObjectQueryParam;
 import org.escidoc.core.services.fedora.management.DatastreamProfileTO;
 import org.escidoc.core.utils.io.MimeTypes;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import de.escidoc.core.common.business.fedora.Constants;
 import de.escidoc.core.common.business.fedora.Triple;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.GenericResourcePid;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.business.fedora.resources.interfaces.ComponentInterface;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.DcReadHandler;
 import de.escidoc.core.common.util.stax.handler.RelsExtReadHandler;
 import de.escidoc.core.common.util.stax.handler.item.ComponentPropertiesUpdateHandler;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.renderer.VelocityXmlItemFoXmlRenderer;
 import de.escidoc.core.common.util.xml.renderer.interfaces.ItemFoXmlRendererInterface;
 
 /**
  * Component resource of eSciDoc.
  * 
  * @author Frank Schwichtenberg
  */
 @Configurable(preConstruction = true)
 public class Component extends GenericResourcePid implements ComponentInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Component.class);
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     private Datastream dc;
 
     private final Map<String, Datastream> mdRecords = new HashMap<String, Datastream>();
 
     private final String parent;
 
     private Datastream content;
 
     private final DateTime parentVersionDate;
 
     private ItemFoXmlRendererInterface foxmlRenderer;
 
     /**
      * Constructs the Component with the specified id from the Repository. The datastreams are instantiated and
      * retrieved if the related getter is called.
      * 
      * @param id
      *            The id of an item managed in Fedora.
      * @param parentId
      *            The id of the parent object.
      * @param timestamp
      *            The timestamp which specifies the version of the datastreams to retrieve.
      * @throws ResourceNotFoundException
      *             Thrown if the Component resource was not found.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     public Component(final String id, final String parentId, final DateTime timestamp)
         throws ResourceNotFoundException, IntegritySystemException, FedoraSystemException, TripleStoreSystemException,
         XmlParserSystemException, WebserverSystemException {
         super(id);
         this.parent = parentId;
         this.parentVersionDate = timestamp;
         setHref(Constants.COMPONENT_URL_PART + id);
         init();
     }
 
     private void init() throws ResourceNotFoundException, IntegritySystemException, FedoraSystemException,
         TripleStoreSystemException, XmlParserSystemException, WebserverSystemException {
 
         /*
          * There was no StreamNotFoundException thrown before from this method and to avoid such a change, throw a
          * ComponentNotFoundException instead.
          */
         try {
             initDatastreams(getDatastreamProfiles());
         }
         catch (final StreamNotFoundException e) {
             throw new ComponentNotFoundException(e);
         }
         getSomeValuesFromFedora();
         if (!checkResourceType(ResourceType.COMPONENT)) {
             throw new ItemNotFoundException("Component with the provided objid '" + this.getId() + "' does not exit.");
         }
     }
 
     @Override
     protected void initDatastream(final DatastreamProfileTO profile) throws WebserverSystemException,
         FedoraSystemException, TripleStoreSystemException, IntegritySystemException, StreamNotFoundException {
 
         super.initDatastream(profile);
 
         if (profile.getDsAltID().contains(Datastream.METADATA_ALTERNATE_ID)) {
             this.mdRecords.put(profile.getDsID(), new Datastream(profile, getId(), this.parentVersionDate));
         }
         else if (Datastream.RELS_EXT_DATASTREAM.equals(profile.getDsID())) {
            this.relsExt = new Datastream(profile, getId(), this.parentVersionDate);
         }
         else if ("DC".equals(profile.getDsID())) {
             this.dc = new Datastream(profile, getId(), this.parentVersionDate);
         }
         else if ("content".equals(profile.getDsID())) {
             this.content = new Datastream(profile, getId(), this.parentVersionDate);
         }
         else {
             LOGGER.warn("Stream " + getId() + '/' + profile.getDsID() + " not instanziated in Component<init>.");
         }
     }
 
     /**
      * Retrieving some values from Fedora & TripleStore and keep it in internal HashMap.
      * 
      * @throws TripleStoreSystemException
      *             Thrown if request to TripleStore failed.
      * @throws XmlParserSystemException
      *             Thrown if parsing of RELS-EXT failed.
      * @throws WebserverSystemException
      *             Thrown if request to TripleStore failed.
      */
     private void getSomeValuesFromFedora() throws XmlParserSystemException {
 
         final Map<String, String> properties = new HashMap<String, String>();
         properties.putAll(obtainRelsExtValues());
 
         final StaxParser sp = new StaxParser();
         final DcReadHandler dch = new DcReadHandler(sp);
         sp.addHandler(dch);
         try {
             sp.parse(new ByteArrayInputStream(this.getDc().getStream()));
         }
         catch (final Exception e) {
             throw new XmlParserSystemException("Unexpected exception during DC datastream parsing.", e);
         }
 
         properties.put(de.escidoc.core.common.business.Constants.DC_NS_URI + Elements.ELEMENT_DC_TITLE, dch
             .getPropertiesMap().get(Elements.ELEMENT_DC_TITLE));
 
         final String description = dch.getPropertiesMap().get(Elements.ELEMENT_DESCRIPTION);
         if (description != null && !(description.length() == 0)) {
             properties.put(de.escidoc.core.common.business.Constants.DC_NS_URI + Elements.ELEMENT_DESCRIPTION,
                 description);
         }
 
         // }
 
         final String title =
             properties.get(de.escidoc.core.common.business.Constants.DC_NS_URI + Elements.ELEMENT_DC_TITLE);
         if (title == null || title.length() == 0) {
             setTitle("Component " + getId());
         }
         else {
             setTitle(title);
         }
 
         properties.put(Elements.ELEMENT_COMPONENT_CONTENT_CHECKSUM_ALGORITHM, this.content.getChecksumMethod());
         properties.put(Elements.ELEMENT_COMPONENT_CONTENT_CHECKSUM, this.content.getChecksum());
         addResourceProperties(properties);
     }
 
     /**
      * Get Content as Stream.
      * 
      * @return content
      */
     public Datastream getContent() {
         return this.content;
     }
 
     public String getChecksum() throws TripleStoreSystemException, WebserverSystemException {
         return getProperty(Elements.ELEMENT_COMPONENT_CONTENT_CHECKSUM);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.common.business.fedora.resources.GenericResource#setRelsExt
      * (de.escidoc.core.common.business.fedora.datastream.Stream)
      */
     @Override
     public void setRelsExt(final Datastream ds) throws FedoraSystemException, WebserverSystemException {
 
         super.setRelsExt(ds);
         if (this.isNeedSync()) {
             final UpdateObjectPathParam path = new UpdateObjectPathParam(this.parent);
             final UpdateObjectQueryParam query = new UpdateObjectQueryParam();
             query.setLogMessage("touched");
             this.getFedoraServiceClient().updateObject(path, query);
             this.getFedoraServiceClient().sync();
             try {
                 this.tripleStoreUtility.reinitialize();
             }
             catch (final TripleStoreSystemException e) {
                 throw new FedoraSystemException("Error on reinitializing triple store.", e);
             }
         }
     }
 
     public void setContent(final Datastream ds) {
         throw new UnsupportedOperationException("Component.setContent.");
     }
 
     public void notifySetContent() throws FedoraSystemException {
         final UpdateObjectPathParam path = new UpdateObjectPathParam(this.parent);
         final UpdateObjectQueryParam query = new UpdateObjectQueryParam();
         query.setLogMessage("touched");
         this.getFedoraServiceClient().updateObject(path, query);
         this.getFedoraServiceClient().sync();
         try {
             this.tripleStoreUtility.reinitialize();
         }
         catch (final TripleStoreSystemException e) {
             throw new FedoraSystemException("Error on reinitializing triple store.", e);
         }
         this.content = null;
     }
 
     public Datastream getLicenses() {
         throw new UnsupportedOperationException("Licenses are not yet available.");
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.ComponentInterface #getMdRecords()
      */
     @Override
     public Map<String, Datastream> getMdRecords() {
         return this.mdRecords;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.ComponentInterface #setMdRecords(java.util.HashMap)
      */
     @Override
     public void setMdRecords(final Map<String, Datastream> ds) throws FedoraSystemException, WebserverSystemException,
         EncodingSystemException, IntegritySystemException, TripleStoreSystemException, XmlParserSystemException {
         // compare
         // with
         // Item.setMdRecords
         // check if mdRecords is set, contains all metadata
         // datastreams, is equal to given mdRecords and save every
         // changed datastream to fedora
         boolean modified = false;
         // get list of names of data streams with alternateId = "metadata"
         final Set<String> namesInFedora = getMdRecords().keySet();
         // delete data streams which are in fedora but not in mdRecords
         for (final String nameInFedora : namesInFedora) {
             if (!ds.containsKey(nameInFedora)) {
                 final Datastream fedoraDs = getMdRecord(nameInFedora);
                 fedoraDs.delete();
                 if ("escidoc".equals(fedoraDs.getName())) {
                     // Stream dcDs = getDc();
                     final ItemFoXmlRendererInterface iri = new VelocityXmlItemFoXmlRenderer();
                     final String dcContent = iri.renderDefaultDc(getId());
                     final Datastream newDc;
                     try {
                         newDc =
                             new Datastream("DC", getId(), dcContent.getBytes(XmlUtility.CHARACTER_ENCODING),
                                 MimeTypes.TEXT_XML);
                     }
                     catch (final UnsupportedEncodingException e) {
                         throw new EncodingSystemException(e);
                     }
                     setDc(newDc);
                 }
                 modified = true;
             }
         }
         final Iterator<Entry<String, Datastream>> nameIt = ds.entrySet().iterator();
         // create/activate data streams which are in mdRecords but not in fedora
         while (nameIt.hasNext()) {
             final Entry<String, Datastream> mapEntry = nameIt.next();
             final String name = mapEntry.getKey();
             final Datastream currentMdRecord = mapEntry.getValue();
             setMdRecord(name, currentMdRecord);
             if (!namesInFedora.contains(name)) {
                 nameIt.remove();
             }
         }
         if (modified) {
             final UpdateObjectPathParam path = new UpdateObjectPathParam(this.parent);
             final UpdateObjectQueryParam query = new UpdateObjectQueryParam();
             query.setLogMessage("touched");
             this.getFedoraServiceClient().updateObject(path, query);
             this.getFedoraServiceClient().sync();
             try {
                 this.tripleStoreUtility.reinitialize();
             }
             catch (final TripleStoreSystemException e) {
                 throw new FedoraSystemException("Error on reinitializing triple store.", e);
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.ComponentInterface #getMdRecord(java.lang.String)
      */
     @Override
     public Datastream getMdRecord(final String name) {
         return this.mdRecords.get(name);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.ComponentInterface #setMdRecord(java.lang.String,
      * de.escidoc.core.common.business.fedora.datastream.Stream)
      */
     @Override
     public void setMdRecord(final String name, final Datastream ds) throws WebserverSystemException,
         EncodingSystemException, IntegritySystemException, FedoraSystemException, TripleStoreSystemException,
         XmlParserSystemException {
         // check if the metadata datastream is set, is equal to ds and save to
         // fedora
 
         // don't trust the handler
         // ds.addAlternateId("metadata");
         final String type = ds.getAlternateIDs().get(1);
         final String schema = ds.getAlternateIDs().get(2);
         final String mimeType = ds.getMimeType();
         try {
             boolean contentChanged = false;
             boolean isNew = false;
 
             final Datastream curDs = getMdRecord(name);
             if (curDs == null) {
                 isNew = true;
             }
             else {
                 final String curMimeType = curDs.getMimeType();
                 String curType = "";
                 String curSchema = "";
                 final List<String> altIds = curDs.getAlternateIDs();
                 if (altIds.size() > 1) {
                     curType = altIds.get(1);
                     if (altIds.size() > 2) {
                         curSchema = altIds.get(2);
                     }
                 }
                 if (!ds.equals(curDs) || !type.equals(curType) || !schema.equals(curSchema)
                     || !mimeType.equals(curMimeType)) {
                     contentChanged = true;
                 }
             }
 
             if (contentChanged || isNew) {
                 if ("escidoc".equals(name)) {
 
                     final Map<String, String> mdProperties = ds.getProperties();
                     if (mdProperties != null) {
                         // if (mdProperties.get("nsUri") != null) {
                         if (mdProperties.containsKey("nsUri")) {
                             final String nsUri = mdProperties.get("nsUri");
                             // FIXME get content model ID from Item Object (see
                             // Item. and Container.setMdRecord)
 
                             // no content model id for component dc-mapping,
                             // default mapping
                             // should be applied
                             final String dcNewContent = XmlUtility.createDC(nsUri, ds.toStringUTF8(), getId(), null);
                             if (dcNewContent != null && dcNewContent.trim().length() > 0) {
                                 final Datastream dcNew;
                                 try {
                                     dcNew =
                                         new Datastream("DC", getId(), dcNewContent
                                             .getBytes(XmlUtility.CHARACTER_ENCODING), MimeTypes.TEXT_XML);
                                 }
                                 catch (final UnsupportedEncodingException e) {
                                     throw new EncodingSystemException(e);
                                 }
                                 setDc(dcNew);
                             }
                         }
                         else {
                             throw new IntegritySystemException("namespace uri of 'escidoc' metadata"
                                 + " does not set in datastream.");
                         }
                     }
                     else {
                         throw new IntegritySystemException("Properties of 'md-record' datastream"
                             + " with then name 'escidoc' does not exist");
                     }
                 }
 
                 // isNew does not indicate that the datastream does not exist
                 // in fedora, it may be deleted
                 this.mdRecords.put(name, ds);
                 ds.merge();
                 final UpdateObjectPathParam path = new UpdateObjectPathParam(this.parent);
                 final UpdateObjectQueryParam query = new UpdateObjectQueryParam();
                 query.setLogMessage("touched");
                 this.getFedoraServiceClient().updateObject(path, query);
                 this.getFedoraServiceClient().sync();
                 try {
                     this.tripleStoreUtility.reinitialize();
                 }
                 catch (final TripleStoreSystemException e) {
                     throw new FedoraSystemException("Error on reinitializing triple store.", e);
                 }
             }
         }
         catch (final FedoraSystemException e) {
             // this is not an update; its a create
             ds.addAlternateId(type);
             ds.addAlternateId(schema);
             this.mdRecords.put(name, ds);
             ds.persist(false);
             final UpdateObjectPathParam path = new UpdateObjectPathParam(this.parent);
             final UpdateObjectQueryParam query = new UpdateObjectQueryParam();
             query.setLogMessage("touched");
             this.getFedoraServiceClient().updateObject(path, query);
             this.getFedoraServiceClient().sync();
             try {
                 this.tripleStoreUtility.reinitialize();
             }
             catch (final TripleStoreSystemException e2) {
                 throw new FedoraSystemException("Error on reinitializing triple store.", e2);
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource
      * #setRelsExt(de.escidoc.core.common.business.fedora.datastream.Stream)
      */
     public void setDc(final Datastream ds) throws FedoraSystemException, WebserverSystemException,
         XmlParserSystemException {
         final Datastream curDs = getDc();
         if (!ds.equals(curDs)) {
             this.dc = ds;
             ds.merge();
             getSomeValuesFromFedora();
             final UpdateObjectPathParam path = new UpdateObjectPathParam(this.parent);
             final UpdateObjectQueryParam query = new UpdateObjectQueryParam();
             query.setLogMessage("touched");
             this.getFedoraServiceClient().updateObject(path, query);
             this.getFedoraServiceClient().sync();
             try {
                 this.tripleStoreUtility.reinitialize();
             }
             catch (final TripleStoreSystemException e) {
                 throw new FedoraSystemException("Error on reinitializing triple store.", e);
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource #getRelsExt()
      */
     public Datastream getDc() {
 
         return this.dc;
     }
 
     /**
      * @return the href
      * @deprecated This href does not contain the items version number. Use Item.getHref() + Component.getHrefPart()
      *             instead.
      */
     @Override
     @Deprecated
     public String getHref() {
         return de.escidoc.core.common.business.Constants.ITEM_URL_BASE + this.parent + super.getHref();
     }
 
     /**
      * @return the components part of the href, after the item id
      */
     public String getHrefPart() {
         return super.getHref();
     }
 
     /**
      * Set the Component properties. ! Side effect: the RELS-EXT of the Component is updated with the new obtained
      * values.
      * <p/>
      * Once meant as setter-like method. Compare to setMdRecord, setMdRecords, setContent (FRS).
      * 
      * @param xml
      *            The XML with properties section of Component.
      * @return Map of Component properties.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidContentException
      */
     public Map<String, String> setProperties(final String xml) throws InvalidContentException,
         TripleStoreSystemException, EncodingSystemException, FedoraSystemException, XmlParserSystemException,
         WebserverSystemException {
 
         final StaxParser sp = new StaxParser();
         final ComponentPropertiesUpdateHandler cpuh =
             new ComponentPropertiesUpdateHandler(getResourceProperties(), '/' + Elements.ELEMENT_PROPERTIES, sp);
         sp.addHandler(cpuh);
         try {
             sp.parse(xml);
         }
         catch (final InvalidContentException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         final Map<String, String> properties = cpuh.getProperties();
         setRelsExt(getFoxmlRenderer().renderComponentRelsExt(getId(), properties, false));
 
         return properties;
     }
 
     /**
      * @return The foxml renderer.
      */
     public ItemFoXmlRendererInterface getFoxmlRenderer() {
 
         if (this.foxmlRenderer == null) {
             this.foxmlRenderer = new VelocityXmlItemFoXmlRenderer();
         }
         return this.foxmlRenderer;
     }
 
     /**
      * Obtains values from RelsExt.
      * 
      * @return map with predictate and objects
      * @throws XmlParserSystemException
      *             Thrown if parse of RELS-EXT failed.
      */
     private Map<String, String> obtainRelsExtValues() throws XmlParserSystemException {
 
         final StaxParser sp = new StaxParser();
 
         final RelsExtReadHandler eve = new RelsExtReadHandler(sp);
         eve.cleanIdentifier(true);
         sp.addHandler(eve);
         try {
             sp.parse(getRelsExt().getStream());
         }
         catch (final IntegritySystemException e) {
             throw new XmlParserSystemException("Unexpected exception during RELS-EXT parsing.", e);
         }
         catch (final Exception e) {
             throw new XmlParserSystemException("Unexpected exception during RELS-EXT parsing.", e);
         }
         final Map<String, String> properties = new HashMap<String, String>();
         final List<Triple> triples = eve.getElementValues().getTriples();
         for (final Triple triple : triples) {
             properties.put(triple.getPredicate(), triple.getObject());
         }
         return properties;
     }
 }
