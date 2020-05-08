 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Checks
 {
 	// The worker number of the instantiating GradeWorker to be used for retrieving the correct
 	// timer.
 	int										number;
 
 	// The runtime for executing commands
 	static Runtime							runtime		= Runtime.getRuntime();
 
 	// The thread executor used for printing the stdout and stderr of the run commands
 	public static ExecutorService			exec;
 
 	// The stdout and stderr for this class
 	final PrintWriter						out;
 	final PrintWriter						err;
 
 	final static int						BUFF_SIZE	= 2 * (int) Math.pow(1024, 2);
 
 	// The header and the footer to denote System errors that occur (and may or may not be due to
 	// the student)
 	static String							header		= "==========ERROR==========\n";
 	static String							footer		= "=========================";
 	static String							fileSep		= System.getProperty("file.separator");
 	static String							progDir		= System.getProperty("user.dir") + fileSep;
 
 	// A field to contain the current process to be tested that is running.
 	Process									currProc;
 	// A timer to be used to cut down on thread creation
 	public static Timer						tmArr[];
 
 	// An array used to hold the "return" for StreamPrinter
 	private boolean[]						streamFindings;
 
 	// A linked list to hold the parsed git commits
 	LinkedList<String>						commitTitles;
 	LinkedList<String>						commitBodies;
 	public static HashSet<String>			invalidCommitRegex;
 	private LinkedBlockingQueue<Integer>	messanger;
 	private LinkedBlockingQueue<String>		pipe;
 
 	private Process							simulProc;
 	private ReentrantLock					simulProcLock;
 
 	public boolean checkMakeClean(File partDir, String makeName)
 	{
 		out.println("Checking make clean:");
 		// Holding the return value. We still need to clean up the directory (so we can't return
 		// directly)
 		boolean cleanWorked = true;
 		// Checking to make sure there are files and a directory to clean up.
 		if (partDir == null || !partDir.isDirectory() || partDir.listFiles().length == 0)
 			return false;
 		// Run make clean in the current directory.
 		try {
 			Process makeClean = runtime.exec("make clean", null, partDir);
 			currProc = makeClean;
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(makeClean.getInputStream()), BUFF_SIZE);
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(makeClean.getErrorStream()), BUFF_SIZE);
 			// Print the stdout of this process
 			exec.execute(new StreamPrinter(1, stdout, 1, 0));
 			// Print the stderr of this process
 			exec.execute(new StreamPrinter(2, stderr, 1, 0));
 			// Wait for the command to finish
 			int success = makeClean.waitFor();
 			messanger.take();
 			messanger.take();
 			if (success != 0)
 				cleanWorked = false;
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": Unable to run make clean in directory: "
 				+ partDir);
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			System.err.println("Grader " + number
 				+ ": Interrupted while waiting for make clean to finish.");
 			e.printStackTrace();
 		}
 		// Checking to make sure that make clean removed everything it should
 		String[] names = makeName.split(",\\ ");
 		HashSet<String> nameSet = new HashSet<String>();
 		for (String n : names)
 			nameSet.add(n);
 		for (File f : partDir.listFiles())
 			if (f.getName().endsWith(".o") || nameSet.contains(f.getName())
 				|| f.getName().compareTo("core") == 0 || f.getName().compareTo("a.out") == 0
 				|| f.getName().endsWith(".gch") || f.getName().endsWith(".a")
 				|| f.getName().compareTo("main") == 0) {
 				f.delete();
 				cleanWorked = false;
 			}
 		out.println();
 		currProc = null;
 		return cleanWorked;
 	}
 
 	/** This command tests some arbitrary command by directly calling exec on the given command
 	 * string. It takes two optional arguments. <code>inputFile</code> refers to a file to be fed
 	 * directly into the program's stdin, and <code>limit</code> refers to how long in seconds to
 	 * run the program. If <code>inputFile</code> is null, then nothing will be piped to the
 	 * program. If <code>limit</code> is 0, then the program will run indefinetly.
 	 * @param workingDir
 	 *            - The initial working directory for this program (e.g. part1, part2,...)
 	 * @param command
 	 * @param inputFile
 	 * @return <code>{memory error, leak error}</code> */
 	public boolean[] testCommand(File workingDir, String command, File inputFile, int limit,
 		ArgumentGenerator gen, String simulCommand, boolean noValgrind)
 	{
 		// Marking the command name
 		final String commandName = command.split("\\ ")[0];
 		out.println("Command results:");
 		// Check to make sure the directory exists (i.e. they did this part)
 		if (workingDir == null)
 			return new boolean[] {true, true};
 
 		int retVal = 1;
 		try {
 			if (inputFile == null)
 				out.println("\tTesting: " + command);
 			else
 				out.println("\tTesting: " + command + " with " + inputFile.getName());
 
 			// Setup the simultaneous process
 			Process simul = null;
 			PrintWriter simulWriter = null;
 			if (simulCommand != null) {
 				String simulName = simulCommand.split("\\ ")[0];
 				File simulOut = new File(workingDir, simulName + ".txt");
 				simulOut.createNewFile();
 				simulWriter = new PrintWriter(new FileOutputStream(simulOut), true);
 			}
 
 			// Create the process
 			Process proc;
 			if (gen != null)
 				proc =
 					runtime.exec((noValgrind ? "" : "valgrind --leak-check=yes ./") + command + " "
 						+ gen.getNextArgument(), null, workingDir);
 			else
 				proc =
 					runtime.exec((noValgrind ? "" : "valgrind --leak-check=yes ./") + command,
 						null, workingDir);
 			// Print the pid of this process
 			printPid(proc, workingDir);
 			// If we have a simultaneous program, run it now
 			if (simulCommand != null) {
 				simul = runtime.exec(getExecPath(simulCommand), null, workingDir);
 				simulProc = simul;
 				final BufferedReader stdout =
 					new BufferedReader(new InputStreamReader(simul.getInputStream()), BUFF_SIZE);
 				final BufferedReader stderr =
 					new BufferedReader(new InputStreamReader(simul.getErrorStream()), BUFF_SIZE);
 				exec.execute(new StreamPrinter(simulWriter, stdout));
 				exec.execute(new StreamPrinter(simulWriter, stderr));
 			}
 			// If we have an input file, feed that to the process
 			if (inputFile != null)
 				exec.execute(new LineFeeder(proc.getOutputStream(), inputFile));
 			System.out.println("Grader " + number + ": started " + commandName);
 			currProc = proc;
 
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(proc.getInputStream()), BUFF_SIZE);
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(proc.getErrorStream()), BUFF_SIZE);
 			// Print the stdout of this process
 			exec.execute(new StreamPrinter(1, stdout, 2, 0, simulCommand != null));
 			// This stderr stream contains the output of valgrind. We can easily check for
 			// errors because:
 			// (1) "ERROR SUMMARY: 0" not being found means there were memory errors
 			// (2) "LEAK SUMMARY:" being found means there were... leaks
 			exec.execute(new StreamPrinter(2, stderr, 2, (noValgrind ? 1 : 2), simulCommand != null));
 
 			// Wake us limit number of seconds later if we were given a non-zero value for limit
 			if (limit > 0) {
 				ProcessKiller pk = new ProcessKiller(commandName);
 				tmArr[number].schedule(pk, limit * 1000);
 				System.out.println("Grader " + number + ": timed waiting on " + commandName);
 				// Now we wait
 				retVal = proc.waitFor();
 				// If we reached here, then the process may have terminated already and we should
 				// not kill it again.
 				pk.cancel();
 			} else {
 				System.out.println("Grader " + number + ": waiting on " + commandName);
 				retVal = proc.waitFor();
 			}
 			messanger.take();
 			messanger.take();
 			if (simulCommand != null) {
 				simul.waitFor();
 				simulProcLock.lock();
 				simulProc = null;
 				simulProcLock.unlock();
 				messanger.take();
 				messanger.take();
 			}
 			out.println("\tReturn code: " + retVal + "\n");
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": An error occured while trying to run: "
 				+ commandName);
 			currProc = null;
 			return new boolean[] {true, true};
 		} catch (InterruptedException e) {
 			System.err.println("Grader " + number + ": Interrupted while waiting for "
 				+ commandName + " to complete.");
 			e.printStackTrace();
 		}
 		currProc = null;
 		// return code 126 for valgrind means it cannot find the file specified
 		if (retVal == 126)
 			return new boolean[] {true, true};
 		return streamFindings;
 	}
 
 	private void printPid(Process proc, File workingDir)
 	{
 		try {
 			// Get the PID of the process
 			Field f = proc.getClass().getDeclaredField("pid");
 			f.setAccessible(true);
 			String pid = f.get(proc).toString();
 			File pidFile = new File(workingDir, "val-pid.txt");
 			pidFile.createNewFile();
 			PrintWriter pidOut = new PrintWriter(pidFile);
 			pidOut.print(pid);
 			pidOut.close();
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/** This command tests some arbitrary command by directly calling exec on the given command
 	 * string. It takes two optional arguments. <code>inputFile</code> refers to a file to be fed
 	 * directly into the program's stdin, and <code>limit</code> refers to how long in seconds to
 	 * run the program. If <code>inputFile</code> is null, then nothing will be piped to the
 	 * program. If <code>limit</code> is 0, then the program will run indefinetly.
 	 * @param workingDir
 	 *            - The initial working directory for this program (e.g. part1, part2,...)
 	 * @param command
 	 * @param input
 	 * @return <code>{memory error, leak error}</code> */
 	public boolean[] testCommand(File workingDir, String command, InputGenerator input, int limit,
 		ArgumentGenerator gen, String simulCommand, boolean noValgrind)
 	{
 		// Marking the command name
 		final String commandName = command.split("\\ ")[0];
 		out.println("Command results:");
 		// Check to make sure the directory exists (i.e. they did this part)
 		if (workingDir == null)
 			return new boolean[] {true, true};
 
 		int retVal = 1;
 		try {
 			if (input == null)
 				out.println("\tTesting: " + command);
 			else
 				out.println("\tTesting: " + command + " with InputGenerator: " + input.getClass());
 
 			// If we have a simultaneous command, set it up
 			Process simul = null;
 			PrintWriter simulWriter = null;
 			if (simulCommand != null) {
 				String simulName = simulCommand.split("\\ ")[0];
 				File simulOut = new File(workingDir, simulName);
 				simulOut.createNewFile();
 				simulWriter = new PrintWriter(new FileOutputStream(simulOut), true);
 			}
 
 			Process proc;
 			if (gen != null)
 				proc =
 					runtime.exec((noValgrind ? "" : "valgrind --leak-check=yes ./") + command + " "
 						+ gen.getNextArgument(), null, workingDir);
 			else
 				proc =
 					runtime.exec((noValgrind ? "" : "valgrind --leak-check=yes ./") + command,
 						null, workingDir);
 			// Print the pid of this process
 			printPid(proc, workingDir);
 
 			// If we have a simultaneous program, run it now
 			if (simulCommand != null) {
 				simul = runtime.exec(getExecPath(simulCommand), null, workingDir);
 				simulProc = simul;
 				final BufferedReader stdout =
 					new BufferedReader(new InputStreamReader(simul.getInputStream()), BUFF_SIZE);
 				final BufferedReader stderr =
 					new BufferedReader(new InputStreamReader(simul.getErrorStream()), BUFF_SIZE);
 				exec.execute(new StreamPrinter(simulWriter, stdout));
 				exec.execute(new StreamPrinter(simulWriter, stderr));
 			}
 
 			// If we have an input generator, feed that to the process
 			if (input != null)
 				exec.execute(new LineFeeder(proc.getOutputStream(), input));
 			System.out.println("Grader " + number + ": started " + commandName);
 			currProc = proc;
 
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(proc.getInputStream()), BUFF_SIZE);
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(proc.getErrorStream()), BUFF_SIZE);
 			// Print the stdout of this process
 			if (input != null)
 				exec.execute(new StreamPrinter(1, stdout, 2, 0, input, simulCommand != null));
 			else
 				exec.execute(new StreamPrinter(1, stdout, 2, 0, simulCommand != null));
 			// This stderr stream contains the output of valgrind. We can easily check for
 			// errors because:
 			// (1) "ERROR SUMMARY: 0" not being found means there were memory errors
 			// (2) "LEAK SUMMARY:" being found means there were... leaks
 			if (input != null)
 				exec.execute(new StreamPrinter(2, stderr, 2, (noValgrind ? 1 : 2), input,
 					simulCommand != null));
 			else
 				exec.execute(new StreamPrinter(2, stderr, 2, (noValgrind ? 1 : 2),
 					simulCommand != null));
 
 			// Wake us limit number of seconds later if we were given a non-zero value for limit
 			if (limit > 0) {
 				ProcessKiller pk = new ProcessKiller(commandName);
 				tmArr[number].schedule(pk, limit * 1000);
 				System.out.println("Grader " + number + ": timed waiting on " + commandName);
 				// Now we wait
 				retVal = proc.waitFor();
 				// If we reached here, then the process may have terminated already and we should
 				// not kill it again.
 				pk.cancel();
 			} else {
 				System.out.println("Grader " + number + ": waiting on " + commandName);
 				retVal = proc.waitFor();
 			}
 			messanger.take();
 			messanger.take();
 			if (simulCommand != null) {
 				simul.waitFor();
 				simulProcLock.lock();
 				simulProc = null;
 				simulProcLock.unlock();
 				messanger.take();
 				messanger.take();
 			}
 			out.println("\tReturn code: " + retVal + "\n");
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": An error occured while trying to run: "
 				+ commandName);
 			currProc = null;
 			return new boolean[] {true, true};
 		} catch (InterruptedException e) {
 			System.err.println("Grader " + number + ": Interrupted while waiting for "
 				+ commandName + " to complete.");
 			e.printStackTrace();
 		}
 		currProc = null;
 		// return code 126 for valgrind means it cannot find the file specified
 		if (retVal == 126)
 			return new boolean[] {true, true};
 		return streamFindings;
 	}
 
 	public int jockeyCommand(File workingDir, String command, File inputFile)
 	{
 		int retVal = -1;
 		try {
 			Process proc = runtime.exec(command, null, workingDir);
 			currProc = proc;
 			// Feed the file
 			if (inputFile != null)
 				exec.execute(new LineFeeder(proc.getOutputStream(), inputFile));
 			// Get the process streams
 			exec.execute(new StreamGobbler(proc.getInputStream()));
 			exec.execute(new StreamGobbler(proc.getErrorStream()));
 			retVal = proc.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		currProc = null;
 		return retVal;
 	}
 
 	public int runCommand(File workingDir, String command, File inputFile, int limit)
 	{
 		String commandName = command.split("\\ ")[0];
 		out.println("====Custom command:" + commandName + "====");
 		int retVal = -1;
 		try {
 			Process proc = runtime.exec(getExecPath(command), null, workingDir);
 			if (inputFile != null)
 				exec.execute(new LineFeeder(proc.getOutputStream(), inputFile));
 			currProc = proc;
 			// Get the process streams
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(proc.getInputStream()), BUFF_SIZE);
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(proc.getErrorStream()), BUFF_SIZE);
 			// Print them
 			exec.execute(new StreamPrinter(1, stdout, 0, 0));
 			exec.execute(new StreamPrinter(2, stderr, 0, 0));
 			// Wake us limit number of seconds later if we were given a non-zero value for limit
 			if (limit > 0) {
 				ProcessKiller pk = new ProcessKiller(commandName);
 				tmArr[number].schedule(pk, limit * 1000);
 				System.out.println("Grader " + number + ": timed waiting on " + commandName);
 				// Now we wait
 				retVal = proc.waitFor();
 				// If we reached here, then the process may have terminated already and we should
 				// not kill it again.
 				pk.cancel();
 			} else {
 				System.out.println("Grader " + number + ": waiting on " + commandName);
 				retVal = proc.waitFor();
 			}
 			messanger.take();
 			messanger.take();
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": An error occured while trying to run: "
 				+ commandName);
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			System.err.println("Grader " + number + ": Interrupted while waiting for "
 				+ commandName + " to complete.");
 			e.printStackTrace();
 		}
 		out.println("===================" + commandName.replaceAll(".", "=") + "====");
 		currProc = null;
 		return retVal;
 	}
 
 	public boolean[] checkMake(File partDir, String makeName)
 	{
 		out.println("Make results:");
 		String[] names = makeName.split(",\\ ");
 		Process makeProc = null;
 		if (partDir == null || !partDir.isDirectory())
 			return new boolean[names.length + 1];
 
 		// Run the make
 		try {
 			makeProc = runtime.exec("make", null, partDir);
 			currProc = makeProc;
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(makeProc.getInputStream()), BUFF_SIZE);
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(makeProc.getErrorStream()), BUFF_SIZE);
 			// Print the stdout of this process
 			exec.execute(new StreamPrinter(1, stdout, 1, 0));
 			// If anything prints to here on make, then either gcc or make had an error, in which
 			// case, this test is a fail
 			exec.execute(new StreamPrinter(2, stderr, 1, 1));
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": Could not run \"make\" in: "
 				+ partDir.getPath());
 			e.printStackTrace();
 		}
 
 		// Wait for the process to terminate
 		int goodMake = 1;
 		if (makeProc != null) {
 			try {
 				goodMake = makeProc.waitFor();
 				messanger.take();
 				messanger.take();
 			} catch (InterruptedException e) {
 				System.err.println("Grader " + number
 					+ ": Interrupted while waiting for make to compile.");
 				e.printStackTrace();
 			}
 		}
 
 		// Finally, check that the executable(s) exists:
 		HashSet<String> nameSet = new HashSet<String>();
 		HashMap<String, Integer> namePos = new HashMap<String, Integer>();
 		int pos = 1;
 		for (String n : names) {
 			nameSet.add(n);
 			namePos.put(n, pos++);
 		}
 		// Make the array for each executable's find status
 		boolean[] makeResults = new boolean[names.length + 1];
 		for (File f : partDir.listFiles())
 			if (nameSet.contains(f.getName()))
 				makeResults[namePos.get(f.getName())] = true;
 		out.println();
 		// Compile the results
 		if (goodMake == 0 && !streamFindings[0])
 			makeResults[0] = true;
 		return makeResults;
 	}
 
 	public void shutdown()
 	{
 		out.flush();
 		err.flush();
 		out.close();
 		err.close();
 	}
 
 	public boolean checkGitCommits(File partDir)
 	{
 		out.println("Checking git commits:");
 
 		// Get the git log of the given directory //
 		// Run git log
 		Process gitLog;
 		try {
 			gitLog =
 				runtime.exec("git log --abbrev-commit --format=format:\"%s:|:%b%n...\"", null,
 					partDir);
 			// Ignore stderr
 			exec.execute(new StreamGobbler(gitLog.getErrorStream()));
 			// Read stdout
 			exec.execute(new LogReader(gitLog.getInputStream()));
 			// Wait for the reader to finish
 			gitLog.waitFor();
 			// And the LogReader to parse
 			messanger.take();
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": Could not run git log in directory:"
 				+ partDir.getAbsolutePath());
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			System.err.println(header + "Grader " + number
 				+ ": Interrupted while waiting for git log to finish.");
 			e.printStackTrace();
 			System.err.println(footer);
 		}
 
 		// Make sure they have at least 5 commits
 		if (commitBodies.size() < 5)
 			return false;
 
 		int count = 0;
 		// Make sure the commits are valid
 		for (int i = 0; i < commitBodies.size(); i++) {
 			String t = commitTitles.get(i);
 			String b = commitBodies.get(i);
 			// Print out the title and body
 			out.println("\t" + (i + 1) + ") Title:");
 			String[] tArr = t.split("(\r)?\n");
 			for (String s : tArr)
 				out.println("\t\t   " + s);
 			out.println("\t   Body:");
 			String[] bArr = b.split("(\r)?\n");
 			for (String s : bArr)
 				out.println("\t\t   " + s);
 			// Check to make sure the commits are valid
 			boolean badTitle = false;
 			boolean badBody = false;
 			for (String regex : invalidCommitRegex) {
 				Pattern p = Pattern.compile(regex);
 				Matcher title = p.matcher(t);
 				Matcher body = p.matcher(b);
 				badTitle = title.find();
 				badBody = body.find();
 			}
 			if (badTitle && badBody)
 				continue;
 			else
 				count++;
 		}
 		// We didn't have enough good commits
 		if (count < 5)
 			return false;
 		else
 			return true;
 	}
 
 	public void printMessage(String message, int stream)
 	{
 		if (stream == 1)
 			out.println(message);
 		else if (stream == 2)
 			err.println(message);
 	}
 
 	public Checks(File target, int number) throws FileNotFoundException
 	{
 		FileOutputStream fileStream = null;
 		fileStream = new FileOutputStream(target);
 		this.number = number;
 		out = new PrintWriter(fileStream, true);
 		err = new PrintWriter(fileStream, true);
 		invalidCommitRegex = new HashSet<String>();
 		messanger = new LinkedBlockingQueue<Integer>();
 		pipe = new LinkedBlockingQueue<String>();
 		simulProcLock = new ReentrantLock(true);
 	}
 
 	private void killProcess()
 	{
 		try {
			// PID of the process
			Field f;
 			Process kill;
 			// If there's a simultaneous process, kill it first
 			simulProcLock.lock();
 			if (simulProc != null) {
				f = simulProc.getClass().getDeclaredField("pid");
				f.setAccessible(true);
 				kill = runtime.exec("kill -s SIGINT " + f.get(simulProc));
 				kill.waitFor();
 			}
 			simulProcLock.unlock();
 			// Kill the process
			f = currProc.getClass().getDeclaredField("pid");
			f.setAccessible(true);
 			kill = runtime.exec("kill -s SIGKILL " + f.get(currProc));
 			kill.waitFor();
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static String getExecPath(String command)
 	{
 		// If the command begins with a path separator, return the command, as its an absolute
 		// directory
 		if (command.startsWith(fileSep))
 			return command;
 		// If the commant starts with ~/, then expand that to the home directory
 		if (command.startsWith("~" + fileSep))
 			return System.getProperty("user.home")
 				+ command.replaceFirst("\\~\\" + fileSep, "\\" + fileSep);
 		String commandName = command.split("\\ ")[0];
 		// If the command contains a path separator, then it is a relative file and should be
 		// prepended with our path
 		if (commandName.contains(fileSep))
 			return progDir + command;
 		// Otherwise, it's just a normal command in PATH
 		return command;
 	}
 
 	private class LogReader implements Runnable
 	{
 		BufferedReader	logStream;
 
 		public LogReader(InputStream stream)
 		{
 			logStream = new BufferedReader(new InputStreamReader(stream), BUFF_SIZE);
 		}
 
 		@Override
 		public void run()
 		{
 			commitTitles = new LinkedList<String>();
 			commitBodies = new LinkedList<String>();
 			try {
 				String line;
 				StringBuilder title = new StringBuilder();
 				boolean breakFound = false;
 				StringBuilder body = new StringBuilder();
 				while ((line = logStream.readLine()) != null) {
 					if (line.startsWith("...") && line.matches("[[^a-z][^A-Z][^0-9]]+")) {
 						commitTitles.add(title.toString().trim());
 						commitBodies.add(body.toString().trim());
 						title.delete(0, title.length());
 						body.delete(0, body.length());
 						breakFound = false;
 					} else {
 						if (line.contains(":|:")) {
 							String[] commentStart = line.split("\\:\\|\\:");
 							title.append(commentStart[0]);
 							if (commentStart.length == 2)
 								body.append(commentStart[1]);
 							breakFound = true;
 						} else {
 							if (breakFound)
 								body.append(line);
 							else
 								title.append(line);
 						}
 					}
 				}
 			} catch (IOException e) {
 				err.println("Failed to read git log.");
 			}
 			messanger.add(0);
 		}
 	}
 
 	public class StreamPrinter implements Runnable
 	{
 		PrintWriter		writer;
 		BufferedReader	stream;
 		int				errorLevel;
 		String			tabs;
 		InputGenerator	gen;
 		int				type;
 		private boolean	shouldPipe;
 
 		/** @param streamType
 		 *            - 1 for stdout, 2 for stderr
 		 * @param stream
 		 *            - The {@link BufferedReader} to read from
 		 * @param numTabs
 		 *            - The number of tabs to indent
 		 * @param errorType
 		 *            - The error logging checks. 0 for no checking, 1 for checking if the stream
 		 *            was written to, 2 for valgrind checking */
 		public StreamPrinter(int streamType, BufferedReader stream, int numTabs, int errorType)
 		{
 			type = streamType;
 			if (streamType == 1)
 				writer = out;
 			else if (streamType == 2)
 				writer = err;
 			this.stream = stream;
 			errorLevel = errorType;
 			tabs = "";
 			for (int i = 0; i < numTabs; i++)
 				tabs += "\t";
 		}
 
 		public StreamPrinter(PrintWriter out, BufferedReader stream)
 		{
 			type = 0;
 			writer = out;
 			shouldPipe = false;
 			this.stream = stream;
 			errorLevel = 0;
 			tabs = "";
 		}
 
 		public StreamPrinter(int streamType, BufferedReader stream, int numTabs, int errorType,
 			InputGenerator input)
 		{
 			this(streamType, stream, numTabs, errorType);
 			gen = input;
 		}
 
 		public StreamPrinter(int streamType, BufferedReader stream, int numTabs, int errorType,
 			InputGenerator input, boolean shouldPipe)
 		{
 			this(streamType, stream, numTabs, errorType, input);
 			this.shouldPipe = shouldPipe;
 		}
 
 		public StreamPrinter(int streamType, BufferedReader stream, int numTabs, int errorType,
 			boolean shouldPipe)
 		{
 			this(streamType, stream, numTabs, errorType);
 			this.shouldPipe = shouldPipe;
 		}
 
 		@Override
 		public void run()
 		{
 			String line;
 			try {
 				if (errorLevel == 0) {
 					while ((line = stream.readLine()) != null) {
 						writer.println(tabs + line);
 						if (gen != null) {
 							if (type == 1)
 								gen.putNextStdOut(line);
 							else if (type == 2)
 								gen.putNextStdErr(line);
 						}
 						if (shouldPipe)
 							pipe.add(type + "1" + line);
 					}
 				} else if (errorLevel == 1) {
 					boolean streamWrote = false;
 					while ((line = stream.readLine()) != null) {
 						// If this line is empty or whitespace, continue
 						if (line.trim().isEmpty())
 							continue;
 						writer.println(tabs + line);
 						if (!streamWrote)
 							streamWrote = true;
 						if (gen != null) {
 							if (type == 1)
 								gen.putNextStdOut(line);
 							else if (type == 2)
 								gen.putNextStdErr(line);
 						}
 						if (shouldPipe)
 							pipe.add(type + "1" + line);
 					}
 					streamFindings = new boolean[] {streamWrote};
 				} else {
 					boolean memErr = true;
 					boolean leakErr = false;
 					while ((line = stream.readLine()) != null) {
 						// There was no memory error
 						if (line.contains("ERROR SUMMARY: 0"))
 							memErr = false;
 						// There were leaks
 						if (line.contains("LEAK SUMMARY:"))
 							leakErr = true;
 						err.println(tabs + line);
 						if (gen != null) {
 							if (type == 1)
 								gen.putNextStdOut(line);
 							else if (type == 2)
 								gen.putNextStdErr(line);
 						}
 						if (shouldPipe)
 							pipe.add(type + "1" + line);
 					}
 					streamFindings = new boolean[] {memErr, leakErr};
 				}
 				stream.close();
 				if (shouldPipe)
 					pipe.add(type + "0");
 				if (gen != null) {
 					if (type == 1)
 						gen.putNextStdOut(null);
 					else if (type == 2)
 						gen.putNextStdErr(null);
 				}
 				messanger.add(0);
 			} catch (IOException e) {
 				System.err.println(header + "Grader " + number + ":");
 				e.printStackTrace();
 				System.err.println(footer);
 			}
 		}
 	}
 
 	private class ProcessKiller extends TimerTask
 	{
 		String	commandName;
 
 		public ProcessKiller(String name)
 		{
 			commandName = name;
 		}
 
 		@Override
 		public void run()
 		{
 			killProcess();
 			System.err.println("Grader " + number + ": killed " + commandName);
 			out.println("\nKilled " + commandName + ". Rerun to see if it behaves itself.");
 		}
 	}
 
 	private class LineFeeder implements Runnable
 	{
 		PrintWriter		stdin;
 		Scanner			reader;
 		InputGenerator	gen;
 		private boolean	concurrent;
 
 		public LineFeeder(OutputStream processStdin, File inputFile) throws FileNotFoundException
 		{
 			stdin = new PrintWriter(processStdin, true);
 			reader = new Scanner(inputFile);
 		}
 
 		public LineFeeder(OutputStream processStdin, InputGenerator input)
 		{
 			stdin = new PrintWriter(processStdin, true);
 			gen = input;
 		}
 
 		@Override
 		public void run()
 		{
 			if (reader != null)
 				while (reader.hasNextLine())
 					stdin.println(reader.nextLine());
 			else if (concurrent) {
 				String line;
 				try {
 					do {
 						stdin.println((line = pipe.take()));
 					} while (line.charAt(1) != '0');
 				} catch (InterruptedException e) {}
 			} else {
 				String line;
 				while ((line = gen.getNextInput()) != null)
 					stdin.println(line);
 			}
 			if (reader != null)
 				reader.close();
 			stdin.close();
 		}
 	}
 }
