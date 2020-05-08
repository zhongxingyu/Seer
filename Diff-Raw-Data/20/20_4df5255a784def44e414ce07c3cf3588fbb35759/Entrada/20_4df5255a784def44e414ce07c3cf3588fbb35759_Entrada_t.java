 package principal;
 
 /**
  * Interface que controla los distintos tipos de Entrada.
  * @author RASS
  *
  */
 public interface Entrada {
 	
 	public abstract String getLinVenta();
 	
 	public abstract String getTarjetaFid();
 	
 	public abstract String getEmpleado();
 	
 	public abstract String getDia();
 	
 	public abstract String getHora();
 	
 	public abstract boolean isFinalFichero();
 }
