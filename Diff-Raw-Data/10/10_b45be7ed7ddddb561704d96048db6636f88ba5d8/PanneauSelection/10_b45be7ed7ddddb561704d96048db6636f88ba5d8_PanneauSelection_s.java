 package com.arene.editeur.vue;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 import com.arene.editeur.controleur.ControleurPanneauSelection;
 import com.arene.editeur.modele.SelectionCategorie;
 import com.arene.editeur.modele.SelectionElement;
 
 @SuppressWarnings("serial")
 public class PanneauSelection extends JPanel
 {
 	private ControleurPanneauSelection ctrlPS;
 	
 	private JScrollPane spCategories;
 	private JScrollPane spElements;
 	private JPanel pCategories = new JPanel();
 	private JPanel pElements = new JPanel();
 	private JButton categorieSelectionnee;
 	
 	public PanneauSelection(ControleurPanneauSelection ctrlPS)
 	{
 		this.ctrlPS = ctrlPS;
 		
 		// Bordure du panneau Selection
 		this.setBorder(BorderFactory.createRaisedBevelBorder());
 		
 		this.setLayout(new BorderLayout());
 		pCategories = new JPanel();
 		pElements = new JPanel();
 		
 		spCategories = creerJSP("Catégories", pCategories);
 		spElements = creerJSP("Élements", pElements);
 		
 		verifierNbreCategories();
 		this.add(spElements, BorderLayout.CENTER);
 	}
 	
 	private JScrollPane creerJSP(String titre, JPanel contenu)
 	{
 		JScrollPane jsp = new JScrollPane(contenu);
 		// Bordure des panneaux internes
 		jsp.setBorder(new TitledBorder(BorderFactory.createLoweredBevelBorder(), titre));
 		
 		// Barres de défilement
 		jsp
         .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		jsp
         .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
 		return jsp;
 	}
 
 	public void updateCategories(ArrayList<String> categories)
     {
 		pCategories.removeAll();
 		categorieSelectionnee = null;
 	    for (String categorie : categories)
 	    {
 	    	JButton bCategorie = new JButton(categorie);
 	    	bCategorie.addActionListener(new ActionListener(){
 
 				@Override
                 public void actionPerformed(ActionEvent arg0)
                 {
 					JButton boutonClique = ((JButton)arg0.getSource());
 	                if (categorieSelectionnee != null)
 	                {
 	                	categorieSelectionnee.setEnabled(true);
 	                }
 	                
 	                categorieSelectionnee = boutonClique;
 	                boutonClique.setEnabled(false);
 	                ctrlPS.selectionnerCategorie(boutonClique.getText());
                 }
 	    		
 	    	});
 	    	pCategories.add(bCategorie);
 	    }
 	    
 	    verifierNbreCategories();
 	    pCategories.revalidate();
 	    pCategories.repaint();
 	    spCategories.revalidate();
 	    spCategories.repaint();
 	    this.revalidate();
 	    this.repaint();
 	}
 	
 	private void verifierNbreCategories()
 	{
 		int nbreCategories = ctrlPS.getNbreCategories();
 		if (nbreCategories > 1)
 		{
 			this.add(spCategories, BorderLayout.NORTH);
 		}
 	}
 
 	public void afficherElements(ArrayList<SelectionElement> elements)
     {
 		pElements.removeAll();
 	    for (SelectionElement element : elements)
 	    {
 	    	JButton bElement = new JButton(element.getIcone());
 	    	bElement.setName(element.getNom());
 	    	bElement.addActionListener(new ActionListener(){
 
 				@Override
                 public void actionPerformed(ActionEvent arg0)
                 {
 					JButton boutonClique = ((JButton)arg0.getSource());
	                if (categorieSelectionnee != null)
 	                {
	                	categorieSelectionnee.setEnabled(true);
 	                }
 	                
	                categorieSelectionnee = boutonClique;
 	                boutonClique.setEnabled(false);
 	                ctrlPS.selectionnerElement(boutonClique.getName());
                 }
 	    		
 	    	});
 	    	pElements.add(bElement);
 	    }
 	    
 	    pElements.revalidate();
 	    pElements.repaint();
 	    spElements.revalidate();
 	    spElements.repaint();
 	    this.revalidate();
 	    this.repaint();
     }
 
 	public void deselectionnerCategories()
     {
 		this.categorieSelectionnee.setEnabled(true);
 		this.categorieSelectionnee = null;
 		this.afficherElements(null);
     }
 }
