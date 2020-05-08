 package IHM.Menu;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.*;
 import java.util.Scanner;
 
 import javax.swing.*;
 
 import Main.*;
 import util.*;
 
 /*
  * Composition du Menu Contextuel
  * 
  * Projet						Page					Element
  *  ________________		 _______________		 _______________
  * | Nouveau ->		|		| Nouveau ->	|		| Nouveau ->	|
  * | Generer Projet	|		| Generer ->	|		| Generer ->	|
  * | Renommer		|		| Ajouter ->	|		| Ajouter ->	|
  * | Supprimer		|		| TODO nom		|		| TODO nom		|
  * | Propriete		|		| Renommer		|		| Modifier		|
  * |________________|		| Supprimer		|		| Supprimer		|
  * 							| Propriete		|		|_______________|
  * 							|_______________|		
  * 
  * 
  * Nouveau -> Projet
  * 			  Page
  * 
  * Generer -> Projet
  * 			  Page
  * 
  * Ajouter -> Titre
  * 			  Paragraphe
  * 			  Image
  * 
  * Changer Niveau  -> Monter
  * 			  		  Descendre
  */
 
 public class MenuContextuel implements ActionListener
 {
 	private JTree arbre;
 	private Object[] noeud;
 	private int location;
 	
 	private JMenuItem itemNouveauProjet;
 	private JMenuItem itemNouvellePage;
 	
 	private JMenuItem itemGenererProjet;
 	private JMenuItem itemGenererPage;
 	
 	private JMenuItem itemAjoutTitre;
 	private JMenuItem itemAjoutParagraphe;
 	private JMenuItem itemAjoutImage;
 	
 	private JMenuItem itemRenommer;
 	private JMenuItem itemModifier;
 	private JMenuItem itemPropriete;
 	private JMenuItem itemSupprimer;
 	
 	public MenuContextuel(MouseEvent me, int location, Object[] obj)
 	{
 		arbre = (me != null) ? (JTree) me.getSource(): null;
 		this.location = location;
 		noeud = (obj != null) ? obj : null;
 		
 		JPopupMenu jpm = new JPopupMenu();
 		
 		//Menu nouveau
 		JMenu menuNouveau = new JMenu("Nouveau");
 		menuNouveau.setIcon(new ImageIcon("images/filenew.png"));
 		
 		itemNouveauProjet = new JMenuItem("Nouveau Projet");
 		itemNouveauProjet.setIcon(new ImageIcon("images/project-new.png"));
 		itemNouveauProjet.addActionListener(this);
 		itemNouvellePage = new JMenuItem("Nouvelle Page");
 		itemNouvellePage.setIcon(new ImageIcon("images/page-new.png"));
 		itemNouvellePage.addActionListener(this);
 		
 		menuNouveau.add(itemNouveauProjet);
 		menuNouveau.add(itemNouvellePage);
 		
 		// item Generer
 		itemGenererProjet = new JMenuItem("Generer le projet");
 		itemGenererProjet.setIcon(new ImageIcon("images/generate.png"));
 		itemGenererProjet.addActionListener(this);
 		itemGenererPage = new JMenuItem("Generer la page");
 		itemGenererPage.setIcon(new ImageIcon("images/generate.png"));
 		itemGenererPage.addActionListener(this);
 		
 		// item ajouts
 		JMenu menuAjout = new JMenu("Ajouter");
 		menuAjout.setIcon(new ImageIcon("images/add.png"));
 		
 		itemAjoutTitre = new JMenuItem("Ajouter un titre");
 		itemAjoutTitre.addActionListener(this);
 		itemAjoutParagraphe = new JMenuItem("Ajouter un paragraphe");
 		itemAjoutParagraphe.addActionListener(this);
 		itemAjoutImage = new JMenuItem("Ajouter une image");
 		itemAjoutTitre.addActionListener(this);
 		
 		menuAjout.add(itemAjoutTitre);
 		menuAjout.add(itemAjoutParagraphe);
 		menuAjout.add(itemAjoutImage);
 		
 		// item Renommer
 		itemRenommer = new JMenuItem("Renommer");
 		itemRenommer.setIcon(new ImageIcon("images/rename.jpeg"));
 		itemRenommer.addActionListener(this);
 		
 		// item Modifier
 		itemModifier = new JMenuItem("Modifier");
 		itemModifier.setIcon(new ImageIcon("images/edit.png"));
 		itemModifier.addActionListener(this);
 		
 		// item Supprimer
 		itemSupprimer = new JMenuItem("Supprimer");
 		itemSupprimer.setIcon(new ImageIcon("images/delete.png"));
 		itemSupprimer.addActionListener(this);
 		
 		// item Propriete
 		itemPropriete = new JMenuItem("Proprietes");
 		itemPropriete.setIcon(new ImageIcon("images/properties.png"));
 		itemPropriete.addActionListener(this);
 		
 		/*
 		 * Menu pour un projet
 		 */
 		if (location == 2)
 		{
 			jpm.add(menuNouveau);
 			jpm.add(itemGenererProjet);
 			jpm.add(itemRenommer);
 			jpm.add(itemSupprimer);
 			jpm.add(itemPropriete);
 		}
 		/*
 		 * Menu pour une page
 		 */
 		else if (location == 3)
 		{
 			JMenu menuGenerer = new JMenu("Generer");
 			menuGenerer.setIcon(new ImageIcon("images/generate.png"));
 			itemGenererProjet.setIcon(null);
 			itemGenererPage.setIcon(null);
 			menuGenerer.add(itemGenererProjet);
 			menuGenerer.add(itemGenererPage);
 			
 			jpm.add(menuNouveau);
 			jpm.add(menuGenerer);
 			jpm.add(menuAjout);
 			jpm.add(itemRenommer);
 			jpm.add(itemSupprimer);
 			jpm.add(itemPropriete);
 		}
 		/*
 		 * Menu pour un element
 		 */
 		else if (location >= 3)
 		{
 			JMenu menuGenerer = new JMenu("Generer");
 			menuGenerer.setIcon(new ImageIcon("images/generate.png"));
 			itemGenererProjet.setIcon(null);
 			itemGenererPage.setIcon(null);
 			menuGenerer.add(itemGenererProjet);
 			menuGenerer.add(itemGenererPage);
 			
 			jpm.add(menuNouveau);
 			jpm.add(menuGenerer);
 			jpm.add(menuAjout);
 			jpm.add(itemModifier);
 			jpm.add(itemSupprimer);
 		}
 		
 		// on affiche le menuContextuel a la l'arbre
 		jpm.show(arbre, me.getX(), me.getY());
 	}
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		JMenuItem mi = (JMenuItem) e.getSource();
 		
 		// si c'est un projet
 		if (location == 2)
 		{
 			Projet projet = Controleur.metier.getProjet(noeud[1].toString());
 			
 			// proprietes
 			if (mi.equals(itemPropriete))
 				Controleur.creerPanelPropriete(projet);
 
 			if (mi.equals(itemSupprimer))
 			{
 				int option = Controleur.creerOptionPaneConfirm("Supprimez le projet", "Voulez-vous supprimez le projet ?");
 				if (option == JOptionPane.OK_OPTION)
 				{
 					// TODO code pour supprimer l'element
 				}
 			}
 		}
 		// si c'est un page
 		else if (location == 3)
 		{
 			Projet projet = Controleur.metier.getProjet(noeud[1].toString());
 			Page page = projet.getPage(noeud[2].toString());
 			
 			// Proprietes
 			if (mi.equals(itemPropriete))
 				Controleur.creerPanelPropriete(page);
 			else if (mi.equals(itemAjoutTitre))
 				Controleur.creerPanelAjouterTitre(0, "");
 			else if (mi.equals(itemAjoutParagraphe))
 				Controleur.creerPanelAjouterParagraphe(0, "");
 			else if (mi.equals(itemAjoutImage))
 				Controleur.creerPanelAjouterImage(0);
 		}
 		// si c'est un element
 		else if (location >= 3)
 		{
 			Projet projet = Controleur.metier.getProjet(noeud[1].toString());
 			Page page = projet.getPage(noeud[2].toString());
 			
 			Scanner sc = new Scanner(noeud[3].toString()).useDelimiter(" ");
 			String str = sc.next();
 			int indice = Integer.parseInt(sc.next());
 			
 			if (mi.equals(itemModifier))
 			{
 				if (str.equals("Titre"))
 				{
 					String ancienTitre = page.getAlTitre().get(indice-1);
 					Controleur.creerPanelAjouterTitre(1, ancienTitre);
 				}
 				if (str.equals("Paragraphe"))
 				{
 					String ancienParagraphe = page.getAlParagrapheHTML().get(indice-1);
 					Controleur.creerPanelAjouterParagraphe(1, ancienParagraphe);
 				}
 				if (str.equals("Image"))
 					Controleur.creerPanelAjouterImage(1);
 			}
 			else if (mi.equals(itemSupprimer))
 			{
 				String nomElement = noeud[3].toString();
 
 				int ind = page.getIndice(nomElement);
 				
 				if (ind != -1)
 					Controleur.fenetre.getArborescence().supprimerNoeud(noeud[2], ind);
 				
 				if (str.equals("Titre"))
 				{
 					page.getAlTitre().remove(indice-1);
 				}
 				if (str.equals("Paragraphe"))
 				{
 					page.getAlParagraphe().remove(indice-1);
 					page.getAlParagrapheHTML().remove(indice-1);
 				}
 				if (str.equals("Image"))
 				{
 					page.getAlImage().remove(indice-1);
 				}
				page.getAlOrdre().remove(ind);
 			}
 		}
 		
 		if (mi.equals(itemNouveauProjet))
 			Controleur.creerPanelCreerProjet();
 		else if (mi.equals(itemNouvellePage) && noeud[1] != null)
 			Controleur.creerPanelCreerPage();
 	}
 }
