 package com.bluespot.swing;
 
 import java.io.File;
 import java.util.List;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
/**
 * Collection of utilty methods dealing with creating dialogs.
 * 
 * @author Aaron Faanes
 * 
 */
public final class Dialogs {

	private Dialogs() {
		// Suppress default constructor to ensure non-instantiability.
		throw new AssertionError("Instantiation not allowed");
	}
 
 	public static class CancelledException extends Exception {
 
 		public CancelledException() {
 			super(CancelledException.MESSAGE);
 		}
 
 		public static final String MESSAGE = "The operation was cancelled.";
 	}
 
 	public static void error(final String message, final String title) {
 		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
 	}
 
 	public static Integer getNumber(final String message) throws CancelledException {
 		while (true) {
 			int number = -1;
 			try {
 				final String numText = JOptionPane.showInputDialog(message);
 				if (numText == null) {
 					throw new CancelledException();
 				}
 				number = Integer.parseInt(numText);
 			} catch (final NumberFormatException e) {
 				Dialogs.error("The number was invalid!", "Invalid Number");
 				continue;
 			}
 			if (number < 0) {
 				Dialogs.error("The number was invalid!", "Invalid Number");
 				continue;
 			}
 			return number;
 		}
 	}
 
 	public static <T> T getSelection(final String message, final List<T> options, final String title)
 			throws CancelledException {
 		final int selection = JOptionPane.showOptionDialog(null, message, title, JOptionPane.DEFAULT_OPTION,
 				JOptionPane.INFORMATION_MESSAGE, null, options.toArray(), null);
 		if (selection < 0) {
 			throw new CancelledException();
 		}
 		return options.get(selection);
 	}
 
 	public static String getString(final String message) throws CancelledException {
 		while (true) {
 			final String input = JOptionPane.showInputDialog(message);
 			if (input == null) {
 				throw new CancelledException();
 			}
 			if (input.trim() == "") {
 				Dialogs.error("Invalid input. Please try again.", "Error!");
 				continue;
 			}
 			return input.trim();
 		}
 	}
 
 	public static void info(final String message, final String title) {
 		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public static File openFile() throws CancelledException {
 		final JFileChooser fileChooser = new JFileChooser(".");
 		fileChooser.showOpenDialog(null);
 		final File file = fileChooser.getSelectedFile();
 		if (file == null) {
 			throw new CancelledException();
 		}
 		return file;
 	}
 
 	public static File saveFile() throws CancelledException {
 		final JFileChooser fileChooser = new JFileChooser(".");
 		fileChooser.showSaveDialog(null);
 		final File file = fileChooser.getSelectedFile();
 		if (file == null) {
 			throw new CancelledException();
 		}
 		return file;
 	}
 
 	public static void warn(final String message, final String title) {
 		JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
 	}
 }
