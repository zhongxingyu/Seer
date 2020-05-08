 package business;
 
 import hibernate.ConnectionManagement;
 import hibernate.FieldManagement;
 import hibernate.SubjectManagement;
 import hibernate.UserManagement;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 
 import models.Connection;
 import models.Field;
 import models.Subject;
 import models.User;
 
 public class SeekUsersWithGivenSubject {
 	
 	//skal bruk en metode fra usermanagement for  f en liste med brukere
 	private ArrayList<User> mentors;
 	private String subject;
 	private static FieldManagement fm = new FieldManagement();
 	
 	public SeekUsersWithGivenSubject(String subject) {
 		populateDatabase();
 		Field foundsubject = fm.getSingleByTitle(subject);
 		mentors = (ArrayList<User>) fm.getMentors(foundsubject);
 	}
 	
 	private static void populateDatabase(){
 		SubjectManagement cm = new SubjectManagement();
 		UserManagement um = new UserManagement();
 		ConnectionManagement xm = new ConnectionManagement();
 		
 		Subject catJava = new Subject("Java", "THIS IS JAVAAAAA");
 		Subject catCsharp = new Subject("C#", "Kinda like Java but not really");
 		Subject catCplus = new Subject("C++", "Cplusplus");
 		Subject catJavaScript = new Subject("JavaScript", "Programming language for adding dynamic elements in websites");
 		Subject catPython = new Subject("Python", "Programming language, often used as scripting language");
 		Subject catPerl = new Subject("Perl", "Programming language");
 		
 		cm.addSubject(catJava);
 		cm.addSubject(catCsharp);
 		cm.addSubject(catCplus);
 		
 		Field subJava3D = new Field("Java 3D", "Old 3D graphics API for n00bs", cm.getByTitle("Java").get(0));
 		Field subJava2D = new Field("Java 2D", "2D Java graphics API", cm.getByTitle("Java").get(0));
 		
 		Field subCSh1 = new Field("C# field 1", "C# for everybody", cm.getByTitle("C#").get(0));
 		Field subCSh2 = new Field("C#", "C# for extreme", cm.getByTitle("C#").get(0));
 		Field subCSh3 = new Field("C#","C# General",cm.getByTitle("C#").get(0));
 		Field subCpluss = new Field("C++","C++ General",cm.getByTitle("C++").get(0));
 		
 		fm.addField(subJava3D);
 		fm.addField(subJava2D);
 		fm.addField(subCSh1);
 		fm.addField(subCSh2);
 		fm.addField(subCSh3);
 		fm.addField(subCpluss);
 		
		User user1 = new User("1","glenn","falnes","jj@gmail.com","norway");
		User user2 = new User("2","rjand","frodstad","f@gmail.com","norway");
		User user3 = new User("3","flo","fjre","ff@gmail.com","norway");
		User user4 = new User("4","frj","kylling","fk@gmail.com","norway");
		User user5 = new User("5","f","ff","gmail","norway");
		User user6 = new User("6","j","jeg","g","norway");
 		
 		um.addUser(user1);
 		xm.createOpenMentor(user1, subCSh3);
 		xm.createOpenMentor(user1, subJava2D);
 		xm.createOpenTrainee(user1, subCpluss);
 		xm.createOpenTrainee(user1, subJava3D);
 		
 		um.addUser(user2);
 		xm.createOpenMentor(user2, subCSh2);
 		xm.createOpenMentor(user2, subJava2D);
 		xm.createOpenTrainee(user2, subJava3D);
 		
 		um.addUser(user3);
 		xm.createOpenTrainee(user3, subCpluss);
 		xm.createOpenTrainee(user3, subJava2D);
 		
 		um.addUser(user4);
 		xm.createOpenMentor(user4, subCpluss);
 		
 		um.addUser(user5);
 		xm.createOpenMentor(user5, subCpluss);
 		xm.createOpenMentor(user5, subJava3D);
 		xm.createOpenTrainee(user5, subCSh1);
 		
 		um.addUser(user6);
 		xm.createOpenMentor(user6, subJava2D);
 		xm.createOpenTrainee(user6, subJava3D);
 	}
 	
 	public ArrayList<User> getMentorsWithSbject() {
 		return mentors;
 	}
 }
