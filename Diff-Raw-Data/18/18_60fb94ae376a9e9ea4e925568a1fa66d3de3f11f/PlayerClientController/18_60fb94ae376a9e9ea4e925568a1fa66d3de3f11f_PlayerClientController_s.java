 package sshaclient;
 
 import Model.Player;
 import Model.Skills.Skill;
 import Model.Skills.Hunter.*;
 import Model.Skills.Warrior.*;
 import Model.Skills.Wizard.*;
 
 public class PlayerClientController implements PlayerControl {
 
 	private SocketClient sc;
 	private Player tp; 
 
 	private int playerID;
 	private boolean isAlive = false;
 	
 	public PlayerClientController(SocketClient sc, Player tp) {
 		this.sc = sc;
 		this.tp = tp;
 		
 		isAlive = true;
 	}
 	
 	
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		String tempStats;
 		Skill[] tempSkills = new Skill[5];
 		Skill[] realSkills;
 		String currentSkill;
 		int currentSkillLvl;
 		
 		while(isAlive) {
 			
 			tempStats = sc.getPlayerStats(playerID);
 			
 			// Set Player stats in lobby with info from Server
 			if(Constants.getItem(tempStats, 2).equals("lobby")) {
 				tp.setKills(Integer.parseInt(Constants.getItem(tempStats, 4)));
 				tp.setGold(Integer.parseInt(Constants.getItem(tempStats, 5)));
 				for(int i = 0; i < tempSkills.length; i++) {
 					currentSkill = Constants.getItem(tempStats, (2*(i+1)+5));
 					currentSkillLvl = Integer.parseInt(Constants.getItem(tempStats, (2*(i+1)+6)));
 					switch(currentSkill) {
 					case "arrowflurry":
 						tempSkills[i] = new SkillArrowFlurry();
 						break;
 					case "barrelroll":
 						tempSkills[i] = new SkillBarrelRoll();
 						break;
 					case "cripplingshot":
 						tempSkills[i] = new SkillCripplingShot();
 						break;
 					case "flamingarrow":
 						tempSkills[i] = new SkillFlamingArrow();
 						break;
 					case "guidedarrow":
 						tempSkills[i] = new SkillGuidedArrow();
 						break;
 					case "lifestealingarrows":
 						tempSkills[i] = new SkillLifestealingArrows();
 						break;
 					case "passivedodge":
 						tempSkills[i] = new SkillPassiveDodge();
 						break;
 					case "sprint":
 						tempSkills[i] = new SkillSprint();
 						break;
 					case "stealth":
 						tempSkills[i] = new SkillStealth();
 						break;
 					case "arrow":
 						tempSkills[i] = new SkillArrow();
 						break;
 					case "adrenaline":
 						tempSkills[i] = new SkillAdrenaline();
 						break;
 					case "firstaid":
 						tempSkills[i] = new SkillFirstAid();
 						break;
 					case "grapplinghook":
 						tempSkills[i] = new SkillGrapplingHook();
 						break;
 					case "improvedarmor":
 						tempSkills[i] = new SkillImprovedArmor();
 						break;
 					case "increasedmovement":
 						tempSkills[i] = new SkillIncreasedMovement();
 						break;
 					case "leapattack":
 						tempSkills[i] = new SkillLeapAttack();
 						break;
 					case "shieldstance":
 						tempSkills[i] = new SkillShieldStance();
 						break;
 					case "slash":
 						tempSkills[i] = new SkillSlash();
 						break;
 					case "throwingaxe":
 						tempSkills[i] = new SkillThrowingAxe();
 						break;
 					case "warstomp":
 						tempSkills[i] = new SkillWarstomp();
 						break;
 					case "absorb":
 						tempSkills[i] = new SkillAbsorb();
 						break;
 					case "blizzard":
 						tempSkills[i] = new SkillBlizzard();
 						break;
 					case "fireball":
 						tempSkills[i] = new SkillFireball();
 						break;
 					case "firestorm":
 						tempSkills[i] = new SkillFirestorm();
 						break;
 					case "flamewave":
 						tempSkills[i] = new SkillFlamewave();
 						break;
 					case "iceblock":
 						tempSkills[i] = new SkillIceblock();
 						break;
 					case "ironcloak":
 						tempSkills[i] = new SkillIroncloak();
 						break;
 					case "teleport":
 						tempSkills[i] = new SkillTeleport();
 						break;
 					case "unstablemagic":
 						tempSkills[i] = new SkillUnstableMagic();
 						break;
 					case "wandattack":
 						tempSkills[i] = new SkillWandattack();
 						break;
 					case "noskill":
 						tempSkills[i] = null;
 						break;
 					}
 					if(tempSkills[i] != null) {
 						tempSkills[i].setCurrentLvl(currentSkillLvl);
 					}
 				}
 				
 				tp.setSkillList(tempSkills);
 				
 			} else {
 				tp.setX(Float.parseFloat(Constants.getItem(tempStats, 3)));
 				tp.setY(Float.parseFloat(Constants.getItem(tempStats, 4)));
 				tp.setRotation(Float.parseFloat(Constants.getItem(tempStats, 5)));
 				tp.setRunningState(Boolean.valueOf(Constants.getItem(tempStats, 6)));
 				tp.setStunState(Boolean.valueOf(Constants.getItem(tempStats, 7)));
 				tp.setMovementSpeed(Double.parseDouble(Constants.getItem(tempStats, 8)));
 				
 				realSkills = tp.getSkillList();
 				
 				for(int j = 0; j < realSkills.length; j++) {
 					if(realSkills[j] != null) {
 						realSkills[j].setAttX(Float.parseFloat(Constants.getItem(tempStats, (j+1)*5+6)));
 						realSkills[j].setAttY(Float.parseFloat(Constants.getItem(tempStats, (j+1)*5+7)));
 						realSkills[j].setRotation(Float.parseFloat(Constants.getItem(tempStats, (j+1)*5+8)));
 						realSkills[j].setAttackingState(Boolean.valueOf(Constants.getItem(tempStats, (j+1)*5+9)));
 					}
 				}
 			}
 			
 			try {
 				Thread.sleep(Constants.globalSleep);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	@Override
 	public void killItWithFire() {
 		System.out.println("Will terminate");
 		isAlive = false;
 	}
 }
