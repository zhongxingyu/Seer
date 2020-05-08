 /*
  *  $Id: PaperWorker2.java 2013/09/28 15:16:12 masamitsu $
  *
  *  ===============================================================================
  *
  *   Copyright (C) 2013  Masamitsu Oikawa  <oicawa@gmail.com>
  *   
  *   Permission is hereby granted, free of charge, to any person obtaining a copy
  *   of this software and associated documentation files (the "Software"), to deal
  *   in the Software without restriction, including without limitation the rights
  *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *   copies of the Software, and to permit persons to whom the Software is
  *   furnished to do so, subject to the following conditions:
  *   
  *   The above copyright notice and this permission notice shall be included in
  *   all copies or substantial portions of the Software.
  *   
  *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *   THE SOFTWARE.
  *
  *  ===============================================================================
  */
 
 package pw.core.ui.command;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import pw.core.PWAction;
 import pw.core.PWError;
 import pw.core.PWItem;
 import pw.core.PWSession;
 import pw.core.PWUtilities;
 import pw.core.PWAccesser;
 import pw.core.action.BasicAddAction;
 import pw.core.action.BasicDeleteAction;
 import pw.core.action.BasicDetailAction;
 import pw.core.action.BasicListAction;
 import pw.core.action.BasicUpdateAction;
 import pw.core.setting.JobSetting;
 import pw.core.setting.ActionSetting;
 import pw.core.ui.command.operation.BasicAddOperation;
 import pw.core.ui.command.operation.BasicDeleteOperation;
 import pw.core.ui.command.operation.BasicDetailOperation;
 import pw.core.ui.command.operation.BasicListOperation;
 import pw.core.ui.command.operation.BasicUpdateOperation;
 
 /**
  * @author masamitsu
  *
  */
 public class PaperWorker implements Closeable {
 	
 	private final String BUILTIN_COMMAND_QUIT = "quit";
 	private final String BUILTIN_COMMAND_JOB = "job";
 	
 	private PWSession session = new PWSession();
 	Jobs jobs;
 	
 	public PaperWorker(String userId) {
 		session.setAccesser(PWAccesser.getAccesser(userId));
 		session.setUserId(userId);
 		jobs = new Jobs(session);
 	}
 	
 	public static void main(String[] args) {
 		
 		String userId = "";
 		if (args.length == 1) {
 			userId = args[1];
 		}
 
 		try {
 			PaperWorker paperworker = new PaperWorker(userId);
 			try {
 				paperworker.run();
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				paperworker.close();
 			}
 		} catch (PWError e) {
 			error(e.getMessage());
 			error(e.getInnerException());
 		}
 	}
 	
 	public void close() {
 		session.getAccesser().close();
 	}
 
 	private void run() {
 		message("==================================================");
		message("PaperWorker2");
 		message("==================================================");
 		flush();
 		while (true) {
 			String input = prompt("> ");
 			
 			if (input.equals("")) {
 				continue;
 			}
 			
 			if (input.equals("quit")) {
 				message("Bye.");
 				return;
 			}
 			
 			if (input.equals("help")) {
 				printJobs();
 				continue;
 			}
 			
 			String[] commandLine = input.split(" ");
 			String commandName = commandLine[0];
 			
 			if (commandName.equals("job")) {
 				doJob(commandLine);
 				continue;
 			}
 			
 			doAction(commandLine);
 		}
 	}
 	
 	private int printList(List<Object> list, String keyName, String descriptionName) {
 		List<String> names = new ArrayList<String>();
 		List<String> descriptions = new ArrayList<String>();
 		for (Object object : list) {
 			PWItem item = (PWItem)object;
 			names.add(PWItem.getValueAsString(item, keyName));
 			descriptions.add(PWItem.getValueAsString(item, descriptionName));
 		}
 		
 		int nameMaxLength = PWUtilities.getMaxLength(names);
 		String format = nameMaxLength == 0 ? "%s : %s" : String.format("%%-%ds : %%s", nameMaxLength);
 		for (int i = 0; i < list.size(); i++) {
 			message(format, names.get(i), descriptions.get(i));
 		}
 		return nameMaxLength;
 	}
 	
 	private void printJobs() {
 		PWAction action = jobs.getAction(Jobs.LISTJOB);
 		@SuppressWarnings("unchecked")
 		List<Object> jobSettingObjects = (List<Object>)action.run();
 		
 		// Add BuiltIn commands
 		JobSetting job = new JobSetting();
 		job.setName(BUILTIN_COMMAND_JOB);
 		job.setDescription("Job setting maintenance.");
 		jobSettingObjects.add((Object)job);
 		
 		JobSetting quit = new JobSetting();
 		quit.setName(BUILTIN_COMMAND_QUIT);
 		quit.setDescription("Quit from PaperWorker");
 		jobSettingObjects.add((Object)quit);
 		
 		printList(jobSettingObjects, "name", "description");
 	}
 	
 	private void doJob(String... commandLine) {
 		Jobs controller = jobs;
 		
 		int length = commandLine.length;
 		if (length == 1) {
 			printActionList(controller);
 			return;
 		}
 		
 		String commandName = commandLine[0];
 		String actionName = commandLine[1];
 		PWAction action = controller.getAction(actionName);
 		if (action == null) {
 			error("No such action in '%s'command.", commandName);
 			return;
 		}
 		
 		try {
 			PWOperation operation = getOperation(action);
 			operation.run(commandLine);
 		} catch (PWError e) {
 			error("*** ERROR *** %s", e.getMessage());
 		}
 	}
 	
 	private void doAction(String... commandLine) {
 		
 		// Print actions in this command. 
 		String commandName = commandLine[0];
 		if (commandLine.length == 1) {
 			ActionSetting conditionItem = new ActionSetting();
 			conditionItem.setJobName(commandName);
 			PWAction listAction = jobs.getAction(Jobs.LISTACTION);
 			@SuppressWarnings("unchecked")
 			List<Object> list = (List<Object>)listAction.run(conditionItem);
 			printList(list, "actionName", "description");
 			return;
 		}
 		
 		// Get target action setting
 		String actionName = commandLine[1];
 		PWAction detailAction = jobs.getAction(Jobs.DETAILACTION);
 		ActionSetting setting = (ActionSetting)detailAction.run(commandName, actionName);
 		if (setting == null) {
 			throw new PWError("No such action [name: %s]", actionName);
 		}
 		
 		// Create action
 		@SuppressWarnings("unchecked")
 		Class<PWAction> actionType = (Class<PWAction>)PWUtilities.getClass(setting.getActionClassPath());
 		PWAction action = (PWAction)PWUtilities.createInstance(actionType);
 		action.setSession(session);
 		action.setParameters(setting.getArgumentArray());
 		
 		try {
 			PWOperation operation = getOperation(action);
 			operation.run(commandLine);
 		} catch (PWError e) {
 			error("*** ERROR *** %s", e.getMessage());
 		}
 	}
 	
 	/**
 	 * @param jobName
 	 * @param actionName
 	 * @return
 	 */
 	private PWOperation getOperation(PWAction action) {
 		Class<?> actionType = action.getClass();
 		while (actionType != null) {
 			if (actionType == BasicAddAction.class) {
 				return new BasicAddOperation((BasicAddAction)action);
 			} else if (actionType == BasicDeleteAction.class) {
 				return new BasicDeleteOperation((BasicDeleteAction)action);
 			} else if (actionType == BasicDetailAction.class) {
 				return new BasicDetailOperation((BasicDetailAction)action);
 			} else if (actionType == BasicListAction.class) {
 				return new BasicListOperation((BasicListAction)action);
 			} else if (actionType == BasicUpdateAction.class) {
 				return new BasicUpdateOperation((BasicUpdateAction)action);
 			} else {
 				actionType = actionType.getSuperclass();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param controller
 	 */
 	private void printActionList(Jobs controller) {
 		List<String> names = new ArrayList<String>(controller.getActionNames());
 		Collections.sort(names);
 		
 		for (String name : names) {
 			message(name);
 		}
 	}
 
 	public static void message(String format, Object... args) {
 		System.out.println(String.format(format, args));
 	}
 	
 	public static void error(String format, Object... args) {
 		System.err.println(String.format(format, args));
 		System.err.flush();
 	}
 	
 	public static void error(Exception e) {
 		e.printStackTrace();
 	}
 	
 	public static void flush() {
 		System.out.flush();
 	}
 	
 	public static String prompt(String format, Object... args) {
 		System.out.print(String.format(format, args));
 		flush();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in), 1);
 		String input = "";
 		try {
 			input = reader.readLine();
 		} catch (IOException e) {
 			throw new PWError(e, e.getMessage());
 		}
 		return input;
 	}
 	
 	public static boolean confirm(String message, String again, String ok, String cancel) {
 		while (true) {
 			String input = PaperWorker.prompt(message).toUpperCase();
 			if (input.equals(ok.toUpperCase())) {
 				return true;
 			} else if (input.equals(cancel.toUpperCase())) {
 				return false;
 			} else {
 				PaperWorker.message("");
 				PaperWorker.message(again);
 			}
 		}
 	}
 }
