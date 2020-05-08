 package com.tp.action;
 
 import java.io.File;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.utils.ServletUtils;
 import com.tp.utils.Struts2Utils;
 
 @Namespace("/report")
 public class ViewSourceAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 	private Logger logger = LoggerFactory.getLogger(ViewSourceAction.class);
 	private String path;
 	private long MAX_FILE_SIZE = 30000000;
 	private String userDir = System.getProperty("user.dir");
 
 	@Override
 	public String execute() throws Exception {
 
 		return SUCCESS;
 	}
 
 	public String view() throws Exception {
 		try {
 			HttpServletResponse response = Struts2Utils.getResponse();
 			HttpServletRequest request = Struts2Utils.getRequest();
 			String userDir = System.getProperty("user.dir");
 			String download = request.getParameter("download");
 			String fname = request.getParameter("fname");
 
 			if (StringUtils.isBlank(fname)) {
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "fname parametter is required.");
 				return null;
 			}
 			path = StringUtils.strip(path, null);
 
 			File file;
 			if (StringUtils.isBlank(path) && userDir != null) {
 
 				file = new File(userDir + "/logs", fname);
 			} else {
 				file = new File(path, fname);
 			}
 			OutputStream output = response.getOutputStream();
 
 			if (!file.exists()) {
 
 				Struts2Utils.renderText("文件不存在");
 				return null;
 			}
 			if (file.length() > MAX_FILE_SIZE && download == null) {
 				Struts2Utils.renderText("文件过大，请带上下载参数download=true保存到本地");
 				return null;
 			}
 			if (download != null) {
 				ServletUtils.setFileDownloadHeader(response, file.getName());
 			}
 			FileUtils.copyFile(file, output);
 			output.flush();
 		} catch (Exception e) {
			logger.warn(e.getMessage() + "下载日志文件时非正常中断");
 		}
 		return null;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public String getUserDir() {
 		return userDir;
 	}
 }
