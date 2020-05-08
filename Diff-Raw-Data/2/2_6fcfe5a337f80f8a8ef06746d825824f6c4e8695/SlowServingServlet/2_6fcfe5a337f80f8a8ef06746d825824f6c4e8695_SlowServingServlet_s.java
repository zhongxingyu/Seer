 package com.scheffield.ria.loading;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 
 public class SlowServingServlet extends HttpServlet {
 
 	private static final String TIME_TO_WAIT_PARAM = "t";
 	private static final String RESOURCE_TO_LOAD = "r";
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		final Integer timeToWait = Integer.valueOf(req.getParameter(TIME_TO_WAIT_PARAM));
 		final String resource = req.getParameter(RESOURCE_TO_LOAD);
 		
 		try {
			Thread.currentThread().wait(timeToWait * 1000);
 		} catch (InterruptedException e) {
 			throw new ServletException(e);
 		}
 		
 		final File file = new File(resource);
 		FileInputStream fileInputStream = null;
 		ServletOutputStream os = null;
 		
 		
 		try {
 			fileInputStream = new FileInputStream(file);
 			os = resp.getOutputStream();
 			IOUtils.copy(fileInputStream, os);
 		} finally {
 			IOUtils.closeQuietly(fileInputStream);
 			IOUtils.closeQuietly(os);
 		}
 	}
 	
 }
