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
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 import devhood.im.sim.event.EventDispatcher;
 import devhood.im.sim.event.EventObserver;
 import devhood.im.sim.event.Events;
 import devhood.im.sim.model.FileSendRequestMessage;
 import devhood.im.sim.service.interfaces.MessageSender;
 
 /**
  * Frame bei dem Empfang einer Datei.
  * 
  * @author flo
  * 
  */
 public class ReceiveFileFrame extends JFrame implements EventObserver {
 
 	private JPanel panel = new JPanel();
 	private JPanel buttons = new JPanel();
 	private JPanel container = new JPanel();
 
 	private String id;
 	private FileSendRequestMessage fileSendRequestMessage;
 	private long sizeToReceive;
 
 	private Timer progressTimer = new Timer();
 	private TimerTask updateProgressBarTask;
 
 	private MessageSender messageSender;
 
 	public void init() {
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setAlwaysOnTop(true);
 		EventDispatcher.add(this);
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				updateProgressBarTask.cancel();
 				progressTimer.cancel();
 			}
 		});
 	}
 
 	public void showFrame() {
 		// panel.setLayout();
 		setTitle(fileSendRequestMessage.getFilename() + " von "
 				+ fileSendRequestMessage.getSender() + " empfangen...");
 
 		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
 
 		JLabel label = new JLabel(fileSendRequestMessage.getSender()
 				+ " möchte Ihnen folgende Datei schicken:");
 		JLabel label1 = new JLabel("Dateiname : "
 				+ fileSendRequestMessage.getFilename());
 		JLabel label2 = new JLabel("Dateigröße: "
 				+ fileSendRequestMessage.getSize());
 		JLabel label3 = new JLabel("Nachricht : "
 				+ fileSendRequestMessage.getText());
 
 		final JProgressBar bar = new JProgressBar();
 		bar.setIndeterminate(true);
 		bar.setValue(0);
 		bar.setMaximum(100);
 
 		final JButton ok = new JButton("Datei empfangen");
 		final JButton close = new JButton("Schließen");
 		close.setVisible(false);
 		close.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				updateProgressBarTask.cancel();
 				progressTimer.cancel();
 				dispose();
 			}
 		});
 
		JButton reject = new JButton("Abbrechen");
 
 		ok.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 				int returnVal = fc.showOpenDialog(panel);
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					final File file = fc.getSelectedFile();
 					id = fileSendRequestMessage.getId();
 
 					messageSender.acceptFileMessage(
 							fileSendRequestMessage.getSender(),
 							fileSendRequestMessage.getId(),
 							file.getAbsolutePath());
 					ok.setVisible(false);
 
 					sizeToReceive = fileSendRequestMessage.getSize();
 
 					updateProgressBarTask = new TimerTask() {
 
 						@Override
 						public void run() {
 							try {
 								if (bar.isIndeterminate() && bar.getValue() > 0) {
 									bar.setIndeterminate(false);
 								}
 
 								if (bar.getValue() < 100) {
 									int perc = (int) ((messageSender
 											.getProgress(id) * 100) / sizeToReceive);
 									bar.setValue(perc);
 								} else {
 									close.setVisible(true);
 								}
 
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						}
 					};
 					progressTimer.schedule(updateProgressBarTask, 0, 500);
 
 				} else {
 
 				}
 			}
 		});
 
 		reject.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				messageSender.rejectFileMessage(fileSendRequestMessage.getId(),
 						fileSendRequestMessage.getSender());
 				dispose();
 			}
 		});
 
 		panel.add(label);
 		panel.add(label1);
 		panel.add(label2);
 		panel.add(label3);
 
 		buttons.add(ok);
 		buttons.add(close);
 		buttons.add(reject);
 
 		container.add(panel);
 		container.add(bar);
 		container.add(buttons);
 		add(container);
 
 		pack();
 		setVisible(true);
 
 		// initTransfer();
 	}
 
 	@Override
 	public void eventReceived(Events event, Object o) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public JPanel getPanel() {
 		return panel;
 	}
 
 	public void setPanel(JPanel panel) {
 		this.panel = panel;
 	}
 
 	public FileSendRequestMessage getFileSendRequestMessage() {
 		return fileSendRequestMessage;
 	}
 
 	public void setFileSendRequestMessage(
 			FileSendRequestMessage fileSendRequestMessage) {
 		this.fileSendRequestMessage = fileSendRequestMessage;
 	}
 
 	public MessageSender getMessageSender() {
 		return messageSender;
 	}
 
 	public void setMessageSender(MessageSender messageSender) {
 		this.messageSender = messageSender;
 	}
 }
