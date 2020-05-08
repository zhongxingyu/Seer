 package main.web;  
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import main.util.DBConnection;
 import main.util.ReportRow;
 
 public class DataAnalysis extends HttpServlet {
     private static final long serialVersionUID = 1L;
     
     //Populates the drop down menus for DataAnalysis.jsp
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection myConn = null;
         try{
             
             // Check to make sure that only "admin" can access this screen.
             HttpSession session = request.getSession();
             String username = (String) session.getAttribute("username");
             if (username == null || !username.equals("admin")) {
                 request.setAttribute("errorMessage", "You must be an administrator to view this screen.");
                 request.setAttribute("errorBackLink", "/PhotoWebApp/Home");
                 request.getRequestDispatcher("/Error.jsp").forward(request, response);
                 return;
             }
             // Find all possible users and subjects the administrator may wish to query with
        	 	myConn = DBConnection.createConnection();
        	 	ArrayList<String> users = new ArrayList<String>();
        	 	ArrayList<String> subjects = new ArrayList<String>();
        	 	
        	 	String userQuery = "SELECT user_name FROM persons";
        	 	String subjectQuery = "SELECT DISTINCT subject FROM images";
        	 	
        	 	PreparedStatement findUsers = myConn.prepareStatement(userQuery);
        	 	PreparedStatement findSubjects = myConn.prepareStatement(subjectQuery);
        	 	
        	 	ResultSet userResults = findUsers.executeQuery();
        	 	ResultSet subjectResults = findSubjects.executeQuery();
        	 	
        	 	while (userResults.next()) {
        	 		users.add(userResults.getString("user_name"));
        	 	}
        	 	while (subjectResults.next()){
        	 		subjects.add(subjectResults.getString("subject"));
        	 	}
        	 	
        	 	request.setAttribute("users", users);
        	 	request.setAttribute("subjects", subjects);
             userResults.close();
             subjectResults.close();
         } catch(Exception ex) {
             System.out.println("An error occured while loading the data analysis screen: " + ex);
             request.setAttribute("errorMessage", "An error occured while loading the data analysis screen. Please try again.");
             request.setAttribute("errorBackLink", "/PhotoWebApp/Home");
             request.getRequestDispatcher("/Error.jsp").forward(request, response);
             return;
         } finally {
             try {
                 myConn.close();
             } catch (Exception ex) {
                 System.out.println("An error occured while loading the data analysis screen: " + ex);
                 request.setAttribute("errorMessage", "An error occured while loading the data analysis screen. Please try again.");
                 request.setAttribute("errorBackLink", "/PhotoWebApp/Home");
                 request.getRequestDispatcher("/Error.jsp").forward(request, response);
                 return;
             }
         }
         
 		request.getRequestDispatcher("/DataAnalysis.jsp").forward(request, response);
 	}
     
 	//Generates the OLAP results displayed by DataAnalysisResults
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         
     	 String user = request.getParameter("user");
     	 String subject = request.getParameter("subject");
          String fromDate = request.getParameter("fromDate");
          String toDate = request.getParameter("toDate");
          String drillDown = request.getParameter("drillDown");
          //If coming from DataAnalysis.jsp the first time drillDown will be null
          if(drillDown == null){
         	 drillDown = "None";
          }
          
          //Build the query based on the fields
          String select = "SELECT owner_name, subject,";
          String from = "FROM fact_table";
          String where = "";
          String group = " GROUP BY owner_name, subject";
          String order = " ORDER BY owner_name, subject";
          
          if(!user.equals("All") && subject.equals("All")){
         	 where += " WHERE owner_name = ? ";
          }else if(user.equals("All") && !subject.equals("All")){
              if(subject.isEmpty()){
                  where += " WHERE subject IS NULL ";
              } else {
                  where += " WHERE subject = ? ";
              }
          }else if(!user.equals("All") && !subject.equals("All")){
              if(subject.isEmpty()){
                  where += " WHERE owner_name = ? AND subject IS NULL ";
              } else {
                  where += " WHERE owner_name = ? AND subject = ? ";
              }
          }
          
          if(drillDown.equals("Yearly")){
         	 select += " year, COUNT(*) ";
         	 group += ", year";
         	 order += ", year";
     	 }else if(drillDown.equals("Monthly")){
     		 select += " month, COUNT(*) ";
 
     		 group += ", month";
     		 order += ", month";
     	 }else if(drillDown.equals("Weekly")){
     		 select += " week, COUNT(*) ";
     		 group += ", week";
     		 order += ", week";
     	 }else{
     		 select += " null, COUNT(*) ";
     	 }
          
          if(!fromDate.isEmpty()){
         	 if(user.equals("All") && subject.equals("All") && drillDown.equals("None")){
         		 where += " WHERE ";
         	 }else{
         		 where += " AND ";
         	 }
         		 
         	 where += " time >=  TO_DATE( ?, 'DD/MM/YYYY HH24:MI:SS') ";
          }
          if(!toDate.isEmpty()){
         	 if(user.equals("All") && subject.equals("All") && fromDate.isEmpty() && drillDown.equals("None")){
         		 where += " WHERE ";
         	 }else{
         		 where += " AND ";
         	 }
         	 where += " time <= TO_DATE( ?, 'DD/MM/YYYY HH24:MI:SS') ";
          }
          
          //Assemble query and execute
          String query = select + from + where + group + order;
          Connection myConn = null;
          try{
         	 	myConn = DBConnection.createConnection();
         	 	
         	 	PreparedStatement getReport = myConn.prepareStatement(query);
         	 	int n = 1;
         	    if(!user.equals("All")){
         	    	getReport.setString(n, user);
         	    	n++;
                 }
         	    if(!subject.equals("All")){
         	    	if(!subject.isEmpty()){
         	    		getReport.setString(n, subject);
         	    	}
                	 	n++;
                 }
         	    if(!fromDate.isEmpty()){
                	 	getReport.setString(n, fromDate);
                	 	n++;
                 }
         	    if (!toDate.isEmpty()){
         	    	getReport.setString(n, toDate);
         	    	n++;
         	    }
         	    
         	 	ResultSet report = getReport.executeQuery();
         	 	ArrayList<ReportRow> rows = new ArrayList<ReportRow>();
        	 	
        	 	// No results: display an error
        	 	if (!report.isBeforeFirst() ) {    
                    request.setAttribute("errorMessage", "There are no results for the given parameters. Please try again.");
                    request.setAttribute("errorBackLink", "/PhotoWebApp/DataAnalysis");
                    request.getRequestDispatcher("/Error.jsp").forward(request, response);
                    return;
        	 	}
        	 	
         	 	while (report.next()){
         	 		ReportRow row = new ReportRow();
 
         	 		if(user.equals("All") && subject.equals("All")){
         	 			row.setCol1(report.getString(1));
         	 			row.setCol3(report.getString(2));
         	 		}else if(!subject.equals("All") && user.equals("All")){
         	 			row.setCol3(report.getString(1));
         	 		}else if(!user.equals("All") && subject.equals("All")){
         	 			row.setCol3(report.getString(2));
         	 		}else if(!user.equals("All") && !subject.equals("All")){
         	 		}
         	 		
         	 		if(!drillDown.equals("None")){
         	 			row.setCol2(report.getString(3));
         	 		}
         	 		
         	 		row.setCol4(Integer.toString(report.getInt(4)));
         	 		rows.add(row);
         	 	}
         	 	report.close();
         	 	if(!user.equals("All")){
         	 		request.setAttribute("head1", "User");
                	 	request.setAttribute("head3", "Subject");
                	 	rows.get(0).setCol1(user);
                	 	if(!subject.equals("All")){
                	 	rows.get(0).setCol3(subject);
                	 	}
     	 		}else if(!subject.equals("All")){
     	 			request.setAttribute("head1", "Subject");
                	 	request.setAttribute("head3", "User");
                	 	rows.get(0).setCol1(subject);
     	 		}else{
     	 			request.setAttribute("head1", "User");
                	 	request.setAttribute("head3", "Subject");
     	 		}
     	 		request.setAttribute("head2", "Time");
            	 	request.setAttribute("head4", "Total");
           	 	request.setAttribute("drillDown", drillDown);
     	 		
          		if(drillDown.equals("None")){
          			rows.get(0).setCol2(fromDate + " ~ " + toDate);
     	 		}
         	 	request.setAttribute("reportRows", rows);
          } catch(Exception ex) {
              System.out.println("An error occured while performing the data analysis: " + ex);
              request.setAttribute("errorMessage", "An error occured while performing the data analysis. Please try again.");
              request.setAttribute("errorBackLink", "/PhotoWebApp/DataAnalysis");
              request.getRequestDispatcher("/Error.jsp").forward(request, response);
              return;
          } finally {
              try {
                  myConn.close();
              } catch (Exception ex) {
                  System.out.println("An error occured while performing the data analysis: " + ex);
                  request.setAttribute("errorMessage", "An error occured while performing the data analysis. Please try again.");
                  request.setAttribute("errorBackLink", "/PhotoWebApp/DataAnalysis");
                  request.getRequestDispatcher("/Error.jsp").forward(request, response);
                  return;
              }
          }
          request.getRequestDispatcher("/DataAnalysisResults.jsp").forward(request, response);
     }   
 }
