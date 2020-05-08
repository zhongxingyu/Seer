 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 
 @SuppressWarnings("serial")
 public class BarreMenu extends JMenuBar{
 	
 	private Controleur controleur;
 	//JMenu "Fichier"
 	private JMenu menuFichier = new JMenu("Fichier");
 	private JMenuItem nouveau = new JMenuItem("Nouveau");
 	private JMenuItem ouvrirImage = new JMenuItem("Ouvrir une image");
 	private JMenuItem ouvrirHistorique = new JMenuItem("Ouvrir un historique");
 	private JMenuItem enregistrerImage = new JMenuItem("Enregistrer l'image");
 	private JMenuItem enregistrerHistorique = new JMenuItem("Enregistrer l'historique");
 	private JMenuItem quitter = new JMenuItem("Quitter");
     
     //JMenu "Outils"
 	private JMenu menuOutils = new JMenu("Outils");
 	private JMenuItem options = new JMenuItem("Paramètres");
 	private JMenuItem aPropos = new JMenuItem("A propos");
 	
     /**
      *  Constructeur de la Barre d'Outils
      */
 	BarreMenu(){
 		super();
 		
 		//Menu "Fichier"
 		this.add(menuFichier);
 		menuFichier.add(nouveau);
 		menuFichier.add(ouvrirHistorique);
 		menuFichier.add(ouvrirImage);
         menuFichier.add(enregistrerHistorique);
         menuFichier.add(enregistrerImage);
 		menuFichier.add(quitter);
 		
 		//Menu "Outils"
 		this.add(menuOutils);
 		menuOutils.add(options);
 		menuOutils.add(aPropos);
 		
 		//Ajout des raccourcis
 		//menuFichier.setMnemonic('F');
 		menuOutils.setMnemonic('O');
 		nouveau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
 		ouvrirHistorique.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
 		ouvrirImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
 		enregistrerHistorique.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
 		enregistrerImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
 		quitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
 		options.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
 		aPropos.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_MASK));
 		
 		//Ajout des Action Listener
 		nouveau.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				nouveau();
 			}
 		});
 		ouvrirImage.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				ouvrirImage();
 			}
 		});
 		ouvrirHistorique.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				ouvrirHistorique();
 			}
 		});
 		enregistrerImage.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				enregistrerImage();
 			}
 		});
 		enregistrerHistorique.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				enregistrerHistorique();
 			}
 		});
 		quitter.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				quitter();
 			}
 		});
 		options.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				MenuOption menuOption = new MenuOption(null, "Option", true);
 				
 			}
 		});
 		aPropos.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				aPropos();
 			}
 		});
 		
 	}
 	
 	private void nouveau(){
 		controleur.commande("new", true);
 	}
 	private void ouvrirImage(){
 		controleur.commande("open", true);
 	}
 	private void ouvrirHistorique(){
 		controleur.commande("exec", true);
 	}	
 	private void enregistrerImage(){
 		controleur.commande("save", true);
 	}
 	
 	private void enregistrerHistorique(){
 		controleur.commande("savehistory", true);
 	}
 	
 	/**Entre la commande "exit" dans le terminal, pour fermer le programme*/
 	public void quitter(){
         controleur.commande("exit", false);
 	}
 	
 	private void aPropos(){
 		JOptionPane.showMessageDialog(null,
 								"Carapuce est un projet Universitaire développé par 4 étudiants :\n\n" +
 								"Mehdi Khelifi\n" +
 								"Loïc Runarvot\n" +
								"Gauthier Lo\n",
 								"A propos",
 								JOptionPane.INFORMATION_MESSAGE);
 	}
 	
     /**
      *  Modifieur du controleur
      *  @param c nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.controleur = c;
     } 
 }
