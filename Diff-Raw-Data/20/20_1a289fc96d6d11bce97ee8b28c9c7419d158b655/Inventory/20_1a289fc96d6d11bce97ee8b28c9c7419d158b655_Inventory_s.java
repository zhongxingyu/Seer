 package doom;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 class Inventory {
     private GameTile[] inventory;
     
     //Constructor to create 
     public Inventory () {
         this.inventory = new GameTile[8];
         for (int i = 0; i<this.inventory.length; i++) {
             this.inventory[i] = new EmptyTile(0, 0);
         }
     }
     
    //Getter for Inventory Item
     public GameTile getItem(int i){
         return this.inventory[i];
     }
     
 //    //Setter for Sword damage
 //    public void setSwordDamage(Gui mainWindow, int index){
 //        int damage = Integer.parseInt(inventory[index].substring(1));
 //        damage -= 20;
 //        inventory[index]="s"+ damage;
 //        if (Integer.parseInt(inventory[index].substring(1))<=0){
 //            deleteItem(index, mainWindow);
 //        }
 //        setInventoryIcons(mainWindow);
 //    }
     
     
     public boolean fillInventory (GameTile object, Gui mainWindow) {
         boolean indicator = false;
         for (int i = 0; i<this.inventory.length; i++) {
             if ((this.inventory[i].getID() == 1) && !indicator) {
                 this.inventory[i] = object;
                 indicator = true;
             }
         }
         setInventoryIcons(mainWindow);
         return indicator;
     }
 
     
     //Sets up all the Icons for items in the inventory
     private void setInventoryIcons(Gui mainWindow){
       javax.swing.JLabel[] labels = mainWindow.getInventoryLabels();
       for(int i=0; i < labels.length; i++){
            labels[i].setIcon(chooseIcon(inventory[i]));
       }
     }
     
    
     //Dictionary for the icon-locations
     private Icon chooseIcon(GameTile item){
        Icon icon = null;
        
        switch(item.getID()){
            case 1:    icon = new ImageIcon(getClass().getResource("/doom/icons/empty.png")); 
                         break;
            case 7:    icon = new ImageIcon(getClass().getResource("/doom/icons/water.png")); 
                         break;
            case 9:    icon = new ImageIcon(getClass().getResource("/doom/icons/"+ ((KeyTile)item).getColorIndex() +"Key.png"));
                         break;
            case 8:    icon = new ImageIcon(getClass().getResource("/doom/icons/sword.png"));
                         break;
        }
        
        return icon;
    }
 
 
     //Deletes an item from the inventory and rearranges the items
     public void deleteItem(int index, Gui mainWindow){
      inventory[index] = new EmptyTile(0, 0);
      GameTile[] newInventory = new GameTile[8];
      int ni=0;
      
      for(int oi= 0; oi<inventory.length; oi++){
          if(inventory[oi].getID() != 1){
              newInventory[ni]=inventory[oi];
              ni++;
          } else {
              newInventory[ni] = new EmptyTile(0, 0);
              ni++;
           }
       }
      
      inventory = newInventory;
       setInventoryIcons(mainWindow);
    }
 }  
