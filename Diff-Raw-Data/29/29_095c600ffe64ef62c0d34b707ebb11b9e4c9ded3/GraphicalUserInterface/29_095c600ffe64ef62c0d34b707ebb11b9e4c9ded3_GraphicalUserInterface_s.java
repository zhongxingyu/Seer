 package mensonge.userinterface;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import mensonge.core.Extraction;
 import mensonge.core.BaseDeDonnees.BaseDeDonnees;
 import mensonge.core.BaseDeDonnees.DBException;
 import mensonge.core.plugins.Plugin;
 import mensonge.core.plugins.PluginManager;
 import mensonge.core.tools.Cache;
 import mensonge.core.tools.Locker;
 import mensonge.userinterface.tree.PanneauArbre;
 import uk.co.caprica.vlcj.binding.LibVlc;
 import uk.co.caprica.vlcj.filter.swing.SwingFileFilterFactory;
 import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 
 import com.sun.jna.Native;
 import com.sun.jna.NativeLibrary;
 
 /**
  * 
  * Classe Interface graphique contenant tous les composants graphiques
  * 
  */
 public class GraphicalUserInterface extends JFrame implements ActionListener
 {
 	private static final long serialVersionUID = 5373991180139317820L;
 	private static Logger logger = Logger.getLogger("gui");
 	private JTabbedPane onglets;
 
 	private JMenuItem aideAPropos;
 	private JMenuItem fichierFermer;
 	private JMenuItem fichierOuvrir;
 	private String previousPath;
 
 	private PanneauArbre panneauArbre;
 	private BaseDeDonnees bdd;
 
 	private JMenuBar menuBar;
 
 	private PluginManager pluginManager;
 
 	private Locker locker;
 
 	private JMenu menuOutils;
 	private StatusBar statusBar;
 	private Cache cache;
 	private Extraction extraction;
 
 	/**
 	 * Créé une nouvelle fenêtre avec les différents panneaux (onglets et arbre), défini les propriétés de la fenêtre et
 	 * lance la connxion à la BDD
 	 */
 	public GraphicalUserInterface()
 	{
 		this.locker = new Locker();
 		this.extraction = new Extraction();
 		this.cache = new Cache();
 
 		connexionBase("LieLab.db");
 		this.setLayout(new BorderLayout());
 		this.previousPath = null;
 		this.statusBar = new StatusBar();
 		this.cache.addObserver(statusBar);
 		this.cache.fireUpdateCache();
 		this.extraction.addObserver(statusBar);
 		this.add(statusBar, BorderLayout.SOUTH);
 		this.panneauArbre = new PanneauArbre(bdd, cache);
 		this.bdd.addObserver(panneauArbre);
 		this.bdd.addObserver(statusBar);
 		this.locker.addTarget(panneauArbre);
 		this.ajoutBarMenu();
 
 		/*
 		 * Conteneur
 		 */
 		this.onglets = new JTabbedPane();
 		PanelWithBackground panel = new PanelWithBackground(new BorderLayout());
 		panel.add(onglets, BorderLayout.CENTER);
 
 		this.add(panel, BorderLayout.CENTER);
 		this.add(panneauArbre, BorderLayout.EAST);
 
 		/*
 		 * Fenêtre
 		 */
 		try
 		{
 			this.setIconImage(ImageIO.read(new File("images/LieLabIcon.png")));
 		}
 		catch (IOException e)
 		{
 			logger.log(Level.WARNING, e.getLocalizedMessage());
 			popupErreur(e.getMessage());
 		}
 
 		this.setTransferHandler(new HandlerDragLecteur(this, this.bdd, this.extraction));
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(true);
 		this.setTitle("LieLab");
 		this.setLocationRelativeTo(null);
 		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		this.setVisible(true);
 		this.setEnabled(true);
 	}
 
 	/**
 	 * Ajoute une bar de menu
 	 */
 	private void ajoutBarMenu()
 	{
 		this.fichierFermer = new JMenuItem("Quitter", 'Q');
 		this.fichierFermer.addActionListener(this);
 
 		this.fichierOuvrir = new JMenuItem("Ouvrir", 'O');
 		this.fichierOuvrir.addActionListener(this);
 
 		JMenuItem baseExporter = new JMenuItem("Exporter", 'E');
 		baseExporter.addActionListener(new ExporterBaseListener(this));
 
 		JMenuItem baseImporter = new JMenuItem("Importer", 'I');
 		baseImporter.addActionListener(new ImporterBaseListener(this));
 
 		JMenuItem baseAjouterCategorie = new JMenuItem("Ajouter catégorie");
 		JMenuItem baseAjouterSujet = new JMenuItem("Ajouter sujet");
 		baseAjouterCategorie.addMouseListener(new AjouterCategorieListener(bdd));
 		baseAjouterSujet.addMouseListener(new AjouterSujetListener(bdd));
 
 		JMenuItem fichierPurger = new JMenuItem("Purger le cache");
 		fichierPurger.addActionListener(new PurgerCacheListener());
 		JMenu menuFichier = new JMenu("Fichier");
 		menuFichier.add(fichierOuvrir);
 		menuFichier.add(baseExporter);
 		menuFichier.add(baseImporter);
 		menuFichier.add(fichierPurger);
 		menuFichier.addSeparator();
 		menuFichier.add(fichierFermer);
 
 		this.aideAPropos = new JMenuItem("À propos");
 		this.aideAPropos.addActionListener(this);
 
 		JMenuItem baseCompacter = new JMenuItem("Compacter la base de données");
 		baseCompacter.addActionListener(new CompacterBaseListener());
 
 		JMenu menuAide = new JMenu("Aide");
 		menuAide.add(aideAPropos);
 
 		JMenu menuBase = new JMenu("Base de données");
 		menuBase.add(baseAjouterCategorie);
 		menuBase.add(baseAjouterSujet);
 		menuBase.add(baseCompacter);
 
 		this.fichierFermer.setAccelerator(KeyStroke.getKeyStroke('Q', KeyEvent.CTRL_DOWN_MASK));
 		this.fichierOuvrir.setAccelerator(KeyStroke.getKeyStroke('O', KeyEvent.CTRL_DOWN_MASK));
 		baseImporter.setAccelerator(KeyStroke.getKeyStroke('I', KeyEvent.CTRL_DOWN_MASK));
 		baseExporter.setAccelerator(KeyStroke.getKeyStroke('E', KeyEvent.CTRL_DOWN_MASK));
 
 		this.menuBar = new JMenuBar();
 
 		this.menuBar.add(menuFichier);
 		this.ajoutMenuPlugins();
 		this.menuBar.add(menuBase);
 		this.menuBar.add(menuAide);
 		this.panneauArbre.setEvent(true);
 		if (this.panneauArbre.isEvent())
 		{
 			JMenu menuAffichage = new JMenu("Affichage");
 			JMenuItem affichageDisableEvent = new JMenuItem("Desactiver l'evennement");
 			affichageDisableEvent.addActionListener(new DesactiverEvent());
 			menuAffichage.add(affichageDisableEvent);
 			this.menuBar.add(menuAffichage);
 		}
 		this.setJMenuBar(this.menuBar);
 	}
 
 	/**
 	 * Ajoute un menu des plugins existants
 	 */
 	private void ajoutMenuPlugins()
 	{
 		menuOutils = new JMenu("Outils");
 		this.menuBar.add(menuOutils);
 
 		pluginManager = new PluginManager();
 		this.chargerListePlugins();
 	}
 
 	/**
 	 * Charge la liste des plugins et l'affiche dans le menu
 	 */
 	private void chargerListePlugins()
 	{
 		final String aucunOutil = "Aucun outil";
 		final String errorMsg = "Impossible de charger les outils : ";
 		menuOutils.removeAll();
 		try
 		{
 			pluginManager.loadPlugins();
 			Map<String, Plugin> mapPlugins = pluginManager.getPlugins();
 
 			if (pluginManager.getPlugins().isEmpty())
 			{
 				menuOutils.add(new JMenuItem("Aucun outil"));
 			}
 			else
 			{
 				for (Entry<String, Plugin> entry : mapPlugins.entrySet())
 				{
 					JMenuItem item = new JMenuItem(entry.getKey());
 					item.addActionListener(new ItemPluginListener(mapPlugins.get(entry.getKey()), this.panneauArbre,
 							this, extraction));
 					menuOutils.add(item);
 				}
 			}
 		}
 		catch (ClassNotFoundException e)
 		{
 			GraphicalUserInterface.popupErreur(errorMsg + e.getMessage());
 			menuOutils.add(new JMenuItem(aucunOutil));
 		}
 		catch (InstantiationException e)
 		{
 			GraphicalUserInterface.popupErreur(errorMsg + e.getMessage());
 			menuOutils.add(new JMenuItem(aucunOutil));
 		}
 		catch (IllegalAccessException e)
 		{
 			GraphicalUserInterface.popupErreur(errorMsg + e.getMessage());
 			menuOutils.add(new JMenuItem(aucunOutil));
 		}
 		catch (IOException e)
 		{
 			GraphicalUserInterface.popupErreur(errorMsg + e.getMessage());
 			menuOutils.add(new JMenuItem(aucunOutil));
 		}
 
 		this.menuOutils.add(new JSeparator(JSeparator.HORIZONTAL));
 
 		JMenuItem itemRechargerPlugins = new JMenuItem("Rafraîchir la liste des outils");
 		itemRechargerPlugins.addActionListener(new ReloadPluginsListener());
 		menuOutils.add(itemRechargerPlugins);
 	}
 
 	/**
 	 * Ajoute un nouvel onglet à l'interface graphique
 	 * 
 	 * @param onglet
 	 *            Onglet à ajouter
 	 */
 	protected void ajouterOnglet(OngletLecteur onglet)
 	{
 		JButton boutonFermeture = new JButton(new ImageIcon("images/CloseTab.png"));
 		boutonFermeture.setToolTipText("Fermer cet onglet");
 		boutonFermeture.setContentAreaFilled(false);
 		boutonFermeture.setFocusable(false);
 		boutonFermeture.setBorder(BorderFactory.createEmptyBorder());
 		boutonFermeture.setBorderPainted(false);
 		boutonFermeture.addActionListener(new FermetureOngletListener(this.onglets, onglet));
 
 		JPanel panelFermeture = new JPanel();
 		panelFermeture.setBackground(new Color(0, 0, 0, 0));
 		panelFermeture.add(new JLabel(onglet.getNom()));
 		panelFermeture.add(boutonFermeture);
 
 		this.onglets.add(onglet);
 		this.onglets.setTabComponentAt(this.onglets.getTabCount() - 1, panelFermeture);
 		onglet.requestFocus();
 	}
 
 	/**
 	 * Permet de fermer proprement tous les onglets ouverts de l'application
 	 */
 	private void closeAllTabs()
 	{
 		// On ferme proprement tous les onglets avant de quitter
 		for (Component comp : onglets.getComponents())
 		{
 			if (comp instanceof OngletLecteur)
 			{
 				// On met le focus sur l'onglet si celui-ci génère un event du genre popup pour qu'on soit sur le bon
 				// onglet
 				onglets.setSelectedComponent(comp);
 				comp.requestFocusInWindow();
 
 				// on appel sa méthode pour qu'il ferme proprement tout ce qu'il a ouvert
 				((OngletLecteur) comp).fermerOnglet();
 				// Puis on le supprime
 				onglets.remove(comp);
 			}
 		}
 	}
 
 	/**
 	 * Quitte le programme en fermant proprement ce qui est nécessaire
 	 */
 	private void quitter()
 	{
 		this.panneauArbre.close();
 		this.pluginManager.unloadPlugins();
 		this.closeAllTabs();
 		this.dispose();
 	}
 
 	@Override
 	protected void processWindowEvent(WindowEvent event)
 	{
 		if (event.getID() == WindowEvent.WINDOW_CLOSING)
 		{
 			this.quitter();
 		}
 		else if (event.getID() == WindowEvent.WINDOW_DEACTIVATED)
 		{
 			this.panneauArbre.getMenuClicDroit().setEnabled(false);
 			this.panneauArbre.getMenuClicDroit().setVisible(false);
 		}
 		else
 		{
 			super.processWindowEvent(event);
 		}
 	}
 
 	private void connexionBase(String fichier)
 	{
 		try
 		{
 			bdd = new BaseDeDonnees(fichier);
 			bdd.connexion();// connexion et verification de la validite de la
 							// table
 		}
 		catch (DBException e)
 		{
 			int a = e.getCode();
 			if (a == 2)
 			{
 				try
 				{
 					bdd.createDatabase();
 				}
 				catch (DBException e1)
 				{
 					logger.log(Level.SEVERE, e1.getLocalizedMessage());
 					popupErreur("Erreur lors de la création de la base de données : " + e1.getMessage());
 				}
 			}
 			else
 			{
 				logger.log(Level.SEVERE, e.getLocalizedMessage());
 				popupErreur("Erreur lors de la connexion de la base de données : " + e.getMessage());
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Affiche une popup qui signale une erreur
 	 * 
 	 * @param message
 	 *            Le message d'erreur à afficher
 	 * @param title
 	 *            Le titre de la popup
 	 */
 	public static void popupErreur(String message, String title)
 	{
 		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
 	}
 
 	/**
 	 * Affiche une popup qui signale une erreur avec en titre Erreur
 	 * 
 	 * @param message
 	 *            Le message d'erreur à afficher
 	 */
 	public static void popupErreur(String message)
 	{
 		popupErreur(message, "Erreur");
 	}
 
 	/**
 	 * Affiche une popup d'information
 	 * 
 	 * @param message
 	 *            L'information à afficher
 	 * @param title
 	 *            Le titre de la popup
 	 */
 	public static void popupInfo(String message, String title)
 	{
 		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event)
 	{
 		if (event.getSource() == fichierFermer)
 		{
 			this.quitter();
 		}
 		else if (event.getSource() == aideAPropos)
 		{
 			JOptionPane.showMessageDialog(null, "Projet d'aide à l'étude de la détection de mensonge", "À propos",
 					JOptionPane.PLAIN_MESSAGE);
 		}
 		else if (event.getSource() == fichierOuvrir)
 		{
 			JFileChooser fileChooser = new JFileChooser(previousPath);
 			fileChooser.setFileFilter(SwingFileFilterFactory.newVideoFileFilter());
 			fileChooser.addChoosableFileFilter(SwingFileFilterFactory.newAudioFileFilter());
 			fileChooser.setMultiSelectionEnabled(true);
 			int option = fileChooser.showOpenDialog(this);
 			if (option == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFiles() != null)
 			{
 				try
 				{
 					for (File file : fileChooser.getSelectedFiles())
 					{
 						previousPath = file.getCanonicalPath();
 						this.ajouterOnglet(new OngletLecteur(file, this.bdd, this, extraction));
 					}
 				}
 				catch (IOException e)
 				{
 					logger.log(Level.WARNING, e.getLocalizedMessage());
 					popupErreur(e.getMessage());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Classe Listener gérant la fermeture des onglets, qui sera ajouté à chaque onglet
 	 */
 	private static class FermetureOngletListener implements ActionListener
 	{
 		private JTabbedPane onglets;
 		private OngletLecteur onglet;
 
 		public FermetureOngletListener(JTabbedPane onglets, OngletLecteur onglet)
 		{
 			this.onglet = onglet;
 			this.onglets = onglets;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			onglet.fermerOnglet();
 			onglets.remove(onglet);
 		}
 	}
 
 	/**
 	 * Listener pour le bouton d'exportation dans le menu
 	 * 
 	 */
 	private class ExporterBaseListener implements ActionListener
 	{
 		private GraphicalUserInterface fenetre;
 		private String previousPathBase;
 
 		public ExporterBaseListener(GraphicalUserInterface g)
 		{
 			previousPathBase = null;
 			fenetre = g;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			JFileChooser fileChooser = new JFileChooser(previousPathBase);
 			fileChooser.showOpenDialog(fenetre);
 			if (fileChooser.getSelectedFile() != null)
 			{
 				try
 				{
 					statusBar.setMessage("Exportation en cours...");
 					previousPathBase = fileChooser.getSelectedFile().getCanonicalPath();
 					bdd.exporter(previousPathBase, -1, 1);
 				}
 				catch (DBException e1)
 				{
 					statusBar.setMessage("Erreur durant l'exportation");
 					logger.log(Level.WARNING, e1.getLocalizedMessage());
 					popupErreur(e1.getMessage());
 				}
 				catch (IOException e1)
 				{
 					statusBar.setMessage("Erreur durant l'exportation");
 					logger.log(Level.WARNING, e1.getLocalizedMessage());
 					popupErreur(e1.getMessage());
 				}
 				statusBar.done();
 			}
 		}
 	}
 
 	/**
 	 * Listener pour le bouton d'importation dans le menu
 	 * 
 	 */
 	private class ImporterBaseListener implements ActionListener
 	{
 		private GraphicalUserInterface fenetre;
 		private String previousPathBase;
 
 		public ImporterBaseListener(GraphicalUserInterface g)
 		{
 			previousPathBase = null;
 			fenetre = g;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			JFileChooser fileChooser = new JFileChooser(previousPathBase);
 			fileChooser.showOpenDialog(fenetre);
 			if (fileChooser.getSelectedFile() != null)
 			{
 				locker.lockUpdate();
 				try
 				{
 					statusBar.setMessage("Importation en cours...");
 					previousPathBase = fileChooser.getSelectedFile().getCanonicalPath();
 					bdd.importer(previousPathBase);
 					statusBar.setMessage("Importation terminée");
 				}
 				catch (IOException e1)
 				{
 					statusBar.setMessage("Erreur durant l'importation");
 					logger.log(Level.WARNING, e1.getLocalizedMessage());
 					popupErreur(e1.getMessage());
 				}
 				catch (DBException e2)
 				{
 					statusBar.setMessage("Erreur durant l'importation");
 					logger.log(Level.WARNING, e2.getLocalizedMessage());
 					popupErreur(e2.getMessage());
 				}
 				locker.unlockUpdate();
 				statusBar.done();
 			}
 		}
 	}
 
 	/**
 	 * Listener pour les items des différents plugins dans le menu. Un item = un listener spécifique pour lancer le bon
 	 * plugin au clic
 	 * 
 	 */
 	private static class ItemPluginListener implements ActionListener
 	{
 		private Plugin plugin;
 		private PanneauArbre panneauArbre;
 		private GraphicalUserInterface gui;
 		private Extraction extraction;
 
 		/**
 		 * Nouveau listener pour un item de plugin
 		 * 
 		 * @param plugin
 		 *            Plugin associé à l'item dans le menu
 		 * @param panneauArbre
 		 *            PanneauArbre servant à récupérer la liste des enregistrements selectionnés
 		 * @param gui
 		 *            Instance de la gui pour définir le cursor wait/default pour montrer le chargement du plugin
 		 */
 		public ItemPluginListener(Plugin plugin, PanneauArbre panneauArbre, GraphicalUserInterface gui,
 				Extraction extraction)
 		{
 			this.extraction = extraction;
 			this.gui = gui;
 			this.plugin = plugin;
 			this.panneauArbre = panneauArbre;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			// TODO Peut être voir pour aussi donner en plus l'instance de la BDD ça pourrait être utile au final ? :D
 			gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			this.plugin.lancer(extraction, panneauArbre.getListSelectedRecords());
 			gui.setCursor(Cursor.getDefaultCursor());
 		}
 	}
 
 	/**
 	 * Listener pour le bouton de rechargement de la liste des plugins dans le menu
 	 * 
 	 */
 	private class ReloadPluginsListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			chargerListePlugins();
 		}
 	}
 
 	/**
 	 * Listener pour le bouton de purge du cache dans le menu
 	 * 
 	 */
 	private class PurgerCacheListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			Map<String, Plugin> mapPlugins = pluginManager.getPlugins();
 			for (Entry<String, Plugin> entry : mapPlugins.entrySet())
 			{
 				if (entry.getValue().isActive())
 				{
 					popupErreur("Vous avez au moins 1 plugin en cours d'éxecution. La purge du cache est donc désactivé.");
 					return;
 				}
 			}
 			cache.purge();
 		}
 	}
 
 	/**
 	 * Listener pour le bouton de compactage de la bdd dans le menu
 	 * 
 	 */
 	private class CompacterBaseListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			SwingUtilities.invokeLater(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					try
 					{
 						bdd.compacter();
 					}
 					catch (DBException e1)
 					{
 						logger.log(Level.WARNING, e1.getLocalizedMessage());
 						GraphicalUserInterface.popupErreur(e1.getLocalizedMessage());
 					}
 				}
 
 			});
 		}
 	}
 
 	private class DesactiverEvent implements ActionListener
 	{
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			panneauArbre.setEvent(!panneauArbre.isEvent());
 			JMenuItem menu = (JMenuItem) e.getSource();
 			if (!panneauArbre.isEvent())
 			{
 				menu.setText("Activer l'évènement");
 			}
 			else
 			{
 				menu.setText("Désactiver l'évènement");
 			}
 
 		}
 	}
 
 	/**
 	 * Main de l'application défini les constantes pour l'anti-aliasing, tente de charger le thème nimbus, charge les
 	 * libs de VLC et lance la fenêtre de l'application
 	 * 
 	 * @param args
 	 *            Arguments du main, ne sont pas pris en compte
 	 */
 	public static void main(String args[])
 	{
 		// On active l'anti-aliasing
 		System.setProperty("awt.useSystemAAFontSettings", "on");
 		System.setProperty("swing.aatext", "true");
 
 		// Tente de changer le thème avec le thème nimbus
 		try
 		{
			for(LookAndFeelInfo look : UIManager.getInstalledLookAndFeels())
 			{
				if(look.getClassName().contains("nimbus"))
 				{
 					UIManager.setLookAndFeel(look.getClassName());
 					break;
 				}
 			}
 		}
 		catch (ClassNotFoundException e1)
 		{
 			logger.log(Level.WARNING, e1.getLocalizedMessage());
 		}
 		catch (InstantiationException e1)
 		{
 			logger.log(Level.WARNING, e1.getLocalizedMessage());
 		}
 		catch (IllegalAccessException e1)
 		{
 			logger.log(Level.WARNING, e1.getLocalizedMessage());
 		}
 		catch (UnsupportedLookAndFeelException e1)
 		{
 			logger.log(Level.WARNING, e1.getLocalizedMessage());
 		}
 
 		// Chargement des bibliothèques pour VLCJ
 		try
 		{
 			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(),
 					new File("lib/libvlc" + System.getProperty("sun.arch.data.model")).getCanonicalPath());
 		}
 		catch (IOException e)
 		{
 			logger.log(Level.SEVERE, e.getLocalizedMessage());
 		}
 		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
 
 		// Lancement de la fenêtre
 		javax.swing.SwingUtilities.invokeLater(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				new GraphicalUserInterface();
 			}
 		});
 	}
 }
