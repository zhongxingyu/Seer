 package supervision;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import model.Schedule;
 
 /* ListLivraison.java requires no other files. */
 public class ListLivraison extends JPanel {
 	
     private JTree tree;
     private DefaultTreeModel treeModel;
     private ArrayList<Schedule> schedules;
 	private JButton jButtonSupprimer;
     private ArrayList<Integer> idSchedules; /*numero de ligne de la Jlist 
     ou commence la liste des livraisons de la plage horaire*/
 
     public ListLivraison(){
         super(new BorderLayout());
     }
     
     public void setjButtonSupprimer(JButton jb){
     	this.jButtonSupprimer=jb;
     }
     
 	//Teste la presence d'un noeud dans l'arbre par l'id 
     public boolean livExists(String addr){
 		//recherche de la racine
 		DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
 		
 		//Scan de l'arbre par plage horaire puis par children de ces plages horaires.
 		DefaultMutableTreeNode nodeBuffer;
     	for(int i = 0;i<treeModel.getChildCount(root);i++){
 			nodeBuffer = (DefaultMutableTreeNode)treeModel.getChild(root, i);
 			for(int j = 0;j<treeModel.getChildCount(nodeBuffer);j++){
 				if(addr.equals(((DefaultMutableTreeNode)treeModel.getChild(nodeBuffer, j)).toString())){
 					//noeud trouve
 					return true;
 				}
 			}
     	}
     	return false;
     }
     
 	//R�cup�re l'id d'une adresse de livraison
    private Integer getIdSchedule(Schedule schedule) {
     	for(int i = 0;i<schedules.size();i++){
     		if(schedule.getEndTime()==schedules.get(i).getEndTime()){
     			return i;
     		}
     	}
     	return -1;
     }
     
     public void addLiv (Schedule schedule, String addr){
 		//recuperation de l'indice du noeud de plage horaire voulue
     	Integer idSchedule=getIdSchedule(schedule);
 		//recupere le noeud relatif a une plage horaire
 		DefaultMutableTreeNode scheduleNode=(DefaultMutableTreeNode)((DefaultMutableTreeNode)treeModel.getRoot()).getChildAt(idSchedule);
 		//insere un noeud dans la plage horaire trouvee au dessus
     	treeModel.insertNodeInto(new DefaultMutableTreeNode(addr),scheduleNode, scheduleNode.getChildCount());
     }
 	
     /*Supprimer une livraison de la liste*/
     public void remLiv (){
 		//recupere le noeud selectionne
     	DefaultMutableTreeNode node = ((DefaultMutableTreeNode)tree.getLastSelectedPathComponent());
 		
 		//supprime le noeud en question
     	treeModel.removeNodeFromParent(node);
     }
     
     public void setSchedule(ArrayList<Schedule> aschedules) {
         schedules = aschedules;
 		//d�finition de la racine de l'arbre et declaration du model de l'arbre (contient la totalite des donnees et conditionne le comportement de l'arbre)
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Plages horaires");
         treeModel = new DefaultTreeModel(root);
 		
 		//creee les noeuds relatifs aux plages horaires
         for(int i=0;i<schedules.size();i++){
 			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(schedules.get(i).getSliceString());
 			//treeNode.setAllowsChildren(true);
             treeModel.insertNodeInto(treeNode, root, i);
         }
         
         //creee l'arbre, edite son mode de fonctionnement, son etat initial et le met dans le scrollpane
         tree = new JTree(treeModel);
         tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         tree.setSelectionPath(new TreePath(root));
         JScrollPane listScrollPane = new JScrollPane(tree);
         add(listScrollPane, BorderLayout.CENTER);
 
         /*listener qui detecte une selection sur la liste
         et enable le bouton supprimer s'il s'agit d'une livraison*/
     	tree.addTreeSelectionListener(new TreeSelectionListener() {
 			
 			@Override
 			public void valueChanged(TreeSelectionEvent e) {
 				
 				//la ligne selectionnee est une livraison?
 				if(((DefaultMutableTreeNode)tree.getLastSelectedPathComponent()).getLevel()>1){
 					//System.out.println("liv");
 					jButtonSupprimer.setEnabled(true);
 				}
 				else{
 					jButtonSupprimer.setEnabled(false);
 				}
 			}
 		});
     }
 
 	//Retourne l'arbre
     public JTree getList() {
         return tree;
     }
 
 	//Retourne le model de l'arbre
     public DefaultTreeModel getListModel() {
         return treeModel;
     }
     
 }
