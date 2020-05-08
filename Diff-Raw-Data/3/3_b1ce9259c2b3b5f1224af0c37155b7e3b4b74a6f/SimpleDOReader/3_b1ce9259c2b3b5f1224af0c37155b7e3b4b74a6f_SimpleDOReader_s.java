 package fedora.server.storage;
 
 import fedora.server.Context;
 import fedora.server.Logging;
 import fedora.server.StdoutLogging;
 import fedora.server.errors.DatastreamNotFoundException;
 import fedora.server.errors.DisseminatorNotFoundException;
 import fedora.server.errors.MethodNotFoundException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.UnsupportedTranslationException;
 import fedora.server.storage.translation.DOTranslator;
 import fedora.server.storage.types.BasicDigitalObject;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DSBinding;
 import fedora.server.storage.types.DSBindingAugmented;
 import fedora.server.storage.types.DSBindingMapAugmented;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.MethodDefOperationBind;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.translation.DOTranslationUtility;
 import fedora.server.utilities.DateUtility;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 
 /**
  *
  * <p><b>Title:</b> SimpleDOReader.java</p>
  * <p><b>Description:</b> A DOReader backed by a DigitalObject.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class SimpleDOReader
         extends StdoutLogging
         implements DOReader {
 
     protected DigitalObject m_obj;
     private Context m_context;
     private RepositoryReader m_repoReader;
     private DOTranslator m_translator;
     private String m_exportFormat;
     private String m_storageFormat;
     private String m_encoding;
 
     private SimpleDateFormat m_formatter=
             new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 
     public SimpleDOReader(Context context, RepositoryReader repoReader,
             DOTranslator translator,
             String exportFormat, String storageFormat,
             String encoding,
             InputStream serializedObject, Logging logTarget)
             throws ObjectIntegrityException, StreamIOException,
             UnsupportedTranslationException, ServerException {
         super(logTarget);
         m_context=context;
         m_repoReader=repoReader;
         m_translator=translator;
         m_exportFormat=exportFormat;
 		m_storageFormat=storageFormat;
         m_encoding=encoding;
         m_obj=new BasicDigitalObject();
         m_translator.deserialize(serializedObject, m_obj, m_storageFormat,
         	encoding, DOTranslationUtility.DESERIALIZE_INSTANCE);
     }
 
     /**
      * Alternate constructor for when a DigitalObject is already available
      * for some reason.
      */
     public SimpleDOReader(Context context, RepositoryReader repoReader,
             DOTranslator translator,
             String exportFormat,
             String encoding, DigitalObject obj,
             Logging logTarget) {
         super(logTarget);
         m_context=context;
         m_repoReader=repoReader;
         m_translator=translator;
         m_exportFormat=exportFormat;
         m_encoding=encoding;
         m_obj=obj;
     }
 
     public String getFedoraObjectType() {
         int t=m_obj.getFedoraObjectType();
         if (t==DigitalObject.FEDORA_OBJECT) {
             return "O";
         } else {
             if (t==DigitalObject.FEDORA_BMECH_OBJECT) {
                 return "M";
             } else {
                 return "D";
             }
         }
     }
 
     public String getContentModelId() {
         return m_obj.getContentModelId();
     }
 
     public Date getCreateDate() {
         return m_obj.getCreateDate();
     }
 
     public Date getLastModDate() {
         return m_obj.getLastModDate();
     }
 
     public String getOwnerId() {
         return m_obj.getOwnerId();
     }
 
     public List getAuditRecords() {
         return m_obj.getAuditRecords();
     }
 
 	/**
 	 * Return the object as an XML input stream in the internal
 	 * serialization format.
 	 */
     public InputStream GetObjectXML()
             throws ObjectIntegrityException, StreamIOException,
             UnsupportedTranslationException, ServerException {
         ByteArrayOutputStream bytes=new ByteArrayOutputStream();
         m_translator.serialize(m_obj, bytes, m_storageFormat,
 			"UTF-8", DOTranslationUtility.SERIALIZE_STORAGE_INTERNAL);
         return new ByteArrayInputStream(bytes.toByteArray());
     }
 
 	/**
 	 * Return the object as an XML input stream in the specified XML
 	 * format and in the specified export context.
 	 * 
 	 * See DOTranslationUtility.class for description of export contexts
 	 * (translation contexts).
      *
      * @param format The format to export the object in.  If null or "default",
      *               will use the repository's configured default export format.
      * @param exportContext  The use case for export (public, migrate, archive)
      *               which results in different ways of representing datastream 
      *               URLs or datastream content in the output.
 	 */
     public InputStream ExportObject(String format, String exportContext)
             throws ObjectIntegrityException, StreamIOException,
             UnsupportedTranslationException, ServerException {
         ByteArrayOutputStream bytes=new ByteArrayOutputStream();
 		int transContext;        
 		// first, set the translation context...
 		if (fedora.server.Debug.DEBUG) 
 			System.out.println("SimpleDOReader.ExportObject export context: " + exportContext);
 
 		if (exportContext==null || exportContext.equals("") || 
 		    exportContext.equalsIgnoreCase("default")) {
 			// null and default is set to PUBLIC translation
 			transContext=DOTranslationUtility.SERIALIZE_EXPORT_PUBLIC; 
 		} else if (exportContext.equalsIgnoreCase("public")){
 			transContext=DOTranslationUtility.SERIALIZE_EXPORT_PUBLIC;
         } else if (exportContext.equalsIgnoreCase("migrate")){
 			transContext=DOTranslationUtility.SERIALIZE_EXPORT_MIGRATE;
 		} else if (exportContext.equalsIgnoreCase("archive")){
 			throw new UnsupportedTranslationException("Export context of 'archive' " +
				"is not supported in Fedora 2.0. Will be available in future.");
         } else {
 			throw new UnsupportedTranslationException("Export context " +
 				exportContext + " is not valid.");       	
         }
         // now serialize for export in the proper XML format...			        
 		if (format==null || format.equals("") || format.equalsIgnoreCase("default")) {
 			if (fedora.server.Debug.DEBUG) 
 				System.out.println("SimpleDOReader.ExportObject in default format: " + m_exportFormat);
 			m_translator.serialize(m_obj, bytes, m_exportFormat, "UTF-8", transContext);
 		}
 		else {
 			if (fedora.server.Debug.DEBUG) 
 				System.out.println("SimpleDOReader.ExportObject in format: " + format);
 			m_translator.serialize(m_obj, bytes, format, "UTF-8", transContext);
 		}
 
         return new ByteArrayInputStream(bytes.toByteArray());
     }
 
     public String GetObjectPID() {
         return m_obj.getPid();
     }
 
     public String GetObjectLabel() {
         return m_obj.getLabel();
     }
 
     public String GetObjectState() {
         if (m_obj.getState()==null) return "A"; // shouldn't happen, but if it does don't die
         return m_obj.getState();
     }
 
     public String[] ListDatastreamIDs(String state) {
         Iterator iter=m_obj.datastreamIdIterator();
         ArrayList al=new ArrayList();
         while (iter.hasNext()) {
             String dsId=(String) iter.next();
             if (state==null) {
                 al.add(dsId);
             } else {
                 // below should never return null -- already know id exists,
                 // and am asking for any the latest existing one.
                 Datastream ds=GetDatastream(dsId, null);
                 if (ds.DSState.equals(state)) {
                     al.add(dsId);
                 }
             }
         }
         iter=al.iterator();
         String[] out=new String[al.size()];
         int i=0;
         while (iter.hasNext()) {
             out[i]=(String) iter.next();
             i++;
         }
         return out;
     }
 
     // returns null if can't find
 	public Datastream getDatastream(String dsID, String versionID) {
 	    List allVersions=m_obj.datastreams(dsID);
         for (int i=0; i<allVersions.size(); i++) {
 		    Datastream ds=(Datastream) allVersions.get(i);
 			if (ds.DSVersionID.equals(versionID)) {
 			    return ds;
 			}
 		}
 		return null;
 	}
 
     // returns null if can't find
     public Datastream GetDatastream(String datastreamID, Date versDateTime) {
         List allVersions=m_obj.datastreams(datastreamID);
         if (allVersions.size()==0) {
             return null;
         }
         // get the one with the closest creation date
         // without going over
         Iterator dsIter=allVersions.iterator();
         Datastream closestWithoutGoingOver=null;
         Datastream latestCreated=null;
         long bestTimeDifference=-1;
         long latestCreateTime=-1;
         long vTime=-1;
         if (versDateTime!=null) {
             vTime=versDateTime.getTime();
         }
         while (dsIter.hasNext()) {
             Datastream ds=(Datastream) dsIter.next();
             if (versDateTime==null) {
                 if (ds.DSCreateDT.getTime() > latestCreateTime) {
                     latestCreateTime=ds.DSCreateDT.getTime();
                     latestCreated=ds;
                 }
             } else {
                 long diff=vTime-ds.DSCreateDT.getTime();
                 if (diff >= 0) {
                     if ( (diff < bestTimeDifference)
                             || (bestTimeDifference==-1) ) {
                         bestTimeDifference=diff;
                         closestWithoutGoingOver=ds;
                     }
                 }
             }
         }
         if (versDateTime==null) {
             return latestCreated;
         } else {
             return closestWithoutGoingOver;
         }
     }
 
     public Date[] getDatastreamVersions(String datastreamID) {
         List l=m_obj.datastreams(datastreamID);
         Date[] versionDates=new Date[l.size()];
         for (int i=0; i<l.size(); i++) {
             versionDates[i]=((Datastream) l.get(i)).DSCreateDT;
         }
         return versionDates;
     }
 
     public Date[] getDisseminatorVersions(String dissID) {
         List l=m_obj.disseminators(dissID);
         Date[] versionDates=new Date[l.size()];
         for (int i=0; i<l.size(); i++) {
             versionDates[i]=((Disseminator) l.get(i)).dissCreateDT;
         }
         return versionDates;
     }
 
     public Datastream[] GetDatastreams(Date versDateTime, String state) {
         String[] ids=ListDatastreamIDs(null);
         ArrayList al=new ArrayList();
         for (int i=0; i<ids.length; i++) {
            Datastream ds=GetDatastream(ids[i], versDateTime);
            if (ds!=null && (state==null || ds.DSState.equals(state)) ) {
                al.add(ds);
            }
         }
         Datastream[] out=new Datastream[al.size()];
         Iterator iter=al.iterator();
         int i=0;
         while (iter.hasNext()) {
             out[i]=(Datastream) iter.next();
             i++;
         }
         return out;
     }
 
     public String[] ListDisseminatorIDs(String state) {
         Iterator iter=m_obj.disseminatorIdIterator();
         ArrayList al=new ArrayList();
         while (iter.hasNext()) {
             String dissId=(String) iter.next();
             if (state==null) {
                 al.add(dissId);
             } else {
                 Disseminator diss=GetDisseminator(dissId, null);
                 if (diss.dissState.equals(state)) {
                     al.add(dissId);
                 }
             }
         }
         iter=al.iterator();
         String[] out=new String[al.size()];
         int i=0;
         while (iter.hasNext()) {
             out[i]=(String) iter.next();
             i++;
         }
         return out;
     }
 
     public Disseminator GetDisseminator(String disseminatorID, Date versDateTime) {
         List allVersions=m_obj.disseminators(disseminatorID);
         if (allVersions.size()==0) {
             return null;
         }
         // get the one with the closest creation date
         // without going over
         Iterator dissIter=allVersions.iterator();
         Disseminator closestWithoutGoingOver=null;
         Disseminator latestCreated=null;
         long bestTimeDifference=-1;
         long latestCreateTime=-1;
         long vTime=-1;
         if (versDateTime!=null) {
             vTime=versDateTime.getTime();
         }
         while (dissIter.hasNext()) {
             Disseminator diss=(Disseminator) dissIter.next();
             if (versDateTime==null) {
                 if (diss.dissCreateDT.getTime() > latestCreateTime) {
                     latestCreateTime=diss.dissCreateDT.getTime();
                     latestCreated=diss;
                 }
             } else {
                 long diff=vTime-diss.dissCreateDT.getTime();
                 if (diff >= 0) {
                     if ( (diff < bestTimeDifference)
                             || (bestTimeDifference==-1) ) {
                         bestTimeDifference=diff;
                         closestWithoutGoingOver=diss;
                     }
                 }
             }
         }
         if (versDateTime==null) {
             return latestCreated;
         } else {
             return closestWithoutGoingOver;
         }
     }
 
     public Disseminator[] GetDisseminators(Date versDateTime, String state) {
         String[] ids=ListDisseminatorIDs(null);
         ArrayList al=new ArrayList();
         for (int i=0; i<ids.length; i++) {
            Disseminator diss=GetDisseminator(ids[i], versDateTime);
            if (diss!=null && (state==null || diss.dissState.equals(state)) ) {
                al.add(diss);
            }
         }
         Disseminator[] out=new Disseminator[al.size()];
         Iterator iter=al.iterator();
         int i=0;
         while (iter.hasNext()) {
             out[i]=(Disseminator) iter.next();
             i++;
         }
         return out;
     }
 
     public String[] GetBehaviorDefs(Date versDateTime) {
         Disseminator[] disses=GetDisseminators(versDateTime, null);
         String[] bDefIds=new String[disses.length];
         for (int i=0; i<disses.length; i++) {
             bDefIds[i]=disses[i].bDefID;
         }
         return bDefIds;
     }
 
     /**
      * <p>Gets the change history of an object by returning a list of timestamps
      * that correspond to modification dates of components. This currently includes
      * changes to datastreams and disseminators.</p>
      *
      * @param PID The persistent identifier of the digitla object.
      * @return An Array containing the list of timestamps indicating when changes
      *         were made to the object.
      */
     public String[] getObjectHistory(String PID) {
         String[] dsIDs = ListDatastreamIDs("A");
         String[] dissIDs = ListDisseminatorIDs("A");
         TreeSet modDates = new TreeSet();
         for (int i=0; i<dsIDs.length; i++) {
             Date[] dsDates = getDatastreamVersions(dsIDs[i]);
             for (int j=0; j<dsDates.length; j++) {
                 modDates.add(DateUtility.convertDateToString(dsDates[j]));
             }
         }
         for (int i=0; i<dissIDs.length; i++) {
             Date[] dissDates = getDisseminatorVersions(dissIDs[i]);
             for (int j=0; j<dissDates.length; j++) {
                 modDates.add(DateUtility.convertDateToString(dissDates[j]));
             }
         }
 
         return (String[])modDates.toArray(new String[0]);
     }
 
     public MethodDef[] listMethods(String bDefPID, Date versDateTime)
             throws MethodNotFoundException, ServerException {
 
         if ( bDefPID.equalsIgnoreCase("fedora-system:1") ||
              bDefPID.equalsIgnoreCase("fedora-system:3"))
         {
           throw new MethodNotFoundException("[getObjectMethods] The object, "
             + m_obj.getPid()
             + ", will not report on dynamic method definitions "
             + "at this time (fedora-system:1 and fedora-system:3.");
         }
         String mechPid=getBMechPid(bDefPID, versDateTime);
         if (mechPid==null) {
             return null;
         }
         MethodDef[] methods = m_repoReader.getBMechReader(m_context, mechPid).
                 getServiceMethods(versDateTime);
         // Filter out parms that are internal to the mechanism and not part
         // of the abstract method definition.  We just want user parms.
         for (int i=0; i<methods.length; i++)
         {
           methods[i].methodParms = filterParms(methods[i]);
         }
         return methods;
     }
 
     /**
      * Get the parameters for a given method.  The parameters returned
      * will be those that pertain to the abstract method definition, meaning
      * they will only be user-supplied parms.  Mechanism-specific parms
      * (system default parms and datastream input parms) will be filtered out.
      * @param bDefPID
      * @param methodName
      * @param versDateTime
      * @return an array of method parameter definitions
      * @throws DisseminatorNotFoundException
      * @throws MethodNotFoundException
      * @throws ServerException
      */
     public MethodParmDef[] getObjectMethodParms(String bDefPID,
             String methodName, Date versDateTime)
             throws MethodNotFoundException, ServerException {
 
         if ( bDefPID.equalsIgnoreCase("fedora-system:1") ||
              bDefPID.equalsIgnoreCase("fedora-system:3"))
         {
           throw new MethodNotFoundException("[getObjectMethodParms] The object, "
             + m_obj.getPid()
             + ", will not report on dynamic method definitions "
             + "at this time (fedora-system:1 and fedora-system:3.");
         }
         // The parms are expressed in the abstract method definitions
         // in the behavior mechanism object. Note that the mechanism object
         // is used here as if it were a behavior definition object.
         String mechPid=getBMechPid(bDefPID, versDateTime);
         if (mechPid==null) {
             return null;
         }
         MethodDef[] methods = m_repoReader.getBMechReader(m_context, mechPid).
                 getServiceMethods(versDateTime);
         for (int i=0; i<methods.length; i++)
         {
           if (methods[i].methodName.equalsIgnoreCase(methodName))
           {
             return filterParms(methods[i]);
           }
         }
         throw new MethodNotFoundException("The object, " + m_obj.getPid()
                     + ", does not have a method named '" + methodName);
     }
 
     /**
      * Filter out mechanism-specific parms (system default parms and datastream
      * input parms) so that what is returned is only method parms that reflect
      * abstract method definitions.  Abstract method definitions only
      * expose user-supplied parms.
      * @param method
      * @return
      */
      private MethodParmDef[] filterParms(MethodDef method)
      {
         ArrayList filteredParms = new ArrayList();
         MethodParmDef[] parms = method.methodParms;
         for (int i=0; i<parms.length; i++)
         {
           if (parms[i].parmType.equalsIgnoreCase(MethodParmDef.USER_INPUT))
           {
             filteredParms.add(parms[i]);
           }
         }
         return (MethodParmDef[])filteredParms.toArray(new MethodParmDef[0]);
      }
 
     /**
      * Gets the bmech id for the disseminator subscribing to the bdef.
      *
      * @return null if it's the bootstrap bdef
      * @throws DisseminatorNotFoundException if no matching disseminator
      *         is found in the object.
      */
     private String getBMechPid(String bDefPID, Date versDateTime)
             throws DisseminatorNotFoundException {
         if (bDefPID.equals("fedora-system:1")) {
             return null;
         }
         Disseminator[] disses=GetDisseminators(versDateTime, null);
         String bMechPid=null;
         for (int i=0; i<disses.length; i++) {
             if (disses[i].bDefID.equals(bDefPID)) {
                bMechPid=disses[i].bMechID;
             }
         }
         if (bMechPid==null) {
             throw new DisseminatorNotFoundException("The object, "
                     + m_obj.getPid() + ", does not have a disseminator"
                     + " with bdef " + bDefPID + " at "
                     + getWhenString(versDateTime));
         }
         return bMechPid;
     }
 
     protected String getWhenString(Date versDateTime) {
         if (versDateTime!=null) {
             return m_formatter.format(versDateTime);
         } else {
             return "the current time";
         }
     }
 
     public DSBindingMapAugmented[] GetDSBindingMaps(Date versDateTime)
           throws ObjectIntegrityException, ServerException {
         Disseminator[] disses=GetDisseminators(versDateTime, null);
         DSBindingMapAugmented[] augMaps=new DSBindingMapAugmented[disses.length];
         for (int i=0; i<disses.length; i++) {
             DSBindingMapAugmented augMap=new DSBindingMapAugmented();
             augMap.dsBindMapID=disses[i].dsBindMap.dsBindMapID;
             augMap.dsBindMapLabel=disses[i].dsBindMap.dsBindMapLabel;
             augMap.dsBindMechanismPID=disses[i].dsBindMap.dsBindMechanismPID;
             DSBinding[] bindings=disses[i].dsBindMap.dsBindings;
             DSBindingAugmented[] augBindings=new DSBindingAugmented[bindings.length];
             for (int j=0; j<bindings.length; j++) {
                 DSBindingAugmented augBinding=new DSBindingAugmented();
                 augBinding.bindKeyName=bindings[j].bindKeyName;
                 augBinding.bindLabel=bindings[j].bindLabel;
                 augBinding.datastreamID=bindings[j].datastreamID;
                 augBinding.seqNo=bindings[j].seqNo;
                 // add values from the appropriate version of the datastream
                 Datastream ds=GetDatastream(bindings[j].datastreamID, versDateTime);
                 if (ds==null) {
                     String whenString=getWhenString(versDateTime);
                     throw new ObjectIntegrityException("The object, "
                             + m_obj.getPid() + ", does not have a datastream"
                             + " with id " + bindings[j].datastreamID
                             + " at " + whenString
                             + ", so the datastream binding map used by "
                             + "disseminator " + disses[i].dissID + " at "
                             + whenString + " is invalid.");
                 }
                 augBinding.DSVersionID=ds.DSVersionID;
                 augBinding.DSControlGrp=ds.DSControlGrp;
                 augBinding.DSLabel=ds.DSLabel;
                 augBinding.DSMIME=ds.DSMIME;
                 augBinding.DSLocation=ds.DSLocation;
                 augBindings[j]=augBinding;
             }
             augMap.dsBindingsAugmented=augBindings;
             augMaps[i]=augMap;
         }
         return augMaps;
     }
 
     private String getDisseminatorID(String bDefPID)
             throws DisseminatorNotFoundException {
         String[] ids=ListDisseminatorIDs(null);
         for (int i=0; i<ids.length; i++) {
             Disseminator diss=GetDisseminator(ids[i], null);
             if (diss.bDefID.equals(bDefPID)) {
                 return diss.dissID;
             }
         }
         throw new DisseminatorNotFoundException("Cannot find a disseminator "
                 + " subscribing to bdef " + bDefPID);
     }
 
     public DisseminationBindingInfo[] getDisseminationBindingInfo(String bDefPID,
           String methodName, Date versDateTime)
           throws ServerException {
         // Results will be returned in this array, one item per datastream
         DisseminationBindingInfo[] bindingInfo;
         // The disseminator provides the datastream bindings and the bmech pid,
         // which we need in order to construct the bindingInfo array.
         Disseminator diss=GetDisseminator(getDisseminatorID(bDefPID), versDateTime);
         if (diss==null) {
             throw new DisseminatorNotFoundException("Cannot get binding info "
                     + "for disseminator " + bDefPID + " because the disseminator"
                     + " was not found in this object.");
         }
         DSBinding[] dsBindings=diss.dsBindMap.dsBindings;
         int dsCount=dsBindings.length;
         bindingInfo=new DisseminationBindingInfo[dsCount];
         // The bmech reader provides information about the service and params.
         BMechReader mech=m_repoReader.getBMechReader(m_context, diss.bMechID);
         MethodParmDef[] methodParms=mech.getServiceMethodParms(methodName, versDateTime);
         // Find the operation bindings for the method in question
         MethodDefOperationBind[] opBindings=mech.getServiceMethodBindings(versDateTime);
         String addressLocation=null;
         String operationLocation=null;
         String protocolType=null;
         boolean foundMethod=false;
         for (int i=0; i<opBindings.length; i++) {
             if (opBindings[i].methodName.equals(methodName)) {
                 foundMethod=true;
                 addressLocation=opBindings[i].serviceBindingAddress;
                 operationLocation=opBindings[i].operationLocation;
                 protocolType=opBindings[i].protocolType;
             }
         }
         if (!foundMethod) {
             throw new MethodNotFoundException("Method " + methodName
                     + " was not found in " + diss.bMechID + "'s operation "
                     + " binding.");
         }
         // For each datastream referenced by the disseminator's ds bindings,
         // add an element to the output array which includes key information
         // on the operation and the datastream.
         for (int i=0; i<dsCount; i++) {
             String dsID=dsBindings[i].datastreamID;
             bindingInfo[i]=new DisseminationBindingInfo();
             bindingInfo[i].DSBindKey=dsBindings[i].bindKeyName;
             // get key info about the datastream and put it here
             Datastream ds=GetDatastream(dsID, versDateTime);
             if (ds == null) {
                 String message = "The object \""+GetObjectPID()+"\" "
                     + "contains no datastream for dsID \""+dsID+"\" "
                     + "that was created on or before the specified date/timestamp "
                     + " of \"" + DateUtility.convertDateToString(versDateTime)
                     + "\" .";
                 throw new DatastreamNotFoundException(message);
             }
             bindingInfo[i].dsLocation=ds.DSLocation;
             bindingInfo[i].dsControlGroupType=ds.DSControlGrp;
             bindingInfo[i].dsID=dsID;
             bindingInfo[i].dsVersionID=ds.DSVersionID;
             bindingInfo[i].dsState=ds.DSState;
             // these will be the same for all elements of the array
             bindingInfo[i].methodParms=methodParms;
             bindingInfo[i].AddressLocation=addressLocation;
             bindingInfo[i].OperationLocation=operationLocation;
             bindingInfo[i].ProtocolType=protocolType;
         }
         return bindingInfo;
     }
 
     public ObjectMethodsDef[] listMethods(Date versDateTime)
             throws ServerException {
         String[] ids=ListDisseminatorIDs("A");
         ArrayList methodList=new ArrayList();
         ArrayList bDefIDList=new ArrayList();
         for (int i=0; i<ids.length; i++) {
             Disseminator diss=GetDisseminator(ids[i], versDateTime);
             if (diss!=null) {
                 MethodDef[] methods=listMethods(diss.bDefID,
                         versDateTime);
                 if (methods!=null) {
                     for (int j=0; j<methods.length; j++) {
                         methodList.add(methods[j]);
                         bDefIDList.add(diss.bDefID);
                     }
                 }
             }
         }
         ObjectMethodsDef[] ret=new ObjectMethodsDef[methodList.size()];
         for (int i=0; i<methodList.size(); i++) {
             MethodDef def=(MethodDef) methodList.get(i);
             ret[i]=new ObjectMethodsDef();
             ret[i].PID=GetObjectPID();
             ret[i].bDefPID=(String) bDefIDList.get(i);
             ret[i].methodName=def.methodName;
             ret[i].methodParmDefs=def.methodParms;
             ret[i].asOfDate=versDateTime;
         }
         return ret;
     }
 
 }
