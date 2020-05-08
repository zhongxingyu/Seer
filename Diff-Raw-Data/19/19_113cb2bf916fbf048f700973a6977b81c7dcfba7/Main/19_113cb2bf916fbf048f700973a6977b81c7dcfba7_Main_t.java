 package prototype;
 
 import java.awt.Desktop;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.List;
 import player.PlayerIO;
 import player.Player;
import tiles.Checkpoint;
 import typen.WinTile;
 
 
 public class Main {
 
 
 	public static void main(String[] args) {	
 		
 		Menue m = new Menue();
 		
 		while(true){
 			
 			
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e1) {	e1.printStackTrace();}
 						
 			if(m.play){
 				//Start Einstellungen 
 				int startx = 0;
 				int starty = 0;
 				List<Zauber> Zaubern= new LinkedList<Zauber>();
 				List<Gegner> Enemys= new LinkedList<Gegner>();
 
 				int x_MapTiles = 32;
 				int y_MapTiles = 18;
 
 
 //				Initialisierung
 				Map map=new Map(x_MapTiles,y_MapTiles,null);		
 				Player player = new Player(startx,starty,map, Zaubern, Enemys);
 				MapLoader ml = new MapLoader(map, Enemys, Zaubern);
 
 
 				ml.lesen("maps/AktuelleMap.txt");
 
 				//Sound
 				boolean playMusic = false;
 
 				//Spielfenster
 				Frame spielFenster = new Frame("Gruppe93", map, Zaubern,Enemys);
 
 				//Frameratelimiter Variabeln
 				float timeSinceLastFrame =0;
 				long lastFrame=0;
 				long currentFrame = 0;
 				long nachBerechnungsZeit=0;
 				long berechnungsZeit=0;
 				lastFrame=System.currentTimeMillis();
 
 
 
 				// Haupschleife mit FPS Limiter (ca 60 FPS
 				while(true){
 					currentFrame = System.currentTimeMillis();
 					timeSinceLastFrame = ((float)(currentFrame - lastFrame)/1000f);
 					lastFrame = currentFrame;
 //					System.out.println(timeSinceLastFrame);		//evtl fuer debugging benoetigt
 //					System.out.println(berechnungsZeit);
 //					System.out.println(player.getBounding().x+"    "+player.getBounding().y);
 //					System.out.println(spielFenster.getInsets());
 //					System.out.println(spielFenster.getSize());
 
 
 
 						//Updates der Objekte und Akteure
 					PlayerIO.playerUpdate(timeSinceLastFrame);
 					map.spielerTodAnimation(timeSinceLastFrame);
 					for(int i = 0; i<Zaubern.size(); i++){
 						Zaubern.get(i).update(timeSinceLastFrame);}
 					for(int i = 0; i<Enemys.size(); i++){
 						Enemys.get(i).update(timeSinceLastFrame);}
 					spielFenster.nextFrame();			//naechster frame
 
 
 
 					//Spiel beenden
 					if(Keyboard.isKeyDown(KeyEvent.VK_ESCAPE))System.exit(0);
 
 					// Debugging-Hilfen spaeter entfernen
 					if(Keyboard.isKeyDown(KeyEvent.VK_R))PlayerIO.respawn();
 					if(Keyboard.isKeyDown(KeyEvent.VK_K))PlayerIO.setBCheck(true);
 					if(Keyboard.isKeyDown(KeyEvent.VK_L))PlayerIO.setBCheck(false);
 
 			// 		Hintergrund Musik wird abgespielt
 					if(playMusic==false){
 						HintergrundMusik so = new HintergrundMusik();
 						Thread x = new Thread(so);
 						x.start();
 						playMusic = true;
 					}
 
 			// Beispiel fuer das Abspielen von Soundeffekten
 			// Im Anwendungsfall Verzoegerung einbauen
 
 					if(Keyboard.isKeyDown(KeyEvent.VK_B))
 					{
 						SoundFX eins = new SoundFX("sound/boing.mp3");
 						Thread y = new Thread(eins);
 						y.start();
 					}
 
 					if(Map.needPort){
 						ml.lesen(map.getNextMap());
 						Map.needPort = false;
 					}
 
 					if(Map.goShop){
 						ml.lesen("maps/shop.txt", true);
 						Map.goShop = false;
 					}
 					if(Map.resetMap){
						ml.lesen(Checkpoint.getMapname());
 						Map.resetMap =false;
 					}
 
 
 
 			//Schlafen
 					nachBerechnungsZeit=System.currentTimeMillis();
 					berechnungsZeit=nachBerechnungsZeit-currentFrame;
 					if(berechnungsZeit<15){
 						try {Thread.sleep(15-berechnungsZeit);} 
 						catch (InterruptedException e) {e.printStackTrace();}
 					}
 				}
 			}//WHILE ENDE
 		}
 	}//main Ende
 }
