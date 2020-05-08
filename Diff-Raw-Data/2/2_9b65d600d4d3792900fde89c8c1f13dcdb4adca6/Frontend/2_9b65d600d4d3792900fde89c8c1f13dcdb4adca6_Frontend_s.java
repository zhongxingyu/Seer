 package com.ijg.darklight.sdk.core;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 import com.ijg.darklight.sdk.core.gui.GUI;
 
 public class Frontend {
 	
 	private ArrayList<String> validEntry;
 	
 	private String userName = "unset";
 	
 	GUI gui;
 	CoreEngine engine;
 	
 	/**
 	 * Set this.engine and initiate the gui
 	 * @param engine
 	 */
 	public Frontend(CoreEngine engine) {
 		this.engine = engine;
 		gui = new GUI(engine);
 	}
 	
 	/**
 	 * Prompt for name to be used with Darklight Web
 	 */
 	public void promptForName() {
 		if (readName() != null) {
 			if (!userName.equals("unset")) {
 				engine.authUser();
 				return;
 			}
 		} else {
 			String localName = "";
 			String testName = "";
 			try {
 				if (engine.teamSession()) {
 					localName = JOptionPane.showInputDialog(null, "Enter your team name", "It's all about identity", 1);
 				} else {
 					localName = JOptionPane.showInputDialog(null, "Enter your name", "It's all about identity", 1);
 				}
 				testName = localName.toLowerCase().trim();
 			} catch (Exception e) {
 				if (e instanceof NullPointerException) {
 					System.out
 							.println("No input from user, assuming termination is desired. Terminating...");
 					System.exit(0);
 				}
 				System.err.println("Error prompting for user: " + e.getMessage());
 				promptForName();
 				return;
 			}
 			
			if (engine.settings.isApiEnabled()) {
 				validEntry = (engine.teamSession()) ? engine.settings.getVerificationTeams() : engine.settings.getVerificationNames();
 				
 				boolean promptForName = true;
 				for (String name : validEntry) {
 					if (name.toLowerCase().equals(testName.toLowerCase())) {
 						promptForName = false;
 					}
 				}
 				
 				if (promptForName) {
 					System.out.println("Invalid entry: " + localName.trim());
 					promptForName();
 					return;
 				}
 			}
 
 			setUserName(localName.trim());
 			try {
 				if (!writeName()) {
 					promptForName();
 					return;
 				}
 				engine.authUser();
 			} catch (IOException e) {
 				System.err
 						.println("Error writing name file: " + e.getMessage());
 				promptForName();
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Read name written to the name file
 	 * @return The name written to the name file, or null if the name file was not found
 	 */
 	private String readName() {
 		File f = new File(engine.settings.getNameFile());
 		try (Scanner s = new Scanner(f)) {
 			setUserName(s.nextLine().trim());
 			System.out.println("Found name file, contains: " + userName);
 			return userName;
 		} catch (FileNotFoundException e) {
 			System.err.println("Error: Name file not found, should be "
 					+ engine.settings.getNameFile());
 		}
 		return null;
 	}
 	
 	/**
 	 * Write the name received from promptForName() to the name file
 	 * @return True if name was successfully written
 	 * @throws IOException If there was an error writing to the name file
 	 */
 	private boolean writeName() throws IOException {
 		if (!userName.equals("unset")) {
 			File f = new File(engine.settings.getNameFile());
 			if (!f.createNewFile()) {
 				System.out
 						.println("Can not create name file at: " + engine.settings.getNameFile() + ", continuing anyways");
 				f.delete();
 				f.createNewFile();
 			}
 			FileWriter fw = new FileWriter(f);
 			fw.write(userName);
 			fw.close();
 			System.out.println("Wrote \"" + userName + "\" to name file");
 			return true;
 		}
 		throw new IOException("Could not write name to name file");
 	}
 	
 	/**
 	 * Set userName
 	 * @param name The name to be written to the name file
 	 */
 	private void setUserName(String name) {
 		System.out.println("Setting username: " + name);
 		userName = name;
 	}
 	
 	/**
 	 * Get userName
 	 * @return The name read from the name file
 	 */
 	public String getUserName() {
 		return userName;
 	}
 }
