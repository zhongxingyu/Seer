 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.openal.SoundStore;
 
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.TimeUnit;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 public class Game implements Runnable {
 	/**
 	 * 2D-Array als Spielfeld
 	 */
 	public static Feld[][] spielfeld;
 	//public Player one, two;
 	/**
 	 * Liste des Spieler
 	 */
 	public final static List<Player> players = new ArrayList<Player>();
 	/**
 	 * Unterbrechungsschluessel fuer Thread
 	 */
 	static Lock lock1 = new ReentrantLock();
 
 	/**
 	 * Ist es ein Extra?
 	 */
 	private boolean[] arr;
 	
 	/**
 	 * Breite und Hoehe des Spielfeldes
 	 */
 	protected int breit, hoch;
 	
 	/**
 	 * ist es ein netzwerk-spiel?
 	 */
 	private boolean netzwerk = false;
 	
 	private Network net;
 	/**
 	 * Spiel wird geladen
 	 * @param spielerzahl
 	 * @param level - Dateiname des Levels als xml
 	 * @param netzwerk - boolean wert fuer netzwerk spiel
 	 */
 	public Game(int spielerzahl, String level, boolean netzwerk, boolean hosting) {
 		//Spielfeld laden
 		try {
 			initialfeld(level, spielerzahl); 
 		} catch (FileNotFoundException e) {
 			// Level nicht gefunden
 			e.printStackTrace();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 		
 		this.netzwerk = netzwerk;
 		if(netzwerk) {
 			net = new Network(hosting);
 		}
 	}
 	
 	/**
 	 * Veraendert die Zustaende des Arrays bzgl. der Anzahl der Power-Ups
 	 */
 	public void setArray(int i)
 	{
 		if(arr[i])
 		{
 			
 		}
 	}
 	/**
 	 * Zeichnung wird gestellt
 	 */
 	
 	
 	public void pollInput(){
 		lock1.lock();
 		
 		for(int i = 0; i < players.size(); i++) {
 			Player p = players.get(i);
 				
 			if (spielfeld[p.getx()][p.gety()] instanceof Explosionsfeld) {
 				p.hit(((Explosionsfeld)spielfeld[p.getx()][p.gety()]).getBomb());
 //				lucida.setScale(0.33f);
 //				lucida.print(10, 5, "Spieler "+i+" hat noch "+p.lives+" Leben.");
 				}
 //			System.out.println(p.lives); //Ueberpruefung der Leben
 			if(spielfeld[p.getx()][p.gety()] instanceof Exitfeld){
 					if(((Exitfeld)spielfeld[p.getx()][p.gety()]).isCovered() == false) {
 						Main.m.gameover.setText("Spieler "+(i+1)+" hat gewonnen!");
 						p.die();
 					}
 				}			
 //PowerUps einsammeln
 			if (spielfeld[p.getx()][p.gety()] instanceof Extrasfeld){
 				p.collect((Extrasfeld)spielfeld[p.getx()][p.gety()]);
 				spielfeld[p.getx()][p.gety()]= new Leerfeld();
 			}
 			
 //Falle des Gegners auslösen
 			if (spielfeld[p.getx()][p.gety()] instanceof Fallenfeld){
 				if (!( ((Fallenfeld)spielfeld[p.getx()][p.gety()]).getFallensteller()==p.getName() )){
 					if (((Fallenfeld)spielfeld[p.getx()][p.gety()]).getArt()==5){
 						p.setLatency(450);spielfeld[p.getx()][p.gety()]= new Leerfeld();break;
 					}
 					if (((Fallenfeld)spielfeld[p.getx()][p.gety()]).getArt()==8){
 						p.confuse(); System.out.println("test3");
 						spielfeld[p.getx()][p.gety()]= new Leerfeld();break;
 					}										
 				}
 			}
 			
 			
 			if(!p.isAlive()) { continue; }
 			
 			if (Keyboard.isKeyDown(p.getKeyRight())){
 				p.setRightMovement(true);
 			}
 					 
 			if (p.isMovingRight()) {
 				if (spielfeld[p.getx()+1][p.gety()] instanceof Leerfeld | 
 					spielfeld[p.getx()+1][p.gety()] instanceof Explosionsfeld){
 					 p.move(1,0);
 				}  else if(spielfeld[p.getx()+1][p.gety()] instanceof Extrasfeld){
 					if(((Extrasfeld)spielfeld[p.getx()+1][p.gety()]).isCovered() == false){
 						p.move(1,0);
 					}
 					    } else if(spielfeld[p.getx()+1][p.gety()] instanceof Exitfeld){
 					    		if (((Exitfeld)spielfeld[p.getx()+1][p.gety()]).isCovered() == false){ 
 					    			p.move(1,0);
 					    		}
 							   } else if ((spielfeld[p.getx()+1][p.gety()] instanceof Bombenfeld) && (p.getKicker())){
 											Bombe.Bombs.get(Bombe.getBomb(p.getx()+1, p.gety())).setkickedRight();	
 									} else if (spielfeld[p.getx()+1][p.gety()] instanceof Fallenfeld) {
 										p.move(1,0);;
 									}
 			p.setRightMovement(false); 
 			}
 			
 			if (Keyboard.isKeyDown(p.getKeyLeft())){
 				p.setLeftMovement(true);
 			}
 					 
 			if (p.isMovingLeft()) {
 				if (spielfeld[p.getx()-1][p.gety()] instanceof Leerfeld | 
 					spielfeld[p.getx()-1][p.gety()] instanceof Explosionsfeld){
 					p.move(-1,0);
 				}  else if(spielfeld[p.getx()-1][p.gety()] instanceof Extrasfeld){
 					if(((Extrasfeld)spielfeld[p.getx()-1][p.gety()]).isCovered() == false){ 
 						p.move(-1,0);
 						}
 					} else if(spielfeld[p.getx()-1][p.gety()] instanceof Exitfeld){
 						if(((Exitfeld)spielfeld[p.getx()-1][p.gety()]).isCovered() == false){ 
 							p.move(-1,0);
 						}
 						   } else if ((spielfeld[p.getx()-1][p.gety()] instanceof Bombenfeld) && (p.getKicker())){
 							   			Bombe.Bombs.get(Bombe.getBomb(p.getx()-1, p.gety())).setkickedLeft();
 							   	  } else if (spielfeld[p.getx()-1][p.gety()] instanceof Fallenfeld) {
 							   		  		p.move(-1,0);
 							   	  		 }
 			p.setLeftMovement(false);			     
 			}
 			
 			if (Keyboard.isKeyDown(p.getKeyDown())){
 				p.setDownMovement(true);
 			}
 					 
 			if (p.isMovingDown()) {
 				if (spielfeld[p.getx()][p.gety()+1] instanceof Leerfeld | 
 					spielfeld[p.getx()][p.gety()+1] instanceof Explosionsfeld){
 						p.move(0,1);
 				} else if(spielfeld[p.getx()][p.gety()+1] instanceof Extrasfeld){
 					if (((Extrasfeld)spielfeld[p.getx()][p.gety()+1]).isCovered()==false){
 						p.move(0,1);
 					}
 					   } else if(spielfeld[p.getx()][p.gety()+1] instanceof Exitfeld){
 							if(((Exitfeld)spielfeld[p.getx()][p.gety()+1]).isCovered() == false){ 
 								p.move(0,1);
 							}
 							  } else if ((spielfeld[p.getx()][p.gety()+1] instanceof Bombenfeld) && (p.getKicker())){
 								  		Bombe.Bombs.get(Bombe.getBomb(p.getx(), p.gety()+1)).setkickedDown();
 								     } else if (spielfeld[p.getx()][p.gety()+1] instanceof Fallenfeld) {
 								    	 		p.move(0,1);
 								     		}
 			p.setDownMovement(false);				  	
 			}
 			
 			
 			if (Keyboard.isKeyDown(p.getKeyUp())){
 				p.setUpMovement(true);
 			}
 					 
 			if (p.isMovingUp()){
 				if (spielfeld[p.getx()][p.gety()-1] instanceof Leerfeld | 
 						spielfeld[p.getx()][p.gety()-1] instanceof Explosionsfeld){
 							p.move(0,-1);
 					} else if(spielfeld[p.getx()][p.gety()-1] instanceof Extrasfeld){
 						if (((Extrasfeld)spielfeld[p.getx()][p.gety()-1]).isCovered()==false){
 							p.move(0,-1);
 						}
 						   } else if(spielfeld[p.getx()][p.gety()-1] instanceof Exitfeld){
 								if(((Exitfeld)spielfeld[p.getx()][p.gety()-1]).isCovered() == false){ 
 									p.move(0,-1);
 								}
 								  } else if ((spielfeld[p.getx()][p.gety()-1] instanceof Bombenfeld) && (p.getKicker())){
 										  Bombe.Bombs.get(Bombe.getBomb(p.getx(), p.gety()-1)).setkickedUp();
 									  }  else if (spielfeld[p.getx()][p.gety()-1] instanceof Fallenfeld) {
 										  			p.move(0,-1);
 											  }
 				p.setUpMovement(false);	  	
 				}
 			
 			if (Keyboard.isKeyDown(p.getKeyBomb())){
 				p.setPlantingBomb(true);
 			}
 			
 			if (p.isPlantingBomb()) {
 				if((Bombe.getBombs(p.getName()) < p.getBombs()) && (!p.isPress_bomb()) && !(spielfeld[p.getx()][p.gety()] instanceof Bombenfeld)) {
 					new Bombe(p.getx(),p.gety(), p.getName(), p.getBombsRange()).start();
 					spielfeld[p.getx()][p.gety()]= new Bombenfeld();
 				}
 				p.setPress_bomb(true);
 				p.setPlantingBomb(false);
 			} else {
 				p.setPress_bomb(false);
 				p.setPlantingBomb(false);
 			  }
 			
 			
 			
 			
 			if (Keyboard.isKeyDown(p.getKeySpecial()) ){
 				p.setUsingSpecials(true);
 			}
 			
 			if (p.isUsingSpecials()) {
 				int item=p.getItem();
 				switch(item){
 				case 4: {p.setInvisible();p.delItem();break;}
 				case 5: {spielfeld[p.getx()][p.gety()]= new Fallenfeld(p.getName(),5);p.delItem();break;}
 				case 8: {spielfeld[p.getx()][p.gety()]= new Fallenfeld(p.getName(),8);p.delItem();break;}
 				case 9: {p.shield();p.delItem();break;}
 				}
 				p.setUsingSpecials(false);
 			}
 			
 		} //ende for
 		
 			
 		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
 			for(int i = 0; i < 4; i++){
 				Main.m.hauptButtons[i].setVisible(false);
 				if(i!=2){
 					Main.m.spielButtons[i].setVisible(true);
 				}
 				Main.m.gameover.setVisible(true);
 			}
 			Main.m.setVisible(true);
 			Keyboard.destroy();
 			try {
 				Keyboard.create();
 			} catch (LWJGLException e){
 				e.printStackTrace();
 			}
 			Main.t1.suspend();
 		}		
 		lock1.unlock();
 	}
 	 
 	
     public void run() {
 		GameTime.init();
     	Renderer.initDisplay(Feld.size*breit+100,Feld.size*hoch,60);
     	Renderer.initGL();
     	Renderer.setClearColor(1.0f, 1.0f, 1.0f, 1.0f); //white
     	
 		for(int i = 0; i < players.size(); i++) {
 			Player p = players.get(i);
 			
 			p.loadSprite("player.png");
     		p.setBombs(Main.m.getBombs());
     		
     		if(i == 0) {
     			p.setKeys(Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT, Keyboard.KEY_UP, Keyboard.KEY_DOWN, Keyboard.KEY_SPACE, Keyboard.KEY_RMENU);
     		} else {
     			p.setKeys(Keyboard.KEY_A, Keyboard.KEY_D, Keyboard.KEY_W, Keyboard.KEY_S, Keyboard.KEY_F, Keyboard.KEY_E);
     		}
 		}
     	
     	Menue.conti = true;
     	
     	//Hintergrund-Musik starten
     	//Renderer.Theme.playAsSoundEffect(1.0f, 1.0f, false);
    	if(netzwerk) {
	    	while(!Display.isCloseRequested() && (net.isConnected()==false)) {
	    		Renderer.clearGL();
	    		Renderer.print(5, 5, "waiting for connection...",0.5f);
	    		if(net.isHost()) net.pollConnect();
			    GameTime.update();
			    Renderer.sync();
	    	}
     	}
     	
         while (!Display.isCloseRequested() && playersAlive() && (Menue.conti == true)) {
         	// conti bezeichnet den Unterschied zwischen Spiel- und Hauptmenu
         	Renderer.clearGL();
         	
 			pollInput();
 		    
 		    for(int x = 0; x < spielfeld.length; x++) {
 		    	for(int y = 0; y < spielfeld[0].length; y++) {
 		    		spielfeld[x][y].draw(x, y);
 		    	}
 		    }
 		    
 		    
 			for(int i = 0; i < players.size(); i++) {
 				Player p = players.get(i);
 				if(p.isAlive()) p.draw();
 				if (p.getInvisible()==false){
 					Renderer.print((int)(p.getx()*Feld.getSize()),(int)(p.gety()*Feld.getSize()) - 6, p.getName(), 0.33f);
 				}
 			}
 			
 			if(players.size()==1) {
 				Renderer.print(5, 5, "Player 1 is " + (players.get(0).isAlive()?"alive":"dead") + " (" + players.get(0).getLives() + ")", 0.5f);
 			}
 			else {
 				Renderer.print(5, 5, "Player 1 is " + (players.get(0).isAlive()?"alive":"dead") + " (" + players.get(0).getLives() + ") , Player 2 is " + (players.get(1).isAlive()?"alive":"dead")  + " (" + players.get(1).getLives() + ") ... " + GameTime.getFPS(), 0.5f);
 			}
 			
 			if(netzwerk) {
 				Renderer.print(5, 25, "recv: " + net.recv(), 0.5f);
 				if(Keyboard.isKeyDown(Keyboard.KEY_1)) net.send(1);
 				if(Keyboard.isKeyDown(Keyboard.KEY_2)) net.send(2);
 			}
 			
 			SoundStore.get().poll(0);
 		    GameTime.update();
 		    Renderer.sync();
 		}   
         
         if(netzwerk) {
         	net.destroy();
         }
         
         for(int i = 0; i < players.size(); i++) {
         	if(players.get(i).isAlive() == false) Main.m.gameover.setText("Spieler "+players.get(i).getName()+" ist tot!" + " (" + players.get(0).getLives() + ")");
         }
         
         players.clear();
         
         try {
 			TimeUnit.SECONDS.sleep(1);
 		} catch (InterruptedException e) {
 			
 			e.printStackTrace();
 		}    
         for(int i = 0; i < 4; i++){
 			Main.m.hauptButtons[i].setVisible(true);
 			Main.m.gameover.setVisible(true);
 			Menue.conti = false;
 			Main.m.Ton(-1);
 		}
 		Main.m.setVisible(true);
 		Keyboard.destroy(); //Keyboard wird zerstört...
 		try {
 			Keyboard.create(); // ...und hier wieder neu erstellt.
 		} catch (LWJGLException e){
 			e.printStackTrace();
 		}
 		Main.m.Ton(2);
 		
 		//Renderer.Theme.stop();//Hintergrund-Musik stopppen
 		Renderer.destroy();
     }
     
 /*    public boolean playersAlive() {
     	
 		if(players.size() <= 0) return false;
 		
 		for(int i = 0; i < players.size(); i++) {
 			Player p = players.get(i);
 			if(p.isAlive() && p.getLives()!=0) {return true;}
 			else{Main.m.gameover.setText("Spieler "+(i+1)+" ist tot!");}
 		}
     	return false;
     } */
     /**
      * Sterbebedingungen fuer Einzel- und Mehrspieler
      * @param p: Uebergabe des zu ueberpruefenden Spielers
      * @return: true, falls betreffender Spieler lebt; false, falls betreffender Spieler tot
      */
     public boolean playersAlive(){
     	for(int i = 0; i < players.size(); i++) {
     		if(players.get(i).isAlive() == false) return false;
     	}	
     	return true;
     }
     /**
      *  Aktueller Spieler
      * @param playername: Name des Spielers
      * @return: Aktueller Spieler
      */
 	public static Player getPlayer(int num) {
 		/*Player personOfInterest= new Player("test",0,0);
 		for(int i = 0; i < playersStatic.size(); i++){
 			if(playersStatic.get(i).getName()==playername){
 				personOfInterest=playersStatic.get(i);
 			}
 			
 	} */
 		return players.get(num);
 	}
 	
 	public static boolean isMultiplayer() {
 		return (players.size()==2);
 	}
 
 /**
  * Laed das gewuenschte Level
  * @param name: Uebergabe des Levelnamens
  * @throws FileNotFoundException
  * @throws XMLStreamException
  */
 	protected void initialfeld(String name, int spielerzahl) throws FileNotFoundException, XMLStreamException{
 		InputStream in = new FileInputStream(name);
 		XMLInputFactory factory = XMLInputFactory.newInstance();
 		XMLStreamReader parser = factory.createXMLStreamReader(in);	
 		
 		int y = -1;		//y++ somit y=0 beim ersten Attribut
 		
 		while( parser.hasNext() ) {
 		    int event = parser.next();
 		   
 		    switch (event) {
 		        case XMLStreamConstants.END_DOCUMENT:
 		            parser.close();
 		            break;
 		        case XMLStreamConstants.START_ELEMENT:
 		        	if(parser.getLocalName().equals("Groesse")){
 		        		breit= Integer.parseInt(parser.getAttributeValue(0));
 		        		hoch= Integer.parseInt(parser.getAttributeValue(1));
 		        		spielfeld= new Feld[breit][hoch];
 		        	}
 		        	else if(parser.getLocalName().equals("Startpositionen")){
 		        		int x1 = Integer.parseInt(parser.getAttributeValue(0));
 		        		int y1 = Integer.parseInt(parser.getAttributeValue(1));
 		        		int x2 = Integer.parseInt(parser.getAttributeValue(2));
 		        		int y2 = Integer.parseInt(parser.getAttributeValue(3));
 		        		
 		        		if(spielerzahl==1) {
 		        			players.add(new Player("One",x1,y1));
 		        			//playersStatic.add(players.get(0));
 		        		}
 		        		if(spielerzahl==2) {
 		        			players.add(new Player("One",x1,y1));
 		        			players.add(new Player("Two",x2,y2));
 		        			//playersStatic.add(players.get(0));
 		        			//playersStatic.add(players.get(1));
 		        		}
 		        	} else if(parser.getLocalName().startsWith("Zeile")){
 		        		y++;
 		        		for( int x = 0; x < parser.getAttributeCount(); x++ ){
 		        			String test = parser.getAttributeValue(x);
 		            	
 		            		if (test.equals("S")){spielfeld[x][y]= new Steinfeld();}
 		            		else if (test.equals("M")){spielfeld[x][y]= new Mauerfeld();}
 		            		else if (test.equals("L")){spielfeld[x][y]= new Leerfeld();}
 		        		}//for
 		        	} else if(parser.getLocalName().equals("Ausgang") && (spielerzahl == 1)){	//Im Einspieler-Modus wird der Ausgang erstellt
 		        		spielfeld[Integer.parseInt(parser.getAttributeValue(0))][Integer.parseInt(parser.getAttributeValue(1))]=new Exitfeld();
 		        	}//if
 		            break;
 		        
 		       
 		        default:
 		            break;
 		    }
 		}
 		
 		
 
 		
 		int a = 1;
 		int b = 1;
 		
 		/*Ausgangsfeld mit zufaelligen Variablen
 		do {
 			a = (int) ((Math.random()*100)%(breit-2))+1;
 			b = (int) ((Math.random()*100)%(hoch-2))+1;
 		}
 		while(spielfeld[a][b] instanceof Steinfeld);
 
 		spielfeld[a][b]=new Exitfeld();
 		*/
 		//rangeTest
 		 //a = 3;
 		 //b = 4;
 		 //spielfeld[a][b]=new Extrasfeld(10);
 		
 		//Powerups generieren
 		int powerzahl=breit*hoch/12; //Anzahl Powerups je nach map einstellen?
 		int art=0;	//Art der Powerups
 		int n=0; 
 		
 		for (int i=0; i<powerzahl;i++){
 		do {
 			a = (int) ((Math.random()*100)%(breit-2))+1;
 			b = (int) ((Math.random()*100)%(hoch-2))+1;
 			n++;//falls es keine Mauerfelder gibt, gibts auch keine extras
 		}
 		while((spielfeld[a][b] instanceof Steinfeld || spielfeld[a][b] instanceof Exitfeld || spielfeld[a][b] instanceof Extrasfeld || spielfeld[a][b] instanceof Leerfeld) && n<100);
 		if (n<100){
 			int zahl= (int) (Math.random()*100)+1;
 			if(zahl>=1 && zahl<20) art=2;
 			if(zahl>=20 && zahl<40) art=10;
 			if(zahl>=40 && zahl<50) art=1;
 			if(zahl>=50 && zahl<60) art=3;
 			if(zahl>=60 && zahl<70) art=9;
 			if(zahl>=70 && zahl<80) art=8;
 			if(zahl>=80 && zahl<85) art=4;
 			if(zahl>=85 && zahl<90) art=5;
 			if(zahl>=90 && zahl<95) art=6;
 			if(zahl>=95 && zahl<=100) art=7;
 			spielfeld[a][b]=new Extrasfeld(art);
 			}
 		}//for
 		
 		/*
 		int art = 1;
 		arr = new boolean[13];
 		for(int i=0;i<arr.length;i++)
 		{
 			arr[i] = true;
 		}
 		
 		if(Main.m.getPowerUps())
 		{
 			for(int i=0;i<5;i++)
 			{
 				int zahl = (int)((Math.random()*100)%100)+1;
 				
 					if(zahl>=1 && zahl<10 && arr[0] == true)
 					{ 	art = 1;
 						arr[0] = false;
 					}
 					//if(arr[0] == false) i--;
 					
 					if(zahl>=10 && zahl<26 && arr[1] == true)
 					{ 	art = 2;
 					arr[1] = false;
 					}
 					//if(arr[1] == false) i--;
 					
 					if(zahl>=26 && zahl<35 && arr[2] == true)
 					{ 	art = 3;
 					arr[2] = false;
 					}
 					//if(arr[2] == false) i--;
 					
 					if(zahl>=35 && zahl<40 && arr[3] == true)
 					{ 	art = 4;
 					arr[3] = false;
 					}
 					//if(arr[3] == false) i--;
 					
 					if(zahl>=40 && zahl<49 && arr[4] == true){ art = 5;}
 					if(zahl>=49 && zahl<53 && arr[5] == true){ art = 6;}
 					if(zahl>=53 && zahl<62 && arr[6] == true){ art = 7;}
 					if(zahl>=62 && zahl<66 && arr[7] == true){ art = 8;}
 					if(zahl>=66 && zahl<70 && arr[8] == true){ art = 9;}
 					if(zahl>=70 && zahl<73 && arr[9] == true){ art = 10;}
 					if(zahl>=73 && zahl<77 && arr[10] == true){ art = 11;}
 					if(zahl>=77 && zahl<86 && arr[11] == true){ art = 12;}
 					if(zahl>=86 && zahl<100 && arr[12] == true){ art = 13;}
 				
 			}
 		}
 		// art = Algorithmus... For-Schleife inklusive boolean um erneut zu generieren um mehrfach Extrafeld zu vermeiden
 		spielfeld[x][y]=new Extrasfeld(art);*/
 		
 	}
 	
 	
 }//end class
