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
 package gov.nih.nci.caintegrator2.application.study.deployment;
 
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.ValidationException;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.genomic.Array;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayDataType;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.DataRetrievalException;
 import gov.nih.nci.caintegrator2.external.caarray.CaArrayFacade;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Provides base handling to retrieve copy number data based on a copy number mapping file.
  */
 public abstract class AbstractUnparsedSupplementalMappingFileHandler extends AbstractSupplementalMappingFileHandler {
 
     AbstractUnparsedSupplementalMappingFileHandler(GenomicDataSourceConfiguration genomicSource,
             CaArrayFacade caArrayFacade, ArrayDataService arrayDataService, CaIntegrator2Dao dao) {
         super(genomicSource, caArrayFacade, arrayDataService, dao);
     }
 
     /**
      * 
      * @param sampleName the sample name to retrieve
      * @param subjectIdentifier the study assignment id
      * @return the sample object
      * @throws ValidationException Validation exception
      * @throws FileNotFoundException IO exception
      */
     protected Sample getSample(String sampleName, String subjectIdentifier)
     throws FileNotFoundException, ValidationException {
         Sample sample = getGenomicSource().getSample(sampleName);
         if (sample == null) {
             sample = new Sample();
             sample.setName(sampleName);
             getGenomicSource().getSamples().add(sample);
             addSampleAcquisition(subjectIdentifier, sample);
         } else if (sample.getSampleAcquisition() == null) {
             addSampleAcquisition(subjectIdentifier, sample);
         }
         return sample;
     }
 
     private void addSampleAcquisition(String subjectIdentifier, Sample sample) throws ValidationException,
             FileNotFoundException {
         StudySubjectAssignment assignment = getSubjectAssignment(subjectIdentifier);
         SampleAcquisition acquisition = new SampleAcquisition();
         acquisition.setAssignment(assignment);
         acquisition.setSample(sample);
         sample.setSampleAcquisition(acquisition);
         assignment.getSampleAcquisitionCollection().add(acquisition);
         getDao().save(sample);
     }
 
     /**
      * 
      * @return the mapping file.
      * @throws FileNotFoundException IO exception.
      */
     protected File getMappingFile() throws FileNotFoundException {
         return getGenomicSource().getDnaAnalysisDataConfiguration().getMappingFile();
     }
 
     abstract List<ArrayDataValues> loadArrayData()
     throws ConnectionException, DataRetrievalException, ValidationException, IOException;
     
     /**
      * Get the platform.
      * @param reporterListNames the set of report lists.
      * @return Platform
      * @throws ValidationException when platform not found or not unique.
      */
     protected Platform getPlatform(Set<String> reporterListNames) throws ValidationException {
         Set<Platform> platforms = new HashSet<Platform>();
         for (String reporterListName : reporterListNames) {
             ReporterList reporterList = getDao().getReporterList(reporterListName);
             if (reporterList == null) {
                 throw new ValidationException("There is no platform that supports chip type " + reporterListName);
             }
             platforms.add(reporterList.getPlatform());
         }
         if (platforms.size() > 1) {
             throw new ValidationException(
                    "DNA analysis data files for a single sample are mapped to multiple platforms.");
         }
         return platforms.iterator().next();
     }
 
     /**
      * Clean up.
      * @param dataFile the data file to delete.
      */
     protected void doneWithFile(File dataFile) {
         dataFile.delete();
     }
     
     abstract String getFileType();
 
     /**
      * Create the ArrayData for the sample.
      * @param sample the sample to get arrayData for.
      * @param reporterLists the set of report lists.
      * @param dataType the array data type.
      * @return ArrayData
      */
     protected ArrayData createArrayData(Sample sample, Set<ReporterList> reporterLists, ArrayDataType dataType) {
         ArrayData arrayData = new ArrayData();
         arrayData.setType(dataType);
         arrayData.setSample(sample);
         sample.getArrayDataCollection().add(arrayData);
         arrayData.setStudy(getGenomicSource().getStudyConfiguration().getStudy());
         Array array = new Array();
         array.getArrayDataCollection().add(arrayData);
         arrayData.setArray(array);
         array.getSampleCollection().add(sample);
         sample.getArrayCollection().add(array);
         if (!reporterLists.isEmpty()) {
             arrayData.getReporterLists().addAll(reporterLists);
             for (ReporterList reporterList : reporterLists) {
                 reporterList.getArrayDatas().add(arrayData);    
             }
             array.setPlatform(reporterLists.iterator().next().getPlatform());
         }
         return arrayData;
     }
 
     private StudySubjectAssignment getSubjectAssignment(String subjectIdentifier)
     throws ValidationException, FileNotFoundException {
         StudySubjectAssignment assignment = 
             getGenomicSource().getStudyConfiguration().getSubjectAssignment(subjectIdentifier);
         if (assignment == null) {
             throw new ValidationException("Subject identifier " + subjectIdentifier + " in DNA analysis mapping file " 
                     + getMappingFile().getAbsolutePath() + " doesn't map to a known subject in the study.");
         }
         return assignment;
     }
 
 }
