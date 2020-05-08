 package doom;
 
 class Events {
   
     //event-manager
     public String eventManager (Gui window, String eventIndex, Inventory inventory, LevelManager levelManager, Player gamer) {
             char c = eventIndex.charAt(0);
            System.out.println(eventIndex);
             switch (c){
                 case '$' : return "m" + moneyEvent(window);
                 case 't' : triggerEvent(window, eventIndex.charAt(1)); return " ";
                 case '@' : gamer.setPlayerHealth(-400); return " ";
                case '0' : levelManager.setCurrentLevelIndex(Integer.parseInt(eventIndex.charAt(1) + "")); return " ";
                 case 'w' : if (inventory.fillInventory(eventIndex, window)) {
                                 window.setEventLabel("Your Inventory is already filled!"); return "yes"; } else {
                                 window.setEventLabel("You found a bottle of water!"); return " ";}
                 case 'k' : if (inventory.fillInventory(eventIndex, window)) {
                                 window.setEventLabel("Your Inventory is already filled!"); return "yes"; } else {
                                 window.setEventLabel("You found a key!"); return " ";}
                 case 's' : if (inventory.fillInventory(eventIndex, window)) {
                                 window.setEventLabel("Your Inventory is already filled!"); return "yes"; } else {
                                 window.setEventLabel("You found a sword!"); return " ";}
                 
                 default:    /*window.setVisibility(false); */return "e";
             }
     }
     
     //money-event
     public int moneyEvent(Gui window){
          double money = Math.random();
          int Pmoney = (int)((money *100) + 5);
          window.setEventLabel("You found "+ Pmoney +"$!");
          return Pmoney;
     }
     
     //trigger-event
     public void triggerEvent(Gui window, char index) {
         
         switch(index) {
             case 'r' : window.setEventLabel("Next to you is a red door!");
                 break;
             case 'y' : window.setEventLabel("Next to you is a yellow door!");
                 break;
             case '@' : window.setEventLabel("Next to you is a Dragon!");
                 break;
             default  : window.setEventLabel("Next to you is a black hole!");
                 break;
         }           
     }
 
     //should the programm delete the event-GameTile?
     public boolean delEvent(String eventIndex) {
         char c = eventIndex.charAt(0);
         
         switch (c) {
             case '$': return true;
             case 'w': return true;
             case 'k': return true;
             case 's': return true;
             default : return false;
         }
     }
     
     //should the programm print a new gamefield?
     public boolean refreshWorld(String eventIndex) {
         char c = eventIndex.charAt(0);
         
         switch (c) {
             case '0': return true;
             default : return false;
         }
     }
 }
