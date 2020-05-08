 package ru.timtish.bridge.web;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ru.timtish.bridge.box.StreamsBox;
 import ru.timtish.bridge.pipeline.AbstractStream;
 import ru.timtish.bridge.web.util.UrlConstants;
 
 /**
  * @author Timofey Tishin (ttishin@luxoft.com)
  */
 public class ZipExportServlet extends HttpServlet {
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<String> streamKeyList = new ArrayList<String>();

		streamKeyList.addAll(Arrays.asList(request.getParameterValues(UrlConstants.PARAM_KEYS + "[]")));
 
 		// todo: check permissions
 
		String zipName = "box.zip";
 
 		response.setContentType("application/zip");
 		response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(zipName, "UTF-8") + "\"");
 		response.addCookie(new Cookie("fileDownload", "true"));
 
 		ZipOutputStream zipStream = new ZipOutputStream(response.getOutputStream());
 		try {
 			for (String key : streamKeyList) {
 				AbstractStream stream = StreamsBox.getInstance().getStream(key);
 
 				if (stream == null) continue; // todo: show warning
 
 				ZipEntry zipEntry = new ZipEntry(stream.getName());
 				zipEntry.setSize(stream.getSize());
 				zipEntry.setComment(stream.getDescription());
 				zipStream.putNextEntry(zipEntry);
 
 				stream.write(zipStream);
 
 				if (!stream.isRepeatable()) {
 					StreamsBox.getInstance().release(key);
 				}
 			}
 		} finally {
 			zipStream.close();
 		}
 	}
 
 }
