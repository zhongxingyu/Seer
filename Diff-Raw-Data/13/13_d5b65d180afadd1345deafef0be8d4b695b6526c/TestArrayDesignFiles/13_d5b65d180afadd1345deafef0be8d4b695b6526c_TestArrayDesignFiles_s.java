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
 
import static gov.nih.nci.caintegrator2.TestDataFiles.getFile;

 import java.io.File;
 
 public class TestArrayDesignFiles {
 
     public static final String EMPTY_PATH = "/arraydesign/affymetrix/emptyfile.csv";
     public static final File EMPTY_FILE = getFile(EMPTY_PATH);
     
     // Affymetrix files
     public static final String HG_U133_PLUS_2_CDF_PATH = "/arraydesign/affymetrix/HG-U133_Plus_2.cdf";
     public static final String HG_U133A_CDF_PATH = "/arraydesign/affymetrix/HG-U133A.cdf";
     public static final String HG_U133_PLUS_2_ANNOTATION_PATH = "/arraydesign/affymetrix/HG-U133_Plus_2.na26.annot.csv";
     public static final String HG_U133A_ANNOTATION_PATH = "/arraydesign/affymetrix/HG-U133A.na27.annot.csv";
     public static final String YEAST_2_CDF_PATH = "/arraydesign/affymetrix/Yeast_2.cdf";
     public static final String YEAST_2_ANNOTATION_PATH = "/arraydesign/affymetrix/Yeast_2.na26.annot.csv";
     public static final String TEST3_ANNOTATION_PATH = "/arraydesign/affymetrix/Test3.annot.csv";
     public static final String HG_U133B_ANNOTATION_PATH = "/arraydesign/affymetrix/HG-U133B.na27.annot.csv";
     public static final String HG_U95AV2_ANNOTATION_PATH = "/arraydesign/affymetrix/HG_U95Av2.na27.annot.csv";
     public static final String HG_U95B_ANNOTATION_PATH = "/arraydesign/affymetrix/HG_U95B.na27.annot.csv";
     public static final String HG_U95C_ANNOTATION_PATH = "/arraydesign/affymetrix/HG_U95C.na27.annot.csv";
     public static final String HG_U95D_ANNOTATION_PATH = "/arraydesign/affymetrix/HG_U95D.na27.annot.csv";
     public static final String HG_U133B_CDF_PATH = "/arraydesign/affymetrix/HG-U133B.cdf";
     public static final String HG_U95AV2_CDF_PATH = "/arraydesign/affymetrix/HG_U95Av2.CDF";
     public static final String HG_U95B_CDF_PATH = "/arraydesign/affymetrix/HG_U95B.CDF";
     public static final String HG_U95C_CDF_PATH = "/arraydesign/affymetrix/HG_U95C.CDF";
     public static final String HG_U95D_CDF_PATH = "/arraydesign/affymetrix/HG_U95D.CDF";
     public static final String MAPPING_50K_HIND_ANNOTATION_PATH = "/arraydesign/affymetrix/Mapping50K_Hind240.na27.annot.csv";
     public static final String MAPPING_50K_HIND_CDF_PATH = "/arraydesign/affymetrix/Mapping50K_Hind240.cdf";
     public static final String MAPPING_50K_XBA_CDF_PATH = "/arraydesign/affymetrix/Mapping50K_Xba240.CDF";
     public static final String MAPPING_50K_XBA_ANNOTATION_PATH = "/arraydesign/affymetrix/Mapping50K_Xba240.na27.annot.csv";
     
     public static final File HG_U133A_CDF_FILE = getFile(HG_U133A_CDF_PATH);
     public static final File HG_U133A_ANNOTATION_FILE = getFile(HG_U133A_ANNOTATION_PATH);
     public static final File HG_U133_PLUS_2_CDF_FILE = getFile(HG_U133_PLUS_2_CDF_PATH);
     public static final File HG_U133_PLUS_2_ANNOTATION_FILE = getFile(HG_U133_PLUS_2_ANNOTATION_PATH);
     public static final File YEAST_2_ANNOTATION_FILE = getFile(YEAST_2_ANNOTATION_PATH);
     public static final File YEAST_2_CDF_FILE = getFile(YEAST_2_CDF_PATH);
     public static final File TEST3_ANNOTATION_FILE = getFile(TEST3_ANNOTATION_PATH);
     public static final File HG_U133B_ANNOTATION_FILE = getFile(HG_U133B_ANNOTATION_PATH);
     public static final File HG_U95AV2_ANNOTATION_FILE = getFile(HG_U95AV2_ANNOTATION_PATH);
     public static final File HG_U95B_ANNOTATION_FILE = getFile(HG_U95B_ANNOTATION_PATH);
     public static final File HG_U95C_ANNOTATION_FILE = getFile(HG_U95C_ANNOTATION_PATH);
     public static final File HG_U95D_ANNOTATION_FILE = getFile(HG_U95D_ANNOTATION_PATH);
     public static final File HG_U133B_CDF_FILE = getFile(HG_U133B_CDF_PATH);
     public static final File HG_U95AV2_CDF_FILE = getFile(HG_U95AV2_CDF_PATH);
     public static final File HG_U95B_CDF_FILE = getFile(HG_U95B_CDF_PATH);
     public static final File HG_U95C_CDF_FILE = getFile(HG_U95C_CDF_PATH);
     public static final File HG_U95D_CDF_FILE = getFile(HG_U95D_CDF_PATH);
     public static final File MAPPING_50K_HIND_ANNOTATION_FILE = getFile(MAPPING_50K_HIND_ANNOTATION_PATH);
     public static final File MAPPING_50K_HIND_CDF_FILE = getFile(MAPPING_50K_HIND_CDF_PATH);
     public static final File MAPPING_50K_XBA_CDF = getFile(MAPPING_50K_XBA_CDF_PATH);
     public static final File MAPPING_50K_XBA_ANNOTATION_FILE = getFile(MAPPING_50K_XBA_ANNOTATION_PATH);
 
 
     // Agilent files
     public static final String HUMAN_GENOME_CGH244A_ANNOTATION_PATH = "/arraydesign/agilent/014693_D_GeneList_20070207.txt";
     public static final String AGILENT_G4502A_07_01_TCGA_ADF_ANNOTATION_PATH = "/arraydesign/agilent/AgilentG4502A_07_01.tcga.adf";
     public static final File HUMAN_GENOME_CGH244A_ANNOTATION_FILE = getFile(HUMAN_GENOME_CGH244A_ANNOTATION_PATH);
     public static final File AGILENT_G4502A_07_01_TCGA_ADF_ANNOTATION_FILE = getFile(AGILENT_G4502A_07_01_TCGA_ADF_ANNOTATION_PATH);
 
 }
