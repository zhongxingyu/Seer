 package ui;
 
 import javax.swing.JPanel;
 
 import java.awt.GridBagLayout;
 
 import javax.swing.JLabel;
 
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.JButton;
 import javax.swing.SwingUtilities;
 
 import dao.MysqlDao;
 import model.Aeroport;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.sql.SQLException;
 import java.util.List;
 
 @SuppressWarnings("serial")
 public class PanelModifAeroport extends JPanel {
 	private JTextField textFieldCode;
 	private JTextField textFieldVille;
 	private JTextField textFieldPays;
 	private JLabel lblMessage;
 	private JButton btnMettreAJour;
 	private JButton btnReinitialiser;
 	private JButton btnSupprimer;
 	private MysqlDao dao = new MysqlDao();
 
 	public JTextField getTextFieldCode() {
 		return textFieldCode;
 	}
 
 	public JTextField getTextFieldVille() {
 		return textFieldVille;
 	}
 
 	public JTextField getTextFieldPays() {
 		return textFieldPays;
 	}
 
 	public JButton getBtnMettreAJour() {
 		return btnMettreAJour;
 	}
 
 	public JButton getBtnReinitialiser() {
 		return btnReinitialiser;
 	}
 
 	public JButton getBtnSupprimer() {
 		return btnSupprimer;
 	}
 	
 	public JLabel getLblMessage() {
 		return lblMessage;
 	}
 
 
 	public PanelModifAeroport() {
 		// on joue sur les dimensions de la grille pour positionner les éléments :
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 46, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		
 		// Label qui pourra contenir les différents messages à afficher :
 		lblMessage = new JLabel("");
 		lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
 		lblMessage.setForeground(Color.RED);
 		GridBagConstraints gbc_lblMessage = new GridBagConstraints();
 		gbc_lblMessage.gridwidth = 3;
 		gbc_lblMessage.anchor = GridBagConstraints.EAST;
 		gbc_lblMessage.insets = new Insets(0, 0, 5, 5);
 		gbc_lblMessage.gridx = 1;
 		gbc_lblMessage.gridy = 1;
 		add(lblMessage, gbc_lblMessage);
 		
 		// Les différents éléments du formulaires :
 		
 		JLabel lblCode = new JLabel("code AITA");
 		lblCode.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblCode = new GridBagConstraints();
 		gbc_lblCode.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCode.gridx = 1;
 		gbc_lblCode.gridy = 2;
 		add(lblCode, gbc_lblCode);
 		
 		textFieldCode = new JTextField();
 		textFieldCode.setEditable(false); // on empêche la modification de l'id
 		GridBagConstraints gbc_textFieldCode = new GridBagConstraints();
 		gbc_textFieldCode.gridwidth = 2;
 		gbc_textFieldCode.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldCode.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldCode.gridx = 2;
 		gbc_textFieldCode.gridy = 2;
 		add(textFieldCode, gbc_textFieldCode);
 		textFieldCode.setColumns(10);
 		
 		JLabel lblVille = new JLabel("ville");
 		lblVille.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblVille = new GridBagConstraints();
 		gbc_lblVille.insets = new Insets(0, 0, 5, 5);
 		gbc_lblVille.gridx = 1;
 		gbc_lblVille.gridy = 3;
 		add(lblVille, gbc_lblVille);
 		
 		textFieldVille = new JTextField();
 		GridBagConstraints gbc_textFieldVille = new GridBagConstraints();
 		gbc_textFieldVille.gridwidth = 2;
 		gbc_textFieldVille.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldVille.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldVille.gridx = 2;
 		gbc_textFieldVille.gridy = 3;
 		add(textFieldVille, gbc_textFieldVille);
 		textFieldVille.setColumns(10);
 		
 		JLabel lblPays = new JLabel("pays");
 		lblPays.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblPays = new GridBagConstraints();
 		gbc_lblPays.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPays.gridx = 1;
 		gbc_lblPays.gridy = 4;
 		add(lblPays, gbc_lblPays);
 		
 		textFieldPays = new JTextField();
 		GridBagConstraints gbc_textFieldPays = new GridBagConstraints();
 		gbc_textFieldPays.gridwidth = 2;
 		gbc_textFieldPays.insets = new Insets(0, 0, 5, 5);
 		gbc_textFieldPays.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFieldPays.gridx = 2;
 		gbc_textFieldPays.gridy = 4;
 		add(textFieldPays, gbc_textFieldPays);
 		textFieldPays.setColumns(10);
 		
 		btnMettreAJour = new JButton("mettre à jour");
 		btnMettreAJour.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// au clic sur le bouton, on va mettre à jour l'aéroport
 				
 				// On s'assure au préalable qu'un aéroport est bien sélectionné
 				// On récupère le code de l'aéroport en cours de modification
 				String codeAita = getTextFieldCode().getText();
 				if(codeAita.isEmpty()){
 					// Si aucun aéroport n'est sélectionné, on affiche un message et on ne continue pas
 					lblMessage.setText("Vous devez sélectionner un aéroport ci-dessus !");
 				}else{				
 					// Sinon, on récupère les autres informations du formulaire :
 					String ville = getTextFieldVille().getText();
 					String pays = getTextFieldPays().getText();
 					
 					// on vérifie que la ville n'existe pas déjà en base
 					// (pas possible de créer 2 aéroports pour une même ville)
 					try {
 						String[]villesEnBase = util.Donnees.getVillesProposees();
 						// On initialise 2 booléens à false
 						boolean villeExistante = false; // la ville existe-t-elle en base ?
 						boolean villeOk = false; // (si elle existe), correspond-elle à l'aéroport en cours de modif ?
 						for(String villeEnBase : villesEnBase){
 							if(villeEnBase.toUpperCase().equals(ville.toUpperCase())){
 								villeExistante = true;
 							}
 							if(villeExistante){
 								// si la ville existe déjà, je regarde si elle correspond à l'aéroport en cours de modification
 								// (dans ce cas, on ne bloque pas la modif ! On peut très bien modifier le pays pour cette ville)
 								// Je passe la première lettre de la ville en majuscule pour coller à la syntaxe en base
 								String villeBonFormat = util.MiseEnForme.UpperCaseFirstLetter(ville);
 								Aeroport aeroportVilleEnCours = dao.getAeroportByVille(villeBonFormat);
 								// on regarde si le code de l'aéroport en cours de modif correspond au code aéroport de la ville en cours
 								
 								if(aeroportVilleEnCours.getCodeAeroport().equals(codeAita)){
 									villeOk = true; // les villes coïncident, on ne bloque pas les modifs
 								}
 							}
 						}
 						if(!villeExistante || villeOk){
 							// si la ville n'existe pas déjà, ou si elle correspond à l'aéroport en cours
 							// on peut modifier l'aéroport
 					
 							// On définit l'expression régulière.
 							String regexLettresAccentsTirets = "^[A-Za-zàâäéèêëìîïôöòùûüçÀÂÄÉÈËÏÎÌÔÖÙÛÜÇ-]+$";
 							
 							// On vérifie que les 2 champs ne sont pas vides,
 							// qu'ils contiennent des lettres (accentuées ou non) ou tirets
 							if(ville.length()!=0 && pays.length()!=0
 									&& ville.matches(regexLettresAccentsTirets)
 									&& pays.matches(regexLettresAccentsTirets)){
 								// On met la première lettre de la ville et du pays en majuscule
 								// (uniformisé + permet que le tri des villes par ordre alphabétique soit correct dans le formulaire de création d'un vol)
 								String villeBonFormat = util.MiseEnForme.UpperCaseFirstLetter(ville);
 								String paysBonFormat = util.MiseEnForme.UpperCaseFirstLetter(pays);
 								
 								// On crée un objet Aeroport, et on appelle la méthode du dao pour la mise à jour
 								Aeroport a = new Aeroport(codeAita, villeBonFormat, paysBonFormat);
 								try {
 									if(dao.updateAeroport(a)){ // si la mise à jour s'est bien passée
 										// On affiche un message :
 										lblMessage.setText("La mise à jour de l'aéroport " + codeAita + " a bien été effectuée !");
 										
 										// On vide les champs texte et on rafraichit les données :
 										refreshData();
 									}else{
 										// En cas d'erreur, on affiche un message (soit il y a eu saisie ds le formulaire
 										// sans avoir sélectionné un aéroport, soit celui-ci est déjà utilisé pour un vol)
 										lblMessage.setText("<html><p>La mise à jour n'a pas pu être effectuée !<br>"
 												+ "Veuillez sélectionner un aéroport ci-dessus et renouveler l'opération.<br>"
 												+ "<u>ATTENTION, les aéroports déjà utilisés pour un vol ne sont plus modifiables.</u></p></html>");
 									}
 									
 								} catch (SQLException e) {
 									lblMessage.setText(e.getMessage());
 								}
 							}else{
 								lblMessage.setText("Les champs doivent contenir des lettres !");
 							}	
 						}else{
 							// si la ville existe déjà pour un autre aéroport, on affiche un message
 							lblMessage.setText("La ville " + ville + " existe déjà.");
 						}
 					} catch (SQLException e1) {
 						lblMessage.setText(e1.getMessage());
 					}
 				}
 			}
 		});
 		GridBagConstraints gbc_btnMettreAJour = new GridBagConstraints();
 		gbc_btnMettreAJour.insets = new Insets(0, 0, 5, 5);
 		gbc_btnMettreAJour.gridx = 2;
 		gbc_btnMettreAJour.gridy = 5;
 		add(btnMettreAJour, gbc_btnMettreAJour);
 		
 		btnReinitialiser = new JButton("réinitialiser");
 		btnReinitialiser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// au clic sur "réinitialiser", on réinitalise les champs à leur valeur initiale
 				
 				// On efface l'éventuel message saisi :
 				lblMessage.setText("");
 				
 				// On récupère le code de l'aéroport en cours de modification
 				String codeAita = getTextFieldCode().getText();
 				if(codeAita.isEmpty()){
 					lblMessage.setText("Vous devez sélectionner un aéroport ci-dessus !");
 				}else{
 					// On récupère les données de l'aéroport correspondant :
 					try {
 						Aeroport a = dao.getAeroportByCode(codeAita);
 						// On réinitialise les champs ville et pays :
 						getTextFieldVille().setText(a.getVille());
 						getTextFieldPays().setText(a.getPays());
 						
 					} catch (SQLException e) {
 						lblMessage.setText(e.getMessage());
 					}
 				}
 			}
 		});
 		GridBagConstraints gbc_btnAnnuler = new GridBagConstraints();
 		gbc_btnAnnuler.insets = new Insets(0, 0, 5, 5);
 		gbc_btnAnnuler.gridx = 3;
 		gbc_btnAnnuler.gridy = 5;
 		add(btnReinitialiser, gbc_btnAnnuler);
 		
 		btnSupprimer = new JButton("supprimer");
 		btnSupprimer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// au clic sur supprimer, on supprime l'aéroport de la base
 				
 				// On récupère le code de l'aéroport à supprimer :
 				String codeAita = getTextFieldCode().getText();
 				// On supprime l'aéroport :
 				try {
 					if(dao.deleteAeroport(codeAita)){ // si la suppression s'est bien passée
 						// On affiche un message :
 						lblMessage.setText("L'aéroport " + codeAita + " a bien été supprimé !");
 						
 						// On vide les champs texte et on rafraichit les données :
 						refreshData();
 						
 					}else{
 						// En cas d'erreur, on affiche un message (soit il y a eu saisie ds le formulaire
 						// sans avoir sélectionné un aéroport, soit celui-ci est déjà utilisé pour un vol)
 						lblMessage.setText("<html><p>La suppression n'a pas pu être effectuée !<br>"
 								+ "Veuillez sélectionner un aéroport ci-dessus et renouveler l'opération.<br>"
 								+ "<u>ATTENTION, les aéroports déjà utilisés pour un vol ne sont plus supprimables.</u></p></html>");
 					}
 				} catch (SQLException e) {
 					lblMessage.setText(e.getMessage());
 				}
 				
 			}
 		});
 		btnSupprimer.setFont(new Font("Tahoma", Font.BOLD, 13));
 		btnSupprimer.setBackground(Color.PINK);
 		btnSupprimer.setForeground(Color.RED);
 		GridBagConstraints gbc_btnSupprimer = new GridBagConstraints();
 		gbc_btnSupprimer.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSupprimer.gridx = 2;
 		gbc_btnSupprimer.gridy = 6;
 		add(btnSupprimer, gbc_btnSupprimer);
 
 	}
 
 	// méthode appelée après la mise à jour ou la suppression d'un aéroport
 	private void refreshData() throws SQLException{
 		// On vide les champs texte :
 		getTextFieldCode().setText("");
 		getTextFieldVille().setText("");
 		getTextFieldPays().setText("");
 		
 		// on recharge la liste des aéroports pour que les modifications apparaissent
 		List<Aeroport> aeroports = dao.getAllAeroports();
 		// on récupère la frame principale
 		FenetrePrincipale frame = (FenetrePrincipale) SwingUtilities.getRoot(PanelModifAeroport.this);
 		// on récupère la JTable
 		JTable table = frame.getPanelAeroports().getTableAeroports();
 		// On crée le model avec les bonnes données et on le donne à la JTable
 		// On utilise pour cela la méthode statique définie dans Aeroport
 		util.MiseEnForme.TableCreationAeroports(aeroports, table);
 		
		// on va également recharger la liste des villes proposées dans les formulaires de création
		// et modification d'un vol
 		String[]villes = util.Donnees.getVillesProposees();
 		// On insère les villes dans les comboBox
		JComboBox<String> comboBoxDepart1 = frame.getPanelNouveauVol().getComboBoxVilleDeDepart();
		JComboBox<String> comboBoxArrivee1 = frame.getPanelNouveauVol().getComboBoxVilleDarrivee();
		JComboBox<String> comboBoxDepart2 = frame.getPanelVolsEnAttente().getPanelModifVolEnAttente().getComboBoxVilleDeDepart();
		JComboBox<String> comboBoxArrivee2 = frame.getPanelVolsEnAttente().getPanelModifVolEnAttente().getComboBoxVilleDarrivee();
		JComboBox<String> comboBoxDepart3 = frame.getPanelVolsProgrammes().getPanelModifVolProgramme().getComboBoxVilleDeDepart();
		JComboBox<String> comboBoxArrivee3 = frame.getPanelVolsProgrammes().getPanelModifVolProgramme().getComboBoxVilleDarrivee();
		util.MiseEnForme.comboBoxCreation(villes, comboBoxDepart1);
		util.MiseEnForme.comboBoxCreation(villes, comboBoxArrivee1);
		util.MiseEnForme.comboBoxCreation(villes, comboBoxDepart2);
		util.MiseEnForme.comboBoxCreation(villes, comboBoxArrivee2);
		util.MiseEnForme.comboBoxCreation(villes, comboBoxDepart3);
		util.MiseEnForme.comboBoxCreation(villes, comboBoxArrivee3);
 	}
 }
