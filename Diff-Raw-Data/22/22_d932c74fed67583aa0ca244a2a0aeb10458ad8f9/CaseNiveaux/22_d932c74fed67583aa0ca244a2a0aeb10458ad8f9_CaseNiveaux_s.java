 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.Serializable;
 
 @SuppressWarnings("serial")
 public class CaseNiveaux extends AbstractCase
 {
 	private int 		nbrNiveaux; 	// Nombre total de niveaux
 	private Sprite[] 	tabSprite; 			// Images de chaque niveaux
 	private boolean[] 	visible; 		// Indique la visibilité du niveau ; mis à jour avec DP observer
 
 	/**
 	 * @param l
 	 *            La largeur de la case en pixels
 	 * @param h
 	 *            La hauteur de la case en pixels
 	 * @param niv
 	 * 			  Le nombre maximal de niveaux
 	 */
 	public CaseNiveaux(int l, int h, int niv) 
 	{
 		super(l, h);
 		nbrNiveaux = niv;
 		tabSprite = new Sprite[nbrNiveaux];
 		visible = new boolean[nbrNiveaux];
 		for(int k = 0 ; k < nbrNiveaux ; k++)
 		{
 			tabSprite[k] = new Sprite(largeur, hauteur);
 			visible[k] = true;
 		}
 	}
 
 	/**
 	 * @param l
 	 *            La largeur de la case en pixels
 	 * @param h
 	 *            La hauteur de la case en pixels
 	 * @param couleurB
 	 *            La couleur de la bordure
 	 * @param couleurF
 	 *            La couleur du fond
 	 * @param niv
 	 * 			  Le nombre maximal de niveaux
 	 */
 	public CaseNiveaux(int l, int h, Color couleurB, Color couleurF, int niv) 
 	{
 		super(l, h, couleurB, couleurF);
 		nbrNiveaux = niv;
 		tabSprite = new Sprite[nbrNiveaux];
 		visible = new boolean[nbrNiveaux];
 		for(int k = 0 ; k < nbrNiveaux ; k++)
 		{
 			tabSprite[k] = new Sprite(largeur, hauteur);
 			visible[k] = true;
 		}
 	}
 
 	/**
 	 * @param niv
 	 *            Le niveau où récupérer l'image : à partir de 1
 	 * @return image
 	 *            L'image de ce niveau
 	 */
 	public Image getImage(int niv)
 	{
 		if(niv <= nbrNiveaux)
 		{
 			return tabSprite[niv - 1].getImage();
 		}
 		else
 		{
 			System.err.println("Niveau incorrect : " + niv + " dans CaseNiveaux::getImage(int)");
 			return null;
 		}
 	}
 	
 	/**
 	 * @param img
 	 *            L'image à rajouter
 	 * @param niv
 	 *            Le niveau où rajouter l'image : à partir de 1
 	 */
 	public void setImage(Image img, String codeImg, int niv)
 	{
 		if(niv <= nbrNiveaux)
 		{
 			tabSprite[niv - 1 ].setImage(img, codeImg);
 		}
 		else
 			System.err.println("Niveau incorrect : " + niv + " dans CaseNiveaux::setImage(Image, int)");
 	}
 	
 	/**
 	 * @param niv
 	 *            Le niveau où récupérer le sprite : à partir de 1
 	 * @return sprite
 	 *            Le sprite de ce niveau
 	 */
 	public Sprite getSprite(int niv)
 	{
 		if(niv <= nbrNiveaux)
 		{
 			return tabSprite[niv - 1];
 		}
 		else
 		{
 			System.err.println("Niveau incorrect : " + niv + " dans CaseNiveaux::getSprite(int)");
 			return null;
 		}
 	}
 	
 	/**
 	 * @param sprite
 	 *            Le sprite à rajouter
 	 * @param niv
 	 *            Le niveau où rajouter le sprite : à partir de 1
 	 */
 	public void setSprite(Sprite sprite, int niv)
 	{
 		if(niv <= nbrNiveaux)
 		{
			tabSprite[niv - 1 ] = new Sprite(sprite);
 		}
 		else
 			System.err.println("Niveau incorrect : " + niv + " dans CaseNiveaux::setSprite(Sprite, int)");
 	}
 	
 	/**
 	 * @param niv
 	 *            Le niveau à effacer : à partir de 1
 	 */
 	public void clear(int niv)
 	{
 		if(niv <= nbrNiveaux)
 			tabSprite[niv - 1 ].setImage(null, "");
 		else
 			System.err.println("Niveau incorrect : " + niv + " dans CaseNiveaux::setImage(Image, int)");
 	}
 	
 	/**
 	 * @param niv
 	 *            Le niveau où modifier la visibilité : à partir de 1
 	 * @param vis
 	 *            Indique la visibilité voulue (true = visible)
 	 */
 	public void setNivVisible(int niv, boolean vis)
 	{
 		visible[niv - 1] = vis;
 	}
 	
 	/**
 	 * @return nbrNiveaux
 	 *            Le nombre de niveaux max
 	 */            
 	public int getNbrNiveaux()
 	{
 		return nbrNiveaux;
 	}
 	
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		// Dessine une couleur de fond si elle a été spécifiée
 		if(couleurFond != null)
 		{
 			g.setColor(couleurFond);
 			g.fillRect(0, 0, largeur, hauteur);
 		}
 		
 		// Dessine l'image si elle a été définie
 		for(int i = 0 ; i < nbrNiveaux ; i++)
 		{
 			if(visible[i]) 
 			{
 				tabSprite[i].draw(g);
 			}
 		}
 		
 
 		// Dessine une bordure
 		
 		if (bordureOn || isHovered || isChosen) 
 		{
 			g.setColor(couleurBordure);
 			
 			if(isChosen)
 				g.setColor(Color.RED);
 			if(isHovered)
 				g.setColor(Color.ORANGE);
 			
 			g.drawRect(0, 0, largeur - 1, hauteur - 1);
 		}
 	}
 }
