 /*
  *  OpenSDI Manager
  *  Copyright (C) 2013 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.opensdi.operations;
 
 import it.geosolutions.opensdi.model.FileUpload;
 import it.geosolutions.opensdi.mvc.model.statistics.ExtendedFileBrowser;
 import it.geosolutions.opensdi.mvc.model.statistics.FileBrowser;
 import it.geosolutions.opensdi.service.GeoBatchClient;
 import it.geosolutions.opensdi.utils.ControllerUtils;
import it.geosolutions.opensdi.utils.GeoBatchRunInfoUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.AbstractRefreshableApplicationContext;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.multipart.MultipartFile;
 
 @Controller
 public class FileBrowserOperationController extends UserOperation implements
         ApplicationContextAware, Operation {
 private final static Logger LOGGER = Logger
         .getLogger(FileBrowserOperationController.class);
 
 /**
 * Separator for file system
 */
public static final String SEPARATOR = GeoBatchRunInfoUtils.SEPARATOR;

/**
  * Key for the sub folder open
  */
 public static final String DIRECTORY_KEY = "d";
 
 /**
  * Manager Suffix. Linked to files.jsp
  */
 private static final String MANAGER_SUFFIX = "Manager";
 
 private ApplicationContext applicationContext;
 
 private String operationName = "FileBrowser";
 
 private String operationRestPath = "filebrowser";
 
 private String operationJSP = "files";
 
 protected String defaultBaseDir;
 
 private Boolean canNavigate;
 
 private Boolean canUpload;
 
 private Boolean canDelete;
 
 private Boolean showRunInformation;
 
 private Boolean showRunInformationHistory;
 
 private UUID uniqueKey;
 
 private String accept;
 
 private String fileRegex;
 
 private List<String> allowedOperations;
 
 private Boolean canManageFolders;
 
 private Boolean canDownloadFiles;
 
 /**
  * Default method to upload is OLD
  */
 private UploadMethod uploadMethod = UploadMethod.OLD;
 
 /**
  * Max chunk size for PLUPLOAD. Default is '1mb'
  */
 private String chunkSize = "1mb";
 
 /**
  * Max file size for PLUPLOAD. Default is '100mb'
  */
 private String maxFileSize = "100mb";
 
 /**
  * Extension filter for PLUPLOAD
  */
 private String extensionFilter = null;
 
 @Autowired
 GeoBatchClient geoBatchClient;
 
 public FileBrowserOperationController() {
     setDefaultBaseDir("G:/OpenSDIManager/test_shapes/");
     uniqueKey = UUID.randomUUID();
 }
 
 /**
  * Shows the list of files inside the selected folder after a file upload
  * 
  * @param model
  * @return
  */
 // @RequestMapping(value = "/files", method = RequestMethod.POST)
 public String saveFileAndList(
         @ModelAttribute("uploadFile") FileUpload uploadFile, ModelMap model) {
 
     List<MultipartFile> files = uploadFile.getFiles();
 
     List<String> fileNames = new ArrayList<String>();
 
     HashMap<String, List<Operation>> availableOperations = getAvailableOperations();
 
     if (null != files && files.size() > 0) {
         for (MultipartFile multipartFile : files) {
 
             String fileName = multipartFile.getOriginalFilename();
             if (!"".equalsIgnoreCase(fileName)) {
                 // Handle file content - multipartFile.getInputStream()
                 try {
                     multipartFile.transferTo(new File(getRunTimeDir()
                             + fileName));
                 } catch (IllegalStateException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 fileNames.add(fileName);
             }
             LOGGER.debug(fileName);
 
         }
     }
 
     model.addAttribute("uploadedFiles", fileNames);
     model.addAttribute("accept", accept);
     FileBrowser fb = null;
     if (Boolean.TRUE.equals(this.showRunInformation)) {
         fb = new ExtendedFileBrowser();
         ((ExtendedFileBrowser) fb).setAvailableOperations(availableOperations);
         ((ExtendedFileBrowser) fb).setGeoBatchClient(geoBatchClient);
     } else {
         fb = new FileBrowser();
     }
     fb.setBaseDir(getRunTimeDir());
     fb.setRegex(fileRegex);
     fb.setScanDiretories(canNavigate);
     model.addAttribute("fileBrowser", fb);
     model.addAttribute("showRunInformation", showRunInformation);
 
     model.addAttribute("operations", availableOperations);
 
     model.addAttribute("context", operationJSP);
     ControllerUtils.setCommonModel(model);
 
     return "template";
 
 }
 
 /**
  * Provide a HashMap of every LocalOperation bean loaded
  * 
  * @return
  */
 private HashMap<String, List<Operation>> getAvailableOperations() {
 
     HashMap<String, List<Operation>> ocontrollersHashMap = new HashMap<String, List<Operation>>();
 
     String[] operationBeanNames = applicationContext
             .getBeanNamesForType(LocalOperation.class);
     for (String opearationBeanName : operationBeanNames) {
         LocalOperation fo = (LocalOperation) applicationContext
                 .getBean(opearationBeanName);
         // skip operation not allowed for the file browser
         if (allowedOperations != null) {
             if (!allowedOperations.contains(opearationBeanName)) {
                 continue;
             }
         }
         if (!fo.isMultiple()) {
             List<String> exts = fo.getExtensions();
             for (String extString : exts) {
                 if (ocontrollersHashMap.containsKey(extString)) {
                     ocontrollersHashMap.get(extString).add(fo);
                 } else {
                     List<Operation> olist = new ArrayList<Operation>();
                     olist.add(fo);
                     ocontrollersHashMap.put(extString, olist);
                 }
             }
         }
     }
 
     return ocontrollersHashMap;
 
 }
 
 @Override
 public void setApplicationContext(ApplicationContext arg0)
         throws BeansException {
     this.applicationContext = arg0;
 }
 
 /**
  * Register a manager a file manager if needed
  */
 private void registerManager(){
     if((Boolean.TRUE.equals(this.canManageFolders) || Boolean.TRUE.equals(this.canDownloadFiles)) 
             && this.applicationContext != null
             && this.applicationContext instanceof AbstractRefreshableApplicationContext){
         String managerName = this.operationRestPath + MANAGER_SUFFIX;
         if(!this.applicationContext.containsBean(managerName)){
             // Register manager singleton
             FolderManagerOperationController manageFolderOperation = new FolderManagerOperationController(managerName, this);
             ConfigurableListableBeanFactory beanFactory = ((AbstractRefreshableApplicationContext)this.applicationContext).getBeanFactory();
             beanFactory.registerSingleton(managerName, manageFolderOperation);
         }
     }
 }
 
 @Override
 public String getName() {
     return operationName;
 }
 
 /**
  * Getter for operationName
  * 
  * @return
  */
 public String getOperationName() {
     return operationName;
 }
 
 /**
  * Setter for operationName
  * 
  * @param newName
  */
 public void setOperationName(String newName) {
     this.operationName = newName;
 }
 
 @Override
 public String getRESTPath() {
     return operationRestPath;
 }
 
 /**
  * @param operationRestPath the operationRestPath to set
  */
 public void setRESTPath(String operationRestPath) {
     this.operationRestPath = operationRestPath;
 }
 
 @Override
 public String getJsp() {
     return operationJSP;
 }
 
 @Override
 public String getJsp(ModelMap model, HttpServletRequest request,
         List<MultipartFile> files) {
     
     registerManager();
 
     LOGGER.debug("getJSP di FileBrowser");
 
     // update key
     String update = request.getParameter("update");
 
     HashMap<String, List<Operation>> availableOperations = getAvailableOperations();
 
     String baseDir = getRunTimeDir();
 
     FileBrowser fb = null;
     if (Boolean.TRUE.equals(this.showRunInformation)) {
         fb = new ExtendedFileBrowser();
         ((ExtendedFileBrowser) fb).setAvailableOperations(availableOperations);
         ((ExtendedFileBrowser) fb).setGeoBatchClient(geoBatchClient);
         ((ExtendedFileBrowser) fb).setUpdateStatus(update != null);
     } else {
         fb = new FileBrowser();
     }
 
     Object gotParam = model.get("gotParam");
 
     @SuppressWarnings("unchecked")
     Map<String, String[]> parameters = request.getParameterMap();
 
     for (String key : parameters.keySet()) {
         LOGGER.debug(key); // debug
         String[] vals = parameters.get(key);
         for (String val : vals)
             // debug
             LOGGER.debug(" -> " + val); // debug
         if (key.equalsIgnoreCase(DIRECTORY_KEY)) {
             String dirString = parameters.get(key)[0].trim();
 
             dirString = ControllerUtils.preventDirectoryTrasversing(dirString);
 
            if (dirString.startsWith(SEPARATOR)) {
                 dirString = dirString.substring(1);
             }
 
             // remove last slash
 
            if (dirString.lastIndexOf(SEPARATOR) >= 0
                    && dirString.lastIndexOf(SEPARATOR) == (dirString.length() - 1)) {
                 LOGGER.debug("stripping last slash"); // debug
                 dirString = dirString.substring(0, dirString.length() - 1);
             }
 
             // second check
            if (dirString.lastIndexOf(SEPARATOR) >= 0) {
                 model.addAttribute("directoryBack",
                        dirString.substring(0, dirString.lastIndexOf(SEPARATOR)));
             } else {
                 model.addAttribute("directoryBack", "");
             }
 
            dirString = dirString.concat(SEPARATOR);
             baseDir = baseDir + dirString;
             model.addAttribute("directory", dirString);
            model.addAttribute("jsDirectory", dirString.replace(SEPARATOR, "/"));
 
         }
     }
 
     if (gotParam != null) {
         LOGGER.debug(gotParam); // debug
     }
     String gotAction = request.getParameter("action");
     String fileToDel = request.getParameter("toDel");
     if (gotAction != null && gotAction.equalsIgnoreCase("delete")
             && fileToDel != null) {
         String deleteFileString = baseDir + fileToDel;
         boolean res = deleteFile(deleteFileString);
         LOGGER.debug("Deletted " + deleteFileString + ": " + res); // debug
     }
 
     model.addAttribute("operationName", this.operationName);
     model.addAttribute("operationRESTPath", this.getRESTPath());
 
     fb.setBaseDir(baseDir);
     fb.setRegex(fileRegex);
     fb.setScanDiretories(canNavigate);
 
     if (null != files && files.size() > 0) {
         List<String> fileNames = new ArrayList<String>();
         for (MultipartFile multipartFile : files) {
             if (multipartFile == null)
                 continue;
             String fileName = multipartFile.getOriginalFilename();
             if (!"".equalsIgnoreCase(fileName)) {
                 try {
                     multipartFile.transferTo(new File(baseDir + fileName));
                 } catch (IllegalStateException e) {
                     LOGGER.error(e.getMessage());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 fileNames.add(fileName);
             }
             LOGGER.debug("filename: " + fileName); // debug
         }
     }
 
     model.addAttribute("fileBrowser", fb);
 
     model.addAttribute("operations", availableOperations);
 
     model.addAttribute("canDelete", this.canDelete);
     model.addAttribute("canUpload", this.canUpload);
     model.addAttribute("uploadMethod", this.uploadMethod.name());
     model.addAttribute("maxFileSize", this.maxFileSize);
     model.addAttribute("chunkSize", this.chunkSize);
     model.addAttribute("extensionFilter", this.extensionFilter);
     model.addAttribute("showRunInformation", this.showRunInformation);
     model.addAttribute("showRunInformationHistory",
             this.showRunInformationHistory);
     model.addAttribute("canManageFolders", this.canManageFolders);
     model.addAttribute("canDownloadFiles", this.canDownloadFiles);
 
     model.addAttribute("containerId", uniqueKey.toString().substring(0, 8));
     model.addAttribute("formId", uniqueKey.toString().substring(27, 36));
     model.addAttribute("accept", accept);
     return operationJSP;
 }
 
 /**
  * @return the defaultBaseDir
  */
 public String getDefaultBaseDir() {
     return defaultBaseDir;
 }
 
 /**
  * @param defaultBaseDir the defaultBaseDir to set
  */
 public void setDefaultBaseDir(String defaultBaseDir) {
 	this.defaultBaseDir = ControllerUtils.normalizePath(defaultBaseDir);
 }
 
 /**
  * @return the canNavigate
  */
 public Boolean getCanNavigate() {
     return canNavigate;
 }
 
 /**
  * @param canNavigate the canNavigate to set
  */
 public void setCanNavigate(Boolean canNavigate) {
     this.canNavigate = canNavigate;
 }
 
 private boolean deleteFile(String fileName) {
     if (fileName != null) {
         File toDel = new File(fileName);
         if (toDel.exists() && toDel.isFile()) {
             return toDel.delete();
         }
     }
     return false;
 }
 
 /**
  * @return the canUpload
  */
 public Boolean getCanUpload() {
     return canUpload;
 }
 
 /**
  * @param canUpload the canUpload to set
  */
 public void setCanUpload(Boolean canUpload) {
     this.canUpload = canUpload;
 }
 
 /**
  * @return the canDelete
  */
 public Boolean getCanDelete() {
     return canDelete;
 }
 
 /**
  * @param canDelete the canDelete to set
  */
 public void setCanDelete(Boolean canDelete) {
     this.canDelete = canDelete;
 }
 
 /**
  * a (optional) list of comma separated mi accepted MIME types allowed to be
  * uploaded (GUI only) e.g. "image/*,application/zip"
  * 
  * @param accept the accept string
  */
 public String getAccept() {
     return accept;
 }
 
 /**
  * set the accept string a (optional) list of comma separated mi accepted MIME
  * types allowed to be uploaded (GUI only) e.g. "image/*,application/zip"
  * 
  * @param accept
  */
 public void setAccept(String accept) {
     this.accept = accept;
 }
 
 /**
  * Optional file regex to filter file names
  * 
  * @return the fileRegex
  */
 public String getFileRegex() {
     return fileRegex;
 }
 
 /**
  * @param fileRegex the fileRegex to set
  */
 public void setFileRegex(String fileRegex) {
     this.fileRegex = fileRegex;
 }
 
 /**
  * Optional allowedOperations list
  * 
  * @return the allowedOperations
  */
 public List<String> getAllowedOperations() {
     return allowedOperations;
 }
 
 /**
  * The allowed operations for this file browser
  * 
  * @param allowedOperations a list of bean Id of the operations
  */
 public void setAllowedOperations(List<String> allowedOperations) {
     this.allowedOperations = allowedOperations;
 }
 
 /**
  * @return the showRunInformation
  */
 public Boolean getShowRunInformation() {
     return showRunInformation;
 }
 
 /**
  * @param showRunInformation the showRunInformation to set
  */
 public void setShowRunInformation(Boolean showRunInformation) {
     this.showRunInformation = showRunInformation;
 }
 
 /**
  * @return the showRunInformationHistory
  */
 public Boolean getShowRunInformationHistory() {
     return showRunInformationHistory;
 }
 
 /**
  * @param showRunInformationHistory the showRunInformationHistory to set
  */
 public void setShowRunInformationHistory(Boolean showRunInformationHistory) {
     this.showRunInformationHistory = showRunInformationHistory;
 }
 
 /**
  * @return the canManageFolders
  */
 public Boolean getCanManageFolders() {
     return canManageFolders;
 }
 
 /**
  * @param canManageFolders the canManageFolders to set
  */
 public void setCanManageFolders(Boolean canManageFolders) {
     this.canManageFolders = canManageFolders;
 }
 
 /**
  * @return the canDownloadFiles
  */
 public Boolean getCanDownloadFiles() {
     return canDownloadFiles;
 }
 
 /**
  * @param canDownloadFiles the canDownloadFiles to set
  */
 public void setCanDownloadFiles(Boolean canDownloadFiles) {
     this.canDownloadFiles = canDownloadFiles;
 }
 
 /**
  * @return the uploadMethod
  */
 public UploadMethod getUploadMethod() {
     return uploadMethod;
 }
 
 /**
  * @param uploadMethod the uploadMethod to set
  */
 public void setUploadMethod(UploadMethod uploadMethod) {
     this.uploadMethod = uploadMethod;
 }
 
 /**
  * Upload method used on file browser
  * 
  * @author adiaz
  * 
  * @see files.jsp
  *
  */
 public enum UploadMethod{
     OLD, PLUPLOAD
 }
 
 /**
  * @return the chunkSize
  */
 public String getChunkSize() {
     return chunkSize;
 }
 
 /**
  * @return the maxFileSize
  */
 public String getMaxFileSize() {
     return maxFileSize;
 }
 
 /**
  * @return the extensionFilter
  */
 public String getExtensionFilter() {
     return extensionFilter;
 }
 
 /**
  * @param chunkSize the chunkSize to set
  */
 public void setChunkSize(String chunkSize) {
     this.chunkSize = chunkSize;
 }
 
 /**
  * @param maxFileSize the maxFileSize to set
  */
 public void setMaxFileSize(String maxFileSize) {
     this.maxFileSize = maxFileSize;
 }
 
 /**
  * @param extensionFilter the extensionFilter to set
  */
 public void setExtensionFilter(String extensionFilter) {
     this.extensionFilter = extensionFilter;
 }
 
 }
