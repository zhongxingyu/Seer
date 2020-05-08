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
 import java.sql.SQLException;
 import java.util.Enumeration;
 
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
	private int idLu = -1;
 
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
 		this.arbre.expandPath(new TreePath(this.racine));
 	}
 
 	public void remplirArbreEnregistrementCategorie()
 	{
 		ResultSet rsCat = null, rsEnr = null;
 
 		try
 		{
 			rsCat = this.bdd.getListeCategorie();
 			while (rsCat.next())
 			{
 				Branche node = new Branche(rsCat.getString("nomcat"));
 				rsEnr = this.bdd.getListeEnregistrementCategorie(rsCat.getInt("idcat"));
 				while (rsEnr.next())
 				{
 					Feuille f = new Feuille(rsEnr.getInt("id"), rsEnr.getString("nom"), rsEnr.getInt("duree"),
 							rsEnr.getInt("taille"), rsEnr.getString("nomCat"), rsEnr.getString("nomsuj"));
 					node.add(f);
 				}
 				rsEnr.close();
 				this.racine.add(node);
 
 			}
 			rsCat.close();
 			this.racine.setUserObject("Categorie");
 		}
 		catch (DBException e)
 		{
 			GraphicalUserInterface.popupErreur(
 					"Erreur lors du chargement des enregistrements : " + e.getLocalizedMessage(), "Erreur");
 		}
 		catch (SQLException e)
 		{
 			GraphicalUserInterface.popupErreur(
 					"Erreur lors du chargement des enregistrements : " + e.getLocalizedMessage(), "Erreur");
 		}
 	}
 
 	public void remplirArbreEnregistrementSujet()
 	{
 		ResultSet rsCat = null, rsEnr = null;
 
 		try
 		{
 			rsCat = this.bdd.getListeSujet();
 			while (rsCat.next())
 			{
 				Branche node = new Branche(rsCat.getString("nomsuj"));
 				rsEnr = this.bdd.getListeEnregistrementSujet(rsCat.getInt("idsuj"));
 				while (rsEnr.next())
 				{
 					Feuille f = new Feuille(rsEnr.getInt("id"), rsEnr.getString("nom"), rsEnr.getInt("duree"),
 							rsEnr.getInt("taille"), rsEnr.getString("nomCat"), rsEnr.getString("nomsuj"));
 					node.add(f);
 				}
 				rsEnr.close();
 				this.racine.add(node);
 
 			}
 			rsCat.close();
 			this.racine.setUserObject("Sujet");
 		}
 		catch (DBException e)
 		{
 			GraphicalUserInterface.popupErreur(
 					"Erreur lors du chargement des enregistrements : " + e.getLocalizedMessage(), "Erreur");
 		}
 		catch (SQLException e)
 		{
 			GraphicalUserInterface.popupErreur(
 					"Erreur lors du chargement des enregistrements : " + e.getLocalizedMessage(), "Erreur");
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
 			for (TreePath path : paths)
 			{
 				if (!(path.getLastPathComponent() instanceof Branche))
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
 			for (TreePath path : paths)
 			{
 				if (!(path.getLastPathComponent() instanceof Feuille))
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
 
 	/**
 	 * Efface le menu contextuel dû à un clic droit
 	 */
 	public void effacerMenuContextuel()
 	{
 		if (this.menuClicDroit != null)
 		{
 			this.menuClicDroit.setEnabled(false);// On efface le menu contextuel
 			this.menuClicDroit.setVisible(false);
 		}
 	}
 
 	class ClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			effacerMenuContextuel();
 			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
 			{
 				if (arbre.getSelectionCount() <= 1)
 				{
 					arbre.setSelectionPath(arbre.getPathForLocation(e.getX(), e.getY()));
 				}
 				menuClicDroit = new JPopupMenu();
 
 				JMenuItem changerTri = new JMenuItem();
 				changerTri.addMouseListener(new ModifierTri());
 
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
 					JMenuItem collapseAll = new JMenuItem("Replier tout");
 					JMenuItem expandAll = new JMenuItem("Développer tout");
 					collapseAll.addMouseListener(new CollapseClicDroit());
 					expandAll.addMouseListener(new ExpandClicDroit());
 					menuClicDroit.add(expandAll);
 					menuClicDroit.add(collapseAll);
 					if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 					{
 						JMenuItem ajouterSujet = new JMenuItem("Ajouter sujet");
 						ajouterSujet.addMouseListener(new AjouterSujetClicDroit(menuClicDroit, bdd));
 						menuClicDroit.add(ajouterSujet);
 					}
 					else if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 					{
 						JMenuItem ajouterCategorie = new JMenuItem("Ajouter catégorie");
 						ajouterCategorie.addMouseListener(new AjouterCategorieEnregistrementClicDroit(menuClicDroit,
 								bdd));
 						menuClicDroit.add(ajouterCategorie);
 					}
 
 				}
 				else if (onlySelectFeuille())
 				{
 					if (arbre.getSelectionCount() >= 1)
 					{
 						JMenuItem supprimer = new JMenuItem("Supprimer les enregistrements");
 						supprimer.addMouseListener(new SupprimerEnregistrementClicDroit());
 						menuClicDroit.add(supprimer);
 						if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 						{
 							JMenuItem modifierCategorie = new JMenuItem("Changer catégorie");
 							modifierCategorie.addMouseListener(new ModifierCategorieEnregistrementClicDroit());
 							menuClicDroit.add(modifierCategorie);
 						}
 						else if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 						{
 							JMenuItem modifierSujet = new JMenuItem("Changer sujet");
 							modifierSujet.addMouseListener(new ModifierSujetEnregistrementClicDroit());
 							menuClicDroit.add(modifierSujet);
 						}
 					}
 					if (arbre.getSelectionCount() == 1)
 					{
 						JMenuItem exporter = new JMenuItem("Exporter");
 						JMenuItem renommer = new JMenuItem("Renommer");
 						JMenuItem ecouter = new JMenuItem("Écouter");
 						exporter.addMouseListener(new ExporterEnregistrementClicDroit());
 						ecouter.addMouseListener(new PlayEcouteArbre());
 						renommer.addMouseListener(new RenommerEnregistrementClicDroit());
 						menuClicDroit.add(renommer);
 						menuClicDroit.add(ecouter);
 						menuClicDroit.add(exporter);
						if (idLu != ((Feuille) arbre.getLastSelectedPathComponent()).getId())
						{
							idLu = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
							loadAudioFile(idLu);
						}
 					}
 
 				}
 				else if (arbre.getSelectionCount() >= 1 && onlySelectBranche())
 				{
 					if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
 					{
 						JMenuItem renomerCategorie = new JMenuItem("Renommer catégorie");
 						renomerCategorie.addMouseListener(new RenommerCategorieClicDroit());
 						JMenuItem supprimerCategorie = new JMenuItem("Supprimer catégorie");
 						supprimerCategorie.addMouseListener(new SupprimerCategorieEnregistrementClicDroit());
 						menuClicDroit.add(renomerCategorie);
 						menuClicDroit.add(supprimerCategorie);
 					}
 					else if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
 					{
 						JMenuItem renomerSujet = new JMenuItem("Renommer sujet");
 						renomerSujet.addMouseListener(new RenommerSujetClicDroit());
 						JMenuItem supprimerSujet = new JMenuItem("Supprimer sujet");
 						supprimerSujet.addMouseListener(new SupprimerSujetClicDroit());
 						menuClicDroit.add(renomerSujet);
 						menuClicDroit.add(supprimerSujet);
 					}
 				}
 
 				menuClicDroit.add(changerTri);
 
 				menuClicDroit.setEnabled(true);
 				menuClicDroit.setVisible(true);
 
 				menuClicDroit.show(arbre, e.getX(), e.getY());
 			}
 		}
 	}
 
 	class SupprimerEnregistrementClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			effacerMenuContextuel();
 			int option = JOptionPane.showConfirmDialog(null,
 					"Voulez-vous supprimer les enregistrements ?\n(Notez que les catégories seront conservées)",
 					"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 			if (option == JOptionPane.OK_OPTION)
 			{
 				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
 				{
 					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
 					{
 						try
 						{
 							bdd.supprimerEnregistrement(((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent())
 									.getId());
 						}
 						catch (DBException exception)
 						{
 							GraphicalUserInterface.popupErreur("Impossible de supprimer l'enregistrement : "
 									+ exception.getMessage());
 						}
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
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
 			effacerMenuContextuel();
 			lecteurAudio.play();
 		}
 	}
 
 	/**
 	 * Charge un fichier enregistrement.
 	 * 
 	 */
 	private class ClicGauche extends MouseAdapter
 	{
 		@Override
 		public void mouseClicked(MouseEvent e)
 		{
 			if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
 			{
 				if (arbre.getSelectionCount() == 1 && onlySelectFeuille())
 				{
 					if (idLu != ((Feuille) arbre.getLastSelectedPathComponent()).getId())
 					{
 						idLu = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
 						loadAudioFile(idLu);
 					}
 					if (e.getClickCount() > 1)
 					{
 						lecteurAudio.play();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Permet replier l'arbre
 	 * 
 	 * @author Azazel
 	 * 
 	 */
 	class CollapseClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			effacerMenuContextuel();
 			Enumeration children = racine.children();
 			Object tmp, tab[] = new Object[2];
 			tab[0] = racine;
 			while (children.hasMoreElements())
 			{
 				tmp = children.nextElement();
 				tab[1] = tmp;
 				arbre.collapsePath(new TreePath(tab));
 			}
 		}
 	}
 
 	/**
 	 * Permet deplier l'arbre
 	 * 
 	 * @author Azazel
 	 * 
 	 */
 	class ExpandClicDroit extends MouseAdapter
 	{
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			effacerMenuContextuel();
 			Enumeration children = racine.children();
 			Object tmp, tab[] = new Object[2];
 			tab[0] = racine;
 			while (children.hasMoreElements())
 			{
 				tmp = children.nextElement();
 				tab[1] = tmp;
 				arbre.expandPath(new TreePath(tab));
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
