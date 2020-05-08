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
 package gov.nih.nci.caintegrator2.application.analysis;
 
 import edu.mit.broad.genepattern.gp.services.CaIntegrator2GPClient;
 import gov.nih.nci.caintegrator2.application.CaIntegrator2BaseService;
 import gov.nih.nci.caintegrator2.application.analysis.geneexpression.AbstractGEPlotHandler;
 import gov.nih.nci.caintegrator2.application.analysis.geneexpression.AbstractGEPlotParameters;
 import gov.nih.nci.caintegrator2.application.analysis.geneexpression.ControlSamplesNotMappedException;
 import gov.nih.nci.caintegrator2.application.analysis.geneexpression.GenesNotFoundInStudyException;
 import gov.nih.nci.caintegrator2.application.analysis.grid.GenePatternGridRunner;
 import gov.nih.nci.caintegrator2.application.analysis.grid.gistic.GisticParameters;
 import gov.nih.nci.caintegrator2.application.analysis.grid.gistic.GisticSamplesMarkers;
 import gov.nih.nci.caintegrator2.application.analysis.grid.pca.PCAParameters;
 import gov.nih.nci.caintegrator2.application.analysis.grid.preprocess.PreprocessDatasetParameters;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapFileTypeEnum;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapParameters;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapResult;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVFileTypeEnum;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVParameters;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVResult;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValueType;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.application.geneexpression.GeneExpressionPlotGroup;
 import gov.nih.nci.caintegrator2.application.geneexpression.GeneExpressionPlotService;
 import gov.nih.nci.caintegrator2.application.kmplot.KMPlot;
 import gov.nih.nci.caintegrator2.application.kmplot.KMPlotService;
 import gov.nih.nci.caintegrator2.application.query.InvalidCriterionException;
 import gov.nih.nci.caintegrator2.application.query.QueryManagementService;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.common.ConfigurationParameter;
 import gov.nih.nci.caintegrator2.common.GenePatternUtil;
 import gov.nih.nci.caintegrator2.common.HibernateUtil;
 import gov.nih.nci.caintegrator2.domain.analysis.GisticAnalysis;
 import gov.nih.nci.caintegrator2.domain.analysis.GisticResultZipFileParser;
 import gov.nih.nci.caintegrator2.domain.application.AbstractPersistedAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.AnalysisJobStatusEnum;
 import gov.nih.nci.caintegrator2.domain.application.ComparativeMarkerSelectionAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.CopyNumberCriterionTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.GisticAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.PrincipalComponentAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Array;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayDataType;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneLocationConfiguration;
 import gov.nih.nci.caintegrator2.domain.genomic.GenomeBuildVersionEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.GisticGenomicRegionReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleSet;
 import gov.nih.nci.caintegrator2.domain.genomic.SegmentData;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.DataRetrievalException;
 import gov.nih.nci.caintegrator2.external.ParameterException;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 import gov.nih.nci.caintegrator2.file.AnalysisFileManager;
 import gov.nih.nci.caintegrator2.file.FileManager;
 import gov.nih.nci.caintegrator2.file.FileManagerImpl;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.genepattern.webservice.WebServiceException;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Implementation of the AnalysisService subsystem.
  */
 @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
 public class AnalysisServiceImpl extends CaIntegrator2BaseService implements AnalysisService {
     private KMPlotService kmPlotService;
     private GeneExpressionPlotService gePlotService;
     private QueryManagementService queryManagementService;
     private GenePatternClientFactory genePatternClientFactory;
     private GenePatternGridRunner genePatternGridRunner;
     private FileManager fileManager;
     private AnalysisFileManager analysisFileManager;
     private ArrayDataService arrayDataService;
     private SessionAnalysisResultsManager sessionAnalysisResultsManager;
     private CBSToHeatmapFactory cbsToHeatmapFactory;
     /**
      * {@inheritDoc}
      */
     @Override
     public List<AnalysisMethod> getGenePatternMethods(ServerConnectionProfile server)
     throws WebServiceException {
         return new GenePatternHelper(retrieveClient(server)).getMethods();
     }
 
     private CaIntegrator2GPClient retrieveClient(ServerConnectionProfile server) throws WebServiceException {
         return genePatternClientFactory.retrieveOldGenePatternClient(server);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public JobInfoWrapper executeGenePatternJob(ServerConnectionProfile server, AnalysisMethodInvocation invocation)
     throws WebServiceException {
         JobInfoWrapper jobInfo = new GenePatternHelper(retrieveClient(server)).execute(invocation);
         URL serviceUrl;
         URL resultUrl;
         try {
             serviceUrl = new URL(server.getUrl());
             resultUrl = new URL(serviceUrl.getProtocol(), serviceUrl.getHost(), serviceUrl.getPort(),
                     "/gp/pages/jobResults.jsf");
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("Invalid URL provided for server " + server.getUrl(), e);
         }
         jobInfo.setUrl(resultUrl);
         return jobInfo;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public File executeGridPreprocessComparativeMarker(StatusUpdateListener updater,
             ComparativeMarkerSelectionAnalysisJob job)
     throws ConnectionException, InvalidCriterionException {
         StudySubscription studySubscription = getDao().get(job.getSubscription().getId(), StudySubscription.class);
         PreprocessDatasetParameters preprocessParams = job.getForm().getPreprocessDatasetparameters();
         job.setSubscription(studySubscription);
         GctDataset gctDataset = createGctDataset(studySubscription, preprocessParams.getClinicalQueries(),
                 preprocessParams.getPlatformName(), preprocessParams.getExcludedControlSampleSet(), true);
         File gctFile = createGctFile(gctDataset, studySubscription,
                 preprocessParams.getProcessedGctFilename());
         File clsFile = createClassificationFile(studySubscription,
                 job.getForm().getComparativeMarkerSelectionParameters().getClinicalQueries(),
                 job.getForm().getComparativeMarkerSelectionParameters().getClassificationFileName(),
                 gctDataset.getColumnSampleNames());
         job.setInputZipFile(fileManager.createInputZipFile(studySubscription, job,
                 "CMS_INPUT_" +  System.currentTimeMillis() + ".zip",
                 gctFile, clsFile));
         checkForMissingSubjects(job, gctDataset.getSubjectsNotFoundFromQueries());
         return genePatternGridRunner.runPreprocessComparativeMarkerSelection(updater, job, gctFile, clsFile);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public File executeGridGistic(StatusUpdateListener updater, GisticAnalysisJob job)
     throws ConnectionException, InvalidCriterionException, ParameterException, IOException,
             DataRetrievalException {
         File resultsZipFile = null;
         StudySubscription studySubscription = getDao().get(job.getSubscription().getId(), StudySubscription.class);
         job.setSubscription(studySubscription);
         GisticSamplesMarkers gisticSamplesMarkers =
             GenePatternUtil.createGisticSamplesMarkers(queryManagementService,
                     job.getGisticAnalysisForm().getGisticParameters(), job.getSubscription());
         checkForMissingSubjects(job, queryManagementService.getAllSubjectsNotFoundInCriteria(job
                 .getGisticAnalysisForm().getGisticParameters().getClinicalQuery()));
         resultsZipFile = runGistic(updater, job, studySubscription, gisticSamplesMarkers);
         if (!job.getStatus().isErrorState()) {
             GisticAnalysis gisticAnalysis = createGisticAnalysis(job, gisticSamplesMarkers.getUsedSamples());
             Map<String, Map<GisticGenomicRegionReporter, Float>> gisticData = parseGisticResults(
                     gisticAnalysis.getReporterList(), resultsZipFile);
             getDao().save(studySubscription);
             createArrayData(studySubscription.getStudy(), gisticAnalysis, gisticData);
             job.setStatus(AnalysisJobStatusEnum.COMPLETED);
         }
         return resultsZipFile;
     }
 
     private File runGistic(StatusUpdateListener updater, GisticAnalysisJob job, StudySubscription studySubscription,
             GisticSamplesMarkers gisticSamplesMarkers) throws IOException, ConnectionException,
             InvalidCriterionException, ParameterException {
         File resultsZipFile;
         List<File> inputFiles = new ArrayList<File>();
         File segmentFile = fileManager.createSamplesFile(studySubscription, gisticSamplesMarkers.getSamples());
         File markersFile = fileManager.createMarkersFile(studySubscription, gisticSamplesMarkers.getMarkers());
         File cnvFile = job.getGisticAnalysisForm().getGisticParameters().getCnvSegmentsToIgnoreFile();
         if (cnvFile != null) {
             File newCnvFile = fileManager.renameCnvFile(cnvFile);
             inputFiles.add(newCnvFile);
         }
         inputFiles.add(segmentFile);
         inputFiles.add(markersFile);
         job.setInputZipFile(fileManager.createInputZipFile(studySubscription, job,
                 "GISTIC_INPUT_" +  System.currentTimeMillis() + ".zip",
                 inputFiles.toArray(new File[inputFiles.size()])));
         if (job.isGridServiceCall()) {
             resultsZipFile = genePatternGridRunner.runGistic(updater, job, segmentFile, markersFile, cnvFile);
         } else {
             resultsZipFile = runGisticWebService(job, updater, segmentFile, markersFile);
         }
         return resultsZipFile;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean validateGenePatternConnection(ServerConnectionProfile server) {
         try {
             return retrieveClient(server).validateConnection();
         } catch (Exception e) {
             return false;
         }
     }
 
     private Map<String, Map<GisticGenomicRegionReporter, Float>> parseGisticResults(
             ReporterList reporterList, File resultsZipFile) throws DataRetrievalException {
         if (resultsZipFile != null) {
             return new GisticResultZipFileParser(reporterList, getDao()).parse(resultsZipFile);
         }
         return null;
     }
 
     private void createArrayData(Study study, GisticAnalysis gisticAnalysis,
             Map<String, Map<GisticGenomicRegionReporter, Float>> gisticData) {
         if (gisticData != null) {
             for (String sampleName : gisticData.keySet()) {
                 ArrayData arrayData = createArrayData(study, gisticAnalysis.getSample(sampleName), gisticAnalysis
                         .getReporterList());
                 getDao().save(arrayData);
                 List<AbstractReporter> regionReporters = new ArrayList<AbstractReporter>();
                 for (GisticGenomicRegionReporter reporter : gisticData.get(sampleName).keySet()) {
                     regionReporters.add(reporter);
                 }
                 ArrayDataValues values = new ArrayDataValues(regionReporters);
                 for (GisticGenomicRegionReporter regionReporter : gisticData.get(sampleName).keySet()) {
                     values.setFloatValue(arrayData, regionReporter, ArrayDataValueType.DNA_ANALYSIS_LOG2_RATIO,
                             gisticData.get(sampleName).get(regionReporter));
                 }
                 getArrayDataService().save(values);
             }
         }
     }
 
     private ArrayData createArrayData(Study study, Sample sample, ReporterList reporterList) {
         ArrayData arrayData = new ArrayData();
         arrayData.setType(ArrayDataType.GISTIC_ANALYSIS);
         arrayData.setSample(sample);
         sample.getArrayDataCollection().add(arrayData);
         arrayData.setStudy(study);
         Array array = new Array();
         array.getArrayDataCollection().add(arrayData);
         arrayData.setArray(array);
         array.getSampleCollection().add(sample);
         sample.getArrayCollection().add(array);
         arrayData.getReporterLists().add(reporterList);
         reporterList.getArrayDatas().add(arrayData);
         return arrayData;
     }
 
 
     private GisticAnalysis createGisticAnalysis(GisticAnalysisJob job, Set<Sample> samplesUsed) {
         GisticAnalysis gisticAnalysis = new GisticAnalysis();
         StudySubscription subscription = job.getSubscription();
         GisticParameters parameters = job.getGisticAnalysisForm().getGisticParameters();
         gisticAnalysis.setAmplificationsThreshold(parameters.getAmplificationsThreshold());
         gisticAnalysis.setDeletionsThreshold(parameters.getDeletionsThreshold());
         gisticAnalysis.setGenomeBuildInformation(parameters.getRefgeneFile().getParameterValue());
         gisticAnalysis.setJoinSegmentSize(parameters.getJoinSegmentSize());
         gisticAnalysis.setName(job.getName());
         if (parameters.getClinicalQuery() != null) {
             gisticAnalysis.setQueryOrListName(parameters.getClinicalQuery().getName());
         }
         gisticAnalysis.setQvThreshold(parameters.getQvThresh());
         gisticAnalysis.setUrl(parameters.getServer().getUrl());
         gisticAnalysis.getSamplesUsedForCalculation().addAll(samplesUsed);
         subscription.getCopyNumberAnalysisCollection().add(gisticAnalysis);
 
         ReporterList reporterList = new ReporterList("GISTIC result", ReporterTypeEnum.GISTIC_GENOMIC_REGION_REPORTER);
         gisticAnalysis.setReporterList(reporterList);
         gisticAnalysis.setCreationDate(job.getCreationDate());
         return gisticAnalysis;
     }
 
     private File runGisticWebService(GisticAnalysisJob job, StatusUpdateListener updater,
             File segmentFile, File markersFile)
     throws ConnectionException, InvalidCriterionException, IOException {
         ServerConnectionProfile server = job.getGisticAnalysisForm().getGisticParameters().getServer();
         GisticWebServiceRunner gisticWebServiceRunner;
         try {
             gisticWebServiceRunner = new GisticWebServiceRunner(retrieveClient(server), getFileManager());
             return gisticWebServiceRunner.runGistic(updater, job, segmentFile, markersFile);
         } catch (WebServiceException e) {
             throw new ConnectionException("Unable to connect to GenePattern.", e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public File executeGridPCA(StatusUpdateListener updater,
             PrincipalComponentAnalysisJob job) throws ConnectionException, InvalidCriterionException {
         StudySubscription studySubscription = getDao().get(job.getSubscription().getId(), StudySubscription.class);
         job.setSubscription(studySubscription);
         PCAParameters parameters = job.getForm().getPcaParameters();
         GctDataset gctDataset = createGctDataset(studySubscription, parameters.getClinicalQueries(),
                 parameters.getPlatformName(), parameters.getExcludedControlSampleSet(), true);
         File gctFile = createGctFile(gctDataset, studySubscription, parameters.getGctFileName());
         checkForMissingSubjects(job, gctDataset.getSubjectsNotFoundFromQueries());
         job.setInputZipFile(fileManager.createInputZipFile(studySubscription, job,
                 "PCA_INPUT_" +  System.currentTimeMillis() + ".zip",
                 gctFile));
         return genePatternGridRunner.runPCA(updater, job, gctFile);
     }
 
     private void checkForMissingSubjects(AbstractPersistedAnalysisJob job, Set<String> subjectsNotFound) {
         if (!subjectsNotFound.isEmpty()) {
             job.setStatusDescription("Subjects not found in queries/subject lists: "
                     + StringUtils.join(subjectsNotFound, ","));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public KMPlot createKMPlot(StudySubscription subscription, AbstractKMParameters kmParameters)
         throws InvalidCriterionException, GenesNotFoundInStudyException, InvalidSurvivalValueDefinitionException {
         AbstractKMPlotHandler kmPlotHandler = AbstractKMPlotHandler.createKMPlotHandler(
             subscription, getDao(), kmParameters.getSurvivalValueDefinition(), queryManagementService, kmParameters);
         kmPlotHandler.setupAndValidateParameters(this);
         return kmPlotHandler.createPlot(kmPlotService);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public GeneExpressionPlotGroup createGeneExpressionPlot(StudySubscription studySubscription,
             AbstractGEPlotParameters plotParameters)
     throws ControlSamplesNotMappedException, InvalidCriterionException, GenesNotFoundInStudyException {
         if (StringUtils.isNotBlank(plotParameters.getGeneSymbol())) {
             plotParameters.getGenesNotFound().clear();
             plotParameters.getGenesNotFound().addAll(validateGeneSymbols(studySubscription,
                     Cai2Util.createListFromCommaDelimitedString(plotParameters.getGeneSymbol())));
         }
         AbstractGEPlotHandler gePlotHandler = AbstractGEPlotHandler.createGeneExpressionPlotHandler(
                 getDao(), queryManagementService, plotParameters, gePlotService);
 
         return gePlotHandler.createPlots(studySubscription);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String executeIGV(IGVParameters igvParameters)
     throws InvalidCriterionException {
         IGVResult igvResult = new IGVResult();
         StudySubscription studySubscription = getRefreshedEntity(igvParameters.getStudySubscription());
         igvParameters.setStudySubscription(studySubscription);
         igvParameters.getQuery().setSubscription(studySubscription);
         if (igvParameters.isViewAllData()) {
             runIGVForAllData(igvParameters, igvResult);
         } else {
            igvResult = runIGVForQueryResult(igvParameters);
         }
         if (igvResult.getGeneExpressionFile() == null && igvResult.getSegmentationFile() == null) {
             throw new InvalidCriterionException("Unable to create IGV viewer: No data found from selection.");
         }
         CopyNumberCriterionTypeEnum copyNumberSubType = retrieveCopyNumberSubtypeForIGV(igvParameters, igvResult);
         igvResult.setSampleInfoFile(analysisFileManager.createIGVSampleClassificationFile(
                 createAnnotationBasedQueryResultsForSamples(igvParameters),
                 igvParameters.getSessionId(), igvParameters.getQuery().getColumnCollection(), copyNumberSubType));
         analysisFileManager.createIGVSessionFile(igvParameters, igvResult);
         sessionAnalysisResultsManager.storeJobResult(igvParameters.getSessionId(), igvResult);
         FileManager thisFileMgrImpl = analysisFileManager.getFileManager();
         String retStr = ((FileManagerImpl) thisFileMgrImpl).getConfigurationHelper()
                             .getString(ConfigurationParameter.BROAD_HOSTED_IGV_URL);
         retStr = retStr.concat(encodeUrl(igvParameters.getUrlPrefix()));
         retStr = retStr.concat(IGVFileTypeEnum.SESSION.getFilename());
         return retStr;
     }
 
     private CopyNumberCriterionTypeEnum retrieveCopyNumberSubtypeForIGV(
             IGVParameters igvParameters, IGVResult igvResult) {
         CopyNumberCriterionTypeEnum copyNumberSubType = null;
         if (igvResult.getSegmentationFile() != null) {
             copyNumberSubType = igvParameters.isUseCGHCall()
                     ? CopyNumberCriterionTypeEnum.CALLS_VALUE : CopyNumberCriterionTypeEnum.SEGMENT_VALUE;
         }
         return copyNumberSubType;
     }
 
     private void runIGVForAllData(IGVParameters igvParameters, IGVResult igvResult) {
         List<Platform> refreshedPlatforms = new ArrayList<Platform>();
         for (Platform platform : igvParameters.getPlatforms()) {
             if (platform.getPlatformConfiguration().getPlatformType().isGeneExpression()) {
                 igvResult.setGeneExpressionFile(
                     createGeneExpressionFile(igvParameters.getStudySubscription(), platform));
             } else if (platform.getPlatformConfiguration().getPlatformType().isCopyNumber()) {
                 igvResult.setSegmentationFile(
                     createIGVSegmentationFile(igvParameters.getStudySubscription(), platform,
                             igvParameters.isUseCGHCall()));
             }
             refreshedPlatforms.add(getRefreshedEntity(platform));
         }
         igvParameters.getPlatforms().clear();
         igvParameters.getPlatforms().addAll(refreshedPlatforms);
     }
 
     private IGVResult runIGVForQueryResult(IGVParameters igvParameters)
         throws InvalidCriterionException {
         IGVResult igvResult = new IGVResult();
         Set<Query> queries = new HashSet<Query>();
         queries.add(igvParameters.getQuery());
         Query queryToExecute = queryManagementService.retrieveQueryToExecute(igvParameters.getQuery());
         if (igvParameters.getStudySubscription().getStudy().getStudyConfiguration().hasExpressionData()) {
             try {
                String genePlatformName = getGenePlatformName(queryToExecute);
                 igvResult.setGeneExpressionFile(analysisFileManager.createIGVGctFile(
                    createGctDataset(igvParameters.getStudySubscription(), queries, genePlatformName, null, false),
                     igvParameters.getSessionId()));
                 igvParameters.addPlatform(queryToExecute.getGeneExpressionPlatform());
             } catch (InvalidCriterionException e) {
                 igvResult.setGeneExpressionFile(null);
             }
         }
         if (igvParameters.getStudySubscription().getStudy().getStudyConfiguration().hasCopyNumberData()) {
             try {
                 igvResult.setSegmentationFile(analysisFileManager.createIGVSegFile(
                     createSegmentDataset(igvParameters.getStudySubscription(), queries, null, null),
                     igvParameters.getSessionId(), igvParameters.isUseCGHCall()));
                 igvParameters.addPlatform(queryToExecute.getCopyNumberPlatform());
             } catch (InvalidCriterionException e) {
                 igvResult.setSegmentationFile(null);
             }
         }
         return igvResult;
     }
 
    private String getGenePlatformName(Query query) {
        return query.getGeneExpressionPlatform() != null ? query.getGeneExpressionPlatform().getName() : null;
    }

     /**
      * {@inheritDoc}
      */
     @Override
     public String executeHeatmap(HeatmapParameters heatmapParameters)
     throws InvalidCriterionException {
         HeatmapResult heatmapResult = new HeatmapResult();
         if (heatmapParameters.isViewAllData()) {
             generateHeatmapGenomicFileAllData(heatmapParameters, heatmapResult);
         } else {
             generateHeatmapGenomicFileForQuery(heatmapParameters, heatmapResult);
         }
         if (heatmapResult.getGenomicDataFile() == null) {
             throw new InvalidCriterionException("Unable to create Heat Map viewer: No data found from selection.");
         }
         if (!heatmapParameters.getQuery().getColumnCollection().isEmpty()) {
             heatmapResult.setSampleAnnotationFile(analysisFileManager.createHeatmapSampleClassificationFile(
                 createAnnotationBasedQueryResultsForSamples(heatmapParameters),
                 heatmapParameters.getSessionId(), heatmapParameters.getQuery().getColumnCollection()));
         }
         analysisFileManager.createHeatmapJnlpFile(heatmapParameters, heatmapResult);
         sessionAnalysisResultsManager.storeJobResult(heatmapParameters.getSessionId(), heatmapResult);
         return heatmapParameters.getUrlPrefix() + HeatmapFileTypeEnum.LAUNCH_FILE.getFilename();
     }
 
     private void generateHeatmapGenomicFileForQuery(HeatmapParameters heatmapParameters, HeatmapResult heatmapResult)
     throws InvalidCriterionException {
         heatmapParameters.getQuery().setResultType(ResultTypeEnum.COPY_NUMBER);
         Set<Query> queries = new HashSet<Query>();
         queries.add(heatmapParameters.getQuery());
         Query queryToExecute = queryManagementService.retrieveQueryToExecute(heatmapParameters.getQuery());
         Platform platform = queryToExecute.getCopyNumberPlatform();
         createHeatmapDataFiles(heatmapParameters, heatmapResult, queries, platform);
     }
 
     private void generateHeatmapGenomicFileAllData(HeatmapParameters heatmapParameters, HeatmapResult heatmapResult) {
         Platform platform = heatmapParameters.getPlatform();
         StudySubscription studySubscription = heatmapParameters.getStudySubscription();
         File genomicDataFile = analysisFileManager.retrieveHeatmapFile(studySubscription.getStudy(),
                 heatmapParameters.isUseCGHCall() ? HeatmapFileTypeEnum.CALLS_DATA
                     : HeatmapFileTypeEnum.GENOMIC_DATA, platform.getName());
         File layoutFile = analysisFileManager.retrieveHeatmapFile(studySubscription.getStudy(),
                 HeatmapFileTypeEnum.LAYOUT, platform.getName());
         if (!genomicDataFile.exists() || !layoutFile.exists()) {
             createHeatmapDataFiles(heatmapParameters, heatmapResult, new HashSet<Query>(), platform);
         } else {
             heatmapResult.setGenomicDataFile(genomicDataFile);
             heatmapResult.setLayoutFile(layoutFile);
         }
     }
 
     private void createHeatmapDataFiles(HeatmapParameters heatmapParameters, HeatmapResult heatmapResult,
             Set<Query> queries, Platform platform) {
         String platformName = queries.isEmpty() ? platform.getName() : null;
         try {
             analysisFileManager.createHeatmapGenomicFile(heatmapParameters, heatmapResult,
                     createSegmentDataset(heatmapParameters.getStudySubscription(), queries,
                             platformName, null), getGeneLocationsForPlatform(platform), cbsToHeatmapFactory);
         } catch (Exception e) {
             heatmapResult.setGenomicDataFile(null);
             heatmapResult.setLayoutFile(null);
         }
     }
 
     private GeneLocationConfiguration getGeneLocationsForPlatform(Platform platform) {
             GenomeBuildVersionEnum genomeBuildVersion = platform.getGenomeVersion();
             if (genomeBuildVersion == null || !getDao().isGenomeVersionMapped(genomeBuildVersion)) {
                 genomeBuildVersion = GenomeBuildVersionEnum.HG18; // Default build.
             }
             return getDao().getGeneLocationConfiguration(genomeBuildVersion);
         }
 
     private File createGeneExpressionFile(StudySubscription studySubscription, Platform platform) {
         File gctFile = null;
         gctFile = analysisFileManager.retrieveIGVFile(studySubscription.getStudy(),
                 IGVFileTypeEnum.GENE_EXPRESSION, platform.getName());
         if (!gctFile.exists()) {
             try {
             gctFile = analysisFileManager.createIGVGctFile(
                 createGctDataset(studySubscription, new HashSet<Query>(),
                         platform.getName(), null, false),
                 studySubscription.getStudy(), platform.getName());
             } catch (InvalidCriterionException e) {
                 return null;
             }
         }
         return gctFile;
     }
 
     private File createIGVSegmentationFile(StudySubscription studySubscription, Platform platform,
             boolean isUseCGHCall) {
         File segFile = null;
         segFile = analysisFileManager.retrieveIGVFile(
                 studySubscription.getStudy(),
                 isUseCGHCall ? IGVFileTypeEnum.SEGMENTATION_CALLS : IGVFileTypeEnum.SEGMENTATION,
                 platform.getName());
         if (!segFile.exists()) {
             try {
                 segFile = analysisFileManager.createIGVSegFile(
                     createSegmentDataset(studySubscription, new HashSet<Query>(),
                             platform.getName(), null),
                     studySubscription.getStudy(), platform.getName(), isUseCGHCall);
             } catch (InvalidCriterionException e) {
                 return null;
             }
         }
         return segFile;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void createViewerFiles(StudySubscription studySubscription, HeatmapParameters heatmapParameters,
             Platform platform)
     throws InvalidCriterionException, IOException {
         if (platform.getPlatformConfiguration().getPlatformType().isGeneExpression()) {
             createGeneExpressionFile(studySubscription, platform);
         }
         if (platform.getPlatformConfiguration().getPlatformType().isCopyNumber()) {
             createIGVSegmentationFile(studySubscription, platform, false);
             heatmapParameters.setPlatform(platform);
             generateHeatmapGenomicFileAllData(heatmapParameters, new HeatmapResult());
         }
     }
 
     private QueryResult createAnnotationBasedQueryResultsForSamples(AbstractViewerParameters parameters)
     throws InvalidCriterionException {
         ResultColumn sampleColumn = new ResultColumn();
         sampleColumn.setEntityType(EntityTypeEnum.SAMPLE);
         sampleColumn.setColumnIndex(1);
         parameters.getQuery().getColumnCollection().add(sampleColumn);
         ResultTypeEnum resultType = parameters.getQuery().getResultType();
         parameters.getQuery().setResultType(ResultTypeEnum.CLINICAL);
         QueryResult result;
         if (parameters.isViewAllData()) {
             result = queryManagementService.execute(parameters.getQuery());
         } else {
             result = queryManagementService.execute(parameters.getQuery());
         }
         parameters.getQuery().setResultType(resultType);
         parameters.getQuery().getColumnCollection().remove(sampleColumn);
         return result;
     }
 
     private String encodeUrl(String url) {
         String encodedUrl = url.replaceAll("\\?", "%3F");
         encodedUrl = encodedUrl.replaceAll("=", "%3D");
         encodedUrl = encodedUrl.replaceAll("&", "%26");
         return encodedUrl;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<String> validateGeneSymbols(StudySubscription studySubscription, List<String> geneSymbols)
     throws GenesNotFoundInStudyException {
         return queryManagementService.validateGeneSymbols(studySubscription, geneSymbols);
     }
 
     private File createClassificationFile(StudySubscription studySubscription, List<Query> clinicalQueries,
             String clsFilename, List<String> sampleColumnOrdering) throws InvalidCriterionException {
         return fileManager.createClassificationFile(studySubscription,
                 GenePatternUtil.createSampleClassification(
                         queryManagementService, clinicalQueries, sampleColumnOrdering),
                 clsFilename);
     }
 
     private File createGctFile(GctDataset gctDataset, StudySubscription studySubscription, String filename)
     throws InvalidCriterionException {
         return fileManager.createGctFile(studySubscription, gctDataset, filename);
     }
 
     private GctDataset createGctDataset(StudySubscription studySubscription, Collection<Query> querySet,
             String platformName, SampleSet excludedSet, boolean addGenesToReporters) throws InvalidCriterionException {
         SampleSet refreshedExcludedSet = excludedSet == null ? null : getRefreshedEntity(excludedSet);
         return GenePatternUtil.createGctDataset(studySubscription, querySet,
                 refreshedExcludedSet, queryManagementService, platformName, addGenesToReporters);
     }
 
     private Collection<SegmentData> createSegmentDataset(StudySubscription studySubscription,
             Collection<Query> querySet, String platformName, SampleSet excludedSet) throws InvalidCriterionException {
         SampleSet refreshedExcludedSet = excludedSet == null ? null : getRefreshedEntity(excludedSet);
         return GenePatternUtil.createSegmentDataset(studySubscription, querySet,
                 refreshedExcludedSet, queryManagementService, platformName);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public StudySubscription getRefreshedStudySubscription(StudySubscription studySubscription) {
         StudySubscription refreshedStudySubscription = getRefreshedEntity(studySubscription);
         HibernateUtil.loadCollection(refreshedStudySubscription);
         return refreshedStudySubscription;
     }
 
     /**
      * @return the kmPlotService
      */
     public KMPlotService getKmPlotService() {
         return kmPlotService;
     }
 
     /**
      * @param kmPlotService the kmPlotService to set
      */
     public void setKmPlotService(KMPlotService kmPlotService) {
         this.kmPlotService = kmPlotService;
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
 
     /**
      * @return the gePlotService
      */
     public GeneExpressionPlotService getGePlotService() {
         return gePlotService;
     }
 
     /**
      * @param gePlotService the gePlotService to set
      */
     public void setGePlotService(GeneExpressionPlotService gePlotService) {
         this.gePlotService = gePlotService;
     }
 
     /**
      * @return the genePatternClientFactory
      */
     public GenePatternClientFactory getGenePatternClientFactory() {
         return genePatternClientFactory;
     }
 
     /**
      * @param genePatternClientFactory the genePatternClientFactory to set
      */
     public void setGenePatternClientFactory(GenePatternClientFactory genePatternClientFactory) {
         this.genePatternClientFactory = genePatternClientFactory;
     }
 
     /**
      * @return the genePatternGridRunner
      */
     public GenePatternGridRunner getGenePatternGridRunner() {
         return genePatternGridRunner;
     }
 
     /**
      * @param genePatternGridRunner the genePatternGridRunner to set
      */
     public void setGenePatternGridRunner(GenePatternGridRunner genePatternGridRunner) {
         this.genePatternGridRunner = genePatternGridRunner;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void deleteAnalysisJob(Long jobId) {
         AbstractPersistedAnalysisJob job = getAnalysisJob(jobId);
         job.getSubscription().getAnalysisJobCollection().remove(job);
         deleteFiles(job);
         getDao().delete(job);
     }
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteViewerDirectory(Study study) {
         analysisFileManager.deleteViewerDirectory(study);
     }
 
     private void deleteFiles(AbstractPersistedAnalysisJob job) {
         if (job.getResultsZipFile() != null) {
             FileUtils.deleteQuietly(job.getResultsZipFile().getFile());
         }
         if (job.getInputZipFile() != null) {
             FileUtils.deleteQuietly(job.getInputZipFile().getFile());
         }
     }
 
     private AbstractPersistedAnalysisJob getAnalysisJob(Long id) {
         return getDao().get(id, AbstractPersistedAnalysisJob.class);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void deleteGisticAnalysis(GisticAnalysis gisticAnalysis) {
         StudySubscription studySubscription = gisticAnalysis.getStudySubscription();
         if (gisticAnalysis.getReporterList() != null) {
             getArrayDataService().deleteGisticAnalysisNetCDFFile(studySubscription.getStudy(),
                     gisticAnalysis.getReporterList().getId());
             for (ArrayData arrayData : gisticAnalysis.getReporterList().getArrayDatas()) {
                 for (Sample sample : arrayData.getArray().getSampleCollection()) {
                     sample.getArrayCollection().remove(arrayData.getArray());
                     sample.getArrayDataCollection().remove(arrayData);
                 }
                 getDao().delete(arrayData.getArray());
             }
         }
         studySubscription.getCopyNumberAnalysisCollection().remove(gisticAnalysis);
         getDao().delete(gisticAnalysis);
         getDao().save(studySubscription);
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
 
     /**
      * @return the analysisFileManager
      */
     public AnalysisFileManager getAnalysisFileManager() {
         return analysisFileManager;
     }
 
     /**
      * @param analysisFileManager the analysisFileManager to set
      */
     public void setAnalysisFileManager(AnalysisFileManager analysisFileManager) {
         this.analysisFileManager = analysisFileManager;
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
 
     /**
      * @return the sessionAnalysisResultsManager
      */
     public SessionAnalysisResultsManager getSessionAnalysisResultsManager() {
         return sessionAnalysisResultsManager;
     }
 
     /**
      * @param sessionAnalysisResultsManager the sessionAnalysisResultsManager to set
      */
     public void setSessionAnalysisResultsManager(SessionAnalysisResultsManager sessionAnalysisResultsManager) {
         this.sessionAnalysisResultsManager = sessionAnalysisResultsManager;
     }
 
     /**
      * @return the cbsToHeatmapFactory
      */
     public CBSToHeatmapFactory getCbsToHeatmapFactory() {
         return cbsToHeatmapFactory;
     }
 
     /**
      * @param cbsToHeatmapFactory the cbsToHeatmapFactory to set
      */
     public void setCbsToHeatmapFactory(CBSToHeatmapFactory cbsToHeatmapFactory) {
         this.cbsToHeatmapFactory = cbsToHeatmapFactory;
     }
 }
