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
 package gov.nih.nci.caintegrator2.file;
 
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisViewerTypeEnum;
 import gov.nih.nci.caintegrator2.application.analysis.CBSToHeatmapFactory;
 import gov.nih.nci.caintegrator2.application.analysis.GctDataset;
 import gov.nih.nci.caintegrator2.application.analysis.GctDatasetFileWriter;
 import gov.nih.nci.caintegrator2.application.analysis.SegmentDatasetFileWriter;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapFileTypeEnum;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapGenomicDataFileWriter;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapJnlpFileWriter;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapParameters;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapResult;
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapSampleAnnotationsFileWriter;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVFileTypeEnum;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVParameters;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVResult;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVSampleInfoFileWriter;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVSessionFileWriter;
 import gov.nih.nci.caintegrator2.common.QueryUtil;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneLocationConfiguration;
 import gov.nih.nci.caintegrator2.domain.genomic.SegmentData;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * 
  */
 public class AnalysisFileManagerImpl implements AnalysisFileManager {
     private FileManager fileManager;
     
     /**
      * {@inheritDoc}
      */
     public File createIGVGctFile(GctDataset gctDataset, String sessionId) {
         return gctDataset == null ? null : GctDatasetFileWriter.writeAsGct(gctDataset, 
                 new File(getIGVDirectory(sessionId).getAbsolutePath()
                        + File.separator + IGVFileTypeEnum.GENE_EXPRESSION.getFilename()).getAbsolutePath());
     }
     
     /**
      * {@inheritDoc}
      */
     public File createIGVGctFile(GctDataset gctDataset, Study study, String platformName) {
         return gctDataset == null ? null : GctDatasetFileWriter.writeAsGct(gctDataset, 
                 retrieveIGVFile(study, IGVFileTypeEnum.GENE_EXPRESSION, platformName).getAbsolutePath());
     }
     
     /**
      * {@inheritDoc}
      */
     public File createIGVSampleClassificationFile(QueryResult queryResult, String sessionId, 
             Collection<ResultColumn> columns) {
         return new IGVSampleInfoFileWriter().writeSampleInfoFile(QueryUtil.retrieveSampleValuesMap(queryResult),
                 columns, new File(getIGVDirectory(sessionId).getAbsolutePath()
                         + File.separator + IGVFileTypeEnum.SAMPLE_CLASSIFICATION.getFilename()).getAbsolutePath());
     }
     
     /**
      * {@inheritDoc}
      */
     public File createHeatmapSampleClassificationFile(QueryResult queryResult, String sessionId, 
             Collection<ResultColumn> columns) {
         return new HeatmapSampleAnnotationsFileWriter().writeSampleInfoFile(QueryUtil.
             retrieveSampleValuesMap(queryResult), columns, new File(getHeatmapDirectory(sessionId).getAbsolutePath()
                 + File.separator + HeatmapFileTypeEnum.ANNOTATIONS.getFilename()).getAbsolutePath());
     }
     
     /**
      * {@inheritDoc}
      */
     public File createIGVSegFile(Collection<SegmentData> segmentDatas, String sessionId) {
         return segmentDatas.isEmpty() ? null : SegmentDatasetFileWriter.writeAsSegFile(segmentDatas,
                 new File(getIGVDirectory(sessionId).getAbsolutePath() + File.separator
                         + IGVFileTypeEnum.SEGMENTATION.getFilename()).getAbsolutePath());
     }
 
     /**
      * {@inheritDoc}
      */
     public File createIGVSegFile(Collection<SegmentData> segmentDatas, Study study, String platformName) {
         return segmentDatas.isEmpty() ? null : SegmentDatasetFileWriter.writeAsSegFile(segmentDatas,
                 retrieveIGVFile(study, IGVFileTypeEnum.SEGMENTATION, platformName).getAbsolutePath());
     }
     
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("PMD.ExcessiveParameterList") // All parameters are needed.
     public void createHeatmapGenomicFile(HeatmapParameters parameters, HeatmapResult result, 
             Collection<SegmentData> segmentDatas, GeneLocationConfiguration geneLocationConfiguration, 
             CBSToHeatmapFactory cbsToHeatmapFactory) throws IOException {
         File genomicDataFile = null;
         File layoutFile = null;
        if (parameters.isViewAllData()) {
             genomicDataFile = retrieveHeatmapFile(parameters.getStudySubscription().getStudy(), 
                     HeatmapFileTypeEnum.GENOMIC_DATA, parameters.getPlatform().getName());
             layoutFile = retrieveHeatmapFile(parameters.getStudySubscription().getStudy(), 
                     HeatmapFileTypeEnum.LAYOUT, parameters.getPlatform().getName());
         } else { 
             File sessionDirectory = new File(getHeatmapDirectory(parameters.getSessionId()) + File.separator); 
             genomicDataFile = new File(sessionDirectory, HeatmapFileTypeEnum.GENOMIC_DATA.getFilename());
             layoutFile = new File(sessionDirectory, HeatmapFileTypeEnum.LAYOUT.getFilename());
         }
         if (!segmentDatas.isEmpty()) {
             HeatmapGenomicDataFileWriter fileWriter = new HeatmapGenomicDataFileWriter(segmentDatas,
                     geneLocationConfiguration, parameters, cbsToHeatmapFactory);
             fileWriter.writeGenomicDataFiles(
                 genomicDataFile.getAbsolutePath(), layoutFile.getAbsolutePath());
         }
         result.setGenomicDataFile(genomicDataFile);
         result.setLayoutFile(layoutFile);
     }
     
     /**
      * {@inheritDoc}
      */
     public void createHeatmapJnlpFile(HeatmapParameters heatmapParameters, HeatmapResult heatmapResult) {
         HeatmapJnlpFileWriter.writeJnlpFile(getHeatmapDirectory(heatmapParameters.getSessionId()), 
                 heatmapParameters.getUrlPrefix(), heatmapParameters.getHeatmapJarUrlPrefix(), heatmapResult);
     }
     
     /**
      * {@inheritDoc}
      */
     public void createIGVSessionFile(IGVParameters igvParameters, IGVResult igvResult) {
         IGVSessionFileWriter.writeSessionFile(getIGVDirectory(igvParameters.getSessionId()), 
                 igvParameters.getUrlPrefix(), igvResult, igvParameters.getPlatforms());
     }
 
     /**
      * {@inheritDoc}
      */
     public void deleteViewerDirectory(Study study) {
         try {
             FileUtils.deleteDirectory(getStudyIGVDirectory(study));
             FileUtils.deleteDirectory(getStudyHeatmapDirectory(study));
         } catch (IOException e) {
             return;
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void deleteSessionDirectories(String sessionId) {
         try {
             FileUtils.deleteDirectory(getIGVDirectory(sessionId));
             FileUtils.deleteDirectory(getHeatmapDirectory(sessionId));
         } catch (IOException e) {
             return;
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void deleteAllTempAnalysisDirectories() {
         FileUtils.deleteQuietly(new File(fileManager.getTempDirectory(), AnalysisViewerTypeEnum.IGV.getValue()));
         FileUtils.deleteQuietly(new File(fileManager.getTempDirectory(), AnalysisViewerTypeEnum.HEATMAP.getValue()));
     }
     
     /**
      * {@inheritDoc}
      */
     public File getIGVDirectory(String sessionId) {
         if (StringUtils.isBlank(sessionId)) {
             throw new IllegalArgumentException("Session ID is null or blank, unable to get the IGV directory.");
         }
         return fileManager.getNewTemporaryDirectory(AnalysisViewerTypeEnum.IGV.getValue() + File.separator + sessionId);
     }
     
     /**
      * {@inheritDoc}
      */
     public File retrieveIGVFile(Study study, IGVFileTypeEnum fileType, String platformName) {
         return new File(getStudyIGVDirectory(study) + File.separator
                 + platformName + "_" + fileType.getFilename());
     }
     
     private File getStudyIGVDirectory(Study study) {
         File file = new File(fileManager.getStudyDirectory(study) + File.separator
                 + AnalysisViewerTypeEnum.IGV.getValue());
         file.mkdir();
         return file;
     }
     
     /**
      * {@inheritDoc}
      */
     public File getHeatmapDirectory(String sessionId) {
         if (StringUtils.isBlank(sessionId)) {
             throw new IllegalArgumentException("Session ID is null or blank, unable to get the heatmap directory.");
         }
         return fileManager.getNewTemporaryDirectory(AnalysisViewerTypeEnum.HEATMAP.getValue() + File.separator
                 + sessionId);
     }
     
     /**
      * {@inheritDoc}
      */
     public File retrieveHeatmapFile(Study study, HeatmapFileTypeEnum fileType, String platformName) {
         return new File(getStudyHeatmapDirectory(study) + File.separator
                 + platformName + "_" + fileType.getFilename());
     }
     
     private File getStudyHeatmapDirectory(Study study) {
         File file = new File(fileManager.getStudyDirectory(study) + File.separator
                 + AnalysisViewerTypeEnum.HEATMAP.getValue());
         file.mkdir();
         return file;
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
