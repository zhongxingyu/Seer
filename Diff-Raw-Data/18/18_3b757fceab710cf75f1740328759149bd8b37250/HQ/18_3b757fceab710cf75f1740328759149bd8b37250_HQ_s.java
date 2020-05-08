 package sts.game;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import sts.Local;
 import sts.gui.ImageHandler;
 
 /**
  * The player's chief building. Creates villagers.
  * @author Phillip Cohen
  */
 public class HQ extends ProductionBuilding
 {
     public HQ( int x, int y, Player player, boolean preBuilt )
     {
         super( x, y, 1200, 750, player );
         giveableCommands = new Command[2];
         giveableCommands[0] = new Command( "Create villager", 20, 100, ImageHandler.getVillager() );
         giveableCommands[1] = new Command( "Give gold (devkey)", 0, -10000, ImageHandler.getGold() );
        
         if ( preBuilt )
             setHealth( 700 );
     }
 
     @Override
     public void draw( Graphics2D g )
     {
         Color c = getOwningPlayer().getColor();
 
         if ( Local.getSelectedObjects().contains( this ) )
             c = ImageHandler.getOppositeColor( c );
 
         ImageHandler.drawHQ( g, getX(), getY(), c );
     }
 
     @Override
     protected void doCreation( Command type )
     {
         if ( type == giveableCommands[0] )
             getOwningPlayer().giveObject( new Villager( getX() + 30, getY() + 54, getOwningPlayer() ) );
     }
 
     @Override
     public boolean isClickContained( int x, int y, int width, int height )
     {
         return isClickContainedInRectangle( this, x, y, 64 + width, 64 + height );
     }
 
     @Override
     public String getName()
     {
         return "HQ";
     }
 
     @Override
     public Location getLoc()
     {
         return new Location( getX() + 32, getY() + 44 );
     }
 }
