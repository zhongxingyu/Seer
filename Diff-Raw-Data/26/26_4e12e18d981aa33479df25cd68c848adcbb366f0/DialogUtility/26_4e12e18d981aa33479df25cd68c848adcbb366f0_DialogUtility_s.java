 package gsingh.learnkirtan.utility;
 
 import static gsingh.learnkirtan.Constants.VERSION;
 import gsingh.learnkirtan.shabad.ShabadMetaData;
 import gsingh.learnkirtan.ui.action.ActionFactory.PropertiesAction.MetaDataDialogCallback;
 
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 /**
  * Static class that wraps various Dialog box calls and provides interfaces to
  * their result values
  * 
  * @author Gulshan
  * 
  */
 public class DialogUtility {
 
 	private static JFrame frame = null;
 
 	public static void setFrame(JFrame frame) {
 		DialogUtility.frame = frame;
 	}
 
 	public static int showSaveDialog() {
 		return JOptionPane.showConfirmDialog(frame,
 				"Would you like to save before proceeding?");
 	}
 
 	public static int showAboutDialog() {
 		return JOptionPane
 				.showConfirmDialog(
 						frame,
 						"This software was written by Gulshan Singh (gulshan@umich.edu) and is free \n"
 								+ "and opensource under the Apache License. Please contact me if you would like to contribute.\n"
 								+ "\n Version " + VERSION, "About",
 						JOptionPane.DEFAULT_OPTION,
 						JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public static int showUpToDateDialog() {
 		return JOptionPane.showConfirmDialog(frame,
 				"The software is up to date.", "No Updates",
 				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public static int showUpdateAvailableDialog(JPanel panel) {
 		return JOptionPane.showOptionDialog(null, panel, "Update Available",
 				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
 				null, null, null);
 	}
 
 	public static int showOverwriteDialog() {
 		return JOptionPane.showConfirmDialog(frame, "Overwrite existing file?",
 				"Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE);
 	}
 
 	public static void showFileDoesntExistDialog() {
 		JOptionPane.showMessageDialog(frame, "Error: File doesn't exist.",
 				"Error", JOptionPane.ERROR_MESSAGE);
 	}
 
 	public static void showInvalidShabadDialog() {
 		JOptionPane
 				.showMessageDialog(
 						frame,
 						"The shabad contains errors. Please fix all red colored cells.",
 						"Error", JOptionPane.ERROR_MESSAGE);
 	}
 
 	public static int showChangeSaDialog(JPanel panel) {
 		return JOptionPane.showConfirmDialog(frame, panel, "Change Sa Key",
 				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 	}
 
 	public static void showDashFirstNoteDialog() {
 		JOptionPane
 				.showMessageDialog(
 						frame,
 						"Error: The first note found was a dash, which is not allowed.",
 						"Error", JOptionPane.ERROR_MESSAGE);
 	}
 
 	public static void showNoStartLabelDialog() {
 		JOptionPane
 				.showMessageDialog(
 						frame,
 						"Error: You specified that playback should start at a label, "
 								+ "but that label could not be found. Make sure there is a "
 								+ "'#' before the label.", "Error",
 						JOptionPane.ERROR_MESSAGE);
 	}
 
 	public static void showNoteOutOfBoundsDialog(String invalidNote) {
 		JOptionPane.showMessageDialog(frame, "Error: Invalid note '"
 				+ invalidNote + "' is too low or too high", "Error",
 				JOptionPane.ERROR_MESSAGE);
 	}
 
 	/*************************************************************************/
 
 	public static boolean isCancelledOrClosed(int result) {
 		if (result == JOptionPane.CANCEL_OPTION
 				|| result == JOptionPane.CLOSED_OPTION) {
 			return true;
 		} else
 			return false;
 	}
 
 	public static boolean isYes(int result) {
 		if (result == JOptionPane.YES_OPTION)
 			return true;
 		else
 			return false;
 	}
 
 	public static boolean isOK(int result) {
 		if (result == JOptionPane.OK_OPTION)
 			return true;
 		else
 			return false;
 	}
 
 	public static void showMessage(String message) {
 		JOptionPane.showMessageDialog(frame, message);
 	}
 
 	public static void showMetaDataDialog(ShabadMetaData data,
 			final MetaDataDialogCallback callback) {
 		final JTextField name = new JTextField(15);
 		final JTextField taal = new JTextField(15);
 		final JTextField raag = new JTextField(15); // TODO Dropdown
 		final JTextField ang = new JTextField(15); // TODO Spinner
 		final JTextArea notes = new JTextArea(4, 15);
 		final JScrollPane sp = new JScrollPane(notes);
 		
 		name.setText(data.getName());
 		taal.setText(data.getTaal());
 		raag.setText(data.getRaag());
 		ang.setText(data.getAng());
 		notes.setText(data.getNotes());
 
 		JPanel panel = new JPanel();
 		panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		panel.add(new JLabel("Name: "));
 		panel.add(name);
 		panel.add(new JLabel("Taal: "));
 		panel.add(taal);
 		panel.add(new JLabel("Raag: "));
 		panel.add(raag);
 		panel.add(new JLabel("Ang: "));
 		panel.add(ang);
 		panel.add(new JLabel("Notes: "));
 		panel.add(sp);
 
 		final JDialog dialog = new JDialog(frame);
 
 		JButton ok = new JButton("Ok");
 		ok.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				dialog.dispose();
 				callback.completed(new ShabadMetaData(name.getText(), taal
 						.getText(), raag.getText(), ang.getText(), notes
 						.getText()));
 			}
 		});
		panel.add(ok);
 
		dialog.setContentPane(panel);
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		dialog.setVisible(true);
 	}
 }
