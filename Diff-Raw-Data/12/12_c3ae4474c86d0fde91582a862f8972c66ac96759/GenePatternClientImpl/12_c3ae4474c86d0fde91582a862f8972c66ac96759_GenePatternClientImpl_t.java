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
 package edu.mit.broad.genepattern.gp.services;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.xml.rpc.ServiceException;
 
 import org.apache.commons.io.IOUtils;
 
 /**
  * Wrapper around the GenePattern web service.
  */
 public class GenePatternClientImpl implements GenePatternClient {
     
     private static final String CLIENT_FILENAME_ATTRIBUTE = "client_filename";
     private static final String MODE_ATTRIBUTE = "MODE";
     private static final String TYPE_ATTRIBUTE = "TYPE";
     private static final String INPUT_MODE = "IN";
     private static final String URL_INPUT_MODE = "URL_IN";
     private static final String FILE_TYPE = "FILE";
     private Analysis analysis;
     private Map<String, TaskInfo> taskMap;
     private String url;
     private String username;
     private String password;
 
     private Analysis getAnalysis() throws GenePatternServiceException {
         if (analysis == null) {
             analysis = openAnalysisSerivce();
         }
         return analysis;
     }
 
     private void closeAnalysisService() {
         analysis = null;
         taskMap = null;
     }
 
     private Analysis openAnalysisSerivce() throws GenePatternServiceException {
         if (url == null || username == null) {
             throw new IllegalStateException("Must set URL and username prior to using GenePattern service");
         }
         try {
             AnalysisService service = new AnalysisServiceLocator();
             URL address = new URL(url);
             Analysis analysis = service.getAnalysis(address);
             ((AnalysisSoapBindingStub) analysis).setUsername(username);
             ((AnalysisSoapBindingStub) analysis).setPassword(password);
             ((AnalysisSoapBindingStub) analysis).setMaintainSession(true);
             return analysis;
         } catch (ServiceException e) {
             throw new GenePatternServiceException(e);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("Bad URL " + url, e);
         }
     }
     
     /**
      * Returns the task information for the requested task.
      * 
      * @param name name of the task to retrieve.
      * @return the information.
      * @throws GenePatternServiceException if there's a problem communicating with the service.
      */
     public TaskInfo getTaskInfo(String name) throws GenePatternServiceException {
         return getTaskMap().get(name);
     }
     
     private Map<String, TaskInfo> getTaskMap() throws GenePatternServiceException {
         if (taskMap == null) {
             loadTaskMap();
         }
         return taskMap;
     }
 
     private void loadTaskMap() throws GenePatternServiceException {
         taskMap = new HashMap<String, TaskInfo>();
         for (TaskInfo taskInfo : getTasksOld()) {
             taskMap.put(taskInfo.getName(), taskInfo);
         }
     }
     
     /**
      * Returns information for all available tasks
      * 
      * @return information on all tasks.
      * @throws GenePatternServiceException if there's a problem communicating with the service.
      */
     public TaskInfo[] getTasksOld() throws GenePatternServiceException {
         try {
             return getAnalysis().getTasks();
         } catch (WebServiceException e) {
             throw new GenePatternServiceException(e);
         } catch (RemoteException e) {
             throw new GenePatternServiceException(e);
         }
     }
     
     /**
      * Runs the given job.
      * 
      * @param taskName the name of the analysis method
      * @param parameters the parameters to submit
      * @return the job information
      * @throws GenePatternServiceException if there's a failure communicating with GenePattern
      */
    public JobInfo runAnalysisCai2(String taskName, List<ParameterInfo> parameters) throws GenePatternServiceException {
         TaskInfo taskInfo = getTaskInfo(taskName);
         ParameterInfo[] fullJobParameters = getFullJobParameters(parameters, taskInfo);
         HashMap<String, DataHandler> fileDataHandlers = getFileDataHandlers(fullJobParameters);
         try {
             return getAnalysis().submitJob(taskInfo.getID(), fullJobParameters, fileDataHandlers);
         } catch (WebServiceException e) {
             throw new GenePatternServiceException(e);
         } catch (RemoteException e) {
             throw new GenePatternServiceException(e);
         }
     }
 
     private HashMap<String, DataHandler> getFileDataHandlers(ParameterInfo[] parameters) {
         HashMap<String, DataHandler> handlers = new HashMap<String, DataHandler>();
         for (ParameterInfo parameter : parameters) {
             if (isFileParameter(parameter)) {
                 DataHandler handler = getFileDataHandler(parameter);
                 handlers.put(handler.getName(), handler);
             }
         }
         return handlers;
     }
 
     @SuppressWarnings("unchecked")  // parameter attributes is untyped
     private boolean isFileParameter(ParameterInfo parameter) {
         Map attributes = parameter.getAttributes();
         return attributes != null
             && FILE_TYPE.equals(attributes.get(TYPE_ATTRIBUTE))
             && INPUT_MODE.equals(attributes.get(MODE_ATTRIBUTE));
     }
 
     private DataHandler getFileDataHandler(ParameterInfo parameter) {
         String filePath = parameter.getValue();
         FileDataSource fileDataSource = new FileDataSource(filePath) {
             @Override
             public String getName() {
                 return getFile().getName();
             }
         };
         DataHandler handler = new DataHandler(fileDataSource);
         parameter.setValue(fileDataSource.getName());
         return handler;
     }
 
     private ParameterInfo[] getFullJobParameters(List<ParameterInfo> parameters, TaskInfo taskInfo) {
         List<ParameterInfo> fullParameters = new ArrayList<ParameterInfo>();
         Map<String, ParameterInfo> templateParameterMap = getTemplateParameterMap(taskInfo);
         addUserSuppliedParameters(fullParameters, parameters, templateParameterMap);
         addMissingParameters(fullParameters, templateParameterMap);
         return fullParameters.toArray(new ParameterInfo[fullParameters.size()]);
     }
 
     private void addUserSuppliedParameters(List<ParameterInfo> fullParameters, List<ParameterInfo> userParameters,
             Map<String, ParameterInfo> templateParameterMap) {
         for (ParameterInfo userParameter : userParameters) {
             ParameterInfo parameterTemplate = templateParameterMap.remove(userParameter.getName());
             ParameterInfo newParameter = new ParameterInfo();
             newParameter.setName(userParameter.getName());
             newParameter.setValue(getValue(userParameter, parameterTemplate));
             if (parameterTemplate.isInputFile()) {
                 handleInputFileParameter(newParameter, parameterTemplate);
             }
             fullParameters.add(newParameter);
         }
     }
 
     private void handleInputFileParameter(ParameterInfo parameter, ParameterInfo parameterTemplate) {
         HashMap<String, String> attributes = new HashMap<String, String>();
         parameter.setAttributes(attributes);
         attributes.put(CLIENT_FILENAME_ATTRIBUTE, parameter.getValue());
         parameter.setInputFile(true);
         if (parameter.getValue() != null && new File(parameter.getValue()).exists()) {
             attributes.put(TYPE_ATTRIBUTE, FILE_TYPE);
             attributes.put(MODE_ATTRIBUTE, INPUT_MODE);
         } else if (parameter.getValue() != null) {
             attributes.remove(TYPE_ATTRIBUTE);
             attributes.put(MODE_ATTRIBUTE, URL_INPUT_MODE);
         }
     }
 
     private String getValue(ParameterInfo userParameter, ParameterInfo taskParameter) {
         if (userParameter.getValue() != null) {
             return userParameter.getValue();
         } else
             return getDefaultValue(taskParameter);
     }
 
     private String getDefaultValue(ParameterInfo parameterTemplate) {
         if (parameterTemplate.getDefaultValue() == null && !parameterTemplate.isOptional()) {
             throw new IllegalArgumentException("No value supplied for required parameter " + parameterTemplate.getName());
         } else {
             return parameterTemplate.getDefaultValue();
         }
     }
 
     private void addMissingParameters(List<ParameterInfo> fullParameters, Map<String, ParameterInfo> templateParameterMap) {
         for (ParameterInfo parameterTemplate : templateParameterMap.values()) {
             ParameterInfo newParameter = new ParameterInfo();
             newParameter.setName(parameterTemplate.getName());
             newParameter.setValue(getDefaultValue(parameterTemplate));
             if (parameterTemplate.isInputFile()) {
                 handleInputFileParameter(newParameter, parameterTemplate);
             }
             fullParameters.add(newParameter);
         }
     }
 
     private Map<String, ParameterInfo> getTemplateParameterMap(TaskInfo taskInfo) {
         Map<String, ParameterInfo> templateParameterMap = new HashMap<String, ParameterInfo>();
         for (ParameterInfo parameterInfo : taskInfo.getParameterInfoArray()) {
             templateParameterMap.put(parameterInfo.getName(), parameterInfo);
         }
         return templateParameterMap;
     }
 
     /**
      * @param url the url to set
      */
     public void setUrl(String url) {
         closeAnalysisService();
         this.url = url;
     }
 
     /**
      * @param username the username to set
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * @return the password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * @param password the password to set
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * {@inheritDoc}
      */
     public JobInfo getStatus(JobInfo jobInfo) throws GenePatternServiceException {
         try {
             return getAnalysis().checkStatus(jobInfo.getJobNumber());
         } catch (WebServiceException e) {
             throw new GenePatternServiceException(e);
         } catch (RemoteException e) {
             throw new GenePatternServiceException(e);
         } catch (GenePatternServiceException e) {
             throw new GenePatternServiceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FileWrapper[] getResultFiles(JobInfo jobInfo) throws GenePatternServiceException {
         return getResultFilesFromJobNumber(jobInfo.getJobNumber());
     }
 
     private FileWrapper[] getResultFilesFromJobNumber(int jobInfoNumber) throws GenePatternServiceException {
         try {
             return getAnalysis().getResultFiles(jobInfoNumber);
         } catch (WebServiceException e) {
             throw new GenePatternServiceException(e);
         } catch (RemoteException e) {
             throw new GenePatternServiceException(e);
         } catch (GenePatternServiceException e) {
             throw new GenePatternServiceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FileWrapper getResultFile(JobInfo jobInfo, String filename) throws GenePatternServiceException {
         return getResultFileFromJobNumber(jobInfo.getJobNumber(), filename);
     }
 
 
     private FileWrapper getResultFileFromJobNumber(int jobInfoNumber, String filename) throws GenePatternServiceException {
         try {
             FileWrapper[] wrappers = getAnalysis().getResultFiles(jobInfoNumber, new String[] {filename});
             if (wrappers.length == 0) {
                 return null;
             } else {
                 return wrappers[0];
             }
         } catch (WebServiceException e) {
             throw new GenePatternServiceException(e);
         } catch (RemoteException e) {
             throw new GenePatternServiceException(e);
         } catch (GenePatternServiceException e) {
             throw new GenePatternServiceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public File getResultFile(org.genepattern.webservice.JobInfo jobInfo, String filename)
             throws org.genepattern.webservice.WebServiceException {
         
         try {
             return createFileFromFileWrapper(getResultFileFromJobNumber(jobInfo.getJobNumber(), filename), null);
         } catch (Exception e) {
             throw new org.genepattern.webservice.WebServiceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public File[] getResultFiles(org.genepattern.webservice.JobInfo jobInfo, File resultsDir)
             throws org.genepattern.webservice.WebServiceException, IOException {
         try {
             FileWrapper[] files = getResultFilesFromJobNumber(jobInfo.getJobNumber());
             File[] newFiles = new File[files.length];
             int ctr = 0;
             for (FileWrapper fileWrapper : files) {
                 newFiles[ctr] = createFileFromFileWrapper(fileWrapper, resultsDir);
                 ctr++;
             }
             return newFiles;
         } catch (GenePatternServiceException e) {
             throw new org.genepattern.webservice.WebServiceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public org.genepattern.webservice.JobInfo getStatus(org.genepattern.webservice.JobInfo jobInfo)
             throws org.genepattern.webservice.WebServiceException {
         try {
             return convertJobInfoToNewVersion(getAnalysis().checkStatus(jobInfo.getJobNumber()));
         } catch (Exception e) {
             throw new org.genepattern.webservice.WebServiceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public org.genepattern.webservice.JobInfo runAnalysis(String moduleName,
             List<org.genepattern.webservice.ParameterInfo> parameters)
             throws org.genepattern.webservice.WebServiceException {
         try {
            return convertJobInfoToNewVersion(runAnalysisCai2(moduleName, convertParametersToOldVersion(parameters)));
         } catch (GenePatternServiceException e) {
             throw new org.genepattern.webservice.WebServiceException(e);
         }
         
     }
 
     /**
      * {@inheritDoc}
      */
     public org.genepattern.webservice.TaskInfo[] getTasks() throws org.genepattern.webservice.WebServiceException {
         try {
             TaskInfo[] origTasks = getTasksOld();
             List<org.genepattern.webservice.TaskInfo> tasksList = new ArrayList<org.genepattern.webservice.TaskInfo>();
             for (TaskInfo taskInfo : origTasks) {
                 if (taskInfo != null) {
                     tasksList.add(convertTaskInfoToNewVersion(taskInfo));
                 }
             }
             return tasksList.toArray(new org.genepattern.webservice.TaskInfo[tasksList.size()]);
         } catch (GenePatternServiceException e) {
             throw new org.genepattern.webservice.WebServiceException(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     private org.genepattern.webservice.TaskInfo convertTaskInfoToNewVersion(TaskInfo taskInfo) {
         org.genepattern.webservice.TaskInfo newTaskInfo = new org.genepattern.webservice.TaskInfo();
         newTaskInfo.setAccessId(taskInfo.getAccessId());
         newTaskInfo.setAttributes(taskInfo.getAttributes());
         newTaskInfo.setDescription(taskInfo.getDescription());
         newTaskInfo.setID(taskInfo.getID());
         newTaskInfo.setName(taskInfo.getName());
         newTaskInfo.setParameterInfo(taskInfo.getParameterInfo());
         newTaskInfo.setParameterInfoArray(convertParameterInfoToNewVersion(taskInfo.getParameterInfoArray()));
         newTaskInfo.setTaskInfoAttributes((Map) taskInfo.getTaskInfoAttributes());
         newTaskInfo.setUserId(taskInfo.getUserId());
         return newTaskInfo;
     }
 
     private org.genepattern.webservice.ParameterInfo[] convertParameterInfoToNewVersion(
             ParameterInfo[] parameterInfoArray) {
         List<org.genepattern.webservice.ParameterInfo> newParameterInfoArray = new ArrayList<org.genepattern.webservice.ParameterInfo>(); 
         for (ParameterInfo parameterInfo : parameterInfoArray) {
             if (parameterInfo != null) {
                 org.genepattern.webservice.ParameterInfo newParameterInfo = 
                     new org.genepattern.webservice.ParameterInfo();
                 newParameterInfo.setAttributes(parameterInfo.getAttributes());
                 newParameterInfo.setDescription(parameterInfo.getDescription());
                 newParameterInfo.setLabel(parameterInfo.getLabel());
                 newParameterInfo.setName(parameterInfo.getName());
                 newParameterInfo.setValue(parameterInfo.getValue());
                 newParameterInfoArray.add(newParameterInfo);
             }
         }
         return newParameterInfoArray.toArray(new org.genepattern.webservice.ParameterInfo[newParameterInfoArray.size()]);
     }
     
     private org.genepattern.webservice.JobInfo convertJobInfoToNewVersion(JobInfo jobInfo) {
         org.genepattern.webservice.JobInfo newJobInfo = new org.genepattern.webservice.JobInfo();
         newJobInfo.setJobNumber(jobInfo.getJobNumber());
         newJobInfo.setStatus(jobInfo.getStatus());
         newJobInfo.setTaskID(jobInfo.getTaskID());
         newJobInfo.setTaskLSID(jobInfo.getTaskLSID());
         newJobInfo.setTaskName(jobInfo.getTaskName());
         newJobInfo.setUserId(jobInfo.getUserId());
         return newJobInfo;
     }
 
     private List<ParameterInfo> convertParametersToOldVersion(List<org.genepattern.webservice.ParameterInfo> parameters) {
         List<ParameterInfo> parameterInfoList = new ArrayList<ParameterInfo>();
         for (org.genepattern.webservice.ParameterInfo newParameterInfo : parameters) {
             ParameterInfo oldParameterInfo = new ParameterInfo();
             oldParameterInfo.setAttributes(newParameterInfo.getAttributes());
             oldParameterInfo.setDescription(newParameterInfo.getDescription());
             oldParameterInfo.setLabel(newParameterInfo.getLabel());
             oldParameterInfo.setName(newParameterInfo.getName());
             oldParameterInfo.setValue(newParameterInfo.getValue());
             parameterInfoList.add(oldParameterInfo);
         }
         return parameterInfoList;
     }
     
     private File createFileFromFileWrapper(FileWrapper fileWrapper, File directory) throws IOException {
         File outputFile = directory == null ? 
                 File.createTempFile(fileWrapper.getFilename(), "") 
                 : File.createTempFile(fileWrapper.getFilename(), "", directory); 
         FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
         IOUtils.copy(fileWrapper.getDataHandler().getInputStream(), fileOutputStream);
         fileOutputStream.close();
         return outputFile;
     }
 
 }
