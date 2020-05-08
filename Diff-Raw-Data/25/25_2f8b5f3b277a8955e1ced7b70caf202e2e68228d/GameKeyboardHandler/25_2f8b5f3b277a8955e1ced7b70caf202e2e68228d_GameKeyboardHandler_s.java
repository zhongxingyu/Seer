 package com.thisisdinosaur.protosaurus.client;
 
 
 public class GameKeyboardHandler extends KeyboardHandler{
 
     private MapDrawer mapDrawer;
     private MapLogic mapLogic;
     
     public GameKeyboardHandler (MapLogic mapLogic, MapDrawer mapDrawer) {
         this.mapDrawer = mapDrawer;
         this.mapLogic = mapLogic;
     }
     
     @Override
     public void keyDown(int keyCode) {
        System.out.println(keyCode);
         switch (keyCode) {
         
             // w
             case 87:
                 mapDrawer.pan(0, 20);
             break;
             
             // d
             case 68:
                 mapDrawer.pan(-20, 0);
             break;
             
             // s
             case 83:
                 mapDrawer.pan(0, -20);
             break;
                
             // a
             case 65:
                 mapDrawer.pan(20, 0);
             break;
             
             //SPACE
             case 32:
             	
             break;
         }
         
     }
     
 }
