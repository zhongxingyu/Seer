 package edu.harvard.cs262.grading;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 /*import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;*/
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Iterator;
 import java.util.Set;
 
 
 /**
  * Servlet is the middle communication layer between Administrative
  * front end web app and the SubmissionStorageService. RMI is used to
  * talk to the server, and http is the is used between this
  * servlet and the web app.
  */
 public class AdminGetSubmissionsServlet extends AdminFrontEndServlet {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 11L;
 	// operation codes
     static final int GET_SUBMISSION = 00;
     static final int GET_ALL_FOR_ASSIGNMENT = 01;
     static final int GET_ALL_FOR_STUDENT = 02;
 
     SubmissionStorageService submissionStorage;
     
     public void lookupServices() {
 
         try {
             // get reference to database service
         	Registry registry = LocateRegistry.getRegistry();
         	submissionStorage = (SubmissionStorageService) registry.lookup("SubmissionStorageService");
         	System.err.println("Successfully located submission storage service.");
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
     	
     	// build response
     	StringBuilder responseBuilder = new StringBuilder();
     	
     	// attempt to get submission for corresponding parameters
     	if(rawStudent != null && rawAssignment != null) {
         	
     		Submission submission = null;
     		
         	// try to convert parameters into usable format
         	try{
     	    	Integer studentID = Integer.parseInt(rawStudent);
     	    	Integer assignmentID = Integer.parseInt(rawAssignment);
     	    	Student student = new StudentImpl(studentID);
     	    	Assignment assignment = new AssignmentImpl(assignmentID);
     	    	
     	    	// get submission
    	    	submission = submissionStorage.getLatestSubmission(student, assignment);
     	    	
         	} catch (NumberFormatException e){
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                         "invalid values given");
         	} catch (NullPointerException e) {
         		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
         				"submission retrieval failed");
         		e.printStackTrace();
         	}
         	
         	responseBuilder.append("{submissions:[");
         	if(submission != null) {
     			responseBuilder.append("{student:");
     			responseBuilder.append(submission.getStudent().studentID());
     			responseBuilder.append(",contents:");
     			responseBuilder.append(submission.getContents().toString());
     			responseBuilder.append("}");
         	}
         	responseBuilder.append("]}");
 
         	response.setContentType("text/Javascript");
         	response.setCharacterEncoding("UTF-8");
         	response.getWriter().write(responseBuilder.toString());
     		
     	}
     	else if(rawAssignment != null) {
         	
     		Set<Submission> submissions = null;
     		
         	// try to convert parameters into usable format
         	try{
     	    	Integer assignmentID = Integer.parseInt(rawAssignment);
     	    	Assignment assignment = new AssignmentImpl(assignmentID);
     	    	
     	    	// get submission
     	    	submissions = submissionStorage.getAllSubmissions(assignment);
     	    	
         	} catch (NumberFormatException e){
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                         "invalid values given");
         	} catch (NullPointerException e) {
         		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
         				"submission retrieval failed");
         		e.printStackTrace();
         	}
         	
         	responseBuilder.append("{submissions:[");
         	if(submissions != null) {
         		Iterator<Submission> submissionIter = submissions.iterator();
         		while(submissionIter.hasNext()) {
         			Submission submission = submissionIter.next();
         			responseBuilder.append("{student:");
         			responseBuilder.append(submission.getStudent().studentID());
         			responseBuilder.append(",contents:");
         			responseBuilder.append(submission.getContents());
         			responseBuilder.append("}");
         		}
         	}
         	responseBuilder.append("]}");
 
         	response.setContentType("text/Javascript");
         	response.setCharacterEncoding("UTF-8");
         	response.getWriter().write(responseBuilder.toString());
         	
     	}
     	else if (rawStudent != null) {
         	
     		Set<Submission> submissions = null;
     		
         	// try to convert parameters into usable format
         	try{
     	    	Integer studentID = Integer.parseInt(rawStudent);
     	    	Student student = new StudentImpl(studentID);
     	    	
     	    	// get submission
     	    	submissions = submissionStorage.getStudentWork(student);
     	    	
         	} catch (NumberFormatException e){
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                         "invalid values given");
         	} catch (NullPointerException e) {
         		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
         				"submission retrieval failed");
         		e.printStackTrace();
         	}
         	
         	responseBuilder.append("{submissions:[");
         	if(submissions != null) {
         		Iterator<Submission> submissionIter = submissions.iterator();
         		while(submissionIter.hasNext()) {
         			Submission submission = submissionIter.next();
         			responseBuilder.append("{student:");
         			responseBuilder.append(submission.getStudent().studentID());
         			responseBuilder.append(",contents:");
         			responseBuilder.append(submission.getContents());
         			responseBuilder.append("}");
         		}
         	}
         	responseBuilder.append("]}");
 
         	response.setContentType("text/Javascript");
         	response.setCharacterEncoding("UTF-8");
         	response.getWriter().write(responseBuilder.toString());
     		
     	}
     	else {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     "parameter(s) not set");
     	}
     	
     	/* ObjectStream version
         // use ObjectStream to send objects between web front and servers
         ObjectInputStream in = new ObjectInputStream(request.getInputStream());
         ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
 
         // read in Objects from front end
         Integer opcode;
         try {
             Assignment assignment;
             Student student;
             opcode = (Integer) in.readObject();
             switch(opcode) {
                 case GET_SUBMISSION:
                     Object param1 = in.readObject();
                     if(param1 instanceof Student) {
                         student = (Student) param1;
                         assignment = (Assignment) in.readObject();
 
                     } else if (param1 instanceof Assignment) {
                         assignment = (Assignment) param1;
                         student = (Student) in.readObject();
                     } else {
                         System.err.println("AdminGetSubmissionsServlet: Type mismatch for get submission");
                         response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                 "Invalid Parameters for get submission");
                         break;
                     }
                     out.writeObject(submissionStorage.getSubmission(student, assignment));
                     break;
                 case GET_ALL_FOR_ASSIGNMENT:
                     assignment = (Assignment) in.readObject();
                     out.writeObject(submissionStorage.getAllSubmissions(assignment));
                     break;
                 case GET_ALL_FOR_STUDENT:
                     student = (Student) in.readObject();
                     out.writeObject(submissionStorage.getStudentWork(student));
                     break;
                 default:
                     System.err.println("AdminGetSubmissionsServlet: Unrecognized operation: "+opcode);
                     response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                             "Unrecognized operation");
             }
         } catch (ClassNotFoundException e) {
             System.err.println("AdminGetSubmissionsServlet: Read Object was not of expected type.");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     "Invalid arguments for operation.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (ClassCastException e) {
             System.err.println("AdminGetSubmissionsServlet: Read Object was not of expected type.");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     "Invalid arguments for operation.");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }*/
 
     }
 }
