 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.zcu.kiv.os.processes;
 
 import cz.zcu.kiv.os.Utilities;
 import cz.zcu.kiv.os.core.Core;
 import cz.zcu.kiv.os.core.NoSuchProcessException;
 import cz.zcu.kiv.os.core.Process;
 import cz.zcu.kiv.os.core.ProcessGroup;
 import cz.zcu.kiv.os.core.ProcessProperties;
 import cz.zcu.kiv.os.core.device.*;
 import cz.zcu.kiv.os.core.filesystem.FileManager;
 import cz.zcu.kiv.os.core.filesystem.InvalidPathCharactersException;
 import cz.zcu.kiv.os.core.interrupts.KeyboardEvent;
 import cz.zcu.kiv.os.terminal.InputParser;
 import cz.zcu.kiv.os.terminal.ParseException;
 import cz.zcu.kiv.os.terminal.ParseResult;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author bydga
  */
 public class Shell extends Process {
 
 	private static final String EXIT_COMMAND = "exit";
 	private static final String CWD_COMMAND = "cd";
 	private List<String> history;
 	private int historyIndex = 0;
	private String curentCommand;
 	private IOutputDevice historyLog;
 
 	/**
 	 * Initializes new instance of output device - suitable for forwarding process's output/error streams. If given path
 	 * is null, last argument is used as a return value.
 	 *
 	 * @param path Path to a desired file
 	 * @param append append to existing file or create/overwrite
 	 * @param nullValue Value, that will be returned in case path is null - output is not forwarded
 	 * @return
 	 */
 	private IOutputDevice createOutput(String path, boolean append, IOutputDevice nullValue) throws InvalidPathCharactersException {
 		if (path != null) {
 
 			try {
 				return Core.getInstance().getServices().openFileForWrite(this, path, append);
 			} catch (IOException ex) {
 				Utilities.log("Error when moving output to " + path);
 			}
 		}
 
 		return nullValue;
 	}
 
 	/**
 	 * Initializes new instance of in device - suitable for forwarding process's input stream. If given path is null,
 	 * last argument is used as a return value.
 	 *
 	 * @param path Path to a desired file
 	 * @param nullValue Value, that will be returned in case path is null - input is not forwarded
 	 * @return
 	 */
 	private IInputDevice createInput(String path, IInputDevice nullValue) throws InvalidPathCharactersException {
 		if (path != null) {
 
 			try {
 				return Core.getInstance().getServices().openFileForRead(this, path);
 			} catch (IOException ex) {
 				Utilities.log("Error when moving stdout to " + path);
 			}
 		}
 
 		return nullValue;
 	}
 
 	/**
 	 * Handles parseresult received from InputParser and creates process according to given input. In case there are
 	 * more processes to be run in a pipeline, this method creates all of them.
 	 *
 	 * @param parseResult
 	 * @return The newly created process, or in case there is the pipeline, the latest process.
 	 * @throws NoSuchProcessException
 	 */
 	private List<Process> createProcess(ParseResult parseResult) throws NoSuchProcessException, InterruptedException, InvalidPathCharactersException {
 
 		ProcessGroup group = new ProcessGroup(new ThreadGroup("group_" + parseResult.args[0]));
 		//only for debugging reasons
 		List<Process> list = new ArrayList<Process>();
 		IInputDevice in = this.createInput(parseResult.stdIn, this.getInputStream());
 		IOutputDevice out;
 		IOutputDevice err;
 
 
 		//there is another process waiting in the pipeline for ouput of this process
 		while (parseResult.pipeline != null) {
 			//create the pipe
 			AbstractIODevice pipe = Core.getInstance().getServices().createPipe();
 			out = this.createOutput(parseResult.stdOut, parseResult.stdOutAppend, pipe);
 			err = this.createOutput(parseResult.stdErr, parseResult.stdErrAppend, this.getErrorStream());
 			in = this.createInput(parseResult.stdIn, in);
 
			ProcessProperties properties = new ProcessProperties(this, this.getUser(), parseResult.args, in, out, err, this.getWorkingDir(), group, parseResult.isBackgroundTask);
			Process p = Core.getInstance().getServices().createProcess(parseResult.args[0], properties);
 			list.add(p);
 			//pipe was used as an output device here, remember it for next iteration, where it will stand as an input
 			in = pipe;
 
 			parseResult = parseResult.pipeline;
 		}
 
 		//handle last process of the pipeline - or in case of no pipeline the only executed process
 		in = this.createInput(parseResult.stdIn, in);
 		out = this.createOutput(parseResult.stdOut, parseResult.stdOutAppend, this.getOutputStream());
 		err = this.createOutput(parseResult.stdErr, parseResult.stdErrAppend, this.getErrorStream());
 
		ProcessProperties properties = new ProcessProperties(this, this.getUser(), parseResult.args, in, out, err, this.getWorkingDir(), group, parseResult.isBackgroundTask);
		Process p = Core.getInstance().getServices().createProcess(parseResult.args[0], properties);
 
 		list.add(p);
 		return list;
 	}
 
 	@Override
 	protected void run(String[] args) throws Exception {
 		InputParser parser = new InputParser();
 		this.history = new ArrayList<String>();
 		this.historyLog = Core.getInstance().getServices().openFileForWrite(this, "history.txt", true);
 
 		//this loop reads input from terminal
 		while (Core.getInstance().getServices().isRunning()) {
 			this.checkForStop();
 			try {
 				this.getOutputStream().write(this.getUser() + " " + this.getWorkingDir() + " $");
			} catch(InterruptedException e) {
 				continue;
 			}
			
 			String command = this.getInputStream().readLine();
 
 			this.checkForFinishedChildren();
			
 			if (command != null && !command.isEmpty()) {
 				this.history.add(command);
 				this.historyLog.writeLine(this.getWorkingDir() + " $ " + command);
 				this.historyIndex = this.history.size();
 
 				try {
 					//process command
 					ParseResult result = parser.parse(command);
 					//check file paths for invalid characters
 					try {
 						ParseResult tmp = result;
 						do {
 							FileManager.checkProhibitedChars(tmp.stdIn);
 							FileManager.checkProhibitedChars(tmp.stdOut);
 							FileManager.checkProhibitedChars(tmp.stdErr);
 							tmp = tmp.pipeline;
 						} while (tmp != null);
 					} catch (InvalidPathCharactersException ex) {
 						this.getOutputStream().writeLine("Following characters cannot be used as filename: " + InvalidPathCharactersException.invalidCharsList());
 						continue;
 					}
 
 
 					//special cases of input command
 					if (result.args.length == 0) {
 						continue;
 					}
 					if (result.args[0].equals(Shell.EXIT_COMMAND)) {
 						return; //return from the main loop - ends the process
 					} else if (result.args[0].equals(Shell.CWD_COMMAND)) {
 						if (result.args.length > 1) {
 							this.setWorkingDir(result.args[1]);
 						}
 
 						continue;
 					} else if (result.args[0].equals("")) {
 						continue;
 					}
 
 					List<Process> processes = this.createProcess(result);
 					if (!result.isBackgroundTask) {
 						for (Process p : processes) {
 							int ret = Core.getInstance().getServices().readProcessExitCode(p);
 						}
 					} else {
 						this.getOutputStream().writeLine("[" + processes.get(processes.size() - 1).getPid() + "] " + command);
 					}
 				} catch (ParseException e) {
 					this.getOutputStream().writeLine("Invalid input: " + e.getMessage());
 				} catch (NoSuchProcessException ex) {
 					this.getOutputStream().writeLine("Invalid process name");
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void handleSignalSIGTERM() {
 		this.handleSignalSIGQUIT();
 	}
 
 	@Override
 	protected void handleKeyboardEvent(KeyboardEvent e) {
 
 		if (e == KeyboardEvent.ARROW_DOWN) {
 			this.historyIndex++;
 			if (this.historyIndex >= this.history.size()) {
				Core.getInstance().getServices().setTerminalCommand(this.curentCommand);
 				this.historyIndex = this.history.size();
 			} else {
 				Core.getInstance().getServices().setTerminalCommand(this.history.get(this.historyIndex));
 			}
 		} else if (e == KeyboardEvent.ARROW_UP) {
 			if (this.historyIndex == this.history.size()) {
 				this.curentCommand = Core.getInstance().getServices().getTerminalCommand();
 			}
 
 			if (this.historyIndex > 0) {
 				this.historyIndex--;
 				Core.getInstance().getServices().setTerminalCommand(this.history.get(this.historyIndex));
 			}
 		}
 	}
 }
