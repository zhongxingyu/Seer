 package fr.mercredymurderparty.ihm.composants;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import fr.mercredymurderparty.ihm.fenetres.FenetreAdmin;
 import fr.mercredymurderparty.outil.BaseDeDonnees;
 import fr.mercredymurderparty.outil.Message;
 import fr.mercredymurderparty.serveur.CoeurServeur;
 
 import javax.swing.JTextArea;
 import javax.swing.JComboBox;
 import net.miginfocom.swing.MigLayout;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import javax.swing.DefaultComboBoxModel;
 import java.awt.Font;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.FlowLayout;
 
 @SuppressWarnings("serial")
 public class ComposantGestionIndices extends JPanel
 {
 	
 // ----- ATTRIBUTS ----- //
 	
 	// ----- INTERFACE GRAPHIQUE ----- //
 	private JPanel panelGestionIndices;
 	private File image;
 	private JTextField champTitre;
 	private JTextField champNomPerso;
 	private JTextField champImageLien;
 	private JTextArea zoneContenu;
 	private JComboBox choixImportance;
 	private JCheckBox checkIndicePenalite;
 	private JComboBox choixPersonnage;
 	private JComboBox choixPersonnage2;
 	private JCheckBox checkTousLesJoueurs;
 	private DefaultTableModel modeleTableIndice;
 	private JTable tableIndices;
 	private JPanel pnlFormulaire;
 	private JButton btnAjouter;
 	private JButton btnModifier;
 	private JButton btnAjouterToJoueur;
 	private JCheckBox checkMemo;
 	
 	// ----- MODELES & CLASSES ----- //
 	private BaseDeDonnees bdd;
 	private CoeurServeur coeurServeur;
 	
 	// ----- VARIABLES ----- //
 	private boolean tempsPenalite = false;
 	private boolean tempsTousJoueurs = false;			
 	private boolean verrouillerComposant = false;
 	private int idIndice = -1;
 	private String loginJoueur = null;
 	private JPanel panel;
 	private JTextField champHeure;
 	private JLabel lblNewLabel;
 	private JTextField champMin;
 	private JLabel lblM;
 	private JTextField champSec;
 	private JLabel lblNewLabel_1;
 	
 	
 // ----- CONSTRUCTEUR ----- //
 	/**
 	 * FIXME
 	 * @param _fenetre
 	 * @param _springLayout
 	 * @param _coeurServeur
 	 */
 	public ComposantGestionIndices(FenetreAdmin _fenetre, final SpringLayout _springLayout, CoeurServeur _coeurServeur)
 	{
 		coeurServeur = _coeurServeur;
 		panelGestionIndices = new JPanel();
 		panelGestionIndices.setOpaque(false);
 		_springLayout.putConstraint(SpringLayout.NORTH, panelGestionIndices, -200, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
		_springLayout.putConstraint(SpringLayout.SOUTH, panelGestionIndices, 300, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.WEST, panelGestionIndices, -350, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.EAST, panelGestionIndices, 400, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_fenetre.getContentPane().add(panelGestionIndices);
 		panelGestionIndices.setLayout(new MigLayout("", "[][10.00][grow]", "[335.00,top]"));
 		this.setLayout(_springLayout);
 		SpringLayout springLayout = new SpringLayout();
 		setLayout(springLayout);
 		
 		pnlFormulaire = new JPanel();
 		pnlFormulaire.setOpaque(false);
 		springLayout.putConstraint(SpringLayout.NORTH, pnlFormulaire, 10, SpringLayout.NORTH, this);
 		springLayout.putConstraint(SpringLayout.WEST, pnlFormulaire, 10, SpringLayout.WEST, this);
 		springLayout.putConstraint(SpringLayout.SOUTH, pnlFormulaire, 394, SpringLayout.NORTH, this);
 		springLayout.putConstraint(SpringLayout.EAST, pnlFormulaire, 315, SpringLayout.WEST, this);
 		panelGestionIndices.add(pnlFormulaire, "cell 0 0");
 		pnlFormulaire.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(48dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(50dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(50dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(45dlu;min)"),
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
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JButton btnNouveauIndice = new JButton("Nouveau indice");
 		btnNouveauIndice.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _arg0) 
 			{
 				effacerFormulaire();
 			}
 		});
 		pnlFormulaire.add(btnNouveauIndice, "4, 2, 3, 1");
 		
 		JLabel lblTitre = new JLabel("Titre :");
 		pnlFormulaire.add(lblTitre, "2, 4");
 		
 		champTitre = new JTextField();
 		pnlFormulaire.add(champTitre, "4, 4, 3, 1, fill, default");
 		champTitre.setColumns(10);
 		
 		JLabel lblContenu = new JLabel("Contenu :");
 		pnlFormulaire.add(lblContenu, "2, 6");
 		
 		JScrollPane scrollPaneArea = new JScrollPane();
 		pnlFormulaire.add(scrollPaneArea, "4, 6, 3, 1, fill, fill");
 		zoneContenu = new JTextArea();
 		zoneContenu.setLineWrap(true);
 		scrollPaneArea.setViewportView(zoneContenu);
 		
 		JLabel lblPersonnage = new JLabel("Personnage :");
 		pnlFormulaire.add(lblPersonnage, "2, 8");
 		
 		choixPersonnage = new JComboBox();
 		choixPersonnage.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _e) 
 			{
 				if (verrouillerComposant == false)
 				{
 					String[] identiteJoueur = new String[2];
 					identiteJoueur = identiteJoueur((String) choixPersonnage.getSelectedItem());
 					if (identiteJoueur[0] != null)
 					{
 						champNomPerso.setText(identiteJoueur[0] + " " + identiteJoueur[1]);
 					}
 					else
 					{
 						rafraichirListeIndices();
 					}
 				}
 				
 				if (getIdIndice() == -1)
 				{
 					btnAjouter.setText("Ajouter");
 				}
 			}
 		});
 		pnlFormulaire.add(choixPersonnage, "4, 8, 3, 1, fill, default");
 		
 		champNomPerso = new JTextField();
 		champNomPerso.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		champNomPerso.setEditable(false);
 		pnlFormulaire.add(champNomPerso, "4, 10, 3, 1, fill, default");
 		champNomPerso.setColumns(10);
 		
 		JLabel lblImage = new JLabel("Image :");
 		pnlFormulaire.add(lblImage, "2, 12");
 		
 		champImageLien = new JTextField();
 		pnlFormulaire.add(champImageLien, "4, 12, fill, default");
 		champImageLien.setColumns(10);
 		
 		JButton btnChoisirImage = new JButton("Choisir");
 		btnChoisirImage.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _e) 
 			{
 				choisirImage();
 			}
 		});
 		pnlFormulaire.add(btnChoisirImage, "6, 12");
 		
 		JLabel lblPriorite = new JLabel("Importance :");
 		pnlFormulaire.add(lblPriorite, "2, 14");
 		
 		choixImportance = new JComboBox();
 		choixImportance.setModel((ComboBoxModel) new DefaultComboBoxModel(new String[] {"Peu important", "Important", "Tr\u00E8s important"}));
 		pnlFormulaire.add(choixImportance, "4, 14, 3, 1, fill, default");
 		
 		JLabel lblTemps = new JLabel("P\u00E9nalit\u00E9 :");
 		pnlFormulaire.add(lblTemps, "2, 16");
 		
 		panel = new JPanel();
 		pnlFormulaire.add(panel, "4, 16, 4, 1, left, fill");
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		champHeure = new JTextField();
 		panel.add(champHeure);
 		champHeure.setColumns(3);
 		
 		lblNewLabel = new JLabel("h");
 		panel.add(lblNewLabel);
 		
 		champMin = new JTextField();
 		panel.add(champMin);
 		champMin.setColumns(3);
 		
 		lblM = new JLabel("m");
 		panel.add(lblM);
 		
 		champSec = new JTextField();
 		panel.add(champSec);
 		champSec.setColumns(3);
 		
 		lblNewLabel_1 = new JLabel("s");
 		panel.add(lblNewLabel_1);
 		
 		JLabel lblOptions = new JLabel("Options:");
 		pnlFormulaire.add(lblOptions, "2, 18");
 		
 		checkIndicePenalite = new JCheckBox("Indice contre du temps");
 		checkIndicePenalite.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				if (checkIndicePenalite.isSelected())
 				{
 					champHeure.setText("00");
 					champMin.setText("00");
 					champSec.setText("00");
 					tempsPenalite = true;
 				}
 				else
 				{
 					champHeure.setText(null);
 					champMin.setText(null);
 					champSec.setText(null);
 					tempsPenalite = false;
 				}
 			}
 		});
 		pnlFormulaire.add(checkIndicePenalite, "4, 18, 4, 1");
 		
 		checkTousLesJoueurs = new JCheckBox("Indice pour tous les joueurs");
 		checkTousLesJoueurs.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				if (checkTousLesJoueurs.isSelected())
 				{
 					tempsTousJoueurs = true;
 					champHeure.setEditable(false);
 					champMin.setEditable(false);
 					champSec.setEditable(false);
 					choixPersonnage.setSelectedIndex(0);
 					choixPersonnage.setEnabled(false);
 					champNomPerso.setText("Tous les joueurs !");
 				}
 				else
 				{
 					tempsTousJoueurs = false;
 					champHeure.setEditable(true);
 					champMin.setEditable(true);
 					champSec.setEditable(true);
 					choixPersonnage.setEnabled(true);
 					champNomPerso.setText(null);
 				}
 			}
 		});
 		pnlFormulaire.add(checkTousLesJoueurs, "4, 20, 4, 1");
 		
 		btnAjouter = new JButton("Ajouter");
 		btnAjouter.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _arg0) 
 			{
 				// Vrifier si les champs sont biens remplis
 				if (champTitre.getText() != null && zoneContenu.getText() != null)
 				{
 					if (ajouterIndice())
 					{
 						JOptionPane.showMessageDialog(ComposantGestionIndices.this, "L'indice a t ajout avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un problme empche d'ajouter l'indice :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un ou plusieurs champs n'ont pas ts remplis ou ne correspondent pas aux critres requis.\nChamps obligatoires: titre, contenu.", "Formulaire non valide", JOptionPane.ERROR_MESSAGE);
 				}
 				coeurServeur.traiterMessage(1, new Message(1, Message.MAJ_LISTE_INDICES, (String) choixPersonnage.getSelectedItem()));
 			}
 		});
 		
 		btnAjouterToJoueur = new JButton("Ajouter  un joueur");
 		btnAjouterToJoueur.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _arg0) 
 			{
 				// Vrifier si les champs sont biens remplis
 				if (champTitre.getText() != null && zoneContenu.getText() != null)
 				{
 					if (ajouterRelationJoueurIndice())
 					{
 						JOptionPane.showMessageDialog(ComposantGestionIndices.this, "L'indice a t ajout avec succs au joueur !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un problme empche d'ajouter l'indice :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un ou plusieurs champs n'ont pas ts remplis ou ne correspondent pas aux critres requis.\nChamps obligatoires: titre, contenu.", "Formulaire non valide", JOptionPane.ERROR_MESSAGE);
 				}
 				coeurServeur.traiterMessage(1, new Message(1, Message.MAJ_LISTE_INDICES, (String) choixPersonnage.getSelectedItem()));
 			}
 		});
 		
 		btnModifier = new JButton("Modifier");
 		btnModifier.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _arg0) 
 			{
 				if (getIdIndice() != -1)
 				{
 					if (tempsTousJoueurs ==  false)
 					{
 						if (modifierIndice())
 						{
 							JOptionPane.showMessageDialog(ComposantGestionIndices.this, "L'indice a t modif avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 						}
 						else
 						{
 							JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un problme empche de modifier l'indice :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 						}
 					}
 					else
 					{
 						indiceTousLesJoueurs();
 					}
 				}
 				coeurServeur.traiterMessage(1, new Message(1, Message.MAJ_LISTE_INDICES, (String) choixPersonnage.getSelectedItem()));
 			}
 		});
 		
 		checkMemo = new JCheckBox("Se souvenir des donn\u00E9es");
 		checkMemo.setToolTipText("Cochez cette case pour garder les donn\u00E9es du dernier\r\nindice afin de les r\u00E9utiliser sur un autre\r\npersonnage, de fa\u00E7on diff\u00E9rente...");
 		pnlFormulaire.add(checkMemo, "4, 22, 4, 1");
 		pnlFormulaire.add(btnModifier, "4, 24");
 		pnlFormulaire.add(btnAjouter, "6, 24");
 		pnlFormulaire.add(btnAjouterToJoueur, "4, 26");
 		
 		JPanel pnlIndices = new JPanel();
 		pnlIndices.setOpaque(false);
 		panelGestionIndices.add(pnlIndices, "cell 2 0,grow");
 		pnlIndices.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(64dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				RowSpec.decode("max(15dlu;default)"),
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(160dlu;min)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblHistoriqueDesIndices = new JLabel("Historique des indices :");
 		lblHistoriqueDesIndices.setFont(new Font("Tahoma", Font.BOLD, 11));
 		pnlIndices.add(lblHistoriqueDesIndices, "2, 1, 3, 1");
 		
 		JLabel lblIndicesDuJoueur = new JLabel("Indices du Joueur:");
 		pnlIndices.add(lblIndicesDuJoueur, "2, 2, right, default");
 		
 		choixPersonnage2 = new JComboBox();
 		choixPersonnage2.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _arg0) 
 			{
 				if ((String) choixPersonnage2.getSelectedItem() != null)
 				{
 					int nombreLignes = tableIndices.getRowCount();
 					
 					for(int i = nombreLignes - 1; i >= 0; i--)
 					{
 						modeleTableIndice.removeRow(i);
 					}
 					
 					historique(tableIndices, (String) choixPersonnage2.getSelectedItem());
 				}
 				else
 				{
 					rafraichirListeIndices();
 				}
 			}
 		});
 		pnlIndices.add(choixPersonnage2, "4, 2, fill, default");
 		
 		JScrollPane spIndices = new JScrollPane();
 		pnlIndices.add(spIndices, "2, 4, 3, 5, fill, fill");
 		
 		// Charger les personnages
 		
 		modeleTableIndice = new DefaultTableModel();
 		tableIndices = new JTable(modeleTableIndice);
 		tableIndices.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseClicked(MouseEvent e) 
 			{
 				setIdIndice((Integer) contenuPremiereCelluleTableau(tableIndices, 0));
 				setLoginJoueur((String) contenuPremiereCelluleTableau(tableIndices, 1));
 				recupererContenuIndice(getIdIndice(), getLoginJoueur());
 			}
 		});
 		spIndices.setViewportView(tableIndices);
 		
 		JLabel lblChoisirUnIndice = new JLabel("Choisir un indice dans l'historique pour l'\u00E9diter ou supprimer");
 		lblChoisirUnIndice.setFont(new Font("Tahoma", Font.ITALIC, 11));
 		pnlIndices.add(lblChoisirUnIndice, "2, 10, 3, 1");
 		
 		JButton btnSupprimerIndice = new JButton("Supprimer l'indice");
 		btnSupprimerIndice.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _arg0) 
 			{
 				// Si on a slectionner un indice, on le supprime
 				if (getIdIndice() != -1)
 				{
 					if (supprimerIndice())
 					{
 						JOptionPane.showMessageDialog(ComposantGestionIndices.this, "L'indice a t supprimer avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un problme empche la suppression de l'indice :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Vous n'avez pas slectionner l'indice  supprimer !", "Attention", JOptionPane.WARNING_MESSAGE);
 				}
 				coeurServeur.traiterMessage(1, new Message(1, Message.MAJ_LISTE_INDICES, (String) choixPersonnage.getSelectedItem()));
 			}
 		});
 		pnlIndices.add(btnSupprimerIndice, "2, 14, 3, 1");
 	}
 	
 	
 // ----- METHODES ----- //
 	
 	/**
 	 * La mthode estVisible permet d'afficher ou non le conteneur de gestion indices
 	 * @param _visible : "boolean" affiche ou masque ce conteneur
 	 */
 	final public void estVisible(boolean _visible)
 	{
 		if (_visible)
 			this.setEnabled(true);
 		else
 			this.setEnabled(false);
 		
 		panelGestionIndices.setVisible(_visible);
 	}
 	
 	/**
 	 * Procdure qui rcupre dans la bdd l'identit du joueur
 	 * @param _login Login du joueur
 	 * @return Tableau avec nom et prenom
 	 */
 	private String[] identiteJoueur(String _login)
 	{
 		String login = _login;
 		String[] identite = new String[2];
 		
 		bdd = new BaseDeDonnees(coeurServeur);
 		try 
 		{
 		    ResultSet rs = bdd.executerRequete("SELECT nom, prenom FROM personnage WHERE login = '" + login + "'");
 		    while (rs.next()) 
 		    {
 		    	identite[0] = rs.getString("nom");
 		    	identite[1] = rs.getString("prenom");
 		    }
 		    rs.close();
 		} 
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 		}
 		bdd.fermerConnexion();
 		
 		return identite;	
 	}
 	
 	/**
 	 * Procdure qui charger les personnages de la partie en cours dans le combobox
 	 */
 	public void chargerPersonnages()
 	{
 		choixPersonnage.removeAllItems();
 		choixPersonnage2.removeAllItems();
 		
 		choixPersonnage.addItem(null);
 		choixPersonnage2.addItem(null);
 		
 		// Ouvrire une nouvelle connexion a la bdd
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 		    ResultSet rs = bdd.executerRequete("SELECT login FROM personnage");
 		    while (rs.next()) 
 		    {
 		      choixPersonnage.addItem(rs.getString("login"));
 		      choixPersonnage2.addItem(rs.getString("login"));
 		    }
 		    rs.close();
 		} 
 		catch (Exception e)
 		{
 			System.out.println(e.getMessage());
 		}
 		
 		// Fermer la connexion a la bdd
 		bdd.fermerConnexion();
 	}
 	
 	/**
 	 * Choisir une image sur le disque dur afin de dfinir l'indice visuel
 	 */
 	private void choisirImage()
 	{
 		JFileChooser dialogue = new JFileChooser(new File("."));
 		PrintWriter sortie;
 		File fichier;
 		
 		if (dialogue.showOpenDialog(null)== JFileChooser.APPROVE_OPTION) 
 		{
 		    fichier = dialogue.getSelectedFile();
 		    try 
 		    {
 				sortie = new PrintWriter(new FileWriter(fichier.getPath(), true));
 			    sortie.println();
 			    sortie.close();
 			} 
 		    catch (IOException e) 
 		    {
 				e.printStackTrace();
 			}
 		    finally
 		    {
 		    	champImageLien.setText(fichier.getName());
 		    	image = fichier;
 		    }
 		}
 	}
 	
 	/**
 	 * Procdure qui recupere le contenu d'un indice afin de l'diter ou le supprimer
 	 * @param _idIndice Numro d'identifiant de l'indice (pris depuis un jTable par exemple)
 	 */
 	public void recupererContenuIndice(int _idIndice, String _loginJoueur)
 	{
 		// Variables locale
 		String loginJoueur = _loginJoueur;
 		int idIndice = _idIndice;
 		String loginTemp = null;
 		
 		// Verrouille certains composants qui risquent de rentrer en conflits
 		verrouillerComposant = true;
 		
 		if (idIndice != -1)
 		{
 			// Ouvrir une nouvelle connexion  la bdd
 			bdd = new BaseDeDonnees(coeurServeur);
 			
 			try 
 			{
 				// recuperer l'id du joueur
 				int idJoueur = -1;
 				
 				if (loginJoueur != null)
 				{
 					ResultSet rs = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + _loginJoueur + "'");
 					rs.next();
 					idJoueur = rs.getInt("id"); if (rs.wasNull()) idJoueur = -1;
 					rs.close();
 				}
 
 				ResultSet rs2;
 				if (loginJoueur != null)
 				{
 					rs2 = bdd.executerRequete("" +
 							" SELECT ind.titre, ind.contenu, ind.importance, perso.login " +
 							" FROM indice ind, indice_relation indr " +
 							" LEFT JOIN personnage perso ON indr.ref_perso = perso.id " +
 							" WHERE ind.id = " + idIndice + " " +
 							" AND ind.id = indr.ref_indice" +
 							" AND  indr.ref_perso = " + idJoueur + " "
 					);
 				}
 				else
 				{
 					rs2 = bdd.executerRequete("" +
 							" SELECT ind.titre, ind.contenu, ind.importance, perso.login " +
 							" FROM indice ind, indice_relation indr " +
 							" LEFT JOIN personnage perso ON indr.ref_perso = perso.id " +
 							" WHERE ind.id = " + idIndice + " " +
 							" AND ind.id = indr.ref_indice"
 					);
 				}
 				
 				// Afficher les donnes du ResultSet
 				while (rs2.next()) 
 				{
 					// On temporise le login pour l'utiliser plus bas en dehors du ResultSet
 					loginTemp = rs2.getString("login"); 
 					
 					champTitre.setText(rs2.getString("titre"));
 					zoneContenu.setText(rs2.getString("contenu"));
 					choixPersonnage.setSelectedItem(loginTemp);
 					choixImportance.setSelectedIndex(Integer.parseInt(rs2.getString("importance")));
 				}
 				
 				// Fermer le ResultSet
 				rs2.close();
 				
 				// On dfinit le nom et prenom dans le champ appropri pour savoir a qui appartient le login
 				String[] identiteJoueur = new String[2];
 				identiteJoueur = identiteJoueur(loginTemp);
 				if (identiteJoueur[0] != null)
 				{
 					champNomPerso.setText(identiteJoueur[0] + " " + identiteJoueur[1]);
 				}
 				else
 				{
 					champNomPerso.setText(null);
 				}
 			} 
 			catch (SQLException sqe) 
 			{
 				System.out.println("Erreur base de donnes: " + sqe.getMessage());
 			}
 			catch (Exception e) 
 			{
 				System.out.println(e.getMessage());
 			}
 			finally
 			{
 				// On rentre en mode dition
 				btnModifier.setEnabled(true);
 			}
 			
 			// Fermer la connexion  la bdd
 			bdd.fermerConnexion();	
 		}
 		
 		// D-Verrouille certains composants qui risquent de rentrer en conflits
 		verrouillerComposant = false;
 	}
 	
 	/**
 	 * Procdure qui enregistre un nouvelle indice dans la bdd
 	 */
 	public boolean ajouterIndice()
 	{
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// Ajouter un nouvel indice dans la base de donnes
 			String sqlNouveauIndice = "INSERT INTO indice (titre, contenu, importance) VALUES (" +
 					"\"" + champTitre.getText() + "\", " +
 					"\"" + zoneContenu.getText() + "\", " +
 					"" + choixImportance.getSelectedIndex() +
 			" ) ";
 
 			// Si le MJ veux ajouter un indice a 1 ou tous les personnages...
 			if (tempsTousJoueurs == false)
 			{
 				// Ajouter un nouvel indice dans la base de donnes
 				bdd.executerInstruction(sqlNouveauIndice);
 				
 				// On recupre le dernier id de l'indice dans la bdd
 				int idIndice = -1;
 				idIndice = bdd.executerRequete("SELECT last_insert_rowid()").getInt("last_insert_rowid()");
 				
 				// si on a dfinit une image, on l'insre
 				if (champImageLien.getText().length() > 0)
 				{
 					bdd.enregistrerImage(image.getAbsolutePath(), idIndice);
 				}
 				
 				// On recupre l'id du joueur
 				int idJoueur = -1;
 				if (choixPersonnage.getSelectedItem() != null)
 					idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + (String) choixPersonnage.getSelectedItem() + "'").getInt("id");
 				
 				// Ajouter un nouvel indice dans la base de donnes
 				bdd.executerInstruction("INSERT INTO indice_relation (ref_perso, ref_indice) VALUES (" +
 						" " + idJoueur + ", " +
 						" " + idIndice + " )" +
 				"");
 			}
 			else
 			{
 				// Ajouter un nouvel indice dans la base de donnes
 				bdd.executerInstruction(sqlNouveauIndice);
 				
 				// On recupre le dernier id de l'indice dans la bdd
 				int idIndice = -1;
 				idIndice = bdd.executerRequete("SELECT last_insert_rowid()").getInt("last_insert_rowid()");
 				
 				// si on a dfinit une image, on l'insre
 				if (champImageLien.getText().length() > 0)
 				{
 					bdd.enregistrerImage(image.getAbsolutePath(), idIndice);
 				}
 				
 				for (int i=0; i<choixPersonnage.getItemCount(); i++)
 				{
 					if (choixPersonnage.getItemAt(i) != null)
 					{
 						// On recupre l'id du joueur
 						int idJoueur = -1;
 						idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + choixPersonnage.getItemAt(i) + "'").getInt("id");
 						
 						// Ajouter un nouvel indice dans la base de donnes
 						bdd.executerInstruction("INSERT INTO indice_relation (ref_perso, ref_indice) VALUES (" +
 								" " + idJoueur + ", " +
 								" " + idIndice + " )" +
 						"");
 					}
 				}
 			}
 			
 			// Penalit du temps joueur
 			if (tempsPenalite && choixPersonnage.getSelectedItem() != null)
 			{
 				tempsContreIndice((String) choixPersonnage.getSelectedItem());
 			}
 		} 
 		catch (SQLException sqe) 
 		{
 			System.out.println("Erreur base de donnes: " + sqe.getMessage());
 			return false;
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		
 		// Fermer la connexion  la base de donnes
 		bdd.fermerConnexion();
 		
 		// Actualiser la liste des indices
 		rafraichirListeIndices();
 		
 		// R.A.Z des composants du formulaire
 		effacerFormulaire();
 		
 		// Si on arrive ici, c'est qu'on a pas eu de problmes
 		return true;
 	}
 	
 	/**
 	 * Procdure qui enregistre un nouvelle indice dans la bdd
 	 */
 	public boolean ajouterRelationJoueurIndice()
 	{
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// Si le MJ veux ajouter un indice a 1 ou tous les personnages...
 			if (tempsTousJoueurs == false)
 			{
 				int idInd = getIdIndice();
 				System.out.println(idInd);
 				// si on a dfinit une image, on l'insre
 				if (champImageLien.getText().length() > 0)
 				{
 					bdd.enregistrerImage(image.getAbsolutePath(), idIndice);
 				}
 				
 				// On recupre l'id du joueur
 				int idJoueur = -1;
 				if (choixPersonnage.getSelectedItem() != null)
 					idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + (String) choixPersonnage.getSelectedItem() + "'").getInt("id");
 				
 				// Ajouter un nouvel indice dans la base de donnes
 				bdd.executerInstruction("INSERT INTO indice_relation (ref_perso, ref_indice) VALUES (" +
 						" " + idJoueur + ", " +
 						" " + idIndice + " )" +
 				"");
 			}
 			else
 			{
 				// On recupre le dernier id de l'indice dans la bdd
 				int idIndice = -1;
 				idIndice = bdd.executerRequete("SELECT last_insert_rowid()").getInt("last_insert_rowid()");
 				
 				// si on a dfinit une image, on l'insre
 				if (champImageLien.getText().length() > 0)
 				{
 					bdd.enregistrerImage(image.getAbsolutePath(), idIndice);
 				}
 				
 				for (int i=0; i<choixPersonnage.getItemCount(); i++)
 				{
 					if (choixPersonnage.getItemAt(i) != null)
 					{
 						// On recupre l'id du joueur
 						int idJoueur = -1;
 						idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + choixPersonnage.getItemAt(i) + "'").getInt("id");
 						
 						// Ajouter un nouvel indice dans la base de donnes
 						bdd.executerInstruction("INSERT INTO indice_relation (ref_perso, ref_indice) VALUES (" +
 								" " + idJoueur + ", " +
 								" " + idIndice + " )" +
 						"");
 					}
 				}
 			}
 			
 			// Penalit du temps joueur
 			if (tempsPenalite && choixPersonnage.getSelectedItem() != null)
 			{
 				tempsContreIndice((String) choixPersonnage.getSelectedItem());
 			}
 		} 
 		catch (SQLException sqe) 
 		{
 			System.out.println("Erreur base de donnes: " + sqe.getMessage());
 			return false;
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		
 		// Fermer la connexion  la base de donnes
 		bdd.fermerConnexion();
 		
 		// Actualiser la liste des indices
 		rafraichirListeIndices();
 		
 		// R.A.Z des composants du formulaire
 		effacerFormulaire();
 		
 		// Si on arrive ici, c'est qu'on a pas eu de problmes
 		return true;
 	}
 	
 	/**
 	 * Procdure qui modifie un indice existant dans la bdd
 	 */
 	public boolean modifierIndice()
 	{
 		// Variables locale
 		int idIndice = getIdIndice();
 		String loginJoueur = getLoginJoueur();
 		
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// Ajouter un nouvel indice dans la base de donnes
 			bdd.executerInstruction("UPDATE indice SET " +
 					" titre = \"" + champTitre.getText() + "\", " +
 					" contenu = \"" + zoneContenu.getText() + "\", " +
 					" importance = " + choixImportance.getSelectedIndex()  +
 					" WHERE id = " + idIndice
 			);
 			
 			// si on a dfinit une image, on l'insre
 			if (champImageLien.getText().length() > 0)
 			{
 				bdd.enregistrerImage(image.getAbsolutePath(), idIndice);
 			}
 			
 			// recuperer l'id du joueur
 			int idNouveauJoueur = -1;
 			if ((loginJoueur != null && (String) choixPersonnage.getSelectedItem() != null) || (String) choixPersonnage.getSelectedItem() != null)
 			{
 				idNouveauJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + (String) choixPersonnage.getSelectedItem() + "'").getInt("id");
 			}
 			
 			// Si on modifie selon le login d'un joueur
 			// recuperer l'id du joueur
 			int idAncienJoueur = -1;
 			if (loginJoueur != null)
 			{
 				idAncienJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + getLoginJoueur() + "'").getInt("id");
 			}
 			
 			if (loginJoueur != null)
 			{
 				// Mettre a jours les relations indices <> personnages
 				bdd.executerInstruction("UPDATE indice_relation SET " +
 						" ref_perso = " + idNouveauJoueur  +
 						" WHERE ref_indice = " + idIndice +
 						" AND ref_perso = " + idAncienJoueur
 				);
 			}
 			else
 			{
 				if (idNouveauJoueur == -1)
 				{
 					// Mettre a jours les relations indices <> personnages
 					bdd.executerInstruction("UPDATE indice_relation SET " +
 							" ref_perso = -1 " +
 							" WHERE ref_indice = " + idIndice +
 							" AND ref_perso = " + idAncienJoueur
 					);
 				}
 				else
 				{
 					// Mettre a jours les relations indices <> personnages
 					bdd.executerInstruction("UPDATE indice_relation SET " +
 							" ref_perso = " + idNouveauJoueur  +
 							" WHERE ref_indice = " + idIndice +
 							" AND ref_perso = " + idAncienJoueur
 					);
 				}
 			}
 			
 			// Penalit du temps joueur
 			if (tempsPenalite && choixPersonnage.getSelectedItem() != null)
 			{
 				tempsContreIndice((String) choixPersonnage.getSelectedItem());
 			}
 		} 
 		catch (SQLException sqe) 
 		{
 			System.out.println("Erreur base de donnes: " + sqe.getMessage());
 			return false;
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		
 		// Fermer la connexion  la base de donnes
 		bdd.fermerConnexion();
 		
 		// Actualiser la liste des indices
 		rafraichirListeIndices();
 		
 		// R.A.Z des composants du formulaire
 		effacerFormulaire();
 		
 		// Si on arrive ici, c'est qu'on a pas eu de problmes
 		return true;
 	}
 	
 	/**
 	 * On traite comme cas  part quand on souhaite ajouter / modifier indice(s) pour tous les joueurs
 	 */
 	public boolean indiceTousLesJoueurs()
 	{
 		int idIndiceLocal = getIdIndice();
 		String sqlNouveauIndice = null;
 		boolean etat = true;
 		
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// Ajouter un nouvel indice dans la base de donnes
 			sqlNouveauIndice = "INSERT INTO indice (titre, contenu, importance) VALUES (" +
 					"\"" + champTitre.getText() + "\", " +
 					"\"" + zoneContenu.getText() + "\", " +
 					"" + choixImportance.getSelectedIndex() +
 			" ) ";
 			
 			// Ajouter un nouvel indice dans la base de donnes
 			bdd.executerInstruction(sqlNouveauIndice);
 			
 			// On recupre le dernier id de l'indice dans la bdd
 			int idIndice = -1;
 			idIndice = bdd.executerRequete("SELECT last_insert_rowid()").getInt("last_insert_rowid()");
 			
 			// si on a dfinit une image, on met a jours le champs en question
 			if (champImageLien.getText().length() > 0)
 			{
 				bdd.enregistrerImage(image.getAbsolutePath(), idIndice);
 			}
 			
 			for (int i=0; i<choixPersonnage.getItemCount(); i++)
 			{
 				if (choixPersonnage.getItemAt(i) != null)
 				{
 					// On recupre l'id du joueur
 					int idJoueur = -1;
 					idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + choixPersonnage.getItemAt(i) + "'").getInt("id");
 					
 					// Ajouter un nouvel indice dans la base de donnes
 					bdd.executerInstruction("INSERT INTO indice_relation (ref_perso, ref_indice) VALUES (" +
 							" " + idJoueur + ", " +
 							" " + idIndice + " )" +
 					"");
 				}
 			}
 			
 			// Supprimer l'indice vide & sa relation
 			bdd.executerInstruction("DELETE FROM indice " + " WHERE id = " + idIndiceLocal);
 			bdd.executerInstruction("DELETE FROM indice_relation " + " WHERE ref_indice = " + idIndiceLocal);
 			
 			// Message de confirmation
 			JOptionPane.showMessageDialog(ComposantGestionIndices.this, "L'indice a t distribu a tous les joueurs avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 		} 
 		catch (Exception e) 
 		{
 			// Trace debug
 			System.out.println("Erreur lors du traitement pour les indices vers tous les joueurs: \n" + e.getStackTrace());
 			
 			// Message d'erreur
 			JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un problme empche de distribuer l'indice  tous les joueurs :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 			
 			// Changer l'etat de fin de fonction
 			etat = false;
 		}
 		
 		// Fermer la connexion  la bdd
 		bdd.fermerConnexion();
 		
 		// Actualiser la liste des indices
 		rafraichirListeIndices();
 		
 		// R.A.Z des composants du formulaire
 		effacerFormulaire();
 		
 		// Retourner l'etat de la fonction
 		return etat;
 	}
 	
 	/**
 	 * Procdure qui rafraichit les indices dans le tableau
 	 * A utiliser par exemple si vous avez ajouter un nouvelle indice  la bdd
 	 */
 	public void rafraichirListeIndices()
 	{
 		int nombreLignes = tableIndices.getRowCount();
 		
 		for(int i = nombreLignes - 1; i >= 0; i--)
 		{
 			modeleTableIndice.removeRow(i);
 		}
 		
 		tableIndices = historique(tableIndices, null);
 	}
 	
 	/**
 	 * Petite fonction qui retourne les donnes de la ligne du tableau point par la sourie
 	 * @param _table Fournir un tableau de type JTable
 	 * @param _indexLigne Indexe de la ligne
 	 * @param _indexColonne Index de la colonne
 	 * @return Le contenu  la ligne et colonne specifi en paramtre
 	 */
 	public Object obtenirDonneesLigneTableau(JTable _table, int _indexLigne, int _indexColonne)
 	{
 		  return _table.getModel().getValueAt(_indexLigne, _indexColonne);
 	}  
 	
 	/**
 	 * Fonction qui prend le contenu de la ligne et colonne point par la sourie
 	 * @param _table Fourir le tableau de type JTable
 	 * @return Un objet generique  caster comme vous voulez (string, int, ...)
 	 */
 	public Object contenuCelluleTableau(JTable _table)
 	{
 		// Variables locale
 		Object cellule = null;
 		int ligne = _table.getSelectedRow();
 		int colonne = _table.getSelectedColumn();
 		
 		// Vrifier si on a une ligne et une colonne point
 		if (ligne != -1 & colonne != -1)
 		{
 			cellule = _table.getValueAt(ligne,colonne);
 		}
 		
 		// Retourner le contenu de la cellule
 		return cellule;
 	}
 	
 	/**
 	 * Fonction qui prend le contenu de la 1re colonne  la 1re ligne du tableau
 	 * @param _table Fourir le tableau de type JTable
 	 * @param _indexColonne A quelle colonne le contenu doit etre piocher (par dfaut mettre 0 !!!)
 	 * @return Un objet generique  caster comme vous voulez (string, int, ...)
 	 */
 	public Object contenuPremiereCelluleTableau(JTable _table, int _indexColonne)
 	{
 		// Variables locale
 		Object cellule = null;
 		int ligne = _table.getSelectedRow();
 		int colonne = _indexColonne;
 		
 		// Vrifier si on a une ligne point
 		if (ligne != -1)
 		{
 			cellule = _table.getValueAt(ligne, colonne);
 		}
 		return cellule;
 	}
 	
 	/**
 	 * Afficher l'histoirique des indices
 	 * Si login non null, on affiche les indices du joeur en question
 	 * @param _table Composant JTable ou seront affichs les indices
 	 * @param _login Identifiant du joueur
 	 * @return Un Jtable remplis de donnes
 	 */
 	public final JTable historique(JTable _table, String _login)
 	{
 		// Variables locales
 		ResultSet rs;
 		JTable tableLocal = _table;
 		String login = _login;
 		String sqlJoint = " ";
 		
 		if (login != null)
 		{
 			sqlJoint = " AND perso.login = '" + login + "' ";
 		}
 		
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// Dfinition des colonnes de la table
 			String[] tableColumnsName = { "N", "Joueur", "Titre" };
 			
 			DefaultTableModel tableModele = (DefaultTableModel) tableLocal.getModel();
 			tableModele.setColumnIdentifiers(tableColumnsName);
 			
 			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModele);
 			tableLocal.setRowSorter(sorter);
 			
 			// Parser les donnes de la requte dans un ResultSet
 			rs = bdd.executerRequete("" +
 					" SELECT ind.id, perso.login, ind.titre " +
 					" FROM indice ind, indice_relation indr " +
 					" LEFT JOIN personnage perso ON indr.ref_perso = perso.id " +
 					" WHERE ind.id = indr.ref_indice " +
 					sqlJoint + 
 					" ORDER BY ind.importance DESC "
 			);
 
 			ResultSetMetaData rsMetaData = rs.getMetaData();
 			
 			// Rcuperer le nombre de colonnes
 			int numeroColonne = rsMetaData.getColumnCount();
 			
 			// Boucle  travers le ResultSet et le transfert dans le modle
 			while (rs.next()) 
 			{
 				Object[] objet = new Object[numeroColonne];
 
 				for (int i = 0; i < numeroColonne; i++) 
 				{
 					objet[i] = rs.getObject(i + 1);
 				}
 				
 				// Ajouter une ligne dans le JTable avec les donnes du l'array objet
 				tableModele.addRow(objet);
 			}
 			
 			tableLocal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 			
 			// Dfinir le modle du tableau
 			tableLocal.setModel(tableModele);
 			
 			// Dfinir la taille des colonnes
 		    TableColumn tableColonne = tableIndices.getColumnModel().getColumn(0);        
 		    tableColonne.setPreferredWidth(50);
 		    
 		    TableColumn tableColonne2 = tableIndices.getColumnModel().getColumn(2);        
 		    tableColonne2.setPreferredWidth(250);
 			
 			return tableLocal;
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Procdure qui supprimer un indice selectionner dans le tableau
 	 */
 	private boolean supprimerIndice()
 	{
 		// Variables locale
 		int idIndice = getIdIndice();
 		
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// Supprimer l'indice de la bdd
 			bdd.executerInstruction("DELETE FROM indice " +
 					" WHERE id = " + idIndice
 			);
 			
 			// Supprimer les relations personnage li a l'indice
 			bdd.executerInstruction("DELETE FROM indice_relation " +
 					" WHERE ref_indice = " + idIndice
 			);
 		} 
 		catch (SQLException sqe) 
 		{
 			System.out.println("Erreur base de donnes: " + sqe.getMessage());
 			return false;
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		
 		// Fermer la connexion  la base de donnes
 		bdd.fermerConnexion();
 		
 		// Actualiser la liste des indices
 		rafraichirListeIndices();
 		
 		// R.A.Z des composants du formulaire
 		effacerFormulaire();
 		
 		// Si on arrive ici, cela signifie qu'on a pas eut de problemes
 		return true;
 	}
 	
 	/**
 	 * Supprimer tous les indices du joueur
 	 * @param _idJoueur Identifiant du joueur
 	 * @return Confirmation que tout est ok
 	 */
 	public boolean supprimerIndicesJoueur(int _idJoueur)
 	{
 		int refIndiceTemp = -1;
 		int cptIndices = 0;
 		
 		try 
 		{
 			// Ouvrir une nouvelle connexion  la bdd
 			bdd = new BaseDeDonnees(coeurServeur);
 			
 			while (refIndiceTemp != 0) 
 			{
 				// On recupre l'id de l'indice dans la relation selon le personnage
 				refIndiceTemp = bdd.executerRequete("SELECT ref_indice FROM indice_relation WHERE ref_perso = " + _idJoueur).getInt("ref_indice");
 			
 				// Supprimer les relations personnage li a l'indice
 				bdd.executerInstruction("DELETE FROM indice_relation " +
 						" WHERE ref_indice = " + refIndiceTemp +
 						" AND ref_perso = " + _idJoueur
 				);
 				
 				// Voir si l'indice est encore utilis par quelqu'un
 				cptIndices = bdd.executerRequete("SELECT count(id) cpt FROM indice_relation WHERE ref_indice = " + refIndiceTemp).getInt("cpt");
 				if (cptIndices == 0)
 				{
 					// Supprimer l'indice
 					bdd.executerInstruction("DELETE FROM indice " +
 							" WHERE id = " + refIndiceTemp
 					);
 				}
 			}
 			
 			// Fermer la connexion  la base de donnes
 			bdd.fermerConnexion();
 		} 
 		catch (Exception e) 
 		{
 			e.getMessage();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private void tempsContreIndice(String _personnage)
 	{
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// recuperation du JTextField
 			int champChronoInt = Integer.decode(champHeure.getText())*3600 + Integer.decode(champMin.getText())*60 + Integer.decode(champSec.getText());
 			
 			// Recuperation du temps actuel du joueur
 			int ancienTempsChrono = bdd.executerRequete("SELECT temps FROM personnage WHERE login = \"" + _personnage + "\" ").getInt("temps");
 			
 			// Dfinir le nouveau temps
 			int nouveauTempsChrono = ancienTempsChrono - champChronoInt;
 			
 			// Mettre a jours la base de donnes
 			bdd.executerInstruction("UPDATE personnage SET " +
 					" temps = " + nouveauTempsChrono + " " +
 					" WHERE login = \"" + _personnage + "\" "
 			);
 		} 
 		catch (Exception e) 
 		{
 			e.getStackTrace();
 		}
 		
 		// Fermer la connexion  la base de donnes
 		bdd.fermerConnexion();
 	}
 	
 	/**
 	 * Procdure qui efface les donnes saisies dans le formulaires
 	 */
 	public void effacerFormulaire()
 	{
 		// Si cette case est choch, cela signifie que le client veux se souvenir
 		// du contenu de l'indice car, il veux l'atribuer a quelqu'un d'autre
 		if(!checkMemo.isSelected())
 		{
 			champTitre.setText(null);
 			zoneContenu.setText(null);
 			choixPersonnage.setSelectedIndex(0);
 			champNomPerso.setText(null);
 			champImageLien.setText(null);
 			choixImportance.setSelectedIndex(0);
 			champHeure.setText(null);
 			champMin.setText(null);
 			champSec.setText(null);
 			tableIndices.getSelectionModel().clearSelection();
 			setLoginJoueur(null);
 			champTitre.requestFocusInWindow();
 		}
 		
 		tableIndices.getSelectionModel().clearSelection();
 		setIdIndice(-1);
 		btnModifier.setEnabled(false);
 	}
 
 	
 // ----- Getters & Setters ----- //
 	
 	/**
 	 * Getter du panel gestion des indices
 	 * @return Un composant JPanel
 	 */
 	public JPanel getPanelGestionIndices() 
 	{
 		return panelGestionIndices;
 	}
 
 	/**
 	 * Setter du panel gestion des indices
 	 * @param panelGestionIndices Un composant JPanel
 	 */
 	public void setPanelGestionIndices(JPanel _panelGestionIndices) 
 	{
 		this.panelGestionIndices = _panelGestionIndices;
 	}
 
 	/**
 	 * Getter de l'id de l'indice en mmoire
 	 * @return Le numro d'indice
 	 */
 	public int getIdIndice() 
 	{
 		return idIndice;
 	}
 
 	/**
 	 * Setter pour dfinir un id d'indice en mmoire
 	 * @param _idIndice Le numro d'indice
 	 */
 	public void setIdIndice(int _idIndice) 
 	{
 		this.idIndice = _idIndice;
 	}
 
 
 	/**
 	 * Getter du login du joueur
 	 * @return Un login du joueur
 	 */
 	public String getLoginJoueur() 
 	{
 		return loginJoueur;
 	}
 
 	/**
 	 * Setter pour dfinir le login du joueur
 	 * @param _loginJoueur Un login du joueur
 	 */
 	public void setLoginJoueur(String _loginJoueur) 
 	{
 		this.loginJoueur = _loginJoueur;
 	}
 }
