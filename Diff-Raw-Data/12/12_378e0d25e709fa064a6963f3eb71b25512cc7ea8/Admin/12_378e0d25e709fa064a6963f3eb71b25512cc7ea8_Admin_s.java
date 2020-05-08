 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import models.DbManager;
 import models.User;
 
 import org.apache.commons.io.IOUtils;
 
 import play.Play;
 import play.mvc.Controller;
 import play.mvc.With;
 import xml.XMLParser;
 
 /**
  * This Controller manages administration
  */
 @With(Secure.class)
 public class Admin extends Controller {
 
 	private static DbManager manager = DbManager.getInstance();
 	/** renders user profile to admin */
 	public static void showAdminUserProfile(String user) {
 		ArrayList<User> informationOwner = new ArrayList<User>();
 		informationOwner.add(manager.getUserByName(user));
 		render(informationOwner);
 	}
 
 	/** Admin form for saving changes in user profiles */
 	public static void editInformation(String owner, String name, String email,
 			String password, String password2) throws Throwable {
 
 		String username;
 		if (name.equals("")) {
 			username = owner;
 		} else {
 			username = name;
 		}
 
 		// Checks if user name is already occupied
 		if (!name.equals("") && !name.equals(" ")) {
 			if (!manager.checkUserNameIsOccupied(name)) {
 				manager.getUserByName(owner).setName(name);
 			} else {
 				flash.error("Sorry, this user already exists!");
 				Admin.showAdminUserProfile("");
 			}
 
 		}
 		// Checks if the email has a @ and a dot
 		if (!email.equals("")) {
 			if (email.contains("@") || email.contains(".")) {
 				manager.getUserByName(username).setEmail(email);
 			} else {
 				flash.error("Please re-check your email address!");
 				Admin.showAdminUserProfile("");
 			}
 		}
 		// Checks if two similar password were typed in.
 		if (!password.equals("")) {
 			if (password.equals(password2)) {
 				manager.getUserByName(username).setPassword(password);
 			} else {
 				flash.error("Passwords are not identical!");
 				Admin.showAdminUserProfile("");
 			}
 		}
 
 		redirect("/editUserGroup");
 	}
 
 	public static void showAdminPage() {
 		String uname = session.get("username");
		if (!manager.getUserByName(uname).isAdmin())
 			redirect("/");
 		else
 			render(uname);
 	}
 
 	/** Form for uploading and importing XML-Data files. */
 	public static void importData() {
 		render();
 	}
 
 	public static void loadData(File data) throws Exception {
 		ArrayList<String> message = new ArrayList<String>();
 		if (data != null) {
 			File xmlDir = new File(Play.applicationPath.getAbsolutePath()
 					+ "/public/data");
 			if (!xmlDir.exists()) {
 				xmlDir.mkdir();
 			} else {
 				File existingData = new File(xmlDir + "/data");
 				if (existingData.exists())
 					existingData.delete();
 			}
 
 			File newData = new File(xmlDir.getPath() + "/data");
 
 			try {
 				newData.createNewFile();
 				FileInputStream in = new FileInputStream(data);
 				FileOutputStream out = new FileOutputStream(newData);
 				IOUtils.copy(in, out);
 				out.close();
 				in.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			XMLParser.main();
 			message = XMLParser.getMessage();
 			message.addAll(XMLParser.getReport());
 		} else
 			message.add("An error occured. No Data was imported.");
 
 		if (message.isEmpty()) {
 			message.add("All data imported. No errors occured.");
 			message.addAll(XMLParser.getReport());
 		}
 
 		render(message);
 
 	}
 }
