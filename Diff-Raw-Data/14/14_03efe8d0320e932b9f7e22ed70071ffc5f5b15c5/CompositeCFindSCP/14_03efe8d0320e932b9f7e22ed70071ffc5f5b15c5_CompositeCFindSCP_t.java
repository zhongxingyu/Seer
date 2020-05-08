 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4chee.archive.beans.query;
 
 
 import org.dcm4che.data.Attributes;
 import org.dcm4che.data.AttributesValidator;
 import org.dcm4che.data.Tag;
 import org.dcm4che.data.VR;
 import org.dcm4che.net.ApplicationEntity;
 import org.dcm4che.net.Association;
 import org.dcm4che.net.Device;
 import org.dcm4che.net.Status;
 import org.dcm4che.net.pdu.ExtendedNegotiation;
 import org.dcm4che.net.pdu.QueryOption;
 import org.dcm4che.net.service.BasicCFindSCP;
 import org.dcm4che.net.service.DicomServiceException;
 import org.dcm4che.net.service.Matches;
 import org.dcm4chee.archive.beans.util.JNDIUtils;
 import org.dcm4chee.archive.ejb.query.InstanceQuery;
 import org.dcm4chee.archive.ejb.query.PatientQuery;
 import org.dcm4chee.archive.ejb.query.SeriesQuery;
 import org.dcm4chee.archive.ejb.query.StudyQuery;
 import org.dcm4chee.archive.persistence.AttributeFilter;
 
 /**
  * @author Gunter Zeilinger <gunterze@gmail.com>
  */
 public class CompositeCFindSCP extends BasicCFindSCP {
 
     private final String[] qrLevels;
     private boolean matchUnknown;
 
     public CompositeCFindSCP(Device device, String sopClass,
             String... qrLevels) {
         super(device, sopClass);
         this.qrLevels = qrLevels;
     }
 
     @Override
     protected Matches calculateMatches(Association as, Attributes rq,
             Attributes keys) throws DicomServiceException {
         AttributesValidator validator = new AttributesValidator(keys);
         String level = validator.getType1String(
                 Tag.QueryRetrieveLevel, 0, 1, qrLevels);
         check(rq, validator);
         try {
             switch (level.charAt(1)) {
             case 'A':
                 return patientMatches(as, rq, keys);
             case 'T':
                 return studyMatches(as, rq, keys);
             case 'E':
                 return seriesMatches(as, rq, keys);
             case 'M':
                 return instanceMatches(as, rq, keys);
             }
         } catch (Exception e) {
             throw new DicomServiceException(rq, Status.UnableToProcess, e);
         }
         throw new AssertionError();
     }
 
     @Override
     protected Attributes adjust(Attributes match, Attributes keys,
             Association as) {
         Attributes filtered = new Attributes(match.size());
         filtered.setString(Tag.QueryRetrieveLevel, VR.CS,
                 keys.getString(Tag.QueryRetrieveLevel, null));
         filtered.addSelected(match, Tag.SpecificCharacterSet,
                 Tag.RetrieveAETitle, Tag.InstanceAvailability);
         filtered.addSelected(match, keys);
         return filtered;
     }
 
     private Matches patientMatches(Association as, Attributes rq,
             Attributes keys) throws Exception {
         PatientQuery query = (PatientQuery) JNDIUtils.lookup(PatientQuery.JNDI_NAME);
         String cuid = rq.getString(Tag.AffectedSOPClassUID);
         ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
         ApplicationEntity ae = as.getApplicationEntity();
         AttributeFilter filter = (AttributeFilter) ae.getProperty(AttributeFilter.class.getName());
         query.find(rq, pids(keys), keys, filter, QueryOption.toOptions(extNeg), matchUnknown);
         return query;
     }
 
     private Matches studyMatches(Association as, Attributes rq,
             Attributes keys) throws Exception {
         String cuid = rq.getString(Tag.AffectedSOPClassUID, null);
         ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
         StudyQuery query = (StudyQuery) JNDIUtils.lookup(StudyQuery.JNDI_NAME);
         ApplicationEntity ae = as.getApplicationEntity();
         AttributeFilter filter = (AttributeFilter) ae.getProperty(AttributeFilter.class.getName());
        query.find(rq, pids(keys), keys, filter, QueryOption.toOptions(extNeg), matchUnknown, roles());
         return query;
     }
 
     private Matches seriesMatches(Association as, Attributes rq,
             Attributes keys) throws Exception {
         String cuid = rq.getString(Tag.AffectedSOPClassUID, null);
         ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
         SeriesQuery query = (SeriesQuery) JNDIUtils.lookup(SeriesQuery.JNDI_NAME);
         ApplicationEntity ae = as.getApplicationEntity();
         AttributeFilter filter = (AttributeFilter) ae.getProperty(AttributeFilter.class.getName());
        query.find(rq, pids(keys), keys, filter, QueryOption.toOptions(extNeg), matchUnknown, roles());
         return query;
     }
 
     private Matches instanceMatches(Association as, Attributes rq,
             Attributes keys) throws Exception {
         String cuid = rq.getString(Tag.AffectedSOPClassUID, null);
         ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
         InstanceQuery query = (InstanceQuery) JNDIUtils.lookup(InstanceQuery.JNDI_NAME);
         ApplicationEntity ae = as.getApplicationEntity();
         AttributeFilter filter = (AttributeFilter) ae.getProperty(AttributeFilter.class.getName());
        query.find(rq, pids(keys), keys, filter, QueryOption.toOptions(extNeg), matchUnknown, roles());
         return query;
     }
 
    private String[] roles() {
        // TODO Auto-generated method stub
        return null;
    }

     private String[] pids(Attributes keys) {
         String pid = keys.getString(Tag.PatientID, null);
         return pid == null
                 ? null 
                 : new String[] { 
                     pid,
                     keys.getString(Tag.IssuerOfPatientID, null)};
     }
 
     private static void check(Attributes rq, AttributesValidator validator)
             throws DicomServiceException {
         if (validator.hasOffendingElements())
             throw new DicomServiceException(rq,
                     Status.IdentifierDoesNotMatchSOPClass, validator
                             .getErrorComment()).setOffendingElements(validator
                     .getOffendingElements());
     }
 }
