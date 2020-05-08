 /******************************************************
 Cours:  LOG121
 Projet: Squelette du laboratoire #1
 Nom du fichier: MenuFenetre.java
 Date créé: 2013-05-03
  *******************************************************
 Historique des modifications
  *******************************************************
  *@author Patrice Boucher
 2013-05-03 Version initiale
  *******************************************************/
 
 import javax.swing.*;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 
 /**
  * Crée le menu de la fenêtre de l'applicationé
  */
 public class MenuFenetre extends JMenuBar {
 
 	private static final long serialVersionUID = 1536336192561843187L;
 
 	private static final int MENU_FICHIER_QUITTER_TOUCHE_MASK = ActionEvent.CTRL_MASK;
 	private static final char MENU_FICHIER_QUITTER_TOUCHE_RACC = KeyEvent.VK_Q;
 
 	private static final int MENU_FICHIER_OBTENIR_FORMES_TOUCHE_MASK = ActionEvent.CTRL_MASK;
 	private static final char MENU_FICHIER_OBTENIR_FORMES_TOUCHE_RACC = KeyEvent.VK_O;
 
 	private static final String MENU_FICHIER_TITRE = "app.frame.menus.file.title",
 			MENU_FICHIER_OBTENIR_FORMES = "app.frame.menus.file.obtenir",
 			MENU_FICHIER_QUITTER = "app.frame.menus.file.exit",
 			MENU_SORT_TITRE = "app.frame.menus.sort.title",
 			MENU_SORT_AUCUN = "app.frame.menus.sort.aucun",
 			MENU_SORT_AIRE_ASC = "app.frame.menus.sort.aire.asc",
 			MENU_SORT_AIRE_DESC = "app.frame.menus.sort.aire.desc",
 			MENU_SORT_DISTANCE_MAXIMALE_ASC = "app.frame.menus.sort.distancemax.asc",
 			MENU_SORT_DISTANCE_MAXIMALE_DESC = "app.frame.menus.sort.distancemax.desc",
 			MENU_SORT_NO_SEQUENTIEL_ASC = "app.frame.menus.sort.noseq.asc",
 			MENU_SORT_NO_SEQUENTIEL_DESC = "app.frame.menus.sort.noseq.desc",
 			MENU_SORT_TYPE_FORME_ASC = "app.frame.menus.sort.typeforme.asc",
 			MENU_SORT_TYPE_FORME_DESC = "app.frame.menus.sort.typeforme.desc",
 			MENU_SORT_HAUTEUR_ASC = "app.frame.menus.sort.hauteur.asc",
 			MENU_SORT_HAUTEUR_DESC = "app.frame.menus.sort.hauteur.desc",
 			MENU_SORT_LARGEUR_ASC = "app.frame.menus.sort.largeur.asc",
 			MENU_SORT_LARGEUR_DESC = "app.frame.sort.largeur.desc",
 			MENU_AIDE_TITRE = "app.frame.menus.help.title",
 			MENU_AIDE_PROPOS = "app.frame.menus.help.about",
 			DIALOG_CONNEXION_MSG = "app.frame.menus.connect.msg",
 			DIALOG_CONNEXION_ERR_UNKNOWNHOST = "app.frame.menus.connect.err.unknownhost",
 			DIALOG_CONNEXION_ERR_MALFORMED_URL = "app.frame.menus.connect.err.malformedurl",
 			DIALOG_CONNEXION_ERR_IO = "app.frame.menus.connect.err.io",
 			DIALOG_CONNEXION_ERR = "app.frame.menus.connect.err";
 
 	private static final String MESSAGE_DIALOGUE_A_PROPOS = "app.frame.dialog.about";
 
 	private static final int DELAI_QUITTER_MSEC = 200;
 
 	private final CommBase comm;
 
 	private final FenetreFormes fenetreFormes;
 	
 	/**
 	 * Constructeur
 	 * @param fenetreFormes 
 	 */
 	public MenuFenetre(CommBase comm, FenetreFormes fenetreFormes) {
 		this.comm = comm;
 		this.fenetreFormes = fenetreFormes;
 		addMenuFichier();
 		addMenuSort();
 		addMenuAide();
 	}
 
 	/**
 	 * Initialise la connexion avec le serveur
 	 */
 	private void startConnection() {
 		String addr = "";
 		try {
 			addr = JOptionPane.showInputDialog(this,
 					LangueConfig.getResource(DIALOG_CONNEXION_MSG),
 					"localhost:10000");
 
 			if (addr == null)
 				return;
 
 			fenetreFormes.effacerTout();
 			comm.start(addr);
 		} catch (UnknownHostException ex) {
 			displayErrorMessage(DIALOG_CONNEXION_ERR_UNKNOWNHOST);
 		} catch (MalformedURLException ex) {
 			displayErrorMessage(DIALOG_CONNEXION_ERR_MALFORMED_URL);
 		} catch (IOException ex) {
 			displayErrorMessage(DIALOG_CONNEXION_ERR_IO);
 		} catch (Exception ex) {
 			displayErrorMessage(DIALOG_CONNEXION_ERR);
 		}
 	}
 
 	private void displayErrorMessage(String msgKey) {
 		JOptionPane.showMessageDialog(this,
 				LangueConfig.getResource(msgKey));
 		startConnection();
 	}
 
 	/**
 	 * Créer le menu "File".
 	 */
 	protected void addMenuFichier() {
 		JMenu menu = creerMenu(MENU_FICHIER_TITRE, new String[] {
 				MENU_FICHIER_OBTENIR_FORMES, MENU_FICHIER_QUITTER });
 
 		menu.getItem(0).addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				startConnection();
 			}
 		});
 		menu.getItem(0).setAccelerator(
 				KeyStroke.getKeyStroke(MENU_FICHIER_OBTENIR_FORMES_TOUCHE_RACC,
 						MENU_FICHIER_OBTENIR_FORMES_TOUCHE_MASK));
 
 		menu.getItem(1).addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					comm.stop();
 					Thread.sleep(DELAI_QUITTER_MSEC);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 				System.exit(0);
 			}
 		});
 		menu.getItem(1).setAccelerator(
 				KeyStroke.getKeyStroke(MENU_FICHIER_QUITTER_TOUCHE_RACC,
 						MENU_FICHIER_QUITTER_TOUCHE_MASK));
 		add(menu);
 	}
 
 	protected void addMenuSort() {
 		JMenu menu = creerMenu(MENU_SORT_TITRE, new String[] {});
 		
 		ButtonGroup group = new ButtonGroup();
 		
 		JRadioButtonMenuItem noneBtn = creerSortRadioButtonMenuItem(menu, group, MENU_SORT_AUCUN, FormeComparatorType.AUCUN);
 		noneBtn.setSelected(true);
 		
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_AIRE_ASC, FormeComparatorType.AIRE_ASC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_AIRE_DESC, FormeComparatorType.AIRE_DESC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_DISTANCE_MAXIMALE_ASC, FormeComparatorType.DISTANCE_MAXIMALE_ASC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_DISTANCE_MAXIMALE_DESC, FormeComparatorType.DISTANCE_MAXIMALE_DESC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_NO_SEQUENTIEL_ASC, FormeComparatorType.NO_SEQUENTIEL_ASC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_NO_SEQUENTIEL_DESC, FormeComparatorType.NO_SEQUENTIEL_DESC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_TYPE_FORME_ASC, FormeComparatorType.TYPE_FORME_ASC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_TYPE_FORME_DESC, FormeComparatorType.TYPE_FORME_DESC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_HAUTEUR_ASC, FormeComparatorType.HAUTEUR_ASC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_HAUTEUR_DESC, FormeComparatorType.HAUTEUR_DESC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_LARGEUR_ASC, FormeComparatorType.LARGEUR_ASC);
 		creerSortRadioButtonMenuItem(menu, group, MENU_SORT_LARGEUR_DESC, FormeComparatorType.LARGEUR_DESC);
 		
 		add(menu);
 	}
 
 	/**
 	 * Créer le menu "Help".
 	 */
 	private void addMenuAide() {
 		JMenu menu = creerMenu(MENU_AIDE_TITRE,
 				new String[] { MENU_AIDE_PROPOS });
 		menu.getItem(0).addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(null,
 						LangueConfig.getResource(MESSAGE_DIALOGUE_A_PROPOS),
 						LangueConfig.getResource(MENU_AIDE_PROPOS),
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 		add(menu);
 	}
 
 	/**
 	 * Créer un élément de menu à partir d'un champs principal et ses éléments
 	 * 
 	 * @param titleKey
 	 *            champs principal
 	 * @param itemKeys
 	 *            éléments
 	 * @return le menu
 	 */
 	private static JMenu creerMenu(String titleKey, String[] itemKeys) {
 		JMenu menu = new JMenu(LangueConfig.getResource(titleKey));
 		for (String itemKey : itemKeys) {
 			menu.add(new JMenuItem(LangueConfig.getResource(itemKey)));
 		}
 		return menu;
 	}	
 	
 	private JRadioButtonMenuItem creerSortRadioButtonMenuItem(JComponent menu, ButtonGroup group, String titleKey, final FormeComparatorType typeComparator)
 	{
 		JRadioButtonMenuItem btn = new JRadioButtonMenuItem(LangueConfig.getResource(titleKey));
 		group.add(btn);
 		menu.add(btn);
 				
 		final FormeComparatorFactory factory = new FormeComparatorFactory();
 		btn.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				fenetreFormes.setComparator(factory.getComparator(typeComparator));
 			}
 		});
 		
 		return btn;		
 	}
 
 }
