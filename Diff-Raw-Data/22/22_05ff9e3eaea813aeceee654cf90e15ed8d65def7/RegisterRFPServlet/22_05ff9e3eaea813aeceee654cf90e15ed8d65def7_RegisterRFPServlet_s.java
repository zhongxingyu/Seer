 package com.rfp.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import com.rfp.to.RFPTO;
 import com.rfp.wrapper.RFPWrapper;
 
 /**
  * Servlet implementation class RegisterRFPServlet
  */
 @WebServlet("/RegisterRFPServlet")
 public class RegisterRFPServlet extends HttpServlet {
 
 	public static final String PATH;
 	
 	static{
 		String temp = System.getProperty("user.dir");
 		int binId = temp.lastIndexOf("bin");
		PATH = temp.substring(0, binId) + "webapps\\ROOT\\docsRFP";
 	}
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 	
 	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if (processRFP(request))
 		{
 			response.sendRedirect("registerRFPResult.jsp?success=true");
 		}
 		else
 		{
 			response.sendRedirect("registerRFPResult.jsp?success=false");
 		}
 	}
 	
 	public boolean processRFP(HttpServletRequest request)
 	{
 		boolean result = false;
 		try
 		{
 			RFPTO rfp = new RFPTO();
 			FileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			List items = upload.parseRequest(request);
 			Iterator iter = items.iterator();
 			FileItem toSave = null;
 			while(iter.hasNext())
 			{
 				FileItem item = (FileItem) iter.next();
 				if (item.isFormField())
 				{					
 					if(item.getFieldName().equals("name"))
 					{
 						rfp.setName(item.getString());
 					}
 					else if (item.getFieldName().equals("company"))
 					{
 						rfp.setCompany(item.getString());
 					}
 					else if (item.getFieldName().equals("startDate"))
 					{
 						rfp.setStartDate(stringToCalendar(item.getString()));
 					}
 					else if (item.getFieldName().equals("decitionDate"))
 					{
 						rfp.setDecitionDate(stringToCalendar(item.getString()));
 					}
 				}
 				else
 				{
 					toSave = item;
 					rfp.setFilename(item.getName());
 				}
 			}
 			if (rfp.getName() != null && rfp.getCompany() != null && rfp.getFilename() != null
 					&& rfp.getStartDate() != null && rfp.getDecitionDate() != null)
 			{
 				RFPWrapper wrapper = new RFPWrapper();
 				wrapper.registerRFP(rfp);
 				if (rfp.getRequestId() != -1)
 				{
 					rfp.setFilename(toSave.getName());
 					if (!new File(PATH).exists())
 					{
 						new File(PATH).mkdir();
 					}
					if (new File(PATH + "\\" + rfp.getRequestId()).mkdir())
 					{
						String filename = PATH + "\\" + rfp.getRequestId() + "\\" + toSave.getName();
 						File file = new File(filename);
 						toSave.write(file);
 						result = true;
 					}
 				}
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	public Calendar stringToCalendar(String str)
 	{
 		try
 		{
 			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 			Date date = (Date)formatter.parse(str); 
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(date);
 			return cal;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
