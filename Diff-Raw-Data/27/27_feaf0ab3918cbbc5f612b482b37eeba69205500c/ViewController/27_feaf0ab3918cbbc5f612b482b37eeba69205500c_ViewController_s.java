 /*
  *                 Sun Public License Notice
  *
  * The contents of this file are subject to the Sun Public License
  * Version 1.0 (the "License"). You may not use this file except in
  * compliance with the License. A copy of the License is available at
  * http://www.sun.com/
  *
  * The Original Code is NetBeans. The Initial Developer of the Original
  * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
  * Microsystems, Inc. All Rights Reserved.
  */
 package edu.duke.cabig.catrip.gui.dnd;
 
 import edu.duke.cabig.catrip.gui.common.ClassBean;
 import edu.duke.cabig.catrip.gui.panels.*;
 import edu.duke.cabig.catrip.gui.query.DCQLRegistry;
 import org.netbeans.graph.api.control.builtin.DefaultViewController;
 import org.netbeans.graph.api.model.builtin.GraphNode;
 import org.netbeans.graph.api.model.IGraphNode;
 import org.netbeans.graph.api.model.IGraphLink;
 import org.netbeans.graph.api.model.GraphEvent;
 
 import javax.swing.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import org.netbeans.graph.api.model.builtin.GraphPort;
 
 /**
  * @author Sanjeev Agarwal
  */
 public class ViewController extends DefaultViewController implements ActionListener {
     
     private JPopupMenu singleNodePopup;
     private JMenuItem miDelete;
     private JMenuItem miViewProp;
     private JMenuItem miShowClass;
     
     private JMenuItem miSetTarget;
     
     
     private JPopupMenu twoNodePopup;
     private JMenuItem miLinkCDE;
     
     private JPopupMenu graphPopup;
     private JMenuItem miDeleteAll;
     
     
     private VisualQueryDesignerPanel visualQueryDesignerPanel;
     
     public ViewController(VisualQueryDesignerPanel test) {
         this.visualQueryDesignerPanel = test;
         singleNodePopup = new JPopupMenu();
         
         miDelete = new JMenuItem("Delete Model element");
         miDelete.addActionListener(this);
         singleNodePopup.add(miDelete);
         
         miViewProp = new JMenuItem("Edit Model Properties/Attributes");
         miViewProp.addActionListener(this);
         singleNodePopup.add(miViewProp);
         
        singleNodePopup.add(new JMenuItem("Get Associated Domain Model Classes"));
        singleNodePopup.add(new JMenuItem("Get All Domain Model Classes"));
        
        miShowClass = new JMenuItem("Retrieve Semantically Equivalent Classes");
        miShowClass.addActionListener(this);
        singleNodePopup.add(miShowClass);
         
         
         miSetTarget = new JMenuItem("Set object as Target Object.");
         miSetTarget.addActionListener(this);
         singleNodePopup.add(miSetTarget);
         
         
        twoNodePopup = new JPopupMenu();
        miLinkCDE = new JMenuItem("Link Classes via CDE");
        miLinkCDE.addActionListener(this);
        twoNodePopup.add(miLinkCDE);
         
         
         graphPopup = new JPopupMenu();
         miDeleteAll = new JMenuItem("Delete All Nodes from Graph.");
         miDeleteAll.addActionListener(this);
         graphPopup.add(miDeleteAll);
         
         
     }
     
     protected JPopupMenu getPopupMenu() {
         // these 3 lines were added by sanjeev.
         Object[] selectedComponents = getHelper().getSelectedComponents();
         
         if (selectedComponents != null  &&  selectedComponents.length == 2 &&  (selectedComponents[0] instanceof IGraphNode) && (selectedComponents[1] instanceof IGraphNode))
             return twoNodePopup;
         
         if (selectedComponents == null  ||  selectedComponents.length != 1 ||  !(selectedComponents[0] instanceof IGraphNode))
             return graphPopup; // return base graph menu
         
         
         //useful code here..
 //        IGraphNode[] nodes = document.getSelectedComponents().getNodes();
 //        nodes[0].getID();
         
         return singleNodePopup;
         
     }
     
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == miDelete) {
             final Object[] selectedComponents = getHelper().getSelectedComponents();
             
             ArrayList nodes = new ArrayList();
             ArrayList links = new ArrayList();
             if (selectedComponents != null)
                 for (int i = 0; i < selectedComponents.length; i++) {
                 Object selectedComponent = getHelper().getModelComponent(selectedComponents[i]);
                 if (selectedComponent instanceof IGraphNode) {
                     nodes.add(selectedComponent);
                     // sanjeev  // DONE - delete all links that leads in or out from any ports of the node
                     GraphNode gn = (GraphNode) selectedComponent;
                     GraphPort[] gp = (GraphPort[]) gn.getPorts();
                     
                     for (int ii = 0; ii < gp.length; ii++){
                         GraphPort gpp = gp[ii];
                         IGraphLink[] gpl = null;
                         try{
                             gpl = gpp.getLinks();
                         } catch (Exception nulle){
                             gpl = null;
                         }
                         if(!(gpl == null)){
                             for (int jj = 0; jj < gpl.length; jj++){
                                 links.add(gpl[jj]);
                             }
                         }
                     }
                     // sanjeev
                     
                     
                 } else if (selectedComponent instanceof IGraphLink)
                     links.add(selectedComponent);
                 }
             final IGraphNode[] _nodes = (IGraphNode[]) nodes.toArray(new IGraphNode[nodes.size()]);
             final IGraphLink[] _links = (IGraphLink[]) links.toArray(new IGraphLink[links.size()]);
             visualQueryDesignerPanel.getDocument().removeComponents(GraphEvent.create(_nodes, _links));
             
         }
         //sanjeev : not required as properties are shown on click only..
         else if(e.getSource() == miViewProp){
             // show the properties in the properties pane..
             final Object[] selectedComponents = getHelper().getSelectedComponents();
             if ((selectedComponents != null) && selectedComponents.length == 1){
                 Object selectedComponent = getHelper().getModelComponent(selectedComponents[0]);
                 if (selectedComponent instanceof IGraphNode) {
                     //nodes.add(selectedComponent);
                     ClassBean node = ((ClassNode)selectedComponent).getAssociatedClassObject();  //getDisplayName();
                     popupPropertiesPanel(node);
                     
                 }
             }
             
             // @todo : Remove later, sanjeev dummy stitching code...
         } else if(e.getSource() == miShowClass){
             java.awt.EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     SemanticallyEquivalentClassListPanel ws= new SemanticallyEquivalentClassListPanel();
                     ws.main(null);
                 }
             });
             // @todo : Remove later, sanjeev dummy stitching code...
         } else if(e.getSource() == miLinkCDE){
             // show a dialog with main frame as parent.. send 2 graph nodes..
             
             final Object[] selectedComponents = getHelper().getSelectedComponents();
             ClassNode source = null;
             ClassNode target = null;
             
             if ((selectedComponents != null) && selectedComponents.length == 2){
                 Object selectedComponent = getHelper().getModelComponent(selectedComponents[0]);
                 if (selectedComponent instanceof IGraphNode) {
                     source = (ClassNode)selectedComponent;
                 }
                 selectedComponent = getHelper().getModelComponent(selectedComponents[1]);
                 if (selectedComponent instanceof IGraphNode) {
                     target = (ClassNode)selectedComponent;
                 }
                 visualQueryDesignerPanel.linkCDEs( source, target);
             }
             
             
             
         }
         
         else if(e.getSource() == miSetTarget){
             // set the current node class bean as target object..
             final Object[] selectedComponents = getHelper().getSelectedComponents();
             if (selectedComponents != null && selectedComponents.length == 1){
                 Object selectedComponent = getHelper().getModelComponent(selectedComponents[0]);
                 if (selectedComponent instanceof ClassNode) {
                     ClassNode cNode = (ClassNode)selectedComponent;
 //                    System.out.println("xxxxx TargetObject is:"+cNode.getAssociatedClassObject().getFullyQualifiedName());
                     // first get the old target node from Registry. remove target status from it
                     DCQLRegistry.getTargetNode().isNotTargetNode();
                     visualQueryDesignerPanel.repaint();  // to re-render the old Target node..
                     // set the new node as Target node.. both in the Node and in the registry..
                     cNode.setAsTargetNode();
                     DCQLRegistry.setTargetNode(cNode);
                     // TODO -  reverse the foreign associations and other objects for dcql.
                 }
             }
         }
         // delete all nodes.. clean registry and reset the graphNode Ids
         else if(e.getSource() == miDeleteAll){
             visualQueryDesignerPanel.getDocument().removeComponents(visualQueryDesignerPanel.getDocument().getComponents());
             visualQueryDesignerPanel.resetID();
             DCQLRegistry.clean();
         }
         
         
         
     }
     
     
     private void popupPropertiesPanel(ClassBean name){
         // put this code in a jpanel class.. doesn;t look good here..
         // can have a method like   visualQueryDesignerPanel.popupPropertiesPanel(nodeClass)
         visualQueryDesignerPanel.showNodePrperties(name);
         final JFrame jFrame = new JFrame("Edit Properties");
         
         JPanel jPanel = new javax.swing.JPanel();
         JButton button = new javax.swing.JButton();
         button.setText("Ok");
         button.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jFrame.setVisible(false);
             }
         });
         jPanel.add(button);
         jFrame.getContentPane().add(jPanel, java.awt.BorderLayout.SOUTH);
         
         
         PropertiesPanel propertiesPanel = new PropertiesPanel();
         propertiesPanel.setMainFrame(visualQueryDesignerPanel.getMainFrame());
         propertiesPanel.showNodeProperties(name);
         jFrame.getContentPane().add(propertiesPanel, java.awt.BorderLayout.CENTER);
         jFrame.setBounds(100,100,300,200);
         jFrame.setVisible(true);
         //
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 }
 
 
