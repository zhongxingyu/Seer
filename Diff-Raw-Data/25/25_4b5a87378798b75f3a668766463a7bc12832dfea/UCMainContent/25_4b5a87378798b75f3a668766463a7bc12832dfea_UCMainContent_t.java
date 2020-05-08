 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BT.modules.UC;
 
 import BT.managers.CoordinateManager;
 import BT.managers.UC.UCActor;
 import BT.managers.UC.UCJoinEdge;
 import BT.managers.UC.UCPlaceManager;
 import BT.managers.UC.UCUseCase;
 import GUI.DrawingPane;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author Karel Hala
  */
 public final class UCMainContent {
     private JPanel mainContentPane;
     private JToggleButton selectedItemButton;
     private JToggleButton selectedJoinEdgeButton;
     private UCPlaceManager places;
     private DrawingPane drawingPane;
     private UCJoinEdge newJoinEdge;
     
     public UCMainContent()
     {
         this.places = new UCPlaceManager();
         this.mainContentPane = new JPanel(new BorderLayout());
         this.drawingPane = new DrawingPane(this.places);
         createMainPane();
     }
     
     public JPanel getMainContentPane ()
     {
         return this.mainContentPane;
     }
     
     public void createMainPane()
     {   
         this.drawingPane.getDrawing().setBackground(Color.WHITE);
         UCDrawingListeners alpha = new UCDrawingListeners(this.drawingPane.getDrawing(), this);
         this.drawingPane.getDrawing().addMouseMotionListener(alpha);
         this.drawingPane.getDrawing().addMouseListener(alpha);
         drawingPane.getDrawing().repaint();
         drawingPane.setButtonsListeners();
         JScrollPane myScrollPane = new JScrollPane(drawingPane.getDrawing());
         
         this.mainContentPane.add(myScrollPane, BorderLayout.CENTER);
     }
 
     public void setSelectedItemButton(JToggleButton toggleButton) {
         this.selectedItemButton = toggleButton;
     }
     
     public void setSelectedJoinEdgeButton(JToggleButton selectedButton) {
         this.selectedJoinEdgeButton = selectedButton;
        if (selectedButton == null)
        {
            this.places.removeJointEdge(newJoinEdge);
            newJoinEdge = null;
            this.drawingPane.getDrawing().repaint();
        }
     }
     
     public void mouseDragged(MouseEvent e, CoordinateManager dragged)
     {
         dragged.setX(e.getX());
         dragged.setY(e.getY());
         this.drawingPane.getDrawing().repaint();
     }
     
     public void drawingPaneClicked(MouseEvent evt) {
         if (this.selectedItemButton!=null){
             switch (this.selectedItemButton.getName()){
                 case "actor":
                         UCActor actor = new UCActor();
                         actor.setX(evt.getX());
                         actor.setY(evt.getY());
                         this.places.addActor(actor);
                         this.drawingPane.setPlaces(places);
 //                        objectClicked(actor);
                         
                         this.drawingPane.getDrawing().repaint();
                         System.out.println(this.selectedItemButton.getText());
                      break;
                     
                case "useCase":
                         UCUseCase useCase = new UCUseCase();
                         useCase.setX(evt.getX());
                         useCase.setY(evt.getY());
                         this.places.addUseCase(useCase);
                         this.drawingPane.setPlaces(places);
 //                        objectClicked(useCase);
                         
                         this.drawingPane.getDrawing().repaint();
                         System.out.println(this.selectedItemButton.getText());
                      break;
                 }
         }
     }
     
     public void clickedOnObject(CoordinateManager clickedObject) {      
         if (this.selectedJoinEdgeButton!=null)
         {
             if (this.newJoinEdge == null)
             {
                 this.newJoinEdge = new UCJoinEdge();
             }
             createJoinEdge(clickedObject);
             places.addJoinEdge(this.newJoinEdge);
             switch (this.selectedJoinEdgeButton.getName())
             {
                case "association":
                         System.out.println(this.selectedJoinEdgeButton.getText());
                      break;
                    
                case "uses":  
                         System.out.println(this.selectedJoinEdgeButton.getText());
                      break;
                    
                case "extend":  
                         System.out.println(this.selectedJoinEdgeButton.getText());
                      break;
             }
             
             if (this.newJoinEdge.getfirstObject() != null && this.newJoinEdge.getSecondObject() != null)
             {
                 this.newJoinEdge = null;
             }
         }
     }
     
     public void createJoinEdge(CoordinateManager clickedObject)
     {
         System.out.println("ssss");
         
         if (this.newJoinEdge.getfirstObject() == null)
         {
             this.newJoinEdge.setFirstObject(clickedObject);
         }
         else if (this.newJoinEdge.getSecondObject() == null)
         {
             this.newJoinEdge.setSecondObject(clickedObject);
         }       
         
     }
     
     public void drawingPanecheckMove(MouseEvent evt) {
         int x = evt.getX();
         int y = evt.getY();
         if (this.newJoinEdge != null)
         {
             if (this.newJoinEdge.getSecondObject() == null)
             {
                 this.newJoinEdge.setMouseCoordinates(x, y);
             }
         }
         UCActor actor = isActorUnderMouse(x, y);
         if (actor != null)
         {
             actor.setColor(Color.red);
         }
         UCUseCase usecase = isUseCaseUnderMouse(x, y);
         if (usecase != null)
         {
             usecase.setColor(Color.green);
         }
         this.drawingPane.getDrawing().repaint();
     }
     
     public UCActor isActorUnderMouse(int x, int y)
     {
         for (UCActor actor : places.getActors())
         {
             if (actor.isActor(x,y))
             {
                 return actor;
             }
             else
             {
                 actor.setBasicColor();
             }
         }
         return null;
     }
 
     public UCUseCase isUseCaseUnderMouse(int x, int y) {
        for (UCUseCase useCase : places.getUseCases())
         {
             if (useCase.isUseCase(x,y))
             {
                 return useCase;
             }
             else
             {
                 useCase.setBasicColor();
             }
         }
         return null;
     }
 
     public void objectDoubleClicked(CoordinateManager pressedObject) {
         String name = (String) JOptionPane.showInputDialog("Enter name of the object",pressedObject.getName());
         if (name!= null && !"".equals(name))
             pressedObject.setName(name);
         this.drawingPane.getDrawing().repaint();
     }
 
     public void setSelectedObject(CoordinateManager clickedObject) {
         if (clickedObject!=null)
         {
             this.drawingPane.setSelectedObject(clickedObject);
         }
         else
         {
             this.drawingPane.setSelectedObject(null);
         }
         this.drawingPane.getDrawing().repaint();
     }
 }
