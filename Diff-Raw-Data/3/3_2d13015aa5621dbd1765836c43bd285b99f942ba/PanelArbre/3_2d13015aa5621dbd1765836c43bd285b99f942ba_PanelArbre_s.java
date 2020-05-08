 package IHM.Panel;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.tree.*;
 
 import java.io.*;
 import java.util.*;
 
 import Main.Generateur;
 import Utilitaire.*;
 
 public class PanelArbre extends JPanel 
 {
 	private JTree arbre;
 	private DefaultMutableTreeNode racine;
 	private JScrollPane editeurScrollHorizontal;
 	private JScrollPane editeurScrollVertical;
 
 	private Object parentNodeFichier = null;
 	private Object parentNodeProjet = null;
 	
 	private Page pageSelectionnee;
 	
 	public PanelArbre(JFrame f) 
 	{		
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(150, 100));
 
 		listRoot(f);
 
 		editeurScrollHorizontal = new JScrollPane(arbre);
 		editeurScrollHorizontal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		editeurScrollHorizontal.setPreferredSize(new Dimension(250, 145));
 		
 		editeurScrollVertical = new JScrollPane(arbre);
 		editeurScrollVertical.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		editeurScrollVertical.setPreferredSize(new Dimension(250, 145));
 		
 		add(editeurScrollHorizontal);
 		add(editeurScrollVertical);
 	}
 
 	private void listRoot(JFrame f) 
 	{
 		racine = new DefaultMutableTreeNode();
 		File file = new File("site/");
 		Generateur.alProjet.add(new Projet(file.getName()));
 
 		DefaultMutableTreeNode lecteur = new DefaultMutableTreeNode(file.getPath());
 		try 
 		{
 			for (File nom : file.listFiles()) 
 			{
 				if (!nom.getName().equals("content"))
 				{
 					Generateur.alProjet.get(0).ajouterPage(new Page(nom.getName()));
 					DefaultMutableTreeNode node = new DefaultMutableTreeNode(nom.getName() + "\\");
 					lecteur.add(this.listFile(nom, node));
 				}
 			}
 		} catch (NullPointerException e) {}
 
 		racine.add(lecteur);
 
 		arbre = new JTree(racine);
 		
 		f.getContentPane().add(new JScrollPane(arbre));
 		
 		arbre.addMouseListener(new MouseAdapter() {
 		      public void mouseClicked(MouseEvent me) {
 		        doMouseClicked(me);
 		      }
 		    });
 	}
 	
 	void doMouseClicked(MouseEvent me) 
 	{
 		TreePath path = arbre.getPathForLocation(me.getX(), me.getY());
 		/*
 		 * Exemple path
 		 * [null, site, test.html, titre 1]
 		 */
		Object[] tabObj = path.getPath();
 		
 		if (path != null)
 		{
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
 				parentNodeProjet = arbre.getLastSelectedPathComponent();
 			}
 			else if (location == 3)
 			{
 				Generateur.fenetre.getMenu().activerAjout();
 				
 				// on selectionne le dernier noeud selectionnee
 				parentNodeFichier = arbre.getLastSelectedPathComponent();
 				// on selectionne la page pour mettre les paragraphes, ect..
 				pageSelectionnee = Generateur.alProjet.get(0).getPage(path.getLastPathComponent().toString());
 				
 				Generateur.alProjet.get(0).setPageSelectionne(pageSelectionnee);
 			}
 			else if (location > 3)
 			{
 				// on recupere la page et le noeud grace a path
 				parentNodeFichier = tabObj[2];
 				pageSelectionnee = Generateur.alProjet.get(0).getPage(parentNodeFichier.toString());
 				
 				Scanner sc = new Scanner(path.getLastPathComponent().toString()).useDelimiter(" ");
 				String str = sc.next();
 				int indice = Integer.parseInt(sc.next());
 				
 				if (str.equals("Titre")) 
 				{
 					String ancienTitre = Generateur.alProjet.get(0).getPage(pageSelectionnee).getAlTitre().get(indice-1);
 					Generateur.creerFenetreAjouterTitre(pageSelectionnee, 1, ancienTitre, indice);
 				}
 				if (str.equals("Paragraphe"))
 				{
 					String ancienParagraphe = Generateur.alProjet.get(0).getPage(pageSelectionnee).getAlParagraphe().get(indice-1);
 					Generateur.creerFenetreAjouterParagraphe(pageSelectionnee, 1, ancienParagraphe, indice);
 				}
 			}
 		}
 	}
 	
 	public ArrayList<String> getOrdreElement()
 	{
 		ArrayList<String> alS = new ArrayList<String>();
 		
 		TreeNode tn = (TreeNode) parentNodeFichier;
 		for (int i = 0; i < tn.getChildCount(); i++)
 		{
 			TreePath tp = arbre.getPathForRow(i+arbre.getLeadSelectionRow()+1);
 			alS.add(tp.getLastPathComponent().toString());
 		}
 		return alS;
 	}
 	
 	public void ajoutFils(String type, String s) 
 	{
 		DefaultTreeModel dtm = new DefaultTreeModel(racine);
 		MutableTreeNode parent  = (MutableTreeNode) ((parentNodeProjet == null) ? dtm.getChild(racine, 0) : parentNodeProjet);
 		MutableTreeNode parent2 = (MutableTreeNode) ((parentNodeFichier == null) ? dtm.getChild(parent, 0) : parentNodeFichier);
 
 		
 		DefaultMutableTreeNode mtn = new DefaultMutableTreeNode(new File(s));
 		if (type.equals("element"))
 		{
 			dtm.insertNodeInto(mtn, parent2, parent2.getChildCount());
 			updateTree(parent2);
 		}
 		else if (type.equals("fichier"))
 		{
 			dtm.insertNodeInto(mtn, parent, parent.getChildCount());
 			updateTree(parent);
 		}
 		else if (type.equals("projet"))
 		{
 			dtm.insertNodeInto(mtn, racine, racine.getChildCount());
 			updateTree(racine);
 		}
 	}
 	
 	private void updateTree(Object o)
 	{
 		((DefaultTreeModel) arbre.getModel()).reload((TreeNode) o);
 	}
 
 	private DefaultMutableTreeNode listFile(File file,DefaultMutableTreeNode node) 
 	{
 		if (file.isFile())
 			return new DefaultMutableTreeNode(file.getName());
 		else 
 		{
 			for (File nom : file.listFiles()) 
 			{
 				DefaultMutableTreeNode subNode;
 				if (nom.isDirectory()) 
 				{
 					subNode = new DefaultMutableTreeNode(nom.getName() + "\\");
 					node.add(this.listFile(nom, subNode));
 				} 
 				else
 					subNode = new DefaultMutableTreeNode(nom.getName());
 
 				node.add(subNode);
 			}
 			return node;
 		}
 	}
 }
