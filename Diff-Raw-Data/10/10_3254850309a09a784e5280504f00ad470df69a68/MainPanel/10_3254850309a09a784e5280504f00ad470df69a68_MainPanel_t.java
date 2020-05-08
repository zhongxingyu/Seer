 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class MainPanel {
 
 	JFrame frame;
 	JPanel panel;
 	JPanel south = new JPanel();
 	JPanel center = new JPanel();
 	
 	JButton searchButton;
 	JTextField input_text;
 	JComboBox<String> select_list;
 	JTextArea result_list;
 	boolean ord;
 	
 	// add components to frame
 	// design components
 	// add action for components
 	public MainPanel() {
 		
 		frame = new JFrame();
 		panel = new JPanel(new BorderLayout());
 		searchButton = new JButton();
 		input_text = new JTextField(20);
 		String[] select_choice = {"inceput", "sfarsit"};
 		select_list = new JComboBox<String>(select_choice);
 		result_list = new JTextArea("", 10, 25);
 		result_list.setLineWrap(true);
         JScrollPane scroll = new JScrollPane (result_list, 
 				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         
         south.add(input_text);
 		south.add(select_list);
 		south.add(searchButton);
 		
 		center.add(scroll, BorderLayout.CENTER);
 		panel.add(south, BorderLayout.NORTH);
 		
 		panel.add(center, BorderLayout.CENTER);
 		panel.setSize(400, 400);
 		frame.add(panel);
 		
 		design();
 		frame.setVisible(true);
 		
 		controller();	
 	}
 	
 	private void design() {
 
 		ImageIcon icon = new ImageIcon("logo.jpg");
 		Image img = icon.getImage();
 		frame.setIconImage(img);
 		frame.setForeground(Color.white);
 		frame.setTitle("Cu Substrat v1.0");
 		frame.setSize(600, 400);
 		
 		input_text.setBackground(new Color(170, 227, 214));
 		
 		searchButton.setBackground(Color.orange);
 		searchButton.setText("Cauta Substrat");
 		
 		result_list.setBackground(new Color(170, 227, 214));
 		result_list.setSelectionColor(Color.orange);
 	}
 	
 	private void controller() {
 		searchButton.addActionListener(new ActionListener() {
 		
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Controller controller;
 				if(select_list.getSelectedItem().toString() == "inceput") {
 					ord = true;
 				} else {
 					ord = false;
 				}
 				controller = new Controller(ord);
 				try {
 					controller.createTrie();
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				String searchedText = input_text.getText().toUpperCase();
				if (ord) {
					result_list.setText(controller.getWordList
							(searchedText));
				} else {
					result_list.setText(controller.getWordList
							(new StringBuilder(searchedText).reverse().toString()));
				}
				
 			}
 		});
 	}
 	
 	public static void main(String[] ar) {
 		new MainPanel();
 	}
 }
