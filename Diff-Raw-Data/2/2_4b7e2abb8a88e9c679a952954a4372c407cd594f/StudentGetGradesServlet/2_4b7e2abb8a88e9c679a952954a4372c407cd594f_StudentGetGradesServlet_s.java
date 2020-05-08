 package edu.harvard.cs262.grading;
 
 import java.io.IOException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.*;
 
 // Will return grades for all the submissions from given student
 
 // not sure how to fix this serialization error
 
 public class StudentGetGradesServlet extends AdminFrontEndServlet{
 
     GradeStorageService gradeStorage;
     SubmissionStorageService submissionStorage;
     
     public void lookupServices() {
 
         try {
             // get reference to database service
             // get reference to database service
         	Registry registry = LocateRegistry.getRegistry();
         	gradeStorage = (GradeStorageService) registry.lookup("GradeStorageService");
         } catch (RemoteException e) {
             System.err.println("AdminGetGradesServlet: Could not contact registry.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NotBoundException e) {
             System.err.println("AdminGetGradesServlet: Could not find GradeStorageService in registry.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         try {
             // get reference to database service
         	Registry registry = LocateRegistry.getRegistry();
         	submissionStorage = (SubmissionStorageService) registry.lookup("SubmissionStorageService");
         } catch (RemoteException e) {
             System.err.println("AdminGetSubmissionsServlet: Could not contact registry.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NotBoundException e) {
             System.err.println("AdminGetSubmissionsServlet: Could not find SubmissionStorageService in registry.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     	
     }
 
     public void init(ServletConfig config) throws ServletException {
 
         super.init(config);
         
         lookupServices();
 
     }
 
     public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
     	
     	// get posted parameters
     	String rawStudent = request.getParameter("student");
     	
     	// attempt to get corresponding grade
     	if(rawStudent == null) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     "parameters not set");
     	} else {
     	
 	    	// try to convert parameters into usable format
 	    	try{
 		    	Integer studentID = Integer.parseInt(rawStudent);
 		    	Student student = new StudentImpl(studentID);
 		    	
 		    	Set<Submission> allSubmissions = submissionStorage.getStudentWork(student);
 		    	Set<Assignment> allAssignments = new HashSet<Assignment>();
 		    	
 		    	for(Submission s : allSubmissions){
 		    		allAssignments.add(s.getAssignment());
 		    	}		    	
 		    	
 		    	// get all grades
 		    	Set<List<Grade>> allGradeLists = new HashSet<List<Grade>>();
 		    	
 		    	for(Assignment assignment : allAssignments){
		    		allGradeLists.add(gradeStorage.getGrade(submissionStorage.getSubmission(student, assignment)));
 		    	}
 		    	
 		    	for(List<Grade> grades : allGradeLists){
 		    	
 		    		StringBuilder responseBuilder = new StringBuilder();
 		    		responseBuilder.append("{grades:[");
 		    		if(grades != null) {
 		    			ListIterator<Grade> gradeIter = grades.listIterator();
 		    			while(gradeIter.hasNext()) {
 		    				Grade grade = gradeIter.next();
 		    				responseBuilder.append("score:");
 		    				responseBuilder.append(grade.getScore().getScore()+"/"+grade.getScore().maxScore());
 		    				responseBuilder.append("}");
 		    			}
 		    		}
 		    		responseBuilder.append("]}");
 	
 		    		response.setContentType("text/Javascript");
 		    		response.setCharacterEncoding("UTF-8");
 		    		response.getWriter().write(responseBuilder.toString());
 		   		}
 	    	} catch (NumberFormatException e){
 	            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 	                    "invalid values given");
 	    	} catch (NullPointerException e) {
 	    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 	    				"grade retrieval failed");
 	    		e.printStackTrace();
 	    	}
 	    
     	}
     }
 }
