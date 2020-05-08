 package edu.harvard.cs262.grading;
 
 import java.io.IOException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet is the middle communication layer between Administrative
  * front end web app and the GradeStorageService. RMI is used to
  * talk to the server, and http is the is used between this
  * servlet and the web app.
  */
 public class AdminGetGradesServlet extends AdminFrontEndServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5522723969145795538L;
 
 	// operation codes
     static final int GET_GRADES_FOR_SUBMISSION = 00;
 
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
     	String rawAssignment = request.getParameter("assignment");
     	
     	// attempt to get corresponding grade
     	List<Grade> grades = null;
     	if(rawStudent == null || rawAssignment == null) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     "parameters not set");
     	} else {
     	
 	    	// try to convert parameters into usable format
 	    	try{
 		    	Integer studentID = Integer.parseInt(rawStudent);
 		    	Integer assignmentID = Integer.parseInt(rawAssignment);
 		    	Student student = new StudentImpl(studentID);
 		    	Assignment assignment = new AssignmentImpl(assignmentID);
 		    	
		    	// XXX: This needs to be fixed to make sure to the correct submission, as there might be duplicates
		    	// get grade
		    	grades = gradeStorage.getGrade(submissionStorage.getSubmission(student, assignment));
 		    	
 		    	StringBuilder responseBuilder = new StringBuilder();
 		    	responseBuilder.append("{grades:[");
 		    	if(grades != null) {
 		    		ListIterator<Grade> gradeIter = grades.listIterator();
 		    		while(gradeIter.hasNext()) {
 		    			Grade grade = gradeIter.next();
 		    			responseBuilder.append("{grader:");
 		    			responseBuilder.append(grade.getGrader().studentID());
 		    			responseBuilder.append(",score:");
 		    			responseBuilder.append(grade.getScore().getScore()+"/"+grade.getScore().maxScore());
 		    			responseBuilder.append("}");
 		    		}
 		    	}
 		    	responseBuilder.append("]}");
 	
 		    	response.setContentType("text/Javascript");
 		    	response.setCharacterEncoding("UTF-8");
 		    	response.getWriter().write(responseBuilder.toString());
 		    	
 	    	} catch (NumberFormatException e){
 	            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 	                    "invalid values given");
 	    	} catch (NullPointerException e) {
 	    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 	    				"grade retrieval failed");
 	    		e.printStackTrace();
 	    	}
 	    	
     	}
     	
     	/* ObjectStream version
         // use ObjectStream to send objects between web front and servers
         ObjectInputStream in = new ObjectInputStream(request.getInputStream());
         ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
 
         // read in Objects from front end
         Integer opcode;
         try {
             opcode = (Integer) in.readObject();
             switch(opcode) {
                 case GET_GRADES_FOR_SUBMISSION:
                     Submission submission = (Submission) in.readObject();
                     out.writeObject(gradeStorage.getGrade(submission));
                     response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                             "Unrecognized operation");
                     break;
                 default:
                     System.err.println("AdminGetGradesServlet: Unrecognized operation: "+opcode);
             }
         } catch (ClassNotFoundException e) {
             System.err.println("AdminGetGradesServlet: Read Object was not of expected type.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (ClassCastException e) {
             System.err.println("AdminGetGradesServlet: Read Object was not of expected type.");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     "Invalid arguments for operation.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }*/
 
     }
 }
