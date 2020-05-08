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
 
 import gov.nih.nci.caintegrator2.domain.annotation.SubjectAnnotation;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator2.domain.translational.Subject;
 import gov.nih.nci.caintegrator2.domain.translational.Timepoint;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Holds data about the sources of study data and authorization for access to data.
  */
 public class StudyConfiguration {
     
     private Long id;
     private Visibility visibility;
     private Status status = Status.NOT_DEPLOYED;
     private Study study;
     private List<AbstractClinicalSourceConfiguration> clinicalConfigurationCollection =
         new ArrayList<AbstractClinicalSourceConfiguration>();   
     private List<GenomicDataSourceConfiguration> genomicDataSources = new ArrayList<GenomicDataSourceConfiguration>();
 
     private transient Map<String, StudySubjectAssignment> identifierToSubjectAssignmentMap;
     private transient Map<String, Timepoint> nameToTimepointMap;
     
 
     StudyConfiguration() {
         super();
     }
 
     StudyConfiguration(Study study) {
         this();
         setStudy(study);
     }
 
     /**
      * Returns the id.
      *
      * @return the id
      */
     public Long getId() {
         return id;
     }
 
     /**
      * Sets the id.
      *
      * @param id the id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * @return the clinicalConfigurationCollection
      */
     public List<AbstractClinicalSourceConfiguration> getClinicalConfigurationCollection() {
         return clinicalConfigurationCollection;
     }
 
     /**
      * @param clinicalConfigurationCollection the clinicalConfigurationCollection to set
      */
     public void setClinicalConfigurationCollection(
             List<AbstractClinicalSourceConfiguration> clinicalConfigurationCollection) {
         this.clinicalConfigurationCollection = clinicalConfigurationCollection;
     }
     
     /**
      * Adds a configuration to the collection.
      * @param configuration - Configuration file to add to the list.
      */
     public void addClinicalConfiguration(AbstractClinicalSourceConfiguration configuration) {
         this.clinicalConfigurationCollection.add(configuration);
     }
 
     /**
      * @return the visibility
      */
     public Visibility getVisibility() {
         return visibility;
     }
 
     /**
      * @param visibility the visibility to set
      */
     public void setVisibility(Visibility visibility) {
         this.visibility = visibility;
     }
 
     /**
      * @return the study
      */
     public Study getStudy() {
         return study;
     }
 
     private void setStudy(Study study) {
         this.study = study;
     }
 
     /**
      * @return the status
      */
     public Status getStatus() {
         return status;
     }
 
     /**
      * @param status the status to set
      */
     public void setStatus(Status status) {
         this.status = status;
     }
 
     /**
      * @return the genomicDataSources
      */
     public List<GenomicDataSourceConfiguration> getGenomicDataSources() {
         return genomicDataSources;
     }
 
     @SuppressWarnings("unused")
     private void setGenomicDataSources(List<GenomicDataSourceConfiguration> genomicDataSources) {
         this.genomicDataSources = genomicDataSources;
     }
 
     Set<AnnotationFieldDescriptor> getAllExistingDescriptors() {
         Set<AnnotationFieldDescriptor> existingDescriptors = new HashSet<AnnotationFieldDescriptor>();
         for (AbstractClinicalSourceConfiguration clinicalSourceConfiguration : clinicalConfigurationCollection) {
             existingDescriptors.addAll(clinicalSourceConfiguration.getAnnotationDescriptors());
         }
         return existingDescriptors;
     }
 
     StudySubjectAssignment getOrCreateSubjectAssignment(String identifier) {
         if (getIdentifierToSubjectAssignmentMap().containsKey(identifier)) {
             return getIdentifierToSubjectAssignmentMap().get(identifier);
         } else {
             return createSubjectAssignment(identifier);
         }
     }
 
     private StudySubjectAssignment createSubjectAssignment(String identifier) {
         StudySubjectAssignment studySubjectAssignment = new StudySubjectAssignment();
         studySubjectAssignment.setSubject(new Subject());
         studySubjectAssignment.setStudy(getStudy());
         studySubjectAssignment.setIdentifier(identifier);
         studySubjectAssignment.setSubjectAnnotation(new HashSet<SubjectAnnotation>());
         getIdentifierToSubjectAssignmentMap().put(identifier, studySubjectAssignment);
         return studySubjectAssignment;
     }
 
     private Map<String, StudySubjectAssignment> getIdentifierToSubjectAssignmentMap() {
         if (identifierToSubjectAssignmentMap == null) {
             loadIdentifierToSubjectAssignmentMap();
         }
         return identifierToSubjectAssignmentMap;
     }
     
     private void loadIdentifierToSubjectAssignmentMap() {
         identifierToSubjectAssignmentMap = new HashMap<String, StudySubjectAssignment>();
         for (StudySubjectAssignment assignment : getStudy().getAssignmentCollection()) {
             identifierToSubjectAssignmentMap.put(assignment.getIdentifier(), assignment);
         }
     }
 
     Timepoint getOrCreateTimepoint(String name) {
         if (getNameToTimepointMap().containsKey(name)) {
             return getNameToTimepointMap().get(name);
         } else {
             return createTimepoint(name);
         }
     }
 
     private Timepoint createTimepoint(String name) {
         Timepoint timepoint = new Timepoint();
         timepoint.setName(name);
         getNameToTimepointMap().put(name, timepoint);
         return timepoint;
     }
 
     private Map<String, Timepoint> getNameToTimepointMap() {
         if (nameToTimepointMap == null) {
             loadNameToTimepointMap();
         }
         return nameToTimepointMap;
     }
     
     private void loadNameToTimepointMap() {
         nameToTimepointMap = new HashMap<String, Timepoint>();
         for (Timepoint timepoint : getStudy().getTimepointCollection()) {
             nameToTimepointMap.put(timepoint.getName(), timepoint);
         }
     }
 
 }
