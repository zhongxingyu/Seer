 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import java.io.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.TimeUnit;
 
 public class Game implements Runnable {
 
 	public static Feld[][] spielfeld;
 	public Player one;
 	public static boolean bombeVerwendbar = true;
 	static Lock lock1 = new ReentrantLock();
 	
 	public Game() {
 		try {
 			ObjectInputStream o = new ObjectInputStream(new FileInputStream("default.map")); //hier muss der Pfad der default.map angegeben werden sonst Error
 			spielfeld = (Feld[][]) o.readObject();
 			o.close();
 		} 
 		catch (IOException i) {
 			i.printStackTrace();
 		} 
 		catch (ClassNotFoundException e) {
 			//Fehlerbehandlung "Level nicht gefunden"
 			e.printStackTrace();
 		} 
 		
 		one = new Player("One",1,1);
 		spielfeld[13][11]=new Exitfeld();//Ausgang f�r Pr�sentation
 		GameTime.init();
 	}
 	
 	public void pollInput(){
 		lock1.lock();
 		if (spielfeld[one.getx()][one.gety()] instanceof Explosionsfeld | spielfeld[one.getx()][one.gety()] instanceof Exitfeld) {one.die();};
 		
 				 
 		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
 			if (spielfeld[one.getx()+1][one.gety()] instanceof Leerfeld | spielfeld[one.getx()+1][one.gety()] instanceof Exitfeld){
 			
 				 one.move(1,0);
 			}
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 			if (spielfeld[one.getx()-1][one.gety()] instanceof Leerfeld | spielfeld[one.getx()-1][one.gety()] instanceof Exitfeld){
 			
 				one.move(-1,0);
 			}
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
 			if (spielfeld[one.getx()][one.gety()+1] instanceof Leerfeld | spielfeld[one.getx()][one.gety()+1] instanceof Exitfeld){
 			 
 				one.move(0,1);
 			}
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
 			if (spielfeld[one.getx()][one.gety()-1] instanceof Leerfeld | spielfeld[one.getx()][one.gety()-1] instanceof Exitfeld){
 				one.move(0,-1);
 			}
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && bombeVerwendbar == true) {
 			new Bombe(one.getx(),one.gety()).start();
 			bombeVerwendbar = false;
 			spielfeld[one.getx()][one.gety()]= new Bombenfeld();
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
 			for(int i=0;i<4;i++){
 				Main.m.hauptButtons[i].setVisible(false);
 				Main.m.spielButtons[i].setVisible(true);
 			}
 			Main.m.setVisible(true);
 			Keyboard.destroy(); //Ab hier neu. Keyboard wird zerstört...
 			try {
 				Keyboard.create(); // ...und hier wieder neu erstellt.
 			} catch (LWJGLException e){
 				e.printStackTrace();
 			}
 			Main.t1.suspend();
 		}
 			lock1.unlock();
 	}
 	
 	static class Bombe extends Thread{
 		private int x;
 		private int y;
 		
 		public Bombe(int a, int b){
 			x=a;
 			y=b;
 		}
 		
 	private void explosion(int x, int y){
 		spielfeld[x][y]= new Explosionsfeld();
 		for (int i=1;i<4;i++){		//Explosion nach links
 			if (spielfeld[x-i][y] instanceof Leerfeld | spielfeld[x-i][y] instanceof Explosionsfeld) {spielfeld[x-i][y]= new Explosionsfeld();}
 			else {break;}
 		}
 		for (int i=1;i<4;i++){		//Explosion nach rechts
 			if (spielfeld[x+i][y] instanceof Leerfeld | spielfeld[x+i][y] instanceof Explosionsfeld) {spielfeld[x+i][y]= new Explosionsfeld();}
 			else {break;}
 		}
 		for (int i=1;i<4;i++){		//Explosion nach oben
 			if (spielfeld[x][y-i] instanceof Leerfeld | spielfeld[x][y-i] instanceof Explosionsfeld) {spielfeld[x][y-i]= new Explosionsfeld();}
 			else {break;}
 		}
 		for (int i=1;i<4;i++){		//Explosion nach unten
 			if (spielfeld[x][y+i] instanceof Leerfeld | spielfeld[x][y+i] instanceof Explosionsfeld) {spielfeld[x][y+i]= new Explosionsfeld();}
 			else {break;}
 		}
 		bombeVerwendbar = true;
 	}
 		
 
 	private void clean(int x, int y){ //macht aus den Explosionsfeldern Leerfelder
 		spielfeld[x][y]=new Leerfeld();
 		for (int i=1;i<4;i++){		//links
 			if (spielfeld[x-i][y] instanceof Explosionsfeld | spielfeld[x-i][y] instanceof Leerfeld) {spielfeld[x-i][y]= new Leerfeld();}
 			else {break;}
 		}
 		for (int i=1;i<4;i++){		//rechts
 			if (spielfeld[x+i][y] instanceof Explosionsfeld | spielfeld[x+i][y] instanceof Leerfeld) {spielfeld[x+i][y]= new Leerfeld();}
 			else {break;}
 		}
 		for (int i=1;i<4;i++){		//oben
 			if (spielfeld[x][y-i] instanceof Explosionsfeld | spielfeld[x][y-i] instanceof Leerfeld) {spielfeld[x][y-i]= new Leerfeld();}
 			else {break;}
 		}
 		for (int i=1;i<4;i++){		//unten
 			if (spielfeld[x][y+i] instanceof Explosionsfeld | spielfeld[x][y+i] instanceof Leerfeld) {spielfeld[x][y+i]= new Leerfeld();}
 			else {break;}
 		}
 	}
 		public void run(){
 			try{
 			TimeUnit.SECONDS.sleep(3);
 			} catch(InterruptedException e){};
 			lock1.lock();
 			explosion(x,y);
 			lock1.unlock();
 			try {
 				TimeUnit.SECONDS.sleep(1);
 			} catch (InterruptedException e) {}
 			lock1.lock();
 			clean(x,y);
 			lock1.unlock();
 		}
 	}
 
 	
 	
 	
 	
     public void run() {
     	
     	Renderer.initDisplay(800,600,60);
     	Renderer.initGL();
     	Renderer.setClearColor(1.0f, 1.0f, 1.0f, 1.0f); //white
     	
     	one.loadSprite("player.png"); // kann erst nach initGL benutzt werden, alternativ initGL usw mit im konstruktor?
     	Menue.conti = true;
         while (!Display.isCloseRequested() && one.isAlive() && Menue.conti == true) {
<<<<<<< HEAD
         	// conti bezeichnet den Unterschied zwischen Spiel- und Hauptmenu
=======
        	
>>>>>>> d5be4a20f6f2d641fca711effa874842a7ae5089
         	Renderer.clearGL();
         	
 
 				pollInput();
 		    
 		    for(int x = 0; x < spielfeld.length; x++) {
 		    	for(int y = 0; y < spielfeld[0].length; y++) {
 		    		spielfeld[x][y].draw(x, y);
 		    	}
 		    }
 		    
 		    one.draw();
 		    
 		    GameTime.update();
 		    Renderer.sync();
 		}
 	    //ggf. Game-Over Behandlung    
         try {
 			TimeUnit.SECONDS.sleep(1);
 		} catch (InterruptedException e) {
 			
 			e.printStackTrace();
 		}    
         for(int i=0;i<4;i++){
 			Main.m.hauptButtons[i].setVisible(true);
 			
 		}
 		Main.m.setVisible(true);
 		Keyboard.destroy(); //Ab hier neu. Keyboard wird zerstört...
 		try {
 			Keyboard.create(); // ...und hier wieder neu erstellt.
 		} catch (LWJGLException e){
 			e.printStackTrace();
 		}
 		Renderer.destroy();
     }
     public static void destroy()
     {
     	Renderer.destroy();
     }
 }
 
