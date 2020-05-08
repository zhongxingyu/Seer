 package ch.minepvp.spout.kingdoms.component;
 
 import ch.minepvp.spout.kingdoms.Kingdoms;
 import ch.minepvp.spout.kingdoms.database.table.Kingdom;
 import ch.minepvp.spout.kingdoms.database.table.Member;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.component.impl.SceneComponent;
 import org.spout.api.component.type.EntityComponent;
 import org.spout.api.entity.Player;
 import org.spout.api.lang.Translation;
 import org.spout.vanilla.component.living.neutral.Human;
 
 public class KingdomsComponent extends EntityComponent {
 
     public Kingdom getKingdom() {
         return Kingdoms.getInstance().getKingdomManager().getKingdomByPlayer( (Player)getOwner() );
     }
 
     public Member getMember() {
         return Kingdoms.getInstance().getMemberManager().getMemberByPlayer( (Player)getOwner() );
     }
 
     @Override
     public void onTick( float dt ) {
 
         Player player = (Player)getOwner();
         SceneComponent scene = getOwner().getScene();
 
         if ( scene.getPosition().getBlockX() == player.add(Human.class).getLivePosition().getBlockX() &&
              scene.getPosition().getBlockY() == player.add(Human.class).getLivePosition().getBlockY() &&
              scene.getPosition().getBlockZ() == player.add(Human.class).getLivePosition().getBlockZ() ) {
 
 
             //player.sendMessage("not move");
 
 
             return;
         }
 
 
         //player.sendMessage("move");
 
 
         Kingdom lastKingdom = Kingdoms.getInstance().getKingdomManager().getKingdomByPoint( scene.getPosition() );
         Kingdom nowKingdom = Kingdoms.getInstance().getKingdomManager().getKingdomByPoint( player.add(Human.class).getLivePosition() );
 
         if ( lastKingdom == null ) {
 
             if ( nowKingdom != null ) {
 
                 player.sendMessage(ChatArguments.fromFormatString( Translation.tr("{{GOLD}}You are now in Kingdom %0", player, nowKingdom.getName()) ));
 
             }
 
         } else {
 
             if ( nowKingdom == null ) {
 
                player.sendMessage(ChatArguments.fromFormatString( Translation.tr("{{RED}}You leave the Kingdom %0", player, lastKingdom.getName()) ));
 
             }
 
         }
 
     }
 
 }
