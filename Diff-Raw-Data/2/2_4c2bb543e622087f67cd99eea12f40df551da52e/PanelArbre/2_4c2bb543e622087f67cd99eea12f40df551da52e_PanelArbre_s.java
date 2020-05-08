 package IHM.Panel;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.io.*;
 import java.util.ArrayList;
 
 import javax.swing.*;
 import javax.swing.tree.*;
 
 public class PanelArbre extends JPanel 
 {
 	private JTree arbre;
 	private DefaultMutableTreeNode racine;
 	private JScrollPane editeurScrollHorizontal;
 	private JScrollPane editeurScrollVertical;
 	private ArrayList<String> alS;
 	private static int cpt;
 	
 	public PanelArbre(JFrame f) 
 	{
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(150, 100));
 		
 		alS = new ArrayList<String>();
 		cpt=0;
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
 
 		DefaultMutableTreeNode lecteur = new DefaultMutableTreeNode(file.getPath());
 		try {
 			for (File nom : file.listFiles()) {
 				if (!nom.getName().equals("content"))
 				{
 					DefaultMutableTreeNode node = new DefaultMutableTreeNode(nom.getName() + "\\");
 					lecteur.add(this.listFile(nom, node));
 				}
 			}
 		} catch (NullPointerException e) {
 		}
 
 		racine.add(lecteur);
 
 		arbre = new JTree(racine);
 		
 		f.getContentPane().add(new JScrollPane(arbre));
 	}
 	
 	public void ajoutFils(String s, String value) {
 		DefaultTreeModel dtm = new DefaultTreeModel(racine);
 		Object parent = dtm.getChild(racine,0);
		Object parent2 = dtm.getChild(parent, 1);
 
 		DefaultMutableTreeNode mtn = new DefaultMutableTreeNode(new File(s));
 		dtm.insertNodeInto(mtn,(MutableTreeNode) parent2,cpt);
 		cpt++;
 		updateTree(parent2);
 		addToAls(value);
 	}
 	
 	private void updateTree(Object o)
 	{
 		((DefaultTreeModel) arbre.getModel()).reload((TreeNode) o);
 	}
 	
 	private void addToAls(String s)
 	{
 		alS.add(s);
 		System.out.println(alS);
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
 				if (nom.isDirectory()) {
 					subNode = new DefaultMutableTreeNode(nom.getName() + "\\");
 					node.add(this.listFile(nom, subNode));
 				} else {
 					subNode = new DefaultMutableTreeNode(nom.getName());
 				}
 				node.add(subNode);
 			}
 			return node;
 		}
 	}
 }
