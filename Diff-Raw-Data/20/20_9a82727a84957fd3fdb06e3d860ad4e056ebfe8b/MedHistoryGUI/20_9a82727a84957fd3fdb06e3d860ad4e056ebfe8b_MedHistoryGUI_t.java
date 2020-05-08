 package com.vetapp.history;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 
 //exo pros8esei kai forma gia na kineitai o xristis me tab kai oxi mono me click, gia na mpei xreiazetai import to 
 //parakato package kai uncomment to line 130
 
 //import org.eclipse.wb.swing.FocusTraversalOnArray;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.vetapp.customer.Customer;
 import com.vetapp.customer.CustomerGUI;
 import com.vetapp.main.VetApp;
 import com.vetapp.pet.Pet;
 import com.vetapp.pet.PetGUI;
 import com.vetapp.util.PropetJMenuBar;
 
 public class MedHistoryGUI extends JFrame {
 
 	private static MedHistory history;
 	private static String petGender;
 	private PropetJMenuBar bar = new PropetJMenuBar();
 	private JLabel lblMedicalHistory;
 	private JLabel NeuValLbl;
 	private JButton EditBtn;
 	private JButton CancelBtn;
 	private MedHistory newhistory;
 
 	public MedHistoryGUI(final Pet aPet) {
 
 		//history = new FemMedHistory();
 
 		//getting required history & passing it to private variable
 		petGender = aPet.getGender();
 		history = VetApp.db.DBGetMedHistory(aPet);
 
 		//JMenuBar
 		setJMenuBar(bar.drawJMenuBar());
 
 		//JFrame configuration
 		setVisible(true);
 		setBounds(100, 100, 511, 440);
 		setResizable(false);
 
 		getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("65px"),
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(200px;default):grow"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("77px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("86px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("25px"),
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("16px"),
 				ColumnSpec.decode("88px"),
 				ColumnSpec.decode("46px"),},
 				new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("20px"),
 				FormFactory.LINE_GAP_ROWSPEC,
 				RowSpec.decode("23px"),
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
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(7dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 
 		lblMedicalHistory = new JLabel();
 		lblMedicalHistory.setText("Medical History");
 		getContentPane().add(lblMedicalHistory, "6, 2, center, default");
 
 		JLabel VaccineLbl = new JLabel("   Vaccinated:");
 		VaccineLbl.setVerticalAlignment(SwingConstants.TOP);
 		getContentPane().add(VaccineLbl, "6, 5, fill, center");
 
 		VaccinesTxtFld = new JTextField();
 		VaccinesTxtFld.setEditable(false);
 		VaccineLbl.setLabelFor(VaccinesTxtFld);
 		getContentPane().add(VaccinesTxtFld, "6, 7, fill, fill");
 		VaccinesTxtFld.setColumns(10);
 		VaccinesTxtFld.setText(history.getGrafts());
 
 		JLabel AllergiesLbl = new JLabel("   Allergies:");
 		AllergiesLbl.setLabelFor(AllergiesLbl);
 		getContentPane().add(AllergiesLbl, "6, 9, left, center");
 
 		AllergiesTxtFld = new JTextField();
 		AllergiesTxtFld.setEditable(false);
 		getContentPane().add(AllergiesTxtFld, "6, 11, fill, fill");
 		AllergiesTxtFld.setColumns(10);
 		AllergiesTxtFld.setText(history.getAllergies());
 
 		JLabel DiseasesLbl = new JLabel("   Diseases:");
 		getContentPane().add(DiseasesLbl, "6, 13, left, center");
 
 		DiseasesTxtFld = new JTextField();
 		DiseasesTxtFld.setEditable(false);
 		DiseasesLbl.setLabelFor(DiseasesTxtFld);
 		getContentPane().add(DiseasesTxtFld, "6, 15, fill, fill");
 		DiseasesTxtFld.setColumns(10);
 		DiseasesTxtFld.setText(history.getDiseases());
 
 		JLabel SurgeriesLbl = new JLabel("   Surgeries:");
 		SurgeriesLbl.setLabelFor(SurgeriesLbl);
 		getContentPane().add(SurgeriesLbl, "6, 17, left, center");
 
 		SurgTxtFld = new JTextField();
 		SurgTxtFld.setEditable(false);
 		getContentPane().add(SurgTxtFld, "6, 19, fill, fill");
 		SurgTxtFld.setColumns(10);
 		SurgTxtFld.setText(history.getSurgeries());
 
 
 		JLabel MedTrLbl = new JLabel("   Medical Treatment:");
 		getContentPane().add(MedTrLbl, "6, 21, left, center");
 
 		MedTreatmentTxtFld = new JTextField();
 		MedTreatmentTxtFld.setEditable(false);
 		MedTrLbl.setLabelFor(MedTreatmentTxtFld);
 		getContentPane().add(MedTreatmentTxtFld, "6, 23, fill, fill");
 		MedTreatmentTxtFld.setColumns(10);
 		MedTreatmentTxtFld.setText(history.getMedicalTreatment());
 
 		JLabel NeuteringLbl = new JLabel("   Neutering:");
 		getContentPane().add(NeuteringLbl, "6, 25, left, top");
 
 		btnBirths = new JButton("Births");
 		btnBirths.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				BirthGUI birthwindow = new BirthGUI(history);
 				birthwindow.setVisible(true);
 			}
 		});
 		getContentPane().add(btnBirths, "10, 25");
 
 		CancelBtn = new JButton();
 		CancelBtn.setText("Cancel");
 		getContentPane().add(CancelBtn, "8, 31, 2, 1");
 		CancelBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				MedHistoryGUI.super.dispose();
 			}
 		});
 
 
 		NeuValLbl = new JLabel();
 		NeuValLbl.setText("No");
 		NeuValLbl.setEnabled(false);
 		getContentPane().add(NeuValLbl, "8, 25, center, default");
 
 		EditBtn = new JButton();
 		EditBtn.setText("Edit Medical History");
 		getContentPane().add(EditBtn, "6, 31");
 		EditBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				//make textfields editable
 				VaccinesTxtFld.setEditable(true);
 				AllergiesTxtFld.setEditable(true);
 				DiseasesTxtFld.setEditable(true);
 				SurgTxtFld.setEditable(true);
 				MedTreatmentTxtFld.setEditable(true);
 
 				//removing neutering label
 				NeuValLbl.setVisible(false);
 
 				//change title
 				lblMedicalHistory.setText("Edit Medical History");
 
 				//add the radio buttons
 				ButtonGroup BtnGroup = new ButtonGroup();
 
 				JRadioButton NeutBtnYes = new JRadioButton("Yes");
 				getContentPane().add(NeutBtnYes, "8, 25, center, top");
 				BtnGroup.add(NeutBtnYes);
 
 				JRadioButton NeutBtnNo = new JRadioButton("No");
 				NeutBtnNo.setSelected(true);
 				getContentPane().add(NeutBtnNo, "8, 26, center, top");
 				BtnGroup.add(NeutBtnNo);
 
 				//births button
 				btnBirths.setVisible(false);
 
 				//modify the buttons
 				CancelBtn.setText("Cancel");
 				CancelBtn.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						MedHistoryGUI.super.dispose();
 						new MedHistoryGUI(aPet);
 					}
 				});
 
 				EditBtn.setText("Save");
 				EditBtn.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						MedHistory newhistory = new MedHistory(AllergiesTxtFld.getText(),DiseasesTxtFld.getText(),VaccinesTxtFld.getText(),MedTreatmentTxtFld.getText(), SurgTxtFld.getText());
 						VetApp.db.DBUpdateMedHistory(history, newhistory);
 						MedHistoryGUI.super.dispose();
 						new MedHistoryGUI(aPet);
 					}
 				});
 
 
 
 
 			}
 		});
 
 
 		//getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{VaccinesTxtFld, 
 		//AllergiesTxtFld, DiseasesTxtFld, SurgTxtFld, MedTreatmentTxtFld, NeutBtnNo, NeutBtnYes, btnSaveChanges, 
 		//btnNewButton, VaccineLbl, MedTrLbl, AllergiesLbl, DiseasesLbl, SurgeriesLbl, NeuteringLbl}));
 
 	}
 	private String Grafts;
 	private String Alergies;
 	private String As8eneies;
 	private String Surgeries;
 	private String MedicalTreatment;
 	//private BirthGUI births;
 	private JTextField VaccinesTxtFld;
 	private JTextField AllergiesTxtFld;
 	private JTextField DiseasesTxtFld;
 	private JTextField SurgTxtFld;
 	private JTextField MedTreatmentTxtFld;
 	private static Boolean Female = false;
 	private JButton btnNewButton;
 	private JButton btnBirths;
 
 	public String getGrafts() {
 		return Grafts;
 	}
 	public void setGrafts(String grafts) {
 		Grafts = grafts;
 	}
 
 	public String getAlergies() {
 		return Alergies;
 	}
 	public void setAlergies(String alergies) {
 		Alergies = alergies;
 	}
 
 	public String getAs8eneies() {
 		return As8eneies;
 	}
 	public void setAs8eneies(String as8eneies) {
 		As8eneies = as8eneies;
 	}
 
 	public String getSurgeries() {
 		return Surgeries;
 	}
 	public void setSurgeries(String surgeries) {
 		Surgeries = surgeries;
 	}
 
 	public String getMedicalTreatment() {
 		return MedicalTreatment;
 	}
 	public void setMedicalTreatment(String medicalTreatment) {
 		MedicalTreatment = medicalTreatment;
 	}
 
 	public void CheckGender(){
 		if(petGender.equals("Female")){
 			Female = true;
 		}
 		//}
 	}
 
 	public static boolean isFemale() {
 		return Female.booleanValue();
 	}
 
 
 
 	//	//============================================================================================
 	//	//--------------------------------- EditMedHistoryGUI CLASS ----------------------------------
 	//	//============================================================================================
 	//
 	//		//metafer8ike ston listener tou medhistory
 	// 	public static class EditMedHistoryGUI extends JFrame { 
 	//
 	//		private String Grafts;
 	//		private String Alergies;
 	//		private String As8eneies;
 	//		private String Surgeries;
 	//		private String MedicalTreatment;
 	//		private BirthGUI births;
 	//		private JTextField VaccinesTxtFld;
 	//		private JTextField AllergiesTxtFld;
 	//		private JTextField DiseasesTxtFld;
 	//		private JTextField SurgTxtFld;
 	//		private final JRadioButton NeutBtnYes = new JRadioButton("Yes");
 	//		private JTextField MedTreatmentTxtFld;
 	//		private MedHistory newhistory;
 	//		private Boolean Female = MedHistoryGUI.isFemale();
 	//
 	//
 	//		public EditMedHistoryGUI() {
 	//			getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
 	//					FormFactory.UNRELATED_GAP_COLSPEC,
 	//					FormFactory.DEFAULT_COLSPEC,
 	//					FormFactory.UNRELATED_GAP_COLSPEC,
 	//					ColumnSpec.decode("65px"),
 	//					FormFactory.UNRELATED_GAP_COLSPEC,
 	//					ColumnSpec.decode("max(200px;default):grow"),
 	//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 	//					ColumnSpec.decode("77px"),
 	//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 	//					ColumnSpec.decode("86px"),
 	//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 	//					ColumnSpec.decode("25px"),
 	//					FormFactory.UNRELATED_GAP_COLSPEC,
 	//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 	//					ColumnSpec.decode("16px"),
 	//					ColumnSpec.decode("88px"),
 	//					ColumnSpec.decode("46px"),},
 	//					new RowSpec[] {
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					RowSpec.decode("20px"),
 	//					FormFactory.LINE_GAP_ROWSPEC,
 	//					RowSpec.decode("23px"),
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,
 	//					FormFactory.RELATED_GAP_ROWSPEC,
 	//					FormFactory.DEFAULT_ROWSPEC,}));
 	//
 	//			JLabel lblMedicalHistory = new JLabel("Edit Medical History");
 	//			getContentPane().add(lblMedicalHistory, "6, 2, 1, 2, center, default");
 	//
 	//			JLabel VaccineLbl = new JLabel("   Vaccinated:");
 	//			VaccineLbl.setVerticalAlignment(SwingConstants.TOP);
 	//			getContentPane().add(VaccineLbl, "6, 7, fill, center");
 	//
 	//			VaccinesTxtFld = new JTextField();
 	//			VaccineLbl.setLabelFor(VaccinesTxtFld);
 	//			getContentPane().add(VaccinesTxtFld, "6, 9, fill, fill");
 	//			VaccinesTxtFld.setColumns(10);
 	//			VaccinesTxtFld.setText(Grafts);
 	//
 	//			JLabel AllergiesLbl = new JLabel("   Allergies:");
 	//			AllergiesLbl.setLabelFor(AllergiesLbl);
 	//			getContentPane().add(AllergiesLbl, "6, 11, left, center");
 	//
 	//			AllergiesTxtFld = new JTextField();
 	//			getContentPane().add(AllergiesTxtFld, "6, 13, fill, fill");
 	//			AllergiesTxtFld.setColumns(10);
 	//			AllergiesTxtFld.setText(Alergies);
 	//
 	//			JLabel DiseasesLbl = new JLabel("   Diseases:");
 	//			getContentPane().add(DiseasesLbl, "6, 15, left, center");
 	//
 	//			DiseasesTxtFld = new JTextField();
 	//			DiseasesLbl.setLabelFor(DiseasesTxtFld);
 	//			getContentPane().add(DiseasesTxtFld, "6, 17, fill, fill");
 	//			DiseasesTxtFld.setColumns(10);
 	//			DiseasesTxtFld.setText(As8eneies);
 	//
 	//			JLabel SurgeriesLbl = new JLabel("   Surgeries:");
 	//			SurgeriesLbl.setLabelFor(SurgeriesLbl);
 	//			getContentPane().add(SurgeriesLbl, "6, 19, left, center");
 	//
 	//			SurgTxtFld = new JTextField();
 	//			getContentPane().add(SurgTxtFld, "6, 21, fill, fill");
 	//			SurgTxtFld.setColumns(10);
 	//			SurgTxtFld.setText(Surgeries);
 	//
 	//
 	//			JLabel MedTrLbl = new JLabel("   Medical Treatment:");
 	//			getContentPane().add(MedTrLbl, "6, 23, left, center");
 	//
 	//			MedTreatmentTxtFld = new JTextField();
 	//			MedTrLbl.setLabelFor(MedTreatmentTxtFld);
 	//			getContentPane().add(MedTreatmentTxtFld, "6, 25, fill, fill");
 	//			MedTreatmentTxtFld.setColumns(10);
 	//			MedTreatmentTxtFld.setText(MedicalTreatment);
 	//
 	//			JLabel NeuteringLbl = new JLabel("   Neutering:");
 	//			getContentPane().add(NeuteringLbl, "6, 27, left, default");
 	//
 	//			
 	//
 	//			
 	//			//getContentPane().add(btnNewButton, "8, 33, 2, 1");
 	//			//getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{VaccinesTxtFld, 
 	//			//AllergiesTxtFld, DiseasesTxtFld, SurgTxtFld, MedTreatmentTxtFld, NeutBtnNo, NeutBtnYes, btnSaveChanges, 
 	//			//btnNewButton, VaccineLbl, MedTrLbl, AllergiesLbl, DiseasesLbl, SurgeriesLbl, NeuteringLbl}));
 	//
 	//		}
 
 	//============================================================================================
 	//------------------------------------- BirthGUI CLASS ---------------------------------------
 	//============================================================================================
 
 	public class BirthGUI extends JFrame {
 		private JTable table;  // BirthTable
 		private JButton back_button ;
 		private JButton add_button;
 		private JButton save_button;
 		private BirthTableModel myModel = new BirthTableModel();;
 		public SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy");
 
 		private	 List<Birth> birthlist = new ArrayList<Birth>();
 
 		public BirthGUI(MedHistory history) {
 
 			//passing & getting required objects
 			birthlist = VetApp.db.DBGetAllBirths(history);
 
 			//JMenuBar
 			setJMenuBar(bar.drawJMenuBar());
 
 			//JFrame Configuration
 			setBounds(100, 100, 295, 374);
 
 			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			setAutoRequestFocus(false);
 
 			JPanel  panel = new JPanel();
 			panel.setLayout(new BorderLayout());
 
 			//-------------------- BIRTH TABLE --------------------
 
 			table = new JTable();
 			table.setModel(myModel);
 			myModel.reloadBirthJTable();
 			table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
 
 			table.getColumnModel().getColumn(0).setPreferredWidth(100);
 			table.getColumnModel().getColumn(1).setPreferredWidth(80);
 			table.getColumnModel().getColumn(2).setPreferredWidth(115);
 			table.setRowHeight(20);
 
 			JScrollPane scroller = new JScrollPane(table );
 			table.setBounds(49, 85, 295, 374);
 
 			panel.add(scroller,BorderLayout.CENTER);
 
 			JLabel label = new JLabel("Births Given");
 			//label.setFont(new Font("Tahoma", Font.BOLD, 11));
 
 			panel.add(label ,BorderLayout.NORTH);
 			back_button = new JButton("Back");
 			add_button = new JButton("Add");
 			save_button = new JButton("Save");
 
 			back_button.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0) {
 					dispose();
 				}
 			});
 
 			add_button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					myModel.addRow(new Object[]{"", "", ""});
 				}
 			});
 
 			save_button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 
 				}
 			});
 
 			JPanel panel_1 = new JPanel();  // Voithitiko panel
 			this.setContentPane(panel);
 
 			panel_1.add(add_button);
 			panel_1.add(save_button);
 			panel_1.add(back_button);
 
 			panel.add(panel_1 , BorderLayout.SOUTH);
 
 			this.setVisible(true);
 			this.setResizable(false);
 
 			//MouseAdapter
 			table.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {					
 					if (e.getClickCount() == 2) {
 						System.out.println("double click detected!");
 						int row = table.getSelectedRow();
 						table.isCellEditable(row, 0);
 						table.isCellEditable(row, 1); 
 						table.isCellEditable(row, 2); 
 						
 					}
 				}
 			}
 					);
 
 			table.addKeyListener(new KeyListener() {
 				public void keyReleased(KeyEvent e) {
 					int key=e.getKeyCode();
 					if (key==KeyEvent.VK_ENTER) {
 						System.out.println("Enter key pressed!");
 						System.out.println("Birth on " + table.getSelectedRow() + " row was selected!");
 						int row = table.getSelectedRow();
 						CellEditor cellEditor = table.getCellEditor();
 						if (cellEditor != null)
 							if (cellEditor.getCellEditorValue() != null)
 								cellEditor.stopCellEditing();
 							else
 								cellEditor.cancelCellEditing();
 						for(int i=0; i< birthlist.size(); i++){
 							if (birthlist.get(i).getDate()==table.getValueAt(row, 0) 
 									&& birthlist.get(i).getNumberOfChildren()==(Integer)table.getValueAt(row, 1)) {
 
 								Birth tempBirth =  birthlist.get(i);
 								Date birthDate = null;
 								try {
 									birthDate = ft.parse((String) table.getValueAt(row, 0));
 								} catch (ParseException e1) {
 									JOptionPane.showMessageDialog(null, "Error on date format.", "Date error", JOptionPane.ERROR_MESSAGE);
 								}
 								Calendar cal = new GregorianCalendar();
 								cal.setTime(birthDate);
 								birthlist.get(i).setDate(cal);
 								birthlist.get(i).setNumberOfChildren((Integer) table.getValueAt(row, 1));
 								birthlist.get(i).setComplications( (String) table.getValueAt(row, 2));
 								VetApp.db.DBUpdateBirth(tempBirth, birthlist.get(i));  // Enhmerwsh ths database
 								System.out.println("Birth Updated!");
 
 							}
 						}
 					}
 				}
 
 				@Override
 				public void keyPressed(KeyEvent arg0) {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void keyTyped(KeyEvent arg0) {
 					// TODO Auto-generated method stub
 
 				}
 			});
 
 		}
 	}
 
 	public static class BirthTableModel extends DefaultTableModel {
 
 		private String[] columnNames = {"Date", "Children", "Complications/Comments"};		//column header labels
 		private Object[][] data = new Object[20][3];
 		private  List<Birth>  list;
 
 		public void reloadBirthJTable() {
 			System.out.println("loading birth table..");
 			List<Birth> birthlist = new ArrayList<Birth>();
 			birthlist = VetApp.db.DBGetAllBirths(history);
 			clearJTable();
 			for(int i=0; i<birthlist.size(); i++){
 				data[i][0] = birthlist.get(i).getDate() ;
 				data[i][1] = birthlist.get(i).getNumberOfChildren();
 				data[i][2] = birthlist.get(i).getComplications();
 				this.addRow(data);
 			}
 		}
 
 		public void clearJTable() {
 			this.setRowCount(0);
 		}
 
		@Override
		public void addRow(Object[] arg0) {
			// TODO Auto-generated method stub
			super.addRow(arg0);
		}

 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 
 		@Override
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 
		@Override
		public boolean isCellEditable(int row, int col) {
		
 			return true;
		
 		}
 	}
 
 
 
 
 }
 
 
 
 
