 package com.mydlp.ui.framework.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.web.HttpRequestHandler;
 
 import com.mydlp.ui.thrift.MyDLPUIThriftService;
 
 @Service("apiQueryServlet")
 public class ApiQueryServlet implements HttpRequestHandler {
 
 	private static Logger logger = LoggerFactory
 			.getLogger(ApiQueryServlet.class);
 
 	private static final String ERROR = "error";
 	
 	protected static final int MAX_CONTENT_LENGTH = 10*1024*1024;
 
 	@Autowired
 	protected MyDLPUIThriftService thriftService;
 
 	@Override
 	public void handleRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String returnStr = ERROR;
 		try {
 			String filename= req.getParameter("filename");
 			String ipAddress = req.getRemoteAddr();
 			
 			if (req.getContentLength() < MAX_CONTENT_LENGTH)
 			{
 				ByteBuffer data = ByteBuffer.allocate(EndpointReceiveServlet.READ_BLOCK);
 				ReadableByteChannel channel = Channels.newChannel(req.getInputStream());
 				
 				while (channel.read(data) != -1)  
 					data = EndpointReceiveServlet.resizeBuffer(data);
 				data.position(0);
 				channel.close();
 				
 				returnStr = thriftService.apiQuery(ipAddress, filename, data);
 			}
 		} catch (IOException e) {
 			logger.error("IOError occurred", e);
 		} catch (RuntimeException e) {
 			logger.error("Runtime error occured", e);
 		}
 		if (returnStr == null || returnStr.length() == 0)
 			returnStr = ERROR;
 		PrintWriter out = resp.getWriter();
 		out.print(returnStr);
 		out.flush();
 		out.close();
 	}
 
 }
