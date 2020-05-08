 package prototype;
 
 import java.awt.Desktop;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Main {
 	
 	public static void main(String[] args) {	
 		//Start Einstellungen 
 		int worldsizex = 1280;
 		int worldsizey = 720;
 		int startx = 120;
 		int starty = 550;
 		List<Zauber> Zaubern= new LinkedList<Zauber>();
 		List<Gegner> Enemys= new LinkedList<Gegner>();
 		
 		int x_MapTiles = 32;
 		int y_MapTiles = 18;
 		
 		
 //		Initialisierung
 		Map map=new Map(x_MapTiles,y_MapTiles,null);		
 		Player player = new Player(startx,starty,map, Zaubern, Enemys);
 		MapLoader ml = new MapLoader(map, player, Enemys);
		//Shop shop = new Shop();
 		
 		ml.lesen("maps/test.txt");
 		
 		
 		
 		//Sound
 		boolean playMusic = false;
 		
 		//Spielfenster
 		Frame spielFenster = new Frame("Gruppe93",player, map, Zaubern,Enemys);
 		spielFenster.makeBuff();		//ist fuer die BufferStrategy zwingend erforderlich
 		spielFenster.setSizeRight(worldsizex,worldsizey);	//Groee kann erst hier gesetzt werden, weil im Konstruktor die Insets des Fenster noch falsch sind
 //		spielFenster.setLocationRelativeTo(null);
 		
 		
 		
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
 //			System.out.println(timeSinceLastFrame);		//evtl fuer debugging benoetigt
 //			System.out.println(berechnungsZeit);
 //			System.out.println(player.getBounding().x+"    "+player.getBounding().y);
 //			System.out.println(spielFenster.getInsets());
 //			System.out.println(spielFenster.getSize());
 			
 			
 			
 			//Updates der Objekte und Akteure
 			player.update(timeSinceLastFrame);
 			map.spielerTodAnimation(timeSinceLastFrame);
 			for(int i = 0; i<Zaubern.size(); i++){
 				Zaubern.get(i).update(timeSinceLastFrame);}
 			for(int i = 0; i<Enemys.size(); i++){
 				Enemys.get(i).update(timeSinceLastFrame);}
 			spielFenster.nextFrame();			//naechster frame
 			
 			
 			
 			//Spiel beenden
 			if(Keyboard.isKeyDown(KeyEvent.VK_ESCAPE))System.exit(0);
 			
 			// Debugging-Hilfen spaeter entfernen
 			if(Keyboard.isKeyDown(KeyEvent.VK_R))player.respawn();
 			if(Keyboard.isKeyDown(KeyEvent.VK_K))player.bCheckOn();
 			if(Keyboard.isKeyDown(KeyEvent.VK_L))player.bCheckOff();
 			if(Keyboard.isKeyDown(KeyEvent.VK_0))map = new Map(15, 15,null);
 			
 			// Hintergrund Musik wird abgespielt
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
 			
 			//Aufruf bei Sieg 
 			//TODO Verlagern
 			if(map.getTile(1, 1).getTex()==8){
 				try {
 					Desktop.getDesktop().browse(new URI("http://www.youtube.com/watch?v=DLTZctTG6cE")); //Ruft Youtube auf siehe Java API
 				} catch (IOException | URISyntaxException e) {}
 				try {
 					Thread.sleep(8000);
 				} catch (InterruptedException e) {}
 				System.exit(0);
 				
 			}
 					
 		if(player.getNeedPort()){
 			ml.lesen(map.getNextMap(),false);
 			player.setNeedPort();
 		}
 		
 		if(player.getGoShop()){
 			ml.lesen("maps/shop.txt", true);
 			player.setGoShop();
 		}
 		if(player.getResetMap()){
 			ml.lesen("maps/test.txt");
 			player.setResetMap();
 		}
 		
 
 			
 			//Schlafen
 			nachBerechnungsZeit=System.currentTimeMillis();
 			berechnungsZeit=nachBerechnungsZeit-currentFrame;
 			if(berechnungsZeit<15){
 				try {Thread.sleep(15-berechnungsZeit);} 
 				catch (InterruptedException e) {e.printStackTrace();}
 			}	
 		}
 	}//main Ende
 }
