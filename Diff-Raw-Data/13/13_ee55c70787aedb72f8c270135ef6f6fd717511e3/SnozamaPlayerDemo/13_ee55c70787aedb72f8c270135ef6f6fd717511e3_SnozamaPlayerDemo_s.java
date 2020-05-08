 package snozama.runtime.demos;
 
 import snozama.client.SnozamaPlayer;
 import snozama.ui.api.AUI;
 import snozama.ui.eventListeners.UIReadyListener;
 
 public class SnozamaPlayerDemo
 {
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
		player.joinRoom(0);
 	}
 }
