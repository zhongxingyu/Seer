 package de.th.wildau.dsc.sne.webserver;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
 * The script executer is a class which interprets script files over the cmd/cli.
  */
 public class ScriptExecutor {
 
 	/**
 	 * Check which scripting languages are supported on the current machine.
 	 * 
 	 * @return list of {@link ScriptLanguages}
 	 */
 	public static List<ScriptLanguage> getSupportedScriptLanguages() {
 
 		ScriptExecutor se = new ScriptExecutor();
 		List<ScriptLanguage> scriptLanguages = new ArrayList<ScriptLanguage>();
 
 		// php
 		if (!se.execute("php -version").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.PHP;
 			sl.setExecuteComand("php");
 			scriptLanguages.add(sl);
 		} else if (!se.execute("/usr/bin/php -version").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.PHP;
 			sl.setExecuteComand("/usr/bin/php");
 			scriptLanguages.add(sl);
 		}
 
 		// perl
 		if (!se.execute("perl -version").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.PERL;
 			sl.setExecuteComand("perl");
 			scriptLanguages.add(sl);
 		} else if (!se.execute("/usr/bin/perl -version").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.PERL;
 			sl.setExecuteComand("/usr/bin/perl");
 			scriptLanguages.add(sl);
 		}
 
 		// python
 		if (!se.execute("python -h").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.PYTHON;
 			sl.setExecuteComand("python");
 			scriptLanguages.add(sl);
 		} else if (!se.execute("/usr/bin/python -h").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.PYTHON;
 			sl.setExecuteComand("/usr/bin/python");
 			scriptLanguages.add(sl);
 		}
 
 		// ruby
 		if (!se.execute("ruby -version").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.RUBY;
 			sl.setExecuteComand("ruby");
 			scriptLanguages.add(sl);
 		} else if (!se.execute("/usr/bin/ruby -version").trim().isEmpty()) {
 			ScriptLanguage sl = ScriptLanguage.RUBY;
 			sl.setExecuteComand("/usr/bin/ruby");
 			scriptLanguages.add(sl);
 		}
 
 		return Collections.unmodifiableList(scriptLanguages);
 	}
 
 	/**
 	 * This method tries to execute a file with the given {@link ScriptLanguage}
 	 * .
 	 * 
 	 * @param command
 	 *            to execute
 	 * @return command output
 	 */
 	public String execute(ScriptLanguage scriptLanguage, File file) {
 		String output = "";
 		if (file.isFile() && file.canRead()) {
 			output = execute(scriptLanguage.getExecuteComand() + " "
 					+ file.toString());
 		} else {
 			Log.error("ScriptExecutor can't read file " + file);		}
 		return output;
 	}
 
 	/**
 	 * Help method which executes commands.
 	 */
 	private String execute(String command) {
 
 		Process process;
 		try {
 			process = Runtime.getRuntime().exec(command);
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					process.getInputStream()));
 			StringBuilder sb = new StringBuilder();
 			String line = "";
 			while ((line = in.readLine()) != null) {
 				sb.append(line);
 			}
 			return sb.toString();
 		} catch (final IOException ex) {
 			Log.error("Script execution failed.", ex);
 		}
 		return null;
 	}
 }
