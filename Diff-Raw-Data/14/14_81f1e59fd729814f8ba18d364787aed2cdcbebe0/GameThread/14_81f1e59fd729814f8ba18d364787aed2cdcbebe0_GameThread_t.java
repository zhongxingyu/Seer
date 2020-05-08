 package edu.mondragon.eps.jaagirre.game.desktop.tdd.spaceinvaders;
 
/*Comienzo de arreglo para Hotfix de join de hilos.
 * Modificaciones a realizar:
 * Cuando finaliza
 * */

 public class GameThread  extends Thread{
 	private boolean running;
 	Game game;
 	public GameThread(Game game){
 		this.game = game;
 		running = false;
 	}
 		//gameLogic.update();
 		//Canvas tiene que tener una refencia al gameLogic y nada mas de manera que lo sbuffer sean del otro
 	@Override
 	public void run() {
         running = true;
         while( game.isGameRunning() ){
                game.step();
         }
         running = false;
         
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}	
 	
 }
