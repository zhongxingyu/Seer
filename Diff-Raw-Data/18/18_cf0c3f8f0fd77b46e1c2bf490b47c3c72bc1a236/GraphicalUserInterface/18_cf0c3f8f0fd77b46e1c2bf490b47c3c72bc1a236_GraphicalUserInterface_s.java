 package mensonge.userinterface;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
 
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
 import javax.swing.UIManager;
 
 import com.sun.jna.Native;
 import com.sun.jna.NativeLibrary;
 
 import uk.co.caprica.vlcj.binding.LibVlc;
 import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 
 import mensonge.core.Extraction;
 import mensonge.core.BaseDeDonnees.BaseDeDonnees;
 import mensonge.core.BaseDeDonnees.DBException;
 import mensonge.core.plugins.Plugin;
 import mensonge.core.plugins.PluginManager;
 
 /**
  * 
  * Classe Interface graphique contenant tous les composants graphiques
  * 
  */
 public class GraphicalUserInterface extends JFrame implements ActionListener
 {
 	private static final long serialVersionUID = 5373991180139317820L;
 
 	private JTabbedPane onglets;
 
 	private JMenuItem aideAPropos;
 	private JMenuItem fichierFermer;
 	private JMenuItem fichierOuvrir;
 
 	private PanneauArbre panneauArbre;
 	private BaseDeDonnees bdd;
 
 	private JMenuBar menuBar;
 
 	private PluginManager pluginManager;
 
 	private JMenu menuOutils;
 
 	public GraphicalUserInterface()
 	{
 		/*
 		 * Connexion à la base
 		 */
 		connexionBase("LieLab.db");
 
 		this.panneauArbre = new PanneauArbre(bdd);
 		this.ajoutBarMenu();
 
 		/*
 		 * Conteneur
 		 */
 		this.onglets = new JTabbedPane();
		PanelWithBackground panel = new PanelWithBackground();
		panel.add(onglets);
		
 		JPanel conteneur = new JPanel(new BorderLayout());
 		conteneur.add(panel, BorderLayout.CENTER);
 		conteneur.add(panneauArbre, BorderLayout.EAST);
 
 		/*
 		 * Fenêtre
 		 */
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(true);
 		this.setTitle("LieLab");
 		this.setLocationRelativeTo(null);
 		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 		this.setContentPane(conteneur);
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
 		baseAjouterCategorie.addMouseListener(panneauArbre.new AjouterCategorieEnregistrementClicDroit(null, bdd));
 		baseAjouterSujet.addMouseListener(panneauArbre.new AjouterSujetClicDroit(null, bdd));
 
 		JMenu menuFichier = new JMenu("Fichier");
 		menuFichier.add(fichierOuvrir);
 		menuFichier.add(baseExporter);
 		menuFichier.add(baseImporter);
 		menuFichier.addSeparator();
 		menuFichier.add(fichierFermer);
 
 		this.aideAPropos = new JMenuItem("À propos");
 		this.aideAPropos.addActionListener(this);
 
 		JMenu menuAide = new JMenu("Aide");
 		menuAide.add(aideAPropos);
 
 		JMenu menuBase = new JMenu("Base");
 		menuBase.add(baseAjouterCategorie);
 		menuBase.add(baseAjouterSujet);
 
 		this.fichierFermer.setAccelerator(KeyStroke.getKeyStroke('Q', KeyEvent.CTRL_DOWN_MASK));
 		this.fichierOuvrir.setAccelerator(KeyStroke.getKeyStroke('O', KeyEvent.CTRL_DOWN_MASK));
 		baseImporter.setAccelerator(KeyStroke.getKeyStroke('I', KeyEvent.CTRL_DOWN_MASK));
 		baseExporter.setAccelerator(KeyStroke.getKeyStroke('E', KeyEvent.CTRL_DOWN_MASK));
 
 		this.menuBar = new JMenuBar();
 		this.menuBar.add(menuFichier);
 		this.ajoutMenuPlugins();
 		this.menuBar.add(menuBase);
 		this.menuBar.add(menuAide);
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
 		menuOutils.removeAll();
 		try
 		{
 			pluginManager.chargerPlugins();
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
 					item.addActionListener(new ItemPluginListener(mapPlugins.get(entry.getKey())));
 					menuOutils.add(item);
 				}
 			}
 		}
 		catch (ClassNotFoundException e)
 		{
 			GraphicalUserInterface.popupErreur("Impossible de charger les outils : " + e.getMessage());
 			menuOutils.add(new JMenuItem("Aucun outil"));
 		}
 		catch (InstantiationException e)
 		{
 			GraphicalUserInterface.popupErreur("Impossible de charger les outils : " + e.getMessage());
 			menuOutils.add(new JMenuItem("Aucun outil"));
 		}
 		catch (IllegalAccessException e)
 		{
 			GraphicalUserInterface.popupErreur("Impossible de charger les outils : " + e.getMessage());
 			menuOutils.add(new JMenuItem("Aucun outil"));
 		}
 		catch (IOException e)
 		{
 			GraphicalUserInterface.popupErreur("Impossible de charger les outils : " + e.getMessage());
 			menuOutils.add(new JMenuItem("Aucun outil"));
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
 	private void ajouterOnglet(OngletLecteur onglet)
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
 	 * Quitte le programme
 	 */
 	private void quitter()
 	{
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
 					popupErreur("Erreur lors de la création de la base de données : " + e1.getMessage());
 				}
 			}
 			else
 			{
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
 			JFileChooser fileChooser = new JFileChooser();
 			fileChooser.showOpenDialog(this);
 			if (fileChooser.getSelectedFile() != null)
 			{
 				try
 				{
 					this.ajouterOnglet(new OngletLecteur(new File(fileChooser.getSelectedFile().getCanonicalPath())));
 				}
 				catch (IOException e)
 				{
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
 
 	private class ExporterBaseListener implements ActionListener
 	{
 		private GraphicalUserInterface fenetre;
 
 		public ExporterBaseListener(GraphicalUserInterface g)
 		{
 			fenetre = g;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			JFileChooser fileChooser = new JFileChooser();
 			fileChooser.showOpenDialog(fenetre);
 			if (fileChooser.getSelectedFile() != null)
 			{
 				try
 				{
 					bdd.exporter(fileChooser.getSelectedFile().getCanonicalPath(), -1, 1);
 				}
 				catch (DBException e1)
 				{
 					popupErreur(e1.getMessage());
 
 				}
 				catch (IOException e1)
 				{
 					popupErreur(e1.getMessage());
 				}
 
 			}
 		}
 	}
 
 	private class ImporterBaseListener implements ActionListener
 	{
 		private GraphicalUserInterface fenetre;
 
 		public ImporterBaseListener(GraphicalUserInterface g)
 		{
 			fenetre = g;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			JFileChooser fileChooser = new JFileChooser();
 			fileChooser.showOpenDialog(fenetre);
 			String fichier;
 			if (fileChooser.getSelectedFile() != null)
 			{
 				try
 				{
 					fichier = fileChooser.getSelectedFile().getCanonicalPath();
 					bdd.importer(fichier);
 
 				}
 				catch (IOException e1)
 				{
 					popupErreur(e1.getMessage());
 
 				}
 				catch (DBException e2)
 				{
 					popupErreur(e2.getMessage());
 
 				}
 				// updateArbre();
 			}
 		}
 	}
 
 	private static class ItemPluginListener implements ActionListener
 	{
 		private static final Extraction EXTRACTION = new Extraction();
 		private Plugin plugin;
 
 		public ItemPluginListener(Plugin plugin)
 		{
 			this.plugin = plugin;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			// TODO Ajouter la liste des fichiers selectionnés
 			// Peut être voir pour aussi donner en plus l'instance de la BDD ça pourrait être utile au final ? :D
 			this.plugin.lancer(EXTRACTION, null);
 		}
 	}
 
 	private class ReloadPluginsListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			chargerListePlugins();
 		}
 	}
 
 	public static void main(String args[])
 	{
 		System.setProperty("awt.useSystemAAFontSettings", "on");
 		System.setProperty("swing.aatext", "true");
 		try
 		{
 			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
 		}
 		catch (Exception e)
 		{
 			// No Nimbus
 		}
 		try
 		{
 			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), new File("lib").getCanonicalPath());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
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
