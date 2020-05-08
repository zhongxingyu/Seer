 package com.arene.editeur.controleur;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import javax.swing.JComboBox;
 
 import com.arene.editeur.modele.SpriteTest;
 import com.arene.editeur.utils.file.FileTools;
 import com.arene.editeur.vue.ESSpriteCarac;
 
 public class ControleurESSpriteCarac
 {
 	private ControleurEditeurSprites parentControleur;
 	private ESSpriteCarac pCarac;
 	private File fichierImage;
 	private File dossierProjet;
 	private SpriteTest sprite;
 	private Properties types;
 
 	public ControleurESSpriteCarac(File dossierProjet, ControleurEditeurSprites parentControleur)
 	{
 		this.fichierImage = null;
 		this.parentControleur = parentControleur;
 		this.dossierProjet = dossierProjet;
 		types = new Properties();
 		try
 		{
 			File fichierConfig =
 			        new File(dossierProjet.getCanonicalPath()
 			                + "/config/types.config");
 
 			if (fichierConfig.exists())
 			{
 				types = FileTools.readConfig(fichierConfig);
 			}
 		}
 		catch (IOException e)
 		{
 			System.err
 			        .println("Erreur lors de l'accèss au chemin du dossier projet dans ControleurESSpriteCarac().");
 		}
 		pCarac = new ESSpriteCarac(this);
 	}
 
 	public void selectionnerSprite(SpriteTest spriteSelectionne,
 	        File fichierImage)
 	{
 		this.sprite = spriteSelectionne;
 		this.fichierImage = fichierImage;
 		pCarac.selectionnerSprite(sprite);
 	}
 
 	public ESSpriteCarac getPanneau()
 	{
 		return this.pCarac;
 	}
 
 	public void getTypes(JComboBox cbType)
 	{
 		cbType.addItem("");
 		Enumeration e = types.elements();
 		while (e.hasMoreElements())
 		{
 			cbType.addItem(((String) e.nextElement()).substring(3));
 		}
 		types.values();
 	}
 
 	public void sauverProprietes()
 	{
 		Enumeration e = types.elements();
 		String typeSprite = sprite.getType();
 		if (!typeSprite.isEmpty())
 		{
 			String typeId = "00";
 			String typeDossierNom = "";
 			while (e.hasMoreElements())
 			{
 				String type = (String) e.nextElement();
 				String typeNom = type.substring(3);
 				if (typeSprite.equals(typeNom))
 				{
 					typeId = type.substring(0, 2);
 					typeDossierNom = type;
 				}
 			}
 
 			String dernierNumero = recupererDernierNumero(typeId);
 			sprite.setCode(typeId + dernierNumero);
 			boolean deplacement = false;
 
 			String cheminDossier =
 			        dossierProjet.getPath() + "/images" + "/" + typeDossierNom
 			                + "/";
 			// déplacement du fichier
 			try
 			{
 				deplacement =
 				        FileTools.deplacer(fichierImage, new File(cheminDossier
				                + sprite.getFichierNomCree() + ".png"));
 				if (deplacement)
 				{
 					FileTools.saveConfig(
					        new File(cheminDossier + sprite.getFichierNomCree()
 					                + ".sprconf"), sprite.getProprietes());
 				}
 			}
 			catch (SecurityException se)
 			{
 				se.printStackTrace();
 			}
 
 			if (deplacement)
 			{
 				System.err.println("fichier sauvegardé");
 			}
 			else
 			{
 				System.err.println("erreur lors du déplacement du fichier");
 			}
 			parentControleur.updateCategories();
 		}
 		else
 		{
 			System.err.println("Pas de type sélectionné");
 		}
 
 	}
 
 	private String recupererDernierNumero(String categorieId)
 	{
 		File dossierCategorie =
 		        FileTools.getDirectoryBeginsWith(new File(dossierProjet.getPath() + "/images"), categorieId);
 		File[] fichiers = dossierCategorie.listFiles();
 
 		String dernierNumeroStr = "001";
 		int dernierNumero = 0;
 
 		for (File fichier : fichiers)
 		{
 			if (fichier.getName().endsWith(".png"))
 			{
 				int fichierNumero =
 				        new Integer(fichier.getName().substring(2, 5));
 				if (fichierNumero > dernierNumero)
 				{
 					dernierNumero = fichierNumero;
 				}
 			}
 		}
 		dernierNumero++;
 		// Ajout des 0
 		if (dernierNumero < 10)
 		{
 			dernierNumeroStr = "00" + dernierNumero;
 		}
 		else if (dernierNumero < 100)
 		{
 			dernierNumeroStr = "0" + dernierNumero;
 		}
 		else
 		{
 			dernierNumeroStr = "" + dernierNumero;
 		}
 
 		return dernierNumeroStr;
 	}
 }
