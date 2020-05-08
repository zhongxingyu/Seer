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
 package gov.nih.nci.caintegrator2.external.ncia;
 
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.ncia.domain.Image;
 import gov.nih.nci.ncia.domain.Patient;
 import gov.nih.nci.ncia.domain.Series;
 import gov.nih.nci.ncia.domain.Study;
 
 import java.util.List;
 
 /**
  * Sends off queries to the NCIA system and returns back the appropriate NCIA objects.
  */
 public interface NCIASearchService {
 
     /**
      * Retrieves a list of all different Projects from the CollectionName object.
      * @return List of Projects in String format.
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<String> retrieveAllCollectionNameProjects() throws ConnectionException;
 
     /**
      * Retrieves a list of all Patients given an Project.
      * @param collectionNameProject project to find the patients of.
      * @return List of Patients
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<Patient> retrievePatientCollectionFromCollectionNameProject(String collectionNameProject)
             throws ConnectionException;
     
     /**
      * Retrieves a list of all Patient Identifiers given an Project.
      * @param collectionNameProject project to find the patients of.
      * @return List of Patient Identifiers.
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<String> retrievePatientCollectionIdsFromCollectionNameProject(String collectionNameProject) 
         throws ConnectionException;
 
     /**
      * Retrieves a list of all Studies given a Patient ID.
      * @param patientId patient ID to find the studies for.
      * @return List of Studies
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<Study> retrieveStudyCollectionFromPatient(String patientId) throws ConnectionException;
     
     /**
      * Retrieves a list of all Study IDs given a Patient ID.
      * @param patientId patient ID to find the studies for.
      * @return List of Study IDs.
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<String> retrieveStudyCollectionIdsFromPatient(String patientId) throws ConnectionException;
 
     /**
      * Retrieves a list of all image series from a study.
      * @param studyInstanceUID study UID to retrieve image series from.
      * @return List of ImageSeries.
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<Series> retrieveImageSeriesCollectionFromStudy(String studyInstanceUID) throws ConnectionException;
     
     /**
      * Retrieves a list of all image series IDs from a study.
      * @param studyInstanceUID study UID to retrieve image series from.
      * @return List of ImageSeries IDs.
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<String> retrieveImageSeriesCollectionIdsFromStudy(String studyInstanceUID) throws ConnectionException;
 
     /**
      * Retrieves a list of all images given a series ID.
      * @param seriesInstanceUID series ID to find images from.
      * @return List of Image metadata
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<Image> retrieveImageCollectionFromSeries(String seriesInstanceUID) throws ConnectionException;
     
     /**
      * Retrieves a list of all images IDs given a series ID.
      * @param seriesInstanceUID series ID to find images from.
      * @return List of Image IDs.
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     List<String> retrieveImageCollectionIdsFromSeries(String seriesInstanceUID) throws ConnectionException;
     
     /**
      * Retrieves a representative image given a series ID.
      * @param seriesInstanceUID series ID to find image from.
      * @return Image metadata
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     Image retrieveRepresentativeImageBySeries(String seriesInstanceUID) throws ConnectionException;
 
     /**
      * Given a seriesInstanceUID we validate whether the series object exists or not.
      * @param seriesInstanceUID sent to see whether the UID is present or not 
      * @return true or false
      * @throws ConnectionException if there's a problem connecting to the NCIA server.
      */
     boolean validate(String seriesInstanceUID) throws ConnectionException;

    /**
     * Given a seriesInstanceUID, return all SopInstanceUID identifiers for that seriesInstanceUID. 
     * @param seriesInstanceUID as a string
     * @return String array of SopInstanceUID identifiers
     * @throws ConnectionException 
     */
    String[] getSOPInstanceUIDsFromSeriesInstanceUIDNew(String string) throws ConnectionException;
 }
