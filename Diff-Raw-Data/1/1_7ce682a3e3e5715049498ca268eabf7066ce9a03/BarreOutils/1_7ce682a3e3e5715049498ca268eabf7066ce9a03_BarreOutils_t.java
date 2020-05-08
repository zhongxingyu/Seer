 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 @SuppressWarnings("serial")
 public class BarreOutils extends JMenuBar {
 	private Curseur curseur;
 	private ZoneDessin zoneDessin;
 	
     private Controleur controleur;
     private JButton boutonPoserCrayon;
     private JButton boutonGomme;
     private JSlider slider;
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
 		slider = slider();
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
 		
 		JPanel panOutils = new JPanel();
 		panOutils.setLayout(new BoxLayout(panOutils, BoxLayout.LINE_AXIS));
 		panOutils.add(Box.createRigidArea(new Dimension(5,0)));
 		panOutils.add(boutonPoserCrayon);
 		panOutils.add(Box.createRigidArea(new Dimension(5,0)));
 		panOutils.add(boutonGomme);
 		panOutils.add(slider);
 		
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
 				if (curseur.isDown()){
 					boutonPoserCrayon.setText("Poser l'outil");
 					curseur.setIsDown(false);
 					zoneDessin.repaint();
 				}
 				else{
 					boutonPoserCrayon.setText("Lever l'outil");
 					curseur.setIsDown(true);
 					zoneDessin.repaint();
 
 				}
 			}
 		});
 		boutonGomme.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				if (curseur.getType() == 0){
 					boutonGomme.setText("Crayon");
 					curseur.setType(1);
 					zoneDessin.repaint();
 				}
 				else{
 					boutonGomme.setText("Gomme");
 					curseur.setType(0);
 					zoneDessin.repaint();
 				}
 			}
 		});
         
         
         
     }
     
 	public JSlider slider(){
 		JSlider slider = new JSlider();
 		   
 	    slider.setMaximum(100);
 	    slider.setMinimum(0);
 	    slider.setValue(curseur.getEpaisseur());
 	    slider.setPaintTicks(true);
 	    slider.setPaintLabels(true);
 	    slider.setMinorTickSpacing(25);
 	    slider.setMajorTickSpacing(25);
 	    slider.addChangeListener(new ChangeListener(){
 	      public void stateChanged(ChangeEvent event){
 	    	  curseur.setEpaisseur(((JSlider)event.getSource()).getValue());
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
 	    	  curseur.setCouleurRouge(((JSlider)event.getSource()).getValue());
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
 	    	  curseur.setCouleurVert(((JSlider)event.getSource()).getValue());
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
 	    	  curseur.setCouleurBleu(((JSlider)event.getSource()).getValue());
 	    	  zoneDessin.repaint();
 	    	  vignetteCouleur.repaint();
 	      }
 	    });	    
 	    return slider;
 	}
 	
 	 /**
 	  * Fonction renvoyant le Bouton Lever/Poser le Crayon
 	  */
 	public JButton boutonPoserCrayon(){
 		JButton bouton = new JButton();
 		//Texte contenu dans le bouton
 		if (curseur.isDown()) bouton.setText("Lever le crayon");
 		else bouton.setText("Poser le Crayon");
 		
 		//Return du bouton
 		return bouton;
 	}
 	
 	 /**
 	  * Fonction renvoyant le Bouton Crayon/Gomme
 	  */
 	public JButton boutonGomme(){
 		JButton bouton = new JButton();
 		//Texte contenu dans le bouton
 		if (curseur.getType() == 1) bouton.setText("Crayon");
 		else bouton.setText("Gomme");
 		
 		//Return du bouton
 		return bouton;
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
