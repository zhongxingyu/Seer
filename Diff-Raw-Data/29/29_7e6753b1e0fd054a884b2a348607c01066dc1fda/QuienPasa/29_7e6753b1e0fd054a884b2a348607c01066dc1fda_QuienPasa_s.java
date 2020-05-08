 /**
  * 
  */
 package reglas;
 
 import models.*;
 
 /**
  * @author lopez
 * Esta Regla sirve para Generar un Pronostico en Base al Torneo de la quiniela para todos los resultados.
  * 
  */
 public class QuienPasa extends ReglaBase {
 

 	public QuienPasa(String Parametro) {
 		super(Parametro);
 	}
 
 	@Override
 	public long cacular(Pronostico Pronostico) {
 		return -1;
 	}
 	@Override
 	public long cacular(Torneo Torneo) {
		return -1;
 	}
 
 	@Override
 	public long Generar(Pronostico Pronostico) {
 		return -1;
 	}
 
 	@Override
 	public String IncluirJS() {
 		return null;
 	}
 
 
 }
