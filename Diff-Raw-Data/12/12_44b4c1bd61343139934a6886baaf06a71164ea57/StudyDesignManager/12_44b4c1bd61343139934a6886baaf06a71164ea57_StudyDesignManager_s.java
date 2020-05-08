 /*
  * Study Design Service for the GLIMMPSE Software System.  
  * This service stores study design definitions for users of the GLIMMSE interface.
  * Service contain all information related to a power or sample size calculation.  
  * The Study Design Service simplifies communication between different screens in the user interface.
  * 
  * Copyright (C) 2010 Regents of the University of Colorado.  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package edu.ucdenver.bios.studydesignsvc.manager;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.swing.text.StyledEditorKit.BoldAction;
 
 import org.hibernate.Query;
 
 import edu.ucdenver.bios.studydesignsvc.exceptions.StudyDesignException;
 import edu.ucdenver.bios.webservice.common.domain.BetaScale;
 import edu.ucdenver.bios.webservice.common.domain.BetweenParticipantFactor;
 import edu.ucdenver.bios.webservice.common.domain.BetweenParticipantFactorList;
 import edu.ucdenver.bios.webservice.common.domain.Blob2DArray;
 import edu.ucdenver.bios.webservice.common.domain.Category;
 import edu.ucdenver.bios.webservice.common.domain.ClusterNode;
 import edu.ucdenver.bios.webservice.common.domain.ConfidenceIntervalDescription;
 import edu.ucdenver.bios.webservice.common.domain.Covariance;
 import edu.ucdenver.bios.webservice.common.domain.CovarianceSet;
 import edu.ucdenver.bios.webservice.common.domain.Hypothesis;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisBetweenParticipantMapping;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisRepeatedMeasuresMapping;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisSet;
 import edu.ucdenver.bios.webservice.common.domain.NamedMatrix;
 import edu.ucdenver.bios.webservice.common.domain.NominalPower;
 import edu.ucdenver.bios.webservice.common.domain.PowerCurveDescription;
 import edu.ucdenver.bios.webservice.common.domain.PowerMethod;
 import edu.ucdenver.bios.webservice.common.domain.Quantile;
 import edu.ucdenver.bios.webservice.common.domain.RelativeGroupSize;
 import edu.ucdenver.bios.webservice.common.domain.RepeatedMeasuresNode;
 import edu.ucdenver.bios.webservice.common.domain.RepeatedMeasuresNodeList;
 import edu.ucdenver.bios.webservice.common.domain.ResponseNode;
 import edu.ucdenver.bios.webservice.common.domain.SampleSize;
 import edu.ucdenver.bios.webservice.common.domain.SigmaScale;
 import edu.ucdenver.bios.webservice.common.domain.Spacing;
 import edu.ucdenver.bios.webservice.common.domain.StandardDeviation;
 import edu.ucdenver.bios.webservice.common.domain.StatisticalTest;
 import edu.ucdenver.bios.webservice.common.domain.StudyDesign;
 import edu.ucdenver.bios.webservice.common.domain.TypeIError;
 import edu.ucdenver.bios.webservice.common.hibernate.BaseManagerException;
 import edu.ucdenver.bios.webservice.common.uuid.UUIDUtils;
 
 // TODO: Auto-generated Javadoc
 /**
  * Manager class which provides CRUD functionality for StudyDesign object.
  * 
  * @author Uttara Sakhadeo
  */
 public class StudyDesignManager extends StudyDesignParentManager {
 
     /**
      * Create a database manager class for study design objects.
      * 
      * @throws BaseManagerException
      *             the base manager exception
      */
     public StudyDesignManager() throws BaseManagerException {
         super();
     }
 
     /**
      * Check existance of a study design object by the specified UUID.
      * 
      * @param uuidBytes
      *            the uuid bytes
      * @return boolean
      * @throws StudyDesignException
      *             the study design exception
      */
     public boolean hasUUID(byte[] uuidBytes) throws StudyDesignException {
         if (!transactionStarted)
             throw new StudyDesignException("Transaction has not been started");
         try {
             // byte[] uuidBytes = UUIDUtils.asByteArray(uuid);
             StudyDesign studyDesign = (StudyDesign) session.get(
                     StudyDesign.class, uuidBytes);
             if (studyDesign != null)
                 return true;
             else
                 return false;
         } catch (Exception e) {
             throw new StudyDesignException(
                     "Failed to retrieve StudyDesign for UUID '"
                             + uuidBytes.toString() + "': " + e.getMessage());
         }
     }
 
     /**
      * Retrieve a study design object by the specified UUID.
      * 
      * @return data feed object
      * @throws StudyDesignException
      *             the study design exception
      */
     /*
      * public StudyDesign get(byte[] uuidBytes) throws StudyDesignException { if
      * (!transactionStarted) throw new
      * StudyDesignException("Transaction has not been started"); try { //byte[]
      * uuidBytes = UUIDUtils.asByteArray(uuid); StudyDesign studyDesign =
      * (StudyDesign) session.get(StudyDesign.class, uuidBytes); return
      * studyDesign; } catch (Exception e) { System.out.println(e.getMessage());
      * throw new
      * StudyDesignException("Failed to retrieve StudyDesign for UUID '" +
      * uuidBytes.toString() + "': " + e.getMessage()); } }
      */
 
     /*
      * Retrieve
      */
     /**
      * Gets the study uui ds.
      * 
      * @return the study uui ds
      * @throws StudyDesignException
      *             the study design exception
      */
     public List<byte[]> getStudyUUIDs() throws StudyDesignException {
         if (!transactionStarted)
             throw new StudyDesignException("Transaction has not been started.");
         try {
             Query query = session
                     .createQuery("select uuid from edu.ucdenver.bios.webservice.common.domain.StudyDesign");
             // Query query =
             // session.createQuery("select StudyUUID from tablestudydesign");
             @SuppressWarnings("unchecked")
             List<byte[]> results = query.list();
             return results;
         } catch (Exception e) {
             throw new StudyDesignException("Failed to retrieve uuids: "
                     + e.getMessage());
         }
     }
 
     /**
      * Gets the study uui ds.
      * 
      * @return the study uui ds
      * @throws StudyDesignException
      *             the study design exception
      */
     public List<StudyDesign> getStudyDesigns() throws StudyDesignException {
         if (!transactionStarted)
             throw new StudyDesignException("Transaction has not been started.");
         try {
             Query query = session
                     .createQuery("from edu.ucdenver.bios.webservice.common.domain.StudyDesign");
             // Query query =
             // session.createQuery("select StudyUUID from tablestudydesign");
             @SuppressWarnings("unchecked")
             List<StudyDesign> results = query.list();
             return results;
         } catch (Exception e) {
             throw new StudyDesignException("Failed to retrieve uuids: "
                     + e.getMessage());
         }
     }
 
     // /**
     // * Retrieve a study design representation by the specified UUID
     // *
     // * @param studyUUID:UUID
     // * @return study design object
     // */
     // public StudyDesign getStudyDesign(UUID studyUUID)
     // {
     // if (!transactionStarted)
     // throw new StudyDesignException("Transaction has not been started.");
     // StudyDesign studyDesign = null;
     // try
     // {
     // studyDesign =
     // (StudyDesign)session.get(StudyDesign.class,studyUUID.toString());
     // }
     // catch(Exception e)
     // {
     // throw new
     // StudyDesignException("Failed to retrieve study design for UUID '" +
     // studyUUID + "': " + e.getMessage());
     // }
     // return studyDesign;
     // }
 
     /**
      * Retrieve a study design representation by the specified UUID.
      * 
      * @param uuidBytes
      *            the uuid bytes
      * @return study design object
      * @throws StudyDesignException
      *             the study design exception
      */
     /*
      * public StudyDesign get(String studyUUID) throws StudyDesignException {
      * if(!transactionStarted) throw new
      * StudyDesignException("Transaction has not been started."); StudyDesign
      * studyDesign = null; try { Query q =
      * session.createQuery("select name StudyDesign"); List ls = q.list(); }
      * catch(Exception e) { System.out.println(e.getMessage()); throw new
      * StudyDesignException("Failed to retrieve study design for UUID '" +
      * studyUUID + "': " + e.getMessage()); } return studyDesign; }
      */
 
     /**
      * Delete a study design representation by the specified UUID
      * 
      * @param studyUUID
      *            :UUID
      * @return study design object
      */
     public StudyDesign delete(byte[] uuid) throws StudyDesignException {
         if (!transactionStarted)
             throw new StudyDesignException("Transaction has not been started.");
         StudyDesign studyDesign = null;        
         try {
             studyDesign = get(uuid);
             if (studyDesign != null) {
                 List<TypeIError> alphaList = studyDesign.getAlphaList();
 
                 List<BetaScale> betaScaleList = studyDesign.getBetaScaleList();
 
                 List<SigmaScale> sigmaScaleList = studyDesign
                         .getSigmaScaleList();
 
                 List<RelativeGroupSize> relativeGroupSizeList = studyDesign
                         .getRelativeGroupSizeList();
 
                 List<SampleSize> sampleSizeList = studyDesign
                         .getSampleSizeList();
 
                 List<StatisticalTest> statisticalTestList = studyDesign
                         .getStatisticalTestList();
 
                 List<PowerMethod> powerMethodList = studyDesign
                         .getPowerMethodList();
 
                 List<Quantile> quantileList = studyDesign.getQuantileList();
 
                 List<NominalPower> nominalPowerList = studyDesign
                         .getNominalPowerList();
 
                 List<ResponseNode> responseList = studyDesign.getResponseList();
 
                 ConfidenceIntervalDescription confidenceIntervalDescriptions = studyDesign
                         .getConfidenceIntervalDescriptions();
 
                 PowerCurveDescription powerCurveDescriptions = studyDesign
                         .getPowerCurveDescriptions();
 
                 List<BetweenParticipantFactor> betweenParticipantFactorList = studyDesign
                         .getBetweenParticipantFactorList();
 
                 List<RepeatedMeasuresNode> repeatedMeasuresTree = studyDesign
                         .getRepeatedMeasuresTree();
 
                 List<ClusterNode> clusteringTree = studyDesign
                         .getClusteringTree();
 
                 Set<Hypothesis> hypothesis = studyDesign.getHypothesis();
 
                 Set<Covariance> covariance = studyDesign.getCovariance();
 
                 Set<NamedMatrix> matrixSet = studyDesign.getMatrixSet();
 
                 Iterator itr;
                 /*
                  * Delete child objects
                  */
                 /*
                  * PowerCurveDescription
                  */
                 if (powerCurveDescriptions != null) {
                     session.delete(powerCurveDescriptions);
                 }
                 /*
                  * ConfidenceIntervalDescription
                  */
                 if (confidenceIntervalDescriptions != null) {
                     session.delete(confidenceIntervalDescriptions);
                 }
                 /*
                  * Lists
                  */
                 if (alphaList != null && !alphaList.isEmpty()) {
                     itr = alphaList.iterator();
                     while (itr.hasNext()) {
                         TypeIError typeIError = (TypeIError) itr.next();
                         if(typeIError != null) {                             
                             session.delete(typeIError);
                         }
                     }
                 }
                 if (betaScaleList != null && !betaScaleList.isEmpty()) {
                     itr = betaScaleList.iterator();
                     while (itr.hasNext()) {
                         BetaScale betaScale = (BetaScale) itr.next();
                         if(betaScale != null) {                            
                             session.delete(betaScale);
                         }
                     }
                 }
                 if (nominalPowerList != null && !nominalPowerList.isEmpty()) {
                     itr = nominalPowerList.iterator();
                     while (itr.hasNext()) {
                         NominalPower nominalPower = (NominalPower) itr.next();
                         if(nominalPower != null) {
                             session.delete(nominalPower);
                         }
                     }
                 }
                 if (powerMethodList != null && !powerMethodList.isEmpty()) {
                     itr = powerMethodList.iterator();
                     while (itr.hasNext()) {
                         PowerMethod powerMethod = (PowerMethod) itr.next();
                         if(powerMethod != null) {
                             session.delete(powerMethod);
                         }
                     }
                 }
                 if (quantileList != null && !quantileList.isEmpty()) {
                     itr = quantileList.iterator();
                     while (itr.hasNext()) {
                         Quantile quantile = (Quantile) itr.next();
                         if(quantile != null) {
                             session.delete(quantile);
                         }
                     }
                 }
                 if (relativeGroupSizeList != null
                         && !relativeGroupSizeList.isEmpty()) {
                     itr = relativeGroupSizeList.iterator();
                     while (itr.hasNext()) {
                         RelativeGroupSize relativeGroupSize = (RelativeGroupSize) itr.next();
                         if(relativeGroupSize != null) {
                             session.save(relativeGroupSize);
                         }
                     }
                 }
                 if (sampleSizeList != null && !sampleSizeList.isEmpty()) {
                     itr = sampleSizeList.iterator();
                     while (itr.hasNext()) {
                         SampleSize sampleSize = (SampleSize) itr.next();
                         if(sampleSize != null) {
                             session.delete(sampleSize);
                         }
                     }
                 }
                 if (responseList != null && !responseList.isEmpty()) {
                     itr = responseList.iterator();
                     while (itr.hasNext()) {
                         ResponseNode responseNode = (ResponseNode) itr.next();
                         if(responseNode != null) {
                             session.delete(responseNode);
                         }
                     }
                 }
                 if (sigmaScaleList != null && !sigmaScaleList.isEmpty()) {
                     itr = sigmaScaleList.iterator();
                     while (itr.hasNext()) {
                         SigmaScale sigmaScale = (SigmaScale) itr.next();
                         if(sigmaScale != null) {
                             session.save(sigmaScale);
                         }
                     }
                 }
                 if (statisticalTestList != null
                         && !statisticalTestList.isEmpty()) {
                     itr = statisticalTestList.iterator();
                     while (itr.hasNext()) {
                         StatisticalTest statisticalTest = (StatisticalTest) itr.next();
                         if(statisticalTest != null) {
                            session.delete(itr.next());
                         }
                     }
                 }
                 /*
                  * Clustering
                  */
                 if (clusteringTree != null && !clusteringTree.isEmpty()) {
                     itr = clusteringTree.iterator();
                     while (itr.hasNext()) {
                         ClusterNode clusterNode = (ClusterNode) itr.next();
                         if(clusterNode != null) {
                            session.delete(itr.next());
                         }
                     }
                 }
                 /*
                  * Matrix
                  */
                 if (matrixSet != null && !matrixSet.isEmpty()) {
                     itr = matrixSet.iterator();
                     while (itr.hasNext()) {
                         NamedMatrix matrix = (NamedMatrix) itr.next();
                         if(matrix != null) {
                            session.delete(itr.next());
                         }
                     }
                 }
                 /*
                  * Covariance
                  */
                 if (covariance != null && !covariance.isEmpty()) {
                     itr = covariance.iterator();
                     while (itr.hasNext()) {
                         Covariance cov = (Covariance) itr.next();
                         if(cov != null) {
                              /* 
                               * Save changes to Standard Deviation List
                               */
                              
                             List<StandardDeviation> std = cov
                                     .getStandardDeviationList();
                             if (std != null && !std.isEmpty()) {
                                 Iterator itrStd = std.iterator();
                                 while (itrStd.hasNext()) {
                                     session.delete(itrStd.next());
                                 }
                             }
                             session.delete(cov);
                         }
                     }
                     /*CovarianceSetManager covarianceManager = new CovarianceSetManager();
                     covarianceManager.delete(uuid);*/
                 }
                 /*
                  * BetweenParticipantFactors
                  */
                 if (betweenParticipantFactorList != null
                         && !betweenParticipantFactorList.isEmpty()) {
                     itr = betweenParticipantFactorList.iterator();
                     while (itr.hasNext()) {
                         BetweenParticipantFactor factor = (BetweenParticipantFactor) itr
                                 .next();
                         if(factor != null) {
                              /*
                               * Save changes to Category List
                               */
                              
                             List<Category> categoryList = factor.getCategoryList();
                             if (categoryList != null && !categoryList.isEmpty()) {
                                 Iterator itrCategory = categoryList.iterator();
                                 while (itrCategory.hasNext()) {
                                     session.delete(itrCategory.next());
                                 }
                             }
                             session.delete(factor);
                         }
                     }
                     /*BetweenParticipantFactorManager betManager = new BetweenParticipantFactorManager();
                     betManager.delete(uuid);*/
                 }
                 /*
                  * RepeatedMeasures
                  */
                 if (repeatedMeasuresTree != null
                         && !repeatedMeasuresTree.isEmpty()) {
                     itr = repeatedMeasuresTree.iterator();
                     while (itr.hasNext()) {
                         RepeatedMeasuresNode node = (RepeatedMeasuresNode) itr
                                 .next();
                         if(node != null) {
                              /*
                               * Save changes to Spacing List
                               */
                              
                             List<Spacing> spacing = node.getSpacingList();
                             if (spacing != null && !spacing.isEmpty()) {
                                 Iterator itrSpacing = spacing.iterator();
                                 while (itrSpacing.hasNext()) {
                                     session.delete(itrSpacing.next());
                                 }
                             }
                             session.delete(node);
                         }
                     }
                     /*RepeatedMeasuresManager reptManager = new RepeatedMeasuresManager();
                     reptManager.delete(uuid);*/
                 }
                 /*
                  * Hypothesis
                  */
                 if (hypothesis != null && !hypothesis.isEmpty()) {                    
                     itr = hypothesis.iterator();
                     while(itr.hasNext()) { 
                         Hypothesis hyp = (Hypothesis)itr.next();
                         if(hyp != null) {
                              /*
                               * Save changes to HypothesisBetweenParticipantMapping List
                               */
                              
                             List<HypothesisBetweenParticipantMapping> betMap = hyp.getBetweenParticipantFactorMapList();
                             if(betMap != null && !betMap.isEmpty()) {
                                 Iterator itrBetMap = betMap.iterator();
                                 while(itrBetMap.hasNext()) {
                                     HypothesisBetweenParticipantMapping map = (HypothesisBetweenParticipantMapping) itrBetMap.next();
                                     if(map != null){
                                         session.delete(map);
                                     }
                                 }
                             }
                             
                              /*
                               * Save changes to HypothesisRepeatedMeasuresMapping List
                               */
                              
                             List<HypothesisRepeatedMeasuresMapping> reptMap = hyp.getRepeatedMeasuresMapTree();
                             if(reptMap != null && !reptMap.isEmpty()) {
                                 Iterator itrReptMap = reptMap.iterator();
                                 while(itrReptMap.hasNext()) {
                                     HypothesisRepeatedMeasuresMapping map = (HypothesisRepeatedMeasuresMapping) itrReptMap.next();
                                     if(map != null){
                                         session.delete(map);
                                     }
                                 }
                             }
                             session.delete(hyp);
                         }
                     }
                     /*HypothesisSetManager hypothesisSetManager = new HypothesisSetManager();
                     hypothesisSetManager.delete(uuid);*/
                 }
                 /*
                  * Delete Study Design.
                  */
                 session.delete(studyDesign);
 
             } else
                 throw new StudyDesignException("Error ! No such UUID present");
         } catch (Exception e) {
             // throw new
             // StudyDesignException("Failed to delete study design for UUID '" +
             // studyUUID + "': " + e.getMessage());
             System.out.println(e.getMessage());
             throw new StudyDesignException(
                     "Failed to delete study design for UUID '" + uuid
                             + "': " + e.getMessage());
         }
         return studyDesign;
     }
 
     private StudyDesign delete(StudyDesign studyDesign)
             throws StudyDesignException {
         if (!transactionStarted)
             throw new StudyDesignException("Transaction has not been started.");
         if (studyDesign == null)
             throw new StudyDesignException("Error ! No such UUID present");
         try {
             delete(studyDesign.getUuid());
         } catch (Exception e) {
             // throw new
             // StudyDesignException("Failed to delete study design for UUID '" +
             // studyUUID + "': " + e.getMessage());
             System.out.println(e.getMessage());
             throw new StudyDesignException(
                     "Failed to delete study design for UUID '"
                             + studyDesign.getUuid() + "': " + e.getMessage());
         }
         return studyDesign;
     }
     
     public StudyDesign create() throws StudyDesignException
     {
         if (!transactionStarted)
             throw new StudyDesignException("Transaction has not been started.");
         /*
          * Geneate a new UUID
          */
         byte[] uuid = UUIDUtils.asByteArray(UUID.randomUUID());
         StudyDesign studyDesign = null;
         /*
          * Check existance of this UUID in the database
          */
         while(uuid != null && get(uuid) != null)
         {
             uuid = UUIDUtils.asByteArray(UUID.randomUUID()); 
         }
         
             /*
              * Create a StudyDesign with this unique UUID
              */
             studyDesign = new StudyDesign(uuid);               
             try {
                 /*
                  * Persist the created StudyDesign
                  */     
                studyDesign = saveOrUpdate(studyDesign, true);
             } catch (Exception e) {
                 System.out.println(e.getMessage());
                 throw new StudyDesignException("Failed to save study design : "
                         + e.getMessage() + "\n" + e.getStackTrace());
             }
         
         return studyDesign;
     }
 
     /**
      * Create or update a study design object in the database.
      * 
      * @param studyDesign
      *            the study design
      * @param isCreation
      *            the is creation
      * @return study design object
      * @throws StudyDesignException
      *             the study design exception
      */
     public StudyDesign saveOrUpdate(StudyDesign studyDesign, boolean isCreation)
             throws StudyDesignException {
         if (!transactionStarted)
             throw new StudyDesignException("Transaction has not been started.");
         byte[] uuid = studyDesign.getUuid();
         
         List<TypeIError> alphaList = studyDesign.getAlphaList();
 
         List<BetaScale> betaScaleList = studyDesign.getBetaScaleList();
 
         List<SigmaScale> sigmaScaleList = studyDesign.getSigmaScaleList();
 
         List<RelativeGroupSize> relativeGroupSizeList = studyDesign.getRelativeGroupSizeList();
 
         List<SampleSize> sampleSizeList = studyDesign.getSampleSizeList();
 
         List<StatisticalTest> statisticalTestList = studyDesign.getStatisticalTestList();
 
         List<PowerMethod> powerMethodList = studyDesign.getPowerMethodList();
 
         List<Quantile> quantileList = studyDesign.getQuantileList();
 
         List<NominalPower> nominalPowerList = studyDesign.getNominalPowerList();
 
         List<ResponseNode> responseList = studyDesign.getResponseList();
 
         ConfidenceIntervalDescription confidenceIntervalDescriptions = studyDesign.getConfidenceIntervalDescriptions();
 
         PowerCurveDescription powerCurveDescriptions = studyDesign.getPowerCurveDescriptions();
 
         List<BetweenParticipantFactor> betweenParticipantFactorList = studyDesign.getBetweenParticipantFactorList();
 
         List<RepeatedMeasuresNode> repeatedMeasuresTree = studyDesign.getRepeatedMeasuresTree();
 
         List<ClusterNode> clusteringTree = studyDesign.getClusteringTree();
 
         Set<Hypothesis> hypothesis = studyDesign.getHypothesis();
 
         Set<Covariance> covariance = studyDesign.getCovariance();
 
         Set<NamedMatrix> matrixSet = studyDesign.getMatrixSet();
         
         Iterator itr = null;
         
         try {
             /*
              * Retrieve Study Design Object
              */
             StudyDesign originalStudyDesign = get(uuid);            
             if (originalStudyDesign != null) {
                 /*
                  * Delete Existing Study Design Object
                  */
                 delete(originalStudyDesign);
             }
             /*
              * Save/Update child objects
              */
                 /*
                  * PowerCurveDescription
                  */
                 if(powerCurveDescriptions != null) {                        
                     session.save(powerCurveDescriptions);
                 }
                 /*
                  * ConfidenceIntervalDescription
                  */
                 if(confidenceIntervalDescriptions != null) {
                     session.save(confidenceIntervalDescriptions);
                 }
                 /*
                  * Lists
                  */     
                     if(alphaList != null && !alphaList.isEmpty()) {
                         itr = alphaList.iterator();
                         while(itr.hasNext()) {   
                             TypeIError typeIError = (TypeIError) itr.next();
                             if(typeIError != null) {
                                 session.save(typeIError);
                             }
                         }
                     }
                     if(betaScaleList != null && !betaScaleList.isEmpty()) {
                         itr = betaScaleList.iterator();
                         while(itr.hasNext()) {    
                             BetaScale betaScale = (BetaScale) itr.next();
                             if(betaScale != null) {
                                 session.save(betaScale);
                             }
                         }
                     }
                     if(nominalPowerList != null && !nominalPowerList.isEmpty()) {
                         itr = nominalPowerList.iterator();
                         while(itr.hasNext()) {  
                             NominalPower nominalPower = (NominalPower) itr.next();
                             if(nominalPower != null) {
                                 session.save(nominalPower);
                             }
                         }
                     }
                     if(powerMethodList != null && !powerMethodList.isEmpty()) {
                         itr =powerMethodList.iterator();
                         while(itr.hasNext()) {       
                             PowerMethod powerMethod = (PowerMethod) itr.next();
                             if(powerMethod != null) {
                                 session.save(powerMethod);
                             }
                         }
                     }
                     if(quantileList != null && !quantileList.isEmpty()) {
                         itr = quantileList.iterator();
                         while(itr.hasNext()) {        
                             Quantile quantile = (Quantile) itr.next();
                             if(quantile != null) {
                                 session.save(quantile);
                             }
                         }
                     }
                     if(relativeGroupSizeList != null && !relativeGroupSizeList.isEmpty()) {
                         itr = relativeGroupSizeList.iterator();
                         while(itr.hasNext()) {
                             RelativeGroupSize relativeGroupeSize = (RelativeGroupSize) itr.next();
                             if(relativeGroupeSize != null) {
                                 session.save(relativeGroupeSize);
                             }
                         }
                     }
                     if(sampleSizeList != null && !sampleSizeList.isEmpty()) {
                         itr = sampleSizeList.iterator();
                         while(itr.hasNext()) { 
                             SampleSize sampleSize = (SampleSize) itr.next();
                             if(sampleSize != null) {
                                 session.save(sampleSize);
                             }
                         }
                     }
                     if(responseList != null && !responseList.isEmpty()) {
                         itr = responseList.iterator();
                         while(itr.hasNext()) { 
                             ResponseNode response = (ResponseNode) itr.next();
                             if(response != null) {
                                 session.save(response);
                             }
                         }
                     }
                     if(sigmaScaleList != null && !sigmaScaleList.isEmpty()) {
                         itr = sigmaScaleList.iterator();
                         SigmaScale sigmaScale = (SigmaScale) itr.next();
                         while(itr.hasNext()) {
                             if(sigmaScale != null) {
                                 session.save(sigmaScale);
                             }
                         }
                     }
                     if(statisticalTestList != null && !statisticalTestList.isEmpty()) {
                         itr = statisticalTestList.iterator();
                         while(itr.hasNext()) {  
                             StatisticalTest statisticalTest = (StatisticalTest) itr.next();
                             if(statisticalTest != null) {
                                 session.save(statisticalTest);
                             }
                         }
                     }
                 /*
                  * Matrix
                  */
                     if(matrixSet != null && !matrixSet.isEmpty()) {
                         itr = matrixSet.iterator();
                         while(itr.hasNext()) {    
                             NamedMatrix matrix = (NamedMatrix) itr.next();
                             if(matrix != null) {
                                 session.save(matrix);
                             }
                         }
                     }
                 /*
                  * Clustering
                  */
                     if(clusteringTree != null && !clusteringTree.isEmpty()) {
                         itr = clusteringTree.iterator();
                         while(itr.hasNext()) {
                             ClusterNode clusterNode = (ClusterNode) itr.next();
                             if(clusterNode != null) {
                                 session.save(clusterNode);
                             }
                         }
                     }
                  /*
                   * Covariance
                   */
                     if(covariance != null && !covariance.isEmpty()) {
                         itr = covariance.iterator();
                         while(itr.hasNext()) {
                             Covariance cov = (Covariance) itr.next();
                             if(cov != null) {
                                 /*
                                  * Save changes to Standard Deviation List
                                  */
                                 List<StandardDeviation> std= cov.getStandardDeviationList();
                                 if(std != null && !std.isEmpty()) {
                                     Iterator itrStd = std.iterator();
                                     while(itrStd.hasNext() && itrStd != null) {
                                         StandardDeviation standardDeviation = (StandardDeviation) itrStd.next();
                                         if(standardDeviation != null) {                                        
                                             session.save(standardDeviation);
                                         }
                                     }
                                 }
                                 session.save(cov);
                             }
                         }
                     /*CovarianceSetManager covarianceManager = new CovarianceSetManager();
                     covarianceManager.saveOrUpdate(new CovarianceSet(uuid,covariance), true);*/
                     }                
                 /*
                  * BetweenParticipantFactors
                  */
                     if(betweenParticipantFactorList != null && !betweenParticipantFactorList.isEmpty()) {
                         itr = betweenParticipantFactorList.iterator();                        
                         while(itr.hasNext()) {                        
                             BetweenParticipantFactor factor = (BetweenParticipantFactor)itr.next();
                             if(factor != null) {
                                 /*
                                  * Save changes to Category List
                                  */
                                 List<Category> categoryList = factor.getCategoryList();
                                 if(categoryList != null && !categoryList.isEmpty()) {
                                     Iterator itrCategory = categoryList.iterator();
                                     while(itrCategory.hasNext()) {                    
                                         session.save(itrCategory.next());
                                     }
                                 }
                                 session.save(factor);
                             }
                         }
                         /*BetweenParticipantFactorManager betManager = new BetweenParticipantFactorManager();
                         betManager.saveOrUpdate(new BetweenParticipantFactorList(uuid, betweenParticipantFactorList), true);*/
                     }
                 /*
                  * RepeatedMeasures
                  */
                     if(repeatedMeasuresTree != null && !repeatedMeasuresTree.isEmpty()) {
                         itr = repeatedMeasuresTree.iterator();
                         while(itr.hasNext()) {    
                             RepeatedMeasuresNode node = (RepeatedMeasuresNode) itr.next();
                             if(node != null) {
                                 /*
                                  * Save changes to Spacing List
                                  */
                                 List<Spacing> spacing = node.getSpacingList(); 
                                 if(spacing != null && !spacing.isEmpty()) {
                                     Iterator itrSpacing = spacing.iterator();
                                     while(itrSpacing.hasNext()) {
                                         session.save(itrSpacing.next());
                                     }
                                 }
                                 session.save(node);
                             }
                         }
                         /*RepeatedMeasuresManager reptManager = new RepeatedMeasuresManager();
                         reptManager.saveOrUpdate(new RepeatedMeasuresNodeList(uuid, repeatedMeasuresTree), true);*/
                     }
                 /*
                  * Hypothesis
                  */
                     if(hypothesis != null && !hypothesis.isEmpty()) {
                         
                         /*HypothesisSetManager hypothesisSetManager = new HypothesisSetManager();
                         hypothesisSetManager.saveOrUpdate(new HypothesisSet(uuid,hypothesis),true);*/
                         itr = hypothesis.iterator();
                         while(itr.hasNext()) { 
                             Hypothesis hyp = (Hypothesis)itr.next();
                             if(hyp != null) {
                                 /*
                                  * Save changes to HypothesisBetweenParticipantMapping List
                                  */
                                 List<HypothesisBetweenParticipantMapping> betMap = hyp.getBetweenParticipantFactorMapList();
                                 if(betMap != null && !betMap.isEmpty()) {
                                     Iterator itrBetMap = betMap.iterator();
                                     HypothesisBetweenParticipantMapping map = (HypothesisBetweenParticipantMapping) itrBetMap.next();
                                     while(itrBetMap.hasNext()) {
                                         if(map != null){
                                             session.save(map);
                                         }
                                     }
                                 }
                                 /*
                                  * Save changes to HypothesisRepeatedMeasuresMapping List
                                  */
                                 List<HypothesisRepeatedMeasuresMapping> reptMap = hyp.getRepeatedMeasuresMapTree();
                                 if(reptMap != null && !reptMap.isEmpty()) {
                                     Iterator itrReptMap = reptMap.iterator();
                                     while(itrReptMap.hasNext()) {
                                         HypothesisRepeatedMeasuresMapping map = (HypothesisRepeatedMeasuresMapping) itrReptMap.next();
                                         if(map != null){
                                             session.save(map);
                                         }
                                     }
                                 }
                                 session.save(hyp);
                             }
                         }
                     }
             /*
              * Save/Update Study Design.
              */
             session.save(studyDesign);            
             
         } catch (Exception e) {
             System.out.println(e.getMessage());
             throw new StudyDesignException("Failed to save study design : "
                     + e.getMessage() + "\n" + e.getStackTrace());
         }
         return studyDesign;
     }    
 
 }
