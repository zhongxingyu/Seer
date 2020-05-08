 /*
  * FBDPaletteBar.java
  *
  * Created on June 25, 2007, 8:03 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.gatech.statics.modes.fbd;
 
 import edu.gatech.statics.application.ui.*;
 import com.jmex.bui.BButton;
 import com.jmex.bui.BComboBox;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.BImage;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.icon.BIcon;
 import com.jmex.bui.icon.ImageIcon;
 import com.jmex.bui.layout.GroupLayout;
 import edu.gatech.statics.application.StaticsApplication;
 /**
  *
  * @author Calvin Ashmore
  */
 public class FBDPaletteBar extends Toolbar {
     
     private BButton returnButton;
     private BButton resetButton;
     private BButton nextButton;
     private FBDWorld fbd;
     private FBDIcon fbdIcon;
     
     /** Creates a new instance of FBDPaletteBar */
     public FBDPaletteBar(final FBDWorld fbd) {
         super(GroupLayout.makeHoriz(GroupLayout.LEFT));
         setStyleClass("info_window");
         this.fbd = fbd;
         
         returnButton = new BButton("Return", new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 getApp().loadExercizeWorld();
             }
         }, "return");
         
         resetButton = new BButton("Reset", new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 //getApp().loadExercizeWorld();
                 fbd.reset();
             }
         }, "reset");
         
         nextButton = new BButton("next", new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 if(!fbd.isLocked())
                     performCheck();
                 else getApp().loadEquation(fbd);
             }
         }, "next");
         
         if(fbd.isLocked()) {
             nextButton.setText("Equation");
             resetButton.setEnabled(false);
         } else nextButton.setText("Check");
         
         
         // THIS is the container that will have the drop down for other FBDs below it.
         BContainer controlContainer = new BContainer(GroupLayout.makeVert(GroupLayout.TOP));
         BContainer actionContainer = new BContainer(GroupLayout.makeHoriz(GroupLayout.LEFT));
         actionContainer.add(returnButton);
         actionContainer.add(resetButton);
         actionContainer.add(nextButton);
         controlContainer.add(actionContainer);
         
         BComboBox fbdCombo = new BComboBox();
         for(FBDWorld fbd1 : StaticsApplication.getApp().getExercise().getDiagrams()) {
             fbdCombo.addItem(fbd1);
         }
         // add listeners to fbdCombo
         fbdCombo.selectItem(fbd);
         //controlContainer.add(fbdCombo);
         add(controlContainer);
         
         fbdIcon = fbd.getIcon(); //new FBDIcon(getApp(), fbd);
         //add(fbdIcon);
         //add(new BLabel(fbdIcon.getIcon()));
         add(fbdIcon.makeLabel());
         
         //ActionListener warningListener = new WarningListener();
         ActionListener toolListener = new ToolListener();
         
         try {
         
             BIcon icon;
             BButton button;
             
             BContainer buttonBar1 = new BContainer(GroupLayout.makeHoriz(GroupLayout.LEFT));
 
             /*icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/label.png")));
             button = new BButton(icon, toolListener, "label");
             buttonBar1.add(button);*/
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/force.png")));
             button = new BButton(icon, toolListener, "force");
            button.setTooltipText(java.util.ResourceBundle.getBundle("rsrc/Strings").getString("fbd_tooltip_force"));
             buttonBar1.add(button);
             /*
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/weight.png")));
             button = new BButton(icon, buttonListener2, "weight");
             add(button);
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/reactionForce.png")));
             button = new BButton(icon, buttonListener2, "reaction");
             add(button);
             */
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/moment.png")));
             button = new BButton(icon, toolListener, "moment");
            button.setTooltipText(java.util.ResourceBundle.getBundle("rsrc/Strings").getString("fbd_tooltip_moment"));
             buttonBar1.add(button);
             
             add(buttonBar1);
             
             /*
             add(new Spacer(20,1));
             
             GroupLayout bar2Layout = GroupLayout.makeHoriz(GroupLayout.LEFT);
             bar2Layout.setOffAxisJustification(GroupLayout.TOP);
             BContainer buttonBar2 = new BContainer(bar2Layout);
             BContainer vertGroup;
             
             vertGroup = new BContainer(GroupLayout.makeVert(GroupLayout.TOP));
 
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/pulley.png")));
             button = new BButton(icon, warningListener, "pulley");
             vertGroup.add(button);
             
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/roller.png")));
             button = new BButton(icon, warningListener, "roller");
             vertGroup.add(button);
             
             buttonBar2.add(vertGroup);
             vertGroup = new BContainer(GroupLayout.makeVert(GroupLayout.TOP));
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/fixed.png")));
             button = new BButton(icon, warningListener, "fixed");
             vertGroup.add(button);
             
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/pin.png")));
             button = new BButton(icon, warningListener, "pin");
             vertGroup.add(button);
             
             buttonBar2.add(vertGroup);
             vertGroup = new BContainer(GroupLayout.makeVert(GroupLayout.TOP));
             
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/cable.png")));
             button = new BButton(icon, warningListener, "cable");
             vertGroup.add(button);
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/plate.png")));
             button = new BButton(icon, warningListener, "plate");
             vertGroup.add(button);
             
             buttonBar2.add(vertGroup);
             vertGroup = new BContainer(GroupLayout.makeVert(GroupLayout.TOP));
             
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/beam.png")));
             button = new BButton(icon, warningListener, "beam");
             vertGroup.add(button);
             
             buttonBar2.add(vertGroup);
             vertGroup = new BContainer(GroupLayout.makeVert(GroupLayout.TOP));
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/point.png")));
             button = new BButton(icon, warningListener, "point");
             vertGroup.add(button);
 
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/FBD_Interface/centerOfMass.png")));
             button = new BButton(icon, warningListener, "centerOfMass");
             vertGroup.add(button);
             
             buttonBar2.add(vertGroup);
             
             add(buttonBar2);*/
         
         } catch(Exception e) {}
     }
 
     public void activate() {
         //fbdIcon.update();
     }
     
     
     public void enableEquationButton() {nextButton.setEnabled(true);}
     
     private class ToolListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             if(fbd.isLocked())
                 return;
             
             if(event.getAction().equals("force")) {
                 CreateForceTool2D tool = new CreateForceTool2D(fbd);
                 tool.activate();
             }
             if(event.getAction().equals("moment")) {
                 CreateMomentTool2D tool = new CreateMomentTool2D(fbd);
                 tool.activate();
             }
             if(event.getAction().equals("label")) {
             
                 LabelSelector tool = new LabelSelector(fbd, FBDPaletteBar.this);
                 tool.activate();
             }
         }
     }
     
     /*
     private class WarningListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             final BPopupWindow popup = new BPopupWindow(FBDPaletteBar.this, new BorderLayout(5,5));
             popup.setModal(true);
             HTMLView view2 = new HTMLView();
             view2.setStyleSheet("infoWindow");
             view2.setContents(
                     "<html><body>" +
                     "<h3>WARNING</h3>" +
                     "You can not use this tool in a Free Body Diagram.<br>" +
                     "You may use Moments and Forces, but not others." +
                     "</body></html>");
 
             popup.add(view2, BorderLayout.CENTER);
             popup.popup(350,400,true);
             popup.setBackground(popup.getState(), new TintedBackground(new ColorRGBA(.7f,.6f,.6f,.8f)));
             popup.addListener(new MouseListener() {
                 public void mousePressed(MouseEvent event) {}
                 public void mouseReleased(MouseEvent event) {
                     popup.dismiss();
                 }
                 public void mouseEntered(MouseEvent event) {}
                 public void mouseExited(MouseEvent event) {}
             });
         }
     }*/
     
     protected void performCheck() {
         System.out.println("performing FBD check...");
         
         if(fbd.checkDiagram()) {
             System.out.println("test passed!");
             fbd.createEquationWorld();
             enableEquationButton();
             fbd.setLocked();
             
             nextButton.setText("Equation");
             resetButton.setEnabled(false);
             getApp().loadEquation(fbd);
             
         } else {
             System.out.println("test failed!");
         }
     }
 }
