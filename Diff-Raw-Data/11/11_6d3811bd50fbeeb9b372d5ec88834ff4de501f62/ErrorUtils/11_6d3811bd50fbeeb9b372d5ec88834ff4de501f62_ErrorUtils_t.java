 /*
  * FrontlineSMS <http://www.frontlinesms.com>
  * Copyright 2007, 2008 kiwanja
  * 
  * This file is part of FrontlineSMS.
  * 
  * FrontlineSMS is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at
  * your option) any later version.
  * 
  * FrontlineSMS is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.frontlinesms;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Collection;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.log4j.LogManager;
 
 import serial.SerialClassFactory;
 
 import net.frontlinesms.email.EmailException;
 import net.frontlinesms.messaging.CommProperties;
 import net.frontlinesms.plugins.PluginProperties;
 import net.frontlinesms.resources.ResourceUtils;
 import net.frontlinesms.ui.SimpleConstraints;
 import net.frontlinesms.ui.SimpleLayout;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 /**
  * Utilities for handling errors.
  * @author Carlos Eduardo Genz <kadu@masabi.com>
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 public class ErrorUtils {
 //> CONSTANTS
 	/** Left ndentation in pixels used for swing components in {@link #showErrorDialog(String, String, Throwable, boolean)} */
 	private static final int EM__LEFT_INDENT = 10;
 	/** Top indentation in pixels used for swing components in {@link #showErrorDialog(String, String, Throwable, boolean)} */
 	private static final int EM__TOP_INDENT = 10;
 	/** Linespacing in pixels used for swing components in {@link #showErrorDialog(String, String, Throwable, boolean)} */
 	private static final int EM__LINESPACING = 10;
 	
 //> I18N KEYS
 	private static final I18nString I18N_LOGS_COMPRESSED_SAVE_LOCATION = new I18nString("error.logs.compressed.save.location",
 			"E-mail report failure. Where do you want to save your compressed logs?");
 	private static final I18nString I18N_LOGS_SENT_SUCCESSFULLY = new I18nString("error.logs.sent",
 			"Log files were successfully sent to frontline support team.");
 	private static final I18nString I18N_LOGS_SAVED_SUCCESSFULLY = new I18nString("error.logs.saved",
 			"Logs were saved successfully. Please e-mail them to %0.");
 	private static final I18nString I18N_UNABLE_TO_SEND_LOGS = new I18nString("error.logs.send.failed",
 			"Unable to send logs at this time.");
 	private static final I18nString I18N_COPY_FAILED = new I18nString("error.logs.copy.failed",
 			"Failed to copy [%0]. Your logs are located in [%1].");
 	private static final I18nString I18N_GO_TO_FORUM = new I18nString("error.logs.navigate.forum", "Go to forum");
 	private static final I18nString I18N_CLOSE = new I18nString("action.close", "Close");
 	private static final I18nString I18N_COMMUNITY_DIALOG_TITLE = new I18nString("error.logs.community.dialog.title", 
 			"Visit FrontlineSMS Community website?");
 	private static final I18nString I18N_DETAILS_PROMPT = new I18nString("error.logs.details.prompt", 
 			"Please enter your details below.");
 	private static final I18nString I18N_EMAIL_PROMPT = new I18nString("error.logs.email.prompt", 
 			"You can e-mail FrontlineSMS support team for troubleshooting.");
 	private static final I18nString I18N_SEND_LOGS = new I18nString("error.logs.action.logs.send", "Send Logs");
 	private static final I18nString I18N_YOUR_EMAIL = new I18nString("error.logs.field.email", "Your email:");
 	private static final I18nString I18N_YOUR_NAME = new I18nString("error.logs.field.name", "Your name:");
 	private static final I18nString I18N_DESCRIPTION = new I18nString("error.logs.field.description", "Description:");;
 
 	private static final I18nString I18N_VISIT_COMMUNITY_BODY = new I18nString("error.logs.community.dialog.body",
 			"Please also report this error on the FrontlineSMS community forum at %0" +
 			"\n\nWould you like to go there now?");
 	
 //> STATIC METHODS
 	/**
 	 * Attempts to email error logs to the FrontlineSMS support team at the specified email
 	 * address.  Returns true if logs were succesfully sent, or succesfully saved at a location
 	 * of the user's choosing, or succesfully saved at a different location which the user is
 	 * informed of.
 	 * @param userName
 	 * @param userEmail
 	 * @return <code>true</code> if the error report was sent successfully; <code>false</code> otherwise
 	 */
 	public static boolean reportError(String userName, String userEmail, String description) {
 		try {
 			sendLogs(userName, userEmail, description, false);
 			showMessageDialog(I18N_LOGS_SENT_SUCCESSFULLY.toString());
 			return true;
 		} catch (EmailException e) {
 			// Show user the option to save the logs.zip to a place they know!
 			String dir = showFileChooserForSavingZippedLogs();
 			if (dir != null) {
 				try {
 					copyLogsZippedToDir(dir);
 				} catch (IOException e1) {
 					showMessageDialog(I18N_COPY_FAILED.toString(FrontlineSMSConstants.ZIPPED_LOGS_FILENAME,
 									ResourceUtils.getConfigDirectoryPath() + "logs")); // FIXME this config path will not be correct when log config is changed from default
 							
 				}
 				showMessageDialog(I18N_LOGS_SAVED_SUCCESSFULLY.toString(FrontlineSMSConstants.FRONTLINE_SUPPORT_EMAIL));
 				return true;
 			} else {
 				showMessageDialog(I18N_UNABLE_TO_SEND_LOGS.toString());
 				return false; // Slightly unclear how this would happen
 			}
 		} catch (IOException e) {
 			// Problem writing logs.zip
 			showMessageDialog(I18N_UNABLE_TO_SEND_LOGS.toString());
 			try {
 				sendLogsToFrontlineSupport(userName, userEmail, description, null);
 				return true;
 			} catch (EmailException e1) {
 				// If it fails, there is nothing we can do.
 				return false;
 			}
 		} finally {
 			// TODO why is this here?  Surely this should only be deleted if
 			// copying/emailing was succesful or the whole operation was a
 			// failure?  If copying failed, we inform the user the location
 			// the logs, which we then appear to delete(?!)
 			File input = new File(ResourceUtils.getConfigDirectoryPath() + FrontlineSMSConstants.ZIPPED_LOGS_FILENAME);
 			if (input.exists()) {
 				input.deleteOnExit();
 			}
 		}
 	}
 	
 	private static final void showMessageDialog(String message) {
 		Object[] options = new Object[]{I18N_GO_TO_FORUM, I18N_CLOSE};
 		String forumMessage = I18N_VISIT_COMMUNITY_BODY.toString(FrontlineSMSConstants.URL_FRONTLINESMS_COMMUNITY);
 		int option = JOptionPane.showOptionDialog(null, message + "\n\n" + forumMessage,
 				I18N_COMMUNITY_DIALOG_TITLE.toString(), JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 		if(option == 0) {
 			// Open community forum link.
 			FrontlineUtils.openExternalBrowser(FrontlineSMSConstants.URL_FRONTLINESMS_COMMUNITY);
 		}
 	}
 	                                                             
 
 	/**
 	 * Put the exception stack trace into a StringBuilder, just to show it
 	 * friendlier to the user.
 	 * @param t Exception
 	 * @param sb
 	 */
 	private static final void buildStacktraceAsString(Throwable t, StringBuilder sb) {
 		sb.append(t.getClass());
 		sb.append(" :: ");
 		sb.append(t.getMessage());
 		sb.append("\n");
 		for(StackTraceElement s : t.getStackTrace()) {
 			sb.append("\tat ");
 			sb.append(s.toString());
 			sb.append("\n");
 		}
 		if(t.getCause() != null) {
 			sb.append("Caused by: ");
 			buildStacktraceAsString(t.getCause(), sb);
 		}
 	}
 	
 	/**
 	 * Get AWT panel showing controls and instructions for emailing an error to the FrontlineSMS team.
 	 * @param errorFrame
 	 * @return A {@link ComponentDescription} wrapping the email panel
 	 */
 	private static ComponentDescription getEmailPanel(final JFrame errorFrame) {
 		final JPanel emailPanel = new JPanel(new SimpleLayout());
 		int cumulativeY = EM__LINESPACING;
 		
 		final JLabel emailInstructions = new JLabel(I18N_EMAIL_PROMPT.toString());
 		ImageIcon emailInstructionsIcon = getImageIcon("/icons/tip.png");
 		int emailInstructionsIconWidth = 0;
 		if(emailInstructionsIcon != null) {
 			emailInstructions.setIcon(emailInstructionsIcon);
 			emailInstructionsIconWidth = emailInstructionsIcon.getIconWidth();
 		}
 		emailPanel.add(emailInstructions, new SimpleConstraints(0, cumulativeY));
 		
 		final int FONT_HEIGHT = emailInstructions.getFontMetrics(emailInstructions.getFont()).getHeight();
 		cumulativeY += FONT_HEIGHT + EM__LINESPACING;
 		
 		final JLabel detailsInstructions = new JLabel(I18N_DETAILS_PROMPT.toString());
 		emailPanel.add(detailsInstructions, new SimpleConstraints(emailInstructionsIconWidth, cumulativeY));
 		cumulativeY += FONT_HEIGHT + EM__LINESPACING;
 		
 
 		final JLabel nameLabel = new JLabel(I18N_YOUR_NAME.toString());
 		final JLabel emailLabel = new JLabel(I18N_YOUR_EMAIL.toString());
 		final JLabel descriptionLabel = new JLabel(I18N_DESCRIPTION.toString());
 		final int TF_NAME_X = nameLabel.getFontMetrics(nameLabel.getFont()).stringWidth(nameLabel.getText()) + EM__LEFT_INDENT;
 		final int TF_EMAIL_X = emailLabel.getFontMetrics(emailLabel.getFont()).stringWidth(emailLabel.getText()) + EM__LEFT_INDENT;
 		final int TF_DESCRIPTION_X = descriptionLabel.getFontMetrics(descriptionLabel.getFont()).stringWidth(descriptionLabel.getText()) + EM__LEFT_INDENT;
 		final int MAX_X = Math.max(TF_NAME_X, Math.max(TF_EMAIL_X, TF_DESCRIPTION_X));
 		
 		emailPanel.add(nameLabel, new SimpleConstraints(5, cumulativeY));
 		final JTextField nameTextfield = new JTextField(35);
 		emailPanel.add(nameTextfield,  new SimpleConstraints(MAX_X, cumulativeY));
 				
 		cumulativeY += FONT_HEIGHT + EM__LINESPACING;
 		
 		emailPanel.add(emailLabel, new SimpleConstraints(5, cumulativeY));
 		final JTextField emailTextfield = new JTextField(35);
 		emailPanel.add(emailTextfield, new SimpleConstraints(MAX_X, cumulativeY));
 		
 		cumulativeY += FONT_HEIGHT + EM__LINESPACING;
 		
 		emailPanel.add(descriptionLabel, new SimpleConstraints(5, cumulativeY));
 		final JTextArea descriptionTextArea = new JTextArea(3, 35);
 		descriptionTextArea.setLineWrap(true);
 		
 		final JScrollPane descriptionScrollPane = new JScrollPane(descriptionTextArea);
 		
 		emailPanel.add(descriptionScrollPane, new SimpleConstraints(MAX_X, cumulativeY));
 		
 		cumulativeY += 65;
 		
 		final JButton btSend = new JButton(I18N_SEND_LOGS.toString());
 		ImageIcon sendIcon = getImageIcon("/icons/email_send.png");
 		if(sendIcon != null) {
 			btSend.setIcon(sendIcon);
 		}
 		btSend.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(reportError(nameTextfield.getText(), emailTextfield.getText(), descriptionTextArea.getText())) {
 					errorFrame.dispose();
 				}
 			}
 		});
 		final int BT_SEND_X = 260;
 		emailPanel.add(btSend, new SimpleConstraints(TF_NAME_X + BT_SEND_X + EM__LEFT_INDENT, cumulativeY - 3));
 		
 		final int PN_EMAIL_HEIGHT = cumulativeY + FONT_HEIGHT*2 + EM__LINESPACING;
 		
 		return new ComponentDescription(emailPanel, PN_EMAIL_HEIGHT);
 	}
 	
 	/**
 	 * Converts the stack trace of a {@link Throwable} into a {@link String}.
 	 * @param t
 	 * @return A string representation of the stacktrace of the throwable
 	 */
 	public static String getStackTraceAsString(Throwable t) {
 		StringBuilder bob = new StringBuilder();
 		buildStacktraceAsString(t, bob);
 		return bob.toString();
 	}
 	
 	/**
 	 * Show a non-fatal error dialog shpwing stack trace of a {@link Throwable} and 
 	 * an OK button.  Addition actions can be attached to the OK button via the
 	 * {@link ActionListener}.
 	 * @param title Title of the dialog
 	 * @param message Message displayed in the dialog
 	 * @param t {@link Throwable} which caused this dialog to be shows
 	 * @param actionListener Action listener to attach to the OK button
 	 */
 	public static void showErrorDialog(String title, String message, Throwable t, ActionListener actionListener) {
 		showErrorDialog(title, message, t, false, false, actionListener);
 	}
 	
 	/**
 	 * Show the error dialog, either with the option of emailing the error logs to the FronlineSMS
 	 * team, or showing an OK button which removes the dialog.  You may attach an additional
 	 * {@link ActionListener} to the OK button if you are going with this option.
 	 * @param title Title of the dialog
 	 * @param message Message displayed in the dialog
 	 * @param t {@link Throwable} which caused this dialog to be shows
 	 * @param fatal <code>true</code> if the application should exit after this dialog is closed, <code>false</code> otherwise
 	 * @param showLogging <code>true</code> if the option to send logs to the server should be shown, <code>false</code> if an OK button should be shown instead
 	 * @param actionListener Action listener to attach to the OK button if showLogging is <code>false</code> 
 	 */
 	private static void showErrorDialog(String title, String message, Throwable t, final boolean fatal, final boolean showLogging, ActionListener actionListener) {
 		final JFrame errorFrame = new JFrame();
 		errorFrame.setTitle(title);
 		final Container contentPane = errorFrame.getContentPane();
 		contentPane.setLayout(new SimpleLayout());
 		errorFrame.setIconImage(FrontlineUtils.getImage("/icons/fatal_error.png", DesktopLauncher.class));
 		JLabel errorMessage = new JLabel(message);
 		Font messageFont = new Font(errorMessage.getFont().getFontName(), Font.BOLD, 15);
 		errorMessage.setFont(messageFont);
 		final int LB_MESSAGE_Y = EM__TOP_INDENT;
 		contentPane.add(errorMessage, new SimpleConstraints(EM__LEFT_INDENT, LB_MESSAGE_Y));
 		final int LB_MESSAGE_HEIGHT = errorMessage.getFontMetrics(messageFont).getHeight();
 		final int FONT_HEIGHT = LB_MESSAGE_HEIGHT;
 		
 		final int BT_DETAILS_Y = LB_MESSAGE_Y + LB_MESSAGE_HEIGHT + EM__LINESPACING;
 		
 		final int BT_DETAILS_HEIGHT = FONT_HEIGHT * 2;
 		final int LAST_COMPONENT_Y = BT_DETAILS_HEIGHT + BT_DETAILS_Y;
 		final ComponentDescription lastComponent;
 		final int LAST_COMPONENT_X;
 		if(showLogging) {
 			lastComponent = getEmailPanel(errorFrame);
 			LAST_COMPONENT_X = 0;
 		} else {
 			// Make a button here which removes this window, and possibly does something in addition to that
 			JButton btOk = new JButton("OK");
 			// TODO okButton.addActionListener(l)
 			btOk.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					errorFrame.dispose();
 				}
 			});
 			btOk.addActionListener(actionListener);
 			lastComponent = new ComponentDescription(btOk,  FONT_HEIGHT*2);
 			LAST_COMPONENT_X = 380;
 		}
 		contentPane.add(lastComponent.getComponent(), new SimpleConstraints(EM__LEFT_INDENT+LAST_COMPONENT_X, LAST_COMPONENT_Y));
 
 		
 		final JScrollPane details = new JScrollPane(new JTextArea(getStackTraceAsString(t)));
 		final int detailsHeight = 260;
 		details.setPreferredSize(new Dimension(440, detailsHeight));
 		details.setBorder(new TitledBorder("Stack trace"));
 
 		JToggleButton btDetails = new JToggleButton("Details >>");
 		contentPane.add(btDetails, new SimpleConstraints(EM__LEFT_INDENT, BT_DETAILS_Y));	
 		
 		final int finalHeight_noDetails = LAST_COMPONENT_Y + lastComponent.getHeight() + EM__LINESPACING + EM__LINESPACING;
 		
 		btDetails.setIcon(getImageIcon("/icons/about.png"));
 		btDetails.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JToggleButton bt = (JToggleButton) e.getSource();
 				contentPane.remove(details);
 				contentPane.remove(lastComponent.getComponent());
 				if (bt.isSelected()) {
 					bt.setText("Details <<");
 					int detailsY = LAST_COMPONENT_Y;
 					contentPane.add(details, new SimpleConstraints(EM__LEFT_INDENT, detailsY));
 					contentPane.add(lastComponent.getComponent(), new SimpleConstraints(EM__LEFT_INDENT + LAST_COMPONENT_X, detailsY + detailsHeight + EM__LINESPACING));
 					errorFrame.setSize(468, finalHeight_noDetails + detailsHeight + EM__LINESPACING);
 				} else {
 					bt.setText("Details >>");
 					contentPane.add(lastComponent.getComponent(), new SimpleConstraints(EM__LEFT_INDENT + LAST_COMPONENT_X, LAST_COMPONENT_Y));
 					errorFrame.setSize(468, finalHeight_noDetails);
 				}
 				errorFrame.validate();
 				errorFrame.repaint();
 			}
 		});	
 		errorFrame.setSize(468, finalHeight_noDetails);
 		
 		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
 		errorFrame.setLocation((screen_size.width - errorFrame.getWidth()) >> 1, 
 				(screen_size.height - errorFrame.getHeight()) >> 1);
 		errorFrame.setResizable(false);
 		errorFrame.setVisible(true);
 		errorFrame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				errorFrame.dispose();
 				if(fatal) System.exit(0);
 			}
 		});
 	}
 	
 	/**
 	 * Generate and display the emergency error Dialog using Swing components.  We do this
 	 * when something has gone so badly wrong that we can't trust Thinlet to display the
 	 * error, and we can't trust our i18n code to give us messages in foreign languages!
 	 * @param t			the Throwable that caused the error
 	 * @param title		the title of the dialog that is displayed
 	 * @param message	the error message to display to the user
 	 * @param fatal		set TRUE if the application should exit after this dialog is removed; FALSE otherwise
 	 */
 	public static void showErrorDialog(String title, String message, Throwable t, final boolean fatal) {
 		showErrorDialog(title, message, t, fatal, true, null);
 	}
 
 	/**
 	 * Zip the log files and send them via email.
 	 * @param name The name to show the email as coming from
 	 * @param emailAddress The email address to show the logs as being from
 	 * @param resetConfiguration if <code>true</code>, the log configuration will be reloaded after the logs were sent
 	 * @throws IOException
 	 * @throws MessagingException
 	 */
 	public static void sendLogs(String name, String emailAddress, String description, boolean resetConfiguration) throws IOException, EmailException {
 		LogManager.shutdown();
 		try {
 			// FIXME this will not actually work if the log directory has been configured to be different to the default
 			ResourceUtils.zip(ResourceUtils.getConfigDirectoryPath() + "logs",
 					new File(ResourceUtils.getConfigDirectoryPath() + FrontlineSMSConstants.ZIPPED_LOGS_FILENAME));
 			sendLogsToFrontlineSupport(name, emailAddress, description, ResourceUtils.getConfigDirectoryPath() + FrontlineSMSConstants.ZIPPED_LOGS_FILENAME);
 		} finally {
 			if (resetConfiguration) {
 				FrontlineUtils.loadLogConfiguration();
 			}
 		}
 	}
 
 	/**
 	 * Submit log files to the FrontlineSMS Support email account.
 	 * TODO smtp sending should be refactored into email.smtp.SmtpMessageSender
 	 * @param fromName
 	 * @param fromEmailAddress
 	 * @param attachment
 	 * @throws MessagingException
 	 */
 	public static void sendLogsToFrontlineSupport(String fromName, String fromEmailAddress, String description, String attachment) throws EmailException {
 		StringBuilder sb = new StringBuilder();
 		
		appendDescription(sb, description);
		
 	    appendFrontlineProperties(sb);
 	    appendSystemProperties(sb);
 	    appendCommProperties(sb);
 	    appendPluginProperties(sb);
 	    
 	    String textContent = sb.toString();
 	    String subject = "FrontlineSMS log files";
 	    
 		FrontlineUtils.sendToFrontlineSupport(fromName, fromEmailAddress, subject, textContent, attachment);
 	}
 	
 	/**
 	 * Appends the {@link CommProperties} to the error report body.
 	 * @param bob {@link StringBuilder} used for compiling the body of the error report.
 	 */
 	private static void appendCommProperties(StringBuilder bob) {
 		beginSection(bob, "Comm Properties");
 		
 		bob.append("Serial package name: '" + SerialClassFactory.getInstance().getSerialPackageName() + "'\n");
 		
 		CommProperties properties = CommProperties.getInstance();
 		
 		String[] ignoredPortList = properties.getIgnoreList();
 		bob.append("Ignored ports: " + ignoredPortList.length + "\n");
 		int lastPortIndex = 0;
 		for(String ignoredPort : ignoredPortList) {
 			++lastPortIndex;
 			bob.append("Ignored Port " + lastPortIndex + ": '" + ignoredPort + "'\n");
 		}
 		
 		endSection(bob, "Comm Properties");
 	}
 	
 	/**
 	 * Appends the {@link PluginProperties} to the error report body.
 	 * @param bob {@link StringBuilder} used for compiling the body of the error report.
 	 */
 	private static void appendPluginProperties(StringBuilder bob) {
 		beginSection(bob, "Plugin Properties");
 		
 		PluginProperties pluginProperties = PluginProperties.getInstance();
 
 		Collection<String> pluginClassNameList = pluginProperties.getPluginClassNames();
 		bob.append("Plugins listed: " + pluginClassNameList.size() + "\n");
 		int lastPluginIndex = 0;
 		for(String pluginClassName : pluginClassNameList) {
 			++lastPluginIndex;
 			bob.append("Plugin class " + lastPluginIndex + ": '" + pluginClassName + "'\n");
 		}
 		
 		endSection(bob, "Plugin Properties");
 	}
 	
	private static void appendDescription(StringBuilder bob, String description) {
		beginSection(bob, "User Description");
		bob.append(description);
		bob.append("\n");
		endSection(bob, "User Description");
	}
	
 	private static void appendFrontlineProperties(StringBuilder bob) {
 		beginSection(bob, "FrontlineSMS Properties");
 		
 		// Including flsms version, so we don't need to open logs to find that out.
 		appendProperty(bob, "FrontlineSMS Version", BuildProperties.getInstance().getVersion());
 		appendProperty(bob, "User ID", AppProperties.getInstance().getUserId());
 		appendProperty(bob, "User Email", AppProperties.getInstance().getUserEmail());
 
 		endSection(bob, "FrontlineSMS Properties");
 	}
 
 	/**
 	 * Appends pertinent system properties to a {@link StringBuilder}.
 	 * @param bob
 	 */
 	private static void appendSystemProperties(StringBuilder bob) {
 		beginSection(bob, "System Properties");
 		appendSystemProperty(bob, "OS", "os.name");
 		appendSystemProperty(bob, "OS Architecture", "os.arch");
 		appendSystemProperty(bob, "OS Version", "os.version");
 		
 		try {
 			String hostName = InetAddress.getLocalHost().getHostName();
 			appendProperty(bob, "Host name", hostName);
 		} catch (UnknownHostException ex) { /* ignore me */ }
 		
 		
 		appendSystemProperty(bob, "User Country", "user.country");
 		appendSystemProperty(bob, "User home", ResourceUtils.SYSPROPERTY_USER_HOME);
 		appendSystemProperty(bob, "User name", "user.name");
 		appendSystemProperty(bob, "User language", "user.language");
 		
 		appendSystemProperty(bob, "Java Runtime name", "java.runtime.name");
 		appendSystemProperty(bob, "Java Vendor", "java.specification.vendor");
 		appendSystemProperty(bob, "Java Runtime Version", "java.runtime.version");
 		appendSystemProperty(bob, "Java home", "java.home");
 		appendSystemProperty(bob, "Java Version", "java.version");
 		appendSystemProperty(bob, "Java VM Version", "java.vm.version");
 		
 		endSection(bob, "System Properties");
 	}
 	
 	/**
 	 * Starts a section of the error report body.
 	 * Sections started with this method should be ended with {@link #endSection(StringBuilder, String)}
 	 * @param bob The {@link StringBuilder} used for building the error report body.
 	 * @param sectionName The name of the section of the report that is being started.
 	 */
 	private static void beginSection(StringBuilder bob, String sectionName) {
 		bob.append("\n### Begin Section '" + sectionName + "' ###\n");
 	}
 	
 	/**
 	 * Ends a section of the error report body.
 	 * Sections ended with this should have been started with {@link #beginSection(StringBuilder, String)}
 	 * @param bob The {@link StringBuilder} used for building the error report body.
 	 * @param sectionName The name of the section of the report that is being started.
 	 */
 	private static void endSection(StringBuilder bob, String sectionName) {
 		bob.append("### End Section '" + sectionName + "' ###\n");
 	}
 
 	/**
 	 * Appends a property to the supplied {@link StringBuilder}.  This method should only be called
 	 * by {@link #appendSystemProperties(StringBuilder)} and {@link #appendSystemProperty(StringBuilder, String, String)}.
 	 * @param bob
 	 * @param label
 	 * @param value
 	 */
 	private static void appendProperty(StringBuilder bob, String label, String value) {
 		bob.append(label);
 		bob.append(" = ");
 		bob.append(value);
 		bob.append('\n');
 	}
 	
 	/**
 	 * Appends a system property to the supplied {@link StringBuilder}.  This method should only be called
 	 * by {@link #appendSystemProperties(StringBuilder)}.
 	 * @param bob
 	 * @param label
 	 * @param propertyKey
 	 */
 	private static void appendSystemProperty(StringBuilder bob, String label, String propertyKey) {
 		appendProperty(bob, label, System.getProperty(propertyKey));
 	}
 
 	/**
 	 * Copies the zipped log files to a specific directory.
 	 * @param dir
 	 * @throws IOException
 	 */
 	// FIXME should probably replace this with a generic *move* method, and if not
 	// that, then at least a generic *copy* method
 	// FIXME does not close "out" stream safely
 	public static void copyLogsZippedToDir(String dir) throws IOException {
 		File destination = new File(dir, FrontlineSMSConstants.ZIPPED_LOGS_FILENAME);
 		File input = new File(ResourceUtils.getConfigDirectoryPath() + FrontlineSMSConstants.ZIPPED_LOGS_FILENAME);
 		if (input.exists()) {
 			byte[] buffer = new byte[2048];
 			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destination), 2048);
 			ResourceUtils.stream2stream(new FileInputStream(input), out, buffer);
 			out.close();
 		}
 	}
 
 	/**
 	 * Shows a swing file chooser for choosing the location of the zipped log files.
 	 * @return File chooser UI for saving zipped log files 
 	 */
 	public static String showFileChooserForSavingZippedLogs() {
 		JFileChooser chooser = new JFileChooser(new File("."));
 		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		chooser.setDialogTitle(I18N_LOGS_COMPRESSED_SAVE_LOCATION.toString());
 		chooser.setFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(File f) {
 				return f.isDirectory();
 			}
 			@Override
 			public String getDescription() {
 				return "Directories";
 			}
 		});
 		int resp = chooser.showSaveDialog(null);
 		if (resp == JFileChooser.APPROVE_OPTION) {
 			return chooser.getSelectedFile().getAbsolutePath();
 		}
 		return null;
 	}
 	
 	/**
 	 * Attempts to load an {@link ImageIcon} from the specified location on the classpath.
 	 * @param path The path to the image
 	 * @return the {@link ImageIcon} found at the specified location, or <code>null</code> if none could be found.
 	 */
 	private static ImageIcon getImageIcon(String path) {
 		Image image = FrontlineUtils.getImage(path, ErrorUtils.class);
 		if(image == null) {
 			return null;
 		} else {
 			return new ImageIcon(image);
 		}
 	}
 }
 
 class I18nString {
 	final String key;
 	final String defaultValue;
 	
 	I18nString(String key, String defaultValue) {
 		this.key = key;
 		this.defaultValue = defaultValue;
 	}
 	
 	public String getKey() {
 		return key;
 	}
 	
 	public String getDefaultValue() {
 		return defaultValue;
 	}
 	
 	@Override
 	public String toString() {
 		String text = null;
 		try {
 			text = InternationalisationUtils.getI18NString(getKey());
 		} catch(Throwable t) { /* Something went wrong.  We'll return the default value later. */ }
 		
 		if(text != null) {
 			return text;
 		} else {
 			return getDefaultValue();
 		}
 	}
 	
 	public String toString(String... argValues) {
 		return InternationalisationUtils.formatString(this.toString(), argValues);
 	}
 }
 
 /**
  * Wrapper for an AWT component that we have built, including the expected
  * height of the component.
  * @author Alex
  */
 class ComponentDescription {
 	/** AWT Component we are wrapping */
 	private final Component component;
 	/** Expected height of the component. */
 	private final int height;
 	
 	/**
 	 * Wrap a component and additional details about it.
 	 * @param component
 	 * @param height
 	 */
 	ComponentDescription(Component component, int height) {
 		super();
 		this.component = component;
 		this.height = height;
 	}
 	
 	/**
 	 * get {@link #component} 
 	 * @return {@link #component}
 	 */
 	public Component getComponent() {
 		return component;
 	}
 	/**
 	 * get {@link #height} 
 	 * @return {@link #height}
 	 */
 	public int getHeight() {
 		return height;
 	}
 }
