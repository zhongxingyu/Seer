 package com.iappsam.servlet.database;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import javax.servlet.*;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 
 import com.iappsam.database.BackupDatabaseTool;
 import com.iappsam.logging.Logger;
 
 /**
  * Servlet implementation class CreateAccount
  */
 @WebServlet("/database/backup.sql")
 public class BackupDatabase extends HttpServlet {
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		BackupDatabaseTool bdTool = new BackupDatabaseTool();
 		ServletOutputStream stream = null;
 		BufferedInputStream buf = null;
 
 		try {
 
 			stream = response.getOutputStream();
 			File file = new File(bdTool.initBackUp().getAbsolutePath() + ".sql");
 			String fileName = "IAPPSAMS_DB_Backup_" + (getCurrentDate()) + ".sql";
			Logger.log(request, "Database was backed up");
 			
 			// set response headers
 			response.setContentType("text/plain");
 			response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
 			response.setContentLength((int) file.length());
 
 			FileInputStream input = new FileInputStream(file);
 			buf = new BufferedInputStream(input);
 			int readBytes = 0;
 
 			while ((readBytes = buf.read()) != -1)
 				stream.write(readBytes);
 
 		} catch (IOException ioe) {
 
 			throw new ServletException(ioe.getMessage());
 
 		} finally {
 
 			if (stream != null)
 				stream.close();
 			if (buf != null)
 				buf.close();
 		}
 	}
 
 	private static String getCurrentDate() {
 		String DATE_FORMAT_NOW = "[HH'h'-mm'm'-ss's']_MM-dd-yyyy";
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
 		return sdf.format(cal.getTime());
 	}
 }
