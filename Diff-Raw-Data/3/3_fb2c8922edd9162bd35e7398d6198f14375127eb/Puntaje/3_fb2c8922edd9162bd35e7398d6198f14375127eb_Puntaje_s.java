 package juego;
 
 import interfaces.Reseteable;
 
 public class Puntaje implements Reseteable {
 
     private int puntos;
 
     public Puntaje() {
             puntos = 10000;
     }
 
     public int puntos() {
             return puntos;
     }
 
     public void descontarPuntos(int puntosPerdidos) {
             puntos -= puntosPerdidos;        
     }
 
 	@Override
 	public void reset() {
		this.puntos();
 		
 	}
 
 }
