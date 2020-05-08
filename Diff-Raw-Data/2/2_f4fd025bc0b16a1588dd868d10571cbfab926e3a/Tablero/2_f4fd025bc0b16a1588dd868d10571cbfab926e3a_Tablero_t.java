 package main.model.tablero;
 
 import main.model.naves.Nave;
 import main.model.naves.Parte;
 import fiuba.algo3.titiritero.modelo.ObjetoPosicionable;
 
 /**
  * Representa el Tablero de juego.
  * 
  * @author daniel.pilla
  */
 public class Tablero implements ObjetoPosicionable{
 
 	/*
 	 * Declaracion de constantes
 	 */
 	private final int FILAS_TABLERO = 10;
 	private final int COLUMNAS_TABLERO = 10;
 	private static Tablero tablero;
 
 	/**
 	 * Matriz de Casilleros.
 	 */
 	private Casillero[][] casilleros;
 
 	/**
 	 * Constructor por defecto. Instancia un Tablero con sus Casilleros y Coordenadas.
 	 */
 	private Tablero () {
 		casilleros = new Casillero[FILAS_TABLERO][COLUMNAS_TABLERO];
 		crearCoordenadas();
 	}
 
 	/**
 	 * Crea y asigna las coordenadas a los Casilleros del tablero.
 	 */
 	private void crearCoordenadas() {
 		for (int fila = 0; fila < FILAS_TABLERO; fila++) {
 			for(int columna = 0; columna < COLUMNAS_TABLERO; columna++) {
 				Coordenada coordenada = new Coordenada(fila, columna);
 				casilleros[fila][columna] = new Casillero(coordenada);
 			}
 		}
 	}
 
 	/**
 	 * Devuelve True si el Tablero tiene barcos aun sin destruir.
 	 * 
 	 * @return boolean
 	 */
 	public boolean tieneBarcosNoDestruidos(){
 		for (int fila = 0; fila < FILAS_TABLERO; fila++) {
 			for(int columna = 0; columna < COLUMNAS_TABLERO; columna++) {
 				if(!casilleros[fila][columna].tieneBarcosNoDestruidos()) return true; 
 			}
 		}
 		return false;
 	}
 	
 	public static Tablero getTablero() {
		if (tablero == null) {
 			tablero = new Tablero();
 		}
 		return tablero;
 	}
 	
 	public void removerNave (Nave nave) {
 		Parte parte = nave.getPartes().get(0);
 		casilleros[parte.getPosicion().getY()][parte.getPosicion().getX()].removerNave(nave);
 	}
 	
 	public void reubicarNave (Nave nave) {
 		Parte parte = nave.getPartes().get(0);
 		casilleros[parte.getPosicion().getY()][parte.getPosicion().getX()].agregarNave(nave);
 	}
 	
 	/**
 	 * Devuelve la matriz de Casilleros del Tablero.
 	 *
 	 * @return casilleros Matriz de Casilleros.
 	 */
 	public Casillero[][] getCasilleros() {
 		return casilleros;
 	}
 
 	@Override
 	public int getX() {
 		return 0;
 	}
 
 	@Override
 	public int getY() {
 		return 0;
 	}
 }
