 package gov.nih.nci.eagle.service.handlers;
 
 import gov.nih.nci.caintegrator.domain.study.bean.StudyParticipant;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.caintegrator.studyQueryService.QueryHandler;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.BehavioralCriterion;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.EPIQueryDTO;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.EducationLevel;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.EnvironmentalTobaccoSmokeCriterion;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.FamilyHistoryCriterion;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.Gender;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.MaritalStatus;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.PatientCharacteristicsCriterion;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.Relative;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.Religion;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.SmokingExposure;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.SmokingStatus;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.TobaccoConsumptionCriterion;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Conjunction;
 import org.hibernate.criterion.CriteriaSpecification;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 public class EpidemiologicalQueryHandler implements QueryHandler {
 
     private SessionFactory sessionFactory;
 
     // private static final String TARGET_FINDING_ALIAS = " finding";
 
     public Integer getResultCount(QueryDTO query) {
         throw new UnsupportedOperationException();
     }
 
     public List getResults(QueryDTO dto, Integer page) {
         throw new UnsupportedOperationException();
     }
 
     public List getResults(QueryDTO queryDTO) {
 
         EPIQueryDTO epiQueryDTO = (EPIQueryDTO) queryDTO;
         Session session = sessionFactory.getCurrentSession();
         Criteria targetCrit = session.createCriteria(StudyParticipant.class);
         targetCrit.createAlias("epidemiologicalFinding", "finding");
         targetCrit.createAlias("finding.tobaccoConsumptionCollection", "tc",
                 CriteriaSpecification.LEFT_JOIN);
         targetCrit.createAlias("finding.behavioralAssessment", "ba");
         targetCrit.createAlias("finding.lifestyle", "ls");
        targetCrit.createAlias("finding.relativeCollection", "relatives", CriteriaSpecification.LEFT_JOIN);
         targetCrit.createAlias("finding.environmentalFactorCollection", "factors", CriteriaSpecification.LEFT_JOIN);
 
         /* 1. Handle PatientCharacteristics Criterion */
         PatientCharacteristicsCriterion patCharacterCrit = epiQueryDTO
                 .getPatientCharacteristicsCriterion();
         if (patCharacterCrit != null)
             populatePatientCharacteristicsCriterion(patCharacterCrit,
                     targetCrit);
 
         /* 2. Handle Tobacco Dependency Criterion */
         BehavioralCriterion behaviorCrit = epiQueryDTO.getBehavioralCriterion();
         if (behaviorCrit != null)
             populateBehaviorCriterion(behaviorCrit, targetCrit);
 
         /* Handle Tobacco Consumption Criterion */
         TobaccoConsumptionCriterion tobaccoCrit = epiQueryDTO
                 .getTobaccoConsumptionCriterion();
         if (tobaccoCrit != null)
             populateTobaccoConsumptionCrit(tobaccoCrit, targetCrit);
 
         FamilyHistoryCriterion familyHistcrit = epiQueryDTO
                 .getFamilyHistoryCriterion();
         if (familyHistcrit != null)
             populateFamilyHistoryCrit(familyHistcrit, targetCrit);
 
         EnvironmentalTobaccoSmokeCriterion envCrit = epiQueryDTO
                 .getEnvironmentalTobaccoSmokeCriterion();
         if (envCrit != null && envCrit
                 .getSmokingExposureCollection() != null) {
             Collection<SmokingExposure> exposure = envCrit
                     .getSmokingExposureCollection();
             List<String> exposures = new ArrayList<String>();
             for (SmokingExposure ex : exposure) {
                 exposures.add(ex.toString());
             }
             targetCrit.add(Restrictions.in("factors.exposureType", exposures));
 
         }
 
         // Handle patient ID criteria
         if (epiQueryDTO.getPatientIds() != null
                 && epiQueryDTO.getPatientIds().size() > 0) {
             targetCrit.add(Restrictions.in("studySubjectIdentifier",
                     epiQueryDTO.getPatientIds()));
         }
         targetCrit.addOrder(Order.asc("id"));
         List<StudyParticipant> l = targetCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 
         return l;
     }
 
     private void populateFamilyHistoryCrit(
             FamilyHistoryCriterion familyHistcrit, Criteria targetCrit) {
         Integer lungCancerrelativeCrit = familyHistcrit
                 .getFamilyLungCancer();
         if (lungCancerrelativeCrit != null) {
 
             targetCrit.add(Restrictions.eq("finding.relativeWithLungCancer",
                     lungCancerrelativeCrit.toString()));
         }
         Collection<Relative> smokingRelativeCrit = familyHistcrit
                 .getSmokingRelativeCollection();
         if (smokingRelativeCrit != null) {
             Disjunction dis = Restrictions.disjunction();
             for(Relative r : smokingRelativeCrit) {
                 Conjunction con = Restrictions.conjunction();
                 con.add(Restrictions.eq("relatives.relationshipType", r.getName().toUpperCase()));
                 con.add(Restrictions.eq("relatives.smokingStatus", "1"));
                 dis.add(con);
             }
             targetCrit.add(dis);
         }
     }
 
     private void populateTobaccoConsumptionCrit(
             TobaccoConsumptionCriterion tobaccoCrit, Criteria targetCrit) {
 
         if (tobaccoCrit != null) {
             Double lowerIntensity = tobaccoCrit.getIntensityLower();
             Double upperIntensity = tobaccoCrit.getIntensityUpper();
             if (lowerIntensity != null && upperIntensity != null) {
                 assert (upperIntensity.compareTo(lowerIntensity) > 0);
                 targetCrit.add(Restrictions.between("tc.intensity",
                         lowerIntensity, upperIntensity));
             }
             Integer durationLower = tobaccoCrit.getDurationLower();
             Integer durationUpper = tobaccoCrit.getDurationUpper();
             if (durationLower != null && durationUpper != null) {
                 assert (durationUpper.compareTo(durationUpper) > 0);
                 targetCrit.add(Restrictions.between("tc.duration",
                         durationLower, durationUpper));
             }
             Integer ageLower = tobaccoCrit.getAgeAtInitiationLower();
             Integer ageUpper = tobaccoCrit.getAgeAtInitiationUpper();
             if (ageLower != null && ageUpper != null) {
                 assert (ageUpper.compareTo(ageLower) > 0);
                 targetCrit.add(Restrictions.between("tc.ageAtInitiation",
                         ageLower, ageUpper));
             }
 
             SmokingStatus smokeStatus = tobaccoCrit.getSmokingStatus();
             if (smokeStatus != null)
                 targetCrit.add(Expression.eq("tc.smokingStatus", new Integer(
                         smokeStatus.getValue()).toString()));
 
         }
     }
 
     private void populateBehaviorCriterion(BehavioralCriterion behaviorCrit,
             Criteria targetCrit) {
 
         Integer fScore = behaviorCrit.getFagerstromScore();
         if (fScore != null)
             targetCrit.add(Expression.eq("ba.fagerstromScore", fScore));
 
         Integer dScore = behaviorCrit.getDepressionScore();
         if (dScore != null)
             targetCrit.add(Expression.eq("ba.depressionScore", dScore));
 
         Integer aScore = behaviorCrit.getAnxietyScore();
         if (aScore != null)
             targetCrit.add(Expression.eq("ba.anxietyScore", aScore));
     }
 
     private void populatePatientCharacteristicsCriterion(
             PatientCharacteristicsCriterion patCharacterCrit,
             Criteria targetCrit) {
         assert (patCharacterCrit != null);
         pupulatePatientAttributesCriterion(patCharacterCrit, targetCrit);
         populateLifeStyleCriterion(targetCrit, patCharacterCrit);
     }
 
     private void pupulatePatientAttributesCriterion(
             PatientCharacteristicsCriterion patCharacterCrit,
             Criteria targetCrit) {
         Double lowerAgeLimit = patCharacterCrit.getAgeLowerLimit();
         Double upperAgeLimit = patCharacterCrit.getAgeUpperLimit();
         if ((lowerAgeLimit != null && lowerAgeLimit != 0)
                 && (upperAgeLimit != null && upperAgeLimit != 0))
             targetCrit.add(Restrictions.between("ageAtDiagnosis.absoluteValue",
                     lowerAgeLimit, upperAgeLimit));
 
         Gender gender = patCharacterCrit.getSelfReportedgender();
         if (gender != null)
             targetCrit.add(Restrictions.eq("administrativeGenderCode", gender
                     .getName().toUpperCase()));
 
         Double lowerWtLimit = patCharacterCrit.getWeightLowerLimit();
         Double upperWtLimit = patCharacterCrit.getWeightUpperLimit();
         if ((lowerWtLimit != null && lowerWtLimit != 0)
                 && (upperWtLimit != null && upperWtLimit != 0))
             targetCrit.add(Restrictions.between("weight", lowerWtLimit,
                     upperWtLimit));
 
         Double lowerHtLimit = patCharacterCrit.getHeightLowerLimit();
         Double upperHtLimit = patCharacterCrit.getHeightUpperLimit();
         if ((lowerHtLimit != null && lowerHtLimit != 0)
                 && (upperHtLimit != null && upperHtLimit != 0))
             targetCrit.add(Restrictions.between("height", lowerHtLimit,
                     upperHtLimit));
 
         Double lowerWaistLimit = patCharacterCrit.getWaistLowerLimit();
         Double upperWaistLimit = patCharacterCrit.getWaisUpperLimit();
         if ((lowerWaistLimit != null && lowerWaistLimit != 0)
                 && (upperWaistLimit != null && upperWaistLimit != 0))
             targetCrit.add(Restrictions.between("waistCircumference",
                     lowerWaistLimit, upperWaistLimit));
     }
 
     private void populateLifeStyleCriterion(Criteria targetCrit,
             PatientCharacteristicsCriterion patCharacterCrit) {
 
         MaritalStatus mStatus = patCharacterCrit.getMaritalStatus();
         if (mStatus != null)
             targetCrit.add(Expression.eq("ls.maritalStatus", new Integer(
                     mStatus.getValue()).toString()));
 
         Religion religion = patCharacterCrit.getReligion();
         if (religion != null)
             targetCrit.add(Expression.eq("ls.religion", new Integer(religion
                     .getValue()).toString()));
 
         String ra = patCharacterCrit.getResidentialArea();
         if (ra != null)
             targetCrit.add(Expression.eq("ls.residentialArea", ra));
 
         EducationLevel el = patCharacterCrit.getEducationLevel();
         if (el != null)
             targetCrit.add(Expression.eq("ls.educationLevel", new Integer(el
                     .getValue()).toString()));
     }
 
     public boolean canHandle(QueryDTO query) {
         return (query instanceof EPIQueryDTO);
     }
 
     public SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     public void setSessionFactory(SessionFactory sessionFacotry) {
         this.sessionFactory = sessionFacotry;
     }
 }
