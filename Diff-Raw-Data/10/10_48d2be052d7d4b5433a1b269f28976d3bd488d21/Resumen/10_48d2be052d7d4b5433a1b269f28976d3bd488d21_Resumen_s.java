 /**
  * 
  */
 package utilitario;
 
 /**
  * @author Antonio Lopez
  *
  */
 public class Resumen implements Comparable<Resumen> {
 	
 	Long Posicion;
 	
 	models.Usuario Jugador;
 	
 	models.Pronostico Pronostico;
 	
 	Long Punto;
 	
 	Float PorcentajeTotal;
 	
 	Long Maximo;
 	
 	Long TotalMaximo;
 	
 	Boolean Participa;
 	
 	@Override
 	public int compareTo(Resumen o) {
		return this.Punto.compareTo(o.Punto);
 	}
 
 	public Long getPosicion() {
 		return Posicion;
 	}
 
 	public void setPosicion(Long posicion) {
 		Posicion = posicion;
 	}
 
 	public models.Usuario getJugador() {
 		return Jugador;
 	}
 
 	public void setJugador(models.Usuario jugador) {
 		Jugador = jugador;
 	}
 
 	public Long getPunto() {
 		return Punto;
 	}
 
 	public void setPunto(Long punto) {
 		Punto = punto;
 	}
 
 	public Float getPorcentajeTotal() {
 		return PorcentajeTotal;
 	}
 
 	public void setPorcentajeTotal(Float porcentajeTotal) {
 		PorcentajeTotal = porcentajeTotal;
 	}
 
 	public Long getMaximo() {
 		return Maximo;
 	}
 
 	public void setMaximo(Long maximo) {
 		Maximo = maximo;
 	}
 
 	public Boolean getParticipa() {
 		return Participa;
 	}
 
 	public void setParticipa(Boolean participa) {
 		Participa = participa;
 	}
 
 	public Long getTotalMaximo() {
 		return TotalMaximo;
 	}
 
 	public void setTotalMaximo(Long totalMaximo) {
 		TotalMaximo = totalMaximo;
 	}
 
 	public models.Pronostico getPronostico() {
 		return Pronostico;
 	}
 
 	public void setPronostico(models.Pronostico pronostico) {
 		Pronostico = pronostico;
 	}
 
 
 }
