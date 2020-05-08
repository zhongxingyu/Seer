 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.game.Layer;
 import javax.microedition.lcdui.game.Sprite;
 import javax.microedition.lcdui.game.TiledLayer;
 
 public class Player {
 	//Direcciones
 	public static final int DIRN 	= 0; //Norte
 	public static final int DIRNE 	= 1; //NorEste
 	public static final int DIRE 	= 2; //Este
 	public static final int DIRSE 	= 3; //SurEste
 	public static final int DIRS 	= 4; //Sur
 	public static final int DIRSO 	= 5; //SurOeste
 	public static final int DIRO 	= 6; //Oeste
 	public static final int DIRNO 	= 7; //NorOeste
 	
 	//identificador
 	public int identificador ;
 	public char[] nombre = new char[BEMIDlet.MAX_NAME_LENGTH]; 
 	
 	public Sprite s_player;
 	
 	//Posicion
 	private int xinicio;
 	private int yinicio;
 	public int x;
 	public int y;
 	public int width = 48;
 	public int height = 48;
 	public int dir; //señala donde esta mirando, para saber donde disparar
 	
 	private int step = 1; //cuanto se mueve por frame
 	
 	//TODO arreglar
 	public int dinero = 2000;
 	
 	public int idMoneda 	= -1; 
 
 	private int idAsesino = -1;
 	//Atributos
 	private int inicio_v 	= 10;
 	private int inicio_e 	= 100;
 	private int inicio_p 	= 10;
 	private int inicio_c 	= 100;
 	
 	public DisparoJugador disparo = null;
 	public int velocidad;
 	public int escudo;
 	public int potencia;
 	public int cristales;
 	
	public int vidas 		= 3;
 	public int puntos 		= 0;
 	
 	public boolean estaVivo = false;
 	public boolean puedeDisparar = true;
 	//cuanto sale cada mejora
 	private int mejoraVelocidad 	= 10;
 	private int mejoraEscudo 		= 5;
 	private int mejoraPotencia 		= 10;
 	private int mejoraCristales 	= 1;
 	
 	public Juego juego;
 	
 	public Player(Juego _j, int _id, int _xinicio, int _yinicio, String _nombre){
 		
 		this.juego = _j;
 		this.identificador = _id;
 		this.xinicio = _xinicio;
 		this.yinicio = _yinicio;
 		this.x = _xinicio;
 		this.y = _yinicio;
 		this.disparo = new DisparoJugador(juego, this);
 		
 		_nombre.getChars(0, _nombre.length(), this.nombre, 0);
 		setInicio(); //setea valores por defecto
 		s_player = new Sprite(juego.im.getImgNavePlayer(),48,48);
 		
 		s_player.setRefPixelPosition(24, 24); //centra las coordenadas
 		s_player.setPosition(this.x, this.y);
 		cambiarFrame();
 		estaVivo = true;
 	}
 	
 	
 	public void dibujar(Graphics g){
 		this.s_player.paint(g);
 		this.disparo.dibujar(g);
 	}
 	
 	public String generarMensaje(){
 		char life;
 		if(estaVivo | escudo == -1) 	life = 'V';
 		else			life = 'M';
 		
 		MessageFromPlayer mfp = 
 			new MessageFromPlayer(identificador, nombre, escudo, x, y, dir,
 								  life, disparo.potencia, disparo.x, disparo.y, idMoneda, idAsesino, (int) 1000/juego.milisegundosEnDibujar);
 		//lo revivo DESPUES de mandar el mensaje
 		if(estaVivo == false){
 			setInicio();
 		}
 		return mfp.getMsg();
 	}
 	
 	public void setInicio(){
 		this.x 			= this.xinicio;
 		this.y 			= this.yinicio;
 		this.dir	    = Player.DIRN;
 		
 		if(vidas > 0){
 			this.escudo 	= this.inicio_e;
 			this.estaVivo		= true;
 			this.puedeDisparar = true;
 		}
 		else{
 			this.escudo 	= -1;
 			this.estaVivo = false;
 			this.s_player.setVisible(false);
 			//juego.lm.remove(this.s_player); //no lo dibujo mas
 			this.puedeDisparar = false; //ya no puede disparar
 		}
 		this.velocidad 	= this.inicio_v;
 		this.potencia 	= this.inicio_p;
 		this.cristales 	= this.inicio_c;
 		this.idMoneda = -1;
 		this.idAsesino = -1;
 	}
 	
 	//retorna el escudo que le queda o -1 si lo destruyo y se regenero en otro lado
 	public int recibirDisparo(DisparoEnemigo _de){
 		int _potenciaDisparo = _de.potencia;
 		escudo = escudo - _potenciaDisparo;
 		if(escudo <= 0){ 			
 			idAsesino = _de.id;
 			estaVivo = false;
 			decrementarVida();
 			this.escudo = 0;
 			return -1; 		//escudo destruido 
 		}
 		return escudo;		//escudo restante
 	}
 	
 	
 	
 	public void actualizar(int _x, int _y, int _dir){
 		this.x = _x;
 		this.y = _y;
 		this.dir = _dir;
 		cambiarFrame();
 		
 	}
 	
 	public void moverDisparo(){
 		this.disparo.mover();
 	}
 
 
 	private void cambiarFrame(){
 		switch(this.dir){
 		case Player.DIRN:{
 			this.s_player.setFrame(0);
 			break;
 		}
 		case Player.DIRNE:{
 			this.s_player.setFrame(1);
 			break;
 		}
 		case Player.DIRE:{
 			this.s_player.setFrame(2);
 			break;
 		}
 		case Player.DIRSE:{
 			this.s_player.setFrame(3);
 			break;
 		}
 		case Player.DIRS:{
 			this.s_player.setFrame(4);
 			break;
 		}
 		case Player.DIRSO:{
 			this.s_player.setFrame(5);
 			break;
 		}
 		case Player.DIRO:{
 			this.s_player.setFrame(6);
 			break;
 		}
 		case Player.DIRNO:{
 			this.s_player.setFrame(7);
 			break;
 		}
 		}
 	}
 
 	public void incrementarMoneda(int _valor){
 		dinero = dinero + _valor;
 	}
 	
 	public int decrementarVida(){
 		vidas = vidas - 1;
 		return vidas;
 	}
 	
 	
 	
 	public void setDisparar(boolean _d){
 		this.puedeDisparar = _d;
 	}
 	public void incrementarPuntos(){
 		puntos = puntos + 1;
 	}
 	
 	
 	
 	//no se esta usando, no anda bien
 	public boolean colisionaConMapaLevel2(){
 		int mapax = (int) this.x/Mapa.TILESIZE;
 		int mapay = (int) this.y/Mapa.TILESIZE;
 		int mapindex = mapax*Mapa.MAPSIDE + mapay;
 		return juego.mapa.colisionPlayer(mapindex);
 	}
 	
 	//funciones de interaccion
 	public void mover(int _direccion){
 		dir = _direccion;
 		
 		//TODO fix
 		
 		int prevx = this.x;
 		int prevy = this.y;
 		
 		//TODO revisar
 		step = velocidad;
 		switch(dir){
 			case Player.DIRN:{
 				this.y = this.y - step;
 				break;
 			}
 			case Player.DIRNE:{
 				this.x = this.x + step;
 				this.y = this.y - step;
 				break;
 			}
 			case Player.DIRE:{
 				this.x = this.x + step;
 				break;
 			}
 			case Player.DIRSE:{
 				this.x = this.x + step;
 				this.y = this.y + step;
 				break;
 			}
 			case Player.DIRS:{
 				this.y = this.y + step;
 				break;
 			}
 			case Player.DIRSO:{
 				this.x = this.x - step;
 				this.y = this.y + step;
 				break;
 			}
 			case Player.DIRO:{
 				this.x = this.x - step;
 				break;
 			}
 			case Player.DIRNO:{
 				this.x = this.x - step;
 				this.y = this.y - step;
 				break;
 			}
 		}
 		/*
 		//THIS IS HORRIBLE, STAY AWAY
 		Sprite l_splayer   = (Sprite) juego.lm.getLayerAt(juego.lm.getSize()-3);
 		l_splayer.setPosition(this.x,this.y);
 		if( l_splayer.collidesWith(
 				((TiledLayer) juego.lm.getLayerAt(juego.lm.getSize()-2)),
 				true) ){
 			this.x = prevx;
 			this.y = prevy;
 			return; //si colisiona, no debo moverlo
 		}
 		//if(l_splayer.collidesWith(l_back2, false)){
 			
 		//}*/
 		/*if(this.x != this.xinicio)
 			if( this.s_player.collidesWith(juego.mapa.backgroundL2, true)){
 				return;
 			}*/
 		this.s_player.setPosition(this.x,this.y);
 		cambiarFrame();
 	}
 	
 	public boolean disparar(){
 		if(this.puedeDisparar){
 			this.disparo.set(this.x,this.y,this.dir,this.potencia,this.cristales);
 			setDisparar(false); //ahora no puede disparar
 			return true;
 		}
 		return false;
 		
 	}
 	
 	public boolean colisionar(DisparoEnemigo de){
 		return (this.s_player.collidesWith(de.s_disparoenemigo,true));
 	}
 	
 	public boolean colisionar(Moneda m){
 		return (this.s_player.collidesWith(m.s_moneda, true));
 	}
 	
 	//funciones que aumentan los stats
 	public boolean aumentarVelocidad(){
 		if (dinero-costoVelocidad() >= 0) {
 			dinero 		= dinero - costoVelocidad();
 			velocidad 	= velocidad + 2;
 			inicio_v	= velocidad;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public int costoVelocidad(){ return (mejoraVelocidad * velocidad); 	}
 	
 	public boolean aumentarEscudo(){
 		if (dinero-costoEscudo() >= 0) {
 			dinero 		= dinero - costoEscudo();
 			escudo		= (int) ( (double) escudo * 1.1);
 			inicio_e	= escudo;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public int costoEscudo(){return (mejoraEscudo * escudo); }
 	
 	public boolean aumentarPotencia(){
 		if (dinero-costoPotencia() >= 0) {
 			dinero 		= dinero - costoPotencia();
 			potencia	= (int) ( (double) potencia * 1.1); //agrega 10%
 			inicio_p	= potencia;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public int costoPotencia(){	return (mejoraPotencia * potencia);	}
 	
 	public boolean aumentarCristales(){
 		if (dinero-costoCristales() >= 0) {
 			dinero 		= dinero - costoCristales();
 			cristales	= (int) ( (double) cristales * 1.2); //agrega 20%
 			inicio_c	= cristales;
 			juego.actualizarCristales();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public int costoCristales(){ return (mejoraCristales * cristales);	}
 	
 }
