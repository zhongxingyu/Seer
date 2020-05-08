 package com.papteco.web.controllers;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 import com.google.common.collect.ImmutableMap;
 import com.papteco.web.beans.DocTypeFieldSet;
 import com.papteco.web.beans.FileBean;
 import com.papteco.web.beans.FileLockBean;
 import com.papteco.web.beans.ProjectBean;
 import com.papteco.web.beans.QueueItem;
 import com.papteco.web.dbs.UserIPDAO;
 import com.papteco.web.netty.OpenFileClientBuilder;
 import com.papteco.web.netty.ReleaseFileClientBuilder;
 import com.papteco.web.services.FileServiceImpl;
 import com.papteco.web.services.ProjectServiceImpl;
 import com.papteco.web.utils.EncoderDecoderUtil;
 import com.papteco.web.utils.FilesUtils;
 import com.papteco.web.utils.TaskUtils;
 import com.papteco.web.utils.WebUtils;
 
 @Controller
 public class FileController extends BaseController {
 	@Autowired
 	private FileServiceImpl fileService;
 	@Autowired
 	private ProjectServiceImpl projectService;
 
 	private String fileStructPath;
 	private String serverFilePath;
 
 	private String prepareFileName(DocTypeFieldSet bean) {
 		StringBuffer trgFileName = new StringBuffer();
 
 		trgFileName.append(bean.getUpload_doctype());
 		trgFileName.append(bean.getClientNo());
 		trgFileName.append("-");
 
 		if (StringUtils.isNotBlank(bean.getDrawintType())) {
 			trgFileName.append(bean.getDrawintType());
 			trgFileName.append(bean.getRef());
 			trgFileName.append("-");
 			trgFileName.append(bean.getL1() + " ");
 			trgFileName.append(bean.getL2() + " ");
 			trgFileName.append(bean.getL3());
 			trgFileName.append("-");
 		}
 
 		if (StringUtils.isNotBlank(bean.getDateWith4digs())) {
 			trgFileName.append(bean.getDateWith4digs());
 			trgFileName.append("-");
 			trgFileName.append(bean.getRef());
 			trgFileName.append("-");
 
 		}
 
 		if (StringUtils.isNotBlank(bean.getDateWith6digs())) {
 			trgFileName.append(bean.getDateWith6digs());
 			trgFileName.append("-");
 			trgFileName.append(bean.getRef());
 			trgFileName.append("-");
 		}
 
 		trgFileName.append(StringEscapeUtils.unescapeHtml(bean.getDescription()));
 		trgFileName.append("-");
 		trgFileName.append("Rev");
 		if (bean.getRev().length() < 3)
 			trgFileName.append(formatedNumber(bean.getRev(), 3));
 		else
 			trgFileName.append(bean.getRev());
 
 		if (StringUtils.isNotBlank(bean.getUploadedCopyForm())
 				&& bean.getUploadedCopyForm().contains(".")) {
 			trgFileName.append(bean.getUploadedCopyForm().substring(
 					bean.getUploadedCopyForm().lastIndexOf(".")));
 		} else {
 			if (bean.getUploadfile().getOriginalFilename().contains(".")) {
 				trgFileName.append(bean
 						.getUploadfile()
 						.getOriginalFilename()
 						.substring(
 								bean.getUploadfile().getOriginalFilename()
 										.lastIndexOf(".")));
 			}
 		}
 		return trgFileName.toString();
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "submitUploadFile")
 	@ResponseBody
 	public String submitUploadFile(DocTypeFieldSet bean, Model model)
 			throws Exception {
 
 		// TODO Cony
 
 		if (StringUtils.isNotBlank(bean.getUploadedCopyForm())
 				&& !"undefined".equals(bean.getUploadedCopyForm())) {
 
 			String fromfileFolder;
 			String fileFolder;
 			if (bean.getUploadedCopyForm().substring(1, 2).equals("0")) {
 				fromfileFolder = combineFolderPath(
 						combineFolderPath(rootpath, "Templates"),
 						this.sysConfig.getFolderNameByFolderCde(bean
 								.getUpload_doctype()));
 			}else{
 				fromfileFolder = combineFolderPath(
 						combineFolderPath(rootpath, bean.getProjectCde()),
 						this.sysConfig.getFolderNameByFolderCde(bean
 								.getUpload_doctype()));
 			}
 			fileFolder = combineFolderPath(
 					combineFolderPath(rootpath, bean.getProjectCde()),
 					this.sysConfig.getFolderNameByFolderCde(bean
 							.getUpload_doctype()));
 
 			String fileName = prepareFileName(bean);
 
 			File fromfile = new File(fromfileFolder, StringEscapeUtils.unescapeHtml(bean.getUploadedCopyForm()));
 			
 			File tofile = new File(fileFolder, fileName);
 			FileBean fileBean = new FileBean();
 			fileBean.setFileId(FilesUtils.genFileId());
 			fileBean.setFileName(fileName);
 			fileBean.setInitUploadAt(new Date());
 			fileBean.setLastModifiedAt(new Date());
 			fileBean.setLastModifiedBy("admin");
 			fileBean.setInitUploadBy("cony");
 			BeanUtils.copyProperties(fileBean, bean);
 
 			if (fileService.isFileNameExisting(bean.getProjectId(),
 					fileBean.getFileName())) {
 				return null;
 			} else {
 				if (!fromfile.exists()) {
 					fromfile.createNewFile();
 				}
 				FilesUtils.copyFile(fromfile.getPath(), tofile.getPath());
 				fileService.saveUploadFile(bean.getProjectId(),
 						bean.getUpload_doctype(), fileBean);
 
 				// TODO Cony
 				// Order client to open the file
 				// temp solution for upload
 				serverFilePath = tofile.getPath();
 				fileStructPath = combineFolderPath(
 						bean.getProjectCde(),
 						combineFolderPath(this.sysConfig
 								.getFolderNameByFolderCde(bean
 										.getUpload_doctype()), fileBean
 								.getFileName()));
 
 				// open add rev file on local
 				QueueItem openfile = new QueueItem();
 				openfile.setActionType("OPENFILE");
 				openfile.setParam(fileStructPath);
 				fileService.saveFileLock(new FileLockBean(fileBean.getFileId(),
 						"conygychen", new Date()));
 				new Thread(new OpenFileClientBuilder(UserIPDAO.getUserIPBean(
 						"conygychen").getPCIP(), openfile, serverFilePath,
 						fileStructPath)).start();
 				return EncoderDecoderUtil.encodeURIComponent(fileBean.getFileName());
 			}
 
 		} else {
 			String fileFolder = combineFolderPath(
 					combineFolderPath(rootpath, bean.getProjectCde()),
 					this.sysConfig.getFolderNameByFolderCde(bean
 							.getUpload_doctype()));
 			String fileName = prepareFileName(bean);
 
 			File file = new File(fileFolder, fileName);
 			FileBean fileBean = new FileBean();
 			fileBean.setFileId(FilesUtils.genFileId());
 			fileBean.setFileName(fileName);
 			fileBean.setInitUploadAt(new Date());
 			fileBean.setLastModifiedAt(new Date());
 			fileBean.setLastModifiedBy("admin");
 			fileBean.setInitUploadBy("cony");
 			BeanUtils.copyProperties(fileBean, bean);
 
 			if (fileService.isFileNameExisting(bean.getProjectId(),
 					fileBean.getFileName())) {
 				return null;
 			} else {
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 				bean.getUploadfile().transferTo(file);
 				fileService.saveUploadFile(bean.getProjectId(),
 						bean.getUpload_doctype(), fileBean);
 				return EncoderDecoderUtil.encodeURIComponent(fileBean.getFileName());
 			}
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "uploadfile.do")
 	public String handleUploadProcess(MultipartHttpServletRequest request,
 			Model model) throws Exception {
 		System.out.println("Upload success");
 		MultipartFile file = request.getFile("uploadedfile");
 		model.addAttribute("success", "true");
 		return "uploadView";
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "uploadfile2.do")
 	public String handleUploadProcess2(
 			@RequestParam("file") MultipartFile file, Model model)
 			throws Exception {
 		System.out.println("Upload success");
 		System.out.println(file);
 		model.addAttribute("success", "true");
 		return "uploadView";
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "getDocs")
 	@ResponseBody
 	public Map getDocs(@RequestParam String projectId) throws Exception {
 		System.out.println("getDocs:" + projectId);
 
 		return WebUtils.toDocsSummaries(projectId, this.sysConfig);
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "deleteDocs")
 	@ResponseBody
 	public Map deleteDocs(@RequestParam String projectId,
 			@RequestParam String filename) throws Exception {
 		filename = EncoderDecoderUtil.decodeURIComponent(filename);
 		ProjectBean project = fileService.getProjectBeanByProjectId(projectId);
 
 		String fileFolder = combineFolderPath(
 				combineFolderPath(rootpath, project.getProjectCde()),
 				this.sysConfig.getFolderNameByFolderCde(filename
 						.substring(0, 1)));
 		File file = new File(fileFolder, filename);
 		if (file.exists()) {
 			file.delete();
 			fileService.deleteFile(projectId, filename.substring(0, 1),
 					filename);
 		}
 
 		return WebUtils.responseWithStatusCode();
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "viewDocs")
 	@ResponseBody
 	public Map viewDocs(@RequestParam String projectId,
 			@RequestParam String filename, HttpServletResponse response)
 			throws Exception {
		filename = EncoderDecoderUtil.decodeURIComponent(filename);
 		ProjectBean project = fileService.getProjectBeanByProjectId(projectId);
 
 		String fileFolder = combineFolderPath(
 				combineFolderPath(rootpath, project.getProjectCde()),
 				this.sysConfig.getFolderNameByFolderCde(filename
 						.substring(0, 1)));
 		File file = new File(fileFolder, filename);
 		if (file.exists()) {
 			response.setContentType("application/x-download");
 			response.setHeader("Content-disposition", "attachment; filename="
 					+ filename);
 			IOUtils.copy(fileService.getFileIs(file.getPath()),
 					response.getOutputStream());
 			response.flushBuffer();
 		} else {
 			System.out.println("no such file.");
 		}
 		System.out.println("viewDocs: " + filename);
 
 		return WebUtils.responseWithStatusCode();
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "editFile")
 	@ResponseBody
 	public Map editFile(@RequestParam String projectId,
 			@RequestParam String docType, @RequestParam String filename,
 			@RequestParam String fileid) throws Exception {
 		filename = EncoderDecoderUtil.decodeURIComponent(filename);
 		System.out.println("projectId:" + projectId + " fileid:" + fileid
 				+ " doctype:" + docType + " filename:" + filename);
 
 		// if(!fileService.isFileLocked(fileid)){
 		ProjectBean project = projectService.getProject(projectId);
 		if (project != null) {
 			String fileFolder = combineFolderPath(
 					combineFolderPath(rootpath, project.getProjectCde()),
 					this.sysConfig.getFolderNameByFolderCde(docType));
 			File file = new File(fileFolder, filename);
 
 			serverFilePath = file.getPath();
 			fileStructPath = combineFolderPath(
 					project.getProjectCde(),
 					combineFolderPath(
 							this.sysConfig.getFolderNameByFolderCde(docType),
 							filename));
 
 			// open add rev file on local
 			QueueItem openfile = new QueueItem();
 			openfile.setActionType("OPENFILE");
 			openfile.setParam(fileStructPath);
 			if (!fileService.isFileLocked(fileid)) {
 				fileService.saveFileLock(new FileLockBean(fileid, "conygychen",
 						new Date()));
 			}
 			new Thread(new OpenFileClientBuilder(UserIPDAO.getUserIPBean(
 					"conygychen").getPCIP(), openfile, serverFilePath,
 					fileStructPath)).start();
 		}
 
 		// }else{
 		// return ImmutableMap.of("open","fail");
 		// }
 		// TODO Cony
 		// order client to open doc locally and locked this file
 		if (true) {
 			return ImmutableMap.of("open", "succ");
 		} else {
 			return ImmutableMap.of("open", "fail");
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "releaseFile")
 	@ResponseBody
 	public Map releaseFile(@RequestParam String projectId,
 			@RequestParam String docType, @RequestParam String filename,
 			@RequestParam String fileid) throws Exception {
 		filename = EncoderDecoderUtil.decodeURIComponent(filename);
 		System.out.println("projectId:" + projectId + " fileid:" + fileid
 				+ " doctype:" + docType + " filename:" + filename);
 
 		// TODO Cony
 		// order client upload file and release this file
 
 		if (fileService.isFileLocked(fileid)) {
 			String taskid = TaskUtils.genTaskId();
 			TaskUtils.setTaskStatus(taskid, TaskUtils.STUS_START, "Starting release.");
 			ProjectBean project = projectService.getProject(projectId);
 			if (project != null) {
 				String fileFolder = combineFolderPath(
 						combineFolderPath(rootpath, project.getProjectCde()),
 						this.sysConfig.getFolderNameByFolderCde(docType));
 				File file = new File(fileFolder, filename);
 
 				serverFilePath = file.getPath();
 				fileStructPath = combineFolderPath(
 						project.getProjectCde(),
 						combineFolderPath(this.sysConfig
 								.getFolderNameByFolderCde(docType), filename));
 				// fileService.releaseFile(fileid);
 				new Thread(new ReleaseFileClientBuilder(UserIPDAO
 						.getUserIPBean("conygychen").getPCIP(), serverFilePath,
 						fileStructPath, fileid, taskid)).start();
 
 			}
 			
 			//3 mins timeout
 			int i=0;
 			while(i<90){
 				i++;
 				Thread.sleep(2000);
 				
 				String[] task = TaskUtils.getTaskStatus(taskid);
 				
 				if(task != null && task[0] != "START" ){
 					return ImmutableMap.of("FLAG", task[0],"Message", task[1]);
 				}
 			}
 			
 			return ImmutableMap.of("FLAG", "FAIL","Message", "Operation timeout, please retry");
 		}
 		
 		return ImmutableMap.of("FLAG", "FAIL","Message", "File is not locked");
 	}
 }
