 package main.model.tablero;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import main.juego.MapsModeloVista;
 import main.model.disparos.Disparo;
 import main.model.naves.Nave;
 import main.model.naves.Parte;
 import main.view.juego.DibujablesList;
 import main.view.naves.VistaParteDaniada;
 import fiuba.algo3.titiritero.dibujables.Imagen;
 import fiuba.algo3.titiritero.modelo.ObjetoPosicionable;
 import fiuba.algo3.titiritero.modelo.ObjetoVivo;
 
 /**
  * Representa el Tablero de juego.
  * 
  * @author daniel.pilla
  */
 public class Tablero implements ObjetoVivo, ObjetoPosicionable{
 
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
 		casilleros = new Casillero[COLUMNAS_TABLERO][FILAS_TABLERO];
 		crearCoordenadas();
 	}
 
 	/**
 	 * Crea y asigna las coordenadas a los Casilleros del tablero.
 	 */
 	private void crearCoordenadas() {
 		for (int fila = 0; fila < COLUMNAS_TABLERO; fila++) {
 			for(int columna = 0; columna < FILAS_TABLERO; columna++) {
 				Coordenada coordenada = new Coordenada(fila, columna);
 				casilleros[columna][fila] = new Casillero(coordenada);
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
				if(casilleros[fila][columna].tieneBarcosNoDestruidos()) return true; 
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
 	
 	public static void reiniciarTablero() {
 		tablero = null;
 	}
 	
 	public void removerNave (Nave nave) {
 		for (Parte parte : nave.getPartes()) {
 			casilleros[parte.getPosicion().getX()][parte.getPosicion().getY()].removerNave(nave);
 		}
 	}
 	
 	public void reubicarNave (Nave nave) {
 		for (Parte parte : nave.getPartes()) {
 			casilleros[parte.getPosicion().getX()][parte.getPosicion().getY()].agregarNave(nave);
 		}
 	}
 	
 	public void agregarDisparo(Disparo disparo) {
 		casilleros[disparo.getCoordenada().getY()][disparo.getCoordenada().getX()].agregarDisparo(disparo);
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
 
 	@Override
 	public void vivir() {
 		for (Casillero[] filas : casilleros) {
 			for(Casillero casillero : filas) {
 				this.verificarDisparosEnCasillero(casillero);
 			}
 		}
 		this.eliminarNavesDestruidas();
 	}
 	
 	private void verificarDisparosEnCasillero(Casillero casillero){
 		ArrayList<Disparo> disparosRealizados = new ArrayList<Disparo>();
 		for (Disparo disparo : casillero.getDisparos()) {
 			if (disparo.debeExplotar()) {
 				DibujablesList.getDibujablesList().remover(MapsModeloVista.getMapsModeloVista().getMapDisparos().get(disparo));
 				MapsModeloVista.getMapsModeloVista().removerDisparo(disparo);
 				this.verificarSiHayNaveEnCasillero(casillero, disparo);
 				disparosRealizados.add(disparo);
 			}
 		}
 		this.eliminarDisparos(disparosRealizados,casillero);
 	}
 	
 	private void verificarSiHayNaveEnCasillero(Casillero casillero, Disparo disparo) {
 		HashSet<Nave> naves = this.buscarNaves(casillero, disparo.getRadio());
 		ArrayList<Coordenada> coordenadas = this.buscarCoordenadas(casillero.getCoordenada(), disparo.getRadio());
 		for (Nave nave : naves) {
 			for (Parte parte : nave.getPartes()) {
 				if (!parte.estaDestruida() && coordenadas.contains((parte.getPosicion()))) {
 					disparo.accionarMina(nave, parte);
					if (parte.estaDestruida()){
						Imagen parteDaniada = null;
						try {
							parteDaniada = new VistaParteDaniada(new URL("file:./images/fuego.png"), parte);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						MapsModeloVista.getMapsModeloVista().agregarParte(parte, parteDaniada);
						DibujablesList.getDibujablesList().agregar(parteDaniada);
 					}
 				}
 			}
 		}
 	}
 	
 	private void eliminarDisparos(ArrayList<Disparo> disparosRealizados, Casillero casillero) {
 		for (Disparo disparo : disparosRealizados) {
 			casillero.getDisparos().remove(disparo);
 		}
 	}
 	
 	private HashSet<Nave> buscarNaves(Casillero casillero, Integer radio){
 		Integer x = casillero.getCoordenada().getX();
 		Integer y = casillero.getCoordenada().getY();
 		HashSet<Nave> naves = new HashSet<Nave>();
 		naves.addAll(casillero.getNaves());
 		if (radio == 0) return naves;
 		for (int i = 1; i < radio+1; i++) {
 			if (x-i >= 0){
 				naves.addAll(this.casilleros[x-i][y].getNaves());
 				for (int j = 1; j < radio+1; j++) {
 					if (y-j >= 0) naves.addAll(this.casilleros[x-i][y-j].getNaves());
 					if (y+j <= FILAS_TABLERO-1) naves.addAll(this.casilleros[x-i][y+j].getNaves());
 				}
 			}
 			if (x+i <= COLUMNAS_TABLERO-1){
 				naves.addAll(this.casilleros[x+i][y].getNaves());
 				for (int j = 1; j < radio+1; j++) {
 					if (y-j >= 0) naves.addAll(this.casilleros[x+i][y-j].getNaves());
 					if (y+j <= FILAS_TABLERO-1) naves.addAll(this.casilleros[x+i][y+j].getNaves());					
 				}
 			}
 		}
 		for (int j = 1; j < radio+1; j++) {
 			if (y-j >= 0) naves.addAll(this.casilleros[x][y-j].getNaves());
 			if (y+j <= FILAS_TABLERO-1) naves.addAll(this.casilleros[x][y+j].getNaves());					
 		}
 		return naves;
 	}
 	
 	private ArrayList<Coordenada> buscarCoordenadas(Coordenada coordenada, Integer radio){
 		Integer x = coordenada.getX();
 		Integer y = coordenada.getY();
 		ArrayList<Coordenada> coordenadas = new ArrayList<Coordenada>();
 		coordenadas.add(coordenada);
 		if (radio == 0) return coordenadas;
 		for (int i = 1; i < radio+1; i++) {
 			if (x-i >= 0){
 				coordenadas.add(new Coordenada(x-i, y));
 				for (int j = 1; j < radio+1; j++) {
 					if (y-j >= 0) coordenadas.add(new Coordenada(x-i, y-j));
 					if (y+j <= FILAS_TABLERO-1) coordenadas.add(new Coordenada(x-i, y+j));
 				}
 			}
 			if (x+i <= COLUMNAS_TABLERO-1){
 				coordenadas.add(new Coordenada(x+i, y));
 				for (int j = 1; j < radio+1; j++) {
 					if (y-j >= 0) coordenadas.add(new Coordenada(x+i, y-j));
 					if (y+j <= FILAS_TABLERO-1) coordenadas.add(new Coordenada(x+i, y+j));					
 				}
 			}
 		}
 		for (int j = 1; j < radio+1; j++) {
 			if (y-j >= 0) coordenadas.add(new Coordenada(x, y-j));
 			if (y+j <= FILAS_TABLERO-1) coordenadas.add(new Coordenada(x, y+j));					
 		}
 		return coordenadas;
 	}
 
 	private void eliminarNavesDestruidas(){
 		HashSet<Nave> navesEnTablero = this.obtenerNavesEnTablero();
 		ArrayList<Nave> navesEliminar = new ArrayList<Nave>();
 		for (Nave nave : navesEnTablero) {
 			if (nave.estaDestruida()){
 				Imagen naveEliminar =  MapsModeloVista.getMapsModeloVista().getMapNaves().get(nave);
 				DibujablesList.getDibujablesList().remover(naveEliminar);
 				for (Parte parte : nave.getPartes()) {
 					Imagen parteEliminar = MapsModeloVista.getMapsModeloVista().getMapPartes().get(parte);
 					DibujablesList.getDibujablesList().remover(parteEliminar);
 					MapsModeloVista.getMapsModeloVista().removerParte(parte);
 				}
 				MapsModeloVista.getMapsModeloVista().removerNave(nave);
 				navesEliminar.add(nave);
 			}
 		}
 		for (Nave nave : navesEliminar) {
 			for (Casillero[] filas : casilleros) {
 				for(Casillero casillero : filas) {
 					if (casillero.getNaves().contains(nave)){
 						casillero.removerNave(nave);
 					}
 				}
 			}
 		}
 	}
 
 	private HashSet<Nave> obtenerNavesEnTablero(){
 		HashSet<Nave> naves = new HashSet<Nave>();
 		for (Casillero[] filas : casilleros) {
 			for(Casillero casillero : filas) {
 				naves.addAll(casillero.getNaves());
 			}
 		}
 		return naves;
 	}
 	
 }
