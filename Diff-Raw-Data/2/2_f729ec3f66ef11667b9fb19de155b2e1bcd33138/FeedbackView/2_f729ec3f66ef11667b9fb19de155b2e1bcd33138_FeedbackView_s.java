 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.views;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.mail.Message;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.progress.UIJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.system.info.SystemInformation;
 
 public class FeedbackView extends ViewPart {
 
 	private static Logger logger = LoggerFactory.getLogger(FeedbackView.class);
 	
 	private static final String DAWN_FEEDBACK = "[DAWN-FEEDBACK]";
 	//this is the default to the java property "org.dawnsci.feedbackmail"
 	private static final String MAIL_TO = "dawnjira@diamond.ac.uk";
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "uk.ac.diamond.scisoft.views.FeedbackView";
 	private Action feedbackAction;
 	private Text emailAddress;
 	private Text messageText;
 	private Text subjectText;
 
 	/**
 	 * The constructor.
 	 */
 	public FeedbackView() {
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialise it.
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 
 		parent.setLayout(new GridLayout(1, false));
 		{
 			Label lblEmailAddress = new Label(parent, SWT.NONE);
 			lblEmailAddress.setText("Your email address for Feedback");
 		}
 		{
 			emailAddress = new Text(parent, SWT.BORDER | SWT.MULTI);
 			emailAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		}
 		{
 			Label lblSubject = new Label(parent, SWT.NONE);
 			lblSubject.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 			lblSubject.setText("Summary");
 		}
 		{
 			subjectText = new Text(parent, SWT.BORDER | SWT.MULTI);
 			subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		}
 		{
 			Label lblComment = new Label(parent, SWT.NONE);
 			lblComment.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 			lblComment.setText("Comment");
 		}
 		{
			messageText = new Text(parent, SWT.BORDER | SWT.MULTI);
 			messageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		}
 		{
 			Button btnSendFeedback = new Button(parent, SWT.NONE);
 			btnSendFeedback.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseUp(MouseEvent e) {
 					feedbackAction.run();
 				}
 			});
 			btnSendFeedback.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 			btnSendFeedback.setText("Send Feedback");
 		}
 		makeActions();
 		hookContextMenu();
 		contributeToActionBars();
 	}
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				FeedbackView.this.fillContextMenu(manager);
 			}
 		});
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(feedbackAction);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(feedbackAction);
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(feedbackAction);
 	}
 
 	private void makeActions() {
 
 		feedbackAction = new Action() {
 			@Override
 			public void run() {
 
 				UIJob feedbackJob = new UIJob("Sending feedback to DAWN developers") {
 
 					@Override
 					public IStatus runInUIThread(IProgressMonitor monitor) {
 
 						try {
 							String mailserver = "localhost";
 							String fromvalue = emailAddress.getText();
 							if (fromvalue.length() == 0) {
 								fromvalue = "user";
 							}
 							String from = fromvalue;
 							String subject = DAWN_FEEDBACK + " - " + subjectText.getText();
 							StringBuilder messageBody = new StringBuilder();
 							messageBody.append("Message from : "+ System.getProperty("user.name", "Unknown User")+"\n");
 							String computerName = "Unknown";
 							try {
 								computerName = InetAddress.getLocalHost().getHostName();
 							} finally {
 
 							}
 							messageBody.append("Machine is   : "+computerName+"\n");
 							String versionNumber = System.getProperty("sda.version", "Unknown");
 							messageBody.append("Version is   : "+versionNumber+"\n");
 							messageBody.append(messageText.getText());
 							messageBody.append("\n\n\n");
 							messageBody.append(SystemInformation.getSystemString());
 
 							File logpath = new File(System.getProperty("user.home"), "dawnlog.html");
 
 							// get the mail to address from the properties
 							String mailTo = System.getProperty("org.dawnsci.feedbackmail", MAIL_TO);
 														
 							sendMail(mailserver, from, mailTo, subject, messageBody.toString(), "log.html", logpath, monitor);
 						} catch (Exception e) {
 							return Status.CANCEL_STATUS;
 						}
 
 						return Status.OK_STATUS;
 					}
 
 				};
 				feedbackJob.setUser(true);
 				feedbackJob.schedule();				
 			}
 		};
 		feedbackAction.setText("Send Feedback");
 		feedbackAction.setToolTipText("Send Feedback");
 		feedbackAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 				getImageDescriptor(ISharedImages.IMG_ETOOL_HOME_NAV));
 
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	@Override
 	public void setFocus() {
 		messageText.setFocus();
 	}
 
 	public void sendMail(String mailServer, final String from, final String to,
 			final String subject, final String messageBody, final String attachmentName,
 			final File attachmentContent, IProgressMonitor monitor) {
 
 
 		// monitoring
 		monitor.beginTask("Sending feedback", 2);
 		monitor.worked(1);
 
 
 		// Get system properties
 		Properties props = System.getProperties();
 
 		// Setup mail server
 		props.put("mail.smtp.host", mailServer);
 
 
 		try {
 			// Get session
 			Session session = Session.getInstance(props, null);
 
 			// Define message
 			MimeMessage message = new MimeMessage(session);
 			message.setFrom(new InternetAddress(from));
 			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
 			message.setSubject(subject);
 
 			// create the message part 
 			MimeBodyPart messageBodyPart = new MimeBodyPart();
 
 			//fill message
 			messageBodyPart.setText(messageBody);
 
 			Multipart multipart = new MimeMultipart();
 			multipart.addBodyPart(messageBodyPart);
 
 			// Part two is attachment
 			messageBodyPart = new MimeBodyPart();
 			DataSource source = new FileDataSource(attachmentContent);
 			messageBodyPart.setDataHandler(new DataHandler(source));
 			messageBodyPart.setFileName(attachmentName);
 			multipart.addBodyPart(messageBodyPart);
 
 			// Put parts in message
 			message.setContent(multipart);
 
 			// Send the message
 			Transport.send( message );
 
 			Display.getDefault().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 							"Feedback Sucsessfully Sent", "Thank you for your contribution");
 				}
 			});
 			
 			
 		} catch (Exception e) {
 			logger.error("Feedback email not sent", e);
 			Display.getDefault().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 							"Feedback not sent!", "There appears to be no email service set up on this computer, and so we cannot automatically process your feedback.  Please go to www.dawnsci.org and register your problem there. (This is accessible form the welcome page)");
 				}
 			});
 		}
 
 		// monitoring
 		monitor.worked(1);
 
 	} 
 
 }
