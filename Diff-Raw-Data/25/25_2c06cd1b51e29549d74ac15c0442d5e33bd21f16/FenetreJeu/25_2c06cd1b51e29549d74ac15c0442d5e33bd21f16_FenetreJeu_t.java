 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.*;
 
 
 
 public class FenetreJeu extends JFrame{
 
 	// public Plateau plateau;
 	public Partie partie;
 	public FenetreMenu fm;
 	ClickAction listener;
 	public Panneau pan;
 	private Menu menu;
     private JPanel container = new JPanel();
 	private JPanel treePanel = new JPanel();
     private int x,y ;
 
 
 	//L'arbre de coups
 	public JTree arbre;
 	//Les deux variables de taille de fenetre
 	private final static int LARGEUR=758;
 	private final static int HAUTEUR=675;
 
     // La declaration pour le menu de la JMenuBar.    
 	private JMenuBar menuBar = new JMenuBar();
 
     private JMenu m_partie = new JMenu("Partie"),
 				  lancement  = new JMenu("Lancement"),
     		      forme      = new JMenu("Niveaux"),
     			  difficulte = new JMenu("Difficulte"),
 						edition		= new JMenu("Edition"),
     		      aPropos    = new JMenu("Info");
 
     private JMenuItem enregistrer = new JMenuItem("Enregistrer"),
 					  enregistrerSous = new JMenuItem("Enregistrer Sous"),
               		  charger = new JMenuItem("Charger"),
 										enregistrerPosition = new JMenuItem("Enregistrer position"),
 										chargerPosition = new JMenuItem("Charger Position"),
                       lancer = new JMenuItem("Lancer la partie"),
 	    			  arreter 	= new JMenuItem("Arreter la partie"),
 					  abandonner= new JMenuItem("Abandonner la partie"),
 	    			  quitter 	= new JMenuItem("Quitter"),
 							viderPlateau = new JMenuItem("Vider plateau"),
 							ajouterBille = new JMenuItem("Ajouter billes"),
 							supprimerBille = new JMenuItem("Supprimer billes"),
 							marquerBille = new JMenuItem("Marquer billes"),
 	    			  aProposItem = new JMenuItem("?");
 
 
     private JRadioButtonMenuItem 	facile    = new JRadioButtonMenuItem("Facile"),
     			    				moyen     = new JRadioButtonMenuItem("Moyen"),
     			    				difficile = new JRadioButtonMenuItem("Difficile"),
     			    				maitre    = new JRadioButtonMenuItem("Maitre");
 
     private ButtonGroup bg = new ButtonGroup();
 
     // La declaration pour le menu contextuel.
 	private JPopupMenu jpm   = new JPopupMenu();
 
     private JMenuItem launch = new JMenuItem("Lancer la partie");	            		
     private JMenuItem stop 	 = new JMenuItem("Arreter la partie");
 
 
     // creation des listener globaux.
     private StopPartieListener  stopPartie  = new StopPartieListener();
     private StartPartieListener startPartie = new StartPartieListener();
     
 		//Pour le JFileChooser
 			//Les partie seront sauvegardees dans le dossier sauvegarde
 			JFileChooser fileChooser = new JFileChooser("sauvegarde/");
 			JFileChooser fileChooserPos = new JFileChooser("positions/");
 			
 			//Les filtres
 			AbFileFilter filtre = new AbFileFilter(".ab", "Fichier Abalone");
 			AbFileFilter posFiltre = new AbFileFilter(".pos", "Fichier Abalone de position de depart");
 			File file;
 		
 
     // Creation de notre barre d'outils.
     private JToolBar toolBar = new JToolBar();
 
     //Les boutons
     private JButton 	play   = new JButton(new ImageIcon("images/play.jpg")),
     					cancel = new JButton(new ImageIcon("images/stop.jpg")),
     					cpu    = new JButton(new ImageIcon("images/ordinateur.jpg")),
     					player = new JButton(new ImageIcon("images/joueur.jpg")),
     					stat   = new JButton(new ImageIcon("images/stat.jpg"));
 
 	 //Icones de l'arbre
 	 private ImageIcon miniN = new ImageIcon("images/miniN.jpg");
 	 private ImageIcon miniB = new ImageIcon("images/miniB.jpg");
 
   	private Color fondBouton = Color.white;
 	private Color couleurFond = new Color(63,92,106);
 	//fenetres des scores
 	JTextField scoreJ1,scoreJ2;
 	//scores
 	int entScoreJ1,entScoreJ2;
 	//joueur actuel
 	int joueurActuel;
 	JTextField text1,text2;
 
 	/**
 	* Constructeur de la fenetre
 	*/
     public FenetreJeu(Partie partie){
 			super();
			Thread.currentThread().setPriority(7);
			
 			this.partie = partie;
 			this.setTitle("Abalone");
 			this.setSize(LARGEUR,HAUTEUR);
 			this.setResizable(false);
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			this.setLocationRelativeTo(null);
 			container.setBackground(couleurFond);
 			container.setLayout(new BorderLayout());
 			this.listener = new ClickAction(this);
 			this.pan = new Panneau(partie.plateau,listener);
 
 			pan.setBackground(couleurFond);
 
 				//On ajoute nos filtres sur la partie
 						this.fileChooser.addChoosableFileFilter(filtre);
 						this.fileChooserPos.addChoosableFileFilter(posFiltre);
 						
 				
         //On initialise le menu stop
         stop.setEnabled(false);
         //On affecte les ecouteurs
         stop.addActionListener(stopPartie);
 
     		launch.addActionListener(startPartie);
 
 			joueurActuel = partie.plateau.getJoueurActuel();
 			Box scoreBox = Box.createHorizontalBox();
 			//Affiche le score du joueur NOIR
 			text1 = new JTextField("Score Joueur 1 (N):");
 			if(joueurActuel == 1){
 				text1.setForeground(Color.GREEN);
 			}
 			text1.setEditable(false);
 
 			entScoreJ1 = partie.plateau.getScore(1);
 			String score1 = Integer.toString(entScoreJ1); 
 			scoreJ1 = new JTextField(score1);
 			scoreJ1.setEditable(false);
 
 			//Affiche le score du joueur BLANC
 			text2 = new JTextField("Score Joueur 2 (B):");
 			if(joueurActuel == 2){
 				text1.setForeground(Color.GREEN);
 			}
 			text2.setEditable(false);
 			entScoreJ2 = partie.plateau.getScore(2);
 			String score2 = Integer.toString(entScoreJ2); 
 			scoreJ2 = new JTextField(score2);
 			scoreJ2.setEditable(false);
 
 			scoreBox.add(text1);
 			scoreBox.add(scoreJ1);
 			scoreBox.add(text2);
 			scoreBox.add(scoreJ2);
 			
 			//On initialise le JTree avec le TreeModele de la partie
 			arbre = new JTree(partie.coups);
 			arbre.addTreeSelectionListener(new ChoixCoupArbre());
 			arbre.setPreferredSize(new Dimension(150,300));
 			arbre.setCellRenderer(new MonRenderer());
 			
 			treePanel.setPreferredSize(new Dimension(150,300));
 			treePanel.add(new JScrollPane(arbre), BorderLayout.CENTER); 	
 			
 			//initialisation finale de la fenetre
 			container.add(new JScrollPane(treePanel), BorderLayout.EAST);
 			container.add(scoreBox,BorderLayout.SOUTH);
       		container.add(pan, BorderLayout.CENTER);	
 			this.getContentPane().add(container);
             this.setContentPane(container);
             this.initMenu();
             this.initToolBar();
             this.setVisible(true);            
 
     }
 
 	/**
 	* methode pour rafrachir plateau avec le nouveau tableau
 	*/
 	public void rafraichir(Plateau plateau){
 		pan.rafraichir(plateau);
 		joueurActuel = plateau.getJoueurActuel();
 
 		if(joueurActuel == 1){
 			text1.setForeground(Color.GREEN);
 		}else{
 			text1.setForeground(Color.BLACK);
 		}
 		if(joueurActuel == 2){
 			text2.setForeground(Color.GREEN);
 		}else{
 			text2.setForeground(Color.BLACK);
 		}
 		entScoreJ1 = plateau.getScore(1);
 		entScoreJ2 = plateau.getScore(2);
 		scoreJ1.setText(Integer.toString(entScoreJ1));
 		scoreJ2.setText(Integer.toString(entScoreJ2));
 	}
 	
 	/**
 	* methode pour rafrachir le plateau avec le nouveau tableau avec billes selectionnees
 	*/
 	public void rafraichirBS1(int bille1){
 		pan.rafraichirBS1(bille1);
 	}
 	public void rafraichirBS2(int bille2, int bille3){
 		pan.rafraichirBS2(bille2,bille3);
 	}
 
 
 	/**
 	* methode pour initialiser la barre de raccourcis
 	*/
     private void initToolBar(){
 
     	this.cancel.setEnabled(false);
     	this.cancel.addActionListener(stopPartie);
     	this.cancel.setBackground(fondBouton);
     	this.play.addActionListener(startPartie);
     	this.play.setBackground(fondBouton);
 
     	this.toolBar.add(play);
     	this.toolBar.add(cancel);
     	this.toolBar.addSeparator();
 
     	//Ajout des Listeners
     	this.player.setBackground(fondBouton);
     	this.toolBar.add(player);
 
     	this.cpu.setBackground(fondBouton);
     	this.toolBar.add(cpu);
 
     	this.stat.setBackground(fondBouton);
     	this.toolBar.add(stat);
 
     	this.add(toolBar, BorderLayout.NORTH);
 
     }
 
 
 	/**
 	* methode pour initialiser la barre de menu
 	*/
     private void initMenu(){
 
     	//------------Menu partie---------------
 				//Enregistrer une partie
         enregistrer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
 				enregistrer.addActionListener(new ActionListener(){
 
 							public void actionPerformed(ActionEvent arg0) {
 								ObjectOutputStream oos ;
 								//S'il ne s'agit pas du premier enregistrement !
 								if(file != null){
 									try {
 
 										oos = new ObjectOutputStream(new FileOutputStream(file));
 										oos.writeObject(partie);
 										oos.close();
 
 									} catch (FileNotFoundException e) {
 										e.printStackTrace();
 									} catch (IOException e) {
 										e.printStackTrace();
 									}
 								}
 								//Sinon on demande le nom du fichier
 								else{
 									if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
 										file = fileChooser.getSelectedFile();
 										//Si l'extension est valide
 										if(fileChooser.getFileFilter().accept(file))
 										{
 											try {
 
 												oos = new ObjectOutputStream(new FileOutputStream(file));
 												oos.writeObject(partie);
 												oos.close();
 
 											} catch (FileNotFoundException e) {
 												e.printStackTrace();
 											} catch (IOException e) {
 												e.printStackTrace();
 											}
 										}						
 										else{
 											//Si extension invalide ! 
 											JOptionPane alert = new JOptionPane();
 											alert.showMessageDialog(null, "Erreur: votre fichier doit avoir l'extension .ab! \nVotre sauvegarde a echoue !", "Erreur", JOptionPane.ERROR_MESSAGE);
 										}						
 									}
 								}
 							}			
 				
 				});
 				//Enregistrer une partie sous
 				enregistrerSous.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
 				enregistrerSous.addActionListener(new ActionListener(){
 
 					public void actionPerformed(ActionEvent arg0) {
 						if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
 							file = fileChooser.getSelectedFile();
 							//Si l'extension est valide
 							if(fileChooser.getFileFilter().accept(file))
 							{
 								try {
 
 									ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
 									oos.writeObject(partie);
 									oos.close();
 
 								} catch (FileNotFoundException e) {
 									e.printStackTrace();
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 							}
 							else{
 								//Si vous n'avez pas specifie une extension valide ! 
 								JOptionPane alert = new JOptionPane();
 								alert.showMessageDialog(null, "Erreur: votre fichier doit avoir l'extension .ab! \nVotre sauvegarde a echoue !", "Erreur", JOptionPane.ERROR_MESSAGE);
 							}
 						}
 					}			
 				});
 				//Enregistrer l'etat d'une plateau comme un position de depart possible pour une partie
 				enregistrerPosition.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
 				enregistrerPosition.addActionListener(new ActionListener(){
 
 					public void actionPerformed(ActionEvent arg0) {
 						if(fileChooserPos.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
 							file = fileChooserPos.getSelectedFile();
 							//Si l'extension est valide
 							if(fileChooserPos.getFileFilter().accept(file))
 							{
 								try {
 
 									ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
 									//On enregistre uniquement la position des billes du pateau en cours
 									oos.writeObject(partie.plateau.cases);
 									oos.close();
 
 								} catch (FileNotFoundException e) {
 									e.printStackTrace();
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 							}
 							else{
 								//Si vous n'avez pas specifie une extension valide ! 
 								JOptionPane alert = new JOptionPane();
 								alert.showMessageDialog(null, "Erreur: votre fichier doit avoir l'extension .pos! \nVotre sauvegarde a echoue !", "Erreur", JOptionPane.ERROR_MESSAGE);
 							}
 						}
 					}			
 				});
 				//Charger une partie
 				charger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
 				charger.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent e){
 						if(fileChooser.showOpenDialog(null) ==JFileChooser.APPROVE_OPTION){
 							file = fileChooser.getSelectedFile();
 							if(fileChooser.getFileFilter().accept(file))
 							{
 								try {
 
 									ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
 									//On charge la partie
 									partie = ((Partie)ois.readObject());
 									ois.close();
 									//On rafraichit pour voir la partie chargee
 									rafraichir(partie.plateau);
 									arbre.setModel(partie.coups);
 									partie.coups.reload();
 									expandAll(arbre);
 
 								} catch (FileNotFoundException e1) {
 									e1.printStackTrace();
 								} catch (IOException e1) {
 									e1.printStackTrace();
 								} catch (ClassNotFoundException e2) {
 									e2.printStackTrace();
 								}
 							}
 							else{
 								JOptionPane alert = new JOptionPane();
 								alert.showMessageDialog(null, "Erreur d'extension de fichier ! \nVotre chargement a echoue !", "Erreur", JOptionPane.ERROR_MESSAGE);
 							}
 						}
 					}
 				});
 				//Charger une position
 				chargerPosition.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
 				chargerPosition.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent e){
 						if(fileChooserPos.showOpenDialog(null) ==JFileChooser.APPROVE_OPTION){
 							file = fileChooserPos.getSelectedFile();
 							if(fileChooserPos.getFileFilter().accept(file))
 							{
 								try {
 
 									ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
 									//On charge les nouvelles cases (une position ne change pas l'etat de la partie)
 									partie.plateau.cases = ((Case[])ois.readObject());
 									ois.close();
 									//On rafraichit pour voir la partie chargee
 									rafraichir(partie.plateau);
 									//On remplace le dernier coups par le coups charger
 									partie.dernierCoup.setUserObject(new Codage(partie.plateau));
 									partie.coups.reload();
 					
 
 								} catch (FileNotFoundException e1) {
 									e1.printStackTrace();
 								} catch (IOException e1) {
 									e1.printStackTrace();
 								} catch (ClassNotFoundException e2) {
 									e2.printStackTrace();
 								}
 							}
 							else{
 								JOptionPane alert = new JOptionPane();
 								alert.showMessageDialog(null, "Erreur d'extension de fichier ! \nVotre chargement a echoue !", "Erreur", JOptionPane.ERROR_MESSAGE);
 							}
 						}
 					}
 				});
 				
 				//On ajoute les sous menu au menu partie
 				m_partie.add(enregistrer);
 				m_partie.add(enregistrerSous);
 				m_partie.add(enregistrerPosition);
 				m_partie.add(charger);
 				m_partie.add(chargerPosition);
 				
 				
 			//--------------FIN Menu Partie-----------------
 			//--------------Menu Edition--------------------
 				//Vider le plateau de jeu
 				viderPlateau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
 				viderPlateau.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0){
 						//On vide le plateau de jeu
 						partie.plateau.viderPlateau();
 						//On rafraichit les donnees de l'arbre
 						partie.changementPlateau();
 						//On rafraichit graphiquement
 						rafraichir(partie.plateau);
 						
 					}
 				});
 				//Mode suppression de billes
 				ajouterBille.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
 				ajouterBille.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0){}
 				});
 				//Mode ajout de billes
 				supprimerBille.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
 				supprimerBille.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0){}
 				});
 				//Mode marquage de billes
 				marquerBille.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK));
 				marquerBille.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0){}
 				});
 				
 				//On ajoute les sous menu au menu edition
 				edition.add(viderPlateau);
 				edition.add(ajouterBille);
 				edition.add(supprimerBille);
 				edition.add(marquerBille);
 				
 			//--------------FIN Menu Edition----------------
 			//Menu Lancement
     	lancer.addActionListener(startPartie);
     	//On attribue l'accelerateur c
     	lancer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
     	lancement.add(lancer);
 
 
     	// Ajout du listener pour arrêter la partie
     	// listener a changer ici aussi
 
     	arreter.addActionListener(stopPartie);
     	arreter.setEnabled(false);
     	arreter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
     	lancement.add(arreter);
 
 			//Abandonner
 			abandonner.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0) {
 					
 					ImageIcon image = new ImageIcon("images/abandon.jpg");
 					JOptionPane jop = new JOptionPane();			
 					int option = jop.showConfirmDialog(null, "Voulez-vous vraiment abandonner ?", "", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, image);
 
 					if(option == JOptionPane.OK_OPTION){
 						partie.abandonner(joueurActuel);
 						String message = "La partie se termine par abandon.\n";
 						if(partie.gagnant == Partie.NOIR)
 							message+="Le joueur NOIR remporte la partie";
 						else
 							message+="Le joueur BLANC remporte la partie";
 					 	
 						jop.showMessageDialog(null, message); 
 						//retour au menu	
 						fm = new FenetreMenu();
 						dispose();					
 					}
 				}
 			});
 			lancement.add(abandonner);
     	// lancement.addSeparator();
     	quitter.addActionListener(new ActionListener(){
     		public void actionPerformed(ActionEvent event){
     			fm = new FenetreMenu();
 					dispose();
     		}
     	});
     	lancement.add(quitter);
 
 		  bg.add(facile);
     	bg.add(difficile);
     	bg.add(moyen);
     	bg.add(maitre);
 			
 			//Ajouter les niveau de difficultes
     	difficulte.add(facile);
     	difficulte.add(moyen);    	
     	difficulte.add(difficile);
     	difficulte.add(maitre);
     	
     	moyen.setSelected(true);
     	
     	forme.add(difficulte);
 
     	//menu à propos
 
     	//Ajout de ce que doit faire le "?"
     	aProposItem.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0) {
 					JOptionPane jop = new JOptionPane();
 					ImageIcon img = new ImageIcon("images/avatar.jpg");
 
 					String mess = "Salut ! \nIci c'est le projet Abalone !\n";
 
 					jop.showMessageDialog(null, mess, "Info", JOptionPane.INFORMATION_MESSAGE, img);
 
 				}    		    		
     	});
     	aPropos.add(aProposItem);
 
     	// Ajout des menus dans la barre de menus.
 			m_partie.setMnemonic('S');
 			menuBar.add(m_partie);
 			
 			edition.setMnemonic('E');
 			menuBar.add(edition);
 			
     	lancement.setMnemonic('L');
     	menuBar.add(lancement);
 
     	forme.setMnemonic('N');
     	menuBar.add(forme);
 
     	aPropos.setMnemonic('I');
     	menuBar.add(aPropos);
 
 
     	// Ajout de la barre de menus sur la fenêtre.
 
     	this.setJMenuBar(menuBar);
     }
 
 
 	 /**
 	 * Écouteur du menu Lancer.
 	*/
 	public class StartPartieListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent arg0) {
 
 			JOptionPane jop = new JOptionPane();			
 			int option = jop.showConfirmDialog(null, "Voulez-vous lancer la partie ?", "Lancement de la partie", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
 
 			if(option == JOptionPane.OK_OPTION)
 			{
 				lancer.setEnabled(false);
 				arreter.setEnabled(true);
 
 				//instruction pour le menu contextuel
 				launch.setEnabled(false);
 				stop.setEnabled(true);
 
 				play.setEnabled(false);
 				cancel.setEnabled(true);
 
 				// animated = true;
 				// t = new Thread(new PlayPartie());
 				// t.start();			
 			}
 		}		
 	}
 
 	 /**
 	 * Écouteur du menu Quitter.
 	 */
 	class StopPartieListener  implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 
 
 			JOptionPane jop = new JOptionPane();			
 			int option = jop.showConfirmDialog(null, "Voulez-vous arreter la partie ?", "Arret de la partie", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 
 			if(option != JOptionPane.NO_OPTION && option != JOptionPane.CANCEL_OPTION && option != JOptionPane.CLOSED_OPTION)
 			{
 				// animated = false;
 
 
 				// On remplace nos boutons par nos MenuItem
 				lancer.setEnabled(true);
 				arreter.setEnabled(false);
 
 				// instruction pour le menu contextuel
 				launch.setEnabled(true);
 				stop.setEnabled(false);
 
 				play.setEnabled(true);
 				cancel.setEnabled(false);
 
 			}
 		}		
 	}	
 	
 	/** Ecouteur de l'arbre*/
 	class ChoixCoupArbre implements TreeSelectionListener{
 		public void valueChanged(TreeSelectionEvent event) {
 						if(arbre.getLastSelectedPathComponent() != null){
 							partie.dernierCoup = (DefaultMutableTreeNode)(arbre.getLastSelectedPathComponent());							
 							partie.plateau = ((Codage)(partie.dernierCoup).getUserObject()).decodage();
 
 							rafraichir(partie.plateau);
 						}
 					}
 		
 	}
 	
 	
 	/** Derouler l'arbre*/
 	public void expandAll(JTree tree) {
 	    int row = 0;
 	    while (row < tree.getRowCount()) {
 	      tree.expandRow(row);
 	      row++;
 	      }
 	    }
 	
 	
 	
 	/** Permet de redefinir le DefaultTreeCellRenderer et d'avoir une modification d'icones de l'arbre personnalisee*/
 	private class MonRenderer extends DefaultTreeCellRenderer {
 
 	        public MonRenderer() {}
 
 	        public Component getTreeCellRendererComponent(
 	                            JTree tree,
 	                            Object value,
 	                            boolean sel,
 	                            boolean expanded,
 	                            boolean leaf,
 	                            int row,
 	                            boolean hasFocus) {
 
 	            super.getTreeCellRendererComponent(
 	                            tree, value, sel,
 	                            expanded, leaf, row,
 	                            hasFocus);
 	            if (estBilleNoir(value)) {
 	                setIcon(miniN);
 	            } else {
 	                setIcon(miniB);
 	            }
 
 	            return this;
 	        }
 
 	        protected boolean estBilleNoir(Object value) {
 	            DefaultMutableTreeNode noeud = (DefaultMutableTreeNode)value;
 	            Plateau infoNoeud = ((Codage)(noeud.getUserObject())).decodage();
 	            int numCoup = infoNoeud.numCoup;
 	            if (numCoup % 2 == 0) {
 	                return true;
 	            } 
 
 	            return false;
 	        }
 	    }
 	
 
 
 }
 
 /** Filtre pour le JFileChooser (sauvegarde et chargement de partie)*/
 class AbFileFilter extends FileFilter{
 
 	private String extension = ".ab", description = "Fichier Abalone";
 	public AbFileFilter(){}
 
 	public AbFileFilter(String ext, String descrip){
 		this.extension = ext;
 		this.description = descrip;
 	}
 
 	public boolean accept(File file){
 		return (file.isDirectory() || file.getName().endsWith(this.extension));
 	}
 
 	public String getDescription(){
 		return this.extension + " - " + this.description;
 	}	
 }
 
