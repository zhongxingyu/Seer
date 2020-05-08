 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 
 @SuppressWarnings("serial")
 public class ImagePanel extends JPanel implements Observable
 {
 	private Image img; // L'image globale
 	private Dimension dimImage; // La dimension de l'image
 	
 	// Liste des observateurs
 	private ArrayList<Observateur> listeObservateur =
 	        new ArrayList<Observateur>();
 
 	
 	/**
 	 * Crée un nouveau panneau d'image à partir d'une image avec les dimensions voulues.
 	 * 
 	 * @param img
 	 * 			L'image du panneau
 	 * @param dim
 	 * 			Les dimensions souhaitées
 	 */
 	public ImagePanel(Image img, Dimension dim)
 	{
 		this.img = img;
 		this.dimImage = dim;
 		this.setMinimumSize(dim);
 		this.setPreferredSize(dim);
		this.setOpaque(false);
 	}
 
 	/**
 	 * Mets une nouvelle image dans le panneau
 	 * 
 	 * @param img
 	 * 			Le nouvelle image
 	 */
 	public void setImage(Image img)
 	{
 		this.img = img;
 	}
 	
 	/**
 	 * Renvoie l'image contenue dans le panneau
 	 * 
 	 * @return
 	 * 		L'image du panneau
 	 */
 	public Image getImage()
 	{
 		return this.img;
 	}
 	
 	/**
 	 * Modifie les dimensions du panneau
 	 * 
 	 * @param dim
 	 * 			Les nouvelles dimensions du panneau
 	 */
 	public void setDimension(Dimension dim)
 	{
 		dimImage = dim;
 	}
 	
 	/**
 	 * Renvoie les dimensions du panneau
 	 * 
 	 * @return
 	 * 		Les dimensions du panneau
 	 */
 	public Dimension getDimension()
 	{
 		return dimImage;
 	}
 	
 	@Override
 	public void paintComponent(Graphics g)
 	{
 //		g.fillRect(0, 0, dimImage.width, dimImage.height);
 		if(img != null)
 		{
 			g.drawImage(img, 0, 0, dimImage.width, dimImage.height, null);			
 		}
 	}
 	
 	@Override
 	public void addObservateur(Observateur obs)
 	{
 		listeObservateur.add(obs);
 	}
 
 	@Override
 	public void rmvObservateur(Observateur obs)
 	{
 		listeObservateur.remove(obs);
 	}
 
 	@Override
 	public void updateObservateur()
 	{
 		for (Observateur obs : listeObservateur)
 		{
 		}
 	}
 }
