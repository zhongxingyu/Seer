 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import java.util.Iterator;
 
 public class Model {
 
 	private UserAuthenticator userAuth;
 	private AuditLog auditLog;
 	private FileStorage fileStorage;
 
 	//list of all existing course names;
 	private LinkedList<String> courseList;
 	// user -> list of courses user is participating in
 	private HashMap<String,LinkedList<String>> userCourses;
 
 	public Model(char[] password) {
 		try {
 			fileStorage = new FileStorage(password);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		try{
 			userAuth = new UserAuthenticator(password);
 		} catch(Exception e){
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 
 		auditLog = new AuditLog();
 		try{
 			this.initCourseList();
 			this.initUserCourses();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 
 	}
 
 
 	public char[] createUser(String username, char[] role)
 		throws Exception{
 		char[] newpass;
 		try{
 			newpass = userAuth.createUser(username,role);
 			this.clearUserCourses(username);
 		} catch (Exception e){
 			auditLog.update(username,"failed user creation with exception: "+e.getMessage());
 			throw e;
 		}
 
 		auditLog.update("admin", "createUser: " + username);
 
 		return newpass;
 
 	}
 
 	public void changePassword(String username, char[] oldPassword, char[] newPassword)
 		throws Exception{
 		try{
 			userAuth.changePassword(username, oldPassword, newPassword);
 		} catch (Exception e){
 			auditLog.update(username,"failed change password with exception: "+e.getMessage());
 			throw e;
 		}
 		auditLog.update(username, "change password");
 	}
 
 	public char[] resetPassword(String username)
 		throws Exception{
 
 		char[] passwd;
 		try{
 			passwd = userAuth.resetPassword(username);
 		} catch (Exception e){
 			auditLog.update(username, "failed reset password with exception: "+e.getMessage());
 			throw e;
 		}
 		auditLog.update(username, "reset password");
 		return passwd;
 	}
 
 
 	public void initCourseList()
 		throws Exception{
 		try{
 			FileInputStream fin = new FileInputStream("courseList.dat");
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			Object obj = ois.readObject();
 			ois.close();
 			if(obj instanceof LinkedList<?>)
 				courseList = (LinkedList<String>) obj;
 			else
 				throw new Exception("Type mismatch on course list");
 		} catch(FileNotFoundException e){
 			courseList = new LinkedList<String>();
 		}
 	}
 
 	public void initUserCourses()
 		throws Exception{		
 		try{
 			FileInputStream fin = new FileInputStream("userCourses.dat");
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			Object obj = ois.readObject();
 			ois.close();
 			if(obj instanceof HashMap<?,?>)
 				userCourses = (HashMap<String,LinkedList<String>>) obj;
 			else
 				throw new Exception("Type mismatch on course list");
 		} catch(FileNotFoundException e){
 			userCourses = new HashMap<String,LinkedList<String>>();
 		}
 	}
 
 	public synchronized boolean appendCourseList(String courseName){
 		if (courseList.contains(courseName))
 			return false;
 		courseList.add(courseName);
 
 		try{
 			FileOutputStream fos = new FileOutputStream("courseList.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject( courseList );
 			oos.close();
 		} catch (Exception e){
 			e.printStackTrace();
 			return false;
 		} 
 		return true;
 	}
 
 	public LinkedList<String> getCourseList(){
 		return courseList;
 	}
 
 	public synchronized void addUserCourse(String username, String courseName)
 		throws Exception{
 		if(!userAuth.userExists(username)){
 			throw new Exception("User not found");
 		}
 		LinkedList<String> userCourseList = userCourses.get(username);
 		userCourseList.add(courseName);
 		userCourses.put(username,userCourseList);
 
 		try{
 			FileOutputStream fos = new FileOutputStream("userCourses.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject( userCourses );
 			oos.close();
 		} catch (Exception e){
 			e.printStackTrace();
 		} 
 
 	}
 
 	public synchronized void clearUserCourses(String username)
 		throws Exception{
 		if(!userAuth.userExists(username)){
 			throw new Exception("User not found");
 		}
 		userCourses.put(username,new LinkedList<String>());
 
 		try{
 			FileOutputStream fos = new FileOutputStream("userCourses.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject( userCourses );
 			oos.close();
 		} catch (Exception e){
 			e.printStackTrace();
 		} 
 	}
 
 	public String[] login(String username, char[] password)
 		throws Exception {
 		String[] returnValue;
 
 		try{
 			returnValue = userAuth.login(username,password);
 		} catch (Exception e){
 			auditLog.update(username,"failed login attempt with exception: "+e.getMessage());
 			throw e;
 		}
 		auditLog.update(username,"login");
 		return returnValue;
 
 	}
 
 	public void createCourse(String professorName, String courseName)
 		throws Exception{
 
 		if(!userAuth.userExists(professorName))
 			throw new Exception("Named professor doesn't exist");
 
 		Course course = new Course(professorName, courseName);
 		fileStorage.updateCourse(course);
 		this.addUserCourse(professorName,courseName);
 		this.appendCourseList(courseName);
 
 	}
 
 	public void addUserToCourse(String username, String courseName, String courseRole)
 		throws Exception{
 
 		if(!userAuth.userExists(username))
 			throw new Exception("User does not exist");
 
 		if(!courseList.contains(courseName))
 			throw new Exception("Course does not exist");
 
 		LinkedList<String> userCourseList = userCourses.get(username);
 		if(userCourseList.contains(courseName))
 			throw new Exception("The specified user is already enrolled");
 
 		Course course = fileStorage.getCourse(courseName);
 		course.addUser(username,courseRole);
 		this.addUserCourse(username,courseName);
 		fileStorage.updateCourse(course);
 	}
 
 	public void createAssignment(String courseName, String assignmentName){
 		Course course = fileStorage.getCourse(courseName);
 		course.createAssignment(assignmentName);
 		fileStorage.makeDir(courseName,assignmentName);
 		fileStorage.updateCourse(course);
 
 	}	
 
 	public void addHandout(String courseName, String handoutName,
 			InputStream is, int filesize) throws Exception{
 		Course course = fileStorage.getCourse(courseName);
 		course.addHandout(handoutName);
 		fileStorage.saveStreamToFile(courseName,handoutName,is,filesize);
 		fileStorage.updateCourse(course);
 
 	}
 
 	public LinkedList<String> getCourseHandouts(String courseName){
 		return fileStorage.getCourse(courseName).getHandouts();
 	}
 
 	public InputStream getHandoutStream(String courseName, String handoutName)
 		throws Exception{
 		if( !courseList.contains(courseName))
 			throw new Exception("Course not found");
 		Course course = fileStorage.getCourse(courseName);
 		if( !course.getHandouts().contains(handoutName))
 			throw new Exception("Handout not found");
 		return fileStorage.getStreamFromFile(courseName,handoutName);
 
 	}
 
 	public LinkedList <Assignment> getAssignmentsFor(String courseName, String username){
 		Course course = fileStorage.getCourse(courseName);
 		return course.getAssignmentsFor(username);
 
 	}
 
 	public void makeSubmission(String courseName, String username,
 			String assignmentName, InputStream is, int filesize)
 		throws Exception{
 		Course course = fileStorage.getCourse(courseName);
 		course.makeSubmission(assignmentName,username);
 		fileStorage.saveStreamToFile(courseName,assignmentName+"/"+username,is,filesize);
 		fileStorage.updateCourse(course);
 	}
 
 	public HashMap<String,Assignment> getSubmissions(String courseName,
 			String assignmentName) throws Exception{
 		return fileStorage.getCourse(courseName).getSubmissions(assignmentName);
 
 	}
 
 	public InputStream getSubmissionFile(String courseName,
 			String assignmentName, String userName) throws Exception{
 		return fileStorage.getStreamFromFile(courseName,assignmentName+"/"+userName);
 	}
 
 	public void updateGradesFor(String courseName, String assignmentName,
 			HashMap<String,Assignment> assignmentHash) throws Exception{
 		Course course = fileStorage.getCourse(courseName);
 		//TODO Check that assignment exists!!! This is actually important.
 		course.gradeAssignment(assignmentName, assignmentHash);
 		fileStorage.updateCourse(course);
 	}
 
 	public void saveFile(InputStream is, String filename, int filesize,
 			String username) {
 
 		fileStorage.saveFile(is,filename,filesize,username);
 		auditLog.update(username,"Saved file: "+filename);
 
 	}
 
 	private static void testAuth(Model model){
 		String[] login_out;
 		try{
 			model.login("admin", "123456".toCharArray());
 
 			char[] alicepass = model.createUser("alice","Student".toCharArray());
 			model.changePassword("alice",alicepass,"alicepass".toCharArray());
 			login_out = model.login("alice","alicepass".toCharArray());
 			System.out.println(login_out[0]+' '+login_out[1]+' '+login_out[2]);
 
 
 		} catch (Exception e) {
 			if (e.getMessage().equals("invalpass") || e.getMessage().equals("No such user"))
 				System.out.println("Failed successfully");
 			else{
 				System.out.println("Failed at failing");
 				e.printStackTrace();
 			}
 		}
 
 		for(int i=0; i<6; i++){
 			try{
 				model.login("alice","badpass".toCharArray());
 			} catch(Exception e){
 				System.out.println("Intentional bad password number: "+i);
 			}
 		}
 
 		//This should fail.
 
 		try{
 			model.login("alice","alicepass".toCharArray());
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 
 		try{
 			char[] tempPass = model.resetPassword("alice");
 			model.changePassword("alice",tempPass,"alicepass".toCharArray());
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 
 		//This should succeed.
 
 		try{
 			model.login("alice","alicepass".toCharArray());
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	private static void testCourseCreation(Model model)
 		throws Exception{
 		//Create some users
 		model.createUser("Alice","Professor".toCharArray());
 		model.createUser("Bob","Student".toCharArray());
 		model.createUser("SomeGuy","TA".toCharArray());
 
 		//Make a course and enroll people.
 		model.createCourse("Alice","ECE4750");
 		model.addUserToCourse("Bob","ECE4750","Student");
 		model.addUserToCourse("SomeGuy","ECE4750","TA");
 
 		//Make a handout for an assignment.
 		File handoutFile = new File("handout.testfile");
 		FileInputStream handoutStream = new FileInputStream(handoutFile);
 		model.addHandout("ECE4750","AssignmentOneHandout",handoutStream,(int)handoutFile.length());
 
 		//Make an assignment
 		model.createAssignment("ECE4750","AssignmentOne");
 
 	}
 
 	private static void testCreationWithFiles(Model model)
 		throws Exception{
 
 		//Retrieve a handout
 		FileOutputStream handoutOut = new FileOutputStream("handout.output");
 		InputStream is = model.getHandoutStream("ECE4750","AssignmentOneHandout");
 		//TODO test this with a filesize.
 		byte[] buf = new byte[1024];
 		int bytesRead;
 		while( (bytesRead = is.read(buf)) != -1 ){
 				handoutOut.write(buf,0,bytesRead);
 		}
 		handoutOut.close();
 
 		//Send a submission.
 		File subfile = new File("AssignOne.in");
 		FileInputStream submissionStream = new FileInputStream(subfile);
 		model.makeSubmission("ECE4750","Bob","AssignmentOne",submissionStream,
 			(int) subfile.length() );
 
 		//Retrieve submission.
 		InputStream subIs = model.getSubmissionFile("ECE4750",
 			"AssignmentOne","Bob");
 		FileOutputStream subFOS = new FileOutputStream("AssignmentOne.out");
 		//TODO test this with a filesize.
 		buf = new byte[1024];
 		while( (bytesRead = subIs.read(buf)) != -1 ){
 				subFOS.write(buf,0,bytesRead);
 		}
 	    subFOS.close();
 
 		//grade the assignment
 		HashMap<String,Assignment> assignOneHash = model.getSubmissions(
 				"ECE4750","AssignmentOne");
 		Assignment bobSub = assignOneHash.get("Bob");
 		bobSub.grade=97;
 		assignOneHash.put("Bob",bobSub);
 		model.updateGradesFor("ECE4750","AssignmentOne",assignOneHash);
 		
 
 
 	}
 
 	public static void testCourseRetrieval(Model model)
 		throws Exception{
 		System.out.println("Get the List of Courses");
 		Iterator<String> courseList = model.getCourseList().iterator();
 		while (courseList.hasNext())
 			System.out.println(courseList.next());
 		System.out.println("-----------------------------------------\n");
 
 		System.out.println("Get the list of Handouts");	
 		Iterator<String> handoutList= model.getCourseHandouts("ECE4750").iterator();
 		while (handoutList.hasNext())
 			System.out.println(handoutList.next());
 		System.out.println("-----------------------------------------\n");
 
 		System.out.println("Get assignments for Bob");
 		Iterator<Assignment> assignmentList= model.getAssignmentsFor("ECE4750","Bob").iterator();
 		while (assignmentList.hasNext()){
 			Assignment a = assignmentList.next();
 			System.out.println(a.assignmentName+" has grade "+a.grade);
 		}
 		System.out.println("-----------------------------------------\n");
 	}
 
 	public static void main(String[] args) {
 		Model model = new Model("123456".toCharArray());
 		try{
			testCourseCreation(model);
			testCreationWithFiles(model);
			testCourseRetrieval(model);
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 
 
 }
