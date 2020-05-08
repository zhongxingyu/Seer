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
 package gov.nih.nci.caintegrator2;
 
 import java.io.File;
 
 public class TestDataFiles {
 
     private static final String AFFYMETRIX_DATA_FILE_DIRECTORY_PATH = "/arraydata/affymetrix";
     private static final String AGILENT_DATA_FILE_DIRECTORY_PATH = "/arraydata/agilent";
 
     public static final String VALID_FILE_RESOURCE_PATH = "/csvtestclinical.csv";
     public static final String VALID_ANNOTATION_GROUP_FILE_PATH = "/csvtestclinical_annotationGroup.csv";
     public static final String VALID_FILE_TIMEPOINT_RESOURCE_PATH = "/csvtestclinical-timepoint.csv";
     public static final String INVALID_FILE_MISSING_VALUE_RESOURCE_PATH = "/csvtestclinical-missing-value.csv";
     public static final String INVALID_FILE_EMPTY_RESOURCE_PATH = "/emptyfile.txt";
     public static final String INVALID_FILE_NO_DATA_RESOURCE_PATH = "/csvtestclinical-no-data.csv";
     public static final String INVALID_FILE_DOESNT_EXIST_PATH = "nofile.txt";
     public static final String REMBRANDT_CLINICAL_FILE_PATH = "/rembrandt_clinical_Aug08_subset.csv";
     public static final String REMBRANDT_ANNOTATION_GROUP_FILE_PATH = "/rembrandt_annotation_group.csv";
     public static final String REMBRANDT_SAMPLE_MAPPING_FILE_PATH = "/rembrandt_caarray_sample_mapping.csv";
     public static final String REMBRANDT_CONTROL_SAMPLE_SET_NAME = "Rembrandt Control Sample Set";
     public static final String REMBRANDT_CONTROL_SAMPLES_FILE_PATH = "/rembrandt_control_samples.csv";
     public static final String SHORT_REMBRANDT_CONTROL_SAMPLE_SET_NAME = "Short Rembrandt Control Sample Set";
     public static final String SHORT_REMBRANDT_CONTROL_SAMPLES_FILE_PATH = "/short_rembrandt_control_samples.csv";
     public static final String SHORT_REMBRANDT_SAMPLE_MAPPING_FILE_PATH = "/short_rembrandt_sample_mapping.csv";
     public static final String GENE_LIST_SAMPLES_FILE_PATH = "/gene_list_samples.csv";
     public static final String JAGLA_00034_CONTROL_SAMPLES_SET_NAME = "JAGLA 00034 Control Sample Set";
     public static final String JAGLA_00034_CONTROL_SAMPLES_FILE_PATH = "/jagla_0034_control_samples.csv";
     public static final String VASARI_IMAGE_ANNOTATION_FILE_PATH = "/vasari_image_annotation_mapped.csv";
     public static final String REMBRANDT_IMAGE_STUDIES_TO_SUBJECT_FILE_PATH = "/rembrandt_ncia_image_studies.csv";
     public static final String SIMPLE_AGILENT_SAMPLE_MAPPING_FILE_PATH = "/simple_Agilent_sample_mapping.csv";
     public static final String SIMPLE_SAMPLE_MAPPING_FILE_PATH = "/simple_sample_mapping.csv";
     public static final String SIMPLE_IMAGE_MAPPING_FILE_PATH = "/simple_image_mapping.csv";
     public static final String SIMPLE_EXTERNAL_LINKS_FILE_PATH = "/simple_external_links.csv";
     public static final String ISPY_IMAGE_ANNOTATION_FILE_PATH = "/ispy_image_annotations.csv";
     public static final String ISPY_IMAGE_SERIES_TO_SUBJECT_FILE_PATH = "/ispy_clinical_image_mapping.csv";
     public static final String REMBRANDT_NCRI_CLINICAL_FILE_PATH = "/rembrandt_clinical_Aug08_subset_mod_for_NCRI.csv";
     public static final String REMBRANDT_NCRI_IMAGE_ANNOTATION_FILE_PATH = "/ncri_image_annotations.csv";
     public static final String REMBRANDT_NCRI_IMAGE_SERIES_TO_SUBJECT_FILE_PATH = "/ncri_image_mapping.csv";
     public static final String REMBRANDT_NCRI_SAMPLE_MAPPING_FILE_PATH = "/ncri_sample_mapping.csv";
     public static final String REMBRANDT_NCRI_COPY_NUMBER_MAPPING_FILE_PATH = "/rembrandt_copy_number_mapping.csv";
     public static final String NCRI_LOGO_FILE_PATH = "/ncri_logo.jpg";
     
     public static final File VALID_FILE = getFile(VALID_FILE_RESOURCE_PATH);
     public static final File VALID_ANNOTATION_GROUP_FILE = getFile(VALID_ANNOTATION_GROUP_FILE_PATH);
     public static final File VALID_FILE_TIMEPOINT = getFile(VALID_FILE_TIMEPOINT_RESOURCE_PATH);
     public static final File INVALID_FILE_MISSING_VALUE = getFile(INVALID_FILE_MISSING_VALUE_RESOURCE_PATH);
     public static final File INVALID_FILE_EMPTY = getFile(INVALID_FILE_EMPTY_RESOURCE_PATH);
     public static final File INVALID_FILE_NO_DATA = getFile(INVALID_FILE_NO_DATA_RESOURCE_PATH);
     public static final File INVALID_FILE_DOESNT_EXIST= new File(INVALID_FILE_DOESNT_EXIST_PATH);
     public static final File REMBRANDT_CLINICAL_FILE = getFile(REMBRANDT_CLINICAL_FILE_PATH);
     public static final File REMBRANDT_ANNOTATION_GROUP_FILE = getFile(REMBRANDT_ANNOTATION_GROUP_FILE_PATH);
     public static final File REMBRANDT_SAMPLE_MAPPING_FILE = getFile(REMBRANDT_SAMPLE_MAPPING_FILE_PATH);
     public static final File REMBRANDT_CONTROL_SAMPLES_FILE = getFile(REMBRANDT_CONTROL_SAMPLES_FILE_PATH);
     public static final File SHORT_REMBRANDT_SAMPLE_MAPPING_FILE = getFile(SHORT_REMBRANDT_SAMPLE_MAPPING_FILE_PATH);
     public static final File SHORT_REMBRANDT_CONTROL_SAMPLES_FILE = getFile(SHORT_REMBRANDT_CONTROL_SAMPLES_FILE_PATH);
     public static final File SIMPLE_AGILENT_SAMPLE_MAPPING_FILE = getFile(SIMPLE_AGILENT_SAMPLE_MAPPING_FILE_PATH);
     public static final File SIMPLE_SAMPLE_MAPPING_FILE = getFile(SIMPLE_SAMPLE_MAPPING_FILE_PATH);
     public static final File SIMPLE_IMAGE_MAPPING_FILE = getFile(SIMPLE_IMAGE_MAPPING_FILE_PATH);
     public static final File SIMPLE_EXTERNAL_LINKS_FILE = getFile(SIMPLE_EXTERNAL_LINKS_FILE_PATH);
     public static final File VASARI_IMAGE_ANNOTATION_FILE = getFile(VASARI_IMAGE_ANNOTATION_FILE_PATH);
     public static final File REMBRANDT_IMAGE_STUDIES_TO_SUBJECT_FILE = getFile(REMBRANDT_IMAGE_STUDIES_TO_SUBJECT_FILE_PATH);
     public static final File ISPY_IMAGE_ANNOTATION_FILE = getFile(ISPY_IMAGE_ANNOTATION_FILE_PATH);
     public static final File ISPY_IMAGE_SERIES_TO_SUBJECT_FILE = getFile(ISPY_IMAGE_SERIES_TO_SUBJECT_FILE_PATH);
     public static final File NCRI_LOGO = getFile(NCRI_LOGO_FILE_PATH);
     
     // Test genomic data files
     public static final String TEST_AGILENT_SAMPLE_MAPPING_FILE_PATH = "/test_Agilent_sample_mapping.csv";
     public static final File TEST_AGILENT_SAMPLE_MAPPING_FILE = getFile(TEST_AGILENT_SAMPLE_MAPPING_FILE_PATH);
     
 
     // Files for DC Lung Study (liu-00175 in caarray-stage)
     public static final String DC_LUNG_CLINICAL_FILE_PATH = "/dc_lung_clinical_data.csv";
     public static final String DC_LUNG_SAMPLE_MAPPING_FILE_PATH = "/dc_lung_sample_mapping.csv";
     public static final String DC_LUNG_SAMPLE_MAPPING_FULL_FILE_PATH = "/dc_lung_sample_mapping_full.csv";
     public static final String DC_LUNG_ANNOTATION_DEFINITIONS_FILE_PATH = "/dc_lung_annotation_definitions.csv";
 
     public static final File DC_LUNG_CLINICAL_FILE = getFile(DC_LUNG_CLINICAL_FILE_PATH);
     public static final File DC_LUNG_SAMPLE_MAPPING_FILE = getFile(DC_LUNG_SAMPLE_MAPPING_FILE_PATH);
     public static final File DC_LUNG_SAMPLE_MAPPING_FULL_FILE = getFile(DC_LUNG_SAMPLE_MAPPING_FULL_FILE_PATH);
     public static final File DC_LUNG_ANNOTATION_DEFINITIONS_FILE = getFile(DC_LUNG_ANNOTATION_DEFINITIONS_FILE_PATH);
     
     // Files for public DC Lung Study
     public static final String DC_LUNG_PUBLIC_CLINICAL_FILE_PATH = "/dc_lung_clinical_data_public.csv";
     public static final String DC_LUNG_PUBLIC_SAMPLE_MAPPING_FILE_PATH = "/dc_lung_sample_mapping_full.csv";
     public static final String DC_LUNG_PUBLIC_CONTROL_SAMPLE_SET_NAME = "DC Lung Control_Sample Set";
     public static final String DC_LUNG_PUBLIC_CONTROL_SAMPLE_MAPPING_FILE_PATH = "/dc_lung_control_samples.csv";
     public static final String DC_LUNG_PUBLIC_ANNOTATION_GROUP_FILE_PATH = "/dc_lung_public_annotation_group.csv";
 
     public static final File DC_LUNG_PUBLIC_CLINICAL_FILE = getFile(DC_LUNG_PUBLIC_CLINICAL_FILE_PATH);
     public static final File DC_LUNG_PUBLIC_SAMPLE_MAPPING_FILE = getFile(DC_LUNG_PUBLIC_SAMPLE_MAPPING_FILE_PATH);
     public static final File DC_LUNG_PUBLIC_CONTROL_SAMPLE_MAPPING_FILE = getFile(DC_LUNG_PUBLIC_CONTROL_SAMPLE_MAPPING_FILE_PATH);
     public static final File DC_LUNG_PUBLIC_ANNOTATION_GROUP_FILE = getFile(DC_LUNG_PUBLIC_ANNOTATION_GROUP_FILE_PATH);
 
     
     // Samples of genelist file upload
     public static final File GENE_LIST_SAMPLES_FILE = getFile(GENE_LIST_SAMPLES_FILE_PATH);
     
     // File for Annotation Group
     public static final String ANNOTATION_GROUP_FILE_PATH = "/csvtest_annotationGroup.csv";
     public static final File ANNOTATION_GROUP_FILE = getFile(ANNOTATION_GROUP_FILE_PATH);
     
     // Samples from caArray Experiment jagla-00034
     public static final File JAGLA_00034_CONTROL_SAMPLES_FILE = getFile(JAGLA_00034_CONTROL_SAMPLES_FILE_PATH);
 
     // Trial NCRI from NCIA
     public static final File REMBRANDT_NCRI_CLINICAL_FILE = getFile(REMBRANDT_NCRI_CLINICAL_FILE_PATH);
     public static final File REMBRANDT_NCRI_IMAGE_ANNOTATION_FILE = getFile(REMBRANDT_NCRI_IMAGE_ANNOTATION_FILE_PATH);
     public static final File REMBRANDT_NCRI_IMAGE_SERIES_TO_SUBJECT_FILE = getFile(REMBRANDT_NCRI_IMAGE_SERIES_TO_SUBJECT_FILE_PATH);
     public static final File REMBRANDT_NCRI_SAMPLE_MAPPING_FILE = getFile(REMBRANDT_NCRI_SAMPLE_MAPPING_FILE_PATH);
     public static final File REMBRANDT_NCRI_COPY_NUMBER_MAPPING_FILE = getFile(REMBRANDT_NCRI_COPY_NUMBER_MAPPING_FILE_PATH);
     
 
     // Copy number test data
     public static final String HIND_COPY_NUMBER_CHP_FILE_PATH = "/arraydata/affymetrix/E10003_T_Hind.CN4.cnchp";
     public static final File HIND_COPY_NUMBER_CHP_FILE = getFile(HIND_COPY_NUMBER_CHP_FILE_PATH);
     public static final String XBA_COPY_NUMBER_CHP_FILE_PATH = "/arraydata/affymetrix/E07733_T_Xba.CN4.cnchp";
     public static final File XBA_COPY_NUMBER_CHP_FILE = getFile(XBA_COPY_NUMBER_CHP_FILE_PATH);
     
     public static final String AGILENT_COPY_NUMBER_DATA_FILE_PATH = "/arraydata/agilent/mskcc.org_OV.HG-CGH-244A.9.data.txt";
     public static final File AGILENT_COPY_NUMBER_DATA_FILE = getFile(AGILENT_COPY_NUMBER_DATA_FILE_PATH);
 
     public static final String REMBRANDT_COPY_NUMBER_FILE_PATH = "/rembrandt_copy_number_mapping.csv";
     public static final File REMBRANDT_COPY_NUMBER_FILE = getFile(REMBRANDT_COPY_NUMBER_FILE_PATH);
 
     public static final String SHORT_COPY_NUMBER_FILE_PATH = "/short_copy_number_mapping.csv";
     public static final File SHORT_COPY_NUMBER_FILE = getFile(SHORT_COPY_NUMBER_FILE_PATH);
     
     public static final String SHORT_AGILENT_COPY_NUMBER_FILE_PATH = "/short_agilent_copy_number_mapping.csv";
     public static final File SHORT_AGILENT_COPY_NUMBER_FILE = getFile(SHORT_AGILENT_COPY_NUMBER_FILE_PATH);
 
     public static final String TCGA_AGILENT_COPY_NUMBER_MAPPING_FILE_PATH = "/tcga_agilent_copy_number_mapping.csv";
     public static final File TCGA_AGILENT_COPY_NUMBER_MAPPING_FILE = getFile(TCGA_AGILENT_COPY_NUMBER_MAPPING_FILE_PATH);
     
     public static final String TCGA_LEVEL_2_DATA_FILE_PATH = "/arraydata/agilent/tcga_level_2_test_data.txt";
     public static final File TCGA_LEVEL_2_DATA_FILE = getFile(TCGA_LEVEL_2_DATA_FILE_PATH);
     
     public static final String HUAITIAN_LEVEL_2_DATA_FILE_PATH = "/arraydata/agilent/Huaitian_level2_test_data.txt";
     public static final File HUAITIAN_LEVEL_2_DATA_FILE = getFile(HUAITIAN_LEVEL_2_DATA_FILE_PATH);
     
     // Analysis Data Files
     // CMS
     public static final String CMS_CLASSIFICATIONS_FILE_PATH = "/cms_classifications.cls";
     public static final File CMS_CLASSIFICATIONS_FILE = getFile(CMS_CLASSIFICATIONS_FILE_PATH);
     
     public static final String CMS_GCT_FILE_PATH = "/cms_features.gct";
     public static final File CMS_GCT_FILE = getFile(CMS_GCT_FILE_PATH);
     
     // PCA
     public static final String PCA_TRAIN_FILE_PATH = "/pca_train.gct";
     public static final File PCA_TRAIN_FILE = getFile(PCA_TRAIN_FILE_PATH);
     
     // GISTIC
     public static final String GISTIC_CNV_FILE_PATH = "/gistic_cnv.txt";
     public static final File GISTIC_CNV_FILE = getFile(GISTIC_CNV_FILE_PATH);
     
     public static final String GISTIC_MARKERS_FILE_PATH = "/gistic_markers.txt";
     public static final File GISTIC_MARKERS_FILE = getFile(GISTIC_MARKERS_FILE_PATH);
     
     public static final String GISTIC_SAMPLES_FILE_PATH = "/gistic_samples.txt";
     public static final File GISTIC_SAMPLES_FILE = getFile(GISTIC_SAMPLES_FILE_PATH);
     
     public static File getAffymetrixDataFile(String filename) {
         return getFile(AFFYMETRIX_DATA_FILE_DIRECTORY_PATH + "/" + filename);
     }
     
     public static File getAgilentDataFile(String filename) {
         return getFile(AGILENT_DATA_FILE_DIRECTORY_PATH + "/" + filename);
     }
     
     public static File getFile(String resourcePath) {
         return new File(TestDataFiles.class.getResource(resourcePath).getFile());
     }
 
 
 }
