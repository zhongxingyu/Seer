 package prototype;
 
 
 public class Map 
 {
 	private boolean spielertot=false;
 	private float sekundenTakt=0;
 	private static Tile[][] tiles;
 	private static int x_Tiles;
 	private static int y_Tiles;
	private static String nextmap;
	//private String currentMap;
 	
 	
 	//KONSTRUKTOR
 	public Map(int x_Tiles, int y_Tiles, String nextmap){
		Map.nextmap=nextmap;
 		Map.x_Tiles = x_Tiles+1;
 		Map.y_Tiles = y_Tiles+1;
 		tiles = new Tile[x_Tiles+2][y_Tiles+2];
 		for(int x = 1; x <= x_Tiles; x++)
 		{
 			for(int y = 0; y <= y_Tiles; y++){
 				tiles[x][y] = new  Tile(x*40, y*40, false, 1); //Initialisiert jedes Feld der nutzbaren Map
 			}
 		}
 		
 		// Zwangsrahmen
 		
 		for(int x = 0; x <= x_Tiles+1;x++){tiles[x][0]= new Tile(x*40, 0*40, true, 2);}
 		for(int x = 0; x <= x_Tiles+1;x++){tiles[x][y_Tiles+1]= new Tile(x*40, (y_Tiles+1)*40, true, 2);}
 		for(int y = 0; y <= y_Tiles+1;y++){tiles[0][y]= new Tile(0*40, y*40, true, 2);}
 		for(int y = 0; y <= y_Tiles+1;y++){tiles[x_Tiles+1][y]= new Tile((x_Tiles+1)*40, y*40, true, 2);}
 	}
 
 	//METHODEN
 	public Tile getTile(int x,int y)
 	{
 		return tiles[x][y];
 	}
 	
 	public int getXTiles(){
 		return x_Tiles;
 	}
 	
 	public int getYTiles() {
 		return y_Tiles;
 	}
 	
 	public void setNextMap(String nextmap)
 	{
		Map.nextmap = nextmap;
 	}
 	
 	public String getNextMap(){
 		return nextmap;
 	}
 	
 	public void setSpielerTod(boolean tot)
 	{
 		spielertot=tot;
 		sekundenTakt=0;//damit das Blinken immer auf schwarz beginnt und gleichmaeig startet
 	}
 	
 	public void spielerTodAnimation(float frametime)//Blinken fuer Spielertod
 	{
 		sekundenTakt+=frametime;
 		if(sekundenTakt>=2)sekundenTakt=0;
 		if(spielertot&&sekundenTakt<1)
 		{
 			for(int x = 0; x <=x_Tiles; x++)
 			{
 				for(int y = 0; y <= y_Tiles; y++)
 				{
 					tiles[x][y].setTex(5);
 				}
 			}
 		}else if(spielertot)//sekundentakt zwischen 1und 2 ist hier klar und muss nicht nochmal geprueft werden
 		{
 			for(int x = 0; x <=x_Tiles; x++)
 			{
 				for(int y = 0; y <=y_Tiles; y++)
 				{
 					tiles[x][y].setTex(6);
 				}
 			}
 		}
 	}
 	
 	public void errMap()//Errstellt map die auf Fehler hinweisen soll 
 	{
 		for(int x = 0; x<=x_Tiles;x++)
 		{
 			for(int y = 0;y<=y_Tiles;y++)
 			{
 				tiles[x][y].setErr();
 			}
 		}
 	}
 	
 	public void setWin(){ 		//Pokale Pokale Pokale
 		for(int x = 0; x<=x_Tiles;x++)
 		{
 			for(int y = 0;y<y_Tiles;y++)
 			{
 				tiles[x][y].setWintile();
 			}
 		}
 	}
 }      
