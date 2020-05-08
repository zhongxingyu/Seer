 import java.io.*;
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.*;
 import java.net.*;
 import java.util.Properties;
 import java.text.SimpleDateFormat;
 
 public class LeadAdder extends HttpServlet {
 	
	Hashtable<String,Object> myList = new Hashtable<String,Object>();
 	
 	public static class ShellOut {
 
 		public void shell(String to,String sub, String text) {  
 			try {  
 
 				String command = "cmd /c cd C:\\SendMail && set CLASSPATH=%CLASSPATH%;C:\\OpenGeo\\webapps\\bids\\WEB-INF\\lib\\javax.mail.jar;. && java SendMail " + to + " \"" + sub + "\" \"" + text + "\"";
 				Process p = Runtime.getRuntime().exec(command); 
 
 				BufferedReader in = new BufferedReader(  
 						new InputStreamReader(p.getInputStream()));  
 				String line = null;  
 				while ((line = in.readLine()) != null) {  
 					System.out.println(line);  
 				}  
 			} catch (IOException e) {  
 				e.printStackTrace();  
 			}  
 		} 
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 		Connection con = null;
 		Statement stmt = null;
 		ResultSet rs = null;
 
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 
 		String query = "";
 		String names = "\"Timestamp\",\"Contact_Timestamp\",";
 		String values = "\'NOW()\',\'NOW()\',";
 		String sectors = "";		
 		boolean isUpdate=false;
 
 		Enumeration paramNames = req.getParameterNames();
 		
 		PrintWriter printWriter = new PrintWriter(new FileWriter("LeadLog.txt", true));
 		
 		java.util.Date date = new java.util.Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 		
 		String editType = req.getParameter("editType");
 		String lat = req.getParameter("Lat");
 		String lon = req.getParameter("Lon");
 		String fid = req.getParameter("fid");
 		String update="";
 
 		fid = fid.substring(fid.indexOf('.')+1);
 
 		
 				
 		if(editType.equals("edit")){
 			isUpdate=true;
 
 			myList.clear();
 
 			while (paramNames.hasMoreElements()) {
 				String paramName = (String) paramNames.nextElement();
 				String[] paramValues = req.getParameterValues(paramName);
 
 
 				String paramValue = paramValues[0];
 				
 				paramValue = paramValue.replace("'", "''");
 				paramValue = paramValue.replace('"', '\"');
 				paramValue = paramValue.replace("\\", "\\\\");
 				paramValue = paramValue.replace("\r", " ");
 				paramValue = paramValue.replace("\n", " ");
 				paramValue = paramValue.replace("\t", " ");
 				paramValue = paramValue.replace("\f", " ");
 				
 				//myList.add(paramName + " : " + paramValue);
 
 				if(paramName.equals("editType")){
 
 				}
 				else if(paramName.equals("Clear")){
 
 				}
 				else if(paramName.equals("fid")){
 
 				}
 				else if(paramName.startsWith("ch")){
 
 				}
 				else if(paramName.equals("Tender_Date")&&paramValue.equals("")){
 
 				}
 				else if(paramName.equals("Project_Announced")&&paramValue.equals("")){
 
 				}
 				else if (paramName.startsWith("Country")) {
 								
 								String wkt = Geocode(lat, lon);
 								printWriter.println("*********WKT"+sdf.format(date));
 								printWriter.println (wkt);
 								if (wkt.equals("0")) {
 
 								}
 								else{
 									/*names += "\"" + paramName + "\"" + ",";
 									values += "\'" + paramValue + "\'" + ",";
 									names += "\"" + "the_geom" + "\"" + ",";
 									values +=  wkt +  ",";*/
 									
 									update+= "\"" + paramName +"\"=\'" + paramValue + "\',";
 									update+= "\"" + "the_geom" +"\"=" + wkt + ",";
 								}
 							}
 				else{
 					update+= "\"" + paramName +"\"=\'" + paramValue + "\',";
 				}
 			}
 
 			update = update.substring(0, update.length()-1);
 			update = update + " where fid = " + fid;
 			update = "update Opengeo.\"DATATABLE\" set " + update;
 			out.println(update);
 		}
 		else{
 			
 			myList.clear();
 			myList.put("Project_Title", req.getParameter("Project_Title"));
 			myList.put("Specific_Location", req.getParameter("Specific_Location"));
 			myList.put("Country", req.getParameter("Country"));
 			myList.put("Lat", req.getParameter("Lat"));
 			myList.put("Lon", req.getParameter("Lon"));
 			myList.put("Sector", req.getParameter("Sector"));
 			myList.put("Project_Size", req.getParameter("Project_Size"));
 			myList.put("Status", req.getParameter("Status"));
 			myList.put("Project_Number", req.getParameter("Project_Number"));
 			myList.put("Project_Funding_Source", req.getParameter("Project_Funding_Source"));
 			myList.put("Source", req.getParameter("Source"));
 			myList.put("Project_Description", req.getParameter("Project_Description"));
 			myList.put("Keyword", req.getParameter("Keyword"));
 			myList.put("Project_Announced", req.getParameter("Project_Announced"));
 			myList.put("Tender_Date", req.getParameter("Tender_Date"));
 			myList.put("Borrowing_Entity", req.getParameter("Borrowing_Entity"));
 			myList.put("Implementing_Entity", req.getParameter("Implementing_Entity"));
 			myList.put("Link_To_Project", req.getParameter("Link_To_Project"));
 			myList.put("Business_URL", req.getParameter("Business_URL"));
 			myList.put("Submitting_Officer", req.getParameter("Submitting_Officer"));
 			myList.put("Submitting_Officer_Contact", req.getParameter("Submitting_Officer_Contact"));
 			myList.put("Project_POCs", req.getParameter("Project_POCs"));
 			myList.put("Post_Comments", req.getParameter("Post_Comments"));
 			
 			while (paramNames.hasMoreElements()) {
 				String paramName = (String) paramNames.nextElement();
 				String[] paramValues = req.getParameterValues(paramName);
 
 				if(paramName.equals("editType")){
 
 				}
 	
 				else if(paramName.equals("fid")){
 
 				}
 
 				else if (paramValues.length > 0) {
 
 					String paramValue = paramValues[0];
 					
 					paramValue = paramValue.replace("'", "''");
 					paramValue = paramValue.replace('"', '\"');
 					paramValue = paramValue.replace("\\", "\\\\");
 					paramValue = paramValue.replace("\r", " ");
 					paramValue = paramValue.replace("\n", " ");
 					paramValue = paramValue.replace("\t", " ");
 					paramValue = paramValue.replace("\f", " ");
 					
 					if (paramValue.length() > 0) {
 
 						if (paramName.startsWith("ch")) {
 
 							//String newName = checkName(paramName);
 							//sectors += newName + " ";
 		
 						} else {
 
 							if (paramName.startsWith("Country")) {
 								
 								String wkt = Geocode(lat, lon);
 								printWriter.println("*********WKT"+sdf.format(date));
 								printWriter.println (wkt);
 								if (wkt.equals("0")) {
 
 								}
 								else{
 									names += "\"" + paramName + "\"" + ",";
 									values += "\'" + paramValue + "\'" + ",";
 									names += "\"" + "the_geom" + "\"" + ",";
 									values +=  wkt +  ",";
 								}
 							} else {
 								
 								names += "\"" + paramName + "\"" + ",";
 								values += "\'" + paramValue + "\'" + ",";
 							}
 						}
 
 					}
 				}
 
 			}
 
 			/*if (sectors.length() > 0) {
 				names += "\"Sector\"" + ",";
 				values += "\'" + sectors + "\'" + ",";
 			}*/
 
 			names = names.substring(0, names.length() - 1);
 			values = values.substring(0, values.length() - 1);
 		}
 
 		try {
 
 			Class.forName("org.postgresql.Driver");
 
 			con = DriverManager.getConnection(
 					"jdbc:postgresql://localhost:54321/geoserver", "postgres",
 					"postgres");
 			stmt = con.createStatement();
 			
 			String poc = req.getParameter("Submitting_Officer_Contact");
 			String fs = req.getParameter("Source");
 			String mid = req.getParameter("fid");
 			
 			if(editType.equals("clear")){
 			
 			send(poc,fs,mid,"clear");
 			}
 			else if(editType.equals("edit")){
 				
 				printWriter.println("*********UPDATE"+sdf.format(date));
 				printWriter.println (update);
 				int resUpdate = stmt.executeUpdate(update);
 				send(poc,fs,mid,"edit");
 				
 				//printWriter.close (); 
 			}else{
 				
 				String f = "select max(fid) from Opengeo.\"DATATABLE\"";
 				rs = stmt.executeQuery(f);
 				
 				while (rs.next()) {
 				mid = rs.getString(1);
 				}
 
 				int hid = Integer.parseInt(mid);
 				hid++;
 				mid = Integer.toString(hid);
 								
 				String insert = "INSERT INTO Opengeo.\"DATATABLE\" (" + names
 						+ ") VALUES(" + values + ")";
 				
 				int result = stmt.executeUpdate(insert);
 				
 				printWriter.println("*********INSERT"+sdf.format(date));
 				printWriter.println (insert);
 				send(poc,fs,mid,"insert");
 				printWriter.println("Email sent");
 				//printWriter.close (); 
 			}
 			
 		} catch (ClassNotFoundException e) {
 			out.println("Couldn't load database driver: " + e.getMessage());
 			printWriter.println ("Couldn't load database driver: " + e.getMessage());
 		} catch (SQLException e) {
 			out.println ("SQLException caught: " + e.getMessage());
 			printWriter.println ("SQLException caught: " + e.getMessage());
 		} finally {
 			
 			try {
 				if (con != null)
 					con.close();
 			} catch (SQLException ignored) {
 			}
 		}
 	}
 
 	public void send(String em, String fs, String mid, String edit){
 		
 		Properties prop = new Properties();
 		
 		String m1sub="";
 		String m1text="";
 		String m1to="";
 		String m2sub="";
 		String m2esub=""; 
 		String m3sub="";
 		String m2text="";
 		String m3text="";
 		String m3text2="";
 		
 		try {
 
 			String propertiesFilePath = getServletContext().getRealPath("WEB-INF/config.properties");
 			prop.load(new FileInputStream(propertiesFilePath));
 
 			m1sub = prop.getProperty("m1sub");
 			m1text = prop.getProperty("m1text");
 			m1to = prop.getProperty("m1to");
 			m2sub = prop.getProperty("m2sub");
 			m2esub = prop.getProperty("m2esub");
 			m2text = prop.getProperty("m2text");
 			m3sub = prop.getProperty("m3sub");
 			m3text = prop.getProperty("m3text");
 			m3text2 = prop.getProperty("m3text2");
 
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		
 		m1text+="<br/>";
 		m2esub+=fs;
 		m2sub+=fs;
 		m2text+="<br/>";
 		
 		String title = (String)myList.get("Project_Title");
 		String spec = (String)myList.get("Specific_Location");
 		String country = (String)myList.get("Country");
 		String lat = (String)myList.get("Lat");
 		String lon = (String)myList.get("Lon");
 		String sector = (String)myList.get("Sector");
 		String size = (String)myList.get("Project_Size");
 		String status = (String)myList.get("Status");
 		String number = (String)myList.get("Project_Number");
 		String fundSource = (String)myList.get("Project_Funding_Source");
 		String source = (String)myList.get("Source");
 		String descrip = (String)myList.get("Project_Description");
 		String keywords = (String)myList.get("Keyword");
 		String announDate = (String)myList.get("Project_Announced");
 		String tenderDate = (String)myList.get("Tender_Date");
 		String borrowEntity = (String)myList.get("Borrowing_Entity");
 		String impleEntity = (String)myList.get("Implementing_Entity");
 		String projLink = (String)myList.get("Link_To_Project");
 		String busURL = (String)myList.get("Business_URL");
 		String subOff = (String)myList.get("Submitting_Officer");
 		String subOffEmail = (String)myList.get("Submitting_Officer_Contact");
 		String pocs = (String)myList.get("Project_POCs");
 		String comments = (String)myList.get("Post_Comments");
 		
 		m1text+="<br/>Project Title: <b>"+title+"</b><br/>Sector: <b>"+sector+"</b><br/>Project Size: US$<b>"+size+"</b><br/>Status: <b>"+status+"</b><br/>Project Number: <b>"+number+"</b><br/>Funding Source: <b>"+fundSource+"</b><br/>Information Source: <b>"+source+"</b><br/>Project Description: <b>"+descrip+"</b><br/>Keywords: <b>"+keywords+"</b><br/>Announced Date: <b>"+announDate+"</b><br/>Tender Date: <b>"+tenderDate+"</b><br/>Borrowing Entity: <b>"+borrowEntity+"</b><br/>Implementing Entity: <b>"+impleEntity+"</b><br/>Project Website: <b>"+projLink+"</b><br/>Business Tab: <b>"+busURL+"</b><br/>Submitting Officer: <b>"+subOff+"</b><br/>Submitting Officer Email: <b>"+subOffEmail+"</b><br/>Project Point of Contacts: <b>"+pocs+"</b><br/>Post Comments: <b>"+comments+"</b><br/>Tracking ID: <b>"+mid+"</b><br/><p>Please also confirm the location details. You entered: <br/>Country: <b>"+country+"</b><br/><br/>Geographic Location: <b>"+spec+"</b><br/></p>";
 		m2text+="<br/>Project Title: <b>"+title+"</b><br/>Sector: <b>"+sector+"</b><br/>Project Size: US$<b>"+size+"</b><br/>Status: <b>"+status+"</b><br/>Project Number: <b>"+number+"</b><br/>Funding Source: <b>"+fundSource+"</b><br/>Information Source: <b>"+source+"</b><br/>Project Description: <b>"+descrip+"</b><br/>Keywords: <b>"+keywords+"</b><br/>Announced Date: <b>"+announDate+"</b><br/>Tender Date: <b>"+tenderDate+"</b><br/>Borrowing Entity: <b>"+borrowEntity+"</b><br/>Implementing Entity: <b>"+impleEntity+"</b><br/>Project Website: <b>"+projLink+"</b><br/>Business Tab: <b>"+busURL+"</b><br/>Submitting Officer: <b>"+subOff+"</b><br/>Submitting Officer Email: <b>"+subOffEmail+"</b><br/>Project Point of Contacts: <b>"+pocs+"</b><br/>Post Comments: <b>"+comments+"</b><br/>Tracking ID: <b>"+mid+"</b><br/><p>Please also confirm the location details. You entered: <br/>Country: <b>"+country+"</b><br/><br/>Geographic Location: <b>"+spec+"</b><br/></p><a href=\"http://bids.state.gov/servlet/Clear?fid="+mid+"\">Click here to Clear</a>";
 		
 		m3text = m3text + " " + m3text2;
 		
 		myList.clear();
 		
 		LeadAdder.ShellOut b = new LeadAdder.ShellOut();
 		if(edit.equals("edit")||edit.equals("insert"))
 		b.shell(em+","+m1to,m1sub,m1text);
 		if(edit.equals("edit"))
 		b.shell(m1to,m2esub,m2text);
 		else if(edit.equals("insert"))
 		b.shell(m1to,m2sub,m2text);
 		else if(edit.equals("clear"))
 		b.shell(m1to,m3sub,m3text);
 		
 	}
 
 	public String checkName(String ch) {
 
 		String newCheck = "";
 
 		if (ch.equals("chAg")) {
 			newCheck = "Ag and Environment";
 		} else if (ch.equals("chICT")) {
 			newCheck = "ICT";
 		} else if (ch.equals("chGovernance")) {
 			newCheck = "Governance and Services";
 		} else if (ch.equals("chInfrasctructure")) {
 			newCheck = "Infrastructure";
 		} else if (ch.equals("chNatural")) {
 			newCheck = "Natural Resources";
 		}
 		return newCheck;
 	}
 
 	public String Geocode(String lat, String lon) {
 		
 		String wkt = "ST_GeomFromText(\'POINT(" + lon + " " + lat + ")\', 4326)";
 		
 		return wkt;
 	}
 
 }
