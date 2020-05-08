 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 @SuppressWarnings("serial")
 public class BarreOutils extends JMenuBar {
 	private Curseur curseur;
 	private ZoneDessin zoneDessin;
 	
     private Controleur controleur;
     private JToggleButton boutonPoserCrayon;
     private JToggleButton boutonGomme;
     private JToggleButton boutonForme;
     private JSlider sliderEpaisseur;
     private JSlider sliderRed;
     private JSlider sliderGreen;
     private JSlider sliderBlue;
     private BarreOutilsVignette vignetteCouleur;
     /**
      *  Constructeur de la zone de bouton
      */
     
 	BarreOutils(final Curseur curseur, final ZoneDessin zoneDessin){
 		this.curseur = curseur;
 		this.zoneDessin = zoneDessin;
 		boutonPoserCrayon = boutonPoserCrayon();
 		boutonGomme = boutonGomme();
 		boutonForme = boutonForme();
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
 				if (curseur.getForme() == 0){
 					curseur.setForme(1);
 					zoneDessin.repaint();
 				}
 				else{
 					curseur.setForme(0);
 					zoneDessin.repaint();
 				}
 			}
 		});
         
         
         
     }
     
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
 	    	  curseur.setEpaisseur(((JSlider)event.getSource()).getValue());/*A ENLEVER PAR LA SUITE*/
 	    	  controleur.commande("cursorwidth " + ((JSlider)event.getSource()).getValue(), true);
 	    	  zoneDessin.repaint();
 	      }
 	    });
 	    
 	    return slider;
 	}
 	
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
 	
 	 /**
 	  * Fonction renvoyant le Bouton Lever/Poser le Crayon
 	  */
 	public JToggleButton boutonPoserCrayon(){
 		ImageIcon icon = new ImageIcon("img/crayon_pose.png");
 		JToggleButton bouton = new JToggleButton(icon);
 		bouton.setToolTipText("Poser l'outil");
 		bouton.setPreferredSize(new Dimension(40,40));
 		bouton.setMaximumSize(new Dimension(40,40));
 		bouton.setMinimumSize(new Dimension(40,40));
 		
 		if (curseur.isDown()) bouton.setSelected(true);
 		else bouton.setSelected(false);
 		//Return du bouton
 		return bouton;
 	}
 	
 	 /**
 	  * Fonction renvoyant le Bouton Crayon/Gomme
 	  */
 	public JToggleButton boutonGomme(){
 		ImageIcon icon = new ImageIcon("img/gomme.png");
 		JToggleButton bouton = new JToggleButton(icon);
 		bouton.setToolTipText("Utiliser la gomme");
 		bouton.setPreferredSize(new Dimension(40,40));
 		bouton.setMaximumSize(new Dimension(40,40));
 		bouton.setMinimumSize(new Dimension(40,40));
 		
 		if (curseur.getType() == 1) bouton.setSelected(true);
 		
 		//Return du bouton
 		return bouton;
 	}
 	
 	 /**
 	  * Fonction renvoyant le Bouton Forme Rond/Carre
 	  */
 	public JToggleButton boutonForme(){
 		ImageIcon icon = new ImageIcon("img/forme_carre.png");
 		JToggleButton bouton = new JToggleButton(icon);
 		bouton.setToolTipText("Utiliser une forme carré");
 		bouton.setPreferredSize(new Dimension(40,40));
 		bouton.setMaximumSize(new Dimension(40,40));
 		bouton.setMinimumSize(new Dimension(40,40));
 		
 		if (curseur.getForme() == 1) bouton.isSelected();
 		
 		//Return du bouton
 		return bouton;
 	}
 	
 	/**
 	 *  Fonction gérant l'interaction avec le bouton d'outil 
 	 *  Appelée lors d'un clic gauche sur le bouton d'outil ou lors d'un clic droit sur la zone de dessin
 	 */
 	public void interactionBoutonOutil(){
 		if (curseur.getType() == 0){
             controleur.commande("eraser", true);
 			zoneDessin.repaint();
 		}
 		else{
             controleur.commande("pencil", true);
 			zoneDessin.repaint();
 		}
 		if (curseur.getType() == 1) boutonGomme.setSelected(true);
  		else boutonGomme.setSelected(false);
 	}
 	
 	public void interactionBoutonPoserOutil(){
 		if (curseur.isDown()){
 			controleur.commande("penup", true);
 			zoneDessin.repaint();
 		}
 		else{
 			controleur.commande("pendown", true);
             zoneDessin.repaint();
 		}
 		if (curseur.isDown()) boutonPoserCrayon.setSelected(true);
  		else boutonPoserCrayon.setSelected(false);
 	}
 	
 	public void interactionSliderEpaisseur(int v){
 		sliderEpaisseur.setValue(sliderEpaisseur.getValue() + v);
 		controleur.commande("cursorwidth " + sliderEpaisseur.getValue(), true);
 		zoneDessin.repaint();
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
