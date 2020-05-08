 /*******************************************************************************
  * Copyright (c) 2011 Nicolas Roduit.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Nicolas Roduit - initial API and implementation
  ******************************************************************************/
 package org.weasis.dicom;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.tool.dcmqr.DcmQR;
 import org.dcm4che2.tool.dcmqr.DcmQR.QueryRetrieveLevel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.weasis.dicom.EncryptionTLS.TLS;
 import org.weasis.launcher.wado.Patient;
 import org.weasis.launcher.wado.SOPInstance;
 import org.weasis.launcher.wado.Series;
 import org.weasis.launcher.wado.Study;
 
 public class BuildManifestDcmQR {
     /*
      * Two DICOM actors:
      * 
      * nodeCalling: DICOM node that makes the query
      * 
      * nodeSource: DICOM node that responds to the query (PACS)
      */
 
     private static final Logger LOGGER = LoggerFactory.getLogger(BuildManifestDcmQR.class);
 
     public static List<Patient> buildFromPatientID(DicomNode nodeSource, String callingAet, String patientID)
         throws Exception {
         if (patientID == null || patientID.trim().equals("")) {
             return null;
         }
         String[] matchingKeys = { Integer.toHexString(Tag.PatientID), patientID };
         String[] returnKeys =
             { Integer.toHexString(Tag.PatientName), Integer.toHexString(Tag.PatientBirthDate),
                 Integer.toHexString(Tag.PatientSex), Integer.toHexString(Tag.ReferringPhysicianName),
                 Integer.toHexString(Tag.StudyDescription) };
         List<DicomObject> studies =
             query(nodeSource, null, callingAet, QueryRetrieveLevel.STUDY, true, matchingKeys, returnKeys);
         List<Patient> patientList = new ArrayList<Patient>();
         if (studies != null) {
             Patient patient = null;
             if (studies.size() > 0) {
                 patient = getPatient(patientList, studies.get(0));
             }
             for (DicomObject studyDataSet : studies) {
                 String studyInstanceUID = studyDataSet.getString(Tag.StudyInstanceUID);
                 if (studyInstanceUID != null && !"".equals(studyInstanceUID.trim())) {
                     Study study = getStudy(patient, studyDataSet);
                     List<DicomObject> series =
                         query(nodeSource, null, callingAet, QueryRetrieveLevel.SERIES, true,
                             new String[] { Integer.toHexString(Tag.StudyInstanceUID), studyInstanceUID },
                             new String[] { Integer.toHexString(Tag.SeriesDescription) });
                     if (series != null) {
                         for (DicomObject seriesDataset : series) {
                             String serieInstanceUID = seriesDataset.getString(Tag.SeriesInstanceUID);
                             if (serieInstanceUID != null && !"".equals(serieInstanceUID.trim())) {
                                 Series s = getSeries(study, seriesDataset);
                                 List<DicomObject> instances =
                                     query(nodeSource, null, callingAet, QueryRetrieveLevel.IMAGE, true,
                                         new String[] { Integer.toHexString(Tag.StudyInstanceUID), studyInstanceUID,
                                             Integer.toHexString(Tag.SeriesInstanceUID), serieInstanceUID }, null);
                                 if (instances != null) {
                                     for (DicomObject instanceDataSet : instances) {
                                         String sopUID = instanceDataSet.getString(Tag.SOPInstanceUID);
                                         if (sopUID != null) {
                                             SOPInstance sop = new SOPInstance(sopUID);
                                             sop.setInstanceNumber(instanceDataSet.getString(Tag.InstanceNumber));
                                             s.addSOPInstance(sop);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return patientList;
     }
 
     public static List<Patient> buildFromStudyInstanceUID(DicomNode nodeSource, String callingAet,
         String studyInstanceUID) throws Exception {
         if (studyInstanceUID == null || studyInstanceUID.trim().equals("")) {
             return null;
         }
         String[] matchingKeys = { Integer.toHexString(Tag.StudyInstanceUID), studyInstanceUID };
         return buildFromStudylevel(nodeSource, callingAet, matchingKeys, Tag.StudyInstanceUID);
     }
 
     public static List<Patient> buildFromStudyAccessionNumber(DicomNode nodeSource, String callingAet,
         String accessionNumber) throws Exception {
         if (accessionNumber == null || accessionNumber.trim().equals("")) {
             return null;
         }
         String[] matchingKeys = { Integer.toHexString(Tag.AccessionNumber), accessionNumber };
         return buildFromStudylevel(nodeSource, callingAet, matchingKeys, Tag.AccessionNumber);
     }
 
     private static List<Patient> buildFromStudylevel(DicomNode nodeSource, String callingAet, String[] matchingKeys,
         int tag) throws Exception {
         String[] returnKeys =
             { Integer.toHexString(Tag.PatientName), Integer.toHexString(Tag.PatientID),
                 Integer.toHexString(Tag.PatientBirthDate), Integer.toHexString(Tag.PatientSex),
                 Integer.toHexString(Tag.ReferringPhysicianName), Integer.toHexString(Tag.StudyDescription) };
         List<DicomObject> studies =
             query(nodeSource, null, callingAet, QueryRetrieveLevel.STUDY, true, matchingKeys, returnKeys);
         List<Patient> patientList = new ArrayList<Patient>();
         if (studies != null && studies.size() > 0) {
             Patient patient = getPatient(patientList, studies.get(0));
             for (DicomObject studyDataSet : studies) {
                 if (matchingKeys[1].equals(studyDataSet.getString(tag))) {
                     Study study = getStudy(patient, studyDataSet);
                     List<DicomObject> series =
                         query(nodeSource, null, callingAet, QueryRetrieveLevel.SERIES, true, matchingKeys,
                             new String[] { Integer.toHexString(Tag.SeriesDescription) });
                     if (series != null) {
                         for (DicomObject seriesDataset : series) {
                             String seriesInstanceUID = seriesDataset.getString(Tag.SeriesInstanceUID);
                             if (seriesInstanceUID != null && !"".equals(seriesInstanceUID.trim())) {
                                 Series s = getSeries(study, seriesDataset);
                                 List<DicomObject> instances =
                                     query(
                                         nodeSource,
                                         null,
                                         callingAet,
                                         QueryRetrieveLevel.IMAGE,
                                         true,
                                         new String[] { Integer.toHexString(Tag.StudyInstanceUID),
                                             study.getStudyInstanceUID(), Integer.toHexString(Tag.SeriesInstanceUID),
                                             seriesInstanceUID }, null);
                                 if (instances != null) {
                                     for (DicomObject instanceDataSet : instances) {
                                         String sopUID = instanceDataSet.getString(Tag.SOPInstanceUID);
                                         if (sopUID != null) {
                                             SOPInstance sop = new SOPInstance(sopUID);
                                             sop.setInstanceNumber(instanceDataSet.getString(Tag.InstanceNumber));
                                             s.addSOPInstance(sop);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return patientList;
     }
 
     public static List<Patient> buildFromSeriesInstanceUID(DicomNode nodeSource, String callingAet,
         String seriesInstanceUID) throws Exception {
         if (seriesInstanceUID == null || seriesInstanceUID.trim().equals("")) {
             return null;
         }
         String[] matchingKeys = { Integer.toHexString(Tag.SeriesInstanceUID), seriesInstanceUID };
         String[] returnKeys =
             { Integer.toHexString(Tag.PatientName), Integer.toHexString(Tag.PatientID),
                 Integer.toHexString(Tag.PatientBirthDate), Integer.toHexString(Tag.PatientSex),
                 Integer.toHexString(Tag.StudyDate), Integer.toHexString(Tag.StudyTime),
                 Integer.toHexString(Tag.AccessionNumber), Integer.toHexString(Tag.ReferringPhysicianName),
                 Integer.toHexString(Tag.StudyDescription), Integer.toHexString(Tag.SeriesDescription),
                 Integer.toHexString(Tag.StudyInstanceUID) };
 
         List<DicomObject> series =
             query(nodeSource, null, callingAet, QueryRetrieveLevel.SERIES, true, matchingKeys, returnKeys);
         List<Patient> patientList = new ArrayList<Patient>();
         if (series != null && series.size() > 0) {
             DicomObject dataset = series.get(0);
             Patient patient = getPatient(patientList, dataset);
             Study study = getStudy(patient, dataset);
             for (DicomObject seriesDataset : series) {
                 if (seriesInstanceUID.equals(seriesDataset.getString(Tag.SeriesInstanceUID))) {
                     Series s = getSeries(study, seriesDataset);
                     List<DicomObject> instances =
                         query(nodeSource, null, callingAet, QueryRetrieveLevel.IMAGE, true,
                             new String[] { Integer.toHexString(Tag.StudyInstanceUID), study.getStudyInstanceUID(),
                                 Integer.toHexString(Tag.SeriesInstanceUID), seriesInstanceUID }, null);
                     if (instances != null) {
                         for (DicomObject instanceDataSet : instances) {
                             String sopUID = instanceDataSet.getString(Tag.SOPInstanceUID);
                             if (sopUID != null) {
                                 SOPInstance sop = new SOPInstance(sopUID);
                                 sop.setInstanceNumber(instanceDataSet.getString(Tag.InstanceNumber));
                                 s.addSOPInstance(sop);
                             }
                         }
                     }
                 }
             }
         }
         return patientList;
     }
 
     public static List<Patient> buildFromSopInstanceUID(DicomNode nodeSource, String callingAet, String sopInstanceUID)
         throws Exception {
         if (sopInstanceUID == null || sopInstanceUID.trim().equals("")) {
             return null;
         }
         String[] matchingKeys = { Integer.toHexString(Tag.SOPInstanceUID), sopInstanceUID };
         String[] returnKeys =
             { Integer.toHexString(Tag.PatientName), Integer.toHexString(Tag.PatientID),
                 Integer.toHexString(Tag.PatientBirthDate), Integer.toHexString(Tag.PatientSex),
                 Integer.toHexString(Tag.StudyDate), Integer.toHexString(Tag.StudyTime),
                 Integer.toHexString(Tag.AccessionNumber), Integer.toHexString(Tag.Modality),
                 Integer.toHexString(Tag.ReferringPhysicianName), Integer.toHexString(Tag.StudyDescription),
                 Integer.toHexString(Tag.SeriesDescription), Integer.toHexString(Tag.StudyInstanceUID),
                 Integer.toHexString(Tag.SeriesInstanceUID), Integer.toHexString(Tag.StudyID),
                 Integer.toHexString(Tag.SeriesNumber) };
         List<Patient> patientList = new ArrayList<Patient>();
         List<DicomObject> instances =
             query(nodeSource, null, callingAet, QueryRetrieveLevel.IMAGE, true, matchingKeys, returnKeys);
         if (instances != null && instances.size() > 0) {
             DicomObject dataset = instances.get(0);
             Patient patient = getPatient(patientList, dataset);
             Study study = getStudy(patient, dataset);
             Series s = getSeries(study, dataset);
             for (DicomObject instanceDataSet : instances) {
                 String sopUID = instanceDataSet.getString(Tag.SOPInstanceUID);
                 if (sopUID != null) {
                     SOPInstance sop = new SOPInstance(sopUID);
                     sop.setInstanceNumber(instanceDataSet.getString(Tag.InstanceNumber));
                     s.addSOPInstance(sop);
                 }
             }
         }
         return patientList;
     }
 
     protected static Patient getPatient(final List<Patient> patientList, final DicomObject patientDataset)
         throws Exception {
         if (patientDataset == null) {
             throw new IllegalArgumentException("patientDataset cannot be null");
         }
         String uid = patientDataset.getString(Tag.PatientID);
         for (Patient p : patientList) {
             if (p.getPatientID().equals(uid)) {
                 return p;
             }
         }
         Patient p = new Patient(uid);
         p.setPatientName(patientDataset.getString(Tag.PatientName));
         p.setPatientBirthDate(patientDataset.getString(Tag.PatientBirthDate));
         // p.setPatientBirthTime(patientDataset.getString(Tag.PatientBirthTime));
         p.setPatientSex(patientDataset.getString(Tag.PatientSex));
         patientList.add(p);
         return p;
     }
 
     protected static Study getStudy(Patient patient, final DicomObject studyDataset) throws Exception {
         if (studyDataset == null) {
             throw new IllegalArgumentException("studyDataset cannot be null");
         }
         String uid = studyDataset.getString(Tag.StudyInstanceUID);
         Study s = patient.getStudy(uid);
         if (s == null) {
             s = new Study(uid);
             s.setStudyDescription(studyDataset.getString(Tag.StudyDescription));
             s.setStudyDate(studyDataset.getString(Tag.StudyDate));
             s.setStudyTime(studyDataset.getString(Tag.StudyTime));
             s.setAccessionNumber(studyDataset.getString(Tag.AccessionNumber));
             s.setStudyID(studyDataset.getString(Tag.StudyID));
             s.setReferringPhysicianName(studyDataset.getString(Tag.ReferringPhysicianName));
             patient.addStudy(s);
         }
         return s;
     }
 
     protected static Series getSeries(Study study, final DicomObject seriesDataset) throws Exception {
         if (seriesDataset == null) {
             throw new IllegalArgumentException("seriesDataset cannot be null");
         }
         String uid = seriesDataset.getString(Tag.SeriesInstanceUID);
         Series s = study.getSeries(uid);
         if (s == null) {
             s = new Series(uid);
             s.setModality(seriesDataset.getString(Tag.Modality));
             s.setSeriesNumber(seriesDataset.getString(Tag.SeriesNumber));
             s.setSeriesDescription(seriesDataset.getString(Tag.SeriesDescription));
             study.addSeries(s);
         }
         return s;
     }
 
     public static List<DicomObject> query(DicomNode nodeSource, EncryptionTLS tls, String callingAet,
         QueryRetrieveLevel level, boolean relationQR, String[] matchingKeys, String[] returnKeys) {
         List<DicomObject> result = new ArrayList<DicomObject>();
         DcmQR dcmqr = new DcmQR(callingAet);
         boolean init = initQuery(nodeSource, tls, dcmqr, level, relationQR, matchingKeys, returnKeys);
         if (!init) {
             return null;
         }
         try {
             if (dcmqr.isCFind()) {
                 long t2 = System.currentTimeMillis();
                 result = dcmqr.query();
                 LOGGER.info("Received {} matching entries in {} s", Integer.valueOf(result.size()),
                     Float.valueOf((System.currentTimeMillis() - t2) / 1000f));
             }
             LOGGER.info("Released connection to " + nodeSource.getAet());
         } catch (IOException e) {
             LOGGER.error("ERROR: Failed to perform c-find:" + e.getMessage());
             LOGGER.debug(e.getMessage(), e);
             return null;
         } catch (InterruptedException e) {
             LOGGER.error("ERROR: Failed to execute c-find:" + e.getMessage());
             LOGGER.debug(e.getMessage(), e);
             return null;
         } finally {
             try {
                 dcmqr.close();
             } catch (InterruptedException e) {
             }
         }
         return result;
     }
 
     private static boolean initQuery(DicomNode nodeSource, EncryptionTLS tls, DcmQR dcmqr, QueryRetrieveLevel level,
         boolean relationQR, String[] matchingKeys, String[] returnKeys) {
         if (nodeSource == null && matchingKeys == null || (matchingKeys.length % 2) != 0) {
             throw new IllegalArgumentException();
         }
         if (level == null) {
             level = QueryRetrieveLevel.STUDY;
         }
         dcmqr.setCalledAET(nodeSource.getAet(), false);
         dcmqr.setRemoteHost(nodeSource.getHostname());
         dcmqr.setRemotePort(nodeSource.getPort());
         dcmqr.setPackPDV(true);
         dcmqr.setTcpNoDelay(true);
         dcmqr.setMaxOpsInvoked(1);
         dcmqr.setMaxOpsPerformed(0);
         dcmqr.setCFind(true);
         dcmqr.setCGet(false);
         dcmqr.setQueryLevel(level);
        // Add the default return keys for the selected query level
        dcmqr.addDefReturnKeys();
         dcmqr.setRelationQR(relationQR);
         // Manifest fields
         for (int i = 1; i < matchingKeys.length; i++, i++) {
             dcmqr.addMatchingKey(Tag.toTagPath(matchingKeys[i - 1]), matchingKeys[i]);
         }
         // DICOM fields to return
         if (returnKeys != null) {
             for (int i = 0; i < returnKeys.length; i++) {
                 dcmqr.addReturnKey(Tag.toTagPath(returnKeys[i]));
             }
         }
 
         dcmqr.configureTransferCapability(false);
 
         if (tls != null) {
             TLS cipher = tls.getTlsEncryption();
             if (TLS.NO_ENCRYPTION.equals(cipher)) {
                 dcmqr.setTlsWithoutEncyrption();
             } else if (TLS.TLS_3DSE.equals(cipher)) {
                 dcmqr.setTls3DES_EDE_CBC();
             } else if (TLS.TLS_AES.equals(cipher)) {
                 dcmqr.setTlsAES_128_CBC();
             } else {
                 LOGGER.error("Invalid parameter for TLS encryption: " + cipher);
                 return false;
             }
 
             dcmqr.setTlsProtocol(tls.getTlsProtocol());
             dcmqr.setTlsNeedClientAuth(tls.isNoclientauth());
 
             if (tls.getKeystore() != null) {
                 dcmqr.setKeyStoreURL(tls.getKeystore());
             }
             if (tls.getKeystorepw() != null) {
                 dcmqr.setKeyStorePassword(tls.getKeystorepw());
             }
             if (tls.getKeypw() != null) {
                 dcmqr.setKeyPassword(tls.getKeypw());
             }
             if (tls.getTruststore() != null) {
                 dcmqr.setTrustStoreURL(tls.getTruststore());
             }
             if (tls.getTruststorepw() != null) {
                 dcmqr.setTrustStorePassword(tls.getTruststorepw());
             }
             try {
                 dcmqr.initTLS();
             } catch (Exception e) {
                 LOGGER.error("ERROR: Failed to initialize TLS context:" + e.getMessage());
                 return false;
             }
         }
 
         long t1 = System.currentTimeMillis();
         try {
             dcmqr.open();
         } catch (Exception e) {
             LOGGER.error("ERROR: Failed to establish association:" + e.getMessage());
             LOGGER.debug(e.getMessage(), e);
             return false;
         }
         long t2 = System.currentTimeMillis();
         LOGGER.info("Connected to {} in {} s", nodeSource.getAet(), Float.valueOf((t2 - t1) / 1000f));
         return true;
     }
 
 }
