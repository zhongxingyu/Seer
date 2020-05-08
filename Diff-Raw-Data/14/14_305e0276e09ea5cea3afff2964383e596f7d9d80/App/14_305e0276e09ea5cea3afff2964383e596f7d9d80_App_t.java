 package com.oshmidt;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Scanner;
 
 public class App {
 
 	private static Scanner scanner = new Scanner(System.in);
 
 	private User user = new User();
 
 	private static App app;
 	
 	private static final String CONFIG_FILE_NAME = "config.properties";
 
 	private static GistManager gistManager = new GistManager();
 	
 	public static void main(String[] args) throws IOException {
 		// resource = ResourceBundle.getBundle("strings", Locale.getDefault());
 		app = new App();
 		//user = new User();
 	
 		while (true) {
 			System.out.println("");
 			System.out.print(Messages.getString("typeCommand"));
 			String command = scanner.nextLine();
 			app.doCommand(command);
 		}
 	}
 
 	public void doCommand(String command) throws IOException {
 		if (command.equals("login")) {
 			readLogin();
 		} else if (command.equals("exit")) {
 			System.out.println(Messages.getString("leaveMessage"));
 			System.exit(0);
 		} else if (command.equals("status")) {
 			System.out.println(Messages.getString("login")
 					+ user.getLogin());
 			System.out.println(Messages.getString("pass")
 					+ new String(user.getPassword()));
 		} else if (command.toLowerCase().equals("creategist")) {
 			gistManager.createNewGist(user);
 		} else if (command.toLowerCase().equals("loadgists")) {
 			gistManager.loadGists(user);
 		} else if (command.toLowerCase().equals("showgists")) {
 			gistManager.showGists();
 		} else if (command.toLowerCase().equals("savelp")) {
 			saveLoginAndPassword();
 		} else if (command.toLowerCase().equals("loadlp")) {
 			loadLoginAdnPassword();
 		} else if (command.toLowerCase().equals("loadfiles")) {
 			gistManager.loadFiles(user.getLogin());
 		} else if (command.toLowerCase().equals("uploadfiles")) {
 			gistManager.uploadFiles(user);
 		} else if (command.equals("") || command.equals("help")) {
 			showHelp();
 		} else {
 			System.out.println(Messages.getString("unknownCommand"));
 			System.out.println();
 		}
 	}
 
 	public void saveLoginAndPassword() {
 		Properties prop = new Properties();
 		try {
 			if (user.getLogin() != null) {
 				prop.setProperty("login", user.getLogin());
 			}
 			if (user.getPassword() != null) {
 				prop.setProperty("password",
 						new String(user.getPassword()));
 			}
 			prop.store(new FileOutputStream(CONFIG_FILE_NAME), null);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void loadLoginAdnPassword() {
 		Properties prop = new Properties();
 		try {
 			prop.load(new FileInputStream(CONFIG_FILE_NAME));
 			user.setLogin(prop.getProperty("login"));
 			user.setPassword(prop.getProperty("password").toCharArray());
 
 			System.out.println(user.getLogin());
 			System.out.println(user.getPassword());
 
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void readLogin() {
 		System.out.print(Messages.getString("typeLogin"));
 		user.setLogin(scanner.nextLine());
 		readPassword();
 	}
 
 	public void readPassword() {
 		System.out.print(Messages.getString("typePassword"));
 		user.setPassword(scanner.nextLine().toCharArray());
 	}
 
 	public void showHelp() {
 		System.out.println(Messages.getString("commandList"));
 		System.out.println(Messages.getString("statusDescription"));
 		System.out.println(Messages.getString("loginDescription"));
 		System.out.println(Messages.getString("uploadLoginPasswordDescription"));
 		System.out.println(Messages.getString("loadLoginPasswordDescription"));
 		System.out.println(Messages.getString("loadGistsDescription"));
 		System.out.println(Messages.getString("showGistsDescription"));
		System.out.println(Messages.getString("createGistDescription"));
		System.out.println(Messages.getString("loadFilesDescription"));
		System.out.println(Messages.getString("uploadFilesDescription"));
 		System.out.println(Messages.getString("exit"));
 	}
 
}
