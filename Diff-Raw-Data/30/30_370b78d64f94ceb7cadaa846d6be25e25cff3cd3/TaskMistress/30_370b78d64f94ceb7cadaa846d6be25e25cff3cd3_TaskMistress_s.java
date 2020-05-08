/* TaskMistress.java
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 /* TODO add a debugger; something that outputs debug information to a debug stream, to help squash bugs... */
 
 /**
  * A class that runs the TaskMistress program.
  * @author anonpds <anonpds@gmail.com>
  */
 public class TaskMistress {
 	/** Configuration file directory for the program. */
 	private static final String CONFIG_DIR = "TaskMistress";
 	
 	/** Configuration file name for the program. */
 	private static final String CONFIG_FILE = "conf.txt";
 
 	/** The name of the program. */
 	public static final String PROGRAM_NAME = "Task Mistress";
 
 	/** The current version of the program. */
 	public static final String PROGRAM_VERSION = "0.-1";
 
 	/**
 	 * Launches an instance of the program at the given file system path.
 	 * @param path the path to the task tree to edit
 	 * @throws Exception when the TaskStore cannot be initialised
 	 */
 	public TaskMistress(File path) throws Exception {
 		/* DEBUG */ System.out.println("Running " + PROGRAM_NAME + " " + PROGRAM_VERSION +  " at " + path.getPath());
 		TaskStore store = new TaskStore(path);
 		new MainWindow(store);
 	}
 	
 	/**
 	 * Shows a dialog that allows the user to select a directory path.
 	 * @return the selected directory or null if no directory was selected
 	 */
 	public static File showPathDialog() {
 		/* create a file chooser dialog that only allows single selection and only directories */
 		JFileChooser chooser = new JFileChooser();
 		chooser.setMultiSelectionEnabled(false);
 		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 		/* show the dialog and return the selected directory (returns null if no directory chosen) */
 		chooser.showOpenDialog(null);
 		return(chooser.getSelectedFile());
 	}
 
 	/**
 	 * Returns the configuration file. Three environment variables are examined for the directory that contains the
 	 * configuration file in the following order: XDG_CONFIG_HOME, HOME and APPDATA. If HOME is used, a directory
 	 * called ".config" is appended to it.
 	 * 
 	 * In all three cases the directory stored in the constant CONFIG_DIR is further appended to the used environment
 	 * variable to get the directory. Then, finally, the actual configuration file name is appended from the constant
 	 * CONFIG_FILE.
 	 * 
 	 * @return the configuration file or null if no suitable place for the configuration file was found
 	 */
 	@SuppressWarnings("unused")
 	private static File getConfigFile() {
 		File path = null;
 		
 		/* array of tried env. variables and paths that are appended to them */
 		String[][] envs = { { "XDG_CONFIG_HOME", null }, { "HOME", ".local" }, { "APPDATA", null } };
 		for (int i = 0; i < envs.length; i++) {
 			String var = System.getenv(envs[i][0]);
 			if (var != null) {
 				/* the environment variable exists; use it */
 				path = new File(var);
 				if (envs[i][1] != null) path = new File(path, envs[i][1]);
 				break;
 			}
 		}
 		/* TODO an error should be given if neither XDG_CONFIG_HOME, HOME nor APPDATA env. variables exist. */
 		
 		/* if one of the environment variables existed, append the config file directory and name to it*/
 		if (path != null) {
 			path = new File(path, CONFIG_DIR);
 			path = new File(path, CONFIG_FILE);
 		}
 		
 		return path;
 	}
 
 	/**
 	 * Runs the program.
 	 * @param args command line arguments (unused)
 	 */
 	public static void main(String[] args) {
 		/* set native look and feel if possible */
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) { /* TODO add error reporting */ }
 
 		/* a path is needed for the task tree root; either read it from configuration file or ask from user */
 		File path = null;
 		File conf = null; /* TODO TaskMistress.getConfigFile(); -- conf. file is disabled for now */
 
 		while (path == null || !path.exists()) {
 			if (conf == null || !conf.exists()) {
 				/* no configuration file; query user */
 				path = TaskMistress.showPathDialog();
 				if (path == null) {
 					/* no directory chosen, show a message and terminate the program */
 					JOptionPane.showMessageDialog(null,
 					                              "No directory chosen. Terminating the program.",
 					                              PROGRAM_NAME + " " + PROGRAM_VERSION,
 					                              JOptionPane.INFORMATION_MESSAGE);
 					System.exit(0);
 				}
 				
 				/* directory chosen; write it config file */
 				if (conf != null) {
 					if (!conf.getParentFile().exists()) conf.getParentFile().mkdirs(); /* TODO errors */
 					try {
 						BufferedWriter writer = new BufferedWriter(new FileWriter(conf));
 						writer.write(path.getPath());
 						writer.close();
 					} catch (Exception e) { /* TODO error reporting */ System.out.println(e.getMessage());}
 				}
 			} else {
 				/* the configuration file exists; try to read it */
 				try {
 					BufferedReader reader = new BufferedReader(new FileReader(conf));
 					path = new File(reader.readLine());
 					reader.close();
 				} catch (Exception e) {
 					/* could not read the configuration file */
 					String msg = "Error: could not read " + conf.getName() + ": " + e.getMessage() +
 							", choose a working directory or quit by pressing cancel";
 					JOptionPane.showMessageDialog(null,
 					                              msg,
 					                              PROGRAM_NAME + " " + PROGRAM_VERSION,
 					                              JOptionPane.ERROR_MESSAGE);
 					/* set the conf to null, so the next iteration will show the path dialog */
 					conf = null;
 				}
 			}
 		}
 		
 		/* launch TaskMistress from the given path */
 		try {
 			new TaskMistress(path);
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null,
 			                              "Failed to initialize the program: " + e.getMessage(),
 			                              PROGRAM_NAME + " " + PROGRAM_VERSION,
 			                              JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 	}
 }
