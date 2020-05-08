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
 package de.escidoc.core.oum.business.fedora.resources;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.escidoc.core.services.fedora.AddDatastreamPathParam;
 import org.escidoc.core.services.fedora.AddDatastreamQueryParam;
 import org.escidoc.core.services.fedora.FedoraServiceClient;
 import org.escidoc.core.services.fedora.management.DatastreamProfilesTO;
 import org.escidoc.core.utils.io.MimeTypes;
 import org.escidoc.core.utils.io.Stream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.GenericResource;
 import de.escidoc.core.common.business.fedora.resources.Predecessor;
 import de.escidoc.core.common.business.fedora.resources.PredecessorForm;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.oum.business.fedora.resources.interfaces.OrganizationalUnitInterface;
 
 /**
  * Resource implementation of an organizational unit resource.
  * 
  * @author Michael Schneider
  */
 @Configurable(preConstruction = true)
 public class OrganizationalUnit extends GenericResource implements OrganizationalUnitInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationalUnit.class);
 
     public static final String ESCIDOC = "escidoc";
 
     public static final String NS_URI = "nsUri";
 
     private String name;
 
     private String description;
 
     private String publicStatus;
 
     private String publicStatusComment;
 
     private String createdByTitle;
 
     private String modifiedBy;
 
     private String modifiedByTitle;
 
     private List<String> parents;
 
     private List<Predecessor> predecessors;
 
     private List<Predecessor> successors;
 
     private boolean hasChildren;
 
     @Autowired
     private FedoraServiceClient fedoraServiceClient;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     /**
      * Constructs the Context with the specified id. The datastreams are instantiated and retrieved if the related
      * getter is called.
      * 
      * @param id
      *            The id of an organizational unit managed in Fedora.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     public OrganizationalUnit(final String id) throws OrganizationalUnitNotFoundException, TripleStoreSystemException,
         IntegritySystemException {
         super(id);
         init();
     }
 
     private void init() throws OrganizationalUnitNotFoundException, TripleStoreSystemException,
         IntegritySystemException {
         if (this.getId() != null) {
             this.getUtility().checkIsOrganizationalUnit(this.getId());
         }
         setHref(Constants.ORGANIZATIONAL_UNIT_URL_BASE + this.getId());
         getSomeValuesFromFedora();
     }
 
     /**
      * Retrieve a property value from the triplestore.
      * 
      * @param property
      *            The name of the expected property.
      * @return The retrieved value of the property.
      * @throws TripleStoreSystemException
      *             If access to the triplestore fails.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     private String getPropertyFromTriplestore(final String property) throws TripleStoreSystemException {
         return this.tripleStoreUtility.getPropertiesElements(getId(), property);
     }
 
     /**
      * Get the values of the properties stored in RELS-EXT datastream. If possible retrieve them directly from the
      * triplestore.
      * 
      * @throws TripleStoreSystemException
      *             Thrown if access to TripleStore failed.
      * @throws WebserverSystemException
      *             If access to the backend (fedora or triplestore) fails.
      */
     protected final void getSomeValuesFromFedora() throws TripleStoreSystemException {
 
         // this.creationDate = getTripleStoreUtility().getCreationDate(getId());
         // this.createdBy =
         // getPropertyFromTriplestore(TripleStoreUtility.PROP_CREATED_BY_ID);
         this.createdByTitle = getPropertyFromTriplestore(TripleStoreUtility.PROP_CREATED_BY_TITLE);
 
         // this.lastModificationDate =
         // getTripleStoreUtility().getLastModificationDate(getId());
         this.modifiedBy = getPropertyFromTriplestore(TripleStoreUtility.PROP_MODIFIED_BY_ID);
         this.modifiedByTitle = getPropertyFromTriplestore(TripleStoreUtility.PROP_MODIFIED_BY_TITLE);
         this.publicStatus = getPropertyFromTriplestore(TripleStoreUtility.PROP_PUBLIC_STATUS);
         this.publicStatusComment = getPropertyFromTriplestore(TripleStoreUtility.PROP_PUBLIC_STATUS_COMMENT);
 
         this.hasChildren = !this.tripleStoreUtility.getChildren(getId()).isEmpty();
         this.name = this.tripleStoreUtility.getTitle(getId());
         this.description = this.tripleStoreUtility.getDescription(getId());
         this.parents = this.tripleStoreUtility.getParents(getId());
         this.predecessors = getPredecessors(getId());
 
     }
 
     /**
      * Get list of predecessors of OU.
      * 
      * @param ouId
      *            Id of Organizational Unit.
      * @return List of predecessors for the selected OU.
      * @throws TripleStoreSystemException
      *             Thrown if request TripleStore failed.
      */
     public List<Predecessor> getPredecessors(final String ouId) throws TripleStoreSystemException {
         final List<Predecessor> predecessors = new ArrayList<Predecessor>();
         // collect affiliations
         List<String> pred = this.tripleStoreUtility.executeQueryId(ouId, false, Constants.PREDECESSOR_AFFILIATION);
         Iterator<String> it = pred.iterator();
         while (it.hasNext()) {
             predecessors.add(new Predecessor(it.next(), PredecessorForm.AFFILIATION));
         }
 
         // collect fusion
         pred = this.tripleStoreUtility.executeQueryId(ouId, false, Constants.PREDECESSOR_FUSION);
         it = pred.iterator();
         while (it.hasNext()) {
             predecessors.add(new Predecessor(it.next(), PredecessorForm.FUSION));
         }
 
         // collect replacement
         pred = this.tripleStoreUtility.executeQueryId(ouId, false, Constants.PREDECESSOR_REPLACEMENT);
         it = pred.iterator();
         while (it.hasNext()) {
             predecessors.add(new Predecessor(it.next(), PredecessorForm.REPLACEMENT));
         }
 
         // collect spin-off
         pred = this.tripleStoreUtility.executeQueryId(ouId, false, Constants.PREDECESSOR_SPIN_OFF);
         it = pred.iterator();
         while (it.hasNext()) {
             predecessors.add(new Predecessor(it.next(), PredecessorForm.SPIN_OFF));
         }
 
         // collect splitting
         pred = this.tripleStoreUtility.executeQueryId(ouId, false, Constants.PREDECESSOR_SPLITTING);
         it = pred.iterator();
         while (it.hasNext()) {
             predecessors.add(new Predecessor(it.next(), PredecessorForm.SPLITTING));
         }
         return predecessors;
     }
 
     /**
      * Get list of successors of OU.
      * 
      * @param ouId
      *            Id of Organizational Unit.
      * @return List of successors for the selected OU.
      * @throws TripleStoreSystemException
      *             Thrown if request TripleStore failed.
      */
     public List<Predecessor> getSuccessors(final String ouId) throws TripleStoreSystemException {
         final List<Predecessor> successors = new ArrayList<Predecessor>();
         final Collection<String> ids = new ArrayList<String>();
         ids.add(ouId);
         // collect affiliations
         List<String> pred = this.tripleStoreUtility.executeQueryForList(ids, true, Constants.PREDECESSOR_AFFILIATION);
         Iterator<String> it = pred.iterator();
         while (it.hasNext()) {
             successors.add(new Predecessor(XmlUtility.getIdFromURI(it.next()), PredecessorForm.AFFILIATION));
         }
 
         // collect fusion
         pred = this.tripleStoreUtility.executeQueryForList(ids, true, Constants.PREDECESSOR_FUSION);
         it = pred.iterator();
         while (it.hasNext()) {
             successors.add(new Predecessor(XmlUtility.getIdFromURI(it.next()), PredecessorForm.FUSION));
         }
 
         // collect replacement
         pred = this.tripleStoreUtility.executeQueryForList(ids, true, Constants.PREDECESSOR_REPLACEMENT);
         it = pred.iterator();
         while (it.hasNext()) {
             successors.add(new Predecessor(XmlUtility.getIdFromURI(it.next()), PredecessorForm.REPLACEMENT));
         }
 
         // collect spin-off
         pred = this.tripleStoreUtility.executeQueryForList(ids, true, Constants.PREDECESSOR_SPIN_OFF);
         it = pred.iterator();
         while (it.hasNext()) {
             successors.add(new Predecessor(XmlUtility.getIdFromURI(it.next()), PredecessorForm.SPIN_OFF));
         }
 
         // collect splitting
         pred = this.tripleStoreUtility.executeQueryForList(ids, true, Constants.PREDECESSOR_SPLITTING);
         it = pred.iterator();
         while (it.hasNext()) {
             successors.add(new Predecessor(XmlUtility.getIdFromURI(it.next()), PredecessorForm.SPLITTING));
         }
         return successors;
     }
 
     /**
      * @return the createdByTitle
      */
     public String getCreatedByTitle() {
         return this.createdByTitle;
     }
 
     /**
      * @param createdByTitle
      *            the createdByTitle to set
      */
     public void setCreatedByTitle(final String createdByTitle) {
         this.createdByTitle = createdByTitle;
     }
 
     /**
      * @return the hasChildren
      */
     public boolean hasChildren() {
         return this.hasChildren;
     }
 
     /**
      * Get the list of children ids for this organizational unit from the triplestore.
      * 
      * @return The list of children ids for this organizational unit.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     public List<String> getChildrenIds() throws TripleStoreSystemException {
         return this.tripleStoreUtility.getChildren(getId());
     }
 
     /**
      * @param hasChildren
      *            the hasChildren to set
      */
     public void setHasChildren(final boolean hasChildren) {
         this.hasChildren = hasChildren;
     }
 
     /**
      * @return the modifiedBy
      */
     public String getModifiedBy() {
         return this.modifiedBy;
     }
 
     /**
      * @param modifiedBy
      *            the modifiedBy to set
      */
     public void setModifiedBy(final String modifiedBy) {
         this.modifiedBy = modifiedBy;
     }
 
     /**
      * @return the modifiedByTitle
      */
     public String getModifiedByTitle() {
         return this.modifiedByTitle;
     }
 
     /**
      * @param modifiedByTitle
      *            the modifiedByTitle to set
      */
     public void setModifiedByTitle(final String modifiedByTitle) {
         this.modifiedByTitle = modifiedByTitle;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return this.name;
     }
 
     /**
      * @param name
      *            the name to set
      */
     public void setName(final String name) {
         this.name = name;
     }
 
     /**
      * OU title equals OU name.
      * 
      * @return the title
      */
     @Override
     public String getTitle() {
         return this.name;
     }
 
     /*
      * See Interface for functional description.
      * 
      * @param name
      * 
      * @return
      * 
      * @throws FedoraSystemException
      * 
      * @seede.escidoc.core.oum.business.fedora.resources.interfaces.
      * OrganizationalUnitInterface#getMdRecord(java.lang.String)
      */
     @Override
     public Datastream getMdRecord(final String name) throws FedoraSystemException, StreamNotFoundException {
 
         return new Datastream(name, getId(), null);
     }
 
     /*
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.oum.business.fedora.resources.interfaces. OrganizationalUnitInterface#getMdRecords()
      */
     @Override
     public Map<String, Datastream> getMdRecords() throws FedoraSystemException, IntegritySystemException {
 
         final DatastreamProfilesTO profiles =
             getFedoraServiceClient().getDatastreamProfilesByAltId(getId(), Datastream.METADATA_ALTERNATE_ID, null);
 
         return Datastream.convertDatastreamProfilesTO(profiles, getId());
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public void setMdRecord(final String name, final Datastream ds) throws EncodingSystemException,
         IntegritySystemException, FedoraSystemException, WebserverSystemException, TripleStoreSystemException {
         final String mimeType = ds.getMimeType();
         String type = Constants.DEFAULT_ALTID_TYPE;
         String schema = Constants.DEFAULT_ALTID_SCHEMA;
         if (ds.getAlternateIDs().size() >= 3) {
             type = ds.getAlternateIDs().get(1);
             schema = ds.getAlternateIDs().get(2);
         }
 
         try {
             final Datastream curDs = getMdRecord(name);
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
             final boolean contentChanged = !ds.equals(curDs);
             if (contentChanged || !type.equals(curType) || !schema.equals(curSchema) || !mimeType.equals(curMimeType)) {
                 if (contentChanged && name.equals(ESCIDOC)) {
 
                     final Map<String, String> mdProperties = ds.getProperties();
                     if (mdProperties != null) {
                         if (mdProperties.containsKey(NS_URI)) {
                             final String dcNewContent =
                                 XmlUtility.createDC(mdProperties.get(NS_URI), ds.toStringUTF8(), getId());
                             if (dcNewContent != null && dcNewContent.trim().length() > 0) {
                                 try {
                                     setDc(new Datastream(Datastream.DC_DATASTREAM, getId(), dcNewContent
                                         .getBytes(XmlUtility.CHARACTER_ENCODING), MimeTypes.TEXT_XML));
                                 }
                                 catch (final UnsupportedEncodingException e) {
                                     throw new EncodingSystemException(e.getMessage(), e);
                                 }
                             }
                         }
                         else {
                             throw new IntegritySystemException("Namespace URI of 'escidoc' metadata"
                                 + " is not set in datastream.");
                         }
                     }
                     else {
                         throw new IntegritySystemException("Properties of 'md-record' datastream"
                             + " with then name 'escidoc' do not exist");
                     }
                 }
                 ds.merge();
             }
         }
         catch (final StreamNotFoundException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on setting MD-records.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on setting MD-records.", e);
             }
             // this is not an update; its a create
             ds.addAlternateId(type);
             ds.addAlternateId(schema);
             ds.persist(false);
         }
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public void setMdRecords(final Map<String, Datastream> mdRecords) throws IntegritySystemException,
         FedoraSystemException, WebserverSystemException, EncodingSystemException, TripleStoreSystemException {
         // Container.setMdRecords throws FedoraSystemException,
         // WebserverSystemException,
         // TripleStoreSystemException, IntegritySystemException,
         // EncodingSystemException
 
         // get list of names of data streams with alternateId = "metadata"
         final Set<String> namesInFedora = getMdRecords().keySet();
 
         // delete Datastreams which are in Fedora but not in mdRecords
         for (final String nameInFedora : namesInFedora) {
             if (!mdRecords.containsKey(nameInFedora)) {
                 try {
                     final Datastream fedoraDs = getMdRecord(nameInFedora);
                     if (fedoraDs != null) {
                         fedoraDs.delete();
                     }
                 }
                 catch (final StreamNotFoundException e) {
                     // Do nothing, datastream is already deleted.
                     if (LOGGER.isWarnEnabled()) {
                         LOGGER.warn("Unable to find datastream '" + nameInFedora + "'.");
                     }
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Unable to find datastream '" + nameInFedora + "'.", e);
                     }
                 }
             }
         }
 
         // create or update Datastreams which are send
         for (final Entry<String, Datastream> stringDatastreamEntry : mdRecords.entrySet()) {
             if (namesInFedora.contains(stringDatastreamEntry.getKey())) {
                 setMdRecord(stringDatastreamEntry.getKey(), stringDatastreamEntry.getValue());
                 namesInFedora.remove(stringDatastreamEntry.getKey());
             }
             else {
                 final Datastream currentMdRecord = stringDatastreamEntry.getValue();
 
                 final AddDatastreamPathParam path = new AddDatastreamPathParam(getId(), stringDatastreamEntry.getKey());
                 final AddDatastreamQueryParam query = new AddDatastreamQueryParam();
                 query.setAltIDs(currentMdRecord.getAlternateIDs());
                 query.setDsLabel(XmlUtility.NAME_MDRECORD);
                 query.setVersionable(Boolean.FALSE);
                 final Stream stream = new Stream();
                 try {
                     stream.write(currentMdRecord.getStream());
                     stream.lock();
                 }
                 catch (final IOException e) {
                     throw new WebserverSystemException(e);
                 }
                 this.fedoraServiceClient.addDatastream(path, query, stream);
                 // TODO should new Stream be put in list of md-records of this
                 // OU?
             }
         }
     }
 
     /**
      * Get DC datastream.
      * 
      * @return The DC datastream.
      * @throws StreamNotFoundException
      *             If there is no DC datastream and parentId in Fedora.
      * @throws FedoraSystemException
      *             Thrown in case of an internal system error caused by failed Fedora access.
      */
     public Datastream getDc() throws StreamNotFoundException, FedoraSystemException {
 
         return new Datastream(Datastream.DC_DATASTREAM, getId(), null);
     }
 
     /**
      * Set DC datastream.
      * 
      * @param ds
      *            DC datastream
      * @throws StreamNotFoundException
      *             If there is no datastream identified by name and parentId in Fedora.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      */
     public void setDc(final Datastream ds) throws StreamNotFoundException, TripleStoreSystemException,
         FedoraSystemException, WebserverSystemException {
 
         try {
             if (!ds.equals(getDc())) {
                 ds.merge();
             }
         }
         catch (final StreamNotFoundException e) {
             throw new StreamNotFoundException("No DC for organizational-unit " + getId() + '.', e);
         }
         getSomeValuesFromFedora();
     }
 
     /**
      * @return the publicStatus
      */
     public String getPublicStatus() {
         return this.publicStatus;
     }
 
     /**
      * @param publicStatus
      *            the publicStatus to set
      */
     public void setPublicStatus(final String publicStatus) {
         this.publicStatus = publicStatus;
     }
 
     /**
      * @return the publicStatusComment
      */
     public String getPublicStatusComment() {
        return this.publicStatusComment;
     }
 
     /**
      * @param publicStatus
      *            the publicStatus to set
      */
     public void setPublicStatusComment(final String publicStatusComment) {
         this.publicStatusComment = publicStatusComment;
     }
 
     /**
      * @return the parentOus
      */
     public List<String> getParents() {
         return this.parents;
     }
 
     /**
      * Get predecessors of OU.
      * 
      * @return the predecessors of the OU
      */
     public List<Predecessor> getPredecessors() {
         return this.predecessors;
     }
 
     /**
      * Get successors of OU.
      * 
      * @return the successors of the OU
      * @throws WebserverSystemException
      *             Thrown if creating instance of TripleStoreUtility failed.
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore request failed.
      */
     public List<Predecessor> getSuccessors() throws TripleStoreSystemException {
         if (this.successors == null) {
             this.successors = getSuccessors(getId());
         }
         return this.successors;
     }
 
     /**
      * @return the description
      */
     @Override
     public String getDescription() {
         return this.description;
     }
 
 }
