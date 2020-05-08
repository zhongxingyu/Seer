 package views.gui;
 
 import controllers.CalendarController;
 import controllers.WeekPickerController;
 import controllers.WeekPickerBackController;
 import controllers.WeekPickerNextController;
 import models.DisplayState;
 import models.Organizer;
 import views.gui.components.JEventDisplay;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 
 import models.Event;
 
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 public class Calendar implements Observer {
 
 	public static final String SRC_MAIN_IMAGES_DATE_PICKER_ICON_GIF = "src/main/images/DatePickerIcon.gif";
 	public static final String DEFAULT_USER_ICON = "src/main/images/DefaultUserIcon.png";
 	private JFrame frame;
 	private JEventDisplay eventDisplay;
 	private JLabel lblThereWillBe;
 	private JLabel lblLogin;
 	private JLabel lblUsername;
 	private JLabel lblUserPic;
 	private JLabel lblVelocity;
 	
     public JEventDisplay getEventDisplay() {
 		return eventDisplay;
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Calendar() {
 		initialize();
 	}
 
     public void setVisibility(boolean value) {
         frame.setVisible(value);
     }
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		
 		Organizer.getInstance();
 		frame = new JFrame();
 		frame.setBounds(100, 100, 800, 430);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setMinimumSize(new Dimension(frame.getWidth(), frame.getHeight()));
 		
 		JPanel panel = new JPanel();
 		
 		JPanel panel_2 = new JPanel();
 		
 		eventDisplay = new JEventDisplay();
 		eventDisplay.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				new EventDetails((Event) arg0.getSource()).setVisible(true);
 			}
 		});
 		eventDisplay.setToolTipText("");
 		
 		BufferedImage icon;
 		try {
 			icon = ImageIO.read(new File(Organizer.getInstance().getCurrentUser().getUserProfile().getIconPath()));
 			lblUserPic = new JLabel(new ImageIcon(icon));
 		} catch (Exception e) {
 			try {
 				icon = ImageIO.read(new File(Calendar.DEFAULT_USER_ICON));
 				lblUserPic = new JLabel(new ImageIcon(icon));
 			} catch (IOException e2) {
 				e2.printStackTrace();
 			}
 		}
 		
 		lblLogin = new JLabel("You are logged in as :");
 		lblUsername = new JLabel(Organizer.getInstance().getCurrentUser().getUsername());
 		
 		JPanel panel_1 = new JPanel();
 		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
 					.addGap(6))
 				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 					.addComponent(eventDisplay, GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap())
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 						.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(panel_2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 					.addGap(1)
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
 							.addGap(11))
 						.addComponent(eventDisplay, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)))
 		);
 		
 		JButton btnAddEvent = new JButton("Add Event");
 		btnAddEvent.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				new AddEvent().setVisible(true);
 			}
 		});
 		
 		JButton btnSettings = new JButton("Settings");
 		btnSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new Settings().setVisible(true);
 			}
 		});
 		
 		JButton btnImport = new JButton("Import");
 		btnImport.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Import clicked");
 			}
 		});
 		
 		JButton btnExport = new JButton("Export");
 		btnExport.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Export clicked");
 			}
 		});
 		
 		JButton btnChangeUser = new JButton("Log out");
 		btnChangeUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				new Login().setVisible(true);
 				frame.dispose();
 			}
 		});
 		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
 		gl_panel_1.setHorizontalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addComponent(lblLogin, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				.addComponent(btnAddEvent, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				.addComponent(btnSettings, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				.addComponent(btnExport, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				.addComponent(btnImport, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				.addComponent(btnChangeUser, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblUserPic)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(lblUsername, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(20, Short.MAX_VALUE))
 		);
 		gl_panel_1.setVerticalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addComponent(lblLogin)
 					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_1.createSequentialGroup()
 							.addGap(18)
 							.addComponent(lblUserPic))
 						.addGroup(gl_panel_1.createSequentialGroup()
 							.addGap(34)
 							.addComponent(lblUsername)))
 					.addGap(18)
 					.addComponent(btnAddEvent)
 					.addGap(18)
 					.addComponent(btnSettings)
 					.addGap(18)
 					.addComponent(btnImport)
 					.addGap(18)
 					.addComponent(btnExport)
 					.addGap(18)
 					.addComponent(btnChangeUser)
 					.addGap(145))
 		);
 		panel_1.setLayout(gl_panel_1);
 		
 		lblVelocity = new JLabel("Velocity: " + Organizer.getInstance().getCurrentUser().getUserProfile().getVelocity().toString());
 		lblVelocity.setFont(new Font("Dialog", Font.BOLD, 20));
 		panel_2.add(lblVelocity);
 		
 		JButton btnPrevious = new JButton("Previous");
 		btnPrevious.addActionListener(new WeekPickerBackController());
 		panel.add(btnPrevious);
 		
 		JButton btnNext = new JButton("Next");
 		btnNext.addActionListener(new WeekPickerNextController());
 		panel.add(btnNext);
 
         // TODO: find better way to provide initial value
 		lblThereWillBe = new JLabel(Organizer.getInstance().getCurrentUser().getUserProfile().getState().getRangeDisplay(), JLabel.CENTER);
         lblThereWillBe.setOpaque(true);
         lblThereWillBe.setBackground(Color.WHITE);
         lblThereWillBe.setPreferredSize(new Dimension(250, 25));
 		panel.add(lblThereWillBe);
 		
 		JButton btnDatePicker = new JButton(new ImageIcon(SRC_MAIN_IMAGES_DATE_PICKER_ICON_GIF));
 		btnDatePicker.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("DatePicker clicked");
 				new DatePicker(new WeekPickerController()).setVisible(true);
 			}
 		});
 		panel.add(btnDatePicker);
 		CalendarController calendarController = new CalendarController(eventDisplay);
 		Organizer.getInstance().addObserver(calendarController);
 		
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 	        public void windowClosing(WindowEvent e) {
 	        	Organizer.getInstance().saveToFile();
 	        }
 	        
 			@Override
 	        public void windowClosed(WindowEvent e) {
 				Organizer.getInstance().saveToFile();
 			}
 	    });
 		
 		frame.getContentPane().setLayout(groupLayout);
 		frame.setVisible(true);
 	}
 
     @Override
     public void update(Observable o, Object arg) {
     	if(arg != null) {
     		DisplayState state = (DisplayState) arg;
     		lblThereWillBe.setText(state.getRangeDisplay());
     	}
        lblVelocity.setText("Velocity: " + Organizer.getInstance().getCurrentUser().getUserProfile().getVelocity().toString());
     }
     
 	protected JLabel getLblThereWillBe() {
 		return lblThereWillBe;
 	}
 }
