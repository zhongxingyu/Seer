 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.game.Sprite;
 
 public class DisparoJugador {
 	//posicion inicial
 	public int xinicial;
 	public int yinicial;
 	//posicion
 	public int x;
 	public int y;
 	//pendiente
 	public int mx; 
 	public int my;
 	public int dir;
 	//potencia del disparador
 	public int potencia;
 	//distancia que puede recorrer
 	public int distancia;  
 	//owner
 	public Player jugador;
 	public Sprite s_disparo;
 	private boolean existe = false;
 	private int step = 3;
 	private Juego juego = null;
 	public DisparoJugador(Juego _j, Player _duenio){ //constructor por defecto
 		this.juego = _j;
 		this.jugador = _duenio;
 		this.x = 0;
 		this.y = 0;
 		
 		this.potencia = DisparoEnemigo.VALORESPECIAL;
 		s_disparo = new Sprite(juego.im.getImgDisparo());
 		s_disparo.setVisible(false);
 		s_disparo.setRefPixelPosition(16, 16);
 	}
 	
 	
 	public void set(int _x, int _y, int _dir, int _potencia, int _distancia){
 		this.xinicial = _x;//correccion por sprite
 		this.yinicial = _y;
 		this.x = _x; 
 		this.y = _y; 
 		this.dir = _dir;
 		this.distancia = _distancia;
 		switch(dir){
 			case Player.DIRN:{
 				this.mx =  0;
 				this.my = -1;
 				break;
 			}
 			case Player.DIRNE:{
 				this.mx =  1;
 				this.my = -1;
 				break;
 			}
 			case Player.DIRE:{
 				this.mx =  1;
 				this.my =  0;
 				break;
 			}
 			case Player.DIRSE:{
 				this.mx =  1;
 				this.my =  1;
 				break;
 			}
 			case Player.DIRS:{
 				this.mx =  0;
 				this.my =  1;
 				break;
 			}
 			case Player.DIRSO:{
 				this.mx = -1;
 				this.my =  1;
 				break;
 			}
 			case Player.DIRO:{
 				this.mx = -1;
 				this.my =  0;
 				break;
 			}
 			case Player.DIRNO:{
 				this.mx = -1;
 				this.my = -1;
 				break;
 			}
 		}
 		s_disparo.setPosition(this.x,this.y);
 		s_disparo.setVisible(true);
 		this.potencia = _potencia;
 		this.existe   = true;
 	}
 	
 	public void mover(){
 		if(existe == true){
 			step = jugador.velocidad*2;
 			this.x = this.x + this.mx*step;
 			this.y = this.y + this.my*step;
 			double d = dist(this.xinicial,this.yinicial,this.x,this.y);
 			s_disparo.setPosition(this.x,this.y);
 			if(d >= this.distancia){
 				this.unSet();
 			}
 		}
 	}
 	
 	public void unSet(){
 		this.existe = false;
		this.x = -1;
		this.y = -1;
 		this.potencia = -1;
 		s_disparo.setVisible(false);
 		jugador.setDisparar(true);
 	}
 	
 	public static double dist(int x0, int y0, int x1, int y1){
 		return Math.sqrt( (x1-x0)*(x1-x0) + (y1-y0)*(y1-y0));
 	}
 	
 	public void dibujar(Graphics g){
 		if(existe == true){
 			s_disparo.paint(g);
 		}	
 	}
 }
