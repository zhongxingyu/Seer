 package de.lankenau.rubenwrite;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import de.lankenau.rubenwrite.data.BookDAO;
 
 public class MainFrame extends JFrame {
 	String text = "No TEXT";
 
 	JEditorPane editor = null;
 	JButton btnCheck;
 	JButton btnStart;
 	JButton btnEnd;
 	JTextField txtDuration = null;
 	int duration = 0;
 	final int MAX_DURATION = 60*90;
 	
 	public MainFrame() {
 		try {
 			text = BookDAO.getText(200);
 		} catch (Exception e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(this, e.toString());
 		}
 
 		Font bigFont = new Font("Serif", Font.PLAIN, 24);
 		
 		getContentPane().setLayout(new GridBagLayout());
 		
 		setTitle("Simple example");
 		setSize(800, 600);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		
 
 		JTextArea textArea = new JTextArea();		
 		JScrollPane scrollPane = new JScrollPane(textArea);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1;
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 
 		this.getContentPane().add(scrollPane, c);
 		textArea.setText(text);
 		textArea.setFocusable(false);
 		textArea.setFont(bigFont);
 		
 		
 		editor = new JEditorPane();
 		editor.setFont(bigFont);
 		JScrollPane scrollPaneEditor = new JScrollPane(editor);
 	
 		editor.getDocument().addDocumentListener(new DocumentListener() {
 
 			@Override
 			public void changedUpdate(DocumentEvent arg0) {
 				calculateTime();
 				btnStart.setEnabled(false);
 			}
 
 			@Override
 			public void insertUpdate(DocumentEvent arg0) {
 				calculateTime();
 				btnStart.setEnabled(false);
 			}
 
 			@Override
 			public void removeUpdate(DocumentEvent arg0) {
 				calculateTime();	
 				btnStart.setEnabled(false);
 			}
 			
 		});
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1;
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 1;
 		
 		this.getContentPane().add(scrollPaneEditor, c);
 		
 		
 		JPanel controlPanel = new JPanel();
 		controlPanel.setLayout(new FlowLayout());
 		btnCheck = new JButton();
 		btnCheck.setText("Prfen");
 		btnCheck.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				checkText();
 			}			
 		});
 		controlPanel.add(btnCheck);
 	
 		txtDuration = new JTextField();
 		txtDuration.setMinimumSize(new Dimension(60,30));
 		txtDuration.setPreferredSize(new Dimension(60,30));
 		controlPanel.add(txtDuration);
 		
 		btnStart = new JButton("Internet Starten");
 		controlPanel.add(btnStart);
 		btnStart.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				startInternet();
 				
 			}
 			
 		});
 		btnStart.setEnabled(false);
 		
 		
 		btnEnd = new JButton("Internet Stoppen");
 		controlPanel.add(btnEnd);
 		btnEnd.setEnabled(false);
 		btnEnd.addActionListener( new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				stopInternet(); 
 				
 			}
 			
 		});
 		c = new GridBagConstraints();
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0;
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 2;
 		this.getContentPane().add(controlPanel, c);		
 	}
 
 	protected static int realTextLength(String text) {
 		return text.replace(" ", "").replace("\t", "").replace("\n", "").replace("\r", "").length();
 	}
 
 	protected static String formatTime(int sec) {
 		int hour = sec / (60*60);
 		sec -= hour * 60 * 60;
 		int min = sec / 60;
 		sec -= min * 60;
 		return ""+hour+":"+min+":"+sec;
 	}
 	
 	protected void calculateTime() {
 		float orgSize = realTextLength(text);
 		float editSize = realTextLength(editor.getText());
 		duration = (int) (((float)MAX_DURATION) / orgSize * editSize);
 		txtDuration.setText(formatTime(duration));
 	}
 	
 	protected void checkText() {
 		int pos = Checker.check(editor.getText(), text);
 		if (pos == -1) {
 			// all right
 			btnCheck.setBackground(Color.GREEN);
 			btnStart.setEnabled(true);
 		}
 		else {
 			editor.setCaretPosition(pos);
 			btnCheck.setBackground(Color.RED);
 			editor.requestFocus();
 		}		
 	}
 
 	public void startInternet() {
 		editor.setEnabled(false);
 		btnCheck.setEnabled(false);
 		btnStart.setEnabled(false);
 		btnEnd.setEnabled(true);
 		Network.start();
 		
 		Thread thread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				runInternet();
 			}			
 		});
 		thread.start();
 	}
 	
 	
 	public void runInternet() {
 		Date startTime = new Date();
 		while (true) {
 			long timeUsed = ((new Date()).getTime() - startTime.getTime()) / 1000L;
 			int timeLeft = duration - (int) timeUsed;
 			
 			txtDuration.setText(formatTime(timeLeft));
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			if (timeLeft <= 0) {
 				break;
 			}
 		}
 		stopInternet();
 	}
 	
 	
 	public void stopInternet() {
 		btnStart.setEnabled(true);
 		btnEnd.setEnabled(false);
		Network.start();
 	}
 	
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				MainFrame ex = new MainFrame();
 				ex.setVisible(true);
 			}
 		});
 	}
 
 }
