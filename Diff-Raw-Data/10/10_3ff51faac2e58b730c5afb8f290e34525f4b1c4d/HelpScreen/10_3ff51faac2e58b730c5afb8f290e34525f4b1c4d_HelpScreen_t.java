 package btlshp.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileInputStream;
 import java.util.Scanner;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class HelpScreen {
 	JFrame        mainFrame;
 	JButton       closeButton;
 	JPanel        buttonPane;
 	JLabel        helpLabel;
 	JScrollPane   helpScrollPane;
 	
 	
 	public HelpScreen() {
 	}
 	
 	public void buildUi() {
 		mainFrame = new JFrame("Btlshp Help");
 		mainFrame.setSize(640, 480);
 		//mainFrame.getContentPane().setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.PAGE_AXIS));
 		mainFrame.setResizable(false);
 		
 		helpLabel = new JLabel() {
 			private static final long serialVersionUID = 1493078874270486716L;
			
 		};
 		helpLabel.setBackground(mainFrame.getBackground());
 		helpLabel.setHorizontalAlignment(JLabel.CENTER);
 		helpLabel.setVerticalAlignment(JLabel.TOP);
 		
 		try {
 			FileInputStream ins = new FileInputStream("html/help.html");
 			Scanner			scanner = new Scanner(ins);
 			StringBuilder   text = new StringBuilder();			
 
 			while(scanner.hasNextLine()) {
 				text.append(scanner.nextLine() + "\n");
 			}
 			helpLabel.setText(text.toString());
 			ins.close();
 		}
 		catch(Exception ex) {
 			System.out.print(ex.toString());
 		}
 		
 		helpScrollPane = new JScrollPane(helpLabel) {
 			private static final long serialVersionUID = 1L;
 			public Dimension getPreferredSize() {
 				return new Dimension(638, 456);
 			}
 			public Dimension getMinimumSize() {
 				return new Dimension(638, 456);
 			}
 			public Dimension getMaximumSize() {
 				return new Dimension(638, 456);
 			}
 		};
 		mainFrame.add(helpScrollPane, BorderLayout.CENTER);
 		
 		closeButton = new JButton("Close");
 		closeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ev) {
 				mainFrame.setVisible(false);
 			}
 		});
 		
 		buttonPane = new JPanel();
 		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
 		buttonPane.add(Box.createHorizontalGlue());
 		buttonPane.add(closeButton);
 		
 		mainFrame.add(buttonPane, BorderLayout.PAGE_END);
 	}
 	
 	
 	public void show() {
 		mainFrame.pack();
 		mainFrame.setVisible(true);
 	}
 }
