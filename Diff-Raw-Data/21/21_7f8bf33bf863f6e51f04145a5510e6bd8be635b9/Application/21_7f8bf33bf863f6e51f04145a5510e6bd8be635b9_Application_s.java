 package tim.view;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Observable;
 
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import tim.application.Config;
 import tim.application.exception.ExceptionFormatter;
 import tim.application.exception.PersistanceException;
 import tim.application.utils.ErrorHandler;
 import tim.lib.dialog.Form;
 import tim.lib.dialog.FormComponent;
 import tim.lib.dialog.FormEntry;
 import tim.lib.dialog.TestDialog;
 import tim.view.appointmentdialog.AppointmentDialog;
 import tim.view.calendar.CalendarContainer;
 import tim.view.calendar.DayNavigation;
 
 public class Application extends JFrame implements AbstractView{
 	
 	JButton btnDialog;
 	AppointmentDialog eventDialog;
 	Menu menu;
 	CalendarContainer calendarContainer;
 	
 	//test
 	TestDialog tstDialog;
 	JButton btnTstDialog;
 	
 	public Application() {
 		try {
 			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
 		} catch (Exception ex) {
			ExceptionFormatter.format(ex, this.getClass().getName(), "constructor");
 		}
 
 		setTitle("TIM - Time Is Money");
 		setPreferredSize(new Dimension(600, 600));
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 		
 		menu = new Menu();
 		setJMenuBar(menu);
 		
 		btnDialog = new JButton("Dialogue");
 
 		btnTstDialog = new JButton("Dialogue de test");
 		calendarContainer = new CalendarContainer();
 
 		
 		Container container = getContentPane();
 
 		btnDialog.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showDialog();
 			}
 			
 		});
 		
 		btnTstDialog.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showTstDialog();
 			}
 			
 		});
 		
 		container.setLayout(new BorderLayout());
 		
 		JPanel testBar = new JPanel(new FlowLayout());
 		testBar.add(btnDialog);
 		testBar.add(btnTstDialog);
 		
 		JPanel navBar = new DayNavigation();
 		
 		container.add(testBar, BorderLayout.NORTH);
 		container.add(navBar, BorderLayout.CENTER);
 		container.add(calendarContainer, BorderLayout.SOUTH);
 	}
 
 	
 	public void showDialog() {
 
 		eventDialog = new AppointmentDialog(Application.this);
 		eventDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		eventDialog.setModal(true);
 		eventDialog.setResizable(false);
 		eventDialog.pack();
 		eventDialog.setLocationRelativeTo(Application.this);
 		eventDialog.setVisible(true);
 	}
 	
 	public void showTstDialog() {
 		Form form = new Form();
 		
 
 		JComboBox cbClient = new JComboBox();
 		
 		cbClient.addItem("test1");
 		cbClient.addItem("test2");
 		
 		
 		FormComponent cClient = new FormComponent(cbClient);
 		FormEntry eClient = new FormEntry(new JLabel("Client"));
 		eClient.addComponent(cClient);
 		form.addEntry(eClient);
 		
 		FormComponent cDate = new FormComponent(new JTextField(10));
 		FormEntry eDate = new FormEntry(new JLabel("Date"));
 		eDate.addComponent(cDate);
 		form.addEntry(eDate);
 		
 		JComboBox cbBeginH = new JComboBox();
 		JComboBox cbBeginM = new JComboBox();
 
 		JComboBox cbEndH = new JComboBox();
 		JComboBox cbEndM = new JComboBox();
 		
 		for (int i = Config.CALENDAR_DAY_START; i <= Config.CALENDAR_DAY_END; i++) {
 			cbBeginH.addItem(i);
 			cbEndH.addItem(i);
 		}
 
 		for (int i = 0; i < 60; i += Config.CALENDAR_DAY_INTERVAL) {
 			cbBeginM.addItem(i);
 			cbEndM.addItem(i);
 		}
 		
 		FormComponent cBeginH = new FormComponent(cbBeginH);
 		FormComponent cBeginM = new FormComponent(cbBeginM);
 		FormComponent cEndH = new FormComponent(cbEndH);
 		FormComponent cEndM = new FormComponent(cbEndM);
 		FormComponent cSeparator = new FormComponent(new JLabel(":"));
 		FormEntry eTest = new FormEntry(new JLabel ("Begin"));
 		eTest.addComponent(cBeginH);
 		eTest.addComponent(cBeginM);
 		eTest.addComponent(cSeparator);
 		eTest.addComponent(cEndH);
 		eTest.addComponent(cEndM);
 		form.addEntry(eTest);
 		
 		
 		tstDialog = new TestDialog(null, Application.this, form, null);
 		tstDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		tstDialog.setModal(true);
 		tstDialog.setResizable(false);
 		tstDialog.pack();
 		tstDialog.setLocationRelativeTo(Application.this);
 		tstDialog.setVisible(true);
 	}
 
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 }
