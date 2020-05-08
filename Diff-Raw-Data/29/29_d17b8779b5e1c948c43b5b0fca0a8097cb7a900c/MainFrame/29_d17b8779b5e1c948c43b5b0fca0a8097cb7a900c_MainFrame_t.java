 package org.haibara.autoxm;
 
 import info.clearthought.layout.TableLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import org.jdesktop.application.SingleFrameApplication;
 
 /**
  * This code was edited or generated using CloudGarden's Jigloo
  * SWT/Swing GUI Builder, which is free for non-commercial
  * use. If Jigloo is being used commercially (ie, by a corporation,
  * company or business for any purpose whatever) then you
  * should purchase a license for each developer using Jigloo.
  * Please visit www.cloudgarden.com for details.
  * Use of Jigloo implies acceptance of these licensing terms.
  * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
  * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
  * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
  */
 /**
  * 
  */
 public class MainFrame extends SingleFrameApplication {
 	private JPanel topPanel;
 	private JLabel chatLabel;
 	private JButton chatButton;
 	private JTextArea jTextArea1;
 	private JTextField chatField;
 
 	@Override
 	protected void startup() {
 		GridLayout mainFrameLayout = new GridLayout(1, 1);
 		mainFrameLayout.setHgap(5);
 		mainFrameLayout.setVgap(5);
 		mainFrameLayout.setColumns(1);
 		getMainFrame().getContentPane().setLayout(mainFrameLayout);
 		{
 			topPanel = new JPanel();
 			TableLayout topPanelLayout = new TableLayout(new double[][] {{70.0, TableLayout.FILL, 142.0, TableLayout.FILL}, {7.0, 35.0, 38.0, 4.0, TableLayout.FILL}});
 			topPanelLayout.setHGap(5);
 			topPanelLayout.setVGap(5);
 			topPanel.setLayout(topPanelLayout);
 			getMainFrame().getContentPane().add(topPanel);
 			topPanel.setPreferredSize(new java.awt.Dimension(364, 331));
 			{
 				chatLabel = new JLabel();
 				topPanel.add(chatLabel, "0,1,c,c");
 				chatLabel.setName("chatLabel");
 			}
 			{
 				chatField = new JTextField();
 				topPanel.add(chatField, "1,1,2,1,f,c");
 				chatField.setName("chatField");
 			}
 			{
 				chatButton = new JButton();
 				topPanel.add(chatButton, "3,1,c,c");
 				chatButton.setName("chatButton");
 				chatButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						chatButtonActionPerformed(evt);
 					}
 				});
 			}
 			{
 				jScrollPane1 = new JScrollPane();
				topPanel.add(jScrollPane1, "0,4,3,4,f,f");
				jScrollPane1.setName("jScrollPane1");
				jScrollPane1.getVerticalScrollBar().setAutoscrolls(true);
				jScrollPane1.getHorizontalScrollBar().setAutoscrolls(true);
				jScrollPane1.setPreferredSize(new java.awt.Dimension(273, 224));
 				{
 					jTextArea1 = new JTextArea();
 					jScrollPane1.setViewportView(jTextArea1);
 					jTextArea1.setName("jTextArea1");
 				}
 			}
 			{
 				jLabel1 = new JLabel();
 				topPanel.add(jLabel1, "0,2,c,c");
 				jLabel1.setName("jLabel1");
 			}
 			{
 				jTextField1 = new JTextField();
 				topPanel.add(jTextField1, "1,2,2,2,f,c");
 			}
 			{
 				jButton1 = new JButton();
 				topPanel.add(jButton1, "3,2,c,c");
 				jButton1.setName("jButton1");
 				jButton1.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						jButton1ActionPerformed(evt);
 					}
 				});
 			}
 		}
 		{
 			getMainFrame().setSize(380, 372);
 		}
 		show(topPanel);
 		this.myStartup();
 	}
 
 	private void myStartup() {
 		ps = new PrintStream(new OutputStream() {
 			@Override
 			public void write(int b) throws IOException {
 
 			}
 
 			public void write(byte data[], int off, int len) throws IOException {
 				final String message = new String(data, off, len);
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						sb.append(message);
 						jTextArea1.setText(sb.toString());
 						jTextArea1.setCaretPosition(jTextArea1.getText().length());
 					}
 				});
 			}
 		});
 		System.setOut(ps);
 		System.setErr(ps);
 		jTextArea1.append("Loading...");
 		String rootPath = MainFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath();
 		rootPath = rootPath.substring(rootPath.indexOf("/")+1,rootPath.lastIndexOf("/"));
 		XMDriver.root = rootPath;
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				InitializeXMDriver();
 			}
 		});	
 	}
 	private void InitializeXMDriver() {
 		driver = new XMDriver();
 		driver.setNotifier(this);
 		try {
 			driver.start();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	private StringBuffer sb = new StringBuffer();
 	private PrintStream ps = null;
 	private XMDriver driver = null;
 
 	public static void main(String[] args) {
 		launch(MainFrame.class, args);
 	}
 
 	private boolean chatState = false;
 	private JButton jButton1;
 	private JTextField jTextField1;
 	private JLabel jLabel1;
 	private JScrollPane jScrollPane1;
 	
 	private void jButton1ActionPerformed(ActionEvent evt) {
 		if (this.driver != null) {
 			if (!this.jTextField1.getText().trim().isEmpty()) {
 				this.driver.setAudienceChatPeriod(Integer.parseInt(this.jTextField1.getText().trim()));
 			}
 		}
 		
 	}
 	
 	private void chatButtonActionPerformed(ActionEvent evt) {
 		if (chatState == true) {
 			if (this.driver != null) {
 				this.driver.stopAudienceChat();
 			}
 			chatState = false;
 			this.chatButton.setText("start");
 		} else {
 			if (!this.chatField.getText().trim().isEmpty()) {
 				if (this.driver != null) {
 					this.driver.setAudienceChat(this.chatField.getText());
 				}
 				chatState = true;
 				this.chatButton.setText("stop");
 			} else {
 				
 			}
 		}
 	}
 
 }
