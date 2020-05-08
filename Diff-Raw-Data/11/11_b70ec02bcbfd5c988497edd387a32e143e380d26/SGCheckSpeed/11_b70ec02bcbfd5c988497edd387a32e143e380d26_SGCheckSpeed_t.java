 package com.xdrapor.safeguard.checks.movement;
 
 import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import com.xdrapor.safeguard.checks.SGCheck;
 import com.xdrapor.safeguard.core.permissions.SGPermissibleNodes;
 import com.xdrapor.safeguard.player.SGPlayer;
 import com.xdrapor.safeguard.utilities.SGBlockUtil;
 import com.xdrapor.safeguard.utilities.SGCheckTag;
 import com.xdrapor.safeguard.utilities.SGMovementUtil;
 
 public class SGCheckSpeed extends SGCheck {
 
 	@Override
 	public String getDescription() {
 		return "Checks to see if a player is moving too fast.";
 	}
 
 	@Override
 	public void runCheck(Event event, SGPlayer player) {
 
 		if(player == null || event == null)return;
 		if(sgPermissions.hasPermission(player, SGPermissibleNodes.MOVEMENT_SPEED) || !sgConfig.isCheckEnabled(this))return;
 		
 		PlayerMoveEvent playerMoveEvent = (PlayerMoveEvent)event;
 		Player sgPlayer = player.getPlayer();
 		
 		this.to = playerMoveEvent.getTo();
 		this.from = playerMoveEvent.getFrom();
 		
 		if(SGMovementUtil.getDistanceY(to, from, false) >= 1.0) {
			
 			playerMoveEvent.setTo(SGBlockUtil.findClosestGroundToLocation(sgPlayer));
 
 			safeGuard.sgPlayerManager.getPlayer(sgPlayer.getName()).addVL(SGCheckTag.MOVEMENT_SPEED, (SGMovementUtil.getDistanceVertical(this.to, this.from) * 10));
 			
 			publishCheck(getClass(), sgPlayer, SGCheckTag.MOVEMENT_SPEED);
		
 		}
 		
 		if(((getSpeedAmplifier(((CraftPlayer)sgPlayer).getHandle()) * getPlayerSpeed(sgPlayer)) * 1.3) < SGMovementUtil.getDistanceHorizontal(this.to, this.from) && !player.isOnLily()) {
 			safeGuard.sgPlayerManager.getPlayer(sgPlayer.getName()).addVL(SGCheckTag.MOVEMENT_SPEED, (SGMovementUtil.getDistanceHorizontal(this.to, this.from) * 10) - (getSpeedAmplifier(((CraftPlayer)sgPlayer).getHandle()) * getPlayerSpeed(sgPlayer)));
 			
 			if (safeGuard.sgPlayerManager.getPlayer(sgPlayer.getName()).getVL(SGCheckTag.MOVEMENT_SPEED) > 6) {
 				
 				publishCheck(getClass(), sgPlayer, SGCheckTag.MOVEMENT_SPEED);
 				
 				playerMoveEvent.setTo(this.from);
 			}
 
 			return;
 		}
 		
 		safeGuard.sgPlayerManager.getPlayer(sgPlayer.getName()).reduceVL(SGCheckTag.MOVEMENT_SPEED);
 	}
 }
