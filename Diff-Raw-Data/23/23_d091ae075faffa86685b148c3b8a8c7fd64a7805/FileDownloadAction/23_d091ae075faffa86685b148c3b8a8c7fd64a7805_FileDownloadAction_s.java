 package com.tp.action;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.entity.ClientFile;
 import com.tp.service.ClientFileManager;
 import com.tp.utils.Constants;
 import com.tp.utils.Struts2Utils;
 
 public class FileDownloadAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 
 	private String inputPath;
 	private String downloadFileName;
 	private long contentLength;
 	private ClientFileManager clientFileManager;
 
 	@Override
 	public String execute() throws Exception {
 		HttpServletResponse response = Struts2Utils.getResponse();
 		HttpServletRequest request = Struts2Utils.getRequest();
 		if (StringUtils.isBlank(inputPath)) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "path parametter is required.");
 			return null;
 		}
 		inputPath = Constants.LOCKER_STORAGE + new String(inputPath.getBytes("iso-8859-1"), Constants.ENCODE_UTF_8);
 		File file = new File(inputPath);
 		if (file.exists()) {
 			long p = 0;
 			long fileLength = file.length();
 			downloadFileName = new String(file.getName().getBytes(), "ISO-8859-1");
 
 			InputStream inputStream = new FileInputStream(file);
 
 			response.reset();
 			response.setHeader("Accept-Ranges", "bytes");
 			String range = request.getHeader("Range");
 			if (range != null) {
 				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
 				String r = range.replaceAll("bytes=", "");
 				String[] rs = r.split("-");
 
 				p = Long.parseLong(rs[0]);
 			}
 			response.setHeader("content-Length", String.valueOf(fileLength - p));
 			if (p != 0) {
 				String contentRange = new StringBuffer("bytes").append(new Long(p).toString()).append("-")
 						.append(new Long(fileLength - 1).toString()).append("/")
 						.append(new Long(fileLength).toString()).toString();
 				response.setHeader("Content-Range", contentRange);
 				inputStream.skip(p);
 			}
 			response.addHeader("Content-Disposition", "attachment; filename=" + "\"" + downloadFileName + "\"");
//			response.setContentType("application/vnd.android.package-archive");
 			Cookie c = new Cookie("downloadFlag", "on");
 			c.setMaxAge(180);
 			response.addCookie(c);
 
 			OutputStream output = response.getOutputStream();
 			try {
 				IOUtils.copy(inputStream, output);
 				output.flush();
 			} catch (Exception e) {
 				//忽略通过android浏览器下载带来的异常
 			} finally {
 				IOUtils.closeQuietly(inputStream);
 			}
 		}
 
 		return null;
 	}
 
 	public String getClient() throws Exception {
 
 		String version = Struts2Utils.getParameter(Constants.PARA_CLIENT_VERSION);
 		String contentVersion = Struts2Utils.getParameter(Constants.PARA_CONTENT_VERSION);
 		String ct = Struts2Utils.getParameter(Constants.PARA_CLIENT_TYPE);
 		String app = Struts2Utils.getParameter("app"); //兼容老版本内容下载参数v 重复混乱的情况
 		if (app != null && !app.isEmpty()) {
 			app = new String(app.getBytes("iso-8859-1"), Constants.ENCODE_UTF_8);
 		}
 		if (ct != null && ct.equals(Constants.DM_LOCKER)) {
 			return getLockerClient(version, app, Constants.LOCKER_DM);
 		} else if (ct != null) {
 			return getLockerClient(version, app, ct);
 		}
 
 		String market = Struts2Utils.getParameter(Constants.PARA_FROM_MARKET);
 		if (contentVersion != null && contentVersion.contains(Constants.LOCKER_DM)) {
 			return getLockerClient(version, app, Constants.LOCKER_DM);
 		}
 		if (market != null && market.equals(Constants.MARKET_GOOGLE)) {
 			return getLockerClient(version, app, Constants.LOCKER_JP);
 		}
 		return getLockerClient(version, app, Constants.LOCKER_STANDARD);
 
 	}
 
 	private String getLockerClient(String version, String app, String dtype) throws Exception {
 
 		if (version == null || version.isEmpty()) {
 			version = clientFileManager.getNewestVersionCode(dtype);
 		} else if (version != null && app != null && !app.equals("千机解锁") && !app.equals("Funlocker")) {//内容下客户端,兼容老版本客户端v参数值的错误
 			version = clientFileManager.getNewestVersionCode(dtype);
 		}
 		ClientFile newClient = clientFileManager.getClientByVersion(version);
 
 		if (newClient != null) {
 			this.setInputPath("/" + newClient.getPath());
 			return execute();
 		} else {
 			return null;
 		}
 	}
 
 	@Autowired
 	public void setClientFileManager(ClientFileManager clientFileManager) {
 		this.clientFileManager = clientFileManager;
 	}
 
 	public void setInputPath(String inputPath) {
 		this.inputPath = inputPath;
 	}
 
 	public String getDownloadFileName() {
 		return downloadFileName;
 	}
 
 	public long getContentLength() {
 		return contentLength;
 	}
 }
