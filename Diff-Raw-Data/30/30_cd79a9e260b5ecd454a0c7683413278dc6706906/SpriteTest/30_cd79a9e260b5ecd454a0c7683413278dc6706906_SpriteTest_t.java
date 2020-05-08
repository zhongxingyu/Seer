 package com.arene.editeur.modele;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 public class SpriteTest implements SelectionElement
 {
 	private Image image;
 	private String nom;
 	private int largeur;
 	private int hauteur;
 	private BufferedImage imageAffichee;
 	private String code;
 	private String fichierNomOrigine;
 	
 	private Properties proprietes;
 	
 	public SpriteTest(File file, int hauteur, int largeur)
 	{
 		try
         {
 	        image = ImageIO.read(file);
 	        fichierNomOrigine = file.getName();
 	        String[] fileName = fichierNomOrigine.split("_|\\.");
 	        if (fileName.length == 3)
 	        {
 	        	nom = fileName[1];
 	        	code = fileName[0];
 	        } else
 	        {
 	        	nom = fileName[0];
 	        	code = "00000";
 	        }
         }
         catch (IOException e)
         {
 	        e.printStackTrace();
         }
 		
 		this.hauteur = hauteur;
 		this.largeur = largeur;
 		imageAffichee = new BufferedImage(this.largeur, this.hauteur, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2D = imageAffichee.createGraphics();
 		g2D.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
 		proprietes = new Properties();
 	}
 	
 	@Override
     public Image getImage()
     {
 	    return image;
     }
 
 	@Override
     public String getNom()
     {
 	    return nom;
     }
 
 	@Override
     public ImageIcon getIcone()
     {
 	    return new ImageIcon(imageAffichee);
     }
 	
 	public String getCode()
 	{
 		return this.code;
 	}
 
 	public void setType(String type)
 	{
 		proprietes.setProperty("type", type);
 	}
 	
 	public String getType()
 	{
 		return proprietes.getProperty("type", "");
 	}
 	
 	public Properties getProprietes()
 	{
 		return this.proprietes;
 	}
 	
	public String getFichierNomCree()
 	{
 		String fichierNom = this.code + "_" + this.getType() + this.code.substring(2);
		
 		return fichierNom;
 	}
	
	public String getFichierNomOrigine()
	{
		return this.fichierNomOrigine;
	}
 
 	public void setCode(String newCode)
     {
 		if (code.length() == 5)
 		{
 			this.code = newCode;
 		}
     }
 }
