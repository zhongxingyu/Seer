 package snozama.runtime.demos;
 
 import snozama.client.SnozamaPlayer;
 import snozama.ui.api.AUI;
 import snozama.ui.eventListeners.UIReadyListener;
 
 public class SnozamaPlayerDemo
 {
	private static final int OK = 0;
	private static final int KAL = 1;
	private static final int YELLOW = 2;
	private static final int BEAR = 3;
	private static final int BEAVER = 4;
	private static final int JACKPINE = 5;
	private static final int WOOD = 6;
	
	private static int room = OK;
	
 	public static void main(String args[])
 	{
 		AUI.getUI();
 		AUI.ready(new UIReadyListener(){
 			@Override
 			public void ready()
 			{
 				//Does anything need to go in here?
 				/* - alex: anything that requires the UI to be ready to interact with
 				 * the application should go in here. It's probably a good idea to put
 				 * everything in inside the ready.
 				 */
 			}
 		});
 		
 		SnozamaPlayer player = new SnozamaPlayer();
		player.joinRoom(room);
 	}
 }
