 package prototype;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.Scanner;
 
 public class MapLoader {
 
 	
 	private Map map;
 
 	private InputStream files;
 	private Player player;
 	private List<Gegner> Enemys;
 	
 	MapLoader(Map map, Player player,List<Gegner> Enemys)
 	{
 		this.map = map;
 		
 		this.player = player;
 		this.Enemys = Enemys;
 
 	}
 	
 	public void lesen(String filename)
 	{
 		lesen(filename,false);
 	}
 
 	public void lesen(String filename, boolean intern)
 	{
 		// Loescht Gegner
 		
 		int gegneranzahl=Enemys.size();// Festellung der Gegnerzahl
 		for(int i = 0; i<gegneranzahl; i++){
 		Enemys.remove(0);}
 		
 		
 		//intern = intern;
 	
 		
 		files = MapLoader.class.getClassLoader().getResourceAsStream(filename);
 		Scanner s = new Scanner(files);
 		
 		if(s.hasNext())
 		{
 			
 			//Header Lesen und verarbeiten 
 			int sizeX = s.nextInt();   	//Hoehe in Tiles
 			int sizeY = s.nextInt();	//Breite in Tiles
 			int startX = s.nextInt();	//Spawnposition 
 			int startY = s.nextInt();	// s.o.
 			//int sizeCheck = sizeX*sizeY;
 			int tileType;
 			String nextMap = s.next();
				//  Ort der naechsten Karte
 			if(nextMap.equals(null))map.setWin();
 			player.setPosition((float)startX ,(float)startY);	//Spieler Spawnen lassen
 			
 			
 			map = new Map(sizeX,sizeY,nextMap);		//Map Laden
			map.setNextMap(nextMap);
 			
 			for(int y= 1; y<=sizeY;y++)
 			{
 				for(int x = 1; x<=sizeX;x++)
 				{
 					if(s.hasNextInt()){tileType = s.nextInt();}else{tileType = 0;}
 					
 					switch(tileType)
 					{
 					case 1:
 						map.getTile(x, y).setFloor();
 						break;
 					case 2:
 						map.getTile(x, y).setWall();
 						break;
 					case 3:
 						map.getTile(x, y).setTrap();
 						break;
 					case 4:
 						map.getTile(x, y).setTeleporter();
 						break;
 					case 5:
 						map.getTile(x, y).setExit();
 						break;
 					case 6:
 						map.getTile(x, y).setWintile();
 						break;
 					case 7:
 						map.getTile(x, y).setShop();
 						break;
 						
 					// Spawnt Gegnet
 					case 51:
 						map.getTile(x, y).setFloor();
 						Enemys.add(new Gegner(x*40+10, y*40+10, Enemys));
 						break;
 					
 					default:
 						map.getTile(x, y).setErr();
 						break;
 						
 					
 					}
 				}
 			}
 		}
 		s.close();
 	}
 }
