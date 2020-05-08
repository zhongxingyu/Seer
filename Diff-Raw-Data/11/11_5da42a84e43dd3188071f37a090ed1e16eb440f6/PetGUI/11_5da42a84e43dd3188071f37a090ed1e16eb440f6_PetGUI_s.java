 package com.vetapp.pet;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JLabel;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.RowSpec;
 import com.vetapp.customer.Customer;
 import com.vetapp.customer.CustomerGUI;
 import com.vetapp.history.MedHistory;
 import com.vetapp.history.MedHistoryGUI;
 import com.vetapp.main.VetApp;
 import com.vetapp.util.PropetJMenuBar;
 
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 public class PetGUI extends JFrame implements ActionListener {
 
 	private JPanel contentPane;
 	private Customer customer = new Customer();
 	private Pet aPet = new Pet();
 	public static String MAIN_WINDOW_TITLE =" ProPet v0.5";
 	public SimpleDateFormat sdf1 = new SimpleDateFormat ("yyyy-MM-dd hh:mm"); 	//edited this//
 	public SimpleDateFormat sdf2 = new SimpleDateFormat ("yyyy-MM-dd"); 		//edited this//
 	private PropetJMenuBar bar = new PropetJMenuBar();
 
 
 	public PetGUI(Customer cus, Pet thePet) {
 		
 		//passing to private variables
 		customer = cus;
 		aPet = thePet;
 		
 		//JMenuBar
 		setJMenuBar(bar.drawJMenuBar());
 		
 		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED); //to default frame border gia ta panels me perigramma
 		setBounds(100, 100, 410, 355);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 
 		//-------------------- PET INFO PANEL ------------------------
 
 		JPanel petInfo_panel = new JPanel(); 		
 		petInfo_panel.setBounds(10, 11, 230, 180);
 		petInfo_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Pet Info"));
 		contentPane.add(petInfo_panel);
 		petInfo_panel.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("1px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("83px"),
 				ColumnSpec.decode("65px"),
 				ColumnSpec.decode("60px"),
 				ColumnSpec.decode("50dlu"),},
 				new RowSpec[] {
 				RowSpec.decode("1px"),
 				RowSpec.decode("max(1dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("14px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("14px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("14px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("14px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("14px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("14px"),}));
 
 		//Dedomena tou xristi (8a antikatastountai me ali8ina dedomena kata tin dimiourgia tou frame)
 		JLabel species = new JLabel(aPet.getSpecies());
 		petInfo_panel.add(species, "3, 4, 2, 1, left, center");
 
 		JLabel name = new JLabel(aPet.getName());
 		petInfo_panel.add(name, "3, 6, 2, 1, left, center");
 
 		JLabel gender = new JLabel(aPet.getGender());
 		petInfo_panel.add(gender, "3, 8, 2, 1, left, center");
 
		//JLabel birthDay = new JLabel(aPet.getBirthDay());
		//petInfo_panel.add(birthDay, "3, 10, 2, 1, left, center");
 
 		JLabel furColor = new JLabel(aPet.getFurColour());
 		petInfo_panel.add(furColor, "3, 12, 2, 1, left, center");
 
 		JLabel specialChars = new JLabel(aPet.getSpecialChars());
 		petInfo_panel.add(specialChars, "3, 14, 2, 1, left, center");
 
 
 		//-------------------- ELECTRIC CHIP PANEL ------------------------
 
 		JPanel electicchip_panel = new JPanel();				
 		electicchip_panel.setBounds(10, 202, 230, 50);
 		electicchip_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Electric Chip"));
 		contentPane.add(electicchip_panel);
 		electicchip_panel.setLayout(null);
 
 		JLabel chipNumberLabel = new JLabel(aPet.getChipNumber());
 		chipNumberLabel.setBounds(15, 10, 122, 39);
 		electicchip_panel.add(chipNumberLabel);
 
 		JButton deletePetButton = new JButton("Delete Pet (!)");			//Delete customer button 
 		deletePetButton.addActionListener(this);
 		deletePetButton.setBounds(10, 264, 120, 28);
 		contentPane.add(deletePetButton);
 
 		JButton editPetButton = new JButton("Edit Pet");					//Edit customer button 
 		editPetButton.addActionListener(this);
 		editPetButton.setBounds(147, 264, 90, 28);
 		contentPane.add(editPetButton);
 
 		//-------------------- IMAGE  --------------------
 
 		ImageIcon icon = setIcon("http://i.imgur.com/XPNpZSa.png");
 		JLabel imagePetLabel = new JLabel("",icon,JLabel.CENTER);
 		imagePetLabel.setBounds(250, 10, 140, 140);
 		contentPane.add(imagePetLabel);
 
 		JButton medicalHistoryButton = new JButton("Medical Histoty");      //Medical History  button 
 		medicalHistoryButton.addActionListener(this);
 		medicalHistoryButton.setBounds(253, 152, 130, 28);
 		contentPane.add(medicalHistoryButton);
 
 		JButton backButton = new JButton("Back");			//Back button
 		backButton.addActionListener(this);
 		backButton.setBounds(300, 264, 89, 28);
 		contentPane.add(backButton);
 
 		this.setResizable(false);
 		this.setVisible(true);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 	}
 
 	//----------------------------- PetGUI ACTION LISTENERS ----------------------------------
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		if (e.getActionCommand().equals("Delete Pet (!)")) {
 			Object[] options = {"Yes, delete this pet.",
 			"No way!"};
 			int n = JOptionPane.showOptionDialog(this,
 					"Are you sure you want to delete this pet record?\n"
 							+ "Warning: this action cannot be undone!",
 							"Confirm Pet Deletion",
 							JOptionPane.YES_NO_OPTION,
 							JOptionPane.WARNING_MESSAGE,
 							null,     //no custom icon
 							options,  //the titles of buttons
 							options[0]); //default button title
 			if (n == JOptionPane.YES_OPTION) {
 				VetApp.db.DBDeletePet(customer, aPet);
 				this.dispose();
 			} 
 		} else if (e.getActionCommand().equals("Edit Pet")) {
 			new editPetGUI();
 			PetGUI.this.dispose();
 		} else if (e.getActionCommand().equals("Medical Histoty")) {
 			new MedHistoryGUI(aPet);
 		} else if (e.getActionCommand().equals("Back")) {
 			this.dispose();
 		}
 
 	}
 	
 	/*
 	 * A method to retrieve the logo icon
 	 * */
 	public ImageIcon setIcon(String link) {
 		try {
 			URL url = new URL(link);
 			return (new ImageIcon(url));
 		} catch (MalformedURLException e) {
 			return null;
 		}
 	}
 
 	protected static ImageIcon createImageIcon(String path, String description) {
 		java.net.URL imgURL = CustomerGUI.class.getResource(path);
 		if (imgURL != null) {
 			return new ImageIcon(imgURL, description);
 		} 
 		else {
 			System.err.println("Couldn't find file: " + path);
 			return null;
 		}
 	}
 	
 	//============================================================================================
 	//--------------------------------- editPetGUI CLASS -----------------------------------------
 	//============================================================================================
 
 	public class editPetGUI extends JFrame implements ActionListener {
 
 		private JTextField speciesTxt;
 		private JTextField nameTxt;
 		private JTextField genderTxt;
 		private JTextField birthDayTxt;
 		private JTextField furColorTxt;
 		private JTextField specialCharsTxt;
 		private JTextField electicchipTxt;
 		private JPanel contentPane;
 
 		public editPetGUI() {
 			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED); //to default frame border gia ta panels me perigramma
 			setBounds(100, 100, 550, 380);
 			contentPane = new JPanel();
 			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 			setContentPane(contentPane);
 			contentPane.setLayout(null);
 
 
 			//-------------------- PET INFO PANEL ------------------------
 
 			JPanel petInfo_panel = new JPanel(); 		
 			petInfo_panel.setBounds(10, 11, 380, 200);
 			petInfo_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Edit Pet Info"));
 			contentPane.add(petInfo_panel);
 			petInfo_panel.setLayout(new FormLayout(new ColumnSpec[] {
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("max(8dlu;default)"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("max(69dlu;default):grow"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("default:grow"),},
 					new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC}));
 
 			JLabel speciesLabel = new JLabel("Species:*:");
 			petInfo_panel.add(speciesLabel, "2, 2,3,1");
 
 			speciesTxt = new JTextField();
 			speciesTxt.setText(aPet.getSpecies());
 			petInfo_panel.add(speciesTxt, "8, 2, 3, 1, fill, default");
 			speciesTxt.setColumns(10);
 
 			JLabel nameLabel = new JLabel("Name*:");
 			petInfo_panel.add(nameLabel, "2, 4,3,1");
 
 			nameTxt = new JTextField();
 			nameTxt.setText(aPet.getName());	
 			petInfo_panel.add(nameTxt, "8, 4, 3, 1, fill, default");
 			nameTxt.setColumns(10);
 
 			JLabel genderLabel = new JLabel("Gender:");
 			petInfo_panel.add(genderLabel, "2, 6");
 
 			genderTxt = new JTextField();
 			genderTxt.setText(aPet.getGender());
 			petInfo_panel.add(genderTxt, "8, 6, 3, 1, fill, default");
 			genderTxt.setColumns(10);
 
 			JLabel birthDayLabel = new JLabel("Birthday:");
 			petInfo_panel.add(birthDayLabel, "2, 8,3,1");
 
 			birthDayTxt = new JTextField();
			birthDayTxt.setText(sdf2.format(aPet.getBirthDay().getTime())); 
			//epeleksa to sdf2 format giati thewrhsa oti h wra den exei nohma gia thn genna, 
 			//alla me xrhsh tou sdf1 tha empanizontai epishs wra kai lepta.
 			petInfo_panel.add(birthDayTxt, "8, 8, 3, 1, fill, default");
 			birthDayTxt.setColumns(10);
 
 			JLabel furColorLabel = new JLabel("Fur Color:");
 			petInfo_panel.add(furColorLabel, "2, 10");
 
 			furColorTxt = new JTextField();
 			furColorTxt.setText(aPet.getFurColour());
 			petInfo_panel.add(furColorTxt, "8, 10, 3, 1, fill, default");
 			furColorTxt.setColumns(10);
 
 			JLabel specialCharsLabel = new JLabel("Special Characteristics:");
 			petInfo_panel.add(specialCharsLabel, "2, 12");
 
 			specialCharsTxt = new JTextField();
 			specialCharsTxt.setText(aPet.getSpecialChars());
 			petInfo_panel.add(specialCharsTxt, "8, 12, 3, 1, fill, default");
 			specialCharsTxt.setColumns(10);
 
 			//-------------------- ELECTRIC CHIP PANEL ------------------------
 
 			JPanel electicchip_panel = new JPanel();				
 			electicchip_panel.setBounds(10, 222, 380, 69);
 			electicchip_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Edit Electric Chip"));
 			contentPane.add(electicchip_panel);
 			electicchip_panel.setLayout(null);
 
 			JLabel electicchipLabel = new JLabel("ChipNumber:");
 			electicchipLabel.setBounds(18, 25, 74, 27);
 			electicchip_panel.add(electicchipLabel);
 
 			JTextField electicchipTxt = new JTextField();
 			electicchipTxt.setText(aPet.getChipNumber());
 			electicchipTxt.setBounds(101, 25, 198, 26);
 			electicchip_panel.add(electicchipTxt);
 
 			JButton SaveChangesButton = new JButton("Save Changes");			//Save Changes button 
 			SaveChangesButton.addActionListener(this);
 			SaveChangesButton.setBounds(187, 304, 120, 28);
 			contentPane.add(SaveChangesButton);
 
 			//-------------------- IMAGE  --------------------
 
 			JButton UploadImageButton = new JButton("Upload image");      
 			UploadImageButton.addActionListener(this);
 			UploadImageButton.setBounds(403, 52, 130, 28);
 			contentPane.add(UploadImageButton);
 
 			JButton cancelButton = new JButton("Cancel");			
 			cancelButton.addActionListener(this);
 			cancelButton.setBounds(320, 304, 89, 28);
 			contentPane.add(cancelButton);
 
 			this.setResizable(false);
 			this.setVisible(true);
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		}
 
 		
 		//----------------------------- editPetGUI ACTION LISTENERS ------------------------------------------
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getActionCommand().equals("Upload image")) {
 				//TODO
 
 			} else if (e.getActionCommand().equals("Save Changes")) {
 				//TODO
 
 			} else if (e.getActionCommand().equals("Cancel")) {
 				new PetGUI(customer,aPet);
 				dispose();
 
 			}
 		}
 	}
 }
