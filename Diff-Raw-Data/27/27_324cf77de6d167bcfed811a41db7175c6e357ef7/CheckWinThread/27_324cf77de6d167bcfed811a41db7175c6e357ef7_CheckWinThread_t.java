 package com.minecarts.sandandgravel.game;
 
 import com.minecarts.sandandgravel.*;
 import com.minecarts.sandandgravel.game.*;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.util.Vector;
 
 public class CheckWinThread implements Runnable {
     private SandAndGravel plugin;
     
     private Game.State nextTurn = Game.State.TURN_GRAVEL;
     private int column; //Column where the last block was placed
     
     public CheckWinThread(SandAndGravel plugin, Game.State nextTurn, int column){
         this.plugin = plugin;
         this.nextTurn = nextTurn;
         this.column = column;
     }
     
     private boolean findMove(Vector position, Vector direction, int moves, Material blockType) throws WinnerException{
         Block b = Game.world.getBlockAt(position.toLocation(Game.world));
         plugin.log.finest(String.format("Looking for [%s]: %s, %s, %s (%s) [Move: %s]",blockType,position.getBlockX(), position.getBlockY(), position.getBlockZ(),b.getType(),moves));
         if(b.getType() == blockType){
             if(moves == 4){
                 plugin.log.finest("WINNING MOVE FOUND! Direction: " + direction);
                 throw new WinnerException(blockType, direction);
             } else {
                 return findMove(position.add(direction),direction,++moves,blockType);
             }
         }
         return false;
     }
     
     class WinnerException extends Exception{
         Material blockType;
         Vector direction;
         public WinnerException(Material blockType, Vector direction){
             super();
             this.blockType = blockType;
             this.direction = direction;
         }
     }
     
     
     public void run(){
         int usedBlocks = 0;
         int z = Locations.gridTopLeft.getBlockZ() + 1;
                 
         //Find the top most block of the column (the one the player just placed)
         for(int y = Locations.gridTopLeft.getBlockY(), yMin = y-6; y>yMin; y--){
             Block b = Game.world.getBlockAt(column,y,z);
             Material blockType = b.getType();
             if(blockType == Material.SAND || blockType == Material.GRAVEL){
                 //We have our top block, the one that was just placed
                 try{
                     findMove(b.getLocation().toVector(),new Vector(1,0,0),1,blockType); // +x
                     findMove(b.getLocation().toVector(),new Vector(-1,0,0),1,blockType); // -x
     
                     findMove(b.getLocation().toVector(),new Vector(0,1,0),1,blockType); // +y
                     findMove(b.getLocation().toVector(),new Vector(0,-1,0),1,blockType); // -y
                     
                     findMove(b.getLocation().toVector(),new Vector(-1,1,0),1,blockType);
                     findMove(b.getLocation().toVector(),new Vector(1,1,0),1,blockType); 
                     findMove(b.getLocation().toVector(),new Vector(1,-1,0),1,blockType); 
                     findMove(b.getLocation().toVector(),new Vector(-1,-1,0),1,blockType); 
                 } catch(WinnerException e){ //We have a wining move!
                     //Change the blocks to gold blocks
                     for(int i = 0; i<4;i++){
                         Vector temp = e.direction.clone();
                         Block bl = Game.world.getBlockAt(b.getLocation().toVector().add(temp.multiply(i)).toLocation(Game.world));
                         plugin.log.finest(String.format("[%s] Setting block %s, %s, %s [%s]", i, bl.getLocation().getBlockX(), bl.getLocation().getBlockY(), bl.getLocation().getBlockZ(), e.direction));
                         bl.setType(Material.DIAMOND_BLOCK);
                     }
                     
                     if(e.blockType == Material.GRAVEL){
                         Game.changeState(Game.State.WINNER_GRAVEL);
                         return;
                     } else if (e.blockType == Material.SAND){
                         Game.changeState(Game.State.WINNER_SAND);
                         return;
                     } else {
                         plugin.log.warning("Unknown SandAndGravel block for winner: " + e.blockType);
                     }
                 }
 
                 //Check all the directionals
                     //
             }
         }
          
            
         //Check to see if the board is full
         //  we can do this by seeing if there are any spaces free at the top most layer
         for(int x = Locations.gridTopLeft.getBlockX(), xMax = x+7; x<xMax; x++){
             Block b = Game.world.getBlockAt(x,Locations.gridTopLeft.getBlockY(),z);
             plugin.log.finer("Checking block for full: " + b.getType());
             if(b.getType() == Material.SAND || b.getType() == Material.GRAVEL){
                if(++usedBlocks == 7){
                    Game.changeState(Game.State.GAME_TIE);
                    return;
                }
             }
         }
         
         
         
         //Else, no one won, so next turn it up
         Game.changeState(nextTurn);
     }
 }
