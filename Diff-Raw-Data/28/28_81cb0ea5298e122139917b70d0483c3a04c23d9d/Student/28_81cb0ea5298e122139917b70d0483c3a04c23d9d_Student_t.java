 package dashboard.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.persistence.Id;
 
 import com.googlecode.objectify.annotation.Serialized;
 
 import dashboard.error.AlreadyEndedException;
 import dashboard.error.AlreadyRequestedException;
 import dashboard.error.CourseAlreadyTakenException;
 import dashboard.error.InvalidAmountException;
 import dashboard.error.InvalidEmailException;
 import dashboard.error.InvalidEndDateException;
 import dashboard.error.InvalidPasswordException;
 import dashboard.error.InvalidStudyMomentException;
 import dashboard.error.InvalidUserNameException;
 import dashboard.error.NameAlreadyInUseException;
 import dashboard.error.NoSuchCourseException;
 import dashboard.error.NotFriendException;
 import dashboard.error.NotStudyingException;
 import dashboard.util.OwnOfy;
 
 public class Student implements Comparable<Student>,Cloneable,Serializable {
 
 	private static final long serialVersionUID = -6268846212512642033L;
 	@Id private Long id;
 	private String firstName;
 	private String lastName;
 	private String userName;
 	private String mail;
 	private String password;
 	@Serialized private StudyMoment currentStudyMoment;
 	@Serialized private ArrayList<String> friendList;
 	@Serialized private ArrayList<String> friendRequests;
 	@Serialized private ArrayList<StudyMoment> studyMoments;
 	@Serialized private ArrayList<Location> starredLocations;
 	@Serialized private ArrayList<CourseContract> courses;
 	public Student(){
 	}
 
 	/**
 	 * initiates a user
 	 * @param 	firstName
 	 * the first name you want your student to have
 	 * @param 	lastName
 	 * the last name you want your student to have
 	 * @param	userName
 	 * the username you want your user to have
 	 * @param	mail
 	 * the mail adress you want your user to have
 	 * @param	passWord
 	 * the password you want your user to have
 	 * @throws InvalidUserNameException 
 	 *	|	(!isValidUserName(userName))
 	 * @throws InvalidEmailException 
 	 *	|	(!isValidUserMail(mail))
 	 * @throws InvalidPasswordException 
 	 *	|	(!isValidPassword(password))
 	 * @effect
 	 * setName(name);
 	 * @effect
 	 * setPassword(passWord);
 	 * @post
 	 * new.getUserName() = userName
 	 * @post
 	 * new.getMail() = mail
 	 */
 	public Student(String firstName, String lastName, String userName, String mail, String password)
 			throws InvalidUserNameException, InvalidEmailException, InvalidPasswordException{
 		if(!isValidUserName(userName))
 			throw new InvalidUserNameException();
 		if(!isValidMail(mail))
 			throw new InvalidEmailException();
 		if(!isValidPassword(password))
 			throw new InvalidPasswordException();
 		this.userName = userName;
 		this.mail = mail;
 		this.password = password;
 		setFirstName(firstName);
 		setLastName(lastName);
 		convertEmptyArrayLists();
 		OwnOfy.ofy().put(this);
 	}
 	
 	/**
 	 * @return	
 	 *	the first name of the student
 	 * 	|	first name
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	/**
 	 * @param firstName
 	 * the new first name of the user
 	 * @post	the first name was changed
 	 * 	|	new.getFirstName() = firstName
 	 */
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 		OwnOfy.ofy().put(this);
 	}
 
 	/**
 	 * @return	
 	 *	the last name of the student
 	 * 	|	last name
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 	
 
 	/**
 	 * @param lastName
 	 * the new last name of the user
 	 * @post	the last name was changed
 	 * 	|	new.getLastName() = lastName
 	 */
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 		OwnOfy.ofy().put(this);
 	}
 
 	/**
 	 * @return	
 	 * the username of the student
 	 * 	|	userName
 	 */
 	public String getUserName() {
 		return userName;
 	}
 	
 	/**
 	 * @return
 	 * the mail address of the user
 	 * 	|	mail
 	 */
 	public String getMail() {
 		return mail;
 	}
 	
 	/**
 	 * @return
 	 * the password of the user
 	 * 	|	password
 	 */
 	public String getPassword() {
 		return password;
 	}
 	
 	public StudyMoment getMoment(String momentString){
 		for(StudyMoment moment : getStudyMoments())
 			if(moment.getStart().toString().equals(momentString))
 				return moment;
 		return null;
 	}
 	
 	/**
 	 * @param password
 	 * 	the new password of the user
 	 * @throws InvalidPasswordException 
 	 * @post	the password was changed
 	 * 	|	new.getPassword() = password
 	 */
 	public void setPassword(String password) throws InvalidPasswordException {
 		if(!isValidPassword(password))
 			throw new InvalidPasswordException();
 		this.password = password;
 		OwnOfy.ofy().put(this);
 	}
 
 	/**
 	 * @param 	password
 	 * 	the password that has to be checked
 	 * @return
 	 * 	true if the password matches this user's password
 	 * 	|	(getPassword().equals(password))
 	 */
 	public boolean isCorrectPassword(String password){
 		return (getPassword().equals(password));
 	}
 
 	/**
 	 * @return
 	 * 	the current study moment 
 	 * 	|	currentStudyMoment
 	 */
 	public StudyMoment getCurrentStudyMoment() {
 		return currentStudyMoment;
 	}
 	
 	/**
 	 * @param currentStudyMoment
 	 * 	the studymoment you want to save as current studymoment
 	 * @post	the current studymoment was changed	
 	 * 	| 	new.getCurrentStudyMoment() = studyMoment
 	 */
 	public void setCurrentStudyMoment(StudyMoment currentStudyMoment) {
 		this.currentStudyMoment = currentStudyMoment;
 		OwnOfy.ofy().put(this);
 	}
 
 	/**
 	 * @param amount
 	 * 	the amount he studied
 	 * @param kind
 	 * 	what kind of studying he did
 	 * @throws AlreadyEndedException
 	 * @throws InvalidAmountException
 	 * @throws InvalidStudyMomentException 
 	 * @effect
 	 * 	|	StudyMoment moment = getCurrentStudyMoment()
 	 * 	|	addStuddyMoment(moment)
 	 * @effect
 	 * 	|	StudyMoment moment = getCurrentStudyMoment()
 	 * 	|	setCurrentStudyMoment(null)
 	 */
 	public void endStudying(Date endDate, int amount, String kind) 
 			throws AlreadyEndedException, InvalidAmountException,InvalidEndDateException, InvalidStudyMomentException{
 		StudyMoment moment = getCurrentStudyMoment();
 		moment.endMoment(endDate, amount, kind);
 		addStudyMoment(moment);
 		setCurrentStudyMoment(null);
 	}
 
 	/**
 	 * @throws NotStudyingException 
 	 * 	|	getCurrentStudyMoment() == null
 	 * @effect
 	 * 	|	setCurrentStudyMoment(null)
 	 */
 	public void cancelCurrentStudyMoment() throws NotStudyingException{
 		if(getCurrentStudyMoment() == null)
 			throw new NotStudyingException();
 		setCurrentStudyMoment(null);
 	}
 
 	/**
 	 * @return
 	 * 	the studymoments of this student
 	 * 	|	studyMoments
 	 */
 	public ArrayList<StudyMoment> getStudyMoments() {
 		return studyMoments;
 	}
 	
 	public void setStudyMoments(ArrayList<StudyMoment> studyMoments) {
 		this.studyMoments = studyMoments;
 	}
 	
 	/**
 	 * @param moment
 	 * 	the moment you want to add
 	 * @throws InvalidStudyMomentException 
 	 * @post
 	 * 	the moment was added to the student's studymoments
 	 * 	|	new.studyMoments.contains(moment)
 	 */
 	public void addStudyMoment(StudyMoment moment) throws InvalidStudyMomentException {
 		if(!IsValidStudyMoment(moment))
 			throw new InvalidStudyMomentException();
 		if(getStudyMoments().isEmpty()){
 			getStudyMoments().add(moment);
 			OwnOfy.ofy().put(this);
 			return;
 		}
 		for(int i = getStudyMoments().size() - 1; i!=-1; i--)
 			if(moment.getStart().after(getStudyMoments().get(i).getStart())){
 				getStudyMoments().add(i + 1, moment);
 				OwnOfy.ofy().put(this);
 				return;
 			}
 		getStudyMoments().add(0, moment);
 		OwnOfy.ofy().put(this);
 		return;
 	}
 
 	public long getTotalTimeStudied(){
 		long time = 0;
 		for(StudyMoment s: studyMoments){
 			time += s.getTime();
 		}
 		return time;
 	}
 
 	/**
 	 * @return
 	 * 	the courses of the student
 	 * 	|	courses
 	 */
 	public ArrayList<CourseContract> getCourses() {
 		return courses;
 	}
 	
 	/**
 	 * Returns a list of the courses the student is taking, NOT THE COURSECONTRACTS!!!!
 	 * @return
 	 */
 	public ArrayList<Course> getCourseList(){
 		ArrayList<Course> courseList = new ArrayList<Course>();
 		for(CourseContract courseContract: courses){
 			courseList.add(courseContract.getCourse());
 		}
 		return courseList;
 	}
 
 	/**
 	 * @param courses
 	 * @post
 	 * 	the courses have been changed
 	 * 	|	new.getCourses() = courses
 	 */
 	public void setCourses(ArrayList<CourseContract> courses) {
 		this.courses = courses;
 		OwnOfy.ofy().put(this);
 	}
 
	public void setFriendList(ArrayList<String> friendList) {
		this.friendList = friendList;
	}
	
	public void setFriendRequests(ArrayList<String> friendRequests) {
		this.friendRequests = friendRequests;
	}
	
	public void setStarredLocations(ArrayList<Location> starredLocations) {
		this.starredLocations = starredLocations;
	}
	
 	/**
 	  * @param course
 	 * 	the course you want to add
 	 * @throws CourseAlreadyTakenException 
 	 * @post
 	 * 	the courseContract was added to the student's courses
 	 * 	|	new.courses.contains(course)
 	 */
 	public void addCourse(CourseContract course) throws CourseAlreadyTakenException{
 		for(CourseContract userCourse : getCourses()){
 			if(userCourse.getCourse().getName().equals(course.getCourse().getName())){
 				throw(new CourseAlreadyTakenException());
 			}
 		}
 		getCourses().add(course);
 		OwnOfy.ofy().put(this);
 	}
 
 	/**
 	 * @param	courseName
 	 * 	the name of the course you want to remove
 	 * @throws NoSuchCourseException 
 	 * |	!getCourses().contains(course)
 	 */
 	public void removeCourse(String courseName) throws NoSuchCourseException {
 		for(CourseContract course : getCourses()){
 			if(course.getCourse().getName().equals(courseName)){
 				getCourses().remove(course);
 				OwnOfy.ofy().put(this);
 				return;
 			}
 		}
 		throw(new NoSuchCourseException());
 	}
 
 	public void removeMoment(String momentString){
 		getStudyMoments().remove(getMoment(momentString));
 		OwnOfy.ofy().put(this);
 	}
 	
 	/**
 	 * @return
 	 *  the friend list of the student
 	 *  | 	friendList
 	 */
 	public ArrayList<String> getFriendList(){
 		return friendList;
 	}
 	
 	/**
 	 * @param userName
 	 * the user name to add to friend list
 	 * @throws InvalidUserNameException 
 	 * 
 	 */
 	public void addFriend(String userName) {
 		getFriendList().add(userName);
 		OwnOfy.ofy().put(this);
 	}
 
 	/**
 	 * @param userName
 	 * 
 	 */
 	public void removeFriend(String userName) throws NotFriendException{
 		if(!isAFriend(userName))
 			throw new NotFriendException();
 		friendList.remove(userName);
 		OwnOfy.ofy().put(this);
 	}
 
 	/**
 	 * @param userName
 	 * the user name that has to be checked
 	 * @return
 	 * true is the user name matches a friend's user name
 	 * |	friendList.contains(userName)
 	 */
 	public boolean isAFriend(String userName){
 		return friendList.contains(userName);
 	}
 
 	/**
 	 * @return
 	 *  the requested friends of the student
 	 *  |	friendRequests
 	 */
 	public ArrayList<String> getFriendRequests() {
 		return friendRequests;
 	}
 	
 	/**
 	 * @param username
 	 * the user name who requested you as a friend
 	 */
 	public void requestedAsFriend(String userName) 
 			throws AlreadyRequestedException{
 		if(getFriendRequests().contains(userName))
 			throw new AlreadyRequestedException();
 		getFriendRequests().add(userName);
 		OwnOfy.ofy().put(this);
 	}
 
 	public void removeRequest(String username){
 		if(getFriendRequests().contains(username))
 			getFriendRequests().remove(username);
 		OwnOfy.ofy().put(this);
 	}
 
 	public int getRequestNumbers(){
 		return getFriendRequests().size();
 	}
 
 	/**
 	 * @return
 	 * 	the starred locations of the student
 	 * 	|	starredLocations
 	 */
 	public ArrayList<Location> getStarredLocations(){
 		return starredLocations;
 	}
 
 	/**
 	  * @param location
 	 * 	the starred location you want to add
 	 * @post
 	 * 	the location was added to the student's starred locations
 	 * 	|	new.starredLocations.contains(location)
 	 */
 	public void addStarredLocation(Location location) throws NameAlreadyInUseException{
 		String name = location.getAlias();
 		for(Location existing : getStarredLocations()){
 			if(name.equals(existing.getAlias())){
 	 		 throw(new NameAlreadyInUseException());}
 		}
 		getStarredLocations().add(location);
 		OwnOfy.ofy().put(this);
 	}
 	
 	/**
 	 * match a location with a starred location
 	 * @return
 	 * the starred location if a match was found
 	 * @return
 	 * null if no match was found
 	 */
 	public  Location matchStarredLocation(Location location,double radius) {
 		Iterator<Location> it = starredLocations.iterator();
 		Location bestMatch = null;
 		double distance = radius;
 		while(it.hasNext()){
 			Location loc = it.next();
 			double dist = loc.distanceWorstCase(location);
 			if(dist<distance){
 				distance = dist;
 				bestMatch = loc;
 			}	
 		}
 		return bestMatch;
 		
 	}
 
 	public void convertEmptyArrayLists(){
 		if(studyMoments == null)
 			studyMoments = new ArrayList<StudyMoment>();
 		if(courses == null)
 			courses = new ArrayList<CourseContract>();
 		if(friendList == null)
 			friendList = new ArrayList<String>();
 		if(friendRequests == null)
 			friendRequests = new ArrayList<String>();
 		if(starredLocations == null)
 			starredLocations = new ArrayList<Location>();
 	}
 
 	/**
 	 * compares user with other user
 	 * @return
 	 * 0 if the usernames of both users match
 	 * 	|	if(other.getUserName().equals(getUserName()))
 	 *	|		return 0
 	 * @return
 	 * -1 if the usernames of both users match
 	 * 	|	if(!other.getUserName().equals(getUserName()))
 	 *	|		return -1
 	 */
 	public int compareTo(Student other) {
 		if(other.getUserName().equals(getUserName()))
 			return 0;
 		else
 			return -1;
 	}
 	
 	public Object clone() throws CloneNotSupportedException {
 		Student clone = null;
 		try {
 			clone = new Student(getFirstName(),getLastName(),getUserName(),getMail(),getPassword());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		clone.setStudyMoments((ArrayList<StudyMoment>) getStudyMoments().clone());
		clone.setStarredLocations((ArrayList<Location>) starredLocations.clone());
		clone.setFriendRequests((ArrayList<String>) friendRequests.clone());
		clone.setFriendList((ArrayList<String>) getFriendList().clone());
		setCourses((ArrayList<CourseContract>) getCourses().clone());
 		clone.setCurrentStudyMoment(getCurrentStudyMoment());
 		return clone;
 	}
 	
 	/**
 	 * checks the validity of the username
 	 * @param userName
 	 * 	the username that has to be checked
 	 * @return
 	 * 	true, if it is a valid username
 	 * 	|	(userName.length() > 5) && (userName.length() < 25) &&
 	 *	|	(userName.matches("^[a-zA-Z_0-9]+$"))
 	 */
 	private boolean isValidUserName(String userName){
 		return 	(userName.length() > 5) && (userName.length() < 25) &&
 				(userName.matches("^[a-zA-Z_0-9]+$"));
 	}
 
 	/**
 	 * checks the validity of the mail address
 	 * @param 	mail
 	 * 	the mail address that has to be checked
 	 * @return
 	 * 	true, if it is a valid mail address
 	 * 	|	(mail.contains("@"))
 	 */
 	private boolean isValidMail(String mail) {
 		return	(mail.contains("@"));
 	}
 
 	/**
 	 * checks the validity of the password
 	 * @param	password
 	 * 	the password that has to be checked
 	 * @return
 	 * 	true, if the password is valid
 	 * 	|	(password.length() > 5) && (password.length() < 25)
 	 */
 	private boolean isValidPassword(String password){
 		return 	(password.length() > 5) && (password.length() < 25);
 	}
 
 	/**
 	 * @param moment
 	 * the moment that has to be checked
 	 * @return
 	 * true if the studymoment is valid
 	 *  |	!moment.overlaps(studyMoments)
 	 */
 	private boolean IsValidStudyMoment(StudyMoment moment) {
 		boolean isValidMoment = true;
 		for(int i = 0; i < studyMoments.size(); i++){
 			StudyMoment momentToCheck = studyMoments.get(i);
 			Date a = momentToCheck.getStart();
 			Date b = momentToCheck.getEnd();
 			Date c = moment.getStart();
 			Date d = moment.getEnd();
 			if(c.after(a) && c.before(b))
 				isValidMoment = false;			
 			if(d.after(a) && d.before(b))
 				isValidMoment = false;
 			if(a.after(c) && a.before(d))
 				isValidMoment = false;
 		}
 			return isValidMoment;
 	}
 
 }
