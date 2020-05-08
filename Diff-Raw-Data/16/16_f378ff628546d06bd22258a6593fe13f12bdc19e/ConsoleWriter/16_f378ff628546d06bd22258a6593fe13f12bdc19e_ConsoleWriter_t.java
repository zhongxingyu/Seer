 package io;
 
 import javax.swing.*;
 import java.util.logging.Logger;
 
 /**
  * @author Anton Chepurov
  */
 public class ConsoleWriter {
 	private static final String NEW_LINE = System.getProperty("line.separator");
 	public static final String DONE_TEXT = "\t\tDone.";
 	public static final String FAILED_TEXT = "\t\tFailed." + NEW_LINE;
 
 	private static final Logger LOGGER = Logger.getLogger(ConsoleWriter.class.getName());
 
 	private final JTextArea consoleTextArea;
 	private final boolean toConsole;
 	private StringBuilder logMessageBuilder = null;
 	private String lastLine;
 	private boolean hasWarnings;
 	private static final String DOT_TEXT = ".";
 	private static final String IN_TEXT = " in ";
 	private static final String MS_TEXT = " ms.";
 
 	public ConsoleWriter(JTextArea consoleTextArea, boolean toConsole) {
 		this.consoleTextArea = consoleTextArea;
 		this.toConsole = toConsole;
 	}
 
 	public void done(long ms) {
 		if (hasWarnings) {
 			write(lastLine);
 		}
 		writeLn(insertMs(ms));
		invalidateWarnings();
 	}
 
 	public void done() {
 		if (hasWarnings) {
 			write(lastLine);
 		}
 		writeLn(DONE_TEXT);
		invalidateWarnings();
 	}
 
 	public void failed() {
 		writeLn(FAILED_TEXT);
 	}
 
 	public void writeLn(String line) {
 		if (toConsole) {
 			System.out.println(line);
 		}
 		consoleTextArea.append(line + NEW_LINE);
 		logPrint(line);
 		/* Auto-Scroll to bottom */
 		autoScrollToBottom();
 	}
 
 	public void write(String line) {
 		if (toConsole) {
 			System.out.print(line);
 		}
 		consoleTextArea.append(line);
 		logCollect(line); // collect for further print into log
 		/* Auto-Scroll to bottom */
 		autoScrollToBottom();
 		lastLine = line;
 	}
 
 	public void newLine() {
 		writeLn("");
 	}
 
 	public void warning(String line) {
 		writeLn("### WARNING ###  " + line);
 		hasWarnings = true;
 	}
 
 	private String insertMs(long ms) {
 		return DONE_TEXT.replace(DOT_TEXT, IN_TEXT + ms + MS_TEXT);
 	}
 
 	private void autoScrollToBottom() {
 		/* Auto-Scroll to bottom */
 		int length = consoleTextArea.getDocument().getLength();
 		consoleTextArea.select(length, length);
 	}
 
 	/* ######################
 	######  LOGGER #########*/
 
 	private void logCollect(String line) {
 		getLogMessageBuilder().append(line); // collect for further print into log
 	}
 
 	private void logPrint(String line) {
 		StringBuilder mesBuilder = getLogMessageBuilder();
 		LOGGER.info(mesBuilder.append(line).toString());
 		logReset();
 	}
 
 	private void logReset() {
 		logMessageBuilder = new StringBuilder();
 	}
 
 	public StringBuilder getLogMessageBuilder() {
 		if (logMessageBuilder == null) {
 			logReset();
 		}
 		return logMessageBuilder;
 	}
 
 	public static ConsoleWriter getStub() {
 		return new ConsoleWriter(new JTextArea(), false);
 	}

	public void invalidateWarnings() {
		hasWarnings = false;
	}
 }
