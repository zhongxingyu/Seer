 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 import main.Time;
 import model.Register;
 
 public class RegisterButton extends JButton implements ActionListener {
 	private BasicGUI gui;
 	private Register register;
 
 
 	/**
 	 * The constructor which creates a RegisterButton
 	 * 
 	 * @param gui
 	 *          the basicGUI
 	 * @param register
 	 * 			the target register
 	 */	
 	public RegisterButton(BasicGUI gui, Register register) {
 		super("Registrera förare");
 		this.gui = gui;
 		this.register = register;
 		addActionListener(this);
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 		String name = gui.getDriverText();
 		String[] times = Time.makeTimeList();
 		if (gui.getDriverText().length() != 0) {
 			regDriverToFile(name, times);
 
 		} else {
 			try {
 
 				String driverID = JOptionPane.showInputDialog(null,
 						"Den registrerade tiden är : " + times[0] + "."
 								+ times[1] + "." + times[2]
 								+ " \n Förarnummer: ");
 
 				if (!driverID.equals(JOptionPane.OK_OPTION)) {
 					regDriverToFile(driverID, times);
 				}
 			} catch (NullPointerException e) {
 			}
 
 		}
 	}
 
 	private void regDriverToFile(String name, String[] times) {
 		register.registerDriver(name);
 		gui.writeInScrollPane(times[0], times[1], times[2], name);
 	}
 }
