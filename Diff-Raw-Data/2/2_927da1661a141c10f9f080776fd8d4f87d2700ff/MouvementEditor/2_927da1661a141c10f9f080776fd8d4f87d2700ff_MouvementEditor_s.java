 package heig.igl3.roc2.GUI;
 
 import heig.igl3.roc2.Business.Budget;
 import heig.igl3.roc2.Business.Categorie;
 import heig.igl3.roc2.Business.Mouvement;
 import heig.igl3.roc2.Business.SousCategorie;
 import heig.igl3.roc2.Data.Roc2DB;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.text.MaskFormatter;
 
 /**
  * Classe MouvementEditor Affichage de l'éditeur de mouvements
  * 
  * @author Raphael Santos, Olivier Francillon, Chris Paccaud, Cédric Bugnon
  * 
  */
 @SuppressWarnings("serial")
 public class MouvementEditor extends JDialog implements ActionListener,
 		ItemListener, FocusListener {
 
 	private JPanel panel;
 	private JTextField libelle, montant;
 	private JFormattedTextField date;
 	private JComboBox<Categorie> CBcategorie;
 	private JComboBox<SousCategorie> CBsousCategorie;
 	private JComboBox<String> CBtype, CBtypeES;
 	private JComboBox<Integer> CBperiodicite;
 	@SuppressWarnings("unused")
 	// sur lblType
 	private JLabel lblLibelle, lblMontant, lblDate, lblCategorie,
 			lblSousCategorie, lblType, lblTypeES, lblPeriodicite;
 	private JButton btSubmit, btCancel;
 	public Mouvement mouvement, mouvToEdit;
 	private Budget budget;
 	private boolean edit;
 
 	/**
 	 * Constructeur
 	 * 
 	 * @param frame
 	 * @param modal
 	 * @param budget
 	 */
 	public MouvementEditor(JFrame frame, boolean modal, Budget budget) {
 		super(frame, modal);
 		this.budget = budget;
 
 		MaskFormatter df = null;
 
 		try {
 			df = new MaskFormatter("##.##.####");
 		} catch (java.text.ParseException e) {
 			System.err.println(e);
 		}
 		;
 
 		df.setPlaceholderCharacter('_');
 
 		lblLibelle = new JLabel("Libellé:");
 		lblMontant = new JLabel("Montant:");
 		lblDate = new JLabel("Date:");
 		lblCategorie = new JLabel("Catégorie:");
 		lblSousCategorie = new JLabel("Sous-catégorie:");
 		lblType = new JLabel("Type:");
 		lblTypeES = new JLabel("Entrée/Sortie:");
 		lblPeriodicite = new JLabel("Périodicité:");
 
 		libelle = new JTextField(25);
 
 		montant = new JTextField(10);
 		date = new JFormattedTextField(df);
 		CBtype = new JComboBox<String>();
 		CBtype.addItem("Ponctuel");
 		CBtype.addItem("Récurrent");
 		CBtypeES = new JComboBox<String>();
 		CBtypeES.addItem("Entrée");
 		CBtypeES.addItem("Sortie");
 		CBperiodicite = new JComboBox<Integer>();
		for (int i = 1; i < 12; i++) {
 			CBperiodicite.addItem(i);
 		}
 
 		CBcategorie = new JComboBox<Categorie>();
 		for (Categorie cat : budget.categories) {
 			CBcategorie.addItem(cat);
 		}
 		CBcategorie.addItemListener(this);
 
 		CBsousCategorie = new JComboBox<SousCategorie>();
 		Categorie cat = (Categorie) CBcategorie.getSelectedItem();
 		for (SousCategorie sousCat : cat.sousCategories)
 			CBsousCategorie.addItem(sousCat);
 
 		btSubmit = new JButton("Valider");
 		btCancel = new JButton("Annuler");
 		btSubmit.addActionListener(this);
 		btCancel.addActionListener(this);
 		montant.addFocusListener(this);
 
 		KeyAdapter actionClavier = new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				int key = e.getKeyCode();
 				if (key == KeyEvent.VK_ENTER)
 					btSubmit.doClick();
 				else if (key == KeyEvent.VK_ESCAPE)
 					btCancel.doClick();
 			}
 		};
 
 		panel = new JPanel(new GridLayout(8, 1));
 		panel.add(lblLibelle);
 		panel.add(libelle);
 		panel.add(lblMontant);
 		panel.add(montant);
 		panel.add(lblDate);
 		panel.add(date);
 		panel.add(lblCategorie);
 		panel.add(CBcategorie);
 		panel.add(lblSousCategorie);
 		panel.add(CBsousCategorie);
 		// panel.add(lblType);
 		// panel.add(CBtype);
 		panel.add(lblTypeES);
 		panel.add(CBtypeES);
 		panel.add(lblPeriodicite);
 		panel.add(CBperiodicite);
 		panel.add(btCancel);
 		panel.add(btSubmit);
 		setTitle("ROC2");
 		add(panel, BorderLayout.CENTER);
 		for (Component c : panel.getComponents()) {
 			c.addKeyListener(actionClavier);
 		}
 
 	}
 
 	/**
 	 * Constructeur
 	 * 
 	 * @param frame
 	 * @param modal
 	 * @param budget
 	 * @param mouv
 	 */
 	public MouvementEditor(JFrame frame, boolean modal, Budget budget,
 			Mouvement mouv) {
 		this(frame, modal, budget);
 		mouvToEdit = mouv;
 		this.edit = true;
 		libelle.setText(mouv.libelle);
 		montant.setText(Float.toString(mouv.montant));
 		GregorianCalendar dateGreg = mouv.date;
 		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 		dateFormat.setCalendar(dateGreg);
 		date.setText(dateFormat.format(dateGreg.getTime()));
 		Categorie categorie = null;
 		for (Categorie cat : budget.categories) {
 			if (cat.id == mouv.idCategorie) {
 				categorie = cat;
 			}
 		}
 		CBcategorie.setSelectedItem(categorie);
 		SousCategorie sousCategorie = null;
 		for (SousCategorie sousCat : categorie.sousCategories) {
 			if (sousCat.id == mouv.idSousCategorie) {
 				sousCategorie = sousCat;
 			}
 		}
 		CBsousCategorie.setSelectedItem(sousCategorie);
 		CBtypeES.setSelectedIndex(mouv.ESType);
 		CBperiodicite.setSelectedIndex(mouv.periodicite - 1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == btCancel) {
 			this.setVisible(false);
 			mouvement = null;
 		}
 
 		if (e.getSource() == btSubmit) {
 			if (libelle.getText().length() > 3
 					&& montant.getText().length() > 0
 					&& Float.valueOf(montant.getText()) > 0.00
 					&& montant.getText().matches("[0-9]*\\.?[0-9]+$")
 					&& date.getText().matches("[1-31]\\.[1-12]\\.[1-2999]") && edit) {
 				Categorie cat = (Categorie) CBcategorie.getSelectedItem();
 				SousCategorie sousCat = (SousCategorie) CBsousCategorie
 						.getSelectedItem();
 				DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
 				Date dateDate = null;
 				try {
 					dateDate = df.parse(date.getText());
 				} catch (ParseException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				GregorianCalendar cal = new GregorianCalendar();
 				cal.setTime(dateDate);
 				mouvement = Roc2DB.editMouvement(mouvToEdit.id,
 						libelle.getText(), Float.parseFloat(montant.getText()),
 						1, CBtypeES.getSelectedIndex(), cal,
 						CBperiodicite.getSelectedIndex() + 1, cat, sousCat,
 						budget.idBudget);
 				setVisible(false);
 			} else if (libelle.getText().length() > 3
 					&& montant.getText().length() > 0
 					&& Float.valueOf(montant.getText()) > 0.00
 					&& montant.getText().matches("[0-9]*\\.?[0-9]+$")
 					&& !date.getText().matches("[1-31]\\.[1-12]\\.[1-2999]")
 					&& CBsousCategorie.getSelectedItem() != null) {
 				Categorie cat = (Categorie) CBcategorie.getSelectedItem();
 				SousCategorie sousCat = (SousCategorie) CBsousCategorie
 						.getSelectedItem();
 				DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
 				Date dateDate = null;
 				try {
 					dateDate = df.parse(date.getText());
 				} catch (ParseException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				GregorianCalendar cal = new GregorianCalendar();
 				cal.setTime(dateDate);
 				mouvement = Roc2DB.addMouvement(libelle.getText(),
 						Float.parseFloat(montant.getText()), 1,
 						CBtypeES.getSelectedIndex(), cal,
 						CBperiodicite.getSelectedIndex() + 1, cat, sousCat,
 						budget.idBudget);
 				setVisible(false);
 			} else {
 				String message = "";
 				if(libelle.getText().length() < 4){
 					message = message + "- Le libellé est trop court (4 caractères minimum)\n";
 				}
 				if(montant.getText().length() == 0){
 				message = message + "- Veuillez entrer un montant\n";
 				}else{
 					if(Float.valueOf(montant.getText()) == 0.0){
 						message = message + "- Veuillez entrer un montant non nul\n";
 					}		
 					if(!montant.getText().matches("[0-9]*\\.?[0-9]")){
 						message = message + "- Veuillez entrez le montant sous la forme ##.##\n";
 					}
 				}
 				if(!date.getText().matches("[1-31]\\.[1-12]\\.[1-2999]")){
 					message = message + "- Veuillez entrer une date\n";
 				}
 				if(CBsousCategorie.getSelectedItem() == null){
 					message = message + "- Veuillez d'abors créer une sous catégorie\n";
 				}
 				JOptionPane.showMessageDialog(this,
 						message);
 				
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
 	 */
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		CBsousCategorie.removeAllItems();
 		Categorie cat = (Categorie) CBcategorie.getSelectedItem();
 		for (SousCategorie sousCat : cat.sousCategories)
 			CBsousCategorie.addItem(sousCat);
 
 	}
 
 	@Override
 	public void focusGained(FocusEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
 	 */
 	@Override
 	public void focusLost(FocusEvent e) {
 		if (e.getSource() == montant) {
 			if (!montant.getText().matches("[0-9]*\\.?[0-9]+$")) {
 				montant.setText("0.00");
 			}
 		}
 
 	}
 
 }
