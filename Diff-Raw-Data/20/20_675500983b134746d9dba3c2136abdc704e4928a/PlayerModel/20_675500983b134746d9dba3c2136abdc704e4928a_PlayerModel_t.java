 package Model;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Random;
 
 import Control.GlobalClassSelector;
 import Model.*;
 import Model.Classes.*;
 import Model.Obstacles.*;
 import Model.Skills.*;
 
 public class PlayerModel implements ActionListener {
 	
 	Player player;
 	//Player enemy;
 	Skill[] playerSkills;
 	Skill currentActiveSkill;
 	
 	Obstacle[] obstacles;
 
 	public PlayerModel(Player player, Obstacle[] obstacles){
 		
 		this.player = player;
 		
 		playerSkills = player.getSkillList();
 		currentActiveSkill = playerSkills[0];
 		
 		this.obstacles = obstacles;
 	}
 	
 	public Player getPlayer(){
 		return player;
 	}
 	
 	public void killPlayer(){
 		player.setAliveState(false);
 		player.setX(1000);
 		player.setY(-1000);
 		player.setRunningState(false);
 		player.resetStatusEffects();
 	}
 	
 	public void ressurectPlayer() throws SlickException{
 		player.setAliveState(true);
 		player.setStunState(false);
 		player.setPushState(false);
 		player.resetStatusEffects();
 		checkSpawnCollision();
 		player.resetHP();
 		player.setX(player.getStartX());
 		player.setY(player.getStartY());
 		for(int i=0; i<playerSkills.length; i++){
 			playerSkills[i].resetCooldown();
 			playerSkills[i].collidedShot();
 		}
 	}
 	
 	public Skill getCurrentActiveSkill(){
 		return currentActiveSkill;
 	}
 	public void setCurrentActiveSkill(int index){
 		
 		for(int i=0; i<playerSkills.length; i++){
 			if(i == index){
 				currentActiveSkill = playerSkills[i];
 				playerSkills[i].setChosenState(true);
 			}else{
 				playerSkills[i].setChosenState(false);
 			}
 		}
 	}
 	
 	public void isRunning() throws SlickException{
 		if(player.isAlive() && !player.isPushed() && !player.isStunned() && !checkObstacleCollision((float)(player.getXDirMove()*player.getMoveSpeed()), (float)(player.getYDirMove()*player.getMoveSpeed())) && player.getMoveSpeed() > 0){
 			player.addX((float)(player.getXDirMove()*player.getMoveSpeed()));
 			player.addY((float)(player.getYDirMove()*player.getMoveSpeed()));
 			player.incMoveCounter();
 			if(player.getMoveCounter()*player.getMoveSpeed() >= player.getGenDirMove())
 				player.setRunningState(false);
 			
 		}else if(player.isAlive() && player.isPushed() && !checkObstacleCollision((float)(player.getXDirMove()*player.getMoveSpeed()), (float)(player.getYDirMove()*player.getMoveSpeed())) && player.getMoveSpeed() > 0){
 			
 			double tempSpeed = player.getPushSpeed();
 			double calculateDecision = tempSpeed*player.getMoveCounter();
 			if(calculateDecision>player.getGenDirMove()*0.5){
 				tempSpeed *= 0.7;
 				if(calculateDecision>player.getGenDirMove()*0.7){
 					tempSpeed *= 0.7;
 					if(calculateDecision>player.getGenDirMove()*0.9){
 						tempSpeed *= 0.7;
 					}
 				}
 				
 			}else if(calculateDecision<player.getGenDirMove()*0.3){
 				tempSpeed *= 1.5;
 			}
 			
 			
 			player.addX((float)(player.getXDirMove()*tempSpeed));
 			player.addY((float)(player.getYDirMove()*tempSpeed));
 			
 			player.incMoveCounter();
 			if(player.getMoveCounter()*player.getPushSpeed() >= player.getGenDirMove()){
 				player.setRunningState(false);
 				player.setPushState(false);
 			}
 		}else{
 			player.setRunningState(false);
 		}
 	}
 	
 	//Determines action depending on what state the skill is in
 	public void isAttacking(Skill attackingSkill){
 		if(attackingSkill != null){
 			if(!attackingSkill.isEndState() && attackingSkill.isProjectile()){
 				
 				//Calculates the new direction if the skill is guided
 				if(attackingSkill.isGuided()){
 					//TODO make guided just slightly influenced by target and not completely
 				/*	float xDiff = attackingSkill.getGuidedTarget().getX()-attackingSkill.getMouseXPosAtt();
 					float yDiff = attackingSkill.getGuidedTarget().getY()-attackingSkill.getMouseYPosAtt();
 					float genDiff = (float)Math.sqrt(xDiff*xDiff+yDiff*yDiff);
 					xDiff /= genDiff;
 					yDiff /= genDiff;*/
 					
 					float xDir = attackingSkill.getGuidedTarget().getX()-attackingSkill.getAttX();
 					float yDir = attackingSkill.getGuidedTarget().getY()-attackingSkill.getAttY();
 					
 			//		float xDir = attackingSkill.getXDirAtt() - xDiff;
 			//		float yDir = attackingSkill.getYDirAtt() - yDiff;
 				
 					float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 					
 					attackingSkill.setXDirAtt(xDir/genDir);
 					attackingSkill.setYDirAtt(yDir/genDir);
 					double rotation = Math.toDegrees(Math.atan2((yDir),(xDir)));
 					attackingSkill.setRotation(90 + (float)rotation);
 					
 				//	attackingSkill.addAttX(xDir/genDir);
 				//	attackingSkill.addAttY(yDir/genDir);
 					
 				}
 				//determines which x and y the skill will have in the next render session
 				attackingSkill.addAttX((float)(attackingSkill.getXDirAtt()*attackingSkill.getAttSpeed()));
 				attackingSkill.addAttY((float)(attackingSkill.getYDirAtt()*attackingSkill.getAttSpeed()));
 				
 				attackingSkill.incAttCounter();
 				
 				if(attackingSkill.getProjectileAnimationTimer() != null){
 					Image projectileImage = attackingSkill.getProjectileAnimationTimer().getCurrentAnimationImage();
 				
 					if(projectileImage != null)
 						attackingSkill.setImage(projectileImage);
 					
 					attackingSkill.setRotation(attackingSkill.getRotation());
 				}
 				
 				if(attackingSkill.getAttCounter()*attackingSkill.getAttSpeed() >= attackingSkill.getGenDirAtt()){
 					if(!attackingSkill.hasEndState()){
 						attackingSkill.setAttackingState(false);
 					}else{
 						attackingSkill.activateEndState();
 						System.out.println("Commencing end state with " + attackingSkill.getName());
 					}
 				}
 				
 			}else if(!attackingSkill.isEndState() && !attackingSkill.isProjectile()){
 				attackingSkill.activateEndState();					
 				System.out.println("Commencing end state with " + attackingSkill.getName());
 			
 			}else if(attackingSkill.isEndState()){
 				if(attackingSkill.getAnimationTimer() != null){
 					Image animationImage = attackingSkill.getAnimationTimer().getCurrentAnimationImage();
 				
 					if(animationImage != null)
 						attackingSkill.setEndStateImage(animationImage);
 				}
 			}
 		}
 	}
 	
 	public boolean isColliding(Skill skill) throws SlickException{
 
 		if(skill.getAttX() <= player.getX() && skill.getAttX()+skill.getCurrentWidth() >= player.getX()){
 			if(skill.getAttY() >= player.getY() && skill.getAttY() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY()+skill.getCurrentHeight() >= player.getY() && skill.getAttY()+skill.getCurrentHeight() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY() <= player.getY() && skill.getAttY()+skill.getCurrentHeight() >= player.getY()+player.getImage().getHeight() ){
 				return true;
 			}
 		}else if(skill.getAttY() <= player.getY() && skill.getAttY()+skill.getCurrentHeight() >= player.getY()){
 			if(skill.getAttX() >= player.getX() && skill.getAttX() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX()+skill.getCurrentWidth() >= player.getX() && skill.getAttX()+skill.getCurrentWidth() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX() <= player.getX() && skill.getAttX()+skill.getCurrentWidth() >= player.getX()+player.getImage().getWidth() ){
 				return true;
 			}
 		}else if(skill.getAttX() <= player.getX()+player.getImage().getWidth()  && skill.getAttX()+skill.getCurrentWidth() >= player.getX()+player.getImage().getWidth() ){
 			if(skill.getAttY() >= player.getY() && skill.getAttY() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY()+skill.getCurrentHeight() >= player.getY() && skill.getAttY()+skill.getCurrentHeight() <= player.getY()+player.getImage().getHeight() 
 					|| skill.getAttY() <= player.getY() && skill.getAttY()+skill.getCurrentHeight() >= player.getY()+player.getImage().getHeight() ){
 				return true;
 			}
 		}else if(skill.getAttY() <= player.getY()+player.getImage().getHeight()  && skill.getAttY()+skill.getCurrentHeight() >= player.getY()+player.getImage().getHeight() ){
 			if(skill.getAttX() >= player.getX() && skill.getAttX() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX()+skill.getCurrentWidth() >= player.getX() && skill.getAttX()+skill.getCurrentWidth() <= player.getX()+player.getImage().getWidth() 
 					|| skill.getAttX() <= player.getX() && skill.getAttX()+skill.getCurrentWidth() >= player.getX()+player.getImage().getWidth() ){
 				return true;
 			}
 		}
 		
 	//	System.out.println("Skill: " + skill.getName() + " X: " + skill.getAttX() + " Y: " + skill.getAttY() + " W: " + skill.getCurrentWidth() + " H: " + skill.getCurrentHeight());
 		
 		return false;
 	}
 
 	
 	
 	public void move(int x, int y){
 	//	mouseXPosMove = Mouse.getX();
 	//	mouseYPosMove = 720 - Mouse.getY();
 		if(!player.isPushed()){
 			rotate(x, y);
 			player.setMouseXPosMove(x);
 			player.setMouseYPosMove(y);
 			
 			float xDir = x - player.getX();
 			float yDir = y - player.getY();
 			float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 			
 			
 			Double findNaN = (double)genDir;
 			if(!findNaN.isNaN() && !findNaN.isInfinite()){
 				player.setXDirMove(xDir);
 				player.setYDirMove(yDir);
 				player.setGenDirMove(genDir);
 				player.setXDirMove(player.getXDirMove()/player.getGenDirMove());
 				player.setYDirMove(player.getYDirMove()/player.getGenDirMove());
 				
 				player.resetMoveCounter();
 				
 			//	System.out.println("Running " + player.getGenDirMove() + " pixels");
 				player.setRunningState(true);
 			}
 		}
 	}
 	
 	public void attack(int x, int y){
 		
 		if(currentActiveSkill.isGuided()){
 			findAndSetGuidedTarget(currentActiveSkill);
 		}
 		//Setting x and y to be in middle of mouse click
 		x -= currentActiveSkill.getCurrentWidth()/2;
 		y -= currentActiveSkill.getCurrentHeight()/2;
 		rotate(x, y);
 		if(currentActiveSkill != null && player.isAlive() && !player.isStunned() && currentActiveSkill.checkCooldown() == currentActiveSkill.getCoolDown()){
 			
 				currentActiveSkill.activateSkill();
 				
 				currentActiveSkill.setMouseXPos(x);
 				currentActiveSkill.setMouseYPos(y);
 				
 				
 				currentActiveSkill.resetShot(player);
 				
 				float xDir = x - currentActiveSkill.getAttX();
 				float yDir = y - currentActiveSkill.getAttY();
 				float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 				
 				
 				Double findNaN = (double)genDir;
 				if(!findNaN.isNaN() && !findNaN.isInfinite()){
 				
 					currentActiveSkill.setRotation(player.getRotation());
 					
 					currentActiveSkill.setGenDirAtt(genDir);
 					currentActiveSkill.setXDirAtt(xDir/currentActiveSkill.getGenDirAtt());
 					currentActiveSkill.setYDirAtt(yDir/currentActiveSkill.getGenDirAtt());
 					
 					if(currentActiveSkill.getGenDirAtt() > currentActiveSkill.getAttackRange()){
 						currentActiveSkill.setGenDirAtt(currentActiveSkill.getAttackRange());
 					}
 					currentActiveSkill.resetAttCounter();
 					if(!currentActiveSkill.isProjectile()){
 						currentActiveSkill.setNonProjectileShot();
 					}
 					System.out.println("Attacking with " + currentActiveSkill.getName() + " at the range of " + currentActiveSkill.getGenDirAtt() + " pixels");
 					currentActiveSkill.setAttackingState(true);
 					
 					currentActiveSkill.setMouseXPos(player.getX()+currentActiveSkill.getXDirAtt()*currentActiveSkill.getGenDirAtt());
 					currentActiveSkill.setMouseYPos(player.getY()+currentActiveSkill.getYDirAtt()*currentActiveSkill.getGenDirAtt());
 					
 				}
 				
 			if(currentActiveSkill.getAffectSelf()){
 				if(currentActiveSkill.getStatusEffect() != null && !currentActiveSkill.getStatusEffect().hasBeenGivenTo(player.getName())){
 					player.addStatusEffect(currentActiveSkill.getStatusEffect().cloneTo(player));
 				}
 			}
 		}
 	}
 
 	public void rotate(int x, int y){
 		if(!player.isStunned()){
 			player.setMouseXPosMove(x);
 			player.setMouseYPosMove(y);
 			double rotation = Math.toDegrees(Math.atan2((player.getMouseYPosMove()-player.getY()),(player.getMouseXPosMove()-player.getX())));
 			player.setRotation(90 + (float)rotation);
 		}
 	}
 
 	public void checkCollision(Skill[] playerSkills) throws SlickException{
 		if(player.isAlive()){
 			for(int i=0; i<playerSkills.length; i++){
 				if(playerSkills[i] != null && isColliding(playerSkills[i])){
 					int evasion = player.getEvasion();
					System.out.println(evasion);
 					//Calculates new evasion to check if player will evade the attack in this state
					if(evasion>=0){
 						Random generator = new Random();
 						evasion = generator.nextInt(100) - evasion;
 					}
					System.out.println(evasion);
 					//Checks if collided skill has a statusEffect and adds it to the player it hit
 					//And if it can affect others
 					if(playerSkills[i].getStatusEffect() != null && !playerSkills[i].getStatusEffect().hasBeenGivenTo(player.getName()) 
 							&& !playerSkills[i].getAffectSelf() && evasion > 0){
 						player.addStatusEffect(playerSkills[i].getStatusEffect().cloneTo(player));
 					}
 					
 					if(!playerSkills[i].isEndState()){
						if(evasion>=0){
 							pushPlayer(playerSkills[i].getXDirAtt(), playerSkills[i].getYDirAtt());
 						}
 						if(!playerSkills[i].hasEndState()){
 							playerSkills[i].setAttackingState(false);
 							System.out.println("Target hit with " + playerSkills[i].getName());
 							playerSkills[i].collidedShot();
							if(evasion>=0){
 								player.dealDamage(playerSkills[i].getDamage());
 							}
 						}else{
 							System.out.println("Target hit with " + playerSkills[i].getName());
							if(evasion>=0){
 								player.dealDamage(playerSkills[i].getDamage());
 							}
 							playerSkills[i].activateCollisionEndState();
 						}
 					}else{
 						if(playerSkills[i].getESIT() != null && playerSkills[i].getESIT().checkESColTimer() == playerSkills[i].getESColInterval()){
 							System.out.println("Target hit with " + playerSkills[i].getName());
							if(evasion>=0){
 								player.dealDamage(playerSkills[i].getDamage());
 							}
 							playerSkills[i].getESIT().resetESColTimer();
 						}else if(playerSkills[i].getESIT() == null){
 							playerSkills[i].activateESIT(player);
 						}
 					}
 				}
 			}
 		}
 		if(player.getHP() <= 0){
 			killPlayer();
 		}
 	}
 	
 	public boolean checkObstacleCollision(float x, float y) throws SlickException{
 		for(int i=0; i<obstacles.length; i++){
 			if(obstacles[i] != null && isColliding(obstacles[i], x, y)){
 				
 				if(obstacles[i].isSolid()){
 					player.setPushState(false);
 					
 					
 				}
 
 //				System.out.println("Target ran into " + obstacles[i].getType());
 				player.dealDamage(obstacles[i].getDamage());
 				return true;
 
 			}
 		}
 		return false;
 	}
 	
 	public boolean isColliding(Obstacle obstacle, float x, float y) throws SlickException{
 
 		if(obstacle.getX() <= player.getX()+x && obstacle.getX()+obstacle.getCurrentWidth() >= player.getX()+x){
 			if(obstacle.getY() >= player.getY()+y && obstacle.getY() <= player.getY()+y+player.getImage().getHeight() 
 					|| obstacle.getY()+obstacle.getCurrentHeight() >= player.getY()+y && obstacle.getY()+obstacle.getCurrentHeight() <= player.getY()+y+player.getImage().getHeight() 
 					|| obstacle.getY() <= player.getY()+y && obstacle.getY()+obstacle.getCurrentHeight() >= player.getY()+y+player.getImage().getHeight() ){
 				return true;
 			}
 		}else if(obstacle.getY() <= player.getY()+y && obstacle.getY()+obstacle.getCurrentHeight() >= player.getY()+y){
 			if(obstacle.getX() >= player.getX()+x && obstacle.getX() <= player.getX()+x+player.getImage().getWidth() 
 					|| obstacle.getX()+obstacle.getCurrentWidth() >= player.getX()+x && obstacle.getX()+obstacle.getCurrentWidth() <= player.getX()+x+player.getImage().getWidth() 
 					|| obstacle.getX() <= player.getX()+x && obstacle.getX()+obstacle.getCurrentWidth() >= player.getX()+x+player.getImage().getWidth() ){
 				return true;
 			}
 		}else if(obstacle.getX() <= player.getX()+x+player.getImage().getWidth()  && obstacle.getX()+obstacle.getCurrentWidth() >= player.getX()+x+player.getImage().getWidth() ){
 			if(obstacle.getY() >= player.getY()+y && obstacle.getY() <= player.getY()+y+player.getImage().getHeight() 
 					|| obstacle.getY()+obstacle.getCurrentHeight() >= player.getY()+y && obstacle.getY()+obstacle.getCurrentHeight() <= player.getY()+y+player.getImage().getHeight() 
 					|| obstacle.getY() <= player.getY()+y && obstacle.getY()+obstacle.getCurrentHeight() >= player.getY()+y+player.getImage().getHeight() ){
 				return true;
 			}
 		}else if(obstacle.getY() <= player.getY()+y+player.getImage().getHeight()  && obstacle.getY()+obstacle.getCurrentHeight() >= player.getY()+y+player.getImage().getHeight() ){
 			if(obstacle.getX() >= player.getX()+x && obstacle.getX() <= player.getX()+x+player.getImage().getWidth() 
 					|| obstacle.getX()+obstacle.getCurrentWidth() >= player.getX()+x && obstacle.getX()+obstacle.getCurrentWidth() <= player.getX()+x+player.getImage().getWidth() 
 					|| obstacle.getX() <= player.getX()+x && obstacle.getX()+obstacle.getCurrentWidth() >= player.getX()+x+player.getImage().getWidth() ){
 				return true;
 			}
 		}
 		
 	//	System.out.println("Skill: " + skill.getName() + " X: " + skill.getAttX() + " Y: " + skill.getAttY() + " W: " + skill.getCurrentWidth() + " H: " + skill.getCurrentHeight());
 		
 		return false;
 	}
 	
 	public void checkSpawnCollision() throws SlickException{
 		for(int i=0; i<obstacles.length; i++){
 			if(obstacles[i] != null){
 				while(isColliding(obstacles[i], 1, 0)){
 					player.addX(1);
 				}
 			}
 		}
 	}
 	
 	
 	int changeRate = 10;
 	public void checkUserImageChange(){
 		if(changeRate<=0){
 			player.changeUserImage();
 			changeRate = 20;
 		}else{
 			changeRate--;
 		}
 	}
 	
 	
 	public void checkStatusEffects(){
 		//Goes through all the current StatusEffects player has
 		for(int i=0; i<player.getStatusEffects().size(); i++){
 			StatusEffect currentStatusEffect = player.getStatusEffects().get(i);
 			
 			//Checks and makes the effect if timer has not run out. If it has it will return false and remove the statusEffect from player
 			if(!currentStatusEffect.checkStatusEffect()){
 				player.removeStatusEffect(currentStatusEffect);
 				if(currentStatusEffect.hasStun()){
 					boolean stunDecision = false;
 					for(int j=0; j<player.getStatusEffects().size(); j++){
 						StatusEffect stunCheckSE = player.getStatusEffects().get(j);
 						if(stunCheckSE.hasStun()){
 							stunDecision = true;
 						}
 						player.setStunState(stunDecision);
 					}
 				}
 			}
 		}
 	}
 
 	public void pushPlayer(float xDir, float yDir){
 		System.out.println("PUSHES");
 		
 		float x = player.getX()+xDir*150;
 		float y = player.getY()+yDir*150;
 		
 		
 
 		player.setMouseXPosMove(x);
 		player.setMouseYPosMove(y);
 		
 		xDir = x - player.getX();
 		yDir = y - player.getY();
 		
 		float genDir = (float)Math.sqrt(xDir*xDir+yDir*yDir);
 		
 		
 		Double findNaN = (double)genDir;
 		if(!findNaN.isNaN() && !findNaN.isInfinite()){
 			player.setXDirMove(xDir);
 			player.setYDirMove(yDir);
 			player.setGenDirMove(genDir);
 			player.setXDirMove(player.getXDirMove()/player.getGenDirMove());
 			player.setYDirMove(player.getYDirMove()/player.getGenDirMove());
 			
 			player.resetMoveCounter();
 			player.setRunningState(true);
 			player.setPushState(true);
 		}
 	}
 	
 	
 	/**
 	 * Finds the enemy closest enemy to where the player used the attack and sets him as the target for the guided attack
 	 * @param skill which is the skill that will have a target assigned to it
 	 */
 	public void findAndSetGuidedTarget(Skill skill){
 		Player[] guidedPlayers = GlobalClassSelector.getController().getPlayers();
 		int targetPlayer = player.getPlayerListIndex() != 0 ? 0 : 1;
 		float targetPlayerXDir = guidedPlayers[targetPlayer].getX() - player.getX();
 		float targetPlayerYDir = guidedPlayers[targetPlayer].getY() - player.getY();
 		float targetPlayerGenDir = (float)Math.sqrt(targetPlayerXDir*targetPlayerXDir+targetPlayerYDir*targetPlayerYDir);
 		
 		for(int i=targetPlayer+1; i<guidedPlayers.length; i++){
 			if(guidedPlayers[i] != null && player.getPlayerListIndex() != i){
 				float compXDir = guidedPlayers[i].getX() - player.getX();
 				float compYDir = guidedPlayers[i].getY() - player.getY();
 				float compGenDir = (float)Math.sqrt(compXDir*compXDir+compYDir*compYDir);
 				
 				if(compGenDir<targetPlayerGenDir){
 					targetPlayer = i;
 					targetPlayerXDir = compXDir;
 					targetPlayerYDir = compYDir;
 					targetPlayerGenDir = compGenDir;
 				}
 			}
 		}
 		skill.setGuidedTarget(guidedPlayers[targetPlayer]);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 }
