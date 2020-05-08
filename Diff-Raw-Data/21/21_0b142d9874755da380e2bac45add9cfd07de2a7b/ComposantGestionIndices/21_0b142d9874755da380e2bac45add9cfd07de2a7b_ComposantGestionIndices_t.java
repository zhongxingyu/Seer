 package fr.mercredymurderparty.ihm.composants;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 
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
 import fr.mercredymurderparty.outil.Fonctions;
 import fr.mercredymurderparty.serveur.Indice;
 
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
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 @SuppressWarnings("serial")
 public class ComposantGestionIndices extends JPanel
 {
 	
 // ----- ATTRIBUTS ----- //
 	
 	// ----- INTERFACE GRAPHIQUE ----- //
 	private JPanel panelGestionIndices;
 	private File image;
 	private JTextField champTitre;
 	private JTextField champNomPerso;
 	private JTextField champTemps;
 	private JTextField champImageLien;
 	private JTextArea zoneContenu;
	private JComboBox/*<String>*/ choixImportance;
 	private JCheckBox checkGratuit;
	private JComboBox/*<String>*/ choixPersonnage;
 	private JCheckBox checkTousLesJoueurs;
 	private DefaultTableModel modeleTableIndice;
 	private JTable tableIndices;
 	private JPanel pnlFormulaire;
 	private JButton btnValider;
 	
 	// ----- CLASSES ----- //
 	private BaseDeDonnees bdd;
 	private Fonctions fct;
 	private Indice ind;
 	
 	// ----- VARIABLES ----- //
 	private boolean tempsPenalite = false;
 	private boolean tempsTousJoueurs = false;			
 	private boolean verrouillerComposant = false;
 	private int editIdIndice = -1;
 	private String editLoginJoueur = null;
 	
 // ----- CONSTRUCTEUR ----- //
 	
 	public ComposantGestionIndices(FenetreAdmin _fenetre, final SpringLayout _springLayout)
 	{
 		panelGestionIndices = new JPanel();
 		_springLayout.putConstraint(SpringLayout.NORTH, panelGestionIndices, -200, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.SOUTH, panelGestionIndices, 300, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.WEST, panelGestionIndices, -350, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.EAST, panelGestionIndices, 400, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_fenetre.getContentPane().add(panelGestionIndices);
 		panelGestionIndices.setLayout(new MigLayout("", "[][][grow]", "[335.00,top]"));
 		this.setLayout(_springLayout);
 		SpringLayout springLayout = new SpringLayout();
 		setLayout(springLayout);
 		
 		pnlFormulaire = new JPanel();
 		springLayout.putConstraint(SpringLayout.NORTH, pnlFormulaire, 10, SpringLayout.NORTH, this);
 		springLayout.putConstraint(SpringLayout.WEST, pnlFormulaire, 10, SpringLayout.WEST, this);
 		springLayout.putConstraint(SpringLayout.SOUTH, pnlFormulaire, 394, SpringLayout.NORTH, this);
 		springLayout.putConstraint(SpringLayout.EAST, pnlFormulaire, 315, SpringLayout.WEST, this);
 		panelGestionIndices.add(pnlFormulaire, "cell 0 0");
 		pnlFormulaire.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(48dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(25dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(25dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(25dlu;default)"),
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
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblFormulaireDeSaisie = new JLabel("Ajouter / modifier un indice :");
 		lblFormulaireDeSaisie.setFont(new Font("Tahoma", Font.BOLD, 11));
 		pnlFormulaire.add(lblFormulaireDeSaisie, "2, 2, 8, 1");
 		
 		JLabel lblTitre = new JLabel("Titre :");
 		pnlFormulaire.add(lblTitre, "2, 4");
 		
 		champTitre = new JTextField();
 		pnlFormulaire.add(champTitre, "4, 4, 5, 1, fill, default");
 		champTitre.setColumns(10);
 		
 		JLabel lblContenu = new JLabel("Contenu :");
 		pnlFormulaire.add(lblContenu, "2, 6");
 		
 		zoneContenu = new JTextArea();
 		pnlFormulaire.add(zoneContenu, "4, 6, 5, 1, fill, fill");
 		
 		JLabel lblPersonnage = new JLabel("Personnage :");
 		pnlFormulaire.add(lblPersonnage, "2, 8");
 		
		choixPersonnage = new JComboBox/*<String>*/();
 		choixPersonnage.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				if (verrouillerComposant == false)
 				{
 					String[] identiteJoueur = new String[2];
 					identiteJoueur = identiteJoueur((String) choixPersonnage.getSelectedItem());
 					if (identiteJoueur[0] != null)
 					{
 						champNomPerso.setText(identiteJoueur[0] + " " + identiteJoueur[1]);
 						
 						int nombreLignes = tableIndices.getRowCount();
 						
 						for(int i = nombreLignes - 1; i >= 0; i--)
 						{
 							modeleTableIndice.removeRow(i);
 						}
 						
 						historique(tableIndices, (String) choixPersonnage.getSelectedItem());
 					}
 					else
 					{
 						rafraichirListeIndices();
 					}
 				}
 				
 				btnValider.setText("Ajouter");
 			}
 		});
 		pnlFormulaire.add(choixPersonnage, "4, 8, 5, 1, fill, default");
 		
 		champNomPerso = new JTextField();
 		champNomPerso.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		champNomPerso.setEditable(false);
 		pnlFormulaire.add(champNomPerso, "4, 10, 3, 1, fill, default");
 		champNomPerso.setColumns(10);
 		
 		JLabel lblImage = new JLabel("Image :");
 		pnlFormulaire.add(lblImage, "2, 12");
 		
 		champImageLien = new JTextField();
 		pnlFormulaire.add(champImageLien, "4, 12, 3, 1, fill, default");
 		champImageLien.setColumns(10);
 		
 		JButton btnChoisirImage = new JButton("Choisir");
 		btnChoisirImage.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				choisirImage();
 			}
 		});
 		pnlFormulaire.add(btnChoisirImage, "8, 12");
 		
 		JLabel lblPriorite = new JLabel("Importance :");
 		pnlFormulaire.add(lblPriorite, "2, 14");
 		
		choixImportance = new JComboBox/*<String>*/();
		choixImportance.setModel(new DefaultComboBoxModel/*<String>*/(new String[] {"Peu important", "Important", "Tr\u00E8s important"}));
 		pnlFormulaire.add(choixImportance, "4, 14, 5, 1, fill, default");
 		
 		JLabel lblTemps = new JLabel("P\u00E9nalit\u00E9 :");
 		pnlFormulaire.add(lblTemps, "2, 16");
 		
 		champTemps = new JTextField();
 		champTemps.setToolTipText("Saisir le temps au format: mm:ss, exemple: 01:30");
 		pnlFormulaire.add(champTemps, "4, 16, 3, 1, fill, default");
 		champTemps.setColumns(10);
 		
 		JLabel lblSecondes = new JLabel("min : sec");
 		pnlFormulaire.add(lblSecondes, "8, 16");
 		
 		JLabel lblOptions = new JLabel("Options:");
 		pnlFormulaire.add(lblOptions, "2, 18");
 		
 		checkGratuit = new JCheckBox("Indice gratuit");
 		checkGratuit.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				if (checkGratuit.isSelected())
 				{
 					tempsPenalite = true;
 				}
 				else
 				{
 					tempsPenalite = false;
 				}
 			}
 		});
 		pnlFormulaire.add(checkGratuit, "4, 18, 3, 1");
 		
 		checkTousLesJoueurs = new JCheckBox("Indice pour tous les joueurs");
 		checkTousLesJoueurs.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				if (checkTousLesJoueurs.isSelected())
 				{
 					tempsTousJoueurs = true;
 					champTemps.setText("00:00");
 					champTemps.setEditable(false);
 					choixPersonnage.setSelectedIndex(0);
 					choixPersonnage.setEnabled(false);
 					champNomPerso.setText("Tous les joueurs !");
 				}
 				else
 				{
 					tempsTousJoueurs = false;
 					champTemps.setText("00:00");
 					champTemps.setEditable(true);
 					choixPersonnage.setEnabled(true);
 					champNomPerso.setText(null);
 				}
 			}
 		});
 		pnlFormulaire.add(checkTousLesJoueurs, "4, 20, 3, 1");
 		
 		btnValider = new JButton("Ajouter");
 		btnValider.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				// Vrifier si les champs sont biens remplis
 				if ((choixPersonnage.getSelectedItem() != null || tempsTousJoueurs == true) && champTitre.getText() != null && zoneContenu.getText() != null)
 				{
 					if (editIdIndice == -1)
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
 						if (modifierIndice())
 						{
 							JOptionPane.showMessageDialog(ComposantGestionIndices.this, "L'indice a t modif avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 						}
 						else
 						{
 							JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un problme empche de modifier l'indice :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 						}
 					}
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(ComposantGestionIndices.this, "Un ou plusieurs champs n'ont pas ts remplis ou ne correspondent pas aux critres requis.\nChamps obligatoires: titre, contenu, personnage (a dfaut coch sur la case 'indice pour tous les joueurs').", "Formulaire non valide", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		
 		JButton btnEffacer = new JButton("Effacer");
 		btnEffacer.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				effacerFormulaire();
 			}
 		});
 		pnlFormulaire.add(btnEffacer, "6, 24");
 		pnlFormulaire.add(btnValider, "8, 24");
 		
 		JPanel pnlIndices = new JPanel();
 		panelGestionIndices.add(pnlIndices, "cell 2 0,grow");
 		pnlIndices.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				RowSpec.decode("max(15dlu;default)"),
 				RowSpec.decode("max(130dlu;min)"),
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
 		pnlIndices.add(lblHistoriqueDesIndices, "2, 1");
 		
 		JScrollPane spIndices = new JScrollPane();
 		pnlIndices.add(spIndices, "2, 2, 1, 5, fill, fill");
 		
 		// Charger les personnages
 		chargerPersonnages();
 		
 		modeleTableIndice = new DefaultTableModel();
 		tableIndices = new JTable(modeleTableIndice);
 		tableIndices.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseClicked(MouseEvent e) 
 			{
 				editIdIndice = (Integer) contenuPremiereCelluleTableau(tableIndices, 0);
 				editLoginJoueur = (String) contenuPremiereCelluleTableau(tableIndices, 1);
 				recupererContenuIndice(editIdIndice, editLoginJoueur);
 			}
 		});
 		spIndices.setViewportView(tableIndices);
 		
 		JLabel lblChoisirUnIndice = new JLabel("Choisir un indice dans l'historique pour l'\u00E9diter ou supprimer");
 		lblChoisirUnIndice.setFont(new Font("Tahoma", Font.ITALIC, 11));
 		pnlIndices.add(lblChoisirUnIndice, "2, 8");
 		
 		JButton btnSupprimerIndice = new JButton("Supprimer l'indice");
 		btnSupprimerIndice.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
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
 		});
 		pnlIndices.add(btnSupprimerIndice, "2, 12");
 		
 		rafraichirListeIndices();
 	}
 	
 	
 // ----- METHODES ----- //
 	
 	/**
 	 * La mthode estVisible permet d'afficher ou non le conteneur de cration joueur
 	 * @param _visible : "boolean" affiche ou masque ce conteneur
 	 */
 	final public void estVisible(boolean _visible)
 	{
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
 		
 		bdd = new BaseDeDonnees();
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
 	private void chargerPersonnages()
 	{
 		choixPersonnage.addItem(null);
 		
 		bdd = new BaseDeDonnees();
 		try 
 		{
 		    ResultSet rs = bdd.executerRequete("SELECT login FROM personnage");
 		    while (rs.next()) 
 		    {
 		      choixPersonnage.addItem(rs.getString("login"));
 		    }
 		    rs.close();
 		} 
 		catch (Exception e)
 		{
 			System.out.println(e.getMessage());
 		}
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
 	 * Procdure qui copie une image d'un rpertoire  un autre
 	 * @param _imageSource Repertoire source de l'image
 	 * @param _imageDestination Repertoire destination de l'image
 	 */
 	private void copierImage(String _imageSource, String _imageDestination) 
 	{
 		try 
 		{
 			// Fichier source et buffer en entre
 			File fichierSource = new File(_imageSource);
 			BufferedInputStream bufEntree = new BufferedInputStream(new FileInputStream(fichierSource), 4096);
 			
 			// Fichier destination et buffer en sortie
 			File fichierDestination = new File(_imageDestination);
 			BufferedOutputStream bufSortie = new BufferedOutputStream(new FileOutputStream(fichierDestination), 4096);
 			
 			int leChar;
 			while ((leChar = bufEntree.read()) != -1) 
 			{
 				bufSortie.write(leChar);
 			}
 			
 			// Femer les buffers
 			bufSortie.close();
 			bufEntree.close();
 		} 
 		catch (Exception e) 
 		{
 			e.getStackTrace();
 		}
 	}
 	
 	/**
 	 * Procdure qui recupere le contenu d'un indice afin de l'diter ou le supprimer
 	 * @param _idIndice Numro d'identifiant de l'indice (pris depuis un jTable par exemple)
 	 */
 	public void recupererContenuIndice(int _idIndice, String _loginJoueur)
 	{
 		// Variables locale
 		int idIndice = editIdIndice;
 		String loginTemp = null;
 		
 		// Verrouille certains composants qui risquent de rentrer en conflits
 		verrouillerComposant = true;
 		
 		if (idIndice != -1)
 		{
 			// Ouvrir une nouvelle connexion  la bdd
 			bdd = new BaseDeDonnees();
 			
 			try 
 			{
 				// recuperer l'id du joueur
 				int idJoueur = -1;
 				idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + _loginJoueur + "'").getInt("id");
 				
 				// Parser les donnes de la requte dans un ResultSet
 				ResultSet rs = bdd.executerRequete("" +
 						" SELECT ind.titre, ind.contenu, ind.importance, perso.login " +
 						" FROM indice ind, indice_relation indr " +
 						" LEFT JOIN personnage perso ON indr.ref_perso = perso.id " +
 						" WHERE ind.id = " + idIndice + " " +
 						" AND ind.id = indr.ref_indice" +
 						" AND  indr.ref_perso = " + idJoueur + " "
 				);
 				
 				// Afficher les donnes du ResultSet
 				while (rs.next()) 
 				{
 					// On temporise le login pour l'utiliser plus bas en dehors du ResultSet
 					loginTemp = rs.getString("login"); 
 					
 					champTitre.setText(rs.getString("titre"));
 					zoneContenu.setText(rs.getString("contenu"));
 					choixPersonnage.setSelectedItem(loginTemp);
 					choixImportance.setSelectedIndex(Integer.parseInt(rs.getString("importance")));
 				}
 				
 				// On dfinit le nom et prenom dans le champ appropri pour savoir a qui appartient le login
 				String[] identiteJoueur = new String[2];
 				identiteJoueur = identiteJoueur(loginTemp);
 				if (identiteJoueur[0] != null)
 				{
 					champNomPerso.setText(identiteJoueur[0] + " " + identiteJoueur[1]);
 				}
 				
 				// Femer le ResultSet
 				rs.close();
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
 				btnValider.setText("Modifier");
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
 		// 2 variables pour dfinir un nom de l'image: le temps en ms (dison
 		long valeurAleatoire = System.currentTimeMillis();
 		String definirNomImage = null;
 		
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees();
 		
 		try 
 		{
 			if (champImageLien.getText().length() > 0)
 			{
 				// Copier l'image dans le repertoire du jeu
 				copierImage(image.getAbsolutePath(), System.getProperty("user.dir") + File.separator + "res" + File.separator + "indices" + File.separator + valeurAleatoire + "_" + image.getName());
 				definirNomImage = valeurAleatoire + "_" + image.getName();
 			}
 			else
 			{
 				champImageLien.setText(null);
 			}
 			
 			// Ajouter un nouvel indice dans la base de donnes
 			bdd.executerInstruction("INSERT INTO indice (titre, contenu, importance, image) VALUES (" +
 					"\"" + champTitre.getText() + "\", " +
 					"\"" + zoneContenu.getText() + "\", " +
 					"" + choixImportance.getSelectedIndex()  + ", " +
 					"\"" + definirNomImage + "\" )" +
 			"");
 			
 			// On recupre le dernier id de l'indice dans la bdd
 			int idIndice = -1;
 			idIndice = bdd.executerRequete("SELECT last_insert_rowid()").getInt("last_insert_rowid()");
 			
 			if (tempsTousJoueurs == false)
 			{
 				// On recupre l'id du joueur
 				int idJoueur = -1;
 				idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + (String) choixPersonnage.getSelectedItem() + "'").getInt("id");
 				
 				// Ajouter un nouvel indice dans la base de donnes
 				bdd.executerInstruction("INSERT INTO indice_relation (ref_perso, ref_indice) VALUES (" +
 						" " + idJoueur + ", " +
 						" " + idIndice + " )" +
 				"");
 			}
 			else
 			{
 				for (int i=0; i<choixPersonnage.getItemCount(); i++)
 				{
 					if (choixPersonnage.getItemAt(i) != null)
 					{
 						// On recupre l'id du joueur
 						int idJoueur = -1;
 						idJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + (String) choixPersonnage.getItemAt(i) + "'").getInt("id");
 						
 						// Ajouter un nouvel indice dans la base de donnes
 						bdd.executerInstruction("INSERT INTO indice_relation (ref_perso, ref_indice) VALUES (" +
 								" " + idJoueur + ", " +
 								" " + idIndice + " )" +
 						"");
 					}
 
 				}
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
 		int idIndice = editIdIndice;
 		
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees();
 		
 		try 
 		{
 			// Ajouter un nouvel indice dans la base de donnes
 			bdd.executerInstruction("UPDATE indice SET " +
 					" titre = \"" + champTitre.getText() + "\", " +
 					" contenu = \"" + zoneContenu.getText() + "\", " +
 					" importance = " + choixImportance.getSelectedIndex()  +
 					" WHERE id = " + idIndice
 			);
 			
 			// recuperer l'id du joueur
 			int idNouveauJoueur = -1;
 			idNouveauJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + (String) choixPersonnage.getSelectedItem() + "'").getInt("id");
 			
 			// recuperer l'id du joueur
 			int idAncienJoueur = -1;
 			idAncienJoueur = bdd.executerRequete("SELECT id FROM personnage WHERE login = '" + editLoginJoueur + "'").getInt("id");
 			
 			// Mettre a jours les relations indices <> personnages
 			bdd.executerInstruction("UPDATE indice_relation SET " +
 					" ref_perso = " + idNouveauJoueur  +
 					" WHERE ref_indice = " + idIndice +
 					" AND ref_perso = " + idAncienJoueur
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
 		
 		// Si on arrive ici, c'est qu'on a pas eu de problmes
 		return true;
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
 		
 		bdd = new BaseDeDonnees();
 		
 		try 
 		{
 			// Dfinition des colonnes de la table
 			String[] tableColumnsName = { "N", "Joueur", "Titre" };
 			
 			DefaultTableModel tableModele = (DefaultTableModel) tableLocal.getModel();
 			tableModele.setColumnIdentifiers(tableColumnsName);
 			
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
 			
 			// Dfinir le modle du tableau
 			tableLocal.setModel(tableModele);
 			
 			// Dfinir la taille des colonnes
 		    TableColumn tableColonne = tableIndices.getColumnModel().getColumn(0);        
 		    tableColonne.setPreferredWidth(50);
 		    
 		    TableColumn tableColonne2 = tableIndices.getColumnModel().getColumn(2);        
 		    tableColonne2.setPreferredWidth(200);
 			
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
 		int idIndice = editIdIndice;
 		
 		// Ouvrir une nouvelle connexion  la bdd
 		bdd = new BaseDeDonnees();
 		
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
 	 * Procdure qui efface les donnes saisies dans le formulaires
 	 */
 	private void effacerFormulaire()
 	{
 		champTitre.setText(null);
 		zoneContenu.setText(null);
 		choixPersonnage.setSelectedIndex(0);
 		champNomPerso.setText(null);
 		champImageLien.setText(null);
 		choixImportance.setSelectedIndex(0);
 		champTemps.setText(null);
 		tableIndices.getSelectionModel().clearSelection();
 		editIdIndice = -1;
 		editLoginJoueur = null;
 		champTitre.requestFocusInWindow();
 		btnValider.setText("Ajouter");
 	}
 
 
 	public JPanel getPanelGestionIndices() {
 		return panelGestionIndices;
 	}
 
 
 	public void setPanelGestionIndices(JPanel panelGestionIndices) {
 		this.panelGestionIndices = panelGestionIndices;
 	}
 }
