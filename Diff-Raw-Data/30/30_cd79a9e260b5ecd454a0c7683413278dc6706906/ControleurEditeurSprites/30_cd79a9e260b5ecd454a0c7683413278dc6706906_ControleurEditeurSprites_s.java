 package com.arene.editeur.controleur;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Properties;
 
 import com.arene.editeur.listener.SelectionElementListener;
 import com.arene.editeur.modele.ConfigProjet;
 import com.arene.editeur.modele.SelectionCategorie;
 import com.arene.editeur.modele.SelectionElement;
 import com.arene.editeur.modele.SpriteTest;
 import com.arene.editeur.utils.file.FileTools;
 import com.arene.editeur.vue.ESSpriteCarac;
 import com.arene.editeur.vue.EditeurSprites;
 import com.arene.editeur.vue.PanneauSelection;
 
 /**
  * Le controleur de l'éditeur de sprites.
  * 
  * @author Camille
  */
 public class ControleurEditeurSprites implements SelectionElementListener
 {
 	/**
 	 * Le dossier racine du projet.
 	 */
 	private File dossierProjet = null;
 
 	/**
 	 * La configuration du projet.
 	 */
 	private ConfigProjet configProjet;
 
 	private SpriteTest spriteSelectionne;
 
 	private ControleurESSpriteCarac ctrlESSC;
 
 	private ControleurPanneauSelection ctrlPS;
 
 	/**
 	 * Constructeur avec la configuration du projet.
 	 * 
 	 * @param configProjet
 	 *        La configuration du projet.
 	 */
 	public ControleurEditeurSprites(ConfigProjet configProjet)
 	{
 		this.configProjet = configProjet;
 		this.dossierProjet = configProjet.getDossierProjet();
 	}
 
 	/**
 	 * Afficher l'éditeur à l'écran.
 	 */
 	public void afficherEditeur()
 	{
 		EditeurSprites editeurSprites = new EditeurSprites(this);
 		editeurSprites.setVisible(true);
 	}
 
 	/**
 	 * Créer le panneau de sélection.
 	 * 
 	 * @return Le panneau de sélection.
 	 */
 	public PanneauSelection creerPanneauSelection()
 	{
 		ctrlPS = new ControleurPanneauSelection();
 		ctrlPS.addSelectionElementListener(this);
 
 		ctrlPS.setCategories(creerCategories());
 
 		return ctrlPS.getPanneau();
 	}
 
 	private ArrayList<SelectionCategorie> creerCategories()
 	{
 		ArrayList<SelectionCategorie> categories =
 		        new ArrayList<SelectionCategorie>();
 
 		File dossierImages = new File(dossierProjet + "/images");
 		File[] dossiers = dossierImages.listFiles();
 
 		categories.add(new SelectionCategorie("nouveau",
 		        creerSprites(dossierImages)));
 		Properties types =
 		        FileTools.readConfig(new File(dossierProjet.getPath()
 		                + "/config" + "/types.config"));
 		Iterator it = types.values().iterator();
 
 		while (it.hasNext())
 		{
 			String type = (String) it.next();
 
 			String[] elementsNom = type.split("_");
 
 			if (elementsNom.length == 2)
 			{
 				categories.add(new SelectionCategorie(elementsNom[1],
 				        elementsNom[0], creerSprites(new File(dossierImages
 				                .getPath() + "/" + type))));
 			}
 		}
 		
 		return categories;
 	}
 
 	/**
 	 * Créer les sprites pour le panneau de sélection à partir d'un dossier.
 	 * 
 	 * @param dossier
 	 *        Le dossier contenant les images à transformer en sprites.
 	 * @return Les sprites créés.
 	 */
 	private ArrayList<SelectionElement> creerSprites(File dossier)
 	{
 		ArrayList<SelectionElement> elements =
 		        new ArrayList<SelectionElement>();
 		File[] images = dossier.listFiles();
 		for (File image : images)
 		{
 			if (image.isFile() && !image.getName().endsWith(".sprconf"))
 			{
 				elements.add(new SpriteTest(image, configProjet.getHauteur(),
 				        configProjet.getLargeur()));
 			}
 		}
 
 		return elements;
 	}
 
 	public ESSpriteCarac creerPanneauCarac()
 	{
 		ctrlESSC = new ControleurESSpriteCarac(dossierProjet, this);
 		return ctrlESSC.getPanneau();
 	}
 
 	@Override
 	public void elementSelectionne(SelectionElement element)
 	{
 		this.spriteSelectionne = (SpriteTest) element;
 		SelectionCategorie categorie = ctrlPS.getCategorieSelectionnee();
 		String nomCategorie = "";
 		String cheminImage = dossierProjet.getPath() + "/images" + "/";
 		if (!categorie.getId().equals("0"))
 		{
 			nomCategorie += categorie.getId() + "_" + categorie.getNom();
 			cheminImage += nomCategorie + "/";
 
 			Properties prop =
 			        FileTools.readConfig((FileTools.getFileMatching(new File(
 			                cheminImage), spriteSelectionne.getCode()
 			                + ".*.sprconf")));
 			spriteSelectionne.setType(prop.getProperty("type", ""));
 		}
 		cheminImage +=
		        spriteSelectionne.getCode() + "_" + spriteSelectionne.getNom()
		                + ".png";
 		ctrlESSC.selectionnerSprite(this.spriteSelectionne, new File(
 		        cheminImage));
 	}
 
 	public void updateCategories()
 	{
 		ArrayList<SelectionCategorie> categories = creerCategories();
 		SelectionCategorie categorieSelectionnee = new SelectionCategorie(null, null);
 		String nomCategorieSelectionnee = ctrlPS.getCategorieSelectionnee().getNom();
 		
 		for (SelectionCategorie categorie : categories)
 		{
 			if (categorie.getNom().equalsIgnoreCase(nomCategorieSelectionnee))
 			{
 				categorieSelectionnee = categorie;
 			}
 		}
 		
 		ctrlPS.updateCategories(categories, categorieSelectionnee);
 		ctrlESSC.selectionnerSprite(null, null);
 	}
 }
