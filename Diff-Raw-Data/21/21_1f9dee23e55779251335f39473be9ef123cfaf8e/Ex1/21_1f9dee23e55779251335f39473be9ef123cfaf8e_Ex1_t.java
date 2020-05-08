 package max.test.ui.miglayout;
 
 import java.awt.EventQueue;
 import java.awt.LayoutManager;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 
 public class Ex1 extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4299592918557121279L;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Ex1 frame = new Ex1();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public Ex1() {
 
 		init();
 	}
 
 	private void init() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 
 		LayoutManager mgr = new MigLayout("flowy", "[grow,fill]", "[grow]");
 		getContentPane().setLayout(mgr);
 
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, "cell 0 0,grow");

		JScrollPane scrollPane = new JScrollPane();
		tabbedPane.addTab("New tab", null, scrollPane, null);

 		for (int i = 0; i < 3; i++) {
 			getContentPane().add(useCellsForComponents(), "cell 0 0,grow");
 		}
 	}
 
 	private JPanel useCellsForComponents() {
 		JTextField textField;
 		JTextField textField_1;
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new MigLayout("", "[][grow]", "[][]"));
 
 		JLabel lblSize = new JLabel("Enter size:");
 		textField = new JTextField();
 		textField.setColumns(10);
 		JLabel lblWieght = new JLabel("Enter weight:");
 		textField_1 = new JTextField();
 		textField_1.setColumns(10);
 
 		panel.add(lblSize, "cell 0 0,alignx trailing");
 		panel.add(textField, "cell 1 0,growx");
 		panel.add(lblWieght, "cell 0 1,alignx trailing");
 		panel.add(textField_1, "cell 1 1");
 
 		return panel;
 	}
 }
