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
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.log4j.Logger;
 
 import edu.cudenver.bios.matrix.FixedRandomMatrix;
 import edu.cudenver.bios.power.GLMMPower;
 import edu.cudenver.bios.power.Power;
 import edu.cudenver.bios.power.glmm.GLMMTestFactory;
 import edu.cudenver.bios.power.parameters.GLMMPowerParameters;
 import edu.ucdenver.bios.powersvc.application.PowerConstants;
 import edu.ucdenver.bios.powersvc.application.PowerLogger;
 import edu.ucdenver.bios.webservice.common.domain.BetaScale;
 import edu.ucdenver.bios.webservice.common.domain.ConfidenceInterval;
 import edu.ucdenver.bios.webservice.common.domain.NamedMatrix;
 import edu.ucdenver.bios.webservice.common.domain.NominalPower;
 import edu.ucdenver.bios.webservice.common.domain.PowerMethod;
 import edu.ucdenver.bios.webservice.common.domain.PowerResult;
 import edu.ucdenver.bios.webservice.common.domain.Quantile;
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
         params.setTheta(thetaNullMatrixFromStudyDesign(studyDesign));
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
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_DESIGN));
         } else {
             // TODO
             // determine dimensions
             int columns = 1;
             int rows = 1;
             // create the design matrix
             RealMatrix designEssenceMatrix = new Array2DRowRealMatrix(rows, columns);
             return designEssenceMatrix;
         }
     }
 
     /**
      * Create a fixed/random beta matrix from the study design.
      * @param studyDesign study design object
      * @return fixed/random beta matrix
      */
     private static FixedRandomMatrix betaMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             NamedMatrix betaFixed = 
                 studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETA);
             NamedMatrix betaRandom = 
                 studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETA_RANDOM);
 
             FixedRandomMatrix betaFixedRandom = 
                new FixedRandomMatrix((betaFixed != null ? betaFixed.getDataFromBlob() : null),
                        (betaRandom != null ? betaRandom.getDataFromBlob() : null),
                         false);
             return betaFixedRandom;
         } else {
             return null; // TODO
         }
     }
 
     /**
      * Create a fixed/random between participant contrast (C matrix)
      *  from the study design.
      * @param studyDesign study design object
      * @return fixed/random C matrix
      */
     private static FixedRandomMatrix betweenParticipantContrastFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             NamedMatrix cFixed = 
                 studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETWEEN_CONTRAST);
             NamedMatrix cRandom = 
                 studyDesign.getNamedMatrix(PowerConstants.MATRIX_BETWEEN_CONTRAST_RANDOM);
 
             FixedRandomMatrix betaFixedRandom = 
                new FixedRandomMatrix((cFixed != null ? cFixed.getDataFromBlob() : null),
                        (cRandom != null ? cRandom.getDataFromBlob() : null),
                         true);
             return betaFixedRandom;
         } else {
             return null; // TODO
         }
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
             return null; // TODO
         }
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
             return null; // TODO
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
             return null; // TODO
         }
     }
 
     /**
      * Create a sigma outcomes/covariate matrix from the study design.
      * @param studyDesign study design object
      * @return sigma outcomes/covariate matrix
      */
     private static RealMatrix sigmaOutcomesCovariateMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_SIGMA_OUTCOME_GAUSSIAN));
         } else {
             return null; // TODO
         }
     }
 
     /**
      * Create a sigma covariate matrix from the study design.
      * @param studyDesign study design object
      * @return sigma covariate matrix
      */
     private static RealMatrix sigmaCovariateMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_SIGMA_GAUSSIAN));
         } else {
             return null; // TODO
         }
     }
 
     /**
      * Create a null hypothesis matrix from the study design.
      * @param studyDesign study design object
      * @return theta null matrix
      */
     private static RealMatrix thetaNullMatrixFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign.getViewTypeEnum() == StudyDesignViewTypeEnum.MATRIX_MODE) {
             return toRealMatrix(studyDesign.getNamedMatrix(PowerConstants.MATRIX_THETA_NULL));
         } else {
             return null; // TODO
         }
     }
 
     /**
      * Create the list of matrices generated by the specified study design
      * @param design study design object
      * @return
      */
     public static ArrayList<NamedMatrix> namedMatrixListFromStudyDesign(StudyDesign studyDesign) {
         if (studyDesign == null) {
             return null;
         }
         // allocate a result list
         ArrayList<NamedMatrix> matrixList = new ArrayList<NamedMatrix>();
         // parse the study design into matrices 
         // build design matrix
         matrixList.add(toNamedMatrix(designMatrixFromStudyDesign(studyDesign),
                 PowerConstants.MATRIX_DESIGN));
         // build beta matrix
         FixedRandomMatrix beta = betaMatrixFromStudyDesign(studyDesign);
         matrixList.add(toNamedMatrix(beta.getFixedMatrix(), PowerConstants.MATRIX_BETA));
         if (studyDesign.isGaussianCovariate()) {
             matrixList.add(toNamedMatrix(beta.getRandomMatrix(), 
                     PowerConstants.MATRIX_BETA_RANDOM));
         }
         // build the between subject contrast
         FixedRandomMatrix C = betweenParticipantContrastFromStudyDesign(studyDesign);
         matrixList.add(toNamedMatrix(C.getFixedMatrix(), 
                 PowerConstants.MATRIX_BETWEEN_CONTRAST));
         if (studyDesign.isGaussianCovariate()) {
             matrixList.add(toNamedMatrix(C.getRandomMatrix(), 
                     PowerConstants.MATRIX_BETWEEN_CONTRAST_RANDOM));
         }
 
         // build the within subject contrast
         RealMatrix U = withinParticipantContrastFromStudyDesign(studyDesign);
         if (U != null) {
             matrixList.add(toNamedMatrix(U, PowerConstants.MATRIX_WITHIN_CONTRAST));
         }
         // build theta null matrix
         matrixList.add(toNamedMatrix(thetaNullMatrixFromStudyDesign(studyDesign),
                 PowerConstants.MATRIX_THETA_NULL));
         // add matrices for either GLMM(F) or GLMM(F,g) designs
         if (studyDesign.isGaussianCovariate()) {
             matrixList.add(toNamedMatrix(sigmaOutcomesMatrixFromStudyDesign(studyDesign),
                     PowerConstants.MATRIX_SIGMA_OUTCOME));
             matrixList.add(toNamedMatrix(sigmaCovariateMatrixFromStudyDesign(studyDesign),
                     PowerConstants.MATRIX_SIGMA_GAUSSIAN));
             matrixList.add(toNamedMatrix(sigmaOutcomesCovariateMatrixFromStudyDesign(studyDesign),
                     PowerConstants.MATRIX_SIGMA_OUTCOME_GAUSSIAN));
         } else {
             matrixList.add(toNamedMatrix(sigmaErrorMatrixFromStudyDesign(studyDesign),
                     PowerConstants.MATRIX_SIGMA_ERROR));
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
             realMatrix = new Array2DRowRealMatrix(namedMatrix.getData().getData());
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
        namedMatrix.setData(matrix.getData());
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
     public static ArrayList<PowerResult> toPowerResultList(List<Power> powerList) {
         ArrayList<PowerResult> resultList = new ArrayList<PowerResult>();
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
