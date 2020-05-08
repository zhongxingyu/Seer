 /**
  * Written by Marcin Susel.
  * Provides Subversion integration with FitNesse using the CM_SYSTEM mechanism and relies on svn command provided by OS
  */
 
 package org.fitnesse.plugins;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SvnFitnesse {
 	private static List<String> ignoredPaths = new ArrayList<String>();
 
 	private static Pattern payloadPattern = Pattern
 			.compile(".+?\\s+(.*?)/(.*?)\\s*"); // class svnuser/svnpassword
 	private static String hostname;
 	private static String workingDir;
 
 	static {
 		ignoredPaths.add("/RecentChanges/");
 		ignoredPaths.add("/ErrorLogs/");
 
 		try {
 			hostname = InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 			hostname = "(unknown host)";
 		}
 
 		try {
 			workingDir = new File(".").getCanonicalPath();
 		} catch (IOException e) {
 			workingDir = "(unknown directory)";
 		}
 	}
 	
 	enum SvnCommand{
 		ADD,
 		DELETE,
 		UPDATE,
 		COMMIT
 	}
 
 	/**
 	 * Called before saving the changed file
 	 * 
 	 * @param file
 	 *            path to file
 	 * @param payload
 	 *            contents of CM_SYSTEM
 	 * @throws IOException
 	 */
 	public static void cmEdit(String file, String payload) throws IOException{
 		if (isIgnored(file)) {
 			return;
 		}
 		CommandExecutor.exec(buildCommand(payload, SvnCommand.ADD, file));
 	}
 
 	/**
 	 * Called after the file is saved
 	 * 
 	 * @param file
 	 *            path to file
 	 * @param payload
 	 *            contents of CM_SYSTEM
 	 * @throws IOException
 	 */
 	public static void cmUpdate(String file, String payload)
 			throws IOException {
 		if (isIgnored(file)) {
 			return;
 		}
		File f = new File(file);
		String parent = f.getParent();
		CommandExecutor.exec(buildCommand(payload, SvnCommand.COMMIT, parent));
 	}
 
 	/**
 	 * Called before the file is deleted
 	 * 
 	 * @param file
 	 *            path to file
 	 * @param payload
 	 *            contents of CM_SYSTEM
 	 * @throws IOException
 	 */
 	public static void cmPreDelete(String file, String payload)
 			throws IOException {
 		if (isIgnored(file)) {
 			return;
 		}
 		CommandExecutor.exec(buildCommand(payload, SvnCommand.DELETE, file));
 	}
 
 	/**
 	 * Called after the file is deleted
 	 * 
 	 * @param file
 	 *            path to file
 	 * @param payload
 	 *            contents of CM_SYSTEM
 	 * @throws IOException
 	 */
 	public static void cmDelete(String file, String payload)
 			throws IOException {
 		if (isIgnored(file)) {
 			return;
 		}
		File f = new File(file);
		String parent = f.getParent();
		CommandExecutor.exec(buildCommand(payload, SvnCommand.COMMIT, parent));
 	}
 
 	private static boolean isIgnored(String filePath) {
 		File currentFile = new File(filePath);
 		String absolutePath = currentFile.getAbsolutePath();
 
 		for (String ignoredItem : ignoredPaths) {
 			if (absolutePath.contains(ignoredItem))
 				return true;
 		}
 		return false;
 	}
 
 	private static IllegalArgumentException usage() {
 		return new IllegalArgumentException("CM_SYSTEM should be: "
 				+ SvnFitnesse.class.getName() + " user/password");
 	}
 
 	private static String buildCommand (String payload, SvnCommand command, String file)
 			throws IOException{
 		String path = file;
 		String addOptions = " ";
 		switch (command){
 		case ADD:
 			addOptions = addOptions.concat("--force --parents");
 			break;
 		case COMMIT:
 			addOptions = addOptions.concat("-m \"Change from Fitnesse server:\"" + hostname);
 			break;
 		case DELETE:
 			addOptions = addOptions.concat("--force");
 			break;
 		case UPDATE:
 			addOptions = addOptions.concat("--force --parents");
 			break;
 		}
 		Matcher payloadMatcher = payloadPattern.matcher(payload);
 		if (!payloadMatcher.matches()) {
 			throw usage();
 		}
 		String user = payloadMatcher.group(1);
 		String password = payloadMatcher.group(2);
 		String cmd = "svn" + addOptions + " --username " + user + " --password " + password + " " + 
				command.toString().toLowerCase() + " " + path;
 		return cmd;
 	}
 }
