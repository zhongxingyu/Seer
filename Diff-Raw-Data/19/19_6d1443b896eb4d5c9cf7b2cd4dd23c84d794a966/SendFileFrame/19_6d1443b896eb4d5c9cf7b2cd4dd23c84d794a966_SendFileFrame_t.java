 package devhood.im.sim.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 import devhood.im.sim.event.EventDispatcher;
 import devhood.im.sim.event.EventObserver;
 import devhood.im.sim.event.Events;
 import devhood.im.sim.model.FileSendAcceptMessage;
 import devhood.im.sim.model.FileSendRejectMessage;
 import devhood.im.sim.service.interfaces.MessageSender;
 
 /**
  * Frame in dem der Versand von Dateien behandelt wird.
  * 
  * @author flo
  * 
  */
 public class SendFileFrame extends JFrame implements EventObserver {
 	/**
 	 * Timer zum aktualisierem der Progressbar.
 	 */
 	private Timer progressTimer = new Timer("Progressbar");
 	private TimerTask updateProgressBarTask;
 
 	private JPanel panel = new JPanel();
 
 	/**
 	 * File das versendet werden soll.
 	 */
 	private File fileToSend;
 
 	/**
 	 * User, der die Datei empfangen soll.
 	 */
 	private String toUser;
 
 	/**
 	 * Service der den versand durchfuehrt.
 	 */
 	private MessageSender messageSender;
 
 	/**
 	 * Id des Versands in diesem Frame.
 	 */
 	private String id;
 
 	public void init() {
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setAlwaysOnTop(true);
 		EventDispatcher.add(this);
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				progressTimer.cancel();
 				updateProgressBarTask.cancel();
 			}
 
 		});
 	}
 
 	public void sendComplete() {
 
 	}
 
 	public void showFrame() {
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		setTitle(fileToSend.getName() + " an " + toUser + " schicken...");
 
 		JLabel label = new JLabel("Versuche " + fileToSend.getName() + " an "
 				+ toUser + " zu senden, warte auf Antwort...");
 		final JProgressBar bar = new JProgressBar();
 		bar.setIndeterminate(true);
 		bar.setValue(0);
 		bar.setMaximum(100);
 
 		JPanel p = new JPanel();
 		final JButton close = new JButton("Schließen");
 		close.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				progressTimer.cancel();
 				updateProgressBarTask.cancel();
 				dispose();
 			}
 		});
 
 		close.setVisible(false);
 		final JButton cancel = new JButton("Cancel");
 
 		updateProgressBarTask = new TimerTask() {
 
 			@Override
 			public void run() {
 				try {
 					if (bar.getValue() > 0) {
 						bar.setIndeterminate(false);
 					}
 
 					if (bar.getValue() < 100) {
 						int perc = (int) ((messageSender.getProgress(id) * 100) / fileToSend
 								.length());
 						bar.setValue(perc);
 					} else {
 						close.setVisible(true);
						cancel.setVisible(false);
 					}
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		progressTimer.schedule(updateProgressBarTask, 0, 500);
 
 		p.add(close);
 		p.add(cancel);
 
 		panel.add(label);
 		panel.add(bar);
 		panel.add(p);
 
 		add(panel);
 		pack();
 		setVisible(true);
 
 		sendFileRequest();
 	}
 
 	@Override
 	public void eventReceived(Events event, Object o) {
 		if (Events.MESSAGE_FILE_ACCEPT_RECEIVED.equals(event)) {
 			FileSendAcceptMessage m = (FileSendAcceptMessage) o;
 			id = m.getId();
 			System.out.println("Fileversand sollte loslaufen..");
 		} else if (Events.MESSAGE_FILE_REJECT_RECEIVED.equals(event)) {
 			FileSendRejectMessage m = (FileSendRejectMessage) o;
 			if (id != null) {
 				if (id.equals(m.getId())) {
 					requestCanceled();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Die Anfrage zum Dateiversand wurde abgelehnt.
 	 */
 	public void requestCanceled() {
 		JOptionPane.showMessageDialog(this,
 				"Ihre Anfrage wurde abgelehnt oder unterbrochen!", "Abbruch",
 				JOptionPane.WARNING_MESSAGE);
 		progressTimer.cancel();
 		updateProgressBarTask.cancel();
 		dispose();
 	}
 
 	/**
 	 * Sendet die Anfrage fuer den Dateiversand.
 	 */
 	public void sendFileRequest() {
 		id = messageSender.sendFileRequest(fileToSend, toUser);
 	}
 
 	public File getFileToSend() {
 		return fileToSend;
 	}
 
 	public void setFileToSend(File fileToSend) {
 		this.fileToSend = fileToSend;
 	}
 
 	public String getToUser() {
 		return toUser;
 	}
 
 	public void setToUser(String toUser) {
 		this.toUser = toUser;
 	}
 
 	public MessageSender getMessageSender() {
 		return messageSender;
 	}
 
 	public void setMessageSender(MessageSender messageSender) {
 		this.messageSender = messageSender;
 	}
 
 }
