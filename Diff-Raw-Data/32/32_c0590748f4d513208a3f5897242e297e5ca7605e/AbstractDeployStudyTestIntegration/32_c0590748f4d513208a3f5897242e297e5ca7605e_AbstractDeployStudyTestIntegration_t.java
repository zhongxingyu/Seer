 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.application.study;
 
 import gov.nih.nci.caintegrator2.AcegiAuthenticationStub;
 import gov.nih.nci.caintegrator2.application.arraydata.AffymetrixPlatformSource;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataType;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.application.arraydata.DataRetrievalRequest;
 import gov.nih.nci.caintegrator2.application.arraydata.PlatformHelper;
 import gov.nih.nci.caintegrator2.application.arraydata.PlatformLoadingException;
 import gov.nih.nci.caintegrator2.application.query.QueryManagementService;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.StringPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalValueDefinition;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GeneNameCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.DataRetrievalException;
 import gov.nih.nci.caintegrator2.external.caarray.ExperimentNotFoundException;
 import gov.nih.nci.caintegrator2.external.caarray.NoSamplesForExperimentException;
import gov.nih.nci.caintegrator2.file.FileManager;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public abstract class AbstractDeployStudyTestIntegration extends AbstractTransactionalSpringContextTests {
     
     private final Logger logger = Logger.getLogger(getClass());
     private long startTime;
     
     private StudyManagementService service;
     private QueryManagementService queryManagementService;
     private StudyConfiguration studyConfiguration;
     private DelimitedTextClinicalSourceConfiguration sourceConfiguration;
     private CaIntegrator2Dao dao;
     private ArrayDataService arrayDataService;
     private Platform design;
    private FileManager fileManager;
     
     public AbstractDeployStudyTestIntegration() {
         setDefaultRollback(false);
     }
     
     protected String[] getConfigLocations() {
         return new String[] {"classpath*:/**/service-test-integration-config.xml"};
     }
     
     /**
      * @param caIntegrator2Dao the caIntegrator2Dao to set
      */
     public void setStudyManagementService(StudyManagementService studyManagementService) {
         this.service = studyManagementService;
     }
     
     public void deployStudy() throws ValidationException, IOException, ConnectionException, PlatformLoadingException, DataRetrievalException, ExperimentNotFoundException, NoSamplesForExperimentException {
         AcegiAuthenticationStub authentication = new AcegiAuthenticationStub();
         authentication.setUsername("manager");
         SecurityContextHolder.getContext().setAuthentication(authentication);
         loadDesign();
         studyConfiguration = new StudyConfiguration();
         studyConfiguration.getStudy().setShortTitleText(getStudyName());
         studyConfiguration.getStudy().setLongTitleText(getDescription());
         service.save(studyConfiguration);
        clearStudyDirectory(studyConfiguration.getStudy());
         loadAnnotationDefinitions();
         loadClinicalData();
         loadSurvivalValueDefinition();
         loadSamples();
         mapSamples();
         loadControlSamples();
         ImageDataSourceConfiguration imageSource = loadImages();
         mapImages();
         loadImageAnnotation(imageSource);
         deploy();
         checkArrayData();
         checkQueries();
     }
 
    private void clearStudyDirectory(Study study) throws IOException {
        File studyDirectory = fileManager.getStudyDirectory(study);
        if (studyDirectory.exists()) {
            FileUtils.cleanDirectory(studyDirectory);            
        }
    }

     abstract protected String getStudyName();
     
     protected String getDescription() {
         return getStudyName() + " demo study";
     }
 
     private void loadDesign() throws PlatformLoadingException {
         if (getLoadDesign()) {
             logStart();
             design = getExistingDesign();
             if (design == null) {
                 AffymetrixPlatformSource designSource = new AffymetrixPlatformSource(getPlatformAnnotationFile());
                 design = arrayDataService.loadArrayDesign(designSource);
             }
             logEnd();
         }
     }
 
     private void logStart() {
         startTime = System.currentTimeMillis();
         logger.info("start " + getMethodName() + "()");
     }
 
     private void logEnd() {
         long duration = System.currentTimeMillis() - startTime;
         logger.info("end " + getMethodName() + "(), duration: " + duration + " ms");
     }
 
     private String getMethodName() {
         Exception e = new Exception();
         e.fillInStackTrace();
         return e.getStackTrace()[2].getMethodName();
     }
 
     private Platform getExistingDesign() {
         return dao.getPlatform(getPlatformName());
     }
 
     protected abstract String getPlatformName();
     
     protected abstract File getPlatformAnnotationFile();
 
     protected boolean getLoadDesign() {
         return true;
     }
 
     abstract protected String getNCIAServerUrl();
     
     private ImageDataSourceConfiguration loadImages() throws ConnectionException {
         if (getLoadImages()) {
             logStart();
             ImageDataSourceConfiguration imageSource = new ImageDataSourceConfiguration();
             imageSource.getServerProfile().setUrl(getNCIAServerUrl());
             imageSource.getServerProfile().setHostname(Cai2Util.getHostNameFromUrl(getNCIAServerUrl()));
             imageSource.setCollectionName(getNCIATrialId());
             imageSource.setMappingFileName(getImageMappingFile().getName());
             service.addImageSource(studyConfiguration, imageSource);
             logEnd();
             return imageSource;
         }
         return null;
     }
 
     protected boolean getLoadImages() {
         return true;
     }
 
     abstract protected String getNCIATrialId();
 
     private void loadImageAnnotation(ImageDataSourceConfiguration imageSource) throws ValidationException, IOException {
         if (getLoadImageAnnotation()) {
             logStart();
             ImageAnnotationConfiguration imageAnnotationConfiguration = 
                 service.addImageAnnotationFile(imageSource, getImageAnnotationFile(), 
                         getImageAnnotationFile().getName());
             imageSource.setImageAnnotationConfiguration(imageAnnotationConfiguration);
             imageAnnotationConfiguration.getAnnotationFile().setIdentifierColumnIndex(0);
             service.loadImageAnnotation(studyConfiguration);
             logEnd();
         }
     }
 
     protected boolean getLoadImageAnnotation() {
         return true;
     }
 
     protected abstract File getImageAnnotationFile();
 
     private void mapImages() throws ValidationException, IOException {
         if (getMapImages()) {
             logStart();
             service.mapImageSeriesAcquisitions(studyConfiguration, getImageMappingFile());
             logEnd();
         }
     }
 
     protected boolean getMapImages() {
         return true;
     }
 
     abstract protected File getImageMappingFile();
 
     private void checkArrayData() {
         if (getLoadSamples()) {
             logStart();
             Collection<ArrayData> arrayDatas = studyConfiguration.getGenomicDataSources().get(0).getMappedSamples().get(0).getArrayDataCollection();
             for (ArrayData arrayData : arrayDatas) {
                 assertEquals(studyConfiguration.getStudy(), arrayData.getStudy());
             }
             PlatformHelper platformHelper = new PlatformHelper(design);
             ReporterList probeSetReporterList = platformHelper.getReporterList(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
             DataRetrievalRequest probeSetRequest = new DataRetrievalRequest();
             probeSetRequest.addArrayDatas(probeSetReporterList.getArrayDatas());
             probeSetRequest.addType(ArrayDataType.EXPRESSION_SIGNAL);
             probeSetRequest.addReporters(probeSetReporterList.getReporters());
             DataRetrievalRequest geneRequest = new DataRetrievalRequest();
             ReporterList geneReporterList = platformHelper.getReporterList(ReporterTypeEnum.GENE_EXPRESSION_GENE);
             geneRequest.addArrayDatas(geneReporterList.getArrayDatas());
             geneRequest.addType(ArrayDataType.EXPRESSION_SIGNAL);
             geneRequest.addReporters(geneReporterList.getReporters());
             ArrayDataValues values = arrayDataService.getData(probeSetRequest);
             assertEquals(getExpectedSampleCount(), values.getArrayDatas().size());
             assertEquals(getExpectedNumberProbeSets(), values.getReporters().size());
             values = arrayDataService.getData(geneRequest);
             assertEquals(getExpectedSampleCount(), values.getArrayDatas().size());
             assertEquals(getExpectedNumberOfGeneReporters(), values.getReporters().size());
             logEnd();
         }
     }
 
     protected abstract int getExpectedNumberOfGeneReporters();
 
     protected abstract int getExpectedNumberProbeSets();
     
     protected boolean getLoadSamples() {
         return true;
     }
 
     protected abstract int getExpectedSampleCount();
 
     protected abstract int getExpectedMappedSampleCount();
 
     protected abstract int getExpectedControlSampleCount();
 
     private void loadSamples() throws ConnectionException, ExperimentNotFoundException, NoSamplesForExperimentException {
         if (getLoadSamples()) {
             logStart();
             GenomicDataSourceConfiguration genomicSource = new GenomicDataSourceConfiguration();
             genomicSource.getServerProfile().setHostname(getCaArrayHostname());
             genomicSource.getServerProfile().setPort(8080);
             genomicSource.getServerProfile().setUsername(getCaArrayUsername());
             genomicSource.getServerProfile().setPassword(getCaArrayPassword());
             genomicSource.setExperimentIdentifier(getCaArrayId());
             service.addGenomicSource(studyConfiguration, genomicSource);
             assertTrue(genomicSource.getSamples().size() > 0);
             logEnd();
         }
     }
 
     protected String getCaArrayHostname() {
         return "array.nci.nih.gov";
     }
 
     abstract protected String getCaArrayId();
 
     private void mapSamples() throws ValidationException, IOException {
         if (getLoadSamples()) {
             logStart();
             service.mapSamples(studyConfiguration, getSampleMappingFile());
             assertEquals(getExpectedMappedSampleCount(), studyConfiguration.getGenomicDataSources().get(0).getMappedSamples().size());
             logEnd();
         }
     }
 
     private void loadControlSamples() throws ValidationException, IOException {
         if (getLoadSamples() && getControlSamplesFile() != null) {
             logStart();
             service.addControlSamples(studyConfiguration, getControlSamplesFile());
             assertEquals(getExpectedControlSampleCount(), studyConfiguration.getStudy().getControlSampleCollection().size());
             logEnd();
         }
     }
 
     abstract protected File getSampleMappingFile();
 
     abstract protected File getControlSamplesFile();
     
     private void deploy() throws ConnectionException, DataRetrievalException {
         logStart();
         service.deployStudy(studyConfiguration);
         logEnd();
     }
 
     private void loadClinicalData() throws IOException, ValidationException {
         logStart();
         sourceConfiguration = 
             service.addClinicalAnnotationFile(studyConfiguration, getSubjectAnnotationFile(), 
                     getSubjectAnnotationFile().getName());
         sourceConfiguration.getAnnotationFile().setIdentifierColumnIndex(0);
         assertTrue(sourceConfiguration.isLoadable());
         service.loadClinicalAnnotation(studyConfiguration, sourceConfiguration);
         assertTrue(sourceConfiguration.isCurrentlyLoaded());
         logEnd();
     }
 
     abstract protected File getSubjectAnnotationFile();
 
     private void loadAnnotationDefinitions() throws IOException {
         CSVReader reader = new CSVReader(new FileReader(getAnnotationDefinitionsFile()));
         String[] fields;
         while ((fields = reader.readNext()) != null) {
             AnnotationDefinition definition = new AnnotationDefinition();
             definition.setDisplayName(fields[0]);
             definition.setKeywords(definition.getDisplayName());
             definition.setType(AnnotationTypeEnum.getByValue(fields[1]).getValue());
             if (!StringUtils.isBlank(fields[2])) {
                 Collection<AbstractPermissibleValue> permissibleValues = new HashSet<AbstractPermissibleValue>();
                 String[] values = fields[2].split(";");
                 for (String value : values) {
                     StringPermissibleValue permissibleValue = new StringPermissibleValue();
                     permissibleValue.setStringValue(value);
                     permissibleValues.add(permissibleValue);
                     dao.save(permissibleValue);
                 }
                 definition.setPermissibleValueCollection(permissibleValues);
             }
             dao.save(definition);
         }
     }
     
     private void loadSurvivalValueDefinition() {
         SurvivalValueDefinition definition = service.createNewSurvivalValueDefinition(studyConfiguration.getStudy());
         definition.setSurvivalStartDate(dao.getAnnotationDefinition(getSurvivalStartDateName()));
         definition.setDeathDate(dao.getAnnotationDefinition(getDeathDateName()));
         definition.setLastFollowupDate(dao.getAnnotationDefinition(getLastFollowupDateName()));
         definition.setName("Survival From Start Date");
         dao.save(definition);
     }
     
     abstract protected String getSurvivalStartDateName();
     abstract protected String getDeathDateName();
     abstract protected String getLastFollowupDateName();
 
     abstract protected File getAnnotationDefinitionsFile();
     
     public CaIntegrator2Dao getCaIntegrator2Dao() {
         return dao;
     }
 
     public void setCaIntegrator2Dao(CaIntegrator2Dao caIntegrator2Dao) {
         this.dao = caIntegrator2Dao;
     }
 
     /**
      * @return the arrayDataService
      */
     public ArrayDataService getArrayDataService() {
         return arrayDataService;
     }
 
     /**
      * @param arrayDataService the arrayDataService to set
      */
     public void setArrayDataService(ArrayDataService arrayDataService) {
         this.arrayDataService = arrayDataService;
     }
 
     private void checkQueries() {
         checkClinicalQuery();
         checkGenomicQuery();
     }
 
     private void checkClinicalQuery() {
         logStart();
         Query query = createQuery();
         query.setResultType(ResultTypeEnum.CLINICAL);
         logEnd();
     }
 
     private void checkGenomicQuery() {
         if (getLoadSamples() && getLoadDesign()) {
             logStart();
             Query query = createQuery();
             query.setResultType(ResultTypeEnum.GENOMIC);
             query.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
             GeneNameCriterion geneCriterion = new GeneNameCriterion();
             geneCriterion.setGeneSymbol("EGFR");
             query.getCompoundCriterion().getCriterionCollection().add(geneCriterion);
             
             GenomicDataQueryResult result = queryManagementService.executeGenomicDataQuery(query);
             assertFalse(result.getColumnCollection().isEmpty());
             assertFalse(result.getRowCollection().isEmpty());
             logEnd();
         }
     }
 
     abstract protected Logger getLogger();
 
     private Query createQuery() {
         Query query = new Query();
         query.setColumnCollection(new HashSet<ResultColumn>());
         query.setCompoundCriterion(new CompoundCriterion());
         query.getCompoundCriterion().setBooleanOperator(BooleanOperatorEnum.AND);
         query.getCompoundCriterion().setCriterionCollection(new HashSet<AbstractCriterion>());
         query.setSubscription(new StudySubscription());
         query.getSubscription().setStudy(studyConfiguration.getStudy());
         return query;
     }
 
     /**
      * @return the queryManagementService
      */
     public QueryManagementService getQueryManagementService() {
         return queryManagementService;
     }
 
     /**
      * @param queryManagementService the queryManagementService to set
      */
     public void setQueryManagementService(QueryManagementService queryManagementService) {
         this.queryManagementService = queryManagementService;
     }
 
     protected String getCaArrayUsername() {
         return null;
     }
 
     protected String getCaArrayPassword() {
         return null;
     }
 
    /**
     * @return the fileManager
     */
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * @param fileManager the fileManager to set
     */
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

 }
