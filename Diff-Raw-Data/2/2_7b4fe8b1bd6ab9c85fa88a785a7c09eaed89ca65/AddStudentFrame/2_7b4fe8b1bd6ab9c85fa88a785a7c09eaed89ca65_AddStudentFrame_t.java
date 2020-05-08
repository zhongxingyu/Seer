 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 
 public class AddStudentFrame extends JFrame implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 10L;
 	private JPanel contentPane;
 	public JTextField txtFieldFirstName;
 	public JTextField txtFieldLastName;
 	public JTextField txtFieldStudentID;
 	public JTextField txtFieldBirthDate;
 	public JComboBox combBoxBhLevel;
 	public JComboBox combBoxMathAsses;
 	public JComboBox combBoxReadAsses;
 	public JComboBox combBoxLaAsses;
 	public JButton add;
 	private MainFrame mc;
 	private JButton btnCancel;
 	
 	/**
 	 * Create the frame.
 	 */
 	public AddStudentFrame(MainFrame m) {
 		mc = m;
 	}
 	
 	void buildFrame() {
 		
 		//create the frame
 		setAlwaysOnTop(true);
 		setResizable(false);
 		setTitle("Add Student");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 230, 400);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
 		
 		txtFieldStudentID = new JTextField();
 		txtFieldStudentID.setEditable(true);
 		
 		txtFieldFirstName = new JTextField();
 		txtFieldFirstName.setEditable(true);
 		
 		txtFieldLastName = new JTextField();
 		txtFieldLastName.setEditable(true);
 		
 		txtFieldBirthDate = new JTextField();
 		txtFieldBirthDate.setEditable(true);
 		
 		
 		combBoxBhLevel = new JComboBox();
 		combBoxBhLevel.addItem(new Integer(1));
 		combBoxBhLevel.addItem(new Integer(2));
 		combBoxBhLevel.addItem(new Integer(3));
 		
 		String[] validStates = { "K", "1", "2", "3", "4", "5", "6", "7", "8" };
 		combBoxMathAsses = new JComboBox(validStates);
 		combBoxLaAsses = new JComboBox(validStates);
 		combBoxReadAsses = new JComboBox(validStates);
 		
 		contentPane.add(new JLabel("Student ID"));
 		contentPane.add(txtFieldStudentID);
 		contentPane.add(new JLabel("First Name"));
 		contentPane.add(txtFieldFirstName);
 		contentPane.add(new JLabel("Last Name"));
 		contentPane.add(txtFieldLastName);
 		contentPane.add(new JLabel("Birth Date"));
 		contentPane.add(new JLabel("(yyyy-mm-dd)"));
 		contentPane.add(txtFieldBirthDate);
 		contentPane.add(new JLabel("Behavioral Lavel"));
 		contentPane.add(combBoxBhLevel);
 		contentPane.add(new JLabel("Math"));
 		contentPane.add(combBoxMathAsses);
 		contentPane.add(new JLabel("LA"));
 		contentPane.add(combBoxLaAsses);
 		contentPane.add(new JLabel("Reading"));
 		contentPane.add(combBoxReadAsses);
 		
 		
 		//create button panel
 		JPanel btnPanel = new JPanel();
 		btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		contentPane.add(btnPanel);
 		
 		add = new JButton("Add");
 		add.addActionListener(mc);	
 		
 		btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(this);
 		
 		btnPanel.add(add);
 		btnPanel.add(btnCancel);
 		
 		pack();
 		setVisible(true);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == btnCancel) {
 			this.dispose();
 		}
 		
 	}
 }
