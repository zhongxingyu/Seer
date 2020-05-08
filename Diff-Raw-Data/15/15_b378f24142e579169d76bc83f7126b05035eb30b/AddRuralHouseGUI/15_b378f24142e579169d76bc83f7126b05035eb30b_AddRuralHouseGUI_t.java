 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 
 
 import businessLogic.ApplicationFacadeInterface;
 
 import domain.Owner;
 
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.rmi.RemoteException;
 import java.awt.Color;
 import javax.swing.JRadioButton;
 import javax.swing.JCheckBox;
 
 public class AddRuralHouseGUI extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtHouseNum;
 	private JTextField txtTown;
 	private JTextField txtNumberOfRooms;
 	private JTextField txtNumberOfBeds;
 	private JCheckBox chckbxWifi = new JCheckBox();
 	private JFileChooser chooser; 
 
 	//	private AddRuralHouseBL BL = new AddRuralHouseBL();
 	private ApplicationFacadeInterface facade=StartWindow.getBusinessLogic();
 
 	/**
 	 * Create the frame.
 	 */
 	public AddRuralHouseGUI(final Owner owner) {
 		setBounds(100, 100, 460, 360);
 		contentPane = new JPanel();
 		contentPane.setBackground(new Color(152, 251, 152));
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		JLabel lblHouseNum = new JLabel("House number:");
 		lblHouseNum.setBounds(32, 21, 93, 41);
 		contentPane.add(lblHouseNum);
 
 		txtHouseNum = new JTextField();
 		txtHouseNum.setBounds(121, 31, 74, 20);
 		contentPane.add(txtHouseNum);
 		txtHouseNum.setColumns(10);
 
 		JLabel lblNumberOfBeds = new JLabel("Number of Rooms:");
 		lblNumberOfBeds.setBounds(32, 91, 93, 23);
 		contentPane.add(lblNumberOfBeds);
 
 		JLabel lblTown = new JLabel("Town:");
 		lblTown.setBounds(223, 34, 46, 14);
 		contentPane.add(lblTown);
 
 		txtTown = new JTextField();
 		txtTown.setBounds(261, 31, 86, 20);
 		contentPane.add(txtTown);
 		txtTown.setColumns(10);
 
 		txtNumberOfRooms = new JTextField();
 		txtNumberOfRooms.setBounds(135, 92, 212, 20);
 		contentPane.add(txtNumberOfRooms);
 		txtNumberOfRooms.setColumns(10);
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.setBackground(new Color(152, 251, 152));
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancel.setBounds(109, 266, 89, 23);
 		contentPane.add(btnCancel);
 
 		JButton btnAccept = new JButton("Accept");
 		btnAccept.setBackground(new Color(152, 251, 152));
 		btnAccept.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				int houseNumber = Integer.parseInt(txtHouseNum.getText());
 				String town = txtTown.getText();
 				int numberOfRooms = Integer.parseInt(txtNumberOfRooms.getText());
 				int numberOfBeds = Integer.parseInt(txtNumberOfBeds.getText());
 				Boolean wifi = (Boolean)chckbxWifi.isSelected();
 						//chooser returns File but we need Image
				String image = null;
				if (chooser != null)
					image = chooser.getSelectedFile().getAbsolutePath();
 
 				try {
 					facade.addRuralHouse(houseNumber, owner , numberOfRooms, numberOfBeds, wifi, image, town);
 					StartWindow.OWNER = facade.ownerloginBL(new Owner(owner.getName(),owner.getUsername(), owner.getPassword())); 
 				} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				setVisible(false);
 
 			}
 		});
 		btnAccept.setBounds(258, 266, 89, 23);
 
 		contentPane.add(btnAccept);
 
 		JButton btnImage = new JButton("Search image");
 		btnImage.setBackground(new Color(152, 251, 152));
 		btnImage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				chooser = new JFileChooser(); 
 				chooser.setCurrentDirectory(new java.io.File("."));
 				String choosertitle = null;
 				chooser.setDialogTitle(choosertitle);
 				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 				chooser.setAcceptAllFileFilterUsed(true);
 
 				//Print for test if it works
 				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
 					System.out.println("getCurrentDirectory(): " 
 							+  chooser.getCurrentDirectory());
 					System.out.println("getSelectedFile() : " 
 							+  chooser.getSelectedFile());
 				}
 				else {
 					System.out.println("No Selection ");
 				}
 			}
 		});
 		btnImage.setBounds(172, 216, 117, 25);
 		contentPane.add(btnImage);
 
 		JLabel lblNumberOfBeds_1 = new JLabel("Number of Beds:");
 		lblNumberOfBeds_1.setBounds(32, 125, 93, 14);
 		contentPane.add(lblNumberOfBeds_1);
 
 		txtNumberOfBeds = new JTextField();
 		txtNumberOfBeds.setBounds(135, 122, 212, 20);
 		contentPane.add(txtNumberOfBeds);
 		txtNumberOfBeds.setColumns(10);
 
 		JCheckBox chckbxWifi = new JCheckBox("Wifi");
 		chckbxWifi.setBounds(135, 163, 97, 23);
 		contentPane.add(chckbxWifi);
 	}
 }
