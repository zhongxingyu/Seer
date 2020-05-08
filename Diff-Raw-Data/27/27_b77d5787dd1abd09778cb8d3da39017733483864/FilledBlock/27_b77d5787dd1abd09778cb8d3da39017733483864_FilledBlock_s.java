 package fearlesscode;
 
 import fearlesscode.util.*;
 
 public class FilledBlock extends Block
 {
 
 	public FilledBlock()
     {
         super();
         //Logger.call(this,"FilledBlock.init()");
         //Logger.call(this,"FilledBlock.init()");
 	}
 
 
 	public void checkBorders()
     {
         Logger.call(this,"checkBorders()");
        boolean isAtBlockSide=false;
        boolean isPossibleToGetTrough=false;
        boolean doesFallOut=false;
        boolean isOutOfTheBlock=false;
        Block neighbour;
        EntityPosition pos;

        if(isAtBlockSide){
           if(isPossibleToGetTrough){
                 player.player.enterBlock(neighbour);
                 neighbour.setPlayer(player.player,pos);
            }
           else if(doesFallOut){
                //????????vissza kéne hogy hivjon a PlayField-re de nincs rá ref??????
 
            }
         }
        else if(isOutOfTheBlock){
            player.player.leaveBlock(this);
         }
         Logger.call(this,"checkBorders()");
 
 	}
 
 	public void processCollisions(){
         Logger.call(this,"processCollisions()");
 
         for(EntityContainer e : entities){
            boolean collision=false;
            if(collision)
             {
                 e.entity.meetPlayer(player.player);
             }
         }
         Logger.ret(this,"processCollisions()");
 	}
 
 }
