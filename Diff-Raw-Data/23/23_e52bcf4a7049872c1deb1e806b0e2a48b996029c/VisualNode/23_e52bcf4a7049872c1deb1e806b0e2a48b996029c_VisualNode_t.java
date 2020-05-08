 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package visual.node;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import atom.AtomFloat;
 import dataScene.DataScene;
 import data.DataNode;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import visual.node.PortGroup.PortType;
 import visual.scene.VisualScene;
 import org.netbeans.api.visual.action.ActionFactory;
 import org.netbeans.api.visual.action.TextFieldInplaceEditor;
 import org.netbeans.api.visual.action.WidgetAction;
 import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
 import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
 import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetEvent;
 import org.netbeans.api.visual.action.WidgetAction.WidgetFocusEvent;
 import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
 import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
 import org.netbeans.api.visual.action.WidgetAction.WidgetMouseWheelEvent;
 import org.netbeans.api.visual.laf.LookFeel;
 import org.netbeans.api.visual.layout.LayoutFactory;
 import org.netbeans.api.visual.model.ObjectState;
 import org.netbeans.api.visual.widget.LabelWidget;
 import org.netbeans.api.visual.widget.Widget;
 import overtoneinterface.IUGen;
 import overtoneinterface.IUGenInfo;
 import overtoneinterface.TestUGen;
 import trie.Trie;
 import visual.scene.Connection;
 
 /**
  *
  * @author Jon Rose
  */
 public class VisualNode extends Widget {
 
     private WidgetAction editorAction;
     protected LabelWidget labelWidget;
     LookFeel lookFeel = getScene().getLookFeel();
     PortGroup outputPorts;
     PortGroup inputPorts;
     protected int numInputs = 0;
     protected int numOutputs = 0;
     protected int portWidth;
     protected int borderRadius = 10;
     protected Rectangle area;
     protected VisualScene visualScene;
     NodeMenu menu;
     DataScene dataScene;
     DataNode dataNode;
     NodeCreator dataNodeCreator;
     protected float value = 0;
     protected double mouseX;
     protected double mouseY;
     protected double lastX;
     protected double lastY;
     protected double xVelocity;
     protected double yVelocity;
     protected double width = 1;
     public static String UI = "ui";
     public static String DATA = "data";
     protected String type;
     private boolean mouseDragged = false;
     NodeBorder border;
     private DataNode lastDataNode;
     protected Color fillColor = Color.white;
     protected Color selectedFillColor = Color.lightGray;
     protected Color textColor = Color.BLACK;
     private TestUGen ugen;
 
     public VisualNode(VisualScene scene) {
         super(scene);
         visualScene = scene;
         editorAction = ActionFactory.createInplaceEditorAction(new LabelTextFieldEditor(this));
 
         this.setLayout(LayoutFactory.createAbsoluteLayout());
         border  = new NodeBorder(visualScene, borderRadius, 5, 3, new Color(255, 255, 255), new Color(250, 250, 250), 3);
 
         createLabelWidget(scene);
         createInputPorts(scene);
         createOutputPorts(scene);
 
         scene.addNode(this);
     }
 
     /**
      * Implements the widget-state specific look of the widget.
      * @param previousState the previous state
      * @param state the new state
      */
     @Override
     public void notifyStateChanged(ObjectState previousState, ObjectState state) {
         LookFeel lookFeel = getScene().getLookFeel();
         labelWidget.setBorder(border);
         labelWidget.setForeground(state.isSelected() ? Color.black : Color.black);
         border.setFill(state.isSelected() ? new Color(200, 200, 200) : Color.white);
     }
 
     private void createOutputPorts(VisualScene scene) {
         outputPorts = new PortGroup(this, numOutputs, PortType.OUTPUT);
         addChild(outputPorts);
         outputPorts.bringToFront();
     }
 
     private void createInputPorts(VisualScene scene) {
         inputPorts = new PortGroup(this, numInputs, PortType.INPUT);
         inputPorts.setPreferredLocation(new Point(0, -22));
         addChild(inputPorts);
         inputPorts.bringToFront();
     }
 
     private void createLabelWidget(VisualScene scene) {
         this.setBackground(new Color(0, 0, 0, 0));
         border.setFill(Color.white);
         border.setDraw(Color.gray);
         labelWidget = new LabelWidget(visualScene);
         labelWidget.setFont(new Font("verdana", Font.BOLD, 13));
         labelWidget.getActions().addAction(editorAction);
         labelWidget.setLabel("            ");
         labelWidget.getActions().addAction(ActionFactory.createResizeAction());
         System.out.println(labelWidget.getBounds());
         labelWidget.setPreferredLocation(new Point(borderRadius / 2, 0));
 
         addChild(labelWidget);
     }
 
     public void removeLabelEditor() {
         labelWidget.getActions().removeAction(editorAction);
     }
 
     public UINode getUINode() {
         return null;
     }
 
     public void setValue(float f) {
         dataNode.setValue(new AtomFloat(f));
         value = dataNode.getValue().getFloat();
     }
 
     public float getValue() {
         return value;
     }
 
     /**
      * Set node tool tip
      * @param string
      */
     public void setToolTip(String string) {
         this.setToolTipText(string);
     }
 
     public void setLabel(String s) {
         labelWidget.setLabel(s);
     }
 
     public void setPortWidth(int w) {
         portWidth = w;
     }
 
     private void setArea(Rectangle bounds) {
         area = bounds;
     }
 
     Object getLabel() {
         return labelWidget;
     }
 
     public void setDataNode(DataNode d) {
         dataNode = d;
     }
 
     public Widget getOutputPort(int i) {
         return outputPorts.getPort(i);
     }
 
     public Widget getInputPort(int i) {
         return inputPorts.getPort(i);
     }
 
     public void setOutputPortToolTip(int i, String s) {
         outputPorts.setPortToolTip(i, s);
     }
 
     public void setInputPortToolTip(int i, String s) {
         inputPorts.setPortToolTip(i, s);
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public void isEditable() {
         labelWidget.isEnabled();
     }
 
     public void setParameters(IUGen source) {
         visualScene.removeNode(this);
         removeChild(inputPorts);
         removeChild(outputPorts);
         removeChild(labelWidget);
 
         labelWidget.setLabel(source.getName());
         addChild(labelWidget);
 
         width = labelWidget.getBounds().getHeight();
 
         numInputs = source.numInputs();
         numOutputs = source.numOutputs();
 
         inputPorts = new PortGroup(this, numInputs, PortType.INPUT);
         inputPorts.setPreferredLocation(new Point(borderRadius / 2, -16));
         addChild(inputPorts);
 
         outputPorts = new PortGroup(this, numOutputs, PortType.OUTPUT);
         outputPorts.setPreferredLocation(new Point(borderRadius / 2, 4));
         addChild(outputPorts);
 
 //        if (numOutputs > 0) {
 //            outputPorts.setSapcing((int) (50 / (numOutputs * 6)));
 //        }
 //        if (numInputs > 0) {
 //            inputPorts.setSapcing((int) (50 / (numInputs * 6)));
 //        }
 
        visualScene.getScene().addChild(this);
     }
 
     public Widget getLabelWidget() {
         return labelWidget;
     }
 
     public void update() {
         updateNode();
     }
 
     public void updateNode() {
     }
 
     public int getBorderRadius() {
         return borderRadius;
     }
 
     public void updateModel() {
     }
 
     public DataNode getDataNode() {
         return dataNode;
     }
 
     public Object getModelScene() {
         return visualScene;
     }
 
     public void render() {
         //    this.repaint();
     }
 
     public void setMouseDragged(boolean b) {
         mouseDragged = b;
     }
 
     public boolean getMouseDragged() {
         return mouseDragged;
     }
 
     public double getLastX() {
         return lastX;
     }
 
     public void setLastX(double lastX) {
         this.lastX = lastX;
     }
 
     public double getLastY() {
         return lastY;
     }
 
     public void setLastY(double lastY) {
         this.lastY = lastY;
     }
 
     public double getMouseX() {
         return mouseX;
     }
 
     public void setMouseX(double mouseX) {
         this.mouseX = mouseX;
     }
 
     public double getMouseY() {
         return mouseY;
     }
 
     public void setMouseY(double mouseY) {
         this.mouseY = mouseY;
     }
 
     public double getxVelocity() {
         return xVelocity;
     }
 
     public void setxVelocity(double xVelocity) {
         this.xVelocity = xVelocity;
     }
 
     public double getyVelocity() {
         return yVelocity;
     }
 
     public void setyVelocity(double yVelocity) {
         this.yVelocity = yVelocity;
     }
 
     public void renderLabel() {
         labelWidget.repaint();
     }
 
     public DataNode getLastDataNode() {
         return lastDataNode;
     }
 
     public int getNumOutputs() {
         return numOutputs;
     }
 
     public int getNumInputs() {
         return numInputs;
     }
 
     public void setMouseEvent(WidgetMouseEvent wme) {
         mouseX = wme.getPoint().getX();
         mouseY = wme.getPoint().getY();
         xVelocity = lastX - mouseX;
         yVelocity = lastY - mouseY;
 
         lastX = mouseX;
         lastY = mouseY;
     }
 
     public void removeHoverActions() {
         outputPorts.removeHoverActions();
         //      System.out.println("hover model node off");
     }
 
     public Connection[] getConnections() {
         ArrayList<Connection> connections = new ArrayList<Connection>();
         Connection[] conns;
         for (int i = 0; i < visualScene.getConnections().size(); i++) {
             VisualNode source = (VisualNode) visualScene.getConnections().get(i).getSource();
             VisualNode target = (VisualNode) visualScene.getConnections().get(i).getTarget();
             if (source.equals(this) || target.equals(this)) {
                 connections.add(visualScene.getConnections().get(i));
             }
         }
         conns = new Connection[connections.size()];
         return connections.toArray(conns);
     }
 
     public void createNewIUGenNode(IUGenInfo info) {
         if (info != null) {
             this.ugen = new TestUGen(info);
             this.setParameters(ugen);
             this.setToolTip(info.getDoc());
         } else {
             System.out.println("that is not a valid ugen");
         }
     }
 
     int getWidith() {
         if (labelWidget.getBounds() != null) {
             return (int) this.getBounds().getWidth();
         }
         return (int) width;
     }
 
     private class LabelTextFieldEditor implements TextFieldInplaceEditor {
 
         private VisualNode node;
 
         public LabelTextFieldEditor(VisualNode n) {
             node = n;
         }
 
         @Override
         public boolean isEnabled(Widget widget) {
             return true;
         }
 
         @Override
         public String getText(Widget widget) {
             return ((LabelWidget) widget).getLabel();
         }
 
         @Override
         public void setText(Widget widget, String text) {
             LabelWidget label = (LabelWidget) widget;
             label.setLabel(text);
 
             try {
                 //Trie returns the object if there is a single match, otherwisr it returns a tri of elements
                 createNewIUGenNode((IUGenInfo) visualScene.getTrie().searchExact(text));
             } catch (java.lang.ClassCastException e) {
                 Trie trie = (Trie) visualScene.getTrie().getTrieFor(text);
                 //  trie.searchExact(text)
                 System.out.println(trie.contents());
             }
             //VisualNode newNode = (VisualNode) widget.getParentWidget();
             //newNode.createDataNode(text);
         }
     }
 
     public void mouseClicked(Widget widget, WidgetMouseEvent wme) {
         this.setLastX(this.getMouseX());
         this.setLastY(this.getMouseY());
         this.setMouseX(wme.getPoint().getX());
         this.setMouseY(wme.getPoint().getY());
         updateUI();
     }
 
     public void mousePressed(Widget widget, WidgetMouseEvent wme) {
         this.setMouseDragged(true);
         this.setLastX(this.getMouseX());
         this.setLastY(this.getMouseY());
         this.setMouseX(wme.getPoint().getX());
         this.setMouseY(wme.getPoint().getY());
         updateUI();
     }
 
     public void mouseReleased(Widget widget, WidgetMouseEvent wme) {
         this.setMouseDragged(false);
     }
 
     public void mouseEntered(Widget widget, WidgetMouseEvent wme) {
         this.setLastX(this.getMouseX());
         this.setLastY(this.getMouseY());
         this.setMouseX(wme.getPoint().getX());
         this.setMouseY(wme.getPoint().getY());
         updateUI();
     }
 
     public void mouseExited(Widget widget, WidgetMouseEvent wme) {
     }
 
     public void mouseDragged(Widget widget, WidgetMouseEvent wme) {
         this.setLastX(this.getMouseX());
         this.setLastY(this.getMouseY());
         this.setMouseX(wme.getPoint().getX());
         this.setMouseY(wme.getPoint().getY());
         this.setxVelocity(this.getLastX() - this.getMouseX());
         this.setyVelocity(this.getMouseY() - this.getLastY());
         updateUI();
     }
 
     public void mouseMoved(Widget widget, WidgetMouseEvent wme) {
         updateUI();
 //        if(wme.getPoint().x > labelWidget.getBounds().getWidth() - borderRadius/2){
 //            this.getActions().addAction(ActionFactory.createResizeAction());
 //        } else {
 //            labelWid.getActions().removeAction(ActionFactory.createResizeAction());
 //        }
     }
 
     public void mouseWheelMoved(Widget widget, WidgetMouseWheelEvent wmwe) {
     }
 
     public void keyTyped(Widget widget, WidgetKeyEvent wke) {
     }
 
     public void keyPressed(Widget widget, WidgetKeyEvent wke) {
     }
 
     public void keyReleased(Widget widget, WidgetKeyEvent wke) {
     }
 
     public void focusGained(Widget widget, WidgetFocusEvent wfe) {
     }
 
     public void focusLost(Widget widget, WidgetFocusEvent wfe) {
         System.out.println("focus is lost on " + widget);
     }
 
     public void dragEnter(Widget widget, WidgetDropTargetDragEvent wdtde) {
     }
 
     public void dragOver(Widget widget, WidgetDropTargetDragEvent wdtde) {
     }
 
     public void dropActionChanged(Widget widget, WidgetDropTargetDragEvent wdtde) {
     }
 
     public void dragExit(Widget widget, WidgetDropTargetEvent wdte) {
     }
 
     public void drop(Widget widget, WidgetDropTargetDropEvent wdtde) {
     }
 
     public void updateUI() {
         //mouse event driven methods go here
         //       System.out.println("update");
     }
 }
 
