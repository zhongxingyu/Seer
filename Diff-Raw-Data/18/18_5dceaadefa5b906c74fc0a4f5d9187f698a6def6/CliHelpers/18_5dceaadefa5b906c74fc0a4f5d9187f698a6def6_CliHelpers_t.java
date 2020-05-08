 package grisu.frontend.view.cli;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import jline.ConsoleReader;
 import jline.Terminal;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.collect.ImmutableList;
 
 public class CliHelpers {
 
 	public static final boolean ENABLE_PROGRESS = true;
 
 	public static final Terminal terminal = Terminal.setupTerminal();
 	private static ConsoleReader consoleReader = null;
 
 	private static Thread indeterminateProgress = null;
 	public static String[] indeterminateProgressStrings = new String[] { "-",
		// "\\", "|", "/" };
		"*", ".", "d" };
 
 	public static synchronized ConsoleReader getConsoleReader() {
 		if ( consoleReader == null ) {
 			try {
 				consoleReader = new ConsoleReader();
 				terminal.beforeReadLine(consoleReader, "", (char) 0);
				terminal.afterReadLine(consoleReader, "", (char) 0);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return consoleReader;
 	}
 
 	private static int getTermwidth() {
 		return getConsoleReader().getTermwidth();
 	}
 
 	public static String getUserChoice(Collection<String> collection,
 			String nonSelectionText) {
 		return getUserChoice(collection, null, null, nonSelectionText);
 	}
 	public static String getUserChoice(Collection<String> collection,
 			String prompt, String defaultValue, String nonSelectionText) {
 
 		if (StringUtils.isBlank(prompt)) {
 			prompt = "Enter selection";
 		}
 
 		int defaultIndex = -1;
 		final ImmutableList<String> list = ImmutableList.copyOf(collection
 				.iterator());
 		for (int i = 0; i < list.size(); i++) {
 			System.out.println("[" + (i + 1) + "] " + list.get(i));
 			if (list.get(i).equals(defaultValue)) {
 				defaultIndex = i + 1;
 			}
 		}
 
 		if (StringUtils.isNotBlank(nonSelectionText)) {
 			System.out.println("\n\n[0] " + nonSelectionText);
 			prompt = prompt + "[0]: ";
 			defaultIndex = 0;
 		} else {
 			prompt = prompt + ": ";
 		}
 
 		int choice = -1;
 		int startIndex = 0;
 		if (StringUtils.isBlank(nonSelectionText)) {
 			startIndex = 1;
 		}
 		while ((choice < startIndex) || (choice > list.size())) {
 			String input;
 			try {
 				input = getConsoleReader().readLine(prompt);
 			} catch (final IOException e) {
 				throw new RuntimeException(e);
 			}
 
 			if (StringUtils.isBlank(input)) {
 				if ((StringUtils.isNotBlank(nonSelectionText) && (defaultIndex >= 0))
 						|| (StringUtils.isBlank(nonSelectionText) && (defaultIndex >= 1))) {
 					choice = defaultIndex;
 				} else {
 					continue;
 				}
 			} else {
 				try {
 					choice = Integer.parseInt(input);
 				} catch (final Exception e) {
 					continue;
 				}
 			}
 		}
 
 		if (choice == 0) {
 			return null;
 		} else {
 			return list.get(choice - 1);
 		}
 
 	}
 
 
 	public static void main(String[] args) throws InterruptedException {
 
 
 		// for (int i = 0; i <= 20; i++) {
 		// setProgress(i, 20);
 		//
 		// Thread.sleep(400);
 		// }
 
 		setIndeterminateProgress("Testing...", true);
 
 		Thread.sleep(4000);
 
 		setIndeterminateProgress("Success.", false);
 	}
 
 	private static String repetition(String string, int progress) {
 		StringBuffer result = new StringBuffer();
 		for ( int i=0;i<progress;i++) {
 			result.append(string);
 		}
 		return result.toString();
 	}
 
 	public static void setIndeterminateProgress(boolean start) {
 		setIndeterminateProgress(null, start);
 	}
 
 	public static void setIndeterminateProgress(final String message,
 			boolean start) {
 
 		if (terminal == null) {
 			return;
 		}
 
		getConsoleReader().setDefaultPrompt("");

 		if (start) {
 			if ((indeterminateProgress != null)
 					&& indeterminateProgress.isAlive()) {
 				// already running
 				return;
 			}
 
 			indeterminateProgress = new Thread() {
 				@Override
 				public void run() {
 					int i = 0;
 					String msg = message;
 					do {
 						if (StringUtils.isBlank(message)) {
 							msg = indeterminateProgressStrings[i];
 						} else {
 							msg = message + " "
 									+ indeterminateProgressStrings[i];
 						}
 						writeToTerminal(msg);
 
 						try {
 							Thread.sleep(200);
 						} catch (InterruptedException e) {
 							break;
 						}
 						i = i + 1;
 						if (i >= indeterminateProgressStrings.length) {
 							i = 0;
 						}
 					} while (!Thread.interrupted());
 
 					writeToTerminal(" ");
 				}
 			};
 			indeterminateProgress.start();
 
 		} else if (!start) {
 			if ((indeterminateProgress != null)
 					&& indeterminateProgress.isAlive()) {
 				indeterminateProgress.interrupt();
 				try {
 					indeterminateProgress.join();
 				} catch (InterruptedException e) {
 				}
 
 				if (!StringUtils.isBlank(message)) {
 					System.out.println(message);
 				}
 			}
 		}
 
 
 	}
 
 	public static void setProgress(int completed, int total) {
 		if (terminal == null) {
 			return;
 		}
 
		getConsoleReader().setDefaultPrompt("");

 		int progress = (completed * 20) / total;
 		String totalStr = String.valueOf(total);
 		String percent = String.format("%0"+totalStr.length()+"d/%s [", completed, totalStr);
 		String result = percent + repetition("-", progress)
 				+ repetition(" ", 20 - progress) + "]";
 
 		writeToTerminal(result);
 
 	}
 
 	private static void writeToTerminal(String message) {
 		getConsoleReader().getCursorBuffer().clearBuffer();
 		getConsoleReader().getCursorBuffer().write(message);
 		try {
 			getConsoleReader().setCursorPosition(getTermwidth());
 			getConsoleReader().redrawLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
