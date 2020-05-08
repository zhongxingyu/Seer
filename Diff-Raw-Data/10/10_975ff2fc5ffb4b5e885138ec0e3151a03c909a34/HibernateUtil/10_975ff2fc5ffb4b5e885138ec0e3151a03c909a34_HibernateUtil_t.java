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
 package gov.nih.nci.caintegrator2.common;
 
 import gov.nih.nci.caintegrator2.application.study.AbstractClinicalSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.AnnotationFile;
 import gov.nih.nci.caintegrator2.application.study.AnnotationGroup;
 import gov.nih.nci.caintegrator2.application.study.DelimitedTextClinicalSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.FileColumn;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.SelectedValueCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleSet;
 import gov.nih.nci.caintegrator2.domain.imaging.ImageSeries;
 import gov.nih.nci.caintegrator2.domain.imaging.ImageSeriesAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.hibernate.Hibernate;
 
 /**
  * This is a static utility class which is used by CaIntegrator2 objects for hibernate.
  */
 public final class HibernateUtil {
 
     private HibernateUtil() { }
 
 
     /**
      * Make sure all persistent collections are loaded.
      *
      * @param definition the definition to ensure is loaded from Hibernate.
      */
     public static void loadCollections(AnnotationDefinition definition) {
         loadCollection(definition.getAnnotationValueCollection());
         loadCollection(definition.getPermissibleValueCollection());
     }
 
     /**
      * Make sure all persistent collections are loaded.
      *
      * @param query the query to ensure is loaded from Hibernate.
      */
     public static void loadCollection(Query query) {
         if (query.getCompoundCriterion() != null) {
             loadCollection(query.getCompoundCriterion().getCriterionCollection());
             for (AbstractCriterion criterion : query.getCompoundCriterion().getCriterionCollection()) {
                 if (criterion instanceof SelectedValueCriterion) {
                     loadCollection(((SelectedValueCriterion) criterion).getValueCollection());
                 } else if (criterion instanceof FoldChangeCriterion) {
                     loadCollection(((FoldChangeCriterion) criterion).getCompareToSampleSet().getSamples());
                 }
             }
         }
         loadCollection(query.getSubscription().getStudy().getAssignmentCollection());
         loadCollection(query.getColumnCollection());
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * @param studyConfiguration to load from hibernate.
      */
     public static void loadCollection(StudyConfiguration studyConfiguration) {
         loadCollection(studyConfiguration.getClinicalConfigurationCollection());
         loadGenomicSources(studyConfiguration.getGenomicDataSources());
         loadImagingSources(studyConfiguration.getImageDataSources());
         loadClinicalSources(studyConfiguration.getClinicalConfigurationCollection());
         HibernateUtil.loadCollection(studyConfiguration.getStudy().getAssignmentCollection());
         for (StudySubjectAssignment assignment : studyConfiguration.getStudy().getAssignmentCollection()) {
             loadCollection(assignment.getSampleAcquisitionCollection());
             loadCollection(assignment.getImageStudyCollection());
             loadCollection(assignment.getSubjectAnnotationCollection());
             for (SampleAcquisition sampleAcquisition : assignment.getSampleAcquisitionCollection()) {
                 loadCollection(sampleAcquisition.getAnnotationCollection());
                 loadSampleCollections(sampleAcquisition.getSample());
             }
         }
         loadCollection(studyConfiguration.getStudy());
     }
 
     /**
      * Loads study annotation groups.
      * @param study to load.
      */
     public static void loadCollection(Study study) {
         loadCollection(study.getAnnotationGroups());
         for (AnnotationGroup group : study.getAnnotationGroups()) {
             loadCollection(group.getAnnotationFieldDescriptors());
         }
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * @param studySubscription to load from hibernate.
      */
     public static void loadCollection(StudySubscription studySubscription) {
         loadCollection(studySubscription.getQueryCollection());
         loadCollection(studySubscription.getListCollection());
         loadCollection(studySubscription.getAnalysisJobCollection());
         for (Query query : studySubscription.getQueryCollection()) {
             loadCollection(query);
         }
         loadCollection(studySubscription.getStudy().getStudyConfiguration());
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * @param clinicalSource to load from hibernate.
      */
     private static void loadClinicalSource(AbstractClinicalSourceConfiguration clinicalSource) {
         loadCollection(clinicalSource.getAnnotationDescriptors());
         if (clinicalSource instanceof DelimitedTextClinicalSourceConfiguration) {
             loadAnnotationFile(((DelimitedTextClinicalSourceConfiguration) clinicalSource).getAnnotationFile());
         } else {
             throw new IllegalStateException("Unknown clinical source type.");
         }
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * @param genomicSources List of GenomicDataSourceConfiguration to load from hibernate.
      */
     public static void loadGenomicSources(List<GenomicDataSourceConfiguration> genomicSources) {
         loadCollection(genomicSources);
         for (GenomicDataSourceConfiguration genomicSource : genomicSources) {
             loadGenomicSource(genomicSource);
         }
     }
 
 
     /**
      * Loads the genomic source.
      * @param genomicSource to load.
      */
     public static void loadGenomicSource(GenomicDataSourceConfiguration genomicSource) {
         if (genomicSource != null) {
             loadSamples(genomicSource.getSamples());
             loadSampleSets(genomicSource.getControlSampleSetCollection());
             Hibernate.initialize(genomicSource.getServerProfile());
         }
     }
 
     /**
      * Make sure all persistent collections are loaded.
      * @param imageSources List of ImageDataSourceConfiguration to load from hibernate.
      */
     public static void loadImagingSources(List<ImageDataSourceConfiguration> imageSources) {
         loadCollection(imageSources);
         for (ImageDataSourceConfiguration imageSource : imageSources) {
             loadImagingSource(imageSource);
         }
     }
 
     private static void loadClinicalSources(List<AbstractClinicalSourceConfiguration> clinicalConfigurationCollection) {
         for (AbstractClinicalSourceConfiguration clinicalSource : clinicalConfigurationCollection) {
             loadClinicalSource(clinicalSource);
         }
     }
 
     /**
      * Loads the imaging source.
      * @param imageSource to load.
      */
     public static void loadImagingSource(ImageDataSourceConfiguration imageSource) {
         Hibernate.initialize(imageSource);
         Hibernate.initialize(imageSource.getServerProfile());
         loadImageSeriesAcquisitions(imageSource.getImageSeriesAcquisitions());
         if (imageSource.getImageAnnotationConfiguration() != null) {
             Hibernate.initialize(imageSource.getImageAnnotationConfiguration());
             if (imageSource.getImageAnnotationConfiguration().isAimDataService()) {
                 Hibernate.initialize(imageSource.getImageAnnotationConfiguration().getAimServerProfile());
             } else {
                 loadAnnotationFile(imageSource.getImageAnnotationConfiguration().getAnnotationFile());
             }
         }
     }
 
     private static void loadAnnotationFile(AnnotationFile annotationFile) {
         Hibernate.initialize(annotationFile);
         loadCollection(annotationFile.getColumns());
         for (FileColumn column : annotationFile.getColumns()) {
             Hibernate.initialize(column);
             if (column.getFieldDescriptor() != null) {
                 Hibernate.initialize(column.getFieldDescriptor());
                 if (column.getFieldDescriptor().getDefinition() != null) {
                     loadCollections(column.getFieldDescriptor().getDefinition());
                 }
             }
         }
     }
 
 
     private static void loadImageSeriesAcquisitions(List<ImageSeriesAcquisition> imageSeriesAcquisitions) {
         loadCollection(imageSeriesAcquisitions);
         for (ImageSeriesAcquisition imageSeriesAcquisition : imageSeriesAcquisitions) {
             Hibernate.initialize(imageSeriesAcquisition);
             for (ImageSeries imageSeries : imageSeriesAcquisition.getSeriesCollection()) {
                 Hibernate.initialize(imageSeries);
                 loadCollection(imageSeries.getImageCollection());
                 loadCollection(imageSeries.getAnnotationCollection());
             }
         }
 
     }
 
     /**
      * Loads the sample set collection, as well as the subcollections.
      * @param sampleSets to load.
      */
 
     public static void loadSampleSets(Collection<SampleSet> sampleSets) {
         for (SampleSet sampleSet : sampleSets) {
             loadSamples(sampleSet.getSamples());
         }
     }
 
     /**
      * Loads the samples collection, as well as the subcollections.
      * @param samples to load.
      */
     public static void loadSamples(Collection<Sample> samples) {
         loadCollection(samples);
         for (Sample sample : samples) {
             loadSampleCollections(sample);
         }
     }
 
     /**
      * Loads the subcollections for the Sample object.
      * @param sample to load.
      */
     public static void loadSampleCollections(Sample sample) {
         loadCollection(sample.getArrayCollection());
         loadCollection(sample.getArrayDataCollection());
         for (ArrayData arrayData : sample.getArrayDataCollection()) {
             loadCollection(arrayData.getReporterLists());
         }
     }
 
     /**
      * Ensure that a persistent collection is loaded.
      *
      * @param collection the collection to load.
      */
     public static void loadCollection(Collection<? extends Object> collection) {
         Hibernate.initialize(collection);
     }
 
 }
