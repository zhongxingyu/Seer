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
 
 package de.escidoc.core.common.business.fedora.resources;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.validation.constraints.NotNull;
 
 import net.sf.oval.guard.Guarded;
 
 import org.escidoc.core.services.fedora.FedoraServiceClient;
 import org.escidoc.core.services.fedora.access.ObjectProfileTO;
 import org.escidoc.core.services.fedora.management.DatastreamProfileTO;
 import org.escidoc.core.services.fedora.management.DatastreamProfilesTO;
 import org.escidoc.core.utils.io.MimeTypes;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.LockHandler;
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.Triple;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.ItemRelsExtUpdateHandler;
 import de.escidoc.core.common.util.stax.handler.MultipleExtractor;
 import de.escidoc.core.common.util.stax.handler.RelsExtReadHandler;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithChildElements;
 
 /**
  * Generic Resource supports object id, title, last modified, datastream, locking and sync mechanisms.
  * 
  * @author Steffen Wagner
  */
 @Configurable(preConstruction = true)
 @Guarded(applyFieldConstraintsToConstructors = true, applyFieldConstraintsToSetters = true, assertParametersNotNull = false, checkInvariants = true, inspectInterfaces = true)
 public class GenericResource implements FedoraResource {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GenericResource.class);
 
     private static final String DATASTREAM_DEFAULT = "datastream";
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     @Autowired
     @Qualifier("business.Utility")
     private Utility utility;
 
     @Autowired
     private FedoraServiceClient fedoraServiceClient;
 
     @Autowired
     @Qualifier("business.LockHandler")
     private LockHandler lockHandler;
 
     /**
      * TODO NeedSync in this form is ineffective. This should be at least a Vector as register which data set is out of
      * sync, an only this is called during the persist() method.
      */
     private boolean needSync;
 
     // -----------------------
 
    protected Datastream relsExt;
 
     protected Datastream dc;
 
     private String id;
 
     private String title;
 
     private String description;
 
     /**
      * The resource Href.
      */
     private String href;
 
     private Datastream datastream;
 
     // for versionated resources (like Item/Container) is the creationDate not
     // the Fedora CreationDate!
     protected String creationDate;
 
     private String createdBy;
 
     private Collection<String> propertiesNames = new ArrayList<String>();
 
     /**
      * Mapping from the TripleStore or WOV keys to the internal keys.
      */
     private Map<String, String> propertiesNamesMapping = new HashMap<String, String>();
 
     /**
      * Properties value map. Should contain all properties values of the resource.
      */
     private Map<String, String> propertiesMap = new HashMap<String, String>();
 
     // -----------------------
 
     /**
      * Constructor.
      */
     public GenericResource() {
         setPropertiesNames(expandPropertiesNames(getPropertiesNames()),
             expandPropertiesNamesMapping(getPropertiesNamesMapping()));
     }
 
     protected Utility getUtility() {
         return utility;
     }
 
     protected FedoraServiceClient getFedoraServiceClient() {
         return fedoraServiceClient;
     }
 
     /**
      * Constructor.
      * 
      * @param objid
      *            The id of the object in the repository.
      */
     public GenericResource(final String objid) {
         this.id = objid;
         setPropertiesNames(expandPropertiesNames(getPropertiesNames()),
             expandPropertiesNamesMapping(getPropertiesNamesMapping()));
     }
 
     /**
      * Get the resource description.
      * 
      * @return description
      */
     public String getDescription() {
         return this.description;
     }
 
     /**
      * Set the resource description.
      * 
      * @param description
      *            New description of resource.
      */
     protected void setDescription(final String description) {
         this.description = description;
     }
 
     /**
      * Get object Id (without version suffix).
      * 
      * @return objid (without version suffix)
      */
     @Override
     public final String getId() {
 
         return this.id;
     }
 
     /**
      * Href of the Resource. This method is to override through the inherited classes.
      * 
      * @return the href
      */
     @Override
     public String getHref() {
 
         return this.href;
     }
 
     /**
      * Set the href of the Resource. The href path is to set by the inherit classes depending on the resource path.
      * (/ir/item/.. or /ir/container/..)
      * 
      * @param path
      *            The path to the resource.
      */
     public void setHref(final String path) {
 
         this.href = path;
     }
 
     /**
      * Set object id.
      * 
      * @param id
      *            The object Id.
      * @throws TripleStoreSystemException
      *             Thrown if communication with TripleStore fails.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      * @throws ResourceNotFoundException
      *             Thrown if the resource with the provided id does not exist.
      */
     public void setId(final String id) throws TripleStoreSystemException, WebserverSystemException,
         ResourceNotFoundException {
 
         this.id = XmlUtility.getObjidWithoutVersion(id);
     }
 
     /**
      * Get creation date of resource.
      * 
      * @return creation date
      * @throws TripleStoreSystemException
      *             Thrown if request to TripleStore failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public String getCreationDate() throws TripleStoreSystemException {
 
         if (this.creationDate == null) {
             this.creationDate = this.tripleStoreUtility.getCreationDate(this.id);
         }
         return this.creationDate;
     }
 
     /**
      * Set the creation date.
      * 
      * @param creationDate
      *            The creationDate to set
      */
     public void setCreationDate(final String creationDate) {
         this.creationDate = creationDate;
     }
 
     public boolean isNeedSync() {
         return this.needSync;
     }
 
     public void setNeedSync(final boolean needSync) {
         this.needSync = needSync;
     }
 
     /**
      * Get last modification date of the resource. This modification date differs from the Fedora object (last)
      * modification date!
      * 
      * @return last-modification-date
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      * @throws FedoraSystemException
      *             Thrown if access to Fedora fails.
      */
     public DateTime getLastModificationDate() throws WebserverSystemException, FedoraSystemException {
 
         DateTime lastModificationDate = null;
 
         try {
 
             final String propLMD = getProperty(PropertyMapKeys.LAST_MODIFICATION_DATE);
 
             if (propLMD != null) {
                 lastModificationDate = new DateTime(propLMD, DateTimeZone.UTC);
             }
 
             if (lastModificationDate == null) {
                 final ObjectProfileTO objectProfile = this.fedoraServiceClient.getObjectProfile(this.id);
                 lastModificationDate = objectProfile.getObjLastModDate();
                 setLastModificationDate(lastModificationDate);
             }
         }
         catch (final TripleStoreSystemException e) {
             throw new WebserverSystemException(e);
         }
 
         return lastModificationDate;
     }
 
     /**
      * Set the last-modification-date.
      * 
      * @param timestamp
      *            The new timestamp for the lastModificationDate.
      * @throws WebserverSystemException
      *             Thrown if requesting TripleStore failed.
      */
     public void setLastModificationDate(final DateTime timestamp) throws WebserverSystemException {
         // this.lastModifiedDate = timestamp;
         try {
             setProperty(PropertyMapKeys.LAST_MODIFICATION_DATE, timestamp.toString());
         }
         catch (final TripleStoreSystemException e) {
             throw new WebserverSystemException(e);
         }
     }
 
     /**
      * Get last modification date of object from Fedora. This is the date of the Fedora object and differs from the
      * last-modification-date of the resource!
      * 
      * @return last-modification-date
      * @throws FedoraSystemException
      *             Thrown if access to Fedora fails.
      */
     @Override
     public DateTime getLastFedoraModificationDate() throws FedoraSystemException {
         final ObjectProfileTO objectProfile = this.fedoraServiceClient.getObjectProfile(this.id);
         return objectProfile.getObjLastModDate();
     }
 
     /**
      * Get title of object.
      * 
      * @return object title
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public String getTitle() throws WebserverSystemException {
         if (this.title == null) {
             try {
                 this.title = this.tripleStoreUtility.getTitle(this.id);
             }
             catch (final TripleStoreSystemException e) {
                 throw new WebserverSystemException(e);
             }
         }
         return this.title;
     }
 
     /**
      * Set the resource title.
      * 
      * @param title
      *            The new title.
      */
     public void setTitle(final String title) {
         this.title = title;
     }
 
     /**
      * Get object status.
      * 
      * @return status of resource.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public String getStatus() throws WebserverSystemException {
 
         final String status;
         try {
             status = getProperty(PropertyMapKeys.PUBLIC_STATUS);
         }
         catch (final TripleStoreSystemException e) {
             throw new WebserverSystemException(e);
         }
 
         return status;
     }
 
     /**
      * Get createdBy.
      * 
      * @return createdBy
      * @throws TripleStoreSystemException
      *             Thrown if request to TripleStore failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public String getCreatedBy() throws TripleStoreSystemException {
 
         if (this.createdBy == null) {
             this.createdBy =
                 this.tripleStoreUtility.getPropertiesElements(this.id, TripleStoreUtility.PROP_CREATED_BY_ID);
         }
 
         return this.createdBy;
     }
 
     /**
      * Set createdBy.
      * 
      * @param createdBy
      *            The id of the creator;
      */
     public void setCreatedBy(final String createdBy) {
         this.createdBy = createdBy;
     }
 
     // -------------------------------------------------------------------------
 
     /**
      * Check if Resource exist in Fedora.
      * 
      * @throws ResourceNotFoundException
      *             Thrown if no resource could be found under the object id.
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public void checkResourceExist() throws ResourceNotFoundException, TripleStoreSystemException {
 
         if (this.id == null || !this.tripleStoreUtility.exists(this.id)) {
             throw new ResourceNotFoundException("Resource with id " + this.id + " does not exist.");
         }
     }
 
     // -------------------------------------------------------------------------
 
     /**
      * Get RELS-EXT datastream.
      * 
      * @return RELS-EXT datastream.
      * @throws FedoraSystemException
      *             Thrown in case of exceptions with Fedora.
      * @throws StreamNotFoundException
      *             Thrown if RELS-EXT datastream could not be found.
      */
     @Override
     public Datastream getRelsExt() throws FedoraSystemException, StreamNotFoundException {
 
        if (this.relsExt == null) {
            this.relsExt = new Datastream(Datastream.RELS_EXT_DATASTREAM, this.id, null);
         }
        return this.relsExt;
     }
 
     /**
      * Get RELS-EXT datastream.
      * 
      * @return RELS-EXT datastream as String.
      * @throws EncodingSystemException
      *             Thrown if data stream encoding failed.
      * @throws StreamNotFoundException
      *             Thrown if the datastream was not found.
      * @throws FedoraSystemException
      *             Thrown if Fedora request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     public String getRelsExtAsString() throws EncodingSystemException, FedoraSystemException, StreamNotFoundException {
 
         final String xml;
         try {
             xml = new String(getRelsExt().getStream(), XmlUtility.CHARACTER_ENCODING);
         }
         catch (final UnsupportedEncodingException e) {
             throw new EncodingSystemException(e);
         }
         return xml;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param ds
      *            The RELS-EXT datasream.
      * @throws FedoraSystemException
      *             Thrown if Fedora request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     @Override
     public void setRelsExt(final Datastream ds) throws FedoraSystemException, WebserverSystemException {
 
        if (this.relsExt == null || !this.relsExt.equals(ds)) {
            this.relsExt = ds;
             this.needSync = true;
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param relsExt
      *            The RELS-EXT datasream.
      * @throws FedoraSystemException
      *             Thrown if Fedora request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     @Override
     public void setRelsExt(final byte[] relsExt) throws FedoraSystemException, WebserverSystemException {
 
         // TODO for resources that don't use versions could the RELS-EXT set to
         // override mode
         setRelsExt(new Datastream(Datastream.RELS_EXT_DATASTREAM, this.id, relsExt, MimeTypes.TEXT_XML));
     }
 
     /**
      * Set the RELS-EXT data stream.
      * 
      * @param relsExt
      *            The RELS-EXT datasream.
      * @throws EncodingSystemException
      *             Thrown if data stream encoding failed.
      * @throws StreamNotFoundException
      *             Thrown if the datastream was not found.
      * @throws FedoraSystemException
      *             Thrown if Fedora request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     public void setRelsExt(final ByteArrayOutputStream relsExt) throws FedoraSystemException, WebserverSystemException,
         EncodingSystemException {
 
         try {
             setRelsExt(relsExt.toString(XmlUtility.CHARACTER_ENCODING));
         }
         catch (final UnsupportedEncodingException e) {
             throw new EncodingSystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param relsExt
      *            The RELS-EXT datasream.
      * @throws EncodingSystemException
      *             Thrown if data stream encoding failed.
      * @throws FedoraSystemException
      *             Thrown if Fedora request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     @Override
     public void setRelsExt(final String relsExt) throws EncodingSystemException, FedoraSystemException,
         WebserverSystemException {
 
         try {
             setRelsExt(relsExt.getBytes(XmlUtility.CHARACTER_ENCODING));
         }
         catch (final UnsupportedEncodingException e) {
             throw new EncodingSystemException(e);
         }
     }
 
     /**
      * Unset RELS-EXT datastream to prevent merge with repository.
      */
     protected void unsetRelsExt() {
        this.relsExt = null;
     }
 
     /**
      * Update Elements in RELS-EXT.
      * 
      * @param elementsToUpdate
      *            Map of elements which are to update.
      * @param elementsToRemove
      *            Map of elements which are to remove.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     protected void updateRelsExt(
         final Map<String, StartElementWithChildElements> elementsToUpdate,
         final Map<String, List<StartElementWithChildElements>> elementsToRemove) throws WebserverSystemException {
 
         final StaxParser sp = new StaxParser();
 
         if (elementsToUpdate != null) {
             final ItemRelsExtUpdateHandler itemRelsExtUpdateHandler =
                 new ItemRelsExtUpdateHandler(elementsToUpdate, sp);
             sp.addHandler(itemRelsExtUpdateHandler);
         }
 
         final HashMap<String, String> pathes = new HashMap<String, String>();
         pathes.put("/RDF", null);
         final MultipleExtractor me = new MultipleExtractor(pathes, sp);
         me.removeElements(elementsToRemove);
         sp.addHandler(me);
 
         try {
             // final ByteArrayInputStream relsExtBA =
             // new ByteArrayInputStream(getRelsExt().getStream());
             sp.parse(getRelsExt().getStream());
             setRelsExt((ByteArrayOutputStream) me.getOutputStreams().get("RDF"));
         }
         catch (final Exception e) {
             throw new WebserverSystemException("Unexpected Exception.", e);
         }
     }
 
     /* ------------------------------------------------------------------------- */
 
     /**
      * Get resource properties. The internal propertiesNames are use to request the parameter. If this propertiesNames
      * differ from the default list then either set a new list or use the getResourceProperties(propertiesNames) method.
      * The values of this request are cached. The key names are mapped to the internal key representation.
      * 
      * @return resource properties.
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     public Map<String, String> getResourceProperties() throws TripleStoreSystemException, WebserverSystemException {
 
         if (this.propertiesMap == null) {
             final Map<String, String> lastVersionData = mapTripleList2TupleList(parseTriplesFromRelsExt());
             this.propertiesMap = mapTripleStoreKeys(lastVersionData);
         }
         return this.propertiesMap;
     }
 
     /**
      * Get value of property. Moved from Item (FRS).
      * 
      * @param prop
      *            Name of property.
      * @return Value of property.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     public String getProperty(final String prop) throws TripleStoreSystemException, WebserverSystemException {
 
         return getResourceProperties().get(prop);
     }
 
     /**
      * Get resource properties with a provided set of propertiesNames. The values of this request are not cached.
      * 
      * @param propertiesNamesCol
      *            The collection of properties names that are to request.
      * @return resource properties.
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     public Map<String, String> getResourceProperties(final Collection<String> propertiesNamesCol)
         throws TripleStoreSystemException {
 
         // return getTripleStoreUtility().getProperties(getId(),
         // propertiesNamesCol);
 
         final Map<String, String> tripleStoreValues =
             this.tripleStoreUtility.getProperties(getId(), propertiesNamesCol);
 
         return mapTripleStoreKeys(tripleStoreValues);
     }
 
     /**
      * Maps the keys from the TripleStore to the internal used keys (from PropertyMapKeys). This method maps all keys
      * which are used in this class.
      * 
      * @param tripleStoreMap
      *            A map with TripleStore key, value pairs
      * @return A map with key, values pairs where the keys are object consist (see PropertyMapKeys class)
      */
     public Map<String, String> mapTripleStoreKeys(final Map<String, String> tripleStoreMap) {
 
         final Map<String, String> properties = new HashMap<String, String>();
 
         for (final Entry<String, String> stringStringEntry : tripleStoreMap.entrySet()) {
             final String value = stringStringEntry.getValue();
             if (value != null) {
                 final String targetKey = this.propertiesNamesMapping.get(stringStringEntry.getKey());
                 if (targetKey != null) {
                     properties.put(targetKey, value);
                 }
                 else {
                     properties.put(stringStringEntry.getKey(), value);
                 }
             }
         }
 
         return properties;
     }
 
     /**
      * Add value to the resource properties map. Double key entries are overridden.
      * 
      * @param valueMap
      *            map with new values
      */
     public void addResourceProperties(final Map<String, String> valueMap) {
 
         if (this.propertiesMap == null) {
             this.propertiesMap = valueMap;
         }
         else {
             this.propertiesMap.putAll(valueMap);
         }
     }
 
     /**
      * Get a list of all in this class used (RELS-EXT) properties names.
      * 
      * @return list of RELS-EXT properties names.
      */
     protected final Collection<String> getPropertiesNames() {
         return this.propertiesNames;
     }
 
     /**
      * Get a list of all in this class used (RELS-EXT) properties names.
      * 
      * @return list of RELS-EXT properties names.
      */
     protected final Map<String, String> getPropertiesNamesMapping() {
         return this.propertiesNamesMapping;
     }
 
     /**
      * Set the Collection of properties names. This names are defined as resource parameter. The cached of properties
      * values is deleted.
      * 
      * @param propertiesNames
      *            The Collection of parameter names for this resource.
      * @param propertiesNamesMapping
      *            The Map of key names how they to map to the internal key names.
      */
     protected final void setPropertiesNames(
         final Collection<String> propertiesNames, final Map<String, String> propertiesNamesMapping) {
         this.propertiesNames = propertiesNames;
         this.propertiesNamesMapping = propertiesNamesMapping;
         this.propertiesMap = null;
     }
 
     /**
      * Set a specific property.
      * 
      * @param name
      *            property name
      * @param value
      *            property value
      * 
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     public void setProperty(final String name, final String value) throws TripleStoreSystemException,
         WebserverSystemException {
         getResourceProperties().put(name, value);
     }
 
     // -------------------------------------------------------------------------
 
     /**
      * Check if object is locked.
      * 
      * @return Whether the resource is locked or not.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     public final boolean isLocked() throws WebserverSystemException {
         return this.lockHandler.isLocked(this.id);
     }
 
     /**
      * If the item is locked the lock owner is returned, null otherwise.
      * 
      * @return The lock owner or null.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     public final String getLockOwner() throws WebserverSystemException {
         return this.lockHandler.getLockOwner(this.id);
     }
 
     /**
      * If the item is locked the lock owner title is returned, null otherwise.
      * 
      * @return The lock owner or null.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     public final String getLockOwnerTitle() throws WebserverSystemException {
         return this.lockHandler.getLockOwnerTitle(this.id);
     }
 
     /**
      * If the container is locked the lock date is returned, null otherwise.
      * 
      * @return The lock date or null.
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      */
     public final String getLockDate() throws WebserverSystemException {
         return this.lockHandler.getLockDate(this.id);
     }
 
     /**
      * Lock/Unlock object.
      * 
      * @param lock
      *            True == lock object. False == unlock object.
      * @param lockOwner
      *            Ids who is lock the object.
      * @throws LockingException
      *             Thrown if locking fails.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
      */
     public final void setLocked(final boolean lock, @NotNull
     final String[] lockOwner) throws SqlDatabaseSystemException, WebserverSystemException {
         // Should lock only be checked in handler? No, it is part of the
         // resource representation.
         if (lock && !isLocked()) {
             this.lockHandler.lock(this.id, lockOwner);
         }
         else if (isLocked()) {
             this.lockHandler.unlock(this.id);
         }
     }
 
     // -------------------------------------------------------------------------
 
     /**
      * Get a new, virgin Stream.
      * 
      * @return datastream
      * @throws StreamNotFoundException
      *             Thrown if the datastream was not found.
      * @throws FedoraSystemException
      *             Thrown if Fedora request failed.
      */
     public Datastream getDatastream() throws StreamNotFoundException, FedoraSystemException {
 
         if (this.datastream == null) {
             this.datastream = new Datastream(DATASTREAM_DEFAULT, this.id, null);
         }
         return this.datastream;
     }
 
     /**
      * Set Stream to the resource.
      * 
      * @param ds
      *            The datastream.
      * @throws StreamNotFoundException
      *             Thrown if the datastream was not found.
      * @throws LockingException
      *             Thrown if resource is locked.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      */
     public void setDatastream(final Datastream ds) throws StreamNotFoundException, LockingException,
         FedoraSystemException, WebserverSystemException {
 
         if (isLocked()) {
             final String msg = "Resource " + this.id + " is locked.";
             throw new LockingException(msg);
         }
         // check if datastream is set, is equal to ds and save to fedora
         try {
             // FIXME the getStream method delivers an virgin Stream. Is
             // this compare really necessary (Ok, this prevents writing of
             // unused data streams.)?
             final Datastream curDs = getDatastream();
 
             if (!ds.equals(curDs)) {
 
                 this.datastream = ds;
                 final DateTime lmd = ds.merge();
                 setLastModificationDate(lmd);
                 this.needSync = true;
             }
         }
         catch (final StreamNotFoundException e) {
             // An item have to have a RELS-EXT datastream
             throw new StreamNotFoundException("No " + Datastream.RELS_EXT_DATASTREAM + " for item " + this.id + '.', e);
         }
 
         // // don't forget to update
         // // TODO check if the new introduced return value of the
         // Stream.merge
         // // method makes this update redundant!
         // setLastModificationDate(getFedoraUtility().getLastModificationDate(
         // this.id));
         //
         // this.needSync = true;
 
     }
 
     // -------------------------------------------------------------------------
 
     /**
      * @return Is true if object out of sync with Repository.
      */
     public boolean isNewVersion() {
         return this.needSync;
     }
 
     /**
      * Persists the whole object to Fedora.
      * 
      * @return lastModificationDate of the resource
      * @throws FedoraSystemException
      *             Thrown if connection to Fedora failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public DateTime persist() throws FedoraSystemException, WebserverSystemException {
 
         return persist(true);
     }
 
     /**
      * Persists the whole object to Fedora.
      * 
      * @param sync
      *            Set {@code true} if TripleStore sync is to force.
      * @return lastModificationDate of the resource.
      * @throws FedoraSystemException
      *             Thrown if connection to Fedora failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public DateTime persist(final boolean sync) throws FedoraSystemException, WebserverSystemException {
         /*
          * Well, this is not nice but the used comparing method, to detect changes, is expensive. If RELS-EXT was not
          * updated (through the methods of this class), then should a persist be redundant.
          */
         if (this.needSync) {
             final DateTime lastModificationDate = persistRelsExt();
             setLastModificationDate(lastModificationDate);
         }
         if (sync) {
             this.fedoraServiceClient.sync();
         }
         try {
             this.tripleStoreUtility.reinitialize();
         }
         catch (final TripleStoreSystemException e) {
             throw new FedoraSystemException("Error on reinitializing triple store.", e);
         }
         return getLastFedoraModificationDate();
     }
 
     /**
      * Write RELS-EXT to Fedora.
      * 
      * @return new timestamp of data stream (or null if not updated)
      * @throws FedoraSystemException
      *             Thrown if connection to Fedora failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     protected DateTime persistRelsExt() throws WebserverSystemException {
 
         DateTime timestamp = null; // Maybe would it be better, if we use the
         // old timestamp instead of null.
        if (this.relsExt != null) {
            timestamp = new DateTime(this.relsExt.merge(), DateTimeZone.UTC);
         }
 
         return timestamp;
     }
 
     // -------------------------------------------------------------------------
     // private methods
     // -------------------------------------------------------------------------
 
     /**
      * Expand a list with names of properties values with the propertiesNames for a versionated resource. These list
      * could be used to request the TripleStore.
      * 
      * @param propertiesNames
      *            Collection of propertiesNames. The collection contains only the version resource specific
      *            propertiesNames.
      * @return Parameter name collection
      */
     private static Collection<String> expandPropertiesNames(final Collection<String> propertiesNames) {
 
         final Collection<String> newPropertiesNames =
             propertiesNames != null ? propertiesNames : new ArrayList<String>();
 
         newPropertiesNames.add(TripleStoreUtility.PROP_DC_TITLE);
         // FIXME only container has description
         newPropertiesNames.add(TripleStoreUtility.PROP_DC_DESCRIPTION);
         newPropertiesNames.add(TripleStoreUtility.PROP_CREATED_BY_TITLE);
         newPropertiesNames.add(TripleStoreUtility.PROP_CREATED_BY_ID);
         newPropertiesNames.add(TripleStoreUtility.PROP_PUBLIC_STATUS);
         newPropertiesNames.add(TripleStoreUtility.PROP_PUBLIC_STATUS_COMMENT);
 
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_MODEL_TITLE);
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_MODEL_ID);
 
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTEXT_TITLE);
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTEXT_ID);
 
         newPropertiesNames.add(TripleStoreUtility.PROP_LATEST_VERSION_USER_ID);
         newPropertiesNames.add(TripleStoreUtility.PROP_LATEST_VERSION_USER_TITLE);
 
         newPropertiesNames.add(TripleStoreUtility.PROP_ORIGIN);
         newPropertiesNames.add(TripleStoreUtility.PROP_ORIGIN_VERSION);
 
         return newPropertiesNames;
 
     }
 
     /**
      * Expand the map for the to mapping key names. The properties key names from the TripleStore differ to the internal
      * representation. Therefore we translate the key names to the internal.
      * 
      * @param propertiesNamesMap
      *            The key is the to replace value. E.g. the &lt;oldKeyName, newKeyName&gt;
      * @return propertiesNamesMappingMap
      */
     private static Map<String, String> expandPropertiesNamesMapping(final Map<String, String> propertiesNamesMap) {
 
         final Map<String, String> newPropertiesNamesMap =
             propertiesNamesMap != null ? propertiesNamesMap : new HashMap<String, String>();
 
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_DC_TITLE, PropertyMapKeys.LATEST_VERSION_TITLE);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_DC_DESCRIPTION, PropertyMapKeys.LATEST_VERSION_DESCRIPTION);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_CREATED_BY_TITLE, PropertyMapKeys.CREATED_BY_TITLE);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_CREATED_BY_ID, PropertyMapKeys.CREATED_BY_ID);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_PUBLIC_STATUS, PropertyMapKeys.PUBLIC_STATUS);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_PUBLIC_STATUS_COMMENT, PropertyMapKeys.PUBLIC_STATUS_COMMENT);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_CONTENT_MODEL_TITLE,
             PropertyMapKeys.LATEST_VERSION_CONTENT_MODEL_TITLE);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_CONTENT_MODEL_ID,
             PropertyMapKeys.LATEST_VERSION_CONTENT_MODEL_ID);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_CONTEXT_TITLE, PropertyMapKeys.LATEST_VERSION_CONTEXT_TITLE);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_CONTEXT_ID, PropertyMapKeys.LATEST_VERSION_CONTEXT_ID);
 
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_LATEST_VERSION_USER_ID,
             PropertyMapKeys.LATEST_VERSION_MODIFIED_BY_ID);
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_LATEST_VERSION_USER_TITLE,
             PropertyMapKeys.LATEST_VERSION_MODIFIED_BY_TITLE);
 
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_ORIGIN, PropertyMapKeys.ORIGIN);
 
         newPropertiesNamesMap.put(TripleStoreUtility.PROP_ORIGIN_VERSION, PropertyMapKeys.ORIGIN_VERSION);
 
         return newPropertiesNamesMap;
     }
 
     /**
      * Map the value of the sourceKey from the sourceMap to the targetKey in the targetMap.
      * 
      * @param sourceMap
      *            The source map of values.
      * @param targetMap
      *            The target map for values.
      * @param sourceKey
      *            The key in the source Map.
      * @param targetKey
      *            The key in the target Map where the value is to store.
      */
     public static void map(
         final Map<String, String> sourceMap, final Map<String, String> targetMap, final String sourceKey,
         final String targetKey) {
 
         final String value = sourceMap.get(sourceKey);
         if (value != null) {
             targetMap.put(targetKey, value);
         }
     }
 
     /**
      * FIXME move to genericResource
      * <p/>
      * Check if the Resource is fro the provided type.
      * 
      * @param resourceType
      *            Required resource type.
      * @return true if resource is from provided type.
      * @throws IntegritySystemException
      *             Thrown if object type could not retrieved
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     public boolean checkResourceType(final ResourceType resourceType) throws TripleStoreSystemException,
         WebserverSystemException {
 
         final String type = getProperty(TripleStoreUtility.PROP_OBJECT_TYPE);
 
         if (resourceType == ResourceType.ITEM) {
             if (type.equals(Constants.ITEM_OBJECT_TYPE)) {
                 return true;
             }
         }
         else if (resourceType == ResourceType.COMPONENT) {
             if (type.equals(Constants.COMPONENT_OBJECT_TYPE)) {
                 return true;
             }
         }
         else if (resourceType == ResourceType.CONTAINER) {
             if (type.equals(Constants.CONTAINER_OBJECT_TYPE)) {
                 return true;
             }
         }
         else if (resourceType == ResourceType.CONTEXT) {
             if (type.equals(Constants.CONTEXT_OBJECT_TYPE)) {
                 return true;
             }
         }
         else if (resourceType == ResourceType.CONTENT_RELATION && type.equals(Constants.CONTENT_RELATION2_OBJECT_TYPE)) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Map a List fo Triples to a tuple list (where predicate is key and object is value). The subject is dropped.
      * 
      * @param triples
      *            The Triples which are to map.
      * @return Tupel list.
      */
     private Map<String, String> mapTripleList2TupleList(final Iterable<Triple> triples) {
         final Map<String, String> lastVersionData = new HashMap<String, String>();
         for (final Triple triple : triples) {
             lastVersionData.put(triple.getPredicate(), triple.getObject());
             if (triple.getPredicate().equals(TripleStoreUtility.PROP_DC_TITLE)) {
                 this.title = triple.getObject();
             }
         }
         return lastVersionData;
 
     }
 
     /**
      * Parse RelsExt and obtain Triples.
      * 
      * @return List with Triples of RelsExt.
      * @throws WebserverSystemException
      *             Thrown if retrieve of RelsExt or parsing failed.
      */
     private List<Triple> parseTriplesFromRelsExt() throws WebserverSystemException {
         final Datastream tmpRelsExt;
         try {
             tmpRelsExt = new Datastream(Datastream.RELS_EXT_DATASTREAM, getId(), null);
         }
         catch (final FedoraSystemException e1) {
             throw new WebserverSystemException(e1);
         }
         catch (final StreamNotFoundException e1) {
             throw new WebserverSystemException(e1);
         }
 
         final StaxParser sp = new StaxParser();
 
         final RelsExtReadHandler eve = new RelsExtReadHandler(sp);
         eve.cleanIdentifier(true);
         sp.addHandler(eve);
         try {
             sp.parse(new ByteArrayInputStream(tmpRelsExt.getStream()));
         }
         catch (final Exception e) {
             throw new WebserverSystemException("Unexpected exception during RELS-EXT parsing.", e);
         }
 
         return eve.getElementValues().getTriples();
     }
 
     protected DatastreamProfilesTO getDatastreamProfiles() throws WebserverSystemException {
         return getFedoraServiceClient().getDatastreamProfiles(getId(), null);
     }
 
     /**
      * TODO: There are no Exceptions thrown here.
      * 
      * @param datastreamProfiles
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     protected final void initDatastreams(final DatastreamProfilesTO datastreamProfiles)
         throws WebserverSystemException, FedoraSystemException, TripleStoreSystemException, IntegritySystemException,
         StreamNotFoundException {
 
         for (final DatastreamProfileTO profile : datastreamProfiles.getDatastreamProfile()) {
             initDatastream(profile);
         }
     }
 
     /**
      * Override this method to support more than the usual datastream types.
      * 
      * @param profile
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     protected void initDatastream(final DatastreamProfileTO profile) throws WebserverSystemException,
         FedoraSystemException, TripleStoreSystemException, IntegritySystemException, StreamNotFoundException {
         // RELS-EXT
         if (Datastream.RELS_EXT_DATASTREAM.equals(profile.getDsID())) {
             // The RELS-EXT in the Fedora repository is newer than the
             // version specified by versionDate. The difference between both
             // versions are timestamps (version/date, release/date).
            this.relsExt = new Datastream(profile, getId());
         }
         // DC
         else if ("DC".equals(profile.getDsID()) && this.dc == null) {
             this.dc = new Datastream(profile, getId());
         }
         else {
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Stream " + getId() + '/' + profile.getDsID()
                     + " not instanziated in GenericResource.<init>" + '.');
             }
         }
     }
 }
