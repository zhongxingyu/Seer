 /*
     Rockodroid: Music Player for android
     Copyright (C) 2012  Laura K. Salazar, Roberto R. De la Parra, Juan C. Orozco.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.rockodroid.model.queue;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Define el modo aleatorio de reproduccion.
  * Crea una lista desordenada con los índices de 0 hasta la cantidad indicada.
  * Una vez desordenado, gestiona el índice en forma secuencial.
  * 
  * @author Juan C. Orozco
  */
 public class ModoAleatorio implements ModoEleccion {
 
 	/** índice de elementos */
 	private ArrayList<Integer> elementos;
 
 	/** elemento actual */
 	private int actual;
 
 	public ModoAleatorio(int cantidad, int actual) { 
 		elementos = new ArrayList<Integer>(cantidad);
 		this.actual = actual;
 
 		crearIndiceAleatorio(cantidad);
 	}
 
 	/**
 	 * Crea el índice con la capacidad indicada en forma aleatoria.
 	 */
 	private void crearIndiceAleatorio(int cantidad) {
 		for(int i = 0; i < cantidad; i++) elementos.add(new Integer(i));
		if(cantidad > 0) desordenar();
 	}
 
 	/**
 	 * Desordena la lista de índices.
 	 * Establece al elemento actual como el primero de la lista.
 	 */
 	private void desordenar() {
 		Random rnd = new Random(System.currentTimeMillis());
 		Integer elementoAux;
 		int current = elementos.get(actual);
 		int indexAux, size = elementos.size();
 		for (int i = 0; i < size; i++) {
 			indexAux = rnd.nextInt(size);
 			elementoAux = elementos.get(i);
 			elementos.set(i, elementos.get(indexAux));
 			elementos.set(indexAux, elementoAux);
 		}
 		indexAux = elementos.indexOf(current);
 		elementos.set(indexAux, elementos.get(0));
 		elementos.set(0, current);
 	}
 
 	/**
 	 * Retorna el indice del siguiente elemento.
 	 * Si el actual elemento es el último retorna -1.
 	 */
 	public int getSiguiente() {
 		if(actual == elementos.size() - 1) return-1;
 		return elementos.get(++actual);
 	}
 
 	/**
 	 * Retorna el índice del anterior elemento.
 	 * Si el actual elemento es el primero retorna -1.
 	 */
 	public int getAnterior() {
 		if(actual -1 == 0) return -1;
 		return elementos.get(--actual);
 	}
 
 	/**
 	 * Obtiene el actual indice en la lista.
 	 */
 	public int getActual() {
 		return elementos.get(actual);
 	}
 
 	/**
 	 * Establece cual índice dentro del conjunto debe ser el actual.
 	 * A partir de este índice se ejecuta anterior y siguiente.
 	 */
 	public void setActual(int actual) {
 		int i = elementos.indexOf(actual);
 		if(i > -1)
 			actual = i;
 	}
 }
