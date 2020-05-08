 package mensonge.userinterface;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 
 import javax.swing.JFileChooser;
 
 import javax.swing.BorderFactory;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 
 import javax.swing.JTree;
 
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 
 import mensonge.core.DataBaseObserver;
 import mensonge.core.BaseDeDonnees.BaseDeDonnees;
 import mensonge.core.BaseDeDonnees.DBException;
 
 public class PanneauArbre extends JPanel implements DataBaseObserver
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final int TYPE_TRIE_CATEGORIE = 1;
 	private static final int TYPE_TRIE_SUJET = 2;
 
 	private BaseDeDonnees bdd = null;
 
 	private LecteurAudio lecteurAudio;
 
 	private PanneauInformationFeuille infoArbre = new PanneauInformationFeuille();
 	private DefaultMutableTreeNode racine;
 	private JTree arbre;
 	private JScrollPane scrollPane;
 
 	private JPopupMenu menuClicDroit = new JPopupMenu();// sers au clic droit
 
 	private int typeTrie = PanneauArbre.TYPE_TRIE_SUJET;
 	private File cacheDirectory;
 
 	public PanneauArbre(BaseDeDonnees bdd)
 	{
 		cacheDirectory = new File("cache");
 		if (!cacheDirectory.exists())
 		{
 			cacheDirectory.mkdir();
 		}
 		else if (cacheDirectory.exists() && !cacheDirectory.isDirectory())
 		{
 			cacheDirectory.delete();
 			cacheDirectory.mkdir();
 		}
 
 		this.setLayout(new BorderLayout());
 		this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
 		this.bdd = bdd;
 
 		this.racine = new DefaultMutableTreeNode("Sujet");
 		this.remplirArbreEnregistrementSujet();
 		this.arbre = new JTree(racine);
 		this.arbre.addMouseListener(new ClicDroit());
 		this.arbre.addMouseListener(new ClicGauche());
 
 		this.arbre.addTreeSelectionListener(new TreeSelectionListener()
 		{
 			@Override
 			public void valueChanged(TreeSelectionEvent event)
 			{
 
 				if (arbre.getLastSelectedPathComponent() != null
 						&& arbre.getLastSelectedPathComponent() instanceof Feuille)
 				{
 					infoArbre.setListeInfo(((Feuille) arbre.getLastSelectedPathComponent()).getInfo());// On informe le
 																										// panneau
 																										// d'information
 					infoArbre.repaint();// on le repaint
 					lecteurAudio.setVisible(true);
 				}
 				else
 				{
 					infoArbre.setListeInfo(null);
 					infoArbre.repaint();// on le repaint
 					lecteurAudio.setVisible(false);
 				}
 			}
 		});
 
 		this.scrollPane = new JScrollPane(arbre);
 		this.scrollPane.setPreferredSize(new Dimension(270, 300));
 		this.scrollPane.setAutoscrolls(true);
 
 		this.infoArbre.setPreferredSize(new Dimension(270, 100));
 
 		JPanel panelArbre = new JPanel(new GridLayout(0, 1));
 		panelArbre.add(this.scrollPane);
 		panelArbre.add(this.infoArbre);
 
 		this.lecteurAudio = new LecteurAudio();
 		this.lecteurAudio.setVisible(false);
 
 		this.add(panelArbre, BorderLayout.CENTER);
 		this.add(lecteurAudio, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Permet de fermer proprement ce qu'il a ouvert
 	 */
 	public void close()
 	{
 		this.lecteurAudio.close();
 	}
 
 	public void updateArbre()
 	{
 		viderNoeud(this.racine);
 		if (this.typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 		{
 			remplirArbreEnregistrementCategorie();
 		}
 		else if (this.typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 		{
 			remplirArbreEnregistrementSujet();
 		}
 		this.arbre.updateUI();
 		this.arbre.setExpandsSelectedPaths(true);
 		int nb = this.arbre.getRowCount();
 
 		for (int i = 0; i < nb; i++)
 		{
 			this.arbre.expandRow(i);
 		}
 	}
 
 	public void remplirArbreEnregistrementCategorie()
 	{
 		ResultSet rs_cat = null, rs_enr = null;
 		try
 		{
 			rs_cat = this.bdd.getListeCategorie();
 			while (rs_cat.next())
 			{
 				Branche node = new Branche(rs_cat.getString("nomcat"));
 				rs_enr = this.bdd.getListeEnregistrementCategorie(rs_cat.getInt("idcat"));
 				while (rs_enr.next())
 				{
 					Feuille f = new Feuille(rs_enr.getInt("id"), rs_enr.getString("nom"), rs_enr.getInt("duree"),
 							rs_enr.getInt("taille"), rs_enr.getString("nomCat"), rs_enr.getString("nomsuj"));
 					node.add(f);
 				}
 				rs_enr.close();
 				this.racine.add(node);
 
 			}
 			rs_cat.close();
 			this.racine.setUserObject("Categorie");
 		}
 		catch (Exception e)
 		{
 			GraphicalUserInterface.popupErreur("Erreur lors du chargement des enregistrements", "Erreur");
 		}
 	}
 
 	public void remplirArbreEnregistrementSujet()
 	{
 		ResultSet rs_cat = null, rs_enr = null;
 		try
 		{
 			rs_cat = this.bdd.getListeSujet();
 			while (rs_cat.next())
 			{
 				Branche node = new Branche(rs_cat.getString("nomsuj"));
 				rs_enr = this.bdd.getListeEnregistrementSujet(rs_cat.getInt("idsuj"));
 				while (rs_enr.next())
 				{
 					Feuille f = new Feuille(rs_enr.getInt("id"), rs_enr.getString("nom"), rs_enr.getInt("duree"),
 							rs_enr.getInt("taille"), rs_enr.getString("nomCat"), rs_enr.getString("nomsuj"));
 					node.add(f);
 				}
 				rs_enr.close();
 				this.racine.add(node);
 
 			}
 			rs_cat.close();
 			this.racine.setUserObject("Sujet");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			GraphicalUserInterface.popupErreur("Erreur lors du chargement des enregistrements", "Erreur");
 		}
 	}
 
 	public void viderNoeud(DefaultMutableTreeNode selectednode)
 	{
 		int nbchildren = selectednode.getChildCount();
 
 		for (int i = 0; i < nbchildren; i++)
 		{
 			if (selectednode.getChildAt(0).isLeaf())
 			{
 				((DefaultMutableTreeNode) selectednode.getChildAt(0)).removeFromParent();
 			}
 			else
 			{
 				viderNoeud((DefaultMutableTreeNode) selectednode.getChildAt(0));
 			}
 		}
 		if (selectednode.isRoot() == false)
 		{
 			selectednode.removeFromParent();
 		}
 	}
 
 	public boolean onlySelectBranche()
 	{
 		TreePath[] paths = arbre.getSelectionPaths();
 		if (paths != null)
 		{
 			for (int i = 0; i < paths.length; i++)
 			{
 				if (!(paths[i].getLastPathComponent() instanceof Branche))
 				{
 					return false;
 				}
 			}
 		}
 		else
 		{
 			return false;
 		}
 		return true;
 	}
 
 	public boolean onlySelectFeuille()
 	{
 		TreePath[] paths = arbre.getSelectionPaths();
 		if (paths != null)
 		{
 			for (int i = 0; i < paths.length; i++)
 			{
 				if (!(paths[i].getLastPathComponent() instanceof Feuille))
 				{
 					return false;
 				}
 			}
 		}
 		else
 		{
 			return false;
 		}
 		return true;
 	}
 
 	public JPopupMenu getMenuClicDroit()
 	{
 		return this.menuClicDroit;
 	}
 
 	public void setMenuClicDroit(JPopupMenu menuClicDroit)
 	{
 		this.menuClicDroit = menuClicDroit;
 	}
 
 	class ClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
 			{
 				if (arbre.getSelectionCount() <= 1)
 				{
 					arbre.setSelectionPath(arbre.getPathForLocation(e.getX(), e.getY()));
 				}
 				menuClicDroit.setEnabled(false);
 				menuClicDroit.setVisible(false);
 				menuClicDroit = new JPopupMenu();
 				JMenuItem exporter = new JMenuItem("Exporter");
 				JMenuItem renommer = new JMenuItem("Renommer");
 
 				JMenuItem ecouter = new JMenuItem("Écouter");
 				JMenuItem modifierCategorie = new JMenuItem("Changer catégorie");
 				JMenuItem supprimer = new JMenuItem("Supprimer les enregistrements");
 				JMenuItem ajouterCategorie = new JMenuItem("Ajouter catégorie");
 				JMenuItem supprimerCategorie = new JMenuItem("Supprimer catégorie");
 				JMenuItem ajouterSujet = new JMenuItem("Ajouter sujet");
 				JMenuItem supprimerSujet = new JMenuItem("Supprimer sujet");
 				JMenuItem modifierSujet = new JMenuItem("Changer sujet");
 				JMenuItem changerTri = new JMenuItem();
 				JMenuItem renomerCategorie = new JMenuItem("Renommer catégorie");
 				JMenuItem renomerSujet = new JMenuItem("Renommer sujet");
 
 				exporter.addMouseListener(new ExporterEnregistrementClicDroit());
 				renommer.addMouseListener(new RenommerEnregistrementClicDroit());
 				ecouter.addMouseListener(new PlayEcouteArbre());
 				ajouterCategorie.addMouseListener(new AjouterCategorieEnregistrementClicDroit(menuClicDroit, bdd));
 				modifierCategorie.addMouseListener(new ModifierCategorieEnregistrementClicDroit());
 				supprimer.addMouseListener(new SupprimerEnregistrementClicDroit());
 				supprimerCategorie.addMouseListener(new SupprimerCategorieEnregistrementClicDroit());
 				ajouterSujet.addMouseListener(new AjouterSujetClicDroit(menuClicDroit, bdd));
 				supprimerSujet.addMouseListener(new SupprimerSujetClicDroit());
 				modifierSujet.addMouseListener(new ModifierSujetEnregistrementClicDroit());
 				changerTri.addMouseListener(new ModifierTri());
 				renomerCategorie.addMouseListener(new RenommerCategorieClicDroit());
 				renomerSujet.addMouseListener(new RenommerSujetClicDroit());
 
 				if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 				{
 					changerTri.setText("Grouper par catégorie");
 				}
 				else if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 				{
 					changerTri.setText("Grouper par sujet");
 				}
 
 				if (arbre.getSelectionCount() == 0)
 				{
 					if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 					{
 						menuClicDroit.add(ajouterSujet);
 					}
 					else if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 					{
 						menuClicDroit.add(ajouterCategorie);
 					}
 
 				}
 				if (arbre.getSelectionCount() >= 1 && onlySelectFeuille())
 				{
 					menuClicDroit.add(supprimer);
 					if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 					{
 						menuClicDroit.add(modifierCategorie);
 					}
 					if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 					{
 						menuClicDroit.add(modifierSujet);
 					}
 
 				}
 				if (arbre.getSelectionCount() == 1 && onlySelectFeuille())
 				{
 					menuClicDroit.add(renommer);
 					menuClicDroit.add(ecouter);
 					menuClicDroit.add(exporter);
 				}
 
 				if (arbre.getSelectionCount() >= 1 && onlySelectBranche()
 						&& typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 				{
 					menuClicDroit.add(renomerCategorie);
 					menuClicDroit.add(supprimerCategorie);
 				}
 				if (arbre.getSelectionCount() >= 1 && onlySelectBranche() && typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 				{
 					menuClicDroit.add(renomerSujet);
 					menuClicDroit.add(supprimerSujet);
 				}
 
 				menuClicDroit.add(changerTri);
 
 				menuClicDroit.setEnabled(true);
 				menuClicDroit.setVisible(true);
 
 				menuClicDroit.show(arbre, e.getX(), e.getY());
 			}
 			else
 			{
 				menuClicDroit.setEnabled(false);
 				menuClicDroit.setVisible(false);
 			}
 		}
 	}
 
 	class SupprimerEnregistrementClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			int option = JOptionPane.showConfirmDialog(null,
 					"Voulez-vous supprimer les enregistrements ?\n(Notez que les catégories seront conservées)",
 					"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 			if (option == JOptionPane.OK_OPTION)
 			{
 				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
 				{
 					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
 					{
 						bdd.supprimerEnregistrement(((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent())
 								.getId());
 					}
 				}
 			}
 			updateArbre();
 		}
 
 	}
 
 	class ExporterEnregistrementClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			JFileChooser fileChooser = new JFileChooser();
 			fileChooser.showOpenDialog(null);
 			String fichier;
 			if (fileChooser.getSelectedFile() != null)
 			{
 				try
 				{
 					fichier = fileChooser.getSelectedFile().getCanonicalPath();
 					int id = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
 					// afficher gif
 					bdd.exporter(fichier, id, 2);
 				}
 				catch (Exception e1)
 				{
 					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 					return;
 				}
 			}
 		}
 	}
 
 	class RenommerEnregistrementClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			String nom = JOptionPane.showInputDialog(null, "Entrez le nouveau nom", "Renommer",
 					JOptionPane.QUESTION_MESSAGE);
 			if (nom != null && !nom.equals(""))
 			{
 				try
 				{
 					if (arbre.getLastSelectedPathComponent() instanceof Feuille)// renommer enregistrement
 					{
 						bdd.modifierEnregistrementNom(((Feuille) arbre.getLastSelectedPathComponent()).getId(), nom);
 					}
 					else if (arbre.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode
 							&& typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)// renommer une categorie
 					{
 						bdd.modifierCategorie(
 								bdd.getCategorie(arbre.getSelectionPaths()[0].getLastPathComponent().toString()), nom);
 					}
 					else if (arbre.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode
 							&& typeTrie == PanneauArbre.TYPE_TRIE_SUJET)// renommer une categorie
 					{
 						bdd.modifierSujet(bdd.getSujet(arbre.getSelectionPaths()[0].getLastPathComponent().toString()),
 								nom);
 					}
 				}
 				catch (DBException e1)
 				{
 					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 				}
 			}
 			updateArbre();
 		}
 	}
 
 	class RenommerCategorieClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			String nom = JOptionPane.showInputDialog(null, "Entrez le nouveau nom", "Renommer",
 					JOptionPane.QUESTION_MESSAGE);
 			if (nom != null && !nom.equals(""))
 			{
 				try
 				{
 					if (arbre.getLastSelectedPathComponent() instanceof Branche
 							&& typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)// renommer une categorie
 					{
 						bdd.modifierCategorie(
 								bdd.getCategorie(arbre.getSelectionPaths()[0].getLastPathComponent().toString()), nom);
 					}
 				}
 				catch (DBException e1)
 				{
 					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 				}
 			}
 			updateArbre();
 		}
 	}
 
 	class RenommerSujetClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			String nom = JOptionPane.showInputDialog(null, "Entrez le nouveau nom", "Renommer",
 					JOptionPane.QUESTION_MESSAGE);
 			if (nom != null && !nom.equals(""))
 			{
 				try
 				{
 					if (arbre.getLastSelectedPathComponent() instanceof Branche
 							&& typeTrie == PanneauArbre.TYPE_TRIE_SUJET)// renommer une categorie
 					{
 						bdd.modifierSujet(bdd.getSujet(arbre.getSelectionPaths()[0].getLastPathComponent().toString()),
 								nom);
 					}
 				}
 				catch (DBException e1)
 				{
 					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 				}
 			}
 			updateArbre();
 		}
 	}
 
 	class AjouterCategorieEnregistrementClicDroit extends MouseAdapter
 	{
 		private JPopupMenu menuClicDroit;
 		private BaseDeDonnees bdd;
 
 		public AjouterCategorieEnregistrementClicDroit(JPopupMenu menuClicDroit, BaseDeDonnees bdd)
 		{
 			this.bdd = bdd;
 			this.menuClicDroit = menuClicDroit;
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			if (menuClicDroit != null)
 			{
 				menuClicDroit.setEnabled(false);
 				menuClicDroit.setVisible(false);
 			}
 
 			String nom = JOptionPane.showInputDialog(null, "Entrez le nom de la nouvelle catégorie", "Renommer",
 					JOptionPane.QUESTION_MESSAGE);
 			if (nom != null && !nom.equals(""))
 			{
 				try
 				{
 					bdd.ajouterCategorie(nom);
 				}
 				catch (DBException e1)
 				{
 					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 				}
 			}
 			updateArbre();
 		}
 	}
 
 	class ModifierCategorieEnregistrementClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			DialogueNouvelleCategorie pop = new DialogueNouvelleCategorie(null, null, true, bdd);
 			String nom = ((String) pop.activer()[0]);
 			if (!nom.equals("Ne rien changer"))
 			{
 				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
 				{
 					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
 					{
 						try
 						{
 							bdd.modifierEnregistrementCategorie(
 									((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent()).getId(), nom);
 						}
 						catch (DBException e1)
 						{
 							GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 						}
 					}
 				}
 				updateArbre();
 			}
 		}
 	}
 
 	class SupprimerCategorieEnregistrementClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			int option = JOptionPane.showConfirmDialog(null, "Voulez-vous supprimer les catégories ?\n", "Suppression",
 					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 			if (option == JOptionPane.OK_OPTION)
 			{
 				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
 				{
 					try
 					{
 						if (!(arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille))
 						{
 							ResultSet rs = bdd.getListeEnregistrementCategorie(bdd.getCategorie(arbre
 									.getSelectionPaths()[i].getLastPathComponent().toString()));
 							if (rs.next())
 							{
 								GraphicalUserInterface.popupErreur(
 										"Une catégorie peut être supprimée quand elle n'a plus d'enregistrements.",
 										"Erreur");
 							}
 							else
 							{
 								bdd.supprimerCategorie(bdd.getCategorie(arbre.getSelectionPaths()[i]
 										.getLastPathComponent().toString()));
 							}
 							rs.close();
 						}
 					}
 					catch (Exception e1)
 					{
 						GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 					}
 				}
 			}
 			updateArbre();
 		}
 	}
 
 	class AjouterSujetClicDroit extends MouseAdapter
 	{
 		private JPopupMenu menuClicDroit;
 		private BaseDeDonnees bdd;
 
 		public AjouterSujetClicDroit(JPopupMenu menuClicDroit, BaseDeDonnees bdd)
 		{
 			this.bdd = bdd;
 			this.menuClicDroit = menuClicDroit;
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			if (menuClicDroit != null)
 			{
 				menuClicDroit.setEnabled(false);
 				menuClicDroit.setVisible(false);
 			}
 			String option = JOptionPane.showInputDialog("Nouveau sujet");
 			if (option != "" && option != null)
 			{
 				try
 				{
 					this.bdd.ajouterSujet(option);
 				}
 				catch (Exception e1)
 				{
 					GraphicalUserInterface.popupErreur(
 							"Erreur lors de l'ajout du sujet " + option + " " + e1.getMessage(), "Erreur");
 				}
 			}
 			updateArbre();
 		}
 	}
 
 	class SupprimerSujetClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			int option = JOptionPane.showConfirmDialog(null, "Voulez-vous supprimer les sujets ?\n", "Suppression",
 					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 			if (option == JOptionPane.OK_OPTION)
 			{
 				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
 				{
 					try
 					{
 						if (!(arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille))
 						{
 							ResultSet rs = bdd.getListeEnregistrementSujet(bdd.getSujet(arbre.getSelectionPaths()[i]
 									.getLastPathComponent().toString()));
 							if (rs.next())
 							{
 								GraphicalUserInterface.popupErreur(
 										"Un sujet peut être supprimé quand il n'a plus d'enregistrements.", "Erreur");
 							}
 							else
 							{
 								bdd.supprimerSujet(bdd.getSujet(arbre.getSelectionPaths()[i].getLastPathComponent()
 										.toString()));
 							}
 							rs.close();
 						}
 					}
 					catch (Exception e1)
 					{
 						GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 					}
 				}
 			}
 			updateArbre();
 		}
 	}
 
 	class ModifierSujetEnregistrementClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			DialogueNouveauSujet pop = new DialogueNouveauSujet(null, null, true, bdd);
 			String nom = ((String) pop.activer()[0]);
 			if (!nom.equals("Ne rien changer"))
 			{
 				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
 				{
 					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
 					{
 						try
 						{
 							bdd.modifierEnregistrementSujet(
 									((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent()).getId(), nom);
 						}
 						catch (DBException e1)
 						{
 							GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
 						}
 					}
 				}
 				updateArbre();
 			}
 		}
 	}
 
 	class ModifierTri extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			menuClicDroit.setEnabled(false);
 			menuClicDroit.setVisible(false);
 			if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 			{
 				typeTrie = PanneauArbre.TYPE_TRIE_SUJET;
 			}
 			else
 			{
 				typeTrie = PanneauArbre.TYPE_TRIE_CATEGORIE;
 			}
 			updateArbre();
 		}
 	}
 
 	class PlayEcouteArbre extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent event)
 		{
 			if (menuClicDroit != null)
 			{
 				menuClicDroit.setEnabled(false);
 				menuClicDroit.setVisible(false);
 			}
 			lecteurAudio.play();
 		}
 	}
 
 	private class ClicGauche extends MouseAdapter
 	{
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
 			{
 				if (arbre.getSelectionCount() == 1 && onlySelectFeuille())
 				{
 					loadAudioFile(((Feuille) arbre.getLastSelectedPathComponent()).getId());
 				}
 			}
 		}
 	}
 
 	private void loadAudioFile(final int id)
 	{
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				try
 				{
 					lecteurAudio.stop();
 					File idAudioFile = new File(cacheDirectory, id + ".wav");
 					if (!idAudioFile.exists())
 					{
 						idAudioFile.createNewFile();
 						byte[] contenu = bdd.recupererEnregistrement(id);
 						FileOutputStream fos = new FileOutputStream(idAudioFile);
 						fos.write(contenu);
 						fos.flush();
 						fos.close();
 					}
 					lecteurAudio.load(idAudioFile.getCanonicalPath());
 				}
 				catch (FileNotFoundException e)
 				{
 					GraphicalUserInterface.popupErreur("Création du fichier audio temporaire : " + e.getMessage());
 				}
 				catch (IOException e)
 				{
 					GraphicalUserInterface.popupErreur("Création du fichier audio temporaire : " + e.getMessage());
 				}
 				catch (DBException e)
 				{
 					GraphicalUserInterface.popupErreur("Création du fichier audio temporaire : " + e.getMessage());
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onUpdateDataBase()
 	{
 		this.updateArbre();
 	}
 }
