 import java.io.*;
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.*;
 import java.net.*;
 
 public class LeadAdder extends HttpServlet {
 
 	public void doPost(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 		Connection con = null;
 		Statement stmt = null;
 		ResultSet rs = null;
 
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 
 		String query = "";
 		String names = "\"Timestamp\",";
 		String values = "\'NOW()\',";
 		String sectors = "";		
 		boolean isUpdate=false;
 
 		Enumeration paramNames = req.getParameterNames();
 
 		String editType = req.getParameter("editType");
 		String fid = req.getParameter("fid");
 		String update="";
 		
 		fid = fid.substring(fid.indexOf('.')+1);
 
 		if(editType.equals("edit")){
 			isUpdate=true;
 
 
 
 			while (paramNames.hasMoreElements()) {
 				String paramName = (String) paramNames.nextElement();
 				String[] paramValues = req.getParameterValues(paramName);
 
 
 				String paramValue = paramValues[0];
 
 				if(paramName.equals("editType")){
 
 				}
 				else if(paramName.equals("fid")){
 
 				}
 				else if(paramName.startsWith("ch")){
 
 				}
 				else{
 					update+= "\"" + paramName +"\"=\'" + paramValue + "\',";
 				}
 			}
 
 			update = update.substring(0, update.length()-1);
 			update = update + " where fid = " + fid;
 			update = "update Opengeo.\"DATATABLE\" set " + update;
 			out.print(update);
 
 		}
 		else{
 
 			while (paramNames.hasMoreElements()) {
 				String paramName = (String) paramNames.nextElement();
 				String[] paramValues = req.getParameterValues(paramName);
 				
 				if(paramName.equals("editType")){
 
 				}
 				
 				else if(paramName.equals("fid")){
 
 				}
 
 				else if (paramValues.length > 0) {
 
 					String paramValue = paramValues[0];
 					if (paramValue.length() > 0) {
 
 						if (paramName.startsWith("ch")) {
 
							if (paramName.equals("chOth")) {
 
 								sectors += paramValue + " ";
							} else {
 
 								String newName = checkName(paramName);
 								// names += "\"" + paramName + "\"" + ",";
 								// values += "\'" + paramValue + "\'" + ",";
 
 								sectors += newName + " ";
							}
 						} else {
 
 							if (paramName.startsWith("Spec")) {
 								String wkt = Geocode(paramValue);
 								out.println(wkt);
 								if (wkt.equals("0")) {
 
 								}
 								else{
 									names += "\"" + paramName + "\"" + ",";
 									values += "\'" + paramValue + "\'" + ",";
 									names += "\"" + "the_geom" + "\"" + ",";
 									values +=  wkt +  ",";
 								}
 							} else {
 								// String paramValue = paramValues[0];
 
 								// query+= ""
 
 								names += "\"" + paramName + "\"" + ",";
 								values += "\'" + paramValue + "\'" + ",";
 							}
 						}
 
 					}
 				}
 
 			}
 
 			if (sectors.length() > 0) {
 				names += "\"Sector\"" + ",";
 				values += "\'" + sectors + "\'" + ",";
 			}
 
 			names = names.substring(0, names.length() - 1);
 			values = values.substring(0, values.length() - 1);
 			out.println(names + ":" + values);
 		}
 
 		try {
 
 			Class.forName("org.postgresql.Driver");
 
 			con = DriverManager.getConnection(
 					"jdbc:postgresql://localhost:54321/geoserver", "postgres",
 					"postgres");
 			stmt = con.createStatement();
 
 			if(editType.equals("edit")){
 				rs = stmt.executeQuery(update);
 			}else{
 				String insert = "INSERT INTO Opengeo.\"DATATABLE\" (" + names
 						+ ") VALUES(" + values + ")";
 						rs = stmt.executeQuery(insert);
 			}
 			/*
 			 * if(col.equals("Sector")){ rs =
 			 * stmt.executeQuery("SELECT DISTINCT \"Sector\" FROM Opengeo.\"DATATABLE\""
 			 * ); }
 			 */
 
 		} catch (ClassNotFoundException e) {
 			out.println("Couldn't load database driver: " + e.getMessage());
 		} catch (SQLException e) {
 			out.println("SQLException caught: " + e.getMessage());
 		} finally {
 			// Always close the database connection.
 			try {
 				if (con != null)
 					con.close();
 			} catch (SQLException ignored) {
 			}
 		}
 	}
 
 	public String checkName(String ch) {
 
 		String newCheck = "";
 
		if (ch.equals("chAdmin")) {
 			newCheck = "Administration";
 		} else if (ch.equals("chAgr")) {
 			newCheck = "Agriculture";
 		} else if (ch.equals("chEd")) {
 			newCheck = "Education";
 		} else if (ch.equals("chEn")) {
 			newCheck = "Energy";
 		} else if (ch.equals("chFin")) {
 			newCheck = "Finance";
 		} else if (ch.equals("chInf")) {
 			newCheck = "Infrastructure";
 		} else if (ch.equals("chRes")) {
 			newCheck = "Resource Management";
 		} else if (ch.equals("chSoc")) {
 			newCheck = "Social Services";
 		} else if (ch.equals("chTel")) {
 			newCheck = "Telecommunications";
 		} else if (ch.equals("chTou")) {
 			newCheck = "Tourism";
 		} else if (ch.equals("chTra")) {
 			newCheck = "Transportation";
 		} else if (ch.equals("chWa")) {
 			newCheck = "Water";
 		} else if (ch.equals("chOth")) {
 
		}
 
 		return newCheck;
 
 	}
 
 	public String Geocode(String line) {
 		//PrintWriter out = res.getWriter();
 		//line = "dfae";
 		String wkt = "";
 
 		line = line.replace(' ', '+');
 		System.out.println(line);
 
 		try {
 			URL google = new URL(
 					"http://maps.googleapis.com/maps/api/geocode/json?address="
 							+ line + "&sensor=false");
 			URLConnection gc = google.openConnection();
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					gc.getInputStream()));
 
 			String inputLine;
 			String json = "";
 
 			while ((inputLine = in.readLine()) != null) {
 
 				json += inputLine;
 			}
 
 			in.close();
 
 			String snip = "";
 
 			if (json.contains("ZERO_RESULTS")) {
 				wkt = "0";
 			} else {
 				snip = json.substring(json.indexOf("\"location\""),
 						json.indexOf("location_type"));
 				snip = snip.substring(snip.indexOf("lat"));
 				String lat = snip.substring(snip.indexOf(':') + 1,
 						snip.indexOf(','));
 				lat = lat.trim();
 				String lon = snip.substring(snip.indexOf("lng"),
 						snip.indexOf('}') - 1);
 				lon = lon.substring(lon.indexOf(":") + 1);
 				lon = lon.trim();
 
 				System.out.println(snip);
 				System.out.println(lat);
 				System.out.println(lon);
 
 				wkt = "ST_GeomFromText(\'POINT(" + lon + " " + lat
 						+ ")\', 4326)";
 
 			}
 		} catch (MalformedURLException e) {
 			//out.println(e.getMessage());
 		} catch (IOException e) {
 			//out.println(e.getMessage());
 		}
 		return wkt;
 	}
 
 }
