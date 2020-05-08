 package fr.mercredymurderparty.ihm.composants;
 
 import javax.swing.JPanel;
 import net.miginfocom.swing.MigLayout;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 
 import fr.mercredymurderparty.ihm.fenetres.FenetreAdmin;
 import fr.mercredymurderparty.outil.BaseDeDonnees;
 import fr.mercredymurderparty.serveur.CoeurServeur;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JButton;
 import javax.swing.SpringLayout;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JTextArea;
 
 @SuppressWarnings("serial")
 public class ComposantGestionAlerte extends JPanel 
 {
 	
 // ----- ATTRIBUTS ----- //
 	
 	// ----- COMPOSANTS IHM ----- //
 	private JTextField champTitre, champRef, champTemps;
 	private JTextArea champContenu;
 	private JTable tableAlerte;
 	private DefaultTableModel modeleTableAlertes;
 	private JButton btnValider;
 	
 	// ----- CLASSES ----- //
 	private BaseDeDonnees bdd;
 	private CoeurServeur coeurServeur;
 
 	// ----- VARIABLES ----- //
 	private int idAlerte = -1;
 
 	
 
 // ----- CONSTRUCTEUR ----- //
 	
 	public ComposantGestionAlerte(FenetreAdmin _fenetre, final SpringLayout _springLayout, CoeurServeur _coeurServeur) 
 	{
 		coeurServeur = _coeurServeur;
 		setLayout(new MigLayout("", "[281.00][5.00][grow]", "[281.00,grow]"));
 		
 		JPanel pnlFormulaire = new JPanel();
 		
 		_springLayout.putConstraint(SpringLayout.NORTH, this, -200, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.SOUTH, this, 300, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.WEST, this, -350, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.EAST, this, 400, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_fenetre.getContentPane().add(this);		
 	
 		add(pnlFormulaire, "cell 0 0,grow");
 		
 		//_fenetre.getContentPane().add(pnlFormulaire, "cell 0 0,grow");
 		pnlFormulaire.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("left:max(60dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(120dlu;min):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(5dlu;default)"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(48dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JButton btnNouveau = new JButton("Nouvelle alerte");
 		btnNouveau.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				initChamp();
 			}
 		});
 		
 		JLabel lblAjouterModifier = new JLabel("Formulaire pour Ajouter / Modifier l'alerte :");
 		pnlFormulaire.add(lblAjouterModifier, "2, 2, 5, 1");
 		pnlFormulaire.add(btnNouveau, "4, 4");
 		
 		JLabel lblTitre = new JLabel("Titre :");
 		pnlFormulaire.add(lblTitre, "2, 6, left, default");
 		
 		champTitre = new JTextField();
 		pnlFormulaire.add(champTitre, "4, 6, fill, default");
 		champTitre.setColumns(10);
 		
 		JLabel lblContenu = new JLabel("Contenu :");
 		pnlFormulaire.add(lblContenu, "2, 8, left, default");
 		
 		champContenu = new JTextArea();
 		pnlFormulaire.add(champContenu, "4, 8, fill, fill");
 		
 		JLabel lblRef = new JLabel("Ref :");
 		pnlFormulaire.add(lblRef, "2, 10, left, default");
 		
 		champRef = new JTextField();
 		pnlFormulaire.add(champRef, "4, 10, fill, default");
 		champRef.setColumns(10);
 		
 		JLabel lblDureDuChrono = new JLabel("Dur\u00E9e du chrono :");
 		pnlFormulaire.add(lblDureDuChrono, "2, 12, left, default");
 		
 		btnValider = new JButton("Cr\u00E9er l'alerte");
 		btnValider.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				// Vrifier si les champs sont biens remplis
 				if ((champTitre.getText().length() > 0))
 				{
 					if (idAlerte == -1)
 					{
 						if (ajouterAlerte())
 						{
 							JOptionPane.showMessageDialog(ComposantGestionAlerte.this, "L'alerte a t crer avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 						}
 						else
 						{
 							JOptionPane.showMessageDialog(ComposantGestionAlerte.this, "Un problme empche de crer l'alerte :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 						}
 					}
 					else
 					{
 						if (modifierAlerte())
 						{
 							JOptionPane.showMessageDialog(ComposantGestionAlerte.this, "L'alerte a t modif avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 						}
 						else
 						{
 							JOptionPane.showMessageDialog(ComposantGestionAlerte.this, "Un problme empche de modifier l'alerte :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 						}
 					}
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(ComposantGestionAlerte.this, "Un ou plusieurs champs n'ont pas ts remplis ou ne correspondent pas aux critres requis.", "Formulaire non valide", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		
 		champTemps = new JTextField();
 		pnlFormulaire.add(champTemps, "4, 12, fill, default");
 		champTemps.setColumns(10);
 		pnlFormulaire.add(btnValider, "4, 16");
 		
 		JPanel pnlTableau = new JPanel();
 		add(pnlTableau, "cell 2 0,grow");
 		pnlTableau.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(135dlu;min)"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(170dlu;min)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblHistoriqueDesAlertes = new JLabel("Historique des alertes :");
 		pnlTableau.add(lblHistoriqueDesAlertes, "2, 2");
 		
 		JScrollPane scrollPane = new JScrollPane();
 		pnlTableau.add(scrollPane, "2, 4, fill, fill");
 		
 		modeleTableAlertes = new DefaultTableModel();
 		tableAlerte = new JTable(modeleTableAlertes);
 		tableAlerte.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseClicked(MouseEvent arg0) 
 			{
 				idAlerte = (Integer) contenuPremiereCelluleTableau(tableAlerte, 0);
 				recupererContenuAlerte(idAlerte);
 			}
 		});
 		scrollPane.setViewportView(tableAlerte);
 		
 		JButton btnSupprimerAlerte = new JButton("Supprimer l'alerte");
 		btnSupprimerAlerte.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) 
 			{
 				idAlerte = (Integer) contenuPremiereCelluleTableau(tableAlerte, 0);
 				if (supprimerAlerte(idAlerte))
 				{
 					JOptionPane.showMessageDialog(ComposantGestionAlerte.this, "L'alerte a t supprimer avec succs !", "Message de confirmation", JOptionPane.INFORMATION_MESSAGE);
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(ComposantGestionAlerte.this, "Un problme empche la suppression de l'alerte :(", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		pnlTableau.add(btnSupprimerAlerte, "2, 6");
 	}
 	
 	
 // ----- METHODES ----- //
 	
 	/**
 	 * Fonction qui supprime une alerte
 	 * @param idAlerte L'id de l'alerte a supprimer
 	 * @return Un booleen qui indique si traitement reussit ou pas
 	 */
 	public boolean supprimerAlerte(int idAlerte) 
 	{
 		try
 		{
 			// connexion base de donnees
 			bdd = new BaseDeDonnees(coeurServeur);
 			
			bdd.executerInstruction("DELETE FROM alerte WHERE rowid = " + idAlerte + "");
 			
 			// deconnexion base de donnees
 			bdd.fermerConnexion();
 			
 			// Rafraichir la liste des joueurs
 			rafraichirListeAlerte();
 			
 			// R.A.Z le formulaire et le mode d'edition
 			initChamp();
 		}
 		catch (SQLException esq)
 		{
 			esq.printStackTrace();
 			return false;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Fonction qui ajoute une alerte
 	 * @return Booleen pour indiquer si l'opration a reussit
 	 */
 	public boolean ajouterAlerte()
 	{
 		// variable qui contiendra la valeur du JtextField mais en int
 		int champChronoInt; 
 		
 		try
 		{	
 			// recuperation du JTextField
 			champChronoInt = Integer.parseInt(champTemps.getText());
 			
 			// connexion a la base de donnes
 			bdd = new BaseDeDonnees(coeurServeur);
 			
 			// insertion dans la base de donnes des valeurs des champs
 			bdd.executerInstruction("INSERT INTO alerte (titre, contenu, ref, temps) VALUES ( " +
 									"\"" + champTitre.getText() +"\", " +
 									"\"" + champContenu.getText()  +"\", " +
 									"\"" + champRef.getText()         +"\", " +
 									"\"" + champChronoInt       +"\")");
 			
 			// deconnexion base de donnes
 			bdd.fermerConnexion(); 
 			
 			// Rafraichir la liste des joueurs
 			rafraichirListeAlerte();
 			
 			// R.A.Z le formulaire et le mode d'edition
 			initChamp();
 		} 
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false; 
 		}
 		catch(NumberFormatException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Afficher la liste des alertes dans un JTable
 	 * @param _table Composant JTable ou seront affichs les alertes
 	 * @param _login Filtrer les alertes selon un login (si null alors on affiche toutes les alertes)
 	 * @return La liste des alertes
 	 */
 	public final JTable listeAlerte(JTable _table, String _login)
 	{
 		// Variables locales
 		ResultSet rs;
 		JTable tableLocal = _table;
 		
 		bdd = new BaseDeDonnees(coeurServeur);
 		
 		try 
 		{
 			// Dfinition des colonnes de la table
 			String[] tableColumnsName = { "N", "Alerte"};
 			
 			DefaultTableModel tableModele = (DefaultTableModel) tableLocal.getModel();
 			tableModele.setColumnIdentifiers(tableColumnsName);
 			
 			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModele);
 			tableLocal.setRowSorter(sorter);
 			
 			// Parser les donnes de la requte dans un ResultSet
 			rs = bdd.executerRequete("" +
					" SELECT rowid, titre FROM alerte "
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
 		    TableColumn tableColonne = tableAlerte.getColumnModel().getColumn(0);        
 		    tableColonne.setPreferredWidth(50);
 		    
 		    TableColumn tableColonne2 = tableAlerte.getColumnModel().getColumn(1);        
 		    tableColonne2.setPreferredWidth(100);
 			
 			return tableLocal;
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Recuperer le contenu d'une alerte et l'afficher dans les camps
 	 * @param _idAlerte Numro de l'alerte
 	 */
 	public void recupererContenuAlerte(int _idAlerte)
 	{
 		if (_idAlerte != -1)
 		{
 			// Ouvrir une nouvelle connexion  la bdd
 			bdd = new BaseDeDonnees(coeurServeur);
 			
 			try 
 			{
 				// Parser les donnes de la requte dans un ResultSet
 				ResultSet rs = bdd.executerRequete("" +
 						" SELECT titre, contenu, ref, temps " +
						" FROM alerte WHERE rowid  = " + _idAlerte + " "
 				);
 				
 				// Afficher les donnes du ResultSet
 				while (rs.next()) 
 				{
 					champTitre.setText(rs.getString("titre"));
 					champContenu.setText(rs.getString("contenu"));
 					champRef.setText(rs.getString("ref"));
 					champTemps.setText(rs.getString("temps"));
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
 				btnValider.setText("Modifier l'alerte");
 			}
 			
 			// Fermer la connexion  la bdd
 			bdd.fermerConnexion();	
 		}
 	}
 	
 	/**
 	 * Procdure qui modifie une alerte existant dans la bdd
 	 */
 	public boolean modifierAlerte()
 	{
 		 // variable qui contiendra la valeur du JtextField mais en int
 		int champChronoInt;
 		
 		champTemps.setEditable(false);
 		
 		try 
 		{
 			// Ouvrir une nouvelle connexion  la bdd
 			bdd = new BaseDeDonnees(coeurServeur); 
 			
 			// Recuperation du JTextField avec le temps
 			champChronoInt = Integer.parseInt(champTemps.getText());
 			
 			// Mettre a jours le joueur
 			bdd.executerInstruction("UPDATE alerte SET " +
 					" titre = "   + "'" + champTitre.getText() + "'," +
 					" contenu = " + "'" + champContenu.getText()  + "'," +
 					" ref = "     + "'" + champRef.getText()         + "'," +
 					" temps = "   + "" + champChronoInt + " " +
					" WHERE rowid = " + "" + idAlerte  + "");
 			
 			// Fermer la connexion  la base de donnes
 			bdd.fermerConnexion();
 			
 			// Actualiser la liste des joueurs
 			rafraichirListeAlerte();
 			
 			// R.A.Z des composants du formulaire
 			initChamp();
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
 		
 		// Si on arrive ici, c'est qu'on a pas eu de problmes
 		return true;
 	}
 
 	/**
 	 * La mthode estVisible permet d'afficher ou non le conteneur de paramtres
 	 * @param _visible : "boolean" affiche ou masque ce conteneur
 	 */
 	final public void estVisible(boolean _visible)
 	{
 		if (_visible)
 			this.setEnabled(true);
 		else
 			this.setEnabled(false);
 		
 		this.setVisible(_visible);
 	}
 	
 	/**
 	 * Procdure qui rafraichit les alertes dans le tableau
 	 * A utiliser par exemple si vous avez ajouter une nouvelle alerte  la bdd
 	 */
 	public void rafraichirListeAlerte()
 	{
 		int nombreLignes = tableAlerte.getRowCount();
 		
 		for(int i = nombreLignes - 1; i >= 0; i--)
 		{
 			modeleTableAlertes.removeRow(i);
 		}
 		
 		tableAlerte = listeAlerte(tableAlerte, null);
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
 	 * Proccure qui initialise les champs et certains paramtres
 	 */
 	final public void initChamp()
 	{
 		btnValider.setText("Crer l'alerte");
 		
 		champTemps.setEditable(true);
 		
 		champTitre.setText("");
 		champContenu.setText("");
 		champRef.setText("");
 		//champPrenom.setText("");
 		champTemps.setText("");
 		
 		champTitre.requestFocusInWindow();
 		tableAlerte.getSelectionModel().clearSelection();
 		
 		idAlerte = -1;
 	}
 
 	
 // ----- Getters & Setters ----- //
 	
 	/**
 	 * Getter qui recupre la table des alertes
 	 * @return Composant JTable
 	 */
 	public JTable getTableAlerte()
 	{
 		return tableAlerte;
 	}
 
 	/**
 	 * Setter qui dfinit la table des joueurs
 	 * @param tableAlerte Composant JTable
 	 */
 	public void setTableJoueurs(JTable tableAlerte)
 	{
 		this.tableAlerte = tableAlerte;
 	}
 }
