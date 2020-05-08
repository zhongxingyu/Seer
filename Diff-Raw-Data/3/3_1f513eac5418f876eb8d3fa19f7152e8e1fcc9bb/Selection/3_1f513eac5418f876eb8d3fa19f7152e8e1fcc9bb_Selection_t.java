 package com.carte.panels;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 import com.carte.sprites.Case;
 import com.carte.sprites.Sprite;
 import com.carte.utils.Observable;
 import com.carte.utils.Observateur;
 
 
 @SuppressWarnings("serial")
 public class Selection extends JPanel implements ActionListener, Observable,
         MouseListener
 {
 	private int largeurCase; // Largeur d'une case
 	private int hauteurCase; // hauteur d'une case
 	private int nbrColonnes; // Nombre de colonnes de sprites
 	private int nbrColonnesAff; // Nombre de colonnes à afficher
 	private int nbrLignesAff = 3; // Nombre de lignes de sprites affichées
 	private ArrayList<Case> casesSelected = new ArrayList<Case>(); // Case
 	                                                              // actuellement
 	                                                              // sélectionnée,
 	                                                              // peut être
 	                                                              // vide
 	private int[] indiceShiftSelection = new int[2]; // coordonnées d'une
 	                                                 // sélection en i,j
 	private boolean premierShiftOk = false; // indique si le prochain clic avec
 	                                        // shift cloturera une sélection
 
 	// Liste des observateurs
 	private ArrayList<Observateur> listeObservateur =
 	        new ArrayList<Observateur>();
 
 	// Catégories de sprite
 	private JPanel categories = new JPanel();
 	private ButtonGroup groupe = new ButtonGroup();
 
 	// Affichage des sprites de la catégorie choisie
 	private JPanel sprites = new JPanel();
 	private JPanel panneauSprites = new JPanel();
 	private GridLayout layoutSprites;
 	private ArrayList<Case> cases = new ArrayList<Case>();
 	private JScrollPane scrollSpr;
 	private JScrollPane scrollSel;
 
 	/**
 	 * @param lc
 	 *        Largeur d'une case en pixels
 	 * @param hc
 	 *        Hauteur d'une case en pixels
 	 * @param nbC
 	 *        Nombre de colonnes
 	 */
 	public Selection(int lc, int hc, int nbC)
 	{
 		// Initialisation
 		largeurCase = lc;
 		hauteurCase = hc;
 		nbrColonnes = nbC;
 		nbrColonnesAff = nbrColonnes + 8; // Le 8 correspond à  peu près à  la
 		                                  // place prise par le panneau option
 
 		// Bordure du panneau Selection
 		this.setBorder(BorderFactory.createRaisedBevelBorder());
 
 		// Bordure des panneaux internes
 		Border b = BorderFactory.createLoweredBevelBorder();
 
 		// Ajout de scrollbar
 		scrollSel = new JScrollPane(createCategoriesPanel());
 		scrollSel
 		        .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollSel
 		        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
 		scrollSel.setBorder(new TitledBorder(b, "Catégories"));
 		scrollSel.setMinimumSize(new Dimension(nbrColonnesAff * lc, 120));
 		scrollSel.setMaximumSize(new Dimension(nbrColonnesAff * lc, 120));
 
 		scrollSpr = new JScrollPane(createSpritesPanel());
 
 		// avec un peu d'espace pour les scroll bar
 		scrollSpr.setMinimumSize(new Dimension(nbrColonnesAff * lc + 30,
 		        nbrLignesAff * hc));
 		scrollSpr.setMaximumSize(new Dimension(nbrColonnesAff * lc + 30,
 		        nbrLignesAff * hc + 60));
 		scrollSpr
 		        .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		scrollSpr.setBorder(b);
 		int widthMin =
 		        scrollSpr.getMinimumSize().width
 		                + scrollSpr.getMinimumSize().width + 30;
 		int heightMin =
 		        scrollSpr.getMinimumSize().height
 		                + scrollSpr.getMinimumSize().height + 30;
 		this.setMinimumSize(new Dimension(widthMin, heightMin));
 
 		// Layout
 		setLayout(new GridBagLayout());
 		add(scrollSel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
 		        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
 		                5, 5, 5), 0, 0));
 		add(scrollSpr, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
 		        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
 		                5, 10, 5), 0, 0));
 	}
 
 	/**
 	 * Affiche les sprites correspondant aux images contenues dans un dossier
 	 * 
 	 * @param dossier
 	 *        Dossier contenant des images
 	 */
 	public void afficherSprites(File dossier)
 	{
 		// Initialisation
 		int nbrImages = 0;
 		int nbrLignesReel = nbrLignesAff;
 
 		ArrayList<File> fichiers = getImageFiles(dossier.listFiles());
 		nbrImages = fichiers.size();
 		if (nbrImages == 0)
 		{
 			System.err.println("Erreur dans "
 			        + "Selection.afficherSprites(File dossier) : "
 			        + "pas de fichiers.");
 		}
 
 		// Nettoyage des anciennes images
 		sprites.removeAll();
 		cases.clear();
 
 		// Calcul de la taille du gridLayout
 		if (nbrImages > (nbrLignesAff * nbrColonnesAff))
 		{
 			nbrLignesReel = nbrImages / nbrColonnesAff;
 			if (nbrImages % (nbrColonnesAff) != 0)
 				nbrLignesReel++;
 		}
 		layoutSprites.setRows(nbrLignesReel);
 
 		// on met toutes les images contenues dans le dossier
 		for (int i = 0; i < nbrImages; i++)
 		{
 			try
 			{
 				cases.add(new Case(largeurCase, hauteurCase, Color.darkGray,
 				        Color.black));
 				sprites.add(cases.get(i));
 				cases.get(i).addActionListener(this);
 				cases.get(i).addMouseListener(this);
 				cases.get(i).setImage(ImageIO.read(fichiers.get(i)), getCodeImage(fichiers.get(i).getName()), 0);
 			}
 			catch (IOException err)
 			{
 				System.err.println("Erreur dans "
 				        + "Selection.afficherSprites(File dossier) : "
 				        + "le fichier lu n'est pas une image.");
 				err.printStackTrace();
 			}
 		}
 
 		// On rajoute des panels factices pour que le grid layout mette le bon
 		// nombre de colonnes
 		int nbrImagesFactices = (nbrLignesReel * nbrColonnesAff) - nbrImages;
 
 		// Si on a moins de nbrLignesAff lignes, on en rajoute une pour le
 		// layout
 		if (nbrImages < nbrColonnesAff)
 			nbrImagesFactices +=
 			        (nbrLignesAff - nbrLignesReel) * nbrColonnesAff;
 
 		for (int i = 0; i < nbrImagesFactices; i++)
 		{
 			
 			if(i==0 && nbrImages == 0)
 			{
 				JPanel sizedPanel = new JPanel();
 				sizedPanel.setMinimumSize(new Dimension(largeurCase, hauteurCase));
 				sizedPanel.setPreferredSize(new Dimension(largeurCase, hauteurCase));
 				sprites.add(sizedPanel);
 			}
 			else
 			{
 				sprites.add(new JPanel());
 			}
 		}
 		repaint();
 	}
 
 	/**
 	 * Récupère le code image à partir du nom complet de l'image.
 	 * 
 	 * @param fileName
 	 * 				Le nom de l'image doit être sous la forme NNNNN_nomImage où N est un nombre.
 	 * 				Le nom de l'image est quelconque et peut contenir des _.
 	 * @return codeImg
 	 * 				Le code image correspondant au nom de fichier.
 	 */
 	private String getCodeImage(String fileName)
     {
 	    String codeImg = "";
 	    
 	    String[] split = fileName.split("_");
 	    // Si le nom de fichier comporte au moins deux parties
 	    if(split.length >= 2)
 	    {
 	    	// Si on a exactement 5 nombres
 	    	if(split[0].matches("[0-9]{5}"))
 	    	{
 	    		codeImg = split[0];
 	    	}
 	    }
 	    return codeImg;
     }
 
 	/**
 	 * Ne récupère que les images .png, .bmp et .jpg contenues dans une liste de fichiers
 	 * 
 	 * @param listFiles
 	 * 			Une liste de fichiers à filtrer
 	 * @return
 	 * 			La liste des fichiers correspondant à des images .png, .bmp et .jpg
 	 */
 	private ArrayList<File> getImageFiles(File[] listFiles)
 	{
 		ArrayList<File> imageFiles = new ArrayList<File>();
 
 		for (int i = 0; i < listFiles.length; i++)
 		{
 			if (listFiles[i].getName().endsWith("png") || listFiles[i].getName().endsWith("bmp")
 					|| listFiles[i].getName().endsWith("jpg"))
 			{
 				imageFiles.add(listFiles[i]);
 			}
 		}
 		return imageFiles;
 	}
 
 	/**
 	 * @param caseSel
 	 *        Case à  sélectionner
 	 */
 	public void selectCase(Case caseSel)
 	{
 		casesSelected.add(caseSel);
 		caseSel.setBordure(true);
 		caseSel.setCouleurBordure(Color.RED);
 		caseSel.setHovered(false);
 	}
 
 	/**
 	 * Renvoie la liste des cases sélectionnées
 	 * 
 	 * @return
 	 * 		La liste des cases sélectionnées
 	 */
 	public ArrayList<Case> getCasesSelected()
 	{
 		return casesSelected;
 	}
 
 	/**
 	 * Efface la sélection
 	 */
 	public void clearSelection()
 	{
 		if (casesSelected.size() > 1)
 		{
 			for (Case caseSel : casesSelected)
 			{
 				caseSel.setCouleurBordure(Color.darkGray);
 			}
 		}
 		else if (!casesSelected.isEmpty())
 		{
 			casesSelected.get(0).setCouleurBordure(Color.darkGray);
 		}
 		casesSelected.clear();
 		repaint();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		Object source = e.getSource();
 		String classe = source.getClass().getName();
 
 		// Gestion du clic sur un bouton radio : changer les sprites affichés
 		if (classe.equals("javax.swing.JRadioButton"))
 		{
 			File dossier =
 			        new File("images/" + ((JRadioButton) source).getText());
 			afficherSprites(dossier);
 			premierShiftOk = false;
 			scrollSpr.revalidate();
 			scrollSpr.repaint();
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
 			ArrayList<Sprite> listSprite = new ArrayList<Sprite>();
 			for (Case caseSel : casesSelected)
 			{
 				listSprite.add(caseSel.getSprite());
 			}
 			
 			obs.update(listSprite, "Selection");
 		}
 	}
 
 	/**
 	 * Crée et renvoie le panneau de catégories
 	 * 
 	 * @return 
 	 * 			Le panel contenant les catégories
 	 */
 	public JPanel createCategoriesPanel()
 	{
 		// Création des boutons radio
 		int nbrCat = 0;
 		try
 		{
 			File racine = new File("images");
 
 			for (String s : racine.list())
 			{
 				if (new File("images/" + s).isDirectory())
 				{
 					nbrCat++;
 					JRadioButton bouton = new JRadioButton(s);
 					categories.add(bouton);
 					groupe.add(bouton);
 					bouton.addActionListener(this);
 				}
 			}
 		}
 		catch (NullPointerException e)
 		{
 			System.err.println("Erreur dans "
 			        + "Selection.createCategoriesPanel() : " + e.getMessage());
 		}
 
 		categories.setLayout(new GridLayout(3, nbrCat / 3));
 
 		return categories;
 	}
 
 	/**
 	 * Crée et renvoie le panneau de sprites
 	 * 
 	 * @return 
 	 * 			Le panel contenant les sprites
 	 */
 	public JPanel createSpritesPanel()
 	{
 		panneauSprites.setLayout(new GridBagLayout());
 		panneauSprites.add(sprites, new GridBagConstraints(0, 0, 1, 1, 0.0,
 		        0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
 		        new Insets(0, 0, 0, 0), 0, 0));
 		// Création de l'affichage des sprites
 
 		// sprites.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
 		layoutSprites = new GridLayout();
 		layoutSprites.setHgap(0);
 		layoutSprites.setVgap(0);
 		sprites.setLayout(layoutSprites);
 		if (groupe.getButtonCount() <= 0)
 		{
 			System.err.println("Erreur dans Selection.Selection(int lc, "
 			        + "int hc) : pas de dossiers dans le dossier images.");
 		}
 		else
 		{
 			((JRadioButton) categories.getComponent(0)).setSelected(true);
 			this.afficherSprites(new File("images/"
 			        + ((JRadioButton) categories.getComponent(0)).getText()));
 		}
 
 		// sprites.setLayout(new GridLayout(nbrSprites/nbrColonnes, nbrColonnes,
 		// 0, 0));
 		repaint();
 		return panneauSprites;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0)
 	{
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0)
 	{
 		Object source = arg0.getSource();
 		String classe = source.getClass().getName();
		
		if (classe.equalsIgnoreCase("com.carte.sprites.Case"))
 		{
 			// Désélection de(s) la case(s) précédente(s)
 			if (!arg0.isControlDown() && !arg0.isShiftDown())
 			{
 				if (!casesSelected.isEmpty())
 					clearSelection();
 				// Sélection de la nouvelle
 				selectCase((Case) source);
 				indiceShiftSelection[0] = cases.indexOf(((Case) source));
 				premierShiftOk = true;
 			}
 			else if (arg0.isControlDown())
 			{
 				if (casesSelected.contains((Case) source))
 				{
 					casesSelected.remove((Case) source);
 					((Case) source).setCouleurBordure(Color.darkGray);
 				}
 				else
 				{
 					// Sélection de la nouvelle
 					selectCase((Case) source);
 				}
 			}
 			else if (arg0.isShiftDown())
 			{
 				if (premierShiftOk)
 				{
 					indiceShiftSelection[1] = cases.indexOf(((Case) source));
 					if (indiceShiftSelection[0] != indiceShiftSelection[1])
 					{
 						if (!casesSelected.isEmpty())
 							clearSelection();
 						// Sélection de la nouvelle
 						for (int i =
 						        Math.min(indiceShiftSelection[0],
 						                indiceShiftSelection[1]); i <= Math
 						        .max(indiceShiftSelection[0],
 						                indiceShiftSelection[1]); i++)
 							selectCase(cases.get(i));
 					}
 				}
 				else
 				{
 					if (!casesSelected.isEmpty())
 						clearSelection();
 					// Sélection de la nouvelle
 					selectCase((Case) source);
 					indiceShiftSelection[0] =
 					        casesSelected.indexOf(((Case) source));
 					premierShiftOk = true;
 				}
 			}
 
 		}
 		// Mise à  jour des observateurs
 		this.updateObservateur();
 
 	}
 }
