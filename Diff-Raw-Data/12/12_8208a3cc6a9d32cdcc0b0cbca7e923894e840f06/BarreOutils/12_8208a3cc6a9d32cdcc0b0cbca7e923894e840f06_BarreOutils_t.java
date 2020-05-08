 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 @SuppressWarnings("serial")
 public class BarreOutils extends JToolBar {
 	private Curseur curseur;
 	private ZoneDessin zoneDessin;
 	
     private Controleur controleur;
     private JToggleButton boutonPoserCrayon;
     private JToggleButton boutonGomme;
     private JToggleButton boutonForme;
     private JButton boutonUndo;
     private JButton boutonRedo;
     private JSlider sliderEpaisseur;
     private JSlider sliderRed;
     private JSlider sliderGreen;
     private JSlider sliderBlue;
     private BarreOutilsVignette vignetteCouleur;
     /**
      *  Constructeur de barre d'outils
      *  @param curseur : Le curseur du programme
      *  @param zoneDessin : La zone de dessin du programme
      */
     
 	BarreOutils(final Curseur curseur, final ZoneDessin zoneDessin){
 		this.curseur = curseur;
 		this.zoneDessin = zoneDessin;
 		boutonPoserCrayon = boutonPoserCrayon();
 		boutonGomme = boutonGomme();
 		boutonForme = boutonForme();
 		boutonUndo = boutonUndo();
 		boutonRedo = boutonRedo();
 		sliderEpaisseur = sliderEpaisseur();
 		sliderRed = sliderRed();
 		sliderBlue = sliderBlue();
 		sliderGreen = sliderGreen();
 		vignetteCouleur = new BarreOutilsVignette(curseur);
 		vignetteCouleur.setMinimumSize(new Dimension(20, 20));
 		vignetteCouleur.setPreferredSize(new Dimension(20,20));
 		vignetteCouleur.setMaximumSize(new Dimension(20, 20));
 		
 		//Ajout des boutons		
 		
 		JPanel panPrincipal = new JPanel();
 		panPrincipal.setLayout(new BoxLayout(panPrincipal, BoxLayout.PAGE_AXIS));
 		
 		//Premiere ligne de la partie d'outils
 		
 		JPanel panOutils = new JPanel();
 		panOutils.setLayout(new BoxLayout(panOutils, BoxLayout.LINE_AXIS));
 		panOutils.add(Box.createRigidArea(new Dimension(5,0)));
 		panOutils.add(boutonPoserCrayon);
 		panOutils.add(Box.createRigidArea(new Dimension(5,0)));
 		panOutils.add(boutonGomme);
 		panOutils.add(Box.createRigidArea(new Dimension(5,0)));
 		panOutils.add(boutonForme);
 		panOutils.add(Box.createRigidArea(new Dimension(5,0)));
 		panOutils.add(boutonUndo);
 		panOutils.add(Box.createRigidArea(new Dimension(5,0)));
 		panOutils.add(boutonRedo);
 		
 		JPanel panSliderEpaisseur = new JPanel();
 		panSliderEpaisseur.setLayout(new BoxLayout(panSliderEpaisseur, BoxLayout.PAGE_AXIS));
 		JLabel labEpaisseur = new JLabel("Epaisseur");
 		panSliderEpaisseur.add(labEpaisseur);
 		panSliderEpaisseur.add(sliderEpaisseur);
 		panOutils.add(panSliderEpaisseur);
 		
 		//panOutils.setPreferredSize(new Dimension(this.getWidth(), 55));
 		
 		//Seconde ligne de la partie d'outils
 		
 		
 		JPanel panCurseur = new JPanel();
 		panCurseur.setLayout(new BoxLayout(panCurseur, BoxLayout.LINE_AXIS));
 		panCurseur.add(Box.createRigidArea(new Dimension(5,0)));
 		panCurseur.add(vignetteCouleur);
 		panCurseur.add(Box.createRigidArea(new Dimension(5,0)));
 		
 		JPanel panCurseurRouge = new JPanel();
 		panCurseurRouge.setLayout(new BoxLayout(panCurseurRouge, BoxLayout.PAGE_AXIS));
 		JLabel labRouge = new JLabel("Rouge");
 		panCurseurRouge.add(labRouge);
 		panCurseurRouge.add(sliderRed);
 		
 		JPanel panCurseurVert = new JPanel();
 		panCurseurVert.setLayout(new BoxLayout(panCurseurVert, BoxLayout.PAGE_AXIS));
 		JLabel labVert = new JLabel("Vert");
 		panCurseurVert.add(labVert);
 		panCurseurVert.add(sliderGreen);
 		
 		JPanel panCurseurBleu = new JPanel();
 		panCurseurBleu.setLayout(new BoxLayout(panCurseurBleu, BoxLayout.PAGE_AXIS));
 		JLabel labBleu = new JLabel("Bleu");
 		panCurseurBleu.add(labBleu);
 		panCurseurBleu.add(sliderBlue);
 		
 		panCurseur.add(panCurseurRouge);
 		panCurseur.add(panCurseurVert);
 		panCurseur.add(panCurseurBleu);
 		panCurseur.setPreferredSize(new Dimension(this.getWidth(), 35));
 		
 		panPrincipal.add(panOutils);
 		panPrincipal.add(panCurseur);
 		this.add(panPrincipal);
 		
 		
 		//Action des boutons
 		boutonPoserCrayon.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				interactionBoutonPoserOutil();
 			}
 		});
 		boutonGomme.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				interactionBoutonOutil();
 			}
 		});
 		boutonForme.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
                 controleur.commande("forme", true);
 			}
 		});
 		boutonRedo.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				interactionBoutonRedo();
 			}
 		});
 		boutonUndo.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				interactionBoutonUndo();
 			}
 		});
         
         
         
     }
     
 	/**Renvoit le Slider lié à l'épaisseur du curseur
 	 * @return Le JSlider lié à l'épaisseur du curseur
 	 */
 	public JSlider sliderEpaisseur(){
 		JSlider slider = new JSlider();
 		   
 	    slider.setMaximum(100);
 	    slider.setMinimum(1);
 	    slider.setValue(curseur.getEpaisseur());
 	    slider.setPaintTicks(true);
 	    slider.setPaintLabels(true);
 	    slider.setMinorTickSpacing(25);
 	    slider.setMajorTickSpacing(25);
 	    slider.addChangeListener(new ChangeListener(){
 	      public void stateChanged(ChangeEvent event){
 	    	  controleur.commande("cursorwidth " + ((JSlider)event.getSource()).getValue(), true);
 	    	  zoneDessin.repaint();
 	      }
 	    });
 	    
 	    return slider;
 	}
 
     /**Modifie le slider lors de la commande cursorwidth
      * @param width largeur
      */
     public void misAJourSliderEpaisseur(int width)
     {
    	ChangeListener[] tmp = sliderEpaisseur.getChangeListeners();
    	for ( ChangeListener listener : tmp )
    	{
    		sliderEpaisseur.removeChangeListener(listener);
    	}
    	
         sliderEpaisseur.setValue(width);
        
        for ( ChangeListener listener : tmp )
        {
        	sliderEpaisseur.addChangeListener(listener);
        }
     }
 	
 	/**Renvoit le Slider lié à la composante Rouge de la couleur du curseur
 	 * @return Le JSlider lié à la composante Rouge de la couleur du curseur
 	 */
 	public JSlider sliderRed(){
 		JSlider slider = new JSlider();
 		   
 	    slider.setMaximum(255);
 	    slider.setMinimum(0);
 	    slider.setValue(curseur.getCouleur().getRed());
 	    slider.setPaintTicks(true);
 	    slider.setPaintLabels(true);
 	    slider.addChangeListener(new ChangeListener(){
 	      public void stateChanged(ChangeEvent event){
 	    	  int r = ((JSlider)event.getSource()).getValue();
 	    	  controleur.commande("setcolor " + r + " " + curseur.getCouleur().getGreen()
                   + " " + curseur.getCouleur().getBlue(), true);
               zoneDessin.repaint();
 	    	  vignetteCouleur.repaint();
 	      }
 	    });	    
 	    return slider;
 	}
 	
 	/**Renvoit le Slider lié à la composante vert de la couleur du curseur
 	 * @return Le JSlider lié à la composante vert de la couleur du curseur
 	 */
 	public JSlider sliderGreen(){
 		JSlider slider = new JSlider();
 		   
 	    slider.setMaximum(255);
 	    slider.setMinimum(0);
 	    slider.setValue(curseur.getCouleur().getGreen());
 	    slider.setPaintTicks(true);
 	    slider.setPaintLabels(true);
 	    slider.addChangeListener(new ChangeListener(){
 	      public void stateChanged(ChangeEvent event){
 	    	  int g = ((JSlider)event.getSource()).getValue();
               controleur.commande("setcolor " + curseur.getCouleur().getRed() + " " + g
                   + " " + curseur.getCouleur().getBlue(), true);
 	    	  zoneDessin.repaint();
 	    	  vignetteCouleur.repaint();
 	      }
 	    });	    
 	    return slider;
 	}
 
 	/**Renvoit le Slider lié à la composante bleue de la couleur du curseur
 	 * @return Le JSlider lié à la composante bleue de la couleur du curseur
 	 */
 	public JSlider sliderBlue(){
 		JSlider slider = new JSlider();
 		   
 	    slider.setMaximum(255);
 	    slider.setMinimum(0);
 	    slider.setValue(curseur.getCouleur().getBlue());
 	    slider.setPaintTicks(true);
 	    slider.setPaintLabels(true);
 	    slider.addChangeListener(new ChangeListener(){
 	      public void stateChanged(ChangeEvent event){
 	    	  int b = ((JSlider)event.getSource()).getValue(); /* A ENLEVER */
               controleur.commande("setcolor " + curseur.getCouleur().getRed() + " "
                   + curseur.getCouleur().getGreen() + " " + b, true);
 	    	  zoneDessin.repaint();
 	    	  vignetteCouleur.repaint();
 	      }
 	    });	    
 	    return slider;
 	}
 
     /**Met à jour les sliders
      * @param red couleur rouge
      * @param green couleur verte
      * @param blue couleur bleue
      */
     public void misAJourSliderCouleur(int red, int green, int blue)
     {
         sliderRed.setValue(red);
         sliderGreen.setValue(green);
         sliderBlue.setValue(blue);
     }
 	
 	/**Fonction renvoyant le Bouton Lever/Poser l'outil
 	 * @return le bouton lever/popser l'outil*/
 	public JToggleButton boutonPoserCrayon(){
 	    ImageIcon iconBoutonPoserCrayon;
 		if(curseur.isDown())
 			iconBoutonPoserCrayon = new ImageIcon("../img/crayon_pose.png");
 		else
 			iconBoutonPoserCrayon = new ImageIcon("../img/crayon_leve.png");
 		JToggleButton bouton = new JToggleButton(iconBoutonPoserCrayon);
 		bouton.setToolTipText("Poser l'outil");
 		bouton.setPreferredSize(new Dimension(30,30));
 		bouton.setMaximumSize(new Dimension(30,30));
 		bouton.setMinimumSize(new Dimension(30,30));
 		
 		if (curseur.isDown()) bouton.setSelected(true);
 		else bouton.setSelected(false);
 		//Return du bouton
 		return bouton;
 	}
 	
 	/** Fonction renvoyant le bouton gomme
 	 * @return le bouton gomme*/
 	public JToggleButton boutonGomme(){
 		ImageIcon icon = new ImageIcon("../img/gomme.png");
 		JToggleButton bouton = new JToggleButton(icon);
 		bouton.setToolTipText("Utiliser la gomme");
 		bouton.setPreferredSize(new Dimension(30,30));
 		bouton.setMaximumSize(new Dimension(30,30));
 		bouton.setMinimumSize(new Dimension(30,30));
 		
 		if (curseur.getType() == 1) bouton.setSelected(true);
 		
 		//Return du bouton
 		return bouton;
 	}
 	
 	/**Fonction renvoyant le Bouton Forme Rond/Carre
 	 * @return le bouton forme Rond/Carre*/
 	public JToggleButton boutonForme(){
 		ImageIcon icon = new ImageIcon("../img/forme_carre.png");
 		JToggleButton bouton = new JToggleButton(icon);
 		bouton.setToolTipText("Utiliser une forme carré");
 		bouton.setPreferredSize(new Dimension(30,30));
 		bouton.setMaximumSize(new Dimension(30,30));
 		bouton.setMinimumSize(new Dimension(30,30));
 		
 		//if (curseur.getForme() == 1) bouton.isSelected();
 		
 		//Return du bouton
 		return bouton;
 	}
 	
 	/**Fonction renvoyant le Bouton "Revenir en arriere"
 	 * @return le bouton forme "Revenir en arriere"*/
 	public JButton boutonUndo(){
 		ImageIcon icon = new ImageIcon("../img/undo.png");
 		JButton bouton = new JButton(icon);
 		bouton.setToolTipText("Revenir en arrière");
 		bouton.setPreferredSize(new Dimension(30,30));
 		bouton.setMaximumSize(new Dimension(30,30));
 		bouton.setMinimumSize(new Dimension(30,30));
         bouton.setEnabled(false);
 
 		//Return du bouton
 		return bouton;
 	}
 	/**Fonction renvoyant le Bouton "Suivant"
 	 * @return le bouton forme "Suivant"*/
 	public JButton boutonRedo(){
 		ImageIcon icon = new ImageIcon("../img/redo.png");
 		JButton bouton = new JButton(icon);
 		bouton.setToolTipText("Action suivante");
 		bouton.setPreferredSize(new Dimension(30,30));
 		bouton.setMaximumSize(new Dimension(30,30));
 		bouton.setMinimumSize(new Dimension(30,30));
 	    bouton.setEnabled(false);
 
 		//Return du bouton
 		return bouton;
 	}
 	
 	/**Fonction gérant l'interaction avec le bouton d'outil 
 	 *  Appelée lors d'un clic gauche sur le bouton d'outil ou lors d'un clic droit sur la zone de dessin*/
 	public void interactionBoutonOutil(){
 		if (curseur.getType() == 0){
             controleur.commande("eraser", true);
 			zoneDessin.repaint();
 		}
 		else{
             controleur.commande("pencil", true);
 			zoneDessin.repaint();
 		}
 	}
 	
 	/**Fonction gérant l'interaction avec le bouton "poser l'outil"
 	 *  Appelée lors d'un clic gauche sur le bouton "poser l'outil" ou lors d'un clic du milieu sur la zone de dessin*/
 	public void interactionBoutonPoserOutil(){
 		if (curseur.isDown()){
 			controleur.commande("penup", true);
 		}
 		else{
 			controleur.commande("pendown", true);
 		}
 	}
 	
 	/**Fonction gérant l'interaction avec le bouton "revenir en arriere"*/
 	public void interactionBoutonUndo(){
 		controleur.commande("undo", true);
 	}
 
     /**Fonction desactivant le bouton si la fonction ne peut plus être lancé */
     public void disableBoutonUndo(){
         boutonUndo.setEnabled(false);
     }
     
     /**Fonction réactivant le bouton si la fonction peut être lancé */
     public void enableBoutonUndo(){
         boutonUndo.setEnabled(true);
     }
 
 	/**Fonction gérant l'interaction avec le bouton "revenir en arriere"*/
 	public void interactionBoutonRedo(){
 		controleur.commande("redo", true);
 	}
 
     /**Fonction desactivant le bouton si la fonction ne peut plus être lancé */
     public void disableBoutonRedo(){
         boutonRedo.setEnabled(false);
     }
     
     /**Fonction réactivant le bouton si la fonction peut être lancé */
     public void enableBoutonRedo(){
         boutonRedo.setEnabled(true);
     }
 	
 	/**Fonction gérant l'interaction avec le slider lié à l'épaisseur du curseur
 	 *  Appelée lors d'une modification de la valeur du curseur */
 	public void interactionSliderEpaisseur(int v){
 		sliderEpaisseur.setValue(sliderEpaisseur.getValue() + v);
 		controleur.commande("cursorwidth " + sliderEpaisseur.getValue(), true);
 	}
 	
     /**
      *  Modifieur du controleur
      *  @param c nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.controleur = c;
     }   
     
     /**Fonction gérant l'affichage du bouton "poser l'outil"*/
 	public void affichageBoutonPoserOutil(){
 		if(curseur.isDown()){
 			boutonPoserCrayon.setSelected(true);
 			boutonPoserCrayon.setIcon(new ImageIcon("../img/crayon_pose.png"));
 		}
 		else{
 			boutonPoserCrayon.setSelected(false);
 			boutonPoserCrayon.setIcon(new ImageIcon("../img/crayon_leve.png"));
 			
 		}
 	}
 	
 	
 	/**Fonction gérant l'affichage du bouton "gomme"*/
 	public void affichageBoutonOutil(){
 		if(curseur.getType() == 0){
 			boutonGomme.setSelected(false);
 		}
 		else
 			boutonGomme.setSelected(true);
 	}
 	
 	/**Fonction gérant l'affichage du bouton "forme du curseur"*/
 	public void affichageBoutonForme(){
 		if(curseur.getForme() == 0){
 			boutonForme.setSelected(false);
 		}
 		else{
 			boutonForme.setSelected(false);
 		}
 	}
 }
