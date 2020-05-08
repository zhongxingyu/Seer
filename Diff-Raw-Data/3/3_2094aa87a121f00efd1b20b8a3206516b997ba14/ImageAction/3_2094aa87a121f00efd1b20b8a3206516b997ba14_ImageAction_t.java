 package com.tp.action;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URLDecoder;
 import java.util.zip.GZIPOutputStream;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.utils.Constants;
 import com.tp.utils.ServletUtils;
 import com.tp.utils.Struts2Utils;
 
 public class ImageAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = LoggerFactory.getLogger(ImageAction.class);
 
 	private static final String[] GZIP_MIME_TYPES = { "text/html", "text/xml", "text/plain", "text/css",
 			"text/javascript", "application/xml", "application/xhtml+xml", "application/x-javascript" };
 
 	private static final int GZIP_MINI_LENGTH = 512;
 
 	private MimetypesFileTypeMap mimetypesFileTypeMap;
 	public static final long ONE_YEAR_SECONDS = 60 * 60 * 24 * 365;
 
 	@Override
 	public String execute() throws Exception {
 		return getImage();
 	}
 
 	public String getImage() throws Exception {
 		String contentPath = Struts2Utils.getParameter("path");
 		responseImage(contentPath);
 		return null;
 	}
 
 	private void responseImage(String contentPath) throws Exception {
 		HttpServletResponse response = Struts2Utils.getResponse();
 		HttpServletRequest request = Struts2Utils.getRequest();
 		if (StringUtils.isBlank(contentPath)) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "path parametter is required.");
 			return;
 		}
 		contentPath = URLDecoder.decode(contentPath, "UTF-8");
 		String realPath = Constants.LOCKER_STORAGE + new String(contentPath.getBytes("iso-8859-1"), "utf-8");
 		ContentInfo contentInfo = getContentInfo(realPath);
 
 		//根据Etag或ModifiedSince Header判断客户端的缓存文件是否有效, 如仍有效则设置返回码为304,直接返回.
 		if (!ServletUtils.checkIfModifiedSince(request, response, contentInfo.lastModified)
 				|| !ServletUtils.checkIfNoneMatchEtag(request, response, contentInfo.etag)) {
 			return;
 		}
 		ServletUtils.setExpiresHeader(response, ONE_YEAR_SECONDS);
 		ServletUtils.setLastModifiedHeader(response, System.currentTimeMillis());
 		ServletUtils.setEtag(response, contentInfo.etag);
 
 		response.setContentType(contentInfo.mimeType);
 
 		//设置弹出下载文件请求窗口的Header
 		if (request.getParameter("download") != null) {
 			ServletUtils.setFileDownloadHeader(response, contentInfo.fileName);
 		}
 
 		//构造OutputStream
 		OutputStream output;
 		if (checkAccetptGzip(request) && contentInfo.needGzip) {
 			//使用压缩传输的outputstream, 使用http1.1 trunked编码不设置content-length.
 			output = buildGzipOutputStream(response);
 		} else {
 			//使用普通outputstream, 设置content-length.
 			response.setContentLength(contentInfo.length);
 			output = response.getOutputStream();
 		}
 
 		//高效读取文件内容并输出,然后关闭input file
 
 		try {
 			FileUtils.copyFile(contentInfo.file, output);
			output.flush();
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 		}
 
 	}
 
 	/**
 	 * 检查浏览器客户端是否支持gzip编码.
 	 */
 	private static boolean checkAccetptGzip(HttpServletRequest request) {
 		//Http1.1 header
 		String acceptEncoding = request.getHeader("Accept-Encoding");
 
 		return StringUtils.contains(acceptEncoding, "gzip");
 	}
 
 	/**
 	 * 设置Gzip Header并返回GZIPOutputStream.
 	 */
 	private OutputStream buildGzipOutputStream(HttpServletResponse response) throws IOException {
 		response.setHeader("Content-Encoding", "gzip");
 		response.setHeader("Vary", "Accept-Encoding");
 		return new GZIPOutputStream(response.getOutputStream());
 	}
 
 	@PostConstruct
 	public void init() {
 		mimetypesFileTypeMap = new MimetypesFileTypeMap();
 		mimetypesFileTypeMap.addMimeTypes("text/css css");
 	}
 
 	private ContentInfo getContentInfo(String contentPath) {
 		ContentInfo contentInfo = new ContentInfo();
 		File file = new File(contentPath);
 		contentInfo.file = file;
 		contentInfo.contentPath = contentPath;
 		contentInfo.fileName = file.getName();
 		contentInfo.length = (int) file.length();
 
 		contentInfo.lastModified = file.lastModified();
 		contentInfo.etag = "W/\"" + contentInfo.lastModified + "\"";
 
 		contentInfo.mimeType = mimetypesFileTypeMap.getContentType(contentInfo.fileName);
 
 		if (contentInfo.length >= GZIP_MINI_LENGTH && ArrayUtils.contains(GZIP_MIME_TYPES, contentInfo.mimeType)) {
 			contentInfo.needGzip = true;
 		} else {
 			contentInfo.needGzip = false;
 		}
 
 		return contentInfo;
 	}
 
 	static class ContentInfo {
 		protected String contentPath;
 		protected File file;
 		protected String fileName;
 		protected int length;
 		protected String mimeType;
 		protected long lastModified;
 		protected String etag;
 		protected boolean needGzip;
 	}
 }
