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
 package gov.nih.nci.caintegrator2.application.analysis.grid.gistic;
 
 import gov.nih.nci.caintegrator2.common.CaGridUtil;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.ParameterException;
 import gov.nih.nci.caintegrator2.file.FileManager;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.cagrid.transfer.context.client.TransferServiceContextClient;
 import org.cagrid.transfer.context.client.helper.TransferClientHelper;
 import org.cagrid.transfer.context.stubs.types.TransferServiceContextReference;
 import org.cagrid.transfer.descriptor.Status;
 import org.genepattern.cabig.util.ZipUtils;
 import org.genepattern.gistic.common.GisticI;
 import org.genepattern.gistic.context.client.GisticContextClient;
 import org.genepattern.gistic.context.stubs.types.AnalysisNotComplete;
 import org.genepattern.gistic.context.stubs.types.CannotLocateResource;
 import org.genepattern.gistic.stubs.types.InvalidParameterException;
 import org.oasis.wsrf.faults.BaseFaultTypeDescription;
 
 /**
  * Runs the GenePattern grid service Gistic.
  */
 public class GisticGridRunner {
     
     private static final Logger LOGGER = Logger.getLogger(GisticGridRunner.class);
     private static final int DOWNLOAD_REFRESH_INTERVAL = 5000; // Every 5 seconds
     private static final int TIMEOUT_SECONDS = 7200; // For 3600 total Seconds (60 total minutes)
     private final GisticI client;
     private final FileManager fileManager;
     
     /**
      * Public Constructor.
      * @param client of grid service.
      * @param fileManager to store results zip file.
      */
     public GisticGridRunner(GisticI client, FileManager fileManager) {
         this.client = client;
         this.fileManager = fileManager;
     }
 
     
     /**
      * Runs GISTIC based on input parameters.
      * @param studySubscription study to save downloaded file to.
      * @param parameters from user input.
      * @param segmentFile for segmentation data.
      * @param markersFile for marker data.
      * @param cnvFile for cnv.
      * @return Gistic Results.
      * @throws ParameterException if have invalid parameter.
      * @throws ConnectionException if unable to connect.
      * @throws IOException if there's a problem saving files.
      * @throws InterruptedException if execution is interrupted.
      */
     public File execute(StudySubscription studySubscription, 
             GisticParameters parameters, File segmentFile, File markersFile, File cnvFile) 
     throws ParameterException, ConnectionException, IOException, InterruptedException {
         try {
             GisticContextClient analysisClient = client.createAnalysis(); 
             postUpload(analysisClient, parameters, segmentFile, markersFile, cnvFile);
             return downloadResult(studySubscription, analysisClient);
         } catch (InvalidParameterException e) {
             throw new ParameterException("The given parameters were invalid: " + getDescription(e), e);
         } catch (RemoteException e) {
             LOGGER.error("Couldn't complete GISTIC job", e);
             throw new ConnectionException("Unable to connect to server.", e);
         } catch (MalformedURIException e) {
             throw new ConnectionException("Malformed URI.", e);
         } 
     }
 
     private void postUpload(GisticContextClient analysisClient, 
             GisticParameters parameters, File segmentFile, File markersFile, File cnvFile)
             throws IOException, ConnectionException {
         TransferServiceContextReference up = analysisClient.submitData(parameters.createParameterList(), 
                                                                        parameters.createGenomeBuild());
         TransferServiceContextClient tClient = new TransferServiceContextClient(up.getEndpointReference());
         BufferedInputStream bis = null;
         Set<File> fileSet = createGisticInputFileSet(segmentFile, markersFile, cnvFile);
         File zipFile = new File(ZipUtils.writeZipFile(fileSet));
         try {
             long size = zipFile.length();
             bis = new BufferedInputStream(new FileInputStream(zipFile));
             TransferClientHelper.putData(bis, size, tClient.getDataTransferDescriptor());
         } catch (Exception e) {
             // For some reason TransferClientHelper throws "Exception", going to rethrow a connection exception.
             throw new ConnectionException("Unable to transfer data to the server.", e);
         } finally {
             if (bis != null) {
                 bis.close();
             }
             for (File file : fileSet) {
                 FileUtils.deleteQuietly(file);
             }
             FileUtils.deleteQuietly(zipFile);
         }
         tClient.setStatus(Status.Staged);
     }
 
     private Set<File> createGisticInputFileSet(File segmentFile, File markersFile, File cnvFile)
             throws IOException {
         Set<File> fileSet = new HashSet<File>();
         fileSet.add(markersFile);
         fileSet.add(segmentFile);
         if (cnvFile != null) {
             fileSet.add(cnvFile);
         }
         return fileSet;
     }
     
     private File downloadResult(StudySubscription studySubscription, GisticContextClient analysisClient) 
         throws ConnectionException, InterruptedException, MalformedURIException, RemoteException {
         String filename = new File(fileManager.getUserDirectory(studySubscription) + File.separator
                 + "GISTIC_RESULTS_" + System.currentTimeMillis() + ".zip").getAbsolutePath();
         TransferServiceContextReference tscr = null;
         int callCount = 0;
        String hostInfo = analysisClient.getEndpointReference().getAddress().getHost().toString()
         + ":"
         + analysisClient.getEndpointReference().getAddress().getPort()
         + analysisClient.getEndpointReference().getAddress().getPath();
         while (tscr == null) {
             try {
                 callCount++;
                 tscr = analysisClient.getResult();
             } catch (AnalysisNotComplete e) {
                 LOGGER.info("GISTIC - Attempt # " + callCount + " to host: " + hostInfo
                         + " - Analysis not complete");
                 checkTimeout(callCount);
             } catch (CannotLocateResource e) {
                 LOGGER.info("GISTIC - Attempt # " + callCount + " to host: " + hostInfo
                         + " - Cannot locate resource");
                 checkTimeout(callCount);
             } catch (RemoteException e) {
                 throw new ConnectionException("Unable to connect to server to download result.", e);
             }
             Thread.sleep(DOWNLOAD_REFRESH_INTERVAL);
         }
         return CaGridUtil.retrieveFileFromTscr(filename, tscr);
     }
     
     private void checkTimeout(int callCount) throws ConnectionException {
         if (callCount >= TIMEOUT_SECONDS) {
             throw new ConnectionException("Timed out trying to download GISTIC results");
         }
     }
 
     private String getDescription(InvalidParameterException e) {
         StringBuffer description = new StringBuffer();
         for (BaseFaultTypeDescription typeDescription : e.getDescription()) {
             if (description.length() > 0) {
                 description.append(", ");
             }
             description.append(typeDescription.get_value());
         }
         return description.toString();
     }
 
 }
