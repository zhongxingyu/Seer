 package webApplication.grafica;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.DropMode;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.TransferHandler;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import webApplication.business.*;
 
 public class TreePanel extends JPanel implements ActionListener, TreeSelectionListener	{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8834919285623008305L;
 	
 	private static String ROOT = "Home";
 	private static String DELETEMESSAGE = "You are going to delete ";
 	private static String CONFIRMMESSAGE ="\n Do you want to continue?";
 	private static String NOTEMPTYMESSAGE = " is not empty.\n Deleting this element will delete all the elements inside.\n Do you want to continue?";
 	private static String CLEARALL = "This will delete the current work.\n Do you wish to continue?";
 	private static String ROOTDELETE = "You cannot delete this element because it is the tree root";
 	private static String EMPTYSELECTION = "No element selected";
 	private static String ADDCHILDRENNOTALLOWED = " can't have children";
 	protected static String ADD_COMMAND = "add";
     protected static String REMOVE_COMMAND = "remove";
     protected static String CLEAR_COMMAND = "clear";
 	
 	private DefaultMutableTreeNode rootNode;
 	private DefaultTreeModel model;
 	private JTree tree;
 	private TreeTransferHandler th;
 	//private CustomTreeEditor te;
 	
 	public TreePanel() {
 		init();
 	}
 
 	/**
 	 * Initialize the Tree component
 	 */
 	private void init() {
 		th = new TreeTransferHandler();
 		rootNode = new DefaultMutableTreeNode(ROOT);
 		model = new DefaultTreeModel(rootNode);
 		tree =  new JTree(model);
 		tree.setShowsRootHandles(true); // rende visibile il nodo root
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); //solo un nodo alla volta � selezionabile
 		tree.setCellRenderer(new CustomCellRenderer());
 		tree.setDragEnabled(true);
 		tree.setDropMode(DropMode.ON_OR_INSERT);
 		tree.setTransferHandler(th);
 		tree.addTreeSelectionListener(this); //il listener per l'evento di selezione di un elemento -> devo aggiungere anche il frame per abilitare/disabilitare i pulsanti?
 		//editor delle celle
 		tree.setEditable(false); // fa in modo che l'albero non sia editabile
 		tree.setAutoscrolls(true);
 		setMappings(tree);
 		add(tree);
 		//pannello contenente il tree
 		JScrollPane scrollPane = new JScrollPane(tree,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		add(scrollPane);
 	}
 
 	/**
 	 * Reset the Tree
 	 */
 	protected void clear()	{
 		if (rootNode.getChildCount()!=0)	{
 			//chiedo conferma prima di resettare tutto
 			System.out.println("Ancestor: "+this.getTopLevelAncestor().getClass());
 			int choice = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), CLEARALL,"Warning!",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
 			if (choice == 1)	{
 				return;
 			}
 		}
 		rootNode.removeAllChildren();
 		//model.nodeStructureChanged(rootNode);
 		//model.reload();
 	}
 	
 
 	/**
 	 * Remove the current selected node
 	 */
 	protected void removeSelectedNode() {
         TreePath currentSelection = tree.getSelectionPath();
 		if (currentSelection != null) {
 			if (currentSelection.getParentPath() != null) {
 				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
 				String name = currentNode.toString();//((Componente) currentNode.getUserObject()).toString();
 				String message = DELETEMESSAGE + name + CONFIRMMESSAGE;
 				// Chiede conferma per la cancellazione dell'elemento selezionato
 				int choice = JOptionPane.showConfirmDialog(
 						this.getTopLevelAncestor(), message, "Warning!",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 				if (choice == 0) {
 					if (currentSelection != null) {
 						if (currentNode.getChildCount() != 0) {
 							message = name + NOTEMPTYMESSAGE;
 							// Chiede conferma per eliminare un componente contenente altri elementi
 							choice = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), message,"Warning!", JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
 						}
 						if (choice == 1) {
 							return;
 						}
 						model.removeNodeFromParent(currentNode);
 						tree.clearSelection();
 					}
 				}
 			}
         	else	{
         		//� stata selezionata la radice perci� avviso l'utente che non pu� essere cancellata->risolto: la root non pu� essere selezionata
         		JOptionPane.showMessageDialog(this.getTopLevelAncestor(),ROOTDELETE,"Error!",JOptionPane.ERROR_MESSAGE);
         	}
 		}
 		else	{
 			//Segnala che non c'� nulla selezionato quindi non pu� rimuovere nulla->da rivedere in disabilita Remove se non c'� nulla selezionato
 			JOptionPane.showMessageDialog(this.getTopLevelAncestor(),EMPTYSELECTION,"Error!",JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 
 	/**
 	 * Add a new node to the Tree
 	 * @param parent	The parent of the node
 	 * @param node		The node
 	 */
 	protected void addNode(DefaultMutableTreeNode parent, Componente node) {
 		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);
 		if (parent == null) {
 			parent = rootNode;
 		}/*	else	{
 			Componente compElem = (Componente)parent.getUserObject();
 			String type = compElem.getType();
 			if (type==ComponenteComposto.COMPOSTOTYPE)	{
 				((ComponenteComposto)compElem).aggiungiOpzione((ComponenteSemplice) node);
 			}
 			else if (type==ComponenteAlternative.ALTERNATIVETYPE)	{
 				((ComponenteAlternative)compElem).aggiungiOpzione((ComponenteSemplice)node);
 			}
 		}*/
 		//Verifico che il nodo genitore possa avere nodi figli anche se con il drag&drop � gi� esclusa come destinazione possibile (vedi canImport)
 		if (parent.getAllowsChildren()==false)	{
 			JOptionPane.showMessageDialog(this.getTopLevelAncestor(),parent.toString()+ADDCHILDRENNOTALLOWED,"Error!",JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		model.insertNodeInto(childNode, parent, parent.getChildCount());
 		//NOTA: Java SE7 pu� fare switch su String ma Java SE6 no!
		if (node.getType()==Testo.TEXTTYPE || node.getType()==Immagine.IMAGETYPE || node.getType()==Link.LINKTYPE)	{
 			childNode.setAllowsChildren(false);
 		}
 		if (!node.isSimple()){
 			parent=childNode;
 			ComponenteMolteplice parentComp = (ComponenteMolteplice)parent.getUserObject();
 			for (int i=0; i<parentComp.getOpzioni().size(); i++)	{
 				addNode(parent,parentComp.getOpzione(i));
 			}
 		}
 		model.reload(); //ricarico il modello dopo le modifiche
 		//Espande l'albero fino al nuovo componente e centra la vista sul nuovo componente
 		tree.scrollPathToVisible(new TreePath(childNode.getPath()));
 		//Seleziona l'elemtno appena creato
 		tree.setSelectionPath(new TreePath(childNode.getPath()));
 	}
 	
 	/**
 	 * Aggiunge le azioni di copia/taglia/incolla
 	 * @param tree
 	 */
 	private void setMappings(JTree tree)	{
 		ActionMap map = tree.getActionMap();
 	    map.put(TransferHandler.getCutAction().getValue(Action.NAME),TransferHandler.getCutAction());
 	    map.put(TransferHandler.getCopyAction().getValue(Action.NAME),TransferHandler.getCopyAction());
 	    map.put(TransferHandler.getPasteAction().getValue(Action.NAME),TransferHandler.getPasteAction());
 		
 		//disabilito le azioni di default di ctrl+x ctrl+c ctrl+v per avere un controllo migliore 
 		tree.getInputMap().put(KeyStroke.getKeyStroke("control C"), "none");
 		tree.getInputMap().put(KeyStroke.getKeyStroke("control X"), "none");
 		tree.getInputMap().put(KeyStroke.getKeyStroke("control V"), "none");
 	}
 
 	/**
 	 * TODO con i giusti command e listener!
 	 */
 	public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (ADD_COMMAND.equals(command)) {
         	ComponenteComposto composto1 = new ComponenteComposto("Composite1","Comp",0,0);
             addNode(null,composto1);
             Testo testo1 = new Testo("Testo1","Txt",0,0,"Sono un testo");
             addNode(null,testo1);
             Immagine img1 = new Immagine("Immagine1","Img",0,0,"Boh");
             addNode(null,img1);
             Link link1 = new Link("Link1", "lnk", 0, 0, "boh", "boh");
             addNode(null, link1);
             ComponenteAlternative alternative1 = new ComponenteAlternative("Alternative1","Alte",0,0);
             addNode(null,alternative1);
         } else if (REMOVE_COMMAND.equals(command)) {
             removeSelectedNode();
         } else if (CLEAR_COMMAND.equals(command)) {
             clear();
         }
 	}
 
 
 	/**
 	 * @param e
 	 */
 	public void valueChanged(TreeSelectionEvent e) {
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
 		if (node == null || node.isRoot())	{
 			tree.clearSelection();
 			//non � stato selezionato nulla o � stata selezionata la radice;
 			return;
 		}	else	{
 			Componente comp = (Componente) node.getUserObject();
 			System.out.println("Componente: "+comp.getNome());
 			if (!comp.isSimple())	{
 				Vector<ComponenteSemplice> elementi= ((ComponenteMolteplice)comp).getOpzioni();
 				System.out.println("Elementi del componente:");
 				for (int i=0; i<elementi.size(); i++)	{
 					System.out.println(elementi.elementAt(i).getNome());
 				}
 			}
 		}
 	}
 	
 	public JTree getTree()	{
 		return tree;
 	}
 	
 	public boolean isEmpty()	{
 		if ((((DefaultMutableTreeNode) model.getRoot()).getChildCount())!=0)	{
 			return false;
 		}
 		return true;
 	}
 	
 	public Vector<Componente> getComponenti()	{
 		Vector<Componente> comps = new Vector<Componente>();
 		for (int i=0; i< rootNode.getChildCount(); i++)	{
 			comps.add((Componente)((DefaultMutableTreeNode) rootNode.getChildAt(i)).getUserObject());
 		}
 		return comps;
 	}
 	
 	public void setComponenti(Vector<Componente> comps)	{
 		for (int i=0; i<comps.size(); i++)	{
 			addNode(null,(Componente)comps.get(i));
 		}
 	}
 	
 }
