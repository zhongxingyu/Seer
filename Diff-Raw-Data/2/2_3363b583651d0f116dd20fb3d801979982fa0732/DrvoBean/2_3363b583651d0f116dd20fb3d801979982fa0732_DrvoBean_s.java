 package com.bluelotussoftware.example.jsf;
 
 import java.io.Serializable;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 import org.primefaces.model.DefaultTreeNode;
 import org.primefaces.model.TreeNode;
 
 @ManagedBean
 @ViewScoped
 public class DrvoBean implements Serializable {
 
 	private TreeNode root;
 
 	public DrvoBean() {
 		root = new DefaultTreeNode("Root", null);
 
 		TreeNode node0 = new DefaultTreeNode(new Podatak("Milan", "neki link"), root);
		TreeNode node1 = new DefaultTreeNode("Miodrag", root);
 		TreeNode node2 = new DefaultTreeNode(new Podatak("Petar", "neki link"), root);
 
 		TreeNode node00 = new DefaultTreeNode(new Podatak("Mila", "neki link"), node0);
 		TreeNode node01 = new DefaultTreeNode(new Podatak("Proka", "neki link"), node0);
 
 		TreeNode node10 = new DefaultTreeNode(new Podatak("Jelena", "neki link"), node1);
 		TreeNode node11 = new DefaultTreeNode(new Podatak("Milica", "neki link"), node1);
 
 		TreeNode node000 = new DefaultTreeNode(new Podatak("Laki", "neki link"), node00);
 		TreeNode node001 = new DefaultTreeNode(new Podatak("Tisma", "neki link"), node00);
 		TreeNode node010 = new DefaultTreeNode(new Podatak("Neko", "neki link"), node01);
 
 		TreeNode node100 = new DefaultTreeNode(new Podatak("Neko2", "neki link"), node10);
 	}
 
 	public TreeNode getRoot() {
 		return root;
 	}
 }
