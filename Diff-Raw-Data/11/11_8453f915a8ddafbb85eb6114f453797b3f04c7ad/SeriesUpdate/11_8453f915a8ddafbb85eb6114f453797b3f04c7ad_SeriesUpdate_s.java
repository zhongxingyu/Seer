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
  * Portions created by the Initial Developer are Copyright (C) 2012
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
 
 package org.dcm4chee.archive.ejb.store;
 
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.dcm4che.util.StringUtils;
 import org.dcm4chee.archive.persistence.Availability;
 import org.dcm4chee.archive.persistence.Code;
 import org.dcm4chee.archive.persistence.QCode;
 import org.dcm4chee.archive.persistence.QInstance;
 import org.dcm4chee.archive.persistence.Series;
 import org.dcm4chee.archive.persistence.Study;
 import org.hibernate.Session;
 
 import com.mysema.query.BooleanBuilder;
 import com.mysema.query.jpa.hibernate.HibernateQuery;
 
 /**
  * @author Gunter Zeilinger <gunterze@gmail.com>
  */
 public class SeriesUpdate {
 
     public static void updateSeries(EntityManager em, Series series,
             List<Code> hideConceptNameCodes, List<Code> hideRejectionCodes) {
         if (series == null)
             return;
 
         series.setNumberOfSeriesRelatedInstances(
                 countRelatedInstancesOf(em, series, hideConceptNameCodes, hideRejectionCodes));
         series.setRetrieveAETs(retrieveAETsOf(em, series));
         series.setExternalRetrieveAET(externalRetrieveAETOf(em, series));
         series.setAvailability(availabilityOf(em, series));
         series.setDirty(false);
 
         Study study = series.getStudy();
         study.setModalitiesInStudy(modalitiesOf(em, study));
         study.setSOPClassesInStudy(sopClassesOf(em, study));
         study.setNumberOfStudyRelatedSeries(countRelatedSeriesOf(em, study));
         study.setNumberOfStudyRelatedInstances(countRelatedInstancesOf(em, study));
         study.setRetrieveAETs(retrieveAETsOf(em, study));
         study.setExternalRetrieveAET(externalRetrieveAETOf(em, study));
         study.setAvailability(availabilityOf(em, study));
     }
 
     private static int countRelatedInstancesOf(EntityManager em, Series series,
             List<Code> hideConceptNameCodes, List<Code> hideRejectionCodes) {
         BooleanBuilder builder = new BooleanBuilder();
         builder.and(QInstance.instance.series.eq(series));
         builder.and(QInstance.instance.replaced.isFalse());
         andNotIn(builder, QInstance.instance.conceptNameCode, hideConceptNameCodes);
         andNotIn(builder, QInstance.instance.rejectionCode, hideRejectionCodes);
         Session session = (Session) em.getDelegate();
         return (int) new HibernateQuery(session)
             .from(QInstance.instance)
             .where(builder)
             .count();
     }
 
     private static void andNotIn(BooleanBuilder builder, QCode code, List<Code> codes) {
         if (codes != null && !codes.isEmpty())
            builder.andNot(code.in(codes));
     }
 
     private static String[] retrieveAETsOf(EntityManager em, Series series) {
         return commonAETs(em.createNamedQuery(Series.RETRIEVE_AETS, String.class)
                 .setParameter(1, series)
                 .getResultList());
     }
 
     private static String externalRetrieveAETOf(EntityManager em, Series series) {
         return commonAET(em.createNamedQuery(Series.EXTERNAL_RETRIEVE_AET, String.class)
                 .setParameter(1, series)
                 .getResultList());
     }
 
     private static Availability availabilityOf(EntityManager em, Series series) {
         return em.createNamedQuery(Series.AVAILABILITY, Availability.class)
                 .setParameter(1, series)
                 .getSingleResult();
     }
 
     private static String[] modalitiesOf(EntityManager em, Study study) {
         List<String> resultList = 
                 em.createNamedQuery(Study.MODALITIES_IN_STUDY, String.class)
                     .setParameter(1, study)
                     .getResultList();
             resultList.remove(null);
             return resultList.toArray(StringUtils.EMPTY_STRING);
     }
 
     private static String[] sopClassesOf(EntityManager em, Study study) {
         return em.createNamedQuery(Study.SOP_CLASSES_IN_STUDY, String.class)
                 .setParameter(1, study)
                 .getResultList().toArray(StringUtils.EMPTY_STRING);
     }
 
     private static int countRelatedSeriesOf(EntityManager em, Study study) {
         return em.createNamedQuery(Study.COUNT_RELATED_SERIES, Long.class)
                 .setParameter(1, study)
                 .getSingleResult().intValue();
     }
 
     private static int countRelatedInstancesOf(EntityManager em, Study study) {
         return em.createNamedQuery(Study.COUNT_RELATED_INSTANCES, Long.class)
                 .setParameter(1, study)
                 .getSingleResult().intValue();
     }
 
     private static String[] retrieveAETsOf(EntityManager em, Study study) {
         return commonAETs(em.createNamedQuery(Study.RETRIEVE_AETS, String.class)
                 .setParameter(1, study)
                 .getResultList());
     }
 
     private static String externalRetrieveAETOf(EntityManager em, Study study) {
         return commonAET(em.createNamedQuery(Study.EXTERNAL_RETRIEVE_AET, String.class)
                 .setParameter(1, study)
                 .getResultList());
     }
 
     private static Availability availabilityOf(EntityManager em, Study study) {
         return em.createNamedQuery(Study.AVAILABILITY, Availability.class)
                 .setParameter(1, study)
                 .getSingleResult();
     }
 
     private static String[] commonAETs(List<String> resultList) {
         LinkedHashSet<String> set = null;
         for (String aets : resultList) {
             if (aets == null)
                 return StringUtils.EMPTY_STRING;
             List<String> aetList = Arrays.asList(StringUtils.split(aets, '\\'));
             if (set == null) {
                 set = new LinkedHashSet<String>(aetList);
             } else {
                 set.retainAll(aetList);
             }
             if (set.isEmpty())
                 return StringUtils.EMPTY_STRING;
         }
         return set.toArray(StringUtils.EMPTY_STRING);
     }
 
     private static String commonAET(List<String> resultList) {
         String common = null;
         for (String aet : resultList) {
             if (aet == null)
                 return null;
             if (common == null) {
                 common = aet;
             } else {
                 if (!common.equals(aet));
                     return null;
             }
         }
         return common;
     }
 
 }
