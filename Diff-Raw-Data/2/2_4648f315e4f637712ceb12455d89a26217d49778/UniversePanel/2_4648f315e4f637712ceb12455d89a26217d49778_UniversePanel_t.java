 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.awt.*;
 
 public class UniversePanel extends JPanel {
     private final GameController gc;
     private final GamePanel gamePanel;
     private final PlanetButton[][] planetButtons;
     private BufferedImage universeBackground;
     private Player player;
 
     public UniversePanel(SolarSystem[][] universe, GameController gc, GamePanel gamePanel) {
         try{
             universeBackground = ImageIO.read(new File("images/star_background.jpg"));
         } catch(IOException ioe) {
             System.err.println("Error getting images/UniverseBackground.png");
             universeBackground = null;
         }
         this.gc = gc;
         this.gamePanel = gamePanel;
         int TILE_SIZE = 60;
         setPreferredSize(new Dimension(5* TILE_SIZE, 5* TILE_SIZE));
         setLayout(new GridLayout(GameController.UNIVERSE_SIZE, GameController.UNIVERSE_SIZE));
         planetButtons = new PlanetButton[GameController.UNIVERSE_SIZE][GameController.UNIVERSE_SIZE];
         for (int i = 0; i < GameController.UNIVERSE_SIZE; i++) {
             for (int j = 0; j < GameController.UNIVERSE_SIZE; j++) {
                 planetButtons[i][j] = new PlanetButton(universe[i][j].getPlanet(),
                     new Dimension(40, 40), this);
                 this.add(planetButtons[i][j]);
             }
         }
     }
 
 
     public void setPlayer(Player p) {
         this.player = p;
         planetButtons[(int) player.getLocation().getX()][(int) player.getLocation().getY()].setHere(true);
         planetButtons[(int) player.getLocation().getX()][(int) player.getLocation().getY()].repaint();
         repaint();
     }
 
     public void goToPlanet(Planet p) {
         if (!playerNearPlanet(p)) {
             JOptionPane.showMessageDialog(this, "That planet is too far away.");
             return;
     	}
 
         if (!sufficientFuel(p)) {
             JOptionPane.showMessageDialog(this, "Not enough fuel.");
             return;
         }
 
         //fuel cost
         SpaceShip ship = player.getShip();
         double newFuel = ship.getFuel()-(Math.abs(player.getLocation().getX() - p.getLocation().getX()))-(Math.abs(player.getLocation().getY() - p.getLocation().getY()));
         ship.setFuel((int)newFuel);
         planetButtons[(int) player.getLocation().getX()][(int) player.getLocation().getY()].setHere(false);
         planetButtons[(int) player.getLocation().getX()][(int) player.getLocation().getY()].repaint();
     	player.setLocation(p.getLocation());
     	planetButtons[(int) p.getLocation().getX()][(int) p.getLocation().getY()].setHere(true);
     	planetButtons[(int) p.getLocation().getX()][(int) p.getLocation().getY()].repaint();
         gc.updateMarketPanel(p);
         player.setPlanet(p);
         gamePanel.updatePlayerPanel();
 
         // so, when you need to change the current location
         // all you need to do is setHere on the button that corresponds to the location they are going to 
         // then call the repaint on the button 
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         g.drawImage(universeBackground, 0, 0, this.getWidth(), this.getHeight(), null);
     }
 
   private boolean playerNearPlanet(Planet p) {
       return Math.abs(player.getLocation().getX() - p.getLocation().getX()) < 2 &&
               Math.abs(player.getLocation().getY() - p.getLocation().getY()) < 2;
 
   }
 
     private boolean sufficientFuel(Planet p) {
         SpaceShip ship = player.getShip();
         return ship.getFuel() >=
                Math.abs(Math.abs(player.getLocation().getX() - p.getLocation().getX()) -
                 (player.getLocation().getY() - p.getLocation().getY()));
 
     }
 
 }
