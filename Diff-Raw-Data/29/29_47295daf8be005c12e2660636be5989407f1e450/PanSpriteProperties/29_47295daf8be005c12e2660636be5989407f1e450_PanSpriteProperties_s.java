 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import javax.swing.border.Border;
 
 @SuppressWarnings("serial")
 public class PanSpriteProperties extends JPanel implements Observateur, MouseListener
 {
 	private JPanel contentPane; // Le conteneur
 	private Sprite sprite; // Le sprite à afficher
 	private JLabel labCode; // Le label du code image
 	private ImagePanel panImage; // Le panneau contenant l'image
 	private JLabel niv; // Le label contenant le niveau du panneau
 	private int niveau; // Le niveau du panneau
 	private int nbrNiveaux; // Le nombre de niveaux max
 	private int largeur; // La largeur d'un sprite
 	private int hauteur; // La hauteur d'un sprite
 	private JToolBar toolbar = new JToolBar(); // La toolbar si besoin
 	// QUESTION : est-ce qu'on fait pas un élément toolbar plutot que de tout
 	// mettre ici ?
 	private JButton bAvant; // Le bouton avant de la toolbar
 	private JButton bApres; // Le bouton après de la toolbar
 	private JButton bSuppr; // Le bouton suppr de la toolbar
 	private JButton bCopier; // Le bouton copier de la toolbar
 	private JButton bColler; // Le bouton coller de la toolbar
 	
 	// Liste des observateurs
 	private ArrayList<Observateur> listeObservateur =
 	        new ArrayList<Observateur>();
 	private int[] update = new int[1];
 
 
 	/**
 	 * Construit un panneau de propriétés avec les éléments nécessaires
 	 * 
 	 * @param nbrNiv
 	 *        Le nombre de niveaux max
 	 * @param largeur
 	 *        La largeur d'un sprite
 	 * @param hauteur
 	 *        La hauteur d'un sprite
 	 */
 	public PanSpriteProperties(Sprite sprite, int niv, int nbrNiv)
 	{
 		this.sprite = sprite;
 		niveau = niv;
 		nbrNiveaux = nbrNiv;
 		hauteur = sprite.getHauteur();
 		largeur = sprite.getLargeur();
 		
 		Border b = BorderFactory.createLoweredBevelBorder();
 		this.setBorder(b);
 		contentPane = new JPanel();
 		contentPane.setLayout(new GridBagLayout());
 
 		this.setLayout(new BorderLayout());
 		this.add(contentPane, BorderLayout.CENTER);
 		updateContent();
 	}
 
 	/**
 	 * Ajoute une toolbar si besoin
 	 */
 	public void addToolbar()
 	{
 		bAvant = new JButton(new ImageIcon("fleche_haut.png"));
 		bAvant.setMargin(new Insets(1, 1, 1, 1));
 		bAvant.setActionCommand("avant_" + String.valueOf(niveau));
 		if (niveau == 1)
 		{
 			bAvant.setEnabled(false);
 		}
 
 		bApres = new JButton(new ImageIcon("fleche_bas.png"));
 		bApres.setMargin(new Insets(1, 1, 1, 1));
 		bApres.setActionCommand("apres_" + String.valueOf(niveau));
 		if (niveau == nbrNiveaux)
 		{
 			bApres.setEnabled(false);
 		}
 
 		bSuppr = new JButton(new ImageIcon("suppr.png"));
 		bSuppr.setMargin(new Insets(1, 1, 1, 1));
 		bSuppr.setActionCommand("suppr_" + String.valueOf(niveau));
 
 		bCopier = new JButton("C");
 		bColler = new JButton("V");
 
 		toolbar.add(bAvant);
 		toolbar.add(bApres);
 		toolbar.add(bSuppr);
 		toolbar.addSeparator();
 		toolbar.add(bCopier);
 		toolbar.add(bColler);
 
 		this.add(toolbar, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Renvoie le sprite de ce panneau
 	 * 
 	 * @return
 	 * 		Le sprite de ce panneau
 	 */
 	public Sprite getSprite()
 	{
 		return sprite;
 	}
 
 	/**
 	 * Modifie le sprite de ce panneau
 	 * 
 	 * @param sprite2
 	 * 				Le sprite à mettre
 	 */
 	public void setSprite(Sprite sprite2)
 	{
 		sprite = sprite2;
 		updateContent();
 	}
 
 	/**
 	 * Met à jour le contenu du panneau de propriétés
 	 */
 	public void updateContent()
 	{
 		String code =
 		        (sprite.getCode().matches("[0-9]{5}") && !sprite.getCode()
 		                .isEmpty()) ? sprite.getCode() : "00000";
 		labCode = new JLabel(code);
 		niv = new JLabel(" " + String.valueOf(niveau));
 		update[0] = niveau;
 		niv.setBorder(BorderFactory.createLoweredBevelBorder());
 		
 		panImage =
 		        new ImagePanel(sprite.getDrawImage(), new Dimension(
 		                sprite.getLargeur(), sprite.getHauteur()));
 		panImage.addMouseListener(this);
 		sprite.addObservateur(this);
 		//panImage.setBorder(BorderFactory.createRaisedBevelBorder());
 
 		contentPane.removeAll();
 		contentPane.add(labCode, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
 		        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
 		        new Insets(5, 10, 5, 5), 5, 0));
 		contentPane.add(panImage, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
 		        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
 		                5, 5, 5, 10), 0, 0));
 		contentPane.add(niv, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
 		        GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,
 		                15, 0, 0), 2, 0));
 	}
 
 	/**
 	 * Renvoie la toolbar de ce panneau
 	 * 
 	 * @return
 	 * 		La toolbar
 	 */
 	public JToolBar getToolbar()
 	{
 		return toolbar;
 	}
 
 	/**
 	 * Renvoie le panneau image de ce panneau
 	 * 
 	 * @return
 	 * 		Le panneau image
 	 */
 	public ImagePanel getImagePanel()
 	{
 		return panImage;
 	}
 
 	/**
 	 * Permet d'ajouter un listener à la toolbar
 	 * 
 	 * @param listener
 	 * 				Le listener à rajouter
 	 */
 	public void addToolbarListener(ActionListener listener)
 	{
 		for (int i = 0; i < toolbar.getComponentCount(); i++)
 		{
 			if (toolbar.getComponent(i).getClass().getCanonicalName()
 			        .equals("javax.swing.JButton"))
 			{
 				((JButton) toolbar.getComponent(i)).addActionListener(listener);
 			}
 		}
 	}
 
 	/**
 	 * Retourne le niveau de ce panneau
 	 * 
 	 * @return
 	 * 		Le niveau du panneau
 	 */
 	public int getNiveau()
 	{
 		return niveau;
 	}
 
 	/**
 	 * Modifie le niveau de ce panneau
 	 * 
 	 * @param niv
 	 * 			Le nouveau niveau
 	 */
 	public void setNiveau(int niv)
 	{
 		this.niveau = niv;
 		update[0] = niveau;
 		this.niv.setText(String.valueOf(niv));
 
 		bAvant.setActionCommand("avant_" + String.valueOf(niveau));
 		bApres.setActionCommand("apres_" + String.valueOf(niveau));
 		bSuppr.setActionCommand("suppr_" + String.valueOf(niveau));
 
 		if (niv == 1)
 		{
 			bAvant.setEnabled(false);
 		}
 		else
 		{
 			bAvant.setEnabled(true);
 		}
 
 		if (niv == nbrNiveaux)
 		{
 			bApres.setEnabled(false);
 		}
 		else
 		{
 			bApres.setEnabled(true);
 		}
 	}
 
 	/**
 	 * Met un sprite vide à la place du sprite présent
 	 */
 	public void supprSprite()
 	{
 		sprite = new Sprite(largeur, hauteur);
 		updateContent();
 
 		revalidate();
 		repaint();
 	}
 
 	@Override
     public void update(ArrayList<Sprite> sprites, String source)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 
 	@Override
     public void update(boolean[] bool)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 
 	@Override
     public void update(int[] integer)
     {
 		// Update de l'image active depuis le panneau image
 	    sprite.setDefautImage(integer[0]);
 	}
 
 	@Override
     public void update(String[] string)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 	
 	@Override
     public void update()
     {
 	    panImage.repaint();
     }
 	
 	@Override
     public void mouseClicked(MouseEvent e)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 
 	@Override
     public void mousePressed(MouseEvent e)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 
 	@Override
     public void mouseReleased(MouseEvent e)
 	{		
 		if(e.getSource().getClass().getCanonicalName().equalsIgnoreCase("ImagePanel"))
 		{
 			if(e.getButton() == MouseEvent.BUTTON1)
 			{
 				sprite.startAnimation();
 			}else if(e.getButton() == MouseEvent.BUTTON3)
 			{
 				sprite.changerDefImg();
 			}
 		}
     }
 
 	@Override
     public void mouseEntered(MouseEvent e)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 
 	@Override
     public void mouseExited(MouseEvent e)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 }
