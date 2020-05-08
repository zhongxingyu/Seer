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
 
 import gov.nih.nci.caintegrator2.domain.AbstractCaIntegrator2Object;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 import gov.nih.nci.caintegrator2.external.caarray.SampleIdentifier;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Records sample and array data retrieval information.
  */
 public class GenomicDataSourceConfiguration extends AbstractCaIntegrator2Object {
     
     private static final long serialVersionUID = 1L;
     private StudyConfiguration studyConfiguration;
     private ServerConnectionProfile serverProfile = new ServerConnectionProfile();
     private String experimentIdentifier;
     private String platformVendor;
     private String platformName;
     private String sampleMappingCommaSeparatedFileNames;
     private String controlSampleMappingCommaSeparatedFileNames;
     private List<SampleIdentifier> sampleIdentifiers = new ArrayList<SampleIdentifier>();
     private List<Sample> samples = new ArrayList<Sample>();
     private CopyNumberDataConfiguration copyNumberDataConfiguration;
 
     /**
      * @return the experimentIdentifier
      */
     public String getExperimentIdentifier() {
         return experimentIdentifier;
     }
     
     /**
      * @return the sampleIdentifiers
      */
     public List<SampleIdentifier> getSampleIdentifiers() {
         return sampleIdentifiers;
     }
 
     /**
      * @return the serverProfile
      */
     public ServerConnectionProfile getServerProfile() {
         return serverProfile;
     }
 
     /**
      * @param experimentIdentifier the experimentIdentifier to set
      */
     public void setExperimentIdentifier(String experimentIdentifier) {
         this.experimentIdentifier = experimentIdentifier;
     }
 
     @SuppressWarnings("unused")
     private void setServerProfile(ServerConnectionProfile serverProfile) {
         this.serverProfile = serverProfile;
     }
 
     @SuppressWarnings("unused")
     private void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
         this.sampleIdentifiers = sampleIdentifiers;
     }
 
     /**
      * @return the samples
      */
     public List<Sample> getSamples() {
         return samples;
     }
 
     /**
      * @param samples the samples to set
      */
     public void setSamples(List<Sample> samples) {
         this.samples = samples;
     }
 
     /**
      * @return the mapped samples
      */
     public List<Sample> getMappedSamples() {
         List<Sample> mappedSamples = new ArrayList<Sample>();
         mappedSamples.addAll(getSamples());
         mappedSamples.retainAll(getStudyConfiguration().getSamples());
         return mappedSamples;
     }
 
     /**
      * @return the control samples
      */
     public List<Sample> getControlSamples() {
         if (!getStudyConfiguration().getStudy().getDefaultControlSampleSet().getSamples().isEmpty()) {
             List<Sample> controlSamples = new ArrayList<Sample>();
             controlSamples.addAll(getStudyConfiguration().getStudy().getDefaultControlSampleSet().getSamples());
             controlSamples.retainAll(getSamples());
             return controlSamples;
         } else {
             return Collections.emptyList();
         }    
     }
 
     /**
      * @return the unmapped samples
      */
     public List<Sample> getUnmappedSamples() {
         List<Sample> unmappedSamples = new ArrayList<Sample>();
         unmappedSamples.addAll(getSamples());
         if (!getStudyConfiguration().getStudy().getDefaultControlSampleSet().getSamples().isEmpty()) {
             unmappedSamples.removeAll(getStudyConfiguration().getStudy().getDefaultControlSampleSet().getSamples());
         }
         unmappedSamples.removeAll(getStudyConfiguration().getSamples());
         return unmappedSamples;
     }
 
     /**
      * @return the studyConfiguration
      */
     public StudyConfiguration getStudyConfiguration() {
         return studyConfiguration;
     }
 
     /**
      * @param studyConfiguration the studyConfiguration to set
      */
     public void setStudyConfiguration(StudyConfiguration studyConfiguration) {
         this.studyConfiguration = studyConfiguration;
     }
 
     /**
      * @return the platformVendor
      */
     public String getPlatformVendor() {
         return platformVendor;
     }
 
     /**
      * @param platformVendor the platformVendor to set
      */
     public void setPlatformVendor(String platformVendor) {
         this.platformVendor = platformVendor;
     }
 
     /**
      * @return the platformName
      */
     public String getPlatformName() {
         return platformName;
     }
 
     /**
      * @param platformName the platformName to set
      */
     public void setPlatformName(String platformName) {
         this.platformName = platformName;
     }
 
     /**
      * @return the copyNumberDataConfiguration
      */
     public CopyNumberDataConfiguration getCopyNumberDataConfiguration() {
         return copyNumberDataConfiguration;
     }
 
     /**
      * @param copyNumberDataConfiguration the copyNumberDataConfiguration to set
      */
     public void setCopyNumberDataConfiguration(CopyNumberDataConfiguration copyNumberDataConfiguration) {
         this.copyNumberDataConfiguration = copyNumberDataConfiguration;
     }
     
     /**
      * Adds the filename to the comma separated list of file names.
      * @param filename to add to the list.
      */
     public void addSampleMappingFileName(String filename) {
         if (sampleMappingCommaSeparatedFileNames == null) {
             setSampleMappingCommaSeparatedFileNames(filename);    
         } else {
             setSampleMappingCommaSeparatedFileNames(sampleMappingCommaSeparatedFileNames 
                                                     + "," + filename);
         }
     }
 
     /**
      * @return the sampleMappingCommaSeparatedFileNames
      */
     public String getSampleMappingCommaSeparatedFileNames() {
         return sampleMappingCommaSeparatedFileNames;
     }
 
     private void setSampleMappingCommaSeparatedFileNames(String sampleMappingCommaSeparatedFileNames) {
         this.sampleMappingCommaSeparatedFileNames = sampleMappingCommaSeparatedFileNames;
     }
 
     /**
      * Used for the visual display of the sample mapping file names.
      * @return list of sample mapping file names.
      */
     public List<String> getSampleMappingFileNames() {
         List<String> sampleMappingFileNames = new ArrayList<String>();
         if (StringUtils.isBlank(sampleMappingCommaSeparatedFileNames)) {
             sampleMappingFileNames.add("None Configured");
         } else {
             sampleMappingFileNames.addAll(Arrays.asList(StringUtils.split(sampleMappingCommaSeparatedFileNames, ",")));
         }
         return sampleMappingFileNames;
     }
     
     /**
      * Adds the filename to the comma separated list of file names.
      * @param filename to add to the list.
      */
     public void addControlSampleMappingFileName(String filename) {
         if (controlSampleMappingCommaSeparatedFileNames == null) {
             setControlSampleMappingCommaSeparatedFileNames(filename);    
         } else {
             setControlSampleMappingCommaSeparatedFileNames(controlSampleMappingCommaSeparatedFileNames 
                                                     + "," + filename);
         }
     }
     
     /**
      * @return the controlSampleMappingCommaSeparatedFileName
      */
     public String getControlSampleMappingCommaSeparatedFileNames() {
         return controlSampleMappingCommaSeparatedFileNames;
     }
 
     private void setControlSampleMappingCommaSeparatedFileNames(String controlSampleMappingCommaSeparatedFileNames) {
         this.controlSampleMappingCommaSeparatedFileNames = controlSampleMappingCommaSeparatedFileNames;
     }
 
     /**
      * Used for the visual display of the control sample mapping file names.
      * @return list of control sample mapping file names.
      */
     public List<String> getControlSampleMappingFileNames() {
         List<String> controlSampleMappingFileNames = new ArrayList<String>();
         if (StringUtils.isBlank(controlSampleMappingCommaSeparatedFileNames)) {
             controlSampleMappingFileNames.add("None Configured");
         } else {
             controlSampleMappingFileNames.addAll(Arrays.asList(
                     StringUtils.split(controlSampleMappingCommaSeparatedFileNames, ",")));
         }
         return controlSampleMappingFileNames;
     }
     
     /**
      * Used for the visual display of the copy number mapping file name.
      * @return file name.
      */
     public String getCopyNumberMappingFileName() {
         if (getCopyNumberDataConfiguration() != null 
             && getCopyNumberDataConfiguration().getMappingFile() != null) {
             return getCopyNumberDataConfiguration().getMappingFile().getName();
         }
         return "None Configured";
     }
 
     Sample getSample(String sampleName) {
         for (Sample sample : getSamples()) {
            if (sampleName.equals(sample.getName())) {
                 return sample;
             }
         }
         return null;
     }
 }
