 package platformer01;
 
 public class CollisionCheck {
 
     public static void playerCheck(Player player, GameObject gameObject) {
         
         /* TODO 
          * 
          * - fix the combined overhead and sideways collision 
          * - set collision detection to check for player location + player speed
          * - simplify
          */
         
         if (player.isLeftBlocked == true || player.isRightBlocked == true){
             player.isLeftBlocked = false;
             player.isRightBlocked = false;
         }
         
         /* Collision to the left */
         if ((gameObject.contains(player.getLowerLeftCorner().addToX(-1.0f))
                 && gameObject.contains(player.getLowerLeftCorner().addToX(-1.0f).addToY(player.blockHeight)))
                || gameObject.contains(player.getUpperLeftCorner().addToX(-1.0f))) {
             player.isLeftBlocked = true;
             player.targetLocation.x = gameObject.getLowerRightCorner().x + 1.0f;
         }
         
         /* Collision to the right */
         if ((gameObject.contains(player.getLowerRightCorner().addToX(1.0f))
                 && gameObject.contains(player.getLowerRightCorner().addToX(1.0f).addToY(player.blockHeight)))
                || gameObject.contains(player.getUpperRightCorner().addToX(1.0f))) {
             player.isRightBlocked = true;
             player.targetLocation.x = gameObject.getLowerLeftCorner().x - 1.0f - player.size.x;
         }
         
         /* Collision to below */
         if (gameObject.contains(player.getLowerLeftCorner().addToY(player.gravity.y))
                 || gameObject.contains(player.getLowerRightCorner().addToY(player.gravity.y))) {
             player.isSupported = true;
             player.targetLocation.y = gameObject.location.y - player.size.y - 1.0f;
             if (gameObject.contains(player.getLowerLeftCorner())
                     || gameObject.contains(player.getLowerRightCorner())) {
                 player.isSupported = true;
                 player.targetLocation.y = gameObject.location.y - player.size.y - 1.0f;
             }
         }
         /* Collision to above */
         if (gameObject.contains(player.getUpperLeftCorner().addToY(-1.0f))
                 || gameObject.contains(player.getUpperRightCorner().addToY(-1.0f))) {
             player.targetLocation.y = gameObject.getLowerLeftCorner().y;
             player.isUpBlocked = true;
             player.jumpingPeaked = true;
             player.isJumping = false;
         }
        
        /* Adjustments to target location here */
        
     }
 }
