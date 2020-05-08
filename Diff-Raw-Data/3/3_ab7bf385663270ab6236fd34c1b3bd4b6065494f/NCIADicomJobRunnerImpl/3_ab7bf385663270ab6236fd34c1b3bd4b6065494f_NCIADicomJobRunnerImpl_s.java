 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caArray
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caArray Software License (the License) is between NCI and You. You (or 
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
  * its rights in the caArray Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caArray Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator Software and any 
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
 
 import gov.nih.nci.cagrid.ncia.client.NCIACoreServiceClient;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.file.FileManager;
 import gov.nih.nci.ivi.utils.ZipEntryInputStream;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.RemoteException;
 import java.util.zip.ZipInputStream;
 
 import org.apache.axis.types.URI.MalformedURIException;
 import org.cagrid.transfer.context.client.TransferServiceContextClient;
 import org.cagrid.transfer.context.client.helper.TransferClientHelper;
 import org.cagrid.transfer.context.stubs.types.TransferServiceContextReference;
 
 /**
  * Class to deal with retrieving and temporarily storing DICOM files from NCIA through the grid.
  */
 public class NCIADicomJobRunnerImpl implements NCIADicomJobRunner {
     
     private static final Integer BUFFER_SIZE = 8192;
     private static final String REMOTE_CONNECTION_FAILED = "Remote Connection Failed.";
     private static final String IMAGE_SERIES_DIR = "SERIES_";
     private static final String IMAGE_STUDY_DIR = "STUDY_";
     private final File temporaryStorageDirectory;
     private final NCIADicomJob job;
     
     /**
      * Public Constructor.
      * @param fileManager determines where to place the temporary storage directory.
      * @param job task that needs to run.
      */
     public NCIADicomJobRunnerImpl(FileManager fileManager, NCIADicomJob job) {
         temporaryStorageDirectory = fileManager.getNewTemporaryDirectory(job.getJobId());
         this.job = job;
     }
     
     /**
      * {@inheritDoc}
      */
     public File retrieveDicomFiles() throws ConnectionException {
         if (!job.hasData()) {
             return null;
         }
         try {
             NCIACoreServiceClient client = new NCIACoreServiceClient(job.getServerConnection().getUrl());
             parseImageSeriesFromJob(client);
             parseImageStudyFromJob(client);
         } catch (MalformedURIException e) {
             throw new ConnectionException("Malformed URI.", e);
         } catch (RemoteException e) {
             throw new ConnectionException(REMOTE_CONNECTION_FAILED, e);
         } 
         job.setCompleted(true);
         try {
             return Cai2Util.zipAndDeleteDirectory(temporaryStorageDirectory.getCanonicalPath());
         } catch (IOException e) {
             return null;
         }
     }
 
     private void parseImageSeriesFromJob(NCIACoreServiceClient client) throws ConnectionException {
         if (!job.getImageSeriesIDs().isEmpty()) {
             for (String imageSeriesId : job.getImageSeriesIDs()) {
                 retrieveImageSeriesDicomFiles(client, imageSeriesId);
             }
         }
     }
     
     private void parseImageStudyFromJob(NCIACoreServiceClient client) throws ConnectionException {
         if (!job.getImageStudyIDs().isEmpty()) {
             for (String imageStudyId : job.getImageStudyIDs()) {
                 retrieveImageStudyDicomFiles(client, imageStudyId);
             }
         }
     }
 
     private void retrieveImageSeriesDicomFiles(NCIACoreServiceClient client, String imageSeriesUID) 
         throws ConnectionException {
         try {
             TransferServiceContextReference tscr = client.retrieveDicomDataBySeriesUID(imageSeriesUID);
             gridTransferDicomData(tscr, IMAGE_SERIES_DIR + imageSeriesUID);
         } catch (RemoteException e) {
             throw new ConnectionException(REMOTE_CONNECTION_FAILED, e);
         }
     }
 
     private void retrieveImageStudyDicomFiles(NCIACoreServiceClient client, String studyInstanceUID)
             throws ConnectionException {
         TransferServiceContextReference tscr;
         try {
             tscr = client.retrieveDicomDataByStudyUID(studyInstanceUID);
             gridTransferDicomData(tscr, IMAGE_STUDY_DIR + studyInstanceUID);
         } catch (RemoteException e) {
             throw new ConnectionException(REMOTE_CONNECTION_FAILED, e);
         }
     }
 
     private void gridTransferDicomData(TransferServiceContextReference tscr, String parentDir) 
         throws ConnectionException {
         try {
             TransferServiceContextClient tclient = new TransferServiceContextClient(tscr.getEndpointReference());
             InputStream istream = TransferClientHelper.getData(tclient.getDataTransferDescriptor());
             storeDicomFiles(istream, parentDir);
             tclient.destroy();
         } catch (MalformedURIException e) {
             throw new ConnectionException("Malformed URI.", e);
         } catch (RemoteException e) {
             throw new ConnectionException(REMOTE_CONNECTION_FAILED, e);
         } catch (Exception e) {
             throw new ConnectionException("Unable to get dicom data from Transfer Client.", e);
         }
     }
     
     private void storeDicomFiles(InputStream istream, String parentDir) throws IOException {
         File dicomDirectory = new File(temporaryStorageDirectory, parentDir);
         dicomDirectory.mkdir();
         ZipInputStream zis = new ZipInputStream(istream);
         ZipEntryInputStream zeis = null;
         while (true) {
             try {
                 zeis = new ZipEntryInputStream(zis);
             } catch (EOFException e) {
                 break;
             } catch (IOException e) {
                 break;
             }
             BufferedInputStream bis = new BufferedInputStream(zeis);
             byte[] data = new byte[BUFFER_SIZE];
             int bytesRead = 0;
             File dicomFile = new File(dicomDirectory, zeis.getName());
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dicomFile));
             while ((bytesRead = (bis.read(data, 0, data.length))) > 0) {
                 bos.write(data, 0, bytesRead);
             }
             bos.flush();
             bos.close();
            zis.close();
         }
     }
 
 }
