 package com.xdrapor.safeguard.checks.movement;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import com.xdrapor.safeguard.checks.SGCheck;
 import com.xdrapor.safeguard.core.permissions.SGPermissibleNodes;
 import com.xdrapor.safeguard.player.SGPlayer;
 import com.xdrapor.safeguard.utilities.SGCheckTag;
 import com.xdrapor.safeguard.utilities.SGMovementUtil;
 
 public class SGCheckFall extends SGCheck {
 
 	@Override
 	public String getDescription() {
 		return "Prevents usage of hacks such as 'NoFall' that prevents fall-damage from being applied on the entity.";
 	}
 
 	@Override
 	public void runCheck(Event event, SGPlayer player) {
 
 		if(player == null || event == null)return;
 		if(sgPermissions.hasPermission(player, SGPermissibleNodes.MOVEMENT_FALL) || !sgConfig.isCheckEnabled(this))return;
 
 		PlayerMoveEvent playerMoveEvent = (PlayerMoveEvent)event;
 		Player sgPlayer = player.getPlayer();
 
 		this.to = playerMoveEvent.getTo();
 		this.from = playerMoveEvent.getFrom();
 
 
 		if((isCreativeFlight(sgPlayer) && sgPlayer.isFlying()) || isCreative(sgPlayer))return;
 
 		if(SGMovementUtil.getFalling(to, from)) {
 
 			if(!player.isFalling()) {
 
 				player.setFalling(true);
 				player.setFellFrom(from);
 				player.setFallInitialHealth(sgPlayer.getHealth());
 			}
 
 		} else {
 
 			if(player.isFalling()) {
 
 				player.setFellTo(sgPlayer.getLocation());
 				player.setFallFinalHealth(sgPlayer.getHealth());
 				int blocksFallen  = (int) SGMovementUtil.getDistanceVertical(player.getFellTo(), player.getFellFrom());
 
 				if((player.getFallInitialHealth() - player.getFallFinalHealth() < (blocksFallen - 3)) && blocksFallen > 3) {
 
 					int avoidedDiff = ((blocksFallen - 3) - (player.getFallInitialHealth() - player.getFallFinalHealth()));
 					sgPlayer.damage(avoidedDiff);
 
 					safeGuard.sgPlayerManager.getPlayer(sgPlayer.getName()).addVL(SGCheckTag.MOVEMENT_FALL, avoidedDiff * 10);
 
 					publishCheck(getClass(), sgPlayer, SGCheckTag.MOVEMENT_FALL);
 				}
 
 				player.resetFallingValues();
 			}
 		}
 	}
 }
}
