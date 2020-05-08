 import buttons.Button;
 
 public class ShipEditor extends Player {
     public ShipEditor(){
         setXY(Window.getWidth()/2, Window.getHeight()/2);
         setPixelSize(20);
         setGrid(true);
     }
     public void down(int x, int y){
         if (setShipWall(x, y)){ //if the grid is selected
             Game.player.setMoney(getMoney());
            Window.panel.particles.addClusterAt(250,215,10);
         }
     }
 }
