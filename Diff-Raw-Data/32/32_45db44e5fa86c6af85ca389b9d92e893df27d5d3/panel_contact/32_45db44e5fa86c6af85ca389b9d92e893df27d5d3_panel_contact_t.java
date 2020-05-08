 package windows.forms;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Point;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import session.Session;
 import session.contact;
 import session.group;
 import socket.packet.handlers.sends.MoveGroup_handler;
 import windows.actions.buttons.ChangeStatus_button;
 import windows.actions.click.contact_onclick;
 
 public class panel_contact extends JPanel implements DropTargetListener, DragGestureListener, DragSourceListener{
 
 	private static final long serialVersionUID = 1L;
 	private JLabel Titre;
 	private JLabel Soustitre;
 	private JTree  tree;
 	private JComboBox changstatus;
 	private form_communicate comm;
 
 	private DragSource dragSource = null;
 	private DefaultMutableTreeNode selecContact = null;
 	private DefaultMutableTreeNode dropContact = null;
 	
 	private JTextField msgperso;
 
 	public panel_contact()
 	{
 		BuildPanel();
 	}
 
 	private void BuildPanel()
 	{
 		
 		setLayout(new FlowLayout());
 		setBackground(new Color(128,128,255));
 		setLayout(new FlowLayout(FlowLayout.CENTER,400,10));
 		
 		Titre = new JLabel(Session.getPseudo());
 		
 		changstatus= new JComboBox();
 		
 		changstatus.addItem("Online");
 		changstatus.addItem("Busy");
 		changstatus.addItem("AFK");
 		changstatus.addItem("Offline");
		changstatus.setSelectedIndex(Session.getStatus());
 		changstatus.addActionListener(new ChangeStatus_button(changstatus));
 		msgperso = new JTextField(20);
 		msgperso.setText(Session.getPerso_msg());
 		
 		Soustitre = new JLabel("Liste de vos contacts : ");
 		
 		add(Titre);
 		add(changstatus);
 		add(msgperso);
 		add(Soustitre);
 		
 		SetListContact();	
 		
 		comm = null;
 	}
 	
 	private void SetListContact()
 	{
 		GenerateNodes();
 		
 		new DropTarget(tree, this);
 	    dragSource = new DragSource();
 	    dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_MOVE, this);
 	 
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		tree.addMouseListener(new contact_onclick(tree,this));
 		tree.setRootVisible(false);
 		this.add(tree);
 		JScrollPane Scrollbar = new JScrollPane(tree);
 		Scrollbar.setPreferredSize(new Dimension(150, 300));
 		this.add(Scrollbar);
 	}
 	
 	private void GenerateNodes()
 	{
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Contactlist");
 		
 		for(group g : Session.getGroups())
 		{
 			DefaultMutableTreeNode tmp_grp = new DefaultMutableTreeNode(g,true);
 
 			root.add(tmp_grp);
 			for(contact ct : g.getContacts())
 			{
 				DefaultMutableTreeNode tmp_contact = new DefaultMutableTreeNode(ct,false);
 				tmp_grp.add(tmp_contact);	
 			}
 		}
 		
 		// Construction du modle de l'arbre.
 		DefaultTreeModel myModel = new DefaultTreeModel(root);
 		myModel.setAsksAllowsChildren(true);
 
 		// Construction de l'arbre.
 		tree = new JTree(myModel);
 		tree.setCellRenderer(new Tree_Renderer());
 	}
 	
 	public void RefreshContactList()
 	{
 		GenerateNodes();
 
 		tree.repaint();
 	}
 
 	public void setComm(form_communicate comm) { this.comm = comm; }
 	public form_communicate getComm() { return comm; }
 	
 	public void ChPseudo(String n_pseudo) {	Titre.setText("Pseudo : " + n_pseudo); }
 
 	/*
 	 * Drag and Drop
 	 */
 	public void dragEnter(DropTargetDragEvent e) {	e.acceptDrag(DnDConstants.ACTION_MOVE);	}
 	public void dragGestureRecognized(DragGestureEvent e) 
 	{
 		selecContact = null;
 	    dropContact = null;
 	    Object selected = tree.getSelectionPath();
 
 	    if (selected != null) 
 	    {
 	      TreePath treepath = (TreePath) selected;
 	      selecContact = (DefaultMutableTreeNode) treepath.getLastPathComponent();
 	      if(selecContact.getLevel() == 1)
 	    	  return;
 	      dragSource.startDrag(e, DragSource.DefaultMoveDrop, new StringSelection(selected.toString()), this);
 	    }
 	}
 
 	public void drop(DropTargetDropEvent e) 
 	{
 		 Transferable transferable = e.getTransferable();
 		 
 		 if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
 		     e.acceptDrop(DnDConstants.ACTION_MOVE);
 		     Point dropPoint = e.getLocation();
 		     TreePath dropPath = tree.getClosestPathForLocation(dropPoint.x,dropPoint.y);
 		     dropContact = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
 		     if(dropContact.getLevel() == 2)
 		    	 return;
 		     e.getDropTargetContext().dropComplete(true);
 		 }
 		 else
 		    e.rejectDrop();
 	}
 	
 	public void dragDropEnd(DragSourceDropEvent e) {
 		if (e.getDropSuccess()) {
 			if (dropContact == null)
 		        return;
 		    else
 		    {
 		      ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(selecContact);
 			  ((DefaultTreeModel) tree.getModel()).insertNodeInto(selecContact, dropContact, dropContact.getChildCount());
 			  MoveGroup_handler pck = new MoveGroup_handler(((contact)selecContact.getUserObject()).getCid(),
 					  ((group)dropContact.getUserObject()).getGid());
 			  pck.Send();
 			  
 		    }
 		}
 	}
 	
 	public void dragExit(DropTargetEvent arg0) {}
 	public void dragOver(DropTargetDragEvent arg0) {}
 	public void dropActionChanged(DropTargetDragEvent arg0) {}
 	public void dragEnter(DragSourceDragEvent dsde) {}
 	public void dragExit(DragSourceEvent dse) {}
 	public void dragOver(DragSourceDragEvent dsde) {}
 	public void dropActionChanged(DragSourceDragEvent dsde) {}
 }
