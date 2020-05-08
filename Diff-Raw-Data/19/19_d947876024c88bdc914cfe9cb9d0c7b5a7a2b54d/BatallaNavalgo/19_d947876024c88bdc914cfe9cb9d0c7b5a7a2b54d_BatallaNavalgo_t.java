 package main.juego;
 
 import main.model.naves.Buque;
 import main.model.naves.Destructor;
 import main.model.naves.EnumDirecciones.DireccionMovimiento;
 import main.model.naves.EnumDirecciones.DireccionSentido;
 import main.model.naves.Lancha;
import main.model.naves.Nave;
 import main.model.naves.Portaaviones;
 import main.model.naves.RompeHielos;
 import main.model.tablero.Coordenada;
 import main.model.tablero.Tablero;
 
 /**
  * Clase principal del Juego BatallaNavalgo.
 * Se encarga de manejar la l�gica del juego.
  * 
  * @author daniel.pilla
  */
 public class BatallaNavalgo {
 
 	/*
 	 * Definicion de constantes.
 	 */
 	private final static Integer CANT_LANCHAS = 2;
 	private final static Integer CANT_DESTRUCTORES = 2;
 	private final static Integer CANT_BUQUES = 1;
 	private final static Integer CANT_PORTA_AVIONES = 1;
 	private final static Integer CANT_ROMPE_HIELOS = 1;
 	
 	private final static DireccionSentido sentidosNave[] = 
 		{DireccionSentido.HORIZONTAL, DireccionSentido.VERTICAL};
 	private final static DireccionMovimiento movimientosNave[] =
 		{DireccionMovimiento.ESTE, DireccionMovimiento.NORESTE, 
 		DireccionMovimiento.NOROESTE, DireccionMovimiento.NORTE,
 		DireccionMovimiento.OESTE, DireccionMovimiento.SUR,
 		DireccionMovimiento.SURESTE, DireccionMovimiento.SUROESTE};
 
 	/**
 	 * Metodo principal del Programa. Inicia un Tablero, un Jugador,
 	 * coloca los barcos de la maquina y controla el flujo de los turnos del juego.
 	 * 
 	 * @param args Array de Strings con atributos recibidos por consola.
 	 */
 	public static void main(String[] args) {
 		Tablero tablero = new Tablero();
 		colocarBarcosEnTablero(tablero);
 	}
 
 	/**
 	 * Crea y coloca todos las naves en el Tablero.
 	 * 
 	 * @param tablero El Tablero sobre el cual se van a colocar las naves. No puede ser nulo.
 	 */
 	private static void colocarBarcosEnTablero(Tablero tablero) {
 		colocarLanchas(tablero);
 		colocarDestructores(tablero);
 		colocarBuques(tablero);
 		colocarPortaAviones(tablero);
 		colocarRompeHielos(tablero);
 	}
 
 	/**
 	 * Crea y coloca todas las lanchas en el Tablero.
 	 * 
 	 * @param tablero El tablero sobre el cual se van a colocar las lanchas. No puede ser nulo.
 	 */
 	private static void colocarLanchas(Tablero tablero) {
 		for (int i = 0; i < CANT_LANCHAS; i++) {
 			Coordenada coordenada = crearCoordenada();
 			DireccionSentido sentido = getSentidoRandom();
 			DireccionMovimiento movimiento = getMovimientoRandom();
			Nave lancha = new Lancha(coordenada, sentido, movimiento);
 			tablero.getCasilleros()[coordenada.getX()][coordenada.getY()]
 				.agregarNave(lancha);
 		}
 	}
 
 	/**
 	 * Crea y coloca todos los destructores en el Tablero.
 	 * 
 	 * @param tablero El tablero sobre el cual se van a colocar los destructores. No puede ser nulo.
 	 */
 	private static void colocarDestructores(Tablero tablero) {
 		for (int i = 0; i < CANT_DESTRUCTORES; i++) {
 			Coordenada coordenada = crearCoordenada();
 			DireccionSentido sentido = getSentidoRandom();
 			DireccionMovimiento movimiento = getMovimientoRandom();
			Nave destructor = new Destructor(coordenada,
 				sentido, movimiento);
 			tablero.getCasilleros()[coordenada.getX()][coordenada.getY()]
 				.agregarNave(destructor);
 		}
 	}
 
 	/**
 	 * Crea y coloca todos los buques en el Tablero.
 	 * 
 	 * @param tablero El tablero sobre el cual se van a colocar los buques. No puede ser nulo.
 	 */
 	private static void colocarBuques(Tablero tablero) {
 		for (int i = 0; i < CANT_BUQUES; i++) {
 			Coordenada coordenada = crearCoordenada();
 			DireccionSentido sentido = getSentidoRandom();
 			DireccionMovimiento movimiento = getMovimientoRandom();
			Nave buque = new Buque(coordenada, sentido, movimiento);
 			tablero.getCasilleros()[coordenada.getX()][coordenada.getY()]
 				.agregarNave(buque);
 		}
 	}
 
 	/**
 	 * Crea y coloca todos los porta aviones en el Tablero.
 	 * 
 	 * @param tablero El tablero sobre el cual se van a colocar los porta aviones. No puede ser nulo.
 	 */
 	private static void colocarPortaAviones(Tablero tablero) {
 		for (int i = 0; i < CANT_PORTA_AVIONES; i++) {
 			Coordenada coordenada = crearCoordenada();
 			DireccionSentido sentido = getSentidoRandom();
 			DireccionMovimiento movimiento = getMovimientoRandom();
			Nave portaAviones = new Portaaviones(coordenada,
 				sentido, movimiento);
 			tablero.getCasilleros()[coordenada.getX()][coordenada.getY()]
 					.agregarNave(portaAviones);
 		}
 	}
 
 	/**
 	 * Crea y coloca todos los rompe hielos en el Tablero.
 	 * 
 	 * @param tablero El tablero sobre el cual se van a colocar los rompe hielos. No puede ser nulo.
 	 */
 	private static void colocarRompeHielos(Tablero tablero) {
 		for (int i = 0; i < CANT_ROMPE_HIELOS; i++) {
 			Coordenada coordenada = crearCoordenada();
 			DireccionSentido sentido = getSentidoRandom();
 			DireccionMovimiento movimiento = getMovimientoRandom();
			Nave rompeHielos = new RompeHielos(coordenada,
 				sentido, movimiento);
 			tablero.getCasilleros()[coordenada.getX()][coordenada.getY()]
 				.agregarNave(rompeHielos);
 		}
 	}
 
 	/**
 	 * Crea una coordenada con valores aleatorios entre 0-9 de X e Y.
 	 * 
 	 * @return Coordenada La coordenada aleatoria donde se ubica el principio de una nave.
 	 */
 	private static Coordenada crearCoordenada() {
 		Integer fila = (int) (Math.random() * 10);
 		Integer columna = (int) (Math.random() * 10);
 		return new Coordenada(fila, columna);
 	}
 
 	/**
 	 * Devuelve el sentido de la nave aleatorio.
 	 * 
 	 * @return DireccionSentido Valor aleatorio de sentido de ubicacion de la nave.
 	 */
 	private static DireccionSentido getSentidoRandom() {
 		return sentidosNave[(int)(Math.random()*1)];
 	}
 
 	/**
 	 * Devuelve una direccion de movimiento aleatoria.
 	 * 
 	 * @return DireccionMovimiento Valor aleatorio de direccion de movimiento de la nave.
 	 */
 	private static DireccionMovimiento getMovimientoRandom() {
 		return movimientosNave[(int)(Math.random()*8)];
 	}
 }
