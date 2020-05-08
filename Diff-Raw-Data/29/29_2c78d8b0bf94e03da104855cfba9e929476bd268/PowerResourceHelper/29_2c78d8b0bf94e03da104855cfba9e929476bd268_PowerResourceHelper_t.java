 /*
  * Power Service for the GLIMMPSE Software System.  Processes
  * incoming HTTP requests for power, sample size, and detectable
  * difference
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
  * Foundation, Inc., 51 Franklin Street, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 package edu.ucdenver.bios.powersvc.resource;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.log4j.Logger;
 
 import edu.cudenver.bios.matrix.FixedRandomMatrix;
 import edu.cudenver.bios.matrix.MatrixUtils;
 import edu.cudenver.bios.power.GLMMPower;
 import edu.cudenver.bios.power.Power;
 import edu.cudenver.bios.power.glmm.GLMMPowerConfidenceInterval.ConfidenceIntervalType;
 import edu.cudenver.bios.power.glmm.GLMMTestFactory;
 import edu.cudenver.bios.power.parameters.GLMMPowerParameters;
 import edu.ucdenver.bios.powersvc.application.PowerConstants;
 import edu.ucdenver.bios.powersvc.application.PowerLogger;
 import edu.ucdenver.bios.webservice.common.domain.BetaScale;
 import edu.ucdenver.bios.webservice.common.domain.BetweenParticipantFactor;
 import edu.ucdenver.bios.webservice.common.domain.Blob2DArray;
 import edu.ucdenver.bios.webservice.common.domain.Category;
 import edu.ucdenver.bios.webservice.common.domain.ClusterNode;
 import edu.ucdenver.bios.webservice.common.domain.ConfidenceInterval;
 import edu.ucdenver.bios.webservice.common.domain.ConfidenceIntervalDescription;
 import edu.ucdenver.bios.webservice.common.domain.Covariance;
 import edu.ucdenver.bios.webservice.common.domain.Hypothesis;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisBetweenParticipantMapping;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisRepeatedMeasuresMapping;
 import edu.ucdenver.bios.webservice.common.domain.NamedMatrix;
 import edu.ucdenver.bios.webservice.common.domain.NamedMatrixList;
 import edu.ucdenver.bios.webservice.common.domain.NominalPower;
 import edu.ucdenver.bios.webservice.common.domain.PowerMethod;
 import edu.ucdenver.bios.webservice.common.domain.PowerResult;
 import edu.ucdenver.bios.webservice.common.domain.PowerResultList;
 import edu.ucdenver.bios.webservice.common.domain.Quantile;
 import edu.ucdenver.bios.webservice.common.domain.RepeatedMeasuresNode;
 import edu.ucdenver.bios.webservice.common.domain.SampleSize;
 import edu.ucdenver.bios.webservice.common.domain.SigmaScale;
 import edu.ucdenver.bios.webservice.common.domain.StatisticalTest;
 import edu.ucdenver.bios.webservice.common.domain.StudyDesign;
 import edu.ucdenver.bios.webservice.common.domain.TypeIError;
 import edu.ucdenver.bios.webservice.common.enums.PowerMethodEnum;
 import edu.ucdenver.bios.webservice.common.enums.StatisticalTestTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.StudyDesignViewTypeEnum;
 
 /**
  * Helper class for conversion to/from domain layer objects
  * @author Sarah Kreidler
  *
  */
 public final class PowerResourceHelper {
 
     /** The logger. */
     private static Logger logger = PowerLogger.getInstance();
 
     /**
      * Convert a study design object into a power parameters object
      * TODO: should be removed once modifications to java stats are complete
      * 
      * @param studyDesign
      * @return
      */
     public static GLMMPowerParameters studyDesignToPowerParameters(StudyDesign studyDesign)
     throws IllegalArgumentException {
         GLMMPowerParameters params = new GLMMPowerParameters();
 
         /** Build list inputs **/
         // add tests
         if (studyDesign.getStatisticalTestList() != null) {
             for(StatisticalTest test: studyDesign.getStatisticalTestList()) {
                 params.addTest(toGLMMTest(test));
             }
         }
         // add alpha values
         if (studyDesign.getAlphaList() != null) {
             for(TypeIError alpha: studyDesign.getAlphaList()) {
                 params.addAlpha(alpha.getAlphaValue());
             }
         }
         // add nominal powers
         if (studyDesign.getNominalPowerList() != null) {
             for(NominalPower power: studyDesign.getNominalPowerList()) {
                 params.addPower(power.getValue());
             }
         }
         // add per group sample sizes
         if (studyDesign.getSampleSizeList() != null) {
             for(SampleSize size: studyDesign.getSampleSizeList()) {
                 params.addSampleSize(size.getValue());
             }
         }
         // add beta scale values
         if (studyDesign.getBetaScaleList() != null) {
             for(BetaScale betaScale: studyDesign.getBetaScaleList()) {
                 params.addBetaScale(betaScale.getValue());
             }
         }
         // add sigma scale values
         if (studyDesign.getSigmaScaleList() != null) {
             for(SigmaScale scale: studyDesign.getSigmaScaleList()) {
                 params.addSigmaScale(scale.getValue());
             }
         }
 
         /** Generate and add matrices **/
         // build design matrix
         params.setDesignEssence(designMatrixFromStudyDesign(studyDesign));
         // build beta matrix
         params.setBeta(betaMatrixFromStudyDesign(studyDesign));
         // build the between subject contrast
         params.setBetweenSubjectContrast(betweenParticipantContrastFromStudyDesign(studyDesign));
         // build the within subject contrast
         params.setWithinSubjectContrast(withinParticipantContrastFromStudyDesign(studyDesign));
         // build theta null matrix
         params.setTheta(thetaNullMatrixFromStudyDesign(studyDesign, 
                 params.getBetweenSubjectContrast(),
                 params.getWithinSubjectContrast()));
         // add matrices for either GLMM(F) or GLMM(F,g) designs
         if (studyDesign.isGaussianCovariate()) {
             params.setSigmaOutcome(sigmaOutcomesMatrixFromStudyDesign(studyDesign));
             params.setSigmaGaussianRandom(sigmaCovariateMatrixFromStudyDesign(studyDesign));
             params.setSigmaOutcomeGaussianRandom(sigmaOutcomesCovariateMatrixFromStudyDesign(studyDesign));
 
             // add power methods
             if (studyDesign.getPowerMethodList() != null) {
                 for(PowerMethod method: studyDesign.getPowerMethodList()) {
                     params.addPowerMethod(toGLMMPowerMethod(method));
                 }
             }
             // add quantiles
             if (studyDesign.getQuantileList() != null) {
                 for(Quantile quantile: studyDesign.getQuantileList()) {
                     params.addQuantile(quantile.getValue());
                 }
             }
         } else {
             params.setSigmaError(sigmaErrorMatrixFromStudyDesign(studyDesign));
             params.addPowerMethod(GLMMPowerParameters.PowerMethod.CONDITIONAL_POWER);
 
             // add confidence intervals if specified
             ConfidenceIntervalDescription CIdescr = studyDesign.getConfidenceIntervalDescriptions();
             if (CIdescr != null) {
                 params.setAlphaLowerConfidenceLimit(CIdescr.getLowerTailProbability());
                 params.setAlphaUpperConfidenceLimit(CIdescr.getUpperTailProbability());
                 params.setDesignMatrixRankForEstimates(CIdescr.getRankOfDesignMatrix());
                 params.setSampleSizeForEstimates(CIdescr.getSampleSize());
                 if (CIdescr.isBetaFixed() && !CIdescr.isSigmaFixed()) {
                     params.setConfidenceIntervalType(ConfidenceIntervalType.BETA_KNOWN_SIGMA_ESTIMATED);
                 } else if (!CIdescr.isBetaFixed() && !CIdescr.isSigmaFixed()) {
                     params.setConfidenceIntervalType(ConfidenceIntervalType.BETA_SIGMA_ESTIMATED);
                 }
             }
         }
 
         return params;
     }
 
     /**
      * Convert a domain layer PowerMethod to a GLMMPowerParameters.PowerMethod type.
      * @param method domain layer PowerMethod object
      * @return GLMM PowerMethod enum type
      * @throws IllegalArgumentException on unknown power methods
      */
     private static GLMMPowerParameters.PowerMethod toGLMMPowerMethod(PowerMethod method) 
     throws IllegalArgumentException {
         switch(method.getPowerMethodEnum()) {
         case CONDITIONAL:
             return GLMMPowerParameters.PowerMethod.CONDITIONAL_POWER;
         case QUANTILE:
             return GLMMPowerParameters.PowerMethod.QUANTILE_POWER;
         case UNCONDITIONAL:
             return GLMMPowerParameters.PowerMethod.CONDITIONAL_POWER;
         default:
             throw new IllegalArgumentException("unknown power method");
         }
     }
 
     /**
      * Convert a domain layer PowerMethod to a GLMMPowerParameters.PowerMethod type.
      * @param method domain layer PowerMethod object
      * @return GLMM PowerMethod enum type
      * @throws IllegalArgumentException on unknown power methods
      */
     private static PowerMethod toPowerMethod(GLMMPowerParameters.PowerMethod method) 
     throws IllegalArgumentException {
         switch(method) {
         case CONDITIONAL_POWER:
             return new PowerMethod(PowerMethodEnum.CONDITIONAL);
         case QUANTILE_POWER:
             return new PowerMethod(PowerMethodEnum.QUANTILE);
         case UNCONDITIONAL_POWER:
             return new PowerMethod(PowerMethodEnum.CONDITIONAL);
         default:
             throw new IllegalArgumentException("unknown power method");
         }
     }
 
     /**
      * Convert a domain layer statistical test to a GLMMTest enum
      */
     private static GLMMTestFactory.Test toGLMMTest(StatisticalTest test) 
     throws IllegalArgumentException {
         switch(test.getType()) {
         case UNIREP:
             return GLMMTestFactory.Test.UNIREP;
         case UNIREPBOX:
             return GLMMTestFactory.Test.UNIREP_BOX;
         case UNIREPGG:
             return GLMMTestFactory.Test.UNIREP_GEISSER_GREENHOUSE;
         case UNIREPHF:
             return GLMMTestFactory.Test.UNIREP_HUYNH_FELDT;
         case WL:
             return GLMMTestFactory.Test.WILKS_LAMBDA;
         case PBT:
             return GLMMTestFactory.Test.PILLAI_BARTLETT_TRACE;
         case HLT:
             return GLMMTestFactory.Test.HOTELLING_LAWLEY_TRACE;
         default:
             throw new IllegalArgumentException("unknown test");
         }
     }
 
     /**
      * Convert a domain layer statistical test to a GLMMTest enum
      */
     private static StatisticalTest toStatisticalTest(GLMMTestFactory.Test test) 
     throws IllegalArgumentException {
         switch(test) {
         case UNIREP:
             return new StatisticalTest(StatisticalTestTypeEnum.UNIREP);
         case UNIREP_BOX:
             return new StatisticalTest(StatisticalTestTypeEnum.UNIREPBOX);
         case UNIREP_GEISSER_GREENHOUSE:
             return new StatisticalTest(StatisticalTestTypeEnum.UNIREPGG);
         case UNIREP_HUYNH_FELDT:
             return new StatisticalTest(StatisticalTestTypeEnum.UNIREPHF);
         case WILKS_LAMBDA:
             return new StatisticalTest(StatisticalTestTypeEnum.WL);
         case PILLAI_BARTLETT_TRACE:
             return new StatisticalTest(StatisticalTestTypeEnum.PBT);
         case HOTELLING_LAWLEY_TRACE:
             return new StatisticalTest(StatisticalTestTypeEnum.HLT);
         default:
             throw new IllegalArgumentException("unknown test");
         }
     }
 
     /**
      * Return the design matrix if present, or generate a cell means coded
      * design essence matrix for "guided" study designs
      * @param studyDesign study design object
      * @return design essence matrix
      */
     private static RealMatrix designMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             // matrix based design
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_DESIGN));
         } else {
             /* For Guided study designs, we assume a cell means coding.  Thus, the design matrix is 
              * simply an identity matrix with dimension equal to the product of the number of levels
              * of each between subject factor
              */
             int dimension = 1;
             // calculate the product of the #levels of each between participant factor
             List<BetweenParticipantFactor> factors = studyDesign.getBetweenParticipantFactorList();
             for(BetweenParticipantFactor factor: factors) {
                 List<Category> categoryList = factor.getCategoryList();
                 if (categoryList != null) {
                     dimension *= categoryList.size();
                 }
             }
             // create the design matrix
             RealMatrix designEssenceMatrix = 
                 org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(dimension);
             return designEssenceMatrix;
         }
     }
 
     /**
      * Create a fixed/random beta matrix from the study design.
      * @param studyDesign study design object
      * @return fixed/random beta matrix
      */
     private static FixedRandomMatrix betaMatrixFromStudyDesign(StudyDesign studyDesign) {
         double[][] betaFixedData = null;
         double[][] betaRandomData = null;
         int rows = 0;
         
         NamedMatrix betaFixed = 
             studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETA);
         NamedMatrix betaRandom = 
             studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETA_RANDOM);
         // get the beta information from the study design matrices
         if (betaFixed != null) {
             betaFixedData = betaFixed.getData().getData();
             rows = betaFixed.getRows();
         }
         if (betaRandom != null) {
             betaRandomData = betaRandom.getData().getData();
         }
         // for guided mode designs, we need to adjust for clustering
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.GUIDED_MODE) {
             List<ClusterNode> clusterNodeList = studyDesign.getClusteringTree();
             if (clusterNodeList != null && clusterNodeList.size() > 0) {
                 int totalColumns = 1;
                 for(ClusterNode node: clusterNodeList) {
                     totalColumns *= node.getGroupSize();
                 }
                 
                 // direct product the beta matrix with a matrix of ones to 
                 // generate the proper dimensions for a cluster sample
                 RealMatrix oneMatrix = MatrixUtils.getRealMatrixWithFilledValue(rows, totalColumns, 1);
                 RealMatrix betaFixedMatrix = new Array2DRowRealMatrix(betaFixedData);
                 betaFixedMatrix = MatrixUtils.getHorizontalDirectProduct(betaFixedMatrix, oneMatrix);
                 // reset the data
                 betaFixedData = betaFixedMatrix.getData();
                 
                 // now repeat for the beta random matrix
                 if (betaRandom != null) {
                     oneMatrix = MatrixUtils.getRealMatrixWithFilledValue(1, totalColumns, 1);
                     RealMatrix betaRandomMatrix = new Array2DRowRealMatrix(betaRandomData);
                     betaRandomMatrix = MatrixUtils.getHorizontalDirectProduct(betaRandomMatrix, oneMatrix);
                     // reset the data
                     betaRandomData = betaRandomMatrix.getData();
                 } 
             }
         }
         
         FixedRandomMatrix betaFixedRandom = 
             new FixedRandomMatrix(betaFixedData, betaRandomData, false);
         return betaFixedRandom;
     }
 
     /**
      * Create a fixed/random between participant contrast (C matrix)
      *  from the study design.
      * @param studyDesign study design object
      * @return fixed/random C matrix
      */
     private static FixedRandomMatrix betweenParticipantContrastFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             // matrix based design
             NamedMatrix cFixed = 
                 studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETWEEN_CONTRAST);
             NamedMatrix cRandom = 
                 studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETWEEN_CONTRAST_RANDOM);
 
             return 
             new FixedRandomMatrix((cFixed != null ? cFixed.getData().getData() : null),
                     (cRandom != null ? cRandom.getData().getData() : null),
                     true);
 
         } else {
             // Guided design
             Set<Hypothesis> hypothesisSet = studyDesign.getHypothesis();
             if (hypothesisSet != null) {
                 // only consider the primary hypothesis at present (i.e. the first one)
                 Hypothesis hypothesis = hypothesisSet.iterator().next();
                 if (hypothesis != null) {
                     RealMatrix cFixed = null;
                     RealMatrix cRandom = null;
                     // get the factor of interest
                     List<HypothesisBetweenParticipantMapping> betweenMap = 
                         hypothesis.getBetweenParticipantFactorMapList();
                     if (betweenMap != null && betweenMap.size() > 0) {
                         // build the fixed part of the contrast based on the hypothesis of interest
                         switch (hypothesis.getType()) {
                         case MAIN_EFFECT:
                             // between subject factor of interest
                             cFixed = ContrastHelper.mainEffectBetween(betweenMap.get(0).getBetweenParticipantFactor(), 
                                     studyDesign.getBetweenParticipantFactorList());
                             break;
                         case INTERACTION:
                             cFixed = ContrastHelper.interactionBetween(betweenMap, 
                                     studyDesign.getBetweenParticipantFactorList());
                             break;
                         case TREND:
                             HypothesisBetweenParticipantMapping trendFactor = betweenMap.get(0);
                             cFixed = ContrastHelper.trendBetween(trendFactor,                                    
                                     studyDesign.getBetweenParticipantFactorList());
                         }
                     } else {
                         cFixed = ContrastHelper.grandMeanBetween(studyDesign.getBetweenParticipantFactorList());
                     }
 
                     // build the random contrast if the design has a baseline covariate
                     if (studyDesign.isGaussianCovariate()) {
                         if (cFixed != null) {
                             cRandom = MatrixUtils.getRealMatrixWithFilledValue(cFixed.getRowDimension(), 1, 0);
                         }
                     }
                     return new FixedRandomMatrix((cFixed != null ? cFixed.getData() : null), 
                             (cRandom != null ? cRandom.getData() : null), true);
                 }
 
                 // no hypothesis specified
                 return null; 
             }
         }
 
         // unknown view type
         return null; 
     }
 
 
 
     /**
      * Create the within participant contrast (U matrix)
      *  from the study design.
      * @param studyDesign study design object
      * @return U matrix
      */
     private static RealMatrix withinParticipantContrastFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_WITHIN_CONTRAST));
         } else {
             // Guided design
             Set<Hypothesis> hypothesisSet = studyDesign.getHypothesis();
             if (hypothesisSet != null) {
                 // only consider the primary hypothesis at present (i.e. the first one)
                 Hypothesis hypothesis = hypothesisSet.iterator().next();
                 if (hypothesis != null) {
                     RealMatrix withinContrast = null;
                     // get the factor of interest
                     List<HypothesisRepeatedMeasuresMapping> withinMap = 
                         hypothesis.getRepeatedMeasuresMapTree();
                     if (withinMap != null && withinMap.size() > 0) {
                         // build the fixed part of the contrast based on the hypothesis of interest
                         switch (hypothesis.getType()) {
                         case MAIN_EFFECT:
                             // between subject factor of interest
                             withinContrast = 
                                 ContrastHelper.mainEffectWithin(withinMap.get(0).getRepeatedMeasuresNode(), 
                                     studyDesign.getRepeatedMeasuresTree(),
                                     studyDesign.getResponseList());
                             break;
                         case INTERACTION:
                             withinContrast = ContrastHelper.interactionWithin(withinMap, 
                                    studyDesign.getRepeatedMeasuresTree(),
                                    studyDesign.getResponseList());
                             break;
                         case TREND:
                             HypothesisRepeatedMeasuresMapping trendFactor = withinMap.get(0);
                             withinContrast = ContrastHelper.trendWithin(trendFactor,                                    
                                     studyDesign.getRepeatedMeasuresTree(),
                                     studyDesign.getResponseList());
                         }
                     } else {
                         withinContrast = 
                                 ContrastHelper.grandMeanWithin(studyDesign.getRepeatedMeasuresTree(),
                                         studyDesign.getResponseList());
                     }
 
                     // expand rows if clustering is present
                     if (withinContrast != null) {
                         List<ClusterNode> clusterNodeList = studyDesign.getClusteringTree();
                         if (clusterNodeList != null && clusterNodeList.size() > 0) {
                             int totalRows = 1;
                             for(ClusterNode node: clusterNodeList) {
                                 totalRows *= node.getGroupSize();
                             }
 
                             // direct product the U matrix with a matrix of ones to 
                             // generate the proper dimensions for a cluster sample
                             RealMatrix oneMatrix = 
                                 MatrixUtils.getRealMatrixWithFilledValue(withinContrast.getColumnDimension(),
                                         totalRows, 1);
                             withinContrast = MatrixUtils.getHorizontalDirectProduct(withinContrast.transpose(), 
                                     oneMatrix).transpose();
 
                         }
                     }
                     
                     return withinContrast;
                 }
 
                 // no hypothesis specified
                 return null; 
             }
         }
 
         // unknown view type
         return null; 
     }
 
     /**
      * Create a sigma error matrix from the study design.
      * @param studyDesign study design object
      * @return sigma error matrix
      */
     private static RealMatrix sigmaErrorMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_SIGMA_ERROR));
         } else {
             // guided mode, so we need to decode the covariance objects
             // first, allocate a list of matrices to build the overall kronecker covariance
             ArrayList<RealMatrix> kroneckerMatrixList = new ArrayList<RealMatrix>();
             
             // add covariance information for clustering
             List<ClusterNode> clusterNodeList = studyDesign.getClusteringTree();
             if (clusterNodeList != null) {
                 for(ClusterNode clusterNode: clusterNodeList) {
                     int rows = clusterNode.getGroupSize();
                     int columns = rows;
                     double rho = clusterNode.getIntraClusterCorrelation();
                     // build a compound symmetric matrix
                     double[][] data = new double[rows][columns];
                     for(int row = 0; row < rows; row++) {
                         for(int col = row; col < columns; col++) {
                             if (row == col) {
                                 data[row][col] = 1;
                             } else {
                                 data[row][col] = rho;
                                 data[col][row] = rho;
                             }
                         }
                     }
                     // add the matrix to the kronecker product list
                     kroneckerMatrixList.add(new Array2DRowRealMatrix(data));
                 }
             }
             
             // add covariance for repeated measures
             List<RepeatedMeasuresNode> rmNodeList = studyDesign.getRepeatedMeasuresTree();
             if (rmNodeList != null) {
                 for(RepeatedMeasuresNode rmNode: rmNodeList) {
                     Covariance covariance = studyDesign.getCovarianceFromSet(rmNode.getDimension());
                     if (covariance != null) {
                         RealMatrix kroneckerMatrix = 
                                 CovarianceHelper.covarianceToRealMatrix(covariance, rmNode);
                         if (kroneckerMatrix != null) {
                             kroneckerMatrixList.add(kroneckerMatrix);
                         } else {
                             throw new IllegalArgumentException("Invalid covariance information for factor: " + 
                                     rmNode.getDimension());
                         }
                     } else {
                         throw new IllegalArgumentException("Missing covariance information for factor: " + 
                                 rmNode.getDimension());
                     }
                 }
             }
             // lastly, we need to add the covariance of responses
             Covariance covariance = studyDesign.getCovarianceFromSet(
                     PowerConstants.RESPONSES_COVARIANCE_LABEL);
             RealMatrix kroneckerMatrix = CovarianceHelper.covarianceToRealMatrix(covariance, 
                     studyDesign.getResponseList());
             if (kroneckerMatrix != null) {
                 kroneckerMatrixList.add(kroneckerMatrix);
             } else {
                 throw new IllegalArgumentException("Invalid covariance information for response variables");
             }
             
             return MatrixUtils.getKroneckerProduct(kroneckerMatrixList);
         }
     }
 
     /**
      * Create a sigma outcomes matrix from the study design.
      * @param studyDesign study design object
      * @return sigma outcomes matrix
      */
     private static RealMatrix sigmaOutcomesMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_SIGMA_OUTCOME));
         } else {
             return sigmaErrorMatrixFromStudyDesign(studyDesign);
         }
     }
 
     /**
      * Create a sigma outcomes/covariate matrix from the study design.
      * @param studyDesign study design object
      * @return sigma outcomes/covariate matrix
      */
     private static RealMatrix sigmaOutcomesCovariateMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.
                     MATRIX_SIGMA_OUTCOME_GAUSSIAN));
         } else {
             RealMatrix sigmaYG = toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.
                     MATRIX_SIGMA_OUTCOME_GAUSSIAN));
             if (sigmaYG != null) {
                 List<ClusterNode> clusterNodeList = studyDesign.getClusteringTree();
                 if (clusterNodeList != null && clusterNodeList.size() > 0) {
                     int totalRows = 1;
                     for(ClusterNode node: clusterNodeList) {
                         totalRows *= node.getGroupSize();
                     }
                     
                     // direct product the sigmaYG matrix with a matrix of ones to 
                     // generate the proper dimensions for a cluster sample
                     RealMatrix oneMatrix = MatrixUtils.getRealMatrixWithFilledValue(1,totalRows, 1);
                     RealMatrix sigmaYGTranspose = 
                             MatrixUtils.getHorizontalDirectProduct(sigmaYG.transpose(), oneMatrix);
                     sigmaYG = sigmaYGTranspose.transpose();
                 }
             }
             return sigmaYG;
         }
     }
 
     /**
      * Create a sigma covariate matrix from the study design.
      * @param studyDesign study design object
      * @return sigma covariate matrix
      */
     private static RealMatrix sigmaCovariateMatrixFromStudyDesign(StudyDesign studyDesign) {
         return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_SIGMA_GAUSSIAN));
     }
 
     /**
      * Create a null hypothesis matrix from the study design.
      * @param studyDesign study design object
      * @return theta null matrix
      */
     private static RealMatrix thetaNullMatrixFromStudyDesign(StudyDesign studyDesign,
             FixedRandomMatrix C, RealMatrix U) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_THETA_NULL));
         } else {
             RealMatrix thetaNull = 
                 toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_THETA_NULL));
             if (thetaNull == null) {
                 if (C != null && C.getFixedMatrix() != null && U != null) {
                     int rows = C.getFixedMatrix().getRowDimension();
                     int columns = U.getColumnDimension();
                     thetaNull = MatrixUtils.getRealMatrixWithFilledValue(rows, columns, 0);
                 } 
             }
             return thetaNull; 
         }
     }
 
     /**
      * Create the list of matrices generated by the specified study design
      * @param design study design object
      * @return
      */
     public static NamedMatrixList namedMatrixListFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign == null) {
             return null;
         }
         // allocate a result list
         NamedMatrixList matrixList = new NamedMatrixList();
         // parse the study design into matrices 
         // build design matrix
         NamedMatrix X = toNamedMatrix(designMatrixFromStudyDesign(studyDesign),
                 PowerConstants.MATRIX_DESIGN);
         if (X != null) matrixList.add(X);
         // build beta matrix
         FixedRandomMatrix beta = betaMatrixFromStudyDesign(studyDesign);
         if (beta != null) {
             matrixList.add(toNamedMatrix(beta.getFixedMatrix(), PowerConstants.MATRIX_BETA));
             if (studyDesign.isGaussianCovariate()) {
                 matrixList.add(toNamedMatrix(beta.getRandomMatrix(), 
                         PowerConstants.MATRIX_BETA_RANDOM));
             }
         }
         // build the between subject contrast
         FixedRandomMatrix C = betweenParticipantContrastFromStudyDesign(studyDesign);
         if (C != null) {
             matrixList.add(toNamedMatrix(C.getFixedMatrix(), 
                     PowerConstants.MATRIX_BETWEEN_CONTRAST));
             if (studyDesign.isGaussianCovariate()) {
                 matrixList.add(toNamedMatrix(C.getRandomMatrix(), 
                         PowerConstants.MATRIX_BETWEEN_CONTRAST_RANDOM));
             }
         }
 
         // build the within subject contrast
         RealMatrix U = withinParticipantContrastFromStudyDesign(studyDesign);
         if (U != null) {
             matrixList.add(toNamedMatrix(U, PowerConstants.MATRIX_WITHIN_CONTRAST));
         }
         // build theta null matrix
         NamedMatrix thetaNull = toNamedMatrix(thetaNullMatrixFromStudyDesign(studyDesign, C, U),
                 PowerConstants.MATRIX_THETA_NULL);
         matrixList.add(thetaNull);
 
         // add matrices for either GLMM(F) or GLMM(F,g) designs
         if (studyDesign.isGaussianCovariate()) {
             NamedMatrix sigmaY = 
                 toNamedMatrix(sigmaOutcomesMatrixFromStudyDesign(studyDesign),
                         PowerConstants.MATRIX_SIGMA_OUTCOME);
             if (sigmaY != null) matrixList.add(sigmaY);
 
             NamedMatrix sigmaG =
                 toNamedMatrix(sigmaCovariateMatrixFromStudyDesign(studyDesign),
                         PowerConstants.MATRIX_SIGMA_GAUSSIAN);
             if (sigmaG != null) matrixList.add(sigmaG);
 
             NamedMatrix sigmaYG = 
                 toNamedMatrix(sigmaOutcomesCovariateMatrixFromStudyDesign(studyDesign),
                         PowerConstants.MATRIX_SIGMA_OUTCOME_GAUSSIAN);
             if (sigmaYG != null) matrixList.add(sigmaYG);
         } else {
             NamedMatrix sigmaE = 
                 toNamedMatrix(sigmaErrorMatrixFromStudyDesign(studyDesign),
                         PowerConstants.MATRIX_SIGMA_ERROR);
             if (sigmaE != null) matrixList.add(sigmaE);
         }
         return matrixList;
     }
 
     /**
      * This method takes Named Matrix and converts it into a RealMatrix and
      * returns a Real Matrix.
      * 
      * @param matrix
      *            The matrix is a input matrix of type NamedMatrix which is to
      *            be converted to type RealMatrix.
      * @return RealMatrix Returns a RealMatrix which is obtained by converting
      *         the input matrix to a RealMatrix.
      */
     public static RealMatrix toRealMatrix(final NamedMatrix namedMatrix) {
         RealMatrix realMatrix = null;
         if (namedMatrix != null) {
             Blob2DArray blob = namedMatrix.getData();
             if (blob != null) {
                 realMatrix = new Array2DRowRealMatrix(blob.getData());
             }
         }
         return realMatrix;
     }
 
     /**
      * This method takes a Real Matrix and converts it into a Named Matrix and
      * returns that Named Matrix.
      * 
      * @param matrix
      *            The matrix is a input matrix of type RealMatrix and is to be
      *            converted to a NamedMatrix.
      * @param name
      *            the name is a String, which is to be assigned to named matrix.
      * @return namedMatrix Returns a NamedMatrix which is obtained by converting
      *         the input matrix to NamedMatrix
      */
     public static NamedMatrix toNamedMatrix(final RealMatrix matrix, final String name) {
         if (matrix == null || name == null || name.isEmpty()) {
             logger.error("failed to create NamedMatrix object name=[" + (name != null ? name : "NULL")+ "]");
             return null;
         }
         NamedMatrix namedMatrix = new NamedMatrix();
         namedMatrix.setDataFromArray(matrix.getData());
         namedMatrix.setName(name);
         namedMatrix.setColumns(matrix.getColumnDimension());
         namedMatrix.setRows(matrix.getRowDimension());
         return namedMatrix;
     }
 
     /**
      * Convert a list of GLMMPower objects to a list of PowerResult objects
      * @param powerList GLMMPower object list
      * @return list of PowerResult objects
      */
     public static PowerResultList toPowerResultList(List<Power> powerList) {
         PowerResultList resultList = new PowerResultList();
         for(Power power: powerList)
         {
             resultList.add(toPowerResult((GLMMPower) power));
         }
         return resultList;
     }
 
     /**
      * Convert a GLMMPower object to PowerResult objects
      * @param glmmPower GLMMPower object
      * @return PowerResult object
      */
     public static PowerResult toPowerResult(GLMMPower glmmPower) {
         Quantile quantile = null;
         if (!Double.isNaN(glmmPower.getQuantile())) {
             quantile = new Quantile(glmmPower.getQuantile());
         }
 
         return new PowerResult(
                 toStatisticalTest(glmmPower.getTest()),
                 new TypeIError(glmmPower.getAlpha()),
                 new NominalPower(glmmPower.getNominalPower()),
                 glmmPower.getActualPower(),
                 glmmPower.getTotalSampleSize(),
                 new BetaScale(glmmPower.getBetaScale()),
                 new SigmaScale(glmmPower.getSigmaScale()),
                 toPowerMethod(glmmPower.getPowerMethod()),
                 quantile,
                 toConfidenceInterval(glmmPower.getConfidenceInterval())  
         );        
     }
 
     /**
      * Convert a JavaStatistics confidence interval into a domain layer
      * confidence interval
      * @param ci
      * @return domain layer confidence interval object
      */
     private static ConfidenceInterval toConfidenceInterval(edu.cudenver.bios.utils.ConfidenceInterval ci) {
         if (ci == null) {
             return null;
         } else {
             return new ConfidenceInterval(ci.getLowerLimit(), ci.getUpperLimit(),
                     ci.getAlphaLower(), ci.getAlphaUpper());
         }
     }
 
 }
