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
 package gov.nih.nci.caintegrator2.application.analysis.grid;
 
 import gov.nih.nci.cagrid.metadata.exceptions.ResourcePropertyRetrievalException;
 import gov.nih.nci.caintegrator2.common.ConfigurationHelper;
 import gov.nih.nci.caintegrator2.common.ConfigurationParameter;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.log4j.Logger;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.springframework.scheduling.quartz.QuartzJobBean;
 
 /**
  * Grid discovery job to query available services.
  */
 @SuppressWarnings("PMD.CyclomaticComplexity") // Check for multiple grid services
 public class GridDiscoveryServiceJob extends QuartzJobBean {
 
     private static final Logger LOGGER = Logger.getLogger(GridDiscoveryServiceJob.class);
 
     private static GridDiscoveryClient gridDiscoveryClient;
     private static ConfigurationHelper configurationHelper;
     private static Map<String, String> gridNbiaServices
         = Collections.synchronizedMap(new HashMap<String, String>());
     private static Map<String, String> gridPreprocessServices
         = Collections.synchronizedMap(new HashMap<String, String>());
     private static Map<String, String> gridCmsServices
         = Collections.synchronizedMap(new HashMap<String, String>());
     private static Map<String, String> gridPcaServices
         = Collections.synchronizedMap(new HashMap<String, String>());
     private static Map<String, String> gridCaDnaCopyServices 
         = Collections.synchronizedMap(new HashMap<String, String>());
     private static Map<String, String> gridGisticServices 
         = Collections.synchronizedMap(new HashMap<String, String>());
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void executeInternal(JobExecutionContext context)
         throws JobExecutionException {
         queryGridServices();
     }
     
     private static void queryGridServices() {
         try {
             // Get all services
             EndpointReferenceType[] searchedServices = gridDiscoveryClient.getServices();
             if (searchedServices != null) {
                 clearServices();
                 for (EndpointReferenceType epr : searchedServices) {
                     extractSelectedServices(epr);
                 }
             }
             setDefaultServices();
         } catch (MalformedURIException e) {
             LOGGER.error("Error getting directory from GridDiscoveryClient", e);
             setDefaultServices();
         } catch (ResourcePropertyRetrievalException e) {
             LOGGER.error("Error getting directory from GridDiscoveryClient", e);
             setDefaultServices();
         }
     }
 
     private static void extractSelectedServices(EndpointReferenceType epr)  {
         String url = gridDiscoveryClient.getAddress(epr);
         String hostingCenter = "Unknown hosting center";
         try {
             hostingCenter = gridDiscoveryClient.getHostinCenter(epr);
         } catch (ResourcePropertyRetrievalException e) {
             LOGGER.warn("Error getting hosting center for grid service URL" + url + ": " + e.getMessage());
         }
         extractSelectedServices(hostingCenter, url);
     }
     
     private static void clearServices() {
         gridNbiaServices.clear();
         gridPreprocessServices.clear();
         gridCmsServices.clear();
         gridPcaServices.clear();
         gridCaDnaCopyServices.clear();
         gridGisticServices.clear();
     }
     
     private static void setDefaultServices() {
         setDefaultNbiaService();
         setDefaultPreprocessService();
         setDefaultCmsService();
         setDefaultPcaService();
         setDefaultCaDnaCopyService();
         setDefaultGisticService();
     }
     
     private static void setDefaultNbiaService() {
         if (gridNbiaServices.isEmpty()) {
             String defaultUrl = configurationHelper.getString(ConfigurationParameter.NBIA_URL);
             gridNbiaServices.put(defaultUrl, "Default NBIA service - " + defaultUrl);
         }
     }
     
     private static void setDefaultPreprocessService() {
         if (gridPreprocessServices.isEmpty()) {
             String defaultUrl = configurationHelper.getString(ConfigurationParameter.PREPROCESS_DATASET_URL);
             gridPreprocessServices.put(defaultUrl, "Default Broad service - " + defaultUrl);
         }
     }
     
     private static void setDefaultCmsService() {
         if (gridCmsServices.isEmpty()) {
             String defaultUrl = configurationHelper.getString(ConfigurationParameter.COMPARATIVE_MARKER_SELECTION_URL);
             gridCmsServices.put(defaultUrl, "Default Broad service - " + defaultUrl);
         }
     }
 
     private static void setDefaultPcaService() {
         if (gridPcaServices.isEmpty()) {
             String defaultUrl = configurationHelper.getString(ConfigurationParameter.PCA_URL);
             gridPcaServices.put(defaultUrl, "Default Broad service - " + defaultUrl);
         }
     }
     
     private static void setDefaultCaDnaCopyService() {
         if (gridCaDnaCopyServices.isEmpty()) {
             String defaultUrl = configurationHelper.getString(ConfigurationParameter.CA_DNA_COPY_URL);
             gridCaDnaCopyServices.put(defaultUrl, "Default Bioconductor service - " + defaultUrl);
         }
     }
     
     private static void setDefaultGisticService() {
         if (gridGisticServices.isEmpty()) {
             String defaultUrl = configurationHelper.getString(ConfigurationParameter.GISTIC_URL);
             gridGisticServices.put(defaultUrl, "Default GISTIC service - " + defaultUrl);
         }
     }
 
     @SuppressWarnings("PMD.CyclomaticComplexity") // Check for multiple grid services
     private static void extractSelectedServices(String hostingCenter, String url) {
         if (url.contains("MAGES")) {
             extractCmsServices(hostingCenter, url);
         } else if (shouldAdd(url, "PCA", gridPcaServices)) {
             gridPcaServices.put(url, buildDisplayName(hostingCenter, url));
         } else if (shouldAdd(url, "CaDNAcopy", gridCaDnaCopyServices)) {
             gridCaDnaCopyServices.put(url, buildDisplayName(hostingCenter, url));
         } else if (shouldAdd(url, "Gistic", gridGisticServices)) {
             gridGisticServices.put(url, buildDisplayName(hostingCenter, url));
         } else if (shouldAdd(url, "NCIA", gridNbiaServices)) {
            gridNbiaServices.put(url, buildDisplayName(hostingCenter, url));
         }
     }
 
     private static String buildDisplayName(String hostingCenter, String url) {
         return hostingCenter + " - " + url;
     }
 
     private static boolean shouldAdd(String url, String serviceName, Map<String, String> serviceMap) {
         return url.contains(serviceName) && !serviceMap.containsKey(url);
     }
     
     private static void extractCmsServices(String hostingCenter, String url) {
         if (url.contains("Comparative")
                 && !gridCmsServices.containsKey(url)) {
             gridCmsServices.put(url, buildDisplayName(hostingCenter, url));
         } else if (url.contains("Preprocess")
                 && !gridPreprocessServices.containsKey(url)) {
             gridPreprocessServices.put(url, buildDisplayName(hostingCenter, url));
         }
     }
 
     /**
      * @return the gridNbiaServices
      */
     public static Map<String, String> getGridNbiaServices() {
         if (gridNbiaServices.isEmpty()) {
             setDefaultNbiaService();
         }
         return gridNbiaServices;
     }
 
     /**
      * @return the gridPreprocessServices
      */
     public static Map<String, String> getGridPreprocessServices() {
         if (gridPreprocessServices.isEmpty()) {
             setDefaultPreprocessService();
         }
         return gridPreprocessServices;
     }
 
     /**
      * @return the gridCmsServices
      */
     public static Map<String, String> getGridCmsServices() {
         if (gridCmsServices.isEmpty()) {
             setDefaultCmsService();
         }
         return gridCmsServices;
     }
 
     /**
      * @return the gridPcaServices
      */
     public static Map<String, String> getGridPcaServices() {
         if (gridPcaServices.isEmpty()) {
             setDefaultPcaService();
         }
         return gridPcaServices;
     }
 
     /**
      * @return the gridCaDnaCopyServices
      */
     public static Map<String, String> getGridCaDnaCopyServices() {
         if (gridCaDnaCopyServices.isEmpty()) {
             setDefaultCaDnaCopyService();
         }
         return gridCaDnaCopyServices;
     }
 
     /**
      * @return the gridGisticServices
      */
     public static Map<String, String> getGridGisticServices() {
         if (gridGisticServices.isEmpty()) {
             setDefaultGisticService();
         }
         return gridGisticServices;
     }
 
     /**
      * @param gridDiscoveryClient the gridDiscoveryClient to set
      */
     public void setGridDiscoveryClient(GridDiscoveryClient gridDiscoveryClient) {
         GridDiscoveryServiceJob.gridDiscoveryClient = gridDiscoveryClient;
     }
 
     /**
      * @param configurationHelper the configurationHelper to set
      */
     public void setConfigurationHelper(ConfigurationHelper configurationHelper) {
         GridDiscoveryServiceJob.configurationHelper = configurationHelper;
     }
 
 }
