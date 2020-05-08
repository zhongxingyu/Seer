 package IHM.Panel;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.tree.*;
 
 import java.io.*;
 import java.util.*;
 
 import Main.*;
 import Utilitaire.*;
 
 public class PanelArbre extends JPanel implements Serializable
 {
 	private JTree arbre;
 	private DefaultMutableTreeNode racine;
 	private JScrollPane editeurScrollHorizontal;
 	private JScrollPane editeurScrollVertical;
 
 	private Object parentNodeFichier = null;
 	private Object parentNodeProjet = null;
 	
 	private Page pageSelectionnee;
 	private Projet projetSelectionne;
 	
	int locationRow;
	
 	public PanelArbre(JFrame f) 
 	{		
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(175, 100));
 
 		init();
 		
 		if(arbre == null)
 		{
 			racine = new DefaultMutableTreeNode(new File("projet"));
 			arbre = new JTree(racine);
 			f.getContentPane().add(new JScrollPane(arbre));
 		}
 		
 		// ajoute l'action clic a l'arbre
 		arbre.addMouseListener(new MouseAdapter() {
 		      public void mouseClicked(MouseEvent me) {
 		        doMouseClicked(me);
 		      }
 		    });
 
 		editeurScrollHorizontal = new JScrollPane(arbre);
 		editeurScrollHorizontal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		editeurScrollHorizontal.setPreferredSize(new Dimension(250, 145));
 		
 		editeurScrollVertical = new JScrollPane(arbre);
 		editeurScrollVertical.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		editeurScrollVertical.setPreferredSize(new Dimension(250, 145));
 		
 		add(editeurScrollHorizontal);
 		add(editeurScrollVertical);
 	}
 	
 	private void init() 
 	{
 		try {
 			FileInputStream fichier = new FileInputStream("temp/arbre.dat");
 			ObjectInputStream ois = new ObjectInputStream(fichier);
 			arbre = (JTree) ois.readObject();
 			racine = (DefaultMutableTreeNode) ois.readObject();
 		}
 		catch (IOException ignored) 		{}
 		catch (ClassNotFoundException e)	{}
 		if (arbre != null && racine != null)
 			updateTree(racine);
 	}
 	
 	void doMouseClicked(MouseEvent me) 
 	{
 		TreePath path = arbre.getPathForLocation(me.getX(), me.getY());
		locationRow = arbre.getClosestRowForLocation(me.getX(), me.getY());
 		/*
 		 * Exemple path
 		 * [null, site, test.html, titre 1]
 		 */
 		if (path != null)
 		{
 			Object[] tabObj = path.getPath();
 			int location = path.getPathCount();
 			/*
 			 * Exemple Location :
 			 * 1	2	3	4
 			 * /home
 			 * 		site
 			 * 			test1.html
 			 * 				Titre 1
 			 * 				Paragraphe 1
 			 * 			test2.html
 			 * 				Titre 1
 			 * 				Paragraphe 1
 			 */
 			if (location == 2)
 			{
 				Controleur.fenetre.getMenu().activerAjoutProjet();
 				// on desactive les ajouts de titre/paragraphe/image
 				Controleur.fenetre.getMenu().desactiveAjoutPage();	
 				Controleur.fenetre.getPanelListeAction().desactiveAjout();
 				
 				parentNodeProjet = arbre.getLastSelectedPathComponent();
 				projetSelectionne = Controleur.metier.getProjet(path.getLastPathComponent().toString());
 				
 				Controleur.metier.setProjetSelectionne(projetSelectionne);
 			}
 			else if (location == 3)
 			{
 				// on active les boutons/items
 				Controleur.fenetre.getMenu().activerAjoutPage();
 				Controleur.fenetre.getPanelListeAction().activerAjout();
 				
 				projetSelectionne = Controleur.metier.getProjet(tabObj[1].toString());
 				// on selectionne le dernier noeud selectionnee
 				parentNodeFichier = arbre.getLastSelectedPathComponent();
 				// on selectionne la page pour mettre les paragraphes, ect..
 				pageSelectionnee = projetSelectionne.getPage(tabObj[2].toString());
 				
 				Controleur.metier.setProjetSelectionne(projetSelectionne);
 				projetSelectionne.setPageSelectionne(pageSelectionnee);
 				
 				Controleur.fenetre.getPanelVisu().previsualisation();
 			}
 			else if (location > 3)
 			{
 				// on recupere le projet, la page et le noeud grace a path
 				projetSelectionne = Controleur.metier.getProjet(tabObj[1].toString());
 				parentNodeFichier = tabObj[2];
 				pageSelectionnee = projetSelectionne.getPage(parentNodeFichier.toString());
 				
 				Scanner sc = new Scanner(path.getLastPathComponent().toString()).useDelimiter(" ");
 				String str = sc.next();
 				int indice = Integer.parseInt(sc.next());
 				
 				if (str.equals("Titre")) 
 				{
 					String ancienTitre = projetSelectionne.getPage(pageSelectionnee).getAlTitre().get(indice-1);
 					Controleur.creerFenetreAjouterTitre(1, ancienTitre, indice);
 				}
 				if (str.equals("Paragraphe"))
 				{
 					String ancienParagraphe = projetSelectionne.getPage(pageSelectionnee).getAlParagraphe().get(indice-1);
 					Controleur.creerFenetreAjouterParagraphe(1, ancienParagraphe, indice);
 				}
 				if (str.equals("Image"))
 					Controleur.creerFenetreAjouterImage(1);
 			}
 		}
 	}
 	
 	public JTree getArbre() 					{	return arbre;	}
 	public DefaultMutableTreeNode getRacine()	{	return racine;	}
 
 	public ArrayList<String> getOrdreElement()
 	{
 		ArrayList<String> alS = new ArrayList<String>();
 
 		DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) parentNodeFichier;
 		for (int i = 0; i < dtn.getChildCount(); i++)
 		{
			TreePath tp = arbre.getPathForRow(i+locationRow+1);
 			if (tp == null)
 			{
 				Controleur.CreerOptionPane("error", "Une erreur est survenue");
 				return null;
 			}
 			alS.add(tp.getLastPathComponent().toString());
 		}
 		return alS;
 	}
 	
 	public boolean ajoutFils(String type, String s) 
 	{
 		DefaultTreeModel dtm = new DefaultTreeModel(racine);
 		DefaultMutableTreeNode mtn = new DefaultMutableTreeNode(new File(s));
 		if (type.equals("projet"))
 		{
 			dtm.insertNodeInto(mtn, racine, racine.getChildCount());
 			updateTree(racine);
 			return true;
 		}
 		
 		MutableTreeNode parent  = (MutableTreeNode) ((parentNodeProjet == null) ?  dtm.getChild(racine, 0) : parentNodeProjet);
 		if (type.equals("fichier"))
 		{
 			dtm.insertNodeInto(mtn, parent, parent.getChildCount());
 			updateTree(parent);
 			return true;
 		}
 		MutableTreeNode parent2 = (MutableTreeNode) ((parentNodeFichier == null) ?  dtm.getChild(parent, 0) : parentNodeFichier);
 
 		if (type.equals("element"))
 		{
 			dtm.insertNodeInto(mtn, parent2, parent2.getChildCount());
 			updateTree(parent2);
 			return true;
 		}
 		return false;
 	}
 	
 	private void updateTree(Object o)
 	{
 		((DefaultTreeModel) arbre.getModel()).reload((TreeNode) o);
 	}
 	
 	public void enregistrerArbre()
 	{
 		try {
 			FileOutputStream fichier = new FileOutputStream("temp/arbre.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fichier);
 			oos.writeObject(arbre);
 			oos.writeObject(racine);
 			oos.flush();
 			oos.close();
 		}
 		catch (java.io.IOException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 }
