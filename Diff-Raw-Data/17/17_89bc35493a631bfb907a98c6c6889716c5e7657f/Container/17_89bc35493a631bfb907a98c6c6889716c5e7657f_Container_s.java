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
 package de.escidoc.core.common.business.fedora.resources;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.interfaces.ContainerInterface;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.DcReadHandler;
 import de.escidoc.core.common.util.stax.handler.RelsExtContentRelationsReadHandler;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 /**
  * Implementation of a Fedora Container Object which consist of datastreams
  * managed in Fedora Digital Repository System.
  * 
  * @author FRS
  * 
  */
 public class Container extends GenericVersionableResourcePid
     implements ContainerInterface {
 
     private static AppLogger log = new AppLogger(Container.class.getName());
 
     private Datastream dc = null;
 
     private Map<String, Datastream> mdRecords = null;
 
     private Datastream cts;
 
     private String contextId = null;
 
     private Datastream escidocRelsExt = null;
 
     public static final String DATASTREAM_ESCIDOC_RELS_EXT = "ESCIDOC_RELS_EXT";
 
     /**
      * Constructor of Container with specified id. The datastreams are
      * instantiated and retrieved if the related getter is called.
      * 
      * @param id
      *            The id of an container managed in Fedora.
      * @throws StreamNotFoundException
      *             Thrown if a datastream could not be found.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws ResourceNotFoundException
      *             Thrown if no container could be found under the provided id.
      */
     public Container(final String id) throws StreamNotFoundException,
         SystemException, ResourceNotFoundException {
 
         super(id);
         setPropertiesNames(expandPropertiesNames(getPropertiesNames()),
             expandPropertiesNamesMapping(getPropertiesNamesMapping()));
 
         Utility.getInstance().checkIsContainer(getId());
 
         setHref(Constants.CONTAINER_URL_BASE + getId());
         getVersionData();
         if (getVersionNumber() != null) {
             // setVersionData();
             setDcData();
         }
         this.mdRecords = new HashMap<String, Datastream>();
         // getSomeValuesFromFedora();
     }
 
     /**
      * Get creation date of a versionated resource.
      * 
      * Attention: The creation date of a resource differs from the creation date
      * in the Fedora resource.
      * 
      * @return creation date
      * 
      * @throws TripleStoreSystemException
      *             Thrown if request to TripleStore failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     @Override
     public String getCreationDate() throws TripleStoreSystemException,
         WebserverSystemException {
 
         if (this.creationDate == null) {
             /*
              * The creation version date is the date of the first version. This
              * is not the creation date of the Fedora object! With Fedora
              * 3.2/3.3 is the date indirectly obtained from the date of the
              * first RELS-EXT version or from the version/date entry of the
              * second version of RELS-EXT or the 'created date' of the RELS-EXT
              * datastream.
              * 
              * Another way would be to obtain the creation date from the WOV.
              * 
              * The current implementation derives the creation date from the
              * 'created Date' of the RELS-EXT datastream.
              */
 
             try {
                 org.fcrepo.server.types.gen.Datastream[] datastreams =
                     getFedoraUtility().getDatastreamHistory(getId(),
                         DATASTREAM_ESCIDOC_RELS_EXT);
                 this.creationDate =
                     datastreams[datastreams.length - 1].getCreateDate();
             }
             catch (FedoraSystemException e) {
                 throw new WebserverSystemException(e);
             }
 
         }
         return this.creationDate;
     }
 
     /**
      * Obtain title and description from DC an write them to properties map.
      * 
      * @throws XmlParserSystemException
      *             Thrown if parsing of DC failed.
      */
     private void setDcData() throws XmlParserSystemException {
         /*
          * TODO maybe parsing could be removed, because values from DC are
          * written to TrippleSTore by Fedora.
          */
         // parse DC data stream
         StaxParser sp = new StaxParser();
         DcReadHandler dch = new DcReadHandler(sp);
         sp.addHandler(dch);
         try {
             sp.parse(this.getDc().getStream());
         }
         catch (Exception e) {
             throw new XmlParserSystemException("Unexpected exception.", e);
         }
         setTitle(dch.getPropertiesMap().get(Elements.ELEMENT_DC_TITLE));
         setDescription(dch.getPropertiesMap().get(Elements.ELEMENT_DESCRIPTION));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource
      * #getGenericProperties()
      */
     public Datastream getCts() throws StreamNotFoundException,
         FedoraSystemException {
         if (this.cts == null) {
             try {
                 this.cts =
                     new Datastream(Elements.ELEMENT_CONTENT_MODEL_SPECIFIC,
                         getId(), getVersionDate());
             }
             catch (WebserverSystemException e) {
                 throw new FedoraSystemException(e);
             }
         }
         return cts;
     }
 
     /**
      * 
      * @param ds
      * @throws StreamNotFoundException
      * @throws FedoraSystemException
      * @throws TripleStoreSystemException
      * @throws WebserverSystemException
      */
     public void setCts(Datastream ds) throws StreamNotFoundException,
         FedoraSystemException, TripleStoreSystemException,
         WebserverSystemException {
         try {
             Datastream curDs = getCts();
             if (!ds.equals(curDs)) {
                 ds.merge();
                 this.cts = ds;
             }
         }
         catch (StreamNotFoundException e) {
             // this is not an update; its a create
             ds.persist(false);
             this.cts = ds;
         }
         // FedoraException when datastreams are preinitialized (see Item) and
         // getCts does not throw an exception on non-existing datastream.
         catch (final FedoraSystemException e) {
             // this is not an update; its a create
             ds.persist(false);
             this.cts = ds;
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @return MdRecords HashMap
      * 
      * @throws IntegritySystemException
      * @throws FedoraSystemException
      * @see de.escidoc.core.common.business.fedora.resources.interfaces.ContainerInterface#getMdRecords()
      */
     public Map<String, Datastream> getMdRecords()
        throws IntegritySystemException, FedoraSystemException {
 
         Map<String, Datastream> result = new HashMap<String, Datastream>();
         org.fcrepo.server.types.gen.Datastream[] datastreams =
            getDatastreamsInformation();
 
         Vector<String> names = new Vector<String>();
         for (int i = 0; i < datastreams.length; i++) {
             List<String> altIDs = Arrays.asList(datastreams[i].getAltIDs());
             if (altIDs != null
                 && altIDs.contains(Datastream.METADATA_ALTERNATE_ID)) {
                 names.add(datastreams[i].getID());
             }
         }
         Iterator<String> namesIter = names.iterator();
         while (namesIter.hasNext()) {
             String name = namesIter.next();
             try {
                 Datastream newDs =
                     new Datastream(name, getId(), getVersionDate());
                 result.put(name, newDs);
             }
             catch (StreamNotFoundException e) {
                 final String message =
                     "Metadata record \"" + name + "\" not found for container "
                         + getId() + ".";
                 log.error(message, e);
                 throw new IntegritySystemException(message, e);
             }
             catch (WebserverSystemException e) {
                 throw new FedoraSystemException(e);
             }
 
         }
         this.mdRecords = result;
         return result;
     }
 
     /**
      * 
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @see de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource#setMdRecords(java.util.HashMap)
      */
     public void setMdRecords(final HashMap<String, Datastream> mdRecords)
         throws FedoraSystemException, WebserverSystemException,
         TripleStoreSystemException, IntegritySystemException,
         EncodingSystemException {
         // check if mdRecords is set, contains all meta data
         // data streams, is equal to given mdRecords and save every
         // changed data stream to fedora
 
         // get list of names of data streams with alternateId = "metadata"
         Set<String> namesInFedora = getMdRecords().keySet();
         // delete data streams which are in fedora but not in mdRecords
         Iterator<String> fedoraNamesIt = namesInFedora.iterator();
         while (fedoraNamesIt.hasNext()) {
             String nameInFedora = fedoraNamesIt.next();
             if (!mdRecords.containsKey(nameInFedora)) {
                 Datastream fedoraDs = null;
                 try {
                     fedoraDs = getMdRecord(nameInFedora);
                     if(fedoraDs != null) {
                         // FIXME remove the entire datastream
                         fedoraDs.delete();
                     }
                 } catch (StreamNotFoundException e) {
                     log.warn("Failed to set MdRecords.");
                 }
             }
         }
 
         Iterator<String> nameIt = mdRecords.keySet().iterator();
         // create or activate data streams which are in mdRecords but not in
         // fedora
         while (nameIt.hasNext()) {
             String name = nameIt.next();
             if (!namesInFedora.contains(name)) {
 
                 Datastream currentMdRecord = mdRecords.get(name);
                 byte[] stream;
                 stream = currentMdRecord.getStream();
                 Vector<String> altIds = currentMdRecord.getAlternateIDs();
                 String[] altIDs = new String[altIds.size()];
                 for (int i = 0; i < altIds.size(); i++) {
                     altIDs[i] = altIds.get(i);
                 }
                 getFedoraUtility().addDatastream(getId(), name, altIDs,
                     "md-record", true, stream, false);
                 this.mdRecords.put(name, currentMdRecord);
                 nameIt.remove();
             }
         }
         Iterator<String> nameItNew = mdRecords.keySet().iterator();
         while (nameItNew.hasNext()) {
             String name = nameItNew.next();
             setMdRecord(name, mdRecords.get(name));
         }
 
     }
 
     /**
      * 
      * @see de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource#getMdRecord(java.lang.String)
      */
     public Datastream getMdRecord(final String name)
         throws StreamNotFoundException, FedoraSystemException {
         // check if the ds is set
         if (!this.mdRecords.containsKey(name)) {
             // retrieve from fedora and add to map
             Datastream ds;
             try {
                 ds = new Datastream(name, getId(), getVersionDate());
             }
             catch (WebserverSystemException e) {
                 log.error(e);
                 throw new FedoraSystemException(e);
             }
             this.mdRecords.put(name, ds);
         }
         return this.mdRecords.get(name);
     }
 
     /**
      * 
      * @throws WebserverSystemException
      *             Thrown in case of an internal error.
      * @see de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource#setMdRecord(java.lang.String,
      *      de.escidoc.core.common.business.fedora.datastream.Datastream)
      */
     public void setMdRecord(final String name, final Datastream ds)
         throws WebserverSystemException, FedoraSystemException,
         TripleStoreSystemException, EncodingSystemException,
         IntegritySystemException {
         // check if the metadata datastream is set, is equal to ds and save to
         // fedora
 
         // don't trust the handler
         // ds.addAlternateId("metadata");
         String type = ds.getAlternateIDs().get(1);
         String schema = ds.getAlternateIDs().get(2);
         String mimeType = ds.getMimeType();
         try {
             Datastream curDs = getMdRecord(name);
             String curMimeType = curDs.getMimeType();
             String curType = "";
             String curSchema = "";
             Vector<String> altIds = curDs.getAlternateIDs();
             if (altIds.size() > 1) {
                 curType = altIds.get(1);
                 if (altIds.size() > 2) {
                     curSchema = altIds.get(2);
                 }
             }
             boolean contentChanged = false;
             if (!ds.equals(curDs)) {
                 contentChanged = true;
             }
             if (contentChanged || !type.equals(curType)
                 || !schema.equals(curSchema) || !mimeType.equals(curMimeType)) {
                 if (contentChanged && name.equals("escidoc")) {
 
                     HashMap<String, String> mdProperties = ds.getProperties();
                     if (mdProperties != null) {
                         if (mdProperties.containsKey("nsUri")) {
                             String nsUri = mdProperties.get("nsUri");
                             String dcNewContent =
                                 XmlUtility
                                     .createDC(
                                         nsUri,
                                         ds.toStringUTF8(),
                                         getId(),
                                         getResourcePropertiesValue(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_ID));
                             Datastream dcNew = null;
                             if (dcNewContent != null
                                 && dcNewContent.trim().length() > 0) {
                                 try {
                                     dcNew =
                                         new Datastream(
                                             "DC",
                                             getId(),
                                             dcNewContent
                                                 .getBytes(XmlUtility.CHARACTER_ENCODING),
                                             "text/xml");
                                 }
                                 catch (UnsupportedEncodingException e) {
                                     throw new EncodingSystemException(e);
                                 }
                                 setDc(dcNew);
                             }
                         }
                         else {
                             String message =
                                 "namespace uri of 'escidoc' metadata"
                                     + " does not set in datastream.";
 
                             log.error(message);
                             throw new IntegritySystemException(message);
                         }
                     }
                     else {
                         String message =
                             "Properties of 'md-record' datastream"
                                 + " with then name 'escidoc' does not exist";
                         log.error(message);
                         throw new IntegritySystemException(message);
                     }
                 }
 
                 // ds.replaceAlternateId(type, 1);
                 this.mdRecords.put(name, ds);
                 ds.merge();
 
             }
         }
         catch (StreamNotFoundException e) {
             // this is not an update; its a create
             ds.addAlternateId(type);
             ds.addAlternateId(schema);
             this.mdRecords.put(name, ds);
             ds.persist(false);
         }
     }
 
     public Datastream getMembers() {
         // TODO Auto-generated method stub
         return null;
     }
 
     /**
      * Get the ESCIDOC_RELS_EXT for the corresponding version of the Resource.
      * 
      * @return ESCIDOC_RELS_EXT corresponding to the Resource version.
      * @throws StreamNotFoundException
      *             Thrown if the ESCIDOC_RELS_EXT data stream (with specified
      *             version) was not found.
      * @throws FedoraSystemException
      *             Thrown in case of internal error.
      */
 
     public Datastream getEscidocRelsExt() throws StreamNotFoundException,
         FedoraSystemException {
 
         if (this.escidocRelsExt == null) {
             try {
                 // TODO: USe a different constructor to set a correct control
                 // group
                 if (isLatestVersion()) {
                     setEscidocRelsExt(new Datastream(
                         DATASTREAM_ESCIDOC_RELS_EXT, getId(), null));
                 }
                 else {
                     setEscidocRelsExt(new Datastream(
                         DATASTREAM_ESCIDOC_RELS_EXT, getId(), getVersionDate()));
                 }
             }
             catch (WebserverSystemException e) {
                 throw new FedoraSystemException(e);
             }
         }
 
         return this.escidocRelsExt;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param ds
      *            The ESCIDOC_RELS_EXT datasream.
      * @throws StreamNotFoundException
      *             Thrown if the datastream was not found.
      * @throws FedoraSystemException
      *             Thrown if Fedora request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      * @see de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource#setRelsExt(de.escidoc.core.common.business.fedora.datastream.Datastream)
      */
     public void setEscidocRelsExt(final Datastream ds)
         throws StreamNotFoundException, FedoraSystemException,
         WebserverSystemException {
 
         if (this.escidocRelsExt == null) {
             this.escidocRelsExt = ds;
 
         }
         else if (!this.escidocRelsExt.equals(ds)) {
             this.escidocRelsExt = ds;
 
         }
     }
 
     public void setMembers(Datastream ds) {
         // TODO Auto-generated method stub
 
     }
 
     /**
      * Persists the whole object to Fedora.
      * 
      * @return lastModificationDate of the resource (Attention this timestamp
      *         differs from the last-modification timestamp of the repository.
      *         See Versioning Concept.)
      * 
      * @throws FedoraSystemException
      *             Thrown if connection to Fedora failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     @Override
     public String persist() throws FedoraSystemException,
         WebserverSystemException {
         /*
          * Persist persists the data streams of the object and updates all
          * version depending values. These values are RELS-EXT (version/date)
          * and WOV timestamp.
          * 
          * It is assumed that all data (except timestamp information) are
          * up-to-date in the datastreams! Afterwards should no operations be
          * necessary.
          * 
          * Procedure to persist an resource with versions:
          * 
          * 1.write ESCIDOC_RELS_EXT with a content of the RELS-EXT data stream.
          * 
          * 2. get last-modifcation-date from fedora object (We need the
          * timestamp from ESCIDOC_RELS_EXT precisely. But if no other method
          * writes (hopefully) to this object so we can use the object
          * timestamp.)
          * 
          * 3. write the timestamp to the WOV
          * 
          * 4. Update RELS-EXT (/version/date) with the timestamp (which is
          * written to WOV)
          * 
          * Note: These are to many data stream updates to write one single
          * information (timestamp)
          */
         String timestamp = null;
         if (this.needSync) {
             // ----------------------------------------------
             // writing RELS-EXT once (problem: /version/date is to old)
             // timestamp = getLastFedoraModificationDate();
             //
             // updateRelsExtVersionTimestamp(timestamp);
             // persistRelsExt();
             // updateWovTimestamp(getVersionNumber(), timestamp);
             // persistWov();
 
             // ----------------------------------------------
             // writing RELS-EXT twice.
             timestamp = persistEscidocRelsExt();
             if (timestamp == null) {
                 // timestamp = getLastModificationDate();
                 timestamp = getLastFedoraModificationDate();
             }
             updateWovTimestamp(getVersionNumber(), timestamp);
             persistWov();
             updateRelsExtVersionTimestamp(timestamp);
             persistRelsExt();
             setResourceProperties(PropertyMapKeys.LAST_MODIFICATION_DATE,
                 timestamp);
         }
 
         // if (sync) {
         getFedoraUtility().sync();
         // }
 
         // getsomevaluesfromFedora();
         return timestamp;
     }
 
     /**
      * 
      * @param isRoot
      * @return Vector with HashMaps of relations.
      * @throws FedoraSystemException
      * @throws IntegritySystemException
      * @throws XmlParserSystemException
      * @throws WebserverSystemException
      * @throws SystemException
      */
     public Vector<HashMap<String, String>> getRelations()
         throws FedoraSystemException, IntegritySystemException,
         XmlParserSystemException, WebserverSystemException {
 
         Datastream datastreamWithRelations = null;
         try {
             if (getVersionNumber() == null) {
                 datastreamWithRelations = getRelsExt();
             }
             else {
                 datastreamWithRelations = getEscidocRelsExt();
             }
         }
         catch (StreamNotFoundException e1) {
             throw new IntegritySystemException("Datastream not found.", e1);
         }
         byte[] datastreamWithRelationsContent =
             datastreamWithRelations.getStream();
 
         ByteArrayInputStream relsExtInputStream;
         StaxParser sp = new StaxParser();
         relsExtInputStream =
             new ByteArrayInputStream(datastreamWithRelationsContent);
 
         RelsExtContentRelationsReadHandler reHandler =
             new RelsExtContentRelationsReadHandler(sp);
         sp.addHandler(reHandler);
         try {
             sp.parse(relsExtInputStream);
         }
         catch (WebserverSystemException e) {
             throw e;
         }
         catch (Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         Vector<HashMap<String, String>> relations = reHandler.getRelations();
         return relations;
     }
 
     /**
      * Write ESCIDOC_RELS_EXT to Fedora.
      * 
      * @return new timestamp of data stream (or null if not updated)
      * 
      * @throws FedoraSystemException
      *             Thrown if connection to Fedora failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     protected String persistEscidocRelsExt() throws FedoraSystemException,
         WebserverSystemException {
 
         String timestamp = null; // Maybe would it be better, if we use the
         // old timestamp instead of null.
         try {
             if (this.escidocRelsExt != null) {
                 this.escidocRelsExt.setStream(getRelsExt().getStream());
             }
             else {
                 escidocRelsExt =
                     new Datastream(DATASTREAM_ESCIDOC_RELS_EXT, getId(),
                         getRelsExt().getStream(), "text/xml");
                 escidocRelsExt.setControlGroup("M");
                 setEscidocRelsExt(escidocRelsExt);
 
             }
         }
         catch (StreamNotFoundException e) {
             String message =
                 "RELS-EXT datastream not found in" + " container with id "
                     + getId();
             log.error(message);
             throw new WebserverSystemException(message, e);
         }
 
         timestamp = this.escidocRelsExt.merge();
 
         return timestamp;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource
      * #setDc(de.escidoc.core.common.business.fedora.datastream.Datastream)
      */
     public void setDc(Datastream ds) throws StreamNotFoundException,
         FedoraSystemException, WebserverSystemException,
         TripleStoreSystemException {
         // TODO should lock only be checked in handler?
         // if (this.isLocked) {
         // throw new LockingException("Item " + getId() + " is locked.");
         // }
         // check if relsExt is set, is equal to ds and save to fedora
         try {
 
             Datastream curDs = getDc();
 
             if (!ds.equals(curDs)) {
 
                 this.dc = ds;
                 ds.merge();
             }
         }
         catch (StreamNotFoundException e) {
             // An item have to have a RELS-EXT datastream
             throw new StreamNotFoundException(
                 "No DC for item " + getId() + ".", e);
         }
         getSomeValuesFromFedora();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource
      * #getDc()
      */
     public Datastream getDc() throws StreamNotFoundException,
         FedoraSystemException {
         if (this.dc == null) {
             try {
                 this.dc =
                     new Datastream(Datastream.DC_DATASTREAM, getId(),
                         getVersionDate());
             }
             catch (WebserverSystemException e) {
                 throw new FedoraSystemException(e);
             }
         }
         return this.dc;
     }
 
     /**
      * Get the Id of the context.
      * 
      * @return context id
      * 
      * @throws WebserverSystemException
      *             Thrown if the id could not retrieved from the TripleStore.
      */
     public String getContextId() throws WebserverSystemException {
 
         if (this.contextId == null) {
             try {
                 this.contextId =
                     TripleStoreUtility.getInstance().getPropertiesElements(
                         getId(), TripleStoreUtility.PROP_CONTEXT_ID);
             }
             catch (final TripleStoreSystemException e) {
                 throw new WebserverSystemException(e);
             }
         }
 
         return this.contextId;
     }
 
     /**
      * Get the href of the Context.
      * 
      * @return href of context.
      * 
      * @throws WebserverSystemException
      *             Thrown if determining of contextId failed.
      */
     public String getContextHref() throws WebserverSystemException {
 
         return Constants.CONTEXT_URL_BASE + getContextId();
     }
 
     /**
      * The Title of the Container Context.
      * 
      * @return title of Context
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     public String getContextTitle() throws WebserverSystemException {
 
         String contextTitle;
 
         try {
             contextTitle =
                 TripleStoreUtility.getInstance().getPropertiesElements(getId(),
                     TripleStoreUtility.PROP_CONTEXT_TITLE);
         }
         catch (final TripleStoreSystemException e) {
             throw new WebserverSystemException(e);
         }
 
         return contextTitle;
     }
 
     /**
      * Expand a list with names of properties values with the propertiesNames
      * for a versionated resource. These list could be used to request the
      * TripleStore.
      * 
      * @param propertiesNames
      *            Collection of propertiesNames. The collection contains only
      *            the version resource specific propertiesNames.
      * @return Parameter name collection
      */
     private Collection<String> expandPropertiesNames(
         final Collection<String> propertiesNames) {
 
         Collection<String> newPropertiesNames;
         if (propertiesNames != null) {
             newPropertiesNames = propertiesNames;
         }
         else {
             newPropertiesNames = new Vector<String>();
         }
 
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_MODEL_TITLE);
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_CATEGORY);
 
         return newPropertiesNames;
     }
 
     /**
      * Expanding the properties naming map.
      * 
      * @param propertiesMapping
      *            The properties name mapping from external as key and the
      *            internal name as value. E.g. with the key "version-status" and
      *            "LATEST_VERSION_STATUS" as value is the value of
      *            "versin-status" after the mapping accessible with the internal
      *            key "LATEST_VERSION_STATUS".
      * @return The key mapping.
      */
     private HashMap<String, String> expandPropertiesNamesMapping(
         final HashMap<String, String> propertiesMapping) {
 
         HashMap<String, String> newPropertiesNames;
         if (propertiesMapping != null) {
             newPropertiesNames = propertiesMapping;
         }
         else {
             newPropertiesNames = new HashMap<String, String>();
         }
 
         newPropertiesNames.put(TripleStoreUtility.PROP_LATEST_VERSION_PID,
             PropertyMapKeys.LATEST_VERSION_PID);
         newPropertiesNames.put(TripleStoreUtility.PROP_CONTENT_CATEGORY,
             PropertyMapKeys.LATEST_VERSION_CONTENT_CATEGORY);
         // FIXME release is a method of Item/Container so this is to move higher
         // within the hirarchie
         newPropertiesNames.put(TripleStoreUtility.PROP_LATEST_RELEASE_PID,
             PropertyMapKeys.LATEST_RELEASE_PID);
 
         return newPropertiesNames;
     }
 
     /**
      * Get Whole Object Version datastream (WOV).
      * 
      * @return WOV
      * @throws StreamNotFoundException
      *             Thrown if wov datastream was not found.
      * @throws FedoraSystemException
      *             Thrown in case of Fedora error.
      */
     public Datastream getWov() throws StreamNotFoundException,
         FedoraSystemException {
 
         if (this.wov == null) {
             this.wov = new Datastream(DATASTREAM_WOV, getId(), null);
             this.wov.setControlGroup("M");
         }
         return this.wov;
     }
 
     /**
      * Write the WOV data stream to Fedora repository.
      * 
      * @param ds
      *            The WOV data stream.
      * @throws StreamNotFoundException
      *             Thrown if the WOV data stream was not found.
      * @throws FedoraSystemException
      *             Thrown in case of Fedora error.
      * @throws TripleStoreSystemException
      *             Thrown if request of TripleStore failed.
      * @throws IntegritySystemException
      *             Thrown if data integrity is violated.
      * @throws WebserverSystemException
      *             Thrown in case of internal error.
      */
     public void setWov(final Datastream ds) throws FedoraSystemException,
         StreamNotFoundException {
         if (!getWov().equals(ds)) {
             ds.setControlGroup("M");
             this.wov = ds;
             this.needSync = true;
         }
     }
 }
