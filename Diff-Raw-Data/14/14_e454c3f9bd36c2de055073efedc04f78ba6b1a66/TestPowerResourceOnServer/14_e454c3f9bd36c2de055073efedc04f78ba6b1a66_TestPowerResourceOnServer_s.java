 package edu.ucdenver.bios.powersvc.resource.test;
 
 import java.util.ArrayList;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.math.linear.MatrixUtils;
 import org.restlet.resource.ClientResource;
 
 import edu.ucdenver.bios.powersvc.application.PowerConstants;
 import edu.ucdenver.bios.powersvc.resource.DetectableDifferenceResource;
 import edu.ucdenver.bios.powersvc.resource.PowerMatrixResource;
 import edu.ucdenver.bios.powersvc.resource.PowerResource;
 import edu.ucdenver.bios.powersvc.resource.PowerResourceHelper;
 import edu.ucdenver.bios.powersvc.resource.SampleSizeResource;
 import edu.ucdenver.bios.webservice.common.domain.BetaScale;
 import edu.ucdenver.bios.webservice.common.domain.NamedMatrix;
 import edu.ucdenver.bios.webservice.common.domain.NominalPower;
 import edu.ucdenver.bios.webservice.common.domain.PowerResult;
 import edu.ucdenver.bios.webservice.common.domain.SampleSize;
 import edu.ucdenver.bios.webservice.common.domain.SigmaScale;
 import edu.ucdenver.bios.webservice.common.domain.StatisticalTest;
 import edu.ucdenver.bios.webservice.common.domain.StudyDesign;
 import edu.ucdenver.bios.webservice.common.domain.TypeIError;
 import edu.ucdenver.bios.webservice.common.enums.SolutionTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.StatisticalTestTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.StudyDesignViewTypeEnum;
 
 public class TestPowerResourceOnServer extends TestCase {
 
     private static final String HOST_PORT = "localhost:8080";
     private static final String POWER_URI = "http://" + HOST_PORT + "/power/power";
     private static final String SAMPLE_SIZE_URI = "http://" + HOST_PORT + "/power/samplesize";
     private static final String DIFFERENCE_URI = "http://" + HOST_PORT + "/power/difference";
     private static final String MATRIX_URI = "http://" + HOST_PORT + "/power/matrix";
     
     /**
      * Calculate power for a two-sample t test
      */
     public void testUnivariateMatrixPower()
     {
         try
         {
             // build an input study design
             StudyDesign studyDesign = buildUnivariateMatrixDesign(SolutionTypeEnum.POWER);
             // connect to local Power Service
             ClientResource clientResource = new ClientResource(POWER_URI); 
             PowerResource powerResource = clientResource.wrap(PowerResource.class);
             // calculate power
             System.out.println(studyDesign.getName() + ": calculating power.");
             ArrayList<PowerResult> powerList = powerResource.getPower(studyDesign);
             System.out.println("Number of results: " + (powerList != null ? powerList.size() : 0));
             for(PowerResult power : powerList)
             {
                 System.out.println(power.toXML());
             }
             assertTrue(true);
         }
         catch (Exception e)
         {
             System.err.println("Failed: " + e.getMessage());
             fail();
         }
     }
 
     /**
      * Calculate sample size for a two-sample t test
      */
     public void testUnivariateMatrixSampleSize()
     {
         try
         {
             // build an input study design
             StudyDesign studyDesign = buildUnivariateMatrixDesign(SolutionTypeEnum.SAMPLE_SIZE);
             // connect to local Power Service
             ClientResource clientResource = new ClientResource(SAMPLE_SIZE_URI); 
             SampleSizeResource sampleSizeResource = clientResource.wrap(SampleSizeResource.class);
             // calculate sample size
             System.out.println(studyDesign.getName() + ": calculating sample size.");
             ArrayList<PowerResult> powerList = sampleSizeResource.getSampleSize(studyDesign);
             System.out.println("Number of results: " + (powerList != null ? powerList.size() : 0));
             for(PowerResult power : powerList)
             {
                 System.out.println(power.toXML());
             }
             assertTrue(true);
         }
         catch (Exception e)
         {
             System.err.println("Failed: " + e.getMessage());
             fail();
         }
     }
 
     /**
      * Calculate detectable difference for a two-sample t test
      */
     public void testUnivariateMatrixDifference()
     {
         try
         {
             // build an input study design
             StudyDesign studyDesign = buildUnivariateMatrixDesign(SolutionTypeEnum.DETECTABLE_DIFFERENCE);
             // connect to local Power Service
             ClientResource clientResource = new ClientResource(DIFFERENCE_URI); 
             DetectableDifferenceResource differenceResource = clientResource.wrap(DetectableDifferenceResource.class);
             // get detectable difference
             System.out.println(studyDesign.getName() + ": calculating detectable difference.");
             ArrayList<PowerResult> powerList = differenceResource.getDetectableDifference(studyDesign);
             System.out.println("Number of results: " + (powerList != null ? powerList.size() : 0));
             for(PowerResult power : powerList)
             {
                 System.out.println(power.toXML());
             }
             assertTrue(true);
         }
         catch (Exception e)
         {
             System.err.println("Failed: " + e.getMessage());
             fail();
         }
     }
 
     /**
      * 
      */
     public void testUnivariateStudyDesignMatrices()
     {
         try
         {
             // build an input study design
             StudyDesign studyDesign = buildUnivariateMatrixDesign(SolutionTypeEnum.POWER);
             // connect to local Power Service
             ClientResource clientResource = new ClientResource(MATRIX_URI); 
             PowerMatrixResource matrixResource = clientResource.wrap(PowerMatrixResource.class);
             // get matrices from study design
             System.out.println(studyDesign.getName() + ": power matrices:");
             ArrayList<NamedMatrix> matrixList = matrixResource.getMatrices(studyDesign);
             System.out.println("Number of results: " + (matrixList != null ? matrixList.size() : 0));
             for(NamedMatrix matrix : matrixList)
             {
                 System.out.println(matrix.toString());
             }
             assertEquals((matrixList != null ? matrixList.size() : 0), 5);
         }
         catch (Exception e)
         {
             System.err.println("Failed: " + e.getMessage());
             fail();
         }
     }
     
     /**
      * Create a StudyDesign object representing a basic t-test design
      * 
      * @return StudyDesign 
      */
     private StudyDesign buildUnivariateMatrixDesign(SolutionTypeEnum solvingFor)
     {
         StudyDesign studyDesign = new StudyDesign();
         studyDesign.setViewTypeEnum(StudyDesignViewTypeEnum.MATRIX_MODE);
         studyDesign.setSolutionTypeEnum(solvingFor);
         studyDesign.setName("Two Sample T-Test");
 
         if (solvingFor == SolutionTypeEnum.POWER
                 || solvingFor == SolutionTypeEnum.SAMPLE_SIZE) {
             // add beta scale values
             ArrayList<BetaScale> betaScaleList = new ArrayList<BetaScale>();
             betaScaleList.add(new BetaScale(0.5));
             //            betaScaleList.add(new BetaScale(1.0));
             //            betaScaleList.add(new BetaScale(2.0));
             studyDesign.setBetaScaleList(betaScaleList);
         }
 
         if (solvingFor == SolutionTypeEnum.POWER
                 || solvingFor == SolutionTypeEnum.DETECTABLE_DIFFERENCE) {
             // add per group sample sizes
             ArrayList<SampleSize> sampleSizeList = new ArrayList<SampleSize>();
             sampleSizeList.add(new SampleSize(10));
             sampleSizeList.add(new SampleSize(20));
             sampleSizeList.add(new SampleSize(40));
             studyDesign.setSampleSizeList(sampleSizeList);
         }
 
         if (solvingFor == SolutionTypeEnum.SAMPLE_SIZE
                 || solvingFor == SolutionTypeEnum.DETECTABLE_DIFFERENCE) {
             // add nominal power values
             ArrayList<NominalPower> nominalPowerList = new ArrayList<NominalPower>();
             nominalPowerList.add(new NominalPower(0.8));
             nominalPowerList.add(new NominalPower(0.9));
             nominalPowerList.add(new NominalPower(0.975));
             studyDesign.setNominalPowerList(nominalPowerList);
         }
 
         // add a test 
         ArrayList<StatisticalTest> testList = new ArrayList<StatisticalTest>();
         testList.add(new StatisticalTest(StatisticalTestTypeEnum.UNIREP));
         studyDesign.setStatisticalTestList(testList);
 
         // add alpha values
         ArrayList<TypeIError> alphaList = new ArrayList<TypeIError>();
         alphaList.add(new TypeIError(0.05));
         //        alphaList.add(new TypeIError(0.01));
         studyDesign.setAlphaList(alphaList);
 
         // add sigma scale values
         ArrayList<SigmaScale> sigmaScaleList = new ArrayList<SigmaScale>();
         //        sigmaScaleList.add(new SigmaScale(0.5));
         //        sigmaScaleList.add(new SigmaScale(1.0));
         sigmaScaleList.add(new SigmaScale(2.0));
         studyDesign.setSigmaScaleList(sigmaScaleList);
 
         // build the design eseence matrix
         studyDesign.setNamedMatrix(
                 PowerResourceHelper.toNamedMatrix(
                         MatrixUtils.createRealIdentityMatrix(2),
                         PowerConstants.MATRIX_DESIGN));
 
         // build between subject contrast
         double [][] betweenData = {{1,-1}};
         NamedMatrix betweenContrast = new NamedMatrix(PowerConstants.MATRIX_BETWEEN_CONTRAST);
        betweenContrast.setData(betweenData);
         betweenContrast.setRows(1);
         betweenContrast.setColumns(2);
         studyDesign.setNamedMatrix(betweenContrast);
 
         // build beta matrix
         double [][] betaData = {{0},{1}};
         NamedMatrix beta = new NamedMatrix(PowerConstants.MATRIX_BETA);
        beta.setData(betaData);
         beta.setRows(2);
         beta.setColumns(1);
         studyDesign.setNamedMatrix(beta);
 
         // build theta null matrix
         double [][] thetaNullData = {{0}};
         NamedMatrix thetaNull = new NamedMatrix(PowerConstants.MATRIX_THETA_NULL);
        thetaNull.setData(thetaNullData);
         thetaNull.setRows(1);
         thetaNull.setColumns(1);
         studyDesign.setNamedMatrix(thetaNull);
 
         // build sigma matrix
         double [][] sigmaData = {{1}};
         NamedMatrix sigmaError = new NamedMatrix(PowerConstants.MATRIX_SIGMA_ERROR);
        sigmaError.setData(sigmaData);
         sigmaError.setRows(1);
         sigmaError.setColumns(1);
         studyDesign.setNamedMatrix(sigmaError);

         return studyDesign;
     }
 }
