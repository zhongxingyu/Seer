 package battleships.server;
 
 import battleships.game.Coordinate;
 import battleships.game.Navy;
 import battleships.game.ServerAI;
 import battleships.game.Ship;
 import battleships.game.Validator;
 import battleships.message.FinishedMessage;
 import battleships.message.HitMessage;
 import battleships.message.Message;
 import battleships.message.NavyMessage;
 import battleships.message.Shot;
 import battleships.message.ValidationMessage;
 
 /**
  * A game session object.
  * @author Magnus Hedlund
  * */
 public class Session implements Runnable{
 	private final static int PLAYER0 =0, PLAYER1=1, SUBMARINES=5, DESTROYERS=3, AIRCRAFT_CARRIERS=1;
 	private Player[] player = new Player[2];
 	private boolean[] navyValid = new boolean[2];
 	private Navy[] navy = new Navy[2];
 	private int currentPlayer=PLAYER0, otherPlayer=PLAYER1;
 	private boolean isHit=false;
 	private boolean isSunk=false;
 	private boolean grantTurn=false;
 	private boolean finished=false;
 	private ServerAI serverAI=null;
 	
 	public Session(Player first, Player second){
 		player[PLAYER0]=first;
 		player[PLAYER1]=second;
 	}
 	
 	public Session(Player first){
 		player[PLAYER0]=first;
 		serverAI = new ServerAI(5,3,1); 
 		navyValid[PLAYER1]=true; //skip validation of server Navy
 		navy[PLAYER1]=serverAI.getNavy();
 	}
 	
 	/**
 	 * Runs the game.
 	 * */
 	@Override
 	public void run() {
 		
 		/* listen and validate Navy objects*/
 		while(!navyValid[PLAYER0] && !navyValid[PLAYER1]){
 			if(!navyValid[PLAYER0]){
 				boolean valid = readAndValidate(PLAYER0);
 				navyValid[PLAYER0]=valid;
 				player[PLAYER0].sendMessage(new ValidationMessage(valid));
 			}
 			if(!navyValid[PLAYER1]){
 				boolean valid = readAndValidate(PLAYER1);	
 				navyValid[PLAYER1]=valid;
 				player[PLAYER0].sendMessage(new ValidationMessage(valid));
 			}
 		}
 		
 		enterGameLoop();
 		
 	}//run end
 	
 	/**
 	 * Listening for NavyMessage and verifies the Navy object.
 	 * 
 	 * */
 	private boolean readAndValidate(int playerNumber){
 		Validator validator = new Validator(SUBMARINES,DESTROYERS,AIRCRAFT_CARRIERS);
		Message msg = readMessage(player[playerNumber]);
 		if(msg.getType()=="NavyMessage"){
 			NavyMessage navMsg = (NavyMessage)msg;
 			if(validator.validateNavy(navMsg.getNavy())){  //TODO Correct call 
 				navy[playerNumber]=navMsg.getNavy();
 				return true;
 			}
 			else{
 				return false;
 			}
 			
 		}
 		else{
 			return false;
 		}	
 	}
 	
 	
 	/**
 	 * The game loop. Read messages/getshots and evaluate. Messages are sent to clients(not to the ServerAI)
 	 * */
 	private void enterGameLoop(){
 		boolean loop=true;
 		Coordinate shotCoordinate=null;
 		while(loop){
 			
 			//reset
 			isHit=false;
 			isSunk=false;
 			grantTurn=false;
 			finished=false;
 			shotCoordinate=null;
 			
 			//Read message
 			if(player[currentPlayer]!=null){  //an actual player
				
				Message msg = readMessage(player[currentPlayer]);
 				Shot navMsg=null;
 				if(msg.getType()=="Shot"){
 					navMsg = (Shot)msg;
 					shotCoordinate = navMsg.getCoordinate();
 				}
				
					
				
 			}
 			else {  //get shot coordinate from ServerAI
 				if(serverAI!=null){
 					shotCoordinate = serverAI.shoot();
 				}	
 			}
 			
 			// do we have a Coordinate?
 			if(shotCoordinate!=null){
 				
 				Ship hitShip=navy[otherPlayer].shot(shotCoordinate);
 				
 				//a hit
 				if(hitShip!=null){
 					isHit=true;
 					if(!hitShip.isSunk()){
 						hitShip=null;  //dont send Ship unless sunk
 					}
 					else{
 						isSunk=true;
 						// check if won
 						if(navy[otherPlayer].allGone()){
 							finished=true;
 						}
 					}
 				}
 				// no hit
 				else{
 					//if miss  let the other one fire
 					grantTurn=true;
 				}
 				
 				if(finished){
 					if(player[currentPlayer]!=null){
 						player[currentPlayer].sendMessage(new FinishedMessage(true, navy[currentPlayer]));
 					}
 					
 					//send hitMessage to otherPlayer
 					if(player[otherPlayer]!=null){
 						player[otherPlayer].sendMessage(new FinishedMessage(false, navy[otherPlayer]));
 					}
 					
 					loop=false;
 				}
 				else{
 					//send hitMessage to currentPlayer
 					if(player[currentPlayer]!=null){
 						player[currentPlayer].sendMessage(new HitMessage(isHit, shotCoordinate, isSunk, hitShip));
 					}
 					
 					//send hitMessage to otherPlayer
 					if(player[currentPlayer]!=null){
 						player[otherPlayer].sendMessage(new NavyMessage(navy[otherPlayer], grantTurn));
 					}
 				}
 
 				//if otherPlayer was granted next turn, Switch player
 				if(grantTurn){
 					switchPlayer();
 				}
 			}
 			else{ //the message received was of wrong type
 				if(player[currentPlayer]!=null){
 					player[currentPlayer].sendMessage(new ValidationMessage(false));
 				}
 				
 			}
 		}//while end
 	}
 	
 	/**
 	 * 
 	 * Switch positions of the currentPlayer and otherPlayer
 	 * 
 	 * */
 	private void switchPlayer(){
 		
 		otherPlayer=currentPlayer;
 		
 		if(currentPlayer==PLAYER0){
 			currentPlayer=PLAYER1;
 		}
 		else{
 			currentPlayer=PLAYER0;
 		}
 	}
 	
	private Message readMessage(Player p){
		Message msg=null;
		while(msg==null){
			try{
				msg=p.readMessage();
				if(msg==null){
					Thread.sleep(500);
				}
			}catch(Exception e){
				 
			}
		}
		return msg;	
	}
	
 }
