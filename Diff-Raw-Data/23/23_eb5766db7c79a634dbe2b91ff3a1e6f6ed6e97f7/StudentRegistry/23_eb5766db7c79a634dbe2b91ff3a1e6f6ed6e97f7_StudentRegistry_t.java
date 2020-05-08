 package dashboard.registry;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 
 import dashboard.error.EmailInUseException;
 import dashboard.error.InvalidEmailException;
 import dashboard.error.InvalidPasswordException;
 import dashboard.error.InvalidUserNameException;
 import dashboard.error.UserNameInUseException;
 import dashboard.model.OwnOfy;
 import dashboard.model.Student;
 
 
 public class StudentRegistry {
 
 	private static List<Student> users = new ArrayList<Student>();
 		
 	/*
 	 * Temporary method to add fake user
 	 */
 	static{
 		loadFromDatastore();
 		//addFakeUser();
 		
 		
 	}
 	
 	public static void addFakeUser(){
 		try {
 			addUser(new Student("voornaam","achternaam","testuser","user@email.com","password"));
 		} catch (EmailInUseException e) {
 			e.printStackTrace();
 		} catch (UserNameInUseException e) {
 			e.printStackTrace();
 		} catch (InvalidUserNameException e) {
 			e.printStackTrace();
 		} catch (InvalidEmailException e) {
 			e.printStackTrace();
 		} catch (InvalidPasswordException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private static void loadFromDatastore(){
 		
 		users = OwnOfy.ofy().query(Student.class).list();
 	}
 	
 	/**
 	 * @return	the users of the userregistry
 	 * 	|	users
 	 */
 	private static List<Student> getUsers() {
 		return users;
 	}
 	
 	/**
 	 * @param 	username
 	 * 	the username of the user you need
 	 * @return
 	 *	the user with the same name
 	 *	|	if(user.contains(user.getUsername().equals(username)))
 	 *	|		return user
 	 * @return
 	 * 	null if no such user exist
 	 * 	|	if(!user.contains(user.getUsername().equals(username)))
 	 *	|		return null
 	 */
 	public static Student getUserByUserName(String username){
 		for(Student user: getUsers())
 			if(user.getUserName().equals(username))
 				return user;
 		return null;
 	}
 	
 	/**
 	 * @param 	mail
 	 * 	the mail address of the user you need
 	 * @return
 	 *	the user with the same mail address
 	 *	|	if(user.contains(user.getMail().equals(mail)))
 	 *	|		return user
 	 * @return
 	 * 	null if no such user exist
 	 * 	|	if(!user.contains(user.getMail().equals(mail)))
 	 *	|		return null
 	 */
 	public static Student getUserByMail(String mail){
		for(Student user: getUsers()){
			List<Student> userslist = getUsers();
 			if(user.getMail().equals(mail))
 				return user;
		}
 		return null;
 	}
 	
 	/**
 	 * @param 	mail
 	 * 	the mail you are looking availability for
 	 * @return
 	 * 	true if the mail dosen't exist
 	 * 	|	(getUserByMail(mail) != null)
 	 */
 	public static boolean isMailExisting(String mail){
 		return (getUserByMail(mail) != null);
 	}
 	
 	/**
 	 * @param 	userName
 	 * 	the userName you are looking availability for
 	 * @return
 	 * 	true if the userName dosen't exist
 	 * 	|	(getUserByUserName(userName) != null)
 	 */
 	public static boolean isUserNameExisting(String userName){
 		return (getUserByUserName(userName) != null);
 	}
 	
 	/**
 	 * @param 	userName
 	 * 	the username of the user
 	 * @param 	password
 	 * 	the password of the user
 	 * @return
 	 * 	true if user exists and password is correct, false otherwise
 	 */
 	public static boolean isValidlogIn(String userName, String password){
 		if(getUserByUserName(userName) != null){
 			return	(StudentRegistry.getUserByUserName(userName).isCorrectPassword(password));
 		} else if(getUserByMail(userName) != null){
 			return (StudentRegistry.getUserByMail(userName).isCorrectPassword(password));
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * @param 	user
 	 * 	the user you want to add
 	 * @throws 	EmailInUseException 
 	 * 	|	(isMailExisting(user.getMail()))
 	 * @throws UserNameInUseException
 	 * 	|	 (isUserNameExisting(user.getUserName()))
 	 * @post
 	 * 	the user was added to users
 	 * 	|	new.getUsers().contains(user)
 	 */
 	public static void addUser(Student user) 
 			throws EmailInUseException, UserNameInUseException{
 		if(isMailExisting(user.getMail()))
 			throw new EmailInUseException();
 		else if(isUserNameExisting(user.getUserName()))
 			throw new UserNameInUseException();
 		else
 			getUsers().add(user);
 	}
 	
 	//TODO
 	//Remove later
 	public static void deleteAll(){
 		users = new ArrayList<Student>();
 	}
 	
 }
