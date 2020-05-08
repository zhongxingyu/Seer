 package com;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.JFileChooser;
 import javax.swing.JTextField;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import java.awt.Color;
 import java.awt.Font;
 import javax.swing.JToggleButton;
 import javax.swing.UIManager;
 
 @SuppressWarnings("serial")
 public class StartFrame extends JFrame implements ActionListener {
 
 	private JPanel contentPane;
 	private JTextField txtTuBdzieTekst;
 	private JToggleButton btnPrzegladaj;
 	private JToggleButton btnStart;
 	private File file;
 	private JFileChooser chooser;
 	private String filePath;
 	
 	public StartFrame() {
 		setBackground(new Color(255, 51, 153));
 		setTitle("Statystyki");
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 258, 132);
 		contentPane = new JPanel();
 		contentPane.setBackground(UIManager.getColor("info"));
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		chooser = new JFileChooser("C:\\Users\\Paulina\\Desktop");
 		FileNameExtensionFilter filter = new FileNameExtensionFilter("pdf", "pdf");
 		chooser.setFileFilter(filter);
 		
 		btnPrzegladaj = new JToggleButton("Przegl\u0105daj...");
 		btnPrzegladaj.setFont(new Font("Verdana", Font.PLAIN, 11));
 		btnPrzegladaj.addActionListener(this);
 		
 		btnStart = new JToggleButton("Start!");
 		btnStart.setFont(new Font("Verdana", Font.PLAIN, 11));
 		btnStart.addActionListener(this);
 		btnStart.setVisible(false);
 	
 		
 		txtTuBdzieTekst = new JTextField();
 		txtTuBdzieTekst.setColumns(10);
 		
 		
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
 						.addComponent(btnStart)
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addComponent(txtTuBdzieTekst, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 							.addGap(18)
 							.addComponent(btnPrzegladaj)
 							.addPreferredGap(ComponentPlacement.RELATED)))
 					.addContainerGap(19, Short.MAX_VALUE))
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(txtTuBdzieTekst, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(btnPrzegladaj))
 					.addGap(18)
 					.addComponent(btnStart)
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		contentPane.setLayout(gl_contentPane);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == btnPrzegladaj) {
 			 int returnVal = chooser.showOpenDialog(StartFrame.this);
 	            if (returnVal == JFileChooser.APPROVE_OPTION) {
 	                file = chooser.getSelectedFile();
 	                this.filePath = file.getAbsolutePath();
 	                txtTuBdzieTekst.setText(filePath);
 	        		System.out.println(filePath);
 	        		btnStart.setVisible(true);
 	            }
 		 } 
 		 if (e.getSource() == btnStart) {
 			 if (this.filePath != null) {
 				 try {
 					 ResultFrame result = new ResultFrame(this.filePath);
 					 result.setVisible(true);
 				 } catch (Exception e1) {
 					 e1.printStackTrace();
 				 }
 			 }
 		 }
 		
 	}
 	
 	public String getFilePath() {
 		return this.filePath;
 	}
 }
