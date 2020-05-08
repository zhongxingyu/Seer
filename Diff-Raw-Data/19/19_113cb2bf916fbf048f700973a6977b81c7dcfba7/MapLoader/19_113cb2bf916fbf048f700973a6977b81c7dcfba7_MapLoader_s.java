 package prototype;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.Scanner;
 
 import player.PlayerIO;
 import tiles.*;
 import typen.Boden;
 import typen.CheckpointTile;
 import typen.Err;
 import typen.Exit;
 import typen.Teleporter;
 import typen.Trap;
 import typen.Wand;
 import typen.WinTile;
 
 public class MapLoader {
 
 	
 	private InputStream files;
 	private List<Gegner> Enemys;
 	private List<Zauber> Zaubern;
 	private boolean comeback = false;
 	
 	MapLoader(Map map,List<Gegner> Enemys,List<Zauber> Zaubern)
 	{
 		this.Enemys = Enemys;
 		this.Zaubern = Zaubern;
 	}
 	
 	public void lesen(String filename)
 	{
 		lesen(filename,false);
 	}
 
 	public void lesen(String filename, boolean intern)
 	{
 		int sizeX;
 		int sizeY;
 		float startX;
 		float startY;
 		String nextMap;
 		
 		//intern Store
 		if(intern)
 		{
 			if(Shop.isInShop()){
 				filename = Shop.getLastMap();
 				Shop.setInShop(false);
 				Shop.setNextPort(3000+System.currentTimeMillis());
 				comeback = true;
 			}else{
 				Shop.setInShop(true);
 				Shop.setF_playPosx(PlayerIO.getF_PlayerPostionX());
 				Shop.setF_playPosy(PlayerIO.getF_PlayerPostionY());
 				Shop.setLastMap(Map.getCurrentMap());
 				Shop.setNextPort(3000+System.currentTimeMillis());
 			}
 		}
 		
 		//Map reset
 		
 		if(Map.resetMap&&Checkpoint.isSet()){nextMap = Checkpoint.getMapname();}
 		
		
		
 		// Loescht Gegner und Zauber
 		int gegneranzahl=Enemys.size();
 		for(int i = 0; i<gegneranzahl; i++){
 		Enemys.remove(0);}
 				
 		int zauberzahl=Zaubern.size();
 		for(int i = 0; i < zauberzahl; i++){
 			Zaubern.remove(0);}
 		
 	
 		try{
 			files = MapLoader.class.getClassLoader().getResourceAsStream(filename);
 			Scanner s = new Scanner(files);
 			
 			if(s.hasNext())
 			{	
 				//Header Lesen und verarbeiten 
 				sizeX = s.nextInt();   	//Hoehe in Tiles
 				sizeY = s.nextInt();	//Breite in Tiles
 				startX = s.nextInt();	//Spawnposition 
 				startY = s.nextInt();	// s.o.
 				nextMap = s.next();//  Ort der naechsten Karte
 				
 				
 				//Modifikatoren fuer internen Rueckport
 				if(comeback){
 					startX = Shop.getF_playPosx();
 					startY = Shop.getF_playPosy();
 					comeback = false;
 				}
 				
 				if(Map.resetMap&&Checkpoint.isSet()){
					startX = (float)Checkpoint.getPositionX();
					startY = (float)Checkpoint.getPositionY();
 				}
 				
 				
 				//Neuaufbau
 				new Map(sizeX,sizeY,nextMap);
 				PlayerIO.playerTeleport(startX, startY);	//Spieler Spawnen lassen
 				Map.setCurrentMap(filename);
 				
 				
 				//Tiles fuellen
 				int tileType;
 				for(int y= 1; y<=sizeY;y++)
 				{
 					for(int x = 1; x<=sizeX;x++)
 					{
 						if(s.hasNextInt()){tileType = s.nextInt();}else{tileType = 0;}
 						Map.tiles[x][y]=new Tile(x*40, y*40);
 						switch(tileType)
 						{
 						case 1:
 							Map.tiles[x][y].setTileTyp(new Boden());
 							break;
 						case 2:
 							Map.tiles[x][y].setTileTyp(new Wand());
 							break;
 						case 3:
 							Map.tiles[x][y].setTileTyp(new Trap());
 							break;
 						case 4:
 							Map.tiles[x][y].setTileTyp(new Teleporter());
 							break;
 						case 5:
 							Map.tiles[x][y].setTileTyp(new typen.Shop());
 							break;
 						case 6:
 							Map.tiles[x][y].setTileTyp(new Exit());
 							break;
 						case 7:
 							Map.tiles[x][y].setTileTyp(new WinTile());
 							break;
 						case 8:
 							Map.tiles[x][y].setTileTyp(new CheckpointTile());
 							break;
 							
 						
 							// Spawnt Gegner
 						case 51:
 							Map.tiles[x][y].setTileTyp(new Boden());
 							Enemys.add(new Gegner(x*40+10, y*40+10,1, Enemys,Zaubern));
 							break;
 						case 52:
 							Map.tiles[x][y].setTileTyp(new Boden());
 							Enemys.add(new Gegner(x*40+10, y*40+10,2, Enemys,Zaubern));
 							break;
 					
 						default:
 							Map.tiles[x][y].setTileTyp(new Err());
 							break;
 							
 					
 						}
 					}
 				}
 			}
 			s.close();
 		}catch (Exception e){//map.errMap();
 		}
 	}
 }
