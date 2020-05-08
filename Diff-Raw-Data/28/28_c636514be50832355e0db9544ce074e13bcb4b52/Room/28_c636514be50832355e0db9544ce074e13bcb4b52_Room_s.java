 package gameWorld;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Random;
 import javax.swing.ImageIcon;
 import userInterface.CustomComponent;
 
 /**
  * Represents a Room, which can contain a Player, Zombie, Item and Doors
  *
  */
 public class Room extends CustomComponent {
 
     private String roomName;
     private ImageIcon roomBackground;
 
     private ArrayList<Player> playersInTheRoom = new ArrayList<Player>();
     private Zombie zombieOfTheRoom = null;
     private boolean hasZombie = false;
     private ArrayList<Door> doorsOfTheRoom = new ArrayList<Door>();
     Random randomGenerator = new Random();
     private Rectangle backWall;
     
     
     
     private Door[] doors;
         private ArrayList<Item> items = new ArrayList<Item>();
 
     public Room(int... destinations) {
 //		this.roomName = name;
 //		this.roomBackground = background;
         //this.setItemsOfTheRoom(items);
         //this.playersInTheRoom = players;
         //this.zombieOfTheRoom = zombie;
         //this.doorsOfTheRoom = doors;
         /*
          * if (!(zombie == null)) this.hasZombie = true; *
          */
         
         doors = new Door[destinations.length];
         for(int i = 0; i < doors.length; i++){
             doors[i] = new Door(destinations[i], i);
             this.add(doors[i]);
         }
         
         this.addKeyListener(new KeyAdapter(){
             @Override
             public void keyPressed(KeyEvent e){
                 System.out.println("KEY PRESSED: " + KeyEvent.getKeyText(e.getKeyCode()));
                 GameState.sendToNetwork(e.getKeyCode());
             }
         });
         
     }
 
     public void paintContent(Graphics2D g) {
         int w = getWidth();
         int h = getHeight();
 
         g.setColor(Color.LIGHT_GRAY);
         g.fillRect(0, 0, w, h);
 
         g.setColor(Color.BLACK);
         
         int xFactor = 5;
         int yFactor = 3;
 
         g.setColor(Color.BLACK);
 
         backWall = new Rectangle(w / xFactor, -1, w - w / xFactor * 2, h - h / yFactor + 1);
 
         g.drawLine(0, backWall.y + backWall.height + h / yFactor / 2, backWall.x, backWall.y + backWall.height);
         g.drawLine(w, backWall.y + backWall.height + h / yFactor / 2, backWall.x + backWall.width, backWall.y + backWall.height);
 
         g.draw(backWall);
 
         int doorWidth = w / 8;
         int doorHeight = h / 3;
         
         int pc = doorWidth / 4; // perspective correction
 
 
         for (int i = 0; i < doors.length; i++) {
             if (i == 0)      doors[0].setBounds(backWall.x + backWall.width / 2 - doorWidth / 2, backWall.y + backWall.height - doorHeight, doorWidth, doorHeight);
             else if (i == 1) doors[1].setBounds(w / 4 / 2 - doorWidth / 2 - pc / 2, backWall.y + backWall.height - doorHeight + doorHeight / 8, doorWidth - pc, doorHeight + doorHeight / 4);
             else if (i == 2) doors[2].setBounds(w - w / 4 / 2 - pc / 2, backWall.y + backWall.height - doorHeight + doorHeight / 8, doorWidth - pc, doorHeight + doorHeight / 4);
         }
 
         for (Item item : items) {
             
             int iw, ih;
 
             if (item.image == null) return;
        
            double sourceRatio = item.image.getWidth() / item.image.getHeight();
            double currentRatio = (item.w * w) / (item.h * h);
             
            System.out.println("sourceRatio = " + sourceRatio);
            System.out.println("currentRatio = " + currentRatio);

            if (sourceRatio >= currentRatio) { // 
                System.out.println(item + " src > current");
                 iw = (int)(item.w * w);
                ih = (int)(sourceRatio * iw);
 
             } else {
                System.out.println(item + " src < current");
                iw = (int)(sourceRatio * (int)(item.h * h));
                 ih = (int)(item.h * h);
             }
 
             item.setBounds((int) (item.x * getWidth()), (int) (item.y * getHeight()) - ih, iw, ih);
 
         }
     }
 
     public void addItem(Item item) {
         items.add(item);
         this.add(item);
         repaint();
     }
 
     public void removeItem(Item item) {
         items.remove(item);
         this.remove(item);
         repaint();
     }
     
     
 //     private MouseAdapter mouse = new MouseAdapter() {
 //
 //        @Override
 //        public void mouseEntered(MouseEvent e) {
 //            mouseOver = true;
 //            repaint();
 //        }
 //
 //        public void mouseExited(MouseEvent e) {
 //            mouseOver = false;
 //            repaint();
 //        }
 //
 //        public void mouseReleased(MouseEvent e) {
 //            if (Door.this.contains(e.getPoint())) onMouseClick(e);
 //        }
 //    };
 //
 //    public void onMouseClick(MouseEvent e) {
 //        System.err.println("Door clicked");
 //        System.err.println("Switching to Room: " + destination);
 //        RoomTemplate.ROOM_SWITCHER.switchTo(destination);
 //    }
 
     public String getLocation_name() {
         return roomName;
     }
 
     public ArrayList<Player> getPlayers_in_location_ArrayList() {
         return playersInTheRoom;
     }
 
     public Zombie getZombie() {
         return zombieOfTheRoom;
     }
 
     public boolean isHasZombie() {
         return hasZombie;
     }
 
     public void setHasZombie(boolean hasZombie) {
         this.hasZombie = hasZombie;
     }
 
     public ArrayList<Door> getDoorsOfTheRoom() {
         return doorsOfTheRoom;
     }
 
     public void setDoorsOfTheRoom(ArrayList<Door> doorsOfTheRoom) {
         this.doorsOfTheRoom = doorsOfTheRoom;
     }
 
     public ArrayList<Item> getItemsOfTheRoom() {
         return items;
     }
 
     public void setItemsOfTheRoom(ArrayList<Item> itemsOfTheRoom) {
         this.items = itemsOfTheRoom;
     }
 }
