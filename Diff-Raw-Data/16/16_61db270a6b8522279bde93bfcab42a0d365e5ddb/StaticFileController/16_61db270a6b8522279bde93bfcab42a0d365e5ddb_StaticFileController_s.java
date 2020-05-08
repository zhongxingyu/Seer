 package com.internetitem.simpleweb.controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URLConnection;
 import java.util.Collection;
 
 import javax.servlet.ServletException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.internetitem.simpleweb.annotation.ControllerOptions;
 import com.internetitem.simpleweb.annotation.WebAction;
 import com.internetitem.simpleweb.router.ControllerBase;
 import com.internetitem.simpleweb.router.Response;
 import com.internetitem.simpleweb.router.ResponseHeader;
 import com.internetitem.simpleweb.router.exception.HttpError;
 import com.internetitem.simpleweb.utility.Params;
 
 @ControllerOptions(exposeAll = false)
 public class StaticFileController implements ControllerBase {
 
 	private static final Logger logger = LoggerFactory.getLogger(StaticFileController.class);
 
 	@WebAction
	public StaticFileStreamer index(Params params) throws HttpError, ServletException {
 		String filename = params.getEvaluatedValue("file");
 		String path;
 		if (filename != null) {
 			path = filename;
 		} else {
 			path = params.getValue("path");
 		}
 		if (!path.startsWith("/")) {
 			path = "/" + path;
 		}
 		path = path.replaceAll("/\\.\\.", "");
 
 		String serveFrom = params.getValue("serveFrom");
 		InputStream istream;
 		if (serveFrom != null && serveFrom.equals("classpath")) {
 			istream = getClass().getResourceAsStream(path);
 		} else {
			try {
				File file = new File(path);
				if (!file.isFile()) {
					throw new HttpError("File Not Found", 404);
				}
				istream = new FileInputStream(path);
			} catch (IOException e) {
				throw new HttpError(e.getMessage(), 404);
 			}
 		}
 
 		if (istream == null) {
 			logger.warn("Unable to find file " + path);
 			throw new HttpError("File not Found", 404);
 		}
 
 		String contentType = getContentType(params, path);
 		return new StaticFileStreamer(contentType, istream);
 	}
 
 	private String getContentType(Params params, String path) {
 		String contentType = params.getValue("contentType");
 		if (contentType != null) {
 			return contentType;
 		}
 
 		return URLConnection.guessContentTypeFromName(path);
 	}
 
 	private class StaticFileStreamer implements Response {
 
 		private String contentType;
 		private InputStream inputStream;
 
 		public StaticFileStreamer(String contentType, InputStream inputStream) {
 			this.contentType = contentType;
 			this.inputStream = inputStream;
 		}
 
 		@Override
 		public String getContentType() {
 			return contentType;
 		}
 
 		@Override
 		public void writeResponse(OutputStream outputStream) throws IOException {
 			int numRead;
 			byte[] buf = new byte[4096];
 			while ((numRead = inputStream.read(buf)) > 0) {
 				outputStream.write(buf, 0, numRead);
 			}
 		}
 
 		@Override
 		public Collection<ResponseHeader> getHeaders() {
 			return null;
 		}
 
 	}
 }
