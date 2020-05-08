 
 package edu.duke.cabig.catrip.gui.dnd;
 
 import edu.duke.cabig.catrip.gui.common.AttributeBean;
 import edu.duke.cabig.catrip.gui.common.AttributeBeanComparator;
 import edu.duke.cabig.catrip.gui.common.ClassBean;
 import edu.duke.cabig.catrip.gui.common.ForeignAssociationBean;
 import edu.duke.cabig.catrip.gui.discovery.DomainModelMetaDataRegistry;
 import edu.duke.cabig.catrip.gui.panels.VisualQueryDesignerPanel;
 import edu.duke.cabig.catrip.gui.query.DCQLRegistry;
 import java.util.Collections;
 import org.netbeans.graph.api.IGraphEventHandler;
 import org.netbeans.graph.api.model.GraphEvent;
 import org.netbeans.graph.api.model.IGraphLink;
 import org.netbeans.graph.api.model.IGraphNode;
 import org.netbeans.graph.api.model.IGraphPort;
 import org.netbeans.graph.api.model.builtin.GraphLink;
 import org.netbeans.graph.api.model.builtin.GraphNode;
 import org.netbeans.graph.api.model.builtin.GraphPort;
 
 import javax.swing.undo.UndoableEdit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
 import java.util.ArrayList;
 import org.netbeans.graph.api.model.ability.IDirectionable;
 import org.openide.util.Utilities;
 
 
 
 /**
  * @author Sanjeev Agarwal
  */
 public class EventHandler extends IGraphEventHandler {
     
     private VisualQueryDesignerPanel visualQueryDesignerPanel;
     
     public EventHandler(VisualQueryDesignerPanel test) {
         this.visualQueryDesignerPanel = test;
     }
     
     public void setSourcePort(IGraphLink link, IGraphPort sourcePort) {
         ((GraphLink) link).setSourcePort((GraphPort) sourcePort);
     }
     
     public void setTargetPort(IGraphLink link, IGraphPort targetPort) {
         ((GraphLink) link).setTargetPort((GraphPort) targetPort);
     }
     
     public boolean isLinkCreateable(IGraphPort sourcePort, IGraphPort targetPort) {
         return true;
     }
     
     public void createLink(IGraphPort sourcePort, IGraphPort targetPort) {
 
          // TODO - Check for the null ports before you try to create a link at all..
          // This is to avoide the null pointer exception when the target link is missed..
         
         // sanjeev
         boolean samePort = sourcePort.equals(targetPort);
         boolean sameNode = ((GraphPort) sourcePort).getNode().equals( ((GraphPort) targetPort).getNode());
         
         int sourceDirection = 0;//((GraphPort) sourcePort).getDirection();
         int targetDirection = 0;//((GraphPort) targetPort).getDirection();
         try{
             sourceDirection = ((GraphPort) sourcePort).getDirection();
             targetDirection = ((GraphPort) targetPort).getDirection();
         }catch (Exception ne){}
         
         // if it is a CDE link... create a foreign association...
         if ((sourceDirection == IDirectionable.BOTTOM_LEFT) && (targetDirection == IDirectionable.BOTTOM_LEFT) && !samePort && !sameNode){
 //            System.out.println("XXXX creating a foreign association..");
 //
 //            System.out.println("XXXX  Attribute name is :"+ ( (AttributePort) sourcePort).getAttributeName()  );
 //            System.out.println("XXXX  class is :"+ ((ClassNode)sourcePort.getNode()).getAssociatedClassObject().getFullyQualifiedName()  );
             //  - add the foreign association here..
             ForeignAssociationBean foreignAssociationBean = new ForeignAssociationBean();
             ClassBean leftClassBeanObject = ((ClassNode)sourcePort.getNode()).getAssociatedClassObject();
             foreignAssociationBean.setLeftObj(leftClassBeanObject);
             foreignAssociationBean.setRighObj(((ClassNode)targetPort.getNode()).getAssociatedClassObject());
             foreignAssociationBean.setLeftProperty(( (AttributePort) sourcePort).getAttributeName());
             foreignAssociationBean.setRightProperty(( (AttributePort) targetPort).getAttributeName());
             // add a foreign association with the node..
             leftClassBeanObject.setHasForeignAssociations(true);
             leftClassBeanObject.addForeignAssociation(foreignAssociationBean);
             
             
             // now create the visual link..
             GraphLink link = new GraphLink();
             link.setID(visualQueryDesignerPanel.createID("link"));
             link.setSourcePort((GraphPort) sourcePort);
             link.setTargetPort((GraphPort) targetPort);
             visualQueryDesignerPanel.getDocument().addComponents(GraphEvent.createSingle(link));
         }
         
         // if it is a normal link.. create a normal association...
         if ( ((sourceDirection == IDirectionable.RIGHT) || (sourceDirection == IDirectionable.LEFT)) && ((targetDirection == IDirectionable.RIGHT) || (targetDirection == IDirectionable.LEFT))  && !samePort && !sameNode ){
             // this is a class assoctaion link...
 //            System.out.println("XXXX creating a local/class association..");
             
             ClassNode leftNode = (ClassNode)sourcePort.getNode();
             ClassNode rightNode = (ClassNode)targetPort.getNode();
             ClassBean leftClassBeanObject = leftNode.getAssociatedClassObject();
             leftClassBeanObject.setHasAssociations(true);
             leftClassBeanObject.addAssociation(rightNode.getAssociatedClassObject());
 //            leftClassBeanObject.
             
             
             // now create the visual link..
             GraphLink link = new GraphLink();
             link.setID(visualQueryDesignerPanel.createID("link"));
             link.setSourcePort((GraphPort) sourcePort);
             link.setTargetPort((GraphPort) targetPort);
             visualQueryDesignerPanel.getDocument().addComponents(GraphEvent.createSingle(link));
         }
         
         // show the error msg that do not link to itself
         if (samePort || sameNode){
             if ((sourceDirection == IDirectionable.BOTTOM_LEFT) && (targetDirection == IDirectionable.BOTTOM_LEFT)){
                 visualQueryDesignerPanel.showWarning("You can not link CDEs in the same class.");
             } else {
                 visualQueryDesignerPanel.showWarning("You can not link to same class.");
             }
         }
         
         // show the error msg that do not link class to CDE.
         if( ((sourceDirection == IDirectionable.RIGHT) || (sourceDirection == IDirectionable.LEFT)   || (targetDirection == IDirectionable.RIGHT) || (targetDirection == IDirectionable.LEFT)   ) &&  ((sourceDirection == IDirectionable.BOTTOM_LEFT) || (targetDirection == IDirectionable.BOTTOM_LEFT)) ){
             visualQueryDesignerPanel.showWarning("You can not link CDE to a class.");
         }
         
         
         // sanjeev
         
         
     }
     
     public void componentsSelected(GraphEvent event) {
         visualQueryDesignerPanel.getDocument().selectComponents(event);
         // sanjeev : show the properties of this link in the properties table..
         Object[] selectedComponents =  visualQueryDesignerPanel.getDocument().getSelectedComponents().getNodes();
         
         if ((selectedComponents != null) && selectedComponents.length == 1){
             Object selectedComponent = selectedComponents[0];
             if (selectedComponent instanceof IGraphNode) {
                 ClassBean node = ((ClassNode)selectedComponent).getAssociatedClassObject(); //getDisplayName();
                 visualQueryDesignerPanel.showNodePrperties(node);
             }
         }
         
         
     }
     
     public boolean isAcceptable(IGraphNode node, DataFlavor[] dataFlavors) {
         return true;
 //            final DataFlavor dataFlavor = DataFlavor.selectBestTextFlavor (dataFlavors);
 //            System.out.println (dataFlavor);
 //            return dataFlavor != null;
     }
     
     public void accept(IGraphNode dropNode, Transferable transferable) {
         String value = null;
         try {
             value = (String) transferable.getTransferData(DataFlavor.stringFlavor);
         } catch (UnsupportedFlavorException e) {
             e.printStackTrace(); // TODO
         } catch (IOException e) {
             e.printStackTrace(); // TODO
         }
         
        ClassBean cBean = DomainModelMetaDataRegistry.lookupClassByRefId(value).clone();
         
         if (cBean != null){
             ClassNode node = null;//GraphNode node = null;
             try {
                 node = (ClassNode) ClassNode.class.newInstance(); // set the information of the GraphNode type with ClassBean itself.
                 node.setID(visualQueryDesignerPanel.createID(cBean.getId()));
                 node.setDisplayName(cBean.getClassName());
 //                node.setIcon( Utilities.loadImage(cBean.getIcon())); 
                 node.setIcon( cBean.getIcon()); 
                 addAttrubutePorts(node, cBean);
                 node.setAssociatedClassObject(cBean);// attach a classBean instance with this Graph node.
                 String toolTipText = "<html>Service Name: <b>"+cBean.getServiceName()+"</b><br>Package Name: <b>"+cBean.getPackageName()+"</b></html>";
                 node.setTooltipText(toolTipText);
             } catch (InstantiationException e) {
                 e.printStackTrace(); // TODO
             } catch (IllegalAccessException e) {
                 e.printStackTrace(); // TODO
             }
             visualQueryDesignerPanel.getDocument().addComponents(GraphEvent.createSingle(node));
             
             // set the first object as target object..
             if (visualQueryDesignerPanel.getLastID() == 1){
                 node.setAsTargetNode();
                 DCQLRegistry.setTargetNode(node);
             }
             
             return;
         }
         
     }
     
     public void undoableEditHappened(UndoableEdit edit) {
     }
     
     public void notifyModified() {
     }
     
     
     
     
     
     // sanjeev
     private void addAttrubutePorts(GraphNode node, ClassBean classBean){
         
         ArrayList attributes = classBean.getAttributes();
         Collections.sort(attributes, new AttributeBeanComparator());
         for (int k = attributes.size()-1; k >= 0; k--) {
             AttributeBean aBean = (AttributeBean)attributes.get(k);
             node.addPort( new AttributePort(aBean.getCDEName(),aBean.getAttributeName(),IDirectionable.BOTTOM_LEFT));
         }
         
     }
     // sanjeev
     
     
 }
