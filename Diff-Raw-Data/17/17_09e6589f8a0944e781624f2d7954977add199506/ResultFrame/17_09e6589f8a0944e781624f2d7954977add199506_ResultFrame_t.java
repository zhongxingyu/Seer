 package com;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.Color;
 import javax.swing.JTextField;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import java.awt.Font;
 import java.io.IOException;
 
 import javax.swing.JLabel;
 import javax.swing.UIManager;
 
 public class ResultFrame extends JFrame {
 	
 	private ExtractPageContent pdf;
 	private JPanel contentPane;
 	private JTextField textField;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JLabel label;
 	private JTextField textField_3;
 
 	/**
 	 * Launch the application.
 	 */
 
 	/**
 	 * Create the frame.
 	 */
 	public ResultFrame(String filePath) {
 		setResizable(false);
 		setTitle("Statystyki");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 259, 199);
 		contentPane = new JPanel();
 		contentPane.setBackground(UIManager.getColor("info"));
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		System.out.println(filePath);
 		
		TextAnalysis text = new TextAnalysis( new ExtractPageContent(filePath).getFileContents() );
 		
 		textField = new JTextField();
		textField.setText(text.getNumberOfSigns().toString());
 		textField.setColumns(10);
 		
 		textField_1 = new JTextField();
		textField_1.setText(text.getNumberOfWords().toString());
 		textField_1.setColumns(10);
 		
 		textField_2 = new JTextField();
		textField_2.setText(text.getNumberOfSentences().toString());
 		textField_2.setColumns(10);
 		
 		JLabel lblIloZnakw = new JLabel("Ilo\u015B\u0107 znak\u00F3w:");
 		lblIloZnakw.setFont(new Font("Verdana", Font.BOLD, 12));
 		
 		JLabel lblIloSw = new JLabel("Ilo\u015B\u0107 s\u0142\u00F3w:");
 		lblIloSw.setFont(new Font("Verdana", Font.BOLD, 12));
 		
 		JLabel lblIloZda = new JLabel("J\u0119zyk:");
 		lblIloZda.setFont(new Font("Verdana", Font.BOLD, 12));
 		
 		label = new JLabel("Ilo\u015B\u0107 zda\u0144:");
 		label.setFont(new Font("Verdana", Font.BOLD, 12));
 		
 		textField_3 = new JTextField();
 		textField_3.setColumns(10);
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.TRAILING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblIloSw, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
 						.addComponent(lblIloZnakw)
 						.addComponent(label, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
 						.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(99))
 				.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblIloZda, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
 					.addGap(18)
 					.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(99, Short.MAX_VALUE))
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addGap(28)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblIloZda, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
 						.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblIloZnakw)
 						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblIloSw, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
 						.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(label, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap(24, Short.MAX_VALUE))
 		);
 		contentPane.setLayout(gl_contentPane);
 	}
 }
