 package chessMod.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.monster.EntityZombie;
 import net.minecraft.world.World;
 
 /**
  * MineChess
  * @author MineMaarten
  * www.minemaarten.com
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 
 public class EntityPawn extends EntityBaseChessPiece{
 
     public EntityPawn(World par1World){
         super(par1World);
         setSize(0.4F, 0.93F);
     }
 
     @Override
     public Entity getMob(){
         return new EntityZombie(worldObj);
     }
 
     @Override
     public List<int[]> getPossibleMoves(){
         List<int[]> moves = new ArrayList<int[]>();
         int[] move1 = new int[2];
         move1[0] = targetX;
         move1[1] = targetZ + (isBlack() ? -1 : 1); // we don't have to check boundaries, as pawns are immeadiatly transformed into queens on the max Z.
         if(firstMove) {
             int[] move2 = new int[2];
             move2[0] = targetX;
             move2[1] = targetZ + (isBlack() ? -2 : 2);
             if(!isEnemyAt(move2[0], move2[1])) moves.add(move2);
         }
         if(!isEnemyAt(move1[0], move1[1])) moves.add(move1);
 
         // capturing rules
         if(isEnemyAt(targetX + 1, targetZ + (isBlack() ? -1 : 1))) {
             int[] move3 = new int[2];
             move3[0] = targetX + 1;
             move3[1] = targetZ + (isBlack() ? -1 : 1);
             moves.add(move3);
         }
         if(isEnemyAt(targetX - 1, targetZ + (isBlack() ? -1 : 1))) {
             int[] move4 = new int[2];
             move4[0] = targetX - 1;
             move4[1] = targetZ + (isBlack() ? -1 : 1);
             moves.add(move4);
         }
         return moves;
     }
 
     // returns true if there is an enemy at the given x,z. It also returns true
     // if there can be enPassanted.
     private boolean isEnemyAt(int x, int z){
         List<EntityBaseChessPiece> pieces = getChessPieces(true);
         for(int i = 0; i < pieces.size(); i++) {
            if(pieces.get(i).getTargetPosition()[0] == x && (pieces.get(i).getTargetPosition()[1] == z || pieces.get(i).enPassePossibility && pieces.get(i).targetZ == targetZ) && pieces.get(i).isBlack() ^ isBlack()) {
                 return true;
             }
         }
         return false;
     }
 
 }
