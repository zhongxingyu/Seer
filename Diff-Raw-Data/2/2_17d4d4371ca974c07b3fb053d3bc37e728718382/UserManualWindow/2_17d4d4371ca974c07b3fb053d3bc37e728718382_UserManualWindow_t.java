 package gui;
 
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import api.API_Interface;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import javax.swing.JPanel;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Window;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JScrollPane;
 
 public class UserManualWindow {
 
 	private static JFrame frame;
 	private final JLabel SlideLabel = new JLabel("");
 	private int Slide = 1;
 	private API_Interface api;
 	/**
 	 * Create the application.
 	 */
 	public UserManualWindow(API_Interface a) 
 	{
 		api = a;
 		initialize();
 	}
 
 	/**
 	 * Initialise the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 1000, 825);
 		frame.setResizable(false);
 		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		frame.setResizable(true);
 		frame.getContentPane().setBackground(Color.WHITE);
 		final ImageIcon LessonPicture = api.getUserManual(Slide);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		frame.getContentPane().add(scrollPane, "2, 2, fill, fill");
 		
 		JPanel SlidePanel = new JPanel();
 		scrollPane.setViewportView(SlidePanel);
 		SlidePanel.setBackground(Color.WHITE);
 		SlidePanel.add(SlideLabel);
 		SlideLabel.setOpaque(true);
 		SlideLabel.setBackground(Color.WHITE);
 		SlideLabel.setIcon(LessonPicture);
 		
 		JPanel ButtonPanel = new JPanel();
 		ButtonPanel.setBackground(Color.WHITE);
 		frame.getContentPane().add(ButtonPanel, "2, 4, fill, fill");
 		ButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JButton PreviousButton = new JButton("Previous");
 		PreviousButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
				if(Slide > 1)
 				{
 					ImageIcon image = api.getUserManual(--Slide);
 					SlideLabel.setIcon(image);
 				}	
 			}
 		});
 		ButtonPanel.add(PreviousButton);
 		
 		JButton NextButton = new JButton("Next");
 		NextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				ImageIcon image = api.getUserManual(++Slide);
 				if(image != null)
 					SlideLabel.setIcon(image);
 				else
 					Slide--;
 			}
 		});
 		ButtonPanel.add(NextButton);
 	}
 
 	public void OpenWindow() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					//Test window = new Test();
 					frame.setVisible(true);
 					frame.setTitle("User Manual");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public static Window getFrame() {
 		return frame;
 	}
 }
