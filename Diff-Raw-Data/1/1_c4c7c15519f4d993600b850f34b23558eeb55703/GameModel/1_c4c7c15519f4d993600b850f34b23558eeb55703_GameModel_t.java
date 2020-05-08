 package edu.chl.codenameg.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 import edu.chl.codenameg.model.levels.Level;
 import edu.chl.codenameg.model.levels.LevelFactory;
 
 // Remove comments in here
 public class GameModel {
 	private boolean 				running;
 	private int 					selectedLevel;
 	private World 					world;
 	private List<PlayerCharacter> 	listOfPC;
 
 	public GameModel() {
 		listOfPC = new ArrayList<PlayerCharacter>();
 
 		this.selectLevel(1);
 	}
 
 	public void setWorld(World w) {
 		this.world = w;
 	}
 
 	public void startGame() {
 		this.running = true;
 	}
 
 	public void restartGame() {
 		this.endGame();
 		this.selectLevel(selectedLevel);
		this.startGame();
 	}
 
 	public void pauseGame(World w) {
 		this.running = false;
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public void endGame() {
 		if (world != null && running) {
 			this.running = false;
 			this.world = null;
 		}
 	}
 
 	public Level getLevel(int i) {
 		return LevelFactory.getInstance().getLevel(i);
 	}
 
 	public void selectLevel(int i) {
 		Level level = this.getLevel(i);
 		World temp = new World(level);
 		this.selectedLevel = i;
 
 		List<PlayerCharacter> playerList = new ArrayList<PlayerCharacter>();
 		
 		for (int j=1; j <= level.getAmountOfPlayers(); j++) {
 			PlayerCharacter player = new PlayerCharacter(temp);
 			player.setPosition(this.getLevel(i).getPlayerSpawnPosition(j));
 			playerList.add(player);
 		}
 
 		for(PlayerCharacter pc : playerList) {
 			temp.add(pc);
 		}
 		listOfPC.removeAll(listOfPC);
 		listOfPC.addAll(playerList);
 		this.setWorld(temp);
 		System.out.println("Select level " + i);
 	}
 
 	public int getSelectedLevel() {
 		return this.selectedLevel;
 	}
 
 	public void performAction(Action action) {
 		switch (action) {
 		case START_GAME:
 			this.startGame();
 			break;
 		case PAUSE_GAME:
 			this.pauseGame(this.getWorld());
 			break;
 		case RESTART_GAME:
 			this.restartGame();
 			break;
 		case PLAYER_1_MOVE_LEFT:
 			getPlayer(1).move(Direction.LEFT);
 			break;
 		case PLAYER_1_MOVE_RIGHT:
 			getPlayer(1).move(Direction.RIGHT);
 			break;
 		case PLAYER_1_JUMP:
 			getPlayer(1).jump();
 			break;
 		case PLAYER_1_TOGGLE_CROUCH:
 			getPlayer(1).toggleCrouch();
 			break;
 		case PLAYER_1_TOGGLE_LIFT:
 			if (!listOfPC.get(0).isLifting())
 				getPlayer(1).toggleLift();
 			else
 				getPlayer(1).unToggleLift();
 			break;
 		case PLAYER_2_MOVE_LEFT:
 			if (this.listOfPC.size() > 1) {
 				getPlayer(2).move(Direction.LEFT);
 				break;
 			}
 		case PLAYER_2_MOVE_RIGHT:
 			if (this.listOfPC.size() > 1) {
 				getPlayer(2).move(Direction.RIGHT);
 				break;
 			}
 		case PLAYER_2_JUMP:
 			if (this.listOfPC.size() > 1) {
 				getPlayer(2).jump();
 				break;
 			}
 		case PLAYER_2_TOGGLE_CROUCH:
 			if (this.running && this.listOfPC.size() > 1) {
 				getPlayer(2).toggleCrouch();
 				break;
 			}
 		case PLAYER_2_TOGGLE_LIFT:
 			if (this.listOfPC.size() > 1) {
 				if (!listOfPC.get(1).isLifting())
 					getPlayer(2).toggleLift();
 				else
 					getPlayer(2).unToggleLift();
 				break;
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void stopAction(Action action) {
 		boolean twoPlayer = (this.listOfPC.size() > 1) ? true : false;
 		
 		switch (action) {
 		
 		case PLAYER_1_MOVE_LEFT:
 			getPlayer(1).stopMove();
 			break;
 		case PLAYER_1_MOVE_RIGHT:
 			getPlayer(1).stopMove();
 			break;
 		case PLAYER_1_JUMP:
 			getPlayer(1).stopJump();
 			break;
 		case START_GAME:
 			this.startGame();
 			break;
 		case PLAYER_1_TOGGLE_CROUCH:
 			getPlayer(1).unToggleCrouch();
 			break;
 		case PLAYER_2_MOVE_LEFT:
 			if (twoPlayer) {
 				getPlayer(2).stopMove();
 				break;
 			}
 		case PLAYER_2_MOVE_RIGHT:
 			if (twoPlayer) {
 				getPlayer(2).stopMove();
 				break;
 			}
 		case PLAYER_2_JUMP:
 			if (twoPlayer) {
 				getPlayer(2).stopJump();
 				break;
 			}
 		case PLAYER_2_TOGGLE_CROUCH:
 			if (this.running && twoPlayer) {
 				getPlayer(2).unToggleCrouch();
 				break;
 			}
 		default:
 			break;
 		}
 	}
 
 	public PlayerCharacter getPlayer(int num) {
 		return listOfPC.get(num - 1);
 	}
 
 	public void update(int elapsedTime) {
 		if (world != null && running) {
 			world.update(elapsedTime);
 		}
 		if (world.isGameOver()) {
 			if (world.isGameWon()) {
 				System.out.println("Congratulations!");
 			}
 			this.restartGame();
 		}
 		// for (Entity entity : world.getEntities()) {
 		// if (entity instanceof PlayerCharacter) {
 		// PlayerCharacter pc = (PlayerCharacter)entity;
 		// if (pc.hasWonGame()) {
 		// this.restartGame();
 		// System.out.println("Congratulations for winning the game!");
 		// } else if (!(pc.isAlive())) {
 		// this.restartGame(); // TODO Skriv klart
 		// }
 		// }
 		// }
 	}
 }
